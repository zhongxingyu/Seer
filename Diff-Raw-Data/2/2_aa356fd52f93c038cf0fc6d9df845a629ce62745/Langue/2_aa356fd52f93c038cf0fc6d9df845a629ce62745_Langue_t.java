 package general;
 
 import ihm.fenetres.FenetrePrincipale;
 
 import java.awt.Component;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import modele.Billeterie;
 
 public class Langue {	
 	
 	private static Locale locale;
 	private static ResourceBundle res;
 	
 	/**
 	 * Initialisation de la langue
 	 * @throws Exception
 	 */
 	public static void langueInit() {
 		try {
 			/* Recuperation de la langue dans le fichier de proprietes */
 			locale = new Locale(Proprietes.getOption("langage"), Proprietes.getOption("country"));
 			res = ResourceBundle.getBundle("Messages", locale);
 		} catch (Exception e) {
 			/* Langue par defaut */
 			locale = new Locale("en", "US");
 			res = ResourceBundle.getBundle("Messages", locale);
 			
 		}
 	}
 	
 	public static String getTraduction(String traduction){
 		String result = "";
 		try {
 			result = res.getString(traduction);
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(new JFrame(), "Error_unexpected" 
					+ "Error : traduction of " + traduction + "\n" + e.toString(), "Warning", JOptionPane.INFORMATION_MESSAGE);
 		}
 		return result;
 	}
 	
 	public static String aPropos() {
 		return Langue.getTraduction("software_versus") + Constantes.versionLogiciel + Langue.getTraduction("authors");
 	}
 
 	/**
 	 * Gestion du menu deroulant du choix des langues
 	 * @param fenetrePrincipale
 	 * @return
 	 */
 	public static Component choixLangueMenu(final FenetrePrincipale fenetrePrincipale) {
 		JComboBox<String> jcb = new JComboBox<String>();
 		jcb.addItem("Francais");
 		jcb.addItem("English");
 		jcb.addItemListener(new ItemListener(){
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getItem().equals("Francais")) {
 					locale = new Locale("fr", "FR");
 					res = ResourceBundle.getBundle("Messages", locale);
 				} else if (e.getItem().equals("English")) {
 					locale = new Locale("en", "US");
 					res = ResourceBundle.getBundle("Messages", locale);
 				}
 				
 				fenetrePrincipale.dispose();
 				FenetrePrincipale frame = new FenetrePrincipale(new Billeterie("database.sqlite"));
 				frame.setVisible(true);
 			}
 		});
 		return jcb;
 	}
 }
