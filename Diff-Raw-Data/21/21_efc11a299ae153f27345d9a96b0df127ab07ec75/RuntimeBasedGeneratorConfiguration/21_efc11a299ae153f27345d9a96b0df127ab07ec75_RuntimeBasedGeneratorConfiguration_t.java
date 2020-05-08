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
 import org.eclipse.draw2d.DelegatingLayout;
 import org.eclipse.draw2d.FreeformLayer;
 import org.eclipse.draw2d.LayeredPane;
 import org.eclipse.draw2d.ScalableLayeredPane;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Translatable;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPartFactory;
 import org.eclipse.gef.EditPartViewer;
 import org.eclipse.gef.LayerConstants;
 import org.eclipse.gef.RequestConstants;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.editparts.LayerManager;
 import org.eclipse.gef.editparts.ScalableRootEditPart;
 import org.eclipse.gmf.codegen.gmfgen.GenCommonBase;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagram;
 import org.eclipse.gmf.codegen.util.Generator;
 import org.eclipse.gmf.gmfgraph.BasicFont;
 import org.eclipse.gmf.gmfgraph.Font;
 import org.eclipse.gmf.gmfgraph.FontStyle;
 import org.eclipse.gmf.gmfgraph.GMFGraphFactory;
 import org.eclipse.gmf.internal.common.codegen.GeneratorBase;
 import org.eclipse.gmf.runtime.common.core.command.CommandResult;
 import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
 import org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker;
 import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
 import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramCommandStack;
 import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditDomain;
 import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramEditDomain;
 import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
 import org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeRequest;
 import org.eclipse.gmf.runtime.diagram.ui.requests.EditCommandRequestWrapper;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest.ConnectionViewAndElementDescriptor;
 import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
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
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.osgi.framework.Bundle;
 
 public class RuntimeBasedGeneratorConfiguration implements GeneratorConfiguration {
 	public GeneratorBase createGenerator(GenDiagram diagram) {
 		return new Generator(diagram.getEditorGen());
 	}
 
 	public ViewerConfiguration createViewerConfiguration(Composite parent, SessionSetup sessionSetup, Diagram canvas) throws Exception {
 		EditPartViewer viewer = createViewer(parent);
 		String epFactoryClassName = sessionSetup.getGenModel().getGenDiagram().getEditPartFactoryQualifiedClassName();
 		Class epFactory = sessionSetup.getGenProject().getBundle().loadClass(epFactoryClassName);
 		assert EditPartFactory.class.isAssignableFrom(epFactory);
 		viewer.setEditPartFactory((EditPartFactory) epFactory.newInstance());
 		viewer.setContents(canvas);
 		return createViewerConfiguration(sessionSetup, viewer);
 	}
 
 	protected ViewerConfiguration createViewerConfiguration(SessionSetup sessionSetup, EditPartViewer viewer) throws Exception {
 		return new DefaultViewerConfiguration(sessionSetup, viewer);
 	}
 
 	private EditPartViewer createViewer(Composite parent) {
 		// make sure there's display for current thread
 		Display.getDefault();
 	
 		EditPartViewer gv = createViewerInstance();
 		gv.createControl(parent);
 		DiagramEditDomain ded = new DiagramEditDomain(null);
 		gv.setEditDomain(ded);
 		gv.getEditDomain().setCommandStack(new DiagramCommandStack(ded));
 		return gv;
 	}
 
 	protected EditPartViewer createViewerInstance() {
 		return new FakeViewer();
 	}
 
 	protected static class DefaultViewerConfiguration implements ViewerConfiguration {
 		private EditPartViewer myViewer;
 		private Bundle myGenProject;
 		private SessionSetup mySessionSetup;
		private PreferencesHint myDefaultPreferences;
 
 		public DefaultViewerConfiguration(SessionSetup sessionSetup, EditPartViewer viewer) throws Exception {
 			myViewer = viewer;
 			mySessionSetup = sessionSetup;
 			myGenProject = sessionSetup.getGenProject().getBundle();
 		}
 
 		public EditPartViewer getViewer() {
 			return myViewer;
 		}
 
 		public EditPart findEditPart(View notationElement) {
 			return (EditPart) myViewer.getEditPartRegistry().get(notationElement);
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
 
 		public void dispose() {
 			myViewer = null;
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
 		
 		public Font getDefaultFont() {
 			BasicFont result = GMFGraphFactory.eINSTANCE.createBasicFont();
 			FontData defaultFontData = PreferenceConverter.getDefaultFontData(getDefaultPreferences(), IPreferenceConstants.PREF_DEFAULT_FONT);
 			result.setFaceName(defaultFontData.getName());
 			result.setHeight(defaultFontData.getHeight());
 			result.setStyle(FontStyle.get(defaultFontData.getStyle()));
 			return result;
 		}
 		
 		private IPreferenceStore getDefaultPreferences() {
			if (myDefaultPreferences == null){
				try {
					Class activatorClazz = loadGeneratedClass(mySessionSetup.getGenModel().getGenDiagram().getEditorGen().getPlugin().getActivatorQualifiedClassName());
					Field field = activatorClazz.getField("DIAGRAM_PREFERENCES_HINT");
					myDefaultPreferences = (PreferencesHint)field.get(null);
				} catch (ClassNotFoundException e) {
					myDefaultPreferences = PreferencesHint.USE_DEFAULTS;
				} catch (SecurityException e) {
					myDefaultPreferences = PreferencesHint.USE_DEFAULTS;
				} catch (NoSuchFieldException e) {
					myDefaultPreferences = PreferencesHint.USE_DEFAULTS;
				} catch (IllegalAccessException e) {
					myDefaultPreferences = PreferencesHint.USE_DEFAULTS;
				}
			}
			return (IPreferenceStore) myDefaultPreferences.getPreferenceStore();
 		}
 
 		protected final Class loadGeneratedClass(String qualifiedClassName) throws ClassNotFoundException {
 			return myGenProject.loadClass(qualifiedClassName);
 		}
 
 		protected DiaGenSource getGenModel() {
 			return mySessionSetup.getGenModel();
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
 				assert type != null : "Metatype field in the ElementTypes class should be initialized"; //$NON-NLS-1$
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
 	
 	protected static abstract class FakeViewerBase extends NoUpdateViewer {
 		protected void createDefaultRoot() {
 			// Important for MapModeUtil.getMapMode() method implementation.
 			setRootEditPart(new ScalableRootEditPart() {
 				protected ScalableLayeredPane createScaledLayers() {
 					class LayeredPane extends ScalableLayeredPane implements IMapMode {
 
 						public int DPtoLP(int deviceUnit) {
 							return deviceUnit;
 						}
 
 						public Translatable DPtoLP(Translatable t) {
 							t.performScale(1.0);
 							return t;
 						}
 
 						public int LPtoDP(int logicalUnit) {
 							return logicalUnit;
 						}
 
 						public Translatable LPtoDP(Translatable t) {
 							t.performScale(1.0);
 							return t;
 						}
 						
 					}
 					ScalableLayeredPane layers = new LayeredPane();
 					layers.add(createGridLayer(), GRID_LAYER);
 					layers.add(getPrintableLayers(), PRINTABLE_LAYERS);
 					FreeformLayer feedbackLayer = new FreeformLayer();
 					feedbackLayer.setEnabled(false);
 					layers.add(feedbackLayer, SCALED_FEEDBACK_LAYER);
 					return layers;
 				}
 			});
 			//code from <...>GenDiagramEditor -- required to work with external labels
 			LayerManager root = (LayerManager) getRootEditPart();
 			LayeredPane printableLayers = (LayeredPane) root.getLayer(LayerConstants.PRINTABLE_LAYERS);
 			FreeformLayer extLabelsLayer = new FreeformLayer();
 			extLabelsLayer.setLayoutManager(new DelegatingLayout());
 			printableLayers.addLayerAfter(extLabelsLayer, "External Node Labels", LayerConstants.PRIMARY_LAYER);
 		}
 	}
 
 	private static final class FakeViewer extends FakeViewerBase implements IDiagramGraphicalViewer{
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
