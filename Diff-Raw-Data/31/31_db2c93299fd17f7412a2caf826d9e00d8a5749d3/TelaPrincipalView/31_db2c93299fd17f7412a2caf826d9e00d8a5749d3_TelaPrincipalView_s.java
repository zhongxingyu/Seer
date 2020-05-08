 package views;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.UIManager;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import java.awt.GridLayout;
 import javax.swing.JButton;
 import javax.swing.JTextArea;
 import java.awt.Font;
 
 public class TelaPrincipalView extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	// VAR QUE REPRESENTA QUE EST NA TELA INICIAL:
 	public boolean telaInicio = true;
 
 	// ATRIBUTOS DO MENU PRINCIPAL:
 	public JMenuBar menuBar = new JMenuBar();
 	public JMenu mnNovoCrammer = new JMenu("Novo Crammer");
 	public JMenuItem mntmx = new JMenuItem("2x2");
 	public JMenuItem mntmx_1 = new JMenuItem("3x3");
 	public JMenu mnSobre = new JMenu("Sobre");
 	public JMenuItem mntmx_2 = new JMenuItem("Desenvolvedores");
 	public JMenuItem mntmx_3 = new JMenuItem("Teorema de Crammer");
 
 	// PAINIS:
 	public JPanel panelPrincipal = new JPanel();
 	public JPanel panelInicio = new JPanel();
 	public JPanel panelCrammer2x2 = new JPanel();
 	public JPanel panelCrammer3x3 = new JPanel();
 
 	// CAMPOS DE TEXTO PARA O CRAMMER:
 	public JTextField tfX1 = new JTextField();
 	public JTextField tfX2 = new JTextField();
 	public JTextField tfX3 = new JTextField();
 	public JTextField tfY1 = new JTextField();
 	public JTextField tfY2 = new JTextField();
 	public JTextField tfY3 = new JTextField();
 	public JTextField tfZ1 = new JTextField();
 	public JTextField tfZ2 = new JTextField();
 	public JTextField tfZ3 = new JTextField();
 	public JTextField tfI1 = new JTextField();
 	public JTextField tfI2 = new JTextField();
 	public JTextField tfI3 = new JTextField();
 
 	// CAMPOS DE TEXTO COM RESULTADOS:
 	public JTextField tfDeltaX = new JTextField();
 	public JTextField tfDeltaY = new JTextField();
 	public JTextField tfDeltaZ = new JTextField();
 	public JTextField tfDeterminanteSistema = new JTextField();
 
 	// TEXTOS E LEGENDAS:
 	private JTextArea txtrInfoCrammer = new JTextArea();
 	private JLabel lblResultadoCrammer = new JLabel();
 	private JLabel lblTipoTabela = new JLabel("");
 	private JLabel lblI = new JLabel("Independentes", SwingConstants.CENTER);
 	private JLabel lblY = new JLabel("Y", JLabel.CENTER);
 	private JLabel lblX = new JLabel("X", JLabel.CENTER);
 	private JLabel lblZ = new JLabel("Z", JLabel.CENTER);
 
 	// BOTES:
 	private JButton btnCalcularCrammer2x2 = new JButton();
 	private JButton btnCalcularCrammer3x3 = new JButton();
 	private JButton btnLimparCrammer = new JButton();
 
 	public TelaPrincipalView() {
 
 		// INFO GERAL:
 		super("Teorema de Crammer");
 		tfY1.setColumns(10);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// CENTRALIZA NA TELA ESTA JANELA:
 		Dimension screenSize = new Dimension(Toolkit.getDefaultToolkit()
 				.getScreenSize());
 		setPreferredSize(new Dimension(600, 600));
 		int wdwLeft = screenSize.width / 2 - 300;
 		int wdwTop = screenSize.height / 2 - 300;
 		pack();
 		setLocation(wdwLeft, wdwTop);
 
 		setResizable(false);
 
 		// ICONE DO JFRAME:
 		BufferedImage image = null;
 		try {
 			image = ImageIO.read(getClass()
 					.getResource("/assets/img/icone.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		setIconImage(image);
 
 		// MONTA O MENU PRINCIPAL:
 		montaMenuPrincipal();
 
 		// MONTA O PAINEL PRINCIPAL:
 		montaPainelPrincipal();
 
 		// MONTA O PAINEL INCIO:
 		montaPainelInicio();
 
 		// MONTATEXTO INFO CRAMMER:
 		this.txtrInfoCrammer.setFont(new Font("Monospaced", Font.PLAIN, 11));
 		this.txtrInfoCrammer.setBackground(UIManager
 				.getColor("Button.background"));
 		this.txtrInfoCrammer.setEditable(false);
 		this.txtrInfoCrammer.setLineWrap(true);
 		this.txtrInfoCrammer
 				.setText("* Informe somente os num\u00E9ros, caso seja um n\u00FAmero negativo informe o sinal de menos na frente do n\u00FAmero. \r\nExemplo: -7 (sem espa\u00E7os)\r\n\r\n* S\u00E3o aceitos n\u00FAmeros decimais, caso queira informar uma fra\u00E7\u00E3o\r\ninforme em decimal.\r\nExemplo 1/5 = 0.2 (somente ponto)\r\n\r\n* Caso algum campo esteja em branco, se auto assumir\u00E1 como 1.");
 		this.txtrInfoCrammer.setBounds(24, 40, 546, 160);
 
 		setVisible(true);
 	}
 
 	// MONTA O MENU PRINCIPAL:
 	public void montaMenuPrincipal() {
 		this.mnNovoCrammer.add(this.mntmx);
 		this.mnNovoCrammer.add(this.mntmx_1);
 		this.mnSobre.add(this.mntmx_2);
 		this.mnSobre.add(this.mntmx_3);
 		this.menuBar.add(this.mnNovoCrammer);
 		this.menuBar.add(this.mnSobre);
 		setJMenuBar(this.menuBar);
 
 	}
 
 	public void montaPainelPrincipal() {
 		getContentPane().setLayout(new BorderLayout(0, 0));
 
 		// ROLAGEM NA TELA:
 		JScrollPane scrollPane = new JScrollPane(this.panelPrincipal);
 		getContentPane().add(scrollPane);
 		this.panelPrincipal.setLayout(new BorderLayout(0, 0));
 	}
 
 	public void montaPainelInicio() {
 		this.panelInicio.setPreferredSize(new Dimension(500, 500));
 		this.panelInicio.setMinimumSize(new Dimension(500, 500));
 		this.panelInicio.add(new JLabel(new ImageIcon(this.getClass()
 				.getResource("/assets/img/telaInicio.jpg"))));
 
 		this.panelPrincipal.add(this.panelInicio);
 	}
 
 	// MONTA TELA CRAMMER 2X2:
 	public void montaPainelCrammer2x2() {
 
 		// INFORMA QUE SAIU DA TELA INICIAL:
 		this.telaInicio = false;
 
 		this.panelCrammer2x2.setLayout(null);
 		this.lblTipoTabela.setBounds(24, 0, 155, 45);
 
 		this.lblTipoTabela.setText("Tabela (Crammer 2x2):");
 		this.panelCrammer2x2.add(this.lblTipoTabela);
 
 		// TABELA CRAMMER 2X2 XY:
 		JPanel panelTabelaCrammerXY = new JPanel();
 		panelTabelaCrammerXY.setBounds(24, 220, 304, 99);
 
 		this.panelCrammer2x2.add(panelTabelaCrammerXY);
 		panelTabelaCrammerXY.setLayout(new GridLayout(3, 2, 0, 0));
 
 		panelTabelaCrammerXY.add(this.lblX);
 		panelTabelaCrammerXY.add(this.lblY);
 		panelTabelaCrammerXY.add(this.tfX1);
 		panelTabelaCrammerXY.add(this.tfY1);
 		panelTabelaCrammerXY.add(this.tfX2);
 		panelTabelaCrammerXY.add(this.tfY2);
 
 		// TABELA INDEPENDENTES:
 		JPanel panelTabelaIndependentesXY = new JPanel();
 		panelTabelaIndependentesXY.setBounds(390, 220, 180, 99);
 
 		this.panelCrammer2x2.add(panelTabelaIndependentesXY);
 		panelTabelaIndependentesXY.setLayout(new GridLayout(3, 1, 0, 0));
 
 		panelTabelaIndependentesXY.add(this.lblI);
 		panelTabelaIndependentesXY.add(this.tfI1);
 		panelTabelaIndependentesXY.add(this.tfI2);
 
 		this.tfI2.setHorizontalAlignment(JTextField.CENTER);
 		this.tfI1.setHorizontalAlignment(JTextField.CENTER);
 		this.tfY2.setHorizontalAlignment(JTextField.CENTER);
 		this.tfX2.setHorizontalAlignment(JTextField.CENTER);
 		this.tfX1.setHorizontalAlignment(JTextField.CENTER);
 		this.tfY1.setHorizontalAlignment(JTextField.CENTER);
 
 		this.btnCalcularCrammer2x2.setBounds(24, 350, 162, 23);
 		this.btnLimparCrammer.setBounds(215, 350, 162, 23);
 
 		// BTN CRAMMER:
 		this.btnCalcularCrammer2x2.setText("Calcular Crammer 2x2");
 		this.btnLimparCrammer.setText("Limpar Crammer");
 		this.panelCrammer2x2.add(this.btnCalcularCrammer2x2);
 		this.panelCrammer2x2.add(this.btnLimparCrammer);
 
 		// SINAL DE IGUAL E INFO:
 		JLabel lblIgual = new JLabel("=", JLabel.CENTER);
 		lblIgual.setBounds(338, 260, 40, 14);
 		JLabel lblIgual2 = new JLabel("=", SwingConstants.CENTER);
 		lblIgual2.setBounds(338, 293, 40, 14);
 		this.panelCrammer2x2.add(lblIgual);
 		this.panelCrammer2x2.add(lblIgual2);
 		this.panelCrammer2x2.add(this.txtrInfoCrammer);
 
 		this.lblResultadoCrammer.setText("Resultado Crammer 2x2:");
 		this.lblResultadoCrammer.setBounds(24, 400, 155, 45);
 		this.panelCrammer2x2.add(this.lblResultadoCrammer);
 
 		// RESULTADOS:
 		JPanel panelTabelaResultadoCrammer2x2 = new JPanel();
 		panelTabelaResultadoCrammer2x2.setBounds(24, 450, 546, 65);
 		this.panelCrammer2x2.add(panelTabelaResultadoCrammer2x2);
 		panelTabelaResultadoCrammer2x2.setLayout(new GridLayout(2, 3, 0, 0));
 
 		JLabel lblDeltaX = new JLabel("Delta X", SwingConstants.CENTER);
 		panelTabelaResultadoCrammer2x2.add(lblDeltaX);
 
 		JLabel lblDeltaY = new JLabel("Delta Y", SwingConstants.CENTER);
 		panelTabelaResultadoCrammer2x2.add(lblDeltaY);
 
 		JLabel lblDeterminanteDoSistema = new JLabel("Determinante do Sistema",
 				SwingConstants.CENTER);
 		panelTabelaResultadoCrammer2x2.add(lblDeterminanteDoSistema);
 
 		this.tfDeltaX.setEnabled(false);
 		this.tfDeltaX.setEditable(false);
 		this.tfDeltaX.setHorizontalAlignment(JTextField.CENTER);
 		panelTabelaResultadoCrammer2x2.add(this.tfDeltaX);
 
 		this.tfDeltaY.setEnabled(false);
 		this.tfDeltaY.setEditable(false);
 		this.tfDeltaY.setHorizontalAlignment(JTextField.CENTER);
 		panelTabelaResultadoCrammer2x2.add(this.tfDeltaY);
 
 		this.tfDeterminanteSistema.setEnabled(false);
 		this.tfDeterminanteSistema.setEditable(false);
 		this.tfDeterminanteSistema.setHorizontalAlignment(JTextField.CENTER);
 		panelTabelaResultadoCrammer2x2.add(this.tfDeterminanteSistema);
 
 		this.panelPrincipal.add(this.panelCrammer2x2);
 		this.panelPrincipal.invalidate();
 		this.panelPrincipal.validate();
 		this.panelPrincipal.repaint();
 	}
 
 	// MONTA TELA CRAMMER 3X3:
 	public void montaPainelCrammer3x3() {
 
 		// INFORMA QUE SAIU DA TELA INICIAL:
 		this.telaInicio = false;
 
 		this.panelCrammer3x3.setLayout(null);
 		this.lblTipoTabela.setBounds(24, 0, 155, 45);
 
 		this.lblTipoTabela.setText("Tabela (Crammer 3x3):");
 		this.panelCrammer3x3.add(this.lblTipoTabela);
 
 		// TABELA CRAMMER 3X3 XYZ:
 		JPanel panelTabelaCrammerXYZ = new JPanel();
 		panelTabelaCrammerXYZ.setBounds(24, 220, 304, 99);
 
 		this.panelCrammer3x3.add(panelTabelaCrammerXYZ);
 		panelTabelaCrammerXYZ.setLayout(new GridLayout(4, 3, 0, 0));
 
 		panelTabelaCrammerXYZ.add(this.lblX);
 		panelTabelaCrammerXYZ.add(this.lblY);
 		panelTabelaCrammerXYZ.add(this.lblZ);
 		panelTabelaCrammerXYZ.add(this.tfX1);
 		panelTabelaCrammerXYZ.add(this.tfY1);
 		panelTabelaCrammerXYZ.add(this.tfZ1);
 		panelTabelaCrammerXYZ.add(this.tfX2);
 		panelTabelaCrammerXYZ.add(this.tfX2);
 		panelTabelaCrammerXYZ.add(this.tfY2);
 		panelTabelaCrammerXYZ.add(this.tfZ2);
		panelTabelaCrammerXYZ.add(this.tfZ3);
		panelTabelaCrammerXYZ.add(this.tfY3);
 		panelTabelaCrammerXYZ.add(this.tfX3);
 
 		// TABELA INDEPENDENTES:
 		JPanel panelTabelaIndependentesXYZ = new JPanel();
 		panelTabelaIndependentesXYZ.setBounds(390, 220, 180, 99);
 
 		this.panelCrammer3x3.add(panelTabelaIndependentesXYZ);
 		panelTabelaIndependentesXYZ.setLayout(new GridLayout(4, 1, 0, 0));
 
 		panelTabelaIndependentesXYZ.add(this.lblI);
 		panelTabelaIndependentesXYZ.add(this.tfI1);
 		panelTabelaIndependentesXYZ.add(this.tfI2);
 		panelTabelaIndependentesXYZ.add(this.tfI3);
 
 		this.tfI1.setHorizontalAlignment(JTextField.CENTER);
 		this.tfI3.setHorizontalAlignment(JTextField.CENTER);
 		this.tfI2.setHorizontalAlignment(JTextField.CENTER);
 
 		this.tfX1.setHorizontalAlignment(JTextField.CENTER);
 		this.tfX2.setHorizontalAlignment(JTextField.CENTER);
 		this.tfX3.setHorizontalAlignment(JTextField.CENTER);

 		this.tfY1.setHorizontalAlignment(JTextField.CENTER);
 		this.tfY2.setHorizontalAlignment(JTextField.CENTER);
 		this.tfY3.setHorizontalAlignment(JTextField.CENTER);

 		this.tfZ1.setHorizontalAlignment(JTextField.CENTER);
 		this.tfZ2.setHorizontalAlignment(JTextField.CENTER);
 		this.tfZ3.setHorizontalAlignment(JTextField.CENTER);
 
 		this.btnCalcularCrammer3x3.setBounds(24, 350, 162, 23);
 		this.btnLimparCrammer.setBounds(215, 350, 162, 23);
 
 		// BTN CRAMMER:
 		this.btnCalcularCrammer3x3.setText("Calcular Crammer 3x3");
 		this.btnLimparCrammer.setText("Limpar Crammer");
 		this.panelCrammer3x3.add(this.btnCalcularCrammer3x3);
 		this.panelCrammer3x3.add(this.btnLimparCrammer);
 
 		// SINAL DE IGUAL E INFO:
 		JLabel lblIgual = new JLabel("=", JLabel.CENTER);
 		lblIgual.setBounds(338, 250, 40, 14);
 		JLabel lblIgual2 = new JLabel("=", SwingConstants.CENTER);
 		lblIgual2.setBounds(338, 275, 40, 14);
 		JLabel lblIgual3 = new JLabel("=", SwingConstants.CENTER);
 		lblIgual3.setBounds(338, 300, 40, 14);
 		this.panelCrammer3x3.add(lblIgual);
 		this.panelCrammer3x3.add(lblIgual2);
 		this.panelCrammer3x3.add(lblIgual3);
 		this.lblResultadoCrammer.setText("Resultado Crammer 3x3:");
 		this.lblResultadoCrammer.setBounds(24, 400, 155, 45);
 		this.panelCrammer3x3.add(this.lblResultadoCrammer);
 		this.panelCrammer3x3.add(this.txtrInfoCrammer);
 
 		// RESULTADOS:
 		JPanel panelTabelaResultadoCrammer3x3 = new JPanel();
 		panelTabelaResultadoCrammer3x3.setBounds(24, 450, 546, 65);
 		this.panelCrammer3x3.add(panelTabelaResultadoCrammer3x3);
 		panelTabelaResultadoCrammer3x3.setLayout(new GridLayout(2, 4, 0, 0));
 
 		JLabel lblDeltaX = new JLabel("Delta X", SwingConstants.CENTER);
 		panelTabelaResultadoCrammer3x3.add(lblDeltaX);
 
 		JLabel lblDeltaY = new JLabel("Delta Y", SwingConstants.CENTER);
 		panelTabelaResultadoCrammer3x3.add(lblDeltaY);
 
 		JLabel lblDeltaZ = new JLabel("Delta Z", SwingConstants.CENTER);
 		panelTabelaResultadoCrammer3x3.add(lblDeltaZ);
 
 		JLabel lblDeterminanteDoSistema = new JLabel("D. do Sistema",
 				SwingConstants.CENTER);
 		panelTabelaResultadoCrammer3x3.add(lblDeterminanteDoSistema);
 
 		this.tfDeltaX.setEnabled(false);
 		this.tfDeltaX.setEditable(false);
 		this.tfDeltaX.setHorizontalAlignment(JTextField.CENTER);
 		panelTabelaResultadoCrammer3x3.add(this.tfDeltaX);
 
 		this.tfDeltaY.setEnabled(false);
 		this.tfDeltaY.setEditable(false);
 		this.tfDeltaY.setHorizontalAlignment(JTextField.CENTER);
 		panelTabelaResultadoCrammer3x3.add(this.tfDeltaY);
 
 		this.tfDeltaZ.setEnabled(false);
 		this.tfDeltaZ.setEditable(false);
 		this.tfDeltaZ.setHorizontalAlignment(JTextField.CENTER);
 		panelTabelaResultadoCrammer3x3.add(this.tfDeltaZ);
 
 		this.tfDeterminanteSistema.setEnabled(false);
 		this.tfDeterminanteSistema.setEditable(false);
 		this.tfDeterminanteSistema.setHorizontalAlignment(JTextField.CENTER);
 		panelTabelaResultadoCrammer3x3.add(this.tfDeterminanteSistema);
 		this.panelPrincipal.add(this.panelCrammer3x3);
 		this.panelPrincipal.invalidate();
 		this.panelPrincipal.validate();
 		this.panelPrincipal.repaint();
 	}
 
 	// ADD OUVINTE AO MENU Crammer2x2:
 	public void addOuvinteMenu2x2(ActionListener ouvinte) {
 		this.mntmx.addActionListener(ouvinte);
 	}
 
 	// ADD OUVINTE AO MENU Crammer3x3:
 	public void addOuvinteMenu3x3(ActionListener ouvinte) {
 		this.mntmx_1.addActionListener(ouvinte);
 	}
 
 	// ADD OUVINTE AO MENU DESENVOLVEDORES:
 	public void addOuvinteMenuDesenvolvedores(ActionListener ouvinte) {
 		this.mntmx_2.addActionListener(ouvinte);
 	}
 
 	// ADD OUVINTE AO MENU CRAMMER:
 	public void addOuvinteMenuCrammer(ActionListener ouvinte) {
 		this.mntmx_3.addActionListener(ouvinte);
 	}
 
 	// ADD OUVINTE AO BTN LIMPAR CRAMMER:
 	public void addOuvinteBtnLimparCrammer(ActionListener ouvinte) {
 		this.btnLimparCrammer.addActionListener(ouvinte);
 	}
 
 	// ADD OUVINTE AO BTN CALCULAR CRAMMER 2x2:
 	public void addOuvinteBtnCalcular2x2(ActionListener ouvinte) {
 		this.btnCalcularCrammer2x2.addActionListener(ouvinte);
 	}
 
 	// ADD OUVINTE AO BTN CALCULAR CRAMMER 3x3:
 	public void addOuvinteBtnCalcular3x3(ActionListener ouvinte) {
 		this.btnCalcularCrammer3x3.addActionListener(ouvinte);
 	}
 }
