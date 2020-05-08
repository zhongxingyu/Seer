 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.netbeans.modules.javafx.preview;
 
 import com.sun.java.swing.plaf.windows.WindowsInternalFrameTitlePane;
 import com.sun.java.swing.plaf.windows.WindowsInternalFrameUI;
 import java.awt.BorderLayout;
 import java.util.Map;
 import java.lang.reflect.Method;
 import java.lang.reflect.Field;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import org.netbeans.api.java.classpath.ClassPath;
 import org.openide.filesystems.FileStateInvalidException;
 import org.openide.util.Exceptions;
 import java.awt.Color;
 import java.awt.Window;
 import java.awt.event.MouseEvent;
 import java.io.Serializable;
 import java.net.URL;
 import java.net.URLClassLoader;
 import javax.swing.JDialog;
 import javax.swing.event.MouseInputAdapter;
 
 public class CodeUtils {
     private static final String[] getComponentNames = {"getComponent", "getJComponent"};                     // NOI18N
     private static final String[] frameNames = {"frame", "window"};                                          // NOI18N
     private static final String[] dialogNames = {"jdialog", "jDialog"};                                      // NOI18N
     private static final String[] getVisualNodeNames = {"getVisualNode", "getSGNode", "impl_getSGNode"};     // NOI18N
     
     private static final String secuencesClassName = "com.sun.javafx.runtime.sequence.Sequences";            // NOI18N
     private static final String secuenceClassName = "com.sun.javafx.runtime.sequence.Sequence";              // NOI18N
     private static final String runMethodName = "javafx$run$";                                               // NOI18N
     private static final String makeMethodName = "make";                                                     // NOI18N
     private static final String getMethodName = "get";                                                       // NOI18N
     private static final String jsgPanelClassName = "com.sun.scenario.scenegraph.JSGPanel";                  // NOI18N
     private static final String sgNodeClassName = "com.sun.scenario.scenegraph.SGNode";                      // NOI18N
     private static final String setSceneMethodName = "setScene";                                             // NOI18N
     private static final String NOCOLOR = "NOCOLOR";                                                         // NOI18N
     private static final String typeInfoClassName = "com.sun.javafx.runtime.TypeInfo";                       // NOI18N
     private static final String stringFieldNameName = "String";                                                         // NOI18N
     
     public static class Context implements Serializable {
         Context(
             Map<String, byte[]> classBytes,
             String className,
             String fileName,
             URL[] sourceCP,
             URL[] executeCP,
             URL[] bootCP) {
             this.classBytes = classBytes;
             this.className = className;
             this.fileName = fileName;
             this.sourceCP = sourceCP;
             this.executeCP = executeCP;
             this.bootCP = bootCP;
         };
         public Map<String, byte[]> classBytes;
         public String className;
         public String fileName;
         public URL[] sourceCP;
         public URL[] executeCP;
         public URL[] bootCP;
     }
 
     public static Object run(Object context) {
         Object obj = null;
         if (context != null) {
 
             Context env = (Context)context;
             if (env.classBytes != null) {
                 env.className = checkCase(env.classBytes, env.className);
                 try {
                     obj = run(env.className, env.sourceCP, env.executeCP, env.bootCP, env.classBytes);
                 } catch (Exception ex){
                     ex.printStackTrace();
                 }
             }
         }
         return obj;
     }
     
     public static ClassLoader getBootClassloader(Object context, ClassLoader parent) {
         Context env = (Context)context;
         return new URLClassLoader(env.bootCP, parent, null);
     }
     
     public static Object run(Object context, ClassLoader bootClassLoader) {
         Object obj = null;
         if (context != null) {
 
             Context env = (Context)context;
             if (env.classBytes != null) {
                 env.className = checkCase(env.classBytes, env.className);
                 try {
                     obj = run(env.className, env.sourceCP, env.executeCP, bootClassLoader, env.classBytes);
                 } catch (Exception ex){
                     ex.printStackTrace();
                 }
             }
         }
         return obj;
     }
     
     private static URL[] toURLs(ClassPath classPath) {
         URL urls[] = new URL[classPath.getRoots().length];
         for (int j = 0; j < classPath.getRoots().length; j++)
             try {
                 urls[j] = classPath.getRoots()[j].getURL();
             } catch (FileStateInvalidException ex) {
                 Exceptions.printStackTrace(ex);
             }
         return urls;
     }
     
     private static URL[] toURLs(ClassPath[] classPaths) {
         int counter = 0;
         for (int i = 0; i < classPaths.length; i++) counter+= classPaths[i].getRoots().length;
         URL urls[] = new URL[counter];
         counter = 0;
         for (int i = 0; i < classPaths.length; i++) 
             for (int j = 0; j < classPaths[i].getRoots().length; j++)
                 try {
                     urls[counter++] = classPaths[i].getRoots()[j].getURL();
                 } catch (FileStateInvalidException ex) {
                     Exceptions.printStackTrace(ex);
                 }
         return urls;
     }
         
     private static Object run(String name, ClassPath sourceClassPath, ClassPath execClassPath, ClassPath bootClassPath, Map<String, byte[]> classBytes) throws Exception {
         MemoryClassLoader memoryClassLoader = new MemoryClassLoader(new ClassPath[] {sourceClassPath, execClassPath, bootClassPath});
         memoryClassLoader.loadMap(classBytes);
         return run(name, memoryClassLoader);
     }
     
     private static Object run(String name, URL[] sourceCP, URL[] executeCP, URL[] bootCP, Map<String, byte[]> classBytes) throws Exception {
         MemoryClassLoader memoryClassLoader = new MemoryClassLoader(sourceCP, executeCP, bootCP);
         MFOURLStreamHanfler.setClassLoader(memoryClassLoader);
         memoryClassLoader.loadMap(classBytes);
         return run(name, memoryClassLoader);
     }
     
     private static Object run(String name, URL[] sourceCP, URL[] executeCP, ClassLoader bootClassLoader, Map<String, byte[]> classBytes) throws Exception {
         MemoryClassLoader memoryClassLoader = new MemoryClassLoader(sourceCP, executeCP, bootClassLoader);
         MFOURLStreamHanfler.setClassLoader(memoryClassLoader);
         memoryClassLoader.loadMap(classBytes);
         return run(name, memoryClassLoader);
     }
     
     private static Object run(String name, ClassLoader classLoader) throws Exception {
         Thread.currentThread().setContextClassLoader(classLoader);
         Class<?> mainClass = classLoader.loadClass(name);
         Class<?> paramClass = classLoader.loadClass(secuenceClassName); 
         Class<?> sequencesClass = classLoader.loadClass(secuencesClassName); 
         Method runMethod = mainClass.getDeclaredMethod(runMethodName, paramClass);
         runMethod.setAccessible(true);
         Class<?> typeinfoClass = classLoader.loadClass(typeInfoClassName); 
         Object commandLineArgs = new String[]{};
         Method makeMethod = sequencesClass.getDeclaredMethod(makeMethodName, typeinfoClass, Object[].class);
         Field stringField = typeinfoClass.getDeclaredField(stringFieldNameName);
         Object args = makeMethod.invoke(null, stringField.get(null), commandLineArgs);
         Object obj = runMethod.invoke(null, args);
         return obj;
     }
     
     private static JComponent parseJComponentObj(Object obj) {
         JComponent comp = null;
         try {
             Method getComponent = null;
             for (String getComponentStr : getComponentNames) {
                 try {
                     getComponent = obj.getClass().getDeclaredMethod(getComponentStr);
                 } catch (Exception ex) {
                 }
                 if (getComponent != null) break;
             }
             if (getComponent != null)
                 comp = (JComponent)getComponent.invoke(obj);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return comp;
     }
     
     private static Window parseTrueJFrameJDialog(Object obj) {
         if ((obj instanceof JFrame) || (obj instanceof JDialog))
             return (Window)obj;
         else
             return null;
     }
     
     private static Window parseJFrameJDialogObj(Object obj) {
         Window frame = null;
         try {
             Field field = null;
             for (String frameStr : frameNames) {
                 try {
                     field = obj.getClass().getDeclaredField(frameStr);
                 } catch (Exception ex) {
                 }
                 if (field != null) break;
             }
             if (field == null)
                 for (String frameStr : dialogNames) {
                     try {
                         field = obj.getClass().getDeclaredField(frameStr);
                     } catch (Exception ex) {
                     }
                     if (field != null) break;
                 }
             if (field != null) {
                 Object frameObj = field.get(obj);
                 if (frameObj != null) {
                     Method getMethod = frameObj.getClass().getDeclaredMethod(getMethodName);
                     frame = (Window)getMethod.invoke(frameObj);
                     return frame;
                 }
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return frame;
     }
     
     static class FixedWindowsInternalFrameUI extends WindowsInternalFrameUI {
 
         public FixedWindowsInternalFrameUI(JInternalFrame w) {
             super(w);
         }
 
         @Override
         protected MouseInputAdapter createBorderListener(JInternalFrame w) {
             return new FixedBorderListener();
         }
 
         protected class FixedBorderListener extends BorderListener {
 
             public FixedBorderListener() {
             }
 
             @Override
             public void mouseDragged(MouseEvent e) {
                 if (e.getSource() instanceof WindowsInternalFrameTitlePane) {
                     JInternalFrame w = (JInternalFrame) ((WindowsInternalFrameTitlePane) e.getSource()).getParent();
                     int y = w.getY();
                     int min_y = getNorthPane().getHeight() - 2;
                     if (y >= -min_y) {
                         super.mouseDragged(e);
                         y = w.getY();
                         if (y < -min_y) {
                             w.setLocation(w.getX(), -min_y);
                         }
                     } else {
                         w.setLocation(w.getX(), -min_y);
                     }
                 }
                 else {
                     super.mouseDragged(e);
                 }
             }
         }
     } 
     
     public static void moveToInner(AutoResizableDesktopPane jdp, Object frame) {
         try {
             JInternalFrame intFrame = new JInternalFrame();
             if (intFrame.getUI().getClass() == WindowsInternalFrameUI.class) {
                 intFrame.setUI(new FixedWindowsInternalFrameUI(intFrame));
             }
             if (frame instanceof Window) {
                 ((Window)frame).setVisible(false);
                 intFrame.setSize(((Window)frame).getSize());
                 if (frame instanceof JFrame) {
                     intFrame.setContentPane(((JFrame)frame).getContentPane());
                     intFrame.setTitle(((JFrame)frame).getTitle());
                     intFrame.setJMenuBar(((JFrame)frame).getJMenuBar());
                     if (((JFrame)frame).getContentPane().getBackground().toString().contentEquals(NOCOLOR)) {
                         intFrame.getContentPane().setBackground(Color.white);
                     } else {
                         intFrame.getContentPane().setBackground(((JFrame)frame).getContentPane().getBackground());
                     }
                         
                 } else {
                     if (frame instanceof JDialog) {
                         intFrame.setContentPane(((JDialog)frame).getContentPane());
                         intFrame.setTitle(((JDialog)frame).getTitle());
                         intFrame.setJMenuBar(((JDialog)frame).getJMenuBar());
                         if (((JDialog)frame).getContentPane().getBackground().toString().contentEquals(NOCOLOR)) {
                             intFrame.getContentPane().setBackground(Color.white);
                         } else {
                            intFrame.getContentPane().setBackground(((JFrame)frame).getContentPane().getBackground());
                         }
                     }
                 }
                 intFrame.setBackground(((Window)frame).getBackground());
                 intFrame.setForeground(((Window)frame).getForeground());
                 ((Window)frame).dispose();
             } else {
                 intFrame.setLayout(new BorderLayout());
                 intFrame.add((JComponent)frame);
                 intFrame.setSize(200, 200);
             }
             intFrame.setResizable(true);
             intFrame.setClosable(true);
             intFrame.setMaximizable(true);
             intFrame.setIconifiable(true);
             intFrame.setVisible(true);
 
             jdp.setBackground(Color.WHITE);
             jdp.add(intFrame);
             jdp.setMinimumSize(intFrame.getSize());
         } catch (Throwable th) {
             th.printStackTrace();
         }
     }
 
     private static JComponent parseSGNodeObj(Object obj) {
         JComponent comp = null;
         try {
             Method getVisualNode = null;
             for (String getVisualNodeStr : getVisualNodeNames) {
                 try {
                     getVisualNode = obj.getClass().getDeclaredMethod(getVisualNodeStr);
                 } catch (Exception ex) {
                 }
                 if (getVisualNode != null) break;
             }
             if (getVisualNode != null) {
                 Object sgNode = getVisualNode.invoke(obj);
                 if (sgNode != null) {
                     Class<?> jsgPanelClass = obj.getClass().getClassLoader().loadClass(jsgPanelClassName);
                     Class<?> sgNodeClass = obj.getClass().getClassLoader().loadClass(sgNodeClassName);
                     Object panel = jsgPanelClass.newInstance();
                     Method setSceneMethod = jsgPanelClass.getDeclaredMethod(setSceneMethodName, sgNodeClass);
                     setSceneMethod.invoke(panel, sgNode);
                     comp = (JComponent)panel;
                 }
             }  
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return comp;
     }
     
     public static Window parseWindow(Object obj) {
         Window window = null;
         if ((window = parseJFrameJDialogObj(obj)) == null)
             window = parseTrueJFrameJDialog(obj);
         return window;
     }
     
     public static JComponent parseComponent(Object obj) {
         JComponent comp = null;
         if ((comp = parseJComponentObj(obj)) == null)
             comp = parseSGNodeObj(obj);
         return comp;
     }
     
     
     private static String checkCase(Map<String, byte[]> classBytes, String name) {
         for (String key : classBytes.keySet()) {
             if (key.toLowerCase().contentEquals(name.toLowerCase())) {
                 return key;
             }
         }
         return name;
     }
 }
