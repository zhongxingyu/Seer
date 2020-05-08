 package lab4;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * This interface represents the general contract for all File Writer 
  * Strategy implementations. The return type is generic so that future
  * implementations can use whatever data type is necessary.
  * 
  * @author  Jim Lombardo, jlombardo@wctc.edu
  * @version 1.00
  */
 public interface FileWriterStrategy<T> {
 
     String getFilePath();
     
     boolean getAppendIndicator();
     
     FormatStrategy getFormatter();
 
    void writeFile(T dataOut) throws IOException;
 
     void setFilePath(String filePath);
 
     void setAppendIndicator(boolean appendIndicator);
     
     void setFormatter(FormatStrategy formatter);
     
 }
