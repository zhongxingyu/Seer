 package game.ui;
 
 import game.Cell;
 import game.Command;
 import game.State;
 import game.ai.Fitness;
 import game.log.Log;
 import game.stepper.SingleStepper;
 import game.util.MapUtil;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.LinkedList;
import java.util.prefs.BackingStoreException;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JToolBar;
 
 /**
  * @author christoph
  * @author Felix Rieger
  */
 
 public class SimulateWindow extends JFrame implements KeyListener {
 	
 	private static final long serialVersionUID = 1L;
 	
 	public LinkedList<State> states;
 	public LinkedList<State> customStates;
 	public JTextArea textArea;
 	public int current = 0;
 	public boolean playing = false;
 	public Player player;
 	public SingleStepper stepper;
 	public Fitness fitness;
 	public State custom;
 
 	public class Player extends Thread {
 		private int delta;
 		private long last = 0;
 		public boolean stopped = false;
 				
 		public Player(int time) {
 			super();
 			this.delta = time;
 		}
 		
 		public void run() {
 			if(last == 0) {
 				last = System.currentTimeMillis();
 			}
 			while(!stopped && current < states.size()-1) {
 				if(last + delta < System.currentTimeMillis()) {
 					updateText(states.get(++current));
 					last = System.currentTimeMillis();
 				}
 			}
 		}
 		
 	}
 	
 	public SimulateWindow(String name, Fitness fitness, SingleStepper stepper) {		
 	  super("Simulation " + name);
 	  player = this.new Player(200);
 	  custom = null;
 	  customStates = new LinkedList<State>();
 	  this.fitness = fitness;
 	  this.stepper = stepper;
 	  
 	  this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	  this.setSize(300, 400);
 	  
 	  JToolBar tbar = new JToolBar();
 	  
 	  final JButton buttonBack = new JButton("Back");
 	  buttonBack.setEnabled(false);
 	  tbar.add(buttonBack);
 	  
 	  final JButton buttonNext = new JButton("Next");
 	  tbar.add(buttonNext);
 	  final JButton buttonPlay = new JButton("Play"); 
	  tbar.add(buttonPlay);
 	    
 	  buttonPlay.addActionListener(new ActionListener() {
 		
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			playing = !playing;
 			if(playing) {
 				buttonPlay.setText("Pause");
 				player.stopped = false;
 				player.start();
 			} else {
 				buttonPlay.setText("Play");
 				player.stopped = true;
 			}
 			
 		}
 		  
 	  });
 	  
 	  buttonBack.addActionListener(new ActionListener() {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			updateText(states.get(--current));
 			if(current == 0) {
 				buttonBack.setEnabled(false);		
 			}
 			buttonNext.setEnabled(true);
 		}
 			  
 	  });
 
 	  buttonNext.addActionListener(new ActionListener() {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			updateText(states.get(++current));
 			if(current == (states.size()-1)) {
 				buttonNext.setEnabled(false);		
 			}
 			buttonBack.setEnabled(true);
 		}
 		  
 	  });
 	  
 	  
 	  textArea = new JTextArea() {
 	    @Override
 	    public void paintComponent(Graphics textAreaG) {
 	      super.paintComponent(textAreaG);
 	      drawStuff(textAreaG);
 	    }
 	  };
 	  
 	  Font font = new Font("Monospaced", Font.PLAIN, 18);
 	  
 	  textArea.setFont(font);
 	  textArea.setEditable(false);
 	  textArea.addKeyListener(this);
 	  /*g.setColor(new Color(1.0f, 0.0f, 0.0f));
 	  g.fillOval(10, 30, 100, 200);*/
 	  
 	  JPanel jplContentPane = new JPanel();
 	  jplContentPane.setLayout(new BorderLayout());
 	  jplContentPane.setPreferredSize(new Dimension(400, 100));
 	  jplContentPane.add(tbar, BorderLayout.NORTH);
 	  jplContentPane.add(textArea, BorderLayout.CENTER);
 	  System.out.println(" ---> " + textArea.getGraphics());
 	  //Graphics2D g = (Graphics2D) textArea.getGraphics();
 	  //g.setColor(new Color(1.0f, 0.0f, 0.0f));
     //g.fillOval(10, 30, 100, 200);
     
   	  setContentPane(jplContentPane);  
   	  
   	
 
   	  states = new LinkedList<State>();
 	}
 	
 	private State currentState;  // needed for drawing; was too lazy to implement it differently.-Felix
 	
 	
 	boolean showNextLambdaArrows = false;
 	boolean showLambdaDensity = false;
 	boolean showRobot = false;
 	
 	public void drawStuff(Graphics g) {
 	    //FontMetrics fm = g.getFontMetrics(new Font("Monospaced", Font.PLAIN, 12));
 	    if (currentState != null) {
 	    /*int yoffset = 20;
 	    int boxSizeX = 8;
 	    int boxSizeY = 18;*/
 	    int yoffset = 26;
 	    int boxSizeX = 11;
 	    int boxSizeY = 25;
 	    if (showNextLambdaArrows) {
   	    for (int i = 0; i < currentState.board.length; i++) {
   	      int xcoord = (i / currentState.board.width);     // XXX shouldn't this be the other way round?
   	      int ycoord = (i % currentState.board.height);    // XXX maybe nextLambda is encoded wrongly?
   	      int nl = currentState.nextLambda(i);
   	      //g.setColor(Color.GREEN);
   	      //g.drawRect(boxSizeX * xcoord, yoffset + boxSizeY * ycoord, boxSizeX, boxSizeY);
   	      g.setColor(new Color(1.0f, 0f,0f,0.6f));
   	      if (nl != -1) {
   	        drawArrow(g, boxSizeX * xcoord + boxSizeX/2, yoffset + boxSizeY * (currentState.board.height - 1 - ycoord) + boxSizeY/2, boxSizeX/2 + boxSizeX * (nl / currentState.board.width), yoffset + boxSizeY/2 + boxSizeY * (currentState.board.height-1 - (nl % currentState.board.height)));
   	      }	      
   	    }
 	    }
 	    if (showLambdaDensity) {
   	    int[] integralBoard = MapUtil.computeIntegralBoard(currentState.board, Cell.Lambda);
   	    float[] lambdaDensity = MapUtil.getDensityMap(5, currentState.board.width, integralBoard);
   	    for (int i = 0; i < lambdaDensity.length; i++) {
   	       int xcoord = (i / currentState.board.width);     
   	       int ycoord = (i % currentState.board.height);   
   	       float ld = lambdaDensity[i];
   	       g.setColor(new Color(ld, ld, ld, 0.4f));
   	       g.fillRect(boxSizeX * xcoord, yoffset + boxSizeY * (currentState.board.height - 1 - ycoord), boxSizeX, boxSizeY);
   	    }
 	    }
 	    if (showRobot) {
 	      g.setColor(new Color(0f,1f,0f,0.8f));
 	      g.fillOval(boxSizeX * currentState.robotCol, boxSizeY * (currentState.board.height - currentState.robotRow), boxSizeX, boxSizeY);
 	    }
 	    
 	  }
 	}
 	
 	public void drawArrow(Graphics g, int startx, int starty, int endx, int endy) {
 	  g.drawLine(startx, starty, endx, endy);
 	  g.fillRect(endx-1, endy-1, 3, 3);
 	}
 	
 	public void addState(State state) {
 		states.add(state);
 		if(states.size() == 1) {
 			updateText(state);
 		}
 	}
 	
 	public void updateText(State state) {
 	  currentState = state;
	  textArea.setText(
	    "fitness=" + state.fitness + ", score=" + state.score + "\n"
	  + state.toString());
 	}
 
 	@Override
 	public void keyPressed(KeyEvent arg0) {
 	  // gui events:
 	  switch(arg0.getKeyCode()) {
       case KeyEvent.VK_0:
         showNextLambdaArrows = !showNextLambdaArrows;
         repaint();
         return;
       case KeyEvent.VK_9:
         showLambdaDensity = !showLambdaDensity;
         repaint();
         return;
       case KeyEvent.VK_8:
         showRobot = !showRobot;
         repaint();
         return;
 	  }
 	  
 	  
 	  // state-modifying events
 		State parent;
 		if(custom == null) {
 			parent = states.get(current);
 		} else {
 			parent = custom; 
 		}
 		switch(arg0.getKeyCode()) {
 			case KeyEvent.VK_UP:
 				custom = stepper.step(parent, Command.Up);
 				break;
 			case KeyEvent.VK_DOWN:
 				custom = stepper.step(parent, Command.Down);
 				break;
 			case KeyEvent.VK_LEFT:
 				custom = stepper.step(parent, Command.Left);
 				break;
 			case KeyEvent.VK_RIGHT:
 				custom = stepper.step(parent, Command.Right);
 				break;
 			case KeyEvent.VK_W:
 				custom = stepper.step(parent, Command.Wait);
 				break;
 			case KeyEvent.VK_U:
 				custom = customStates.removeLast();
 				if(customStates.size() != 0) {
 					parent = customStates.removeLast();
 				} else {
 					parent = states.get(current);
 				}
 				break;
 			default:
 			  return;  // don't add extra state on invalid keypress
 		}
 		customStates.add(parent);
 		custom.fitness = fitness.evaluate(custom);
 		updateText(custom);
 		Log.println(customStates.size());	
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 }
