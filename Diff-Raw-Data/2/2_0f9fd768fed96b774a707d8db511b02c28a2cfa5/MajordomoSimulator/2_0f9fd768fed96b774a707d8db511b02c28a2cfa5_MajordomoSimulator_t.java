 package de.altimos.mdsd.majordomo.simulator;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.WindowConstants;
 
 import de.altimos.mdsd.majordomo.simulator.assemblies.MAssemblyContainer;
 import de.altimos.mdsd.majordomo.simulator.assemblies.MClockSensorAssembly;
 import de.altimos.mdsd.majordomo.simulator.assemblies.MLightSensorAssembly;
 import de.altimos.mdsd.majordomo.simulator.assemblies.MTemperatureSensorAssembly;
 
 public class MajordomoSimulator extends JFrame {
 
 	private static final long serialVersionUID = -3121504439011367966L;
 	
 	private HashMap<Long, MAssemblyContainer> containers = new HashMap<Long, MAssemblyContainer>();
 	private List<MAssemblyContainer> containerList = new LinkedList<MAssemblyContainer>();
 	private MConfiguration config;
 	
 	private Simulation sim;
 	private JToolBar toolbar = new JToolBar();
 	JToggleButton button = new JToggleButton("Run Simulation");
 	
 	public static void main(String[] args) {
 		new MajordomoSimulator().setVisible(true);
 	}
 
 	public MajordomoSimulator() {
 		config = new MConfiguration();
 		config.buildAssemblies(this);
 		
 		toolbar.add(button);
 		button.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				sim.running = button.isSelected();
 			}
 		});
 		
 		setLayout(new BorderLayout());
 		add(toolbar, BorderLayout.NORTH);
 		
 		JPanel p = new JPanel();
 		
 		p.setLayout(new GridLayout((int)Math.floor((containerList.size() / 2) + 1), 2));
 		for(MAssemblyContainer panel : containerList) {
 			p.add(panel.getUI());
 		}
 		
 		add(p);
 		
 		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 		setTitle("Majordomo Simulator");
 		setPreferredSize(new Dimension(1000, (int)Math.floor((containerList.size() / 2) + 1) * 150));
 		pack();
 		
 		config.buildAssemblyProcessors();
 		config.setupAssemblies();
 		config.initAssemblies();
 		
 		sim = new Simulation();
 		Thread t = new Thread(sim);
 		t.setDaemon(true);
 		t.start();
 	}
 	
 	public MAssemblyContainer createAssemblyContainer(long id, String name) {
 		if(!containers.containsKey(id)) {
 			MAssemblyContainer panel = new MAssemblyContainer(name);
 			containers.put(id, panel);
 			containerList.add(panel);
 		}
 		return containers.get(id);
 	}
 	
 	private class Simulation implements Runnable {
 		
 		public boolean running = false;
 		private MClockSensorAssembly clock;
 		private MTemperatureSensorAssembly outTemp;
 		private MLightSensorAssembly light;
 		
 		public Simulation() {
 			MAssemblyContainer c = containers.get(0l);
 			if(c != null) {
 				outTemp = c.getTemperatureAssembly();
 				clock = c.getClockAssembly();
 				light = c.getLightAssembly();
 				
 				outTemp.setValue(1.0);
 			} else {
 				System.err.println("Cannot find global TemperatureSensorAssembly.");
 			}
 		}
 
 		public void step() {
 			if(clock != null) {
 				clock.step();
 			}
 
 			if(clock != null && light != null) {
 				if(clock.readValue() < 7) light.setValue(0.0);
 				if(clock.readValue() >= 7 && clock.readValue() < 9) light.setValue((clock.readValue() - 7) / 2.0);
 				if(clock.readValue() >= 9 && clock.readValue() < 16) light.setValue(1.0);
 				if(clock.readValue() >= 16 && clock.readValue() < 18) light.setValue(1.0 - (clock.readValue() - 16) / 2.0);
 				if(clock.readValue() >= 18) light.setValue(0.0);
 			}
 			
 			if(outTemp != null && clock != null) {
				if(clock.readValue() >= 7.0 && clock.readValue() <= 14.30) outTemp.setValue(outTemp.readValue() + 0.3);
 				if(clock.readValue() >= 19.30 || clock.readValue() <= 3.00) outTemp.setValue(outTemp.readValue() - 0.3);
 			}
 			
 			for(MAssemblyContainer container : containerList) {
 				container.step(
 						outTemp == null ? 18.0 : outTemp.readValue(),
 						light == null ? 0.5 : light.readValue(),
 						clock == null ? 0.0 : clock.readValue());
 			}
 		}
 		
 		@Override
 		public void run() {
 			while(true) {
 				if(running) {
 					step();
 				}
 				
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 	}
 }
