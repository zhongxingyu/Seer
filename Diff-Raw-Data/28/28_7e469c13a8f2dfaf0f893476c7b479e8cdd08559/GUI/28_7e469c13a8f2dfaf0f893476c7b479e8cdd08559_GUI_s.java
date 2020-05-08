 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import model.DiagramObject;
 import model.UMLCRUD;
 import model.UMLClass;
 import model.UMLUsecase;
 
 import controller.GUIController;
 
 public class GUI {
 	private GUIController guic = new GUIController();
 	private JTextField filename = new JTextField(), dir = new JTextField();
 	private JFrame frame = new JFrame();
 	private JFileChooser fileBrowser = new JFileChooser();
 	private JComboBox<String> exportType = new JComboBox<String>();
 
 	public GUI() {
 		frame.setLayout(new BorderLayout());
 		frame.getContentPane().setBackground(Color.WHITE);
 		
 		frame.setSize(500,500);
 		frame.setVisible(true);
 		frame.setResizable(false);
 		
 		JButton readButton = new JButton("read file");
 		readButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				int rVal = fileBrowser.showOpenDialog(frame);
 				if (rVal == JFileChooser.APPROVE_OPTION) {
 					filename.setText(fileBrowser.getSelectedFile().getName());
 					dir.setText(fileBrowser.getCurrentDirectory().toString());
 					guic.read(dir.getText()+"\\"+filename.getText());
					initializeUMLDropdownPanel();
 				}
 				if (rVal == JFileChooser.CANCEL_OPTION) {
 					filename.setText("You pressed cancel");
 					dir.setText("");
 				}
 			}
 		});
 		
 		exportType.addItem("xmlmd");
 		
 		JLabel chooseExportType = new JLabel("Kies export formaat");
 		
 		JButton exportButton = new JButton("save file");
 		exportButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				int rVal = fileBrowser.showSaveDialog(frame);
 				if (rVal == JFileChooser.APPROVE_OPTION) {
 					filename.setText(fileBrowser.getSelectedFile().getName());
 					dir.setText(fileBrowser.getCurrentDirectory().toString());
 					guic.write(dir.getText()+"\\"+filename.getText(), (String) exportType.getSelectedItem());
 				}
 				if (rVal == JFileChooser.CANCEL_OPTION) {
 					filename.setText("You pressed cancel");
 					dir.setText("");
 				}
 			}
 		});
 		
 		JPanel exportPanel = new JPanel();
 		exportPanel.setLayout(new GridLayout(3,1));
 		exportPanel.add(chooseExportType);
 		exportPanel.add(exportType);
 		exportPanel.add(exportButton);
 
 		JButton reportButton = new JButton("generate report");
 		reportButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				guic.generateReport();
 			}
 		});		
 		
 		JPanel drawPanel = new JPanel();
 		drawPanel.setPreferredSize(new Dimension(400,400));
 		drawPanel.setBackground(Color.RED);
 		drawPanel.setForeground(Color.BLACK);
 
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.add(readButton);
 		buttonPanel.add(exportPanel);
 		buttonPanel.add(reportButton);
 		
 		frame.add(buttonPanel, BorderLayout.NORTH);
 		frame.add(drawPanel, BorderLayout.CENTER);
 
 		Graphics g = drawPanel.getGraphics();
 		g.fillRect(40, 160, 50, 160);
 	}
 	
	private void initializeUMLDropdownPanel() {
		JPanel modelDropdowns = new JPanel();
 		JComboBox<UMLClass> UMLClass = new JComboBox<UMLClass>();
 		JComboBox<UMLUsecase> UMLUsecase = new JComboBox<UMLUsecase>();
 		
 		if(guic.getModel() == null) {
 			System.out.println("NULL modeldiagram");
 			return;
 		}
 		
 		for(DiagramObject dio : guic.getModel().getDiagramObjects()) {
 			if(dio.getClass() == UMLClass.class) {
 				UMLClass.addItem((model.UMLClass) dio);
 			}
 			if(dio.getClass() == UMLUsecase.class) {
 				UMLUsecase.addItem((model.UMLUsecase) dio);
 			}
 		}
 		
 		modelDropdowns.setLayout(new BorderLayout());
 		modelDropdowns.add(UMLClass,BorderLayout.EAST);
 		modelDropdowns.add(UMLUsecase,BorderLayout.WEST);
		frame.add(modelDropdowns);
 	}
 }
