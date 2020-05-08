 package gui.Ver;
 
 import gui.EditableTextField;
 import gui.FondoNormal;
 import gui.Util;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Vector;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.component.DateField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.component.NumericChoiceField;
 import net.rim.device.api.ui.component.TextField;
 
 public class VerProcesoScreen extends FondoNormal {
 
 	private EditableTextField _txtDemandante;
 	private EditableTextField _txtDemandado;
 	private EditableTextField _txtJuzgado;
 	private EditableTextField _txtRadicado;
 	private EditableTextField _txtRadicadoUnico;
 	private EditableTextField _txtEstado;
 	private EditableTextField _txtCategoria;
 	private EditableTextField _txtTipo;
 	private EditableTextField _txtNotas;
 	private DateField _dfFecha;
 	private NumericChoiceField _nfPrioridad;
 
 	private Vector _txtCampos;
 
 	private Field _focused;
 
 	public VerProcesoScreen() {
 		_txtCampos = new Vector();
 		setTitle("Ver proceso");
 
 		_txtDemandante = new EditableTextField("Demandante: ",
 				TextField.NO_NEWLINE);
 		add(_txtDemandante);
 
 		_txtDemandado = new EditableTextField("Demandado: ",
 				TextField.NO_NEWLINE);
 		add(_txtDemandado);
 
 		_txtJuzgado = new EditableTextField("juzgado: ", TextField.NO_NEWLINE);
 		add(_txtJuzgado);
 
 		_dfFecha = new DateField("Fecha: ", 0, DateField.DATE_TIME);
 		_dfFecha.setEditable(false);
 		add(_dfFecha);
 
 		_txtRadicado = new EditableTextField("Radicado: ");
 		add(_txtRadicado);
 
 		_txtRadicadoUnico = new EditableTextField("Radicado unico: ");
 		add(_txtRadicadoUnico);
 
 		_txtEstado = new EditableTextField("Estado: ");
 		add(_txtEstado);
 
 		_txtCategoria = new EditableTextField("Categora: ");
 		add(_txtCategoria);
 
 		_txtTipo = new EditableTextField("Tipo: ");
 		add(_txtTipo);
 
 		_txtNotas = new EditableTextField("Notas: ");
 		add(_txtNotas);
 
 		_nfPrioridad = new NumericChoiceField("Prioridad: ", 0, 10, 1);
 		_nfPrioridad.setEditable(false);
 		add(_nfPrioridad);
 	}
 
 	public void alert(String alert) {
 		Dialog.alert(alert);
 	}
 
 	public int ask(Object[] options, String string, int index) {
 		return Dialog.ask(string, options, index);
 	}
 
 	public void addCampo(Object cookie, String nombre, String valor) {
 		nombre = nombre + ": ";
 		EditableTextField txt = new EditableTextField(nombre);
 		txt.setText(valor);
 		txt.setCookie(cookie);
 		_txtCampos.addElement(txt);
 		add(txt);
 	}
 
 	public void eliminarCampo(int index) {
 		_txtCampos.removeElementAt(index);
 	}
 
 	public void eliminarCampo() {
 		delete(_focused);
 		_txtCampos.removeElement(_focused);
 	}
 
 	public void modificarCampo(Object cookie, String text) {
 		_focused.setCookie(cookie);
 		((EditableTextField) _focused).setLabel(text + ": ");
 	}
 
 	public Object[] getCampo(int index) {
 		Object[] ret = new Object[2];
 		ret[0] = ((EditableTextField) _txtCampos.elementAt(index)).getCookie();
 		ret[1] = ((EditableTextField) _txtCampos.elementAt(index)).getText();
 		return ret;
 	}
 
 	public void setDemandante(String text) {
 		_txtDemandante.setText(text);
 	}
 
 	public void setDemandado(String text) {
 		_txtDemandado.setText(text);
 	}
 
 	public void setJuzgado(String text) {
 		_txtJuzgado.setText(text);
 	}
 
 	public void setRadicado(String text) {
 		_txtRadicado.setText(text);
 	}
 
 	public void setRadicadoUnico(String text) {
 		_txtRadicadoUnico.setText(text);
 	}
 
 	public void setEstado(String text) {
 		_txtEstado.setText(text);
 	}
 
 	public void setCategoria(String text) {
 		_txtCategoria.setText(text);
 	}
 
 	public void setTipo(String text) {
 		_txtTipo.setText(text);
 	}
 
 	public void setNotas(String text) {
 		_txtNotas.setText(text);
 	}
 
 	public void setFecha(Date date) {
 		_dfFecha.setDate(date);
 	}
 
 	public void setPrioridad(int element) {
 		_nfPrioridad.setSelectedIndex(String.valueOf(element));
 	}
 
 	public String getEstado() {
 		return _txtEstado.getText();
 	}
 
 	public Object getCookieOfFocused() {
 		return _focused.getCookie();
 	}
 
 	public Calendar getFecha() {
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(new Date(_dfFecha.getDate()));
 		return calendar;
 	}
 
 	public String getNotas() {
 		return _txtNotas.getText();
 	}
 
 	public int getPrioridad() {
 		return _nfPrioridad.getSelectedValue();
 	}
 
 	public String getRadicado() {
 		return _txtRadicado.getText();
 	}
 
 	public String getRadicadoUnico() {
 		return _txtRadicadoUnico.getText();
 	}
 
 	public String getTipo() {
 		return _txtTipo.getText();
 	}
 
 	protected void makeMenu(Menu menu, int instance) {
 		Field f = getFieldWithFocus();
 
 		menu.add(menuAddActuacion);
 		menu.add(menuAddCampo);
 		menu.addSeparator();
 
 		menu.add(menuVerActuaciones);
 
 		if (f.equals(_txtCategoria)) {
 			menu.add(menuCambiar);
 			menu.addSeparator();
 		} else if (f.equals(_txtDemandante) || f.equals(_txtDemandado)
 				|| f.equals(_txtJuzgado)) {
 			menu.add(menuCambiar);
 			menu.add(menuEliminar);
 			menu.addSeparator();
 		}
 
 		menu.add(menuEditar);
 		menu.add(menuEditarTodo);
 		menu.addSeparator();
 
 		if (EditableTextField.class.isInstance(f)) {
 			if (f.getCookie() != null) {
 				_focused = f;
 				menu.add(menuModificarCampo);
 				menu.add(menuEliminarCampo);
 				menu.addSeparator();
 			}
 		}
 		menu.add(menuGuardar);
 	}
 
 	private final MenuItem menuVerActuaciones = new MenuItem("Ver actuaciones", 0,
 			0) {
 		public void run() {
 			fieldChangeNotify(Util.VER_LISTADO_ACTUACIONES);
 		}
 	};
 
 	private final MenuItem menuModificarCampo = new MenuItem("Modificar", 0, 0) {
 
 		public void run() {
 			fieldChangeNotify(Util.VER_CAMPO);
 		}
 	};
 
 	private final MenuItem menuAddCampo = new MenuItem(
 			"Agregar campo personalizado", 0, 0) {
 
 		public void run() {
 			fieldChangeNotify(Util.ADD_CAMPO);
 		}
 	};
 
 	private final MenuItem menuEliminar = new MenuItem("Eliminar del proceso",
 			0, 0) {
 
 		public void run() {
 			EditableTextField f = (EditableTextField) getFieldWithFocus();
 			if (f.equals(_txtDemandante)) {
 				fieldChangeNotify(Util.ELIMINAR_DEMANDANTE);
 			} else if (f.equals(_txtDemandado)) {
 				fieldChangeNotify(Util.ELIMINAR_DEMANDADO);
 			} else if (f.equals(_txtJuzgado)) {
 				fieldChangeNotify(Util.ELIMINAR_JUZGADO);
 			}
 		}
 	};
 
 	private final MenuItem menuEliminarCampo = new MenuItem(
 			"Eliminar del proceso", 0, 0) {
 		public void run() {
 			fieldChangeNotify(Util.ELIMINAR_CAMPO);
 		}
 	};
 
 	private final MenuItem menuGuardar = new MenuItem("Guardar", 0, 0) {
 
 		public void run() {
 			fieldChangeNotify(Util.GUARDAR);
 		}
 	};
 
 	private final MenuItem menuEditar = new MenuItem("Editar", 0, 0) {
 
 		public void run() {
 			Field f = getFieldWithFocus();
 			if (f.equals(_txtDemandante)) {
 				fieldChangeNotify(Util.VER_DEMANDANTE);
 			} else if (f.equals(_txtDemandado)) {
 				fieldChangeNotify(Util.VER_DEMANDADO);
 			} else if (f.equals(_txtJuzgado)) {
 				fieldChangeNotify(Util.VER_JUZGADO);
 			} else if (f.equals(_txtCategoria)) {
 				fieldChangeNotify(Util.VER_CATEGORIA);
 			} else {
 				f.setEditable(true);
 			}
 		}
 	};
 
 	private final MenuItem menuEditarTodo = new MenuItem("Editar todo", 0, 0) {
 
 		public void run() {
 			_dfFecha.setEditable(true);
 			_txtRadicado.setEditable();
 			_txtRadicadoUnico.setEditable();
 			_txtEstado.setEditable();
 			_txtTipo.setEditable();
 			_txtNotas.setEditable();
 			_nfPrioridad.setEditable(true);
 		}
 	};
 
 	private final MenuItem menuCambiar = new MenuItem("Cambiar", 0, 0) {
 
 		public void run() {
 			EditableTextField f = (EditableTextField) getFieldWithFocus();
 			if (f.equals(_txtDemandante)) {
 				fieldChangeNotify(Util.ADD_DEMANDANTE);
 			} else if (f.equals(_txtDemandado)) {
 				fieldChangeNotify(Util.ADD_DEMANDADO);
 			} else if (f.equals(_txtJuzgado)) {
 				fieldChangeNotify(Util.ADD_JUZGADO);
 			} else if (f.equals(_txtCategoria)) {
 				fieldChangeNotify(Util.ADD_CATEGORIA);
 			}
 		}
 	};
 
 	private final MenuItem menuAddActuacion = new MenuItem("Agregar actuacin",
 			0, 0) {
 
 		public void run() {
 			fieldChangeNotify(Util.NEW_ACTUACION);
 		}
 	};
 
 	public boolean onClose() {
 		fieldChangeNotify(Util.CERRAR);
 		return false;
 	}
 }
