 package fr.univ.lille1;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 import fr.univ.lille1.CodeSearchEngine.TypeKind;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			final Document doc = Jsoup.parse(new File("commons-collections.xml"), "UTF-8");
 			final CodeSearchEngine cse = new CodeSearchEngineImpl(doc);
 //			System.out.println(cse.findType("int"));
 //			System.out.println(cse.findSubTypesOf("AbstractAction"));
 //			System.out.println(cse.findFieldsTypedWith("Integer"));
 //			System.out.println(cse.findMethodsOf("Action"));
 //			System.out.println(cse.findMethodsReturning("int"));
 //			System.out.println(cse.findMethodsTakingAsParameter("String").size());
 //			System.out.println(cse.findMethodsCalled("nextToken").size());
 //			List<Field> field = cse.findFieldsTypedWith("ImageDescriptor");
 //			for (Field field2 : field) {
 //				cse.findAllReadAccessesOf(field2);
 //			}
 //			System.out.println(cse.findNewOf("GC"));
 //			System.out.println(cse.findCastsTo("String"));
 //			System.out.println(cse.findInstanceOf("TreePath"));
 //			System.out.println(cse.findMethodsThrowing("InterruptedException"));
 //			System.out.println(cse.findCastsTo("IllegalArgumentException"));
 //			System.out.println(cse.findMethodsCalled("iterator"));
 			System.out.println(cse.findOverridingMethodsOf(cse.findMethodsCalled("equals").get(0)));
 //			System.out.println(cse.findAllReadAccessesOf(new FieldImpl(new TypeImpl("IContributionManager", "org.eclipse.jface.action", TypeKind.CLASS, null), "parent")));
//			System.out.println(cse.findAllWriteAccessesOf(new FieldImpl(new TypeImpl("IContributionManager", "org.eclipse.jface.action", TypeKind.CLASS, null), "parent")));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
