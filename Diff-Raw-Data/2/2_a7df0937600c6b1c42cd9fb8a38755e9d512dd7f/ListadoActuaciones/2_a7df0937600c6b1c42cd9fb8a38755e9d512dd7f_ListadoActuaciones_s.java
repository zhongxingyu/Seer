 package gui;
 
 import java.util.Vector;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.Screen;
 import net.rim.device.api.ui.UiApplication;
 import persistence.Persistence;
 import core.Actuacion;
 import core.Proceso;
 
 public class ListadoActuaciones {
 
 	private Vector _vectorActuaciones;
 	private ListadosInterface _screen;
 	private long _style;
 	private Actuacion _selected;
 	private Proceso _proceso;
 	
 	public static final int SEARCH = 1;
 	public static final int ON_CLICK_VER = 2;
 	public static final int ON_CLICK_SELECT = 4;
 	public static final int NO_NUEVO = 8;
 	
 	public ListadoActuaciones(Proceso proceso) {
 		this(proceso ,false, 0);
 	}
 	
 	public ListadoActuaciones(Proceso proceso, boolean popup) {
 		this(proceso ,popup, 0);
 	}
 	
 	public ListadoActuaciones(Proceso proceso, long style) {
 		this(proceso, false, style);
 	}
 
 	public ListadoActuaciones(Proceso proceso, boolean popup, long style) {
 		_style = style;
 		_proceso = proceso;
 		if(popup) {
 			_screen = new ListadoActuacionesPopUp();
 		}
 		else {
 			_screen = new ListadoActuacionesScreen();
 		}
 		
 		try {
 			_vectorActuaciones = new Persistence().consultarActuaciones(_proceso);
 		} catch(NullPointerException e) {
 			_screen.alert(Util.noSDString());
 			System.exit(0);
 		} catch (Exception e) {
 			_screen.alert(e.toString());
 		}
 		
 		addActuaciones();
 		((Screen)_screen).setChangeListener(listener);
 		
 		if((_style & SEARCH) == SEARCH) {
 			_screen.setSearchField();
 		}
		if((_style & SEARCH) != NO_NUEVO) {
 			_screen.addElement("Crear nueva actuacin", 0);
 		}
 	}
 	
 	FieldChangeListener listener = new FieldChangeListener() {
 		
 		public void fieldChanged(Field field, int context) {
 			if(context == Util.CLICK) {
 				onClick();
 			} else if(context == Util.VER_ELEMENTO) {
 				verActuacion();
 			} else if (context == Util.CERRAR) {
 				cerrarPantalla();
 			} else if(context == Util.ELIMINAR) {
 				eliminarActuacion();
 			}
 		}
 	};
 
 	private void addActuaciones() {
 		if(_vectorActuaciones != null) {
 			_screen.loadFrom(_vectorActuaciones);
 		}
 	}
 
 	public void setVectorActuaciones(Vector actuaciones) {
 		_vectorActuaciones = actuaciones;
 		addActuaciones();
 	}
 
 	public Actuacion getSelected() {
 		return _selected;
 	}
 
 	public Screen getScreen() {
 		return (Screen)_screen;
 
 	}
 	
 	public void onClick() {
 		if(String.class.isInstance(_screen.getSelected())) {
 			nuevaActuacion();
 		} else {
 			if((_style & ON_CLICK_VER) == ON_CLICK_VER) {
 				verActuacion();
 			} else {
 				_selected = (Actuacion)_screen.getSelected();
 				UiApplication.getUiApplication().popScreen((Screen)_screen);
 			}
 		}
 	}
 	
 	private void nuevaActuacion() {
 		Actuacion actuacion = Util.nuevaActuacion(_proceso);
 		if (actuacion != null) {
 			if ((_style & NO_NUEVO) == NO_NUEVO) {
 				_screen.addElement(actuacion, 0);
 			} else {
 				_screen.addElement(actuacion, 1);
 			}
 		}
 	}
 	
 	private void verActuacion() {
 		Actuacion selected = (Actuacion)_screen.getSelected();
 		Actuacion actuacion = Util.verActuacion(selected);
 		if(actuacion != null) {
 			_screen.replace(selected, actuacion);
 		} else {
 			_screen.remove(selected);
 		}
 	}
 	
 	private void eliminarActuacion() {
 		Object[] ask = { "Aceptar", "Cancelar" };
 		int sel = _screen.ask(ask, Util.delBDActuacion(), 1);
 		if (sel == 0) {
 			Actuacion selected = (Actuacion) _screen.getSelected();
 			try {
 				new Persistence().borrarActuacion(selected);
 			} catch (NullPointerException e) {
 				Util.noSd();
 			} catch (Exception e) {
 				Util.alert(e.toString());
 			}
 			_screen.remove(selected);
 		}
 	}
 	
 	private void cerrarPantalla() {
 		if(String.class.isInstance(_screen.getSelected())) {
 			_selected = null;
 		} else {
 			_selected = (Actuacion)_screen.getSelected();
 		}
 		UiApplication.getUiApplication().popScreen((Screen)_screen);
 	}
 }
