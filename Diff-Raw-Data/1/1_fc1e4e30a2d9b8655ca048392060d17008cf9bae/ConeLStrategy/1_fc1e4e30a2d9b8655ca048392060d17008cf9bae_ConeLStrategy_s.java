 package ua.luxoft.odessa.tetris.impl.strategies;
 
 import ua.luxoft.odessa.tetris.api.IFigure.Coordinates;
 import ua.luxoft.odessa.tetris.api.IFigureCheckStrategy;
 import ua.luxoft.odessa.tetris.impl.Board;
 
 /*
  *  Cone Left
  * 
  *  Presentations: 
  *  
  *   down		 left 		 up			 right  
  *  - - - -	   - - - -		- - - -		- - - - 
  *  * - - -    * * * - 		- * * -		- - - -
  *  * - - -    * - - - 		- - * -		- - * -
  *  * * - -    - - - -		- - * -		* * * -
  *  
  * */
 
 public class ConeLStrategy implements IFigureCheckStrategy {
 	
 	public static final int WIDTH = 4;
 	private OrientationLURD mOrientation;
 	
 	public ConeLStrategy()
 	{
 		mOrientation = OrientationLURD.DOWN;
 	}
 	
 	private Boolean checkSpace(int xPos, int yPos, Board board)
 	{
 		for (int x = 0; x < 3; x++)
 			for (int y = 0; y < 3; y++)
 				if (board.getMap(xPos + x, yPos + y + 1))
 					return false;
 		return true;		
 	}
 	
 	@Override
 	public void checkUp(Coordinates coordinates, Board board) {
 		switch (mOrientation)
 		{
 		case DOWN:
 			if (coordinates.x + 2 >= Board.WIDTH)
 			{
 				if (checkSpace(coordinates.x - 1, coordinates.y, board) == false)
 					return;
 			}
 			else 
 				if (checkSpace(coordinates.x, coordinates.y, board) == false)
 					return;
 			break;
 		case LEFT:
 			if (checkSpace(coordinates.x, coordinates.y, board) == false)
 				return;
 			break;
 		case UP:
 			if (coordinates.x < 0)
 			{
 				if (checkSpace(coordinates.x + 1, coordinates.y, board) == false)
 					return;
 				coordinates.x++;
 			}
 			else 
 				if (checkSpace(coordinates.x, coordinates.y, board) == false)
 					return;
 			break;
 		case RIGHT:
 			if (checkSpace(coordinates.x,  coordinates.y, board) == false)
 				return;
 			break;
 		}
 		mOrientation = mOrientation.next();
 	}
 
 	@Override
 	public Boolean[][] getPresentation() {
 		Boolean[][] presentation = new Boolean[WIDTH][WIDTH];
 		switch (mOrientation)
 		{
 		case DOWN:
 			for (int i = 0; i < 3; i++)
 				presentation[0][i+1] = true;
 			presentation[1][3] = true;
 			break;
 		case LEFT:
 			for (int i = 0; i < 3; i++)
 				presentation[i][1] = true;
 			presentation[0][2] = true;
 			break;
 		case UP:
 			for (int i = 0; i < 3; i++)
 				presentation[2][i+1] = true;
 			presentation[1][1] = true;
 			break;
 		case RIGHT:
 			for (int i = 0; i < 3; i++)
 				presentation[i][3] = true;
 			presentation[2][2] = true;
 			break;
 		}
 		return presentation;
 	}
 
 }
