 package gov.abrs.etms.service.duty;
 
 import static org.junit.Assert.*;
 import gov.abrs.etms.common.util.Carrier;
 import gov.abrs.etms.common.util.DateUtil;
 import gov.abrs.etms.model.abnormal.Abnormal;
 import gov.abrs.etms.model.baseinfo.Dept;
 import gov.abrs.etms.model.baseinfo.Group;
 import gov.abrs.etms.model.baseinfo.Operation;
 import gov.abrs.etms.model.duty.Duty;
 import gov.abrs.etms.model.duty.DutyCheck;
 import gov.abrs.etms.model.duty.DutyPrompt;
 import gov.abrs.etms.model.duty.DutyRecord;
 import gov.abrs.etms.model.duty.DutyRecordA;
 import gov.abrs.etms.model.duty.DutyRecordD;
 import gov.abrs.etms.model.duty.DutyRecordO;
 import gov.abrs.etms.model.duty.DutyRecordP;
 import gov.abrs.etms.model.duty.DutyRecordW;
 import gov.abrs.etms.model.duty.DutySchedule;
 import gov.abrs.etms.model.duty.DutyWarning;
 import gov.abrs.etms.model.duty.PatrolTime;
 import gov.abrs.etms.model.duty.StaffOnDuty;
 import gov.abrs.etms.model.para.BroadByReason;
 import gov.abrs.etms.model.para.BroadByStation;
 import gov.abrs.etms.model.para.WarnType;
 import gov.abrs.etms.model.rept.BroadByTime;
 import gov.abrs.etms.service.abnormal.AbnormalService;
 import gov.abrs.etms.service.baseinfo.OperationService;
 import gov.abrs.etms.service.baseinfo.ParaDtlService;
 import gov.abrs.etms.service.baseinfo.PersonService;
 import gov.abrs.etms.service.util.UtilService;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 
 import com.google.common.collect.Lists;
 import com.taiji.common.test.DatabaseTestCase;
 
@ContextConfiguration(locations = { "/conf/spring/applicationContext-test.xml","/conf/spring/applicationContext-jms-all.xml" })
 public class DutyServiceTest extends DatabaseTestCase {
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
 	@Autowired
 	private DutyScheduleService dutyScheduleService;
 
 	//测试新增排班
 	//	24 25 30   1 2 3 7 8 9
 	//早                       1 2 1 2
 	//晚                       2 1 2 1
 	//白班                                    1 2 
 	//测试添加一个一运转10号的班
 	@Test
 	public void testArrangeClass1() {
 		//历史数据个数和最后一个duty
 		int count = getRowsCount(Duty.class);
 		Duty last = dutyService.get(new Long(count));
 		//新增排班
 		int[] ids = { 1 };
 		DutySchedule ds = dutyScheduleService.get(1L);
 		dutyService.arrangeClass(new Dept(1L), DateUtil.createDate("2010-12-10"), DateUtil.createDate("2010-12-10"),
 				Lists.newArrayList(ds), ids, 1, 1);
 		this.flushSessionAndCloseSessionAndNewASession();
 		//测试新增成功
 		assertRowsCount(Duty.class, count + 1);
 		Duty newDuty = dutyService.getOneDayFirstDuty(new Dept(1L), DateUtil.createDate("2010-12-10"));
 		assertNotNull(newDuty);
 		assertEquals(newDuty.getFirst(), true);
 		assertEquals(newDuty.getLast(), true);
 		assertEquals(newDuty.getPreDuty().getId().intValue(), last.getId().intValue());
 	}
 
 	//测试新增排班
 	//	24 25 30   1 2 3 7 8 9
 	//早                       1 2 1 2
 	//晚                       2 1 2 1
 	//白班                                    1 2 
 	//测试添加一个二运转10号的班
 	@Test
 	public void testArrangeClass2() {
 		//历史数据个数和最后一个duty
 		int count = getRowsCount(Duty.class);
 		Duty last = dutyService.get(new Long(count));
 		//新增排班
 		int[] ids = { 1, 2 };
 		DutySchedule ds1 = dutyScheduleService.get(2L);
 		DutySchedule ds2 = dutyScheduleService.get(3L);
 		dutyService.arrangeClass(new Dept(1L), DateUtil.createDate("2010-12-10"), DateUtil.createDate("2010-12-10"),
 				Lists.newArrayList(ds1, ds2), ids, 1, 1);
 		this.flushSessionAndCloseSessionAndNewASession();
 		//测试新增成功
 		assertRowsCount(Duty.class, count + 2);
 		Duty last1 = dutyService.get(new Long(count));
 		Duty newDuty = last1.getNextDuty();
 		assertNotNull(newDuty);
 		assertEquals(newDuty.getFirst(), true);
 		assertEquals(newDuty.getLast(), null);
 		assertEquals(newDuty.getPreDuty().getId(), last.getId());
 		Duty newDuty2 = newDuty.getNextDuty();
 		assertNotNull(newDuty2);
 		assertEquals(newDuty2.getFirst(), null);
 		assertEquals(newDuty2.getLast(), true);
 		assertEquals(newDuty2.getPreDuty().getId(), newDuty.getId());
 	}
 
 	//测试新增排班
 	//	24 25 30   1 2 3 7 8 9
 	//早                       1 2 1 2
 	//晚                       2 1 2 1
 	//白班                                    1 2 
 	//测试添加一个一运转5号的班
 	@Test
 	public void testArrangeClass3() {
 		//历史数据个数和最后一个duty
 		int count = getRowsCount(Duty.class);
 		//新增排班
 		int[] ids = { 1 };
 		DutySchedule ds = dutyScheduleService.get(1L);
 		dutyService.arrangeClass(new Dept(1L), DateUtil.createDate("2010-12-05"), DateUtil.createDate("2010-12-05"),
 				Lists.newArrayList(ds), ids, 1, 1);
 		this.flushSessionAndCloseSessionAndNewASession();
 		//测试新增成功
 		assertRowsCount(Duty.class, count + 1);
 		Duty newDuty = dutyService.getOneDayFirstDuty(new Dept(1L), DateUtil.createDate("2010-12-05"));
 		assertNotNull(newDuty);
 		assertEquals(newDuty.getFirst(), true);
 		assertEquals(newDuty.getLast(), true);
 		assertEquals(newDuty.getPreDuty().getId().intValue(), 13);
 		Duty nextDuty = dutyService.get(14L);
 		assertEquals(nextDuty.getPreDuty().getId(), newDuty.getId());
 	}
 
 	//测试新增排班
 	//	24 25 30   1 2 3 7 8 9
 	//早                       1 2 1 2
 	//晚                       2 1 2 1
 	//白班                                    1 2 
 	//测试添加一个一运转7号的班，替换原先7号的班
 	@Test
 	public void testArrangeClass4() {
 		//历史数据个数和最后一个duty
 		int count = getRowsCount(Duty.class);
 		//新增排班
 		int[] ids = { 1 };
 		DutySchedule ds = dutyScheduleService.get(1L);
 		dutyService.arrangeClass(new Dept(1L), DateUtil.createDate("2010-12-07"), DateUtil.createDate("2010-12-07"),
 				Lists.newArrayList(ds), ids, 1, 1);
 		this.flushSessionAndCloseSessionAndNewASession();
 		//测试新增成功
 		assertRowsCount(Duty.class, count - 1);//删除原先7号的两个班，新增一个7号的班
 		Duty newDuty = dutyService.getOneDayFirstDuty(new Dept(1L), DateUtil.createDate("2010-12-07"));
 		assertNotNull(newDuty);
 		assertEquals(newDuty.getFirst(), true);
 		assertEquals(newDuty.getLast(), true);
 		assertEquals(newDuty.getPreDuty().getId().intValue(), 13);
 		Duty nextDuty = dutyService.get(16L);
 		assertEquals(nextDuty.getPreDuty().getId(), newDuty.getId());
 	}
 
 	//测试新增排班
 	//	24 25 30   1 2 3 7 8 9
 	//早                       1 2 1 2
 	//晚                       2 1 2 1
 	//白班                                    1 2 
 	//测试添加一个一运转3号到8号的二运转班，周期为3，开始为第二天
 	//1天 2天 3天
 	//1  2   1
 	//1  2   2
 	//   开
 	//排完正常应该是
 	//	24 25 30   1 2 3 4 5 6 7 8 9
 	//早                       1 2 2 1 1 2 1 1
 	//晚                       2 1 2 2 1 2 2 1
 	//白班                                                   2 
 	@Test
 	public void testArrangeClass5() {
 		//历史数据个数和最后一个duty
 		int count = getRowsCount(Duty.class);
 		//新增排班
 		int[] ids = { 1, 2, 1, 1, 2, 2 };
 		DutySchedule ds1 = dutyScheduleService.get(2L);//早
 		DutySchedule ds2 = dutyScheduleService.get(3L);//晚
 		dutyService.arrangeClass(new Dept(1L), DateUtil.createDate("2010-12-03"), DateUtil.createDate("2010-12-08"),
 				Lists.newArrayList(ds1, ds2), ids, 3, 2);
 		this.flushSessionAndCloseSessionAndNewASession();
 		//测试新增成功
 		assertRowsCount(Duty.class, count + 7);//删除原先3号8号总共5个班，新增一个2*6总共12班
 		Duty newDuty = dutyService.getOneDayFirstDuty(new Dept(1L), DateUtil.createDate("2010-12-03"));
 		assertNotNull(newDuty);
 		assertEquals(newDuty.getFirst(), true);
 		assertEquals(newDuty.getLast(), null);
 		assertEquals(newDuty.getPreDuty().getId().intValue(), 11);
 		Duty lastDuty = dutyService.getOneDayLastDuty(new Dept(1L), DateUtil.createDate("2010-12-08"));
 		Duty nextDuty = dutyService.get(17L);//9号的班
 		assertEquals(nextDuty.getPreDuty().getId(), lastDuty.getId());//8号最后一个
 	}
 
 	//测试新增排班
 	//	24 25 30   1 2 3 7 8 9
 	//早                       1 2 1 2
 	//晚                       2 1 2 1
 	//白班                                    1 2 
 	//测试添加一个一运转3号到10号的二运转班，周期为3，开始为第二天
 	//1天 2天 3天
 	//1  2   1
 	//1  2   2
 	//   开
 	//排完正常应该是
 	//	24 25 30   1 2 3 4 5 6 7 8 9 10
 	//早                       1 2 2 1 1 2 1 1 2 1
 	//晚                       2 1 2 2 1 2 2 1 2 2
 	//白班                                                    
 	@Test
 	public void testArrangeClass6() {
 		//历史数据个数和最后一个duty
 		int count = getRowsCount(Duty.class);
 		//新增排班
 		int[] ids = { 1, 2, 1, 1, 2, 2 };
 		DutySchedule ds1 = dutyScheduleService.get(2L);//早
 		DutySchedule ds2 = dutyScheduleService.get(3L);//晚
 		dutyService.arrangeClass(new Dept(1L), DateUtil.createDate("2010-12-03"), DateUtil.createDate("2010-12-10"),
 				Lists.newArrayList(ds1, ds2), ids, 3, 2);
 		this.flushSessionAndCloseSessionAndNewASession();
 		//测试新增成功
 		assertRowsCount(Duty.class, count + 10);//删除原先3号9号总共6个班，新增一个2*8总共16班
 		Duty newDuty = dutyService.getOneDayFirstDuty(new Dept(1L), DateUtil.createDate("2010-12-03"));
 		assertNotNull(newDuty);
 		assertEquals(newDuty.getFirst(), true);
 		assertEquals(newDuty.getLast(), null);
 		assertEquals(newDuty.getPreDuty().getId().intValue(), 11);
 		Duty lastDuty = dutyService.getOneDayLastDuty(new Dept(1L), DateUtil.createDate("2010-12-10"));
 		Duty nextDuty = dutyService.getOneDayFirstDuty(new Dept(1L), DateUtil.createDate("2010-12-10"));
 		assertEquals(lastDuty.getPreDuty().getId(), nextDuty.getId());
 		assertNull(lastDuty.getNextDuty());
 	}
 
 	/*---------------------分界线-------------------------*/
 	//测试新增排班
 	@Test
 	public void testSave() {
 		Duty duty = new Duty();
 		duty.setStartTime(DateUtil.createDateTime("2010-11-25 8:00:00"));
 		duty.setEndTime(DateUtil.createDateTime("2010-11-25 12:00:00"));
 		duty.setGroup(new Group(2L));
 		duty.setDayPartCount(2);
 		duty.setPreDuty(this.dutyService.get(2L));
 		duty.setSchName("早班");
 		dutyService.save(duty);
 		List<Duty> all = dutyService.getAll();
 		assertEquals(all.size(), 18);
 	}
 
 	//测试级联更新
 	@Test
 	public void testSaveCasacade() {
 		int countDuty = getRowsCount(Duty.class);
 		int countDutyRecord = getRowsCount(DutyRecord.class);
 		int countDutyCheck = getRowsCount(DutyCheck.class);
 		int countDutyPrompt = getRowsCount(DutyPrompt.class);
 		int countBroadByTime = getRowsCount(BroadByTime.class);
 		int countAbnormal = getRowsCount(Abnormal.class);
 		int countDutyWarning = getRowsCount(DutyWarning.class);
 		int countPatrolTime = getRowsCount(PatrolTime.class);
 		int countStaffOnDuty = getRowsCount(StaffOnDuty.class);
 
 		Date now = this.utilService.getSysTime();
 		Duty duty = this.dutyService.get(2L);
 		Operation op = this.operationService.get(1L);
 		WarnType warnType = (WarnType) paraDtlService.get(15L);
 
 		List<DutyRecord> list1 = Lists.newArrayList(new DutyRecordO(duty, now, "123"), new DutyRecordA(duty, now,
 				"123", this.abnormalService.get(1L)), new DutyRecordD(duty, now, "123", new BroadByTime(op,
 				new BroadByReason(1L), true, "王来科", "赵霞", now, now, "D", new BroadByStation(1L), "234", "123", "0")),
 				new DutyRecordW(duty, now, "123", new DutyWarning(op, now, warnType, "123", "234")), new DutyRecordP(
 						duty, now, "123", new PatrolTime(now, "赵霞", now, now, "345")));
 		List<DutyCheck> list2 = Lists.newArrayList(new DutyCheck(duty, "123", "吕斌", now));
 		List<DutyPrompt> list3 = Lists
 				.newArrayList(new DutyPrompt(duty, "123", now, "吕斌", now, DateUtil.addDay(now, 1)));
 		duty.setDutyRecords(list1);
 		duty.setDutyChecks(list2);
 		duty.setDutyPrompts(list3);
 		this.dutyService.save(duty);
 
 		assertEquals(getRowsCount(Duty.class), countDuty);
 		assertEquals(getRowsCount(DutyRecord.class), countDutyRecord + 5);
 		assertEquals(getRowsCount(DutyCheck.class), countDutyCheck + 1);
 		assertEquals(getRowsCount(DutyPrompt.class), countDutyPrompt + 1);
 		assertEquals(getRowsCount(BroadByTime.class), countBroadByTime + 1);
 		assertEquals(getRowsCount(Abnormal.class), countAbnormal);
 		assertEquals(getRowsCount(DutyWarning.class), countDutyWarning + 1);
 		assertEquals(getRowsCount(PatrolTime.class), countPatrolTime + 1);
 		assertEquals(getRowsCount(StaffOnDuty.class), countStaffOnDuty);
 	}
 
 	//测试查找全部
 	@Test
 	public void testGetAll() {
 		List<Duty> list = this.dutyService.getAll();
 		assertEquals(list.size(), 17);
 	}
 
 	//测试级联删除
 	@Test
 	public void testDelete() {
 		int countDuty = getRowsCount(Duty.class);
 		int countDutyRecord = getRowsCount(DutyRecord.class);
 		int countDutyCheck = getRowsCount(DutyCheck.class);
 		int countDutyPrompt = getRowsCount(DutyPrompt.class);
 		int countBroadByTime = getRowsCount(BroadByTime.class);
 		int countAbnormal = getRowsCount(Abnormal.class);
 		int countDutyWarning = getRowsCount(DutyWarning.class);
 		int countPatrolTime = getRowsCount(PatrolTime.class);
 		int countStaffOnDuty = getRowsCount(StaffOnDuty.class);
 
 		this.dutyService.delete(1L);
 		assertEquals(getRowsCount(Duty.class), countDuty - 1);
 		assertEquals(getRowsCount(DutyRecord.class), countDutyRecord - 5);
 		assertEquals(getRowsCount(DutyCheck.class), countDutyCheck - 1);
 		assertEquals(getRowsCount(DutyPrompt.class), countDutyPrompt - 1);
 		assertEquals(getRowsCount(BroadByTime.class), countBroadByTime - 1);
 		assertEquals(getRowsCount(Abnormal.class), countAbnormal);
 		assertEquals(getRowsCount(DutyWarning.class), countDutyWarning - 1);
 		assertEquals(getRowsCount(PatrolTime.class), countPatrolTime - 1);
 		assertEquals(getRowsCount(StaffOnDuty.class), countStaffOnDuty - 2);
 	}
 
 	//测试查找某部门某天是否有班（是）
 	@Test
 	public void testHasDutyThisDay1() {
 		Boolean flag = this.dutyService.hasDutyThisDay(new Dept(1L), DateUtil.createDate("2010-11-24"));
 		assertTrue(flag);
 	}
 
 	//测试查找某部门某天是否有班(否)
 	@Test
 	public void testHasDutyThisDay2() {
 		Boolean flag = this.dutyService.hasDutyThisDay(new Dept(2L), DateUtil.createDate("2010-11-24"));
 		assertFalse(flag);
 	}
 
 	//测试某部门某天是否所有的班都交接了(是)
 	@Test
 	public void testHasDutyShiftedThisDay1() {
 		Boolean flag = this.dutyService.hasDutyShiftedThisDay(new Dept(1L), DateUtil.createDate("2010-11-25"));
 		assertTrue(flag);
 	}
 
 	//测试某部门某天是否所有的班都交接了(否)
 	@Test
 	public void testHasDutyShiftedThisDay2() {
 		Boolean flag = this.dutyService.hasDutyShiftedThisDay(new Dept(1L), DateUtil.createDate("2010-11-24"));
 		assertFalse(flag);
 	}
 
 	//测试某部门某天是否所有的班都交接了(当天没有班)
 	@Test
 	public void testHasDutyShiftedThisDay3() {
 		Boolean flag = this.dutyService.hasDutyShiftedThisDay(new Dept(1L), DateUtil.createDate("2010-10-24"));
 		assertFalse(flag);
 	}
 
 	//测试找到某天的所有班
 	@Test
 	public void testGetByThisDay() {
 		List<Duty> list = this.dutyService.getByThisDay(new Dept(1L), DateUtil.createDate("2010-11-24"));
 		assertEquals(3, list.size());
 	}
 
 	//测试查找某天某部门的最后一个班
 	@Test
 	public void testGetOneDayLastDuty1() {
 		Duty duty = this.dutyService.getOneDayLastDuty(new Dept(1L), DateUtil.createDate("2010-11-24"));
 		assertEquals(new Long(3), duty.getId());
 	}
 
 	//测试查找某天某部门的最后一个班（没有的情况下）
 	@Test
 	public void testGetOneDayLastDuty2() {
 		Duty duty = this.dutyService.getOneDayLastDuty(new Dept(1L), DateUtil.createDate("2010-11-26"));
 		assertNull(duty);
 	}
 
 	//查找某天某部门的第一个班(这个班是本部门系统中的第一个班)
 	@Test
 	public void testGetOneDayFirstDuty1() {
 		Duty duty = this.dutyService.getOneDayFirstDuty(new Dept(1L), DateUtil.createDate("2010-11-24"));
 		assertEquals(new Long(1), duty.getId());
 	}
 
 	//查找某天某部门的第一个班（没有的情况下）
 	@Test
 	public void testGetOneDayFirstDuty2() {
 		Duty duty = this.dutyService.getOneDayFirstDuty(new Dept(1L), DateUtil.createDate("2010-11-26"));
 		assertNull(duty);
 	}
 
 	//查找某天某部门的第一个班
 	@Test
 	public void testGetOneDayFirstDuty3() {
 		Duty duty = this.dutyService.getOneDayFirstDuty(new Dept(1L), DateUtil.createDate("2010-11-25"));
 		assertEquals(new Long(4), duty.getId());
 	}
 
 	//删除一段时间的排班(包括级联)
 	@Test
 	public void testDeleteALongTime() {
 		int countDuty = getRowsCount(Duty.class);
 		int countDutyRecord = getRowsCount(DutyRecord.class);
 		int countDutyCheck = getRowsCount(DutyCheck.class);
 		int countDutyPrompt = getRowsCount(DutyPrompt.class);
 		int countBroadByTime = getRowsCount(BroadByTime.class);
 		int countAbnormal = getRowsCount(Abnormal.class);
 		int countDutyWarning = getRowsCount(DutyWarning.class);
 		int countPatrolTime = getRowsCount(PatrolTime.class);
 		int countStaffOnDuty = getRowsCount(StaffOnDuty.class);
 		this.dutyService.delete(new Dept(1L), DateUtil.createDate("2010-11-24"), DateUtil.createDate("2010-12-1"));
 
 		assertEquals(getRowsCount(Duty.class), countDuty - 9);
 		assertEquals(getRowsCount(DutyRecord.class), countDutyRecord - 5);
 		assertEquals(getRowsCount(DutyCheck.class), countDutyCheck - 2);
 		assertEquals(getRowsCount(DutyPrompt.class), countDutyPrompt - 1);
 		assertEquals(getRowsCount(BroadByTime.class), countBroadByTime - 1);
 		assertEquals(getRowsCount(Abnormal.class), countAbnormal);
 		assertEquals(getRowsCount(DutyWarning.class), countDutyWarning - 1);
 		assertEquals(getRowsCount(PatrolTime.class), countPatrolTime - 1);
 		assertEquals(getRowsCount(StaffOnDuty.class), countStaffOnDuty - 10);
 	}
 
 	//测试获取某人某天已经交接过的班的集合(包括昨天的最后一个班)
 	@Test
 	public void testGetAlreadyShiftedDuties() {
 		List<Duty> list = this.dutyService.getAlreadyShiftedDuties(new Dept(1L), DateUtil.createDate("2010-11-25"),
 				this.personService.get(2L));
 		assertEquals(3, list.size());
 		Boolean flag1 = false;
 		Boolean flag2 = false;
 		Boolean flag3 = false;
 		for (Duty duty : list) {
 			if (duty.getId() == 4L) {
 				flag1 = true;
 			}
 			if (duty.getId() == 5L) {
 				flag2 = true;
 			}
 			if (duty.getId() == 3L) {
 				flag3 = true;
 			}
 		}
 		assertTrue(flag1);
 		assertTrue(flag2);
 		assertTrue(flag3);
 	}
 
 	//测试是否今天我的所有的班都已经交接完成(是)
 	@Test
 	public void testIsAllDayDutiesShifted1() {
 		Boolean flag = this.dutyService.isAllDayDutiesShifted(new Dept(1L), DateUtil.createDate("2010-11-25"),
 				this.personService.get(2L));
 		assertTrue(flag);
 	}
 
 	//测试是否今天所有的班都已经交接完成(否)
 	@Test
 	public void testIsAllDayDutiesShifted2() {
 		Boolean flag = this.dutyService.isAllDayDutiesShifted(new Dept(1L), DateUtil.createDate("2010-11-24"),
 				this.personService.get(2L));
 		assertFalse(flag);
 	}
 
 	//测试查找一天内所有未交接的班
 	@Test
 	public void testGetCanShiftDuties() {
 		List<Duty> list = this.dutyService.getCanShiftDuties(new Dept(1L), DateUtil.createDate("2010-11-24"),
 				this.personService.get(1L));
 		Boolean flag1 = false;
 		for (Duty duty : list) {
 			if (duty.getId() == 1L) {
 				flag1 = true;
 			}
 		}
 		assertTrue(flag1);
 	}
 
 	//测试查找某部门某天之前的最后一个班(有)
 	@Test
 	public void testGetPrevLastDuty1() {
 		Duty duty = this.dutyService.getPrevLastDuty(new Dept(1L), DateUtil.createDate("2010-11-26"));
 		Boolean flag1 = false;
 		if (duty.getId() == 5L) {
 			flag1 = true;
 		}
 		assertTrue(flag1);
 	}
 
 	//测试查找某部门某天之前的最后一个班(否)
 	@Test
 	public void testGetPrevLastDuty2() {
 		Duty duty = this.dutyService.getPrevLastDuty(new Dept(1L), DateUtil.createDate("2010-11-22"));
 		assertNull(duty);
 	}
 
 	//测试查找某部门某天之后的第一个班(有)
 	@Test
 	public void testGetNextFirstDuty1() {
 		Duty duty = this.dutyService.getNextFirstDuty(new Dept(1L), DateUtil.createDate("2010-11-20"));
 		Boolean flag1 = false;
 		if (duty.getId() == 1L) {
 			flag1 = true;
 		}
 		assertTrue(flag1);
 	}
 
 	//测试查找某部门某天之后的第一个班(否)
 	@Test
 	public void testGetNextFirstDuty2() {
 		Duty duty = this.dutyService.getNextFirstDuty(new Dept(1L), DateUtil.createDate("2010-12-10"));
 		assertNull(duty);
 	}
 
 	//测试查询排班信息的分页方法
 	@Test
 	public void testGet1() {
 		Carrier<Duty> carrier = new Carrier<Duty>();
 		carrier.setSidx("date");
 		Dept dept = new Dept(1L);
 		Group group = new Group(1L);
 		Date startDate = DateUtil.createDate("2010-11-24");
 		Date endDate = DateUtil.createDate("2010-11-25");
 		dutyService.get(carrier, dept, group, startDate, endDate);
 
 		assertEquals(2, carrier.getResult().size());
 	}
 
 	//测试查询排班信息的分页方法
 	@Test
 	public void testGet2() {
 		Carrier<Duty> carrier = new Carrier<Duty>();
 		carrier.setSidx("staffOnDuty");
 		Dept dept = new Dept(1L);
 		Group group = new Group(1L);
 		Date startDate = DateUtil.createDate("2010-11-24");
 		Date endDate = DateUtil.createDate("2010-11-25");
 		dutyService.get(carrier, dept, group, startDate, endDate);
 
 		assertEquals(2, carrier.getResult().size());
 	}
 
 	//测试根据开始时间结束时间部门查找班，不分页
 	@Test
 	public void testGetDuties() {
 		Dept dept = new Dept(1L);
 		Date startDate = DateUtil.createDate("2010-11-24");
 		Date endDate = DateUtil.createDate("2010-11-25");
 		List<Duty> list = dutyService.getDuties(dept, startDate, endDate);
 		assertEquals(5, list.size());
 	}
 
 	//测试查询值班记录
 	@Test
 	public void testGet3() {
 		Carrier<Duty> carrier = new Carrier<Duty>();
 		carrier.setSidx("date");
 		Dept dept = new Dept(1L);
 		Group group = new Group(1L);
 		Date startDate = DateUtil.createDate("2010-11-24");
 		Date endDate = DateUtil.createDate("2010-11-25");
 		String empName = "赵霞";
 		Boolean hasCheckes = true;
 		dutyService.get(carrier, dept, group, empName, hasCheckes, startDate, endDate);
 		assertEquals(1, carrier.getResult().size());
 	}
 
 	//测试查询值班记录
 	@Test
 	public void testGet4() {
 		Carrier<Duty> carrier = new Carrier<Duty>();
 		carrier.setSidx("staffOnDuty");
 		Dept dept = new Dept(1L);
 		Group group = new Group(2L);
 		Date startDate = DateUtil.createDate("2010-11-24");
 		Date endDate = DateUtil.createDate("2010-11-25");
 		String empName = "赵霞";
 		Boolean hasCheckes = false;
 		dutyService.get(carrier, dept, group, empName, hasCheckes, startDate, endDate);
 		assertEquals(3, carrier.getResult().size());
 	}
 
 	//测试添加一条值班记录审核信息
 	@Test
 	public void testAddADutyCheck() {
 		int countDutyCheck = getRowsCount(DutyCheck.class);
 		Duty duty = dutyService.get(1L);
 		DutyCheck dutyCheck = new DutyCheck(duty, "123", "边迪", DateUtil.createDateTime("2010-01-01 00:00:00"));
 		dutyService.addADutyCheck(duty, dutyCheck);
 		assertEquals(countDutyCheck + 1, getRowsCount(DutyCheck.class));
 	}
 
 	//测试为了生成排班表查询某部门，某个运转数的，一段时间内的班
 	@Test
 	public void testGet5() {
 		Dept dept = new Dept(1L);
 		Date start = DateUtil.createDate("2010-11-24");
 		Date end = DateUtil.createDate("2010-11-25");
 		int dayPartCount = 2;
 		List<Duty> list = dutyService.get(dept, start, end, dayPartCount);
 		assertEquals(5, list.size());
 	}
 
 	//测试快速调班的Service
 	@Test
 	public void testSavechangeDetails() {
 		JSONArray array = new JSONArray();
 		JSONObject obj = new JSONObject();
 		obj.put("id", 1);
 		obj.put("moniter", "123");
 		JSONArray array1 = new JSONArray();
 		JSONObject obj1 = new JSONObject();
 		obj1.put("watcher", "234");
 		array1.add(obj1);
 		obj.put("watchers", array1);
 		array.add(obj);
 		dutyService.savechangeDetails(array);
 
 		assertEquals("234", dutyService.get(1L).getWatchers().get(0).getName());
 		assertEquals("123", dutyService.get(1L).getMoniter().getName());
 	}
 
 	@Test
 	public void testGetEffectedGroupNumAjax() {
 		String empName = "赵霞";
 		DutySchedule dutySchedule = dutyScheduleService.get(2L);
 		Date startDate = DateUtil.createDate("2010-11-24");
 		Date endDate = DateUtil.createDate("2010-11-24");
 		int num = dutyService.getEffectedGroupNum(empName, dutySchedule, startDate, endDate);
 
 		assertEquals(1, num);
 	}
 
 	//测试替班
 	@Test
 	public void testGetCompareList1() throws UnsupportedEncodingException {
 		Duty duty1 = dutyService.get(1L);
 		Duty duty2 = dutyService.get(2L);
 		List<Duty> oldList = Lists.newArrayList(duty1, duty2);
 
 		Map<String, String[]> params = new HashMap<String, String[]>();
 		params.put("startDateArray", new String[] { "2010-11-24" });
 		params.put("endDateArray", new String[] { "2010-11-24" });
 		params.put("dutyScheduleNameArray", new String[] { new String("早班".getBytes("UTF-8"), "iso8859_1") + "_2" });
 		params.put("empNameArray", new String[] { new String("赵霞".getBytes("UTF-8"), "iso8859_1") });
 		params.put("flagsArray", new String[] { "0" });
 		params.put("empRpNameArray", new String[] { new String("王二虎".getBytes("UTF-8"), "iso8859_1") });
 		params.put("dutyScheduleRpNameArray", new String[] {});
 
 		List<Duty> newList = dutyService.getCompareList(oldList, params);
 		assertEquals("<span style='font-weight:bold;background:#9901d8'><font color='white'>王二虎</font></span>", newList
 				.get(0).getMoniter().getName());
 		assertEquals("赵霞", oldList.get(0).getMoniter().getName());
 		assertEquals("早班", newList.get(0).getSchName());
 		assertEquals(new Integer(2), newList.get(0).getDayPartCount());
 		assertEquals("2010-11-24 09:20:00", DateUtil.getDateTimeStr(newList.get(0).getStartTime()));
 		assertEquals("赵霞", newList.get(1).getMoniter().getName());
 	}
 
 	//测试换班1
 	@Test
 	public void testGetCompareList2() throws UnsupportedEncodingException {
 		Duty duty1 = dutyService.get(1L);
 		Duty duty2 = dutyService.get(2L);
 		List<Duty> oldList = Lists.newArrayList(duty1, duty2);
 
 		Map<String, String[]> params = new HashMap<String, String[]>();
 		params.put("startDateArray", new String[] { "2010-11-24" });
 		params.put("endDateArray", new String[] { "2010-11-24" });
 		params.put("dutyScheduleNameArray", new String[] { new String("早班".getBytes("UTF-8"), "iso8859_1") + "_2" });
 		params.put("empNameArray", new String[] { new String("赵霞".getBytes("UTF-8"), "iso8859_1") });
 		params.put("flagsArray", new String[] { "1" });
 		params.put("empRpNameArray", new String[] { new String("牛博辉".getBytes("UTF-8"), "iso8859_1") });
 		params.put("dutyScheduleRpNameArray", new String[] { new String("晚班".getBytes("UTF-8"), "iso8859_1") + "_2" });
 
 		List<Duty> newList = dutyService.getCompareList(oldList, params);
 		assertEquals("<span style='font-weight:bold;background:blue'><font color='white'>牛博辉</font></span>", newList
 				.get(0).getMoniter().getName());
 		assertEquals("赵霞", oldList.get(0).getMoniter().getName());
 		assertEquals("早班", newList.get(0).getSchName());
 		assertEquals(new Integer(2), newList.get(0).getDayPartCount());
 		assertEquals("2010-11-24 09:20:00", DateUtil.getDateTimeStr(newList.get(0).getStartTime()));
 		assertEquals("牛博辉", newList.get(1).getWatchers().get(0).getName());
 	}
 
 	//测试换班2
 	@Test
 	public void testGetCompareList3() throws UnsupportedEncodingException {
 		Duty duty1 = dutyService.get(1L);
 		Duty duty2 = dutyService.get(2L);
 		List<Duty> oldList = Lists.newArrayList(duty1, duty2);
 
 		Map<String, String[]> params = new HashMap<String, String[]>();
 		params.put("startDateArray", new String[] { "2010-11-24" });
 		params.put("endDateArray", new String[] { "2010-11-24" });
 		params.put("dutyScheduleNameArray", new String[] { new String("早班".getBytes("UTF-8"), "iso8859_1") + "_2" });
 		params.put("empNameArray", new String[] { new String("王健".getBytes("UTF-8"), "iso8859_1") });
 		params.put("flagsArray", new String[] { "1" });
 		params.put("empRpNameArray", new String[] { new String("牛博辉".getBytes("UTF-8"), "iso8859_1") });
 		params.put("dutyScheduleRpNameArray", new String[] { new String("晚班".getBytes("UTF-8"), "iso8859_1") + "_2" });
 
 		List<Duty> newList = dutyService.getCompareList(oldList, params);
 		assertEquals("<span style='font-weight:bold;background:blue'><font color='white'>牛博辉</font></span>", newList
 				.get(0).getWatchers().get(0).getName());
 		assertEquals("赵霞", oldList.get(0).getMoniter().getName());
 		assertEquals("早班", newList.get(0).getSchName());
 		assertEquals(new Integer(2), newList.get(0).getDayPartCount());
 		assertEquals("2010-11-24 09:20:00", DateUtil.getDateTimeStr(newList.get(0).getStartTime()));
 		assertEquals("<span style='font-weight:bold;background:blue'><font color='white'>王健</font></span>", newList
 				.get(1).getWatchers().get(0).getName());
 	}
 
 	//测试替班
 	@Test
 	public void testUpdateReplaceResult1() {
 		JSONObject obj = new JSONObject();
 		obj.put("startDateMin", "2010-11-24");
 		obj.put("endDateMax", "2010-11-24");
 		obj.put("dpId", 1L);
 
 		JSONArray list = new JSONArray();
 		JSONObject obj1 = new JSONObject();
 		obj1.put("startDate", "2010-11-24");
 		obj1.put("endDate", "2010-11-24");
 		obj1.put("flag", 0);
 		obj1.put("isReplacedName", "赵霞");
 		obj1.put("replaceName", "王二虎");
 		obj1.put("schName", "早班");
 		obj1.put("dayPartCount", 2);
 		list.add(obj1);
 		obj.put("list", list);
 
 		dutyService.updateReplaceResult(obj);
 
 		assertEquals("王二虎", dutyService.get(1L).getMoniter().getName());
 	}
 
 	//测试换班
 	@Test
 	public void testUpdateReplaceResult2() {
 		JSONObject obj = new JSONObject();
 		obj.put("startDateMin", "2010-11-24");
 		obj.put("endDateMax", "2010-11-24");
 		obj.put("dpId", 1L);
 
 		JSONArray list = new JSONArray();
 		JSONObject obj1 = new JSONObject();
 		obj1.put("startDate", "2010-11-24");
 		obj1.put("endDate", "2010-11-24");
 		obj1.put("flag", 1);
 		obj1.put("isReplacedName", "王健");
 		obj1.put("replaceName", "牛博辉");
 		obj1.put("schName", "早班");
 		obj1.put("dayPartCount", 2);
 		obj1.put("schNameRp", "晚班");
 		obj1.put("dayPartCountRp", 2);
 
 		list.add(obj1);
 		obj.put("list", list);
 
 		dutyService.updateReplaceResult(obj);
 
 		assertEquals("牛博辉", dutyService.get(1L).getWatchers().get(0).getName());
 		assertEquals("王健", dutyService.get(2L).getWatchers().get(0).getName());
 	}
 
 	@Test
 	public void testGetDuty1() {
 		Date startDate = DateUtil.createDate("2010-11-24");
 		Date endDate = DateUtil.createDate("2010-11-24");
 		Dept dept = new Dept(1L);
 		String empName = "赵霞";
 		List<Duty> list = dutyService.getDuties(dept, startDate, endDate, empName);
 		assertEquals(3, list.size());
 		Boolean flag1 = false;
 		Boolean flag2 = false;
 		Boolean flag3 = false;
 		for (Duty duty : list) {
 			if (duty.getId() == 1L) {
 				flag1 = true;
 			}
 			if (duty.getId() == 2L) {
 				flag2 = true;
 			}
 			if (duty.getId() == 3L) {
 				flag3 = true;
 			}
 		}
 		assertTrue(flag1);
 		assertTrue(flag2);
 		assertTrue(flag3);
 	}
 
 	@Test
 	public void testGetDuty2() {
 		Date startDate = DateUtil.createDate("2010-11-24");
 		Date endDate = DateUtil.createDate("2010-11-24");
 		Dept dept = new Dept(1L);
 		String empName = "王健";
 		List<Duty> list = dutyService.getDuties(dept, startDate, endDate, empName);
 		assertEquals(1, list.size());
 		assertEquals(Long.valueOf(1), list.get(0).getId());
 	}
 
 	@Test
 	public void testDeleteStaffOnDuty1() {
 		List<Long> duIds1 = Lists.newArrayList(1L, 2L);
 		String empName1 = "赵霞";
 		dutyService.deleteStaffOnDuty(duIds1, empName1);
 		assertEquals("王健", dutyService.get(1L).getMoniter().getName());
 		assertEquals("牛博辉", dutyService.get(2L).getMoniter().getName());
 	}
 
 	@Test
 	public void testDeleteStaffOnDuty2() {
 		List<Long> duIds2 = Lists.newArrayList(3L);
 		String empName2 = "牛博辉";
 		dutyService.deleteStaffOnDuty(duIds2, empName2);
 		assertEquals(0, dutyService.get(3L).getWatchers().size());
 	}
 
 	@Test
 	public void testGetDuty3() {
 		Date startDate = DateUtil.createDate("2010-11-24");
 		Date endDate = DateUtil.createDate("2010-11-24");
 		Dept dept = new Dept(1L);
 		Group group = null;
 		List<Duty> list = dutyService.getDuties(dept, startDate, endDate, group);
 		assertEquals(3, list.size());
 		assertEquals(Long.valueOf(1), list.get(0).getId());
 	}
 
 	@Test
 	public void testGetDuty4() {
 		Date startDate = DateUtil.createDate("2010-11-24");
 		Date endDate = DateUtil.createDate("2010-11-24");
 		Dept dept = new Dept(1L);
 		Group group = new Group(1L);
 		List<Duty> list = dutyService.getDuties(dept, startDate, endDate, group);
 		assertEquals(1, list.size());
 		assertEquals(Long.valueOf(1), list.get(0).getId());
 	}
 
 	@Test
 	public void testAddStaffOnDuty1() {
 		List<Long> duIds1 = Lists.newArrayList(1L, 2L);
 		String empName1 = "王虎";
 		dutyService.addStaffOnDuty(duIds1, empName1);
 		assertEquals("王虎", dutyService.get(1L).getWatchers().get(1).getName());
 		assertEquals("王虎", dutyService.get(2L).getWatchers().get(1).getName());
 	}
 
 	@Test
 	public void testGet6() {
 		Dept dept = new Dept(1L);
 		Date date = DateUtil.createDate("2010-11-24");
 		Duty duty = dutyService.get(dept, date);
 		assertEquals(Long.valueOf(3), duty.getId());
 	}
 
 	@Test
 	public void testHasDutyShiftedFromThisDay() {
 		Dept dept = new Dept(1L);
 		Date startDate = DateUtil.createDate("2010-11-30");
 		assertEquals(true, dutyService.hasDutyShiftedFromThisDay(dept, startDate));
 	}
 
 }
