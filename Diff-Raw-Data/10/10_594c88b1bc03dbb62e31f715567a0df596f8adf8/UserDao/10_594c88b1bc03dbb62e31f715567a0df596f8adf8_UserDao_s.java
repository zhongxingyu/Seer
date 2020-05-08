 package core.User;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import com.mysql.jdbc.ResultSet;
 
 import core.DBConnection;
 import core.Car.Car;
 import core.CreditCard.CreditCard;
 import core.DrivingPlan.DrivingPlan;
 import core.DrivingPlan.PlanType;
 
 public class UserDao {
     static DBConnection connection = new DBConnection();
     String username, password, firstName, middleInit, lastName, email,
     phone, address, plan_Type, nameOnCard, billingAddress;
     String expiryDate;
     BigInteger cardNo;
     Integer cvv;
 
     public User addUser(String username, String password, UserType type) {
         if(isUsernameExistent(username)) {
             return null;
         }
         else {
             Connection conn = connection.createConnection();
             try {
                 if(type.equals(UserType.ADMIN)){
                     String statement = "INSERT INTO Administrator(Username,Pass) VALUES(?,?)";
                     PreparedStatement prep = conn.prepareStatement(statement);
                     prep.setString(1, username);
                     prep.setString(2, password);
                     prep.executeUpdate();
                     prep.close();
                     connection.closeConnection(conn);
                     return new AdminUser(username, password);
                 }
                 else if(type.equals(UserType.EMPLOYEE)){
                     String statement = "INSERT INTO Employee(Username,Pass) VALUES(?,?)";
                     PreparedStatement prep = conn.prepareStatement(statement);
                     prep.setString(1, username);
                     prep.setString(2, password);
                     prep.executeUpdate();
                     prep.close();
                     connection.closeConnection(conn);
                     return new EmployeeUser(username, password);
                 }
                 else if(type.equals(UserType.MEMBER)){
                     String statement = "INSERT INTO Member(Username,Pass) VALUES(?,?)";
                     PreparedStatement prep = conn.prepareStatement(statement);
                     prep.setString(1, username);
                     prep.setString(2, password);
                     prep.execute();
                     prep.close();
                     connection.closeConnection(conn);
                     return new MemberUser(username, password);
                 }
             }                
             catch (SQLException e) {}
             connection.closeConnection(conn);
             return null;
         }
     }
 
     public boolean isUsernameExistent(String username) {
         Connection conn = connection.createConnection();
         try {
             String statement = "SELECT Username from Administrator union " +
             "SELECT Username from Employee union SELECT Username from Member";
             PreparedStatement prep = conn.prepareStatement(statement);
             ResultSet rs = (ResultSet) prep.executeQuery();
             while(rs.next()){
                 String s = rs.getString("Username");
                 if(username.equals(s)){
                     prep.close();
                     connection.closeConnection(conn);
                     return true;
                 }
             }
             prep.close();
             connection.closeConnection(conn);
             return false;
         }
         catch (SQLException e) {}
         connection.closeConnection(conn);
         return false;
     }
 
     public User login(String username, String password){
         Connection conn = null;
         if(isUsernameExistent(username)) {
             try {
                 conn = connection.createConnection();
                 String statement = "SELECT Username,Pass from Member";
                 PreparedStatement prep = conn.prepareStatement(statement);
                 ResultSet rs = (ResultSet) prep.executeQuery();
                 while(rs.next()) {
                     String s = rs.getString("Username");
                     if(s.equals(username)){
                         if(rs.getString("Pass").equals(password)){
                             prep.close();
                             MemberUser memUser = new MemberUser(username, password);
                             statement = "SELECT First_Name, Middle_Initial, Last_Name, Email_Address, "
                                 + "Phone, Address, Plan_Type, Card_No, Name_on_card, CVV, Expiry_Date, Billing_Address "
                                 + "FROM Member NATURAL JOIN Credit_Card "
                                 + "WHERE Username = ?";
 
                             prep = conn.prepareStatement(statement);
                             prep.setString(1, username);
                             rs = (ResultSet) prep.executeQuery();
                             while (rs.next()) {
                                 firstName = rs.getString("First_Name");
                                 middleInit = rs.getString("Middle_Initial");
                                 lastName = rs.getString("Last_Name");
                                 email = rs.getString("Email_Address");
                                 phone = rs.getString("Phone");
                                 address = rs.getString("Address");
                                 plan_Type = rs.getString("Plan_Type");
                                 DrivingPlan drivingPlan = new DrivingPlan();
                                 if(PlanType.OCCASIONAL.toString().equals(plan_Type)) 
                                     drivingPlan = new DrivingPlan(PlanType.OCCASIONAL);
                                 else if(PlanType.FREQUENT.toString().equals(plan_Type))
                                     drivingPlan = new DrivingPlan(PlanType.FREQUENT);
                                 else if(PlanType.DAILY.toString().equals(plan_Type))
                                     drivingPlan = new DrivingPlan(PlanType.DAILY);
                                 Long tempL = rs.getLong("Card_No");
                                 BigInteger temp = new BigInteger(tempL.toString());
                                 cardNo = temp;
                                 cvv = rs.getInt("CVV");
                                 expiryDate = rs.getString("Expiry_Date");
                                 nameOnCard = rs.getString("Name_on_card");
                                 billingAddress = rs.getString("Billing_Address");
                                 CreditCard creditCard = new CreditCard(cardNo, nameOnCard,
                                         cvv, expiryDate, billingAddress);
                                 memUser.setAllMemFields(firstName, middleInit, lastName, email, phone,
                                         address, creditCard, drivingPlan);
                                 return memUser;
                             }
                             prep.close();
                             connection.closeConnection(conn);
                         }
                     }
                 }
                 statement = "SELECT Username,Pass from Employee";
                 prep = conn.prepareStatement(statement);
                 rs = (ResultSet) prep.executeQuery();
                 while(rs.next()) {
                     String s = rs.getString("Username");
                     if(s.equals(username)){
                         if(rs.getString("Pass").equals(password)){
                             prep.close();
                             return new EmployeeUser(username, password);
                         }
                     }
                 }
                 statement = "SELECT Username,Pass from Administrator";
                 prep = conn.prepareStatement(statement);
                 rs = (ResultSet) prep.executeQuery();
                 while(rs.next()) {
                     String s = rs.getString("Username");
                     if(s.equals(username)){
                         if(rs.getString("Pass").equals(password)){
                             prep.close();
                             return new AdminUser(username, password);
                         }
                     }
                 }
                 prep.close();
             }
             catch (Exception e) {}
 
             connection.closeConnection(conn);
             return null;
         }
         return null;
     }
 
     public MemberUser editMemberInfo(MemberUser member) {
         Connection conn = connection.createConnection();
         try {
             if(isCreditCardExisting(member.creditCard.getCardNumber())){
                 String statement = "UPDATE Member SET First_Name = ?, " +
                 "Middle_Initial = ?, Last_Name = ?, Email_Address = ?, " +
                 "Phone = ?, Address = ?, Plan_Type = ?, Card_No = ? " +
                 "WHERE Username = ?";
                 PreparedStatement prep = conn.prepareStatement(statement);
                 prep.setString(1, member.firstName);
                 prep.setString(2, member.middleInit);
                 prep.setString(3, member.lastName);
                 prep.setString(4, member.email);
                 prep.setString(5, member.phone);
                 prep.setString(6, member.address);
                 prep.setString(7, member.drivingPlan.getName());
                 prep.setBigDecimal(8, new BigDecimal(member.creditCard.getCardNumber()));
                 prep.setString(9, member.username);
                 prep.execute();
                 prep.close();
 
                 statement = "UPDATE Credit_Card SET Name_on_card = ?, CVV = ?, " +
                 "Expiry_Date = ?, Billing_Address = ? " +
                 "WHERE Card_No = ( SELECT Card_No " +
                 "FROM Member WHERE Username = ?)";
 
                 prep = conn.prepareStatement(statement);
                 prep.setString(1, member.creditCard.getNameOnCard());
                 prep.setInt(2, member.creditCard.getCvv());
                 prep.setString(3, member.creditCard.getExpiryDate());
                 prep.setString(4, member.creditCard.getBillingAddress());
                 prep.setString(5, member.username);
                 prep.execute();
                 prep.close();
             }
             else {
                 String statement = "INSERT INTO Credit_Card VALUES (?,?,?,?,?)";
                 PreparedStatement prep = conn.prepareStatement(statement);
                 prep.setBigDecimal(1, new BigDecimal(member.creditCard.getCardNumber()));
                 prep.setString(2, member.creditCard.getNameOnCard());
                 prep.setInt(3, member.creditCard.getCvv());
                 prep.setString(4, member.creditCard.getExpiryDate());
                 prep.setString(5, member.creditCard.getBillingAddress());
                 prep.execute();
                 prep.close();
 
                 statement = "DELETE FROM Credit_Card WHERE Name_on_card = ? AND Card_No <> ?";
                 prep = conn.prepareStatement(statement);
                 prep.setString(1, member.creditCard.getNameOnCard());
                 prep.setBigDecimal(2, new BigDecimal(member.creditCard.getCardNumber()));
                 prep.execute();
                 prep.close();
 
 
                 statement = "UPDATE Member SET First_Name = ?, " +
                 "Middle_Initial = ?, Last_Name = ?, Email_Address = ?, " +
                 "Phone = ?, Address = ?, Plan_Type = ?, Card_No = ? " +
                 "WHERE Username = ?";
                 prep = conn.prepareStatement(statement);
                 prep.setString(1, member.firstName);
                 prep.setString(2, member.middleInit);
                 prep.setString(3, member.lastName);
                 prep.setString(4, member.email);
                 prep.setString(5, member.phone);
                 prep.setString(6, member.address);
                 prep.setString(7, member.drivingPlan.getName());
                 prep.setBigDecimal(8, new BigDecimal(member.creditCard.getCardNumber()));
                 prep.setString(9, member.username);
                 prep.executeUpdate();
                 prep.close();
             }
             connection.closeConnection(conn);
             return member;
         } catch (SQLException e) {
             connection.closeConnection(conn);
             return null;
         }
     }
 
     public boolean isCreditCardExisting(BigInteger cardNo) {
         Connection conn = connection.createConnection();
         try {
             String statement = "SELECT Card_No FROM Credit_Card";
             PreparedStatement prep = conn.prepareStatement(statement);
             ResultSet rs = (ResultSet) prep.executeQuery();
             while(rs.next()) {
                 Long tempL = rs.getLong("Card_No");
                 BigInteger tempB = new BigInteger(tempL.toString());
                 if(tempB == cardNo)
                     return true;
             }
             return false;
         }
         catch (SQLException e){}
         return false;
     }
 
     public Car insertCar(Car carAdded) {
         Connection conn = connection.createConnection();
         try {
            String statement = "INSERT INTO Car (Vehicle_Sno, Location_Name, Auxiliary_Cable, " +
            "Under_Maintenance_Flag, Model_Name, Car_Type, Color, Hourly_Rate, Daily_Rate," +
            " Bluetooth, Seating_Cap, Transmission_Type) VALUES ? , ? , ? ," +
            " ? , ? , ? , ? , ? , ? , ? , ? , ? " +
            "WHERE NOT EXISTS (SELECT Vehicle_Sno FROM Car WHERE (Location_Name = ?))";
             PreparedStatement prep = conn.prepareStatement(statement);
             prep.setString(1, carAdded.getVehicleSNO());
             prep.setString(2, carAdded.getLocName());
             String tempStr;
             if(carAdded.isAuxCable() == true){
                 tempStr = "Yes";
             }else{
                 tempStr = "No";
             }
             prep.setString(3, tempStr);
             prep.setString(4, "No");
             prep.setString(5, carAdded.getModelType());
             prep.setString(6, carAdded.getCarType());
             prep.setString(7, carAdded.getColor());
             prep.setInt(8, carAdded.getHourlyRate());
             prep.setInt(9, carAdded.getDailyRate());
             String tempS;
             if(carAdded.isBluetooth() == true){
                 tempS = "Yes";
             }else{
                 tempS = "No";
             }
             prep.setString(10, tempS);
             prep.setInt(11, carAdded.getSeatCapacity());
             prep.setString(12, carAdded.getTransmission());
             prep.setString(13, carAdded.getLocName());
             prep.execute();
             prep.close();
             connection.closeConnection(conn);
             return carAdded;
         } catch (SQLException e) {
             connection.closeConnection(conn);
             return null;
         }
     }
 
     //    public static void main(String[] args) {
     //        UserDao acc = new UserDao();       
     //        AdminUser admin1 = (AdminUser) acc.login("Bohr", "electron1");
     //        AdminUser admin2 = (AdminUser) acc.login("Newton", "secret");
     //        AdminUser admin3 = (AdminUser) acc.login("rlobo3", "pass123");
     //        MemberUser mem1 = (MemberUser) acc.login("mem1", "M1");
     //        MemberUser mem2 = (MemberUser) acc.login("mem2", "M3");
     //        MemberUser mem3 = (MemberUser) acc.login("rlobo", "pass123");
     //        EmployeeUser emp1 = (EmployeeUser) acc.login("emp1", "pass1");
     //        EmployeeUser emp2 = (EmployeeUser) acc.login("emp2", "pass3");
     //        EmployeeUser emp3 = (EmployeeUser) acc.login("rlobo", "pass123");
     //        AdminUser admin1 = (AdminUser) acc.addUser("hvu", "1A", UserType.ADMIN);
     //        AdminUser admin2 = (AdminUser) acc.addUser("rlobo", "pass123", UserType.ADMIN);
     //        MemberUser mem1 = (MemberUser) acc.addUser("m1", "pass", UserType.MEMBER);
     //        MemberUser mem2 = (MemberUser) acc.addUser("rlobo", "pass123", UserType.MEMBER);
     //        EmployeeUser emp1 = acc.addUser("Emp1", "pass");
     //        EmployeeUser emp2 = acc.addUser("rlobo", "pass123");
     //        if(admin1 != null)
     //        if(mem1 != null)
     //        if(emp1 != null)
     //            System.out.println("Found");
     //        else
     //            System.out.println("Failed");
     //        
     //        if(admin2 != null)
     //        if(mem2 != null)
     //        if(emp2 != null)
     //            System.out.println("Found");
     //        else
     //            System.out.println("Failed");
     //          
     //        if(admin3 != null)
     //          if(mem3 != null)
     //          if(emp3 != null)
     //            System.out.println("Added");
     //        else
     //            System.out.println("Failed");
     //    }
 
 } 
