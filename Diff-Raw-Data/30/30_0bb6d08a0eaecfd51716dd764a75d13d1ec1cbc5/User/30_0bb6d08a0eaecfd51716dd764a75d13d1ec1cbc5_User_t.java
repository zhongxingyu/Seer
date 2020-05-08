 /*
  * User.java
  * StockPlay - Gebruikersklasse
  *
  * Copyright (c) 2010 StockPlay development team
  * All rights reserved.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.kapti.data;
 
 import com.kapti.exceptions.InvocationException;
 import com.kapti.exceptions.StockPlayException;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.Hashtable;
 import java.security.MessageDigest;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class User {
     //
     // Member data
     //
 
     public static enum Fields {
 
         ID, NICKNAME, PASSWORD, EMAIL, LASTNAME, FIRSTNAME, REGDATE, ROLE, POINTS, STARTAMOUNT, CASH, RRN
     }
     private int id = -1;
     private String nickname = "";
     private String password = "";
     private String email = "";
     private String lastname = "";
     private String firstname = "";
     private Date regdate = null;
     private int role = 0;
     private int points = 0;
     private double startamount = 0;
     private double cash = 0;
     private long rrn = 0;
 
     //
     // Construction
     //
 //    public User(int id, String nickname, String email, String lastname, String firstname, Date regdate) {
 //        this.id = id;
 //        this.nickname = nickname;
 //        this.email = email;
 //        this.lastname = lastname;
 //        this.firstname = firstname;
 //        this.regdate = regdate;
 //    }
 
    public User(){}

     public User (int id){
         this.id = id;
     }
 
     //
     // Methods
     //

    private void setId(int id){
        this.id = id;
    }

     public void setEncryptedPassword(String password) {
 
         this.password = password;
     }
 
     public void setPassword(String password) {
         this.password = encryptPassword(password);
     }
 
     public String getPassword() {
         return password;
     }
 
     private static String byteArrayToHexString(byte[] b) {
         StringBuffer sb = new StringBuffer(b.length * 2);
         for (int i = 0; i < b.length; i++) {
             int v = b[i] & 0xff;
             if (v < 16) {
                 sb.append('0');
             }
             sb.append(Integer.toHexString(v));
         }
         return sb.toString().toUpperCase();
     }
 
     private String encryptPassword(String password) {
         try {
             //we halen de salt op
             Properties properties = new Properties();
             properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("backend.properties"));
 
             String salt = properties.getProperty("salt");
 
             MessageDigest digest = MessageDigest.getInstance("SHA-256");
             digest.reset();
             digest.update(salt.getBytes());
             return byteArrayToHexString(digest.digest(password.getBytes()));
         } catch (IOException ex) {
             Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         } catch (NoSuchAlgorithmException ex) {
             Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
     }
 
     public boolean checkPassword(String password) {
         return this.password.equals(encryptPassword(password));
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getFirstname() {
         return firstname;
     }
 
     public int getRole() {
         return role;
     }
 
     public void setRole(int role) {
         this.role = role;
     }
 
     public String getLastname() {
         return lastname;
     }
 
     public String getNickname() {
         return nickname;
     }
 
     public double getCash() {
         return cash;
     }
 
     public int getId() {
         return id;
     }
 
     public int getPoints() {
         return points;
     }
 
     public Date getRegdate() {
         return regdate;
     }
 
     public double getStartamount() {
         return startamount;
     }
 
     public void setCash(double cash) {
         this.cash = cash;
     }
 
     public void setPoints(int points) {
         this.points = points;
     }
 
     public void setStartamount(double startamount) {
         this.startamount = startamount;
     }
 
     public long getRijksregisternummer() {
         return rrn;
     }
 
     public void setRijksregisternummer(long rrn) {
         this.rrn = rrn;
     }
 
     public void setFirstname(String firstname) {
         this.firstname = firstname;
     }
 
     public void setLastname(String lastname) {
         this.lastname = lastname;
     }
 
     public void setNickname(String nickname) {
         this.nickname = nickname;
     }
 
     public void setRegdate(Date regdate) {
         this.regdate = regdate;
     }
 
 
 
     /**
      * Geeft het User-object in een generiek hashtable-object terug, zodat het geserialiseerd kan worden voor XML-RPC
      * @param iFields De velden die moeten worden opgenomen in de struct (Opmerking: een paswoord kan niet worden opgevraagd!)
      * @return
      *
      */
     public Hashtable<String, Object> toStruct(Fields... iFields) {
         Hashtable<String, Object> oStruct = new Hashtable<String, Object>();
         for (Fields tField : iFields) {
             switch (tField) {
                 case ID:
                     oStruct.put(tField.name(), getId());
                     break;
                 case NICKNAME:
                     oStruct.put(tField.name(), getNickname());
                     break;
                 case EMAIL:
                     oStruct.put(tField.name(), getEmail());
                     break;
                 case LASTNAME:
                     if (getLastname() != null) {
                         oStruct.put(tField.name(), getLastname());
                     }
                     break;
                 case FIRSTNAME:
                     if (getFirstname() != null) {
                         oStruct.put(tField.name(), getFirstname());
                     }
                     break;
                 case REGDATE:
                     oStruct.put(tField.name(), getRegdate());
                     break;
                 case ROLE:
                     oStruct.put(tField.name(), getRole());
                     break;
                 case POINTS:
                     oStruct.put(tField.name(), getPoints());
                     break;
                 case STARTAMOUNT:
                     oStruct.put(tField.name(), getStartamount());
                     break;
                 case CASH:
                     oStruct.put(tField.name(), getCash());
                     break;
                 case RRN:
                     oStruct.put(tField.name(), Long.toString(getRijksregisternummer()));
                     break;
             }
         }
         return oStruct;
     }
 
     public void applyStruct(Hashtable<String, Object> iStruct) throws StockPlayException {
         for (String tKey : iStruct.keySet()) {
             Object tValue = iStruct.get(tKey);
             Fields tField = null;
             try {
                 tField = Fields.valueOf(tKey.toUpperCase());
             } catch (IllegalArgumentException e) {
                 throw new InvocationException(InvocationException.Type.KEY_DOES_NOT_EXIST, "requested key '" + tKey + "' does not exist");
             }
 
 
             switch (tField) {
                case ID:
                    setId((Integer) tValue);
                 case FIRSTNAME:
                     setFirstname((String) tValue);
                     break;
                 case LASTNAME:
                     setLastname((String) tValue);
                     break;
                 case NICKNAME:
                     setNickname((String) tValue);
                     break;
                 case PASSWORD:
                     setPassword((String) tValue);
                     break;
                 case EMAIL:
                     setEmail((String) tValue);
                     break;
                 case ROLE:
                     setRole((Integer) tValue);
                     break;
                 case RRN:
                     if (tValue instanceof String) {
                         setRijksregisternummer(Long.parseLong((String) tValue));
                     } else {
                         setRijksregisternummer(((Integer) tValue).longValue());
                     }
                     break;
                 case STARTAMOUNT:
                 case POINTS:
                 case CASH:
                 case REGDATE:
                 default:
                     throw new InvocationException(InvocationException.Type.READ_ONLY_KEY, "requested key '" + tKey + "' cannot be modified");
             }
         }
     }
 
     public static User fromStruct(Hashtable<String, Object> iStruct) throws StockPlayException {
         // Create case mapping
         Hashtable<Fields, String> tStructMap = new Hashtable<Fields, String>();
         for (String tKey : iStruct.keySet()) {
             Fields tField = null;
             try {
                 tField = Fields.valueOf(tKey.toUpperCase());
             } catch (IllegalArgumentException e) {
                 throw new InvocationException(InvocationException.Type.KEY_DOES_NOT_EXIST, "requested key '" + tKey + "' does not exist");
             }
             tStructMap.put(tField, tKey);
         }
 
         // Check needed keys
        //if (tStructMap.containsKey(Fields.ID)){// && tStructMap.containsKey(Fields.NICKNAME) && tStructMap.containsKey(Fields.EMAIL) && tStructMap.containsKey(Fields.FIRSTNAME) && tStructMap.containsKey(Fields.LASTNAME) && tStructMap.containsKey(Fields.REGDATE)) {
            User tUser = new User();
              //      (Integer) iStruct.get(tStructMap.get(Fields.ID)));
 //                    (String) iStruct.get(tStructMap.get(Fields.NICKNAME)),
 //                    (String) iStruct.get(tStructMap.get(Fields.EMAIL)),
 //                    (String) iStruct.get(tStructMap.get(Fields.LASTNAME)),
 //                    (String) iStruct.get(tStructMap.get(Fields.FIRSTNAME)),
 //                    (Date) iStruct.get(tStructMap.get(Fields.REGDATE)));
       //     iStruct.remove(tStructMap.get(Fields.ID));
 //            iStruct.remove(tStructMap.get(Fields.NICKNAME));
 //            iStruct.remove(tStructMap.get(Fields.EMAIL));
 //            iStruct.remove(tStructMap.get(Fields.LASTNAME));
 //            iStruct.remove(tStructMap.get(Fields.FIRSTNAME));
 //            iStruct.remove(tStructMap.get(Fields.REGDATE));
             return tUser;
//        } else {
//            throw new ServiceException(ServiceException.Type.NOT_ENOUGH_INFORMATION);
//        }
     }
 }
