 package org.timadorus.webapp.client.character.ui.selectskill;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.timadorus.webapp.beans.Character;
 import org.timadorus.webapp.beans.Skill;
 import org.timadorus.webapp.beans.User;
 import org.timadorus.webapp.client.DefaultTimadorusWebApp;
 import org.timadorus.webapp.client.character.ui.DefaultActionHandler;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
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
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 //FormPanel for selecting Skill-Level-1 items of a Character-Object
 public class DefaultSkillLevelWidget extends FormPanel implements ChangeHandler, DefaultSelectSkillLevelDialog.Display {
 
   // private final DefaultTimadorusWebApp entry;
 
   private Button nextButton = new Button("weiter");
 
   private Button prevButton = new Button("zur&uuml;ck");
 
   private VerticalPanel panel = new VerticalPanel();
 
   private FlexTable buttonGrid = new FlexTable();
 
   private FlexTable selectSkillGrid = new FlexTable();
 
   private ListBox skillListBox = new ListBox();
 
   private Button addButton = new Button("+");
 
   private Button removeButton = new Button("-");
 
   private ListBox addedskillListBox = new ListBox();
 
   private Label skillLabel = new Label("L1 Fertigkeiten w&auml;hlen: ");
 
   private Label addedskillLabel = new Label("L1 Ausgew&auml:hlte Fertigkeiten: ");
 
   private FlexTable selectStatGrid = new FlexTable();
 
   private FlexTable selectSkillGridBox = new FlexTable();
 
   private Button resetPage = new Button("reset");
 
   private List<TextBox[]> tbObjList = new ArrayList<TextBox[]>();
 
   private String[] titleList = { "Skill-Name", "Cost", "Rank", "Rk_Bn", "Stat_Bn", "Level_Bn", "Item", "Total" };
 
   private List<TextBoxHandler> textBoxHandler;
 
   private Label myGenderRaceLabel;
 
   private Label myClassFactionLabel;
 
   private Label mySkillLabel;
 
   public DefaultSkillLevelWidget() {
     super();
    
    textBoxHandler = new ArrayList<TextBoxHandler>();
 
     addedskillLabel.setStyleName("labelColorRed");
     nextButton.setEnabled(false);
     Image progressBar = new Image("media/images/progressbar_7.png");
 
     selectSkillGrid.setBorderWidth(0);
     selectSkillGrid.setStylePrimaryName("selectGrid");
 
    
     skillListBox.setVisibleItemCount(skillListBox.getItemCount());
     addedskillListBox.setVisibleItemCount(skillListBox.getItemCount());
     addedskillListBox.setWidth("150");
 
     // Label choosenskillLabel = new Label("Ausgewählte Fertigkeiten: ");
     selectSkillGrid.setWidget(0, 0, skillLabel);
     selectSkillGrid.setWidget(0, 12, addedskillLabel);
     selectSkillGrid.setWidget(1, 0, skillListBox);
     selectSkillGrid.setWidget(1, 8, addButton);
     selectSkillGrid.setWidget(2, 8, removeButton);
     selectSkillGrid.setWidget(1, 12, addedskillListBox);
 
     buttonGrid.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
     buttonGrid.setWidth("350px");
     buttonGrid.setWidget(0, 0, prevButton);
     buttonGrid.setWidget(0, 1, nextButton);
 
     selectStatGrid = getSkillCostTableLabel();
 
     myClassFactionLabel = new Label("Kalsse: | Fraktion: ");
     myGenderRaceLabel = new Label("Geschlecht: | Rasse: ");
     mySkillLabel = new Label("Skills: ");
 
     // Add it to the root panel.
 
     HTML headline = new HTML("<h1>L1 Fertigkeiten w&auml;hlen</h1>");
 
     panel.setStyleName("panel");
     panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
     panel.setWidth("100%");
 
     panel.add(progressBar);
     panel.add(new Label("Schritt 7 von 9"));
     panel.add(myGenderRaceLabel);
     panel.add(myClassFactionLabel);
     panel.add(mySkillLabel);
 
     panel.add(headline);
     panel.add(selectSkillGrid);
     panel.add(selectStatGrid);
     panel.add(selectSkillGridBox);
     panel.add(buttonGrid);
 
     RootPanel.get("information").clear();
     RootPanel.get("information").add(getInformation());
 
     RootPanel.get("content").clear();
     RootPanel.get("content").add(panel);
 
   }
 
   private static final HTML getInformation() {
     HTML information = new HTML("<h1>L1 Fertigkeiten Wählen</h1><p>Wählen sie hier die L1 Fertigkeiten ihres "
         + ". Beachten Sie, dass bestimmte Fertigkeiten ab bestimmten Level-Grad zu beziehen " + "sind.</p>");
     return information;
   }
 
   public void reloadSkillCostTable(Set<String> addedSkillList, List<Skill> skillLevel1) {
 
     Set<Skill> skillSet = new HashSet<Skill>();
 
     for (String skillName : addedSkillList) {
       for (Skill skill : skillLevel1) {
         if (skill.getName().equals(skillName)) {
           skillSet.add(skill);
         }
       }
     }
 
     panel.remove(selectStatGrid);
     selectStatGrid = getSkillCostTableLabel();
 
     int j = 0;
     TextBox[] tbArr = new TextBox[8];
 
     for (Skill skill : skillSet) {
       tbArr = new TextBox[8];
       for (int i = 0; i < 8; i++) {
         TextBox tb = new TextBox();
         if (i == 0) {
           tb.setWidth("100");
         } else {
           tb.setWidth("60");
         }
 
         tb.setText(skill.getGesamtInfo()[i]);
         tb.setTitle(titleList[i]);
         selectStatGrid.setWidget(j + 1, i, tb);
         tbArr[i] = tb;
 
         tb.addChangeHandler(this);
       }
       tbObjList.add(tbArr); // merke zu jedem Skill die TextBox-Felder in einer Liste
       j++;
 
     }
     if (skillSet.size() != 0) {
       selectSkillGrid.setWidget(j + 1, 4, resetPage);
     } else {
       selectSkillGrid.remove(resetPage);
     }
 
     panel.add(selectStatGrid);
     panel.add(selectSkillGridBox);
     panel.add(resetPage);
     panel.add(buttonGrid);
 
     RootPanel.get("content").add(panel);
   }
 
   public FlexTable getSkillCostTableLabel() {
 
     FlexTable selectStatGrid1 = new FlexTable();
     selectStatGrid1.setBorderWidth(0);
     selectStatGrid1.setStyleName("selectGrid");
 
     selectStatGrid1.setWidget(0, 0, new Label("Skill                 "));
     selectStatGrid1.setWidget(0, 1, new Label("Cost   "));
     selectStatGrid1.setWidget(0, 2, new Label("Rank   "));
     selectStatGrid1.setWidget(0, 3, new Label("Rk_Bn  "));
     selectStatGrid1.setWidget(0, 4, new Label("Stat_Bn"));
     selectStatGrid1.setWidget(0, 5, new Label("Level_Bn."));
     selectStatGrid1.setWidget(0, 6, new Label("Item   "));
     selectStatGrid1.setWidget(0, 7, new Label("Total"));
 
     for (int i = 0; i < 8; i++) {
       TextBox tb = new TextBox();
       if (i == 0) {
         tb.setWidth("100");
       } else {
         tb.setWidth("60");
       }
 
       selectStatGrid1.setWidget(1, i, tb);
     }
 
     return selectStatGrid1;
   }
 
   @Override
   public FormPanel getFormPanel() {
     return this;
   }
 
   @Override
   public String getSelectedItemAddedSkills() {
     int selectedIndex = addedskillListBox.getSelectedIndex();
     try {
       return addedskillListBox.getValue(selectedIndex);
     } catch (IndexOutOfBoundsException ex) {
       return null;
     }
   }
 
   @Override
   public void removeItemAddedSkills(String itemname) {
     for (int i = 0; i < addedskillListBox.getItemCount(); i++) {
       if (addedskillListBox.getItemText(i).equals(itemname)) {
         addedskillListBox.removeItem(i);
       }
     }
   }
 
   public void readyToSave(boolean hasSelectedItems) {
     if (!hasSelectedItems) {
       addedskillLabel.setStyleName("labelColorRed");
       nextButton.setEnabled(false);
       resetPage.setEnabled(false);
     } else {
       addedskillLabel.setStyleName("labelColorGreen");
       nextButton.setEnabled(true);
       resetPage.setEnabled(true);
     }
   }
 
   @Override
   public String getSelectedItemSkills() {
     try {
       return skillListBox.getValue(skillListBox.getSelectedIndex());
     } catch (IndexOutOfBoundsException ex) {
       return null;
     }
   }
 
   @Override
   public void addItemAddedSkills(String item) {
     addedskillListBox.addItem(item);
   }
 
   @Override
   public void addTextBoxHanlder(TextBoxHandler handler) {
     textBoxHandler.add(handler);
   }
 
   @Override
   public void onChange(ChangeEvent event) {
     String[] skInfo = new String[3];
 
     TextBox textBox = (TextBox) event.getSource();
     String id = String.valueOf(textBox.hashCode());
 
     String title = ((TextBox) event.getSource()).getTitle();
     String value = ((TextBox) event.getSource()).getValue();
 
     for (TextBox[] tb : tbObjList) {
       String skillN = "";
       for (int i = 0; i < tb.length; i++) {
         if (tb[i] != null) {
           if (i == 0) {
             skillN = tb[i].getText();
             int m = Integer.valueOf(tb[3].getText()) + Integer.valueOf(tb[4].getText()); // update "Total"-Cell
             tb[7].setText("" + m);
           }
 
           String texBoxID = "" + ((Object) tb[i]).hashCode();
           if (texBoxID.equals(id)) {
             skInfo[0] = skillN;
             skInfo[1] = title;
             skInfo[2] = value;
           }
         }
       }
     }
     for (TextBoxHandler tbh : textBoxHandler) {
       tbh.onChange(skInfo);
     }
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
   public void addPrevButtonHandler(final DefaultActionHandler handler) {
     prevButton.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent event) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addAddButtonHandler(final DefaultActionHandler handler) {
     addButton.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent event) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addRemoveButtonHandler(final DefaultActionHandler handler) {
     removeButton.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent event) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addResetButtonHandler(final DefaultActionHandler handler) {
     resetPage.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent event) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addSkillListBoxHandler(final DefaultActionHandler handler) {
     skillListBox.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent event) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void onNextButtonClick(DefaultTimadorusWebApp entry, Character character, User user) {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void onPrevButtonClick(DefaultTimadorusWebApp entry, Character character, User user) {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void setContent(Character characterIn, List<Skill> chooseableSkills) {
     //Setting character infomations
     myGenderRaceLabel
         .setText("Geschlecht: " + characterIn.getGender() + " | Rasse: " + characterIn.getRace().getName());
     myClassFactionLabel.setText("Klasse: " + characterIn.getCharClass().getName() + " | Faction: "
         + characterIn.getFaction().getName());
     mySkillLabel.setText("Skills: " + characterIn.getSkillListNames());
     
     //setting skill inforamtions:
     skillListBox.clear();
     
     for (Skill skill : chooseableSkills) {
       skillListBox.addItem(skill.getName());
     }
 
     skillListBox.setVisibleItemCount(skillListBox.getItemCount());
     addedskillListBox.setVisibleItemCount(skillListBox.getItemCount());
     addedskillListBox.setWidth("150");
   }
 }
