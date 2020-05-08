 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.event.MouseInputListener;
 
 
 
 public class Menu extends JPanel implements Runnable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5237335232850181080L;
 	private Thread menu;
 	private final int DELAY = 50;
 	private GridLayout layoutMGR =new GridLayout(8,1,10,10);
 	private Board gameBoard;
 	static private boolean addRecycleTower=false;
 	static private boolean addInceneratorTower=false;
 	static private boolean addWindmillTower=false;
 	private boolean startWave=false;
 	private String currentTowerDirection="South";
 
 	public Menu(Board board){
 		
 	    	gameBoard=board;
 		board.addMouseListener(new Mouse());
 		board.addMouseMotionListener(new Mouse());
 		
 		setLayout(layoutMGR);
 		//ActionListener AL=new ActionListener();
 		JButton recycleButton=new JButton("Add Recycling-$200");
 		recycleButton.addActionListener(new RecycleButtonListener());
 		
 		JButton inceneratorButton=new JButton("Add Incenerator-$100");
 		inceneratorButton.addActionListener(new InceneratorButtonListener());
 		
 		JButton muteButton=new JButton("Mute");
 		muteButton.addActionListener(new MuteButtonListener());
 		
 		
 		JButton startWaveButton=new JButton("Send Next Wave");
 		startWaveButton.addActionListener(new StartWaveButtonListener());
 		
 		JButton buyWindmillButton= new JButton("Add Windmill-$300");
 		buyWindmillButton.addActionListener(new WindmillButtonListener());
 		
 		add(recycleButton);
 		add(inceneratorButton);
 		add(buyWindmillButton);
 		add(startWaveButton);
 		add(muteButton);
 
 
 	}
 	
 	private class StartWaveButtonListener implements ActionListener{
 		
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 		    	
 			startWave=!startWave;
 			gameBoard.startWave();
 		}
 	}
 	private class MuteButtonListener implements ActionListener{
 		
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 		    if(gameBoard.isMuted()){
 			gameBoard.startMusic();
 		    }else{
 			gameBoard.stopMusic();
 		    }
 		    
 		}
 	}
 	
 	private class WindmillButtonListener implements ActionListener{
 		
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 		    if(gameBoard.getBudget()>=getCost(Util.TowerType.windmill)){
 			addWindmillTower=!addWindmillTower;
 			addInceneratorTower=false;
 			addRecycleTower=false;
 		    }
 		}
 	}
 	
 	private class RecycleButtonListener implements ActionListener{
 	
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 		    if(gameBoard.getBudget()>=getCost(Util.TowerType.recycle)){
 			addRecycleTower=!addRecycleTower;
 			addInceneratorTower=false;
 			addWindmillTower=false;
 		    }
 		}
 	}
 	
 	private class InceneratorButtonListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 		    if(gameBoard.getBudget()>=getCost(Util.TowerType.incenerator)){
 			addInceneratorTower=!addInceneratorTower;
 			addRecycleTower=false;
 			addWindmillTower=false;
 			
 		    }
 		}
 	}
 	
 	private class Mouse implements MouseInputListener{
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 						
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			// TODO Auto-generated method stub
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 			// TODO Auto-generated method stub
 		}
 
 		@Override
 		public  void mousePressed(MouseEvent e) {
 		    
 		    int mouseX=e.getPoint().x;
 		    int mouseY=e.getPoint().y;
 		    
 		    int adjX=mouseX-20;
 		    int adjY=mouseY-20;
 		    
 		    //Detect a left click
 		    if(e.getButton()==1){
 			
 			 Util.TowerType type=null;
 			 
 			    
 			 if(addInceneratorTower)
 			 {
 			     type=Util.TowerType.incenerator;
    			     addInceneratorTower=false;
     						    
     			 }else if((addRecycleTower)){    
     			     type=Util.TowerType.recycle;
    			     addRecycleTower=false;
     				    
     			 }else if((addWindmillTower)){ 
     				    
     			     type=Util.TowerType.windmill;
    			     addWindmillTower=false;
     			 }
 			 
 			 boolean isValid=validTower(mouseX, mouseY, type);   
         		 
			 gameBoard.addTower(new Tower(adjX,adjY,1,25,type,isValid,currentTowerDirection));
			 gameBoard.removeMoney(getCost(type));   
        		 gameBoard.pendingTower=null;
         			
         	    //Detect a right click		
 		    }else if(e.getButton()==3){
 			
 			currentTowerDirection=gameBoard.pendingTower.rotateDir();
 						
 			Util.TowerType type=null;
 			 
 			    
 			 if(addInceneratorTower)
 			 {
 			     type=Util.TowerType.incenerator;
    			    
    						    
    			 }else if((addRecycleTower)){    
    			     type=Util.TowerType.recycle;
    			     
    				    
    			 }else if((addWindmillTower)){ 
    				    
    			     type=Util.TowerType.windmill;
    			    
    			 }
 			 
 			 boolean isValid=validTower(mouseX, mouseY, type);   
 			 gameBoard.pendingTower=new Tower(adjX,adjY, 1, 25, type, isValid, currentTowerDirection);
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
 			
 		    Util.TowerType type=null;
 		    
 		    int mouseX=e.getPoint().x;
 		    int mouseY=e.getPoint().y;
 		    
 		    int adjX=mouseX-20;
 		    int adjY=mouseY-20;
 		    
 		    
 		    
 		    	if(addInceneratorTower)
 			{
 		    	    type=Util.TowerType.incenerator;
 		    	    boolean isValid=validTower(mouseX, mouseY, type);
 			    gameBoard.pendingTower=new Tower(adjX,adjY,1,30,type,isValid,currentTowerDirection);
 				
 			    
 			}else if((addRecycleTower)){ 
 			    
 			    type=Util.TowerType.recycle;
 			    boolean isValid=validTower(mouseX, mouseY, type);
 			    gameBoard.pendingTower=new Tower(adjX,adjY,1,30,type,isValid,currentTowerDirection);
 			    
 			}else if((addWindmillTower)){ 
 			    
 			    type=Util.TowerType.windmill;
 			    boolean isValid=validTower(mouseX, mouseY, type);
 			    gameBoard.pendingTower=new Tower(adjX,adjY,1,30,type,isValid,currentTowerDirection);
 			}
 		    
 		    
 		    
 				
 		}
 	}
 	public boolean validTower(int x, int y, Util.TowerType type){
 	    
 	    if(gameBoard.inPath(x, y)){
 		return false;
 	    }
 	    
 	    if(gameBoard.getBudget()<getCost(type)){
 		return false;
 	    }
 	    
 	    
 	    return true;
 	}
 	
 	
 	public int getCost(Util.TowerType type){
 		if(type==Util.TowerType.recycle){
 		    return 200;
 		}else if(type==Util.TowerType.incenerator){
 		    return 100;
 		}else if(type==Util.TowerType.windmill){
 		    return 300;
 		}else{
 		    return 0;
 		}
 	    }
 
 	@Override
 	public void run() {
 	    long beforeTime, timeDiff, sleep;
             
 	        beforeTime = System.currentTimeMillis();
 	        
 	           
 	        
 	        
 	        while (true) {
 	            
 	            long pause=0;
 	        	
 	            
 	          
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
 	
 	public void addNotify() {
 	        super.addNotify();
 	        menu = new Thread(this);
 	        menu.start();
 	}
 	
 
 }
