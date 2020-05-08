 /*******************************************************************************
  * Copyright (c) 2012 BragiSoft, Inc.
  * This source is subject to the BragiSoft Permissive License.
  * Please see the License.txt file for more information.
  * All other rights reserved.
  * 
  * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY 
  * KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
  * PARTICULAR PURPOSE.
  * 
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  *
  * Contributors:
 * Dominik Kuenne - First 'working' very basic prototype
  * Martin Kiessling - Everything else
  * 
  *******************************************************************************/
 
 package com.bragi.sonify;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.EnumSet;
 
 import javax.sound.midi.InvalidMidiDataException;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 import javax.swing.filechooser.FileFilter;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 /**
  * This class is the GUI of the sonificator.
  * 
  * @author Dominik Kuenne
  * @author Martin Kiessling
  */
 public class GUI extends JFrame implements ActionListener {
 
 	private static final long serialVersionUID = 1L;
 
 	/* components */
 	private JPanel contentPane;
 	private JTextField inputField;
 	private JTextField outputField;
 	private JComboBox<String> genreChooser;
 	private JButton inputButton;
 	private JButton outputButton;
 	private JButton startSonificationButton;
 	private JPanel corporatePane;
 	private ImageIcon corporateLogo;
 	private JLabel corporateName;
 	
 	private static final String ICON_PATH = "etc/img/OmniSenseAuge.png";
 
 	/* variables */
 	private final EnumSet<Genre> genres = EnumSet.allOf(Genre.class);
 	private final Image omnisenseIcon = new ImageIcon(ICON_PATH).getImage().getScaledInstance(150, 75, java.awt.Image.SCALE_SMOOTH);
 	private File inputFile;
 	private File outputFile;
 	private String selectedGenre;
 
 	/**
 	 * constructor of GUI
 	 */
 	public GUI(String title) {
 		super(title);
 		initComponents();
 		setVisible(true);
 	}
 
 	/**
 	 * This function initializes the GUI components
 	 */
 	private void initComponents() {
 		String[] genreStrings = new String[ genres.size()];
 		int i = 0;
 		for( Genre g : genres) {
 			genreStrings[i++] = g.name;
 		}
 		
 		// initialize selected genre
 		selectedGenre = genreStrings[0];
 		
 		// initialize corporatePane
 		corporatePane = new JPanel();
 		corporatePane.setBorder(new EmptyBorder(10, 10, 10, 10));
 		corporatePane.setLayout(new GridLayout(1, 2, 5, 5));
 
 		// initialize contentPane
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
 		contentPane.setLayout(new GridLayout(3, 2, 5, 5));
 
 		// frame
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(new BorderLayout());
 		add(corporatePane, BorderLayout.NORTH);
 		add(contentPane, BorderLayout.SOUTH);
 		setResizable(false);
 
 		// initialize components
 		inputField = new JTextField();
 		inputField.setEditable(false);
 		outputField = new JTextField();
 		outputField.setEditable(false);
 		inputButton = new JButton("Eingabedatei...");
 		outputButton = new JButton("Ausgabedatei...");
 		startSonificationButton = new JButton("Audifikation starten");
 		genreChooser = new JComboBox<String>(genreStrings);
 		corporateLogo = new ImageIcon(omnisenseIcon);
 		corporateName = new JLabel(	"<html>OMNI <FONT COLOR=#009933>Sense</FONT></html>", JLabel.CENTER);
 		corporateName.setFont(new Font("Microsoft Tai Le", Font.BOLD, 20));
 
 		// add components to contentPane
 		corporatePane.add(corporateName);
 		corporatePane.add(new JLabel(corporateLogo));
 		contentPane.add(inputField);
 		contentPane.add(inputButton);
 		contentPane.add(outputField);
 		contentPane.add(outputButton);
 		contentPane.add(genreChooser);
 		contentPane.add(startSonificationButton);
 
 		pack();
 
 		// add actionListeners to buttons
 		inputButton.addActionListener(this);
 		outputButton.addActionListener(this);
 		startSonificationButton.addActionListener(this);
 		genreChooser.addActionListener(this);
 		
 	}
 
 	/**
 	 * Overrides the actionPerformed-method and determines which button was
 	 * pressed to start the right action.
 	 */
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		Object src = event.getSource();
 		if (src == inputButton) {
 			inputFile = new FileChooser().inputFile();
 			if (inputFile != null) {
 				inputField.setText(inputFile.getAbsolutePath());
 			} else {
 				inputField.setText(null);
 			}
 		} else if (src == outputButton) {
 			outputFile = new FileChooser().outputFile();
 			if (outputFile != null) {
 				outputField.setText(outputFile.getAbsolutePath());
 			} else {
 				outputField.setText(null);
 			}
 		} else if ( src == genreChooser) {
 			// Returns an array of Objects containing one element -- the selected item
 			Object selected[] = genreChooser.getSelectedObjects();
 			if( selected.length == 0 ) {
 				selectedGenre = null;
 			} else {
 				selectedGenre = (String) selected[0];
 			}
 		} else if (src == startSonificationButton) {
 			if( inputFile != null && outputFile != null && selectedGenre != null ) {
 				try {
 					if( outputFile.exists()) {
 						Object[] options = {"Ja", "Nein"};
 						int selected = JOptionPane.showOptionDialog(null, "Die Datei mit dem Namen " + outputFile.getAbsolutePath()
 								+ "existiert bereits. Wollen Sie diese wirklich überschreiben?", "Ausgabedatei existiert bereits!", JOptionPane.DEFAULT_OPTION,
 								JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
 						if( selected == 1) {
 							return;
 						}
 					}
 					Sonificator.sonificate( Genre.getByName(selectedGenre), inputFile, outputFile);
 					JOptionPane.showMessageDialog(null, "Sonifizierung wurde erfolgreich abgeschlossen!", "Erfolg", JOptionPane.INFORMATION_MESSAGE);
 				}catch (IOException e) {					
 					JOptionPane.showMessageDialog(null, e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
 				} catch (InvalidMidiDataException e) {
 					JOptionPane.showMessageDialog(null, "Die Dateien zum Generieren der Musik sind korrupt. Wenden Sie sich an den IT-Support unter der Nummer 867-5309", "Installation korrupt!", JOptionPane.ERROR_MESSAGE);
 				} catch(NotImplementedException e) {
 					JOptionPane.showMessageDialog(null, "Der Algorithmus zum Generieren von " + selectedGenre
 							+ " ist noch nicht implementiert! Bitte wählen Sie eine anderes Genre aus.", "Fehler", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		} 
 	}
 
 	/**
 	 * The main-method creates a new Instance of GUI.
 	 */
 	public static void main(String[] args) {
 		new GUI("OMNI Sense - Audifizierung");
 	}
 
 	/**
 	 * This class is a simple FileChooser.
 	 * 
 	 * @param file is either the input or the output file
 	 * @param FilenameFilter filters only textfiles for the open-dialog
 	 * @author Martin Kiessling
 	 */
 	private class FileChooser extends JFileChooser {
 		private static final long serialVersionUID = 1L;
 
 		/* variables */
 		private File file;
 		private FileFilter filterTXT = new FileFilter() {
 			
 			@Override
 			public boolean accept(File f) {
 				return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
 			}
 
 			@Override
 			public String getDescription() {
 				return ".txt";
 			}
 		};
 
 		/**
 		 * constructor of FileChooser
 		 */
 		public FileChooser() {
 			super();
 		}
 
 		/**
 		 * This method returns a file which is meant to be the input-file.
 		 * 
 		 * @return returns the input-file
 		 */
 		public File inputFile() {
 			this.setFileFilter(filterTXT);
 			this.setMultiSelectionEnabled(false);
 			int state = this.showOpenDialog(contentPane);
 			if (state == JFileChooser.APPROVE_OPTION) {
 				if (this.getSelectedFile().exists()) {
 					file = this.getSelectedFile();
 					return file;
 				} else {
 					JOptionPane.showMessageDialog(contentPane,
 							"Die angegebene Datei existiert nicht.",
 							"Information", JOptionPane.INFORMATION_MESSAGE);
 					if (inputFile != null) {
 						return inputFile;
 					} else {
 						return null;
 					}
 				}
 			} else {
 				if (inputFile != null) {
 					return inputFile;
 				} else {
 					return null;
 				}
 			}
 		}
 
 		/**
 		 * This method returns a file which is meant to be the output-file.
 		 * 
 		 * @return returns the output-file
 		 */
 		public File outputFile() {
 			int state = this.showSaveDialog(contentPane);
 			if (state == JFileChooser.APPROVE_OPTION) {
 				file = this.getSelectedFile();
 				return file;
 			} else {
 				if (outputFile != null) {
 					return outputFile;
 				} else {
 					return null;
 				}
 			}
 		}
 	}
 }
