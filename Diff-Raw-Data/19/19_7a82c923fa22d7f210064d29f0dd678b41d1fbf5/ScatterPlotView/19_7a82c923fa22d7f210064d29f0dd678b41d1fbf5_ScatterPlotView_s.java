 package scatterPlot;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.TextField;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.io.File;
 import java.io.IOException;
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Vector;
 import java.util.concurrent.atomic.AtomicReference;
 
 import javax.swing.BoxLayout;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.RowFilter;
 import javax.swing.RowSorter;
 import javax.swing.SortOrder;
 import javax.swing.border.BevelBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 import org.lwjgl.BufferUtils;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Cursor;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.glu.GLU;
 
 import de.matthiasmann.twl.GUI;
 import de.matthiasmann.twl.Label;
 import de.matthiasmann.twl.Widget;
 import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
 import de.matthiasmann.twl.theme.ThemeManager;
 import edu.umd.cs.piccolo.PCamera;
 import edu.umd.cs.piccolo.PCanvas;
 import edu.umd.cs.piccolo.PNode;
 import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
 import edu.umd.cs.piccolo.event.PInputEvent;
 import edu.umd.cs.piccolo.event.PInputEventListener;
 import edu.umd.cs.piccolo.nodes.PPath;
 import edu.umd.cs.piccolo.nodes.PText;
 import edu.umd.cs.piccolox.util.PFixedWidthStroke;
 
 public class ScatterPlotView extends Widget{
 	// UI initialize
 	private final static AtomicReference<Dimension> newCanvasSize = new AtomicReference<Dimension>();
 	boolean closeRequested = false;
 
 	//camera
 	private static Camera camera;
 
 	//database
 	static ScatterPlotModel spModel;
 	JTable detailTable;
 	static Vector<ExpressionData> rawTable;
 	Vector<String> selectedItems = new Vector<String>();
 	int totalSampleSize = 0;
 	int numOfshowingDots = 0;
 
 	double maxXY;
 	double minXY;
 	double maxX;
 	double maxY;
 
 
 	//to picking
 	IntBuffer selectBuff;
 	int renderMode = GL11.GL_RENDER;
 	int clickedIndex =-1;
 	int overedIndex =-1;
 	boolean isDowned = false;
 
 	ThemeManager themeManager;
 
 
 	FileOpenDialog fileDialog = new FileOpenDialog();
 
 	//to print text
 	Label toolTipBox = new Label();
 
 
 	//layouts
 	JMenuBar menuBar;
 	JFrame mainFrame = new JFrame("RPKM Scatterplot");
 	JFrame controlFrame = new JFrame("Control Frame");
 	final Canvas canvas = new Canvas();
 
 	//labels
 	Label xAxisLabel = new Label("X");
 	Label yAxisLabel = new Label("Y");
 	Label xyLabel = new Label("X = Y");
 	Label xMaxLabel = new Label();
 	Label yMaxLabel = new Label();
 
 	//filtering
 	List<RowFilter<Object, Object>> tableFilter = new ArrayList<RowFilter<Object, Object>>(4);
 	double smallFilter = log2(0.1);
 	double equalFilter = 1;
 	ArrayList<ExpressionData> dimmingPoints = new ArrayList<ExpressionData>();
 	boolean isAdjusting = false;
 	JSlider equalSlider;
 	JSlider smallSlider;
 	JCheckBox scaleCheckBox;
 	JPanel rightPanel;
 
 	//is log scale
 	boolean isLogScale = true;
 
 	JLabel statusLabel;
 
 
 	//for drawing
 	Hashtable<String, Color> colormap = new Hashtable<String, Color>();
 
 	public ScatterPlotView(ScatterPlotModel model){
 		spModel = model;
 		rawTable = spModel.getDataTable();
 
 	}
 	void fileChanged(){
 		updateMinXY();
 		double margin = (maxXY - minXY)/20.0;
 		camera = new Camera(minXY - margin, maxXY + margin, minXY - margin, maxXY + margin, -10, 10);
 		scaleCheckBox.setSelected(true);
 		equalSlider.setMaximum((int)(spModel.getMaxA()*1000));
 		equalSlider.setMinimum(1000);
 		equalSlider.setValue(1000);
 		smallSlider.setMaximum((int)(Math.log(spModel.getMax()+.1)/Math.log(2)*1000));
 		smallSlider.setMinimum((int)(Math.log(.1)/Math.log(2)*1000));
 		smallSlider.setValue(smallSlider.getMinimum());
 		TableModel detailTableModel = new NonEditableTableModel(spModel.getDataTable(), spModel.getColumnNames());
		detailTable = new JTable(detailTableModel);
		totalSampleSize = detailTable.getRowCount();
 		detailTable.setRowSorter(getRowSorter(detailTable));
 		((TableRowSorter<TableModel>)detailTable.getRowSorter()).setRowFilter(RowFilter.andFilter(tableFilter));
 		for(Component c : rightPanel.getComponents()){
 			if(c instanceof PCanvas){
 				rightPanel.remove(c);
 				rightPanel.add(getHistogram());
				rightPanel.repaint();
 			}
 		}
 
 }
 
 	void initFilters(){
 		tableFilter.add(RowFilter.regexFilter(""));
 		tableFilter.add(RowFilter.regexFilter(""));
 		tableFilter.add(RowFilter.regexFilter(""));
 		tableFilter.add(RowFilter.regexFilter(""));
 	}
 	private void updateMinXY(){
 		if(isLogScale){
 			maxXY = log2(Math.max(spModel.getMaxX(), spModel.getMaxY()));
 			minXY = log2(0.1);
 			maxX = log2(spModel.getMaxX());
 			maxY = log2(spModel.getMaxY());
 		}
 		else{
 			maxXY = Math.max(spModel.getMaxX(), spModel.getMaxY());
 			minXY = 0;
 			maxX = spModel.getMaxX();
 			maxY = spModel.getMaxY();
 		}
 	}
 	public void start() {
 		updateMinXY();
 		double margin = (maxXY - minXY)/20.0;
 		camera = new Camera(minXY - margin, maxXY + margin, minXY - margin, maxXY + margin, -10, 10);
 		makeColorMap();
 		initLayout();
 		try {
 			canvas.setVisible(true);
 			mainFrame.setVisible(true);
 			controlFrame.setVisible(true);
 			Display.setParent(canvas);
 			Display.setResizable(true);
 			Display.setVSyncEnabled(true);
 			mainFrame.pack();
 			mainFrame.addComponentListener(new ComponentAdapter() {
 				@Override
 				public void componentResized(ComponentEvent e) {
 					// TODO Auto-generated method stub
 					controlFrame.setLocation(mainFrame.getX()+mainFrame.getWidth(), mainFrame.getY());
 				}
 
 				@Override
 				public void componentMoved(ComponentEvent e) {
 					controlFrame.setLocation(mainFrame.getX()+mainFrame.getWidth(), mainFrame.getY());
 				}
 			});
 
 			controlFrame.setLocation(mainFrame.getX()+mainFrame.getWidth(), mainFrame.getY());
 			controlFrame.pack();
 			Display.create();
 
 			GUI gui = initTWL();
 			initOpenGL();
 			initFilters();
 
 			LWJGLRenderer renderer = new LWJGLRenderer();
 
 			Dimension newDim;
 
 			while (!Display.isCloseRequested() && ! closeRequested){
 				newDim = newCanvasSize.getAndSet(null);
 
 				if(newDim != null){
 					GL11.glViewport(0, 0, newDim.width, newDim.height);
 					renderer.syncViewportSize();
 				}
 				if(isLogScale)
 					statusLabel.setText(detailTable.getRowCount()+" out of "+totalSampleSize + " in log scale");
 				else
 					statusLabel.setText(detailTable.getRowCount()+" out of "+totalSampleSize);
 
 				display();
 				mouseClickHandler(Mouse.getX(), Mouse.getY());
 				if(toolTipBox.getText()==null)
 					toolTipBox.setVisible(false);
 				else
 					toolTipBox.setVisible(true);
 				toolTipBox.setPosition(Mouse.getX()+20, canvas.getHeight() - Mouse.getY()+10);
 				gui.update();
 				Display.update();
 			}
 			Display.destroy();
 			mainFrame.dispose();
 			System.exit(0);
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private GUI initTWL() {
 		LWJGLRenderer renderer;
 		try {
 			renderer = new LWJGLRenderer();
 			GUI gui = new GUI(this, renderer);
             themeManager = ThemeManager.createThemeManager(getClass().getResource("resources/simple.xml"), renderer);
             gui.applyTheme(themeManager);
             toolTipBox.setAutoSize(true);
             toolTipBox.setTheme("faketooltip");
             toolTipBox.setBorderSize(1);
             xAxisLabel.setTheme("bigLabel");
             yAxisLabel.setTheme("bigLabel");
             xyLabel.setTheme("bigLabel");
             xMaxLabel.setTheme("label");
             yMaxLabel.setTheme("label");
 
     		add(toolTipBox);
     		add(xAxisLabel);
     		add(yAxisLabel);
     		add(xyLabel);
     		add(xMaxLabel);
     		add(yMaxLabel);
     		return gui;
 		} catch (LWJGLException e1) {
 			e1.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	private void initLayout() {
 		makeMenubar();
 
 
 
 		controlFrame.setPreferredSize(new Dimension(600, 600));
 
 		rightPanel = new JPanel();
 		controlFrame.add(rightPanel);
 
 		//frame.setLayout(new BorderLayout());
 
 
 
 		canvas.addComponentListener(new ComponentAdapter() {
 			@Override
 			public void componentResized(ComponentEvent e)
 			{ newCanvasSize.set(canvas.getSize()); }
 		});
 		mainFrame.addWindowListener(new WindowAdapter(){
 			@Override
 			public void windowClosing(WindowEvent e){
 				closeRequested = true;
 			}
 		});
 		controlFrame.addWindowListener(new WindowAdapter(){
 			@Override
 			public void windowClosing(WindowEvent e){
 				closeRequested = true;
 			}
 		});
 
 		mainFrame.add(canvas, BorderLayout.CENTER);
 		canvas.setPreferredSize(new java.awt.Dimension(600, 600));
 
 		JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
 
 		statusLabel = new JLabel();
 		//statusLabel.setPreferredSize(new Dimension(600, 16));
 
 		statusPanel.add(statusLabel);
 		mainFrame.add(statusPanel, BorderLayout.SOUTH);
 
 		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
 		TableModel detailTableModel = new NonEditableTableModel(spModel.getDataTable(), spModel.getColumnNames());
 
 
 		detailTable = new JTable(detailTableModel);
 
 		detailTable.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				JTable me = ((JTable)e.getSource());
 				clickedIndex = me.convertRowIndexToModel(me.getSelectedRow());
 			}
 		});
 		totalSampleSize = detailTable.getRowCount();
 		detailTable.setRowSorter(getRowSorter(detailTable));
 
 
 
 		JScrollPane tablePane = new JScrollPane(detailTable);
 		rightPanel.add(tablePane);
 
 		scaleCheckBox = new JCheckBox("Log Scale");
 		scaleCheckBox.setSelected(true);
 		scaleCheckBox.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				JCheckBox me = (JCheckBox)e.getSource();
 				isLogScale = me.isSelected();
 				updateMinXY();
 				double margin = (maxXY-minXY)/20.0;
 				camera.setCamera(minXY-margin, maxXY+margin, minXY-margin, maxXY+margin, -10, 10);
 			}
 		});
 		rightPanel.add(scaleCheckBox);
 
		PCanvas histogramView = getHistogram();
		rightPanel.add(histogramView);

 		JPanel smallFilterPanel = getSmallFilterPanel(scaleCheckBox);
 		rightPanel.add(smallFilterPanel);
 
 		JPanel equalPanel = getEqualFilterPanel();
 		rightPanel.add(equalPanel);
 	}
 	private TableRowSorter<TableModel> getRowSorter(JTable table) {
 		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
 		sorter.setComparator(0, new Comparator(){
 			@Override
 			public int compare(Object o1, Object o2) {
 				// TODO Auto-generated method stub
 				String i1 = (String) o1;
 				String i2 = (String) o2;
 				for(int i = 0; i < selectedItems.size(); i++){
 					if(selectedItems.get(i).compareTo(i1) == 0)
 						return -1;
 					if(selectedItems.get(i).compareTo(i2) == 0)
 						return 1;
 				}
 				return 0;
 			}
 		});
 		Vector<RowSorter.SortKey> sortkeys = new Vector<RowSorter.SortKey>();
 		sortkeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
 		sortkeys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));
 		sorter.setSortKeys(sortkeys);
 		return sorter;
 	}
 	private JPanel getEqualFilterPanel() {
 		JPanel equalPanel = new JPanel();
 		equalPanel.setLayout(new FlowLayout());
 
 		JLabel equalFilterLabel = new JLabel("Difference Filter");
 		equalFilterLabel.setPreferredSize(new Dimension(100, 20));
 		equalPanel.add(equalFilterLabel);
 		final IntegerTextField equalTextField = new IntegerTextField();
 		equalSlider = new JSlider(1000, (int)(spModel.getMaxA()*1000), 1000);
 
 		/***
 		 * Equal Text Field
 		 */
 		equalTextField.setPreferredSize(new Dimension(100, 20));
 		equalTextField.setHorizontalAlignment(JTextField.RIGHT);
 		equalTextField.addKeyListener(new KeyAdapter(){
 			public void keyPressed(KeyEvent e){
 				int key = e.getKeyCode();
 				if (key == KeyEvent.VK_ENTER) {
 					try{
 						double v = Double.parseDouble(equalTextField.getText());
 						equalSlider.setValue((int) (v*1000));
 					}
 					catch (NumberFormatException e1){
 						return ;
 					}
 				}
 			}
 		});
 		equalTextField.setEditable(true);
 		equalPanel.add(equalTextField);
 
 
 		equalSlider.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent e){
 				final JSlider me = (JSlider)e.getSource();
 				me.setToolTipText(""+equalFilter);
 				equalTextField.setText(""+me.getValue()/1000.0);
 				isAdjusting = false;
 			}
 			@Override
 			public void mousePressed(MouseEvent e){
 				isAdjusting = true;
 			}
 		});
 		equalSlider.addChangeListener(new ChangeListener(){
 			@Override
 			public void stateChanged(ChangeEvent arg0) {
 				JSlider me = (JSlider)arg0.getSource();
 				equalFilter = me.getValue()/1000.0;
 				equalTextField.setText(equalFilter+"");
 				tableFilter.set(1, new RowFilter<Object, Object>(){
 					public boolean include(Entry<? extends Object, ? extends Object> entry) {
 						double x = Double.parseDouble(""+entry.getValue(1));
 						double y = Double.parseDouble(""+entry.getValue(2));
 
 						if(Math.max(x, y)/Math.min(x, y) > equalFilter)
 							return true;
 						else
 							return false;
 					}
 				});
 				((TableRowSorter<TableModel>)detailTable.getRowSorter()).setRowFilter(RowFilter.andFilter(tableFilter));
 			}
 		});
 		equalSlider.setMajorTickSpacing(1000);
 		equalSlider.setPaintLabels(true);
 		equalSlider.setPaintTicks(true);
 		final Hashtable<Integer, JLabel> equalLabelTable = new Hashtable<Integer, JLabel>();
 		for(int i = 1; i < spModel.getMaxA(); i++){
 			equalLabelTable.put(new Integer(i*1000), new JLabel(i+""));
 		}
 		equalSlider.setLabelTable(equalLabelTable);
 		equalPanel.add(equalSlider);
 		return equalPanel;
 	}
 	private PCanvas getHistogram() {
 		PCanvas histogramView = new PCanvas();
 
 		histogramView.setPanEventHandler(null);
 
 
 		final PText tooltipNode = new PText();
 		final PCamera pcamera = histogramView.getCamera();
 
 		tooltipNode.setPickable(false);
 		pcamera.addChild(tooltipNode);
 
 		pcamera.addInputEventListener(new PBasicInputEventHandler(){
 			public void mouseMoved(PInputEvent event){
 				updateToolTip(event);
 			}
 			public void mouseDragged(PInputEvent event){
 				updateToolTip(event);
 			}
 			public void updateToolTip(PInputEvent event){
 				PNode n = event.getInputManager().getMouseOver().getPickedNode();
 				String name = (String)n.getAttribute("name");
 				Point2D p = event.getCanvasPosition();
 
 
 				event.getPath().canvasToLocal(p, pcamera);
 				tooltipNode.setText(name);
 				tooltipNode.setOffset(p.getX() + 8, p.getY() - 8);
 
 			}
 		});
 
 		histogramView.setPreferredSize(new Dimension(getWidth(), 200));
 
 		List<String> categoryNames = new ArrayList<String>(spModel.biggerCategoties.keySet());
 		Collections.sort(categoryNames);
 		int categoryWidth = 20;
 		int x = categoryWidth;
 		for(String name : categoryNames){
 			final Category category = spModel.biggerCategoties.get(name);
 			PNode categoryNode = PPath.createRectangle(x, 100-category.data.size()/50, categoryWidth, category.data.size()/50);
 			final PText categoryText = new PText(category.category);
 			final PText categoryValue = new PText(category.data.size()+"");
 
 
 			categoryNode.addAttribute("name", category.category);
 			categoryNode.setPaint(getColorByCategory(category.category));
 			categoryText.setOffset(x+categoryWidth/2-categoryText.getWidth()/2, 100);
 			categoryText.setPickable(false);
 			categoryValue.setScale(0.8);
 			categoryValue.setOffset(x+categoryWidth/2-categoryValue.getWidth()/2, 100 - category.data.size()/50-categoryValue.getHeight());
 			categoryValue.setTextPaint(Color.gray);
 			categoryValue.setPickable(false);
 
 			categoryNode.addInputEventListener(new PBasicInputEventHandler(){
 				public void mousePressed(PInputEvent event){
 					super.mousePressed(event);
 					category.toggleActivation();
 					if(category.isActivated){
 						event.getPickedNode().setPaint(getColorByCategory(category.category));
 					}
 					else{
 						event.getPickedNode().setPaint(Color.WHITE);
 					}
 					tableFilter.add(2, new RowFilter<Object, Object>(){
 						public boolean include(Entry<? extends Object, ? extends Object> entry) {
 							String categoryName = ((Category)entry.getValue(3)).category.substring(0,1);
 							if(spModel.biggerCategoties.get(categoryName).isActivated)
 								return true;
 							else
 								return false;
 						}
 					});
 					((TableRowSorter<TableModel>)detailTable.getRowSorter()).setRowFilter(RowFilter.andFilter(tableFilter));
 				}
 				public void mouseEntered(PInputEvent event){
 					super.mouseEntered(event);
 					((PText)event.getPickedNode().getChild(1)).setTextPaint(Color.black);
 					java.awt.Cursor a= new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR);
 					event.pushCursor(a);
 				}
 				public void mouseExited(PInputEvent event){
 					super.mouseExited(event);
 					((PText)event.getPickedNode().getChild(1)).setTextPaint(Color.gray);
 					event.popCursor();
 				}
 			});
 
 			histogramView.getLayer().addChild(categoryNode);
 			categoryNode.addChild(categoryText);
 			categoryNode.addChild(categoryValue);
 			x += categoryWidth*1.1;
 		}
 		return histogramView;
 	}
 	private JPanel getSmallFilterPanel(JCheckBox scaleCheckBox){
 		JPanel smallSliderPanel = new JPanel();
 		JLabel smallFilterLabel = new JLabel("RPKM Filter");
 		smallFilterLabel.setPreferredSize(new Dimension(100, 20));
 		final IntegerTextField smallTextField = new IntegerTextField();
 		smallSlider = new JSlider();
 		if(isLogScale){
 			smallSlider.setMaximum((int)(Math.log(spModel.getMax()+.1)/Math.log(2)*1000));
 			smallSlider.setMinimum((int)(Math.log(.1)/Math.log(2)*1000));
 			smallSlider.setValue(smallSlider.getMinimum());
 		}
 		else{
 			smallSlider.setMaximum((int)(spModel.getMax()*1000));
 			smallSlider.setMinimum(0);
 			smallSlider.setValue(smallSlider.getMinimum());
 		}
 
 		smallSliderPanel.setLayout(new FlowLayout());
 		smallSliderPanel.add(smallFilterLabel);
 		smallSliderPanel.add(smallTextField);
 
 
 		/***
 		 * Small Text Field
 		 */
 		smallTextField.setPreferredSize(new Dimension(100, 20));
 		smallTextField.setHorizontalAlignment(JTextField.RIGHT);
 		smallTextField.addKeyListener(new KeyAdapter(){
 			public void processKeyEvent(KeyEvent e){
 
 			}
 			public void keyPressed(KeyEvent e){
 				int key = e.getKeyCode();
 				if (key == KeyEvent.VK_ENTER) {
 					try{
 						double v = Double.parseDouble(smallTextField.getText());
 						smallSlider.setValue((int) (v*1000));
 					}
 					catch (NumberFormatException e1){
 						return ;
 					}
 				}
 			}
 		});
 		smallTextField.setEditable(true);
 		smallTextField.setFocusable(true);
 
 
 		smallSliderPanel.add(smallSlider);
 		smallSlider.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent e){
 				isAdjusting = false;
 			}
 			@Override
 			public void mousePressed(MouseEvent e){
 				isAdjusting = true;
 			}
 		});
 		smallSlider.addChangeListener(new ChangeListener(){
 			@Override
 			public void stateChanged(ChangeEvent arg0) {
 				JSlider me = (JSlider)arg0.getSource();
 				smallFilter = me.getValue()/1000.0-.1;
 				tableFilter.set(0, new RowFilter<Object, Object>(){
 					public boolean include(Entry<? extends Object, ? extends Object> entry) {
 						if(isLogScale){
 							if(Math.log(Double.parseDouble(""+entry.getValue(1))+.1)/Math.log(2) > smallFilter &&
 									Math.log(Double.parseDouble(""+entry.getValue(2))+.1)/Math.log(2) > smallFilter)
 								return true;
 							else
 								return false;
 						}
 						else{
 							if(Double.parseDouble(""+entry.getValue(1)) > smallFilter &&
 									Double.parseDouble(""+entry.getValue(2)) > smallFilter)
 								return true;
 							else
 								return false;
 						}
 					}
 				});
 				((TableRowSorter<TableModel>)detailTable.getRowSorter()).setRowFilter(RowFilter.andFilter(tableFilter));
 				smallTextField.setText(""+me.getValue()/1000.0);
 			}
 		});
 
 		smallSlider.setPaintLabels(true);
 		smallSlider.setPaintTicks(true);
 
 		final Hashtable<Integer, JLabel> labelTableLogScale = new Hashtable<Integer, JLabel>();
 		for(int i = (int) log2(spModel.getMin()); i < log2(spModel.getMax()); i ++){
 			labelTableLogScale.put(new Integer(i*1000), new JLabel(i+""));
 		}
 		final Hashtable<Integer, JLabel> labelTableOriginalScale = new Hashtable<Integer, JLabel>();
 		for(int i = 0; i < spModel.getMax(); i+=Math.pow(10, (int)Math.log10(spModel.getMax()))){
 			labelTableOriginalScale.put(new Integer(i*1000), new JLabel(i+""));
 		}
 
 		smallSlider.setLabelTable(labelTableLogScale);
 		scaleCheckBox.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				JCheckBox me = (JCheckBox)e.getSource();
 				isLogScale = me.isSelected();
 				if(isLogScale){
 					smallSlider.setLabelTable(labelTableLogScale);
 					smallSlider.setMaximum((int)(Math.log(spModel.getMax()+.1)/Math.log(2)*1000));
 					smallSlider.setMinimum((int)(Math.log(.1)/Math.log(2)*1000));
 					smallSlider.setValue(smallSlider.getMinimum());
 				}
 				else{
 					smallSlider.setLabelTable(labelTableOriginalScale);
 					smallSlider.setMaximum((int)(spModel.getMax()*1000));
 					smallSlider.setMinimum(0);
 					smallSlider.setValue(smallSlider.getMinimum());
 				}
 			}
 		});
 		return smallSliderPanel;
 	}
 	private void makeMenubar() {
 		menuBar = new JMenuBar();
 
 		JMenu fileMenu = new JMenu("File");
 		JMenuItem open = new JMenuItem("Open", KeyEvent.VK_O);
 		open.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				fileDialog.show();
 				File file = fileDialog.getFile();
 				if(file != null){
 					spModel.readTXTData(file.getPath(), "Feature ID", "RPKM1", "RPKM2", "COG Category");
 					fileChanged();
 				}
 			}
 
 		});
 		fileMenu.add(open);
 
 		JMenuItem close = new JMenuItem("Exit", KeyEvent.VK_X);
 		close.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				System.exit(0);
 			}
 		});
 		fileMenu.add(close);
 
 		menuBar.add(fileMenu);
 
 		JMenu cameraMenu = new JMenu("Camera");
 		JMenuItem resetCamera = new JMenuItem("Reset camera", KeyEvent.VK_R);
 		resetCamera.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
 		resetCamera.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				camera.resetCamera();
 			}
 		});
 		cameraMenu.add(resetCamera);
 
 		menuBar.add(cameraMenu);
 
 		JMenu optionMenu = new JMenu("Option");
 		JMenuItem option = new JMenuItem("Option");
 		option.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				new Option();
 			}
 		});
 		optionMenu.add(option);
 		menuBar.add(optionMenu);
 
 		mainFrame.setJMenuBar(menuBar);
 	}
 	private void initOpenGL() {
 
 		selectBuff = BufferUtils.createIntBuffer(1024);
 		GL11.glSelectBuffer(selectBuff);
 
 		GL11.glClearColor(1, 1, 1, 1);
 
 		GL11.glEnable(GL11.GL_NORMALIZE);
 
 
 		GL11.glEnable(GL11.GL_POINT_SMOOTH);
 		GL11.glEnable(GL11.GL_BLEND);
 		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 
 
 	}
 	private void makeColorMap() {
 		int alpha = 255;
 		colormap.put("J", new Color(255, 0, 0, alpha));
 		colormap.put("A", new Color(194, 175, 88, alpha));
 		colormap.put("K", new Color(255, 153, 0, alpha));
 		colormap.put("L", new Color(255, 255, 0, alpha));
 		colormap.put("B", new Color(255, 198, 0, alpha));
 		colormap.put("D", new Color(0x99, 0xff, 0, alpha));
 		colormap.put("Y", new Color(0x49, 0x31, 0x26, alpha));
 		colormap.put("V", new Color(0xff, 0x0, 0x8a, alpha));
 		colormap.put("T", new Color(0, 0, 0xff, alpha));
 		colormap.put("M", new Color(0x9e, 0xc9, 0x28, alpha));
 		colormap.put("N", new Color(0x0, 0x66, 0x33, alpha));
 		colormap.put("Z", new Color(0x66, 0, 0x99, alpha));
 		colormap.put("W", new Color(0x33, 0x66, 0x99, alpha));
 		colormap.put("U", new Color(0x33, 0xcc, 0x99, alpha));
 		colormap.put("O", new Color(0x0, 0xff, 0xff, alpha));
 		colormap.put("C", new Color(0x99, 0x00, 0xff, alpha));
 		colormap.put("G", new Color(0x80, 0x56, 0x42, alpha));
 		colormap.put("E", new Color(0xff, 0x0, 0xff, alpha));
 		colormap.put("F", new Color(0x99, 0x33, 0x4d, alpha));
 		colormap.put("H", new Color(0x72, 0x7d, 0xcc, alpha));
 		colormap.put("I", new Color(0x5c, 0x5a, 0x1b, alpha));
 		colormap.put("P", new Color(0x0, 0x99, 0xff, alpha));
 		colormap.put("Q", new Color(0xff, 0xcc, 0x99, alpha));
 		colormap.put("R", new Color(0xff, 0x99, 0x99, alpha));
 		colormap.put("S", new Color(0xd6, 0xaa, 0xdf, alpha));
 	}
 
 	void mouseClickHandler(int x, int y){
 		GL11.glRenderMode(GL11.GL_SELECT);
 		renderMode = GL11.GL_SELECT;
 		display();
 		renderMode = GL11.GL_RENDER;
 		int hits = GL11.glRenderMode(GL11.GL_RENDER);
 		if(hits > 0){
 			int depth, choose;
 			choose = selectBuff.get(3);
 			depth = selectBuff.get(1);
 			for(int loop = 1; loop < hits ; loop++){
 				if(selectBuff.get(loop*4+1) < depth){
 					choose = selectBuff.get(loop*4+3);
 					depth = selectBuff.get(loop*4+1);
 				}
 			}
 			ExpressionData data = spModel.getDataTable().get(choose);
 			if(Mouse.isButtonDown(0)){
 				isDowned = true;
 			}
 			else if(!Mouse.isButtonDown(0) && isDowned){
 				selectedItems.clear();
 				for(int loop = 0; loop < hits ; loop++){
 					int rowIndex = detailTable.convertRowIndexToView(selectBuff.get(loop*4+3));
 					selectedItems.add((String) detailTable.getValueAt(rowIndex, 0));
 				}
 				clickedIndex = overedIndex;
 				detailTable.getRowSorter().toggleSortOrder(0);
 				detailTable.getRowSorter().toggleSortOrder(0);
 
 				int index = detailTable.convertRowIndexToView(choose);
 				detailTable.getSelectionModel().setSelectionInterval(0, hits-1);
 				detailTable.scrollRectToVisible(detailTable.getCellRect(index, 0, true));
 				detailTable.repaint();
 
 
 				isDowned = false;
 			}
 			else{
 				overedIndex = choose;
 			}
 			toolTipBox.setText(String.format("%s (%.2f, %.2f)",data.getName(), data.getX(), data.getY()));
 		}
 		else{
 			overedIndex = -1;
 			toolTipBox.setText(null);
 			if(Mouse.isButtonDown(0)&&clickedIndex != -1){
 				clickedIndex = -1;
 			}
 		}
 	}
 	private void display(){
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 
 
 		if(renderMode == GL11.GL_SELECT){
 			IntBuffer viewport = BufferUtils.createIntBuffer(16);
 			GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
 			GLU.gluPickMatrix(Mouse.getX(), Mouse.getY(), 3, 3, viewport);
 		}
 		camera.move();
 
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
 
 		drawDots();
 		if(isAdjusting){
 			drawDimmingDots();
 		}
 		dimmingPoints.clear();
 		camera.getInput();
 
 		if(renderMode != GL11.GL_SELECT){
 			if(isLogScale){
 				drawAxis(log2(Math.max(spModel.getMaxX(), spModel.getMaxX())), isLogScale);
 			}
 			else{
 				drawAxis(Math.max(spModel.getMaxX(), spModel.getMaxX()), isLogScale);
 			}
 			//drawFilterArea();
 			drawMinMax();
 			drawXYLine(isLogScale);
 		}
 	}
 	double[] getAdjustedLocation(ExpressionData data, boolean isLogScale){
 		double x, y;
 		if(isLogScale){
 			x = log2(data.getX()+0.1);
 			y = log2(data.getY()+0.1);
 		}
 		else{
 			x = data.getX();
 			y = data.getY();
 		}
 		 return new double[]{x, y};
 	}
 	private void drawDimmingDots(){
 		for(int i = 0; i < dimmingPoints.size(); i++){
 			ExpressionData data = dimmingPoints.get(i);
 			double[] loc = getAdjustedLocation(data, isLogScale);
 
 			GL11.glPointSize(7);
 			Color categoryColor = getColorByCategory(data.getCategory().toString().substring(0, 1));
 			GL11.glColor4d(categoryColor.getRed()/255.0, categoryColor.getGreen()/255.0, categoryColor.getBlue()/255.0, .1);
 			GL11.glBegin(GL11.GL_POINTS);
 			GL11.glVertex2d(loc[0], loc[1]);
 			GL11.glEnd();
 		}
 
 
 	}
 	private void drawDots() {
 		GL11.glInitNames();
 		Vector<Integer> drawOnTop = new Vector<Integer>();
 
		for(int i = 0; i < detailTable.getRowCount(); i++){
 			ExpressionData data = rawTable.get(i);
 
 			double[] xy = getAdjustedLocation(data, isLogScale);
 
 			if(detailTable.convertRowIndexToView(i) < 0){
 				dimmingPoints.add(data);
 				continue;
 			}
 
 			if(overedIndex == i||clickedIndex == i){
 				drawOnTop.add(i);
 				continue;
 			}
 			else{
 				GL11.glPushName(i);
 				GL11.glPointSize(7);
 				Color categoryColor = getColorByCategory(detailTable.getModel().getValueAt(i, 3).toString().substring(0, 1));
 				GL11.glColor4d(categoryColor.getRed()/255.0, categoryColor.getGreen()/255.0, categoryColor.getBlue()/255.0, data.alpha);
 				GL11.glBegin(GL11.GL_POINTS);
 				GL11.glVertex2d(xy[0], xy[1]);
 				GL11.glEnd();
 				GL11.glPopName();
 			}
 		}
 		//because we enable alpha blending and z buffering
 		for(Integer i : drawOnTop){
 			ExpressionData data = rawTable.get(i);
 			double[] xy = getAdjustedLocation(data, isLogScale);
 			GL11.glPushName(i);
 			GL11.glPointSize(14);
 			GL11.glColor3f(0, 0, 0);
 			GL11.glBegin(GL11.GL_POINTS);
 			GL11.glVertex3d(xy[0], xy[1], 0);
 			GL11.glEnd();
 			GL11.glPopName();
 		}
 
 	}
 	private Color getColorByCategory(String category){
 		if(category.length() > 1){
 			category = category.substring(0, 1);
 		}
 
 		return colormap.get(category);
 
 	}
 
 	private void drawXYLine(boolean isLogScale){
 		if(isLogScale){
 			GL11.glLineWidth(2);
 			GL11.glBegin(GL11.GL_LINES);
 			GL11.glVertex2d(-maxXY, -maxXY);
 			GL11.glVertex2d(maxXY, maxXY);
 			GL11.glEnd();
 		}
 		else{
 			GL11.glLineWidth(2);
 			GL11.glBegin(GL11.GL_LINES);
 			GL11.glVertex2d(0, 0);
 			GL11.glVertex2d(maxXY, maxXY);
 			GL11.glEnd();
 		}
 		int [] pos = getBoundary(maxXY, maxXY, 0);
 		xyLabel.setPosition(pos[0]-50, pos[1]+50);
 
 	}
 	/***
 	 * returns intersection point with boundaries
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @return
 	 */
 	int []getBoundary(double x, double y, double z){
 		int [] coord = Translater.getScreenCoordinate((float)x, (float)y, (float)z);
 		if(coord[0] > Display.getWidth()){
 			coord[0] = Display.getWidth();
 		}
 		else if(coord[0] < 0){
 			coord[0] = 0;
 		}
 		if(coord[1] > Display.getHeight()){
 			coord[1] = Display.getHeight();
 		}
 		else if(coord[1] < 0){
 			coord[1] = 0;
 		}
 		coord[1] = Display.getHeight() - coord[1];
 
 		return coord;
 	}
 	private void drawAxis(double maxXY, boolean isLogScale) {
 		if(isLogScale){
 			GL11.glLineWidth(2);
 			GL11.glColor3d(0, 0, 0);
 			GL11.glBegin(GL11.GL_LINES);
 			GL11.glVertex2d(-maxXY*1.1, 0);
 			GL11.glVertex2d(maxXY*1.1, 0);
 			GL11.glVertex2d(0, -maxXY*1.1);
 			GL11.glVertex2d(0, maxXY*1.1);
 			GL11.glEnd();
 			GL11.glLineWidth(1);
 			GL11.glColor4d(0, 0, 0, 0.1);
 			GL11.glBegin(GL11.GL_LINES);
 			if(Option.showTick){
 				for(double i = 0; i < spModel.getMax()*1.1; i = i + Option.tickInterval){
 					GL11.glVertex2d(log2(i), -maxXY);
 					GL11.glVertex2d(log2(i), maxXY);
 					GL11.glVertex2d(-maxXY, log2(i));
 					GL11.glVertex2d(maxXY, log2(i));
 					GL11.glVertex2d(-log2(i), -maxXY);
 					GL11.glVertex2d(-log2(i), maxXY);
 					GL11.glVertex2d(-maxXY, -log2(i));
 					GL11.glVertex2d(maxXY, -log2(i));
 				}
 			}
 			GL11.glEnd();
 		}
 		else{
 			GL11.glLineWidth(2);
 			GL11.glColor3d(0, 0, 0);
 			GL11.glBegin(GL11.GL_LINES);
 			GL11.glVertex2d(0, 0);
 			GL11.glVertex2d(maxXY*1.1, 0);
 			GL11.glVertex2d(0, 0);
 			GL11.glVertex2d(0, maxXY*1.1);
 			GL11.glEnd();
 			GL11.glLineWidth(1);
 			GL11.glColor4d(0, 0, 0, 0.1);
 			GL11.glBegin(GL11.GL_LINES);
 			if(Option.showTick){
 				for(double i = 0; i < maxXY*1.1; i = i + Option.tickInterval){
 					GL11.glVertex2d(i, 0);
 					GL11.glVertex2d(i, maxXY*1.1);
 					GL11.glVertex2d(0, i);
 					GL11.glVertex2d(maxXY*1.1, i);
 				}
 			}
 			GL11.glEnd();
 		}
 
 		int xpos[] = getBoundary(maxXY*1.15, 0, 0);
 		int ypos[] = getBoundary(0, maxXY*1.15, 0);
 
 		xAxisLabel.setPosition(xpos[0]-10, xpos[1]+30);
 		yAxisLabel.setPosition(ypos[0]-10, ypos[1]+10);
 
 	}
 	void drawFilterArea(){
 		//draw small
 		GL11.glColor4d(1, 0, 0, 0.2);
 		GL11.glBegin(GL11.GL_QUADS);
 		if(isLogScale){
 			GL11.glVertex2d(log2(0.1), log2(0.1));
 			GL11.glVertex2d(smallFilter, smallFilter);
 			GL11.glVertex2d(log2(spModel.getMax()), smallFilter);
 			GL11.glVertex2d(log2(spModel.getMax()), log2(.1));
 
 			GL11.glVertex2d(log2(0.1), log2(0.1));
 			GL11.glVertex2d(smallFilter, smallFilter);
 			GL11.glVertex2d(smallFilter, log2(spModel.getMax()));
 			GL11.glVertex2d(log2(.1), log2(spModel.getMax()));
 
 		}
 		else{
 			GL11.glVertex2d(0, 0);
 			GL11.glVertex2d(smallFilter, smallFilter);
 			GL11.glVertex2d(spModel.getMax(), smallFilter);
 			GL11.glVertex2d(spModel.getMax(), 0);
 
 			GL11.glVertex2d(0, 0);
 			GL11.glVertex2d(smallFilter, smallFilter);
 			GL11.glVertex2d(smallFilter, spModel.getMax());
 			GL11.glVertex2d(0, spModel.getMax());
 		}
 		GL11.glEnd();
 
 
 		//draw equal
 		GL11.glColor3d(0.8, 0.8, 1);
 		GL11.glBegin(GL11.GL_TRIANGLES);
 		if(isLogScale){
 			double prev = 2;
 			for(double i = 2; i < spModel.getMax(); i+= 2){
 				GL11.glVertex2d(log2(0.1), log2(0.1));
 				GL11.glVertex2d(log2(prev), log2(prev*equalFilter));
 				GL11.glVertex2d(log2(i), log2(i*equalFilter));
 
 				GL11.glVertex2d(log2(0.1), log2(0.1));
 				GL11.glVertex2d(log2(prev*equalFilter), log2(prev));
 				GL11.glVertex2d(log2(i*equalFilter), log2(i));
 
 				prev = i;
 
 			}
 			GL11.glVertex2d(log2(0.1), log2(0.1));
 			GL11.glVertex2d(log2(spModel.getMax()), log2(spModel.getMax()));
 			GL11.glVertex2d(log2(spModel.getMax()), log2(spModel.getMax()*equalFilter));
 			GL11.glVertex2d(log2(0.1), log2(0.1));
 			GL11.glVertex2d(log2(spModel.getMax()), log2(spModel.getMax()));
 			GL11.glVertex2d(log2(spModel.getMax()*equalFilter), log2(spModel.getMax()));
 
 		}
 		else{
 			GL11.glVertex2d(0, 0);
 			GL11.glVertex2d(spModel.getMax(), spModel.getMax());
 			GL11.glVertex2d(spModel.getMax(), spModel.getMax()*equalFilter);
 
 			GL11.glVertex2d(0, 0);
 			GL11.glVertex2d(spModel.getMax(), spModel.getMax());
 			GL11.glVertex2d(spModel.getMax(), spModel.getMax()/equalFilter);
 		}
 		GL11.glEnd();
 
 	}
 	private double log2(double a){
 		return Math.log(a)/Math.log(2);
 	}
 	private void drawMinMax(){
 		GL11.glColor3d(0, 0, 0);
 		int xpos[] = Translater.getScreenCoordinate((float) maxX, 0, 0);
 		xMaxLabel.setText(String.format("%.2f", spModel.getMaxX()));
 		xMaxLabel.setPosition(xpos[0], Display.getHeight()-xpos[1]+xMaxLabel.getPreferredHeight()-10);
 		GL11.glBegin(GL11.GL_LINES);
 		GL11.glVertex2d(maxX, -.1);
 		GL11.glVertex2d(maxX, .1);
 		GL11.glEnd();
 
 		int ypos[] = Translater.getScreenCoordinate(0, (float) maxY, 0);
 		yMaxLabel.setText(String.format("%.2f", spModel.getMaxY()));
 		yMaxLabel.setPosition(ypos[0] - yMaxLabel.getPreferredWidth(), Display.getHeight()-ypos[1]);
 		GL11.glBegin(GL11.GL_LINES);
 		GL11.glVertex2d(-.1, maxY);
 		GL11.glVertex2d(.1, maxY);
 		GL11.glEnd();
 
 
 	}
 }
 
