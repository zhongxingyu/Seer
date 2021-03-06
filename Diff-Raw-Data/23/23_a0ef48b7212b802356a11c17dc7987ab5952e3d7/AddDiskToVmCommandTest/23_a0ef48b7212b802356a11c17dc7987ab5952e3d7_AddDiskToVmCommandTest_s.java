 package org.ovirt.engine.core.bll;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.doNothing;
 import static org.mockito.Mockito.doReturn;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.when;
 import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.ovirt.engine.core.common.action.AddDiskParameters;
 import org.ovirt.engine.core.common.businessentities.DiskImage;
 import org.ovirt.engine.core.common.businessentities.DiskInterface;
 import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
 import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
 import org.ovirt.engine.core.common.businessentities.StorageType;
 import org.ovirt.engine.core.common.businessentities.VM;
 import org.ovirt.engine.core.common.businessentities.VMStatus;
 import org.ovirt.engine.core.common.businessentities.VolumeFormat;
 import org.ovirt.engine.core.common.businessentities.VolumeType;
 import org.ovirt.engine.core.common.businessentities.storage_domains;
 import org.ovirt.engine.core.common.businessentities.storage_pool;
 import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
 import org.ovirt.engine.core.common.config.ConfigValues;
 import org.ovirt.engine.core.compat.Guid;
 import org.ovirt.engine.core.dal.VdcBllMessages;
 import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBaseMockUtils;
 import org.ovirt.engine.core.dao.StorageDomainDAO;
 import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
 import org.ovirt.engine.core.dao.StoragePoolDAO;
 import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
 import org.ovirt.engine.core.dao.VmDAO;
 import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;
 import org.ovirt.engine.core.utils.MockConfigRule;
 import org.ovirt.engine.core.utils.log.Log;
 import org.ovirt.engine.core.utils.log.LogFactory;
 
 @RunWith(MockitoJUnitRunner.class)
 public class AddDiskToVmCommandTest {
     private static int MAX_BLOCK_SIZE = 8192;
     private static int FREE_SPACE_LOW = 10;
     private static int FREE_SPACE_CRITICAL_LOW_IN_GB = 5;
 
     @Rule
     public static MockConfigRule mcr = new MockConfigRule(
             mockConfig(ConfigValues.MaxBlockDiskSize, MAX_BLOCK_SIZE),
             mockConfig(ConfigValues.FreeSpaceLow, FREE_SPACE_LOW),
             mockConfig(ConfigValues.FreeSpaceCriticalLowInGB, FREE_SPACE_CRITICAL_LOW_IN_GB)
             );
 
     @Mock
     private StorageDomainDAO storageDomainDAO;
 
     @Mock
     private StorageDomainStaticDAO storageDomainStaticDAO;
 
     @Mock
     private StoragePoolIsoMapDAO storagePoolIsoMapDAO;
 
     @Mock
     private VmNetworkInterfaceDAO vmNetworkInterfaceDAO;
 
     @Mock
     private VmDAO vmDAO;
 
     @Mock
     private StoragePoolDAO storagePoolDAO;
 
     /**
      * The command under test.
      */
     private AddDiskCommand<AddDiskParameters> command;
 
     @Test
     public void canDoActionSucceedsOnDiskDomainCheckWhenNoDisks() throws Exception {
         Guid storageId = Guid.NewGuid();
         initializeCommand(storageId);
 
         mockVm();
         mockStorageDomain(storageId);
         mockStoragePoolIsoMap();
 
         runAndAssertCanDoActionSuccess();
     }
 
     @Test
     public void canDoActionSucceedsOnDiskDomainCheckWhenEmptyStorageGuidInParams() throws Exception {
         initializeCommand(Guid.Empty);
         Guid storageId = Guid.NewGuid();
 
         mockVmWithDisk(storageId);
         mockStorageDomain(storageId);
         mockStoragePoolIsoMap();
 
         runAndAssertCanDoActionSuccess();
     }
 
     @Test
     public void canDoActionSucceedsOnDiskDomainCheckWhenStorageGuidInParamsMatches() throws Exception {
         Guid storageId = Guid.NewGuid();
         initializeCommand(storageId);
 
         mockVmWithDisk(storageId);
         mockStorageDomain(storageId);
         mockStoragePoolIsoMap();
 
         runAndAssertCanDoActionSuccess();
     }
 
     @Test
     public void canDoActionSucceedsOnDiskDomainCheckWhenStorageGuidInParamsMismatches() throws Exception {
         Guid storageId = Guid.NewGuid();
         initializeCommand(storageId);
 
         mockVmWithDisk(Guid.NewGuid());
         mockStorageDomain(storageId);
         mockStoragePoolIsoMap();
 
         assertTrue(command.canDoAction());
     }
 
     @Test
     public void canDoActionFailsOnNullDiskInterface() throws Exception {
         Guid storageId = Guid.NewGuid();
         DiskImage image = new DiskImage();
         image.setvolume_format(VolumeFormat.COW);
         image.setvolume_type(VolumeType.Preallocated);
         AddDiskParameters params = new AddDiskParameters(Guid.NewGuid(), image);
         initializeCommand(storageId, params);
         assertFalse(command.validateInputs());
         assertTrue(command.getReturnValue().getCanDoActionMessages().contains("VALIDATION.DISK_INTERFACE.NOT_NULL"));
     }
 
     @Test
     public void canDoActionThinProvisioningSpaceCheckSucceeds() throws Exception {
         final int availableSize = 6;
         final int usedSize = 4;
         Guid sdid = Guid.NewGuid();
         initializeCommand(sdid, VolumeType.Sparse);
 
         mockVm();
         storage_domains domains = mockStorageDomain(sdid, availableSize, usedSize);
         mockStoragePoolIsoMap();
         mockStorageDomainSpaceChecker(domains, true);
 
         assertTrue(command.canDoAction());
     }
 
     @Test
     public void canDoActionThinProvisioningSpaceCheckFailsSize() {
         final int availableSize = 4;
         final int usedSize = 6;
         Guid sdid = Guid.NewGuid();
         initializeCommand(sdid, VolumeType.Sparse);
 
         mockVm();
         storage_domains domains = mockStorageDomain(sdid, availableSize, usedSize);
         mockStoragePoolIsoMap();
         mockStorageDomainSpaceChecker(domains, false);
 
         assertFalse(command.canDoAction());
         assertTrue(command.getReturnValue()
                 .getCanDoActionMessages()
                 .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
     }
 
     @Test
     public void canDoActionThinProvisioningSpaceCheckFailsPct() {
         final int availableSize = 9;
         final int usedSize = 191;
         Guid sdid = Guid.NewGuid();
         initializeCommand(sdid, VolumeType.Sparse);
 
         mockVm();
         storage_domains domains = mockStorageDomain(sdid, availableSize, usedSize);
         mockStoragePoolIsoMap();
         mockStorageDomainSpaceChecker(domains, false);
 
         assertFalse(command.canDoAction());
         assertTrue(command.getReturnValue()
                 .getCanDoActionMessages()
                 .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
     }
 
     @Test
     public void canDoActionPreallocatedSpaceCheckSucceeds() {
         final int availableSize = 12;
         final int usedSize = 8;
         Guid sdid = Guid.NewGuid();
         initializeCommand(sdid, VolumeType.Preallocated);
 
         mockVm();
         storage_domains domains = mockStorageDomain(sdid, availableSize, usedSize);
         mockStoragePoolIsoMap();
         mockStorageDomainSpaceCheckerRequest(domains, true);
         assertTrue(command.canDoAction());
     }
 
     @Test
     public void canDoActionPreallocatedSpaceCheckFailsSize() {
         final int availableSize = 8;
         final int usedSize = 7;
         Guid sdid = Guid.NewGuid();
         initializeCommand(sdid, VolumeType.Preallocated);
 
         mockVm();
         storage_domains domains = mockStorageDomain(sdid, availableSize, usedSize);
         mockStoragePoolIsoMap();
         mockStorageDomainSpaceCheckerRequest(domains, false);
 
         assertFalse(command.canDoAction());
         assertTrue(command.getReturnValue()
                 .getCanDoActionMessages()
                 .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
     }
 
     @Test
     public void canDoActionPreallocatedSpaceCheckFailsPct() {
         final int availableSize = 9;
         final int usedSize = 191;
         Guid sdid = Guid.NewGuid();
         initializeCommand(sdid, VolumeType.Preallocated);
 
         mockVm();
         storage_domains domains = mockStorageDomain(sdid, availableSize, usedSize);
         mockStoragePoolIsoMap();
         mockStorageDomainSpaceCheckerRequest(domains, false);
 
         assertFalse(command.canDoAction());
         assertTrue(command.getReturnValue()
                 .getCanDoActionMessages()
                 .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
     }
 
     /**
      * CanDoAction should succeed when the requested disk space is less or equal than 'MaxBlockDiskSize'
      */
     @Test
     public void canDoActionMaxBlockDiskSizeCheckSucceeds() {
         Guid storageId = Guid.NewGuid();
         AddDiskParameters parameters = createParameters();
         parameters.setDiskInfo(createDiskImage(MAX_BLOCK_SIZE));
         initializeCommand(storageId, parameters);
 
         mockVm();
         mockStorageDomain(storageId, StorageType.ISCSI);
         mockStoragePoolIsoMap();
 
         runAndAssertCanDoActionSuccess();
     }
 
     /**
      * CanDoAction should fail when the requested disk space is larger than 'MaxBlockDiskSize'
      */
     @Test
     public void canDoActionMaxBlockDiskSizeCheckFails() {
         Guid storageId = Guid.NewGuid();
         AddDiskParameters parameters = createParameters();
         parameters.setDiskInfo(createDiskImage(MAX_BLOCK_SIZE * 2));
         initializeCommand(storageId, parameters);
 
         mockVm();
         mockStorageDomain(storageId, StorageType.ISCSI);
         mockStoragePoolIsoMap();
 
         assertFalse(command.canDoAction());
         assertTrue(command.getReturnValue()
                 .getCanDoActionMessages()
                 .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_MAX_SIZE_EXCEEDED.toString()));
     }
 
     /**
      * Initialize the command for testing, using the given storage domain id for the parameters.
      *
      * @param storageId
      *            Storage domain id for the parameters
      */
     private void initializeCommand(Guid storageId) {
         initializeCommand(storageId, VolumeType.Unassigned);
     }
 
     private void initializeCommand(Guid storageId, VolumeType volumeType) {
         AddDiskParameters parameters = createParameters();
         parameters.setStorageDomainId(storageId);
         if (volumeType == VolumeType.Preallocated) {
             parameters.setDiskInfo(createPreallocDiskImage());
         } else if (volumeType == VolumeType.Sparse) {
             parameters.setDiskInfo(createSparseDiskImage());
         }
         initializeCommand(storageId, parameters);
     }
 
     private void initializeCommand(Guid storageId, AddDiskParameters params) {
         params.setStorageDomainId(storageId);
         command = spy(new AddDiskCommand<AddDiskParameters>(params));
         doReturn(true).when(command).acquireLockInternal();
         doReturn(storageDomainDAO).when(command).getStorageDomainDAO();
         doReturn(storagePoolIsoMapDAO).when(command).getStoragePoolIsoMapDao();
         doReturn(storageDomainStaticDAO).when(command).getStorageDomainStaticDao();
         doReturn(storagePoolDAO).when(command).getStoragePoolDAO();
         doReturn(vmNetworkInterfaceDAO).when(command).getVmNetworkInterfaceDAO();
         AuditLogableBaseMockUtils.mockVmDao(command, vmDAO);
         doNothing().when(command).updateDisksFromDb();
         doReturn(true).when(command).checkImageConfiguration();
         doReturn(true).when(command).performImagesChecks(any(VM.class));
         doReturn(true).when(command).isStorageDomainBelowThresholds(any(storage_domains.class));
     }
 
     /**
      * Mock a VM that has a disk.
      *
      * @param storageId
      *            Storage domain id of the disk.
      */
     private void mockVmWithDisk(Guid storageId) {
         DiskImage image = new DiskImage();
         image.setstorage_ids(new ArrayList<Guid>(Arrays.asList(storageId)));
         mockVm().getDiskMap().put(image.getId(), image);
     }
 
     /**
      * Mock a good {@link storage_pool_iso_map}.
      */
     private void mockStoragePoolIsoMap() {
         storage_pool_iso_map spim = new storage_pool_iso_map();
         when(storagePoolIsoMapDAO.get(any(StoragePoolIsoMapId.class))).thenReturn(spim);
     }
 
     /**
      * Mock a VM.
      */
     private VM mockVm() {
         VM vm = new VM();
         vm.setstatus(VMStatus.Down);
         vm.setstorage_pool_id(Guid.NewGuid());
         when(vmDAO.get(command.getParameters().getVmId())).thenReturn(vm);
 
         return vm;
     }
 
     /**
      * Mock a {@link storage_domains}.
      *
      * @param storageId
      *            Id of the domain.
      */
     private storage_domains mockStorageDomain(Guid storageId) {
         return mockStorageDomain(storageId, 6, 4, StorageType.UNKNOWN);
     }
 
     private storage_domains mockStorageDomain(Guid storageId, StorageType storageType) {
         return mockStorageDomain(storageId, 6, 4, storageType);
     }
 
     private storage_domains mockStorageDomain(Guid storageId, int availableSize, int usedSize) {
         return mockStorageDomain(storageId, 6, 4, StorageType.UNKNOWN);
     }
 
     private storage_domains mockStorageDomain(Guid storageId, int availableSize, int usedSize, StorageType storageType) {
         Guid storagePoolId = Guid.NewGuid();
         storage_pool sp = new storage_pool();
         sp.setId(storagePoolId);
         when(storagePoolDAO.get(storagePoolId)).thenReturn(sp);
 
         storage_domains sd = new storage_domains();
         sd.setavailable_disk_size(availableSize);
         sd.setused_disk_size(usedSize);
         sd.setstorage_pool_id(storagePoolId);
         sd.setstatus(StorageDomainStatus.Active);
         sd.setstorage_type(storageType);
         when(storageDomainDAO.get(storageId)).thenReturn(sd);
         when(storageDomainDAO.getAllForStorageDomain(storageId)).thenReturn(Collections.singletonList(sd));
         when(storageDomainDAO.getForStoragePool(storageId, storagePoolId)).thenReturn(sd);
 
         return sd;
     }
 
     /**
      * Run the canDoAction and assert that it succeeds
      */
     private void runAndAssertCanDoActionSuccess() {
         boolean canDoAction = command.canDoAction();
         log.info(command.getReturnValue().getCanDoActionMessages());
         assertTrue(canDoAction);
     }
 
     /**
      * @return Valid parameters for the command.
      */
     private static AddDiskParameters createParameters() {
         DiskImage image = new DiskImage();
         image.setDiskInterface(DiskInterface.IDE);
         AddDiskParameters parameters = new AddDiskParameters(Guid.NewGuid(), image);
         return parameters;
     }
 
     private static DiskImage createSparseDiskImage() {
         DiskImage image = new DiskImage();
         image.setvolume_type(VolumeType.Sparse);
         image.setDiskInterface(DiskInterface.IDE);
         return image;
     }
 
     private static DiskImage createPreallocDiskImage() {
         DiskImage image = new DiskImage();
         image.setvolume_type(VolumeType.Preallocated);
         image.setDiskInterface(DiskInterface.IDE);
         image.setSizeInGigabytes(5);
         return image;
     }
 
     private static DiskImage createDiskImage(long sizeInGigabytes) {
         DiskImage image = new DiskImage();
         image.setSizeInGigabytes(sizeInGigabytes);
         return image;
     }
 
     private void mockStorageDomainSpaceChecker(storage_domains domain, boolean succeeded) {
         doReturn(succeeded).when(command).isStorageDomainBelowThresholds(domain);
     }
 
     private void mockStorageDomainSpaceCheckerRequest(storage_domains domain, boolean succeeded) {
         doReturn(succeeded).when(command).doesStorageDomainhaveSpaceForRequest(domain);
     }
 
     private static final Log log = LogFactory.getLog(AddDiskToVmCommandTest.class);
 }
