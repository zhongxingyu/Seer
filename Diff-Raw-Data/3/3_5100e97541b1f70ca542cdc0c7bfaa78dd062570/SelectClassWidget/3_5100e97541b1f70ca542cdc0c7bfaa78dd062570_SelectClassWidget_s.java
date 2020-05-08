 package org.timadorus.webapp.client.character.ui.selectclass;
 
 import java.util.List;
 
 import org.timadorus.webapp.beans.CClass;
 import org.timadorus.webapp.beans.Character;
 import org.timadorus.webapp.beans.User;
 import org.timadorus.webapp.client.DefaultTimadorusWebApp;
 import org.timadorus.webapp.client.character.ui.DefaultActionHandler;
 import org.timadorus.webapp.client.character.ui.selectfraction.SelectFactionDialog;
 import org.timadorus.webapp.client.character.ui.selectrace.SelectRaceDialog;
 
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
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 //Panel for selecting characters Class
 public class SelectClassWidget extends FormPanel implements SelectClassDialog.Display {
 
   Button nextButton = new Button("weiter");
 
   Button prevButton = new Button("zurück");
 
   VerticalPanel panel = new VerticalPanel();
 
   FlexTable selectClassGrid = new FlexTable(); // grid for handling selections
 
   FlexTable buttonGrid = new FlexTable(); // grid for next/prev buttons
 
   ListBox classListBox = new ListBox(); // listbox for available classes
 
   public SelectClassWidget(Character characterIn, List<CClass> classes) {
     super();
 
     // headline
     HTML headline = new HTML("<h1>Klasse wählen</h1>");
     // progress bar picture
     Image progressBar = new Image("media/images/progressbar_2.png");
 
     // setting properties for selectClassGrid
     selectClassGrid.setBorderWidth(0);
     selectClassGrid.setStylePrimaryName("selectGrid");
 
     // filling the list with available classes
     for (CClass newClass : classes) {
       if (characterIn.getRace().containsClass(newClass)) {
         classListBox.addItem(newClass.getName());
       }
     }
 
     Label classLabel = new Label("Klasse wählen: ");
 
     selectClassGrid.setWidget(0, 0, classLabel);
     selectClassGrid.setWidget(0, 1, classListBox);
 
     classListBox.setVisibleItemCount(classListBox.getItemCount());
 
     // set properties of buttongrid
     buttonGrid.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
     buttonGrid.setWidth("350px");
     buttonGrid.setWidget(0, 0, prevButton);
     buttonGrid.setWidget(0, 1, nextButton);
     // setting properties of the main panel
     panel.setStyleName("panel");
     panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
     panel.setWidth("100%");
 
     // adding widgets to the main panel
     panel.add(progressBar);
     panel.add(new Label("Schritt 2 von 9"));
     panel.add(new Label("Geschlecht: " + characterIn.getGender() + " | Rasse: " + characterIn.getRace().getName()));
 
     panel.add(headline);
     panel.add(selectClassGrid);
     panel.add(buttonGrid);
 
     // clearing "information" #div and adding actual informations for this panel
     RootPanel.get("information").clear();
     RootPanel.get("information").add(getInformation());
 
     // clearing "content" #div and adding this mainpanel to this #div
     RootPanel.get("content").clear();
     RootPanel.get("content").add(panel);
 
   }
 
   @Override
   public String getSelectedClass() {
 
     return classListBox.getValue(classListBox.getSelectedIndex());
 
   }
 
   // returns and hols current panel information
   private static final HTML getInformation() {
     HTML information = new HTML("<h1>Klasse wählen</h1><p>Wählen sie hier die Klasse ihres Charakteres. "
         + "Die Klasse bestimmt wie gut sie bestimmte Fähigkeiten lernen können."
        + "</p><p>Beachten sie, dass bestimmte Klassen nur bestimmte Rassen sowie " + "Fraktionen wählen können.</p>");
     return information;
   }
 
   @Override
   public FormPanel getFormPanel() {
     return this;
   }
 
   @Override
   public void setPrevButtonHandler(final DefaultActionHandler handler) {
     prevButton.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent event) {
         handler.onAction();
 
       }
     });
   }
 
   @Override
   public void setNextButtonHandler(final DefaultActionHandler handler) {
     nextButton.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent event) {
         handler.onAction();
 
       }
     });
   }
 
   @Override
   public void setClassGridButtonHandler(final DefaultActionHandler handler) {
     selectClassGrid.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent event) {
         handler.onAction();
 
       }
     });
   }
 
   @Override
   public void loadSelectRaceDialog(DefaultTimadorusWebApp entry, Character character, User user) {
     RootPanel.get("content").clear();
     RootPanel.get("content").add(SelectRaceDialog.getDialog(entry, user, character).getFormPanel());
   }
 
   @Override
   public void loadSelectFactionDialog(DefaultTimadorusWebApp entry, Character character, User user) {
     RootPanel.get("content").clear();
     RootPanel.get("content").add(SelectFactionDialog.getDialog(entry, character, user).getFormPanel());
   }
 
 }
