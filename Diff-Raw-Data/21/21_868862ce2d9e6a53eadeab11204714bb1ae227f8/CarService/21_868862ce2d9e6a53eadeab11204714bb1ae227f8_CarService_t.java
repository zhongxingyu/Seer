 package com.marbl.administration.backend.service;
 
 //<editor-fold defaultstate="collapsed" desc="Imports">
 import com.marbl.administration.backend.dao.CarDAO;
 import com.marbl.administration.domain.Car;
 import java.io.Serializable;
 import java.util.ArrayList;
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 //</editor-fold>
 
 @Stateless
 @Path("cars")
 public class CarService implements Serializable {
 
     //<editor-fold defaultstate="collapsed" desc="Fields">
     @Inject
     private CarDAO carDAO;
     //</editor-fold>
 
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public Response create(Car car) {
         carDAO.create(car);
         return Response.status(Response.Status.CREATED).entity(car).build();
     }
 
     @PUT
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public Response edit(Car car) {
         car = carDAO.edit(car);
         return Response.status(Response.Status.OK).entity(car).build();
     }
 
     @DELETE
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public Response remove(Car car) {
         carDAO.remove(car);
         return Response.status(Response.Status.OK).entity(car).build();
     }
 
     @DELETE
     @Path("{carTrackerId}")
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.APPLICATION_JSON)
     public Response remove(@PathParam("carTrackerId") String carTrackerId) {
         Car car = carDAO.find(carTrackerId);
         return remove(car);
     }
 
     @GET
     @Path("{carTrackerId}")
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.APPLICATION_JSON)
     public Response find(@PathParam("carTrackerId") String carTrackerId) {
         Car car = carDAO.find(carTrackerId);
         return Response.status(Response.Status.OK).entity(car).build();
     }
 
     @GET
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.APPLICATION_JSON)
     public Response findAll(
             @QueryParam("carTrackerId") String carTrackerId,
             @QueryParam("driverBSN") Integer driverBSN,
             @QueryParam("licensePlate") String licensePlate) {
 
         ArrayList<Car> cars = new ArrayList();
 
         for (Car car : carDAO.findAll()) {
             if (true
                     && (carTrackerId == null || carTrackerId.equals(car.getCarTrackerId()))
                    && (driverBSN == null || driverBSN.equals(car.getDriverBSN()))
                     && (licensePlate == null || licensePlate.equals(car.getLicensePlate()))) {
                 cars.add(car);
             }
         }
 
         return Response.status(Response.Status.OK).entity(cars).build();
     }
 
     @GET
     @Path("count")
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.TEXT_PLAIN)
     public Response count() {
         String s = String.valueOf(carDAO.count());
         return Response.status(Response.Status.OK).entity(s).build();
     }
 }
