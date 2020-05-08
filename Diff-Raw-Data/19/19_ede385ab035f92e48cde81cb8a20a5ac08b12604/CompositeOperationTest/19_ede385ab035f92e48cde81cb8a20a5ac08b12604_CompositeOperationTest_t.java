 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.test.changeTracking.operations;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 
import org.eclipse.emf.emfstore.client.handler.ESOperationModifier;
 import org.eclipse.emf.emfstore.client.test.WorkspaceTest;
 import org.eclipse.emf.emfstore.client.test.model.document.DocumentFactory;
 import org.eclipse.emf.emfstore.client.test.model.document.LeafSection;
 import org.eclipse.emf.emfstore.client.test.model.requirement.RequirementFactory;
 import org.eclipse.emf.emfstore.client.test.model.requirement.UseCase;
 import org.eclipse.emf.emfstore.client.test.model.task.ActionItem;
 import org.eclipse.emf.emfstore.client.test.model.task.TaskFactory;
 import org.eclipse.emf.emfstore.client.test.model.task.WorkPackage;
import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionRegistry;
 import org.eclipse.emf.emfstore.internal.client.model.CompositeOperationHandle;
 import org.eclipse.emf.emfstore.internal.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.internal.client.model.exceptions.InvalidHandleException;
 import org.eclipse.emf.emfstore.internal.client.model.impl.AutoOperationWrapper;
 import org.eclipse.emf.emfstore.internal.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.internal.client.model.util.EMFStoreCommandWithResult;
 import org.eclipse.emf.emfstore.internal.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.internal.common.model.Project;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.CompositeOperation;
 import org.junit.Test;
 
 /**
  * Tests the comnposite operation recording.
  * 
  * @author koegel
  */
 public class CompositeOperationTest extends WorkspaceTest {
 
 	private LeafSection addSection() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 
 		return new EMFStoreCommandWithResult<LeafSection>() {
 			@Override
 			protected LeafSection doRun() {
 				getProject().addModelElement(section);
 				section.setName("Name");
 				section.setDescription("Description");
 
 				assertEquals(true, getProject().contains(section));
 				assertEquals("Name", section.getName());
 				assertEquals("Description", section.getDescription());
 				assertEquals(0, section.getModelElements().size());
 
 				clearOperations();
 
 				return section;
 			}
 		}.run(false);
 	}
 
 	/**
 	 * Test the creation and completion of a composite operation.
 	 */
 	@Test
 	public void createSmallComposite() {
 
 		final LeafSection section = addSection();
 		final UseCase useCase = RequirementFactory.eINSTANCE.createUseCase();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				CompositeOperationHandle handle = getProjectSpace().beginCompositeOperation();
 				section.setName("newName");
 				section.setDescription("newDescription");
 				section.getModelElements().add(useCase);
 
 				assertEquals(true, getProject().contains(useCase));
 				assertEquals(getProject(), ModelUtil.getProject(useCase));
 				assertEquals(useCase, section.getModelElements().iterator().next());
 				assertEquals("newName", section.getName());
 				assertEquals("newDescription", section.getDescription());
 
 				ModelElementId sectionId = ModelUtil.getProject(section).getModelElementId(section);
 
 				try {
 					handle.end("sectionCreation", "description", sectionId);
 				} catch (InvalidHandleException e) {
 					fail();
 				}
 			}
 		}.run(false);
 
 		assertEquals(true, getProject().contains(useCase));
 		assertEquals(getProject(), ModelUtil.getProject(useCase));
 		assertEquals(useCase, section.getModelElements().iterator().next());
 		assertEquals("newName", section.getName());
 		assertEquals("newDescription", section.getDescription());
 
 		assertEquals(1, getProjectSpace().getOperations().size());
 		AbstractOperation operation = getProjectSpace().getOperations().iterator().next();
 		assertEquals(true, operation instanceof CompositeOperation);
 		CompositeOperation compositeOperation = (CompositeOperation) operation;
 		assertEquals(4, compositeOperation.getSubOperations().size());
 	}
 
 	/**
 	 * Test the creation and completion of a composite operation.
 	 */
 	@Test
 	public void createSmallCompositeAcrossCommands() {
 
 		final LeafSection section = addSection();
 		final UseCase useCase = RequirementFactory.eINSTANCE.createUseCase();
 
 		final CompositeOperationHandle handle = new EMFStoreCommandWithResult<CompositeOperationHandle>() {
 			@Override
 			protected CompositeOperationHandle doRun() {
 				CompositeOperationHandle handle = getProjectSpace().beginCompositeOperation();
 				section.setName("newName");
 				section.setDescription("newDescription");
 				section.getModelElements().add(useCase);
 
 				assertEquals(true, getProject().contains(useCase));
 				assertEquals(getProject(), ModelUtil.getProject(useCase));
 				assertEquals(useCase, section.getModelElements().iterator().next());
 				assertEquals("newName", section.getName());
 				assertEquals("newDescription", section.getDescription());
 
 				return handle;
 			}
 		}.run(false);
 
 		final ModelElementId sectionId = ModelUtil.getProject(section).getModelElementId(section);
 		ProjectSpace projectSpace = ModelUtil.getParent(ProjectSpace.class, section);
 		assertEquals(0, projectSpace.getOperations().size());
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				try {
 					handle.end("sectionCreation", "description", sectionId);
 				} catch (InvalidHandleException e) {
 					fail();
 				}
 			}
 		}.run(false);
 
 		assertEquals(true, getProject().contains(useCase));
 		assertEquals(getProject(), ModelUtil.getProject(useCase));
 		assertEquals(useCase, section.getModelElements().iterator().next());
 		assertEquals("newName", section.getName());
 		assertEquals("newDescription", section.getDescription());
 
 		assertEquals(1, getProjectSpace().getOperations().size());
 		AbstractOperation operation = getProjectSpace().getOperations().iterator().next();
 		assertEquals(true, operation instanceof CompositeOperation);
 		CompositeOperation compositeOperation = (CompositeOperation) operation;
 		assertEquals(4, compositeOperation.getSubOperations().size());
 	}
 
 	/**
 	 * Test the creation and completion of a composite operation.
 	 */
 	@Test
 	public void createSmallCompositeAcrossCommandsWithAutoOperationWrapper() {
 
		// TODO: Think about elegant solution to replace the operation modifier during a single test
		ESOperationModifier operationModifier = ExtensionRegistry.INSTANCE.get(ESOperationModifier.ID,
			ESOperationModifier.class);

		ExtensionRegistry.INSTANCE.set(
			ESOperationModifier.ID,
			new AutoOperationWrapper());

 		final LeafSection section = addSection();
 		final UseCase useCase = RequirementFactory.eINSTANCE.createUseCase();
 
 		final CompositeOperationHandle handle = new EMFStoreCommandWithResult<CompositeOperationHandle>() {
 			@Override
 			protected CompositeOperationHandle doRun() {
 				CompositeOperationHandle handle = getProjectSpace().beginCompositeOperation();
 				section.setName("newName");
 				section.setDescription("newDescription");
 				section.getModelElements().add(useCase);
 
 				assertEquals(true, getProject().contains(useCase));
 				assertEquals(getProject(), ModelUtil.getProject(useCase));
 				assertEquals(useCase, section.getModelElements().iterator().next());
 				assertEquals("newName", section.getName());
 				assertEquals("newDescription", section.getDescription());
 
 				return handle;
 			}
 		}.run(false);
 
 		final ModelElementId sectionId = ModelUtil.getProject(section).getModelElementId(section);
 		ProjectSpace projectSpace = ModelUtil.getParent(ProjectSpace.class, section);
 		assertEquals(0, projectSpace.getOperations().size());
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				try {
 					handle.end("sectionCreation", "description", sectionId);
 				} catch (InvalidHandleException e) {
 					fail();
 				}
 			}
 		}.run(false);
 
 		assertEquals(true, getProject().contains(useCase));
 		assertEquals(getProject(), ModelUtil.getProject(useCase));
 		assertEquals(useCase, section.getModelElements().iterator().next());
 		assertEquals("newName", section.getName());
 		assertEquals("newDescription", section.getDescription());
 
 		assertEquals(1, getProjectSpace().getOperations().size());
 		AbstractOperation operation = getProjectSpace().getOperations().iterator().next();
 		assertEquals(true, operation instanceof CompositeOperation);
 		CompositeOperation compositeOperation = (CompositeOperation) operation;
 		assertEquals(4, compositeOperation.getSubOperations().size());

		ExtensionRegistry.INSTANCE.set(
			ESOperationModifier.ID,
			operationModifier);
 	}
 
 	/**
 	 * Test the creation and abort of a composite operation.
 	 * 
 	 * @throws InvalidHandleException if the test fails
 	 * @throws IOException
 	 */
 	@Test
 	public void abortSmallComposite() throws InvalidHandleException, IOException {
 
 		final LeafSection section = addSection();
 
 		final UseCase useCase = RequirementFactory.eINSTANCE.createUseCase();
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				CompositeOperationHandle handle = getProjectSpace().beginCompositeOperation();
 				section.setName("newName");
 				section.setDescription("newDescription");
 				section.getModelElements().add(useCase);
 
 				assertEquals(true, getProject().contains(useCase));
 				assertEquals(getProject(), ModelUtil.getProject(useCase));
 				assertEquals(useCase, section.getModelElements().iterator().next());
 				assertEquals("newName", section.getName());
 				assertEquals("newDescription", section.getDescription());
 
 				try {
 					handle.abort();
 				} catch (InvalidHandleException e) {
 					fail();
 				}
 			}
 		}.run(false);
 
 		assertEquals(true, getProject().contains(section));
 		assertEquals("Name", section.getName());
 		assertEquals("Description", section.getDescription());
 		assertEquals(0, section.getModelElements().size());
 		assertEquals(false, getProject().contains(useCase));
 
 		assertEquals(0, getProjectSpace().getOperations().size());
 
 		Project loadedProject = ModelUtil.loadEObjectFromResource(
 			org.eclipse.emf.emfstore.internal.common.model.ModelFactory.eINSTANCE.getModelPackage().getProject(),
 			getProject()
 				.eResource().getURI(), false);
 
 		assertTrue(ModelUtil.areEqual(loadedProject, getProject()));
 		assertEquals(false, getProject().contains(useCase));
 		assertEquals(true, getProject().contains(section));
 	}
 
 	/**
 	 * Test the creation and abort of a composite operation after some elements have been added. Check if the abort
 	 * reverses the last operation.
 	 */
 	@Test
 	public void beginAndAbortEmptyCompositeAfterSimpleOperation() {
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 				final WorkPackage workPackage = TaskFactory.eINSTANCE.createWorkPackage();
 				final ActionItem actionItem = TaskFactory.eINSTANCE.createActionItem();
 				getProject().addModelElement(section);
 				getProject().addModelElement(workPackage);
 				getProject().addModelElement(actionItem);
 				actionItem.setContainingWorkpackage(workPackage);
 				CompositeOperationHandle compositeOperationHandle = getProjectSpace().beginCompositeOperation();
 				try {
 					compositeOperationHandle.abort();
 				} catch (InvalidHandleException e) {
 					throw new IllegalStateException(e);
 				}
 
 				assertEquals(workPackage, actionItem.getContainingWorkpackage());
 
 			}
 		}.run(false);
 	}
 
 	/**
 	 * Test the creation and abort of a composite operation.
 	 */
 	@Test
 	public void beginAndAbortEmptyComposite() {
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				CompositeOperationHandle compositeOperationHandle = getProjectSpace().beginCompositeOperation();
 				try {
 					compositeOperationHandle.abort();
 					compositeOperationHandle = getProjectSpace().beginCompositeOperation();
 					compositeOperationHandle.abort();
 					compositeOperationHandle = getProjectSpace().beginCompositeOperation();
 					compositeOperationHandle.abort();
 				} catch (InvalidHandleException e) {
 					fail();
 				}
 			}
 		}.run(false);
 	}
 }
