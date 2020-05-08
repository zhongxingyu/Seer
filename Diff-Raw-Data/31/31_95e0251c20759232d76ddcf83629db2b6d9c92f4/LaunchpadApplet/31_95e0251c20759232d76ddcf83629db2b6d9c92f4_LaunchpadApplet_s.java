 package com.rightscale.ssh;
 
 import com.rightscale.ssh.launchers.Launcher;
 import com.rightscale.ssh.*;
 import com.rightscale.util.*;
 import com.rightscale.ssh.launchers.*;
 import com.rightscale.ssh.launchers.java.*;
 
 import java.lang.reflect.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.net.*;
 import java.io.*;
 import java.util.*;
 import java.applet.Applet;
 import java.applet.AppletStub;
 import java.security.*;
 
 public class LaunchpadApplet
         extends Applet
         implements AppletStub
 {
     public static final String AUTH_METHOD_PUBLIC_KEY = "publickey";
     public static final String AUTH_METHOD_PASSWORD   = "password";
     
     public static final String CHOOSING        = "choosing";
     public static final String USING_MINDTERM  = "usingMindterm";
     public static final String FORCED_MINDTERM = "forcedMindterm";
     public static final String USING_NATIVE    = "usingNative";
 
     private SimpleLaunchpad      _launchpad   = new SimpleLaunchpad();
     private boolean              _ranNative   = false;
     private boolean              _ranMindterm = false;
     
     ////
     //// Methods that allow the page's JS to query our state
     ////
 
     public boolean ranNative() {
         return _ranNative;
     }
 
     public boolean ranMindterm() {
         return _ranMindterm;
     }
     
     ////
     //// AppletStub implementation
     ////
 
     public void appletResize( int width, int height ){
         resize( width, height );
     }
     
     ////
     //// Applet implementation
     ////
 
     /**
      * Initialization method that will be called after the applet is loaded
      * into the browser.
      */
     public void init() {
         try {
             AccessController.doPrivileged(new PrivilegedExceptionAction() {
                 public Object run() throws IOException {
                     init_();
                     return null;
                 }
             });
         }
         catch(PrivilegedActionException e) {
             _launchpad.reportError("Failed to acquire the privilege necessary to initialize the applet.", e);
         }
     }
 
     /**
      * Called every time the browser requests a new "instance" of the applet.
      */
     public void start() {
         try {
             AccessController.doPrivileged(new PrivilegedExceptionAction() {
                 public Object run() throws IOException {
                     start_();
                     return null;
                 }
             });
         }
         catch(PrivilegedActionException e) {
             _launchpad.reportError("Failed to acquire the privilege necessary to initialize the applet.", e);
         }
     }
 
     public void stop() {
     }
 
     ////
     //// Properties and accessors.
     ////
 
     protected boolean isAutorun() {
         String v = getParameter("autorun");
         if(v == null) {
             return true;
         }
 
         v = v.toLowerCase();
         if(v.startsWith("y") || v.startsWith("t") || v.startsWith("1")) {
             return true;
         }
         else {
             return false;
         }
     }
 
     protected boolean isAttemptNative() {
         String v = getParameter("native");
         if(v == null) {
             return false;
         }
 
         v = v.toLowerCase();
         if(v.startsWith("y") || v.startsWith("t") || v.startsWith("1")) {
             return true;
         }
         else {
             return false;
         }
     }
 
     protected String getUsername() {
         String v = getParameter("username");
 
         if(v != null)
             return v;
         else
             return "root";
     }
 
     protected String getServer() {
         String v = getParameter("server");
 
         if(v != null)
             return v;
         else
             return "localhost";
     }
 
     protected String getServerUUID() {
         return getParameter("server-uuid");
     }
 
     protected String getAuthMethod() {
         if("publickey".equals(getParameter("auth-method")))
             return AUTH_METHOD_PUBLIC_KEY;
         else if("password".equals(getParameter("auth-method")))
             return AUTH_METHOD_PASSWORD;
         else
             return null;
     }
 
     protected String getServerKeyMaterial() {
         String newline = System.getProperty("line.separator");
         String km = getParameter("openssh-key-material");
         if(km == null) return null;
         return km.replaceAll("\\*", newline);
     }
 
     protected String getServerPuttyKeyMaterial() {
         String newline = System.getProperty("line.separator");
         String km = getParameter("putty-key-material");
         if(km == null) return null;
         return km.replaceAll("\\*", newline);
     }
 
     protected String getPassword() {
         return getParameter("password");
     }
 
     protected String getUserKeyName() {
        String ukn = getParameter("user-key-name");
        if(ukn != null)
            return ukn;
        else
            return "identity";
     }
 
     protected File getServerKeyFile() {
         return new File(_launchpad.getSafeDirectory(), getServerUUID());
     }
 
     protected File getServerPuttyKeyFile() {
         return new File(_launchpad.getSafeDirectory(), getServerUUID() + ".ppk");
     }
 
     protected File getUserKeyFile() {
         return new File(_launchpad.getSafeDirectory(), getUserKeyName());
     }
 
     protected File getUserPuttyKeyFile() {
         return new File(_launchpad.getSafeDirectory(), getUserKeyName() + ".ppk");
     }
 
     protected boolean hasUserKeyFile() {
         File f = getUserKeyFile();
         return f.exists();
     }
 
     protected boolean hasUserPuttyKeyFile() {
         File f = getUserPuttyKeyFile();
         return f.exists();
     }
 
     protected String getUserKeyMaterial()
     {
         try {
             if(hasUserKeyFile()) {
                 File f = getUserKeyFile();
                 BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                 StringBuffer sb = new StringBuffer();
                 while(br.ready()) {
                     sb.append(br.readLine());
                     sb.append("\n");
                 }
 
                 return sb.toString();
             }
             else {
                 throw new Error("Key file does not exist");
             }
         }
         catch(Exception e) {
             return null;
         }
     }
 
     protected String getUserPuttyKeyMaterial()
     {
         try {
             if(hasUserPuttyKeyFile()) {
                 File f = getUserPuttyKeyFile();
                 BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                 StringBuffer sb = new StringBuffer();
                 while(br.ready()) {
                     sb.append(br.readLine());
                     sb.append("\n");
                 }
 
                 return sb.toString();
             }
             else {
                 throw new Error("Key file does not exist");
             }
         }
         catch(Exception e) {
             return null;
         }
     }
 
     protected URL getTroubleshootingLink() {
         try {
             return new URL( getParameter("troubleshooting-url") );
         }
         catch(MalformedURLException e) {
             return null;
         }
     }
 
     ////
     //// "Internal" Applet implementation and related methods; all require the
     //// caller to have already elevated privilege.
     ////
 
     private void init_()
     {
         Map keyMaterial = new HashMap();
 
         if( getUserKeyName() != null && hasUserKeyFile() ) {
             keyMaterial.put( new Integer(Launcher.OPENSSH_KEY_FORMAT), getUserKeyMaterial() );
         }
         else if( getServerKeyMaterial() != null ) {
             keyMaterial.put( new Integer(Launcher.OPENSSH_KEY_FORMAT), getServerKeyMaterial() );
         }
         else if( getAuthMethod() == AUTH_METHOD_PUBLIC_KEY ) {
             boolean why = getUserKeyName() != null && hasUserKeyFile();
             System.out.println("WARNING: OpenSSH key material is unavailable (" + why + ")");
         }
 
         if( getUserKeyName() != null && hasUserPuttyKeyFile() ) {
             keyMaterial.put( new Integer(Launcher.PUTTY_KEY_FORMAT), getUserPuttyKeyMaterial() );
         }
         if( getServerPuttyKeyMaterial() != null ) {
             keyMaterial.put( new Integer(Launcher.PUTTY_KEY_FORMAT), getServerPuttyKeyMaterial() );
         }
         else if( getAuthMethod() == AUTH_METHOD_PUBLIC_KEY ) {
             boolean why = getUserKeyName() != null && hasUserPuttyKeyFile();
             System.out.println("WARNING: PuTTY key material is unavailable (" + why + ")");
         }
 
         if(keyMaterial.isEmpty()) {
             _launchpad.reportError("Unable to find a suitable private key on your local disk or in the applet parameters.", null);
         }
 
         //Initialize the launchpad business logic
         _launchpad.setUsername(getUsername());
         _launchpad.setServer(getServer());
         _launchpad.setServerUUID(getServerUUID());
         _launchpad.setKeyMaterial(keyMaterial);
         _launchpad.init();
         if(getPassword() != null && getPassword().length() > 0) {
             _launchpad.setPassword(getPassword());
         }
 
         //Fix up the "use native client" button's text for friendlier UI
         if(_launchpad.isNativeClientAvailable()) {
             _actRunNative.putValue(_actRunNative.NAME, "Use " + _launchpad.getNativeClientName());
         }
         else {
             _lblForcedMindtermReason.setText(_launchpad.getNativeClientStatus());
         }
 
         //Initialize the UI (only if we haven't already done it)
         if(!_initialized) {
             initUI();
         }
 
         _initialized = true;
     }
 
     private void start_()
     {
         try {
             if( isAutorun() ) {
                 autorun_();
             }
             else {
                 choose_();
             }
         }
         catch(IOException e) {
             _launchpad.reportError("Encountered an error while invoking the SSH client.", e);
         }
     }
 
     private boolean autorun_()
             throws IOException
     {
         System.err.println("Attempting autorun...");
 
         boolean didLaunch = false;
 
         if( isAttemptNative() ) {
             try {
                 setDisplayState(USING_NATIVE);
                 _ranNative = didLaunch = _launchpad.runNative();
             }
             catch(IOException e) {
                 didLaunch = false;
                 _launchpad.reportError("Could not invoke your system's SSH client.", e);
             }
         }
 
         if(didLaunch) {
             return true;
         }
 
         try {
             if( isAttemptNative() ) {
                 setDisplayState(FORCED_MINDTERM);
             }
             else {
                 setDisplayState(USING_MINDTERM);
             }
 
             _ranMindterm = didLaunch = _launchpad.runMindterm(this, this);
         }
         catch(IOException e) {
             didLaunch = false;
             _launchpad.reportError("Could not invoke the MindTerm SSH applet.\nSorry, we can't connect you right now!", e);
         }
 
         return didLaunch;
     }
 
     private void choose_()
             throws IOException
     {
         if( _launchpad.isNativeClientAvailable() ) {
             setDisplayState(CHOOSING);
         }
         else {
             setDisplayState(FORCED_MINDTERM);
             _ranMindterm = _launchpad.runMindterm(this, this);
         }
     }
 
     ////
     //// UI fields and functions.
     ////
 
     boolean        _initialized = false;
     JPanel         _pnlMain     = null;
 
     JLabel _lblForcedMindtermReason =
         new JLabel( "Could not invoke your system's SSH client.");
 
     
     Action _actTroubleshoot = new AbstractAction("Troubleshoot") {
         public void actionPerformed(ActionEvent evt) {
             URL url = getTroubleshootingLink();
             if(url != null) {
                 LaunchpadApplet.this.getAppletContext().showDocument(url, "_blank");
             }
         }
     };
 
     Action _actRunNative = new AbstractAction("Use Native") {
         public void actionPerformed(ActionEvent evt) {
             try {
                 _ranNative = _launchpad.runNative();
                 setDisplayState(USING_NATIVE);
             }
             catch(IOException e) {
                 _launchpad.reportError("Could not invoke your computer's SSH application.", e);
             }
         }
     };
 
     Action _actRunMindterm = new AbstractAction("Use Mindterm") {
         public void actionPerformed(ActionEvent evt) {
             try {
                 _ranMindterm = _launchpad.runMindterm(LaunchpadApplet.this, LaunchpadApplet.this);
                 setDisplayState(USING_MINDTERM);
             }
             catch(IOException e) {
                 _launchpad.reportError("Could not invoke MindTerm.", e);
             }
         }
     };
 
     private void setDisplayState(String newState) {
         if(_initialized) {
             CardLayout layout = (CardLayout)_pnlMain.getLayout();
             layout.show(_pnlMain, newState);
         }
     }
 
     private void initUI() {
         //A header that is shared between all display states
         Container header           = createHeaderUI();
 
         //One panel for each display state the applet can be in
         Container pnlChoosing       = createChoosingUI(),
                   pnlUsingNative    = createUsingNativeUI(),
                   pnlUsingMindterm  = createUsingMindtermUI(),
                   pnlForcedMindterm = createForcedMindtermUI();
 
         //Add all of the initialized panels to the main (CardLayout) panel
        _pnlMain = new JPanel();
         _pnlMain.setLayout(new CardLayout());
         _pnlMain.add(pnlChoosing, CHOOSING);
         _pnlMain.add(pnlUsingNative, USING_NATIVE);
         _pnlMain.add(pnlUsingMindterm, USING_MINDTERM);
         _pnlMain.add(pnlForcedMindterm, FORCED_MINDTERM);
 
         //Add the main and header panels to ourself
         this.setLayout(new BorderLayout());
         this.add(header, BorderLayout.NORTH);
         this.add(_pnlMain, BorderLayout.CENTER);
     }
 
    private Container createHeaderUI() {
         JPanel pnl = new JPanel();
         pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
         JLabel lbl = new JLabel( "Connecting to" );
         lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
         pnl.add(lbl);
         lbl = new JLabel( getServer() );
         lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
         pnl.add(lbl);
         pnl.add(Box.createRigidArea(new Dimension(1, 16)));
         return pnl;
     }
 
 
     private Container createChoosingUI() {
        JPanel pnl = new JPanel();
         Box pnlCenter = Box.createVerticalBox();
         Box pnlButtons = Box.createHorizontalBox();
         JLabel lbl = new JLabel( "How do you want to connect?" );
         lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
         lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
         pnlCenter.add(lbl);
         pnlButtons.add(new JButton(_actRunNative));
         pnlButtons.add(new JButton(_actRunMindterm));
         pnlCenter.add(pnlButtons);
         pnl.setLayout(new BorderLayout());
         pnl.add(pnlCenter, BorderLayout.CENTER);
 
         return pnl;
     }
 
     private Container createUsingNativeUI() {
        JPanel pnl = new JPanel();
         Box pnlCenter = Box.createVerticalBox();
         Box pnlButtons = Box.createHorizontalBox();
         JLabel lbl = new JLabel( _launchpad.getNativeClientName() + " will launch in a separate window." );
         lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
         pnlCenter.add(lbl);
         lbl = new JLabel( "If you encounter problems, use MindTerm instead." );
         lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
         pnlCenter.add(lbl);
         pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
         JButton btnRunNative = new JButton(_actRunNative);
                 btnRunNative.setText("New Session");
         pnlButtons.add(btnRunNative);
         pnlButtons.add(new JButton(_actRunMindterm));
         pnlCenter.add(pnlButtons);
         pnl.setLayout(new BorderLayout());
         pnl.add(pnlCenter, BorderLayout.CENTER);
         return pnl;
     }
 
     private Container createUsingMindtermUI() {
        JPanel pnl = new JPanel();
         Box pnlCenter = Box.createVerticalBox();
         JLabel lbl = new JLabel( "Using MindTerm pure-Java SSH client.");
         lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
         pnlCenter.add(lbl);
         lbl = new JLabel( "Session will display in a separate window.");
         lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
         pnlCenter.add(lbl);
         pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
         lbl = new JLabel( "This window must stay open while using SSH.");
         lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
         lbl.setForeground(Color.RED);
         lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
         pnlCenter.add(lbl);
         pnl.setLayout(new BorderLayout());
         pnl.add(pnlCenter, BorderLayout.CENTER);
         return pnl;
     }
 
     private Container createForcedMindtermUI() {
        JPanel pnl = new JPanel();
         Box pnlCenter = Box.createVerticalBox();
 
         _lblForcedMindtermReason.setAlignmentX(JComponent.CENTER_ALIGNMENT);
         pnlCenter.add(_lblForcedMindtermReason);
         JLabel lbl = null;
         lbl = new JLabel( "Using MindTerm instead.");
         lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
         pnlCenter.add(lbl);
         lbl = new JLabel( "Session will display in a separate window.");
         lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
         pnlCenter.add(lbl);
         pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
         lbl = new JLabel( "This window must stay open while using SSH.");
         lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
         lbl.setForeground(Color.RED);
         lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
         pnlCenter.add(lbl);
 
         if(getTroubleshootingLink() != null) {
             pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
             Box pnlButtons = Box.createHorizontalBox();
             pnlButtons.add(new JButton(_actTroubleshoot));
             pnlCenter.add(pnlButtons);
         }
 
         pnl.setLayout(new BorderLayout());
         pnl.add(pnlCenter, BorderLayout.CENTER);
         return pnl;
     }
 
     ////
     //// Overridden superclass methods used to keep MindTerm from trashing
     //// our components and layout.
     ////
 
     @Override
     public void setLayout(LayoutManager layout) {
         if(!_initialized) {
             super.setLayout(layout);
         }
     }
 
     @Override
     public Component add(Component comp) {
         if(!_initialized) {
             return super.add(comp);
         }
         else {
             return comp;
         }
     }
 
     @Override
     public void add(Component comp, Object constraints) {
         if(!_initialized) {
             super.add(comp, constraints);
         }
     }
 }
