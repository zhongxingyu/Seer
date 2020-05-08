 package presenter.ui.editor;
 import java.io.File;
 
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 
 import presenter.model.PdfNotes;
 import presenter.model.Presentation;
 import presenter.model.Slide;
 
 
 public class SlideItem extends Composite {
 
 	private Slide mySlide;
 	private EditDialog myDialog;
 	private Menu dropMenu;
 
 	public SlideItem(Composite parent, int style, Slide slide,
 			EditDialog editDialog) {
 		super(parent, style | SWT.BORDER);
 		mySlide = slide;
 		myDialog = editDialog;
 
 		this.setLayout(new GridLayout(2, false));
 
 		// TODO find appropriate aspect ratio
 		int width = myDialog.getTileWidth();
 		this.setLayoutData(new RowData(width, width / 2));
 		this.setBackground(parent.getDisplay().getSystemColor(
 				SWT.COLOR_DARK_GRAY));
 
 		ToolBar toolbar = new ToolBar(this, SWT.FLAT);
 		GridData layout = new GridData();
 		layout.horizontalSpan = 2;
 		toolbar.setLayoutData(layout);
 
 		ToolItem addNotesButton = new ToolItem(toolbar, SWT.DROP_DOWN);
 		addNotesButton.setText("add notes");
 		addNotesButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (dropMenu == null) {
 					dropMenu = new Menu(getShell(), SWT.POP_UP);
 					getShell().setMenu(dropMenu);
 
 					MenuItem addTextNotesItem = new MenuItem(dropMenu, SWT.PUSH);
 					addTextNotesItem.setText("add text notes");
 					addTextNotesItem
 							.addSelectionListener(new SelectionAdapter() {
 								@Override
 								public void widgetSelected(SelectionEvent e) {
 								}
 
 							});
 					MenuItem addNotesFileItem = new MenuItem(dropMenu, SWT.PUSH);
 					addNotesFileItem.setText("add notes file");
 					addNotesFileItem
 							.addSelectionListener(new SelectionAdapter() {
 
 								@Override
 								public void widgetSelected(SelectionEvent e) {
 									FileDialog fileDialog = new FileDialog(
 											getShell());
									fileDialog
											.setFilterExtensions(new String[] { "*.jpg;*.pdf" });

									// open dialog
									String result = fileDialog.open();

									// and return if cancelled
									if ("".equals(result))
										return;

									File notesFile = new File(result);
 
 									if (notesFile.getName().endsWith(".pdf")) {
 										InputDialog dialog = new InputDialog(
 												getShell(),
 												"",
 												"please select the pdf page number",
 												"1", null);
 										dialog.open();
 										// dialog.getValue()
 										mySlide.addNotes(new PdfNotes(
 												notesFile, Integer
 														.valueOf(dialog
 																.getValue())));
 										
 									} else
 										mySlide.addNotesFromFile(notesFile);
 
 									myDialog.update();
 								}
 							});
 				}
 
 				final ToolItem toolItem = (ToolItem) e.widget;
 				final ToolBar toolBar = toolItem.getParent();
 				Rectangle rect = toolItem.getBounds();
 				Point point = toolBar.toDisplay(new Point(rect.x, rect.y
 						+ rect.height));
 				dropMenu.setLocation(point.x, point.y);
 				dropMenu.setVisible(true);
 			}
 		});
 
 		ToolItem upButton = new ToolItem(toolbar, SWT.PUSH);
 		upButton.setText("up");
 		upButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				Presentation.getEditor().moveUp(mySlide);
 				myDialog.update();
 			}
 		});
 
 		ToolItem downButton = new ToolItem(toolbar, SWT.PUSH);
 		downButton.setText("down");
 		downButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				Presentation.getEditor().moveDown(mySlide);
 				myDialog.update();
 			}
 		});
 
 		ToolItem removeButton = new ToolItem(toolbar, SWT.PUSH);
 		removeButton.setText("x");
 		removeButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				Presentation.getEditor().remove(mySlide);
 				myDialog.update();
 			}
 		});
 
 		final Label slideLabel = new Label(this, SWT.CENTER);
 		slideLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 				true));
 		slideLabel.setBackground(new Color(getDisplay(), 50, 50, 50));
 
 		Display.getCurrent().asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 
 				Image src = (Image) mySlide.getContent();
 				Point dimensions = resize(src.getBounds(),
 						slideLabel.getBounds());
 
 				final Image target = new Image(getDisplay(), dimensions.x,
 						dimensions.y);
 
 				GC gc = new GC(target);
 				gc.setAntialias(SWT.ON);
 				gc.setInterpolation(SWT.HIGH);
 				gc.drawImage(src, 0, 0, src.getBounds().width,
 						src.getBounds().height, 0, 0, dimensions.x,
 						dimensions.y);
 
 				slideLabel.setImage(target);
 				slideLabel.redraw();
 			}
 		});
 
 		final Label notesLabel = new Label(this, SWT.CENTER);
 		notesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 				true));
 		notesLabel.setBackground(new Color(getDisplay(), 50, 50, 50));
 
 		Display.getCurrent().asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				Image src = (Image) mySlide.getNotes().getContent();
 
 				Point dimensions = resize(src.getBounds(),
 						notesLabel.getBounds());
 				final Image target = new Image(getDisplay(), dimensions.x,
 						dimensions.y);
 
 				GC gc = new GC(target);
 				gc.setAntialias(SWT.ON);
 				gc.setInterpolation(SWT.HIGH);
 				gc.drawImage(src, 0, 0, src.getBounds().width,
 						src.getBounds().height, 0, 0, dimensions.x,
 						dimensions.y);
 
 				notesLabel.setImage(target);
 				notesLabel.redraw();
 			}
 		});
 
 	}
 
 	private Point resize(Rectangle src, Rectangle target) {
 		Point result = new Point(target.width, target.height);
 		if (src.width > src.height)
 			result.y = (int) ((double) target.width / src.width * src.height);
 		else
 			result.x = (int) ((double) target.width / src.height * src.width);
 
 		return result;
 	}
 }
