 import com.google.gson.*;
 import org.apache.commons.math3.random.RandomDataGenerator;
 import org.apache.commons.math3.util.ArithmeticUtils;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.text.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dextor
  * Date: 7/30/13
  * Time: 9:49 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GenerateCSPPanel extends JPanel {
 
     private final JButton loadCSPButton;
     private MainWindow parent;
 
     private JTextField nVariablesField;
     private JTextField maxArityField;
     private JTextField domainSizeField;
 
     private JSlider densitySlider;
     private JSlider tightnessSlider;
 
     private JLabel nVariablesLabel;
     private JLabel maxArityLabel;
     private JLabel domainSizeLabel;
     private JLabel densityLabel;
     private JLabel tightnessLabel;
 
     private JPanel nVariablesPanel;
     private JPanel maxArityPanel;
     private JPanel domainSizePanel;
     private JPanel densityPanel;
     private JPanel tightnessPanel;
 
     private JPanel leftContainer;
     private JPanel rightContainer;
 
     private final RandomDataGenerator random;
 
     private String lastGeneratedCSP;
 
     private JButton generateCSPButton;
     private JButton saveGeneratedCSPButton;
     private JButton solveCSPButton;
 
     public GenerateCSPPanel(final MainWindow parent) {
         this.parent = parent;
 
         setLayout(new BorderLayout());
 
         nVariablesField = new JTextField(3);
         ((AbstractDocument) nVariablesField.getDocument()).setDocumentFilter(new NumericDocumentFilter(3));
         nVariablesField.setHorizontalAlignment(JTextField.RIGHT);
         nVariablesLabel = new JLabel("Insert the number of variables");
 
         maxArityField = new JTextField(3);
         ((AbstractDocument) maxArityField.getDocument()).setDocumentFilter(new NumericDocumentFilter(1));
         maxArityField.setHorizontalAlignment(JTextField.RIGHT);
         maxArityLabel = new JLabel("<html>Insert the maximum arity for each constraint</html>");
 
         domainSizeField = new JTextField(3);
         ((AbstractDocument) domainSizeField.getDocument()).setDocumentFilter(new NumericDocumentFilter(3));
         domainSizeField.setHorizontalAlignment(JTextField.RIGHT);
        domainSizeLabel = new JLabel("Insert the domain size of the variables");
 
         Hashtable labelTable = new Hashtable();
         labelTable.put( new Integer(0), new JLabel("0.00") );
         labelTable.put( new Integer(50), new JLabel("0.50") );
         labelTable.put( new Integer(100), new JLabel("1.00") );
 
         densitySlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
         densitySlider.setLabelTable(labelTable);
         densitySlider.setPaintTicks(true);
         densitySlider.setPaintLabels(true);
         String densityText = "Insert the density of the constraints' graph: ";
         final int densityTextLength = densityText.length();
         densityText = densityText + "0.00";
         densityLabel = new JLabel(densityText);
         densitySlider.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent changeEvent) {
                 JSlider slider = (JSlider) changeEvent.getSource();
                 if (!slider.getValueIsAdjusting()) {
                     float f = (float) slider.getValue() / 100;
                     densityLabel.setText(densityLabel.getText().substring(0, densityTextLength - 1) + String.format("%.2f", f));
                 }
             }
         });
 
 
         tightnessSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
         tightnessSlider.setLabelTable(labelTable);
         tightnessSlider.setPaintTicks(true);
         tightnessSlider.setPaintLabels(true);
         String tightnessText = "Insert the tightness of the constraints' graph: ";
         final int tightnessTextLength = tightnessText.length();
         tightnessText = tightnessText + "0.00";
         tightnessLabel = new JLabel(tightnessText);
         tightnessSlider.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent changeEvent) {
                 JSlider slider = (JSlider) changeEvent.getSource();
                 if (!slider.getValueIsAdjusting()) {
                     float f = (float) slider.getValue() / 100;
                     tightnessLabel.setText(tightnessLabel.getText().substring(0, tightnessTextLength - 1) + String.format("%.2f", f));
                 }
             }
         });
 
         nVariablesPanel = new JPanel(new BorderLayout());
         nVariablesPanel.setBorder(BorderFactory.createTitledBorder("Variables number"));
         nVariablesPanel.add(nVariablesLabel, BorderLayout.WEST);
         nVariablesPanel.add(nVariablesField, BorderLayout.EAST);
 
         maxArityPanel = new JPanel();
         BorderLayout maxArityLayout = new BorderLayout();
         maxArityLayout.setHgap(10);
         maxArityPanel.setLayout(maxArityLayout);
         maxArityPanel.setBorder(BorderFactory.createTitledBorder("Maximum arity"));
         maxArityPanel.add(maxArityLabel, BorderLayout.WEST);
         maxArityPanel.add(maxArityField, BorderLayout.EAST);
 
         domainSizePanel = new JPanel(new BorderLayout());
         domainSizePanel.setBorder(BorderFactory.createTitledBorder("Domain size"));
         domainSizePanel.add(domainSizeLabel, BorderLayout.WEST);
         domainSizePanel.add(domainSizeField, BorderLayout.EAST);
 
         densityPanel = new JPanel();
         BorderLayout densityLayout = new BorderLayout();
         maxArityLayout.setHgap(10);
         densityPanel.setLayout(densityLayout);
         densityPanel.setBorder(BorderFactory.createTitledBorder("Density"));
         densityPanel.add(densityLabel, BorderLayout.WEST);
         densityPanel.add(densitySlider, BorderLayout.EAST);
 
         tightnessPanel = new JPanel(new BorderLayout());
         tightnessPanel.setBorder(BorderFactory.createTitledBorder("Tightness"));
         tightnessPanel.add(tightnessLabel, BorderLayout.WEST);
         tightnessPanel.add(tightnessSlider, BorderLayout.EAST);
 
         leftContainer = new JPanel();
         leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.Y_AXIS));
         leftContainer.add(nVariablesPanel);
         leftContainer.add(maxArityPanel);
         leftContainer.add(domainSizePanel);
 
         rightContainer = new JPanel();
         rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.Y_AXIS));
         rightContainer.add(densityPanel);
         rightContainer.add(tightnessPanel);
 
         generateCSPButton = new JButton("Generate CSP");
         generateCSPButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 if (nVariablesField.getText().equals("") || maxArityField.getText().equals("") || domainSizeField.getText().equals(""))
                     JOptionPane.showMessageDialog(GenerateCSPPanel.this, "Please, fill in all the fields!");
                 else if (Integer.valueOf(nVariablesField.getText()).intValue() < Integer.valueOf(maxArityField.getText()).intValue())
                     JOptionPane.showMessageDialog(GenerateCSPPanel.this, "Max arity cannot be greater than the number of variables");
                 else {
                     lastGeneratedCSP = generateCSP();
                     parent.showCSP(lastGeneratedCSP);
                 }
             }
         });
 
         loadCSPButton = new JButton("Load CSP from file");
         loadCSPButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
                 if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                     try {
                         File f = chooser.getSelectedFile();
                         BufferedReader reader = new BufferedReader(new FileReader(f));
 
                         String line;
                         StringBuffer content = new StringBuffer();
 
                         while ((line = reader.readLine()) != null) {
                             content.append(line);
                         }
 
                         reader.close();
 
                         lastGeneratedCSP = content.toString();
 
                         resetFields();
                         parent.showCSP(lastGeneratedCSP);
 
                     } catch (FileNotFoundException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     } catch (IOException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                 }
             }
         });
 
         saveGeneratedCSPButton = new JButton("Save CSP to file");
         saveGeneratedCSPButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
                 if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                     try {
                         File file = chooser.getSelectedFile();
                         if (!file.exists()) {
                             file.createNewFile();
                         }
 
                         FileWriter writer = new FileWriter(file);
 
                         Gson gson = new GsonBuilder().setPrettyPrinting().create();
                         JsonElement root = new JsonParser().parse(lastGeneratedCSP);
 
                         writer.write(gson.toJson(root));
                         writer.close();
 
                         JOptionPane.showMessageDialog(GenerateCSPPanel.this, "CSP successfully exported to file.", "CSP exportation", JOptionPane.INFORMATION_MESSAGE);
 
                     } catch (IOException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                 }
             }
         });
 
         solveCSPButton = new JButton("Solve CSP");
         solveCSPButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 if (lastGeneratedCSP != null && !lastGeneratedCSP.isEmpty())
                     parent.solve(lastGeneratedCSP);
                 else
                     JOptionPane.showMessageDialog(GenerateCSPPanel.this, "Generate a random CSP first!");
             }
         });
 
         add(leftContainer, BorderLayout.WEST);
         add(rightContainer, BorderLayout.EAST);
 
         JPanel leftButtonsContainer = new JPanel();
         leftButtonsContainer.setBorder(BorderFactory.createTitledBorder("Generation"));
         leftButtonsContainer.add(generateCSPButton);
 
         JPanel centerButtonsContainer = new JPanel();
         centerButtonsContainer.setBorder(BorderFactory.createTitledBorder("Save/Load CSP"));
         centerButtonsContainer.add(loadCSPButton);
         centerButtonsContainer.add(saveGeneratedCSPButton);
 
         JPanel rightButtonsContainer = new JPanel();
         rightButtonsContainer.setBorder(BorderFactory.createTitledBorder("Solution"));
         rightButtonsContainer.add(solveCSPButton);
 
         JPanel buttonsContainer = new JPanel();
         buttonsContainer.setLayout(new FlowLayout());
 
         buttonsContainer.add(leftButtonsContainer);
         buttonsContainer.add(centerButtonsContainer);
         buttonsContainer.add(rightButtonsContainer);
 
         add(buttonsContainer, BorderLayout.SOUTH);
 
         nVariablesField.setText("5");
         maxArityField.setText("3");
         domainSizeField.setText("10");
 
         // initializing random generator
         random = new RandomDataGenerator();
     }
 
     private void resetFields() {
         nVariablesField.setText("");
         maxArityField.setText("");
         domainSizeField.setText("");
 
         densitySlider.setValue(50);
         tightnessSlider.setValue(50);
     }
 
     public String generateCSP() {
         int numberOfVariables = Integer.valueOf(nVariablesField.getText()).intValue();
         int maxArity = Integer.valueOf(maxArityField.getText()).intValue();
         int domainSize = Integer.valueOf(domainSizeField.getText()).intValue();
         float density = (float) densitySlider.getValue() / 100;
 
         int numberOfConstraints = (int) (density * computeBinomialCoefficient(numberOfVariables, maxArity)); //TODO troncato
 
         Constraint[] constraints = new Constraint[numberOfConstraints];
 
         for (int i=0; i<numberOfConstraints; i++) {
             int currentVarsNumber = random.nextInt(1, maxArity); // k>=2 //TODO controllare che maxArity <= nVariables
 
             Vector<Integer> variables = new Vector<Integer>();
             for (int j=0; j<numberOfVariables; j++) {
                 variables.add(j);
             }
 
             ArrayList<Integer> zeroedVariables = new ArrayList<Integer>();
             for (int j=0; j<(numberOfVariables - currentVarsNumber); j++) {
 
                 // calculating random index in vector of variables
                 int randomIndex = random.nextInt(0, variables.size() - 1);
 
                 // adding the variable that has to be set to 0 value
                 zeroedVariables.add(variables.get(randomIndex));
 
                 // removing the zeroed variable from the original vector
                 variables.remove(randomIndex);
             }
 
             int[] coeffs = new int[numberOfVariables];
             for (int j=0; j<zeroedVariables.size(); j++) {
                 coeffs[zeroedVariables.get(j)] = 0;
             }
 
             for (int j=0; j<variables.size(); j++) {
                 coeffs[variables.get(j)] = random.nextInt(1, domainSize);
             }
 
             int cterm = random.nextInt(0, domainSize);
 
             Constraint c = new Constraint();
             c.coeffs = coeffs;
             c.cterm = cterm;
 
             constraints[i] = c;
         }
 
         JsonArray linearizedCoeffs = new JsonArray();
         JsonArray linearizedCterms = new JsonArray();
         for (int i=0; i<constraints.length; i++) {
             Constraint currentConstraint = constraints[i];
             for (int j=0; j<currentConstraint.coeffs.length; j++) {
                 linearizedCoeffs.add(new JsonPrimitive(currentConstraint.coeffs[j]));
             }
 
             linearizedCterms.add(new JsonPrimitive(currentConstraint.cterm));
         }
 
         JsonArray domains = new JsonArray();
         for (int i=0; i<numberOfVariables; i++) {
             JsonArray jsonedDomain = new JsonArray();
             jsonedDomain.add(new JsonPrimitive(1));
             jsonedDomain.add(new JsonPrimitive(domainSize));
             domains.add(jsonedDomain);
         }
 
         JsonObject root = new JsonObject();
         root.add("vnum", new JsonPrimitive(numberOfVariables));
         root.add("cnum", new JsonPrimitive(numberOfConstraints));
         root.add("coeffs", linearizedCoeffs);
         root.add("cterms", linearizedCterms);
         root.add("domain", domains);
 
         Gson gson = new GsonBuilder().create();
 
         String output = gson.toJson(root);
 
         return output;
     }
 
     private long computeBinomialCoefficient(int n, int k) {
         return ArithmeticUtils.binomialCoefficient(n, k);
     }
 
     /**
      * Private class that models a constraint.
      */
     private static class Constraint {
         public int[] coeffs;
         public int cterm;
     }
 
     /**
      * Private class that allows insertion of numeric characters only in a JTextField.
      */
     private static class NumericDocumentFilter extends DocumentFilter {
 
         private int maxTextLength;
 
         public NumericDocumentFilter(int maxTextLength) {
             this.maxTextLength = maxTextLength;
         }
 
         private boolean verifyText(String text) {
             return text.length() <= maxTextLength;
         }
 
         @Override
         public void insertString(FilterBypass fb, int off, String str, AttributeSet attr) throws BadLocationException {
             Document doc = fb.getDocument();
             String oldText = doc.getText(0, doc.getLength());
             StringBuilder sb = new StringBuilder(oldText);
             sb.insert(off, str);
 
             if (verifyText(sb.toString())) {
                 // remove non-digits
                 fb.insertString(off, str.replaceAll("\\D++", ""), attr);
             }
         }
 
         @Override
         public void replace(FilterBypass fb, int off, int len, String str, AttributeSet attr) throws BadLocationException {
             Document doc = fb.getDocument();
             String oldText = doc.getText(0, doc.getLength());
             StringBuilder sb = new StringBuilder(oldText);
             sb.insert(off, str);
 
             if (verifyText(sb.toString())) {
                 // remove non-digits
                 fb.replace(off, len, str.replaceAll("\\D++", ""), attr);
             }
         }
 
         @Override
         public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
             Document doc = fb.getDocument();
             String oldText = doc.getText(0, doc.getLength());
             StringBuilder sb = new StringBuilder(oldText);
 
             sb.replace(offset, offset + length, "");
 
             if (verifyText(sb.toString())) {
                 super.remove(fb, offset, length);
             }
         }
     }
 
 }
