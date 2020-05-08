 package edu.teco.dnd.graphiti;
 
 import edu.teco.dnd.graphiti.model.FunctionBlockModel;
 import edu.teco.dnd.graphiti.model.OutputModel;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.datatypes.IDimension;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.ILayoutContext;
 import org.eclipse.graphiti.features.impl.AbstractLayoutFeature;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
 import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Font;
 import org.eclipse.graphiti.mm.algorithms.styles.Point;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.FixPointAnchor;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.ui.services.GraphitiUi;
 
 /**
  * Layouts a block after resizing.
  */
 public class DNDLayoutBlockFeature extends AbstractLayoutFeature {
 	/**
 	 * Logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(DNDLayoutBlockFeature.class);
 
 	/**
 	 * Minimum width of a block.
 	 */
 	public static final int MINIMUM_WIDTH = 250;
 
 	/**
 	 * Extra space for the name.
 	 */
 	public static final int NAME_EXTRA_SPACE = 10;
 
 	/**
 	 * Passes the feature provider to the super constructor.
 	 * 
 	 * @param fp
 	 *            the feature provider
 	 */
 	public DNDLayoutBlockFeature(final IFeatureProvider fp) {
 		super(fp);
 	}
 
 	/**
 	 * If the given context can be layouted.
 	 * 
 	 * @param context
 	 *            the context
 	 * @return whether or not the feature can be used
 	 */
 	@Override
 	public final boolean canLayout(final ILayoutContext context) {
 		LOGGER.entry(context);
 		PictogramElement pe = context.getPictogramElement();
 		if (!(pe instanceof ContainerShape)) {
 			LOGGER.exit(false);
 			return false;
 		}
 		EList<EObject> bussinessObjects = pe.getLink().getBusinessObjects();
 		if (bussinessObjects.size() == 1 && bussinessObjects.get(0) instanceof FunctionBlockModel) {
 			LOGGER.exit(true);
 			return true;
 		} else {
 			LOGGER.exit(false);
 			return false;
 		}
 	}
 
 	/**
 	 * Layouts the given context.
 	 * 
 	 * @param context
 	 *            the context
 	 * @return whether or not anything was changed
 	 */
 	@Override
 	public final boolean layout(final ILayoutContext context) {
 		LOGGER.entry(context);
 		boolean changed = false;
 
 		ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
 		GraphicsAlgorithm containerGa = containerShape.getGraphicsAlgorithm();
 		FunctionBlockModel functionBlock = (FunctionBlockModel) containerShape.getLink().getBusinessObjects().get(0);
 
 		LOGGER.debug("layouting {} with ga {} of block {}", containerShape, containerGa, functionBlock);
 
 		Text text = getName(containerShape);
 		int minWidth = MINIMUM_WIDTH;
 		if (text != null) {
 			LOGGER.trace("found name: {}", text);
			final String value = text.getValue();
			final Font font = text.getFont();
			int width = 0;
			if (value != null && font != null) {
				IDimension textSize = GraphitiUi.getUiLayoutService().calculateTextSize(value, font);
				if (textSize != null) {
					width = textSize.getWidth();
				}
			}
			minWidth = Math.max(minWidth, width + NAME_EXTRA_SPACE);
 		}
 		LOGGER.trace("minimum width is {}", minWidth);
 		if (containerGa.getWidth() < minWidth) {
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("width {} is less than minimum width {}", containerGa.getWidth(), minWidth);
 			}
 			containerGa.setWidth(minWidth);
 			changed = true;
 		}
 
 		int minHeight =
 				DNDAddBlockFeature.CONNECTION_OFFSET
 						+ (Math.max(functionBlock.getInputs().size(), functionBlock.getOutputs().size()) + functionBlock
 								.getOptions().size())
 						* (DNDAddBlockFeature.CONNECTION_SIZE + DNDAddBlockFeature.CONNECTION_SPACE);
 		LOGGER.trace("minimum height is {}");
 		if (containerGa.getHeight() < minHeight) {
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("height {} is less than minimum height {}", containerGa.getHeight(), minHeight);
 			}
 			containerGa.setHeight(minHeight);
 			changed = true;
 		}
 
 		int width = containerGa.getWidth();
 
 		IGaService gaService = Graphiti.getGaService();
 		for (Shape shape : containerShape.getChildren()) {
 			GraphicsAlgorithm ga = shape.getGraphicsAlgorithm();
 			if (ga instanceof Polyline) {
 				if (ga.getWidth() != width) {
 					LOGGER.trace("{} resized to {}", ga, width);
 					Polyline polyline = (Polyline) ga;
 					Point endPoint = polyline.getPoints().get(1);
 					Point newPoint = gaService.createPoint(width, endPoint.getY());
 					polyline.getPoints().set(1, newPoint);
 					changed = true;
 				}
 			} else if (ga instanceof Text) {
 				switch (((Text) ga).getHorizontalAlignment()) {
 				case ALIGNMENT_LEFT:
 					if (DNDAddBlockFeature.CONNECTION_VALUE.equals(Graphiti.getPeService().getPropertyValue(ga,
 							DNDAddBlockFeature.CONNECTION_KEY))) {
 						if (LOGGER.isTraceEnabled()) {
 							LOGGER.trace("{} set to half width {}", ga, width / 2 - DNDAddBlockFeature.CONNECTION_SIZE
 									- 2 * DNDAddBlockFeature.CONNECTION_EXTRA);
 						}
 						gaService.setWidth(ga, width / 2 - DNDAddBlockFeature.CONNECTION_SIZE - 3
 								* DNDAddBlockFeature.CONNECTION_EXTRA);
 					} else {
 						if (LOGGER.isTraceEnabled()) {
 							LOGGER.trace("{} set to half width {}", ga, width / 2 - 3 * DNDAddBlockFeature.OPTION_EXTRA);
 						}
 						gaService.setWidth(ga, width / 2 - 2 * DNDAddBlockFeature.OPTION_EXTRA);
 					}
 					break;
 				case ALIGNMENT_RIGHT:
 					if (DNDAddBlockFeature.CONNECTION_VALUE.equals(Graphiti.getPeService().getPropertyValue(ga,
 							DNDAddBlockFeature.CONNECTION_KEY))) {
 						if (LOGGER.isTraceEnabled()) {
 							LOGGER.trace("{} set to half width {} and moved to {}", ga,
 									2 * DNDAddBlockFeature.OPTION_EXTRA, 2 * DNDAddBlockFeature.OPTION_EXTRA);
 						}
 						gaService.setWidth(ga, width / 2 - 2 * DNDAddBlockFeature.CONNECTION_SIZE
 								- DNDAddBlockFeature.CONNECTION_EXTRA * 5);
 						gaService.setLocation(ga, width / 2 + DNDAddBlockFeature.CONNECTION_SIZE + 2
 								* DNDAddBlockFeature.CONNECTION_EXTRA, ga.getY());
 					} else {
 						if (LOGGER.isTraceEnabled()) {
 							LOGGER.trace("{} set to half width {} and moved to {}", ga,
 									2 * DNDAddBlockFeature.OPTION_EXTRA, 2 * DNDAddBlockFeature.OPTION_EXTRA);
 						}
 						gaService.setWidth(ga, width / 2 - 2 * DNDAddBlockFeature.OPTION_EXTRA);
 						gaService.setLocation(ga, width / 2 + DNDAddBlockFeature.OPTION_EXTRA, ga.getY());
 					}
 					break;
 				default:
 					if (ga.getWidth() == width) {
 						break;
 					}
 					LOGGER.trace("{} set to full width {}", ga, width);
 					gaService.setWidth(ga, width);
 					break;
 				}
 				changed = true;
 			} else if (ga.getWidth() != width) {
 				LOGGER.trace("{} set to full width {}", ga, width);
 				gaService.setWidth(ga, width);
 				changed = true;
 			}
 		}
 		for (Anchor anchor : containerShape.getAnchors()) {
 			if (anchor.getLink().getBusinessObjects().get(0) instanceof OutputModel) {
 				FixPointAnchor fixPointAnchor = (FixPointAnchor) anchor;
 				Point location = fixPointAnchor.getLocation();
 				if (location.getX() != width - DNDAddBlockFeature.CONNECTION_SIZE / 2) {
 					fixPointAnchor.setLocation(gaService.createPoint(width - DNDAddBlockFeature.CONNECTION_SIZE / 2
 							- DNDAddBlockFeature.CONNECTION_EXTRA, location.getY()));
 					changed = true;
 				}
 			}
 		}
 
 		LOGGER.exit(changed);
 		return changed;
 	}
 
 	/**
 	 * Returns the Text element containing the name of the block.
 	 * 
 	 * @param containerShape
 	 *            the containerShape of the block
 	 * @return the Text graphics algorithm representing the name
 	 */
 	private Text getName(final ContainerShape containerShape) {
 		LOGGER.entry(containerShape);
 		for (Shape shape : containerShape.getChildren()) {
 			GraphicsAlgorithm ga = shape.getGraphicsAlgorithm();
 			if (ga instanceof Text
 					&& getBusinessObjectForPictogramElement(shape) == getBusinessObjectForPictogramElement(containerShape)) {
 				LOGGER.exit(ga);
 				return (Text) ga;
 			}
 		}
 		LOGGER.exit(null);
 		return null;
 	}
 }
