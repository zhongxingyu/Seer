 package com.me.ifly6;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.InetAddress;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.swing.*;
 import javax.swing.text.DefaultCaret;
 
 public class Console extends JFrame implements KeyListener, ActionListener{
 
	/**
	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	// SWING DATA
 	JPanel pane = new JPanel();
 	static JTextArea output = new JTextArea();
 	static JTextArea bug = new JTextArea();
 	static JTextField input = new JTextField();
 	JScrollPane scp = new JScrollPane(output);
 
 	// INTERNAL DATA
 	static String t1;
 	static String[] t2;
 	static String computername = "Unknown";
 	static String[] mem = new String[10];
 	static final String starter = "\n == iUtilities Console " + Data.version + " == " + 
 			"\n Hello " + System.getProperty("user.name") + "!" + 
 			"\n Type 'help' for help.";
 	static String help = ("\n == Help Menu ==" +
 			"\n * Commands: 'acknowledgements', 'changelog', 'copyright', '/clear'" +
 			"\n * Most (but not all) bash commands are accepted, and will run.");
 	static String storage;
 	static int status;
 
 	// MENUBAR DATA
 	JMenuBar menubar = new JMenuBar();
 	JMenu menufile = new JMenu("File");
 	JMenu menucomm = new JMenu("Commands");
 	JMenu menuview = new JMenu("View");
 	JMenuItem export = new JMenuItem("Exportation");
 	JMenuItem script = new JMenuItem("Script Input");
 	JMenuItem mindterm = new JMenuItem("Mindterm");
 	JMenuItem purge = new JMenuItem("Inactive Memory Purge");
 	JMenuItem debug = new JMenuItem("Log Console");
 	JMenuItem info = new JMenuItem("System Readout");
 	JMenuItem ping = new JMenuItem("Ping Utility");
 	JMenuItem clear = new JMenuItem("Clear Screen");
 	JMenuItem viewswitch = new JMenuItem("Switch View");
 	JMenuItem del = new JMenuItem("Delete iUtilities Files");
 	JMenuItem term = new JMenuItem("Terminate");
 
 	Console()
 	{
 		// Base GUI, in Swing.
 		super("iUtilities " + Data.version);
 		setBounds(50, 50, 670, 735);
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		Container con = getContentPane();
 		getContentPane().setLayout(new BorderLayout());
 		con.add(this.pane);
 		pane.setLayout(new BorderLayout());
 
 		pane.add(scp, BorderLayout.CENTER);
 		pane.add(input, BorderLayout.SOUTH);
 		output.setEditable(false);
 		input.addKeyListener(this);
 
 		Font font = new Font("Monaco", 0, 11);
 		output.setFont(font);
 		output.setBackground(Color.black);
 		output.setForeground(Color.green);
 
 		input.setFont(font);
 		input.setBackground(Color.black);
 		input.setForeground(Color.green);
 		input.setCaretColor(Color.green);
 		this.pane.setBackground(Color.DARK_GRAY);
 		DefaultCaret caret = (DefaultCaret)output.getCaret();
 		caret.setUpdatePolicy(2);
 
 		// MENUBAR CREATION
 		menubar.add(menufile);
 		menubar.add(menucomm);
 		menubar.add(menuview);
 
 		// File
 		menufile.add(export);
 		menufile.add(script);
 		export.addActionListener(this);
 		script.addActionListener(this);
 		// Commands
 		menucomm.add(purge);
 		menucomm.add(debug);
 		menucomm.add(info);
 		menucomm.add(ping);
 		purge.addActionListener(this);
 		debug.addActionListener(this);
 		info.addActionListener(this);
 		ping.addActionListener(this);
 		// View
 		menuview.add(clear);
 		menuview.add(viewswitch);
 		menuview.add(del);
 		menuview.add(term);
 		clear.addActionListener(this);
 		viewswitch.addActionListener(this);
 		term.addActionListener(this);
 		del.addActionListener(this);
 
 		pane.add(menubar, BorderLayout.NORTH);
 		setVisible(true);
 		bug.append("\nJava Swing GUI Initialised and Rendered");
 	}
 
 	// MAIN THREAD.
 	public static void main(String[] args) {
 		new Console();
 		output.append(starter);
 		try {
 			computername = InetAddress.getLocalHost().getHostName(); 
 		} catch (Exception localException) { }
 		@SuppressWarnings("unused")
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		Date date = new Date();
 		bug.append("\niUtilities " + Data.version + " Initialised. Date: " + date);
 		status = 0;
 	}
 
 	// EVENT HANDLER
 	public void keyPressed(KeyEvent e) {
 		int keyCode = e.getKeyCode();
 		if (keyCode == 10) {
 			try {
 				processing(null);
 			} catch (InterruptedException e1) { bug.append("\nkeyPressed Error");
 			} catch (IOException e1) { bug.append("\nkeyPressed Error"); }
 		}
 		if (keyCode == 38){ input.setText(t1); }
 	}
 	public void keyReleased(KeyEvent arg0) { }
 	public void keyTyped(KeyEvent arg0) { }
 
 	// ACTIONPREFORMED LISTENER FOR ALL THE DAMN BUTTONS
 	public void actionPerformed(ActionEvent e) {
 		Object eventSource = e.getSource();
 		if (eventSource == export) {
 			try {
 				Addons.save(null);
 			} catch (IOException e1) { bug.append("\nExport Failed, IOException"); }
 		}
 		if (eventSource == script) {
 			Addons.script();
 			bug.append("Script Look Executed. May or may not have run.");
 		}
 		if (eventSource == mindterm) {
 			try {
 				Addons.mindterm();
 			} catch (IOException e1) { bug.append("\nMindterm Download Failed: IOException"); }
 			bug.append("\nMindterm Download Commenced.");
 		}
 		if (eventSource == purge) {
 			try {
 				Addons.purge(null);
 			} catch (IOException e1) { bug.append("\nPurge Failed: IOException");}
 		}
 		if (eventSource == debug) {
 			try {
 				Addons.debug(null);
 			} catch (IOException e1) { bug.append("\nBug JTextArea Export Failed: IOException"); }
 		}
 		if (eventSource == info){
 			try {
 				Addons.info(null);
 			} catch (InterruptedException e1) { bug.append("\nInformation Not Exported: InterruptedException");
 			} catch (IOException e1) { bug.append("\nInformation Not Exported: IOException"); }
 		}
 		if (eventSource == ping){
 			input.setText("ping -c 1 ");
 			bug.append("\nPing Shortcut Accessed.");
 		}
 		if (eventSource == clear){
 			output.setText(starter);
 			bug.append("\nConsole Text Cleared");
 		}
 		// Needs Work
 		if (eventSource == viewswitch){
 			if (status == 0){
 				storage = output.getText();
 				output.setText(null);
 				Console.output.setText(Console.bug.getText());
 				output.append("\nViewSwitched to Debug");
 				try {
 					Thread.sleep(50);
 				} catch (InterruptedException e1) { }
 				status = 1;
 			}
 			if (status == 1){
 				output.setText(storage);
 				try {
 					Thread.sleep(50);
 				} catch (InterruptedException e1) { }
 				status = 0;
 				bug.append("\nViewSwitched to Output");
 			}
 		}
 		if (eventSource == del){
 			try {
 				Addons.delete(null);
 			} catch (IOException e1) { bug.append("\nDeletion Failed: IOException"); }
 			output.append("\nAll iUtilities files in ~/Library/Application Support/iUtilities have been deleted.");
 		}
 		if (eventSource == term){
 			bug.append("\nTermination of Programme Switched");
 			System.exit(0);
 		}
 	}
 
 	// PROCESSING STREAM
 	public static void processing(String[] args) throws InterruptedException, IOException {
 		boolean triggered = false;
 		t1 = input.getText();
 		output.append("\n" + computername + "~ $ " + t1);
 		input.setText(null);
 		t2 = t1.split(" ");
 
 		// Sub-commands
 		Runtime rt = Runtime.getRuntime();
 		if (t2[0].equals("changelog")) {
 			try {
 				String userName = System.getProperty("user.name");
 				File folder = new File("/Users/" + userName + "/Library/Application Support/iUtilities");
 				folder.mkdirs();
 				String[] url = { "curl","-o","/Users/" + userName + 
 						"/Library/Application Support/iUtilities/changelog.txt", "http://ifly6server.no-ip.org/iUtilities/changelog.txt" };
 				rt.exec(url);
 				String r = "\n";
 				FileReader fstream = new FileReader("/Users/" + userName + "/Library/Application Support/iUtilities/changelog.txt");
 				BufferedReader br = new BufferedReader(fstream);
 				r = br.readLine();
 				while ((r = br.readLine()) != null)
 					output.append("\n " + r);
 			} catch (IOException localIOException) { bug.append("\nChangelog Failed: IOException"); }
 			triggered = true;
 			bug.append("\nChangelog Processing Trigger Invoked.");
 		}
 		if (t2[0].equals("copyright")) {
 			output.append("\n" + Data.copyright);
 			triggered = true;
 			bug.append("\nCopyright Processing Trigger Invoked");
 		}
 		if (t2[0].equals("help")) {
 			output.append(help);
 			triggered = true;
 			bug.append("\nHelp Processing Trigger Invoked");
 		}
 		if (t2[0].equals("/clear")) {
 			output.setText(starter);
 			triggered = true;
 			bug.append("\nCommand to Clear Screen Invoked");
 		}
 		if (t2[0].equals("acknowledgements")) {
 			try {
 				String userName = System.getProperty("user.name");
 				File folder = new File("/Users/" + userName + "/Library/Application Support/iUtilities");
 				folder.mkdirs();
 				String[] url = { "curl", "-o", "/Users/" + userName + 
 						"/Library/Application Support/iUtilities/acknowledgements.txt", "http://ifly6server.no-ip.org/iUtilities/acknowledgements.txt" };
 				ProcessBuilder builder = new ProcessBuilder(url);
 				builder.start();
 				String r = "\n";
 				FileReader fstream = new FileReader("/Users/" + userName + "/Library/Application Support/iUtilities/acknowledgements.txt");
 				BufferedReader br = new BufferedReader(fstream);
 				r = br.readLine();
 				while ((r = br.readLine()) != null)
 					output.append("\n " + r);
 			} catch (IOException localIOException2) { bug.append("\nAcknowledgements Failed: IOException"); }
 			triggered = true;
 			bug.append("\nAcknowledgements Processing Trigger Invoked");
 		}
 
 		// ProcessBuilder Calling System
 		else { 
 			if ((!t2[0].equals("bash")) && (!triggered)) {
 				exec(null);
 				bug.append("\nBASH COMMAND INVOKED: " + t1);
 			}
 		}	
 	}
 
 	// EXECUTION STREAM
 	public static void exec(String[] args) throws IOException{
 		// Output Stream
 		ProcessBuilder builder = new ProcessBuilder(t2);
 		Process process = builder.start();
 		InputStream is = process.getInputStream();
 		InputStreamReader isr = new InputStreamReader(is);
 		BufferedReader br = new BufferedReader(isr);
 		String line;
 		output.append("\n");
 		while ((line = br.readLine()) != null) {
 			output.append(line + "\n");
 		}
 		// Error Stream
 		InputStream stderr1 = process.getErrorStream();
 		InputStreamReader isr1 = new InputStreamReader(stderr1);
 		BufferedReader br1 = new BufferedReader(isr1);
 		String line1 = null;
 		output.append("\n");
 		while ((line1 = br1.readLine()) != null)
 			output.append(" " + line1 + "\n");
 	}
 }
