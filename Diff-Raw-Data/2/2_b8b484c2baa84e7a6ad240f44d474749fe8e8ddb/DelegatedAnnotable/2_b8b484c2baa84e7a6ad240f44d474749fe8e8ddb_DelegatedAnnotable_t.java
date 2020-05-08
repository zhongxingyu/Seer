 package sk.stuba.fiit.perconik.utilities.reflect.annotation;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.AnnotatedElement;
 import java.util.Set;
 import com.google.common.base.Preconditions;
 
 final class DelegatedAnnotable implements Annotable
 {
 	private final AnnotatedElement element;
 	
 	DelegatedAnnotable(AnnotatedElement element)
 	{
 		this.element = Preconditions.checkNotNull(element);
 	}
 
 	public final boolean hasAnnotation(Class<? extends Annotation> type)
 	{
 		return this.element.isAnnotationPresent(type);
 	}
 
 	public final <A extends Annotation> A getAnnotation(Class<A> type)
 	{
		return this.element.getAnnotation(type);
 	}
 
 	public final Set<Annotation> getAnnotations()
 	{
 		return Annotations.ofElement(this.element);
 	}
 }
