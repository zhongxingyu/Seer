 package game;
 
 import game.screen.ScreenTools;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 
 public class Font {
 
 	String font = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
 				  "0123456789!?/\\\"()#><|{}%" +
 				  "+-.,:";
 	
 	private BufferedImage fontsheet;
 	private BufferedImage[] chars;
 	private Graphics g;
 	private Game game;
 	
 	
 	public Font(BufferedImage fontsheet, Graphics g, Game game)
 	{
 		this.game = game;
 		this.g = g;
 		this.fontsheet = fontsheet;
 		
 		chars = new BufferedImage[fontsheet.getHeight() * fontsheet.getWidth()];
 		
 		int rows = fontsheet.getWidth() / 8; 
 		int cols = fontsheet.getHeight() /8;
 
 		
 
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				chars[(i * cols) + j] = fontsheet.getSubimage(j * 8,
 						i * 8, 8, 8);
 			}
 		}
 		
 		
 	}
 	
 	
 	public void drawCenteredString(String text, int y, int size)
 	{
 		drawString(text,(game.getWidth()/2)-text.length()*6, y, size);
 	}
 	
 	public void drawString(String text, int x, int y, int size)
 	{
 		
 		for(int i = 0; i < text.length(); i++)
 		{
 			for(int j = 0; j < font.length(); j++)
 			{
 				if(font.toCharArray()[j] == text.toUpperCase().toCharArray()[i])
 				{
 					g.drawImage(ScreenTools.recolourImage(chars[j], Color.white), x+(i*size*8), y, 8*size, 8*size, game);
 				}
 				
 			}
 			
 		}
 		
 	}
 	public void drawString(String text, int x, int y, int size, Color colour)
 	{
 		
 		for(int i = 0; i < text.length(); i++)
 		{
 			for(int j = 0; j < font.length(); j++)
 			{
 				if(font.toCharArray()[j] == text.toUpperCase().toCharArray()[i])
 				{
 					g.drawImage(ScreenTools.recolourImage(chars[j], colour), x+(i*size*8), y, 8*size, 8*size, game);
 				}
 				
 			}
 			
 		}
 		
 	}
 	
 	
 }
