 /*
  * IndexDAO.java
  * StockPlay - Abastracte Data access object laag voor de indexen
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
 
 package com.kapti.data.persistence.oracle;
 
 import com.kapti.exceptions.*;
 import com.kapti.data.*;
 import com.kapti.data.persistence.GenericDAO;
 import com.kapti.filter.Filter;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Collection;
 
 public class IndexDAO implements GenericDAO<Index, String> {
     
     private static final String SELECT_INDEX = "SELECT name, exchange, symbol FROM indexes WHERE isin = ?";
     private static final String SELECT_INDEXES = "SELECT name, exchange, isin, symbol FROM indexes";
     private static final String INSERT_INDEX = "INSERT INTO indexes(name, exchange, isin, symbol) VALUES(?, ?, ?, ?)";
     private static final String UPDATE_INDEX = "UPDATE indexes SET name = ?, exchange = ?, symbol = ? WHERE isin = ?";
     private static final String DELETE_INDEX = "DELETE FROM indexes WHERE isin = ?";
 
     private static IndexDAO instance = new IndexDAO();
 
     private  IndexDAO(){}
 
     public static IndexDAO getInstance() {
         return instance;
     }
 
     public Index findById(String isin) throws StockPlayException {
         Connection conn = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {
             try {
                 conn = OracleConnection.getConnection();
                 stmt = conn.prepareStatement(SELECT_INDEX);
 
                 stmt.setString(1, isin);
 
                 rs = stmt.executeQuery();
                 if (rs.next()) {
                     Index tIndex = new Index(isin, rs.getString("symbol"), rs.getString("exchange"));
                     tIndex.setName(rs.getString("name"));
                     return tIndex;
                 } else {
                     throw new InvocationException(InvocationException.Type.NON_EXISTING_ENTITY, "There is no index with isin '" + isin + "'");
                 }
             } finally {
                 if (rs != null) {
                     rs.close();
                 }
                 if (stmt != null) {
                     stmt.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             }
         } catch (SQLException ex) {
             throw new DBException(ex);
         }
     }
 
     public Collection<Index> findByFilter(Filter iFilter) throws StockPlayException, FilterException {
         if (iFilter.empty())
             return findAll();
         Connection conn = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {
             try {
                 conn = OracleConnection.getConnection();
                 stmt = conn.prepareStatement(SELECT_INDEXES + " WHERE " + (String)iFilter.compile());
 
                 rs = stmt.executeQuery();
                 ArrayList<Index> list = new ArrayList<Index>();
                 while (rs.next()) {
                     Index tIndex = new Index(rs.getString("isin"), rs.getString("symbol"), rs.getString("exchange"));
                     tIndex.setName(rs.getString("name"));
                     list.add(tIndex);
                }
                 return list;
             } finally {
                 if (rs != null) {
                     rs.close();
                 }
                 if (stmt != null) {
                     stmt.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             }
         } catch (SQLException ex) {
             throw new DBException(ex);
         }
 
     }
 
     public Collection<Index> findAll() throws StockPlayException {
         Connection conn = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {
             try {
                 conn = OracleConnection.getConnection();
                 stmt = conn.prepareStatement(SELECT_INDEXES);
 
                 rs = stmt.executeQuery();
                 ArrayList<Index> list = new ArrayList<Index>();
                 while (rs.next()) {
                     Index tIndex = new Index(rs.getString("isin"), rs.getString("symbol"), rs.getString("exchange"));
                     tIndex.setName(rs.getString("name"));
                     list.add(tIndex);
                }
                 return list;
             } finally {
                 if (rs != null) {
                     rs.close();
                 }
                 if (stmt != null) {
                     stmt.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             }
         } catch (SQLException ex) {
             throw new DBException(ex);
         }
     }
 
     /**
      * Maakt de opgegeven index aan in de database. De id van het object wordt genegeerd, en er wordt door de database mbv. een sequence een uniek nummer gecreërd.
      * @param entity Het object dat moet worden aangemaakt in de database
      * @return 
      * @throws StockPlayException
      */
     public int create(Index entity) throws StockPlayException {
         Connection conn = null;
         PreparedStatement stmt = null;
         PreparedStatement stmtID = null;
         ResultSet rs = null;
         try {
             try {
                 conn = OracleConnection.getConnection();
                 stmt = conn.prepareStatement(INSERT_INDEX);
 
                 stmt.setString(1, entity.getName());
                 stmt.setString(2, entity.getExchange());
                stmt.setString(3, entity.getIsin());
                stmt.setString(4, entity.getSymbol());
 
                 return stmt.executeUpdate();
             } finally {
                 if (rs != null) {
                     rs.close();
                 }
                 if (stmt != null) {
                     stmt.close();
                 }
                 if(stmtID != null){
                     stmtID.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             }
         } catch (SQLException ex) {
             throw new DBException(ex);
         }
     }
 
     /**
      * Past de Index met de opgegeven id aan in de database.
      * @param entity Het object dat moet worden aangemaakt in de database
      * @return
      * @throws StockPlayException
      */
     public boolean update(Index entity) throws StockPlayException {
         Connection conn = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {
             try {
                 conn = OracleConnection.getConnection();
                 stmt = conn.prepareStatement(UPDATE_INDEX);
 
                 stmt.setString(1, entity.getName());
                 stmt.setString(2, entity.getExchange());
                 stmt.setString(3, entity.getSymbol());
                 stmt.setString(4, entity.getIsin());
 
                 return stmt.executeUpdate() == 1;
 
 
             } finally {
                 if (rs != null) {
                     rs.close();
                 }
                 if (stmt != null) {
                     stmt.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             }
         } catch (SQLException ex) {
             throw new DBException(ex);
         }
     }
 
     /**
      * Verwijdert de index met de id van het object uit de database.
      * @param entity Enkel de Id van het object is van belang
      * @return True als het verwijderen van de index gelukt is.
      * @throws StockPlayException
      */
     public boolean delete(Index entity) throws StockPlayException {
         Connection conn = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {
             try {
                 conn = OracleConnection.getConnection();
                 stmt = conn.prepareStatement(DELETE_INDEX);
 
                 stmt.setString(1, entity.getIsin());
 
                 return stmt.executeUpdate() == 1;
 
 
             } finally {
                 if (rs != null) {
                     rs.close();
                 }
                 if (stmt != null) {
                     stmt.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             }
         } catch (SQLException ex) {
             throw new DBException(ex);
         }
     }
 }
