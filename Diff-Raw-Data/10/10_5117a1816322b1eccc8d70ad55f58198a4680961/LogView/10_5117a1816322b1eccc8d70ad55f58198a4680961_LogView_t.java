 package no.knubo.accounting.client.views;
 
 import no.knubo.accounting.client.Constants;
 import no.knubo.accounting.client.Elements;
 import no.knubo.accounting.client.I18NAccount;
 import no.knubo.accounting.client.Util;
 import no.knubo.accounting.client.misc.AuthResponder;
 import no.knubo.accounting.client.misc.ServerResponse;
 
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 
 public class LogView extends Composite {
     private static LogView me;
     private final I18NAccount messages;
     private final Constants constants;
     private FlexTable table;
 
     public static LogView show(I18NAccount messages, Constants constants, Elements elements) {
         if (me == null) {
             me = new LogView(messages, constants, elements);
         }
         return me;
     }
 
     public LogView(I18NAccount messages, Constants constants, Elements elements) {
         this.messages = messages;
         this.constants = constants;
 
         DockPanel dp = new DockPanel();
 
         table = new FlexTable();
         table.setStyleName("tableborder");
         table.setHTML(0, 0, elements.log());
         table.getRowFormatter().setStyleName(0, "header");
         table.getFlexCellFormatter().setColSpan(0, 0, 6);
 
         table.setText(1, 0, elements.id());
         table.setText(1, 1, elements.occured());
         table.setText(1, 2, elements.category());
         table.setText(1, 3, elements.action());
         table.setText(1, 4, elements.user());
         table.setText(1, 5, elements.message());
         table.getRowFormatter().setStyleName(1, "header");

         dp.add(table, DockPanel.NORTH);

         initWidget(dp);
     }
 
     public void init() {
         while (table.getRowCount() > 2) {
             table.removeRow(2);
         }
 
         ServerResponse resphandler = new ServerResponse() {
             public void serverResponse(JSONValue jsonValue) {
                 JSONArray array = jsonValue.isArray();
 
                 for (int i = 0; i < array.size(); i++) {
                     JSONValue value = array.get(i);
 
                     JSONObject data = value.isObject();
 
                     table.setText(i + 2, 0, Util.str(data.get("id")));
                     table.setText(i + 2, 1, Util.str(data.get("occured")));
                     table.setText(i + 2, 2, Util.str(data.get("category")));
                     table.setText(i + 2, 3, Util.str(data.get("action")));
                     table.setText(i + 2, 4, Util.str(data.get("username")));
                     table.setText(i + 2, 5, Util.str(data.get("message")));
 
                     String style = (((i) % 6) < 3) ? "line2 logline" : "line1 logline";
                     table.getRowFormatter().addStyleName(i + 2, style);
 
                    table.getCellFormatter().setStyleName(1 + 2, 1, "nowrap");
 
                 }
             }
 
         };
 
         AuthResponder.get(constants, messages, resphandler, "/logging.php?action=list");
 
     }
 }
