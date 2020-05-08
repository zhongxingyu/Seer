 package ehmsoft;
 
 import java.util.Enumeration;
 import java.util.Vector;
 
 import persistence.ConnectionManager;
 import persistence.Persistence;
 import core.Actuacion;
 import core.Plantilla;
 import core.ActuacionCritica;
 import core.Preferencias;
 import core.Proceso;
 import gui.Cita;
 import gui.Llaves;
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
 import gui.Nuevos.NuevoProceso;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.system.Display;
 import net.rim.device.api.ui.Color;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FocusChangeListener;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.FontFamily;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.TransitionContext;
 import net.rim.device.api.ui.Ui;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.UiEngine;
 import net.rim.device.api.ui.UiEngineInstance;
 import net.rim.device.api.ui.XYEdges;
 import net.rim.device.api.ui.component.Dialog;
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
 import net.rim.device.api.ui.decor.BorderFactory;
 
 public class ScreenMain extends MainScreen {
 	private String _licencia = "AVISO IMPORTANTE: Antes de descargar, instalar, copiar o utilizar " +
 			"el software lea los siguientes trminos y condiciones: ehmSoftware es el propietario del " +
 			"software y del soporte fsico. ehmSoftware  concede este software bajo una licencia " +
 			"exclusivamente de uso a una persona natural o jurdica. Disponiendo de sta, la instalacin podr " +
 			"realizarse solamente en un dispositivo, el cual est permitido cambiarlo, para esto debe " +
 			"eliminar por completo la instalacin en el dispositivo anterior y contactar a ehmSoftware. Para " +
 			"que el software funcione correctamente se debe contar con un sistema operativo BlackBerry OS 5.0 " +
 			"o superior y una tarjeta microSD presente en el dispositivo, ehmSoftware no se har responsable " +
 			"por prdidas, daos, reclamaciones o costes de cualquier naturaleza, incluyendo cualquier dao " +
 			"consecuente, indirecto o secundario, ni de cualquier prdida de beneficios o ganancias, daos " +
 			"que resulten de la interrupcin del negocio, dao personal o incumplimiento de cualquier deber " +
 			"de diligencia o reclamaciones de terceros, del mismo modo no garantiza que el software este " +
 			"libre de errores y se recomienda que en el caso de que stos se presente sean reportados a soporte@ehmsoft.com " +
 			"para realizar el seguimiento de los mismos y tomar medidas para su correccin en el menor tiempo " +
 			"posible. ehmSoftware se reserva todos los derechos que no se le conceden expresamente a usted en " +
 			"este documento y puede retirar la licencia de uso si usted no cumple con los trminos y condiciones. " +
 			"Si usted presiona el botn S indica que ha ledo, comprendido y est de acuerdo con todos los " +
 			"trminos y condiciones expuestos en el presente documento as como con la licencia exclusivamente " +
 			"de uso del software. Si no est de acuerdo presione el botn No, destruya todas las copias del software " +
 			"que tenga en su poder y pngase en contacto con ehmSoftware. ";
 	private GridFieldManager _grid;
 	private VerticalFieldManager _info;
 	private ObjectListField _lista;
 	private LabelField _juzgado;
 	private LabelField _fecha;
 	private LabelField _fechaProxima;
 	private LabelField _descripcion;
 	private VerticalFieldManager _fldLista;
 	private VerticalFieldManager _fldInfo;
 	private int column1;
 	private int column2;
 	private int row;
 	private int row1;
 	private int row2;
 	private int column;
 	private Font _defaultFont = Font.getDefault();
 	
 
 	public ScreenMain() {
 		super(NO_VERTICAL_SCROLL);
 						
 		try {
 			if (!new ConnectionManager().existeBD()) {
 				String licencia = _licencia + "\nAcepta la licencia?";
 				if (Dialog.ask(Dialog.D_YES_NO, licencia, Dialog.NO) != Dialog.YES) {
 					System.exit(0);
 				}
 				_licencia = null; 
 				licencia = null;
 			}
 		} catch (NullPointerException e) {
 			Dialog.alert(Util.noSDString());
 			System.exit(0);
 		} catch (Exception e) {
 			Dialog.alert(e.toString());
 		}
 		
 		dibujarPantalla();
 		cargarBD();
 	}
 	
 	private void cargarParametros() {
 		column1 = (Display.getWidth() / 2) - (int) (Display.getWidth() * (30.3 / 480));
 		column2 = (Display.getWidth() / 2) + (int) (Display.getWidth() * (16.3 / 480));
 		row = Display.getHeight()	- (int) (Display.getHeight() * (13.3 / 360));
 		row1 = Display.getHeight() / 2 - (int) (Display.getWidth() * (32.3 / 480));
 		row2 = Display.getHeight() / 2;
 		column = Display.getWidth() - 7 - (int) (Display.getWidth() * (32.3 / 360));
 	}
 	
 	private void dibujarPantalla() {
 		
 		cargarParametros();		
 		try {
 			FontFamily fontF = FontFamily.forName("BBAlpha Sans");
 
 			if (Display.getOrientation() == Display.ORIENTATION_LANDSCAPE) {
 				_defaultFont = fontF.getFont(0, Display.getHeight() / 13);
 			} else if (Display.getOrientation() == Display.ORIENTATION_PORTRAIT) {
 				_defaultFont = fontF.getFont(0, Display.getHeight() / 24);
 			}
 
 			
 		} catch (ClassNotFoundException e1) {
 		}
 		
 		actuacionesManager();
 		
 		Bitmap backGround = Bitmap.getBitmapResource("bg480x360.png");
 		if(Display.getWidth() == 320 && Display.getHeight() == 240) {
 			backGround = Bitmap.getBitmapResource("bg320x240.png");
 		} else if(Display.getWidth() == 320 && Display.getHeight() == 480) {
 			backGround = Bitmap.getBitmapResource("bg320x480.png");
 		} else if(Display.getWidth() == 360 && Display.getHeight() == 480) {
 			backGround = Bitmap.getBitmapResource("bg360x480.png");
 		} else if(Display.getWidth() == 480 && Display.getHeight() == 360) {
 			backGround = Bitmap.getBitmapResource("bg480x360.png");
 		}
 
 		getMainManager().setBackground(
 				BackgroundFactory.createBitmapBackground(backGround));
 		
 		_fldLista = new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR);
 		_fldInfo = new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR);
 		
 		_fldLista.add(_lista);
 		_fldInfo.add(_info);
 		
 		_fldLista.setBorder(BorderFactory.createBitmapBorder(new XYEdges(5, 5, 5, 5), Bitmap.getBitmapResource("blackBorder.png")));
 		
 		if (Display.getOrientation() == Display.ORIENTATION_PORTRAIT) {
 			_grid = new GridFieldManager(2, 1, GridFieldManager.FIXED_SIZE) {
 				public Field getField(int index) {
 					try {
 						return super.getField(index);
 					} catch (ArrayIndexOutOfBoundsException e) {
 						return super.getField(0);
 					}
 				}
 			};
 		} else if (Display.getOrientation() == Display.ORIENTATION_LANDSCAPE) {
 			_grid = new GridFieldManager(1, 2, GridFieldManager.FIXED_SIZE);
 		}
 		
 		add(_grid);
 		
 		_grid.add(_fldLista);
 		_grid.add(_fldInfo);
 	}
 	
 	private void cargarBD() {
 		final PopupScreen wait = new PopupScreen(new VerticalFieldManager());
 		wait.add(new LabelField("Por favor espere..."));
 		wait.add(new LabelField(
 				"Verificando la base de datos y cargando las preferencias, esto puede tomar unos minutos"));
 		UiApplication.getUiApplication().pushGlobalScreen(wait, 0,
 				UiEngine.GLOBAL_SHOW_LOWER);
 
 		UiApplication.getUiApplication().invokeLater(new Runnable() {
 
 			public void run() {
 				try {
 					new ConnectionManager().prepararBD();
 					new Persistence().consultarPreferencias();
 				} catch (NullPointerException e) {
 					UiApplication.getUiApplication().popScreen(wait);
 					Dialog.alert(Util.noSDString());
 					System.exit(0);
 				} catch (Exception e) {
 					UiApplication.getUiApplication().popScreen(wait);
 					Util.alert(e.toString());
 				}
 				UiApplication.getUiApplication().popScreen(wait);
 				Llaves llaves = new Llaves();
 				if (!llaves.verificarLlaves()) {
 					UiApplication.getUiApplication().pushModalScreen(
 							llaves.getScreen());
 					if (!llaves.verificarLlaves()) {
 						System.exit(0);
 					}
 				}
 				cargarActuaciones();
				_fldLista.setFocus();
 			}
 		});
 	}
 
 	private void cargarActuaciones() {
 		setStatus(Util.getWaitLabel());
 		UiApplication.getUiApplication().invokeLater(new Runnable() {
 
 			public void run() {
 				try {
 					_fldLista.deleteAll();
 					initLista();
 					Vector v = new Persistence()
 							.consultarActuacionesCriticas(Preferencias
 									.getCantidadActuacionesCriticas());
 					Enumeration e = v.elements();
 					while (e.hasMoreElements()) {
 						_lista.insert(_lista.getSize(), e.nextElement());
 
 					}
 				} catch (NullPointerException e) {
 					Util.noSd();
 				} catch (Exception e) {
 					Util.alert(e.toString());
 				} finally {
 					_fldLista.add(_lista);
 				}
 				_lista.focusChangeNotify(0);
 				setStatus(null);
 				_grid.invalidate();
 			}
 		});
 	}
 
 	private void actuacionesManager() {
 		initLista();
 		initInfo();
 	}
 
 	private void initLista() {
 		_lista = new ObjectListField() {
 			public void drawListRow(ListField listField, Graphics graphics,
 					int index, int y, int width) {
 				Actuacion objeto = (Actuacion) this.get(listField, index);
 				Cita cita = new Cita(objeto.getUid());
 				int count = 0;
 				if(cita.exist()) {
 					graphics.drawBitmap(0, y, 16, 16, Bitmap.getBitmapResource("clock.png"), 0, 0);
 					count += 16;
 					if(cita.hasAlarma()) {
 						graphics.drawBitmap(count, y, 16, 16, Bitmap.getBitmapResource("bell.png"), 0, 0);
 						count += 16;
 					}
 				}
 				graphics.drawText(objeto.toString(), count, y);
 				graphics.drawText(
 						Util.calendarToString(objeto.getFechaProxima(), false),
 						(int) (Display.getWidth() * 13.3 / 480), y
 								+ getFont().getHeight());
 			};
 
 			protected void paint(Graphics g) {
 				g.setColor(Color.WHITE);
 				super.paint(g);
 			}
 		};
 
 		Font font = _defaultFont;
 		float fHeight = font.getHeight();
 		int fStyle = font.getStyle();
 		int resultHeight = (int)fHeight;
 		if (Display.getOrientation() == Display.ORIENTATION_LANDSCAPE) {
 			resultHeight = (int)(fHeight - fHeight * (8.0 / 27));
 			_lista.setFont(font.derive(fStyle, resultHeight));
 		} else if (Display.getOrientation() == Display.ORIENTATION_PORTRAIT) {
 			resultHeight = (int)(fHeight - fHeight * (5.0 / 20));
 		}
 		_lista.setFont(font.derive(fStyle, resultHeight));
 		_lista.setRowHeight(_lista.getFont().getHeight() * 2);
 
 		_lista.setFocusListener(listener);
 	}
 
 	private void initInfo() {
 		_info = new VerticalFieldManager();
 		Font font = _defaultFont;
 		float fHeight = font.getHeight();
 		int fStyle = font.getStyle();
 		int resultHeight = (int)fHeight;
 		if (Display.getOrientation() == Display.ORIENTATION_LANDSCAPE) {
 			resultHeight = (int)(fHeight - fHeight * (6.0 / 27));
 		} else if (Display.getOrientation() == Display.ORIENTATION_PORTRAIT) {
 			resultHeight = (int)(fHeight - fHeight * (4.0 / 20));
 		}
 		_info.setFont(font.derive(fStyle, resultHeight));
 
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
 
 		Font bold = _info.getFont().derive(Font.BOLD);
 
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
 
 		_info.add(lblDescripcion);
 		_info.add(_descripcion);
 		_info.add(new SeparatorField());
 		_info.add(lblJuzgado);
 		_info.add(_juzgado);
 		_info.add(new SeparatorField());
 		_info.add(lblFecha);
 		_info.add(_fecha);
 		_info.add(new SeparatorField());
 		_info.add(lblFechaProxima);
 		_info.add(_fechaProxima);
 	}
 
 	private FocusChangeListener listener = new FocusChangeListener() {
 
 		public void focusChanged(Field field, int context) {
 			try {
 				if (Display.getOrientation() == Display.ORIENTATION_LANDSCAPE) {
 					_grid.setColumnProperty(0, GridFieldManager.FIXED_SIZE,
 							column1);
 					_grid.setColumnProperty(1, GridFieldManager.FIXED_SIZE,
 							column2);
 					_grid.setRowProperty(0, GridFieldManager.FIXED_SIZE, row);
 
 				} else if (Display.getOrientation() == Display.ORIENTATION_PORTRAIT) {
 					_grid.setRowProperty(0, GridFieldManager.FIXED_SIZE, row1);
 					_grid.setRowProperty(1, GridFieldManager.FIXED_SIZE, row2);
 					_grid.setColumnProperty(0, GridFieldManager.FIXED_SIZE,
 							column);
 					_grid.setPadding(new XYEdges(
 							(int) (Display.getWidth() * (16.3 / 360)), 0, 0,
 							(int) (Display.getWidth() * (16.3 / 360))));
 				}
 
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
 			}
 		}
 	};
 
 	protected void makeMenu(Menu menu, int instance) {
 		if (_lista.getSize() != 0) {
 			menu.add(menuVerProceso);
 			menu.add(menuVerActuacion);
 		}
 		menu.add(menuListas);
 		menu.add(menuNuevos);
 		menu.add(menuPreferencias);
 		menu.add(menuCerrar);
 		menu.add(menuAcerca);
 	}
 
 	private final MenuItem menuVerProceso = new MenuItem("Ver proceso", 65537,
 			0) {
 
 		public void run() {
 			ActuacionCritica a = (ActuacionCritica) _lista.get(_lista,
 					_lista.getSelectedIndex());
 			Proceso p = null;
 			try {
 				p = new Persistence().consultarProceso(a.getId_proceso());
 			} catch (NullPointerException e) {
 				Util.noSd();
 			} catch (Exception e) {
 				Util.alert(e.toString());
 			}
 			if (p != null) {
 				Util.verProceso(p);
 				cargarActuaciones();
 			}
 		}
 	};
 
 	private final MenuItem menuVerActuacion = new MenuItem("Ver actuacion",
 			65537, 1) {
 
 		public void run() {
 			ActuacionCritica a = (ActuacionCritica) _lista.get(_lista,
 					_lista.getSelectedIndex());
 			Util.verActuacion(a);
 			cargarActuaciones();
 		}
 	};
 
 	private final MenuItem menuListas = new MenuItem("Listado", 131075, 2) {
 
 		public void run() {
 			UiApplication.getUiApplication().pushModalScreen(
 					new Listados(Listados.LISTA));
 			cargarActuaciones();
 		}
 	};
 
 	private final MenuItem menuNuevos = new MenuItem("Nuevo", 131075, 3) {
 
 		public void run() {
 			UiApplication.getUiApplication().pushModalScreen(
 					new Listados(Listados.NUEVO));
 			cargarActuaciones();
 		}
 	};
 
 	private final MenuItem menuAcerca = new MenuItem("Acerca de", 327682, 5) {
 
 		public void run() {
 			TransitionContext transition = new TransitionContext(
 					TransitionContext.TRANSITION_SLIDE);
 			transition.setIntAttribute(TransitionContext.ATTR_DURATION, 500);
 			transition.setIntAttribute(TransitionContext.ATTR_DIRECTION,
 					TransitionContext.DIRECTION_DOWN);
 			transition.setIntAttribute(TransitionContext.ATTR_STYLE,
 					TransitionContext.STYLE_OVER);
 			About nextScreen = new About();
 			UiEngineInstance engine = Ui.getUiEngineInstance();
 			engine.setTransition(null, nextScreen,
 					UiEngineInstance.TRIGGER_PUSH, transition);
 			Util.pushModalScreen(nextScreen);
 			cargarActuaciones();
 		}
 	};
 
 	private final MenuItem menuPreferencias = new MenuItem("Preferencias",
 			262147, 4) {
 
 		public void run() {
 			PreferenciasGenerales p = new PreferenciasGenerales();
 			Util.pushModalScreen(p.getScreen());
 			cargarActuaciones();
 		}
 	};
 	private MenuItem menuCerrar = new MenuItem("Salir de Aplicacin",
 			1000000000, 6) {
 
 		public void run() {
 			System.exit(0);
 		}
 	};
 
 	public boolean onClose() {
 		System.exit(0);
 		return true;
 	}
 	
 	protected void paint(Graphics g) {
 		if (Display.getOrientation() == Display.ORIENTATION_LANDSCAPE) {
 			if(_grid.getRowCount() > 1) {
 				deleteAll();
 				dibujarPantalla();
 				cargarBD();
 			}
 		} else if (Display.getOrientation() == Display.ORIENTATION_PORTRAIT) {
 			if(_grid.getColumnCount() > 1) {
 				deleteAll();
 				dibujarPantalla();
 				cargarBD();
 			}
 		}
 		super.paint(g);
 	}
 }
 
 class Listados extends PopupScreen {
 
 	private ObjectListField _lista;
 	private int _style;
 
 	public static final short LISTA = 1;
 	public static final short NUEVO = 2;
 
 	public Listados(int style) {
 		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR));
 		_style = style;
 		_lista = new ObjectListField();
 		if ((_style & LISTA) == LISTA) {
 			add(new LabelField("Ver listado de:", FIELD_HCENTER));
 			Object[] o = { "Demandantes", "Demandados", "Juzgados",
 					"Campos personalizados", "Categoras", "Procesos",
 					"Plantillas", "Actuaciones" };
 			_lista.set(o);
 		} else if ((_style & NUEVO) == NUEVO) {
 			add(new LabelField("Crear:", FIELD_HCENTER));
 			Object[] o = { "Demandante", "Demandado", "Juzgado",
 					"Campo personalizado", "Categora", "Proceso", "Plantilla",
 					"Actuacin", "Proceso a partir de plantilla" };
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
 			ListadoProcesos procesos = new ListadoProcesos(true,
 					ListadoProcesos.NO_NUEVO);
 			procesos.setTitle("Procesos ");
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
 		} else if (index == 8) {
 			ListadoPlantillas plantillas = new ListadoPlantillas(true,
 					ListadoPlantillas.ON_CLICK_SELECT
 							| ListadoPlantillas.NO_NUEVO);
 			plantillas.setTitle("Seleccione una plantilla");
 			Util.pushModalScreen(plantillas.getScreen());
 			Plantilla plantilla = plantillas.getSelected();
 			if (plantilla != null) {
 				NuevoProceso n = new NuevoProceso(plantilla);
 				UiApplication.getUiApplication().pushModalScreen(n.getScreen());
 			}
 		}
 		return true;
 	}
 
 	public boolean onClose() {
 		UiApplication.getUiApplication().popScreen(this);
 		return true;
 	}
 }
