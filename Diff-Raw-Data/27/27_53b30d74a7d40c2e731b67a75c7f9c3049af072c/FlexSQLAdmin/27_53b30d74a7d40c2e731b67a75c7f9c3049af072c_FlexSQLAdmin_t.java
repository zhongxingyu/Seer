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
 
 import javax.jws.WebService;
 import javax.jws.WebMethod;
 import javax.annotation.security.PermitAll;
 import javax.annotation.security.RolesAllowed;
 
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.Produces;
 
 import com.dynamobi.ws.domain.*;
 
 /**
  * The implementor is responsible for executing generic queries
  * along with returning XML for the ObjectTree.
  *
  * @author Ray Zhang
  * @since Feb 04,2010
  *
  * @author Kevin Secretan
  * @since June 15, 2010
  * 
  */
 @WebService(serviceName="FlexSQLAdminService", name="FlexSQLAdminService")
 @PermitAll
 public interface FlexSQLAdmin
 {
     /**
      * This will return all schemas, tables, views, and columns
      * for a given catalog.
      *
      * @param connection - Unused
      * @param catalog - Catalog for fetching data.
      * @return Returns an XML string of the metadata.
      */
     @WebMethod
     @GET
     @Path("/getmetadata/{connection}/{catalog}")
     @RolesAllowed( {"Admin","Authenticated"} )
     public String getDBMetaData(@PathParam("connection") String connection,
         @PathParam("catalog") String catalog);
 
     /**
      * This allows support for calling generic queries, including multiple
      * ones separated by semicolons (for example for multiple inserts).
      * Some queries like SELECT return specificly formatted XML, others
      * like DROP do not.
      * @param connection - Unused
      * @param sqlquerytype
      *        This defines whether we just want to perform a raw query
      *        (pass "normal") or if we want to get the execution plan
      *        of a query (pass "showplan"). We currently do not support
      *        getting both at once. 
      * @param sql
      *        The sql query string must be formatted properly for LucidDB
      *        (ANSI-standard SQL).
      * @param toomany - Limit the number of rows returned.
      * @return Returns the result of the query, in String-XML.
      */
     @WebMethod
     @GET
     @Path("/query")
     @RolesAllowed( {"Admin","Authenticated"} )
     public String execSQL(
     		@QueryParam("connection") @DefaultValue("server=;uid=;pwd=;database=;") String connection,
     		@QueryParam("sqlquerytype") @DefaultValue("normal") String sqlquerytype,
     		@QueryParam("sql") String sql,
         @QueryParam("toomany") @DefaultValue("1000") int toomany);
     
     /**
      * Attempts to safely update data by checking for affected rows.
      * @param connection - Unused
      * @param testsql - Query to check that only one row will be affected.
      *                (e.g. a select...where cond)
      * @param sql - The actual query to be performed if only one row would
      *              be affected. (e.g. a delete...where cond)
      * @param toomany - Limit returned rows.
      * @return Returns similar to execSQL.
      */
     @WebMethod
     @GET
     @Path("/update/{connection}/{testsql}/{sql}/{toomany}")
     @RolesAllowed( {"Admin","Authenticated"} )
     public String handleUpdate(@PathParam("connection") String connection,
         @PathParam("testsql") String testsql,
         @PathParam("sql") String sql,
         @PathParam("toomany") int toomany);
 
 
     /**
      * Gets a list of users for the object tree.
      * @return XML-string of users.
      * &lt;users&gt;&lt;user label="name" /&gt;&lt;users /&gt;
      */
     @WebMethod
     @GET
     @Path("/getusers")
     @RolesAllowed( {"Admin","Authenticated"} )
     @Produces("application/xml")
     public XMLStructure getUsers();
 
     /**
      * Gets a list of users for the object tree. (JSON version.)
      * @return JSON-string of users.
      * {"users": ["u1", "u2"]}
      */
     @WebMethod
     @GET
     @Path("/getusers")
     @RolesAllowed( {"Admin","Authenticated"} )
     @Produces("application/json")
     public XMLStructure getUsersJson();
 
     /**
      * Gets a list of roles for the object tree.
      * @return XML-string of roles.
      * &lt;roles&gt;&lt;role label="name" /&gt;&lt;roles /&gt;
      */
     @WebMethod
     @GET
     @Path("/getroles")
     @RolesAllowed( {"Admin","Authenticated"} )
     @Produces("application/xml")
     public XMLStructure getRoles();
 
     /**
      * Gets a list of roles for the object tree.
      * @return JSON-string of roles.
      * {"roles": ["r1", "r2"]}
      */
     @WebMethod
     @GET
     @Path("/getroles")
     @RolesAllowed( {"Admin","Authenticated"} )
     @Produces("application/json")
     public XMLStructure getRolesJson();
 
     /**
      * Gets a list of foreign tables for the object tree.
      * (UNIMPLEMENTED, data returned with normal metadata.)
      * @param schema - Schema to get foreign tables in.
      * @return XML-string of foreign tables.
      * &lt;foreign_tables&gt;&lt;foreign_table label="name" /&gt;&lt;foreign_tables /&gt;
      */
     @WebMethod
     @GET
     @Path("/getforeigntables/{schema}")
     @RolesAllowed( {"Admin","Authenticated"} )
     public String getForeignTables(@PathParam("schema") String schema);
 
     /**
      * Gets a list of functions for the object tree.
      * @param schema - Schema to get functions in.
      * @return XML-string of functions.
      * &lt;functions&gt;&lt;function label="name" /&gt;&lt;functions /&gt;
      */
     @WebMethod
     @GET
     @Path("/getfunctions/{schema}")
     @RolesAllowed( {"Admin","Authenticated"} )
     @Produces("application/xml")
     public XMLStructure getFunctions(@PathParam("schema") String schema);
 
     /**
      * Gets a list of functions for the object tree.
      * @param schema - Schema to get functions in.
      * @return JSON-string of functions.
      * {"functions": [ {"label": "name"} ]}
      */
     @WebMethod
     @GET
     @Path("/getfunctions/{schema}")
     @RolesAllowed( {"Admin","Authenticated"} )
     @Produces("application/json")
     public XMLStructure getFunctionsJson(@PathParam("schema") String schema);
 
     /**
      * Gets a list of procedures for the object tree.
      * @param schema - Schema to get procedures in.
      * @return XML-string of procedures.
      * &lt;procedures&gt;&lt;procedure label="name" /&gt;&lt;procedures /&gt;
      */
     @WebMethod
     @GET
     @Path("/getprocedures/{schema}")
     @RolesAllowed( {"Admin","Authenticated"} )
     @Produces("application/xml")
     public XMLStructure getProcedures(@PathParam("schema") String schema);
 
     /**
      * Gets a list of procedures for the object tree.
      * @param schema - Schema to get procedures in.
      * @return JSON-string of procedures.
      */
     @WebMethod
     @GET
     @Path("/getprocedures/{schema}")
     @RolesAllowed( {"Admin","Authenticated"} )
     @Produces("application/json")
     public XMLStructure getProceduresJson(@PathParam("schema") String schema);
 
     /**
      * Gets a list of foreign data wrappers and servers for the object tree.
      * @return XML-string of foreign data wrappers and servers.
      */
     @WebMethod
     @GET
     @Path("/getremotedata")
     @RolesAllowed( {"Admin", "Authenticated"} )
     public String getRemoteData();
 
     /**
      * Gets a list of jars for a schema's tree.
      * @param schema - Schema to get jars for.
      * @return XML-string of jars.
      */
     @WebMethod
     @GET
     @Path("/getjars/{schema}")
     @RolesAllowed( {"Admin", "Authenticated"} )
     @Produces("application/xml")
     public XMLStructure getJars(@PathParam("schema") String schema);
 
     /**
      * Gets a list of jars for a schema's tree.
      * @param schema - Schema to get jars for.
      * @return JSON-string of jars.
      */
     @WebMethod
     @GET
     @Path("/getjars/{schema}")
     @RolesAllowed( {"Admin", "Authenticated"} )
     @Produces("application/json")
     public XMLStructure getJarsJson(@PathParam("schema") String schema);
 
     /**
      * Gets the DDL for the given schema.
      * @param catalog - Catalog of schema
      * @param schema - Schema to get ddl for.
      * @return Returns a simple &lt;statement&gt; with the string of
      * the ddl as its value.
      */
     @WebMethod
     @GET
     @Path("/ddl/schema/{catalog}/{schema}")
     @RolesAllowed( {"Admin", "Authenticated"} )
    @Produces("application/xml")
     public XMLStructure getSchemaDdl(@PathParam("catalog") String catalog,
         @PathParam("schema") String schema);
 
     /**
      * Gets the DDL for the given schema.
      * @param catalog - Catalog of schema
      * @param schema - Schema to get ddl for.
      * @return Returns a &lt;statement&gt; list with the string of
      * the ddl as its values.
      */
     @WebMethod
     @GET
     @Path("/ddl/schema/{catalog}/{schema}")
     @RolesAllowed( {"Admin", "Authenticated"} )
    @Produces("application/json")
     public XMLStructure getSchemaDdlJson(@PathParam("catalog") String catalog,
         @PathParam("schema") String schema);
 }
