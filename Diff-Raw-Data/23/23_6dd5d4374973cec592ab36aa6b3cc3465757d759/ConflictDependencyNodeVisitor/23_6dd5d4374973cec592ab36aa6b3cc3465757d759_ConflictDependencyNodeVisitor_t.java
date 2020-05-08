 package de.mytoys.maven.plugins.debug;
 
 /*
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import com.google.common.collect.LinkedHashMultimap;
 import com.google.common.collect.SetMultimap;
 import java.io.Writer;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Stack;
 import org.apache.maven.shared.dependency.tree.DependencyNode;
 import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;
 
 /**
  *
  * @author <a href="mailto:leonard.ehrenfried@web.de">Leonard Ehrenfried</a>
  */
 public class ConflictDependencyNodeVisitor implements DependencyNodeVisitor {
 
 	SetMultimap<String, DependencyNode> dependencies = LinkedHashMultimap.create();
 	Writer writer;
 	boolean conflictsFound = false;
 
 	public ConflictDependencyNodeVisitor(Writer writer) {
 		if (writer != null) {
 			throw new UnsupportedOperationException("this visitor does not use the writer, please call #getConflictInfo() after #visit()");
 		}
 	}
 
 	public boolean visit(DependencyNode dn) {
 		String ga = dn.getArtifact().getGroupId() + ':' + dn.getArtifact().getArtifactId();
 		dependencies.put(ga, dn);
 		return true;
 	}
 
 	public boolean endVisit(DependencyNode dn) {
 		return true;
 	}
 
 	public String getConflictInfo() {
 		StringBuilder buf = new StringBuilder();
 		for (String key : dependencies.keySet()) {
 			if (getDifferingVersions(key) > 1) {
 				//Überschrift
 				buf.append('\n');
 				buf.append('\n');
 				buf.append("=== ");
 				buf.append(key);
 				buf.append(" ===");
 				buf.append('\n');
 
 				for (DependencyNode dn : dependencies.get(key)) {
 					//einzelne Artefakte
 					buf.append(inheritanceToString(dn));
 				}
 				conflictsFound = true;
 			}
 		}
 		return buf.toString();
 	}
 
	public boolean isConflictsFound() {
 		return conflictsFound;
 	}
 
 	/**
 	 * Prüft wie viele verschiedene Versionen das Artefakt mit key im Dependency Tree hat.
 	 * @param key
 	 * @return 
 	 */
 	private int getDifferingVersions(String key) {
 		Set<String> versions = new HashSet();
 		for (DependencyNode dn : dependencies.get(key)) {
 			versions.add(dn.getArtifact().getVersion());
 		}
 
 		return versions.size();
 	}
 
 	private static String inheritanceToString(DependencyNode dn) {
 		Stack<DependencyNode> parents = new Stack<DependencyNode>();
 		StringBuilder buf = new StringBuilder();
 		
 		DependencyNode i=dn;
 		while(i.getParent() != null && i.getParent().getParent()!=null) {
 			i = i.getParent();
 			parents.add(i);
 		}
 
 		while(!parents.isEmpty()) {
 			DependencyNode j=parents.pop();
 			buf.append(j.getArtifact().getArtifactId());
 			buf.append(':');
 			buf.append(j.getArtifact().getVersion());
 			buf.append(" -> ");
 		}
 
 		buf.append(dn.getArtifact().getArtifactId());
 		buf.append(':');
 		buf.append(dn.getArtifact().getVersionRange());
 		buf.append('\n');
 		
 		return buf.toString();
 	}
 }
