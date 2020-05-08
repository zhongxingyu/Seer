 package com.sandreckoning.hadoop.checksum;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.mapred.InputSplit;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.Vector;
 
 class PathInputSplit implements InputSplit {
    static class PathInputSplitPart {
         public final Path path;
         public final long length;
 
         public PathInputSplitPart(Path path, long length) {
             this.path = path;
             this.length = length;
         }
     }
 
     private Vector<PathInputSplitPart> paths = new Vector<PathInputSplitPart>();
 
     public int numPaths() {
         return paths.size();
     }
 
     public Path getPath(int idx) {
         return paths.get(idx).path;
     }
 
     public void insertPath(Path path, long len) {
         paths.add(new PathInputSplitPart(path, len));
     }
 
     @Override
     public long getLength() throws IOException {
         long size = 0;
 
         for (PathInputSplitPart part : paths)
             size += part.length;
 
         return size;
     }
 
     @Override
     public String[] getLocations() throws IOException {
         return new String[0];
     }
 
     @Override
     public void write(DataOutput dataOutput) throws IOException {
         dataOutput.writeInt(paths.size());
 
         for (PathInputSplitPart part : paths) {
             byte[] bytes = part.path.toString().getBytes();
             dataOutput.writeInt(bytes.length);
             dataOutput.write(bytes);
             dataOutput.writeLong(part.length);
         }
     }
 
     @Override
     public void readFields(DataInput dataInput) throws IOException {
         int vectorSize = dataInput.readInt();
         paths = new Vector<PathInputSplitPart>(vectorSize);
 
         for (int i = 1; i < vectorSize; i++) {
             int size = dataInput.readInt();
 
             byte[] bytes = new byte[size];
             dataInput.readFully(bytes);
             Path path = new Path(new String(bytes));
 
             long length = dataInput.readLong();
             paths.add(new PathInputSplitPart(path, length));
         }
     }
 }
