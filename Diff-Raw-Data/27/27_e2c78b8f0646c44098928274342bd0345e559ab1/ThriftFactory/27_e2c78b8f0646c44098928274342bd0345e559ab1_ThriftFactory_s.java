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
