 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package netcracker.dao;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author lasha.k
  */
 public class EmployeeDAOImpl implements EmployeeDAO {
 
     private static final Logger log =
             Logger.getLogger(EmployeeDAOImpl.class.getName());
 
     public EmployeeDAOImpl() {
     }
 
     @Override
     public boolean createEmployee(Employee emp) {
         PreparedStatement stmtInsert = null;
         Statement stmtCheckLogin = null;
         ResultSet resultCheckLogin = null;
         Connection conn = DAOFactory.createConnection();
         try {
             StringBuilder sbCheckLogin = new StringBuilder();
             sbCheckLogin.append("SELECT count(id_employee) FROM ");
             sbCheckLogin.append(DAOConstants.EmpTableName);
             sbCheckLogin.append(" WHERE login like '").append(emp.getLogin()).append("'");
             stmtCheckLogin = conn.createStatement();
             resultCheckLogin = stmtCheckLogin.executeQuery(sbCheckLogin.toString());
             int rowsCount = 0;
             while (resultCheckLogin.next()) {
                 rowsCount = resultCheckLogin.getInt(1);
             }
             if (rowsCount > 0) {
                 return false;
             } else {
                 StringBuilder sbInsert = new StringBuilder();
                 sbInsert.append("INSERT INTO ");
                 sbInsert.append(DAOConstants.EmpTableName);
                 sbInsert.append(" (login, password, first_name, last_name, email, id_role)");
                 sbInsert.append(" VALUES(");
                 sbInsert.append("?, ?, ?, ?, ?, ?)");
                 stmtInsert = conn.prepareStatement(sbInsert.toString());
                 stmtInsert.setString(1, emp.getLogin());
                 stmtInsert.setString(2, this.getPasswordHash(emp.getPassword()));
                 stmtInsert.setString(3, emp.getFirstName());
                 stmtInsert.setString(4, emp.getLastName());
                 stmtInsert.setString(5, emp.getEmail());
                 stmtInsert.setInt(6, emp.getIdRole());
 
                 int rows = stmtInsert.executeUpdate();
                 if (rows != 1) {
                     log.info("Error in createEmployee");
                 }
             }
         } catch (SQLException ex) {
             log.log(Level.SEVERE, null, ex);
         } finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closePreparedStatement(stmtInsert);
         }
         return true;
     }
 
     @Override
     public boolean deleteEmployeeById(int idEmployee) {
         Connection conn = DAOFactory.createConnection();
         PreparedStatement stmtDelete = null;
         try {
             StringBuilder sbDelete = new StringBuilder();
             sbDelete.append("DELETE FROM ");
             sbDelete.append(DAOConstants.EmpTableName);
             sbDelete.append(" WHERE id_employee = ?");
             stmtDelete = conn.prepareStatement(sbDelete.toString());
             stmtDelete.setInt(1, idEmployee);
             stmtDelete.executeUpdate();
         } catch (SQLException ex) {
             log.log(Level.SEVERE, null, ex);
         } finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closePreparedStatement(stmtDelete);
         }
         return true;
     }
 
     @Override
     public Employee getEmployeeById(int idEmployee) {
         Statement stmtSelect = null;
         Connection conn = DAOFactory.createConnection();
         ResultSet result = null;
         Employee emp = null;
         try {
             StringBuilder sbSelect = new StringBuilder();
             sbSelect.append("SELECT login, password, first_name, last_name, email, id_role FROM ");
             sbSelect.append(DAOConstants.EmpTableName);
             sbSelect.append(" WHERE id_employee = ").append(idEmployee);
             stmtSelect = conn.createStatement();
             result = stmtSelect.executeQuery(sbSelect.toString());
             int rowsCount = 0;
             while (result.next()) {
                 emp = new Employee(result.getString(1), result.getString(2),
                         result.getString(3), result.getString(4),
                         result.getString(5), result.getInt(6));
                 rowsCount++;
             }
             if (rowsCount < 1) {
                 log.info("No employees found with id ".concat(
                         Integer.toString(idEmployee)));
             }
         } catch (SQLException ex) {
             log.log(Level.SEVERE, null, ex);
         } finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closeStatement(stmtSelect);
         }
         return emp;
     }
 
     @Override
     public List<String> getAllRoles() {
         List<String> rolesList = new ArrayList<String>();
         Connection conn = DAOFactory.createConnection();
         Statement stmtSelect = null;
         ResultSet res = null;
         try {
             StringBuilder sbSelect = new StringBuilder();
             sbSelect.append("SELECT id_role, role_name FROM ");
             sbSelect.append(DAOConstants.RolesTableName);
             stmtSelect = conn.createStatement();
             res = stmtSelect.executeQuery(sbSelect.toString());
             int rowsCount = 0;
             while (res.next()) {
                 rolesList.add(res.getString(1));
                 rolesList.add(res.getString(2));
                 rowsCount++;
             }
             if (rowsCount <= 0) {
                 log.info("No roles found");
             }
         } catch (SQLException ex) {
             log.log(Level.SEVERE, null, ex);
         } finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closeStatement(stmtSelect);
         }
         return rolesList;
     }
 
     @Override
     public List<Employee> getEmployeeListByRole(String roleName) {
         List<Employee> empList = new ArrayList<Employee>();
         Statement stmtSelect = null;
         Connection conn = DAOFactory.createConnection();
         ResultSet result = null;
         try {
             StringBuilder sbSelect = new StringBuilder();
             sbSelect.append("SELECT login, password, first_name, last_name,").append(" email, id_role FROM ").append(DAOConstants.EmpTableName).append(" WHERE id_role = (SELECT id_role FROM roles").append(" WHERE role_name = '").append(roleName).append("')");
 
             stmtSelect = conn.createStatement();
             result = stmtSelect.executeQuery(sbSelect.toString());
             while (result.next()) {
                 empList.add(new Employee(
                         result.getString(1),
                         result.getString(2),
                         result.getString(3),
                         result.getString(4),
                         result.getString(5),
                         result.getInt(6)));
             }
         } catch (SQLException ex) {
             log.log(Level.SEVERE, null, ex);
         } finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closeStatement(stmtSelect);
         }
         return empList;
     }
 
     @Override
     public List<Employee> getInterviewerList() {
         return this.getEmployeeListByRole("Interviewer");
     }
 
     @Override
     public List<Employee> getHRList() {
         return this.getEmployeeListByRole("HR");
     }
 
     @Override
     public boolean checkPassword(String login, String password) {
         Statement stmtSelect = null;
         Connection conn = DAOFactory.createConnection();
         ResultSet result = null;
         int rowsCount = 0;
         try {
             StringBuilder sbSelect = new StringBuilder();
             sbSelect.append("SELECT login FROM ");
             sbSelect.append(DAOConstants.EmpTableName);
             sbSelect.append(" WHERE login like '%").append(login);
             sbSelect.append("%' AND password like '%");
             sbSelect.append(this.getPasswordHash(password)).append("%'");
 
             stmtSelect = conn.createStatement();
             result = stmtSelect.executeQuery(sbSelect.toString());
             while (result.next()) {
                 rowsCount++;
             }
         } catch (SQLException ex) {
             log.log(Level.SEVERE, null, ex);
         } finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closeStatement(stmtSelect);
         }
         return rowsCount > 0 ? true : false;
     }
 
     @Override
     public boolean changePassword(int idEmployee,
             String oldPassword,
             String newPassword) {
         Statement stmtSelect = null;
         Connection conn = DAOFactory.createConnection();
         ResultSet resultLogin = null;
         String login = null;
         try {
             StringBuilder sbSelect = new StringBuilder();
             sbSelect.append("SELECT login FROM ");
             sbSelect.append(DAOConstants.EmpTableName);
             sbSelect.append(" WHERE id_employee = ").append(idEmployee);
 
             stmtSelect = conn.createStatement();
             resultLogin = stmtSelect.executeQuery(sbSelect.toString());
             while (resultLogin.next()) {
                 login = resultLogin.getString(1);
             }
         } catch (SQLException ex) {
             log.log(Level.SEVERE, null, ex);
         }
 
         if (! this.checkPassword(login, oldPassword)) {
             return false;
         }
 
         PreparedStatement stmtUpdate = null;
         try {
             StringBuilder sbUpdate = new StringBuilder();
             sbUpdate.append("UPDATE ");
             sbUpdate.append(DAOConstants.EmpTableName);
             sbUpdate.append(" SET password = ? WHERE id_employee = ?");
 
             stmtUpdate = conn.prepareStatement(sbUpdate.toString());
             stmtUpdate.setString(1, this.getPasswordHash(newPassword));
             stmtUpdate.setInt(2, idEmployee);
 
             int rows = stmtUpdate.executeUpdate();
             if (rows != 1) {
                 log.info("Error in changePassword");
             }
         } catch (SQLException ex) {
             log.log(Level.SEVERE, null, ex);
         } finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closePreparedStatement(stmtUpdate);
         }
         return true;
     }
 
     @Override
     public boolean resetPassword(int idEmployee, String oldPassword) {
         return changePassword(idEmployee, oldPassword, "");
     }
 
     /**
      * Password hashing (md5)
      */
    public String getPasswordHash(String password) {
         MessageDigest md5;
         StringBuilder hexString = new StringBuilder();
         try {
             md5 = MessageDigest.getInstance("md5");
             md5.reset();
             md5.update(password.getBytes());
             byte messageDigest[] = md5.digest();
             for (int i = 0; i < messageDigest.length; i++) {
                 hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
             }
         } catch (NoSuchAlgorithmException ex) {
             log.log(Level.SEVERE, null, ex);
         }
         return hexString.toString();
     }
 
     @Override
     public int getIdEmployeeByLogin(String login) {
 
         int idEmployee = 0;
         Connection conn = DAOFactory.createConnection();
         Statement stmtSelect = null;
         ResultSet res = null;
         try {
             StringBuilder sbSelect = new StringBuilder();
             sbSelect.append("SELECT id_employee FROM ");
             sbSelect.append(DAOConstants.EmpTableName);
             sbSelect.append(" WHERE login like '").append(login).append("'");
             stmtSelect = conn.createStatement();
             res = stmtSelect.executeQuery(sbSelect.toString());
             int rowsCount = 0;
             while (res.next()) {
                 idEmployee = res.getInt(1);
                 rowsCount++;
             }
             if (rowsCount <= 0) {
                 log.info("No employee found with login ".concat(login));
             }
         } catch (SQLException ex) {
             log.log(Level.SEVERE, null, ex);
         } finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closeStatement(stmtSelect);
         }
         return idEmployee;
     }
 
     @Override
     public List<String> getAllEmailsOfHR() {
         List<String> emails = new ArrayList<String>();
         Connection conn = DAOFactory.createConnection();
         Statement stmtSelect = null;
         ResultSet res = null;
         try {
             StringBuilder sbSelect = new StringBuilder();
             sbSelect.append("SELECT email FROM ");
             sbSelect.append(DAOConstants.EmpTableName + ", " + DAOConstants.RolesTableName);
             sbSelect.append(" WHERE employees.id_role = roles.id_role ");
             sbSelect.append(" AND roles.role_name like 'HR'");
             stmtSelect = conn.createStatement();
             res = stmtSelect.executeQuery(sbSelect.toString());
             int rowsCount = 0;
             while (res.next()) {
                 emails.add(res.getString(1));
                 rowsCount++;
             }
             if (rowsCount <= 0) {
                 log.info("No emails found");
             }
         } catch (SQLException ex) {
             log.log(Level.SEVERE, null, ex);
         } finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closeStatement(stmtSelect);
         }
         return emails;
     }
 }
