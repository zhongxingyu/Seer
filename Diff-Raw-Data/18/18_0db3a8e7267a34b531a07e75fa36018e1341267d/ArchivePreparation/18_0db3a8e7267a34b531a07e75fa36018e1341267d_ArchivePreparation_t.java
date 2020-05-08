 package test.org.charleech.arq.eval.cen.intrcptr;
 
 import java.io.File;
 
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.extern.slf4j.Slf4j;
 
 import org.charleech.arq.eval.cen.intrcptr.CentralizedInterceptable;
 import org.charleech.arq.eval.helper.AbstractMarker;
 import org.charleech.arq.eval.helper.ArquillianConstant;
 import org.charleech.arq.eval.helper.ArquillianFeatureConstant;
 import org.charleech.arq.eval.helper.util.FeatureWrappable;
 import org.charleech.arq.eval.helper.util.FeatureWrapper;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 
 import test.org.charleech.arq.eval.cen.intrcptr.local.DummyManageable;
 import test.org.charleech.arq.eval.cen.intrcptr.service.DummyServiceable;
 import test.org.charleech.arq.eval.cen.intrcptr.util.DummyUtil;
 
 /**
  * <p>
  * This is a concrete implementing class which provides the feature described
  * at {@link ArchivePreparable}.
  * </p>
  *
  * @author charlee.ch
  * @version 0.0.1
  * @since 0.0.1
  * @see ArchivePreparable
  * @see WebArchive
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
 public final class ArchivePreparation
            extends AbstractMarker
         implements ArchivePreparable<WebArchive> {
 
     /**
      * This is a constant which represents the {@link FeatureWrappable}.
      *
      * @since 0.0.1
      */
     private static final FeatureWrappable FEATURE;
 
     /**
      * <p>
      * This is a self instance which represents and implements the singleton
      * pattern.
      * </p>
      *
      * @since 0.0.1
      */
     @Getter(value = AccessLevel.PUBLIC)
     private static final ArchivePreparable<WebArchive> INSTANCE;
 
 
     static {
         FEATURE  = FeatureWrapper.getINSTANCE();
         INSTANCE = new ArchivePreparation();
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
     private ArchivePreparation() {
 
     }
 
     @Override
     public WebArchive getDeploying(final String prefix) {
         return this.createWeb(prefix);
     }
 
     /**
      * Create the {@link WebArchive}.
      *
      * @return The created {@link WebArchive}
     * @see #createLib2() The lib2 is a big point, please see javadoc for
     *      further information.
      * @since 0.0.1
      */
     public WebArchive createWeb(final String prefix) {
         WebArchive web = null;
         try {
             web = ShrinkWrap.create(WebArchive.class,
                                     prefix.concat(ArquillianConstant.
                                        WEB_ARCHIVE_NAME.getValue())).
                     addAsWebInfResource(this.getWebXml(),
                                         ArquillianFeatureConstant.
                                            WEB.getValue()).
                     addAsWebInfResource(this.getGfWebXml(),
                                         ArquillianFeatureConstant.
                                            GF_WEB.getValue()).
                     addAsWebInfResource(this.getGfEjbJarXml(),
                                         ArquillianFeatureConstant.
                                            GF_EJB_JAR.getValue()).
                     addAsWebInfResource(this.getBeansXml(),
                                         ArquillianFeatureConstant.
                                            BEANS.getValue()).
                     addPackages(true,
                                 DummyServiceable.class.getPackage()).
                     addPackages(true,
                                 DummyManageable.class.getPackage()).
                     addAsLibraries(this.createLib()).
                    //The lib2 is a big point, please see javadoc for further
                    //information
                     addAsLibraries(this.createLib2());
             ArchivePreparation.log.info(this.getMarker(),
                                         "The deploying web is\r\n{}",
                                          web.toString(true));
             return web;
         } finally {
             web = null;
         }
     }
 
     /**
      * Create java library for web application.
      *
      * @return The created java library for web application
      * @since 0.0.1
      */
     private JavaArchive createLib() {
         JavaArchive lib = null;
         try {
             lib = ShrinkWrap.create(JavaArchive.class, "my-lib.jar").
                     addAsManifestResource(this.getBeansXml(),
                             ArquillianFeatureConstant.
                                BEANS.getValue()).
                      addAsResource(this.getLogbackTestXml(),
                                    ArquillianFeatureConstant.
                                       LOG_BACK_TEST.getValue()).
                      addPackages(true,
                                  CentralizedInterceptable.class.getPackage());
 
             ArchivePreparation.log.info(this.getMarker(),
                     "The deploying lib is\r\n{}",
                      lib.toString(true));
 
             return lib;
         } finally {
             lib = null;
         }
     }
 
     /**
      * Create java library for web application.
      * <p>
      * The lib2 is a big point. Even the interceptor is enabled via its
      * beans.xml, it is not activated. Sadly, we need to set the
      * glassfish-web-.xml as &lt;class-loader delegate="false"/&gt;
      * </p>
     * <p>
     * It's depended on the class loader, the first library (sorted by name)
     * which the container first found beans.xml
     * </p>
     * <p>
     * If the first not mentions interceptor, Even the sencond mentions
     * intereptor, all the rest are ignored.
     * </p>
      *
      * @return The created java library for web application
      * @since 0.0.1
      */
     private JavaArchive createLib2() {
         JavaArchive lib = null;
         try {
             lib = ShrinkWrap.create(JavaArchive.class, "my-lib2.jar").
                      addAsManifestResource(this.getEmptyBeansXml(),
                             ArquillianFeatureConstant.
                                BEANS.getValue()).
                      addAsResource(this.getLogbackTestXml(),
                                    ArquillianFeatureConstant.
                                       LOG_BACK_TEST.getValue()).
                      addClass(DummyUtil.class);
 
             ArchivePreparation.log.info(this.getMarker(),
                     "The deploying lib 2 is\r\n{}",
                      lib.toString(true));
 
             return lib;
         } finally {
             lib = null;
         }
     }
 
     /**
      * Get the beans.xml configuration file.
      *
      * @return The beans.xml configuration file.
      * @since 0.0.1
      */
     private File getBeansXml() {
         return this.asFileFromJaveEE(
                 ArquillianFeatureConstant.BEANS.getValue());
     }
 
     /**
      * Get the empty beans.xml configuration file.
      *
      * @return The empty beans.xml configuration file.
      * @since 0.0.1
      */
     private File getEmptyBeansXml() {
         return this.asFileFromJaveEE(
                 "/empty-beans.xml");
     }
 
     /**
      * Get the web.xml configuration file.
      *
      * @return The web.xml configuration file.
      * @since 0.0.1
      */
     private File getWebXml() {
         return this.asFileFromJaveEE(
                 ArquillianFeatureConstant.WEB.getValue());
     }
 
     /**
      * Get the glassfish-web.xml configuration file.
      *
      * @return The glassfish-web.xml configuration file.
      * @since 0.0.1
      */
     private File getGfWebXml() {
         return this.asFileFromGf(
                 ArquillianFeatureConstant.GF_WEB.getValue());
     }
 
     /**
      * Get the glassfish-ejb-jar.xml configuration file.
      *
      * @return The glassfish-ejb-jar.xml configuration file.
      * @since 0.0.1
      */
     private File getGfEjbJarXml() {
         return this.asFileFromGf(
                        ArquillianFeatureConstant.GF_EJB_JAR.getValue());
     }
 
     /**
      * Get the logback-test.xml configuration file.
      *
      * @return The logback-test.xml configuration file.
      * @since 0.0.1
      */
     private File getLogbackTestXml() {
         return this.asFileFromJaveEE(
                 ArquillianFeatureConstant.LOG_BACK_TEST.getValue());
     }
 
     /**
      * Get the resource from {@link ArquillianConstant#ROOT_CONFIGURE_JAVAEE} by
      * using the specified file name
      *
      * @param fileName
      *            The loading file name
      * @return The loaded file
      * @since 0.0.1
      */
     private File asFileFromJaveEE(final String fileName) {
         return ArchivePreparation.FEATURE.asFile(
                 ArquillianConstant.ROOT_CONFIGURE.getValue().
                 concat(ArquillianConstant.ROOT_CONFIGURE_JAVAEE.getValue()).
                 concat(fileName)
               );
     }
 
     /**
      * Get the resource from {@link ArquillianConstant#ROOT_CONFIGURE_GF} by
      * using the specified file name
      *
      * @param fileName
      *            The loading file name
      * @return The loaded file
      * @since 0.0.1
      */
     private File asFileFromGf(final String fileName) {
         return ArchivePreparation.FEATURE.asFile(
                 ArquillianConstant.ROOT_CONFIGURE.getValue().
                 concat(ArquillianConstant.ROOT_CONFIGURE_GF.getValue()).
                 concat(fileName)
               );
     }
 }
