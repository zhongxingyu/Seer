 package ehmsoft;
 
 import java.util.Enumeration;
 import java.util.Vector;
 
 import persistence.Persistence;
 import core.Actuacion;
 import core.Preferencias;
 import core.Proceso;
 import gui.PreferenciasGenerales;
 import gui.Util;
 import gui.About;
 import gui.Listados.ListadoActuaciones;
 import gui.Listados.ListadoCampos;
 import gui.Listados.ListadoCategorias;
 import gui.Listados.ListadoJuzgados;
 import gui.Listados.ListadoPersonas;
 import gui.Listados.ListadoProcesos;
 import gui.Listados.ListadoPlantillas;
 import gui.Nuevos.NuevaActuacion;
 import net.rim.device.api.system.Display;
 import net.rim.device.api.ui.Color;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FocusChangeListener;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.TransitionContext;
 import net.rim.device.api.ui.Ui;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.UiEngineInstance;
 import net.rim.device.api.ui.XYEdges;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.ListField;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.component.ObjectListField;
 import net.rim.device.api.ui.component.SeparatorField;
 import net.rim.device.api.ui.container.GridFieldManager;
 import net.rim.device.api.ui.container.MainScreen;
 import net.rim.device.api.ui.container.PopupScreen;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import net.rim.device.api.ui.decor.BackgroundFactory;
 import net.rim.device.api.ui.decor.Border;
 import net.rim.device.api.ui.decor.BorderFactory;
 
 public class ScreenMain extends MainScreen {
 
 	private  GridFieldManager _grid;
 	private  VerticalFieldManager _vertical;
 	private  ObjectListField _lista;
 	private  LabelField _juzgado;
 	private  LabelField _fecha;
 	private  LabelField _fechaProxima;
 	private  LabelField _descripcion;
 	private  final int column1 = (Display.getWidth() / 2) - 32;
 	private  final int column2 = (Display.getWidth() / 2) + 16;
 	private  final int row = Display.getHeight() - 32;
 
 	public ScreenMain() {
 		super();
 
 		try {
 			new Persistence().consultarPreferencias();
 		} catch (NullPointerException e) {
 			Util.noSd();
 		} catch (Exception e) {
 			Util.alert(e.toString());
 		}
 
 		this.getMainManager().setBackground(
 				BackgroundFactory.createSolidBackground(Color.BLACK));
 
 		actuacionesManager();
 
 		_grid = new GridFieldManager(1, 2, GridFieldManager.FIXED_SIZE);
 
 		VerticalFieldManager right = new VerticalFieldManager(VERTICAL_SCROLL
 				| VERTICAL_SCROLLBAR);
 		VerticalFieldManager left = new VerticalFieldManager(VERTICAL_SCROLL
 				| VERTICAL_SCROLLBAR);
 		left.setBorder(BorderFactory.createRoundedBorder(
 				new XYEdges(5, 5, 5, 5), Color.WHITE, Border.STYLE_SOLID));
 
 		left.add(_lista);
 		right.add(_vertical);
 
 		_grid.add(left);
 		_grid.add(right);
 
 		add(_grid);
 		left.setFocus();
 		left.invalidate();
 	}
 
 	private void actuacionesManager() {
 		initRight();
 		initLista();
 	}
 
 	private void initLista() {
		int index = 0;
		if(_lista != null) {
			index = _lista.getSelectedIndex();
		}
 		_lista = new ObjectListField() {
 			public void drawListRow(ListField listField, Graphics graphics,
 					int index, int y, int width) {
 				Actuacion objeto = (Actuacion) this.get(listField, index);
 				graphics.setColor(Color.WHITE);
 				graphics.drawText(objeto.toString(), 0, y);
 				graphics.drawText(
 						Util.calendarToString(objeto.getFechaProxima(), false),
 						15, y + getFont().getHeight());
 			}
 		};
 		_lista.setFont(_lista.getFont().derive(_lista.getFont().getStyle(),
 				_lista.getFont().getHeight() - 8));
 		_lista.setRowHeight(_lista.getFont().getHeight() * 2);
 
 		try {
 			Vector v = new Persistence().consultarActuacionesCriticas(Preferencias.getCantidadActuacionesCriticas());
 			Enumeration e = v.elements();
 			while (e.hasMoreElements()) {
 				_lista.insert(_lista.getSize(), e.nextElement());
 			}
 		} catch (NullPointerException e) {
 			Util.noSd();
 		} catch (Exception e) {
 			Util.alert(e.toString());
 		}
 		_lista.setFocusListener(listener);
		_lista.setSelectedIndex(index);
		_lista.invalidate();
		_vertical.invalidate();
 	}
 	
 	private void initRight() {
 		_vertical = new VerticalFieldManager();
 		_vertical.setFont(_vertical.getFont().derive(
 				_vertical.getFont().getStyle(),
 				_vertical.getFont().getHeight() - 5));
 
 		LabelField lblDescripcion = new LabelField("Descripcin:", FOCUSABLE) {
 			protected void paint(Graphics g) {
 				g.setColor(Color.WHITE);
 				super.paint(g);
 			}
 		};
 		LabelField lblJuzgado = new LabelField("Juzgado:", FOCUSABLE) {
 			protected void paint(Graphics g) {
 				g.setColor(Color.WHITE);
 				super.paint(g);
 			}
 		};
 		LabelField lblFecha = new LabelField("Fecha:", FOCUSABLE) {
 			protected void paint(Graphics g) {
 				g.setColor(Color.WHITE);
 				super.paint(g);
 			}
 		};
 		LabelField lblFechaProxima = new LabelField("Fecha prxima:", FOCUSABLE) {
 			protected void paint(Graphics g) {
 				g.setColor(Color.WHITE);
 				super.paint(g);
 			}
 		};
 
 		Font bold = _vertical.getFont().derive(Font.BOLD);
 
 		lblDescripcion.setFont(bold);
 		lblJuzgado.setFont(bold);
 		lblFecha.setFont(bold);
 		lblFechaProxima.setFont(bold);
 
 		_juzgado = new LabelField("", FOCUSABLE) {
 			protected void paint(Graphics g) {
 				g.setColor(Color.WHITE);
 				super.paint(g);
 			}
 		};
 		_fecha = new LabelField("", FOCUSABLE) {
 			protected void paint(Graphics g) {
 				g.setColor(Color.WHITE);
 				super.paint(g);
 			}
 		};
 		_fechaProxima = new LabelField("", FOCUSABLE) {
 			protected void paint(Graphics g) {
 				g.setColor(Color.WHITE);
 				super.paint(g);
 			}
 		};
 		_descripcion = new LabelField("", FOCUSABLE) {
 			protected void paint(Graphics g) {
 				g.setColor(Color.WHITE);
 				super.paint(g);
 			}
 		};
 
 		_vertical.add(lblDescripcion);
 		_vertical.add(_descripcion);
 		_vertical.add(new SeparatorField());
 		_vertical.add(lblJuzgado);
 		_vertical.add(_juzgado);
 		_vertical.add(new SeparatorField());
 		_vertical.add(lblFecha);
 		_vertical.add(_fecha);
 		_vertical.add(new SeparatorField());
 		_vertical.add(lblFechaProxima);
 		_vertical.add(_fechaProxima);
 	}
 	
 	private FocusChangeListener listener = new FocusChangeListener() {
 
 		public void focusChanged(Field field, int context) {
 			try {
 				Actuacion a = (Actuacion) _lista.get(_lista,
 						_lista.getSelectedIndex());
 				if (a == null) {
 					a = (Actuacion) _lista.get(_lista, 0);
 				}
 				_descripcion.setText(a.getDescripcion());
 				_juzgado.setText(a.getJuzgado().getNombre());
 				String fecha = Util.calendarToString(a.getFecha(), true);
 				_fecha.setText(fecha);
 				fecha = Util.calendarToString(a.getFechaProxima(), true);
 				_fechaProxima.setText(fecha);
 			} catch (Exception e) {
 			} finally {
 				_grid.setColumnProperty(0, GridFieldManager.FIXED_SIZE, column1);
 				_grid.setColumnProperty(1, GridFieldManager.FIXED_SIZE, column2);
 				_grid.setRowProperty(0, GridFieldManager.FIXED_SIZE, row);
 			}
 		}
 	};
 
 	protected void makeMenu(Menu menu, int instance) {
 		menu.add(menuListas);
 		menu.add(menuNuevos);
 		menu.add(menuPreferencias);
 		menu.add(menuCerrar);
 		menu.add(menuAcerca);
 	}
 
 	private final MenuItem menuListas = new MenuItem("Listado", 65537, 0) {
 
 		public void run() {
 			UiApplication.getUiApplication().pushModalScreen(
 					new Listados(Listados.LISTA));
 		}
 	};
 
 	private final MenuItem menuNuevos = new MenuItem("Nuevo", 65537, 1) {
 
 		public void run() {
 			UiApplication.getUiApplication().pushModalScreen(
 					new Listados(Listados.NUEVO));
 		}
 	};
 
 	private final MenuItem menuAcerca = new MenuItem("Acerca de", 262147, 3) {
 
 		public void run() {
 			TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
 		    transition.setIntAttribute(TransitionContext.ATTR_DURATION, 500);
 		    transition.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_DOWN);
 		    transition.setIntAttribute(TransitionContext.ATTR_STYLE, TransitionContext.STYLE_OVER);
 		    About nextScreen = new About();
 		    UiEngineInstance engine = Ui.getUiEngineInstance();
 		    engine.setTransition(null, nextScreen, UiEngineInstance.TRIGGER_PUSH, transition);
 		    Util.pushModalScreen(nextScreen);
 		}
 	};
 
 	private final MenuItem menuPreferencias = new MenuItem("Preferencias",
 			131075, 2) {
 
 		public void run() {
 			PreferenciasGenerales p = new PreferenciasGenerales();
 			Util.pushModalScreen(p.getScreen());
 		}
 	};
 	private MenuItem menuCerrar = new MenuItem("Salir de Aplicacin",
 			1000000000, 4) {
 
 		public void run() {
 			System.exit(0);
 		}
 	};
 }
 
 class Listados extends PopupScreen {
 
 	private ObjectListField _lista;
 	private int _style;
 
 	public static final short LISTA = 1;
 	public static final short NUEVO = 2;
 
 	public Listados(int style) {
		super(new VerticalFieldManager());
 		_style = style;
 		_lista = new ObjectListField();
 		if ((_style & LISTA) == LISTA) {
 			add(new LabelField("Ver listado de:", FIELD_HCENTER));
 			Object[] o = { "Demandantes", "Demandados", "Juzgados",
 					"Campos personalizados", "Categoras", "Procesos",
 					"Plantillas", "Actuaciones"};
 			_lista.set(o);
 		} else if ((_style & NUEVO) == NUEVO) {
 			add(new LabelField("Crear:", FIELD_HCENTER));
 			Object[] o = { "Demandante", "Demandado", "Juzgado",
 					"Campos personalizado", "Categora", "Proceso",
 					"Plantilla", "Actuacin"};
 			_lista.set(o);
 		}
 		add(new SeparatorField());
 		add(_lista);
 	}
 
 	protected boolean navigationClick(int arg0, int arg1) {
 		int index = _lista.getSelectedIndex();
 		UiApplication.getUiApplication().popScreen(this);
 		if (index == 0) {
 			if ((_style & LISTA) == LISTA) {
 				Util.listadoPersonas(1, false, ListadoPersonas.ON_CLICK_VER);
 			} else if ((_style & NUEVO) == NUEVO) {
 				Util.nuevaPersona(1);
 			}
 		} else if (index == 1) {
 			if ((_style & LISTA) == LISTA) {
 				Util.listadoPersonas(2, false, ListadoPersonas.ON_CLICK_VER);
 			} else if ((_style & NUEVO) == NUEVO) {
 				Util.nuevaPersona(2);
 			}
 		} else if (index == 2) {
 			if ((_style & LISTA) == LISTA) {
 				Util.listadoJuzgados(false, ListadoJuzgados.ON_CLICK_VER);
 			} else if ((_style & NUEVO) == NUEVO) {
 				Util.nuevoJuzgado();
 			}
 		} else if (index == 3) {
 			if ((_style & LISTA) == LISTA) {
 				Util.listadoCampos(false, ListadoCampos.ON_CLICK_VER);
 			} else if ((_style & NUEVO) == NUEVO) {
 				Util.nuevoCampoPersonalizado();
 			}
 		} else if (index == 4) {
 			if ((_style & LISTA) == LISTA) {
 				Util.listadoCategorias(false, ListadoCategorias.ON_CLICK_VER);
 			} else if ((_style & NUEVO) == NUEVO) {
 				Util.nuevaCategoria(false);
 			}
 		} else if (index == 5) {
 			if ((_style & LISTA) == LISTA) {
 				Util.listadoProcesos(false, ListadoProcesos.ON_CLICK_VER);
 			} else if ((_style & NUEVO) == NUEVO) {
 				Util.nuevoProceso();
 			}
 		} else if (index == 6) {
 			if ((_style & LISTA) == LISTA) {
 				Util.listadoPlantillas(false, ListadoPlantillas.ON_CLICK_VER);
 			} else if ((_style & NUEVO) == NUEVO) {
 				Util.nuevaPlantilla();
 			}
 		} else if (index == 7) {
 			ListadoProcesos procesos = new ListadoProcesos(true, ListadoProcesos.NO_NUEVO);
 			procesos.setTitle("Seleccione un proceso");
 			Util.pushModalScreen(procesos.getScreen());
 			Proceso proceso = procesos.getSelected();
 			if (proceso != null) {
 				if ((_style & LISTA) == LISTA) {
 					ListadoActuaciones actuaciones = new ListadoActuaciones(
 							proceso);
 					Util.pushModalScreen(actuaciones.getScreen());
 				} else if ((_style & NUEVO) == NUEVO) {
 					NuevaActuacion actuacion = new NuevaActuacion(proceso);
 					Util.pushModalScreen(actuacion.getScreen());
 				}
 			}
 		}
 		return true;
 	}
 
 	public boolean onClose() {
 		UiApplication.getUiApplication().popScreen(this);
 		return true;
 	}
 }
