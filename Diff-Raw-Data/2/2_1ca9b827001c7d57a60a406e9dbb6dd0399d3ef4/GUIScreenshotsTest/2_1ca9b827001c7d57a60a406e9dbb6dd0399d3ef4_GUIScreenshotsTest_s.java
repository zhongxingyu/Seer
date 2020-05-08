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
 import java.lang.reflect.InvocationTargetException;
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
     
     /** Make screenshot taking part of unit tests */
     @Test
     public static void testScreenshots() throws Exception {
 	File shotdir = FileUtils.createTempDir("jgridstart-screenshots-");
 	try {
 	    doScreenshots(shotdir);
 	} catch(Throwable e) {
 	    // on error, output final screenshot as base64 on debug log
 	    File errorshot = new File(shotdir, "error.png");
 	    saveScreenshot(errorshot);
 	    Thread.sleep(500);
 	    FileInputStream in = new FileInputStream(errorshot);
 	    byte[] data = new byte[(int)errorshot.length()];
 	    in.read(data, 0, data.length);
 	    // need to log in chunks because logger doesn't seem to be able to support >4096 chars 
 	    String basedata = new String(Base64.encode(data));
 	    logger.finest("Interactive UI testing failed, last screenshot (base64 encoded):");
 	    logger.finest("=== BEGIN PNG ===");
 	    int pos = 0;
 	    while (pos < basedata.length()) {
 		int len = 1024;
 		if (pos+len < basedata.length())
 		    logger.finest(basedata.substring(pos, pos+len));
 		else 
 		    logger.finest(basedata.substring(pos));
 		pos += len;
 	    }
 	    logger.finest("=== END PNG ===");
 	    // destroy window
 	    Window mainwnd = AWT.getActiveWindow();
 	    if (mainwnd!=null && mainwnd.isVisible()) mainwnd.dispose();
 	    // pass on error
 	    if (e instanceof Exception) throw (Exception)e;
 	    else if (e instanceof Error) throw (Error)e;
 	    else throw new Exception("Unknown throwable: ", e);
     	} finally {
 	    // remove screenshot directory again
     	    FileUtils.recursiveDelete(shotdir);
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
 	logger.info("Setting up jGridstart interactive screenshot and testing environment");
 	File tmphome = FileUtils.createTempDir("jgridstart-home");
 	Window mainwnd = null;
 	try {
 	    System.setProperty("jgridstart.ca.provider", "LocalCA");
 	    System.setProperty("jgridstart.ca.local.hold", "true");
 	    System.setProperty("user.home", tmphome.getCanonicalPath());
 	    // create standard gui
 	    nl.nikhef.jgridstart.gui.Main.main(new String[]{});
 	    LogHelper.setupLogging(true);
 	    // move mouse here since closing window may give up focus later
 	    Thread.sleep(2000); guiSleep();
 	    mainwnd = AWT.getActiveWindow();
 	    assertNotNull(mainwnd);
 	    tester.mouseMove(mainwnd.getComponents()[0]);
 	    assertWindowname("jgridstart-main-window");
 
 	    /*
 	     * Request new
 	     */
 	    logger.info("Interactive testing scenario: Request New");
 	    // start screen
 	    saveScreenshot(new File(shotdir, prefix+"newrequest01.png"));
 	    // new request wizard
 	    guiSleep(); tester.key(new Integer('N'), InputEvent.CTRL_MASK);
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"newrequest02.png"));
 	    // enter details
 	    guiSleep();
 	    assertWindowname("jgridstart-requestwizard-0");
 	    focusByName("givenname"); 
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
 	    logger.info("Interactive testing scenario: Renewal");
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
 	    Thread.sleep(500);
 	    guiSleep();
 	    saveScreenshot(new File(shotdir, prefix+"renew02.png"));
 	    assertWindowname("jgridstart-requestwizard-0");
 	    focusByName("email");
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
 	    logger.info("Interactive testing scenario: Import/Export");
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
 	    assertWindowname("jgridstart-main-window");
 	    
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
 	    logger.info("Interactive testing scenario: Certificate details");
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
 	    logger.info("Interactive testing finished");
 	    /* Quit does a {@link System.exit}, which JUnit doesn't like. The
 	     * error it gives is something like:
 	     *   [junit] Test <testclass> FAILED (crashed)
 	     * So we leave it to the calling function to dispose of the window.
 	     */
 	    //tester.key(new Integer('Q'), InputEvent.CTRL_MASK);
 	    
 	} finally {
 	    guiSleep(); Thread.sleep(500); // for screenshot to complete ...
 	    FileUtils.recursiveDelete(tmphome);
 	}
 	// exit!
 	return;
     }
     
     /** Write screenshot of current window to specified file as png.
      * <p>
      * Assumes a single screen. */
     protected static void saveScreenshot(final File dst) throws AWTException, IOException, InterruptedException, InvocationTargetException {
 	final Robot robot = new Robot();
 	guiSleep(); guiSleep(); guiSleep();
 	// capture screen
 	javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
 	    public void run() {
 		logger.info("Saving screenshot: "+dst);
 		try {
 		    // find active window area, or full desktop if that fails
 		    Window w = AWT.getActiveWindow();
 		    Rectangle captureSize = w != null ? w.getBounds() :
 			new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
 		    // take image
 		    BufferedImage img = robot.createScreenCapture(captureSize);
 		    img.flush();
 		    ImageIO.write(img, "png", dst);
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
 	    logger.fine("Detecting robot keymapping");
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
     protected static void assertWindowname(String name) throws InterruptedException, ComponentNotFoundException {
 	logger.fine("Expecting window name: "+name);
 	Window w = null;
 	for (int i=0; i<10; i++) {
 	    guiSleep();
 	    w = AWT.getActiveWindow();
 	    if (w==null) continue;
 	    if (name.equals(w.getName())) return;
 	    Thread.sleep(100);
 	}
 	throw new ComponentNotFoundException("Window name not found: "+name + (w!=null ? (" (currently focused: "+w.getName()+")") : ""));
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
 	if (c==null) throw new ComponentNotFoundException("Component not found or enabled: "+text);
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
     
     /** Gives focus to a {@linkplain Component} by its name */
     protected static boolean focusByName(final String name) throws ComponentNotFoundException, MultipleComponentsFoundException {
 	Component c = findByName(name);
 	if (c.hasFocus()) return true;
 	// try ordinary method
 	if (c.requestFocusInWindow()) {
 	    while (!c.hasFocus()) guiSleep();
 	    return true;
 	}
 	// press tab until we have the correct focus
 	for (int i=0; i<25 /* TODO proper number */; i++) {
 	    tester.keyStroke('\t');
 	    guiSleep();
 	    if (name.equals(AWT.getActiveWindow().getFocusOwner().getName())) return true;
 	}
 	// failed ...
 	logger.warning("Could not give focus to component: "+name);
 	return true;
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
