 /*
  * Copyright (C) 2009-2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ContiguousSet;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSortedSet;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Range;
 import com.google.i18n.phonenumbers.NumberParseException;
 import com.google.i18n.phonenumbers.PhoneNumberUtil;
 import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
 import com.stackframe.collect.RangeUtilities;
 import com.stackframe.sarariman.outofoffice.OutOfOfficeEntry;
 import com.stackframe.sarariman.projects.Project;
 import com.stackframe.sarariman.taskassignments.DefaultTaskAssignment;
 import com.stackframe.sarariman.taskassignments.TaskAssignment;
 import com.stackframe.sarariman.tasks.Task;
 import com.stackframe.sarariman.tickets.Ticket;
 import com.stackframe.sarariman.timesheets.Timesheet;
 import com.stackframe.sarariman.vacation.VacationEntry;
 import java.math.BigDecimal;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.sql.DataSource;
 import org.joda.time.LocalDate;
 
 /**
  *
  * @author mcculley
  */
 class StackFrameEmployee extends AbstractLinkable implements Employee {
 
     private final String fullName;
 
     private final String userName;
 
     private final int number;
 
     private final boolean fulltime;
 
     private final boolean active;
 
     private final String email;
 
     private final LocalDate birthdate;
 
     private final String displayName;
 
     private final Range<java.sql.Date> periodOfService;
 
     private final byte[] photo;
 
     private final LDAPDirectory directory;
 
     private final DataSource dataSource;
 
     private final Sarariman sarariman;
 
     private final String mobile;
 
     private final Iterable<URL> profileLinks;

     private final Iterable<String> titles;
 
     StackFrameEmployee(String fullName, String userName, int number, boolean fulltime, boolean active, String email,
                        LocalDate birthdate, String displayName, Range<java.sql.Date> periodOfService, byte[] photo,
                        LDAPDirectory directory, DataSource dataSource, Sarariman sarariman, String mobile,
                       Iterable<URL> profileLinks, Iterable<String> titles) {
         this.directory = directory;
         this.fullName = fullName;
         this.userName = userName;
         this.number = number;
         this.fulltime = fulltime;
         this.active = active;
         this.email = email;
         this.birthdate = birthdate;
         this.displayName = displayName;
         this.periodOfService = periodOfService;
         this.photo = photo;
         this.dataSource = dataSource;
         this.sarariman = sarariman;
         this.mobile = mobile;
        this.profileLinks = profileLinks;
        this.titles = titles;
     }
 
     public String getFullName() {
         return fullName;
     }
 
     public String getUserName() {
         return userName;
     }
 
     public String getDisplayName() {
         return displayName;
     }
 
     public int getNumber() {
         return number;
     }
 
     public boolean isFulltime() {
         return fulltime;
     }
 
     public boolean isActive() {
         return active;
     }
 
     public InternetAddress getEmail() {
         try {
             return new InternetAddress(email, true);
         } catch (AddressException ae) {
             throw new RuntimeException("could not construct an email address", ae);
         }
     }
 
     public Iterable<Range<java.sql.Date>> getPeriodsOfService() {
         return Collections.singletonList(periodOfService);
     }
 
     public boolean isAdministrator() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT administrator FROM employee WHERE id = ?");
                 try {
                     s.setInt(1, number);
                     ResultSet rs = s.executeQuery();
                     try {
                         return rs.first() && rs.getBoolean("administrator");
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
 
     public void setAdministrator(boolean administrator) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("UPDATE employee SET administrator = ? WHERE id = ?");
                 try {
                     s.setBoolean(1, administrator);
                     s.setInt(2, number);
                     int rowCount = s.executeUpdate();
                     assert rowCount == 1;
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
 
     public Set<Project> getCurrentlyAssignedProjects() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement(
                         "SELECT DISTINCT(p.id) AS project " +
                         "FROM projects AS p " +
                         "JOIN tasks AS t ON t.project = p.id " +
                         "JOIN task_assignments AS ta ON ta.task = t.id " +
                         "WHERE ta.employee = ? AND " +
                         "p.active = TRUE ");
                 try {
                     s.setInt(1, number);
                     ResultSet rs = s.executeQuery();
                     try {
                         Set<Project> c = new HashSet<Project>();
                         while (rs.next()) {
                             int project_id = rs.getInt("project");
                             c.add(sarariman.getProjects().get(project_id));
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
 
     public Set<Project> getProjectsAdministrativelyAssisting() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement(
                         "SELECT project " +
                         "FROM project_administrative_assistants " +
                         "WHERE assistant = ?");
                 try {
                     s.setInt(1, number);
                     ResultSet rs = s.executeQuery();
                     try {
                         Set<Project> c = new HashSet<Project>();
                         while (rs.next()) {
                             int project_id = rs.getInt("project");
                             c.add(sarariman.getProjects().get(project_id));
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
 
     public Iterable<Project> getRelatedProjects() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 // FIXME: This triggers the slow query log on MySQL for no good reason that I can figure out.
                 PreparedStatement s = connection.prepareStatement(
                         "SELECT pm.project " +
                         "FROM project_managers AS pm " +
                         "JOIN projects AS p ON pm.project = p.id " +
                         "WHERE pm.employee = @employee AND " +
                         "p.active = TRUE " +
                         "UNION " +
                         "SELECT pm.project " +
                         "FROM project_cost_managers AS pm " +
                         "JOIN projects AS p ON pm.project = p.id " +
                         "WHERE pm.employee = @employee AND " +
                         "p.active = TRUE " +
                         "UNION " +
                         "SELECT DISTINCT(p.id) AS project " +
                         "FROM projects AS p " +
                         "JOIN tasks AS t ON t.project = p.id " +
                         "JOIN task_assignments AS ta ON ta.task=t.id " +
                         "WHERE ta.employee = @employee AND " +
                         "p.active = TRUE " +
                         "UNION " +
                         "SELECT project FROM project_administrative_assistants WHERE assistant = @employee");
                 try {
                     s.execute(String.format("SET @employee = %d", number));
                     ResultSet rs = s.executeQuery();
                     try {
                         Collection<Project> c = new ArrayList<Project>();
                         while (rs.next()) {
                             int project_id = rs.getInt("project");
                             c.add(sarariman.getProjects().get(project_id));
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
 
     public boolean isApprover() {
         return sarariman.getApprovers().contains(this);
     }
 
     public boolean isInvoiceManager() {
         return sarariman.getInvoiceManagers().contains(this);
     }
 
     public LocalDate getBirthdate() {
         return birthdate;
     }
 
     public int getAge() {
         return DateUtils.yearsBetween(birthdate.toDateMidnight(), new Date());
     }
 
     public PhoneNumber getMobile() {
         if (mobile == null) {
             return null;
         } else {
             try {
                 return PhoneNumberUtil.getInstance().parse(mobile, "US");
             } catch (NumberParseException npe) {
                 throw new IllegalArgumentException(npe);
             }
         }
     }
 
     public boolean active(java.sql.Date date) {
         return RangeUtilities.contains(getPeriodsOfService(), date);
     }
 
     public SortedSet<Employee> getReports() {
         Collection<Integer> reportIDs = sarariman.getOrganizationHierarchy().getReports(number);
         final Function<Integer, Employee> employeeIDToEmployee = new Function<Integer, Employee>() {
             public Employee apply(Integer f) {
                 return directory.getByNumber().get(f);
             }
 
         };
         Collection<Employee> reports = Collections2.transform(reportIDs, employeeIDToEmployee);
         Comparator<Employee> fullNameComparator = new Comparator<Employee>() {
             public int compare(Employee t, Employee t1) {
                 return t.getFullName().compareTo(t1.getFullName());
             }
 
         };
         return ImmutableSortedSet.copyOf(fullNameComparator, reports);
     }
 
     public BigDecimal getDirectRate() {
         try {
             Connection connection = sarariman.openConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT rate FROM direct_rate WHERE employee = ? ORDER BY effective DESC LIMIT 1");
                 try {
                     s.setInt(1, number);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         if (hasRow) {
                             return r.getBigDecimal("rate");
                         } else {
                             return null;
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
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public BigDecimal getDirectRate(java.sql.Date date) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT rate FROM direct_rate WHERE employee = ? AND effective <= ? ORDER BY effective DESC LIMIT 1");
                 try {
                     s.setInt(1, number);
                     s.setDate(2, date);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         if (hasRow) {
                             return r.getBigDecimal("rate");
                         } else {
                             return null;
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
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public Collection<Task> getDefaultTasks() {
         Collection<Task> tasks = new ArrayList<Task>();
         for (DefaultTaskAssignment a : sarariman.getDefaultTaskAssignments().getAll()) {
             if (fulltime || !a.isFullTimeOnly()) {
                 tasks.add(a.getTask());
             }
         }
 
         return tasks;
     }
 
     public Iterable<Task> getTasks() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement ps = connection.prepareStatement(
                         "SELECT t.id FROM tasks AS t " +
                         "JOIN task_assignments AS a ON a.task = t.id " +
                         "LEFT OUTER JOIN projects AS p ON t.project = p.id " +
                         "LEFT OUTER JOIN customers AS c ON c.id = p.customer " +
                         "WHERE employee = ? AND t.active = TRUE AND " +
                         "(p.active = TRUE OR p.active IS NULL) AND " +
                         "(c.active = TRUE OR c.active IS NULL) ORDER BY t.billable, t.id");
                 try {
                     ps.setInt(1, number);
                     ResultSet resultSet = ps.executeQuery();
                     try {
                         Collection<Task> list = new ArrayList<Task>();
                         while (resultSet.next()) {
                             int id = resultSet.getInt("id");
                             list.add(sarariman.getTasks().get(id));
                         }
 
                         list.addAll(getDefaultTasks());
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
 
     public Iterable<Task> getAssignedTasks() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement ps = connection.prepareStatement("SELECT task FROM task_assignments WHERE employee = ?");
                 try {
                     ps.setInt(1, number);
                     ResultSet resultSet = ps.executeQuery();
                     try {
                         Collection<Task> list = new ArrayList<Task>();
                         while (resultSet.next()) {
                             int id = resultSet.getInt("task");
                             list.add(sarariman.getTasks().get(id));
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
 
     @Override
     public String toString() {
         return "{" + fullName + "," + userName + "," + number + ",fulltime=" + fulltime + ",email=" + email + "}";
     }
 
     public BigDecimal getPaidTimeOff() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT SUM(amount) AS total " + "FROM paid_time_off " + "WHERE employee = ?");
                 try {
                     s.setInt(1, number);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         assert hasRow;
                         return r.getBigDecimal("total");
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public BigDecimal getRecentEntryLatency() {
         // FIXME: This needs to be parameterized and/or moved elsewhere
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement(
                         "SELECT AVG(DATEDIFF(hours_changelog.timestamp, hours.date)) AS average " +
                         "FROM hours " +
                         "JOIN hours_changelog ON hours.employee = hours_changelog.employee AND " +
                         "hours.task = hours_changelog.task AND hours.date = hours_changelog.date " +
                         "WHERE hours.employee = ? AND hours.date > DATE_SUB(NOW(), INTERVAL 7 DAY)");
                 try {
                     s.setInt(1, number);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         assert hasRow;
                         return r.getBigDecimal("average");
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public Iterable<Employee> getAdministrativeAssistants() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement ps = connection.prepareStatement("SELECT assistant " + "FROM individual_administrative_assistants " + "WHERE employee = ?");
                 ps.setInt(1, number);
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
 
     public URI getURI() {
         return URI.create(String.format("%sstaff/%s", sarariman.getMountPoint(), userName));
     }
 
     public URL getPhotoURL() {
         try {
             return new URL(String.format("%sphoto?uid=%s", sarariman.getMountPoint(), userName));
         } catch (MalformedURLException e) {
             throw new AssertionError(e);
         }
     }
 
     public Map<Week, Timesheet> getTimesheets() {
         ContiguousSet<Week> allWeeks = ContiguousSet.create(Range.<Week>all(), Week.discreteDomain);
         Function<Week, Timesheet> f = new Function<Week, Timesheet>() {
             public Timesheet apply(Week f) {
                 return sarariman.getTimesheets().get(StackFrameEmployee.this, f);
             }
 
         };
         return Maps.asMap(allWeeks, f);
     }
 
     public Map<Task, TaskAssignment> getTaskAssignments() {
         Function<Task, TaskAssignment> f = new Function<Task, TaskAssignment>() {
             public TaskAssignment apply(Task f) {
                 return sarariman.getTaskAssignments().get(StackFrameEmployee.this, f);
             }
 
         };
         return Maps.asMap(sarariman.getTasks().getAll(), f);
     }
 
     public Iterable<VacationEntry> getUpcomingVacation() {
         try {
             Connection c = dataSource.getConnection();
             try {
                 PreparedStatement s = c.prepareStatement("SELECT id FROM vacation WHERE employee=? AND (begin >= DATE(NOW()) OR end >= DATE(NOW()))");
                 try {
                     s.setInt(1, number);
                     ResultSet r = s.executeQuery();
                     try {
                         List<VacationEntry> l = new ArrayList<VacationEntry>();
                         while (r.next()) {
                             int entryID = r.getInt("id");
                             l.add(sarariman.getVacations().get(entryID));
                         }
                         return l;
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 c.close();
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public Iterable<OutOfOfficeEntry> getUpcomingOutOfOffice() {
         try {
             Connection c = dataSource.getConnection();
             try {
                 PreparedStatement s = c.prepareStatement("SELECT id FROM out_of_office WHERE employee=? AND end >= DATE(NOW())");
                 try {
                     s.setInt(1, number);
                     ResultSet r = s.executeQuery();
                     try {
                         List<OutOfOfficeEntry> l = new ArrayList<OutOfOfficeEntry>();
                         while (r.next()) {
                             int entryID = r.getInt("id");
                             l.add(sarariman.getOutOfOfficeEntries().get(entryID));
                         }
                         return l;
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 c.close();
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public Collection<Ticket> getUnclosedTickets() {
         try {
             Connection c = dataSource.getConnection();
             try {
                 PreparedStatement s = c.prepareStatement(
                         "SELECT updated.ticket, updated.latest, ticket_status.status " +
                         "FROM (SELECT assigned.ticket, MAX(ticket_status.updated) AS latest " +
                         "FROM (SELECT ticket, SUM(assignment) AS sum " +
                         "FROM ticket_assignment WHERE assignee = ? GROUP BY ticket) AS assigned " +
                         "JOIN ticket_status ON ticket_status.ticket = assigned.ticket " +
                         "WHERE assigned.sum > 0 GROUP BY ticket) AS updated " +
                         "JOIN ticket_status ON updated.ticket = ticket_status.ticket AND updated.latest = ticket_status.updated " +
                         "WHERE ticket_status.status != 'closed'");
                 try {
                     s.setInt(1, number);
                     ResultSet r = s.executeQuery();
                     try {
                         List<Ticket> l = new ArrayList<Ticket>();
                         while (r.next()) {
                             int ticketID = r.getInt("ticket");
                             l.add(sarariman.getTickets().get(ticketID));
                         }
 
                         return l;
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 c.close();
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public BigDecimal getMonthlyHealthInsurancePremium() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT p.premium AS premium FROM insurance_membership AS m JOIN insurance_premium AS p ON m.plan = p.plan AND m.coverage = p.coverage AND m.begin <= DATE(NOW()) and (m.end IS NULL OR m.end >= DATE(NOW())) AND employee=?");
                 try {
                     s.setInt(1, number);
                     ResultSet r = s.executeQuery();
                     try {
                         boolean hasRow = r.first();
                         if (!hasRow) {
                             return BigDecimal.ZERO;
                         } else {
                             return r.getBigDecimal("premium");
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
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public boolean isPayrollAdministrator() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT payroll_administrator FROM employee WHERE id = ?");
                 try {
                     s.setInt(1, number);
                     ResultSet rs = s.executeQuery();
                     try {
                         return rs.first() && rs.getBoolean("payroll_administrator");
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
 
     public void setPayrollAdministrator(boolean payrollAdministrator) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("UPDATE employee SET payroll_administrator = ? WHERE id = ?");
                 try {
                     s.setBoolean(1, payrollAdministrator);
                     s.setInt(2, number);
                     int rowCount = s.executeUpdate();
                     assert rowCount == 1;
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
 
     public boolean isBenefitsAdministrator() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("SELECT benefits_administrator FROM employee WHERE id = ?");
                 try {
                     s.setInt(1, number);
                     ResultSet rs = s.executeQuery();
                     try {
                         return rs.first() && rs.getBoolean("benefits_administrator");
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
 
     public void setBenefitsAdministrator(boolean benefitsAdministrator) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 PreparedStatement s = connection.prepareStatement("UPDATE employee SET benefits_administrator = ? WHERE id = ?");
                 try {
                     s.setBoolean(1, benefitsAdministrator);
                     s.setInt(2, number);
                     int rowCount = s.executeUpdate();
                     assert rowCount == 1;
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
 
     public byte[] getPhoto() {
         return photo;
     }
 
     public Iterable<URL> getProfileLinks() {
         return profileLinks;
     }
 
     public Iterable<String> getTitles() {
         return titles;
     }
 
     public Object getPresence() {
         return sarariman.getXMPPServer().getPresence(userName + "@stackframe.com");
     }
 
     public String getVcard() {
         StringBuilder buf = new StringBuilder();
 
         buf.append("BEGIN:VCARD\n");
         buf.append("VERSION:3.0\n");
 
         // FIXME: out.println("N:" + fields.get("sn") + ';' + fields.get("givenName") + ";;;");
 
         buf.append("FN:" + getFullName() + "\n");
         buf.append("ORG:StackFrame\\, LLC\n");
         // FIXME: buf.append("TITLE:" + fields.get("title"));
         buf.append("EMAIL;type=INTERNET;type=WORK:" + getEmail() + "\n");
         // FIXME: buf.append("TEL;type=WORK:" + fields.get("telephoneNumber") + "\n");
         // FIXME: buf.append("TEL;type=WORK;type=FAX:" + fields.get("facsimileTelephoneNumber") + "\n");
        String formattedMobile = PhoneNumberUtil.getInstance().format(getMobile(), PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
        buf.append("TEL;type=CELL:" + formattedMobile + "\n");
         // FIXME: buf.append("URL:http://www.stackframe.com/people/" + fields.get("uid") + '/'+"\n");
 
         buf.append("ADR;WORK;PARCEL;POSTAL;DOM:114 W. 1st St.\\, Suite 246;Sanford;FL;32771-1273\n");
 
         buf.append("END:VCARD\n");
 
         return buf.toString();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final StackFrameEmployee other = (StackFrameEmployee)obj;
         if (this.number != other.number) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 59 * hash + this.number;
         return hash;
     }
 
 }
