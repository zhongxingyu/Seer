 package org.opnfre.mail;
 
 import java.io.IOException;
 import java.util.UUID;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 public class SendTest extends TestCase {
 	public void testSend() throws IOException {
 		String uuid = UUID.randomUUID().toString();
 		Send send = new Send();
 		assertTrue(send.connect() == 220);
 		assertTrue(send.sayHelo("51pass.org") == 250);
 		send.addTo("刘冲", "liuchong14@qq.com");
 		send.addBcc("liuchong14@gmail.com");
 		send.addBcc("liuchong14@163.com");
 		send.setFrom("liuchong", "liuchong@51pass.org");
 		send.setSubject("Test sendmail by LcJavaMail，中文, " + uuid);
 		send.setMessage("<h2>Hi!</h2>\n<p>Test，测试，中文</p>");
 		assertTrue(send.go() == 250);
 		assertTrue(send.close() == 221);
 	}

 	public void testSendSimple() throws IOException {
 		String uuid = UUID.randomUUID().toString();
 		Send send = new Send();
 		assertTrue(send.connect() == 220);
 		assertTrue(send.sayHelo("51pass.org") == 250);
 		send.setFrom("刘冲", "liuchong@51pass.org");
 		send.addTo("liuchong14@gmail.com");
 		send.setSubject("Test sendmail by LcJavaMail，中文, Simple, " + uuid);
 		send.setMessage("<h2>Hi!</h2>\n<p>Test，测试，中文</p>");
		int result = send.go();
		boolean right = result == 250;
		if (!right)
			System.out.println(result);
		assertTrue(right);
 		assertTrue(send.close() == 221);
 	}
 
 	public static void main(String[] args) {
 		TestSuite ts = new TestSuite();
 		ts.addTestSuite(SendTest.class);
 		junit.textui.TestRunner.run(ts);
 	}
 }
