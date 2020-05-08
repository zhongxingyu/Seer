 package Model;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.print.PageFormat;
 import java.awt.print.Printable;
 
 
 /**
  * This class represents each string that needs to be printed.
  * It writes text onto a graphics context by parsing it into 
  * tokens (or lines) selected by the '\n' (new line) delimiter. 
  * Then this is passed to the printer in Print class.
  * @author Oscar
  */
 class PrintObject implements Printable {
 
 	String toPrint;
 	String delimiter = "\n";
 	String[] tokens;
 
 	/**
 	 * Default constructor for PrintObject class
 	 * Sets up the global variable with the text to be printed and
 	 * the delimiter that will separate the text into individual lines
 	 * or tokens.
 	 */
 	public PrintObject(String calculations) {
 		toPrint = calculations;
 		tokens = toPrint.split(delimiter);
 	}
 
 	/**
 	 * Writes the text from the tokens into a graphics context.
 	 */
 	public int print(Graphics g, PageFormat f, int pageIndex) {
		Graphics2D g2 = (Graphics2D) g; 
 		
 		int x = 80;
 		int y = 100;
 
 		switch (pageIndex) {
 		case 0:
 			g2.setColor(Color.black);
 
			for (int i = 0; i < tokens.length; i++) {
 
 				g2.drawString(tokens[i], x, y);
 				y += 20;
 
 			}
 
 			return PAGE_EXISTS;
 
 		default:
 			return NO_SUCH_PAGE;
 		}
 	}
 }
