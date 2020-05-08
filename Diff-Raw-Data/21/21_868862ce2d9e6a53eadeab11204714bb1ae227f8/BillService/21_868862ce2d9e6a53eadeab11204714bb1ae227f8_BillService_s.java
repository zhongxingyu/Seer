 package com.marbl.administration.backend.service;
 
 //<editor-fold defaultstate="collapsed" desc="Imports">
 import com.marbl.administration.backend.dao.BillDAO;
 import com.marbl.administration.domain.Bill;
 import java.io.Serializable;
 import java.util.ArrayList;
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 //</editor-fold>
 
 @Stateless
 @Path("bills")
 public class BillService implements Serializable {
 
     //<editor-fold defaultstate="collapsed" desc="Fields">
     @Inject
     private BillDAO billDAO;
     //</editor-fold>
 
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public Response create(Bill bill) {
         billDAO.create(bill);
         return Response.status(Response.Status.CREATED).entity(bill).build();
     }
 
     @PUT
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public Response edit(Bill bill) {
         bill = billDAO.edit(bill);
         return Response.status(Response.Status.OK).entity(bill).build();
     }
 
     @DELETE
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public Response remove(Bill bill) {
         billDAO.remove(bill);
         return Response.status(Response.Status.OK).entity(bill).build();
     }
 
     @DELETE
     @Path("{id}")
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.APPLICATION_JSON)
     public Response remove(@PathParam("id") Long id) {
         Bill bill = billDAO.find(id);
         return remove(bill);
     }
 
     @GET
     @Path("{id}")
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.APPLICATION_JSON)
     public Response find(@PathParam("id") Long id) {
         Bill bill = billDAO.find(id);
         return Response.status(Response.Status.OK).entity(bill).build();
     }
 
     @GET
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.APPLICATION_JSON)
     public Response findAll(
             @QueryParam("carTrackerId") String carTrackerId,
             @QueryParam("driverBSN") Integer driverBSN,
             @QueryParam("id") Long id) {
 
         ArrayList<Bill> bills = new ArrayList();

         for (Bill bill : billDAO.findAll()) {
             if (true
                     && (carTrackerId == null || carTrackerId.equals(bill.getCarTrackerId()))
                    && (driverBSN == null || driverBSN == bill.getDriverBSN())
                    && (id == null || id == bill.getID())) {
                 bills.add(bill);
             }
         }
 
         return Response.status(Response.Status.OK).entity(bills).build();
     }
 
     @GET
     @Path("count")
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.TEXT_PLAIN)
     public Response count() {
         String s = String.valueOf(billDAO.count());
         return Response.status(Response.Status.OK).entity(s).build();
     }
 }
