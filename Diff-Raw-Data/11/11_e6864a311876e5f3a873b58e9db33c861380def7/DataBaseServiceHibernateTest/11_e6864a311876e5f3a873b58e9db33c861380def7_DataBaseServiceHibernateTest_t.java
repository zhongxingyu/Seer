 package ru.mail.projects.account.database.impl_test;
 
 import java.util.Queue;
 
 import junit.framework.Assert;
 
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import ru.mail.projects.account.database.impl.DatabaseServiceCap;
 import ru.mail.projects.account.database.impl.DatabaseServiceHibernate;
 import ru.mail.projects.base.Address;
 import ru.mail.projects.base.MessageSystem;
 import ru.mail.projects.base.Msg;
 import ru.mail.projects.base.ResourceSystem;
 import ru.mail.projects.base.VFS;
 import ru.mail.projects.message.system.impl.MessageSystemImpl;
 import ru.mail.projects.messages.MsgUpdateUserId;
 import ru.mail.projects.resource.system.impl.ResourceSystemImpl;
 import ru.mail.projects.utils.Context;
 import ru.mail.projects.utils.LongId;
 import ru.mail.projects.utils.SessionId;
 import ru.mail.projects.vfs.impl.VFSImpl;
 
 public class DataBaseServiceHibernateTest {
 	
 	Context context;
 	MessageSystem MsgSystem;
 	
 	@BeforeClass
 	public void InitMsgSystem() {
 		
 		context = new Context();
 		VFS vfs = new VFSImpl("./resources");
     	
 		MsgSystem = new MessageSystemImpl();
 		context.add(MessageSystem.class, MsgSystem);
 		context.add(VFS.class, vfs);
     	ResourceSystem rsSystem = new ResourceSystemImpl(vfs);
     	rsSystem.loadResources();
     	context.add(ResourceSystem.class, rsSystem);
 	}
 	
 	@Test
 	public void CreateServiceHibernateTest() {
 		
 		DatabaseServiceHibernate.count = 0;
 		DatabaseServiceHibernate dbhib1 = new DatabaseServiceHibernate(context); 
 		DatabaseServiceHibernate dbhib2 = new DatabaseServiceHibernate(context);
 				
		Assert.assertNotSame(dbhib1.getId(), 4);
		Assert.assertNotSame(dbhib2.getId(), 5);
		Assert.assertNotSame(DatabaseServiceHibernate.count, 4);
		Assert.assertNotSame(dbhib1.getName(), "DatabaseService10");
		Assert.assertNotSame(dbhib2.getName(), "DatabaseService11");
 		
 		dbhib1.setName("AnotherService");
 		Assert.assertEquals(dbhib1.getName(), "AnotherService");
 	}
 	
 	@Test(dependsOnMethods = { "CreateServiceHibernateTest" })
 	public void FindUserTest() {
 		
 		DatabaseServiceHibernate dbhib1 = new DatabaseServiceHibernate(context);
 		MsgSystem.addService(dbhib1);
 		
 		Address addrTo = dbhib1.getAddress();
 		
 		LongId<SessionId> sId = new LongId<SessionId>(10);
 		String name1 = new String("Roma");
 		String name2 = new String("10101");
 		
 		dbhib1.FindUser(sId, name1, addrTo);
 		dbhib1.FindUser(sId, name2, addrTo);
 		
 		Queue<Msg> qMsg = MsgSystem.getQueueMsg(addrTo);
 		Msg msgUpd1 = qMsg.poll();
 		
 		Assert.assertTrue(msgUpd1 instanceof MsgUpdateUserId); 
 		MsgUpdateUserId msgUpdUserId1 = (MsgUpdateUserId)msgUpd1;
 		
 		Assert.assertEquals(addrTo, msgUpdUserId1.getTo());
 		Assert.assertEquals(sId, msgUpdUserId1.getSession());
 	}
 	
 	@Test(dependsOnMethods = { "CreateServiceHibernateTest" })
 	public void AddUserTest() {
 		
 		DatabaseServiceHibernate dbcap1 = new DatabaseServiceHibernate(context);
 		MsgSystem.addService(dbcap1);
 		
 		Address addrTo = dbcap1.getAddress();
 		
 		LongId<SessionId> sId = new LongId<SessionId>(10);
 		String name1 = new String("Roma");
 		String name2 = new String("10101");
 		
 		dbcap1.AddUser(sId, name1, addrTo);
 		dbcap1.AddUser(sId, name2, addrTo);
 		
 		Queue<Msg> qMsg = MsgSystem.getQueueMsg(addrTo);
 		Msg msgUpd1 = qMsg.poll();
 		
 		Assert.assertTrue(msgUpd1 instanceof MsgUpdateUserId); 
 		MsgUpdateUserId msgUpdUserId1 = (MsgUpdateUserId)msgUpd1;
 		
 		Assert.assertEquals(addrTo, msgUpdUserId1.getTo());
 		Assert.assertEquals(sId, msgUpdUserId1.getSession());
 		Assert.assertEquals(null, msgUpdUserId1.getUserId());
 		
 		Msg msgUpd2 = qMsg.poll();
 		
 		Assert.assertTrue(msgUpd2 instanceof MsgUpdateUserId); 
 		MsgUpdateUserId msgUpdUserId2 = (MsgUpdateUserId)msgUpd2;
 		
 		Assert.assertEquals(addrTo, msgUpdUserId2.getTo());
 		Assert.assertEquals(sId, msgUpdUserId2.getSession());
 	}
 }
