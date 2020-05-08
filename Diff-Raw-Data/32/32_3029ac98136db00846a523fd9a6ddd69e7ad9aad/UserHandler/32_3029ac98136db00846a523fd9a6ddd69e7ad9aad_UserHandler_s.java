 /*
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
 package com.kapti.backend.api;
 
 import com.kapti.backend.helpers.DateHelper;
 import com.kapti.backend.security.SessionsHandler;
 import com.kapti.data.User;
 import com.kapti.data.persistence.GenericDAO;
 import com.kapti.exceptions.InvocationException;
 import com.kapti.exceptions.StockPlayException;
 import com.kapti.filter.Filter;
 import com.kapti.filter.parsing.Parser;
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashMap;
import java.util.HashMap;
 import java.util.Iterator;
 import java.util.TimeZone;
 import java.util.Vector;
 
 /**
  * \brief   Handler van de User klasse.
  *
  * Deze klasse is de handler van de User klasse. Ze staat in
  * voor de verwerking van aanroepen van functies die zich in deze klasse
  * bevinden, lokaal de correcte aanvragen uit te voeren, en het resultaat
  * op conforme wijze terug te sturen.
  */
 public class UserHandler extends MethodClass {
     //
     // Methodes
     //
 
     public int Hello(String iClient, int iProtocolVersion) throws StockPlayException {
         getLogger().info("Client connected: " + iClient);
         if (iProtocolVersion != PROTOCOL_VERSION) {
             throw new InvocationException(InvocationException.Type.VERSION_NOT_SUPPORTED);
         }
         return 1;
     }
 
     public int Create(HashMap<String, Object> iDetails) throws StockPlayException {
         // Get DAO reference
         GenericDAO<com.kapti.data.User, Integer> tUserDAO = getDAO().getUserDAO();
 
         // Instantiate a new user
         User tUser = User.fromStruct(iDetails);
         tUser.applyStruct(iDetails);
         tUser.setStartamount(100000); //TODO hier berekenen van het startamount
         tUser.setCash(100000);
         tUser.setRegdate(DateHelper.convertCalendar(Calendar.getInstance(), TimeZone.getTimeZone("GMT")).getTime());
 
         return tUserDAO.create(tUser);
     }
 
     public Vector<HashMap<String, Object>> Details() throws StockPlayException {
         return Details("id == '" + getUser().getId() + "" + "'s");
     }
 
     public Vector<HashMap<String, Object>> Details(String iFilter) throws StockPlayException {
         // Get DAO reference
         GenericDAO<com.kapti.data.User, Integer> userDAO = getDAO().getUserDAO();
 
         Parser parser = Parser.getInstance();
         Filter filter = parser.parse(iFilter);
 
         // Fetch and convert all Indexs
         Collection<com.kapti.data.User> tUsers = userDAO.findByFilter(filter);
         Vector<HashMap<String, Object>> oVector = new Vector<HashMap<String, Object>>();
         for (com.kapti.data.User tUser : tUsers) {
             oVector.add(tUser.toStruct(
                     com.kapti.data.User.Fields.ID,
                     com.kapti.data.User.Fields.NICKNAME,
                     com.kapti.data.User.Fields.EMAIL,
                     com.kapti.data.User.Fields.LASTNAME,
                     com.kapti.data.User.Fields.FIRSTNAME,
                     com.kapti.data.User.Fields.CASH,
                     com.kapti.data.User.Fields.ROLE,
                     com.kapti.data.User.Fields.RRN,
                     com.kapti.data.User.Fields.STARTAMOUNT,
                     com.kapti.data.User.Fields.REGDATE,
                     com.kapti.data.User.Fields.POINTS));
         }
 
         return oVector;
     }
 
     public Vector<HashMap<String, Object>> List(String iFilter) throws StockPlayException {
         // Get DAO reference
         GenericDAO<com.kapti.data.User, Integer> userDAO = getDAO().getUserDAO();
 
         Parser parser = Parser.getInstance();
         Filter filter = parser.parse(iFilter);
         // Fetch and convert all Indexs
         Collection<com.kapti.data.User> tUsers = userDAO.findByFilter(filter);
         Vector<HashMap<String, Object>> oVector = new Vector<HashMap<String, Object>>();
         for (com.kapti.data.User tUser : tUsers) {
             oVector.add(tUser.toStruct(
                     com.kapti.data.User.Fields.ID,
                     com.kapti.data.User.Fields.NICKNAME,
                     com.kapti.data.User.Fields.EMAIL,
                     com.kapti.data.User.Fields.LASTNAME,
                     com.kapti.data.User.Fields.FIRSTNAME,
                     com.kapti.data.User.Fields.CASH,
                     com.kapti.data.User.Fields.ROLE,
                     com.kapti.data.User.Fields.RRN,
                     com.kapti.data.User.Fields.STARTAMOUNT,
                     com.kapti.data.User.Fields.REGDATE,
                     com.kapti.data.User.Fields.POINTS));
         }
 
         return oVector;
     }
 
     public int Modify(HashMap<String, Object> iDetails) throws StockPlayException {
         return Modify(("id == '" + getUser().getId() + "" + "'s"), iDetails);
     }
 
     public int Modify(String iFilter, HashMap<String, Object> iDetails) throws StockPlayException {
         // Get DAO reference
         GenericDAO<com.kapti.data.User, Integer> tUserDAO = getDAO().getUserDAO();
 
         Parser parser = Parser.getInstance();
         Filter filter = parser.parse(iFilter);
 
         // Now apply the new properties
         // TODO: controleren of de struct geen ID field bevat, deze kan _enkel_
         //       gebruikt worden om een initiële Exchange aa nte maken (Create)
         for (com.kapti.data.User tUser : tUserDAO.findByFilter(filter)) {
             tUser.applyStruct(iDetails);
             tUserDAO.update(tUser);
         }
 
         return 1;
     }
 
     public int Remove(String iFilter) throws StockPlayException {
         // Get DAO reference
         GenericDAO<com.kapti.data.User, Integer> tUserDAO = getDAO().getUserDAO();
 
         Parser parser = Parser.getInstance();
         Filter filter = parser.parse(iFilter);
 
         for (com.kapti.data.User tUser : tUserDAO.findByFilter(filter)) {
             tUserDAO.delete(tUser);
         }
 
         return 1;
     }
 
     // Authenticatiemethode
     public String Validate(String nickname, String password) throws StockPlayException {
         GenericDAO<com.kapti.data.User, Integer> tUserDAO = getDAO().getUserDAO();
 
         Parser parser = Parser.getInstance();
         Filter filter = parser.parse("nickname == '" + nickname + "'");
 
         Collection<com.kapti.data.User> tUsers = tUserDAO.findByFilter(filter);
        if (tUsers.size() == 0) {
            return "";  // Throw invalid credentials exception
        }
         Iterator<User> uIterator = tUsers.iterator();
         User user = uIterator.next();
         if (user.checkPassword(password)) {
 
             return createAndRegisterSession(user);
 
         } else {
             return "";
         }
     }
 
     private String CreateSessionForUser(int id) throws StockPlayException {
         GenericDAO<com.kapti.data.User, Integer> tUserDAO = getDAO().getUserDAO();
 
         User u = tUserDAO.findById(id);
         return createAndRegisterSession(u);
 
     }
 
     private String createAndRegisterSession(User user) {
         //we generereren nu een sessie-id voor de gebruiker
         java.security.SecureRandom random = new SecureRandom();
         String sessionid = new BigInteger(130, random).toString(32);
         SessionsHandler.getInstance().registerSession(sessionid, user);
         return sessionid;
     }
 }
