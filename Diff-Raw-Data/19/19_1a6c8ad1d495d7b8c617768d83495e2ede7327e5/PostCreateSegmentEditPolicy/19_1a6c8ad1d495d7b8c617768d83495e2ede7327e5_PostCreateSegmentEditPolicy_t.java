 package org.eclipse.uml2.diagram.timing.edit.policies.create;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.editpolicies.AbstractEditPolicy;
 import org.eclipse.gmf.runtime.common.core.command.CommandResult;
 import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
 import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
 import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
 import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
 import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;
 import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
 import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.diagram.common.editpolicies.AbstractPostCreateCommand;
import org.eclipse.uml2.diagram.timing.draw2d.SegmentGeometry;
 import org.eclipse.uml2.diagram.timing.edit.parts.DSegmentStartEditPart;
 import org.eclipse.uml2.diagram.timing.edit.policies.SegmentAnchor;
 import org.eclipse.uml2.diagram.timing.model.timingd.DSegment;
 import org.eclipse.uml2.diagram.timing.model.timingd.DSegmentEnd;
 import org.eclipse.uml2.diagram.timing.model.timingd.DSegmentStart;
 import org.eclipse.uml2.diagram.timing.model.timingd.TimingDFactory;
 import org.eclipse.uml2.diagram.timing.part.TimingDVisualIDRegistry;
 import org.eclipse.uml2.diagram.timing.providers.TimingDElementTypes;
 
 
 public class PostCreateSegmentEditPolicy extends AbstractEditPolicy {
	public static final String ROLE = PostCreateSegmentEditPolicy.class.getName() + ":Role";
	
 	@Override
 	public Command getCommand(Request request) {
 		if (understandsRequest(request)){
 			return getPostCreateCommand((CreateViewAndElementRequest)request);
 		}
 		return null;
 	}
 	
 	@Override
 	public boolean understandsRequest(Request req) {
 		if (false == req instanceof CreateViewAndElementRequest){
 			return false;
 		}
 		CreateViewAndElementRequest cvaeReq = (CreateViewAndElementRequest)req;
 		CreateElementRequestAdapter requestAdapter = cvaeReq.getViewAndElementDescriptor().getCreateElementRequestAdapter();
 		CreateElementRequest semanticRequest = (CreateElementRequest) requestAdapter.getAdapter(CreateElementRequest.class);
 		return (semanticRequest.getElementType() == TimingDElementTypes.DSegment_3003);
 	}
 	
 	private Command getPostCreateCommand(CreateViewAndElementRequest cvaeReq){
 		CompositeTransactionalCommand cc = new CompositeTransactionalCommand(getDomain(), DiagramUIMessages.AddCommand_Label);
 		cc.compose(new PostCreateSegmentCommand(getDomain(), cvaeReq, getHostImpl()));
 		return new ICommandProxy(cc.reduce());
 	}
 	
 	private IGraphicalEditPart getHostImpl(){
 		return (IGraphicalEditPart) getHost();
 	}
 	
 	private TransactionalEditingDomain getDomain(){
 		return getHostImpl().getEditingDomain();	
 	}
 	
 	private static class PostCreateSegmentCommand extends AbstractPostCreateCommand {
 		public PostCreateSegmentCommand(TransactionalEditingDomain domain, CreateViewAndElementRequest cvaeReq, IGraphicalEditPart hostEditPart){
 			super(domain, cvaeReq, hostEditPart, TimingDVisualIDRegistry.TYPED_ADAPTER);
 		}
 		
 		@Override
 		protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 			SegmentAnchor anchor = SegmentAnchor.Util.getAnchor(getCvaeReq());
 			System.err.println("Anchor: " + anchor);
 			System.err.println("Location: " + getCvaeReq().getLocation());
 			System.err.println("Size: " + getCvaeReq().getSize());
 
 			View createdView = getCreatedView();
 			EObject createdEntity = getCreatedEntity();
 			
 			System.err.println("Created View: " + createdView);
 			System.err.println("Created Segment: " + createdEntity);
 			
 			DSegment segment = (DSegment)createdEntity;
 			DSegmentStart segmentStart = segment.getStart();
 			if (segmentStart == null){
 				segmentStart = TimingDFactory.eINSTANCE.createDSegmentStart();
 				segment.setStart(segmentStart);
 			}
 			
 			View segmentStartView = findChildByType(createdView, DSegmentStartEditPart.VISUAL_ID);
 			if (segmentStartView == null){
 				segmentStartView = ViewService.getInstance().createNode(new EObjectAdapter(segmentStart), createdView, null, ViewUtil.APPEND, getPreferencesHint());
 			}
 			
 			completeOverlappingSegment(anchor);
 			
 			return CommandResult.newOKCommandResult();
 		}
 		
 		private void completeOverlappingSegment(SegmentAnchor anchor){
 			if (anchor == null){
 				return;
 			}
 			DSegment oldSegment = anchor.getOverlappingSegment();
 			View oldSegmentView = anchor.getOverlappingSegmentView();
 			if (oldSegment == null || oldSegmentView == null){
 				return;
 			}
 			
 			if (oldSegment.isClosedSegment() || anchor.getRightAnchor() != null){
 				return;
 			}
 			
 			DSegmentEnd segmentEnd = TimingDFactory.eINSTANCE.createDSegmentEnd();
 			oldSegment.setEnd(segmentEnd);
			ViewService.getInstance().createNode(new EObjectAdapter(segmentEnd), oldSegmentView, null, ViewUtil.APPEND, getPreferencesHint());
 			
			Rectangle oldBounds = anchor.getOverlappingSegmentGlobalBounds();
			Point splitLocation = getCvaeReq().getLocation();
			Dimension completedSize = splitLocation.getDifference(oldBounds.getTopLeft());
			completedSize.height = oldBounds.height;
			completedSize.width += 2 * SegmentGeometry.CIRCLE_RADIUS;
			setSize(oldSegmentView, completedSize);
 		}
 	}
 	
 	
 }
