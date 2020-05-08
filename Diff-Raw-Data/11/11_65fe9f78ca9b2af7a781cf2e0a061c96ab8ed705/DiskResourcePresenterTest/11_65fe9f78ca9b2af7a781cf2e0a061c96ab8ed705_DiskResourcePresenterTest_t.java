 /**
  *
  */
 package org.iplantc.core.uidiskresource.client.presenters;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.iplantc.core.resources.client.messages.IplantDisplayStrings;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.models.DiskResourceAutoBeanFactory;
 import org.iplantc.core.uidiskresource.client.models.DiskResourceMetadata;
 import org.iplantc.core.uidiskresource.client.models.File;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.core.uidiskresource.client.models.Permissions;
 import org.iplantc.core.uidiskresource.client.presenters.handlers.ToolbarButtonVisibilityGridHandler;
 import org.iplantc.core.uidiskresource.client.presenters.handlers.ToolbarButtonVisibilityNavigationHandler;
 import org.iplantc.core.uidiskresource.client.search.models.DataSearchAutoBeanFactory;
 import org.iplantc.core.uidiskresource.client.services.DiskResourceServiceFacade;
 import org.iplantc.core.uidiskresource.client.views.DiskResourceView;
 import org.iplantc.core.uidiskresource.client.views.widgets.DiskResourceViewToolbar;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.gwt.core.client.Callback;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.sencha.gxt.data.shared.loader.TreeLoader;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
 
 
 
 /**
  * @author jstroot
  *
  */
 @RunWith(JMock.class)
 public class DiskResourcePresenterTest {
 
     public Mockery context = new JUnit4Mockery();
     private DiskResourceView view;
     private DiskResourceView.Proxy proxy;
     private DiskResourceServiceFacade diskResourceService;
     private IplantDisplayStrings display;
     private DiskResourceAutoBeanFactory drFactory;
     private DataSearchAutoBeanFactory dataSearchFactory;
 
     /**
      * @throws java.lang.Exception
      */
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         UserInfo userInfo = UserInfo.getInstance();
         userInfo.setUsername("TestUser");
         userInfo.setTrashPath("Test_TrashPath");
     }
 
     /**
      * @throws java.lang.Exception
      */
     @AfterClass
     public static void tearDownAfterClass() throws Exception {}
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
         view = context.mock(DiskResourceView.class);
         proxy = context.mock(DiskResourceView.Proxy.class);
         diskResourceService = context.mock(DiskResourceServiceFacade.class);
         display = context.mock(IplantDisplayStrings.class);
         drFactory = context.mock(DiskResourceAutoBeanFactory.class);
         dataSearchFactory = context.mock(DataSearchAutoBeanFactory.class);
     }
 
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws Exception {}
 
     @SuppressWarnings("unchecked")
     private void checkConstruction() {
         context.checking(new Expectations() {
             {
                 DiskResourceViewToolbar toolbar = context.mock(DiskResourceViewToolbar.class);
 
                 oneOf(view).getTreeStore();
                 oneOf(view).getToolbar();
                 will(returnValue(toolbar));
                 oneOf(view).addDiskResourceSelectChangedHandler(
                         with(aNonNull(ToolbarButtonVisibilityGridHandler.class)));
                 oneOf(view).addFolderSelectionHandler(
                         with(aNonNull(ToolbarButtonVisibilityNavigationHandler.class)));
 
                 oneOf(view).setTreeLoader(with(aNonNull(TreeLoader.class)));
                 oneOf(view).setPresenter(with(aNonNull(DiskResourceView.Presenter.class)));
                 oneOf(proxy).setPresenter(with(aNonNull(DiskResourceView.Presenter.class)));
                 oneOf(diskResourceService).getDataSearchHistory(with(aNonNull(AsyncCallback.class)));
                 oneOf(diskResourceService).getUserTrashPath(with(aNonNull(String.class)), with(aNonNull(AsyncCallback.class)));
 
                 oneOf(toolbar).setNewFolderButtonEnabled(with(false));
                 oneOf(toolbar).setRefreshButtonEnabled(with(false));
                 oneOf(toolbar).setDownloadsEnabled(with(false));
                 oneOf(toolbar).setBulkDownloadButtonEnabled(with(false));
                 oneOf(toolbar).setSimpleDowloadButtonEnabled(with(false));
                 oneOf(toolbar).setRenameButtonEnabled(with(false));
                 oneOf(toolbar).setShareButtonEnabled(with(false));
                 oneOf(toolbar).setDeleteButtonEnabled(with(false));
                 oneOf(toolbar).setRestoreMenuItemEnabled(with(false));
                 oneOf(toolbar).setEditEnabled(with(false));
             }
         });
     }
 
     /**
      * Test method for {@link DiskResourceView.Presenter#onFoldersSelected(Folder)} when a single folder
      * is passed in which is already loaded.
      */
     @SuppressWarnings("unchecked")
     @Test
     public void testOnFolderSelected_OneFolder() {
 
         checkConstruction();
         DiskResourceView.Presenter presenter = new DiskResourcePresenterImpl(view, proxy,
                 diskResourceService, display, drFactory, dataSearchFactory);
 
         final Folder folder = context.mock(Folder.class);
 
         // Test for when a non-null folder is loaded
         context.checking(new Expectations() {
             {
                 oneOf(view).deSelectDiskResources();
                 oneOf(view).isLoaded(folder);
                 will(returnValue(true));
                 oneOf(view).setDiskResources(with(aNonNull(Set.class)));
                 oneOf(view).showDataListingWidget();
                 exactly(2).of(folder).getFolders();
                 will(returnValue(Lists.newArrayList()));
                 exactly(2).of(folder).getFiles();
                 will(returnValue(Lists.newArrayList()));
             }
         });
 
         presenter.onFolderSelected(folder);
 
         context.assertIsSatisfied();
 
         // Test for when one folder is not loaded
         context.checking(new Expectations() {
             {
                 oneOf(view).deSelectDiskResources();
                 oneOf(view).showDataListingWidget();
                 oneOf(view).isLoaded(folder);
                 will(returnValue(false));
                 oneOf(proxy).load(with(folder), with(aNonNull(Callback.class)));
             }
         });
 
         presenter.onFolderSelected(folder);
 
         context.assertIsSatisfied();
     }
 
     /**
      * This tests the case when {@link DiskResourceView.Presenter#onFolderLoad(Folder, ArrayList)} is
      * called with a
      * folder which <em>is not equal</em> to the currently selected folder.
      * The expected result here is that the view will not be updated.
      */
 //    @SuppressWarnings("unchecked")
 //    @Test
 //    public void testOnFolderLoadWithNonMatchingSelectedFolder() {
 //
 //        final Folder folder = context.mock(Folder.class, "inputFolder");
 //        final Folder retFolder = context.mock(Folder.class, "retFolder");
 //        checkConstruction();
 //        DiskResourcePresenterImpl presenter = new DiskResourcePresenterImpl(view, proxy,
 //                diskResourceService, display, drFactory, dataSearchFactory);
 //        context.checking(new Expectations() {
 //            {
 //                atLeast(1).of(view).getSelectedFolder();
 //                will(returnValue(retFolder));
 //                never(view).setDiskResources(with(any(Set.class)));
 //            }
 //        });
 //
 //        presenter.onFolderLoad(folder, null);
 //
 //        context.assertIsSatisfied();
 //    }
 
     /**
      * This tests the case when {@link DiskResourceView.Presenter#onFolderLoad(Folder, ArrayList)} is
      * called with a folder which <em>is equal</em> to the currently selected folder.
      */
 //    @Test
 //    public void testOnFolderLoadWithMatchingSelectedFolder() {
 //
 //        final Folder folder = context.mock(Folder.class);
 //        final Set<DiskResource> folderChildren = Sets.newHashSet();
 //        checkConstruction();
 //        DiskResourcePresenterImpl presenter = new DiskResourcePresenterImpl(view, proxy,
 //                diskResourceService, display, drFactory, dataSearchFactory);
 //        context.checking(new Expectations() {
 //            {
 //                atLeast(1).of(view).getSelectedFolder();
 //                will(returnValue(folder));
 //                oneOf(view).setDiskResources(with(folderChildren));
 //            }
 //        });
 //
 //        presenter.onFolderLoad(folder, folderChildren);
 //
 //        context.assertIsSatisfied();
 //    }
 
     @Test
     public void testToolbarVisibility_FolderSelection_HasOwnership() {
 
         final DiskResourceViewToolbar toolbar = context.mock(DiskResourceViewToolbar.class, "tb");
         final Folder folder = context.mock(Folder.class);
         final Permissions permissions = context.mock(Permissions.class);
         ToolbarButtonVisibilityNavigationHandler navHandler = new ToolbarButtonVisibilityNavigationHandler(
                 toolbar);
         ToolbarButtonVisibilityGridHandler gridHandler = new ToolbarButtonVisibilityGridHandler(toolbar);
         // Test the case where one folder is selected, and user IS the owner
         context.checking(new Expectations() {
             {
                 oneOf(toolbar).setUploadsEnabled(with(true));
                 oneOf(toolbar).setDownloadsEnabled(with(true));
                 oneOf(toolbar).setEditEnabled(with(true));
 
                 oneOf(toolbar).setBulkUploadEnabled(with(true));
                 oneOf(toolbar).setSimpleUploadEnabled(with(true));
                 oneOf(toolbar).setImportButtonEnabled(with(true));
                 oneOf(toolbar).setNewFolderButtonEnabled(with(true));
                 oneOf(toolbar).setRefreshButtonEnabled(with(true));
                 oneOf(toolbar).setSimpleDowloadButtonEnabled(with(false));
                 oneOf(toolbar).setBulkDownloadButtonEnabled(with(true));
                 oneOf(toolbar).setRenameButtonEnabled(with(true));
                 oneOf(toolbar).setDeleteButtonEnabled(with(true));
                 oneOf(toolbar).setShareButtonEnabled(with(true));
                oneOf(toolbar).setShareMenuItemEnabled(with(true));
                oneOf(toolbar).setDataLinkMenuItemEnabled(with(false));
                 oneOf(toolbar).setRestoreMenuItemEnabled(with(false));
                 oneOf(toolbar).setMetaDatMenuItemEnabled(with(true));
                 allowing(permissions).isOwner();
                 will(returnValue(true));
                 allowing(folder).getPermissions();
                 will(returnValue(permissions));
 
                 allowing(folder).getId();
                 will(returnValue(""));
 
             }
         });
 
         navHandler.onSelection(new SelectionEvent<Folder>(folder) {
         });
         gridHandler.onSelection(new SelectionEvent<DiskResource>(folder) {
         });
 
         context.assertIsSatisfied();
 
     }
 
     @Test
     public void testToolbarVisibility_FolderSelection_NoOwnership() {
 
         final DiskResourceViewToolbar toolbar = context.mock(DiskResourceViewToolbar.class);
         final Folder folder = context.mock(Folder.class);
         final Permissions permissions = context.mock(Permissions.class);
         ToolbarButtonVisibilityNavigationHandler navHandler = new ToolbarButtonVisibilityNavigationHandler(
                 toolbar);
         ToolbarButtonVisibilityGridHandler gridHandler = new ToolbarButtonVisibilityGridHandler(toolbar);
 
         // Test the case where one folder is selected, and user IS NOT the owner
         context.checking(new Expectations() {
             {
                 oneOf(toolbar).setUploadsEnabled(with(false));
                 oneOf(toolbar).setDownloadsEnabled(with(true));
                 oneOf(toolbar).setEditEnabled(with(false));
                 oneOf(toolbar).setBulkUploadEnabled(with(false));
                 oneOf(toolbar).setSimpleUploadEnabled(with(false));
                 oneOf(toolbar).setImportButtonEnabled(with(false));
                 oneOf(toolbar).setNewFolderButtonEnabled(with(false));
                 oneOf(toolbar).setRefreshButtonEnabled(with(true));
                 oneOf(toolbar).setSimpleDowloadButtonEnabled(with(false));
                 oneOf(toolbar).setBulkDownloadButtonEnabled(with(true));
                 oneOf(toolbar).setRenameButtonEnabled(with(false));
                 oneOf(toolbar).setDeleteButtonEnabled(with(false));
                 oneOf(toolbar).setShareButtonEnabled(with(false));
                oneOf(toolbar).setShareMenuItemEnabled(with(false));
                oneOf(toolbar).setDataLinkMenuItemEnabled(with(false));
                 oneOf(toolbar).setRestoreMenuItemEnabled(with(false));
                 oneOf(toolbar).setMetaDatMenuItemEnabled(with(false));
 
                 allowing(permissions).isOwner();
                 will(returnValue(false));
                 allowing(folder).getPermissions();
                 will(returnValue(permissions));
 
                 allowing(folder).getId();
                 will(returnValue(""));
             }
         });
 
         navHandler.onSelection(new SelectionEvent<Folder>(folder) {
         });
         gridHandler.onSelection(new SelectionEvent<DiskResource>(folder) {
         });
 
         context.assertIsSatisfied();
     }
 
     @Test
     public void testToolbarVisibility_singleDiskResSelection_HasOwnership() {
 
         final DiskResourceViewToolbar toolbar = context.mock(DiskResourceViewToolbar.class);
         final Folder folder = context.mock(Folder.class);
         final Permissions permissions = context.mock(Permissions.class);
         ToolbarButtonVisibilityNavigationHandler navHandler = new ToolbarButtonVisibilityNavigationHandler(
                 toolbar);
         ToolbarButtonVisibilityGridHandler gridHandler = new ToolbarButtonVisibilityGridHandler(toolbar);
         // Test the case where one folder is selected, and user IS the owner
         context.checking(new Expectations() {
             {
                 oneOf(toolbar).setUploadsEnabled(with(true));
                 oneOf(toolbar).setDownloadsEnabled(with(true));
                 oneOf(toolbar).setEditEnabled(with(true));
 
                 oneOf(toolbar).setBulkUploadEnabled(with(true));
                 oneOf(toolbar).setSimpleUploadEnabled(with(true));
                 oneOf(toolbar).setImportButtonEnabled(with(true));
                 oneOf(toolbar).setNewFolderButtonEnabled(with(true));
                 oneOf(toolbar).setRefreshButtonEnabled(with(true));
                 oneOf(toolbar).setSimpleDowloadButtonEnabled(with(false));
                 oneOf(toolbar).setBulkDownloadButtonEnabled(with(true));
                 oneOf(toolbar).setRenameButtonEnabled(with(true));
                 oneOf(toolbar).setDeleteButtonEnabled(with(true));
                 oneOf(toolbar).setShareButtonEnabled(with(true));
                oneOf(toolbar).setShareMenuItemEnabled(with(true));
                oneOf(toolbar).setDataLinkMenuItemEnabled(with(false));
                 oneOf(toolbar).setRestoreMenuItemEnabled(with(false));
                 oneOf(toolbar).setMetaDatMenuItemEnabled(with(true));
 
                 allowing(permissions).isOwner();
                 will(returnValue(true));
                 allowing(folder).getPermissions();
                 will(returnValue(permissions));
 
                 allowing(folder).getId();
                 will(returnValue(""));
             }
         });
 
         List<Folder> folderList = Lists.newArrayList();
         folderList.add(folder);
         List<DiskResource> list = Lists.newArrayList();
         list.add(folder);
         navHandler.onSelectionChanged(new SelectionChangedEvent<Folder>(folderList) {
         });
         gridHandler.onSelectionChanged(new SelectionChangedEvent<DiskResource>(list));
 
         context.assertIsSatisfied();
 
     }
 
     @Test
     public void testToolbarVisibility_singleDiskResSelection_NoOwnership() {
 
         final DiskResourceViewToolbar toolbar = context.mock(DiskResourceViewToolbar.class);
         final Folder folder = context.mock(Folder.class);
         final Permissions permissions = context.mock(Permissions.class);
         ToolbarButtonVisibilityNavigationHandler navHandler = new ToolbarButtonVisibilityNavigationHandler(
                 toolbar);
         ToolbarButtonVisibilityGridHandler gridHandler = new ToolbarButtonVisibilityGridHandler(toolbar);
 
         // Test the case where one folder is selected, and user IS NOT the owner
         context.checking(new Expectations() {
             {
                 oneOf(toolbar).setUploadsEnabled(with(false));
                 oneOf(toolbar).setDownloadsEnabled(with(true));
                 oneOf(toolbar).setEditEnabled(with(false));
 
                 oneOf(toolbar).setBulkUploadEnabled(with(false));
                 oneOf(toolbar).setSimpleUploadEnabled(with(false));
                 oneOf(toolbar).setImportButtonEnabled(with(false));
                 oneOf(toolbar).setNewFolderButtonEnabled(with(false));
                 oneOf(toolbar).setRefreshButtonEnabled(with(true));
                 oneOf(toolbar).setSimpleDowloadButtonEnabled(with(false));
                 oneOf(toolbar).setBulkDownloadButtonEnabled(with(true));
                 oneOf(toolbar).setRenameButtonEnabled(with(false));
                 oneOf(toolbar).setDeleteButtonEnabled(with(false));
                 oneOf(toolbar).setShareButtonEnabled(with(false));
                oneOf(toolbar).setShareMenuItemEnabled(with(false));
                oneOf(toolbar).setDataLinkMenuItemEnabled(with(false));
                 oneOf(toolbar).setRestoreMenuItemEnabled(with(false));
                 oneOf(toolbar).setMetaDatMenuItemEnabled(with(false));
 
                 allowing(permissions).isOwner();
                 will(returnValue(false));
                 allowing(folder).getPermissions();
                 will(returnValue(permissions));
 
                 allowing(folder).getId();
                 will(returnValue(""));
             }
         });
 
         List<Folder> folderList = Lists.newArrayList();
         folderList.add(folder);
         List<DiskResource> list = Lists.newArrayList();
         list.add(folder);
         navHandler.onSelectionChanged(new SelectionChangedEvent<Folder>(folderList) {
         });
         gridHandler.onSelectionChanged(new SelectionChangedEvent<DiskResource>(list));
 
         context.assertIsSatisfied();
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testDoDelete_diskResourcesSelected_noFolders_isOwner() {
         checkConstruction();
         DiskResourceView.Presenter presenter = new DiskResourcePresenterImpl(view, proxy,
                 diskResourceService, display, drFactory, dataSearchFactory);
         final File file = context.mock(File.class);
         final Permissions permissions = context.mock(Permissions.class);
         context.checking(new Expectations() {
             {
                 oneOf(display).loadingMask();
                 oneOf(view).mask(with(aNonNull(String.class)));
 
                 allowing(view).getSelectedDiskResources();
                 will(returnValue(Sets.newHashSet(file)));
                 allowing(file).getPermissions();
                 will(returnValue(permissions));
                 allowing(permissions).isOwner();
                 will(returnValue(true));
 
                 oneOf(view).getSelectedFolder();
                 oneOf(diskResourceService).deleteDiskResources(with(Sets.newHashSet(file)),
                         with(aNonNull(AsyncCallback.class)));
             }
         });
 
         presenter.doDelete();
 
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testDoDelete_diskResourcesSelected_withFolders_isOwner() {
         checkConstruction();
         DiskResourceView.Presenter presenter = new DiskResourcePresenterImpl(view, proxy,
                 diskResourceService, display, drFactory, dataSearchFactory);
         final File file = context.mock(File.class);
         final Folder folder = context.mock(Folder.class);
         final Set<DiskResource> resSet = Sets.newHashSet();
         resSet.add(file);
         resSet.add(folder);
         final Permissions permissions = context.mock(Permissions.class);
         context.checking(new Expectations() {
             {
                 oneOf(display).loadingMask();
                 oneOf(view).mask(with(aNonNull(String.class)));
 
                 allowing(view).getSelectedDiskResources();
                 will(returnValue(resSet));
                 allowing(file).getPermissions();
                 will(returnValue(permissions));
                 allowing(folder).getPermissions();
                 will(returnValue(permissions));
                 allowing(permissions).isOwner();
                 will(returnValue(true));
 
                 oneOf(view).getSelectedFolder();
                 oneOf(diskResourceService).deleteDiskResources(with(Sets.newHashSet(file, folder)),
                         with(aNonNull(AsyncCallback.class)));
             }
         });
 
         presenter.doDelete();
 
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testDoDelete_folderSelected_noDiskResourcesSelected_isOwner() {
         checkConstruction();
         DiskResourceView.Presenter presenter = new DiskResourcePresenterImpl(view, proxy,
                 diskResourceService, display, drFactory, dataSearchFactory);
         final Folder folder = context.mock(Folder.class);
         final Permissions permissions = context.mock(Permissions.class);
         context.checking(new Expectations() {
             {
                 oneOf(display).loadingMask();
                 oneOf(view).mask(with(aNonNull(String.class)));
                 allowing(view).getSelectedDiskResources();
                 will(returnValue(Sets.newHashSet()));
                 allowing(folder).getPermissions();
                 will(returnValue(permissions));
                 allowing(permissions).isOwner();
                 will(returnValue(true));
 
                 allowing(view).getSelectedFolder();
                 will(returnValue(folder));
                 oneOf(diskResourceService).deleteDiskResources(with(Sets.newHashSet(folder)),
                         with(aNonNull(AsyncCallback.class)));
             }
         });
 
         presenter.doDelete();
 
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testDoDelete_diskResourcesSelected_oneNotOwner() {
         checkConstruction();
         DiskResourceView.Presenter presenter = new DiskResourcePresenterImpl(view, proxy,
                 diskResourceService, display, drFactory, dataSearchFactory);
         final File file = context.mock(File.class);
         final Folder folder = context.mock(Folder.class);
         final Folder parentFolder = context.mock(Folder.class, "ParentFolder");
         final Permissions permissions = context.mock(Permissions.class, "Permissions_isOwner");
         final Permissions permissionsNotOwner = context.mock(Permissions.class, "Permissions_notOwner");
         final Set<DiskResource> resSet = Sets.newHashSet();
         resSet.add(file);
         resSet.add(folder);
         context.checking(new Expectations() {
             {
                 never(display).loadingMask();
                 never(view).mask(with(any(String.class)));
                 allowing(view).getSelectedDiskResources();
                 will(returnValue(resSet));
 
                 allowing(folder).getPermissions();
                 will(returnValue(permissions));
                 allowing(parentFolder).getPermissions();
                 will(returnValue(permissionsNotOwner));
                 allowing(file).getPermissions();
                 will(returnValue(permissionsNotOwner));
 
                 allowing(permissions).isOwner();
                 will(returnValue(true));
                 allowing(permissionsNotOwner).isOwner();
                 will(returnValue(false));
 
                 allowing(view).getSelectedFolder();
                 will(returnValue(parentFolder));
                 never(diskResourceService).deleteDiskResources(with(any(Set.class)),
                         with(any(AsyncCallback.class)));
             }
         });
 
         presenter.doDelete();
 
     }
 
     /**
      * This test simply verifies that the proper disk resource service method is invoked.
      */
     @SuppressWarnings("unchecked")
     @Test
     public void testDoRename() {
         checkConstruction();
         DiskResourceView.Presenter presenter = new DiskResourcePresenterImpl(view, proxy,
                 diskResourceService, display, drFactory, dataSearchFactory);
         final DiskResource resource = context.mock(DiskResource.class);
         final String newName = "new name";
         context.checking(new Expectations() {
             {
                 oneOf(display).loadingMask();
                 oneOf(view).mask(with(aNonNull(String.class)));
                 oneOf(diskResourceService).renameDiskResource(with(resource), with(newName),
                         with(any(AsyncCallback.class)));
             }
         });
 
         presenter.doRename(resource, newName);
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testDoFolderCreate() {
         checkConstruction();
         DiskResourceView.Presenter presenter = new DiskResourcePresenterImpl(view, proxy,
                 diskResourceService, display, drFactory, dataSearchFactory);
         final Folder parentFolder = context.mock(Folder.class);
         final String newFolderName = "new folder name";
         context.checking(new Expectations() {
             {
                 oneOf(display).loadingMask();
                 oneOf(view).mask(with(aNonNull(String.class)));
                 oneOf(diskResourceService).createFolder(with(parentFolder), with(newFolderName),
                         with(any(AsyncCallback.class)));
             }
         });
 
         presenter.doCreateNewFolder(parentFolder, newFolderName);
     }
 
     /**
      * This test simply verifies that the proper disk resource service method is invoked.
      */
     @SuppressWarnings("unchecked")
     @Test
     public void testGetMetadata() {
         checkConstruction();
         DiskResourceView.Presenter presenter = new DiskResourcePresenterImpl(view, proxy,
                 diskResourceService, display, drFactory, dataSearchFactory);
 
         final DiskResource resource = context.mock(DiskResource.class);
         context.checking(new Expectations() {
             {
                 oneOf(diskResourceService).getDiskResourceMetaData(with(resource),
                         with(any(AsyncCallback.class)));
             }
         });
 
         presenter.getDiskResourceMetadata(resource, null);
     }
 
     /**
      * This test simply verifies that the proper disk resource service method is invoked.
      */
     @SuppressWarnings("unchecked")
     @Test
     public void testSetMetadata() {
         checkConstruction();
         DiskResourceView.Presenter presenter = new DiskResourcePresenterImpl(view, proxy,
                 diskResourceService, display, drFactory, dataSearchFactory);
 
         final Set<DiskResourceMetadata> metadataToDelete = Sets.newHashSet();
         final Set<DiskResourceMetadata> metadataToAdd = Sets.newHashSet();
         final DiskResource resource = context.mock(DiskResource.class);
         context.checking(new Expectations() {
             {
                 oneOf(diskResourceService).setDiskResourceMetaData(with(resource), with(metadataToAdd),
                         with(metadataToDelete),
                         with(any(AsyncCallback.class)));
             }
         });
 
         presenter.setDiskResourceMetaData(resource, metadataToAdd, metadataToDelete, null);
     }
 
 }
