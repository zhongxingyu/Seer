 package com.page5of4.codon.extender.internal;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ExtendedBundle {
    private static final Logger logger = LoggerFactory.getLogger(ExtendedBundle.class);
    private final BundleContext extenderContext;
    private final Bundle bundle;
 
    public ExtendedBundle(BundleContext extenderContext, Bundle bundle) {
       super();
       this.extenderContext = extenderContext;
       this.bundle = bundle;
    }
 
    public void open() {
       logger.info("Opening");
    }
 
    public void close() {
       logger.info("Closing");
    }
 }
