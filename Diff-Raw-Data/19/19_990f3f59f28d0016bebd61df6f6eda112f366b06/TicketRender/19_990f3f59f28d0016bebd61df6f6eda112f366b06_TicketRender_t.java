 package sh.app.ticket_printer.ticket;
 
 import static sh.app.ticket_printer.PrinterApplet.isLogEnabled;
 import static sh.app.ticket_printer.ticket.model.AbstractTicketElement.DEFAULT_ROTATION_VALUE;
 
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.font.TextAttribute;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.security.InvalidParameterException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 import sh.app.ticket_printer.PrinterApplet;
 import sh.app.ticket_printer.ticket.model.AbstractTicketElement;
 import sh.app.ticket_printer.ticket.model.Barcode;
 import sh.app.ticket_printer.ticket.model.Form;
 import sh.app.ticket_printer.ticket.model.Image;
 import sh.app.ticket_printer.ticket.model.Text;
 import sh.app.ticket_printer.ticket.model.TicketPart;
 
 public class TicketRender {
 
 	private static final float transformToPxs = 28f / 100f;
 
 	public static void render(Ticket ticket, Graphics2D g) {
 		if (PrinterApplet.isLogEnabled()) {
             System.out.println("Entering TicketRender.render()");
         }
     	
 		renderTicketPart(ticket.getForm(), g);
 
 		for (AbstractTicketElement attr : ticket.getElements()) {
 			renderTicketPart(attr, g);
 		}
 	}
 
 	private static void renderTicketPart(TicketPart part, Graphics2D g) {
 		switch (part.getType()) {
 		case FORM:
 			renderForm((Form) part, g);
 			break;
 		case TEXT:
 			renderText((Text) part, g);
 			break;
 		case IMAGE:
 			renderImage((Image) part, g);
 			break;
 		case BARCODE:
 			renderBarcode((Barcode) part, g);
 			break;
 		default:
 			throw new InvalidParameterException("Unknown ticket part type");
 		}
 	}
 
     private static void renderForm(Form form, Graphics2D g) {
 		if (form.hasBorder()) {
 			g.draw(new Rectangle2D.Double(0, 0, form.getWidth() * transformToPxs, form.getHeight() * transformToPxs));
 		}
 	}
 
     private static void renderText(Text text, Graphics2D g) {
         int textStyle = Font.PLAIN;
         if (text.isBold()) {
             textStyle |= Font.BOLD;
         }
         if (text.isItalic()) {
             textStyle |= Font.ITALIC;
         }
 
         Font font = new Font(text.getFontName(), textStyle, text.getFontSize());
 
         if (text.isUnderline()) {
             Map<TextAttribute, Integer> attributes = new HashMap<TextAttribute, Integer>();
             attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
             font = font.deriveFont(attributes);
         }
 
         AffineTransform transform = buildTransform(text);
         if (transform != null) {
             font = font.deriveFont(transform);
         }
 
         g.setFont(font);
        
        int dx, dy;
        switch (text.getRotation().intValue()) {
            case 0 : dx = 0; dy = 1;
                break;
            case 1 : dx = 1; dy = 0;
                break;
            case 2 : dx = 0; dy = 0;
                break;
            case 3 : dx = -1; dy = 0;
                break;
            default : dx = 0; dy = 0;
                break;
        }

        g.drawString(text.getData(), text.getPosX() * transformToPxs + dx * text.getFontSize(),
                text.getPosY() * transformToPxs + dy * text.getFontSize());
     }
 
 	private static void renderBarcode(Barcode barcode, Graphics2D g) {
 	}
 
 	private static void renderImage(Image image, Graphics2D g) {
 	    try {
 	        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image.getData()));
 	        double x = image.getPosX().intValue() * transformToPxs;
 	        double y = image.getPosY().intValue() * transformToPxs;
 	        double angle = -Math.toRadians(90 * image.getRotation().intValue());
 	        
 	        AffineTransform transform = new AffineTransform();
 	        transform.setToTranslation(x, y);
 	        transform.rotate(angle);
 	        g.drawImage(bufferedImage, transform, null);
 	    } catch (IOException e) {
 	        if (isLogEnabled()) {
 	            System.err.println("WARNING: Caught exception while rendering Image: " + e);
 	        }
         }
 	}
 	
     private static AffineTransform buildTransform(AbstractTicketElement element) {
         if (DEFAULT_ROTATION_VALUE.equals(element.getRotation())) {
             return null;
         }
 
         return AffineTransform.getRotateInstance(-Math.toRadians(90 * element.getRotation().intValue()));
     }
 }
