 import java.util.Arrays;
 
 public class USBHardDiskAdapterTest extends Assert {
 
 	private Root root;
 	private USBPort usbPort;
 
 	@Before
 	public void setUp() {
 		usbPort = new USBPort();
 		root = new Root(usbPort);
 	}
 
 	@Tst
 	public void assembleIt() {
 		HDD internalHDD = new HDD("1TB-Platte");
 		USBHardDiskAdapter<HDD> usbHDD = new USBHardDiskAdapter<HDD>(internalHDD);
 		boolean insertWorks;
 
 		assertTrue(root.volumes().isEmpty());
 
 		insertWorks = usbPort.insert(usbHDD);
 
 		assertTrue(insertWorks);
 		assertEquals(Arrays.asList("1TB-Platte"), root.volumes());
 	}
 
 	@Tst
 	public void insertTwiceDoesNotWork() {
 		HDD internalHDD = new HDD("2TB-Platte");
 		USBHardDiskAdapter<HDD> usbHDD = new USBHardDiskAdapter<HDD>(internalHDD);
 		boolean insertWorks;
 
 		insertWorks = usbPort.insert(usbHDD);
 		assertTrue(insertWorks);
 
 		insertWorks = usbPort.insert(usbHDD);
 		assertFalse(insertWorks);
 	}
 
 	@Tst
 	public void adaptsToSSDsToo() {
 		SSD internalSSD = new SSD("kleine Platte");
 		USBHardDiskAdapter<SSD> usbSSD = new USBHardDiskAdapter<SSD>(internalSSD);
		assertTrue(usbPort.insert(usbSSD));
 	}
 }
