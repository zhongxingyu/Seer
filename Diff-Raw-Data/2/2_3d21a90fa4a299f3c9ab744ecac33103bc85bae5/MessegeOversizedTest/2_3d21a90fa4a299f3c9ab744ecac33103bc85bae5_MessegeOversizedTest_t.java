 package pt.com.broker.functests.negative;
 
 import pt.com.broker.functests.helpers.GenericNegativeTest;
 
 public class MessegeOversizedTest extends GenericNegativeTest
 {
 
 	public MessegeOversizedTest()
 	{
 		super("Message oversize");
 
 		setDataToSend(new byte[] { 0, (byte) getEncodingProtocolType().ordinal(), 0, 0, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0, 0 });
 
 		setFaultCode("1101");
 		setFaultMessage("Invalid message size");
 		
		setOkToTimeOut(true);
 	}
 }
