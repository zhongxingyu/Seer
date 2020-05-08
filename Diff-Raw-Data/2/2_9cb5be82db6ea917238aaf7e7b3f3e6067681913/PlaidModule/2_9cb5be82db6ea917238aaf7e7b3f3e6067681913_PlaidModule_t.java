 package com.example.plaid.services;
 
 import com.example.checked.CheckedModule;
 import com.example.tartan.TartanModule;
 import org.apache.tapestry5.SymbolConstants;
 import org.apache.tapestry5.ioc.MappedConfiguration;
 import org.apache.tapestry5.ioc.ServiceBinder;
 import org.apache.tapestry5.ioc.annotations.SubModule;
 
 @SubModule({ CheckedModule.class, TartanModule.class })
 public class PlaidModule
 {
 
     public static void bind(ServiceBinder binder)
     {
         binder.bind(PlaidService.class, PlaidServiceImpl.class);
     }
 
     public static void contributeApplicationDefaults(MappedConfiguration<String, Object> configuration)
     {
        configuration.add(SymbolConstants.PRODUCTION_MODE, true);
     }
 
 }
 
