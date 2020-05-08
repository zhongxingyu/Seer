 package amstin.tools;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.lang.reflect.Field;
 import java.util.IdentityHashMap;
 import java.util.List;
 
 import amstin.tools.utils.Labeling;
 
 @SuppressWarnings("unchecked")
 public class ModelToDot {
 
 	public static void main(String[] args) throws IOException {
 		Object o = amstin.models.meta.Boot.instance;
 		PrintWriter w = new PrintWriter(System.out);
 		toDot(o, w);
 		w.flush();
 
 		
 		FileWriter f = new FileWriter(new File("meta.dot"));
 		toDot(o, f);
 		f.close();
 		
 		o = amstin.models.grammar.Boot.instance;
 		f = new FileWriter(new File("grammar.dot"));
 		toDot(o, f);
 		f.close();
 	}
 	
 	public static void toDot(Object obj, Writer output) {
 		ModelToDot inst = new ModelToDot(obj, output);
 		try {
 			inst.todot();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private Object root;
 	private Writer output;
 	private IdentityHashMap<Object, Integer> labels;
 	private int primId;
 
 
 	private ModelToDot(Object obj, Writer output) {
 		this.root = obj;
 		this.output = output;
 		this.labels = Labeling.label(root);
 		this.primId = 0;
 	}
 
 	private void todot() throws IOException {
 		header();
 		nodes();
 		edges();
 		footer();
 	}
 	
 	private void edges() throws IOException {
 		for (Object o: labels.keySet()) {
 			edges(o);
 		}
 	}
 
 	private void edges(Object o) throws IOException {
 		Class<?> klass = o.getClass();
 		for (Field f: klass.getFields()) {
 			try {
 				Object kid = f.get(o);
 				String name = f.getName();
 				
 				// TODO: what if a List contains str/int/etc. ?
 				if (isPrimitive(kid)) {
 					continue;
 				}
 				
 				if (kid instanceof List) {
 					List l = (List)kid;
 					for (int i = 0; i < l.size(); i++) {
 						Object elt = l.get(i);
 						String label = name + "@" + i;
 						if (isPrimitive(elt)) {
 							writeEdgeToPrimitive(o, elt, label);
 						}
 						else {
 							writeEdge(o, elt, label);
 						}
 					}
 				}
 				else if (kid != null) {
 					writeEdge(o, kid, name);
 				}
 				
 			} catch (IllegalArgumentException e) {
 				throw new AssertionError(e);
 			} catch (IllegalAccessException e) {
 				throw new AssertionError(e);
 			}
 		}
 		
 	}
 
 	private void writeEdgeToPrimitive(Object o, Object elt, String label) throws IOException {
 		String id = "primNode" + primId++;
		output.write(id + " [shape=plaintext, label=\"" + dotEscape(elt.toString()) + "\"]\n");		
		output.write(nodeId(o) + " -> " + id + " [label=\"" + dotEscape(label) + "\"]\n");		
 	}
 
 	private void writeEdge(Object o, Object elt, String label) throws IOException {
		output.write(nodeId(o) + " -> " + nodeId(elt) + " [label=\"" + dotEscape(label) + "\"]\n");
 	}
 
 	private void nodes() throws IOException {
 		for (Object o: labels.keySet()) {
 			node(o);
 		}
 	}
 
 	private void node(Object o) throws IOException {
 		output.write(nodeId(o) + " [label=\"" + makeLabel(o) + "\"]\n");
 	}
 	
 	private String nodeId(Object o) {
 		return "node_" + labels.get(o);
 	}
 
 	private String makeLabel(Object o) {
 		Class<?> klass = o.getClass();
 		String label = "";
 		for (Field f: klass.getFields()) {
 			try {
 				Object kid = f.get(o);
 				String name = f.getName();
 				if (isPrimitive(kid)) {
 					label += "|" + name + " = " + dotEscape(kid.toString());
 				}
 			} catch (IllegalArgumentException e) {
 				throw new AssertionError(e);
 			} catch (IllegalAccessException e) {
 				throw new AssertionError(e);
 			}
 		}
 		label = "{" + labels.get(o) + ": " + klass.getSimpleName() + label + "}";
 		return label;
 	}
 
 	private static boolean isPrimitive(Object kid) {
 		return kid instanceof String || kid instanceof Boolean || kid instanceof Integer || kid instanceof Double;
 	}
 
 	private static String dotEscape(String string) {
 		return string
		.replaceAll("\\|", "\\\\|")
		.replaceAll("\n", "\\n")
		.replaceAll("\t", "\\t")
		.replaceAll("\f", "\\f")
		.replaceAll("\r", "\\r")
		.replaceAll("\"", "\\\"")
		.replaceAll("\\}", "\\\\}")
		.replaceAll("\\{", "\\\\{") // these two don't work???
		.replaceAll("<", "\\\\<")
		.replaceAll(">", "\\\\>");
 	}
 
 	
 
 	private void header() throws IOException {
 		output.write("digraph aGraph {\n");
 		output.write("node [shape=Mrecord]\n");
 	}
 
 	private void footer() throws IOException {
 		output.write("}\n");
 	}
 
 
 }
