 package Model;
 
 import java.awt.Color;
import java.awt.Font;
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
		Font font = new Font("serif", Font.PLAIN, 12);
		Font bigFont = new Font("arial", Font.BOLD, 16);
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setFont(bigFont);
 		
 		int x = 80;
 		int y = 100;
 
 		switch (pageIndex) {
 		case 0:
 			g2.setColor(Color.black);
			
			String header = "Redeclaration Details for " + tokens[0] + " Airport";
			
			g2.drawString(header, x, y);
			g2.setFont(font);
			y += 20;
 
			for (int i = 1; i < tokens.length; i++) {
 
 				g2.drawString(tokens[i], x, y);
 				y += 20;
 
 			}
 
 			return PAGE_EXISTS;
 
 		default:
 			return NO_SUCH_PAGE;
 		}
 	}
 }
