 package com.marbl.administration.backend.service;
 
 //<editor-fold defaultstate="collapsed" desc="Imports">
 import com.marbl.administration.backend.dao.*;
 import com.marbl.administration.backend.utils.CarGenerator;
 import com.marbl.administration.backend.utils.DriverGenerator;
 import com.marbl.administration.domain.*;
 import com.marbl.administration.domain.utils.Hasher;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import javax.annotation.PostConstruct;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 import javax.inject.Inject;
 //</editor-fold>
 
 @Singleton
 @Startup
 public class StartupService implements Serializable {
 
     //<editor-fold defaultstate="collapsed" desc="Fields">
     @Inject
     private BillDAO billDAO;
     @Inject
     private CarDAO carDAO;
     @Inject
     private DriverDAO driverDAO;
     @Inject
     private EmployeeDAO employeeDAO;
     @Inject
     private RateDAO rateDAO;
     //</editor-fold>
 
     @PostConstruct
     public void postConstruct() {
         Hasher hasher = new Hasher("SHA-256", "UTF-8");
 
         //<editor-fold defaultstate="collapsed" desc="Employees">
         ArrayList<Employee> employees = new ArrayList<>();
 
         employees.add(new Employee("admin", hasher.hash("admin123"), EmployeeGroup.ADMIN));
         employees.add(new Employee("employee", hasher.hash("employee123"), EmployeeGroup.EMPLOYEE));
         employees.add(new Employee("rate123", hasher.hash("rate123"), EmployeeGroup.RATE_EMPLOYEE));
 
         for (int i = 0; i < employees.size(); i++) {
             employeeDAO.create(employees.get(i));
         }
         //</editor-fold>
 
         //<editor-fold defaultstate="collapsed" desc="Drivers">
         DriverGenerator driverGenerator = new DriverGenerator("marbl.com", hasher,
                 new String[]{"Alexander", "Bas", "Leslie", "Ruud", "Mathijs"},
                 new String[]{"Arends", "Geerts", "Aerts", "Lenders", "Stertefeld"});
 
         Driver[] drivers = driverGenerator.generate();
 
         for (int i = 0; i < drivers.length; i++) {
            System.out.println(drivers[i].getEmail());
             driverDAO.create(drivers[i]);
         }
         //</editor-fold>
 
         //<editor-fold defaultstate="collapsed" desc="Cars">
         CarGenerator carGenerator = new CarGenerator(drivers,
                 new String[] {"Ford", "Suzuki", "Fiat", "Volkswagen"},
                 new String[] {"Mustang", "Swift", "Panda", "Golf"});
 
         Car[] cars = carGenerator.generate(drivers.length * 3 / 2);
 
         for (int i = 0; i < cars.length; i++) {
             carDAO.create(cars[i]);
         }
         //</editor-fold>
 
         //<editor-fold defaultstate="collapsed" desc="Bills">
         ArrayList<Bill> bills = new ArrayList<>();
 
         bills.add(new Bill(1L, new Date(), new Date(), 100, PaymentStatus.CANCELED,
                 drivers[0].getBSN(), cars[0].getCarTrackerId()));
         bills.add(new Bill(2L, new Date(), new Date(), 100, PaymentStatus.PAID,
                 drivers[1].getBSN(), cars[1].getCarTrackerId()));
         bills.add(new Bill(3L, new Date(), new Date(), 100, PaymentStatus.OPEN,
                 drivers[2].getBSN(), cars[2].getCarTrackerId()));
 
         for (int i = 0; i < bills.size(); i++) {
             billDAO.create(bills.get(i));
         }
         //</editor-fold>
 
         //<editor-fold defaultstate="collapsed" desc="CityRates">
         ArrayList<Rate> cityRates = new ArrayList<>();
  
         cityRates.add(new Rate("Den Bosch", Rate.Type.CITY, 1.2));
         cityRates.add(new Rate("Eindhoven", Rate.Type.CITY, 1));
         cityRates.add(new Rate("Tilburg", Rate.Type.CITY, 1.2));
  
         for (int i = 0; i < cityRates.size(); i++) {
             rateDAO.create(cityRates.get(i));
         }
         //</editor-fold>
  
         //<editor-fold defaultstate="collapsed" desc="HighwayRates">
         ArrayList<Rate> highwayRates = new ArrayList<>();
  
         highwayRates.add(new Rate("A2", Rate.Type.HIGHWAY,      new double[]{1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.8, 1.8, 1.8, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.8, 1.8, 1.8, 1.1, 1.1, 1.1, 1.1}));
         highwayRates.add(new Rate("A50", Rate.Type.HIGHWAY,     new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.5, 1.5, 1.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.5, 1.5, 1.5, 1.0, 1.0, 1.0, 1.0}));
         highwayRates.add(new Rate("A67", Rate.Type.HIGHWAY,     new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.5, 1.5, 1.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.5, 1.5, 1.5, 1.0, 1.0, 1.0, 1.0}));
         highwayRates.add(new Rate("A270", Rate.Type.HIGHWAY,    new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}));
  
         for (int i = 0; i < highwayRates.size(); i++) {
             rateDAO.create(highwayRates.get(i));
         }
         //</editor-fold>
  
         //<editor-fold defaultstate="collapsed" desc="RegionRates">
         ArrayList<Rate> regionRates = new ArrayList<>();
  
         regionRates.add(new Rate("Regio Den Bosch", Rate.Type.REGION, 1.2));
         regionRates.add(new Rate("Regio Eindhoven", Rate.Type.REGION, 1));
         regionRates.add(new Rate("Regio Helmond", Rate.Type.REGION, 0.8));
         regionRates.add(new Rate("Regio Tilburg", Rate.Type.REGION, 1.1));
         regionRates.add(new Rate("Regio Weert", Rate.Type.REGION, 0.8));
  
         for (int i = 0; i < regionRates.size(); i++) {
             rateDAO.create(regionRates.get(i));
         }
         //</editor-fold>
  
         //<editor-fold defaultstate="collapsed" desc="VehicleRates">
         ArrayList<Rate> vehicleRates = new ArrayList<>();
  
         vehicleRates.add(new Rate("Unknown", Rate.Type.VEHICLE, 1));
         vehicleRates.add(new Rate("Passenger Car", Rate.Type.VEHICLE, 1));
         vehicleRates.add(new Rate("Van", Rate.Type.VEHICLE, 1.1));
         vehicleRates.add(new Rate("Autobus", Rate.Type.VEHICLE, 1.3));
         vehicleRates.add(new Rate("Truck", Rate.Type.VEHICLE, 1.5));
  
         for (int i = 0; i < vehicleRates.size(); i++) {
             rateDAO.create(vehicleRates.get(i));
         }
         //</editor-fold>
        
         //<editor-fold defaultstate="collapsed" desc="MassRate">
         Rate massRate = new Rate("Massa", Rate.Type.MASS, 0.1);
         //<editor-fold>
     }
 }
