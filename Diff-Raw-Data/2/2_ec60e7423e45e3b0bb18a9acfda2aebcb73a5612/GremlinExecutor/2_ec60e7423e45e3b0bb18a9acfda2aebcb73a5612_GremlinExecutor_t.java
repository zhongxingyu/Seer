 package org.umlg.runtime.adaptor;
 
 import com.tinkerpop.blueprints.Graph;
 import com.tinkerpop.blueprints.util.wrappers.readonly.ReadOnlyGraph;
 import com.tinkerpop.gremlin.groovy.Gremlin;
 import com.tinkerpop.pipes.Pipe;
 import com.tinkerpop.pipes.transform.ToStringPipe;
 import com.tinkerpop.pipes.util.iterators.SingleIterator;
 import groovy.lang.Binding;
 import groovy.lang.GroovyShell;
 import org.apache.commons.lang.time.StopWatch;
 import org.codehaus.groovy.control.CompilerConfiguration;
 
 /**
  * Date: 2013/06/09
  * Time: 8:34 PM
  */
 public class GremlinExecutor {
 
     /**
      * Executes a gremlin query. If the contextId is null then it is ignored.
      * If it is not null then all instances of the keywork "this" will be replaced with "g.v(contextId)"
      * @param contextId
      * @param gremlin
      * @return
      */
     public static String executeGremlinViaGroovy(Object contextId, String gremlin) {
         StopWatch stopWatch = new StopWatch();
         stopWatch.start();
 
         //remove uml namespacing
        gremlin = gremlin.replace("::", "_");
 
         if (contextId != null) {
             if (!(contextId instanceof Long)) {
                 gremlin = gremlin.replace("self", "g.v(\"" + contextId.toString() + "\")");
             } else {
                 gremlin = gremlin.replace("self", "g.v(" + contextId + ")");
             }
         }
         Graph graph = new ReadOnlyGraph(GraphDb.getDb());
         CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
         compilerConfiguration.setScriptBaseClass("org.umlg.runtime.adaptor.GremlinExecutorBaseClass");
         Binding binding = new Binding();
         binding.setVariable("g", graph);
         GroovyShell shell = new GroovyShell(binding, compilerConfiguration);
         Object pipe = shell.evaluate("return " + gremlin + ";");
         ToStringPipe toStringPipe = new ToStringPipe();
         toStringPipe.setStarts(new SingleIterator<Object>(pipe));
         StringBuilder result = new StringBuilder();
         while (toStringPipe.hasNext()) {
             result.append(toStringPipe.next());
             result.append("\n");
         }
         stopWatch.stop();
         result.append("Time to execute query = ");
         result.append(stopWatch.toString());
         return result.toString();
     }
 
 }
