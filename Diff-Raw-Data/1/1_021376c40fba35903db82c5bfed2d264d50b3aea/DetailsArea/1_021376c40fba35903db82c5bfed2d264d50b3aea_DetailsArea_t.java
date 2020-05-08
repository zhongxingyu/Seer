 package itemfiler.ui;
 
 import itemfiler.model.Item;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 
 public class DetailsArea extends Refreshable {
 
 	private Text nameText;
 	private Composite tagsContainer;
 	private MainWindow mainWindow;
 	private Composite nothingSelectedComposite;
 	private Composite itemsSelectedComposite;
 
 	public DetailsArea(Composite parent, int style, MainWindow main) {
 		super(parent, style | SWT.BORDER);
 
 		mainWindow = main;
 
 		this.setLayout(new GridLayout());
 
 		nothingSelectedComposite = new Composite(this, SWT.NONE);
 		nothingSelectedComposite
 .setLayoutData(new GridData(
 				GridData.FILL_HORIZONTAL));
 		nothingSelectedComposite.setLayout(new FillLayout());
 		Label nothingSelectedLabel = new Label(nothingSelectedComposite,
 				SWT.CENTER);
 		nothingSelectedLabel.setText("- nothing selected -");
 
 		itemsSelectedComposite = new Composite(this, SWT.NONE);
 		itemsSelectedComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		itemsSelectedComposite.setVisible(false);
 		itemsSelectedComposite.setLayout(new GridLayout(1, false));
 
 		Label nameLabelLabel = new Label(itemsSelectedComposite, SWT.NONE);
 		nameLabelLabel.setText("Name: ");
 		nameText = new Text(itemsSelectedComposite, SWT.BORDER);
 		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		nameText.addKeyListener(new KeyAdapter() {
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				for (Item current : mainWindow.getSelected())
 					current.setName(nameText.getText());
 			}
 		});
 
 		Label tagsContainerLabel = new Label(itemsSelectedComposite, SWT.NONE);
 		tagsContainerLabel.setText("Tags: ");
 		tagsContainer = new Composite(itemsSelectedComposite, SWT.NONE);
 		tagsContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		GridLayout layout = new GridLayout(2, false);
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		tagsContainer.setLayout(layout);
 		Button addTagButton = new Button(itemsSelectedComposite, SWT.PUSH);
 		addTagButton.setText("add tags");
 		addTagButton.setToolTipText("add new tags");
 		addTagButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				AddTagDialog dialog = new AddTagDialog(getShell(), mainWindow
 						.getSelected());
 				dialog.open();
 				mainWindow.refresh();
 			}
 		});
 
 		this.layout();
 	}
 
 	public void refresh() {
 		try {
 			Set<String> commonTags = null;
 			String name = null;
 			for (Item current : mainWindow.getSelected()) {
 				List<String> tags = current.getTags();
 				if (null == commonTags) {
 					commonTags = new HashSet<String>();
 					commonTags.addAll(tags);
 				} else
 					commonTags.retainAll(tags);
 
 				if (null == name)
 					name = current.getName();
 				else if (!name.equals(current.getName()))
 					name = "";
 			}
 
 			nameText.setText(name);
 
 			// cleanup
 			for (Control current : tagsContainer.getChildren())
 				current.dispose();
 
 			for (final String current : commonTags) {
 				Text tmplabel = new Text(tagsContainer, SWT.BORDER);
 				tmplabel.setText(current);
 				tmplabel.setEditable(false);
 				tmplabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 				Button tmpbutton = new Button(tagsContainer, SWT.PUSH);
 				tmpbutton.setText("x");
 				tmpbutton.setToolTipText("remove tag");
 				tmpbutton.addSelectionListener(new SelectionAdapter() {
 
 					@Override
 					public void widgetSelected(SelectionEvent e) {
 						for (Item currentItem : mainWindow.getSelected())
 							currentItem.removeTag(current);
 						mainWindow.refresh();
 					}
 				});
 			}
 			nothingSelectedComposite.setVisible(false);
 			itemsSelectedComposite.setVisible(true);
 		} catch (Exception e) {
 			nothingSelectedComposite.setVisible(true);
 			itemsSelectedComposite.setVisible(false);
 		}
 
 		itemsSelectedComposite.layout();
		tagsContainer.layout();
 	}
 }
