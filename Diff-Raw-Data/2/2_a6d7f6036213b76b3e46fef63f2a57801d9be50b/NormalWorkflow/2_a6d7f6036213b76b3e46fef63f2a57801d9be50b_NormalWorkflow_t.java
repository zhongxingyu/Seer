 package org.cishell.reference.gui.workflow.model;
 
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.cishell.app.service.datamanager.DataManagerService;
 import org.cishell.framework.data.Data;
 import org.cishell.reference.gui.workflow.Activator;
 import org.osgi.framework.BundleContext;
 
 
 public class NormalWorkflow implements Workflow {
 	private String name;
 	private Long id;
 	private Long lastCreatedID;
 	private LinkedHashMap<Long, WorkflowItem> itemMap ;
 
 	public LinkedHashMap<Long, WorkflowItem> getMap() {
 		return itemMap;
 	}
 
 	public void setMap(LinkedHashMap<Long, WorkflowItem> map) {
 		this.itemMap = map;
 	}
 
 	public  NormalWorkflow(String name, Long id)
 	{
 		this.name = name;
 		this.id = id;
 		itemMap = new LinkedHashMap<Long, WorkflowItem> ();
 		lastCreatedID = new Long(1);
 	}
 	
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public Long getInternalId() {
 		return id;
 	}
 	
 	@Override
 	public void setInternalId(Long id) {
      this.id = id;
    }
 	
 	@Override
 	public void run() {
 		System.out.println("Running workflow");
 		BundleContext bundleContext = Activator.getContext();
 		DataManagerService dataManager = (DataManagerService)
 				bundleContext.getService(
 						bundleContext.getServiceReference(
 								DataManagerService.class.getName()));
 		System.out.println("data manager for workflow");
 	  Data[] data = dataManager.getSelectedData();
 		System.out.println("data manager is not null"+ "map size="+itemMap.size());
 
 	 // if(data[0]!= null)
 	   // System.out.println(data[0].getMetadata().get(DataProperty.LABEL));
 
 		for(Map.Entry<Long, WorkflowItem> entry: itemMap.entrySet())
 		{
 			System.out.println("Inside for loop for ");
 			WorkflowItem item = entry.getValue();
 			if(item instanceof AlgorithmWorkflowItem)
 			{
 				AlgorithmWorkflowItem algo = (AlgorithmWorkflowItem)item;				
 				algo.setInputData(data);
 				System.out.println("Running Algorithm" + algo.getName());
 				data = (Data[])algo.run();		
 				System.out.println("Completed Running Algorithm" + algo.getName());
 
 			}			
 		}
		if (data != null && data.length != 0) {
 			for (int ii = 0; ii < data.length; ii++) {
 				dataManager.addData(data[ii]);
 			}
 			dataManager.setSelectedData(data);
 
 		}
 	}
 
 	@Override
 	public void add(WorkflowItem item) {
 
         itemMap.put(item.getIternalId(), item);		
 	}
 
 
     public Long getUniqueInternalId()
 	 {
 		  while(itemMap.containsKey(lastCreatedID))		  
 			   lastCreatedID++;
 				  
 		return lastCreatedID;
 
 	}
 
 
 	@Override
 	public void remove(WorkflowItem item) {
 		try{
 		boolean flag=false;
 		
 		Set set =itemMap.entrySet();
 		Iterator i = set.iterator();
 
 		while(i.hasNext()){
 			Map.Entry me = (Map.Entry) i.next();
 			if(me.getKey()==item.getIternalId()){
 				flag = true;
 			}
 			if(flag==true){
 				System.out.println("Removed Algorithm: Key: " + me.getKey() + " Value: "+ me.getValue());
 				System.out.println("Before :" +itemMap.size());
 				itemMap.remove(me.getKey());
 				System.out.println("After :" + itemMap.size());
 			}		
 		}
 		}catch (Exception e){
 			System.out.println("Error in Removing Algorithm");
 			e.printStackTrace();
 		}	
 	}
 
 	@Override
 	public void setName(String name) {
         this.name= name;		
 	}	
 }
