 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.ui.structure;
 
 import java.util.Arrays;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.etrice.core.naming.RoomFragmentProvider;
 import org.eclipse.etrice.ui.structure.support.ActorContainerRefSupport;
 import org.eclipse.etrice.ui.structure.support.AutoUpdateFeature;
 import org.eclipse.etrice.ui.structure.support.BindingSupport;
 import org.eclipse.etrice.ui.structure.support.LayerConnectionSupport;
 import org.eclipse.etrice.ui.structure.support.PortSupport;
 import org.eclipse.etrice.ui.structure.support.SPPSupport;
 import org.eclipse.etrice.ui.structure.support.StructureClassSupport;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.ILayoutFeature;
 import org.eclipse.graphiti.features.IMoveShapeFeature;
 import org.eclipse.graphiti.features.IReconnectionFeature;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.IResizeShapeFeature;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.IDoubleClickContext;
 import org.eclipse.graphiti.features.context.ILayoutContext;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.context.IPictogramElementContext;
 import org.eclipse.graphiti.features.context.IReconnectionContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.custom.ICustomFeature;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.styles.LineStyle;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
 import org.eclipse.graphiti.tb.IContextButtonPadData;
 import org.eclipse.graphiti.tb.ISelectionInfo;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.graphiti.tb.SelectionInfoImpl;
 import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
 import org.eclipse.graphiti.util.IColorConstant;
 
 import org.eclipse.etrice.core.room.ActorContainerRef;
 import org.eclipse.etrice.core.room.Binding;
 import org.eclipse.etrice.core.room.LayerConnection;
 import org.eclipse.etrice.core.room.Port;
 import org.eclipse.etrice.core.room.SPPRef;
 import org.eclipse.etrice.core.room.StructureClass;
 import org.eclipse.etrice.core.room.util.RoomSwitch;
 
 public class ProviderDispatcher {
 
 	private class FeatureProviderSwitch extends RoomSwitch<IFeatureProvider> {
 		@Override
 		public IFeatureProvider doSwitch(EObject theEObject) {
         	
 			if (theEObject==null)
 				return null;
 			
 			if (theEObject.eIsProxy()) {
         		if (RoomFragmentProvider.isPort(theEObject))
         			return portSupport.getFeatureProvider();
         		if (RoomFragmentProvider.isSPP(theEObject))
         			return sppSupport.getFeatureProvider();
         		if (RoomFragmentProvider.isStructureClass(theEObject))
         			return structureClassSupport.getFeatureProvider();
         		if (RoomFragmentProvider.isBinding(theEObject))
         			return bindingSupport.getFeatureProvider();
         		if (RoomFragmentProvider.isLayerConnection(theEObject))
         			return layerConnectionSupport.getFeatureProvider();
         		if (RoomFragmentProvider.isRef(theEObject))
         			return actorContainerRefSupport.getFeatureProvider();
         	}
 			return super.doSwitch(theEObject);
 		}
 		
 		@Override
 		public IFeatureProvider caseStructureClass(StructureClass object) {
 			return structureClassSupport.getFeatureProvider();
 		}
 
 		@Override
 		public IFeatureProvider casePort(Port port) {
 			return portSupport.getFeatureProvider();
 		}
 		
 		@Override
 		public IFeatureProvider caseSPPRef(SPPRef object) {
 			return sppSupport.getFeatureProvider();
 		}
 		@Override
 		public IFeatureProvider caseActorContainerRef(ActorContainerRef object) {
 			return actorContainerRefSupport.getFeatureProvider();
 		}
 		
 		@Override
 		public IFeatureProvider caseBinding(Binding object) {
 			return bindingSupport.getFeatureProvider();
 		}
 		
 		@Override
 		public IFeatureProvider caseLayerConnection(LayerConnection object) {
 			return layerConnectionSupport.getFeatureProvider();
 		}
 		
 		@Override
 		public IFeatureProvider defaultCase(EObject object) {
 			return null;
 		}
 	
 		public ICreateFeature[] getCreateFeatures() {
 			return concatAll(
 					structureClassSupport.getFeatureProvider().getCreateFeatures(),
 					portSupport.getFeatureProvider().getCreateFeatures(),
 					sppSupport.getFeatureProvider().getCreateFeatures(),
 					actorContainerRefSupport.getFeatureProvider().getCreateFeatures()
 				);
 		}
 
 		public ICreateConnectionFeature[] getCreateConnectionFeatures() {
 			return concatAll(
 					bindingSupport.getFeatureProvider().getCreateConnectionFeatures(),
 					layerConnectionSupport.getFeatureProvider().getCreateConnectionFeatures()
 				);
 		}
 	}
 
 	private class ToolBehaviorProviderSwitch extends RoomSwitch<IToolBehaviorProvider> {
 		@Override
 		public IToolBehaviorProvider doSwitch(EObject theEObject) {
         	
 			if (theEObject==null)
 				return null;
       	
 			if (theEObject.eIsProxy()) {
         		if (RoomFragmentProvider.isPort(theEObject))
         			return portSupport.getToolBehaviorProvider();
         		if (RoomFragmentProvider.isSPP(theEObject))
         			return sppSupport.getToolBehaviorProvider();
         		if (RoomFragmentProvider.isStructureClass(theEObject))
         			return structureClassSupport.getToolBehaviorProvider();
         		if (RoomFragmentProvider.isBinding(theEObject))
         			return bindingSupport.getToolBehaviorProvider();
         		if (RoomFragmentProvider.isLayerConnection(theEObject))
         			return layerConnectionSupport.getToolBehaviorProvider();
         		if (RoomFragmentProvider.isRef(theEObject))
         			return actorContainerRefSupport.getToolBehaviorProvider();
         	}
 			
 			return super.doSwitch(theEObject);
 		}
 		
 		@Override
 		public IToolBehaviorProvider caseStructureClass(StructureClass object) {
 			return structureClassSupport.getToolBehaviorProvider();
 		}
 		
 		@Override
 		public IToolBehaviorProvider casePort(Port port) {
 			return portSupport.getToolBehaviorProvider();
 		}
 		
 		@Override
 		public IToolBehaviorProvider caseSPPRef(SPPRef object) {
 			return sppSupport.getToolBehaviorProvider();
 		}
 		
 		@Override
 		public IToolBehaviorProvider caseActorContainerRef(ActorContainerRef object) {
 			return actorContainerRefSupport.getToolBehaviorProvider();
 		}
 		
 		@Override
 		public IToolBehaviorProvider caseBinding(Binding object) {
 			return bindingSupport.getToolBehaviorProvider();
 		}
 		
 		@Override
 		public IToolBehaviorProvider caseLayerConnection(LayerConnection object) {
 			return layerConnectionSupport.getToolBehaviorProvider();
 		}
 		
 		@Override
 		public IToolBehaviorProvider defaultCase(EObject object) {
 			return null;
 		}
 		
 	}
 
 	private class DispatchingFeatureProvider extends DefaultFeatureProvider {
 
 		public DispatchingFeatureProvider(IDiagramTypeProvider dtp) {
 			super(dtp);
 		}
 		
 		@Override
 	    public ICreateFeature[] getCreateFeatures() {
 	        return getAllCreateFeatures();
 	    }
 		
 		@Override
 		public ICreateConnectionFeature[] getCreateConnectionFeatures() {
 	        return getAllCreateConnectionFeatures();
 		}
 		
 		@Override
 		public IAddFeature getAddFeature(IAddContext context) {
 			IFeatureProvider fp = featureSwitch.doSwitch(((EObject) context.getNewObject()));
 			if (fp==null)
 				return super.getAddFeature(context);
 			else
 				return fp.getAddFeature(context);
 		}
 
 		@Override
 	    public ILayoutFeature getLayoutFeature(ILayoutContext context) {
 	        IFeatureProvider fp = featureSwitch.doSwitch(getBusinessObject(context));
 			if (fp==null)
 				return super.getLayoutFeature(context);
 			else
 				return fp.getLayoutFeature(context);
 	    }
 		
 		@Override
 		public IMoveShapeFeature getMoveShapeFeature(IMoveShapeContext context) {
 	        IFeatureProvider fp = featureSwitch.doSwitch(getBusinessObject(context));
 			if (fp==null)
 				return super.getMoveShapeFeature(context);
 			else
 				return fp.getMoveShapeFeature(context);
 		}
 		
 		@Override
 		public IResizeShapeFeature getResizeShapeFeature(IResizeShapeContext context) {
 	        IFeatureProvider fp = featureSwitch.doSwitch(getBusinessObject(context));
 			if (fp==null)
 				return super.getResizeShapeFeature(context);
 			else
 				return fp.getResizeShapeFeature(context);
 		}
 		
 		@Override
 		public IUpdateFeature getUpdateFeature(IUpdateContext context) {
 			if (context.getPictogramElement() instanceof Diagram)
 				return new AutoUpdateFeature(this);
 			
 	        IFeatureProvider fp = featureSwitch.doSwitch(getBusinessObject(context));
 			if (fp!=null)
 				return fp.getUpdateFeature(context);
 			else
 				return super.getUpdateFeature(context);
 		}
 		
 		@Override
 		public IReconnectionFeature getReconnectionFeature(IReconnectionContext context) {
 			IFeatureProvider fp = getFeatureProvider(context);
 			if (fp!=null)
 				return fp.getReconnectionFeature(context);
 			else
 				return super.getReconnectionFeature(context);
 		}
 		
 		@Override
 		public IDeleteFeature getDeleteFeature(IDeleteContext context) {
 	        IFeatureProvider fp = featureSwitch.doSwitch(getBusinessObject(context));
 			if (fp!=null)
 				return fp.getDeleteFeature(context);
 			else
 				return super.getDeleteFeature(context);
 		}
 		
 		@Override
 		public IRemoveFeature getRemoveFeature(IRemoveContext context) {
 	        IFeatureProvider fp = featureSwitch.doSwitch(getBusinessObject(context));
 			if (fp!=null)
 				return fp.getRemoveFeature(context);
 			else
 				return super.getRemoveFeature(context);
 		}
 		
 		@Override
 		public ICustomFeature[] getCustomFeatures(ICustomContext context) {
 	        IFeatureProvider fp = featureSwitch.doSwitch(getBusinessObject(context));
 			if (fp!=null)
 				return fp.getCustomFeatures(context);
 			else
 				return super.getCustomFeatures(context);
 		}
 		
 		private EObject getBusinessObject(IPictogramElementContext context) {
 			PictogramElement pictogramElement = context.getPictogramElement();
 			EObject bo = (EObject) getBusinessObjectForPictogramElement(pictogramElement);
 			return bo;
 		}
 		
 		private EObject getBusinessObject(ICustomContext context) {
 			PictogramElement[] pes = context.getPictogramElements();
 			if (pes.length>0) {
 				EObject bo = (EObject) getBusinessObjectForPictogramElement(pes[0]);
 				return bo;
 			}
 			return null;
 		}
 		
 		private IFeatureProvider getFeatureProvider(IReconnectionContext context) {
 			return featureSwitch.doSwitch((EObject) getBusinessObjectForPictogramElement(context.getConnection()));
 		}
 	}
 	
 	private class DispatchingToolBehaviorProvider extends DefaultToolBehaviorProvider {
 
 		public DispatchingToolBehaviorProvider(IDiagramTypeProvider diagramTypeProvider) {
 			super(diagramTypeProvider);
 		}
 
 		@Override
 	    public GraphicsAlgorithm[] getClickArea(PictogramElement pe) {
 	        IToolBehaviorProvider bp = getToolBehaviorProvider(pe);
 	        if (bp==null)
 	        	return super.getClickArea(pe);
 	        else
 	        	return bp.getClickArea(pe);
 	    }
 
 		@Override
 	    public GraphicsAlgorithm getSelectionBorder(PictogramElement pe) {
 	        IToolBehaviorProvider bp = getToolBehaviorProvider(pe);
 	        if (bp==null)
 	        	return super.getSelectionBorder(pe);
 	        else
 	        	return bp.getSelectionBorder(pe);
 	    }
 		
 		@Override
 		public ICustomFeature getDoubleClickFeature(IDoubleClickContext context) {
 	        IToolBehaviorProvider bp = getToolBehaviorProvider(context.getPictogramElements()[0]);
 	        if (bp==null)
 	        	return super.getDoubleClickFeature(context);
 	        else
 	        	return bp.getDoubleClickFeature(context);
 		}
 		
 		protected IToolBehaviorProvider getToolBehaviorProvider(PictogramElement pe) {
 			IFeatureProvider fp = getFeatureProvider();
 			EObject bo = (EObject) fp.getBusinessObjectForPictogramElement(pe);
 			if (bo==null)
 				return null;
 			
 			IToolBehaviorProvider bp = behaviorSwitch.doSwitch(bo);
 			return bp;
 		}
 		
 		@Override
 		public IContextButtonPadData getContextButtonPad(IPictogramElementContext context) {
 	        IToolBehaviorProvider bp = getToolBehaviorProvider(context.getPictogramElement());
 	        if (bp==null)
 	        	return super.getContextButtonPad(context);
 	        else
 	        	return bp.getContextButtonPad(context);
 		}
 		
 		@Override
 		public String getToolTip(GraphicsAlgorithm context) {
 	        IToolBehaviorProvider bp = getToolBehaviorProvider(context.getPictogramElement());
 	        if (bp==null)
 	        	return super.getToolTip(context);
 	        else
 	        	return bp.getToolTip(context);
 		}
 
 		public ISelectionInfo getSelectionInfoForShape(Shape shape) {
 			ISelectionInfo si = new SelectionInfoImpl(IColorConstant.SHAPE_SELECTION_FG, IColorConstant.HANDLE_FG, IColorConstant.HANDLE_BG,
 					LineStyle.DASH);
			si.setPrimarySelectionBackgroundColor(IColorConstant.LIGHT_ORANGE);
			si.setSecondarySelectionBackgroundColor(IColorConstant.LIGHT_ORANGE);
 			return si;
 		}
 	}
 	
 	private StructureClassSupport structureClassSupport;
 	private PortSupport portSupport;
 	private SPPSupport sppSupport;
 	private ActorContainerRefSupport actorContainerRefSupport;
 	private BindingSupport bindingSupport;
 	private LayerConnectionSupport layerConnectionSupport;
 	
 	private FeatureProviderSwitch featureSwitch;
 	private ToolBehaviorProviderSwitch behaviorSwitch;
 	
 	private DispatchingFeatureProvider dispatchingFP;
 	private DispatchingToolBehaviorProvider dispatchingBP;
 	
 	
 	public ProviderDispatcher(IDiagramTypeProvider dtp) {
 		// create those first before using them
 		dispatchingFP = new DispatchingFeatureProvider(dtp);
 		dispatchingBP = new DispatchingToolBehaviorProvider(dtp);
 
 		structureClassSupport = new StructureClassSupport(dtp, dispatchingFP);
 		portSupport = new PortSupport(dtp, dispatchingFP);
 		sppSupport = new SPPSupport(dtp, dispatchingFP);
 		actorContainerRefSupport = new ActorContainerRefSupport(dtp, dispatchingFP);
 		bindingSupport = new BindingSupport(dtp, dispatchingFP);
 		layerConnectionSupport = new LayerConnectionSupport(dtp, dispatchingFP);
 		
 		featureSwitch = new FeatureProviderSwitch();
 		behaviorSwitch = new ToolBehaviorProviderSwitch();
 	}
 
 	public IFeatureProvider getFeatureProvider() {
 		return dispatchingFP;
 	}
 	
 	public IToolBehaviorProvider getToolBehaviorProvider() {
 		return dispatchingBP;
 	}
 
 	private ICreateFeature[] getAllCreateFeatures() {
 		return featureSwitch.getCreateFeatures();
 	}
 
 	private ICreateConnectionFeature[] getAllCreateConnectionFeatures() {
 		return featureSwitch.getCreateConnectionFeatures();
 	}
 	
 	private static <T> T[] concatAll(T[] first, T[]... rest) {
 		int totalLength = first.length;
 		for (T[] array : rest) {
 			totalLength += array.length;
 		}
 		T[] result = Arrays.copyOf(first, totalLength);
 		int offset = first.length;
 		for (T[] array : rest) {
 			System.arraycopy(array, 0, result, offset, array.length);
 			offset += array.length;
 		}
 		return result;
 	}
 }
