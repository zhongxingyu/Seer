 package view;
 
import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
 import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 
 public class Window {
 
 	private JFrame frame;
 	public static JButton btnSelecionarCor;
 	public final static Canvas canvas = new Canvas();
 	private JTextField textField;
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Window window = new Window();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public Window() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 607, 498);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		
 		canvas.setBackground(Color.WHITE);
 		canvas.setLayout(null);
 		
 		JPanel panel = new JPanel();
 		
 		JButton btnLimparCanvas = new JButton("Limpar Canvas");
 		btnLimparCanvas.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				canvas.getPoints().clear();
 				canvas.repaint();
 			}
 		});
 		
 		btnSelecionarCor = new JButton("Selecionar cor");
 		
 		JComboBox comboBox = new JComboBox();
 		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Rasteriza\u00E7\u00E3o", "Wireframe", "Ambos"}));
 		comboBox.addActionListener(new ActionListener() {			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JComboBox comboBox = (JComboBox) arg0.getSource();
 				
 				switch (comboBox.getSelectedIndex()) {
 				case 0:
 					canvas.setWireframe(false);
 					canvas.setRasterization(true);
 					break;
 				case 1:
 					canvas.setWireframe(true);
 					canvas.setRasterization(false);
 					break;
 				case 2:
 					canvas.setWireframe(true);
 					canvas.setRasterization(true);
 					break;				
 				}
 				canvas.repaint();
 			}
 		});
 		
 		JLabel lblNewLabel = new JLabel("# Lados\n");
 		
 		textField = new JTextField();
 		textField.setText("3");
 		textField.setColumns(10);
 		textField.getDocument().addDocumentListener(new DocumentListener() {
 			
 			@Override
 			public void removeUpdate(DocumentEvent arg0) {
 			}
 			
 			@Override
 			public void insertUpdate(DocumentEvent arg0) {
 				canvas.setNumEdges(new Integer(textField.getText()));
 				canvas.repaint();
 			}
 			
 			@Override
 			public void changedUpdate(DocumentEvent arg0) {			
 			}
 		});
 		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 607, GroupLayout.PREFERRED_SIZE)
 						.addComponent(canvas, GroupLayout.PREFERRED_SIZE, 607, GroupLayout.PREFERRED_SIZE))
 					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 126, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(canvas, GroupLayout.PREFERRED_SIZE, 344, GroupLayout.PREFERRED_SIZE)
 					.addGap(44))
 		);
 		GroupLayout gl_panel = new GroupLayout(panel);
 		gl_panel.setHorizontalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addGap(5)
 					.addComponent(btnLimparCanvas, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE)
 					.addGap(5)
 					.addComponent(btnSelecionarCor)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addGap(6)
 					.addComponent(lblNewLabel)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(textField, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
 					.addGap(79))
 		);
 		gl_panel.setVerticalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addGap(5)
 					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(btnLimparCanvas)
 						.addComponent(btnSelecionarCor)
 						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addComponent(lblNewLabel)
 						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addGap(92))
 		);
 		panel.setLayout(gl_panel);
 		frame.getContentPane().setLayout(groupLayout);
 		btnSelecionarCor.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				SwingColorChooserDemo.createAndShowGUI();
 			}
 		});
 	}
 }
