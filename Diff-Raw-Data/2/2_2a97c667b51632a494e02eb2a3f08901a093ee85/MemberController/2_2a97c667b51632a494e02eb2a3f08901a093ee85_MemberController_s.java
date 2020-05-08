 package org.yajug.users.api;
 
 import java.util.Collections;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.yajug.users.domain.Member;
 import org.yajug.users.domain.utils.MemberComparator;
 import org.yajug.users.service.DataException;
 import org.yajug.users.service.MemberService;
 import org.yajug.users.service.MembershipService;
 import org.yajug.users.vo.GridVo;
 
 import com.google.common.collect.Lists;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.inject.Inject;
 
 /**
  * This controller expose the Member API over HTTP
  * 
  * @author Bertrand Chevrier <bertrand.chevrier@yajug.org>
  */
 @Path("member")
 public class MemberController extends RestController {
 	
 	private final static Logger logger = LoggerFactory.getLogger(MemberController.class);
 
 	private final static List<String> sortableFields = Lists.newArrayList("firstName", "lastName", "email", "company");
 	
 	@Inject private MemberService memberService;
 	@Inject private MembershipService membershipService;
 	
 	/**
 	 * List {@link Member}s regarding some filerting and ordering parameters
 	 * 
 	 * @param callback the JSONP callback
 	 * @param sortName the attribute used to sort
 	 * @param sortOrder the sort direction
 	 * @param search to filter the list based on this expression
 	 * @param page the page to get (based on a paginated list)
 	 * @param rows the rows to get (based on a paginated list)
 	 * @return a JSON representation of the {@link Member}s
 	 */
 	@GET
 	@Path("list")
 	@Produces({MediaType.APPLICATION_JSON})
 	public String list( @QueryParam("callback") String callback,
 						@QueryParam("sortName") String sortName,
 						@QueryParam("sortOrder") String sortOrder,
 						@QueryParam("search") String search,
 						@QueryParam("page") int page,
 						@QueryParam("rows") int rows ) {
 		
 		String response = "";
 		
 		try {
 			List<Member> membersList = null;
 			if(StringUtils.isNotBlank(search)){
 				membersList = Lists.newArrayList(memberService.findAll(search));
 			} else {
 				membersList = Lists.newArrayList(memberService.getAll());
 			}
 			
 			//ordering
 			if(StringUtils.isBlank(sortName) || !sortableFields.contains(sortName)){
				sortName = "lastNname";
 			}
 			if("desc".equalsIgnoreCase(sortOrder)){
 				Collections.sort(membersList, Collections.reverseOrder(new MemberComparator(sortName)));
 			} else {
 				Collections.sort(membersList, new MemberComparator(sortName));
 			}
 			
 			//pagination
 			if(rows <= 0){
 				rows = 25;
 			}
 			if(page <= 0){
 				page = 1;
 			}
 			int start = Math.abs((page-1) * rows);
 			int end = Math.min(membersList.size(), start + rows);
 			int total = membersList.size();
 			
 			logger.debug("rows " + rows);
 			logger.debug("page " + page);
 			logger.debug("start " + start);
 			logger.debug("end " + end);
 			logger.debug("size " + membersList.size());
 			
 			if(start >= 0 && end > start){
 				membersList = membersList.subList(start, end);
 			}
 			
 			logger.debug("final size " + membersList.size());
 			
 			//jsonize
 			response = serializeJsonp(new GridVo(membersList, total), callback);
 			
 		} catch (DataException e) {
 			logger.error(e.getLocalizedMessage(), e);
 			response = serializeException(e);
 		} 
 		return response;
 	}
 	
 	/**
 	 * Search {@link Member}s from a string, used to get autocompleted field
 	 * 
 	 * @param term the string 
 	 * @return a JSON string that contains an array of matching members 
 	 * 			in a particular format : {@code [{label: name, value : key}, ...]
 	 */
 	@GET
 	@Path("acSearch")
 	@Produces({MediaType.APPLICATION_JSON})
 	public String acSearch( @QueryParam("term") String term){
 		String response = "";
 		try {
 			JsonArray array = new JsonArray();
 			
 			if(StringUtils.isNotBlank(term)){
 			
 				for(Member member : memberService.findAll(term)){
 					JsonObject item = new JsonObject();
 					item.addProperty("label", member.getName());
 					item.addProperty("value", member.getKey());
 					array.add(item);
 				}
 			}
 			response = serializer.get().toJson(array);
 		} catch (DataException e) {
 			logger.error(e.getLocalizedMessage(), e);
 			response = serializeException(e);
 		} 
 		return response;
 	}
 	
 	@GET
 	@Path("acCompaniesSearch")
 	@Produces({MediaType.APPLICATION_JSON})
 	public String acCompaniesSearch( @QueryParam("term") String term){
 		String response = "";
 		try {
 			if(StringUtils.isNotBlank(term)){
 				response = serializer.get().toJson(memberService.findCompanies(term));
 			}
 		} catch (DataException e) {
 			logger.error(e.getLocalizedMessage(), e);
 			response = serializeException(e);
 		} 
 		return response;
 	}
 	
 	/**
 	 * Get a {@link Member} from it's identifier 
 	 * 
 	 * @param id the {@link Member} id : {@link Member#getKey()}
 	 * @return a JSON representation of the {@link Member}
 	 */
 	@GET
 	@Path("getOne")
 	@Produces({MediaType.APPLICATION_JSON})
 	public String getOne(@QueryParam("id") String id){
 		String response = "";
 		
 		try {
 			
 			if(StringUtils.isBlank(id)){
 				throw new DataException("Unable to retrieve member from a wrong id");
 			}
 			response = serializer.get().toJson(memberService.getOne(id));
 			
 		} catch (DataException e) {
 			logger.error(e.getLocalizedMessage(), e);
 			response = serializeException(e);
 		} 
 		return response;
 	}
 	
 	/**
 	 * Insert a {@link Member}
 	 * @param member the JSON representation of the {@link Member} to insert
 	 * @return a JSON response : {@code {saved: true|false}
 	 */
 	@PUT
 	@Path("add")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public String add(@FormParam("member") String member){
 		return saveMember(member);
 	}
 	
 	/**
 	 * Update a {@link Member}
 	 * @param member the JSON representation of the {@link Member} to update
 	 * @return a JSON response : {@code {saved: true|false}
 	 */
 	@POST
 	@Path("update")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public String update(@FormParam("member") String member){
 		return saveMember(member);
 	}
 	
 	/**
 	 * Save a {@link Member}
 	 * @param memberData the JSON representation of the {@link Member} to update
 	 * @return a JSON response : {@code {saved: true|false}
 	 */
 	private String saveMember(String memberData){
 		JsonObject response = new JsonObject();
 		boolean saved = false;
 		
 		Member member = serializer.get().fromJson(memberData, Member.class);
 			
 		try {
 			
 			saved = this.memberService.save(member);
 			
 		} catch (DataException e) {
 			logger.error(e.getLocalizedMessage(), e);
 			response.addProperty("error", e.getLocalizedMessage());
 		} 
 		response.addProperty("saved", saved);
 		
 		return serializer.get().toJson(response);
 	}
 	
 	
 	@DELETE
 	@Path("remove/{id}")
 	@Produces({MediaType.APPLICATION_JSON})
 	public String remove(@PathParam("id") String id){
 		JsonObject response = new JsonObject();
 		boolean removed = false;
 		
 		try {
 			
 			if(StringUtils.isBlank(id)){
 				throw new DataException("Unable to remove member from a wrong id");
 			}
 			
 			Member member = memberService.getOne(id);
 			if(member != null){
 				removed = memberService.remove(member);
 			}
 		
 		} catch (DataException e) {
 			logger.error(e.getLocalizedMessage(), e);
 			response.addProperty("error", e.getLocalizedMessage());
 		} 
 		response.addProperty("removed", removed);
 			
 		return serializer.get().toJson(response);
 	}
 }
