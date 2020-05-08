 /**
  * Copyright (C) [2013] [The FURTHeR Project]
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.utah.further.fqe.mpi.ws.api;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 
 import edu.utah.further.core.api.ws.Documentation;
 import edu.utah.further.core.api.ws.ExamplePath;
 import edu.utah.further.fqe.mpi.ws.api.to.IdentifierTo;
 
 /**
  * REST interface for the identifier service.
  * <p>
  * -----------------------------------------------------------------------------------<br>
  * (c) 2008-2013 FURTHeR Project, Health Sciences IT, University of Utah<br>
  * Contact: {@code <further@utah.edu>}<br>
  * Biomedical Informatics, 26 South 2000 East<br>
  * Room 5775 HSEB, Salt Lake City, UT 84112<br>
  * Day Phone: 1-801-581-4080<br>
  * -----------------------------------------------------------------------------------
  * 
  * @author N. Dustin Schultz {@code <dustin.schultz@utah.edu>}
  * @version Jul 8, 2010
  */
@Path("/id")
 @Produces("application/xml")
 @Documentation(name = "FQE WS-REST", description = "Restful identifier service")
 public interface IdentifierServiceRest
 {
 	// ========================= METHODS ================================
 
 	/**
 	 * Generates a new Identifier
 	 * 
 	 * @return a new unique identifier
 	 */
 	@GET
 	@Produces(
 	{ "application/xml" })
 	@Path("/generate/new")
 	@ExamplePath("id/generate/new")
 	@Documentation(name = "Generate new Id", description = "Generates new a unique identifier.")
 	IdentifierTo generateNew();
 
 	/**
 	 * Generates a unique identifier based on the parameters. If an identifier already
 	 * exists for these parameters, the existing identifier is returned.
 	 * 
 	 * @param params
 	 *            Parameters required to generate or retrieve the identifier
 	 * @return the generated or existing identifier
 	 */
 	@GET
 	@Produces(
 	{ "application/xml" })
 	@Path("generate/{name}/{attr}/{srcNamespace}/{srcName}/{srcAttr}/{srcId}/{queryId}")
 	@ExamplePath("/generate/PERSON/PERSON_ID/32776/PATIENT/PAT_DE_ID/12345/862c9130-0e89-11e3-bb9f-f23c91aec05e")
 	@Documentation(name = "Generate Id", description = "Generates a unique identifier or returns an existing one.")
 	IdentifierTo generateId(@PathParam("name") String name,
 			@PathParam("attr") String attr, @PathParam("srcNamespace") Long srcNamespace,
 			@PathParam("srcName") String srcName, @PathParam("srcAttr") String srcAttr,
 			@PathParam("srcId") String srcId, @PathParam("queryId") String queryId);
 }
