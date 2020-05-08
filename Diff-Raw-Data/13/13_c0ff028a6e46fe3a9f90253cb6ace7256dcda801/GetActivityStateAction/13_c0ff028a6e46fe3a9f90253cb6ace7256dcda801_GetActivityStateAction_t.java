 package be.betty.gwtp.client.action;
 
 import com.gwtplatform.dispatch.shared.ActionImpl;
 
 import be.betty.gwtp.client.action.GetActivityStateActionResult;
 
 public class GetActivityStateAction extends
 		ActionImpl<GetActivityStateActionResult> {
 
 	private int instanceBddId;
 
 	@SuppressWarnings("unused")
 	private GetActivityStateAction() {
 		// For serialization only
 	}
 	
	@Override public boolean isSecured() {
 		return false;
 	}
 
 	public GetActivityStateAction(int instanceBddId) {
 		this.instanceBddId = instanceBddId;
 	}
 
 	public int getInstanceBddId() {
 		return instanceBddId;
 	}
 }
