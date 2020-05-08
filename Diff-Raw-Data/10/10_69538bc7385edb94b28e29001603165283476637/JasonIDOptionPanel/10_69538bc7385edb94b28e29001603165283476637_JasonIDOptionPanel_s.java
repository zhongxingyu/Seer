 //----------------------------------------------------------------------------
 // Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //
 // To contact the authors:
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 //----------------------------------------------------------------------------
 
 package jason.jeditplugin;
 
 
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 import javax.swing.filechooser.FileFilter;
 
 import org.gjt.sp.jedit.AbstractOptionPane;
 
 public class JasonIDOptionPanel extends AbstractOptionPane  {
 	
 	private static final long serialVersionUID = 1L;
 
 	JTextField jasonTF;
 	JTextField javaTF;
 	JTextField antTF;
 	JTextField shellTF;
 	//JCheckBox  insideJIDECBox;
 	JCheckBox  closeAllCBox;
     JCheckBox  checkVersionCBox;
 
 	JTextField saciTF;
 	JTextField jadeJarTF;
 	JTextField jadeArgsTF;
 
 	static Config userProperties = Config.get();
 
 	static {
         String currJasonVersion = userProperties.getJasonRunningVersion();
 
 		// check new version
     	//File jasonConfFile = getUserConfFile();
     	if (userProperties.getProperty("version") != null) {
     		//userProperties.load(new FileInputStream(jasonConfFile));
     		if (!userProperties.getProperty("version").equals(currJasonVersion) && !currJasonVersion.equals("?")) { 
     			// new version, set all values to default
     			System.out.println("This is a new version of Jason, reseting configuration...");
     			//userProperties.clear();
     			userProperties.remove(Config.SACI_JAR);
     			userProperties.remove(Config.JADE_JAR);
     			userProperties.remove(Config.JASON_JAR);
                 userProperties.remove(Config.ANT_LIB);
                 userProperties.remove(Config.CHECK_VERSION);
     		}
     	} 
 
     	userProperties.fix();
     	userProperties.store();
 	}
 	
 	public JasonIDOptionPanel() {
 		super("Jason");
 	}
 
 	protected void _init() {
		JPanel pop = new JPanel(new GridLayout(0,1));
 
     	// jason home
     	jasonTF = new JTextField(25);
     	JPanel jasonHomePanel = new JPanel(new GridLayout(0,1));
     	jasonHomePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
 				.createEtchedBorder(), "Jason", TitledBorder.LEFT, TitledBorder.TOP));
         JPanel jasonJarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
     	jasonJarPanel.add(new JLabel("jason.jar location"));
     	jasonJarPanel.add(jasonTF);
     	jasonJarPanel.add(createBrowseButton("jason.jar", jasonTF));
         jasonHomePanel.add(jasonJarPanel);
 
         // jason check version
         JPanel checkVersionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         //checkVersionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Jason options", TitledBorder.LEFT, TitledBorder.TOP));
        checkVersionCBox = new JCheckBox("Check for Jason new version on startup", false);
         checkVersionPanel.add(checkVersionCBox);
         jasonHomePanel.add(checkVersionPanel);
     	pop.add(jasonHomePanel);
     	
 
     	// java home
     	JPanel javaHomePanel = new JPanel();
     	javaHomePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
 				.createEtchedBorder(), "Java Home", TitledBorder.LEFT, TitledBorder.TOP));
     	javaHomePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
     	javaHomePanel.add(new JLabel("Directory"));
     	javaTF = new JTextField(25);
     	javaHomePanel.add(javaTF);
     	JButton setJava = new JButton("Browse");
     	setJava.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 	            try {
 	                JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
 					 chooser.setDialogTitle("Select the Java JDK Home directory");
 	                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 	                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
 	                	String javaHome = (new File(chooser.getSelectedFile().getPath())).getCanonicalPath();
 	                	if (Config.checkJavaHomePath(javaHome)) {
 	                		javaTF.setText(javaHome);
 	                	} else {
 	                		JOptionPane.showMessageDialog(null, "The selected JDK home directory has not the file bin/javac inside!");
 	                	}
 	                }
 	            } catch (Exception e) {}
 			}
     	});
     	javaHomePanel.add(setJava);
     	pop.add(javaHomePanel);
     	
     	// ant lib home
     	JPanel antHomePanel = new JPanel();
     	antHomePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
 				.createEtchedBorder(), "Ant libs", TitledBorder.LEFT, TitledBorder.TOP));
     	antHomePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
     	antHomePanel.add(new JLabel("Directory"));
     	antTF = new JTextField(25);
     	antHomePanel.add(antTF);
     	JButton setAnt = new JButton("Browse");
     	setAnt.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 	            try {
 	                JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
                     chooser.setDialogTitle("Select the directory with ant.jar and ant-launcher.jar files");
 	                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 	                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
 	                	String antLib = (new File(chooser.getSelectedFile().getPath())).getCanonicalPath();
 	                	if (Config.checkAntLib(antLib)) {
 	                		antTF.setText(antLib);
 	                	} else {
 	                		JOptionPane.showMessageDialog(null, "The selected directory has not the files ant.jar and ant-launcher.jar!");
 	                	}
 	                }
 	            } catch (Exception e) {}
 			}
     	});
     	antHomePanel.add(setAnt);
     	pop.add(antHomePanel);
 
     	
     	// jade home
     	jadeJarTF  = new JTextField(25);
     	jadeArgsTF = new JTextField(30);
     	JPanel jadeHomePanel = new JPanel(new GridLayout(0,1));
     	jadeHomePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
 				.createEtchedBorder(), "Jade", TitledBorder.LEFT, TitledBorder.TOP));
 
         JPanel jadeJarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
     	jadeJarPanel.add(new JLabel("jade.jar location"));
     	jadeJarPanel.add(jadeJarTF);
     	jadeJarPanel.add(createBrowseButton("jade.jar", jadeJarTF));
         jadeHomePanel.add(jadeJarPanel);
         JPanel jadeArgsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
     	jadeArgsPanel.add(new JLabel("jade.Boot arguments"));
     	jadeArgsPanel.add(jadeArgsTF);
         jadeHomePanel.add(jadeArgsPanel);
     	pop.add(jadeHomePanel);
 
     	// saci home
     	JPanel saciHomePanel = new JPanel();
     	saciHomePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
 				.createEtchedBorder(), "Saci", TitledBorder.LEFT, TitledBorder.TOP));
     	saciHomePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
     	saciHomePanel.add(new JLabel("saci.jar location"));
     	saciTF = new JTextField(25);
     	saciHomePanel.add(saciTF);
     	saciHomePanel.add(createBrowseButton("saci.jar", saciTF));
     	pop.add(saciHomePanel);
 
     	
     	// shell command
         /*
     	JPanel shellPanel = new JPanel();
     	shellPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
 				.createEtchedBorder(), "Shell command", TitledBorder.LEFT, TitledBorder.TOP));
     	shellPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
     	shellTF = new JTextField(30);
     	shellTF.setToolTipText("This command will be used to run the scripts that run the MAS.");
     	shellPanel.add(shellTF);
     	pop.add(shellPanel);
         */
         
     	// run centralised inside jIDE
     	/*
     	JPanel insideJIDEPanel = new JPanel();
     	insideJIDEPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Centralised MAS execution mode", TitledBorder.LEFT, TitledBorder.TOP));
     	insideJIDEPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
     	insideJIDECBox = new JCheckBox("Run MAS as a JasonIDE internal thread instead of another process.");
     	insideJIDEPanel.add(insideJIDECBox);
     	pop.add(insideJIDEPanel);
     	*/
 
     	// close all before opening mas project
     	JPanel closeAllPanel = new JPanel();
     	closeAllPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "jEdit options", TitledBorder.LEFT, TitledBorder.TOP));
     	closeAllPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
     	closeAllCBox = new JCheckBox("Close all files before opening a new Jason Project.");
     	closeAllPanel.add(closeAllCBox);
     	pop.add(closeAllPanel);
 

         addComponent(pop);
     	
     	saciTF.setText(userProperties.getSaciJar());
     	jadeJarTF.setText(userProperties.getJadeJar());
     	jadeArgsTF.setText(userProperties.getJadeArgs());
     	jasonTF.setText(userProperties.getJasonJar());
     	javaTF.setText(userProperties.getJavaHome());
     	antTF.setText(userProperties.getAntLib());
     	//shellTF.setText(userProperties.getShellCommand());
     	//insideJIDECBox.setSelected(userProperties.runAsInternalTread());
     	closeAllCBox.setSelected(userProperties.getBoolean(Config.CLOSEALL));
         checkVersionCBox.setSelected(userProperties.getBoolean(Config.CHECK_VERSION));
 	}
 
     private JButton createBrowseButton(final String jarfile, final JTextField tf) {
     	JButton bt = new JButton("Browse");
     	bt.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 	            try {
 	                JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
 					chooser.setDialogTitle("Select the "+jarfile+" file");
 					chooser.setFileFilter(new JarFileFilter(jarfile, "The "+jarfile+" file"));
 					//chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 	                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
 	                	String selJar = (new File(chooser.getSelectedFile().getPath())).getCanonicalPath();
 	                	if (Config.checkJar(selJar)) {
 	                		tf.setText(selJar);
 	                	} else {
 	                		JOptionPane.showMessageDialog(null, "The selected "+jarfile+" file was not ok!");
 	                	}
 	                }
 	            } catch (Exception e) {}
 			}
     	});
         return bt;
     }
 
 	protected void _save() {
 		if (Config.checkJar(saciTF.getText())) {
 			userProperties.put(Config.SACI_JAR, saciTF.getText().trim());
 		}
 
 		if (Config.checkJar(jadeJarTF.getText())) {
 			userProperties.put(Config.JADE_JAR, jadeJarTF.getText().trim());
 		}
         userProperties.put(Config.JADE_ARGS, jadeArgsTF.getText().trim());
 
 		if (Config.checkJar(jasonTF.getText())) {
 			userProperties.put(Config.JASON_JAR, jasonTF.getText().trim());
 		}
 
 		if (Config.checkJavaHomePath(javaTF.getText())) {
 			userProperties.setJavaHome(javaTF.getText().trim());
 		}
 
 		if (Config.checkAntLib(antTF.getText())) {
 			userProperties.setAntLib(antTF.getText().trim());
 		}
 
 		//userProperties.put(Config.SHELL_CMD, shellTF.getText().trim());
 		//userProperties.put(Config.RUN_AS_THREAD, insideJIDECBox.isSelected()+"");
 		userProperties.put(Config.CLOSEALL, closeAllCBox.isSelected()+"");
         userProperties.put(Config.CHECK_VERSION, checkVersionCBox.isSelected()+"");
 		userProperties.store();
 	}
 
     class JarFileFilter extends FileFilter {
 		String jar,ds;
 		public JarFileFilter(String jar, String ds) {
 			this.jar = jar;
 			this.ds  = ds;
 		}
         public boolean accept(File f) {
             if (f.getName().endsWith(jar) || f.isDirectory()) {
 				return true;
             } else {
 				return false;
             }
         }
         
         public String getDescription() {
             return ds;
         }
     }
 }
