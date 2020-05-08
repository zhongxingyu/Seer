 /* 
  * Created on 11-03-2013 19:40:09 by Andrzej Ludwikowski
  */
 
 package fluentbuilder.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.lang.model.element.AnnotationMirror;
 import javax.lang.model.element.Element;
 
 
 public class TypeUtils {
 
 	public static boolean containsAnnotation(Element element, String... annotations) {
 		assert element != null;
 		assert annotations != null;
 
 		List<String> annotationClassNames = new ArrayList<String>();
 		Collections.addAll(annotationClassNames, annotations);
 
		return containsAnnotation(element, annotationClassNames);
	}

	public static boolean containsAnnotation(Element element, Collection<String> annotationClassNames) {

 		List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
 		for (AnnotationMirror mirror : annotationMirrors) {
 			if (annotationClassNames.contains(mirror.getAnnotationType().toString())) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
