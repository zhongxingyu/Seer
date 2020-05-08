 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman.projects;
 
 import com.google.common.collect.ImmutableList;
 import com.stackframe.sarariman.AbstractLinkable;
 import com.stackframe.sarariman.Audit;
 import com.stackframe.sarariman.DateUtils;
 import com.stackframe.sarariman.Directory;
 import com.stackframe.sarariman.Employee;
 import com.stackframe.sarariman.LaborCategory;
 import com.stackframe.sarariman.LaborCategoryAssignment;
 import com.stackframe.sarariman.NamedResource;
 import com.stackframe.sarariman.NamedResourceImpl;
 import com.stackframe.sarariman.OrganizationHierarchy;
 import com.stackframe.sarariman.PeriodOfPerformance;
 import com.stackframe.sarariman.ProjectFundingAudit;
 import com.stackframe.sarariman.ProjectLineItemAudit;
 import com.stackframe.sarariman.ProjectOrgChartAudit;
 import com.stackframe.sarariman.ProjectPeriodOfPerformanceAudit;
 import com.stackframe.sarariman.Week;
 import com.stackframe.sarariman.Workdays;
 import com.stackframe.sarariman.clients.Client;
 import com.stackframe.sarariman.clients.Clients;
 import com.stackframe.sarariman.lineitems.LineItem;
 import com.stackframe.sarariman.tasks.Task;
 import com.stackframe.sarariman.tasks.Tasks;
 import static com.stackframe.sql.SQLUtilities.convert;
 import java.math.BigDecimal;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import javax.sql.DataSource;
 
 /**
  *
  * @author mcculley
  */
 public class ProjectImpl extends AbstractLinkable implements Project {
 
     private final int id;
     private final DataSource dataSource;
     private final OrganizationHierarchy organizationHierarchy;
     private final Directory directory;
     private final Tasks tasks;
     private final Projects projects;
     private final String servletPath;
     private final Clients clients;
     private final Workdays workdays;
     private final Map<Long, LaborCategory> categoriesById;
     private final Collection<LaborCategoryAssignment> projectBillRates;
 
     ProjectImpl(int id, DataSource dataSource, OrganizationHierarchy organizationHierarchy, Directory directory, Tasks tasks,
                 Projects projects, String servletPath, Clients clients, Workdays workdays,
                 Map<Long, LaborCategory> categoriesById, Collection<LaborCategoryAssignment> projectBillRates) {
         this.id = id;
         this.dataSource = dataSource;
         this.organizationHierarchy = organizationHierarchy;
         this.directory = directory;
         this.tasks = tasks;
         this.projects = projects;
         this.servletPath = servletPath;
         this.clients = clients;
         this.workdays = workdays;
         this.categoriesById = categoriesById;
         this.projectBillRates = projectBillRates;
     }
 
     public int getId() {
         return id;
     }
 
     public String getName() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT name FROM projects WHERE id = ?");
                 try {
                     s.setInt(1, id);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         assert hasRow;
                         return r.getString("name");
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public void setName(String name) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("UPDATE projects SET name = ? WHERE id = ?");
                 try {
                     s.setString(1, name);
                     s.setInt(2, id);
                     int numRows = s.executeUpdate();
                     assert numRows == 1;
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public String getContract() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT contract_number FROM projects WHERE id = ?");
                 try {
                     s.setInt(1, id);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         assert hasRow;
                         return r.getString("contract_number");
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public void setContract(String contract) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("UPDATE projects SET contract_number = ? WHERE id = ?");
                 try {
                     s.setString(1, contract);
                     s.setInt(2, id);
                     int numRows = s.executeUpdate();
                     assert numRows == 1;
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public String getSubcontract() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT subcontract_number FROM projects WHERE id = ?");
                 try {
                     s.setInt(1, id);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         assert hasRow;
                         return r.getString("subcontract_number");
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public void setSubcontract(String subcontract) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("UPDATE projects SET subcontract_number = ? WHERE id = ?");
                 try {
                     s.setString(1, subcontract);
                     s.setInt(2, id);
                     int numRows = s.executeUpdate();
                     assert numRows == 1;
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public String getPurchaseOrder() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT purchase_order FROM projects WHERE id = ?");
                 try {
                     s.setInt(1, id);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         assert hasRow;
                         return r.getString("purchase_order");
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public void setPurchaseOrder(String purchaseOrder) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("UPDATE projects SET purchase_order = ? WHERE id = ?");
                 try {
                     s.setString(1, purchaseOrder);
                     s.setInt(2, id);
                     int numRows = s.executeUpdate();
                     assert numRows == 1;
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public String getInvoiceText() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT invoice_text FROM projects WHERE id = ?");
                 try {
                     s.setInt(1, id);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         assert hasRow;
                         return r.getString("invoice_text");
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public void setInvoiceText(String text) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("UPDATE projects SET invoice_text = ? WHERE id = ?");
                 try {
                     s.setString(1, text);
                     s.setInt(2, id);
                     int numRows = s.executeUpdate();
                     assert numRows == 1;
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public BigDecimal getFunded() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT funded FROM projects WHERE id = ?");
                 try {
                     s.setInt(1, id);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         assert hasRow;
                         return r.getBigDecimal("funded");
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public void setFunded(BigDecimal funded) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("UPDATE projects SET funded = ? WHERE id = ?");
                 try {
                     s.setBigDecimal(1, funded);
                     s.setInt(2, id);
                     int numRows = s.executeUpdate();
                     assert numRows == 1;
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public BigDecimal getPreviouslyBilled() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT previously_billed FROM projects WHERE id = ?");
                 try {
                     s.setInt(1, id);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         assert hasRow;
                         return r.getBigDecimal("previously_billed");
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public void setPreviouslyBilled(BigDecimal previouslyBilled) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("UPDATE projects SET previously_billed = ? WHERE id = ?");
                 try {
                     s.setBigDecimal(1, previouslyBilled);
                     s.setInt(2, id);
                     int numRows = s.executeUpdate();
                     assert numRows == 1;
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public BigDecimal getODCFee() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT odc_fee FROM projects WHERE id = ?");
                 try {
                     s.setInt(1, id);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         assert hasRow;
                         return r.getBigDecimal("odc_fee");
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public void setODCFee(BigDecimal fee) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("UPDATE projects SET odc_fee = ? WHERE id = ?");
                 try {
                     s.setBigDecimal(1, fee);
                     s.setInt(2, id);
                     int numRows = s.executeUpdate();
                     assert numRows == 1;
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public boolean isManager(Employee employee) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement ps = connection.prepareStatement("SELECT * FROM project_managers WHERE employee=? AND project=?");
                 try {
                     ps.setInt(1, employee.getNumber());
                     ps.setLong(2, id);
                     ResultSet rs = ps.executeQuery();
                     try {
                         return rs.first();
                     } finally {
                         rs.close();
                     }
                 } finally {
                     ps.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public boolean isCostManager(Employee employee) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement ps = connection.prepareStatement("SELECT * FROM project_cost_managers WHERE employee=? AND project=?");
                 try {
                     ps.setInt(1, employee.getNumber());
                     ps.setLong(2, id);
                     ResultSet rs = ps.executeQuery();
                     try {
                         return rs.first();
                     } finally {
                         rs.close();
                     }
                 } finally {
                     ps.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public Client getClient() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT customer FROM projects WHERE id = ?");
                 try {
                     s.setInt(1, id);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         assert hasRow;
                         int client_id = r.getInt("customer");
                         if (client_id == 0) {
                             return null;
                         } else {
                             return clients.get(client_id);
                         }
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public void setClient(Client client) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("UPDATE projects SET customer = ? WHERE id = ?");
                 try {
                     s.setInt(1, client.getId());
                     s.setInt(2, id);
                     int numRows = s.executeUpdate();
                     assert numRows == 1;
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public PeriodOfPerformance getPoP() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT pop_start, pop_end FROM projects WHERE id = ?");
                 try {
                     s.setInt(1, id);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.next();
                         assert hasRow;
                         Date pop_start = r.getDate("pop_start");
                         Date pop_end = r.getDate("pop_end");
                         return new PeriodOfPerformance(pop_start, pop_end);
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public void setPoP(PeriodOfPerformance pop) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("UPDATE projects SET pop_start = ?, pop_end = ? WHERE id = ?");
                 try {
                     s.setDate(1, convert(pop.getStart()));
                     s.setDate(2, convert(pop.getEnd()));
                     s.setInt(3, id);
                     int numRows = s.executeUpdate();
                     assert numRows == 1;
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public int getTerms() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT terms FROM projects WHERE id = ?");
                 try {
                     s.setInt(1, id);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         assert hasRow;
                         return r.getInt("terms");
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public void setTerms(int terms) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("UPDATE projects SET terms = ? WHERE id = ?");
                 try {
                     s.setInt(1, terms);
                     s.setInt(2, id);
                     int numRows = s.executeUpdate();
                     assert numRows == 1;
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public boolean isActive() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT active FROM projects WHERE id = ?");
                 try {
                     s.setInt(1, id);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         assert hasRow;
                         return r.getBoolean("active");
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public void setActive(boolean active) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("UPDATE projects SET active = ? WHERE id = ?");
                 try {
                     s.setBoolean(1, active);
                     s.setInt(2, id);
                     int numRows = s.executeUpdate();
                     assert numRows == 1;
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public Collection<Audit> getAudits() {
         Collection<Audit> c = new ArrayList<Audit>();
         c.add(new ProjectOrgChartAudit(this, dataSource, organizationHierarchy, directory));
         c.add(new ProjectPeriodOfPerformanceAudit(id, projects));
         c.add(new ProjectFundingAudit(id, projects));
         c.add(new ProjectLineItemAudit(id, dataSource, projects));
         return c;
     }
 
     public BigDecimal getExpended() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT SUM(TRUNCATE(c.rate * h.duration + 0.009, 2)) AS costTotal " +
                         "FROM hours AS h " +
                         "JOIN tasks AS t on h.task = t.id " +
                         "JOIN projects AS p on p.id = t.project " +
                         "JOIN labor_category_assignments AS a ON (a.employee = h.employee AND h.date >= a.pop_start AND h.date <= a.pop_end) " +
                         "JOIN labor_categories AS c ON (c.id = a.labor_category AND h.date >= c.pop_start AND h.date <= c.pop_end AND c.project = p.id)" +
                         "WHERE t.project = ? AND t.billable = TRUE and h.duration > 0");
                 try {
                     s.setInt(1, id);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.next();
                         assert hasRow;
                         return r.getBigDecimal("costTotal");
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public Iterable<Date> getDaysBilled() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement ps = connection.prepareStatement("SELECT DISTINCT(date) AS date " +
                         "FROM hours AS h " +
                         "JOIN tasks AS t on h.task = t.id " +
                         "JOIN projects AS p on p.id = t.project " +
                         "JOIN labor_category_assignments AS a ON (a.employee = h.employee AND h.date >= a.pop_start AND h.date <= a.pop_end) " +
                         "JOIN labor_categories AS c ON (c.id = a.labor_category AND h.date >= c.pop_start AND h.date <= c.pop_end AND c.project = p.id) " +
                         "WHERE p.id = ? AND t.billable = TRUE;");
                 try {
                     ps.setLong(1, id);
                     ResultSet rs = ps.executeQuery();
                     try {
                         Collection<Date> days = new TreeSet<Date>();
                         while (rs.next()) {
                             days.add(rs.getDate("date"));
                         }
 
                         return days;
                     } finally {
                         rs.close();
                     }
                 } finally {
                     ps.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public Collection<LineItem> getLineItems() {
         try {
             return LineItem.getLineItems(dataSource, id);
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public Collection<Task> getTasks() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement ps = connection.prepareStatement("SELECT t.id AS task_id " +
                         "FROM tasks AS t " +
                         "WHERE t.project = ?");
                 ps.setInt(1, id);
                 try {
                     ResultSet resultSet = ps.executeQuery();
                     try {
                         Collection<Task> list = new ArrayList<Task>();
                         while (resultSet.next()) {
                             int task_id = resultSet.getInt("task_id");
                             list.add(tasks.get(task_id));
                         }
 
                         return list;
                     } finally {
                         resultSet.close();
                     }
                 } finally {
                     ps.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public Iterable<Employee> getAdministrativeAssistants() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement ps = connection.prepareStatement(
                         "SELECT assistant " +
                         "FROM project_administrative_assistants " +
                         "WHERE project = ?");
                 ps.setInt(1, id);
                 try {
                     ResultSet resultSet = ps.executeQuery();
                     try {
                         ImmutableList.Builder<Employee> listBuilder = ImmutableList.<Employee>builder();
                         while (resultSet.next()) {
                             int task_id = resultSet.getInt("assistant");
                             listBuilder.add(directory.getByNumber().get(task_id));
                         }
 
                         return listBuilder.build();
                     } finally {
                         resultSet.close();
                     }
                 } finally {
                     ps.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public Iterable<Employee> getManagers() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement ps = connection.prepareStatement(
                         "SELECT employee " +
                         "FROM project_managers " +
                         "WHERE project = ?");
                 ps.setInt(1, id);
                 try {
                     ResultSet resultSet = ps.executeQuery();
                     try {
                         ImmutableList.Builder<Employee> listBuilder = ImmutableList.<Employee>builder();
                         while (resultSet.next()) {
                             int task_id = resultSet.getInt("employee");
                             listBuilder.add(directory.getByNumber().get(task_id));
                         }
 
                         return listBuilder.build();
                     } finally {
                         resultSet.close();
                     }
                 } finally {
                     ps.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public Iterable<Employee> getCostManagers() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement ps = connection.prepareStatement(
                         "SELECT employee " +
                         "FROM project_cost_managers " +
                         "WHERE project = ?");
                 ps.setInt(1, id);
                 try {
                     ResultSet resultSet = ps.executeQuery();
                     try {
                         ImmutableList.Builder<Employee> listBuilder = ImmutableList.<Employee>builder();
                         while (resultSet.next()) {
                             int task_id = resultSet.getInt("employee");
                             listBuilder.add(directory.getByNumber().get(task_id));
                         }
 
                         return listBuilder.build();
                     } finally {
                         resultSet.close();
                     }
                 } finally {
                     ps.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public URI getURI() {
         return URI.create(String.format("%s?id=%d", servletPath, id));
     }
 
     private Collection<Date> getWorkedDates() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement ps = connection.prepareStatement(
                         "SELECT DISTINCT(h.date) " +
                         "FROM hours AS h " +
                         "JOIN tasks AS t ON h.task = t.id " +
                         "JOIN projects AS p ON p.id = t.project " +
                         "WHERE project = ?");
                 ps.setInt(1, id);
                 try {
                     ResultSet resultSet = ps.executeQuery();
                     try {
                         ImmutableList.Builder<Date> listBuilder = ImmutableList.<Date>builder();
                         while (resultSet.next()) {
                             Date date = resultSet.getDate("date");
                             listBuilder.add(date);
                         }
 
                         return listBuilder.build();
                     } finally {
                         resultSet.close();
                     }
                 } finally {
                     ps.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public Iterable<Week> getWorkedWeeks() {
         Set<Week> weeks = new TreeSet<Week>();
         Collection<Date> dates = getWorkedDates();
         for (Date date : dates) {
             Week week = DateUtils.week(date);
             weeks.add(week);
         }
 
         return weeks;
     }
 
     public void delete() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement ps = connection.prepareStatement("DELETE FROM projects WHERE id=?");
                 try {
                     ps.setLong(1, id);
                     ps.executeUpdate();
                 } finally {
                     ps.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public Set<Employee> getCurrentlyAssigned() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement(
                         "SELECT DISTINCT(ta.employee) AS employee " +
                         "FROM projects AS p " +
                         "JOIN tasks AS t ON t.project = p.id " +
                         "JOIN task_assignments AS ta ON ta.task = t.id " +
                         "WHERE p.id = ? AND " +
                         "p.active = TRUE ");
                 try {
                     s.setInt(1, id);
                     ResultSet rs = s.executeQuery();
                     try {
                         Set<Employee> c = new HashSet<Employee>();
                         while (rs.next()) {
                             int employee = rs.getInt("employee");
                             c.add(directory.getByNumber().get(employee));
                         }
                         return c;
                     } finally {
                         rs.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public Collection<NamedResource> getResources() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement(
                         "SELECT URL, description " +
                         "FROM project_links " +
                         "WHERE project = ?");
                 try {
                     s.setInt(1, id);
                     ResultSet rs = s.executeQuery();
                     try {
                         ImmutableList.Builder<NamedResource> b = ImmutableList.<NamedResource>builder();
                         while (rs.next()) {
                             URL url = rs.getURL("URL");
                             String description = rs.getString("description");
                             try {
                                 b.add(new NamedResourceImpl(url.toURI(), description));
                             } catch (URISyntaxException e) {
                                 throw new RuntimeException(e);
                             }
                         }
 
                         return b.build();
                     } finally {
                         rs.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public Collection<LaborProjection> getLaborProjections() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement ps = connection.prepareStatement(
                         "SELECT labor_projection.id FROM labor_projection " +
                         "JOIN tasks ON labor_projection.task = tasks.id " +
                         "WHERE tasks.project = ?");
                 ps.setInt(1, id);
                 try {
                     ResultSet resultSet = ps.executeQuery();
                     try {
                         Collection<LaborProjection> list = new ArrayList<LaborProjection>();
                         while (resultSet.next()) {
                             int laborProjectionId = resultSet.getInt("labor_projection.id");
                             list.add(new LaborProjectionImpl(laborProjectionId, dataSource, directory, tasks));
                         }
 
                         return list;
                     } finally {
                         resultSet.close();
                     }
                 } finally {
                     ps.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public Collection<ProjectedExpense> getProjectedExpenses(PeriodOfPerformance pop) {
         Collection<ProjectedExpense> result = new ArrayList<ProjectedExpense>();
         Collection<LaborProjection> laborProjections = getLaborProjections();
         for (LaborProjection projection : laborProjections) {
             PeriodOfPerformance intersection = projection.getPeriodOfPerformance().intersection(pop);
             ProjectedExpense projectedExpense = new ProjectedExpenseImpl(projection.getEmployee(), intersection,
                                                                         projection.getTask(), projection.getUtilization(),
                                                                         workdays, categoriesById, projectBillRates, dataSource);
             result.add(projectedExpense);
         }
 
         return result;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 23 * hash + this.id;
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final ProjectImpl other = (ProjectImpl)obj;
         if (this.id != other.id) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return Integer.toString(id);
     }
 
 }
