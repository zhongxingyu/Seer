 package edu.stuy.goldfish;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 
 import java.util.Random;
 
 public class Render extends Canvas implements Runnable, MouseListener,
         MouseMotionListener, ActionListener {
     private static final long serialVersionUID = 1L;
 
     public static String title = "Goldfish";
     public static int width;
     public static int height;
     public static int scale;
 
     public int fps_now;
     public boolean paused;
     public String rule;
     public boolean reset;
 
     private Grid _grid;
     private int[] _pixels;
     private BufferedImage _image;
     private long _lastTick;
     private String[] _rules;
     private JFrame _frame;
     private JButton pauseButton;
     private Random random = new Random();
 
     public Render(int width, int height, Grid g, String[] rules) {
         addMouseListener(this);
         addMouseMotionListener(this);
         Render.width = width;
         Render.height = height;
         setScale();
         paused = false;
         reset = false;
         rule = rules[0];
         _grid = g;
         _image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
         _pixels = ((DataBufferInt) _image.getRaster().getDataBuffer()).getData();
         _lastTick = 0;
         _rules = rules;
         fps_now = 15;
         Dimension size = new Dimension(width * scale, height * scale);
         setPreferredSize(size);
         setFrame();
     }
 
     public Render(int width, int height, Grid g) {
         this(width, height, g, new String[0]);
     }
 
     public Render(int width, int height) {
         this(width, height, new Grid(width, height));
     }
 
     public Render() {
         this(256, 256);
     }
 
     private void setScale() {
         if (height <= 128 || width <= 128) {
             Render.scale = 4;
         } else if (height <= 256 || width <= 256) {
             Render.scale = 2;
         } else {
             Render.scale = 1;
         }
     }
 
     private void setFrame() {
         JMenuBar menuBar = new JMenuBar();
         JMenu menuAlgo = new JMenu("Algorithms");
         for (String rule : _rules) {
             JMenuItem menuAlgoItem = new JMenuItem(rule);
             menuAlgo.add(menuAlgoItem);
             menuAlgoItem.addActionListener(this);
         }
         menuBar.add(menuAlgo);
 
         pauseButton = new JButton("Pause");
         pauseButton.setActionCommand("pause");
         pauseButton.setPreferredSize(new Dimension(90,0));
         menuBar.add(pauseButton);
         pauseButton.addActionListener(this);
 
         JButton resetButton = new JButton("Reset");
         resetButton.setActionCommand("reset");
         menuBar.add(resetButton);
         resetButton.addActionListener(this);
 
         JButton randomButton = new JButton("Random");
         randomButton.setActionCommand("random");
         menuBar.add(randomButton);
         randomButton.addActionListener(this);
 
         JButton clearButton = new JButton("Clear");
         clearButton.setActionCommand("clear");
         menuBar.add(clearButton);
         clearButton.addActionListener(this);
 
         JSlider framesPerSecond = new JSlider(JSlider.HORIZONTAL, 0, 30, 15);
         ChangeListener fpschange = new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent event) {
                 JSlider source = (JSlider) event.getSource();
                 if (!source.getValueIsAdjusting()) {
                     int fps = (int) source.getValue();
                     if (fps == 0) {
                         paused = true;
                         pauseButton.setText("Unpause");
                     } else {
                         paused = false;
                         fps_now = fps;
                         pauseButton.setText("Pause");
                     }
                 }
             }
         };
         framesPerSecond.addChangeListener(fpschange);
         framesPerSecond.setMajorTickSpacing(10);
         framesPerSecond.setMinorTickSpacing(1);
         framesPerSecond.setPaintTicks(true);
         framesPerSecond.setPaintLabels(true);
         menuBar.add(framesPerSecond);
 
         _frame = new JFrame();
         _frame.setJMenuBar(menuBar);
         _frame.setResizable(false);
         _frame.setTitle(title);
         _frame.add(this);
         _frame.pack();
         _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         _frame.setLocationRelativeTo(null);
         _frame.setVisible(true);
     }
 
     private void update() {
         int state;
         int states = Goldfish.getMaxStates(rule);
         for (int i = 0; i < width; i++) {
             for (int j = 0; j < height; j++) {
                 state = _grid.getPatch(i, j).getState();
                 if (_pixels[i + j * width] == state) {
                 } else {
                     draw(i, j, (int) ((state / ((double) states - 1)) * 0xffffff));
                 }
             }
         }
     }
 
     public void run() {
         BufferStrategy bs;
         Graphics g;
 
         bs = getBufferStrategy();
         if (bs == null) {
             createBufferStrategy(1);
             return;
         }
 
         update();
 
         g = bs.getDrawGraphics();
         g.drawImage(_image, 0, 0, getWidth(), getHeight(), null);
         g.dispose();
         bs.show();
     }
 
     public void sleep() {
         long since = System.currentTimeMillis() - _lastTick;
         if (since < 1000 / fps_now) {
             try {
                 Thread.sleep(1000 / fps_now - since);
             } catch (InterruptedException e) {
                 return;
             }
         }
         _lastTick = System.currentTimeMillis();
     }
 
     public void setGrid(Grid g) {
         _grid = g;
     }
 
     public void draw(int x, int y, int color) {
         _pixels[x + y * width] = color;
     }
 
     public void clear() {
         for (int i = 0; i < _grid.getHeight(); i++) {
             for (int j = 0; j < _grid.getWidth(); j++) {
                 _grid.getPatch(i, j).setState(0);
             }
         }
     }
 
     public void randomize() {
         for (int i = 0; i < _grid.getHeight(); i++) {
             for (int j = 0; j < _grid.getWidth(); j++) {
                 _grid.getPatch(i, j).setState(random.nextInt(Goldfish.getMaxStates(rule)));
             }
         }
     }
 
     @Override
     public void mouseDragged(MouseEvent e) {
         int states = Goldfish.getMaxStates(rule) - 1;
         if (e.getX() < 0 || e.getY() < 0 || e.getX() / scale > width || e.getY() / scale > height)
             return;
         _grid.getPatch(e.getX() / scale, e.getY() / scale).setState(states);
         e.consume();
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         int states = Goldfish.getMaxStates(rule) - 1;
         _grid.getPatch(e.getX() / scale, e.getY() / scale).setState(states);
         e.consume();
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
         int states = Goldfish.getMaxStates(rule) - 1;
         _grid.getPatch(e.getX() / scale, e.getY() / scale).setState(states);
         e.consume();
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
     }
 
     @Override
     public void mouseMoved(MouseEvent e) {
     }
 
    // Affects the algorithms menu
     @Override
     public void actionPerformed(ActionEvent event) {
         if ("pause".equals(event.getActionCommand())) {
             if (paused) {
                 paused = false;
                 pauseButton.setText("Pause");
             } else {
                 paused = true;
                 pauseButton.setText("Unpause");
             }
         } else if ("reset".equals(event.getActionCommand())) {
             clear();
             reset = true;
         } else if ("random".equals(event.getActionCommand())) {
             randomize();
         } else if ("clear".equals(event.getActionCommand())) {
             clear();
         } else {
             rule = event.getActionCommand();
         }
     }
 }
