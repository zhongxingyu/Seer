 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 import javax.swing.JTextField;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.JComboBox;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import java.awt.event.ItemListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 
 
 public class VentanaPrincipal extends JFrame {
 
 	private JPanel contentPane;
 	private JTextField txtNombre;
 	private JTextField txtApellidos;
 	private boolean nombre = false ;
 	private boolean apellidos = false ;
 	private boolean numero = false ;
 	private JMenuBar menuBar;
 	private JTextField textField;
 	private JTextField textField_1;
 	private JTextField textField_2;
 	private JLabel lblDatosContacto;
 	private JComboBox comboBox_2;
 	private JTextField textField_3;
 	private JButton btnGuardar_1;
 	private JComboBox comboBox_3;
 	private JTextField textField_4;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					VentanaPrincipal frame = new VentanaPrincipal();
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
 	public VentanaPrincipal() {
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 450, 300);
 		
 		menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		JMenu mnNuevo = new JMenu("Nuevo");
 		menuBar.add(mnNuevo);
 		
 		JMenu mnEditar = new JMenu("Editar");
 		menuBar.add(mnEditar);
 		
 		JMenu mnBorrar = new JMenu("Borrar");
 		menuBar.add(mnBorrar);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
		JButton btnGuardar = new JButton("Guardar");
		btnGuardar.setBounds(345, 239, 89, 23);
		contentPane.add(btnGuardar);
		
 		txtNombre = new JTextField();
 		txtNombre.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				
 				if(nombre == false) {
 					nombre = true ;
 					txtNombre.setText("") ;
 				}
 			}
 		});
 		txtNombre.setHorizontalAlignment(SwingConstants.CENTER);
 		txtNombre.setText("Nombre");
 		txtNombre.setBounds(43, 38, 116, 20);
 		contentPane.add(txtNombre);
 		txtNombre.setColumns(10);
 		
 		txtApellidos = new JTextField();
 		txtApellidos.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				
 				if(apellidos == false) {
 					apellidos = true ;
 					txtApellidos.setText("") ;
 				}
 			}
 		});
 		txtApellidos.setHorizontalAlignment(SwingConstants.CENTER);
 		txtApellidos.setText("Apellidos");
 		txtApellidos.setToolTipText("");
 		txtApellidos.setBounds(181, 38, 243, 20);
 		contentPane.add(txtApellidos);
 		txtApellidos.setColumns(10);
 		
 		JComboBox comboBox = new JComboBox();
 		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Calle ", "Avenida", "Paseo"}));
 		comboBox.setBounds(43, 80, 76, 20);
 		contentPane.add(comboBox);
 		
 		textField = new JTextField();
 		textField.setHorizontalAlignment(SwingConstants.CENTER);
 		textField.setBounds(129, 80, 175, 20);
 		contentPane.add(textField);
 		textField.setColumns(10);
 		
 		final JComboBox comboBox_1 = new JComboBox();
 		comboBox_1.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				
 				if("Fijo" == comboBox_1.getSelectedItem())
 					textField_1.setText("958") ;
 				else
 					textField_1.setText("660") ;
 			}
 		});
 		comboBox_1.setModel(new DefaultComboBoxModel(new String[] {"", "Fijo", "Movil"}));
		comboBox_1.setBounds(42, 122, 54, 20);
 		contentPane.add(comboBox_1);
 		
 		textField_1 = new JTextField();
 		textField_1.setHorizontalAlignment(SwingConstants.CENTER);
 		textField_1.setBounds(117, 122, 42, 20);
 		contentPane.add(textField_1);
 		textField_1.setColumns(10);
 		
 		textField_2 = new JTextField();
 		textField_2.setHorizontalAlignment(SwingConstants.CENTER);
 		textField_2.setBounds(169, 122, 81, 20);
 		contentPane.add(textField_2);
 		textField_2.setColumns(10);
 		
 		lblDatosContacto = new JLabel("Datos Contacto");
 		lblDatosContacto.setHorizontalAlignment(SwingConstants.CENTER);
 		lblDatosContacto.setBounds(154, 0, 96, 14);
 		contentPane.add(lblDatosContacto);
 		
 		comboBox_2 = new JComboBox();
 		comboBox_2.setModel(new DefaultComboBoxModel(new String[] {"", "Numero", "Piso", "Bloque", "Portal"}));
 		comboBox_2.setBounds(314, 80, 78, 20);
 		contentPane.add(comboBox_2);
 		
 		textField_3 = new JTextField();
 		textField_3.setBounds(396, 80, 28, 20);
 		contentPane.add(textField_3);
 		textField_3.setColumns(10);
 		
 		btnGuardar_1 = new JButton("Guardar");
		btnGuardar_1.setBounds(345, 218, 89, 23);
 		contentPane.add(btnGuardar_1);
 		
 		comboBox_3 = new JComboBox();
 		comboBox_3.setModel(new DefaultComboBoxModel(new String[] {"", "Gmail", "Outlook", "Yahoo"}));
 		comboBox_3.setBounds(43, 168, 62, 20);
 		contentPane.add(comboBox_3);
 		
 		textField_4 = new JTextField();
 		textField_4.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent arg0) {
 				String inicial = textField_4.getText() ;
 				String resultado = null ;
 				
 				
 				if("Gmail" == comboBox_3.getSelectedItem())
 					resultado = inicial + "@gmail.com" ;
 				else if("Outlook" == comboBox_3.getSelectedItem())
 					resultado = inicial + "@hotmail.com" ;
 				else if("Yahoo" == comboBox_3.getSelectedItem()) 
 					resultado = inicial + "yahoo.es" ;
 				else
 					resultado = inicial ;
 				
 				textField_4.setText(resultado) ;
 			}
 		});
 		textField_4.addMouseListener(new MouseAdapter() {
 		});
 		textField_4.setBounds(117, 168, 187, 20);
 		contentPane.add(textField_4);
 		textField_4.setColumns(10);
 	}
 }
