 package ch.erebetez.activititestapp4.dataobjects;
 
 import java.util.Date;
 import java.util.Map;
 
 import org.activiti.engine.RuntimeService;
 import org.activiti.engine.task.Task;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 @Configurable(preConstruction = true)
 public class InventoryItemDefinition implements InventoryItem {
 
 	private Task task;
 
 	@Autowired
 	private RuntimeService runtimeservice;
 
 	private String getVariable(String key) {
		@SuppressWarnings("unchecked")		
 		Map<String, String> itemData = (Map<String, String>) runtimeservice
 				.getVariable(task.getExecutionId(), "itemData");
		
		if(itemData  == null){
			return null;
		}
		
 		return itemData.get(key);
 	}
 
 	public InventoryItemDefinition(Task task) {
 		this.task = task;
 	}
 
 	@Override
 	public void setTask(Task task) {
 		this.task = task;
 	}
 
 	@Override
 	public String getLocation() {
 		return getVariable(LOCATION);
 	}
 
 	@Override
 	public String getLot() {
 		return getVariable(LOT);
 	}
 
 	@Override
 	public String getId() {
 		return task.getId();
 	}
 
 	@Override
 	public String getName() {
 		return task.getName();
 	}
 
 	@Override
 	public String getDescription() {
 		return task.getDescription();
 	}
 
 	@Override
 	public int getPriority() {
 		return task.getPriority();
 	}
 
 	@Override
 	public Date getDueDate() {
 		return task.getDueDate();
 	}
 
 	@Override
 	public Date getCreateTime() {
 		return task.getCreateTime();
 	}
 
 }
