 package models;
 import java.util.ArrayList;
 import java.util.Random;
 
 import util.datastructures.TilePoint;
import util.interfaces.DataUpdated_Listener;
 import util.interfaces.MoreLines;
 
 import models.tiles.LBlock;
 import models.tiles.LinePiece;
 import models.tiles.RevLBlock;
 import models.tiles.RevSquigeling;
 import models.tiles.Square;
 import models.tiles.Squigeling;
 import models.tiles.TBlock;
 import models.tiles.Tile;
 
 public class Board {
 	private Random ran = new Random();
 	public int length = 16;
 	public int height = 30;
 	public int[][] field = new int[length+2][height+2];
 		// rand um feld, +2 je dimensin, damit jedes teilchen ab beginn drehbar,
 		// bzw , damit man prfen kann: figur ist drehbar?
 		// field[i][j] = -1, wenn feld == rand
 		//				  0, wenn feld nicht belegt
 		//				  1, wenn feld belegt
 	
 	public int[][] field_setfix = new int[length+2][height+2];
 	public Tile current_figure;
 	private int line_candidate_index;
 	public int points = 0;
 	public int line_count = 0;
 
 	
 	public Board(){
 		// initialisiere Rand mit -1
 		for(int i = 0; i < length+2; i++){
 			field[i][0] = -1;
 			field[i][height+1] = -1;
 		}
 		
 		for(int i = 1; i < height+2; i++){
 			field[0][i] = -1;
 			field[length+1][i] = -1;    
 		}
 	}
 	
 	/**
 	 * Private methods 
 	 **/
 	
 	private void move_figure(Tile figure, int x, int y){
 		// verschiebt eine gegebene figur auf neue koordinaten
 		// vor dem verschieben noch prfen 
 		// ob figur berhaupt verschiebbar ist.
 		delete_figure(figure);
 		figure.move(new TilePoint(x, y));
 		set_figure(figure);
 	}
 	
 	private void rotate_figure(Tile figure){
 		// drehe gegebene figur im uhrzeigersinn
 		// vor drehen, teste
 		// ob figur berhaupt drehbar ist
 		delete_figure(figure);
 		figure.rotate();
 		set_figure(figure);
 	}
 	
 	/**
 	 * tests
 	 */
 	
 	private boolean movable(Tile figure, int x, int y){
 		// kann aktuelle figur berhaupt verschoben werden?
 		// lege dafr kopie von aktueller figur an und teste
 		// ob diese verschiebbar ist, dh gltige position auf field aufweist.
 		figure.move(new TilePoint(x, y));
 		boolean check = setzbar(figure);
 		figure.move(new TilePoint(-x, -y));
 		return check;
 	}
 	
 	private boolean rotatable(Tile figure){
 		// kann aktuelle figur berhaupt gedreht werden?
 		// lege dafr kopie von aktueller figur an und teste
 		// ob diese drehbar ist, dh gltige position auf field aufweist.
 		
 		figure.rotate();
 		boolean check = setzbar(figure);
 		figure.invRotate();
 		return check;
 	}
 	
 	private boolean setzbar(Tile figure){
 		// kann figur an aktueller position gsetzt werden?
 		int color = current_figure.getColor();
 
 		int fx0 = figure.getAt(0, 0);
 		int fy0 = figure.getAt(0, 1);
 		int fx1 = figure.getAt(1, 0);
 		int fy1 = figure.getAt(1, 1);
 		int fx2 = figure.getAt(2, 0);
 		int fy2 = figure.getAt(2, 1);
 		int fx3 = figure.getAt(3, 0);
 		int fy3 = figure.getAt(3, 1);
 		
 		boolean result = (field[fx0][fy0] == color || field[fx0][fy0] == 0) &&
 						 (field[fx1][fy1] == color || field[fx1][fy1] == 0) &&
 						 (field[fx2][fy2] == color || field[fx2][fy2] == 0) &&
 						 (field[fx3][fy3] == color || field[fx3][fy3] == 0);
 		
 		boolean flag2 = (field_setfix[fx0][fy0] != 1) &&
 						(field_setfix[fx1][fy1] != 1) &&
 						(field_setfix[fx2][fy2] != 1) &&
 						(field_setfix[fx3][fy3] != 1);
 		
 		return result && flag2;
 	}
 	
 	private boolean is_full_line(){
 		// for each line in the board
 		// test, if there is a complete line
 		// remember the position
 		
 		boolean flag1 = false;
 		boolean line_found = true;
 		int line_candidate = -1;
 		for(int i = 1; i < height+1; i++ ){
 			for(int j = 1; j < length+1; j++){
 			
 				if(field_setfix[j][i] == 0){
 					line_found = false;
 					break;
 				}
 			}
 		
 			if(line_found){
 				flag1 = true;
 				line_candidate = i;
 				break;
 			}else{
 				line_found = true;
 			}
 		}
 		if(line_candidate != -1) line_candidate_index = line_candidate;
 		return flag1;
 	}
 	
 	private boolean is_double_line(){
 		//for each line in the board
 		// test if there are two consecutive lines 
 		
 		boolean flag = true;
 		if(line_candidate_index <= height){
 			for(int i = 1; i < length; i++){
 				if(field_setfix[i][line_candidate_index+1] == 0){
 					flag = false;
 					break;
 				}
 			}
 		}
 		return flag;
 	}
 	
 	private boolean is_tribble_line(){
 		//for each line in the board
 		// test if there are three consecutive lines
 		
 		boolean flag = true;
 		if(line_candidate_index <= height){
 			for(int i = 1; i < length+1 + 1; i++){
 				if(field_setfix[i][line_candidate_index+2] == 0){
 					flag = false;
 					break;
 				}
 			}
 		}
 		return flag;
 	}
 	private boolean is_tetris(){
 		//for each line in the board
 		// test if there are four consecutive lines 
 		
 		boolean flag = true;
 		if(line_candidate_index <= height){
 			for(int i = 1; i < length + 1; i++){
 				if(field_setfix[i][line_candidate_index+3] == 0){
 					flag = false;
 					break;
 				}
 			}
 		}
 		return flag;
 	}
 	
 	public boolean zero_line_fixed(){
 		// hat eis auf dem oberen rand steine?
 		// wenn ja, dann verloren
 		boolean flag = false;
 		for(int i = 1; i < length + 1; i++){
 			if(field_setfix[i][0] == 1){
 				flag = true;
 				break;
 			}
 		}
 		if(flag)System.out.print("have a lose");
 		return flag;
 	}
 	
 	/**
 	 * handle figure
 	 */
 	
 	private void delete_figure(Tile figure){
 		updateField(field, figure, 0);
 	}
 	
 	private void set_figure(Tile figure){
 		updateField(field, figure, figure.getColor());
 	}
 	
 	private void updateField(int[][] base, Tile figure, int value){
 		int fx0 = figure.getAt(0, 0);
 		int fy0 = figure.getAt(0, 1);
 		int fx1 = figure.getAt(1, 0);
 		int fy1 = figure.getAt(1, 1);
 		int fx2 = figure.getAt(2, 0);
 		int fy2 = figure.getAt(2, 1);
 		int fx3 = figure.getAt(3, 0);
 		int fy3 = figure.getAt(3, 1);
 		
 		
 		base[fx0][fy0] = value;
 		base[fx1][fy1] = value;
 		base[fx2][fy2] = value;
 		base[fx3][fy3] = value;
 	}
 	
 	private void lay_donw(Tile figure){
 		figure.addColor(8);
 		set_figure(figure);
 	}
 	
 	
 	private void delete_line(int index, int count){
 		// lsche eine ganze zeile
 		for(int k = index; k < index + count; k++){
 			for(int i = 1; i < length+1; i++ ){
 				field_setfix[i][index] = 0;
 				field[i][index] = 0;
 			}
 		}
 	}
 	
 	private void move_other_down(int n){
 		// bewegt steine an richtige position nach eiflsen von k-zeilen
 		field_setfix = copy_matry_do_translation(field_setfix, line_candidate_index, line_candidate_index+n);
 		field = copy_matry_do_translation(field, line_candidate_index, line_candidate_index+n);
 		
 		// stelle invis rand um board wieder her
 		for(int i = 0; i < length+2; i++){
 			field[i][0] = -1;
 			field[i][height+1] = -1;
 		}
 		
 		for(int i = 1; i < height+2; i++){
 			field[0][i] = -1;
 			field[length+1][i] = -1;    
 		}
 
 	}
 	
 	private int[][] copy_matry_do_translation(int[][] source,int start_index, int end_index){
 		// copy matrix
 		// verschiebe alles ab a nach k zeilen nach unten
 		// swap translated array mit original array
 		
 		int[][] tmp = new int[length+2][height+2]; // neue werte
 		
 		// unterer teil
 		for(int i = end_index; i < height+1; i++ ){
 			for(int j = 1; j < length+1; j++){
 				tmp[j][i] = source[j][i];
 			}
 			System.out.println(i);
 		}
 		
 		int distance = (end_index - start_index) -1;
 		
 		// oberer teil, der nach unten gemoves werden muss
 		for(int i = 1; i < start_index; i++ ){
 			for(int j = 1; j < length+1; j++){	
 				tmp[j][i+distance+1] = source[j][i];
 			}
 		}
 		source = null;
 		return tmp;
 	}
 	
 	/**
 	 * Public methods 
 	 * Schnittstellenmethoden fr kommunikation von aussen.
 	 **/
 	
 	public synchronized void nach_unten(){
 		// wemm keine kollision 
 		// bewege gegebene figur um eine einheit nach unten
 		// sonst fixiere figur an position,
 		// teste ob nun mind eine zeile aufgelst werden kann
 		// spawn die nchste figur und informiere den controller.
 		//System.out.println("Thread id " + Thread.currentThread().getId());
 		if(movable(current_figure, 0, 1)){
 			move_figure(current_figure, 0, 1);
 		}else{
 			this.updateField(field_setfix, current_figure, 1);
 			while(true){
 				if(is_full_line()){
 					if(is_double_line()){
 						if(is_tribble_line()){
 							if(is_tetris()){
 								delete_line(line_candidate_index, 4);
 								points += 150;
 								line_count += 4;
 								move_other_down(4);
 								notify_line_observers();
 							}else{
 								delete_line(line_candidate_index, 3);
 								points += 70;
 								line_count += 3;
 								move_other_down(3);
 								notify_line_observers();
 							}	
 						}else{
 							delete_line(line_candidate_index, 2);
 							points += 30;
 							line_count +=2;
 							move_other_down(2);
 							notify_line_observers();
 						}
 					}else{
 						// wenn wir nur eine full line haben,
 						// dann kille diese nun
 						delete_line(line_candidate_index, 1);
 						points += 10;
 						line_count +=1;
 						move_other_down(1);
 						notify_line_observers();
 					}
 				}else{
 					break;	
 				}
 			}
 			//synchronized(this){
 			//	System.out.println("new one");
 			new_figure();
 			//}
 		}
 		notify_observers();
 	}
 	
 	public void nach_rechts(){
 		// wenn mglich, dann bewege aktuelle figur nach rechts
 		if(movable(current_figure, 1, 0))
 			move_figure(current_figure, 1, 0);
 		notify_observers();
 	}
 	
 	public void nach_links(){
 		// wenn mglich, dann bewege aktuelle figur nach links
 		if(movable(current_figure, -1, 0))
 			move_figure(current_figure, -1, 0);
 		notify_observers();
 	}
 	
 	public void rotate(){
 		// wenn mglich rotiere aktuelle figur
 		if(rotatable(current_figure) && current_figure.getColor() != 11){
 			rotate_figure(current_figure);
 		}	
 		notify_observers();
 	}
 	
 	public void new_figure(){
 
 
 		int i = ran.nextInt() % 7;
 				
 		switch (i) {
 		case 0:
 			current_figure = new LinePiece();
 			break;
 			
 		case 1:
 			current_figure = new TBlock();
 			break;
 			
 		case 2:
 			current_figure = new Square();
 			break;
 			
 		case 3:
 			current_figure = new Squigeling();
 			break;
 			
 		case 4:
 			current_figure = new RevSquigeling();
 			break;
 			
 		case 5:
 			current_figure = new LBlock();
 			break;
 			
 		case 6:
 			current_figure = new RevLBlock();
 			break;
 		}	
 		
 		
 		
 		lay_donw(current_figure);
 		notify_observers();
 	}
 	
 	public void fall(){
 		// instand nach unten fallen lassen, bis kollision
 		while(true){
 			if(movable(current_figure, 0, 1))
 				move_figure(current_figure, 0, 1);
 			else break;
 		}
 		notify_observers();
 	}
 	
 	
 	/**
 	 * for reset
 	 **/
 	
 	
 	public void clear_all(){
 		// reset alle felder der matrizen zum default wert
 		// informiere controller
 		
 		current_figure = null;
 		for(int i = 0; i < length+1; i++)
 			for(int j = 0; j < height+1; j++){
 				field[i][j] = 0;
 				field_setfix[i][j] = 0;
 			}
 		
 		for(int i = 0; i < length+2; i++){
 			field[i][0] = -1;
 			field[i][height+1] = -1;
 		}
 		
 		for(int i = 1; i < height+2; i++){
 			field[0][i] = -1;
 			field[length+1][i] = -1;    
 		}
 		points = 0;
 		line_count = 0;
 		line_candidate_index = 0;
 		notify_observers();
 	}
 	
 	/*************************************************
 	 * Observer Pattern
 	 *************************************************/
 	
	private ArrayList<DataUpdated_Listener> listeners = new ArrayList<DataUpdated_Listener>();
 	
 	public void notify_observers(){
 		// call this method everytime we have new date for our observers rdy.
 		// that mean, everytime our computation thread has produced new data
 		
 		for(int i = 0; i < listeners.size(); i++){
 			listeners.get(i).handleEvent();
 		}
 	}
 	
	public void subscribeListener(DataUpdated_Listener l) {
 		listeners.add(l);
 	}
 	
	public void unsubscribeListener(DataUpdated_Listener l) {
 		listeners.remove(l);
 	}	
 	
 	
 	/*************************************************
 	 * Observer Pattern
 	 *************************************************/
 	
 	private ArrayList<MoreLines> line_listeners = new ArrayList<MoreLines>();
 	
 	public void notify_line_observers(){
 		// call this method everytime we have new date for our observers rdy.
 		// that mean, everytime our computation thread has produced new data
 		
 		for(int i = 0; i < line_listeners.size(); i++){
 			line_listeners.get(i).handle_line_Event();
 		}
 	}
 	
 	public void subscribe_line_Listener(MoreLines l) {
 		line_listeners.add(l);
 	}
 	
 	public void unsubscribe_line_Listener(MoreLines l) {
 		line_listeners.remove(l);
 	}
 }
