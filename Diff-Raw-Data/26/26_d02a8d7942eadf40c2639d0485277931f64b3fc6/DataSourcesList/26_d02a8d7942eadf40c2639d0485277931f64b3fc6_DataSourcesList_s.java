 package org.pentaho.pac.client.datasources;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.pentaho.pac.client.PacServiceFactory;
 import org.pentaho.pac.client.PentahoAdminConsole;
 import org.pentaho.pac.client.common.ui.dialog.MessageDialog;
 import org.pentaho.pac.client.i18n.PacLocalizedMessages;
 import org.pentaho.pac.common.datasources.PentahoDataSource;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.ListBox;
 
 public class DataSourcesList extends ListBox {
   private static final PacLocalizedMessages MSGS = PentahoAdminConsole.getLocalizedMessages();
   List<PentahoDataSource> dataSources = new ArrayList<PentahoDataSource>();
   boolean isInitialized = false;
   
   public DataSourcesList() {
     super(true);
   }
 
   public PentahoDataSource[] getDataSources() {
     return (PentahoDataSource[])dataSources.toArray(new PentahoDataSource[0]);
   }
 
   public void setDataSources(PentahoDataSource[] dataSources) {
    
    this.dataSources.clear();
    this.dataSources.addAll(Arrays.asList(dataSources));
    clear();
    for (int i = 0; i < dataSources.length; i++) {
      addItem(dataSources[i].getName());
     }
    isInitialized = true;
   }
   
   public void setDataSource(int index, PentahoDataSource dataSource) {
     dataSources.set(index, dataSource);
     setItemText(index, dataSource.getName());
   }
   
   public PentahoDataSource[] getSelectedDataSources() {
     List<PentahoDataSource> selectedDataSources = new ArrayList<PentahoDataSource>();
     int itemCount = getItemCount();
     for (int i = 0; i < itemCount; i++) {
       if (isItemSelected(i)) {
         selectedDataSources.add(dataSources.get(i));
       }
     }
     return (PentahoDataSource[])selectedDataSources.toArray(new PentahoDataSource[0]);
   }
   
   public void setSelectedDataSource(PentahoDataSource dataSource) {
     setSelectedDataSources(new PentahoDataSource[]{dataSource});
   }
   
   public void setSelectedDataSources(PentahoDataSource[] dataSources) {
     List<String> dataSourceNames = new ArrayList<String>();
     for (int i = 0; i < dataSources.length; i++) {
       dataSourceNames.add(dataSources[i].getName());
     }
     int itemCount = getItemCount();
     for (int i = 0; i < itemCount; i++) {
       setItemSelected(i, dataSourceNames.contains(getItemText(i)));
     }
     
   }
   
   public void removeSelectedDataSources() {
     int numDataSourcesDeleted = 0;
     int selectedIndex = -1;
     for (int i = getItemCount() - 1; i >= 0; i--) {
       if (isItemSelected(i)) {
         dataSources.remove(i);
         removeItem(i);
         numDataSourcesDeleted++;
         selectedIndex = i;
       }
     }
     if (numDataSourcesDeleted == 1) {
       if (selectedIndex >= getItemCount()) {
         selectedIndex = getItemCount() - 1;
       }
       if (selectedIndex >= 0) {
         setItemSelected(selectedIndex, true);
       }
     }
   }
   
   public void removeDataSources(PentahoDataSource[] dataSourcesToRemove) {
     int numDataSourcesDeleted = 0;
     int selectedIndex = -1;
     for (int i = getItemCount() - 1; i >= 0; i--) {
       if (isItemSelected(i)) {
         selectedIndex = i;
       }
       for (int j = 0; j < dataSourcesToRemove.length; j++) {
         if (dataSources.get(i).equals(dataSourcesToRemove[j])) {
           dataSources.remove(i);
           removeItem(i);
           numDataSourcesDeleted++;
         }
       }
     }
     if (numDataSourcesDeleted == 1) {
       if (selectedIndex >= getItemCount()) {
         selectedIndex = getItemCount() - 1;
       }
       if (selectedIndex >= 0) {
         setItemSelected(selectedIndex, true);
       }
     }
   }
   
   public void refresh() {
     setDataSources(new PentahoDataSource[0]);
     isInitialized = false;
     AsyncCallback<PentahoDataSource[]> callback = new AsyncCallback<PentahoDataSource[]>() {
       public void onSuccess(PentahoDataSource[] result) {
         setDataSources(result);
         isInitialized = true;
       }
 
       public void onFailure(Throwable caught) {
         setDataSources(null);
         MessageDialog errorDialog = new MessageDialog(MSGS.error() );
         errorDialog.setText(MSGS.errorLoadingDataSources());
         errorDialog.setMessage(MSGS.dataSourcesRefreshError(caught.getMessage()));
         errorDialog.center();
       }
     };
     
     PacServiceFactory.getPacService().getDataSources(callback);
   }
   
   public boolean addDataSource(PentahoDataSource dataSource) {
     boolean result = false;
     boolean dataSourceNameExists = false;
     for (Iterator<PentahoDataSource> iter = dataSources.iterator(); iter.hasNext() && !dataSourceNameExists;) {
       PentahoDataSource tmpDataSource = (PentahoDataSource)iter.next();
       dataSourceNameExists = tmpDataSource.getName().equals(dataSource.getName());      
     }
     if (!dataSourceNameExists) {
       dataSources.add(dataSource);
       addItem(dataSource.getName());
       result = true;
     }
     return result;
   }
   
   public boolean isInitialized() {
     return isInitialized;
   }
   
   public void clearDataSourcesCache() {
     setDataSources(new PentahoDataSource[0]);
     isInitialized = false;
   }
 }
