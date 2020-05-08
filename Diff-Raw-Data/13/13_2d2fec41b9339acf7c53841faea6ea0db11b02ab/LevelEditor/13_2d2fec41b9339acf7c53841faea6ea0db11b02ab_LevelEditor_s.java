 package JPushy.LevelEditor;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferStrategy;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 import JPushy.Blocks;
 import JPushy.GraphicUtils;
 import JPushy.Gui.LevelSelector;
 import JPushy.Types.Blocks.Block;
 
 public class LevelEditor extends Canvas implements MouseListener, MouseMotionListener, MouseWheelListener {
 	private static final long	serialVersionUID	= 1L;
 
 	private JFrame	          frame;
 	private boolean	          running;
 	private JTextField	      xsize;
 	private JTextField	      ysize;
 	private JTextField	      layers;
 
 	private static final Font	font	           = new Font("Verdana", 1, 18);
 
 	private int	              width	           = 8;
 	private int	              height	         = 8;
 	private int	              length	         = 1;
 
 	private double	          zoom	           = 1;
 
 	private float	            xOff;
 	private float	            yOff;
 	private int	              clayer	         = 0;
 
 	private float	            lmx;
 	private float	            lmy;
 
 	private boolean[]	        mouse	           = new boolean[12];
 
 	private boolean	          showBlockIds	   = false;
 
 	private int[][][]	        tiles	           = new int[8][8][1];
 
 	private JButton	          save;
 	private JTextField	      name;
 	JTextField	              cLayerF	         = new JTextField("0");
 	JComboBox	                comboBox	       = new JComboBox();
 	String[]	                model	           = new String[Blocks.blockRegistry.size()];
 
 	private int	              tile;
 	private String	          lastTile	       = Blocks.blockRegistry.get(0).getName();
 
 	private LevelSelector	    selector;
 
 	public LevelEditor(LevelSelector selector) {
 		this.selector = selector;
 		Blocks.wakeUpDummy();
 		for (int i = 0; i < Blocks.blockRegistry.size(); i++) {
 			model[i] = Blocks.blockRegistry.get(i).getName();
 		}
 
 		setUpGUI();
 		running = true;
 		while (running) {
 			update();
 			render();
 			try {
 				Thread.sleep(2);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void setUpGUI() {
 		frame = new JFrame("Level Editor");
 		frame.setSize(1280, 720);
 		frame.setResizable(false);
 		frame.setLocationRelativeTo(null);
 		frame.getContentPane().setLayout(null);
 		frame.getContentPane().setBackground(Color.DARK_GRAY);

 		addMouseListener(this);
 		addMouseMotionListener(this);
 		addMouseWheelListener(this);
 		this.addKeyListener(new KeyListener() {

 			@Override
 			public void keyTyped(KeyEvent e) {
 				if (e.getKeyChar() == '+') {
 					int temp = clayer;
 					temp++;
 					if (temp == length) {
 					} else {
 						clayer++;
 						cLayerF.setText(clayer + "");
 					}
 				} else if (e.getKeyChar() == '-') {
 					int temp = clayer;
 					temp--;
 					if (temp < 0) {
 					} else {
 						clayer--;
 						cLayerF.setText(clayer + "");
 					}
 				}
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 			}
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 			}
 		});
 
 		setBounds(0, 0, 900, 720);
 		setBackground(Color.WHITE);
 		frame.getContentPane().add(this);
 
 		JLabel sizeX = new JLabel("X Size:");
 		sizeX.setBounds(920, 10, 200, 20);
 		applyFont(sizeX);
 		frame.getContentPane().add(sizeX);
 		xsize = new JTextField("8");
 		xsize.setBounds(1000, 10, 200, 25);
 		frame.getContentPane().add(xsize);
 
 		JCheckBox showIds = new JCheckBox("Show Block ID's");
 		showIds.setBounds(920, 130, 200, 20);
 		showIds.setFont(font);
 		showIds.setForeground(Color.WHITE);
 		showIds.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				showBlockIds = !showBlockIds;
 			}
 		});
 		frame.getContentPane().add(showIds);
 
 		JLabel sizeY = new JLabel("Y Size:");
 		sizeY.setBounds(920, 40, 200, 20);
 		applyFont(sizeY);
 		frame.getContentPane().add(sizeY);
 		ysize = new JTextField("8");
 		ysize.setBounds(1000, 40, 200, 25);
 		frame.getContentPane().add(ysize);
 
 		JLabel layersL = new JLabel("Layers:");
 		layersL.setBounds(920, 70, 200, 20);
 		applyFont(layersL);
 		frame.getContentPane().add(layersL);
 		layers = new JTextField("1");
 		layers.setBounds(1000, 70, 200, 25);
 		frame.getContentPane().add(layers);
 
 		JButton applySize = new JButton("Apply");
 		applySize.setBounds(1000, 100, 200, 25);
 		frame.getContentPane().add(applySize);
 		applySize.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				width = Integer.parseInt(xsize.getText());
 				height = Integer.parseInt(ysize.getText());
 				length = Integer.parseInt(layers.getText());
 				tiles = new int[width][height][length];
 				createWalls();
 			}
 		});
 
 		JLabel cLayerL = new JLabel("Current Layer:");
 		cLayerL.setBounds(920, 160, 220, 20);
 		applyFont(cLayerL);
 		frame.getContentPane().add(cLayerL);
 		cLayerF.setBounds(1070, 160, 146, 25);
 		frame.getContentPane().add(cLayerF);
 		cLayerF.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				clayer = Integer.parseInt(((JTextField) e.getSource()).getText());
 			}
 		});
 
 		JLabel stile = new JLabel("Selected tile:");
 		stile.setBounds(920, 190, 220, 20);
 		applyFont(stile);
 		frame.getContentPane().add(stile);
 
 		save = new JButton("Save");
 		save.setBounds(1000, 600, 200, 25);
 		frame.getContentPane().add(save);
 
 		JLabel nameL = new JLabel("Name:");
 		nameL.setBounds(920, 570, 200, 20);
 		applyFont(nameL);
 		frame.getContentPane().add(nameL);
 		name = new JTextField("Untitled");
 		name.setBounds(1000, 570, 200, 25);
 		frame.getContentPane().add(name);
 
 		comboBox.setModel(new DefaultComboBoxModel(model));
 		comboBox.setBounds(1070, 191, 146, 20);
 		comboBox.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				System.out.println(e.getItem());
 				if (e.getItem().toString() == lastTile) {
 				} else {
 					String n = e.getItem().toString();
 					for (int i = 0; i < model.length; i++) {
 						if (model[i] == n) {
 							tile = i;
 						}
 					}
 					lastTile = e.getItem().toString();
 				}
 			}
 		});
 		frame.getContentPane().add(comboBox);
 
 		save.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				save();
 			}
 		});
 
 		frame.setVisible(true);
 
 		createWalls();
 	}
 
 	private void save() {
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("Data/lvl/" + name.getText() + ".lvl")));
 			writer.write("<level name=\"" + name.getText() + "\" version='0.1'>\n");
 			for (int z = 0; z < length; z++) {
 				writer.write("<stage id=" + z + ">\n");
 				for (int y = 0; y < height; y++) {
 					for (int x = 0; x < width; x++) {
 						writer.write("" + tiles[x][y][z]);
 					}
 					writer.newLine();
 				}
 				writer.write("</stage>\n");
 			}
 			writer.close();
 			writer = new BufferedWriter(new FileWriter(new File("Data/lvl/" + name.getText() + ".cfg")));
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		selector.updateLevels();
 	}
 
 	private void createWalls() {
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				for (int z = 0; z < length; z++) {
 					if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
 						tiles[x][y][z] = 1;
 					}
 				}
 			}
 		}
 	}
 
 	public void update() {
 		if (mouse[1]) {
 			int ax = (int) ((lmx - xOff) / zoom) / 16;
 			int ay = (int) ((lmy - yOff) / zoom) / 16;
 			if (ax < 0 || ax >= width || ay < 0 || ay >= height)
 				return;
 			tiles[ax][ay][clayer] = tile;
 		}
 	}
 
 	public void render() {
 		BufferStrategy bs = getBufferStrategy();
 		if (bs == null) {
 			createBufferStrategy(2);
 			requestFocus();
 			return;
 		}
 		Block b;
 		Graphics2D g = (Graphics2D) bs.getDrawGraphics();
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, getWidth(), getHeight());
 		g.setColor(Color.BLACK);
 		g.translate(xOff, yOff);
 		g.scale(zoom, zoom);
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				b = Blocks.getBlockById(tiles[x][y][clayer]);
 				g.drawRect(x * 16, y * 16, 16, 16);
 				g.drawImage(GraphicUtils.getImageFromPicture(b.getTexture()), x * 16, y * 16, 16, 16, null);
 				g.setFont(new Font("Arial", 0, 15));
 				Rectangle2D r = g.getFontMetrics().getStringBounds("" + tiles[x][y][clayer], g);
 				if (showBlockIds)
 					g.drawString("" + tiles[x][y][clayer], (int) ((x * 16) + (16 - r.getWidth()) / 2), (int) ((y * 16) + (13 - r.getHeight()) / 2) + 16);
 			}
 		}
 		g.dispose();
 		bs.show();
 	}
 
 	private void applyFont(JLabel l) {
 		l.setFont(font);
 		l.setForeground(Color.WHITE);
 	}
 
 	public void mouseWheelMoved(MouseWheelEvent e) {
 		zoom += e.getWheelRotation() / 5.0;
 		if (zoom < 0)
 			zoom = 0;
 	}
 
 	public void mouseDragged(MouseEvent e) {
 		if (!mouse[3]) {
 			lmx = e.getX();
 			lmy = e.getY();
 			return;
 		}
 		xOff += e.getX() - lmx;
 		yOff += e.getY() - lmy;
 		lmx = e.getX();
 		lmy = e.getY();
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		if (e.getButton() != 0)
 			return;
 		lmx = e.getX();
 		lmy = e.getY();
 	}
 
 	public void mouseClicked(MouseEvent e) {
 
 	}
 
 	public void mouseEntered(MouseEvent e) {
 
 	}
 
 	public void mouseExited(MouseEvent e) {
 	}
 
 	public void mousePressed(MouseEvent e) {
 		mouse[e.getButton()] = true;
 		if (e.getButton() == 2) {
 			int ax = (int) ((lmx - xOff) / zoom) / 16;
 			int ay = (int) ((lmy - yOff) / zoom) / 16;
 			if (ax < 0 || ax >= width || ay < 0 || ay >= height)
 				return;
 			tile = tiles[ax][ay][clayer];
 			int val = Integer.valueOf(tile);
 			comboBox.setSelectedIndex(val);
 		}
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		mouse[e.getButton()] = false;
 	}
 
 	public boolean isRunning() {
 		return running;
 	}
 
 	public void setRunning(boolean running) {
 		this.running = running;
 	}
 
 }
