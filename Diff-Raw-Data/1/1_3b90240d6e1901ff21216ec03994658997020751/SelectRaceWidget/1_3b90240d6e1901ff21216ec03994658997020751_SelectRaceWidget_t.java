 package org.timadorus.webapp.client.character.ui.selectrace;
 
 import java.util.List;
 
 import org.timadorus.webapp.beans.Character;
 import org.timadorus.webapp.beans.User;
 import org.timadorus.webapp.client.DefaultTimadorusWebApp;
 import org.timadorus.webapp.client.character.ui.DefaultActionHandler;
 import org.timadorus.webapp.client.character.ui.createcharacter.CreateDialog;
 import org.timadorus.webapp.client.character.ui.selectclass.SelectClassDialog;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * FormPanel for selecting Race of a Character-Object
  * 
  * @author aaz210
  * 
  */
 public class SelectRaceWidget extends FormPanel implements SelectRaceDialog.Display {
 
   private Button nextButton;
 
   private Button prevButton;
 
   private VerticalPanel panel;
 
   private RadioButton selectMale;
 
   private RadioButton selectFemale;
 
   private FlexTable selectGenderGrid;
 
   private FlexTable buttonGrid;
 
   private FlexTable selectRaceGrid;
 
   private ListBox raceListBox;
 
   public SelectRaceWidget(List<String> racenames) {
     super();
 
     // init controls
     nextButton = new Button("weiter");
     prevButton = new Button("zurück");
     panel = new VerticalPanel();
     selectMale = new RadioButton("selectGender", "männlich");
     selectFemale = new RadioButton("selectGender", "weiblich");
     selectGenderGrid = new FlexTable();
    selectRaceGrid = new FlexTable();
     buttonGrid = new FlexTable();
     raceListBox = new ListBox();
 
     // arrange controls
     Image progressBar = new Image("media/images/progressbar_1.png");
 
     selectGenderGrid.setBorderWidth(0);
     selectGenderGrid.setStylePrimaryName("selectGrid");
 
     Label genderLabel = new Label("Geschlecht wählen:");
 
     selectGenderGrid.setWidget(0, 0, genderLabel);
     selectGenderGrid.setWidget(0, 1, selectMale);
     selectGenderGrid.setWidget(0, 2, selectFemale);
 
     selectMale.setValue(true);
 
     selectRaceGrid.setBorderWidth(0);
     selectRaceGrid.setStylePrimaryName("selectGrid");
 
     for (String racename : racenames) {
       raceListBox.addItem(racename);
     }
 
     Label raceLabel = new Label("Rasse wählen: ");
     selectRaceGrid.setWidget(0, 0, raceLabel);
     selectRaceGrid.setWidget(0, 1, raceListBox);
 
     raceListBox.setVisibleItemCount(raceListBox.getItemCount());
 
     buttonGrid.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
     buttonGrid.setWidth("350px");
     buttonGrid.setWidget(0, 0, prevButton);
     buttonGrid.setWidget(0, 1, nextButton);
 
     // Add it to the root panel.
     HTML headline = new HTML("<h1>Geschlecht und Rasse wählen</h1>");
 
     panel.setStyleName("panel");
     panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
     panel.setWidth("100%");
 
     panel.add(progressBar);
     panel.add(new Label("Schritt 1 von 9"));
     panel.add(headline);
 
     panel.add(selectGenderGrid);
 
     panel.add(selectRaceGrid);
 
     panel.add(buttonGrid);
 
     RootPanel.get("information").clear();
     RootPanel.get("information").add(getInformation());
 
     RootPanel.get("content").clear();
     RootPanel.get("content").add(panel);
 
   }
 
   public void loadCharacterPanel(User user, DefaultTimadorusWebApp entry) {
     RootPanel.get("content").clear();
     RootPanel.get("content").add(CreateDialog.getCreateDialog(entry, user).getFormPanel());
   }
 
   public void loadSelectClassPanel(DefaultTimadorusWebApp entry, User user, Character character) {
     RootPanel.get("content").clear();
     RootPanel.get("content").add(SelectClassDialog.getSelecteddDialog(entry, character, user)
                                      .getFormPanel());
   }
 
   public String getSelectedRace() {
 
     return raceListBox.getValue(raceListBox.getSelectedIndex());
 
   }
 
   public String getSelectedGender() {
     String gender = "männlich";
     if (selectMale.getValue()) {
       gender = "männlich";
     } else if (selectFemale.getValue()) {
       gender = "weiblich";
     }
     return gender;
   }
 
   private static final HTML getInformation() {
     HTML information = new HTML(
                                 "<h1>Rasse und Geschlecht wählen</h1><p>Wählen sie hier das Geschlecht "
                                     + "und die Rasse ihres Charakteres. Beachten sie, dass bestimmte"
                                     + " Rassen nur bestimmte Klassen sowie "
                                     + "Fraktionen wählen können.</p>");
     return information;
   }
 
   @Override
   public FormPanel getFormPanel() {
     return this;
   }
 
   @Override
   public void addRaceSelectionHandler(final DefaultActionHandler handler) {
     raceListBox.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent event) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addPrevButtonHandler(final DefaultActionHandler handler) {
     prevButton.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent event) {
         handler.onAction();
 
       }
     });
   }
 
   @Override
   public void addNextButtonHandler(final DefaultActionHandler handler) {
     nextButton.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent event) {
         handler.onAction();
 
       }
     });
   }
 
   @Override
   public void showRaceSelection(String raceName, String raceDescription,
                                 List<String> availableClasses, List<String> availableFactions) {
 
     RootPanel.get("information").clear();
 
     RootPanel.get("information").add(new HTML("<h1>" + raceName + "</h1><p>" + raceDescription
                                          + "</p>"));
 
     // Show available Classes
     RootPanel.get("information").add(new HTML("<h2>Wählbare Klassen</h2>"));
 
     StringBuilder sb = new StringBuilder("<ul>");
     for (String classname : availableClasses) {
       sb.append("<li>");
       sb.append(classname);
       sb.append("</li>");
     }
     sb.append("</ul>");
     RootPanel.get("information").add(new HTML(sb.toString()));
 
     // Show available Factions
     RootPanel.get("information").add(new HTML("<h2>Wählbare Fraktionen</h2>"));
 
     sb = new StringBuilder("<ul>");
     for (String factionname : availableFactions) {
       sb.append("<li>");
       sb.append(factionname);
       sb.append("</li>");
     }
     sb.append("</ul>");
     RootPanel.get("information").add(new HTML(sb.toString()));
 
   }
 
 }
