 package com.mapr.util;
 
 import org.apache.pig.*;
 import org.apache.pig.data.*;
 import java.io.*;
 
 public class MinLoader extends LoadFunc implements LoadMetadata
 {
 	org.apache.hadoop.mapreduce.RecordReader reader;
 
 	public MinLoader() {
 		/* We don't take any params, so nothing to do in the constructor. */
 
 	}
 
 	/* Basic LoadFunc implementation */
 	
 	/** 
 	 * Returns a {@link org.apache.hadoop.mapreduce.lib.input.TextInputFormat TextInputFormat} object.  This is usually suitable for line-based files.
 	 * If your application needs more than a single line of context, you'll need to implement
 	 * a {@link org.apache.hadoop.mapreduce.RecordReader RecordReader} that knows how to break up files, and an {@link org.apache.hadoop.mapreduce.InputFormat InputFormat} that returns that
 	 * {@link org.apache.hadoop.mapreduce.RecordReader RecordReader}.
 	 */
 	@Override
 	public org.apache.hadoop.mapreduce.InputFormat getInputFormat() {
 		return new org.apache.hadoop.mapreduce.lib.input.TextInputFormat();
 	}
 
	@Override
 	public void prepareToRead(org.apache.hadoop.mapreduce.RecordReader reader, org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit split)
 	{
 		this.reader = reader;
 		return;
 	}
 
 	@Override
 	public void setLocation(String location, org.apache.hadoop.mapreduce.Job job) throws IOException
 	{
 		org.apache.hadoop.mapreduce.lib.input.FileInputFormat.setInputPaths(job,location);
 		return;
 	}
 
 	@Override
 	public Tuple getNext() throws IOException {
		Tuple output = TupleFactory.getInstance().newTuple(1);
 
 		try {
 			if(this.reader.nextKeyValue()) {
				org.apache.hadoop.io.Text rawText = (org.apache.hadoop.io.Text)reader.getCurrentValue();
 				output.set(0, rawText.toString());	
 				return output;
 			}
 		} catch (Exception e) {
			/* No handling on the exception, just ignore the record */
 		}
     	return null;
 	}
 
 	/* LoadMetadata Implementation */
 
 	/**
 	 * This loader is intended for record-based files, and does not support partitions.
 	 * @return Always returns null.
 	 */
	public String[] getPartitionKeys(String location, org.apache.hadoop.mapreduce.Job job) throws IOException
 	{
 		return null;	
 	}
 
 	/**
 	 * This loader is intended for record-based files, and does not support partitions.
 	 */
	public void setPartitionFilter(Expression partitionFilter) throws IOException
 	{
 	 	return;   
 	}
 
 	/**
 	 * This loader does not support statistics.
 	 * @return Always returns null.
 	 */
 	public ResourceStatistics getStatistics(String location, org.apache.hadoop.mapreduce.Job job) throws IOException
 	{
 		return null;
 	}
 
 	/**
 	 * This loader supports a schema containing a single CHARARRAY field.
 	 * @return A ResourceSchema containing a single CHARARRAY field named 'text'.
 	 */
 	public ResourceSchema getSchema(String location, org.apache.hadoop.mapreduce.Job job) throws IOException
 	{
 		return new ResourceSchema(
 				new org.apache.pig.impl.logicalLayer.schema.Schema(
 					new org.apache.pig.impl.logicalLayer.schema.Schema.FieldSchema("text", org.apache.pig.data.DataType.CHARARRAY)
 					)
 				);
 	}
 
 }
