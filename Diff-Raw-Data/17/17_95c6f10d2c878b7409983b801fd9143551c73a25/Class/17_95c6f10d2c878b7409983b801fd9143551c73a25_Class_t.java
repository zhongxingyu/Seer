 package uk.ac.ic.doc.gander.model;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.python.pydev.parser.jython.SimpleNode;
 import org.python.pydev.parser.jython.ast.ClassDef;
 import org.python.pydev.parser.jython.ast.NameTok;
 import org.python.pydev.parser.jython.ast.exprType;
 
 public class Class implements Namespace {
 
 	private Map<String, Function> methods = new HashMap<String, Function>();
 	private Map<String, Class> classes = new HashMap<String, Class>();
 
 	private ClassDef cls;
 	private Namespace parent;
 
 	public Class(ClassDef cls, Namespace parent) {
 		this.cls = cls;
 		this.parent = parent;
 	}
 
 	public exprType[] inheritsFrom() {
 		return cls.bases;
 	}
 
 	public String getName() {
 		return ((NameTok) (cls.name)).id;
 	}
 
 	public String getFullName() {
		String parentName = parent.getFullName();
		if (parentName.isEmpty())
			return getName();
		else
			return parentName + "." + getName();
 	}
 
 	public ClassDef getClassDef() {
 		return cls;
 	}
 
 	public Namespace getParentScope() {
 		return parent;
 	}
 
 	public Map<String, Package> getPackages() {
 		return Collections.emptyMap();
 	}
 
 	public Map<String, Module> getModules() {
 		return Collections.emptyMap();
 	}
 
 	public Map<String, Class> getClasses() {
 		return Collections.unmodifiableMap(classes);
 	}
 
 	public Map<String, Function> getFunctions() {
 		return Collections.unmodifiableMap(methods);
 	}
 
 	public void addPackage(Package pkg) {
 		throw new Error("A class cannot contain a package");
 	}
 
 	public void addModule(Module module) {
 		throw new Error("A class cannot contain a package");
 	}
 
 	public void addClass(Class klass) {
 		classes.put(klass.getName(), klass);
 	}
 
 	public void addFunction(Function function) {
 		methods.put(function.getName(), function);
 	}
 
 	@Override
 	public String toString() {
 		return "Class[" + getFullName() + "]";
 	}
 
 	/**
 	 * Classes inherit their systemness from their parent.
 	 * 
 	 * It isn't possible for a class to be system if it's containing module
 	 * isn't a system module. In other words, the resolution of systemness is at
 	 * the module level and all namespaces below that, inherit from their
 	 * parent.
 	 * 
 	 * XXX: Another way to look at this is that systemness is a property of the
 	 * associated <b>hierarchy</b> element so perhaps we should link model
 	 * element to their hierarchy parent. However, some model elements don't
 	 * have a hierarchy element. For example the dummy_builtin module.
 	 */
 	public boolean isSystem() {
 		return parent.isSystem();
 	}
 
 	public SimpleNode getAst() {
 		return getClassDef();
 	}
 }
