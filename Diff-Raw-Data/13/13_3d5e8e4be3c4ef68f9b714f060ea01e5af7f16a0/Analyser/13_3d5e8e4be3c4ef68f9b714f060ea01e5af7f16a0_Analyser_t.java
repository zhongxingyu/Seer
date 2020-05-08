 package uk.me.thega;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.Model;
 
 import uk.me.thega.graph.DotColours;
 import uk.me.thega.graph.DotGenerator;
 import uk.me.thega.url.RepoURLReader;
 
 
 import fr.loria.GraphViz;
 
 /**
  * Main class for running the maven-dependency analyser.
  * <br>
  * Takes arguments for:
  * <ul>
  * <li>The file to read the repo paths from</li>
  * <li>The restriction upon the artifacts mapped</li>
  * <li>The username if the repos are behind basic HTTP authentication</li>
  * <li>The password if the repos are behind basic HTTP authentication</li>
  * </ul>
  * 
  * @author pwhittlesea
  *
  */
 public class Analyser {
 
 	private final Map<String, Map<String, List<String>>> dependedArtifacts = new HashMap<String, Map<String, List<String>>>();
 
 	private final Map<String, List<String>> artifactsByGroupId = new HashMap<String, List<String>>();
 
 	private final List<String> repos;
 
 	private final String restriction;
 
 	private final String username;
 
 	private final String password;
 
 	public static void main(final String[] args) throws Exception {
 		final AnalyserCLI cli = new AnalyserCLI();
 		cli.parse(args);
 
 		final String restriction = cli.getRestriction();
 		final List<String> repos = cli.getRepoPaths();
 		final String username = cli.getUsername();
 		final String password = cli.getPassword();
 
 		final Analyser analyser = new Analyser(repos, restriction, username, password);
 		analyser.analyse();
 	}
 
 	static Map<String, String> createNodesForArtifacts(final Map<String, List<String>> artifactsToCreate) {
 		final Map<String, String> nodeMap = new HashMap<String, String>();
 		int i = 0;
 		for (final String groupId : artifactsToCreate.keySet()) {
 			for (final String artifactId : artifactsToCreate.get(groupId)) {
 				nodeMap.put(groupId + ":" + artifactId, "Node_" + i++);
 			}
 		}
 		return nodeMap;
 	}
 
 	public Analyser(final List<String> repos, final String restriction, final String username, final String password) {
 		this.repos = repos;
 		this.restriction = restriction;
 		this.username = username;
 		this.password = password;
 	}
 
 	public void analyse() throws Exception {
 		System.out.println("Processing");
 
 		final RepoURLReader pomReader = new RepoURLReader(username, password);
 		final List<Model> poms = new ArrayList<Model>();
 
 		for (final String repo : repos) {
 			poms.addAll(pomReader.readPomsAt(repo, ""));
 		}
 
 		for (final Model pom : poms) {
 			final String groupId = (pom.getGroupId() == null) ? pom.getParent().getGroupId() : pom.getGroupId();
 			final String artifactId = pom.getArtifactId();
 
 			final List<Dependency> dependencies = pom.getDependencies();
 			for (final Dependency dependency : dependencies) {
 				final String depGroupId = dependency.getGroupId();
 				final String depArtifactId = dependency.getArtifactId();
 				final String depVersion = dependency.getVersion();
 
 				addDependencyBetweenAtrifacts(depGroupId, depArtifactId, depVersion, groupId, artifactId);
 			}
 		}
 
 		System.out.println("Processed " + poms.size() + " modules");
 
 		final DotGenerator graphGenerator = new DotGenerator();
 		graphGenerator.start(1000, 1000);
 		
 		// Write all the artifacts to the graph, this does not include links
 		graphGenerator.writeNodesToGraph(true, artifactsByGroupId);
 
 		// Get the list of dependencies seen
 		for (final String dependency : dependedArtifacts.keySet()) {
 			// Get the list of dependency versions
 			final Map<String, List<String>> dependedVersions = dependedArtifacts.get(dependency);
 			for (final String dependencyVersion : dependedVersions.keySet()) {
 				// TODO Figure out which is the latest version of a Dependency
 				final boolean outOfDate = false;
 				final int importance = (outOfDate) ? 10 : 1;
 				// Iterate over the modules that depend upon our current dependency version 
 				for (final String dependee : dependedVersions.get(dependencyVersion)) {
 					// Get the groupId and artifactId from our components
 					final String[] dependeeComponents = dependee.split(":");
 					final String[] dependencyComponents = dependency.split(":");
 					final String dependeeGroupId = dependeeComponents[0];
 					final String dependeeArtifactId = dependeeComponents[1];
 					final String dependencyGroupId = dependencyComponents[0];
 					final String dependencyArtifactId = dependencyComponents[1];
					if (dependee.startsWith(restriction) || dependency.startsWith(restriction)) {
						graphGenerator.linkNodesOnGraph(dependencyGroupId, dependencyArtifactId, dependeeGroupId, dependeeArtifactId, importance);
					}
 				}
 			}
 		}
 		System.out.println(graphGenerator.end());
 	}
 
 	private void addDependencyBetweenAtrifacts(final String depGroupId, final String depArtifactId, final String depVersion, String groupId, String artifactId) {
 		if (!depGroupId.startsWith(restriction)) {
 			return;
 		}
 
 		// Create the UUID for this artifact
 		final String refererUUID = groupId + ":" + artifactId;
 
 		// Create the UUID for this dependency
 		final String depUUID = depGroupId + ":" + depArtifactId;
 
 		// Add the artifact to our list of artifacts to create
 		addArtifactToList(groupId, artifactId);
 		addArtifactToList(depGroupId, depArtifactId);
 
 		if (!dependedArtifacts.containsKey(depUUID)) {
 			dependedArtifacts.put(depUUID, new HashMap<String, List<String>>());
 		}
 		final Map<String, List<String>> artifactVersions = dependedArtifacts.get(depUUID);
 		if (!artifactVersions.containsKey(depVersion)) {
 			artifactVersions.put(depVersion, new ArrayList<String>());
 		}
 		final List<String> referencingArtifacts = artifactVersions.get(depVersion);
 		referencingArtifacts.add(refererUUID);
 	}
 
 	private void addArtifactToList(final String groupId, final String artifactId) {
 		if (!groupId.startsWith(restriction)) {
 			return;
 		}
 		if (!artifactsByGroupId.containsKey(groupId)) {
 			artifactsByGroupId.put(groupId, new ArrayList<String>());
 		}
 		final List<String> list = artifactsByGroupId.get(groupId);
 		if (!list.contains(artifactId)) {
 			list.add(artifactId);
 		}
 	}
 }
