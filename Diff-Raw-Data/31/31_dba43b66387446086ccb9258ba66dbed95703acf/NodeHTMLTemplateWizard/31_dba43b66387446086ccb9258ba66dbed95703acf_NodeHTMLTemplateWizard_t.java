 package com.netxforge.netxstudio.models.export.ui;
 
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IWorkbench;
 
 import com.netxforge.netxstudio.models.export.XpandTemplate;
import com.netxforge.netxstudio.models.export.impl.XpandNodeToHtml;
import com.netxforge.netxstudio.operators.Node;
 
 public class NodeHTMLTemplateWizard extends AbstractModelExportWizard {
 		
 	@Override
 	public boolean performFinish() {
 		return super.performFinish();
 	}
 
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.selection = selection;
		setWindowTitle("Export a Network Element instance to HTML");
 		setNeedsProgressMonitor(true);
 		
 	}
 
	Node n = null;
 	
 	@Override
 	public XpandTemplate getTargetTemplate() {
		XpandTemplate template = new XpandNodeToHtml();
 		// For object specific exports. 
 		if(selection != null){
 			Object o = selection.getFirstElement();
			if(o instanceof Node){
				n = (Node) o; 
 			}
 		}
		template.setTargetObject(n);
 		return template;
 	}
 
 }
