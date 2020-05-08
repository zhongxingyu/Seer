 package grisu.backend.model.fs;
 
 import grisu.backend.model.User;
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.RemoteFileSystemException;
 import grisu.model.FileManager;
 import grisu.model.MountPoint;
 import grisu.model.dto.GridFile;
 import grisu.settings.ServerPropertiesManager;
 import grith.jgrith.voms.VO;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import net.sf.ehcache.util.NamedThreadFactory;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A plugin that lists (non-volatile) filesystems in a tree-like group
  * structure.
  * 
  * The base url for this plugin is grid://groups . The next token will be the
  * name of the VO and beneath that this plugin will populate folders with both
  * sub-vos and files (provided the VO/Sub-VO inquestion is associated with a
  * filesystem).
  * 
  * Groups and "real" files will be merged in the child files of a url.
  * 
  * Since the way the filelisting is done can be a bit slow at times, if you only
  * want to know the child files for a certain VO, seperate the VO-part of the
  * url and the path-part using //, e.g. grid://groups/nz/NeSI//folder1/folder2 .
  * This will make sure to only query all mountpoints associated with the
  * /nz/NeSI VO, but it will not list filesystems possibly associated to the /nz
  * VO for a (real) folder called /NeSI.
  * 
  * 
  * @author Markus Binsteiner
  * 
  */
 public class GroupFileSystemPlugin implements VirtualFileSystemPlugin {
 
 	static final Logger myLogger = LoggerFactory
 			.getLogger(GroupFileSystemPlugin.class.getName());
 
 	public static final String IDENTIFIER = "groups";
 
 	private final static String BASE = (ServiceInterface.VIRTUAL_GRID_PROTOCOL_NAME
 			+ "://" + IDENTIFIER);
 
 	private final User user;
 
 	public GroupFileSystemPlugin(User user) {
 		this.user = user;
 	}
 
 	public GridFile createGridFile(String path, int recursiveLevels)
 			throws RemoteFileSystemException {
 
 		path = FileManager.removeTrailingSlash(path);
 
 		// Thread.dumpStack();
 		if (recursiveLevels > 1) {
 			throw new RuntimeException(
 					"Recursion levels greater than 1 not supported yet");
 		}
 
 		final String rightPart = path.substring(BASE.length());
 		if (rightPart.contains("//")) {
 			GridFile result = null;
 
 			// that means we everything before the // is the fqan and everything
 			// after is the path
 
 			final int i = rightPart.indexOf("//");
 			String fqanT = rightPart.substring(0, i);
 			final String restPath = rightPart.substring(i + 2);
 			if (!fqanT.startsWith("/")) {
 				fqanT = "/" + fqanT;
 			}
 
 			final Set<String> urls = resolveUrls(restPath, fqanT, true);
 
 			if (urls.size() == 0) {
 				// TODO not sure what to do
 				throw new RuntimeException("No real url found for virtual url.");
 			} else if (urls.size() == 1) {
 				result = new GridFile(urls.iterator().next(), -1L);
 				result.setIsVirtual(false);
 				result.setPath(path);
 			} else {
 				result = new GridFile(path, -1L);
 				result.setIsVirtual(true);
 				result.setPath(path);
 			}
 
 			if (recursiveLevels == 0) {
 				return result;
 			}
 
			final Set<GridFile> childs = listGroup(fqanT, restPath, true);
 
 			for (final GridFile file : childs) {
 
 				if (file.isInaccessable()) {
 					result.addChild(file);
 				} else {
 					result.addChildren(file.getChildren());
 				}
 
 			}
 
 			final Map<String, Set<String>> temp = findDirectChildFqans(fqanT);
 			final Set<String> childFqans = temp.keySet();
 			for (final String fqan : childFqans) {
 
 				final Set<MountPoint> mps = user.getMountPoints(fqan);
 				// we need to remove all volatile mountpoints first, users are
 				// not interested in those
 				final Iterator<MountPoint> it = mps.iterator();
 				while (it.hasNext()) {
 					final MountPoint mp = it.next();
 					if (mp.isVolatileFileSystem()) {
 						it.remove();
 					}
 				}
 
 				if (mps.size() == 1) {
 					final GridFile file = new GridFile(mps.iterator().next());
 					file.setName(FileManager.getFilename(fqan));
 					final String pathNew = (path + "/" + file.getName())
 							.replace("///", "/").replace("//", "/") + "//";
 					file.setPath(pathNew);
 					file.setIsVirtual(true);
 					file.addSites(temp.get(fqan));
 					result.addChild(file);
 				} else {
 					final GridFile file = new GridFile((BASE + fqan).replace(
 							"///", "/").replace("//", "/")
 							+ "//", fqan);
 					final String pathNew = (path + "/" + file.getName())
 							.replace("///", "/").replace("//", "/") + "//";
 					// String pathNew = file.getUrl();
 					file.setPath(pathNew);
 					for (final MountPoint mp : mps) {
 						// X.p("Add" + mp.getRootUrl());
 						file.addUrl(mp.getRootUrl(),
 								GridFile.FILETYPE_MOUNTPOINT_PRIORITY);
 					}
 					file.setIsVirtual(true);
 					file.addSites(temp.get(fqan));
 					result.addChild(file);
 				}
 			}
 
 			return result;
 
 		}
 
 		final int index = BASE.length();
 		final String importantUrlPart = path.substring(index);
 		final String[] tokens = StringUtils.split(importantUrlPart, '/');
 
 		GridFile result = null;
 
 		if (tokens.length == 0) {
 			// means root of the groupfilesystem
 
 			result = new GridFile(path, -1L);
 			result.setIsVirtual(true);
 			result.setPath(path);
 
 			if (recursiveLevels == 0) {
 				return result;
 			}
 
 			for (final VO vo : new TreeSet<VO>(user.getFqans().values())) {
 				final GridFile f = new GridFile(BASE + "/" + vo.getVoName(),
 						-1L);
 				f.setIsVirtual(true);
 				f.setPath(path + "/" + vo.getVoName());
 
 				for (final MountPoint mp : user.getMountPoints("/"
 						+ vo.getVoName())) {
 					f.addSite(mp.getSite());
 					f.addUrl(mp.getRootUrl(),
 							GridFile.FILETYPE_MOUNTPOINT_PRIORITY);
 				}
 
 				result.addChild(f);
 			}
 
 		} else if (tokens.length == 1) {
 			// means is root of VO so we need to list potential files on all
 			// sites that support this vo
 			// and also all child vos
 			final String parentfqan = "/" + tokens[0];
 			final Set<String> urls = resolveUrls(path, parentfqan, true);
 
 			if (urls.size() == 1) {
 				result = new GridFile(urls.iterator().next(), -1L);
 				result.setIsVirtual(false);
 				result.setPath(path);
 			} else {
 				result = new GridFile(path, -1L);
 				result.setIsVirtual(true);
 				result.setPath(path);
 				for (final String u : urls) {
 					result.addUrl(u, GridFile.FILETYPE_MOUNTPOINT_PRIORITY);
 				}
 			}
 
 			if (recursiveLevels == 0) {
 				return result;
 			}
 
 			final Map<String, Set<String>> temp = findDirectChildFqans(parentfqan);
 			final Set<String> childFqans = temp.keySet();
 
 			for (final String fqan : childFqans) {
 
 				final Set<MountPoint> mps = user.getMountPoints(fqan);
 
 				// we need to remove volatile mountpoints
 				final Iterator<MountPoint> it = mps.iterator();
 				while (it.hasNext()) {
 					final MountPoint mp = it.next();
 					if (mp.isVolatileFileSystem()) {
 						it.remove();
 					}
 				}
 
 				if (mps.size() == 1) {
 					final GridFile file = new GridFile(mps.iterator().next());
 					file.setName(FileManager.getFilename(fqan));
 					file.setPath(path + "/" + file.getName());
 					file.setIsVirtual(true);
 					file.addSites(temp.get(fqan));
 					result.addChild(file);
 				} else {
 					final GridFile file = new GridFile(BASE + fqan, fqan);
 					file.setPath(path + "/" + file.getName());
 					file.setIsVirtual(true);
 					file.addSites(temp.get(fqan));
 					for (final MountPoint mp : mps) {
 						file.addUrl(mp.getRootUrl(),
 								GridFile.FILETYPE_MOUNTPOINT_PRIORITY);
 					}
 					file.addFqan(fqan);
 					result.addChild(file);
 
 				}
 			}
 
			final Set<GridFile> files = listGroup("/" + tokens[0], "", true);
 			for (final GridFile file : files) {
 				if (file.isInaccessable()) {
 					result.addChild(file);
 				} else {
 					result.addChildren(file.getChildren());
 				}
 			}
 
 		} else {
 
 			String currentUrl = BASE;
 			String potentialFqan = "";
 
 			final Set<String> parentUrls = new HashSet<String>();
 			final Set<GridFile> children = new TreeSet<GridFile>();
 
 			for (final String token : tokens) {
 
 				currentUrl = currentUrl + "/" + token;
 				potentialFqan = potentialFqan + "/" + token;
 
 				if (!user.getFqans().keySet().contains(potentialFqan)) {
 					continue;
 				}
 
 				final String rest = path.substring(currentUrl.length());
 
 				final Set<String> urls = resolveUrls(rest, potentialFqan, true);
 				parentUrls.addAll(urls);
 
 				if (recursiveLevels == 1) {
 
					final Set<GridFile> files = listGroup(potentialFqan, rest,
							false);
 					for (final GridFile file : files) {
 
 						if (file.isInaccessable()) {
 							children.add(file);
 						} else {
 							children.addAll(file.getChildren());
 						}
 					}
 
 				}
 
 			}
 
 			if (recursiveLevels == 1) {
 
 				final Map<String, Set<String>> temp = findDirectChildFqans(potentialFqan);
 				final Set<String> childFqans = temp.keySet();
 				for (final String fqan : childFqans) {
 					final Set<MountPoint> mps = user.getMountPoints(fqan);
 
 					// we need to remove volatile mountpoints
 					final Iterator<MountPoint> it = mps.iterator();
 					while (it.hasNext()) {
 						final MountPoint mp = it.next();
 						if (mp.isVolatileFileSystem()) {
 							it.remove();
 						}
 					}
 					if (mps.size() == 0) {
 						continue;
 					}
 					if (mps.size() == 1) {
 						final GridFile file = new GridFile(mps.iterator()
 								.next());
 						file.setName(FileManager.getFilename(fqan));
 						file.setPath(path + "/" + file.getName() + "//");
 						file.setIsVirtual(true);
 						file.addSites(temp.get(fqan));
 						children.add(file);
 					} else {
 						final GridFile file = new GridFile(BASE + fqan, fqan);
 						file.setPath(path + "/" + file.getName() + "//");
 						file.setIsVirtual(true);
 						file.addSites(temp.get(fqan));
 						children.add(file);
 					}
 				}
 			}
 
 			if (recursiveLevels == 0) {
 
 				myLogger.debug("Checking whether any of the possible real urls for virtual url "
 						+ path + " exist...");
 				// test whether files actually exist
 
 				if (parentUrls.size() > 0) {
 					final NamedThreadFactory tf = new NamedThreadFactory(
 							"realUrlCheckForVfs");
 					final ExecutorService executor = Executors
 							.newFixedThreadPool(parentUrls.size(), tf);
 
 					// let's sort using last modified date, so that we can give
 					// the
 					// last file the highest priority
 					final Map<Long, String> temp = Collections
 							.synchronizedMap(new TreeMap<Long, String>());
 
 					for (final String url : parentUrls) {
 						final Thread t = new Thread() {
 							@Override
 							public void run() {
 								try {
 									final long ts = user.getFileManager()
 											.lastModified(url);
 
 									temp.put(ts, url);
 									myLogger.debug("File exists: " + url);
 									// boolean exists =
 									// user.getFileSystemManager()
 									// .fileExists(url);
 									// if (exists) {
 									// myLogger.debug("File exists: " + url);
 									// temp.add(url);
 									// } else {
 									// myLogger.debug("File does not exit: " +
 									// url);
 									// }
 								} catch (final Exception e) {
 									myLogger.debug("File does not exit: " + url
 											+ " - " + e.getLocalizedMessage());
 								}
 
 							}
 						};
 						executor.execute(t);
 					}
 
 					executor.shutdown();
 					try {
 						executor.awaitTermination(60, TimeUnit.SECONDS);
 					} catch (final InterruptedException e) {
 					}
 
 					if (temp.size() == 1) {
 						result = new GridFile(temp.values().iterator().next(),
 								-1L);
 						result.setIsVirtual(false);
 						result.setPath(path);
 					} else {
 						result = new GridFile(path, -1L);
 						result.setIsVirtual(true);
 						result.setPath(path);
 						int i = 0;
 						for (final Long lm : temp.keySet()) {
 							result.addUrl(temp.get(lm),
 									GridFile.FILETYPE_FOLDER_PRIORITY + i);
 							i = i + 1;
 						}
 					}
 
 					return result;
 				}
 
 			}
 
 			// xxx
 
 			if (parentUrls.size() == 1) {
 				final String url = parentUrls.iterator().next();
 				final boolean isFolder = user.getFileManager().isFolder(
 						url);
 				if (isFolder) {
 					result = new GridFile(url, -1L);
 				} else {
 					result = user.ls(url, 0);
 				}
 				result.setIsVirtual(false);
 				result.setPath(path);
 			} else {
 				result = new GridFile(path, -1L);
 				result.setIsVirtual(true);
 				result.setPath(path);
 				for (final String u : parentUrls) {
 					result.addUrl(u, GridFile.FILETYPE_FOLDER_PRIORITY);
 				}
 			}
 
 			result.addChildren(children);
 		}
 
 		return result;
 
 	}
 
 	private Map<String, Set<String>> findDirectChildFqans(String parentFqan) {
 
 		final String[] tokens = parentFqan.substring(1).split("/");
 
 		final Map<String, Set<String>> result = new TreeMap<String, Set<String>>();
 
 		for (final String fqan : user.getFqans().keySet()) {
 
 			if (!fqan.startsWith(parentFqan)) {
 				continue;
 			}
 
 			final String[] fqanTokens = fqan.substring(1).split("/");
 			final int fqanTokenLength = fqanTokens.length;
 
 			if ((fqanTokenLength == (tokens.length + 1))
 					&& fqanTokens[tokens.length - 1]
 							.equals(tokens[tokens.length - 1])) {
 
 				final Set<String> sites = new TreeSet<String>();
 				final Set<MountPoint> mps = user.getMountPoints(fqan);
 				// removing volatile mountpoints
 				final Iterator<MountPoint> it = mps.iterator();
 				while (it.hasNext()) {
 					final MountPoint mp = it.next();
 					if (mp.isVolatileFileSystem()) {
 						it.remove();
 					}
 				}
 
 				final Map<String, Set<String>> childFqans = findDirectChildFqans(fqan);
 				boolean hasInterestingChilds = false;
 
 				for (final String fqanTemp : childFqans.keySet()) {
 					if (childFqans.get(fqanTemp).size() > 0) {
 						hasInterestingChilds = true;
 						break;
 					}
 				}
 
 				if ((mps.size() == 0) && !hasInterestingChilds) {
 					continue;
 				}
 
 				for (final MountPoint mp : mps) {
 					sites.add(mp.getSite());
 				}
 
 				result.put(fqan, sites);
 			}
 		}
 		return result;
 	}
 
	private Set<GridFile> listGroup(String fqan, String path,
			final boolean includeFailedChilds)
					throws RemoteFileSystemException {
 
 		final Set<MountPoint> mps = user.getMountPoints(fqan);
 
 		// removing volatile mountpoints
 		final Iterator<MountPoint> it = mps.iterator();
 		while (it.hasNext()) {
 			final MountPoint mp = it.next();
 			if (mp.isVolatileFileSystem()) {
 				it.remove();
 			}
 		}
 
 		if (mps.size() == 0) {
 			return new TreeSet<GridFile>();
 		}
 
 		final Map<String, GridFile> result = Collections
 				.synchronizedMap(new HashMap<String, GridFile>());
 
 		final NamedThreadFactory tf = new NamedThreadFactory("listGroup");
 		final ExecutorService pool = Executors.newFixedThreadPool(mps.size(),
 				tf);
 
 		for (final MountPoint mp : mps) {
 
 			final String urlToQuery = mp.getRootUrl() + "/" + path;
 
 			final Thread t = new Thread() {
 				@Override
 				public void run() {
 
 					myLogger.debug("Groupfilesystem list group started: "
 							+ mp.getAlias() + " / " + urlToQuery);
 
 					result.put(urlToQuery, null);
 
 					try {
 						final GridFile file = user.getFileManager()
 								.getFolderListing(urlToQuery, 1);
 						file.addSite(mp.getSite());
 						result.put(urlToQuery, file);
 
 					} catch (final RemoteFileSystemException rfse) {
 						final String msg = rfse.getLocalizedMessage();
						if (includeFailedChilds
								&& !msg.contains("not a folder")) {
 							final GridFile f = new GridFile(urlToQuery, false,
 									rfse);
 							f.addSite(mp.getSite());
 							result.put(urlToQuery, f);
 						}
 					} catch (final Exception ex) {
 						final GridFile f = new GridFile(urlToQuery, false, ex);
 						f.addSite(mp.getSite());
 						result.put(urlToQuery, f);
 					}
 
 					myLogger.debug("Groupfilesystem list group finished: "
 							+ mp.getAlias() + " / " + urlToQuery);
 
 				}
 			};
 
 			pool.execute(t);
 		}
 
 		pool.shutdown();
 
 		final int timeout = ServerPropertiesManager.getFileListingTimeOut();
 		try {
 			final boolean timedOut = !pool.awaitTermination(timeout,
 					TimeUnit.SECONDS);
 			if (timedOut) {
 				myLogger.debug("GroupfilePlugin list group timed out....");
 
 				// filling missing files
 				for (final String url : result.keySet()) {
 					if (result.get(url) == null) {
 						final GridFile temp = new GridFile(
 								url,
 								false,
 								new Exception(
 										"Timeout ("
 												+ timeout
 												+ " seconds) while trying to list children."));
 						result.put(url, temp);
 					}
 				}
 			}
 		} catch (final InterruptedException e) {
 			throw new RemoteFileSystemException(e);
 		}
 
 		return new TreeSet<GridFile>(result.values());
 
 	}
 
 	private Set<String> resolveUrls(String path, String fqan,
 			boolean excludeVolatileFilesystems) {
 
 		final Set<MountPoint> mps = user.getMountPoints(fqan);
 
 		// remove volatile mountpoints
 		final Iterator<MountPoint> it = mps.iterator();
 		while (it.hasNext()) {
 			final MountPoint mp = it.next();
 			if (excludeVolatileFilesystems && mp.isVolatileFileSystem()) {
 				it.remove();
 			}
 		}
 
 		final Set<String> urls = new HashSet<String>();
 
 		for (final MountPoint mp : mps) {
 			final String urlToQuery = mp.getRootUrl() + "/" + path;
 			urls.add(urlToQuery);
 
 		}
 
 		return urls;
 	}
 }
