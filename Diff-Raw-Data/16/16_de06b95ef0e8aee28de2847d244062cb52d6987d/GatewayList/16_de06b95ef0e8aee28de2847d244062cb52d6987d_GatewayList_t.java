 /**
  * Copyright (c) 2000-2007, Shakarchi Asaf
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package velo.ejb.seam;
 
 import java.util.Arrays;
 import java.util.List;
 
import javax.annotation.PostConstruct;

 import org.jboss.seam.annotations.Factory;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.framework.EntityQuery;
 
 import velo.entity.Gateway;
 import velo.entity.Gateway.GatewayType;
 
 @Name("gatewayList")
 public class GatewayList extends EntityQuery {
 
 	private static final String[] RESTRICTIONS = {
 		"lower(gateway.uniqueName) like concat(lower(#{gatewayList.gateway.uniqueName}),'%')",
 		"lower(gateway.displayName) like concat(lower(#{gatewayList.gateway.displayName}),'%')",
 		"lower(gateway.description) like concat(lower(#{gatewayList.gateway.description}),'%')",};
 
 	private Gateway gateway = new Gateway();
 	
 	@Override
 	public String getEjbql() {
 		setOrder("displayName ASC");
 		
 		if (super.getEjbql() == null) {
 			return "select gateway from Gateway gateway";
 		}
 		else {
 			return super.getEjbql();
 		}
 	}
 
 	@Override
 	public Integer getMaxResults() {
 		if (super.getMaxResults() != null) {
 			return super.getMaxResults();
 		}
 		else {
 			return 25;
 		}
 	}
 
 	public Gateway getGateway() {
 		return gateway;
 	}
 
 	
 	@Factory("gatewayTypes")
 	public GatewayType[] getGatewayTypes() {
 		return GatewayType.values();
 	}
	
	@PostConstruct
    public void initialize() {
    	setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    	setEjbql(getEjbql());
    }
 }
