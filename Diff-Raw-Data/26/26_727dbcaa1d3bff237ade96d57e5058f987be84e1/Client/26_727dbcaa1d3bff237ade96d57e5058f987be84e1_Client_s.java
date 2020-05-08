 package model;
 
 import java.io.File;
 import java.util.HashMap;
 
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import view.EditorView;
 import view.LinkView;
 import view.MainView;
 import view.OutlineView;
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
         Mailbox mainMailbox = new Mailbox();
         Mailbox editorMailbox = new Mailbox();
         Session session = new Session(mainMailbox, editorMailbox);
         Mediator mediator = new Mediator(mainMailbox, editorMailbox);
         
         Outliner outliner = new Outliner(session);
         
         // link the session and mediator together
         session.setMediator(mediator);
         mediator.setSession(session);
         mediator.setOutliner(outliner);
         
         // add each command into the HashMap, which will be passed into the GUI.
         // the GUI is able to access commands by their Class @formatter:off
         commands = new HashMap<Class<?>, Command>();
         commands.put(CmdAutoIndent.class     , new CmdAutoIndent    (mediator));
         commands.put(CmdAutoWrap.class       , new CmdAutoWrap      (mediator));
         commands.put(CmdClose.class          , new CmdClose         (mediator));
         commands.put(CmdCloseAll.class       , new CmdCloseAll      (mediator));
         commands.put(CmdIndent.class         , new CmdIndent        (mediator));
         commands.put(CmdInsert.class         , new CmdInsert        (mediator));
         commands.put(CmdNew.class            , new CmdNew           (mediator));
         commands.put(CmdOpen.class           , new CmdOpen          (mediator));
         commands.put(CmdSave.class           , new CmdSave          (mediator));
         commands.put(CmdSaveAll.class        , new CmdSaveAll       (mediator));
         commands.put(CmdSaveAs.class         , new CmdSaveAs        (mediator));
         commands.put(CmdWellFormed.class     , new CmdWellFormed    (mediator));
         commands.put(CmdSetCurrentTab.class  , new CmdSetCurrentTab (mediator));
         commands.put(CmdForceClose.class     , new CmdForceClose    (mediator));
         commands.put(CmdSetIndentSpace.class , new CmdSetIndentSpace(mediator));
         commands.put(CmdToggleRender.class   , new CmdToggleRender  (mediator));
         commands.put(CmdUndo.class           , new CmdUndo          (mediator));
         commands.put(CmdRedo.class           , new CmdRedo          (mediator));
         commands.put(CmdRefresh.class        , new CmdRefresh       (mediator));
         commands.put(CmdRefreshOutline.class , new CmdRefreshOutline(mediator));
         //@formatter:on
         
         try
         {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
         catch (ClassNotFoundException e)          { e.printStackTrace(); }
         catch (InstantiationException e)          { e.printStackTrace(); }
         catch (IllegalAccessException e)          { e.printStackTrace(); }
         catch (UnsupportedLookAndFeelException e) { e.printStackTrace(); }
         
         
         EditorView ev = new EditorView();
         LinkView lv = new LinkView();
         OutlineView ov = new OutlineView();
         
         // create the view
         MainView view = new MainView(ev, lv, ov);
         view.storeCommands(commands);
 
         // cause the view to update against the session and mediator
         session.addObserver(view);
         session.addObserver(ev);
         mainMailbox.addObserver(view);
         editorMailbox.addObserver(ev);
         
         outliner.addObserver(ov);
         
         if(args.length > 1)
         {
             String filename = args[0];
             
             File init = new File(filename);
             
             session.setInitialFile(init);
         }
         else
         {
             session.setInitialFile(null);
         }
 
         try
         {
             Thread.sleep(500);
         }
         catch (InterruptedException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         outliner.init();
         
         // show the view
         view.setVisible(true);
     }
 }
