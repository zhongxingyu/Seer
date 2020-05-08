 package renderers;
 
 import java.awt.Rectangle;
 
 import managers.SelectTileManager;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.me.mygdxgame.Mule;
 
 /**
  * This will represent the GUI to show who has what at the top of the screen
  * @author antonio
  *
  */
 public class PlayerSelectionGui {
 
	private final int HEIGHT = 100;
 	private final int WIDTH = Mule.WIDTH;
 	
 	private int BOX_WIDTHS;
 	
 	private SelectTileManager manager;
 	
 	private BitmapFont font;
 	
 	private int lastX;
 	private int lastY;
 	
 	public PlayerSelectionGui(SelectTileManager m){
 		super();
 		manager = m;
 		BOX_WIDTHS = WIDTH / Mule.pm.getNumberOfPlayers();
 		font = new BitmapFont();
 		lastX = -100;
 		lastY = -100;
 	}
 	
 	public void draw(SpriteBatch batch){
 			
 		ShapeRenderer sr = new ShapeRenderer();
 				
 		drawBackground(sr, batch);
 	
 		drawData(sr, batch);
 	}
 	
 	private void drawButton(ShapeRenderer sr, SpriteBatch batch, int i){
 		int size = 25;
 		
 		sr.begin(ShapeType.Filled);
 		sr.setColor(Color.WHITE);
 		sr.rect((BOX_WIDTHS * (i + 1)) - size, Mule.HEIGHT - HEIGHT, size, size);
 		sr.end();
 		
 		sr.begin(ShapeType.Line);
 		sr.setColor(Color.BLACK);
 		sr.rect((BOX_WIDTHS * (i + 1)) - size, Mule.HEIGHT - HEIGHT, size, size);
 		if(manager.getPlayerDone(i)){
 			sr.line((BOX_WIDTHS * (i+1)) - size, Mule.HEIGHT - HEIGHT, (BOX_WIDTHS * (i+1)), Mule.HEIGHT - HEIGHT + size);
 		}
 		sr.end();
 	}
 	
 	private void drawData(ShapeRenderer sr, SpriteBatch batch){
 		
 		for(int i = 0 ; i < Mule.pm.getNumberOfPlayers() ; i ++){
 			batch.begin();
 			
 			batch.setColor(Color.BLACK);
 			font.draw(batch, Mule.pm.getPlayer(i).getName(), i * BOX_WIDTHS, Mule.HEIGHT - 20);
 			font.draw(batch, "MONEY : " + Mule.pm.getPlayer(i).getMoney(), i * BOX_WIDTHS + 40, Mule.HEIGHT - 50);
 			
 			batch.end();
 			
 			sr.begin(ShapeType.Filled);
 			sr.setColor(Mule.pm.getPlayer(i).getColor());
 			sr.rect(i * BOX_WIDTHS, Mule.HEIGHT - 80, 30, 30);
 			sr.end();
 			
 			drawButton(sr, batch, i);
 		}
 
 	}	
 	
 	private void drawBackground(ShapeRenderer sr, SpriteBatch batch){
 		batch.end();
 		
 		sr.begin(ShapeType.Filled);
 		sr.setColor(Color.GRAY);
 		sr.rect(0, Mule.HEIGHT - HEIGHT, WIDTH, HEIGHT);
 		sr.end();
 		
 		sr.begin(ShapeType.Line);
 		sr.setColor(Color.BLACK);
 		sr.rect(0, Mule.HEIGHT - HEIGHT, WIDTH, HEIGHT);
 		for(int i = 1 ; i < Mule.pm.getNumberOfPlayers() ; i ++){
 			sr.line(i * BOX_WIDTHS, Mule.HEIGHT, i * BOX_WIDTHS, Mule.HEIGHT - HEIGHT);
 		}
 		sr.end();
 	}
 	
 	public void mouseClicked(int x, int y){
 		if(x == lastX && y == lastY){
 			return;
 		}
 		lastX = x;
 		lastY = y;
 		Rectangle rect;
 		int size = 25;
 		for(int i = 0 ; i < Mule.pm.getNumberOfPlayers() ; i ++){
 			rect = new Rectangle((BOX_WIDTHS * (i + 1)) - size, Mule.HEIGHT - HEIGHT, size, size);
 			if(rect.contains(x, Mule.HEIGHT - y)){
 				manager.setPlayerDone(i);
 				return;
 			}
 		}
 	}
 }
