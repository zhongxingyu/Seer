 package view.menu.subMenuPanels;
 
 
 import java.awt.GridLayout;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
import model.MainModel;
 import model.save.ScoreModel;
 import resources.Translator;
 import view.menu.MenuButton;
 
 /**
  * 
  * @author Vidar Eriksson
  *
  */
 @SuppressWarnings("serial")
 public class Score extends SubMenuPanel {
		
	public Score(MenuButton button, MainModel mainModel) {
		super(Translator.getMenuString("highScore"), getPanel(mainModel), button);
 	}
 	
	private static JPanel getPanel(MainModel mainModel){
 		JPanel p = new JPanel();
		ScoreModel h = mainModel.getScoreModel();
 		
 		p.setLayout(new GridLayout(0, 1, 0, resources.MenuLookAndFeel.getGap()));
 		
 		if (h.existsNoScore()){
 			JLabel l = new JLabel(Translator.getMenuString("noScoreExplain"));
 			l.setFont(resources.MenuLookAndFeel.getLargeFont());
 			p.add(l);
 		} else {
 			for (int a=0; a<h.getLenght(); a++){
 				String s = a+1 + ". "; 
 				if (h.getScore(a).length() < 1){
 					s+=Translator.getMenuString("noScore");
 				} else {
 					s+=h.getScore(a) + "p ";
 				}
 				
 				if (h.getName(a).length() < 1){
 					s+=Translator.getMenuString("noName");
 				} else {
 					s+=h.getName(a) + "";
 				}
 				
 				JLabel l = new JLabel(s);
 				l.setFont(resources.MenuLookAndFeel.getLargeFont());
 				p.add(l);
 			}
 		}
 
 		return p;
 	}
 
 }
