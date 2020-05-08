 package de.enwida.web.dao.implementation;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 
 import de.enwida.transport.Aspect;
 import de.enwida.web.dao.interfaces.IAspectsDao;
 import de.enwida.web.model.AspectRight;
 
 public class AspectDaoImpl implements IAspectsDao {
     
     @Autowired
     private DriverManagerDataSource datasource;
 
     public List<Aspect> getAspects(int chartId) {
         // TODO Auto-generated method stub
         return Collections.emptyList();
     }
 
     public List<AspectRight> getAllAspects(long roleID) {
         String sql = "select * FROM users.rights";
         Connection conn = null;
         ArrayList<AspectRight> rights = new ArrayList<AspectRight>();
         try {
             conn = datasource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 AspectRight right = new AspectRight();
                 right.setRightID(rs.getLong("right_id"));
                right.setAspectName(rs.getString("aspect_id"));
                 right.setRoleID(rs.getLong("role_id"));
                 right.setResolution(rs.getString("resolution"));
                 right.setProduct(rs.getString("product"));
                 right.setTso(rs.getString("tso"));
                 right.setEnabled(rs.getBoolean("enabled"));
                 rights.add(right);
             }
             rs.close();
             ps.close();
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             if (conn != null) {
                 try {
                 conn.close();
                 } catch (SQLException e) {}
             }
         }
         return rights;
     }
 
 }
