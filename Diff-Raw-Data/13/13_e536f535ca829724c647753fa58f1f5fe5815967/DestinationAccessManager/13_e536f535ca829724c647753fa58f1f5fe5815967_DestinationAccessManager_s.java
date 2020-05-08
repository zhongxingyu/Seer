 /*
  *    GeoTools - The Open Source Java GIS Toolkit
  *    http://geotools.org
  *
  *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
  *
  *    This library is free software; you can redistribute it and/or
  *    modify it under the terms of the GNU Lesser General Public
  *    License as published by the Free Software Foundation;
  *    version 2.1 of the License.
  *
  *    This library is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *    Lesser General Public License for more details.
  */
 package org.geoserver.security.accessmanager;
 
 import org.geoserver.catalog.FeatureTypeInfo;
 import org.geoserver.catalog.LayerGroupInfo;
 import org.geoserver.catalog.LayerInfo;
 import org.geoserver.catalog.ResourceInfo;
 import org.geoserver.catalog.StyleInfo;
 import org.geoserver.catalog.WorkspaceInfo;
 import org.geoserver.security.CatalogMode;
 import org.geoserver.security.DataAccessLimits;
 import org.geoserver.security.LayerGroupAccessLimits;
 import org.geoserver.security.ResourceAccessManager;
 import org.geoserver.security.StyleAccessLimits;
 import org.geoserver.security.VectorAccessLimits;
 import org.geoserver.security.WorkspaceAccessLimits;
 import org.geoserver.security.impl.GeoServerRole;
 import org.geotools.factory.CommonFactoryFinder;
 import org.opengis.filter.Filter;
 import org.opengis.filter.FilterFactory2;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 
 /**
  * Custom ResourceAccessManager for the Destination project.
  * 
  * Support 3 different roles: BASE, MAJOR, SUPER with different
  * access to the siig_mtd_d_elaborazione table.
  * 
  * Each one uses a different filter.
  * 
  * 
  * @author Mauro Bartolomeoli
  *
  */
 public class DestinationAccessManager implements ResourceAccessManager {
 
 	FilterFactory2 ff;
 	
 	public DestinationAccessManager() {
 		super();
 		ff = CommonFactoryFinder.getFilterFactory2();
 	}
 
 	boolean isAdmin(Authentication user) {
         if (user.getAuthorities() != null) {
             for (GrantedAuthority authority : user.getAuthorities()) {
                 final String userRole = authority.getAuthority();
                 if (GeoServerRole.ADMIN_ROLE.getAuthority().equals(userRole) ) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 	
 	boolean hasAuthority(Authentication user, String authorityName) {
         if (user.getAuthorities() != null) {
             for (GrantedAuthority authority : user.getAuthorities()) {
                final String userRole = authority.getAuthority();
                if (authorityName.equalsIgnoreCase(userRole) ) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 	
 	@Override
 	public WorkspaceAccessLimits getAccessLimits(Authentication user,
 			WorkspaceInfo workspace) {
 		return new WorkspaceAccessLimits(CatalogMode.HIDE, true, true);
 	}
 
 	@Override
 	public DataAccessLimits getAccessLimits(Authentication user, LayerInfo layer) {
 		return getAccessLimits(user, layer.getResource());
 	}
 
 	@Override
 	public DataAccessLimits getAccessLimits(Authentication user,
 			ResourceInfo resource) {
 		if(!isAdmin(user)) {
 			if (resource instanceof FeatureTypeInfo) {				
 				FeatureTypeInfo info = (FeatureTypeInfo)resource;
 				// we have only one limited table, siig_mtd_d_elaborazione
 				if(info.getName().equalsIgnoreCase("siig_mtd_d_elaborazione")) {
 					Filter filter;
 					if(hasAuthority(user, "BASE")) {
 						// base users only have access to Elaborazione Standard (id_elaborazione = 1)
 						filter = ff.equals(ff.property("id_elaborazione"), ff.literal(1));
 					} else if(hasAuthority(user, "MAJOR")) {
 						// major users have access to Elaborazione Standard and Personalizzata (id_elaborazione = 1,2)
 						filter = ff.less(ff.property("id_elaborazione"), ff.literal(3));
 					} else if(hasAuthority(user, "SUPER")) {
 						// super users have full access
 						filter = Filter.INCLUDE;
 					} else {
 						// other users have no access
 						filter = Filter.EXCLUDE;
 					}
 				
 					return new VectorAccessLimits(CatalogMode.HIDE, null, filter, null,
 							filter);
 				}
 			}           
         }
         return null;
 	}
 
 	@Override
 	public StyleAccessLimits getAccessLimits(Authentication user,
 			StyleInfo style) {
		return new StyleAccessLimits(CatalogMode.HIDE);
 	}
 
 	@Override
 	public LayerGroupAccessLimits getAccessLimits(Authentication user,
 			LayerGroupInfo layerGroup) {
		return new LayerGroupAccessLimits(CatalogMode.HIDE);
 	}
 
 }
