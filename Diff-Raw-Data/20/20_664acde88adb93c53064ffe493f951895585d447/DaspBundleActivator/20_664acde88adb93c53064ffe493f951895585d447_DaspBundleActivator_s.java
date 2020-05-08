 package ddth.dasp.id.osgi;
 
import org.osgi.framework.BundleContext;

 import ddth.dasp.framework.osgi.BaseSpringBundleActivator;
 import ddth.dasp.id.DaspBundleConstants;
 
 public class DaspBundleActivator extends BaseSpringBundleActivator {
    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        // IdGenerator.disposeInstance(idGen);
        super.stop(bundleContext);
    }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected String getModuleName() {
         return DaspBundleConstants.MODULE_NAME;
     }
 
     @Override
     protected String[] getSpringConfigFiles() {
         return new String[] { "/META-INF/osgispring/*.xml" };
     }
 }
