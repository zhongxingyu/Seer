 /*******************************************************************************
  * Copyright 2009 Regents of the University of Minnesota. All rights
  * reserved.
  * Copyright 2009 Mayo Foundation for Medical Education and Research.
  * All rights reserved.
  *
  * This program is made available under the terms of the Eclipse
  * Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR
  * IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR CONDITIONS
  * OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
  * PARTICULAR PURPOSE.  See the License for the specific language
  * governing permissions and limitations under the License.
  *
  * Contributors:
  * Minnesota Supercomputing Institute - initial API and implementation
  ******************************************************************************/
 
 package edu.umn.msi.tropix.webgui.client.components.impl;
 
 import java.util.List;
 
 import com.google.common.base.Supplier;
 import com.google.gwt.user.client.Command;
 import com.google.inject.Inject;
 import com.smartgwt.client.data.DataSource;
 import com.smartgwt.client.data.fields.DataSourceDateField;
 import com.smartgwt.client.data.fields.DataSourceTextField;
 import com.smartgwt.client.widgets.Button;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.Window;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 
 import edu.umn.msi.tropix.models.TropixObject;
 import edu.umn.msi.tropix.models.locations.Locations;
 import edu.umn.msi.tropix.webgui.client.AsyncCallbackImpl;
 import edu.umn.msi.tropix.webgui.client.Resources;
 import edu.umn.msi.tropix.webgui.client.Session;
 import edu.umn.msi.tropix.webgui.client.components.WindowComponent;
 import edu.umn.msi.tropix.webgui.client.constants.ConstantsInstances;
 import edu.umn.msi.tropix.webgui.client.mediators.LocationUpdateMediator;
 import edu.umn.msi.tropix.webgui.client.mediators.LocationUpdateMediator.UpdateEvent;
 import edu.umn.msi.tropix.webgui.client.models.SearchResultUtils;
 import edu.umn.msi.tropix.webgui.client.widgets.CanvasWithOpsLayout;
 import edu.umn.msi.tropix.webgui.client.widgets.PopOutWindowBuilder;
 import edu.umn.msi.tropix.webgui.client.widgets.SmartUtils;
 import edu.umn.msi.tropix.webgui.services.object.LocalSearchService;
 import edu.umn.msi.tropix.webgui.services.object.ObjectService;
 import edu.umn.msi.tropix.webgui.services.object.SharedFolder;
 
 public class FindSharedFoldersWindowComponentSupplierImpl implements Supplier<WindowComponent<Window>> {
   private Session session;
   
   
   @Inject
   public FindSharedFoldersWindowComponentSupplierImpl(final Session session) {
     this.session = session;
   }
   
   private class FindSharedFoldersWindowComponentImpl extends WindowComponentImpl<Window> {
 
     FindSharedFoldersWindowComponentImpl() {
       final ListGrid grid = this.getGrid();
       grid.setShowFilterEditor(true);
       
       final Button addButton = SmartUtils.getButton(ConstantsInstances.COMPONENT_INSTANCE.findSharedAdd(), Resources.FOLDER_NEW, new Command() {
         public void execute() {
           final ListGridRecord record = grid.getSelectedRecord();
           final TropixObject object = (TropixObject) record.getAttributeAsObject("object");
           addSharedFolder(object.getId());
         }        
       });
       
       final Button addGroupButton = SmartUtils.getButton(ConstantsInstances.COMPONENT_INSTANCE.findSharedAddGroup(), Resources.FOLDER_NEW, new Command() {
         public void execute() {
           final ListGridRecord record = grid.getSelectedRecord();
           final TropixObject object = (TropixObject) record.getAttributeAsObject("object");
           addGroupSharedFolder(object.getId());
         }        
       });
 
       
       SmartUtils.enabledWhenHasSelection(addButton, grid, false);
       final Button cancelButton = SmartUtils.getCancelButton(this);
       populateData(grid);
       final Canvas[] buttons;
       if(session.getPrimaryGroup() != null) {
         buttons = new Canvas[] {addButton, addGroupButton, cancelButton };
       } else {
        buttons = new Canvas[] {addButton };
       }
       final CanvasWithOpsLayout<ListGrid> layout = new CanvasWithOpsLayout<ListGrid>("Choose a shared folder to add to your shared folders.", grid, buttons);     
       setWidget(PopOutWindowBuilder.titled(ConstantsInstances.COMPONENT_INSTANCE.findSharedTitle()).sized(600, 400).withIcon(Resources.SHARED_FOLDER_16).withContents(layout).get());
     }
 
     private ListGrid getGrid() {
       final DataSourceTextField idField = new DataSourceTextField("id");
       final DataSourceTextField nameField = new DataSourceTextField("name", "Name");
       final DataSourceTextField descriptionField = new DataSourceTextField("description", "Description");
       final DataSourceTextField ownerField = new DataSourceTextField("owner", "Owner");
       final DataSourceDateField creationDateField = new DataSourceDateField("creationDate", "Creation");
       creationDateField.setCanFilter(false);
       idField.setPrimaryKey(true);
 
       final DataSource dataSource = new DataSource();
       dataSource.setFields(nameField, descriptionField, ownerField, creationDateField);
       dataSource.setClientOnly(true);
 
       final ListGrid listGrid = new ListGrid();
       listGrid.setShowAllRecords(false);
       listGrid.setUseAllDataSourceFields(true);
       listGrid.setShowFilterEditor(true);
       listGrid.setDataSource(dataSource);
       listGrid.setAutoFetchData(true);
       return listGrid;
     }
     
     private void addGroupSharedFolder(final String folderId) {
       final AsyncCallbackImpl<Void> callback = getCallback(Locations.MY_GROUP_SHARED_FOLDERS_ID);
       ObjectService.Util.getInstance().addGroupSharedFolder(session.getPrimaryGroup().getId(), folderId, callback);      
     }
 
     private AsyncCallbackImpl<Void> getCallback(final String updateLocationId) {
       final AsyncCallbackImpl<Void> callback = new AsyncCallbackImpl<Void>() {
         public void onSuccess(final Void ignore) {
           LocationUpdateMediator.getInstance().onEvent(new UpdateEvent(updateLocationId, null));
           get().destroy();
         }
       };
       return callback;
     }
 
     private void addSharedFolder(final String folderId) {
       final AsyncCallbackImpl<Void> callback = getCallback(Locations.MY_SHARED_FOLDERS_ID);
       ObjectService.Util.getInstance().addSharedFolder(folderId, callback);
     }
 
     private void populateData(final ListGrid grid) {
       final AsyncCallbackImpl<List<SharedFolder>> callback = new AsyncCallbackImpl<List<SharedFolder>>() {
         @Override
         public void onSuccess(final List<SharedFolder> sharedFolders) {
           grid.setEmptyMessage(ConstantsInstances.COMPONENT_INSTANCE.findSharedEmpty());
           for(final SharedFolder sharedFolder : sharedFolders) {
             grid.getDataSource().addData(SearchResultUtils.toRecord(sharedFolder));
           }
         }
       };
       LocalSearchService.Util.getInstance().getSharedFolders(callback);
     }
 
   }
 
   public WindowComponent<Window> get() {
     return new FindSharedFoldersWindowComponentImpl();
   }
 
 }
