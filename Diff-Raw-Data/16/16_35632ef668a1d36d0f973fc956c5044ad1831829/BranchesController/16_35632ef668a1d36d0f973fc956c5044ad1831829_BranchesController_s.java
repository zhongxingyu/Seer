 package org.vamdc.portal.session.queryBuilder.branches;
 
 import java.util.EnumSet;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.vamdc.dictionary.Requestable;
 import org.vamdc.dictionary.RequestableLogic;
 import org.vamdc.dictionary.RequestableLogicImpl;
 import org.vamdc.portal.session.queryBuilder.QueryData;
 
 
 /**
  * Class implementing branches controller
  *
  */
 @Name("branchesController")
 @Scope(ScopeType.PAGE)
 public class BranchesController {
 	
 
 	@In QueryData queryData;
 	private RequestableLogic logic = new RequestableLogicImpl(); 
 	
 	public boolean isOpen(){
 		return true;
 	}
 	
 	public boolean isActive(String node){
 		Boolean result=true;
 		if (queryData.getRequest()==null)
 			return false;
 		try{
 			Requestable key = Requestable.valueOfIgnoreCase(node);
 			result= logic.isEnabled(queryData.getRequest(), key);
 		}catch(IllegalArgumentException e){
 		}
 		return result;
 	}
 	
 	public void enable(Object node){
 		if (queryData.getRequest()==null)
 			queryData.setRequest(EnumSet.of((Requestable)node));
 		queryData.setRequest(logic.enableKey(queryData.getRequest(), (Requestable) node));
 	}
 	
 	public void disable(Object node){
 		if (queryData.getRequest()==null)
 			queryData.setRequest(EnumSet.of((Requestable)node));
 		queryData.setRequest(logic.disableKey(queryData.getRequest(), (Requestable) node));
 	}
 	
 	public void enableAll(){
		queryData.setRequest(logic.normalizeKeys(EnumSet.allOf(Requestable.class)));
 	}
 	public void disableAll(){
 		queryData.setRequest(null);
 	}
 }
