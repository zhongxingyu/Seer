 package edu.wustl.cab2b.client.ui;
 
 import org.jdesktop.swingx.JXPanel;
 
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.query.IClientQueryBuilderInterface;
 import edu.wustl.common.querysuite.queryobject.IExpressionId;
 
 //TODO : This needs to be replaced such that these methods got into a searchPanel, rather than remain in an interface
 public abstract class ContentPanel extends Cab2bPanel 
 {
 	
 	/*
 	 * Given the flow of events, the method will be called when the
 	 * dynamically generated UI is loaded into an instance of the
 	 * AddLimitPanel. Thus we need to be able to get a reference to that
 	 * instance and be able to set the query instance in the main DAG Panel.
 	 */
 	public abstract void setQueryObject(IClientQueryBuilderInterface query);
 	
 	public abstract void refresh(JXPanel[] arrPanel,String strClassName);    
     
	public abstract void setSearchResultPanel(SearchResultPanel searchResultPanel);	
     
     public abstract void setSearchPanel(SearchPanel panel);
     
     public abstract SearchPanel getSearchPanel();
 
 }
