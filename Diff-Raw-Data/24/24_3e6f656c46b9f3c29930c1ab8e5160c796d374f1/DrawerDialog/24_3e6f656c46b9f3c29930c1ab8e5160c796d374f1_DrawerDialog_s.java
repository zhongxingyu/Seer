 package at.photoselector.ui.drawer;
 
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 
 import at.photoselector.model.Photo;
 import at.photoselector.ui.ControlsDialog;
 import at.photoselector.ui.UncloseableApplicationWindow;
 
 public class DrawerDialog extends UncloseableApplicationWindow {
 
 	
 	private ToolItem showAcceptedButton;
 	private ToolItem showDeclinedButton;
 	private Composite photoListContentComposite;
 	private int boundingBox = 100;
 	private ToolItem showControlsButton;
 
 	public DrawerDialog(Shell parentShell, ControlsDialog dialog) {
 		super(parentShell, dialog);
 
 		addToolBar(SWT.FLAT);
 	}
 
 	@Override
 	protected void configureShell(Shell shell) {
 		super.configureShell(shell);
 
 		shell.setText("Drawer");
 	}
 
 	protected Control createContents(Composite parent) {
 		ToolBar drawerToolbar = (ToolBar) getToolBarControl();
 
 		showAcceptedButton = new ToolItem(drawerToolbar, SWT.CHECK);
 		showAcceptedButton.setText("accepted");
 		showAcceptedButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				controlsDialog.update();
 			}
 		});
 		showDeclinedButton = new ToolItem(drawerToolbar, SWT.CHECK);
 		showDeclinedButton.setText("declined");
 		showDeclinedButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				controlsDialog.update();
 			}
 		});
 
 		ToolItem zoomInButton = new ToolItem(drawerToolbar, SWT.PUSH);
 		zoomInButton.setText("+");
 		zoomInButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 				boundingBox += 50;
 				update();
 			}
 		});
 
 		ToolItem zoomOutButton = new ToolItem(drawerToolbar, SWT.PUSH);
 		zoomOutButton.setText("-");
 		zoomOutButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 				boundingBox -= 50;
 				update();
 			}
 		});
 
 		showControlsButton = new ToolItem(drawerToolbar, SWT.CHECK);
 		showControlsButton.setText("show controls");
 		showControlsButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// photoListContentComposite.layout();
 				photoListContentComposite.redraw();
 			}
 		});
 
 		ScrolledComposite photoListComposite = new ScrolledComposite(parent,
 				SWT.V_SCROLL);
 		photoListComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		photoListContentComposite = new Composite(
 				photoListComposite, SWT.NONE);
 		photoListContentComposite.setBackground(parent.getDisplay()
 				.getSystemColor(SWT.COLOR_DARK_GRAY));
 		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
 		layout.wrap = true;
 		photoListContentComposite.setLayout(layout);
 
 		photoListComposite.setContent(photoListContentComposite);
 		photoListComposite.setExpandHorizontal(true);
 		photoListComposite.setExpandVertical(true);
 
 		photoListContentComposite.addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				getShell().setFullScreen(!getShell().getFullScreen());
 			}
 		});
 
 		update();
 
 		return parent;
 	}
 
 	@Override
 	protected ToolBarManager createToolBarManager(int style) {
 		return super.createToolBarManager(style);
 	}
 
 	@Override
 	public void update() {
 		for (Control current : photoListContentComposite.getChildren())
 			current.dispose();
 		int filter = Photo.UNPROCESSED;
 		if (showAcceptedButton.getSelection())
 			filter |= Photo.ACCEPTED;
 		if (showDeclinedButton.getSelection())
 			filter |= Photo.DECLINED;
 		for (Photo current : Photo.getFiltered(true, filter)) {
 			new ListItem(photoListContentComposite, this, controlsDialog,
 					current);
 		}
 
		// just make sure that there has been a layout calculated before
		photoListContentComposite.getParent().getParent().layout();
		Rectangle r = photoListContentComposite.getParent().getClientArea();
		((ScrolledComposite) photoListContentComposite.getParent())
				.setMinSize(photoListContentComposite.computeSize(r.width,
						SWT.DEFAULT));
		photoListContentComposite.getParent().redraw();

 		photoListContentComposite.layout();
 	}
 
 	public int getBoundingBox() {
 		return boundingBox;
 	}
 
 	public boolean isShowDialogs() {
 		return showControlsButton.getSelection();
 	}
 
 }
