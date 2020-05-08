 package com.Shows.Presentation.View;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 import java.util.Set;
 
 import javax.swing.Box;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.border.EmptyBorder;
 
 import com.Shows.Data.DataMapper.HibernateUtil;
 import com.Shows.Presentation.Controller.FrontController;
 import com.Shows.TupleTypes.DadesEntrada;
 import com.Shows.TupleTypes.DadesRepresentacio;
 import com.Shows.TupleTypes.PosicioSeient;
 
 public class IniciView extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
	private static final int INICI = 0;
	private static final int ESPECTACLES = INICI + 1;
 	private static final int REPRESENTACIONS = ESPECTACLES + 1;
 	private static final int SEIENTS = REPRESENTACIONS + 1;
 	private static final int PAGAMENT = SEIENTS + 1;
 
 	private static final String[] flowNames = { "Inici", "Espectacles",
 			"Representacions", "Seleccionar seients", "Pagament" };
 
 	private FrontController frontController;
 	/**
 	 * Instancia de los Panels
 	 */
 	private EspectaclePanel espectaclePanel;
 	private RepresentacioPanel representacioPanel;
 	private SeientsPanel seientsPanel;
 	private PagamentPanel pagamentPanel;
 	private IniciPanel iniciPanel;
 
 	private JPanel contentPane;
 	private JPanel centerPanel = new JPanel();
 	private CardLayout card = new CardLayout();
 
 	private Box horizontalBox_1;
 	private JLabel stateLabel;
 
 	private Box horizontalBox;
 	private ArrayList<JLabel> navigationLabels;
 	private ArrayList<Box> navigationHorizontalBox;
 
 	private int width = 600;
 	private int heigth = 400;
 
 	private String espectacle = new String();
 	private String data = new String();
 	private String local = new String();
 	private String sessio = new String();
 	private String nombEspectadors = new String();
 	private String estrena = new String();
 	private String preuSeient = new String();
 	private String seients = new String();
 
 	/**
 	 * Create the frame.
 	 */
 	public IniciView(final FrontController frontController) {
 
 		this.frontController = frontController;
 
 		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
 
 		setBounds(dimension.width / 2 - width / 2, dimension.height / 2
 				- heigth / 2, width, heigth);
 
 		setResizable(false);
 
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent windowEvent) {
 				mostraAvis("Est segur que desitja tancar l'aplicaci? Es perdrn els canvis.");
 			}
 		});
 
 		Color backgroundColor = frontController.getBackgroundColor();
 
 		setBackground(backgroundColor);
 
 		setVisible(true);
 
 		contentPane = new JPanel();
 		contentPane.setBackground(backgroundColor);
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 
 		horizontalBox = Box.createHorizontalBox();
 		horizontalBox.setBorder(new EmptyBorder(10, 0, 10, 0));
 		contentPane.add(horizontalBox, BorderLayout.NORTH);
 
 		horizontalBox_1 = Box.createHorizontalBox();
 		horizontalBox_1.setAlignmentY(Component.CENTER_ALIGNMENT);
 		contentPane.add(horizontalBox_1, BorderLayout.CENTER);
 
 		stateLabel = new JLabel();
 		stateLabel.setVerticalTextPosition(SwingConstants.TOP);
 		stateLabel.setVerticalAlignment(SwingConstants.TOP);
 		stateLabel.setBorder(new EmptyBorder(10, 10, 0, 0));
 		stateLabel.setMaximumSize(new Dimension(160, 600));
 		stateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
 		horizontalBox_1.add(stateLabel);
 
 		espectaclePanel = new EspectaclePanel(frontController, this);
 		representacioPanel = new RepresentacioPanel(frontController, this);
 		seientsPanel = new SeientsPanel(frontController, this);
 		pagamentPanel = new PagamentPanel(frontController, this);
 		iniciPanel = new IniciPanel(frontController, this);
 
 		iniciPanel.setPreferredSize(new Dimension(0, 0));
 		iniciPanel.setMinimumSize(new Dimension(0, 0));
 		iniciPanel.setMaximumSize(new Dimension(0, 0));
 
 		espectaclePanel.setBackground(backgroundColor);
 		representacioPanel.setBackground(backgroundColor);
 		seientsPanel.setBackground(backgroundColor);
 		pagamentPanel.setBackground(backgroundColor);
 		iniciPanel.setBackground(backgroundColor);
 		centerPanel.setPreferredSize(new Dimension(0, 0));
 		centerPanel.setMinimumSize(new Dimension(0, 0));
 		centerPanel.setMaximumSize(new Dimension(600, 600));
 		horizontalBox_1.add(centerPanel);
 
 		centerPanel.setBackground(backgroundColor);
 		centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 
 		centerPanel.setLayout(card);
 		centerPanel.add(iniciPanel, flowNames[0]);
 		centerPanel.add(espectaclePanel, flowNames[1]);
 		centerPanel.add(representacioPanel, flowNames[2]);
 		centerPanel.add(seientsPanel, flowNames[3]);
 		centerPanel.add(pagamentPanel, flowNames[4]);
 
 		setContentPane(contentPane);
 
 		/**
 		 * Aadir los paneles para luego poder mostrarlos
 		 */
 
 		navigationLabels = new ArrayList<JLabel>(5);
 		navigationHorizontalBox = new ArrayList<Box>(5);
 
 		for (int i = 0; i < 5; i++) {
 			JLabel jLabel = new JLabel(flowNames[i]);
 
 			jLabel.setFont(new Font("Arial", Font.BOLD, 14));
 
 			Box hBox = Box.createHorizontalBox();
 
 			if (i != 0) {
 				JLabel indicatorLabel = new JLabel(">");
 				indicatorLabel.setFont(new Font("Arial", Font.BOLD, 14));
 				indicatorLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
 				hBox.add(indicatorLabel);
 			}
 			hBox.add(jLabel);
 			navigationLabels.add(jLabel);
 
 			navigationHorizontalBox.add(hBox);
 			jLabel.setEnabled(false);
 			jLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
 			hBox.setVisible(false);
 
 			jLabel.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mouseReleased(MouseEvent mouseEvent) {
 
 					JLabel jLabel = (JLabel) mouseEvent.getComponent();
 					if (jLabel.isEnabled())
 						setFlowState(navigationLabels.indexOf(jLabel), false);
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent mouseEvent) {
 
 					JLabel jLabel = (JLabel) mouseEvent.getComponent();
 					jLabel.setForeground(new Color(163, 184, 204));
 					if (jLabel.isEnabled())
 						IniciView.this
 								.setCursor(new Cursor(Cursor.HAND_CURSOR));
 				}
 
 				@Override
 				public void mouseExited(MouseEvent mouseEvent) {
 
 					JLabel jLabel = (JLabel) mouseEvent.getComponent();
 					jLabel.setForeground(Color.BLACK);
 					if (jLabel.isEnabled())
 						IniciView.this.setCursor(new Cursor(
 								Cursor.DEFAULT_CURSOR));
 				}
 			});
 
 			horizontalBox.add(hBox);
 		}
		setFlowState(INICI, false);
 	}
 
 	private void setFlowState(int flowState, boolean simple) {
 
 		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 		if (!simple)
 			for (int i = 0; i < 5; i++) {
 				navigationLabels.get(i).setEnabled((i < flowState));
 				navigationHorizontalBox.get(i).setVisible((i <= flowState));
 			}
 		else {
 			navigationLabels.get(0).setEnabled(true);
 		}
 
 		switch (flowState) {
		case INICI:
 			stateLabel.setText("");
 			stateLabel.setVisible(false);
 			break;
 
 		case ESPECTACLES:
 			stateLabel.setText("");
 			stateLabel.setVisible(false);
 			break;
 
 		case REPRESENTACIONS:
 			stateLabel.setVisible(true);
 			stateLabel.setText("<html><b>Espectacle: </b>" + espectacle
 					+ "<br><b>Data: </b>" + data + "</html>");
 			break;
 
 		case SEIENTS:
 			stateLabel.setVisible(!simple);
 			stateLabel.setText("<html><b>Espectacle: </b>" + espectacle
 					+ "<br><b>Data: </b>" + data + "<br><b>Local: </b>" + local
 					+ "<br><b>Sessi: </b>" + sessio
 					+ "<br><b>Espectadors: </b>" + nombEspectadors
 					+ "<br><b>Estrena: </b>" + estrena
 					+ "<br><b>Preu per seient: </b>" + preuSeient + "</html>");
 			break;
 
 		case PAGAMENT:
 			stateLabel.setVisible(true);
 			stateLabel.setText("<html><b>Espectacle: </b>" + espectacle
 					+ "<br><b>Data: </b>" + data + "<br><b>Local: </b>" + local
 					+ "<br><b>Sessi: </b>" + sessio
 					+ "<br><b>Espectadors: </b>" + nombEspectadors
 					+ "<br><b>Estrena: </b>" + estrena
 					+ "<br><b>Preu per seient: </b>" + preuSeient
 					+ "<br><b>Seients: </b>" + seients + "</html>");
 			break;
 
 		default:
 			break;
 		}
 
 		card.show(centerPanel, flowNames[flowState]);
 	}
 
 	public void setRepresentacionsString(final String espectacle,
 			final String data) {
 		this.espectacle = espectacle;
 		this.data = data;
 	}
 
 	public void setSeientsString(final String local, final String sessio,
 			final String nombEspectadors, final String estrena,
 			final String preuSeient) {
 		this.local = local;
 		this.sessio = sessio;
 		this.nombEspectadors = nombEspectadors;
 		this.estrena = estrena;
 		this.preuSeient = preuSeient;
 	}
 
 	public void setPagamentString(String seients) {
 		this.seients = seients;
 	}
 
	public void mostraInici() {
		setFlowState(INICI, false);
	}

 	public void mostraEspectacles(Set<String> espectacles) {
 
 		espectaclePanel.setEspectacleComboBox(espectacles);
 		setFlowState(ESPECTACLES, false);
 	}
 
 	public void mostraRepresentacions(Set<DadesRepresentacio> representacions,
 			boolean simple) {
 
 		representacioPanel.setInfo(representacions, simple);
 		setFlowState(REPRESENTACIONS, simple);
 	}
 
 	public void mostraOcupacio(Set<PosicioSeient> seients, boolean simple) {
 
 		seientsPanel.setSeients(seients, simple);
 		setFlowState(SEIENTS, simple);
 	}
 
 	public void mostraPreu(DadesEntrada dadesEntrada) {
 
 		pagamentPanel.setDadesEntrada(dadesEntrada);
 		setFlowState(PAGAMENT, false);
 	}
 
 	public void mostraPreuMoneda(Float preu) {
 
 		pagamentPanel.setPreu(preu);
 	}
 
 	public void mostraMissatge(String missatge) {
 
 		JOptionPane.showMessageDialog(this, missatge, "Informaci",
 				JOptionPane.WARNING_MESSAGE);
 	}
 
 	public void mostraAvisFi(String missatge) {
 
 		int confirmation = JOptionPane.showConfirmDialog(this, missatge,
 				"Informaci", JOptionPane.DEFAULT_OPTION,
 				JOptionPane.INFORMATION_MESSAGE);
 
 		if (confirmation == JOptionPane.OK_OPTION) {
 			frontController.PrFi();
 		}
 	}
 
 	public void mostraAvis(String missatge) {
 
 		int confirmation = JOptionPane.showConfirmDialog(this, missatge,
 				"Confirmaci", JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.QUESTION_MESSAGE);
 
 		if (confirmation == JOptionPane.OK_OPTION) {
			tancar();
 		}
 	}
 
 	public void tancar() {
 
 		try {
 			HibernateUtil.getSession().close();
 			finalize();
 			System.exit(0);
 		} catch (Throwable throwable) {
 			throwable.printStackTrace();
 		}
 	}
 }
