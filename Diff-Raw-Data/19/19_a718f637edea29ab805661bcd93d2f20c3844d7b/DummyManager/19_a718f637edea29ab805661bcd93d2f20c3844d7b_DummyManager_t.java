 package test.org.charleech.arq.eval.cen.intrcptr.local;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import lombok.extern.slf4j.Slf4j;
 
import org.charleech.arq.eval.cen.intrcptr.CentralizedInterceptable;
 import org.charleech.arq.eval.helper.AbstractMarker;
 
 import test.org.charleech.arq.eval.cen.intrcptr.service.DummyWrapper;
 
 /**
  * <p>
  * This is a concrete implementing class which provides the feature described
  * at {@link DummyManageable}.
  * </p>
  *
  * @author charlee.ch
  * @version 0.0.1
  * @since 0.0.1
  * @see AbstractMarker
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
@CentralizedInterceptable
 @Stateless
 @Slf4j
 public class DummyManager extends AbstractMarker implements DummyManageable {
 
     /**
      * This is the {@link DummyWrapper} instance as the injected utility.
      *
      * @since 0.0.1
      */
     @Inject
     private DummyWrapper wrapper;
 
     @Override
     public void doProcess() {
         this.wrapper.dummy();
         DummyManager.log.info(this.getMarker(), "This is a dummy process.");
     }
 }
