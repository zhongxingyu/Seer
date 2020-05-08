 package com.trabajo.ejb;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import javax.annotation.Resource;
 import javax.ejb.EJB;
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.sql.DataSource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.trabajo.ValidationException;
 import com.trabajo.engine.EngineFactory;
 import com.trabajo.engine.SysConfig;
 import com.trabajo.engine.TSession;
 import com.trabajo.engine.bobj.Groups;
 import com.trabajo.engine.bobj.Roles;
 import com.trabajo.engine.bobj.Users;
 import com.trabajo.process.IGroup;
 import com.trabajo.process.IRole;
 import com.trabajo.process.IUser;
 import com.trabajo.values.CreateGroupSpec;
 import com.trabajo.values.CreateRoleSpec;
 import com.trabajo.values.CreateUserSpec;
 
 /**
  * Session Bean implementation class EngineBean
  */
 @Stateless
 @LocalBean
 public class SystemInitBean {
 
 	private final static Logger logger = LoggerFactory.getLogger(SystemInitBean.class);
 
 	@Resource(mappedName = "java:/trabajo")	DataSource dataSource;
 	@PersistenceContext(name = "trabajo")	private EntityManager em;
 
 	private static final String obliterateSQL = "delete from task_roles;\r\n" + "delete from taskupload;\r\n" + "delete from taskbarrier;\r\n"
 			+ "delete from nodetimer;\r\n" + "delete from node;\r\n" + "\r\n" + "delete from qrtz_locks;\r\n" + "delete from qrtz_fired_triggers;\r\n"
 			+ "delete from qrtz_job_details;\r\n" + "delete from qrtz_calendars;\r\n" + "delete from qrtz_blob_triggers;\r\n" + "delete from qrtz_cron_triggers;\r\n"
 			+ "delete from qrtz_paused_trigger_grps;\r\n" + "delete from qrtz_scheduler_state;\r\n" + "delete from qrtz_simple_triggers;\r\n"
 			+ "delete from qrtz_simprop_triggers;\r\n" + "delete from qrtz_triggers;\r\n" + "\r\n" + "delete from instance;\r\n" + "delete from procdef;\r\n"
 			+ "\r\n" + "delete from servicedef;\r\n" + "delete from classloaderdef;\r\n";
 
 	@EJB
 	EngineBean engine;
 
 	public SystemInitBean() {
 	}
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRED)
 	public void init() throws ValidationException {
 
 		SysConfig sc=engine.getConfig();
 		EngineFactory.init(sc);
 		
 		IRole dflt = Roles.findByName(em, "default");
 
 		if (dflt == null) {
 			CreateRoleSpec spec = new CreateRoleSpec("cog.builtin", "default", "default role");
 			Roles.create(em, spec);
 		}
 
 		IRole adminRole = Roles.findByName(em, "admin");
 
 		if (adminRole == null) {
 			CreateRoleSpec spec = new CreateRoleSpec("cog.builtin", "admin", "admin role");
 			adminRole = Roles.create(em, spec);
 		}
 
 		IUser admin = Users.findByName(em, "admin");
 		if (admin == null) {
 			CreateUserSpec spec = new CreateUserSpec("", "Cog Administrator", "admin", "password", "admin@myorg.com", "EN", "EN");
 			admin = Users.createUser(em, spec);
 			admin.addRole(adminRole);
 		}
 
 		IGroup dfltGrp = Groups.findByName(em, "admin");
 
 		if (dfltGrp == null) {
 			dfltGrp = Groups.create(em, new CreateGroupSpec("cog.builtin", "admin", "Admin group"));
 		}
 
 		if (!dfltGrp.getRoles().contains(adminRole)) {
 			dfltGrp.add(adminRole);
 		}
 		if (!dfltGrp.getMembers().contains(admin)) {
 			dfltGrp.add(admin);
 		}
 		TSession ts = new TSession("system");
 		ts.newRequest();
 
 		boolean purgeMySelf = sc.isObliterateOnRestart();
 		if (purgeMySelf) {
 			sc.setObliterateOnRestart(false);
 			engine.updateConfig(sc);
 			
 			try(Connection conn=dataSource.getConnection(); PreparedStatement ps=conn.prepareStatement(obliterateSQL)) {
 				ps.executeUpdate();
 			} catch (SQLException e) {
 				logger.error("obliteration failed", e);
 			}
 		}
 	}
 }
