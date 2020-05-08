 package br.com.sani.telas;
 
 import java.awt.EventQueue;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.border.EmptyBorder;
 
 import br.com.sani.util.SwingUtil;
 import br.com.sani.util.Wallpaper;
 
 
 public class frmMenuPrincipalFuncionario extends JFrame {
 
 	private JPanel contentPane;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					frmMenuPrincipalAdmin frame = new frmMenuPrincipalAdmin();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Create the frame.
 	 */
 	public frmMenuPrincipalFuncionario() {
 		setExtendedState(JFrame.MAXIMIZED_BOTH);
 		SwingUtil.lookWindows(this);
 		setIconImage(Toolkit.getDefaultToolkit().getImage(frmMenuPrincipalAdmin.class.getResource("/br/com/images/home_badge.png")));
 		setTitle("FUNCION\u00C1RIO - Sistema Administrador de Neg\u00F3cios Imobili\u00E1rios - v0.3.4");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 675, 450);
 		contentPane = new Wallpaper();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		setVisible(false);
 		
 		JMenuBar menuBarArquivo = new JMenuBar();
 		setJMenuBar(menuBarArquivo);
 		
 		JMenu mnArquivo = new JMenu("Arquivo");
 		menuBarArquivo.add(mnArquivo);
 		
 		JMenuItem mntmImportar = new JMenuItem("Importar...");
 		mntmImportar.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JOptionPane.showMessageDialog(null, "Funo no liberada\n" +
 				"Funo estar disponvel em breve!");
 			}
 		});
 		mnArquivo.add(mntmImportar);
 		
 		JMenuItem mntmExportar = new JMenuItem("Exportar...");
 		mntmExportar.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JOptionPane.showMessageDialog(null, "Funo no liberada\n" +
 				"Funo estar disponvel em breve!");
 			}
 		});
 		mnArquivo.add(mntmExportar);
 		
 		JMenuItem mntmImprimir = new JMenuItem("Imprimir");
 		mntmImprimir.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JOptionPane.showMessageDialog(null, "Funo no liberada\n" +
 				"Funo estar disponvel em breve!");
 			}
 		});
 		mnArquivo.add(mntmImprimir);
 		
 		JSeparator separator = new JSeparator();
 		mnArquivo.add(separator);
 		
 		JMenuItem mntmSair = new JMenuItem("Sair");
 		mntmSair.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			System.exit(0);
 			}
 		});
 		mnArquivo.add(mntmSair);
 		
 		JMenu mnCadastro = new JMenu("Cadastro");
 		menuBarArquivo.add(mnCadastro);
 		
 		JMenuItem mntmFuncionarios = new JMenuItem("Funcion\u00E1rios");
 		mntmFuncionarios.setEnabled(false);
 		mntmFuncionarios.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			frmCadastroFuncionario frameCadFun = null;
 			try {
 				frameCadFun = new frmCadastroFuncionario();
 			} catch (Throwable e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			frameCadFun.setVisible(true);
 			}
 		});
 		
 		JMenuItem mntmPropriedades = new JMenuItem("Propriedades");
 		mntmPropriedades.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			frmCadastroPropriedade frameCadProp = new frmCadastroPropriedade();
 			frameCadProp.setVisible(true);
 			}
 		});
 		mnCadastro.add(mntmPropriedades);
 		
 		JMenu mnClientes = new JMenu("Clientes");
 		mnCadastro.add(mnClientes);
 		
 		JMenuItem mntmClienteComprador = new JMenuItem("Cliente Comprador");
 		mntmClienteComprador.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
			frmCadastroClienteCompradorFis frameCCC = new frmCadastroClienteCompradorFis(null);
 			frameCCC.setVisible(true);
 			}
 		});
 		mnClientes.add(mntmClienteComprador);
 		
 		JMenuItem mntmClienteProprietario = new JMenuItem("Cliente Propriet\u00E1rio");
 		mntmClienteProprietario.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			frmCadastroClienteProprietario frameCadCliProp;
 			try {
 				frameCadCliProp = new frmCadastroClienteProprietario();
 				frameCadCliProp.setVisible(true);
 			} catch (Throwable e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			}
 		});
 		mnClientes.add(mntmClienteProprietario);
 		mnCadastro.add(mntmFuncionarios);
 				
 		JMenuItem mntmMetas = new JMenuItem("Metas");
 		mntmMetas.setEnabled(false);
 		mntmMetas.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			frmCadastroMetas frameCadMetas = new frmCadastroMetas();
 			frameCadMetas.setVisible(true);
 			}
 		});
 		mnCadastro.add(mntmMetas);
 		
 		JMenu mnConsulta = new JMenu("Consulta");
 		menuBarArquivo.add(mnConsulta);
 		
 		JMenuItem mntmPropriedadesConsulta = new JMenuItem("Propriedades");
 		mntmPropriedadesConsulta.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			frmConsultaPropriedade frameConsProp = new frmConsultaPropriedade();
 			frameConsProp.setVisible(true);
 			}
 		});
 		mnConsulta.add(mntmPropriedadesConsulta);
 		
 		JMenu mnClienteConsulta = new JMenu("Clientes");
 		mnConsulta.add(mnClienteConsulta);
 		
 		JMenuItem mntmFuncionariosConsulta = new JMenuItem("Funcion\u00E1rios");
 		mntmFuncionariosConsulta.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				frmConsultaFuncionario frameConsFun = new frmConsultaFuncionario();
 				frameConsFun.setVisible(true);
 			}
 		});
 		mnConsulta.add(mntmFuncionariosConsulta);
 		
 		JMenuItem mntmMetasConsulta = new JMenuItem("Metas");
 		mntmMetasConsulta.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			frmConsultaMetas frameConsMetas;
 			try {
 				frameConsMetas = new frmConsultaMetas();
 				frameConsMetas.setVisible(true);
 			} catch (Throwable e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			}
 		});
 		mnConsulta.add(mntmMetasConsulta);
 		
 		JMenu mnRelatorio = new JMenu("Relat\u00F3rio");
 		mnRelatorio.setEnabled(false);
 		menuBarArquivo.add(mnRelatorio);
 		
 		JMenuItem mntmEmitirRelatorio = new JMenuItem("Emitir Relat\u00F3rio...");
 		mnRelatorio.add(mntmEmitirRelatorio);
 		
 		JMenu mnAjuda = new JMenu("Ajuda");
 		menuBarArquivo.add(mnAjuda);
 		
 		JMenuItem mntmTutorialDoSistema = new JMenuItem("Tutorial do Sistema");
 		mntmTutorialDoSistema.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JOptionPane.showMessageDialog(null, "Funo no liberada\n" +
 				"Funo estar disponvel em breve!");
 			}
 		});
 		mnAjuda.add(mntmTutorialDoSistema);
 		
 		JMenuItem mntmDesenvolvedores = new JMenuItem("Desenvolvedores");
 		mntmDesenvolvedores.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			frmDesenvolvedores frameDev = new frmDesenvolvedores();
 			frameDev.setVisible(true);
 			
 			}
 		});
 		mnAjuda.add(mntmDesenvolvedores);
 		
 		JSeparator separator_1 = new JSeparator();
 		mnAjuda.add(separator_1);
 		
 		JMenuItem mntmSobre = new JMenuItem("Sobre...");
 		mntmSobre.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			frmAbout frameSobre = new frmAbout();
 			frameSobre.setVisible(true);
 			}
 		});
 		mnAjuda.add(mntmSobre);
 		
 	}
 
 }
 
