 package tests;
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import java.util.Arrays;
 import java.lang.Thread;
 import pl.com.henrietta.lnx2.exceptions.*;
 import pl.com.henrietta.lnx2.Packet;
 import pl.com.henrietta.lnx2.Channel;
 import pl.com.henrietta.lnx2.RetransmissionMode;
 
 public class channeltest {
 	
 	private byte[] test_data = {0, 1, 2, 3};
 	private byte[] alt_test_data = {4, 5, 6, 7};
 	private byte[] alt2_test_data = {8, 9, 10, 11};
 	
 	@Test
 	public void testChannelIDPublic() {
 		Channel alice_0 = new Channel((byte)0, RetransmissionMode.RTM_NONE, 1, 1);
 		if (alice_0.channel_id != 0) fail("Invalid channel id");
 	}
 	
 	@Test
 	public void testRTM_NONE() throws NothingToSend, NothingToRead {
 		Channel alice_0 = new Channel((byte)0, RetransmissionMode.RTM_NONE, 1, 1);
 		Channel bob_0 = new Channel((byte)0, RetransmissionMode.RTM_NONE, 1, 1);
 		
 		alice_0.write(this.test_data);
 		if (!alice_0.is_tx_in_progress()) fail("Comms ended");
 		
 		Packet pkt = alice_0.on_sendable();
 		bob_0.on_received(pkt);
 		
 		byte[] received = bob_0.read();
 		
 		if (!Arrays.equals(received, this.test_data)) fail("Not equal");		
 	}
 	
 	@Test
 	public void testRTM_MANUAL_normalsend() throws NothingToSend, NothingToRead {
 		Channel alice_0 = new Channel((byte)0, RetransmissionMode.RTM_MANUAL, 1, 1);
 		Channel bob_0 = new Channel((byte)0, RetransmissionMode.RTM_MANUAL, 1, 1);
 		
 		alice_0.write(this.test_data);
 		Packet pkt = alice_0.on_sendable();
 		
 		bob_0.on_received(pkt);
 		
 		if (!Arrays.equals(bob_0.read(), this.test_data)) fail("Not equal");
 		
 		Packet ack = bob_0.on_sendable();
 		if (!ack.is_ack) fail("Not ACK");
 		
 		alice_0.on_received(ack);
 		if (alice_0.is_tx_in_progress()) fail("Comms not ended");
 	}
 
 	@Test
 	public void testRTM_MANUAL_resend() throws NothingToSend, NothingToRead, InterruptedException {
 		Channel alice_0 = new Channel((byte)0, RetransmissionMode.RTM_MANUAL, (float)0.5, 1);
 		Channel bob_0 = new Channel((byte)0, RetransmissionMode.RTM_MANUAL, (float)0.5, 1);
 		
 		alice_0.write(this.test_data);
 		Packet pkt = alice_0.on_sendable();
 		
 		Thread.sleep(600);
 		
 		pkt = alice_0.on_sendable();
 		bob_0.on_received(pkt);
 		
 		Packet ack = bob_0.on_sendable();
 		if (!ack.is_ack) fail("Not ACK");
 		
 		alice_0.on_received(ack);
 		if (alice_0.is_tx_in_progress()) fail("Comms not ended");
 	}
 	
 	@Test
	public void testRTM_MANUAL_dualsend() throws NothingToSend, NothingToRead, InterruptedException {
		Channel alice_0 = new Channel((byte)0, RetransmissionMode.RTM_MANUAL, (float)0.5, 1);
		Channel bob_0 = new Channel((byte)0, RetransmissionMode.RTM_MANUAL, (float)0.5, 1);

		alice_0.write(this.test_data);
		Packet pkt = alice_0.on_sendable();
		bob_0.on_received(pkt);
		alice_0.on_received(bob_0.on_sendable());
		
		// Ok, we should have a good packet here
		byte[] rdata = bob_0.read();
		if (!Arrays.equals(this.test_data, rdata)) fail("Data not equal");
		
		// Let's roll it again
		alice_0.write(this.test_data);
		pkt = alice_0.on_sendable();
		bob_0.on_received(pkt);
		alice_0.on_received(bob_0.on_sendable());

		rdata = bob_0.read();
		if (!Arrays.equals(this.test_data, rdata)) fail("Data not equal");
	}
	
	@Test
 	public void testRTM_MANUAL_duplication() throws NothingToSend, NothingToRead {
 		Channel bob_0 = new Channel((byte)0, RetransmissionMode.RTM_MANUAL, 1, 1);
 
 		Packet pkt = new Packet(this.test_data, (byte)0, (byte)0);
 		
 		bob_0.on_received(pkt);
 		bob_0.on_received(pkt);
 
 		if (!Arrays.equals(bob_0.read(), this.test_data)) fail("Not equal");
 		
 		boolean thrown = false;
 		try {
 			bob_0.read();
 		} catch (NothingToRead n) {
 			thrown = true;
 		}
 		
 		if (!thrown) fail("Data readed - shouldn't");
 	}
 	
 	@Test
 	public void testRTM_MANUAL_ackduplication() throws NothingToSend, NothingToRead {
 		Channel alice_0 = new Channel((byte)0, RetransmissionMode.RTM_MANUAL, 1, 1);
 		Channel bob_0 = new Channel((byte)0, RetransmissionMode.RTM_MANUAL, 1, 1);
 
 		alice_0.write(this.test_data);
 		Packet pkt = alice_0.on_sendable();
 		
 		bob_0.on_received(pkt);
 		if (!Arrays.equals(bob_0.read(), this.test_data)) fail("Not equal");
 		
 		Packet ack = bob_0.on_sendable();
 		if (!ack.is_ack) fail("Not ACK");
 		
 		alice_0.on_received(ack);
 		alice_0.on_received(ack);
 		
 		if (alice_0.is_tx_in_progress()) fail("Comms not ended");
 	}
 	
 	@Test
 	public void testRTM_MANUAL_handover() throws NothingToSend, NothingToRead, InterruptedException {
 		Channel alice_0 = new Channel((byte)0, RetransmissionMode.RTM_MANUAL, (float)0.5, 1);
 		Channel bob_0 = new Channel((byte)0, RetransmissionMode.RTM_MANUAL, (float)0.5, 1);
 
 		alice_0.write(this.test_data);
 		Packet pkt = alice_0.on_sendable();
 		
 		Thread.sleep(600);
 		
 		alice_0.write(this.alt_test_data);
 		
 		pkt = alice_0.on_sendable();
 		
 		bob_0.on_received(pkt);
 		if (!Arrays.equals(bob_0.read(), this.alt_test_data)) fail("Not equal");
 		Packet ack = bob_0.on_sendable();
 		if (!ack.is_ack) fail("Not ACK");
 		
 		alice_0.on_received(ack);
 		if (alice_0.is_tx_in_progress()) fail("Comms not ended");
 	}
 	
 	@Test
 	public void testRTM_AUTO_normalsend() throws NothingToSend, NothingToRead {
 		Channel alice_0 = new Channel((byte)0, RetransmissionMode.RTM_AUTO, 1, 1);
 		Channel bob_0 = new Channel((byte)0, RetransmissionMode.RTM_AUTO, 1, 1);
 		
 		alice_0.write(this.test_data);
 		Packet pkt = alice_0.on_sendable();
 		
 		bob_0.on_received(pkt);
 		
 		if (!Arrays.equals(bob_0.read(), this.test_data)) fail("Not equal");
 		
 		Packet ack = bob_0.on_sendable();
 		if (!ack.is_ack) fail("Not ACK");
 		
 		alice_0.on_received(ack);
 		if (alice_0.is_tx_in_progress()) fail("Comms not ended");
 	}
 
 	@Test
 	public void testRTM_AUTO_resend() throws NothingToSend, NothingToRead, InterruptedException {
 		Channel alice_0 = new Channel((byte)0, RetransmissionMode.RTM_AUTO, (float)0.5, 1);
 		Channel bob_0 = new Channel((byte)0, RetransmissionMode.RTM_AUTO, (float)0.5, 1);
 		
 		alice_0.write(this.test_data);
 		if (!alice_0.is_tx_in_progress()) fail("Comms ended");
 		Packet pkt = alice_0.on_sendable();
 		if (!alice_0.is_tx_in_progress()) fail("Comms ended");
 		
 		Thread.sleep(600);
 		
 		pkt = alice_0.on_sendable();
 		bob_0.on_received(pkt);
 		
 		Packet ack = bob_0.on_sendable();
 		if (!ack.is_ack) fail("Not ACK");
 		
 		alice_0.on_received(ack);
 		if (alice_0.is_tx_in_progress()) fail("Comms not ended");
 	}
 	
 	@Test
 	public void testRTM_AUTO_duplication() throws NothingToSend, NothingToRead {
 		Channel bob_0 = new Channel((byte)0, RetransmissionMode.RTM_AUTO, 1, 1);
 
 		Packet pkt = new Packet(this.test_data, (byte)0, (byte)0);
 		
 		bob_0.on_received(pkt);
 		bob_0.on_received(pkt);
 
 		if (!Arrays.equals(bob_0.read(), this.test_data)) fail("Not equal");
 		
 		boolean thrown = false;
 		try {
 			bob_0.read();
 		} catch (NothingToRead n) {
 			thrown = true;
 		}
 		
 		if (!thrown) fail("Data readed - shouldn't");
 	}
 	
 	@Test
 	public void testRTM_AUTO_ackduplication() throws NothingToSend, NothingToRead {
 		Channel alice_0 = new Channel((byte)0, RetransmissionMode.RTM_AUTO, 1, 1);
 		Channel bob_0 = new Channel((byte)0, RetransmissionMode.RTM_AUTO, 1, 1);
 
 		alice_0.write(this.test_data);
 		Packet pkt = alice_0.on_sendable();
 		if (!alice_0.is_tx_in_progress()) fail("Comms ended");
 		
 		bob_0.on_received(pkt);
 		if (!Arrays.equals(bob_0.read(), this.test_data)) fail("Not equal");
 		
 		Packet ack = bob_0.on_sendable();
 		if (!ack.is_ack) fail("Not ACK");
 		
 		alice_0.on_received(ack);
 		alice_0.on_received(ack);
 		
 		if (alice_0.is_tx_in_progress()) fail("Comms not ended");
 	}	
 	
 	
 	@Test
 	public void testRTM_AUTO_bundle_control_1() throws NothingToSend, NothingToRead {
 		Channel alice_0 = new Channel((byte)0, RetransmissionMode.RTM_AUTO, (float)0.5, 2);
 		Channel bob_0 = new Channel((byte)0, RetransmissionMode.RTM_AUTO, (float)0.5, 2);
 		
 		alice_0.write(this.test_data);
 		alice_0.write(this.alt_test_data);
 		alice_0.write(this.alt2_test_data);
 		
 		@SuppressWarnings("unused")
 		Packet pk1 = alice_0.on_sendable();
 		Packet pk2 = alice_0.on_sendable();
 		
 		boolean thrown = false;
 		try { alice_0.on_sendable(); } catch (NothingToSend n) { thrown = true; }
 		if (!thrown) fail("Not thrown");
 		
 		bob_0.on_received(pk2);
 		Packet ack_win2 = bob_0.on_sendable();
 		if (!ack_win2.is_ack) fail("Not ACK");
 		if (ack_win2.window_id != 1) fail("Window ID mismatch");
 		
 		alice_0.on_received(ack_win2);
 		if (!Arrays.equals(bob_0.read(), this.alt_test_data)) fail("Not equal");
 		
 		thrown = false;
 		try { alice_0.on_sendable(); } catch (NothingToSend n) { thrown = true; }
 		// it is forbidden to send alt2_test_data now because test_data has not been 
 		// acknowledged and max bundle size is 2
 		if (!thrown) fail("Not thrown");		
 	}
 	
 	@Test
 	public void testRTM_AUTO_bundle_control_2() throws NothingToSend, NothingToRead {
 		Channel alice_0 = new Channel((byte)0, RetransmissionMode.RTM_AUTO, (float)0.5, 2);
 		Channel bob_0 = new Channel((byte)0, RetransmissionMode.RTM_AUTO, (float)0.5, 2);
 		
 		alice_0.write(this.test_data);
 		alice_0.write(this.alt_test_data);
 		alice_0.write(this.alt2_test_data);
 		
 		Packet pk1 = alice_0.on_sendable();
 		alice_0.on_sendable();
 
 		boolean thrown = false;
 		try { alice_0.on_sendable(); } catch (NothingToSend n) { thrown = true; }
 		if (!thrown) fail("Not thrown");
 
 		bob_0.on_received(pk1);
 		Packet ack_win1 = bob_0.on_sendable();
 		if (!ack_win1.is_ack) fail("Not ACK");
 		if (ack_win1.window_id != 0) fail("window ID mismatch");
 		
 		alice_0.on_received(ack_win1);
 		
 		Packet pk3 = alice_0.on_sendable();
 		if (!Arrays.equals(pk3.data, this.alt2_test_data)) fail("Not equal");
 		// Now alt2 is allowed to be sent because test_data was confirmed
 	}
 	
 	@Test
 	public void testRTM_AUTO_ORDER_reordering() throws NothingToSend, NothingToRead {
 		Channel alice_0 = new Channel((byte)0, RetransmissionMode.RTM_AUTO_ORDERED, (float)0.5, 5);
 		Channel bob_0 = new Channel((byte)0, RetransmissionMode.RTM_AUTO_ORDERED, (float)0.5, 5);
 		
 		alice_0.write(this.test_data);
 		alice_0.write(this.alt_test_data);
 		alice_0.write(this.alt2_test_data);
 		
 		Packet pk1 = alice_0.on_sendable();
 		Packet pk2 = alice_0.on_sendable();
 		Packet pk3 = alice_0.on_sendable();
 		
 		bob_0.on_received(pk3);
 		
 		boolean thrown = false;
 		try { bob_0.read(); } catch (NothingToRead r) { thrown = true; }
 		if (!thrown) fail("Not thrown");
 		
 		bob_0.on_received(pk2);
 		thrown = false;
 		try { bob_0.read(); } catch (NothingToRead r) { thrown = true; }
 		if (!thrown) fail("Not thrown");
 
 		bob_0.on_received(pk1);
 		
 		if (!Arrays.equals(bob_0.read(), this.test_data)) fail("Not equal");
 		if (!Arrays.equals(bob_0.read(), this.alt_test_data)) fail("Not equal");
 		if (!Arrays.equals(bob_0.read(), this.alt2_test_data)) fail("Not equal");
 	}
 }
