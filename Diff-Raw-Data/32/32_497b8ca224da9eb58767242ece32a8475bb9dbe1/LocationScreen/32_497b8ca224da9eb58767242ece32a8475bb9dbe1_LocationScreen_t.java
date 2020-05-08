 package com.game.rania.screen;
 
 import com.badlogic.gdx.Gdx;
 import com.game.rania.controller.Controllers;
 import com.game.rania.controller.LocationController;
 import com.game.rania.screen.part.InfoPanel;
 import com.game.rania.screen.part.Parts;
 import com.game.rania.screen.part.SideBar;
 import com.game.rania.screen.part.SkillsPanel;
 
 public class LocationScreen extends LoadableScreen
 {
 
   private LocationController locController = Controllers.locController;
 
   private SideBar            sideBar       = Parts.getSideBar();
   private InfoPanel          infoPanel     = Parts.getInfoPanel();
   private SkillsPanel        skillsPanel   = Parts.getSkillsPanel();
 
   public LocationScreen()
   {
     super();
 
     LoadObject loadObject = new LoadObject(new String(" ..."))
     {
       @Override
       public void load()
       {
         locController.loadTextures();
       }
     };
     addLoadObject(loadObject);
 
     loadObject = new LoadObject(new String(" ..."))
     {
       @Override
       public void load()
       {
         locController.loadDomains();
       }
     };
     addLoadObject(loadObject);
 
     loadObject = new LoadObject(new String(" ..."))
     {
       @Override
       public void load()
       {
         locController.loadItems();
       }
     };
     addLoadObject(loadObject);
 
     loadObject = new LoadObject(new String(" ..."))
     {
       @Override
       public void load()
       {
         if (!locController.loadPlayer())
           Gdx.app.log("Loading...", "Error load player");
       }
     };
     addLoadObject(loadObject);
 
     loadObject = new LoadObject(new String(" ..."))
     {
       @Override
       public void load()
       {
         locController.loadBackground();
       }
     };
     addLoadObject(loadObject);
 
     loadObject = new LoadObject(new String(" ..."))
     {
       @Override
       public void load()
       {
         locController.loadNebulas();
       }
     };
     addLoadObject(loadObject);
 
     loadObject = new LoadObject(new String(" ..."))
     {
       @Override
       public void load()
       {
         locController.loadPlanets();
       }
     };
     addLoadObject(loadObject);
 
     loadObject = new LoadObject(new String("  ..."))
     {
       @Override
       public void load()
       {
         locController.loadRadar();
         infoPanel.loadPart();
         skillsPanel.loadPart();
       }
     };
     addLoadObject(loadObject);
 
     loadObject = new LoadObject(new String(" ."))
     {
       @Override
       public void load()
       {
         locController.loadComplete();
       }
     };
     addLoadObject(loadObject);
   }
 
   @Override
   public void show()
   {
     if (locController.getPlayer() == null || locController.getCurrentLocation() == null)
       return;
 
     locController.addBackground();
     locController.addNebulas();
     locController.addPlanets();
     locController.addRadar();
     locController.addPlayer();
     sideBar.addPart();
     infoPanel.addPart();
     skillsPanel.addPart();
   }
 
   @Override
   public void dispose()
   {
     super.dispose();
     locController.clearObjects();
     infoPanel.unloadPart();
     skillsPanel.unloadPart();
   }
 
   @Override
   public void update(float deltaTime)
   {
     super.update(deltaTime);
     locController.update(deltaTime);
   }
 }
