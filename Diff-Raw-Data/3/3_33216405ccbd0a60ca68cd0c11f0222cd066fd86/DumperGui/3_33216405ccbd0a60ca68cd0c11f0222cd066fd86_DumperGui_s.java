 package org.mediawiki.dumper.gui;
 
 import javax.swing.JPanel;
 import javax.swing.JFrame;
 import java.awt.GridBagLayout;
 import javax.swing.JLabel;
 import java.awt.GridBagConstraints;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.SwingUtilities;
 
 import org.mediawiki.dumper.Tools;
 import org.mediawiki.importer.DumpWriter;
 import org.mediawiki.importer.MultiWriter;
 import org.mediawiki.importer.XmlDumpReader;
 import org.mediawiki.importer.XmlDumpWriter;
 
 public class DumperGui extends JFrame {
 	private static final long serialVersionUID = 1L;
 	private JPanel jContentPane = null;
 	private JLabel inputLabel = null;
 	private JTextField inputField = null;
 	private JButton inputBrowse = null;
 	private JButton goButton = null;
 	
 	private final JFrame frame = this;
 	private JLabel statusLabel = null;
 	
 	private boolean running = false;
 	
 	/**
 	 * This method initializes jTextField	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */
 	private JTextField getJTextField() {
 		if (inputField == null) {
 			inputField = new JTextField();
 		}
 		return inputField;
 	}
 
 	/**
 	 * This method initializes jButton	
 	 * 	
 	 * @return javax.swing.JButton	
 	 */
 	private JButton getJButton() {
 		if (inputBrowse == null) {
 			inputBrowse = new JButton();
 			inputBrowse.setText("Browse...");
 			inputBrowse.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
 					JFileChooser chooser = new JFileChooser();
 					chooser.showOpenDialog(frame);
 					File selection = chooser.getSelectedFile();
 					try {
 						inputField.setText(selection.getCanonicalPath());
 					} catch (IOException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 				}
 			});
 		}
 		return inputBrowse;
 	}
 
 	/**
 	 * This method initializes jButton	
 	 * 	
 	 * @return javax.swing.JButton	
 	 */
 	private JButton getJButton2() {
 		if (goButton == null) {
 			goButton = new JButton();
 			goButton.setText("Go!");
 			goButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					if (running) {
 						// NOP
 					} else {
 						running = true;
 						goButton.setEnabled(false);
 						
 						try {
 							startImport(inputField.getText());
 						} catch (FileNotFoundException e1) {
 							statusLabel.setText("FAILED: File not found");
 							running = false;
 							goButton.setEnabled(true);
 						} catch (IOException e1) {
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 							statusLabel.setText("FAILED: " + e1.getMessage());
 							running = false;
 							goButton.setEnabled(true);
 						}
 					}
 				}
 			});
 		}
 		return goButton;
 	}
 
 	void startImport(String inputFile) throws IOException {
 		// TODO work right ;)
 		final InputStream stream = Tools.openInputFile(inputFile);
 		DumpWriter writer = new MultiWriter();
 		DumpWriter progress = new GraphicalProgressFilter(writer, 1000, statusLabel);
 		final XmlDumpReader reader = new XmlDumpReader(stream, progress);
 		new Thread() {
 			public void run() {
 				try {
 					reader.readDump();
 					stream.close();
 				} catch(IOException e) {
 					setStatusLabel("FAILED: " + e.getMessage());
 				}
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run() {
 						running = false;
 						goButton.setEnabled(true);
 					}
 				});
 			}
 			
 			private void setStatusLabel(String text) {
 				final String _text = text;
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run() {
 						statusLabel.setText(_text);
 					}
 				});
 			}
 		}.start();
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		// Try to workaround Java 1.4 XML parser bug
		System.setProperty("entityExpansionLimit","2147483647");
		
 		DumperGui gui = new DumperGui();
 		gui.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		gui.setVisible(true);
 	}
 
 	/**
 	 * This is the default constructor
 	 */
 	public DumperGui() {
 		super();
 		initialize();
 	}
 
 	/**
 	 * This method initializes this
 	 * 
 	 * @return void
 	 */
 	private void initialize() {
 		this.setSize(640, 300);
 		this.setContentPane(getJContentPane());
 		this.setTitle("MediaWiki data dump importer");
 	}
 
 	/**
 	 * This method initializes jContentPane
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJContentPane() {
 		if (jContentPane == null) {
 			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
 			gridBagConstraints31.gridx = 1;
 			gridBagConstraints31.gridy = 2;
 			statusLabel = new JLabel();
 			statusLabel.setText("No input file selected.");
 			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
 			gridBagConstraints11.gridx = 2;
 			gridBagConstraints11.gridy = 2;
 			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
 			gridBagConstraints3.gridx = 1;
 			gridBagConstraints3.gridy = 3;
 			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
 			gridBagConstraints2.gridx = 2;
 			gridBagConstraints2.gridy = 1;
 			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
 			gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
 			gridBagConstraints1.gridy = 1;
 			gridBagConstraints1.weightx = 1.0;
 			gridBagConstraints1.gridx = 1;
 			GridBagConstraints gridBagConstraints = new GridBagConstraints();
 			gridBagConstraints.gridx = 0;
 			gridBagConstraints.gridy = 1;
 			inputLabel = new JLabel();
 			inputLabel.setText("Read dump from file:");
 			jContentPane = new JPanel();
 			jContentPane.setLayout(new GridBagLayout());
 			jContentPane.add(inputLabel, gridBagConstraints);
 			jContentPane.add(getJTextField(), gridBagConstraints1);
 			jContentPane.add(getJButton(), gridBagConstraints2);
 			jContentPane.add(getJButton2(), gridBagConstraints11);
 			jContentPane.add(statusLabel, gridBagConstraints31);
 		}
 		return jContentPane;
 	}
 
 }  //  @jve:decl-index=0:visual-constraint="9,10"
