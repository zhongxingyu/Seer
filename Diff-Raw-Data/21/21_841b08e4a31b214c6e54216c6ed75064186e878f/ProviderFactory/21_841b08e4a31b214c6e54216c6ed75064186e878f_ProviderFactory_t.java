 package io.cloudsoft.utilities.providers;
 
 import brooklyn.config.BrooklynProperties;
 import brooklyn.config.ConfigKey;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 
 import javax.annotation.Nullable;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static io.cloudsoft.utilities.providers.Provider.AWS_PROVIDER;
 import static io.cloudsoft.utilities.providers.Provider.GOOGLE_COMPUTE_ENGINE_PROVIDER;
 import static io.cloudsoft.utilities.providers.Provider.HPCLOUD_PROVIDER;
 import static io.cloudsoft.utilities.providers.Provider.IBM_SCE_PROVIDER;
 import static io.cloudsoft.utilities.providers.Provider.INTEROUTE_PROVIDER;
 import static io.cloudsoft.utilities.providers.Provider.RACKSPACE_UK_PROVIDER;
 import static io.cloudsoft.utilities.providers.Provider.RACKSPACE_US_PROVIDER;
 import static io.cloudsoft.utilities.providers.Provider.SOFTLAYER_PROVIDER;
 
 public class ProviderFactory {
 
    public Provider createProvider(String provider) {
         if (provider.equals(AWS_PROVIDER)) {
             return new Ec2(AWS_PROVIDER, getIdentity(AWS_PROVIDER), getCredential(AWS_PROVIDER));
         } else if (provider.equals(RACKSPACE_UK_PROVIDER)) {
             return new Openstack(RACKSPACE_UK_PROVIDER, getIdentity(RACKSPACE_UK_PROVIDER),
                     getCredential(RACKSPACE_UK_PROVIDER));
         } else if (provider.equals(RACKSPACE_US_PROVIDER)) {
             return new Openstack(RACKSPACE_US_PROVIDER, getIdentity(RACKSPACE_US_PROVIDER),
                     getCredential(RACKSPACE_US_PROVIDER));
         } else if (provider.equals(HPCLOUD_PROVIDER)) {
             return new Openstack(HPCLOUD_PROVIDER, getIdentity(HPCLOUD_PROVIDER), getCredential(HPCLOUD_PROVIDER));
         } else if (provider.equals(SOFTLAYER_PROVIDER)) {
             return new Softlayer(getIdentity(SOFTLAYER_PROVIDER), getCredential(SOFTLAYER_PROVIDER));
         } else if (provider.equals(GOOGLE_COMPUTE_ENGINE_PROVIDER)) {
             return new GoogleComputeEngine(getIdentity(GOOGLE_COMPUTE_ENGINE_PROVIDER),
                     getCredential(GOOGLE_COMPUTE_ENGINE_PROVIDER));
         } else if (provider.equals(INTEROUTE_PROVIDER)) {
             return new Interoute(getIdentity(INTEROUTE_PROVIDER), getCredential(INTEROUTE_PROVIDER));
         } else if (provider.equals(IBM_SCE_PROVIDER)) {
             return new IbmSmartCloudEnterprise(getIdentity(IBM_SCE_PROVIDER), getCredential(IBM_SCE_PROVIDER));
         } else {
             throw new RuntimeException("Not supported api/provider: " + provider);
         }
     }
 
     private String getIdentity(final String provider) {
         return getValue(provider, "identity");
     }
 
     private String getCredential(String provider) {
         return getValue(provider, "credential");
     }
 
     private String getValue(final String provider, final String end) {
         final BrooklynProperties brooklynProperties = BrooklynProperties.Factory.newDefault();
         ConfigKey key = checkNotNull(Iterables.getFirst(Iterables.filter(brooklynProperties.getAllConfig().keySet(),
                 new Predicate<ConfigKey>() {
                     @Override
                     public boolean apply(@Nullable ConfigKey input) {
                         return input.getName().contains(provider) && input.getName().endsWith(end);
                     }
                 }), null));
         return brooklynProperties.getConfig(key).toString();
     }
 }
