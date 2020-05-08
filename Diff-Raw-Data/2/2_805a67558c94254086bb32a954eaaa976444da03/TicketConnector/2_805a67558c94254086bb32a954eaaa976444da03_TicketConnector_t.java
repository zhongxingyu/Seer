 package org.penguin.kayako;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import org.penguin.kayako.domain.BasicTicket;
 import org.penguin.kayako.domain.BasicTicketCollection;
 import org.penguin.kayako.domain.Ticket;
 import org.penguin.kayako.domain.TicketCollection;
 import org.penguin.kayako.exception.ApiRequestException;
 import org.penguin.kayako.exception.ApiResponseException;
 
 import java.util.List;
 import java.util.Set;
 
 import static com.google.common.collect.Sets.newHashSet;
 import static com.google.common.collect.Sets.union;
 import static java.util.Arrays.asList;
 
 /**
  * Wrapper for any API calls specific to tickets.
  * 
  * @author raynerw
  * @author fatroom
  * 
  */
 public class TicketConnector extends AbstractConnector {
 
     protected TicketConnector(final KayakoClient client) {
         super(client);
     }
 
     /**
      * Fetches open tickets that belong to a given department. Tickets are not ordered.
      * 
      * @param departmentId
      *            The identifier of the department your are fetching tickets for.
      * @return An ordered list of tickets that match your given criteria
      * @throws ApiResponseException
      *             A wrapped exception of anything that went wrong when handling the response from kayako.
      * @throws org.penguin.kayako.exception.ApiRequestException
      *             A wrapped exception of anything that went wrong sending the request to kayako.
      */
     public List<BasicTicket> forDepartment(final int departmentId) throws ApiResponseException, ApiRequestException {
         return forDepartment(departmentId, DepartmentTicketRequest.where());
     }
     
     /**
      * Fetches open tickets that belong to a given department and filter. Tickets are not ordered.
      * 
      * @param departmentId
      *            The identifier of the department your are fetching tickets for.
      * @param filter
      *            A filter object specifying limiting the scope of the request.
      * @return An ordered list of tickets assigned to the department you specified.
      * @throws ApiResponseException
      *             A wrapped exception of anything that went wrong when handling the response from kayako.
      * @throws ApiRequestException
      *             A wrapped exception of anything that went wrong sending the request to kayako.
      */
     public List<BasicTicket> forDepartment(final int departmentId, final DepartmentTicketRequest filter) throws ApiResponseException, ApiRequestException {
         return forDepartments(Lists.newArrayList(departmentId), filter);
     }
     
     /**
      * Fetches open tickets that belong to given departments. Tickets are not ordered.
      * 
      * @param departmentIds
      *            A collection of department identifiers.
      * @return An ordered list of tickets assigned to the departments you specified.
      * @throws ApiResponseException
      *             A wrapped exception of anything that went wrong when handling the response from kayako.
      * @throws ApiRequestException
      *             A wrapped exception of anything that went wrong sending the request to kayako.
      */
     public List<BasicTicket> forDepartments(final Iterable<Integer> departmentIds) throws ApiResponseException, ApiRequestException {
         return forDepartments(departmentIds, DepartmentTicketRequest.where());
     }
     
     /**
      * Fetches open tickets that belong to given departments and match the filter you supply. Tickets are not ordered.
      * 
      * @param departmentIds
      *            A collection of department identifiers.
      * @param filter
      *            A filter object specifying limiting the scope of the request.
      * @return An ordered list of tickets assigned to the departments you specified.
      * @throws ApiResponseException
      *            A wrapped exception of anything that went wrong when handling the response from kayako.
      * @throws ApiRequestException
      *            A wrapped exception of anything that went wrong sending the request to kayako.
      */
     public List<BasicTicket> forDepartments(final Iterable<Integer> departmentIds, final DepartmentTicketRequest filter) throws ApiResponseException, ApiRequestException {
         return getApiRequest()
                 .withPath("ListAll")
                 .withPathRaw(Joiner.on(',').skipNulls().join(departmentIds))
                 .withPathRaw(Joiner.on(',').join(filter.getTicketStatusIds()))
                 .withPathRaw(Joiner.on(',').join(filter.getOwnerStaffIds()))
                 .withPathRaw(Joiner.on(',').join(filter.getUserIds()))
                 .get().as(BasicTicketCollection.class)
                 .getTickets();
     }
 
     /**
      * Fetches ticket identified by ticket id or by ticket mask ID.
      *
      * @param ticketId The unique numeric identifier of the ticket or the ticket mask ID (e.g. ABC-123-4567).
      * @return An ordered list of tickets with specified id.
      * @throws ApiResponseException
      *            A wrapped exception of anything that went wrong when handling the response from kayako.
      * @throws ApiRequestException
      *            A wrapped exception of anything that went wrong sending the request to kayako.
      */
     public List<Ticket> forId(final String ticketId) throws ApiResponseException, ApiRequestException {
         return getApiRequest()
                 .withPathRaw(ticketId)
                 .get().as(TicketCollection.class)
                 .getTickets();
     }
 
     /**
      * Create a new ticket.
      *
      * @param request ticket creation request. {@link TicketCreateRequest}
      * @return collection with an created ticket.
      * @throws ApiResponseException
      *            A wrapped exception of anything that went wrong when handling the response from kayako.
      * @throws ApiRequestException
      *            A wrapped exception of anything that went wrong sending the request to kayako.
      */
     public List<Ticket> createTicket(final TicketCreateRequest request) throws ApiRequestException, ApiResponseException {
         request.validate();
         ApiRequest apiRequest = getApiRequest()
                 .withPostParam("subject", request.getSubject())
                 .withPostParam("fullname", request.getFullname())
                 .withPostParam("email", request.getEmail())
                 .withPostParam("contents", request.getContents())
                 .withPostParam("departmentid", request.getDepartmentId())
                 .withPostParam("ticketstatusid", request.getStatusId())
                 .withPostParam("ticketpriorityid", request.getPriorityId())
                 .withPostParam("tickettypeid", request.getTypeId());
         if (request.getAutoUserId() != null && request.getAutoUserId()) {
             apiRequest = apiRequest.withPostParam("autouserid", 1);
         }
         if (request.getUserId() != null) {
             apiRequest = apiRequest.withPostParam("userid", request.getUserId());
 
         }
         if (request.getStaffId() != null) {
             apiRequest = apiRequest.withPostParam("staffid", request.getStaffId());
         }
         if (request.getOwnerStaffId() != null) {
             apiRequest = apiRequest.withPostParam("ownerstaffid", request.getOwnerStaffId());
         }
         if (request.getType() != null) {
             apiRequest = apiRequest.withPostParam("type", request.getType());
         }
         if (request.getTemplateGroup() != null) {
             apiRequest = apiRequest.withPostParam("templategroup", request.getTemplateGroup());
 
         }
         if (request.getIgnoreAutoresponder() != null && request.getIgnoreAutoresponder()) {
             apiRequest = apiRequest.withPostParam("ignoreautoresponder", 1);
         }
         return apiRequest
                 .post()
                 .as(TicketCollection.class)
                 .getTickets();
     }
 
     /**
      * Update the ticket identified by ticket id.
      *
      * @param ticketId ticket id
      * @param request ticket update request {@link TicketUpdateRequest}
      * @return collection with modified ticket
      * @throws ApiResponseException
      *            A wrapped exception of anything that went wrong when handling the response from kayako.
      * @throws ApiRequestException
      *            A wrapped exception of anything that went wrong sending the request to kayako.
      */
     public List<Ticket> updateTicket(final String ticketId, final TicketUpdateRequest request) throws ApiRequestException, ApiResponseException {
         request.validate();
         ApiRequest apiRequest = getApiRequest()
                 .withPathRaw(ticketId);
         if (request.getSubject() != null) {
             apiRequest = apiRequest.withPostParam("subject", request.getSubject());
         }
         if (request.getFullname() != null) {
             apiRequest = apiRequest.withPostParam("fullname", request.getFullname());
         }
         if (request.getEmail() != null) {
             apiRequest = apiRequest.withPostParam("email", request.getEmail());
         }
         if (request.getDepartmentId() != null) {
             apiRequest = apiRequest.withPostParam("departmentid", request.getDepartmentId());
         }
         if (request.getStatusId() != null) {
             apiRequest = apiRequest.withPostParam("ticketstatusid", request.getStatusId());
         }
         if (request.getPriorityId() != null) {
             apiRequest = apiRequest.withPostParam("ticketpriorityid", request.getPriorityId());
         }
         if (request.getTypeId() != null) {
             apiRequest = apiRequest.withPostParam("tickettypeid", request.getTypeId());
         }
         if (request.getOwnerStaffId() != null) {
             apiRequest = apiRequest.withPostParam("ownerstaffid", request.getOwnerStaffId());
         }
         if (request.getUserId() != null) {
             apiRequest = apiRequest.withPostParam("userid", request.getUserId());
         }
         if (request.getTemplateGroup() != null) {
             apiRequest = apiRequest.withPostParam("templategroup", request.getTemplateGroup());
         }
         return apiRequest
                 .put()
                 .as(TicketCollection.class)
                 .getTickets();
     }
 
     /**
      * Delete the ticket identified by ticket id.
      *
      * @param ticketId The unique numeric identifier of the ticket.
      * @throws ApiRequestException
      *            A wrapped exception of anything that went wrong sending the request to kayako.
      * @throws ApiResponseException
      *            A wrapped exception of anything that went wrong when handling the response from kayako.
      */
     public void delete(String ticketId) throws ApiRequestException, ApiResponseException {
         getApiRequest()
                 .withPathRaw(ticketId)
                 .delete();
     }
 
     @Override
     protected ApiRequest getApiRequest() {
         ApiRequest request = super.getApiRequest();
         return request
                 .withPath("Tickets")
                 .withPath("Ticket");
     }
 
     public static class DepartmentTicketRequest {
         private final Set<Integer> ticketStatusIds;
         private final Set<Integer> ownerStaffIds;
         private final Set<Integer> userIds;
         
         private DepartmentTicketRequest(Set<Integer> ticketStatusIds, Set<Integer> ownerStaffIds, Set<Integer> userIds) {
             this.ticketStatusIds = ticketStatusIds;
             this.ownerStaffIds = ownerStaffIds;
             this.userIds = userIds;
         }
         
         public static DepartmentTicketRequest where() {
             return new DepartmentTicketRequest(null, null, null);
         }
         
         public DepartmentTicketRequest ticketStatusId(Integer... ticketStatusIds) {
             return new DepartmentTicketRequest(addToSet(this.ticketStatusIds, ticketStatusIds), ownerStaffIds, userIds);
         }
         
         public DepartmentTicketRequest ownerStaffId(Integer... ownerStaffIds) {
             return new DepartmentTicketRequest(ticketStatusIds, addToSet(this.ownerStaffIds, ownerStaffIds), userIds);
         }
         
         public DepartmentTicketRequest userId(Integer... userIds) {
             return new DepartmentTicketRequest(ticketStatusIds, ownerStaffIds, addToSet(this.userIds, userIds));
         }
         
         private static final Set<Integer> EMPTY_SET = ImmutableSet.<Integer> builder().add(-1).build();
         
         private Set<Integer> getTicketStatusIds() {
             if (ticketStatusIds == null || ticketStatusIds.size() == 0) {
                 return EMPTY_SET;
             }
             return ticketStatusIds;
         }
         
         private Set<Integer> getOwnerStaffIds() {
             if (ownerStaffIds == null || ownerStaffIds.size() == 0) {
                 return EMPTY_SET;
             }
             return ownerStaffIds;
         }
         
         private Set<Integer> getUserIds() {
             if (userIds == null || userIds.size() == 0) {
                 return EMPTY_SET;
             }
             return userIds;
         }
         
         private static Set<Integer> addToSet(Set<Integer> set, Integer... values) {
             if (set == null) {
                 return newHashSet(asList(values));
             }
             return union(set, newHashSet(asList(values)));
         }
     }
 
     public static class TicketCreateRequest extends AbstractRequest {
         public enum TICKETTYPE {
             DEFAULT("default"),
             PHONE("phone");
 
             private String representation;
 
             private TICKETTYPE(String representation) {
                 this.representation = representation;
             }
 
             @Override
             public String toString() {
                 return representation;
             }
         }
 
         private String subject;
         private String fullname;
         private String email;
         private String contents;
         private Integer departmentId;
         private Integer statusId;
         private Integer priorityId;
         private Integer typeId;
 
         private Boolean autoUserId;
         private Integer userId;
         private Integer staffId;
 
         private Integer ownerStaffId;
         private TICKETTYPE type;
         private String  templateGroup;
         private Boolean ignoreAutoresponder;
 
         private TicketCreateRequest() {
         }
 
         private TicketCreateRequest(TicketCreateRequest request) {
             this.subject = request.getSubject();
             this.fullname = request.getFullname();
             this.email = request.getEmail();
            this.contents = request.getContents();
             this.departmentId = request.getDepartmentId();
             this.statusId = request.getStatusId();
             this.priorityId = request.getPriorityId();
             this.typeId = request.getTypeId();
             this.autoUserId = request.getAutoUserId();
             this.userId = request.getUserId();
             this.staffId = request.getStaffId();
             this.ownerStaffId = request.getOwnerStaffId();
             this.type = request.type;
             this.templateGroup = request.getTemplateGroup();
             this.ignoreAutoresponder = request.getIgnoreAutoresponder();
         }
 
         public static TicketCreateRequest where() {
             return new TicketCreateRequest();
         }
 
         /**
          * The Ticket Subject.
          *
          * @param subject subject
          * @return instance of request
          * @throws ApiRequestException in case subject is null
          */
         public TicketCreateRequest subject(String subject) throws ApiRequestException {
             checkNotNull(subject);
             TicketCreateRequest request = new TicketCreateRequest(this);
             request.subject = subject;
             return request;
         }
 
         /**
          * Full name of ticket creator.
          *
          * @param fullname full name
          * @return instance of request
          * @throws ApiRequestException if case subject is null
          */
         public TicketCreateRequest fullname(String fullname) throws ApiRequestException {
             checkNotNull(fullname);
             TicketCreateRequest request = new TicketCreateRequest(this);
             request.fullname = fullname;
             return request;
         }
 
         /**
          * Email address of ticket creator.
          *
          * @param email address
          * @return instance of request
          * @throws ApiRequestException in case email is null
          */
         public TicketCreateRequest email(String email) throws ApiRequestException {
             checkNotNull(email);
             TicketCreateRequest request = new TicketCreateRequest(this);
             request.email = email;
             return request;
         }
 
         /**
          * The contents of the first ticket post.
          *
          * @param contents contents of the first ticket post
          * @return instance of request
          * @throws ApiRequestException in case contents is null
          */
         public TicketCreateRequest contents(String contents) throws ApiRequestException {
             checkNotNull(contents);
             TicketCreateRequest request = new TicketCreateRequest(this);
             request.contents = contents;
             return request;
         }
 
         /**
          * The department id.
          *
          * @param id department id
          * @return instance of request
          */
         public TicketCreateRequest departmentId(int id) {
             TicketCreateRequest request = new TicketCreateRequest(this);
             request.departmentId = id;
             return request;
         }
 
         /**
          * The ticket status id.
          *
          * @param id status id
          * @return instance of request
          */
         public TicketCreateRequest ticketStatusId(int id) {
             TicketCreateRequest request = new TicketCreateRequest(this);
             request.statusId = id;
             return request;
         }
 
         /**
          * The ticket priority id.
          *
          * @param id priority id
          * @return instance of request
          */
         public TicketCreateRequest ticketPriorityId(int id) {
             TicketCreateRequest request = new TicketCreateRequest(this);
             request.priorityId = id;
             return request;
         }
 
         /**
          * The ticket type id.
          *
          * @param id type id
          * @return instance of request
          */
         public TicketCreateRequest ticketTypeId(int id) {
             TicketCreateRequest request = new TicketCreateRequest(this);
             request.typeId = id;
             return request;
         }
 
         /**
          * If dispatched then User ID is looked up based on the email address.
          * If none is found, the system ends up creating a new user based on the information supplied.
          *
          * @return instance of request
          * @throws ApiRequestException in case ticket creator user id or ticket creator staff id explicitly set in this request instance.
          */
         public TicketCreateRequest autoCreateUser() throws ApiRequestException {
             if (userId != null) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration"));
             }
             if (staffId != null) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration"));
             }
             TicketCreateRequest request = new TicketCreateRequest(this);
             request.autoUserId = true;
             return request;
         }
 
         /**
          * The user id, if the ticket is to be created as a user.
          *
          * @param id user id
          * @return instance of request
          * @throws ApiRequestException in case automatic user id lookup or ticket creator staff id explicitly set in this request instance.
          */
         public TicketCreateRequest userId(int id) throws ApiRequestException {
             if (autoUserId != null && autoUserId) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration"));
             }
             if (staffId != null) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration"));
             }
             TicketCreateRequest request = new TicketCreateRequest(this);
             request.userId = id;
             return request;
         }
 
         /**
          * The staff id, if the ticket is to be created as a staff.
          *
          * @param id staff id
          * @return request instance
          * @throws ApiRequestException in case automatic user id lookup or ticket creator user id explicitly set in this request instance.
          */
         public TicketCreateRequest staffId(int id) throws ApiRequestException {
             if (autoUserId != null && autoUserId) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration"));
             }
             if (userId != null) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration"));
             }
             TicketCreateRequest request = new TicketCreateRequest(this);
             request.staffId = id;
             return request;
         }
 
         /**
          * The owner staff id, if you want to set an Owner for this ticket.
          * Optional parameter.
          *
          * @param id staff id
          * @return request instance
          */
         public TicketCreateRequest ownerStaffId(int id) {
             TicketCreateRequest request = new TicketCreateRequest(this);
             request.ownerStaffId = id;
             return request;
         }
 
         /**
          * The ticket type. Check for available types {@link TICKETTYPE}.
          * Optional parameter.
          *
          * @param type type
          * @return request instance
          */
         public TicketCreateRequest type(TICKETTYPE type) {
             TicketCreateRequest request = new TicketCreateRequest(this);
             request.type = type;
             return request;
         }
 
         /**
          * The custom template group identifier (ID or Name) for the Ticket.
          * Optional parameter.
          *
          * @param templateGroup identifier
          * @return request instance
          */
         public TicketCreateRequest templateGroup(String templateGroup) {
             TicketCreateRequest request = new TicketCreateRequest(this);
             request.templateGroup = templateGroup;
             return request;
         }
 
         /**
          * Option to disable autoresponder email.
          *
          * @return request instance
          */
         public TicketCreateRequest ignoreAutoresponder() {
             TicketCreateRequest request = new TicketCreateRequest(this);
             request.ignoreAutoresponder = true;
             return request;
         }
 
         protected void validate() {
             if (subject == null) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration. Ticket subject is required"));
             }
             if (fullname == null) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration. Ticket creator full name is required"));
             }
 
             if (email == null) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration. Ticket creator email is required"));
             }
 
             if (contents == null) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration. Ticket contents is required"));
             }
 
             if (departmentId == null) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration. Ticket department id is required"));
             }
 
             if (statusId == null) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration. Ticket status id is required"));
             }
 
             if (priorityId == null) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration. Ticket priority id is required"));
             }
 
             if (typeId == null) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration. Ticket type id is required"));
             }
             if ((autoUserId == null || !autoUserId) && (userId == null) && (staffId == null) ) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration. Ticket creator id should be specified or allow auto lookup"));
             }
         }
 
         private String getSubject() {
             return subject;
         }
 
         private String getFullname() {
             return fullname;
         }
 
         private String getEmail() {
             return email;
         }
 
         private String getContents() {
             return contents;
         }
 
         private Integer getDepartmentId() {
             return departmentId;
         }
 
         private Integer getStatusId() {
             return statusId;
         }
 
         private Integer getPriorityId() {
             return priorityId;
         }
 
         private Integer getTypeId() {
             return typeId;
         }
 
         private Boolean getAutoUserId() {
             return autoUserId;
         }
 
         private Integer getUserId() {
             return userId;
         }
 
         private Integer getStaffId() {
             return staffId;
         }
 
         private Integer getOwnerStaffId() {
             return ownerStaffId;
         }
 
         private String getType() {
             if (type == null) {
                 return null;
             }
             return type.toString();
         }
 
         private String getTemplateGroup() {
             return templateGroup;
         }
 
         private Boolean getIgnoreAutoresponder() {
             return ignoreAutoresponder;
         }
     }
 
     public static class TicketUpdateRequest extends AbstractRequest {
         private String subject;
         private String fullname;
         private String email;
         private Integer departmentId;
         private Integer statusId;
         private Integer priorityId;
         private Integer typeId;
         private Integer ownerStaffId;
         private Integer userId;
         private String templateGroup;
 
         private boolean initialized;
 
         private TicketUpdateRequest() {
         }
 
         private TicketUpdateRequest(TicketUpdateRequest request) {
             this.subject = request.getSubject();
             this.fullname = request.getFullname();
             this.email = request.getFullname();
             this.departmentId = request.getDepartmentId();
             this.statusId = request.getStatusId();
             this.priorityId = request.getPriorityId();
             this.typeId = request.getPriorityId();
             this.ownerStaffId = request.getOwnerStaffId();
             this.userId = request.getUserId();
             this.templateGroup = request.getTemplateGroup();
         }
 
         public static TicketUpdateRequest where() {
             return new TicketUpdateRequest();
         }
 
         /**
          * Update ticket subject field.
          *
          * @param subject new value
          * @return request instance
          */
         public TicketUpdateRequest subject(final String subject) {
             TicketUpdateRequest request = new TicketUpdateRequest(this);
             request.initialized = true;
             request.subject = subject;
             return request;
         }
 
         /**
          * Update full name of ticket creator.
          *
          * @param fullname new value
          * @return request instance
          */
         public TicketUpdateRequest fullname(final String fullname) {
             TicketUpdateRequest request = new TicketUpdateRequest(this);
             request.initialized = true;
             request.fullname = fullname;
             return request;
         }
 
         /**
          * Update email address of ticket creator.
          *
          * @param email new value
          * @return request instance
          */
         public TicketUpdateRequest email(final String email) {
             TicketUpdateRequest request = new TicketUpdateRequest(this);
             request.initialized = true;
             request.email = email;
             return request;
         }
 
         /**
          * Update department id field of ticket.
          *
          * @param id new value
          * @return request instance
          */
         public TicketUpdateRequest departmentId(final int id) {
             TicketUpdateRequest request = new TicketUpdateRequest(this);
             request.initialized = true;
             request.departmentId = id;
             return request;
         }
 
         /**
          * Update status id field of ticket.
          *
          * @param id new value
          * @return request instance
          */
         public TicketUpdateRequest statusId(final int id) {
             TicketUpdateRequest request = new TicketUpdateRequest(this);
             request.initialized = true;
             request.statusId = id;
             return request;
         }
 
         /**
          * Update priority id field of ticket.
          *
          * @param id new value
          * @return request instance
          */
         public TicketUpdateRequest priorityId(final int id) {
             TicketUpdateRequest request = new TicketUpdateRequest(this);
             request.initialized = true;
             request.priorityId = id;
             return request;
         }
 
         /**
          * Update type id field of ticket.
          *
          * @param id new value
          * @return request instance
          */
         public TicketUpdateRequest typeId(final int id) {
             TicketUpdateRequest request = new TicketUpdateRequest(this);
             request.initialized = true;
             request.typeId = id;
             return request;
         }
 
         /**
          * Update owner staff id field of ticket.
          *
          * @param id new value
          * @return request instance
          */
         public TicketUpdateRequest ownerStaffId(final int id) {
             TicketUpdateRequest request = new TicketUpdateRequest(this);
             request.initialized = true;
             request.ownerStaffId = id;
             return request;
         }
 
         /**
          * Update user id field of ticket.
          *
          * @param id new value
          * @return request instance
          */
         public TicketUpdateRequest userId(final int id) {
             TicketUpdateRequest request = new TicketUpdateRequest(this);
             request.initialized = true;
             request.userId = id;
             return request;
         }
 
         /**
          * Update custom template group identifier (ID or Name) for the ticket.
          *
          * @param templateGroup new value
          * @return request instance
          */
         public TicketUpdateRequest templateGroup(final String templateGroup) {
             TicketUpdateRequest request = new TicketUpdateRequest(this);
             request.initialized = true;
             request.templateGroup = templateGroup;
             return request;
         }
 
         protected void validate() throws ApiRequestException {
             if (!initialized) {
                 throw new ApiRequestException(new IllegalStateException("Invalid request configuration. At least one field for update should be specified"));
             }
         }
 
         private String getSubject() {
             return subject;
         }
 
         private String getFullname() {
             return fullname;
         }
 
         private String getEmail() {
             return email;
         }
 
         private Integer getDepartmentId() {
             return departmentId;
         }
 
         private Integer getStatusId() {
             return statusId;
         }
 
         private Integer getPriorityId() {
             return priorityId;
         }
 
         private Integer getTypeId() {
             return typeId;
         }
 
         private Integer getOwnerStaffId() {
             return ownerStaffId;
         }
 
         private Integer getUserId() {
             return userId;
         }
 
         private String getTemplateGroup() {
             return templateGroup;
         }
     }
 }
