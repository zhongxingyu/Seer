 package by.epamlab.elevator.ui.widgets;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.font.TextAttribute;
 import java.text.AttributedString;
 
 import org.jsfml.system.Vector2i;
 
 
 public class WText extends WBase {
 	private String text;
 	private AttributedString as;
 	private Font font;
 	private String fontFamily;
 	private int fontSize;
 	private int fontStyle;
 	
 	public WText() {
 		super();
 		text = new String(" ");
 		init();
 	}
 	
 	public WText(Vector2i position) {
 		super(position);
 		text = new String(" ");
 		init();
 	}
 	public WText(String text) {
 		super();
 		this.text = text;
 		init();
 	}
 
 	public WText(String text, Vector2i position) {
 		super();
 		this.text = text;
 		this.position = position;
 		init();
 	}
 
 	public void init() {
 		fontFamily = "Sans";
 		fontStyle = Font.BOLD;
 		fontSize = 18;
 		super.setColor(Color.BLACK);
 		fontRebuild();
 	}
 	
 	public void fontRebuild() {
 		as = new AttributedString(text);
 		as.addAttribute(TextAttribute.FOREGROUND, super.getColor()); //Using superclass color
 		as.addAttribute(TextAttribute.FONT, new Font(fontFamily, fontStyle, fontSize));
 		setOrigin(new Vector2i(0, fontSize));
 	}
 	
 	public String getText() {
 		return text;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 		fontRebuild();
 	}
 	
 	public Font getFont() {
 		return font;
 	}
 
 	public void setFont(Font font) {
 		this.font = font;
 		fontRebuild();
 	}
 
 	public String getFontFamily() {
 		return fontFamily;
 	}
 
 	public void setFontFamily(String fontFamily) {
 		this.fontFamily = fontFamily;
 		fontRebuild();
 	}
 
 	public int getFontSize() {
 		return fontSize;
 	}
 
 	public void setFontSize(int fontSize) {
 		this.fontSize = fontSize;
 		fontRebuild();
 	}
 
 	public int getFontStyle() {
 		return fontStyle;
 	}
 
 	public void setFontStyle(int fontStyle) {
 		this.fontStyle = fontStyle;
 		fontRebuild();
 	}
 
 	@Override
 	public void draw(Graphics target) {
		target.drawString(as.getIterator(), realPos.x, realPos.y);
 	}
 
 	@Override
 	public void update(float dt) {
 		//Text do nothing, isn't it?
 	}
 }
