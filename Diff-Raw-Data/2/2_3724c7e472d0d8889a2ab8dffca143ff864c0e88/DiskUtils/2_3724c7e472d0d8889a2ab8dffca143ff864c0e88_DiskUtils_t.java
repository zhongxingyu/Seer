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
 import java.util.Properties;
 import java.util.UUID;
 
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 
 import eu.stratuslab.marketplace.metadata.MetadataUtils;
 import eu.stratuslab.storage.disk.main.RootApplication;
 import eu.stratuslab.storage.disk.main.ServiceConfiguration;
 import eu.stratuslab.storage.disk.main.ServiceConfiguration.ShareType;
 import eu.stratuslab.storage.disk.plugins.DiskSharing;
 import eu.stratuslab.storage.disk.plugins.DiskStorage;
 import eu.stratuslab.storage.disk.plugins.FileSystemSharing;
 import eu.stratuslab.storage.disk.plugins.IscsiSharing;
 import eu.stratuslab.storage.disk.plugins.LvmStorage;
 import eu.stratuslab.storage.disk.plugins.PosixStorage;
 
 public final class DiskUtils {
 
 	private DiskUtils() {
 
 	}
 
 	private static DiskSharing getDiskSharing() {
 		switch (RootApplication.CONFIGURATION.SHARE_TYPE) {
 		case NFS:
 			return new FileSystemSharing();
 		case ISCSI:
 			return new IscsiSharing();
 		default:
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
 		}
 	}
 
 	private static DiskStorage getDiskStorage() {
 
 		if (RootApplication.CONFIGURATION.SHARE_TYPE == ShareType.NFS
 				|| RootApplication.CONFIGURATION.ISCSI_DISK_TYPE == ServiceConfiguration.DiskType.FILE) {
 
 			return new PosixStorage();
 		} else {
 			return new LvmStorage();
 		}
 	}
 
 	public static void createDisk(Properties properties) {
 		String uuid = properties.getProperty(DiskProperties.UUID_KEY)
 				.toString();
 
 		DiskSharing diskSharing = getDiskSharing();
 		DiskStorage diskStorage = getDiskStorage();
 
 		diskSharing.preDiskCreationActions();
 
 		diskStorage.create(uuid, getSize(properties));
 
 		properties.put(DiskProperties.UUID_KEY, uuid);
 		DiskProperties zk = new DiskProperties();
 		zk.saveDiskProperties(properties);
 
 		diskSharing.postDiskCreationActions();
 	}
 
 	public static String createCoWDisk(Properties properties) {
 		String uuid = properties.getProperty(DiskProperties.UUID_KEY)
 				.toString();
 
 		DiskSharing diskSharing = getDiskSharing();
 		DiskStorage diskStorage = getDiskStorage();
 
 		diskSharing.preDiskCreationActions();
 
 		String cowUuid = generateUUID();
 
 		diskStorage.createCopyOnWrite(uuid, cowUuid, getSize(properties));
 
 		// TODO: refactor
 		properties.put(DiskProperties.UUID_KEY, cowUuid);
 		String baseDiskHref = String.format("<a href='%s'>basedisk<a/>",
 				DiskProperties.getDiskPath(uuid));
 		properties.put(DiskProperties.DISK_COW_BASE_KEY, baseDiskHref);
 		DiskProperties zk = new DiskProperties();
 		zk.saveDiskProperties(properties);
 
 		diskSharing.postDiskCreationActions();
 
 		return cowUuid;
 	}
 
 	public static String rebaseDisk(Properties properties) {
 		String uuid = properties.getProperty(DiskProperties.UUID_KEY)
 				.toString();
 
 		DiskStorage diskStorage = getDiskStorage();
 
 		String rebaseUuid = DiskUtils.generateUUID();
 
 		diskStorage.create(rebaseUuid, getSize(properties));
 
 		String rebasedUuid = diskStorage.rebase(uuid, rebaseUuid);
 
 		return rebasedUuid;
 	}
 
 	protected static long getSize(Properties properties) {
 		Long bytes = (Long) properties.get(DiskProperties.DISK_SIZE_KEY);
 		return bytes;
 	}
 
 	public static void removeDisk(String uuid) {
 
 		DiskSharing diskSharing = getDiskSharing();
 		DiskStorage diskStorage = getDiskStorage();
 
 		diskSharing.preDiskRemovalActions();
 
 		diskStorage.delete(uuid);
 
 		diskSharing.postDiskRemovalActions();
 	}
 
 	public static void attachHotplugDisk(String serviceName, int servicePort,
 			String node, String vmId, String diskUuid, String target) {
 
 		String attachedDisk = RootApplication.CONFIGURATION.CLOUD_NODE_VM_DIR
 				+ "/" + vmId + "/images/pdisk-" + diskUuid;
 
 		List<String> attachCmd = new ArrayList<String>();
 		attachCmd.add("ssh");
 		attachCmd.add("-p");
 		attachCmd.add("22");
 		attachCmd.add("-o");
 		attachCmd.add("ConnectTimeout=5");
 		attachCmd.add("-o");
 		attachCmd.add("StrictHostKeyChecking=no");
 		attachCmd.add("-i");
 		attachCmd.add(RootApplication.CONFIGURATION.CLOUD_NODE_SSH_KEY);
 		attachCmd.add(RootApplication.CONFIGURATION.CLOUD_NODE_ADMIN + "@"
 				+ node);
 		attachCmd.add("/usr/sbin/attach-persistent-disk.sh");
 		attachCmd.add("pdisk:" + serviceName + ":"
 				+ String.valueOf(servicePort) + ":" + diskUuid);
 		attachCmd.add(attachedDisk);
 		attachCmd.add(target);
 
 		ProcessBuilder pb = new ProcessBuilder(attachCmd);
 		ProcessUtils.execute(pb, "Unable to attach persistent disk");
 	}
 
 	public static void detachHotplugDisk(String serviceName, int servicePort,
 			String node, String vmId, String diskUuid, String target) {
 
 		List<String> detachCmd = new ArrayList<String>();
 		detachCmd.add("ssh");
 		detachCmd.add("-p");
 		detachCmd.add("22");
 		detachCmd.add("-o");
 		detachCmd.add("ConnectTimeout=5");
 		detachCmd.add("-o");
 		detachCmd.add("StrictHostKeyChecking=no");
 		detachCmd.add("-i");
 		detachCmd.add(RootApplication.CONFIGURATION.CLOUD_NODE_SSH_KEY);
 		detachCmd.add(RootApplication.CONFIGURATION.CLOUD_NODE_ADMIN + "@"
 				+ node);
 		detachCmd.add("/usr/sbin/detach-persistent-disk.sh");
 		detachCmd.add("pdisk:" + serviceName + ":"
 				+ String.valueOf(servicePort) + ":" + diskUuid);
 		detachCmd.add(target);
 		detachCmd.add(vmId);
 
 		ProcessBuilder pb = new ProcessBuilder(detachCmd);
 		ProcessUtils.execute(pb, "Unable to detach persistent disk");
 	}
 
 	public static String generateUUID() {
 		return UUID.randomUUID().toString();
 	}
 
 	public static String calculateHash(String uuid)
 			throws FileNotFoundException {
 
 		InputStream fis = new FileInputStream(getDevicePath() + uuid);
 
 		Map<String, BigInteger> info = MetadataUtils.streamInfo(fis);
 
 		BigInteger sha1Digest = info.get("SHA-1");
 
 		String identifier = MetadataUtils.sha1ToIdentifier(sha1Digest);
 
 		return identifier;
 
 	}
 
 	public static String getDevicePath() {
 		return RootApplication.CONFIGURATION.LVM_GROUP_PATH + "/";
 	}
 
 	public static void createReadOnlyDisk(Properties diskProperties) {
 		DiskStorage diskStorage = getDiskStorage();
 		String uuid = diskProperties.getProperty(DiskProperties.UUID_KEY);
 		String diskLocation = diskStorage.getDiskLocation(uuid);
 		String cachedDisk = FileUtils.getCachedDiskLocation(uuid);
 
 		FileUtils.copyFile(cachedDisk, diskLocation);
 
 		File cachedDiskFile = new File(cachedDisk);
 		boolean success = cachedDiskFile.delete();
 		if (!success) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
 					"Failed deleting inflated file: " + cachedDisk);
 		}
 		diskProperties.put(DiskProperties.DISK_READ_ONLY_KEY, true);
 	}
 
 	public static void createCompressedDisk(String uuid) {
 		DiskStorage diskStorage = getDiskStorage();
 		String diskLocation = diskStorage.getDiskLocation(uuid);
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
