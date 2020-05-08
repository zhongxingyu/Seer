 package org.wings.comet;
 
 import org.wings.ReloadManager;
 import org.wings.script.ScriptListener;
 import org.wings.plaf.Update;
 import org.wings.resource.UpdateResource;
 import org.wings.session.*;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletOutputStream;
 import java.io.IOException;
 import java.util.Iterator;
 
 // This class isn't used currently - but it may be used in the future? TODO: Jens Hagel
/*
Info about this class from Jens Hagel: (german)      
 
Die Klasse ist dazu da die Updates des Komponentenbaums direkt über
den hangingGetRequest zu schicken (also nicht nur ein Trigger).
Dazu kann es aber zu Schwierigkeiten kommen, weil dann über
2 Verbindungen Updates zum Client kommen können und die
korrekte Reihenfolge somit nicht sichergestellt ist. Du kannst
sie ja mal noch drin lassen (zumindest im air branch).
Wenn man mag kann man das ja später aufgreifen und irgendwas
mit Sequenznummern einbauen damit die konkreten Updates auch
über den HGetRequest geschickt werden können.

 */
 class TomcatUpdatesPushable extends TomcatPushable {
 
     private final ReloadManager reloadManager;
     private final ScriptManager scriptManager;
 
     public TomcatUpdatesPushable(Session session) {
         super(session);
         reloadManager = session.getReloadManager();
         scriptManager = session.getScriptManager();
     }
 
     @Override
     public void push() {
         setValid(false);
         response.setContentType("text/xml; charset=" + SessionManager.getSession().getCharacterEncoding());
         try {
             final ServletOutputStream servletOutputStream = response.getOutputStream();
             out.setServletOutputStream(servletOutputStream);
             UpdateResource.writeHeader(out);
             // update components
             for (Iterator i = reloadManager.getUpdates().iterator(); i.hasNext();) {
                 UpdateResource.writeUpdate(out, (Update) i.next());
             }
             // update scripts
             ScriptListener[] scriptListeners = scriptManager.getScriptListeners();
             for (int i = 0; i < scriptListeners.length; ++i) {
                 if (scriptListeners[i].getScript() != null) {
                     UpdateResource.writeUpdate(out, scriptListeners[i].getScript());
                 }
             }
             scriptManager.clearScriptListeners();
             UpdateResource.writeFooter(out);
             out.flush();
             out.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
 
