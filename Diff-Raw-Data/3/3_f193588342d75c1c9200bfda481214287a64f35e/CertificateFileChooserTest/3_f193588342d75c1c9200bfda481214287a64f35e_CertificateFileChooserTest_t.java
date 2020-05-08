 package nl.nikhef.jgridstart.gui.util;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 
 import junit.extensions.abbot.ComponentTestFixture;
 
 import org.junit.Test;
 
 import abbot.finder.BasicFinder;
 import abbot.finder.ComponentNotFoundException;
 import abbot.finder.Matcher;
 import abbot.finder.MultipleComponentsFoundException;
 import abbot.tester.ComponentTester;
 
 
 public class CertificateFileChooserTest extends ComponentTestFixture {
     
     /** file selected @see #createTestChooser */
     private File selectedFile = null;
     /** text for ok button */
     private final String okBtnTxt = "doit";
     /** Abbot tester */
     private ComponentTester tester = null;
 
     @Override
     protected void setUp() throws IOException {
 	tester = new ComponentTester();
     }
     @Override
     protected void tearDown() {
 	// Default JUnit test runner keeps references to Tests for its
 	// lifetime, so TestCase fields won't be GC'd for the lifetime of the
 	// runner. 
 	tester = null;
     }
     
     /** helper method: create custom test file chooser */
     protected JDialog createTestChooser(File srcdir) {
 	// embed in frame with password selection fields
	final JDialog dlg = new JDialog();
	dlg.setTitle("Custom test filechooser");
 	final JFileChooser chooser = new JFileChooser(srcdir);
 	
 	selectedFile = null;
 
 	JPanel pane = CertificateFileChooser.customFileChooser(dlg, chooser,
 		new AbstractAction(okBtnTxt) {
         	    public void actionPerformed(ActionEvent e) {
         		try {
         		    selectedFile = chooser.getSelectedFile();
         		} catch (Exception e1) {
         		    e1.printStackTrace();
         		}
         		dlg.dispose();
         	    }
 		}
 	);
 	
 	dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 	dlg.pack();
 	dlg.setModal(false); // for easy testing!f
 	return dlg;
     }
     /** helper method: create custom test file chooser */
     protected JDialog createTestChooser() {
 	return createTestChooser(null);
     }
 
     /** Helper method: press custom filechooser ok button */
     protected void pressOk() throws ComponentNotFoundException, MultipleComponentsFoundException {
 	JButton btn = (JButton) new BasicFinder().find(new Matcher() {
 	    public boolean matches(Component c) {
 		return c instanceof JButton && ((JButton)c).getText() == okBtnTxt;
 	    }
 	});
 	tester.actionClick(btn);
     }
     /** Helper method: wait for file to be selected and return it */
     protected File waitSelected() throws Exception {
 	for (int i=0; i<1000; i++) {
 	    if (selectedFile!=null) return selectedFile;
 	    sleep();
 	}
 	throw new Exception("Timeout waiting for file to be selected");
     }
 
     
     /** Test filename with ok button */
     @Test
     public void testCustomFilenameClick() throws Exception {
 	JDialog dlg = createTestChooser();
 	dlg.setVisible(true);
 	tester.waitForIdle();
 	tester.keyString("foobar.xyz");
 	pressOk();
 	assertEquals("foobar.xyz", waitSelected().getName());
     }
 
     /** Test filename with enter */
     @Test
     public void testCustomFilenameEnter() throws Exception {
 	JDialog dlg = createTestChooser();
 	dlg.setVisible(true);
 	tester.waitForIdle();
 	tester.keyString("foobar.xyz\n");
 	assertEquals("foobar.xyz", waitSelected().getName());
     }
 
     /** Test filename+dir with ok button */
     @Test
     public void testCustomCheckpathClick() throws Exception {
 	File dir = new File(System.getProperty("java.io.tmpdir"));
 	JDialog dlg = createTestChooser(dir);
 	dlg.setVisible(true);
 	tester.waitForIdle();
 	tester.keyString("foo.txt");
 	pressOk();
 	assertEquals(new File(dir, "foo.txt").getCanonicalPath(), waitSelected().getCanonicalPath());
     }
 
     /** Test filename+dir with enter */
     @Test
     public void testCustomCheckpathEnter() throws Exception {
 	File dir = new File(System.getProperty("java.io.tmpdir"));
 	JDialog dlg = createTestChooser(dir);
 	dlg.setVisible(true);
 	tester.waitForIdle();
 	tester.keyString("bar.xyz\n");
 	assertEquals(new File(dir, "bar.xyz").getCanonicalPath(), waitSelected().getCanonicalPath());
     }
     
     /** Test full filename entering with ok button */
     @Test
     public void testCustomFullpathClick() throws Exception {
 	File f = new File(System.getProperty("java.io.tmpdir"), "yeah.bl");
 	JDialog dlg = createTestChooser();
 	dlg.setVisible(true);
 	tester.waitForIdle();
 	tester.keyString(f.getPath());
 	pressOk();
 	assertEquals(f.getCanonicalPath(), waitSelected().getCanonicalPath());
     }
    
     /** Test full filename entering with enter */
     @Test
     public void testCustomFullpathEnter() throws Exception {
 	File f = new File(System.getProperty("java.io.tmpdir"), "yeah.cl");
 	JDialog dlg = createTestChooser();
 	dlg.setVisible(true);
 	tester.waitForIdle();
 	tester.keyString(f.getPath()+"\n");
 	assertEquals(f.getCanonicalPath(), waitSelected().getCanonicalPath());
     }
 }
