 package no.knubo.accounting.client.views.registers;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import no.knubo.accounting.client.Constants;
 import no.knubo.accounting.client.Elements;
 import no.knubo.accounting.client.I18NAccount;
 import no.knubo.accounting.client.Util;
 import no.knubo.accounting.client.cache.CacheCallback;
 import no.knubo.accounting.client.misc.AuthResponder;
 import no.knubo.accounting.client.misc.IdHolder;
 import no.knubo.accounting.client.misc.ImageFactory;
 import no.knubo.accounting.client.misc.ServerResponse;
 import no.knubo.accounting.client.ui.NamedButton;
 import no.knubo.accounting.client.ui.TextBoxWithErrorText;
 import no.knubo.accounting.client.validation.MasterValidator;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Widget;
 
 public class MembershipPriceEditView extends Composite implements ClickHandler, CacheCallback {
 
     private static MembershipPriceEditView me;
     private final Constants constants;
     private final I18NAccount messages;
     private FlexTable table;
     private IdHolder<Integer, Image> lineHolder;
     private MembershipPriceEditFields editFields;
     private final Elements elements;
 
     public MembershipPriceEditView(I18NAccount messages, Constants constants, Elements elements) {
         this.messages = messages;
         this.constants = constants;
         this.elements = elements;
 
         DockPanel dp = new DockPanel();
 
         table = new FlexTable();
         table.setStyleName("tableborder");
 
         table.setText(0, 0, elements.menuitem_membership_prices());
        table.getFlexCellFormatter().setColSpan(0, 0, 11);
         table.setText(2, 0, elements.year());
         table.setText(1, 1, "");
         table.setText(1, 2, elements.spring());
         table.setText(2, 1, elements.year_membership());
         table.setText(2, 2, elements.year_membership_youth());
         table.setText(2, 3, elements.course_membership());
         table.setText(2, 4, elements.train_membership());
         table.setText(2, 5, elements.youth_membership());
         table.setText(1, 6, elements.fall());
         table.setText(1, 7, "");
         table.setText(1, 8, "");
         table.setText(1, 9, "");
        table.setText(1, 10, "");
         table.setText(2, 6, elements.course_membership());
         table.setText(2, 7, elements.train_membership());
         table.setText(2, 8, elements.youth_membership());
         table.setText(2, 9, "");
         table.setText(2, 10, "");
         table.getRowFormatter().setStyleName(0, "header");
         table.getRowFormatter().setStyleName(1, "header");
         table.getRowFormatter().setStyleName(2, "header");
 
         dp.add(table, DockPanel.NORTH);
 
         lineHolder = new IdHolder<Integer, Image>();
         initWidget(dp);
     }
 
     public static MembershipPriceEditView show(I18NAccount messages, Constants constants, Elements elements) {
         if (me == null) {
             me = new MembershipPriceEditView(messages, constants, elements);
         }
         me.setVisible(true);
         return me;
     }
 
     public void onClick(ClickEvent event) {
         Widget sender = (Widget) event.getSource();
 
         if (editFields == null) {
             editFields = new MembershipPriceEditFields();
         }
 
         int left = sender.getAbsoluteLeft() - 250;
 
         int top = sender.getAbsoluteTop() + 10;
         editFields.setPopupPosition(left, top);
 
         int row = lineHolder.findId(sender);
 
         editFields.init(row, table.getText(row, 0), table.getText(row, 1), table.getText(row, 2),
                 table.getText(row, 3), table.getText(row, 4), table.getText(row, 5), table.getText(row, 6), table
                         .getText(row, 7), table.getText(row, 8));
 
         editFields.show();
     }
 
     public void init() {
         lineHolder.init();
 
         while (table.getRowCount() > 3) {
             table.removeRow(3);
         }
 
         ServerResponse callback = new ServerResponse() {
 
             public void serverResponse(JSONValue value) {
                 JSONObject obj = value.isObject();
                 JSONArray semesters = obj.get("semesters").isArray();
                 JSONObject prices = obj.get("price").isObject();
 
                 Map<String, String> priceYear = getPricesMap(prices.get("year").isArray(), "year", "amount");
                 Map<String, String> priceYearYouth = getPricesMap(prices.get("year").isArray(), "year", "amountyouth");
                 Map<String, String> priceCourse = getPricesMap(prices.get("course").isArray(), "semester", "amount");
                 Map<String, String> priceTrain = getPricesMap(prices.get("train").isArray(), "semester", "amount");
                 Map<String, String> priceYouth = getPricesMap(prices.get("youth").isArray(), "semester", "amount");
 
                 String currentYear = null;
 
                 int row = 3;
                 for (int i = 0; i < semesters.size(); i++) {
                     JSONObject semObj = semesters.get(i).isObject();
 
                     String year = Util.str(semObj.get("year"));
                     String semester = Util.str(semObj.get("semester"));
                     int colPlus = "1".equals(Util.str(semObj.get("fall"))) ? 3 : 0;
 
                     if (!year.equals(currentYear)) {
                         row++;
                         String style = (((row + 2) % 6) < 3) ? "line2" : "line1";
                         table.getRowFormatter().setStyleName(row, style);
                         table.setText(row, 0, year);
 
                         if (priceYear.containsKey(year)) {
                             table.setText(row, 1, priceYear.get(year));
                         }
                         if (priceYearYouth.containsKey(year)) {
                             table.setText(row, 2, priceYearYouth.get(year));
                         }
 
                         Image editImage = ImageFactory.editImage("membershipPriceEdit");
                         editImage.addClickHandler(me);
                         table.setWidget(row, 9, editImage);
 
                         currentYear = year;
                         lineHolder.add(row, editImage);
 
                         for (int j = 0; j < 9; j++) {
                             table.getCellFormatter().setStyleName(row, j, "right");
                         }
 
                     }
 
                     if (priceCourse.containsKey(semester)) {
                         table.setText(row, 3 + colPlus, priceCourse.get(semester));
                     }
                     if (priceTrain.containsKey(semester)) {
                         table.setText(row, 4 + colPlus, priceTrain.get(semester));
                     }
                     if (priceYouth.containsKey(semester)) {
                         table.setText(row, 5 + colPlus, priceYouth.get(semester));
                     }
 
                 }
 
             }
 
             private Map<String, String> getPricesMap(JSONArray array, String type, String priceFieldName) {
                 HashMap<String, String> result = new HashMap<String, String>();
 
                 for (int i = 0; i < array.size(); i++) {
                     JSONObject priceObj = array.get(i).isObject();
 
                     result.put(Util.str(priceObj.get(type)), Util.str(priceObj.get(priceFieldName)));
                 }
 
                 return result;
             }
 
         };
 
         AuthResponder.get(constants, messages, callback, "registers/membershipprices.php?action=all");
 
     }
 
     class MembershipPriceEditFields extends DialogBox implements ClickHandler {
 
         private Button saveButton;
         private Button cancelButton;
         private HTML mainErrorLabel;
         private TextBoxWithErrorText yearPrice;
         private TextBoxWithErrorText yearPriceYouth;
         private TextBoxWithErrorText springCoursePrice;
         private TextBoxWithErrorText springTrainPrice;
         private TextBoxWithErrorText springYouthPrice;
         private TextBoxWithErrorText fallCoursePrice;
         private TextBoxWithErrorText fallTrainPrice;
         private TextBoxWithErrorText fallYouthPrice;
         private String year;
         private int row;
 
         MembershipPriceEditFields() {
             setText(elements.menuitem_membership_prices());
             FlexTable edittable = new FlexTable();
             edittable.setStyleName("edittable");
 
             edittable.setText(1, 0, elements.year_membership());
             edittable.setText(2, 0, elements.year_membership_youth());
             edittable.setText(3, 0, elements.spring());
             edittable.setText(4, 0, elements.course_membership());
             edittable.setText(5, 0, elements.train_membership());
             edittable.setText(6, 0, elements.youth_membership());
             edittable.setText(7, 0, elements.fall());
             edittable.setText(8, 0, elements.course_membership());
             edittable.setText(9, 0, elements.train_membership());
             edittable.setText(10, 0, elements.youth_membership());
 
             yearPrice = new TextBoxWithErrorText("yearPrice");
             yearPriceYouth = new TextBoxWithErrorText("yearPriceYouth");
             springCoursePrice = new TextBoxWithErrorText("springCoursePrice");
             springTrainPrice = new TextBoxWithErrorText("springTrainPrice");
             springYouthPrice = new TextBoxWithErrorText("springYouthPrice");
             fallCoursePrice = new TextBoxWithErrorText("fallCoursePrice");
             fallTrainPrice = new TextBoxWithErrorText("fallTrainPrice");
             fallYouthPrice = new TextBoxWithErrorText("fallYouthPrice");
 
             edittable.setWidget(1, 1, yearPrice);
             edittable.setWidget(2, 1, yearPriceYouth);
             edittable.setWidget(4, 1, springCoursePrice);
             edittable.setWidget(5, 1, springTrainPrice);
             edittable.setWidget(6, 1, springYouthPrice);
             edittable.setWidget(8, 1, fallCoursePrice);
             edittable.setWidget(9, 1, fallTrainPrice);
             edittable.setWidget(10, 1, fallYouthPrice);
 
             DockPanel dp = new DockPanel();
             dp.add(edittable, DockPanel.NORTH);
 
             saveButton = new NamedButton("membershipPriceEditView_saveButton", elements.save());
             saveButton.addClickHandler(this);
             cancelButton = new NamedButton("membershipPriceEditView_cancelButton", elements.cancel());
             cancelButton.addClickHandler(this);
 
             mainErrorLabel = new HTML();
             mainErrorLabel.setStyleName("error");
 
             HorizontalPanel buttonPanel = new HorizontalPanel();
             buttonPanel.add(saveButton);
             buttonPanel.add(cancelButton);
             buttonPanel.add(mainErrorLabel);
             dp.add(buttonPanel, DockPanel.NORTH);
             setWidget(dp);
         }
 
         public void onClick(ClickEvent event) {
             Widget sender = (Widget) event.getSource();
 
             if (sender == cancelButton) {
                 hide();
             } else if (sender == saveButton && validateFields()) {
                 doSave();
             }
         }
 
         private void doSave() {
             StringBuffer sb = new StringBuffer();
             sb.append("action=save");
             Util.addPostParam(sb, "year", year);
             Util.addPostParam(sb, "yearPrice", Util.money(yearPrice.getText()));
             Util.addPostParam(sb, "yearPriceYouth", Util.money(yearPriceYouth.getText()));
             Util.addPostParam(sb, "springCoursePrice", Util.money(springCoursePrice.getText()));
             Util.addPostParam(sb, "springTrainPrice", Util.money(springTrainPrice.getText()));
             Util.addPostParam(sb, "springYouthPrice", Util.money(springYouthPrice.getText()));
             Util.addPostParam(sb, "fallCoursePrice", Util.money(fallCoursePrice.getText()));
             Util.addPostParam(sb, "fallTrainPrice", Util.money(fallTrainPrice.getText()));
             Util.addPostParam(sb, "fallYouthPrice", Util.money(fallYouthPrice.getText()));
 
             ServerResponse callback = new ServerResponse() {
 
                 public void serverResponse(JSONValue response) {
                     JSONObject responseObj = response.isObject();
 
                     if ("1".equals(Util.str(responseObj.get("status")))) {
                         table.setText(row, 1, Util.money(yearPrice.getText()));
                         table.setText(row, 2, Util.money(yearPriceYouth.getText()));
                         table.setText(row, 3, Util.money(springCoursePrice.getText()));
                         table.setText(row, 4, Util.money(springTrainPrice.getText()));
                         table.setText(row, 5, Util.money(springYouthPrice.getText()));
                         table.setText(row, 6, Util.money(fallCoursePrice.getText()));
                         table.setText(row, 7, Util.money(fallTrainPrice.getText()));
                         table.setText(row, 8, Util.money(fallYouthPrice.getText()));
                         hide();
                     } else {
                         mainErrorLabel.setText(messages.save_failed());
                         Util.timedMessage(mainErrorLabel, "", 10);
                     }
                 }
             };
 
             AuthResponder.post(constants, messages, callback, sb, "registers/membershipprices.php");
 
         }
 
         private void init(int row, String year, String yearPrice, String yearPriceYouth, String springCoursePrice,
                 String springTrainPrice, String springYouthPrice, String fallCoursePrice, String fallTrainPrice,
                 String fallYouthPrice) {
             this.row = row;
             this.year = year;
             this.yearPrice.setText(yearPrice);
             this.yearPriceYouth.setText(yearPriceYouth);
             this.springCoursePrice.setText(springCoursePrice);
             this.springTrainPrice.setText(springTrainPrice);
             this.springYouthPrice.setText(springYouthPrice);
             this.fallCoursePrice.setText(fallCoursePrice);
             this.fallTrainPrice.setText(fallTrainPrice);
             this.fallYouthPrice.setText(fallYouthPrice);
             setText(elements.menuitem_membership_prices() + " " + year);
 
         }
 
         private boolean validateFields() {
             MasterValidator mv = new MasterValidator();
             Widget[] widgets = new Widget[] { yearPrice, yearPriceYouth, springCoursePrice, springTrainPrice,
                     fallCoursePrice, fallTrainPrice };
             mv.mandatory(messages.required_field(), widgets);
             mv.money(messages.field_money(), widgets);
             return mv.validateStatus();
         }
     }
 
     public void flushCompleted() {
         me.init();
     }
 }
