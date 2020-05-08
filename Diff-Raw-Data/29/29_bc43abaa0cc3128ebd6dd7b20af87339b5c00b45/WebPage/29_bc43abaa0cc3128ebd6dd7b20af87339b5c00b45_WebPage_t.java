 package com.traindirector.editors;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.browser.LocationEvent;
 import org.eclipse.swt.browser.LocationListener;
 import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 
 import com.traindirector.uicomponents.WebContent;
 
 public class WebPage extends EditorPart {
 
 	Browser _browser;
 	WebContent _content;
 	
 	public WebPage() {
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 	}
 
 	@Override
 	public void doSaveAs() {
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
 		this.setSite(site);
 		this.setInput(input);
 	}
 
 	@Override
 	public boolean isDirty() {
 		return false;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 
 		_browser = new Browser(parent, SWT.NONE);
 		_browser.setText("<html><body>Hello browser!</body></html>\n");
 
 		_browser.addLocationListener(new LocationListener() {
 			
 			@Override
 			public void changing(LocationEvent event) {
 			}
 			
 			@Override
 			public void changed(LocationEvent event) {
				if(_content.doLink(event.location)) {
					event.doit = false;
					Display.getDefault().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							String body = _content.getHTML();
							_browser.setText(body);
						}
					});
				}
 			}
 		});
 		_content = getContentProvider();
 		if(_content != null) {
 			String body = _content.getHTML();
 			_browser.setText(body);
 		}
 	}
 
 	public WebContent getContentProvider() {
 		return null;
 	}
 
 }
