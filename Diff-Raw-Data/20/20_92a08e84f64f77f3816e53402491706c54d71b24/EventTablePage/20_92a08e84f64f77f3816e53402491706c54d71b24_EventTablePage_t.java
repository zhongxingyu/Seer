 package plugins.FreePublisher.ui;
 
 import freenet.pluginmanager.PluginRespirator;
 import freenet.support.HTMLNode;
 import freenet.support.api.HTTPRequest;
 import java.io.ByteArrayOutputStream;
 import java.util.HashMap;
 import java.util.Map;
 import plugins.FreePublisher.Publisher;
 import plugins.FreePublisher.events.EventTable;
 import plugins.FreePublisher.models.EventTableModel;
 
 /**
  *
  * @author zapu
  */
 public class EventTablePage extends Controller
 {
     private EventTableModel eventTableModel;
 
     public EventTablePage(Publisher publisher)
     {
         super(publisher);
 
         eventTableModel = publisher.getModel(EventTableModel.class);
 
         tables = new HashMap<String, ViewTableAction>();
 
         registerAction("", new IndexAction());
         registerAction("open", new OpenTableAction());
     }
 
     @Override
     public String path()
     {
         return "/publisher/table";
     }
 
     private Map<String, ViewTableAction> tables;
     private int tableId = 0;
 
     private void addShowAction(ViewTableAction action)
     {
         tables.put(action.getKey(), action);
         registerAction("show_" + action.getAutoId(), action);
     }
 
     private void removeShowAction(ViewTableAction action)
     {
         tables.remove(action.getKey());
         removeAction("show_" + action.getAutoId());
     }
 
     class ViewTableAction implements WebPageAction, Runnable
     {
         private String key;
         private Thread thread;
         private EventTable table;
         private String lastError;
         private int autoId;
 
         public ViewTableAction(String key)
         {
             this.key = key;
             this.autoId = tableId++;
         }
 
         public String getKey()
         {
             return key;
         }
 
         public int getAutoId()
         {
             return autoId;
         }
 
         public void startFetching()
         {
             if(thread != null)
                 return;
 
             thread = new Thread(this);
             thread.start();
         }
 
         public int handleAction(HTTPRequest request, HTMLNode contentNode, boolean post)
         {
             contentNode.addChild("h2", "Action " + autoId + " key: " + key);
 
             if(request.getParam("req") != null && request.getParam("req").equals("close"))
             {
                 //Handle this in another action?
                 removeShowAction(this);
 
                 contentNode.addChild("p", "Action closed.");
             }
             else
             {
                 if(thread != null)
                 {
                     contentNode.addChild("pre", "working...");
                 }
                 else if(table != null)
                 {
                     contentNode.addChild("pre", "done...");
                     ByteArrayOutputStream stream = new ByteArrayOutputStream();
                     try
                     {
                         table.outputEventTable(stream);
                         contentNode.addChild("pre", stream.toString());
                     }
                     catch(Exception e)
                     {
                         System.err.println(e);
                     }
                 }
                 else
                 {
                     if(lastError != null)
                     {
                         contentNode.addChild("pre", lastError);
                     }
                 }
             }
 
            contentNode.addChild("a",
                    new String[] { "href" },
                    new String[] { path() },
                    "back");
 
             return 0;
         }
 
         public void run()
         {
             try
             {
                 table = eventTableModel.fetchEventTable(key);
             }
             catch(Exception e)
             {
                 lastError = e.toString();
             }
 
             thread = null;
         }
     }
 
     class OpenTableAction implements WebPageAction
     {
         public int handleAction(HTTPRequest request, HTMLNode contentNode, boolean post)
         {
             if(!post)
             {
                 contentNode.addChild("pre", "!post");
                 return STATUS_ERROR;
             }
 
             if(!request.isPartSet("key"))
             {
                 contentNode.addChild("pre", "no key set");
                 return STATUS_ERROR;
             }
 
             String key = request.getPartAsStringFailsafe("key", 256);
 
             ViewTableAction action = tables.get(key);
             if(action == null)
             {
                 action = new ViewTableAction(key);
                 addShowAction(action);
                 action.startFetching();
             }
             
             //"Redirect" request to action
             return action.handleAction(request, contentNode, false);
         }
 
     }
 
     class IndexAction implements WebPageAction
     {
         public int handleAction(HTTPRequest request, HTMLNode contentNode, boolean post)
         {
             HTMLNode form = getPR().addFormChild(contentNode, "", "form name");
             form.addChild("input",
                             new String[] { "class", "type", "name" },
                             new String[] { "config", "text", "key" });
             form.addChild("input",
                             new String[] { "type", "name", "value" },
                             new String[] { "hidden", "action", "open" });
             form.addChild("input",
                             new String[] { "type", "value", "style" },
                             new String[] { "submit", "Show eventTable", "margin-top:10px; margin-bottom:20px;" });
 
             if(!tables.isEmpty())
             {
                 form.addChild("h1", "Opened:");
 
                 HTMLNode list = contentNode.addChild("ul");
 
                 for(ViewTableAction viewAction : tables.values())
                 {
                     HTMLNode entity = list.addChild("li", viewAction.getAutoId() + " ");
                     entity.addChild("a", new String[] { "href" }, new String[] { path() + "?action=show_" + viewAction.getAutoId() }, viewAction.getKey());
                     entity.addChild("a", new String[] { "href" }, new String[] { path() + "?action=show_" + viewAction.getAutoId() + "&req=close" }, "[Close]");
                 }
             }
 
             return STATUS_NOERROR;
         }
     }
 }
