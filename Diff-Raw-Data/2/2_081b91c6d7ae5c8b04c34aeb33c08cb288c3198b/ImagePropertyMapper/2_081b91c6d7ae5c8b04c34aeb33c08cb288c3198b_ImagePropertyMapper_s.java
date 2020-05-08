 package dev.cloud.group07.backend;
 import java.io.IOException;
 
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.lib.input.FileSplit;
 
 public class ImagePropertyMapper extends Mapper<LongWritable , Text , Text , Text> {
 	private Writer writer;
 	
 	@Override
 	public void setup(Context context) throws IOException , InterruptedException {
 		String filePath = ((FileSplit)context.getInputSplit()).getPath().toString();
 		
 		if(filePath.indexOf(FilePathConstants.FILE_BASE + "/" + FilePathConstants.TSUKUREPO_COUNT_FILE_NAME) > 0 ) {
 			writer = new TsukurepoCountWriter();
 		} else if (filePath.indexOf(FilePathConstants.FILE_BASE + "/" + FilePathConstants.PROCESS_COUNT_FILE_NAME) > 0) {
 		    writer = new ProcessCountWriter();
         } else if (filePath.indexOf(FilePathConstants.FILE_BASE + "/" + FilePathConstants.ALL_FILE_NAME) > 0) {
             writer = new AllWriter();
 		} else if (filePath.indexOf(FilePathConstants.FILE_BASE + "/" + FilePathConstants.MATERIAL_COUNT_FILE_NAME) > 0) {
             writer = new MaterialWriter();
 		} else {
 			throw new RuntimeException("Invalid Input File : " + filePath);
 		}
 	}
 	
 	
 	@Override
 	public void map(LongWritable keyIn , Text valueIn , Context context) throws IOException , InterruptedException {
 		writer.write(keyIn, valueIn , context);
 	}
 	
 	
 	private interface Writer {
 		public void write(LongWritable keyIn , Text valueIn , Context context) throws IOException , InterruptedException;
 	}
 
 	// input: ID,report数
 	private class TsukurepoCountWriter implements Writer {
         @Override
         public void write(LongWritable keyIn , Text valueIn , Context context) throws IOException , InterruptedException {
             String[] recipeIDAndReportNum = valueIn.toString().split(",");
             if (Integer.valueOf(recipeIDAndReportNum[1]) > 50) {
             	recipeIDAndReportNum[1] = "4";
             } else if (Integer.valueOf(recipeIDAndReportNum[1]) > 10) {
             	recipeIDAndReportNum[1] = "3";
             } else if (Integer.valueOf(recipeIDAndReportNum[1]) > 5) {
             	recipeIDAndReportNum[1] = "2";
             } else if (Integer.valueOf(recipeIDAndReportNum[1]) > 0) {
             	recipeIDAndReportNum[1] = "1";
             } else {
             	recipeIDAndReportNum[1] = "0";
             }
             context.write(new Text(recipeIDAndReportNum[0] + "#a"), new Text("reportNum:\""+recipeIDAndReportNum[1]+"\""));
         }
     }
 
     // input: ID,step数
 	private class ProcessCountWriter implements Writer {
 		@Override
 		public void write(LongWritable keyIn , Text valueIn , Context context) throws IOException , InterruptedException {
 			String[] recipeIDAndStepNum = valueIn.toString().split(",");
 			context.write(new Text(recipeIDAndStepNum[0] + "#b"), new Text("stepNum:\""+recipeIDAndStepNum[1]+"\""));
 		}
 	}
 	
 	// input: ID\t...
 	// output: [ID,(imagePath,time,cost)]
     private class AllWriter implements Writer {
         
         private Text keyOut = new Text();
         private Text valueOut = new Text();
         
         @Override
         public void write(LongWritable keyIn , Text valueIn , Context context) throws IOException , InterruptedException {
             String[] recipeInfo = valueIn.toString().split("\t");
             keyOut.set(recipeInfo[0]);
            valueOut.set("smallCategory:\""+recipeInfo[4]+"\",title:\""+recipeInfo[5]+"\",imagePath:\""+recipeInfo[8]+"\",tags:\""+recipeInfo[10]+" "+recipeInfo[11]+" "+recipeInfo[12]+" "+recipeInfo[13]+"\",jikan:\""+recipeInfo[15]+"\",okane:"+recipeInfo[17]+"\"");
             context.write(keyOut, valueOut);
         }
     }
     
     private class MaterialWriter implements Writer {
     	private Text keyOut = new Text();
         private Text valueOut = new Text();
         
         @Override
         public void write(LongWritable keyIn , Text valueIn , Context context) throws IOException , InterruptedException {
         	String[] recipeIDAndMaterials = valueIn.toString().split(",");
 			context.write(new Text(recipeIDAndMaterials[0]), new Text("materials:\""+recipeIDAndMaterials[1]+"\""));
         }
     }
 }
