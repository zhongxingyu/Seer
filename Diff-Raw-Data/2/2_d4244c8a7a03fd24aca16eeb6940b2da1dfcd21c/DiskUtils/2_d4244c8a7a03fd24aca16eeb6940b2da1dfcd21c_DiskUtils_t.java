 package eu.stratuslab.storage.disk.utils;
 
 import java.util.List;
 import eu.stratuslab.storage.disk.main.PersistentDiskApplication;
 import eu.stratuslab.storage.disk.resources.BaseResource;
 
 public final class DiskUtils {
 
 	// Template for an iSCSI target entry.
 	// Fields passed to the formatter should be the path for the disk store and
 	// the uuid.
 	private static final String TARGET_TEMPLATE = "<target iqn.2011-01.eu.stratuslab:%s>\n"
 			+ "backing-store %s/%s\n" + "</target>\n";
 
 	public static Boolean updateISCSIConfiguration() {
 		String configuration = createTargetConfiguration();
 
 		FileUtils.writeToFile(PersistentDiskApplication.ISCSI_CONFIG,
 				configuration);
 
 		updateISCSIServer();
 
 		return true;
 	}
 
 	private static String createTargetConfiguration() {
 		StringBuilder sb = new StringBuilder();
 		List<String> disks = getAllDisks();
 		String disksLocation = getDisksLocation();
 
 		for (String uuid : disks) {
 			sb.append(String.format(TARGET_TEMPLATE, uuid, disksLocation, uuid));
 		}
 
 		return sb.toString();
 	}
 
 	private static void updateISCSIServer() {
 		ProcessBuilder pb = new ProcessBuilder(
 				PersistentDiskApplication.ISCSI_ADMIN, "--update", "ALL");
 
 		ProcessUtils.execute("updateISCSIServer", pb,
 				"Perhaps there is a syntax error in " + 
 				PersistentDiskApplication.ISCSI_CONFIG.getAbsolutePath() +
 				" or in " + PersistentDiskApplication.ISCSI_CONFIG_FILENAME);
 	}
 
 	public static List<String> getAllDisks() {
 		DiskProperties zk = BaseResource.getZooKeeper();
 
 		return zk.getDisks();
 	}
 
 	private static String getDisksLocation() {
 		if (PersistentDiskApplication.DISK_TYPE == PersistentDiskApplication.DiskType.FILE) {
			return PersistentDiskApplication.FILE_DISK_LOCATION.getAbsolutePath();
 		} else {
 			return PersistentDiskApplication.LVM_GROUPE_PATH;
 		}
 	}
 
 }
