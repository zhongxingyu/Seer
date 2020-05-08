 package info.pishen.gameoflife;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.logging.Logger;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.JSlider;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 public class MainGUI extends JFrame {
 	private static Logger log = Logger.getLogger(MainGUI.class.getName());
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static MainGUI current;
 	
 	private JPanel mainPanel, contentPanel;
 	private JButton runPauseButton;
 	private JScrollBar hScrollBar, vScrollBar;
 	//private int contentWidth, contentHeight;
 	private int cellSize = 10;
 	private final int MIN_CELL_SIZE = 2, MAX_CELL_SIZE = 20;
 	private boolean isRunning = false;
 	private boolean updateValue;
 	
 	private CellGrid cellGrid;
 	private UpdateThread updateThread;
 	private JSlider threadNumSlider;
 	private JButton zoomIn;
 	private JButton zoomOut;
 	private JLabel threadNumLabel;
 	private JLabel updateTimeLabel;
 	private JComboBox patternSelector;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					log.info("starting...");
 					log.info("processors: " + Runtime.getRuntime().availableProcessors());
 					MainGUI frame = new MainGUI();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 * @throws URISyntaxException 
 	 * @throws IOException 
 	 * @throws NumberFormatException 
 	 */
 	public MainGUI() throws URISyntaxException, NumberFormatException, IOException {
 		current = this;
 		cellGrid = new CellGrid("clear");
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 850, 550);
 		mainPanel = new JPanel();
 		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
 		mainPanel.setLayout(new BorderLayout(0, 0));
 		setContentPane(mainPanel);
 		
 		JPanel buttomPanel = new JPanel();
 		FlowLayout flowLayout = (FlowLayout) buttomPanel.getLayout();
 		flowLayout.setAlignment(FlowLayout.LEFT);
 		mainPanel.add(buttomPanel, BorderLayout.SOUTH);
 		
 		runPauseButton = new JButton("Run");
 		runPauseButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(isRunning){
 					isRunning = false;
 					runPauseButton.setText("Run");
					//runPauseButton.setEnabled(false);
 					//pause
 					updateThread.lagStop();
 				}else{
 					isRunning = true;
 					runPauseButton.setText("Pause");
					//patternSelector.setEnabled(false);
 					//run
 					updateThread = new UpdateThread(cellGrid);
 					updateThread.start();
 				}
 			}
 		});
 		buttomPanel.add(runPauseButton);
 		
 		zoomIn = new JButton("+");
 		zoomIn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				cellSize++;
 				zoomOut.setEnabled(true);
 				if(cellSize == MAX_CELL_SIZE){
 					zoomIn.setEnabled(false);
 				}
 				updateScale();
 			}
 		});
 		buttomPanel.add(zoomIn);
 		
 		zoomOut = new JButton("-");
 		zoomOut.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				cellSize--;
 				zoomIn.setEnabled(true);
 				if(cellSize == MIN_CELL_SIZE){
 					zoomOut.setEnabled(false);
 				}
 				updateScale();
 			}
 		});
 		buttomPanel.add(zoomOut);
 		
 		File patternDir = new File("pattern");
 		String[] customPatterns = patternDir.list();
 		String[] allPatterns = new String[patternDir.list().length + 3];
 		allPatterns[0] = "clear";
 		allPatterns[1] = "random";
 		allPatterns[2] = "pseudo-random";
 		for(int i = 0; i < customPatterns.length; i++){
 			allPatterns[i + 3] = customPatterns[i];
 		}
 		patternSelector = new JComboBox(allPatterns);
 		patternSelector.setSelectedIndex(0);
 		patternSelector.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(isRunning){
 					isRunning = false;
 					runPauseButton.setText("Run");
 					updateThread.lagStop();
 				}
 				
 				JComboBox cb = (JComboBox)e.getSource();
 				try {
 					cellGrid = new CellGrid((String)cb.getSelectedItem());
 					hScrollBar.setValue(0);
 					vScrollBar.setValue(0);
 					hScrollBar.setMaximum(cellGrid.getColNum() * cellSize);
 					vScrollBar.setMaximum(cellGrid.getRowNum() * cellSize);
 					contentPanel.repaint();
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 		buttomPanel.add(patternSelector);
 		
 		threadNumSlider = new JSlider();
 		threadNumSlider.setMinimum(1);
 		threadNumSlider.setMaximum(Runtime.getRuntime().availableProcessors());
 		threadNumSlider.setValue(1);
 		threadNumSlider.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				threadNumLabel.setText("threads: " + threadNumSlider.getValue());
 				//change number of threads
				updateThread.setParallelLevel(threadNumSlider.getValue());
 			}
 		});
 		buttomPanel.add(threadNumSlider);
 		
 		threadNumLabel = new JLabel("threads: 1");
 		buttomPanel.add(threadNumLabel);
 		
 		updateTimeLabel = new JLabel("time: 0.0");
 		buttomPanel.add(updateTimeLabel);
 		
 		JPanel customScrollPanel = new JPanel();
 		mainPanel.add(customScrollPanel, BorderLayout.CENTER);
 		GridBagLayout gbl_customScrollPanel = new GridBagLayout();
 		customScrollPanel.setLayout(gbl_customScrollPanel);
 		
 		contentPanel = new ContentPanel();
 		contentPanel.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseReleased(MouseEvent e) {}
 			
 			@Override
 			public void mousePressed(MouseEvent e) {
 				int x = e.getX() + hScrollBar.getValue();
 				int y = e.getY() + vScrollBar.getValue();
 				if(isRunning || runPauseButton.isEnabled() == false){
 					return;
 				}
 				if(x >= 0 && x < hScrollBar.getMaximum() && y >= 0 && y < vScrollBar.getMaximum()){
 					int i = y / cellSize, j = x / cellSize;
 					updateValue = !cellGrid.getValue(i, j);
 					cellGrid.updateGrid(i, j, updateValue);
 					contentPanel.repaint();
 				}
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent e) {}
 			
 			@Override
 			public void mouseEntered(MouseEvent e) {}
 			
 			@Override
 			public void mouseClicked(MouseEvent e) {}
 		});
 		contentPanel.addMouseMotionListener(new MouseMotionListener() {
 			@Override
 			public void mouseMoved(MouseEvent e) {}
 			
 			@Override
 			public void mouseDragged(MouseEvent e) {
 				int x = e.getX() + hScrollBar.getValue();
 				int y = e.getY() + vScrollBar.getValue();
 				if(isRunning || runPauseButton.isEnabled() == false){
 					return;
 				}
 				if(x >= 0 && x < hScrollBar.getMaximum() && y >= 0 && y < vScrollBar.getMaximum()){
 					int i = y / cellSize, j = x / cellSize;
 					cellGrid.updateGrid(i, j, updateValue);
 					contentPanel.repaint();
 				}
 			}
 		});
 		GridBagConstraints gbc_contentPanel = new GridBagConstraints();
 		gbc_contentPanel.weighty = 1.0;
 		gbc_contentPanel.weightx = 1.0;
 		gbc_contentPanel.insets = new Insets(0, 0, 0, 0);
 		gbc_contentPanel.fill = GridBagConstraints.BOTH;
 		gbc_contentPanel.gridx = 0;
 		gbc_contentPanel.gridy = 0;
 		customScrollPanel.add(contentPanel, gbc_contentPanel);
 		
 		hScrollBar = new JScrollBar();
 		hScrollBar.setOrientation(JScrollBar.HORIZONTAL);
 		hScrollBar.setMaximum(cellGrid.getColNum() * cellSize);
 		hScrollBar.addAdjustmentListener(new AdjustmentListener() {
 			@Override
 			public void adjustmentValueChanged(AdjustmentEvent e) {
 				contentPanel.repaint();
 			}
 		});
 		hScrollBar.addComponentListener(new ComponentListener() {
 			@Override
 			public void componentShown(ComponentEvent e) {}
 			
 			@Override
 			public void componentResized(ComponentEvent e) {
 				hScrollBar.setValue(Math.min(hScrollBar.getValue(), hScrollBar.getMaximum() - hScrollBar.getWidth()));
 				hScrollBar.setVisibleAmount(hScrollBar.getWidth());
 			}
 			
 			@Override
 			public void componentMoved(ComponentEvent e) {}
 			
 			@Override
 			public void componentHidden(ComponentEvent e) {}
 		});
 		GridBagConstraints gbc_hScrollBar = new GridBagConstraints();
 		gbc_hScrollBar.insets = new Insets(0, 0, 0, 0);
 		gbc_hScrollBar.fill = GridBagConstraints.HORIZONTAL;
 		gbc_hScrollBar.gridx = 0;
 		gbc_hScrollBar.gridy = 1;
 		customScrollPanel.add(hScrollBar, gbc_hScrollBar);
 		
 		vScrollBar = new JScrollBar();
 		vScrollBar.setMaximum(cellGrid.getRowNum() * cellSize);
 		vScrollBar.addAdjustmentListener(new AdjustmentListener() {
 			@Override
 			public void adjustmentValueChanged(AdjustmentEvent e) {
 				contentPanel.repaint();
 			}
 		});
 		vScrollBar.addComponentListener(new ComponentListener() {
 			@Override
 			public void componentShown(ComponentEvent e) {}
 			
 			@Override
 			public void componentResized(ComponentEvent e) {
 				vScrollBar.setValue(Math.min(vScrollBar.getValue(), vScrollBar.getMaximum() - vScrollBar.getHeight()));
 				vScrollBar.setVisibleAmount(vScrollBar.getHeight());
 			}
 			
 			@Override
 			public void componentMoved(ComponentEvent e) {}
 			
 			@Override
 			public void componentHidden(ComponentEvent e) {}
 		});
 		GridBagConstraints gbc_vScrollBar = new GridBagConstraints();
 		gbc_vScrollBar.fill = GridBagConstraints.VERTICAL;
 		gbc_vScrollBar.gridx = 1;
 		gbc_vScrollBar.gridy = 0;
 		customScrollPanel.add(vScrollBar, gbc_vScrollBar);
 	}
 	
 	private void updateScale(){
 		hScrollBar.setMaximum(cellGrid.getColNum() * cellSize);
 		vScrollBar.setMaximum(cellGrid.getRowNum() * cellSize);
 		contentPanel.repaint();
 	}
 	
 	public static MainGUI getCurrentGUI(){
 		return MainGUI.current;
 	}
 	
 	public void repaintGrid(){
 		EventQueue.invokeLater(new Runnable(){
 			@Override
 			public void run() {
 				contentPanel.repaint();
 			}
 		});
 	}
 	
 	/*public void enableRun(){
 		EventQueue.invokeLater(new Runnable(){
 			@Override
 			public void run() {
 				runPauseButton.setEnabled(true);
 				patternSelector.setEnabled(true);
 			}
 		});
 	}*/
 	
 	public void showUpdateTime(final double time){
 		EventQueue.invokeLater(new Runnable(){
 			@Override
 			public void run() {
 				updateTimeLabel.setText("time: " + time);
 			}
 		});
 	}
 	
 	private class ContentPanel extends JPanel{
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		protected void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			int xLeft = 0 - (hScrollBar.getValue() % cellSize);
 			int yTop = 0 - (vScrollBar.getValue() % cellSize);
 			int jLeft = hScrollBar.getValue() / cellSize;
 			int iTop = vScrollBar.getValue() / cellSize;
 			int jRight = Math.min(jLeft + hScrollBar.getVisibleAmount() / cellSize + 1, cellGrid.getColNum() - 1);
 			int iBottom = Math.min(iTop + vScrollBar.getVisibleAmount() / cellSize + 1, cellGrid.getRowNum() - 1);
 
 			boolean[][] partialGrid = cellGrid.getPartialGrid(iTop, jLeft, iBottom, jRight);
 			for(int x = xLeft, j = 0; j < partialGrid[0].length; x += cellSize, j++){
 				for(int y = yTop, i = 0; i < partialGrid.length; y += cellSize, i++){
 					g.drawRect(x, y, cellSize, cellSize);
 					if(partialGrid[i][j] == true){
 						g.fillRect(x, y, cellSize, cellSize);
 					}
 				}
 			}
 		}
 		
 		
 	}
 
 }
