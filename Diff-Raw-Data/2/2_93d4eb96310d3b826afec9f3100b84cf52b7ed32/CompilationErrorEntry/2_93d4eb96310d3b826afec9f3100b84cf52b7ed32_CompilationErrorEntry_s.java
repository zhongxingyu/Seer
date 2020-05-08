 /**
  * 
  */
 package hu.e.compiler.internal.model;
 
 import hu.e.compiler.ECompilerException;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xtext.nodemodel.ICompositeNode;
 import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
 
 /**
  * @author balazs.grill
  *
  */
 public class CompilationErrorEntry implements IProgramStep {
 
 	public static IProgramStep create(ECompilerException e){
 		return new CompilationErrorEntry(IStatus.ERROR, e.getItem(), e.getMessage());
 	}
 	
 	public static IProgramStep error(EObject item, String msg){
 		return new CompilationErrorEntry(IStatus.ERROR, item, msg);
 	}
 	
 	public static IProgramStep warning(EObject item, String msg){
 		return new CompilationErrorEntry(IStatus.WARNING, item, msg);
 	}
 	
 	public static IProgramStep info(EObject item, String msg){
 		return new CompilationErrorEntry(IStatus.INFO, item, msg);
 	}
 	
 	private final int type;
 	private final EObject item;
 	private final String msg;
 	
 	public CompilationErrorEntry(int type, EObject item, String msg) {
 		this.type = type;
 		this.item = item;
 		this.msg = msg;
 	}
 	
 	public int getType() {
 		return type;
 	}
 	
 	public EObject getItem() {
 		return item;
 	}
 	
 	public String getMsg() {
 		return msg;
 	}
 	
 	public String getLocation(EObject element){
 		ICompositeNode cn = NodeModelUtils.findActualNodeFor(element);
 		Package p = null;
 		EObject eo = element;
 		while(!(eo instanceof Package) && (eo != null)){
 			eo = eo.eContainer();
 			if (eo instanceof Package)
 				p = (Package)eo;
 		}
 		if (cn != null && p != null){
 			return " at "+p.getName()+" line "+cn.getStartLine();
 		}
		return "";
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		switch(type){
 		case IStatus.OK:
 			sb.append("OK: ");
 			break;
 		case IStatus.INFO:
 			sb.append("INFO: ");
 			break;
 		case IStatus.WARNING:
 			sb.append("WARNING: ");
 			break;
 		case IStatus.ERROR:
 			sb.append("ERROR: ");
 			break;
 		}
 		sb.append(msg);
 		sb.append(getLocation(item));
 		
 		return sb.toString();
 	}
 	
 }
