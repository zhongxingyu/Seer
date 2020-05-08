 package gui;
 
 import persistence.Persistence;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.container.MainScreen;
 import core.Juzgado;
 
 public class ListadoJuzgadosScreen extends MainScreen {
 
 	private Object _selected;
 	private ListadoJuzgadosLista _lista;
 
 	public ListadoJuzgadosScreen() {
 		super(MainScreen.VERTICAL_SCROLL | MainScreen.VERTICAL_SCROLLBAR);
 
 		setTitle("Listado de juzgados");
 
 		_lista = new ListadoJuzgadosLista() {
 			protected boolean navigationClick(int status, int time) {
 				if (String.class.isInstance(get(_lista, getSelectedIndex()))) {
 					NuevoJuzgado n = new NuevoJuzgado();
 					UiApplication.getUiApplication().pushModalScreen(
 							n.getScreen());
 					try {
 						addJuzgado(n.getJuzgado());
 					} catch (Exception e) {
 						return true;
 					}
					return true;
				} else {
					_selected = get(_lista, getSelectedIndex());
					UiApplication.getUiApplication().popScreen(getScreen());
					return true;
 				}
 			}
 		};
 
 		_lista.insert(0, "Nuevo juzgado");
 		add(_lista);
 		addMenuItem(menuVer);
 		addMenuItem(menuDelete);
 	}
 
 	private final MenuItem menuVer = new MenuItem("Ver", 0, 0) {
 
 		public void run() {
 			int index = _lista.getSelectedIndex();
 			VerJuzgado verJuzgado = new VerJuzgado(
 					(Juzgado) _lista.get(_lista, index));
 			UiApplication.getUiApplication().pushModalScreen(
 					verJuzgado.getScreen());
 			verJuzgado.actualizarJuzgado();
 			_lista.delete(index);
 			_lista.insert(index, verJuzgado.getJuzgado());
 			_lista.setSelectedIndex(index);
 		}
 	};
 	
 	private final MenuItem menuDelete = new MenuItem("Eliminar", 0, 0) {
 
 		public void run() {
 			Persistence persistence = null;
 			try {
 				persistence = new Persistence();
 			} catch (Exception e) {
 				Dialog.alert(e.toString());
 			}
 			int index = _lista.getSelectedIndex();
 			try {
 				persistence.borrarJuzgado((Juzgado) _lista.get(_lista, index));
 			} catch (Exception e) {
 				Dialog.alert(e.toString());
 			}
 
 			_lista.delete(index);
 		}
 	};
 
 	public void addJuzgado(Object juzgado) {
 		_lista.insert(_lista.getSize(), juzgado);
 	}
 
 	public Object getSelected() {
 		return _selected;
 	}
 
 	public boolean onClose() {
 		UiApplication.getUiApplication().popScreen(getScreen());
 		return true;
 	}
 }
