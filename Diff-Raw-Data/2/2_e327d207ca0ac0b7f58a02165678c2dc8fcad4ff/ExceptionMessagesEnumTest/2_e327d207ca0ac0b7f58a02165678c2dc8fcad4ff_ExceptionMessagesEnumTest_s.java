 package com.operativus.senacrs.audit.exceptions;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.operativus.senacrs.audit.properties.messages.MessagesCentral;
 
 
 public class ExceptionMessagesEnumTest {
 
 	@Test
	public void testGetKey() {
 		
 		for (ExceptionMessagesEnum e : ExceptionMessagesEnum.values()) {
 			Assert.assertTrue(MessagesCentral.hasKey(e));			
 		}
 	}
 
 }
