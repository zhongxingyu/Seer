 package forma;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Comparator;
 
 import cascading.tuple.Comparison;
 import cascading.tuple.hadoop.BytesComparator;
 import cascading.tuple.hadoop.SerializationToken;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.io.serializer.Deserializer;
 import org.apache.hadoop.io.serializer.Serialization;
 import org.apache.hadoop.io.serializer.Serializer;
 
 @SerializationToken(tokens = {131}, classNames = {"[I"})
     public class IntsSerialization extends Configured implements Comparison<byte[]>, Serialization<int[]>
 {
 
     public static class RawIntsDeserializer implements Deserializer<int[]>
     {
         private DataInputStream in;
 
         @Override
             public void open( InputStream in ) throws IOException
             {
                 if( in instanceof DataInputStream )
                     this.in = (DataInputStream) in;
                 else
                     this.in = new DataInputStream( in );
             }
         
         @Override
             public int[] deserialize( int[] testText ) throws IOException
             {
                 int len = in.readInt();
 
                 int[] ints = new int[len];
 
                 for (int i = 0; i < len; i++) {
                     ints[i] = in.readInt();
                 }
 
                 return ints;
             }
 
         @Override
             public void close() throws IOException
             {
                 in.close();
             }
     }
 
     public static class RawIntsSerializer implements Serializer<int[]>
     {
         private DataOutputStream out;
 
         @Override
             public void open( OutputStream out ) throws IOException
             {
                 if( out instanceof DataOutputStream )
                     this.out = (DataOutputStream) out;
                 else
                     this.out = new DataOutputStream( out );
             }
 
         @Override
             public void serialize( int[] ints ) throws IOException
             {
                 int len = ints.length;
                 out.writeInt( len );
 
                 for (int i = 0; i < len; i++) {
                     out.writeInt( ints[i] );
                 }
             }
 
         @Override
             public void close() throws IOException
             {
                 out.close();
             }
     }
 
 
     public IntsSerialization()
         {
         }
 
     @Override
         public boolean accept( Class<?> c )
         {
             return int[].class == c;
         }
 
     @Override
         public Serializer<int[]> getSerializer( Class<int[]> c )
         {
             return new RawIntsSerializer();
         }
 
     @Override
         public Deserializer<int[]> getDeserializer( Class<int[]> c )
         {
             return new RawIntsDeserializer();
         }
 
     @Override
         public Comparator<byte[]> getComparator( Class<byte[]> type )
         {
             return new BytesComparator();
         }
}
