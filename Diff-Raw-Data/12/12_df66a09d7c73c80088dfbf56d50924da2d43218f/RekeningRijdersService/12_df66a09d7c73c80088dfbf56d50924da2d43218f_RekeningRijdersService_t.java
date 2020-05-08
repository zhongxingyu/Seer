 package com.marbl.rekeningrijders.website.service;
 
 //<editor-fold defaultstate="collapsed" desc="Imports">
 import com.marbl.administration.domain.*;
 import com.marbl.administration.domain.utils.Hasher;
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
 
     private WebResource resource;
 
     @PostConstruct
     public void postConstruct() {
         ClientConfig config = new DefaultClientConfig();
         Client client = Client.create(config);
         resource = client.resource("http://192.168.30.185:8080/AdministrationBackend/resources/");
     }
 
     public void payBill(Long billId) {
         Bill bill = findBill(billId);
         bill.setPaymentDate(new Date());
         bill.setPaymentStatus(PaymentStatus.PAID);
         resource.path("bills").put(Bill.class, bill);
     }
 
     public void editDriver(Driver driver) {
         resource.path("drivers")
                 .accept(MediaType.APPLICATION_JSON).put(Driver.class, driver);
     }
 
     public Driver findDriver(Integer bsn) {
         Driver driver = resource.path("drivers")
                 .path(Integer.toString(bsn)).accept(MediaType.APPLICATION_JSON)
                 .get(Driver.class);
         return driver;
     }
 
     public Driver findDriverByEmail(String email) {
         ArrayList<Driver> drivers = new ArrayList<>(resource
                 .path("drivers").queryParam("email", email)
                 .accept(MediaType.APPLICATION_JSON)
                 .get(new GenericType<Collection<Driver>>() { }));
 
         if (drivers.size() > 0) {
             return drivers.get(0);
         } else {
             return null;
         }
     }
     
     public Driver login(String email, String password) {
         Hasher hasher = new Hasher("SHA-256", "UTF-8");
         password = hasher.hash(password);
         
        return resource.path("drivers").path("login").queryParam("email", email)
                .queryParam("password", password).get(Driver.class);
     }
 
     public void register(Driver driver) {
         resource.path("drivers").post(Driver.class, driver);
     }
 
     public Collection<Bill> findBillsByBSN(Integer bsn) {
         return resource.path("bills")
                 .queryParam("driverBSN", bsn.toString())
                 .accept(MediaType.APPLICATION_JSON)
                 .get(new GenericType<Collection<Bill>>() {
         });
     }
 
     public Bill findBill(Long id) {
         Bill bill = resource.path("bills")
                 .path(Long.toString(id)).accept(MediaType.APPLICATION_JSON)
                 .get(Bill.class);
 
         return bill;
     }
 
     public void editBill(Bill bill) {
         resource.path("bills")
                 .accept(MediaType.APPLICATION_JSON).put(bill);
     }
 
     public Collection<Car> findCarsByBSN(Integer bsn) {
         return resource.path("cars")
                 .queryParam("driverBSN", bsn.toString())
                 .accept(MediaType.APPLICATION_JSON)
                 .get(new GenericType<Collection<Car>>() {
         });
     }
 
     public Car findCar(String carTrackerId) {
         Car car = resource.path("cars").path(carTrackerId)
                 .accept(MediaType.APPLICATION_JSON).get(Car.class);
         return car;
     }
 
     public void editCar(Car car) {
         resource.path("cars").put(Car.class, car);
     }
 }
