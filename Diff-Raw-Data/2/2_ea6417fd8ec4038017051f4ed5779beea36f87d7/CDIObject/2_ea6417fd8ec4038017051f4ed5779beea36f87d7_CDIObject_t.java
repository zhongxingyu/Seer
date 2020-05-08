 package net.sf.javagimmicks.cdi;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 /**
  * A base class for Java beans which can not be instantiated via CDI (e.g.
  * because they are instantiated via reflection by some given framework) but
  * need access to the CDI context.
  * <p>
  * Upon constructor call this class automatically performs non-constructor
  * injections and post-construct callbacks based on {@link Inject} and
 * {@link PostConstruct} annotations.
  * <p>
  * <b>Attention:</b> Requires CDI 1.1 or higher
  */
 public abstract class CDIObject
 {
    protected CDIObject()
    {
       CDIContext.illuminate(this);
    }
 }
