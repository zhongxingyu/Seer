 /*
  * Copyright 2013 NGDATA nv
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.ngdata.hbaseindexer.mr;
 
 import java.io.File;
 import java.util.Collections;
 
 import net.sourceforge.argparse4j.ArgumentParsers;
 import net.sourceforge.argparse4j.impl.Arguments;
 import net.sourceforge.argparse4j.impl.choice.RangeArgumentChoice;
 import net.sourceforge.argparse4j.impl.type.FileArgumentType;
 import net.sourceforge.argparse4j.inf.Argument;
 import net.sourceforge.argparse4j.inf.ArgumentGroup;
 import net.sourceforge.argparse4j.inf.ArgumentParser;
 import net.sourceforge.argparse4j.inf.ArgumentParserException;
 import net.sourceforge.argparse4j.inf.FeatureControl;
 import net.sourceforge.argparse4j.inf.Namespace;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.solr.hadoop.ForkedMapReduceIndexerTool;
 import org.apache.solr.hadoop.PathArgumentType;
 import org.apache.solr.hadoop.dedup.RetainMostRecentUpdateConflictResolver;
 
 /**
  * See http://argparse4j.sourceforge.net and for details see http://argparse4j.sourceforge.net/usage.html
  */
 class HBaseIndexerArgumentParser {
     
     private static final Log LOG = LogFactory.getLog(HBaseIndexerArgumentParser.class);
 
     /**
      * Parses the given command line arguments.
      * 
      * @return exitCode null indicates the caller shall proceed with processing, non-null indicates the caller shall
      *         exit the program with the given exit status code.
      */
     public Integer parseArgs(String[] args, Configuration conf, HBaseIndexingOptions opts) {
         assert args != null;
         assert conf != null;
         assert opts != null;
 
         if (args.length == 0) {
             args = new String[] { "--help" };
         }
 
         // TODO Add general help here
         ArgumentParser parser = ArgumentParsers.newArgumentParser(
                 "hadoop [GenericOptions]... jar hbase-indexer-mr-*-job.jar", false).defaultHelp(true);
 
         // TODO Add actual help information
         parser.addArgument("--help", "-help", "-h").help("Show this help message and exit").action(
                 Arguments.storeTrue());
 
         ArgumentGroup requiredGroup = parser.addArgumentGroup("Required arguments");
 
         Argument outputDirArg = requiredGroup.addArgument("--output-dir").metavar("HDFS_URI").type(
                 new PathArgumentType(conf) {
                     @Override
                     public Path convert(ArgumentParser parser, Argument arg, String value)
                             throws ArgumentParserException {
                         Path path = super.convert(parser, arg, value);
                         if ("hdfs".equals(path.toUri().getScheme()) && path.toUri().getAuthority() == null) {
                             // TODO: consider defaulting to hadoop's fs.default.name here or in
                             // SolrRecordWriter.createEmbeddedSolrServer()
                             throw new ArgumentParserException("Missing authority in path URI: " + path, parser);
                         }
                         return path;
                     }
                 }.verifyHasScheme()
                 .verifyIsAbsolute()
                 .verifyCanWriteParent())
                 .help(
                 "HDFS directory to write Solr indexes to. Inside there one output directory per shard will be generated. "
                         + "Example: hdfs://c2202.mycompany.com/user/$USER/test");
 
         Argument solrHomeDirArg = parser.addArgument("--solr-home-dir").metavar("DIR").type(new FileArgumentType() {
             @Override
             public File convert(ArgumentParser parser, Argument arg, String value) throws ArgumentParserException {
                 File solrHomeDir = super.convert(parser, arg, value);
                 File solrConfigFile = new File(new File(solrHomeDir, "conf"), "solrconfig.xml");
                 new FileArgumentType().verifyExists().verifyIsFile().verifyCanRead().convert(parser, arg,
                         solrConfigFile.getPath());
                 return solrHomeDir;
             }
         }.verifyIsDirectory().verifyCanRead()).required(false).help(
                 "Relative or absolute path to a local dir containing Solr conf/ dir and in particular "
                         + "conf/solrconfig.xml and optionally also lib/ dir. This directory will be uploaded to each MR task. "
                         + "Example: src/test/resources/solr/minimr");
 
         Argument updateConflictResolverArg = parser.addArgument("--update-conflict-resolver").metavar("FQCN").type(
                 String.class).setDefault(RetainMostRecentUpdateConflictResolver.class.getName()).help(
                 "Fully qualified class name of a Java class that implements the UpdateConflictResolver interface. "
                         + "This enables deduplication and ordering of a series of document updates for the same unique document "
                         + "key. For example, a MapReduce batch job might index multiple files in the same job where some of the "
                         + "files contain old and new versions of the very same document, using the same unique document key.\n"
                         + "Typically, implementations of this interface forbid collisions by throwing an exception, or ignore all but "
                         + "the most recent document version, or, in the general case, order colliding updates ascending from least "
                         + "recent to most recent (partial) update. The caller of this interface (i.e. the Hadoop Reducer) will then "
                         + "apply the updates to Solr in the order returned by the orderUpdates() method.\n"
                         + "The default RetainMostRecentUpdateConflictResolver implementation ignores all but the most recent document "
                         + "version, based on a configurable numeric Solr field, which defaults to the file_last_modified timestamp");
 
 
         Argument reducersArg = parser.addArgument("--reducers").metavar("INTEGER").type(Integer.class).choices(
                 new RangeArgumentChoice(-2, Integer.MAX_VALUE)) // TODO: also support X% syntax where X is an integer
         .setDefault(-1).help(
                 "Tuning knob that indicates the number of reducers to index into. "
                         + "0 indicates that no reducers should be used, and document should be written directly to Solr"
                         + "-1 indicates use all reduce slots available on the cluster. "
                         + "-2 indicates use one reducer per output shard, which disables the mtree merge MR algorithm. "
                         + "The mtree merge MR algorithm improves scalability by spreading load "
                         + "(in particular CPU load) among a number of parallel reducers that can be much larger than the number "
                         + "of solr shards expected by the user. It can be seen as an extension of concurrent lucene merges "
                         + "and tiered lucene merges to the clustered case. The subsequent mapper-only phase "
                         + "merges the output of said large number of reducers to the number of shards expected by the user, "
                         + "again by utilizing more available parallelism on the cluster.");
 
         Argument fanoutArg = parser.addArgument("--fanout").metavar("INTEGER").type(Integer.class).choices(
                 new RangeArgumentChoice(2, Integer.MAX_VALUE)).setDefault(Integer.MAX_VALUE).help(
                 FeatureControl.SUPPRESS);
 
         Argument maxSegmentsArg = parser.addArgument("--max-segments").metavar("INTEGER").type(Integer.class).choices(
                 new RangeArgumentChoice(1, Integer.MAX_VALUE)).setDefault(1).help(
                 "Tuning knob that indicates the maximum number of segments to be contained on output in the index of "
                         + "each reducer shard. After a reducer has built its output index it applies a merge policy to merge segments "
                         + "until there are <= maxSegments lucene segments left in this index. "
                         + "Merging segments involves reading and rewriting all data in all these segment files, "
                         + "potentially multiple times, which is very I/O intensive and time consuming. "
                         + "However, an index with fewer segments can later be merged faster, "
                         + "and it can later be queried faster once deployed to a live Solr serving shard. "
                         + "Set maxSegments to 1 to optimize the index for low query latency. "
                         + "In a nutshell, a small maxSegments value trades indexing latency for subsequently improved query latency. "
                         + "This can be a reasonable trade-off for batch indexing systems.");
 
         Argument fairSchedulerPoolArg = parser.addArgument("--fair-scheduler-pool").metavar("STRING").help(
                 "Optional tuning knob that indicates the name of the fair scheduler pool to submit jobs to. "
                         + "The Fair Scheduler is a pluggable MapReduce scheduler that provides a way to share large clusters. "
                         + "Fair scheduling is a method of assigning resources to jobs such that all jobs get, on average, an "
                         + "equal share of resources over time. When there is a single job running, that job uses the entire "
                         + "cluster. When other jobs are submitted, tasks slots that free up are assigned to the new jobs, so "
                         + "that each job gets roughly the same amount of CPU time. Unlike the default Hadoop scheduler, which "
                         + "forms a queue of jobs, this lets short jobs finish in reasonable time while not starving long jobs. "
                         + "It is also an easy way to share a cluster between multiple of users. Fair sharing can also work with "
                         + "job priorities - the priorities are used as weights to determine the fraction of total compute time "
                         + "that each job gets.");
 
         Argument dryRunArg = parser.addArgument("--dry-run").action(Arguments.storeTrue()).help(
                 "Run in local mode and print documents to stdout instead of loading them into Solr. This executes "
                         + "the morphline in the client process (without submitting a job to MR) for quicker turnaround during "
                         + "early trial & debug sessions.");
 
         Argument log4jConfigFileArg = parser.addArgument("--log4j").metavar("FILE").type(
                 new FileArgumentType().verifyExists().verifyIsFile().verifyCanRead()).help(
                 "Relative or absolute path to a log4j.properties config file on the local file system. This file "
                         + "will be uploaded to each MR task. Example: /path/to/log4j.properties");
 
         Argument verboseArg = parser.addArgument("--verbose", "-v").action(Arguments.storeTrue()).help(
                 "Turn on verbose output.");
 
         ArgumentGroup clusterInfoGroup = parser.addArgumentGroup("Cluster arguments").description(
                 "Arguments that provide information about your Solr cluster. "
                         + "If you are not using --go-live, pass the --shards argument. If you are building shards for "
                         + "a Non-SolrCloud cluster, pass the --shard-url argument one or more times. To build indexes for"
                         + " a replicated cluster with --shard-url, pass replica urls consecutively and also pass --shards. "
                         + "If you are building shards for a SolrCloud cluster, pass the --zk-host argument. "
                         + "Using --go-live requires either --shard-url or --zk-host.");
 
         Argument shardUrlsArg = clusterInfoGroup.addArgument("--shard-url").metavar("URL").type(String.class).action(
                 Arguments.append()).help(
                 "Solr URL to merge resulting shard into if using --go-live. "
                         + "Example: http://solr001.mycompany.com:8983/solr/collection1. "
                         + "Multiple --shard-url arguments can be specified, one for each desired shard. "
                         + "If you are merging shards into a SolrCloud cluster, use --zk-host instead.");
 
         Argument zkHostArg = clusterInfoGroup.addArgument("--zk-host").metavar("STRING").type(String.class).help(
                 "The address of a ZooKeeper ensemble being used by a SolrCloud cluster. "
                         + "This ZooKeeper ensemble will be examined to determine the number of output "
                         + "shards to create as well as the Solr URLs to merge the output shards into when using the --go-live option. "
                         + "Requires that you also pass the --collection to merge the shards into.\n" + "\n"
                         + "The --zk-host option implements the same partitioning semantics as the standard SolrCloud "
                         + "Near-Real-Time (NRT) API. This enables to mix batch updates from MapReduce ingestion with "
                         + "updates from standard Solr NRT ingestion on the same SolrCloud cluster, "
                         + "using identical unique document keys.\n" + "\n"
                         + "Format is: a list of comma separated host:port pairs, each corresponding to a zk "
                         + "server. Example: '127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183' If "
                         + "the optional chroot suffix is used the example would look "
                         + "like: '127.0.0.1:2181/solr,127.0.0.1:2182/solr,127.0.0.1:2183/solr' "
                         + "where the client would be rooted at '/solr' and all paths "
                         + "would be relative to this root - i.e. getting/setting/etc... "
                         + "'/foo/bar' would result in operations being run on "
                         + "'/solr/foo/bar' (from the server perspective).\n" + "\n"
                         + "If --solr-home-dir is not specified, the Solr home directory for the collection "
                         + "will be downloaded from this ZooKeeper ensemble.");
 
         Argument shardsArg = clusterInfoGroup.addArgument("--shards").metavar("INTEGER").type(Integer.class).choices(
                 new RangeArgumentChoice(1, Integer.MAX_VALUE)).help("Number of output shards to generate.");
 
         ArgumentGroup goLiveGroup = parser.addArgumentGroup("Go live arguments").description(
                 "Arguments for merging the shards that are built into a live Solr cluster. "
                         + "Also see the Cluster arguments.");
 
         Argument goLiveArg = goLiveGroup.addArgument("--go-live").action(Arguments.storeTrue()).help(
                 "Allows you to optionally merge the final index shards into a live Solr cluster after they are built. "
                         + "You can pass the ZooKeeper address with --zk-host and the relevant cluster information will be auto detected. "
                         + "If you are not using a SolrCloud cluster, --shard-url arguments can be used to specify each SolrCore to merge "
                         + "each shard into.");
 
         Argument collectionArg = goLiveGroup.addArgument("--collection").metavar("STRING").help(
                 "The SolrCloud collection to merge shards into when using --go-live and --zk-host. Example: collection1");
 
         Argument goLiveThreadsArg = goLiveGroup.addArgument("--go-live-threads").metavar("INTEGER").type(Integer.class).choices(
                 new RangeArgumentChoice(1, Integer.MAX_VALUE)).setDefault(1000).help(
                 "Tuning knob that indicates the maximum number of live merges to run in parallel at one time.");
 
         Argument indexerZkHostArg = requiredGroup.addArgument("--hbase-indexer-zk").metavar("STRING").help(
                 "The name of the ZooKeeper host where the indexer definition is stored."
                         + "Defaults to localhost, or the value of environment variable $HBASE_INDEXER_CLI_ZK "
                         + "if it is present");
 
         Argument indexNameArg = requiredGroup.addArgument("--hbase-indexer-name").metavar("STRING").help(
                 "Name of the index to be run in MapReduce mode");
 
         Argument hbaseTableNameArg = parser.addArgument("--hbase-table-name").metavar("STRING").help(
                 "Name of the HBase table containing the records to be indexed");
 
         // TODO Improve doc info on this arg
         // TODO Do more validation on this file (e.g. is it a valid hbase-indexer config?)
         Argument hbaseIndexerConfigArg = parser.addArgument("--hbase-indexer").metavar("FILE").type(
                 new FileArgumentType().verifyExists().verifyIsFile().verifyCanRead()).help(
                 "Optional HBase indexer xml configuration file");
 
         ArgumentGroup scanArgumentGroup = parser.addArgumentGroup("Scan parameters");
 
         // TODO Provide example in doc of "binary string" format
         Argument startRowArg = scanArgumentGroup.addArgument("--hbase-start-row").help(
                 "Binary string representation of start row from which to start indexing (inclusive)");
 
         // TODO Provide example in doc of "binary string" format
         Argument endRowArg = scanArgumentGroup.addArgument("--hbase-end-row").help(
                 "Binary string representation of end row prefix at which to stop indexing (exclusive)");
 
         Argument startTimeArg = scanArgumentGroup.addArgument("--hbase-start-time").metavar("LONG").help(
                 "Earliest timestamp (inclusive) in time range of HBase cells to be included for indexing");
 
         Argument endTimeArg = scanArgumentGroup.addArgument("--hbase-end-time").metavar("LONG").help(
                 "Latest timestamp (exclusive) of HBase cells to be included for indexing");
 
         Namespace ns;
         try {
             ns = parser.parseArgs(args);
         } catch (FoundHelpArgument e) {
             return 0;
         } catch (ArgumentParserException e) {
             parser.handleError(e);
             return 1;
         }
 
         opts.log4jConfigFile = (File)ns.get(log4jConfigFileArg.getDest());
         if (opts.log4jConfigFile != null) {
             PropertyConfigurator.configure(opts.log4jConfigFile.getPath());
         }
         LOG.debug("Parsed command line args: " + ns);
 
         opts.inputLists = Collections.EMPTY_LIST;
         opts.outputDir = (Path)ns.get(outputDirArg.getDest());
         opts.reducers = ns.getInt(reducersArg.getDest());
         opts.updateConflictResolver = ns.getString(updateConflictResolverArg.getDest());
         opts.fanout = ns.getInt(fanoutArg.getDest());
         opts.maxSegments = ns.getInt(maxSegmentsArg.getDest());
         opts.solrHomeDir = (File)ns.get(solrHomeDirArg.getDest());
         opts.fairSchedulerPool = ns.getString(fairSchedulerPoolArg.getDest());
         opts.isDryRun = ns.getBoolean(dryRunArg.getDest());
         opts.isVerbose = ns.getBoolean(verboseArg.getDest());
         opts.zkHost = ns.getString(zkHostArg.getDest());
         opts.shards = ns.getInt(shardsArg.getDest());
         opts.shardUrls = ForkedMapReduceIndexerTool.buildShardUrls(ns.getList(shardUrlsArg.getDest()), opts.shards);
         opts.goLive = ns.getBoolean(goLiveArg.getDest());
         opts.goLiveThreads = ns.getInt(goLiveThreadsArg.getDest());
         opts.collection = ns.getString(collectionArg.getDest());
 
         opts.hbaseIndexerConfig = (File)ns.get(hbaseIndexerConfigArg.getDest());
         opts.indexerZkHost = ns.getString(indexerZkHostArg.getDest());
         opts.indexerName = ns.getString(indexNameArg.getDest());
         opts.hbaseTableName = ns.getString(hbaseTableNameArg.getDest());
         opts.startRow = ns.getString(startRowArg.getDest());
         opts.endRow = ns.getString(endRowArg.getDest());
         opts.startTime = ns.getLong(startTimeArg.getDest());
         opts.endTime = ns.getLong(endTimeArg.getDest());
         
         try {
             try {
                 opts.evaluate();
             } catch (IllegalStateException ise) {
                throw new ArgumentParserException(ise.getMessage(), parser);
             }
         } catch (ArgumentParserException e) {
             parser.handleError(e);
             return 1;
         }
 
         return null;
     }
 
     /** Marker trick to prevent processing of any remaining arguments once --help option has been parsed */
     private static final class FoundHelpArgument extends RuntimeException {
     }
     
 }
