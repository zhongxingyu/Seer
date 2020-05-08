 package me.chenyi.mm;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowStateListener;
 import java.util.ArrayList;
 import java.util.Map;
 
 import me.chenyi.jython.Script;
 import me.chenyi.jython.action.InitSampleScriptAction;
 import me.chenyi.jython.action.ScriptAction;
 import me.chenyi.jython.ScriptTriggerType;
 import me.chenyi.jython.ScriptUtilities;
 import me.chenyi.jython.action.ScriptEditorAction;
 import me.chenyi.mm.action.ShowAboutAction;
 import me.chenyi.mm.action.ShowHelpAction;
 import me.chenyi.mm.ui.*;
 
 /**
  * Class description goes here
  *
  * @author $Author:$
  * @version $Revision:$
  */
 public class MovieManagerFrame extends JFrame
 {
     public static int INDEX_MAIN = 0;
     public static int INDEX_ADD = 1;
     public static int INDEX_FILTER = 2;
 
     private CurtainControlPanel controllerPanel;
 
     public MovieManagerFrame()
         throws HeadlessException
     {
         super();
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         setLocationRelativeTo(null);
         setSize(800, 600);
 
         addWindowListener(new WindowAdapter()
         {
             @Override
             public void windowClosing(WindowEvent e)
             {
                 super.windowClosing(e);
                 ScriptUtilities.executeScripts(ScriptTriggerType.OnAppExit);
             }
         });
 
         addWindowStateListener(new WindowStateListener()
         {
             @Override
             public void windowStateChanged(WindowEvent e)
             {
                 System.out.println("e = " + e);
             }
         });
 
         getContentPane().setLayout(new BorderLayout());
 
         final CurtainPanel2 curtainPanel = new CurtainPanel2();
 
         java.util.List<CurtainPagePanel> pages = new ArrayList();
         pages.add(new MovieMainPanel(curtainPanel));
         final MovieAddPanel movieAddPanel = new MovieAddPanel(curtainPanel);
         pages.add(movieAddPanel);
         MovieFilterPanel movieFilterPanel = new MovieFilterPanel(curtainPanel);
         pages.add(movieFilterPanel);
 
         curtainPanel.addComponentList(pages);
 
         getContentPane().add(curtainPanel, BorderLayout.CENTER);
 
         controllerPanel = new CurtainControlPanel(curtainPanel);
         setGlassPane(controllerPanel);
         controllerPanel.setVisible(true);
 
         setJMenuBar(createMenuBar());
 
//        setVisible(true);
 
         curtainPanel.setBottomComponent(INDEX_MAIN);//set the MovieMainPanel as the background panel.
 
 //                curtainPanel.setTopComponent(2);
     }
     
     private JMenuBar createMenuBar()
     {
         JMenuBar menuBar = new JMenuBar();
 
         JMenu pluginMenu = new JMenu("Plugin");
         menuBar.add(pluginMenu);
 
         pluginMenu.add(new JMenuItem(new InitSampleScriptAction()));
         pluginMenu.add(new JMenuItem(new ScriptEditorAction()));
 
         pluginMenu.add(new JSeparator());
 
         Map<String,Script> menuScriptMap = ScriptUtilities.getScriptsByTriggerType(ScriptTriggerType.MenuTrigger);
         for (Script script : menuScriptMap.values()) {
             pluginMenu.add(new JMenuItem(new ScriptAction(script)));
         }
 
         JMenu helpMenu = new JMenu("Help");
         menuBar.add(helpMenu);
 
         helpMenu.add(new JMenuItem(new ShowHelpAction()));
         helpMenu.add(new JMenuItem(new ShowAboutAction()));
 
         return menuBar;
     }
 }
