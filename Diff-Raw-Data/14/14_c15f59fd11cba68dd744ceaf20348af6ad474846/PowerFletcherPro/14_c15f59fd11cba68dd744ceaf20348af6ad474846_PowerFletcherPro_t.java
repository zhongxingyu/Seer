 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.imageio.ImageIO;
 
 import org.rsbot.event.events.MessageEvent;
 import org.rsbot.event.listeners.MessageListener;
 import org.rsbot.event.listeners.PaintListener;
 import org.rsbot.script.Script;
 import org.rsbot.script.ScriptManifest;
 import org.rsbot.script.methods.Equipment;
 import org.rsbot.script.methods.Game;
 import org.rsbot.script.methods.Magic;
 import org.rsbot.script.methods.Skills;
 import org.rsbot.script.wrappers.RSComponent;
 import org.rsbot.script.wrappers.RSItem;
 import org.rsbot.script.wrappers.RSObject;
 
 
 @ScriptManifest(authors = {"Ramy"},
 version = 1.0,
 description = "Chop tree Flect and alch it.",
 keywords = {"Pro", "Power","Fletcher","Woodcutting","Magic","Tree","Bow"},
 name = "PowerFletcherPro")
 public class PowerFletcherPro extends Script implements PaintListener, MessageListener, MouseListener{
 
 	Bow bow = null;
 	Alch alch = null;
 	Tabs tab = Tabs.Fletching;
 	protected FlecterGUI FlecterGui;
 	public enum Bow{
 		Arrowshafts(new int[] { 1278, 1276, 38760, 38783, 38787, 38785 },1511,52,4,7,905,14),
 		ShortBow(new int[] { 1278, 1276, 38760, 38783, 38787, 38785 },1511,841,9,15,905,15), 
 		LongBow(new int[] { 1278, 1276, 38760, 38783, 38787, 38785 },1511,48,24,36,905,16), 
 		OakShortBow(new int[] { 1281, 38732 },1521,54,20,30,905,14), 
 		OakLongBow(new int[] { 1281, 38732 },1521,56,32,48,905,15), 
 		WillowShortBow(new int[] { 5551, 5552, 5553, 1308, 8481, 8482, 8483, 8484, 8485, 8486, 8487, 8488, 38627, 38616, 38627, 2210, 142, 2372, 139 },1519,60,40,60,905,14), 
 		WillowLongBow(new int[] { 5551, 5552, 5553, 1308, 8481, 8482, 8483, 8484, 8485, 8486, 8487, 8488, 38627, 38616, 38627, 2210, 142, 2372, 139 },1519,58,64,96,905,15), 
 		MapleShortBow(new int[] { 1307, 51843 },1517,64,80,120,905,14), 
 		MapleLongBow(new int[] { 1307, 51843 },1517,62,128,192,905,15), 
 		YewShortBow(new int[] { 1309, 38755, 40411, 38755, 38782, 38755 },1515,68,160,240,905,14), 
 		YewLongBow(new int[] { 1309, 38755, 40411, 38755, 38782, 38755 },1515,66,256,384,905,15), 
 		MagicShortBow(new int[] { 1306 },1513,72,320,480,905,14), 
 		MagicLongBow(new int[] { 1306 },1513,70,512,768,905,15); 
 		
 		int BowID = 0;
 		int LowAlchPrice = 0;
 		int HightAlchPrice = 0;
 		int InterfaceID = 0;
 		int ComponetID = 0;
 		int LogID = 0;
 		int[] TreeID = {};
 		
 		Bow(int[] TreeID, int LogID,int BowID, int LowAlchPrice , int HightAlchPrice, int InterfaceID , int ComponetID){
 			this.TreeID = TreeID;
 			this.LogID = LogID;
 			this.BowID = BowID;
 			this.HightAlchPrice = HightAlchPrice;
 			this.LowAlchPrice = LowAlchPrice;
 			this.InterfaceID = InterfaceID;
 			this.ComponetID = ComponetID;
 		}
 		public int[] getTreeID(){
 			return TreeID;
 		}
 		public int getLogID(){
 			return LogID;
 		}
 		public int getBowID(){
 			return BowID;
 		}
 		public int getInterfaceID(){
 			return InterfaceID;
 		}
 		public int getComponetID(){
 			return ComponetID;
 		}
 		public int getLowAlchPrice(){
 			return LowAlchPrice;
 		}
 		public int getHightAlchPrice(){
 			return HightAlchPrice;
 		}
 		public int[] getHatchetsID(){
 			return new int[] { 1349, 1351, 1353, 1355, 1357, 1359, 1361, 6739,
 		            13470 };
 		}
 	}
 	
 	//XP
 	int MagicStartXP = 0;
 	int MagicGainedXP = 0;
 	int MagicXPPH = 0;
 	int MagicXPTNL = 0;
 	int WoodcuttingStartXP = 0;
 	int WoodcuttingGainedXP = 0;
 	int WoodcuttingXPPH = 0;
 	int WoodcuttingXPTNL = 0;
 	int FletchingStartXP = 0;
 	int FletchingGainedXP = 0;
 	int FletchingXPPH = 0;
 	int FletchingXPTNL = 0;
 	
 	int cc = 0;
 	
 	//Percent
 	int MagicNLP = 0;
 	int FletchingNLP = 0;
 	int WoodcuttingNLP = 0;
 	
 	//Bows
 	int BowsMade = 0;
 	int BowsPH = 0;
 	
 	//Alch
 	int Alched = 0;
 	int AlchesPH = 0;
 	
 	//Logs
 	int Logschoped = 0;
 	int LogsPH = 0;
 	
 	//Level
 	int MagicStartLevel = 0;
 	int MagicLevelsGained = 0;
 	int MagicCurrentLevel = 0;
 	int WoodcuttingStartLevel = 0;
 	int WoodcuttingLevelsGained = 0;
 	int WoodcuttingCurrentLevel = 0;
 	int FletchingStartLevel = 0;
 	int FletchingLevelsGained = 0;
 	int FletchingCurrentLevel = 0;
 	
 	//Profit
 	int Profit = 0;
 	int ProfitPH = 0;
 	int ProfitPerAlch = 0;
 	int NatureRunePrice = 0;
 	int BowPrice = 0;
 	
 	//RunTime
 	long startTime = 0;
 	long millis = 0;
 	long hours = 0;
 	long minutes = 0;
 	long seconds = 0;
 	
 	public enum Alch{
 		High , Low , Drop
 	}
 	public enum Tabs{
 		Magic , Profit , Fletching , Woodcutting
 	}
 	public enum AntiBan{
 		moveRandomly , HoverSkill , Tabs , Mouse
 	}
 	boolean GUILoaded = false;
 	boolean ChopVaild = false;
 	boolean ClayKnife = false;
 	final int ClayKnifeID = 14111;
 	final int NatureRune = 561;
 	final int FireRune = 554;
 	final int FireStaff = 1387;
 	int KnifeID = 946;
 	
 	public enum Status{
 		Chop, Alch, Flect, Drop
 	}
 	
 	public boolean onStart(){
 		NatureRunePrice = grandExchange.lookup(NatureRune).getGuidePrice();
 		MagicStartLevel = skills.getCurrentLevel(Skills.MAGIC);
 		FletchingStartLevel = skills.getCurrentLevel(Skills.FLETCHING);
 		WoodcuttingStartLevel = skills.getCurrentLevel(Skills.WOODCUTTING);
 		WoodcuttingStartXP = skills.getCurrentExp(Skills.WOODCUTTING);
 		FletchingStartXP = skills.getCurrentExp(Skills.FLETCHING);
 		MagicStartXP = skills.getCurrentExp(Skills.MAGIC);
 		startTime = System.currentTimeMillis();
 		mouse.setSpeed(9);
 		FlecterGui = new FlecterGUI();
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
             	FlecterGui.setVisible(true);
             }
         });
 		while (!GUILoaded) {
 			sleep(random(200, 300));
 		}
 		return true;
 	}
 	public int loop() {
 		switch(getStatus()){
 		case Chop:
 			if(!ChopVaild()){
 				log.severe("No hatchet found!");
 				return -1;
 			}
 			if(getMyPlayer().getAnimation() != 867 ){
 			if(bow != null){
 				RSObject tree = objects.getNearest(bow.getTreeID());
 				if(tree != null){
 					if(tree.isOnScreen()){
 						camera.turnTo(tree);
 						tree.interact("Chop");
 						sleep(500,800);
 					}else{
 						if(calc.distanceTo(tree) < 40){
 							log(tree.getLocation());
 							walking.walkTileMM(tree.getLocation());
 							sleep(500,800);
 							while(getMyPlayer().isMoving()){
 								sleep(500,800);
 							}
 						}
 					}
 				}
 			}
 			}else{
 				int x = random(0,5);
 				switch(x){
 				case 0:
 					AntiBan(AntiBan.moveRandomly,400,800);
 					break;
 				case 1:
 					AntiBan(AntiBan.Tabs,400,800);
 					break;
 				case 2:
 					AntiBan(AntiBan.HoverSkill,400,800);
 					break;
 				case 3:
 					AntiBan(AntiBan.Mouse,400,800);
 					break;
 				}
 				
 			}
 			break;
 		case Alch:
 			if(getMyPlayer().getAnimation() != 1248 ){
 			if(AlchVaild()){
 				if(inventory.contains(FireStaff)){
 					inventory.getItem(FireStaff).interact("Wear");
 					sleep(200,500);
 				}
 				RSItem i = inventory.getItem(bow.getBowID());
 						switch(alch){
 						case High:
 							if(!magic.isSpellSelected()){
 								magic.castSpell(Magic.SPELL_HIGH_LEVEL_ALCHEMY);
 								sleep(200,400);
 								break;
 							}
 						case Low:
 							if(!magic.isSpellSelected()){
 								magic.castSpell(Magic.SPELL_LOW_LEVEL_ALCHEMY);
 							sleep(200,400);
 							break;
 							}
 		                 i.doClick(true);
 		                 Alched++;
 		                 sleep(400,500);
 		                 AntiBan(AntiBan.Mouse,400,800);
 		              }
 		          }else{
 				alch = Alch.Drop;
 		          }
 			}else{
 				int x = random(0,5);
 				switch(x){
 				case 0:
 					AntiBan(AntiBan.moveRandomly,400,800);
 					break;
 				case 1:
 					AntiBan(AntiBan.Tabs,400,800);
 					break;
 				case 2:
 					AntiBan(AntiBan.HoverSkill,400,800);
 					break;
 				case 3:
 					AntiBan(AntiBan.Mouse,400,800);
 					break;
 				}
 				
 			}
 			break;
 		case Flect:
			if(!magic.isSpellSelected()){
				game.openTab(Game.Tab.CLAN_CHAT);
				sleep(500);
			}
			if(getMyPlayer().getAnimation() != 1248 && !magic.isSpellSelected()){
 			if(ClayKnife){
 				KnifeID = ClayKnifeID;
 		    }
 			RSItem knife = inventory.getItem(KnifeID);
 			RSItem log = inventory.getItem(bow.getLogID());
 			final RSItem item = inventory.getItems()[4 + 7 * 4];
 			if(knife != null && log != null){
 				if(item != null && item != knife){
 					mouse.move(knife.getPoint());
 					sleep(800,1500);
 					mouse.drag(new Point(705,444));
 					sleep(800,1500);
 				}
 					inventory.useItem(log,knife);
 					sleep(300,500);
 					RSComponent i = interfaces.get(bow.getInterfaceID()).getComponent(bow.getComponetID());
 					if(i.isValid()){
 						i.doClick();
 						sleep(400,600);
 						AntiBan(AntiBan.Tabs,500,650);
 					}
 
 			}
 			}
 			break;
 		case Drop:
 			inventory.dropAll(bow.getBowID());
 			sleep(400,500);
 			AntiBan(AntiBan.moveRandomly,200,900);
 			break;
 		}
 		return random(200,400);
 	}
 	boolean av = false;
 	public boolean AlchVaild(){
 		if(av){
 			return true;
 		}
 		av = true;
 		return inventory.contains(NatureRune) && (inventory.contains(FireRune) || inventory.contains(FireStaff) || equipment.getItem(Equipment.WEAPON).getID() == FireStaff);
 	}
 	public boolean ChopVaild(){
 		if(ChopVaild){
 			return true;
 		}
 		if(inventory.containsOneOf(bow.getHatchetsID()) || equipment.getItem(Equipment.WEAPON).getName().toLowerCase().contains("hatchet")){
 			ChopVaild = true;
 			return true;
 		}
 		return false;
 	}
 	public Status getStatus(){
 		if(game.getTab() == Game.Tab.MAGIC){
 				if(getMyPlayer().getAnimation() == 867){
 					return Status.Chop;
 				}else if (getMyPlayer().getAnimation() == 1248){
 					return Status.Flect;
 				}else{
 					switch(alch){
 					case High:
 						if(!magic.isSpellSelected()){
 							magic.castSpell(Magic.SPELL_HIGH_LEVEL_ALCHEMY);
 							sleep(200,400);
 							break;
 						}
 					case Low:
 						if(!magic.isSpellSelected()){
 							magic.castSpell(Magic.SPELL_LOW_LEVEL_ALCHEMY);
 						sleep(200,400);
 						break;
 						}
 	              }
 					if(inventory.contains(bow.getBowID()) && !inventory.contains(bow.getLogID())){
 						if(alch != Alch.Drop){
 							RSItem i = inventory.getItem(bow.getBowID());
 			                 i.doClick(true);
 			                 Alched++;
 			                 sleep(400,500);
 			                 AntiBan(AntiBan.Mouse,400,800);
 							return Status.Alch;
 						}else{
 							return Status.Drop;
 						}
 					}else if(inventory.contains(bow.getLogID()) && inventory.isFull()){
 						return Status.Flect;
 					}else{
 						return Status.Chop;
 					}
 				}
 		}else{
 			if(inventory.contains(bow.getBowID()) && !inventory.contains(bow.getLogID())){
 				if(alch != Alch.Drop){
 					return Status.Alch;
 				}else{
 					return Status.Drop;
 				}
 			}else if(inventory.contains(bow.getLogID()) && inventory.isFull()){
 				return Status.Flect;
 			}else{
 				return Status.Chop;
 			}
 		}
 	}
 	public void AntiBan(AntiBan a, int min, int max){
 		switch (a){
 		case HoverSkill:
 			switch(random(0,20)){
 			case 0:
 				skills.doHover(Skills.INTERFACE_WOODCUTTING);
 				sleep(random(min,max));
 				skills.doHover(Skills.INTERFACE_FLETCHING);
 				sleep(random(min,max));
 				skills.doHover(Skills.INTERFACE_MAGIC);
 				sleep(random(min,max));
 				break;
 			case 1:
 				skills.doHover(Skills.INTERFACE_MAGIC);
 				sleep(random(min,max));
 				skills.doHover(Skills.INTERFACE_FLETCHING);
 				sleep(random(min,max));
 				break;
 			case 2:
 				skills.doHover(Skills.INTERFACE_FLETCHING);
 				sleep(random(min,max));
 				skills.doHover(Skills.INTERFACE_WOODCUTTING);
 				sleep(random(min,max));
 				break;
 			case 3:
 				skills.doHover(Skills.INTERFACE_FLETCHING);
 				sleep(random(min,max));
 				break;
 			case 4:
 				skills.doHover(Skills.INTERFACE_WOODCUTTING);
 				sleep(random(min,max));
 				break;
 			case 5:
 				skills.doHover(Skills.INTERFACE_MAGIC);
 				sleep(random(min,max));
 				break;
 			case 6:
 				skills.doHover(Skills.INTERFACE_WOODCUTTING);
 				sleep(random(min,max));
 				skills.doHover(Skills.INTERFACE_MAGIC);
 				sleep(random(min,max));
 				break;
 			}
 			//status = "AntiBan(Hovering)..";
 
 		case Tabs:
 			//status = "AntiBan(Tabs)..";
 			switch(random(0,4)){
 			case 0:
 				game.openTab(Game.Tab.MAGIC);
 				sleep(random(min,max));
 				if(random(0,2) == 0){
 				break;
 				}
 			case 1:
 				game.openTab(Game.Tab.INVENTORY);
 				sleep(random(min,max));
 				break;
 			case 2:
 				game.openTab(Game.Tab.STATS);
 				sleep(random(min,max));
 				if(random(0,2) == 0){
 				break;
 				}
 			case 3:
 				game.openTab(Game.Tab.OPTIONS);
 				sleep(random(min,max));
 				break;
 			}
 			break;
 			
 		case moveRandomly:
 			//status = "AntiBan(Camera)";
 			camera.moveRandomly(random(min,max));
 			break;
 		case Mouse:
                 mouse.moveOffScreen();
                 sleep(random(min,max));
                 break;
 		}
 	}
 	@Override
 	public void messageReceived(MessageEvent e) {
 		String m = e.getMessage();
 		if(m.toLowerCase().contains("you got some") && m.toLowerCase().contains("logs")){
 			Logschoped++;
 		}
 		if(m.toLowerCase().contains("you carefully cut the logs into a")){
 			BowsMade++;
 		}
 	}
     private final Color color1 = new Color(255, 0, 0);
     private final Color color2 = new Color(0, 153, 0);
    //private final Color color3 = new Color(255, 0, 51);
    //private final Color color4 = new Color(204, 0, 51);
     private final Color color5 = new Color(0, 153, 0);
     private final Color color6 = new Color(0, 0, 0);
     private final Color color7 = new Color(153, 153, 153, 173);
     private final Color color8 = new Color(255, 0, 0);
     
     private final BasicStroke stroke1 = new BasicStroke(1);
 
 
     private final Font font1 = new Font("Arial", 1, 21);
     private final Font font2 = new Font("Arial", 1, 13);
    //private final Font font3 = new Font("Arial", 1, 16);
     private final Font font4 = new Font("Arial", 1, 15);
     
     private final Image ChatBoxImage = getImage("http://img104.imageshack.us/img104/5737/blankboxgk9.png");
     private final Image PfPBanner = getImage("http://i471.photobucket.com/albums/rr74/Kuzmin1234556/pfp.png");
     private final Image Right_Arrow = getImage("http://www.runeclub.com/images/right_arrow.png");
     private final Image Left_Arrow = getImage("http://www.runeclub.com/images/left_arrow.png");
 
 	@Override
 	public void onRepaint(Graphics g1) {
 		
 		Graphics2D g = (Graphics2D)g1;
 		/**
 		 * Exp Gained , Exp Per Hour , Exp To Next Level , Percent To Next Level
 		 * @XP
 		 */
 		//Magic
 		MagicGainedXP = skills.getCurrentExp(Skills.MAGIC) - MagicStartXP;
 		MagicXPPH = (int) ((MagicGainedXP) * 3600000D / (System.currentTimeMillis() - startTime));;
 		MagicXPTNL = skills.getExpToNextLevel(Skills.MAGIC);
 		MagicNLP = (int) (skills.getPercentToNextLevel(Skills.MAGIC)*2.22);
 		
 		//WoodCutting
 		WoodcuttingGainedXP = skills.getCurrentExp(Skills.WOODCUTTING) - WoodcuttingStartXP;
 		WoodcuttingXPPH = (int) ((WoodcuttingGainedXP) * 3600000D / (System.currentTimeMillis() - startTime));;
 		WoodcuttingXPTNL = skills.getExpToNextLevel(Skills.WOODCUTTING);
 		WoodcuttingNLP = (int) (skills.getPercentToNextLevel(Skills.WOODCUTTING)*2.22);
 		
 		//FletChing
 		FletchingGainedXP = skills.getCurrentExp(Skills.FLETCHING) - FletchingStartXP;
 		FletchingXPPH = (int) ((FletchingGainedXP) * 3600000D / (System.currentTimeMillis() - startTime));;
 		FletchingXPTNL = skills.getExpToNextLevel(Skills.FLETCHING);
 		FletchingNLP = (int) (skills.getPercentToNextLevel(Skills.FLETCHING)*2.22);
 		
 		/**
 		 * Current Level, Levels Gained
 		 * @Level
 		 */
 		//Magic
 		MagicCurrentLevel = skills.getCurrentLevel(Skills.MAGIC);
 		MagicLevelsGained = MagicCurrentLevel - MagicStartLevel;
 		
 		//WoodCutting
 		WoodcuttingCurrentLevel = skills.getCurrentLevel(Skills.WOODCUTTING);
 		WoodcuttingLevelsGained = WoodcuttingCurrentLevel - WoodcuttingStartLevel;
 		
 		//FletChing
 		FletchingCurrentLevel = skills.getCurrentLevel(Skills.FLETCHING);
 		FletchingLevelsGained = FletchingCurrentLevel - FletchingStartLevel;
 		
 		BowsPH = (int) ((BowsMade) * 3600000D / (System.currentTimeMillis() - startTime));;;;
 		LogsPH = (int) ((Logschoped) * 3600000D / (System.currentTimeMillis() - startTime));;;;
 		AlchesPH = (int) ((Alched) * 3600000D / (System.currentTimeMillis() - startTime));;;;
 		
 		if(alch == Alch.High){
 			ProfitPerAlch = bow.getHightAlchPrice() - NatureRunePrice;
 		}else if (alch == Alch.Low){
 			ProfitPerAlch = bow.getLowAlchPrice() - NatureRunePrice;
 		}
 		
 		Profit = ProfitPerAlch * Alched;
 		ProfitPH = (int) ((Profit) * 3600000D / (System.currentTimeMillis() - startTime));;;
 		
 		millis = System.currentTimeMillis() - startTime;
 		hours = millis / (1000 * 60 * 60);
 		millis -= hours * (1000 * 60 * 60);
 		minutes = millis / (1000 * 60);
 		millis -= minutes * (1000 * 60);
 		seconds = millis / 1000;
 
         //g.setFont(font3);
         //g.setColor(color3);
         //g.drawString("RunTime: ", 9, 77);
         //g.setColor(color4);
         //g.drawString("Status: ", 9, 47);
         
         if(cc != 0){
         	int cx = (int)(cc / 2.22);
         g.setColor(color5);
         g.fillRect(532, 451, cc, 13);
         g.setColor(color6);
         g.setStroke(stroke1);
         g.drawRect(531, 451, 222, 14);
         g.setColor(color7);
         g.fillRect(532, 451, cc, 6);
         g.setFont(font4);
         g.setColor(color8);
         g.drawString( cx +"%", 645, 464);
         }
         
 	switch(tab){
 	case Magic:
 		cc = MagicNLP;
         g.drawImage(ChatBoxImage, 0, 338, null);
         g.drawImage(PfPBanner, 0, 262, null);
         g.drawImage(Right_Arrow, 195, 346, null);
         g.drawImage(Left_Arrow, 160, 346, null);
         g.setFont(font1);
         g.setColor(color1);
         g.drawString("Magic", 22, 364);
         g.setFont(font2);
         g.setColor(color2);
         g.drawString("Gained XP: " + MagicGainedXP + ".", 12, 389);
         g.drawString("XP Per Hour: " + MagicXPPH + ".", 12, 411);
         g.drawString("Alched: " + Alched + ".", 12, 433);
         g.drawString("Alches Per Hour: " + AlchesPH + ".", 12, 455);
         g.drawString("Level: " + MagicCurrentLevel + "(" + MagicLevelsGained + ").", 13, 472);
 	break;
 	case Profit:
 		cc = 0;
         g.drawImage(ChatBoxImage, 0, 338, null);
         g.drawImage(PfPBanner, 0, 262, null);
         g.drawImage(Right_Arrow, 195, 346, null);
         g.drawImage(Left_Arrow, 160, 346, null);
         g.setFont(font1);
         g.setColor(color1);
         g.drawString("Profit", 22, 364);
         g.setFont(font2);
         g.setColor(color2);
         g.drawString("Nature Rune Price: " + NatureRunePrice + ".", 12, 389);
         g.drawString("Profit Per Bow: " + ProfitPerAlch + ".", 12, 411);
         g.drawString("Profit: " + Profit + ".", 12, 433);
         g.drawString("Profit Per Hour: " + ProfitPH + ".", 12, 455);
         break;
 	case Fletching:
 		cc = FletchingNLP;
 		
         g.drawImage(ChatBoxImage, 0, 338, null);
         g.drawImage(PfPBanner, 0, 262, null);
         g.drawImage(Right_Arrow, 195, 346, null);
         g.drawImage(Left_Arrow, 160, 346, null);
         g.setFont(font1);
         g.setColor(color1);
         g.drawString("Fletching", 22, 364);
         g.setFont(font2);
         g.setColor(color2);
         g.drawString("Gained XP: " + FletchingGainedXP + ".", 12, 389);
         g.drawString("XP Per Hour: " + FletchingXPPH + ".", 12, 411);
         g.drawString("Bows: " + BowsMade + ".", 12, 433);
         g.drawString("Bows Per Hour: " + BowsMade + ".", 12, 455);
         g.drawString("Level: " + FletchingCurrentLevel + "(" + FletchingLevelsGained + ").", 13, 472);
         break;
 	case Woodcutting:
 		cc = WoodcuttingNLP;
 		
         g.drawImage(ChatBoxImage, 0, 338, null);
         g.drawImage(PfPBanner, 0, 262, null);
         g.drawImage(Right_Arrow, 195, 346, null);
         g.drawImage(Left_Arrow, 160, 346, null);
         g.setFont(font1);
         g.setColor(color1);
         g.drawString("Woodcutting", 22, 364);
         g.setFont(font2);
         g.setColor(color2);
         g.drawString("Gained XP: " + WoodcuttingGainedXP + ".", 12, 389);
         g.drawString("XP Per Hour: " + WoodcuttingXPPH + ".", 12, 411);
         g.drawString("Logs: " + Logschoped + ".", 12, 433);
         g.drawString("Logs Per Hour: " + LogsPH + ".", 12, 455);
         g.drawString("Level: " + WoodcuttingCurrentLevel + "(" + WoodcuttingLevelsGained + ").", 13, 472);
         break;
 	}
 	}
     private Image getImage(String url) {
         try {
             return ImageIO.read(new URL(url));
         } catch(IOException e) {
             return null;
         }
     }
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		Point p = e.getPoint();
 		Rectangle a = new Rectangle(163, 348, 26, 26);
 		Rectangle b = new Rectangle(199, 347, 27, 27);
 		if(a.contains(p)){
 			if(tab == Tabs.Profit){
 				tab = Tabs.Magic;
 			}else if(tab == Tabs.Fletching){
 				tab = Tabs.Profit;
 			}else if(tab == Tabs.Woodcutting){
 				tab = Tabs.Fletching;
 			}
 		}else if(b.contains(p)){
 			if(tab == Tabs.Magic){
 				tab = Tabs.Profit;
 			}else if(tab == Tabs.Profit){
 				tab = Tabs.Fletching;
 			}else if(tab == Tabs.Fletching){
 				tab = Tabs.Woodcutting;
 			}
 		}
 	}
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		
 	}
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		
 	}
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		
 	}
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		
 	}
 	public class FlecterGUI extends javax.swing.JFrame {
 
 		private static final long serialVersionUID = 1L;
 		public FlecterGUI() {
 	        initComponents();
 	    }
 
 	    @SuppressWarnings({ "unchecked", "rawtypes" })
 	    private void initComponents() {
 
 	        jLabel1 = new javax.swing.JLabel();
 	        jLabel2 = new javax.swing.JLabel();
 	        jComboBox1 = new javax.swing.JComboBox();
 	        jComboBox2 = new javax.swing.JComboBox();
 	        jLabel3 = new javax.swing.JLabel();
 	        jLabel4 = new javax.swing.JLabel();
 	        jComboBox3 = new javax.swing.JComboBox();
 	        jButton1 = new javax.swing.JButton();
 	        jRadioButton1 = new javax.swing.JRadioButton();
 	        
 	        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
 	        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 24));
 	        jLabel1.setText("PowerFletcherPro");
 
 	        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18));
 	        jLabel2.setText("Tree:");
 
 	        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Oak", "Willow", "Maple", "Yew", "Magic" }));
 	        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
 	            public void actionPerformed(java.awt.event.ActionEvent evt) {
 	                jComboBox1ActionPerformed(evt);
 	            }
 	        });
 
 	        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Shortbow", "Longbow", "Arrow shafts" }));
 	        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
 	            public void actionPerformed(java.awt.event.ActionEvent evt) {
 	                jComboBox2ActionPerformed(evt);
 	            }
 	        });
 
 	        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 18));
 	        jLabel3.setText("Flect:");
 
 	        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 18));
 	        jLabel4.setText("Alch:");
 
 	        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Drop", "Low Alch", "Hight Alch" }));
 
 	        jButton1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
 	        jButton1.setText("Start");
 	        jButton1.addActionListener(new java.awt.event.ActionListener() {
 	            public void actionPerformed(java.awt.event.ActionEvent evt) {
 	                jButton1ActionPerformed(evt);
 	            }
 	        });
 
 	        jRadioButton1.setFont(new java.awt.Font("Tahoma", 0, 14));
 	        jRadioButton1.setText("Clay Knife");
 
 	        
 	        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
 	        getContentPane().setLayout(layout);
 	        layout.setHorizontalGroup(
 	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 	            .addGroup(layout.createSequentialGroup()
 	                .addGap(75, 75, 75)
 	                .addComponent(jLabel1)
 	                .addContainerGap(94, Short.MAX_VALUE))
 	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
 	                .addContainerGap(63, Short.MAX_VALUE)
 	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
 	                    .addComponent(jLabel3)
 	                    .addComponent(jLabel4)
 	                    .addComponent(jLabel2))
 	                .addGap(43, 43, 43)
 	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
 	                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
 	                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
 	                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
 	                .addGap(84, 84, 84))
 	            .addGroup(layout.createSequentialGroup()
 	                .addGap(46, 46, 46)
 	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 	                    .addComponent(jRadioButton1)
 	                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE))
 	                .addContainerGap(64, Short.MAX_VALUE))
 	        );
 	        layout.setVerticalGroup(
 	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 	            .addGroup(layout.createSequentialGroup()
 	                .addContainerGap()
 	                .addComponent(jLabel1)
 	                .addGap(34, 34, 34)
 	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 	                    .addComponent(jLabel2)
 	                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
 	                .addGap(18, 18, 18)
 	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 	                    .addComponent(jLabel3)
 	                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
 	                .addGap(18, 18, 18)
 	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 	                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 	                    .addComponent(jLabel4))
 	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
 	                .addComponent(jRadioButton1)
 	                .addGap(18, 18, 18)
 	                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
 	                .addContainerGap())
 	        );
 
 	        pack();
 	    }// </editor-fold>
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {                                           
 	if(jComboBox1.getSelectedItem().toString().equals("Normal")){
 	    jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Shortbow", "Longbow", "Arrow shafts" }));
 	}else{
 	    jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Shortbow", "Longbow"}));
 	}
 	}                                          
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {                                           
 	if(jComboBox2.getSelectedItem().toString().equals("Arrow shafts")){
 	    jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Drop", "Low Alch", "Hight Alch" }));
 	}else{
 	        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Drop", "Low Alch", "Hight Alch" }));
 	}
 	}                                          
 
 	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
 		if(jComboBox1.getSelectedItem().toString().equals("Normal")){
 			if(jComboBox2.getSelectedItem().toString().equals("Shortbow")){
 				bow = Bow.ShortBow;
 			}else if(jComboBox2.getSelectedItem().toString().equals("Longbow")){
 				bow = Bow.LongBow;
 			}else if(jComboBox2.getSelectedItem().toString().equals("Arrow shafts")){
 				bow = Bow.Arrowshafts;
 			}
 		}else if(jComboBox1.getSelectedItem().toString().equals("Oak")){
 			if(jComboBox2.getSelectedItem().toString().equals("Shortbow")){
 				bow = Bow.OakShortBow;
 			}else if(jComboBox2.getSelectedItem().toString().equals("Longbow")){
 				bow = Bow.OakLongBow;
 			}
 		}else if(jComboBox1.getSelectedItem().toString().equals("Willow")){
 			if(jComboBox2.getSelectedItem().toString().equals("Shortbow")){
 				bow = Bow.WillowShortBow;
 			}else if(jComboBox2.getSelectedItem().toString().equals("Longbow")){
 				bow = Bow.WillowLongBow;
 			}
 		}else if(jComboBox1.getSelectedItem().toString().equals("Maple")){
 			if(jComboBox2.getSelectedItem().toString().equals("Shortbow")){
 				bow = Bow.MapleShortBow;
 			}else if(jComboBox2.getSelectedItem().toString().equals("Longbow")){
 				bow = Bow.MapleLongBow;
 			}
 		}else if(jComboBox1.getSelectedItem().toString().equals("Yew")){
 			if(jComboBox2.getSelectedItem().toString().equals("Shortbow")){
 				bow = Bow.YewShortBow;
 			}else if(jComboBox2.getSelectedItem().toString().equals("Longbow")){
 				bow = Bow.YewLongBow;
 			}
 		}else if(jComboBox1.getSelectedItem().toString().equals("Magic")){
 			if(jComboBox2.getSelectedItem().toString().equals("Shortbow")){
 				bow = Bow.MagicShortBow;
 			}else if(jComboBox2.getSelectedItem().toString().equals("Longbow")){
 				bow = Bow.MagicLongBow;
 			}
 		}
 		if(jComboBox3.getSelectedItem().toString().equals("Drop")){
 			alch = Alch.Drop;
 		}else if(jComboBox3.getSelectedItem().toString().equals("Low Alch")){
 			alch = Alch.Low;
 		}else if(jComboBox3.getSelectedItem().toString().equals("Hight Alch")){
 			alch = Alch.High;
 		}
 		if(jRadioButton1.isSelected()){
 			ClayKnife = true;
 		}
 		FlecterGui.setVisible(false);
 		FlecterGui.dispose();
 		BowPrice = grandExchange.lookup(bow.getBowID()).getGuidePrice();
 		GUILoaded = true;
 	}
 	    private javax.swing.JButton jButton1;
 	    @SuppressWarnings("rawtypes")
 		private javax.swing.JComboBox jComboBox1;
 	    @SuppressWarnings("rawtypes")
 		private javax.swing.JComboBox jComboBox2;
 	    @SuppressWarnings("rawtypes")
 		private javax.swing.JComboBox jComboBox3;
 	    private javax.swing.JLabel jLabel1;
 	    private javax.swing.JLabel jLabel2;
 	    private javax.swing.JLabel jLabel3;
 	    private javax.swing.JLabel jLabel4;
 	    private javax.swing.JRadioButton jRadioButton1;
 	}
 }
