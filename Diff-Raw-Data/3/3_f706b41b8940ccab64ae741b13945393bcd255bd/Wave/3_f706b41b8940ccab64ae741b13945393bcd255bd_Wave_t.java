 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JMenuItem;
 //import javax.swing.JOptionPane;
 
 
 public class Wave {
 
 	public int time = 0; //the time that the wave will begin
 	private List<Enemy> waveEnemyList = new ArrayList<Enemy>();
 	public JMenuItem waveButton = null;
 	private Level parentLevel;
 	private ArrayList<Pair<String, Integer>> enemyCountList;
 	public EnemyPlacementGrid Grid;
 	
 	//constructor for wave
 	public Wave(int t, Level l, EnemyPlacementGrid epgRef){
 		time = t;
 		parentLevel = l;
 		Grid = epgRef;
 		//menu set up
 		waveButton = new JMenuItem(Integer.toString(t));  //button for a given wave
 		waveButton.addActionListener(new ActionListener(){ //by clicking on it, you change the current wave
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				//will make a call to load wave
 				System.out.println("Before: " + WaveScreen.currentWave);
 				loadWave();
 				System.out.println("The current Wave has been changed: " + WaveScreen.currentWave);
 			}
 		});
 	}
 	
 	public void loadWave()
 	{
 		//need to set the currentWave to this newly created wave
 		//when switching waves, remove all listeners corresponding to the placement grid
 		//Grid.clear();
 		//add event listeners to new enemies
 		WaveScreen.currentWave = this;
 		
 		//need to redraw
 		//for(int i = 0; i < waveEnemyList.size(); i++){
 			//Grid.paintSprite(waveEnemyList.get(i));
 		//}
 		repaint();
 		WaveScreen.currentLevel = parentLevel;
 	}
 	
 	public void repaint(){
 		Grid.clear();
 		for(int i = 0; i < waveEnemyList.size(); i++){
 			Grid.paintSprite(waveEnemyList.get(i));
 		}
 	}
 	
 	public List<Enemy> getWave(){
 		return waveEnemyList;
 	}
 	
 	public void addEnemy(Enemy e){
 		System.out.println("WAVE: " + time + " " + e.DEBUGPRINTSTRING());
 		waveEnemyList.add(e);
 	}
 	
 	public void removeEnemy(Enemy e){
 		System.out.println("Removing Enemy: " + e);
 		waveEnemyList.remove(e);
 	}
 	
 	public void addEnemyList(List<Enemy> enemyList){
 		waveEnemyList = enemyList;
 	}
 	
 	public String printOut(){
 		return waveEnemyList.toString();
 	}
 	
 	private Boolean typeExists(int i){
 		for (int j = 0; j < enemyCountList.size(); j++){
 			if (waveEnemyList.get(i).type == enemyCountList.get(j).key){
 				enemyCountList.get(j).value += 1;
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public String toString(){
 		enemyCountList = new ArrayList<Pair<String, Integer>>();
 		Pair<String, Integer> tempPair = new Pair<String, Integer>();
 		for (int i = 0; i < waveEnemyList.size(); i++){
 			if (i == 0){
 				tempPair.key = waveEnemyList.get(i).type;
 				tempPair.value = 1;
 				enemyCountList.add(tempPair.clone());
 			}
 			if (!typeExists(i)){
 				tempPair.key = waveEnemyList.get(i).type;
 				tempPair.value = 1;
 				enemyCountList.add(tempPair.clone());
 			}
 			
 		}
 		String printedLine = "";
 		printedLine += ("Time=" + Integer.toString(time));
 		//printedLine += " ";
 		for (int i = 0; i < enemyCountList.size(); i++){
			enemyCountList.get(i).value--;
			printedLine += (" " + (enemyCountList.get(i)));
 		}
 		printedLine += ("\n");
 		for(int i = 0; i < waveEnemyList.size(); i++){
 			printedLine += waveEnemyList.get(i);
 		}
 		return printedLine;
 	}
 	
 	public void clearObject(){
 		for(int i = 0; i < waveEnemyList.size(); i++){
 			waveEnemyList.get(i).clearObject();
 		}
 		if(waveEnemyList != null){
 			waveEnemyList = null;
 		}
 		this.Grid = null;
 		this.waveButton = null;
 	}
 	
 }
