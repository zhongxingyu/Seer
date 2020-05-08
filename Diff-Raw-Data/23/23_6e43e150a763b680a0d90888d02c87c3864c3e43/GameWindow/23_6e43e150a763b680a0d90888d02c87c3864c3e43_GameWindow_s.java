 package it.univr.gameoflife;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 
 public class GameWindow extends JFrame {
 	
 	private final Game game;
 	private final GraphicGrid graphicGrid;
 	private int cellSize = 10;
 	private boolean started;
 	private final int numOfThreads = 2;
 	private int delay = 200;
 	private static final Shape[] shapes = new Shape[] {
 		new Shape("Aliante", new Point(0,0), new Point(0,1), new Point(0,2), new Point(1,0), new Point(2,1)),
 		new Shape("Astronave leggera", new Point(0,0), new Point(0,3), new Point(1,4), new Point(2,0), new Point(2,4), new Point(3,1),
 				new Point(3,2), new Point(3,3), new Point(3,4)),
 		new Shape("Blocco", new Point(0,0), new Point(0,1), new Point(1,0), new Point(1,1)),
 		new Shape("Lampeggiatore", new Point(0,0), new Point(0,1), new Point(0,2))
 	};
 
 	public GameWindow() {
 		super("Game Of Life");
 		Container pane = this.getContentPane();
 
 		game = new Game(75, 45);
 		graphicGrid = new GraphicGrid();
 		
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		pane.setLayout(new BorderLayout());
 		pane.add(createToolBar(), BorderLayout.PAGE_START); 
 		pane.add(graphicGrid, BorderLayout.CENTER);
 		pane.add(createSlider(), BorderLayout.PAGE_END);
 		this.pack();
 		this.addComponentListener(new ComponentAdapter() {
 			
 			@Override
 			public void componentResized(ComponentEvent e) {
 				synchronized (game) {
 					game.resize(graphicGrid.getWidth() / (cellSize + 1), graphicGrid.getHeight() / (cellSize + 1));
 				}
 			}
 		});
 		
 		//TEST: CREIAMO UN GLIDER
 		game.getGrid().changeState(new Point(3,2));
 		game.getGrid().changeState(new Point(4,3));
 		game.getGrid().changeState(new Point(5,1));
 		game.getGrid().changeState(new Point(5,2));
 		game.getGrid().changeState(new Point(5,3));
 
 	}
 	
 	public static void main(String[] args) {
 		new GameWindow().setVisible(true);
 	}
 
 	private JToolBar createToolBar() {
 		JToolBar toolBar = new JToolBar();
 		toolBar.setFloatable(false);
 		final JButton start = new JButton("Inizio");
 		start.addActionListener(new ActionListener() {
 
 			@Override
             public void actionPerformed(ActionEvent e) {
 				started = !started;
 				if (started == true) {
 					start.setText("Stop");
 					new Worker().start();
 				} else
 					start.setText("Inizio");
             }                       
 		});
 		toolBar.add(start);
 		
 		final JButton step = new JButton("Avanti");
 		step.addActionListener(new ActionListener() {
 
 			@Override
             public void actionPerformed(ActionEvent e) {
 				if (started == false) {
 					game.new NextGeneration(numOfThreads);
 					graphicGrid.repaint();
 				}
             }                       
 		});
 		toolBar.add(step);
 		toolBar.addSeparator();
 		
 		final JComboBox zoomSelector = new JComboBox(new String[]{"Piccolo", "Medio", "Grande"});
 		zoomSelector.setSelectedIndex(1);
 		zoomSelector.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				final int selectedIndex = zoomSelector.getSelectedIndex();
				if(selectedIndex != 0) {
					switch(selectedIndex) {
						case 0: cellSize = 8; break;
						case 1: cellSize = 10; break;
						case 2: cellSize = 12; break;
						default: ;
					}
					synchronized (game) {
						game.resize(graphicGrid.getWidth() / (cellSize + 1), graphicGrid.getHeight() / (cellSize + 1));
					}
					if(!started)
						graphicGrid.repaint();
 				}
 			}
 		});
 		toolBar.add(zoomSelector);
 
 		final String[] shapeNames = new String[shapes.length + 1];
 		shapeNames[0] = "Scegli una forma e clicca sulla griglia per inserirla";
 		int i = 1;
 		for(Shape s : shapes)
 			shapeNames[i++] = s.getName();
 		final JComboBox shapeSelector = new JComboBox(shapeNames);
 		shapeSelector.setSelectedIndex(0);
 		shapeSelector.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				final int selectedIndex = shapeSelector.getSelectedIndex();
 				if(selectedIndex != 0) 
 					graphicGrid.selectedShape = shapes[selectedIndex - 1];
 			}
 		});
 		toolBar.add(shapeSelector); 
 		return toolBar;
 	}
 	
 	private JSlider createSlider() {
 		final JSlider slider = new JSlider(0, 475, 300);
 		slider.addChangeListener(new ChangeListener() {
 			
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				delay = 500 - slider.getValue();
 			}
 		});
 		return slider;
 	}
 	
 	private class GraphicGrid extends JLabel {
 		
 		private Shape selectedShape;
 		
 		private GraphicGrid() {
 			this.setPreferredSize(new Dimension(game.getSize().width * (cellSize + 1), game.getSize().height * (cellSize + 1)));
 			this.addMouseListener(new MouseAdapter() {
 				
 				@Override
 				public void mousePressed(MouseEvent e) {
 					if (selectedShape != null) {
 						synchronized(game) {
 							game.getGrid().insertShape(new Point(e.getY() / (cellSize + 1), e.getX() / (cellSize + 1)), selectedShape);
 						}
 						selectedShape = null;
 						graphicGrid.repaint();
 					}
 					else if (started) 
 						game.markDeadCell(e.getY() / (cellSize + 1), e.getX() / (cellSize + 1));
 					else {
 						game.getGrid().changeState(e.getY() / (cellSize + 1), e.getX() / (cellSize + 1));
 						graphicGrid.repaint();
 					}
 				}
 			});
 		}
 	
 		@Override
 		public void paintComponent(Graphics g) {
 			Grid gameGrid = game.getGrid();
 			Dimension gameSize = game.getSize();
 			for (int i = 0; i < gameSize.height; i++)
 				for (int j = 0; j < gameSize.width; j++) {
 					g.setColor(gameGrid.isAlive(i, j) ? Color.GREEN : Color.GRAY);
 					g.fillRect((cellSize + 1) * j, (cellSize + 1) * i, cellSize, cellSize);
 				}
 		}
 		
 		
 	}
 	
 	private class Worker extends Thread {
 		
 		@Override
 		public void run() {
 			while (started) {
 				synchronized (game) {
 					game.new NextGeneration(numOfThreads);
 					graphicGrid.repaint();
 				}
 				try {
 					Thread.sleep(delay);
 				} catch (InterruptedException e) { }
 			}
 		}
 	}
 
 }
