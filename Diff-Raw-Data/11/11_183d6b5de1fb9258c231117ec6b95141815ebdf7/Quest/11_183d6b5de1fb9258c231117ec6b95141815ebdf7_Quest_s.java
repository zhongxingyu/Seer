 package dungeonCrawler;
 
 import java.awt.BorderLayout;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 
 /**
  * Add quests to the Dungeon Crawler in single player mode
  * @author Hucke
  *
  */
 public class Quest{
 	
 	private static JDialog dialogStart;
 
 	private static JDialog dialogComplete; 
 	
 	private static int level = 0;
 	private static int[] kills = {0,0,0};
 	private static int kill_EB = 0;
 	private static int[] money = {0,0,0};
 	private static boolean[] bow = {false,false,false};
 	private static int[] mana = {0,0,0};
 	private static int[] magicShield = {0,0,0};
 	private static int[] health = {0,0,0};
 	public static String levelStart;
 	public static Vector2d startPos;
 	private static boolean singlePlayerGame;
 	
 	
 	/**
 	 * Get restart point after failing a quest
 	 * @param s as {@link String} 
 	 * @return The player start position 
 	 */
 	private static Vector2d getStart(String s){
 		Vector2d startPos=new Vector2d();
 		String[] test =levelStart.split(",");
 		startPos.setX(Integer.parseInt(test[1]));
 		startPos.setY(Integer.parseInt(test[2]));
 		
 		return startPos;
 	}
 	
 	/**
 	 * Start every level a dialog with new quests
 	 * @param level as {@link Integer} as {@link Integer}
 	 */
 	public static void startLevel(int level){
 		if(singlePlayerGame){
 			startPos = getStart(levelStart);
 			getStart(levelStart);
 			dialogStart = new JDialog();
 			dialogStart.setSize(500,280);
 			dialogStart.setLocationRelativeTo(null);
 			JPanel dialogPanel = new JPanel();
 			dialogPanel.setLayout(new BorderLayout());
 			
 			
 			JPanel questPanel = new JPanel();
 			questPanel.setLayout(new BorderLayout());
 			JTextArea textArea = new JTextArea();
 			textArea.setEditable(false);
 			textArea.setPreferredSize(new Dimension(200,200));
 			questPanel.add(textArea, BorderLayout.CENTER);
 			
 			textArea.setText(getQuest(level));
 			
 			
 			JPanel buttonPanel = new JPanel(new FlowLayout());
 			JButton exitButton = new JButton("OK");
 			exitButton.addActionListener(new ActionListener() {
 				
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					dialogStart.dispose();
 					GameLogic.timer.start();
 							
 				}
 			});
 			buttonPanel.add(exitButton);
 			
 			dialogPanel.add(questPanel,BorderLayout.CENTER);
 			dialogPanel.add(buttonPanel,BorderLayout.PAGE_END);
 			dialogStart.add(dialogPanel);
 			GameLogic.timer.stop();
 			dialogStart.setVisible(true);
 		}
 		else if(!singlePlayerGame){
 			GameLogic.timer.start();
 		}
 	}
 		
 		/**
 		 * Check if a level completed
 		 *If a level completed a Dialog show that you can go on else you go to start of the level 
 		 * @param state
 		 */
 		public static void completedMission(boolean state){
 			if(singlePlayerGame){	
 				dialogComplete = new JDialog();
 				dialogComplete = new JDialog();
 				dialogComplete.setLocationRelativeTo(null);
 				dialogComplete.setSize(500,280);
 				JPanel dialogPanel = new JPanel();
 				dialogPanel.setLayout(new BorderLayout());
 				
 				
 				JPanel questPanel = new JPanel();
 				questPanel.setLayout(new BorderLayout());
 				JTextArea textArea = new JTextArea();
 				textArea.setEditable(false);
 				textArea.setPreferredSize(new Dimension(200,200));
 				questPanel.add(textArea, BorderLayout.CENTER);
 				
 				if(state == true){
 					textArea.setText("Alle Aufgaben wurden erledigt");
 				}
 				else if(state == false){
 					textArea.setText("Es sind nach nicht alle aufgaben erfüllt\n\nDamit du alles erfüllen kannst starte noch mal am Anfang");
 				}
 					
 				
 				JPanel buttonPanel = new JPanel(new FlowLayout());
 				JButton	exitButton = new JButton("OK");
 				exitButton.addActionListener(new ActionListener() {
 					
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						dialogComplete.dispose();
 						GameLogic.timer.start();
 					}
 				});
 				buttonPanel.add(exitButton);
 				
 				dialogPanel.add(questPanel,BorderLayout.CENTER);
 				dialogPanel.add(buttonPanel,BorderLayout.PAGE_END);
 				dialogComplete.add(dialogPanel);
 				//GameLogic.timer.stop();
 				dialogComplete.setVisible(true);
 			}
 			
 		}
 
 	/**
 	 * Level getter
 	 * @return level {@link Integer}
 	 */
 	public static int getLevel(){
 		return level;
 	}
 	/**
 	 * Level setter
 	 * @param level as {@link Integer} as {@link Integer}
 	 */
 	public static void setLevel(int level){
 		Quest.level = level;
 		
 	}
 	/**
 	 * killed enemy setter
 	 * @param level as {@link Integer}
 	 */
 	public static void killedEnemys(int level){
 		kills[level] += 1 ;
 	}
 	/**
 	 * killed enemy getter
 	 * @param level as {@link Integer}
 	 * @return killed enemy as {@link Integer}
 	 */
 	public static int getKilledEnemys(int level){
 		return kills[level];
 	}
 	
 	/**
 	 * killed end boss setter
 	 */
 	public static void killedEndBoss(){
 		kill_EB+= 1 ;
 	}
 	/**
 	 * killed end boss getter
 	 * @return killed end bosses {@link Integer}
 	 */
 	public static int getKilledEndBoss(){
 		return kill_EB;
 	}
 	/**
 	 * collected money units setter
 	 * @param level as {@link Integer}
 	 */
 	public static void collectedMoney(int level){
 		money[level] +=1;
 	}
 	/**
 	 * collected money units getter
 	 * @param level as {@link Integer}
 	 * @return number of collected money units {@link Integer}
 	 */
 	public static int getCollectedMoney(int level){
 		return money[level];
 	}
 	/**
 	 * collected bow setter
 	 * @param level as {@link Integer}
 	 */
 	public static void collectedBow(int level){
 		bow[level] = true;
 	}
 	/**
 	 * collected bow getter
 	 * @param level as {@link Integer}
 	 * @return true if you collected a bow {@link boolean}
 	 */
 	
 	public static boolean getCollectedBow(int level){
 		return bow[level];
 	}
 	
 	/**
 	 * Collected mana setter
 	 * @param level as {@link Integer}
 	 */
 	public static void collectedMana(int level){
 		mana[level] +=1;
 	}
 	/**
 	 * Collected mana getter
 	 * @param level as {@link Integer}
 	 * @return number of collected mana units {@link Integer}
 	 */
 	public static int getCollectedMana(int level){
 		return mana[level];
 	}
 	/**
 	 * Collected magic shield setter
 	 * @param level as {@link Integer}
 	 */
 	public static void collectedMagicShield(int level){
 		magicShield[level] +=1;
 	}
 	/**
 	 * Collected magic shield getter
 	 * @param level as {@link Integer}
 	 * @return number of collected magic shield units {@link Integer}
 	 */
 	public static int getCollectedMagicShield(int level){
 		return magicShield[level];
 	}
 	/**
 	 * collected health units setter
 	 * @param level as {@link Integer}
 	 */
 	public static void collectedHealth(int level){
 		health[level] +=1;
 	}
 	/**
 	 * collected health units getter
 	 * @param level as {@link Integer}
 	 * @return number of collected magic shield units {@link Integer}
 	 */
 	public static int getCollectedHealth(int level){
 		return health[level];
 	}
 	/**
 	 * get quest for each of the first three level
 	 * @param level as {@link Integer}
 	 * @return the quest {@link String} 
  	 */
 	public static String getQuest(int level){
 		if(singlePlayerGame){
 			String welcome = "Hallo in Level: " + (level+1) + " hast du folgende Aufgaben:\n\n";
 			switch(level){
 				case 0: return 	welcome +
 								"Sammle einen Bogen\n\n" +
								"Töte alle Teufel\n\n" +
 								"Sammle alle Geldeinheiten";
 				case 1: return 	welcome +
 								"Töte 10 Teufel\n\n" +
 								"Sammle 2 magisches Schild ein\n\n";
 				case 2: return 	welcome +
 								"Sammle 10 Geldeinheien ein\n\n" +
 								"Sammle 10 Manaträne ein\n\n" +
 								"Töte 10 Teufel" +
 								"Töte den End Boss";
 				default: return null;
 			}
 		}
 		else{
 			return null;
 		}
 	}
 	/**
 	 * check if a quest is done
 	 * @param level as {@link Integer}
 	 * @return if done true else false {@link boolean}
 	 */
 	public static boolean doneQuest(int level){
 		if(!singlePlayerGame){
 			return true;
 		}
 		else if (singlePlayerGame){
 			switch (level){
 			case 0:
				if((getCollectedBow(0)==true) && (getCollectedMoney(0)==2) && (getKilledEnemys(0)==8)){
 					return true;
 				}
 			case 1:
 				if((getKilledEnemys(1)>=10) && (getCollectedMagicShield(1)>=1)){
 					return true;
 				}
 			case 2:
 				if((getCollectedMoney(2)>=10) && (getCollectedMana(2)>=10) && (getKilledEnemys(2)>=10) && (getKilledEndBoss()>=1)){
 					return true; 
 				}
 			default: return false;
 			}
 		}
 		else{
 			return false;
 		}
  	}
 	/**
 	 * Enable user out of this package to start and stop the timer
 	 * @param setTo
 	 */
 	public void setTimer(boolean setTo){
 		if(setTo == true){
 			GameLogic.timer.start();
 		}
 		else if(setTo == false){
 			GameLogic.timer.stop();
 		}
 	}
 	/**
 	 * Set quest on or off
 	 * @param state as {@link Boolean} if true start with quest if false start without guest 
 	 */
 	public static void setGameMode(boolean state){
 		singlePlayerGame = state;
 	}
 	
 	public static boolean getGameMode(){
 		return singlePlayerGame;
 	}
 	
 
 }
