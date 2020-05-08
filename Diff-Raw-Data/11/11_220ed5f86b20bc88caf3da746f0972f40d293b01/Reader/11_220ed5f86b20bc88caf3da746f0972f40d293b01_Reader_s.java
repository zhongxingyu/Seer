 package org.parilin.dbgrep;
 
 import java.io.IOException;
 import java.nio.CharBuffer;
 
public interface Reader extends AutoCloseable {
 
     boolean read(CharBuffer buff) throws IOException;
 }
