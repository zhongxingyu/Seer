 package edu.uci.ics.algebricks.examples.piglet.metadata;
 
 import java.util.List;
 
 import edu.uci.ics.algebricks.api.constraints.AlgebricksAbsolutePartitionConstraint;
 import edu.uci.ics.algebricks.api.constraints.AlgebricksPartitionConstraint;
 import edu.uci.ics.algebricks.api.data.IPrinterFactory;
 import edu.uci.ics.algebricks.api.exceptions.AlgebricksException;
 import edu.uci.ics.algebricks.compiler.algebra.base.LogicalVariable;
 import edu.uci.ics.algebricks.compiler.algebra.metadata.IDataSink;
 import edu.uci.ics.algebricks.compiler.algebra.metadata.IDataSource;
 import edu.uci.ics.algebricks.compiler.algebra.metadata.IDataSourceIndex;
 import edu.uci.ics.algebricks.compiler.algebra.metadata.IMetadataProvider;
 import edu.uci.ics.algebricks.compiler.algebra.operators.logical.IOperatorSchema;
 import edu.uci.ics.algebricks.examples.piglet.types.Type;
 import edu.uci.ics.algebricks.runtime.hyracks.base.IPushRuntimeFactory;
 import edu.uci.ics.algebricks.runtime.hyracks.jobgen.impl.JobGenContext;
 import edu.uci.ics.algebricks.runtime.hyracks.operators.std.SinkWriterRuntimeFactory;
 import edu.uci.ics.algebricks.runtime.hyracks.writers.PrinterBasedWriterFactory;
 import edu.uci.ics.algebricks.utils.Pair;
 import edu.uci.ics.hyracks.api.dataflow.IOperatorDescriptor;
 import edu.uci.ics.hyracks.api.dataflow.value.ISerializerDeserializer;
 import edu.uci.ics.hyracks.api.dataflow.value.RecordDescriptor;
 import edu.uci.ics.hyracks.api.job.JobSpecification;
 import edu.uci.ics.hyracks.dataflow.common.data.marshalling.FloatSerializerDeserializer;
 import edu.uci.ics.hyracks.dataflow.common.data.marshalling.IntegerSerializerDeserializer;
 import edu.uci.ics.hyracks.dataflow.common.data.marshalling.UTF8StringSerializerDeserializer;
 import edu.uci.ics.hyracks.dataflow.common.data.parsers.FloatParserFactory;
 import edu.uci.ics.hyracks.dataflow.common.data.parsers.IValueParserFactory;
 import edu.uci.ics.hyracks.dataflow.common.data.parsers.IntegerParserFactory;
 import edu.uci.ics.hyracks.dataflow.common.data.parsers.UTF8StringParserFactory;
 import edu.uci.ics.hyracks.dataflow.std.file.ConstantFileSplitProvider;
 import edu.uci.ics.hyracks.dataflow.std.file.DelimitedDataTupleParserFactory;
 import edu.uci.ics.hyracks.dataflow.std.file.FileScanOperatorDescriptor;
 import edu.uci.ics.hyracks.dataflow.std.file.FileSplit;
 import edu.uci.ics.hyracks.dataflow.std.file.IFileSplitProvider;
 import edu.uci.ics.hyracks.dataflow.std.file.ITupleParserFactory;
 
 public class PigletMetadataProvider implements IMetadataProvider<String, String> {
     @Override
     public IDataSource<String> findDataSource(String id) throws AlgebricksException {
         return null;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public Pair<IOperatorDescriptor, AlgebricksPartitionConstraint> getScannerRuntime(IDataSource<String> dataSource,
             List<LogicalVariable> scanVariables, List<LogicalVariable> projectVariables, boolean projectPushed,
             JobGenContext context, JobSpecification jobSpec) throws AlgebricksException {
         PigletFileDataSource ds = (PigletFileDataSource) dataSource;
 
         FileSplit[] fileSplits = ds.getFileSplits();
         String[] locations = new String[fileSplits.length];
         for (int i = 0; i < fileSplits.length; ++i) {
             locations[i] = fileSplits[i].getNodeName();
         }
         IFileSplitProvider fsp = new ConstantFileSplitProvider(fileSplits);
 
         Object[] colTypes = ds.getSchemaTypes();
         IValueParserFactory[] vpfs = new IValueParserFactory[colTypes.length];
         ISerializerDeserializer[] serDesers = new ISerializerDeserializer[colTypes.length];
 
         for (int i = 0; i < colTypes.length; ++i) {
             Type colType = (Type) colTypes[i];
             IValueParserFactory vpf;
             ISerializerDeserializer serDeser;
             switch (colType.getTag()) {
                 case INTEGER:
                     vpf = IntegerParserFactory.INSTANCE;
                     serDeser = IntegerSerializerDeserializer.INSTANCE;
                     break;
 
                 case CHAR_ARRAY:
                     vpf = UTF8StringParserFactory.INSTANCE;
                     serDeser = UTF8StringSerializerDeserializer.INSTANCE;
                     break;
 
                 case FLOAT:
                     vpf = FloatParserFactory.INSTANCE;
                     serDeser = FloatSerializerDeserializer.INSTANCE;
                     break;
 
                 default:
                     throw new UnsupportedOperationException();
             }
             vpfs[i] = vpf;
             serDesers[i] = serDeser;
         }
 
         ITupleParserFactory tpf = new DelimitedDataTupleParserFactory(vpfs, ',');
         RecordDescriptor rDesc = new RecordDescriptor(serDesers);
 
         IOperatorDescriptor scanner = new FileScanOperatorDescriptor(jobSpec, fsp, tpf, rDesc);
         AlgebricksAbsolutePartitionConstraint constraint = new AlgebricksAbsolutePartitionConstraint(locations);
         return new Pair<IOperatorDescriptor, AlgebricksPartitionConstraint>(scanner, constraint);
     }
 
     @Override
     public boolean scannerOperatorIsLeaf(IDataSource<String> dataSource) {
         return true;
     }
 
     @Override
     public Pair<IPushRuntimeFactory, AlgebricksPartitionConstraint> getWriteFileRuntime(IDataSink sink,
             int[] printColumns, IPrinterFactory[] printerFactories, RecordDescriptor inputDesc)
             throws AlgebricksException {
         PigletFileDataSink ds = (PigletFileDataSink) sink;
         FileSplit[] fileSplits = ds.getFileSplits();
         String[] locations = new String[fileSplits.length];
         for (int i = 0; i < fileSplits.length; ++i) {
             locations[i] = fileSplits[i].getNodeName();
         }
         IPushRuntimeFactory prf = new SinkWriterRuntimeFactory(printColumns, printerFactories, fileSplits[0]
                 .getLocalFile().getFile(), PrinterBasedWriterFactory.INSTANCE, inputDesc);
         AlgebricksAbsolutePartitionConstraint constraint = new AlgebricksAbsolutePartitionConstraint(locations);
         return new Pair<IPushRuntimeFactory, AlgebricksPartitionConstraint>(prf, constraint);
     }
 
     @Override
     public IDataSourceIndex<String, String> findDataSourceIndex(String indexId, String dataSourceId)
             throws AlgebricksException {
         return null;
     }
 
     @Override
     public Pair<IOperatorDescriptor, AlgebricksPartitionConstraint> getWriteResultRuntime(String datasetName,
             IOperatorSchema propagatedSchema, List<LogicalVariable> keys, LogicalVariable payLoadVar,
             JobGenContext context, JobSpecification jobSpec) throws AlgebricksException {
         return null;
     }
 
     @Override
     public Pair<IOperatorDescriptor, AlgebricksPartitionConstraint> getInsertRuntime(String datasetName,
             IOperatorSchema propagatedSchema, List<LogicalVariable> keys, LogicalVariable payLoadVar,
             RecordDescriptor recordDesc, JobGenContext context, JobSpecification jobSpec) throws AlgebricksException {
         return null;
     }
 
     @Override
     public Pair<IOperatorDescriptor, AlgebricksPartitionConstraint> getDeleteRuntime(String datasetName,
            IOperatorSchema propagatedSchema, List<LogicalVariable> keys, LogicalVariable payload,
            RecordDescriptor recordDesc, JobGenContext context, JobSpecification jobSpec) throws AlgebricksException {
         return null;
     }
 
     @Override
     public Pair<IOperatorDescriptor, AlgebricksPartitionConstraint> getIndexInsertRuntime(String datasetName,
             String indexName, IOperatorSchema propagatedSchema, List<LogicalVariable> primaryKeys,
             List<LogicalVariable> secondaryKeys, RecordDescriptor recordDesc, JobGenContext context,
             JobSpecification spec) throws AlgebricksException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Pair<IOperatorDescriptor, AlgebricksPartitionConstraint> getIndexDeleteRuntime(String datasetName,
             String indexName, IOperatorSchema propagatedSchema, List<LogicalVariable> primaryKeys,
             List<LogicalVariable> secondaryKeys, RecordDescriptor recordDesc, JobGenContext context,
             JobSpecification spec) throws AlgebricksException {
         // TODO Auto-generated method stub
         return null;
     }
 }
