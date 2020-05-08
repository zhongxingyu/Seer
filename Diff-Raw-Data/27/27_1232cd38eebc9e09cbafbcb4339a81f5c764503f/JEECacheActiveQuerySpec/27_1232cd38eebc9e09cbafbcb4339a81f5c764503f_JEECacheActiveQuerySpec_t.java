 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.savara.sam.aqs;
 
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.infinispan.util.concurrent.NotifyingFuture;
 import org.savara.sam.aq.ActiveQuerySpec;
 
 public class JEECacheActiveQuerySpec<S,T> extends ActiveQuerySpec {
 
 	private static final Logger LOG=Logger.getLogger(JEECacheActiveQuerySpec.class.getName());
 	
 	private org.infinispan.Cache<S,T> _cache=null;
 
 	/**
 	 * The Active Query specification constructor.
 	 * 
 	 * @param name The active query name
 	 * @param type The type represented by the active query
 	 * @param internalType Optional type that defines the internal representation
 	 * 				if different from the 'type'
 	 */
 	public JEECacheActiveQuerySpec(String name, Class<?> type, Class<?> internalType) {
 		super(name, type, internalType);
 	}
 	
 	public void setCache(org.infinispan.Cache<S,T> cache) {
 		_cache = cache;
 	}
 	
	@SuppressWarnings("unchecked")
 	public Object resolve(Object source) {
 		Object ret=null;
 		
 		if (_cache != null) {
			ret = _cache.get((S)source);
			/*
 			@SuppressWarnings("unchecked")
 			NotifyingFuture<T> future=_cache.getAsync((S)source);
 			try {
 				if (LOG.isLoggable(Level.FINEST)) {
 					LOG.finest("Get cached object, AQ '"+getName()+"' key '"+source+"'");
 				}
 				ret = future.get(1000, TimeUnit.MILLISECONDS);
 			} catch(Exception e) {
 				future.cancel(false);
 				
 				// Trigger retry rather than waiting on a cache lock
 				throw new RuntimeException("Failed to retrieve cached object, AQ '"+
 							getName()+"' key '"+source+"'");
 			}
			*/
 			
 			if (LOG.isLoggable(Level.FINEST)) {
 				LOG.finest("Got cached object, AQ '"+getName()+"' key '"+source+"' ret="+ret);
 			}
 		}
 		
 		return(ret);
 	}
 }
