 package cc.rylander.intellij.plugin.polopoly;
 
 import com.intellij.openapi.actionSystem.AnAction;
 import com.intellij.openapi.actionSystem.AnActionEvent;
 import com.intellij.openapi.actionSystem.PlatformDataKeys;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.ui.Messages;
 import com.intellij.openapi.vfs.VirtualFile;
 
 import java.io.IOException;
import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 public class Import extends AnAction {
     @Override
     public void update(AnActionEvent e) {
         super.update(e);
         VirtualFile[] files = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
         e.getPresentation().setEnabled(
                 files != null &&
                 files.length == 1 &&
                 "xml".equalsIgnoreCase(files[0].getExtension()));
     }
 
     public void actionPerformed(AnActionEvent event) {
         Project project = event.getData(PlatformDataKeys.PROJECT);
         if (project == null) {
             return;
         }
         PolopolyPlugin settings = project.getComponent(PolopolyPlugin.class);
         VirtualFile[] files = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(event.getDataContext());
 
         if (files == null || files.length != 1) {
             return;
         }
 
         HttpURLConnection connection = null;
         try {
             byte[] fileContents = files[0].contentsToByteArray();
             String contentType = "text/xml;charset=" + files[0].getCharset();
             URL url = new URL(settings.url +
                               "?username=" + settings.username +
                               "&password=" + settings.password +
                               "&result=true");
             connection = (HttpURLConnection)url.openConnection();
             connection.setDoOutput(true);
             connection.setDoInput(true);
             connection.setRequestMethod("PUT");
             connection.setRequestProperty("Content-Type", contentType);
             connection.connect();
             connection.getOutputStream().write(fileContents);
 
             int result = connection.getResponseCode();
             if (result < 200 || result > 299) {
                 Messages.showMessageDialog(project,
                         "Error when importing file, see server log for details. Http error was: " + result, "Error",
                         Messages.getErrorIcon());
             }
 
         } catch (IOException e) {
            e.printStackTrace();
         } finally {
             if (connection != null) {
                 connection.disconnect();
             }
         }
     }
 }
