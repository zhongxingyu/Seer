 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: AntBuildDialog.java,v 1.4 2003-07-21 15:04:11 shahid.shah Exp $
  */
 
 package com.netspective.sparx.ant;
 
 import java.io.Writer;
 import java.io.IOException;
 import java.io.File;
 import java.io.PrintStream;
 import java.io.ByteArrayOutputStream;
 
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.Log;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.BuildLogger;
 import org.apache.tools.ant.NoBannerLogger;
 import org.apache.tools.ant.Main;
 
 import com.netspective.sparx.console.form.ConsoleDialog;
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.DialogExecuteException;
 import com.netspective.sparx.form.field.DialogFields;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.sparx.form.field.type.SelectField;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.ant.AntProject;
 import com.netspective.sparx.theme.Theme;
 import com.netspective.commons.value.Value;
 import com.netspective.commons.value.source.StaticValueSource;
 
 public class AntBuildDialog extends ConsoleDialog
 {
     private static final Log log = LogFactory.getLog(AntBuildDialog.class);
     public static final String REQATTRPARAMNAME_ACTIVE_ANT_BUILD_DIALOG = "ant-build-dialog";
 
     private AntTargetsDocumentationPanel activeTargetsDocumentationPanel = new AntTargetsDocumentationPanel();
 
     /**
      * If a target name starts with one of characters in this string, it will be considered a "private target" and not
      * shown to the user.
      */
     private AntProject antProject;
 
     public AntProject getAntProject()
     {
         return antProject;
     }
 
     public void setAntProject(AntProject antProject)
     {
         this.antProject = antProject;
     }
 
     public void finalizeContents(NavigationContext nc)
     {
         super.finalizeContents(nc);
         DialogFields fields = getFields();
         fields.getByName("project-id").setDefault(new StaticValueSource(antProject.getId()));
         fields.getByName("project-file").setDefault(antProject.getFile());
         ((SelectField) fields.getByName("target")).setChoices(antProject.getTargets());
     }
 
     public void populateValues(DialogContext dc, int formatType)
     {
         super.populateValues(dc, formatType);
         if(dc.isInitialEntry() && formatType == DialogContext.STATECALCSTAGE_INITIAL)
         {
             Project project = antProject.getProject(dc);
             dc.getFieldStates().getState("target").getValue().setTextValue(project.getDefaultTarget());
 
             DialogFields fields = getFields();
             for(int i = 0; i < fields.size(); i++)
             {
                 DialogField field = fields.get(i);
                 if(field instanceof AntBuildDialogPropertyField)
                 {
                     AntBuildDialogPropertyField propertyField = (AntBuildDialogPropertyField) field;
                     String propertyName = propertyField.getProperty();
                     String propertyValue = project.getUserProperty(propertyName);
                     if(propertyValue == null)
                         propertyValue = project.getProperty(propertyName);
                     if(propertyValue != null)
                         dc.getFieldStates().getState(propertyField).getValue().setTextValue(propertyValue);
                 }
             }
         }
     }
 
     public void execute(Writer writer, DialogContext dc) throws IOException, DialogExecuteException
     {
         DialogContext.DialogFieldStates states = dc.getFieldStates();
 
         Value projectValue = states.getState("project-file").getValue();
         Value targetValue = states.getState("target").getValue();
         File projectFile = new File(projectValue.getTextValue());
 
         Project antProject = AntProject.getConfiguredProject(projectFile);
 
         ByteArrayOutputStream ostream = new ByteArrayOutputStream();
         PrintStream pstream = new PrintStream(ostream);
 
         BuildLogger logger = new NoBannerLogger();
         logger.setMessageOutputLevel(Project.MSG_INFO);
         logger.setOutputPrintStream(pstream);
         logger.setErrorPrintStream(pstream);
 
        PrintStream saveOut = System.out;
        PrintStream saveErr = System.err;
        System.setOut(pstream);
        System.setErr(pstream);

         antProject.addBuildListener(logger);
        Exception exceptionThrown = null;
         try
         {
             DialogFields fields = getFields();
             for(int i = 0; i < fields.size(); i++)
             {
                 DialogField field = fields.get(i);
                 if(field instanceof AntBuildDialogPropertyField)
                 {
                     AntBuildDialogPropertyField propertyField = (AntBuildDialogPropertyField) field;
                     String propertyName = propertyField.getProperty();
                     String propertyValue = dc.getFieldStates().getState(propertyField).getValue().getTextValue();
                     if(propertyValue != null)
                         antProject.setUserProperty(propertyName, propertyValue);
                 }
             }
             antProject.executeTarget(targetValue.getTextValue());
         }
         catch (Exception e)
         {
            exceptionThrown = e;
         }
 
         writer.write("<div class='textbox'>"+ Main.getAntVersion() +"<p><pre>");
         writer.write(ostream.toString());
         writer.write("</pre>");

        if(exceptionThrown != null)
            renderFormattedExceptionMessage(writer, exceptionThrown);

        System.setOut(saveOut);
        System.setErr(saveErr);
     }
 
     public void render(Writer writer, NavigationContext nc, Theme theme, int flags) throws IOException
     {
         super.render(writer, nc, theme, flags);
         File projectFile = new File(antProject.getFile().getTextValue(nc));
         if(! projectFile.exists())
             return;
 
         writer.write("<p>");
         nc.getRequest().setAttribute(REQATTRPARAMNAME_ACTIVE_ANT_BUILD_DIALOG, this);
         activeTargetsDocumentationPanel.render(writer, nc, theme, flags);
         nc.getRequest().removeAttribute(REQATTRPARAMNAME_ACTIVE_ANT_BUILD_DIALOG);
     }
 }
