 /**
  * $Revision: 1456 $
  * $Date: 2005-06-01 22:04:54 -0700 (Wed, 01 Jun 2005) $
  *
  * Copyright 2003-2005 Jive Software.
  *
  * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jivesoftware.smack.util;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.Vector;
 
 import org.xbill.DNS.Lookup;
 import org.xbill.DNS.Record;
 import org.xbill.DNS.SRVRecord;
 import org.xbill.DNS.TextParseException;
 import org.xbill.DNS.Type;
 
 /**
  * Utilty class to perform DNS lookups for XMPP services.
  *
  * @author Matt Tucker
  */
 public class DNSUtil {
     // Use the same PRNG seed for all lookups, so within a single process
     // we always return weighted results in the same randomized order.
     final private static int seed = new Random().nextInt();
 
     /**
      * Shuffle a list of items, prioritizing the order by their weight, using a simple
      * PRNG with the given seed.
      * <p>
      * For example, if items is [0, 1] and weightList is [10, 30], the result will
      * be [0,1] 25% of the time and [1,0] 75% of the time.
      * <p>
      * Note that this algorithm is O(n^2), and isn't suitable for very large inputs.
      */
     private static <T> Vector<T> getItemsRandomizedByWeight(Vector<T> items, Vector<Integer> weightList)
     {
         Vector<T> result = new Vector<T>();
 
         // Make a copy of items and weightList, since we're going to be modifying them.
         items = new Vector<T>(items);
         weightList = new Vector<Integer>(weightList);
 
         // Shuffle the items first, so items with the same weight are chosen randomly.
         // Reconstruct the PRNG for each shuffle, so the items and weights are kept
         // in sync.
         Collections.shuffle(items, new Random(seed));
         Collections.shuffle(weightList, new Random(seed));
 
         Random prng = new Random(seed);
         while(!items.isEmpty()) {
             Vector<Integer> cumulativeWeights = new Vector<Integer>(weightList.size());
             int maxSum = 0;
             for(int weight: weightList) {
                 // If very large weights would cause us to overflow, clamp all following weights
                 // to 0.
                 if(maxSum + weight < maxSum)
                     weight = 0;
                 maxSum += weight;
                 cumulativeWeights.add(maxSum);
             }
 
             // Choose an item by weight.  Note that we may have items with zero weight,
             // and that nextInt(0) is invalid.
             int weight = 0;
             if(maxSum > 0)
                 weight = prng.nextInt(maxSum);
 
             // Search for the weight we chose.
             int idx = Collections.binarySearch(cumulativeWeights, weight);
             if(idx < 0) {
                 // If idx < 0, then -(idx+1) is the first element > weight, which is what we want.
                 idx = -(idx+1);
             } else {
                 // If idx >= 0, then idx is any element equal to weight.  We want the first value
                 // greater than it, so seek forward to find it.  The last weight in cumulativeWeights
                 // is always greater than weight, so this is guaranteed to terminate.  The exception
                 // is when the list contains only zero weights, in which case we'll use the first
                 // item.
                 if(maxSum == 0)
                     idx = 0;
                 else {
                     while(cumulativeWeights.get(idx) <= weight)
                         ++idx;
                 }
             }
 
             // Add the item we selected to the result.
             result.add(items.get(idx));
 
             // Remove the item we selected from the source data, and restart.
             items.remove(idx);
             weightList.remove(idx);
         }
 
         return result;
     }
 
     private static Vector<HostAddress> resolveSRV(AsyncLookup asyncLookup) {
         Vector<SRVRecord> results = new Vector<SRVRecord>();
         Lookup lookup;
         try {
             lookup = asyncLookup.run();
         } catch (TextParseException e) {
             return new Vector<HostAddress>();
         }
         if(lookup == null)
             return null;
 
         Record recs[] = lookup.getAnswers();
         if (recs == null)
                 return new Vector<HostAddress>();
 
         SRVRecord srecs[] = new SRVRecord[recs.length];
         for(int i = 0; i < recs.length; ++i)
             srecs[i] = (SRVRecord) recs[i];
 
         // Sort the results by ascending priority.
         Arrays.sort(srecs, new Comparator<SRVRecord>() {
             public int compare(SRVRecord lhs, SRVRecord rhs) {
                 return lhs.getPriority() - rhs.getPriority();
             }
         });
 
         HashMap<Integer, Vector<SRVRecord>> resultsByPriority = new HashMap<Integer, Vector<SRVRecord>>();
 
         // Separate the results by priority.
         for(int i = 0; i < srecs.length; ++i) {
             SRVRecord srv = srecs[i];
             Vector<SRVRecord> list = resultsByPriority.get(srv.getPriority());
             if(list == null) {
                 list = new Vector<SRVRecord>();
                 resultsByPriority.put(srv.getPriority(), list);
             }
             list.add(srv);
         }
 
         Vector<Integer> weights = new Vector<Integer>(resultsByPriority.keySet());
         Collections.sort(weights);
 
         // For each priority group, sort the results based on weight.  Do this
         // in sorted order by weight, so priorities closer to 0 are earlier in
         // the list.
         for(int weight: weights) {
             Vector<SRVRecord> list = resultsByPriority.get(weight);
             Vector<Integer> weightList = new Vector<Integer>();
             for(SRVRecord item: list)
                 weightList.add(item.getWeight());
 
             Vector<SRVRecord> output = getItemsRandomizedByWeight(list, weightList);
             results.addAll(output);
         }
 
         Vector<HostAddress> addresses = new Vector<HostAddress>();
         for(SRVRecord result: results) {
             // Host entries in DNS should end with a ".".
             String host = result.getTarget().toString();
             if (host.endsWith("."))
                 host = host.substring(0, host.length() - 1);
             addresses.add(new HostAddress(host, result.getPort()));
         }
         return addresses;
     }
 
     /**
      * Returns the host name and port that the specified XMPP server can be
      * reached at for client-to-server communication. A DNS lookup for a SRV
      * record in the form "_xmpp-client._tcp.example.com" is attempted, according
      * to section 14.4 of RFC 3920. If that lookup fails, a lookup in the older form
      * of "_jabber._tcp.example.com" is attempted since servers that implement an
      * older version of the protocol may be listed using that notation. If that
      * lookup fails as well, it's assumed that the XMPP server lives at the
      * host resolved by a DNS lookup at the specified domain on the default port
      * of 5222.<p>
      *
      * As an example, a lookup for "example.com" may return "im.example.com:5269".
      *
      * @param domain the domain.
      * @return a HostAddress, which encompasses the hostname and port that the XMPP
      *      server can be reached at for the specified domain.
      */
     public static class XMPPDomainLookup {
         private AsyncLookup asyncLookup1;
         private AsyncLookup asyncLookup2;
         private HostAddress defaultResult;
 
         public XMPPDomainLookup(String domain, boolean client) {
             String prefix = client? "_xmpp-client._tcp.":"_xmpp-server._tcp.";
             asyncLookup1 = new AsyncLookup(prefix + domain, Type.SRV);
             asyncLookup2 = new AsyncLookup("_jabber._tcp." + domain, Type.SRV);
             defaultResult = new HostAddress(domain, client? 5222:5269);
         }
 
         /**
          * Perform the lookup.  If cancelled by an asynchronous call to cancel(),
          * return null.
          */
         public Vector<HostAddress> run() {
             Vector<HostAddress> addresses = resolveSRV(asyncLookup1);
            if(addresses == null || !addresses.isEmpty())
                return addresses;

            addresses = resolveSRV(asyncLookup2);
            if(addresses == null || !addresses.isEmpty())
                return addresses;

            addresses = new Vector<HostAddress>();
            addresses.add(defaultResult);
             return addresses;
         }
 
         public void cancel() {
             asyncLookup1.cancel();
             asyncLookup2.cancel();
         }
     };
 
     /**
      * Given a domain, look up the specified attribute according to XEP-0156.
      * <p>
      * For example, resolveXmppConnect("example.com", "_xmpp-client-xbosh")
      * may return [http://bosh.example.com/bind].
      */
     public static class XMPPConnectLookup {
         AsyncLookup asyncLookup;
         public XMPPConnectLookup(String domain, String attribute) {
             domain = "_xmppconnect." + domain;
             asyncLookup = new AsyncLookup(domain, Type.TXT);
         }
 
         public Vector<String> run() {
             Vector<String> results = new Vector<String>();
 
             Lookup lookup;
             try {
                 lookup = asyncLookup.run();
             } catch (TextParseException e) {
                 return new Vector<String>();
             }
             if (lookup == null)
                 return new Vector<String>();
 
             Record recs[] = lookup.getAnswers();
             if (recs == null)
                 return new Vector<String>();
 
             for(int i = 0; i < recs.length; ++i) {
                 String txt = recs[i].rdataToString();
                 // For some reason, the data is in quotes.  Remove them.
                 if(txt.length() < 2 || !txt.startsWith("\"") || !txt.endsWith("\""))
                     continue;
                 txt = txt.substring(1, txt.length()-1);
 
                 int idx = txt.indexOf("=");
                 if(idx == -1)
                     continue;
                 results.add(txt.substring(idx+1, txt.length()));
             }
 
             return results;
         }
 
         public void cancel() {
             asyncLookup.cancel();
         }
     }
 
     public static class AsyncLookup {
         private Thread lookupThread;
 
         /* The parameters to pass to Lookup(): */
         private String name;
         private int type;
 
         /* Results.  When any of these is non-null, or cancelled is true,
          * the operation is complete. */
         private Lookup lookup;
         private TextParseException error;
         private boolean cancelled;
 
         public AsyncLookup(String name, int type) {
             this.name = name;
             this.type = type;
         }
 
         private void thread() {
             TextParseException exception = null;
             Lookup lookup = null;
             try {
                 // Beware: InetSocketAddress will do a pointless reverse lookup on the IP
                 // it's given, so this will block as well, and it may not be cancellable.
                 // The lookup thread is not joined, because we can't reliably cancel it
                 // due to this.
                 lookup = new Lookup(name, type);
                 lookup.run();
             } catch (TextParseException e) {
                 exception = e;
             }
 
             synchronized(this) {
                 this.lookup = lookup;
                 this.error = exception;
                 this.notify();
             }
         }
 
         /** Perform the lookup.  If the lookup completes without being interrupted,
          *  return the Lookup object.  If the lookup is interrupted, return null. */
         public synchronized Lookup run() throws TextParseException
         {
             if(cancelled)
                 return null;
             if(lookupThread != null)
                 throw new RuntimeException("AsyncLookup#run was called multiple times");
 
             lookupThread = new Thread() {
                 public void run() { thread(); }
             };
             lookupThread.setName("DNS: " + name);
             lookupThread.start();
 
             // Wait for the lookup to finish or be cancelled.
             while(lookup == null && error == null && !cancelled) {
                 try {
                     wait();
                 } catch(InterruptedException e) {
                     // This thread was interrupted--not the lookup thread.  We didn't do
                     // this.  Leave the interrupted flag set and return null.
                     Thread.currentThread().interrupt();
                     return null;
                 }
             }
 
             if(error != null) {
                 error.fillInStackTrace();
                 throw error;
             }
 
             // Whether the lookup was finished or cancelled, lookup is the result.
             return lookup;
         }
 
         /**
         * Cancel the lookup.  If run() is called, or is called in the future, null
         * will be returned.  This function can be called asynchronously.
          */
         public synchronized void cancel() {
             cancelled = true;
            // Attempt to stop the lookup, if any.  XXX: This doesn't work, so the lookup
            // is stranded until it times out.
             if(lookupThread != null)
                 lookupThread.interrupt();
            notifyAll();
         }
     };
 
     /**
      * Encapsulates a hostname and port.
      */
     public static class HostAddress {
 
         private String host;
         private int port;
 
         private HostAddress(String host, int port) {
             this.host = host;
             this.port = port;
         }
 
         /**
          * Returns the hostname.
          *
          * @return the hostname.
          */
         public String getHost() {
             return host;
         }
 
         /**
          * Returns the port.
          *
          * @return the port.
          */
         public int getPort() {
             return port;
         }
 
         public String toString() {
             return host + ":" + port;
         }
 
         public boolean equals(Object o) {
             if (this == o) {
                 return true;
             }
             if (!(o instanceof HostAddress)) {
                 return false;
             }
 
             final HostAddress address = (HostAddress) o;
 
             if (!host.equals(address.host)) {
                 return false;
             }
             return port == address.port;
         }
     }
 }
