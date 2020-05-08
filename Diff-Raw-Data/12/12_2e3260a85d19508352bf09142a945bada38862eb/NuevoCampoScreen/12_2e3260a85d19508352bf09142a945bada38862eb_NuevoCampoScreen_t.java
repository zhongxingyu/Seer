 package gui;
 
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.BasicEditField;
 import net.rim.device.api.ui.component.CheckboxField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 
 public class NuevoCampoScreen extends FondoNormal {
 
 	/**
 	 * Crea una nueva ventana para la creacin de un nuevo campo personalizado
 	 */
 	private BasicEditField _txtNombre;
 	private CheckboxField _cbObligatorio;
 	private BasicEditField _txtLongMax;
 	private BasicEditField _txtLongMin;
 
	private boolean _guardar = false;
 
 	public NuevoCampoScreen() {
 
 		setTitle("Nuevo campo personalizado");
 		VerticalFieldManager vertical = new VerticalFieldManager();
 
 		_txtNombre = new BasicEditField(BasicEditField.NO_NEWLINE);
 		_txtNombre.setLabel("Nombre: ");
 
 		_cbObligatorio = new CheckboxField(" Obligatorio", false);
 
 		_txtLongMax = new BasicEditField(BasicEditField.NO_NEWLINE
 				| BasicEditField.FILTER_INTEGER);
 		_txtLongMax.setLabel("Longitud mxima: ");
 
 		_txtLongMin = new BasicEditField(BasicEditField.NO_NEWLINE
 				| BasicEditField.FILTER_INTEGER);
 		_txtLongMin.setLabel("Longitud mnima: ");
 
 		vertical.add(_txtNombre);
 		vertical.add(_cbObligatorio);
 		vertical.add(_txtLongMax);
 		vertical.add(_txtLongMin);
 
 		add(vertical);
 		addMenuItem(menuGuardar);
 	}
 
 	private final MenuItem menuGuardar = new MenuItem("Guardar", 0, 0) {
 
 		public void run() {
 			// TODO Auto-generated method stub
			if (getLongMax() >= getLongMin()) {
				_guardar = true;
 				UiApplication.getUiApplication().popScreen(getScreen());
			}
			else {
 				Dialog.alert("La longitud mnima no puede ser mayor que la longitud mxima, corrija y guarde nuevamente");
			}
 		}
 	};
 
 	public String getNombre() {
 		return _txtNombre.getText();
 	}
 
 	public Boolean isObligatorio() {
 		return new Boolean(_cbObligatorio.getChecked());
 	}
 
 	public int getLongMax() {
 		int lon;
 		try {
 			lon = Integer.parseInt(_txtLongMax.getText());
 		} catch (NumberFormatException e) {
 			return 0;
 		}
 		return lon;
 	}
 
 	public int getLongMin() {
 		int lon;
 		try {
 			lon = Integer.parseInt(_txtLongMin.getText());
 		} catch (NumberFormatException e) {
 			return 0;
 		}
 		return lon;
 	}
 
 	public boolean isGuardado() {
 		return _guardar;
 	}
 
 	public boolean onClose() {
 		if (_txtNombre.getTextLength() == 0 && _txtLongMax.getTextLength() == 0
 				&& _txtLongMin.getTextLength() == 0) {
 			UiApplication.getUiApplication().popScreen(getScreen());
 			return true;
 		} else {
 			Object[] ask = { "Guardar", "Descartar", "Cancelar" };
 			int sel = Dialog.ask("Se han detectado cambios", ask, 2);
 			if (sel == 0) {
 				_guardar = true;
 				UiApplication.getUiApplication().popScreen(getScreen());
 				return true;
 			}
 			if (sel == 1) {
 				UiApplication.getUiApplication().popScreen(getScreen());
 				return true;
 			}
 			if (sel == 2) {
 				return false;
 			} else
 				return false;
 		}
 	}
 }
