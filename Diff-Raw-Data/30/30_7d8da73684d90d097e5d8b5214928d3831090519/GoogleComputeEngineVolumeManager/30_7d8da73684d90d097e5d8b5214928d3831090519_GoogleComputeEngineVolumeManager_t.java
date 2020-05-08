 package brooklyn.location.blockstore.gce;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
import java.io.IOException;
 import java.util.concurrent.Callable;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.jclouds.ContextBuilder;
 import org.jclouds.encryption.bouncycastle.config.BouncyCastleCryptoModule;
 import org.jclouds.googlecomputeengine.GoogleComputeEngineApi;
 import org.jclouds.googlecomputeengine.domain.Disk;
 import org.jclouds.googlecomputeengine.domain.Operation;
 import org.jclouds.googlecomputeengine.features.DiskApi;
 import org.jclouds.googlecomputeengine.features.InstanceApi;
 import org.jclouds.googlecomputeengine.options.AttachDiskOptions;
 import org.jclouds.googlecomputeengine.options.AttachDiskOptions.DiskMode;
 import org.jclouds.googlecomputeengine.options.AttachDiskOptions.DiskType;
 import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
 import org.jclouds.sshj.config.SshjSshClientModule;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Objects;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.collect.ImmutableSet;
 import com.google.inject.Module;
 
 import brooklyn.location.blockstore.AbstractVolumeManager;
 import brooklyn.location.blockstore.BlockDeviceOptions;
 import brooklyn.location.blockstore.api.AttachedBlockDevice;
 import brooklyn.location.blockstore.api.BlockDevice;
 import brooklyn.location.blockstore.api.MountedBlockDevice;
 import brooklyn.location.jclouds.JcloudsLocation;
 import brooklyn.location.jclouds.JcloudsSshMachineLocation;
 import brooklyn.util.internal.Repeater;
 
 public class GoogleComputeEngineVolumeManager extends AbstractVolumeManager {
 
     private static final Logger LOG = LoggerFactory.getLogger(GoogleComputeEngineVolumeManager.class);
     private static final String PROVIDER = "google-compute-engine";
     private static final String DEVICE_PREFIX = "/dev/disk/by-id/google-";
 
     private final LoadingCache<JcloudsLocation, String> locationProjectName = CacheBuilder.newBuilder()
         .maximumSize(1000)
         .build(new CacheLoader<JcloudsLocation, String>() {
             @Override
            public String load(JcloudsLocation location) {
                 String identity = location.getIdentity();
                 identity = identity.substring(0, identity.indexOf('@'));
                 GoogleComputeEngineApi api = getGoogleComputeEngineApi(location);
                 String project = api.getProjectApi().get(identity).getName();
                try {
                    api.close();
                } catch (IOException e) {
                    LOG.debug("Exception closing GCE api", e);
                }
                 return project;
             }});
 
     @Override
     protected String getVolumeDeviceName(char deviceSuffix) {
         return DEVICE_PREFIX + deviceSuffix;
     }
 
     @Override
     protected String getOSDeviceName(char deviceSuffix) {
         return getVolumeDeviceName(deviceSuffix);
     }
 
     @Override
     public BlockDevice createBlockDevice(JcloudsLocation location, BlockDeviceOptions options) {
         LOG.info("Creating device: location={}; options={}", location, options);
 
         GoogleComputeEngineApi computeApi = getGoogleComputeEngineApi(location);
         String project = locationProjectName.getUnchecked(location);
         DiskApi diskApi = computeApi.getDiskApiForProject(project);
         String name = getOrMakeName(location, options);
 
         Operation operation = diskApi.createInZone(name, options.getSizeInGb(), options.getZone());
         waitForOperationToBeDone(computeApi, project, options.getZone(), operation);
 
         Disk created = diskApi.getInZone(options.getZone(), name);
         LOG.info("Created device: location={}, device={}", location, created);
         return new GCEBlockDevice(location, created);
     }
 
     @Override
     public AttachedBlockDevice attachBlockDevice(JcloudsSshMachineLocation machine, BlockDevice device, BlockDeviceOptions options) {
         checkArgument(device instanceof GCEBlockDevice, "GCE volume manager cannot handle device: %s", device);
         Disk disk = GCEBlockDevice.class.cast(device).getDisk();
         LOG.info("Attaching device: machine={}; device={}; options={}", new Object[]{machine, device, options});
 
         JcloudsLocation location = machine.getParent();
         GoogleComputeEngineApi computeApi = getGoogleComputeEngineApi(location);
         String project = locationProjectName.getUnchecked(location);
         InstanceApi instanceApi = computeApi.getInstanceApiForProject(project);
         String zone = getZoneFromDisk(disk);
 
         Operation operation = instanceApi.attachDiskInZone(zone, machine.getNode().getName(),
                 new AttachDiskOptions()
                         .source(disk.getSelfLink())
                         .type(DiskType.PERSISTENT)
                         .mode(DiskMode.READ_WRITE)
                         .deviceName(String.valueOf(options.getDeviceSuffix())));
 
         waitForOperationToBeDone(computeApi, project, zone, operation);
         return device.attachedTo(machine, getVolumeDeviceName(options.getDeviceSuffix()));
     }
 
     @Override
     public BlockDevice detachBlockDevice(AttachedBlockDevice device) {
         checkArgument(device instanceof GCEBlockDevice, "GCE volume manager cannot handle device: %s", device);
         Disk disk = GCEBlockDevice.class.cast(device).getDisk();
         LOG.info("Detaching device: {}", device);
 
         GoogleComputeEngineApi computeApi = getGoogleComputeEngineApi(device.getLocation());
         String project = locationProjectName.getUnchecked(device.getLocation());
         InstanceApi instanceApi = computeApi.getInstanceApiForProject(project);
 
         Operation operation = instanceApi.detachDiskInZone(
                 getZoneFromDisk(disk), device.getMachine().getNode().getName(), String.valueOf(device.getDeviceSuffix()));
         waitForOperationToBeDone(computeApi, project, getZoneFromDisk(disk), operation);
 
         return new GCEBlockDevice(device.getLocation(), disk);
     }
 
     @Override
     public void deleteBlockDevice(BlockDevice device) {
         checkArgument(device instanceof GCEBlockDevice, "GCE volume manager cannot handle device: %s", device);
         Disk disk = GCEBlockDevice.class.cast(device).getDisk();
         LOG.info("Deleting device: {}", device);
 
         GoogleComputeEngineApi computeApi = getGoogleComputeEngineApi(device.getLocation());
         String project = locationProjectName.getUnchecked(device.getLocation());
         DiskApi diskApi = computeApi.getDiskApiForProject(project);
 
         Operation operation = diskApi.deleteInZone(getZoneFromDisk(disk), device.getId());
         waitForOperationToBeDone(computeApi, project, getZoneFromDisk(disk), operation);
     }
 
     /**
      * Describes the given volume. Or returns null if it is not found.
      */
     public Disk describeVolume(BlockDevice device) {
         checkArgument(device instanceof GCEBlockDevice, "GCE volume manager cannot handle device: %s", device);
         Disk disk = GCEBlockDevice.class.cast(device).getDisk();
         if (LOG.isDebugEnabled())
             LOG.debug("Describing device: {}", device);
         GoogleComputeEngineApi computeApi = getGoogleComputeEngineApi(device.getLocation());
         String project = locationProjectName.getUnchecked(device.getLocation());
         DiskApi diskApi = computeApi.getDiskApiForProject(project);
         return diskApi.getInZone(getZoneFromDisk(disk), device.getId());
     }
 
     private GoogleComputeEngineApi getGoogleComputeEngineApi(JcloudsLocation location) {
         String identity = location.getIdentity();
         String credential = location.getCredential();
         Iterable<Module> modules = ImmutableSet.<Module> of(
                 new SshjSshClientModule(),
                 new SLF4JLoggingModule(),
                 new BouncyCastleCryptoModule());
 
         return ContextBuilder.newBuilder(PROVIDER)
               .credentials(identity, credential)
               .modules(modules)
               .buildApi(GoogleComputeEngineApi.class);
     }
 
     private String getZoneFromDisk(Disk disk) {
         // extracts from URL like https://www.googleapis.com/compute/v1beta15/projects/jclouds-gce/zones/europe-west1-a
         String zonePath = disk.getZone().getPath();
         return zonePath.substring(zonePath.lastIndexOf('/')+1);
     }
 
     private Operation waitForOperationToBeDone(final GoogleComputeEngineApi api, final String project,
            final String zone, final Operation operation) {
 
         checkNotNull(operation, "operation should not be null");
         final AtomicReference<Operation> latest = new AtomicReference<Operation>(operation);
         boolean done = Repeater.create("Waiting for operation to be done: " + operation.getName())
                 .every(1, TimeUnit.SECONDS)
                 .limitTimeTo(60, TimeUnit.SECONDS)
                 .until(new Callable<Boolean>() {
                     @Override
                     public Boolean call() throws Exception {
                         Operation current = api.getZoneOperationApiForProject(project).getInZone(zone, operation.getName());
                         latest.set(current);
                         switch (current.getStatus()) {
                             case DONE:
                                 return true;
                             case PENDING:
                             case RUNNING:
                             default:
                                 return false;
                         }
                     }
                 })
                 .run();
         if (done) {
             return latest.get();
         } else {
             LOG.error("Operation {} still incomplete after timeout. Trying to continue. Last poll found: {}", operation.getName(), latest.get());
             return latest.get();
         }
     }
 
     // GCE-specific classes used rather than those in Devices to keep track of the Disk object through a Volume's life
     private static class GCEBlockDevice implements BlockDevice {
 
         private static final Logger LOG = LoggerFactory.getLogger(GCEBlockDevice.class);
 
         private final JcloudsLocation location;
         private final Disk disk;
 
         private GCEBlockDevice(JcloudsLocation location, Disk disk) {
             this.location = checkNotNull(location, "location");
             this.disk = checkNotNull(disk, "disk");
         }
 
         @Override
         public String getId() {
             return disk.getName();
         }
 
         @Override
         public JcloudsLocation getLocation() {
             return location;
         }
 
         public Disk getDisk() {
             return disk;
         }
 
         @Override
         public GCEAttachedBlockDevice attachedTo(JcloudsSshMachineLocation machine, String deviceName) {
             if (!machine.getParent().equals(location)) {
                 LOG.warn("Attaching device to machine in different location to its creation: id={}, location={}, machine={}",
                         new Object[]{getId(), location, machine});
             }
             return new GCEAttachedBlockDevice(machine, disk, deviceName);
         }
 
         @Override
         public String toString() {
             return Objects.toStringHelper(this)
                     .add("id", getId())
                     .add("location", location)
                     .toString();
         }
     }
 
     private static class GCEAttachedBlockDevice extends GCEBlockDevice implements AttachedBlockDevice {
 
         private final JcloudsSshMachineLocation machine;
         private final String deviceName;
 
         private GCEAttachedBlockDevice(JcloudsSshMachineLocation machine, Disk disk, String deviceName) {
             super(machine.getParent(), disk);
             this.machine = machine;
             this.deviceName = deviceName;
         }
 
         @Override
         public String getDeviceName() {
             return deviceName;
         }
 
         @Override
         public char getDeviceSuffix() {
             return getDeviceName().charAt(getDeviceName().length()-1);
         }
 
         @Override
         public JcloudsSshMachineLocation getMachine() {
             return machine;
         }
 
         @Override
         public MountedBlockDevice mountedAt(String mountPoint) {
             return new GCEMountedBlockDevice(this, mountPoint);
         }
 
         @Override
         public String toString() {
             return Objects.toStringHelper(this)
                     .add("id", getId())
                     .add("machine", getMachine())
                     .add("deviceName", getDeviceName())
                     .toString();
         }
 
     }
 
     private static class GCEMountedBlockDevice extends GCEAttachedBlockDevice implements MountedBlockDevice {
         private final String mountPoint;
 
         private GCEMountedBlockDevice(GCEAttachedBlockDevice attachedDevice, String mountPoint) {
             super(attachedDevice.getMachine(), attachedDevice.getDisk(), attachedDevice.getDeviceName());
             this.mountPoint = mountPoint;
         }
 
         @Override
         public String getMountPoint() {
             return mountPoint;
         }
 
         @Override
         public String toString() {
             return Objects.toStringHelper(this)
                     .add("id", getId())
                     .add("machine", getMachine())
                     .add("deviceName", getDeviceName())
                     .add("mountPoint", getMountPoint())
                     .toString();
         }
 
     }
 }
