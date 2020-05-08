 /*
  * Created on 09-Jan-2006
  */
 package uk.org.ponder.beanutil;
 
 /** A tag interface implemented by a BeanLocator indicating that it 
 * puts itself forward as a "fallback" resolver of bean requests. If 
 * a bean definition is not found in the main request container, all
 * bean of type "FallbackBeanLocator" will be queried in turn for a bean
 * of the required name. Their behaviour for a not found bean will be to
 * return <code>null</code> rather than throw a BeanDefinitionNotFound exception.
 * <p>The first FallbackBeanLocator that returns an object will terminate the
 * search. Otherwise, location will continue in the parent locator.
 * <p>NB - this mechanism will probably be replaced once we support "proper"
 * Spring parent containers.
  * @author Antranig Basman (amb26@ponder.org.uk)
  *
  */
 
public interface FallbackBeanLocator extends BeanLocator {
 
 }
