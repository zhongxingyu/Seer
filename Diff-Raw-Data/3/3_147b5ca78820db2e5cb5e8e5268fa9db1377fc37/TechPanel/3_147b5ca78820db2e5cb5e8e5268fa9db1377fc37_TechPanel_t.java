 package linewars.display.panels;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import linewars.display.Animation;
 import linewars.display.Display;
 import linewars.gameLogic.GameStateProvider;
 import linewars.gamestate.tech.CycleException;
 import linewars.gamestate.tech.TechConfiguration;
 import linewars.gamestate.tech.TechGraph;
 import linewars.gamestate.tech.TechGraph.TechNode;
 import linewars.gamestate.tech.UnlockStrategy;
 import linewars.gamestate.tech.UnlockStrategyAll;
 import linewars.gamestate.tech.UnlockStrategyNoSyblings;
 import linewars.gamestate.tech.UnlockStrategyOne;
 import linewars.network.MessageReceiver;
 import configuration.Configuration;
 import editor.BigFrameworkGuy;
 import editor.GenericSelector;
 import editor.GenericSelector.GenericListCallback;
 import editor.GenericSelector.SelectionChangeListener;
 
 @SuppressWarnings("serial")
 public class TechPanel extends Panel
 {
 	private static final double ASPECT_RATIO = 0.75;
 
 	private static final int DEFAULT_WIDTH = 831;
 	static final int DEFAULT_HEIGHT = 491;
 	
 	private static final int TAB_PANEL_X = 84;
 	private static final int TAB_PANEL_Y = 17;
 	
 	private static final int TAB_PANEL_WIDTH = 661;
 	private static final int TAB_PANEL_HEIGHT = 30;
 	
 	private static final int TECH_PANEL_X = 20;
 	private static final int TECH_PANEL_Y = 65;
 	
 	private static final int TECH_PANEL_WIDTH = 789;
 	private static final int TECH_PANEL_HEIGHT = 393;
 	
 	private static final int EDITOR_PANEL_HEIGHT = 30;
 	
 	private Display display;
 	private BigFrameworkGuy bfg;
 	
 	private GridBagLayout techLayout;
 	private GridBagConstraints c;
 	
 	private JPanel tabPanel;
 	private JPanel techPanel;
 	private JPanel editorComponents;
 	
 	private ArrayList<JButton> tabs;
 	private ArrayList<TechDisplay> techs;
 	
 	private TechDisplay activeTech;
 	
 	private JCheckBox enabledBox;
 	private GenericSelector<Configuration> techSelector;
 	private GenericSelector<UnlockStrategy> unlockStrategySelector;
 	
 	private boolean displayed;
 	
 	/**
 	 * Constructs the TechPanel for the editors, allows all elements to be edited.
 	 * @param bfg The BigFrameworkGuy that contains this panel.
 	 */
 	public TechPanel(BigFrameworkGuy bfg)
 	{
 		super(null, DEFAULT_WIDTH, DEFAULT_HEIGHT);
 		
 		this.bfg = bfg;
 
 		tabs = new ArrayList<JButton>();
 		techs = new ArrayList<TechDisplay>();
 		
 		initialize();
 		
 		addEditorElements();
 		
 		addComponentListener(new ResizeListener());
 	}
 
 	/**
 	 * Constructs the TechPanel for use in the game, does not allow editing, displays the state of techs, and researches them.
 	 * @param display The Display that contains this panel.
 	 * @param stateManager The GameStateProvider for the current session of the game.
 	 * @param pID The ID of the player this TechPanel is displayed for.
 	 */
 	public TechPanel(Display display, GameStateProvider stateManager, int pID, MessageReceiver receiver, Animation... anims)
 	{
 		super(stateManager, DEFAULT_WIDTH, DEFAULT_HEIGHT, anims[0]);
 		
 		this.display = display;
 		this.displayed = false;
 		
 		tabs = new ArrayList<JButton>();
 		techs = new ArrayList<TechDisplay>();
 		
 		List<TechGraph> graphs = stateManager.getCurrentGameState().getPlayer(pID).getRace().getAllTechGraphs();
 		
 		for(int i = 0; i < graphs.size(); ++i)
 		{
 			TechGraph graph = graphs.get(i);
 			tabs.add(new JButton(graph.getName()));
 			techs.add(new TechDisplay(stateManager, pID, receiver, this, graph, i, anims[1]));
 		}
 		
 		initialize();
 	}
 	
 	public void setAllTechGraphs(List<TechGraph> graphs)
 	{
 		for(JButton tab : tabs)
 			tabPanel.remove(tab);
 		for(TechDisplay disp : techs)
 			techPanel.remove(disp);
 		
 		for(TechGraph graph : graphs)
 		{
 			TechDisplay tech = new TechDisplay(this, graph);
 			tech.setTechSelector(techSelector);
 			tech.setUnlockStrategySelector(unlockStrategySelector);
 			
 			activeTech = tech;
 			techs.add(tech);
 			techPanel.add(tech);
 			techLayout.addLayoutComponent(tech, c);
 			
 			JButton tab = new JButton(graph.getName());
 			tab.addActionListener(new TabButtonHandler(tech));
 			tabs.add(tab);
 			tabPanel.add(tab);
 		}
 		
 		TechPanel.this.validate();
 	}
 	
 	public void setEnabledTechGraphs(List<TechGraph> graphs)
 	{
 		for(TechDisplay disp : techs)
 			disp.getTechGraph().setEnabled(false);
 		
 		for(TechGraph graph : graphs)
 			graph.setEnabled(true);
 	}
 	
 	public List<TechGraph> getAllTechGraphs()
 	{
 		ArrayList<TechGraph> graphs = new ArrayList<TechGraph>();
 		
 		for(TechDisplay disp : techs)
 		{
 			graphs.add(disp.getTechGraph());
 		}
 		
 		return graphs;
 	}
 	
 	public List<TechGraph> getEnabledTechGraphs()
 	{
 		ArrayList<TechGraph> graphs = new ArrayList<TechGraph>();
 		
 		for(TechDisplay disp : techs)
 		{
 			if(disp.getTechGraph().isEnabled())
 			{
 				graphs.add(disp.getTechGraph());
 			}
 		}
 		
 		return graphs;
 	}
 	
 	public void resetTechGraphs()
 	{
 		setAllTechGraphs(new ArrayList<TechGraph>());
 		setEnabledTechGraphs(new ArrayList<TechGraph>());
 	}
 	
 	private void initialize()
 	{
 		techLayout = new GridBagLayout();
 		c = new GridBagConstraints();
 		techPanel = new JPanel(techLayout);
 		techPanel.setOpaque(false);
 		for(int i = 0; i < techs.size(); ++i)
 		{
 			techs.get(i).setVisible(false);
 			techPanel.add(techs.get(i));
 			techLayout.addLayoutComponent(techs.get(i), c);
 		}
 		
 		tabPanel = new JPanel(new GridLayout());
 		tabPanel.setOpaque(false);
 		for(int i = 0; i < tabs.size(); ++i)
 		{
 			tabs.get(i).addActionListener(new TabButtonHandler(techs.get(i)));
 			tabPanel.add(tabs.get(i));
 		}
 		
 		add(tabPanel);
 		add(techPanel);
 		
 		if(!techs.isEmpty())
 			techs.get(0).setVisible(true);
 	}
 	
 	private void addEditorElements()
 	{
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
 		editorComponents = new JPanel();
 		editorComponents.setLayout(new BoxLayout(editorComponents, BoxLayout.X_AXIS));
 		add(editorComponents);
 		
 		JButton addTechGraph = new JButton("Add Tech Graph");
 		addTechGraph.addActionListener(new AddTechGraphHandler());
 		editorComponents.add(addTechGraph);
 		
 		enabledBox = new JCheckBox("Enabled");
 		enabledBox.addItemListener(new EnabledBoxListener());
 		editorComponents.add(enabledBox);
 		
 		techSelector = new GenericSelector<Configuration>("Tech", new TechListCallback(), new GenericSelector.ShowBFGName<Configuration>());
 		techSelector.addSelectionChangeListener(new TechSelectionListener());
 		editorComponents.add(techSelector);
 		
 		unlockStrategySelector = new GenericSelector<UnlockStrategy>("Unlock Strategy", new UnlockStrategyListCallback());
 		unlockStrategySelector.addSelectionChangeListener(new UnlockStrategySelectionListener());
 		editorComponents.add(unlockStrategySelector);
 	}
 
 	boolean isDisplayed()
 	{
 		return displayed;
 	}
 	
 	void toggleDisplayed()
 	{
 		displayed = !displayed;
 	}
 	
 	private void setAllTechGraphsInvisible()
 	{
 		for(int i = 0; i < techs.size(); ++i)
 		{
 			techs.get(i).setVisible(false);
 		}
 		
 		activeTech.setVisible(true);
 	}
 	
 	@Override
 	public void updateLocation()
 	{
 		scaleFactor = (display.getScreenWidth() * ASPECT_RATIO) / DEFAULT_WIDTH;
 
 		super.updateLocation();
 		
 		if(isDisplayed())
 			setLocation((getParent().getWidth() / 2) - (getWidth() / 2), 0);
 		else
 			setLocation((getParent().getWidth() / 2) - (getWidth() / 2), (int)(scaleFactor * -DEFAULT_HEIGHT));
 		
 		//resize inner panels
 		tabPanel.setSize((int)(TAB_PANEL_WIDTH * scaleFactor), (int)(TAB_PANEL_HEIGHT * scaleFactor));
 		tabPanel.setLocation((int)(TAB_PANEL_X * scaleFactor), (int)(TAB_PANEL_Y * scaleFactor));
 
 		techPanel.setSize((int)(TECH_PANEL_WIDTH * scaleFactor), (int)(TECH_PANEL_HEIGHT * scaleFactor));
 		techPanel.setLocation((int)(TECH_PANEL_X * scaleFactor), (int)(TECH_PANEL_Y * scaleFactor));
 		
 		if(bfg != null)
 		{
 			editorComponents.setSize(getWidth(), EDITOR_PANEL_HEIGHT);
 			editorComponents.setLocation(0, getHeight() - EDITOR_PANEL_HEIGHT);
 		}
 	}
 	
 	@Override
 	public void paint(Graphics g)
 	{
 		if(bfg != null)
 		{
 			g.fillRect(0, 0, getWidth(), getHeight());
 			g.setColor(Color.green);
 //			if(techPanelAnim != null)
 //			{
 //				scaleFactor = (getWidth() * ASPECT_RATIO) / DEFAULT_WIDTH;
 //				ImageDrawer.getInstance().draw(g, techPanelAnim.getImage(0),
 //						DEFAULT_WIDTH, DEFAULT_HEIGHT,
 //						new Position(0, 0), scaleFactor);
 //			}
 		}
 		
 		super.paint(g);
 	}
 	
 	public Dimension getMaxTechDisplaySize()
 	{
 		int width = getWidth();
 		int height = getHeight();
 		
		if(tabPanel != null)
			height -= tabPanel.getHeight();
 		
 		if(editorComponents != null)
 			height -= editorComponents.getHeight();
 		
 		return new Dimension(width, height);
 	}
 	
 	private class ResizeListener extends ComponentAdapter
 	{
 		@Override
 		public void componentResized(ComponentEvent e)
 		{
 			Dimension MaxTechDisplaySize = getMaxTechDisplaySize();
 			
 			for(TechDisplay td : techs)
 			{
 				td.setMaximumSize(MaxTechDisplaySize);
 			}
 			
 			validate();
 			repaint();
 		}
 	}
 	
 	private class TabButtonHandler implements ActionListener
 	{
 		private TechDisplay tech;
 		
 		public TabButtonHandler(TechDisplay tech)
 		{
 			this.tech = tech;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent arg0)
 		{
 			activeTech = tech;
 			setAllTechGraphsInvisible();
 
 			if(bfg != null)
 			{
 				TechNode activeTechNode = activeTech.getActiveTech();
 				if(activeTechNode != null)
 				{
 					enabledBox.setSelected(activeTech.getTechGraph().isEnabled());
 					techSelector.setSelectedObject(activeTechNode.getTechConfig());
 					unlockStrategySelector.setSelectedObject(activeTechNode.getUnlockStrategy());
 				}
 			}
 		}	
 	}
 	
 	private class AddTechGraphHandler implements ActionListener
 	{
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			String s = (String)JOptionPane.showInputDialog(
 					getParent(),
                     "Name the new tech graph",
                     "Tech Graph",
                     JOptionPane.PLAIN_MESSAGE,
                     null,
                     null,
                     "Tech");
 			
 			if(s == null)
 				return;
 			
 			s.trim();
 			if(s.equals(""))
 				return;
 			
 			TechDisplay tech = new TechDisplay(TechPanel.this, new TechGraph(s));
 			tech.setTechSelector(techSelector);
 			tech.setUnlockStrategySelector(unlockStrategySelector);
 			
 			activeTech = tech;
 			techs.add(tech);
 			techPanel.add(tech);
 			techLayout.addLayoutComponent(tech, c);
 			
 			JButton tab = new JButton(s);
 			tab.addActionListener(new TabButtonHandler(tech));
 			tabs.add(tab);
 			tabPanel.add(tab);
 			
 			TechPanel.this.validate();
 		}
 	}
 	
 	private class EnabledBoxListener implements ItemListener
 	{
 		@Override
 		public void itemStateChanged(ItemEvent e)
 		{
 			activeTech.getTechGraph().setEnabled(e.getStateChange() == ItemEvent.SELECTED);
 		}
 	}
 	
 	private class TechListCallback implements GenericListCallback<Configuration>
 	{
 		@Override
 		public List<Configuration> getSelectionList()
 		{
 			return new ArrayList<Configuration>(bfg.getConfigurationsByType(BigFrameworkGuy.ConfigType.tech));
 		}
 	}
 	
 	private class TechSelectionListener implements SelectionChangeListener<Configuration>
 	{
 		@Override
 		public void selectionChanged(Configuration newSelection)
 		{
 			activeTech.getActiveTech().setTech((TechConfiguration)newSelection);
 		}
 	}
 	
 	private class UnlockStrategyListCallback implements GenericListCallback<UnlockStrategy>
 	{
 		@Override
 		public List<UnlockStrategy> getSelectionList()
 		{
 			ArrayList<UnlockStrategy> stratList = new ArrayList<UnlockStrategy>();
 			stratList.add(new UnlockStrategyAll());
 			stratList.add(new UnlockStrategyOne());
 			stratList.add(new UnlockStrategyNoSyblings());
 			
 			return stratList;
 		}	
 	}
 	
 	private class UnlockStrategySelectionListener implements SelectionChangeListener<UnlockStrategy>
 	{
 		@Override
 		public void selectionChanged(UnlockStrategy newSelection)
 		{
 			activeTech.getActiveTech().setUnlockStrategy(newSelection);
 		}
 	}
 }
