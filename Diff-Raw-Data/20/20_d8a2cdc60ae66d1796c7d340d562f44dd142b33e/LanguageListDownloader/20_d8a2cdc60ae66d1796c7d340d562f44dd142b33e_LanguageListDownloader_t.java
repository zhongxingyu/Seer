 package br.org.indt.ndg.mobile.download;
 
 import br.org.indt.ndg.lwuit.ui.GeneralAlert;
 import br.org.indt.ndg.lwuit.ui.WaitingScreen;
 import br.org.indt.ndg.mobile.AppMIDlet;
 import br.org.indt.ndg.mobile.Resources;
 import br.org.indt.ndg.mobile.structures.Language;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Vector;
 import javax.microedition.io.Connector;
 import javax.microedition.io.HttpConnection;
 
 public class LanguageListDownloader implements Runnable {
 
     private String urlAck;
     private static int MAX_LIST_SIZE = 10000;
     private Vector/*<Language>*/ languages = new Vector();
     private LangListDownloaderListener listener = null;
 
     public Vector getLanguages() {
         return languages;
     }
 
     public LanguageListDownloader(LangListDownloaderListener listener) {
         this.listener = listener;
         updateRequestUrls();
     }
 
     private void updateRequestUrls() {
         String urlBase = AppMIDlet.getInstance().getSettings().getStructure().getLanguageListURL();
         urlAck = urlBase + "?do=ack&imei=" + AppMIDlet.getInstance().getIMEI();
     }
 
     public void getLanguageList()
     {
         WaitingScreen.show(Resources.CONNECTING);
         Thread thisThread = new Thread(this);
         thisThread.setPriority( Thread.MIN_PRIORITY );
         thisThread.start();
     }
 
     public void run() {
         try { Thread.sleep(200); } catch(Exception e){}
         boolean bVal = downloadList();
         WaitingScreen.dispose();
         if(bVal){
             listener.langListDownloadFinished();
         }else{
             GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
             GeneralAlert.getInstance().show(Resources.WARNING_TITLE, Resources.DOWNLOAD_LANG_LIST_FAILED, GeneralAlert.WARNING);
         }
     }
 
     private boolean downloadList(){
         ByteArrayOutputStream bytestream = null;
         boolean downloaded = false;
         try {
             bytestream = new ByteArrayOutputStream();
             downloaded = DownloadUtils.getViaServlet(urlAck, null, bytestream);
             String languagesString = new String(bytestream.toByteArray(), 0, bytestream.toByteArray().length, "UTF-8");
             readLanguages(languagesString);
 
             AppMIDlet.getInstance().getSettings().getStructure().setLanguages(languages);
             AppMIDlet.getInstance().getSettings().writeSettings();
         } catch (UnsupportedEncodingException ex) {
             downloaded = false;
         } finally{
             try{
                 if(bytestream != null){
                     bytestream.close();
                 }
             }catch(IOException ex){}
         }
         return downloaded;
     }
 
 
     private void readLanguages(String languagesString) {
         //add default language
         languages.addElement(AppMIDlet.getInstance().getSettings().getStructure().getDefaultLanguage());
 
         int index = 0;
         while( index <= languagesString.length())
         {
             int endLine = languagesString.indexOf('\n', index);
             if(endLine <= index)
             {
                 break;
             }
             if(languagesString.charAt(endLine - 1 ) == '\r')
             {
                 endLine --;
             }
             String  language = languagesString.substring(index, endLine);
             int space = language.indexOf(' ');
             languages.addElement(new Language(language.substring(0, space), language.substring(space + 1, language.length())));
             index = endLine + 1;
         }
     }
 
 
 }
 
