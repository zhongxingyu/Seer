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
 
 	/*
 	 * THINGS TO DO:
 	 * IMPLEMENT A CHANGE DIRECTORY SYSTEM.
 	 * FIX THE SWTICH SCREEN MECHANISM
 	 */
 
 	private static final long serialVersionUID = 1L;
 
 	// SWING DATA
 	JPanel pane = new JPanel();
 	static JTextArea output = new JTextArea();
 	static JTextArea log = new JTextArea();
 	static JTextField input = new JTextField();
 	JScrollPane scp = new JScrollPane(output);
 
 	// INTERNAL DATA
 	static String t1;
 	static String[] t2;
 	static String computername = "Unknown";
 	static String[] mem = new String[10];
 	static final String starter = "\n == iUtilities Console " + Info.version + " == " + 
 			"\n Hello " + System.getProperty("user.name") + "!" + 
 			"\n Type 'help' for help.";
 	static String helpstring = ("\n == Help Menu ==" +
 			"\n * Commands: 'acknowledgements', 'changelog', 'copyright', '/clear'" +
 			"\n * Most (but not all) bash commands are accepted, and will run.");
 	static int status;
 
 	// MENUBAR DATA
 	JMenuBar menubar = new JMenuBar();
 	JMenu menufile = new JMenu("File");
 	JMenu menucomm = new JMenu("Commands");
 	JMenu menuview = new JMenu("View");
 	JMenu menuhelp = new JMenu("Help");
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
 	JMenuItem about = new JMenuItem("About");
 	JMenuItem help = new JMenuItem("Help");
 	JMenuItem updates = new JMenuItem("Updates");
 
 	Console()
 	{
 		// Base GUI, in Swing.
 		super("iUtilities " + Info.version);
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
 		menubar.add(menuhelp);
 
 		// File
 		menufile.add(export);
 		menufile.add(script);
 		menufile.add(mindterm);
 		export.addActionListener(this);
 		script.addActionListener(this);
 		mindterm.addActionListener(this);
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
 		
 		// Help
 		menuhelp.add(about);
 		menuhelp.add(help);
 		menuhelp.add(updates);
 		about.addActionListener(this);
 		help.addActionListener(this);
 		updates.addActionListener(this);
 
 		pane.add(menubar, BorderLayout.NORTH);
 		setVisible(true);
 		log.append("\nJava Swing GUI Initialised and Rendered");
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
 		log.append("\niUtilities " + Info.version + " Initialised. Date: " + date);
 		status = 0;
 	}
 
 	// EVENT HANDLER
 	public void keyPressed(KeyEvent e) {
 		int keyCode = e.getKeyCode();
 		if (keyCode == 10) {
 			try {
 				processing(null);
 			} catch (InterruptedException e1) { log.append("\nkeyPressed Error");
 			} catch (IOException e1) { log.append("\nkeyPressed Error"); }
 		}
 		if (keyCode == 38){ input.setText(t1); }
 	}
 	public void keyReleased(KeyEvent arg0) { }
 	public void keyTyped(KeyEvent arg0) { }
 
 	// ACTIONPREFORMED LISTENER FOR ALL THE DAMN BUTTONS
 	public void actionPerformed(ActionEvent e) {
 		Object eventSource = e.getSource();
 		if (eventSource == export) {
			output.append("\n" + computername + "~ $ File>Export");
 			try {
 				Addons.save(null);
 			} catch (IOException e1) { log.append("\nExport Failed, IOException"); }
 		}
 		if (eventSource == script) {
 			output.append("\n" + computername + "~ $ File>Script");
 			Addons.script();
 			log.append("Script Look Executed. May or may not have run.");
 		}
 		if (eventSource == mindterm) {
 			output.append("\n" + computername + "~ $ File>Mindterm");
 			try {
 				Addons.mindterm();
 			} catch (IOException e1) { log.append("\nMindterm Download Failed: IOException"); }
 			log.append("\nMindterm Download Commenced.");
 		}
 		if (eventSource == purge) {
 			output.append("\n" + computername + "~ $ Command>Purge");
 			try {
 				Addons.purge(null);
 			} catch (IOException e1) { log.append("\nPurge Failed: IOException");}
 		}
 		if (eventSource == debug) {
 			output.append("\n" + computername + "~ $ Command>Debug");
 			try {
 				Addons.debug(null);
 			} catch (IOException e1) { log.append("\nBug JTextArea Export Failed: IOException"); }
 		}
 		if (eventSource == info){
 			output.append("\n" + computername + "~ $ Command>System Readout");
 			try {
 				Addons.info(null);
 			} catch (InterruptedException e1) { log.append("\nInformation Not Exported: InterruptedException");
 			} catch (IOException e1) { log.append("\nInformation Not Exported: IOException"); }
 		}
 		if (eventSource == ping){
 			input.setText("ping -c 1 ");
 			log.append("\nPing Shortcut Accessed.");
 		}
 		if (eventSource == clear){
 			output.setText(starter);
 			log.append("\nConsole Text Cleared");
 		}
 		// Needs Work
 		if (eventSource == viewswitch){
 			output.append("\n" + computername + "~ $ View>Switch View");
 			if (status == 0){
 				output.setText(null);
 				Console.output.setText(Console.log.getText());
 				output.append("\nViewSwitched to Debug");
 				try {
 					Thread.sleep(50);
 				} catch (InterruptedException e1) { }
 				status = 1;
 			}
 			if (status == 1){
 				output.setText(starter);
 				try {
 					Thread.sleep(50);
 				} catch (InterruptedException e1) { }
 				status = 0;
 				log.append("\nViewSwitched to Output");
 			}
 		}
 		if (eventSource == del){
 			output.append("\n" + computername + "~ $ View>Delete iUtilities Files");
 			try {
 				Addons.delete(null);
 			} catch (IOException e1) { log.append("\nDeletion Failed: IOException"); }
 			output.append("\nAll iUtilities files in ~/Library/Application Support/iUtilities have been deleted.");
 		}
 		if (eventSource == term){
 			output.append("\n" + computername + "~ $ View>Terminate");
 			log.append("\nTermination of Programme Switched");
 			System.exit(0);
 		}
 		if (eventSource == about) {
 			output.append("\n" + computername + "~ $ Help>About");
 			output.append("\n== About iUtilities " + Info.version );
 			output.append("\n" + Info.copyright);
 			output.append("\nVersion " + Info.version + " '" + Info.password + "'");
 		}
 		if (eventSource == help){
 			output.append("\n" + computername + "~ $ Help>Help");
 			output.append(helpstring);
 		}
 		if (eventSource == updates){
 			output.append("\n" + computername + "~ $ Help>Updates");
 			// Fix THIS
 		}
 	}
 
 	// PROCESSING STREAM
 	public static void processing(String[] args) throws InterruptedException, IOException {
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
 			} catch (IOException localIOException) { log.append("\nChangelog Failed: IOException"); }
 			log.append("\nChangelog Processing Trigger Invoked.");
 		}
 		if (t2[0].equals("copyright")) {
 			output.append("\n" + Info.copyright);
 			log.append("\nCopyright Processing Trigger Invoked");
 		}
 		if (t2[0].equals("help")) {
 			output.append(helpstring);
 			log.append("\nHelp Processing Trigger Invoked");
 		}
 		if (t2[0].equals("/clear")) {
 			output.setText(starter);
 			log.append("\nCommand to Clear Screen Invoked");
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
 			} catch (IOException localIOException2) { log.append("\nAcknowledgements Failed: IOException"); }
 			log.append("\nAcknowledgements Processing Trigger Invoked");
 		}
 		if (t2[0].equals("/font")){
 			int tmp;
 			if (t2[2].equals(null)){
 				tmp = 11;
 			}
 			tmp = java.lang.Integer.parseInt(t2[2]);
 			Font font = new Font(t2[1], 0, tmp);
 			output.setFont(font);
 		}
 
 		// ProcessBuilder Calling System
 		else { 
 			if ((!t2[0].equals("bash"))) {
 				exec(null);
 				log.append("\nBASH COMMAND INVOKED: " + t1);
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
 		while ((line = br.readLine()) != null) {
 			output.append("\n" + line);
 		}
 		// Error Stream
 		InputStream stderr1 = process.getErrorStream();
 		InputStreamReader isr1 = new InputStreamReader(stderr1);
 		BufferedReader br1 = new BufferedReader(isr1);
 		String line1 = null;
 		while ((line1 = br1.readLine()) != null)
 			output.append("\n " + line1);
 	}
 }
