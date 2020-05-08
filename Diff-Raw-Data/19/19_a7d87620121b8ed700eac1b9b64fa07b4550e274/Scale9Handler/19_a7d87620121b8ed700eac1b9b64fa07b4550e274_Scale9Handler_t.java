 package org.qrone.r7.tag;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.URI;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.qrone.img.ImageBuffer;
 import org.qrone.r7.parser.CSS3Values;
 import org.qrone.r7.parser.HTML5Deck;
 import org.qrone.r7.parser.HTML5Element;
 import org.qrone.r7.parser.ImagePart;
 import org.w3c.dom.css.CSSValue;
 
 public class Scale9Handler extends HTML5TagHandler {
	//public static Pattern urlRegex = Pattern.compile("url\\s*\\(\\s*[\"']?(.*?)[\"']?\\s*\\)");
 	public static Pattern numberRegex = Pattern.compile("([0-9]+)px");
 	public static Pattern colorRegex = Pattern.compile("(#[a-fA-F0-9]+|rgb\\s*\\(\\s*[^()]+\\s*\\))");
 	
 	private HTML5Deck deck;
 	
 	public Scale9Handler(HTML5Deck deck) {
 		this.deck = deck;
 	}
 	
 	@Override
 	public HTML5TagResult process(HTML5Element e) {
 		CSS3Values v = e.getPropertyValue("scale9");
 		
 		if(v != null){
 			String value = v.toString();
 			String url;
 			Matcher mm;
 			
			url = v.getURL();
			if(v == null){
 				return null;
 			}
 
 			mm = numberRegex.matcher(value);
 			final List<Integer> l = new LinkedList<Integer>();
 			while (mm.find()) {
 				l.add(Integer.parseInt(mm.group(1)));
 			}
 			
 			String bgcolor = e.getProperty("background-color");
 			if(bgcolor != null){
 				e.renameProperty("background-color", "scale9-color");
 			}
 
 			String sc = e.getProperty("scale9-color");
 			if(sc != null){
 				bgcolor = sc;
 			}
 
 			String widthValue = e.getProperty("width");
 			if(widthValue !=null){
 				e.renameProperty("width", "scale9-width");
 			}
 			
 			String sw = e.getProperty("scale9-width");
 			if(sw != null){
 				widthValue = sw;
 			}
 			
 			final String c = "background-color:" + bgcolor;
 			final String u = url;
 			final String w = "width:" + widthValue;
 			final URI    f = e.getOM().getURI();
 			if(l.size() == 4){
 				return new HTML5TagResult() {
 					
 					@Override
 					public String prestart() {
 						try {
 							return startScale9(f.resolve(u),
 									l.get(0), l.get(1), l.get(2), l.get(3), c, w);
 						} catch (IOException e) {
 							return null;
 						}
 					}
 
 					@Override
 					public String poststart() {
 						return null;
 					}
 					@Override
 					public String preend() {
 						return null;
 					}
 					
 					
 					@Override
 					public String postend() {
 						try {
 							return endScale9(f.resolve(u),
 									l.get(0), l.get(1), l.get(2), l.get(3));
 						} catch (IOException e) {
 							return null;
 						}
 					}
 				};
 			}else if(l.size() == 2){
 				return new HTML5TagResult() {
 					
 					@Override
 					public String prestart() {
 						try {
 							return startScale3(f.resolve(u),
 									l.get(0), l.get(1), c);
 						} catch (IOException e) {
 							return null;
 						}
 					}
 					
 					@Override
 					public String preend() {
 						return null;
 					}
 					
 					@Override
 					public String poststart() {
 						return null;
 					}
 					
 					@Override
 					public String postend() {
 						try {
 							return endScale3(f.resolve(u),
 									l.get(0), l.get(1));
 						} catch (IOException e) {
 							return null;
 						}
 					}
 				};
 			}
 		}
 		return null;
 	}
 
 	public String startScale3(URI file, int left, int right, String color) throws IOException{
 		ImageBuffer image = deck.getSpriter().getImage(file);
 		int width = image.getWidth();
 		int height = image.getHeight();
 		
 		StringBuffer b = new StringBuffer();
 
 		b.append("<table style=\"width:100%;\" cellspacing=\"0\" cellpadding=\"0\"><tr><td style=\"");
 		b.append(deck.getSpriter().addISprite(new ImagePart(file, 0, 0, left, height)));
 		b.append("\"></td><td valign=\"top\" style=\"" + color);
 		b.append(deck.getSpriter().addHSprite(new ImagePart(file, left, 0, right-left, height)) + "\">");
 		//b.append("<div style=\"position:relative;margin-left:-"+left+"px;margin-right:-"+(width-right)+"px;\">");
 		
 		
 		
 		//b.append(addISprite(new PartOfImage(file, 0, 0, left, height)));
 		//b.append("\"></div><div style=\"float:right;");
 		///b.append(addISprite(new PartOfImage(file, right, 0, width-right, height)));
 		//b.append("\"></div>");
 		return b.toString();
 	}
 	
 	public String endScale3(URI file, int left, int right) throws IOException{
 		ImageBuffer image = deck.getSpriter().getImage(file);
 		int width = image.getWidth();
 		int height = image.getHeight();
 		
 		StringBuffer b = new StringBuffer();
 		//b.append("</div>");
 		b.append("</td><td style=\"");
 		b.append(deck.getSpriter().addISprite(new ImagePart(file, right, 0, width-right, height)));		
 		b.append("\"></td></tr></table>");
 		return b.toString();
 	}
 
 	public String startScale9(URI file, int left, int right, int top, int bottom, String color, String w) throws IOException{
 		
 		ImageBuffer image = deck.getSpriter().getImage(file);
 		int width = image.getWidth();
 		int height = image.getHeight();
 		
 		StringBuffer b = new StringBuffer();
 		b.append("<table" + ( w != null ? " style=\"" + w + "\"" : "" ) + " cellspacing=\"0\" cellpadding=\"0\"><tr><td style=\"");
 		b.append(deck.getSpriter().addISprite(new ImagePart(file, 0, 0, left, top)));
 		b.append("\"></td><td style=\"");
 		b.append(deck.getSpriter().addHSprite(new ImagePart(file, left, 0, right-left, top)));
 		b.append("\"></td><td style=\"");
 		b.append(deck.getSpriter().addISprite(new ImagePart(file, right, 0, width-right, top)));
 		b.append("\"></td></tr><tr><td style=\"");
 		
 		
 		b.append(deck.getSpriter().addVSprite(new ImagePart(file, 0, top, left, bottom-top)));
 		b.append("\"></td><td valign=\"top\"" + ( color != null ? " style=\"" + color + "\"" : "" ) + "\">");
 		//b.append("&nbsp;<div style=\"position:relative;left:-"+left+"px;top:-"+top+"px;margin-right:-"+(width-right+left)+"px;margin-bottom:-"+(height-bottom+top)+"px;\">");
 		return b.toString();
 	}
 
 	public String endScale9(URI file, int left, int right, int top, int bottom) throws IOException{
 		ImageBuffer image = deck.getSpriter().getImage(file);
 		int width = image.getWidth();
 		int height = image.getHeight();
 		
 		StringBuffer b = new StringBuffer();
 		//b.append("</div>");
 		b.append("</td><td style=\"");
 		b.append(deck.getSpriter().addVSprite(new ImagePart(file, right, top, width-right, bottom-top)));
 		b.append("\"></td></tr><tr><td style=\"");
 		b.append(deck.getSpriter().addISprite(new ImagePart(file, 0, bottom, left, height-bottom)));
 		b.append("\"></td><td style=\"");
 		b.append(deck.getSpriter().addHSprite(new ImagePart(file, left, bottom, right-left, height-bottom)));
 		b.append("\"></td><td style=\"");
 		b.append(deck.getSpriter().addISprite(new ImagePart(file, right, bottom, width-right, height-bottom)));
 		b.append("\"></td></tr></table>");
 		return b.toString();
 	}
 }
