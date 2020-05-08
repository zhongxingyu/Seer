 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.redhat.rhevm.api.common.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Cluster;
 import com.redhat.rhevm.api.model.DataCenter;
 import com.redhat.rhevm.api.model.Host;
 import com.redhat.rhevm.api.model.StorageDomain;
 import com.redhat.rhevm.api.model.Template;
 import com.redhat.rhevm.api.model.User;
 import com.redhat.rhevm.api.model.VM;
 import com.redhat.rhevm.api.model.VmPool;
 import com.redhat.rhevm.api.model.Event;
 
 /**
  * A container of static methods related to query resolution.
  */
 public class QueryHelper {
 
     public static final String CONSTRAINT_PARAMETER = "search";
     private static final String RETURN_TYPE_SEPARTOR = " : ";
 
     private QueryHelper() {}
 
     /**
      * Map return types per-collection-class, as there's no logical pattern
      * REVISIT: can we safely just drop the return type specifier?
      * (doesn't seem to have any effect in the powershell case)
      */
     public static Map<Class<?>, String> RETURN_TYPES;
 
     static {
         RETURN_TYPES = new HashMap<Class<?>, String>();
         /**
          * REVISIT: RHEVM Admin Guide is not very clear on whether these
          * return type specifiers should always be pluralized
          */
         RETURN_TYPES.put(VM.class, "VMs" + RETURN_TYPE_SEPARTOR);
         RETURN_TYPES.put(Host.class, "Hosts" + RETURN_TYPE_SEPARTOR);
         RETURN_TYPES.put(Cluster.class, "Clusters" + RETURN_TYPE_SEPARTOR);
         RETURN_TYPES.put(DataCenter.class, "Datacenter" + RETURN_TYPE_SEPARTOR);
         RETURN_TYPES.put(StorageDomain.class, "Storage" + RETURN_TYPE_SEPARTOR);
         RETURN_TYPES.put(Template.class, "Template" + RETURN_TYPE_SEPARTOR);
        RETURN_TYPES.put(User.class, "Users" + RETURN_TYPE_SEPARTOR);
         RETURN_TYPES.put(VmPool.class, "Pools" + RETURN_TYPE_SEPARTOR);
         RETURN_TYPES.put(Event.class, "Events" + RETURN_TYPE_SEPARTOR);
     }
 
     /**
      * Extract constraint from query parameters.
      *
      * @param uriInfo  contains query parameters if set
      * @param clz      the individual return type expected from the query
      * @return         constraint in correct format
      */
     public static String getConstraint(UriInfo uriInfo, Class<?> clz) {
         return getConstraint(uriInfo, null, clz);
     }
 
     /**
      * Extract constraint from query parameters.
      *
      * @param uriInfo  contains query parameters if set
      * @param clz      the individual return type expected from the query
      * @param typePrefix    true if return type prefix is to be included
      * @return         constraint in correct format
      */
     public static String getConstraint(UriInfo uriInfo, Class<?> clz, boolean typePrefix) {
         return getConstraint(uriInfo, null, clz, typePrefix);
     }
 
     /**
      * Extract constraint from query parameters.
      *
      * @param uriInfo       contains query parameters if set
      * @param defaultQuery  raw query to use if not present in URI parameters
      * @param clz           the individual return type expected from the query
      * @return              constraint in correct format
      */
     public static String getConstraint(UriInfo uriInfo, String defaultQuery, Class<?> clz) {
         return getConstraint(uriInfo, defaultQuery, clz, true);
     }
 
     /**
      * Extract constraint from query parameters.
      *
      * @param uriInfo       contains query parameters if set
      * @param defaultQuery  raw query to use if not present in URI parameters
      * @param clz           the individual return type expected from the query
      * @param typePrefix    true if return type prefix is to be included
      * @return              constraint in correct format
      */
     public static String getConstraint(UriInfo uriInfo, String defaultQuery, Class<?> clz, boolean typePrefix) {
         MultivaluedMap<String, String> queries = uriInfo.getQueryParameters();
         String constraint = queries != null
                             && queries.get(CONSTRAINT_PARAMETER) != null
                             && queries.get(CONSTRAINT_PARAMETER).size() > 0
                             ? queries.get(CONSTRAINT_PARAMETER).get(0)
                             : null;
         String prefix = typePrefix ? RETURN_TYPES.get(clz) : "";
         return constraint != null
                ? prefix + constraint
                : defaultQuery != null
                  ? prefix + defaultQuery
                  : null;
     }
 }
