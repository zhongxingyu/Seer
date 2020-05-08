 package ssl.editors.frm;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.ui.part.FileEditorInput;
 
 import ssl.SslPlugin;
 import fdk.proto.PRO;
 
 public class FRMViewer extends EditorPart {
 
     public static final String ID = "ssl.editors.frm.FRMViewer"; //$NON-NLS-1$
 
     private FRMPanel           m_panel;
 
     private FRMList            m_panel1;
 
     public FRMViewer() {}
 
     /**
      * Create contents of the editor part.
      * 
      * @param parent
      */
     @Override
     public void createPartControl(Composite parent) {
         Composite container = new Composite(parent, SWT.NONE);
         container.setLayout(new GridLayout(1, false));
 
         // m_panel = new FRMPanel(container, SWT.NONE);
         // m_panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
         // 1));
         //
         // try {
         // InputStream frmStream = ((FileEditorInput)
         // getEditorInput()).getFile().getContents();
         //
         // InputStream palStream = SslPlugin.getFile(
         // ((FileEditorInput) getEditorInput()).getFile().getProject(),
         // "color.pal")
         // .getContents();
         //
         // m_panel.setImage(frmStream, palStream);
         // frmStream.close();
         // palStream.close();
         // } catch (CoreException e) {
         // e.printStackTrace();
         // } catch (IOException e) {
         // e.printStackTrace();
         // }
 
         m_panel1 = new FRMList(container, SWT.NONE);
         m_panel1.setCellSize(100, 100);
         m_panel1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
         IProject proj = ((FileEditorInput) getEditorInput()).getFile().getProject();
         m_panel1.setPal(SslPlugin.getStdPal(proj));
         ProtoProvider provider = new ProtoProvider(proj);
         m_panel1.setContentProvider(provider);
         m_panel1.setLabelProvider(provider);
        m_panel1.setInput(PRO.Type.TILE);
 
     }
 
     @Override
     public void dispose() {
         super.dispose();
         // m_panel.dispose();
 
     }
 
     @Override
     public void setFocus() {
         m_panel1.setFocus();
     }
 
     @Override
     public void doSave(IProgressMonitor monitor) {}
 
     @Override
     public void doSaveAs() {}
 
     @Override
     public void init(IEditorSite site, IEditorInput input) throws PartInitException {
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
 }
