 package gui;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.DateField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.component.SeparatorField;
 import core.Actuacion;
 import core.CalendarManager;
 import core.Juzgado;
 
 public class VerActuacionScreen extends FondoNormal {
 
 	private EditableTextField _txtJuzgado;
 	private DateField _dfFecha;
 	private DateField _dfFechaProxima;
 	private EditableTextField _txtDescripcion;
 	private Actuacion _actuacion;
 	private Juzgado _juzgado;
	
	private SeparatorField _sfCitaSuperior;
	private LabelField _lblHasCita;
	private SeparatorField _sfCitaInferior;
 	
 
 	private boolean _guardar = false;
 	private boolean _eliminar = false;
 	private String _uid;
 
 	public VerActuacionScreen(Actuacion actuacion) {
 		
		_sfCitaInferior = new SeparatorField();
		_sfCitaSuperior = new SeparatorField();
		_lblHasCita = new LabelField("Esta actuacin tiene una cita en el calendario");		
 
 		_actuacion = actuacion;
 		_uid = _actuacion.getUid();
 		_juzgado = actuacion.getJuzgado();
 
 		setTitle("Ver actuacin");
 
 		_txtJuzgado = new EditableTextField("Juzgado: ", _actuacion
 				.getJuzgado().getNombre());
 
 		_dfFecha = new DateField("Fecha: ", _actuacion.getFecha().getTime()
 				.getTime(), DateField.DATE_TIME);
 		_dfFecha.setEditable(false);
 
 		_dfFechaProxima = new DateField("Fecha prxima: ", _actuacion
 				.getFechaProxima().getTime().getTime(), DateField.DATE_TIME);
 		_dfFechaProxima.setEditable(false);
 
 		_txtDescripcion = new EditableTextField("Descripcin: ",
 				_actuacion.getDescripcion());
 
 		add(_txtJuzgado);
 		add(_dfFecha);
 		add(_dfFechaProxima);
 		add(_txtDescripcion);
 	}
 	
 	public void setCita() {
 		setCita(true);
 	}
 	
 	public void setCita(boolean cita) {
 		if(cita) {
			add(_sfCitaSuperior, false);
			add(_lblHasCita);
			add(_sfCitaInferior);
 		} else {
			delete(_sfCitaInferior);
			delete(_lblHasCita);
			delete(_sfCitaSuperior);
 		}
 	}
 	
 	protected void makeMenu(Menu menu, int instance) {
 		Field focus = UiApplication.getUiApplication().getActiveScreen()
 		.getFieldWithFocus();
 		if(focus.equals(_txtJuzgado)) {
 			menu.add(menuCambiar);
 			menu.addSeparator();
 		}
 		if(_uid != null) {
 			menu.add(menuVerCita);
 			menu.add(menuEliminarCita);
 			menu.addSeparator();
 		} else {
 			menu.add(menuAddCita);
 			menu.addSeparator();
 		}
 		menu.add(menuEditar);
 		menu.add(menuEditarTodo);
 		menu.addSeparator();
 		menu.add(menuEliminarActuacion);
 		menu.add(menuGuardar);
 	}
 
 	private final MenuItem menuVerCita = new MenuItem("Ver cita", 0, 0) {
 
 		public void run() {
 			VerCita v = new VerCita(_uid);
 			UiApplication.getUiApplication().pushModalScreen(v.getScreen());
 			v.guardarCita();
 		}
 	};
 	
 	private final MenuItem menuAddCita = new MenuItem("Agregar cita", 0, 0) {
 
 		public void run() {
 			NuevaCita n = new NuevaCita(getDescripcion(), getFechaProxima().getTime());
 			UiApplication.getUiApplication().pushModalScreen(n.getScreen());
 			_uid = n.getUid();
 			setCita();
 		}
 	};
 	
 	private final MenuItem menuEliminarCita = new MenuItem("Eliminar cita", 0,
 			0) {
 
 		public void run() {
 			Object[] ask = { "Aceptar", "Cancelar" };
 			int sel = Dialog.ask("Desea eliminar la cita del calendario?",
 					ask, 1);
 			if (sel == 0) {
 				try {
 					CalendarManager.borrarCita(_uid);
 					_uid = null;
 					setCita(false);
 				} catch(Exception e) {
 					Dialog.alert(e.toString());
 				}
 			}
 		}
 	};
 	
 	private final MenuItem menuGuardar = new MenuItem("Guardar", 0, 0) {
 
 		public void run() {
 			_guardar = true;
 			UiApplication.getUiApplication().popScreen(getScreen());
 		}
 	};
 
 	private final MenuItem menuEditar = new MenuItem("Editar", 0, 0) {
 
 		public void run() {
 			Field f = getFieldWithFocus();
 			if (f.equals(_txtJuzgado)) {
 				VerJuzgado verJuzgado = new VerJuzgado(_juzgado);
 				UiApplication.getUiApplication().pushModalScreen(
 						verJuzgado.getScreen());
 				_juzgado = verJuzgado.getJuzgado();
 				_txtJuzgado.setText(_juzgado.getNombre());
 				_txtJuzgado.setFocus();
 			}
 			if (f.equals(_dfFecha)) {
 				_dfFecha.setEditable(true);
 				_dfFecha.setFocus();
 			}
 			if (f.equals(_dfFechaProxima)) {
 				_dfFechaProxima.setEditable(true);
 				_dfFechaProxima.setFocus();
 			}
 			if (f.equals(_txtDescripcion)) {
 				_txtDescripcion.setEditable();
 				_txtDescripcion.setFocus();
 			}
 		}
 	};
 
 	private final MenuItem menuEditarTodo = new MenuItem("Editar todo", 0, 0) {
 
 		public void run() {
 			_txtJuzgado.setEditable();
 			_dfFecha.setEditable(true);
 			_dfFechaProxima.setEditable(true);
 			_txtDescripcion.setEditable();
 		}
 	};
 
 	private final MenuItem menuCambiar = new MenuItem("Cambiar", 0, 0) {
 
 		public void run() {
 			Field f = getFieldWithFocus();
 			if (f.equals(_txtJuzgado)) {
 				ListadoJuzgados juzgados = new ListadoJuzgados();
 				UiApplication.getUiApplication().pushModalScreen(
 						juzgados.getScreen());
 				try {
 					_juzgado = juzgados.getSelected();
 					_txtJuzgado.setText(_juzgado.getNombre());
 					_txtJuzgado.setFocus();
 				} catch (NullPointerException e) {
 
 				}
 			}
 		}
 	};
 
 	private final MenuItem menuEliminarActuacion = new MenuItem("Eliminar actuacin", 0, 0) {
 
 		public void run() {
 			Object[] ask = { "Si", "No" };
 			int sel = Dialog.ask("Desea eliminar la actuacin?", ask, 1);
 			if (sel == 0) {
 				if(_actuacion.getUid() != null) {
 					sel = Dialog.ask("Desea eliminar la cita del calendario?", ask, 1);
 					if(sel == 0) {
 						try {
 						CalendarManager.borrarCita(_uid);
 						_uid = null;
 						} catch(Exception e) {
 							Dialog.alert(e.toString());
 						}
 					}
 				}
 				_eliminar = true;
 				UiApplication.getUiApplication().popScreen(getScreen());
 			}
 		}
 	};
 
 	public Juzgado getJuzgado() {
 		return _juzgado;
 	}
 
 	public Calendar getFecha() {
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(new Date(_dfFecha.getDate()));
 		return calendar;
 	}
 
 	public Calendar getFechaProxima() {
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(new Date(_dfFechaProxima.getDate()));
 		return calendar;
 	}
 
 	public String getDescripcion() {
 		return _txtDescripcion.getText();
 	}
 	
 	public String getUid() {
 		return _uid;
 	}
 
 	public boolean isGuardado() {
 		return _guardar;
 	}
 
 	public boolean isEliminado() {
 		return _eliminar;
 	}
 	
 	public boolean isCambiado() {
 		boolean cambio = false;
 
 		Calendar f1 = _actuacion.getFecha();
 		Calendar f2 = getFecha();
 
 		Calendar fP1 = _actuacion.getFechaProxima();
 		Calendar fP2 = getFechaProxima();
 
 		if (!_actuacion.getJuzgado().getId_juzgado()
 				.equals(getJuzgado().getId_juzgado()))
 			cambio = true;
 		else if ((f1.get(Calendar.YEAR) != f2.get(Calendar.YEAR))
 				|| (f1.get(Calendar.MONTH) != f2.get(Calendar.MONTH))
 				|| (f1.get(Calendar.DAY_OF_MONTH) != f2
 						.get(Calendar.DAY_OF_MONTH)))
 			cambio = true;
 		else if ((fP1.get(Calendar.YEAR) != fP2.get(Calendar.YEAR))
 				|| (fP1.get(Calendar.MONTH) != fP2.get(Calendar.MONTH))
 				|| (fP1.get(Calendar.DAY_OF_MONTH) != fP2
 						.get(Calendar.DAY_OF_MONTH)))
 			cambio = true;
 		else if (!_actuacion.getDescripcion().equals(
 				getDescripcion()))
 			cambio = true;
 		else if(_actuacion.getUid() != _uid) {
 			cambio = true;
 		}
 		return cambio;
 	}
 
 	public boolean onClose() {
 		if (!isCambiado()) {
 			UiApplication.getUiApplication().popScreen(getScreen());
 			return true;
 		} else {
 			Object[] ask = { "Guardar", "Descartar", "Cancelar" };
 			int sel = Dialog.ask("Se han detectado cambios", ask, 1);
 			if (sel == 0) {
 				_guardar = true;
 				UiApplication.getUiApplication().popScreen(getScreen());
 				return true;
 			} else if (sel == 1) {
 				UiApplication.getUiApplication().popScreen(getScreen());
 				return true;
 			} else {
 				return false;
 			}
 		}
 	}
 }
