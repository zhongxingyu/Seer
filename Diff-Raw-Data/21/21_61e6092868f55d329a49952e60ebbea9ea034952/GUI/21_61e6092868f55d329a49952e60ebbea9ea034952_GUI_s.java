 package seismes;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Date;
 import java.util.LinkedHashMap;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 /**
  * Classe principale de l'application
  * @author Eric Thivierge, Vincent Foley
  *
  */
 public class GUI {
 	private String filename;
     private JTable output;
     private SeismeTableModel tableModel;
     private final JFrame frame = new JFrame("Séismes bing bang boum boum!");
     
     private JTextField latitude;
     private JTextField longitude;
     private JTextField distance;
     private JTextField date;
     private JTextField minimalMagnitude;
     
     private JRadioButton sortMagnitude;
     private JRadioButton sortDistance;
     private JRadioButton sortDate;
     private String sortType = "Date";
     
     private ActionListener rbAL;
     private ActionListener sbAL;
     
     /**
      * Table de hachage qui contient les validateurs pour les différents
      * champs d'entrée.  Un LinkedHashMap est utilisé pour conserver
      * l'ordre d'insertion.
      */
     private LinkedHashMap<JTextField, Validator> validators;
     
     
     private void setSortType(String sortType) {
     	this.sortType = sortType;
     }
     
     private String getSortType() {
     	return this.sortType;
     }
     
     @SuppressWarnings("deprecation")
 	private Date getDate() {
     	return new Date(date.getText());
     }
     
     private double getLatitude() {
     	return Double.parseDouble(latitude.getText());
     }
     
     private double getLongitude() {
     	return Double.parseDouble(longitude.getText());
     }
     
     private double getDistance() {
     	return Double.parseDouble(distance.getText());
     }
     
     private double getMagnitude() {
     	return Double.parseDouble(minimalMagnitude.getText());
     }
     
     public GUI(String filename) {
     	this.filename = filename;
         validators = new LinkedHashMap<JTextField, Validator>();
         rbAL = new RBActionListener();
         sbAL = new SBActionListener();
     }
     
     
     public LinkedHashMap<JTextField, Validator> getValidators() {
         return validators;
     }
 
     
     /**
      * Bâti l'interface graphique de l'application
      */
     private void createGUI() {
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         // Côté gauche de l'interface
         JPanel leftPanel = new JPanel(new BorderLayout());
         JPanel paramsPanel = new JPanel();	
         paramsPanel.setLayout(new BoxLayout(paramsPanel, BoxLayout.Y_AXIS));
         paramsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Paramètres de recherche"));
         
         latitude = addTextField(paramsPanel, "Latitude de référence");
         longitude = addTextField(paramsPanel, "Longitude de référence");
         distance = addTextField(paramsPanel, "Distance");
         date = addTextField(paramsPanel, "Date de départ");
         minimalMagnitude = addTextField(paramsPanel, "Magitude minimale");
         
         // Créer les validateurs
         validators.put(latitude, new RangeValidator(-90, 90));
         validators.put(longitude, new RangeValidator(-180, 180));
         validators.put(distance, new PositiveDoubleValidator());
         validators.put(date, new DateValidator());
         validators.put(minimalMagnitude, new PositiveDoubleValidator());
         
         for (JTextField tf: validators.keySet()) {
             tf.setToolTipText(validators.get(tf).getErrorMessage());
         }
         
         // Créer les boutons radio de tri
         addRadioButtons(paramsPanel);
         
         JButton searchButton = new JButton("Rechercher");
         leftPanel.add(searchButton);
         
         // Côté droit de l'interface
         searchButton.addActionListener(sbAL);
         leftPanel.add(paramsPanel, BorderLayout.CENTER);
         leftPanel.add(searchButton, BorderLayout.SOUTH);
         
         // Côté droit de l'interface
         tableModel = new SeismeTableModel();
         output = new JTable(tableModel);
         
         // Ajouter le côté gauche et droit à l'application.
         JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
         splitPane.setLeftComponent(leftPanel);
         splitPane.setRightComponent(new JScrollPane(output));
 
         
         frame.add(splitPane);
         frame.pack();
         frame.setSize(900, 640);
         frame.setVisible(true);
     }
     
     /**
      * Ajoute les boutons radio de tri
      * @param panel le panneau auquel ajouter les boutons radio
      */
     private void addRadioButtons(JPanel panel) {
         sortDate = new JRadioButton("Date", true);
         sortDistance = new JRadioButton("Distance");
         sortMagnitude = new JRadioButton("Magnitude");
         sortDate.addActionListener(rbAL);
         sortDistance.addActionListener(rbAL);
         sortMagnitude.addActionListener(rbAL);
         
         ButtonGroup bg = new ButtonGroup();
         bg.add(sortDate);
         bg.add(sortDistance);
         bg.add(sortMagnitude);
         
         JPanel radioPanel = new JPanel();
         radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
         radioPanel.setBorder(new TitledBorder(new EtchedBorder(), "Trier par"));
         radioPanel.add(sortDate);
         radioPanel.add(sortDistance);
         radioPanel.add(sortMagnitude);
         panel.add(radioPanel, BorderLayout.NORTH);
     }
     
     /**
      * Ajoute un JLabel et un JTextField à un panneau
      * @param panel le panneau d'ajout
      * @param labelText le text du JLabel
      * @return le champ texte qui a été créé
      */
     private JTextField addTextField(JPanel panel, String labelText) {
     	JPanel newPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
     	newPanel.add(new JLabel(labelText));
     	JTextField textField = new JTextField(10);
     	newPanel.add(textField);
     	panel.add(newPanel);
     	
     	return textField;
     }
     
     
     private class RBActionListener implements ActionListener {
     	@Override
         public void actionPerformed(ActionEvent e) {
     		setSortType(e.getActionCommand());
         }
     }
     
     private class SBActionListener implements ActionListener {
     	@Override
     	public void actionPerformed(ActionEvent e) {
     		LinkedHashMap<JTextField, Validator> validators = getValidators();
     		StringBuilder errorMessage = new StringBuilder();
     		boolean hasErrors = false;
     		Seisme[] res;
     		String sortType;
 
     		for (JTextField tf: validators.keySet()) {
     			Validator v = validators.get(tf);
     			if (!v.isValid(tf.getText())) {
     				errorMessage.append(tf.getName() + ": " + v.getErrorMessage() + "\n");
     				hasErrors = true;
     			}
     		}
 
     		if (hasErrors)
     			JOptionPane.showMessageDialog(frame, errorMessage, "Erreur", JOptionPane.ERROR_MESSAGE);
     		else {
     			res = SeismeUtils.filterSeismes(getDate(), getLatitude(), getLongitude(),
     					getDistance(), getMagnitude(), filename);
     			sortType = getSortType();
     			if (sortType.equals("Date"))
     				SeismeUtils.sortByDate(res);
     			else if (sortType.equals("Distance")) {
     			    Coord point = new Coord(getLatitude(), getLongitude());
     			    SeismeUtils.sortByDistance(res, point);
     			}
     			else
     				SeismeUtils.sortByMagnitude(res);
     			
     			tableModel.setSeismes(res);
     		}
     	}
     }
     
     
     public static void main(String[] args) {
         if (args.length == 0) {
             System.err.println("Utilisation: java seismes.GUI <fichier csv>");
             System.exit(1);
         }
         GUI gui = new GUI(args[0]);
         gui.createGUI();
     }
 }
