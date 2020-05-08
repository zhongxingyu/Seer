 package seismes;
 
import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
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
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 public class GUI {
     private JTextArea output;
     
     private JTextField latitude;
     private JTextField longitude;
     private JTextField distance;
     private JTextField date;
     private JTextField minimalMagnitude;
     
     private JRadioButton sortMagnitude;
     private JRadioButton sortDistance;
     private JRadioButton sortDate;
     
     /**
      * Table de hachage qui contient les validateurs pour les différents
      * champs d'entrée.  Un LinkedHashMap est utilisé pour conserver
      * l'ordre d'insertion.
      */
     private LinkedHashMap<JTextField, Validator> validators;
     
     
     public GUI() {
         validators = new LinkedHashMap<JTextField, Validator>();
     }
     
     
     public static void main(String[] args) {
         GUI gui = new GUI();
         gui.createGUI();
     }
     
     
     public LinkedHashMap<JTextField, Validator> getValidators() {
         return validators;
     }
 
     
     /**
      * Bâti l'interface graphique de l'application
      */
     private void createGUI() {
         final JFrame frame = new JFrame("Séismes bing bang boum boum!");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         // Côté gauche de l'interface
         JPanel leftPanel = new JPanel(new GridLayout(0, 2, 4, 4));
         
         // Ajouter les différents champs texte
         latitude = addTextField(leftPanel, "Latitude de référence");
         longitude = addTextField(leftPanel, "Longitude de référence");
         distance = addTextField(leftPanel, "Distance");
         date = addTextField(leftPanel, "Date de départ");
         minimalMagnitude = addTextField(leftPanel, "Magitude minimale");
         
         
         // Créer les validateurs
         validators.put(latitude, new RangeValidator(-90, 90));
         validators.put(longitude, new RangeValidator(-180, 180));
         validators.put(distance, new PositiveDoubleValidator());
         validators.put(date, new DateValidator());
         validators.put(minimalMagnitude, new PositiveDoubleValidator());
         
         // Créer les boutons radio de tri
         addRatioButtons(leftPanel);
         
         JButton searchButton = new JButton("Rechercher");
         searchButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 LinkedHashMap<JTextField, Validator> validators = getValidators();
                 StringBuilder errorMessage = new StringBuilder();
                 boolean hasErrors = false;
                 
                 for (JTextField tf: validators.keySet()) {
                     Validator v = validators.get(tf);
                     if (!v.isValid(tf.getText())) {
                         errorMessage.append(tf.getName() + ": " + v.getErrorMessage() + "\n");
                         hasErrors = true;
                     }
                 }
                 
                 if (hasErrors)
                     JOptionPane.showMessageDialog(frame, errorMessage, "Erreur", JOptionPane.ERROR_MESSAGE);
             }
         });
         leftPanel.add(searchButton);
         
         
         
         // Côté droit de l'interface
         output = new JTextArea();
         output.setEnabled(false);
         
         // Ajouter le côté gauche et droit à l'application.
         JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
         splitPane.setLeftComponent(leftPanel);
         splitPane.setRightComponent(new JScrollPane(output));
         
         frame.add(splitPane);
         frame.pack();
         frame.setSize(800, 600);
         frame.setVisible(true);
     }
 
 
     /**
      * Ajoute les boutons radio de tri
      * @param panel le panneau auquel ajouter les boutons radio
      */
     private void addRatioButtons(JPanel panel) {
         sortDate = new JRadioButton("Date", true);
         sortDistance = new JRadioButton("Distance");
         sortMagnitude = new JRadioButton("Magnitude");
         
         ButtonGroup bg = new ButtonGroup();
         bg.add(sortDate);
         bg.add(sortDistance);
         bg.add(sortMagnitude);
         
         JPanel radioPanel = new JPanel();
         radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
         radioPanel.add(sortDate);
         radioPanel.add(sortDistance);
         radioPanel.add(sortMagnitude);
         
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelPanel.add(new JLabel("Trier par"));
        panel.add(labelPanel);
         panel.add(radioPanel);
     }
     
     
     /**
      * Ajoute un JLabel et un JTextField à un panneau
      * @param panel le panneau d'ajout
      * @param labelText le text du JLabel
      * @return le champ texte qui a été créé
      */
     private JTextField addTextField(JPanel panel, String labelText) {
         JTextField textField = new JTextField(10);
         textField.setName(labelText);
         JLabel label = new JLabel(labelText);
         label.setLabelFor(textField);
        
        // Ces panneaux sont utilisés pour s'assurer que les champs
        // texte ont la hauteur "normale".
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel textFieldPanel = new JPanel();
        labelPanel.add(label);
        textFieldPanel.add(textField);

        panel.add(labelPanel);
        panel.add(textFieldPanel);
         
         return textField;
     }
     
 }
