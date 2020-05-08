 package presentacion;
 
 import java.awt.SystemColor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.border.EmptyBorder;
 
 import logica.Gestor;
 
 public class PantallaCrearMecenazgo extends JFrame {
 
 	
 	private JPanel contentPane;
 	private JTextField txtFechaFinal;
 	private JTextField txtIdPintor;
 	private JTextField txtIdMecenas;
 	private JTextField txtFechaInicio;
 	private JLabel lblFechaInicio;
 	
 	private String idMecenas;
 
 	public PantallaCrearMecenazgo(String pid) {
 		
 		idMecenas = pid;
 		
 		setResizable(false);
 		setTitle("Crear Mecenazgo");
 		setBounds(100, 100, 336, 200);
 		contentPane = new JPanel();
 		contentPane.setBackground(SystemColor.inactiveCaptionBorder);
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JLabel lblFechaFinal = new JLabel("Fecha Final");
 		lblFechaFinal.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblFechaFinal.setBounds(45, 39, 105, 14);
 		contentPane.add(lblFechaFinal);
 		
 		JLabel lblIdPintor = new JLabel("Id Pintor");
 		lblIdPintor.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblIdPintor.setBounds(45, 64, 105, 14);
 		contentPane.add(lblIdPintor);
 		
 		JLabel lblIdMecenas = new JLabel("Id Mecenas");
 		lblIdMecenas.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblIdMecenas.setBounds(55, 89, 92, 14);
 		contentPane.add(lblIdMecenas);
 		
 		txtFechaFinal = new JTextField();
 		txtFechaFinal.setBounds(160, 36, 109, 20);
		txtFechaFinal.setText("yyyy-mm-dd");
 		contentPane.add(txtFechaFinal);
 		txtFechaFinal.setColumns(10);
 		
 		txtIdPintor = new JTextField();
 		txtIdPintor.setBounds(160, 61, 109, 20);
 		contentPane.add(txtIdPintor);
 		txtIdPintor.setColumns(10);
 		
 		txtIdMecenas = new JTextField();
 		txtIdMecenas.setBounds(160, 86, 109, 20);
 		contentPane.add(txtIdMecenas);
 		txtIdMecenas.setColumns(10);
 		txtIdMecenas.setText(idMecenas);
 		txtIdMecenas.setEnabled(false);
 		
 		JButton btnCrear = new JButton("Crear");
 		btnCrear.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Gestor.crearMecenazgo( txtFechaInicio.getText(), txtFechaFinal.getText(), txtIdPintor.getText(), txtIdMecenas.getText());
					JOptionPane.showMessageDialog( null, "El mecenazgo ha sido creado exitosamente!" );
					setVisible(false);
 				} catch (Exception e1) {
 					JOptionPane.showMessageDialog( null, "Hubo un error\nPor favor revise los datos ingresados");
 				}
 			}
 		});
 		btnCrear.setBounds(239, 127, 81, 23);
 		contentPane.add(btnCrear);
 		
 		JButton btnCancelar = new JButton("Cancel");
 		btnCancelar.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 			}
 		});
 		btnCancelar.setBounds(148, 127, 81, 23);
 		contentPane.add(btnCancelar);
 		
 		txtFechaInicio = new JTextField();
 		txtFechaInicio.setBounds(160, 11, 109, 20);
		txtFechaInicio.setText("yyyy-mm-dd");
 		contentPane.add(txtFechaInicio);
 		txtFechaInicio.setColumns(10);
 		
 		lblFechaInicio = new JLabel("Fecha de Inicio");
 		lblFechaInicio.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblFechaInicio.setBounds(45, 14, 105, 14);
 		contentPane.add(lblFechaInicio);
 	}
 }
