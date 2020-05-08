 package pl.softwaremill.idea.pastieplugin;
 
 import com.intellij.openapi.actionSystem.AnAction;
 import com.intellij.openapi.actionSystem.AnActionEvent;
 import com.intellij.openapi.actionSystem.DataKeys;
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.ide.CopyPasteManager;
 import com.intellij.openapi.ui.MessageType;
 import com.intellij.openapi.ui.popup.Balloon;
 import com.intellij.openapi.ui.popup.JBPopupFactory;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.openapi.wm.StatusBar;
 import com.intellij.openapi.wm.WindowManager;
 import com.intellij.ui.awt.RelativePoint;
 import pl.softwaremill.idea.pastieplugin.history.PastieHistory;
 
 import java.awt.datatransfer.StringSelection;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 /**
  * Class doing the main job. Sending selected content to pastie.org website.
  *
  * @author Tomasz Dziurko
  */
 public class SendToPastieAction extends AnAction {
 
     private static String PASTIE_BASE_URL = "http://pastie.org/private/";
    private static Pattern pattern = Pattern.compile("download\\?key=(.+?)\" ");
     private static LanguageMap LANGUAGE_MAP = new LanguageMap();
 
 
     @Override
     public void actionPerformed(AnActionEvent actionEvent) {
 
         String selection = extractSelectedText(actionEvent);
         int languageDropdownId = getCorrectLanguageDropdownId(actionEvent);
 
         if (selection == null || selection.trim().length() == 0) {
             showBalloonPopup(actionEvent, "There is nothing to share.", MessageType.WARNING);
             return;
         }
 
         try {
             String pastedCodeFragmentUniqueKey = shareWithPastie(selection, languageDropdownId);
 
             String linkToPastie = PASTIE_BASE_URL + pastedCodeFragmentUniqueKey;
             CopyPasteManager.getInstance().setContents(new StringSelection(linkToPastie));
 
             PastieHistory pastieHistory = DataKeys.PROJECT.getData(actionEvent.getDataContext()).getComponent(PastieHistory.class);
             pastieHistory.addItem(selection, linkToPastie);
 
             showBalloonPopup(actionEvent, "Share with Pastie successful. <br/>Link is waiting in your clipboard.<br/>", MessageType.INFO);
         } catch (Exception e) {
 
             showBalloonPopup(actionEvent, "Something went wrong.<br/><br/>Problem description: " + e.getMessage() +
                     "<br/><br/>Please try again and it problem persits, contact with author.", MessageType.ERROR);
             e.printStackTrace();
         }
     }
 
     private void showBalloonPopup(AnActionEvent actionEvent, String htmlText, MessageType messageType) {
         StatusBar statusBar = WindowManager.getInstance().getStatusBar(DataKeys.PROJECT.getData(actionEvent.getDataContext()));
 
         JBPopupFactory.getInstance()
                 .createHtmlTextBalloonBuilder(htmlText, messageType, null)
                 .setFadeoutTime(7500)
                 .createBalloon()
                 .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
     }
 
     private String extractSelectedText(AnActionEvent actionEvent) {
         Editor editor = DataKeys.EDITOR.getData(actionEvent.getDataContext());
 
         return editor.getSelectionModel().getSelectedText();
     }
 
     private String extractFileExtension(AnActionEvent actionEvent) {
         VirtualFile file = DataKeys.VIRTUAL_FILE.getData(actionEvent.getDataContext());
 
         return file == null ? "" : file.getExtension();
     }
 
     private int getCorrectLanguageDropdownId(AnActionEvent actionEvent) {
         String fileExtension = extractFileExtension(actionEvent);
 
         return LANGUAGE_MAP.getLanguageDropdownIdFor(fileExtension);
     }
 
     private String shareWithPastie(String selection, int languageDropdownId) throws Exception {
 
         String response = shareAndGetResponse(selection, languageDropdownId);
 
         return extractKeyFrom(response);
     }
 
 
     private String shareAndGetResponse(String selection, int languageDropdownId) throws IOException {
         URL url = new URL("http://pastie.org/pastes");
         URLConnection conn = url.openConnection();
         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         conn.setDoOutput(true);
         OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
 
         String data = "paste[parser_id]=" + languageDropdownId + "&paste[authorization]=burger&paste[restricted]=1&paste[body]="
                 + URLEncoder.encode(selection, "UTF-8");
         writer.write(data);
         writer.flush();
         writer.close();
 
         StringBuffer answer = loadResponse(conn);
 
         return answer.toString();
     }
 
     private StringBuffer loadResponse(URLConnection conn) throws IOException {
         StringBuffer answer = new StringBuffer();
         BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         String line;
         while ((line = reader.readLine()) != null) {
             answer.append(line);
         }
 
         reader.close();
         return answer;
     }
 
     private String extractKeyFrom(String response) {
         Matcher matcher = pattern.matcher(response);
 
         if (matcher.find()) {
             return matcher.group(1);
         }
 
         throw new RuntimeException("Sorry. Plugin wasn't able to extract url to pasted code fragment.");
     }
 
 }
