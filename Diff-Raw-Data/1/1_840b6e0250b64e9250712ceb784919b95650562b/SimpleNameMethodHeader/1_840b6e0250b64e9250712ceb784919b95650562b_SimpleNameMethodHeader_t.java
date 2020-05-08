 package chameleon.support.member.simplename;
 
 import chameleon.core.compilationunit.CompilationUnit;
 import chameleon.core.method.Method;
 import chameleon.core.method.MethodHeader;
 import chameleon.core.namespace.Namespace;
 import chameleon.core.namespacepart.NamespacePart;
 import chameleon.core.variable.FormalParameter;
 
 public class SimpleNameMethodHeader<E extends SimpleNameMethodHeader, P extends Method, S extends SimpleNameMethodSignature> extends MethodHeader<E, P, S>{
 
   public SimpleNameMethodHeader(String name) {
     setName(name);
   }
   
   public String getName() {
     return _name;
   }
   
   public void setName(String name) {
     _name = name;
   }
   
   private String _name;
 
 	@Override
 	public E cloneThis() {
 		SimpleNameMethodHeader result = new SimpleNameMethodHeader(getName());
 		for(FormalParameter param: getParameters()) {
 			result.addParameter(param.clone());
 		}
 		return (E) result;
 	}
 
 	@Override
 	public S signature() {
 		SimpleNameMethodSignature result =  new SimpleNameMethodSignature(getName());
		result.setUniParent(parent());
 		for(FormalParameter param: getParameters()) {
 			result.add(param.getTypeReference().clone());
 		}
 		return (S) result;
 	}
 
 	public CompilationUnit getCompilationUnit() {
 		return parent().getCompilationUnit();
 	}
 
 	public NamespacePart getNearestNamespacePart() {
 		return parent().getNearestNamespacePart();
 	}
 
 	public Namespace getNamespace() {
 		return parent().getNamespace();
 	}
 
 }
