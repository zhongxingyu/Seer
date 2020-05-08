 package com.netxforge.netxstudio.models.export.ui;
 
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IWorkbench;
 
import com.netxforge.netxstudio.library.NodeType;
 import com.netxforge.netxstudio.models.export.XpandTemplate;
import com.netxforge.netxstudio.models.export.impl.XpandNodeTypeToHtml;
 
 public class NodeHTMLTemplateWizard extends AbstractModelExportWizard {
 		
 	@Override
 	public boolean performFinish() {
 		return super.performFinish();
 	}
 
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.selection = selection;
		setWindowTitle("Export a Network Element to HTML");
 		setNeedsProgressMonitor(true);
 		
 	}
 
	NodeType nt = null;
 	
 	@Override
 	public XpandTemplate getTargetTemplate() {
		XpandTemplate template = new XpandNodeTypeToHtml();
 		// For object specific exports. 
 		if(selection != null){
 			Object o = selection.getFirstElement();
			if(o instanceof NodeType){
				nt = (NodeType) o; 
 			}
 		}
		template.setTargetObject(nt);
 		return template;
 	}
 
 }
