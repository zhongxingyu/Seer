 /*
  * Copyright Adele Team LIG
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /**
  * 
  */
 package fr.liglab.adele.cilia.remote.impl;
 import java.text.ParseException;
 import java.util.Map;
 
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Instantiate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.ow2.chameleon.json.JSONService;
 
 import fr.liglab.adele.cilia.AdminBinding;
 import fr.liglab.adele.cilia.AdminChain;
 import fr.liglab.adele.cilia.AdminComponent;
 import fr.liglab.adele.cilia.CiliaContext;
 import fr.liglab.adele.cilia.exceptions.CiliaException;
 import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
 import fr.liglab.adele.cilia.model.Chain;
 import fr.liglab.adele.cilia.model.MediatorComponent;
 
 /**
  * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
  *         Team</a>
  *
  */
 @Component(name="remote-admin-chain")
 @Instantiate(name="remote-admin-chain-0")
 @Provides(specifications={AdminChainREST.class})
 @Path(value="{chainid}")
 public class AdminChainREST {
 
 	@Requires
 	AdminChain admin;
 	
 	@Requires
 	CiliaContext ccontext;
 
 	@Requires
 	AdminComponent acomponent;
 
 	@Requires
 	AdminBinding abinding;
 
 	@Requires
 	private JSONService jsonservice; //JsonService, in order to parse
 
 	
 	public String getChainNames() {
 		boolean atLeastOne = false;
 		StringBuffer chainsIds = new StringBuffer("{ \nchains: [");
 		for (String id : ccontext.getApplicationRuntime().getChainId()) {
 			chainsIds.append(id.trim());
 			chainsIds.append(", ");
 			atLeastOne = true;
 		}
 		if (atLeastOne) {
 			chainsIds.delete(chainsIds.length()-2,chainsIds.length());
 		}
 		chainsIds.append("]\n}");
 		return chainsIds.toString();
 	}
 
 	/**
 	 * Retrieve a mediation chain.
 	 * @param id The ID of the chain  to retrieve 
 	 * @return The required Chain, 
 	 * return <code>null<code> if chain does not exist.
 	 * @throws ParseException 
 	 */
 	@GET
 	@Produces("application/json")
	public Response chain(@PathParam("chainid") String chainid) {
 		if (chainid == null || chainid.length()<1 || chainid.compareToIgnoreCase("cilia")==0){
			return Response.ok(getChainNames()).build();
 		}
 		Chain chain = admin.getChain(chainid);
 		if (chain == null) {
			return Response.status(404).build();
 		} else {
			return Response.ok(String.valueOf(chain)).build();
 		}
		
 	}
 
 	/**
 	 * Create a new initial empty chain chain/
 	 * @param id The ID of the new mediation chain.
 	 * @throws CiliaException if the given chain id already exist.
 	 */
 	@POST
 	@Produces("application/json")
 	public String emptyChain(@PathParam("chainid")String id) {
 		try {
 			admin.createEmptyChain(id);
 		} catch (CiliaException e) {
 			e.printStackTrace();
 			return "{ return : false, " +
 			"exception : " + e.getMessage() +
 			"}";
 		}
 		return "{ return : true}";
 	}
 
 
 
 	/**
 	 * Copy the information of an existing component to another one.
 	 * @param chainId The chain Identification.
 	 * @param source The id of the component source.
 	 * @param destination The id of the component destination.
 	 */
 	@PUT
 	@Path("/copy")
 	@Produces("application/json")
 	public String copyComponentPUT(@PathParam("chainid")String chainId, @FormParam("from")String source, @FormParam("to")String destination) {
 		return copyComponent(chainId, source, destination);
 	}
 	/**
 	 * Copy the information of an existing component to another one.
 	 * @param chainId The chain Identification.
 	 * @param source The id of the component source.
 	 * @param destination The id of the component destination.
 	 */
 	//	@POST
 	//	@Path("/copy")
 	//	@Produces("application/json")
 	public String copyComponent(@PathParam("chainid")String chainId, @FormParam("from")String source, @FormParam("to")String destination) {
 		try {
 			admin.copyComponent(chainId, source, destination);
 		} catch (CiliaException e) {
 			e.printStackTrace();
 			return "{ return : false, " +
 			"exception : " + e.getMessage() +
 			"}";
 		}
 		return "{ return : true}";
 	}
 	/**
 	 * Replace one component for another and copy his data.
 	 * @param chainId The chain to modify.
 	 * @param from the original component.
 	 * @param to The destination component
 	 * @throws CiliaException
 	 */
 	@PUT
 	@Path("/replace")
 	@Produces("application/json")
 	public String replaceComponentPUT(@PathParam("chainid")String chainId, @FormParam("from")String from, @FormParam("to")String to) {
 		return replaceComponent(chainId, from, to);
 	}
 	/**
 	 * Replace one component for another and copy his data.
 	 * @param chainId The chain to modify.
 	 * @param from the original component.
 	 * @param to The destination component
 	 * @throws CiliaException
 	 */
 	//	@POST
 	//	@Path("/replace")
 	//	@Produces("application/json")
 	public String replaceComponent(@PathParam("chainid")String chainId, @FormParam("from")String from, @FormParam("to")String to) {
 		try {
 			admin.replaceComponent(chainId, from, to);
 		} catch (CiliaException e) {
 			e.printStackTrace();
 			return "{ return : false, " +
 			"exception : " + e.getMessage() +
 			"}";
 		}
 		return "{ return : true}";
 	}
 	/**
 	 * Delete a mediation chain. 
 	 * @param id The ID of the chain to be deleted
 	 * @return true if chain is successful deleted, false if it does not exist.
 	 */
 	@DELETE
 	@Produces("application/json")
 	public String deleteChain(@PathParam("chainid")String id) {
 		boolean res = admin.deleteChain(id);
 		if (res){
 			return "{result : true}";
 		}
 		return "{result : false}";
 	}
 
 	/**
 	 * Retrieve a mediation component.
 	 * @param chainid The chain where the component is.
 	 * @param componentId The id of the component
 	 * @return The required component, null if it does not exist.
 	 */
 	@GET
 	@Path("{componentId}")
 	@Produces("application/json")
	public Response getComponent(@PathParam("chainid")String chainid, @PathParam("componentId")String componentId) {
 		MediatorComponent component = acomponent.getComponent(chainid, componentId);
 		StringBuilder result = new StringBuilder();
 		if (component != null) {
 			result.append(component);
 		} else {
			return Response.status(404).build();
 		}
		return Response.ok(result.toString()).build();
 	}
 
 	/**
 	 * Create a new mediator component
 	 * @param chainId The chain where the mediator will be created.
 	 * @param componentType The mediator type.
 	 * @param componentID The id of the mediator.
 	 * @param properties The initial properties.
 	 * @throws CiliaIllegalParameterException If the chain does not exist or if the mediator with the same ID already exist.
 	 */
 	@SuppressWarnings("unchecked")
 	@POST
 	@Path("/mediator/{componentType}/{componentId}")
 	@Produces("application/json")
 	public String createMediator(@PathParam("chainid")String chainId, @PathParam("componentType")String componentType, @PathParam("componentId")String componentID, @FormParam("properties")String  properties) {
 		Map<String, Object> prop = null;
 		System.out.println(properties);
 		StringBuilder result = new StringBuilder();
 		if (properties != null && properties.length()>2){
 			try {
 				prop = jsonservice.fromJSON(properties);
 			} catch (Exception e) {
 				e.printStackTrace();
 				result.append(createMessage(chainId, componentID, "Unable to create mediator"));
 				result.append("{ \"exception\" : \"").append(e.getMessage()).append("\"}");
 				return result.toString();
 			}
 		} 
 		try {
 			acomponent.createMediator(chainId, componentType, componentID, prop);
 		} catch (CiliaException e) {
 			e.printStackTrace();
 			result.append(createMessage(chainId, componentID, "Unable to create mediator"));
 			result.append("{ \"exception\" : \"").append(e.getMessage()).append("\"}");
 			return result.toString();
 		}
 		result.append("{ \"result\" : \"Mediator created successfully\"}");
 		return result.toString();
 	}
 
 
 	/**
 	 * Create a new adapter component
 	 * @param chainId The chain where the adapter will be created.
 	 * @param componentType The adapter type.
 	 * @param componentID The id of the adaper.
 	 * @param properties The initial properties.
 	 * @throws CiliaIllegalParameterException If the chain does not exist or if the adapter with the same ID already exist.
 	 */
 	@SuppressWarnings("unchecked")
 	@POST
 	@Path("adapter/{componentType}/{componentId}")
 	@Produces("application/json")
 	public String createAdapter(@PathParam("chainid")String chainId, @PathParam("componentType")String componentType, @PathParam("componentId")String componentID, @FormParam("properties")String  properties) {
 		Map<String, Object> prop = null;
 		StringBuilder result = new StringBuilder();
 		if (properties != null && properties.length()>2){
 			try {
 				prop = jsonservice.fromJSON(properties);
 			}catch(Exception e){
 				e.printStackTrace();
 				result.append(createMessage(chainId, componentID, "Unable to create adapter"));
 				result.append("{ \"exception\" : \"").append(e.getMessage()).append("\"}");
 				return result.toString();
 			}
 
 		}
 		try {
 			acomponent.createAdapter(chainId, componentType, componentID, prop);
 		} catch (CiliaException e) {
 			e.printStackTrace();
 			result.append(createMessage(chainId, componentID, "Unable to create adapter"));
 			result.append("{ \"exception\" : \"").append(e.getMessage()).append("\"}");
 			return result.toString();
 		}
 		result.append("{ \"result\" : \"Adapter created successfully\"}");
 		return result.toString();
 	}
 
 	/**
 	 * Update a mediator component instance.
 	 * @param chainId The chain id where the component is located.
 	 * @param componentId The ID of the component to reconfigure.
 	 * @param properties The new properties
 	 */
 	@PUT
 	@Path("{componentId}/modify")
 	@Produces("application/json")
 	public String updateComponentPUT(@PathParam("chainid")String chainId, @PathParam("componentId")String componentId, @FormParam("properties")String properties) {
 		return updateComponent(chainId, componentId, properties);
 	}
 	/**
 	 * Update a mediator component instance.
 	 * @param chainId The chain id where the component is located.
 	 * @param componentId The ID of the component to reconfigure.
 	 * @param properties The new properties
 	 */
 	//	@POST
 	//	@Path("{componentId}/modify")
 	//	@Produces("application/json")
 	public String updateComponent(@PathParam("chainid")String chainId, @PathParam("componentId")String componentId, @FormParam("properties")String properties) {
 		System.out.println("Will update component");
 		Map<String, Object> prop = null;
 		StringBuilder result = new StringBuilder("{");
 
 		if (properties != null && properties.length()>2){
 			try {
 				prop = jsonservice.fromJSON(properties);
 			} catch (ParseException e) {
 				e.printStackTrace();
 				result.append(createMessage(chainId, componentId, "Unable to parse properties"));
 				result.append("}");
 				return result.toString();
 			}
 		}
 		if (acomponent.getMediator(chainId, componentId) != null) {
 			try {
 				acomponent.updateMediator(chainId, componentId, prop);
 			} catch (CiliaException e) {
 				e.printStackTrace();
 				result.append(createMessage(chainId, componentId, "Unable to update component"));
 				result.append("}");
 				return result.toString();
 			}
 		} else if (acomponent.getAdapter(chainId, componentId) != null){
 			try {
 				acomponent.updateAdapter(chainId, componentId, prop);
 			} catch (CiliaException e) {
 				e.printStackTrace();
 				result.append(createMessage(chainId, componentId, "Unable to update component"));
 				result.append("}");
 				return result.toString();				
 			}
 		} else { // It does not exist
 			result.append(createMessage(chainId, componentId, "Component does not exist"));
 			result.append("}");
 			return result.toString();
 		}
 		result.append("\"result\" : \"Mediator updated successfully\"}");
 		return result.toString();
 	}
 
 	/**
 	 *Delete a mediator component. 
 	 * @param chainId The chain where the component is.
 	 * @param componentId The id to the component to delete.
 	 * @return true if component is deleted, false if not.
 	 */
 	@DELETE
 	@Path("{componentId}")
 	@Produces("application/json")
 	public String deleteComponent(@PathParam("chainid")String chainId, @PathParam("componentId")String componentId){
 		StringBuilder result = new StringBuilder('{');
 		if (acomponent.getMediator(chainId, componentId) != null) {
 			acomponent.deleteMediator(chainId, componentId);
 			result.append(createMessage(chainId, componentId, "true"));
 		} else if (acomponent.getAdapter(chainId, componentId) != null) {
 			acomponent.deleteAdapter(chainId, componentId);
 			result.append(createMessage(chainId, componentId, "true"));
 		} else {
 			result.append(createMessage(chainId, componentId, "false"));
 		}
 		result.append('}');
 		return result.toString();
 
 	}
 
 	/**
 	 * Create a new binding between two components.
 	 * @param chainId The chain Id where the binding will be created.
 	 * @param from The component which will deliver data. Parameter format must be 
 	 * 			<componentID>:<portName>
 	 * @param to The component which will obtain the data. Parameter format must be 
 	 * 			<componentID>:<portName>
 	 * @param properties The properties if needed to create the binding.
 	 */
 	@POST
 	@Path("bind")
 	public String createBinding(@PathParam("chainid")String chainId, @FormParam("from")String from, @FormParam("to")String to, @FormParam("linker")String linker, @FormParam("properties")String properties) {
 		System.out.println("Will create binding");
 		Map<String, Object> prop = null;
 		StringBuilder result = new StringBuilder("{");
 
 		if (properties != null && properties.length()>2){
 			try {
 				prop = jsonservice.fromJSON(properties);
 			} catch (ParseException e) {
 				e.printStackTrace();
 				result.append(createMessage(chainId, from, "Unable to parse properties"));
 				result.append("exception :").append(e.getMessage());
 				result.append("}");
 				return result.toString();
 			}
 		}
 		try {
 			abinding.createBinding(chainId, from, to, linker, prop);
 		} catch (CiliaException e) {
 			e.printStackTrace();
 			result.append(createMessage(chainId, from, "Unable to bind components"));
 			result.append("exception :").append(e.getMessage());
 			result.append("}");
 			return result.toString();
 		}
 		result.append(createMessage(chainId, from, "Binding successful"));
 		return result.toString();
 	}
 
 
 	/**
 	 * Delete a binding from two mediators.
 	 * @param chainID The chain where mediators are.
 	 * @param from The component which deliver data. Parameter format must be 
 	 * 			<componentID>:<portName>
 	 * @param to The component which receives data. Parameter format must be 
 	 * 			<componentID>:<portName>
 	 */
 	@DELETE
 	@Path("bind")
 	public String deleteBinding(@PathParam("chainid")String chainId, @FormParam("from")String from, @FormParam("to")String to) {
 		StringBuilder result = new StringBuilder("{");
 		try {
 			abinding.deleteBinding(chainId, from, to);
 		} catch (CiliaException e) {
 			e.printStackTrace();
 			result.append(createMessage(chainId, from, "Unable to unbind components"));
 			result.append("exception :").append(e.getMessage());
 			result.append("}");
 			return result.toString();
 		}
 		result.append(createMessage(chainId, from, "UnBinding successful"));
 		return result.toString();	}
 
 
 	public String createMessage(String chainId, String componentId, String result){
 		StringBuilder resultString = new StringBuilder();
 		resultString.append("\"chain\" : \"").append(chainId).append("\",\n");
 		resultString.append("\"component ID\" : \"").append(componentId).append("\",\n");
 		resultString.append("\"result\" : \"").append(result).append("\",\n");
 		return resultString.toString();
 	}
 
 
 }
