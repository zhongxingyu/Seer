 package org.parilin.dbgrep;
 
import java.io.Closeable;
 import java.io.IOException;
 import java.nio.CharBuffer;
 
public interface Reader extends Closeable {
 
     boolean read(CharBuffer buff) throws IOException;
 }
