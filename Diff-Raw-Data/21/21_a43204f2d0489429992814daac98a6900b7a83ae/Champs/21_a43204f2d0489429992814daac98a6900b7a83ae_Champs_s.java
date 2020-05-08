 package ihm.barresOutils;
 
 import general.Constantes;
 import modele.Attribut;
 
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.text.NumberFormat;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 //TODO Verifier que les champs de text ne contiennent pas de caracteres etranges, "'" par ex
 public class Champs extends JPanel{
 	private static final long serialVersionUID = 1L;
 	
 	private Map<String, JComponent> listeChamps = new HashMap<String, JComponent>();
 	private List<Attribut> listeAttributs;
 	private GridBagConstraints gridBagConstraints;
 	private int nb = 0;
 	
 	
 	/********** Constructeur ************/
 	/**
 	 * Constructeur d'un champs
 	 * @param listeAttributs
 	 */
 	public Champs(List<Attribut> listeAttributs) {
 		this.listeAttributs = listeAttributs;
 		
 		this.setLayout(new GridBagLayout());
 		gridBagConstraints = new GridBagConstraints();
 		
 		for (int i = 0; i < listeAttributs.size(); i++) {
 			//Ajout de l'etiquette
 			gridBagConstraints.gridx = 0;
 			gridBagConstraints.gridy = nb;
 			gridBagConstraints.anchor = GridBagConstraints.LINE_END;
 			JLabel etiquette = new JLabel(listeAttributs.get(i).getNomInterface() + " : ");
 			add(etiquette,gridBagConstraints);
 			
 			//Ajout du champ
 			gridBagConstraints.gridx = 1;
 			JComponent champ = nouveauChamp(listeAttributs.get(i).getType());
 			listeChamps.put(listeAttributs.get(i).getNomBDD(), champ);
 			add(champ,gridBagConstraints);
 			
 			nb++;
 		}
 	}
 	
 	
 	/********** Methodes ************/
 	/**
 	 * Permet d'affecter des valeurs au champs à remplir
 	 * @param map
 	 */
 	public void setValeurs(Map<String, Object> map) {
 		Set<String> set = map.keySet();
 		Iterator<String> it = set.iterator();
 		while(it.hasNext()) {
 			String nom = it.next();
 			JComponent compo = listeChamps.get(nom);
 			if(compo instanceof JFormattedTextField) {
 				((JFormattedTextField) compo).setValue(map.get(nom));
 			} else if (compo instanceof JCheckBox) {
 				((JCheckBox)compo).setSelected((Boolean) map.get(nom));
 			}
 		}
 	}
 	
 	/**
 	 * @return les donnees saisi dans les champs
 	 * @throws Exception 
 	 */
 	public Map<String, Object> getDonnees() throws Exception {
 		Map<String, Object> resultat = new HashMap<String, Object>();
 		for (int i = 0; i < listeAttributs.size(); i++) {
 			String nom = listeAttributs.get(i).getNomBDD();
 			resultat.put(nom, this.getValeur(i, nom));
 		}
 		return resultat;
 	}
 	
 	private JComponent nouveauChamp(int type){
 		JComponent champ;
 		switch (type) {
 		case Constantes.INTEGER : champ = new JFormattedTextField(NumberFormat.getIntegerInstance()); 
 								((JFormattedTextField) champ).setColumns(20);break;
 		case Constantes.FLOAT : champ = new JFormattedTextField(NumberFormat.getNumberInstance()); 
 								((JFormattedTextField) champ).setColumns(20);break;
 		case Constantes.STRING : champ = new JFormattedTextField(); 
 								((JFormattedTextField) champ).setColumns(20);break;
 		case Constantes.BOOLEAN : champ = new JCheckBox(); break;
 		default : champ = null;
 		}
 
 		return champ;
 	}
 	
 	private Object getValeur(int index, String nom) throws Exception {
 		Object res;
 		JComponent champ = listeChamps.get(nom);
 		int type = listeAttributs.get(index).getType();
 		try {
 		switch (type) {
 			case Constantes.INTEGER : res = Integer.parseInt(((JTextField)champ).getText()); break;
 			case Constantes.FLOAT : res = strToFloat(((JTextField)champ).getText()); break;
 			case Constantes.STRING : res = ((JTextField)champ).getText(); break;
 			case Constantes.BOOLEAN : res = ((JCheckBox)champ).isSelected(); break;
 			default : res = null;
 			}
 		} catch (Exception e) {
 			throw new Exception("Les champs ne sont pas remplis");
 		}
 		if(res instanceof String && res.equals("")) {
 			throw new Exception("Les champs ne sont pas remplis");
 		}
 		return res;
 	}
 	
 	private static float strToFloat(String s){
      	s = s.replace(',', '.');
      	return Float.parseFloat(s);
     }
 }
