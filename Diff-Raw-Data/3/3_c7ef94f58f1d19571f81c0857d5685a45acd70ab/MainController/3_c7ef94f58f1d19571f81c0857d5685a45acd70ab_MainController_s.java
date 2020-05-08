 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.porcupine.psp.controller;
 
 import com.porcupine.psp.model.dao.DAOFactory;
 import com.porcupine.psp.model.dao.exceptions.DataBaseException;
 import com.porcupine.psp.model.dao.exceptions.InsufficientPermissionsException;
 import com.porcupine.psp.model.dao.exceptions.InternalErrorException;
 import com.porcupine.psp.model.dao.exceptions.InvalidAttributeException;
 import com.porcupine.psp.model.dao.exceptions.NonexistentEntityException;
 import com.porcupine.psp.model.dao.exceptions.RequiredAttributeException;
 import com.porcupine.psp.model.entity.ImplSeguridad;
 import com.porcupine.psp.model.entity.Proveedor;
 import com.porcupine.psp.model.service.ServiceFactory;
 import com.porcupine.psp.model.vo.*;
 import com.porcupine.psp.util.DrawingUtilities;
 import com.porcupine.psp.util.ServidoresDisponibles;
 import com.porcupine.psp.util.TipoEmpleado;
 import com.porcupine.psp.view.*;
 import java.io.*;
 import java.math.BigDecimal;
 import java.net.URLDecoder;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.persistence.EntityNotFoundException;
 import javax.swing.DefaultListModel;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.table.DefaultTableModel;
 import javax.mail.*;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 /**
  * El proposito de esta clase es tener un lugar integrado con todos los metodos
  * necesarios para la ejecución satisfactoria de la aplicación, y la logica de
  * carga de datos hacia la vista. La logica de base de datos esta implementada
  * en cada motor y en la capa de DAO's. Se usan objetos de valor para tratar
  * algunos objetos de arriba.
  *
  * @author Zergio
  */
 public class MainController {
 
     //Variables de estado
     static Login login;
     static SelectDataBaseConnection sdb;
     public static Map connectionPropierties;
     public static CreateEmployee crearEmpleado;
     public static Psp psp;
     public static Helper helper;
     public static Helper helper1;
     public static Helper helper2;
     public static RegisterImplement registrarImplemento;
     public static AddContract agregarContrato;
     public static AddClient agregarCliente;
     public static WriteNotice agregarWriteNotice;
     public static ReplyNotice crearReplyNotice;
     public static EmpleadosVO empleadoActivo;
     public static AddContract addContract;
     public static WriteNotice writeNotice;
     public static Secondary secondary;
     public static FindPerson findPerson;
     public static FindClient findClient;
     public static BitacoraDisplay bitacora;
     public static String username;
     public static String selectedDB;
     public static String password;
     //VOS Temporales para hacer operaciones
     public static EmpleadosVO empleadoTemporal;
     static DeleteImplement eliminarImplemento = new DeleteImplement();
     public static DefaultTableModel modelTable;
     public static AssignImplements asignarImplementos;
     public static AddImplement adicionarImplemento;
     public static UpdateImplementList listaActualizarImplementos;
     public static UpdateImplement actualizarImplemento;
     private static AssignContract asignarContrato;
 
     public static Map getConnectionPropierties() {
         return connectionPropierties;
     }
 
     public static void setConnectionPropierties(Map connectionPropierties) {
         MainController.connectionPropierties = connectionPropierties;
     }
 
     public static CreateEmployee getCrearEmpleado() {
         return crearEmpleado;
     }
 
     public static void setCrearEmpleado(CreateEmployee crearEmpleado) {
         MainController.crearEmpleado = crearEmpleado;
     }
 
     public static Helper getHelper() {
         return helper;
     }
 
     public static void setHelper(Helper helper) {
         MainController.helper = helper;
     }
 
     //Constructores y cosas similares
     //<editor-fold defaultstate="collapsed" desc="Constructores">
     public static Login getLogin() {
         return login;
     }
 
     public static void setLogin(Login login) {
         MainController.login = login;
     }
 
     public static SelectDataBaseConnection getSdb() {
         return sdb;
     }
 
     public static void setSdb(SelectDataBaseConnection sdb) {
         MainController.sdb = sdb;
     }
 
     public static Psp getPsp() {
         return psp;
     }
 
     public static void setPsp(Psp psp) {
         MainController.psp = psp;
     }
 
     public static EmpleadosVO getEmpleadoActivo() {
         return empleadoActivo;
     }
 
     public static void setEmpleadoActivo(EmpleadosVO empleadoActivo) {
         MainController.empleadoActivo = empleadoActivo;
     }
 
     public static String getSelectedDB() {
         return selectedDB;
     }
 
     public static void setSelectedDB(String selectedDB) {
         MainController.selectedDB = selectedDB;
     }
 
     public static EmpleadosVO getEmpleadoTemporal() {
         return empleadoTemporal;
     }
 
     public static void setEmpleadoTemporal(EmpleadosVO empleadoTemporal) {
         MainController.empleadoTemporal = empleadoTemporal;
     }
 
     //</editor-fold>
     //Clases de muestra
     public static void mostrarLogin() {
         psp = new Psp();
         psp.setLocationRelativeTo(null);
         try {
             login = new Login();
         } catch (UnsupportedEncodingException ex) {
             Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
         }
         psp.setVisible(true);
         DrawingUtilities.drawPanel(psp, psp.getViewport(), login);
     }
 
     public static void modificarFicheroSeguridad() throws UnsupportedEncodingException, IOException {
         String fileName = "propierties.psp";
 
         String path = Psp.class.getProtectionDomain().getCodeSource().getLocation().getPath();
         path = URLDecoder.decode(path, "UTF-8");
 
         path = path + File.separator + fileName;
         File f = new File(path);
 
         try {
 
             Properties properties = new Properties();
             properties.setProperty("username", sdb.getjTextFieldUserName().getText());
             properties.setProperty("password", new String(sdb.getjPasswordField().getPassword()));
             properties.setProperty("server", sdb.getjComboBox1().getSelectedItem().toString());
 
             FileOutputStream fileOut = new FileOutputStream(f);
             properties.storeToXML(fileOut, "PSPPropierties");
             fileOut.close();
 
             JOptionPane.showMessageDialog(helper, "Nuevos parametros guardados con éxito!", "Proceso Exitoso", JOptionPane.INFORMATION_MESSAGE, null);
 
         } catch (IOException ex) {
             JOptionPane.showMessageDialog(helper, "Error en el almacenamiento de parametros!", "Proceso Fallido", JOptionPane.INFORMATION_MESSAGE, null);
         }
 
     }
 
     public static void leerFicherosConfiguracion() throws UnsupportedEncodingException, FileNotFoundException, IOException {
         String fileName = "propierties.psp";
 
         String path = Psp.class.getProtectionDomain().getCodeSource().getLocation().getPath();
         path = URLDecoder.decode(path, "UTF-8");
 
         path = path + File.separator + fileName;
         File f = new File(path);
 
 
         if (!f.exists()) {
 
             JOptionPane.showMessageDialog(psp, "No existen parametros de configuración, Se intentaran crear a continuación", "Atención", JOptionPane.INFORMATION_MESSAGE, null);
             try {
 
                 Properties properties = new Properties();
                 properties.setProperty("username", "jdrozob");
                 properties.setProperty("password", "s02257974");
                 properties.setProperty("server", ServidoresDisponibles.ORA);
 
                 FileOutputStream fileOut = new FileOutputStream(f);
                 properties.storeToXML(fileOut, "PSPPropierties");
                 fileOut.close();
 
 
 
                 JOptionPane.showMessageDialog(psp, "Nuevos parametros guardados con éxito!", "Proceso Exitoso", JOptionPane.INFORMATION_MESSAGE, null);
 
             } catch (Exception ex) {
             }
         }
 
         Properties props = new Properties();
         props.loadFromXML(new FileInputStream(f));
 
         username = props.getProperty("username");
         password = props.getProperty("password");
         selectedDB = props.getProperty("server");
     }
 
     public static void mostrarSeleccionDB() {
         helper1 = new Helper();
         helper1.setLocationRelativeTo(null);
         sdb = new SelectDataBaseConnection();
         helper1.setVisible(true);
         DrawingUtilities.drawPanel(helper1, helper1.getViewport(), sdb);
     }
 
     public static void mostrarFormularioCrearEmpleado(ArrayList<String> empleadosDisponibles) {
         helper = new Helper();
         helper.setLocationRelativeTo(null);
         crearEmpleado = new CreateEmployee(empleadosDisponibles);
         helper.setVisible(true);
         DrawingUtilities.drawPanel(helper, helper.getViewport(), crearEmpleado);
         helper.setTitle("Crear Empleado...");
 
         crearEmpleado.getjButtonEncontrarContratos().setEnabled(false);
     }
 
     public static void mostrarFormuarioCrearImplementos() {
         helper = new Helper();
         helper.setLocationRelativeTo(null);
         registrarImplemento = new RegisterImplement();
         helper.setVisible(true);
         DrawingUtilities.drawPanel(helper, helper.getViewport(), registrarImplemento);
         helper.setTitle("Porcupine Software Portal");
     }
 
     public static void mostrarFormuariosEliminarImplementos() {
         helper = new Helper();
         helper.setLocationRelativeTo(null);
         eliminarImplemento = new DeleteImplement();
         helper.setVisible(true);
         DrawingUtilities.drawPanel(helper, helper.getViewport(), eliminarImplemento);
         helper.setTitle("Porcupine Software Portal");
     }
 
     public static void mostrarFormularioAsignarImplementos() {
         helper = new Helper();
         helper.setLocationRelativeTo(null);
         asignarImplementos = new AssignImplements();
         helper.setVisible(true);
         DrawingUtilities.drawPanel(helper, helper.getViewport(), asignarImplementos);
         helper.setTitle("Porcupine Software Portal");
     }
 
     public static void mostrarFormularioAdicionarImplementos() {
         helper = new Helper();
         helper.setLocationRelativeTo(null);
         adicionarImplemento = new AddImplement();
         helper.setVisible(true);
         DrawingUtilities.drawPanel(helper, helper.getViewport(), adicionarImplemento);
         helper.setTitle("Porcupine Software Portal");
     }
 
     public static void mostrarFormularioListaActualizarImplementos() {
         helper = new Helper();
         helper.setLocationRelativeTo(null);
         listaActualizarImplementos = new UpdateImplementList();
         helper.setVisible(true);
         DrawingUtilities.drawPanel(helper, helper.getViewport(), listaActualizarImplementos);
         helper.setTitle("Porcupine Software Portal");
     }
 
     public static void mostrarFormularioActualizarImplemento(Short idImplemento) throws EntityNotFoundException, InsufficientPermissionsException {
         actualizarImplemento = new UpdateImplement();
         ImplSeguridadVO implemento = ServiceFactory.getInstance().getImplSeguridadService().find(idImplemento);
         actualizarImplemento.getjTextFieldId().setText(implemento.getIdImplemento().toString());
         actualizarImplemento.getjTextFieldNombre().setText(implemento.getNombreI());
         actualizarImplemento.getjTextFieldValorUnitario().setText(implemento.getPrecioUnitarioI().toString());
         Short cantidad = implemento.getCantidad();
         actualizarImplemento.getjTextFieldCantidad().setText(cantidad.toString());
         actualizarImplemento.getjComboBoxEstado().setSelectedItem(implemento.getEstadoI());
         actualizarImplemento.getjTextAreaDescripcion().setText(implemento.getDescripcionI());
         ProveedorVO proveedor = ServiceFactory.getInstance().getProveedorService().find(implemento.getIdPro());
         actualizarImplemento.getjComboBoxProveedor().setSelectedItem(proveedor.getNombre());
         helper.setVisible(false);
         helper = new Helper();
         helper.setLocationRelativeTo(null);
         helper.setVisible(true);
         DrawingUtilities.drawPanel(helper, helper.getViewport(), actualizarImplemento);
         helper.setTitle("Porcupine Software Portal");
     }
 
     public static void mostrarFormularioContratos() {
         helper = new Helper();
         helper.setLocationRelativeTo(null);
         agregarContrato = new AddContract();
         helper.setVisible(true);
         DrawingUtilities.drawPanel(helper, helper.getViewport(), agregarContrato);
         helper.setTitle("Porcupine Software Portal");
     }
 
     public static void mostrarFormularioWriteNotice() {
         helper = new Helper();
         helper.setLocationRelativeTo(null);
         agregarWriteNotice = new WriteNotice();
         helper.setVisible(true);
         DrawingUtilities.drawPanel(helper, helper.getViewport(), agregarWriteNotice);
         helper.setTitle("Crear comunicado...");
     }
 
     public static void mostrarFormularioEncontrarClientes() {
         helper2 = new Helper();
         helper2.setLocationRelativeTo(null);
         findClient = new FindClient();
         helper2.setVisible(true);
         DrawingUtilities.drawPanel(helper2, helper2.getViewport(), findClient);
         helper2.setTitle("Listado de Clientes...");
 
         List<ClienteVO> clientes = ServiceFactory.getInstance().getClienteService().getList();
 
         DefaultListModel model = new DefaultListModel();
 
         for (ClienteVO each : clientes) {
             model.addElement(each.toCoolString());
         }
 
 
         findClient.getjListResultados().setModel(model);
     }
 
     public static void mostrarFormularioReplyNotice() {
         helper = new Helper();
         helper.setLocationRelativeTo(null);
         crearReplyNotice = new ReplyNotice();
         helper.setVisible(true);
         DrawingUtilities.drawPanel(helper, helper.getViewport(), crearReplyNotice);
         helper.setTitle("Responder comunicado...");
     }
     
     public static void mostrarFormularioAsignarContrato() {
         helper = new Helper();
         helper.setLocationRelativeTo(null);
         asignarContrato = new AssignContract();
         helper.setVisible(true);
         DrawingUtilities.drawPanel(helper, helper.getViewport(), asignarContrato);
         helper.setTitle("Asignar contrato...");
     }
 
     public static void mostrarFormularioCrearCliente() {
         helper = new Helper();
         helper.setLocationRelativeTo(null);
         agregarCliente = new AddClient();
         helper.setVisible(true);
         DrawingUtilities.drawPanel(helper, helper.getViewport(), agregarCliente);
         helper.setTitle("Crear Cliente...");
 
         agregarCliente.getjButtonEncontrarContrato().setEnabled(false);
     }
 
     //utilidades
     public static void cerrar() {
         System.exit(0);
     }
 
     //requerimientos
     public static void login() {
         EmpleadosVO empleado = new EmpleadosVO();
         String cedulaEmpleado = login.getUsuarioTF().getText();
         String password = new String(login.getContrasenaPF().getPassword());
 
         empleado.setCedulaEmpleado(Integer.parseInt(cedulaEmpleado));
         empleado.setContraseniaEmpleado(password);
 
         EmpleadosVO empleadoLogin;
         try {
             empleadoLogin = ServiceFactory.getInstance().getEmpleadosService().login(empleado);
         } catch (DataBaseException ex) {
             reportarError(ex, login);
             return;
         }
 
         if (empleadoLogin != null) {
             switch (empleadoLogin.getRol()) {
                 //TODO set parameters
                 case TipoEmpleado.COORDINADOR_CONTRATO:
                     ContractCordination cCordination = new ContractCordination();
                     cCordination.getjLabelUsername().setText("¡Bienvenido " + empleadoLogin.getNombreEmpleado() + "!");
                     DrawingUtilities.drawPanel(psp, psp.getViewport(), cCordination);
                     break;
                 case TipoEmpleado.COORDINADOR_TECNICO_TECNOLOGICO:
                     TTCordination ttcordination = new TTCordination();
                     ttcordination.getjLabelUsername().setText("¡Bienvenido " + empleadoLogin.getNombreEmpleado() + "!");
                     DrawingUtilities.drawPanel(psp, psp.getViewport(), ttcordination);
                     break;
                 case TipoEmpleado.DIRECTOR_COMERCIAL:
 //                    AddClient addclient = new AddClient();
 //                    DrawingUtilities.drawPanel(psp, psp.getViewport(), addclient);
                     BusinessManagement bmanagement = new BusinessManagement();
                     bmanagement.getjLabelUsername().setText("¡Bienvenido " + empleadoLogin.getNombreEmpleado() + "!");
                     DrawingUtilities.drawPanel(psp, psp.getViewport(), bmanagement);
                     break;
                 case TipoEmpleado.DIRECTOR_GESTION_HUMANA:
                     HumanManagement hmanagement = new HumanManagement();
                     hmanagement.getjLabelUsername().setText("¡Bienvenido " + empleadoLogin.getNombreEmpleado() + "!");
                     DrawingUtilities.drawPanel(psp, psp.getViewport(), hmanagement);
                     break;
                 case TipoEmpleado.DIRECTOR_OPERACIONES:
                     OperationsManagement omanagement = new OperationsManagement();
                     omanagement.getjLabelUsername().setText("¡Bienvenido " + empleadoLogin.getNombreEmpleado() + "!");
                     DrawingUtilities.drawPanel(psp, psp.getViewport(), omanagement);
                     break;
                 case TipoEmpleado.SUBGERENTE:
                     BusinessManagement b2management = new BusinessManagement();
                     b2management.getjLabelUsername().setText("¡Bienvenido " + empleadoLogin.getNombreEmpleado() + "!");
                     b2management.getjButtonVerBitacora().setEnabled(true);
                     DrawingUtilities.drawPanel(psp, psp.getViewport(), b2management);
                     break;
                 //no es mi codigo mas bonito pero parece funcionar
                 case TipoEmpleado.TEMPORAL_ESCOLTA:
                     TemporaryEmployee temployee = new TemporaryEmployee();
                     temployee.getjLabelUsername().setText("¡Bienvenido " + empleadoLogin.getNombreEmpleado() + "!");
                     DrawingUtilities.drawPanel(psp, psp.getViewport(), temployee);
                     break;
                 case TipoEmpleado.TEMPORAL_GUARDA:
                     TemporaryEmployee temployee2 = new TemporaryEmployee();
                     temployee2.getjLabelUsername().setText("¡Bienvenido " + empleadoLogin.getNombreEmpleado() + "!");
                     DrawingUtilities.drawPanel(psp, psp.getViewport(), temployee2);
                     break;
                 default:
                     reportarError(new InternalErrorException("Rol erroneo"), login);
                     break;
             }
             empleadoActivo = empleadoLogin;
         } else {
             JOptionPane.showMessageDialog(login, "¡Usuario o contraseña incorrectos!", "Error", JOptionPane.ERROR_MESSAGE, null);
         }
     }
 
     public static void reportarError(Exception ex, JPanel parent) {
         int opcion = JOptionPane.showOptionDialog(parent, ex.getMessage() + "\n" + ex.getCause().getMessage(), "Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Reportar Error", "Cancelar"}, "Cancelar");
         switch (opcion) {
             case JOptionPane.OK_OPTION:
 
                 try {
                     // Conection Properties
                     Properties props = new Properties();
                     props.setProperty("mail.smtp.host", "smtp.gmail.com");
                     props.setProperty("mail.smtp.starttls.enable", "true");
                     props.setProperty("mail.smtp.port", "587");
                     props.setProperty("mail.smtp.user", "sacortesh@gmail.com");
                     props.setProperty("mail.smtp.auth", "true");
 
                     // Prepare session
                     Session session = Session.getDefaultInstance(props);
 
                     // Build Message
                     MimeMessage message = new MimeMessage(session);
                     message.setFrom(new InternetAddress("gnosisun@gmail.com"));
                     message.addRecipient(
                             Message.RecipientType.TO,
                             new InternetAddress("sacortesh@gmail.com"));
                     message.setSubject("PSP - Reporte de error");
                     message.setText(
                             "<p>Reporte de fallas</p>\n"
                             + "<p>Ha sido reportado una falla en el sistema PSP</p>\n"
                             + "<p>\n"
                             + "" + ex.getMessage()
                             + "</p>"
                             + "<p>\n"
                             + "" + ex.getStackTrace()
                             + "</p>"
                             + "<p>\n"
                             + "Reportado por usuario " + empleadoActivo.getNombreEmpleado() + " " + empleadoActivo.getApellidoEmpleado() + " @ " + empleadoActivo.getCedulaEmpleado().toString()
                             + "</p>",
                             "ISO-8859-1",
                             "html");
 
                     // Send Message
                     Transport t = session.getTransport("smtp");
                     t.connect("gnosisun@gmail.com", "4123gnosis");
                     t.sendMessage(message, message.getAllRecipients());
 
                     // Close
                     t.close();
                 } catch (MessagingException exc) {
                     JOptionPane.showMessageDialog(parent, "Oops... Error enviando el error", "Error", JOptionPane.ERROR_MESSAGE, null);
                 }
 
 
 
                 break;
             case JOptionPane.CANCEL_OPTION:
                 break;
         }
     }
 
     public static void mostrarFormularioBitacora() {
         helper2 = new Helper();
         helper2.setLocationRelativeTo(null);
         bitacora = new BitacoraDisplay();
         helper2.setVisible(true);
         DrawingUtilities.drawPanel(helper2, helper2.getViewport(), bitacora);
         helper2.setTitle("Consulta de Bitacora...");
 
 
         List<BitacoraSegVO> bitacoraList = ServiceFactory.getInstance().getBitacoraSegService().getList();
         modelTable = (DefaultTableModel) bitacora.getjTable1().getModel();
         modelTable.getDataVector().removeAllElements();
         modelTable.fireTableDataChanged();
 
         for (BitacoraSegVO implSeguridadVO : bitacoraList) {
             Object[] datos = {implSeguridadVO.getIdOper(),
                 implSeguridadVO.getFechaOper(),
                 implSeguridadVO.getUsuOper(),
                 implSeguridadVO.getMaqOper(),
                 implSeguridadVO.getTablaMod(),
                 implSeguridadVO.getTipoOper()};
             modelTable.addRow(datos);
         }
     }
 
     public static void mostrarFormularioListarEmpleados() {
         helper2 = new Helper();
         helper2.setLocationRelativeTo(null);
         findPerson = new FindPerson();
         helper2.setVisible(true);
         DrawingUtilities.drawPanel(helper2, helper2.getViewport(), findPerson);
         helper2.setTitle("Responder comunicado...");
 
         List<EmpleadosVO> empleados = ServiceFactory.getInstance().getEmpleadosService().getList();
 
         DefaultListModel model = new DefaultListModel();
 
         for (EmpleadosVO each : empleados) {
             model.addElement(each.toCoolString());
         }
 
 
         findPerson.getjListResultados().setModel(model);
 
 
 
     }
 
     public static void consultarEmpleado() {
         EmpleadosVO empleado = new EmpleadosVO();
 
         //TODO añadir la captura de todos los campos en la interfaz
         String capturedValue = (String) findPerson.getjListResultados().getSelectedValue();
         String[] splitted = capturedValue.split(" ");
         empleado.setCedulaDirector(Integer.parseInt(splitted[0]));
 
         empleado.setCedulaEmpleado(Integer.parseInt(splitted[0]));
 
         try {
             empleado = ServiceFactory.getInstance().getEmpleadosService().find(empleado.getCedulaEmpleado());
         } catch (Exception ex) {
             reportarError(ex, findPerson);
         }
 
         ArrayList<String> listaRoles = new ArrayList<String>();
         listaRoles.add(empleado.getRol());
 
         helper1 = new Helper();
         helper1.setLocationRelativeTo(null);
         crearEmpleado = new CreateEmployee(listaRoles);
         helper1.setVisible(true);
         DrawingUtilities.drawPanel(helper1, helper1.getViewport(), crearEmpleado);
         helper1.setTitle("Consulta de empleado...");
 
         crearEmpleado.getjTextFieldCC().setText(empleado.getCedulaEmpleado().toString());
         crearEmpleado.getjTextFieldCC().setEnabled(false);
         crearEmpleado.getjTextFieldNombres().setText(empleado.getNombreEmpleado());
         crearEmpleado.getjTextFieldNombres().setEnabled(false);
         crearEmpleado.getjTextFieldApellidos().setText(empleado.getApellidoEmpleado());
         crearEmpleado.getjTextFieldApellidos().setEnabled(false);
         crearEmpleado.getjTextFieldContraseña().setEnabled(false);
 
         if (empleado.getRol() == TipoEmpleado.TEMPORAL) {
             crearEmpleado.getjTextFieldDireccion().setText(empleado.getSueldoEmpleadoPlanta().toString());
         }
         crearEmpleado.getjTextFieldDireccion().setEnabled(false);
 
 
         ArrayList<String> telefonos = new ArrayList<String>();
         for (TelefonosVO each : empleado.getTelsEmpList()) {
             telefonos.add(each.getNumeroTelefonoEmpleado());
         }
 
         if (!telefonos.isEmpty()) {
             crearEmpleado.setjListTelefono(new javax.swing.JList(telefonos.toArray()));
         }
 
         crearEmpleado.getjButtonAgregar().setEnabled(false);
         crearEmpleado.getjButtonRemover().setEnabled(false);
 
         crearEmpleado.getjButtonGuardar().setText("Modificar");
 
     }
 
     /**
      * Disponible para: Director de Gestion Humana Guardias y escoltas,
      * coordinador de contrato, tecnico y operaciones Modelo gestionado por
      * vista TODO metodo para obtener la restriccion desde base de datos
      *
      */
     public static void registrarEmpleado() {
 
         EmpleadosVO empleado = new EmpleadosVO();
         empleado.setNombreEmpleado(crearEmpleado.getjTextFieldNombres().getText());
         empleado.setApellidoEmpleado(crearEmpleado.getjTextFieldApellidos().getText());
         empleado.setCedulaEmpleado(Integer.parseInt(crearEmpleado.getjTextFieldCC().getText()));
         empleado.setContraseniaEmpleado(crearEmpleado.getjTextFieldContraseña().getText());
         empleado.setRol(crearEmpleado.getjComboBoxTipoEmpleado().getSelectedItem().toString());
 
         //No hay problema en bd si es nulo
         if (getEmpleadoActivo() != null) {
             empleado.setCedulaDirector(getEmpleadoActivo().getCedulaEmpleado());
         }
         //Vos Externos
 
         if (agregarCliente.getjListTelefono().getModel().getSize() != 0) {
 
             DefaultListModel model = (DefaultListModel) crearEmpleado.getjListTelefono().getModel();
 
             ArrayList<String> tels = new ArrayList<String>();
             for (int x = 0; x < model.size(); x++) {
                 String tel = (String) model.elementAt(x);
                 tels.add(tel);
             }
 
 
 
             //Se agrega cada telefono
             List<TelefonosVO> telefonos = new ArrayList<TelefonosVO>();
             for (String each : tels) {
 
                 TelefonosVO temp = new TelefonosVO();
                 temp.setNumeroTelefonoEmpleado(each);
                 telefonos.add(temp);
 
             }
 
             empleado.setTelsEmpList(telefonos);
 
         }
 
         try {
             ServiceFactory.getInstance().getEmpleadosService().create(empleado);
         } catch (Exception ex) {
             System.out.println(ex.getMessage());
             int opcion = JOptionPane.showOptionDialog(crearEmpleado, ex.getMessage() + "\n" + ex.getCause().getMessage(), "Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Reportar Error", "Cancelar"}, "Cancelar");
             switch (opcion) {
                 case JOptionPane.OK_OPTION:
                     reportarError(ex, crearEmpleado);
                     break;
                 case JOptionPane.CANCEL_OPTION:
                     break;
             }
             return;
         }
 
     }
 
     public static void crearCliente() {
 
         ClienteVO cliente = new ClienteVO();
 
 
         cliente.setCedulaDirector(empleadoActivo.getCedulaEmpleado());
         cliente.setDireccionCliente(agregarCliente.getjTextFieldDireccion().getText());
         cliente.setFechaRegCliente(new Date());
         //No debería ser necesario
         //cliente.setIdCliente(Short.MIN_VALUE);
         cliente.setNombreCliente(agregarCliente.getjTextFieldNombre().getText());
 
         if (agregarCliente.getjListTelefono().getModel().getSize() != 0) {
 
             DefaultListModel model = (DefaultListModel) agregarCliente.getjListTelefono().getModel();
 
             ArrayList<String> tels = new ArrayList<String>();
             for (int x = 0; x < model.size(); x++) {
                 String tel = (String) model.elementAt(x);
                 tels.add(tel);
             }
 
             //Se agrega cada telefono
             List<TelefonosVO> telefonos = new ArrayList<TelefonosVO>();
             for (String each : tels) {
 
                 TelefonosVO temp = new TelefonosVO();
                 temp.setNumeroTelefonoEmpleado(each);
                 telefonos.add(temp);
 
             }
 
             cliente.setTelsCliList(telefonos);
 
         }
 
 
 
         try {
             ServiceFactory.getInstance().getClienteService().create(cliente);
         } catch (Exception ex) {
             System.out.println(ex.getMessage());
             int opcion = JOptionPane.showOptionDialog(agregarCliente, ex.getMessage() + "\n" + ex.getCause().getMessage(), "Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Reportar Error", "Cancelar"}, "Cancelar");
             switch (opcion) {
                 case JOptionPane.OK_OPTION:
                     reportarError(ex, agregarCliente);
                     break;
                 case JOptionPane.CANCEL_OPTION:
                     break;
             }
             return;
         }
 
     }
 
     public static void saveConnectionValues() {
         //TODO edit with values obtained from the view
 
         String susername = username;
         String spassword = password;
         Map dbprops = new HashMap();
 
         //http://java-persistence.blogspot.com/2008/02/testing-eclipselink-jpa-in-javase.html
 
         dbprops.put("javax.persistence.jdbc.user", susername);
         dbprops.put("javax.persistence.jdbc.password", spassword);
 
         if (selectedDB == ServidoresDisponibles.SQL) {
 
             dbprops.put("javax.persistence.jdbc.driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
             dbprops.put("javax.persistence.jdbc.url", "jdbc:sqlserver://168.176.36.26:1433;databaseName=dbd_2");
         } else if (selectedDB == ServidoresDisponibles.SYB) {
 
             dbprops.put("javax.persistence.jdbc.driver", "net.sourceforge.jtds.jdbc.Driver");
             dbprops.put("javax.persistence.jdbc.url", "jdbc:jtds:sybase://168.176.36.25:8101/dbd_2");
         } else {
 
             dbprops.put("javax.persistence.jdbc.driver", "oracle.jdbc.OracleDriver");
             dbprops.put("javax.persistence.jdbc.url", "jdbc:oracle:thin:@168.176.36.14:1521:UNBDS7");
         }
 
         dbprops.put("eclipselink.logging.level", "FINEST");
         connectionPropierties = dbprops;
     }
 
     /**
      * Disponible para: Coordinador tecnico y tecnologico Modelo gestionado por
      * vista TODO metodo para obtener la restriccion desde base de datos Vista
      * principal para este coordinador
      */
     public static void listarInventario() {
     }
 
     //IMPLEMENTOS - INICIO
     public static void crearImplemento() {
         ImplSeguridadVO implSeguridadVO = new ImplSeguridadVO();
         implSeguridadVO.setIdImplemento(new Integer(1).shortValue());
         implSeguridadVO.setNombreI(registrarImplemento.getjTextFieldNombre().getText());
         implSeguridadVO.setPrecioUnitarioI(new BigDecimal(registrarImplemento.getjTextFieldValorUnitario().getText()));
         implSeguridadVO.setCantidad(new Short(registrarImplemento.getjTextFieldCantidad().getText()));
         implSeguridadVO.setEstadoI(registrarImplemento.getjComboBoxEstado().getSelectedItem().toString());
         implSeguridadVO.setFechaRegIm(new Date());
         implSeguridadVO.setDescripcionI(registrarImplemento.getjTextAreaDescripcion().getText());
         String nombreProveedor = registrarImplemento.getjComboBoxProveedor().getSelectedItem().toString();
         Short idProveedor = ServiceFactory.getInstance().getProveedorService().findName(nombreProveedor);
         implSeguridadVO.setIdPro(idProveedor);
         implSeguridadVO.setCedulaCoordTyT(empleadoActivo.getCedulaEmpleado());
         try {
             ServiceFactory.getInstance().getImplSeguridadService().create(implSeguridadVO);
         } catch (Exception e) {
             JOptionPane.showMessageDialog(registrarImplemento, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
         JOptionPane.showMessageDialog(registrarImplemento, "¡Implemento agregado satisfactoriamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
         secondary.setVisible(false);
         secondary = new Secondary();
     }
 
     public static List<String> obtenerListaProveedores() {
         List<ProveedorVO> listaProveedores = ServiceFactory.getInstance().getProveedorService().getList();
         List<String> lista = new ArrayList<>();
         for (ProveedorVO proveedor : listaProveedores) {
             lista.add(proveedor.getNombre());
         }
         return lista;
     }
 
     public static List<String> obtenerListaEmpTemp() {
         List<EmpTempVO> listaEmpTemp = ServiceFactory.getInstance().getEmpTempService().getList();
         List<Integer> listaCedulas = new ArrayList<>();
         for (EmpTempVO empTemp : listaEmpTemp) {
             listaCedulas.add(empTemp.getCedulaEmpleado());
         }
         List<EmpleadosVO> listaEmpleados = new ArrayList<>();
         for (Integer cedula : listaCedulas) {
             listaEmpleados.add(ServiceFactory.getInstance().getEmpleadosService().find(cedula));
         }
         List<String> empleados = new ArrayList<>();
         for (EmpleadosVO empleadoVO : listaEmpleados) {
             empleados.add(empleadoVO.getCedulaEmpleado().toString() + " - " + empleadoVO.getApellidoEmpleado() + " " + empleadoVO.getNombreEmpleado());
         }
         return empleados;
     }
 
     public static Short obtenerCantidadImplementos(String nombreImplemento) {
         List<ImplSeguridadVO> implementos = ServiceFactory.getInstance().getImplSeguridadService().findByName(nombreImplemento);
         ImplSeguridadVO implemento = implementos.get(0);
         Short cantidad = implemento.getCantidad();
         return cantidad;
     }
 
     public static List<String> obtenerListaImplementos() {
         List<ImplSeguridadVO> implementosVO = ServiceFactory.getInstance().getImplSeguridadService().getList();
         List<String> lista = new ArrayList<>();
         for (ImplSeguridadVO implemento : implementosVO) {
             lista.add(implemento.getNombreI());
         }
         return lista;
     }
 
     public static void llenarTabla() {
         List<ImplSeguridadVO> implementosList = ServiceFactory.getInstance().getImplSeguridadService().findByName(eliminarImplemento.getjTextFieldBuscar().getText());
         modelTable = (DefaultTableModel) eliminarImplemento.getjTableBusqueda().getModel();
         modelTable.getDataVector().removeAllElements();
         modelTable.fireTableDataChanged();
 
         for (ImplSeguridadVO implSeguridadVO : implementosList) {
             Object[] datos = {new Short(implSeguridadVO.getIdImplemento()),
                 implSeguridadVO.getNombreI(),
                 implSeguridadVO.getPrecioUnitarioI(),
                 new Short(implSeguridadVO.getCantidad()),
                 implSeguridadVO.getEstadoI(),
                 implSeguridadVO.getFechaRegIm().toString()};
             modelTable.addRow(datos);
         }
     }
 
     public static void llenarTablaBitacora() {
     }
 
     public static void actualizarEmpleado() {
         EmpleadosVO empleado = new EmpleadosVO();
         empleado.setNombreEmpleado(crearEmpleado.getjTextFieldNombres().getText());
         empleado.setApellidoEmpleado(crearEmpleado.getjTextFieldApellidos().getText());
         empleado.setCedulaEmpleado(Integer.parseInt(crearEmpleado.getjTextFieldCC().getText()));
         empleado.setContraseniaEmpleado(crearEmpleado.getjTextFieldContraseña().getText());
         empleado.setRol(crearEmpleado.getjComboBoxTipoEmpleado().getSelectedItem().toString());
 
         //No hay problema en bd si es nulo
         if (getEmpleadoActivo() != null) {
             empleado.setCedulaDirector(getEmpleadoActivo().getCedulaEmpleado());
         }
         //Vos Externos
 
         if (agregarCliente.getjListTelefono().getModel().getSize() != 0) {
 
             DefaultListModel model = (DefaultListModel) crearEmpleado.getjListTelefono().getModel();
 
             ArrayList<String> tels = new ArrayList<String>();
             for (int x = 0; x < model.size(); x++) {
                 String tel = (String) model.elementAt(x);
                 tels.add(tel);
             }
 
 
 
             //Se agrega cada telefono
             List<TelefonosVO> telefonos = new ArrayList<TelefonosVO>();
             for (String each : tels) {
 
                 TelefonosVO temp = new TelefonosVO();
                 temp.setNumeroTelefonoEmpleado(each);
                 telefonos.add(temp);
 
             }
 
             empleado.setTelsEmpList(telefonos);
 
         }
 
         try {
             ServiceFactory.getInstance().getEmpleadosService().update(empleado);
         } catch (Exception ex) {
             System.out.println(ex.getMessage());
             int opcion = JOptionPane.showOptionDialog(crearEmpleado, ex.getMessage() + "\n" + ex.getCause().getMessage(), "Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Reportar Error", "Cancelar"}, "Cancelar");
             switch (opcion) {
                 case JOptionPane.OK_OPTION:
                     reportarError(ex, crearEmpleado);
                     break;
                 case JOptionPane.CANCEL_OPTION:
                     break;
             }
             return;
         }
     }
 
     public static void actualizarCliente() {
         ClienteVO cliente = new ClienteVO();
 
 
         cliente.setCedulaDirector(empleadoActivo.getCedulaEmpleado());
         cliente.setDireccionCliente(agregarCliente.getjTextFieldDireccion().getText());
         cliente.setFechaRegCliente(new Date());
         cliente.setIdCliente(new Short(agregarCliente.getjTextFieldIdCliente().getText()));
         //No debería ser necesario
         //cliente.setIdCliente(Short.MIN_VALUE);
         cliente.setNombreCliente(agregarCliente.getjTextFieldNombre().getText());
 
         if (agregarCliente.getjListTelefono().getModel().getSize() != 0) {
 
             DefaultListModel model = (DefaultListModel) agregarCliente.getjListTelefono().getModel();
 
             ArrayList<String> tels = new ArrayList<String>();
             for (int x = 0; x < model.size(); x++) {
                 String tel = (String) model.elementAt(x);
                 tels.add(tel);
             }
 
             //Se agrega cada telefono
             List<TelefonosVO> telefonos = new ArrayList<TelefonosVO>();
             for (String each : tels) {
 
                 TelefonosVO temp = new TelefonosVO();
                 temp.setNumeroTelefonoEmpleado(each);
                 telefonos.add(temp);
 
             }
 
             cliente.setTelsCliList(telefonos);
 
         }
 
 
 
         try {
             ServiceFactory.getInstance().getClienteService().update(cliente);
         } catch (Exception ex) {
             System.out.println(ex.getMessage());
             int opcion = JOptionPane.showOptionDialog(agregarCliente, ex.getMessage() + "\n" + ex.getCause().getMessage(), "Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Reportar Error", "Cancelar"}, "Cancelar");
             switch (opcion) {
                 case JOptionPane.OK_OPTION:
                     reportarError(ex, agregarCliente);
                     break;
                 case JOptionPane.CANCEL_OPTION:
                     break;
             }
             return;
         }
     }
 
     public static void consultarCliente() {
         ClienteVO empleado = new ClienteVO();
 
         //TODO añadir la captura de todos los campos en la interfaz
         String capturedValue = (String) findClient.getjListResultados().getSelectedValue();
         String[] splitted = capturedValue.split(" ");
         empleado.setIdCliente(new Short(splitted[0]));
 
 
         try {
             empleado = ServiceFactory.getInstance().getClienteService().find(empleado.getIdCliente());
         } catch (Exception ex) {
             reportarError(ex, findClient);
         }
 
         helper1 = new Helper();
         helper1.setLocationRelativeTo(null);
         agregarCliente = new AddClient();
         helper1.setVisible(true);
         DrawingUtilities.drawPanel(helper1, helper1.getViewport(), agregarCliente);
         helper1.setTitle("Consulta de cliente...");
 
         agregarCliente.getjTextFieldDireccion().setText(empleado.getDireccionCliente());
         agregarCliente.getjTextFieldDireccion().setEnabled(false);
         agregarCliente.getjTextFieldNombre().setText(empleado.getNombreCliente());
         agregarCliente.getjTextFieldNombre().setEnabled(false);
 
 
 
         ArrayList<String> telefonos = new ArrayList<String>();
         for (TelefonosVO each : empleado.getTelsCliList()) {
             telefonos.add(each.getNumeroTelefonoEmpleado());
         }
 
         if (!telefonos.isEmpty()) {
             agregarCliente.setjListTelefono(new javax.swing.JList(telefonos.toArray()));
         }
 
         agregarCliente.getjTextFieldIdCliente().setText(empleado.getIdCliente().toString());
         agregarCliente.getjButtonAgregar().setEnabled(false);
         agregarCliente.getjButtonRemover().setEnabled(false);
 
         agregarCliente.getjButtonGuardar().setText("Modificar");
     }
 
     public void listarImplementos() {
         secondary.setVisible(true);
         secondary.setTitle("Eliminar Implemento");
         DrawingUtilities.drawPanel(secondary, secondary.getViewport(), eliminarImplemento);
         llenarTabla();
     }
 
     public static void busquedaCompleta() {
         modelTable = (DefaultTableModel) eliminarImplemento.getjTableBusqueda().getModel();
         modelTable.getDataVector().removeAllElements();
         modelTable.fireTableDataChanged();
         List<ImplSeguridadVO> implementos;
         implementos = ServiceFactory.getInstance().getImplSeguridadService().findByName(eliminarImplemento.getjTextFieldBuscar().getText());
         if (implementos.isEmpty()) {
             eliminarImplemento.getjButtonEliminar().setEnabled(false);
             JOptionPane.showMessageDialog(eliminarImplemento, "¡No se han encontrado resultados!", "Advertencia", JOptionPane.INFORMATION_MESSAGE);
         } else {
             eliminarImplemento.getjButtonEliminar().setEnabled(true);
         }
         for (ImplSeguridadVO implementoVO : implementos) {
             Object[] datos = {new Short(implementoVO.getIdImplemento()),
                 implementoVO.getNombreI(),
                 implementoVO.getPrecioUnitarioI(),
                 new Short(implementoVO.getCantidad()),
                 implementoVO.getEstadoI(),
                 implementoVO.getFechaRegIm().toString()};
             modelTable.addRow(datos);
         }
         modelTable.fireTableDataChanged();
     }
 
     public static void busquedaSencilla() {
         modelTable = (DefaultTableModel) listaActualizarImplementos.getjTableBusqueda().getModel();
         modelTable.getDataVector().removeAllElements();
         modelTable.fireTableDataChanged();
         List<ImplSeguridadVO> implementos;
         implementos = ServiceFactory.getInstance().getImplSeguridadService().findByName(listaActualizarImplementos.getjTextFieldBuscar().getText());
         if (implementos.isEmpty()) {
             listaActualizarImplementos.getjButtonContinuar().setEnabled(false);
             JOptionPane.showMessageDialog(listaActualizarImplementos, "¡No se han encontrado resultados!", "Advertencia", JOptionPane.INFORMATION_MESSAGE);
         } else {
             listaActualizarImplementos.getjButtonContinuar().setEnabled(true);
         }
         for (ImplSeguridadVO implementoVO : implementos) {
             Object[] datos = {new Short(implementoVO.getIdImplemento()), implementoVO.getNombreI()};
             modelTable.addRow(datos);
         }
         modelTable.fireTableDataChanged();
     }
 
     public static void actualizarImplemento() {
         ImplSeguridadVO implSeguridadVO = new ImplSeguridadVO();
         implSeguridadVO.setIdImplemento(new Short(actualizarImplemento.getjTextFieldId().getText()));
         implSeguridadVO.setNombreI(actualizarImplemento.getjTextFieldNombre().getText());
         implSeguridadVO.setPrecioUnitarioI(new BigDecimal(actualizarImplemento.getjTextFieldValorUnitario().getText()));
         implSeguridadVO.setCantidad(new Short(actualizarImplemento.getjTextFieldCantidad().getText()));
         implSeguridadVO.setEstadoI(actualizarImplemento.getjComboBoxEstado().getSelectedItem().toString());
         implSeguridadVO.setFechaRegIm(new Date());
         implSeguridadVO.setDescripcionI(actualizarImplemento.getjTextAreaDescripcion().getText());
         String nombreProveedor = actualizarImplemento.getjComboBoxProveedor().getSelectedItem().toString();
         Short idProveedor = ServiceFactory.getInstance().getProveedorService().findName(nombreProveedor);
         implSeguridadVO.setIdPro(idProveedor);
         implSeguridadVO.setCedulaCoordTyT(empleadoActivo.getCedulaEmpleado());
         try {
             ServiceFactory.getInstance().getImplSeguridadService().update(implSeguridadVO);
         } catch (NonexistentEntityException ex) {
             Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (RequiredAttributeException ex) {
             Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InvalidAttributeException ex) {
             Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InsufficientPermissionsException ex) {
             Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
         }
         JOptionPane.showMessageDialog(actualizarImplemento, "¡Implemento actualizado satisfactoriamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
         secondary.setVisible(false);
         secondary = new Secondary();
     }
 
     public static void borrarImplemento() {
         int opcion = JOptionPane.showOptionDialog(eliminarImplemento, "¿Realmente desea eliminar el implemento?", "Confirmación", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Sí", "Cancelar"}, "Cancelar");
         switch (opcion) {
             case JOptionPane.OK_OPTION:
                 try {
                     ServiceFactory.getInstance().getImplSeguridadService().delete(new Short(eliminarImplemento.getjTableBusqueda().getValueAt(eliminarImplemento.getjTableBusqueda().getSelectedRow(), 0).toString()));
                     JOptionPane.showMessageDialog(eliminarImplemento, "¡El implemento se ha eliminado satisfactoriamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                     llenarTabla();
                 } catch (NonexistentEntityException | InsufficientPermissionsException ex) {
                     JOptionPane.showMessageDialog(eliminarImplemento, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                 }
                 break;
             case JOptionPane.CANCEL_OPTION:
                 break;
         }
     }
 
     public static void cancelar() {
         secondary.setVisible(false);
         secondary.dispose();
     }
 
     public static void asignarImplemento() {
         List<ImplSeguridadVO> implementos = ServiceFactory.getInstance().getImplSeguridadService().findByName(asignarImplementos.getjComboBoxImplemento().getSelectedItem().toString());
         ImplSeguridadVO implemento = implementos.get(0);
         Short idImplemento = implemento.getIdImplemento();
         String[] empleadoSeleccionado = asignarImplementos.getjComboBoxEmpleado().getSelectedItem().toString().split(" ");
         System.out.println(empleadoSeleccionado[0]);
         Integer idEmpleadoTemporal = Integer.parseInt(empleadoSeleccionado[0]);
         Integer idCoordinador = empleadoActivo.getCedulaEmpleado();
         Integer cantidadAsignada = Integer.parseInt(asignarImplementos.getjTextFieldCantidad().getText());
         try {
             DAOFactory.getInstance().getImplSeguridadDAO().asignarImplemento(idImplemento, idEmpleadoTemporal, idCoordinador, cantidadAsignada);
             JOptionPane.showMessageDialog(asignarImplementos, "¡La asignación del implemento al empleado " + empleadoSeleccionado[2] + ", ha sido exitosa!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
         } catch (Exception e) {
             JOptionPane.showMessageDialog(asignarImplementos, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         }
     }
 
     public static void despojarImplemento() {
         List<ImplSeguridadVO> implementos = ServiceFactory.getInstance().getImplSeguridadService().findByName(asignarImplementos.getjComboBoxImplemento().getSelectedItem().toString());
         ImplSeguridadVO implemento = implementos.get(0);
         Short idImplemento = implemento.getIdImplemento();
         String[] empleadoSeleccionado = asignarImplementos.getjComboBoxEmpleado().getSelectedItem().toString().split(" ");
         System.out.println(empleadoSeleccionado[0]);
         Integer idEmpleadoTemporal = Integer.parseInt(empleadoSeleccionado[0]);
         Integer idCoordinador = empleadoActivo.getCedulaEmpleado();
         Integer cantidadDespojada = Integer.parseInt(asignarImplementos.getjTextFieldCantidad().getText());
         try {
             DAOFactory.getInstance().getImplSeguridadDAO().despojarImplemento(idImplemento, idEmpleadoTemporal, idCoordinador, cantidadDespojada);
             JOptionPane.showMessageDialog(asignarImplementos, "¡El despojo del implemento al empleado " + empleadoSeleccionado[2] + ", ha sido exitoso!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
         } catch (Exception e) {
             JOptionPane.showMessageDialog(asignarImplementos, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         }
     }
 
     public static void adicionarImplemento() {
         List<ImplSeguridadVO> implementos = ServiceFactory.getInstance().getImplSeguridadService().findByName(adicionarImplemento.getjComboBoxImplemento().getSelectedItem().toString());
         ImplSeguridadVO implemento = implementos.get(0);
         Short idImplemento = implemento.getIdImplemento();
         Integer cantidad = Integer.parseInt(adicionarImplemento.getjTextFieldCantidadAgregada().getText());
         try {
             DAOFactory.getInstance().getImplSeguridadDAO().adicionarImplemento(idImplemento, cantidad);
             JOptionPane.showMessageDialog(adicionarImplemento, "¡Se han agregado " + cantidad + " implementos satisfactoriamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
         } catch (Exception e) {
             JOptionPane.showMessageDialog(adicionarImplemento, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         }
     }
     //IMPLEMENTOS - FIN
 
     /**
      * Disponible para: Wachiturros Vista principal para este coordinador
      */
     public static void consultarCondicionesContrato() {
     }
 
     public static void listarContratosAsignados() {
     }
 
     public static void consultarDotacion() {
     }
 
     //Coordinador de contrato
     public static void crearContrato() {
         ContratoVO contratoVO = new ContratoVO();
 
         contratoVO.setIdContrato(new Integer(1).shortValue());
         contratoVO.setTipoCont(agregarContrato.getjComboBoxTipoContrato().getSelectedItem().toString());
         contratoVO.setFechaInicioCont(agregarContrato.getjDateChooserFechaInicio().getDate());
         contratoVO.setTipoPersonalCont(agregarContrato.getjComboBoxTipoPersonal().getSelectedItem().toString());
         contratoVO.setCantPersonalCont(new Short(agregarContrato.getjTextFieldCantPerson().getText()));
         contratoVO.setCostoMensual(new BigDecimal(agregarContrato.getjTextFieldCosto().getText()));
         contratoVO.setUbicacionCont(agregarContrato.getjTextFieldUbicacion().getText());
         contratoVO.setHorarioCont(agregarContrato.getjTextFieldHorario().getText());
         contratoVO.setTiempoCont(new Integer(agregarContrato.getjTextFieldTiempo().getText()));
         contratoVO.setCelularCont(agregarContrato.getjTextFieldCelularC().getText());
         contratoVO.setTelefonoCont(agregarContrato.getjTextFieldTelefonoC().getText());
         contratoVO.setCedulaDirComer(empleadoActivo.getCedulaEmpleado());
         contratoVO.setFechaRegCont(new Date());
 
         String nombreCliente = agregarContrato.getjComboBoxCliente().getSelectedItem().toString();
         Short idCliente = ServiceFactory.getInstance().getClienteService().findName(nombreCliente);
         contratoVO.setIdCliente(idCliente);
 
         try {
             ServiceFactory.getInstance().getContratoService().create(contratoVO);
         } catch (Exception e) {
             JOptionPane.showMessageDialog(agregarContrato, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
         JOptionPane.showMessageDialog(agregarContrato, "¡Contrato agregado satisfactoriamente!", "Exito!", JOptionPane.INFORMATION_MESSAGE);
         helper.setVisible(false);
         helper.dispose(); 
     }
     
         public static void asignarContrato() {
 //        List<ContratoVO> contratos = ServiceFactory.getInstance().getContratoService().findByName(asignarContrato.getjComboBoxContract().getSelectedItem().toString());
 //        ContratoVO contrato = contratos.get(0);
 //        Short idContrato = contrato.getIdContrato();
 //        String[] empleadoSeleccionado = asignarContrato.getjComboBoxEmployee().getSelectedItem().toString().split(" ");
 //        System.out.println(empleadoSeleccionado[0]);
 //        Integer idEmpleadoTemporal = Integer.parseInt(empleadoSeleccionado[0]);
 //        Integer idCoordinador = empleadoActivo.getCedulaEmpleado();
 //        try {
 //            DAOFactory.getInstance().getContratoDAO().asignarContrato(idContrato, idEmpleadoTemporal, idCoordinador);
 //            JOptionPane.showMessageDialog(asignarImplementos, "¡La asignación del implemento al empleado " + empleadoSeleccionado[2] + ", ha sido exitosa!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
 //        } catch (Exception e) {
 //            JOptionPane.showMessageDialog(asignarImplementos, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 //        }
     }
     
     public static List<String> obtenerListaContratos() {
         List<ContratoVO> listaContratos = ServiceFactory.getInstance().getContratoService().getList();
         List<String> lista = new ArrayList<>();
         for (ContratoVO contrato : listaContratos) {
             lista.add(contrato.getUbicacionCont());
         }
         return lista;
     }
 
     public static List<String> obtenerListaClientes() {
         List<ClienteVO> listaClientes = ServiceFactory.getInstance().getClienteService().getList();
         List<String> lista = new ArrayList<>();
         for (ClienteVO cliente : listaClientes) {
             lista.add(cliente.getNombreCliente());
         }
         return lista;
     }
 
     public static void borrarContrato() {
     }
 
    public static void asignarContrato() {
    }

     public static void crearComunicacion() {
         ComunicadoVO comunicadoVO = new ComunicadoVO();
 
         comunicadoVO.setIdComunicado(new Integer(1).shortValue());
         comunicadoVO.setTipoCom(agregarWriteNotice.getjComboBoxTipo().getSelectedItem().toString());
         comunicadoVO.setContenidoCom(agregarWriteNotice.getjTextAreaComunicado().getText());
         comunicadoVO.setUrgente(agregarWriteNotice.getjCheckBoxUrgente().isSelected());
         comunicadoVO.setFechaCom(new Date());
         comunicadoVO.setCedulaEmpTemp(empleadoActivo.getCedulaEmpleado());
 
         try {
             ServiceFactory.getInstance().getComunicadoService().create(comunicadoVO);
         } catch (Exception e) {
             JOptionPane.showMessageDialog(agregarWriteNotice, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
         JOptionPane.showMessageDialog(agregarWriteNotice, "¡Comunicado agregado satisfactoriamente!", "Exito!", JOptionPane.INFORMATION_MESSAGE);
 //      helper.setVisible(false);
 //      helper.dispose();
     }
 
     public static void listarComunicacion() {
     }
 }
