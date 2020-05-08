 package edu.wustl.cab2b.client.ui.searchDataWizard;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JScrollPane;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import org.xml.sax.XMLFilter;
 
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.mainframe.MainFrame;
 import edu.wustl.cab2b.client.ui.mainframe.NewWelcomePanel;
 import edu.wustl.cab2b.client.ui.util.WindowUtilities;
 import edu.wustl.cab2b.common.domain.DCQL;
 
 /**
  * This class displays the DCQL Panel which shows the XML format of DCQL along
  * with function of saving the XML file
  * 
  * @author gaurav_mehta
  */
 public class ShowDCQLPanel extends Cab2bPanel {
 
 	/* JDialog in which the Panel is displayed */
 	private JDialog dialog;
 
 	// Cab2bLabel in which the entire XML is
 	final private Cab2bLabel xmlTextPane = new Cab2bLabel();
 
 	// Cab2bPanel for showing Success and failure messages
 	final Cab2bPanel messagePanel = new Cab2bPanel();
 
 	private String dcqlString;
 
 	/**
 	 * @param dcql
 	 */
 	public ShowDCQLPanel(DCQL dcql) {
 		this.dcqlString = dcql.getDcqlQuery();
 		initGUI();
 	}
 
 	private void initGUI() {
 		String xmlText = new XmlParser().parseXml(dcqlString);
 
 		xmlTextPane.setText(xmlText);
 		xmlTextPane.setBackground(Color.WHITE);
 
 		Cab2bPanel xmlPanel = new Cab2bPanel();
 		xmlPanel.add(xmlTextPane);
 		xmlPanel.setBackground(Color.WHITE);
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.getViewport().add(xmlPanel);
 		scrollPane.getViewport().setBackground(Color.WHITE);
 
 		Cab2bPanel xmlNavigationPanel = new Cab2bPanel();
 		Cab2bButton exportButton = new Cab2bButton("Export");
 		Cab2bButton cancelButton = new Cab2bButton("Cancel");
 
 		// Action Listener for Export Button
 		exportButton.addActionListener(new ExportButtonListner());
 
 		// Action Listener for Cancel Button
 		cancelButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				dialog.dispose();
 			}
 		});
 
 		Cab2bPanel buttonPanel = new Cab2bPanel();
 		FlowLayout flowLayout = new FlowLayout(FlowLayout.RIGHT);
 		buttonPanel.setLayout(flowLayout);
 		buttonPanel.add(exportButton);
 		buttonPanel.add(cancelButton);
 		buttonPanel.setBackground(new Color(240, 240, 240));
 
 		xmlNavigationPanel.add("br left", messagePanel);
 		xmlNavigationPanel.add("hfill", buttonPanel);
 		xmlNavigationPanel.setPreferredSize(new Dimension(880, 50));
 		xmlNavigationPanel.setBackground(new Color(240, 240, 240));
 
 		setLayout(new BorderLayout());
 		add(scrollPane, BorderLayout.CENTER);
 		add(xmlNavigationPanel, BorderLayout.SOUTH);
 	}
 
 	/**
 	 * JDialog for showing DCQL XML Details Panel
 	 * 
 	 * @return
 	 */
 	public JDialog showInDialog() {
 		Dimension dimension = MainFrame.getScreenDimesion();
 		dialog = WindowUtilities.setInDialog(NewWelcomePanel.getMainFrame(),
 				this, "DCQL Xml", new Dimension((int) (dimension.width * 0.77),
 						(int) (dimension.height * 0.65)), true, false);
 		dialog.setVisible(true);
 
 		return dialog;
 	}
 
 	private boolean writeFile(String file, String fileContent) {
 		try {
 			PrintWriter out = new PrintWriter(new BufferedWriter(
 					new FileWriter(file)));
 			out.print(fileContent);
 			out.flush();
 			out.close();
 		} catch (IOException e) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Action listener class for Export Button. It saves the XML file to user
 	 * defined location in user defined format
 	 */
 	class ExportButtonListner implements ActionListener {
 
 		public void actionPerformed(ActionEvent actionEvent) {
 			JFileChooser fileChooser = new JFileChooser();
 
 			FileNameExtensionFilter filter = new FileNameExtensionFilter(
 					"Xml Document(*.xml)", "xml");
 			FileNameExtensionFilter filter1 = new FileNameExtensionFilter(
 					"Text Document(*.txt)", "txt");
 			fileChooser.setAcceptAllFileFilterUsed(false);
 			fileChooser.setFileFilter(filter);
 			fileChooser.setFileFilter(filter1);
 
 			// A call to JFileChooser's ShowSaveDialog PopUp
 			fileChooser.showSaveDialog(NewWelcomePanel.getMainFrame());
 
 			File file = fileChooser.getSelectedFile();
 			String fileName = file.toString();
 			FileFilter fileFilter = fileChooser.getFileFilter();
			if (fileFilter.getDescription().equalsIgnoreCase("Txt File Format")) {
 				fileName = fileName + ".txt";
 			} else {
 				fileName = fileName + ".xml";
 			}
 			// Function call for writing the File and saving it
 			boolean saveReturnValue = writeFile(fileName, dcqlString);
 			if (saveReturnValue == true) {
 				Cab2bLabel successResultLabel = new Cab2bLabel(
 						"File Saved Successfully");
 				successResultLabel.setForeground(Color.GREEN);
 				messagePanel.add(successResultLabel);
 				messagePanel.repaint();
 			} else {
 				Cab2bLabel failureResultLabel = new Cab2bLabel(
 						"File Could not be Saved");
 				failureResultLabel.setForeground(Color.RED);
 				messagePanel.add(failureResultLabel);
 				messagePanel.repaint();
 
 			}
 		}
 	}
 }
