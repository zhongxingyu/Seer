 package com.chinarewards.qqgbpvn.main.server;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.util.Arrays;
 
 import org.junit.Test;
 
 import com.chinarewards.qqgbpvn.main.test.JpaGuiceTest;
 import com.chinarewards.qqgbvpn.common.Tools;
 import com.chinarewards.qqgbvpn.config.DatabaseProperties;
 import com.chinarewards.qqgbvpn.domain.Pos;
 import com.chinarewards.qqgbvpn.domain.status.PosDeliveryStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosInitializationStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosOperationStatus;
 import com.chinarewards.qqgbvpn.main.dao.qqapi.PosDao;
 import com.chinarewards.qqgbvpn.main.guice.AppModule;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.CmdConstant;
 import com.chinarewards.qqgbvpn.main.util.HMAC_MD5;
 import com.google.inject.Module;
 import com.google.inject.persist.jpa.JpaPersistModule;
 
 public class MinaProtcolTest extends JpaGuiceTest {
 
 	PosDao posDao;
 
 	@Override
 	protected Module[] getModules() {
 		return new Module[] {
 				new AppModule(),
 				new JpaPersistModule("posnet")
 						.properties(new DatabaseProperties().getProperties()) };
 	}
 	
	//@Test
 	public void testSendViaJavaSocket() throws Exception {
 		
 
 		// prepared data
 //		Pos pos = new Pos();
 //		pos.setPosId("POS-56789012");
 //		pos.setDstatus(PosDeliveryStatus.DELIVERED);
 //		pos.setSecret("012345");
 //		pos.setIstatus(PosInitializationStatus.INITED);
 //		pos.setOstatus(PosOperationStatus.ALLOWED);
 //		getEm().persist(pos);
 //		getEm().flush();
 //		
 //
 		Socket socket = new Socket("localhost", 1234);
 
 		OutputStream os = socket.getOutputStream();
 
 		byte[] initMsg = new byte[] {
 				// SEQ
 				0, 0, 0, 24,
 				// ACK
 				0, 0, 0, 0,
 				// flags
 				0, 0, 
 				// checksum
 				0, 0,
 				// message length
 				0, 0, 0, 32,
 				// command ID
 				0, 0, 0, 5,
 				// POS ID
 				'P', 'O', 'S', '-', '5', '6', '7', '8', '9',
 				'0', '1', '2' };
 		int num = Tools.checkSum(initMsg, initMsg.length);
 		Tools.putUnsignedShort(initMsg, num, 10);
 		System.out.println("Packet size: " + initMsg.length);
 		os.write(initMsg);
 		os.flush();
 		
 		System.out.println("Going to read data");
 		InputStream in = socket.getInputStream();
 		byte[] result = new byte[30];
 		in.read(result);
 		
 		
 		in.close();
 		os.close();
 
 		
 		System.out.println("result=========:"+Arrays.toString(result));
 //		for(byte b:result){
 //			System.out.println("b=====:"+b);
 //		}
 //		byte[] result = new byte[]{1,2,3,4,5,6,7,8};
 //		
 //		
 //		byte[] challengeResponse  = HMAC_MD5.getSecretContent(result, "012345");
 //		
 //		
 //		byte[] loginMsg = new byte[] {
 //				//head start
 //				// SEQ
 //				0, 0, 0, 24,
 //				// ACK
 //				0, 0, 0, 0,
 //				// flags
 //				0, 0, 
 //				// checksum
 //				0, 2,
 //				// message length
 //				0, 0, 0, 48,
 //				//head end
 //				// command ID
 //				0, 0, 0, 7,
 //				// POS ID
 //				'P', 'O', 'S', '-', '5', '6', '7', '8', '9',
 //				'0', '1', '2' 
 //				//challengeResponse
 //				,0,0,0,0,0,0,0,0
 //				,0,0,0,0,0,0,0,0
 //		};
 //		Tools.putBytes(loginMsg, challengeResponse, 32);
 //		System.out.println("Packet size: " + loginMsg.length);
 //		Socket socket = new Socket("localhost", 1234);
 //		OutputStream os = socket.getOutputStream();
 //
 //		os = socket.getOutputStream();
 //		os.write(loginMsg);
 //		os.flush();
 //		socket.close();
 
 //		OutputStream os = socket.getOutputStream();
 		
 //		CMD ID  团购验证请求  見上  見上  Yes  必需 = 3  3  
 //		grouponId  团购编号  \0作結尾的字符串  Yes   456789  
 //		grouponVCode  团购验证码  \0作結尾的字符串  Yes  一般為16字, 但不要限制  4567890  
  
 //				byte[] validateMsg = new byte[] {
 //						// SEQ
 //						0, 0, 0, 24,
 //						// ACK
 //						0, 0, 0, 0,
 //						// flags
 //						0, 0, 
 //						// checksum
 //						0, 2,
 //						// message length
 //						0, 0, 0, 36,
 //						// command ID
 //						0, 0, 0, 3,
 //						// grouponId 
 //						'g', 'r', 'o', 'u', 'p', '-', '1', '\0', 
 //						//grouponVCode 
 //						'v','c', '0', 'd','e','-','1','\0' };
 //				System.out.println("Packet size: " + validateMsg.length);
 //				os.write(validateMsg);
 //				os.flush();
 //				os.close();
 		
 //		OutputStream os = socket.getOutputStream();
 		
 //		CMD ID  团购验证请求  見上  見上  Yes  必需 = 3  3  
 //		grouponId  团购编号  \0作結尾的字符串  Yes   456789  
 //		grouponVCode  团购验证码  \0作結尾的字符串  Yes  一般為16字, 但不要限制  4567890  
  
 //				byte[] searchMsg = new byte[] {
 //						// SEQ
 //						0, 0, 0, 24,
 //						// ACK
 //						0, 0, 0, 0,
 //						// flags
 //						0, 0, 
 //						// checksum
 //						0, 2,
 //						// message length
 //						0, 0, 0, 24,
 //						// command ID
 //						0, 0, 0, 1,
 //						// grouponId 
 //						0, 10, 
 //						//grouponVCode 
 //						0,15 };
 //				System.out.println("Packet size: " + searchMsg.length);
 //				os.write(searchMsg);
 //				os.flush();
 //				os.close();
 	}
 
 }
