 /*
  * Copyright (C) 2009-2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import static com.google.common.base.Preconditions.*;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ImmutableList;
 import com.google.i18n.phonenumbers.NumberParseException;
 import com.google.i18n.phonenumbers.PhoneNumberUtil;
 import com.stackframe.reflect.ReflectionUtils;
 import com.stackframe.sarariman.accesslog.AccessLog;
 import com.stackframe.sarariman.accesslog.AccessLogImpl;
 import com.stackframe.sarariman.clients.Clients;
 import com.stackframe.sarariman.clients.ClientsImpl;
 import com.stackframe.sarariman.contacts.Contacts;
 import com.stackframe.sarariman.contacts.ContactsImpl;
 import com.stackframe.sarariman.errors.Errors;
 import com.stackframe.sarariman.errors.ErrorsImpl;
 import com.stackframe.sarariman.events.Events;
 import com.stackframe.sarariman.events.EventsImpl;
 import com.stackframe.sarariman.holidays.Holidays;
 import com.stackframe.sarariman.holidays.HolidaysImpl;
 import com.stackframe.sarariman.locationlog.LocationLog;
 import com.stackframe.sarariman.locationlog.LocationLogImpl;
 import com.stackframe.sarariman.logincookies.LoginCookies;
 import com.stackframe.sarariman.outofoffice.OutOfOfficeEntries;
 import com.stackframe.sarariman.outofoffice.OutOfOfficeEntriesImpl;
 import com.stackframe.sarariman.projects.LaborProjections;
 import com.stackframe.sarariman.projects.LaborProjectionsImpl;
 import com.stackframe.sarariman.projects.Projects;
 import com.stackframe.sarariman.projects.ProjectsImpl;
 import com.stackframe.sarariman.taskassignments.DefaultTaskAssignments;
 import com.stackframe.sarariman.taskassignments.DefaultTaskAssignmentsImpl;
 import com.stackframe.sarariman.taskassignments.TaskAssignments;
 import com.stackframe.sarariman.taskassignments.TaskAssignmentsImpl;
 import com.stackframe.sarariman.tasks.Tasks;
 import com.stackframe.sarariman.tasks.TasksImpl;
 import com.stackframe.sarariman.telephony.SMSGateway;
 import com.stackframe.sarariman.telephony.twilio.TwilioSMSGatewayImpl;
 import com.stackframe.sarariman.tickets.Tickets;
 import com.stackframe.sarariman.tickets.TicketsImpl;
 import com.stackframe.sarariman.timesheets.Timesheets;
 import com.stackframe.sarariman.timesheets.TimesheetsImpl;
 import com.stackframe.sarariman.vacation.Vacations;
 import com.stackframe.sarariman.vacation.VacationsImpl;
 import com.stackframe.sarariman.xmpp.XMPPServer;
 import com.stackframe.sarariman.xmpp.XMPPServerImpl;
 import com.twilio.sdk.TwilioRestClient;
 import java.io.File;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Timer;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.naming.directory.InitialDirContext;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.sql.DataSource;
 
 /**
  *
  * @author mcculley
  */
 public class Sarariman implements ServletContextListener {
 
     // This ExecutorService is used for background jobs which do not require synchronous completion with regard to an HTTP request.
     private final ExecutorService backgroundExecutor = Executors.newFixedThreadPool(1);
 
     // This ExecutorService is used for background jobs which write to the database and do not require synchronous completion with
     // regard to an HTTP request.
     private final ExecutorService backgroundDatabaseWriteExecutor = Executors.newFixedThreadPool(1);
 
     private final Collection<Employee> approvers = new EmployeeTable(this, "approvers");
 
     private final Collection<Employee> invoiceManagers = new EmployeeTable(this, "invoice_managers");
 
     private final Collection<Employee> timesheetManagers = new EmployeeTable(this, "timesheet_managers");
 
     private final Collection<LaborCategoryAssignment> projectBillRates = new LaborCategoryAssignmentTable(this);
 
     private final Collection<LaborCategory> laborCategories = new LaborCategoryTable(this);
 
     private final Collection<Extension> extensions = new ArrayList<Extension>();
 
     private final Holidays holidays = new HolidaysImpl(getDataSource());
 
     private final DirectorySynchronizer directorySynchronizer = new DirectorySynchronizerImpl();
 
     private OrganizationHierarchy organizationHierarchy;
 
     private LDAPDirectory directory;
 
     private EmailDispatcher emailDispatcher;
 
     private CronJobs cronJobs;
 
     private String logoURL;
 
     private String mountPoint;
 
     private TimesheetEntries timesheetEntries;
 
     private Projects projects;
 
     private Tasks tasks;
 
     private Clients clients;
 
     private Tickets tickets;
 
     private Events events;
 
     private Vacations vacations;
 
     private OutOfOfficeEntries outOfOffice;
 
     private Contacts contacts;
 
     private Timesheets timesheets;
 
     private Errors errors;
 
     private AccessLog accessLog;
 
     private Workdays workdays;
 
     private PaidTimeOff paidTimeOff;
 
     private LaborProjections laborProjections;
 
     private TaskAssignments taskAssignments;
 
     private DefaultTaskAssignments defaultTaskAssignments;
 
     private LoginCookies loginCookies;
 
     private LocationLog locationLog;
 
     private final Timer timer = new Timer("Sarariman");
 
     private SMSGateway SMS;
 
     private XMPPServer xmpp;
 
     public String getVersion() {
         return Version.version;
     }
 
     private static Properties lookupDirectoryProperties(Context envContext) throws NamingException {
         Properties props = new Properties();
         String[] propNames = new String[]{Context.INITIAL_CONTEXT_FACTORY, Context.PROVIDER_URL, Context.SECURITY_AUTHENTICATION,
             Context.SECURITY_PRINCIPAL, Context.SECURITY_CREDENTIALS};
 
         for (String s : propNames) {
             props.put(s, envContext.lookup(s));
         }
 
         return props;
     }
 
     private static Properties lookupMailProperties(Context envContext) throws NamingException {
         Properties props = new Properties();
         String[] propNames = new String[]{"mail.from", "mail.smtp.host", "mail.smtp.port"};
 
         for (String s : propNames) {
             props.put(s, envContext.lookup(s));
         }
 
         return props;
     }
 
     public DataSource getDataSource() {
         try {
             return (DataSource)new InitialContext().lookup("java:comp/env/jdbc/sarariman");
         } catch (NamingException namingException) {
             throw new RuntimeException(namingException);
         }
     }
 
     public Connection openConnection() {
         try {
             return getDataSource().getConnection();
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public Directory getDirectory() {
         return directory;
     }
 
     public EmailDispatcher getEmailDispatcher() {
         return emailDispatcher;
     }
 
     public Collection<Employee> getApprovers() {
         return approvers;
     }
 
     public Collection<Employee> getInvoiceManagers() {
         return invoiceManagers;
     }
 
     public Collection<Employee> getTimesheetManagers() {
         return timesheetManagers;
     }
 
     public Timesheets getTimesheets() {
         return timesheets;
     }
 
     public Clients getClients() {
         return clients;
     }
 
     private Collection<Employee> getAdministrators() {
         Predicate<Employee> isAdministrator = ReflectionUtils.predicateForProperty(Employee.class, "administrator");
         return Collections2.filter(directory.getByUserName().values(), isAdministrator);
     }
 
     public String getLogoURL() {
         return logoURL;
     }
 
     public String getMountPoint() {
         return mountPoint;
     }
 
     public Collection<LaborCategoryAssignment> getProjectBillRates() {
         return projectBillRates;
     }
 
     public Map<Long, LaborCategory> getLaborCategories() {
         Map<Long, LaborCategory> result = new LinkedHashMap<Long, LaborCategory>();
         for (LaborCategory lc : laborCategories) {
             result.put(lc.getId(), lc);
         }
 
         return result;
     }
 
     public Collection<Extension> getExtensions() {
         return extensions;
     }
 
     public AccessLog getAccessLog() {
         return accessLog;
     }
 
     public OrganizationHierarchy getOrganizationHierarchy() {
         return organizationHierarchy;
     }
 
     public TaskAssignments getTaskAssignments() {
         return taskAssignments;
     }
 
     public Collection<Employee> employees(Collection<Integer> ids) {
         Collection<Employee> result = new ArrayList<Employee>();
         for (int id : ids) {
             result.add(directory.getByNumber().get(id));
         }
 
         return result;
     }
 
     public Holidays getHolidays() {
         return holidays;
     }
 
     public Vacations getVacations() {
         return vacations;
     }
 
     public LaborProjections getLaborProjections() {
         return laborProjections;
     }
 
     public DefaultTaskAssignments getDefaultTaskAssignments() {
         return defaultTaskAssignments;
     }
 
     public LoginCookies getLoginCookies() {
         return loginCookies;
     }
 
     public LocationLog getLocationLog() {
         return locationLog;
     }
 
     public Collection<Audit> getGlobalAudits() {
         Collection<Audit> c = new ArrayList<Audit>();
         c.add(new OrgChartGlobalAudit(this));
         c.add(new TimesheetAudit(this, directory));
         c.add(new ContactsGlobalAudit(getDataSource(), contacts));
         c.add(new ProjectAdministrativeAssistantGlobalAudit(getDataSource(), projects));
         c.add(new ProjectManagerGlobalAudit(getDataSource(), projects));
         c.add(new ProjectCostManagerGlobalAudit(getDataSource(), projects));
         c.add(new DirectRateAudit(directory));
         c.add(new TaskAssignmentsGlobalAudit(getDataSource(), directory, tasks, taskAssignments));
         return c;
     }
 
     public static boolean isBoss(OrganizationHierarchy organizationHierarchy, Employee employee) throws SQLException {
         Collection<Integer> bossIDs = organizationHierarchy.getBosses();
         return bossIDs.contains(employee.getNumber());
     }
 
     public boolean isBoss(Employee employee) throws SQLException {
         return isBoss(organizationHierarchy, employee);
     }
 
     public static boolean isBoss(Sarariman sarariman, Employee employee) throws SQLException {
         return sarariman.isBoss(employee);
     }
 
     /**
      * Make contains visible to the tag library.
      *
      * @param coll
      * @param o
      * @return whether or not coll contains o
      */
     public static boolean contains(Collection<?> coll, Object o) {
         checkNotNull(coll);
         return coll.contains(o);
     }
 
     DirectorySynchronizer getDirectorySynchronizer() {
         return directorySynchronizer;
     }
 
     public Timer getTimer() {
         return timer;
     }
 
     public TimesheetEntries getTimesheetEntries() {
         return timesheetEntries;
     }
 
     public Projects getProjects() {
         return projects;
     }
 
     public Tasks getTasks() {
         return tasks;
     }
 
     public Tickets getTickets() {
         return tickets;
     }
 
     public Events getEvents() {
         return events;
     }
 
     public Errors getErrors() {
         return errors;
     }
 
     public OutOfOfficeEntries getOutOfOfficeEntries() {
         return outOfOffice;
     }
 
     public Contacts getContacts() {
         return contacts;
     }
 
     public Workdays getWorkdays() {
         return workdays;
     }
 
     public PaidTimeOff getPaidTimeOff() {
         return paidTimeOff;
     }
 
     public Executor getBackgroundDatabaseWriteExecutor() {
         return backgroundDatabaseWriteExecutor;
     }
 
     public SMSGateway getSMSGateway() {
         return SMS;
     }
 
     public Collection<UIResource> getNavbarLinks() {
         return ImmutableList.<UIResource>of(new UIResourceImpl(getMountPoint(), "Home", "icon-home"),
                                             new UIResourceImpl(getMountPoint() + "tools", "Tools", "icon-wrench"),
                                             new UIResourceImpl(getMountPoint() + "holidays/upcoming.jsp", "Holidays",
                                                                "icon-calendar"));
     }
 
     public void contextInitialized(ServletContextEvent sce) {
         extensions.add(new SAICExtension());
         try {
             Context initContext = new InitialContext();
             Context envContext = (Context)initContext.lookup("java:comp/env");
             Properties directoryProperties = lookupDirectoryProperties(envContext);
             directory = new LDAPDirectory(new InitialDirContext(directoryProperties), this);
             try {
                 directorySynchronizer.synchronize(directory, getDataSource());
             } catch (Exception e) {
                 // FIXME: log
                 System.err.println("Trouble synchronizing directory with database: " + e);
             }
 
             initContext.rebind("sarariman.directory", directory);
             organizationHierarchy = new OrganizationHierarchyImpl(getDataSource(), directory);
             boolean inhibitEmail = (Boolean)envContext.lookup("inhibitEmail");
             String twilioAccountSID = (String)envContext.lookup("twilioAccountSID");
             String twilioAuthToken = (String)envContext.lookup("twilioAuthToken");
             TwilioRestClient twilioClient = new TwilioRestClient(twilioAccountSID, twilioAuthToken);
             boolean inhibitSMS = (Boolean)envContext.lookup("inhibitSMS");
             String SMSFrom = (String)envContext.lookup("SMSFrom");
             try {
                 SMS = new TwilioSMSGatewayImpl(twilioClient, PhoneNumberUtil.getInstance().parse(SMSFrom, "US"), inhibitSMS,
                                                backgroundDatabaseWriteExecutor, getDataSource());
             } catch (NumberParseException pe) {
                 throw new RuntimeException(pe);
             }
 
             emailDispatcher = new EmailDispatcher(lookupMailProperties(envContext), inhibitEmail, backgroundExecutor);
             logoURL = (String)envContext.lookup("logoURL");
             mountPoint = (String)envContext.lookup("mountPoint");
             clients = new ClientsImpl(getDataSource(), mountPoint);
             projects = new ProjectsImpl(getDataSource(), organizationHierarchy, directory, this);
             tasks = new TasksImpl(getDataSource(), getMountPoint(), projects);
             timesheetEntries = new TimesheetEntriesImpl(getDataSource(), directory, tasks, mountPoint);
             tickets = new TicketsImpl(getDataSource(), mountPoint);
             events = new EventsImpl(getDataSource(), mountPoint);
             vacations = new VacationsImpl(getDataSource(), directory);
             outOfOffice = new OutOfOfficeEntriesImpl(getDataSource(), directory);
             contacts = new ContactsImpl(getDataSource(), mountPoint);
             timesheets = new TimesheetsImpl(this, mountPoint);
             errors = new ErrorsImpl(getDataSource(), mountPoint, directory);
             accessLog = new AccessLogImpl(getDataSource(), directory);
             workdays = new WorkdaysImpl(holidays);
             paidTimeOff = new PaidTimeOff(tasks);
             laborProjections = new LaborProjectionsImpl(getDataSource(), directory, tasks, mountPoint);
             taskAssignments = new TaskAssignmentsImpl(directory, getDataSource(), mountPoint);
             defaultTaskAssignments = new DefaultTaskAssignmentsImpl(getDataSource(), tasks);
             loginCookies = new LoginCookies(getDataSource(), timer);
             locationLog = new LocationLogImpl(getDataSource(), directory, backgroundDatabaseWriteExecutor);
             String keyStorePath = (String)envContext.lookup("keyStorePath");
             String keyStorePassword = (String)envContext.lookup("keyStorePassword");
             try {
                 System.err.println("starting XMPP server");
                 xmpp = new XMPPServerImpl(directory, new File(keyStorePath), keyStorePassword);
             } catch (Exception e) {
                 System.err.println("trouble starting XMPP server");
                 e.printStackTrace();
             }
         } catch (NamingException ne) {
             throw new RuntimeException(ne);  // FIXME: Is this the best thing to throw here?
         }
 
         cronJobs = new CronJobs(timer, this, directory, emailDispatcher);
 
         ServletContext servletContext = sce.getServletContext();
         servletContext.setAttribute("sarariman", this);
         servletContext.setAttribute("directory", directory);
 
         cronJobs.start();
         final String hostname = getHostname();
 
         Runnable sendStartupEmailNotification = new Runnable() {
             public void run() {
                 try {
                     for (Employee employee : getAdministrators()) {
                         String message = String.format("Sarariman version %s has been started on %s at %s.", getVersion(), hostname,
                                                        mountPoint);
                         emailDispatcher.send(employee.getEmail(), null, "sarariman started", message);
                         SMS.send(employee.getMobile(), "Sarariman has been started.");
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                     // FIXME: log
                 }
             }
 
         };
         backgroundExecutor.execute(sendStartupEmailNotification);
     }
 
     private static String getHostname() {
         try {
             return InetAddress.getLocalHost().getHostName();
         } catch (UnknownHostException uhe) {
             return "unknown host";
         }
     }
 
     public void contextDestroyed(ServletContextEvent sce) {
         // FIXME: Should we worry about email that has been queued but not yet sent?
         timer.cancel();
         backgroundExecutor.shutdown();
         try {
             xmpp.stop();
         } catch (Exception e) {
             System.err.println("trouble stopping XMPP server");
             e.printStackTrace();
         }
     }
 
 }
