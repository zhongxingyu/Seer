 package com.kodokux.magedox.console;
 
 import com.intellij.execution.impl.ConsoleViewUtil;
 import com.intellij.openapi.Disposable;
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.editor.EditorFactory;
 import com.intellij.openapi.util.Disposer;
 import com.intellij.openapi.wm.ToolWindow;
 import com.intellij.openapi.wm.ToolWindowAnchor;
 import com.intellij.openapi.wm.ToolWindowManager;
 import com.intellij.ui.content.Content;
 import com.intellij.ui.content.ContentFactory;
import com.kodokux.magedox.MagedoxIcon;
 import com.kodokux.magedox.MagedoxModel;
 
 import javax.swing.*;
 import java.awt.*;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  * Created with IntelliJ IDEA.
  * User: johna
  * Date: 13/04/27
  * Time: 1:33
  * To change this template use File | Settings | File Templates.
  */
 public class MyConsole extends JPanel implements Disposable {
 
     private final MagedoxModel magedoxModel;
     public final String MAGEDOX_TOOLWINDOW_ID = "Magedox";
     InputStream inputStream;
     OutputStream outputStream;
 
     public MyConsole(MagedoxModel magedoxModel) {
         this.magedoxModel = magedoxModel;
         ToolWindow toolWindow = ToolWindowManager.getInstance(this.magedoxModel.getProject()).getToolWindow(MAGEDOX_TOOLWINDOW_ID);
         if (toolWindow == null) {
             toolWindow = ToolWindowManager.getInstance(this.magedoxModel.getProject())
                     .registerToolWindow(MAGEDOX_TOOLWINDOW_ID, true, ToolWindowAnchor.BOTTOM, this.magedoxModel.getProject(), true);
         }
 
        final Content content = ContentFactory.SERVICE.getInstance().createContent(this, "", false);
        toolWindow.setIcon(MagedoxIcon.MAGEDOX_ICON_16x16);
         toolWindow.getContentManager().addContent(content);
 
     }
 
     public Editor createLogEditor() {
         final Editor editor = ConsoleViewUtil.setupConsoleEditor(this.magedoxModel.getProject(), false, false);
         Disposer.register(this.magedoxModel, new Disposable() {
             @Override
             public void dispose() {
                 EditorFactory.getInstance().releaseEditor(editor);
             }
         });
         return editor;
     }
 
 
     @Override
     public void dispose() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }
