 package name.richardson.james.bukkit.utilities.updater;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.logging.Level;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
 import org.codehaus.plexus.util.FileUtils;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import name.richardson.james.bukkit.utilities.localisation.Localisation;
 import name.richardson.james.bukkit.utilities.localisation.ResourceBundleByClassLocalisation;
 
 public class BukkitDevPluginUpdater extends AbstractPluginUpdater {
 
 	private static final String API_NAME_VALUE = "name";
 	private static final String API_LINK_VALUE = "downloadUrl";
 	private static final String API_RELEASE_TYPE_VALUE = "releaseType";
 	private static final String API_FILE_NAME_VALUE = "fileName";
 	private static final String API_GAME_VERSION_VALUE = "gameVersion";
 
 	private static final String API_QUERY = "/servermods/files?projectIds=";
 	private static final String API_HOST = "https://api.curseforge.com";
 	private static final String API_USER_AGENT = "BukkitUtilities Updater";
 
 	private static final String DL_HOST = "cursecdn.com";
 
 	private final Localisation localisation = new ResourceBundleByClassLocalisation(BukkitDevPluginUpdater.class);
 	private final int projectId;
 	private final File updateFolder;
 	private final String gameVersion;
 
 	private String versionFileName;
 	private String versionGameVersion;
 	private String versionLink;
 	private String versionName;
 	private String versionType;
 
 	public BukkitDevPluginUpdater(PluginDescriptionFile pluginDescriptionFile, Branch branch, State state, int projectId, File updateFolder, String gameVersion) {
 		super(pluginDescriptionFile, branch, state);
 		this.projectId = projectId;
 		this.updateFolder = updateFolder;
 		this.gameVersion = gameVersion;
 	}
 
 	/**
 	 * Get the current remote version of the plugin.
 	 * <p/>
 	 * This should be the latest released and available version matching the branch requested.
 	 *
 	 * @return The current remote version of the plugin.
 	 */
 	@Override
 	public String getRemoteVersion() {
 		return versionName;
 	}
 
 	@Override
 	public void update() {
 		if (isNewVersionAvailable() && getState() == State.UPDATE) {
 			try {
 				getLogger().log(Level.INFO, "downloading-new-version", versionLink);
 				File destination = new File(updateFolder, getName() + ".jar");
 				URLConnection urlConnection = getConnection(versionLink);
 				FileUtils.copyURLToFile(urlConnection.getURL(), destination);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 			} catch (IOException e) {
 				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 			}
 		}
 	}
 
 	@Override
 	public String toString() {
 		return new ToStringBuilder(this)
 		.append("projectId", projectId)
 		.append("updateFolder", updateFolder)
 		.append("versionFileName", versionFileName)
 		.append("versionGameVersion", versionGameVersion)
 		.append("versionLink", versionLink)
 		.append("versionName", versionName)
 		.append("versionType", versionType)
 		.toString();
 	}
 
 	private URLConnection getConnection(String urlString)
 	throws IOException {
 		if (!urlString.contains(DL_HOST) && !urlString.contains(API_HOST)) throw new IllegalArgumentException();
 		URL url = new URL(urlString);
 		URLConnection urlConnection = url.openConnection();
 		urlConnection.addRequestProperty("User-Agent", API_USER_AGENT);
 		return urlConnection;
 	}
 
 	@Override
 	public void run() {
 		try {
 			URLConnection urlConnection = getConnection(API_HOST + API_QUERY + projectId);
 			final BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
 			String response = reader.readLine();
 			JSONArray array = (JSONArray) JSONValue.parse(response);
 			java.util.ListIterator versions = array.listIterator(array.size() - 1);
 			while(versions.hasPrevious()) {
 				JSONObject latest = (JSONObject) versions.previous();
 				versionType = (String) latest.get(API_RELEASE_TYPE_VALUE);
 				versionGameVersion = (String) latest.get(API_GAME_VERSION_VALUE);
				if ((versionType.equals("beta") || versionType.equals("alpha")) && getBranch().equals(Branch.STABLE)) continue;
 				if (!isCompatiableWithGameVersion()) continue;
 				versionName = (String) latest.get(API_NAME_VALUE);
 				versionLink = (String) latest.get(API_LINK_VALUE);
 				versionFileName = (String) latest.get(API_FILE_NAME_VALUE);
 				versionGameVersion = (String) latest.get(API_GAME_VERSION_VALUE);
 				String[] params = {getName(), getRemoteVersion()};
 				getLogger().log(Level.INFO, "new-version-available", params);
 				break;
 			}
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private boolean isCompatiableWithGameVersion() {
 		final DefaultArtifactVersion current = new DefaultArtifactVersion(this.gameVersion);
 		final DefaultArtifactVersion target = new DefaultArtifactVersion(versionGameVersion);
 		final Object params[] = {target.toString(), current.toString()};
 		return current.compareTo(target) != -1;
 	}
 
 
 }
