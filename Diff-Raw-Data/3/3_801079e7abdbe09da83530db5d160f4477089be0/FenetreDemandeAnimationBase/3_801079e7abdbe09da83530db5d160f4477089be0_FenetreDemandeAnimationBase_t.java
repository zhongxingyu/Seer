 package fr.ujm.tse.info4.pgammon.gui;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Paint;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 
 import javax.swing.JButton;
 
 import fr.ujm.tse.info4.pgammon.models.CouleurCase;
 
 public class FenetreDemandeAnimationBase extends TranstionAnimeeBase{
 	
 	private static final long serialVersionUID = -4787023438548267991L;
 	private char[] c_titre;
 	private boolean isClosing;
 	private SortedSet<String> reponses;
 	private HashMap<MonochromeButton, Integer> corresp;
 	
 	public FenetreDemandeAnimationBase(String titre, String text, SortedSet<String> reponses) {
 		super(10,300);
 		setLayout(null);
 		isClosing = false;
 		setTitle(titre);
 		setTitle(text);
 		this.reponses = reponses;
 		buildResponses();
 		setOpaque(false);
 		
 	}
 	
 	private void buildResponses() {
 		corresp = new HashMap<>();
 		if(reponses == null)
 		{
 			addMouseListener(new MouseListener() {
 				
 				@Override
 				public void mouseReleased(MouseEvent arg0) {}
 				@Override
 				public void mousePressed(MouseEvent arg0) {
 					close();
 				}
 				@Override
 				public void mouseExited(MouseEvent arg0) {}
 				@Override
 				public void mouseEntered(MouseEvent arg0) {}
 				@Override
 				public void mouseClicked(MouseEvent arg0) {}
 			});
 			
 			return;
 		}
 		int i = 0;
 		for (String reponse : reponses) {
			System.out.println(reponse);
 			MonochromeButton btn = new MonochromeButton(reponse);
 			btn.addMouseListener(new MouseListener() {
 				@Override
 				public void mouseReleased(MouseEvent arg0) {}
 				@Override
 				public void mousePressed(MouseEvent evt) {
 					close();
 					System.out.println(evt);
 					dispatchEvent(evt);
 
 				}
 				@Override
 				public void mouseExited(MouseEvent arg0) {}
 				@Override
 				public void mouseEntered(MouseEvent arg0) {}
 				@Override
 				public void mouseClicked(MouseEvent arg0) {}
 			});
 
 			add(btn);
 
 			btn.setBounds(600- 200*i,280,150,45);
 			corresp.put(btn,i);
 			i++;
 		}
 	}
 
 	public void close(){
 		isClosing = true;
 		start();
 	}
 
 	public void setTitle(String title){
 		c_titre = title.toCharArray();
 	}
 	private Point getTitlePosition(){
 		double p = (0.5-getRapport())*(0.5-getRapport())*(0.5-getRapport());
 		
 		return new Point((int) (100),(int) (245 + 400 * p) );
 	}
 	
 	
 	private Point getTextPosition(){
 		return new Point((int) (-200+200 * getRapport()),450 );
 	}
 	
 	private Point getTextPosition2(){
 		return new Point((int) (-300+300 * getRapport()),520 );
 	}
 	
 	private float getTitleAlpha(){
 		float alpha = 2*(0.5f-getRapport());
 		alpha *= alpha;
 		alpha = 1-alpha;
 		if(alpha<0) alpha = 0;
 		if(alpha>1)alpha=1;
 		return alpha;
 	}
 	
 	private float getBGAlpha(){
 		float alpha = 2*(0.5f-getRapport());
 		alpha *= alpha;
 		alpha = 1-alpha;
 		if(alpha<0) alpha = 0;
 		if(alpha>1)alpha=1;
 		return alpha;
 	}
 	@Override
 	protected void paintComponent(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g.create();
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		
 		drawFullBackground(g2);
 		drawBandeau(g2);
 		drawTitle(g2);
 		drawButton(g2);
 
 		g2.dispose(); 
		paintComponents(g);
 	}
 
 
 	private void drawButton(Graphics2D g2) {
 		Collection<MonochromeButton> btns = corresp.keySet();
 		for(MonochromeButton btn : btns)
 			btn.setAlpha(getBGAlpha());
 	}
 
 
 	private void drawTitle(Graphics2D g2) {
 		g2.setFont(new Font("Arial",Font.ITALIC | Font.BOLD,45));
 		g2.setColor(new Color(0xFFFFFF));
     	g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTitleAlpha()));
 
 		Point p = getTitlePosition();
 		g2.drawChars(c_titre, 0, c_titre.length,p.x, p.y);
 	}
 
 
 	private void drawBandeau(Graphics2D g2) {
 
 		int h = getHeight(); 
 		int w = getWidth(); 
 		
 		Paint p;
 
     	g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f*getBGAlpha()));
 		p = new Color(0x333333);
 		 
 		g2.setPaint(p); 
 		g2.fillRect(0, 200, w, 150); 
 
     	g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
 	}
 
 
 	private void drawFullBackground(Graphics2D g2) {
 
 		int h = getHeight(); 
 		int w = getWidth(); 
 		
 		Paint p;
 
     	g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f*getBGAlpha()));
 		p = new Color(0);
 		g2.setPaint(p); 
 		g2.fillRect(0, 0, w, h); 
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		super.actionPerformed(e);
 		if(!isClosing && 2*value>duree)
 			stop();
 	}
 	
 	
 }
