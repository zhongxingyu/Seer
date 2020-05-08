 import java.util.HashMap;
 import java.util.Scanner;
 
 /**
  *
  */
 
 /**
  * @author akai
  * 
  */
 public class CommandReadFile extends Command implements Constants {
 	public String								filename;
 	public int									byteOffset;
 	public int									byteLength;
 	public static HashMap<String, CacheEntry>	cache	= new HashMap<String, CacheEntry>();	;
 
 	public CommandReadFile(String filename, int byteOffset, int byteLength) {
 		super();
 		this.filename = filename;
 		this.byteOffset = byteOffset;
 		this.byteLength = byteLength;
 		this.cmdName = VAL_CMD_READFILE;
 	}
 
 	/**
 	 * 
 	 */
 	public CommandReadFile() {
 		this.cmdName = VAL_CMD_READFILE;
 	}
 
 	/* construct the request from current data to send to server */
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(KEY_FILENAME + ":" + filename + DELIM);
 		return wrapRequest(sb);
 	}
 
 	/* (non-Javadoc)
 	 * 
 	 * @see Command#requestData() */
 	@Override
 	public void requestData() {
 		System.out.print("[info] Please enter filename, byte offset and length: ");
 		Scanner sc = new Scanner(System.in);
 		filename = sc.next();
 		byteOffset = sc.nextInt();
 		byteLength = sc.nextInt();
 
 		/* if the file is in cache and */
 		if (cache.containsKey(filename) && cache.get(filename).isValid()) {
 			System.out.println("[info] from cache");
 			isServed = true;
 			System.out.println("[content] "
 					+ getData(cache.get(filename).data, byteOffset, byteLength));
 		}
 	}
 
 	@Override
 	public boolean processReply() {
		if (!super.processReply())
 			return false;
 		CacheEntry ce = new CacheEntry(request.content, CACHE_FRESHTIME);
 		cache.put(filename, ce);
 		System.out.println("[content] " + getData(request.content, byteOffset, byteLength));
 		return true;
 	}
 
 	public static String getData(String str, int start, int length) {
 		if (str == null)
 			return null;
		if (start > str.length())
 			return null;
 		if (start + length > str.length())
 			return str.substring(start, str.length() - 1);
 		else
 			return str.substring(start, length + start);
 
 	}
 }
