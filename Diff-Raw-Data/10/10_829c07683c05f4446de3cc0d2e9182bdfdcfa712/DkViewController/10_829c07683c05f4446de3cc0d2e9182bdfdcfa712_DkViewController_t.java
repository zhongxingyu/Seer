 package com.dougkoellmer.client.view;
 
 import swarm.client.app.ClientAppConfig;
 import swarm.client.states.Action_Base_HideSupplementState;
 import swarm.client.view.ViewConfig;
 import swarm.client.view.ViewController;
 import swarm.client.view.ViewContext;
 import swarm.client.view.cell.VisualCellContainer;
 import swarm.client.view.cell.VisualCellHud;
 import swarm.shared.statemachine.A_Action;
 
 public class DkViewController extends ViewController
 {
 	public DkViewController(ViewContext viewContext, ViewConfig config, ClientAppConfig appConfig)
 	{
 		super(viewContext, config, appConfig);
 	}
 
 	@Override
 	protected void startUpCoreUI()
 	{
 		m_viewContext.stateContext.performAction(Action_Base_HideSupplementState.class);
 		
 		super.startUpCoreUI();
 		
 		VisualCellContainer cellContainer = m_viewContext.splitPanel.getCellContainer();
 		VisualCellHud cellHud = new VisualCellHud(m_viewContext, m_appConfig);
 		this.addStateListener(cellHud);
 		cellContainer.getScrollContainer().add(cellHud);
 		
 		//cellContainer.getCellContainerInner().getElement().getStyle().setBackgroundColor("#000000");
 	}
 }
