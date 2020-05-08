 package client.view.jeu;
 
 import java.awt.GridLayout;
 
 import javax.swing.JLabel;
 import javax.swing.JProgressBar;
 import javax.swing.border.EmptyBorder;
 
 import client.view.RoundedPanel;
 
 import common.Constante;
 import common.ElementPlateau;
 import common.Translator;
 import common.TypeElementPlateau;
 import common.partie.batiment.TypeBatiment;
 import common.partie.unite.Unite;
 
 /**
  * @author omar
  */
 public class ToolTipInfo extends RoundedPanel{
 	
 	private static final long serialVersionUID = Constante.NUMERO_DE_VERSION;
 	private ElementPlateau element;
 	private int niveau;
 	
	private final static JLabel labelType = new JLabel();
	private final static JLabel labelPtsVie = new JLabel();
	private final static JProgressBar progressBar = new JProgressBar();
 	
	private final static JLabel labelInfo1 = new JLabel();
	private final static JLabel labelInfo2 = new JLabel();
	
	private final static int LARGEUR_TOOLTIP = 200;
	private final static int HAUTEUR_TOOLTIP = 100;
 	
 	public ToolTipInfo(){
 		setBorder(new EmptyBorder(10,10,10,10));
 		
 		setLayout(new GridLayout(5,1));
 		add(labelType);
 		add(labelPtsVie);
 		labelPtsVie.setText(Translator.translate("PointsVie") + " : ");
 		
 		progressBar.setMinimum(0);
 		progressBar.setStringPainted(true);
 		add(progressBar);
 		
 		add(labelInfo1);
 		add(labelInfo2);
 	}
 	
 	private void afficher(){
 		TypeElementPlateau type = element.getType();
 		
		int x = element.getPosition().getX() + 2 * Constante.LARGEUR_CASE + Constante.DECALAGE_PLATEAU_X;
		int y = element.getPosition().getY();
		
		x = Math.min(Constante.DECALAGE_PLATEAU_X + Constante.LARGEUR_PLATEAU - LARGEUR_TOOLTIP - 2 * Constante.LARGEUR_CASE,x);
		y = Math.min(Constante.DECALAGE_PLATEAU_Y + Constante.HAUTEUR_PLATEAU - HAUTEUR_TOOLTIP - 2 * Constante.HAUTEUR_CASE,y);
		
		setBounds(x,y,LARGEUR_TOOLTIP,HAUTEUR_TOOLTIP);
 		
 		labelType.setText(Translator.translate(type.name()));
 		
 		progressBar.setMaximum(type.getPointDeVie(niveau));
 		progressBar.setValue(element.getPointsVie());
 		progressBar.setString(element.getPointsVie() + " / " + type.getPointDeVie(niveau));
 		
 		if (element instanceof Unite){
 			afficherUnite();
 		}else{
 			afficherBatiment();
 		}
 	}
 	
 	private void afficherUnite(){
 		labelInfo1.setText(Translator.translate("DeplacementRestant") + " : ");
 		labelInfo2.setText(((Unite)element).getDeplacementRestant() + "");
 	}
 	
 	private void afficherBatiment(){
 		TypeBatiment type = (TypeBatiment)element.getType();
 		labelInfo1.setText(Translator.translate("Revenu") + " : ");
 		labelInfo2.setText(Constante.formatArgent(type.getRevenu(niveau)));
 	}
 	
 	public ElementPlateau getElement(){
 		return element;
 	}
 	
 	public void setElement(ElementPlateau element,int niveau){
 		this.element = element;
 		this.niveau = niveau;
 		afficher();
 	}
 	
 }
