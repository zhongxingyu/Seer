 package gui;
 
 import scheme.ForeignScheme;
 import scheme.KawaScheme;
 import scheme.SISCScheme;
 import scheme.Scheme;
 
 import javax.swing.*;
 import javax.swing.text.BadLocationException;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.FileNotFoundException;
 import java.util.Stack;
 
 import net.infonode.docking.*;
 import net.infonode.docking.util.*;
 
 
 /**
  * Create a main frame.
  */
 public class MainFrame extends JFrame {
 	private static final long serialVersionUID = 2574330949324570164L;
 
 	// Woo singletons.
     static MainFrame me;
 
     // Things we may need access to.
     public RootWindow Root;
     public DocumentManager Documents;
     public SchemeTextArea History;
     public SchemeTextArea REPL;
     public Scheme SS;
 
     /**
      * Don't directly create this, use me().
      * Use this method to set it up though.
      */
     private MainFrame() {
         // Set frame options.
         setTitle("Wombat");
         try {
             setSize(
                     Integer.parseInt(Options.get("main.width")),
                     Integer.parseInt(Options.get("main.height"))
             );
         } catch (Exception ex) {
             setSize(600, 400);
         }
         setLayout(new BorderLayout(5, 5));
         setLocationByPlatform(true);
         
         // Wait for the program to end.
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 System.exit(0);
             }
         });
 
         // Set up the menus using the above definitions.
         setJMenuBar(MenuManager.menu());
         
         // Create the document window.
         TabWindow documents = new TabWindow();
         StringViewMap viewMap = new StringViewMap();
         Documents = new DocumentManager(viewMap, documents);
         Documents.New();
         
         // Create the REPL.
         History = new SchemeTextArea();
         History.setPreferredSize(new Dimension(100, getHeight() / 2 - 100));
         History.code.setEditable(false);
         
         REPL = new SchemeTextArea();
         REPL.setPreferredSize(new Dimension(100, 100));
         REPL.code.getInputMap().put(
                 KeyStroke.getKeyStroke("ENTER"),
                 new AbstractAction() {
                     private static final long serialVersionUID = 723647997099071931L;
 
 					public void actionPerformed(ActionEvent e) {
                         Stack<Character> brackets = new Stack<Character>();
                         for (char c : REPL.getText().toCharArray()) {
                             if (c == '(') brackets.push(')');
                             else if (c == '[') brackets.push(']');
                             else if (c == ')' || c == ']')
                                 if (!brackets.empty() && brackets.peek() == c)
                                     brackets.pop();
                                 else
                                     return;
                         }
 
                         if (brackets.empty()) {
                             doCommand(REPL.getText());
                             REPL.setText("");
                         } else {
                             try {
                                 REPL.code.getDocument().insertString(REPL.code.getCaretPosition(), SchemeTextArea.NL, null);
                             } catch (BadLocationException ble) {
                                 System.err.println("badwolf");
                             }
                             REPL.tab();
                         }
                     }
                 }
         );
         
         viewMap.addView("History", new View("History", null, History));
         viewMap.addView("REPL", new View("REPL", null, REPL));
         SplitWindow replSplit = new SplitWindow(false, viewMap.getView("History"), viewMap.getView("REPL"));
         
         // Put everything together.
         SplitWindow fullSplit = new SplitWindow(false, 0.6f, documents, replSplit);
         Root = DockingUtil.createRootWindow(new ViewMap(), true);
         Root.setWindow(fullSplit);
         
         add(Root);
         
         // Get a Scheme.
         String schemeVersion = Options.get("scheme", "kawa");
         if ("sisc".equals(schemeVersion.toLowerCase())) {
             SS = new SISCScheme();
         } else if ("kawa".equals(schemeVersion.toLowerCase())) {
         	SS = new KawaScheme();
         }
         else {
             try {
                 SS = new ForeignScheme(schemeVersion);
             } catch (FileNotFoundException e) {
             	ErrorFrame.log(schemeVersion + " not found, falling back to Kawa.");
                 SS = new KawaScheme();
             }
         }
 
         // Bind it to return responses to the History pane.
         Thread t = new Thread(new Runnable() {
             public void run() {
                 while (true) {
                     try {
                         Thread.sleep(50);
                     } catch (InterruptedException ie) {
                     }
 
                     if (SS.hasResponse())
                     {
                         String response = SS.nextResponse();
                         if (response != null)
                             History.append(response + "\n");
                     }
                 }
             }
         });
         t.setDaemon(true);
         t.start();
     }
 
     /**
      * Run a command.
      *
      * @param command The command to run.
      */
     void doCommand(String command) {
         command = command.trim();
         if (command.length() == 0)
             return;
 
         History.append("\n>>> " + command.replace("\n", "\n    ") + "\n");
         SS.doString(command);
     }
 
     /**
      * Access the frame.
      *
      * @return The singleton frame.
      */
     public static MainFrame me() {
         if (me == null)
             me = new MainFrame();
 
         return me;
     }
 }
