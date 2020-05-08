 package org.zend.php.zendserver.deployment.ui.editors;
 
 import java.util.Arrays;
 import java.util.Map;
 
 import org.eclipse.jface.viewers.ILabelDecorator;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.swt.graphics.Image;
 import org.zend.php.zendserver.deployment.core.descriptor.DeploymentDescriptorFactory;
 import org.zend.php.zendserver.deployment.core.descriptor.IDeploymentDescriptor;
 import org.zend.php.zendserver.deployment.core.descriptor.IModelObject;
 import org.zend.php.zendserver.deployment.core.internal.descriptor.Feature;
 import org.zend.php.zendserver.deployment.ui.editors.DescriptorEditorPage.FormDecoration;
 
 public class DeploymentDescriptorLabelDecorator implements ILabelDecorator {
 
 	private DeploymentDescriptorEditor editor;
 
 	public DeploymentDescriptorLabelDecorator(DeploymentDescriptorEditor editor) {
 		this.editor = editor;
 	}
 	
 	public Image decorateImage(Image image, Object element) {
 		if (element instanceof IModelObject) {
 			IModelObject imo = (IModelObject) element;
 			IDeploymentDescriptor imc = editor.getModel();
 			int index = -1;
 			if (imc != null) {
 				Feature f = DeploymentDescriptorFactory.getFeature(imo);
 				index = imc.getChildren(f).indexOf(imo);
 			}
 			
 			Map<Feature, FormDecoration> decorations = null;
 			if (index != -1) {
 				decorations = editor.getDecorationsForFeatures(Arrays.asList(imo.getPropertyNames()), index);
 			} else {
 				decorations = editor.getDecorationsForFeatures(Arrays.asList(imo.getPropertyNames()));
 			}
 			
 			if (decorations.size() > 0) {
				return new ErrorImageComposite(image).createImage(); // TODO leaks?
 			}
 		}
 		
 		return null;
 	}
 
 	public String decorateText(String text, Object element) {
 		return null;
 	}
 
 	public void addListener(ILabelProviderListener listener) {
 	}
 
 	public void dispose() {
 	}
 
 	public boolean isLabelProperty(Object element, String property) {
 		return false;
 	}
 
 	public void removeListener(ILabelProviderListener listener) {
 	}
 }
