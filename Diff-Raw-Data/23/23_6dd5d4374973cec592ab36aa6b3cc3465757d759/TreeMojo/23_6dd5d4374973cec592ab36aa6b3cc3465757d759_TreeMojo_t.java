 package de.mytoys.maven.plugins.debug;
 
 /*
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 import java.io.Writer;
 
 import org.apache.maven.plugin.dependency.treeSerializers.GraphmlDependencyNodeVisitor;
 import org.apache.maven.plugin.dependency.treeSerializers.TGFDependencyNodeVisitor;
 import org.apache.maven.plugin.dependency.treeSerializers.DOTDependencyNodeVisitor;
 import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;
 import org.apache.maven.shared.dependency.tree.traversal.SerializingDependencyNodeVisitor;
 
 /**
  * Displays the dependency tree for this project without requiring conflicts to be resolved
  * first.
  *
  * @author <a href="mailto:leonard.ehrenfried@web.de">Leonard Ehrenfried</a>
  * @goal tree
  */
 public class TreeMojo extends AbstractDebugMojo {
 	
 	/**
 	 * If specified, this parameter will cause the dependency tree to be written using the specified format. Currently
 	 * supported format are text, dot, graphml and tgf.
 	 *
 	 * These formats can be plotted to image files. An example of how to plot a dot file using
 	 * pygraphviz can be found <a href="http://networkx.lanl.gov/pygraphviz/tutorial.html#layout-and-drawing">here</a>
 	 *
 	 * @parameter expression="${outputType}" default-value="text"
 	 * @since 2.1
 	 */
 	protected String outputType;
 	
 	public DependencyNodeVisitor getSerializingDependencyNodeVisitor(Writer writer) {
 		if ("graphml".equals(outputType)) {
 			return new GraphmlDependencyNodeVisitor(writer);
 		} else if ("tgf".equals(outputType)) {
 			return new TGFDependencyNodeVisitor(writer);
 		} else if ("dot".equals(outputType)) {
 			return new DOTDependencyNodeVisitor(writer);
 		} else {
 			return new SerializingDependencyNodeVisitor(writer, toTreeTokens(tokens));
 		}
 	}
 
 	@Override
	protected void postprocessResult() {}
 }
