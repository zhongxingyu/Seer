 /*
  * Created on 09-Jan-2006
  */
 package uk.org.ponder.beanutil;
 
 /** A tag interface implemented by a BeanLocator indicating that it 
 * puts itself forward as a "first-line" resolver of bean requests. If 
 * the BeanLocator returns null to a request, location will fall back to 
 * the original locator.
  * @author Antranig Basman (amb26@ponder.org.uk)
  *
  */
 
public interface FallbackBeanLocator {
 
 }
