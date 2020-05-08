 package de.chesmuh.ordo.gui.composites;
 
 import java.util.Collection;
 import java.util.ResourceBundle;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DragSourceAdapter;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 import de.chesmuh.ordo.config.Config;
 import de.chesmuh.ordo.data.DataAccess;
 import de.chesmuh.ordo.entitys.Museum;
 import de.chesmuh.ordo.entitys.Section;
 import de.chesmuh.ordo.gui.MainFrame;
 import de.chesmuh.ordo.gui.interfaces.UiEvent;
 import de.chesmuh.ordo.gui.interfaces.UiEventType;
 import de.chesmuh.ordo.gui.resources.OrdoUI;
 import de.chesmuh.ordo.gui.resources.ResourceManager;
 
 public class TreeComposite extends Composite {
 
 	private Tree tree;
 	private ResourceBundle bundle;
 
 	public TreeComposite(Composite parent, int style) {
 		super(parent, style);
 		bundle = Config.getInstance().getUIBundle();
 		this.initialize();
 	}
 
 	private void initialize() {
 		// ----- Layout -----
 		this.setLayout(new GridLayout(1, true));
 
 		Group group = new Group(this, SWT.NONE);
 		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		group.setLayout(new GridLayout(1, true));
 		group.setText(bundle.getString(OrdoUI.SECTION_GROUP_TITLE));
 
 		// ----- Buttons -----
 		ToolBar toolBar = new ToolBar(group, SWT.NONE);
 		ToolItem toolItemAdd = new ToolItem(toolBar, SWT.PUSH);
 		toolItemAdd.setImage(ResourceManager.getImage(getDisplay(),
 				OrdoUI.IMAGES_ADD));
 		toolItemAdd.addSelectionListener(new AddSectionAdapter());
 		ToolItem toolItemRemove = new ToolItem(toolBar, SWT.PUSH);
 		toolItemRemove.setImage(ResourceManager.getImage(getDisplay(),
 				OrdoUI.IMAGES_REMOVE));
 		toolItemRemove.addSelectionListener(new RemoveSectionAdapter());
 
 		// ----- Tree -----
 		tree = new Tree(group, SWT.V_SCROLL);
 		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		tree.addListener(SWT.Selection, new TreeListener());
 
 		// ----- DragSource -----
 		DragSource dragSource = new DragSource(tree, DND.DROP_MOVE);
 		dragSource.setTransfer(new Transfer[] { TextTransfer.getInstance() });
 		dragSource.addDragListener(new TreeDragSourceAdapter());
 
 		refreshTree();
 	}
 
 	private void refreshTree() {
 		Collection<Museum> allMuseum = DataAccess.getInstance().getAllMuseum();
 		for (Museum museum : allMuseum) {
 			TreeItem museumItem = new TreeItem(tree, SWT.NONE);
 			museumItem.setData(museum);
 			museumItem.setText(museum.getName());
 			museumItem.setImage(ResourceManager.getImage(getDisplay(),
 					OrdoUI.IMAGES_MUSEUM));
 
 			Collection<Section> allSection = DataAccess.getInstance()
 					.getSectionByMuseumWithNoParent(museum);
 			for (Section section : allSection) {
 				TreeItem sectionItem = new TreeItem(museumItem, SWT.NONE);
 				sectionItem.setData(section);
 				sectionItem.setText(section.getName());
 				sectionItem.setImage(ResourceManager.getImage(getDisplay(),
 						OrdoUI.IMAGES_SECTION));
 				addSubSection(sectionItem);
 			}
 		}
 
 		tree.pack();
 	}
 
 	private void addSubSection(TreeItem item) {
 		Section section = (Section) item.getData();
 		Collection<Section> allSection = DataAccess.getInstance()
 				.getSectionBySection(section);
 		for (Section subSection : allSection) {
 			TreeItem subItem = new TreeItem(item, SWT.NONE);
 			subItem.setData(subSection);
 			subItem.setText(subSection.getName());
 			subItem.setImage(ResourceManager.getImage(getDisplay(),
 					OrdoUI.IMAGES_SECTION));
 			addSubSection(subItem);
 		}
 	}
 
 	private class TreeListener implements Listener {
 
 		@Override
 		public void handleEvent(Event event) {
 			TreeItem selection = tree.getSelection()[0];
 			if (selection.getData() instanceof Section) {
 				UiEvent uiEvent = new UiEvent(tree, selection.getData(),
 						UiEventType.SectionChoose);
 				MainFrame.handleEvent(uiEvent);
 			} else if (selection.getData() instanceof Museum) {
 				UiEvent uiEvent = new UiEvent(tree, selection.getData(),
 						UiEventType.MuseumChoose);
 				MainFrame.handleEvent(uiEvent);
 			}
 		}
 
 	}
 
 	private class AddSectionAdapter extends SelectionAdapter {
 
 		@Override
 		public void widgetSelected(SelectionEvent e) {
			UiEvent event;
			if(tree.getSelection().length > 0) {
				TreeItem treeItem = tree.getSelection()[0];
			 event = new UiEvent(null, treeItem.getData(),
 					UiEventType.AddSection);
			} else {
				event = new UiEvent(null, null, UiEventType.AddSection);
			}
 			MainFrame.handleEvent(event);
 		}
 
 	}
 
 	private class RemoveSectionAdapter extends SelectionAdapter {
 
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			TreeItem treeItem = tree.getSelection()[0];
 			UiEvent event = new UiEvent(null, treeItem.getData(),
 					UiEventType.RemoveSection);
 			MainFrame.handleEvent(event);
 		}
 
 	}
 
 	private class TreeDragSourceAdapter extends DragSourceAdapter {
 
 		@Override
 		public void dragSetData(DragSourceEvent event) {
 			StringBuilder stringBuilder = new StringBuilder();
 			Object object = tree.getSelection()[0].getData();
 			if (object instanceof Section) {
 				stringBuilder.append("section/");
 				stringBuilder.append(Long.toString(((Section)object).getId()));
 			} else if (object instanceof Museum) {
 				stringBuilder.append("museum/");
 				stringBuilder.append(Long.toString(((Museum)object).getId()));
 			} else {
 				return;
 			}			
 			event.data = stringBuilder.toString();
 		}
 
 	}
 
 }
