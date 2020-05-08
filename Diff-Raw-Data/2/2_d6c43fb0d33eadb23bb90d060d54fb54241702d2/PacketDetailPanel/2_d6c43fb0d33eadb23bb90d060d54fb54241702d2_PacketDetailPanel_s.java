 package ch.compass.gonzoproxy.view;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Enumeration;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextPane;
 import javax.swing.SwingConstants;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableColumn;
 
 import ch.compass.gonzoproxy.controller.RelayController;
 import ch.compass.gonzoproxy.listener.StateListener;
 import ch.compass.gonzoproxy.model.packet.Field;
 import ch.compass.gonzoproxy.model.packet.Packet;
 import ch.compass.gonzoproxy.model.ui.PacketDetailTableModel;
 import ch.compass.gonzoproxy.relay.settings.ConnectionState;
 import ch.compass.gonzoproxy.relay.settings.RelayState;
 
 public class PacketDetailPanel extends JPanel {
 
 	private static final long serialVersionUID = -5129650768327234148L;
 
 	private JTable table_detail;
 	private JTextPane textPane_ascii;
 	private JTextPane textPane_hex;
 	private Packet editPacket;
 	private RelayController controller;
 	private PacketDetailTableModel detailTableModel;
 	private Field editField;
 	private JLabel lblRPort;
 	private JLabel lblRHost;
 	private JLabel lblLPort;
 	private JLabel lblStatus;
 
 	public PacketDetailPanel(RelayController controller) {
 		this.controller = controller;
 		this.editPacket = new Packet();
 		this.editField = new Field();
 		this.detailTableModel = new PacketDetailTableModel(editPacket,
 				controller.getSessionModel());
 		initGui();
 		registerSessionStateNotifier();
 	}
 
 	private void registerSessionStateNotifier() {
 		controller.addSessionStateListener(
 				new StateListener() {
 
 					@Override
 					public void sessionStateChanged(RelayState state) {
 						if(state.equals(ConnectionState.MODE_FAILURE)){
 							lblStatus.setForeground(Color.RED);
 						}
 						lblStatus.setText(state.getDescription());
 						updateSessionPrefs();
 					}
 				});
 
 	}
 
 	protected void updateSessionPrefs() {
 		lblLPort.setText(Integer.toString(controller.getCurrentListenPort()));
 		lblRPort.setText(Integer.toString(controller.getCurrentRemotePort()));
 		lblRHost.setText(controller.getCurrentRemoteHost());
 	}
 
 	private void initGui() {
 		setBorder(new TitledBorder(null, "Packet Detail", TitledBorder.LEADING,
 				TitledBorder.TOP, null, null));
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 300, 0, 0 };
 		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
 		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
 		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
 		setLayout(gridBagLayout);
 
 		JScrollPane scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridx = 0;
 		gbc_scrollPane.gridy = 0;
 		add(scrollPane, gbc_scrollPane);
 
 		table_detail = new JTable();
 		table_detail.setModel(detailTableModel);
 		table_detail.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					JTable target = (JTable) e.getSource();
 					int row = target.getSelectedRow();
 					AddNewModifierDialog nd = new AddNewModifierDialog(
 							editPacket, editPacket.getFields().get(row),
 							controller);
 					nd.setVisible(true);
 				}
 			}
 		});
 		configureTable(table_detail);
 		scrollPane.setViewportView(table_detail);
 
 		JPanel panel = new JPanel();
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.insets = new Insets(0, 0, 5, 0);
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.gridx = 1;
 		gbc_panel.gridy = 0;
 		add(panel, gbc_panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[] { 0, 0 };
 		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
 		gbl_panel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
 		gbl_panel.rowWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
 		panel.setLayout(gbl_panel);
 
 		JLabel lblHex = new JLabel("Hex: ");
 		GridBagConstraints gbc_lblHex = new GridBagConstraints();
 		gbc_lblHex.anchor = GridBagConstraints.NORTH;
 		gbc_lblHex.insets = new Insets(0, 0, 5, 5);
 		gbc_lblHex.gridx = 0;
 		gbc_lblHex.gridy = 0;
 		panel.add(lblHex, gbc_lblHex);
 
 		JScrollPane scrollPane_hex = new JScrollPane();
 		GridBagConstraints gbc_scrollPane_hex = new GridBagConstraints();
 		gbc_scrollPane_hex.insets = new Insets(0, 0, 5, 0);
 		gbc_scrollPane_hex.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_hex.gridx = 1;
 		gbc_scrollPane_hex.gridy = 0;
 		panel.add(scrollPane_hex, gbc_scrollPane_hex);
 
 		textPane_hex = new JTextPane();
 		scrollPane_hex.setViewportView(textPane_hex);
 
 		JLabel lblAscii = new JLabel("Ascii: ");
 		GridBagConstraints gbc_lblAscii = new GridBagConstraints();
 		gbc_lblAscii.anchor = GridBagConstraints.NORTH;
 		gbc_lblAscii.insets = new Insets(0, 0, 0, 5);
 		gbc_lblAscii.gridx = 0;
 		gbc_lblAscii.gridy = 1;
 		panel.add(lblAscii, gbc_lblAscii);
 
 		JScrollPane scrollPane_ascii = new JScrollPane();
 		GridBagConstraints gbc_scrollPane_ascii = new GridBagConstraints();
 		gbc_scrollPane_ascii.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_ascii.gridx = 1;
 		gbc_scrollPane_ascii.gridy = 1;
 		panel.add(scrollPane_ascii, gbc_scrollPane_ascii);
 
 		textPane_ascii = new JTextPane();
 		scrollPane_ascii.setViewportView(textPane_ascii);
 
 		lblStatus = new JLabel("Disconnected");
 		lblStatus.setHorizontalAlignment(SwingConstants.LEFT);
 		GridBagConstraints gbc_lblStatus = new GridBagConstraints();
 		gbc_lblStatus.anchor = GridBagConstraints.WEST;
 		gbc_lblStatus.insets = new Insets(0, 0, 0, 5);
 		gbc_lblStatus.gridx = 0;
 		gbc_lblStatus.gridy = 1;
 		add(lblStatus, gbc_lblStatus);
 
 		JPanel panelStatus2 = new JPanel();
 		GridBagConstraints gbc_panelStatus2 = new GridBagConstraints();
 		gbc_panelStatus2.fill = GridBagConstraints.BOTH;
 		gbc_panelStatus2.gridx = 1;
 		gbc_panelStatus2.gridy = 1;
 		add(panelStatus2, gbc_panelStatus2);
 		GridBagLayout gbl_panelStatus2 = new GridBagLayout();
 		gbl_panelStatus2.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
 		gbl_panelStatus2.rowHeights = new int[] { 0, 0 };
 		gbl_panelStatus2.columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0,
 				0.0, 0.0, 0.0, Double.MIN_VALUE };
 		gbl_panelStatus2.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
 		panelStatus2.setLayout(gbl_panelStatus2);
 
 		JLabel lblListenport = new JLabel("Listenport:");
 		lblListenport.setHorizontalAlignment(SwingConstants.RIGHT);
 		GridBagConstraints gbc_lblListenport = new GridBagConstraints();
 		gbc_lblListenport.anchor = GridBagConstraints.EAST;
 		gbc_lblListenport.insets = new Insets(0, 0, 0, 5);
 		gbc_lblListenport.gridx = 1;
 		gbc_lblListenport.gridy = 0;
 		panelStatus2.add(lblListenport, gbc_lblListenport);
 
 		lblLPort = new JLabel("");
 		lblLPort.setHorizontalAlignment(SwingConstants.LEFT);
 		GridBagConstraints gbc_label_1 = new GridBagConstraints();
 		gbc_label_1.insets = new Insets(0, 0, 0, 5);
 		gbc_label_1.gridx = 2;
 		gbc_label_1.gridy = 0;
 		panelStatus2.add(lblLPort, gbc_label_1);
 
 		JLabel lblRemotehost = new JLabel("  Remotehost:");
 		lblRemotehost.setHorizontalAlignment(SwingConstants.RIGHT);
 		GridBagConstraints gbc_lblRemotehost = new GridBagConstraints();
 		gbc_lblRemotehost.insets = new Insets(0, 0, 0, 5);
 		gbc_lblRemotehost.gridx = 3;
 		gbc_lblRemotehost.gridy = 0;
 		panelStatus2.add(lblRemotehost, gbc_lblRemotehost);
 
 		lblRHost = new JLabel("");
 		lblRHost.setHorizontalAlignment(SwingConstants.LEFT);
 		GridBagConstraints gbc_label_3 = new GridBagConstraints();
 		gbc_label_3.insets = new Insets(0, 0, 0, 5);
 		gbc_label_3.gridx = 4;
 		gbc_label_3.gridy = 0;
 		panelStatus2.add(lblRHost, gbc_label_3);
 
 		JLabel lblRemoteport = new JLabel("  Remoteport:");
 		lblRemoteport.setHorizontalAlignment(SwingConstants.RIGHT);
 		GridBagConstraints gbc_lblRemoteport = new GridBagConstraints();
 		gbc_lblRemoteport.insets = new Insets(0, 0, 0, 5);
 		gbc_lblRemoteport.gridx = 5;
 		gbc_lblRemoteport.gridy = 0;
 		panelStatus2.add(lblRemoteport, gbc_lblRemoteport);
 
 		lblRPort = new JLabel("");
 		lblRPort.setHorizontalAlignment(SwingConstants.LEFT);
 		GridBagConstraints gbc_label_5 = new GridBagConstraints();
 		gbc_label_5.gridx = 6;
 		gbc_label_5.gridy = 0;
 		panelStatus2.add(lblRPort, gbc_label_5);
 	}
 
 	public void clearFields() {
 		textPane_ascii.setText("");
 		textPane_hex.setText("");
 	}
 
 	public void setPacket(Packet editPacket) {
 		this.editPacket = editPacket;
 		this.detailTableModel.setPacket(editPacket);
 		clearFields();
 	}
 
 	private void updateFields() {
 		textPane_ascii.setText(editField.toAscii());
 		textPane_hex.setText(editField.getValue());
 	}
 
 	private void configureTable(final JTable table) {
 		table.setSelectionMode(0);
 		table.getSelectionModel().addListSelectionListener(
 				new ListSelectionListener() {
 
 					@Override
 					public void valueChanged(ListSelectionEvent e) {
 						int index = table.getSelectedRow();
 						if (index == -1) {
 							PacketDetailPanel.this.clearFields();
 						} else {
 							PacketDetailPanel.this
 									.setField(PacketDetailPanel.this.editPacket
 											.getFields().get(index));
 						}
 					}
 				});
 		table.getTableHeader().setReorderingAllowed(false);
 		Enumeration<TableColumn> a = table.getColumnModel().getColumns();
 		for (int i = 0; a.hasMoreElements(); i++) {
 			TableColumn tb = (TableColumn) a.nextElement();
 			switch (i) {
 			case 0:
 				tb.setPreferredWidth(60);
 				break;
 			case 1:
 				tb.setPreferredWidth(200);
 				break;
 			case 2:
 				tb.setPreferredWidth(200);
 				break;
 			}
 
 		}
 	}
 
 	protected void setField(Field editField) {
 		this.editField = editField;
 		updateFields();
 	}
 
 }
