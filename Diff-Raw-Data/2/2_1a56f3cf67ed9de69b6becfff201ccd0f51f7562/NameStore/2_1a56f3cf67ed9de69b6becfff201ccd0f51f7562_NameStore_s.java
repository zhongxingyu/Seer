 package net.joala.dns;
 
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import org.xbill.DNS.Address;
 import org.xbill.DNS.Name;
 import org.xbill.DNS.TextParseException;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.annotation.concurrent.ThreadSafe;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 import static java.lang.String.format;
 
 /**
  * <p>
  * This store is responsible for overriding calls for resolving host names. It allows you to manually
  * add and remove known hosts during your tests.
  * </p>
  *
  * @since 10/5/12
  */
 @ThreadSafe
 public final class NameStore {
   /**
    * A logger using System-PrintStreams.
    */
   private static final SystemLogger LOG = SystemLogger.getLogger(NameStore.class);
 
   /**
    * Mapper to map a string to an {@code InetAddress}.
    */
   private static final Function<String, InetAddress> STRING_TO_INET_ADDRESS = new String2InetAddress();
   /**
    * Store which contains the hosts to map to specified internet addresses.
    */
   private final Map<Name, Set<InetAddress>> store = Maps.newHashMap();
   /**
    * Singleton instance.
    */
   private static final NameStore ourInstance = new NameStore();
 
   /**
    * Use {@link #nameStore()} to get an instance.
    */
   private NameStore() {
   }
 
   /**
    * <p>
    * Get singleton instance of name store.
    * </p>
    *
    * @return name store instance
    */
   @Nonnull
   public static NameStore nameStore() {
     return ourInstance;
   }
 
   /**
    * <p>
    * Register name with the given addresses.
    * If name is already registered for some inet addresses
    * the given addresses will be added to the known ones.
    * </p>
    *
    * @param name      host name representation
    * @param addresses list of addresses to register for name
    */
   private void register(@Nonnull final Name name, @Nonnull final Collection<InetAddress> addresses) {
     checkNotNull(name, "Name must not be null.");
     checkNotNull(addresses, "Addresses must not be null.");
     checkArgument(!addresses.isEmpty(), "Addresses must not be empty.");
     synchronized (store) {
       if (store.containsKey(name)) {
         final Set<InetAddress> set = store.get(name);
         set.addAll(addresses);
         LOG.info(format("Updated registration for %s to %s.", name, store.get(name)));
       } else {
         store.put(name, Sets.newHashSet(addresses));
         LOG.info(format("Registered %s with %s.", name, store.get(name)));
       }
     }
   }
 
   /**
    * <p>
    * Register name with the given addresses.
    * If name is already registered for some inet addresses
    * the given addresses will be added to the known ones.
    * </p>
    *
    * @param name      host name
    * @param addresses list of addresses to register for name
    */
   public void register(@Nonnull final String name, @Nonnull final InetAddress... addresses) {
     checkNotNull(name, "Name must not be null.");
     try {
       register(Name.fromString(name), Arrays.asList(addresses));
     } catch (TextParseException e) {
       throw new IllegalArgumentException("Unable to parse name.", e);
     }
   }
 
   /**
    * <p>
    * Register name with the given addresses.
    * If name is already registered for some inet addresses
    * the given addresses will be added to the known ones.
    * </p>
    *
    * @param name      host name
    * @param addresses list of addresses to register for name
    */
   public void register(@Nonnull final String name, @Nonnull final String... addresses) {
     checkNotNull(name, "Name must not be null.");
     final List<InetAddress> inetAddresses = Lists.transform(Arrays.asList(addresses), STRING_TO_INET_ADDRESS);
     try {
       register(Name.fromString(name), inetAddresses);
     } catch (TextParseException e) {
       throw new IllegalArgumentException("Unable to parse name.", e);
     }
   }
 
   /**
    * <p>
    * Unregister name with all its addresses. Requires DNS cache to be disabled.
    * </p>
    *
    * @param name host name
    */
   public void unregister(@Nonnull final String name) {
     checkNotNull(name, "Name must not be null.");
     try {
       synchronized (store) {
         if (null != store.remove(Name.fromString(name))) {
           LOG.info(format("Unregistered %s.", name));
         }
       }
     } catch (TextParseException e) {
       throw new IllegalArgumentException("Invalid name.", e);
     }
   }
 
   /**
    * Clear the complete name store. Recommended before and after running the tests
    * in order to prevent that tests influence each other.
    */
   public void clear() {
     synchronized (store) {
       store.clear();
       LOG.info("Cleared name store.");
     }
   }
 
   /**
    * <p>
    * Try to find a host name in the store based on the given address.
    * </p>
    *
    * @param inetAddress the internet address to search the host name for
    * @return the name found &ndash; or {@code null}, if the internet address could not be found in store
    */
   @Nullable
   Name reverseLookup(@Nullable final InetAddress inetAddress) {
     synchronized (store) {
       final Set<Map.Entry<Name, Set<InetAddress>>> entries = store.entrySet();
       for (final Map.Entry<Name, Set<InetAddress>> entry : entries) {
         if (entry.getValue().contains(inetAddress)) {
           return entry.getKey();
         }
       }
     }
     return null;
   }
 
   /**
    * <p>
    * Lookup registered inet addresses for given name.
    * </p>
    *
    * @param name the host name
    * @return the inet addresses found for the given name; empty list if none
    */
   @Nonnull
   InetAddress[] lookup(@Nonnull final String name) {
     checkNotNull(name, "Name must not be null.");
     final Name someName;
     try {
       someName = Name.fromString(name);
     } catch (TextParseException e) {
       throw new IllegalArgumentException("Unable to parse name.", e);
     }
     return lookup(someName);
   }
 
   /**
    * <p>
    * Lookup registered inet addresses for given name.
    * </p>
    *
    * @param name the host name
    * @return the inet addresses found for the given name; empty list if none
    */
   @Nonnull
   InetAddress[] lookup(@Nonnull final Name name) {
     checkNotNull(name, "Name must not be null.");
     synchronized (store) {
       if (store.containsKey(name)) {
         LOG.info(format("Succeeded to lookup %s.", name));
         return Iterables.toArray(store.get(name), InetAddress.class);
       }
     }
     LOG.info(format("Failed to lookup %s.", name));
     return new InetAddress[0];
   }
 
   /**
    * <p>
    * Mapper to create InetAddresses from Strings.
    * </p>
    */
   private static class String2InetAddress implements Function<String, InetAddress> {
     @Override
     @Nonnull
    public InetAddress apply(@Nullable final String input) {
       try {
         checkNotNull(input, "Address must not be null.");
         return Address.getByAddress(input);
       } catch (UnknownHostException e) {
         throw new IllegalArgumentException("Failed to create InetAddress.", e);
       }
     }
   }
 
   @Override
   public String toString() {
     return Objects.toStringHelper(this)
             .add("store", store)
             .toString();
   }
 }
