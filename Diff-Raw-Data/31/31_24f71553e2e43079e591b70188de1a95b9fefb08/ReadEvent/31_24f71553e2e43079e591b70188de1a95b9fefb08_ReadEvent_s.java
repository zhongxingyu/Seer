 import java.util.*;
 
 public class ReadEvent extends EventObject {
 
 	private byte[] _utf;
 
 	public ReadEvent(Object source, byte[] utf) {
 		super(source);
 		_utf = utf;
 	}
 
	public byte[] getReadInput() {
 		return _utf;
 	}
 }
