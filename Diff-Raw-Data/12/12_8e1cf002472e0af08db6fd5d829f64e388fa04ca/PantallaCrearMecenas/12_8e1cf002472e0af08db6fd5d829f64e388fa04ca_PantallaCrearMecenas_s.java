 package presentacion;
 
 import logica.Gestor;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.SwingConstants;
 import javax.swing.JTextField;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JButton;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.SystemColor;
 
 public class PantallaCrearMecenas extends JFrame {
 
 	private JPanel contentPane;
 	private JTextField txtId;
 	private JTextField txtNombre;
 	private JTextField txtPaisNacimiento;
 	private JTextField txtCiudadNacimiento;
 	private JTextField txtFechaMuerte;
 
 	public PantallaCrearMecenas() {
 		setTitle("Crear Mecenas");
 		setResizable(false);
 		setBounds(100, 100, 323, 206);
 		contentPane = new JPanel();
 		contentPane.setBackground(SystemColor.inactiveCaptionBorder);
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JLabel lblId = new JLabel("Id");
 		lblId.setDisplayedMnemonic('I');
 		lblId.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblId.setBounds(10, 11, 121, 14);
 		contentPane.add(lblId);
 		
 		txtId = new JTextField();
 		lblId.setLabelFor(txtId);
 		txtId.setBounds(141, 8, 86, 20);
 		contentPane.add(txtId);
 		txtId.setColumns(10);
 		
 		JLabel lblNombre = new JLabel("Nombre");
 		lblNombre.setDisplayedMnemonic('N');
 		lblNombre.setLabelFor(lblNombre);
 		lblNombre.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblNombre.setBounds(85, 36, 46, 14);
 		contentPane.add(lblNombre);
 		
 		txtNombre = new JTextField();
 		txtNombre.setBounds(141, 33, 86, 20);
 		contentPane.add(txtNombre);
 		txtNombre.setColumns(10);
 		
 		JLabel lblPaisNacimiento = new JLabel("Pa\u00EDs de nacimiento");
 		lblPaisNacimiento.setDisplayedMnemonic('P');
 		lblPaisNacimiento.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblPaisNacimiento.setBounds(10, 89, 121, 14);
 		contentPane.add(lblPaisNacimiento);
 		
 		txtPaisNacimiento = new JTextField();
 		lblPaisNacimiento.setLabelFor(txtPaisNacimiento);
 		txtPaisNacimiento.setBounds(141, 86, 86, 20);
 		contentPane.add(txtPaisNacimiento);
 		txtPaisNacimiento.setColumns(10);
 		
 		JLabel lblCiudadNacimiento = new JLabel("Ciudad de nacimiento");
 		lblCiudadNacimiento.setDisplayedMnemonic('P');
 		lblCiudadNacimiento.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblCiudadNacimiento.setBounds(10, 61, 121, 14);
 		contentPane.add(lblCiudadNacimiento);
 		
 		txtCiudadNacimiento = new JTextField();
 		lblCiudadNacimiento.setLabelFor(txtCiudadNacimiento);
 		txtCiudadNacimiento.setBounds(141, 58, 86, 20);
 		contentPane.add(txtCiudadNacimiento);
 		txtCiudadNacimiento.setColumns(10);
 		
 		JLabel lblFechaMuerte = new JLabel("Fecha de muerte");
 		lblFechaMuerte.setDisplayedMnemonic('F');
 		lblFechaMuerte.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblFechaMuerte.setBounds(34, 114, 97, 14);
 		contentPane.add(lblFechaMuerte);
 		
 		txtFechaMuerte = new JTextField();
 		lblFechaMuerte.setLabelFor(txtFechaMuerte);
 		txtFechaMuerte.setBounds(141, 111, 86, 20);
 		contentPane.add(txtFechaMuerte);
 		txtFechaMuerte.setColumns(10);
 		
 		JButton btnCrear = new JButton("Crear");
 		btnCrear.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) throws NumberFormatException {
 				try {
 					Gestor.crearMecenas( txtId.getText(), txtNombre.getText(), txtPaisNacimiento.getText(), txtCiudadNacimiento.getText(), txtFechaMuerte.getText());
 					JOptionPane.showMessageDialog( null, "El mecenas ha sido creado exitosamente!" );
 				} catch (Exception e1) {
 					JOptionPane.showMessageDialog( null, "Hubo un error\nPor favor revise los datos ingresados");
 				} 
 			}
 		});
 		btnCrear.setBounds(218, 144, 89, 23);
 		contentPane.add(btnCrear);
 		
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 			}
 		});
 		btnCancel.setBounds(119, 144, 89, 23);
 		contentPane.add(btnCancel);
 	}
 }
