 package ar.edu.utn.frsf.licenciator.gui.windows;
 
 import javax.swing.JDialog;
 import javax.swing.JComboBox;
 import java.awt.BorderLayout;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import javax.swing.JTextPane;
 import java.awt.Insets;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.JButton;
 import java.awt.Component;
 import javax.swing.Box;
 import javax.swing.JOptionPane;
 import javax.swing.JSlider;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.JSeparator;
 
 import com.sun.xml.bind.v2.TODO;
 
 import ar.edu.utn.frsf.licenciator.logica.EmitirLicencia;
 import ar.edu.utn.frsf.licenciator.entidades.*;
 
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 public class EmitirGUI extends JDialog{
 	private JTextField textApellido;
 	private JTextField textNombre;
 	private JTextField textDomicilio;
 	private JTextField textLocalidad;
 	private JTextField textNac;
 	private JTextField textDonante;
 	private JTextField textFactor;
 	private JTextField textFV;
 	private JTextField textFE;
 	private JTextField textNroLic;
 	private JTextField textObs;
 	private JTextField textNroDoc;
 	private JTextField textClase;
 	private Titular titular;
 	private Licencia licencia;
 	public EmitirGUI() {
 		setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\VICTORIA\\Downloads\\descarga (1).jpg"));
 		setTitle("Emitir Licencia");
 		final JComboBox textTipoDoc = new JComboBox();
 		textTipoDoc.addItem("DNI");
 		textTipoDoc.addItem("LC");
 		textTipoDoc.addItem("LE");
 		
 		JButton btnBuscar = new JButton("Buscar");
 		btnBuscar.addActionListener(new ActionListener() {
 			/* Busqueda del titular */
 			public void actionPerformed(ActionEvent arg0) {
 				if (EmitirLicencia.dniValido(textNroDoc.getText())){
 					titular = EmitirLicencia.buscarTitular(textTipoDoc.getSelectedItem().toString(), Long.parseLong(textNroDoc.getText()));
 					if (titular != null){
 						textApellido.setText(titular.getApellido());
 						textNombre.setText(titular.getNombre());
 						textDomicilio.setText(titular.getDomicilio());
 						textLocalidad.setText(titular.getLocalidad());
 						textNac.setText(titular.getFechaNac().toString());
 						if (titular.getDonante()){
 							textDonante.setText("Si");
 						} else {
 							textDonante.setText("No");
 						}
 						textFactor.setText(titular.getTipoSanguineo().getGrupo() + titular.getTipoSanguineo().getFactor());
 					}
 				}
 				else
 				{
					JOptionPane.showMessageDialog(null, "El nmero que ha ingresado es incorrecto", "Nmero de documento no vlido", JOptionPane.ERROR_MESSAGE);	
 				}
 			}
 			
 		});
 		
 		JLabel lblApellido = new JLabel("Apellido:");
 		lblApellido.setHorizontalAlignment(SwingConstants.LEFT);
 		
 		JLabel lblNro = new JLabel("Nro Documento");
 		
 		JLabel lblTipol = new JLabel("Tipo Documento");
 		
 		JLabel lblNombre = new JLabel("Nombre:");
 		
 		JLabel lblDomicilio = new JLabel("Domicilio:");
 		
 		JLabel lblLocalidad = new JLabel("Localidad:");
 		
 		JLabel lblFechaDeNacimiento = new JLabel("Fecha de Nacimiento:");
 		
 		JLabel lblDonante = new JLabel("Donante:");
 		
 		JLabel lblGrupoYFactor = new JLabel("Grupo y factor sanguineo:");
 		
 		textApellido = new JTextField();
 		textApellido.setEditable(false);
 		textApellido.setColumns(10);
 		
 		textNombre = new JTextField();
 		textNombre.setEditable(false);
 		textNombre.setColumns(10);
 		
 		textDomicilio = new JTextField();
 		textDomicilio.setEditable(false);
 		textDomicilio.setColumns(10);
 		
 		textLocalidad = new JTextField();
 		textLocalidad.setEditable(false);
 		textLocalidad.setColumns(10);
 		
 		textNac = new JTextField();
 		textNac.setEditable(false);
 		textNac.setColumns(10);
 		
 		textDonante = new JTextField();
 		textDonante.setEditable(false);
 		textDonante.setColumns(10);
 		
 		textFactor = new JTextField();
 		textFactor.setEditable(false);
 		textFactor.setColumns(10);
 		
 		JLabel lblClase = new JLabel("Clase:");
 		
 		JLabel lblNro_1 = new JLabel("Nro:");
 		
 		JLabel lblFechaEmisin = new JLabel("Fecha emisi\u00F3n:");
 		
 		JLabel lblFechaDeVencimiento = new JLabel("Fecha de vencimiento:");
 		
 		JLabel lblObservaciones = new JLabel("Observaciones:");
 		
 		textFV = new JTextField();
 		textFV.setEditable(false);
 		textFV.setColumns(10);
 		
 		textFE = new JTextField();
 		textFE.setEditable(false);
 		textFE.setColumns(10);
 		
 		textNroLic = new JTextField();
 		textNroLic.setEditable(false);
 		textNroLic.setColumns(10);
 		
 		textClase = new JTextField();
 		textClase.setColumns(10);
 		
 		textObs = new JTextField();
 		textObs.setColumns(10);
 		
 		JSeparator separator_1 = new JSeparator();
 		
 		textNroDoc = new JTextField();
 		textNroDoc.setColumns(10);
 		
 		JButton btnEmitir = new JButton("Emitir");
 		
 		/* Cuando se pulsa el boton Emitir */
 		btnEmitir.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String clase = textClase.getText();
 				if(EmitirLicencia.claseValida(clase))
 				{
 					licencia = EmitirLicencia.emitirLicencia(titular, textClase.getText(), textObs.getText());
 					if (licencia != null)
 					{
 						textNroLic.setText(licencia.getNrolicencia());
 						textFE.setText(licencia.getFechaEmision().toString());
 						textFV.setText(licencia.getFechaVencimiento().toString());
 						
 						/*Deshabilitamos los campos editables*/
 						textTipoDoc.setEnabled(false);
 						textNroDoc.setEnabled(false);
 						textClase.setEnabled(false);
 						textObs.setEnabled(false);
 						
 					} else {
						JOptionPane.showMessageDialog(null, "La licencia requerida no se puede emitir", "Licencia no vlida", JOptionPane.ERROR_MESSAGE);
 					}
 				}
 				else
 				{
					JOptionPane.showMessageDialog(null, "La clase que ha ingresado no corresponde a una clase de licencia", "Clase de licencia no vlida", JOptionPane.ERROR_MESSAGE);	
 				}
 			}
 		});
 		
 		JLabel lblDatosDelTitular = new JLabel("Datos del titular");
 		lblDatosDelTitular.setFont(new Font("Tahoma", Font.PLAIN, 17));
 		
 		JLabel lblDatosDeLa = new JLabel("Datos de la licencia");
 		lblDatosDeLa.setFont(new Font("Tahoma", Font.PLAIN, 17));
 		
 		JButton btnCancelar = new JButton("Cancelar");
 		btnCancelar.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 			}
 		});
 		
 		JButton btnAceptar = new JButton("Aceptar");
 		btnAceptar.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (licencia !=null)
 				{
 					EmitirLicencia.guardarLicencia(licencia);
 				}
 			}
 		});
 		
 		GroupLayout groupLayout = new GroupLayout(getContentPane());
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addComponent(separator_1, GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(lblDatosDelTitular)
 					.addContainerGap(363, Short.MAX_VALUE))
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(lblDatosDeLa)
 					.addContainerGap(338, Short.MAX_VALUE))
 				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 											.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 												.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 													.addGroup(groupLayout.createSequentialGroup()
 														.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 															.addComponent(lblTipol)
 															.addComponent(lblNro)
 															.addComponent(lblApellido, GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
 															.addComponent(lblNombre)
 															.addComponent(lblDomicilio)
 															.addComponent(lblLocalidad))
 														.addPreferredGap(ComponentPlacement.RELATED))
 													.addGroup(groupLayout.createSequentialGroup()
 														.addComponent(lblDonante)
 														.addGap(87)))
 												.addGroup(groupLayout.createSequentialGroup()
 													.addComponent(lblFechaDeNacimiento)
 													.addGap(29)))
 											.addComponent(lblClase))
 										.addGroup(groupLayout.createSequentialGroup()
 											.addComponent(lblNro_1)
 											.addPreferredGap(ComponentPlacement.RELATED)))
 									.addGroup(groupLayout.createSequentialGroup()
 										.addComponent(lblFechaEmisin)
 										.addPreferredGap(ComponentPlacement.RELATED)))
 								.addGroup(groupLayout.createSequentialGroup()
 									.addComponent(lblFechaDeVencimiento)
 									.addPreferredGap(ComponentPlacement.RELATED)))
 							.addGroup(groupLayout.createSequentialGroup()
 								.addComponent(lblObservaciones)
 								.addPreferredGap(ComponentPlacement.RELATED)))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addComponent(lblGrupoYFactor)
 							.addGap(57)))
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addComponent(textFactor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
 										.addGroup(groupLayout.createSequentialGroup()
 											.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 												.addComponent(textClase, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 												.addComponent(textNroLic, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 												.addComponent(textFE, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 												.addComponent(textFV, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 												.addComponent(textObs, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE))
 											.addGap(37))
 										.addGroup(groupLayout.createSequentialGroup()
 											.addComponent(btnAceptar)
 											.addPreferredGap(ComponentPlacement.UNRELATED)))
 									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 										.addComponent(btnCancelar)
 										.addComponent(btnEmitir)))
 								.addGroup(groupLayout.createSequentialGroup()
 									.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
 										.addComponent(textTipoDoc, Alignment.LEADING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 										.addComponent(textNroDoc, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE))
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(btnBuscar))
 								.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
 									.addComponent(textLocalidad, Alignment.LEADING)
 									.addComponent(textDomicilio, Alignment.LEADING)
 									.addComponent(textNombre, Alignment.LEADING)
 									.addComponent(textApellido, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)))
 							.addGap(15))
 						.addComponent(textDonante)
 						.addComponent(textNac)))
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addContainerGap()
 							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblTipol)
 								.addComponent(textTipoDoc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblNro)
 								.addComponent(textNroDoc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGap(22)
 							.addComponent(btnBuscar)))
 					.addGap(21)
 					.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addComponent(lblDatosDelTitular)
 					.addGap(9)
 					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblApellido)
 						.addComponent(textApellido, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblNombre)
 						.addComponent(textNombre, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblDomicilio)
 						.addComponent(textDomicilio, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblLocalidad)
 						.addComponent(textLocalidad, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblDonante)
 						.addComponent(textNac, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblFechaDeNacimiento)
 						.addComponent(textDonante, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblGrupoYFactor)
 						.addComponent(textFactor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
 					.addComponent(lblDatosDeLa)
 					.addGap(38)
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addComponent(btnEmitir)
 							.addGap(183)
 							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(btnCancelar)
 								.addComponent(btnAceptar)))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblClase)
 								.addComponent(textClase, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblNro_1)
 								.addComponent(textNroLic, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblFechaEmisin)
 								.addComponent(textFE, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblFechaDeVencimiento)
 								.addComponent(textFV, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addComponent(textObs, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblObservaciones))))
 					.addContainerGap())
 		);
 		getContentPane().setLayout(groupLayout);
 	}
 	public static void LanzarGUI() {
 		try {
 			EmitirGUI dialog = new EmitirGUI();
 			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 			dialog.setVisible(true);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
