 package at.photoselector.ui.table;
 
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 
 import at.photoselector.model.Photo;
 import at.photoselector.ui.ControlsDialog;
 import at.photoselector.ui.UncloseableApplicationWindow;
 
 public class TableDialog extends UncloseableApplicationWindow {
 
 	public TableDialog(Shell parentShell, ControlsDialog dialog) {
 		super(parentShell, dialog);
 	}
 
 	@Override
 	protected void configureShell(Shell shell) {
 		super.configureShell(shell);
 
 		shell.setText("Table");
 	}
 
 	@Override
 	protected Control createContents(final Composite parent) {
 		parent.setBackground(new Color(parent.getDisplay(), 75, 75, 75));
 
 		DropTarget target = new DropTarget(parent, DND.DROP_MOVE
 				| DND.DROP_COPY | DND.DROP_LINK);
 		target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
 		target.addDropListener(new DropTargetAdapter() {
 			public void drop(DropTargetEvent event) {
 				if (event.data == null) {
 					event.detail = DND.DROP_NONE;
 					return;
 				}
 
 				for (Control current : getShell().getChildren()) {
 					if (current instanceof ImageTile)
 						if (((ImageTile) current)
 								.getPhoto()
 								.getPath()
 								.getAbsolutePath()
 								.equalsIgnoreCase(
 										Photo.get(
 												Integer.valueOf((String) event.data))
 												.getPath().getAbsolutePath()))
 							return;
 				}
 				
 				new ImageTile(parent, controlsDialog, Photo.get(Integer
 						.valueOf((String) event.data)),
 						event.x, event.y);
 			}
 		});
 
 		parent.addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				getShell().setFullScreen(!getShell().getFullScreen());
 			}
 		});
 
 		return parent;
 	}
 
 	@Override
 	public void update() {
 		/*
 		 * TODO if accept/decline is available in drawer also, we might have to
 		 * remove some photos from the table
 		 */
 
 	}
 }
