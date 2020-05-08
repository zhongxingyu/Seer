 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 
 public class Grid{
 	public static int GRID_WIDTH;
 	public static int GRID_HEIGHT;
 	
 	public Gem[][] _grid;
 	public Explosion[][] _explosions;
 	public boolean[][] _toRemove;
 	
 	public Grid(int _width, int _height){
 		GRID_WIDTH = _width;
 		GRID_HEIGHT = _height;
 		_grid = new Gem[GRID_WIDTH][GRID_HEIGHT];
 		_toRemove = new boolean[GRID_WIDTH][GRID_HEIGHT];
 		_explosions = new Explosion[GRID_WIDTH][GRID_HEIGHT];
 		for(int x = 0; x < GRID_WIDTH; x++){
 			for(int y = 0; y < GRID_HEIGHT; y++){
 				_grid[x][y] = new Gem(x, y);
 				_toRemove[x][y] = false;
 				_explosions[x][y] = new Explosion(x,y);
 			}
 		}
 		
 	}
 	
 	public void update(){
 		for(int x = 0; x < GRID_WIDTH; x++){
 			for(int y = 0; y < GRID_HEIGHT; y++){
 				_grid[x][y].update();
 				_explosions[x][y].update();
 			}
 		}
 	}
 	
 	public void draw(Graphics2D g){
 		for(int x = 0; x < GRID_WIDTH; x++){
 			for(int y = 0; y < GRID_HEIGHT; y++){
 				_grid[x][y].draw(g);
 				_explosions[x][y].draw(g);
 			}
 		}
 	}
 	
 	public void setGem(Gem a, int x, int y){
 		_grid[x][y] = a;
 	}
 	
 	public Gem getGem(int x, int y){
 		return _grid[x][y];
 	}
 
 	public void switchGems(int x1, int y1, int x2, int y2){
 		Gem temp = _grid[x1][y1];
 		_grid[x1][y1] = _grid[x2][y2];
 		_grid[x2][y2] = temp;
 
 	}
 	
 	public int searchRow(int y){
 		int count2 = 1;
 		int count = 1;
 		int ans = 0;
 		boolean isFirst = true;
 		String temp = null;
 		
 		for(int i = 0; i <= GRID_WIDTH; i++){
			if (i < GRID_WIDTH){
 				if (_grid[i][y].isSliding() || _grid[i][y].isFalling())
 					break;
			}
			
			if (i < GRID_WIDTH && _grid[i][y].getType().equals(temp)){
				count++;
 			} 
 			
 			if (i == GRID_WIDTH || !_grid[i][y].getType().equals(temp)){ // this needs to be changed to include groups of 3's at the ends;
 				if (count >= 3){
 					if (isFirst)
 						count2 = count;
 					//make gems fall here
 					for (int j = i-count; j < i; j++){
 						_toRemove[j][y] = true;
 					
 					}
 					isFirst = false;
 				}
 				if (i < GRID_WIDTH)
 					temp = _grid[i][y].getType();
 				
 				count = 1;
 			}
 			
 		}
 		if (count >= 3){
 			ans += count * 10;
 		}
 		if (count2 >= 3){
 			ans += count2 * 10;
 		}
 		return ans;
 	}
 	
 	public int searchCol(int x){
 		int count2 = 1;
 		int count = 1;
 		int ans = 0;
 		boolean isFirst = true;
 		String temp = null;
 		
 		for(int i = 0; i <= GRID_HEIGHT; i++){
			if (i < GRID_HEIGHT){
 				if (_grid[x][i].isSliding() || _grid[x][i].isFalling())
 					break;
			}
			if (i < GRID_HEIGHT && _grid[x][i].getType().equals(temp)){
				count++;
 			} 
 			if (i == GRID_HEIGHT || !_grid[x][i].getType().equals(temp)){
 				if (count >= 3){
 					if (isFirst)
 						count2 = count;
 					//make gems fall here
 					for (int j = i-1; j >= i-count; j--){
 					
 						_toRemove[x][j] = true;
 						
 					}
 					isFirst = false;
 				}
 				if(i < GRID_HEIGHT)
 					temp = _grid[x][i].getType();
 				count = 1;
 			}
 			
 		}
 		if (count >= 3){
 			ans += count * 10;
 		}
 		if (count2 >= 3){
 			ans += count2 * 10;
 		}
 		return ans;
 	}
 	
 	public void removeGems(){
 
 		int count = 0;
 		
 		for (int x = 0; x < GRID_WIDTH; x++){
 			count = 0;
 			for (int y = GRID_HEIGHT - 1; y >= 0; y--){
 				_grid[x][y].fallDown(count);
 				_grid[x][y+count] = _grid[x][y];
 				if (_toRemove[x][y]){
 					_toRemove[x][y] = false;
 					_explosions[x][y].explode();
 					count++;
 				}
 			}
 			for (int len = count-1; len >= 0; len--){
 				_grid[x][len] = new Gem(x,len);
 			}
 		}
 		
 		
 	}
 	
 	
 }
 
 
 
 
