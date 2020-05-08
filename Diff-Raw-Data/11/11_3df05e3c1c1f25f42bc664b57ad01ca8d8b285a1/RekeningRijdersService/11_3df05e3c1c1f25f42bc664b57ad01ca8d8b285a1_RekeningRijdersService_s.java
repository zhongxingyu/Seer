 package com.marbl.rekeningrijders.website.service;
 
 //<editor-fold defaultstate="collapsed" desc="Imports">
 import com.marbl.administration.domain.*;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.GenericType;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import javax.annotation.PostConstruct;
 import javax.ejb.Stateless;
 import javax.ws.rs.core.MediaType;
 //</editor-fold>
 
 @Stateless
 public class RekeningRijdersService implements Serializable {
 
     private WebResource service;
 
     @PostConstruct
     public void postConstruct() {
         ClientConfig config = new DefaultClientConfig();
         Client client = Client.create(config);
         service = client.resource("http://localhost:8080/AdministrationBackend/");
     }
 
     public void payBill(Long billId) {
         Bill bill = findBill(billId);
         bill.setPaymentDate(new Date());
         bill.setPaymentStatus(PaymentStatus.PAID);
         service.path("resources").path("bills").put(Bill.class, bill);
     }
 
     public void editDriver(Driver driver) {
         service.path("resources").path("drivers")
                 .accept(MediaType.APPLICATION_JSON).put(Driver.class, driver);
     }
 
     public Driver findDriver(Integer bsn) {
         Driver driver = service.path("resources").path("drivers")
                 .path(Integer.toString(bsn)).accept(MediaType.APPLICATION_JSON)
                 .get(Driver.class);
         return driver;
     }
 
     public Driver findDriverByEmail(String email) {
         Driver driver = new ArrayList<>(service.path("resources")
                 .path("drivers").queryParam("email", email)
                 .accept(MediaType.APPLICATION_JSON)
                 .get(new GenericType<Collection<Driver>>() { })).get(0);
 
         return driver;
     }
 
     public void register(Driver driver) {
         service.path("resources").path("drivers").post(Driver.class, driver);
     }
 
     public Collection<Bill> findBillsByBsn(Integer bsn) {
         return service.path("resources").path("bills")
                 .queryParam("driverBsn", bsn.toString())
                 .accept(MediaType.APPLICATION_JSON)
                 .get(new GenericType<Collection<Bill>>() {
         });
     }
 
     public Bill findBill(Long id) {
        Bill bill = service.path("resources").path("bill")
                 .path(Long.toString(id)).accept(MediaType.APPLICATION_JSON)
                 .get(Bill.class);
 
         return bill;
     }
 
     public void editBill(Bill bill) {
        service.path("resources").path("bill")
                 .accept(MediaType.APPLICATION_JSON).put(bill);
     }
 
     public Collection<Car> findCarsByBsn(Integer bsn) {
         return service.path("resources").path("cars")
                 .queryParam("driverBsn", bsn.toString())
                 .accept(MediaType.APPLICATION_JSON)
                 .get(new GenericType<Collection<Car>>() {
         });
     }
 
     public Car findCar(String carTrackerId) {
        Car car = service.path("resources").path("car").path(carTrackerId)
                 .accept(MediaType.APPLICATION_JSON).get(Car.class);
         return car;
     }
 
     public void editCar(Car car) {
        service.path("resources").path("car").put(Car.class, car);
     }
 }
