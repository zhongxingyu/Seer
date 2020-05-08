 package filesystem;
 
 import java.util.Arrays;
 
 import junit.framework.TestCase;
 import kernel.Kernel;
 import kernel.PCB;
 import machine.Configuration;
 import machine.Machine;
 
 public class DirectoryTest extends TestCase {
 
 	Machine machine;
 	
 	BasicFileSystem fs;
 	
 	@Override
 	protected void setUp() throws Exception {
		DirectoryCreateFS.main(null);
 		
 		machine = new Machine();
 		machine.createDevices();
 		
 		// start hd thread
 		new Thread(machine.hd, "Hard Drive Thread").start();
 		
 		fs = new BasicFileSystem();
 		fs.initialize(machine);
 		
 		Configuration.driveDelay = 0;
 		
 		super.setUp();
 	}
 	
 	public void testChdirRelative() throws Exception {
 		PCB process = new PCB();
 		
 		int rval = fs.chdir("test", process);
 		assertTrue(rval == 0);
		assertTrue(process.cwdBlock != 0);
 		
 		rval = fs.chdir(".", process);
 		assertTrue(rval == 0);
		assertTrue(process.cwdBlock != 0);
 
 		rval = fs.chdir("..", process);
 		assertTrue(rval == 0);
 		assertTrue(process.cwdBlock == 0);
		
		rval = fs.chdir("/..", process);
		assertTrue(rval == 0);
		assertTrue(process.cwdBlock == 0);
 	}
 	
 	public void testChdirAbsolute() throws Exception {
 		PCB process = new PCB();
 		
 		int rval = fs.chdir("/test", process);
 		assertTrue(rval == 0);
		assertTrue(process.cwdBlock != 99);
 		
 		rval = fs.chdir(".", process);
 		assertTrue(rval == 0);
		assertTrue(process.cwdBlock != 99);
 		
 		rval = fs.chdir("..", process);
 		assertTrue(rval == 0);
 		assertTrue(process.cwdBlock == 0);
 		
 		rval = fs.chdir("/test", process);
 		assertTrue(rval == 0);
		assertTrue(process.cwdBlock != 99);
 		
 		rval = fs.chdir("/", process);
 		assertTrue(rval == 0);
 		assertTrue(process.cwdBlock == 0);
 	}
 	
 	public void testMkdir() throws Exception {
 		PCB process = new PCB();
 		
 		int rval = fs.mkdir("test", process);
 		assertTrue(rval == -1);
 		
 		rval = fs.mkdir(".", process);
 		assertTrue(rval == -1);
 		
 		rval = fs.mkdir("..", process);
 		assertTrue(rval == -1);
 		
 		rval = fs.mkdir("asdf", process);
 		assertTrue(rval == 0);
 		
 		rval = fs.chdir("asdf", process);
 		assertTrue(rval == 0);
 		
 		rval = fs.mkdir("tef", process);
 		assertTrue(rval == 0);
 		
 		rval = fs.chdir("tef", process);
 		assertTrue(rval == 0);
 		
 		int fid = fs.create("qwer", process);
 		assertTrue(fid == 2);
 	
 		byte[] data2 = new byte[200];
 		
 		Arrays.fill(data2, (byte)32);
 		
 		// write data to memory
 		for(int i = 0; i < data2.length; i++){
 			machine.memory().writeMem(i, 1, data2[i]);
 		}
 		
 		fs.write(fid, 200, 0, new Kernel(machine), process);
 	
 		// blank memory
 		byte[] data3 = new byte[200];
 		
 		Arrays.fill(data2, (byte)0);
 		
 		// write data to memory
 		for(int i = 0; i < data2.length; i++){
 			machine.memory().writeMem(i, 1, data2[i]);
 		}
 		
 		fs.close(fid, process);
 		
 		fid = fs.open("qwer", process);
 		assertTrue(fid == 2);
 		
 		byte[] out = new byte[data2.length];
 		
 		fs.seek(fid, 0, process);
 		
 		rval = fs.read(fid, out.length, 0, new Kernel(machine), process);
 		
 		for(int i =0; i < out.length; i++){
 			out[i] = (byte) machine.memory().readMem(i, 1);
 		}
 		
 		assertTrue(Arrays.equals(data2, out));
 	}
 	
 	public void testRmdir() throws Exception {
 		PCB process = new PCB();
 		
 		int rval = fs.rmdir("test", process);
 		assertTrue(rval == -1);
 		
 		rval = fs.mkdir("asdf", process);
 		assertTrue(rval == 0);
 		
 		rval = fs.rmdir("/asdf", process);
 		assertTrue(rval == 0);
 		
 		rval = fs.mkdir("/asdf", process);
 		assertTrue(rval == 0);
 		
 		rval = fs.rmdir("asdf", process);
 		assertTrue(rval == 0);
 		
 		rval = fs.chdir("asdf", process);
 		assertTrue(rval == -1);
 		
 	}
 }
