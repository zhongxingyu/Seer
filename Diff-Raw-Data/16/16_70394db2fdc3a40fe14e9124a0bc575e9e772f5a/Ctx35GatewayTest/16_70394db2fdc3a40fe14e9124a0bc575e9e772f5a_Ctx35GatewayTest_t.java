 package no.parasit.x10.ctx35;
 
 import gnu.io.CommPortIdentifier;
 
 import java.io.IOException;
 
 import no.parasit.x10.Addressing;
 import no.parasit.x10.Command;
 import no.parasit.x10.Device;
 import no.parasit.x10.HouseCode;
import no.parasit.x10.Transmission;
 import no.parasit.x10.UnitCode;
 import no.parasit.x10.ctx35.Ctx35Gateway;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class Ctx35GatewayTest {
 	Ctx35Gateway gateway;
 
 	@Before
 	public void setUp() throws Exception {
 		gateway = new Ctx35Gateway();
 		gateway.setCommport("/dev/tty.PL2303-00002006");
 		gateway.init();
 	}
 
 	@Test
 //	@Ignore
 	public void testOnOffSerie() throws InterruptedException, IOException {
 		
		gateway.transmit(new Transmission(new Addressing('H'), Command.all_lights_off));
			gateway.transmit(new Transmission(new Addressing('H', 9,10,11), Command.on));
 			Thread.sleep(2000);
			gateway.transmit(new Transmission(new Addressing('H', 9,10,11), Command.off));
 			Thread.sleep(2000);
			gateway.transmit(new Transmission(new Addressing('H', 9,10,11), Command.on));
 		
 		
 //		gateway.transmit(new HouseCode('H'), new UnitCode[] { new UnitCode(9), new UnitCode(10), new UnitCode(11)}, Command.off, 1);
 //		gateway.transmit(new HouseCode('H'), new UnitCode[] { new UnitCode(9)}, Command.bright, 16);
 //		Thread.sleep(2000);
 		
 //		gateway.transmit(new HouseCode('H'), new UnitCode[] {new UnitCode(9)}, Command.dim, 16);
 //		gateway.transmit(new Device("H9"), Command.dim);
 //		gateway.transmit(new Device("H9"), Command.dim);
		// gateway.transmit(new Device("H9"), Command.dim);
 
 	}
 }
