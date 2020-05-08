 package itemfiler.ui;
 
 import itemfiler.model.Item;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Tracker;
 
 
 public class ObjectList extends Refreshable {
 
 	private Composite listComposite;
 	private MainWindow mainWindow;
 
	public ObjectList(Composite parent, int style, MainWindow mainW) {
 		super(parent, style);
 
		this.mainWindow = mainW;
 
 		this.setLayout(new FillLayout());
 
 		ScrolledComposite scrolledListComposite = new ScrolledComposite(this,
 				SWT.V_SCROLL);
 		scrolledListComposite.setExpandHorizontal(true);
 		scrolledListComposite.setExpandVertical(true);
 
 		listComposite = new Composite(scrolledListComposite, SWT.NONE);
 		scrolledListComposite.setContent(listComposite);
 
 		listComposite.setLayout(new RowLayout());
 		listComposite.setBackground(Display.getCurrent().getSystemColor(
 				SWT.COLOR_LIST_BACKGROUND));
 
 		listComposite.addPaintListener(new PaintListener() {
 
 			@Override
 			public void paintControl(PaintEvent e) {
 				Rectangle r = listComposite.getParent().getClientArea();
 				((ScrolledComposite) listComposite.getParent())
 						.setMinSize(listComposite.computeSize(r.width,
 								SWT.DEFAULT));
 			}
 		});
 
 		listComposite.addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mouseDown(MouseEvent e) {
 				final Tracker tracker = new Tracker(listComposite, SWT.RESIZE);
 
 				tracker.setRectangles(new Rectangle[] { new Rectangle(e.x, e.y,
 						1, 1), });
 				tracker.open();
 
 				Rectangle result = tracker.getRectangles()[0];
 				for (Control current : listComposite.getChildren()) {
 					if (current instanceof ListItem) {
 						if (result.contains(
 								current.getLocation().x
 										+ current.getBounds().width / 2,
 								current.getLocation().y
										+ current.getBounds().height / 2)) {
							mainWindow.addSelected(((ListItem) current)
									.getObject());
 							((ListItem) current).setSelected(true);
						}
 					}
 				}
 			}
 		});
 
 		refresh();
 	}
 
 	public void refresh() {
 		for (Control current : listComposite.getChildren())
 			current.dispose();
 
 		RowData layoutData = new RowData(110, 110);
 
 		for (final Item current : Item.getFiltered(mainWindow.getFilter())) {
 			final ListItem tmp = new ListItem(listComposite, SWT.NONE, current);
 			tmp.setLayoutData(layoutData);
 			try {
 				if (mainWindow.getSelected().contains(current))
 					tmp.setSelected(true);
 			} catch (NullPointerException e) {
 				// nothing selected
 			}
 			tmp.addMouseListener(new MouseAdapter() {
 
 				@Override
 				public void mouseDown(MouseEvent e) {
 					ImageViewer.showItem(current);
 
 					if (tmp.getSelected() && (e.stateMask & SWT.CTRL) > 0) {
 						mainWindow.removeSelected(current);
 						tmp.setSelected(false);
 					} else if ((e.stateMask & SWT.CTRL) > 0) {
 						mainWindow.addSelected(current);
 						tmp.setSelected(true);
 					} else {
 						mainWindow.setSelected(current);
 						for (Control currentItem : listComposite.getChildren())
 							if (currentItem instanceof ListItem)
 								((ListItem) currentItem).setSelected(false);
 						tmp.setSelected(true);
 					}
 				}
 
 				@Override
 				public void mouseDoubleClick(MouseEvent e) {
 					ImageViewer.show();
 					ImageViewer.showItem(current);
 				}
 			});
 		}
 
 		listComposite.layout();
 		this.layout();
 	}
 }
