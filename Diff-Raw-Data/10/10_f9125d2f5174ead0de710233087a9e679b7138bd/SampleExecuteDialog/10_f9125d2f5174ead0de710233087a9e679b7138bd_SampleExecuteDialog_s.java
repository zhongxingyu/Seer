 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: SampleExecuteDialog.java,v 1.1 2003-12-11 17:37:49 shahid.shah Exp $
  */
 
 package app.form.exec.inheritance;
 
 import java.io.Writer;
 import java.io.IOException;
 
 import com.netspective.sparx.form.Dialog;
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.DialogExecuteException;
 import com.netspective.sparx.form.DialogsPackage;
 import com.netspective.sparx.Project;
 
 /**
  * Sample dialog that demonstrates how to obtain input by using multiple <dialog> declarations in project.xml yet
  * creating a single custom dialog to perform the form actions.
  */
 public class SampleExecuteDialog extends Dialog
 {
     public SampleExecuteDialog(Project project)
     {
         super(project);
     }
 
     public SampleExecuteDialog(Project project, DialogsPackage pkg)
     {
         super(project, pkg);
     }
 
     /**
      * The dialog execute method is called as soon as all data is entered and validated. This method is guaranteed to
      * only be called when all fields' data is valid.
      * @param writer The writer where HTML and other output goes -- this is most likely the same as the response stream
      *               but sometimes may be some other writer (if output is being redirected to a file or something).
      * @param dc The {@see DialogContext} contains all the current user's field values, flags, and state-related data.
      * @throws java.io.IOException
      * @throws com.netspective.sparx.form.DialogExecuteException
      */
     public void execute(Writer writer, DialogContext dc) throws IOException, DialogExecuteException
     {
         writer.write("Welcome to NEFS <b>" + dc.getFieldStates().getState("full_name").getValue().getTextValue() +
                      "</b>. ");
         writer.write("You are running the dialog called <b>"+ dc.getDialog().getName() +"</b> in package <b>"+
                      dc.getDialog().getNameSpace().getNameSpaceId() +"</b>.");
 
        // because we're handling both forms (sample1a and sample1b we need to check if we're running the second form)
        if(dc.getDialog().getQualifiedName().equals("inheritance.sample1b"))
             writer.write("<p>You are <b>"+ dc.getFieldStates().getState("age").getValue().getIntValue() + "</b> " +
                          "years old.");
 
         writer.write("<p>");
         writer.write("This code demonstrated how you can take two pieces of data and execute an arbritrary class by " +
                      "setting the 'class' attribute of the dialog. ");
         writer.write("This method of execution is called <i>Dialog Inheritance</i>. ");
 
        String relativePath = "/WEB-INF/classes/app/inheritance/SampleExecuteDialog.java";
         String sourceFileLink = dc.getConsoleFileBrowserLinkShowAlt(
                                    dc.getServlet().getServletConfig().getServletContext().getRealPath(relativePath),
                                    relativePath);
         writer.write("You may review the code at " + sourceFileLink + ".");
         writer.write("<p>");
         writer.write("<a href=\""+ dc.getNavigationContext().getActivePage().getName() +"\">Clear the dialog</a>");
     }
 }
