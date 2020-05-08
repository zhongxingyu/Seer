 import java.awt.Graphics;
 import java.awt.Color;
 
 class PaintableString implements PaintableText {
 
 	private String text;
 	private Color color;
 
 	public PaintableString(String text, Color color) {
 		this.text = text;
 		this.color = color;
 	}
 
 	public PaintableString(String text) {
 		this(text,null);
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public int paint(Graphics g,int start,int end) {
 		return draw( g, text.substring(start, end) );
 	}
 
 	public int paint(Graphics g) {
 		return draw(g, text);
 	}
 
 	private int  draw(Graphics g, String text) {
 
 		Color def = g.getColor();
 
 		if ( color != null ) g.setColor(color);
 
		g.drawString(text,0, g.getFontMetrics().getHeight() - g.getFontMetrics().getMaxDescent() );
 
 		g.setColor(def);
 
 		return g.getFontMetrics().stringWidth(text);
 	}
 }
