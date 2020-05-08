 package eu.stratuslab.storage.disk.plugins;
 
 import eu.stratuslab.storage.disk.utils.DiskUtils;
 import eu.stratuslab.storage.disk.utils.ProcessUtils;
 import eu.stratuslab.storage.persistence.Disk;
 
 public final class NetAppStorage implements DiskStorage {
 
 	private String NETAPP_CONFIG = "";
 	private String NETAPP_CMD = "";
 
 	public NetAppStorage(String netappConfig, String netappCmd) {
 
 		NETAPP_CONFIG = netappConfig;
 		NETAPP_CMD = netappCmd;
 	}
 
 	public void create(String uuid, long size) {
 
 		ProcessBuilder pb = new ProcessBuilder(NETAPP_CMD, "--config",
 				NETAPP_CONFIG, "--action", "create", uuid, Long.toString(size));
 
 		ProcessUtils.execute(pb, "Unable to create volume on NetApp: " + uuid
 				+ " of size " + size);
 	}
 
 	protected void checkDiskExists(String baseUuid) {
 
 		ProcessBuilder pb = new ProcessBuilder(NETAPP_CMD, "--config",
 				NETAPP_CONFIG, "--action", "check", baseUuid);
 
 		ProcessUtils
 				.execute(pb, "Volume does not exist on NetApp: " + baseUuid);
 
 	}
 
 	public String rebase(Disk disk) {
 
 		String rebaseUuid = DiskUtils.generateUUID();
 
 		ProcessBuilder pb = new ProcessBuilder(NETAPP_CMD, "--config",
 				NETAPP_CONFIG, "--action", "rebase", disk.getUuid(), rebaseUuid);
 
 		ProcessUtils.execute(pb,
 				"Cannot rebase image NetApp: " + disk.getUuid() + " "
 						+ rebaseUuid);
 
 		disk.setQuarantine("");
 		disk.setSeed(true);
		disk.store();
 		
 		return rebaseUuid;
 	}
 
 	public void createCopyOnWrite(String baseUuid, String cowUuid, long size) {
 
 		ProcessBuilder pb = new ProcessBuilder(NETAPP_CMD, "--config",
 				NETAPP_CONFIG, "--action", "snapshot", baseUuid, cowUuid,
 				Long.toString(size));
 
 		ProcessUtils.execute(pb, "Cannot create copy on write volume: "
 				+ baseUuid + " " + cowUuid + " " + size);
 
 	}
 
 	public void delete(String uuid) {
 		ProcessBuilder pb = new ProcessBuilder(NETAPP_CMD, "--config",
 				NETAPP_CONFIG, "--action", "delete", uuid, Integer.toString(0));
 
 		ProcessUtils.execute(pb, "Unable to delete volume on NetApp: " + uuid);
 	}
 
 	public String getDiskLocation(String uuid) {
 		return "";
 	}
 
 }
