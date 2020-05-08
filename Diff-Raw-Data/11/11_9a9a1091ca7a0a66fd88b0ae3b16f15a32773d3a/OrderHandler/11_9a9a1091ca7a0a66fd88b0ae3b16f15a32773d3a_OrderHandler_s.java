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
 package com.kapti.backend.api.user;
 
 import com.kapti.backend.api.MethodClass;
 import com.kapti.backend.helpers.DateHelper;
 import com.kapti.data.Order;
 import com.kapti.data.OrderStatus;
 import com.kapti.data.persistence.GenericDAO;
import com.kapti.exceptions.InvocationException;
 import com.kapti.exceptions.StockPlayException;
 import com.kapti.filter.Filter;
 import com.kapti.filter.parsing.Parser;
 import com.kapti.filter.relation.RelationAnd;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.TimeZone;
 import java.util.Vector;
 
 /**
  * \brief   Handler van de User.Order subklasse.
  *
  * Deze klasse is de handler van de User.Order subklasse. Ze staat in
  * voor de verwerking van aanroepen van functies die zich in deze klasse
  * bevinden, lokaal de correcte aanvragen uit te voeren, en het resultaat
  * op conforme wijze terug te sturen.
  */
 public class OrderHandler extends MethodClass {
 
     public Vector<HashMap<String, Object>> List(String iFilter) throws StockPlayException {
         // Get DAO reference
         GenericDAO<com.kapti.data.Order, Integer> orDAO = getDAO().getOrderDAO();
 
         // Create a filter
         Parser parser = Parser.getInstance();
         Filter filter = null;
         Filter base = parser.parse(iFilter);
         if (getRole().isBackendAdmin()) {   // TODO: isOrderAdmin
             filter = base;
         } else {
             Filter user = parser.parse("id == '" + getUser().getId() + "'i");
             filter = Filter.merge(RelationAnd.class, base, user);
         }
 
         // Fetch and convert all orders
         Collection<com.kapti.data.Order> tOrders = orDAO.findByFilter(filter);
         Vector<HashMap<String, Object>> oVector = new Vector<HashMap<String, Object>>();
         for (com.kapti.data.Order tOrder : tOrders) {
             oVector.add(tOrder.toStruct(
                     com.kapti.data.Order.Fields.ID,
                     com.kapti.data.Order.Fields.USER,
                     com.kapti.data.Order.Fields.TYPE,
                     com.kapti.data.Order.Fields.ISIN,
                     com.kapti.data.Order.Fields.AMOUNT,
                     com.kapti.data.Order.Fields.STATUS,
                     com.kapti.data.Order.Fields.CREATIONTIME,
                     com.kapti.data.Order.Fields.EXECUTIONTIME,
                     com.kapti.data.Order.Fields.EXPIRATIONTIME,
                     com.kapti.data.Order.Fields.SECONDAIRYLIMIT,
                     com.kapti.data.Order.Fields.PRICE));
         }
 
         return oVector;
     }
 
     public int Create(HashMap<String, Object> iDetails) throws StockPlayException {
         // Get DAO reference
         GenericDAO<com.kapti.data.Order, Integer> orDAO = getDAO().getOrderDAO();
 
         // Restrict the input hash
         if (! getRole().isBackendAdmin()) {   // TODO: isOrderAdmin
             for (String tKey : iDetails.keySet()) {
                 if (tKey.equalsIgnoreCase(Order.Fields.USER.toString())) {
                    throw new InvocationException(InvocationException.Type.BAD_REQUEST, "you cannot edit other user's orders");
                 }
             }
            iDetails.put(Order.Fields.USER.toString(), getUser().getId());
         }
 

         // Instantiate a new order
         Order tOrder = Order.fromStruct(iDetails);
         tOrder.applyStruct(iDetails);
         tOrder.setStatus(OrderStatus.ACCEPTED);
         tOrder.setCreationTime(DateHelper.convertCalendar(Calendar.getInstance(), TimeZone.getTimeZone("GMT")).getTime());
         return orDAO.create(tOrder);
 
     }
 
     public boolean Modify(String iFilter, HashMap<String, Object> iDetails) throws StockPlayException {
         // Get DAO reference
         GenericDAO<com.kapti.data.Order, Integer> orDAO = getDAO().getOrderDAO();
 
         // Create a filter
         Parser parser = Parser.getInstance();
         Filter filter = null;
         Filter base = parser.parse(iFilter);
         if (getRole().isBackendAdmin()) {   // TODO: isOrderAdmin
             filter = base;
         } else {
             Filter user = parser.parse("id == '" + getUser().getId() + "'i");
             filter = Filter.merge(RelationAnd.class, base, user);
         }
 
         // Get the exchanges we need to modify
         Collection<com.kapti.data.Order> tOrders = orDAO.findByFilter(filter);
 
         // Now apply the cancelation
         boolean success = true;
         for (com.kapti.data.Order tOrder : tOrders) {
             tOrder.applyStruct(iDetails);
             if(!orDAO.update(tOrder))
                 success = false;
         }
         return success;
     }
 
     public int Cancel(String iFilter) throws StockPlayException {
         // Get DAO reference
         GenericDAO<com.kapti.data.Order, Integer> orDAO = getDAO().getOrderDAO();
 
         // Create a filter
         Parser parser = Parser.getInstance();
         Filter filter = null;
         Filter base = parser.parse(iFilter);
         if (getRole().isBackendAdmin()) {   // TODO: isOrderAdmin
             filter = base;
         } else {
             Filter user = parser.parse("id == '" + getUser().getId() + "'i");
             filter = Filter.merge(RelationAnd.class, base, user);
         }
 
         // Get the exchanges we need to modify
         Collection<com.kapti.data.Order> tOrders = orDAO.findByFilter(filter);
 
         // Now apply the cancelation
         for (com.kapti.data.Order tOrder : tOrders) {
             tOrder.setStatus(OrderStatus.CANCELLED);
             orDAO.update(tOrder);
         }
 
         return 1;
     }
 }
