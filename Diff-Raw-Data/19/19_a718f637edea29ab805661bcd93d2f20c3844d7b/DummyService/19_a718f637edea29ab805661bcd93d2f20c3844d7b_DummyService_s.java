 package test.org.charleech.arq.eval.cen.intrcptr.service;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.jws.WebService;
 
import org.charleech.arq.eval.cen.intrcptr.ftprnt.FootprintInterceptable;
import org.charleech.arq.eval.cen.intrcptr.prfrmnc.PerformanceInterceptable;
 
 import test.org.charleech.arq.eval.cen.intrcptr.local.DummyManageable;
 import test.org.charleech.arq.eval.cen.intrcptr.util.DummyUtil;
 
 /**
  * <p>
  * This is a concrete implementing class which provides the feature described
  * at {@link DummyServiceable}.
  * </p>
  *
  * @author charlee.ch
  * @version 0.0.1
  * @see DummyServiceable
  * @see <a rel="license"
  *      href="http://creativecommons.org/licenses/by-nc-sa/3.0/"><img
  *      alt="Creative Commons License" style="border-width:0"
  *      src="http://i.creativecommons.org/l/by-nc-sa/3.0/88x31.png" /></a><br />
  *      <span xmlns:dct="http://purl.org/dc/terms/"
  *      property="dct:title">Charlee@GitHub</span> by <a
  *      xmlns:cc="http://creativecommons.org/ns#"
  *      href="https://github.com/charleech" property="cc:attributionName"
  *      rel="cc:attributionURL">Charlee Chitsuk</a> is licensed under a <a
  *      rel="license"
  *      href="http://creativecommons.org/licenses/by-nc-sa/3.0/">Creative
  *      Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License</a>.
  */
 @Named
@FootprintInterceptable
@PerformanceInterceptable
 @Stateless
 @WebService(
    endpointInterface =
       "test.org.charleech.arq.eval.cen.intrcptr.service.DummyServiceable",
    serviceName       = "engine/ejb/DummyService",
    portName          = "DummyPort"
 )
 public class DummyService implements DummyServiceable {
 
     /**
      * This is the {@link DummyManageable} instance as the injected local
      * service.
      *
      * @since 0.0.1
      */
     @Inject
     private DummyManageable dummyManager;
 
     /**
      * This is the {@link DummyUtil} instance as the injected utility.
      *
      * @since 0.0.1
      */
     @Inject
     private DummyUtil util;
 
     @Override
     public String greet(final String name) {
         String result = null;
         this.util.dummyUtil();
         this.dummyManager.doProcess();
         result = "Hello " + name;
         return result;
     }
 }
