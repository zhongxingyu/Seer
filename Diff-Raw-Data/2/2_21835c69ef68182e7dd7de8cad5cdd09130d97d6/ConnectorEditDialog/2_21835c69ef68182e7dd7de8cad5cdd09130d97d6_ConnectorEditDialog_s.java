 package net.argius.stew.ui.window;
 
 import static java.awt.event.KeyEvent.VK_ESCAPE;
 import static javax.swing.JOptionPane.*;
 import static net.argius.stew.ui.window.ConnectorEditDialog.ActionKey.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import java.sql.*;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.List;
 import java.util.zip.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.table.*;
 
 import net.argius.stew.*;
 
 final class ConnectorEditDialog extends JDialog implements AnyActionListener {
 
     enum ActionKey {
         referToOthers, searchFile, searchDriver, submit, tryToConnect, cancel
     }
 
     private static final ResourceManager res = ResourceManager.getInstance(ConnectorEditDialog.class);
     private static final int TEXT_SIZE = 32;
     private static final Insets TEXT_MARGIN = new Insets(1, 3, 1, 0);
     private static final Color COLOR_ESSENTIAL = new Color(0xFFF099);
 
     private final Connector connector;
     private final JTextField tId;
     private final JTextField tName;
     private final JTextField tClasspath;
     private final JTextField tDriver;
     private final JTextField tUrl;
     private final JTextField tUser;
     private final JPasswordField tPassword;
     private final JComboBox cPasswordClass;
     private final JCheckBox cReadOnly;
     private final JCheckBox cUsesAutoRollback;
 
     private List<ChangeListener> listenerList;
 
     private volatile File currentDirectory;
 
     ConnectorEditDialog(JDialog owner, Connector connector) {
         // [Init Instances]
         super(owner);
         this.connector = connector;
         this.listenerList = new ArrayList<ChangeListener>();
         setTitle(res.get("title"));
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         // [Init Components]
         final FlexiblePanel p = new FlexiblePanel();
         add(p);
         // setup and layout text-fields
         String id = connector.getId();
         tId = new JTextField(id, TEXT_SIZE);
         tId.setEditable(false);
         tId.setMargin(TEXT_MARGIN);
         p.addComponent(new JLabel(res.get("connector.id")), false);
         p.addComponent(tId, true);
         tName = createJTextField(connector.getName(), false);
         p.addComponent(new JLabel(res.get("connector.name")), false);
         p.addComponent(tName, false);
         p.addComponent(createJButton(referToOthers), true);
         tClasspath = createJTextField(connector.getClasspath(), false);
         p.addComponent(new JLabel(res.get("connector.classpath")), false);
         p.addComponent(tClasspath, false);
         p.addComponent(createJButton(searchFile), true);
         tDriver = createJTextField(connector.getDriver(), false);
         p.addComponent(new JLabel(res.get("connector.driver")), false);
         p.addComponent(tDriver, false);
         p.addComponent(createJButton(searchDriver), true);
         tUrl = createJTextField(connector.getUrl(), true);
         p.addComponent(new JLabel(res.get("connector.url")), false);
         p.addComponent(tUrl, true);
         tUser = createJTextField(connector.getUser(), true);
         p.addComponent(new JLabel(res.get("connector.user")), false);
         p.addComponent(tUser, true);
         tPassword = new JPasswordField(TEXT_SIZE);
         tPassword.setBackground(COLOR_ESSENTIAL);
         tPassword.setMargin(TEXT_MARGIN);
         Password password = connector.getPassword();
         if (id.length() > 0 && password.hasPassword()) {
             tPassword.setText(password.getRawString());
         }
         p.addComponent(new JLabel(res.get("connector.password")), false);
         p.addComponent(tPassword, true);
         PasswordItem[] items = {new PasswordItem(PlainTextPassword.class),
                                 new PasswordItem(PbePassword.class)};
         cPasswordClass = new JComboBox(items);
         cPasswordClass.setEditable(true);
         int passwordClassSelectedIndex = -1;
         for (int i = 0; i < items.length; i++) {
             if (items[i].getPasswordClass() == password.getClass()) {
                 passwordClassSelectedIndex = i;
                 cPasswordClass.setSelectedIndex(i);
                 break;
             }
         }
         if (passwordClassSelectedIndex < 0) {
             cPasswordClass.setSelectedItem(password.getClass().getName());
         }
         p.addComponent(new JLabel(res.get("connector.encryption")), false);
         p.addComponent(cPasswordClass, true);
         cReadOnly = new JCheckBox(res.get("connector.readonly"), connector.isReadOnly());
         p.addComponent(cReadOnly, true);
         cUsesAutoRollback = new JCheckBox(res.get("connector.autorollback"),
                                           connector.usesAutoRollback());
         p.addComponent(cUsesAutoRollback, true);
         // setup and layout buttons
         JPanel p2 = new JPanel(new GridLayout(1, 3, 16, 0));
         p2.add(createJButton(submit));
         p2.add(createJButton(tryToConnect));
         p2.add(createJButton(cancel));
         p.c.gridwidth = GridBagConstraints.REMAINDER;
         p.c.insets = new Insets(12, 0, 12, 0);
         p.c.anchor = GridBagConstraints.CENTER;
         p.addComponent(p2, false);
         pack();
         // [Events]
         AnyAction aa = new AnyAction(rootPane);
         // ESC
         aa.bind(this, true, cancel, KeyStroke.getKeyStroke(VK_ESCAPE, 0));
     }
 
     @Override
     protected void processWindowEvent(WindowEvent e) {
         super.processWindowEvent(e);
         if (e.getID() == WindowEvent.WINDOW_CLOSING) {
             anyActionPerformed(new AnyActionEvent(this, cancel));
         }
     }
 
     @Override
     public void anyActionPerformed(AnyActionEvent ev) {
         try {
             if (ev.isAnyOf(referToOthers)) {
                 referToOtherConnectors();
             } else if (ev.isAnyOf(searchFile)) {
                 chooseClasspath();
             } else if (ev.isAnyOf(searchDriver)) {
                 chooseDriverClass();
             } else if (ev.isAnyOf(submit)) {
                 requestClose(true);
             } else if (ev.isAnyOf(tryToConnect)) {
                 tryToConnect();
             } else if (ev.isAnyOf(cancel)) {
                 requestClose(false);
             }
         } catch (Exception ex) {
             WindowOutputProcessor.showErrorDialog(this, ex);
         }
     }
 
     private static JTextField createJTextField(String value, boolean isEssential) {
         final JTextField text = new JTextField(TEXT_SIZE);
         text.setMargin(TEXT_MARGIN);
         if (value != null) {
             text.setText(value);
             text.setCaretPosition(0);
         }
         if (isEssential) {
             text.setBackground(COLOR_ESSENTIAL);
         }
         ContextMenu.createForText(text);
         return text;
     }
 
     private JButton createJButton(ActionKey key) {
         final String cmd = key.toString();
         JButton c = new JButton(res.get("button." + cmd));
         c.setActionCommand(cmd);
         c.addActionListener(new AnyAction(this));
         return c;
     }
 
     void addChangeListener(ChangeListener listener) {
         listenerList.add(listener);
     }
 
     private Connector createConnector() {
         final String id = tId.getText();
         Properties props = new Properties();
         props.setProperty("name", tName.getText());
         props.setProperty("driver", tDriver.getText());
         props.setProperty("classpath", tClasspath.getText());
         props.setProperty("url", tUrl.getText());
         props.setProperty("user", tUser.getText());
         props.setProperty("readonly", Boolean.toString(cReadOnly.isSelected()));
         props.setProperty("rollback", Boolean.toString(cUsesAutoRollback.isSelected()));
         props.setProperty("password.class", getPasswordClassName());
         Connector connector = new Connector(id, props);
         Password password = connector.getPassword();
         password.setRawString(String.valueOf(tPassword.getPassword()));
         props.setProperty("password", password.getTransformedString());
         return new Connector(id, props);
     }
 
     private String getPasswordClassName() {
         final Object item = cPasswordClass.getSelectedItem();
         if (item != null) {
             if (item instanceof PasswordItem) {
                 PasswordItem passwordItem = (PasswordItem)item;
                 return passwordItem.getName();
             }
             if (item instanceof CharSequence) {
                 return item.toString();
             }
         }
         return PlainTextPassword.class.getName();
     }
 
     void referToOtherConnectors() {
         ConnectorMap m;
         try {
             m = ConnectorConfiguration.load();
         } catch (IOException ex) {
             WindowOutputProcessor.showErrorDialog(this, ex);
             return;
         }
         String[] names = {"id", "name", "classpath", "driver", "url", "user"};
         Vector<Vector<String>> data = new Vector<Vector<String>>();
         for (Entry<String, Connector> entry : m.entrySet()) {
             Vector<String> a = new Vector<String>(names.length);
             Properties p = entry.getValue().toProperties();
             p.setProperty("id", entry.getKey());
             for (String name : names) {
                 a.add(p.getProperty(name));
             }
             data.add(a);
         }
         JTable t = new JTable();
         t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         t.setDefaultEditor(Object.class, null);
         Vector<String> headers = new Vector<String>();
         for (String name : names) {
             headers.add(res.get("connector." + name));
         }
         t.setModel(new DefaultTableModel(data, headers));
         JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(res.get("dialog.refer-to-others.message")), BorderLayout.PAGE_START);
         p.add(new JScrollPane(t), BorderLayout.CENTER);
         if (showConfirmDialog(this, p, null, OK_CANCEL_OPTION) != OK_OPTION
             || t.getSelectedRow() < 0) {
             return;
         }
         final Properties selected = m.get(t.getValueAt(t.getSelectedRow(), 0)).toProperties();
         Map<String, JTextField> pairs = new HashMap<String, JTextField>();
         pairs.put("name", tName);
         pairs.put("classpath", tClasspath);
         pairs.put("driver", tDriver);
         pairs.put("url", tUrl);
         pairs.put("user", tUser);
         for (Entry<String, JTextField> entry : pairs.entrySet()) {
             JTextField text = entry.getValue();
             if (text.getText().trim().length() == 0) {
                 text.setText(selected.getProperty(entry.getKey()));
             }
         }
     }
 
     void chooseClasspath() {
         if (currentDirectory == null) {
             synchronized (this) {
                 if (currentDirectory == null) {
                     File file = new File(tClasspath.getText().split(File.pathSeparator)[0]);
                     if (file.isDirectory()) {
                         currentDirectory = file;
                     } else {
                         final File parent = file.getParentFile();
                         if (parent != null && parent.isDirectory()) {
                             currentDirectory = parent;
                         }
                     }
                 }
             }
         }
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.setCurrentDirectory(currentDirectory);
         fileChooser.setDialogTitle(res.get("dialog.search.file.header"));
         fileChooser.setApproveButtonText(res.get("dialog.search.file.button"));
         fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
         fileChooser.showDialog(this, null);
         File file = fileChooser.getSelectedFile();
         if (file != null) {
             tClasspath.setText(file.getPath());
             synchronized (this) {
                 currentDirectory = file.getParentFile();
             }
         }
     }
 
     void chooseDriverClass() {
         String text = tClasspath.getText();
         if (text.trim().length() == 0) {
             String message = res.get("confirm.searchsystemclasspath");
             if (showConfirmDialog(this, message, null, OK_CANCEL_OPTION) != OK_OPTION) {
                 return;
             }
             text = Bootstrap.getProperty("java.class.path");
         }
         final Set<String> classes = new LinkedHashSet<String>();
         for (String path : text.split(File.pathSeparator)) {
             File file = new File(path);
             final URL[] urls;
             try {
                 urls = new URL[]{file.toURI().toURL()};
             } catch (MalformedURLException ex) {
                 continue;
             }
             DriverClassFinder finder = new DriverClassFinder(path, classes);
             finder.setClassLoader(DynamicLoader.getClassLoader(urls));
             finder.setFailMode(true);
             finder.find(file);
         }
         if (classes.isEmpty()) {
             showMessageDialog(this, res.get("search.driver.classnotfound"), null, ERROR_MESSAGE);
         } else {
             final String message = res.get("selectDriverClass");
             Object[] a = classes.toArray();
             Object value = showInputDialog(this, message, "", PLAIN_MESSAGE, null, a, a[0]);
             if (value != null) {
                 tDriver.setText((String)value);
                 tDriver.setCaretPosition(0);
             }
         }
     }
 
     void tryToConnect() throws SQLException {
         Connection conn = createConnector().getConnection();
         try {
             DatabaseMetaData dbmeta = conn.getMetaData();
             final String message = res.get("try.connect",
                                            dbmeta.getDatabaseProductName(),
                                            dbmeta.getDatabaseProductVersion());
             WindowOutputProcessor.showInformationMessageDialog(this, message, "");
         } finally {
             conn.close();
         }
     }
 
     void requestClose(boolean withSaving) {
         Connector newConnector = createConnector();
         if (withSaving) {
             ChangeEvent event = new ChangeEvent(newConnector);
             for (ChangeListener listener : listenerList) {
                 listener.stateChanged(event);
             }
         } else if (!newConnector.equals(this.connector)) {
             if (showConfirmDialog(this,
                                   res.get("i.confirm-without-register"),
                                   null,
                                   OK_CANCEL_OPTION) != OK_OPTION) {
                 return;
             }
         }
         dispose();
     }
 
     private static final class PasswordItem {
 
         final Class<? extends Password> passwordClass;
 
         PasswordItem(Class<? extends Password> passwordClass) {
             this.passwordClass = passwordClass;
         }
 
         Class<? extends Password> getPasswordClass() {
             return passwordClass;
         }
 
         String getName() {
             return toString();
         }
 
         @Override
         public String toString() {
             return passwordClass.getName();
         }
 
     }
 
     private static final class DriverClassFinder {
 
         private static final Logger log = Logger.getLogger(DriverClassFinder.class);
 
         private final String rootPath;
         private final Set<String> classes;
 
         private ClassLoader classLoader;
         private boolean failMode;
 
         DriverClassFinder(String rootPath, Set<String> classes) {
             this.rootPath = normalizePath(rootPath);
             this.classes = classes;
             this.classLoader = ClassLoader.getSystemClassLoader();
         }
 
         void setClassLoader(ClassLoader classLoader) {
             this.classLoader = classLoader;
         }
 
         void setFailMode(boolean failMode) {
             this.failMode = failMode;
         }
 
         final void find(File file) {
             try {
                 final File f = file.getCanonicalFile();
                 if (f.isDirectory()) {
                     File[] files = f.listFiles();
                     if (files != null) {
                         for (File child : files) {
                             find(child);
                         }
                     }
                 } else {
                     final String name = f.getName();
                     if (name.matches("(?i).+\\.class")) {
                         try {
                             filter(resolveClass(f.getPath()));
                         } catch (Throwable ex) {
                             if (failMode) {
                                 fail(f, ex);
                             } else {
                                 throw new RuntimeException(ex);
                             }
                         }
                     } else if (name.matches("(?i).+\\.(jar|zip)")) {
                         find(new ZipFile(f));
                     }
                 }
             } catch (IOException ex) {
                 if (failMode) {
                     fail(file, ex);
                 } else {
                     throw new RuntimeException(ex);
                 }
             }
         }
 
         void find(ZipFile zipFile) {
             Enumeration<?> en = zipFile.entries();
             while (en.hasMoreElements()) {
                 ZipEntry entry = (ZipEntry)en.nextElement();
                 String name = entry.getName();
                 if (name.matches("(?i).+\\.class")) {
                     try {
                         filter(resolveClass(name));
                     } catch (Throwable ex) {
                         if (failMode) {
                             fail(name, ex);
                         } else {
                             throw new RuntimeException(ex);
                         }
                     }
                 }
             }
         }
 
         void filter(Class<?> c) {
             if (Driver.class.isAssignableFrom(c)) {
                 classes.add(c.getName());
             }
         }
 
         static void fail(Object object, Throwable cause) {
             log.trace("%s at object %s", cause, object);
         }
 
         Class<?> resolveClass(String path) throws Throwable {
             String s = normalizePath(path);
             if (s.startsWith(rootPath)) {
                 s = s.substring(rootPath.length() + 1);
             }
             s = s.replaceFirst("\\.class$", "").replace('/', '.');
             return Class.forName(s, false, classLoader);
         }
 
         private static String normalizePath(String path) {
             return path.replaceAll("\\\\", "/");
         }
 
     }
 
 }
