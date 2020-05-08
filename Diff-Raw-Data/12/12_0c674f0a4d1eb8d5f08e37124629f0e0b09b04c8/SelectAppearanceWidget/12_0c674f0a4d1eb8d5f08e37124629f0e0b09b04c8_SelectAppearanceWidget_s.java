 package org.timadorus.webapp.client.character.ui.selectappearance;
 
 import org.timadorus.webapp.beans.User;
 import org.timadorus.webapp.client.DefaultTimadorusWebApp;
 import org.timadorus.webapp.client.character.Character;
 import org.timadorus.webapp.client.character.ui.DefaultActionHandler;
 import org.timadorus.webapp.client.character.ui.selectname.SelectNameDialog;
 import org.timadorus.webapp.client.character.ui.selectskilllevel.SelectSkillLevelDialog;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * Panel for the selection of the characters appearance
  * 
  * @author aaz210
  * 
  */
 public class SelectAppearanceWidget extends FormPanel implements SelectAppearanceDialog.Display {
 
   private Button nextButton;
 
   private Button prevButton;
 
   private VerticalPanel panel;
 
   private Image selectWhiteSkinImage;
 
   private Image selectBlackSkinImage;
 
   private Image selectBrownSkinImage;
 
   private Image selectYellowSkinImage;
 
   private Image selectRedSkinImage;
 
   private Image selectGreenSkinImage;
 
   private Image selectBlueSkinImage;
 
   private Image selectWhiteHairImage;
 
   private Image selectBlackHairImage;
 
   private Image selectBrownHairImage;
 
   private Image selectYellowHairImage;
 
   private Image selectRedHairImage;
 
   private Image selectGreenHairImage;
 
   private Image selectBlueHairImage;
 
   private Label skinColor;
 
   private Label hairColor;
 
   private FlexTable selectSkinColorGrid;
 
   private FlexTable selectHairColorGrid;
 
   // grid for next/prev buttons
   private FlexTable buttonGrid;
 
   private static int zero = 0;
  private static int one = 0;
  private static int two = 0;
  private static int three = 0;
  private static int four = 0;
  private static int five = 0;
  private static int six = 0;
   
   public SelectAppearanceWidget(Character characterIn) {
     super();
 
     // Init Controls
     nextButton = new Button("weiter");
     prevButton = new Button("zurück");
     panel = new VerticalPanel();
     selectWhiteSkinImage = new Image("media/images/whiteBox.jpg");
     selectBlackSkinImage = new Image("media/images/blackBox.jpg");
     selectBrownSkinImage = new Image("media/images/brownBox.jpg");
     selectYellowSkinImage = new Image("media/images/yellowBox.jpg");
     selectRedSkinImage = new Image("media/images/redBox.jpg");
     selectGreenSkinImage = new Image("media/images/greenBox.jpg");
     selectBlueSkinImage = new Image("media/images/blueBox.jpg");
     selectWhiteHairImage = new Image("media/images/whiteBox.jpg");
     selectBlackHairImage = new Image("media/images/blackBox.jpg");
     selectBrownHairImage = new Image("media/images/brownBox.jpg");
     selectYellowHairImage = new Image("media/images/yellowBox.jpg");
     selectRedHairImage = new Image("media/images/redBox.jpg");
     selectGreenHairImage = new Image("media/images/greenBox.jpg");
     selectBlueHairImage = new Image("media/images/blueBox.jpg");
     skinColor = new Label("Klicke auf eine Farbe um deine Hautfarbe zu bestimmen!");
     hairColor = new Label("Klicke auf eine Farbe um deine Haarfarbe zu bestimmen!");
     selectSkinColorGrid = new FlexTable();
     selectHairColorGrid = new FlexTable();
     buttonGrid = new FlexTable();
 
     // Arrange Controls
 
     nextButton.setEnabled(false);
 
     HTML headline = new HTML("<h1>Aussehen waehlen</h1>");
 
     Image progressBar = new Image("media/images/progressbar_7.png");
 
     RootPanel.get("information").add(new HTML("Waehle das Aussehen deines Charakters!"));
 
     buttonGrid.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
     buttonGrid.setWidth("350px");
     buttonGrid.setWidget(0, 0, prevButton);
     buttonGrid.setWidget(0, 1, nextButton);
 
     panel.setStyleName("panel");
     panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
     panel.setWidth("100%");
 
     panel.add(progressBar);
     panel.add(new Label("Schritt 8 von 9"));
     panel.add(new Label("Geschlecht: " + characterIn.getGender() + " | Rasse: "
         + characterIn.getRace().getName()));
     panel.add(new Label("Klasse: " + characterIn.getCharClass().getName() + " | Faction: "
         + characterIn.getFaction().getName()));
     panel.add(new Label("Skills_L0: " + characterIn.getSkillListNames()));
     panel.add(new Label("Skills_L1: " + characterIn.getSkillLevel1ListNames()));
 
     selectSkinColorGrid.setWidget(0, zero, selectWhiteSkinImage);
     selectSkinColorGrid.setWidget(0, one, selectBlackSkinImage);
     selectSkinColorGrid.setWidget(0, two, selectBrownSkinImage);
     selectSkinColorGrid.setWidget(0, three, selectGreenSkinImage);
     selectSkinColorGrid.setWidget(0, four, selectYellowSkinImage);
     selectSkinColorGrid.setWidget(0, five, selectRedSkinImage);
     selectSkinColorGrid.setWidget(0, six, selectBlueSkinImage);
 
     selectHairColorGrid.setWidget(0, zero, selectWhiteHairImage);
     selectHairColorGrid.setWidget(0, one, selectBlackHairImage);
     selectHairColorGrid.setWidget(0, two, selectBrownHairImage);
     selectHairColorGrid.setWidget(0, three, selectGreenHairImage);
     selectHairColorGrid.setWidget(0, four, selectYellowHairImage);
     selectHairColorGrid.setWidget(0, five, selectRedHairImage);
     selectHairColorGrid.setWidget(0, six, selectBlueHairImage);
 
     panel.add(headline);
 
     panel.add(new Label("Waehlen Sie die Hautfarbe Ihres Charakters: "));
     panel.add(selectSkinColorGrid);
     panel.add(skinColor);
 
     panel.add(new Label("Waehlen Sie die Haarfarbe Ihres Charakters: "));
     panel.add(selectHairColorGrid);
     panel.add(hairColor);
 
     panel.add(buttonGrid);
 
     RootPanel.get("information").clear();
     RootPanel.get("information").add(getInformation());
 
     RootPanel.get("content").clear();
     RootPanel.get("content").add(panel);
   }
 
   /**
    * Gets the HTML for the information panel.
    * 
    * @return the HTML that specifies the text and of the information panel
    */
   private static final HTML getInformation() {
     HTML information = new HTML(
                                 "<h1>Aussehen wählen</h1><p>Wählen Sie hier das Aussehen ihres Charakters.</p>");
     return information;
   }
 
   @Override
   public FormPanel getFormPanel() {
     return this;
   }
 
   @Override
   public void addBlackHairHandler(final DefaultActionHandler handler) {
     selectBlackHairImage.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addWhiteHairHandler(final DefaultActionHandler handler) {
     selectWhiteHairImage.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addBrownHairHandler(final DefaultActionHandler handler) {
     selectBrownHairImage.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addGreenHairHandler(final DefaultActionHandler handler) {
     selectGreenHairImage.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addRedHairHandler(final DefaultActionHandler handler) {
     selectRedHairImage.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addYellowHairHandler(final DefaultActionHandler handler) {
     selectYellowHairImage.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addBlueHairHandler(final DefaultActionHandler handler) {
     selectBlueHairImage.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addBlackSkinHandler(final DefaultActionHandler handler) {
     selectBlackSkinImage.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addWhiteSkinHandler(final DefaultActionHandler handler) {
     selectWhiteSkinImage.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addBrownSkinHandler(final DefaultActionHandler handler) {
     selectBrownSkinImage.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addGreenSkinHandler(final DefaultActionHandler handler) {
     selectGreenSkinImage.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addRedSkinHandler(final DefaultActionHandler handler) {
     selectRedSkinImage.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addYellowSkinHandler(final DefaultActionHandler handler) {
     selectYellowSkinImage.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addBlueSkinHandler(final DefaultActionHandler handler) {
     selectBlueSkinImage.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addNextButtonHandler(final DefaultActionHandler handler) {
     nextButton.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void addPrevButtonHandler(final DefaultActionHandler handler) {
     prevButton.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent arg0) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void setNextButtonEnable(boolean enabled) {
     nextButton.setEnabled(enabled);
   }
 
   @Override
   public void setHairColorText(String text) {
     hairColor.setText(text);
   }
 
   @Override
   public void setSkinColorText(String text) {
     skinColor.setText(text);
   }
   
   /**
    * Loads the SelectSkillLvl1Panel.
    * @param entry TODO
    * @param character TODO
    * @param user TODO
    */
   public void loadSelectSkillLvl1Panel(DefaultTimadorusWebApp entry, Character character, User user) {
     RootPanel.get("content").clear();
     RootPanel.get("content").add(SelectSkillLevelDialog.getDialog(entry, character, user).getFormPanel());
   }
 
   /**
    * Loads the SelectSkillNamePanel.
    */
   public void loadSelectNamePanel(DefaultTimadorusWebApp entry, Character character, User user) {
     RootPanel.get("content").clear();
     RootPanel.get("content").add(SelectNameDialog.getDialog(entry, character, user).getFormPanel());
   }
 }
