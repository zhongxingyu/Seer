 package megal.model;
 
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
 import java.util.List;
 import java.util.LinkedList;
import java.util.Set;
 
 public class Model {
 	private List<Decl> decls;	
 	public List<Decl> getDecls() {
 		if (decls==null)
 			decls = new LinkedList<Decl>();
 		return decls;
 	}
 	
 	public void resolve(String entity, Object resource) {
 		EDecl e = this.getEDecl(entity);
 		e.getEntity().resolve(resource);
 	}
 	
 	public EDecl getEDecl(String name) {
 		for (Decl decl : decls){
 			EDecl edecl = (EDecl)decl;
 			if (edecl.getName() == name){
 				return edecl;
 			}		
 		}
 		
 		throw new IllegalArgumentException("Entity with the name " + name + "doesnt' exist");
 	}
 }
