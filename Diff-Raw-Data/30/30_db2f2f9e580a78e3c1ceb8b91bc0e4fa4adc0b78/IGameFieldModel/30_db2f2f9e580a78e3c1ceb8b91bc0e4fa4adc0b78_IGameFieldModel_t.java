 package org.holbreich.lfgm.model;
 
 import org.eclipse.swt.graphics.Point;
 
 /**
  * Common interface for a Life Game field.
  * 
  * @author Alexander Holbreich (http://alexander.holbreich.org)
  * @version $Rev: 1 $, ${date}$
  */
 public interface IGameFieldModel {
 
 	/**
 	 * @return model with in cells.
 	 */
 	int getWidth();
 
 	/**
 	 * @return model height in cells.
 	 */
 	int getHeight();
 
 	/**
 	 * 
 	 */
 	void nextTurn();
 
 	/**
 	 * 
 	 * @return
 	 */
 	boolean isGameOver();
 
 	
 	void addListener(IModelChangeListener mainGameView);
 
 	/**
 	 * 
 	 * @param reverserTranslate
 	 */
 	void setAlifeAt(Point reverserTranslate);
 
 	/**
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	boolean isAlife(int x, int y);
 
	void reset();

 }
