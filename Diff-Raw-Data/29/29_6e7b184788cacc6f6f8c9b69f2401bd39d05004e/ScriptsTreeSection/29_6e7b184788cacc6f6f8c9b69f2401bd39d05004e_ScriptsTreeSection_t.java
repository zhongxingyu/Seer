 package org.zend.php.zendserver.deployment.ui.editors;
 
import java.util.List;

 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.forms.editor.FormEditor;
 import org.eclipse.ui.forms.widgets.FormToolkit;
import org.zend.php.zendserver.deployment.core.descriptor.IDeploymentDescriptor;
 import org.zend.php.zendserver.deployment.core.descriptor.IDescriptorContainer;
 import org.zend.php.zendserver.deployment.ui.Messages;
import org.zend.sdklib.mapping.IMapping;
import org.zend.sdklib.mapping.IMappingChangeEvent;
import org.zend.sdklib.mapping.IMappingEntry;
import org.zend.sdklib.mapping.IMappingEntry.Type;
 import org.zend.sdklib.mapping.IMappingModel;
 
 public class ScriptsTreeSection extends PropertiesTreeSection {
 
 	public ScriptsTreeSection(FormEditor editor, Composite parent,
 			FormToolkit toolkit, IDescriptorContainer model) {
 		super(editor, parent, toolkit, model);
 		setText(Messages.ScriptsTreeSection_ScriptsMapping);
 		setDescription(Messages.ScriptsTreeSection_Info);
 	}
 
 	@Override
 	protected String getFolder() {
 		return IMappingModel.SCRIPTSDIR;
 	}
	
	@Override
	public void mappingChanged(IMappingChangeEvent event) {
		super.mappingChanged(event);
		IMappingModel mappingModel = model.getMappingModel();
		IMappingEntry entry = mappingModel.getEntry(IMappingModel.SCRIPTSDIR, Type.INCLUDE);
		if (entry != null) {
			List<IMapping> mappings = entry.getMappings();
			if (mappings != null && mappings.size() > 0) {
				IDeploymentDescriptor descriptor = model.getDescriptorModel();
				if (descriptor.getScriptsRoot() == null
						|| descriptor.getScriptsRoot().isEmpty()) {
					descriptor.setScriptsRoot("scripts"); //$NON-NLS-1$
				}
			}
		}
	}
	
	
 
 }
