 package eu.stratuslab.storage.disk.utils;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.UUID;
 
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 
 import eu.stratuslab.marketplace.metadata.MetadataUtils;
 import eu.stratuslab.storage.disk.main.RootApplication;
 import eu.stratuslab.storage.disk.main.ServiceConfiguration;
 import eu.stratuslab.storage.disk.plugins.DiskSharing;
 import eu.stratuslab.storage.disk.plugins.DiskStorage;
 import eu.stratuslab.storage.disk.plugins.FileSystemSharing;
 import eu.stratuslab.storage.disk.plugins.IscsiSharing;
 import eu.stratuslab.storage.disk.plugins.LvmStorage;
 import eu.stratuslab.storage.disk.plugins.NetAppStorage;
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
 
 		if (RootApplication.CONFIGURATION.ISCSI_DISK_TYPE == ServiceConfiguration.DiskType.FILE) {
 
 			return new PosixStorage();
 		}
 
 		switch (RootApplication.CONFIGURATION.ISCSI_DISK_TYPE) {
 		case FILE:
 			return new PosixStorage();
 		case LVM:
 			return new LvmStorage();
 		case NETAPP:
			return new NetAppStorage();
 		default:
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
 		}
 	}
 
 	public static void createDisk(DiskProperties zk, Properties properties) {
 		String uuid = properties.getProperty(DiskProperties.UUID_KEY)
 				.toString();
 
 		DiskSharing diskSharing = getDiskSharing();
 		DiskStorage diskStorage = getDiskStorage();
 
 		diskSharing.preDiskCreationActions(uuid);
 
 		diskStorage.create(uuid, getSize(properties));
 
 		properties.put(DiskProperties.UUID_KEY, uuid);
 
 		zk.saveDiskProperties(properties);
 
 		diskSharing.postDiskCreationActions(uuid);
 	}
 
 	public static String createCoWDisk(DiskProperties zk, Properties properties) {
 		String uuid = properties.getProperty(DiskProperties.UUID_KEY)
 				.toString();
 
 		DiskSharing diskSharing = getDiskSharing();
 		DiskStorage diskStorage = getDiskStorage();
 
 		String cowUuid = generateUUID();
 
 		diskSharing.preDiskCreationActions(cowUuid);
 
 		diskStorage.createCopyOnWrite(uuid, cowUuid, getSize(properties));
 
 		// TODO: refactor
 		properties.put(DiskProperties.UUID_KEY, cowUuid);
 		String baseDiskHref = String.format("<a href='%s'>basedisk<a/>",
 				DiskProperties.getDiskPath(uuid));
 		properties.put(DiskProperties.DISK_COW_BASE_KEY, baseDiskHref);
 
 		zk.saveDiskProperties(properties);
 
 		diskSharing.postDiskCreationActions(cowUuid);
 
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
 
 	protected static int getSize(Properties properties) {
 		return Integer.parseInt(properties.getProperty("size"));
 	}
 
 	public static void removeDisk(String uuid) {
 		DiskSharing diskSharing = getDiskSharing();
 
 		diskSharing.preDiskRemovalActions(uuid);
 
 		getDiskStorage().delete(uuid);
 
 		diskSharing.postDiskRemovalActions(uuid);
 	}
 
 	public static void removeDiskSharing(String uuid) {
 		DiskSharing diskSharing = getDiskSharing();
 		diskSharing.preDiskRemovalActions(uuid);
 		diskSharing.postDiskRemovalActions(uuid);
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
 
 	public static List<String> getAllDisks() {
 		DiskProperties zk = null;
 		List<String> disks = Collections.emptyList();
 		try {
 			zk = new DiskProperties();
 			disks = zk.getDisks();
 		} finally {
 			if (zk != null) {
 				zk.close();
 			}
 		}
 		return disks;
 	}
 
 }
