 package fscode.tags;
 
 import fscode.Const;
 import fscode.Emitter;
 import fscode.HtmlEmitter;
 import fscode.exception.NonfatalException;
 import java.util.Collection;
 import java.util.ResourceBundle;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  * Allows the embedding of images into stuff via the <code>image</code> tag.
  *
  * @author cmiller
  * @since 0.1
  */
 public class EmbeddedImage extends Emitter implements HtmlEmitter {
 /*
 src required URL of the image
 alt optional Alternate text to be displayed if the image cannot be displayed
 text optional Text that appears when the mouse hovers over the image
 width optional The width to display the image, in either pixels or percentage
 height optional The height to display the image, in either pixels or percentage
 horizontal-align optional One of right, left, or center
 vertical-align optional One of top, bottom, or middle
 wrap optional One of right, left, or through, this controls where text will appear to either side of the image
 top-margin optional Given in pixels, how much space to keep between the top of the image and other objects
 bottom-margin optional Given in pixels, how much space to keep between the bottom of the image and other objects
 left-margin optional Given in pixels, how much space to keep between the left of the image and other objects
 right-margin optional Given in pixels, how much space to keep between the right of the image and other objects
 */
 	private String		src;
 	private String		alt;
 	private Const		width_type		= Const.WIDTH_PIXELS;
 	private double		width			= 0.00;
 	private Const		height_type		= Const.HEIGHT_PIXELS;
 	private double		height			= 0.00;
 	private Const		horizontal_align= Const.ALIGN_LEFT;
 	private Const		vertical_align	= Const.ALIGN_MIDDLE;
 	private int			margin_top		= 0;
 	private int			margin_left		= 0;
 	private int			margin_right	= 0;
 	private int			margin_bottom	= 0;
 
 	public EmbeddedImage(Emitter parent, Node contents) {
 		super(parent, contents);
 	}
 
 	@Override
 	public Emitter parse() {
 		NamedNodeMap nnm = contents.getAttributes();
 		Node n; String str;
 		// image source url
 		n = nnm.getNamedItem("src");
 		if(n!=null) {
 			src = n.getTextContent();
 			Collection<String> forbiddenLinks = (Collection<String>)
 					getConfig().get("forbiddenLinks");
 			if(forbiddenLinks!=null)
 				for(String rgx:forbiddenLinks) {
 					if(src.matches(rgx)) {
 						src = "";
 						reportProblem("TAGS_IMAGE_ILLEGAL_URL");
 						break;
 					}
 				}
 		}
 		// alternate text
 		n = nnm.getNamedItem("alt");
 		if(n!=null)
 			alt = n.getTextContent();
 		// width
 		n = nnm.getNamedItem("width");
 		if(n!=null) {
 			str = n.getTextContent();
 			if(str.endsWith("%")) {
 				width_type=Const.WIDTH_PERCENT;
 				str = str.replaceAll("%", "");
 			} else if(str.endsWith("px")) {
 				width_type=Const.WIDTH_PIXELS;
 				str = str.replaceAll("px", "");
 			} else {
 				if(str.matches("[\\d]*")) {
 					width_type=Const.WIDTH_PIXELS;
 					if(str.equals(""))
 						width = 0.00;
 					else
 						width = Double.parseDouble(str);
 				} else {
 					reportProblem("TAGS_IMAGE_BAD_WIDTH");
 					width_type=Const.WIDTH_PIXELS;
 					str = "0.00";
 				}
 			}
 			if(str.equals(""))
 				str="0.00";
 			try {
 				width = Double.parseDouble(str);
 			} catch (NumberFormatException e) {
 				reportProblem("TAGS_IMAGE_BAD_WIDTH");
 				width = 0.00;
 			}
 		}
 		// height
 		n = nnm.getNamedItem("height");
 		if(n!=null) {
 			str = n.getTextContent();
 			if(str.endsWith("%")) {
 				height_type=Const.HEIGHT_PERCENT;
 				str = str.replaceAll("%", "");
 			} else if(str.endsWith("px")) {
 				height_type=Const.HEIGHT_PIXELS;
 				str = str.replaceAll("px", "");
 			} else {
 				if(str.matches("[\\d]*")) {
 					height_type=Const.HEIGHT_PIXELS;
 					if(str.equals(""))
 						height = 0.00;
 					else
 						height = Double.parseDouble(str);
 				} else {
 					reportProblem("TAGS_IMAGE_BAD_HEIGHT");
 					height_type=Const.HEIGHT_PIXELS;
 					str = "0.00";
 				}
 			}
 			if(str.equals(""))
 				str="0.00";
 			try {
 				height = Double.parseDouble(str);
 			} catch (NumberFormatException e) {
 				reportProblem("TAGS_IMAGE_BAD_HEIGHT");
 				height = 0.00;
 			}
 		}
 		// horizontal align
 		n = nnm.getNamedItem("horizontal-align");
 		if(n!=null) {
 			str = n.getTextContent();
 			if(str.equalsIgnoreCase("right"))
 				horizontal_align=Const.ALIGN_RIGHT;
 			else if(str.equalsIgnoreCase("left"))
 				horizontal_align=Const.ALIGN_LEFT;
 			else if(str.equalsIgnoreCase("center"))
 				horizontal_align=Const.ALIGN_CENTER;
 			else
 				reportProblem("TAGS_IMAGE_BAD_HORIZONTAL_ALIGN");
 		}
 		// vertical align
 		n = nnm.getNamedItem("vertical-align");
 		if(n!=null) {
 			str = n.getTextContent();
 			if(str.equalsIgnoreCase("top"))
 				vertical_align=Const.ALIGN_TOP;
 			else if(str.equalsIgnoreCase("middle"))
 				vertical_align=Const.ALIGN_MIDDLE;
 			else if(str.equalsIgnoreCase("bottom"))
 				vertical_align=Const.ALIGN_BOTTOM;
 			else
 				reportProblem("TAGS_IMAGE_BAD_VERTICAL_ALIGN");
 		}
 		// margin right
 		n = nnm.getNamedItem("margin-right");
 		if(n!=null) {
 			str = n.getTextContent();
 			str = str.replaceAll("px", "");
 			try {
 				margin_right = Integer.parseInt(str);
 			} catch(NumberFormatException e) {
 				reportProblem("TAGS_IMAGE_BAD_MARGIN_RIGHT");
 			}
 		}
 		// margin left
 		n = nnm.getNamedItem("margin-left");
 		if(n!=null) {
 			str = n.getTextContent();
 			str = str.replaceAll("px", "");
 			try {
 				margin_left = Integer.parseInt(str);
 			} catch(NumberFormatException e) {
 				reportProblem("TAGS_IMAGE_BAD_MARGIN_LEFT");
 			}
 		}
 		// margin top
 		n = nnm.getNamedItem("margin-top");
 		if(n!=null) {
 			str = n.getTextContent();
 			str = str.replaceAll("px", "");
 			try {
 				margin_top = Integer.parseInt(str);
 			} catch(NumberFormatException e) {
 				reportProblem("TAGS_IMAGE_BAD_MARGIN_TOP");
 			}
 		}
 		// margin bottom
 		n = nnm.getNamedItem("margin-bottom");
 		if(n!=null) {
 			str = n.getTextContent();
 			str = str.replaceAll("px", "");
 			try {
 				margin_bottom = Integer.parseInt(str);
 			} catch(NumberFormatException e) {
 				reportProblem("TAGS_IMAGE_BAD_MARGIN_BOTTOM");
 			}
 		}
 
 		return super.parse();
 	}
 
 	public StringBuilder emitHtml() {
 		if(src.equals(""))
 			return new StringBuilder();
 
 		StringBuilder emission = new StringBuilder();
 
 		// opening tag and image source link
 		emission.append("<img src=\"" + src + "\"");
 
 		// image alternate text
 		emission.append(" alt=\"");
 		if(alt!=null&&!alt.equals(""))
 			emission.append(alt);
 		emission.append("\"");
 
 		// image height
 		if(!(height_type==Const.HEIGHT_PIXELS&&height==0.00)) {
 			emission.append(" height=\""+Double.toString(height));
 			if(height_type==Const.HEIGHT_PERCENT)
 				emission.append("%");
 			else
 				emission.append("px");
 			emission.append("\"");
 		}
 
 		// image width
 		if(!(width_type==Const.WIDTH_PIXELS&&width==0.00)) {
 			emission.append(" width=\""+Double.toString(width));
 			if(width_type==Const.WIDTH_PERCENT)
 				emission.append("%");
 			else
 				emission.append("px");
 			emission.append("\"");
 		}
 
 		// style-based stuff
 		StringBuilder styles = new StringBuilder();
 
		// horizontal align
		if(horizontal_align!=Const.ALIGN_LEFT) {
			if(horizontal_align==Const.ALIGN_RIGHT)
				styles.append("right:auto;");
			else if(horizontal_align==Const.ALIGN_CENTER)
				styles.append("left:50%;");
		}

 		// vertical align
 		if(vertical_align!=Const.ALIGN_MIDDLE) {
 			if(vertical_align==Const.ALIGN_TOP)
 				styles.append("top:auto;");
 			else if(vertical_align==Const.ALIGN_BOTTOM)
 				styles.append("bottom:auto;");
 		}
 
 		// top margin
 		if(margin_top!=0)
 			styles.append("margin-top:"+Integer.toString(margin_top)+"px;");
 
 		// left margin
 		if(margin_left!=0)
 			styles.append("margin-left:"+Integer.toString(margin_left)+"px;");
 
 		// right margin
 		if(margin_right!=0)
 			styles.append("margin-right:"+Integer.toString(margin_right)+"px;");
 
 		// bottom margin
 		if(margin_bottom!=0)
 			styles.append("margin-bottom:"+Integer.toString(margin_bottom)
 					+"px;");
 
 		// append relevant styles
 		if(styles.length()!=0)
 			emission.append(" style=\"" + styles + "\"");
 
 		emission.append("/>");
 
 		return emission;
 	}
 
 }
