 package gui.Nuevos;
 
 import gui.Util;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.UiApplication;
 import persistence.Persistence;
 import core.Persona;
 
 public class NuevaPersona {
 	private Persona _persona;
 	private NuevaPersonaScreen _screen;
 	private int _tipo;
 
 	/**
 	 * @param tipo
 	 *            Se crea una NuevaPersona, con un tipo: 1 para demandante 2
 	 *            para demandado
 	 */
 	public NuevaPersona(int tipo) {
 		_tipo = tipo;
 		_screen = new NuevaPersonaScreen();
 		_screen.setChangeListener(listener);
 		if (tipo == 1) {
 			_screen.setTitle("Nuevo demandante");
 		} else {
 			_screen.setTitle("Nuevo demandado");
 		}
 	}
 
 	FieldChangeListener listener = new FieldChangeListener() {
 
 		public void fieldChanged(Field field, int context) {
 			if (context == _screen.GUARDAR) {
 				guardarPersona();
 			} else if (context == _screen.CERRAR) {
 				cerrarPantalla();
 			}
 		}
 	};
 
 	/**
 	 * @return La pantalla asociada al objeto
 	 */
 	public NuevaPersonaScreen getScreen() {
 		return _screen;
 	}
 
 	/**
 	 * @return la nueva Persona creada, s esta no ha sido guardada previamente
 	 *         con guardarPersona(); se invoca dicho metodo
 	 */
 	public Persona getPersona() {
 		return _persona;
 	}
 
 	/**
 	 * Crea el nuevo objeto Persona, y lo guarda en la base de datos usando la
 	 * informacion capturada desde la pantalla
 	 */
 	private void guardarPersona() {
 
 		if (_screen.getNombre().equals("")) {
 			_screen.alert("El campo Nombre es obligatorio");
 		} else if (_screen.getTelefono().equals("")) {
 			Object[] ask = { "Guardar", "Cancelar" };
 			int sel = _screen.ask(ask,
 					"El campo Telfono se considera importante", 1);
 			if (sel == 0) {
 				guardar();
 			}
 		} else {
 			guardar();
 		}
 	}
 
 	private void guardar() {
		Util.pushWaitScreen();
 		UiApplication.getUiApplication().invokeLater(new Runnable() {
 
 			public void run() {
 				_persona = new Persona(_tipo, _screen.getCedula(), _screen
 						.getNombre(), _screen.getTelefono(), _screen
 						.getDireccion(), _screen.getCorreo(), _screen
 						.getNotas());
 				try {
 					new Persistence().guardarPersona(_persona);
 				} catch (NullPointerException e) {
 					Util.noSd();
 				} catch (Exception e) {
 					Util.alert(e.toString());
 				} finally {
 					Util.popScreen(_screen);
 					Util.popWaitScreen();
 				}
 			}
 		});
 	}
 
 	private void cerrarPantalla() {
 		if (_screen.isDirty()) {
 			Object[] ask = { "Guardar", "Descartar", "Cancelar" };
 			int sel = _screen.ask(ask, "Se han detectado cambios", 2);
 			if (sel == 0) {
 				guardarPersona();
 			} else if (sel == 1) {
 				UiApplication.getUiApplication().popScreen(_screen);
 			}
 		} else {
 			UiApplication.getUiApplication().popScreen(_screen);
 		}
 	}
 }
