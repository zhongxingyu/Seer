 /*******************************************************************************
  * Copyright (c) 2006, 2007 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.tests.unit.core.util.efactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.emf.compare.FactoryException;
 import org.eclipse.emf.compare.tests.EMFCompareTestPlugin;
 import org.eclipse.emf.compare.util.EFactory;
 import org.eclipse.emf.compare.util.ModelUtils;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 
 /**
  * Tests the behavior of {@link EFactory#eRemove(EObject, String, Object)}.
  * 
  * @author Laurent Goubet <a href="mailto:laurent.goubet@obeo.fr">laurent.goubet@obeo.fr</a>
  */
 @SuppressWarnings({"unchecked", "nls", })
 public class TestERemove extends TestCase {
 	/** Path to an ecore model to use for those tests. */
 	private static final String INPUT_MODEL_ECORE = "/inputs/attribute/volatile/v2.ecore";
 
 	/** Contains invalid feature names whatever the target. */
 	private String[] invalidFeatureNames = {null, "", "-1", "invalidFeature", };
 
 	/** UML and ecore models that will be used for the tests. Loaded from {@link #setUp()}. */
	private EObject[] models = new EObject[1];
 
 	/**
 	 * Tests {@link EFactory#eRemove(EObject, String, Object)} with <code>null</code> as its first
 	 * parameter. No matter what the other arguments are, expects a {@link NullPointerException} to be thrown.
 	 */
 	public void testERemoveNullObject() {
 		for (int i = 0; i < invalidFeatureNames.length; i++) {
 			try {
 				EFactory.eRemove(null, invalidFeatureNames[i], null);
 				fail("Expected NullPointerException hasn't been thrown by eRemove().");
 			} catch (NullPointerException e) {
 				// We expected this
 			} catch (FactoryException e) {
 				fail("Unexpected FactoryException has been thrown by eRemove() operation.");
 			}
 		}
 	}
 
 	/**
 	 * Tests {@link EFactory#eRemove(EObject, String, Object)} on a valid EObject with an invalid feature
 	 * name. No matter what the value is, expects a {@link FactoryException} to be thrown.
 	 */
 	public void testERemoveValidObjectInvalidFeature() {
 		for (int i = 0; i < models.length; i++) {
 			// For each model, we'll only iterate through the first root's content
 			for (int j = 0; j < models[i].eContents().size(); j++) {
 				for (int k = 0; k < invalidFeatureNames.length; k++) {
 					try {
 						EFactory.eRemove(models[i].eContents().get(j), invalidFeatureNames[k], null);
 						fail("Expected FactoryException hasn't been thrown by eRemove().");
 					} catch (NullPointerException e) {
 						fail("Unexpected NullPointerException has been thrown by eRemove().");
 					} catch (FactoryException e) {
 						// We expected this
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Tests {@link EFactory#eRemove(EObject, String, Object)} on a valid EObject with existing, unsettable
 	 * feature names. Expects a {@link FactoryException} to be thrown if the feature is single-valued.
 	 * Multi-valued features won't be affected.
 	 */
 	public void testERemoveValidObjectUnsettableFeature() {
 		for (int i = 0; i < models.length; i++) {
 			// For each models, we'll only iterate through the first root's content
 			for (int j = 0; j < models[i].eContents().size(); j++) {
 				final EObject object = models[i].eContents().get(j);
 				for (EStructuralFeature feature : object.eClass().getEAllStructuralFeatures()) {
 					if (!feature.isChangeable()) {
 						final Object oldValue = object.eGet(feature);
 						try {
 							EFactory.eRemove(object, feature.getName(), null);
 							if (oldValue instanceof List)
 								assertEquals("eRemove changed the feature's values list.", oldValue, object
 										.eGet(feature));
 							else
 								fail("Expected NullPointerException hasn't been thrown by eRemove();");
 						} catch (FactoryException e) {
 							if (oldValue instanceof List)
 								fail("Unexpected NullPointerException thrown by eRemove.");
 							// This was expected behavior for single-valued features
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Tests {@link EFactory#eRemove(EObject, String, Object)} on a valid EObject with valid feature names. If
 	 * the feature is a list, expects the value to be removed from it, otherwise expects the feature value to
 	 * be reset to its default.
 	 */
 	public void testERemoveValidObjectValidFeature() {
 		for (int i = 0; i < models.length; i++) {
 			// For each models, we'll only iterate through the first root's content
 			for (int j = 0; j < models[i].eContents().size(); j++) {
 				final EObject object = models[i].eContents().get(j);
 				for (EStructuralFeature feature : object.eClass().getEAllStructuralFeatures()) {
 					if (feature.isChangeable()) {
 						if (feature.isMany()) {
 							final Object[] arrayValues = ((List)object.eGet(feature)).toArray();
 							int currentSize = arrayValues.length;
 							for (int k = 0; k < arrayValues.length; k++) {
 								try {
 									EFactory.eRemove(object, feature.getName(), arrayValues[k]);
 									assertSame("Unexpected size of the list after eRemove() execution.",
 											--currentSize, ((List)object.eGet(feature)).size());
 									// As Lists allow duplicates, we can't assert that element is no more
 									// contained
 								} catch (FactoryException e) {
 									fail("Unexpected FactoryException thrown when removing a value from a list feature.");
 								}
 							}
 							assertSame(
 									"Feature values List hasn't been emptied by removing all its elements.",
 									0, ((List)object.eGet(feature)).size());
 						} else {
 							try {
 								// Feature is single valued. eRemove should reset its value whatever the third
 								// argument
 								EFactory.eRemove(object, feature.getName(), "anyValue");
 								assertEquals("Feature hasn't been reset to its default by eRemove().",
 										feature.getDefaultValue(), object.eGet(feature));
 							} catch (FactoryException e) {
 								fail("Unexpected FactoryException thrown when removing a value from a list feature.");
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Tests {@link EFactory#eRemove(EObject, String, Object)} on a valid EObject with valid feature names. As
 	 * we try to remove <code>null</code> from the feature's value, we expect no change to the value if it
 	 * is a {@link List}. Otherwise, we expect the value to be reset to its default value.
 	 */
 	public void testERemoveValidObjectValidFeatureNullValue() {
 		for (int i = 0; i < models.length; i++) {
 			// For each models, we'll only iterate through the first root's content
 			for (int j = 0; j < models[i].eContents().size(); j++) {
 				final EObject object = models[i].eContents().get(j);
 				for (EStructuralFeature feature : object.eClass().getEAllStructuralFeatures()) {
 					if (feature.isChangeable()) {
 						final Object oldValue = object.eGet(feature);
 						try {
 							EFactory.eRemove(object, feature.getName(), null);
 							if (oldValue instanceof List) {
 								assertEquals("eRemove(null) changed the feature's values list.", oldValue,
 										object.eGet(feature));
 							} else {
 								assertEquals("Feature hasn't been set to its default value by eRemove.",
 										feature.getDefaultValue(), object.eGet(feature));
 							}
 						} catch (FactoryException e) {
 							fail("Unexpected FactoryException has been thrown by eRemove().");
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Override
 	protected void setUp() {
 		try {
 			final File ecoreFile = new File(FileLocator.toFileURL(
 					EMFCompareTestPlugin.getDefault().getBundle().getEntry(INPUT_MODEL_ECORE)).getFile());
 			models[0] = ModelUtils.load(ecoreFile, new ResourceSetImpl());
 		} catch (IOException e) {
 			fail("Test setUp failed to load input models.");
 		}
 	}
 }
