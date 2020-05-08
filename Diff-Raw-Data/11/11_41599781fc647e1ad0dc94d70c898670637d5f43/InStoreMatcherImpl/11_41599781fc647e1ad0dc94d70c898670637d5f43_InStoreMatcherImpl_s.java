 package org.xbrlapi.data.resource;
 
 import java.net.URI;
 import java.util.HashMap;
 
 import org.xbrlapi.Fragment;
 import org.xbrlapi.FragmentList;
 import org.xbrlapi.cache.CacheImpl;
 import org.xbrlapi.data.Store;
 import org.xbrlapi.impl.MockFragmentImpl;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * The in-store resource matcher implementation, for use with the
  * persistent store implementations.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 
 public class InStoreMatcherImpl extends BaseMatcherImpl implements Matcher {
 
     /**
      * The store in which the information about matched URIs is
      * to be retained.
      */
     private Store store = null;
     private Store getStore() {
         return store;
     }
     private void setStore(Store store) {
         this.store = store;
     }
     
     /**
      * @param store The data store to use for persisting resource matches
      * @param cache The resource cache to be used by the matcher when accessing
      * resources to determine their signature.
      * @throws XBRLException if the cache parameter is null.
      */
     public InStoreMatcherImpl(Store store, CacheImpl cache) throws XBRLException {
         super(cache,new MD5SignerImpl());
         if (store == null) {
             throw new XBRLException("The store must not be null.");
         }
         setStore(store);
     }
 
     /**
      * @see org.xbrlapi.data.resource.Matcher#getMatch(URI)
      */
     public URI getMatch(URI uri) throws XBRLException {
        logger.debug("Getting match for " + uri);
         
         String query = "/*[" + Constants.XBRLAPIPrefix + ":resource/@uri='" + uri +"']";
         FragmentList<Fragment> matches = getStore().query(query);
         if (matches.getLength() > 1) throw new XBRLException("The wrong number of match fragments was retrieved.  There must be just one.");
         
         if (matches.getLength() == 0) {
            logger.debug(uri + " has not been checked for a match before.");
             String signature = this.getSignature(uri);
            logger.info(uri + " : " + signature);
 
             if (getStore().hasFragment(signature)) {
                logger.info("Signature is already stored.");
                 Fragment match = getStore().getFragment(signature);
                 HashMap<String,String> attr = new HashMap<String,String>();
                 attr.put("uri",uri.toString());
                 match = getStore().getFragment(signature);
                 match.appendMetadataElement("resource",attr);
                 return match.getURI();
             } 
 
            logger.info("Signature needs to be stored.");
             Fragment match = new MockFragmentImpl(signature);
             HashMap<String,String> attr = new HashMap<String,String>();
             attr.put("uri",uri.toString());
             match.setMetaAttribute("uri",uri.toString());
             match.appendMetadataElement("resource",attr);
             getStore().storeFragment(match);
            getStore().serialize(match);
             return uri;
             
         } 
 
         Fragment match = matches.get(0);
         return match.getURI();
         
     }
     
 
 
 }
