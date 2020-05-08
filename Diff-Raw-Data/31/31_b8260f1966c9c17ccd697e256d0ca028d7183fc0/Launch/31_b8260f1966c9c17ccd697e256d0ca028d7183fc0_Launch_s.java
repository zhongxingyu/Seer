 package com.github.vortexellauncher;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import com.github.vortexellauncher.exceptions.InvalidModpackException;
 import com.github.vortexellauncher.exceptions.ValidationException;
 import com.github.vortexellauncher.gui.OfflineDialog;
 import com.github.vortexellauncher.launch.MinecraftLauncher;
 import com.github.vortexellauncher.pack.FileStatus;
 import com.github.vortexellauncher.pack.ModFile;
 import com.github.vortexellauncher.pack.ModType;
 import com.github.vortexellauncher.pack.Modpack;
 import com.github.vortexellauncher.pack.PackCache;
 import com.github.vortexellauncher.pack.PackMetaManager;
 import com.github.vortexellauncher.util.ErrorUtils;
 import com.github.vortexellauncher.util.JsonUtils;
 import com.github.vortexellauncher.util.Utils;
 import com.github.vortexellauncher.workers.GameUpdateWorker;
 import com.github.vortexellauncher.workers.LoginWorker;
 import com.github.vortexellauncher.workers.PackValidator;
 import com.google.gson.JsonElement;
 
 /**
  * Controls the flow of the program
  * @author Bindernews
  *
  */
 public class Launch {
 	
 	public static final VersionData VERSION = new VersionData(1,0,0,0);
 	public static final String UPDATE_URL = "https://googledrive.com/host/0Bw00_I2xsVk3WnNQaTRNby1GeHc/launcher_version.json";
 	
 	private LoginResponse RESPONSE = null;
 	private ExecutorService driverThread = Executors.newSingleThreadExecutor();
 	private ExecutorService workThread = Executors.newSingleThreadExecutor();
 	
 	private static final File lastLoginFile = new File(OS.dataDir(), "loginlast");
 	
 	public Launch() {
 		try {
 			String lastLoginStr = Utils.simpleCryptIn(lastLoginFile);
 			if (lastLoginStr != null) {
 				UserPass up = new UserPass(lastLoginStr);
 				Main.frame().setUserPass(up);
 				Main.frame().getChkRemember().setSelected(true);
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void attemptLogin(final String username, final String password, final boolean save) {
 		if (username.length() == 0) {
 			Main.frame().setLoginStatus("No username");
 			return;
 		}
 		Main.frame().setEnabled(false);
 		LoginWorker worker = new LoginWorker(username, password) {
 			protected void done() {
 				workThread.execute(new Runnable() {
 					public void run() {
 						onDone();
 					}
 				});
 			}
 			public void onDone() {
 				String responseStr = null;
 				try {
 					responseStr = get();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				} catch (ExecutionException e) {
 					if (e.getCause() instanceof IOException
 							|| e.getCause() instanceof MalformedURLException) {
 						OfflineDialog playOffline = new OfflineDialog(Main.frame());
 						playOffline.setVisible(true);
 					}
 					return;
 				}
 				try {
 					RESPONSE = new LoginResponse(responseStr);
 					Main.frame().setLoginStatus("Login succeded");
 					if (save) {
 						try {
 							UserPass up = new UserPass(username, password);
 							Utils.simpleCryptOut(up.combine(), lastLoginFile);
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 				catch (IllegalArgumentException e) {
 					if (responseStr.contains(":")) {
 						// log Invalid server response
 					}
 					else {
 						if (responseStr.equalsIgnoreCase("bad login")) {
 							Main.frame().setLoginStatus("Bad login");
 						}
 						else if (responseStr.equalsIgnoreCase("old version")) {
 							Main.frame().setLoginStatus("Outdated launcher");
 						}
 						else if (responseStr.startsWith("Account migrated, use e-mail")) {
 							Main.frame().setLoginStatus("Login using your email account");
 						}
 						else {
 							Main.frame().setLoginStatus(responseStr);
 							new OfflineDialog(Main.frame()).setVisible(true);
 						}
 					}
 					Main.frame().setEnabled(true);
 					return;
 				}
 				// log "Login complete"
 				runLaunchGame();
 			}
 		};
 		worker.execute();
 	}
 	
 	public void playOffline() {
 		String user = Main.frame().getUsername();
 		RESPONSE = new LoginResponse("0: :" + user + ": ");
 		runLaunchGame();
 	}
 	
 	private static enum ProgressState {
 		None,
 		CheckingForUpdates,
 		ReadingCache,
 		Validating,
 		Updating,
 		Revalidating,
 		Launching
 	}
 	
 	protected void runLaunchGame() {
 		driverThread.submit(new Runnable() {
 			public void run() {
 				launchGame();
 			}
 		});
 	}
 
 	protected void launchGame() {
 		boolean offline = RESPONSE.sessionID.equals("0");
 		PackCache cache;
 		PackValidator validator;
 		List<ModFile> fileList;
 		List<FileStatus> statusList;
 		EnumSet<FileStatus> statusSet;
 		GameUpdateWorker updateWorker;
 		PackMetaManager metaManager;
 		Modpack modpack;
 		
 		ProgressState progress = ProgressState.None;
 		Main.frame().getOptionsGui().updateSettings();
 		try {
 			progress = ProgressState.ReadingCache;
 			System.out.println("Launch status: " + progress.name());
 			metaManager = new PackMetaManager();
 			modpack = metaManager.loadModpack(Main.settings().getModpackName());
 			if (modpack.getUpdateURL() != null) {
 				progress = ProgressState.CheckingForUpdates;
 				System.out.println("Launch status: " + progress.name());
 				try {
 					JsonElement nextJson = JsonUtils.readJsonURL(new URL(modpack.getUpdateURL()));
 					Modpack nextPack = new Modpack(nextJson.getAsJsonObject(), modpack.getUpdateURL());
 					if (nextPack.version.compareTo(modpack.version) > 0) {
 						metaManager.updatePack(nextJson.getAsJsonObject(), modpack.name);
 						modpack = nextPack;
 					}
 				} catch (IOException e) {
 					System.err.println("Failed to check for modpack update");
 					e.printStackTrace();
 				}
 			}
 			cache = new PackCache(modpack);
 			fileList = new ArrayList<ModFile>(cache.getModpack().getMods());
 			populateWithMCCoreFiles(cache, fileList);
 			
 			progress = ProgressState.Validating;
 			System.out.println("Launch status: " + progress.name());
 			validator = new PackValidator(cache, fileList);
 			statusList = validator.runHere();
 			statusSet = EnumSet.copyOf(statusList);
 			
 			if (offline) {
 				if (!FileStatus.StatusCouldPlay.containsAll(statusSet)) { // there are files with status cannotPlay
 					throw new ValidationException("Can not update files while in offline mode");
 				}
 			} else {
 				progress = ProgressState.Updating;
 				System.out.println("Launch status: " + progress.name());
 				if (setsOverlap(statusSet, FileStatus.StatusDoDownload)) {
 					updateWorker = new GameUpdateWorker(cache, fileList, statusList);
 					updateWorker.execute();
 					updateWorker.get();
 				}
 			}
 			progress = ProgressState.Launching;
 			System.out.println("Launch status: " + progress.name());
 			MinecraftLauncher.launchMinecraft(modpack, RESPONSE.username, RESPONSE.sessionID);
 			Main.frame().dispose();
 		} catch (ExecutionException e) {
 			ErrorUtils.showException("Exception occured while " + progress.name(), e.getCause(), true);
 			e.printStackTrace();
 		} catch (IOException | NullPointerException | ValidationException | InterruptedException | InvalidModpackException e) {
 			ErrorUtils.showException("Exception occured while " + progress.name(), e, true);
 			e.printStackTrace();
 		}
 		Main.frame().dispose();
 		Main.attemptExit();
 	}
 	
 	@SuppressWarnings("rawtypes")
 	protected static boolean setsOverlap(Set a, Set b) {
 		if (a.size() < b.size()) {
 			for(Object o : a) {
 				if (b.contains(o))
 					return true;
 			}
 			return false;
 		} else {
 			for(Object o : b) {
 				if (a.contains(o))
 					return true;
 			}
 			return false;
 		}
 	}
 	
 	
 	
 	protected void populateWithMCCoreFiles(PackCache packData, List<ModFile> fileList) {
 		String baseurl = "https://s3.amazonaws.com/MinecraftDownload/";
 		String[] binFileNames = {"jinput.jar", "lwjgl.jar", "lwjgl_util.jar"};
 		for(String bfn : binFileNames) {
 			ModFile mf = new ModFile(bfn, ModType.Bin, baseurl + bfn);
 			mf.setVersion(new VersionData(0));
 			fileList.add(mf);
 		}
 		ModFile mcJar = new ModFile("minecraft.jar", ModType.Bin, packData.getModpack().mcversion.getJarUrl());
 		mcJar.setVersion(new VersionData(0));
		ModFile mcNatives = new ModFile(OS.nativesJar(), ModType.Native, baseurl + OS.nativesJar());
 		mcNatives.isArchive = true;
 		mcNatives.setVersion(new VersionData(0));
 		fileList.add(mcJar);
 		fileList.add(mcNatives);
 	}
 	
 	public void resetLogin() {
 		RESPONSE = null;
 	}
 }
