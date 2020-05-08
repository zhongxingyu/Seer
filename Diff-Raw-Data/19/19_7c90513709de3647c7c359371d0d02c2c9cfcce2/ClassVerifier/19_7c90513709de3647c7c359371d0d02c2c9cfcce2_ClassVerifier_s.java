 /* 
  * Created on 11-03-2013 19:42:45 by Andrzej Ludwikowski
  */
 
 package fluentbuilder.processor;
 
 import javax.lang.model.element.Element;
 
import fluentbuilder.util.TypeUtils;

 
 public class ClassVerifier {
 
 	private final ProcessorContext context;
 
 
 	public ClassVerifier(ProcessorContext context) {
 		this.context = context;
 	}
 
 	public boolean shouldGenerateBuilder(Element element) {
 
 		if (context.isAcceptJavaPersisentceAnnotations()){
 			
				TypeUtils.containsAnnotation(element, annotations)
 		}
 		
 		
 		return false;
 	}
 
 }
