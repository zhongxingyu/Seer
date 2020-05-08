 /**
  * Author's Comments
  * Created by Jazy5552
  * You can take the program, edit it, reuse it, as Pleased.
  * Credit would always be appreciated
  * Sorry for messy coding... I'll get around to redoing it... soon
  */
 package jazy.minecraft.installer;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JScrollPane;
 import javax.swing.JOptionPane;
 
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 //import java.io.InputStream;
 //import java.net.URISyntaxException;
 

//TODO Find a way to extract the resources (Or use them directly!)***

 public class start{
 	static final Class<start> cl = start.class;
 	static boolean debug = false;
 	static extractJar extr = new extractJar(null);
 	static JButton restart,fullExtract,openTerminal;
 	static String version = "3.72";
 	static JTextArea delog;
 	static JScrollPane dep;
 	static JPanel buttonsP;
 	static JFrame m;
 	static JTextArea dis;
 	static JScrollPane jp;
 	static boolean busy=false;
 	static String directory;
 	static int totalbytes;
 	static String nn;
 	static String userName;
 	static String Creator;
 	static boolean eclipse=false;
 	static FileInputStream from = null;
 	static FileOutputStream to = null;
 	
 	
 	static WindowListener wl = new WindowListener(){
 		public void windowActivated(WindowEvent arg0) {
 			
 		}
 
 		public void windowClosed(WindowEvent arg0) {
 			
 		}
 
 		public void windowClosing(WindowEvent arg0) {
 			m.dispose();
 			System.exit(0);
 		}
 
 		public void windowDeactivated(WindowEvent arg0) {
 			if (busy||debug||restarted) return;
 			m.dispose();
 			System.exit(0);
 		}
 
 		public void windowDeiconified(WindowEvent arg0) {
 			
 		}
 
 		public void windowIconified(WindowEvent arg0) {
 			
 		}
 
 		public void windowOpened(WindowEvent arg0) {
 			
 		}
 	};
 	static MouseListener ml = new MouseListener(){
 		public void mouseClicked(MouseEvent e) {
 			
 		}
 
 		public void mouseEntered(MouseEvent e) {
 			
 		}
 
 		public void mouseExited(MouseEvent e) {
 			
 		}
 
 		public void mousePressed(MouseEvent e) {
 			if (e.isShiftDown()&&(e.getButton()==1)){ busy=false;
 				//Debug enabled
 				if (debug) {debug=false;log("Debug off...",false);m.setTitle("Minecraft SnapShooter v"+version);}
 				else {debug=true;log("",true);}
 				return;
 			}
 			restarted=false;
 		}
 
 		public void mouseReleased(MouseEvent e) {
 			if (debug) {try{log("Jar info="+this.getClass().getProtectionDomain().getCodeSource().getLocation(),true);}catch(Exception dontdoit){return;}}
 			if (e.getComponent().getName().toLowerCase().equals("ignore")) return;
 			if (e.getComponent().getName().toLowerCase().equals("restart")) { busy=true;
 			restart();
 				return;
 			} else if (e.getComponent().getName().toLowerCase().equals("fullextract")) { busy=true;
 			new Thread(new Runnable(){ //NEW THREAD >:D
 				public void run(){
 			extr.extract("versions");
 			log("\nExtraction Complete!",false);
 			busy=false;
 				}
 			}).start(); //I always forget the .start() xD
 				return;
 			} else if (e.getComponent().getName().toLowerCase().equals("openterminal")) { busy=true;
 			File te = new File("/Applications/Utilities/Terminal.app");
 			Runtime rt = Runtime.getRuntime();
 			if (te.exists()){ //MAC
 				removeGui();
 				try{
 				rt.exec("/usr/bin/open -a Terminal");
 				}catch(Exception e1){ System.out.println(e1.getMessage()); } //Error?
 				return;
 			}else if ((te=new File("C:/Windows/system32/cmd.exe")).exists()){ //Windows
 				removeGui();
 				try{
 					rt.exec("C:/Windows/system32/cmd.exe /c start cmd.exe");
 					}catch(Exception e1){ System.out.println(e1.getMessage()); } //Error?
 				return;
 			}else{
 				log("ERROR: Can't find command prompt...",false);
 			}
 			busy=false;
 				return;
 			}
 			
 			///Applications/Utilities/
 			//C:/Windows/system32/cmd.exe
 			if (busy||e==null||e.getComponent()==null||(e.getComponent().getName()==null)||e.isShiftDown()||e.isControlDown()) {log("Please try again later...",false); return;} busy=true; if (debug) log("Mouse event captured. (Click)\n"+e.getComponent().getName(),true);
 			write(e.getComponent().getName());
 		}
 	};
 	
 
 	static boolean restarted=false; //My way of making the code drop what its doing....
 	
 	public static void main(String[] args) {
 
 		new start();
 
 	}
 	
 	start(){
 		createGui();
 	}
 	
 	public static void createGui(){
 			if (debug) Creator = "Jazy5552";
 			else Creator = "J***5552";
 		m = new JFrame("Minecraft SnapShooter v"+version);
 		m.getContentPane().setLayout(new FlowLayout());
 		log("Created/Coded by "+Creator+"'s \nMinecraft Snapshot/Update Installer!\nBy: Non other than "+Creator+" himself ;)\nVersion: "+version+"\n\nPlease select your opperating System and Version to continue >>>",false);
 		JMenuBar bar1 = new JMenuBar();
 		JMenuBar bar2 = new JMenuBar();
 		JMenuBar bar3 = new JMenuBar();
 		JMenu win7 = new JMenu("Win7/Vista");
 		JMenu winXP = new JMenu("WindowsXP");
 		JMenu mac = new JMenu("Mac");
 		restart = new JButton("Restart");
 		restart.setName("restart");
 		restart.addMouseListener(ml);
 		fullExtract = new JButton("ExtractRe.");
 		fullExtract.setName("FullExtract");
 		fullExtract.addMouseListener(ml);
 		openTerminal = new JButton("CMD");
 		openTerminal.setName("OpenTerminal");
 		openTerminal.addMouseListener(ml);
 		buttonsP = new JPanel(new GridLayout(0,1));
 		win7.addMouseListener(ml);
 		win7.setName("ignore");
 		winXP.addMouseListener(ml);
 		winXP.setName("ignore");
 		mac.addMouseListener(ml);
 		mac.setName("ignore");
		String[] files = {"ERROR","ERROR","ERROR"}; //Just for when the ifs are passed
 		
 		if (new File(cl.getResource("/versions").getPath()).isDirectory()) { //Eclipse...
 			eclipse = true;
 			log("Resource found",true);
 			files = new File(cl.getResource("/versions").getPath()).list();
 			directory=cl.getResource("/versions").getPath();
 		}else{//New engine (Make files)
 			log("Finding Jar: "+getJarFileName(),true);
 			JarFile jar=null;
 			try{
 			jar = new JarFile(getJarFileName());
 			}catch(Exception e){log("ERROR ATTAINING JAR FILE!: "+e.getMessage(),false);
 			try{
 				jar.close();
 			}catch(Exception ignore){}
 			}
 			log("Jar found successfully!", true);
 			if (jar!=null){
 				log("getJarFileName= "+getJarFileName(),true);
 				Enumeration<JarEntry> entries = jar.entries();
 				JarEntry entry;
 				String temp;
 				ArrayList<String> store = new ArrayList<String>(0);//Store the names
 				while(entries.hasMoreElements()){
 					entry = entries.nextElement();
 					if (entry.getName().contains("minecraft.jar")){//bingo
 						log("ValidEntry: "+entry.getName(),true);
 						temp = entry.getName().substring(0,entry.getName().lastIndexOf("/"));
 						temp = temp.substring(temp.lastIndexOf("/")+1);//We got the name we wanted
 						store.add(temp);
 					}
 				}
 				if (store.size()>0){
 				files = new String[store.size()];
 				for (int i=0;i<store.size();i++){ //transfer
 					files[i] = store.get(i);
 				}
 				}
 			try{
 			jar.close();}catch(Exception ignore){}
 			}
 		}
 		
 		log("Path internal is: "+ cl.getResource("/versions").getPath(),true);
 		for (int i=0;i<files.length;i++){
 			JMenuItem x = new JMenuItem();
 			x.setText(files[i]);
 			x.setName("win7" + files[i]); //For identification purposes...
 			win7.add(x);
 			x.addMouseListener(ml);
 		}
 		for (int i=0;i<files.length;i++){
 			JMenuItem x = new JMenuItem();
 			x.setText(files[i]);
 			x.setName("winXP" + files[i]); //For identification purposes...
 			winXP.add(x);
 			x.addMouseListener(ml);
 		}
 		for (int i=0;i<files.length;i++){
 			JMenuItem x = new JMenuItem();
 			x.setText(files[i]);
 			x.setName("mac" + files[i]);//For identification purposes...
 			mac.add(x);
 			x.addMouseListener(ml);
 		}
 		bar1.add(win7);
 		bar2.add(mac);
 		bar3.add(winXP);
 		buttonsP.add(bar1); buttonsP.add(bar2);buttonsP.add(bar3); 
 		
 		if(debug) {
 			buttonsP.add(restart);
 			buttonsP.add(openTerminal);
 			buttonsP.add(fullExtract);
 		}
 		
 		m.add(buttonsP);
 		m.addWindowListener(wl);
 		m.pack();
 		m.setFocusableWindowState(true);
 		m.setVisible(true);
 		busy=false;
 	}
 	
 	static void write(String name){
 		File loc; //CAUTION big mess below... :/
 		log("Choosen: "+name+"\nSearch for: "+name.substring(0,3),true); //debug
 		int x; String thing; userName = null;totalbytes = 0;nn=name;
 		
 		userName = getUsername(name);
 		if ((userName==null)&&!debug){
 		userName = JOptionPane.showInputDialog(m.getContentPane(), "Input current OS logged-in username\n(Caps Sensitive!)\nDon't run from USB next time...","Username",JOptionPane.PLAIN_MESSAGE);
 		}
 		
 		if ((userName==null)&&!debug) {busy=false;return;}
 		if (debug){ //I <3 Debugging! >:D
 		userName = JOptionPane.showInputDialog(m.getContentPane(), "Input current OS logged-in username\n(Caps Sensitive!)","Username",JOptionPane.PLAIN_MESSAGE);
 		if (userName==null) {busy=false; return;}
 			if (name.contains("win7")) {x=4;loc = new File(((String) JOptionPane.showInputDialog(m.getContentPane(), "minecraft bin directory: (Hit OK if Confused)", "Check", JOptionPane.PLAIN_MESSAGE, null, null,"C:\\Users\\"+userName+"\\AppData\\Roaming\\.minecraft\\bin"))); log("Being written to: "+loc.getPath(),true);thing = "\\";}
 			else if (name.contains("mac")) {x=3;loc = new File(((String) JOptionPane.showInputDialog(m.getContentPane(), "minecraft bin directory: (Hit OK if Confused)", "Check", JOptionPane.PLAIN_MESSAGE, null, null,"/Users/"+userName+"/Library/Application Support/minecraft/bin")));log("Being written to: "+loc.getPath(),true);thing = "/";}
 			else if (name.contains("winXP")) {x=5;loc = new File(((String) JOptionPane.showInputDialog(m.getContentPane(), "minecraft bin directory: (Hit OK if Confused)", "Check", JOptionPane.PLAIN_MESSAGE, null, null,"C:\\Documents and Settings\\"+userName+"\\Application Data\\.minecraft\\bin")));log("Being written to: "+loc.getPath(),true);thing = "\\";}
 			else {log("Wtf you do!!!",false);busy=false;return;}
 		} else {
 			if (name.contains("win7")) {x=4;loc = new File("C:\\Users\\"+userName+"\\AppData\\Roaming\\.minecraft\\bin"); log("Being written to: "+loc.getPath(),true);thing = "\\";}
 			else if (name.contains("mac")) {x=3;loc = new File("/Users/"+userName+"/Library/Application Support/minecraft/bin");log("Being written to: "+loc.getPath(),true);thing = "/";}
 			else if (name.contains("winXP")) {x=5;loc = new File("C:\\Documents and Settings\\"+userName+"\\Application Data\\.minecraft\\bin");log("Being written to: "+loc.getPath(),true);thing = "\\";}
 			else {log("Wtf you do!!!",false);busy=false;return;}
 		} if ((loc==null)||loc.getPath().equals(null)) restart();
 		
 		if (!loc.isDirectory()) {log("ERROR: Incorrect Destination? Maybe MineCraft isn't installed? Or incorrect win/mac username?\nOR There are /s in the folder name!",false);busy=false;return;} //Run checks vvvvv
 		if (!loc.canWrite()) {log("ERROR: Can't write to location...",false);busy=false;return;}
 		loc = new File(loc.getPath()+thing+"minecraft.jar"); //Wow, pretty nasty...
 		try {loc.createNewFile();} catch(Exception e) {log("ERROR: Balls... can't create file -_-'",false);busy=false;log("ERROR: "+e.getMessage(),true);return;}
 		if (!loc.isFile()) {log("ERROR: ...Not even I know?",false);busy=false;return;}
 		from = null;
 		to = null;
 		
 		if (eclipse) {//Way of making my new engine...
 		File update = new File(directory+thing+ name.substring(x,name.length())+"/minecraft.jar");
 		
 		if (!update.exists()) {log("ERROR: File missing??? Check your versions folder...",false);
 			log("File= "+update.getPath()+"\nDir= "+loc.getParentFile().getPath()+"\nUpdate is: "+update.toString(),true);
 			busy=false;
 			return;
 		}
 		
 		try{
 			from = new FileInputStream(update);
 			to = new FileOutputStream(loc);
 			
 			new Thread(new Runnable(){ //Experimental!!
 				public void run(){
 					byte[] buffer = new byte[1024*4]; //  Speed/CPUIntensity
 					int bytesread = 0;
 					try{
 						double total = from.available();
 						double totalbr = 0;
 						while ((bytesread = from.read(buffer))!=-1){
 							//while (bytesread++<300){//Debug
 							totalbytes += bytesread;//Simple Math
 							totalbr = (totalbytes/total)*100;
 							
 							to.write(buffer,0,bytesread); //DO IT!!
 							start.dis.setText(String.format("%.1f Complete!\n%d bytes written.",totalbr,totalbytes));
 						}
 						to.close();
 						from.close();
 						log("\nUpdate Complete in "+nn+":"+userName+"\nMore updates coming soon!\nMade by "+Creator+" ;)",false);
 						busy=false;
 					}catch(Exception e){
 						log("ERROR: Oh balls, something happen while writing...",false);
 						log("ERROR: "+e.getMessage(),true);
 						busy=false;
 						try{to.close();
 						from.close();
 						}catch(Exception da){}
 					}
 				}
 			}).start();
 			
 		} catch(Exception e) {
 				log("ERROR: Oh balls, something happen while writing...",false);
 				log("ERROR: "+e.getMessage(),true);
 		}
 		loc = null;
 		update = null;
 		}else{//New engine time! >:D
 			log("ExtractingTo("+name+" /minecraft.jar,"+loc.getParentFile().getPath()+")",true);
 			final String xx = name.substring(x,name.length())+"/"+"minecraft.jar";//HERHEHREHR
 			final String yy = loc.getParentFile().getPath()+"/";//WTFTWTFWTFTASD
 			new Thread(new Runnable(){
 			public void run(){
 				extractJar extraction = new extractJar(null);
 				int ext = extraction.extractTo(xx,yy);
 				if (ext==-1){//Failed...
 					log("ERROR Extracting Files!",false);
 				}else{
 					log("\nUpdate Complete in "+nn+":"+userName+"\nExtracted "+ext+" files!\nMore updates coming soon!",false);
 				}
 			busy = false;
 			}
 			}).start();
 		}
 	}
 	
 	static void log(String mes, boolean d){
 		if (mes==null) return;
 		if ((!debug)&&(dep!=null)) {
 			dep.removeAll();
 			delog.removeAll();
 			delog = null;
 			dep = null;
 			try{
 				buttonsP.remove(restart);
 				buttonsP.remove(openTerminal);
 				buttonsP.remove(fullExtract);
 			}catch(Exception ignore){} //Nasty workaround...
 		}
 		if (debug&&(delog==null)) { //If debug then lets debug :D
 				delog = new JTextArea(12,20);
 				delog.setText("Debug on: \n");
 				delog.setWrapStyleWord(true);
 				delog.setLineWrap(true);
 				delog.setEditable(false);
 				dep = new JScrollPane(delog);
 				dep.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 				dep.setAutoscrolls(true);
 				m.getContentPane().add(dep);
 				try{m.setTitle("DEBUG MODE ON");
 					buttonsP.add(restart);
 					buttonsP.add(openTerminal);
 					buttonsP.add(fullExtract);
 				}catch(Exception ignore){} //Nasty workaround...
 		}
 		if ((jp == null)&&!d) {
 			dis = new JTextArea(12,20);
 			dis.setText("This is the log window: \n");
 			dis.setWrapStyleWord(true);
 			dis.setLineWrap(true);
 			dis.setEditable(false);
 			jp = new JScrollPane(dis);
 			jp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 			m.getContentPane().add(jp);
 		}
 		m.pack();
 		m.setVisible(true); //Display
 		if (!d) {
 			dis.setText(dis.getText()+mes+"\n");
 			dis.setCaretPosition(dis.getText().length());
 		}
 		if (d&&debug) {
 			delog.setText(delog.getText()+mes+"\n");
 			delog.setCaretPosition(delog.getText().length());
 		}
 	}
 	
 	public static String getUsername(String poop){
 		String dir = cl.getProtectionDomain().getCodeSource().getLocation().toString();
 		log("(getUsername) dir="+dir,true);
 		String from = null;
 		String to = null;
 		String user = null;
 		int x = 0;
 		if (poop.contains("win7")){
 			from = "Users/";
 			x = 6;
 			to = "/";
 		}
 		else if (poop.contains("mac")){
 			from = "Users/";
 			x = 6;
 			to = "/";
 		}
 		else if (poop.contains("winXP")){
 			from = "Settings/";
 			x = 9;
 			to = "/";
 		}
 		else return null;
 		log("(getUsername) from="+from,true);
 		if (!dir.contains(from)) return null;
 		
 		user = dir.substring(dir.indexOf(from)+x);
 		user = user.substring(0,user.indexOf(to));
 		user = user.replaceAll("%20", " ");
 		log("(getUsername) username="+user,true);
 		return user;
 	}
 	
 	public static void restart(){
 				restarted = true;
 				m.setVisible(false);
 				m.removeAll();
 				m.dispose();
 				m=null;
 				delog.removeAll();
 				delog=null;
 				restart=null;
 				dep=null;
 				buttonsP.removeAll();
 				buttonsP=null;
 				dis=null;
 				jp=null; //TODO Display a restarting frame for 1-2 seconds (For sho :D)
 				wait(600);
 				createGui();
 	}
 	
 	public static void removeGui(){
 		restarted = true;
 		m.setVisible(false);
 		m.removeAll();
 		m.dispose();
 		m=null;
 		delog.removeAll();
 		delog=null;
 		restart=null;
 		dep=null;
 		buttonsP.removeAll();
 		buttonsP=null;
 		dis=null;
 		jp=null; //TODO Display a restarting frame for 1-2 seconds (For sho :D)
 	}
 	
 	private static String getJarFileName (){ //Nasty copy from my extractor...
 		String result="";
 		int from,to;
 		try{
 		result = cl.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
 		}catch(Exception e){log(e.getMessage(),false);return null;}
 		if (result.contains("jar:file:")){
 			from = 9;
 		}else if (result.contains("file:")){
 			from = 5;
 		}else{log("Invalid Excecution!: "+result,false);return null;}
 		//We got the path
 		//Lets rid the !/
 		if (result.contains("!/")){
 			to = result.indexOf("!/");
 		}else{to=result.length();}
 		//Trim it
 		result = result.substring(from,to);
 		//Lets fix the spaces (%20)
 		result = result.replaceAll("%20", " ");
 		//We done :D
 		log("result= "+result,true);
 		return result;
     }
 	
 	public static void wait (int n){
 		long time=System.currentTimeMillis();
         long tnow,tto;
         tnow=time;
         tto=time+(n);
         while (tto>tnow){
         	tnow=System.currentTimeMillis();
         }
 	}
 
 }
