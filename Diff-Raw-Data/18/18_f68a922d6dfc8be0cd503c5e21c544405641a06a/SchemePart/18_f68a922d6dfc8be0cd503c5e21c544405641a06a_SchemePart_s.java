 package de.hswt.hrm.scheme.ui.part;
 
 import java.net.URL;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseWheelListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Slider;
 import org.eclipse.ui.dialogs.FilteredTree;
 import org.eclipse.ui.dialogs.PatternFilter;
 
 import de.hswt.hrm.scheme.model.RenderedComponent;
 import de.hswt.hrm.scheme.ui.ItemClickListener;
 import de.hswt.hrm.scheme.ui.SchemeGrid;
 import de.hswt.hrm.scheme.ui.SchemeGridItem;
 import de.hswt.hrm.scheme.ui.SchemeTreePatternFilter;
 import de.hswt.hrm.scheme.ui.dnd.DragData;
 import de.hswt.hrm.scheme.ui.dnd.DragDataTransfer;
 import de.hswt.hrm.scheme.ui.dnd.GridDragListener;
 import de.hswt.hrm.scheme.ui.dnd.GridDropTargetListener;
 import de.hswt.hrm.scheme.ui.dnd.TreeDragListener;
 import de.hswt.hrm.scheme.ui.tree.ImageTreeModelFactory;
 import de.hswt.hrm.scheme.ui.tree.SchemeTreeLabelProvider;
 import de.hswt.hrm.scheme.ui.tree.TreeContentProvider;
 
 /**
  * This is the Part for the scheme builder frame.
  * 
  * @author Michael Sieger
  *
  */
 public class SchemePart {
	
 	private static final String DELETE = "Löschen";
 	
 	/**
 	 * The DND transfer type
 	 */
 	private static final Transfer[] TRANSFER = new Transfer[] {DragDataTransfer.getInstance()};
 	
 	/**
 	 * The background color of the scheme editor.
 	 */
 	private static final RGB EDITOR_BACKGROUND = new RGB(255, 255, 255);
 	
 	private static final int DRAG_OPS = DND.DROP_COPY, DROP_OPS = DND.DROP_COPY;
 	
 	/**
 	 * The pixel per grid range. Defines how far you can zoom in and out
 	 */
 	private static final int MIN_PPG = 20, MAX_PPG = 70;
 
 	/**
 	 * The topmost gui parent
 	 */
 	private Composite root;
 
 	private SchemeGrid grid;
 	
 	private TreeViewer tree;
 	
 	/**
 	 * The PatternFilter defines which TreeItems are visible for a given search pattern
 	 */
 	private PatternFilter filter;
 	
 	private List<RenderedComponent> comps;
 	
 	/*
 	 * DND items for the grid
 	 */
 	private DragSource gridDragSource;
 	private DropTarget gridDropTarget;
 	
 	private GridDropTargetListener gridListener;
 	private GridDragListener gridDragListener;
 	private TreeDragListener treeDragListener;
 
 	@PostConstruct
 	public void postConstruct(Composite parent) {
 		URL url = SchemePart.class.getResource(
 				SchemePart.class.getSimpleName() + IConstants.XWT_EXTENSION_SUFFIX);
 
 		try {
 			root = (Composite) XWT.load(parent, url);
 		    comps = ImageTreeModelFactory.create(
 		                root.getDisplay()).getImages();
 		    
 			initTree();
 			initSchemeGrid();
 			initGridDropTarget();
 			initGridDragSource();
 			initGridDropTargetListener();
 	        initTreeDND();
 			initGridDND();
 			initSlider();
 			initItemClickListener();
 
 		} catch (Throwable e) {
 			throw new Error("Unable to load ", e);
 		}
 	}
 	
 	/*
 	 * init gui elements
 	 */
 	
 	private void initItemClickListener(){
 		grid.setItemClickListener(new ItemClickListener() {
 			
 			@Override
 			public void itemClicked(final MouseEvent e, final SchemeGridItem item) {
 				if(e.button == 3){		//right mouse click
 					final Menu menu = new Menu(root.getShell(), SWT.POP_UP);
 					final MenuItem delete = new MenuItem(menu, SWT.PUSH);
 					delete.addSelectionListener(new SelectionListener() {
 						
 						@Override
 						public void widgetSelected(SelectionEvent e) {
 							grid.removeItem(item);
 						}
 						
 						@Override
 						public void widgetDefaultSelected(SelectionEvent e) {}
 					});
 					delete.setText(DELETE);
 					menu.setVisible(true);
 				}
 			}
 		});
 	}
 	
 	private void initGridDropTargetListener(){
 		gridListener = new GridDropTargetListener(grid, comps, this);
 		gridDropTarget.addDropListener(gridListener);
 	}
 	
 	private void initGridDragSource(){
         gridDragSource = new DragSource(grid, DRAG_OPS);
         gridDragSource.setTransfer(TRANSFER);
 	}
 	
 	private void initGridDropTarget(){
         gridDropTarget = new DropTarget(grid, DROP_OPS);
         gridDropTarget.setTransfer(TRANSFER);
 	}
 	
 	private void initTreeDND(){
 		treeDragListener = new TreeDragListener(tree, comps, grid);
 		tree.addDragSupport(DRAG_OPS, TRANSFER, treeDragListener);
 	}
 	
 	private void initGridDND(){
 		gridDragListener = new GridDragListener(grid, comps);
 		gridDragSource.addDragListener(gridDragListener);
 	}
 	
 	private void initSchemeGrid(){
 		grid = new SchemeGrid(getSchemeComposite(), SWT.NONE, 40, 20, 40);
 		grid.setBackground(new Color(root.getDisplay(), EDITOR_BACKGROUND));
 		getSchemeComposite().setContent(grid);
 		grid.addMouseWheelListener(new MouseWheelListener() {
 			
 			@Override
 			public void mouseScrolled(MouseEvent e) {
 				addZoom(e.count);
 			}
 		});
 	}
 	
 	private void initTree(){
 		/*
 		 * A FilteredTree cant be created in XWT, so its done here
 		 */
 		filter = new SchemeTreePatternFilter();
 		final FilteredTree filteredTree = new FilteredTree(root, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, 
 				filter, true);
 		filteredTree.getFilterControl().addModifyListener(new ModifyListener() {
 			
 			@Override
 			public void modifyText(ModifyEvent e) {
 				updatePatternFilter(filteredTree.getFilterControl().getText());
 			}
 		});
 		FormData formData = new FormData();
 		formData.top = new FormAttachment(0, 10);
 		formData.left = new FormAttachment(0, 10);
 		formData.right = new FormAttachment(0, 200);
 		formData.bottom = new FormAttachment(100, -50);
 		filteredTree.setLayoutData(formData);
 		tree = filteredTree.getViewer();
 		tree.setContentProvider(new TreeContentProvider());
 		tree.setLabelProvider(new SchemeTreeLabelProvider());
 		tree.setInput(comps);
 	}
 	
 	private void initSlider(){
 		Slider zoomSlider = getZoomSlider();
 		zoomSlider.setMaximum(MAX_PPG);
 		zoomSlider.setMinimum(MIN_PPG);
 		zoomSlider.addSelectionListener(new SelectionListener() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				updateZoom();
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {}
 		});
 		updateZoom();
 	}
 	
 	/*
 	 * setter
 	 */
 	
 	private void addZoom(int amount){
 		Slider slider = getZoomSlider();
 		slider.setSelection(slider.getSelection() + amount);
 		updateZoom();
 	}
 	
 	/*
 	 * getter
 	 */
 
 	private Slider getZoomSlider(){
 		return (Slider) XWT.findElementByName(root, "zoomSlider");
 	}
 
 	private ScrolledComposite getSchemeComposite() {
 		return (ScrolledComposite) XWT.findElementByName(root, "schemeComposite");
 	}
 
 	/*
 	 * updates
 	 */
 	
 	private void updateZoom(){
 		grid.setPixelPerGrid(getZoomSlider().getSelection());
 	}
 	
 	private void updatePatternFilter(String text){
 		filter.setPattern(text);
 		tree.refresh();
 		if(text.trim().length() == 0){
 			tree.collapseAll();
 		}else{
 			tree.expandAll();
 		}
 	}
 
     public int getRenderedComponentId(RenderedComponent comp) {
         return comps.indexOf(comp);
     }
 
     public RenderedComponent getRenderedComponent(int id) {
         return comps.get(id);
     }
 
 	public DragData getDraggingItem() {
 		DragData i1 = gridDragListener.getDraggingItem();
 		DragData i2 = treeDragListener.getDraggingItem();
 		if(i1 == null && i2 == null){
 			throw new RuntimeException("No drag and drop is running");
 		}
 		if(i1 != null){
 			return i1;
 		}else{
 			return i2;
 		}
 	}
 
 }
