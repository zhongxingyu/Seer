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
 
 @SerializationToken(tokens = {130}, classNames = {"[F"})
     public class FloatsSerialization extends Configured implements Comparison<byte[]>, Serialization<float[]>
 {
 
     public static class RawFloatsDeserializer implements Deserializer<float[]>
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
             public float[] deserialize( float[] testText ) throws IOException
             {
                 int len = in.readInt();
 
                 float[] floats = new float[len];
 
                 for (int i = 0; i < len; i++) {
                     floats[i] = in.readFloat();
                 }
 
                 return floats;
             }
 
         @Override
             public void close() throws IOException
             {
                 in.close();
             }
     }
 
     public static class RawFloatsSerializer implements Serializer<float[]>
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
             public void serialize( float[] floats ) throws IOException
             {
                 int len = floats.length;
                 out.writeInt( len );
 
                 for (int i = 0; i < len; i++) {
                     out.writeFloat( floats[i] );
                 }
             }
 
         @Override
             public void close() throws IOException
             {
                 out.close();
             }
     }
 
 
     public FloatsSerialization()
         {
         }
 
     @Override
         public boolean accept( Class<?> c )
         {
             return float[].class == c;
         }
 
     @Override
         public Serializer<float[]> getSerializer( Class<float[]> c )
         {
             return new RawFloatsSerializer();
         }
 
     @Override
         public Deserializer<float[]> getDeserializer( Class<float[]> c )
         {
             return new RawFloatsDeserializer();
         }
 
     @Override
         public Comparator<byte[]> getComparator( Class<byte[]> type )
         {
             return new BytesComparator();
         }
}
