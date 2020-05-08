 package org.amanzi.awe.catalog.neo.actions;
 
import org.eclipse.swt.widgets.Display;

 import net.refractions.udig.project.ui.tool.AbstractActionTool;
 
 public class Neo4jActionTool extends AbstractActionTool 
 {
 	
 	NeoDirSelector NDS;
 	
 
 	public Neo4jActionTool() 
 	{
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void run() 
 	{
		final Display display = this.getContext().getViewportPane().getControl().getDisplay();
		this.getContext().updateUI(new Runnable(){
			@Override
			public void run() {
				 NDS=new NeoDirSelector();
				 NDS.run(display);
			}});
 	}
 
 	@Override
 	public void dispose() 
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 }
