 package sk.stuba.fiit.perconik.core.persistence.data;
 
 import java.lang.annotation.Annotation;
 import java.util.Set;
 import sk.stuba.fiit.perconik.core.Registrable;
 import sk.stuba.fiit.perconik.core.Registrables;
 import sk.stuba.fiit.perconik.core.persistence.AnnotableRegistration;
 import sk.stuba.fiit.perconik.utilities.reflect.annotation.Annotable;
 
 /**
  * An abstract implementation of the {@link AnnotableRegistration} interface.
  * 
  * @author Pavol Zbell
  * @since 1.0
  */
 abstract class AbstractAnnotableRegistration implements AnnotableRegistration
 {
	private transient volatile Annotable annotable;
 	
 	/**
 	 * Constructor for use by subclasses.
 	 */
 	protected AbstractAnnotableRegistration()
 	{
 	}
 
 	abstract Registrable source();
 	
 	private final Annotable annotable()
 	{
 		Annotable annotable = this.annotable;
 
 		if (annotable == null)
 		{
 			synchronized (this)
 			{
 				annotable = this.annotable;
 
 				if (annotable == null)
 				{
 					annotable = this.annotable = Registrables.toAnnotable(this.source().getClass());
 				}
 			}
 		}
 
 		return annotable;
 	}
 	
 	public final boolean hasAnnotation(final Class<? extends Annotation> type)
 	{
 		return this.annotable().hasAnnotation(type);
 	}
 
 	public final <A extends Annotation> A getAnnotation(final Class<A> type)
 	{
 		return this.annotable().getAnnotation(type);
 	}
 
 	public final Set<Annotation> getAnnotations()
 	{
 		return this.annotable().getAnnotations();
 	}
 }
