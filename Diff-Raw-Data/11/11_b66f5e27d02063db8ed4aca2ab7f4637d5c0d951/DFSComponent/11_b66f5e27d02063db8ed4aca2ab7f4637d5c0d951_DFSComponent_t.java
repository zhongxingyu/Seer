import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Reader;
 
 import edu.washington.cs.cse490h.lib.Node;
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 
 public class DFSComponent {
 
   protected DFSNode parent;
   public final int addr;
 
   protected DFSComponent(DFSNode parent) {
     this.parent = parent;
     this.addr = parent.addr;
   }
   	
   public String readFileToString(String filename) throws IOException {
     PersistentStorageReader reader = this.getReader(filename);
     return readRemainingContentsToString(reader);
   }
 
   public String readRemainingContentsToString(Reader reader) throws IOException {
     StringBuilder sb = new StringBuilder();
 
     char[] cbuf = new char[1024];
     int bytesRead = reader.read(cbuf, 0, 1024);
     while(bytesRead != -1) {
       sb.append(cbuf, 0, bytesRead);
       bytesRead = reader.read(cbuf, 0, 1024);
     }
 
     return sb.toString();
   }
 
  PersistentStorageReader getReader(String fileName) throws FileNotFoundException {
    return parent.getReader(fileName);
   }
 
  PersistentStorageWriter getWriter(String fileName) throws IOException {
    return parent.getWriter(fileName, false);
   }
 
   public int RIOSend(int destAddr, int protocol, byte[] payload) {
     return parent.RIOSend(destAddr, protocol, payload);
   }
 }
