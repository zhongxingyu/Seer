 package nl.nikhef.jgridstart.gui;
 
 import javax.swing.AbstractAction;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import nl.nikhef.jgridstart.gui.util.URLLauncherCertificate;
 
 import java.awt.event.ActionEvent;
 import java.util.logging.Logger;
 
 /** Show "About" dialog */
 public class ActionAbout extends AbstractAction {
 
     static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui");
 
     public ActionAbout(JFrame parent) {
 	super();
 	putValue(NAME, "About...");
 	putValue(MNEMONIC_KEY, new Integer('A'));
 	URLLauncherCertificate.addAction("about", this);
     }
 
     public void actionPerformed(ActionEvent e) {
 	logger.finer("Action: "+getValue(NAME));
 	JOptionPane.showMessageDialog(CertificateAction.findWindow(e.getSource()),
 		"jGridstart gives you a hassle-free start with the grid.\n" +
 		"At least I hope so!\n" +
 		"\n" +
 		"jGridstart version: "+System.getProperty("jgridstart.version")+ 
 			" (rev "+System.getProperty("jgridstart.revision")+")\n" +
 		"Java runtime environment version: "+System.getProperty("java.version"),
		"About jGridstart", JOptionPane.INFORMATION_MESSAGE);
     }
 }
