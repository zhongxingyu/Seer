 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.core.utils;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.amanzi.neo.core.enums.INodeType;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ComboBoxCellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 
 // TODO: Auto-generated Javadoc
 /**
  * <p>
  * Create Network Config Page
  * </p>
  * .
  * 
  * @author TsAr
  * @since 1.0.0
  */
 public class EditPropertiesPage extends WizardPage {
 
     /** The node type. */
     protected final INodeType nodeType;
 
     /** The viewer. */
     protected TableViewer viewer;
 
     /** The property list. */
     protected List<PropertyWrapper> propertyList = new ArrayList<PropertyWrapper>();
     
     /** The Constant GRAY. */
     protected static final Color GRAY = new Color(null, 240, 240, 240);
     
     /** The types. */
     @SuppressWarnings("rawtypes")
     protected Map<String, Class> types = new LinkedHashMap<String, Class>();
     
     /** The remove. */
     private Button remove;
 
     /**
      * Instantiates a new creates the network config page.
      * 
      * @param pageName the page name
      * @param nodeType the node type
      */
     public EditPropertiesPage(String pageName,String title, INodeType nodeType) {
         super(pageName);
         this.nodeType = nodeType;
         setTitle(title);
         setDescription(getNormalDescription());
 
     }
 
     /**
      * Inits the property.
      */
     protected void initProperty() {
         propertyList.clear();
         PropertyWrapper name = new PropertyWrapper("name", String.class, "", false);
         propertyList.add(name);
         //TODO implement
         if (nodeType==NodeTypes.SITE){
             propertyList.add(new PropertyWrapper("lat", Double.class, "", false));
             propertyList.add(new PropertyWrapper("lon", Double.class, "", false));
             
         }
     }
 
     /**
      * Gets the normal description.
      * 
      * @return the normal description
      */
     protected String getNormalDescription() {
         return "";
     }
 
     /**
      * Creates the control.
      * 
      * @param parent the parent
      */
     @Override
     public void createControl(Composite parent) {
         initProperty();
 
         final Group main = new Group(parent, SWT.FILL);
         
         main.setLayout(new GridLayout(2, false));
         viewer = new TableViewer(main, SWT.FILL | SWT.BORDER | SWT.FULL_SELECTION);
         viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
         TableContentProvider provider = new TableContentProvider();
         viewer.setContentProvider(provider);
         createTableColumn();
         Button add = new Button(main, SWT.PUSH);
         add.setText("add");
         add.setLayoutData(new GridData(SWT.FILL, SWT.UP, false, false, 1, 1));
         add.addSelectionListener(new SelectionListener() {
             
             @Override
             public void widgetSelected(SelectionEvent e) {
                 addProperty();
             }
             
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         remove = new Button(main, SWT.PUSH);
         remove.setText("remove");
         remove.setLayoutData(new GridData(SWT.FILL, SWT.UP, false, false, 1, 1));
         remove.addSelectionListener(new SelectionListener() {
             
             @Override
             public void widgetSelected(SelectionEvent e) {
                 removeSelection();
             }
             
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         remove.setEnabled(false);
         viewer.addSelectionChangedListener(new ISelectionChangedListener() {
             
 
 
             @Override
             public void selectionChanged(SelectionChangedEvent event) {
                 changeTableSelection(event);
             }
 
 
         });
         init();
         setControl(main);
     }
 
 
    /**
     * Change table selection.
     *
     * @param event the event
     */
    protected void changeTableSelection(SelectionChangedEvent event) {
        ISelection sel = event.getSelection();
        if (sel instanceof StructuredSelection){
            PropertyWrapper element=(PropertyWrapper)((StructuredSelection)sel).getFirstElement();
            remove.setEnabled(element!=null&&element.isEditable()); 
        }
    }   /**
      * Removes the selection.
      */
     protected void removeSelection() {
       IStructuredSelection sel= (IStructuredSelection) viewer.getSelection();
       if (sel==null||sel.size()!=1){
           return;
       }
       PropertyWrapper element = (PropertyWrapper)sel.getFirstElement();
       if (element.isEditable()){
           propertyList.remove(element);
           update();
       }
     }
 
     /**
      * Adds the property.
      */
     protected void addProperty() {
         propertyList.add(new PropertyWrapper("type new name", String.class, "", true));
         update();
     }
 
     /**
      * Inits the.
      */
     private void init() {
         update();
     }
 
     /**
      * Creates the table column.
      */
     protected void createTableColumn() {
         Table table = viewer.getTable();
         TableViewerColumn column;
         TableColumn col;
 
         column = new TableViewerColumn(viewer, SWT.CENTER);
         col = column.getColumn();
         col.setText("Name");
         col.setWidth(100);
         col.setResizable(true);
         column.setLabelProvider(getColumnLabelProvider(0));
         column.setEditingSupport(getEditingSupport(viewer, 0));
 
         column = new TableViewerColumn(viewer, SWT.CENTER);
         col = column.getColumn();
         col.setText("Type");
         col.setWidth(200);
         col.setResizable(true);
         column.setLabelProvider(getColumnLabelProvider(1));
         column.setEditingSupport(getEditingSupport(viewer, 1));
 
         column = new TableViewerColumn(viewer, SWT.CENTER);
         col = column.getColumn();
         col.setText("Default value");
         col.setWidth(200);
         col.setResizable(true);
         column.setLabelProvider(getColumnLabelProvider(2));
         column.setEditingSupport(getEditingSupport(viewer, 2));
 
         table.setHeaderVisible(true);
         table.setLinesVisible(true);
 
         viewer.refresh();
 
     }
 
     /**
      * Gets the type.
      * 
      * @return the type
      */
     public INodeType getType() {
         return nodeType;
     }
 
     /**
      * The Class TableContentProvider.
      */
     protected class TableContentProvider implements IStructuredContentProvider {
 
         /**
          * Gets the elements.
          * 
          * @param inputElement the input element
          * @return the elements
          */
         @Override
         public Object[] getElements(Object inputElement) {
             return propertyList.toArray(new PropertyWrapper[0]);
         }
 
         /**
          * Dispose.
          */
         @Override
         public void dispose() {
         }
 
         /**
          * Input changed.
          * 
          * @param viewer the viewer
          * @param oldInput the old input
          * @param newInput the new input
          */
         @Override
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
         }
 
     }
 
     /**
      * The Class ColLabelProvider.
      */
     protected class ColLabelProvider extends ColumnLabelProvider {
 
         /** The column index. */
         protected final int columnIndex;
 
         /**
          * Instantiates a new col label provider.
          * 
          * @param columnIndex the column index
          */
         public ColLabelProvider(int columnIndex) {
             super();
             this.columnIndex = columnIndex;
         }
 
         /**
          * Gets the text.
          *
          * @param element the element
          * @return the text
          */
         @Override
         public String getText(Object element) {
             return String.valueOf(((PropertyWrapper)element).getStringValueById(columnIndex));
         }
         
         /**
          * Gets the background.
          *
          * @param element the element
          * @return the background
          */
         @Override
         public Color getBackground(Object element) {
            return ((PropertyWrapper)element).isEditable()?super.getBackground(element):GRAY;
         }
     }
 
     /**
      * The Class TableEditableSupport.
      */
     protected class TableEditableSupport extends EditingSupport {
 
         /** The editor. */
         private CellEditor editor;
 
         /** The id. */
         protected final int id;
 
         /**
          * Instantiates a new table editable support.
          * 
          * @param viewer the viewer
          * @param id the id
          */
         public TableEditableSupport(TableViewer viewer, int id) {
             super(viewer);
             this.id = id;
             if (id == 1) {
                 editor = new ComboBoxCellEditor(viewer.getTable(), getTypes());
             } else {
                 editor = new TextCellEditor(viewer.getTable());
             }
 
         }
 
         /**
          * Gets the cell editor.
          * 
          * @param element the element
          * @return the cell editor
          */
         @Override
         protected CellEditor getCellEditor(Object element) {
             return editor;
         }
 
         /**
          * Can edit.
          * 
          * @param element the element
          * @return true, if successful
          */
         @Override
         protected boolean canEdit(Object element) {
            return ((PropertyWrapper)element).isEditable();
         }
 
         /**
          * Gets the value.
          * 
          * @param element the element
          * @return the value
          */
         @Override
         protected Object getValue(Object element) {
             String value = ((PropertyWrapper)element).getStringValueById(id);
             if (id != 1) {
                 return value;
             }
             String[] types = getTypes();
             for (int i = 0; i <= types.length; i++) {
                 if (value.equals(types[i])) {
                     return i;
                 }
             }
             return -1;
         }
 
         /**
          * Sets the value.
          * 
          * @param element the element
          * @param value the value
          */
         @Override
         protected void setValue(Object element, Object value) {
             if (((PropertyWrapper)element).setValue(id, value)) {
                 update();
             }
         }
 
     }
 
     /**
      * The Class PropertyWrapper.
      */
     public class PropertyWrapper {
 
         /** The name. */
         protected String name;
 
         /** The type. */
         @SuppressWarnings("rawtypes")
         private Class type = String.class;
 
         /** The def value. */
         private String defValue = "";
 
         /** The editable. */
         private final boolean editable;
 
 
         /**
          * Gets the name.
          *
          * @return the name
          */
         public String getName() {
             return name;
         }
 
         /**
          * Gets the type.
          *
          * @return the type
          */
         @SuppressWarnings("rawtypes")
         public Class getType() {
             return type;
         }
 
         /**
          * Gets the def value.
          *
          * @return the def value
          */
         public String getDefValue() {
             return defValue;
         }
 
         /**
          * Instantiates a new property wrapper.
          * 
          * @param name the name
          * @param type the type
          * @param defValue the def value
          * @param editable the editable
          */
         public PropertyWrapper(String name, Class< ? > type, String defValue, boolean editable) {
             super();
             this.name = name;
             this.type = type;
             this.defValue = defValue;
             this.editable = editable;
         }
 
         /**
          * Checks if is editable.
          * 
          * @return true, if is editable
          */
         public boolean isEditable() {
             return editable;
         }
 
         /**
          * Sets the value.
          * 
          * @param id the id
          * @param value the value
          * @return true, if successful
          */
         public boolean setValue(int id, Object value) {
             if (id == 0) {
                 name = ((String)value).trim();
                 return true;
             }
             if (id == 2) {
                 defValue = ((String)value).trim();
                 return true;
             }
             int typeid = (Integer)value;
             if (typeid < 0) {
                 return false;
             }
             type = types.get(getTypes()[typeid]);
             return true;
         }
 
         /**
          * Gets the value by id.
          * 
          * @param id the id
          * @return the value by id
          */
         public String getStringValueById(int id) {
             if (id == 0) {
                 return name;
             }
             if (id == 1) {
                 return String.valueOf(type == null ? null : type.getSimpleName());
             }
             if (id == 2) {
                 return String.valueOf(defValue);
             }
             return "error";
         }
 
         /**
          * Checks if is valid.
          *
          * @return true, if is valid
          */
         @SuppressWarnings("unchecked")
         public boolean isValid() {
             if (StringUtils.isEmpty(name)){
                 return false;
             }
             if (StringUtils.isEmpty(defValue)){
                 return true;
             }
             if (Number.class.isAssignableFrom(type)){
                 try {
                     NeoUtils.getNumberValue(type, defValue);
                 } catch (Exception e) {
                     return false;
                 }
                 
             }
             return true;
         }
 
         @Override
         public String toString() {
             return name;
         }
 
     }
 
     /**
      * Gets the types.
      * 
      * @return the types
      */
     public String[] getTypes() {
         if (types.isEmpty()) {
             types.put("String", String.class);
             types.put("Integer", Integer.class);
             types.put("Double", Double.class);
             types.put("Float", Float.class);
         }
         return types.keySet().toArray(new String[0]);
     }
 
     /**
      * Update.
      */
     public void update() {
         viewer.setInput("");
         validate();
     }
 
     /**
      * Validate.
      */
     protected void validate() {
         Set<String>names=new HashSet<String>();
         for (int i=0;i<propertyList.size();i++){
             PropertyWrapper wr = propertyList.get(i);
             if (!wr.isValid()){
                 setDescription(String.format("Property \"%s\" not valid", wr));
                 setPageComplete(false);
                 return;
             }
             if (names.contains(wr.name)){
                 setDescription(String.format("Dublicate property name '%s'", wr.name));
                 setPageComplete(false);
                 return;
             }else{
                 names.add(wr.name);
             }
             
         }
         setDescription(getNormalDescription());
         setPageComplete(true);
         return;
     }
 
 
     /**
      * Gets the properties.
      *
      * @return the properties
      */
     public List<PropertyWrapper> getProperties() {
         return propertyList;
     }
 
     protected EditingSupport getEditingSupport(TableViewer viewer, int id) {
         return new TableEditableSupport(viewer, id);
     }
 
     protected ColLabelProvider getColumnLabelProvider(int id) {
         return new ColLabelProvider(id);
     }
 }
