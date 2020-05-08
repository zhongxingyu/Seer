package eu.stratuslab.storage.disk.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 
 import eu.stratuslab.marketplace.metadata.MetadataUtils;
 import eu.stratuslab.storage.disk.backend.BackEndStorage;
 import eu.stratuslab.storage.disk.main.RootApplication;
 import eu.stratuslab.storage.disk.main.ServiceConfiguration;
 import eu.stratuslab.storage.persistence.Disk;
 import eu.stratuslab.storage.persistence.Disk.DiskType;
 
 /**
  * For unit tests see {@link DiskUtilsTest}
  * 
  */
 public final class DiskUtils {
 
 	private DiskUtils() {
 
 	}
 
 	private static BackEndStorage getDiskStorage() {
 
 		return new BackEndStorage();
 
 	}
 
 	public static void createDisk(Disk disk) {
 
 		BackEndStorage diskStorage = getDiskStorage();
 
 		diskStorage.create(disk.getUuid(), disk.getSize());
 		diskStorage.map(disk.getUuid());
 
 		disk.store();
 
 	}
 
 	public static Disk createMachineImageCoWDisk(Disk disk) {
 
 		BackEndStorage diskStorage = getDiskStorage();	
 
 		Disk cowDisk = createCowDisk(disk);
 
 		diskStorage.createCopyOnWrite(disk.getUuid(), cowDisk.getUuid(),
 				disk.getSize());
 
 		cowDisk.setType(DiskType.MACHINE_IMAGE_LIVE);
 		diskStorage.map(disk.getUuid());
 
 		cowDisk.store();
 
 		return cowDisk;
 	}
 
 	protected static Disk createCowDisk(Disk disk) {
 		Disk cowDisk = new Disk();
 		cowDisk.setType(DiskType.DATA_IMAGE_LIVE);
 		cowDisk.setBaseDiskUuid(disk.getUuid());
 		cowDisk.setSize(disk.getSize());
 		cowDisk.setUsersCount(1);
 		cowDisk.setIdentifier(disk.getIdentifier());
 		disk.incrementUserCount();
 		return cowDisk;
 	}
 
 	public static String rebaseDisk(Disk disk) {
 
 		BackEndStorage diskStorage = getDiskStorage();
 
 		return diskStorage.rebase(disk);
 	}
 
 	public static void removeDisk(String uuid) {
 		getDiskStorage().unmap(uuid);
 		getDiskStorage().delete(uuid);
 	}
 
 	public static void attachHotplugDisk(String serviceName, int servicePort,
 			String node, String vmId, String diskUuid, String target) {
 
 		String attachedDisk = getDiskLocation(vmId, diskUuid);
 
 		List<String> cmd = createHotPlugCommand(node);
 		cmd.add("--op up");
 
 		cmd.add("--attach");
 		cmd.add("--link");
 		cmd.add("--mount");
 		cmd.add("--register");
 
 		cmd.add("--pdisk-id");
 		cmd.add("pdisk:" + serviceName + ":"
 				+ String.valueOf(servicePort) + ":" + diskUuid);
 
 		cmd.add("--target");
 		cmd.add(target);
 
                 cmd.add("--vm-id");
                 cmd.add(vmId);
 
                 cmd.add("--vm-disk-name");
                 cmd.add("pdisk-" + diskUuid);
 
 		ProcessBuilder pb = new ProcessBuilder(cmd);
 		ProcessUtils.execute(pb, "Unable to attach persistent disk");
 	}
 
 	public static String attachHotplugDisk(String diskUuid) {
 		int port = ServiceConfiguration.getInstance().PDISK_SERVER_PORT;
 		String host = "localhost";
 		String tmpVmId = DiskUtils.generateUUID();
 
 		// FIXME: host is most probably wrong for the last parameter
 		attachHotplugDisk(host, port, host, tmpVmId, diskUuid, host);
 		
 		return tmpVmId;
 	}
 
 	protected static String getDiskLocation(String vmId, String diskUuid) {
 		String attachedDisk = RootApplication.CONFIGURATION.CLOUD_NODE_VM_DIR
 				+ "/" + vmId + "/images/pdisk-" + diskUuid;
 		return attachedDisk;
 	}
 
 	public static void detachHotplugDisk(String serviceName, int servicePort,
 			String node, String vmId, String diskUuid, String target) {
 
 		List<String> cmd = createHotPlugCommand(node);
 		cmd.add("--op down");
 
 		cmd.add("--attach");
 		cmd.add("--link");
 		cmd.add("--mount");
 		cmd.add("--register");
 
 		cmd.add("--pdisk-id");
 		cmd.add("pdisk:" + serviceName + ":"
 				+ String.valueOf(servicePort) + ":" + diskUuid);
 
 		cmd.add("--target");
 		cmd.add(target);
 
                 cmd.add("--vm-id");
                 cmd.add(vmId);
 
                 cmd.add("--vm-disk-name");
                 cmd.add("pdisk-" + diskUuid);
 
 		ProcessBuilder pb = new ProcessBuilder(cmd);
 		ProcessUtils.execute(pb, "Unable to detach persistent disk");
 	}
 
 	protected static List<String> createHotPlugCommand(String node) {
 		List<String> cmd = new ArrayList<String>();
 		cmd.add("echo");
 		cmd.add("ssh");
 		cmd.add("-p");
 		cmd.add("22");
 		cmd.add("-o");
 		cmd.add("ConnectTimeout=5");
 		cmd.add("-o");
 		cmd.add("StrictHostKeyChecking=no");
 		cmd.add("-i");
 		cmd.add(RootApplication.CONFIGURATION.CLOUD_NODE_SSH_KEY);
 		cmd.add(RootApplication.CONFIGURATION.CLOUD_NODE_ADMIN + "@"
 				+ node);
 		cmd.add("/usr/sbin/stratus-pdisk-client.py");
 		return cmd;
 	}
 
 	public static String generateUUID() {
 		return UUID.randomUUID().toString();
 	}
 
 	public static String calculateHash(String uuid)
 			throws FileNotFoundException {
 
 		InputStream fis = null;// = new FileInputStream(getDevicePath() + uuid);
 
 		return calculateHash(fis);
 
 	}
 
 	public static String calculateHash(File file) throws FileNotFoundException {
 
 		InputStream fis = new FileInputStream(file);
 
 		return calculateHash(fis);
 
 	}
 
 	public static String calculateHash(InputStream fis)
 			throws FileNotFoundException {
 
 		Map<String, BigInteger> info = MetadataUtils.streamInfo(fis);
 
 		BigInteger sha1Digest = info.get("SHA-1");
 
 		String identifier = MetadataUtils.sha1ToIdentifier(sha1Digest);
 
 		return identifier;
 
 	}
 
 	public static String getDevicePath() {
 		return "";//RootApplication.CONFIGURATION.LVM_GROUP_PATH + "/";
 	}
 
 	public static void createAndPopulateDiskLocal(Disk disk) {
 
 		BackEndStorage diskStorage = getDiskStorage();
 
 		String uuid = disk.getUuid();
 		String tmpVmId = DiskUtils.generateUUID();
 
 		String diskLocation = diskStorage.getDiskLocation(tmpVmId, uuid);
 		diskStorage.map(uuid);
 		String cachedDisk = FileUtils.getCachedDiskLocation(uuid);
 
 		int port = ServiceConfiguration.getInstance().PDISK_SERVER_PORT;
 		String host = "localhost";
 
 		DiskUtils.attachHotplugDisk(host, port, host,
 				tmpVmId, disk.getUuid(), host);
 		
 		FileUtils.copyFile(cachedDisk, diskLocation);
 
 		File cachedDiskFile = new File(cachedDisk);
 
 		long size = convertBytesToGigaBytes(cachedDiskFile.length());
 
 		disk.setSize(size);
 
 		boolean success = cachedDiskFile.delete();
 		if (!success) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
 					"Failed deleting inflated file: " + cachedDisk);
 		}
 		disk.setType(DiskType.DATA_IMAGE_RAW_READONLY);
 		disk.setSeed(true);
 		
 		diskStorage.unmap(uuid);
 
 	}
 
 	// FIXME: need to implement this for real!
 	public static long convertBytesToGigaBytes(long sizeInBytes) {
 		long bytesInAGB = 1073741824;
 		long inGB = sizeInBytes / bytesInAGB;
 		return (inGB == 0 ? 1 : inGB);
 	}
 
 	public static void createCompressedDisk(String uuid) {
 		
 		String diskLocation = attachHotplugDisk(uuid);
 		String cachedDisk = FileUtils.getCachedDiskLocation(uuid);
 
 		FileUtils.copyFile(diskLocation, cachedDisk);
 
 		ProcessBuilder pb = new ProcessBuilder(
 				RootApplication.CONFIGURATION.GZIP_CMD, "-f", cachedDisk);
 		ProcessUtils.execute(pb, "Unable to compress disk " + uuid);
 	}
 
 	public static String getCompressedDiskLocation(String uuid) {
 		return RootApplication.CONFIGURATION.CACHE_LOCATION + "/" + uuid
 				+ ".gz";
 	}
 
 	public static Boolean isCompressedDiskBuilding(String uuid) {
 		return FileUtils.isCachedDiskExists(uuid);
 	}
 
 	public static Boolean hasCompressedDiskExpire(String uuid) {
 		File zip = new File(FileUtils.getCompressedDiskLocation(uuid));
 		return hasCompressedDiskExpire(zip);
 	}
 
 	public static Boolean hasCompressedDiskExpire(File disk) {
 		Calendar cal = Calendar.getInstance();
 		return (cal.getTimeInMillis() > (disk.lastModified() + ServiceConfiguration.CACHE_EXPIRATION_DURATION));
 	}
 
 }
