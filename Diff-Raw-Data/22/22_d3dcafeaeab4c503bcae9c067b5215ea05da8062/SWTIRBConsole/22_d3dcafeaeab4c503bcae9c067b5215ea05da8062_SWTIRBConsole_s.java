 package org.amanzi.scripting.jirb;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Insets;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.swing.BorderFactory;
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 
 import org.amanzi.scripting.jruby.ScriptUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.awt.SWT_AWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.jruby.Ruby;
 import org.jruby.RubyInstanceConfig;
 import org.jruby.demo.TextAreaReadline;
 import org.jruby.internal.runtime.ValueAccessor;
 import org.jruby.runtime.builtin.IRubyObject;
 
 /**
  * This class provides for the embedding of a swing based IRB console in
  * an SWT composite or shell. If it is constructed with a shell, the shell
  * is closed on exiting the interpreter, otherwise it is possible to
  * restart the interpreter by calling the restart() method.
  * 
  * @author craig
  */
 public class SWTIRBConsole extends Composite {
     private static final long serialVersionUID = 3746242973444417387L;
     private JEditorPane editorPane = null;
 	private TextAreaReadline tar = null;
 	private Thread irbThread = null;
     private IRBConfigData irbConfig = null;
     private RubyInstanceConfig config = null;
     private Ruby runtime = null;
 	
     /**
      * Construct an instance of this class, which will also result in
      * the IRB interpreter starting and running in a separate thread.
      * @param parent
      */
 	public SWTIRBConsole( Composite parent, IRBConfigData configData ) {
 		super(parent, SWT.EMBEDDED);
 		this.irbConfig = configData;
 		if(irbConfig==null) irbConfig = new IRBConfigData() {{
 	        setTitle(" Welcome to the JRuby IRB Console \n\n");
 	        //setExtraRequire(new String[]{"awescript"});
 	    }};
 
 		java.awt.Frame irbFrame = SWT_AWT.new_Frame(this);
 		java.awt.Panel panel = new java.awt.Panel(new java.awt.BorderLayout());
 		irbFrame.add(panel);
 		JPanel irbPanel = new JPanel();
 		panel.add(irbPanel);
 
 		this.setLayout(new GridLayout(1, false));
 		this.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Shell shell = null;
 		if(parent instanceof Shell) shell = (Shell)parent;
 		editorPane = makeEditorPane(irbPanel);
         tar = new TextAreaReadline(editorPane, irbConfig.getTitle());
         irbThread = start_irb_panel(shell);
 	}
 	
 	public SWTIRBConsole( Composite parent ) {
         this(parent,null);
     }
 
     public void shutdown(){
 		tar.shutdown();
 		tar = null;
 		irbThread = null;
 	}
 
 	/**
 	 * This method can be used to create a standalone window that runs the
 	 * IRBConsole it it. It will create an instance of SWTIRBConsole with
 	 * a new shell (window) based on the provided display, and then register a shutdown
 	 * hook for that shell. To close the shell, either type 'exit' in the
 	 * IRBConsole or click on the window close button.
 	 * 
 	 * @param display
 	 */
 	public static void start(final Display display) {
 		final Shell shell = new Shell(display);
 		shell.setText("JRuby IRB-Console in SWT Shell");
 
 		final SWTIRBConsole ex = new SWTIRBConsole(shell);
 		
 		Listener exitListener = new Listener() {
 			public void handleEvent(Event e) {
                 ex.shutdown();
 				shell.dispose();
 			}
 		};
 		shell.addListener(SWT.Close, exitListener);
 
 		GridLayout layout = new GridLayout(1, false);
 		layout.marginWidth = layout.marginHeight = 0;
 		layout.horizontalSpacing = layout.verticalSpacing = 1;
 		shell.setLayout(layout);
 
 		shell.open();
 	}
 	
 	/**
 	 * This is the primary code for setting up the contents of the IRBConsole
 	 * using swing code based directly on the JRuby org.jruby.demo.IRBConsole
 	 * class. It constructs an instance of org.jruby.demo.TextAreaReadline which
 	 * actually does the work of interfacing the Ruby interpreter with the I/O
 	 * of the text area. And finally it starts a thread to run the interpreter in.
 	 * 
 	 * @param console JPanel to contain the JTextPanel with the irb console 
 	 * @param shell optional SWT shell to be closed if the user terminates the IRB session 
 	 * @return
 	 */
 	private Thread start_irb_panel(final Shell shell) {
         config = new RubyInstanceConfig() {{
         	setJRubyHome(ScriptUtils.getJRubyHome());	// this helps online help work
             setInput(tar.getInputStream());
             setOutput(new PrintStream(tar.getOutputStream()));
             setError(new PrintStream(tar.getOutputStream()));
             setObjectSpaceEnabled(true); // useful for code completion inside the IRB
             
             // The following modification forces IRB to ignore the fact that inside eclipse
             // the STDIN.tty? returns false, and IRB must continue to use a prompt
             List<String> argList = new ArrayList<String>();
             argList.add("--prompt-mode");
             argList.add("default");
             argList.add("--readline");
             setArgv(argList.toArray(new String[0]));
         }};
         runtime = Ruby.newInstance(config);
 
         // Now set the process ID inside ruby to the same as the java system id
         runtime.getGlobalVariables().defineReadonly("$$", new ValueAccessor(runtime.newFixnum(System.identityHashCode(runtime))));
         // Add all required global variables for easy ruby access back into the eclipse workspace
         irbConfig.addExtraGlobal("console", tar);   // This gives us access to the console config itself, including the colors used, should we wish to configure those
         for(Entry<String,Object> entry:irbConfig.getExtraGlobals().entrySet()){
             IRubyObject rubyObj = org.jruby.javasupport.JavaEmbedUtils.javaToRuby(runtime, entry.getValue());
             String key = entry.getKey();
             if(!key.startsWith("$")) key = "$"+key;
             runtime.getGlobalVariables().define(key, new ValueAccessor(rubyObj));
         }
         // Setup the ruby loadpaths so we can find any of the extra requires defined
         runtime.getLoadService().init(ScriptUtils.makeLoadPath(irbConfig.getExtraLoadPath()));
         // Connect the ruby runtime into the console
         tar.hookIntoRuntime(runtime);
 
         // Start and run the thread the will run the IRB
         Thread t2 = new Thread("IRBConsole") {
             public void run() {
                 // set thread context JRuby classloader here, for the main thread
                 try {
                     Thread.currentThread().setContextClassLoader(runtime.getJRubyClassLoader());
                 } catch (SecurityException se) {
                     // can't set TC classloader
                     if (runtime.getInstanceConfig().isVerbose()) {
                         System.err.println("WARNING: Security restrictions disallowed setting context classloader for main thread.");
                     }
                 }
                 IRubyObject result = null;
                for(String scriptlet:irbConfig.getStartScriptlets()){
                    if(!(result = runtime.evalScriptlet(scriptlet)).isTrue()){
                    	System.err.println("Error running scriptlet '"+scriptlet+"': "+result);
                    	runtime.getErr().println("Error running scriptlet '"+scriptlet+"': "+result);
                    	break;
                     }
                 }
                 if(shell!=null){    // we are running in a standalone shell, so close it down
 	                shell.getDisplay().asyncExec(new Runnable(){
 						public void run() {
 							tar.shutdown();
 							shell.dispose();
 						}
 	                });
                 }else{  // we are running in an embedded console, so try report exit code
                 	runtime.getErr().println("IRB-Console exited: "+result);
                 	if(result.isNil()){
 	                	tar.shutdown();
 	                }else if(result.convertToInteger().getLongValue()==0){
 	                	// user typed 'exit' or 'exit 0'
 	                	// we can use the information to restart the interpreter
 	                	tar.shutdown();
 	                }else{
 	                	// user typed 'exit #' with non-zero return code
 	                    runtime.getErr().println("rc="+result.convertToInteger().getLongValue());
 	                	tar.shutdown();
 	                }
                 }
             }
         };
         t2.start();
         return(t2);
     }
 
 	/** Build the editor pane. This code is based directly on org.jruby.demo.IRBConsole(1.1.4) */
 	private JEditorPane makeEditorPane(JPanel console) {
 		console.setLayout(new BorderLayout());
         console.setSize(700, 600);
 
         JEditorPane text = new JTextPane();
 
         text.setMargin(new Insets(8,8,8,8));
         text.setCaretColor(new Color(0xa4, 0x00, 0x00));
         text.setBackground(new Color(0xf2, 0xf2, 0xf2));
         text.setForeground(new Color(0xa4, 0x00, 0x00));
         Font font = ScriptUtils.findFont("Monospaced", Font.PLAIN, 14, new String[] {"Monaco", "Andale Mono"});
 
         text.setFont(font);
         JScrollPane pane = new JScrollPane();
         pane.setViewportView(text);
         pane.setBorder(BorderFactory.createLineBorder(Color.darkGray));
         console.add(pane);
         console.validate();
 		return text;
 	}
 
 	/**
 	 * If the interpreter has exited (for example by typing 'exit' at the console)
 	 * then this method will attempt to re-start it within the same console.
 	 */
 	public void restart() {
 		if(irbThread==null || !irbThread.isAlive()){
 	        tar = new TextAreaReadline(editorPane, irbConfig.getTitle());
 			irbThread = start_irb_panel(null);
 		}
 	}
 
 }
