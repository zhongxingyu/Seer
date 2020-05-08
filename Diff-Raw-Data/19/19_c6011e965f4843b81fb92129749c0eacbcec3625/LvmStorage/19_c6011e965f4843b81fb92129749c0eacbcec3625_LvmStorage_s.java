 package eu.stratuslab.storage.disk.plugins;
 
 import java.io.File;
 
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
 
 import eu.stratuslab.storage.disk.main.RootApplication;
 import eu.stratuslab.storage.disk.utils.DiskUtils;
 import eu.stratuslab.storage.disk.utils.MiscUtils;
 import eu.stratuslab.storage.disk.utils.ProcessUtils;
 
 public final class LvmStorage implements DiskStorage {
 
 	public void create(String uuid, int size) {
 
 		String lvmSize = size + "G";
 		ProcessBuilder pb = new ProcessBuilder(
 				RootApplication.CONFIGURATION.LVCREATE_CMD, "-L", lvmSize,
 				RootApplication.CONFIGURATION.LVM_GROUP_PATH, "-n", uuid);
 
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
 
 	public String rebase(String cowUuid, String rebaseUuid) {
 
 		checkDiskExists(cowUuid);
 
		String rebasedUuid = DiskUtils.generateUUID();

 		String sourcePath = DiskUtils.getDevicePath() + cowUuid;
		String rebasedPath = DiskUtils.getDevicePath() + rebasedUuid;
 
 		ProcessBuilder pb = new ProcessBuilder("dd", "if=" + sourcePath, "of="
 				+ rebasedPath);
 
 		ProcessUtils.execute(pb, "Unable to rebase the LVM volume");
 
 		return rebaseUuid;
 	}
 
 	public void createCopyOnWrite(String baseUuid, String cowUuid, int size) {
 		// lvcreate --snapshot -p rw --size $PDISKID_COPY_SIZE
 		// --name $PDISKID_COPY $VGPATH/$PDISKID
 
 		checkDiskExists(baseUuid);
 
 		String lvmSize = size + "G";
 		String sourcePath = DiskUtils.getDevicePath() + baseUuid;
 		ProcessBuilder pb = new ProcessBuilder(
 				RootApplication.CONFIGURATION.LVCREATE_CMD, "--snapshot", "-p",
 				"rw", "--size", lvmSize, "--name", cowUuid, sourcePath);
 
 		ProcessUtils.execute(pb, "Unable to recreate the LVM volume");
 	}
 
 	public void delete(String uuid) {
 		String volumePath = DiskUtils.getDevicePath() + uuid;
 
 		if (false == (new File(volumePath)).exists()) {
 			return;
 		}
 
 		ProcessBuilder lvremove = new ProcessBuilder(
 				RootApplication.CONFIGURATION.LVREMOVE_CMD, "-f", volumePath);
 
 		if (ProcessUtils.executeGetStatus(lvremove) != 0) {
 			ProcessBuilder lvchange = new ProcessBuilder(
 					RootApplication.CONFIGURATION.LVCHANGE_CMD, "-a n",
 					volumePath);
 			ProcessUtils.executeGetStatus(lvchange);
 
 			if (ProcessUtils.executeGetStatus(lvremove) != 0) {
 				// dmsetup needs as input <VG name>-<UUID> sanitized with
 				// 's/-/--/g;s|/|-|g'
 				String volumeGroupName = MiscUtils.sub("^.*/", "",
 						RootApplication.CONFIGURATION.LVM_GROUP_PATH)
 						.replaceAll("/", "");
 				String uuidDm = uuid.replaceAll("-", "--");
 				String volumePathDm = volumeGroupName + "-" + uuidDm;
 
 				ProcessBuilder dmsetup = new ProcessBuilder(
 						RootApplication.CONFIGURATION.DMSETUP_CMD, "remove",
 						volumePathDm);
 				ProcessUtils.executeGetStatus(dmsetup);
 
 				ProcessUtils.execute(lvremove, "It's possible that the disk "
 						+ uuid + " is still logged on a node");
 			}
 		}
 	}
 }
