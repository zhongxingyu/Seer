 package no.knubo.accounting.client.views;
 
 import no.knubo.accounting.client.Constants;
 import no.knubo.accounting.client.Elements;
 import no.knubo.accounting.client.I18NAccount;
 import no.knubo.accounting.client.Util;
 import no.knubo.accounting.client.cache.PosttypeCache;
 import no.knubo.accounting.client.misc.AuthResponder;
 import no.knubo.accounting.client.misc.ImageFactory;
 import no.knubo.accounting.client.misc.ServerResponse;
 import no.knubo.accounting.client.ui.AccountTable;
 import no.knubo.accounting.client.ui.ListBoxWithErrorText;
 import no.knubo.accounting.client.ui.NamedButton;
 import no.knubo.accounting.client.ui.TextBoxWithErrorText;
 import no.knubo.accounting.client.validation.MasterValidator;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class FirstTimeRegisterView extends DialogBox implements ClickHandler {
 
     private final I18NAccount messages;
     private final Constants constants;
     private final Elements elements;
     private NamedButton newButton;
     private NamedButton completeButton;
     private ListBoxWithErrorText semesters;
     private TextBoxWithErrorText yearBox;
     private TextBoxWithErrorText monthBox;
     private NamedButton addButton;
     private NamedButton cancelButton;
     private TextBoxWithErrorText amountBox;
     private ListBoxWithErrorText postTypeBox;
     private DialogBox newAccountPopup;
     private AccountTable table;
 
     public FirstTimeRegisterView(I18NAccount messages, Constants constants, Elements elements) {
         this.messages = messages;
         this.constants = constants;
         this.elements = elements;
 
         HorizontalPanel hp = new HorizontalPanel();
         VerticalPanel vp = new VerticalPanel();
         hp.add(vp);
         Frame helpFrame = new Frame("help/" + elements.HELP_ROOT() + "/FIRST_TIME_SETUP.html");
         helpFrame.setWidth("50em");
         helpFrame.setHeight("30em");
         hp.add(helpFrame);
         table = new AccountTable("firsttimetable");
         vp.add(table);
 
         setText(elements.first_time_register());
 
         table.setText(1, 0, elements.first_year(), "headernobox desc");
         table.setText(2, 0, elements.first_month(), "headernobox desc");
         table.setText(3, 0, elements.first_semester(), "headernobox desc");
         table.setHTML(4, 0, "&nbsp;");
         table.setText(5, 0, elements.first_start_balance(), "headernobox desc");
         table.getFlexCellFormatter().setColSpan(5, 0, 2);
         table.setText(6, 0, elements.account(), "headernobox desc");
         table.setText(6, 1, elements.amount(), "headernobox desc");
 
         yearBox = new TextBoxWithErrorText("year", 4);
         table.setWidget(1, 1, yearBox);
         monthBox = new TextBoxWithErrorText("month", 2);
         table.setWidget(2, 1, monthBox);
         semesters = new ListBoxWithErrorText("semesters");
         semesters.addItem(elements.spring(), "0");
         semesters.addItem(elements.fall(), "1");
 
         table.setWidget(3, 1, semesters);
 
         newButton = new NamedButton("add_account_amount", elements.add());
         newButton.addClickHandler(this);
 
         table.setWidget(7, 0, newButton);
         completeButton = new NamedButton("complete_button", elements.complete());
        completeButton.addStyleName("buttonrow");
         completeButton.addClickHandler(this);
         vp.add(completeButton);
 
         vp.add(new HTML(messages.first_time_hint()));
         setWidget(hp);
         initData();
     }
 
     private void initData() {
         ServerResponse initer = new ServerResponse() {
 
             public void serverResponse(JSONValue responseObj) {
                 JSONObject obj = responseObj.isObject();
 
                 String year = Util.str(obj.get("year"));
                 String month = Util.str(obj.get("month"));
 
                 monthBox.setText(month);
                 yearBox.setText(year);
                 setPopupPosition(150, 150);
                 show();
                 monthBox.setFocus(true);
             }
         };
         AuthResponder.get(constants, messages, initer, "defaults/first_time_setup.php");
     }
 
     public static void show(I18NAccount messages, Constants constants, Elements elements) {
         new FirstTimeRegisterView(messages, constants, elements);
     }
 
     public void onClick(ClickEvent event) {
         if (event.getSource() == newButton) {
             showAddPopup();
         } else if (event.getSource() == completeButton) {
             completeFirstTimeSetup();
         } else if (event.getSource() == cancelButton) {
             newAccountPopup.hide();
         } else if (event.getSource() == addButton) {
             addAccount();
         } else if (event.getSource() instanceof Image) {
             Image image = (Image) event.getSource();
             String id = image.getElement().getId();
 
             String[] parts = id.split("_");
             String account = parts[1];
 
             int row = findAccount(account);
 
             table.removeRow(row);
         }
     }
 
     private int findAccount(String account) {
         /* Skip button - is last */
         for (int row = 7; row < table.getRowCount() - 1; row++) {
             if (table.getText(row, 3).equals(account)) {
                 return row;
             }
         }
         return -1;
     }
 
     private void addAccount() {
         MasterValidator mv = new MasterValidator();
         mv.mandatory(messages.required_field(), amountBox);
         mv.money(messages.field_money(), amountBox);
 
         String account = Util.getSelected(postTypeBox);
 
         if (findAccount(account) != -1) {
             mv.fail(postTypeBox, true, messages.account_already_used());
         }
 
         if (!mv.validateStatus()) {
             return;
         }
         int row = table.getRowCount();
 
         table.insertRow(--row);
         table.setText(row, 0, Util.getSelectedText(postTypeBox));
         table.setText(row, 1, Util.money(amountBox.getText()), "right");
         Image deleteImage = ImageFactory.deleteImage("delete_" + account);
         deleteImage.addClickHandler(this);
         table.setWidget(row, 2, deleteImage);
         table.setText(row, 3, account, "hidden");
         newAccountPopup.hide();
 
     }
 
     private void completeFirstTimeSetup() {
         MasterValidator mv = new MasterValidator();
         mv.mandatory(messages.required_field(), yearBox, monthBox);
         if (!mv.validateStatus()) {
             return;
         }
 
         ServerResponse post = new ServerResponse() {
 
             public void serverResponse(JSONValue responseObj) {
                 JSONObject object = responseObj.isObject();
                 if (!Util.getBoolean(object.get("result"))) {
                     Window.alert(messages.save_failed_badly());
                 } else {
                     hide();
                 }
             }
         };
         JSONObject obj = buildSendObject();
 
         StringBuffer parameters = new StringBuffer("action=set");
         Util.addPostParam(parameters, "data", obj.toString());
 
         AuthResponder.post(constants, messages, post, parameters, "defaults/first_time_setup.php");
     }
 
     private JSONObject buildSendObject() {
         JSONObject obj = new JSONObject();
         obj.put("year", new JSONString(yearBox.getText()));
         obj.put("semester", new JSONString(Util.getSelected(semesters)));
         obj.put("month", new JSONString(monthBox.getText()));
         JSONObject ib = new JSONObject();
 
         for (int row = 7; row < table.getRowCount() - 1; row++) {
             ib.put(table.getText(row, 3), new JSONString(table.getText(row, 1)));
         }
 
         obj.put("IB", ib);
         return obj;
     }
 
     private void showAddPopup() {
         newAccountPopup = new DialogBox();
         AccountTable vp = new AccountTable("tableborder");
 
         vp.setText(0, 0, elements.account());
         vp.setText(1, 0, elements.amount());
         postTypeBox = new ListBoxWithErrorText("account");
         PosttypeCache cache = PosttypeCache.getInstance(constants, messages);
         cache.fillAllCapital(postTypeBox.getListbox());
         vp.setWidget(0, 1, postTypeBox);
         amountBox = new TextBoxWithErrorText("amount");
         vp.setWidget(1, 1, amountBox);
 
         addButton = new NamedButton("add", elements.add());
         addButton.addStyleName("buttonrow");
         addButton.addClickHandler(this);
         FlowPanel buttonPanel = new FlowPanel();
         buttonPanel.add(addButton);
 
         vp.setWidget(2, 1, buttonPanel);
         cancelButton = new NamedButton("cancel", elements.cancel());
         cancelButton.addClickHandler(this);
         buttonPanel.add(cancelButton);
 
         newAccountPopup.setWidget(vp);
         newAccountPopup.setText(elements.new_init_account());
 
         newAccountPopup.center();
         postTypeBox.setFocus(true);
     }
 
 }
