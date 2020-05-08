 package org.jboss.jdf.example.ticketmonster.rest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ejb.Stateful;
 import javax.enterprise.context.RequestScoped;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.jboss.jdf.example.ticketmonster.model.Allocation;
 import org.jboss.jdf.example.ticketmonster.model.AllocationTicketCategoryCount;
 import org.jboss.jdf.example.ticketmonster.model.Booking;
 import org.jboss.jdf.example.ticketmonster.model.Performance;
 import org.jboss.jdf.example.ticketmonster.model.PriceCategory;
 import org.jboss.jdf.example.ticketmonster.model.Row;
 import org.jboss.jdf.example.ticketmonster.model.Section;
 
 /**
  * @author Marius Bogoevici
  */
 @Path("/bookings")
 @Stateful
 @RequestScoped
 public class BookingService extends BaseEntityService<Booking> {
 
 
     public BookingService() {
         super(Booking.class);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     @DELETE
     @Path("/{id:[0-9][0-9]*}")
     @Consumes(MediaType.APPLICATION_JSON)
     public Response deleteBooking(@PathParam("id") Long id) {
         Booking booking = getEntityManager().find(Booking.class, id);
         if (booking == null) {
             return Response.status(Response.Status.NOT_FOUND).build();
         }
         getEntityManager().remove(booking);
         return Response.ok().build();
     }
 
     @POST
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     public Response createBooking(@FormParam("email") String email,
                                   @FormParam("performance") Long performanceId,
                                   @FormParam("priceCategories") Long[] priceCategoryIds,
                                   @FormParam("sections") Long[] sectionIds,
                                   @FormParam("tickets") String[] ticketCounts) {
         Performance performance = getEntityManager().find(Performance.class, performanceId);
         Booking booking = new Booking();
         booking.setContactEmail(email);
         if (ticketCounts.length != priceCategoryIds.length) {
             Map<String, String> entity = new HashMap<String, String>();
             entity.put("cause", "There must be as many pr as tickets");
             Response response = Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
         }
         Map<Long, Integer> ticketCountsPerSection = new LinkedHashMap<Long, Integer>();
         Map<Long, List<AllocationTicketCategoryCount>> ticketsPerCategory = new LinkedHashMap<Long, List<AllocationTicketCategoryCount>>();
         for (int i = 0; i < ticketCounts.length; i++) {
             if (ticketCounts[i] == null || "".equals(ticketCounts[i].trim()))
                 continue;
             Integer ticketCountAsInteger = Integer.valueOf(ticketCounts[i]);
             if (!ticketCountsPerSection.containsKey(sectionIds[i])) {
                 ticketCountsPerSection.put(sectionIds[i], 0);
                 ticketsPerCategory.put(sectionIds[i], new ArrayList<AllocationTicketCategoryCount> ());
             }
             ticketCountsPerSection.put(sectionIds[i], ticketCountsPerSection.get(sectionIds[i]) + ticketCountAsInteger);
             final PriceCategory priceCategory = getEntityManager().find(PriceCategory.class, priceCategoryIds[i]);
             ticketsPerCategory.get(sectionIds[i]).add(new AllocationTicketCategoryCount(priceCategory.getTicketCategory(), ticketCountAsInteger));
         }
         for (Long sectionId : ticketCountsPerSection.keySet()) {
             int ticketCount = ticketCountsPerSection.get(sectionId);
             if (ticketCount == 0) {
                 continue;
             }
             Section section = getEntityManager().find(Section.class, sectionId);
             Set<Row> rows = section.getSectionRows();
             Allocation createdAllocation = null;
             for (Row row : rows) {
                 List<Allocation> allocations = (List<Allocation>) getEntityManager().createQuery("select a from Allocation a  where a.booking.performance.id = :perfId and a.row.id = :rowId").setParameter("perfId", performanceId).setParameter("rowId", row.getId()).getResultList();
                 if (allocations.size() > 0) {
                     int confirmedCandidate = 0;
                     int nextCandidate = 1;
                     for (Allocation allocation : allocations) {
                         if (allocation.getStartSeat() - nextCandidate >= ticketCount) {
                             confirmedCandidate = nextCandidate;
                             break;
                         }
                         nextCandidate = allocation.getEndSeat() + 1;
                     }
                     if (confirmedCandidate == 0 && ((row.getCapacity() + 1) - nextCandidate) >= ticketCount) {
                         confirmedCandidate = nextCandidate;
                     }
                     if (confirmedCandidate != 0) {
                         createdAllocation = new Allocation();
                         createdAllocation.setRow(row);
                         createdAllocation.setStartSeat(nextCandidate);
                         createdAllocation.setQuantity(ticketCount);
                         createdAllocation.setEndSeat(nextCandidate + ticketCount - 1);
                         break;
                     }
                 } else {
                     createdAllocation = new Allocation();
                     createdAllocation.setRow(row);
                     createdAllocation.setStartSeat(1);
                     createdAllocation.setEndSeat(ticketCount);
                     createdAllocation.setQuantity(ticketCount);
                     break;
                 }
             }
             if (createdAllocation == null) {
                 return Response.status(Response.Status.NOT_MODIFIED).build();
             }
             createdAllocation.setTicketsPerCategory(ticketsPerCategory.get(sectionId));
            System.out.println("Allocating " + createdAllocation.getQuantity() + " tickets ");
            booking.addAllocation(createdAllocation);
         }
         booking.setPerformance(performance);
         booking.setCancellationCode("abc");
         getEntityManager().persist(booking);
         return Response.ok().entity(booking).type(MediaType.APPLICATION_JSON_TYPE).build();
     }
 }
