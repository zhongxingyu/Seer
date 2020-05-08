 package br.com.sani.telas;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JLabel;
 import java.awt.Font;
 import javax.swing.JTextPane;
 
 import br.com.sani.util.SwingUtil;
 
 import java.awt.SystemColor;
 import java.awt.Window.Type;
 import java.awt.Toolkit;
 
 public class frmAbout extends JFrame {
 
 	private JPanel contentPane;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					frmAbout frame = new frmAbout();
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
 	public frmAbout() {
 		SwingUtil.lookWindows(this);
 		setType(Type.UTILITY);
 		setIconImage(Toolkit.getDefaultToolkit().getImage(frmAbout.class.getResource("/br/com/images/home_badge.png")));
 		setTitle("Ajuda - Sobre...");
		setBounds(100, 100, 400, 221);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JLabel lblSobreOSani = new JLabel("Sobre o SANI");
 		lblSobreOSani.setFont(new Font("Arial", Font.BOLD, 18));
 		lblSobreOSani.setBounds(114, 11, 138, 14);
 		contentPane.add(lblSobreOSani);
 		
 		JTextPane txtpnSaniSistema = new JTextPane();
 		txtpnSaniSistema.setEditable(false);
 		txtpnSaniSistema.setBackground(SystemColor.control);
		txtpnSaniSistema.setText("SANI - Sistema Administrador de Neg\u00F3cios Imobili\u00E1rios foi desenvolvido para ser voltado ao mercado Imobili\u00E1rio, abastecendo as Pequenas, M\u00E9dias e Grandes Empresas da \u00E1rea, com uma variedade de fun\u00E7\u00F5es. Adaptado para qualquer ambiente, sendo ele Windows, Linux ou Mac OS, sendo necess\u00E1rio apenas de um interpretador JAVA.\r\n\r\nSANI foi desenvolvido em linguagem JAVA e projetado para os Banco de Dados SQL Server.\r\n");
		txtpnSaniSistema.setBounds(10, 61, 368, 119);
 		contentPane.add(txtpnSaniSistema);
 		
 		JLabel lblVersion = new JLabel("Vers\u00E3o BETA v0.3.4");
 		lblVersion.setFont(new Font("Tahoma", Font.ITALIC, 11));
 		lblVersion.setBounds(124, 36, 109, 14);
 		contentPane.add(lblVersion);
 	}
 
 }
