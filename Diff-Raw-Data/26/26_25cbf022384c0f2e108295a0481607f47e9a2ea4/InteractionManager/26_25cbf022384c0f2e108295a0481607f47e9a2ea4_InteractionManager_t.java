 package com.mymed.controller.core.manager.application;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Map;
 
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.AbstractManager;
 import com.mymed.controller.core.manager.reputation.old.ReputationManager;
 import com.mymed.controller.core.manager.storage.StorageManager;
 import com.mymed.model.data.reputation.MInteractionBean;
 import com.mymed.model.data.reputation.MReputationBean;
 
 /**
  * Manage the reputation of a user
  * 
  * @author lvanni
  * @author Milo Casagrande
  * 
  */
 public class InteractionManager extends AbstractManager implements IInteractionManager {
 
 	private ReputationManager reputationManager;
 
 	public InteractionManager() throws InternalBackEndException {
 		this(new StorageManager());
 	}
 
 	public InteractionManager(final StorageManager storageManager) throws InternalBackEndException {
 		super(storageManager);
 		this.reputationManager = new ReputationManager();
 	}
 
 	@Override
 	public void create(final MInteractionBean interaction) throws InternalBackEndException, IOBackEndException {
 		try { // Verify the interaction does not exist
 			storageManager.selectColumn(CF_INTERACTION, interaction.getId(), "id");
 		} catch (IOBackEndException e) {
 			// INTERACTION CREATION
 			storageManager.insertSlice(CF_INTERACTION, interaction.getId(), interaction.getAttributeToMap());
			if(interaction.getFeedback() != -1) {
 				// REPUTATION UPDATE
 				MReputationBean reputation = reputationManager.read(interaction.getProducer(), interaction.getConsumer(), interaction.getApplication());
 				// count the number of interaction 
 				//			int count = storageManager.countColumns(SC_INTERACTION_LIST, reputation.getInteractionList());
 				int count = reputation.getNbInteraction();
 				// calcul of the new reputation value
 //				System.out.print("\nreputation.getValue() : " + reputation.getValue());
 //				System.out.print("\ninteraction.getFeedback() : " + interaction.getFeedback());
 //				System.out.print("\ncount + 1 : " + count + 1);
 				double reputationValue = (reputation.getValue() + interaction.getFeedback()) / (count + 1);
 //				System.out.println("\nreputationValue : " + reputationValue);
 				reputation.setValue(reputationValue);
 				// update the reputation
 				reputationManager.update(reputation, interaction.getApplication()+interaction.getProducer());
 				try {
 					// update the interaction list
 					storageManager.insertSuperColumn(SC_INTERACTION_LIST, reputation.getInteractionList(), interaction.getId(), "id", interaction.getId().getBytes("UTF-8"));
 					// update the rater list
 					storageManager.insertSuperColumn(SC_USER_LIST, reputation.getRaterList(), interaction.getConsumer(), "user", interaction.getConsumer().getBytes("UTF-8"));
 				} catch (UnsupportedEncodingException e1) {
 					throw new InternalBackEndException(e1);
 				}
 			}
 			return;
 		}
 		throw new IOBackEndException("Interaction already exist!");
 	}
 
 	@Override
 	public MInteractionBean read(final String interactionID) throws InternalBackEndException, IOBackEndException {
 		final MInteractionBean interaction = new MInteractionBean();
 		final Map<byte[], byte[]> args = storageManager.selectAll(CF_INTERACTION, interactionID);
 
 		return (MInteractionBean) introspection(interaction, args);
 	}
 
 	@Override
 	public void update(final MInteractionBean interaction) throws InternalBackEndException, IOBackEndException {
 		create(interaction);
 	}
 
 	@Override
 	public void delete(final String interactionID) throws InternalBackEndException {
 		storageManager.removeAll(CF_INTERACTION, interactionID);
 	}
 }
