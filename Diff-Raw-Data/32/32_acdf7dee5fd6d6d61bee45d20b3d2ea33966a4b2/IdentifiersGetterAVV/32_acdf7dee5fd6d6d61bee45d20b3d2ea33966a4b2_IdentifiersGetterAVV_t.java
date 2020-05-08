 package be.ac.ua.visitors;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.lang.model.element.AnnotationMirror;
 import javax.lang.model.element.AnnotationValue;
 import javax.lang.model.element.AnnotationValueVisitor;
 import javax.lang.model.element.VariableElement;
 import javax.lang.model.type.TypeMirror;
 
 public class IdentifiersGetterAVV implements AnnotationValueVisitor<List<String>, Void> {
 
 	@Override
 	public List<String> visit(AnnotationValue av, Void p) {
 		return null;
 	}
 
 	@Override
 	public List<String> visit(AnnotationValue av) {
 		return null;
 	}
 
 	@Override
 	public List<String> visitBoolean(boolean b, Void p) {
 		return null;
 	}
 
 	@Override
 	public List<String> visitByte(byte b, Void p) {
 		return null;
 	}
 
 	@Override
 	public List<String> visitChar(char c, Void p) {
 		return null;
 	}
 
 	@Override
 	public List<String> visitDouble(double d, Void p) {
 		return null;
 	}
 
 	@Override
 	public List<String> visitFloat(float f, Void p) {
 		return null;
 	}
 
 	@Override
 	public List<String> visitInt(int i, Void p) {
 		return null;
 	}
 
 	@Override
 	public List<String> visitLong(long i, Void p) {
 		return null;
 	}
 
 	@Override
 	public List<String> visitShort(short s, Void p) {
 		return null;
 	}
 
 	@Override
 	public List<String> visitString(String s, Void p) {
 		final String discardRegex = "\\W+";
		System.out.println(Arrays.toString(s.replaceFirst(discardRegex, "")
						.split(discardRegex)));
 		return new ArrayList<String>(Arrays
 				.asList(s.replaceFirst(discardRegex, "")
 						.split(discardRegex)));
 	}
 
 	@Override
 	public List<String> visitType(TypeMirror t, Void p) {
 		return null;
 	}
 
 	@Override
 	public List<String> visitEnumConstant(VariableElement c, Void p) {
 		return null;
 	}
 
 	@Override
 	public List<String> visitAnnotation(AnnotationMirror a, Void p) {
 		return null;
 	}
 
 	@Override
 	public List<String> visitArray(List<? extends AnnotationValue> vals, Void p) {
 		return null;
 	}
 
 	@Override
 	public List<String> visitUnknown(AnnotationValue av, Void p) {
 		return null;
 	}
 
 }
