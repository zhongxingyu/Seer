 // Written by William Cook, Ben Wiedermann, Ali Ibrahim
 // The University of Texas at Austin, Department of Computer Science
 // See LICENSE.txt for license information
 package batch.syntax;
 
 import java.util.Map;
 
 import batch.util.ForestReader;
 import batch.util.ForestWriter;
 
 public class FunClosure<E extends Evaluate> {
 	String var;
 	Map<String, Object> env;
 	E body;
 	ForestReader inputs;
 
 	public FunClosure(String var, E body, Map<String, Object> env, ForestReader inputs) {
 		this.var = var;
 		this.body = body;
 		this.env = env;
 		this.inputs = inputs;
 	}
 
 	public Object apply(Object arg, ForestWriter results) {
 		env.put(var, arg);
 		return body.evaluate(env, inputs, results);
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
		sb.append("CLOSURE(");
 		sb.append(var);
 		sb.append(")");
 		return sb.toString();
 	}
 }
