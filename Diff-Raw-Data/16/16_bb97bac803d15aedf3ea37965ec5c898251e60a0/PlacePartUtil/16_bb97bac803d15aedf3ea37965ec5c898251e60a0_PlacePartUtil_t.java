 package de.hswt.hrm.place.ui.part.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.TableColumn;
 
 import de.hswt.hrm.place.model.Place;
 
 public final class PlacePartUtil {
 	
 	private PlacePartUtil() { }
 	
     private final static int WIDTH = 120;
 
     public static Map<String, String> getDefaultColumnHeaders() {
 
         // TODO Multilanguage support
 
         Map<String, String> columnHeaders = new HashMap<String, String>();
 
         columnHeaders.put("placeName", "Name");
         columnHeaders.put("postCode", "Postleitzahl");
         columnHeaders.put("city", "Stadt");
         columnHeaders.put("street", "Straße");
         columnHeaders.put("streetNo", "Hausnummer");
         columnHeaders.put("location", "Location");
         columnHeaders.put("area", "Bereich");
 
         return columnHeaders;
     }
 
     public static void createColumns(Composite parent, TableViewer viewer,
             Map<String, String> columnHeaders, final PlaceComparator comparator) {
 
         Menu headerMenu = new Menu(viewer.getTable());
         viewer.getTable().setMenu(headerMenu);
 
         // placeName
         TableViewerColumn col = createTableViewerColumn(columnHeaders.get("placeName"), WIDTH,
                 viewer, 0, comparator);
         col.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Place p = (Place) element;
                 return p.getPlaceName();
             }
         });
         createMenuItem(headerMenu, col.getColumn());
 
        // postCode
        col = createTableViewerColumn(columnHeaders.get("postCode"), WIDTH, viewer, 1, comparator);
         col.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Place p = (Place) element;
                 return p.getPostCode();
             }
         });
         createMenuItem(headerMenu, col.getColumn());
 
         // city
         col = createTableViewerColumn(columnHeaders.get("city"), WIDTH, viewer, 2, comparator);
         col.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
             	Place p = (Place) element;
                 return p.getStreet();
             }
         });
         createMenuItem(headerMenu, col.getColumn());
 
         // street
         col = createTableViewerColumn(columnHeaders.get("street"), WIDTH, viewer, 3, comparator);
         col.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
             	Place p = (Place) element;
                 return p.getStreetNo();
             }
         });
         createMenuItem(headerMenu, col.getColumn());
 
         // streetNo
         col = createTableViewerColumn(columnHeaders.get("streetNo"), WIDTH, viewer, 4, comparator);
         col.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
             	Place p = (Place) element;
                 return p.getPostCode();
             }
         });
         createMenuItem(headerMenu, col.getColumn());
 
         // location
         col = createTableViewerColumn(columnHeaders.get("location"), WIDTH, viewer, 5, comparator);
         col.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
             	Place p = (Place) element;
                 return p.getCity();
             }
         });
         createMenuItem(headerMenu, col.getColumn());
         // area
         col = createTableViewerColumn(columnHeaders.get("area"), WIDTH, viewer, 6, comparator);
         col.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
             	Place p = (Place) element;
                 return p.getArea();
 
             }
         });
         createMenuItem(headerMenu, col.getColumn());
 
     }
 
     private static void createMenuItem(Menu parent, final TableColumn column) {
         final MenuItem itemName = new MenuItem(parent, SWT.CHECK);
         itemName.setText(column.getText());
         itemName.setSelection(column.getResizable());
         itemName.addListener(SWT.Selection, new Listener() {
             public void handleEvent(Event event) {
                 if (itemName.getSelection()) {
                     column.setWidth(WIDTH);
                     column.setResizable(true);
                 }
                 else {
                     column.setWidth(0);
                     column.setResizable(false);
                 }
             }
         });
 
     }
 
     private static TableViewerColumn createTableViewerColumn(String title, int bound,
             TableViewer viewer, int colNumber, final PlaceComparator comparator) {
         TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
         TableColumn column = viewerColumn.getColumn();
         column.setText(title);
         column.setWidth(bound);
         column.setResizable(true);
         column.setMoveable(true);
         column.addSelectionListener(getSelectionAdapter(viewer, column, colNumber, comparator));
         return viewerColumn;
     }
 
     private static SelectionListener getSelectionAdapter(final TableViewer viewer,
             final TableColumn column, final int index, final PlaceComparator comparator) {
         SelectionAdapter selectionAdapter = new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
 
                 comparator.setColumn(index);
                 int dir = comparator.getDirection();
                 viewer.getTable().setSortDirection(dir);
                 viewer.getTable().setSortColumn(column);
                 viewer.refresh();
 
             }
         };
         return selectionAdapter;
     }
 }
