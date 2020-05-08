 package filemanagmentsystem;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.List;
 /**
 *
  * @author bspor
  */
 public interface FileFormatStrategy <V> {
     public abstract List<V> decode(List<V> type);
     public abstract List<V> encode(List<V> type);
     public abstract List<V> writeToFile(List<V> type);
 }
