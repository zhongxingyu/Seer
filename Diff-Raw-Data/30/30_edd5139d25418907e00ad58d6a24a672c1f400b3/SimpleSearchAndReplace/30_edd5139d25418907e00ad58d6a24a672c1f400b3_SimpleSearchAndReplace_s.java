 package net.thomasnardone.utils.testtools;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.thomasnardone.utils.gen.SearchAndReplaceGenerator;
 
 public class SimpleSearchAndReplace extends SearchAndReplaceGenerator {
 	private final boolean	preserveAnnotations;
 
 	public SimpleSearchAndReplace(final String className, final String packageName, final boolean preserveAnnotations)
 			throws ClassNotFoundException {
 		super(Object.class.getName(), "gen");
 		this.preserveAnnotations = preserveAnnotations;
 	}
 
 	@Override
 	protected void generateStuff() throws IOException {
 		replaceMethod("method1", getMethod("method1"), preserveAnnotations);
 		replaceMethod("method2", getMethod("method2"), preserveAnnotations);
 		replaceMethod("method3", getMethod("method3"), preserveAnnotations);
 		replaceMethod("method4", getMethod("method4"), preserveAnnotations);
 		replaceMethod("method5", getMethod("method5"), preserveAnnotations);
 		replaceMethod("method6", getMethod("method6"), preserveAnnotations);
 		replaceMethod("method7", getMethod("method7"), preserveAnnotations);
 	}
 
 	@Override
 	protected String getName() {
 		return "SearchAndReplaceClass";
 	}
 
 	@Override
 	protected String getSourcePath() {
 		return "tests/res/";
 	}
 
 	private List<String> getMethod(final String methodName) {
 		List<String> method = new LinkedList<String>();
 		method.add("public void " + methodName + "() {");
 		method.add("System.out.println(\"Change successful!\");");
 		method.add("}");
 		return method;
 	}
 }
