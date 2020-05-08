 package ru.nsu.ccfit.resync.pref;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import ru.nsu.ccfit.resync.pref.jdt.SpCleanup;
 
/*
  * 
  * @author Gleb Kanterov <gleb@kanterov.ru>
  */
 public enum PreferenceProviderRegistry {
 
     INSTANCE;
 
     private final SpCleanup spCleanup = new SpCleanup();
 
     private PreferenceProviderRegistry() {
     }
 
     /*
      * Get active preference providers
      */
     public Collection<PreferenceProvider> getActive() {
         // TODO: provide real implementation
         //
         // TODO: in future implementation it should collect providers from
         // bundles
         //
         // TODO: in future implementation it should be able to disable specific
         // provider
 
         return Arrays.<PreferenceProvider> asList(spCleanup);
     }
 
     public static PreferenceProviderRegistry getInstance() {
         return INSTANCE;
     }
 
 }
