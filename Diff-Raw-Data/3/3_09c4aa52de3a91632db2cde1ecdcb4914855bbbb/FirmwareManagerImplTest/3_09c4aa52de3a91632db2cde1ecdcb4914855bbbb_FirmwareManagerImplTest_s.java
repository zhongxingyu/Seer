 package com.chinarewards.qqgbvpn.main.logic.firmware.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 
 import org.apache.commons.configuration.BaseConfiguration;
 import org.apache.commons.configuration.Configuration;
 import org.apache.mina.core.buffer.IoBuffer;
 import org.junit.Test;
 
import com.chinarewards.qqgbpvn.core.test.JpaGuiceTest;
 import com.chinarewards.qqgbpvn.main.CommonTestConfigModule;
 import com.chinarewards.qqgbpvn.main.TestConfigModule;
 import com.chinarewards.qqgbvpn.common.Tools;
 import com.chinarewards.qqgbvpn.core.jpa.JpaPersistModuleBuilder;
 import com.chinarewards.qqgbvpn.domain.Pos;
 import com.chinarewards.qqgbvpn.domain.status.PosDeliveryStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosInitializationStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosOperationStatus;
 import com.chinarewards.qqgbvpn.main.dao.qqapi.PosDao;
 import com.chinarewards.qqgbvpn.main.guice.AppModule;
 import com.chinarewards.qqgbvpn.main.logic.firmware.FirmwareManager;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.CmdConstant;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.FirmwareUpDoneRequestMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.FirmwareUpDoneResponseMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.firmware.FirmwareUpDoneResult;
 import com.chinarewards.qqgbvpn.main.protocol.socket.ProtocolLengths;
 import com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec.FirmwareUpDoneRequestCodec;
 import com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec.FirmwareUpDoneResponseCodec;
 import com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec.ICommandCodec;
 import com.google.inject.Module;
 import com.google.inject.persist.jpa.JpaPersistModule;
 
 
 /**
  * test firmwareManager
  */
 public class FirmwareManagerImplTest extends JpaGuiceTest {
 	
 	PosDao posDao;
 	
 	private FirmwareManager getManager() {
 		return getInjector().getInstance(FirmwareManager.class);
 	}
 
 	@Override
 	protected Module[] getModules() {
 		
 		CommonTestConfigModule confModule = new CommonTestConfigModule();
 		Configuration configuration = confModule.getConfiguration();
 
 		JpaPersistModule jpaModule = new JpaPersistModule("posnet");
 		JpaPersistModuleBuilder builder = new JpaPersistModuleBuilder();
 		builder.configModule(jpaModule,  configuration, "db");
 		
 		return new Module[] {
 				new AppModule(), jpaModule, confModule };
 	}
 	
 	protected Module buildTestConfigModule() {
 
 		Configuration conf = new BaseConfiguration();
 		// hard-coded config
 		conf.setProperty("server.port", 0);
 		// persistence
 		conf.setProperty("db.user", "sa");
 		conf.setProperty("db.password", "");
 		conf.setProperty("db.driver", "org.hsqldb.jdbcDriver");
 		conf.setProperty("db.url", "jdbc:hsqldb:.");
 		// additional Hibernate properties
 		conf.setProperty("db.hibernate.dialect",
 				"org.hibernate.dialect.HSQLDialect");
 		conf.setProperty("db.hibernate.show_sql", true);
 		// URL for QQ
 		conf.setProperty("qq.groupbuy.url.groupBuyingSearchGroupon",
 				"http://localhost:8086/qqapi");
 		conf.setProperty("qq.groupbuy.url.groupBuyingValidationUrl",
 				"http://localhost:8086/qqapi");
 		conf.setProperty("qq.groupbuy.url.groupBuyingUnbindPosUrl",
 				"http://localhost:8086/qqapi");
 
 		TestConfigModule confModule = new TestConfigModule(conf);
 		return confModule;
 	}
 	
 	@Test
 	public void testFirmWareUpDone() throws IOException {
 		posDao = getInjector().getInstance(PosDao.class);
 
 		// prepared data
 		Pos pos = new Pos();
 		pos.setPosId("pos-0001");
 		pos.setDstatus(PosDeliveryStatus.DELIVERED);
 		pos.setIstatus(PosInitializationStatus.INITED);
 		pos.setOstatus(PosOperationStatus.ALLOWED);
 		pos.setUpgradeRequired(true);
 		getEm().persist(pos);
 		getEm().flush();
 
 		FirmwareUpDoneRequestMessage request = new FirmwareUpDoneRequestMessage();
 		request.setPosId("pos-0001");
 		request.setCmdId(CmdConstant.FIRMWARE_UP_DONE_CMD_ID);
 		
 		FirmwareUpDoneResponseMessage response = getManager().upDoneRequest(request);
 		
 		assertEquals(response.getResult(), FirmwareUpDoneResult.SUCCESS.getPosCode());
 		
 		Pos record = getEm().find(Pos.class, pos.getId());
 		assertNotNull(record);
 		assertFalse(record.getUpgradeRequired());
 	}
 	
 	@Test
 	public void testFirmwareUpDoneRequestCodec() throws Exception{
 		//charset
 		Charset charset = Charset.forName("gbk");
 		
 		final long cmdId = CmdConstant.FIRMWARE_UP_DONE_CMD_ID;
 		final byte[] posId = new String("pos-0001a123").getBytes(charset);
 			
 		// prepare buffer
 		IoBuffer buf = IoBuffer.allocate(ProtocolLengths.COMMAND
 				+ ProtocolLengths.POS_ID);
 		
 		buf.putUnsignedInt(cmdId);// encode data   // command ID
 		buf.put(posId);	// result
 		
 		buf.position(0);
 		ICommandCodec iCommandCodec = new FirmwareUpDoneRequestCodec();
 		FirmwareUpDoneRequestMessage message = (FirmwareUpDoneRequestMessage) iCommandCodec.decode(buf, charset);
 		
 		assertEquals(message.getCmdId(), cmdId);
 		assertEquals(message.getPosId(), Tools.byteToString(posId, charset));
 	}
 	
 	@Test
 	public void testFirmwareUpDoneResponseCodec(){
 		//charset
 		Charset charset = Charset.forName("gbk");
 		final long cmdId = CmdConstant.FIRMWARE_UP_DONE_CMD_ID_RESPONSE;
 		final short result = FirmwareUpDoneResult.SUCCESS.getPosCode();
 		
 		ICommandCodec iCommandCodec = new FirmwareUpDoneResponseCodec();
 		FirmwareUpDoneResponseMessage msg = new FirmwareUpDoneResponseMessage();
 		msg.setCmdId(cmdId);
 		msg.setResult(result);
 		byte[] b = iCommandCodec.encode(msg, charset);
 		
 		assertEquals(b.length, ProtocolLengths.COMMAND + ProtocolLengths.RESULT);
 		
 		byte[] codIdBytes = new byte[ProtocolLengths.COMMAND];
 		System.arraycopy(b, 0, codIdBytes, 0, ProtocolLengths.COMMAND);
 		long tempCmdId = Tools.byteToUnsignedInt(codIdBytes);
 		
 		byte[] resultBytes = new byte[ProtocolLengths.RESULT];
 		System.arraycopy(b, ProtocolLengths.COMMAND, resultBytes, 0, ProtocolLengths.RESULT);
 		int tempResult = Tools.byteToUnsignedShort(resultBytes);
 		
 		assertEquals(cmdId, tempCmdId);
 		
 		assertEquals(result, (short)tempResult);
 	}
 	
 }
