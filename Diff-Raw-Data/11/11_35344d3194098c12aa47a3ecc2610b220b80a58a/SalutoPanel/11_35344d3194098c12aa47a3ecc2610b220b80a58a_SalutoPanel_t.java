 package client;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import client.LogController;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import javax.swing.ImageIcon;
 import javax.swing.UIManager;
 import javax.swing.JSeparator;
 import javax.swing.SwingConstants;
 
 /**
  * @author Veronica
  */
 public class SalutoPanel extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	/**
 	 * Dichiarazione Variabile
 	 * 
 	 * @uml.property name="lblSaluto"
 	 */
 	private JLabel lblSaluto; // Etichette
 	public JButton btnLogOut;
 	public JButton btnGestioneOrdine;
 	/**
 	 * @uml.property name="logController"
 	 * @uml.associationEnd
 	 */
 	private LogController logController;
 	/**
 	 * @uml.property name="nome"
 	 */
 	private String nome;
 	/**
 	 * @uml.property name="panel"
 	 * @uml.associationEnd
 	 */
 	private LogPanel panel;
 
 	// COSTRUTTORE
 	public SalutoPanel(String nome, LogPanel panel) {
 		setForeground(new Color(0, 100, 0));
 		setBackground(Color.WHITE);
 		this.setPanel(panel);
 		this.setNome(nome);
 
 		// Controller
 		logController = new LogController(this, panel);
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 0, 0, 52, 140, 176, 98, 79, 0 };
 		gridBagLayout.rowHeights = new int[] { 23, 30, 0 };
 		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,
 				0.0, 0.0, Double.MIN_VALUE };
 		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
 		setLayout(gridBagLayout);
 
 		lblSaluto = new JLabel("Ciao " + nome + "!");
 		lblSaluto.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 11));
 		lblSaluto.setForeground(new Color(0, 100, 0));
 		GridBagConstraints gbc_lblSaluto = new GridBagConstraints();
 		gbc_lblSaluto.gridwidth = 2;
 		gbc_lblSaluto.fill = GridBagConstraints.BOTH;
 		gbc_lblSaluto.insets = new Insets(0, 0, 5, 5);
 		gbc_lblSaluto.gridx = 0;
 		gbc_lblSaluto.gridy = 0;
 		add(lblSaluto, gbc_lblSaluto);
 		btnLogOut = new JButton("");
		//btnLogOut.setIcon(new ImageIcon(SalutoPanel.class.getResource("/icons/logout-icon.png")));
 		btnLogOut.setToolTipText("Logout");
 		btnLogOut.setActionCommand("Logout");
 		GridBagConstraints gbc_btnLogOut = new GridBagConstraints();
 		gbc_btnLogOut.gridheight = 2;
 		gbc_btnLogOut.fill = GridBagConstraints.BOTH;
 		gbc_btnLogOut.gridx = 6;
 		gbc_btnLogOut.gridy = 0;
 		add(btnLogOut, gbc_btnLogOut);
 		btnGestioneOrdine = new JButton("I tuoi ordini");
		//btnGestioneOrdine.setIcon(new ImageIcon(SalutoPanel.class.getResource("/icons/order-history.png")));
 		GridBagConstraints gbc_btnGestioneOrdine = new GridBagConstraints();
 		gbc_btnGestioneOrdine.gridheight = 2;
 		gbc_btnGestioneOrdine.fill = GridBagConstraints.BOTH;
 		gbc_btnGestioneOrdine.insets = new Insets(0, 0, 0, 5);
 		gbc_btnGestioneOrdine.gridx = 4;
 		gbc_btnGestioneOrdine.gridy = 0;
 		add(btnGestioneOrdine, gbc_btnGestioneOrdine);
 		btnGestioneOrdine.addActionListener(logController);
 		btnGestioneOrdine.setCursor(Cursor
 				.getPredefinedCursor(Cursor.HAND_CURSOR));
 
 		btnCarrello = new JButton("Carrello");
		//btnCarrello.setIcon(new ImageIcon(SalutoPanel.class.getResource("/icons/img_icon.gif.png")));
 		btnCarrello.setBackground(UIManager.getColor("Button.background"));
 		GridBagConstraints gbc_btnCarrello = new GridBagConstraints();
 		gbc_btnCarrello.gridheight = 2;
 		gbc_btnCarrello.fill = GridBagConstraints.BOTH;
 		gbc_btnCarrello.insets = new Insets(0, 0, 0, 5);
 		gbc_btnCarrello.gridx = 5;
 		gbc_btnCarrello.gridy = 0;
 		add(btnCarrello, gbc_btnCarrello);
 		btnCarrello.addActionListener(logController);
 		btnLogOut.addActionListener(logController);
 
 		btnCarrello.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		btnLogOut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 
 		separator = new JSeparator();
 		separator.setOrientation(SwingConstants.VERTICAL);
 		GridBagConstraints gbc_separator = new GridBagConstraints();
 		gbc_separator.gridheight = 2;
 		gbc_separator.insets = new Insets(0, 0, 0, 5);
 		gbc_separator.gridx = 3;
 		gbc_separator.gridy = 0;
 		add(separator, gbc_separator);
 	}
 
 	/**
 	 * @return the nome
 	 * @uml.property name="nome"
 	 */
 	public String getNome() {
 		return nome;
 	}
 
 	/**
 	 * @param nome
 	 *            the nome to set
 	 * @uml.property name="nome"
 	 */
 	public void setNome(String nome) {
 		this.nome = nome;
 	}
 
 	/**
 	 * @return the lblSaluto
 	 * @uml.property name="lblSaluto"
 	 */
 	public JLabel getLblSaluto() {
 		return lblSaluto;
 	}
 
 	/**
 	 * @param lblSaluto
 	 *            the lblSaluto to set
 	 * @uml.property name="lblSaluto"
 	 */
 	public void setLblSaluto(JLabel lblSaluto) {
 		this.lblSaluto = lblSaluto;
 	}
 
 	/**
 	 * @return the panel
 	 * @uml.property name="panel"
 	 */
 	public LogPanel getPanel() {
 		return panel;
 	}
 
 	/**
 	 * @param panel
 	 *            the panel to set
 	 * @uml.property name="panel"
 	 */
 	public void setPanel(LogPanel panel) {
 		this.panel = panel;
 	}
 
 	/**
 	 * @uml.property name="logController1"
 	 * @uml.associationEnd
 	 */
 	private LogController logController1;
 
 	/**
 	 * Getter of the property <tt>logController1</tt>
 	 * 
 	 * @return Returns the logController1.
 	 * @uml.property name="logController1"
 	 */
 	public LogController getLogController1() {
 		return logController1;
 	}
 
 	/**
 	 * Setter of the property <tt>logController1</tt>
 	 * 
 	 * @param logController1
 	 *            The logController1 to set.
 	 * @uml.property name="logController1"
 	 */
 	public void setLogController1(LogController logController1) {
 		this.logController1 = logController1;
 	}
 
 	/**
 	 * @uml.property name="logPanel"
 	 * @uml.associationEnd
 	 */
 	private LogPanel logPanel;
 	private JButton btnCarrello;
 	private JSeparator separator;
 
 	/**
 	 * Getter of the property <tt>logPanel</tt>
 	 * 
 	 * @return Returns the logPanel.
 	 * @uml.property name="logPanel"
 	 */
 	public LogPanel getLogPanel() {
 		return logPanel;
 	}
 
 	/**
 	 * Setter of the property <tt>logPanel</tt>
 	 * 
 	 * @param logPanel
 	 *            The logPanel to set.
 	 * @uml.property name="logPanel"
 	 */
 	public void setLogPanel(LogPanel logPanel) {
 		this.logPanel = logPanel;
 	}
 
 }
