 import java.io.IOException;
 import java.util.logging.Level;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Writable;
 //import org.apache.hadoop.io.SequenceFile;
 //import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 
 //import org.apache.hadoop.mapreduce.Mapper.Context;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 
 public class BetaMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> extends Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {
 
 	Configuration conf = new Configuration();
 	Job nestedJob;
 	
 	Path nestedJobInputPath;
 	Path nestedJobOutputPath;
 	
	Path innerWorks;
 	
 	String writerType = null;
 	String readerType = "SequenceFile";
 	String condition = null;
 	
 	String nestedLevel = null;
 	
 	WriterFactory writerFactory = new WriterFactory();
 	CommonWriterUtils writer;
 	
 	ReaderFactory readerFactory = new ReaderFactory();
 	CommonReaderUtils reader;
 	
 	// TODO Add commented structure
 	@Override
 	public void run(Context context) throws IOException, InterruptedException{
 		
		context.getWorkingDirectory();
		
 		setup(context);
 			try {
 				setupWorkflow(context);
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		cleanup(context);
 	}
 
 	// TODO Add commented structure
 	private void setupWorkflow(Context context) throws IOException, InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException {
 		
		innerWorks = FileOutputFormat.getOutputPath(context).getParent();
		
		//System.out.println("Stuff: " + innerWorks);
 		
 		//conf.set("hadoop.job.ugi", context.getConfiguration().getResource("hadoop.job.ugi").toString());
 		
 		
 		//System.out.println("Fuck knows what i'm looking for: " + );
 		
 		nestedJob = new Job(conf);
 		
 		setupNestedJob(nestedJob, conf, condition);
 		
 		// TODO implement an uniform naming scheme
 		//nestedJob.setJobName("Layer-2");
 		
 			setupInternalWR(context);
 		
 		nestedLevel = nestedJob.getJobName();	
 			
 		//condition = "something";
 
 		// Implement Condition thing;	
 		setupNestedMap(context);
 			
 		//
 		
 		setupNestedJob(nestedJob,conf, condition);
 		
 		condition = "something";
 		
 		setupNesting(nestedJob, conf, condition);
 		
 		while(executeNestedJob(context) != true){
 			// busy wait until nesting is completed
 		}
 		
 		try {
 			setupNormalMap(context);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	
 	// TODO Add commented structure
 	protected void setupNestedJob(Job job, Configuration conf, String condition) throws IOException{
 		setupNesting(nestedJob, conf, condition);
 	}
 	
 	// TODO Add commented structure
 	protected void setupNesting(Job job, Configuration conf, String condition ) throws IOException{
 		// User provided details for nested job configuration
 	}
 	
 	
 	//TODO document method behavior
 	
 	private void setupInternalWR(Context context)
 		throws IOException, ClassNotFoundException, InstantiationException,
 							IllegalAccessException, InterruptedException{
 		
 		//nestedJobInputPath = writer.getPath();
		
 		nestedJobInputPath = new Path(innerWorks + "/inceptions/" + nestedJob.getJobName() + "/" + context.getTaskAttemptID().toString());
		System.out.println(nestedJobInputPath);
 		//nestedJobInputPath = new Path("/tmp/outputs/1");
 		//nestedJobOutputPath = new Path("/tmp/outputs/2");
 		//nestedJobOutputPath = new Path(innerWorks + "/outputs/" + nestedJob.getJobName() + "/" + context.getTaskAttemptID());
 		
 		
 		//SequenceFileInputFormat.addInputPath(nestedJob, nestedJobInputPath);
 		//SequenceFileOutputFormat.setOutputPath(nestedJob, nestedJobOutputPath);
 		
 		
 		if(SequenceFileInputFormat.class.getName() == nestedJob.getInputFormatClass().getName()){
     		SequenceFileInputFormat.addInputPath(nestedJob, nestedJobInputPath);
     		writerType = "SequenceFile";
 		}
     				
 		if(TextInputFormat.class.getName() == nestedJob.getInputFormatClass().getName()){
 			FileInputFormat.addInputPath(nestedJob, nestedJobInputPath);
 			writerType = "BufferFile";
 		}
 		
 		
 		//FIXME either fix or remove bufered output all together 
 		
 	}
 	
 	protected void setupNestedMap(Context context) throws IOException, InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException{
 
 		try {
 			System.out.println("In the writer and the type is : " + writerType);
 			writer = writerFactory.makeWriter(context, innerWorks, nestedLevel, writerType);
 			//writer = new NestedWriterSF<KEYIN, VALUEIN>(context, innerWorks, nestedJob.getJobName());
 			//writer = writerFactory.makeWriter(context, writerType);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		  
 		  while(context.nextKeyValue()){
 			  nestedMap(context.getCurrentKey(), context.getCurrentValue(), condition);
 		  }
 		  
 	    writer.close();
 		 
 	    /* if the user supplies a body for the nested job running run this: */
 	    
 	 //   while(executeNestedJob() != true){
 	    	// we busy wait for the nested job to finalize;
 	 //   }
 	}
 	
 	protected void nestedMap (KEYIN key, VALUEIN value, String condition) throws IOException, InterruptedException{
 		System.out.println(key + " / " + value);
 	
 		writer.write(key, value);
 	}
 	
 	protected boolean executeNestedJob(Context context) throws ClassNotFoundException{
 		
 		// TODO - maybe change condition to something else (ex: anything but null)
 		if (condition != null){	
 
 			nestedJobOutputPath = new Path(innerWorks + "/outputs/" + nestedJob.getJobName() + "/" + context.getTaskAttemptID());
 
 			if(SequenceFileOutputFormat.class.getName() == nestedJob.getOutputFormatClass().getName()){
 				SequenceFileOutputFormat.setOutputPath(nestedJob, nestedJobOutputPath);
 				readerType = "SequenceFile";
 			}
 
 			//FIXME either fix or remove buffered output all together 
 
 			if(TextOutputFormat.class.getName() == nestedJob.getOutputFormatClass().getName()){
 				FileOutputFormat.setOutputPath(nestedJob, nestedJobOutputPath);
 				readerType = "BufferFile";
 			}
 
 
 			try{
 				nestedJob.waitForCompletion(true);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return true;
 	}
 	
 	private void setupNormalMap(Context context) throws Exception {
 		
 		//try{
 		System.out.println("In the reader and the reader is of type: " + readerType);
 		//reader = readerFactory.makeReader(context, readerType);
 		
 		System.out.println("Condition is :  " + condition);
 		
 		if (condition != null){
 			//reader = readerFactory.makeReader(context, innerWorks, nestedJob.getJobName(), readerType, condition);	
 			
 			FileSystem fs = FileSystem.get(conf);
 			Path dir = new Path(innerWorks + "/outputs/" + nestedJob.getJobName() + "/" + context.getTaskAttemptID().toString() + "/");
 			FileStatus[]stats = fs.listStatus(dir);
 			String checker = innerWorks + "/outputs/" + nestedJob.getJobName() + "/" + context.getTaskAttemptID().toString() + "/" + "part-r-";
 			
 			for(FileStatus stat : stats){
 				//System.out.println(context.getTaskAttemptID().toString());
 				//System.out.println("There is hope yet! - " + stat.getPath().toUri().getPath().toString());
 				
 				if(stat.getPath().toUri().getPath().toString().contains(checker)){
 					System.out.println("There is hope yet! - " + stat.getPath().toUri().getPath().toString());
 					reader = readerFactory.makeReader(context, stat.getPath().toUri().getPath().toString(), readerType, condition);
 					
 					while(reader.next()){
 						map(reader.getKey(), reader.getValue(), context);
 					}	
 				}
 
 			}
 
 		}	
 		else{
 			
 			reader = readerFactory.makeReader(context, innerWorks, nestedJob.getJobName(), readerType, condition);
 			
 			FileSystem fs = FileSystem.get(conf);
 			Path dir = new Path(innerWorks + "/outputs/" + nestedJob.getJobName() + " / " + context.getTaskAttemptID().toString());
 			FileStatus[]stats = fs.listStatus(dir);
 			
 			for(FileStatus stat : stats){
 				System.out.println("There is hope yet! - " + stat.getPath().toUri().getPath());
 			}
 			
 			while(reader.next()){
 				map(reader.getKey(), reader.getValue(), context);
 			}
 			
 		}
 		
 //	}catch (Exception ex){
 	//	ex.printStackTrace();
 //	}
 
 //	while(reader.next()){
 	//	reduce(reader.getKey(), reader.getValue(), context);
 //	}
 		
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected void map(Writable key, Writable value, Context context) throws IOException, InterruptedException{
 		
 		System.out.println(key.getClass().getName() +" / "+ value.getClass().getName());
 		
 		context.write((KEYOUT) key, (VALUEOUT) value);
 	}
 	
 	
 	
 
 }
