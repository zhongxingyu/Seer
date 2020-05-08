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
  * Add quests to the Dungeon Crawler
  * @author Hucke
  *
  */
 public class Quest{
 	
 	private JDialog dialogStart;
 
 	private static JDialog dialogComplete; 
 	
 	private static int level = 0;
 	private static int[] kills = {0,0,0};
 	private static int[] money = {0,0,0};
 	private static boolean[] bow = {false,false,false};
 	private static int[] mana = {0,0,0};
 	private static int[] magicShield = {0,0,0};
 	private static int[] health = {0,0,0};
 	
 	public void startLevel(int level){
 		dialogStart = new JDialog();
 		dialogStart.setSize(500,280);
 		dialogStart.setUndecorated(true);
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
 	
 	public static void completedMission(boolean state){
 			dialogComplete = new JDialog();
 			dialogComplete = new JDialog();
 			dialogComplete.setLocationRelativeTo(null);
 			dialogComplete.setSize(500,280);
 			dialogComplete.setUndecorated(true);
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
 				textArea.setText("Es sind nach nicht alle aufgaben erfüllt");
 			}
 				
 			
 			JPanel buttonPanel = new JPanel(new FlowLayout());
 			JButton	exitButton = new JButton("OK");
 			exitButton.addActionListener(new ActionListener() {
 				
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					dialogComplete.dispose();
 					GameLogic.timer.start();
 					notify();
 					
 				}
 			});
 			buttonPanel.add(exitButton);
 			
 			dialogPanel.add(questPanel,BorderLayout.CENTER);
 			dialogPanel.add(buttonPanel,BorderLayout.PAGE_END);
 			dialogComplete.add(dialogPanel);
 			//GameLogic.timer.stop();
 			dialogComplete.setVisible(true);
 	}
 
 	
 	public static int getLevel(){
 		return level;
 	}
 	
 	public static void setLevel(int level){
 		Quest.level = level;
 		
 	}
 	
 	public static void killedEnemys(int level){
 		kills[level] += 1 ;
 	}
 	
 	public static int getKilledEnemys(int level){
 		return kills[level];
 	}
 	
 	public static void collectedMoney(int level){
 		money[level] +=1;
 	}
 	
 	public static int getCollectedMoney(int level){
 		return money[level];
 	}
 	
 	public static void collectedBow(int level){
 		bow[level] = true;
 		
 	}
 	
 	public static boolean getCollectedBow(int level){
 		return bow[level];
 	}
 	
 	public static void collectedMana(int level){
 		mana[level] +=1;
 	}
 	
 	public static int getCollectedMana(int level){
 		return mana[level];
 	}
 	
 	public static void collectedMagicShield(int level){
 		magicShield[level] +=1;
 	}
 
 	public static int getCollectedMagicShield(int level){
 		return magicShield[level];
 	}
 	
 	public static void collectedHealth(int level){
 		health[level] +=1;
 	}
 	
 	public static int getCollectedHealth(int level){
 		return health[level];
 	}
 	
 	public static String getQuest(int level){
 		String welcome = "Hallo in Level: " + (level+1) + " hast du folgende Aufgaben:\n\n";
 		switch(level){
 			case 0: return 	welcome +
 							"Sammle einen Bogen\n\n" +
 							"Töte alle Teufel\n\n" +
 							"Sammle alle Geldeinheiten";
 			case 1: return 	welcome +
 							"Töte 10 Teufel\n\n" +
							"Sammle 2 Healthpacks ein\n\n" +
							"Sammle 2 Manaträne ein";
 			case 2: return 	welcome +
 							"Sammle 10 Geldeinheien ein\n\n" +
 							"Sammle 10 Manaträne ein\n\n" +
 							"Töte 10 Teufel";
 			default: return null;
 		}
 	}
 	
 	public static boolean doneQuest(int level){
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
 			if((getCollectedMoney(2)>=10) && (getCollectedMana(2)>=10) && (getKilledEnemys(2)>=10)){
 				return true; 
 			}
 		default: return false;
 		}
  	}
 	
 	public void setTimer(boolean setTo){
 		if(setTo == true){
 			GameLogic.timer.start();
 		}
 		else if(setTo == false){
 			GameLogic.timer.stop();
 		}
 	}
 	
 }
