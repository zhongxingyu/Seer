 package org.penguin.kayako;
 
 import static com.google.common.collect.Sets.newHashSet;
 import static com.google.common.collect.Sets.union;
 import static java.util.Arrays.asList;
 
 import java.util.List;
 import java.util.Set;
 
 import org.penguin.kayako.ApiRequest.ApiRequestException;
 import org.penguin.kayako.ApiResponse.ApiResponseException;
 import org.penguin.kayako.domain.BasicTicket;
 import org.penguin.kayako.domain.BasicTicketCollection;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 
 /**
  * Wrapper for any API calls specific to tickets
  * 
  * @author raynerw
  * 
  */
 public class TicketConnector {
     private final KayakoClient client;
     
     protected TicketConnector(KayakoClient client) {
         this.client = client;
     }
     
     /**
      * Fetches open tickets that belong to a given department. Tickets are not ordered.
      * 
      * @param departmentId
      *            The identifier of the department your are fetching tickets for.
      * @return An ordered list of tickets that match your given criteria
      * @throws ApiResponseException
      *             A wrapped exception of anything that went wrong when handling the response from kayako.
      * @throws ApiRequestException
      *             A wrapped exception of anything that went wrong sending the request to kayako.
      */
     public List<BasicTicket> forDepartment(int departmentId) throws ApiResponseException, ApiRequestException {
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
     public List<BasicTicket> forDepartment(int departmentId, DepartmentTicketRequest filter) throws ApiResponseException, ApiRequestException {
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
     public List<BasicTicket> forDepartments(Iterable<Integer> departmentIds) throws ApiResponseException, ApiRequestException {
         return forDepartments(departmentIds, DepartmentTicketRequest.where());
     }
     
     /**
      * Fetches open tickets that belong to given departments and match the filter you supply. Tickets are not ordered.
      * 
      * @param departmentIds
      *            A collection of department identifiers.
     * @param A
     *            filter object specifying limiting the scope of the request.
      * @return An ordered list of tickets assigned to the departments you specified.
      * @throws ApiResponseException
     *             A wrapped exception of anything that went wrong when handling the response from kayako.
      * @throws ApiRequestException
     *             A wrapped exception of anything that went wrong sending the request to kayako.
      */
     public List<BasicTicket> forDepartments(Iterable<Integer> departmentIds, DepartmentTicketRequest filter) throws ApiResponseException, ApiRequestException {
         return new ApiRequest(client)
                 .withPath("Tickets")
                 .withPath("Ticket")
                 .withPath("ListAll")
                 .withPathRaw(Joiner.on(',').skipNulls().join(departmentIds))
                 .withPathRaw(Joiner.on(',').join(filter.getTicketStatusIds()))
                 .withPathRaw(Joiner.on(',').join(filter.getOwnerStaffIds()))
                 .withPathRaw(Joiner.on(',').join(filter.getUserIds()))
                 .get().as(BasicTicketCollection.class)
                 .getTickets();
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
 }
