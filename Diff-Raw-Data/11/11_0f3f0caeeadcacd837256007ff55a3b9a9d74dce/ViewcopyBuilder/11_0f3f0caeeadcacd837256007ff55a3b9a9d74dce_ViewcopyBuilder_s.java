 /*
  * The MIT License
  * 
  * Copyright (c) 2013 IKEDA Yasuyuki
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package jp.ikedam.jenkins.plugins.viewcopy_builder;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.io.PrintStream;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import jenkins.model.Jenkins;
 
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.w3c.dom.Document;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import hudson.DescriptorExtensionList;
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.AllView;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor;
 import hudson.model.View;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.util.ComboBoxModel;
 import hudson.util.FormValidation;
 import hudson.util.XStream2;
 
 /**
  * A build step to copy a view.
  * 
  * You can specify additional operations that is performed when copying,
  * and the operations can be extended with plugins using Extension Points.
  */
 public class ViewcopyBuilder extends Builder implements Serializable
 {
     private static final long serialVersionUID = 1598118718029050622L;
     
     private String fromViewName;
     
     /**
      * Returns the name of view to be copied from.
      * 
      * Variable expressions will be expanded.
      * 
      * @return the name of view to be copied from
      */
     public String getFromViewName()
     {
         return fromViewName;
     }
     
     private String toViewName;
     
     /**
      * Returns the name of view to be copied to.
      * 
      * Variable expressions will be expanded.
      * 
      * @return the name of view to be copied to
      */
     public String getToViewName()
     {
         return toViewName;
     }
     
     private boolean overwrite = false;
     
     /**
      * Returns whether to overwrite an existing view.
      * 
      * If the copied-to view is already exists,
      * "copy view" build step works as following depending on this value.
      * <table>
      *     <tr>
      *         <th>isOverwrite</th>
      *         <th>behavior</th>
      *     </tr>
      *     <tr>
      *         <td>true</td>
      *         <td>Overwrite the configuration of the existing view.</td>
      *     </tr>
      *     <tr>
      *         <td>false</td>
      *         <td>Build fails.</td>
      *     </tr>
      * </table>
      * 
      * @return whether to overwrite an existing view.
      */
     public boolean isOverwrite()
     {
         return overwrite;
     }
     
     private List<ViewcopyOperation> viewcopyOperationList;
     
     /**
      * Returns the list of operations.
      * 
      * @return the list of operations
      */
     public List<ViewcopyOperation> getViewcopyOperationList()
     {
         return viewcopyOperationList;
     }
     
     /**
      * Constructor to instantiate from parameters in the job configuration page.
      * 
      * When instantiating from the saved configuration,
      * the object is directly serialized with XStream,
      * and no constructor is used.
      * 
      * @param fromViewName   a name of a view to be copied from. may contains variable expressions.
      * @param toViewName     a name of a view to be copied to. may contains variable expressions.
      * @param overwrite     whether to overwrite if the view to be copied to is already existing.
      * @param viewcopyOperationList
      *                      the list of operations to be performed when copying.
      */
     @DataBoundConstructor
     public ViewcopyBuilder(String fromViewName, String toViewName, boolean overwrite, List<ViewcopyOperation> viewcopyOperationList)
     {
         this.fromViewName = StringUtils.trim(fromViewName);
         this.toViewName = StringUtils.trim(toViewName);
         this.overwrite = overwrite;
         this.viewcopyOperationList = viewcopyOperationList;
     }
     
     /**
      * Performs the build step.
      * 
      * @param build
      * @param launcher
      * @param listener
      * @return  whether the process succeeded.
      * @throws IOException
      * @throws InterruptedException
      * @see hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild, hudson.Launcher, hudson.model.BuildListener)
      */
     @Override
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
         throws IOException, InterruptedException
     {
         EnvVars env = build.getEnvironment(listener);
         PrintStream logger = listener.getLogger();
         
         if(StringUtils.isBlank(getFromViewName()))
         {
             logger.println("From View Name is not specified");
             return false;
         }
         if(StringUtils.isBlank(getToViewName()))
         {
             logger.println("To View Name is not specified");
             return false;
         }
         
         // Expand the variable expressions in view names.
         String fromViewNameExpanded = env.expand(getFromViewName());
         String toViewNameExpanded = env.expand(getToViewName());
         
         if(StringUtils.isBlank(fromViewNameExpanded))
         {
             logger.println("From View Name got to a blank");
             return false;
         }
         if(StringUtils.isBlank(toViewNameExpanded))
         {
             logger.println("To View Name got to a blank");
             return false;
         }
         
         logger.println(String.format("Copying %s to %s", fromViewNameExpanded, toViewNameExpanded));
         
         // Retrieve the view to be copied from.
         View fromView = Jenkins.getInstance().getView(fromViewNameExpanded);
         
         if(fromView == null)
         {
             logger.println("Error: Copying view is not found.");
             return false;
         }
         
         // Check whether the view to be copied to is already exists.
         View toView = Jenkins.getInstance().getView(toViewNameExpanded);
         if(toView != null){
             logger.println(String.format("Already exists: %s", toViewNameExpanded));
             if(!isOverwrite()){
                 return false;
             }
         }
         
        // Retrieve the config.xml of the view copied from.
        // TODO: what happens if this runs on a slave node?
         logger.println(String.format("Fetching configuration of %s...", fromViewNameExpanded));
         
         Document doc;
         try
         {
             doc = getViewConfigXmlDocument(fromView, logger);
         }
         catch (Exception e)
         {
             logger.println("Failed to retrieve configuration.");
             e.printStackTrace(logger);
             return false;
         }
         
         try
         {
             String xml = getDocumentXmlString(doc);
             logger.println("Original xml:");
             logger.println(xml);
         }
         catch(Exception e)
         {
             logger.println("Failed to convert the configuration to a string.");
             e.printStackTrace(logger);
             return false;
         }
         
         // Apply additional operations to the retrieved XML.
         if(getViewcopyOperationList() != null)
         {
             for(ViewcopyOperation operation: getViewcopyOperationList())
             {
                 if(!operation.isApplicable(fromView.getClass()))
                 {
                     logger.println(String.format("Operation %s cannot be applicable to %s(%s)",
                             operation.getClass().getName(),
                             fromView.getViewName(),
                             fromView.getClass().getName()
                     ));
                     return false;
                 }
                 doc = operation.perform(doc, env, logger);
                 if(doc == null)
                 {
                     return false;
                 }
             }
         }
         
         try
         {
             String xml = getDocumentXmlString(doc);
             logger.println("Copied xml:");
             logger.println(xml);
         }
         catch(Exception e)
         {
             logger.println("Failed to convert the configuration to a string.");
             e.printStackTrace(logger);
             return false;
         }
         
         InputStream sin;
         try
         {
             sin = getInputStreamFromDocument(doc);
         }
         catch(Exception e)
         {
             logger.println("Failed to create the stream from converted document.");
             e.printStackTrace(logger);
             return false;
         }
         
         if(toView == null)
         {
             logger.println(String.format("Creating %s", toViewNameExpanded));
             toView = View.createViewFromXML(toViewNameExpanded, sin);
             Jenkins.getInstance().addView(toView);
         }
         else
         {
             logger.println(String.format("Updating %s", toViewNameExpanded));
             toView.updateByXml(new StreamSource(sin));
         }
         
         // add the information of views copied from and to to the build.
         build.addAction(new CopiedviewinfoAction(fromView, toView));
         
         return true;
     }
     
     /**
      * Returns the configuration XML document of a view
      * 
      * @param view
      * @param logger
      * @return
      * @throws IOException 
      * @throws SAXException 
      * @throws ParserConfigurationException 
      */
     private Document getViewConfigXmlDocument(View view, final PrintStream logger)
             throws IOException, SAXException, ParserConfigurationException
     {
        XStream2 xStream2 = new XStream2();
         xStream2.omitField(View.class, "owner");
         
         PipedOutputStream sout = new PipedOutputStream();
         PipedInputStream sin = new PipedInputStream(sout);
         
         xStream2.toXML(view, sout);
         sout.close();
         
         DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = domFactory.newDocumentBuilder();
         builder.setErrorHandler(new ErrorHandler(){
             @Override
             public void warning(SAXParseException exception)
                     throws SAXException
             {
                 exception.printStackTrace(logger);
             }
             
             @Override
             public void error(SAXParseException exception) throws SAXException
             {
                 exception.printStackTrace(logger);
             }
             
             @Override
             public void fatalError(SAXParseException exception)
                     throws SAXException
             {
                 exception.printStackTrace(logger);
             }
         });
         return builder.parse(sin);
     }
     
     /**
      * Returns a InputStream of a XML document.
      * 
      * @param doc
      * @return
      * @throws TransformerException
      * @throws IOException
      */
     private InputStream getInputStreamFromDocument(Document doc)
             throws TransformerException, IOException
     {
         TransformerFactory tfactory = TransformerFactory.newInstance(); 
         Transformer transformer = tfactory.newTransformer(); 
         PipedOutputStream sout = new PipedOutputStream();
         PipedInputStream sin = new PipedInputStream(sout);
         transformer.transform(new DOMSource(doc), new StreamResult(sout)); 
         sout.close();
         
         return sin;
     }
 
     /**
      * Returns a XML string representing the passed XML document.
      * 
      * @param doc
      * @return
      * @throws TransformerException
      */
     private String getDocumentXmlString(Document doc) throws TransformerException
     {
         TransformerFactory tfactory = TransformerFactory.newInstance(); 
         Transformer transformer = tfactory.newTransformer(); 
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         
         StringWriter sw = new StringWriter();
         transformer.transform(new DOMSource(doc), new StreamResult(sw)); 
         
         return sw.toString();
     }
     
     /**
      * The internal class to work with views.
      * 
      * The following files are used (put in main/resource directory in the source tree).
      * <dl>
      *     <dt>config.jelly</dt>
      *         <dd>shown as a part of a job configuration page.</dd>
      * </dl>
      */
     @Extension
     public static final class DescriptorImpl extends BuildStepDescriptor<Builder>
     {
         /**
          * Returns the display name
          * 
          * Displayed in the "Add build step" dropdown in a job configuration page. 
          * 
          * @return the display name
          * @see hudson.model.Descriptor#getDisplayName()
          */
         @Override
         public String getDisplayName()
         {
             return Messages.ViewcopyBuilder_DisplayName();
         }
         
         /**
          * Test whether this build step can be applied to the specified job type.
          * 
          * This build step works for any type of jobs, for always returns true.
          * 
          * @param jobType the type of the job to be tested.
          * @return true
          * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
          */
         @SuppressWarnings("rawtypes")
         @Override
         public boolean isApplicable(Class<? extends AbstractProject> jobType)
         {
             return true;
         }
         
         /**
          * Returns all the available ViewcopyOperation.
          * 
          * Used for the contents of "Add Copy Operation" dropdown.
          * 
          * @return the list of ViewcopyOperation
          */
         public DescriptorExtensionList<ViewcopyOperation,Descriptor<ViewcopyOperation>> getViewcopyOperationDescriptors()
         {
             return ViewcopyOperation.all();
         }
         
         /**
          * Returns the list of views.
          * 
          * Used for the autocomplete of From View Name.
          * 
          * @return the list of views
          */
         public ComboBoxModel doFillFromViewNameItems()
         {
             ComboBoxModel ret = new ComboBoxModel();
             for(View view: Jenkins.getInstance().getViews())
             {
                 if(view instanceof AllView)
                 {
                     continue;
                 }
                 ret.add(view.getViewName());
             }
             
             return ret;
         }
         
         /**
          * Returns whether the value contains variable.
          * 
          * @param value value to be tested.
          * @return whether the value contains variable
          */
         private boolean containsVariable(String value)
         {
             if(StringUtils.isBlank(value) || !value.contains("$")){
                 // apparently contains no variable.
                 return false;
             }
             
             return true;
         }
         
         /**
          * Validate "From View Name" or "To View Name" field.
          * 
          * Returns as following:
          * <table>
          *     <tr>
          *         <th>viewName</th>
          *         <th>warnIfExists</th>
          *         <th>warnIfNotExists</th>
          *         <th>Returns</th>
          *     </tr>
          *     <tr>
          *         <td>Blank</th>
          *         <th>any</th>
          *         <th>any</th>
          *         <td>error</td>
          *     </tr>
          *     <tr>
          *         <td>value containing variables</th>
          *         <th>any</th>
          *         <th>any</th>
          *         <td>ok</td>
          *     </tr>
          *     <tr>
          *         <td>existing view</th>
          *         <th>false</th>
          *         <th>any</th>
          *         <td>ok</td>
          *     </tr>
          *     <tr>
          *         <td>existing view</th>
          *         <th>true</th>
          *         <th>any</th>
          *         <td>warning</td>
          *     </tr>
          *     <tr>
          *         <td>non existing view</th>
          *         <th>any</th>
          *         <th>false</th>
          *         <td>ok</td>
          *     </tr>
          *     <tr>
          *         <td>non existing view</th>
          *         <th>any</th>
          *         <th>true</th>
          *         <td>warning</td>
          *     </tr>
          * </table>
          * 
          * @param viewName
          * @param warnIfExists
          * @param warnIfNotExists
          * @return
          */
         public FormValidation doCheckViewName(String viewName, boolean warnIfExists, boolean warnIfNotExists)
         {
             viewName = StringUtils.trim(viewName);
             
             if(StringUtils.isBlank(viewName))
             {
                 return FormValidation.error(Messages.ViewcopyBuilder_ViewName_empty());
             }
             if(containsVariable(viewName))
             {
                 return FormValidation.ok();
             }
             
             View view = Jenkins.getInstance().getView(viewName);
             if(view != null)
             {
                 // view exists
                 if(warnIfExists)
                 {
                     return FormValidation.warning(Messages.ViewcopyBuilder_ViewName_exists());
                 }
             }
             else
             {
                 // view does not exist
                 if(warnIfNotExists)
                 {
                     return FormValidation.warning(Messages.ViewcopyBuilder_ViewName_notExists());
                 }
             }
             
             return FormValidation.ok();
         }
         
         /**
          * Validate "From View Name" field.
          * 
          * @param fromViewName
          * @return
          */
         public FormValidation doCheckFromViewName(@QueryParameter String fromViewName)
         {
             return doCheckViewName(fromViewName, false, true);
         }
         
         /**
          * Validate "To View Name" field.
          * 
          * @param toViewName
          * @param overwrite
          * @return FormValidation object.
          */
         public FormValidation doCheckToViewName(@QueryParameter String toViewName, @QueryParameter boolean overwrite)
         {
             return doCheckViewName(toViewName, !overwrite, false);
         }
     }
 }
