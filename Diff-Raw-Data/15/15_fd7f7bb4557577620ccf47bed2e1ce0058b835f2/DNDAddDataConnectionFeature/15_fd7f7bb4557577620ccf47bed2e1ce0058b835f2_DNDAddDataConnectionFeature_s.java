 package edu.teco.dnd.graphiti;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IAddConnectionContext;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.impl.AbstractAddFeature;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 import org.eclipse.graphiti.util.IColorConstant;
 
 import edu.teco.dnd.graphiti.model.InputModel;
 import edu.teco.dnd.graphiti.model.OutputModel;
 
 /**
  * Adds a representation for a data connection.
  */
 public class DNDAddDataConnectionFeature extends AbstractAddFeature {
 	/**
 	 * The logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(DNDAddDataConnectionFeature.class);
 
 	/**
 	 * Passes the feature provider to the super constructor.
 	 * 
 	 * @param fp
 	 *            the feature provider
 	 */
 	public DNDAddDataConnectionFeature(final IFeatureProvider fp) {
 		super(fp);
 	}
 
 	/**
 	 * Whether or not the feature can be used in the given context.
 	 * 
 	 * @param context
 	 *            the context
 	 * @return whether or not the feature can be used
 	 */
 	@Override
 	public boolean canAdd(final IAddContext context) {
 		if (!(context instanceof IAddConnectionContext)) {
 			return false;
 		}
 		IAddConnectionContext iac = (IAddConnectionContext) context;
 		Object sourceBo = getBusinessObjectForPictogramElement(iac.getSourceAnchor());
 		Object targetBo = getBusinessObjectForPictogramElement(iac.getTargetAnchor());
 		return sourceBo instanceof OutputModel && targetBo instanceof InputModel;
 	}
 
 	/**
 	 * Returns a graphical representation for the given data connection.
 	 * 
 	 * @param context
 	 *            the context
 	 * @return a graphical representation
 	 */
 	@Override
 	public PictogramElement add(final IAddContext context) {
 		LOGGER.entry(context);
 
 		IAddConnectionContext addConContext = (IAddConnectionContext) context;
 		IPeCreateService peCreateService = Graphiti.getPeCreateService();
 
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Connecting {} and {}", getBusinessObjectForPictogramElement(addConContext.getSourceAnchor()),
 					getBusinessObjectForPictogramElement(addConContext.getTargetAnchor()));
 		}
 
 		// CONNECTION WITH POLYLINE
 		LOGGER.debug("creating polyline");
 		Connection connection = peCreateService.createFreeFormConnection(getDiagram());
 		connection.setStart(addConContext.getSourceAnchor());
 		connection.setEnd(addConContext.getTargetAnchor());
 		LOGGER.debug("Connection is {}", connection);
 		IGaService gaService = Graphiti.getGaService();
 		Polyline polyline = gaService.createPolyline(connection);
 		polyline.setLineWidth(2);
 		polyline.setForeground(manageColor(IColorConstant.RED));
 		LOGGER.debug("Polyline is {}", polyline);
 
		// create link and wire it
		LOGGER.debug("linking");
		link(connection, getBusinessObjectForPictogramElement(addConContext.getTargetAnchor()));

 		LOGGER.exit(connection);
 		return connection;
 	}
 }
