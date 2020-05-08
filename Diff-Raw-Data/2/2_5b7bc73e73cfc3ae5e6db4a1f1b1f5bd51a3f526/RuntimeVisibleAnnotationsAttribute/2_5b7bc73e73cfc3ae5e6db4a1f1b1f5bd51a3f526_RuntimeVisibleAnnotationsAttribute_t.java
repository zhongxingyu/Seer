 package org.jackie.java5.annotation.impl;
 
 import org.objectweb.asm.Attribute;
 import org.objectweb.asm.tree.AnnotationNode;
 import org.jackie.jvm.attribute.JAttribute;
 
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * @author Patrik Beno
  */
 public class RuntimeVisibleAnnotationsAttribute extends Attribute implements JAttribute {
 
	static public final String NAME = "RuntimeVisibleAnnotations"; 
 
 	protected List<AnnotationNode> annos;
 
 	public RuntimeVisibleAnnotationsAttribute() {
 		super(NAME);
 	}
 
 	public String getName() {
 		return type;
 	}
 
 	public void add(AnnotationNode anno) {
 		if (annos == null) {
 			annos = new ArrayList<AnnotationNode>();
 		}
 		annos.add(anno);
 	}
 
 	public List<AnnotationNode> getAnnotationNodes() {
 		return annos;
 	}
 }
