 package elw.dao;
 
 import elw.vo.Group;
 import org.akraievoy.gear.G;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 public class GroupDao {
 	private static final Logger log = LoggerFactory.getLogger(GroupDao.class);
 
 	protected final ObjectMapper mapper;
 	protected int cacheTime = 180000;
 
 	final Map<String, Group> groupCache = new TreeMap<String, Group>();
 
 	protected long cacheStamp = 0;
 
 	public void setCacheTime(int cacheTime) {
 		this.cacheTime = cacheTime;
 	}
 
 	public GroupDao(ObjectMapper mapper) {
 		this.mapper = mapper;
 	}
 
 	public synchronized String[] findGroupIds() {
 		refreshCache();
 
 		final Set<String> keys = groupCache.keySet();
 		return keys.toArray(new String[keys.size()]);
 	}
 
 	public void refreshCache() {
 		final long now = System.currentTimeMillis();
 		if (now - cacheStamp >= cacheTime) {
 			cacheStamp = now;
 		} else {
 			return;
 		}
 
 		final File groupsDir = new File(System.getProperty("user.home"), "elw-data/groups");
 		final File[] groupFiles = groupsDir.listFiles(new FileFilter() {
 			public boolean accept(File pathname) {
 				return pathname.isFile() && pathname.getName().endsWith(".json");
 			}
 		});
 
 		for (File groupFile : groupFiles) {
 			try {
 				final Group course = mapper.readValue(groupFile, Group.class);
 				if (groupFile.getName().equals(course.getId() + ".json")) {
 					groupCache.put(course.getId(), course);
 				} else {
					log.warn("wrong id at file {}", groupFile.getPath());
 				}
 			} catch (IOException e) {
 				log.warn("failed to read {}: {}", groupFile.getPath(), G.report(e));
 				log.trace("trace", e);
 			}
 		}
 	}
 
 	public synchronized Group findGroup(final String id) {
 		refreshCache();
 
 		return groupCache.get(id);
 	}
 
 	public Group[] findAllGroups() {
 		refreshCache();
 
 		final Collection<Group> groups = groupCache.values();
 		return groups.toArray(new Group[groups.size()]);
 	}
 }
