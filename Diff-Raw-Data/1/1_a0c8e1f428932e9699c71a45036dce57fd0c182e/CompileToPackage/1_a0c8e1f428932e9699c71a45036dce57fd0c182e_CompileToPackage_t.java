 package testutils.utils;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import testutils.javaparser.PTJavaParser;
 import AST.ClassDecl;
 import AST.CompilationUnit;
 import AST.ImportDecl;
 import AST.PTPackage;
 import AST.InterfaceDecl;
 import AST.PTEnumDecl;
 
 /* This is the file that ultimately rewrites PT-code to Java code.
    The package name is misleading, this is not a "testutil".
 
    The rewriting is partially done with string replacements, which
    seems inelegant and surely must lead to some edge cases.
    We should aim to replace this with printing by traversing the
    AST eventually, but for now this should do.
 
    (Fun TODO that will lead to depressing hard-to-fix tests: write
    some tests that break because we do raw string replacements.)
 */
 
 public class CompileToPackage extends PTFrontend {
 
 	private Map<String,List<String>> packageNameToClassNames;
 	private Map<String, String> source;
 	private List<String> sourceWithErrors;
 
 	/** Use default JavaParser */
 	public CompileToPackage(String[] filenames) {
 		super(filenames, new PTJavaParser());
 		packageNameToClassNames = new HashMap<String, List<String>>();
 		source = new HashMap<String, String>();
 		sourceWithErrors = new LinkedList<String>();
 	}
 
 	public Iterable<String> getPackageNames() {
 		return packageNameToClassNames.keySet();
 	}
 	
 	@Override
 	protected void processErrors(Collection errors, CompilationUnit unit) {
 		super.processErrors(errors, unit);
 		sourceWithErrors.add(unit.toString());
 	}
 
 	@Override
 	protected void processNoErrors(CompilationUnit unit) {
 		super.processNoErrors(unit);
 		Collection<PTPackage> localPackages = unit.getPTPackages();
 		for (PTPackage p : localPackages) {
 			List<String> classNames = new LinkedList<String>();
                 // actually now not just classes, but everything
                 // that should (or at least might) go in its own file.
 
 			for (PTEnumDecl pted : p.getPTEnumDeclList()) {
                 classNames.add( pted.getID() );
 				StringBuffer source = new StringBuffer();
 				source.append(String.format("package %s;\n\n",p.getID()));
 				for (ImportDecl id : unit.getImportDeclList())
 					source.append(id.toString() + "\n");
 				source.append(pted.toString() + "\n");
 				addSource(p.getID(), pted.getID(), source.toString());
             }
 
 			for (InterfaceDecl ptid : p.getInterfaceDeclList()) {
                 classNames.add( ptid.getID() );
 				StringBuffer source = new StringBuffer();
 				source.append(String.format("package %s;\n\n",p.getID()));
 				for (ImportDecl id : unit.getImportDeclList())
 					source.append(id.toString() + "\n");
 				source.append(ptid.toString() + "\n");
 				addSource(p.getID(), ptid.getID(), source.toString());
             }
 
 			for (ClassDecl c : p.getClassList()) {
 				classNames.add(c.getID());
 				StringBuffer source = new StringBuffer();
 				source.append(String.format("package %s;\n\n",p.getID()));
 				for (ImportDecl id : unit.getImportDeclList())
 					source.append(id.toString() + "\n");
 				source.append(c.toString() + "\n");
 				addSource(p.getID(), c.getID(), source.toString());
 			}
 			packageNameToClassNames.put(p.getID(),classNames);
 		}
 	}
 
 	private void addSource(String packagename, String classname, String classSource) {
 		source.put(createKey(packagename,classname),classSource);
 	}
 	
 	private String createKey(String packagename, String classname) {
 		return packagename + "$" + classname;
 	}
 
 	public Iterable<String> getClassnames(String packageName) {
 		return packageNameToClassNames.get(packageName);
 	}
 
 	public String getCompilableClassData(String packageName, String classname) {
 		String ptCode = source.get(createKey(packageName,classname));
 		String javaCode = makeJavaCompilable(ptCode);
 		return javaCode;
 	}
 
 	/**
 	 * Replaces every:
 	 * tsuper[<classname>]() with tsuper$<classname>$()
 	 * tsuper[<templatename.classname>] with tsuper$<templatename$classname>$()
 	 * tsuper[<classname>].f() with tsuper$<classname>$f()
 	 * tsuper[<templatename.classname>].f() with tsuper$<templatename$classname>$f()
 	 * tsuper[<templatepath.templatename.classname>].f() with tsuper$<templatepath$templatename$classname>$f()
 	 *   where template path is <directory>(.<directory>)*
 	 */
 	private String makeJavaCompilable(String ptCode) {
         String reg = "tsuper";
         Pattern p = Pattern.compile(reg);
         String[] strings = p.split(ptCode);
         StringBuilder sb = new StringBuilder(strings[0]);
         for (int i = 1; i < strings.length; ++i) {
             String str = strings[i];
             if (str.charAt(0) != '[') {
                sb.append("tsuper");
                 sb.append(str);
                 continue;
             }
             sb.append("tsuper");
             int j = 0;
             for ( ; j < str.length() - 1; ++j) {
                 char c = str.charAt(j);
                 if (c == '[' || c == '.')
                     sb.append('$');
                 else if (c == ']') {
                     sb.append('$');
                     break;
                 }
                 else
                     sb.append(c);
             }
             if (str.charAt(++j) == '.')
                 ++j;
             sb.append(str.substring(j));
         }
         return sb.toString();
     }
 
 	public List<String> getSourceWithErrors() {
 		return sourceWithErrors;
 	}
 }
