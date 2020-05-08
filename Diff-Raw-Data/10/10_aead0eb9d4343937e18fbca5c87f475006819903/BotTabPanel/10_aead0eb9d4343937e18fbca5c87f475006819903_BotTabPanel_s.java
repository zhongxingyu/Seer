 package botleecher.client.component;
 
 import botleecher.client.AsyncCallbackAdapter;
 import botleecher.client.BotLeecherGwt;
 import botleecher.client.MediatorService;
 import botleecher.client.MediatorServiceAsync;
 import botleecher.client.event.MessageEvent;
 import botleecher.client.event.PackListEvent;
 import botleecher.client.listener.BotLeecherAdapter;
 import com.chj.gwt.client.soundmanager2.Callback;
 import com.chj.gwt.client.soundmanager2.SoundManager;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.widget.core.client.TabItemConfig;
 import com.sencha.gxt.widget.core.client.TabPanel;
 import com.sencha.gxt.widget.core.client.event.BeforeCloseEvent;
 import de.novanic.eventservice.client.event.RemoteEventService;
 import de.novanic.eventservice.client.event.RemoteEventServiceFactory;
 import de.novanic.eventservice.client.event.domain.DomainFactory;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Maxime Guennec
  * Date: 17/08/13
  * Time: 01:09
  * To change this template use File | Settings | File Templates.
  */
 public class BotTabPanel extends TabPanel {
 
     private HTML logsHtml = new HTML();
     private MediatorServiceAsync mediatorService = MediatorService.App.getInstance();
     private SoundManager soundManager = SoundManager.getInstance();
     private Map<String, BotTab> bots = new HashMap<String, BotTab>();
 
     public BotTabPanel() {
         super();
         soundManager.setUseHtml5Audio(true);
         soundManager.setFlashVersion(9);
         soundManager.setNoSWFCache(true);
         soundManager.setFlashLoadTimeout(1000);
         //SoundManager must be init'd before it can be used.
         DOM.setElementAttribute(logsHtml.getElement(), "id", "gwt-HTML-Logs");
         remoteEventHandle();
         setId("tabs");
         setCloseContextMenu(true);
         setTabScroll(true);
         addBeforeCloseHandler(new BeforeCloseEvent.BeforeCloseHandler<Widget>() {
             @Override
             public void onBeforeClose(BeforeCloseEvent<Widget> widgetBeforeCloseEvent) {
                 final Widget item = widgetBeforeCloseEvent.getItem();
                 if (item instanceof BotTab) {
                     ((BotTab) item).onCloseTab();
                 } else {
                     widgetBeforeCloseEvent.setCancelled(true);
                 }
             }
         });
         setSize("900px", "1000px");
         final ScrollPanel widget = new ScrollPanel(logsHtml);
         widget.setWidth("100%");
         add(widget, "Logs");
         setActiveWidget(widget);
         initData();
     }
 
     private void writeLog(final String log) {
         logsHtml.setHTML(DateTimeFormat.getFormat("[dd/MM/yyyy HH:mm:ss] ").format(new Date()) + log + "<br />" + logsHtml.getHTML());
     }
 
     public void clearBotTabs() {
         if (getWidgetCount() > 1) {
             for (int i = getWidgetCount(); i > 1; i--) {
                 remove(i);
             }
         }
     }
 
     private void remoteEventHandle() {
         final RemoteEventServiceFactory factory = RemoteEventServiceFactory.getInstance();
         final RemoteEventService service = factory.getRemoteEventService();
         service.addListener(DomainFactory.getDomain("bot"), new BotLeecherAdapter() {
             public void onMessageEvent(MessageEvent event) {
                 writeLog(event.getMessage());
                 if (MessageEvent.MessageType.ADDED.equals(event.getType())) {
                     soundManager.beginDelayedInit();
 
                     soundManager.onReady(new Callback() {
                         @Override
                         public void execute() {
                             soundManager.play("soundId", "sounds/notify.wav");
                         }
                     });
                 }
             }
         });
     }
 
     public void addBot(String name, List<PackListEvent.Pack> packs) {
         if (!bots.containsKey(name)) {
             final BotTab tab = new BotTab(name, packs);
             bots.put(name, tab);
             add(tab, new TabItemConfig(name, true));
         }
     }
 
     public void addBot(String name) {
         addBot(name, null);
     }
 
     public void initData() {
         mediatorService.getAllBots(BotLeecherGwt.getSession(), new AsyncCallbackAdapter<List<String>>("Mediator.getAllBots") {
             @Override
             public void onSuccess(List<String> result) {
                 if (result != null && !result.isEmpty()) {
                     for (String bot : result) {
                         addBot(bot);
                     }
                 }
             }
         });
     }
 
 
 }
