 package simulation.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.EnumMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JPanel;
 
 import order.Message.Type;
 
 import simulation.arrows.MessageArrow;
 import simulation.arrows.MultipleArrow;
 import simulation.model.SimulationArrowModel;
 import simulation.view.listener.CreateListener;
 import simulation.view.listener.DeleteListener;
 import simulation.view.listener.MoveListener;
 import simulation.view.listener.ViewListener;
 
 @SuppressWarnings("serial")
 public class SimulationView extends JPanel implements Observer {
 
 	public static enum State {
 		OVER, CREATE, MOVE, DELETE
 	}
 
 	/*
 	 * Constantes
 	 */
 
 	// Constantes del tamaño de las celdas
 	public static final int cellWidth = 40;
 	public static final int cellHeight = 25;
 
 	public static final int paddingX = 40;
 	public static final int paddingY = 30;
 
 	// Constantes de color
 	private static final Color tickLineCol = Color.getHSBColor(.7f, .08f, .9f);
 	private static final Color evenTickCol = Color.getHSBColor(.7f, .05f, 1f);
 	private static final Color oddTickCol = Color.getHSBColor(.4f, .02f, 1f);
 	private static final Color overCellColor = new Color(1f, 1f, 1f, .8f);
 	private static final Color overColColor = new Color(0f, 0f, 0f, .04f);
 	private static final Color invalidOverColor = new Color(1f, .6f, .6f);
 
 	/*
 	 * Atributos
 	 */
 
 	// El modelo de datos
 	private SimulationArrowModel simulationModel;
 
 	// Numero de procesos y de ticks
 	private int processes;
 	private int ticks;
 
 	// Dimensiones del tablero (en pixels)
 	private int width = ticks * cellWidth + paddingX + 1;
 	private int height = processes * (cellHeight + paddingY) + 1;
 
 	// Atributo que usamos para guardar sobre que estamos (celda, corte o fuera
 	// de la pantalla)
 	private IPosition overPosition;
 	private Boolean validOverPosition = true;
 
 	// Flecha que estamos modificando sobre la vista. Como no es una flecha
 	// definitiva, trabajamos directamente sobre la vista en vez del modelo.
 	private MessageArrow drawingArrow;
 
 	// Implementamos un doble buffer, realizando las operaciones de dibujo sobre
 	// el buffer, y después volcando la imagen sobre el contexto del panel.
 	BufferedImage backBuffer;
 
 	// Cuando redimensionamos nuestra vista necesitamos ejecutar super.paint()
 	// para limpiar la ventana.
 	private boolean screenResized;
 
 	// Guardamos en una lista, los listeners que controlan el comportamiento de
 	// la vista.
 	private ViewListener[] listViewListeners = new ViewListener[State.values().length];
 
 	// Guardamos una referencia al listener actual para poder cambiarlo por otro
 	private ViewListener actualViewListener;
 
 	// Lista de observadores de posición. Como por ejemplo el panel de
 	// información de relojes lógicos.
 	private List<IPositionObserver> positionObservers = new LinkedList<IPositionObserver>();
 
 	// Lista de simuladores de información
 	private List<ISimulator> simulatorList = new LinkedList<ISimulator>();
 
 	/**
 	 * Crea un nuevo tablero con el simulation model.
 	 */
 	public SimulationView(SimulationArrowModel simulationModel) {
 
 		this.simulationModel = simulationModel;
 		this.simulationModel.addObserver(this);
 
 		createViewListeners();
 
 		// Por defecto el comportamiento es moverse con el ratón.
 		this.setState(State.CREATE);
 
 		updateData();
 	}
 
 	/**
 	 * Creamos los listeners que controlan el comportamiento de la vista.
 	 * 
 	 */
 	private void createViewListeners() {
 		listViewListeners[State.OVER.ordinal()] = new ViewListener(this);
 		listViewListeners[State.CREATE.ordinal()] = new CreateListener(this);
 		listViewListeners[State.MOVE.ordinal()] = new MoveListener(this);
 		listViewListeners[State.DELETE.ordinal()] = new DeleteListener(this);
 	}
 
 	/*
 	 * Funciones de dibujo
 	 */
 
 	/**
 	 * Dibujar el tablero.
 	 * 
 	 * @param g
 	 *            el contexto grafico en el cual se pinta.
 	 */
 	@Override
 	public void paint(Graphics g) {
 
 		// Obtenemos el doble buffer
 		Graphics2D g2 = (Graphics2D) backBuffer.getGraphics();
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 
 		// Dibujamos las columnas y los procesos
 		paintColumns(g2);
 		paintProcesses(g2);
 
 		// Dibujamos las flechas
 		paintArrows(g2, simulationModel.getArrowList());
 
 		// Si estamos editando una flecha dibujamos la fecha en dibujo
 		if (this.drawingArrow != null)
 			drawingArrow.draw(g2);
 
 		// Si no mostramos la simulación realizada por los simuladores
 		else
 			for (ISimulator simul : this.simulatorList)
 				simul.drawSimulation(g2);
 
 		if (screenResized)
 			super.paint(g);
 
 		// Dibujamos sobre el doble buffer
 		g.drawImage(backBuffer, 0, 0, this);
 	}
 
 	/**
 	 * Dibujar las lineas verticales del tablero.
 	 * 
 	 * @param g
 	 *            el contexto grafico en el cual se pinta.
 	 */
 	private void paintColumns(Graphics2D g) {
 
 		int x0, x1;
 		int y0 = 0;
 		int y1 = height;
 		x1 = cellWidth;
 
 		boolean even = true;
 
 		for (int i = 0; i < ticks; i++) {
 			x0 = i * cellWidth + paddingX;
 
 			g.setColor(even ? evenTickCol : oddTickCol);
 
 			even = !even;
 			g.fillRect(x0, y0, x1, y1);
 			g.setPaint(tickLineCol);
 			g.drawRect(x0, y0, x1, y1);
 		}
 
 		// Dibujamos el efecto over de iluminación. Osea iluminamos toda la
 		// columna.
 		if (overPosition instanceof CutPosition) {
 
 			CutPosition pos = (CutPosition) overPosition;
 			x0 = pos.tick * cellWidth + paddingX;
 			g.setColor(overColColor);
 			g.fillRect(x0, y0, x1, y1);
 
 		}
 	}
 
 	/**
 	 * Dibujar las lineas horizontales del tablero.
 	 * 
 	 * @param g
 	 *            el contexto grafico en el cual se pinta.
 	 */
 	private void paintProcesses(Graphics g) {
 
 		int x1 = cellWidth;
 		int y1 = cellHeight;
 
 		for (int i = 0; i <= processes; i++) {
 			int y0 = i * (cellHeight + paddingY) + paddingY;
 
 			for (int j = 0; j < ticks; j++) {
 				int x0 = paddingX + cellWidth * j;
 
 				g.setColor(Color.getHSBColor(i * .10f % 1, .20f, 1f));
 				g.fillRect(x0, y0, x1, y1);
 				g.setColor(Color.getHSBColor(i * .10f % 1, .50f, .6f));
 				g.drawRect(x0, y0, x1, y1);
 			}
 		}
 
 		// Dibujamos el efecto over de iluminación. Iluminamos la celda cuando
 		// pasamos por encima.
 		if (overPosition instanceof CellPosition) {
 
 			CellPosition pos = (CellPosition) overPosition;
 			int x0 = pos.tick * cellWidth + paddingX;
 			int y0 = pos.process * (cellHeight + paddingY) + paddingY;
 
 			// Si la posicion over es invalida, la pintamos de rojo
 			if (!validOverPosition)
 				g.setColor(invalidOverColor);
 			else
 				g.setColor(overCellColor);
 
 			g.fillRect(x0 + 1, y0 + 1, x1 - 1, y1 - 1);
 
 		}
 	}
 
 	/**
 	 * Dibuja las flechas
 	 * 
 	 * @param g
 	 *            el contexto grafico en el cual se pinta.
 	 */
 	public static void paintArrows(Graphics2D g,
 			List<? extends MessageArrow> arrowList) {
 		for (MessageArrow arrow : arrowList) {
 			arrow.draw(g);
 		}
 	}
 
 	/*
 	 * Getters y setters
 	 */
 
 	/**
 	 * Cambiamos el modo de comportamiento de la vista :
 	 * <ul>
 	 * <li>Type.Over = Moverse sin hacer nada</li>
 	 * <li>Type.Create = Crear flechas, y mover los puntos del final</li>
 	 * <li>Type.Move = Mover flechas</li>
 	 * <li>Type.Delete = Eliminar flechas</li>
 	 * </ul>
 	 * 
 	 * @param state
 	 */
 	public void setState(State state) {
 		if (actualViewListener != null) {
 			this.removeMouseListener(actualViewListener);
 			this.removeMouseMotionListener(actualViewListener);
 		}
 		actualViewListener = listViewListeners[state.ordinal()];
 		this.addMouseListener(actualViewListener);
 		this.addMouseMotionListener(actualViewListener);
 	}
 
 	/**
 	 * 
 	 * @return Devuelve las propiedades de creación de flechas del
 	 *         CreateListener.
 	 */
 	public EnumMap<Type, Boolean> getCreationProperties() {
 		CreateListener listener = (CreateListener) listViewListeners[State.CREATE
 				.ordinal()];
 		return listener.getProperties();
 	}
 
 	/*
 	 * Métodos para obtener y cambiar el modelo de flechas (patrón MVC).
 	 * 
 	 * Podemos cambiar el modelo de flechas ya que soportamos la carga de
 	 * escenacios de flechas desde ficheros de datos.
 	 */
 
 	/**
 	 * Cambia el modelo (patron MVC)
 	 * 
 	 * @param simulationModel
 	 */
 	public void setSimulationModel(SimulationArrowModel simulationModel) {
 
		this.simulationModel.deleteObserver(this);
 		this.simulationModel = simulationModel;
		this.simulationModel.addObserver(this);
 
 		for (MultipleArrow arrow : this.simulationModel.getArrowList())
 			arrow.initialize();
 
 		for (ViewListener view : listViewListeners) {
 			view.updateModel();
 		}
 		updateData();
 	}
 
 	/**
 	 * Devuelve la clase modelo
 	 */
 	public SimulationArrowModel getSimulationModel() {
 		return this.simulationModel;
 	}
 
 	/*
 	 * Funciones para establecer la flecha que estamos editando sobre el canvas.
 	 */
 
 	/**
 	 * @param drawingArrow
 	 *            the drawingArrow to set
 	 */
 	public void setDrawingArrow(MessageArrow drawingArrow) {
 		this.drawingArrow = drawingArrow;
 	}
 
 	/**
 	 * @return the drawingArrow
 	 */
 	public MessageArrow getDrawingArrow() {
 		return drawingArrow;
 	}
 
 	/**
 	 * Calcula la posicion centrada en una celda según la posicion de una celda.
 	 * 
 	 * @param position
 	 * 
 	 * @return Punto centrado en la celda position
 	 */
 	public static Point2D.Float PositionCoords(CellPosition position) {
 		// Espacio de la izquierda + celda * tick + mitad celda
 		int x = paddingX + position.tick * cellWidth + cellWidth / 2;
 
 		// Espacio de arriba + celda * proceso + mitad celda
 		int y = paddingY + position.process * (cellHeight + paddingY)
 				+ cellHeight / 2;
 
 		return new Point2D.Float(x, y);
 	}
 
 	/**
 	 * Establecemos la posición sobre la que esta el cursor. Sólo si cambiamos
 	 * el estado actualizamos y repintamos la pantalla.
 	 * 
 	 * @param newPosition
 	 */
 	public void setPosition(IPosition newPosition, IPositionObserver.Mode mode) {
 		/*
 		 * - Si la posicion antigua era distinta de null y ahora es null, ya no
 		 * estamos encima de la pantalla. (evitamos el null pointer)
 		 * 
 		 * - Si la posicion antigua es distinta de null, comprobamos que la
 		 * posicion nueva sea distinta de la antigua.
 		 */
 		if (mode.equals(IPositionObserver.Mode.Over)
 				&& ((this.overPosition != null && newPosition == null) || (newPosition != null && !newPosition
 						.equals(this.overPosition)))) {
 
 			this.overPosition = newPosition;
 
 			for (IPositionObserver observer : this.positionObservers)
 				observer.setPosition(overPosition, mode);
 
 			this.repaint();
 		} else {
 			for (IPositionObserver observer : this.positionObservers)
 				observer.setPosition(overPosition, mode);
 		}
 	}
 
 	/**
 	 * Establecemos la posición sobre la que esta el cursor. Sólo si cambiamos
 	 * el estado actualizamos y repintamos la pantalla.
 	 * 
 	 * @param newPosition
 	 */
 	public void setPosition(IPosition newPosition, IPositionObserver.Mode mode,
 			Boolean valid) {
 		setPosition(newPosition, mode);
 		this.validOverPosition = valid;
 	}
 
 	/**
 	 * 
 	 * @return Devuelve la posición actual
 	 */
 	public IPosition getOverPosition() {
 		return this.overPosition;
 	}
 
 	/**
 	 * Calcular el indice de la celda en funcion de la posicion del cursor
 	 */
 	public IPosition getPosition(MouseEvent e) {
 
 		int x = e.getX();
 		int y = e.getY();
 
 		// Calculamos donde cae dentro de una fila (donde una fila es el espacio
 		// de un proceso y el espacio que tiene justo antes de el)
 		int fila = y % (paddingY + cellHeight);
 
 		// columna, que es la columna una vez hemos restado el espacio de la
 		// izquierda
 		int columna = (x - paddingX) / cellWidth;
 
 		// // Si está en el margen izquierdo null
 		if (x < paddingX || columna >= ticks || y > height - 1)
 			return null;
 
 		// Sino, puede ser o bien una celda o una columna.
 		if (fila >= paddingY) {
 
 			// Calculamos la fila
 			fila = y / (paddingY + cellHeight);
 
 			return new CellPosition(fila, columna);
 		} // Estamos en una columna
 		else
 			return new CutPosition(columna);
 	}
 
 	/**
 	 * @param obj
 	 *            Añade el objeto a la lista de observadores de posición.
 	 */
 	public void addPositionObserver(IPositionObserver obj) {
 		this.positionObservers.add(obj);
 	}
 
 	/**
 	 * @param obj
 	 *            Añade el simulador a la lista de simuladores.
 	 */
 	public void addSimulator(ISimulator obj) {
 		this.simulatorList.add(obj);
 	}
 
 	/*
 	 * Patron MVC
 	 */
 
 	/**
 	 * Actualizar la vista cuando se actualiza el modelo
 	 */
 	@Override
 	public void update(Observable o, Object arg) {
 		updateData();
 	}
 
 	public void updateData() {
 		int ticks = simulationModel.getTimeTicks();
 		int processes = simulationModel.getNumProcesses();
 
 		// Solo cambiar el tamaño de la ventana si han cambiado el número de
 		// ticks y el número de procesos.
 		if (this.ticks != ticks || this.processes != processes) {
 			this.ticks = ticks;
 			this.processes = processes;
 
 			width = ticks * cellWidth + 2 * paddingX;
 			height = processes * (cellHeight + paddingY) + paddingY;
 
 			this.backBuffer = new BufferedImage(width, height,
 					BufferedImage.TYPE_INT_ARGB);
 
 			this.screenResized = true;
 
 			this.setPreferredSize(new Dimension(width, height));
 
 		} else {
 			this.screenResized = false;
 		}
 
 		// Llamamos a los simuladores
 		for (ISimulator simul : this.simulatorList)
 			simul.simulate(this);
 
 		// Redibujamos la pantalla
 		this.repaint();
 	}
 }
