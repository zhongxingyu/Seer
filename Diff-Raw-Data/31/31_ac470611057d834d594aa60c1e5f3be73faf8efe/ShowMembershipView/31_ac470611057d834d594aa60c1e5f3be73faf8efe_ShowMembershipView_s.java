 package no.knubo.accounting.client.views;
 
 import no.knubo.accounting.client.Constants;
 import no.knubo.accounting.client.Elements;
 import no.knubo.accounting.client.I18NAccount;
 import no.knubo.accounting.client.Util;
 import no.knubo.accounting.client.help.HelpPanel;
 import no.knubo.accounting.client.misc.AuthResponder;
 import no.knubo.accounting.client.misc.IdHolder;
 import no.knubo.accounting.client.misc.ImageFactory;
 import no.knubo.accounting.client.misc.ServerResponse;
 
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Widget;
 
 public class ShowMembershipView extends Composite implements ClickListener {
 
     static ShowMembershipView me;
     private FlexTable table;
     private HTML header;
     private HTML periodeHeader;
     private Image previousImage;
     private Image nextImage;
     private final I18NAccount messages;
     private final Constants constants;
     private final ViewCallback caller;
     private String action;
     protected int currentYear;
     protected int currentSemester;
     private IdHolder idHolder;
     final HelpPanel helpPanel;
     private final Elements elements;
 
     public static ShowMembershipView show(I18NAccount messages, Constants constants,
             ViewCallback caller, HelpPanel helpPanel, Elements elements) {
         if (me == null) {
             me = new ShowMembershipView(messages, constants, caller, helpPanel, elements);
         }
         return me;
     }
 
     public ShowMembershipView(I18NAccount messages, Constants constants, ViewCallback caller,
             HelpPanel helpPanel, Elements elements) {
         this.messages = messages;
         this.constants = constants;
         this.caller = caller;
         this.helpPanel = helpPanel;
         this.elements = elements;
 
         table = new FlexTable();
         table.setStyleName("tableborder");
         table.setText(0, 0, elements.lastname());
         table.setText(0, 1, elements.firstname());
         table.setText(0, 5, "");
         table.getRowFormatter().setStyleName(0, "header");
 
         header = new HTML();
         periodeHeader = new HTML();
         previousImage = ImageFactory.previousImage("ShowMembershipView.previousImage");
         previousImage.addClickListener(this);
         nextImage = ImageFactory.nextImage("ShowMembershipView.nextImage");
         nextImage.addClickListener(this);
         HorizontalPanel hp = new HorizontalPanel();
         hp.add(previousImage);
         hp.add(periodeHeader);
         hp.add(nextImage);
 
         DockPanel dp = new DockPanel();
         dp.add(header, DockPanel.NORTH);
         dp.add(hp, DockPanel.NORTH);
         dp.add(table, DockPanel.NORTH);
 
         initWidget(dp);
         idHolder = new IdHolder();
     }
 
     public void initShowAll() {
         header.setHTML(elements.member_heading_all());
         table.setText(0, 2, elements.year_membership());
         table.setText(0, 3, elements.course_membership());
         table.setText(0, 4, elements.train_membership());
         initAll("");
     }
 
     public void initShowMembers() {
         header.setHTML(elements.member_heading_year());
         action = "year";
         init("");
     }
 
     public void initShowTrainingMembers() {
         header.setHTML(elements.member_heading_train());
         action = "training";
         init("");
     }
 
     public void initShowClassMembers() {
         header.setHTML(elements.member_heading_course());
         action = "class";
         init("");
     }
 
     private void init(String posparam) {
         setVisible(true);
         idHolder.init();
 
         table.setHTML(0, 2, "");
         table.setHTML(0, 3, "");
         table.setHTML(0, 4, "");
 
         while (table.getRowCount() > 1) {
             table.removeRow(1);
         }
 
         ServerResponse callback = new ServerResponse() {
 
             public void serverResponse(JSONValue value) {
 
                 JSONObject root = value.isObject();
 
                 /* Both year and semester isn't present at the same time */
                 currentYear = Util.getInt(root.get("year"));
                 currentSemester = Util.getInt(root.get("semester"));
 
                 JSONArray members = root.get("members").isArray();
 
                 String count = String.valueOf(members.size());
                 periodeHeader.setHTML(messages.members_navig_heading(Util.str(root.get("text")),
                         count));
 
                 for (int i = 0; i < members.size(); i++) {
                     JSONArray names = members.get(i).isArray();
 
                     int row = i + 1;
                     String firstname = Util.str(names.get(1));
                     String lastname = Util.str(names.get(0));
                     String id = Util.str(names.get(2));
                    table.setText(row, 0, lastname);
                    table.setText(row, 1, firstname);
 
                     Image editUserImage = ImageFactory
                             .editImage("ShowMembershipView.editUserImage");
                     editUserImage.addClickListener(me);
                     table.setWidget(row, 5, editUserImage);
 
                     idHolder.add(id, editUserImage);
                     String style = (row % 2 == 0) ? "showlineposts2" : "showlineposts1";
                     table.getRowFormatter().setStyleName(row, style);
                 }
                 helpPanel.resize(me);
             }
         };
 
         AuthResponder.get(constants, messages, callback, "registers/members.php?" + posparam
                 + "&action=" + action);
 
     }
 
     public void onClick(Widget sender) {
         if (sender == previousImage) {
             doNext(-1);
         } else if (sender == nextImage) {
             doNext(1);
         } else {
             doEditUser(sender);
         }
     }
 
     private void doNext(int diff) {
         if (currentSemester != 0) {
             init("semester=" + (currentSemester + diff));
             return;
         }
 
         init("year=" + (currentYear + diff));
 
     }
 
     private void doEditUser(Widget sender) {
         String userId = idHolder.findId(sender);
         setVisible(false);
         caller.editPerson(userId);
     }
 
     private void initAll(String posparam) {
         setVisible(true);
         idHolder.init();
 
         while (table.getRowCount() > 1) {
             table.removeRow(1);
         }
 
         ServerResponse callback = new ServerResponse() {
 
             public void serverResponse(JSONValue value) {
 
                 JSONObject root = value.isObject();
 
                 /* Both year and semester isn't present at the same time */
                 currentYear = Util.getInt(root.get("year"));
                 currentSemester = Util.getInt(root.get("semester"));
 
                 JSONArray members = root.get("members").isArray();
 
                 String count = String.valueOf(members.size());
                 periodeHeader.setHTML(messages.members_navig_heading(Util.str(root.get("text")),
                         count));
 
                 for (int i = 0; i < members.size(); i++) {
                     JSONObject info = members.get(i).isObject();
 
                     int row = i + 1;
                     String firstname = Util.str(info.get("first"));
                     String lastname = Util.str(info.get("last"));
 
                     boolean year = Util.getBoolean(info.get("year"));
                     boolean train = Util.getBoolean(info.get("train"));
                     boolean course = Util.getBoolean(info.get("course"));
 
                     String id = Util.str(info.get("id"));
                     table.setText(row, 0, lastname);
                     table.setText(row, 1, firstname);
 
                     if (year) {
                         table.setText(row, 2, elements.x());
                     }
                     if (course) {
                         table.setText(row, 3, elements.x());
                     }
                     if (train) {
                         table.setText(row, 4, elements.x());
                     }
 
                     table.getCellFormatter().setStyleName(row, 2, "center");
                     table.getCellFormatter().setStyleName(row, 3, "center");
                     table.getCellFormatter().setStyleName(row, 4, "center");
 
                     Image editUserImage = ImageFactory
                             .editImage("ShowMembershipView.editUserImage");
                     editUserImage.addClickListener(me);
                     table.setWidget(row, 5, editUserImage);
 
                     idHolder.add(id, editUserImage);
                     String style = (row % 2 == 0) ? "showlineposts2" : "showlineposts1";
                     table.getRowFormatter().setStyleName(row, style);
                 }
                 helpPanel.resize(me);
             }
         };
 
         AuthResponder.get(constants, messages, callback, "registers/members.php?" + posparam
                 + "&action=all");
 
     }
 }
