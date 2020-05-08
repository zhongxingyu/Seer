 package net.votefucker.nkvoter.applet;
 
 
 /*  The ConsoleApplet provides a basis for writing applets that simulate
     line-oriented console-type input/output.  The class ConsoleCanvas is
     used to provide the I/O routines; these routines type output into
     the ConsoleCanvas and read input typed there by the user.  See the
     files CanvasPanel.java and ConsoleCanvas.java for information.
     
     A console applet is set up to run one particular program.  The applet
     includes a button that the user presses to run the program (or to
     abort it when it is running).  It also includes a label, which by default
     is "Java I/O Console".  The default program, defined in this file,
     is a simple Hello World program.
     
     To write a new console applet, with a more interesting program, you
     should create a subclass of ConsoleApplet.  The subclass should
     override the method  program() , which represents the program to 
     be run.  The program should use the instance variable, console, to
     do input/output.  For example, console.putln("HelloWorld.")
     or N=console.getInt().  All the public methods of class ConsolePanel
     can be called in the program.
     
     (Of course, you could also just modify this file, instead of
     writing a subclass.)
     
     David Eck
     Department of Mathematics and Computer Science
     Hobart and William Smith Colleges
     Geneva, NY 14456
     eck@hws.edu
     
     July 17, 1998: Program was modified to be compliant with Java 1.1.
     December 9, 1997:  This is a modification of a version written
     on August 2, 1996.  (Subclasses of previous versions of the old applet
     should still work with this new version.)
 
 */
 
 
 
 import java.awt.*;
 import java.awt.event.*;
 import java.lang.Object;
 import net.votefucker.nkvoter.Version;
 import net.votefucker.nkvoter.Main;
 
 public class ConsoleApplet extends java.applet.Applet
                            implements Runnable, ActionListener {
     
    private static final Version VERSION = new Version(1, 2, 3);
    public static final Main NKVoter = new Main();
                            
    protected String title = "NKVoter "+VERSION;  
    
 
    protected String getTitle() {
        // Return a label to appears over the console;
        // If you want to change the label, override this
        // method to return a different string.
       return title;
    }
        
    protected  ConsolePanel console;  // console for use in program()
    
    protected void program() {  
           // The console-type program; override this in your sub-class
           // to be the program that you want your applet to run.
           // Use the variable "console", which is already defined,
           // to do inuput/output in your program.
       String arg[] = {""};
        try {
       if (useTor.getState()) {
          console.putln("Starting with Tor");
       }
       else {
          console.putln("Starting in Normal Mode");
       }    
       Main.main(arg);
             } catch (Exception e) {}
      
    }
    
 
    // The remainder of this file consists of implementation details that
    // you don't have to understand in order to write your own console applets.
    
    private Button runButton;  // user presses this to run the program
   private Checkbox useTor;
    
    private Thread programThread = null;     // thread for running the program; the run()
                                             //    method calls program()
    private boolean programRunning = false;
    private boolean firstTime = true;  //    set to false the first time program is run
    
    public void run() {   // just run the program()
       programRunning = true;
       program();
       programRunning = false;
       stopProgram();
    }
    
    synchronized private void startProgram() {
       runButton.setLabel("Abort Program");
       if (!firstTime) {
          console.clear();
          try { Thread.sleep(300); }  // some delay before restarting the program
          catch (InterruptedException e) { }
       }
       firstTime = false;
       programThread = new Thread(this);
       programThread.start();
    }
    
    synchronized private void stopProgram() {
       if (programRunning) {
          programThread.stop();
          try { programThread.join(1000); }
          catch (InterruptedException e) { }
       }
       console.clearBuffers();
       programThread = null;
       programRunning = false;
       runButton.setLabel("Run Again");
       runButton.requestFocus();
    }
 
    public void init() {
    
       setBackground(Color.black);
    
       setLayout(new BorderLayout(2,2));
       console = new ConsolePanel();
       add("Center",console);
       
       Panel temp = new Panel();
       temp.setBackground(Color.white);
       Label lab = new Label(getTitle());
       temp.add(lab);
       lab.setForeground(new Color(180,0,0));
       add("North", temp);
       
      runButton = new Button("Start Voting");
      useTor = new Checkbox("Use Tor",null,false);
       temp = new Panel();
       temp.setBackground(Color.white);
      temp.add(useTor);
       temp.add(runButton);
       runButton.addActionListener(this);
       add("South",temp);
       
    }
 
    public Insets getInsets() {
       return new Insets(2,2,2,2);
    }
       
    public void stop() {
       if (programRunning) {
          stopProgram();
          console.putln();
          console.putln("*** PROGRAM HALTED");
       }
    }
    
    synchronized public void actionPerformed(ActionEvent evt) { 
             // Only possible action is a click on the button.
             // If program is running, stop it; otherwise, run it.
       if (programThread != null) {
          stopProgram();
          console.putln();
          console.putln("*** PROGRAM ABORTED BY USER");
       }
       else
          startProgram();
    }
    
 }
