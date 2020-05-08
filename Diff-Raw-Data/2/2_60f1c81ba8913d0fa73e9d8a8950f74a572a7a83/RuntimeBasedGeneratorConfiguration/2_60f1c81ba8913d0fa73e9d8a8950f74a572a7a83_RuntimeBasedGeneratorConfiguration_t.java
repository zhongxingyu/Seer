 /*
  * Copyright (c) 2006 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Boris Blajer (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests.setup;
 
 import java.lang.reflect.Field;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPartViewer;
 import org.eclipse.gef.RequestConstants;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gmf.codegen.gmfgen.GenCommonBase;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagram;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
 import org.eclipse.gmf.codegen.util.CodegenEmitters;
 import org.eclipse.gmf.codegen.util.Generator;
 import org.eclipse.gmf.internal.common.codegen.GeneratorBase;
 import org.eclipse.gmf.runtime.common.core.command.CommandResult;
 import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
 import org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker;
 import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
 import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
 import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramEditDomain;
 import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
 import org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeRequest;
 import org.eclipse.gmf.runtime.diagram.ui.requests.EditCommandRequestWrapper;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest.ConnectionViewAndElementDescriptor;
 import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
 import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;
 import org.eclipse.gmf.runtime.emf.type.core.IElementType;
 import org.eclipse.gmf.runtime.emf.type.core.IHintedType;
 import org.eclipse.gmf.runtime.emf.type.core.commands.SetValueCommand;
 import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.gmf.tests.EPath;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.swt.graphics.RGB;
 
 public class RuntimeBasedGeneratorConfiguration extends AbstractGeneratorConfiguration {
 
 	public GeneratorBase createGenerator(GenDiagram diagram) {
 		final GenEditorGenerator editorGen = diagram.getEditorGen();
 		// using caching EmitterSource saves few seconds on test execution (115-118 compared to 112)
 		// but it seems reasonable to avoid unpredictability of reuse (due to use of soft references)
 		// and might be good idea to have separate test to test emitter reuse explicitly
 		return new Generator(editorGen, new CodegenEmitters(!editorGen.isDynamicTemplates(), editorGen.getTemplateDirectory()));
 	}
 
 	public ViewerConfiguration createViewerConfiguration(SessionSetup sessionSetup, EditPartViewer viewer) throws Exception {
 		return new DefaultViewerConfiguration(sessionSetup, viewer);
 	}
 
 	protected EditPartViewer createViewerInstance() {
 		return new FakeViewer();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.gmf.tests.setup.GeneratorConfiguration#createDiagram(org.eclipse.emf.ecore.EObject)
 	 */
 	public Diagram createDiagram(EObject domainElement, SessionSetup sessionSetup) throws Exception {
 		String pluginClassName = sessionSetup.getGenModel().getGenDiagram().getEditorGen().getPlugin().getActivatorQualifiedClassName();
 		Class pluginClass = sessionSetup.getGenProject().getBundle().loadClass(pluginClassName);
 		Field field = pluginClass.getField("DIAGRAM_PREFERENCES_HINT");
 		final PreferencesHint hint = (PreferencesHint) field.get(null);
 		return ViewService.createDiagram(domainElement, sessionSetup.getGenModel().getGenDiagram().getEditorGen().getModelID(), hint);
 	}
 
 	protected static class DefaultViewerConfiguration extends AbstractViewerConfiguration {
 
 		public DefaultViewerConfiguration(SessionSetup sessionSetup, EditPartViewer viewer) throws Exception {
 			super(sessionSetup, viewer);
 		}
 
 		public Command getSetBusinessElementStructuralFeatureCommand(View view, String featureName, Object value) {
 			EObject instance = view.getElement();
 			Assert.assertNotNull("No business element bound to notation element", instance); //$NON-NLS-1$
 			EObject resultObj = EPath.findFeature(instance.eClass(), featureName);
 			if (!(resultObj instanceof EStructuralFeature)) {
 				throw new IllegalArgumentException("Not existing feature: " + featureName); //$NON-NLS-1$
 			}
 	
 			EStructuralFeature feature = (EStructuralFeature) resultObj;
 			SetRequest setReq = new SetRequest(instance, feature, value);
 			EditPart editPart = findEditPart(view);
 			TransactionalEditingDomain txEditDomain = getEditDomain(editPart);
 			CompositeTransactionalCommand modelCmd = new CompositeTransactionalCommand(txEditDomain, "Set feature"); //$NON-NLS-1$
 			modelCmd.compose(new SetValueCommand(setReq));
 			return new ICommandProxy(modelCmd);
 		}
 
 		public Command getSetNotationalElementStructuralFeature(View view, final EStructuralFeature feature, final Object value) {
 			Assert.assertNotNull("Null view", view);	//$NON-NLS-1$
 			Assert.assertNotNull("Null feature", feature);	//$NON-NLS-1$
 			final EditPart editPart = findEditPart(view);
 			Assert.assertNotNull("Cannot find edit part", editPart);	//$NON-NLS-1$
 			TransactionalEditingDomain ed = getEditDomain(editPart);
 			Assert.assertNotNull("No TransactionalEditingDomain found", ed);	//$NON-NLS-1$
 			return new ICommandProxy(new AbstractTransactionalCommand(ed, "ChangeColor", Collections.EMPTY_LIST) {
 				protected CommandResult doExecuteWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException  {
 					IGraphicalEditPart ep = (IGraphicalEditPart) editPart; 
 					ep.setStructuralFeatureValue(feature, value);
 					return null;
 				}
 			});
 		}
 
 		protected TransactionalEditingDomain getEditDomain(EditPart editPart) {
 			Assert.assertTrue("IGraphicalEditPart expected", editPart instanceof IGraphicalEditPart); //$NON-NLS-1$
 			return ((IGraphicalEditPart) editPart).getEditingDomain();
 		}
 
 		public Command getCreateNodeCommand(View parentView, GenCommonBase nodeType) {
 			IElementType metamodelType = getElementType(nodeType);
 			CreateUnspecifiedTypeRequest req = new CreateUnspecifiedTypeRequest(Arrays.asList(new IElementType[] { metamodelType }), PreferencesHint.USE_DEFAULTS);
 			return findEditPart(parentView).getCommand(req);
 		}
 
 		public Command getStartLinkCommand(View source, GenCommonBase linkType) {
 			IElementType metamodelType = getElementType(linkType);
 			CreateRelationshipRequest req = new CreateRelationshipRequest(source.getElement(), null, metamodelType);
 			EditCommandRequestWrapper wrapper = new EditCommandRequestWrapper(req);
 			return findEditPart(source).getCommand(wrapper);
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.gmf.tests.setup.GeneratorConfiguration.ViewerConfiguration#getCreateLinkCommand(org.eclipse.gmf.runtime.notation.View, org.eclipse.gmf.runtime.notation.View, org.eclipse.gmf.codegen.gmfgen.GenCommonBase)
 		 */
 		public Command getCreateLinkCommand(View source, View target, GenCommonBase linkType) {
 			IElementType metamodelType = getElementType(linkType);
 			CreateRelationshipRequest relationShipReq = new CreateRelationshipRequest(metamodelType);
 
 			ConnectionViewAndElementDescriptor desc = new ConnectionViewAndElementDescriptor(new CreateElementRequestAdapter(relationShipReq), metamodelType instanceof IHintedType ? ((IHintedType) metamodelType).getSemanticHint() : "", PreferencesHint.USE_DEFAULTS);
 
 			CreateConnectionViewAndElementRequest req = new CreateConnectionViewAndElementRequest(desc);
 			req.setType(RequestConstants.REQ_CONNECTION_START);
 
 			EditPart sourceEditPart = findEditPart(source);
 			req.setSourceEditPart(sourceEditPart);
 			// Note: initializes the sourceCommand in the request
 			Command sourceCmd = sourceEditPart.getCommand(req);
 			if (sourceCmd == null || !sourceCmd.canExecute()) {
 				return null;
 			}
 
 			EditPart targetEditPart = target != null ? findEditPart(target) : null;
 			if (targetEditPart != null) {
 				req.setType(RequestConstants.REQ_CONNECTION_END);
 				req.setTargetEditPart(targetEditPart);
 				req.setLocation(new Point(0, 0));
 				sourceEditPart.getCommand(req);
 				Command targetCmd = targetEditPart.getCommand(req);
 				return targetCmd;
 			}
 			return null;
 		}
 		
 		public RGB getDefaultLinkColor() {
 			return PreferenceConverter.getColor(getDefaultPreferences(), IPreferenceConstants.PREF_LINE_COLOR);
 		}
 
 		private IElementType getElementType(GenCommonBase genElement) {
 			Class clazz = null;
 			try {
 				clazz = loadGeneratedClass(getGenModel().getGenDiagram().getElementTypesQualifiedClassName());
 			} catch (Exception e) {
 				e.printStackTrace();
 				Assert.fail("ElementTypes class not loaded. " + e.getLocalizedMessage()); //$NON-NLS-1$
 			}
 			String identifier = genElement.getUniqueIdentifier();
 			try {
 				Object type = clazz.getField(identifier).get(null);
				assert type != null : "Metatype field in the ElementTypes class should be initialized: " + identifier + " in " + getGenModel().getGenDiagram().getEditorGen().getPlugin().getID(); //$NON-NLS-1$ //$NON-NLS-2$
 				assert type instanceof IElementType : IElementType.class.getName() + ": metamodel type class required"; //$NON-NLS-1$ 
 				return (IElementType) type;
 			} catch (NoSuchFieldException e) {
 				Assert.fail("Metamodel type " + identifier + " is not registered"); //$NON-NLS-1$ //$NON-NLS-2$
 			} catch (Exception e) {
 				e.printStackTrace();
 				Assert.fail("Can't access metamodel type " + identifier); //$NON-NLS-1$			
 			}
 
 			return null;
 		}
 	}
 	
 	private static final class FakeViewer extends AbstractFakeViewer implements IDiagramGraphicalViewer{
 		private FakeViewer() {
 			/*
 			 * When extends DiagramGraphicalViewer, don't forget to 
 			 * super.hookWorkspacePreferenceStore(new PreferenceStore());
 			 */
 		}
 
 		public void setContents(Object contents) {
 			GraphicalEditPart epart = (GraphicalEditPart)getEditPartFactory().createEditPart(null, contents);
 			DiagramEventBroker.startListening(epart.getEditingDomain());
 			super.setContents(epart);
 		}
 
 		public IDiagramEditDomain getDiagramEditDomain() {
 			return (IDiagramEditDomain) super.getEditDomain();
 		}
 
 		public List findEditPartsForElement(String elementIdStr, Class editPartClass) {
 			return Collections.EMPTY_LIST;
 		}
 
 		public void registerEditPartForElement(String elementIdStr, EditPart ep) {
 		}
 
 		public void unregisterEditPartForElement(String elementIdStr, EditPart ep) {
 		}
 	}
 }
