 package edu.teco.dnd.graphiti;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.impl.DefaultRemoveFeature;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 
 import edu.teco.dnd.graphiti.model.InputModel;
 import edu.teco.dnd.graphiti.model.OutputModel;
 
 /**
  * Used to remove connections from inputs and outputs.
  */
 public class DNDRemoveDataConnectionFeature extends DefaultRemoveFeature {
 
 	/**
 	 * Logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(DNDRemoveDataConnectionFeature.class);
 
 	/**
 	 * Initializes a new DNDRemoveDataConnectionFeature.
 	 * 
 	 * @param fp
 	 *            IFeatureProvider.
 	 */
 	public DNDRemoveDataConnectionFeature(final IFeatureProvider fp) {
 		super(fp);
 	}
 
 	@Override
 	public boolean canRemove(final IRemoveContext context) {
 		LOGGER.entry(context);
 		if (!(context.getPictogramElement() instanceof Connection)) {
 			LOGGER.debug("not a Connection");
 			return false;
 		}
 		Connection connection = (Connection) context.getPictogramElement();
 		if (!(getBusinessObjectForPictogramElement(connection.getStart()) instanceof OutputModel)) {
 			LOGGER.debug("begin is not a OutputModel");
 			return false;
 		}
 		if (!(getBusinessObjectForPictogramElement(connection.getEnd()) instanceof InputModel)) {
 			LOGGER.debug("end is not a InputModel");
 			return false;
 		}
 		return true;
 	}
 
 	@Override
	public void execute(final IContext context) {
		Connection connection = (Connection) ((IRemoveContext) context).getPictogramElement();
 		InputModel input = (InputModel) getBusinessObjectForPictogramElement(connection.getEnd());
 		input.setOutput(null);
		super.execute(context);
 	}
 }
