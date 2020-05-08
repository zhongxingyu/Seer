 package gui.Listados;
 
 import gui.ListadosInterface;
 import gui.Util;
 
 import java.util.Vector;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.Screen;
 import net.rim.device.api.ui.UiApplication;
 import persistence.Persistence;
 import core.Persona;
 import core.PhoneManager;
 import core.Preferencias;
 
 public class ListadoPersonas {
 
 	private Vector _vectorPersonas;
 	private ListadosInterface _screen;
 	private long _style;
 	private Persona _selected;
 	private int _tipo;
 
 	public static final int ON_CLICK_VER = 2;
 	public static final int ON_CLICK_SELECT = 4;
 	public static final int NO_NUEVO = 8;
 
 	public ListadoPersonas(int tipo) {
 		this(tipo, false, 0);
 	}
 
 	public ListadoPersonas(int tipo, boolean popup) {
 		this(tipo, popup, 0);
 	}
 
 	public ListadoPersonas(int tipo, long style) {
 		this(tipo, false, style);
 	}
 
 	public ListadoPersonas(int tipo, boolean popup, long style) {
 		_style = style;
 		_tipo = tipo;
 		if (popup) {
 			_screen = new ListadoPersonasPopUp();
 		} else {
 			_screen = new ListadoPersonasScreen();
 		}
 
 		try {
 			if (_tipo == 1) {
 				_vectorPersonas = new Persistence().consultarDemandantes();
 			} else {
 				_vectorPersonas = new Persistence().consultarDemandados();
 			}
 		} catch (NullPointerException e) {
 			Util.noSd();
 		} catch (Exception e) {
 			Util.alert(e.toString());
 		}
 
 		addPersonas();
 		((Screen) _screen).setChangeListener(listener);
 
 		if (Preferencias.isMostrarCampoBusqueda()) {
 			_screen.setSearchField();
 		}
 		if ((_style & NO_NUEVO) != NO_NUEVO) {
 			if (_tipo == 1) {
 				_screen.addElement("Crear nuevo demandante", 0);
 			} else {
 				_screen.addElement("Crear nuevo demandado", 0);
 			}
 		}
 		if ((_style & ON_CLICK_VER) != ON_CLICK_VER
 				&& (_style & ON_CLICK_SELECT) != ON_CLICK_SELECT) {
 			if (popup) {
 				_style = _style | ON_CLICK_SELECT;
 			} else {
 				_style = _style | ON_CLICK_VER;
 			}
 		}
 		if (Preferencias.isMostrarTitulosPantallas() && !popup) {
 			if (tipo == 1) {
				_screen.setTitle("Demandanes");
 			} else {
 				_screen.setTitle("Demandados");
 			}
 		}
 	}
 
 	private FieldChangeListener listener = new FieldChangeListener() {
 
 		public void fieldChanged(Field field, int context) {
 			if (context == Util.CLICK) {
 				onClick();
 			} else if (context == Util.VER_ELEMENTO) {
 				verPersona();
 			} else if (context == Util.CERRAR) {
 				cerrarPantalla();
 			} else if (context == Util.ELIMINAR) {
 				eliminarPersona();
 			} else if (context == Util.LLAMAR) {
 				llamar();
 			} else if (context == Util.NEW) {
 				nuevaPersona();
 			}
 		}
 	};
 
 	private void llamar() {
 		Object selected = _screen.getSelected();
 		if (!String.class.equals(selected)) {
 			if (!((Persona) selected).getTelefono().equals("")) {
 				PhoneManager.call(((Persona) selected).getTelefono());
 			} else {
 				Util.alert("Esta persona no tiene telfono registrado");
 			}
 		}
 	}
 
 	private void addPersonas() {
 		if (_vectorPersonas != null) {
 			_screen.loadFrom(_vectorPersonas);
 		}
 	}
 
 	public Persona getSelected() {
 		return _selected;
 	}
 
 	public Screen getScreen() {
 		return (Screen) _screen;
 
 	}
 
 	public void onClick() {
 		try {
 			if (String.class.isInstance(_screen.getSelected())) {
 				nuevaPersona();
 			} else {
 				if ((_style & ON_CLICK_VER) == ON_CLICK_VER) {
 					verPersona();
 				} else {
 					_selected = (Persona) _screen.getSelected();
 					UiApplication.getUiApplication()
 							.popScreen((Screen) _screen);
 				}
 			}
 		} catch (Exception e) {
 		}
 	}
 
 	private void nuevaPersona() {
 		Persona persona = Util.nuevaPersona(_tipo);
 		if (persona != null) {
 			if ((_style & NO_NUEVO) == NO_NUEVO) {
 				_screen.addElement(persona, 0);
 			} else {
 				_screen.addElement(persona, 1);
 			}
 		}
 	}
 
 	private void verPersona() {
 		Persona selected = (Persona) _screen.getSelected();
 		Persona persona = Util.verPersona(selected);
 		if (persona != null) {
 			_screen.replace(selected, persona);
 		} else {
 			_screen.remove(selected);
 		}
 	}
 
 	private void eliminarPersona() {
 		Object[] ask = { "Aceptar", "Cancelar" };
 		int sel = _screen.ask(ask, Util.delBDPersona(), 1);
 		if (sel == 0) {
 			Persona selected = (Persona) _screen.getSelected();
 			try {
 				new Persistence().borrarPersona(selected);
 			} catch (NullPointerException e) {
 				_screen.alert(Util.noSDString());
 				System.exit(0);
 			} catch (Exception e) {
 				_screen.alert(e.toString());
 			}
 			_screen.remove(selected);
 		}
 	}
 
 	private void cerrarPantalla() {
 		_selected = null;
 		UiApplication.getUiApplication().popScreen((Screen) _screen);
 	}
 
 	public String toString() {
 		if (_tipo == 1) {
 			return "Lista de demandantes";
 		} else if (_tipo == 2) {
 			return "Lista de demandados";
 		} else {
 			return "Lista de personas";
 		}
 	}
 }
