 package ikm.views;
 
 import java.util.Vector;
 
 import ikm.Main;
 import ikm.ViewManager;
 import ikm.data.Animal;
 import ikm.util.RichText;
 
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.CustomItem;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.Font;
 import javax.microedition.lcdui.Form;
 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.Item;
 import javax.microedition.lcdui.ItemCommandListener;
 import javax.microedition.lcdui.StringItem;
 
 class DetailItem extends CustomItem {
 	private int width;
 	private int height;
 	private RichText rich;
	private int generatedWidth = 0;
 	
 	private String text;
 	private Font baseFont = Font.getFont(Font.FONT_STATIC_TEXT);
 	private Font font = Font.getFont(baseFont.getFace(), baseFont.getStyle(), Font.SIZE_SMALL);
 	private Font boldFont = Font.getFont(baseFont.getFace(), Font.STYLE_BOLD, Font.SIZE_SMALL);
		
 	protected DetailItem(String label, int width, String text) {
 		super(label);
 		this.width = width;
 		this.text = text;
		
	}
	
	private void generateText() {
		if (width != generatedWidth) {
			rich = new RichText(width);
			rich.addFormattedText(text, font, boldFont);
			generatedWidth = width;
		}
 	}
 
 	protected int getMinContentHeight() {
 		return 10;
 	}
 
 	protected int getMinContentWidth() {
 		return width;
 	}
 
 	protected int getPrefContentHeight(int www) {
		generateText();
 		return (rich.getLineCount()) * font.getHeight();
 	}
 
 	protected int getPrefContentWidth(int height) {
 		return width;
 	}
 
 	protected void paint(Graphics g, int w, int h) {
		generateText();
 		g.setColor(Main.display.getColor(Display.COLOR_FOREGROUND));
 		rich.draw(g);
 	}
 	
 	protected void sizeChanged(int w, int h) {
 		width = w;
 		height = h;
		generateText();
 	}
 }
 
 public class Article extends Form implements CommandListener {	
 	private ViewManager viewManager;
 	private Command exitCommand = new Command("Back", Command.BACK, 1);
 	private String[] line;
 	private String[] fieldNames;
 	
 	public Article(String caption, ViewManager viewManager, String[] line,
 			String[] fieldNames) {
 		super(caption);
 		
 		this.viewManager = viewManager;
 		this.line = line;
 		this.fieldNames = fieldNames;
 		addCommand(exitCommand);
 		setCommandListener(this);
 
 		fillDetails();
 	}
 
 	private void addText(String caption, String data) {
 		//StringItem item = new StringItem(caption, data);
 		//append(item);
 		DetailItem t = new DetailItem(caption, getWidth(), data);
 		append(t);
 	}
 	
 	private void fillDetails() {
 		//TestItem t = new TestItem("test", getWidth(), line[1 + 1]);
 		//append(t);
 		
 		for (int i = 1; i < fieldNames.length; i++) {
 			addText(fieldNames[i], line[i + 1]);
 		}
 	}
 
 	public void commandAction(Command c, Displayable d) {
 		if (c == exitCommand) {
 			viewManager.goBack();
 		}
 	}
 }
