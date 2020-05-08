 // P A C K A G E ///////////////////////////////////////////////////////////////////////////////////
 package co.warizmi.passwordcard;
 
 // I M P O R T /////////////////////////////////////////////////////////////////////////////////////
 import static java.lang.System.*;
 import static org.eclipse.swt.SWT.*;
 
 import java.io.FileDescriptor;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.Properties;
 
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.graphics.Region;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Tray;
 import org.eclipse.swt.widgets.TrayItem;
 
 // C L A S S ///////////////////////////////////////////////////////////////////////////////////////
 public class App {
     private static final String TRAY_IMAGE = "/icon.png";
     private static final String DEFAULT_PROPERTIES = "/passwordcard.properties";
     private static final String JS_GET_TABLE = "return document.getElementsByTagName ('table')[0]";
 
     public static void main(String[] aArgs) {
         try {
             String config = DEFAULT_PROPERTIES;
             boolean html = false;
             boolean print = false;
 
             for (String arg : aArgs)
                 if (!arg.startsWith ("-"))
                     config = arg;
                 else if (arg.equals ("--html"))
                     html = true;
                 else if (arg.equals ("--print"))
                     print = true;
                 else
                     throw new IllegalArgumentException (arg);
 
             if (print) {
                 setOut (new PrintStream (new FileOutputStream (FileDescriptor.out), true, "UTF-8"));
                 Card card = new Card (config);
                 out.println (html? card.toHtml () : card.toString ());
             }
             else {
                 new App (config).run ();
             }
         }
         catch (Exception e) {
             err.println ("Unhandled exception. Closing application");
             e.printStackTrace ();
             exit (-1);
         }
     }
 
     Properties mConfig = new Properties ();
     Card mCard;
     boolean mOntop, mRounded;
     int mTransparency, mWidth, mHeight;
 
     Display mDisplay;
     Image mImage;
     TrayItem mCardTray;
     Shell mCardWindow;
     Browser mBrowser;
 
     private App (String aConfigProperties) throws IOException {
         super ();
         String props = aConfigProperties;
         try {
            // TODO Check property existence properly
            mConfig.load (new FileReader (aConfigProperties));
         }
         catch (IOException e) {
             InputStream stream = App.class.getResourceAsStream (DEFAULT_PROPERTIES);
             mConfig.load (new InputStreamReader (stream));
             props = DEFAULT_PROPERTIES;
         }
         mOntop = Boolean.valueOf (mConfig.getProperty ("ontop"));
         mTransparency = Integer.valueOf (mConfig.getProperty ("transparency"));
         mRounded = Boolean.valueOf (mConfig.getProperty ("rounded"));
 
         mCard = new Card (props);
         mDisplay = new Display ();
         mCardTray = createTray ();
         mCardWindow = createWindow ();
         mBrowser = createBrowser ();
     }
 
     void toggleWindow () {
         if (mCardWindow.isVisible ()) {
             Shell wnd = mCardWindow;
             mCardWindow = createWindow ();
             mBrowser.setParent (mCardWindow);
             wnd.close ();
         }
         else {
             mCardWindow.open ();
             if (mWidth == 0 || mHeight == 0) {
                 mWidth =
                     ((Double)mBrowser.evaluate (JS_GET_TABLE + ".clientWidth")).intValue () + 2;
                 mHeight =
                     ((Double)mBrowser.evaluate (JS_GET_TABLE + ".clientHeight")).intValue () + 2;
                 mCardWindow.setSize (mWidth, mHeight);
             }
             Rectangle bounds = mDisplay.getBounds ();
             Point mouseLocation = mDisplay.getCursorLocation ();
             int cursorX = mouseLocation.x, cursorY = mouseLocation.y;
             int x = cursorX > bounds.width / 2?
                 cursorX - mWidth - 10 : cursorX + 10;
             int y = cursorY > bounds.height / 2?
                 cursorY - (mHeight / 2) : cursorY;
             mCardWindow.setLocation (x, y);
         }
     }
 
     private Browser createBrowser () throws IOException {
         Browser browser = new Browser (mCardWindow, NONE);
         browser.setText (mCard.toHtml ());
         browser.setJavascriptEnabled (false);
         browser.setEnabled (false);
         browser.addMouseListener (new MouseAdapter () {
             @Override public void mouseUp (MouseEvent aArg0) {
                 toggleWindow ();
             }
         });
         return browser;
     }
 
     private TrayItem createTray () {
         final Tray tray = mDisplay.getSystemTray ();
 
         if (tray == null)
             throw new IllegalStateException ("The system tray is not available");
 
         mImage = new Image (mDisplay, App.class.getResourceAsStream (TRAY_IMAGE));
 
         final TrayItem item = new TrayItem (tray, NONE);
         item.setToolTipText ("Password Card Tray\nPress CTRL + Click to exit");
         item.setImage (mImage);
 
         item.addSelectionListener (new SelectionListener () {
             @Override public void widgetDefaultSelected (SelectionEvent aEvent) {
                 widgetSelected (aEvent);
             }
 
             @Override public void widgetSelected (SelectionEvent aEvent) {
                 if (aEvent.stateMask == CTRL)
                     item.dispose ();
                 else
                     toggleWindow ();
             }
         });
 
         return item;
     }
 
     private Shell createWindow () {
         final Shell shell = new Shell (mDisplay, mOntop? NO_TRIM | ON_TOP : NO_TRIM);
         shell.setLayout (new FillLayout ());
         shell.setAlpha (mTransparency);
         shell.setImage (mImage);
         shell.setText ("Password Card");
         shell.setSize (mWidth, mHeight);
         if (mRounded) {
             Region r = new Region ();
             shell.setRegion (r);
         }
 
         return shell;
     }
 
     private void run () {
         while (!mCardTray.isDisposed ())
             if (!mDisplay.readAndDispatch ())
                 mDisplay.sleep ();
 
         mCardWindow.dispose ();
         mImage.dispose ();
         mDisplay.dispose ();
     }
 }
 // E O F ///////////////////////////////////////////////////////////////////////////////////////////
