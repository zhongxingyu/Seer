 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.window.ApplicationWindow;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 
 
 public class EditDialog extends ApplicationWindow {
 
 	private Composite slidesComposite;
 
 	public EditDialog(Shell parentShell) {
 		super(null);
 
 		addToolBar(SWT.FLAT);
 	}
 
 	@Override
 	protected Control createContents(Composite parent) {
		getShell().setSize(300, 300);
 		slidesComposite = (Composite) super.createContents(parent);
 		slidesComposite.setLayout(new RowLayout(SWT.VERTICAL));
 
 		update();
 
 		return slidesComposite;
 	}
 
 	@Override
 	protected Control createToolBarControl(Composite parent) {
 		ToolBar toolbar = (ToolBar) super.createToolBarControl(parent);
 
 		ToolItem addButton = new ToolItem(toolbar, SWT.PUSH);
 		addButton.setText("add");
 		addButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				FileDialog fileDialog = new FileDialog(getShell(), SWT.MULTI
 						| SWT.OPEN);
 				fileDialog.setFilterExtensions(new String[] { "*.jpg" });
 				fileDialog.open();
 
 				List<File> result = new ArrayList<File>();
 				for (String current : fileDialog.getFileNames())
 					result.add(new File(fileDialog.getFilterPath()
 							+ System.getProperty("file.separator") + current));
 
 				Presentation.add(result);
 				update();
 			}
 		});
 
 		ToolItem exitButton = new ToolItem(toolbar, SWT.PUSH);
 		exitButton.setText("Exit");
 		exitButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				close();
 			}
 		});
 		return toolbar;
 	}
 
 	public void update() {
 		for (Control current : slidesComposite.getChildren())
 			current.dispose();
 
 		for (Slide current : Presentation.getSlides())
 			new SlideItem(slidesComposite, SWT.None, current);

		slidesComposite.layout();
 	}
 
 }
