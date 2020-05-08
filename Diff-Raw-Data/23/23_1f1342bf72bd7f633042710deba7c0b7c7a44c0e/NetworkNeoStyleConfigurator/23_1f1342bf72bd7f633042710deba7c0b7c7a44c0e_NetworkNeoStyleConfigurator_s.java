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
 
 package org.amanzi.awe.neostyle;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.refractions.udig.project.internal.Layer;
 import net.refractions.udig.style.IStyleConfigurator;
 
 import org.amanzi.awe.catalog.neo.GeoNeo;
 import org.amanzi.neo.services.filters.Filter;
 import org.eclipse.jface.viewers.CellLabelProvider;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Table;
 
 /**
  * <p>
  * Configurer of network style
  * </p>
  * 
  * @author TsAr
  * @since 1.0.0
  */
 public class NetworkNeoStyleConfigurator extends IStyleConfigurator {
     private NetworkNeoStyle curStyle;
     private NetworkStyleDefiner defaultStyle = new NetworkStyleDefiner();
     private CheckboxTableViewer viewer;
     private ContentProvider provider;
     private Button createNew;
     private Button remove;
     /** NetworkNeoStyleConfigurator ID field */
     public static final String ID = "org.amanzi.awe.neostyle.style.network"; //$NON-NLS-1$
 
     //private static final String[] ICON_SIZES = new String[] {"6", "8", "12", "16", "32", "48", "64"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
 
     public NetworkNeoStyleConfigurator() {
         super();
 
     }
 
     @Override
     public boolean canStyle(Layer aLayer) {
         return aLayer.getStyleBlackboard().get(ID) != null;
         // try {
         // if (aLayer.getStyleBlackboard().get(ID) != null) {
         // GeoNeo geoNeo;
         // geoNeo = aLayer.findGeoResource(NeoGeoResource.class).resolve(GeoNeo.class, null);
         // return geoNeo.getGisType() == GisTypes.NETWORK;
         // }
         // } catch (IOException e) {
         // e.printStackTrace();
         // }
         // return false;
     }
 
     @Override
     public void createControl(Composite parent) {
         parent.setLayout(new GridLayout(1, false));
         CTabFolder tabFolder = new CTabFolder(parent, SWT.TOP);
         tabFolder.setBorderVisible(true);
         tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
 
         // // Set up a gradient background for the selected tab
         // tabFolder.setSelectionBackground(new Color[] {
         // display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW),
         // display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW),
         // display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW)}, new int[] { 50,
         // 100});
 
         CTabItem item = new CTabItem(tabFolder, SWT.NONE);
         item.setText("Style");
         tabFolder.setSelection(item);
         ScrolledComposite scroll = new ScrolledComposite(tabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
         scroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         scroll.setExpandVertical(true);
         scroll.setExpandHorizontal(true);
         item.setControl(scroll);
         Composite style = new Composite(scroll, SWT.FILL);
         style.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         defaultStyle.createPartControl(style);
         scroll.setContent(style);
         scroll.setMinSize(style.computeSize(SWT.DEFAULT, SWT.DEFAULT));
         item = new CTabItem(tabFolder, SWT.NONE);
         item.setText("Filters");
         scroll = new ScrolledComposite(tabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
         scroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         scroll.setExpandVertical(true);
         scroll.setExpandHorizontal(true);
         item.setControl(scroll);
         Composite filterMain = new Composite(scroll, SWT.NONE);
         filterMain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         createFilterPage(filterMain);
         scroll.setContent(filterMain);
     }
 
     private void createFilterPage(Composite filterMain) {
         filterMain.setLayout(new GridLayout(2, true));
         Label lb = new Label(filterMain, SWT.LEFT);
         lb.setText("Applyed filters:");
         viewer = CheckboxTableViewer.newCheckList(filterMain, SWT.FULL_SELECTION | SWT.BORDER);
         viewer.setColumnProperties(new String[] {"Filter"});
         viewer.setLabelProvider(new FiltrLabelProvider());
         viewer.addCheckStateListener(new ICheckStateListener() {
 
             @Override
             public void checkStateChanged(CheckStateChangedEvent event) {
                 FilterRow row = (FilterRow)event.getElement();
                 row.setSelected(event.getChecked());
             }
         });
         viewer.addSelectionChangedListener(new ISelectionChangedListener() {
 
             @Override
             public void selectionChanged(SelectionChangedEvent event) {
                 IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                 remove.setEnabled(selection.size() == 1);
             }
         });
         viewer.addDoubleClickListener(new IDoubleClickListener() {
 
             @Override
             public void doubleClick(DoubleClickEvent event) {
                 IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                 if (selection.size() == 1) {
                     FilterRow row = (FilterRow)selection.getFirstElement();
                     editStyle(row);
                 }
             }
         });
         GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 10);
         provider = new ContentProvider();
         viewer.setContentProvider(provider);
         viewer.getControl().setLayoutData(layoutData);
         formColumns(viewer);
         viewer.getTable().addControlListener(new ControlListener() {
 
             @Override
             public void controlResized(ControlEvent e) {
                 Table table = (Table)e.widget;
                 int width = table.getClientArea().width;
                 table.getColumn(0).setWidth(width - 2);
             }
 
             @Override
             public void controlMoved(ControlEvent e) {
             }
         });
         viewer.getTable().setHeaderVisible(true);
         // ColumnViewerToolTipSupport.enableFor(viewer);
         createNew = new Button(filterMain, SWT.PUSH);
         createNew.setText("Create filter");
         createNew.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 createNew();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         remove = new Button(filterMain, SWT.PUSH);
         remove.setText("Delete selected");
         remove.setEnabled(false);
         remove.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 removeSelected();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
     }
 
     /**
      * @param row
      */
     protected void editStyle(FilterRow row) {
         NetworkFilterDefiner definer = new NetworkFilterDefiner(viewer.getControl().getShell(), "Edit filter",defaultStyle.getGeoNeo(), row.getName(), row.getWrapper());
         FilterModel model = NeoStylePlugin.getDefault().getFilterModel();
         Set<String> restr = new HashSet<String>();
         restr.addAll(model.getFilterNames());
         restr.remove(row.getName());
         definer.setRestrictedNames(restr);
         IFilterWrapper result = definer.open();
         if (result != null) {
             model.removeFilter(row.getName());
             defaultStyle.getCurStyle().removeFilter(row.getName());
             model.addFilter(definer.getWrapperName(), result);
             defaultStyle.getCurStyle().addFilter(definer.getWrapperName(), result);
             model.store();
         }
         viewer.setInput(defaultStyle.getCurStyle());
     }
 
     /**
      *
      */
     protected void createNew() {
         FilterWrapperImpl<NetworkNeoStyle> wrapper = createDefWrapper();
         NetworkFilterDefiner definer = new NetworkFilterDefiner(viewer.getControl().getShell(), "Create new filter",defaultStyle.getGeoNeo(), "new", wrapper);
         FilterModel model = NeoStylePlugin.getDefault().getFilterModel();
         definer.setRestrictedNames(model.getFilterNames());
         IFilterWrapper result = definer.open();
         if (result != null) {
             model.addFilter(definer.getWrapperName(), result);
             defaultStyle.getCurStyle().addFilter(definer.getWrapperName(), result);
             model.store();
         }
         viewer.setInput(defaultStyle.getCurStyle());
     }
 
     /**
      * @return
      */
     private FilterWrapperImpl<NetworkNeoStyle> createDefWrapper() {
         FilterWrapperImpl<NetworkNeoStyle> result = new FilterWrapperImpl<NetworkNeoStyle>();
         Filter filter = new Filter();
         result.setFilter(filter);
         result.setStyle(new NetworkNeoStyleContent().createDefaultNetworkStyle());
         return result;
     }
 
     /**
      *
      */
     protected void removeSelected() {
         IStructuredSelection sel=(IStructuredSelection)viewer.getSelection();
         if (sel.size()!=1){
             return;
         }
         
         
     }
 
 
     private void formColumns(CheckboxTableViewer viewer) {
         TableViewerColumn column = new TableViewerColumn(viewer, SWT.FILL);
         column.setLabelProvider(new FiltrLabelProvider());
         column.getColumn().setText("Filter name");
     }
 
     @Override
     protected void refresh() {
         curStyle = (NetworkNeoStyle)getStyleBlackboard().get(ID);
         defaultStyle.setCurStyle(curStyle);
         GeoNeo resource;
         try {
             resource = getLayer().findGeoResource(GeoNeo.class).resolve(GeoNeo.class, null);
         } catch (IOException e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
         defaultStyle.setGeoNeo(resource);
         defaultStyle.refresh();
         viewer.setInput(curStyle);
 
     }
 
     @Override
     public void preApply() {
         super.preApply();
         defaultStyle.preApply();
         storeFilters();
         getStyleBlackboard().put(ID, defaultStyle.getCurStyle());
     }
 
     /**
      *
      */
     private void storeFilters() {
         defaultStyle.getCurStyle().setFilterMap(provider.getChecked());
     }
 
     private static class ContentProvider implements IStructuredContentProvider {
 
         List<FilterRow> elements = new ArrayList<FilterRow>();
         private CheckboxTableViewer viewer;
 
         @Override
         public void dispose() {
         }
 
         @Override
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
             this.viewer = (CheckboxTableViewer)viewer;
             if (newInput == null) {
                 elements.clear();
             } else {
                 NetworkNeoStyle style = (NetworkNeoStyle)newInput;
                 formElements(style);
             }
         }
 
         public List<String> getChecked() {
             List<String> checked = new ArrayList<String>();
             for (FilterRow row : elements) {
                 if (row.isSelected()) {
                     checked.add(row.getName());
                 }
             }
             return checked;
         }
 
         @SuppressWarnings("unchecked")
         private void formElements(NetworkNeoStyle style) {
             elements.clear();
             FilterModel model = NeoStylePlugin.getDefault().getFilterModel();
             Set<String> names = style.getFilterNames();
             for (String name : model.getFilterNames(NetworkNeoStyle.class)) {
                 FilterRow wr = new FilterRow();
                 wr.setName(name);
                 wr.setWrapper((FilterWrapperImpl<NetworkNeoStyle>)model.getWrapperByName(name));
                 wr.setSelected(names.contains(name));
                 elements.add(wr);
                viewer.setChecked(wr, wr.isSelected());
             }
 
             Collections.sort(elements, new Comparator<FilterRow>() {
 
                 @Override
                 public int compare(FilterRow o1, FilterRow o2) {
                     return o1.getName().compareTo(o2.getName());
                     // if (o1.isSelected()==o2.isSelected()){
                     // return o1.getName().compareTo(o2.getName());
                     // }else{
                     // return o1.isSelected()?-1:1;
                     // }
                 }
 
             });
         }
 
         @Override
         public Object[] getElements(Object inputElement) {
             return elements.toArray(new FilterRow[0]);
         }
 
     }
 
     private static class FiltrLabelProvider extends CellLabelProvider {
 
         @Override
         public void update(ViewerCell cell) {
             FilterRow wrapper = ((FilterRow)cell.getElement());
             cell.setText(wrapper.getName());
         }
 
         @Override
         public String getToolTipText(Object element) {
             FilterRow wrapper = ((FilterRow)element);
 
             return "Use doubleclick for edit filter '" + wrapper.getName() + "'";
         }
     }
 }
