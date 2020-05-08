 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.event.MouseInputListener;
 
 public class Menu extends JPanel implements Runnable {
 
 	/**
 * 
 */
 	private static final long serialVersionUID = 5237335232850181080L;
 	private Thread menu;
 	private final int DELAY = 50;
 	private Board gameBoard;
 	static private boolean addRecycleTower = false;
 	static private boolean addInceneratorTower = false;
 	static private boolean addWindmillTower = false;
 	static private boolean addMetalTower = false;
 	static private boolean addCompostTower = false;
 	static private boolean addNuclearTower = false;
 	private boolean startWave = false;
 	private String currentTowerDirection = "South";
 	private JLabel info = new JLabel("");
 	private JLabel towerInfo = new JLabel("");
 	private JButton sellButton,cancelButton,muteButton,effectMuteButton,upgradeButton;
 	private Tower clickedTower;
 
 	public Menu(Board board) {
 		setDoubleBuffered(true);
 		gameBoard = board;
 		board.addMouseListener(new Mouse());
 		board.addMouseMotionListener(new Mouse());
 		setLayout(null);
 		
 		
 		info.setHorizontalAlignment(SwingConstants.CENTER);
 		info.setBounds(5,0, 290, 100);
 		
 		towerInfo.setHorizontalAlignment(SwingConstants.CENTER);
 		towerInfo.setVerticalAlignment(SwingConstants.CENTER);
 		towerInfo.setBounds(5, 170, 290, 50);
 		
 		
 		
 		JButton inceneratorButton = new JButton("Incenerator $100");
 		inceneratorButton.addActionListener(new TowerButtonListener(Util.TowerType.incenerator));
 		inceneratorButton.setBounds(0, 250, 150, 50);
 		
 		JButton recycleButton = new JButton("Recycling $200");
 		recycleButton.addActionListener(new TowerButtonListener(Util.TowerType.recycle));
 		recycleButton.setBounds(150, 250, 150, 50);
 		
 		JButton metalButton = new JButton("Scrap Metal $250");
 		metalButton.addActionListener(new TowerButtonListener(Util.TowerType.metal));
 		metalButton.setBounds(0, 300, 150, 50);
 		
 		JButton compostButton = new JButton("Compost $250");
 		compostButton.addActionListener(new TowerButtonListener(Util.TowerType.compost));
 		compostButton.setBounds(150, 300, 150, 50);
 		
 		JButton windmillButton = new JButton("Windmill $300");
 		windmillButton.addActionListener(new TowerButtonListener(Util.TowerType.windmill));
 		windmillButton.setBounds(0,350,150, 50);
 		
 		JButton nuclearButton = new JButton("Nuclear $400");
 		nuclearButton.addActionListener(new TowerButtonListener(Util.TowerType.nuclear));
 		nuclearButton.setBounds(150,350,150, 50);
 		
 		JButton startWaveButton = new JButton("Send Next Wave");
 		startWaveButton.addActionListener(new StartWaveButtonListener());
 		startWaveButton.setBounds(0, 450, 300, 50);
 		
 		muteButton = new JButton("Mute Music ");
 		muteButton.addActionListener(new MuteButtonListener());
 		muteButton.setBounds(0, 550, 150, 50);
 		
 		effectMuteButton = new JButton("Mute Effects");
 		effectMuteButton.addActionListener(new EffectMuteButtonListener());
 		effectMuteButton.setBounds(150,550, 150, 50);
 		
 		
 		
 		add(info);
 		add(towerInfo);
 		add(recycleButton);
 		add(inceneratorButton);
 		add(metalButton);
 		add(windmillButton);
 		add(compostButton);
 		add(nuclearButton);
 		add(startWaveButton);
 		add(muteButton);
 		add(effectMuteButton);
 
 	}
 
 	private class CancelButtonListener implements ActionListener{
 		
 		@Override
 		public void actionPerformed(ActionEvent e){
 			resetButtons();
 			gameBoard.pendingTower=null;
 			remove(cancelButton);
 		}
 	}
 	private class StartWaveButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			startWave = !startWave;
 			gameBoard.startWave();
 		}
 	}
 
 	private class SellTowerButtonListener implements ActionListener {
 		
 		Tower toBeSold;
 		int moneyBack=0;
 		
 		public SellTowerButtonListener(Tower t, int cost){
 			toBeSold=t;
 			moneyBack=cost;
 		}
 		
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			gameBoard.sellTower(toBeSold, moneyBack);
 			remove(sellButton);
 			remove(upgradeButton);
 			sellButton=null;
 			upgradeButton=null;
 			info.setText("");
 			towerInfo.setText("");
 		}
 	}
 	
 private class UpgradeTowerButtonListener implements ActionListener {
 		
 		Tower toBeUpgraded;
		int moneyBack=0;
 		
 		public UpgradeTowerButtonListener(Tower t, int cost){
 			toBeUpgraded=t;
			moneyBack=cost;
 		}
 		
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
			gameBoard.upgradeTower(toBeUpgraded, moneyBack);
			setTowerInfoText(toBeUpgraded);
 		}
 	}
 	
 	private class EffectMuteButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (gameBoard.effectMute) {
 				gameBoard.effectMute=false;
 				effectMuteButton.setBackground(null);
 				effectMuteButton.setForeground(null);
 			} else {
 				gameBoard.effectMute=true;
 				effectMuteButton.setBackground(Color.red);
 				effectMuteButton.setForeground(Color.black);
 			}
 
 		}
 	}
 	
 	
 	
 	private class MuteButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (gameBoard.muted) {
 				gameBoard.startMusic();
 				muteButton.setBackground(null);
 				muteButton.setForeground(null);
 			} else {
 				gameBoard.stopMusic();
 				muteButton.setBackground(Color.red);
 				muteButton.setForeground(Color.black);
 			}
 
 		}
 	}
 		
 	private void fixBooleans(Util.TowerType type){
 		switch(type){
 			case metal:
 				addMetalTower = true;
 				addInceneratorTower = false;
 				addRecycleTower = false;
 				addWindmillTower = false;
 				addCompostTower = false;
 				addNuclearTower = false;
 				break;
 			case recycle:
 				addRecycleTower = true;
 				addInceneratorTower = false;
 				addMetalTower = false;
 				addCompostTower = false;
 				addWindmillTower = false;
 				addNuclearTower = false;
 				break;
 			case windmill:
 				addWindmillTower = true;
 				addMetalTower = false;
 				addInceneratorTower = false;
 				addRecycleTower = false;
 				addCompostTower = false;
 				addNuclearTower = false;
 				break;
 			case incenerator:
 				addInceneratorTower = true;
 				addRecycleTower = false;
 				addWindmillTower = false;
 				addMetalTower = false;	
 				addCompostTower = false;
 				addNuclearTower = false;
 				break;
 			case compost:
 				addCompostTower = true;
 				addInceneratorTower = false;
 				addRecycleTower = false;
 				addWindmillTower = false;
 				addMetalTower = false;	
 				addNuclearTower = false;
 				break;
 			case nuclear:
 				addNuclearTower = true;
 				addInceneratorTower = false;
 				addRecycleTower = false;
 				addWindmillTower = false;
 				addMetalTower = false;	
 				addCompostTower= false;
 				break;
 				
 		}
 	}
 	
 	private class TowerButtonListener implements ActionListener {
 		
 		Util.TowerType thisType;
 		
 		public TowerButtonListener(Util.TowerType type){
 			thisType=type;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			if(sellButton != null){
 				remove(sellButton);
 				if(clickedTower != null){
 					clickedTower.setHighLight(false);
 				}
 			}
 			if (gameBoard.getBudget() >= getCost(thisType)) {
 				fixBooleans(thisType);
 				gameBoard.pendingTower = null;
 				setInfoText(thisType);
 				if(cancelButton==null){
 					cancelButton = new JButton("Cancel Purchase");
 					cancelButton.setBounds(50, 135, 200, 50);
 					cancelButton.addActionListener(new CancelButtonListener());
 				}
 				add(cancelButton);
 				
 			}
 		}
 	}
 	
 	private class Mouse implements MouseInputListener {
 
 		
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 
 			int mouseX = e.getPoint().x;
 			int mouseY = e.getPoint().y;
 
 			//if (!Board.inBetweenLevels) {
 
 				int adjX = mouseX - 20;
 				int adjY = mouseY - 20;
 
 				// Detect a left click
 				if (e.getButton() == 1) {
 					
 					//None of the buttons have been pressed
 					if(!(addCompostTower || addInceneratorTower || addMetalTower || addRecycleTower || addWindmillTower || addNuclearTower )){
 						//User clicks on a tower on the board, and hasn't previously pressed any buttons
 						if(gameBoard.onTower(mouseX, mouseY)){
 							//Change to new clicked tower
 							if(clickedTower != null){
 								clickedTower.setHighLight(false);
 								remove(upgradeButton);
 								remove(sellButton);
 								sellButton=null;
 								upgradeButton=null;
 							}
 							clickedTower=gameBoard.onTowerReturn(mouseX, mouseY);
 							int cost=(int)(getCost(clickedTower.type)*.75);
 							sellButton = new JButton("Sell $"+cost);
 							sellButton.setBounds(15, 135, 135, 50);
 							sellButton.addActionListener(new SellTowerButtonListener(clickedTower,cost));
 							upgradeButton= new JButton("Upgrade $"+cost*2);
 							upgradeButton.setBounds(150,135,135,50);
 							upgradeButton.addActionListener(new UpgradeTowerButtonListener(clickedTower, cost));
 							add(upgradeButton);
 							add(sellButton);
 							setInfoText(clickedTower.type);
 							setTowerInfoText(clickedTower);
 							clickedTower.setHighLight(true);
 						}else{
 							//Remove all focus from any previously clicked towers
 							info.setText("");
 							towerInfo.setText("");
 							if(sellButton !=null){
 								remove(sellButton);
 								remove(upgradeButton);
 								if(clickedTower != null){
 									clickedTower.setHighLight(false);
 								}
 							}
 							clickedTower=null;
 							
 						}
 						
 					}else{
 						Util.TowerType type = null;
 	
 						if (addInceneratorTower) {
 							type = Util.TowerType.incenerator;
 	
 						} else if ((addRecycleTower)) {
 							type = Util.TowerType.recycle;
 	
 						} else if ((addWindmillTower)) {
 	
 							type = Util.TowerType.windmill;
 	
 						} else if ((addMetalTower)) {
 	
 							type = Util.TowerType.metal;
 							
 						} else if ((addCompostTower)) {
 							
 							type = Util.TowerType.compost;
 							
 						} else if ((addNuclearTower)) {
 							
 							type = Util.TowerType.nuclear;
 							
 						}
 	
 						boolean isValid = validTower(mouseX, mouseY, type);
 	
 						if (type != null && isValid) {
 							gameBoard.addTower(new Tower(adjX, adjY, 1, 25, type,
 									isValid, currentTowerDirection));
 							gameBoard.removeMoney(getCost(type));
 							gameBoard.pendingTower = null;
 							resetButtons();
 							remove(cancelButton);
 						}
 					
 					}
 
 					// Detect a right click
 				} else if (e.getButton() == 3) {
 					currentTowerDirection = gameBoard.pendingTower.rotateDir();
 					Util.TowerType type = null;
 
 					if (addInceneratorTower) {
 						type = Util.TowerType.incenerator;
 
 					} else if ((addRecycleTower)) {
 						type = Util.TowerType.recycle;
 
 					} else if ((addWindmillTower)) {
 
 						type = Util.TowerType.windmill;
 
 					} else if ((addMetalTower)) {
 						type = Util.TowerType.metal;
 					} else if ((addCompostTower)) {
 						type = Util.TowerType.compost;
 					} else if ((addNuclearTower)) {
 						
 						type = Util.TowerType.nuclear;
 						
 					}
 
 					boolean isValid = validTower(mouseX, mouseY, type);
 					gameBoard.pendingTower = new Tower(adjX, adjY, 1, 25, type,
 							isValid, currentTowerDirection);
 				}
 			//}
 			
 			if(Board.inBetweenLevels&&Board.messages.size()!=0)
 			{
 				Board.messages.remove(0);
 			}
 
 
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			// TODO Auto-generated method stub
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent e) {
 			// TODO Auto-generated method stub
 		}
 
 		@Override
 		public void mouseMoved(MouseEvent e) {
 			Util.TowerType type = null;
 
 			int mouseX = e.getPoint().x;
 			int mouseY = e.getPoint().y;
 
 			int adjX = mouseX - 20;
 			int adjY = mouseY - 20;
 
 			if (addInceneratorTower) {
 				type = Util.TowerType.incenerator;
 				boolean isValid = validTower(mouseX, mouseY, type);
 				gameBoard.pendingTower = new Tower(adjX, adjY, 1, 30, type,
 						isValid, currentTowerDirection);
 
 			} else if ((addRecycleTower)) {
 
 				type = Util.TowerType.recycle;
 				boolean isValid = validTower(mouseX, mouseY, type);
 				gameBoard.pendingTower = new Tower(adjX, adjY, 1, 30, type,
 						isValid, currentTowerDirection);
 
 			} else if ((addWindmillTower)) {
 
 				type = Util.TowerType.windmill;
 				boolean isValid = validTower(mouseX, mouseY, type);
 				gameBoard.pendingTower = new Tower(adjX, adjY, 1, 30, type,
 						isValid, currentTowerDirection);
 			} else if ((addMetalTower)) {
 
 				type = Util.TowerType.metal;
 				boolean isValid = validTower(mouseX, mouseY, type);
 				gameBoard.pendingTower = new Tower(adjX, adjY, 1, 30, type,
 						isValid, currentTowerDirection);
 			} else if ((addCompostTower)) {
 
 				type = Util.TowerType.compost;
 				boolean isValid = validTower(mouseX, mouseY, type);
 				gameBoard.pendingTower = new Tower(adjX, adjY, 1, 30, type,
 						isValid, currentTowerDirection);
 	
 			} else if ((addNuclearTower)) {
 
 				type = Util.TowerType.nuclear;
 				boolean isValid = validTower(mouseX, mouseY, type);
 				gameBoard.pendingTower = new Tower(adjX, adjY, 1, 30, type,
 						isValid, currentTowerDirection);
 	
 			}
 
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mouseExited(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 	}
 
 	private void resetButtons() {
 		addMetalTower = false;
 		addInceneratorTower = false;
 		addRecycleTower = false;
 		addWindmillTower = false;
 		addCompostTower = false;
 		addNuclearTower= false;
 		info.setText("");
 		towerInfo.setText("");
 	}
 
 	public boolean validTower(int x, int y, Util.TowerType type) {
 
 		if (gameBoard.inPath(x, y) || gameBoard.onTower(x, y)) {
 			return false;
 		}
 
 		if (gameBoard.getBudget() < getCost(type)) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public int getCost(Util.TowerType type) {
 		
 		switch(type){
 			case recycle:
 				return 200;
 			case incenerator:
 				return 100;
 			case windmill:
 				return 300;
 			case metal:
 				return 250;
 			case compost:
 				return 200;
 			case compactor:
 				return 500;
 			case nuclear:
 				return 400;
 			}
 		return 0;
 	}
 
 	@Override
 	public void run() {
 		long beforeTime, timeDiff, sleep;
 
 		beforeTime = System.currentTimeMillis();
 
 		while (true) {
 
 			long pause = 0;
 			repaint();
 			timeDiff = System.currentTimeMillis() - beforeTime;
 			sleep = DELAY - timeDiff;
 
 			if (sleep < 0)
 				sleep = 2;
 			try {
 				Thread.sleep(sleep);
 			} catch (InterruptedException e) {
 				System.out.println("interrupted");
 			}
 
 			beforeTime = System.currentTimeMillis();
 		}
 
 	}
 	public void setTowerInfoText(Tower tower){
 		towerInfo.setText("<html><center>Fire-Rate:" + tower.getRate()+" | Kills:" + tower.getKillCount()+ "</center></html>");
 	}
 	
 	public void setInfoText(Util.TowerType type){
 		switch(type){
 			case incenerator: info.setText("<html><center>Incenerators can handle many types of household trash, but have negative effects on air quality</center></html>"); break;
 			case metal:  info.setText("<html><center>The magnet is for picking up scrap metal</center></html>"); break;
 			case recycle: info.setText("<html><center>Recycle Bins are able to recycle paper, plastic, and aluminum and help to improve air quality</center></html>"); break;
 			case windmill: info.setText("<html><center>Windmills help to create clean energy for your town, therefore saving you money each round on energy costs</center></html>"); break;
 			case compost: info.setText("<html><center>Compost has the ability to properly dispose of food trash that comes through the level and provides more money than using another type of tower on this trash</center></html>"); break;
 			case nuclear: info.setText("<html><center>Nuclear waste centers are for disposing of nuclear waste before it reaches the landfill, they are the only towers that can handle this type of waste</center></html>"); break;
 		}
 	}
 
 	public void addNotify() {
 		super.addNotify();
 		menu = new Thread(this);
 		menu.start();
 	}
 
 }
