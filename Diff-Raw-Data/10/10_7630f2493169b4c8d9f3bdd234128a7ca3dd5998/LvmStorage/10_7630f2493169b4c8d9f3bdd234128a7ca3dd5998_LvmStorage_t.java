 package eu.stratuslab.storage.disk.plugins;
 
 import java.io.File;
 
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 
 import eu.stratuslab.storage.disk.main.RootApplication;
 import eu.stratuslab.storage.disk.utils.DiskUtils;
 import eu.stratuslab.storage.disk.utils.ProcessUtils;
 
 public final class LvmStorage implements DiskStorage {
 
 	public void create(String uuid, int size) {
 
 		String lvmSize = size + "G";
 		ProcessBuilder pb = new ProcessBuilder(
 				RootApplication.CONFIGURATION.LVCREATE_CMD, "-L", lvmSize,
 				RootApplication.CONFIGURATION.LVM_GROUP_PATH, "-n", uuid);
 
 		ProcessUtils.execute(pb, "Unable to recreate the LVM volume");
 	}
 
 	public void createCopyOnWrite(String baseUuid, String cowUuid, int size) {
 		// lvcreate --snapshot -p rw --size $PDISKID_COPY_SIZE
 		// --name $PDISKID_COPY $VGPATH/$PDISKID
 
 		checkDiskExists(baseUuid);
 
 		String lvmSize = size + "G";
 		String sourcePath = RootApplication.CONFIGURATION.LVM_GROUP_PATH + "/"
 				+ baseUuid;
 		ProcessBuilder pb = new ProcessBuilder(
 				RootApplication.CONFIGURATION.LVCREATE_CMD, "--snapshot", "-p",
 				"rw", "--size", lvmSize, "--name", cowUuid, sourcePath);
 
 		ProcessUtils.execute(pb, "Unable to recreate the LVM volume");
 	}
 
 	protected void checkDiskExists(String baseUuid) {
 		File diskFile = new File(RootApplication.CONFIGURATION.LVM_GROUP_PATH,
 				baseUuid);
 		if (!diskFile.exists()) {
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT,
 					"Cannot create copy on write disk, since base disk "
 							+ baseUuid + " doesn't exist");
 		}
 	}
 
 	public void delete(String uuid) {
 		String volumePath = RootApplication.CONFIGURATION.LVM_GROUP_PATH + "/"
 				+ uuid;
 		ProcessBuilder pb = new ProcessBuilder(
 				RootApplication.CONFIGURATION.LVREMOVE_CMD, "-f", volumePath);
 
 		ProcessUtils.execute(pb, "Failed to delete disk " + uuid
 				+ ". It's possible that the disk is still logged on a node");
 	}
 
 	public String rebase(String uuid) {
 
 		checkDiskExists(uuid);
 				
 		String rebasedUuid = DiskUtils.generateUUID();
 
 		String sourcePath = RootApplication.CONFIGURATION.LVM_GROUP_PATH + "/"
 				+ uuid;
 		ProcessBuilder pb = new ProcessBuilder("xxdd", "if=" + sourcePath,
 				"of=" + rebasedUuid);
 
 		ProcessUtils.execute(pb, "Unable to rebase the LVM volume");
 
 		delete(uuid);
 
 		return rebasedUuid;
 	}
 
 }
