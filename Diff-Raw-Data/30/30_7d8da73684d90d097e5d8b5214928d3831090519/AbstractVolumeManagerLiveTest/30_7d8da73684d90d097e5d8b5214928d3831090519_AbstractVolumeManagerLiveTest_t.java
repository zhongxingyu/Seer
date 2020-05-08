 package brooklyn.location.blockstore;
 
 import static brooklyn.util.ssh.CommonCommands.sudo;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertNotEquals;
 
 import java.io.ByteArrayInputStream;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import brooklyn.config.BrooklynProperties;
 import brooklyn.entity.basic.Entities;
 import brooklyn.location.basic.SshMachineLocation;
 import brooklyn.location.blockstore.api.BlockDevice;
 import brooklyn.location.blockstore.api.MountedBlockDevice;
 import brooklyn.location.blockstore.api.VolumeManager;
 import brooklyn.location.jclouds.JcloudsLocation;
 import brooklyn.location.jclouds.JcloudsSshMachineLocation;
 import brooklyn.management.ManagementContext;
 import brooklyn.management.internal.LocalManagementContext;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 
 public abstract class AbstractVolumeManagerLiveTest {
 
     private static final Logger LOG = LoggerFactory.getLogger(AbstractVolumeManagerLiveTest.class);
 
     protected BrooklynProperties brooklynProperties;
     protected ManagementContext ctx;
     
     protected JcloudsLocation jcloudsLocation;
     protected VolumeManager volumeManager;
     protected BlockDevice volume;
     protected List<JcloudsSshMachineLocation> machines = Lists.newCopyOnWriteArrayList();
     
     protected abstract String getProvider();
     protected abstract JcloudsLocation createJcloudsLocation();
     protected abstract VolumeManager createVolumeManager();
     protected abstract int getVolumeSize();
     protected abstract String getDefaultAvailabilityZone();
     protected abstract void assertVolumeAvailable(BlockDevice blockDevice);
     protected abstract JcloudsSshMachineLocation createJcloudsMachine() throws Exception;
 
     /**
      * Speed tests up by rebinding and returning an existing virtual machine.
      * See {@link JcloudsLocation#rebindMachine(brooklyn.util.config.ConfigBag)}.
      */
     protected abstract Optional<JcloudsSshMachineLocation> rebindJcloudsMachine();
     
 
     @BeforeMethod(alwaysRun=true)
     public void setUp() throws Exception {
         ctx = new LocalManagementContext();
         brooklynProperties = (BrooklynProperties) ctx.getConfig();
 
         // Don't let any defaults from brooklyn.properties (except credentials) interfere with test
         brooklynProperties.remove("brooklyn.jclouds."+getProvider()+".image-description-regex");
         brooklynProperties.remove("brooklyn.jclouds."+getProvider()+".image-name-regex");
         brooklynProperties.remove("brooklyn.jclouds."+getProvider()+".image-id");
         brooklynProperties.remove("brooklyn.jclouds."+getProvider()+".inboundPorts");
         brooklynProperties.remove("brooklyn.jclouds."+getProvider()+".hardware-id");
 
         // Also removes scriptHeader (e.g. if doing `. ~/.bashrc` and `. ~/.profile`, then that can cause "stdin: is not a tty")
         brooklynProperties.remove("brooklyn.ssh.config.scriptHeader");
 
         jcloudsLocation = createJcloudsLocation();
         volumeManager = createVolumeManager();
     }
 
     @AfterMethod(alwaysRun=true)
     public void tearDown() throws Exception {
         for (JcloudsSshMachineLocation machine : machines) {
             jcloudsLocation.release(machine);
         }
         machines.clear();
         
        if (volume != null) {
            volumeManager.deleteBlockDevice(volume);
            volume = null;
        }
        if (ctx != null) {
            Entities.destroyAll(ctx);
            ctx = null;
        }
     }
 
     @Test(groups="Live")
     public void testCreateVolume() throws Exception {
         Map<String, String> tags = ImmutableMap.of(
                 "user", System.getProperty("user.name"),
                 "purpose", "brooklyn-blockstore-VolumeManagerLiveTest");
         BlockDeviceOptions options = new BlockDeviceOptions()
                 .zone(getDefaultAvailabilityZone())
                 .sizeInGb(getVolumeSize())
                 .tags(tags);
         volume = volumeManager.createBlockDevice(jcloudsLocation, options);
         assertVolumeAvailable(volume);
     }
 
     // Does the attach+mount twice to ensure that cleanup worked
     @Test(groups="Live", dependsOnMethods = {"testCreateVolume"})
     public void testCreateAndAttachVolume() throws Throwable {
 
         String mountPoint = "/var/opt2/test1";
 
         final BlockDeviceOptions blockDeviceOptions = new BlockDeviceOptions()
                 .sizeInGb(getVolumeSize())
                 .zone(getDefaultAvailabilityZone())
                 .deviceSuffix('h')
                 .tags(ImmutableMap.of(
                         "user", System.getProperty("user.name"),
                         "purpose", "brooklyn-blockstore-VolumeManagerLiveTest"));
         final FilesystemOptions filesystemOptions = new FilesystemOptions(mountPoint, "ext3");
         MountedBlockDevice mountedDevice = null;
 
         // For speed, try to use an existing VM; but if that doesn't exist then fallback to creating a temporary one
         Optional<JcloudsSshMachineLocation> existingMachine = rebindJcloudsMachine();
         JcloudsSshMachineLocation machine;
         if (!existingMachine.isPresent()) {
             // make sure newly created machines are tidied
             LOG.info("No machine to rebind. Falling back to creating temporary VM in " + jcloudsLocation);
             machine = createJcloudsMachine();
             machines.add(machine);
         } else {
             machine = existingMachine.get();
         }
         
         try {
             // Create and mount the initial volume
             mountedDevice = volumeManager.createAttachAndMountVolume(machine, blockDeviceOptions, filesystemOptions);
             volume = mountedDevice;
 
             assertExecSucceeds(machine, "show mount points", ImmutableList.of(
                     "mount -l", "mount -l | grep \""+mountPoint+"\""));
             assertExecSucceeds(machine, "list mount contents", ImmutableList.of("ls -la "+mountPoint));
 
             String tmpDestFile = "/tmp/myfile.txt";
             String destFile = mountPoint+"/myfile.txt";
             machine.copyTo(new ByteArrayInputStream("abc".getBytes()), tmpDestFile);
             assertExecSucceeds(machine, "list mount contents", ImmutableList.of(
                     sudo("cp "+tmpDestFile+" "+destFile)));
             
             // Unmount and detach the volume
             BlockDevice detachedDevice = volumeManager.unmountFilesystemAndDetachVolume(mountedDevice);
 
             assertExecFails(machine, "show mount points", ImmutableList.of(
                     "mount -l", "mount -l | grep \""+mountPoint+"\""));
             assertExecFails(machine, "check file contents", ImmutableList.of("cat "+destFile, "grep abc "+destFile));
             
             // Reattach and mount the volume
             volumeManager.attachAndMountVolume(machine, detachedDevice, blockDeviceOptions, filesystemOptions);
 
             assertExecSucceeds(machine, "show mount points", ImmutableList.of(
                     "mount -l", "mount -l | grep \""+mountPoint+"\""));
             assertExecSucceeds(machine, "list mount contents", ImmutableList.of("ls -la "+mountPoint));
             assertExecSucceeds(machine, "check file contents", ImmutableList.of("cat "+destFile, "grep abc "+destFile));
 
         } catch (Throwable t) {
             LOG.error("Error creating and attaching volume", t);
             throw t;
             
         } finally {
             if (mountedDevice != null) {
                 try {
                     volumeManager.unmountFilesystemAndDetachVolume(mountedDevice);
                 } catch (Exception e) {
                     LOG.error("Error umounting/detaching volume", e);
                 }
             }
         }
     }
 
     @Test(groups="Live", dependsOnMethods = {"testCreateAndAttachVolume"})
     public void testMoveMountedVolumeToAnotherMachine() throws Throwable {
         JcloudsSshMachineLocation machine1 = createJcloudsMachine();
         JcloudsSshMachineLocation machine2 = createJcloudsMachine();
         machines.add(machine1);
         machines.add(machine2);
         final String mountPoint = "/var/opt/mountpoint";
         final String user = System.getProperty("user.name");
 
         BlockDeviceOptions deviceOptions = new BlockDeviceOptions()
                 .sizeInGb(getVolumeSize())
                 .zone(getDefaultAvailabilityZone())
                 .deviceSuffix('h')
                 .tags(ImmutableMap.of(
                     "user", user,
                     "purpose", "brooklyn-blockstore-test-move-between-machines"));
         FilesystemOptions filesystemOptions = new FilesystemOptions(mountPoint, "ext3");
 
         LOG.info("Creating and mounting device on machine1: {}", machine1);
         MountedBlockDevice mountedOnM1 = volumeManager.createAttachAndMountVolume(machine1, deviceOptions, filesystemOptions);
 
         String tmpDestFile = "/tmp/myfile.txt";
         String destFile = mountPoint+"/myfile.txt";
         machine1.copyTo(new ByteArrayInputStream("abc".getBytes()), tmpDestFile);
         assertExecSucceeds(machine1, "list mount contents", ImmutableList.of(
                 "chown -R " + user + " " + mountPoint,
                 sudo("cp " + tmpDestFile + " " + destFile)));
 
         LOG.info("Unmounting filesystem: {}", mountedOnM1);
         BlockDevice unmounted = volumeManager.unmountFilesystemAndDetachVolume(mountedOnM1);
 
         LOG.info("Remounting {} on machine2: {}", unmounted, machine2);
         MountedBlockDevice mountedOnM2 = volumeManager.attachAndMountVolume(machine2, unmounted, deviceOptions, filesystemOptions);
         assertEquals(mountedOnM2.getMountPoint(), mountPoint);
         assertExecSucceeds(machine2, "list mount contents on new machine", ImmutableList.of(
                 "cat " + destFile));
 
         LOG.info("Unmounting {} and deleting device", mountedOnM2);
         volumeManager.unmountFilesystemAndDetachVolume(mountedOnM2);
         volumeManager.deleteBlockDevice(unmounted);
     }
 
     protected void assertExecSucceeds(SshMachineLocation machine, String description, List<String> cmds) {
         int success = machine.execCommands(description, cmds);
         assertEquals(success, 0);
     }
     
     protected void assertExecFails(SshMachineLocation machine, String description, List<String> cmds) {
         int success = machine.execCommands(description, cmds);
         assertNotEquals(success, 0);
     }
 }
