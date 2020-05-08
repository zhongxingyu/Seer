 package org.amanzi.awe.catalog.neo.actions;
 
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
		// TODO Auto-generated method stub
		 NDS=new NeoDirSelector();
		 NDS.run();

 	}
 
 	@Override
 	public void dispose() 
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 }
