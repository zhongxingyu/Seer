 package at.photoselector.ui;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 
 import at.photoselector.Workspace;
 import at.photoselector.tools.Exporter;
 import at.photoselector.ui.drawer.DrawerDialog;
 import at.photoselector.ui.stages.StagesDialog;
 import at.photoselector.ui.table.TableDialog;
 
 public class ControlsDialog extends MyApplicationWindow {
 
 	private StagesDialog stagesDialog;
 	private DrawerDialog drawerDialog;
 	private TableDialog tableDialog;
 
 	public ControlsDialog(Shell parentShell) {
 		super(parentShell);
 	}
 
 	@Override
 	protected void configureShell(Shell shell) {
 		super.configureShell(shell);
 
 		shell.setText("PhotoSelector");
 	}
 
 	@Override
 	protected Control createContents(final Composite parent) {
 		// create other windows
 		final Display display = getShell().getDisplay();
 		stagesDialog = new StagesDialog(new Shell(display), this);
 		display.asyncExec(stagesDialog);
 
 		drawerDialog = new DrawerDialog(new Shell(display), this);
 		display.asyncExec(drawerDialog);
 
 		tableDialog = new TableDialog(new Shell(display), this);
 		display.asyncExec(tableDialog);
 
 		// create controls
 		Composite controlComposite = new Composite(parent, SWT.NONE);
 		controlComposite.setLayout(new RowLayout(SWT.VERTICAL));
 
 		Button switchWorkspaceButton = new Button(controlComposite, SWT.PUSH);
 		switchWorkspaceButton.setText("Switch Workspace");
 		switchWorkspaceButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				SelectWorkspaceDialog selectWorkspaceDialog = new SelectWorkspaceDialog(
 						getShell());
 				display.syncExec(selectWorkspaceDialog);
 				if (Dialog.CANCEL != selectWorkspaceDialog.getReturnCode())
 					update();
 			}
 		});
 
 		Button addPhotosButton = new Button(controlComposite, SWT.PUSH);
 		addPhotosButton.setText("Add photos");
 		addPhotosButton
				.setToolTipText("exiftool \"-FileName<DateTimeOriginal\" -d \"%Y%m%d_%H%M%S.%%e\" .");
 		addPhotosButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				DirectoryDialog dialog = new DirectoryDialog(getShell(),
 						SWT.OPEN);
 				Workspace.addPhoto(dialog.open());
 
 				update();
 			}
 		});
 
 		final Menu exportMenu = new Menu(getShell(), SWT.POP_UP);
 		for (Exporter current : Exporter.getAvailable()) {
 			MenuItem menuItem = new MenuItem(exportMenu, SWT.NONE);
 			menuItem.setText(current.getName());
 			menuItem.addListener(SWT.Selection, new ExportSelectionListener(
 					current));
 		}
 
 		Button exportButton = new Button(controlComposite, SWT.PUSH);
 		exportButton.setText("Export");
 		exportButton
 				.setToolTipText("exiftool '-filename<DateTimeOriginal' -d %y%m%d_%H%M%S%%-c.%%le .");
 		exportButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				Rectangle rect = ((Control) e.widget).getBounds();
 				Point pt = new Point(rect.x, rect.y + rect.height);
 				pt = ((Control) e.widget).getParent().toDisplay(pt);
 				exportMenu.setLocation(pt.x, pt.y);
 				exportMenu.setVisible(true);
 			}
 		});
 
 		Button settingsButton = new Button(controlComposite, SWT.PUSH);
 		settingsButton.setText("Settings");
 		settingsButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				new SettingsDialog(getShell()).open();
 			}
 		});
 
 		Button exitButton = new Button(controlComposite, SWT.PUSH);
 		exitButton.setText("Exit");
 		exitButton.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				close();
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		// update();
 
 		return controlComposite;
 	}
 
 	@Override
 	public boolean close() {
 		stagesDialog.closeApplication();
 		drawerDialog.closeApplication();
 		tableDialog.closeApplication();
 		return super.close();
 	}
 
 	@Override
 	public void update() {
 		stagesDialog.update();
 		drawerDialog.update();
 		tableDialog.update();
 	}
 
 	public void updateAllBut(UncloseableApplicationWindow dialog) {
 		if (!stagesDialog.equals(dialog))
 			stagesDialog.update();
 		if (!drawerDialog.equals(dialog))
 			drawerDialog.update();
 		if (!tableDialog.equals(dialog))
 			tableDialog.update();
 	}
 
 	private class ExportSelectionListener implements Listener {
 
 		private Exporter myExporter;
 
 		public ExportSelectionListener(Exporter exporter) {
 			myExporter = exporter;
 		}
 
 		public void handleEvent(Event event) {
 			myExporter.run();
 		}
 
 	}
 }
