 package be.betty.gwtp.server;
 
 import java.util.HashMap;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import com.gwtplatform.dispatch.server.actionhandler.ActionHandler;
 
 import be.betty.gwtp.client.action.GetActivityStateAction;
 import be.betty.gwtp.client.action.GetActivityStateActionResult;
 import be.betty.gwtp.server.bdd.Activity_entity;
 import be.betty.gwtp.server.bdd.ActivityState;
 import be.betty.gwtp.server.bdd.ProjectInstance;
 import be.betty.gwtp.shared.dto.ActivityState_dto;
 import be.betty.gwtp.shared.dto.Card_dto;
 
 import com.google.inject.Inject;
 import com.gwtplatform.dispatch.server.ExecutionContext;
 import com.gwtplatform.dispatch.shared.ActionException;
 
 public class GetActivityStateActionActionHandler implements
 		ActionHandler<GetActivityStateAction, GetActivityStateActionResult> {
 
 	@Inject
 	public GetActivityStateActionActionHandler() {
 	}
 
 	@Override
 	public GetActivityStateActionResult execute(GetActivityStateAction action,
 			ExecutionContext context) throws ActionException {
 		
 		if (!CheckSession.isProjectActionPermited(action.getInstanceBddId(), true, action.getSessId()))
 			throw new ActionException("Invalid Action, try to re-log");
 		
 		HashMap<String, ActivityState_dto> h = new HashMap<String, ActivityState_dto>();
 		
 		// le but c'est d'avoir l'activityState id le plus haut possible pour une activity donnee
 		HashMap<Integer,Integer> max = new HashMap<Integer, Integer>(); //used to make sure of the correct activityState's order
 		
 		Session s = HibernateUtils.getSession();
 		Transaction t = s.beginTransaction();
 		
 		int activityId;
 		ProjectInstance pi = (ProjectInstance) s.get(ProjectInstance.class, action.getInstanceBddId());

 		for (ActivityState as : pi.getActivitiesState()) {
 			activityId = as.getActivity().getId();
 			if ( max.containsKey(activityId) && max.get(activityId) >= as.getId()) continue;
 			max.put(activityId, as.getId());
			h.put(""+activityId, new ActivityState_dto(as.getDay(), as.getPeriod()));//, ""+as.getRoom().getId()));
 		}
 		
 		t.commit();   
 		s.close();
 		
 		
 		return new GetActivityStateActionResult(h);
 	}
 
 	@Override
 	public void undo(GetActivityStateAction action,
 			GetActivityStateActionResult result, ExecutionContext context)
 			throws ActionException {
 	}
 
 	@Override
 	public Class<GetActivityStateAction> getActionType() {
 		return GetActivityStateAction.class;
 	}
 }
