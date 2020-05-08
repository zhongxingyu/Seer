 package com.game.rania.screen.part;
 
 import com.badlogic.gdx.graphics.Color;
 import com.game.rania.RaniaGame;
 import com.game.rania.controller.Controllers;
 import com.game.rania.model.Font;
 import com.game.rania.model.Group;
 import com.game.rania.model.Object;
 import com.game.rania.model.Object.Align;
 import com.game.rania.model.RegionID;
 import com.game.rania.model.element.Planet;
 import com.game.rania.model.element.Player;
 import com.game.rania.model.element.Text;
 import com.game.rania.model.ui.PressedButton;
 import com.game.rania.model.ui.Slider;
 import com.game.rania.model.ui.SliderAction;
 import com.game.rania.model.ui.TouchAction;
 import com.game.rania.view.MainView;
 
 public class PlanetPanel extends Group implements Part
 {
   private MainView         mView         = RaniaGame.mView;
   private Planet           planet        = null;
 
   private static final int mainMenu      = 0;
   private static final int npcMenu       = 1;
   private static final int tradeMenu     = 2;
   private static final int scienceMenu   = 3;
   private static final int technicMenu   = 4;
   private static final int lifeMenu      = 5;
 
   private int              menuStatus    = mainMenu;
 
   private Group            mainGroup     = new Group();
   private PressedButton    npcButton     = null;
   private PressedButton    tradeButton   = null;
   private PressedButton    scienceButton = null;
   private PressedButton    technicButton = null;
   private PressedButton    lifeButton    = null;
 
   private Group            exitGroup     = new Group();
   private PressedButton    exitButton    = null;
 
   private Group            npcGroup      = new Group();
 
   private Group            tradeGroup    = new Group();
 
   private Group            scienceGroup  = new Group();
 
   private Group            technicGroup  = new Group();
   private Slider           energySlider  = null;
   private Text             minText       = null;
   private Text             maxText       = null;
   private Text             valueText     = null;
   private PressedButton    energyButton  = null;
   private PressedButton    repairButton  = null;
 
   private Group            lifeGroup     = new Group();
 
   public PlanetPanel()
   {
   }
 
   @Override
   public void unloadPart()
   {
     removePart();
     clear();
   }
 
   public void setPlanet(Planet planet)
   {
     this.planet = planet;
   }
 
   @Override
   public void loadPart()
   {
     final Player player = Controllers.locController.getPlayer();
     float halfWidth = mView.getHUDCamera().getWidth() * 0.5f;
     float halfHeight = mView.getHUDCamera().getHeight() * 0.5f;
 
     mView.loadTexture("data/gui/planet_services.png", RegionID.PLANET_PANEL);
 
     mView.loadTexture("data/gui/planet_icons.png", RegionID.PLANET_NPC, 0, 0, 96, 96);
     mView.loadTexture("data/gui/planet_icons.png", RegionID.PLANET_TRADE, 96, 0, 96, 96);
     mView.loadTexture("data/gui/planet_icons.png", RegionID.PLANET_SCIENCE, 192, 0, 96, 96);
    mView.loadTexture("data/gui/planet_icons.png", RegionID.PLANET_SERVICE, 288, 0, 96, 96);
    mView.loadTexture("data/gui/planet_icons.png", RegionID.PLANET_OTHER, 384, 0, 96, 96);
     mView.loadTexture("data/gui/planet_icons.png", RegionID.PLANET_EXIT, 480, 0, 96, 96);
 
     npcButton = new PressedButton(RegionID.PLANET_PANEL,
                                   null,
                                   -halfWidth * 0.375f, 0,
                                   new TouchAction()
                                   {
                                     @Override
                                     public void execute(boolean touch)
                                     {
                                       setVisible(npcGroup);
                                       menuStatus = npcMenu;
                                     }
                                   });
     npcButton.angle.value = 270.0f;
 
     tradeButton = new PressedButton(RegionID.PLANET_PANEL,
                                     null,
                                     -halfWidth * 0.275f, halfHeight * 0.5f,
                                     new TouchAction()
                                     {
                                       @Override
                                       public void execute(boolean touch)
                                       {
                                         setVisible(tradeGroup);
                                         menuStatus = tradeMenu;
                                       }
                                     });
     tradeButton.angle.value = 225.0f;
 
     scienceButton = new PressedButton(RegionID.PLANET_PANEL,
                                       null,
                                       0, halfHeight * 0.7f,
                                       new TouchAction()
                                       {
                                         @Override
                                         public void execute(boolean touch)
                                         {
                                           setVisible(scienceGroup);
                                           menuStatus = scienceMenu;
                                         }
                                       });
     scienceButton.angle.value = 180.0f;
 
     technicButton = new PressedButton(RegionID.PLANET_PANEL,
                                       null,
                                       halfWidth * 0.275f, halfHeight * 0.5f,
                                       new TouchAction()
                                       {
                                         @Override
                                         public void execute(boolean touch)
                                         {
                                           energySlider.updateValue((int) player.energy);
                                           setVisible(technicGroup);
                                           menuStatus = technicMenu;
                                         }
                                       });
     technicButton.angle.value = 135.0f;
 
     lifeButton = new PressedButton(RegionID.PLANET_PANEL,
                                    null,
                                    halfWidth * 0.375f, 0,
                                    new TouchAction()
                                    {
                                      @Override
                                      public void execute(boolean touch)
                                      {
                                        setVisible(lifeGroup);
                                        menuStatus = lifeMenu;
                                      }
                                    });
     lifeButton.angle.value = 90.0f;
 
     exitButton = new PressedButton(RegionID.PLANET_PANEL,
                                    null,
                                    0, -halfHeight * 0.65f,
                                    new TouchAction()
                                    {
                                      @Override
                                      public void execute(boolean touch)
                                      {
                                        switch (menuStatus)
                                        {
                                          case mainMenu:
                                            Controllers.netController.sendOutPlanet();
                                            Controllers.locController.getPlayer().position.set(planet.position);
                                            Parts.showPlanetPanel(false);
                                            Controllers.locController.getRadar().visible = true;
                                            Controllers.locController.getPlayer().planet = 0;
                                            Controllers.locController.enablePlayerController(true);
                                            RaniaGame.mView.getCamera().zoom = 1.0f;
                                            return;
                                          case npcMenu:
                                          case tradeMenu:
                                          case scienceMenu:
                                          case technicMenu:
                                          case lifeMenu:
                                            setVisible(mainGroup);
                                            menuStatus = mainMenu;
                                            return;
                                        }
                                      }
                                    });
 
     mainGroup.addElement(npcButton);
     mainGroup.addElement(new Object(RegionID.PLANET_NPC, -halfWidth * 0.375f, 0, 0, 2, 2));
     mainGroup.addElement(tradeButton);
     mainGroup.addElement(new Object(RegionID.PLANET_TRADE, -halfWidth * 0.275f, halfHeight * 0.5f, 0, 2, 2));
     mainGroup.addElement(scienceButton);
     mainGroup.addElement(new Object(RegionID.PLANET_SCIENCE, 0, halfHeight * 0.7f, 0, 2, 2));
     mainGroup.addElement(technicButton);
    mainGroup.addElement(new Object(RegionID.PLANET_SERVICE, halfWidth * 0.275f, halfHeight * 0.5f, 0, 2, 2));
     mainGroup.addElement(lifeButton);
    mainGroup.addElement(new Object(RegionID.PLANET_OTHER, halfWidth * 0.375f, 0, 0, 2, 2));
     addElement(mainGroup);
 
     exitGroup.addElement(exitButton);
     exitGroup.addElement(new Object(RegionID.PLANET_EXIT, 0, -halfHeight * 0.65f, 0, 2, 2));
     addElement(exitGroup);
 
     energySlider = new Slider(0, halfHeight * 0.7f, new Color(1, 1, 1, 1), true, new SliderAction()
     {
       @Override
       public void execute(Slider slider)
       {
         valueText.content = String.valueOf(slider.value);
       }
     });
 
     energySlider.min = (int) 0;
     energySlider.max = (int) player.maxEnergy;
     energySlider.value = (int) player.energy;
 
     minText = new Text(String.valueOf(energySlider.min), Font.getFont("data/fonts/Arial.ttf", 30), new Color(1, 1, 1, 1), -halfWidth * 0.3f, halfHeight * 0.7f, Align.RIGHT, Align.CENTER);
     maxText = new Text(String.valueOf(energySlider.max), Font.getFont("data/fonts/Arial.ttf", 30), new Color(1, 1, 1, 1), halfWidth * 0.3f, halfHeight * 0.7f, Align.LEFT, Align.CENTER);
     valueText = new Text(String.valueOf(energySlider.value), Font.getFont("data/fonts/Arial.ttf", 30), new Color(1, 1, 1, 1), 0, halfHeight * 0.75f);
 
     energyButton = new PressedButton(RegionID.BTN_UI_MENU_OFF,
                                      RegionID.BTN_UI_MENU_ON,
                                      halfWidth * 0.8f, halfHeight * 0.7f,
                                      new TouchAction()
                                      {
                                        @Override
                                        public void execute(boolean touch)
                                        {
                                          Controllers.netController.sendRecharge();
                                          energySlider.updateValue(energySlider.max);
                                        }
                                      });
 
     repairButton = new PressedButton(RegionID.REPAIR_KIT_SKILL,
                                      null,
                                      halfWidth * 0.8f, halfHeight * 0.5f,
                                      new TouchAction()
                                      {
                                        @Override
                                        public void execute(boolean touch)
                                        {
                                          Controllers.netController.sendRepair();
                                        }
                                      });
 
     technicGroup.addElement(minText);
     technicGroup.addElement(maxText);
     technicGroup.addElement(valueText);
     technicGroup.addElement(energySlider);
     technicGroup.addElement(energyButton);
     technicGroup.addElement(repairButton);
     addElement(technicGroup);
   }
 
   @Override
   public void addPart()
   {
     RaniaGame.mController.addHUDObject(this);
   }
 
   @Override
   public void setVisible(Group group)
   {
     super.setVisible(group);
     exitGroup.setVisible(true);
   }
 
   @Override
   public void setVisible(boolean visible)
   {
     if (!visible)
     {
       super.setVisible(visible);
       return;
     }
 
     mainGroup.setVisible(true);
     exitGroup.setVisible(true);
   }
 
   @Override
   public void removePart()
   {
     RaniaGame.mController.removeHUDObject(this);
   }
 }
