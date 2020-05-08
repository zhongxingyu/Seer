 package ss.gui.out;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.KeyStroke;
 
 import ss.apiImpl.strategies.ReasignationStrategyImpl;
 
 @SuppressWarnings("serial")
 public class MenuBar extends JMenuBar {
 
 	private JMenuBar menuBar;
 	private Frame frame;
 
 	public MenuBar(Frame frame) {
 		this.frame = frame;
 
 		menuBar = new JMenuBar();
 		JMenu simulatorMenu = new JMenu("Simulador");
 		simulatorMenu.setMnemonic(KeyEvent.VK_S);
 
 		JMenuItem simulateWithStrategyIdle = new JMenuItem("Simular con estrategia Idle");
 		JMenuItem simulateWithStrategySwitch = new JMenuItem("Simular con estrategia Switch");
 		JMenuItem simulateWithStrategyFreelance = new JMenuItem("Simular con estrategia Freelance");
		simulateWithStrategyIdle.setMnemonic(KeyEvent.VK_N);
		simulateWithStrategyIdle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
 				ActionEvent.CTRL_MASK));
 		
 		simulateWithStrategyIdle.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				final Thread sim = new Thread(new Runnable() {
 					@Override
 					public void run() {
 						getFrame().getSimulator().build(
 								new SimulationListenerImpl(getFrame()),ReasignationStrategyImpl.IDLE_STRATEGY);
 						getFrame().restart();
 						getFrame().getSimulator().start(10);
 					}
 				});
 				sim.start();
 			}
 		});
 		simulateWithStrategySwitch.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				final Thread sim = new Thread(new Runnable() {
 					@Override
 					public void run() {
 						getFrame().getSimulator().build(
 								new SimulationListenerImpl(getFrame()),ReasignationStrategyImpl.SWITCH_STRATEGY);
 						getFrame().restart();
 						getFrame().getSimulator().start(10);
 					}
 				});
 				sim.start();
 			}
 		});
 		simulateWithStrategyFreelance.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				final Thread sim = new Thread(new Runnable() {
 					@Override
 					public void run() {
 						getFrame().getSimulator().build(
 								new SimulationListenerImpl(getFrame()),ReasignationStrategyImpl.FREELANCE_STRATEGY);
 						getFrame().restart();
 						getFrame().getSimulator().start(10);
 					}
 				});
 				sim.start();
 			}
 		});
 
 		/*
 		 * Closes the application
 		 */
 		JMenuItem close = new JMenuItem("Close");
 		close.setMnemonic(KeyEvent.VK_C);
 		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
 				ActionEvent.CTRL_MASK));
 		close.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				System.exit(0);
 			}
 		});
 
 		simulatorMenu.add(simulateWithStrategyIdle);
 		simulatorMenu.add(new JSeparator());
 		simulatorMenu.add(simulateWithStrategySwitch);
 		simulatorMenu.add(new JSeparator());
 		simulatorMenu.add(simulateWithStrategyFreelance);
 		simulatorMenu.add(new JSeparator());
 		simulatorMenu.add(close);
 		menuBar.add(simulatorMenu);
 	}
 
 	/**
 	 * Returns the menuBar
 	 * 
 	 * @return JMenuBar
 	 */
 	public JMenuBar getMenu() {
 		return menuBar;
 	}
 
 	private Frame getFrame() {
 		return frame;
 	}
 
 }
