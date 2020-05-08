 package com.maton.tools.stiletto.model;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.GradientPaint;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.font.FontRenderContext;
 import java.awt.font.TextAttribute;
 import java.awt.font.TextLayout;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.text.AttributedCharacterIterator;
 import java.text.AttributedString;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.widgets.Display;
 
 import com.maton.tools.stiletto.model.base.IBaseModel;
 import com.maton.tools.stiletto.util.SwingTools;
 
 public class Font implements IBaseModel {
 
 	protected String name;
 	protected String face;
 	protected int size;
 	protected boolean bold;
 	protected boolean italic;
 
 	protected boolean fill;
 	protected int fillAngle;
 	protected Color fillColor0;
 	protected Color fillColor1;
 
 	protected boolean stroke;
 	protected int strokeWidth;
 	protected int strokeAngle;
 	protected Color strokeColor0;
 	protected Color strokeColor1;
 
 	protected boolean shadow;
 	protected int shadowX;
 	protected int shadowY;
 	protected int shadowAlpha;
 	protected Color shadowColor;
 
 	protected String characters;
 
 	protected AlternateFontPool alternates;
 
 	public Font(String name) {
 		this.name = name;
 		alternates = new AlternateFontPool();
 		startup();
 	}
 
 	protected void startup() {
 		face = "Arial";
 		size = 20;
 		bold = false;
 		italic = false;
 
 		fill = true;
 		fillAngle = 0;
 		fillColor0 = Color.BLACK;
 		fillColor1 = Color.BLACK;
 
 		stroke = false;
 		strokeAngle = 0;
 		strokeColor0 = Color.WHITE;
 		strokeColor1 = Color.WHITE;
 
 		shadow = true;
 		shadowX = 0;
 		shadowY = 1;
 		shadowAlpha = 255;
 		shadowColor = Color.GRAY;
 
 		characters = " ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789򖄟?[]{}!~@#$%^&*()-=_+<>,./;:'\"\\|";
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getFace() {
 		return face;
 	}
 
 	public void setFace(String face) {
 		this.face = face;
 	}
 
 	public int getSize() {
 		return size;
 	}
 
 	public void setSize(int size) {
 		this.size = size;
 	}
 
 	public boolean isBold() {
 		return bold;
 	}
 
 	public void setBold(boolean bold) {
 		this.bold = bold;
 	}
 
 	public boolean isItalic() {
 		return italic;
 	}
 
 	public void setItalic(boolean italic) {
 		this.italic = italic;
 	}
 
 	public boolean isFill() {
 		return fill;
 	}
 
 	public void setFill(boolean fill) {
 		this.fill = fill;
 	}
 
 	public int getFillAngle() {
 		return fillAngle;
 	}
 
 	public void setFillAngle(int fillAngle) {
 		this.fillAngle = fillAngle;
 	}
 
 	public Color getFillColor0() {
 		return fillColor0;
 	}
 
 	public void setFillColor0(Color fillColor0) {
 		this.fillColor0 = fillColor0;
 	}
 
 	public Color getFillColor1() {
 		return fillColor1;
 	}
 
 	public void setFillColor1(Color fillColor1) {
 		this.fillColor1 = fillColor1;
 	}
 
 	public boolean isStroke() {
 		return stroke;
 	}
 
 	public void setStroke(boolean stroke) {
 		this.stroke = stroke;
 	}
 
 	public int getStrokeAngle() {
 		return strokeAngle;
 	}
 
 	public void setStrokeAngle(int strokeAngle) {
 		this.strokeAngle = strokeAngle;
 	}
 
 	public Color getStrokeColor0() {
 		return strokeColor0;
 	}
 
 	public void setStrokeColor0(Color strokeColor0) {
 		this.strokeColor0 = strokeColor0;
 	}
 
 	public Color getStrokeColor1() {
 		return strokeColor1;
 	}
 
 	public void setStrokeColor1(Color strokeColor1) {
 		this.strokeColor1 = strokeColor1;
 	}
 
 	public boolean isShadow() {
 		return shadow;
 	}
 
 	public void setShadow(boolean shadow) {
 		this.shadow = shadow;
 	}
 
 	public int getShadowX() {
 		return shadowX;
 	}
 
 	public void setShadowX(int shadowX) {
 		this.shadowX = shadowX;
 	}
 
 	public int getShadowY() {
 		return shadowY;
 	}
 
 	public void setShadowY(int shadowY) {
 		this.shadowY = shadowY;
 	}
 
 	public int getShadowAlpha() {
 		return shadowAlpha;
 	}
 
 	public void setShadowAlpha(int shadowAlpha) {
 		this.shadowAlpha = shadowAlpha;
 	}
 
 	public Color getShadowColor() {
 		return shadowColor;
 	}
 
 	public void setShadowColor(Color shadowColor) {
 		this.shadowColor = shadowColor;
 	}
 
 	public int getFillColor0Int() {
 		return fillColor0.getRGB();
 	}
 
 	public int getFillColor1Int() {
 		return fillColor1.getRGB();
 	}
 
 	public int getStrokeColor0Int() {
 		return strokeColor0.getRGB();
 	}
 
 	public int getStrokeColor1Int() {
 		return strokeColor1.getRGB();
 	}
 
 	public int getShadowColorInt() {
 		return shadowColor.getRGB();
 	}
 
 	protected java.awt.Font font;
 	protected FontRenderContext context;
 
 	public void regenFont(Graphics2D g) {
 		regenFont(g, null);
 	}
 
 	public void regenFont(Graphics2D g, Resolution res) {
 		if (font != null) {
 			font = null;
 		}
 		
 		AlternateFont alt = null;
 		
 		int size = this.size;
 		if (res != null) {
 			alt = getAlternates().getElement(res.getName());
 			
 			if (alt != null) {
 				size = alt.getSize();
 			}
 		}
 
 		int fontHints = java.awt.Font.PLAIN;
 
 		if (bold && italic) {
 			fontHints = java.awt.Font.ITALIC + java.awt.Font.BOLD;
 		} else if (bold) {
 			fontHints = java.awt.Font.BOLD;
 		} else if (italic) {
 			fontHints = java.awt.Font.ITALIC;
 		}
 
 		context = g.getFontRenderContext();
 		font = new java.awt.Font(face, fontHints, size);
 	}
 
 	public void draw(GC gc, int x, int y, CharMetric metric) {
 		BufferedImage buff = new BufferedImage(size * 2, size * 2,
 				BufferedImage.TYPE_4BYTE_ABGR_PRE);
 		Graphics2D g = buff.createGraphics();
 		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
 				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		regenFont(g);
 		calcMetrics(g, metric);
 		adjust(metric);
 		renderChar(g, 0, 0, metric);
 		g.dispose();
 
 		ImageData data = SwingTools.convertToSWT(buff);
 		Image letter = new Image(Display.getCurrent(), data);
 		gc.drawImage(letter, x - metric.x, y - metric.y + 1);
 		letter.dispose();
 	}
 
 	public void adjust(CharMetric metric) {
 		if (metric.x < 0) {
 			metric.width -= metric.x;
 			metric.x = 0;
 		}
 
 		if (metric.y < 0) {
 			metric.height -= metric.y;
 			metric.y = 0;
 		}
 	}
 	
 	public void calcMetrics(Graphics2D g, CharMetric metric) {
 		calcMetrics(g, metric, null);
 	}
 
 	public void calcMetrics(Graphics2D g, CharMetric metric, Resolution res) {
 		AlternateFont alt = null;
 		
 		boolean stroke = this.stroke;
 		int strokeWidth = this.strokeWidth;
 		boolean shadow = this.shadow;
 		int shadowX = this.shadowX;
 		int shadowY = this.shadowY;
 		
 		if (res != null) {
 			alt = getAlternates().getElement(res.getName());
 			
 			if (alt != null) {
 				stroke = alt.getStroke() != 0;
 				strokeWidth = alt.getStroke();
 				
 				shadow = alt.getShadowX() != 0 || alt.getShadowY() != 0;
 				shadowX = alt.getShadowX();
 				shadowY = alt.getShadowY();
 			}
 		}
 
 		char letra = metric.letter;
 		AttributedString as = new AttributedString("" + letra);
 		as.addAttribute(TextAttribute.FONT, font);
 
 		AttributedCharacterIterator aci = as.getIterator();
 		TextLayout tl = new TextLayout(aci, context);
 
 		metric.x = 0;
 		metric.width = 0;
 
 		if (tl.getBounds().getX() < 0) {
 			metric.x = -(int) tl.getBounds().getX() - 1;
 		} else {
 			metric.width = (int) tl.getBounds().getX() + 1;
 		}
 		metric.width += tl.getBounds().getWidth() + 1;
 
 		metric.height = 0;
 		metric.y = 0;
 
 		if (tl.getBounds().getY() < 0) {
 			metric.y -= tl.getBounds().getY();
 		} else {
 			metric.height = (int) tl.getBounds().getY() + 1;
 		}
 		metric.height += tl.getBounds().getHeight() + 1;
 
 		int offY = (int) (tl.getBounds().getHeight() + tl.getBounds().getY());
 		int offX = (int) (tl.getBounds().getWidth() + tl.getBounds().getX());
 
 		if (stroke) {
 			metric.width += strokeWidth + 1;
 			metric.height += strokeWidth + 1;
 			offY += (strokeWidth / 2) + 1;
 			offX += (strokeWidth / 2) + 1;
 			metric.x += (strokeWidth / 2) + 1;
 			metric.y += (strokeWidth / 2) + 1;
 		}
 
 		if (shadow) {
 			if (shadowX < 0) {
 				metric.x -= shadowX;
 				metric.width -= shadowX;
 			} else {
 				metric.width += shadowX;
 			}
 
 			if (shadowY < 0) {
 				metric.y -= shadowY;
 				metric.height -= shadowY;
 			} else {
 				metric.height += shadowY;
 			}
 		}
 
 		metric.xadvance = (int) tl.getAdvance();
		metric.height += 2;
 	}
 	
 	public void renderChar(Graphics2D g, int x, int y, CharMetric metric) {
 		renderChar(g, x, y, metric, null);
 	}
 
 	public void renderChar(Graphics2D g, int x, int y, CharMetric metric, Resolution res) {
 		AlternateFont alt = null;
 		
 		boolean stroke = this.stroke;
 		int strokeWidth = this.strokeWidth;
 		boolean shadow = this.shadow;
 		int shadowX = this.shadowX;
 		int shadowY = this.shadowY;
 		
 		if (res != null) {
 			alt = getAlternates().getElement(res.getName());
 			
 			if (alt != null) {
 				stroke = alt.getStroke() != 0;
 				strokeWidth = alt.getStroke();
 				
 				shadow = alt.getShadowX() != 0 || alt.getShadowY() != 0;
 				shadowX = alt.getShadowX();
 				shadowY = alt.getShadowY();
 			}
 		}
 
 		char letra = metric.letter;
 		AttributedString as = new AttributedString("" + letra);
 		as.addAttribute(TextAttribute.FONT, font);
 
 		AttributedCharacterIterator aci = as.getIterator();
 		TextLayout tl = new TextLayout(aci, context);
 
 		if (metric.x < 0) {
 			metric.width -= metric.x;
 			x += metric.x;
 			metric.x = 0;
 		}
 
 		if (metric.y < 0) {
 			metric.height -= metric.y;
 			y += metric.y;
 			metric.y = 0;
 		}
 
 		// Shadow
 		if (shadow) {
 			AlphaComposite ac = AlphaComposite.SrcOver
 					.derive(((float) shadowAlpha) / 255.0f);
 			g.setComposite(ac);
 
 			Shape shadowClip = tl.getOutline(AffineTransform
 					.getTranslateInstance(metric.x + x + shadowX, metric.y + y
 							+ shadowY + 1));
 			g.setColor(shadowColor);
 			g.fill(shadowClip);
 
 			if (stroke) {
 				g.setStroke(new BasicStroke(strokeWidth));
 				g.draw(shadowClip);
 			}
 
 			g.setComposite(AlphaComposite.Src);
 		}
 
 		// Fill
 		Shape outlineClip = tl.getOutline(AffineTransform.getTranslateInstance(
 				metric.x + x, metric.y + y + 1));
 		if (fill) {
 			if (fillAngle == 0) {
 				g.setPaint(new GradientPaint(x, y, fillColor0, x, y + metric.y
 						+ metric.height, fillColor1));
 			} else {
 				g.setPaint(new GradientPaint(x, y, fillColor0, x + metric.x
 						+ metric.width, y, fillColor1));
 			}
 			g.fill(outlineClip);
 		}
 
 		// Outline
 		if (stroke) {
 			if (strokeAngle == 0) {
 				g.setPaint(new GradientPaint(x, y, strokeColor0, x, y
 						+ metric.y + metric.height, strokeColor1));
 			} else {
 				g.setPaint(new GradientPaint(x, y, strokeColor0, x + metric.x
 						+ metric.width, y, strokeColor1));
 			}
 			g.setStroke(new BasicStroke(strokeWidth));
 			g.draw(outlineClip);
 		}
 	}
 
 	public String getCharacters() {
 		return characters;
 	}
 
 	public void setCharacters(String characters) {
 		this.characters = characters;
 	}
 
 	public int getStrokeWidth() {
 		return strokeWidth;
 	}
 
 	public void setStrokeWidth(int strokeWidth) {
 		this.strokeWidth = strokeWidth;
 	}
 
 	public String getCharactersList() {
 		String ints = "";
 
 		for (int i = 0; i < characters.length(); i++) {
 			int num = (int) characters.charAt(i);
 
 			if (i > 0) {
 				ints += ",";
 			}
 			ints += num;
 		}
 
 		return ints;
 	}
 
 	public void setCharactersList(String list) {
 		StringTokenizer tok = new StringTokenizer(list, ",");
 		characters = "";
 
 		while (tok.hasMoreTokens()) {
 			int c = Integer.parseInt(tok.nextToken());
 			characters += "" + ((char) c);
 		}
 	}
 
 	public Object getSelf() {
 		return this;
 	}
 	
 	public List<CharMetric> getCharMetrics() {
 		return getCharMetrics(null);
 	}
 
 	public List<CharMetric> getCharMetrics(Resolution res) {
 		ArrayList<CharMetric> chars = new ArrayList<CharMetric>();
 		
 		AlternateFont alt = null;
 		
 		int sizex = this.size;
 		if (res != null) {
 			alt = getAlternates().getElement(res.getName());
 			
 			if (alt != null) {
 				sizex = alt.getSize();
 			}
 		}
 
 		BufferedImage buff = new BufferedImage(sizex * 2, sizex * 2,
 				BufferedImage.TYPE_4BYTE_ABGR_PRE);
 		Graphics2D g = buff.createGraphics();
 		regenFont(g, res);
 
 		for (int i = 0; i < characters.length(); i++) {
 			CharMetric metric = new CharMetric();
 			metric.letter = characters.charAt(i);
 			calcMetrics(g, metric, res);
 			adjust(metric);
 
 			chars.add(metric);
 		}
 
 		g.dispose();
 		buff.flush();
 
 		return chars;
 	}
 
 	public AlternateFontPool getAlternates() {
 		return alternates;
 	}
 	
 	public boolean hasAlternate(Resolution res) {
 		if (res == null) {
 			return true;
 		}
 		
 		return getAlternates().getElement(res.getName()) != null;
 	}
	
	AlternateFont currentAlternate;
	
	public void setResolution(Resolution res) {
		if (res != null) {
			currentAlternate = getAlternates().getElement(res.getName());
		} else {
			currentAlternate = null;
		}
	}
	
	public int getAlternateSize() {
		if (currentAlternate == null) {
			return size;
		}
		
		return currentAlternate.getSize();
	}
 }
