 package View;
 
 import java.util.ArrayList;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import Control.GameEngine;
 import Model.MainHub;
 import Model.Player;
 import Model.Items.*;
 import Model.Skills.Skill;
 import Model.Skills.Hunter.*;
 import Model.Skills.Warrior.*;
 import Model.Skills.Wizard.*;
 import Model.Timers.AnimationTimer;
 import Model.Timers.RegularTimer;
 
 
 
 public class ShoppingView extends BasicGameState {
 	
 	Player activePlayer;
 	ArrayList<Skill> ownedSkillList;
 	ArrayList<Item> ownedItemList;
 	
 	String buyString = " ";
 	
 	String classtype="Hunter";
 	
 	Image skillDescBg;
 	String costText;
 	
 	private String itemName;
 	
 	boolean showingSkillDescription = false;
 	boolean showingItemDescription = false;
 	private int xPos = 0;
 	private int yPos = 0;
 	
 	Image buyUpgradeButton;
 	
 	private Image playerGold;
 //	private String playerGoldText;
 	private boolean buyOneTime = false;
 	private int grabbedFromChosenIndex = -1;
 	private boolean dragMouse = false;
 	private boolean allIsReady = false;
 	private RegularTimer startCheckTimer = new RegularTimer(5000, 0);
 	private boolean canResetTimer = true;
 	private AnimationTimer countDownAnimation = null;
 	private Image nextRoundBg;
 	
 	private int chosenSkillsXStart = 17;
 	private int chosenSkillsYStart = 280;
 	
 	private String mouse = "No input yet";
 	Image menuTab;
 	
 	Skill[] chosenSkills = new Skill[5];
 	Skill[] allSkills = new Skill[9];
 	Skill basicSkill;
 	Skill selectedSkill = null;
 	
 	Item[] allItems = new Item[3];
 	Item selectedItem = null;
 	
 	Image playButton;
 	Image optionsButton;
 	Image goButton;
 	
 	Image classPortrait;
 
 	String skillText;
 	
 	Image background;
 	Image skillsText;
 	Image shopText;
 	
 	Image lobbyPlayer;
 	Image lobbyPlayerReady;
 	
 //	Image [] LevelofSkills = new Image [9];
 	Image [] LevelofSkills = new Image [5];
 	Player [] LobbyPlayers;
 	
 	Image headSlotItem;
 	Image chestSlotItem;
 	Image weaponSlotItem;
 
 	//UnicodeFont uFont;
 	//Font font;
 	
 	public ShoppingView (int state){
 		
 	}
 	
 	public int getID(){
 		return 4;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)throws SlickException {
 		
 		LevelofSkills[0] = new Image ("res/skillIcons/Level 0.png");
 		LevelofSkills[1] = new Image ("res/skillIcons/Level 1.png");
 		LevelofSkills[2] = new Image ("res/skillIcons/Level 2.png");
 		LevelofSkills[3] = new Image ("res/skillIcons/Level 3.png");
 		LevelofSkills[4] = new Image ("res/skillIcons/Level 4.png");
 		
 		headSlotItem = new Image ("res/Items/Headwear Empty.png");
 		chestSlotItem = new Image ("res/Items/Armor Empty.png");
 		weaponSlotItem = new Image ("res/Items/Weapon Empty.png");
 		
 		lobbyPlayer = new Image ("res/miscImages/LobbyPlayer.png");
 		lobbyPlayerReady = new Image ("res/miscImages/LobbyPlayerReady.png");
 
 		/*
 		for (int i=0;i<MainHub.getController().getPlayerControllers().length;i++){
 			LobbyPlayers[i] = new Image ("res/miscImages/LobbyPlayer.png");
 		}*/
 		
 				
 		background = new Image("res/miscImages/ShoppingviewBackground.png");
 		skillsText = new Image("res/miscImages/skillsText.png");
 		shopText = new Image("res/miscImages/shopText.png");
 
 
 		playerGold = new Image("res/miscImages/PlayerGold.png");
 	//	if(MainHub.getController().getPlayer(MainHub.getController().getActivePlayerIndex()) != null)
 	//		playerGoldText = "" + MainHub.getController().getPlayers()[MainHub.getController().getActivePlayerIndex()].getGold();
 		
 
 		skillText = " ";
 		costText = " ";
 		skillDescBg = new Image("res/miscImages/initEmptyPic.png");
 		
 		
 		
 		playButton = new Image("res/buttons/playButtons.png");
 		buyUpgradeButton = new Image("res/miscImages/initEmptyPic.png");
 		optionsButton = new Image ("res/buttons/options.png");
 		goButton = new Image ("res/buttons/Ready.png");
 	}
 
 	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
 	      // TODO Auto-generated method stub
 	      super.enter(container, game);
 	      
 	      //Waiting for the connection to give the correct playerIndex
 	      int connectionCheck=0;
 	      while(MainHub.getController().getPlayer(MainHub.getController().getActivePlayerIndex()) == null){
 	    	  connectionCheck++;
 	    	  if(connectionCheck >= 100000){
 	    		  System.out.println(connectionCheck);
	    		  container.exit();
 	    		  if(connectionCheck >= 200000){
 	    			  break;
 	    		  }
 	    	  }
 	      }
 	      
 	      activePlayer = MainHub.getController().getPlayer(MainHub.getController().getActivePlayerIndex());
 	      activePlayer.setReady(false);
 	      activePlayer.setHasClickedStartGame(false);
 	      updateSkillLists();
 	      
 	      ownedItemList = activePlayer.getOwnedItems();
 	      classPortrait = activePlayer.getPortraitImage();
 	      LobbyPlayers = MainHub.getController().getPlayers();
 	      
 	      canResetTimer = true;
 	      Image[] countDownImages = new Image[5];
 	      countDownImages[0] = new Image ("res/miscImages/5.png");
 	      countDownImages[1] = new Image ("res/miscImages/4.png");
 	      countDownImages[2] = new Image ("res/miscImages/3.png");
 	      countDownImages[3] = new Image ("res/miscImages/2.png");
 	      countDownImages[4] = new Image ("res/miscImages/1.png");
 	      countDownAnimation = new AnimationTimer(5000, countDownImages);
 	      nextRoundBg = new Image("res/miscImages/skillDescBg.png");
 	      
 	      
 	      switch(MainHub.getController().getPlayers()[MainHub.getController().getActivePlayerIndex()].getType()){
 			case "Wizard":
 				//Init wizard basic, offensive, defensive and mobility skillists
 				
 				basicSkill = new SkillWandattack();
 				
 				allSkills[0] = new SkillFireball();
 				allSkills[1] = new SkillIroncloak();
 				allSkills[2] = new SkillUnstableMagic();
 				allSkills[3] = new SkillFirestorm();
 				allSkills[4] = new SkillAbsorb();
 				allSkills[5] = new SkillBlizzard();
 				allSkills[6] = new SkillFlamewave();
 				allSkills[7] = new SkillIceblock();
 				allSkills[8] = new SkillTeleport();
 				
 				allItems[0] = new ItemWizardHat();
 				allItems[1] = new ItemWizardRobe();
 				allItems[2] = new ItemWizardStaff();
 				break;
 			case "Hunter":
 				//Init Hunter basic, offensive, defensive and mobility skillists
 				
 				basicSkill = new SkillArrow();
 				
 				allSkills[0] = new SkillFlamingArrow();
 				allSkills[1] = new SkillPassiveDodge();
 				allSkills[2] = new SkillSprint();
 				allSkills[3] = new SkillGuidedArrow();
 				allSkills[4] = new SkillLifestealingArrows();
 				allSkills[5] = new SkillCripplingTrap();
 				allSkills[6] = new SkillArrowFlurry();
 				allSkills[7] = new SkillStealth();
 				allSkills[8] = new SkillBarrelRoll();
 				
 				allItems[0] = new ItemHunterCap();
 				allItems[1] = new ItemHunterArmor();
 				allItems[2] = new ItemHunterBow();
 				break;
 			case "Warrior":
 				//Init warrior basic, offensive, defensive and mobility skillists
 				
 				basicSkill = new SkillSlash();
 
 				allSkills[0] = new SkillThrowingAxe();
 				allSkills[1] = new SkillImprovedArmor();
 				allSkills[2] = new SkillIncreasedMovement();
 				allSkills[3] = new SkillWarstomp();
 				allSkills[4] = new SkillShieldStance();
 				allSkills[5] = new SkillGrapplingHook();
 				allSkills[6] = new SkillAdrenaline();
 				allSkills[7] = new SkillFirstAid();
 				allSkills[8] = new SkillLeapAttack();
 				
 				allItems[0] = new ItemWarriorHelmet();
 				allItems[1] = new ItemWarriorArmor();
 				allItems[2] = new ItemWarriorSword();
 				break;
 	      	}
 	/*      for(int i=0;i<9;i++){
 				Skill skill = findOwnedSkill(allSkills[i].getName());
 				if (skill != null){
 					switch (allSkills[i].getCurrentLvl()){	
 						case 1:
 							LevelofSkills [i] = new Image ("res/skillIcons/Level 1.png");
 							break;
 						case 2:
 							LevelofSkills [i] = new Image ("res/skillIcons/Level 2.png");
 							break;
 						case 3:
 							LevelofSkills [i] = new Image ("res/skillIcons/Level 3.png");
 							break;
 						case 4:
 							LevelofSkills [i] = new Image ("res/skillIcons/Level 4.png");
 							break;
 					}
 				}
 			}*/
 
       		activePlayer.setIndex(MainHub.getController().getActivePlayerIndex());
       		MainHub.getController().addPlayer(activePlayer, MainHub.getController().getActivePlayerIndex());
       		MainHub.getController().getSocketClient().changePlayer(activePlayer);
       		activePlayer.setMode("lobby");
 			
 	   }
 	
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)throws SlickException {
 		//g.setFont(uFont);
 		
 		g.drawImage(background, 0, 0);
 		g.drawImage(playButton, 1120, 670);
 		g.drawImage(optionsButton,980,670);
 		g.setColor(Color.white);
 		if (showingSkillDescription||showingItemDescription){
 			g.drawImage(skillDescBg, 485, 460);
 			g.drawImage(buyUpgradeButton, 710, 610);
 			if (showingItemDescription){
 				g.drawImage(selectedItem.getImage(), 490, 490);
 				g.drawString(costText, 490, 625);
 				g.drawString(skillText, 580, 490);
 				g.drawString(itemName, 490, 470);
 			}else{
 				g.drawImage(selectedSkill.getSkillBarImage(), 490, 470);
 				g.drawString(costText, 490, 625);
 				g.drawString(skillText, 560, 475);
 			}
 		}
 		
 		
 		/*
 		if(selectedSkill != null)
 			g.drawImage(selectedSkill.getSkillBarImage(), 490, 470);
 		*/
 		
 		g.drawString(mouse, 900, 10);
 		
 		g.drawImage(classPortrait, 70, 30);
 		g.drawImage(playerGold,70,185);
 		g.drawString(""+activePlayer.getGold(), 140, 198);
 		g.drawString(activePlayer.getName() + "\nHP: "+activePlayer.getHP() + "\nArmor: " + (int)(activePlayer.getArmor()*100) 
 				+ "%\nKills: " + activePlayer.getTotalKills() , 80 + classPortrait.getWidth(), 20 + classPortrait.getHeight()/2);
 
 
 		for(int j=0; j<chosenSkills.length; j++){
 			g.drawString("" + (j+1), chosenSkillsXStart+30 + 69*j, chosenSkillsYStart-25);
 			if(chosenSkills[j] != null)
 				g.drawImage(chosenSkills[j].getSkillBarImage(), chosenSkillsXStart + 69*j, chosenSkillsYStart);
 		}
 		
 		
 		// Draw out the itemslots
 		
 		g.drawImage(headSlotItem,365,10 );
 		g.drawImage(chestSlotItem,365,100 );
 		g.drawImage(weaponSlotItem,365,190);
 		
 		//Draws out owned items
 		if(ownedItemList.size()!=0){
 			for (int i=0;i<ownedItemList.size();i++){
 				switch (ownedItemList.get(i).getItemSlot()){
 				case "Headwear" :
 					g.drawImage(ownedItemList.get(i).getImage(),365,10);
 					break;
 				case "Armor":
 					g.drawImage(ownedItemList.get(i).getImage(),365,100);
 					break;
 				case "Weapon":
 					g.drawImage(ownedItemList.get(i).getImage(),365,190);
 					break;
 				}
 			}
 		}
 		
 		// Draws out players in lobby
 		for (int i=0; i<LobbyPlayers.length;i++){
 			if (LobbyPlayers[i] != null){
 				g.drawString(MainHub.getController().getPlayer(i).getName(), 910, 120+40*i);
 				g.drawString(MainHub.getController().getPlayer(i).getTotalKills() + "/" + MainHub.getController().getPlayer(i).getDeaths(), 1075, 120+40*i);
 				g.drawImage(lobbyPlayer,897,400+60*i);
 			//	System.out.println(LobbyPlayers[i].getName() + " " + LobbyPlayers[i].isReady());
 				if (LobbyPlayers[i].isReady()){
 					g.drawImage(lobbyPlayerReady,897,400+60*i);
 				}
 				g.drawImage(MainHub.getController().getPlayer(i).getPortraitImageMini(),920,405+60*i);
 				g.drawString(MainHub.getController().getPlayer(i).getName(), 980, 420+60*i);
 			}
 					
 		}
 		
 		for(int i=0; i<9; i++){
 			int row=0;
 			if(i>=3){
 				row++;
 				if(i>=6){
 					row++;
 				}
 			}
 			int col=0;
 			if(i%3 == 1){
 				col=140;
 			}else if(i%3 == 2){
 				col=275;
 			}
 			Skill currentSkill = findOwnedSkill(allSkills[i].getName());
 			
 			g.drawImage(currentSkill != null ? allSkills[i].getRegularSkillBarImage() : allSkills[i].getDisabledSkillBarImage(), 60+col, 440+75*row);
 			g.drawImage(currentSkill != null ? LevelofSkills[currentSkill.getCurrentLvl()] : LevelofSkills[0],114+col,494+75*row);
 			
 		}
 		g.drawString(buyString, 500, 675);
 		
		if(countDownAnimation.getCurrentAnimationImage() != null && LobbyPlayers[0].hasClickedStartGame()){
 			Image currentCount = countDownAnimation.getCurrentAnimationImage();
 			g.drawImage(nextRoundBg, GameEngine.screenWidth/2 - nextRoundBg.getWidth()/2, 200);
 			g.drawImage(currentCount,GameEngine.screenWidth/2-currentCount.getWidth()/2,GameEngine.screenHeight/2-currentCount.getHeight()/2-nextRoundBg.getHeight()/3);
 		}
 		
 		if(dragMouse){
 			g.drawImage(selectedSkill.getSkillBarImage(), xPos, yPos);
 		}
 		//Draw out the items
 		for (int i=0;i<allItems.length;i++){
 			g.drawImage(allItems[i].getImage(),475,80+145*i);
 		}
 	}
 
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)throws SlickException {
 		
 		xPos = Mouse.getX();
 		yPos = 720 - Mouse.getY();
 		
 		//activePlayer.setGold(5000);
 		
 		
 		
 		mouse = "Mouse position: (" + xPos + "," + yPos + ")";
 		
 		Input input = gc.getInput();
 		
 		buyOneTime = true;
 		
 		
 		
 		if(showingSkillDescription||showingItemDescription){
 			skillDescBg = new Image("res/miscImages/skillDescBg.png");
 			if((710<xPos && xPos<830) && (600<yPos && yPos<645)){
 				buyUpgradeButton = new Image("res/buttons/buyOver.png");
 				if(input.isMousePressed(0) && buyOneTime){
 					buyOneTime = false;
 					if(showingSkillDescription){
 						if(activePlayer.getGold()>=selectedSkill.getCost()){					
 							buySkill(selectedSkill);
 						}
 					}else{
 						if(activePlayer.getGold()>=selectedItem.getCost()){					
 							buyItem(selectedItem);
 						}
 					}
 					
 				}
 			}else{
 				buyUpgradeButton = new Image("res/buttons/buy.png");
 			}
 		}
 		
 		allIsReady = true;
 		for(int i=0; i<LobbyPlayers.length; i++){
 			if(LobbyPlayers[i] != null && !LobbyPlayers[i].isReady()){
 				allIsReady = false;
 			}
 		}
 
 		if((1120<xPos && xPos<1240) && (670<yPos && yPos<715)){
 			if (!activePlayer.isReady()){
 				playButton = new Image("res/buttons/ReadyOver.png");
 			}else if (allIsReady && activePlayer == LobbyPlayers[0] && !activePlayer.hasClickedStartGame()){
 				playButton = new Image("res/buttons/GoButtonOver.png");
 			}else {
 				playButton = new Image ("res/buttons/UnreadyOver.png");
 			}
 			if(input.isMousePressed(0)){
 				//Checking state the button 
 				if(allIsReady && activePlayer == LobbyPlayers[0] && !activePlayer.hasClickedStartGame()){
 					activePlayer.setHasClickedStartGame(true);
 				}else if(activePlayer.isReady()){
 					activePlayer.setReady(false);
 				}else{
 					activePlayer.setReady(true);
 				}
 								
 			}			
 		}else if(!activePlayer.isReady()){
 			playButton = new Image("res/buttons/Ready.png");
 		}else if (allIsReady && activePlayer == LobbyPlayers[0] && !activePlayer.hasClickedStartGame()){
 			playButton = new Image("res/buttons/GoButton.png");
 		}else{
 			playButton = new Image ("res/buttons/Unready.png");
 		}
		if(LobbyPlayers[0].hasClickedStartGame() && canResetTimer){
 			canResetTimer = false;
 			startCheckTimer.resetTimer();
 			countDownAnimation.resetCounterAndTimer();
 		}
		if(LobbyPlayers[0].hasClickedStartGame() && !allIsReady){
 			activePlayer.setHasClickedStartGame(false);
 			canResetTimer = true;
 			startCheckTimer.stopTimer();
 		}
		if(LobbyPlayers[0].hasClickedStartGame() && startCheckTimer.checkTimer() == startCheckTimer.getInterval()){
 			pressedReadyOrGo(sbg);
 		}
 		
 		if((980<xPos && xPos<1100) && (670<yPos && yPos<715)){
 			optionsButton = new Image("res/buttons/OptionsOver.png");
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				optionsButton = new Image("res/buttons/Options.png");
 				sbg.enterState(5);
 			}
 			
 		}else{
 			optionsButton = new Image("res/buttons/Options.png");
 		}
 		//Checking if mouse is pressed down
 		if(input.isMousePressed(0) && !dragMouse){
 			int chosenIndex;
 			System.out.println("Tomas");
 			int xAllIndex = -1;
 			int yAllIndex = -1;
 			int xItemIndex = -1;
 			int yItemIndex = -1;
 			int totalIndex = -1;
 			
 			if(475<=xPos && xPos<=555){
 				xItemIndex = 1;
 			}/*else if(565<=xPos && xPos<=645){
 				xItemIndex = 2;
 			}else if(655<=xPos && xPos<=735){
 				xItemIndex = 3;
 			}If we want to add more Items */
 				
 			if(yPos >= 80 && yPos <= 160){
 				yItemIndex = 0;
 			}else if(yPos >= 225 && yPos <= 305){
 				yItemIndex = 1;
 			}else if(yPos >= 370 && yPos <= 450){
 				yItemIndex = 2;
 			}
 			if(60<=xPos && xPos<=124){
 				xAllIndex = 0;
 			}else if(200<=xPos && xPos<=264){
 				xAllIndex = 1;
 			}else if(335<=xPos && xPos<=399){
 				xAllIndex = 2;
 			}
 				
 			if(yPos >= 440 && yPos <= 504){
 				yAllIndex = 0;
 			}else if(yPos >= 515 && yPos <= 579){
 				yAllIndex = 1;
 			}else if(yPos >= 590 && yPos <= 654){
 				yAllIndex = 2;
 			}
 				
 			if(xAllIndex != -1 && yAllIndex != -1){
 				totalIndex = xAllIndex + 3*yAllIndex;
 			}else if (xItemIndex != -1 && yItemIndex != -1){
 				totalIndex = xItemIndex * yItemIndex; /* + xItemIndex; Stays if we want to add more Items */ 
 				setSelectedItem(allItems[totalIndex]);
 			}
 				
 			if(totalIndex >= 0 && xAllIndex != -1 && yAllIndex != -1){		
 				Skill newChosenSkill = findOwnedSkill(allSkills[totalIndex].getName());
 				if(newChosenSkill != null){
 					setSelectedSkill(newChosenSkill);
 					if (!allSkills[totalIndex].isPassive()){
 						dragMouse=true;	
 						grabbedFromChosenIndex = -1;
 					}
 					}else{
 						setSelectedSkill(allSkills[totalIndex]);
 						dragMouse=false;
 					}
 					
 				
 			}else if((chosenIndex = getChosenSkillIndex()) > 0){
 				if(chosenSkills[chosenIndex] != null){
 					setSelectedSkill(chosenSkills[chosenIndex]);
 					dragMouse = true;
 					grabbedFromChosenIndex = chosenIndex;
 				}
 			}
 			
 			
 			
 			
 		}
 
 		//Checking if mouse is released
 		if(dragMouse && !input.isMouseButtonDown(0)){
 			int xIndex = getChosenSkillIndex();
 			if(xIndex == 0){
 				buyString = "You can not change that skill";
 			}else if(xIndex >= 1){
 				boolean alreadyInChosenSkills = false;
 				for(int i=0; i<chosenSkills.length; i++){
 					if(chosenSkills[i] != null && chosenSkills[i].getName() == selectedSkill.getName())
 						alreadyInChosenSkills = true;
 				}
 				if(alreadyInChosenSkills){
 					if(grabbedFromChosenIndex != -1){
 						Skill tempSkill = chosenSkills[xIndex];
 						activePlayer.setSkill(selectedSkill, xIndex);
 						activePlayer.setSkill(tempSkill, grabbedFromChosenIndex);
 					}else{
 						buyString = "Already got that skill in your skillbar";
 					}
 				}else{
 					activePlayer.setSkill(selectedSkill, xIndex);
 					updateSkillLists();
 				}
 			}else{
 				if(grabbedFromChosenIndex != -1){
 					activePlayer.setSkill(null, grabbedFromChosenIndex);
 				}
 			}
 			grabbedFromChosenIndex = -1;
 			dragMouse = false;
 		}
 	}
 	
 
 	/**
 	 * Returns the index in chosen skills that the mouse is above
 	 * @return the index of chosen skills and -1 if mouse is not over
 	 */
 	private int getChosenSkillIndex(){
 		
 		if(xPos >= chosenSkillsXStart && xPos <= 5*69+chosenSkillsXStart && yPos >= chosenSkillsYStart && yPos <= chosenSkillsYStart+64){
 			int xRange = xPos - chosenSkillsXStart;
 			int xIndex = -1;
 			while(xRange > 0){
 				xIndex++;
 				xRange -= 69;
 			}
 			return xIndex;
 		}
 		return -1;
 	}
 	
 	private void updateSkillLists(){
 		chosenSkills = activePlayer.getSkillList();
 		ownedSkillList = activePlayer.getOwnedSkills();
 	}
 	private void buyItem(Item item){
 		boolean alreadyOwnItem = false;
 		for (int i=0; i<ownedItemList.size();i++){
 			if(ownedItemList.get(i).getName()==item.getName()){
 				buyString = "Already own Item";
 				alreadyOwnItem = true;
 				break;
 			}
 		}
 		if(!alreadyOwnItem){
 			buyString = "Succesfully bought the item for " + item.getCost() + "!";
 			activePlayer.addGold(-item.getCost());
 			activePlayer.addItemOwned(item);
 			activePlayer.addPassiveItem(item);
 		}
 	}
 	
 	private void buySkill(Skill skill){
 		boolean alreadyOwnSkill = false;
 		for(int i=0; i<ownedSkillList.size(); i++){
 			if(ownedSkillList.get(i).getName() == skill.getName()){
 				buyString = "Already own skill";
 				alreadyOwnSkill = true;
 				break;
 			}
 		}
 		if(!alreadyOwnSkill){
 			buyString = "Succesfully bought a skill for " + skill.getCost() + "!";
 			activePlayer.addGold(-skill.getCost());
 			activePlayer.addSkillAsOwned(skill);
 			if(skill.isPassive()){
 				activePlayer.addPassiveSkill(skill);
 			}
 		}else{
 			Skill ownedSkill = findOwnedSkill(skill.getName());
 			if(ownedSkill.getCurrentLvl() < 4){
 				buyString = "Succesfully upgraded a skill for " + skill.getCost() + "!";
 				activePlayer.addGold(-skill.getCost());
 				if(skill.isPassive()){
 					activePlayer.removePassiveSkill(ownedSkill);
 					ownedSkill.upgradeSkill();
 					activePlayer.addPassiveSkill(ownedSkill);
 				}else{
 					ownedSkill.upgradeSkill();
 				}
 				
 				
 			}else{
 				buyString = "Skill already max level!";
 			}
 			
 		}
 		updateSkillLists();
 		updateSkillInformation();
 	}
 	
 	private Skill findOwnedSkill(String skillName){
 		for(int i=0; i<ownedSkillList.size(); i++){
 			if(ownedSkillList.get(i).getName() == skillName){
 				return ownedSkillList.get(i);
 			}
 		}
 		
 		return null;
 	}
 	
 	private void setSelectedSkill(Skill skill){
 		skillText = "Level " + skill.getCurrentLvl() + " " + skill.getDescription();
 		costText = "Cost : " + skill.getCost();
 		selectedSkill = skill;
 		showingItemDescription = false;
 		showingSkillDescription = true;
 	}
 	private void setSelectedItem(Item item){
 		System.out.println("kom in");
 		itemName = item.getName();
 		skillText = item.getDescription() ;
 		costText ="Cost : " + item.getCost();
 		selectedItem = item;
 		showingItemDescription = true;
 		showingSkillDescription =false;
 	}
 	
 	private void updateSkillInformation(){
 		setSelectedSkill(selectedSkill);
 	}
 	
 	private void pressedReadyOrGo(StateBasedGame sbg){
 		if(MainHub.getController().isMulti()){
 			activePlayer.setMode("arena");
 			sbg.enterState(1);
 		}else{
 			sbg.enterState(1);
 		}
 		
 		
 	}
 }
