 package no.knubo.accounting.client.views.admin;
 
 import java.util.Set;
 
 import no.knubo.accounting.client.Constants;
 import no.knubo.accounting.client.Elements;
 import no.knubo.accounting.client.I18NAccount;
 import no.knubo.accounting.client.Util;
 import no.knubo.accounting.client.misc.AuthResponder;
 import no.knubo.accounting.client.misc.ServerResponse;
 import no.knubo.accounting.client.misc.ServerResponseString;
 import no.knubo.accounting.client.ui.AccountTable;
 import no.knubo.accounting.client.views.files.UploadDelegate;
 import no.knubo.accounting.client.views.files.UploadDelegateCallback;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class AdminBackupView extends Composite implements UploadDelegateCallback, ClickHandler {
 
     private static AdminBackupView me;
     private final Elements elements;
     private final I18NAccount messages;
     private final Constants constants;
     private UploadDelegate uploadDelegate;
     private AccountTable analyzeTable;
     private CheckBox selectAll;
     private Hidden dbSelect;
     private ListBox dbListbox;
     private Button clearButton;
     private Button installButton;
     private Button installIndexButton;
 
     public AdminBackupView(I18NAccount messages, Constants constants, Elements elements) {
         this.messages = messages;
         this.constants = constants;
         this.elements = elements;
 
         dbSelect = new Hidden("dbSelect");
 
         uploadDelegate = new UploadDelegate("admin/admin_backup_admin.php", this, constants, messages, elements,
                 dbSelect);
 
         DockPanel dp = new DockPanel();
 
         analyzeTable = new AccountTable("tableborder");
         dp.add(uploadDelegate.getForm(), DockPanel.NORTH);
 
         dbListbox = new ListBox();
         dp.add(new Label(elements.select_database()), DockPanel.NORTH);
 
         HorizontalPanel hp = new HorizontalPanel();
 
         clearButton = new Button(elements.clear());
         clearButton.addClickHandler(this);
 
         installButton = new Button(elements.backup_install());
         installButton.addClickHandler(this);
         installButton.setEnabled(false);
 
         installIndexButton = new Button(elements.admin_backup_install_index());
         installIndexButton.addClickHandler(this);
 
         hp.add(dbListbox);
         hp.add(clearButton);
         hp.add(installButton);
         hp.add(installIndexButton);
 
         dp.add(hp, DockPanel.NORTH);
 
         dp.add(analyzeTable, DockPanel.NORTH);
 
         analyzeTable.setHeaders(0, "", "Table", "Drop", "Truncate", "Lock", "Insert", "Unlock", "Table exist",
                 "Backup exist", "Operation status");
 
         selectAll = new CheckBox(elements.select_all());
         selectAll.addClickHandler(this);
         analyzeTable.setWidget(0, 0, selectAll);
 
         initWidget(dp);
     }
 
     public static AdminBackupView getInstance(I18NAccount messages, Constants constants, Elements elements) {
         if (me == null) {
             me = new AdminBackupView(messages, constants, elements);
         }
         return me;
     }
 
     public void init() {
         ServerResponse callback = new ServerResponse() {
 
             @Override
             public void serverResponse(JSONValue responseObj) {
                 JSONObject obj = responseObj.isObject();
                 JSONObject allDB = obj.get("db").isObject();
 
                 dbListbox.clear();
 
                 Set<String> keys = allDB.keySet();
 
                 for (String k : keys) {
                     JSONArray db = allDB.get(k).isArray();
                     dbListbox.addItem(Util.str(db.get(3)), k);
                 }
             }
         };
         AuthResponder.get(constants, messages, callback, "admin/admin_backup_admin.php?action=init");
     }
 
     @Override
     public void uploadComplete() {
         /* Not needed */
     }
 
     @Override
     public boolean uploadBody(String body) {
 
         if (body.length() == 0) {
             Util.log("No message upload admin");
             return true;
         }
 
         installButton.setEnabled(true);
 
         JSONValue jsonValue = JSONParser.parseStrict(body);
         JSONArray array = jsonValue.isArray();
 
         String[] fields = { "table", "dropNotFound", "truncateNotFound", "lockFound", "insertFound", "unlockFound",
                 "tableExist", "backupExist" };
 
         for (int i = 0; i < array.size(); i++) {
             JSONObject obj = array.get(i).isObject();
 
             CheckBox box = new CheckBox();
             analyzeTable.setWidget(i + 1, 0, box);
 
             for (int j = 0; j < fields.length; j++) {
 
                 if (j >= 1) {
                     boolean checked = Util.getBoolean(obj.get(fields[j]));
 
                     if (!checked && !fields[j].equals("tableExist") && !fields[j].equals("backupExist")) {
                         box.setEnabled(false);
                     }
                     analyzeTable.setText(i + 1, j + 1, checked ? elements.ok() : elements.x());
                 } else {
                     String name = Util.str(obj.get(fields[j]));
                     Anchor anchor = new Anchor(name);
                     anchor.setName(name);
                     anchor.addClickHandler(this);
                     analyzeTable.setWidget(i + 1, j + 1, anchor);
                 }
             }
             analyzeTable.setText(i + 1, 9, "");
             analyzeTable.getCellFormatter().addStyleName(i, 9, "desc");
 
         }
         return true;
     }
 
     @Override
     public void preUpload() {
         clear();
 
         dbSelect.setValue(Util.getSelected(dbListbox));
         dbListbox.setEnabled(false);
     }
 
     private void clear() {
         dbListbox.setEnabled(true);
         while (analyzeTable.getRowCount() > 1) {
             analyzeTable.removeRow(1);
         }
         selectAll.setValue(false);
     }
 
     @Override
     public void onClick(ClickEvent event) {
         if (event.getSource() instanceof Anchor) {
             showSqlContentPopup((Anchor) event.getSource());
         } else if (event.getSource() == selectAll) {
             toggleSelectAll();
         } else if (event.getSource() == clearButton) {
             clear();
         } else if (event.getSource() == installButton) {
             installBackup();
         } else if (event.getSource() == installIndexButton) {
             installIndexButton();
         }
     }
 
     int currentRow;
     private String prefix;
 
     private void installBackup() {
         String selectedDB = Util.getSelectedText(dbListbox);
         prefix = calculatePrefix();
         if (!Window.confirm(messages.backup_admin_config(selectedDB, prefix))) {
             return;
         }
         dbListbox.setEnabled(false);
         for (int row = 1; row < analyzeTable.getRowCount(); row++) {
             CheckBox box = getCheckbox(row);
             box.setEnabled(false);
             analyzeTable.setText(row, 9, "");
         }
         currentRow = 0;
         dropBackupTable();
     }
 
     private void installIndexButton() {
         prefix = calculatePrefix();
         ServerResponse callback = new ServerResponseString() {
 
             @Override
             public void serverResponse(JSONValue responseObj) {
                 /* Unused */
             }
 
             @Override
             public void serverResponse(String response) {
                 DialogBox db = new DialogBox();
                 db.setAutoHideEnabled(true);
                 db.setText(response);
                 db.center();
             }
         };
         AuthResponder.get(constants, messages, callback,
                 "admin/admin_backup_admin.php?action=install_indexes&dbSelect=" + Util.getSelected(dbListbox)
                         + "&dbprefix=" + prefix);
 
     }
 
     private String calculatePrefix() {
         String table = analyzeTable.getText(1, 1);
         return table.substring(0, table.indexOf('_'));
     }
 
     private CheckBox getCheckbox(int row) {
         return (CheckBox) analyzeTable.getWidget(row, 0);
     }
 
     private void dropBackupTable() {
         currentRow++;
 
         if (currentRow == analyzeTable.getRowCount()) {
             installBackupTables();
             return;
         }
         CheckBox box = getCheckbox(currentRow);
 
         if (box.getValue()) {
             String table = analyzeTable.getText(currentRow, 1);
 
             ServerResponse callback = new ServerResponse() {
 
                 @Override
                 public void serverResponse(JSONValue responseObj) {
                     analyzeTable.setText(currentRow, 9, "Backup dropped");
 
                     dropBackupTable();
                 }
             };
             String url = "admin/admin_backup_admin.php?action=drop_table&dbSelect=" + Util.getSelected(dbListbox)
                     + "&table=" + table;
             AuthResponder.get(constants, messages, callback, url);
         } else {
             dropBackupTable();
         }
 
     }
 
     private void installBackupTables() {
         ServerResponse callback = new ServerResponse() {
 
             @Override
             public void serverResponse(JSONValue responseObj) {
                 for (int row = 1; row < analyzeTable.getRowCount(); row++) {
                     CheckBox box = getCheckbox(row);
 
                     if (box.getValue()) {
                         analyzeTable.setText(row, 9, analyzeTable.getText(row, 9) + ", Backup table ready");
                    }   
                 }
                 currentRow = 0;
                 copyToBackupTables();
             }
         };
         AuthResponder.get(constants, messages, callback,
                 "admin/admin_backup_admin.php?action=install_backup_tables&dbSelect=" + Util.getSelected(dbListbox)
                         + "&dbprefix=" + prefix);
     }
 
     protected void copyToBackupTables() {
         currentRow++;
 
         if (currentRow == analyzeTable.getRowCount()) {
             currentRow = 0;
             installFromBackup();
             return;
         }
         CheckBox box = getCheckbox(currentRow);
 
         if (box.getValue()) {
             String table = analyzeTable.getText(currentRow, 1);
 
             ServerResponse callback = new ServerResponse() {
 
                 @Override
                 public void serverResponse(JSONValue responseObj) {
                     String count = Util.str(responseObj.isObject().get("count"));
                     analyzeTable
                             .setText(currentRow, 9, analyzeTable.getText(currentRow, 9) + ", Backup rows: " + count);
 
                     copyToBackupTables();
                 }
             };
             String url = "admin/admin_backup_admin.php?action=backup_table&dbSelect=" + Util.getSelected(dbListbox)
                     + "&table=" + table + "&dbprefix=" + prefix;
             AuthResponder.get(constants, messages, callback, url);
         } else {
             copyToBackupTables();
         }
 
     }
 
     private void installFromBackup() {
         currentRow++;
 
         if (currentRow == analyzeTable.getRowCount()) {
             return;
         }
         CheckBox box = getCheckbox(currentRow);
 
         if (box.getValue()) {
             String table = analyzeTable.getText(currentRow, 1);
 
             ServerResponse callback = new ServerResponse() {
 
                 @Override
                 public void serverResponse(JSONValue responseObj) {
                     String count = Util.str(responseObj.isObject().get("count"));
                     analyzeTable.setText(currentRow, 9, analyzeTable.getText(currentRow, 9) + ", Backup installed: "
                             + count);
 
                     installFromBackup();
                 }
             };
             String url = "admin/admin_backup_admin.php?action=install_from_backup&dbSelect="
                     + Util.getSelected(dbListbox) + "&table=" + table;
             AuthResponder.get(constants, messages, callback, url);
         } else {
             installFromBackup();
         }
 
     }
 
     private void toggleSelectAll() {
 
         for (int row = 1; row < analyzeTable.getRowCount(); row++) {
             Widget box = getCheckbox(row);
 
             if (box instanceof CheckBox) {
                 CheckBox cb = (CheckBox) box;
 
                 if (cb.isEnabled()) {
                     cb.setValue(selectAll.getValue());
                 }
             }
 
         }
     }
 
     private void showSqlContentPopup(Anchor anchor) {
         AuthResponder.get(constants, messages, new ServerResponseString() {
 
             @Override
             public void serverResponse(JSONValue responseObj) {
                 /* Not used */
             }
 
             @Override
             public void serverResponse(String response) {
                 DialogBox db = new DialogBox();
                 db.setAutoHideEnabled(true);
                 HTML html = new HTML("<pre style=\"white-space:pre-wrap\">" + response + "</pre>");
                 db.setWidget(html);
                 db.center();
             }
         }, "admin/admin_backup_admin.php?action=view&viewFile=" + anchor.getName());
     }
 }
