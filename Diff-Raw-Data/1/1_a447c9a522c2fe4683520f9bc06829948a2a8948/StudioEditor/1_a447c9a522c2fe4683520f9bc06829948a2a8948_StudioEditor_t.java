 package org.nuxeo.ide.studio.editors;
 import java.io.File;
 import java.net.URL;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.browser.TitleEvent;
 import org.eclipse.swt.browser.TitleListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.EditorPart;
 import org.nuxeo.ide.studio.StudioPlugin;
 import org.nuxeo.ide.studio.data.Node;
 import org.nuxeo.ide.studio.views.StudioBrowserView;
 
 
 public class StudioEditor extends EditorPart {
 
     public static final String ID = "org.nuxeo.ide.studio.editor";
 
     protected Browser browser;
 
     public StudioEditor() {
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
         setSite(site);
         setInput(input);
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
     public void createPartControl(Composite parent) {
         parent.setLayout(new FillLayout());
         browser = new Browser(parent, SWT.NONE);
         Node node = (Node) getEditorInput().getAdapter(Node.class);
         URL url = StudioPlugin.getPreferences().getConnectLocation("ide", "dev", node.getKey());
         StudioPlugin.logInfo("-> " + url);
         browser.setUrl(url.toExternalForm());
         browser.addTitleListener(new TitleListener() {
             public void changed(TitleEvent event) {
                 //id:type:saved!
                 //id:type:created!
                 if (event.title.endsWith(":saved!")) {
                     StudioPlugin.logInfo("<- save done");
                     IWorkbenchWindow window=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                     IWorkbenchPage page = window.getActivePage();
                     IViewPart view = page.findView(StudioBrowserView.ID);
                     if (view != null) {
                         StudioBrowserView studioBrowserView = (StudioBrowserView)view;
                         File file = StudioPlugin.getDefault().getProvider().getJar(studioBrowserView.getProjectName());
                         System.out.println(file);
                     }
                 } else if (event.title.endsWith(":created!")) {
                     String id = null;
                     String[] s = event.title.split(":");
                     if ( s.length > 0 ){
                         id = s[0];
                     }
 
                     StudioPlugin.logInfo("<- create done");
                     IWorkbenchWindow window=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                     IWorkbenchPage page = window.getActivePage();
                     IViewPart view = page.findView(StudioBrowserView.ID);
                     if (view != null) {
                         StudioBrowserView studioBrowserView = (StudioBrowserView)view;
                         studioBrowserView.refresh();
 
                         if ( id != null) {
                             Node node = studioBrowserView.selectNode(id);
                             if ( node != null ){
                                 setInputWithNotify(new StudioEditorInput(node));
                                setPartName("todo");
                             }
                         }
 
                     }
                 }
             }
         });
     }
 
     @Override
     public void setFocus() {
 
     }
 
     @Override
     public String getPartName() {
         return getEditorInput().getName();
     }
 
     public Browser getBrowser() {
         return browser;
     }
 
 }
