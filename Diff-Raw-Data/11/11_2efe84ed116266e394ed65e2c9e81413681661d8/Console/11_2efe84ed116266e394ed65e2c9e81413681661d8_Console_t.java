 package gui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 
 import javax.swing.*;
 import javax.swing.text.*;
 
 import os.Pipe;
 import shell.Shell;
 import job.Job;
 
 /**
  * gui/Console.java 
  * <br><br>
  * Draws gui for Console (Shell).
  * 
  * 
  * @author Lukáš Hain
  * 
  * @team <i>OutOfMemory</i> for KIV/OS 2013
  * @teamLeader Radek Petruška radekp25@students.zcu.cz
  * 
  */
 public class Console extends Job{
 
 	final private static String prompt = "$ ";
 
 	private JFrame frame;
 	private JTextArea console;
 	private int offset = 0;
 	private ConsoleLock cl = new ConsoleLock(true, prompt);
 	private LinkedList<String> history = new LinkedList<String>();
 	
 	private ActionMap restrictedActionMap = new ActionMap();
 	private InputMap restrictedInputMap = new InputMap();
 	private ActionMap normalActionMap = new ActionMap();
 	private InputMap normalInputMap = new InputMap();
 	
 	private Shell shell;
 	
 
 
 	/**
 	 * Classic Constructor of Job.
 	 * 
 	 * @param PID
 	 * @param stdERR
 	 */
 	public Console(Integer PID, Pipe stdERR) {
 		super(PID, stdERR);
 		
 		consoleInit();
 		this.frame = new JFrame();
 		this.frame.setTitle(getClass().getName());
 		this.frame.getContentPane().add(new JScrollPane(getConsole()));
 		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.frame.setSize(900, 600);
 		this.frame.setVisible(true);
 
 		firstLine();
 
 		this.console.setCaretPosition(this.console.getText().length());
 
 		startUpdate();
 	}
 
 	/**
 	 * Console constructor.
 	 * 
 	 * @param PID
 	 * @param stdERR
 	 * @param stdIn
 	 * @param stdOut
 	 */
 	public Console(Integer PID, Pipe stdERR, Pipe stdIn, Pipe stdOut) {
 		super(PID, stdERR);
 		this.setStdIn(stdIn);
 		this.setStdOut(stdOut);
 
 		//this.sh = shell;
 
 		consoleInit();
 		this.frame = new JFrame();
 		this.frame.setTitle(getClass().getName());
 		this.frame.getContentPane().add(new JScrollPane(getConsole()));
 		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.frame.setSize(900, 600);
 		this.frame.setVisible(true);
 
 		firstLine();
 
 		this.console.setCaretPosition(this.console.getText().length());
 
 		startUpdate();
 	}
 
 	public static String getPrompt() {
 		return prompt;
 	}
 
 	public JTextArea getConsole() {
 		return this.console;
 	}
 	
 	public void setShell(Shell shell) {
 		this.shell = shell;
 	}
 
 	/**
 	 * 
 	 * @return number of entries in history
 	 */
 	public int getHistorySize() {
 		return this.history.size();
 	}
 
 	/*public Shell getShell() {
                 return this.sh;
         }*/
 
 	/**
 	 * Repaints terminal every 500 miliseconds because of blinking caret.
 	 */
 	private void startUpdate() {
 		Thread swingWorker = new Thread(){
 
 			@Override
 			public void run(){
 				while(true){
 					try {
 						Thread.sleep(500);
 						getConsole().repaint();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}                        
 				}
 			}
 
 		};
 
 		swingWorker.start();
 
 	}
 
 
 	/**
 	 * Initialize and set text area to input commands.
 	 */
 	private void consoleInit() {
 		this.console = new JTextArea(){
 			@Override
 			public void paintComponent(Graphics g) {
 				Graphics2D graphics2d = (Graphics2D) g;
 				graphics2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 				super.paintComponent(g);
 			}
 		};
 
 
 		//************* STATIC SETTINGS ***************************                
 		FancyCaret fc = new FancyCaret();
 		fc.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
 		this.console.setCaret(fc);
 		this.console.setLineWrap(true);
 		this.console.setWrapStyleWord(true);
 		((AbstractDocument)this.console.getDocument()).setDocumentFilter(this.cl);
 
 		this.history.add("");
 
 
 		ConsoleActions ca = new ConsoleActions(this);
 
 		//======================= ENTER ====================================================================
 		this.console.getInputMap().remove(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
 		this.console.getActionMap().remove("enter");
 
 		this.console.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
 		this.console.getActionMap().put("enter", ca.sendCommand);
 
 		//======================= CTRL + C ====================================================================
 		this.console.getInputMap().remove(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
 		this.console.getActionMap().remove("ctrl c");
 
 		this.console.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), "end");
 		this.console.getActionMap().put("end", ca.end);
 
 		//======================= CTRL + L ====================================================================
 		this.console.getInputMap().remove(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
 		this.console.getActionMap().remove("ctrl l");
 
 		this.console.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK), "clean");
 		this.console.getActionMap().put("clean", ca.clean);
 
 		//======================= UP KEY ====================================================================
 		this.console.getInputMap().remove(KeyStroke.getKeyStroke("UP"));
 		this.console.getActionMap().remove("UP");
 
 		this.console.getInputMap().put(KeyStroke.getKeyStroke("UP"), "moreHistory");
 		this.console.getActionMap().put("moreHistory", ca.moreHistory);
 
 		//======================= DOWN KEY ====================================================================
 		this.console.getInputMap().remove(KeyStroke.getKeyStroke("DOWN"));
 		this.console.getActionMap().remove("DOWN");
 
 		this.console.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "lessHistory");
 		this.console.getActionMap().put("lessHistory", ca.lessHistory);
 
 		this.normalActionMap = this.console.getActionMap();
 		this.normalInputMap = this.console.getInputMap();
 		
 		//======================= SETTING RESTRICTED MAPS ==================================================
 		this.restrictedInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), "end");
 		this.restrictedActionMap.put("end", ca.end);
 		//==================================================================================================
 
 		//************* DYNAMIC SETTINGS ***************************
 		setConsleBackground(Color.BLACK);
 		setConsoleFontColor(new Color(0, 204, 0));
 		setConsoleFontType(new Font("monospaced", Font.BOLD, 24));
 	}
 
 
 	public void setConsoleFontType(Font f){
 		this.getConsole().setFont(f);
 	}
 
 
 	public void setConsoleFontColor(Color c){
 		this.getConsole().setForeground(c);
 	}
 
 
 	public void setConsleBackground(Color c){
 		this.getConsole().setBackground(c);
 	}
 
 
 	/**
 	 * Prints new line and sets carret position there.
 	 */
 	public void newLine(){
 		String temp = getCurrentDir("") + prompt;
 		print("\n" + temp);
 		this.console.setCaretPosition(this.console.getText().length());
 		this.offset = this.console.getText().lastIndexOf(prompt);
 	}
 
 	public void firstLine(){
 		String temp = getCurrentDir("") + prompt;
 		print(temp);
 		this.console.setCaretPosition(this.console.getText().length());
 		this.offset = this.console.getText().lastIndexOf(prompt);
 	}
 
 
 	public void changeToHistory(int value) {
 		if(value < this.history.size()){
 			changeLastLineText(this.history.get(value));
 		} 
 	}
 
 	/**
 	 * This method replaces anything what is behind last prompt with text in parameter. 
 	 * 
 	 * @param text which shoud replace current content
 	 */
 	public void changeLastLineText(String text){
 		try {
 			this.cl.setUseDocumentFilter(false);
 			this.console.setText(this.console.getText(0, this.offset + prompt.length()) + text);
 			this.cl.setUseDocumentFilter(true);
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Prints string
 	 * @param string what to print
 	 */
 	public void print(String string) {
 		/*if(string == null)
 			System.out.println("FUCKKKKKKKKKKKKK");
 		this.console.setEditable(false);*/
 		this.console.append(string);
 		/*this.console.setEditable(true);*/
 	}
 
 	/**
 	 * This will delete all text from console.
 	 */
 	public void clean() {
 		this.cl.setUseDocumentFilter(false);
 		this.console.setText("");
 		this.cl.setUseDocumentFilter(true);
 	}
 
 
 	/**
 	 * Returns last line of text in console (including current path and prompt).
 	 * @return last line of text in console
 	 * @throws BadLocationException if the offset or length are invalid
 	 */
 	private String getLastLine() throws BadLocationException{
 		return this.console.getText(this.offset, this.console.getText().length() - this.offset);
 	}
 
 	/**
 	 * Returns last command entered into console (without current path and prompt).
 	 * @return last command
 	 */
 	public String getLastCommand(){
 		try{
 			String lastLine = getLastLine();
 			String lastCommand = lastLine.substring(lastLine.indexOf(prompt) + prompt.length()).trim();
 			System.out.println(lastCommand);
 			return lastCommand;
 		} catch(BadLocationException e){
 			System.out.println("Unexpected error");
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Adds valid string (must have length 1 or greater and also 
 	 * must be different from last added string) to history of commands.
 	 * 
 	 * @param str command which will be added to history
 	 */
 	private void addToHistory(String str) {
 		if(str.length() > 0){
 			if((this.history.size() > 1 && this.history.get(1).compareTo(str) != 0) || this.history.size() <= 1){
 				this.history.add(1, str);
 			} 
 		}
 	}
 
 	@Override
 	public String getManual() {
 		// TODO not sure
 		return "I am not a real job.";
 	}
 	
 	/**
 	 * This method prevents user from any editing of console. The only action
 	 * user can do after this method is CTRL + C for killing the process.
 	 * 
 	 * IMPORTANT: This method is quite time consuming so please use it with this
 	 * fact in mind.
 	 */
 	public void setRestricted(){
 		this.console.setEditable(false);
 		this.cl.setUseDocumentFilter(false);
 		this.console.setActionMap(this.restrictedActionMap);
 		this.console.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this.restrictedInputMap);
 		this.console.setInputMap(JComponent.WHEN_FOCUSED, this.restrictedInputMap);
 	}
 	
 	/**
 	 * This method clears the restriction that setRestricted() method enabled.
 	 * 
 	 * @see setRestricted()
 	 */
 	public void setUnrestricted(){
 		this.console.setInputMap(JComponent.WHEN_FOCUSED, this.normalInputMap);
 		this.console.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this.normalInputMap);
 		this.console.setActionMap(this.normalActionMap);
 		this.cl.setUseDocumentFilter(true);
 		this.console.setEditable(true);
 	}
 
 	@Override
 	protected void getJobDone() throws InterruptedException {
 		/** Read stdErr pipe **/
 		Thread stdErrThread = new Thread(){
 
 			@Override
 			public void run(){
 				char[] chars = new char[BUF_SIZE];
 				int i;
 				while(true){//TODO something else
 					try{
 						try {
 							i = getStdErr().getData(chars);
							print(String.copyValueOf(chars, 0, i));
 						} catch (IOException e) {
 							print("Console read error");
 						} 
 					}catch (InterruptedException e) {
 						// TODO Solve this later
 						System.out.println("This is realy bad.");
 					}
 				}
 			}
 
 		};
 		stdErrThread.start();
 
 		/** Read stdIn pipe **/
 		char[] chars = new char[BUF_SIZE];
 		int i;
 		while(true){//TODO something else
 			try {
 				i = getData(chars);
 				if(i != -1){
 					//XXX Important: prompt char indicates there should by a new line
 					if(String.copyValueOf(chars, 0, i).equals(prompt)){
 						newLine();
 						setUnrestricted();
 						continue;
 					}
 					print(String.copyValueOf(chars, 0, i));
 				}
 			} catch (IOException e) {
 				pushError("Shell read error");
 			}
 		}
 	}
 
 	/**
 	 * Push last command into pipe.
 	 */
 	public void consolePush(){
 		try {
 			String str = getLastCommand();
 			addToHistory(str);                        
 			pushData(str.toCharArray(), 0, str.toCharArray().length);
 			pushData(prompt.toCharArray(), 0, prompt.toCharArray().length);//let shell know there is the end
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			System.out.println("To snad ne");
 		}
 	}
 
 	//******************************* TESTING ********************************
 	static String getCurrentDir(String name){
 		return new File(name).getAbsolutePath();
 	}
 }
