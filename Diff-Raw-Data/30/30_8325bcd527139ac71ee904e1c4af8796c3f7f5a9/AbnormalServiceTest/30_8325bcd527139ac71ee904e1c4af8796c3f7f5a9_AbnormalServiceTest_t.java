 package gov.abrs.etms.service.abnormal;
 
 import static org.junit.Assert.*;
 import gov.abrs.etms.common.util.DateUtil;
 import gov.abrs.etms.model.abnormal.AbnEquip;
 import gov.abrs.etms.model.abnormal.AbnOperation;
 import gov.abrs.etms.model.abnormal.AbnOperationA;
 import gov.abrs.etms.model.abnormal.AbnOperationN;
 import gov.abrs.etms.model.abnormal.Abnormal;
 import gov.abrs.etms.model.abnormal.AbnormalB;
 import gov.abrs.etms.model.abnormal.AbnormalF;
 import gov.abrs.etms.model.abnormal.AbnormalO;
 import gov.abrs.etms.model.abnormal.AccdDutyTime;
 import gov.abrs.etms.model.baseinfo.Equip;
 import gov.abrs.etms.model.baseinfo.Operation;
 import gov.abrs.etms.model.para.AbnType;
 import gov.abrs.etms.model.para.TransType;
 
 import java.util.Date;
 import java.util.List;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 
 import com.google.common.collect.Lists;
 import com.taiji.common.test.DatabaseTestCase;
 
@ContextConfiguration(locations = { "/conf/spring/applicationContext-test.xml","/conf/spring/applicationContext-jms-all.xml" })
 public class AbnormalServiceTest extends DatabaseTestCase {
 	@Autowired
 	private AbnormalService abnormalService;
 
 	//测试保存故障,如果有影响业务，级联保存
 	@Test
 	public void testSaveAbnormalF() {
 		int abnC = getRowsCount(Abnormal.class);
 		int abnFc = getRowsCount(AbnormalF.class);
 		int abnOC = getRowsCount(AbnormalO.class);
 		int abnBC = getRowsCount(AbnormalB.class);
 		int abnoC = getRowsCount(AbnOperation.class);
 		int abnoAC = getRowsCount(AbnOperationA.class);
 		int abnoNC = getRowsCount(AbnOperationN.class);
 
 		AbnOperation ao1 = new AbnOperationA(new Operation(3L), new Date(), new Date(), 0);
 		AbnOperation ao2 = new AbnOperationA(new Operation(4L), new Date(), new Date(), 1);
 		AbnOperation ao3 = new AbnOperationN(new Operation(3L), new Date(), new Date(), 2);
 		//保存故障信息，级联保存两个停传业务描述和一个非停传业务描述
 		AbnormalF af = new AbnormalF(new TransType(3L), new Date(), new Date(), new AbnType(13L), "desc", "", "",
 				new Date(), "wj", Lists.newArrayList(ao1, ao2, ao3), new Equip(3L));
 		abnormalService.save(af);
 		this.flushSessionAndCloseSessionAndNewASession();
 		//验证保存结果和级联保存结果
 
 		assertEquals(getRowsCount(Abnormal.class), abnC + 1);
 		assertEquals(getRowsCount(AbnormalF.class), abnFc + 1);
 		assertEquals(getRowsCount(AbnormalO.class), abnOC);
 		assertEquals(getRowsCount(AbnormalB.class), abnBC);
 		assertEquals(getRowsCount(AbnOperation.class), abnoC + 3);
 		assertEquals(getRowsCount(AbnOperationA.class), abnoAC + 2);
 		assertEquals(getRowsCount(AbnOperationN.class), abnoNC + 1);
 
 	}
 
 	//测试更新故障，当解除影响业务详细信息关系时，删除孤立都关系对象
 	@Test
 	public void testUpdateAbnormalF() {
 		int abnC = getRowsCount(Abnormal.class);
 		int abnFc = getRowsCount(AbnormalF.class);
 		int abnOc = getRowsCount(AbnOperation.class);
 		//准备数据
 		Abnormal abnormal = abnormalService.get(1L);
 		List<AbnOperation> abnOperations = abnormal.getAbnOperations();
 		//必须反着循环
 		for (int i = 0; i < abnOperations.size(); i++) {
 			AbnOperation abnOperation = abnOperations.get(i);
 			abnormal.getAbnOperations().remove(abnOperation);
 			i = i - 1;
 
 		}
 		abnormalService.save(abnormal);
 		this.flushSessionAndCloseSessionAndNewASession();
 		assertEquals(getRowsCount(Abnormal.class), abnC);
 		assertEquals(getRowsCount(AbnormalF.class), abnFc);
 		assertEquals(getRowsCount(AbnOperation.class), abnOc - 5);
 	}
 
 	//先测试全新的异态的新增,不带影响设备
 	@Test
 	public void testAssemblyAbosNew() {
 		int abnC = getRowsCount(Abnormal.class);
 		int abneF = getRowsCount(AbnormalF.class);
 		int abneB = getRowsCount(AbnormalB.class);
 		int abnOC = getRowsCount(AbnormalO.class);
 		int abnoAC = getRowsCount(AbnOperationA.class);
 		int abnoNC = getRowsCount(AbnOperationN.class);
 
 		//准备一个新的异态信息
 		AbnormalF abnormalF = new AbnormalF();
 		abnormalF.setAbnType(new AbnType(1L));
 		abnormalF.setDesc("");
 		abnormalF.setEmpName("wj");
 		abnormalF.setProcessStep("aab");
 		abnormalF.setReason("aab");
 		abnormalF.setUpdDate(new Date());
 		abnormalF.setStartTime(new Date());
 		abnormalF.setEndTime(new Date());
 		abnormalF.setTransType(new TransType(3L));
 		abnormalF.setAbnType(new AbnType(13L));
 		//准备页面数据
 		List<Long> abnoIds = null;
 		List<String> abnoNames = null;
 		List<Date> abnoStartTimes = null;
 		List<Date> abnoEndTimes = null;
 		List<Integer> accdFlags = null;
 		abnormalService.assemblyAbos(abnormalF, abnoIds, abnoNames, abnoStartTimes, abnoEndTimes, accdFlags);
 		abnormalService.save(abnormalF);
 		this.flushSessionAndCloseSessionAndNewASession();
 
 		assertEquals(getRowsCount(Abnormal.class), abnC + 1);//异态总数加2,一个其他异态，一个其他异态引起的故障
 		assertEquals(getRowsCount(AbnormalF.class), abneF + 1);//正常故障总数不变
 		assertEquals(getRowsCount(AbnormalO.class), abnOC);//其他异态总数加1
 		assertEquals(getRowsCount(AbnormalB.class), abneB);//其他异态总数加1
 		assertEquals(getRowsCount(AbnOperationA.class), abnoAC);//异态影响设备加2
 		assertEquals(getRowsCount(AbnOperationN.class), abnoNC);//级联保存一条异态引起的故障
 
 	}
 
 	//已有的故障，原来有影响业务，现在没有
 	@Test
 	public void testAssemblyAbosUpdate() {
 		int abnC = getRowsCount(Abnormal.class);
 		int abneF = getRowsCount(AbnormalF.class);
 		int abneB = getRowsCount(AbnormalB.class);
 		int abnOC = getRowsCount(AbnormalO.class);
 		int abnoAC = getRowsCount(AbnOperationA.class);
 		int abnoNC = getRowsCount(AbnOperationN.class);
 
 		//准备一个新的异态信息
 		AbnormalF abnormalF = (AbnormalF) abnormalService.get(1L);
 		abnormalF.setAbnType(new AbnType(1L));
 		abnormalF.setDesc("");
 		abnormalF.setEmpName("wj");
 		abnormalF.setProcessStep("aab");
 		abnormalF.setReason("aab");
 		abnormalF.setUpdDate(new Date());
 		abnormalF.setStartTime(new Date());
 		abnormalF.setEndTime(new Date());
 		abnormalF.setTransType(new TransType(3L));
 		abnormalF.setAbnType(new AbnType(13L));
 		//准备页面数据
 		List<Long> abnoIds = null;
 		List<String> abnoNames = null;
 		List<Date> abnoStartTimes = null;
 		List<Date> abnoEndTimes = null;
 		List<Integer> accdFlags = null;
 		abnormalService.assemblyAbos(abnormalF, abnoIds, abnoNames, abnoStartTimes, abnoEndTimes, accdFlags);
 		abnormalService.save(abnormalF);
 		this.flushSessionAndCloseSessionAndNewASession();
 
 		assertEquals(getRowsCount(Abnormal.class), abnC);//异态总数加2,一个其他异态，一个其他异态引起的故障
 		assertEquals(getRowsCount(AbnormalF.class), abneF);//正常故障总数不变
 		assertEquals(getRowsCount(AbnormalO.class), abnOC);//其他异态总数加1
 		assertEquals(getRowsCount(AbnormalB.class), abneB);//其他异态总数加1
 		assertEquals(getRowsCount(AbnOperationA.class), abnoAC - 3);//异态影响设备加2
 		assertEquals(getRowsCount(AbnOperationN.class), abnoNC - 2);//级联保存一条异态引起的故障
 
 	}
 
 	//先测试全新的异态的新增,不带影响设备
 	@Test
 	public void testAssemblyAbosNew1() {
 		int abnC = getRowsCount(Abnormal.class);
 		int abneF = getRowsCount(AbnormalF.class);
 		int abneB = getRowsCount(AbnormalB.class);
 		int abnOC = getRowsCount(AbnormalO.class);
 		int abnoAC = getRowsCount(AbnOperationA.class);
 		int abnoNC = getRowsCount(AbnOperationN.class);
 		List<Integer> accdFlags = null;
 		testAssemblyAbosFn(accdFlags);
 		assertEquals(getRowsCount(Abnormal.class), abnC + 1);//异态总数加2,一个其他异态，一个其他异态引起的故障
 		assertEquals(getRowsCount(AbnormalF.class), abneF + 1);//正常故障总数不变
 		assertEquals(getRowsCount(AbnormalO.class), abnOC);//其他异态总数加1
 		assertEquals(getRowsCount(AbnormalB.class), abneB);//其他异态总数加1
 		assertEquals(getRowsCount(AbnOperationA.class), abnoAC);//异态影响设备加2
 		assertEquals(getRowsCount(AbnOperationN.class), abnoNC + 2);//级联保存一条异态引起的故障
 	}
 
 	//测试两个影响业务，第一个是停传
 	@Test
 	public void testAssemblyAbosNew2() {
 		int abnC = getRowsCount(Abnormal.class);
 		int abneF = getRowsCount(AbnormalF.class);
 		int abneB = getRowsCount(AbnormalB.class);
 		int abnOC = getRowsCount(AbnormalO.class);
 		int abnoAC = getRowsCount(AbnOperationA.class);
 		int abnoNC = getRowsCount(AbnOperationN.class);
 
 		List<Integer> accdFlags = Lists.newArrayList(0);
 		testAssemblyAbosFn(accdFlags);
 
 		assertEquals(getRowsCount(Abnormal.class), abnC + 1);//异态总数加2,一个其他异态，一个其他异态引起的故障
 		assertEquals(getRowsCount(AbnormalF.class), abneF + 1);//正常故障总数不变
 		assertEquals(getRowsCount(AbnormalO.class), abnOC);//其他异态总数加1
 		assertEquals(getRowsCount(AbnormalB.class), abneB);//其他异态总数加1
 		assertEquals(getRowsCount(AbnOperationA.class), abnoAC + 1);//异态影响设备加2
 		assertEquals(getRowsCount(AbnOperationN.class), abnoNC + 1);//级联保存一条异态引起的故障
 
 	}
 
 	//测试两个影响业务，第二个是停传
 	@Test
 	public void testAssemblyAbosNew3() {
 		int abnC = getRowsCount(Abnormal.class);
 		int abneF = getRowsCount(AbnormalF.class);
 		int abneB = getRowsCount(AbnormalB.class);
 		int abnOC = getRowsCount(AbnormalO.class);
 		int abnoAC = getRowsCount(AbnOperationA.class);
 		int abnoNC = getRowsCount(AbnOperationN.class);
 
 		List<Integer> accdFlags = Lists.newArrayList(1);
 		testAssemblyAbosFn(accdFlags);
 
 		assertEquals(getRowsCount(Abnormal.class), abnC + 1);//异态总数加2,一个其他异态，一个其他异态引起的故障
 		assertEquals(getRowsCount(AbnormalF.class), abneF + 1);//正常故障总数不变
 		assertEquals(getRowsCount(AbnormalO.class), abnOC);//其他异态总数加1
 		assertEquals(getRowsCount(AbnormalB.class), abneB);//其他异态总数加1
 		assertEquals(getRowsCount(AbnOperationA.class), abnoAC + 1);//异态影响设备加2
 		assertEquals(getRowsCount(AbnOperationN.class), abnoNC + 1);//级联保存一条异态引起的故障
 
 	}
 
 	//测试两个影响业务，两个都是停传
 	@Test
 	public void testAssemblyAbosNew4() {
 		int abnC = getRowsCount(Abnormal.class);
 		int abneF = getRowsCount(AbnormalF.class);
 		int abneB = getRowsCount(AbnormalB.class);
 		int abnOC = getRowsCount(AbnormalO.class);
 		int abnoAC = getRowsCount(AbnOperationA.class);
 		int abnoNC = getRowsCount(AbnOperationN.class);
 
 		List<Integer> accdFlags = Lists.newArrayList(0, 1);
 		testAssemblyAbosFn(accdFlags);
 
 		assertEquals(getRowsCount(Abnormal.class), abnC + 1);//异态总数加2,一个其他异态，一个其他异态引起的故障
 		assertEquals(getRowsCount(AbnormalF.class), abneF + 1);//正常故障总数不变
 		assertEquals(getRowsCount(AbnormalO.class), abnOC);//其他异态总数加1
 		assertEquals(getRowsCount(AbnormalB.class), abneB);//其他异态总数加1
 		assertEquals(getRowsCount(AbnOperationA.class), abnoAC + 2);//异态影响设备加2
 		assertEquals(getRowsCount(AbnOperationN.class), abnoNC);//级联保存一条异态引起的故障
 
 	}
 
 	//原先是       
 	//A    1  卫星业务1 01 02 (删除)
 	//A    2  卫星业务2 03 04 (变不是)
 	//(加一个是)   
 	//N    3  卫星业务3 05 06  (变是) 
 	//N    4  卫星业务2 07 08   （删除）
 	//A    5  卫星业务2 21 22   （更新）
 	//(加一个不是)
 	//现在是
 	//（删除）
 	//N    2  卫星业务2 03 09 (变不是)
 	//A    0  卫星业务1 10 11 (加一个是)   
 	//A    3  卫星业务3 05 12  (变是) 
 	//（删除）
 	//A    5  卫星业务1 23 24   （更新）
 	//N    0  卫星业务2 13 14   (加一个不是)
 	//先测试全新的异态的新增
 	@Test
 	public void testAssemblyAbos() {
 		int abnC = getRowsCount(Abnormal.class);
 		int abnOC = getRowsCount(AbnormalO.class);
 		int abnoB = getRowsCount(AbnormalB.class);
 		int abnoF = getRowsCount(AbnormalF.class);
 		int abnoC = getRowsCount(AbnOperation.class);
 		int abneAC = getRowsCount(AbnOperationA.class);
 		int abneNC = getRowsCount(AbnOperationN.class);
 
 		//		//准备一个新的异态信息
 		AbnormalF abnormalF = (AbnormalF) abnormalService.get(1L);
 		//准备页面数据
 		List<Integer> accdFlags = Lists.newArrayList(2, 3, 5);
 		List<Long> abnoIds = Lists.newArrayList(2L, 0L, 3L, 5L, 0L);
 		List<String> abnoNames = Lists.newArrayList("卫星业务2", "卫星业务1", "卫星业务3", "卫星业务1", "卫星业务2");
 		List<Date> abnoStartTimes = Lists.newArrayList(DateUtil.createDateTime("2010-11-18 00:00:03"),
 				DateUtil.createDateTime("2010-11-18 00:00:10"), DateUtil.createDateTime("2010-11-18 00:00:05"),
 				DateUtil.createDateTime("2010-11-18 00:00:23"), DateUtil.createDateTime("2010-11-18 00:00:13"));
 		List<Date> abnoEndTimes = Lists.newArrayList(DateUtil.createDateTime("2010-11-18 00:00:09"),
 				DateUtil.createDateTime("2010-11-18 00:00:11"), DateUtil.createDateTime("2010-11-18 00:00:12"),
 				DateUtil.createDateTime("2010-11-18 00:00:24"), DateUtil.createDateTime("2010-11-18 00:00:14"));
 
 		abnormalService.assemblyAbos(abnormalF, abnoIds, abnoNames, abnoStartTimes, abnoEndTimes, accdFlags);
 		abnormalService.save(abnormalF);
 		this.flushSessionAndCloseSessionAndNewASession();
 		assertEquals(getRowsCount(Abnormal.class), abnC);//不变
 		assertEquals(getRowsCount(AbnormalO.class), abnOC);//不变
 		assertEquals(getRowsCount(AbnormalF.class), abnoF);//不变
 		assertEquals(getRowsCount(AbnormalB.class), abnoB);//不变
 		assertEquals(getRowsCount(AbnOperation.class), abnoC);//删了两个，加了两个
 		assertEquals(getRowsCount(AbnOperationA.class), abneAC);//删了1个，加了1个
 		assertEquals(getRowsCount(AbnOperationN.class), abneNC);//删了1个，加了1个
 		AbnormalF result = (AbnormalF) abnormalService.get(1L);
 		//判断结果是否与预期相同
 		List<AbnOperation> abnos = result.getAbnOperations();
 		assertEquals(abnos.size(), 5);
 		//N    2(改状态新增一条变为6)  卫星业务2 03 09 (变不是)
 		AbnOperation abno1 = abnos.get(0);
 		assertTrue(abno1 instanceof AbnOperationN);
 		assertEquals(abno1.getOperation().getName(), "卫星业务2");
 		assertEquals(DateUtil.dateToString(abno1.getStartTime(), DateUtil.FORMAT_DAYTIME), "2010-11-18 00:00:03");
 		assertEquals(DateUtil.dateToString(abno1.getEndTime(), DateUtil.FORMAT_DAYTIME), "2010-11-18 00:00:09");
 		//A    0(改状态新增一条变为7)   卫星业务1 10 11 (加一个是)   
 		AbnOperation abno2 = abnos.get(1);
 		assertTrue(abno2 instanceof AbnOperationA);
 		assertEquals(abno2.getOperation().getName(), "卫星业务1");
 		assertEquals(DateUtil.dateToString(abno2.getStartTime(), DateUtil.FORMAT_DAYTIME), "2010-11-18 00:00:10");
 		assertEquals(DateUtil.dateToString(abno2.getEndTime(), DateUtil.FORMAT_DAYTIME), "2010-11-18 00:00:11");
 		//A    3(改状态新增一条变为8)  卫星业务3 05 12  (变是) 
 		AbnOperation abno3 = abnos.get(2);
 		assertTrue(abno3 instanceof AbnOperationA);
 		assertEquals(abno3.getOperation().getName(), "卫星业务3");
 		assertEquals(DateUtil.dateToString(abno3.getStartTime(), DateUtil.FORMAT_DAYTIME), "2010-11-18 00:00:05");
 		assertEquals(DateUtil.dateToString(abno3.getEndTime(), DateUtil.FORMAT_DAYTIME), "2010-11-18 00:00:12");
 		//A    5  卫星业务1 23 24   （更新）
 		AbnOperation abno4 = abnos.get(3);
 		assertTrue(abno4 instanceof AbnOperationA);
 		assertEquals(abno4.getId().intValue(), 5);
 		assertEquals(abno4.getOperation().getName(), "卫星业务1");
 		assertEquals(DateUtil.dateToString(abno4.getStartTime(), DateUtil.FORMAT_DAYTIME), "2010-11-18 00:00:23");
 		assertEquals(DateUtil.dateToString(abno4.getEndTime(), DateUtil.FORMAT_DAYTIME), "2010-11-18 00:00:24");
 		//		N    0(改状态新增一条变为9)  卫星业务2 13 14   (加一个不是)
 		AbnOperation abno5 = abnos.get(4);
 		assertTrue(abno5 instanceof AbnOperationN);
 		assertEquals(abno5.getOperation().getName(), "卫星业务2");
 		assertEquals(DateUtil.dateToString(abno5.getStartTime(), DateUtil.FORMAT_DAYTIME), "2010-11-18 00:00:13");
 		assertEquals(DateUtil.dateToString(abno5.getEndTime(), DateUtil.FORMAT_DAYTIME), "2010-11-18 00:00:14");
 	}
 
 	private void testAssemblyAbosFn(List<Integer> accdFlags) {
 		//准备一个新的异态信息
 		AbnormalF abnormalF = new AbnormalF();
 		abnormalF.setAbnType(new AbnType(1L));
 		abnormalF.setDesc("");
 		abnormalF.setEmpName("wj");
 		abnormalF.setProcessStep("aab");
 		abnormalF.setReason("aab");
 		abnormalF.setUpdDate(new Date());
 		abnormalF.setStartTime(new Date());
 		abnormalF.setEndTime(new Date());
 		abnormalF.setTransType(new TransType(3L));
 		abnormalF.setAbnType(new AbnType(13L));
 		//准备页面数据
 		List<Long> abnoIds = Lists.newArrayList(0L, 0L);
 		List<String> abnoNames = Lists.newArrayList("卫星业务1", "卫星业务2");
 		List<Date> abnoStartTimes = Lists.newArrayList(DateUtil.createDateTime("2010-11-22 14:55:04"),
 				DateUtil.createDateTime("2010-11-22 14:55:06"));
 		List<Date> abnoEndTimes = Lists.newArrayList(DateUtil.createDateTime("2010-11-22 14:55:05"),
 				DateUtil.createDateTime("2010-11-22 14:55:08"));
 		abnormalService.assemblyAbos(abnormalF, abnoIds, abnoNames, abnoStartTimes, abnoEndTimes, accdFlags);
 		abnormalService.save(abnormalF);
 		this.flushSessionAndCloseSessionAndNewASession();
 	}
 
 	//先测试全新的异态的新增,不带影响设备
 	@Test
 	public void testAssemblyAbesNew() {
 		int abnC = getRowsCount(Abnormal.class);
 		int abnOC = getRowsCount(AbnormalO.class);
 		int abneC = getRowsCount(AbnEquip.class);
 		int abneB = getRowsCount(AbnormalB.class);
 		int abneF = getRowsCount(AbnormalF.class);
 		//准备一个新的异态信息
 		AbnormalO abnormalO = new AbnormalO();
 		abnormalO.setAbnType(new AbnType(1L));
 		abnormalO.setDesc("");
 		abnormalO.setEmpName("wj");
 		abnormalO.setProcessStep("aab");
 		abnormalO.setReason("aab");
 		abnormalO.setUpdDate(new Date());
 		abnormalO.setStartTime(new Date());
 		abnormalO.setEndTime(new Date());
 		abnormalO.setTransType(new TransType(3L));
 		abnormalO.setAbnType(new AbnType(13L));
 		//准备页面数据
 		List<Long> abneIds = null;
 		List<String> abneNames = null;
 		List<Date> abneStartTimes = null;
 		List<Date> abneEndTimes = null;
 		List<Integer> faultFlags = null;
 		abnormalService.assemblyAbes(abnormalO, abneIds, abneNames, abneStartTimes, abneEndTimes, faultFlags);
 		abnormalService.save(abnormalO);
 		this.flushSessionAndCloseSessionAndNewASession();
 		assertEquals(getRowsCount(Abnormal.class), abnC + 1);//异态总数加2,一个其他异态，一个其他异态引起的故障
 		assertEquals(getRowsCount(AbnormalO.class), abnOC + 1);//其他异态总数加1
 		assertEquals(getRowsCount(AbnEquip.class), abneC);//异态影响设备加2
 		assertEquals(getRowsCount(AbnormalB.class), abneB);//级联保存一条异态引起的故障
 		assertEquals(getRowsCount(AbnormalF.class), abneF);//正常故障总数不变
 	}
 
 	//先已有都异态，原来有影响设备，现在没有
 	@Test
 	public void testAssemblyAbesUpdate() {
 		int abnC = getRowsCount(Abnormal.class);
 		int abnOC = getRowsCount(AbnormalO.class);
 		int abneC = getRowsCount(AbnEquip.class);
 		int abneB = getRowsCount(AbnormalB.class);
 		int abneF = getRowsCount(AbnormalF.class);
 		//准备一个新的异态信息
 		AbnormalO abnormalO = (AbnormalO) abnormalService.get(2L);
 		abnormalO.setAbnType(new AbnType(1L));
 		abnormalO.setDesc("");
 		abnormalO.setEmpName("wj");
 		abnormalO.setProcessStep("aab");
 		abnormalO.setReason("aab");
 		abnormalO.setUpdDate(new Date());
 		abnormalO.setStartTime(new Date());
 		abnormalO.setEndTime(new Date());
 		abnormalO.setTransType(new TransType(3L));
 		abnormalO.setAbnType(new AbnType(13L));
 		//准备页面数据
 		List<Long> abneIds = null;
 		List<String> abneNames = null;
 		List<Date> abneStartTimes = null;
 		List<Date> abneEndTimes = null;
 		List<Integer> faultFlags = null;
 		abnormalService.assemblyAbes(abnormalO, abneIds, abneNames, abneStartTimes, abneEndTimes, faultFlags);
 		abnormalService.save(abnormalO);
 		this.flushSessionAndCloseSessionAndNewASession();
 		assertEquals(getRowsCount(Abnormal.class), abnC - 3);//异态总数加2,一个其他异态，一个其他异态引起的故障
 		assertEquals(getRowsCount(AbnormalO.class), abnOC);//其他异态总数加1
 		assertEquals(getRowsCount(AbnEquip.class), abneC - 4);//异态影响设备加2
 		assertEquals(getRowsCount(AbnormalB.class), abneB - 3);//级联保存一条异态引起的故障
 		assertEquals(getRowsCount(AbnormalF.class), abneF);//正常故障总数不变
 	}
 
 	//先测试全新的异态的新增,带影响设备
 	@Test
 	public void testAssemblyAbesNew1() {
 		int abnC = getRowsCount(Abnormal.class);
 		int abnOC = getRowsCount(AbnormalO.class);
 		int abneC = getRowsCount(AbnEquip.class);
 		int abneB = getRowsCount(AbnormalB.class);
 		int abneF = getRowsCount(AbnormalF.class);
 		//准备一个新的异态信息
 		AbnormalO abnormalO = new AbnormalO();
 		abnormalO.setAbnType(new AbnType(1L));
 		abnormalO.setDesc("");
 		abnormalO.setEmpName("wj");
 		abnormalO.setProcessStep("aab");
 		abnormalO.setReason("aab");
 		abnormalO.setUpdDate(new Date());
 		abnormalO.setStartTime(new Date());
 		abnormalO.setEndTime(new Date());
 		abnormalO.setTransType(new TransType(3L));
 		abnormalO.setAbnType(new AbnType(13L));
 		//准备页面数据
 		List<Long> abneIds = Lists.newArrayList(0L, 0L);
 		List<String> abneNames = Lists.newArrayList("上变频器", "调制器");
 		List<Date> abneStartTimes = Lists.newArrayList(DateUtil.createDateTime("2010-11-22 14:55:04"),
 				DateUtil.createDateTime("2010-11-22 14:55:06"));
 		List<Date> abneEndTimes = Lists.newArrayList(DateUtil.createDateTime("2010-11-22 14:55:05"),
 				DateUtil.createDateTime("2010-11-22 14:55:08"));
 		List<Integer> faultFlags = Lists.newArrayList(1);
 		abnormalService.assemblyAbes(abnormalO, abneIds, abneNames, abneStartTimes, abneEndTimes, faultFlags);
 		abnormalService.save(abnormalO);
 		this.flushSessionAndCloseSessionAndNewASession();
 		assertEquals(getRowsCount(Abnormal.class), abnC + 2);//异态总数加2,一个其他异态，一个其他异态引起的故障
 		assertEquals(getRowsCount(AbnormalO.class), abnOC + 1);//其他异态总数加1
 		assertEquals(getRowsCount(AbnEquip.class), abneC + 2);//异态影响设备加2
 		assertEquals(getRowsCount(AbnormalB.class), abneB + 1);//级联保存一条异态引起的故障
 		assertEquals(getRowsCount(AbnormalF.class), abneF);//正常故障总数不变
 	}
 
 	//原先是       
 	//N    1   上变频器 01 02 (非故障变故障)
 	//Y    2   上变频器 03 04 (删除)
 	//Y    3   调制器 05 06   (故障变非故障,更新时间)
 	//Y    4   调制器 07 08    (故障变非故障,更新时间)
 	//(新增)
 	//现在是
 	//Y    1   上变频器 01 12
 	//(删除)
 	//N    3   调制器 05 11
 	//Y    0   上变频器  09 10 (新增)
 	//Y    4   调制器  13 08
 	//先测试全新的异态的新增
 	@Test
 	public void testAssemblyAbes() {
 		int abnC = getRowsCount(Abnormal.class);
 		int abnOC = getRowsCount(AbnormalO.class);
 		int abneC = getRowsCount(AbnEquip.class);
 		int abneB = getRowsCount(AbnormalB.class);
 		int abneF = getRowsCount(AbnormalF.class);
 		//		//准备一个新的异态信息
 		AbnormalO abnormalO = (AbnormalO) abnormalService.get(2L);
 		//准备页面数据
 		List<Integer> faultFlags = Lists.newArrayList(0, 3, 4);
 		List<Long> abneIds = Lists.newArrayList(1L, 3L, 0L, 4L);
 		List<String> abneNames = Lists.newArrayList("上变频器", "调制器", "上变频器", "调制器");
 		List<Date> abneStartTimes = Lists.newArrayList(DateUtil.createDateTime("2010-11-18 00:00:01"),
 				DateUtil.createDateTime("2010-11-18 00:00:05"), DateUtil.createDateTime("2010-11-18 00:00:09"),
 				DateUtil.createDateTime("2010-11-18 00:00:13"));
 		List<Date> abneEndTimes = Lists.newArrayList(DateUtil.createDateTime("2010-11-22 14:55:12"),
 				DateUtil.createDateTime("2010-11-22 14:55:11"), DateUtil.createDateTime("2010-11-22 14:55:10"),
 				DateUtil.createDateTime("2010-11-22 14:55:08"));
 
 		abnormalService.assemblyAbes(abnormalO, abneIds, abneNames, abneStartTimes, abneEndTimes, faultFlags);
 		abnormalService.save(abnormalO);
 		this.flushSessionAndCloseSessionAndNewASession();
 		assertEquals(getRowsCount(Abnormal.class), abnC);//一个转为故障+1,两个转为非故障-2,一个故障删除-1,新增一个故障+1
 		assertEquals(getRowsCount(AbnormalO.class), abnOC);//其他异态总数
 		assertEquals(getRowsCount(AbnormalF.class), abneF);//正常故障总数不变
 		assertEquals(getRowsCount(AbnEquip.class), abneC);//删了一个，加了一个
 		assertEquals(getRowsCount(AbnormalB.class), abneB);//原来是3个，现在是2个(解除关系时删除由异态引起的故障)
 		AbnormalO result = (AbnormalO) abnormalService.get(2L);
 		//判断结果是否与预期相同
 		List<AbnEquip> abnes = result.getAbnEquips();
 		assertEquals(abnes.size(), 4);
 		AbnEquip abne1 = abnes.get(0);
 		assertEquals(abne1.getId().intValue(), 1);
 		assertEquals(abne1.getEquip().getName(), "上变频器");
 		assertEquals(DateUtil.dateToString(abne1.getStartTime(), DateUtil.FORMAT_DAYTIME), "2010-11-18 00:00:01");
 		assertEquals(DateUtil.dateToString(abne1.getEndTime(), DateUtil.FORMAT_DAYTIME), "2010-11-22 14:55:12");
 		assertNotNull(abne1.getAbnormalB());
 		AbnEquip abne2 = abnes.get(1);
 		assertEquals(abne2.getId().intValue(), 3);
 		assertEquals(abne2.getEquip().getName(), "调制器");
 		assertEquals(DateUtil.dateToString(abne2.getStartTime(), DateUtil.FORMAT_DAYTIME), "2010-11-18 00:00:05");
 		assertEquals(DateUtil.dateToString(abne2.getEndTime(), DateUtil.FORMAT_DAYTIME), "2010-11-22 14:55:11");
 		assertNull(abne2.getAbnormalB());
 		AbnEquip abne3 = abnes.get(2);
 		assertEquals(abne3.getEquip().getName(), "上变频器");
 		assertEquals(DateUtil.dateToString(abne3.getStartTime(), DateUtil.FORMAT_DAYTIME), "2010-11-18 00:00:09");
 		assertEquals(DateUtil.dateToString(abne3.getEndTime(), DateUtil.FORMAT_DAYTIME), "2010-11-22 14:55:10");
 		assertNotNull(abne3.getAbnormalB());
 		AbnEquip abne4 = abnes.get(3);
 		assertEquals(abne4.getId().intValue(), 4);
 		assertEquals(abne4.getEquip().getName(), "调制器");
 		assertEquals(DateUtil.dateToString(abne4.getStartTime(), DateUtil.FORMAT_DAYTIME), "2010-11-18 00:00:13");
 		assertEquals(DateUtil.dateToString(abne4.getEndTime(), DateUtil.FORMAT_DAYTIME), "2010-11-22 14:55:08");
 		assertNotNull(abne4.getAbnormalB());
 		assertEquals(DateUtil.dateToString(abne4.getAbnormalB().getStartTime(), DateUtil.FORMAT_DAYTIME),
 				"2010-11-18 00:00:13");
 		assertEquals(DateUtil.dateToString(abne4.getAbnormalB().getEndTime(), DateUtil.FORMAT_DAYTIME),
 				"2010-11-22 14:55:08");
 
 	}
 
 	//测试删除定性信息
 	@Test
 	public void assemblyAbts() {
 		int adtCount = getRowsCount(AccdDutyTime.class);
 		Abnormal abnormal = abnormalService.get(1L);
 		abnormalService.assemblyAbts(abnormal, null, null, null, null);
 		abnormalService.save(abnormal);
 		this.flushSessionAndCloseSessionAndNewASession();
 		assertEquals(getRowsCount(AccdDutyTime.class), adtCount - 3);
 	}
 
 	//测试新增编辑删除定性信息
 	//原先是2异态有6789四个影响业务信息，其中689为事故类型，对应的定性信息为
 	//abnOperationIds, dutyTimeIds, accdDutys,dutyTimes
 	//6 4 人为责任 10（删除）
 	//6 5 人为责任 20（改）
 	//加一个6的
 	//8 6 人为责任 30（改）
 	//8 7 人为责任 40（删）
 	//8 8 人为责任 50
 	//加一个8的
 	//加两个9的
 	//
 	//现在是
 	//（删除）
 	//6 5 外电        60（改）
 	//6 0 人为责任 70（加）
 	//8 6 人为责任 80（改）
 	//（删除）
 	//8 8 人为责任 50
 	//8 0 人为责任 90（加）
 	//9 0 外电        100（加）
 	//9 0 人为责任 110（加）
 	@Test
 	public void assemblyAbtsNew() {
 		int abnOperationCount = getRowsCount(AbnOperation.class);
 		int accdDutyTimeCount = getRowsCount(AccdDutyTime.class);
 		List<Long> abnOperationIds = Lists.newArrayList(6L, 6L, 8L, 8L, 8L, 9L, 9L);
 		List<Long> dutyTimeIds = Lists.newArrayList(5L, 0L, 6L, 8L, 0L, 0L, 0L);
 		List<String> accdDutys = Lists.newArrayList("16", "14", "14", "14", "14", "16", "14");
 		List<String> dutyTimes = Lists.newArrayList("00:01:00", "00:01:10", "00:01:20", "00:00:50", "00:01:30",
 				"00:01:40", "00:01:50");
 		Abnormal abnormal = abnormalService.get(2L);
 		abnormalService.assemblyAbts(abnormal, abnOperationIds, dutyTimeIds, accdDutys, dutyTimes);
 		abnormalService.save(abnormal);
 		this.flushSessionAndCloseSessionAndNewASession();
 		assertEquals(getRowsCount(AbnOperation.class), abnOperationCount);//不影响
 		assertEquals(getRowsCount(AccdDutyTime.class), accdDutyTimeCount + 2);//删两个加四个
 		Abnormal result = abnormalService.get(2L);
 		assertEquals(result.getAbnOperations().size(), 4);
 		//验证6都业务的定性是否正确
 		//（删除）
 		//6 5 外电        60（改）
 		//6 0 人为责任 70（加）
 		AbnOperation ao6 = result.getAbnOperations().get(0);
 		assertTrue(ao6 instanceof AbnOperationA);
 		assertEquals(((AbnOperationA) ao6).getAccdDutyTimes().size(), 2);
 		assertEquals(((AbnOperationA) ao6).getAccdDutyTimes().get(0).getId().intValue(), 5);
 		assertEquals(((AbnOperationA) ao6).getAccdDutyTimes().get(0).getAccdDuty().getId().intValue(), 16);
 		assertEquals(((AbnOperationA) ao6).getAccdDutyTimes().get(0).getDutyTime().intValue(), 60);
 		assertNotNull(((AbnOperationA) ao6).getAccdDutyTimes().get(1).getId());
 		assertEquals(((AbnOperationA) ao6).getAccdDutyTimes().get(1).getAccdDuty().getId().intValue(), 14);
 		assertEquals(((AbnOperationA) ao6).getAccdDutyTimes().get(1).getDutyTime().intValue(), 70);
 		//验证7
 		AbnOperation ao7 = result.getAbnOperations().get(1);
 		assertTrue(ao7 instanceof AbnOperationN);
 		//验证8
 		//8 6 人为责任 80（改）
 		//（删除）
 		//8 8 人为责任 50
 		//8 0 人为责任 90（加）
 		AbnOperation ao8 = result.getAbnOperations().get(2);
 		assertTrue(ao8 instanceof AbnOperationA);
 		assertEquals(((AbnOperationA) ao8).getAccdDutyTimes().size(), 3);
 		assertEquals(((AbnOperationA) ao8).getAccdDutyTimes().get(0).getId().intValue(), 6);
 		assertEquals(((AbnOperationA) ao8).getAccdDutyTimes().get(0).getAccdDuty().getId().intValue(), 14);
 		assertEquals(((AbnOperationA) ao8).getAccdDutyTimes().get(0).getDutyTime().intValue(), 80);
 		assertEquals(((AbnOperationA) ao8).getAccdDutyTimes().get(1).getId().intValue(), 8);
 		assertEquals(((AbnOperationA) ao8).getAccdDutyTimes().get(1).getAccdDuty().getId().intValue(), 14);
 		assertEquals(((AbnOperationA) ao8).getAccdDutyTimes().get(1).getDutyTime().intValue(), 50);
 		assertNotNull(((AbnOperationA) ao8).getAccdDutyTimes().get(2).getId().intValue());
 		assertEquals(((AbnOperationA) ao8).getAccdDutyTimes().get(2).getAccdDuty().getId().intValue(), 14);
 		assertEquals(((AbnOperationA) ao8).getAccdDutyTimes().get(2).getDutyTime().intValue(), 90);
 		//验证9
 		//9 0 外电        100（加）
 		//9 0 人为责任 110（加）
 		AbnOperation ao9 = result.getAbnOperations().get(3);
 		assertEquals(((AbnOperationA) ao9).getAccdDutyTimes().size(), 2);
 		assertNotNull(((AbnOperationA) ao9).getAccdDutyTimes().get(0).getId().intValue());
 		assertEquals(((AbnOperationA) ao9).getAccdDutyTimes().get(0).getAccdDuty().getId().intValue(), 16);
 		assertEquals(((AbnOperationA) ao9).getAccdDutyTimes().get(0).getDutyTime().intValue(), 100);
 		assertNotNull(((AbnOperationA) ao9).getAccdDutyTimes().get(1).getId().intValue());
 		assertEquals(((AbnOperationA) ao9).getAccdDutyTimes().get(1).getAccdDuty().getId().intValue(), 14);
 		assertEquals(((AbnOperationA) ao9).getAccdDutyTimes().get(1).getDutyTime().intValue(), 110);
 	}
 
 	@Test
 	public void testGet() {
 		Date startTime = DateUtil.createDate("2010-11-01");
 		Date endTime = DateUtil.createDate("2010-11-30");
 		List<Abnormal> list = abnormalService.get(startTime, endTime);
 		assertEquals(1, list.size());
 		assertEquals(new Long(6), list.get(0).getId());
 	}
 }
