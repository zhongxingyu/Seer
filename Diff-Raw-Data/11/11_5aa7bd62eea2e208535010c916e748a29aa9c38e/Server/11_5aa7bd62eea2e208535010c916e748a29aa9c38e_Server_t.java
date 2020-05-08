 package com.meaglin.sms.model;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.ProcessBuilder.Redirect;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.nio.file.Files;
 import java.nio.file.StandardOpenOption;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.meaglin.sms.SmsServer;
 
 public class Server {
 
 	private SmsServer controller;
 
 	private static final String SELECT_FILES = "SELECT * FROM files WHERE serverid = ?";
 
 	private int id, filecount, categorycount;
 	private long lastupdate, lastchange;
 
 	private boolean enabled, disconnected;
 
 	private String name, displayname, code, type, description, config, status, ip;
 	private Map<String, String> configMap;
 
 	private Map<Integer, ServerCategory> categories;
 
 	public List<ServerFile> toUpdate;
 	private List<ServerFile> currentFiles;
 
 	public Server(SmsServer smsServer) {
 		controller = smsServer;
 		categories = new HashMap<>();
 		configMap = new HashMap<>();
 		toUpdate = new ArrayList<>();
 	}
 
 	public void log(String message) {
 		// TODO: make proper logger.
 		System.out.println("[" + getName() + "]" + message);
 	}
 
 	public boolean isAvailable() {
 		boolean reachable = false;
 		for (int i = 0; i < 10; i += 1) {
 			if (reachable) {
 				break;
 			}
 			Socket sock = new Socket();
 			int port = 139;
 			if (getType().equalsIgnoreCase("nfs")) {
 				port = 111;
 			}
 			try {
 				sock.connect(new InetSocketAddress(getIp(), port), 1000);
 			} catch (IOException ex) {
 			}
 
 			reachable = sock.isConnected();
 
 			try {
 				sock.close();
 			} catch (IOException e) {
 			}
 		}
 		return reachable;
 	}
 
 	public List<ServerFile> getFiles() {
 		List<ServerFile> files = new ArrayList<>();
 
 		Connection conn = null;
 		PreparedStatement st = null;
 		ResultSet res = null;
 		try {
 			conn = getController().getDb().getConnection();
 			st = conn.prepareStatement(SELECT_FILES);
 			st.setInt(1, getId());
 
 			res = st.executeQuery();
 			while (res.next()) {
 				files.add(((new ServerFile()).bind(res)));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			getController().getDb().collect(conn, st, res);
 		}
 
 		for (ServerFile file : files) {
 			// TODO: unmount the belonging share somehow.
 			ServerCategory cat = categories.get(file.getServercategoryid());
 			if (cat == null) {
 				file.setFlag(ServerFile.DELETED);
 				toUpdate.add(file);
 				// continue;
 			}
 			file.setServercategory(cat);
 			file.setServer(this);
 		}
 
 		return files;
 	}
 
 	public File getPath() {
 		if (isWindows()) {
 			return new File("\\\\" + getIp());
 		} else {
 			return new File(getController().getConfig()
 					.getProperty("mount.dir") + "/" + getName());
 		}
 	}
 
 	public File getLnPath() {
 		if (isWindows()) {
 			return new File("\\\\" + getIp());
 		} else {
 			return new File(getController().getConfig().getProperty(
 					"mount.lndir")
 					+ "/" + getName());
 		}
 
 	}
 
 	public List<String> getMountFolders() {
 		List<String> dirs = new ArrayList<>();
 
 		sc: for (ServerCategory cat : this.categories.values()) {
 			String path = cat.getPath();
 			Iterator<String> it = dirs.iterator();
 			String dir;
 			while(it.hasNext()) {
 				dir = it.next();
				if(path.startsWith(dir + "/")) { // We already know this mount.
 					continue sc;
 				}
				if(dir.startsWith(path + "/")) {
 					it.remove();
 				}
 			}
 			dirs.add(path);
 		}
 
 		return dirs;
 	}
 
 	public void addServerCategory(ServerCategory toAdd) {
 		categories.put(toAdd.getId(), toAdd);
 		this.setCategorycount(categories.size());
 	}
 
 	/**
 	 * @return the id
 	 */
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * @return the filecount
 	 */
 	public int getFilecount() {
 		return filecount;
 	}
 
 	public Collection<ServerCategory> getCategories() {
 		return categories.values();
 	}
 
 	/**
 	 * @return the categorycount
 	 */
 	public int getCategorycount() {
 		return categorycount;
 	}
 
 	/**
 	 * @return the lastupdate
 	 */
 	public long getLastupdate() {
 		return lastupdate;
 	}
 
 	/**
 	 * @return the lastchange
 	 */
 	public long getLastchange() {
 		return lastchange;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return the displayname
 	 */
 	public String getDisplayname() {
 		return displayname;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @return the status
 	 */
 	public String getStatus() {
 		return status;
 	}
 
 	/**
 	 * @return the ip
 	 */
 	public String getIp() {
 		return ip;
 	}
 
 	/**
 	 * @return the controller
 	 */
 	public SmsServer getController() {
 		return controller;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	/**
 	 * @param id
 	 *			the id to set
 	 */
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	/**
 	 * @param filecount
 	 *			the filecount to set
 	 */
 	public void setFilecount(int filecount) {
 		this.filecount = filecount;
 	}
 
 	/**
 	 * @param categorycount
 	 *			the categorycount to set
 	 */
 	public void setCategorycount(int categorycount) {
 		this.categorycount = categorycount;
 	}
 
 	/**
 	 * @param lastupdate
 	 *			the lastupdate to set
 	 */
 	public void setLastupdate(long lastupdate) {
 		this.lastupdate = lastupdate;
 	}
 
 	/**
 	 * @param lastchange
 	 *			the lastchange to set
 	 */
 	public void setLastchange(long lastchange) {
 		this.lastchange = lastchange;
 	}
 
 	/**
 	 * @param name
 	 *			the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @param displayname
 	 *			the displayname to set
 	 */
 	public void setDisplayname(String displayname) {
 		this.displayname = displayname;
 	}
 
 	/**
 	 * @param description
 	 *			the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * @param status
 	 *			the status to set
 	 */
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
 	/**
 	 * @param ip
 	 *			the ip to set
 	 */
 	public void setIp(String ip) {
 		this.ip = ip;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	/**
 	 * @return the disconnected
 	 */
 	public boolean isDisconnected() {
 		return disconnected;
 	}
 
 	/**
 	 * @param disconnected
 	 *			the disconnected to set
 	 */
 	public void setDisconnected(boolean disconnected) {
 		this.disconnected = disconnected;
 	}
 
 	/**
 	 * @return the enabled
 	 */
     public boolean isEnabled() {
 	    return enabled;
     }
 
 	/**
 	 * @param enabled the enabled to set
 	 */
     public void setEnabled(boolean enabled) {
 	    this.enabled = enabled;
     }
 
 	/**
 	 * @return the code
 	 */
     public String getCode() {
 	    return code;
     }
 
 	/**
 	 * @param code the code to set
 	 */
     public void setCode(String code) {
 	    this.code = code;
     }
 
 	public Server bind(ResultSet res) throws SQLException {
 		id = res.getInt("id");
 		filecount = res.getInt("filecount");
 		categorycount = res.getInt("categorycount");
 		lastupdate = res.getLong("lastupdate");
 		lastchange = res.getLong("lastchange");
 		name = res.getString("name");
 		displayname = res.getString("displayname");
 		description = res.getString("description");
 		status = res.getString("status");
 		setEnabled(res.getBoolean("enabled"));
 		setDisconnected(res.getBoolean("disconnected"));
 		config = res.getString("config");
 		ip = res.getString("ip");
 		type = res.getString("type");
 		setCode(res.getString("code"));
 		parseConfig();
 		return this;
 	}
 
 	private void parseConfig() {
 		String[] vars = config.split("<;>");
 		for (String var : vars) {
 			String[] args = var.split("<,>");
 			if (args.length != 2) {
 				continue;
 			}
 			configMap.put(args[0], args[1]);
 		}
 	}
 
 	public void save() {
 		if (id == 0) { // We never create 'servers' inside sms.
 			return;
 		}
 		getController().getDb().save(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Server other = (Server) obj;
 		if (id != other.id)
 			return false;
 		return true;
 	}
 
 	private static String OS = null;
 
 	public static String getOsName() {
 		if (OS == null) {
 			OS = System.getProperty("os.name");
 		}
 		return OS;
 	}
 
 	public static boolean isWindows() {
 		return getOsName().startsWith("Windows");
 	}
 
 	public void refreshFiles() {
 		boolean reachable = isAvailable();
 		setLastupdate(System.currentTimeMillis());
 		setStatus(reachable ? "Online" : "Offline");
 		save();
 
		toUpdate.clear();
 		log("Status: " + this.getStatus());

 		if (!reachable) {
 			if (!isDisconnected()) {
 				disconnect();
 			}
 			return;
 		}
 		setDisconnected(false);
 		log("Loading...");
 
 		mountFolders();
 		
 
 		List<ServerFile> files = getFiles();
 		log("Loaded " + files.size() + " from db.");
 
 		List<ServerFile> newfiles = new ArrayList<ServerFile>();
 
 		// TODO: clean this up and optimize.
 		for (ServerCategory servercategory : this.categories.values()) {
 			newfiles.addAll(servercategory.getServerFiles());
 		}
 		log("Loaded " + newfiles.size() + " from server.");
 
 		for (ServerFile newFile : newfiles) {
 			ServerFile found = null;
 			for (ServerFile file : files) {
 				if (file.getName().equals(newFile.getName())
 						&& file.getDirectory().equals(newFile.getDirectory())) {
 					found = file;
 					break;
 				}
 			}
 			if (found != null) {
 				if (found.getFlag() == ServerFile.DELETED) {
 					found.setFlag(ServerFile.CREATED);
 					toUpdate.add(found);
 				} else if (found.getFlag() == ServerFile.UP_TO_DATE) {
 					// File is already known and registered.
 					getController().occupyFile(found.getPath());
 				}
 				files.remove(found);
 			} else {
 				toUpdate.add(newFile);
 			}
 		}
 
 		for (ServerFile file : files) {
 			file.setFlag(ServerFile.DELETED);
 			toUpdate.add(file);
 		}
 
 		log("Recorded " + toUpdate.size() + " changes on server.");
 	}
 
 	public void saveChanges() {
 		this.getController().getDb().save(toUpdate);
 		if (toUpdate.size() != 0) {
 			this.setLastchange(System.currentTimeMillis());
 		}
 		this.save();
 	}
 
 	public void updateServerStats() {
 		if (currentFiles == null) {
 			currentFiles = getFiles();
 		}
 		int cnt = 0;
 		for (ServerFile file : currentFiles) {
 			if (file.getFlag() != ServerFile.DELETED) {
 				cnt += 1;
 			}
 		}
 		this.setFilecount(cnt);
 		this.save();
 	}
 
 	public void mountFolders() {
 		if (isWindows()) {
 			mountOnWindows();
 		} else {
 			mountOnLinux();
 		}
 	}
 
 	public void mountOnLinux() {
 		if (!getType().equalsIgnoreCase("smb")
 				&& !getType().equalsIgnoreCase("nfs")) {
 			// TODO: find better way to throw errors without needing to add
 			// throw declarations everywhere.
 			throw new RuntimeException("Nope, invalid mount type " + getType()
 					+ " on server [" + getName() + "]" + getIp());
 		}
 
 		if (getType().equalsIgnoreCase("smb")) {
 			for (String dir : this.getMountFolders()) {
 				if(isMounted("//" + getIp() + "/" + dir, getPath() + "/" + dir)) {
 					continue;
 				}
 				(new File(getPath() + "/" + dir)).mkdirs();
 				ProcessBuilder task = new ProcessBuilder(new String[] {
 						"/bin/mount", "-t", "cifs", "//" + getIp() + "/" + dir,
 						getPath() + "/" + dir, "-o",
 						configMap.get("mount.options") });
 				task.redirectErrorStream(true); // Channel errors to stdOut.
 				task.redirectInput(Redirect.INHERIT);
 				task.redirectOutput(Redirect.INHERIT);
 				try {
 					Process proc = task.start();
 					proc.waitFor();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 			}
 			
 			// TODO: test this.
 		} else if (getType().equalsIgnoreCase("nfs")) {
 			for (String dir : this.getMountFolders()) {
 				if(isMounted(getIp() + ":/" + dir, getPath() + "/" + dir)) {
 					continue;
 				}
 				(new File(getPath() + "/" + dir)).mkdirs();
 				ProcessBuilder task = new ProcessBuilder(new String[] {
 						"/bin/mount", "-t", "nfs", getIp() + ":/" + dir,
 						getPath() + "/" + dir, "-o",
 						configMap.get("mount.options") });
 				task.redirectErrorStream(true); // Channel errors to stdOut.
 				task.redirectInput(Redirect.INHERIT);
 				task.redirectOutput(Redirect.INHERIT);
 				try {
 					Process proc = task.start();
 					proc.waitFor();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 			}
 		}
 	}
 
 	private boolean isMounted(String mount, String mountpoint) {
 		List<String[]> mounts = getController().getMounts();
 		for(String[] mnt : mounts) {
 			if(mnt[1].equals(mountpoint)) {
 				if(mnt[0].equals(mount)) {
 					return true;
 				} else {
 					throw new RuntimeException("[" + getName() + "] Trying to mount " + mount + " on already ocupied mountpoint " + mountpoint);
 				}
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * This doesn't actually mount anything.
 	 * 
 	 * Testing only ;)
 	 */
 	public void mountOnWindows() {
 		if (currentFiles == null) {
 			currentFiles = getFiles();
 		}
 
 		for (String dir : this.getMountFolders()) {
 			File directory = new File(getController().getConfig().getProperty(
 					"mount.testdir")
 					+ "/mount/" + getName() + "/" + dir);
 			directory.mkdirs();
 		}
 	}
 
 	public void updateFileLinks() {
 		if (isWindows()) {
 			updateFileLinksOnWindows();
 		} else {
 			updateFileLinksOnLinux();
 		}
 	}
 
 	public void updateFileLinksOnWindows() {
 		if (currentFiles == null) {
 			currentFiles = getFiles();
 		}
 
 		toUpdate.clear();
 
 		List<ServerFile> toDelete = new ArrayList<>();
 		for (ServerFile file : currentFiles) {
 			if (file.getFlag() == ServerFile.DELETED) {
 				file.tryDelete(getController().getConfig().getProperty(
 						"mount.testdir")
 						+ "/categories/");
 				toDelete.add(file);
 			}
 		}
 		this.getController().getDb().delete(toDelete);
 
 		for (ServerFile serverfile : currentFiles) {
 			if (serverfile.getFlag() != ServerFile.CREATED
 					&& serverfile.getFlag() != ServerFile.UPDATED) {
 				continue;
 			}
 			File file = new File(getController().getConfig().getProperty(
 					"mount.testdir")
 					+ "/categories/" + serverfile.getPath());
 			file.getParentFile().mkdirs();
 			try {
 				Files.write(file.toPath(), serverfile.getServerpath()
 						.getBytes(), StandardOpenOption.CREATE,
 						StandardOpenOption.WRITE,
 						StandardOpenOption.TRUNCATE_EXISTING);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			serverfile.setFlag(ServerFile.UP_TO_DATE);
 			toUpdate.add(serverfile);
 		}
 		this.saveChanges();
 	}
 
 	public void updateFileLinksOnLinux() {
 		if(isDisconnected()) {
 			return;
 		}
 		
 		if (currentFiles == null) {
 			currentFiles = getFiles();
 		}
 
 		toUpdate.clear();
 
 		String rootDir = getController().getConfig().getProperty(
 				"mount.categorydir");
 
 		List<ServerFile> toDelete = new ArrayList<>();
 		for (ServerFile file : currentFiles) {
 			if (file.getFlag() == ServerFile.DELETED) {
 				file.tryDelete(rootDir);
 				toDelete.add(file);
 				getController().forgetFile(file.getPath());
 			}
 		}
 		this.getController().getDb().delete(toDelete);
 
 		for (ServerFile serverfile : currentFiles) {
 			if (serverfile.getFlag() != ServerFile.CREATED
 					&& serverfile.getFlag() != ServerFile.UPDATED) {
 				continue;
 			}
 			// Account for files with the same dir and name on different servers.
 			if(!getController().occupyFile(serverfile.getPath())) {
 				if(serverfile.isDuplicate()) {
 					serverfile.setFlag(ServerFile.DUPLICATE);
 					toUpdate.add(serverfile);
 					log("Duplicate^2 file known on path " + serverfile.getPath());
 					continue;
 				}
 				serverfile.setDuplicate(true);
 				serverfile.generatePath(); // Update the path.
 				if(!getController().occupyFile(serverfile.getPath())) {
 					serverfile.setFlag(ServerFile.DUPLICATE);
 					toUpdate.add(serverfile);
 					log("Duplicate^2 file on path " + serverfile.getPath());
 					continue;
 				}
 			}
 			
 			File file = new File(rootDir + "/" + serverfile.getPath());
 			file.getParentFile().mkdirs();
 			try {
 				Files.createSymbolicLink(file.toPath(), serverfile
 						.getServerFile().toPath());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			serverfile.setFlag(ServerFile.UP_TO_DATE);
 			toUpdate.add(serverfile);
 		}
 		this.saveChanges();
 	}
 
 	public void disconnect() {
 		this.setDisconnected(true);
 		log("disconnecting...");
 		deleteFileLinks();
 		log("deleted file links.");
 		unmountFolders();
 		log("unmounted shares");
 	}
 
 	public void deleteFileLinks() {
 		if (isWindows()) {
 			deleteFileLinksOnWindows();
 		} else {
 			deleteFileLinksOnLinux();
 		}
 	}
 
 	private void deleteFileLinksOnWindows() {
 		if (currentFiles == null) {
 			currentFiles = getFiles();
 		}
 
 		toUpdate.clear();
 
 		for (ServerFile serverfile : currentFiles) {
 			if (serverfile.getFlag() == ServerFile.CREATED) { // Doesn't exist
 															  // yet ;).
 				continue;
 			}
 			serverfile.tryDelete(getController().getConfig().getProperty(
 					"mount.testdir")
 					+ "/categories/");
 			serverfile.setFlag(ServerFile.CREATED); // Recreates the files when
 													// share is up again.
 			toUpdate.add(serverfile);
 		}
 		this.saveChanges();
 	}
 
 	private void deleteFileLinksOnLinux() {
 		if (currentFiles == null) {
 			currentFiles = getFiles();
 		}
 
 		toUpdate.clear();
 
 		String rootDir = getController().getConfig().getProperty(
 				"mount.categorydir");
 
 		for (ServerFile serverfile : currentFiles) {
 			if (serverfile.getFlag() == ServerFile.CREATED) { // Doesn't exist
 															  // yet ;).
 				continue;
 			}
 			serverfile.tryDelete(rootDir);
 			serverfile.setFlag(ServerFile.CREATED); // Recreates the files when
 													// share is up again.
 			toUpdate.add(serverfile);
 		}
 
 		this.saveChanges();
 	}
 
 	public void unmountFolders() {
 		if (isWindows()) {
 			unmountFoldersOnWindows();
 		} else {
 			unmountFoldersOnLinux();
 		}
 	}
 
 	private void unmountFoldersOnWindows() {
 		for (String dir : this.getMountFolders()) {
 			File directory = new File(getController().getConfig().getProperty(
 					"mount.testdir")
 					+ "/mount/" + getName() + "/" + dir);
 			directory.delete();
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void unmountFoldersOnLinux() {
 		if (!getType().equalsIgnoreCase("smb")
 				&& !getType().equalsIgnoreCase("nfs")) {
 			// TODO: find better way to throw errors without needing to add
 			// throw declarations everywhere.
 			throw new RuntimeException("Nope, invalid mount type " + getType()
 					+ " on server [" + getName() + "]" + getIp());
 		}
 
 		for (String dir : this.getMountFolders()) {
 			
 			int status = unmount(getPath() + "/" + dir);
 			if(status == 0) {
 				log("removed mount " + getPath() + "/" + dir);
 				continue;
 			}
 			
 			killMountLocks(getPath() + "/" + dir);
 			unmount(getPath() + "/" + dir);
 			
 			log("removed mount with lock-remove " + getPath() + "/" + dir);
 		}
 	}
 	
 	// Slow... TODO: make faster
 	private void killMountLocks(String mountpoint) {
 		for(String pid : getLockingPids(mountpoint)) {
 			killPid(pid);
 		}
 	}
 	
 	private int unmount(String mountpoint) {
 		ProcessBuilder task = new ProcessBuilder(new String[] { "/bin/umount", mountpoint });
 		
 		task.redirectErrorStream(true); // Channel errors to stdOut.
 		task.redirectInput(Redirect.INHERIT);
 		task.redirectOutput(Redirect.INHERIT);
 		try {
 			Process proc = task.start();
 			return proc.waitFor();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 	
 	
 	// TODO: check if we can use simple kills instead of -9
 	private void killPid(String pid) {
 		ProcessBuilder task = new ProcessBuilder(new String[] {
 				"kill"
 //				, "-9"
 				, pid });
 		task.redirectErrorStream(true); // Channel errors to stdOut.
 		task.redirectInput(Redirect.INHERIT);
 		task.redirectOutput(Redirect.INHERIT);
 		try {
 			Process proc = task.start();
 			proc.waitFor();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private Set<String> getLockingPids(String mountpoint) {
 		List<String> lines = new ArrayList<>();
 		
 		ProcessBuilder task = new ProcessBuilder(new String[] { "lsof", "-F", "p", "+D", mountpoint });
 		task.redirectErrorStream(true); // Channel errors to stdOut.
 		task.redirectInput(Redirect.INHERIT);
 		
 		try {
 			Process proc = task.start();
 			BufferedReader input = new BufferedReader (new InputStreamReader( proc.getInputStream()));
 			String line;
 			while ((line = input.readLine ()) != null) {
 				lines.add(line);
 			}
 			input.close();
 			proc.waitFor();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		Set<String> pids = new HashSet<>();
 		
 		for(String line : lines) {
 			if(line == null || line.trim().length() == 0) {
 				continue;
 			}
 			if(line.startsWith("p")) {
 				pids.add(line.substring(1));
 			} 
 		}
 		
 		return pids;
 	}
 	
 	
     public Map<String, String> getLocks(String mountpoint) {
 		List<String> lines = new ArrayList<>();
 		
 		ProcessBuilder task = new ProcessBuilder(new String[] { "lsof", "-F", "pn", "+D", mountpoint });
 		task.redirectErrorStream(true); // Channel errors to stdOut.
 		task.redirectInput(Redirect.INHERIT);
 		
 		try {
 			Process proc = task.start();
 			BufferedReader input = new BufferedReader (new InputStreamReader( proc.getInputStream()));
 			String line;
 			while ((line = input.readLine ()) != null) {
 				lines.add(line);
 			}
 			input.close();
 			proc.waitFor();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		Map<String, String> locks = new HashMap<>();
 		
 		String currentPid = null;
 		for(String line : lines) {
 			if(line == null || line.trim().length() == 0) {
 				continue;
 			}
 			if(line.startsWith("p")) {
 				currentPid = line.substring(1);
 			} else if(line.startsWith("n")) {
 				locks.put(line.substring(1), currentPid);
 			}
 		}
 		
 		return locks;
 	}
 }
