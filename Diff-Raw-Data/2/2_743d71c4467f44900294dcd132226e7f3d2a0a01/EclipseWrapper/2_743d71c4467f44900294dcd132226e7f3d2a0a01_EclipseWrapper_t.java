 package org.charleech.arq.eval.helper.eclipse;
 
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.extern.slf4j.Slf4j;
 
 import org.charleech.arq.eval.helper.AbstractMarker;
 import org.charleech.arq.eval.helper.ArquillianConfigureConstant;
 
 /**
  * <p>
  * This is a concrete implementing class which provides the feature described
  * at {@link EclipseWrappable}.
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
 @Slf4j
 public final class EclipseWrapper extends AbstractMarker
                                implements EclipseWrappable {
 
     /**
      * <p>
      * This is a self instance which represents and implements the singleton
      * pattern.
      * </p>
      *
      * @since 0.0.1
      */
     @Getter(value = AccessLevel.PUBLIC)
     private static final EclipseWrappable INSTANCE;
 
     /**
      * This is a constant which represents the parameter 1.
      *
      * @since 0.0.1
      */
     private static final String PARAM1;
 
     /**
      * This is a constant which represents the error code 001.
      *
      * @since 0.0.1
      */
     private static final String ERR001;
 
     static {
         PARAM1    = "_P1";
         ERR001    = "The variable named "
                   + EclipseWrapper.PARAM1
                   + " must be existed at "
                   + "environment variable or system properties";
         INSTANCE  = new EclipseWrapper();
     }
 
     /**
      * <p>
      * This is a default constructor.
      * </p>
      * <p>
      * It is a private accessor with purpose to prevent to be instantiated by
      * other with purpose to ensure the singleton pattern.
      * </p>
      *
      * @since 0.0.1
      */
     private EclipseWrapper() {
         super();
     }
 
     @Override
     public void setSystemProperties() {
         String root = null;
         try {
             if (this.isExisted()) {
                 EclipseWrapper.log.info(
                    this.getMarker(),
                    "The required system properties is existed already.");
                 return;
             }
 
             root = this.getRoot();
 
             System.setProperty(ArquillianConfigureConstant.
                                   GfBindHttpPort.getValue(),
                               "14079");
             System.setProperty(ArquillianConfigureConstant.
                                   GfInstallRoot.getValue(),
                                root.concat("/mygf"));
             System.setProperty(ArquillianConfigureConstant.
                                GfInstanceRoot.getValue(),
                                root.concat("/mygf/domains/domain1"));
             System.setProperty(ArquillianConfigureConstant.
                                   JavaEndorsedDirs.getValue(),
                                root.concat("/mygf/modules/endorsed"));
 
         } finally {
             root = null;
         }
     }
 
     /**
      * Determine if the required system properties are existed or not.
      *
      * @return The determining result.
      * @since 0.0.1
      */
     private boolean isExisted() {
         String httpPort     = null;
         String installRoot  = null;
         String instanceRoot = null;
         try {
             httpPort     = System.getProperty(ArquillianConfigureConstant.
                                                  GfBindHttpPort.getValue());
             installRoot  = System.getProperty(ArquillianConfigureConstant.
                                                  GfInstallRoot.getValue());
             instanceRoot = System.getProperty(ArquillianConfigureConstant.
                                                  GfInstanceRoot.getValue());
             return (httpPort != null)
                 && (installRoot != null)
                 && (instanceRoot != null);
         } finally {
             httpPort     = null;
             installRoot  = null;
             instanceRoot = null;
         }
     }
 
     /**
      * Get the root configuration folder.
      *
      * @return The root configuration folder.
      */
     private String getRoot() {
         String root = null;
         try {
 
             root = System.getenv(ArquillianConfigureConstant.
                     ArquillianGfRoot.getValue());
             //Exist at environment variable.
             if (root != null) {
                 return root;
             }
 
             root = System.getProperty(ArquillianConfigureConstant.
                                      ArquillianGfRoot.getValue());
 
             //Exist at system properties variable.
             if (root != null) {
                 return root;
             }
 
             //Not exist.
             throw new Error(EclipseWrapper.ERR001.replaceAll(
                                EclipseWrapper.PARAM1,
                                ArquillianConfigureConstant.
                                   ArquillianGfRoot.getValue()));
 
 
         } finally {
             root = null;
         }
     }
 }
