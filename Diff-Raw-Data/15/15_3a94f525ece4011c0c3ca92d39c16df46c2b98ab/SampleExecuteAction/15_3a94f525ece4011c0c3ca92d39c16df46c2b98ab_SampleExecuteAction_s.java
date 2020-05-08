 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: SampleExecuteAction.java,v 1.1 2004-04-03 23:16:35 shahid.shah Exp $
  */
 
 package app.form.exec.action;
 
 import java.io.IOException;
 import java.io.Writer;
 
 /**
  * Sample bean that demonstrates how to perform an action after a dialog has accepted data entry. The "action" dialog
  * type will automatically figure out what the Action class needs by way of the constructor, what the execute method
  * signature is, and if there is any validation method available. The primary benefit of an action class like this is
  * that it is a "normal" java bean and does not need to have any imports from com.netspective.* packages unless the
  * programmer desires it. When no reference is made to the Sparx container classes or to any Servlet packages then the
  * action dialog may be easier to unit test as an independent piece of business logic.
  *
  * Constructors may be of the following form:
  *     SampleExecuteAction() -- default constructor
  *
  * If any of the following setXXX() methods are found, they will be called automatically to provide active values
  *     setActionDialogContext(ActionDialogContext) -- action class will be provided the active dialog context
  *     setServletEnvironment(Servlet, ServletRequest, ServletResponse) -- action class will be provided the active servlet
  *     setConnectionContext(ConnectionContext) -- if the action needs access to a connection context (in transaction mode)
  *     setConnection(Connection) -- if the action needs access to a JDBC connection (in transaction mode)
 *     * if either setConnectionContext or setConnection are found, an optional getDataSourceId() may be provided by the
  *       action class to supply the data source id for the connection that should be set
  *     * when a connection is provided (either using ConnectionContext or Connection) then the framework is responsible
  *       for closing it -- but, the programmer is reponsible for commit and rollback of the transaction
  *
  * Execute method (the method may have any name, "execute" is default) may be of the following form:
  *     public void execute(Writer) -- provides access to the output stream to be written to, no redirect required
  *     public String execute(Writer) -- provides access to the output stream to be written to, allows for redirect
  *     public void execute() -- an action will be performed and the navigation page will handle the redirect (no writer needed)
  *     public String execute() -- an action will be performed and the return value is either a string or a value source
  *                                spec telling the framework the next action. because the return value will be checked for
  *                                ':' you will need to escape ':' as '\:' when ':' is part of the return value.
  *
  * Validate method (may have any name, but "isValid" is the default) may be of the following form:
  *     public boolean isValid() -- call will be made and if false is returned a standard message will be displayed
  *     public boolean isValid(List) -- call will be made and if false programmer can supply messages by adding to list
  */
 public class SampleExecuteAction
 {
     private String fullName;
     private int age = Integer.MIN_VALUE;
 
     public SampleExecuteAction()
     {
     }
 
     public String getFullName()
     {
         return fullName;
     }
 
     /**
      * This method will be called by the Sparx action dialog handler to set the full_name field.
      * @param fullName The name of the person
      */
     public void setFullName(String fullName)
     {
         this.fullName = fullName;
     }
 
     public int getAge()
     {
         return age;
     }
 
     /**
      * This method will be called by the Sparx action dialog handler to set the age field. Note that Sparx will figure
      * ou that the age mutator is of type int and make the appropriate type conversion
      * @param age The age of the person
      */
     public void setAge(int age)
     {
         this.age = age;
     }
 
     /**
      * The dialog execute method is called as soon as all data is entered and validated. This method is guaranteed to
      * only be called when all fields' data is valid. We are not returning a value because we want the container to
      * automatically manage the next action (in our case, staty where its at to show our message).
      */
     public void execute(Writer writer) throws IOException
     {
         writer.write("Welcome to NEFS <b>" + getFullName() + "</b>. ");
         writer.write("You are running the action class called <b><code>"+ getClass().getName() +"</code></b>.");
 
         // because we're handling both forms (exec1a and exec1b we need to check if we're running the second form)
         if(getAge() > Integer.MIN_VALUE)
             writer.write("<p>You are <b>"+ getAge() + "</b> " + "years old.");
 
         writer.write("<p>");
         writer.write("This code demonstrated how you can take two pieces of data and execute an arbritrary class by " +
                      "using the 'action' tag of the 'action' dialog type. ");
         writer.write("This method of execution is called <i>Dialog Action</i>. ");
 
         writer.write("You may review the code at <code>/WEB-INF/classes/app/form/exec/action/SampleExecuteAction.java</code>.");
     }
 }
