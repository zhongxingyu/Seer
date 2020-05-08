 package org.jmlspecs.jml4.boogie.ast;
 
 import java.util.ArrayList;
 
 import org.eclipse.jdt.internal.compiler.ast.ASTNode;
 import org.jmlspecs.jml4.boogie.BoogieSource;
 
 public class MapTypeReference extends TypeReference {
 	private ArrayList/*<TypeReference>*/ mapTypes;
 	
 	public MapTypeReference(String name, TypeReference[] mapTypes, ASTNode javaNode, Scope scope) {
 		super(name, javaNode, scope);
 		this.mapTypes = new ArrayList();
 		if (mapTypes != null) {
 			for (int i = 0; i < mapTypes.length; i++) {
 				this.mapTypes.add(mapTypes[i]);
 			}
 		}
 	}
 	
 	public ArrayList getMapTypes() { 
 		return mapTypes;
 	}
 
 	public void toBuffer(BoogieSource out) {
 		for (int i = 0; i < getMapTypes().size(); i++) {
 			out.append(TOKEN_LBRACK);
 			((TypeReference)getMapTypes().get(i)).toBuffer(out);
 			out.append(TOKEN_RBRACK + TOKEN_SPACE);
 		}
 		super.toBuffer(out);
 	}
 }
