 package Frames;
 
 import java.awt.ActiveEvent;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 
 import org.tmatesoft.sqljet.core.SqlJetException;
 
 import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;
 
 import prefuse.data.Node;
 import prefuse.data.Table;
 import prefuse.data.Tree;
 import prefuse.visual.VisualItem;
 import ClassAdminBackEnd.EntityType;
 import ClassAdminBackEnd.Project;
 import ClassAdminFrontEnd.BackgroundGradientPanel;
 import ClassAdminFrontEnd.DatePicker;
 import ClassAdminFrontEnd.TreeView;
 
 public class FrmUpdateNode {
 	private Tree activeTree = null;
 	private JDialog frame = null;
 	private Project activeProject = null;
 	private TreeView activeTreeView = null;
 	private LinkedList<EntityType> activeTreeLinkedList = null;
 	private Table nodes;
 	private JFrame parentF = null;
 	private EntityType activeentity;
 	private VisualItem activeItem;
 	private BackgroundGradientPanel backgroundPanel;
 
 	public FrmUpdateNode(Project project, JFrame parentFrame, EntityType entity, VisualItem visualItem) {
 		activeProject = project;
 		parentF = parentFrame;
 		activeentity = entity;
 		activeItem = visualItem;
 
 		frame = new JDialog(parentFrame, true);
 		frame.setSize(356, 233);
 		frame.setLocation(parentF.getWidth()+parentF.getX()-250, parentF.getY()+40);
 		frame.getContentPane().setLayout(null);
 
		frame.setTitle("Edit Node");
 
 		JPanel contentPane = new JPanel();
 		contentPane.setBounds(0, 0, frame.getWidth(), frame.getHeight());
 		frame.setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 		backgroundPanel = new BackgroundGradientPanel(contentPane);
 		backgroundPanel.setBounds(0, 0, contentPane.getWidth(), contentPane.getHeight());
 		contentPane.add(backgroundPanel);
 		backgroundPanel.setLayout(null);
 
 		final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
 		Date date = new Date();
 		final JLabel lblName = new JLabel("Name:");
 		final JTextField txtName = new JTextField(activeentity.getName());
 		final ButtonGroup group = new ButtonGroup();
 		final JLabel lblDate = new JLabel("Date of assesment:");
 		final JTextArea txtDate = new JTextArea();
 		final JLabel lblType = new JLabel("Type:");
 		final JComboBox cmbType = new JComboBox();
 		final JButton btnDate = new JButton("change");
 		final JLabel lblWeight = new JLabel("Weight");
 		JButton btnUpdate = new JButton("Update");
 		JButton btnClose = new JButton("Close");
 
 		cmbType.addItem("Mark - Weighted Average");
 		cmbType.addItem("Mark - Sum ");
 		cmbType.addItem("Mark - Best N");
 		cmbType.addItem("Text");
 		cmbType.addItem("Mixed");
 
 		cmbType.setSelectedIndex(activeentity.getEntityTypeClass());
 
 		SpinnerNumberModel snmWeight = new SpinnerNumberModel(new Double(1.00), // value
 				new Double(0.00), // min
 				new Double(100.00), // max
 				new Double(0.01)); // step
 		final JSpinner txtWeight = new JSpinner(snmWeight);
 		txtWeight.setValue(activeentity.getWeight());
 		txtName.setSize(120, 30);
 
 		// get node's existing data
 		if (activeentity.getDate() == null) {
 			txtDate.setText(dateFormat.format(new Date()));
 		} else {
 			txtDate.setText(dateFormat.format(activeentity.getDate()).toString());
 		}
 		btnDate.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				txtDate.setText(new DatePicker(frame).setPickedDate());
 			}
 		});
 
 		btnUpdate.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				boolean b = true;
 				boolean isText = false;
 				String assDate = "";
 				double weight = 1.0;
 
 				// validation of values
 				if (txtName.getText() == null || txtName.getText().equals("")) {
 					b = false;
 					lblName.setForeground(Color.red);
 				}
 
 				Date d = null;
 				try {
 					d = dateFormat.parse(txtDate.getText());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 				if (d == null) {
 					b = false;
 					lblDate.setForeground(Color.RED);
 				}
 
 				if (b) {
 
 
 					String oldName = activeentity.getName();
 					String oldIsTextField = activeentity.getIsTextField().toString();
 					String oldDate;
 
 					if (activeentity.getDate() == null)
 						oldDate = dateFormat.format(new Date());
 					else
 						oldDate = dateFormat.format(activeentity.getDate());
 					String oldWeight = Double.toString(activeentity.getWeight());
 
 					//create audit entry and update node information
 					activeProject.getAudit().updateNode(oldName, oldIsTextField, oldDate, oldWeight, txtName.getText(), Boolean.toString(isText), dateFormat.format(d), txtWeight.getValue().toString());
 					activeentity.updateEntity(txtName.getText(), isText, d, (Double) txtWeight.getValue());
 					activeentity.setEntityTypeClass(cmbType.getSelectedIndex());
 					
 					//update front end information
 					activeProject.updateTables();
 					activeItem.setString("name", txtName.getText());
 
 					// reset all values
 					txtWeight.setValue(new Double(1.0));
 					txtName.setText(null);
 					txtDate.setText(dateFormat.format(new Date()));
 					lblName.setForeground(Color.BLACK);
 					lblDate.setForeground(Color.BLACK);
 					isText = false;
 
 					txtName.requestFocus(true);
 					frame.dispose();
 				}// if b
 			}// actionListener
 		});
 
 		btnClose.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				activeProject.updateTables();
 				frame.dispose();
 			}
 		});
 
 		frame.addWindowListener(new WindowListener() {
 
 			@Override
 			public void windowOpened(WindowEvent e) {
 			}
 
 			@Override
 			public void windowIconified(WindowEvent e) {
 			}
 
 			@Override
 			public void windowDeiconified(WindowEvent e) {
 			}
 
 			@Override
 			public void windowDeactivated(WindowEvent e) {
 			}
 
 			@Override
 			public void windowClosing(WindowEvent e) {
 				activeProject.updateTables();
 			}
 
 			@Override
 			public void windowClosed(WindowEvent e) {
 			}
 
 			@Override
 			public void windowActivated(WindowEvent e) {
 				txtName.requestFocus(true);
 			}
 		});
 
 		//create form
 		JPanel pnlDate = new JPanel(new GridLayout(1, 2));
 		pnlDate.add(txtDate);
 		pnlDate.add(btnDate);
 		
 		lblType.setBounds(10, 11, 60, 30);
 		cmbType.setBounds(175, 11, 150, 30);
 		lblName.setBounds(10, 46, 71, 30);
 		txtName.setBounds(175, 46, 150, 30);
 		lblDate.setBounds(10, 80, 158, 30);
 		pnlDate.setBounds(175, 80, 150, 30);
 		lblWeight.setBounds(10, 114, 116, 30);
 		txtWeight.setBounds(175, 114, 150, 30);
 		btnUpdate.setBounds(10, 158, 150, 30);
 		btnClose.setBounds(175, 155, 150, 30);
 		
 		lblType.setForeground(new Color(0xEDEDED));
 		lblName.setForeground(new Color(0xEDEDED));
 		lblDate.setForeground(new Color(0xEDEDED));
 		lblWeight.setForeground(new Color(0xEDEDED));
 		
 		backgroundPanel.add(lblType);
 		backgroundPanel.add(cmbType);
 		backgroundPanel.add(lblName);
 		backgroundPanel.add(txtName);
 		backgroundPanel.add(lblDate);
 		backgroundPanel.add(pnlDate);
 		backgroundPanel.add(lblWeight);
 		backgroundPanel.add(txtWeight);
 		backgroundPanel.add(btnUpdate);
 		backgroundPanel.add(btnClose);
 
 	}
 
 	public void showFrmUpdateNode(int p) {
 		frame.setVisible(true);
 	}
 
 }
