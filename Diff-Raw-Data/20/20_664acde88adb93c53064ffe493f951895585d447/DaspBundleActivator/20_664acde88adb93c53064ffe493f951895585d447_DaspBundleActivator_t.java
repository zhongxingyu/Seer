 package ddth.dasp.id.osgi;
 
 import ddth.dasp.framework.osgi.BaseSpringBundleActivator;
 import ddth.dasp.id.DaspBundleConstants;
 
 public class DaspBundleActivator extends BaseSpringBundleActivator {
    // /**
    // * {@inheritDoc}
    // */
    // @Override
    // public void stop(BundleContext bundleContext) throws Exception {
    // super.stop(bundleContext);
    // }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected String getModuleName() {
         return DaspBundleConstants.MODULE_NAME;
     }
 
     @Override
     protected String[] getSpringConfigFiles() {
        // return null; // use configuration in MANIFEST.MF
         return new String[] { "/META-INF/osgispring/*.xml" };
     }
 }
