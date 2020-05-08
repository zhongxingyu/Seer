 package presentacion;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import java.awt.SystemColor;
 
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.SwingConstants;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 
 import logica.Coleccionista;
 import logica.Gestor;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 public class PantallaConsultarColeccionista extends JFrame {
 
 	private JPanel contentPane;
 	private JTextField txtId;
 
 	public PantallaConsultarColeccionista() {
 		setBackground(SystemColor.inactiveCaptionBorder);
 		setResizable(false);
 		setTitle("Consultar Coleccionista");
 		setBounds(100, 100, 311, 131);
 		contentPane = new JPanel();
 		contentPane.setBackground(SystemColor.inactiveCaptionBorder);
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JButton btnConsultar = new JButton("Consultar");
 		btnConsultar.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Coleccionista coleccionista = Gestor.consultarColeccionista( txtId.getText() );
 					PantallaActualizarColeccionista pantallaColeccionista = new PantallaActualizarColeccionista( coleccionista );
 					pantallaColeccionista.setVisible(true);
					setVisible(false);
 				} catch (Exception e1) {
 					JOptionPane.showMessageDialog( null, "No se ha podido encontrar por el id indicado");
 				}
 			}
 		});
 		btnConsultar.setBounds(206, 69, 89, 23);
 		contentPane.add(btnConsultar);
 		
		JLabel lblPorId = new JLabel("Id");
 		lblPorId.setDisplayedMnemonic('I');
 		lblPorId.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblPorId.setBounds(68, 26, 46, 14);
 		contentPane.add(lblPorId);
 		
 		txtId = new JTextField();
 		lblPorId.setLabelFor(txtId);
 		txtId.setBounds(124, 23, 117, 20);
 		contentPane.add(txtId);
 		txtId.setColumns(10);
 		
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 			}
 		});
 		btnCancel.setBounds(107, 69, 89, 23);
 		contentPane.add(btnCancel);
 	}
 }
