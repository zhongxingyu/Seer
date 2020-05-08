 package mcmapconverter.gui;
 
 //TODO: make this not horrible
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Panel;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.Set;
 import javax.swing.*;
 import mcmapconverter.*;
 
 public class ConverterWindow extends JFrame implements ConverterUI {
     private final JLabel inputFileError,
                          outputFileError,
                          inputFormatError,
                          outputFormatError,
                          console;
             
     public ConverterWindow(final Converter converter) {
         final ConverterWindow top = this;
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setResizable(false);
 
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 System.exit(0);
             }
         });
 
         GridBagLayout layout = new GridBagLayout();
         JPanel inputPanel, outputPanel;
         final JTextField inputFileField, outputFileField;
 
         inputPanel = new JPanel(layout);
         outputPanel = new JPanel(layout);
 
         inputFileField = new JTextField(20);
         inputFileField.setEditable(false);
         outputFileField = new JTextField(20);
         outputFileField.setEditable(false);
 
         inputPanel.add(inputFileField);
         outputPanel.add(outputFileField);
 
         inputPanel.setBorder(
                 BorderFactory.createCompoundBorder(
                                 BorderFactory.createTitledBorder("Input Map File"),
                                 BorderFactory.createEmptyBorder(5,5,5,5)));
 
         outputPanel.setBorder(
                 BorderFactory.createCompoundBorder(
                                 BorderFactory.createTitledBorder("Output Map File"),
                                 BorderFactory.createEmptyBorder(5,5,5,5)));
 
         Panel displayPane = new Panel(layout);
         getContentPane().add(displayPane);
 
         JLabel windowDescription = new JLabel("Select an input and output file for conversion:");
 
         JLabel inputFormat = new JLabel("Input Format");
         JLabel outputFormat = new JLabel("Output Format");
 
         inputFileError = new JLabel();
         outputFileError = new JLabel();
         inputFormatError = new JLabel();
         outputFormatError = new JLabel();
         console = new JLabel();
 
         GridBagConstraints constraints = new GridBagConstraints();
 
         constraints.weightx = 0.5;
         constraints.gridx = 0;
         constraints.gridy = 0;
         displayPane.add(windowDescription, constraints);
 
         constraints.weightx = 0.5;
         constraints.gridx = 0;
         constraints.gridy = 1;
         displayPane.add(inputPanel, constraints);
 
         constraints.weightx = 0.5;
         constraints.gridx = 1;
         constraints.gridy = 1;
         displayPane.add(outputPanel, constraints);
 
         final JFileChooser minecraftFileChooser = new MinecraftMapFileChooser(converter.getMapFormats());
 
         final JButton inputButton, outputButton, convertButton;
 
         Set<String> formatSelectionsSet = converter.getMapFormatNames();
         String[] nformatSelections;
         nformatSelections = new String[formatSelectionsSet.size() + 1];
         nformatSelections[0] = "Automatic";
         int i = 1;
         for(String formatSelection : formatSelectionsSet) {
             nformatSelections[i++] = formatSelection;
         }
 
         final String[] formatSelections = nformatSelections;
         final JComboBox inputFormatSelector = new JComboBox(formatSelections);
         final JComboBox outputFormatSelector = new JComboBox(formatSelections);
 
         inputFormatSelector.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if(e.getSource() == inputFormatSelector) {
                     String format;
                     int selectedIndex;
 
                    resetMessages();
                     selectedIndex = inputFormatSelector.getSelectedIndex();
 
                     if(selectedIndex == 0) {
                         converter.setInputFormat(null);
                     } else {
                         format = formatSelections[selectedIndex];
                         converter.setInputFormat(converter.getMapFormat(format));
                     }
                 }
             }
         });
 
         outputFormatSelector.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if(e.getSource() == outputFormatSelector) {
                     String format;
                     int selectedIndex;
 
                    resetMessages();
                     selectedIndex = outputFormatSelector.getSelectedIndex();
                     
                     if(selectedIndex == 0) {
                         converter.setOutputFormat(null);
                     } else  {
                         format = formatSelections[selectedIndex];
                         converter.setOutputFormat(converter.getMapFormat(format));
                     }
                 }
             }
         });
 
         inputButton = new JButton("Choose...");
         inputButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if(e.getSource() == inputButton) {
                     int selectedOption;
 
                     selectedOption = minecraftFileChooser.showOpenDialog(top);
                     if(selectedOption == JFileChooser.APPROVE_OPTION) {
                         File file;
                         javax.swing.filechooser.FileFilter fileFilter;
 
                        resetMessages();
                         fileFilter = minecraftFileChooser.getFileFilter();
                         if(fileFilter instanceof MinecraftMapFormatFileFilter) {
                             MapFormat mapFormat;
                             String name;
 
                             mapFormat = ((MinecraftMapFormatFileFilter)fileFilter).getMapFormat();
                             converter.setOutputFormat(mapFormat);
 
                             name = mapFormat.getName();
                             for(int i = 1;i < formatSelections.length;i++) {
                                 String optionName = formatSelections[i];
                                 if(optionName.equals(name))
                                     inputFormatSelector.setSelectedIndex(i);
                             }
                         }
 
                         file = minecraftFileChooser.getSelectedFile();
                         converter.setInputFile(file);
                         inputFileField.setText(file.getAbsolutePath());
                     }
                 }
             }
         });
 
         outputButton = new JButton("Choose...");
         outputButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if(e.getSource() == outputButton) {
                     int selectedOption;
 
                     selectedOption = minecraftFileChooser.showSaveDialog(top);
                     if(selectedOption == JFileChooser.APPROVE_OPTION) {
                         File file;
                         javax.swing.filechooser.FileFilter fileFilter;
 
                        resetMessages();
                         fileFilter = minecraftFileChooser.getFileFilter();
                         if(fileFilter instanceof MinecraftMapFormatFileFilter) {
                             MapFormat mapFormat;
                             String name;
 
                             mapFormat = ((MinecraftMapFormatFileFilter)fileFilter).getMapFormat();
                             converter.setInputFormat(mapFormat);
 
                             name = mapFormat.getName();
                             for(int i = 1;i < formatSelections.length;i++) {
                                 String optionName = formatSelections[i];
                                 if(optionName.equals(name))
                                     inputFormatSelector.setSelectedIndex(i);
                                else
                                    System.out.println(optionName + " != " + name);
                             }
                         }
 
                         file = minecraftFileChooser.getSelectedFile();
                         converter.setOutputFile(file);
                         outputFileField.setText(file.getAbsolutePath());
                     }
                 }
             }
         });
 
         convertButton = new JButton("Convert");
         convertButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if(e.getSource() == convertButton) {
                    resetMessages();
                     converter.convert(top);
                 }
             }
         });
 
         constraints.weightx = 0.5;
         constraints.gridx = 0;
         constraints.gridy = 2;
         displayPane.add(convertButton, constraints);
 
         constraints.weightx = 0.5;
         constraints.gridx = 1;
         constraints.gridy = 2;
        displayPane.add(console, constraints);
 
         inputPanel.add(inputButton);
 
         constraints.weightx = 0.5;
         constraints.gridx = 0;
         constraints.gridy = 1;
         inputPanel.add(inputFileError, constraints);
 
         constraints.weightx = 0.5;
         constraints.gridx = 0;
         constraints.gridy = 2;
         inputPanel.add(inputFormat, constraints);
 
         constraints.weightx = 0.5;
         constraints.gridx = 1;
         constraints.gridy = 2;
         inputPanel.add(inputFormatSelector, constraints);
 
         constraints.weightx = 0.5;
         constraints.gridx = 0;
         constraints.gridy = 3;
         inputPanel.add(inputFormatError, constraints);
         
         outputPanel.add(outputButton);
 
         constraints.weightx = 0.5;
         constraints.gridx = 0;
         constraints.gridy = 1;
         outputPanel.add(outputFileError, constraints);
 
         constraints.weightx = 0.5;
         constraints.gridx = 0;
         constraints.gridy = 2;
         outputPanel.add(outputFormat, constraints);
 
         constraints.weightx = 0.5;
         constraints.gridx = 1;
         constraints.gridy = 2;
         outputPanel.add(outputFormatSelector, constraints);
 
         constraints.weightx = 0.5;
         constraints.gridx = 0;
         constraints.gridy = 3;
         outputPanel.add(outputFormatError, constraints);
         
         inputPanel.setVisible(true);
         outputPanel.setVisible(true);
         displayPane.setVisible(true);
         console.setVisible(true);
         pack();
         setLocationRelativeTo(null);
         setVisible(true);
     }
 
     public void displayInputFileError(String message) {
         inputFileError.setText(message);
         inputFileError.setForeground(Color.RED);
         refresh();
     }
 
     public void displayOutputFileError(String message) {
         outputFileError.setText(message);
         outputFileError.setForeground(Color.RED);
         refresh();
     }
 
     public void displayInputFormatError(String message) {
         inputFormatError.setText(message);
         inputFormatError.setForeground(Color.RED);
         refresh();
     }
     public void displayOutputFormatError(String message) {
         outputFormatError.setText(message);
         outputFormatError.setForeground(Color.RED);
         refresh();
     }
     public void displayError(String message) {
         console.setText(message);
         console.setForeground(Color.RED);
         refresh();
     }
 
     public void displaySuccess() {
         console.setText("Map converted");
         console.setForeground(Color.GREEN);
         refresh();
     }
 
    private void resetMessages() {
        inputFileError.setText("");
        outputFileError.setText("");
        inputFormatError.setText("");
        outputFormatError.setText("");
        console.setText("");
        refresh();
    }

     private void refresh() {
         pack();
         setVisible(true);
     }
 }
