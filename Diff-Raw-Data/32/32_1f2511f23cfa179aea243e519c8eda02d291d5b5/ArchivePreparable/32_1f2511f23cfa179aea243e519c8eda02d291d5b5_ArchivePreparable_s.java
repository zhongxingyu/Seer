 package test.org.charleech.arq.eval.deploy;
 
 import org.jboss.shrinkwrap.api.Archive;
 
 /**
  * <p>
  * This is an interface which provides the feature for preparing the deploying
  * archive to the Arquillian.
  * </p>
  *
  * @author charlee.ch
  * @version 0.0.1
  * @since 0.0.1
  * @param <T>
  *            Any derived class form the ShrinkWrap {@link Archive}
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
 public interface ArchivePreparable<T extends Archive<T>> {
     /**
      * Get the deploying ShrinkWrap {@link Archive}.
      *
      * @return The deploying ShrinkWrap {@link Archive}.
      * @since 0.0.1
      */
    T getDeploying();
 }
