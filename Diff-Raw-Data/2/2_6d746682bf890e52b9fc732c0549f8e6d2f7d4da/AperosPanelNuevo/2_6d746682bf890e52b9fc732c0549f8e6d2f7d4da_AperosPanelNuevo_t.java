 package code.google.com.opengis.gestionVISUAL;
 
 import javax.swing.JPanel;
 
 import javax.swing.JLabel;
 import java.awt.Rectangle;
 import javax.swing.JButton;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.JPasswordField;
 import javax.swing.JComboBox;
 
 import org.eclipse.swt.custom.CBanner;
 
 import code.google.com.opengis.gestion.Apero;
 import code.google.com.opengis.gestion.Usuarios;
 import code.google.com.opengis.gestionDAO.AperoDAO;
 import code.google.com.opengis.gestionDAO.ConectarDBA;
 import code.google.com.opengis.gestionDAO.Idioma;
 
 import java.awt.Font;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class AperosPanelNuevo extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	private JLabel lblId = null;
 	private JLabel lblNombre = null;
 	private JLabel lblTamao = null;
 	private JLabel lblDescripcion = null;
 	private JLabel lblTarea = null;
 	private JLabel lblUser = null;
 	private JButton bGuardar = null;
 	private JButton bRestablecer = null;
 	private JTextField txtId = null;
 	private JTextField txtNombre = null;
 	private JTextField txtTamao = null;
 	private JTextField txtDescripcion = null;
 	private JTextField txtUser = null;
 	private JComboBox comboTarea = null;
 	private JLabel lblObligtorios = null;
 	private String accion;
 
 	private String id = ""; //$NON-NLS-1$
 	private String nombre = ""; //$NON-NLS-1$
 	private String tamao = ""; //$NON-NLS-1$
 	private String descripcion = ""; //$NON-NLS-1$
 	private String tarea = " "; //$NON-NLS-1$
 	private String user = ""; //$NON-NLS-1$
 	private ConectarDBA dba = new ConectarDBA();
 
 	/**
 	 * Constructor del Panel de gestin de Usuarios. En caso de que la accin
 	 * sea "modificar" el panel se utilizar para modificar. En caso de que la
 	 * accin sea "alta" el panel se utilizar como altas.
 	 */
 	public AperosPanelNuevo(String accion, String id, String nombre,
 			String tamao, String descripcion, String tarea,
 			String user) {
 		super();
 		this.accion = accion;
 		this.id = id;
 		this.nombre = nombre;
 		this.tamao = tamao;
 		this.descripcion = descripcion;
 		this.tarea = tarea;
 		this.user = user;
 		initialize();
 		this.comboTarea.setSelectedIndex(Integer.parseInt(tarea)-1);
 	}
 
 	public AperosPanelNuevo(String accion) {
 
 		super();
 		this.accion = accion;
 
 		initialize();
 		
 		try {
 			String snt = "SELECT MAX(idapero) FROM `apero`"; //$NON-NLS-1$
 			dba.acceder();
 			ResultSet rs2 = dba.consulta(snt);
 			while (rs2.next()) {
 				txtId.setText((rs2.getInt(1) + 1) + ""); //$NON-NLS-1$
 			}
 			txtId.setEnabled(false);
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * This method initializes this
 	 * 
 	 * @return void
 	 */
 	private void initialize() {
 		lblId = new JLabel();
 		lblId.setBounds(new Rectangle(42, 31, 88, 30));
 		lblId.setText(Idioma.getString("etImplementId")); //$NON-NLS-1$
 		lblObligtorios = new JLabel();
 		lblObligtorios.setBounds(new Rectangle(434, 334, 238, 25));
 		lblObligtorios.setFont(new Font(
 				Idioma.getString("Dialog"), Font.ITALIC, 12)); //$NON-NLS-1$
 		lblObligtorios.setText(Idioma.getString("etAllFields")); //$NON-NLS-1$
 		lblUser = new JLabel();
 		lblUser.setBounds(new Rectangle(42, 137, 88, 30));
 		lblUser.setText(Idioma.getString("etUser")); //$NON-NLS-1$
 		lblTarea = new JLabel();
 		lblTarea.setBounds(new Rectangle(320, 84, 88, 30));
 		lblTarea.setText(Idioma.getString("etTask")); //$NON-NLS-1$
 		lblDescripcion = new JLabel();
 		lblDescripcion.setBounds(new Rectangle(320, 137, 88, 30));
 		lblDescripcion.setText(Idioma.getString("etDesc")); //$NON-NLS-1$
 		lblTamao = new JLabel();
 		lblTamao.setBounds(new Rectangle(42, 84, 88, 30));
 		lblTamao.setText(Idioma.getString("etSize")); //$NON-NLS-1$
 		lblNombre = new JLabel();
 		lblNombre.setBounds(new Rectangle(320, 31, 88, 30));
		lblNombre.setText(Idioma.getString("etName")); //$NON-NLS-1$
 		this.setSize(782, 388);
 		this.setLayout(null);
 		this.add(lblId, null);
 		this.add(lblNombre, null);
 		this.add(lblTamao, null);
 		this.add(lblDescripcion, null);
 		this.add(lblTarea, null);
 		this.add(lblUser, null);
 		this.add(getBGuardar(), null);
 		this.add(getBRestablecer(), null);
 		this.add(getTxtId(), null);
 		this.add(getTxtNombre(), null);
 		this.add(getTxtTamao(), null);
 		this.add(getTxtUser(), null);
 		this.add(getTxtDescripcion(), null);
 		this.add(lblObligtorios, null);
 		this.add(getComboTarea(), null);
 	}
 
 	/**
 	 * This method initializes bGuardar
 	 * 
 	 * @return javax.swing.JButton
 	 */
 	private JButton getBGuardar() {
 		if (bGuardar == null) {
 			bGuardar = new JButton();
 			bGuardar.setBounds(new Rectangle(46, 314, 53, 45));
 			bGuardar.setIcon(new ImageIcon(getClass().getResource(
 					"/recursosVisuales/Guardar.png"))); //$NON-NLS-1$
 			bGuardar.setToolTipText(Idioma.getString("etSaveNewUser")); //$NON-NLS-1$
 			bGuardar.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 
 					if (accion.equals("alta")) { //$NON-NLS-1$
 
 						Apero ap = new Apero(Integer.parseInt(txtId.getText()), txtNombre.getText(),
 								Integer.parseInt(txtTamao.getText()), txtDescripcion.getText(),
 								comboTarea.getSelectedIndex()+1, true,
 								txtUser.getText());
 						if (ap.validarDatos(txtId.getText(), txtNombre.getText(),
 								txtTamao.getText(), txtDescripcion.getText(),
 								(comboTarea.getSelectedIndex() + 1) + "", "0", //$NON-NLS-1$ //$NON-NLS-2$
 								txtUser.getText())) {
 							
 							AperoDAO adao = new AperoDAO(txtId.getText(), txtNombre.getText(),
 									txtTamao.getText(), txtDescripcion.getText(),
 									(comboTarea.getSelectedIndex() + 1) + "", "0", //$NON-NLS-1$ //$NON-NLS-2$
 									txtUser.getText());
 							
 							try {
 								adao.altaApero();
 							} catch (SQLException e1) {
 								JOptionPane.showMessageDialog(null, Idioma.getString("msgIDAlreadyExists")); //$NON-NLS-1$
 							}
 						
 
 						}
 					} else {
 						
 						Apero ap = new Apero(Integer.parseInt(txtId.getText()), txtNombre.getText(),
 								Integer.parseInt(txtTamao.getText()), txtDescripcion.getText(),
 								comboTarea.getSelectedIndex()+1, true,
 								txtUser.getText());
 						
 						if (ap.validarDatos(txtId.getText(), txtNombre.getText(),
 								txtTamao.getText(), txtDescripcion.getText(),
 								(comboTarea.getSelectedIndex() + 1) + "", "0", //$NON-NLS-1$ //$NON-NLS-2$
 								txtUser.getText())) {
 							
 							AperoDAO adao = new AperoDAO(txtId.getText(), txtNombre.getText(),
 									txtTamao.getText(), txtDescripcion.getText(),
 									(comboTarea.getSelectedIndex() + 1) + "", "0", //$NON-NLS-1$ //$NON-NLS-2$
 									txtUser.getText());
 							
 							try {
 								adao.MoficicarApero();
 							} catch (SQLException e1) {
 								JOptionPane.showMessageDialog(null, Idioma.getString("msgIDAlreadyExists")); //$NON-NLS-1$
 							}
 						} else {
 
 							JOptionPane.showMessageDialog(null, Idioma.getString("etImplementDataWrong")); //$NON-NLS-1$
 
 						}
 
 					}
 				}
 			});
 		}
 		return bGuardar;
 	}
 
 	/**
 	 * This method initializes bRestablecer
 	 * 
 	 * @return javax.swing.JButton
 	 */
 	private JButton getBRestablecer() {
 		if (bRestablecer == null) {
 			bRestablecer = new JButton();
 			bRestablecer.setBounds(new Rectangle(122, 314, 53, 45));
 			bRestablecer.setIcon(new ImageIcon(getClass().getResource(
 					"/recursosVisuales/Limpiar.png"))); //$NON-NLS-1$
 			bRestablecer.setToolTipText(Idioma.getString("etCleanFields")); //$NON-NLS-1$
 			bRestablecer.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 
 					if (accion != "modificar") { //$NON-NLS-1$
 
 						txtId.setText(""); //$NON-NLS-1$
 
 					}
 					txtNombre.setText(""); //$NON-NLS-1$
 					txtTamao.setText(""); //$NON-NLS-1$
 					txtDescripcion.setText(""); //$NON-NLS-1$
 					txtUser.setText(""); //$NON-NLS-1$
 
 				}
 			});
 		}
 		return bRestablecer;
 	}
 
 	/**
 	 * This method initializes txtId
 	 * 
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtId() {
 		if (txtId == null) {
 			txtId = new JTextField(id);
 			txtId.setBounds(new Rectangle(123, 33, 143, 27));
 
 			if (accion == "modificar") { //$NON-NLS-1$
 
 				txtId.setEnabled(false);
 			}
 
 		}
 		return txtId;
 	}
 
 	/**
 	 * This method initializes txtNombre
 	 * 
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtNombre() {
 		if (txtNombre == null) {
 			txtNombre = new JTextField(nombre);
 			txtNombre.setBounds(new Rectangle(401, 33, 143, 27));
 		}
 		return txtNombre;
 	}
 
 	/**
 	 * This method initializes txtTamao
 	 * 
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtTamao() {
 		if (txtTamao == null) {
 			txtTamao = new JTextField(tamao);
 			txtTamao.setBounds(new Rectangle(123, 86, 143, 27));
 		}
 		return txtTamao;
 	}
 
 	/**
 	 * This method initializes txtDescripcion
 	 * 
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtDescripcion() {
 		if (txtDescripcion == null) {
 			txtDescripcion = new JTextField(descripcion);
 			txtDescripcion.setBounds(new Rectangle(401, 139, 230, 108));
 		}
 		return txtDescripcion;
 	}
 
 	/**
 	 * This method initializes txtUser
 	 * 
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtUser() {
 		if (txtUser == null) {
 			txtUser = new JTextField(user);
 			txtUser.setBounds(new Rectangle(123, 139, 143, 27));
 		}
 		return txtUser;
 	}
 
 	private JComboBox getComboTarea() {
 		if (comboTarea == null) {
 			comboTarea = new JComboBox();
 			comboTarea.setBounds(new Rectangle(401, 86, 143, 27));
 			dba.acceder();
 			String senten = new String("SELECT * FROM tareas"); //$NON-NLS-1$
 			ResultSet rs;
 			try {
 				rs = dba.consulta(senten);
 				while (rs.next()) {
 					comboTarea.addItem("" + rs.getObject(1) + " - " //$NON-NLS-1$ //$NON-NLS-2$
 							+ rs.getObject(2) + " " + rs.getObject(3) + " " //$NON-NLS-1$ //$NON-NLS-2$
 							+ rs.getObject(4));
 				}
 				rs.close();
 				dba.cerrarCon();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return comboTarea;
 	}
 
 } // @jve:decl-index=0:visual-constraint="26,16"
