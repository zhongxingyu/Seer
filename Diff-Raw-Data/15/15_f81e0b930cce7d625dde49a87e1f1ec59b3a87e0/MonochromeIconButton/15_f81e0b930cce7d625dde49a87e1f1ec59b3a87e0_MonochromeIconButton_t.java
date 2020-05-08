 package fr.ujm.tse.info4.pgammon.gui;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Paint;
 import java.awt.RadialGradientPaint;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 
 import fr.ujm.tse.info4.pgammon.models.CouleurCase;
 
 public class MonochromeIconButton extends JButton{
 	private static final long serialVersionUID = -8152217751563954978L;
 	
 	public enum Size
 	{
 		SMALL,
 		BIG
 	};
 	public static final String BLANC = "BALNC";
 	public static final String NOIR  = "NOIR";
 
 	private String couleur;
 	private ImageIcon icon;
 	private JLabel label; 
 	private Size size;
 	
 	public MonochromeIconButton(String iconRef, String text, String couleur){
 		size = Size.SMALL;
 		this.couleur = couleur;
 		
 		build(iconRef, text);
 		
 	}
 	
 	public MonochromeIconButton(String iconRef, String text){
 		size = Size.SMALL;
 		couleur = BLANC;
 		build(iconRef, text);
 		
 	}
 
 	private void build(String iconRef, String text) {
		setOpaque(false);
 		icon = new ImageIcon(iconRef);
 		label = new JLabel(text);
 	}
 	
 	
 	@Override
 	protected void paintComponent(Graphics g) {
 		
 		Graphics2D g2 = (Graphics2D) g.create(); 
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
 		if(isEnabled()==false)
 			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.3f));
 		
 		int h = getHeight(); 
 		int w = getWidth(); 
 		
 		Paint p;
 		
 		//Fond
 		if (!model.isEnabled()) { 
 			setForeground(Color.GRAY);
 			p = new Color(0x444444);
 		}else{ 
 			if (model.isRollover()) 
 				p = (couleur.equals(BLANC))?new Color(0xAAAAAA):new Color(0x666666);
 			else 
 				p = (couleur.equals(BLANC))?new Color(0xCCCCCC):new Color(0x888888);
 		} 
 		g2.setPaint(p); 
 		g2.fillRect(0, 0, w, h); 
 		
 		
 		// Bordure
 		if (model.isPressed())
 			p = (couleur.equals(BLANC))?new Color(0x777777):new Color(0x333333);
 		else 
 			p = (couleur.equals(BLANC))?new Color(0x888888):new Color(0x444444);
 		g2.setStroke(new BasicStroke(5.0f) );
 		g2.setPaint(p); 
 		g2.drawRect(0, 0, w-1 , h-1);
 		
 		// Icone
 		int off_x = (size==Size.BIG)?3:-1;
 		int off_y = (size==Size.BIG)?3:-1;
 		g2.drawImage(icon.getImage(),off_x,off_y,this);
 		
 		g2.dispose(); 
 	}
 	
 	@Override
 	protected void paintBorder(Graphics g) {
 		//super.paintBorder(g);
 	}
 
 	public ImageIcon getIcon() {
 		return icon;
 	}
 
 	public String getLabel() {
 		return label.toString();
 	}
 	
 	public void setSizeSmall(){
 		setPreferredSize(new Dimension(40, 40));
 		size = Size.SMALL;
 	}
 	
 	public void setSizeBig(){
 
 		setPreferredSize(new Dimension(60, 60));
 		size = Size.BIG;
 	}
 }
