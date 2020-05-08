 package com.example.antispam;
 
 import android.content.Intent;
 import android.telephony.PhoneNumberUtils;
 import android.test.ActivityInstrumentationTestCase2;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
import com.dj.antispam.MainActivity;
import com.dj.antispam.dao.SmsDao;
 import org.smslib.GSMAlphabet;
 
 /**
  * This is a simple framework for a test of an Application.  See
  * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
  * how to write and extend Application tests.
  * <p/>
  * To run this test, you can type:
  * adb shell am instrument -w \
  * -e class com.example.antispam.MainActivityTest \
  * com.example.antispam.tests/android.test.InstrumentationTestRunner
  */
 public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
 
 	public MainActivityTest() {
 		super("com.example.antispam", MainActivity.class);
 	}
 
 	public void testBlackAndWhiteList() {
 		SmsDao dao = new SmsDao(getActivity());
 		final String sender = "990099";
 		assertNull(dao.isSenderASpammer(sender));
 		dao.markSender(sender, true);
 		assertTrue(dao.isSenderASpammer(sender));
 		dao.markSender(sender, false);
 		assertFalse(dao.isSenderASpammer(sender));
 		dao.markSender(sender, null);
 		assertNull(dao.isSenderASpammer(sender));
 	}
 
 	private void sendSMS(String sender, String body) throws IOException {
 		byte [] pdu = null ;
 		byte [] scBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD("0000000000");
 		byte [] senderBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD(sender);
 		int lsmcs = scBytes.length;
 		byte [] dateBytes = new byte [ 7 ];
 		Calendar calendar = new GregorianCalendar();
 		dateBytes[ 0 ] = reverseByte(( byte ) (calendar.get(Calendar.YEAR)));
 		dateBytes[ 1 ] = reverseByte(( byte ) (calendar.get(Calendar.MONTH) + 1 ));
 		dateBytes[ 2 ] = reverseByte(( byte ) (calendar.get(Calendar.DAY_OF_MONTH)));
 		dateBytes[ 3 ] = reverseByte(( byte ) (calendar.get(Calendar.HOUR_OF_DAY)));
 		dateBytes[ 4 ] = reverseByte(( byte ) (calendar.get(Calendar.MINUTE)));
 		dateBytes[ 5 ] = reverseByte(( byte ) (calendar.get(Calendar.SECOND)));
 		dateBytes[ 6 ] = reverseByte(( byte ) ((calendar.get(Calendar.ZONE_OFFSET) + calendar
 				.get(Calendar.DST_OFFSET)) / ( 60 * 1000 * 15 )));
 		try {
 			ByteArrayOutputStream bo = new ByteArrayOutputStream();
 			bo.write(lsmcs);
 			bo.write(scBytes);
 			bo.write( 0x04 );
 			bo.write(( byte ) sender.length());
 			bo.write(senderBytes);
 			bo.write( 0x00 );
 			bo.write( 0x00 );  // encoding: 0 for default 7bit
 			bo.write(dateBytes);
 			try {
 				String bodybytes = GSMAlphabet.textToPDU(body);
 				bo.write(bodybytes.getBytes());
 			} catch(Exception e) {}
 
 			pdu = bo.toByteArray();
 		} catch (IOException e) {
 		}
 
 		Intent intent = new Intent();
 		intent.setClassName( "com.android.mms" ,
 				"com.android.mms.transaction.SmsReceiverService" );
 		intent.setAction( "android.provider.Telephony.SMS_RECEIVED" );
 		intent.putExtra( "pdus" , new Object[] { pdu });
 		getActivity().startService(intent);
 	}
 
 	private static byte reverseByte( byte b) {
 		return ( byte ) ((b & 0xF0 ) >> 4 | (b & 0x0F ) << 4 );
 	}
 }
