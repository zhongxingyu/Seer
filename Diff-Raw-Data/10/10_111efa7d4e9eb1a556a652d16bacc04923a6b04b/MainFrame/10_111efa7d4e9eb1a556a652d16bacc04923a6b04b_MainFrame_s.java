 package jgpfun.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import jgpfun.life.Simulation;
 import org.joda.time.DateTimeZone;
 import org.joda.time.Instant;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.PeriodFormat;
 
 /**
  *
  * @author hansinator
  */
 public class MainFrame extends JFrame implements WindowListener {
 
     private MainView mainView;
 
     private final JScrollPane centerPane;
 
     public final JPanel sidePaneLeft, sidePaneRight;
 
     public final BottomPanel bottomPane;
 
     private final Simulation simulation;
 
     private volatile boolean running = true;
 
 
     public MainFrame(int width, int height, Simulation simulation) {
         super("BAH! Bonn!!1!11!!!");
         this.simulation = simulation;
 
         // create all direct clients
         centerPane = new JScrollPane();
         sidePaneLeft = new JPanel();
         sidePaneRight = new StatisticsHistoryPanel(simulation.statisticsHistory);
         bottomPane = new BottomPanel(simulation);
 
         // set default center pane size properties
         centerPane.setPreferredSize(new Dimension(800, 600));
 
         // init application specific content
         initClientViews(width, height);
 
         // add the menu bar
         setJMenuBar(createMenuBar());
 
         // setup and add all stuff to the content pane
         Container contentPane = getContentPane();
         contentPane.setLayout(new BorderLayout());
         contentPane.add(centerPane, BorderLayout.CENTER);
         contentPane.add(sidePaneLeft, BorderLayout.LINE_START);
         contentPane.add(sidePaneRight, BorderLayout.LINE_END);
         contentPane.add(bottomPane, BorderLayout.PAGE_END);
 
         // get ready for action
         pack();
         setVisible(true);
     }
 
 
     private JMenuBar createMenuBar() {
         JMenuBar menuBar = new JMenuBar();
 
         menuBar.add(new JMenu("File")).add(new JMenuItem("Exit")).addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 stopSimulation();
             }
 
         });
 
         JMenu simulationMenu = new JMenu("Simulation");
         simulationMenu.add(new JMenuItem("Reset")).addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 simulation.reset();
             }
 
         });
         simulationMenu.add(new JMenuItem("Preferences"));
         menuBar.add(simulationMenu);
 
         return menuBar;
     }
 
 
     private void initClientViews(int width, int height) {
         // to be put elsewhere
         mainView = new MainView(simulation.world);
         mainView.setPreferredSize(new Dimension(width, height));
 
         setCenterPaneView(mainView);
     }
 
 
     public void setCenterPaneView(Component view) {
         centerPane.setViewportView(view);
     }
 
 
     public void startSimulation() {
         System.out.println("MainView size: " + mainView.getWidth() + "x" + mainView.getHeight());
         System.out.println("Start time: " + DateTimeFormat.fullDateTime().withZone(DateTimeZone.getDefault()).print(new Instant()));
 
         //TODO: to be put somewhere else
         addWindowListener(this);
 
         //TODO: thread this and put it somewhere else
         running = true;
         int startGen = 0;
         long now, generationsPerMinuteAverage = 0, generationsPerMinuteCount = 0;
         long startTime = System.currentTimeMillis();
         long lastStats = startTime;
         while (running) {
             //FIXME: add events to the simulation, so that a main view can draw upon an event
             simulation.runGeneration(4000, mainView, bottomPane.infoPanel);
 
             //print generations per minute info
             now = System.currentTimeMillis();
             if ((now - lastStats) >= 3000) {
                long generationsPerMinute = (simulation.getGeneration() - startGen) * (60000 / (now - startTime));
                 generationsPerMinuteAverage += generationsPerMinute;
                 generationsPerMinuteCount++;
 
                 System.out.println("GPM: " + generationsPerMinute);
                 
                 System.out.println("Runtime: " + PeriodFormat.getDefault().print(new org.joda.time.Period(startTime, now)));
                 startGen = simulation.getGeneration();
                 lastStats = now;
             }
         }
         
         System.out.println("\nRuntime: " + PeriodFormat.getDefault().print(new org.joda.time.Period(startTime, System.currentTimeMillis())));
         System.out.println("Average GPM: " + ((generationsPerMinuteCount>0)?(generationsPerMinuteAverage / generationsPerMinuteCount):0));
         System.exit(0);
     }
 
 
     public void stopSimulation() {
         running = false;
         simulation.reset();
     }
 
 
     @Override
     public void windowOpened(WindowEvent e) {
     }
 
 
     @Override
     public void windowClosing(WindowEvent e) {
         stopSimulation();
     }
 
 
     @Override
     public void windowClosed(WindowEvent e) {
         stopSimulation();
     }
 
 
     @Override
     public void windowIconified(WindowEvent e) {
     }
 
 
     @Override
     public void windowDeiconified(WindowEvent e) {
     }
 
 
     @Override
     public void windowActivated(WindowEvent e) {
     }
 
 
     @Override
     public void windowDeactivated(WindowEvent e) {
     }
 
 }
