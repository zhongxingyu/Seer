 package gui.Listados;
 
 import gui.Util;
 import gui.Nuevos.NuevoProceso;
 
 import java.util.Vector;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.Screen;
 import net.rim.device.api.ui.UiApplication;
 import persistence.Persistence;
 import core.Categoria;
 import core.Preferencias;
 import core.Proceso;
 
 public class ListadoProcesos {
 
 	private Vector _vectorProcesos;
 	private ListadoProcesosInterface _screen;
 	private long _style;
 	private Proceso _selected;
 	private Vector _categorias;
 
 	public static final int ON_CLICK_VER = 2;
 	public static final int ON_CLICK_SELECT = 4;
 	public static final int NO_NUEVO = 8;
 
 	public ListadoProcesos() {
 		this(false, 0);
 	}
 
 	public ListadoProcesos(boolean popup) {
 		this(popup, 0);
 	}
 
 	public ListadoProcesos(long style) {
 		this(false, style);
 	}
 
 	public ListadoProcesos(boolean popup, long style) {
 		_style = style;
 		if (popup) {
 			_screen = new ListadoProcesosPopUp();
 		} else {
 			_screen = new ListadoProcesosScreen();
 		}
 
 		_screen.setStatus(Util.getWaitLabel());
 
 		UiApplication.getUiApplication().invokeLater(new Runnable() {
 
 			public void run() {
 				try {
 					_vectorProcesos = new Persistence().consultarProcesos();
 				} catch (NullPointerException e) {
 					Util.noSd();
 				} catch (Exception e) {
 					Util.alert(e.toString());
 				}
 				addProcesos();
 				if ((_style & NO_NUEVO) != NO_NUEVO) {
 					_screen.addElement("Crear nuevo proceso", 0);
 				}
 				if (Preferencias.isMostrarCampoBusqueda()) {
 					_screen.setSearchField();
 				}
 			}
 		});
 
 		((Screen) _screen).setChangeListener(listener);
 
 		if ((_style & ON_CLICK_VER) != ON_CLICK_VER
 				&& (_style & ON_CLICK_SELECT) != ON_CLICK_SELECT) {
 			if (popup) {
 				_style = _style | ON_CLICK_SELECT;
 			} else {
 				_style = _style | ON_CLICK_VER;
 			}
 		}
 
 		_categorias = Util.consultarCategorias();
 
 		if (_categorias != null) {
 			Object[] o = new Object[_categorias.size() + 1];
 			_categorias.copyInto(o);
 			String todas = "Todas";
 			o[_categorias.size()] = todas;
 			_screen.setCategorias(o);
 			if (Preferencias.getUltimaCategoria() == null) {
 				_screen.setSelectedCategoria(todas);
 			} else {
 				_screen.setSelectedCategoria(Preferencias.getUltimaCategoria());
 			}
 		}
 	}
 
 	private FieldChangeListener listener = new FieldChangeListener() {
 
 		public void fieldChanged(Field field, int context) {
 			if (context == Util.CLICK) {
 				onClick();
 			} else if (context == Util.VER_ELEMENTO) {
 				verProceso();
 			} else if (context == Util.CERRAR) {
 				cerrarPantalla();
 			} else if (context == Util.ELIMINAR) {
 				eliminarProceso();
 			} else if (context == Util.NEW) {
 				nuevoProceso();
 			}
 		}
 	};
 
 	private void addProcesos() {
 		if (_vectorProcesos != null) {
 			_screen.loadFrom(_vectorProcesos);
 		}
 	}
 
 	public Proceso getSelected() {
 		return _selected;
 	}
 
 	public Screen getScreen() {
 		return (Screen) _screen;
 
 	}
 
 	public void onClick() {
 		try {
 			if (String.class.isInstance(_screen.getSelected())) {
 				nuevoProceso();
 			} else {
 				if ((_style & ON_CLICK_VER) == ON_CLICK_VER) {
 					verProceso();
 				} else {
 					setUltimaCategoria();
 					_selected = (Proceso) _screen.getSelected();
 					UiApplication.getUiApplication()
 							.popScreen((Screen) _screen);
 				}
 			}
 		} catch (Exception e) {
 		}
 	}
 	
 	private void reloadCategorias() {
 		_screen.setStatus(Util.getWaitLabel());
 		UiApplication.getUiApplication().invokeLater(new Runnable() {
 
 			public void run() {
 				Object selected = _screen.getSelectedCategoria();
 				Categoria categoria;
 				if (String.class.isInstance(selected)) {
 					categoria = null;
 				} else {
 					categoria = (Categoria) selected;
 				}
 				_categorias = Util.consultarCategorias();
 				Object[] choices = new Object[_categorias.size() + 1];
 				_categorias.copyInto(choices);
 				String todas = "Todas";
 				choices[_categorias.size()] = todas;
 				_screen.setCategorias(choices);
 				if (categoria != null && _categorias.contains(categoria)) {
 					_screen.setSelectedCategoria(categoria);
 				} else {
 					_screen.setSelectedCategoria(todas);
 				}
 				if(Preferencias.isMostrarCampoBusqueda()) {
 					_screen.setSearchField();
 				}
 			}
 		});
 	}
 
 	private void nuevoProceso() {
 		NuevoProceso n = new NuevoProceso();
 		Util.pushModalScreen(n.getScreen());
 		Proceso proceso = n.getProceso();
 		if (proceso != null) {
 			reloadCategorias();
 			if ((_style & NO_NUEVO) == NO_NUEVO) {
 				_screen.addElement(proceso, 0);
 			} else {
 				_screen.addElement(proceso, 1);
 			}
 		}
 		n = null;
 	}
 
 	private void verProceso() {
 		Proceso selected = (Proceso) _screen.getSelected();
 		Proceso proceso = Util.verProceso(selected);
 		reloadCategorias();
 		if (proceso != null) {
 			_screen.replace(selected, proceso);
 		} else {
 			_screen.remove(selected);
 		}
 	}
 
 	private void eliminarProceso() {
 		Object[] ask = { "Aceptar", "Cancelar" };
 		int sel = _screen.ask(ask, Util.delBDProceso(), 1);
 		if (sel == 0) {
 			Proceso selected = (Proceso) _screen.getSelected();
 			try {
 				new Persistence().borrarProceso(selected);
 			} catch (NullPointerException e) {
 				_screen.alert(Util.noSDString());
 				System.exit(0);
 			} catch (Exception e) {
 				_screen.alert(e.toString());
 			}
 			_screen.remove(selected);
 		}
 	}
 
 	private void setUltimaCategoria() {
 		Preferencias.setUltimaCategoria(_screen.getSelectedCategoria());
 	}
 
 	private void cerrarPantalla() {
 		setUltimaCategoria();
 		_selected = null;
 		Util.popScreen((Screen) _screen);
 	}
 
 	public String toString() {
 		return "Lista de procesos";
 	}
 }
