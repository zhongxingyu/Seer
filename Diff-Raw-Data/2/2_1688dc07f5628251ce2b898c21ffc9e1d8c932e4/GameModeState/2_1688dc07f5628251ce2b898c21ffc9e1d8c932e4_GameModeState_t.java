 package slug.soc.game;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 
 import slug.soc.game.gameObjects.GameObjectArmy;
 import slug.soc.game.gameObjects.GameObjectCursor;
 import slug.soc.game.gameObjects.TerrainObject;
 
 /**
  * Represents the current game.
  * @author slug
  *
  */
 public class GameModeState implements IGameState {
 
 	private TerrainObject[][] map;
 	private TerrianGenerator terrianGenerator;
 	private GameObjectCursor cursor = new GameObjectCursor();
 	private Integer currentXPos;
 	private Integer currentYPos;
 	private double[] zoomScales = {
 			1.0, 0.5	
 	};
 
 	private boolean cursorActive = false;
 
 	private int currentZoomIndex = 0;
 	private int frameCounter;
 
 	public GameModeState(){
 		long start = System.nanoTime();
 		terrianGenerator = new TerrianGenerator();
 		map = terrianGenerator.testGenerateMapMultiCont(100, 100);
 		currentXPos = 50;
 		currentYPos = 50;
 		long end = System.nanoTime();
 		System.out.println("GenTime: " + (end - start)/1000000);
 	}
 
 	public TerrainObject[][] getMap(){
 		return map;
 	}
 
 	public void processKey(KeyEvent e){
 		if(e.getKeyCode() == KeyEvent.VK_UP){
 			if(currentYPos > 0){
 				if(cursorActive){
 					map[currentYPos][currentXPos].removeGameObject(cursor);
 					currentYPos--;
 					map[currentYPos][currentXPos].addGameObject(cursor);
 				}
 				else{
 					currentYPos--;
 				}
 			}
 		}
 		else if(e.getKeyCode() == KeyEvent.VK_DOWN){
 			if(currentYPos < getMap().length){
 				if(cursorActive){
 					map[currentYPos][currentXPos].removeGameObject(cursor);
 					currentYPos++;
 					map[currentYPos][currentXPos].addGameObject(cursor);
 				}
 				else{
 					currentYPos++;
 				}
 			}
 		}
 		else if(e.getKeyCode() == KeyEvent.VK_RIGHT){
 			if(currentXPos < getMap().length){
 				if(cursorActive){
 					map[currentYPos][currentXPos].removeGameObject(cursor);
 					currentXPos++;
 					map[currentYPos][currentXPos].addGameObject(cursor);
 				}
 				else{
 					currentXPos++;
 				}
 			}
 		}
 		else if(e.getKeyCode() == KeyEvent.VK_LEFT){
 			if(currentXPos > 0){
 				if(cursorActive){
 					map[currentYPos][currentXPos].removeGameObject(cursor);
 					currentXPos--;
 					map[currentYPos][currentXPos].addGameObject(cursor);
 				}
 				else{
 					currentXPos--;
 				}
 			}
 		}
 		else if(e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET){
 			if(currentZoomIndex - 1 > -1){
 				currentZoomIndex--;
 			}
 		}
 		else if(e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET){
 			if(currentZoomIndex + 1 < zoomScales.length){
 				currentZoomIndex++;
 			}
 		}
 		else if(e.getKeyCode() == KeyEvent.VK_C){
 			if(!cursorActive){
 				map[currentYPos][currentXPos].addGameObject(cursor);
 				cursorActive = true;
 			}
 			else{
 				cursorActive = false;
 				map[currentYPos][currentXPos].removeGameObject(cursor);
 			}
 		}
 	}
 
 	public Image createImage(){
 		frameCounter++;
 		Image gameImage = new BufferedImage(1000,500, BufferedImage.TYPE_INT_RGB);
 		Graphics g = gameImage.getGraphics();
 		int gy = 30;
 		int gx;
 		g.setFont(new Font("Monospaced", Font.PLAIN, (int)(19 * zoomScales[currentZoomIndex])));
 		for(int y = currentYPos - 12, my = 0; my < (25 * 1/zoomScales[currentZoomIndex]); y++,my++){
 			gx = 15;
 			for(int x = currentXPos - 12, mx = 0; mx < (25 * 1/zoomScales[currentZoomIndex]) ; x++, mx++){
 				if(x < 0 || y < 0 || x >= getMap().length || y >= getMap().length ){
 					g.setColor(Color.BLACK);
 					g.drawString(" ", gx, gy);
 				}
 				else{
 					if(frameCounter >= 60){
 						getMap()[y][x].nextTile();
 					}
 					g.setColor(getMap()[y][x].getTile().getColor());
 					g.drawString(getMap()[y][x].getTile().getSymbol().toString(), gx, gy);
 				}
 				gx += g.getFont().getSize();
 			}
 			gy += g.getFont().getSize();
 		}
 		if(frameCounter >= 60){
 			frameCounter = 0;
 		}
 		g.setColor(Color.WHITE);
 		g.drawLine(500, 0, 500, 500);
 
		if(currentYPos > 0 && currentXPos > 0 && currentYPos < map.length && currentXPos < map.length){
 			g.drawString(getMap()[currentYPos][currentXPos].toString(),750,250);
 		}
 		g.drawString("X: " + currentXPos.toString(), 750, 270);
 		g.drawString("Y: " + currentYPos.toString(), 790, 270);
 
 		return gameImage;
 	}	
 }
