 package edu.wheaton.simulator.gui;
 
 import java.util.Set;
 
 import edu.wheaton.simulator.datastructure.AbstractGUIGridObserver;
 import edu.wheaton.simulator.datastructure.AgentAppearance;
 
 public class GridPanelObserver extends AbstractGUIGridObserver{
 
 	private GridPanel gp;
 	
 	public GridPanelObserver(GridPanel gp){
 		this.gp = gp;
 	}
 
 	@Override
 	public void update(Set<AgentAppearance> agents) {
		
 		gp.repaint();
 	}
 }
