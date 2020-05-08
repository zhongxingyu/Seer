 package gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 import config.GuiConfig;
 import data.Service;
 
 /**
  * @author Ghennadi Procopciuc
  */
 public class AddNewService extends JFrame {
 	private static final long	serialVersionUID	= 1L;
 
 	private JPanel				mainPanel;
 	private JLabel				nameLabel;
 	private JTextField			nameField;
 	private JLabel				timeLabel;
 	private JSpinner			timeSpinner;
 	private JLabel				priceLabel;
 	private JTextField			priceField;
 	private JPanel				bottomPanel;
 	private JButton				cancelButton;
 	private JButton				okButton;
 	private MainWindow			mainWindow;
 
 	public AddNewService(MainWindow mainWindow) {
 		this.mainWindow = mainWindow;
 		initComponents();
 	}
 
 	private void initComponents() {
 		mainPanel = new JPanel();
 		nameLabel = new JLabel();
 		nameField = new JTextField();
 		timeLabel = new JLabel();
 		timeSpinner = new JSpinner();
 		priceLabel = new JLabel();
 		priceField = new JTextField();
 		bottomPanel = new JPanel();
 		cancelButton = new JButton();
 		okButton = new JButton();
 
 		// this
 		Container contentPane = getContentPane();
 		contentPane.setLayout(new GridBagLayout());
 		((GridBagLayout) contentPane.getLayout()).columnWidths = new int[] { 15, 0, 10, 0 };
 		((GridBagLayout) contentPane.getLayout()).rowHeights = new int[] { 15, 0, 0, 10, 0 };
 		((GridBagLayout) contentPane.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0,
 				1.0E-4 };
 		((GridBagLayout) contentPane.getLayout()).rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0,
 				1.0E-4 };
 
 		// mainPanel
 		{
 			mainPanel.setLayout(new GridBagLayout());
 			((GridBagLayout) mainPanel.getLayout()).columnWidths = new int[] { 0, 110, 0 };
 			((GridBagLayout) mainPanel.getLayout()).rowHeights = new int[] { 0, 0, 0, 0 };
 			((GridBagLayout) mainPanel.getLayout()).columnWeights = new double[] { 0.0, 0.0, 1.0E-4 };
 			((GridBagLayout) mainPanel.getLayout()).rowWeights = new double[] { 0.0, 0.0, 0.0,
 					1.0E-4 };
 
 			// nameLabel
 			nameLabel.setText(GuiConfig.getValue(GuiConfig.NAME));
 			mainPanel.add(nameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
 					GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0,
 					0));
 			mainPanel.add(nameField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
 					GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0,
 					0));
 
 			// timeLabel
 			timeLabel.setText(GuiConfig.getValue(GuiConfig.TIME));
 			mainPanel.add(timeLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
 					GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0,
 					0));
 
 			// timeSpinner
 			timeSpinner.setModel(new SpinnerDateModel(new java.util.Date((System
 					.currentTimeMillis() / 60000) * 60000), new java.util.Date((System
 					.currentTimeMillis() / 60000) * 60000), null, java.util.Calendar.MINUTE));
 			timeSpinner.setPreferredSize(new Dimension(120, 20));
 			mainPanel.add(timeSpinner, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
 					GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0,
 					0));
 
 			// priceLabel
 			priceLabel.setText(GuiConfig.getValue(GuiConfig.PRICE));
 			mainPanel.add(priceLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
 					GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0,
 					0));
 			mainPanel.add(priceField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
 					GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,
 					0));
 		}
 		contentPane.add(mainPanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
 				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
 
 		// bottomPanel
 		{
 			bottomPanel.setLayout(new GridBagLayout());
 			((GridBagLayout) bottomPanel.getLayout()).columnWidths = new int[] { 0, 0, 0, 0 };
 			((GridBagLayout) bottomPanel.getLayout()).rowHeights = new int[] { 15, 0, 0 };
 			((GridBagLayout) bottomPanel.getLayout()).columnWeights = new double[] { 1.0, 0.0, 0.0,
 					1.0E-4 };
 			((GridBagLayout) bottomPanel.getLayout()).rowWeights = new double[] { 0.0, 0.0, 1.0E-4 };
 
 			// cancelButton
 			cancelButton.setText(GuiConfig.getValue(GuiConfig.CANCEL));
 			cancelButton.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					cancelButtonActionPerformed(e);
 				}
 			});
 			bottomPanel.add(cancelButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
 					GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0,
 					0));
 
 			// okButton
 			okButton.setText(GuiConfig.getValue(GuiConfig.OK));
 			okButton.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					okButtonActionPerformed(e);
 				}
 			});
 			bottomPanel.add(okButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
 					GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,
 					0));
 		}
 		contentPane.add(bottomPanel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
 				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		pack();
 		setLocationRelativeTo(null);
 	}
 
 	private void okButtonActionPerformed(ActionEvent e) {
 		System.out.println(timeSpinner.getValue());
 
 		if (nameField.getText().isEmpty()) {
 			JOptionPane.showMessageDialog(null,
 					GuiConfig.getValue(GuiConfig.EMPTY_SERVICE_NAME_ERROR),
 					GuiConfig.getValue(GuiConfig.EMPTY_SERVICE_NAME), JOptionPane.WARNING_MESSAGE);
 			return;
 		}
 
 		if (mainWindow.getServices().contains(new Service(nameField.getText()))) {
 			JOptionPane.showMessageDialog(null, GuiConfig.getValue(GuiConfig.SERVICE_TWICE_ERROR),
 					"", JOptionPane.WARNING_MESSAGE);
 			return;
 		}
 
 		Double price = 0.0;
 
 		try {
 			price = Double.parseDouble(priceField.getText());
 		} catch (NumberFormatException e2) {
 			JOptionPane.showMessageDialog(null, GuiConfig.getValue(GuiConfig.PRICE_ERROR), "",
 					JOptionPane.WARNING_MESSAGE);
 			return;
 		}
 
 		mainWindow.addService(new Service(nameField.getText()));
		mainWindow.getGui().addService(new Service(nameField.getText()));
 
 		setVisible(false);
 		dispose();
 	}
 
 	private void cancelButtonActionPerformed(ActionEvent e) {
 		setVisible(false);
 		dispose();
 	}
 
 	public static void main(String[] args) {
 		AddNewService service = new AddNewService(null);
 		service.setVisible(true);
 		service.setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}
 }
