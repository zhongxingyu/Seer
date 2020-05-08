 package interdroid.vdb.content;
 
 import interdroid.vdb.content.EntityUriMatcher.MatchType;
 import interdroid.vdb.content.EntityUriMatcher.UriMatch;
 import interdroid.vdb.content.VdbConfig.RepositoryConf;
 import interdroid.vdb.content.avro.AvroContentProvider;
 import interdroid.vdb.content.avro.AvroProviderRegistry;
 import interdroid.vdb.persistence.api.VdbInitializer;
 import interdroid.vdb.persistence.api.VdbRepositoryRegistry;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.ContentProvider;
 import android.content.Context;
 import android.net.Uri;
 
 public class VdbProviderRegistry {
 	private static final Logger logger = LoggerFactory.getLogger(VdbProviderRegistry.class);
 
 	private static final String BASE_TYPE = "vnd." + VdbMainContentProvider.AUTHORITY;
 
 	private final Context context_;
 
 	private static final Map<String,RepositoryInfo> repoInfos_ = new HashMap<String, RepositoryInfo>();
 
 	public static final String REPOSITORY_NAME = "repoName";
 	private static final String REPOSITORY_ID = "id_";
 
 	private static class RepositoryInfo {
 		public final RepositoryConf conf_;
 		public GenericContentProvider provider_ = null;
 
 		public RepositoryInfo(RepositoryConf conf)
 		{
 			conf_ = conf;
 		}
 	}
 
 	public VdbProviderRegistry(Context context) throws IOException {
 		context_ = context;
 
 		if (repoInfos_.size() == 0) {
 			logger.debug("Initializing static repositories.");
 			VdbConfig config = new VdbConfig(context);
 			initializeAll(config.getRepositories());
 
 			logger.debug("Initializing Avro Repos.");
 			List<RepositoryConf> infos = ((AvroProviderRegistry)get(AvroProviderRegistry.URI)).getAllRepositories();
 			initializeAll(infos);
 			logger.debug("All repositories registered.");
 		}
 	}
 
 	private void initializeAll(List<RepositoryConf> repositories) throws IOException {
 		// Initialize all the child content providers, one for each repository.
 		for (RepositoryConf repoConf : repositories) {
 			registerRepository(repoConf);
 		}
 	}
 
 	public void registerRepository(RepositoryConf repoConf) {
 		RepositoryInfo repoInfo = new RepositoryInfo(repoConf);
 		if (!repoInfos_.containsKey(repoInfo.conf_.name_)) {
 			logger.debug("Storing into repoInfos: {}", repoInfo.conf_.name_);
 			repoInfos_.put(repoInfo.conf_.name_, repoInfo);
 		}
 	}
 
 	private void initializeRepo(Context context, String name, VdbInitializer initializer) throws IOException {
 		logger.debug("Initializing repository: {}", name);
 		VdbRepositoryRegistry.getInstance().addRepository(context,
 				name, initializer);
 	}
 
 	private void buildProvider(Context context, RepositoryInfo info) throws IOException {
 		logger.debug("Building provider for: {}", info);
 		try {
 			if (info.provider_ == null) {
 				if(info.conf_.avroSchema_ != null) {
 					info.provider_ = new AvroContentProvider(info.conf_.avroSchema_);
 				} else {
 					info.provider_ = (GenericContentProvider) Class.forName(info.conf_.contentProvider_).newInstance();
 				}
 				initializeRepo(context_, info.conf_.name_, info.provider_.buildInitializer());
 
 				// Do this at the end, since onCreate will be called in the child
 				// We want everything to be registered prior to this happening.
 				logger.debug("Attaching context: {} to provider.", context);
 				info.provider_.attachInfo(context, null);
 				logger.debug("Initialized Repository: " + info.conf_.name_);
 			}
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		} catch (InstantiationException e) {
 			throw new RuntimeException(e);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public ContentProvider get(Uri uri) {
 		UriMatch match = EntityUriMatcher.getMatch(uri);
 		RepositoryInfo info = repoInfos_.get(match.repositoryName);
 		validateUri(uri, info, match);
 		try {
 			buildProvider(context_, info);
 		} catch (IOException e) {
 			throw new RuntimeException("Unable to build provider.", e);
 		}
 		return info.provider_;
 	}
 
 	private void validateUri(Uri uri, RepositoryInfo info, UriMatch match)
 	{
 		if (info == null) {
 			throw new IllegalArgumentException("Bad URI: unregistered repository: " + match.repositoryName);
 		}
 		if (match.type == MatchType.REPOSITORY) {
 			throw new IllegalArgumentException("Bad URI: only repository was specified. " + uri);
 		}
 	}
 
 	public String getType(Uri uri) {
 		UriMatch match = EntityUriMatcher.getMatch(uri);
 		RepositoryInfo info = repoInfos_.get(match.repositoryName);
 		String type = null;
 
 		if (info == null) {
 			throw new IllegalArgumentException("Bad URI: unregistered repository. " + uri);
 		}
 		logger.debug("Getting type: {} : {}", match.entityName, match.type);
 		if (match.entityName == null) { // points to actual commit/branch
 			switch(match.type) {
 			case REPOSITORY:
 				type = BASE_TYPE + "/repository";
 				break;
 			case COMMIT:
 				type = BASE_TYPE + "/commit";
 				break;
 			case LOCAL_BRANCH:
 				type = BASE_TYPE + "/branch.local";
 				break;
 			case REMOTE_BRANCH:
 				type = BASE_TYPE + "/branch.remote";
 				break;
 			case REMOTE:
 				type = BASE_TYPE + "/remote";
 				break;
 			}
 		} else {
 			logger.debug("Asking provider for type: {}", uri);
 			type = info.provider_.getType(uri);
 		}
 		logger.debug("Returning type: {}", type);
 		return type;
 	}
 
 	public void initByName(String name) {
 		RepositoryInfo info = repoInfos_.get(name);
 		try {
 			buildProvider(context_, info);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public List<Map<String, Object>> getAllRepositories() {
 		ArrayList<Map<String, Object>> repositories = new ArrayList<Map<String, Object>>();
 		for (RepositoryInfo info : repoInfos_.values()) {
 			// We exclude all interdroid repositories
 			if (! info.conf_.name_.startsWith("interdroid.vdb")) {
 				HashMap<String, Object> map = new HashMap<String, Object>();
 				map.put(REPOSITORY_ID, info.conf_.name_.hashCode());
 				map.put(REPOSITORY_NAME, info.conf_.name_);
 				repositories.add(map);
 			}
 		}
 		return repositories;
 	}
 
 	public List<String> getAllRepositoryNames() {
 		ArrayList<String> repositories = new ArrayList<String>();
 		for (RepositoryInfo info : repoInfos_.values()) {
 			// We exclude all interdroid repositories
 			if (! info.conf_.name_.startsWith("interdroid.vdb")) {
 				repositories.add(info.conf_.name_);
 			}
 		}
 		return repositories;
 	}
 }
