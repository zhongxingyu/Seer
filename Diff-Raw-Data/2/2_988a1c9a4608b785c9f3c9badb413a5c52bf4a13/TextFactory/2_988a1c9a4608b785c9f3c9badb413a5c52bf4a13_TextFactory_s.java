 package main;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.util.BufferedImageUtil;
 
 public class TextFactory {
 	
 	  public static Texture toTexture(String s, int color, Font f, boolean antialias) {
 		  return toGradientTexture(s, new Color(color).brighter().brighter().getRGB(), new Color(color).darker().darker().getRGB(), f, antialias);
 	  }
 	
 	  public static Texture toGradientTexture(String s, int color1, int color2, Font f, boolean antialias) {
 		  
 		  Texture tfall = TextureBank.getTexture("textg_"+s+"_"+color1+"_"+color2+"_"+f+"_"+antialias);
 		  
 		  if (tfall != null) return tfall;
 		  
 		  BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
 		  Graphics2D g = bi.createGraphics();
 		  if (antialias)
 			  g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		  else
 			  g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
 		  g.setFont(f);
 		  int w1 = g.getFontMetrics().getWidths()[g.getFont().getSize()] + 4;
		  int w = w1 * s.length();
 		  int h1 = g.getFontMetrics().getHeight();
 		  int h = h1 + 8;
 		  if (w1 <= 4 || w == 0) {
 			  w1 = g.getFont().getSize() * 4;
 			  w = w1 * s.length();
 		  }
 		  g.dispose();
 		  w = CMath.power2(w);
 		  h = CMath.power2(h);
 		  bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
 		  g = bi.createGraphics();
 		  g.setFont(f);
 		  if (antialias)
 			  g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		  else
 			  g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
 		  g.setPaint(new GradientPaint(0, 0, new Color(color1), 0, h*2, new Color(color2)));
 		  g.drawString(s, 0, h1);
 		  g.dispose();
 		  try {
 			  TextureBank.addImage("textg_"+s+"_"+color1+"_"+color2+"_"+f+"_"+antialias, bi);
 			  Texture t = TextureUtil.convert(bi);
 			  TextureBank.addTexture("textg_"+s+"_"+color1+"_"+color2+"_"+f+"_"+antialias, t);
 		      return t;
 		  } catch (Exception e) {
 			  return null;
 		  }
 	  }
 	  
 	  public static Texture toGradientTexture(String s, int color1, int color2, boolean antialias) {
 		  // Tahoma or g.getFont().getFontName() ?
 		  //Font f = new Font("Tahoma", 0, 12);
 		  // Better UBUNTUL
 		  Font f = Resources.Fonts.ubuntul;
 		  return toGradientTexture(s, color1, color2, f, antialias);
 	  }
 	  
 	  public static Texture toTexture(String s, int color, boolean antialias) {
 		  // Tahoma or g.getFont().getFontName() ?
 		  //Font f = new Font("Tahoma", 0, 12);
 		  // Better UBUNTUL
 		  Font f = Resources.Fonts.ubuntul;
 		  return toTexture(s, color, f, antialias);
 	  }
 	
 }
