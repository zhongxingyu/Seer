 package net.sf.anathema.charms.character.editor.combo;
 
 import net.sf.anathema.basics.ui.forms.InstructionTextFactory;
 import net.sf.anathema.charms.character.editor.ComboEditorInput;
 import net.sf.anathema.charms.character.editor.table.CharmContentProvider;
 import net.sf.anathema.charms.character.editor.table.CharmNameColumn;
 import net.sf.anathema.charms.character.editor.table.CharmTableLabelProvider;
 import net.sf.anathema.charms.character.editor.table.ComboKeywordColumn;
 import net.sf.anathema.charms.character.editor.table.ICharmTableInput;
 import net.sf.anathema.charms.character.editor.table.ITableColumn;
 import net.sf.anathema.charms.data.ICharmDataMap;
 import net.sf.anathema.charms.data.INameMap;
 
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 public class ComboInputViewFactory {
 
   private final ComboEditorInput editorInput;
   private final FormToolkit toolkit;
 
   public ComboInputViewFactory(ComboEditorInput editorInput, FormToolkit toolkit) {
     this.editorInput = editorInput;
     this.toolkit = toolkit;
   }
 
   public ComboInputWidgets create(Composite parent) {
     ComboInputWidgets widgets = new ComboInputWidgets();
     Composite container = createGridContainer(parent, 2);
    createAvailableRow(widgets, container);
    createEditRowColumn(widgets, container);
     return widgets;
   }
 
  private void createAvailableRow(ComboInputWidgets widgets, Composite container) {
    widgets.availableCharms = createAvailableCharmsTable(container);
    widgets.availableCharms.getTable().setLayoutData(new GridData(GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
    Composite availableCombos = createGridContainer(container, 1);
    toolkit.createLabel(availableCombos, "Learned Combos");
  }

   private Composite createGridContainer(Composite parent, int columnCount) {
     Composite container = toolkit.createComposite(parent);
     container.setLayoutData(new GridData(GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
     container.setLayout(new GridLayout(columnCount, true));
     return container;
   }
 
  private void createEditRowColumn(ComboInputWidgets widgets, Composite parent) {
     InstructionTextFactory textFactory = new InstructionTextFactory(toolkit);
    widgets.selectedCharms = createComboCharmsTable(parent);
     widgets.selectedCharms.getTable().setLayoutData(new GridData(GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
    Composite container = createGridContainer(parent, 1);
    widgets.name = textFactory.create(container, "<Name>", SWT.BORDER);
     widgets.name.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    widgets.description = textFactory.create(container, "<Description>", SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
     widgets.description.setLayoutData(new GridData(GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
     widgets.confirmButton = toolkit.createButton(container, "Confirm", SWT.PUSH);
     widgets.confirmButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
   }
 
   private TableViewer createAvailableCharmsTable(Composite body) {
     Table table = createTable(body);
     return createTableViewer(table, editorInput.getComboableCharms(), "Available Charm");
   }
 
   private TableViewer createComboCharmsTable(Composite body) {
     Table table = createTable(body);
     return createTableViewer(table, editorInput.getComboedCharms(), "Combo Charm");
   }
 
   private Table createTable(Composite body) {
     Table table = toolkit.createTable(body, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
     table.setHeaderVisible(true);
     return table;
   }
 
   private void createColumn(Table table, ITableColumn column, int style) {
     TableColumn commentColumn = new TableColumn(table, style);
     commentColumn.setText(column.getHeader());
     commentColumn.setWidth(column.getWidth());
   }
 
   private TableViewer createTableViewer(Table table, ICharmTableInput input, String header) {
     INameMap nameMap = editorInput.getCharmNameMap();
     ICharmDataMap dataMap = editorInput.getCharmDataMap();
     ITableColumn[] columns = new ITableColumn[] { new CharmNameColumn(nameMap, header), new ComboKeywordColumn(dataMap) };
     for (ITableColumn column : columns) {
       createColumn(table, column, SWT.LEFT);
     }
     TableViewer tableViewer = new TableViewer(table);
     tableViewer.setContentProvider(new CharmContentProvider());
     tableViewer.setLabelProvider(new CharmTableLabelProvider(columns));
     tableViewer.setInput(input);
     return tableViewer;
   }
 }
