 /*
 Dynamo Web Services is a web service project for administering LucidDB
 Copyright (C) 2010 Dynamo Business Intelligence Corporation
 
 This program is free software; you can redistribute it and/or modify it
 under the terms of the GNU General Public License as published by the Free
 Software Foundation; either version 2 of the License, or (at your option)
 any later version approved by Dynamo Business Intelligence Corporation.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
 package com.dynamobi.ws.api;
 
 import java.util.List;
 
 import javax.jws.WebService;
 import javax.jws.WebMethod;
 import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.annotation.XmlSeeAlso;
 
 import com.dynamobi.ws.domain.Schema;
 import com.dynamobi.ws.domain.Table;
 import com.dynamobi.ws.domain.TableDetails;
 import com.dynamobi.ws.domain.TablesInfo;
 import com.dynamobi.ws.util.AppException;
 
 import javax.annotation.security.PermitAll;
 import javax.annotation.security.RolesAllowed;
 
 /**
  * Service for creating/editing and retrieving table data.
  *
  * @ WebMethod is for soap
  * @ GET is for REST
  * @ Path is for the REST service path
  * @ Consumes is for REST
  * 
  * @author Kevin Secretan
  * @since June 17, 2010
  */
 
 @WebService(serviceName="TableDetailsService", name="TableDetailsService")
 // optional: targetNameSpace = "http://url.com"
 
 public interface TableDetailsService {
 
   /**
    * @param catalog - Catalog to place schema.
    * @param schema - Schema name to create.
    * @return Returns an empty string on success, otherwise an SQL error message.
    */ 
   @WebMethod
   @POST
   @Path("/{catalog}/{schema}")
  @Produces("text/plain")
   public String createSchema(@PathParam("catalog") String catalog,
      @PathParam("schema") String schema) throws AppException;
 
 
   /**
    * @param catalog - Table's catalog.
    * @param schema - Table's schema.
    * @param table - Table's name.
    * @return Returns table and column information on a specified table.
    */
   @WebMethod
   @GET
   @Path("/{catalog}/{schema}/{table}")
   @RolesAllowed( {"Admin","Authenticated"} )
   public TableDetails getTableDetails(@PathParam("catalog") String catalog,
       @PathParam("schema") String schema,
       @PathParam("table") String table) throws AppException;
 
   /**
    * @param catalog - Table's catalog.
    * @param schema - Table's schema.
    * @param table - Table's name.
    * @param details - Data structure of necessary details such as column names
    *        and types.
    * @return Returns true on success.
    */
   @WebMethod
   @POST
   @Path("/{catalog}/{schema}/{table}")
   @RolesAllowed( {"Admin","Authenticated"} )
  @Consumes ( {"application/xml", "application/json"} )
  @Produces("text/plain")
  public boolean postTableDetails(@PathParam("catalog") String catalog,
         @PathParam("schema") String schema,
        @PathParam("table") String table,
        @FormParam("details") TableDetails details);
 
 }
