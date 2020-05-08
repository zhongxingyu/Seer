 package com.vectorsf.jvoice.diagram.core.pattern.note;
 
 //import IDirectEditingInfo;
 //import LNote;
 
 import org.eclipse.graphiti.features.IReason;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.context.IDirectEditingContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.impl.ResizeShapeContext;
 import org.eclipse.graphiti.features.impl.Reason;
 import org.eclipse.graphiti.func.IDirectEditing;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Rectangle;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.pattern.id.IdLayoutContext;
 import org.eclipse.graphiti.pattern.id.IdPattern;
 import org.eclipse.graphiti.pattern.id.IdUpdateContext;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 import org.eclipse.graphiti.util.IColorConstant;
 import org.eclipse.graphiti.util.IPredefinedRenderingStyle;
 
 import com.vectorsf.jvoice.diagram.core.pattern.StatePredefinedColoredAreas;
 import com.vectorsf.jvoice.model.operations.Flow;
 import com.vectorsf.jvoice.model.operations.Note;
 import com.vectorsf.jvoice.model.operations.OperationsFactory;
 
 public class NotePattern extends IdPattern {
 
 	protected static final String ID_NAME_TEXT = "nameText";
 	protected static final String ID_MAIN_FIGURE = "mainFigure";
 	protected static final int MIN_WIDTH = 100;
 	protected static final int MIN_HEIGHT = 60;
 	private static final String NOTE = "Note";
 
 	private static final IColorConstant CLASS_TEXT_FOREGROUND = IColorConstant.BLACK;
 	private static final String FONT = "Segoe UI"; //$NON-NLS-1$
 
 	@Override
 	public boolean canAdd(IAddContext context) {
 		return context.getTargetContainer() instanceof Diagram && context.getNewObject() instanceof Note;
 	}
 
 	@Override
 	public void resizeShape(IResizeShapeContext context) {
 		if (context.getWidth() < MIN_WIDTH || context.getHeight() < MIN_HEIGHT) {
 			ResizeShapeContext context2 = new ResizeShapeContext(context.getShape());
 			context2.setWidth(Math.max(MIN_WIDTH, context.getWidth()));
 			context2.setHeight(Math.max(MIN_HEIGHT, context.getHeight()));
 			context2.setX(context.getX());
 			context2.setY(context.getY());
 			context2.setDirection(context.getDirection());
 			super.resizeShape(context2);
 		} else {
 			super.resizeShape(context);
 		}
 	}
 
 	@Override
 	public boolean canCreate(ICreateContext context) {
 		return context.getTargetContainer() instanceof Diagram;
 	}
 
 	@Override
 	public boolean canDirectEdit(IDirectEditingContext context) {
 		return true;
 	}
 
 	@Override
 	protected boolean canDirectEdit(IDirectEditingContext context, String id) {
 		return id.equals(ID_NAME_TEXT);
 	}
 
 	@Override
 	public int getEditingType() {
 		return IDirectEditing.TYPE_TEXT;
 	}
 
 	@Override
 	protected PictogramElement doAdd(IAddContext context) {
 		Note addedDomainObject = (Note) context.getNewObject();
 		IPeCreateService peCreateService = Graphiti.getPeCreateService();
 		IGaService gaService = Graphiti.getGaService();
 
 		Note note = (Note) context.getNewObject();
 		int height = context.getHeight();
 		height = height < 50 ? 50 : height;
 		int width = context.getWidth();
 		width = width < 150 ? 150 : width;
 
 		ContainerShape outerContainerShape = peCreateService.createContainerShape(getDiagram(), true);
 
 		Rectangle mainRectangle = gaService.createRectangle(outerContainerShape);
 		setId(mainRectangle, ID_MAIN_FIGURE);
 		mainRectangle.setFilled(true);
 		gaService.setRenderingStyle(mainRectangle,
 				StatePredefinedColoredAreas.getAdaptedGradientColoredAreas(IPredefinedRenderingStyle.LIGHT_YELLOW_ID));
 		gaService.setLocationAndSize(mainRectangle, context.getX(), context.getY(),
 				Math.max(MIN_WIDTH, context.getWidth()), Math.max(MIN_HEIGHT, context.getHeight()));
 
 		Text text = gaService.createText(mainRectangle, addedDomainObject.getDescription());
 		setId(text, ID_NAME_TEXT);
 		text.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
 		text.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
 		// Create and set text graphics algorithm
 		// Shape textContainer = peCreateService.createShape(outerContainerShape, false);
 		// MultiText text = gaService.createDefaultMultiText(getDiagram(), textContainer, note.getDescription());
 		// text.setForeground(manageColor(CLASS_TEXT_FOREGROUND));
 		// text.setHorizontalAlignment(Orientation.ALIGNMENT_LEFT);
 		// text.setVerticalAlignment(Orientation.ALIGNMENT_TOP);
 		// text.setFont(gaService.manageFont(getDiagram(), FONT, 8, false, false));
 
 		// create link and wire it
 		link(outerContainerShape, note);
 
 		peCreateService.createChopboxAnchor(outerContainerShape);
 
 		link(outerContainerShape, addedDomainObject);
 
 		return outerContainerShape;
 	}
 
 	@Override
 	public boolean isMainBusinessObjectApplicable(Object mainBusinessObject) {
 		return mainBusinessObject instanceof Note;
 	}
 
 	@Override
 	protected boolean layout(IdLayoutContext context, String id) {
 		boolean changesDone = false;
 
 		GraphicsAlgorithm gaOuter = context.getRootPictogramElement().getGraphicsAlgorithm();
 
 		if (id.equals(ID_MAIN_FIGURE) || id.equals(ID_NAME_TEXT)) {
 			GraphicsAlgorithm ga = context.getGraphicsAlgorithm();
 			if (ga.getWidth() != gaOuter.getWidth() || ga.getHeight() != gaOuter.getHeight()) {
 				Graphiti.getGaService().setLocationAndSize(ga, 0, 0, gaOuter.getWidth(), gaOuter.getHeight());
 				changesDone = true;
 			}
 		}
 
 		return changesDone;
 
 	}
 
 	@Override
 	public String getCreateName() {
 		return NOTE;
 	}
 
 	@Override
 	protected IReason updateNeeded(IdUpdateContext context, String id) {
 		if (id.equals(ID_NAME_TEXT)) {
 			Note note = (Note) getBusinessObjectForPictogramElement(context.getRootPictogramElement());
 			Text text = (Text) context.getGraphicsAlgorithm();
 			if (!text.getValue().equals(note.getDescription())) {
 				return Reason.createTrueReason();
 			}
 		}
 		return Reason.createFalseReason();
 	}
 
 	@Override
 	protected boolean update(IdUpdateContext context, String id) {
 		if (id.equals(ID_NAME_TEXT)) {
 			Note note = (Note) getBusinessObjectForPictogramElement(context.getRootPictogramElement());
 			Text text = (Text) context.getGraphicsAlgorithm();
 			text.setValue(note.getDescription());
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public Object[] create(ICreateContext context) {
 
 		Note note = OperationsFactory.eINSTANCE.createNote();
 		note.setDescription("note");
 		note.setName("Note");
 		Flow flow = (Flow) getBusinessObjectForPictogramElement(getDiagram());
 		flow.getNotes().add(note);
 
 		addGraphicalRepresentation(context, note);
 
 		return new Object[] { note };
 	}
 
 	@Override
 	protected void setValue(String value, IDirectEditingContext context, String id) {
 		if (id.equals(ID_NAME_TEXT)) {
 			Note ss = (Note) getBusinessObjectForPictogramElement(context.getPictogramElement());
 			ss.setDescription(value);
 			updatePictogramElement(context.getPictogramElement());
 		}
 	}
 
 }
