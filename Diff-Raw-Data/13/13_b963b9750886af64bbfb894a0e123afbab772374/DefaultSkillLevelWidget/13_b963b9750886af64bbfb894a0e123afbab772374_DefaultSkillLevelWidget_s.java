 package org.timadorus.webapp.client.character.ui.selectskill;
 
 import java.util.ArrayList;
 
 import java.util.List;
 import java.util.Set;
 
 import org.timadorus.webapp.beans.Character;
 import org.timadorus.webapp.beans.Skill;
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
 public class DefaultSkillLevelWidget extends FormPanel implements DefaultSelectSkillLevelDialog.Display {
   
   private Button nextButton;
 
   private Button prevButton;
 
   private VerticalPanel panel;
 
   private FlexTable buttonGrid;
 
   private FlexTable selectSkillGrid;
 
   private ListBox skillListBox;
 
   private Button addButton;
 
   private Button removeButton;
 
   private ListBox addedskillListBox;
 
   private Label skillLabel;
 
   private Label addedskillLabel;
   
   private Label genderLabel;
   
   private Label classFactionLabel;
   
   private Label skillsL0Label;
 
   private FlexTable selectStatGrid;
 
   private FlexTable selectSkillGridBox;
 
   private Button resetPage;
 
   private List<TextBox[]> textBoxList;
 
 
   private List<TextBoxHandler> textBoxHandler;
 
   public DefaultSkillLevelWidget() { 
     super();
 
     textBoxList = new ArrayList<TextBox[]>();    
     nextButton = new Button("weiter");
    prevButton = new Button("zurck");
     panel = new VerticalPanel();
     buttonGrid = new FlexTable();
     selectSkillGrid = new FlexTable();
     skillListBox = new ListBox();
     addButton = new Button("+");
     removeButton = new Button("-");
     addedskillListBox = new ListBox();
    skillLabel = new Label("L1 Fertigkeiten whlen: ");
    addedskillLabel = new Label("L1 Ausgewhlte Fertigkeiten: ");
     selectStatGrid = new FlexTable();
     selectSkillGridBox = new FlexTable();
     resetPage = new Button("reset");
     genderLabel = new Label("Geschlecht: | Rasse: ");
     classFactionLabel = new Label("Klasse: | Faction: ");
     skillsL0Label = new Label("Skills L0:");
    
    HTML headline = new HTML("<h1>L1 Fertigkeiten whlen</h1>");
     Image progressBar = new Image("media/images/progressbar_7.png");
     
     nextButton.setEnabled(false);
 
     addedskillLabel.setStyleName("labelColorRed");
     addedskillListBox.setWidth("150");
     
     selectSkillGrid.setBorderWidth(0);
     selectSkillGrid.setStylePrimaryName("selectGrid");
 
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
 
     selectStatGrid = getNewEmptySkillCostTable();
         
     // setting properties of the main panel
     panel.setStyleName("panel");
     panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
     panel.setWidth("100%");
 
     // adding widgets to the main panel
     panel.add(progressBar);
     panel.add(new Label("Schritt 7 von 9"));
     panel.add(genderLabel);       
     panel.add(classFactionLabel);   
     panel.add(skillsL0Label);  
     
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
    HTML information = new HTML("<h1>L1 Fertigkeiten Whlen</h1><p>Whlen sie hier die L1 Fertigkeiten ihres "
         + ". Beachten Sie, dass bestimmte Fertigkeiten ab bestimmten Level-Grad zu beziehen " + "sind.</p>");
     return information;
   }
   
   @Override
   public void setInformation(HTML information) {
     RootPanel.get("information").clear();
     RootPanel.get("information").add(information);
   }
   
   @Override
   public void setCharacter(Character c) {
       genderLabel.setText("Geschlecht: " + c.getGender() + " | Rasse: " + c.getGender());
       classFactionLabel.setText("Klasse: " + c.getCharClass().getName() + " | Faction: " + c.getFaction().getName());
       skillsL0Label.setText("Skills_L0: " + c.getSkillListNames());
   }
   
   @Override
   public void setNextButtonEnable(boolean enabled) {
     nextButton.setEnabled(enabled);
   }
   
   @Override
   public FormPanel getFormPanel() {
     return this;
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
   public void setChooseableSkills(List<Skill> skills) {
 
     for (Skill skill : skills) {
       skillListBox.addItem(skill.getName());
     }
 
     skillListBox.setVisibleItemCount(skillListBox.getItemCount());
     addedskillListBox.setVisibleItemCount(skillListBox.getItemCount());
   }
     
   private FlexTable getNewEmptySkillCostTable() {
 
     FlexTable table = new FlexTable();
     
     table.setBorderWidth(0);
     table.setStyleName("selectGrid");
 
     //append Header
     table.setWidget(0, 0, new Label("Skill                 "));
     table.setWidget(0, 1, new Label("Cost   "));
     table.setWidget(0, 2, new Label("Rank   "));
     table.setWidget(0, 3, new Label("Rk_Bn  "));
     table.setWidget(0, 4, new Label("Stat_Bn"));
     table.setWidget(0, 5, new Label("Level_Bn."));
     table.setWidget(0, 6, new Label("Item   "));
     table.setWidget(0, 7, new Label("Total"));
 
     //append Empty Line to size the table
     for (int i = 0; i < 8; i++) {
       TextBox tb = new TextBox();
       if (i == 0) {
         tb.setWidth("100");
       } else {
         tb.setWidth("60");
       }
 
       table.setWidget(1, i, tb);
     }
     
     return table;
   }
    
   @Override
   public void setSkillCostTable(Set<Skill> skills) { 
     String[] titleList = { "Skill-Name", "Cost", "Rank", "Rk_Bn", "Stat_Bn", "Level_Bn", "Item", "Total" };
     
     panel.remove(selectStatGrid);
     selectStatGrid = getNewEmptySkillCostTable();
     
     int j = 0;
     TextBox[] textBoxes;
 
     for (Skill skill : skills) {
       textBoxes = new TextBox[8];
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
         textBoxes[i] = tb;
 
         tb.addChangeHandler((ChangeHandler) this);
       }
       textBoxList.add(textBoxes); // merke zu jedem Skill die TextBox-Felder in einer Liste
       j++;
     }
     
     
     if (skills.size() != 0) {
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
 
     for (TextBox[] tb : textBoxList) {
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
 
     
 
 }
