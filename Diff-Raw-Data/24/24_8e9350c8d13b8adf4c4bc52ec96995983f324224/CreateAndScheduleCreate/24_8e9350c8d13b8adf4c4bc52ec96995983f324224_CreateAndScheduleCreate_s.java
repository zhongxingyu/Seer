 package infodoc.basic.activity;
 
 import infodoc.basic.BasicConstants;
 import infodoc.basic.scheduling.CreateActivityScheduler;
 import infodoc.core.dto.Activity;
 import infodoc.core.dto.PropertyValue;
 import infodoc.core.dto.User;
 
 import java.util.Collection;
 import java.util.List;
 
 public class CreateAndScheduleCreate extends Create {
 
 	private static final long serialVersionUID = 1L;
 	
 	protected Long scheduleActivityId;
 	
 	protected Long cronExpressionPropertyId;
 	
 	public CreateAndScheduleCreate() {
 		super();
 	}
 	
 	public CreateAndScheduleCreate(Activity activity, User user) {
 		super(activity, user);
 	}
 	
 	@Override
 	protected int parseOther(String[] params, int startPosition) {
 		if(params.length >= startPosition + 2 && "scheduleActivity".toLowerCase().equals(params[startPosition].toLowerCase().trim())) {
 			scheduleActivityId = Long.parseLong(params[startPosition + 1].trim());
 			return startPosition + 2;
 		}
 		
 		if(params.length >= startPosition + 2 && "cronExpressionProperty".toLowerCase().equals(params[startPosition].toLowerCase().trim())) {
 			cronExpressionPropertyId = Long.parseLong(params[startPosition + 1].trim());
 			return startPosition + 2;
 		}
 		
 		return super.parseOther(params, startPosition);
 	}
 	
 	@Override
 	public boolean afterSaveCase(List<PropertyValue> propertyValuesToSave) {
		CreateActivityScheduler.schedule(form.getCase().getId(), scheduleActivityId, getCronExpression(propertyValuesToSave));
 		return true;
 	}
 
 	public String getCronExpression(Collection<PropertyValue> propertyValues) {
 		String cronExpression = null;
 		
 		for(PropertyValue pv : propertyValues) {
 			if(cronExpressionPropertyId.equals(pv.getProperty().getId())) {
 				cronExpression = pv.getStringValue();
 				break;
 			}
 		}
 		
 		return cronExpression;
 	}
 
 	public Long getScheduleActivityId() {
 		return scheduleActivityId;
 	}
 
 	public Long getCronExpressionPropertyId() {
 		return cronExpressionPropertyId;
 	}
 	
 	@Override
 	public String getHelp() {
 		return BasicConstants.uiHelpCreateAndScheduleCreateActivity;
 	}
 
 }
