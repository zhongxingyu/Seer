 package ru.yole.conflued.client;
 
 import com.intellij.notification.Notification;
 import com.intellij.notification.NotificationType;
 import com.intellij.notification.Notifications;
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.util.ActionCallback;
 import com.intellij.openapi.util.Pair;
 import com.intellij.util.ArrayUtil;
 import com.intellij.util.Consumer;
 import org.apache.xmlrpc.XmlRpcClient;
 import org.jetbrains.annotations.Nullable;
 import ru.yole.conflued.model.ConfPage;
 import ru.yole.conflued.model.ConfServer;
 import ru.yole.conflued.model.ConfSpace;
 
 import java.net.MalformedURLException;
 import java.util.*;
 
 /**
  * @author yole
  */
 public class ConfluenceClient {
    private static final Logger LOG = Logger.getInstance("#ru.yole.conflued.client.ConfluenceClient");

     private static final String CONFLUENCE_PREFIX = "confluence1.";
     private final Map<ConfServer, String> myLoginTokens = new WeakHashMap<ConfServer, String>();
 
     private boolean myOffline;
 
     public static ConfluenceClient getInstance() {
         return ServiceManager.getService(ConfluenceClient.class);
     }
 
     public boolean isOffline() {
         return myOffline;
     }
 
     public void setOffline(boolean offline) {
         myOffline = offline;
     }
 
     public ActionCallback updateSpaces(final ConfServer server) {
         return runWithLoginToken(server, "getSpaces", ArrayUtil.EMPTY_OBJECT_ARRAY,
                 new Consumer<Object>() {
                     public void consume(Object o) {
                         parseSpaces(server, (Vector<Hashtable>) o);
                     }
                 });
     }
 
     private void parseSpaces(ConfServer server, Vector<Hashtable> spaceSummaries) {
         List<ConfSpace> result = new ArrayList<ConfSpace>();
         for (Hashtable spaceSummary : spaceSummaries) {
            String key = (String) spaceSummary.get("key");
            if (key.startsWith("~")) {
                continue;
            }
             ConfSpace space = new ConfSpace();
             space.loaded(server);
            space.setKey(key);
             space.setName((String) spaceSummary.get("name"));
             result.add(space);
         }
         server.spaces = result;
     }
 
     public ActionCallback updatePages(final ConfSpace space) {
         return runWithLoginToken(space.getServer(), "getPages", new Object[] { space.getKey() },
                 new Consumer<Object>() {
                     public void consume(Object o) {
                         parsePages(space, (Vector<Hashtable>) o);
                     }
                 });
     }
 
     private void parsePages(ConfSpace space, Vector<Hashtable> pageSummaries) {
         List<ConfPage> result = new ArrayList<ConfPage>();
         for (Hashtable pageSummary: pageSummaries) {
             ConfPage page = new ConfPage();
             page.setSpace(space);
             page.setId((String) pageSummary.get("id"));
             page.setParentId((String) pageSummary.get("parentId"));
             page.setTitle((String) pageSummary.get("title"));
             result.add(page);
         }
         space.pages = result;
     }
 
     public ActionCallback refreshPage(final ConfPage page, final Consumer<Pair<ConfPage, String>> contentConsumer) {
         return runWithLoginToken(page.getSpace().getServer(), "getPage", new Object[] { page.getId() },
                 new Consumer<Object>() {
                     public void consume(Object o) {
                         parsePage(page, (Hashtable) o, contentConsumer);
                     }
                 });
     }
 
     private void parsePage(ConfPage page, Hashtable pageData, @Nullable Consumer<Pair<ConfPage, String>> contentConsumer) {
         if (page.isNew()) {
             page.setId((String) pageData.get("id"));
         }
         page.setVersion(Integer.parseInt((String) pageData.get("version")));
         if (contentConsumer != null) {
             String content = (String) pageData.get("content");
             contentConsumer.consume(Pair.create(page, content));
         }
     }
 
     public ActionCallback updatePage(final ConfPage page, final String content) {
         Hashtable pageData = new Hashtable();
         if (!page.isNew()) {
             pageData.put("id", page.getId());
             pageData.put("version", String.valueOf(page.getVersion()));
         }
         pageData.put("space", page.getSpace().getKey());
         pageData.put("title", page.getTitle());
         pageData.put("content", content);
         pageData.put("parentId", page.getParentId());
 
         String method;
         Object[] args;
         if (page.isNew()) {
             method = "storePage";
             args = new Object[] { pageData };
         }
         else {
             method = "updatePage";
 
             Hashtable updateOptions = new Hashtable();
             updateOptions.put("versionComment", "Modified in ConfluEd");
             updateOptions.put("minorEdit", false);
 
             args = new Object[] { pageData, updateOptions };
         }
 
 
         return runWithLoginToken(page.getSpace().getServer(), method, args, new Consumer<Object>() {
             public void consume(Object o) {
                 parsePage(page, (Hashtable) o, null);
             }
         });
     }
 
     private ActionCallback runWithLoginToken(final ConfServer server, final String method, final Object[] args, final Consumer<Object> consumer) {
         if (myLoginTokens.containsKey(server)) {
             return doRunWithLoginToken(server, method, args, consumer);
         }
         else {
             final ActionCallback done = new ActionCallback();
             login(server).doWhenDone(new Runnable() {
                 public void run() {
                     doRunWithLoginToken(server, method, args, consumer).notifyWhenDone(done);
                 }
             });
             return done;
         }
     }
 
     private ActionCallback doRunWithLoginToken(ConfServer server, final String method, Object[] args, Consumer<Object> consumer) {
         List<Object> allArgs = new ArrayList<Object>();
         allArgs.add(myLoginTokens.get(server));
         Collections.addAll(allArgs, args);
         return remoteCall(server.getURL() + "/rpc/xmlrpc", CONFLUENCE_PREFIX + method, allArgs.toArray(), consumer,
                 new Consumer<Exception>() {
                     public void consume(Exception e) {
                        LOG.info(e);
                         Notifications.Bus.notify(new Notification("Confluence", "Confluence Operation Failed",
                                 "Failure on " + method + ": " + e.getMessage(),
                                 NotificationType.ERROR));                    }
                 });
     }
 
     private ActionCallback login(final ConfServer server) {
         return remoteCall(server.getURL() + "/rpc/xmlrpc",
                 CONFLUENCE_PREFIX + "login",
                 new Object[] { server.getUserName(), server.getPassword() },
                 new Consumer<Object>() {
                     public void consume(Object o) {
                         myLoginTokens.put(server, (String) o);
                     }
                 },
                 new Consumer<Exception>() {
                     public void consume(Exception e) {
                         Notifications.Bus.notify(new Notification("Confluence", "Confluence Operation Failed",
                                 "Confluence login failed: " + e.getMessage(),
                                 NotificationType.ERROR));
                         server.setLoginFailed(true);
                     }
                 });
     }
 
     private ActionCallback remoteCall(final String url, final String method, final Object[] args,
                                       final Consumer<Object> resultConsumer,
                                       final Consumer<Exception> errorConsumer) {
         final ActionCallback result = new ActionCallback();
         ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
             public void run() {
                 XmlRpcClient client;
                 try {
                     client = new XmlRpcClient(url);
                 } catch (MalformedURLException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     return;
                 }
                 Vector params = new Vector();
                 for (Object arg : args) {
                     params.add(arg);
                 }
                 try {
                     Object result = client.execute(method, params);
                     resultConsumer.consume(result);
 
                 } catch (Exception e) {
                     errorConsumer.consume(e);
                 }
                 result.setDone();
             }
         });
         return result;
     }
 }
