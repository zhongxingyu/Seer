 package assoc_net;
 
 import com.google.common.collect.*;
 import org.jetbrains.annotations.NotNull;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.*;
 import java.util.*;
 import java.util.zip.GZIPInputStream;
 
 /**
  * @author A.Sirenko
  * Date: 7/28/13
  */
 public class AssociativeNet {
 
 	private static Logger LOG = LoggerFactory.getLogger(AssociativeNet.class);
 
 	private static final String DEFAULT_PATH = "data/eng_assoc.gz";
 
 	private Set<String> stims = null;
 
 	private Set<String> reacts = null;
 
 	private final ListMultimap<String, Connection> stimConnections;
 
 	private final ListMultimap<String, Connection> reactConnections;
 
 	private AssociativeNet(@NotNull Set<Connection> conns) {
 		stimConnections = ArrayListMultimap.create();
 		reactConnections = ArrayListMultimap.create();
 
 		for (Connection c : conns) {
 			stimConnections.put(c.getStim(), c);
 			reactConnections.put(c.getReak(), c);
 		}
 	}
 
 	public static @NotNull AssociativeNet loadDefaultNet() throws IOException {
 		List<Connection> conns = new ArrayList<>();
 
 		try(
 				BufferedReader br = new BufferedReader(new InputStreamReader(
 						new GZIPInputStream(new FileInputStream(DEFAULT_PATH))))) {
 			String line;
 			while((line = br.readLine()) != null) {
 				String[] token = line.toLowerCase().split(" ");
 				if (token.length != 3) {
 					throw new RuntimeException("Wrong line:" + line);
 				} else {
 					String stim = token[0];
 					String react = token[1];
 					try {
 						int count = Integer.valueOf(token[2]);
 						conns.add(new Connection(stim, react, count));
 					} catch (NumberFormatException e) {
 						LOG.error("Can't parse {}", line);
 					}
 				}
 			}
 		}
 
 		return new AssociativeNet(maxUniqueFromSourceWithDuplicates(conns));
 	}
 
 	@NotNull
 	static Set<Connection> maxUniqueFromSourceWithDuplicates(@NotNull Collection<Connection> conns) {
 		ListMultimap<String, Connection> stimConnectionsMap = ArrayListMultimap.create();
 		for (Connection c : conns) {
 			if (!stimConnectionsMap.containsKey(c.getStim())) {
 				stimConnectionsMap.put(c.getStim(), c);
 			} else {
 				leaveMax(stimConnectionsMap.get(c.getStim()), c);
 			}
 		}
 
 		return buildValuesSet(stimConnectionsMap.asMap().values());
 	}
 
 	@NotNull
 	private static Set<Connection> buildValuesSet(Collection<Collection<Connection>> groupedConns) {
 		Set<Connection> res = new HashSet<>();
 
 		for (Collection<Connection> pack : groupedConns) {
 			for (Connection c : pack) {
 				if (res.contains(c)) {
 					throw new IllegalStateException("Merged connections contains duplicates");
 				}
 				res.add(c);
 			}
 		}
 		return res;
 	}
 
 	static void leaveMax(@NotNull List<Connection> list, @NotNull Connection c) {
 		boolean merged = false;
 
 		for (int i = 0 ; i < list.size(); ++i) {
 			Connection ci = list.get(i);
 			if (c.equals(ci)) {
 				if (c.getCount() > ci.getCount()) {
 					list.remove(i);
 					list.add(c);
 					LOG.debug("{} replaced by {}", ci, c);
 				}
 				merged = true;
 
 				break;
 			}
 		}
 
 		if (!merged) {
 			list.add(c);
 		}
 	}
 
 	static void summarizeDuplicates(@NotNull List<Connection> list, @NotNull Connection c) {
 		boolean merged = false;
 
 		for (int i = 0 ; i < list.size(); ++i) {
 			Connection ci = list.get(i);
 			if (c.equals(ci)) {
 				list.remove(i);
 				list.add(c.add(ci));
 				merged = true;
 				LOG.debug("Merged {} and {}", c, ci);
 				break;
 			}
 		}
 
 		if (!merged) {
 			list.add(c);
 		}
 	}
 
 	@NotNull
 	public Set<String> getStims() {
 		if (stims == null) {
 			stims = Collections.unmodifiableSet(new HashSet<>(stimConnections.keySet()));
 		}
 		return stims;
 	}
 
 	@NotNull
 	public Set<String> getReacts() {
 		if (reacts == null) {
 			reacts = Collections.unmodifiableSet(new HashSet<>(reactConnections.keySet()));
 		}
 		return reacts;
 	}
 
 	@NotNull
 	public ConnSet getConnsForReact(@NotNull String react) {
 		return (reactConnections.containsKey(react)) ?
 			new ConnSet(reactConnections.get(react)) : ConnSet.EMPTY_SET;
 	}
 
 	@NotNull
 	public ConnSet getConnsForStim(@NotNull String stim) {
 		return (stimConnections.containsKey(stim)) ?
 			new ConnSet(stimConnections.get(stim)) : ConnSet.EMPTY_SET;
 	}
 }
