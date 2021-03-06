 /*
  * HUMBOLDT: A Framework for Data Harmonisation and Service Integration.
  * EU Integrated Project #030962                 01.10.2006 - 30.09.2010
  * 
  * For more information on the project, please refer to the this web site:
  * http://www.esdi-humboldt.eu
  * 
  * LICENSE: For information on the license under which this program is 
  * available, please refer to http:/www.esdi-humboldt.eu/license.html#core
  * (c) the HUMBOLDT Consortium, 2007 to 2010.
  */
 
 package eu.esdihumboldt.hale.cache;
 
 public class CacheManager extends net.sf.ehcache.CacheManager{

 	protected void finalize() {
 		super.shutdown();
 	}
 	
 	public static void flush(String cache) {
 		net.sf.ehcache.CacheManager.getInstance().getCache(cache).flush();
 	}
 }
