 package org.adamk33n3r.karthas.gui.components;
 
 import org.adamk33n3r.karthas.ResizableImage;
 import org.adamk33n3r.karthas.Resources;
 import org.adamk33n3r.karthas.gui.GUI;
 import org.newdawn.slick.Image;
 
 public class TextComponent extends Component{
 	
 	String text, name;
 	int x, y, width;
 	boolean centered;
 	
 	public TextComponent(String text, int x, int y, boolean centered) {
 		this.text = text;
 		this.x = x;
 		this.y = y;
 		this.centered = centered;
 		this.width = GUI.font.getWidth(text);
 		this.name = this.text.replaceAll("\\s","");
 		this.name = this.name.replaceAll("\\W","");
 		this.name = this.name.toLowerCase();
 		Resources.load(Resources.IMAGES.valueOf(name), ((ResizableImage) Resources.get(Resources.IMAGES.RESIZE.componentBack)).build(width + 25, GUI.font.getLineHeight()));
 	}
 
 	@Override
 	public void update() {
 		
 	}
 
 	@Override
 	public void render() {
		GUI.drawImage((Image) Resources.get(Resources.IMAGES.valueOf(text)), x - width / 2 - 15, y - 6);
 		if (centered)
 			GUI.drawStringCentered(x, y, text, GUI.DEFAULT_FONT_COLOR, GUI.font);
 		else
 			GUI.drawString(x, y, text, GUI.DEFAULT_FONT_COLOR, GUI.font);
 	}
 	
 }
