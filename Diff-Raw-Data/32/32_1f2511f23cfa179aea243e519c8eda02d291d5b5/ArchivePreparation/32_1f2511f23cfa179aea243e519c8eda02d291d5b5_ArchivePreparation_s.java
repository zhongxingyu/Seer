 package test.org.charleech.arq.eval.deploy;
 
 import java.io.File;
 
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.extern.slf4j.Slf4j;
 
 import org.charleech.arq.eval.deploy.DummyServiceable;
 import org.charleech.arq.eval.helper.AbstractMarker;
 import org.charleech.arq.eval.helper.ArquillianConstant;
 import org.charleech.arq.eval.helper.ArquillianFeatureConstant;
 import org.charleech.arq.eval.helper.util.FeatureWrappable;
 import org.charleech.arq.eval.helper.util.FeatureWrapper;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 
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
  * @see EnterpriseArchive
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
         implements ArchivePreparable<EnterpriseArchive> {
 
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
     private static final ArchivePreparable<EnterpriseArchive> INSTANCE;
 
 
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
    public EnterpriseArchive getDeploying() {
        EnterpriseArchive ear = null;
        try {
            ear = this.createEar();
            return ear;
        } finally {
            ear = null;
        }
     }
 
     /**
      * Create {@link EnterpriseArchive}.
      *
      * @return The created {@link EnterpriseArchive}
      * @since 0.0.1
      */
    private EnterpriseArchive createEar() {
         EnterpriseArchive ear = null;
         try {
             ear = ShrinkWrap.create(EnterpriseArchive.class,
                                    ArquillianConstant.
                                       EAR_ARCHIVE_NAME.getValue()).
                      addAsApplicationResource(this.getApplicationXml(),
                                               ArquillianFeatureConstant.
                                                  APPLICATION.getValue()).
                      addAsModule(this.createEjb()).
                      addAsModule(this.createWeb());
             ArchivePreparation.log.info(this.getMarker(),
                                         "The deploying ear is\r\n{}",
                                         ear.toString(true));
             return ear;
         } finally {
             ear = null;
         }
     }
 
     /**
      * Create {@link JavaArchive}.
      *
      * @return The created {@link JavaArchive}
      * @since 0.0.1
      */
     private JavaArchive createEjb() {
         JavaArchive ejb = null;
         try {
             ejb = ShrinkWrap.create(JavaArchive.class,
                                     ArquillianConstant.
                                        EJB_ARCHIVE_NAME.getValue()).
                      addAsManifestResource(this.getBeansXml(),
                                            ArquillianFeatureConstant.
                                               BEANS.getValue()).
                      addAsResource(this.getLogbackTestXml(),
                                    ArquillianFeatureConstant.
                                       LOG_BACK_TEST.getValue()).
                      addPackages(true, DummyServiceable.class.getPackage());
             ArchivePreparation.log.info(this.getMarker(),
                                         "The deploying ejb is\r\n{}",
                                          ejb.toString(true));
             return ejb;
         } finally {
             ejb = null;
         }
     }
 
     /**
      * Create the {@link WebArchive}.
      *
      * @return The created {@link WebArchive}
      * @since 0.0.1
      */
     private WebArchive createWeb() {
         WebArchive web = null;
         try {
             web = ShrinkWrap.create(WebArchive.class,
                                     ArquillianConstant.
                                        WEB_ARCHIVE_NAME.getValue()).
                     addAsWebInfResource(this.getWebXml(),
                                         ArquillianFeatureConstant.
                                            WEB.getValue());
             ArchivePreparation.log.info(this.getMarker(),
                                         "The deploying web is\r\n{}",
                                          web.toString(true));
             return web;
         } finally {
             web = null;
         }
     }
 
     /**
      * Get the application.xml configuration file.
      *
      * @return The application.xml configuration file.
      * @since 0.0.1
      */
     private File getApplicationXml() {
         return this.asFileFromJaveEE(
                        ArquillianFeatureConstant.APPLICATION.getValue());
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
 }
