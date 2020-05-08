 package de.objectcode.time4u.client.connection.impl.ws;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.net.ssl.SSLSocketFactory;
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.Service;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 
 import de.objectcode.time4u.client.connection.ConnectionPlugin;
 import de.objectcode.time4u.client.connection.api.ConnectionException;
 import de.objectcode.time4u.client.connection.api.IConnection;
 import de.objectcode.time4u.client.connection.impl.common.ISynchronizationCommand;
 import de.objectcode.time4u.client.connection.impl.common.SynchronizationContext;
 import de.objectcode.time4u.client.connection.impl.common.down.ReceiveDayInfoChangesCommand;
 import de.objectcode.time4u.client.connection.impl.common.down.ReceiveDayTagsCommand;
 import de.objectcode.time4u.client.connection.impl.common.down.ReceivePersonChangesCommand;
 import de.objectcode.time4u.client.connection.impl.common.down.ReceiveProjectChangesCommand;
 import de.objectcode.time4u.client.connection.impl.common.down.ReceiveTaskChangesCommand;
 import de.objectcode.time4u.client.connection.impl.common.down.ReceiveTeamChangesCommand;
 import de.objectcode.time4u.client.connection.impl.common.down.ReceiveTimePolicyChangesCommand;
 import de.objectcode.time4u.client.connection.impl.common.down.ReceiveTodoChangesCommand;
 import de.objectcode.time4u.client.connection.impl.common.up.SendDayInfoChangesCommand;
 import de.objectcode.time4u.client.connection.impl.common.up.SendProjectChangesCommand;
 import de.objectcode.time4u.client.connection.impl.common.up.SendSynchronizationStatusCommand;
 import de.objectcode.time4u.client.connection.impl.common.up.SendTaskChangesCommand;
 import de.objectcode.time4u.client.connection.impl.common.up.SendTimePolicyChangesCommand;
 import de.objectcode.time4u.client.connection.impl.common.up.SendTodoChangesCommand;
 import de.objectcode.time4u.client.connection.util.NaiveTrustManager;
 import de.objectcode.time4u.client.store.api.RepositoryFactory;
 import de.objectcode.time4u.server.api.IConstants;
 import de.objectcode.time4u.server.api.ILoginService;
 import de.objectcode.time4u.server.api.IPersonService;
 import de.objectcode.time4u.server.api.IPingService;
 import de.objectcode.time4u.server.api.IProjectService;
 import de.objectcode.time4u.server.api.IRevisionService;
 import de.objectcode.time4u.server.api.ITaskService;
 import de.objectcode.time4u.server.api.ITeamService;
 import de.objectcode.time4u.server.api.ITodoService;
 import de.objectcode.time4u.server.api.IWorkItemService;
 import de.objectcode.time4u.server.api.data.Person;
 import de.objectcode.time4u.server.api.data.PingResult;
 import de.objectcode.time4u.server.api.data.RegistrationInfo;
 import de.objectcode.time4u.server.api.data.RevisionStatus;
 import de.objectcode.time4u.server.api.data.ServerConnection;
 import de.objectcode.time4u.server.utils.DefaultPasswordEncoder;
 import de.objectcode.time4u.server.utils.IPasswordEncoder;
 
 public class WSConnection implements IConnection
 {
   private final List<ISynchronizationCommand> m_synchronizationCommands;
 
   private final SSLSocketFactory m_sslSocketFactory;
 
   private final ServerConnection m_serverConnection;
   private final IPingService m_pingService;
   private final ILoginService m_loginService;
   private final IRevisionService m_revisionService;
   private final IPersonService m_personService;
   private final ITeamService m_teamService;
   private final IProjectService m_projectService;
   private final ITaskService m_taskService;
   private final ITodoService m_todoService;
   private final IWorkItemService m_workItemService;
 
   public WSConnection(final ServerConnection serverConnection) throws ConnectionException
   {
     m_sslSocketFactory = NaiveTrustManager.getSSLSocketFactory();
 
     // TODO: This may either be static or configurable
     m_synchronizationCommands = new ArrayList<ISynchronizationCommand>();
     m_synchronizationCommands.add(new SendProjectChangesCommand());
     m_synchronizationCommands.add(new SendTaskChangesCommand());
     m_synchronizationCommands.add(new SendDayInfoChangesCommand());
     m_synchronizationCommands.add(new SendTimePolicyChangesCommand());
     m_synchronizationCommands.add(new SendTodoChangesCommand());
     m_synchronizationCommands.add(new ReceivePersonChangesCommand());
     m_synchronizationCommands.add(new ReceiveTeamChangesCommand());
     m_synchronizationCommands.add(new ReceiveDayTagsCommand());
     m_synchronizationCommands.add(new ReceiveProjectChangesCommand());
     m_synchronizationCommands.add(new ReceiveTaskChangesCommand());
     m_synchronizationCommands.add(new ReceiveDayInfoChangesCommand());
     m_synchronizationCommands.add(new ReceiveTimePolicyChangesCommand());
     m_synchronizationCommands.add(new ReceiveTodoChangesCommand());
     m_synchronizationCommands.add(new SendSynchronizationStatusCommand());
 
     m_serverConnection = serverConnection;
 
     m_pingService = getServicePort("PingService", IPingService.class, false);
     m_loginService = getServicePort("LoginService", ILoginService.class, false);
     m_revisionService = getServicePort("RevisionService", IRevisionService.class, true);
     m_personService = getServicePort("PersonService", IPersonService.class, true);
     m_teamService = getServicePort("TeamService", ITeamService.class, true);
     m_projectService = getServicePort("ProjectService", IProjectService.class, true);
     m_taskService = getServicePort("TaskService", ITaskService.class, true);
     m_todoService = getServicePort("TodoService", ITodoService.class, true);
     m_workItemService = getServicePort("WorkItemService", IWorkItemService.class, true);
   }
 
   public boolean testConnection() throws ConnectionException
   {
     try {
       final PingResult pingResult = m_pingService.ping();
 
       return pingResult.getApiVersionMajor() == IConstants.API_VERSION_MAJOR;
     } catch (final Exception e) {
       ConnectionPlugin.getDefault().log(e);
       throw new ConnectionException(e.toString(), e);
     }
   }
 
   public boolean checkLogin(final Map<String, String> credentials) throws ConnectionException
   {
     try {
       return m_loginService.checkLogin(credentials.get("userId"));
     } catch (final Exception e) {
       ConnectionPlugin.getDefault().log(e);
       throw new ConnectionException(e.toString(), e);
     }
   }
 
   public Person getPerson() throws ConnectionException
   {
     return m_personService.getSelf();
   }
 
   public RevisionStatus getRevisionStatus() throws ConnectionException
   {
     try {
       return m_revisionService.getRevisionStatus();
     } catch (final Exception e) {
       ConnectionPlugin.getDefault().log(e);
       throw new ConnectionException(e.toString(), e);
     }
   }
 
   public boolean registerLogin(final Map<String, String> credentials) throws ConnectionException
   {
     try {
       final IPasswordEncoder encoder = new DefaultPasswordEncoder();
 
       final Person owner = RepositoryFactory.getRepository().getOwner();
       final RegistrationInfo registrationInfo = new RegistrationInfo();
 
       registrationInfo.setClientId(RepositoryFactory.getRepository().getClientId());
       registrationInfo.setPersonId(owner.getId());
       registrationInfo.setGivenName(owner.getGivenName());
       registrationInfo.setSurname(owner.getSurname());
       registrationInfo.setEmail(owner.getEmail());
       registrationInfo.setUserId(credentials.get("userId"));
       registrationInfo.setHashedPassword(encoder.encrypt(credentials.get("password").toCharArray()));
 
       return m_loginService.registerLogin(registrationInfo);
     } catch (final Exception e) {
       ConnectionPlugin.getDefault().log(e);
       throw new ConnectionException(e.toString(), e);
     }
   }
 
   public boolean registerClient() throws ConnectionException
   {
     return m_personService.registerClient(RepositoryFactory.getRepository().getClientId());
   }
 
   private <T> T getServicePort(final String serviceName, final Class<T> portInterface, final boolean secure)
       throws ConnectionException
   {
     if (secure && m_serverConnection.getCredentials() == null) {
       return null;
     }
 
     final URL wsdl = getClass().getResource(serviceName + ".wsdl");
     final Service service = Service.create(wsdl, new QName("http://objectcode.de/time4u/api/ws", serviceName
         + "WSService"));
 
     final T port = service.getPort(portInterface);
 
     final BindingProvider bp = (BindingProvider) port;
     bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
         m_serverConnection.getUrl() + "/time4u-ws" + (secure ? "/secure/" : "/") + serviceName);
     if (secure) {
       final Map<String, String> credentials = m_serverConnection.getCredentials();
       bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, credentials.get("userId"));
       bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, credentials.get("password"));
     }
     bp.getRequestContext().put("com.sun.xml.ws.transport.https.client.SSLSocketFactory", m_sslSocketFactory);
 
     return port;
   }
 
   public void sychronizeNow(final IProgressMonitor monitor) throws ConnectionException
   {
     PingResult pingResult;
     try {
       pingResult = m_pingService.ping();
     } catch (final Exception e) {
       ConnectionPlugin.getDefault().log(e);
       throw new ConnectionException(e.toString(), e);
     }
 
     if (pingResult.getApiVersionMajor() != IConstants.API_VERSION_MAJOR) {
       throw new ConnectionException("API Version differs");
     }
 
     monitor.beginTask("Synchronize", m_synchronizationCommands.size());
 
     try {
       final SynchronizationContext context = new SynchronizationContext(RepositoryFactory.getRepository(),
           m_serverConnection, m_revisionService, m_projectService, m_taskService, m_workItemService, m_personService,
           m_teamService, m_todoService, pingResult.getApiVersionMinor());
 
       for (final ISynchronizationCommand command : m_synchronizationCommands) {
         if (monitor.isCanceled()) {
           break;
         }
         if (command.shouldRun(context)) {
           command.execute(context, new SubProgressMonitor(monitor, 1));
         } else {
           monitor.worked(1);
         }
       }
     } catch (final Exception e) {
       ConnectionPlugin.getDefault().log(e);
       throw new ConnectionException(e.toString(), e);
     } finally {
       monitor.done();
     }
   }
 }
