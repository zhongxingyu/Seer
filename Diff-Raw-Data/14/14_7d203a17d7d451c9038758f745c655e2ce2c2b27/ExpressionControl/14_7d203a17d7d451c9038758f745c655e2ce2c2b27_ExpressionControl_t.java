 package org.openetcs.ui.internal.expression;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.emf.ecp.core.ECPProjectManager;
import org.eclipse.emf.ecp.edit.ECPControlContext;
 import org.eclipse.emf.ecp.edit.internal.swt.util.SWTControl;
 import org.eclipse.emf.ecp.emfstore.core.internal.EMFStoreProvider;
 import org.eclipse.emf.ecp.spi.core.InternalProject;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.emfstore.client.ESLocalProject;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.resource.XtextResourceSet;
 import org.openetcs.dsl.ui.internal.ExpressionActivator;
 import org.openetcs.ui.internal.expression.XtextEmbeddedEditorProvider.XtextStuff;
 
 import com.google.inject.Injector;
 
 public class ExpressionControl extends SWTControl {
 
 
 	public ExpressionControl(boolean showLabel,
 			IItemPropertyDescriptor itemPropertyDescriptor,
 			EStructuralFeature feature,
			ECPControlContext modelElementContext, boolean embedded) {
 		super(showLabel, itemPropertyDescriptor, feature, modelElementContext, embedded);
 		project=(InternalProject) ECPProjectManager.INSTANCE.getProject(getModelElementContext().getModelElement());
 	}
 	private InternalProject project;
 	private StyledText text;
 	private XtextEmbeddedEditorProvider provider;
 	private Resource resource;
 	
 	@Override
 	protected void bindValue() {
 		IObservableValue value = SWTObservables.observeText(text, SWT.FocusOut);
 		getDataBindingContext().bindValue(value, getModelValue(), new UpdateValueStrategy() {
 
 			@Override
 			public Object convert(Object value) {
 				return xtextToXmi((String) value);
 			}
 
 		}, new UpdateValueStrategy() {
 			@Override
 			public Object convert(Object value) {
 				return xmiToXtext((String) value);
 			}
 		});
 	}
 
 	@Override
 	public Composite createControl(Composite composite) {
 		provider = getInjector().getInstance(XtextEmbeddedEditorProvider.class);
 		XtextStuff xtextStuff = provider.getXtextEmbeddedEditor(
 				composite, project);
 
 		final ISourceViewer viewer = xtextStuff.embeddedEditor.getViewer();
 		resource=xtextStuff.resource;
 		text = viewer.getTextWidget();
 		bindValue();
 		return text;
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void handleValidation(Diagnostic arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void resetValidation() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setEditable(boolean arg0) {
 		text.setEditable(arg0);
 	}
 	
 	 URI virtualURI = URI.createURI("VirtualURI");
 		private String xtextToXmi(String xtext) {
 			if(xtext==null||xtext.isEmpty())
 				return xtext;
 
 			ResourceSet resourceSet2 = new ResourceSetImpl();
 			Resource resource2 = resourceSet2
 					.createResource(virtualURI);
 			Map<Object,Object> loadOptions=new HashMap<Object,Object>();
 			loadOptions.put(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
 			loadOptions.put(XMIResource.OPTION_ENCODING, "UTF-8");
 			Map<Object,Object> saveOptions=new HashMap<Object,Object>();
 			saveOptions.put(XMIResource.OPTION_ENCODING, "UTF-8");
 			try {
 				resource2.getContents().addAll(resource.getContents());
 				ByteArrayOutputStream baos=new ByteArrayOutputStream();
 				
 				resource2.save(baos, saveOptions);
 				String result= baos.toString("UTF-8");
 				return result;
 			} catch (IOException e) {
 				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(),e));
 			}
 			return xtext;
 		}
 
 		private String xmiToXtext(String xmi) {
 			if(xmi==null||xmi.isEmpty())
 				return xmi;
 			XtextResourceSet resSetresXtext = new XtextResourceSet();
 			Resource resXtext=provider.getResourceFactory().createResource(virtualURI);
 			resSetresXtext.getResources().add(resXtext);
 			
 			ResourceSet resourceSetXmi = new ResourceSetImpl();
 			Resource resourceXmi = resourceSetXmi
 					.createResource(virtualURI);
 
 			Map<Object,Object> saveOptions=new HashMap<Object,Object>();
 			saveOptions.put(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
 			saveOptions.put(XMIResource.OPTION_ENCODING, "UTF-8");
 			ESLocalProject projectSpace = EMFStoreProvider.INSTANCE.getProjectSpace(project);
 			resSetresXtext.addLoadOption("ProjectElements", projectSpace.getAllModelElements());
 			
 			Map<Object,Object> loadOptions=new HashMap<Object,Object>();
 			loadOptions.put(XMIResource.OPTION_ENCODING, "UTF-8");
 			
 			
 			try {
 				ByteArrayInputStream bais = new ByteArrayInputStream(
 						xmi.getBytes("UTF-8"));
 				resourceXmi.load(bais, loadOptions);
 				EcoreUtil.resolveAll(resourceXmi.getContents().get(0));
 				resXtext.getContents().addAll(resourceXmi.getContents());
 				ByteArrayOutputStream baos=new ByteArrayOutputStream();
 				
 				resXtext.save(baos, saveOptions);
 				String result= baos.toString("UTF-8");
 				return result;
 			} catch (IOException e) {
 				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(),e));
 			}
 			return xmi;
 		}
 
 		private Injector getInjector() {
 			return ExpressionActivator
 					.getInstance().getInjector(
 							ExpressionActivator.ORG_OPENETCS_DSL_EXPRESSION);
 		}
 }
