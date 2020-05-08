 package org.eclipse.uml2.diagram.common.async;
 
 import org.eclipse.gmf.runtime.notation.CanonicalStyle;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.NotationFactory;
 import org.eclipse.gmf.runtime.notation.NotationPackage;
 import org.eclipse.gmf.runtime.notation.View;
 
 public class CanonicalHelper implements ICanonicalHelper {
 	private CanonicalStyle getStyle(View view){
 		return (CanonicalStyle) view.getStyle(NotationPackage.eINSTANCE.getCanonicalStyle());
 	}
 
 	public boolean shouldSyncNodes(View view) {
 		CanonicalStyle style = getStyle(view);
 		if (style == null){
 			//in GMF this means "synchronized by default"
 			return true;
 		}
 //		if (style instanceof U2TDiagramCanonicalStyle){
 //			return ((U2TDiagramCanonicalStyle)style).isSyncNodes();
 //		}
 		return style.isCanonical();
 	}
 	
 	public boolean isAutoSynchronized(View view) {
 		return shouldSyncNodes(view);
 	}
 	
 	public void setAutoSynchronized(View target, boolean isAutoSync) {
 		CanonicalStyle style = ensureHasCanonicalStyle(target);
 //		if (target instanceof Diagram && style instanceof U2TDiagramCanonicalStyle){
 //			U2TDiagramCanonicalStyle styleImpl = (U2TDiagramCanonicalStyle)style;
 //			styleImpl.setCanonical(true);
 //			styleImpl.setSyncLinks(true); // sic!  
 //			styleImpl.setSyncNodes(isAutoSync);
 //		} else 
 		{
 			style.setCanonical(isAutoSync);
 		}
 	}
 	
 	public boolean shouldSyncLinks(View view) {
 		CanonicalStyle style = getStyle(view);
 		if (style == null){
 			//again, in GMF it means "synchronized by default"
 			return true;
 		}
 //		if (style instanceof U2TDiagramCanonicalStyle){
 //			return ((U2TDiagramCanonicalStyle)style).isSyncLinks();
 //		}
 		return style.isCanonical();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public CanonicalStyle ensureHasCanonicalStyle(View target){
 		CanonicalStyle result = (CanonicalStyle) target.getStyle(NotationPackage.eINSTANCE.getCanonicalStyle());
 		if (result != null){
 			return result;
 		}
 		if (target instanceof Diagram){
 //			U2TDiagramCanonicalStyle u2tStyle = U2TNotationFactory.eINSTANCE.createU2TDiagramCanonicalStyle();  
 			CanonicalStyle u2tStyle = NotationFactory.eINSTANCE.createCanonicalStyle();
 			u2tStyle.setCanonical(true);
 //			u2tStyle.setSyncLinks(true);
 //			u2tStyle.setSyncNodes(true);
 			target.getStyles().add(u2tStyle);
 			result = u2tStyle;
 		} else {
 			result = (CanonicalStyle) target.createStyle(NotationPackage.eINSTANCE.getCanonicalStyle());
 		}
 		return result;
 	}
 
 }
