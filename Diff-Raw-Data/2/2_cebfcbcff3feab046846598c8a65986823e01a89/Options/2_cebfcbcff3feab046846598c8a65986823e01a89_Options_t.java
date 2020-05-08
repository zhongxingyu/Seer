 /*
  * Copyright (c) 2010 Concurrent, Inc. All Rights Reserved.
  *
  * Project and contact information: http://www.concurrentinc.com/
  */
 
 package cascading.load;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import cascading.load.util.Util;
 import org.kohsuke.args4j.Option;
 
 /**
  *
  */
 public class Options
   {
   boolean debugLogging = false;
   int blockSizeMB = 64;
   int numDefaultMappers = -1;
   int numDefaultReducers = -1;
   boolean mapSpecExec = false;
   boolean reduceSpecExec = false;
   int tupleSpillThreshold = 100000;
   List<String> hadoopProperties = new ArrayList<String>();
   int numMappersPerBlock = 1; // multiplier for num mappers, needs 1.2 wip for this
   int numReducersPerMapper = -1;
 
   String inputRoot;
   String outputRoot;
   String workingRoot = "working_" + System.currentTimeMillis() + "_" + (int) Math.random() * 1000;
   String statsRoot;
 
   boolean runAllLoads = false;
 
   boolean dataGenerate;
   int dataNumFiles = 100;
   int dataFileSizeMB = 100;
   int dataMaxWords = 10;
   int dataMinWords = 10;
   String dataWordDelimiter = " "; // space
   int fillBlocksPerFile = -1;
   int fillFilesPerAvailMapper = -1;
 
   boolean countSort;
 
   boolean multiJoin;
 
   public boolean isDebugLogging()
     {
     return debugLogging;
     }
 
   @Option(name = "-X", usage = "debug logging", required = false)
   public void setDebugLogging( boolean debugLogging )
     {
     this.debugLogging = debugLogging;
     }
 
   public int getBlockSizeMB()
     {
     return blockSizeMB;
     }
 
   @Option(name = "-BS", usage = "default block size", required = false)
   public void setBlockSizeMB( int blockSizeMB )
     {
     this.blockSizeMB = blockSizeMB;
     }
 
   public int getNumDefaultMappers()
     {
     return numDefaultMappers;
     }
 
   @Option(name = "-NM", usage = "default num mappers", required = false)
   public void setNumDefaultMappers( int numDefaultMappers )
     {
     this.numDefaultMappers = numDefaultMappers;
     }
 
   public int getNumDefaultReducers()
     {
     return numDefaultReducers;
     }
 
   @Option(name = "-NR", usage = "default num reducers", required = false)
   public void setNumDefaultReducers( int numDefaultReducers )
     {
     this.numDefaultReducers = numDefaultReducers;
     }
 
   public boolean isMapSpecExec()
     {
     return mapSpecExec;
     }
 
   @Option(name = "-EM", usage = "enable map side speculative execution", required = false)
   public void setMapSpecExec( boolean mapSpecExec )
     {
     this.mapSpecExec = mapSpecExec;
     }
 
   public boolean isReduceSpecExec()
     {
     return reduceSpecExec;
     }
 
   @Option(name = "-ER", usage = "enable reduce side speculative execution", required = false)
   public void setReduceSpecExec( boolean reduceSpecExec )
     {
     this.reduceSpecExec = reduceSpecExec;
     }
 
   public int getTupleSpillThreshold()
     {
     return tupleSpillThreshold;
     }
 
   @Option(name = "-TS", usage = "tuple spill threshold, default 100,000", required = false)
   public void setTupleSpillThreshold( int tupleSpillThreshold )
     {
     this.tupleSpillThreshold = tupleSpillThreshold;
     }
 
   public List<String> getHadoopProperties()
     {
     return hadoopProperties;
     }
 
   @Option(name = "-DH", usage = "optional Hadoop config job properties", required = false, multiValued = true)
   public void setHadoopProperties( String hadoopProperty )
     {
     this.hadoopProperties.add( hadoopProperty );
     }
 
   public int getNumMappersPerBlock()
     {
     return numMappersPerBlock;
     }
 
   @Option(name = "-MB", usage = "mappers per block (unused)", required = false)
   public void setNumMappersPerBlock( int numMappersPerBlock )
     {
     this.numMappersPerBlock = numMappersPerBlock;
     }
 
   public int getNumReducersPerMapper()
     {
     return numReducersPerMapper;
     }
 
   @Option(name = "-RM", usage = "reducers per mapper (unused)", required = false)
   public void setNumReducersPerMapper( int numReducersPerMapper )
     {
     this.numReducersPerMapper = numReducersPerMapper;
     }
 
   //////////////////////////////////
 
   public String getInputRoot()
     {
     return makePathDir( inputRoot );
     }
 
   @Option(name = "-I", usage = "load input data path (generated data arrives here)", required = true)
   public void setInputRoot( String inputRoot )
     {
     this.inputRoot = inputRoot;
     }
 
   public String getOutputRoot()
     {
     return makePathDir( outputRoot );
     }
 
   @Option(name = "-O", usage = "output path for load results", required = true)
   public void setOutputRoot( String outputRoot )
     {
     this.outputRoot = outputRoot;
     }
 
   public String getWorkingRoot()
     {
     return makePathDir( workingRoot );
     }
 
   @Option(name = "-W", usage = "input/output path for working files", required = false)
   public void setWorkingRoot( String workingRoot )
     {
     this.workingRoot = workingRoot;
     }
 
   public boolean hasStatsRoot()
     {
     return statsRoot != null;
     }
 
   public String getStatsRoot()
     {
     return makePathDir( statsRoot );
     }
 
   @Option(name = "-S", usage = "output path for job stats", required = false)
   public void setStatsRoot( String statsRoot )
     {
     this.statsRoot = statsRoot;
     }
 
   private String makePathDir( String path )
     {
     if( path == null || path.isEmpty() )
       return "./";
 
     if( !path.endsWith( "/" ) )
       path += "/";
 
     return path;
     }
 
   public boolean isRunAllLoads()
     {
     return runAllLoads;
     }
 
   @Option(name = "-ALL", usage = "run all available loads", required = false)
   public void setRunAllLoads( boolean runAllLoads )
     {
     this.runAllLoads = runAllLoads;
     }
 //////////////////////////////////
 
   public boolean isDataGenerate()
     {
     return dataGenerate;
     }
 
   @Option(name = "-g", aliases = {"--generate"}, usage = "generate test data", required = false)
   public void setDataGenerate( boolean dataGenerate )
     {
     this.dataGenerate = dataGenerate;
     }
 
   public int getDataNumFiles()
     {
     return dataNumFiles;
     }
 
   @Option(name = "-gf", aliases = {"--generate-num-files"}, usage = "num files to create", required = false)
   public void setDataNumFiles( int dataNumFiles )
     {
     this.dataNumFiles = dataNumFiles;
     }
 
   public int getDataFileSizeMB()
     {
     return dataFileSizeMB;
     }
 
   @Option(name = "-gs", aliases = {"--generate-file-size"}, usage = "size in MB of each file", required = false)
   public void setDataFileSizeMB( int dataFileSizeMB )
     {
     this.dataFileSizeMB = dataFileSizeMB;
     }
 
   public int getDataMaxWords()
     {
     return dataMaxWords;
     }
 
   @Option(name = "-gmax", aliases = {"--generate-max-words"}, usage = "max words per line, inclusive", required = false)
   public void setDataMaxWords( int dataMaxWords )
     {
     this.dataMaxWords = dataMaxWords;
     }
 
   public int getDataMinWords()
     {
     return dataMinWords;
     }
 
   @Option(name = "-gmin", aliases = {"--generate-min-words"}, usage = "min words per line, inclusive", required = false)
   public void setDataMinWords( int dataMinWords )
     {
     this.dataMinWords = dataMinWords;
     }
 
   public String getDataWordDelimiter()
     {
     return dataWordDelimiter;
     }
 
   @Option(name = "-gd", aliases = {"--generate-word-delimiter"}, usage = "delimiter for words", required = false)
   public void setDataWordDelimiter( String dataWordDelimiter )
     {
     this.dataWordDelimiter = dataWordDelimiter;
     }
 
   public int getFillBlocksPerFile()
     {
     return fillBlocksPerFile;
     }
 
   @Option(name = "-gbf", aliases = {"--generate-blocks-per-file"}, usage = "fill num blocks per file", required = false)
   public void setFillBlocksPerFile( int fillBlocksPerFile )
     {
     this.fillBlocksPerFile = fillBlocksPerFile;
     }
 
   public int getFillFilesPerAvailMapper()
     {
     return fillFilesPerAvailMapper;
     }
 
   @Option(name = "-gfm", aliases = {
     "--generate-files-per-mapper"}, usage = "fill num files per available mapper", required = false)
   public void setFillFilesPerAvailMapper( int fillFilesPerAvailMapper )
     {
     this.fillFilesPerAvailMapper = fillFilesPerAvailMapper;
     }
 
   ////////////////////////////////////////
 
   public boolean isCountSort()
     {
     return countSort;
     }
 
   @Option(name = "-c", aliases = {"--count-sort"}, usage = "run count sort load", required = false)
   public void setCountSort( boolean countSort )
     {
     this.countSort = countSort;
     }
 
   ////////////////////////////////////////
 
   public boolean isMultiJoin()
     {
     return multiJoin;
     }
 
   @Option(name = "-m", aliases = {"--multi-join"}, usage = "run multi join load", required = false)
   public void setMultiJoin( boolean multiJoin )
     {
     this.multiJoin = multiJoin;
     }
 
   ////////////////////////////////////////
 
   public void prepare()
     {
    if( isRunAllLoads() )
       {
       setDataGenerate( true );
       setCountSort( true );
       setMultiJoin( true );
       }
 
     if( fillBlocksPerFile != -1 )
       dataFileSizeMB = blockSizeMB * fillBlocksPerFile;
 
     if( fillFilesPerAvailMapper != -1 )
       dataNumFiles = Util.getMaxConcurrentMappers() * fillFilesPerAvailMapper;
     }
 
   @Override
   public String toString()
     {
     final StringBuilder sb = new StringBuilder();
     sb.append( "Options" );
     sb.append( "{debugLogging=" ).append( debugLogging );
     sb.append( ", blockSizeMB=" ).append( blockSizeMB );
     sb.append( ", numDefaultMappers=" ).append( numDefaultMappers );
     sb.append( ", numDefaultReducers=" ).append( numDefaultReducers );
     sb.append( ", mapSpecExec=" ).append( mapSpecExec );
     sb.append( ", reduceSpecExec=" ).append( reduceSpecExec );
     sb.append( ", tupleSpillThreshold=" ).append( tupleSpillThreshold );
     sb.append( ", hadoopProperties=" ).append( hadoopProperties );
     sb.append( ", numMappersPerBlock=" ).append( numMappersPerBlock );
     sb.append( ", numReducersPerMapper=" ).append( numReducersPerMapper );
     sb.append( ", inputRoot='" ).append( inputRoot ).append( '\'' );
     sb.append( ", outputRoot='" ).append( outputRoot ).append( '\'' );
     sb.append( ", workingRoot='" ).append( workingRoot ).append( '\'' );
     sb.append( ", statsRoot='" ).append( statsRoot ).append( '\'' );
     sb.append( ", runAllLoads=" ).append( runAllLoads );
     sb.append( ", dataGenerate=" ).append( dataGenerate );
     sb.append( ", dataNumFiles=" ).append( dataNumFiles );
     sb.append( ", dataFileSizeMB=" ).append( dataFileSizeMB );
     sb.append( ", dataMaxWords=" ).append( dataMaxWords );
     sb.append( ", dataMinWords=" ).append( dataMinWords );
     sb.append( ", dataWordDelimiter='" ).append( dataWordDelimiter ).append( '\'' );
     sb.append( ", fillBlocksPerFile=" ).append( fillBlocksPerFile );
     sb.append( ", fillFilesPerAvailMapper=" ).append( fillFilesPerAvailMapper );
     sb.append( ", countSort=" ).append( countSort );
     sb.append( ", multiJoin=" ).append( multiJoin );
     sb.append( '}' );
     return sb.toString();
     }
   }
