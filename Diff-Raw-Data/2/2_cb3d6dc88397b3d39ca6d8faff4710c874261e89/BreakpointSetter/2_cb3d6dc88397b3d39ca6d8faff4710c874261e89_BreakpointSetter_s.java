 package pcpl.core.breakpoint;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.jdt.debug.core.JDIDebugModel;
 
 public class BreakpointSetter {
 	private static BreakpointSetter instance = null;
 	public static BreakpointSetter getInstance() {
 		if (instance == null) {
 			instance = new BreakpointSetter();
 		}
 		return instance;
 	}
 	
 	public BreakpointSetter(){
 
 	}
 	
 	public void setBreakpoint(IResource resource,int lineNum){
 		String typeName = FileParaviserUtils.getClassName(resource);	//javabreakpoint need this
 		try {
			IBreakpoint bp = JDIDebugModel.createLineBreakpoint(resource,typeName, lineNum, -1, -1, 0,true,null);
 			BreakpointManager.getInstance().addBreakpointSet(bp, resource);
 			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(bp);
 			System.out.print("setBreakpoint at "+typeName+":"+lineNum+"\n");
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
