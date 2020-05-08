 package gui;
 
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.SeparatorField;
 
 public class NuevaPersona extends FondoNuevos {
 
 	private int _tipo;
 	private BasicEditField _txtNombre;
 	private BasicEditField _txtCedula;
 	private BasicEditField _txtDireccion;
 	private BasicEditField _txtTelefono;
 	private BasicEditField _txtCorreo;
 	private BasicEditField _txtNotas;
 
 	public NuevaPersona(int tipo) {
 		// TODO Auto-generated constructor stub
 		super();
 		if (tipo == 1)
 			setTitle("Nuevo demandante");
 		else
 			setTitle("Nuevo demandado");
 
 		// Se inicializan con el estilo
 		_txtNombre = new BasicEditField(BasicEditField.NO_NEWLINE);
 		_txtNombre.setLabel("Nombre: ");
 		_txtCedula = new BasicEditField(BasicEditField.NO_NEWLINE);
 		_txtCedula.setLabel("Id: ");
 		_txtDireccion = new BasicEditField(BasicEditField.NO_NEWLINE);
 		_txtDireccion.setLabel("Direccin: ");
 		_txtNotas = new BasicEditField();
 		_txtNotas.setLabel("Notas: ");
 		_txtTelefono = new BasicEditField(BasicEditField.NO_NEWLINE);
 		_txtTelefono.setLabel("Telfono: ");
 		_txtCorreo = new BasicEditField(BasicEditField.NO_NEWLINE);
 		_txtCorreo.setLabel("Correo: ");
 		// Se agregan los elementos a la pantalla
 
 		_vertical.add(_txtNombre);
		_vertical.add(new SeparatorField());
 		_vertical.add(_txtCedula);
		_vertical.add(new SeparatorField());
 		_vertical.add(_txtDireccion);
		_vertical.add(new SeparatorField());
 		_vertical.add(_txtTelefono);
		_vertical.add(new SeparatorField());
 		_vertical.add(_txtCorreo);
		_vertical.add(new SeparatorField());
 		_vertical.add(_txtNotas);
 		add(_vertical);
 		addMenuItem(menuGuardar);
 	}
 
 	private final MenuItem menuGuardar = new MenuItem("Guardar", 0, 0) {
 
 		public void run() {
 			// TODO Auto-generated method stub
 			UiApplication.getUiApplication().popScreen(getScreen());
 		}
 	};
 
 	public int getTipo() {
 		return _tipo;
 	}
 
 	public String getNombre() {
 		return _txtNombre.getText();
 	}
 
 	public String getCedula() {
 		return _txtCedula.getText();
 	}
 
 	public String getDireccion() {
 		return _txtDireccion.getText();
 	}
 
 	public String getTelefono() {
 		return _txtTelefono.getText();
 	}
 
 	public String getCorreo() {
 		return _txtCorreo.getText();
 	}
 
 	public String getNotas() {
 		return _txtNotas.getText();
 	}
 	
 	public boolean onClose() {
 		UiApplication.getUiApplication().popScreen(getScreen());
 		return true;
 	}
 }
