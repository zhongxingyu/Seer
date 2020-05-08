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
 
 import java.awt.Color;
 import java.awt.SystemColor;
 import java.awt.Window.Type;
 import java.awt.Toolkit;
 
 public class frmDesenvolvedores extends JFrame {
 
 	private JPanel contentPane;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					frmDesenvolvedores frame = new frmDesenvolvedores();
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
 	public frmDesenvolvedores() {
 		SwingUtil.lookWindows(this);
 		setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\Andr\u00E9\\Desktop\\TCC\\agt_home.png"));
 		setType(Type.UTILITY);
 		setTitle("Desenvolvedores");
 		setBounds(100, 100, 430, 220);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JLabel lblDesenvolvedores = new JLabel("Desenvolvedores");
 		lblDesenvolvedores.setFont(new Font("Arial", Font.BOLD, 20));
 		lblDesenvolvedores.setBounds(136, 11, 180, 41);
 		contentPane.add(lblDesenvolvedores);
 		
 		JTextPane txtpnSaniSistema = new JTextPane();
 		txtpnSaniSistema.setBackground(SystemColor.control);
 		txtpnSaniSistema.setEditable(false);
		txtpnSaniSistema.setText("SANI - Sistema Administrador de Neg\u00F3cios Imobili\u00E1rios\r\n\r\nProgramador: Andr\u00E9 Eduardo\r\nDesigner: Andr\u00E9 Eduardo, Anne Helen, Caroline Chaves e Victor Ramos\r\nBanco de Dados: Andr\u00E9 Eduardo, Anne Helen, Caroline Chaves e Victor Ramos\r\nDocumenta\u00E7\u00E3o T\u00E9cnica: Anne Helen, Caroline Chaves e Victor Ramos\r\n\r\nAgradecimentos Especias: Tamires Pereira e Caroline Ara\u00FAjo");
 		txtpnSaniSistema.setBounds(10, 63, 399, 128);
 		contentPane.add(txtpnSaniSistema);
 		
 		setLocationRelativeTo(null);
 	}
 }
