 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.tests;
 
 import static org.junit.Assert.assertNull;
 
 import org.easymock.EasyMock;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.IDirectEditingContext;
 import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
 import org.eclipse.graphiti.features.impl.AbstractAddShapeFeature;
 import org.eclipse.graphiti.features.impl.AbstractDirectEditingFeature;
 import org.eclipse.graphiti.internal.command.CommandContainer;
 import org.eclipse.graphiti.internal.command.DirectEditingFeatureCommandWithContext;
 import org.eclipse.graphiti.internal.command.GenericFeatureCommandWithContext;
 import org.eclipse.graphiti.internal.command.ICommand;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.tb.IContextEntry;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.graphiti.tests.reuse.GFAbstractTestCase;
 import org.eclipse.graphiti.ui.editor.DiagramEditorFactory;
 import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
 import org.eclipse.graphiti.ui.internal.command.AddModelObjectCommand;
 import org.eclipse.graphiti.ui.internal.command.ContextEntryCommand;
 import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;
 import org.eclipse.graphiti.ui.internal.config.IConfigurationProvider;
 import org.eclipse.graphiti.ui.internal.editor.GFCommandStack;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  *
  */
 @SuppressWarnings("restriction")
 public class CommandStackTest extends GFAbstractTestCase {
 
 	@BeforeClass
 	public static void prepareClass() {
 	}
 
 	@Before
 	public void initializeTest() {
 	}
 
 	/**
 	 * Tests that direct editing features that do not change anything don't make
 	 * the command stack dirty. This functionality is e.g. needed when switching
 	 * to direct editing directly after creating a new object and the user
 	 * simply takes over the default name; the null change must not end up in
 	 * the command stack because that would cause an undo that does nothing and
 	 * the user needs to press undo twice in order to undo the create (without
 	 * any feedback for the first undo).
 	 */
 	@Test
 	public void testHasDoneChangesForDirectEditing() {
 		TransactionalEditingDomain editingDomain = DiagramEditorFactory.createResourceSetAndEditingDomain();
 		IConfigurationProvider configurationProvider = initConfigurationProviderForHasDoneChangesTests(editingDomain);
 		GFCommandStack commandStack = new GFCommandStack(configurationProvider, editingDomain);
 		IFeatureProvider featureProvider = configurationProvider.getFeatureProvider();
 
 		TestDirectEditingFeature feature = new TestDirectEditingFeature(featureProvider);
 
 		IDirectEditingContext context = EasyMock.createNiceMock(IDirectEditingContext.class);
 		EasyMock.replay(context);
 
		DirectEditingFeatureCommandWithContext featureCommand = new DirectEditingFeatureCommandWithContext(feature, context, "Initial");
 
 		executeAndCheck(featureCommand, commandStack, editingDomain);
 	}
 
 	@Test
 	public void testHasDoneChangesForCustomFeature() {
 		TransactionalEditingDomain editingDomain = DiagramEditorFactory.createResourceSetAndEditingDomain();
 		IConfigurationProvider configurationProvider = initConfigurationProviderForHasDoneChangesTests(editingDomain);
 		GFCommandStack commandStack = new GFCommandStack(configurationProvider, editingDomain);
 		IFeatureProvider featureProvider = configurationProvider.getFeatureProvider();
 
 		TestCustomFeature feature = new TestCustomFeature(featureProvider);
 
 		ICustomContext context = EasyMock.createNiceMock(ICustomContext.class);
 		EasyMock.replay(context);
 
 		GenericFeatureCommandWithContext featureCommand = new GenericFeatureCommandWithContext(feature, context);
 
 		CommandContainer commandContainer = new CommandContainer(featureProvider);
 		commandContainer.add(featureCommand);
 
 		executeAndCheck(commandContainer, commandStack, editingDomain);
 	}
 
 	@Test
 	public void testHasDoneChangesForAddObject() {
 		TransactionalEditingDomain editingDomain = DiagramEditorFactory.createResourceSetAndEditingDomain();
 		IConfigurationProvider configurationProvider = initConfigurationProviderForHasDoneChangesTests(editingDomain);
 		GFCommandStack commandStack = new GFCommandStack(configurationProvider, editingDomain);
 
 		IAddContext context = EasyMock.createNiceMock(IAddContext.class);
 		EasyMock.replay(context);
 
 		// Feature is set via EasyMock in init method
 		AddModelObjectCommand featureCommand = new AddModelObjectCommand(configurationProvider, null,
 				new StructuredSelection(new Object()), new Rectangle());
 
 		executeAndCheck(featureCommand, commandStack);
 	}
 
 	@Test
 	public void testHasDoneChangesForContextEntry() {
 		TransactionalEditingDomain editingDomain = DiagramEditorFactory.createResourceSetAndEditingDomain();
 		IConfigurationProvider configurationProvider = initConfigurationProviderForHasDoneChangesTests(editingDomain);
 		GFCommandStack commandStack = new GFCommandStack(configurationProvider, editingDomain);
 		IFeatureProvider featureProvider = configurationProvider.getFeatureProvider();
 
 		IContext context = EasyMock.createNiceMock(IContext.class);
 		EasyMock.replay(context);
 
 		IContextEntry contextEntry = EasyMock.createNiceMock(IContextEntry.class);
 		EasyMock.expect(contextEntry.getFeature()).andReturn(new TestDeleteFeature(featureProvider)).anyTimes();
 		EasyMock.expect(contextEntry.getContext()).andReturn(context).anyTimes();
 		EasyMock.replay(contextEntry);
 
 		ContextEntryCommand featureCommand = new ContextEntryCommand(contextEntry);
 
 		executeAndCheck(featureCommand, commandStack);
 	}
 
 	private IConfigurationProvider initConfigurationProviderForHasDoneChangesTests(TransactionalEditingDomain editingDomain) {
 		IToolBehaviorProvider toolBehaviorProvider = EasyMock.createNiceMock(IToolBehaviorProvider.class);
 		EasyMock.replay(toolBehaviorProvider);
 
 		IDiagramTypeProvider diagramTypeProvider = EasyMock.createNiceMock(IDiagramTypeProvider.class);
 		IFeatureProvider featureProvider = EasyMock.createNiceMock(IFeatureProvider.class);
 		EasyMock.expect(featureProvider.getDiagramTypeProvider()).andReturn(diagramTypeProvider).anyTimes();
 		EasyMock.expect(featureProvider.getAddFeature((IAddContext) EasyMock.notNull()))
 				.andReturn(new TestAddObjectFeature(featureProvider)).anyTimes();
 		EasyMock.replay(featureProvider);
 
 		EasyMock.expect(diagramTypeProvider.getCurrentToolBehaviorProvider()).andReturn(toolBehaviorProvider).anyTimes();
 		EasyMock.expect(diagramTypeProvider.getFeatureProvider()).andReturn(featureProvider).anyTimes();
 		EasyMock.replay(diagramTypeProvider);
 
 		IConfigurationProvider configurationProvider = EasyMock.createNiceMock(IConfigurationProvider.class);
 		EasyMock.expect(configurationProvider.getDiagramTypeProvider()).andReturn(diagramTypeProvider).anyTimes();
 		EasyMock.replay(configurationProvider);
 
 		return configurationProvider;
 	}
 
 	private void executeAndCheck(ICommand featureCommand, GFCommandStack commandStack, TransactionalEditingDomain editingDomain) {
 		GefCommandWrapper commandWrapper = new GefCommandWrapper(featureCommand, editingDomain);
 		commandStack.execute(commandWrapper);
 		assertNull("Direct editing command must not appear in command stack", commandStack.getUndoCommand());
 	}
 
 	private void executeAndCheck(Command command, GFCommandStack commandStack) {
 		commandStack.execute(command);
 		assertNull("Direct editing command must not appear in command stack", commandStack.getUndoCommand());
 	}
 
 	private class TestDirectEditingFeature extends AbstractDirectEditingFeature {
 
 		public TestDirectEditingFeature(IFeatureProvider featureProvider) {
 			super(featureProvider);
 		}
 
 		@Override
 		public int getEditingType() {
 			return TYPE_TEXT;
 		}
 
 		@Override
 		public String getInitialValue(IDirectEditingContext context) {
 			return "Initial";
 		}
 
 		@Override
 		public void setValue(String value, IDirectEditingContext context) {
 			// Do nothing
 		}
 	}
 
 	private class TestCustomFeature extends AbstractCustomFeature {
 
 		public TestCustomFeature(IFeatureProvider fp) {
 			super(fp);
 		}
 
 		@Override
 		public boolean canExecute(ICustomContext context) {
 			return true;
 		}
 
 		@Override
 		public void execute(ICustomContext context) {
 			// Do nothing
 		}
 
 		@Override
 		public boolean hasDoneChanges() {
 			return false;
 		}
 	}
 
 	private class TestAddObjectFeature extends AbstractAddShapeFeature {
 
 		public TestAddObjectFeature(IFeatureProvider fp) {
 			super(fp);
 		}
 
 		@Override
 		public boolean canAdd(IAddContext context) {
 			return true;
 		}
 
 		@Override
 		public PictogramElement add(IAddContext context) {
 			// Do nothing
 			return null;
 		}
 
 		@Override
 		public boolean hasDoneChanges() {
 			return false;
 		}
 	}
 
 	private class TestDeleteFeature extends DefaultDeleteFeature {
 
 		public TestDeleteFeature(IFeatureProvider fp) {
 			super(fp);
 		}
 
 		@Override
 		public boolean canDelete(IDeleteContext context) {
 			return true;
 		}
 
 		@Override
 		public void delete(IDeleteContext context) {
 			// Do nothing
 		}
 
 		@Override
 		public boolean hasDoneChanges() {
 			return false;
 		}
 	}
 }
