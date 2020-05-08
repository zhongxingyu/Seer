 package com.marbl.administration.backend.service;
 
 //<editor-fold defaultstate="collapsed" desc="Imports">
 import com.marbl.administration.backend.dao.DriverDAO;
 import com.marbl.administration.domain.Driver;
 import java.io.Serializable;
 import java.util.ArrayList;
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 //</editor-fold>
 
 @Stateless
 @Path("drivers")
 public class DriverService implements Serializable {
 
     //<editor-fold defaultstate="collapsed" desc="Fields">
     @Inject
     private DriverDAO driverDAO;
     //</editor-fold>
 
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public Response create(Driver driver) {
         driverDAO.create(driver);
         return Response.status(Response.Status.CREATED).entity(driver).build();
     }
 
     @PUT
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public Response edit(Driver driver) {
         driver = driverDAO.edit(driver);
         return Response.status(Response.Status.OK).entity(driver).build();
     }
 
     @DELETE
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public Response remove(Driver driver) {
         driverDAO.remove(driver);
         return Response.status(Response.Status.OK).entity(driver).build();
     }
 
     @DELETE
     @Path("{bsn}")
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.APPLICATION_JSON)
     public Response remove(@PathParam("bsn") Integer bsn) {
         Driver driver = driverDAO.find(bsn);
         return remove(driver);
     }
 
     @GET
     @Path("{bsn}")
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.APPLICATION_JSON)
     public Response find(@PathParam("bsn") Integer bsn) {
         Driver driver = driverDAO.find(bsn);
         return Response.status(Response.Status.OK).entity(driver).build();
     }
 
     @GET
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.APPLICATION_JSON)
     public Response findAll(
             @QueryParam("bsn") Integer bsn,
             @QueryParam("email") String email,
             @QueryParam("firstName") String firstName,
             @QueryParam("lastName") String lastName,
             @QueryParam("residence") String residence) {
         
         ArrayList<Driver> drivers = new ArrayList();
 
         for (Driver driver : driverDAO.findAll()) {
             if (true
                    && (bsn == null || bsn.equals(driver.getBSN()))
                     && (email == null || email.equals(driver.getEmail()))
                     && (firstName == null || firstName.equals(driver.getFirstName()))
                     && (lastName == null || lastName.equals(driver.getLastName()))
                     && (residence == null || residence.equals(driver.getResidence()))) {
                 drivers.add(driver);
             }
         }
 
         return Response.status(Response.Status.OK).entity(drivers).build();
     }
 
     @GET
     @Path("count")
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.TEXT_PLAIN)
     public Response count() {
         String s = String.valueOf(driverDAO.count());
         return Response.status(Response.Status.OK).entity(s).build();
     }
     
     @POST
     @Path("login")
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.APPLICATION_JSON)
     public Response login(
             @QueryParam("email") String email,
             @QueryParam("password") String password) {
         
         for (Driver driver : driverDAO.findAll()) {
             if (driver.getEmail().equals(email)) {
                 if (driver.getPassword().equals(password)) {
                     return Response.status(Response.Status.OK).entity(driver).build();
                 } else {
                     break;
                 }
             }
         }
         
         return Response.status(Response.Status.NOT_FOUND).build();
     }
 }
