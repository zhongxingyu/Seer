 package model;
 
 /**
  * Das Spielfeld als Bestandteil des Satzes. GameField bildet das zweidimensionale Spielfeld in Form eines Arrays ab und platziert neue Zge.
  * 
  * Als Boolean:
  * true = x
  * false = o
  * null = kein Stein gesetzt
  * @author Sascha Ulbrich
  *
  */
 
 
 import core.Constants;
 
 public class GameField{
 	private Boolean[][] field;
 	
 	/**
 	 * Konstruktor von GameField 
 	 *  
 	 * @param cols Spaltenanzahl 
 	 * @param rows Zeilenanzahl des Spielfelds
 	 */
 	public GameField(int cols, int rows){
 		field = new Boolean[cols][rows];
 	}
 	
 	/**
 	 * Methode zum Hinzufgen eines Zuges  
 	 *  
 	 * @param move der neue Zug
 	 */
	public void addMove(Move move){		
 		int col = move.getColumn();
		if(col < field.length && col > -1){
			for(int i=0; i<field[col].length; i++){
				if (field[col][i] == null){
					if(move.getRole() == Constants.xRole)
						field[col][i] = true;
					else
						field[col][i] = false;
					break;
				}
 			}
		}
 	}
 	
 	/**
 	 * Methode zum Entfernen eines Zuges  
 	 *  
 	 * @param move der zu entfernende Zug
 	 */
 	public void removeMove(Move move){
 		int col = move.getColumn();
 		for(int i=field[col].length-1; i>-1; i--){
 			if (field[col][i] != null){
 				field[col][i] = null;
 				break;
 			}
 		}		
 	}
 
 	/**
 	 * @return Spielfeld mit Boolean fr jedes Feld (true = xRole|false = oRole|null = noRole)
 	 */
 	public Boolean[][] getField(){
 		Boolean[][] array = new Boolean[field.length][field[0].length];
 		for(int i = 0; i < field.length; i++){
 			for(int j = 0; j< field[0].length; j++){
 				array[i] = field[i].clone();
 			}				
 		}
 		return array;
 	}
 }
