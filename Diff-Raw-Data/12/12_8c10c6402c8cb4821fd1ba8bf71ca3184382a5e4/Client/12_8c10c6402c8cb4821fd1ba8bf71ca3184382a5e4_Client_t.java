 package model;
 
 import java.util.HashMap;
 
 import view.EditorView;
 import cmd.*;
 
 /**
  * The Client is the main entry point into the htmledit program. It has the main
  * function which starts up the components and wires them together
  * 
  * @author Steven Kroh
  */
 public class Client
 {
     /**
      * A hash map relating command Class objects to the instantiated commands
      */
     private static HashMap<Class<?>, Command> commands;
 
     public static void main(String[] args)
     {
         // generate the base model objects
        Mailbox mailbox = new Mailbox();
        Session session = new Session(mailbox);
        Mediator mediator = new Mediator(mailbox);
         
         // link the session and mediator together
         session.setMediator(mediator);
         mediator.setSession(session);
         
         // add each command into the HashMap, which will be passed into the GUI.
         // the GUI is able to access commands by their Class @formatter:off
         commands = new HashMap<Class<?>, Command>();
         commands.put(CmdAutoIndent.class, new CmdAutoIndent (mediator));
         commands.put(CmdAutoWrap.class,   new CmdAutoWrap   (mediator));
         commands.put(CmdClose.class,      new CmdClose      (mediator));
         commands.put(CmdCloseAll.class,   new CmdCloseAll   (mediator));
         commands.put(CmdIndent.class,     new CmdIndent     (mediator));
         commands.put(CmdInsert.class,     new CmdInsert     (mediator));
         commands.put(CmdNew.class,        new CmdNew        (mediator));
         commands.put(CmdOpen.class,       new CmdOpen       (mediator));
         commands.put(CmdSave.class,       new CmdSave       (mediator));
         commands.put(CmdSaveAll.class,    new CmdSaveAll    (mediator));
         commands.put(CmdSaveAs.class,     new CmdSaveAs     (mediator));
         commands.put(CmdWellFormed.class, new CmdWellFormed (mediator));
         //@formatter:on
         
         commands.put(CmdSetIndentSpace.class, new CmdSetIndentSpace(mediator));
         
        
        
         
         // create the view
         EditorView view = new EditorView();
        view.storeCommands(commands);
         
         // cause the view to update against the session and mediator
         session.addObserver(view);
         mediator.addObserver(view);
         
         // show the view
         view.setVisible(true);
     }
 }
