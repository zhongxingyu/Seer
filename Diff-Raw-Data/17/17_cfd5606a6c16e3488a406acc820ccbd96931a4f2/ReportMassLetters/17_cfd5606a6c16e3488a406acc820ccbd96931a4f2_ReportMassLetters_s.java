 package no.knubo.accounting.client.views.reporting;
 
 import java.util.ArrayList;
 
 import no.knubo.accounting.client.Constants;
 import no.knubo.accounting.client.Elements;
 import no.knubo.accounting.client.I18NAccount;
 import no.knubo.accounting.client.Util;
 import no.knubo.accounting.client.misc.AuthResponder;
 import no.knubo.accounting.client.misc.ImageFactory;
 import no.knubo.accounting.client.misc.ServerResponse;
 import no.knubo.accounting.client.misc.ServerResponseString;
 import no.knubo.accounting.client.ui.NamedButton;
 import no.knubo.accounting.client.ui.TextBoxWithErrorText;
 import no.knubo.accounting.client.validation.MasterValidator;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class ReportMassLetters extends Composite implements ClickHandler {
     private static ReportMassLetters reportInstance;
     private final Constants constants;
     private final I18NAccount messages;
     private FlexTable table;
     private ArrayList<RadioButton> radiobuttons;
     private final Elements elements;
     private NamedButton joinButton;
     private NamedButton newButton;
 
     public static ReportMassLetters getInstance(Constants constants, I18NAccount messages, Elements elements) {
         if (reportInstance == null) {
             reportInstance = new ReportMassLetters(constants, messages, elements);
         }
         return reportInstance;
     }
 
     public ReportMassLetters(Constants constants, I18NAccount messages, Elements elements) {
         this.constants = constants;
         this.messages = messages;
         this.elements = elements;
 
         table = new FlexTable();
         table.setStyleName("edittable");
 
         table.setText(0, 0, elements.letter_template());
         table.getRowFormatter().setStyleName(0, "header");
         radiobuttons = new ArrayList<RadioButton>();
 
         DockPanel dp = new DockPanel();
         dp.add(table, DockPanel.NORTH);
 
         joinButton = new NamedButton("join_letters", elements.join_letters());
         joinButton.addClickHandler(this);
 
         newButton = new NamedButton("new_letters", elements.new_massletter());
         newButton.addClickHandler(this);
 
         HorizontalPanel hp = new HorizontalPanel();
         hp.add(joinButton);
         hp.add(newButton);
         dp.add(hp, DockPanel.NORTH);
 
         initWidget(dp);
     }
 
     public void init() {
         while (table.getRowCount() > 1) {
             table.removeRow(1);
         }
 
         radiobuttons.clear();
 
         ServerResponse callback = new ServerResponse() {
 
             public void serverResponse(JSONValue parse) {
 
                 JSONArray array = parse.isArray();
 
                 for (int i = 0; i < array.size(); i++) {
                     JSONValue value = array.get(i);
 
                     RadioButton rb = new RadioButton("letters");
                     rb.setText(Util.str(value));
                     radiobuttons.add(rb);
                     table.setWidget(1 + i, 0, rb);
                     table.getCellFormatter().setStyleName(i + 1, 0, "desc");
                    table.setWidget(i + 1, 1, createEditImage(Util.str(value), "advanced"));
                    table.setText(i + 1, 2, elements.advanced());
 
                     table.setWidget(i + 1, 3, createEditImage(Util.str(value), "simple"));
                     table.setText(i + 1, 4, elements.simplified());
                 }
             }
         };
 
         AuthResponder.get(constants, messages, callback, "reports/massletter.php?action=list");
 
     }
 
     protected Widget createEditImage(final String str, final String editType) {
         Image editImage = ImageFactory.editImage("edit");
         ClickHandler handler = new ClickHandler() {
 
             public void onClick(ClickEvent event) {
                 editFile(str, editType);
             }
         };
         editImage.addClickHandler(handler);
         return editImage;
     }
 
     protected void editFile(final String filename, final String editType) {
         ServerResponse callback = new ServerResponseString() {
 
             public void serverResponse(JSONValue responseObj) {
                 /* Not Used */
             }
 
             public void serverResponse(String response) {
                 openEditDialogSimple(filename, response);
             }
         };
 
         AuthResponder.get(constants, messages, callback, "reports/massletter.php?action=source&template=" + filename);
     }
 
     public void onClick(ClickEvent event) {
         if (joinButton == event.getSource()) {
             for (RadioButton rb : radiobuttons) {
 
                 if (rb.getValue()) {
                     doLetter(rb.getText());
                 }
             }
         }
         if (newButton == event.getSource()) {
             askForNewNameAndOpenEdit();
         }
     }
 
     private void askForNewNameAndOpenEdit() {
         final DialogBox db = new DialogBox();
         db.setText(elements.new_massletter_name());
         VerticalPanel vp = new VerticalPanel();
 
         HorizontalPanel infohp = new HorizontalPanel();
         final TextBoxWithErrorText nameBox = new TextBoxWithErrorText("massletter_name");
 
         infohp.add(new Label(elements.name()));
         infohp.add(nameBox);
         vp.add(infohp);
 
         HorizontalPanel buttonhp = new HorizontalPanel();
         NamedButton okButton = new NamedButton("ok", elements.ok());
         buttonhp.add(okButton);
 
         okButton.addClickHandler(new ClickHandler() {
 
             public void onClick(ClickEvent event) {
                 MasterValidator mv = new MasterValidator();
                 mv.mandatory(messages.required_field(), nameBox);
 
                 if (mv.validateStatus()) {
                     openEditDialogSimple(nameBox.getText() + ".txt", "");
                     db.hide();
                 }
             }
 
         });
 
         NamedButton cancelButton = new NamedButton("cancel", elements.cancel());
         buttonhp.add(cancelButton);
         cancelButton.addClickHandler(new ClickHandler() {
 
             public void onClick(ClickEvent event) {
                 db.hide();
             }
 
         });
 
         vp.add(buttonhp);
         db.setWidget(vp);
         db.center();
         nameBox.setFocus(true);
     }
 
     private void doLetter(String template) {
         Window.open(this.constants.baseurl() + "reports/massletter.php?action=pdf&template=" + template, "_blank", "");
     }
 
     private void openEditDialogSimple(final String filename, String response) {
         EditMassletterView editMassletterView = EditMassletterView.getInstance(constants, messages, elements);
         editMassletterView.init(response, filename);
         CloseHandler<PopupPanel> closehandler = new CloseHandler<PopupPanel>() {
 
             public void onClose(CloseEvent<PopupPanel> event) {
                 init();
             }
         };
         editMassletterView.addCloseHandler(closehandler);
     }
 }
