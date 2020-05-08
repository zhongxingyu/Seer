 package fr.ujm.tse.info4.pgammon.vues;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Paint;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import fr.ujm.tse.info4.pgammon.gui.CaseButton;
 import fr.ujm.tse.info4.pgammon.gui.DeButton;
 import fr.ujm.tse.info4.pgammon.gui.TriangleCaseButton;
 import fr.ujm.tse.info4.pgammon.models.Case;
 import fr.ujm.tse.info4.pgammon.models.CouleurCase;
 import fr.ujm.tse.info4.pgammon.models.DeSixFaces;
 import fr.ujm.tse.info4.pgammon.models.Partie;
 import fr.ujm.tse.info4.pgammon.models.Tablier;
 
 public class VueTablier extends JPanel{
 	
 	private static final long serialVersionUID = -7479996235423541957L;
 	private Partie partie;
 	private Tablier tablier;
 	private ArrayList<CaseButton> casesButtons;
 	private CaseButton candidat;
 	private List<DeButton> desButton;
 	public VueTablier(Partie partie) {
 		this.partie = partie;
 		this.tablier = partie.getTablier();
 		this.casesButtons = new ArrayList<>();
 		this.setCandidat(null);  
 		build();
 	}
 
 	public CaseButton getCandidat() {
 		return candidat;
 	}
 
 	public void setCandidat(CaseButton new_candidat) {
 		if(new_candidat == this.candidat) return;
 		
 		if(this.candidat != null)
 			this.candidat.setCandidated(false);
 		
 		new_candidat.setCandidated(true);
 		this.candidat = new_candidat;
 	}
 
 	public void uncandidateAll() {
 		if(this.candidat != null)
 			this.candidat.setCandidated(false);
 		
 		this.candidat = null;
 	}
 	private void build() {
 		setLayout(null);
 		this.setPreferredSize(new Dimension(550,450));
 		
 		for(Case c : tablier.getListeCase()){
 			creerTriangle(c.getPosition(),c);
 		}
 		for(Case c : tablier.getCaseBarre()){
 			creerCasesBarres(c);
 		}
 		for(Case c : tablier.getCaseVictoire()){
 			creerCasesVictoires(c);
 		}
 		updateDes();
 	}
 	
 	private void creerCasesVictoires(Case c){
 		CaseButton btn = new TriangleCaseButton(c,c.getCouleurDame());
 		int pos_x = 671-173;
 		int pos_y = 30;
 		
 		if(c.getCouleurDame() == CouleurCase.NOIR)
 			pos_y = 266;
 		
 		btn.setBounds(pos_x, pos_y,
 				btn.getPreferredSize().width , btn.getPreferredSize().height);
 		
 		add(btn);
 		casesButtons.add(btn);
 	}
 	
 	private void creerCasesBarres(Case c){
 		CaseButton btn = new TriangleCaseButton(c,c.getCouleurDame());
 		int pos_x = 425-173;
 		int pos_y = 30;
 		
 		if(c.getCouleurDame() == CouleurCase.BLANC)
 			pos_y = 266;
 		
 		btn.setBounds(pos_x, pos_y,
 				btn.getPreferredSize().width , btn.getPreferredSize().height);
 		
 		add(btn);
 		casesButtons.add(btn);
 	}
 	
 	private void creerTriangle(final int position,final Case c) {
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
 		casesButtons.add(triangle);
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
 	
 
 	
 	public void updateDes(){
 
 		List<DeSixFaces> des = partie.getDeSixFaces();
 		
 		if(desButton != null){
 			for(DeButton de_btn : desButton){
 				remove(de_btn);
 			}
 		}
 		desButton = new ArrayList<>();
 		
 		int size = des.size();
 		int i = 0;
 		if(size>0)
 			for(DeSixFaces de : des){
 				DeButton btn = new DeButton(de);
 				int y = (int) (252 + 40*((float)i-size/2));
 				btn.setBounds(427-173, y,
 						btn.getPreferredSize().width , btn.getPreferredSize().height);
 				add(btn);
 				i++;
 			}
 	}
 	
 	public List<CaseButton> getCasesButtons() {
 		return casesButtons;
 	}
 
 }
