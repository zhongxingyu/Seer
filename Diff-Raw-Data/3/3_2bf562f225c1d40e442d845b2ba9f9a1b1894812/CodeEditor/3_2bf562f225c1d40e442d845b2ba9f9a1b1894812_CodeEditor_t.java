 package cbas;
 
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileFilter;
 
 public class CodeEditor extends JFrame implements WindowListener, ChangeListener {
 
     public static File comp;
     public static File dir = new File("").getAbsoluteFile();
     public static File lib = new File(dir, "Lib");
     public static File bin = new File(dir, "Bin");
     public static File obj = new File(dir, "Obj");
     public static File inc = new File(dir, "Includes");
     public static File scr = new File(dir, "Scripts");
     public static File libcbas = new File(lib, "libcbas.dll");
     public static File property = new File(dir, "cbas.prop");
     public static Properties props = new Properties();
     
 
     static {
         System.load(libcbas.getAbsolutePath());
         try {
             if (!property.exists()) {
                 property.createNewFile();
             }
             FileInputStream fin = new FileInputStream(property);
             props.load(fin);
             fin.close();
             if (!props.containsKey("compiler")) {
                 props.setProperty("compiler", "C:/MinGW/bin/mingw32-g++.exe");
             }
             comp = new File(props.getProperty("compiler"));
             CodeDocument.loadColors(props);
             FileOutputStream fout = new FileOutputStream(property);
             props.store(fout, "CBAS User Settings");
             fout.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     private boolean living = true;
     private JMenuBar menuBar;
     private JSplitPane pane;
     private JTabbedPane tabs;
     private JTextArea outputArea;
     private PrintStream out;
     private JMenuItem compile;
     private JMenuItem run;
     private JMenuItem stop;
     private JMenuItem pause;
     private JMenuItem close;
     /**
      * Thread safe map for keeping track of native threads, used nativly
      * You should *not* remove or add anything to this map from java
      * You can enumerate the running scripts via the keys, the integer is the
      * handle to the thread nativly and is of no use to java.
      */
     private final Map<String, Integer> threads = Collections.synchronizedMap(new HashMap<String, Integer>());
     int currentTab;
     private Vector<TabData> tabdata = new Vector<TabData>();
     private File lastFile;
 
     private native void implNativeInit();
 
     /**
      * Creates a new instance of CodeEditor
      */
     public CodeEditor() {
         super("CBAS - The C++ Based Autoing System by BenLand100");
         System.out.println("Object Init");
         tabs = new JTabbedPane();
         outputArea = new JTextArea();
         outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
         out = new PrintStream(new TextAreaOutputStream(outputArea));
         pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabs, new JScrollPane(outputArea));
         pane.setContinuousLayout(true);
         pane.setResizeWeight(0.75);
         add(pane);
         menuBar = initMenuBar();
         setJMenuBar(menuBar);
         setSize(640, 480);
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         addWindowListener(this);
         implNativeInit();
         newTab();
         tabs.addChangeListener(this);
         setIcon();
         out.println("Welcome to CBAS");
     }
 
     private void addTab(TabData tab) {
         tabs.addTab(tab.name, new JScrollPane(tab.codeArea));
         tabdata.add(tab);
         close.setEnabled(tabdata.size() > 1);
     }
 
     /**
      * Removes a tab, if possible and allowed.
      * @param index Tab to remove
      */
     private void removeTab(int index) {
         if (tabdata.size() > 1) {
             if (!checkModified(index)) {
                 return;
             }
             TabData data = tabdata.get(index);
             if (data.running) {
                 implStop(data.dll.getAbsolutePath());
             }
             if (data.dll != null) {
                 implReleaseLib(data.dll.getAbsolutePath());
             }
             tabs.remove(index);
             tabdata.remove(index);
         }
         close.setEnabled(tabdata.size() > 1);
     }
 
     /**
      * Invoked to update the UI on a tab switch
      * @param index Tab switched to
      */
     private void switchedTab(int index) {
         currentTab = index;
         TabData data = tabdata.get(currentTab);
         setCaption(data.name);
         compile.setEnabled(data.compile);
         run.setEnabled(data.run);
         pause.setEnabled(data.pause);
         stop.setEnabled(data.stop);
     }
 
     private void setIcon() {
         try {
             InputStream in = ClassLoader.getSystemResourceAsStream("cbas/CBAS.png");
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             byte[] buffer = new byte[0xFFFF];
             int length;
             while ((length = in.read(buffer)) != -1) {
                 out.write(buffer, 0, length);
             }
             setIconImage(Toolkit.getDefaultToolkit().createImage(out.toByteArray()));
         } catch (Exception e) {
             e.printStackTrace(out);
         }
     }
 
     public void setCaption(String name) {
         if (name != null && name.length() > 0) {
             setTitle("CBAS - The C++ Based Autoing System by BenLand100 - " + name);
         } else {
             setTitle("CBAS - The C++ Based Autoing System by BenLand100");
         }
     }
 
     /**
      * Pauses the native thread executing on the supplied library (compiled 
      * script).
      * @param path Path to the library
      */
     private native void implPause(String path);
 
     /**
      * Pauses the current tab's script if executing.
      */
     private void pause() {
         TabData data = tabdata.get(currentTab);
         implPause(data.dll.getAbsolutePath());
         data.paused = true;
         data.run = true;
         data.pause = false;
         data.stop = true;
         switchedTab(currentTab);
         out.println("Paused " + data.name);
     }
 
     /**
      * Terminate the native thread executing on the supplied library (compiled 
      * script).
      * @param path Path to the library
      */
     private native void implStop(String path);
 
     /**
      * Stops the current tab's script if  executing.
      */
     private void stop() {
         TabData data = tabdata.get(currentTab);
         implStop(data.dll.getAbsolutePath());
     }
 
     /**
      * Unconditional release of a native library (compiled script).
      * WARNING: calling this on an executing script will result in a crash.
      * @param path Path to the library (compiled script)
      */
     private native void implReleaseLib(String path);
 
     /**
      * Compiles the script in the current tab, saving if nessessary.
      */
     private void compile() {
         if (!saveTab(false, currentTab)) {
             return;
         }
         final TabData data = tabdata.get(currentTab);
         if (!data.compiling) {
             data.compiling = true;
             Thread compile = new Thread("Compile") {
 
                 private void pumpErrStream(Process proc) throws IOException {
                     InputStream in = proc.getErrorStream();
                     StringBuilder resBuf = new StringBuilder();
                     byte[] buffer = new byte[5000];
                     int len;
                     while ((len = in.read(buffer)) != -1) {
                         resBuf.append(new String(buffer, 0, len));
                     }
                     String result = resBuf.toString().replaceAll("\\Q" + dir.getAbsolutePath().replaceAll("\\\\", "\\\\") + "\\E", "");
                     out.print(result);
                 }
                 
                 private String getLibs(Process proc) throws IOException {
                     InputStream in = proc.getErrorStream();
                     StringBuilder resBuf = new StringBuilder();
                     byte[] buffer = new byte[5000];
                     int len;
                     while ((len = in.read(buffer)) != -1) {
                         resBuf.append(new String(buffer, 0, len));
                     }
                     Set<String> set = new TreeSet<String>();
                     set.add("libcbas");
                     Matcher m = Pattern.compile("links ([^\n]+)").matcher(resBuf);
                     while (m.find()) {
                         set.add(m.group(1));
                     }
                     StringBuilder libs = new StringBuilder();
                     for (String str : set)
                         libs.append(" -l").append(str.trim());
                     System.out.println(libs);
                     return libs.toString();
                 }
 
                 public void run() {
                     try {
                         File script_source = new File(lib, "Script.cc");
                         if (!script_source.exists()) {
                             out.println("Error: Lib/Script.cc not found!");
                             return;
                         }
                         File script_object = new File(lib, "Script.o");
                         if (!script_object.exists() || script_source.lastModified() > script_object.lastModified()) {
                             out.println("Recompiling Script.cc");
                             Process build = Runtime.getRuntime().exec(comp.getAbsolutePath() + " -I\"" + inc.getAbsolutePath() + "\" -O3 -s -c \"" + script_source.getAbsolutePath() + "\" -o \"" + script_object.getAbsolutePath() + "\"");
                             int error = build.waitFor();
                             if (error == 0) {
                                 out.println("Successfully Recompiled");
                             } else {
                                 out.println("Recompile Failed! Exit Value: " + error);
                                 pumpErrStream(build);
                                 return;
                             }
                         }
                         if (data.dll != null) {
                             implReleaseLib(data.dll.getAbsolutePath());
                         }
                         data.dll = null;
                         out.println("Compiling " + data.name);
                         File object = new File(obj, data.name + ".o");
                         Process build = Runtime.getRuntime().exec(comp.getAbsolutePath() + " -I\"" + inc.getAbsolutePath() + "\" -O3 -s -c \"" + data.file.getAbsolutePath() + "\" -o \"" + object.getAbsolutePath() + "\"");
                         int build_res = build.waitFor();
                         String libs = "";
                         if (build_res == 0) {
                             out.println("Successfully Compiled");
                             libs = getLibs(build);
                         } else {
                             out.println("Compile Failed! Exit Value: " + build_res);
                             pumpErrStream(build);
                             return;
                         }
                         out.println("Linking " + data.name);
                         File dll = new File(bin, data.name + ".dll");
                         Process link = Runtime.getRuntime().exec(comp.getAbsolutePath() + " -shared -o \"" + dll.getAbsolutePath() + "\" -s \"" + object.getAbsolutePath() + "\" \"" + script_object.getAbsolutePath() + "\" -L\"" + lib.getAbsolutePath() + "\"" + libs);
                        System.out.println(link);
                        int link_res =  1;//link.waitFor();
                         if (link_res == 0) {
                             out.println("Successfully Linked");
                         } else {
                             out.println("Link Failed! Exit Value: " + link_res);
                             pumpErrStream(link);
                             return;
                         }
                         data.dll = dll;
                     } catch (Exception e) {
                         e.printStackTrace();
                     } finally {
                         data.compiling = false;
                     }
                 }
             };
             compile.start();
         } else {
             out.println(data.name + " is already compiling!");
         }
     }
 
     /**
      * Starts a thread to execute the compiled script for the current tab, 
      * compiling if nessessary.
      * @param path Path to the compiled script
      * @return 0 on success, else fail
      */
     private native int implRun(String path);
 
     /**
      * Runs the visible tab, compiling if nessessary.
      */
     private void run() {
         final TabData data = tabdata.get(currentTab);
         if (data.paused) {
             implRun(data.dll.getAbsolutePath());
             data.paused = false;
             data.pause = true;
             data.run = false;
             data.stop = true;
             switchedTab(currentTab);
             out.println("Resumed " + data.name);
             return;
         }
         if (!data.running) {
             data.running = true;
             Thread run = new Thread("Run") {
 
                 public void run() {
                     try {
                         int hash = data.codeArea.getText().hashCode();
                         if (hash != data.lastHash || data.dll == null) {
                             compile();
                             while (data.compiling) {
                                 Thread.sleep(500);
                             }
                         }
                         if (data.dll == null) {
                             out.println("Cannot execute " + data.name + ", dll not found!");
                             data.running = false;
                             return;
                         }
                         out.println("Executing " + data.name);
                         int res = implRun(data.dll.getAbsolutePath());
                         switch (res) {
                             case 0:
                                 data.compile = false;
                                 data.run = false;
                                 data.pause = true;
                                 data.stop = true;
                                 switchedTab(currentTab);
                                 break;
                             case 1:
                                 data.running = false;
                                 out.println("Execute failed, dll invalid");
                         }
                     } catch (Exception e) {
                         e.printStackTrace();
                         data.running = false;
                     }
                 }
             };
             run.start();
         } else {
             out.println(data.name + " is already running!");
         }
     }
 
     /**
      * Called by native code when a thread started with implRun terminates
      * @param path Path supplied to implRun
      * @param exitcode Exit code of the thread
      */
     private void finished(String path, int exitcode) {
         String name = new File(path).getName();
         name = name.substring(0, name.lastIndexOf("."));
         TabData data = null;
         for (int i = 0; i < tabdata.size(); i++) {
             if (name.equals(tabdata.get(i).name)) {
                 data = tabdata.get(i);
                 break;
             }
         }
         if (exitcode == 0) {
             out.println("Successfully Executed " + name);
         } else if (exitcode == 1337) {
             out.println("Stopped " + name);
         } else {
             out.println(name + " Terminated. Exit Code: " + exitcode);
         }
         data.running = false;
         data.paused = false;
         data.compile = true;
         data.run = true;
         data.pause = false;
         data.stop = false;
         switchedTab(currentTab);
     }
 
     private boolean checkModified(int tab) {
         TabData data = tabdata.get(tab);
         int hash = data.codeArea.getText().hashCode();
         if (hash != data.lastHash) {
             int res = JOptionPane.showConfirmDialog(this, "Do you want to save changes?", "Code Modified", JOptionPane.YES_NO_CANCEL_OPTION);
             switch (res) {
                 case JOptionPane.YES_OPTION:
                     return saveTab(false, tab);
                 case JOptionPane.NO_OPTION:
                     return true;
                 case JOptionPane.CANCEL_OPTION:
                     return false;
             }
         }
         return true;
     }
 
     private void newTab() {
         TabData tab = new TabData();
         tab.codeArea = new CodeArea();
         tab.codeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
         tab.codeArea.setText("int scriptmain() {\n    return 0;\n}");
         tab.name = "Untitled";
         tab.compile = true;
         tab.pause = false;
         tab.run = true;
         tab.stop = false;
         tab.file = null;
         tab.lastHash = tab.codeArea.getText().hashCode();
         addTab(tab);
         tabs.setSelectedIndex(tabs.getTabCount() - 1);
     }
 
     private void openTab() {
         JFileChooser fc = new JFileChooser();
         if (lastFile != null) {
             fc.setCurrentDirectory(lastFile.getParentFile());
         } else {
             fc.setCurrentDirectory(new File("./Scripts"));
         }
         fc.setFileFilter(new FileFilter() {
 
             public String getDescription() {
                 return "CBAS Scripts";
             }
 
             public boolean accept(File f) {
                 if (f.isDirectory()) {
                     return true;
                 }
                 if (f.getName().contains(".cc")) {
                     return true;
                 }
                 return false;
             }
         });
         if (fc.showOpenDialog(this) == fc.APPROVE_OPTION) {
             try {
                 lastFile = fc.getSelectedFile();
                 FileInputStream in = new FileInputStream(lastFile);
                 ByteArrayOutputStream out = new ByteArrayOutputStream();
                 byte[] buffer = new byte[0xFFFF];
                 int length;
                 while ((length = in.read(buffer)) != -1) {
                     out.write(buffer, 0, length);
                 }
                 TabData tab = new TabData();
                 tab.codeArea = new CodeArea();
                 tab.codeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
                 tab.codeArea.setText(new String(out.toByteArray()));
                 int period = lastFile.getName().lastIndexOf('.');
                 tab.name = period != -1 ? lastFile.getName().substring(0, period) : lastFile.getName();
                 tab.pause = false;
                 tab.run = true;
                 tab.stop = false;
                 tab.compile = true;
                 tab.file = lastFile;
                 tab.lastHash = tab.codeArea.getText().hashCode();
                 tab.dll = null;
                 addTab(tab);
                 tabs.setSelectedIndex(tabs.getTabCount() - 1);
                 out.close();
                 in.close();
             } catch (Exception e) {
                 e.printStackTrace(out);
             }
         }
     }
 
     private boolean saveTab(Boolean as, int tab) {
         TabData data = tabdata.get(tab);
         if (as) {
             JFileChooser fc = new JFileChooser();
             if (data.file != null) {
                 fc.setCurrentDirectory(data.file.getParentFile());
                 fc.setSelectedFile(data.file);
             } else if (lastFile != null) {
                 fc.setCurrentDirectory(lastFile.getParentFile());
                 fc.setSelectedFile(lastFile);
             } else {
                 fc.setCurrentDirectory(scr);
             }
             fc.setFileFilter(new FileFilter() {
 
                 public String getDescription() {
                     return "CBAS Scripts";
                 }
 
                 public boolean accept(File f) {
                     if (f.isDirectory()) {
                         return true;
                     }
                     if (f.getName().contains(".cc")) {
                         return true;
                     }
                     return false;
                 }
             });
             if (fc.showSaveDialog(this) == fc.APPROVE_OPTION) {
                 lastFile = fc.getSelectedFile();
                 if (!lastFile.getName().endsWith(".cc")) {
                     lastFile = new File(lastFile.getParentFile(), lastFile.getName() + ".cc");
                 }
                 data.file = lastFile;
                 String filename = lastFile.getName();
                 data.name = filename.substring(0, filename.length() - 3);
                 tabs.setTitleAt(currentTab, data.name);
                 return saveTab(false, tab);
             }
 
             return false;
         } else if (data.file != null) {
             try {
                 FileOutputStream out = new FileOutputStream(data.file);
                 out.write(data.codeArea.getText().getBytes());
                 out.flush();
                 out.close();
                 data.lastHash = data.codeArea.getText().hashCode();
                 return true;
             } catch (Exception e) {
                 e.printStackTrace(out);
             }
 
             return false;
         } else {
             return saveTab(true, tab);
         }
     }
 
     private void close() {
         removeTab(currentTab);
     }
 
     /**
      * Frees everything allocated nativly from this instance.
      */
     private native void implExit();
 
     private void exit() {
         for (int i = 0; i < tabdata.size(); i++) {
             if (!checkModified(i)) {
                 return;
             }
             TabData data = tabdata.get(i);
             if (data.dll != null) {
                 implReleaseLib(data.dll.getAbsolutePath());
                 if (data.running) {
                     implStop(data.dll.getAbsolutePath());
                 }
             }
         }
         living = false;
         implExit();
         dispose();
     }
 
     private void cut() {
         tabdata.get(currentTab).codeArea.cut();
     }
 
     private void copy() {
         tabdata.get(currentTab).codeArea.copy();
     }
 
     private void paste() {
         tabdata.get(currentTab).codeArea.paste();
     }
 
     private void selectAll() {
         tabdata.get(currentTab).codeArea.selectAll();
     }
 
     private void undo() {
         tabdata.get(currentTab).codeArea.undo();
     }
 
     private void redo() {
         tabdata.get(currentTab).codeArea.redo();
     }
 
     private void save(Boolean as) {
         saveTab(as, currentTab);
     }
 
     private JMenuBar initMenuBar() {
         JMenuBar menu = new JMenuBar();
         JMenu file = new JMenu("File");
         file.add(new JMenuItem(new MethodAction("New", this, "newTab")));
         file.add(new JMenuItem(new MethodAction("Open", this, "openTab")));
         file.add(new JMenuItem(new MethodAction("Save", this, "save", false)));
         file.add(new JMenuItem(new MethodAction("Save As", this, "save", true)));
         file.add(new JSeparator());
         close = new JMenuItem(new MethodAction("Close", this, "close"));
         file.add(close);
         file.add(new JMenuItem(new MethodAction("Exit", this, "exit")));
         menu.add(file);
         JMenu edit = new JMenu("Edit");
         edit.add(new JMenuItem(new MethodAction("Undo", this, "undo")));
         edit.add(new JMenuItem(new MethodAction("Redo", this, "redo")));
         edit.add(new JSeparator());
         edit.add(new JMenuItem(new MethodAction("Cut", this, "cut")));
         edit.add(new JMenuItem(new MethodAction("Copy", this, "copy")));
         edit.add(new JMenuItem(new MethodAction("Paste", this, "paste")));
         edit.add(new JMenuItem(new MethodAction("Select All", this, "selectAll")));
         //Find and Goto in search
         menu.add(edit);
         JMenu script = new JMenu("Script");
         compile = new JMenuItem(new MethodAction("Compile", this, "compile"));
         run = new JMenuItem(new MethodAction("Run", this, "run"));
         pause = new JMenuItem(new MethodAction("Pause", this, "pause"));
         pause.setEnabled(false);
         stop = new JMenuItem(new MethodAction("Stop", this, "stop"));
         stop.setEnabled(false);
         script.add(compile);
         script.add(run);
         script.add(pause);
         script.add(stop);
         menu.add(script);
         return menu;
     }
 
     public void windowOpened(WindowEvent e) {
     }
 
     public void windowIconified(WindowEvent e) {
     }
 
     public void windowDeiconified(WindowEvent e) {
     }
 
     public void windowDeactivated(WindowEvent e) {
     }
 
     public void windowClosing(WindowEvent e) {
         exit();
     }
 
     public void windowClosed(WindowEvent e) {
     }
 
     public void windowActivated(WindowEvent e) {
     }
 
     public void stateChanged(ChangeEvent e) {
         if (e.getSource() == tabs) {
             switchedTab(tabs.getSelectedIndex());
         }
     }
 }
