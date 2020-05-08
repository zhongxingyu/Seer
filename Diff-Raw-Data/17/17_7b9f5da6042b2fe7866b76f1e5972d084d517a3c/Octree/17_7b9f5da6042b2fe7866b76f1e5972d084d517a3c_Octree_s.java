 package octree;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class Octree {
     private Integer depth;
     private Float length, origin[];
     private OctNode root = null;
     private Integer dimensions = 3;
     
     private Octree(Integer depth, Float[] origin, Float length, OctNode root) {
         this.depth = depth;
         this.root = root;
         this.origin = Arrays.copyOf(origin, 3);
         this.length = length;
     }
     
     public OctNode getRoot() { return root; }
     public Integer getDepth() { return this.depth; }
     public Float getLength() { return this.length; }
     public Float[] getOrigin() { return Arrays.copyOf(this.origin, this.origin.length); }
     public Integer getDimensions() { return this.dimensions; }
     public ArrayList<Float[]> getAllVertices() throws OctNodeException { return this.root.getAllVertices(); }
 
     static public Octree parseFromFile(String filename) throws FileNotFoundException, IOException, OctreeException, OctNodeException {
         // Open filename and read content to byte buffer
         File file = new File(filename);
         FileInputStream fis = new FileInputStream(file);
         byte contents[] = new byte[(int)file.length()];
         if (fis.read(contents) != file.length())
             throw new IOException("Unable to read entire file.");
         
         // Check header for correct descriptor
         if (!(new String(contents, 0, 14)).equals("SOG.Format 1.0"))
             throw new InvalidSOGFileSyntaxOctreeException();
 
         // Read origin of axes and cube length
         // ByteBuffer is used due to endianness concerns; Java works in big endian by default, we usually read little endian data
         // The system's native endianness is used instead
         ByteBuffer header = ByteBuffer.wrap(contents, 15, 113).order(ByteOrder.nativeOrder());
         Float origin[] = new Float[3];
         origin[0] = header.getFloat();
         origin[1] = header.getFloat();
         origin[2] = header.getFloat();
         Float length = header.getFloat();
         
         ByteBuffer buffer = ByteBuffer.wrap(contents, 128, contents.length-128).order(ByteOrder.nativeOrder());
         Integer depth = buffer.getInt();
         if (depth <= 0) throw new InvalidSOGFileSyntaxOctreeException();
         depth = 31 - Integer.numberOfLeadingZeros(depth);
         OctNode newRoot = recurseParse(0, buffer, origin, length);
         return new Octree(depth, origin, length, newRoot);
     }
     
     static private OctNode recurseParse(Integer currentDepth, ByteBuffer buffer, Float[] origin, Float length) throws IOException, OctNodeException, OctreeException {
         Byte type = buffer.get();
         switch (type) {
             case OctNode.OCTNODE_EMPTY:
                 Byte value = buffer.get();
                 return new OctNode(currentDepth, (value == 0)?false:true, origin, length);
             case OctNode.OCTNODE_LEAF:
                 Byte signs = buffer.get();
                 Float coords[] = new Float[3];
                 coords[0] = buffer.getFloat();
                 coords[1] = buffer.getFloat();
                 coords[2] = buffer.getFloat();
                 return new OctNode(currentDepth, signs, coords, origin, length);
             case OctNode.OCTNODE_INTERMEDIATE:
                 OctNode children[] = new OctNode[8];
                 for (int i = 0; i < 8; i++) {
                     Float[] newOrigin = new Float[3];
                    newOrigin[0] = origin[0]+((i%2==1)?length/2:0);
                     newOrigin[1] = origin[1]+(((i>>1)%2==1)?length/2:0);
                    newOrigin[2] = origin[2]+(((i>>2)%2==1)?length/2:0);
                     children[i] = recurseParse(currentDepth+1, buffer, newOrigin, length/2);
                 }
                 return new OctNode(currentDepth, children, origin, length);
             default:
                 throw new InvalidSOGFileSyntaxOctreeException();
         }
     }
 }
