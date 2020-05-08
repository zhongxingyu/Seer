 package gui;
 
 import gui.Listados.ListadoPlantillas;
 import gui.Listados.ListadoProcesos;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.ObjectListField;
 import net.rim.device.api.ui.component.SeparatorField;
 import net.rim.device.api.ui.container.PopupScreen;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import core.Plantilla;
 import core.Proceso;
 
 public class ScreenMainListas extends PopupScreen {
 
 	private static ObjectListField _lista;
 	private static int _style;
 	private static VerticalFieldManager _title;
 
 	private static String TITLE_LISTAS = "Ver listado de:";
 	private static String TITLE_NUEVOS = "Crear:";
 
 	public static final byte LISTA = 1;
 	public static final byte NUEVO = 2;
 
 	private static String DEMANDANTE = "Demandante";
 	private static String DEMANDADO = "Demandado";
 	private static String JUZGADO = "Juzgado";
 	private static String CAMPO = "Campo personalizado";
 	private static String CATEGORIA = "Categora";
 	private static String PROCESO = "Proceso";
 	private static String PLANTILLA = "Plantilla";
 	private static String ACTUACION = "Actuacin";
 
 	private static String DEMANDANTES = "Demandantes";
 	private static String DEMANDADOS = "Demandados";
 	private static String JUZGADOS = "Juzgados";
 	private static String CAMPOS = "Campos personalizados";
 	private static String CATEGORIAS = "Categoras";
 	private static String PROCESOS = "Procesos";
 	private static String PLANTILLAS = "Plantillas";
 	private static String ACTUACIONES = "Actuaciones";
 
 	private static String PROCESO_PLANTILLA = "Proceso a partir de plantilla";
 
 	private static String[] LISTAS = { PROCESOS, DEMANDANTES, DEMANDADOS,
 			JUZGADOS, CAMPOS, CATEGORIAS, PLANTILLAS, ACTUACIONES };
 	private static String[] NUEVOS = { PROCESO, DEMANDANTE, DEMANDADO, JUZGADO,
 			CAMPO, CATEGORIA, PLANTILLA, ACTUACION, PROCESO_PLANTILLA };
 
 	public ScreenMainListas() {
 		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR));
 		_title = new VerticalFieldManager();
 		_title.add(new LabelField());
 		_title.add(new SeparatorField());
 		_lista = new ObjectListField();
 		add(_title);
 		add(_lista);
 	}
 
 	public void call(int style) {
 		_style = style;
 		if ((_style & NUEVO) == NUEVO) {
 			((LabelField) _title.getField(0)).setText(TITLE_NUEVOS);
 			_lista.set(NUEVOS);
 		} else if ((_style & LISTA) == LISTA) {
 			((LabelField) _title.getField(0)).setText(TITLE_LISTAS);
 			_lista.set(LISTAS);
 		}
 	}
 
 	protected boolean navigationClick(int arg0, int arg1) {
 		String element = (String) _lista.get(_lista, _lista.getSelectedIndex());
 		Util.popScreen(this);
 
 		if ((_style & NUEVO) == NUEVO) {
 			if (element == DEMANDANTE) {
 				Util.nuevaPersona(1);
 			} else if (element == DEMANDADO) {
 				Util.nuevaPersona(2);
 			} else if (element == JUZGADO) {
 				Util.nuevoJuzgado();
 			} else if (element == CAMPO) {
 				Util.nuevoCampoPersonalizado();
 			} else if (element == CATEGORIA) {
 				Util.nuevaCategoria(false);
 			} else if (element == PROCESO) {
 				Util.nuevoProceso();
 			} else if (element == PLANTILLA) {
 				Util.nuevaPlantilla();
 			} else if (element == ACTUACION) {
 				Proceso proceso = Util.listadoProcesos(true,
 						ListadoProcesos.NO_NUEVO);
 				if (proceso != null) {
 					Util.nuevaActuacion(proceso);
 				}
 			} else if (element == PROCESO_PLANTILLA) {
 				Plantilla plantilla = Util.listadoPlantillas(true,
 						ListadoPlantillas.NO_NUEVO);
 				if (plantilla != null) {
 					Util.nuevoProceso(plantilla);
 				}
 			}
 		} else if ((_style & LISTA) == LISTA) {
 			if (element == DEMANDANTES) {
 				Util.listadoPersonas(1, false, 0);
 			} else if (element == DEMANDADOS) {
 				Util.listadoPersonas(2, false, 0);
 			} else if (element == JUZGADOS) {
 				Util.listadoJuzgados(false, 0);
 			} else if (element == CAMPOS) {
 				Util.listadoCampos(false, 0);
 			} else if (element == CATEGORIAS) {
				Util.listadoCategorias(false, 0);
 			} else if (element == PROCESOS) {
 				Util.listadoProcesos(false, 0);
 			} else if (element == PLANTILLAS) {
 				Util.listadoPlantillas(false, 0);
 			} else if (element == ACTUACIONES) {
 				Proceso proceso = Util.listadoProcesos(true,
 						ListadoProcesos.NO_NUEVO);
 				if (proceso != null) {
 					Util.listadoActuaciones(proceso, false, 0);
 				}
 			}
 		}
 		return true;
 	}
 
 	public boolean onClose() {
 		Util.popScreen(this);
 		return true;
 	}
 }
