 package edu.berkeley.icsi.wlgen;
 
 import eu.stratosphere.nephele.fs.Path;
 import eu.stratosphere.pact.common.contract.FileDataSink;
 import eu.stratosphere.pact.common.contract.FileDataSource;
 import eu.stratosphere.pact.common.contract.MapContract;
 import eu.stratosphere.pact.common.contract.ReduceContract;
 import eu.stratosphere.pact.common.io.RecordOutputFormat;
 import eu.stratosphere.pact.common.io.TextInputFormat;
 import eu.stratosphere.pact.common.plan.Plan;
 import eu.stratosphere.pact.common.type.base.PactString;
 
 final class PactPlanGenerator {
 
 	static Plan toPactPlan(final String basePath, final MapReduceJob mapReduceJob) {
 
 		System.out.println("Generating plan for job " + mapReduceJob.getJobID());
 		final String inputFilePath = basePath + Path.SEPARATOR + mapReduceJob.getInputFile().getName();
 		final String outputFilePath = basePath + Path.SEPARATOR + mapReduceJob.getOutputFile().getName() + "_out";
 
 		final FileDataSource source = new FileDataSource(TextInputFormat.class, inputFilePath, "Input");
 		source.setParameter(TextInputFormat.CHARSET_NAME, "ASCII");
 		source.setDegreeOfParallelism(mapReduceJob.getNumberOfMapTasks());
 
 		final MapContract mapper = MapContract.builder(MapTask.class)
 			.input(source)
 			.dataDistribution(new ReduceDataDistribution(mapReduceJob.getDataDistribution()))
 			.name("Map")
 			.build();
 		mapper.setDegreeOfParallelism(mapReduceJob.getNumberOfMapTasks());
 		mapper.getParameters().setString("pact.out.distribution.class", "daniel");
 
 		final ReduceContract reducer = new ReduceContract.Builder(ReduceTask.class, PactString.class, 0)
 			.input(mapper)
 			.name("Reduce")
 			.build();
 
 		final FileDataSink out = new FileDataSink(RecordOutputFormat.class, outputFilePath, reducer, "Output");
 
 		RecordOutputFormat.configureRecordFormat(out)
 			.recordDelimiter('\n')
 			.fieldDelimiter(' ')
 			.lenient(true)
 			.field(PactString.class, 0)
 			.field(PactString.class, 1);
 
 		final Plan plan = new Plan(out, mapReduceJob.getJobID());
 
 		return plan;
 	}
 }
