 package gov.abrs.etms.service.duty;
 
 import static org.junit.Assert.*;
 import gov.abrs.etms.model.abnormal.Abnormal;
 import gov.abrs.etms.model.baseinfo.Operation;
 import gov.abrs.etms.model.duty.Duty;
 import gov.abrs.etms.model.duty.DutyRecord;
 import gov.abrs.etms.model.duty.DutyRecordA;
 import gov.abrs.etms.model.duty.DutyRecordD;
 import gov.abrs.etms.model.duty.DutyRecordO;
 import gov.abrs.etms.model.duty.DutyRecordP;
 import gov.abrs.etms.model.duty.DutyRecordW;
 import gov.abrs.etms.model.duty.DutyWarning;
 import gov.abrs.etms.model.duty.PatrolTime;
 import gov.abrs.etms.model.para.BroadByReason;
 import gov.abrs.etms.model.para.BroadByStation;
 import gov.abrs.etms.model.para.WarnType;
 import gov.abrs.etms.model.rept.BroadByTime;
 import gov.abrs.etms.service.abnormal.AbnormalService;
 import gov.abrs.etms.service.baseinfo.OperationService;
 import gov.abrs.etms.service.baseinfo.ParaDtlService;
 import gov.abrs.etms.service.baseinfo.PersonService;
 import gov.abrs.etms.service.util.UtilService;
 
 import java.util.Date;
 import java.util.List;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 
 import com.taiji.common.test.DatabaseTestCase;
 
@ContextConfiguration(locations = { "/conf/spring/applicationContext-test.xml","/conf/spring/applicationContext-jms-all.xml" })
 public class DutyRecordServiceTest extends DatabaseTestCase {
 	@Autowired
 	private DutyRecordService dutyRecordService;
 	@Autowired
 	private DutyService dutyService;
 	@Autowired
 	private UtilService utilService;
 	@Autowired
 	private OperationService operationService;
 	@Autowired
 	private AbnormalService abnormalService;
 	@Autowired
 	private ParaDtlService paraDtlService;
 	@Autowired
 	private PersonService personService;
 
 	//测试保存其他类型的值班记录
 	@Test
 	public void testSave1() {
 		Date now = this.utilService.getSysTime();
 		Duty duty = this.dutyService.get(2L);
 		int countDutyRecord = this.getRowsCount(DutyRecordO.class);
 		this.dutyRecordService.save(new DutyRecordO(duty, now, "123"));
 		assertEquals(this.getRowsCount(DutyRecordO.class), countDutyRecord + 1);
 	}
 
 	//测试保存异态类型的值班记录
 	@Test
 	public void testSave2() {
 		Date now = this.utilService.getSysTime();
 		Duty duty = this.dutyService.get(2L);
 		int countDutyRecord = this.getRowsCount(DutyRecordA.class);
 		this.dutyRecordService.save(new DutyRecordA(duty, now, "123", this.abnormalService.get(1L)));
 		assertEquals(this.getRowsCount(DutyRecordA.class), countDutyRecord + 1);
 	}
 
 	//测试保存代播类型的值班记录
 	@Test
 	public void testSave3() {
 		Date now = this.utilService.getSysTime();
 		Duty duty = this.dutyService.get(2L);
 		Operation op = this.operationService.get(1L);
 		int countDutyRecord = this.getRowsCount(DutyRecordD.class);
 		this.dutyRecordService.save(new DutyRecordD(duty, now, "123", new BroadByTime(op, new BroadByReason(1L), true,
 				"王来科", "赵霞", now, now, "D", new BroadByStation(1L), "234", "123", "0")));
 		assertEquals(this.getRowsCount(DutyRecordD.class), countDutyRecord + 1);
 	}
 
 	//测试保存告警类型的值班记录
 	@Test
 	public void testSave4() {
 		Date now = this.utilService.getSysTime();
 		Duty duty = this.dutyService.get(2L);
 		Operation op = this.operationService.get(1L);
 		WarnType warnType = (WarnType) paraDtlService.get(15L);
 		int countDutyRecord = this.getRowsCount(DutyRecordW.class);
 		this.dutyRecordService
 				.save(new DutyRecordW(duty, now, "123", new DutyWarning(op, now, warnType, "123", "234")));
 		assertEquals(this.getRowsCount(DutyRecordW.class), countDutyRecord + 1);
 	}
 
 	//测试保存巡视类型的值班记录
 	@Test
 	public void testSave5() {
 		Date now = this.utilService.getSysTime();
 		Duty duty = this.dutyService.get(2L);
 		int countDutyRecord = this.getRowsCount(DutyRecordP.class);
 		this.dutyRecordService.save(new DutyRecordP(duty, now, "123", new PatrolTime(now, "赵霞", now, now, "345")));
 		assertEquals(this.getRowsCount(DutyRecordP.class), countDutyRecord + 1);
 	}
 
 	//测试删除其他类型的值班记录
 	@Test
 	public void testDelete1() {
 		int countDutyRecord = this.getRowsCount(DutyRecordO.class);
 		this.dutyRecordService.delete(1L);
 		assertEquals(this.getRowsCount(DutyRecordO.class), countDutyRecord - 1);
 	}
 
 	//测试删除异态类型的值班记录
 	@Test
 	public void testDelete2() {
 		int countDutyRecord = this.getRowsCount(DutyRecordA.class);
 		int countAbnormal = getRowsCount(Abnormal.class);
 		this.dutyRecordService.delete(2L);
 		assertEquals(this.getRowsCount(DutyRecordA.class), countDutyRecord - 1);
 		assertEquals(this.getRowsCount(Abnormal.class), countAbnormal);
 	}
 
 	//测试删除代播类型的值班记录
 	@Test
 	public void testDelete3() {
 		int countDutyRecord = this.getRowsCount(DutyRecordD.class);
 		int countBroadByTime = getRowsCount(BroadByTime.class);
 		this.dutyRecordService.delete(3L);
 		assertEquals(this.getRowsCount(DutyRecordD.class), countDutyRecord - 1);
 		assertEquals(this.getRowsCount(BroadByTime.class), countBroadByTime - 1);
 	}
 
 	//测试删除告警类型的值班记录
 	@Test
 	public void testDelete4() {
 		int countDutyRecord = this.getRowsCount(DutyRecordW.class);
 		int countDutyWarning = getRowsCount(DutyWarning.class);
 		this.dutyRecordService.delete(4L);
 		assertEquals(this.getRowsCount(DutyRecordW.class), countDutyRecord - 1);
 		assertEquals(this.getRowsCount(DutyWarning.class), countDutyWarning - 1);
 	}
 
 	//测试删除巡视类型的值班记录
 	@Test
 	public void testDelete5() {
 		int countDutyRecord = this.getRowsCount(DutyRecordP.class);
 		int countPatrolTime = getRowsCount(PatrolTime.class);
 		this.dutyRecordService.delete(5L);
 		assertEquals(this.getRowsCount(DutyRecordP.class), countDutyRecord - 1);
 		assertEquals(this.getRowsCount(PatrolTime.class), countPatrolTime - 1);
 	}
 
 	@Test
 	public void testGetInfluenceRecords() {
 		Long braodByTimeId = 1L;
 		List<DutyRecord> dutyRecords = dutyRecordService.getInfluenceRecords(braodByTimeId);
 		assertEquals(1, dutyRecords.size());
 	}
 }
