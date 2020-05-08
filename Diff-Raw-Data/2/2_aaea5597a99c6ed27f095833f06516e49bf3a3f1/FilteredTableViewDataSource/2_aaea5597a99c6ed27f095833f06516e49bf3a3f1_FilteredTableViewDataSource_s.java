 /* SortableTableViewController */
 
 import org.apache.log4j.Logger;
 
 import com.apple.cocoa.application.NSDraggingInfo;
 import com.apple.cocoa.application.NSPasteboard;
 import com.apple.cocoa.application.NSTableColumn;
 import com.apple.cocoa.application.NSTableView;
 import com.apple.cocoa.foundation.NSArray;
 import com.apple.cocoa.foundation.NSMutableArray;
 import com.apple.cocoa.foundation.NSObject;
 import com.redbugz.macpaf.util.CocoaUtils;
 import com.redbugz.macpaf.util.StringUtils;
 import com.redbugz.macpaf.util.WrappedTableViewDataSource;
 
 /**
  * <p>Title: </p>
  * <p>Description: </p>
  * <p>Copyright: Copyright (c) 2004</p>
  * <p>Company: RedBugz Software</p>
  * @author Logan Allred
  * @version 1.0
  */
 
 public class FilteredTableViewDataSource extends NSObject /*implements NSTableView.DataSource*/ {
 	private static Logger log = Logger.getLogger(FilteredTableViewDataSource.class);
 	
 	public NSTableView tableView; /* IBOutlet */
 	private WrappedTableViewDataSource dataSource;
 	private NSMutableArray filteredIndices = new NSMutableArray();
 	private boolean isFiltered = false;
 	private boolean isCaseSensitive = false;
 		
   public FilteredTableViewDataSource(NSTableView view) {
 	  this(view, view.dataSource());
 	}
 
 	public FilteredTableViewDataSource(NSTableView view, Object source) {
 		super();
 		dataSource = CocoaUtils.wrappedTableViewDataSource(source);
 		tableView = view;
 		tableView.setDataSource(this);
 	}
 
 /**
    * numberOfRowsInTableView
    *
    * @param nSTableView NSTableView
    * @return int
    */
   public int numberOfRowsInTableView(NSTableView nSTableView) {
 	  if (isFiltered) {
 			return filteredIndices.count();		  
 	  } else {
 		  return dataSource().numberOfRowsInTableView(nSTableView);
 	  }
   }
 
   /**
    * tableViewObjectValueForLocation
    *
    * @param nSTableView NSTableView
    * @param nSTableColumn NSTableColumn
    * @param int2 int
    * @return Object
    */
   public Object tableViewObjectValueForLocation(NSTableView nSTableView, NSTableColumn nSTableColumn, int int2) {
 	  if (isFiltered) {
 		return dataSource().tableViewObjectValueForLocation(tableView, nSTableColumn, getFilteredIndex(int2));  
 	  } else {
 	return dataSource().tableViewObjectValueForLocation(nSTableView, nSTableColumn, int2);
 	  }
   }
 
 /**
    * tableViewSetObjectValueForLocation
    *
    * @param nSTableView NSTableView
    * @param object Object
    * @param nSTableColumn NSTableColumn
    * @param int3 int
    */
   public void tableViewSetObjectValueForLocation(NSTableView nSTableView, Object object, NSTableColumn nSTableColumn,
 												 int int3) {
 	  if (isFiltered) {
 		  dataSource().tableViewSetObjectValueForLocation(nSTableView, object, nSTableColumn, 
 (int3));
 	  }
 	dataSource().tableViewSetObjectValueForLocation(nSTableView, object, nSTableColumn, int3);
   }
 
 /**
    * tableViewSortDescriptorsDidChange
    *
    * @param nSTableView NSTableView
    * @param nSArray NSArray
    */
 //  public void tableViewSortDescriptorsDidChange(NSTableView nSTableView, NSArray nSArray) {
 //	dataSource().tableViewSortDescriptorsDidChange(nSTableView, nSArray);
 //  }
 
   /**
    * tableViewWriteRowsToPasteboard
    *
    * @param nSTableView NSTableView
    * @param nSArray NSArray
    * @param nSPasteboard NSPasteboard
    * @return boolean
    */
   public boolean tableViewWriteRowsToPasteboard(NSTableView nSTableView, NSArray nSArray, NSPasteboard nSPasteboard) {
 	return dataSource().tableViewWriteRowsToPasteboard(nSTableView, nSArray, nSPasteboard);
   }
 
   /**
    * tableViewValidateDrop
    *
    * @param nSTableView NSTableView
    * @param nSDraggingInfo NSDraggingInfo
    * @param int2 int
    * @param int3 int
    * @return int
    */
   public int tableViewValidateDrop(NSTableView nSTableView, NSDraggingInfo nSDraggingInfo, int int2, int int3) {
 	return dataSource().tableViewValidateDrop(nSTableView, nSDraggingInfo, int2, int3);
   }
 
   /**
    * tableViewAcceptDrop
    *
    * @param nSTableView NSTableView
    * @param nSDraggingInfo NSDraggingInfo
    * @param int2 int
    * @param int3 int
    * @return boolean
    */
   public boolean tableViewAcceptDrop(NSTableView nSTableView, NSDraggingInfo nSDraggingInfo, int int2, int int3) {
 	return dataSource().tableViewAcceptDrop(nSTableView, nSDraggingInfo, int2, int3);
   }
 
   /**
    * dataSource
    *
    * @return DataSource
    */
   private WrappedTableViewDataSource dataSource() {
 	return dataSource;
   }
 
 /* (non-Javadoc)
  * @see com.apple.cocoa.application.NSTableView.DataSource#tableViewWriteRowsToPasteboard(com.apple.cocoa.application.NSTableView, com.apple.cocoa.foundation.NSIndexSet, com.apple.cocoa.application.NSPasteboard)
  */
 //public boolean tableViewWriteRowsToPasteboard(NSTableView arg0, NSIndexSet arg1, NSPasteboard arg2) {
 //	return dataSource().tableViewWriteRowsToPasteboard(arg0, arg1, arg2);
 //}
 
 /* (non-Javadoc)
  * @see com.apple.cocoa.application.NSTableView.DataSource#tableViewNamesOfPromisedFilesDroppedAtDestination(com.apple.cocoa.application.NSTableView, java.net.URL, com.apple.cocoa.foundation.NSIndexSet)
  */
 //public NSArray tableViewNamesOfPromisedFilesDroppedAtDestination(NSTableView arg0, URL arg1, NSIndexSet arg2) {
 //	return dataSource().tableViewNamesOfPromisedFilesDroppedAtDestination(arg0, arg1, arg2);
 //}
 
 private int getFilteredIndex(int int2) {
 //log.debug("getFilteredIndex("+int2+") filteredIndices size:"+filteredIndices.count());
 if (isFiltered) {
 	return ((Integer)filteredIndices.objectAtIndex(int2)).intValue();
 }
 return int2;
 }
 
 public int getCurrentSelectedIndex() {
 	int selectedRowIndex = tableView.selectedRow();
 	if (selectedRowIndex >= 0) {
 		return getFilteredIndex(selectedRowIndex);
 	}
 	return selectedRowIndex;
 }
 
 public void setFilterString(String filterString) {
 	log.debug("FilteredTableViewDataSource.setFilterString():"+filterString);
 	if (StringUtils.isEmpty(filterString)) {
 		isFiltered = false;
 		filteredIndices.removeAllObjects();
 		log.debug("empty filter string");
 		return;
 	}
 	int numberOfRowsInTableView = dataSource().numberOfRowsInTableView(tableView);
 	log.debug("# of rows before filter: "+numberOfRowsInTableView);
 	NSMutableArray indices = new NSMutableArray();
 	for (int i = 0; i < numberOfRowsInTableView; i++) {
 		NSArray tableColumns = tableView.tableColumns();
 		boolean addColumnIndex = false;
 		for (int j = 0; j < tableColumns.count(); j++) {
 			NSTableColumn tableColumn = (NSTableColumn) tableColumns.objectAtIndex(j);
 			Object object = dataSource().tableViewObjectValueForLocation(tableView, tableColumn, i);
			String objectString = object.toString();
 			if (!isCaseSensitive) {
 				filterString = filterString.toLowerCase();
 				objectString = objectString.toLowerCase();
 			}
 			if (objectString.indexOf(filterString) >= 0) {
 				addColumnIndex = true;
 			}
 		}
 		if (addColumnIndex) {
 			indices.addObject(new Integer(i));			
 		}
 	}
 	isFiltered = true;
 	filteredIndices.setArray(indices);
 	log.debug("filteredIndices: "+filteredIndices);
 	log.debug("# of rows after filter: "+numberOfRowsInTableView(tableView));
 }
 
 public void setCaseSensitivity(boolean caseSensitivity) {
 	isCaseSensitive = caseSensitivity;
 }
 
 }
