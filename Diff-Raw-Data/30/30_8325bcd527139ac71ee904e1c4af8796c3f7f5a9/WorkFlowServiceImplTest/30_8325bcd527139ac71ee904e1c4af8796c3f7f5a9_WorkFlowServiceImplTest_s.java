 package gov.abrs.etms.service.workflow;
 
 import static org.junit.Assert.*;
 import gov.abrs.etms.model.baseinfo.Dept;
 import gov.abrs.etms.model.baseinfo.Person;
 import gov.abrs.etms.model.sys.FunModule;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.jbpm.graph.exe.Comment;
 import org.jbpm.graph.exe.ProcessInstance;
 import org.jbpm.taskmgmt.exe.TaskInstance;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 
@ContextConfiguration(locations = { "/conf/spring/applicationContext-test.xml" })
 public class WorkFlowServiceImplTest extends JbpmTestCase {
 	@Autowired
 	private WorkFlowService workFlowService;
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testCreatProcessInstanceStringStringString() {
 		//开始一个没有权限控制的流程
 		Person person = (Person) this.sessionFactory.getCurrentSession().get(Person.class, 1L);
 		workFlowService.startProcessInstance("accidentReport", "accd-1", "descdesc", person, "注释");
 		this.flushSessionAndCloseSessionAndNewASession();
 		//验证流程保存成功
 		ProcessInstance pi = workFlowService.getProcessInstance("accidentReport", "accd-1");
 		assertNotNull(pi);
 		assertEquals(pi.getKey(), "accd-1");
 		assertEquals(pi.getContextInstance().getVariable("taskDescription"), "descdesc");
 		//验证任务处理正确
 		Collection<TaskInstance> taskInstances = pi.getTaskMgmtInstance().getTaskInstances();
 		assertEquals(taskInstances.size(), 2);
 		Iterator<TaskInstance> iterator = taskInstances.iterator();
 		while (iterator.hasNext()) {
 			TaskInstance ti = iterator.next();
 			if (ti.getActorId().equals("ROLE_WATCH")) {
 				assertTrue(ti.isStartTaskInstance());
 				assertEquals(ti.getName(), "填写异态");
 				assertNotNull(ti.getStart());
 				assertNotNull(ti.getEnd());
 				assertEquals(ti.getComments().size(), 1);
 				assertEquals(((Comment) ti.getComments().get(0)).getActorId(), "王健");
 				assertEquals(((Comment) ti.getComments().get(0)).getMessage(), "注释");
 			} else if (ti.getActorId().equals("ROLE_DIRECTOR")) {
 				assertFalse(ti.isStartTaskInstance());
 				assertEquals(ti.getName(), "机房主任审核");
 				assertNull(ti.getStart());
 				assertNull(ti.getEnd());
 			} else {
 				fail();
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testCreatProcessInstanceStringStringStringSuffix() {
 		//开始一个有权限控制的流程
 		Person person = (Person) this.sessionFactory.getCurrentSession().get(Person.class, 1L);
 		workFlowService.startProcessInstance("accidentReport", "accd-2", "事故上报22", person, "注释", new Dept(2L),
 				FunModule.ACCD);
 		this.flushSessionAndCloseSessionAndNewASession();
 		//验证流程保存成功
 		ProcessInstance pi = workFlowService.getProcessInstance("accidentReport", "accd-2");
 		assertNotNull(pi);
 		assertEquals(pi.getKey(), "accd-2");
 		assertEquals(pi.getContextInstance().getVariable("taskDescription"), "事故上报22");
 		assertEquals(pi.getContextInstance().getVariable("dpId"), "2");
 		assertEquals(pi.getContextInstance().getVariable("funModuleKey"), "accd");
 		//验证任务处理正确
 		Collection<TaskInstance> taskInstances = pi.getTaskMgmtInstance().getTaskInstances();
 		assertEquals(taskInstances.size(), 2);
 		Iterator<TaskInstance> iterator = taskInstances.iterator();
 		while (iterator.hasNext()) {
 			TaskInstance ti = iterator.next();
 			if (ti.getActorId().equals("ROLE_WATCH")) {
 				assertTrue(ti.isStartTaskInstance());
 				assertEquals(ti.getName(), "填写异态");
 				assertNotNull(ti.getStart());
 				assertNotNull(ti.getEnd());
 			} else if (ti.getActorId().equals("ROLE_DIRECTOR")) {
 				assertFalse(ti.isStartTaskInstance());
 				assertEquals(ti.getName(), "机房主任审核");
 				assertNull(ti.getStart());
 				assertNull(ti.getEnd());
 			} else {
 				fail();
 			}
 		}
 	}
 
 	@Test
 	public void testFindActiveTaskInstances() {
 		//开始一个没有权限控制的流程
 		Person person = (Person) this.sessionFactory.getCurrentSession().get(Person.class, 1L);
 		ProcessInstance processInstance = workFlowService.startProcessInstance("accidentReport", "accd-1", "descdesc",
 				person, "注释");
 		this.flushSessionAndCloseSessionAndNewASession();
 		ProcessInstance pi = workFlowService.getProcessInstance(processInstance.getId() + "");
 		TaskInstance ti = workFlowService.findActiveTaskInstance(pi);
 		assertEquals(ti.getActorId(), "ROLE_DIRECTOR");
 		assertEquals(ti.getName(), "机房主任审核");
 		assertNull(ti.getStart());
 		assertNull(ti.getEnd());
 		ti.end();
 		this.flushSessionAndCloseSessionAndNewASession();
 		ProcessInstance pi2 = workFlowService.getProcessInstance(processInstance.getId() + "");
 		TaskInstance ti2 = workFlowService.findActiveTaskInstance(pi2);
 		assertEquals(ti2.getActorId(), "ROLE_TEKOFFICER");
 		assertEquals(ti2.getName(), "技办审核定性");
 		assertNull(ti2.getStart());
 		assertNull(ti2.getEnd());
 	}
 
 	@Test
 	public void testFindActiveTaskInstancesSuffix() {
 		//开始一个没有权限控制的流程
 		Person person1 = (Person) this.sessionFactory.getCurrentSession().get(Person.class, 1L);
 		ProcessInstance processInstance = workFlowService.startProcessInstance("accidentReport", "accd-1", "descdesc",
 				person1, "注释");
 		this.flushSessionAndCloseSessionAndNewASession();
 		//不是机房主任
 		ProcessInstance pi = workFlowService.getProcessInstance(processInstance.getId() + "");
 		Person person = (Person) this.sessionFactory.getCurrentSession().get(Person.class, 2L);
 		assertFalse(workFlowService.hasActiveTaskInstance(pi, person));
 		TaskInstance ti = workFlowService.findActiveTaskInstance(pi);
 		ti.end();
 		this.flushSessionAndCloseSessionAndNewASession();
 		//是技办职员
 		ProcessInstance pi2 = workFlowService.getProcessInstance(processInstance.getId() + "");
 		Person person2 = (Person) this.sessionFactory.getCurrentSession().get(Person.class, 2L);
 		assertTrue(workFlowService.hasActiveTaskInstance(pi2, person2));
 		TaskInstance ti2 = workFlowService.findActiveTaskInstance(pi);
 		ti2.end();
 		this.flushSessionAndCloseSessionAndNewASession();
 		//不是总工
 		ProcessInstance pi3 = workFlowService.getProcessInstance(processInstance.getId() + "");
 		Person person3 = (Person) this.sessionFactory.getCurrentSession().get(Person.class, 2L);
 		assertFalse(workFlowService.hasActiveTaskInstance(pi3, person3));
 	}
 
 	@Test
 	public void testFindActiveTaskInstancesSuffixSuffix() {
 		//开始一个没有权限控制的流程
 		Person person1 = (Person) this.sessionFactory.getCurrentSession().get(Person.class, 1L);
 		ProcessInstance processInstance = workFlowService.startProcessInstance("accidentReport", "accd-1", "descdesc",
 				person1, "注释", new Dept(2L), FunModule.ACCD);
 		this.flushSessionAndCloseSessionAndNewASession();
 		//不是机房主任
 		ProcessInstance pi = workFlowService.getProcessInstance(processInstance.getId() + "");
 		Person person = (Person) this.sessionFactory.getCurrentSession().get(Person.class, 2L);
 		assertFalse(workFlowService.hasActiveTaskInstance(pi, person));
 		TaskInstance ti = workFlowService.findActiveTaskInstance(pi);
 		ti.end();
 		this.flushSessionAndCloseSessionAndNewASession();
 		//是技办职员但数据权限不匹配
 		ProcessInstance pi2 = workFlowService.getProcessInstance(processInstance.getId() + "");
 		Person person2 = (Person) this.sessionFactory.getCurrentSession().get(Person.class, 2L);
 		assertFalse(workFlowService.hasActiveTaskInstance(pi2, person2));
 	}
 
 	@Test
 	public void testFindActiveTaskInstancesSuffixSuffixSuffix() {
 		//开始一个没有权限控制的流程
 		Person person1 = (Person) this.sessionFactory.getCurrentSession().get(Person.class, 1L);
 		ProcessInstance processInstance = workFlowService.startProcessInstance("accidentReport", "accd-1", "descdesc",
 				person1, "注释", new Dept(1L), FunModule.ACCD);
 		this.flushSessionAndCloseSessionAndNewASession();
 		//不是机房主任
 		ProcessInstance pi = workFlowService.getProcessInstance(processInstance.getId() + "");
 		Person person = (Person) this.sessionFactory.getCurrentSession().get(Person.class, 2L);
 		assertFalse(workFlowService.hasActiveTaskInstance(pi, person));
 		TaskInstance ti = workFlowService.findActiveTaskInstance(pi);
 		ti.end();
 		this.flushSessionAndCloseSessionAndNewASession();
 		//是技办职员而且数据权限匹配
 		ProcessInstance pi2 = workFlowService.getProcessInstance(processInstance.getId() + "");
 		Person person2 = (Person) this.sessionFactory.getCurrentSession().get(Person.class, 2L);
 		assertTrue(workFlowService.hasActiveTaskInstance(pi2, person2));
 	}
 }
