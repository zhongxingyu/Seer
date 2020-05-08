 package org.gridsphere.portlets.core.news;
 
 import org.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridsphere.provider.event.jsr.ActionFormEvent;
 import org.gridsphere.provider.event.jsr.FormEvent;
 import org.gridsphere.provider.event.jsr.RenderFormEvent;
 import org.gridsphere.provider.portlet.jsr.ActionPortlet;
 import org.gridsphere.provider.portletui.beans.ListBoxBean;
 import org.gridsphere.provider.portletui.beans.ListBoxItemBean;
 import org.gridsphere.services.core.customization.SettingsService;
 import org.gridsphere.services.core.jcr.ContentDocument;
 import org.gridsphere.services.core.jcr.ContentException;
 import org.gridsphere.services.core.jcr.JCRService;
 
 import javax.portlet.PortletConfig;
 import javax.portlet.PortletException;
 import javax.portlet.PortletMode;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Properties;
 
 /*
 * @author <a href="mailto:wehrens@gridsphere.org">Oliver Wehrens</a>
 * @version $Id$
 */
 public class NewsPortlet extends ActionPortlet {
 
     private JCRService jcrService = null;
     private String document = "MessageOfTheDay";
     private String storeFileName = "motd.properties";
     private Properties props = new Properties();
     //private String
 
     public void init(PortletConfig config) throws PortletException {
         super.init(config);
         DEFAULT_VIEW_PAGE = "doView";
         DEFAULT_EDIT_PAGE = "doConfigure";
         SettingsService settingsService = (SettingsService) PortletServiceFactory.createPortletService(SettingsService.class, true);
         storeFileName = settingsService.getRealSettingsPath("portal") + File.separator + storeFileName;
 
         File file = new File(storeFileName);
         props = new Properties();
         try {
             props.load(new FileInputStream(file));
             document = props.getProperty("message");
         } catch (IOException e) {
             log.error("Could not load properties from " + storeFileName);
         }
         jcrService = (JCRService) createPortletService(JCRService.class);
     }
 
 
     public void doView(RenderFormEvent event) throws PortletException {
 	String lang = null;
 	try {
 		lang = event.getRenderRequest().getLocale().getLanguage();
	} catch (Exception e) {
 		// nothing to do (send null as lang)
 	}
         String content = jcrService.getContent(document, lang);
         event.getRenderRequest().setAttribute("document", content);
         setNextState(event.getRenderRequest(), "news/view.jsp");
     }
 
     public void doMyConfigure(FormEvent event) throws PortletException {
         ListBoxBean docList = event.getListBoxBean("document");
         List<ContentDocument> allDocs = null;
         try {
             allDocs = jcrService.listChildContentDocuments("");
             for (int i = 0; i < allDocs.size(); i++) {
                 ListBoxItemBean item = new ListBoxItemBean();
                 item.setValue(allDocs.get(i).getTitle());
                 item.setName(allDocs.get(i).getTitle());
                 if (allDocs.get(i).getTitle().equals(document)) item.setSelected(true);
                 docList.addBean(item);
             }
         } catch (ContentException e) {
             createErrorMessage(event, "Could not get list of documents.");
         }
     }
 
     public void doConfigure(ActionFormEvent event) throws PortletException {
         doMyConfigure(event);
         setNextState(event.getActionRequest(), "news/admin.jsp");
     }
 
     public void doConfigure(RenderFormEvent event) throws PortletException {
         doMyConfigure(event);
         setNextState(event.getRenderRequest(), "news/admin.jsp");
     }
 
     public void doSave(ActionFormEvent event) throws PortletException {
         ListBoxBean cmsDocument = event.getListBoxBean("document");
         document = cmsDocument.getSelectedName();
         props.setProperty("message", document);
         event.getActionResponse().setPortletMode(PortletMode.VIEW);
         try {
             props.store(new FileOutputStream(storeFileName), "Message of the day.");
         } catch (IOException e) {
             log.error("Could not save MOTD prefs to " + storeFileName);
         }
         setNextState(event.getActionRequest(), "doView");
     }
 }
