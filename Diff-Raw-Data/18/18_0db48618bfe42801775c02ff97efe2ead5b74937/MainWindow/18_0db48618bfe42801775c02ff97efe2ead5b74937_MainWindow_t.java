 package eu.janinko.aiforlife.gui;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import eu.janinko.aiforlife.World.WorldStatistics;
 import eu.janinko.aiforlife.World.World;
 
 public class MainWindow extends JFrame implements ActionListener {
 	private static final long serialVersionUID = -503786105256643576L;
 	
 	private JScrollPane sp = null;
 	private NextButton nextButton = null;
 	private PaintedWorld paintedWorld = null;
 	private JTextArea stats = null;
 	javax.swing.Timer t;
 	
 	World world;
 	
 	private int born = 0;
 	private int damaged = 0;
 	private int died = 0;
 	
 	private int time = 0;
 	
 	private String lsep = System.getProperty("line.separator");
 
 	public MainWindow(World w){
 		super();
 		world = w;
 		init();
 	}
 
 	private void init() {
 		paintedWorld = new PaintedWorld(world);
 		nextButton = new NextButton(world, this);
 		sp = new JScrollPane(paintedWorld,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		stats = new JTextArea();
 		
 		JPanel top = new JPanel();
 		top.add(nextButton, BorderLayout.WEST);
 
 		JButton playpause = new JButton("Play/Pause");
 		playpause.setActionCommand("playpause");
 		playpause.addActionListener(this);
 		top.add(playpause, BorderLayout.CENTER);
 
 		JPanel topright = new JPanel();
 		top.add(topright, BorderLayout.WEST);
 		
 		JButton speedup = new JButton("+");
 		speedup.setActionCommand("speedup");
 		speedup.addActionListener(this);
 		topright.add(speedup, BorderLayout.NORTH);
 		JButton slowdown = new JButton("-");
 		slowdown.setActionCommand("slowdown");
 		slowdown.addActionListener(this);
 		topright.add(slowdown, BorderLayout.SOUTH);
 		
 		printStats();
 		getContentPane().add(top, BorderLayout.NORTH);
 		getContentPane().add(stats, BorderLayout.EAST);
 		getContentPane().add(sp, BorderLayout.CENTER);
 		
 		
 		setTitle("AI for Life");
 	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 	    setLocation(100, 100);
 	    setSize(800, 700);
 
 	    t = new javax.swing.Timer(100, nextButton);
 	}
 
 	public void worldUpdated() {
 		time++;
 		paintedWorld.repaint();
 		if(world instanceof WorldStatistics){
 			WorldStatistics w = (WorldStatistics) world;
 			born += w.getNewbornCount();
 			damaged += + w.getDamagedCount();
 			died += w.getDiedCount();
 		}
 
 		printStats();
 	}
 	
 	private void printStats(){
 		if(world instanceof WorldStatistics){
 			WorldStatistics w = (WorldStatistics) world;
 			stats.setText("Now:" + lsep +
 						  " Born: " + w.getNewbornCount() + lsep +
 					      " Damaged:  "+ w.getDamagedCount() + lsep +
 					      " Died: " + w.getDiedCount() + lsep +
 					      " minbreed: " + w.getProperty("minbreed") + lsep +
 					      " avgbreed: " + w.getProperty("avgbreed") + lsep +
 					      " maxbreed: " + w.getProperty("maxbreed") + lsep +
					      " Pray: " + w.getProperty("praycount") + lsep +
					      " Predator: " + w.getProperty("predatorcount") + lsep +
 					      "Overall:" + lsep + 
 						  " Born: " + born + lsep +
 					      " Damaged:  "+ damaged + lsep +
 					      " Died: " + died + lsep +
 					      "Time: " + time);
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 	    //String s = e.getActionCommand();
 
 	    if(e.getActionCommand().equals("playpause")){
 	    	if(t.isRunning()){
 	    		t.stop();
 	    	}else{
 	    	    t.start();
 	    	}
 	    }
 	    if(e.getActionCommand().equals("speedup")){
 	    	t.setDelay((int) (t.getDelay()*0.9)-1);
 	    }
 	    if(e.getActionCommand().equals("slowdown")){
 	    	t.setDelay((int) (t.getDelay()*1.1)+1);
 	    }
 	}
 
 }
