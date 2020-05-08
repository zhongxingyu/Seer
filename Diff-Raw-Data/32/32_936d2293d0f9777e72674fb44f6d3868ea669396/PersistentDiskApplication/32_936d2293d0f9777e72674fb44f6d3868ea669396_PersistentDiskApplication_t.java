 /*
  Created as part of the StratusLab project (http://stratuslab.eu),
  co-funded by the European Commission under the Grant Agreement
  INSFO-RI-261552.
 
  Copyright (c) 2011, Centre National de la Recherche Scientifique (CNRS)
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 package eu.stratuslab.storage.disk.main;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.restlet.Application;
 import org.restlet.Context;
 import org.restlet.Restlet;
 import org.restlet.data.ChallengeScheme;
 import org.restlet.data.LocalReference;
 import org.restlet.ext.freemarker.ContextTemplateLoader;
 import org.restlet.resource.Directory;
 import org.restlet.routing.Router;
 import org.restlet.security.ChallengeAuthenticator;
 import eu.stratuslab.storage.disk.resources.DiskResource;
 import eu.stratuslab.storage.disk.resources.DisksResource;
 import eu.stratuslab.storage.disk.resources.ForceTrailingSlashResource;
 import eu.stratuslab.storage.disk.resources.CreateResource;
 import eu.stratuslab.storage.disk.resources.HomeResource;
import eu.stratuslab.storage.disk.resources.LogoutResource;
 import eu.stratuslab.storage.disk.utils.DumpVerifier;
 import freemarker.template.Configuration;
 
 public class PersistentDiskApplication extends Application {
 	public enum DiskType {
 		LVM, FILE;
 	}
 
 	public static final String CFG_FILENAME = "/etc/stratuslab/pdisk.cfg";
 	public static final String LVCREATE_DIR = "/sbin";
 	public static final String LVREMOVE_DIR = "/sbin";
 
 	// ZooKeeper default configuration
 	public static final String DEFAULT_ZK_ADDRESS = "127.0.0.1";
 	public static final String DEFAULT_ZK_PORT = "2181";
 	public static final String DEFAULT_ZK_ROOT_PATH = "/disks";
 
 	// Disk creation (file and LVM)
 	public static final String DEFAULT_DISK_TYPE = "file"; // Can be
 															// "lvm"|"file"
 	public static final String DEFAULT_DISK_STORE = "/tmp/diskstore";
 	public static final String DEFAULT_LVM_GROUP_NAME = "/dev/vg.02";
 
 	public static final Properties CONFIGURATION;
 	public static final File DISK_STORE;
 	public static final String ZK_ADDRESS;
 	public static final int ZK_PORT;
 	public static final String ZK_ROOT_PATH;
 	public static final DiskType DISK_TYPE;
 	public static final String LVM_GROUPE_PATH;
 
 	private Configuration freeMarkerConfiguration = null;
 
 	static {
 		CONFIGURATION = readConfigFile();
 		DISK_TYPE = getDiskType();
 		DISK_STORE = getDiskStoreDirectory();
 		ZK_ADDRESS = getConfigValue("disk.store.zk_address", DEFAULT_ZK_ADDRESS);
 		ZK_PORT = Integer.parseInt(getConfigValue("disk.store.zk_port",
 				DEFAULT_ZK_PORT));
 		ZK_ROOT_PATH = getConfigValue("disk.store.zk_root_path",
 				DEFAULT_ZK_ROOT_PATH);
 		LVM_GROUPE_PATH = getConfigValue("disk.store.lvm.group.name",
 				DEFAULT_LVM_GROUP_NAME);
 	}
 
 	public PersistentDiskApplication() {
 
 		setName("StratusLab Persistent Disk Server");
 		setDescription("StratusLab server for persistent disk storage.");
 		setOwner("StratusLab");
 		setAuthor("Charles Loomis");
 
 		getTunnelService().setUserAgentTunnel(true);
 	}
 
 	public static Properties readConfigFile() {
 		File cfgFile = new File(CFG_FILENAME);
 		Properties properties = new Properties();
 
 		if (cfgFile.exists()) {
 			FileReader reader = null;
 			try {
 				reader = new FileReader(cfgFile);
 				properties.load(reader);
 			} catch (IOException consumed) {
 				// TODO: Log this error.
 			} finally {
 				if (reader != null) {
 					try {
 						reader.close();
 					} catch (IOException consumed) {
 						// TODO: Log this error.
 					}
 				}
 			}
 		}
 
 		return properties;
 	}
 
 	public static String getConfigValue(String key, String defaultValue) {
 		String configValue = CONFIGURATION.getProperty(key, defaultValue);
 		return configValue;
 	}
 
 	public static File getDiskStoreDirectory() {
 		String diskStoreDir = getConfigValue("disk.store.dir",
 				DEFAULT_DISK_STORE);
 
 		File diskStoreHandler = new File(diskStoreDir);
 
 		// Don't need check if we not use it
 		if (PersistentDiskApplication.DISK_TYPE == DiskType.LVM) {
 			return diskStoreHandler;
 		}
 
 		// if (PersistentDiskApplication.DISK_TYPE != DiskType.LVM &&
 		// (!diskStoreHandler.canWrite() || !diskStoreHandler.canRead())) {
 		// throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
 		// "disk store have to be readable and writable.");
 		// }
 
 		return diskStoreHandler;
 	}
 
 	public static DiskType getDiskType() {
 
 		String diskType = getConfigValue("disk.store.disk_type",
 				DEFAULT_DISK_TYPE);
 
 		if (diskType.equalsIgnoreCase("lvm"))
 			return DiskType.LVM;
 		else if (diskType.equalsIgnoreCase("file"))
 			return DiskType.FILE;
 		else
 			return DiskType.FILE;
 	}
 
 	@Override
 	public Restlet createInboundRoot() {
 		Context context = getContext();
 
 		freeMarkerConfiguration = createFreeMarkerConfig(context);
 		
 		// The guard is needed although JAAS which is doing the authentication
 		// just to be able to retrieve client information (challenger).
 		DumpVerifier verifier = new DumpVerifier();
 		ChallengeAuthenticator guard = new ChallengeAuthenticator(getContext(),
 				ChallengeScheme.HTTP_BASIC, "Stratuslab Persistent Disk Storage");
 		guard.setVerifier(verifier);
 
 		Router router = new Router(context);
 
 		router.attach("/disks/{uuid}/", DiskResource.class);
 		router.attach("/disks/{uuid}", ForceTrailingSlashResource.class);
 
 		router.attach("/disks/", DisksResource.class);
 		router.attach("/disks", ForceTrailingSlashResource.class);
 
 		router.attach("/create/", CreateResource.class);
 		router.attach("/create", ForceTrailingSlashResource.class);
		
		router.attach("/logout/", LogoutResource.class);
		router.attach("/logout", ForceTrailingSlashResource.class);
 
 		Directory cssDir = new Directory(getContext(), "war:///css");
 		cssDir.setNegotiatingContent(false);
 		cssDir.setIndexName("index.html");
 		router.attach("/css/", cssDir);
 
 		// Unknown root pages get the home page.
 		router.attachDefault(HomeResource.class);
 
 		guard.setNext(router);
 
 		return guard;
 	}
 
 	private static Configuration createFreeMarkerConfig(Context context) {
 
 		Configuration fmCfg = new Configuration();
 		fmCfg.setLocalizedLookup(false);
 
 		LocalReference fmBaseRef = LocalReference.createClapReference("/");
 		fmCfg.setTemplateLoader(new ContextTemplateLoader(context, fmBaseRef));
 
 		return fmCfg;
 	}
 
 	public Configuration getFreeMarkerConfiguration() {
 		return freeMarkerConfiguration;
 	}
 	
 }
