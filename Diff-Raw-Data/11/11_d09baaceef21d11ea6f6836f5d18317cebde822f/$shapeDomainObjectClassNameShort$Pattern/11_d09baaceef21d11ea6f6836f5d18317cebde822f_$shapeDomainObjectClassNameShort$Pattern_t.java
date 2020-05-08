 package $packageName$.patterns;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.context.ILayoutContext;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.pattern.AbstractPattern;
 import org.eclipse.graphiti.pattern.IPattern;
 import org.eclipse.graphiti.pattern.config.IPatternConfiguration;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 
 public class $shapeDomainObjectClassNameShort$Pattern extends AbstractPattern implements IPattern {
 
	public $shapeDomainObjectClassNameShort$Pattern() {
		super(null);
 	}
 
 	@Override
 	public String getCreateName() {
 		return "$shapeDomainObjectClassNameShort$";
 	}
 
 	@Override
 	public boolean isMainBusinessObjectApplicable(Object mainBusinessObject) {
 		// TODO: check for right domain object instances below
 		// return mainBusinessObject instanceof Match;
 		return true;
 	}
 
 	@Override
 	protected boolean isPatternControlled(PictogramElement pictogramElement) {
 		Object domainObject = getBusinessObjectForPictogramElement(pictogramElement);
 		return isMainBusinessObjectApplicable(domainObject);
 	}
 
 	@Override
 	protected boolean isPatternRoot(PictogramElement pictogramElement) {
 		Object domainObject = getBusinessObjectForPictogramElement(pictogramElement);
 		return isMainBusinessObjectApplicable(domainObject);
 	}
 
 	@Override
 	public boolean canCreate(ICreateContext context) {
 		return context.getTargetContainer() instanceof Diagram;
 	}
 	
 	@Override
 	public Object[] create(ICreateContext context) {
 		// TODO: create the domain object here
 		Object new$shapeDomainObjectClassNameShort$ = null;
 		
 		// TODO: in case of an EMF object add the new object to a suitable resource
 		// getDiagram().eResource().getContents().add(new$shapeDomainObjectClassNameShort$);
 
 		addGraphicalRepresentation(context, new$shapeDomainObjectClassNameShort$);
 		return new Object[] { new$shapeDomainObjectClassNameShort$ };
 	}
 	
 	@Override
 	public boolean canAdd(IAddContext context) {
 		// TODO: check for right domain object instance below
 		return /* context.getNewObject() instanceof $shapeDomainObjectClassNameShort$ && */ context.getTargetContainer() instanceof Diagram;
 	}
 	
 	@Override
 	public PictogramElement add(IAddContext context) {
 
 		Diagram targetDiagram = (Diagram) context.getTargetContainer();
 		IPeCreateService peCreateService = Graphiti.getPeCreateService();
 		IGaService gaService = Graphiti.getGaService();
 
 		ContainerShape containerShape = peCreateService.createContainerShape(targetDiagram, true);
 		RoundedRectangle roundedRectangle = gaService.createRoundedRectangle(containerShape, 5, 5);
 		gaService.setLocationAndSize(roundedRectangle, context.getX(), context.getY(), context.getWidth(), context.getHeight());
 		roundedRectangle.setFilled(false);
 		
 		Shape shape = peCreateService.createShape(containerShape, false);
 		Text text = gaService.createText(shape, "$shapeDomainObjectClassNameShort$");
 		text.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
 		text.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
 		gaService.setLocationAndSize(text, 0, 0, context.getWidth(), context.getHeight());
 
 		peCreateService.createChopboxAnchor(containerShape);
 
 		// TODO: enable the link to the domain object
 		// Object addedDomainObject = context.getNewObject();
 		// link(containerShape, addedDomainObject);
 
 		return containerShape;
 	}
 	
 	@Override
 	public boolean canLayout(ILayoutContext context) {
 		// TODO: check for right domain object instances below
 		return context.getPictogramElement() instanceof ContainerShape /* && getBusinessObjectForPictogramElement(context.getPictogramElement()) instanceof $shapeDomainObjectClassNameShort$ */;
 	}
 	
 	@Override
 	public boolean layout(ILayoutContext context) {
 		PictogramElement pictogramElement = context.getPictogramElement();
 		if (pictogramElement instanceof ContainerShape) {
 			ContainerShape containerShape = (ContainerShape) pictogramElement;
 			GraphicsAlgorithm outerGraphicsAlgorithm = containerShape.getGraphicsAlgorithm();
 			if (outerGraphicsAlgorithm instanceof RoundedRectangle) {
 				RoundedRectangle roundedRectangle = (RoundedRectangle) outerGraphicsAlgorithm;
 				EList<Shape> children = containerShape.getChildren();
 				if (children.size() > 0) {
 					Shape shape = children.get(0);
 					GraphicsAlgorithm graphicsAlgorithm = shape.getGraphicsAlgorithm();
 					if (graphicsAlgorithm instanceof Text) {
 						Graphiti.getGaLayoutService().setLocationAndSize(graphicsAlgorithm, 0, 0, roundedRectangle.getWidth(), roundedRectangle.getHeight());
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 }
