 package presentacion;
 
 import logica.Gestor;
 
import java.awt.EventQueue;

 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import java.awt.SystemColor;
 
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.SwingConstants;
 import javax.swing.JTextField;
 import javax.swing.JComboBox;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JTextArea;
 import javax.swing.JScrollPane;
 import javax.swing.JButton;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 public class PantallaCrearAdquisicion extends JFrame {
 
 	private JPanel contentPane;
 	private JTextField txtCosto;
 	private JTextField txtFechaAdquisicion;
 	private JComboBox<String> cmbCondicionAdquisicion;
 	private JLabel lblCondicionActual;
 	private JLabel lblHistoria;
 	private JTextArea txtHistoria;
 	private JScrollPane scrollPane;
 	private JButton btnCrear;
 	private JButton btnCancel;
 	
	private Gestor gestor;
 	private JLabel lblCdigoPintura;
 	private JTextField txtCodigoPintura;
 	private JLabel lblIdPropietario_1;
 	private JTextField txtidPropietario;
 	
 	private String codigoPintura;
 
 	public PantallaCrearAdquisicion(String pcodigoPintura) {
 		
 		codigoPintura = pcodigoPintura;
 		
 		setTitle("Crear Adquisici\u00F3n");
 		setResizable(false);
 		setBounds(100, 100, 441, 279);
 		contentPane = new JPanel();
 		contentPane.setBackground(SystemColor.inactiveCaptionBorder);
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JLabel lblDimensiones = new JLabel("Costo de adquisici\u00F3n");
 		lblDimensiones.setDisplayedMnemonic('D');
 		lblDimensiones.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblDimensiones.setBounds(10, 98, 128, 14);
 		contentPane.add(lblDimensiones);
 		
 		JLabel lblIdPropietario = new JLabel("Fecha de adquisici\u00F3n");
 		lblIdPropietario.setDisplayedMnemonic('F');
 		lblIdPropietario.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblIdPropietario.setBounds(10, 70, 128, 14);
 		contentPane.add(lblIdPropietario);
 		
 		lblCondicionActual = new JLabel("Condici\u00F3n Adquisici\u00F3n");
 		lblCondicionActual.setDisplayedMnemonic('i');
 		lblCondicionActual.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblCondicionActual.setLabelFor(cmbCondicionAdquisicion);
 		lblCondicionActual.setBounds(10, 129, 128, 14);
 		contentPane.add(lblCondicionActual);
 		
 		lblHistoria = new JLabel("Historia");
 		lblHistoria.setDisplayedMnemonic('H');
 		lblHistoria.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblHistoria.setBounds(93, 163, 46, 14);
 		contentPane.add(lblHistoria);
 		lblHistoria.setLabelFor(txtHistoria);
 		
 		txtCosto = new JTextField();
 		lblDimensiones.setLabelFor(txtCosto);
 		txtCosto.setBounds(148, 95, 86, 20);
 		contentPane.add(txtCosto);
 		txtCosto.setColumns(10);
 		
 		txtFechaAdquisicion = new JTextField();
 		lblIdPropietario.setLabelFor(txtFechaAdquisicion);
 		txtFechaAdquisicion.setBounds(148, 67, 86, 20);
 		contentPane.add(txtFechaAdquisicion);
 		txtFechaAdquisicion.setColumns(10);
 		
 		cmbCondicionAdquisicion = new JComboBox<String>();
 		cmbCondicionAdquisicion.setModel(new DefaultComboBoxModel<String>(new String[] {"Excelente", "Buena", "Regular", "Mala"}));
 		cmbCondicionAdquisicion.setBounds(148, 126, 86, 20);
 		contentPane.add(cmbCondicionAdquisicion);
 		
 		scrollPane = new JScrollPane();
 		scrollPane.setBounds(149, 157, 177, 45);
 		contentPane.add(scrollPane);
 		
 		txtHistoria = new JTextArea();
 		txtHistoria.setLineWrap(true);
 		scrollPane.setViewportView(txtHistoria);
 		
 		btnCrear = new JButton("Crear");
 		btnCrear.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
					gestor.crearAdquisicion( txtCodigoPintura.getText() , txtidPropietario.getText(), txtFechaAdquisicion.getText(), Double.parseDouble(txtCosto.getText()), cmbCondicionAdquisicion.getSelectedIndex(), txtHistoria.getText());
 					JOptionPane.showMessageDialog( null, "La adquisicin ha sido creada exitosamente!" );
 				} catch (Exception e1) {
 					JOptionPane.showMessageDialog( null, "Hubo un error\nPor favor revise los datos ingresados");
 					e1.printStackTrace();
 				}
 			}
 		});
 		btnCrear.setBounds(336, 213, 89, 23);
 		contentPane.add(btnCrear);
 		
 		btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 			}
 		});
 		btnCancel.setBounds(237, 213, 89, 23);
 		contentPane.add(btnCancel);
 		
 		lblCdigoPintura = new JLabel("C\u00F3digo Pintura");
 		lblCdigoPintura.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblCdigoPintura.setDisplayedMnemonic('F');
 		lblCdigoPintura.setBounds(10, 14, 128, 14);
 		contentPane.add(lblCdigoPintura);
 		
 		txtCodigoPintura = new JTextField();
 		txtCodigoPintura.setColumns(10);
 		txtCodigoPintura.setBounds(148, 11, 86, 20);
 		txtCodigoPintura.setText(codigoPintura);
 		txtCodigoPintura.setEnabled(false);
 		contentPane.add(txtCodigoPintura);
 		
 		lblIdPropietario_1 = new JLabel("Id Propietario");
 		lblIdPropietario_1.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblIdPropietario_1.setDisplayedMnemonic('F');
 		lblIdPropietario_1.setBounds(10, 42, 128, 14);
 		contentPane.add(lblIdPropietario_1);
 		
 		txtidPropietario = new JTextField();
 		txtidPropietario.setColumns(10);
 		txtidPropietario.setBounds(148, 39, 86, 20);
 		contentPane.add(txtidPropietario);
 	}
 
 }
