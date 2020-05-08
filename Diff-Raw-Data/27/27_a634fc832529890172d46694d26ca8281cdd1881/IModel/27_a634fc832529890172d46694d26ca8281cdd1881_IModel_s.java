 package model;
 
 /**
  * 
  */
 
 import com.google.inject.ImplementedBy;
 
 /**
  * @author Bombilla
  * 
  */
 @ImplementedBy(ModelImpl.class)
 public interface IModel {
 
 	int getMapColor(int x, int y);
 
 	int getScene();
 
 	void resetBrick(int style);
 
 	void turnBrick();
 
 	boolean getBrickvalue(int x, int y);
 
 	void addBrick();
 
 	boolean getMapValue(int x, int y);
 
 	void setMapValue(int x, int y, boolean value);
 
 	/**
 	 * @param posX
 	 *            the posX to set
 	 */
 	void setPosX(int posX);
 
 	/**
 	 * @param posY
 	 *            the posY to set
 	 */
 	void setPosY(int posY);
 
 	/**
 	 * @return the posX
 	 */
 	int getPosX();
 
 	/**
 	 * @return the posY
 	 */
 	int getPosY();
 
 	boolean[][] getNextForm();
 	
 	boolean valitateBrick(boolean[][] matrix, int x, int y);
 	
 	int getLineCount();
 }
