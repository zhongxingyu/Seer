 package com.actions;
 
 import com.config.PluginComponent;
 import com.intellij.openapi.actionSystem.AnAction;
 import com.intellij.openapi.actionSystem.AnActionEvent;
 import com.intellij.openapi.actionSystem.DataKeys;
 import com.intellij.openapi.actionSystem.PlatformDataKeys;
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.openapi.fileEditor.FileEditorManager;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.PsiManager;
 import com.intellij.psi.codeStyle.CodeStyleManager;
 import com.intellij.testFramework.LightVirtualFile;
import com.sdc.abstractLanguage.AbstractClassVisitor;
 import com.sdc.java.JavaClassVisitor;
 import com.sdc.javascript.JSClassVisitor;
 import org.objectweb.asm.ClassReader;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Action for triggering decompilation.
  * Dmitriy Zabranskiy, 2013
  */
 
 public class Decompile extends AnAction {
     private final static int LEN = 4;
     private PluginComponent pluginComponent = ApplicationManager.getApplication().getComponent(PluginComponent.class);
     private static final Map<String, String> myMap;
 
     // Extensions of the languages
     static {
         Map<String, String> aMap = new HashMap<String, String>();
         aMap.put("Java", ".java");
         aMap.put("JavaScript", ".js");
         myMap = Collections.unmodifiableMap(aMap);
     }
 
     public void actionPerformed(AnActionEvent e) {
         VirtualFile virtualFile = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
         if (virtualFile != null && !virtualFile.isDirectory()) {
             try {
                 // Detection class file with any file extension
                 InputStream is = virtualFile.getInputStream();
                 byte[] bytes = new byte[LEN];
                 is.mark(LEN);
                 is.read(bytes);
                 final int magic = ByteBuffer.wrap(bytes).getInt();
                 if (magic == 0xCAFEBABE) {
                     is.reset();
                     LightVirtualFile decompiledFile = new LightVirtualFile(virtualFile.getNameWithoutExtension() + myMap.get(pluginComponent.getChosenLanguage()), decompile(is));
                     final Project currentProject = e.getData(PlatformDataKeys.PROJECT);
                     assert currentProject != null;
                     if (!pluginComponent.isShowPrettyEnabled()) {
                         final PsiFile psiFile = PsiManager.getInstance(currentProject).findFile(decompiledFile);
                         // Reformat decompiled code by IDEA
                         ApplicationManager.getApplication().runWriteAction(new Runnable() {
                             @Override
                             public void run() {
                                 assert psiFile != null;
                                 CodeStyleManager.getInstance(currentProject).reformat(psiFile);
                             }
                         });
 
                     }
                     FileEditorManager.getInstance(currentProject).openFile(decompiledFile, true);
                 }
                 is.close();
             } catch (IOException e1) {
                 e1.printStackTrace();
             }
         }
     }
 
     public String decompile(final InputStream is) throws IOException {
         String language = pluginComponent.getChosenLanguage();
         ClassReader cr = new ClassReader(is);
 
         AbstractClassVisitor cv;
         if (language.equals("JavaScript")) {
             cv = new JSClassVisitor(pluginComponent.getTextWidth(), pluginComponent.getTabSize());
         } else {
             // Java
             cv = new JavaClassVisitor(pluginComponent.getTextWidth(), pluginComponent.getTabSize());
         }
         cr.accept(cv, 0);
         return cv.getDecompiledCode();
     }
 }
 
