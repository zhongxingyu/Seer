 
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import test_calculatrice.Calcul;
 
 public class Interaction implements ActionListener {
 
 	private FenetreCalc calc; // référence à notre fenêtre contenant les boutons
 	private String text = ""; // Le texte saisi et affiché qui sera en argument de Calcul
 	private Double result = 0.0;
 	
 	public Interaction(FenetreCalc calc){
 		this.calc = calc;
 	} // fin constructeur
 	
 	@Override
 	public void actionPerformed(ActionEvent boutons) {
 
 		if (boutons.getSource() == calc.bouton0) {
 			text += "0";
 			calc.text.setText(text);
 		}
 		if (boutons.getSource() == calc.bouton1) {
 			text += "1";
 			calc.text.setText(text);
 		}
 		if (boutons.getSource() == calc.bouton2) {
 			text += "2";
 			calc.text.setText(text);
 		}
 		if (boutons.getSource() == calc.bouton3) {
 			text += "3";
 			calc.text.setText(text);
 		}
 		if (boutons.getSource() == calc.bouton4) {
 			text += "4";
 			calc.text.setText(text);
 		}
 		if (boutons.getSource() == calc.bouton5) {
 			text += "5";
 			calc.text.setText(text);
 		}
 		if (boutons.getSource() == calc.bouton6) {
 			text += "6";
 			calc.text.setText(text);
 		}
 		if (boutons.getSource() == calc.bouton7) {
 			text += "7";
 			calc.text.setText(text);
 		}
 		if (boutons.getSource() == calc.bouton8) {
 			text += "8";
 			calc.text.setText(text);
 		}
 		if (boutons.getSource() == calc.bouton9) {
 			text += "9";
 			calc.text.setText(text);
 		}
 		if (boutons.getSource() == calc.boutonPt) {
 			text += ".";
 			calc.text.setText(text);
 		}
 		
 		if (boutons.getSource() == calc.boutonAd) {
 			text += "+";
 			calc.text.setText(text);
 		}
 		if (boutons.getSource() == calc.boutonSo) {
 			text += "-";
 			calc.text.setText(text);
 		}
 		if (boutons.getSource() == calc.boutonMu) {
 			text += "*";
 			calc.text.setText(text);
 		}
 		if (boutons.getSource() == calc.boutonDi) {
 			text += "/";
 			calc.text.setText(text);
 		}
 		if (boutons.getSource() == calc.boutonEgal) {
 			text += "=";
 //			calc.text.setText(text);
 			Calcul calculette = new Calcul(text);
 			result = calculette.resultat();
			calc.text.setText(result.toString());
 		}
 		if (boutons.getSource() == calc.boutonReset) {
 			text = "";
 			calc.text.setText("0");
 		}
 
 	} // fin actionPerformed
 	
 	
 
 	
 
 } // fin classe Interaction
