 /*
  * The MIT License
  * 
  * Copyright (c) 2012-2013 IKEDA Yasuyuki
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
 package jp.ikedam.jenkins.plugins.jobcopy_builder;
 
 import java.io.PrintStream;
 import java.io.Serializable;
import java.util.regex.Pattern;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.model.Descriptor;
 import hudson.util.FormValidation;
 
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 
 /**
  * Replace the string in the configuration.
  */
 public class ReplaceOperation extends AbstractXmlJobcopyOperation implements Serializable
 {
     private static final long serialVersionUID = 1L;
     
     /**
      * The internal class to work with views.
      * 
      * The following files are used (put in main/resource directory in the source tree).
      * <dl>
      *     <dt>config.jelly</dt>
      *         <dd>shown in the job configuration page, as an additional view to a Jobcopy build step.</dd>
      * </dl>
      */
     @Extension
     public static class DescriptorImpl extends Descriptor<JobcopyOperation>
     {
         /**
          * Returns the string to be shown in a job configuration page,
          * in the dropdown of &quot;Add Copy Operation&quot;.
          * 
          * @return the display name
          * @see hudson.model.Descriptor#getDisplayName()
          */
         @Override
         public String getDisplayName()
         {
             return Messages.ReplaceOperation_DisplayName();
         }
         
         /**
          * Validate the value input to "From String"
          * 
          * @param fromStr
          * @return FormValidation object.
          */
         public FormValidation doCheckFromStr(@QueryParameter String fromStr, @QueryParameter boolean expandFromStr)
         {
             if(StringUtils.isEmpty(fromStr))
             {
                 return FormValidation.error(Messages.ReplaceOperation_fromStr_empty());
             }
             
             String trimmed = StringUtils.trim(fromStr);
             if(!trimmed.equals(fromStr))
             {
                 return FormValidation.warning(Messages.ReplaceOperation_fromStr_enclosedWithBlank());
             }
             
             return FormValidation.ok();
         }
         
         /**
          * Validate the value input to "From String"
          * 
          * @param fromStr
          * @return FormValidation object.
          */
         public FormValidation doCheckToStr(@QueryParameter String toStr, @QueryParameter boolean expandToStr)
         {
             // Nothing to check.
             return FormValidation.ok();
         }
     }
     
     private String fromStr;
     
     /**
      * Returns the string to be replaced.
      * 
      * @return the string to be replaced.
      */
     public String getFromStr()
     {
         return fromStr;
     }
     
     private boolean expandFromStr;
     
     /**
      * Returns whether expand variables in fromStr.
      * 
      * @return whether expand variables in fromStr.
      */
     public boolean isExpandFromStr(){
         return expandFromStr;
     }
     
     private String toStr;
     
     /**
      * Returns the string to be replaced with.
      * 
      * @return the string to be replaced with.
      */
     public String getToStr()
     {
         return toStr;
     }
     
     private boolean expandToStr;
     
     /**
      * Returns whether expand variables in toStr.
      * 
      * @return whether expand variables in toStr.
      */
     public boolean isExpandToStr()
     {
         return expandToStr;
     }
     
     /**
      * Constructor to instantiate from parameters in the job configuration page.
      * 
      * When instantiating from the saved configuration,
      * the object is directly serialized with XStream,
      * and no constructor is used.
      * 
      * @param fromStr           the string to be replaced.
      * @param expandFromStr     whether expand variables in fromStr.
      * @param toStr             the string to be replaced with.
      * @param expandToStr       whether expand variables in toStr.
      */
     @DataBoundConstructor
     public ReplaceOperation(String fromStr, boolean expandFromStr, String toStr, boolean expandToStr)
     {
         this.fromStr = fromStr;
         this.expandFromStr = expandFromStr;
         this.toStr = toStr;
         this.expandToStr = expandToStr;
     }
     
     
     /**
      * Returns modified XML Document of the job configuration.
      * 
      * Replace the strings in the job configuration: 
      * only applied to strings in text nodes, so the XML structure is never destroyed. 
      * 
      * @param doc       XML Document of the job to be copied (job/NAME/config.xml)
      * @param env       Variables defined in the build.
      * @param logger    The output stream to log.
      * @return          modified XML Document. Return null if an error occurs.
      * @see jp.ikedam.jenkins.plugins.jobcopy_builder.AbstractXmlJobcopyOperation#perform(org.w3c.dom.Document, hudson.EnvVars, java.io.PrintStream)
      */
     @Override
     public Document perform(Document doc, EnvVars env, PrintStream logger)
     {
         String fromStr = getFromStr();
         String toStr = getToStr();
         
         if(StringUtils.isEmpty(fromStr))
         {
             logger.println("From String is empty");
             return null;
         }
         if(toStr == null)
         {
             toStr = "";
         }
         String expandedFromStr = isExpandFromStr()?env.expand(fromStr):fromStr;
         String expandedToStr = isExpandToStr()?env.expand(toStr):toStr;
         if(StringUtils.isEmpty(expandedFromStr))
         {
             logger.println("From String got to be empty");
             return null;
         }
        
        // String.replace treats the parameter as a regular expression,
        // so the escaping of special characters of regular expressions are needed.
        String pattern = Pattern.quote(expandedFromStr);
         
         logger.print("Replacing: " + expandedFromStr + " -> " + expandedToStr);
         try
         {
             // Retrieve all text nodes.
             NodeList textNodeList = getNodeList(doc, "//text()");
             
             // Perform replacing to all text nodes.
             // NodeList does not implement Collection, and foreach is not usable.
             for(int i = 0; i < textNodeList.getLength(); ++i)
             {
                 Node node = textNodeList.item(i);
                node.setNodeValue(node.getNodeValue().replaceAll(pattern, expandedToStr));
             }
             logger.println("");
             
             return doc;
         }
         catch(Exception e)
         {
             logger.print("Error occured in XML operation");
             e.printStackTrace(logger);
             return null;
         }
     }
 }
 
