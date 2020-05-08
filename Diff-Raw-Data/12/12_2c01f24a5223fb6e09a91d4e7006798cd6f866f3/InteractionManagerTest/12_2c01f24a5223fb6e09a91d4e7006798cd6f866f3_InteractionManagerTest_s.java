 package com.mymed.tests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Map;
 
 import org.junit.Test;
 
 import com.mymed.model.data.reputation.MInteractionBean;
 
 public class InteractionManagerTest extends GeneralTest {
 	/**
 	 * Create an interaction entry in the database
 	 */
 	@Test
 	public void testCreateInteraction() {
 		try {
 			interactionManager.create(interactionBean);
 		} catch (final Exception ex) {
 			fail(ex.getMessage());
 		}
 	}
 
 	/**
 	 * Read the just created interaction entry from the database
 	 */
 	@Test
 	public void testReadInteraction() {
 		try {
 			final MInteractionBean readValue = interactionManager.read(INTERACTION_ID);
 			assertEquals("The interaction beans are not the same\n", interactionBean, readValue);
 		} catch (final Exception ex) {
 			fail(ex.getMessage());
 		}
 	}
 
 	/**
 	 * Update the interaction bean, and check that the new bean is not the same
 	 * as the old one
 	 */
 	@Test
 	public void testUpdateInteraction() {
 		try {
			final MInteractionBean newInteraction = interactionBean.clone();
			newInteraction.setFeedback(4);
 
 			final MInteractionBean readValue = interactionManager.read(INTERACTION_ID);
 			assertFalse("The interaction beans are the same.", interactionBean.equals(readValue));
 		} catch (final Exception ex) {
 			fail(ex.getMessage());
 		}
 	}
 
 	/**
 	 * Delete the session created
 	 */
 	@Test
 	public void testDeleteInteraction() {
 		try {
 			interactionManager.delete(INTERACTION_ID);
 			final Map<byte[], byte[]> column = storageManager.selectAll("Interaction", INTERACTION_ID);
 			assertTrue("The number of columns after a delete is not 0", column.isEmpty());
 		} catch (final Exception ex) {
 			fail(ex.getMessage());
 		}
 	}
 }
