 package P2PAgent;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Panel;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JSlider;
 import javax.swing.SwingConstants;
 import javax.swing.WindowConstants;
 
 import graphics.ControllableView;
 import graphics.UpdateListener;
 import graphics.ViewerFactory.Type;
 import graphics.ViewerFactory.WindowLayout;
 import graphics.ViewerFactory.WindowLayout.Row;
 import graphics.ViewerFactory.WindowParameters;
 import graphics.ViewerFactoryP2P;
 import logging.Log;
 import scenario.AbstractScenario;
 import scenario.P2PScenario;
 import agent.AgentID;
 import base.Environment;
 import base.Simulation;
 
 
 public class SimulationP2P extends Simulation<EnvironmentP2P, CommandP2P>
 {
 	/**
 	 * 
 	 */
 	private static final long	serialVersionUID	= 1L;
 	private static SimulationP2P p2p;
 	P2PScenario scenario = new P2PScenario("scenarios/p2pScenarioNormal.xml");
 	
 	private ControllableView<EnvironmentP2P>[]		viewers		= null;
 	private static StepNumber		sn			= new StepNumber();
 	private static JSlider			sw			= new JSlider(SwingConstants.HORIZONTAL, 0, 200, 0);
 	
 	private static class StepNumber extends JLabel implements UpdateListener
 	{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public StepNumber()
 		{
 			super("   ---   ");
 			setForeground(Color.black);
 		}
 		
 		@Override
 		public void update()
 		{
 			setText("   step " + Environment.getStep() + "   ");
 		}
 	}
 	
 	public static void main(String[] args)
 	{
 		setP2P(new SimulationP2P());
 	}
 	
 	
 	public SimulationP2P()
 	{
 		commands = scenario.getCommands();
 		init1();
		WindowLayout layout = new WindowLayout(0, 0, 1000, 600, 60, 1, 5, true, true); 
 		Row row = null;
		row = layout.addRow(Type.DOMAIN_INTEREST_GRID, 4);
 		
 		row.add(new WindowParameters(Type.AGENT_SELECTION_GRID));
 		row.add(new WindowParameters(Type.ITEM_GRAF));
 		row.add(new WindowParameters(Type.ITEM_LOCATION_GRAF));
 		row.add(new WindowParameters(Type.ITEM_WANTED_GRAF));
 		layout.addMain(new WindowParameters(Type.CONTROL, -1, -1, this));
 		layout.addMain(new WindowParameters(Type.LOG_VIEWER, -1, -1, 0, 0));
 		
 		// layout.addMain(new WindowParameters(Type.AGENT_DETAILS, -1, -1, 0, 0));
 		
 		viewers = ViewerFactoryP2P.createViewers(environment, layout.toCollection());
 		init2();
 	}
 	
 	private void init1()
 	{
 		// TODO Auto-generated method stub
 		environment= new EnvironmentP2P(this, scenario);
 		environment.getLogger().setLevel(LEVEL);
 		log = new Log(null);
 		environment.getLogger().addLog(log);
 		nextcommand = 0;
 		//to initialize the variable item and itemWanted in P2PAgent
 		int step= Environment.getStep();
 		//System.out.println((step));
 		while(nextcommand < commands.length && commands[nextcommand].getTime() == step)
 		{
 			doCommand(commands[nextcommand++]);
 		}
 		nextcommand = 0;
 	}
 	
 	private void init2()
 	{
 		environment.addUpdateListener(this);
 		environment.addUpdateListener(sn);
 		
 		for(ControllableView<EnvironmentP2P> viewer : viewers)
 			if(viewer != null)
 				viewer.relink(environment);
 		environment.doUpdate();
 	}
 
 	public static void setP2P(SimulationP2P p2p)
 	{
 		SimulationP2P.p2p=p2p;
 	}
 	public static SimulationP2P getP2P()
 	{
 		return SimulationP2P.p2p;
 		
 	}
 
 	@Override
 	public void run()
 	{
 		// TODO Auto-generated method stub
 		active=true;//don't forget to change it after
 		
 		int step = Environment.getStep();
 
 		while(step < scenario.getNsteps() && active)
 		{
 			step = Environment.getStep();
 			if(step == LEVELSWITCH)
 				environment.getLogger().setLevel(LEVELTO);
 			
 			if(step % PRINTSTEP == 0)
 			{
 				log.le("===================== STEP ~ ======================", new Integer(step));
 			}
 			else
 			{
 				log.li("===================== STEP ~ ======================", new Integer(step));
 			}
 			while(nextcommand < commands.length && commands[nextcommand].getTime() == step)
 			{
 				doCommand(commands[nextcommand++]);
 			}
 			try
 			{
 				environment.step();
 			} catch (Exception e1)
 			{
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			try
 			{
 				Thread.sleep(sw.getValue());
 			} catch(InterruptedException e)
 			{
 				e.printStackTrace();
 			}
 			if(oneStep)
 			{
 				oneStep = false;
 				break;
 			}
 		}
 		active = false;
 	}
 
 
 	@Override
 	public void update()
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void createMainWindow(int x, int y, int w, int h)
 	{
 		// TODO Auto-generated method stub
 		this.setLayout(new BorderLayout());
 		
 		Panel box = new Panel();
 		box.setLayout(new BoxLayout(box, BoxLayout.LINE_AXIS));
 		box.setBackground(Color.white);
 		JButton start = new JButton("Start");
 		start.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				start();
 			}
 		});
 		box.add(start);
 		JButton stop = new JButton("Stop");
 		stop.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				stop();
 			}
 		});
 		box.add(stop);
 		JButton step = new JButton("Step");
 		step.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				step();
 			}
 		});
 		box.add(step);
 		// JButton reset = new JButton("Reset !");
 		// reset.addActionListener(new ActionListener() {
 		// @Override
 		// public void actionPerformed(ActionEvent e)
 		// {
 		// init1();
 		// init2();
 		// }
 		// });
 		// box.add(reset);
 		
 		box.add(sn);
 		
 		final JButton randomize = new JButton("Randomize " + AbstractScenario.getSeed());
 		randomize.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				AbstractScenario.resetRandom();
 				randomize.setText("Randomize " + AbstractScenario.getSeed());
 			}
 		});
 		box.add(randomize);
 		JButton save = new JButton("Save");
 		save.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				JFileChooser fc = new JFileChooser(".");
 				
 				if(fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
 				{
 					scenario.save(fc.getSelectedFile());
 				}
 			}
 		});
 		box.add(save);
 		this.add(box, BorderLayout.CENTER);
 		
 		box = new Panel();
 		box.add(sw);
 		this.add(box, BorderLayout.SOUTH);
 		
 		this.setTitle("KCA");
 		this.setLocation(x, y);
 		this.setSize(w, h);
 		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 		this.setVisible(true);
 		
 	}
 
 	@Override
 	protected void doCommand(CommandP2P command)
 	{
 		// TODO Auto-generated method stub
 		if(command.getAction() == CommandP2P.Action.INJECT_ITEM)
 		{
 			log.le("injecting item owned ~", command.getItem().getItemID());
 			AgentID receiver = environment.injectItem(command.getAgentID(), command.getItem());
 			log.le("received by agent ~", receiver.name);
 		}
 		else if(command.getAction() == CommandP2P.Action.INJECT_ITEM_WANTED)
 		{
 			log.le("injecting item wanted ~", command.getItem().getItemID());
 			AgentID receiver = environment.injectItemWanted(command.getAgentID(), command.getItem());
 			log.le("received by agent ~", receiver.name);
 		}
 	}
 }
 
