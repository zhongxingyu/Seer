 package fr.ujm.tse.info4.pgammon.gui;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Paint;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.geom.Rectangle2D;
 
 import javax.swing.ButtonModel;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 
 public class MonochromeButton extends JButton {
 	
 	private static final long serialVersionUID = 1L;
 	String text;
 	JLabel label;
 	public MonochromeButton(){
 		super();
 		
 	}
 
 	public MonochromeButton(String str) {
 
 		super();
 		text = str;
 
 		build();
 	}
 
 	public MonochromeButton(String text, Icon icon) {
 		super();
 		this.text = text;
 		build();
 	}
 
 	private void build() {
 		// TODO Auto-generated method stub
 		label = new JLabel(text);
 		label.setFont(new Font("Arial",Font.PLAIN,20));
 		label.setPreferredSize(getPreferredSize());
 		label.setAlignmentX(CENTER_ALIGNMENT);
 		label.setForeground(new Color(0xF2F2F2));
 		add(label);
 	}
 	
 	@Override
 	public void setPreferredSize(Dimension preferredSize) {
 		// TODO Auto-generated method stub
 		super.setPreferredSize(preferredSize);
 		label.setPreferredSize(preferredSize);
 	}
 	@Override
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g); 
 		Graphics2D g2 = (Graphics2D) g.create(); 
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
 		
 		int h = getHeight(); 
 		int w = getWidth(); 
 		
 		
 		ButtonModel model = getModel(); 
 
 		Paint p;
 		
 		if (!model.isEnabled()) { 
 			setForeground(Color.GRAY);
 			p = new Color(0x444444);
 		}else{ 
 			if (model.isRollover()) { 
 				p = new Color(0x333333);
 
 			} else { 
 				p = new Color(0x222222);
 				
 			} 
 		} 
 		g2.setPaint(p); 
 		
 		Paint p1; 
 		Paint p2; 
 		if (model.isPressed()) { 
 			p1 = new Color(0x888888);
 			p2 = new Color(0x555555);
 		} else { 
 			p1 = new Color(0x555555);
 			p2 = new Color(0x888888);
 		} 
 		Rectangle2D.Float r2d = new Rectangle2D.Float(0, 0, w - 1, h - 1); 
 		
 		Shape clip = g2.getClip(); 
 		
 		g2.clip(r2d); 
 		g2.fillRect(0, 0, w, h); 
 		
 		g2.setClip(clip); 
 		
 		g2.setStroke(new BasicStroke(5.0f) );
 		
 		g2.setPaint(p1); 
 		g2.drawRect(0, 0, w - 1, h - 1);
 		g2.setPaint(p2); 
 		g2.drawRect(1, 1, w - 3, h - 3);
 			
 		//
 		g2.dispose(); 
 		
 	}
 	
 	@Override
 	protected void paintBorder(Graphics g) {
 		
 		// TODO Auto-generated method stub
 		//super.paintBorder(g);
 		
 	}
 
 
 
 }
