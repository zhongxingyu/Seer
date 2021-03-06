 package org.wso2.developerstudio.eclipse.gmf.esb.diagram.custom;
 
 import org.eclipse.gef.Request;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.OpenEditPolicy;
import org.eclipse.gmf.runtime.notation.Node;
import org.wso2.developerstudio.eclipse.gmf.esb.Sequence;
 import org.wso2.developerstudio.eclipse.gmf.esb.diagram.edit.parts.NamedEndpointEditPart;
 import org.wso2.developerstudio.eclipse.gmf.esb.diagram.edit.parts.SequenceEditPart;
 
 public class OpenSeparatelyEditPolicy extends OpenEditPolicy {
 
 	protected Command getOpenCommand(Request request) {
 		if (getTargetEditPart(request) instanceof SequenceEditPart) {
			String name=((Sequence)((Node)((SequenceEditPart)getTargetEditPart(request)).getModel()).getElement()).getName();
			if(!"{XPath}".equals(name) && !name.startsWith("conf:")){
				((SequenceEditPart) getTargetEditPart(request)).createDialogBox();
			}
 			
 		}else if(getTargetEditPart(request) instanceof NamedEndpointEditPart){
 			((NamedEndpointEditPart) getTargetEditPart(request)).createDialogBox();
 			
 		}else if(getTargetEditPart(request) instanceof ComplexFiguredAbstractEndpoint){
 			((ComplexFiguredAbstractEndpoint)getTargetEditPart(request)).openPage();
 		}
 		return  null;
 	}
 
 }
