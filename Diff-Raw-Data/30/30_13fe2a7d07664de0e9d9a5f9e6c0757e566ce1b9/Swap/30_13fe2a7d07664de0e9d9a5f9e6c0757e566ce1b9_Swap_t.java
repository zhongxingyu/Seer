 package nachos.vm;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 
 import nachos.machine.OpenFile;
 import nachos.machine.Processor;
 import nachos.threads.ThreadedKernel;
 
 public class Swap {
 
 	private Swap() {
 		swapFile = ThreadedKernel.fileSystem.open(swapName, true);
 	}
 
 	public static Swap getInstance() {
 		if (instance == null)
 			instance = new Swap();
 		return instance;
 	}
 
 	public byte[] read(int pid, int vpn) {
 		int pos = find(pid, vpn);
 		if (pos == -1)
 			return new byte[pageSize];
 
 		byte[] ret = new byte[pageSize];
 		if (swapFile.read(pos * pageSize, ret, 0, pageSize) == -1)
 			return new byte[pageSize];
 		
 		return ret;
 	}
 
 	public void add(int pid, int vpn) {
 		unallocSet.add(new IPair(pid, vpn));
 	}
 
 	public int write(int pid, int vpn, byte[] page, int offset) {
 		int pos = alloc(pid, vpn);
 		if (pos == -1)
 			return 0;
 
 		swapFile.write(pos * pageSize, page, offset, pageSize);
 		return pos;
 	}
 
 	public void remove(int pid, int vpn) {
 		if (find(pid, vpn) == -1)
 			return;
 		queue.add(swapTable.remove(new IPair(pid, vpn)));
 	}
 
 	public void close() {
 		if (swapFile == null)
 			return;
 
 		swapFile.close();
 		ThreadedKernel.fileSystem.remove(swapName);
 		swapFile = null;
 	}
 
 	private int find(int pid, int vpn) {
 		Integer ret = swapTable.get(new IPair(pid, vpn));
 		return ret == null ? -1 : ret;
 	}
 
 	private int alloc(int pid, int vpn) {
 		IPair ip = new IPair(pid, vpn);
 		if (!unallocSet.contains(ip))
 			return find(pid, vpn);
 
 		unallocSet.remove(ip);
 		if (queue.size() == 0)
 			queue.add(new Integer(swapTable.size()));
 		
 		int index = queue.poll();
 		swapTable.put(new IPair(pid, vpn), index);
 		return index;
 	}
 
 	protected final static char dbgVM = 'v';
 	private static Swap instance = null;
 
 	private HashSet<IPair> unallocSet = new HashSet<IPair>();
 	private HashMap<IPair, Integer> swapTable = new HashMap<IPair, Integer>();
 	private LinkedList<Integer> queue = new LinkedList<Integer>();
 	private OpenFile swapFile;
 	private int pageSize = Processor.pageSize;
 
	private final String swapName = "/SWAP";
 }
