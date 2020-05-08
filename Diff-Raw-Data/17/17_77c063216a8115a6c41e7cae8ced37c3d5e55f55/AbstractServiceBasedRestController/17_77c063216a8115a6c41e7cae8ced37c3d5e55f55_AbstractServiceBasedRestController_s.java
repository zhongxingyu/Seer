 /**
  *
  *
  * Copyright (c) 2007 - 2013 www.Abiss.gr
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package gr.abiss.calipso.controller;
 
 
 import gr.abiss.calipso.jpasearch.data.ParameterMapBackedPageRequest;
 import gr.abiss.calipso.jpasearch.data.RestrictionBackedPageRequest;
 import gr.abiss.calipso.jpasearch.model.FormSchema;
 import gr.abiss.calipso.jpasearch.model.structuredquery.Restriction;
 import gr.abiss.calipso.jpasearch.service.GenericService;
 import gr.abiss.calipso.model.dto.MetadatumDTO;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpServletRequest;
 
 import org.resthub.common.exception.NotFoundException;
 import org.resthub.web.controller.ServiceBasedRestController;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Persistable;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.domain.Sort.Order;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.Assert;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.method.HandlerMethod;
 import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
 import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
 
 import com.wordnik.swagger.annotations.Api;
 import com.wordnik.swagger.annotations.ApiOperation;
 import com.wordnik.swagger.annotations.ApiResponse;
 
 @Controller
 @RequestMapping(produces = { "application/json", "application/xml" })
 @Api(description = "All generic operations for entities", value = "")
 public abstract class AbstractServiceBasedRestController<T extends Persistable<ID>, ID extends Serializable, S extends GenericService<T, ID>>
 		extends
 		ServiceBasedRestController<T, ID, S> {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceBasedRestController.class);
  
 	@Autowired
 	private HttpServletRequest request;
 
 	@Autowired
 	private RequestMappingHandlerMapping requestMappingHandlerMapping;
 
 	/**
 	 * Find all resources matching the given criteria and return a paginated
 	 * collection<br/>
 	 * REST webservice published : GET
 	 * /search?page=0&size=20&properties=sortPropertyName&direction=asc
 	 * 
 	 * @param page
 	 *            Page number starting from 0 (default)
 	 * @param size
 	 *            Number of resources by pages. default to 10
 	 * @return OK http status code if the request has been correctly processed,
 	 *         with the a paginated collection of all resource enclosed in the
 	 *         body.
 	 */
 	@Override
 	@RequestMapping(method = RequestMethod.GET)
 	@ResponseBody
 	@ApiOperation(value = "find (paginated)", notes = "Find all resources matching the given criteria and return a paginated collection", httpMethod = "GET") 
 	public Page<T> findPaginated(
 			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
 			@RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
 			@RequestParam(value = "properties", required = false, defaultValue = "id") String sort,
 			@RequestParam(value = "direction", required = false, defaultValue = "ASC") String direction) {
 
 		Assert.isTrue(page > 0, "Page index must be greater than 0");
 
 		Order order = new Order(
 				direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC
 						: Sort.Direction.DESC, sort);
 		List<Order> orders = new ArrayList<Order>(1);
 		orders.add(order);
 		return this.service.findAll(
 				new ParameterMapBackedPageRequest(request
 				.getParameterMap(), page - 1, size, new Sort(orders)));
 	}
 	
 	
 
     /**
      * {@inheritDoc}
      */
 	@Override
 	@RequestMapping(method = RequestMethod.POST)
     @ResponseStatus(HttpStatus.CREATED)
     @ResponseBody
     @ApiOperation(value = "create", notes = "Create a new resource", httpMethod = "POST")
 	////@ApiResponse(code = 201, message = "created")
 	public T create(@RequestBody T resource) {
 		return super.create(resource);
 	}
 
 
 
     /**
      * {@inheritDoc}
      */
 	@Override
	@RequestMapping(method = RequestMethod.PUT)
     @ResponseBody
     @ApiOperation(value = "update", notes = "Update a resource", httpMethod = "PUT")
 	//@ApiResponse(code = 200, message = "OK")
	public T update(@PathVariable ID id, @RequestBody T resource) {
		// TODO Auto-generated method stub
 		return super.update(id, resource);
 	}
 
     /**
      * {@inheritDoc}
      */
 	@Override
 	@RequestMapping(method = RequestMethod.GET, params="page=no", produces="application/json")
     @ResponseBody
     @ApiOperation(value = "find all", notes = "Find all resources, and return the full collection (i.e. VS a page of the total results)", httpMethod = "GET")
 	//@ApiResponse(code = 200, message = "OK")
 	public Iterable<T> findAll() {
 		return super.findAll();
 	}
 
 
 
 	/**
      * Find a resource by its identifier
      *
      * @param id The identifier of the resouce to find
      * @return OK http status code if the request has been correctly processed, with resource found enclosed in the body
      * @throws NotFoundException
      */
     @RequestMapping(value = "{id}", method = RequestMethod.GET)
     @ResponseBody
     @ApiOperation(value = "find by id", notes = "Find a resource by it's identifier", httpMethod = "GET")
	public T findById(@PathVariable ID id) {
 		return super.findById(id);
 	}
 
 
 
     /**
      * Delete a resource by its identifier. 
      * Return No Content http status code if the request has been correctly processed
      *
      * @param id The identifier of the resource to delete
      * @throws NotFoundException
      */
     @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
     @ResponseStatus(HttpStatus.NO_CONTENT)
     @ApiOperation(value = "delete", notes = "Delete a resource by its identifier. ", httpMethod = "DELETE")
	public void delete(@PathVariable ID id) {
 		// TODO Auto-generated method stub
 		super.delete(id);
 	}
 
 
 
 	/**
 	 * Find all resources matching the given criteria and return a paginated
 	 * collection<br/>
 	 * REST webservice published : GET
 	 * /search?page=0&size=20&properties=sortPropertyName&direction=asc
 	 * 
 	 * @param restriction
 	 *            the structured query as a Restriction instance
 	 * @return OK http status code if the request has been correctly processed,
 	 *         with the a paginated collection of all resource enclosed in the
 	 *         body.
 	 */
 	@RequestMapping(value = "query", produces = { "application/json" }, method = RequestMethod.POST)
 	@ResponseBody
 	@Deprecated
 	@ApiOperation(value = "deprecated: find paginated with restrictions", httpMethod = "GET")
 	public Page<T> findPaginatedWithRestrictions(
 			@RequestBody Restriction restriction) {
 		return this.service.findAll(new RestrictionBackedPageRequest(restriction));
 	}
     
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @Override
 	 * @RequestMapping(method = RequestMethod.GET)
 	 * @ResponseBody public Page<T> findPaginated(@RequestParam(value = "page",
 	 *               required = false, defaultValue = "1") Integer page,
 	 * @RequestParam(value = "size", required = false, defaultValue = "10")
 	 *                     Integer size,
 	 * @RequestParam(value = "direction", required = false, defaultValue =
 	 *                     "ASC") String direction,
 	 * @RequestParam(value = "properties", required = false, defaultValue =
 	 *                     "id") String properties) {
 	 * 
 	 *                     Assert.isTrue(page > 0,
 	 *                     "Page index must be greater than 0");
 	 *                     Assert.isTrue(direction
 	 *                     .equalsIgnoreCase(Sort.Direction.ASC.toString()) ||
 	 *                     direction
 	 *                     .equalsIgnoreCase(Sort.Direction.DESC.toString()),
 	 *                     "Direction should be ASC or DESC");
 	 *                     Assert.notNull(properties);
 	 * 
 	 *                     Page<T> resultPage = this.service .findAll(new
 	 *                     ParameterMapBackedPageRequest( request
 	 *                     .getParameterMap(), page - 1, size, new
 	 *                     Sort(Sort.Direction
 	 *                     .fromString(direction.toUpperCase()),
 	 *                     properties.split(",")))); LOGGER.info("resultPage: "
 	 *                     + resultPage.getClass()); return resultPage; }
 	 */
 	// TODO: refactor to OPTIONS on base path?
 	@RequestMapping(value = "form-schema", produces = { "application/json" }, method = RequestMethod.GET)
 	@ResponseBody
     @ApiOperation(value = "get form schema", notes = "Get a form achema for the controller entity type", httpMethod = "GET")
 	public FormSchema getSchema(
 			@RequestParam(value = "mode", required = false, defaultValue = "search") String mode) {
 		Assert.isTrue(mode == null 
 				|| mode.equalsIgnoreCase("SEARCH") 
 				|| mode.equalsIgnoreCase("CREATE") 
 				|| mode.equalsIgnoreCase("UPDATE"));
 		mode = mode.toUpperCase();
 		try {
 			FormSchema schema = new FormSchema();
 			schema.setDomainClass(
 					((GenericService<Persistable<ID>, ID>) this.service)
 							.getDomainClass());
 			schema.setType(FormSchema.Type.valueOf(mode));
 			return schema;
 		} catch (Exception e) {
 			throw new NotFoundException();
 		}
 	}
 
 	@RequestMapping(produces = { "application/json" }, method = RequestMethod.OPTIONS)
 	@ResponseBody
     @ApiOperation(value = "get form schema", notes = "Get a form achema for the controller entity type", httpMethod = "OPTIONS")
 	public FormSchema getSchemas(
 			@RequestParam(value = "mode", required = false, defaultValue = "search") String mode) {
 		return this.getSchema(mode);
 	}
 
 //	@RequestMapping(value = "apidoc", produces = { "application/json" }, method = {
 //			RequestMethod.GET, RequestMethod.OPTIONS })
 //	@ResponseBody
 //	public List<RestMapping> getRequestMappings() {
 //		List<RestMapping> mappings = new LinkedList<RestMapping>();
 //	    Map<RequestMappingInfo, HandlerMethod> handlerMethods =
 //	                              this.requestMappingHandlerMapping.	getHandlerMethods();
 //
 //	    for(Entry<RequestMappingInfo, HandlerMethod> item : handlerMethods.entrySet()) {
 //	        RequestMappingInfo mapping = item.getKey();
 //	        HandlerMethod method = item.getValue();
 //	        mappings.add(new RestMapping(mapping, method));
 //
 //	        for (String urlPattern : mapping.getPatternsCondition().getPatterns()) {
 //	            System.out.println(
 //	                 method.getBeanType().getName() + "#" + method.getMethod().getName() +
 //	                 " <-- " + urlPattern);
 //
 //	            if (urlPattern.equals("some specific url")) {
 //	               //add to list of matching METHODS
 //	            }
 //	        }
 //	    }       
 //	    return mappings;
 //	}
 
 	// @Secured("ROLE_ADMIN")
 	@RequestMapping(value = "{subjectId}/metadata", method = RequestMethod.PUT)
 	@ResponseBody
     @ApiOperation(value = "add metadatum", notes = "Add or updated a resource metadatum", httpMethod = "GET")
 	public void addMetadatum(@PathVariable ID subjectId,
 			@RequestBody MetadatumDTO dto) {
 		service.addMetadatum(subjectId, dto);
 	}
 
 	// @Secured("ROLE_ADMIN")
 	@RequestMapping(value = "{subjectId}/metadata/{predicate}", method = RequestMethod.DELETE)
 	@ResponseBody
     @ApiOperation(value = "remove metadatum", notes = "Remove a resource metadatum if it exists", httpMethod = "DELETE")
 	public void removeMetadatum(@PathVariable ID subjectId,
 			@PathVariable String predicate) {
 		service.removeMetadatum(subjectId, predicate);
 	}
 }
