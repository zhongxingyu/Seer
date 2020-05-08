 package crisis.ui;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import org.eclipse.core.runtime.IAdapterFactory;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.model.IWorkbenchAdapter;
 
 import crisis.api.model.CrisisGroup;
 import crisis.api.model.CrisisGroupId;
 import crisis.api.model.CrisisTxId;
 import crisis.application.CrisisManager;
 
 public class CrisisAdapterFactory implements IAdapterFactory {
 
 	// special group required for tree viewer
 	public static final CrisisGroupId GROUPS_ROOT_ID = new CrisisGroupId(-1);
 
 	public static final CrisisGroup GROUPS_ROOT = new CrisisGroup(
 			GROUPS_ROOT_ID,
 			"<NONE>",
 			null,
 			"",
 			new LinkedList<CrisisGroupId>(),
 			new ArrayList<CrisisTxId>());
 
 
 	private IWorkbenchAdapter itemGroupAdapter = new IWorkbenchAdapter() {
 
 		
 		@Override
 		public Object[] getChildren(Object o) {
 
 			if (o.getClass() == CrisisGroup.class) {
 			
 				CrisisGroup group = (CrisisGroup) o;
 				
				if(group.getGroupId().equals(GROUPS_ROOT_ID)) {
 					return CrisisManager.getManager().getTopLevelGroups().toArray();
 				} else {
 					return CrisisManager.getManager().getGroupsById(group.getChildGroupIdList()).toArray();
 				}
 			}
 			
 			return null;
 		}
 
 		
 		@Override
 		public ImageDescriptor getImageDescriptor(Object object) {
 
 			return null;
 		}
 
 		
 		@Override
 		public String getLabel(Object o) {
 
 			if (o.getClass() == CrisisGroup.class) {
 			
 				CrisisGroup group = (CrisisGroup) o; 
 			
 				return group.getGroupName();
 			}
 			
 			return null;
 		}
 
 		
 		@Override
 		public Object getParent(Object o) {
 
 			if (o.getClass() == CrisisGroup.class) {
 
 				CrisisGroup group = (CrisisGroup) o;
 				
 				if(group.getParentGroupId() == null &&  !group.getGroupId().equals(GROUPS_ROOT_ID)) {
 					
 					return GROUPS_ROOT;
 				} else {
 					
 					return group.getParentGroupId();
 				}
 			}
 			
 			return null;
 		}
 	};
 
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Object getAdapter(Object adaptableObject, Class adapterType) {
 
 		if (adapterType == IWorkbenchAdapter.class
 				&& adaptableObject instanceof CrisisGroup) {
 			return itemGroupAdapter;
 		}
 		return null;
 	}
 
 	
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Class[] getAdapterList() {
 
 		return new Class[] { IWorkbenchAdapter.class };
 	}
 
 }
