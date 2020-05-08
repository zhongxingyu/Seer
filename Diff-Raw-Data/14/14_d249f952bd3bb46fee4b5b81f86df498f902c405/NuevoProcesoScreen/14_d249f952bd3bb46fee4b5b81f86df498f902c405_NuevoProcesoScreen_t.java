 package gui;
 
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import persistence.Persistence;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.BasicEditField;
 import net.rim.device.api.ui.component.ButtonField;
 import net.rim.device.api.ui.component.DateField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.NumericChoiceField;
 import net.rim.device.api.ui.component.ObjectChoiceField;
 import net.rim.device.api.ui.container.HorizontalFieldManager;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import core.CampoPersonalizado;
 import core.Categoria;
 import core.Juzgado;
 import core.Persona;
 
 public class NuevoProcesoScreen extends FondoNuevos {
 
 	private BasicEditField _txtEstado;
 	private ObjectChoiceField _chCategoria;
 	private NumericChoiceField _chPrioridad;
 	private DateField _dtFecha;
 	private BasicEditField _txtTipo;
 	private BasicEditField _txtNotas;
 	private BasicEditField _txtRadicado;
 	private BasicEditField _txtRadicadoUnico;
 	private ButtonField _btnDemandante;
 	private ButtonField _btnDemandado;
 	private ButtonField _btnJuzgado;
 	private ButtonField _btnCampoPersonalizado;
 
 	private HorizontalFieldManager _fldCampoPersonalizado;
 	private VerticalFieldManager _fldRightCampoPersonalizado;
 	private VerticalFieldManager _fldLeftCampoPersonalizado;
 
 	private Persona _demandante;
 	private Persona _demandado;
 	private Juzgado _juzgado;
 	private Vector _valoresCamposPersonalizados;
 
 	private boolean _guardar = false;
 
 	/**
 	 * Crea un NuevoProcesoScreen con los elementos para la captura de los datos
 	 * para el nuevo Proceso
 	 */
 	public NuevoProcesoScreen() {
 		setTitle("Nuevo Proceso");
 		_valoresCamposPersonalizados = new Vector();
 
 		HorizontalFieldManager fldDemandante = new HorizontalFieldManager();
 		VerticalFieldManager fldRightDemandante = new VerticalFieldManager(
 				USE_ALL_WIDTH);
 		VerticalFieldManager fldLeftDemandante = new VerticalFieldManager();
 		_btnDemandante = new ButtonField("Seleccionar", FIELD_RIGHT);
 		_btnDemandante.setChangeListener(listenerBtnDemandante);
 		fldLeftDemandante.add(new LabelField("Demandante: "));
 		fldRightDemandante.add(_btnDemandante);
 		fldDemandante.add(fldLeftDemandante);
 		fldDemandante.add(fldRightDemandante);
 		_vertical.add(fldDemandante);
 
 		HorizontalFieldManager fldDemandado = new HorizontalFieldManager();
 		VerticalFieldManager fldRightDemandado = new VerticalFieldManager(
 				USE_ALL_WIDTH);
 		VerticalFieldManager fldLeftDemandado = new VerticalFieldManager();
 		_btnDemandado = new ButtonField("Seleccionar", FIELD_RIGHT);
 		_btnDemandado.setChangeListener(listenerBtnDemandado);
 		fldLeftDemandado.add(new LabelField("Demandado: "));
 		fldRightDemandado.add(_btnDemandado);
 		fldDemandado.add(fldLeftDemandado);
 		fldDemandado.add(fldRightDemandado);
 		_vertical.add(fldDemandado);
 
 		HorizontalFieldManager fldJuzgado = new HorizontalFieldManager();
 		VerticalFieldManager fldRightJuzgado = new VerticalFieldManager(
 				USE_ALL_WIDTH);
 		VerticalFieldManager fldLeftJuzgado = new VerticalFieldManager();
 		_btnJuzgado = new ButtonField("Seleccionar", FIELD_RIGHT);
 		_btnJuzgado.setChangeListener(listenerBtnJuzgado);
 		fldLeftJuzgado.add(new LabelField("Juzgado: "));
 		fldRightJuzgado.add(_btnJuzgado);
 		fldJuzgado.add(fldLeftJuzgado);
 		fldJuzgado.add(fldRightJuzgado);
 		_vertical.add(fldJuzgado);
 
 		_txtRadicado = new BasicEditField(BasicEditField.NO_NEWLINE);
 		_txtRadicado.setLabel("Radicado: ");
 		_vertical.add(_txtRadicado);
 
 		_txtRadicadoUnico = new BasicEditField(BasicEditField.NO_NEWLINE);
 		_txtRadicadoUnico.setLabel("Radicado unico: ");
 		_vertical.add(_txtRadicadoUnico);
 
 		_txtTipo = new BasicEditField(BasicEditField.NO_NEWLINE);
 		_txtTipo.setLabel("Tipo: ");
 		_vertical.add(_txtTipo);
 
 		_txtEstado = new BasicEditField(BasicEditField.NO_NEWLINE);
 		_txtEstado.setLabel("Estado:");
 		_vertical.add(_txtEstado);
 
 		Vector v = new Vector();
 		try {
 			Persistence p = new Persistence();
 			v = p.consultarCategorias();
 		} catch (Exception e) {
 			Dialog.alert(e.toString());
 		}
 
 		Enumeration e = v.elements();
 		Object[] o = new Object[v.size()];
 		int i = 0;
 
 		_chCategoria = new ObjectChoiceField();
 		_chCategoria.setLabel("Categoria:");
 
 		while (e.hasMoreElements()) {
 			o[i] = e.nextElement();
 			i++;
 		}
 		_chCategoria.setChoices(o);
 		_vertical.add(_chCategoria);
 
 		_chPrioridad = new NumericChoiceField("Prioridad", 1, 10, 1);
 		_chPrioridad.setSelectedIndex(4);
 		_vertical.add(_chPrioridad);
 
 		_dtFecha = new DateField("Fecha de creacin: ",
 				System.currentTimeMillis(), DateField.DATE_TIME);
 		_dtFecha.setEditable(true);
 		_vertical.add(_dtFecha);
 
 		_txtNotas = new BasicEditField();
 		_txtNotas.setLabel("Notas: ");
 		_vertical.add(_txtNotas);
 
 		_fldCampoPersonalizado = new HorizontalFieldManager();
 		_fldRightCampoPersonalizado = new VerticalFieldManager(USE_ALL_WIDTH);
 		_fldLeftCampoPersonalizado = new VerticalFieldManager();
 		_btnCampoPersonalizado = new ButtonField("Nuevo", FIELD_RIGHT);
 		_btnCampoPersonalizado.setChangeListener(listenerBtnCampoPersonalizado);
 		_fldLeftCampoPersonalizado.add(new LabelField("Campo personalizado: "));
 		_fldRightCampoPersonalizado.add(_btnCampoPersonalizado);
 		_fldCampoPersonalizado.add(_fldLeftCampoPersonalizado);
 		_fldCampoPersonalizado.add(_fldRightCampoPersonalizado);
 		_vertical.add(_fldCampoPersonalizado);
 
 		add(_vertical);
 
 		addMenuItem(menuGuardar);
 		addMenuItem(menuEliminar);
 	}
 
 	private final MenuItem menuGuardar = new MenuItem("Guardar", 0, 0) {
 
 		public void run() {
 			if (_demandante == null)
 				Dialog.alert("Debe seleccionar un demandante");
 			else if (_demandado == null)
 				Dialog.alert("Debe seleccionar un demandado");
 			else if (_juzgado == null)
 				Dialog.alert("Debe Seleccionar un juzgado");
 			else {
 				_guardar = true;
 				UiApplication.getUiApplication().popScreen(getScreen());
 			}
 		}
 	};
 
 	private final MenuItem menuEliminar = new MenuItem("Eliminar", 0, 0) {
 
 		public void run() {
 			Field field = _vertical.getFieldWithFocus();
 			try {
 				Object o = ((BasicEditField) (((VerticalFieldManager) field)
 						.getField(1))).getCookie();
 				if (o != null && CampoPersonalizado.class.isInstance(o)) {
 					_vertical.delete(field);
 					_valoresCamposPersonalizados.removeElement(field);
 				} else
 					throw new Exception();
 			} catch (Exception e) {
 				Dialog.alert("El elemento no puede ser eliminado");
 			}
 		}
 	};
 
 	private FieldChangeListener listenerBtnDemandante = new FieldChangeListener() {
 		public void fieldChanged(Field field, int context) {
			ListadoPersonas l = new ListadoPersonas(1);
 			UiApplication.getUiApplication().pushModalScreen(
					l.getScreen());
 			try {
				_demandante = l.getSelected();
 				_btnDemandante.setLabel(_demandante.getNombre());
 			} catch (NullPointerException e) {
 				if (_demandante == null)
					Dialog.alert("Debe seleccionar un demandante");
			} finally {
				l = null;
 			}
 		}
 	};
 
 	private FieldChangeListener listenerBtnDemandado = new FieldChangeListener() {
 		public void fieldChanged(Field field, int context) {
 			ListadoPersonas demandados = new ListadoPersonas(2);
 			UiApplication.getUiApplication().pushModalScreen(
 					demandados.getScreen());
 			try {
 				_demandado = demandados.getSelected();
 				_btnDemandado.setLabel(_demandado.getNombre());
 			} catch (NullPointerException e) {
 				if (_demandado == null)
 					Dialog.alert("Debe seleccionar un demandado");
 			} finally {
 				demandados = null;
 			}
 		}
 	};
 
 	private FieldChangeListener listenerBtnJuzgado = new FieldChangeListener() {
 		public void fieldChanged(Field field, int context) {
 			ListadoJuzgados juzgados = new ListadoJuzgados();
 			UiApplication.getUiApplication().pushModalScreen(
 					juzgados.getScreen());
 			try {
 				_juzgado = juzgados.getSelected();
 				_btnJuzgado.setLabel(_juzgado.getNombre());
 			} catch (NullPointerException e) {
 				if (_juzgado == null)
 					Dialog.alert("Debe seleccionar un juzgado");
 			} finally {
 				juzgados = null;
 			}
 		}
 	};
 
 	private FieldChangeListener listenerBtnCampoPersonalizado = new FieldChangeListener() {
 		public void fieldChanged(Field field, int context) {
 			NuevoCampoPersonalizado campo = new NuevoCampoPersonalizado();
 			UiApplication.getUiApplication().pushModalScreen(campo.getScreen());
 			campo.guardarCampo();
 			try {
 				addCampoPersonalizado(campo.getCampo());
 			} catch (NullPointerException e) {
 				Dialog.alert("listenerBtnCampoPersonalizado -> " + e.toString());
 			}
 		}
 	};
 
 	/**
 	 * @param campo
 	 *            Agrega un campo personalizado al Proceso en creacion
 	 */
 	public void addCampoPersonalizado(CampoPersonalizado campo) {
 		BasicEditField campoP = new BasicEditField();
 		campoP.setLabel(campo.getNombre() + ": ");
 		campoP.setCookie(campo);
 		if (campo.getLongitudMax() != 0)
 			campoP.setMaxSize(campo.getLongitudMax());
 		_vertical.add(campoP);
 		_valoresCamposPersonalizados.addElement(campoP);
 	}
 
 	/**
 	 * @param demandante
 	 *            Se asigna una Persona demandante al Proceso en creacion
 	 */
 	public void setDemandante(Persona demandante) {
 		_btnDemandante.setLabel(demandante.getNombre());
 		_demandante = demandante;
 	}
 
 	/**
 	 * @param demandado
 	 *            Se asigna una Persona demandado al Proceso en creacion
 	 */
 	public void setDemandado(Persona demandado) {
 		_btnDemandado.setLabel(demandado.getNombre());
 		_demandado = demandado;
 	}
 
 	/**
 	 * @param juzgado
 	 *            Se asigna un Juzgado al Proceso en creacion
 	 */
 	public void setJuzgado(Juzgado juzgado) {
 		_btnJuzgado.setLabel(juzgado.getNombre());
 		_juzgado = juzgado;
 	}
 
 	/**
 	 * @return El vector que contiene los BasicTextField que representan los
 	 *         campos personalizados, cada uno de estos contiene a su vez un
 	 *         coockie en el cual esta almacenado el objeto CampoPersonalizado
 	 */
 	public Vector getValores() {
 		return _valoresCamposPersonalizados;
 	}
 
 	/**
 	 * @return La Persona demandante asociada al nuevo Proceso en creacion
 	 */
 	public Persona getDemandante() {
 		return _demandante;
 	}
 
 	/**
 	 * @return La Persona demandado asociada al nuevo Proceso en creacion
 	 */
 	public Persona getDemandado() {
 		return _demandado;
 	}
 
 	/**
 	 * @return El Juzgado asociado al nuevo Proceso en creacion
 	 */
 	public Juzgado getJuzgado() {
 		return _juzgado;
 	}
 
 	/**
 	 * @return La cadena con el radicado ingresado en la pantalla
 	 */
 	public String getRadicado() {
 		return _txtRadicado.getText();
 	}
 
 	/**
 	 * @return La cadena con el radicado unico ingresado en la pantalla
 	 */
 	public String getRadicadoUnico() {
 		return _txtRadicadoUnico.getText();
 	}
 
 	/**
 	 * @return La cadena con el estado ingresado en la pantalla
 	 */
 	public String getEstado() {
 		return (String) _txtEstado.getText();
 	}
 
 	/**
 	 * @return La cadena con la categoria ingresada en la pantalla
 	 */
 	public Categoria getCategoria() {
 		return (Categoria) _chCategoria.getChoice(_chCategoria
 				.getSelectedIndex());
 	}
 
 	/**
 	 * @return El numero que representa la prioridad ingresada en la pantalla
 	 */
 	public short getPrioridad() {
 		return Short.parseShort((String) _chPrioridad.getChoice(_chPrioridad
 				.getSelectedIndex()));
 	}
 
 	/**
 	 * @return La cadena con las notas ingresadas en la pantalla
 	 */
 	public String getNotas() {
 		return _txtNotas.getText();
 	}
 
 	/**
 	 * @return La cadena con el tipo ingresado en la pantalla
 	 */
 	public String getTipo() {
 		return _txtTipo.getText();
 	}
 
 	/**
 	 * @return El Calendar con la fecha ingresada en la pantalla
 	 */
 	public Calendar getFecha() {
 		Calendar fecha = Calendar.getInstance();
 
 		fecha.setTime(fecha.getTime());
 		return fecha;
 	}
 
 	public boolean isGuardar() {
 		return _guardar;
 	}
 
 	public boolean onClose() {
 		if (_demandante == null && _demandado == null && _juzgado == null
 				&& _txtRadicado.getTextLength() == 0
 				&& _txtRadicadoUnico.getTextLength() == 0
 				&& _txtTipo.getTextLength() == 0
 				&& _txtEstado.getTextLength() == 0
 				&& _txtNotas.getTextLength() == 0) {
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
