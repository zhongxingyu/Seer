 package carrental;
 
 import java.util.ArrayList;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 import java.util.Date;
 import java.sql.Timestamp;
import java.util.Calendar;
 
 /**
  * The model keeps track of the logic of CarRental
  * @author CNN
  * @version 2011-12-02
  */
 public class Model {
     private DbCom database;
     private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.0");
     
     public Model() {
         CarRental.getInstance().appendLog("Creating model...");
         database = new DbCom();
     }
     
     //VEHICLE
     
     /**
      * Get a vehicle matching an id
      * @param id
      * @return Vehicle
      */
     public Vehicle getVehicle(int id) {
         ArrayList<String> v = database.getFirstMatch("SELECT * FROM vehicle WHERE id = '"+id+"'");
         return new Vehicle(Integer.parseInt(v.get(0)),Integer.parseInt(v.get(1)),v.get(3),v.get(4),v.get(2),Integer.parseInt(v.get(5)),v.get(6));
     }
     
     /**
      * Get an array of all vehicles in the database
      * @return Array of vehicles
      */
     public ArrayList<Vehicle> getVehicles() {
         ArrayList<ArrayList<String>> vs = database.getMatches("SELECT * FROM vehicle ORDER BY type,id DESC");
         ArrayList<Vehicle> results = new ArrayList<>();
         for(ArrayList<String> v : vs) {
             results.add(new Vehicle(Integer.parseInt(v.get(0)),Integer.parseInt(v.get(1)),v.get(3),v.get(4),v.get(2),Integer.parseInt(v.get(5)),v.get(6)));
         }
         return results;
     }
     
     /**
      * Save a vehicle to the database
      * @param v the vehicle to save
      */
     public void saveVehicle(Vehicle v) {
         ArrayList<String> save_data = new ArrayList<>();
         save_data.add(Integer.toString(v.getID()));
         save_data.add(Integer.toString(v.getVehicleType()));
         save_data.add(v.getVIN());
         save_data.add(v.getDescription());
         save_data.add(v.getLicensplate());
         save_data.add(Integer.toString(v.getOdo()));
         save_data.add(v.getAdditional());
         database.saveArray("vehicle", save_data);
     }
     
     //VEHICLE TYPE
     
     /**
      * Get a vehicle type by it's id
      * @param id
      * @return VehicleType
      */
     public VehicleType getVehicleType(int id) {
         ArrayList<String> vt = database.getFirstMatch("SELECT * FROM vehicletype WHERE id = '"+id+"'");
         return new VehicleType(Integer.parseInt(vt.get(0)),vt.get(1),vt.get(2),Integer.parseInt(vt.get(3)));
     }
     
     /**
      * Get a vehicle type by it's name
      * @param name
      * @return VehicleType
      */
     public VehicleType getVehicleType(String name) {
         ArrayList<String> vt = database.getFirstMatch("SELECT * FROM vehicletype WHERE name = '"+name+"'");
         return new VehicleType(Integer.parseInt(vt.get(0)),vt.get(1),vt.get(2),Integer.parseInt(vt.get(3)));
     }
     
     /**
      * Get an array of all vehicle types in the database ordered by name
      * @return Array of vehicletypes
      */
     public ArrayList<VehicleType> getVehicleTypes() {
         ArrayList<ArrayList<String>> vts = database.getMatches("SELECT * FROM vehicletype ORDER BY name ASC");
         ArrayList<VehicleType> results = new ArrayList<>();
         for(ArrayList<String> vt : vts) {
             results.add(new VehicleType(Integer.parseInt(vt.get(0)),vt.get(1),vt.get(2),Integer.parseInt(vt.get(3))));
         }
         return results;
     }
     
     /**
      * Save a vehicletype to the database
      * @param vt the vehicletype to save
      */
     public void saveVehicleType(VehicleType vt) {
         ArrayList<String> save_data = new ArrayList<>();
         save_data.add(Integer.toString(vt.getID()));
         save_data.add(vt.getName());
         save_data.add(vt.getDescription());
         save_data.add(Integer.toString(vt.getPricePerDay()));
         database.saveArray("vehicletype", save_data);
     }
     
     //CUSTOMER
     
     /**
      * Get a customer by it's id
      * @param id
      * @return Customer
      */
     public Customer getCustomer(int id) {
         ArrayList<String> c = database.getFirstMatch("SELECT * FROM customer WHERE id = '"+id+"'");
         return new Customer(Integer.parseInt(c.get(0)),Integer.parseInt(c.get(1)),c.get(2),c.get(3),c.get(4));
     }
     
     /**
      * Get a customer by it's phonenumber, only gets the first customer matching the number.
      * @param phonenumber phone number of customer
      * @return Customer
      */
     public Customer getCustomerByPhone(int phonenumber) {
         ArrayList<String> c = database.getFirstMatch("SELECT * FROM customer WHERE telephone = '"+phonenumber+"' LIMIT 1");
         return new Customer(Integer.parseInt(c.get(0)),Integer.parseInt(c.get(1)),c.get(2),c.get(3),c.get(4));
     }
     
     /**
      * Get an array of all customers in the database ordered by phone number and name
      * @return Array of customers
      */
     public ArrayList<Customer> getCustomers() {
         ArrayList<ArrayList<String>> cs = database.getMatches("SELECT * FROM customer ORDER BY telephone,name DESC");
         ArrayList<Customer> results = new ArrayList<>();
         for(ArrayList<String> c : cs) {
             results.add(new Customer(Integer.parseInt(c.get(0)),Integer.parseInt(c.get(1)),c.get(2),c.get(3),c.get(4)));
         }
         return results;
     }
     
     /**
      * Save a customer to the database
      * @param c the customer to save
      */
     public void saveCustomer(Customer c) {
         ArrayList<String> save_data = new ArrayList<>();
         save_data.add(Integer.toString(c.getID()));
         save_data.add(Integer.toString(c.getTelephone()));
         save_data.add(c.getName());
         save_data.add(c.getAdress());
         save_data.add(c.getEMail());
         database.saveArray("customer", save_data);
     }
     
     //RESERVATION
     
     /**
      * Get a reservation by it's id
      * @param id
      * @return Reservation
      */
     public Reservation getReservation(int id) {
         ArrayList<String> r = database.getFirstMatch("SELECT * FROM reservation WHERE id = '"+id+"'");
         try {
             Date date_start_parsed = dateFormat.parse(r.get(2));
             Date date_end_parsed = dateFormat.parse(r.get(3));
            Timestamp t = new Timestamp(date_start_parsed.getTime());
             return new Reservation(Integer.parseInt(r.get(0)),
                     Integer.parseInt(r.get(1)),
                     new Timestamp(date_start_parsed.getTime()),
                     new Timestamp(date_end_parsed.getTime()),
                     Integer.parseInt(r.get(4)));
         }
         catch (ParseException e) {
             CarRental.getInstance().appendLog("Failed to get reservation, parse exception when parsing dates.",e);
             return null;
         }
     }
     
     /**
      * Get an array of all reservations in the database ordered by start date
      * @return Array of reservations
      */
     public ArrayList<Reservation> getReservations() {
         ArrayList<ArrayList<String>> rs = database.getMatches("SELECT * FROM customer ORDER BY start,end DESC");
         ArrayList<Reservation> results = new ArrayList<>();
         for(ArrayList<String> r : rs) {
             try {
                 Date date_start_parsed = dateFormat.parse(r.get(2));
                 Date date_end_parsed = dateFormat.parse(r.get(3));
                 results.add(new Reservation(Integer.parseInt(r.get(0)),
                         Integer.parseInt(r.get(1)),
                         new Timestamp(date_start_parsed.getTime()),
                         new Timestamp(date_end_parsed.getTime()),
                         Integer.parseInt(r.get(4))));
             }
             catch (ParseException e) {
                 CarRental.getInstance().appendLog("Failed to get reservation, parse exception when parsing dates.",e);
                 return null;
             }
         }
         return results;
     }
     
     /**
      * Save a reservation to the database
      * @param r the reservation to save
      */
     public void saveReservation(Reservation r) {
         ArrayList<String> save_data = new ArrayList<>();
         save_data.add(Integer.toString(r.getID()));
         save_data.add(Integer.toString(r.getVehicleID()));
         save_data.add(r.getTStart().toString());
         save_data.add(r.getTEnd().toString());
         save_data.add(Integer.toString(r.getCustomerID()));
         database.saveArray("reservation", save_data);
     }
     
     //MAINTENANCE TYPE
     
     /**
      * Get a maintenance type by it's id
      * @param id
      * @return Maintenance Type
      */
     public MaintenanceType getMaintenanceType(int id) {
         ArrayList<String> m = database.getFirstMatch("SELECT * FROM maintenance_type WHERE id = '"+id+"'");
         boolean is_service = false;
         if(Integer.parseInt(m.get(2)) == 1) is_service = true;
         return new MaintenanceType(Integer.parseInt(m.get(0)),m.get(1),is_service);
     }
     
     /**
      * Get an array of all maintenance types in the database ordered alphabetically by name
      * @return Array of maintenance types
      */
     public ArrayList<MaintenanceType> getMaintenanceTypes() {
         ArrayList<ArrayList<String>> ms = database.getMatches("SELECT * FROM maintenance_type ORDER BY name ASC");
         ArrayList<MaintenanceType> results = new ArrayList<>();
         for(ArrayList<String> m : ms) {
             boolean is_service = false;
             if(Integer.parseInt(m.get(2)) == 1) is_service = true;
             results.add(new MaintenanceType(Integer.parseInt(m.get(0)),m.get(1),is_service));
         }
         return results;
     }
     
     /**
      * Get an array of all maintenance types in the database, that are considered
      * service checks, ordered alphabetically by name
      * @return Array of maintenance types
      */
     public ArrayList<MaintenanceType> getMaintenanceTypesService() {
         ArrayList<ArrayList<String>> ms = database.getMatches("SELECT * FROM maintenance_type WHERE is_service = 1 ORDER BY name ASC");
         ArrayList<MaintenanceType> results = new ArrayList<>();
         for(ArrayList<String> m : ms) {
             boolean is_service = false;
             if(Integer.parseInt(m.get(2)) == 1) is_service = true;
             results.add(new MaintenanceType(Integer.parseInt(m.get(0)),m.get(1),is_service));
         }
         return results;
     }
     
     /**
      * Save a maintenance type to the database
      * @param m the maintenancetype to save
      */
     public void saveMaintenanceType(MaintenanceType m) {
         ArrayList<String> save_data = new ArrayList<>();
         save_data.add(Integer.toString(m.getID()));
         save_data.add(m.getName());
         String is_service = "0";
         if(m.getIs_service()) is_service = "1";
         save_data.add(is_service);
         database.saveArray("maintenance_type", save_data);
     }
     
     //MAINTENANCE
     
     /**
      * Get a maintenance by it's id
      * @param id
      * @return Maintenance
      */
     public Maintenance getMaintenance(int id) {
         ArrayList<String> m = database.getFirstMatch("SELECT * FROM maintenance WHERE id = '"+id+"'");
         try {
             Date date_start_parsed = dateFormat.parse(m.get(2));
             Date date_end_parsed = dateFormat.parse(m.get(3));
             return new Maintenance(Integer.parseInt(m.get(0)),
                     Integer.parseInt(m.get(1)),
                     new Timestamp(date_start_parsed.getTime()),
                     new Timestamp(date_end_parsed.getTime()),
                     Integer.parseInt(m.get(4)));
         }
         catch (ParseException e) {
             CarRental.getInstance().appendLog("Failed to get maintenance from database, parse error when parsing dates.",e);
             return null;
         }
     }
     
     /**
      * Get an array of all maintenances in the database ordered by start date
      * @return Array of maintenances
      */
     public ArrayList<Maintenance> getMaintenances() {
         ArrayList<ArrayList<String>> ms = database.getMatches("SELECT * FROM maintenance ORDER BY date_start,date_end DESC");
         ArrayList<Maintenance> results = new ArrayList<>();
         for(ArrayList<String> m : ms) {
             try {
                 Date date_start_parsed = dateFormat.parse(m.get(2));
                 Date date_end_parsed = dateFormat.parse(m.get(3));
                 results.add(new Maintenance(Integer.parseInt(m.get(0)),
                         Integer.parseInt(m.get(1)),
                         new Timestamp(date_start_parsed.getTime()),
                         new Timestamp(date_end_parsed.getTime()),
                         Integer.parseInt(m.get(4))));
             }
             catch (ParseException e) {
                 CarRental.getInstance().appendLog("Failed to get maintenances from database, parse error when parsing dates.",e);
                 return null;
             }
         }
         return results;
     }
     
     /**
      * Save a maintenance to the database
      * @param m the maintenance to save
      */
     public void saveMaintenance(Maintenance m) {
         ArrayList<String> save_data = new ArrayList<>();
         save_data.add(Integer.toString(m.getID()));
         save_data.add(Integer.toString(m.getVehicleID()));
         save_data.add(m.getTStart().toString());
         save_data.add(m.getTEnd().toString());
         save_data.add(Integer.toString(m.getTypeID()));
         database.saveArray("maintenance", save_data);
     }
 }
