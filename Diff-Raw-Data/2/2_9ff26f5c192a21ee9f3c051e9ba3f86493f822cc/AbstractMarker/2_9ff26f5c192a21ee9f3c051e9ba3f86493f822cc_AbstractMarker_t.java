 package org.charleech.arq.eval.helper;
 
 import org.charleech.arq.eval.helper.util.MarkerWrapper;
 import org.slf4j.Marker;
 
 /**
  * <p>
  * This is an abstract class which provides the feature described at
  * {@link Markable}.
  * </p>
  *
  * @author charlee.ch
  * @version 0.0.1
  * @since 0.0.1
  * @see Markable
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
 public abstract class AbstractMarker implements Markable {
 
     @Override
    public Marker getMarker() {
         return MarkerWrapper.getINSTANCE().getMarker(this.getClass());
     }
 }
