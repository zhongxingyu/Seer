 package fr.ujm.tse.info4.pgammon.vues;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Paint;
 import java.awt.Point;
 import java.awt.RadialGradientPaint;
 import java.awt.RenderingHints;
 import java.awt.geom.Point2D;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import fr.ujm.tse.info4.pgammon.gui.TriangleCaseButton;
 import fr.ujm.tse.info4.pgammon.models.Case;
 import fr.ujm.tse.info4.pgammon.models.CouleurCase;
 import fr.ujm.tse.info4.pgammon.models.Partie;
 import fr.ujm.tse.info4.pgammon.models.Tablier;
 
 public class VueTablier extends JPanel{
 	
 	private static final long serialVersionUID = -7479996235423541957L;
 	private Partie partie;
 	private Tablier tablier;
 
 	public VueTablier(Partie partie) {
 		this.partie = partie;
 		this.tablier = partie.getTablier();
 		build();
 	}
 
 	private void build() {
 		setLayout(null);
 		this.setPreferredSize(new Dimension(550,450));
 		List<Case> cases = tablier.getListeCase();
 		
 		for(Case c : cases){
 			createTriangle(c.getPosition(),c);
 		}
 	}
 
	private void createTriangle(final int position,final  Case c) {
		int num = 25-position;
 		Point p = new Point(0,0);
 		if(num<=6)
 		{
 			p = new Point(454-(num-1)*33,30);
 		}else if(num<=12)
 		{
 			p = new Point(392-173-(num-7)*33,30);
 		}else if(num<=18)
 		{
 			p = new Point(392-173+(num-18)*33,233);
 		}else if(num<=24)
 		{
 			p=new Point(0,0);
 			p = new Point(454+(num-24)*33,233);
 		}
 		CouleurCase couleur = (num%2!=0)?CouleurCase.BLANC:CouleurCase.NOIR;
 		TriangleCaseButton triangle = new TriangleCaseButton(c,couleur,(num >= 13)); 
 		triangle.setBounds(p.x, p.y,
 				triangle.getPreferredSize().width , triangle.getPreferredSize().height);
 		System.out.println(triangle.getPreferredSize());
 		add(triangle);
 	}
 	
 	
 	@Override
 	protected void paintComponent(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g.create(); 
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
 		
 		Paint p;
 		int h = getHeight(); 
 		int w = getWidth(); 
 		
 		// Arriere plan
 		p = new Color(0x333333);
 		g2.setPaint(p); 
 		g2.fillRect(0, 0, w, h); 
 		
 		// Fond tablier
 		p = new Color(0xCCCCCC);
 		g2.setPaint(p); 
 		g2.fillRect(226-173, 61-30, 435 , 382); 
 
 		
 		p = new Color(0x333333);
 		g2.setPaint(p); 
 		g2.fillRect(252, 31, 36 ,387); 
 		
 		g2.dispose(); 
 	}
 	
 	@Override
 	protected void paintBorder(Graphics g) {
 		// Bordure
 		Graphics2D g2 = (Graphics2D) g.create(); 
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
 
 		Paint p;
 		int h = getHeight(); 
 		int w = getWidth(); 
 		p = new Color(0x808080);
 		g2.setStroke(new BasicStroke(10.0f) );
 		g2.setPaint(p); 
 		g2.drawRect(0, 0, w-1, h-1);
 		
 		
 		g2.dispose(); 
 		
 	}
 }
