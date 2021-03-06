 package edu.uci.ics.asterix.dataflow.data.nontagged.printers;
 
 import java.io.PrintStream;
 
 import edu.uci.ics.asterix.dataflow.data.nontagged.serde.ADoubleSerializerDeserializer;
 import edu.uci.ics.hyracks.algebricks.common.exceptions.AlgebricksException;
 import edu.uci.ics.hyracks.algebricks.data.IPrinter;
 
 public class ARectanglePrinter implements IPrinter {
 
     public static final ARectanglePrinter INSTANCE = new ARectanglePrinter();
 
     @Override
     public void init() {
 
     }
 
     @Override
     public void print(byte[] b, int s, int l, PrintStream ps) throws AlgebricksException {
        ps.print("rectangle(\"");
        ps.print(ADoubleSerializerDeserializer.getDouble(b, s + 1));
        ps.print(",");
         ps.print(ADoubleSerializerDeserializer.getDouble(b, s + 9));
        ps.print(" ");
        ps.print(ADoubleSerializerDeserializer.getDouble(b, s + 17));
        ps.print(",");
        ps.print(ADoubleSerializerDeserializer.getDouble(b, s + 25));
        ps.print("\")");
     }
 }
