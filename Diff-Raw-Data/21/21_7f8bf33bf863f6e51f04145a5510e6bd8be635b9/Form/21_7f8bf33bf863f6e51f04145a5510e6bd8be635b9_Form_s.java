 package tim.view.appointmentdialog;
 
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.ParseException;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import tim.application.Config;
 import tim.application.exception.ExceptionFormatter;
 import tim.application.exception.PersistanceException;
 import tim.application.utils.ErrorHandler;
 import tim.controller.AppointmentDialogController;
 import tim.model.Client;
 import tim.model.Element;
 import tim.view.appointmentdialog.AppointmentDialogValidator;
 
 public class Form extends JPanel {
 	AppointmentDialogController controller;
 	private JLabel lblErrorMsg;
 	private JLabel lblClient;
 	private JComboBox cbClient;
 	private JLabel lblDate;
 	private JTextField txtDate;
 	private JLabel lblBegin;
 	private JComboBox cbBeginH;
 	private JComboBox cbBeginM;
 	private JLabel lblEnd;
 	private JComboBox cbEndM;
 	private JComboBox cbEndH;
 	private JLabel lblDescription;
 	private JTextArea txtDescription;
 	private JButton btnSave;
 	private JButton btnDelete;
 	private JButton btnCancel;
 	private JPanel buttonPanel;
 	private JPanel errorPanel;
 
 	public Form(AppointmentDialogController controller) {
 		this.controller = controller;
 		errorPanel = new JPanel();
 		lblErrorMsg = new JLabel (" ");
 		lblErrorMsg.setForeground(Color.RED);
 
 		
 		errorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		errorPanel.add(lblErrorMsg);
 		
 
 		
 		lblClient = new JLabel(Config.RESSOURCE_BUNDLE.getString("dialogClient") + " :");
 		cbClient = new JComboBox();
 		
 		try {
 			for (Element element : controller.getClients()) {
 				cbClient.addItem((Client) element);
 			}
 		} catch (PersistanceException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 
 		lblDate = new JLabel(Config.RESSOURCE_BUNDLE.getString("dialogDate") + " :");
 		txtDate = new JTextField(10);
 
 		lblBegin = new JLabel(Config.RESSOURCE_BUNDLE.getString("dialogBegin") + " :");
 		cbBeginH = new JComboBox();
 		cbBeginM = new JComboBox();
 
 		lblEnd = new JLabel(Config.RESSOURCE_BUNDLE.getString("dialogBegin") + " :");
 		cbEndH = new JComboBox();
 		cbEndM = new JComboBox();
 
 		lblDescription = new JLabel(Config.RESSOURCE_BUNDLE.getString("dialogDescription") + " :");
 		txtDescription = new JTextArea(10, 20);
 		//txtDescription.setBorder(BorderFactory.createCompoundBorder());
 		
 		buttonPanel = new JPanel();
 		btnCancel = new JButton(Config.RESSOURCE_BUNDLE.getString("dialogCancel"));
 		btnSave = new JButton(Config.RESSOURCE_BUNDLE.getString("dialogSave"));
 		
 		btnSave.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				validate();	
 			}
 		});
 		
 		btnDelete = new JButton("Delete :");
 		
 		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		buttonPanel.add(btnCancel);
 		buttonPanel.add(btnDelete);
 		buttonPanel.add(btnSave);
 	
 
 		setLayout(new GridBagLayout());
 		GridBagConstraints gbc = new GridBagConstraints();
 
 		gbc.insets = new Insets(5, 5, 5, 5);
 		gbc.gridwidth = 1;
 
 		/*
 		 * Labels
 		 */
 		gbc.anchor = GridBagConstraints.EAST;
 		
 		
 		gbc.gridy = 5;
 		gbc.gridx = 0;
 		add(lblDescription, gbc);
 
 		gbc.gridy = 1;
 		gbc.gridx = 0;
 		add(lblClient, gbc);
 
 		gbc.gridy = 2;
 		gbc.gridx = 0;
 		gbc.gridwidth = 1;
 		add(lblDate, gbc);
 
 		gbc.gridy = 3;
 		gbc.gridx = 0;
 		gbc.gridwidth = 1;
 		add(lblBegin, gbc);
 
 		gbc.gridy = 4;
 		gbc.gridx = 0;
 		gbc.gridwidth = 1;
 		add(lblEnd, gbc);
 
 		/*
 		 * Components
 		 */
 		gbc.anchor = GridBagConstraints.WEST;
 
 		gbc.gridy = 3;
 		gbc.gridx = 1;
 		add(cbBeginH, gbc);
 
 		gbc.gridy = 3;
 		gbc.gridx = 2;
 		add(new JLabel(":"), gbc);
 
 		gbc.gridy = 3;
 		gbc.gridx = 3;
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		add(cbBeginM, gbc);
 
 		gbc.gridy = 4;
 		gbc.gridx = 1;
 		add(cbEndH, gbc);
 
 		gbc.gridy = 4;
 		gbc.gridx = 2;
 		add(new JLabel(":"), gbc);
 
 		gbc.gridy = 4;
 		gbc.gridx = 3;
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		add(cbEndM, gbc);
 
 		gbc.gridy = 5;
 		gbc.gridx = 1;
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		add(txtDescription, gbc);
 		
 		gbc.gridy = 1;
 		gbc.gridx = 1;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		add(cbClient, gbc);
 
 		gbc.gridy = 2;
 		gbc.gridx = 1;
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		add(txtDate, gbc);
 
 		/*
 		 * Buttonpanel
 		 */
 		gbc.gridy = 6;
 		gbc.gridx = 0;
 		gbc.anchor = GridBagConstraints.SOUTH;
 		gbc.insets = new Insets(15, 0, 0, 0);
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		add(buttonPanel, gbc);
 		
 		gbc.gridy = 0;
 		gbc.gridx = 0;
 		gbc.anchor = GridBagConstraints.NORTH;
 		gbc.insets = new Insets(15, 0, 0, 0);
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		add(errorPanel, gbc);
 		
 		
 		init();
 	}
 
 	public void init() {
 
 		for (int i = Config.CALENDAR_DAY_START; i <= Config.CALENDAR_DAY_END; i++) {
 			cbBeginH.addItem(i);
 			cbEndH.addItem(i);
 		}
 
 		for (int i = 0; i < 60; i += Config.CALENDAR_DAY_INTERVAL) {
 			cbBeginM.addItem(i);
 			cbEndM.addItem(i);
 		}
 	}
 	
 	public void validate() {
 		int beginH = 0, endH = 0, beginM = 0, endM = 0;
 		
 		beginH = (Integer)cbBeginH.getSelectedItem();
 		endH = (Integer) cbEndH.getSelectedItem();
 		beginM = (Integer) cbBeginM.getSelectedItem();
 		endM = (Integer) cbEndM.getSelectedItem();
 		
 		Client client = (Client) cbClient.getSelectedItem();
 		
 		
 		
 		lblErrorMsg.setText(" ");
 		if (!(AppointmentDialogValidator.dateField(lblDate, txtDate) && AppointmentDialogValidator.startEnd(lblBegin, lblEnd, beginH, beginM, endH, endM))) {
 			lblErrorMsg.setText("Please check the following errors: ");
 		}
 		else {
 			try {
 				if (controller.save(null, client, txtDate.getText(), beginH, beginM, endH, endM, txtDescription.getText())) {
 					//close dialog
 				}
 			} catch (ParseException ex) {
				try {
					throw new PersistanceException(ExceptionFormatter.format(ex, this.getClass().getName(), "validate"));
				} catch (PersistanceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 			} catch (ClassCastException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (PersistanceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 
 	}
 }
