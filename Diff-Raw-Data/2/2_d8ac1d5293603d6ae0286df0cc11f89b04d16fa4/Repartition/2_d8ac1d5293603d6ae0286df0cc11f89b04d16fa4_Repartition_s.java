 package edu.umn.cs.spatialHadoop.operations;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Vector;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.fs.PathFilter;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.mapred.ClusterStatus;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.Mapper;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reporter;
 
 import edu.umn.cs.spatialHadoop.CommandLineArguments;
 import edu.umn.cs.spatialHadoop.core.CellInfo;
 import edu.umn.cs.spatialHadoop.core.GlobalIndex;
 import edu.umn.cs.spatialHadoop.core.GridInfo;
 import edu.umn.cs.spatialHadoop.core.GridRecordWriter;
 import edu.umn.cs.spatialHadoop.core.Partition;
 import edu.umn.cs.spatialHadoop.core.Point;
 import edu.umn.cs.spatialHadoop.core.RTree;
 import edu.umn.cs.spatialHadoop.core.RTreeGridRecordWriter;
 import edu.umn.cs.spatialHadoop.core.Rectangle;
 import edu.umn.cs.spatialHadoop.core.ResultCollector;
 import edu.umn.cs.spatialHadoop.core.Shape;
 import edu.umn.cs.spatialHadoop.core.ShapeRecordWriter;
 import edu.umn.cs.spatialHadoop.core.SpatialSite;
 import edu.umn.cs.spatialHadoop.mapred.GridOutputFormat;
 import edu.umn.cs.spatialHadoop.mapred.RTreeGridOutputFormat;
 import edu.umn.cs.spatialHadoop.mapred.ShapeInputFormat;
 import edu.umn.cs.spatialHadoop.mapred.ShapeRecordReader;
 
 /**
  * Repartitions a file according to a different grid through a MapReduce job
  * @author Ahmed Eldawy
  *
  */
 public class Repartition {
   static final Log LOG = LogFactory.getLog(Repartition.class);
   
   /**
    * The map class maps each object to all cells it overlaps with.
    * @author Ahmed Eldawy
    *
    */
   public static class RepartitionMap<T extends Shape> extends MapReduceBase
       implements Mapper<Rectangle, T, IntWritable, T> {
     /**List of cells used by the mapper*/
     private CellInfo[] cellInfos;
     
     /**Used to output intermediate records*/
     private IntWritable cellId = new IntWritable();
     
     @Override
     public void configure(JobConf job) {
       try {
         cellInfos = SpatialSite.getCells(job);
         super.configure(job);
       } catch (IOException e) {
         e.printStackTrace();
       }
     }
     
     /**
      * Map function
      * @param dummy
      * @param shape
      * @param output
      * @param reporter
      * @throws IOException
      */
     public void map(Rectangle cellMbr, T shape,
         OutputCollector<IntWritable, T> output, Reporter reporter)
         throws IOException {
       Rectangle shape_mbr = shape.getMBR();
       // Only send shape to output if its lowest corner lies in the cellMBR
       // This ensures that a replicated shape in an already partitioned file
       // doesn't get send to output from all partitions
       if (!cellMbr.isValid() || cellMbr.contains(shape_mbr.x1, shape_mbr.y1)) {
         for (int cellIndex = 0; cellIndex < cellInfos.length; cellIndex++) {
           if (cellInfos[cellIndex].isIntersected(shape)) {
             cellId.set((int) cellInfos[cellIndex].cellId);
             output.collect(cellId, shape);
           }
         }
       }
     }
   }
   
   /**
    * Calculates number of partitions required to index the given file
    * @param inFs
    * @param inFile
    * @param rtree
    * @return
    * @throws IOException 
    */
   static int calculateNumberOfPartitions(Configuration conf, long inFileSize,
       FileSystem outFs, Path outFile, long blockSize) throws IOException {
     final float IndexingOverhead =
         conf.getFloat(SpatialSite.INDEXING_OVERHEAD, 0.1f);
     long indexedFileSize = (long) (inFileSize * (1 + IndexingOverhead));
     if (blockSize == 0)
       blockSize = outFs.getDefaultBlockSize(outFile);
     return (int)Math.ceil((float)indexedFileSize / blockSize);
   }
 	
 	/**
    * Repartitions a file that is already in HDFS. It runs a MapReduce job
    * that partitions the file into cells, and writes each cell separately.
    * @param conf
    * @param inFile
    * @param outPath
    * @param gridInfo
 	 * @param stockShape 
    * @param pack
    * @param rtree
    * @param overwrite
    * @throws IOException
    */
   public static void repartitionMapReduce(Path inFile, Path outPath,
       Shape stockShape, long blockSize, String gindex,
       String lindex, boolean overwrite) throws IOException {
     
     FileSystem inFs = inFile.getFileSystem(new Configuration());
     FileSystem outFs = outPath.getFileSystem(new Configuration());
 
     // Calculate number of partitions in output file
     // Copy blocksize from source file if it's globally indexed
     if (blockSize == 0) {
       GlobalIndex<Partition> globalIndex = SpatialSite.getGlobalIndex(inFs, inFile);
       if (globalIndex != null) {
         blockSize = inFs.getFileStatus(new Path(inFile, globalIndex.iterator().next().filename)).getBlockSize();
       }
     }
     
     // Calculate the dimensions of each partition based on gindex type
     CellInfo[] cellInfos;
     if (gindex == null) {
       throw new RuntimeException("Unsupported global index: "+gindex);
     } else if (gindex.equals("grid")) {
       Rectangle input_mbr = FileMBR.fileMBRMapReduce(inFs, inFile, stockShape);
       long inFileSize = FileMBR.sizeOfLastProcessedFile;
       int num_partitions = calculateNumberOfPartitions(new Configuration(),
           inFileSize, outFs, outPath, blockSize);
 
       GridInfo gridInfo = new GridInfo(input_mbr.x1, input_mbr.y1,
           input_mbr.x2, input_mbr.y2);
       gridInfo.calculateCellDimensions(num_partitions);
       cellInfos = gridInfo.getAllCells();
     } else if (gindex.equals("rtree")) {
       // Pack in rectangles using an RTree
       cellInfos = packInRectangles(inFs, inFile, outFs, outPath, blockSize, stockShape);
     } else {
       throw new RuntimeException("Unsupported global index: "+gindex);
     }
     
     repartitionMapReduce(inFile, outPath, stockShape, blockSize, cellInfos,
         gindex, lindex, overwrite);
   }
   
   /**
    * Repartitions an input file according to the given list of cells.
    * @param inFile
    * @param outPath
    * @param cellInfos
    * @param pack
    * @param rtree
    * @param overwrite
    * @throws IOException
    */
   public static void repartitionMapReduce(Path inFile, Path outPath,
       Shape stockShape, long blockSize, CellInfo[] cellInfos, String gindex,
       String lindex, boolean overwrite) throws IOException {
     JobConf job = new JobConf(Repartition.class);
     job.setJobName("Repartition");
     FileSystem outFs = outPath.getFileSystem(job);
     
     // Overwrite output file
     if (outFs.exists(outPath)) {
       if (overwrite)
         outFs.delete(outPath, true);
       else
         throw new RuntimeException("Output file '" + outPath
             + "' already exists and overwrite flag is not set");
     }
     
     // Decide which map function to use depending on how blocks are indexed
     // And also which input format to use
     job.setMapperClass(RepartitionMap.class);
     job.setMapOutputKeyClass(IntWritable.class);
     job.setMapOutputValueClass(stockShape.getClass());
     ShapeInputFormat.setInputPaths(job, inFile);
     job.setInputFormat(ShapeInputFormat.class);
     boolean pack = gindex != null && gindex.equals("rtree");
     job.setBoolean(SpatialSite.PACK_CELLS, pack);
 
     ClusterStatus clusterStatus = new JobClient(job).getClusterStatus();
     job.setNumMapTasks(10 * Math.max(1, clusterStatus.getMaxMapTasks()));
 
     // Set default parameters for reading input file
     SpatialSite.setShapeClass(job, stockShape.getClass());
   
     FileOutputFormat.setOutputPath(job,outPath);
     if (lindex == null) {
       job.setOutputFormat(GridOutputFormat.class);
     } else if (lindex.equals("rtree")) {
       // For now, the two types of local index are the same
       job.setOutputFormat(RTreeGridOutputFormat.class);
     } else {
       throw new RuntimeException("Unsupported local index: "+lindex);
     }
     // Copy block size from source file if it's globally indexed
     FileSystem inFs = inFile.getFileSystem(job);
     
     if (blockSize == 0) {
       GlobalIndex<Partition> globalIndex = SpatialSite.getGlobalIndex(inFs, inFile);
       if (globalIndex != null) {
         blockSize = inFs.getFileStatus(new Path(inFile, globalIndex.iterator().next().filename)).getBlockSize();
         LOG.info("Automatically setting block size to "+blockSize);
       }
     }
 
     if (blockSize != 0)
       job.setLong(SpatialSite.LOCAL_INDEX_BLOCK_SIZE, blockSize);
     SpatialSite.setCells(job, cellInfos);
     job.setBoolean(SpatialSite.OVERWRITE, overwrite);
 
     // Do not set a reduce function. Use the default identity reduce function
     job.setNumReduceTasks(Math.max(1, Math.min(cellInfos.length,
         (clusterStatus.getMaxReduceTasks() * 9 + 5) / 10)));
 
     JobClient.runJob(job);
     
     // Concatenate all master files into one file
     FileStatus[] resultFiles = outFs.listStatus(outPath, new PathFilter() {
       @Override
       public boolean accept(Path path) {
         return path.getName().contains("_master");
       }
     });
     Path masterPath = new Path(outPath, "_master" + (pack? ".rtree" : ".grid"));
     OutputStream destOut = outFs.create(masterPath);
     byte[] buffer = new byte[4096];
     for (FileStatus f : resultFiles) {
       InputStream in = outFs.open(f.getPath());
       int bytes_read;
       do {
         bytes_read = in.read(buffer);
         if (bytes_read > 0)
           destOut.write(buffer, 0, bytes_read);
       } while (bytes_read > 0);
       in.close();
       outFs.delete(f.getPath(), false);
     }
     destOut.close();
     
     // Plot an image for the partitions used in file
     Path imagePath = new Path(outPath, "_partitions.png");
     int imageSize = (int) (Math.sqrt(cellInfos.length) * 300);
     Plot.plotLocal(masterPath, imagePath, new Partition(), imageSize, imageSize, false, false, false, false);
   }
 
   public static <S extends Shape> CellInfo[] packInRectangles(FileSystem inFS,
       Path inFile, FileSystem outFS, Path outFile, long blocksize, S stockShape)
       throws IOException {
     return packInRectangles(inFS, new Path[] { inFile },
         outFS, outFile, blocksize, stockShape);
   }
   
   public static <S extends Shape> CellInfo[] packInRectangles(FileSystem fs,
       Path[] files, FileSystem outFileSystem, Path outFile, long blocksize, S stockShape)
       throws IOException {
     final Vector<Point> sample = new Vector<Point>();
     
     double sample_ratio =
         outFileSystem.getConf().getFloat(SpatialSite.SAMPLE_RATIO, 0.01f);
     long sample_size =
       outFileSystem.getConf().getLong(SpatialSite.SAMPLE_SIZE, 100*1024*1024);
     
     // 24 is the estimated size in bytes needed to store each sample point 
     long sample_count = sample_size / 24;
     
     LOG.info("Reading a sample of "+(int)Math.round(sample_ratio*100) + "%");
     ResultCollector<Point> resultCollector = new ResultCollector<Point>(){
       @Override
       public void collect(Point value) {
         sample.add(value.clone());
       }
     };
     Sampler.sampleWithRatio(fs, files, sample_ratio, sample_count,
         System.currentTimeMillis(), resultCollector, stockShape, new Point());
     LOG.info("Finished reading a sample of size: "+sample.size()+" records");
     
     long inFileSize = Sampler.sizeOfLastProcessedFile;
 
     // Compute an approximate MBR to determine the desired number of rows
     // and columns
     Rectangle approxMBR = new Rectangle(Double.MAX_VALUE, Double.MAX_VALUE,
         Double.MIN_VALUE, Double.MIN_VALUE);
     for (Point pt : sample) {
       approxMBR.expand(pt);
     }
     GridInfo gridInfo = new GridInfo(approxMBR.x1, approxMBR.y1, approxMBR.x2, approxMBR.y2);
     gridInfo.calculateCellDimensions(Math.max(1, (int)((inFileSize + blocksize / 2) / blocksize)));
     gridInfo.set(Double.MIN_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
     
     Rectangle[] rectangles = RTree.packInRectangles(gridInfo,
             sample.toArray(new Point[sample.size()]));
     CellInfo[] cellsInfo = new CellInfo[rectangles.length];
     for (int i = 0; i < rectangles.length; i++)
       cellsInfo[i] = new CellInfo(i, rectangles[i]);
     
     return cellsInfo;
   }
   
   public static <S extends Shape> void repartitionLocal(Path inFile,
       Path outFile, S stockShape, long blockSize, String gindex, String lindex,
       boolean overwrite)
           throws IOException {
     
     FileSystem inFs = inFile.getFileSystem(new Configuration());
     FileSystem outFs = outFile.getFileSystem(new Configuration());
     
     // Calculate number of partitions in output file
     // Copy blocksize from source file if it's globally indexed
     if (blockSize == 0) {
       GlobalIndex<Partition> globalIndex = SpatialSite.getGlobalIndex(inFs, inFile);
       if (globalIndex != null) {
         blockSize = inFs.getFileStatus(new Path(inFile, globalIndex.iterator().next().filename)).getBlockSize();
       }
     }
 
     // Calculate the dimensions of each partition based on gindex type
     CellInfo[] cellInfos;
     if (gindex == null) {
       throw new RuntimeException("Unsupported global index: "+gindex);
     } else if (gindex.equals("grid")) {
       Rectangle input_mbr = FileMBR.fileMBRLocal(inFs, inFile, stockShape);
       long inFileSize = FileMBR.sizeOfLastProcessedFile;
       int num_partitions = calculateNumberOfPartitions(new Configuration(),
           inFileSize, outFs, outFile, blockSize);
 
       GridInfo gridInfo = new GridInfo(input_mbr.x1, input_mbr.y1,
           input_mbr.x2, input_mbr.y2);
       gridInfo.calculateCellDimensions(num_partitions);
       cellInfos = gridInfo.getAllCells();
     } else if (gindex.equals("rtree")) {
       cellInfos = packInRectangles(inFs, inFile, outFs, outFile, blockSize, stockShape);
     } else {
       throw new RuntimeException("Unsupported global index: "+gindex);
     }
     
     repartitionLocal(inFile, outFile, stockShape, blockSize, cellInfos, gindex,
         lindex, overwrite);
   }
 
   /**
    * Repartitions a file on local machine without MapReduce jobs.
    * @param inFs
    * @param in
    * @param outFs
    * @param out
    * @param cells
    * @param stockShape
    * @param rtree
    * @param overwrite
    * @throws IOException 
    */
   public static <S extends Shape> void repartitionLocal(Path in, Path out,
       S stockShape, long blockSize, CellInfo[] cells, String gindex, String lindex,
       boolean overwrite) throws IOException {
     FileSystem inFs = in.getFileSystem(new Configuration());
     FileSystem outFs = out.getFileSystem(new Configuration());
     // Overwrite output file
     if (outFs.exists(out)) {
       if (overwrite)
         outFs.delete(out, true);
       else
         throw new RuntimeException("Output file '" + out
             + "' already exists and overwrite flag is not set");
     }
     outFs.mkdirs(out);
     
     ShapeRecordWriter<Shape> writer;
     if (lindex == null) {
       writer = new GridRecordWriter<Shape>(out, null, null, cells, gindex.equals("rtree"));
     } else if (lindex.equals("grid") || lindex.equals("rtree")) {
       writer = new RTreeGridRecordWriter<Shape>(out, null, null, cells, gindex.equals("rtree"));
       writer.setStockObject(stockShape);
     } else {
       throw new RuntimeException("Unupoorted local idnex: "+lindex);
     }
     
     FileStatus inFileStatus = inFs.getFileStatus(in);
     // Copy blocksize from source file if it's globally indexed
     if (blockSize == 0) {
       GlobalIndex<Partition> globalIndex = SpatialSite.getGlobalIndex(inFs, in);
       if (globalIndex != null) {
         blockSize = inFs.getFileStatus(new Path(in, globalIndex.iterator().next().filename)).getBlockSize();
       }
     }
     if (blockSize != 0)
       ((GridRecordWriter<Shape>)writer).setBlockSize(blockSize);
     
     long length = inFileStatus.getLen();
     FSDataInputStream datain = inFs.open(in);
     ShapeRecordReader<S> reader = new ShapeRecordReader<S>(datain, 0, length);
     Rectangle c = reader.createKey();
     
     NullWritable dummy = NullWritable.get();
     
     while (reader.next(c, stockShape)) {
       writer.write(dummy, stockShape);
     }
     writer.close(null);
   }
   
   private static void printUsage() {
     System.out.println("Builds a spatial index on an input file");
     System.out.println("Parameters (* marks required parameters):");
     System.out.println("<input file> - (*) Path to input file");
     System.out.println("<output file> - (*) Path to output file");
     System.out.println("shape:<point|rectangle|polygon> - (*) Type of shapes stored in input file");
     System.out.println("global:<grid|rtree> - (*) Type of global index");
     System.out.println("local:<grid|rtree> - Type of local index");
     System.out.println("mbr:<x,y,w,h> - MBR of data in input file");
     System.out.println("blocksize:<size> - Size of blocks in output file");
     System.out.println("-overwrite - Overwrite output file without noitce");
   }
 
   /**
 	 * Entry point to the file.
 	 * rect:<mbr> mbr of the data in file. Automatically obtained if not set. 
 	 * shape:<s> the shape to use. Automatically inferred from input file if not set.
 	 * gindex<grid:rtree> Type of global index. If not set, no global index is built.
 	 * lindex<grid:rtree> Type of local index. If not set, no local index is built.
 	 * cells-of:<filename> Use the cells of the given file for the global index.
 	 * blocksize:<size> Size of each block in indexed file in bytes.
 	 * -local: If set, the index is built on the local machine.
 	 * input filename: Input file in HDFS
 	 * output filename: Output file in HDFS
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
     CommandLineArguments cla = new CommandLineArguments(args);
     if (cla.getPaths().length < 2 || cla.getGIndex() == null) {
       printUsage();
       throw new RuntimeException("Illegal arguments");
     }
     Path inputPath = cla.getPaths()[0];
     FileSystem fs = inputPath.getFileSystem(new Configuration());
     
     if (!fs.exists(inputPath)) {
       printUsage();
       throw new RuntimeException("Input file does not exist");
     }
     
     Path outputPath = cla.getPaths()[1];
     
     String gindex = cla.getGIndex();
     String lindex = cla.getLIndex();
     
     boolean overwrite = cla.isOverwrite();
     boolean local = cla.isLocal();
     long blockSize = cla.getBlockSize();
     Shape stockShape = cla.getShape(true);
     LOG.info("Shape: "+stockShape.getClass());
     CellInfo[] cells = cla.getCells();
 
     long t1 = System.currentTimeMillis();
     if (cells != null) {
       if (local)
         repartitionLocal(inputPath, outputPath, stockShape,
             blockSize, cells, gindex, lindex, overwrite);
       else
         repartitionMapReduce(inputPath, outputPath, stockShape,
             blockSize, cells, gindex, lindex, overwrite);
     } else {
       if (local)
         repartitionLocal(inputPath, outputPath, stockShape,
             blockSize, gindex, lindex, overwrite);
       else
         repartitionMapReduce(inputPath, outputPath, stockShape,
             blockSize, gindex, lindex, overwrite);
     }
     long t2 = System.currentTimeMillis();
     System.out.println("Total indexing time in millis "+(t2-t1));
 	}
 }
