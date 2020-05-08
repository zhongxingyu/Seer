 /**
  * Copyright (C) 2009 Anthonin Bonnefoy and David Duponchel
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *         http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package airline.servlet.admin;
 
 import airline.criteria.Restriction;
 import airline.criteria.enumeration.SqlConstraints;
 import airline.criteria.model.SelectRequest;
 import airline.dao.AirlineDAO;
 import airline.model.Table;
 import airline.model.TableRow;
 import airline.model.TableColumn;
 import airline.servlet.AbstractInjectableServlet;
 import airline.servlet.enumeration.MessageError;
 import airline.manager.AirlineManager;
 import com.google.inject.Inject;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.*;
 import java.net.URLDecoder;
 import java.sql.SQLException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: dev
  * Date: Feb 4, 2009
  * Time: 9:21:39 AM
  * To change this template use File | Settings | File Templates.
  */
 public class SFW extends AbstractInjectableServlet {
     private Map<String, Table> tables;
     private List<TableColumn> columns;
     private AirlineDAO airlineDAO;
 
     @Inject
     public void setAirlineDAO(AirlineManager airlineDAO) {
         this.airlineDAO = airlineDAO;
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         tables = airlineDAO.getTables();
         SelectRequest selectRequest = new SelectRequest();
         boolean canDoRequest = true;
 
         Table from = handleFrom(request);
 
         if (from != null) {
             columns = airlineDAO.getTableColumns(from);
             request.setAttribute("columns", columns);
             canDoRequest = handleSelect(selectRequest, request) && canDoRequest;
             canDoRequest = handleWhere(selectRequest, request) && canDoRequest;
         } else {
             canDoRequest = false;
         }
 
         RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/sfw.jsp");
         if (canDoRequest) {
             try {
                 doRequest(selectRequest, request);
             } catch (SQLException e) {
                 request.setAttribute("error.type", MessageError.SQL_ERROR);
                 request.setAttribute("error.exception", e);
                dispatcher = this.getServletContext().getRequestDispatcher("/error.jsp");
             }
         }
 
         dispatcher.forward(request, response);
     }
 
     private void doRequest(SelectRequest selectRequest, HttpServletRequest request) throws SQLException {
         Set<TableRow> result = airlineDAO.executeRequest(selectRequest);
         request.setAttribute("rows", result);
     }
 
     private Table handleFrom(HttpServletRequest request) {
         request.setAttribute("tables", tables);
 
         String from = request.getParameter("from");
 
         if (from != null && tables.containsKey(from)) {
             request.setAttribute("selectedTable", tables.get(from));
             return tables.get(from);
         } else {
             return null;
         }
     }
 
     private boolean handleSelect(SelectRequest selectRequest, HttpServletRequest request) {
 
         String[] params = request.getParameterValues("select");
 
         if (params == null || params.length == 0) {
             return false;
         }
 
         List<TableColumn> selected = new ArrayList<TableColumn>();
         List<String> paramList = Arrays.asList(params);
 
         for (TableColumn col : columns) {
             if (paramList.contains(col.getName())) {
                 selected.add(col);
                 selectRequest.addColumn(col);
             }
         }
 
         request.setAttribute("selectedFields", selected);
 
         return !selected.isEmpty();
     }
 
     private boolean handleWhere(SelectRequest selectRequest, HttpServletRequest request) {
 
         String whereField = request.getParameter("whereField");
         TableColumn column = null;
 
         if (whereField != null) {
             Iterator<TableColumn> iterator = columns.iterator();
             while (column == null && iterator.hasNext()) {
                 TableColumn current = iterator.next();
                 if (current.getName().equals(whereField)) {
                     column = current;
                 }
             }
         }
 
         String whereCond = null;
         try {
             whereCond = URLDecoder.decode(request.getParameter("whereCond"), "UTF-8");
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
         SqlConstraints cond = null;
 
        System.out.println("checking whereCond");
         if (whereCond != null) {
            System.out.println("whereCond : " + whereCond);
             if (">".equals(whereCond)) {
                 cond = SqlConstraints.GT;
             } else if (">=".equals(whereCond)) {
                 cond = SqlConstraints.GE;
             } else if ("<".equals(whereCond)) {
                 cond = SqlConstraints.LT;
             } else if ("<=".equals(whereCond)) {
                 cond = SqlConstraints.LE;
             } else if ("=".equals(whereCond)) {
                 cond = SqlConstraints.EQ;
             } else if ("!=".equals(whereCond)) {
                 cond = SqlConstraints.NEQ;
             } else if ("like".equals(whereCond)) {
                 cond = SqlConstraints.LIKE;
             } else if ("ilike".equals(whereCond)) {
                 cond = SqlConstraints.ILIKE;
             }
            System.out.println("result : " + cond);
         }
 
         String whereVal = request.getParameter("whereVal");
 
         request.setAttribute("whereField", column);
         request.setAttribute("whereCond", cond);
         request.setAttribute("whereVal", whereVal);
 
         if (column != null && cond != null && whereVal != null) {
             Restriction restriction = new Restriction();
             restriction.constraint(column, whereVal, cond);
             selectRequest.addRestriction(restriction);
 
             return true;
         } else {
             return false;
         }
     }
 }
