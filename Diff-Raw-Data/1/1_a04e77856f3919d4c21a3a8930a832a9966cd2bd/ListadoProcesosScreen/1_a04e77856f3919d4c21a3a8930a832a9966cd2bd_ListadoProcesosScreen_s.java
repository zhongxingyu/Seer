 package gui.Listados;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.ObjectChoiceField;
 import net.rim.device.api.ui.container.HorizontalFieldManager;
 import gui.ListaScreen;
 
 public class ListadoProcesosScreen extends ListaScreen implements
 		ListadoProcesosInterface {
 
 	private ObjectChoiceField _cfCategorias;
 
 	public ListadoProcesosScreen() {
 		super();
 
 		_cfCategorias = new ObjectChoiceField();
 		_cfCategorias.setChangeListener(listener);
 
 		HorizontalFieldManager title = new HorizontalFieldManager(USE_ALL_WIDTH);
 		title.add(new LabelField("Procesos"));
 		title.add(_cfCategorias);
 
 		setTitle(title);
 
 		_lista = new ListadoProcesosLista() {
 			protected boolean navigationClick(int status, int time) {
 				click();
 				return true;
 			}
 		};
 		add(_lista, false);
 	}
 
 	public void setCategorias(Object[] choices) {
 		_cfCategorias.setChoices(choices);
 		invalidate();
 	}
 
 	public void setSelectedCategoria(Object object) {
 		_cfCategorias.setSelectedIndex(object);
 		invalidate();
 	}
 
 	private FieldChangeListener listener = new FieldChangeListener() {
 
 		public void fieldChanged(Field field, int context) {
 			Object selected = _cfCategorias.getChoice(_cfCategorias
 					.getSelectedIndex());
 			if (selected.toString().equalsIgnoreCase("todas")) {
 				_lista.setText("");
 			} else {
 				_lista.setText(selected.toString());
 			}
 			_lista.updateList();
 		}
 	};
 }
