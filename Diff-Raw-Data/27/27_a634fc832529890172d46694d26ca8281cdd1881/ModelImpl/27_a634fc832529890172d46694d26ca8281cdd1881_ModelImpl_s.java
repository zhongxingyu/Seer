 package model;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 @Singleton
 public class ModelImpl implements IModel {
 
 	private Brick brick;
 	private Map map;
 
 	
 	
 
 	@Inject
 	public ModelImpl() {
 		
 		
 		brick = Brick.getInstance();
 		map = Map.getInstance();
 
 	}
 
 	@Override
 	public void resetBrick(int style) {
 		// TODO Auto-generated method stub
 		
 
 		brick.resetBrick(style);
 
 	}
 
 	@Override
 	public void turnBrick() {
 		// TODO Auto-generated method stub
 		brick.turn();
 	}
 
 	@Override
 	public boolean getBrickvalue(int x, int y) {
 
 		return brick.get(x, y);
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void addBrick() {
 		// TODO Auto-generated method stub
 		
 		map.addBrick(brick);
 
 	}
 
 	@Override
 	public boolean getMapValue(int x, int y) {
 		// TODO Auto-generated method stub
 
 		return map.get(x, y);
 	}
 
 	@Override
 	public void setMapValue(int x, int y, boolean value) {
 		// TODO Auto-generated method stub
 		map.set(x, y, value);
 	}
 
 	/**
 	 * @return the posX
 	 */
 	@Override
 	public int getPosX() {
 		return brick.getPosX();
 	}
 
 	/**
 	 * @param posX
 	 *            the posX to set
 	 */
 	@Override
 	public void setPosX(int posX) {
 		brick.setPosX(posX);
 	}
 
 	/**
 	 * @return the posY
 	 */
 	@Override
 	public int getPosY() {
 		return brick.getPosY();
 	}
 
 	/**
 	 * @param posY
 	 *            the posY to set
 	 */
 	@Override
 	public void setPosY(int posY) {
 		brick.setPosY(posY);
 	}
 
 	@Override
 	public int getScene() {
 		// TODO Auto-generated method stub
 		return brick.getScene();
 	}
 
 	@Override
 	public int getMapColor(int x, int y) {
 		// TODO Auto-generated method stub
 		return map.getMapColor(x, y);
 	}
 
 	@Override
 	public boolean[][] getNextForm() {
 		return brick.getNextForm();
 	}
 
 	@Override
 	public boolean valitateBrick(boolean[][] matrix, int x, int y) {
 		
 		for(int dy = 0; dy < 4; ++dy)
 		{
 			for(int dx = 0; dx < 4; ++dx)
 			{
 				if(matrix[dx][dy] 
 						&& x + dx > 0 && x + dx < 10
 						&& y + dy > 0 && y + dy < 18
 						&& map.get(x + dx, y + dy))
 				{
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public int getLineCount() {
 		// TODO Auto-generated method stub
 		return map.getLineCount();
 	}
 
 }
