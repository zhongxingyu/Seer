 package nl.nikhef.jgridstart.gui.util;
 
 import java.awt.AWTException;
 import java.awt.Component;
 import java.awt.Rectangle;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.text.JTextComponent;
 
 import junit.framework.TestCase;
 
 import org.bouncycastle.util.encoders.Base64;
 import org.junit.Test;
 
 import nl.nikhef.jgridstart.logging.LogHelper;
 import nl.nikhef.jgridstart.util.FileUtils;
 import nl.nikhef.jgridstart.util.PasswordCache;
 
 import abbot.finder.BasicFinder;
 import abbot.finder.ComponentNotFoundException;
 import abbot.finder.Matcher;
 import abbot.finder.MultipleComponentsFoundException;
 import abbot.tester.ComponentTester;
 import abbot.util.AWT;
 
 /** Generate screenshots the for documentation of jGridstart */
 public class GUIScreenshotsTest extends TestCase {
     
     static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui.util");
     protected static ComponentTester tester = new ComponentTester();
     
     /** password used for test certificate */
     protected static String password = "test123pass";
     
     /** replacement characters for {@link #keyString} */
     protected static HashMap<Character, Character> replacemap = null;
     
     /** last screenshot taken */
     protected static File lastScreenshot = null; 
     
     /** Make screenshot taking part of unit tests */
     @Test
     public static void testScreenshots() throws Exception {
 	File ssdir = FileUtils.createTempDir("jgridstart-screenshots-");
 	try {
 	    doScreenshots(ssdir);
 	} catch(Throwable e) {
 	    // on error, output last screenshot as base64 on debug log
 	    if (lastScreenshot!=null) {
 		FileInputStream in = new FileInputStream(lastScreenshot);
 		StringBuffer base64 = new StringBuffer();
 		byte[] c = new byte[100];
 		while (in.read(c)>0) base64.append(new String(Base64.encode(c)));
 		logger.finest("Interactive UI testing failed, last screenshot:");
 		logger.finest("[IMG "+lastScreenshot.getName()+"] "+base64.toString());
 	    }
 	    // destroy window
 	    Window mainwnd = AWT.getActiveWindow();
 	    if (mainwnd!=null && mainwnd.isVisible()) mainwnd.dispose();
 	    // pass on error
 	    if (e instanceof Exception) throw (Exception)e;
 	    else if (e instanceof Error) throw (Error)e;
 	    else throw new Exception("Unknown throwable: ", e);
     	} finally {
 	    // remove screenshot directory again
     	    FileUtils.recursiveDelete(ssdir);
 	}
     }
     
     /** User-callable screenshot taking program */
     public static void main(String[] args) throws Exception {
 	// screenshot output directory
 	if (args.length!=1) {
 	    System.err.println("please give screenshot dir as argument");
 	    return;
 	}
 	doScreenshots(new File(args[0]));
 	System.exit(0);
     }
     
     public static void doScreenshots(File shotdir) throws Exception {
 	shotdir.mkdirs();
 	String prefix = "jgridstart-screenshot-";
 	// setup temporary environment
 	File tmphome = FileUtils.createTempDir("jgridstart-home");
 	Window mainwnd = null;
 	try {
 	    System.setProperty("jgridstart.ca.provider", "LocalCA");
 	    System.setProperty("jgridstart.ca.local.hold", "true");
 	    System.setProperty("user.home", tmphome.getCanonicalPath());
 	    // create standard gui
 	    nl.nikhef.jgridstart.gui.Main.main(new String[]{});
 	    // move mouse here since closing window may give up focus later
 	    Thread.sleep(2000); guiSleep();
 	    mainwnd = AWT.getActiveWindow();
 	    assertNotNull(mainwnd);
 	    tester.mouseMove(mainwnd.getComponents()[0]);
 	    assertWindowname("jgridstart-main-window");
 
 	    /*
 	     * Request new
 	     */
 	    // start screen
 	    saveScreenshot(new File(shotdir, prefix+"newrequest01.png"));
 	    // new request wizard
 	    guiSleep(); tester.key(new Integer('N'), InputEvent.CTRL_MASK);
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"newrequest02.png"));
 	    // enter details
 	    guiSleep();
 	    assertWindowname("jgridstart-requestwizard-0");
 	    findByName("givenname").requestFocus(); guiSleep(); // until focus works on all configurations 
 	    keyString("John\t");
 	    keyString("Doe\t");
 	    keyString("john.doe@example.com\t");
 	    keyString("N\t");
 	    keyString(" \t\t");
 	    keyString(password+"\t");
 	    keyString(password+"\t");
 	    // wait for submission
 	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"newrequest03.png"));
 	    assertWindowname("jgridstart-requestwizard-1");
 	    waitEnabled(JButton.class, "Next");
 	    // verification form
 	    System.setProperty("wizard.show.help1", "true"); // simulate help btn1 pressed
 	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
 	    guiSleep();
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"newrequest04.png"));
 	    assertWindowname("jgridstart-requestwizard-2");
 	    // form display
 	    JButton btn = (JButton) new BasicFinder().find(new Matcher() {
 		public boolean matches(Component c) {
 		    return c instanceof JButton && ((JButton)c).getText().equals("display form");
 		}
 	    });
 	    btn.doClick();
 	    waitEnabled(JButton.class, "Close");
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"newrequest05.png"));
 	    assertWindowname("jgridstart-verification-form");
 	    tester.key(new Integer('C'), InputEvent.ALT_MASK);
 	    // close wizard
 	    guiSleep();
 	    assertWindowname("jgridstart-requestwizard-2");
 	    tester.key(new Integer('C'), InputEvent.ALT_MASK);
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"newrequest06.png"));
 	    assertWindowname("jgridstart-main-window");
 	    // enable certificate in LocalCA and refresh pane
 	    System.setProperty("jgridstart.ca.local.hold", "false");
 	    tester.key(KeyEvent.VK_F5);
 	    Thread.sleep(1000);
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"newrequest07.png"));
 	    assertWindowname("jgridstart-main-window");
 	    // show request wizard again
 	    tester.key(new Integer('A'), InputEvent.ALT_MASK);
 	    tester.key('R');
 	    guiSleep();
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"newrequest08.png"));
 	    assertWindowname("jgridstart-requestwizard-2");
 	    // install step
 	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
 	    Thread.sleep(1000);
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"newrequest09.png"));
 	    assertWindowname("jgridstart-requestwizard-3");
 	    // show final screen
 	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"newrequest10.png"));
 	    assertWindowname("jgridstart-requestwizard-4");
 	    // exit wizard
 	    tester.key(new Integer('C'), InputEvent.ALT_MASK);
 	    // save final screenshot
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"newrequest11.png"));
 	    assertWindowname("jgridstart-main-window");
 	    guiSleep();
 	    
 	    /*
 	     * Renewal
 	     */
 	    System.setProperty("jgridstart.ca.local.hold", "true");
 	    // forget password so we certainly get the password dialog
 	    PasswordCache.getInstance().clear();
 	    // start screen
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"renew01.png"));
 	    assertWindowname("jgridstart-main-window");
 	    // personal details
 	    tester.key(new Integer('A'), InputEvent.ALT_MASK);
 	    tester.key('W');
 	    guiSleep();
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"renew02.png"));
 	    assertWindowname("jgridstart-requestwizard-0");
 	    findByName("email").requestFocus(); guiSleep(); // until focus works on all configurations
 	    keyString("\t");
 	    keyString(password+"\t");
 	    keyString(password+"\t");
 	    keyString(password+"\t");
 	    // wait for submission screen
 	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
 	    // renew03.png used to be a password dialog, which was removed
 	    // submit page
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"renew04.png"));
 	    assertWindowname("jgridstart-requestwizard-1");
 	    waitEnabled(JButton.class, "Next");
 	    // wait for approval page
 	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"renew05.png"));
 	    assertWindowname("jgridstart-requestwizard-2");
 	    // close wizard
 	    guiSleep();
 	    tester.key(new Integer('C'), InputEvent.ALT_MASK);
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"renew06.png"));
 	    assertWindowname("jgridstart-main-window");
 	    // enable certificate in LocalCA and refresh pane
 	    System.setProperty("jgridstart.ca.local.hold", "false");
 	    tester.key(KeyEvent.VK_F5);
 	    Thread.sleep(1000);
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"renew07.png"));
 	    assertWindowname("jgridstart-main-window");
 	    // show request wizard again
 	    tester.key(new Integer('A'), InputEvent.ALT_MASK);
 	    tester.key('R');
 	    waitEnabled(JButton.class, "Next");
 	    Thread.sleep(500);
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"renew08.png"));
 	    assertWindowname("jgridstart-requestwizard-2");
 	    // install step
 	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
 	    Thread.sleep(1000);
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"renew09.png"));
 	    assertWindowname("jgridstart-requestwizard-3");
 	    // exit wizard
 	    tester.key(new Integer('C'), InputEvent.ALT_MASK);
 	    // save final screenshot
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"renew10.png"));
 	    assertWindowname("jgridstart-main-window");
 	    guiSleep();
 	    
 	    /*
 	     * Import/export
 	     */
 	    // forget password so we certainly get the password dialog
 	    PasswordCache.getInstance().clear();
 	    // starting screenshot (multiple certificates)
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"importexport01.png"));
 	    assertWindowname("jgridstart-main-window");
 	    // export dialog
 	    tester.key(new Integer('E'), InputEvent.CTRL_MASK);
 	    waitEnabled(JButton.class, "Export");
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"importexport02.png"));
 	    assertWindowname("jgridstart-export-file-dialog");
 	    // enter name and do export
 	    tester.keyString("my_certificate.p12\n");
 	    Thread.sleep(2000);
 	    saveScreenshot(new File(shotdir, prefix+"importexport03.png"));
 	    assertWindowname("jgridstart-password-entry-decrypt");
 	    tester.keyString(password+"\n");
 	    guiSleep();
 	    
 	    // forget password so we certainly get the password dialog
 	    PasswordCache.getInstance().clear();
 	    
 	    // import dialog
 	    tester.key(new Integer('I'), InputEvent.CTRL_MASK);
 	    waitEnabled(JButton.class, "Import");
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"importexport04.png"));
 	    assertWindowname("jgridstart-import-file-dialog");
 	    guiSleep();
 	    // enter name and do import
 	    tester.keyString("my_certificate.p12\n");
 	    Thread.sleep(1000);
 	    saveScreenshot(new File(shotdir, prefix+"importexport05.png"));
 	    assertWindowname("jgridstart-password-entry-decrypt");
 	    keyString(password+"\n");
 	    guiSleep();
 
 	    /*
 	     * Certificate details
 	     */
 	    // certificate details view
 	    mainwnd.setSize(750, 480);
 	    System.setProperty("view.showdetails", "true");
 	    URLLauncherCertificate.performAction("viewlist(false)", tester.findFocusOwner());
 	    tester.key(KeyEvent.VK_F5);
 	    Thread.sleep(500);
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"viewdetails01.png"));
 	    assertWindowname("jgridstart-main-window");
 	    
 	    /*
 	     * Exit!
 	     */
 	    tester.key(new Integer('Q'), InputEvent.CTRL_MASK);
 	    
 	} finally {
 	    guiSleep(); Thread.sleep(500); // for screenshot to complete ...
 	    FileUtils.recursiveDelete(tmphome);
 	}
 	// exit!
 	return;
     }
     
     /** Write screenshot of current screen to specified file as png.
      * <p>
      * Assumes a single screen. */
     protected static void saveScreenshot(final File dst) throws AWTException, IOException {
 	final Robot robot = new Robot();
 	guiSleep(); guiSleep(); guiSleep();
 	// capture screen
 	javax.swing.SwingUtilities.invokeLater(new Runnable() {
 	    public void run() {
 		try {
 		    // find active window area, or full desktop if that fails
 		    Window w = AWT.getActiveWindow();
 		    Rectangle captureSize = w != null ? w.getBounds() :
 			new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
 		    // take image
 		    BufferedImage img = robot.createScreenCapture(captureSize);
 		    img.flush();
 		    ImageIO.write(img, "png", dst);
 		    lastScreenshot = dst;
 		} catch(IOException e) {
 		    System.err.println(e);
 		}
 	    }
 	});
     }
     
     protected static void guiSleep() {
 	// process gui events
 	tester.waitForIdle();
     }
     
     /** Like {@link ComponentTester#keyString}, but correcting some characters.
      * <p>
      * While Abbot has features to deal with different locales, I have experienced
      * problems where at {@code @} appeared to be typed as {@code "}. This can result,
      * for example, in an invalid email address. This method tries to work around the
      * blocking issues I've encountered here (very crude method though).
      * @throws InterruptedException 
      */
     protected static void keyString(String s) throws AWTException, InterruptedException {
 	char[] c = s.toCharArray();
 	// initialize when needed
 	if (replacemap==null) {
 	    replacemap = new HashMap<Character, Character>();
 	    // create textbox, type in each character, store result
 	    final String chars = "1234567890";
 	    JFrame frame = new JFrame("Detecting key mapping (don't type yourself!)");
 	    JTextField field = new JTextField("", 10);
 	    frame.add(field);
 	    frame.setSize(200, 100);
 	    frame.setVisible(true);
 	    for (int i=0; i<chars.length(); i++) {
 		try {
 		    field.setText("");
 		    tester.setModifiers(InputEvent.SHIFT_MASK, true);
 		    tester.keyStroke(chars.charAt(i));
 		    tester.setModifiers(InputEvent.SHIFT_MASK, false);
 		    guiSleep();
 		    replacemap.put(field.getText().charAt(0), chars.charAt(i));
 		} catch (Exception e) { }
 	    }
 	    frame.setVisible(false);
 	    frame.dispose();
 	}
 	
 	for (int i=0; i<c.length; i++) {
 	    if (replacemap.containsKey(c[i])) {
 		guiSleep();
 		tester.setModifiers(InputEvent.SHIFT_MASK, true);
 		tester.keyStroke(replacemap.get(c[i]));
 		tester.setModifiers(InputEvent.SHIFT_MASK, false);
 		guiSleep();
 	    } else {
 		tester.keyStroke(c[i]);
 	    }
 	}
     }
     
     /** Assert the currently active window has the specified name */
     protected static void assertWindowname(String name) {
 	assertEquals(name, AWT.getActiveWindow().getName());
     }
     
     /** Wait for a component to be present and enabled.
      * <p>
      * @param klass Component descendant, like {@linkplain JLabel}
      * @param text What text the component contains, or {@code null} for any
      */
     protected static void waitEnabled(final Class<?> klass, final String text) throws MultipleComponentsFoundException, InterruptedException, ComponentNotFoundException {
 	Component c = null;
 	long timeout = 40000;
 	long start = System.currentTimeMillis();
 	while ( System.currentTimeMillis() - start < timeout ) {
 	    try {
 		c = (Component)new BasicFinder().find(new Matcher() {
 		    public boolean matches(Component c) {
 			return klass.isInstance(c) && (text==null || text.equals(getComponentText(c))) && c.isEnabled();
 		    }
 		});
 		return;
 	    } catch (Exception e) { }
 	    guiSleep();
 	}
 	if (c==null) throw new ComponentNotFoundException("Component not found or enabled");
     }
     
     /** Return the text of a component, or {@code null} if not supported. */
     protected static String getComponentText(final Component c) {
 	if (c instanceof JButton)
 	    return ((JButton)c).getText();
 	if (c instanceof JLabel)
 	    return ((JLabel)c).getText();
 	if (c instanceof JTextComponent)
 	    return ((JTextComponent)c).getText();
 	// TODO when needed, add others
 	return null;
     }
     
     /** Finds a {@linkplain Component} by its name */
     protected static Component findByName(final String name) throws ComponentNotFoundException, MultipleComponentsFoundException {
 	return new BasicFinder().find(new Matcher() {
 		public boolean matches(Component c) {
 		    return name.equals(c.getName());
 		}
 	});
     }
 }
