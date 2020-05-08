 package gui;
 
 import persistence.Persistence;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.container.MainScreen;
 import core.Juzgado;
 
 public class ListadoJuzgadosScreen extends MainScreen {
 	
 	public static final int ON_CLICK_VER = 1;
 	public static final int ON_CLICK_SELECT = 2;
 	public static final int NO_NUEVO = 4;
 	public static final int SEARCH = 8;
 
 	private Object _selected;
 	private ListadoJuzgadosLista _lista;
	private long _style;
 
 	public ListadoJuzgadosScreen(long style) {
 		super(MainScreen.VERTICAL_SCROLL | MainScreen.VERTICAL_SCROLLBAR);
 		_style = style;
 		_lista = new ListadoJuzgadosLista();

 		if ((_style & NO_NUEVO) != NO_NUEVO) {
			_lista.insert(0, "Crear nuevo juzgado");
 		}
		if((_style & SEARCH) == SEARCH) {
 			add(_lista.getKeywordField());
 		}
 		add(_lista);
 	}
 	
 	public ListadoJuzgadosScreen() {
 		this(0);
 	}
 	
 	protected boolean navigationClick(int status, int time) {
 		if (String.class.isInstance(_lista.getSelectedElement())) {
 			onNew();
 			return true;
 		} else {
 			onClick();
 			return true;
 		}
 	}
 
 	private void onNew() {
 		NuevoJuzgado n = new NuevoJuzgado();
 		UiApplication.getUiApplication().pushModalScreen(n.getScreen());
 		try {
 			if((_style & NO_NUEVO) == NO_NUEVO) {
 				_lista.insert(0, n.getJuzgado());
 				_lista.setSelectedIndex(0);
 			} else {
 				_lista.insert(1, n.getJuzgado());
 				_lista.setSelectedIndex(1);
 			}
 		} catch (Exception e) {
 
 		} finally {
 			n = null;
 		}
 	}
 
 	private void onClick() {
 		if ((_style & ON_CLICK_VER) == ON_CLICK_VER) {
 			menuVer.run();
 		} else {
 			_selected = _lista.getSelectedElement();
 			UiApplication.getUiApplication().popScreen(getScreen());
 		}
 	}
 	
 	protected void makeMenu(Menu menu, int instance) {
 		if (!String.class.isInstance(_lista.getSelectedElement())) {
 			menu.add(menuVer);
 			menu.add(menuDelete);
 		}
 	}
 
 	private final MenuItem menuVer = new MenuItem("Ver", 0, 0) {
 
 		public void run() {
 			Juzgado old = (Juzgado) _lista.getSelectedElement();
 			VerJuzgado v = new VerJuzgado(old);
 			UiApplication.getUiApplication().pushModalScreen(
 					v.getScreen());
 			v.actualizarJuzgado();
 			Juzgado nw = v.getJuzgado();
 			_lista.update(old, nw);
 			if (_lista.getKeywordField().getTextLength() != 0) {
 				_lista.setText(nw.getNombre());
 			}
 			old = null;
 		}
 	};
 
 	private final MenuItem menuDelete = new MenuItem("Eliminar", 0, 0) {
 
 		public void run() {
 			Object[] ask = { "Si", "No" };
 			int sel = Dialog.ask("Desdea eliminar el juzgado?", ask, 1);
 			if (sel == 0) {
 				Persistence persistence = null;
 				try {
 					persistence = new Persistence();
 					persistence.borrarJuzgado((Juzgado) _lista
 							.getSelectedElement());
 				} catch (Exception e) {
 					Dialog.alert(e.toString());
 				}
 
 				int index = _lista.getIndex(_lista
 						.getSelectedElement());
 				_lista.remove(index);
 				_lista.setSelectedIndex(index);
 			}
 		}
 	};
 
 	public void addJuzgado(Object juzgado) {
		_lista.insert(juzgado);
 	}
 
 	public Object getSelected() {
 		return _selected;
 	}
 
 	public boolean onClose() {
 		UiApplication.getUiApplication().popScreen(getScreen());
 		return true;
 	}
 }
