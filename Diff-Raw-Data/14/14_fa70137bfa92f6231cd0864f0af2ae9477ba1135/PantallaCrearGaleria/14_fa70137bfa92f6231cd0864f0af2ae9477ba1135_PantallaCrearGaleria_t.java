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
 
 public class PantallaCrearGaleria extends JFrame {
 
 	private JPanel contentPane;
 	private JTextField txtId;
 	private JTextField txtNombre;
 	private JTextField txtDireccion;
 	private JTextField txtTelefono;
 	private JTextField txtFechaInauguracion;
 	private JTextField txtNombreEncargado;
 	private JTextField txtArea;
 	private JButton btnCancel;
 
 	public PantallaCrearGaleria() {
 		
 		setResizable(false);
 		setTitle("Crear Galer\u00EDa");
 		setBounds(100, 100, 339, 265);
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
 		
 		JLabel lblFechaInauguracion = new JLabel("Fecha de inauguraci\u00F3n");
 		lblFechaInauguracion.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblFechaInauguracion.setBounds(0, 111, 137, 14);
 		contentPane.add(lblFechaInauguracion);
 		
 		JLabel lblNombreEncargado = new JLabel("Nombre del encargado");
 		lblNombreEncargado.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblNombreEncargado.setBounds(0, 136, 137, 14);
 		contentPane.add(lblNombreEncargado);
 		
 		JLabel lblArea = new JLabel("\u00C1rea");
 		lblArea.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblArea.setBounds(91, 161, 46, 14);
 		contentPane.add(lblArea);
 		
 		txtId = new JTextField();
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
 		
 		txtFechaInauguracion = new JTextField();
 		txtFechaInauguracion.setBounds(147, 108, 86, 20);
		txtFechaInauguracion.setText("yyyy-mm-dd");
 		contentPane.add(txtFechaInauguracion);
 		txtFechaInauguracion.setColumns(10);
 		
 		txtNombreEncargado = new JTextField();
 		txtNombreEncargado.setBounds(147, 133, 86, 20);
 		contentPane.add(txtNombreEncargado);
 		txtNombreEncargado.setColumns(10);
 		
 		txtArea = new JTextField();
 		txtArea.setBounds(147, 158, 86, 20);
 		contentPane.add(txtArea);
 		txtArea.setColumns(10);
 		
 		JButton btnCrear = new JButton("Crear");
 		btnCrear.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Gestor.crearGaleria( txtId.getText(), txtNombre.getText(), txtDireccion.getText(), txtTelefono.getText(), txtFechaInauguracion.getText(), txtNombreEncargado.getText(), txtArea.getText());
 					JOptionPane.showMessageDialog( null, "La galeria ha sido creada exitosamente!" );
					setVisible(false);
 				} catch (Exception e1) {
 					JOptionPane.showMessageDialog( null, "Hubo un error\nPor favor revise los datos ingresados");
 				}
 			}
 		});
 		btnCrear.setBounds(234, 203, 89, 23);
 		contentPane.add(btnCrear);
 		
 		btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 			}
 		});
 		btnCancel.setBounds(135, 203, 89, 23);
 		contentPane.add(btnCancel);
 	}
 
 }
