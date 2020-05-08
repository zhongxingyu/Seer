 package com.fasterxml.jackson.dataformat.thrift;
 
 import java.io.IOException;
 
 import com.fasterxml.jackson.core.*;
 import com.fasterxml.jackson.core.format.InputAccessor;
 import com.fasterxml.jackson.core.format.MatchStrength;
 
 public class ThriftFactory extends JsonFactory
 {
     private static final long serialVersionUID = 1;
 
     /*
     /**********************************************************
     /* Factory construction, configuration
     /**********************************************************
      */
     
     public ThriftFactory() { }
 
     public ThriftFactory(ObjectCodec codec) {
         super(codec);
     }
    protected ThriftFactory(ThriftFactory src, ObjectCodec oc)
    {
        super(src, oc);
    }

    @Override
    public ThriftFactory copy()
    {
        _checkInvalidCopy(ThriftFactory.class);
        return new ThriftFactory(this, null);
    }

    /*
    /**********************************************************
    /* Serializable overrides
    /**********************************************************
     */

    /**
     * Method that we need to override to actually make restoration go
     * through constructors etc.
     * Also: must be overridden by sub-classes as well.
     */
    @Override
    protected Object readResolve() {
        return new ThriftFactory(this, _objectCodec);
    }
 
     /*                                                                                       
     /**********************************************************                              
     /* Versioned                                                                             
     /**********************************************************                              
      */
 
     @Override
     public Version version() {
         return PackageVersion.VERSION;
     }
 
     /*
     /**********************************************************
     /* Format detection functionality
     /**********************************************************
      */
     
     @Override
     public String getFormatName()
     {
         return ThriftSchema.FORMAT_NAME;
     }
     
     /**
      * Sub-classes need to override this method
      */
     @Override
     public MatchStrength hasFormat(InputAccessor acc) throws IOException
     {
         // TODO, if possible... probably isn't?
         return MatchStrength.INCONCLUSIVE;
     }
     
 }
