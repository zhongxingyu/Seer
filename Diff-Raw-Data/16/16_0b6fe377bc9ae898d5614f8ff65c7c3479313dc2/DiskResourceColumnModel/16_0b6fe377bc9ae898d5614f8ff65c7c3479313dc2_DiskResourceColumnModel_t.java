 package org.iplantc.core.uidiskresource.client.views;
 
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResource;
 import org.iplantc.core.uicommons.client.models.diskresources.File;
 import org.iplantc.core.uicommons.client.models.diskresources.Folder;
 import org.iplantc.core.uicommons.client.util.DiskResourceUtil;
 import org.iplantc.core.uidiskresource.client.models.DiskResourceProperties;
 import org.iplantc.core.uidiskresource.client.views.cells.DiskResourceNameCell;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.cell.client.DateCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

 import com.sencha.gxt.core.client.IdentityValueProvider;
 import com.sencha.gxt.core.client.ValueProvider;
 import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
 import com.sencha.gxt.widget.core.client.grid.ColumnModel;
 
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

 public class DiskResourceColumnModel extends ColumnModel<DiskResource> {
 
     public DiskResourceColumnModel(DiskResourceView view, DiskResourceSelectionModel sm) {
         super(createColumnConfigList(view, sm));
     }
 
     public static List<ColumnConfig<DiskResource, ?>> createColumnConfigList(DiskResourceView view,
             DiskResourceSelectionModel sm) {
         List<ColumnConfig<DiskResource, ?>> list = new ArrayList<ColumnConfig<DiskResource, ?>>();
 
         DiskResourceProperties props = GWT.create(DiskResourceProperties.class);
 
         ColumnConfig<DiskResource, DiskResource> name = new ColumnConfig<DiskResource, DiskResource>(
                 new IdentityValueProvider<DiskResource>("name"), 100, I18N.DISPLAY.name());
         name.setCell(new DiskResourceNameCell(view, DiskResourceNameCell.CALLER_TAG.DATA));
         name.setComparator(new DiskResourceNameComparator());
 
         ColumnConfig<DiskResource, Date> lastModified = new ColumnConfig<DiskResource, Date>(
                 props.lastModified(), 120, I18N.DISPLAY.lastModified());
         lastModified.setCell(new DateCell(DateTimeFormat
                 .getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM)));
         ColumnConfig<DiskResource, Long> size = new ColumnConfig<DiskResource, Long>(
                 new DiskResourceSizeValueProvider(),
                 50, I18N.DISPLAY.size());
         size.setCell(new DiskResourceSizeCell());
         
         ColumnConfig<DiskResource, String> path = new ColumnConfig<DiskResource, String>(props.id(),
                 100, I18N.DISPLAY.path());
         path.setHidden(true);
 
         ColumnConfig<DiskResource, Date> created = new ColumnConfig<DiskResource, Date>(
                 props.dateSubmitted(), 120, I18N.DISPLAY.dateSubmitted());
        created.setCell(new DateCell(DateTimeFormat
                 .getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM)));
         created.setHidden(true);
         
         list.add(sm.getColumn());
         list.add(name);
         list.add(lastModified);
         list.add(size);
         list.add(path);
         list.add(created);
 
         return list;
     }
 
     public void setCheckboxColumnHidden(boolean hidden) {
         setHidden(0, hidden);
     }
 
     public ColumnConfig<DiskResource, DiskResource> getNameColumn() {
         return getColumn(1);
     }
 
     /**
      * A <code>DiskResource</code> <code>Comparator</code> that sorts <code>Folder</code> names before
      * <code>File</code> names (case-insensitive).
      * 
      * @author psarando
      * 
      */
     private static final class DiskResourceNameComparator implements Comparator<DiskResource> {
 
         @Override
         public int compare(DiskResource dr1, DiskResource dr2) {
             if (dr1 instanceof Folder && dr2 instanceof File) {
                 return -1;
             }
             if (dr1 instanceof File && dr2 instanceof Folder) {
                 return 1;
             }
 
             return dr1.getName().compareToIgnoreCase(dr2.getName());
         }
     }
 
     /**
      * This is a value provider class which returns the size of any <code>DiskResource</code>. If the
      * <code>DiskResource</code> is a <code>Folder</code>, this provider will return null. If it is a
      * <code>File</code>, then it returns the value of the {@link File#getSize()} method as an Integer.
      * 
      * @author jstroot
      * 
      */
     private static final class DiskResourceSizeValueProvider implements
             ValueProvider<DiskResource, Long> {
         @Override
         public Long getValue(DiskResource object) {
             if (object instanceof File) {
                 return new Long(((File)object).getSize());
             } else {
                 return null;
             }
         }
 
         @Override
         public void setValue(DiskResource object, Long value) {
         }
 
         @Override
         public String getPath() {
             return "size"; //$NON-NLS-1$
         }
     }
 
     /**
      * A <code>Cell</code> for converting bytes as integers into human readable <code>File</code> sizes.
      * 
      * @author psarando
      * 
      */
     private static final class DiskResourceSizeCell extends AbstractCell<Long> {
 
         @Override
         public void render(Context context, Long value, SafeHtmlBuilder sb) {
             if (value != null) {
                 sb.appendEscaped(DiskResourceUtil.formatFileSize(value.toString()));
             }
         }
     }
 }
