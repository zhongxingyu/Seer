 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 public class Fenetre extends JFrame{
 	private static final long serialVersionUID = 1L;
 	private Plateau p = new Plateau();
 	private String mot, test;
 	private JTextField jtf = new JTextField();
 	private JPanel jp=new JPanel();
 	private Chrono c=new Chrono();
 	private Toolkit t=getToolkit();
 	private Image i=t.getImage("wallpaper.jpg");
 	static boolean finMot=false, finJeu=false;
 	static int mots=1;
 
 	public Fenetre() { // Construction de la fentre
 		configFrame();
 		creeJTF();
 	}
 	public void creeJTF(){
 		jp.setLayout(null);
 		jtf.setFont(new Font("Arial", Font.BOLD, 14));
         jtf.setSize(150, 30);
         jtf.setLocation(547, 180);
         jtf.addKeyListener(new MyKey());
         jp.add(jtf);
         add(jp);
 	}
 	public void configFrame(){
 		setTitle("Motus -- Level: CM1,CM2 -- By Bryan Vergauwen");
 		setSize(950,670);
 		setLocationRelativeTo(null);
 		setResizable(false);
 		setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 	public String getTest(){
 		return jtf.getText();
 	}
 	public void resetJtf() {
 		jtf.setText("");
 	}
 	public JTextField getJtf(){
 		return jtf;
 	}
 	public Chrono getChrono(){
 		return c;
 	}
 	public Plateau getPlateau(){
 		return p;
 	}
 	public void setMot(String mot){
 		this.mot=mot;
 	}
 	public void setTest(String test){
 		this.test=test;
 	}
 	public void modif(Graphics g){ // Ecriture sur le plateau
 	    g.setFont(new Font("Serif", Font.PLAIN, 50));
		g.setColor(new Color(255,255,255));
 		for(int i=0; i<p.getTab().length; i++){
 			for(int j=0; j<p.getTab().length; j++){
 				if(p.getTab()[i][j]==mot.charAt(j)){
 					g.setColor(new Color(204,0,0));
 					g.fillRect(60+65*j, 150+65*i, 65, 65);
 					g.setColor(new Color(255,255,255));
 					g.drawString(Character.toUpperCase(p.getTab()[i][j])+"", 75+65*j, 198+65*i);
 				}
 				else if(p.getTab()[i][j]=='0')
 					g.drawString(" .", 75+65*j, 198+65*i);
 				else if(p.getTab()[i][j]=='2'){
 					p.getTab()[i][j]=test.charAt(j);
 					g.drawString(Character.toUpperCase(p.getTab()[i][j])+"", 75+65*j, 198+65*i);
 				}
 				else{
 					g.setColor(new Color(255,204,0));
 					if(p.getTab()[i][j]!=' ' && (p.getTab()[i][j]==mot.charAt(0) || p.getTab()[i][j]==mot.charAt(1) || p.getTab()[i][j]==mot.charAt(2) || p.getTab()[i][j]==mot.charAt(3) || p.getTab()[i][j]==mot.charAt(4) || p.getTab()[i][j]==mot.charAt(5) || p.getTab()[i][j]==mot.charAt(6)))
 						g.fillOval(60+65*j, 150+65*i, 63, 63);
 					g.setColor(new Color(255,255,255));
 					g.drawString(Character.toUpperCase(p.getTab()[i][j]) + "", 75+65*j, 198+65*i);
 				}
 			}
 		}
 	}
 	public void display(Graphics g){
 		// Affichage wallpaper
 		if(Fonctions.display)
 			g.drawImage(i, 0, 0, this);
 		// Actualisation affichage Jtf
 		jtf.repaint();
 	    // Affichage Chrono
 		g.setFont(new Font("Serif", Font.PLAIN, 50));
 		g.setColor(new Color(255,255,240));
 		g.fillRect(789, 60, 120, 60);
 		g.setColor(new Color(0,0,0));
 		g.drawRect(788, 60, 120, 60);
 		g.drawString(c.getChrono(), 790, 110);
 		// Affichage infos
         g.setFont(new Font("sansserif", Font.PLAIN, 30));
 		g.drawString("Jeu du Motus!", 60, 80);
 		g.drawString("Vous jouez avec la lettre " + Character.toUpperCase(mot.charAt(0)), 60, 115);
 		g.drawString("Entrez votre mot ici: ", 550, 190);
 		// Affichage Plateau
 		if(Fonctions.display){
 			for(int i=0; i<7; i++){
 				for(int j=0; j<7; j++){
 					g.setColor(new Color(51,102,255));
 					g.fillRect(60+50*i, 150+50*j, 157, 157);
 				}
 			}
 			for(int i=0; i<7; i++){
 				for(int j=0; j<7; j++){
 					g.setColor(new Color(255,255,255));
 					g.drawRect(60+65*i, 150+65*j, 65, 65);
 				}
 			}
 		}
 	}
 	public void testFin(Graphics g){ // Test x mot sur 10
 		g.setFont(new Font("sansserif", Font.PLAIN, 30));
 	    g.setColor(new Color(0,0,0));
 		if(finJeu){
 			g.setFont(new Font("sansserif", Font.ITALIC, 30));
 		    if(!mot.equals(test)){
 		    	g.drawString("Temps coul ... ", 550, 300);
 		    	g.drawString("Dommage!", 550, 340);
 		    }
 		    else{
 		    	g.drawString("Flicitations, vous avez", 550, 300);
 		    	g.drawString("fini le jeu!", 550, 340);
 		    }
 			g.drawString(" bientt :)", 550, 530);
 		}
 		else if(finMot){
 			if(mot.equals(test)){
 				g.drawString("Bravo, vous avez trouv", 550, 300);
 				g.drawString("le mot mystre :)",550, 340);
 			}
 			else{
 				g.drawString("Dommage. Le mot tait ", 550, 300);
 				g.drawString("\"" + mot.toUpperCase() + "\"", 550, 340);
 			}
 			finMot=false;
 		    g.setFont(new Font("Arial", Font.ITALIC, 30));
 			g.drawString("Chargement en cours...", 550, 470);
 		    g.setFont(new Font("Serif", Font.PLAIN, 30));
 		}
 		g.setFont(new Font("sansserif", Font.PLAIN, 30));
 		g.setColor(new Color(255,0,0));
 		g.drawString("Mot " + mots + " sur 10", 550, 100);
 	}
 	public void paint(Graphics g){
 		display(g);
 		modif(g);
 		testFin(g);
 	}
 }
