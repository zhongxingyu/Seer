 package info.pishen.gameoflife;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
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
 import java.util.logging.Logger;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.JSlider;
 import java.awt.FlowLayout;
 
 public class MainGUI extends JFrame {
 	private static Logger log = Logger.getLogger(MainGUI.class.getName());
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JPanel mainPanel, contentPanel;
 	private JButton runPauseButton;
 	private JScrollBar hScrollBar, vScrollBar;
 	private int contentWidth, contentHeight;
 	private int cellSize = 10;
 	private boolean isRunning = false;
 	
 	private CellGrid cellGrid;
 	private ParallelGenerator generator;
 	private JSlider threadNumSlider;
 	private JButton button;
 	private JButton button_1;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					log.info("starting...");
					CellGrid cellGrid = new CellGrid(2000, 2000);
 					MainGUI frame = new MainGUI(cellGrid, new ParallelGenerator(cellGrid));
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public MainGUI(CellGrid cellGrid, ParallelGenerator generatorArg) {
 		this.cellGrid = cellGrid;
 		this.generator = generatorArg;
 		cellGrid.setMainGUI(this);
 		contentWidth = cellGrid.getColNum() * cellSize;
 		contentHeight = cellGrid.getRowNum() * cellSize;
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 650, 500);
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
 					runPauseButton.setEnabled(false);
 					//TODO pause
 					generator.pause(MainGUI.this);
 				}else{
 					isRunning = true;
 					runPauseButton.setText("Pause");
 					//TODO start
 					generator.run();
 				}
 			}
 		});
 		buttomPanel.add(runPauseButton);
 		
 		threadNumSlider = new JSlider();
 		threadNumSlider.setMinimum(1);
		threadNumSlider.setMaximum(6);
 		threadNumSlider.setValue(1);
 		threadNumSlider.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				log.info("value: " + threadNumSlider.getValue());
 				//TODO change number of threads
				
 			}
 		});
 		
 		button = new JButton("+");
 		buttomPanel.add(button);
 		
 		button_1 = new JButton("-");
 		buttomPanel.add(button_1);
 		buttomPanel.add(threadNumSlider);
 		
 		JPanel customScrollPanel = new JPanel();
 		mainPanel.add(customScrollPanel, BorderLayout.CENTER);
 		GridBagLayout gbl_customScrollPanel = new GridBagLayout();
 		customScrollPanel.setLayout(gbl_customScrollPanel);
 		
 		contentPanel = new ContentPanel();
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
 		hScrollBar.setMaximum(contentWidth);
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
 		vScrollBar.setMaximum(contentHeight);
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
 	
 	public void repaintGrid(){
 		EventQueue.invokeLater(new Runnable(){
 			@Override
 			public void run() {
 				contentPanel.repaint();
 			}
 		});
 	}
 	
 	public void enableRun(){
 		runPauseButton.setEnabled(true);
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
