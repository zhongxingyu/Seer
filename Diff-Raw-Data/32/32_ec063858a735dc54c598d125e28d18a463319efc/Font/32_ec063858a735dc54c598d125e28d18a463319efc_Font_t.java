 /*
  * Font.java
  * 
  * Part of the GameSDK starterkit made for the seminar at "The Gathering 2012 - At the end of the universe"
  * 
  * This source code is provided AS-IS and without any warranty. Use at own risk.
  * You can use and modify this source any way you want, but please send me an E-mail to petterroea@skymiastudios.com
  * if you make a game from it - I would like to keep a list of all games, as i am curious. You may also use this for games
  * that cost money. So it is basically public domain, except that i own the rights to it, but give you the rights to use
  * it for the purposes above. You cant sell this kit. You can sell a game based on it, but not the source code to this kit.
  * Also, this big comment must stay in all code files that originated from the starterkit, no matter how much they are
  * modified.
  * 
  * If you want support for this SDK, feel free to contact me. If you are at "The Gathering 2012", you can
  * look for me in the creative lounge(Where i sit). If not, feel free to send me an E-Mail to the above
  * adress. If you need normal java help, ask a mentor.
  * 
  */
 package net.petterroea.starterkit;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 /**
  * This class enables you to create custom fonts for use in the game.
  * @author petterroea
  *
  */
 public class Font {
 	/**
 	 * Map used to find the position of a char in the image
 	 */
 	private String[] chars = {"ABCDEFGHIJ",
 					  "KLMNOPQRST",
 					  "UVWXYZ.,:;",
 					  "!?)([]0123",
 					  "456789"};
 	/**
 	 * The grid of sprites used in the font
 	 */
 	private BufferedImage[][] src;
 	/**
 	 * Constructor for a font
 	 * @param sprite The sprite to use
 	 */
 	public Font(Sprite sprite)
 	{
 		this.src = sprite.getGrid();
 	}
 	/**
 	 * Draws a string to the selected x and y coordinates. I am too lazy to support newline.
 	 * @param g The graphics object
 	 * @param x The x coordinate
 	 * @param y The y coordinate
 	 * @param text The string to be written to the screen
 	 * @param shader The FontShader object. It is not actually a shader
 	 */
 	public void draw(Graphics g, int x, int y, String text, FontShader shader)
 	{
 		int yOffset = 0;
 		int fontw = src[0][0].getWidth();
 		text = text.toUpperCase();
 		for(int i = 0; i < text.length(); i++)
 		{
 			//New letter, search through for position of the character
 			for(int a = 0; a < chars.length; a++)
 			{
 				if(chars[a].indexOf(text.charAt(i)) > -1) //If it exists in this line...
 				{
 					int index = chars[a].indexOf(text.charAt(i));
 					if(shader == null)
 					{
 						g.drawImage(src[index][a], x + (i * fontw) + (i * 2), y, null);
 					}
 					else
 					{
 						//System.out.println("String " + text + " is using a shader!");
 						g.drawImage(shader.render(src[index][a], x + (i * fontw) + (i * 2), y), x + (i * fontw) + (i * 2), y, null);
 					}
 				}
 			}
 		}
 	}
	/**
	 * Gives you the width of one character
	 * @return The width
	 */
	public int getCharWidth() {
		// TODO Auto-generated method stub
		return src[0][0].getWidth() + 1;
	}
	/**
	 * Gives you the height of one character
	 * @return The height
	 */
	public int getCharHeight() {
		// TODO Auto-generated method stub
		return src[0][0].getHeight();
	}
 }
