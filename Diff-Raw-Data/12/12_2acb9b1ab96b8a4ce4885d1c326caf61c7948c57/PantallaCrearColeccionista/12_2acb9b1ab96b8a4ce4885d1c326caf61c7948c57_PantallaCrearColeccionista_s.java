 package presentacion;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.JButton;
 
 import logica.Gestor;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.SystemColor;
 
 public class PantallaCrearColeccionista extends JFrame {
 
 	private JPanel contentPane;
 	private JTextField txtId;
 	private JTextField txtNombre;
 	private JTextField txtDireccion;
 	private JTextField txtTelefono;
 	private JTextField txtFechaInicio;
 	private JButton btnCancel;
 
 	public PantallaCrearColeccionista() {
 		
 		setResizable(false);
 		setTitle("Crear Coleccionista");
 		setBounds(100, 100, 339, 216);
 		contentPane = new JPanel();
 		contentPane.setBackground(SystemColor.inactiveCaptionBorder);
 		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JLabel lblId = new JLabel("Id");
 		lblId.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblId.setBounds(91, 11, 46, 14);
 		contentPane.add(lblId);
 		
 		JLabel lblNombre = new JLabel("Nombre");
 		lblNombre.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblNombre.setBounds(91, 36, 46, 14);
 		contentPane.add(lblNombre);
 		
 		JLabel lblDireccion = new JLabel("Direcci\u00F3n");
 		lblDireccion.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblDireccion.setBounds(54, 61, 83, 14);
 		contentPane.add(lblDireccion);
 		
 		JLabel lblTelefono = new JLabel("Tel\u00E9fono");
 		lblTelefono.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblTelefono.setBounds(54, 86, 83, 14);
 		contentPane.add(lblTelefono);
 		
 		JLabel lblFechaInicio = new JLabel("Fecha de inicio");
 		lblFechaInicio.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblFechaInicio.setBounds(0, 111, 137, 14);
 		contentPane.add(lblFechaInicio);
 		
 		txtId = new JTextField();
 		//txtId.setEditable(false);
 		txtId.setBounds(147, 8, 86, 20);
 		contentPane.add(txtId);
 		txtId.setColumns(10);
 		
 		txtNombre = new JTextField();
 		txtNombre.setBounds(147, 33, 86, 20);
 		contentPane.add(txtNombre);
 		txtNombre.setColumns(10);
 		
 		txtDireccion = new JTextField();
 		txtDireccion.setBounds(147, 58, 86, 20);
 		contentPane.add(txtDireccion);
 		txtDireccion.setColumns(10);
 		
 		txtTelefono = new JTextField();
 		txtTelefono.setBounds(147, 83, 86, 20);
 		contentPane.add(txtTelefono);
 		txtTelefono.setColumns(10);
 		
 		txtFechaInicio = new JTextField();
 		txtFechaInicio.setBounds(147, 108, 86, 20);
 		contentPane.add(txtFechaInicio);
 		txtFechaInicio.setColumns(10);
 		
 		JButton btnCrear = new JButton("Crear");
 		btnCrear.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Gestor.crearColeccionista( txtId.getText(), txtNombre.getText(), txtDireccion.getText(), txtTelefono.getText(), txtFechaInicio.getText());
 					JOptionPane.showMessageDialog( null, "El coleccionista ha sido creado exitosamente!" );
 				} catch (Exception e1) {
 					JOptionPane.showMessageDialog( null, "Hubo un error\nPor favor revise los datos ingresados");
 				}
 			}
 		});
 		btnCrear.setBounds(234, 154, 89, 23);
 		contentPane.add(btnCrear);
 		
 		btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 			}
 		});
 		btnCancel.setBounds(135, 154, 89, 23);
 		contentPane.add(btnCancel);
 	}
 
 }
