 package com.pardot.rhombus.cli.commands;
 
 import com.google.common.collect.Maps;
 import com.pardot.rhombus.RhombusException;
 import com.pardot.rhombus.UpdateProcessor;
 import com.pardot.rhombus.cobject.CQLGenerationException;
 import com.pardot.rhombus.cobject.IndexUpdateRowKey;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: Rob Righter
  * Date: 9/5/13
  */
 public class UpdateCleaner extends RcliWithCassandraConfig{
 
 	public Options getCommandOptions(){
 		Options ret = super.getCommandOptions();
 		Option process = new Option( "p", "Process update fixes" );
 		Option list = OptionBuilder.withArgName("timeInNanos")
 				.hasArg()
 				.withDescription("List all updates in the system where the same object has been updated within timeInNanos")
 				.create( "listUpdates" );
 		ret.addOption(process);
 		ret.addOption(list);
 		return ret;
 	}
 
 	public void displayListResults(List<Map<String,Object>> results){
 		System.out.println(" Difference | Type | Instance | New Values | Old Values");
 		System.out.println("--------------------------------------------------------");
 		for(Map<String,Object> item : results){
			String difference = ((Long)item.get("difference")).toString();
 			String objName = ((IndexUpdateRowKey)item.get("rowkey")).getObjectName();
 			String instanceId = ((IndexUpdateRowKey)item.get("rowkey")).getInstanceId().toString();
			String newValue = (String)item.get("new-item");
			String oldValue = (String)item.get("old-item");
 
 			System.out.println(String.format("%s  %s  %s  %s  %s",
 					difference,
 					objName,
 					instanceId,
 					newValue,
 					oldValue));
 		}
 	}
 
 	public void executeCommand(CommandLine cl){
 		super.executeCommand(cl);
 
 		try{
 			getConnectionManager().setDefaultKeyspace(keyspaceDefinition);
 			String strategy = cl.getOptionValue("strategy");
 			UpdateProcessor up = new UpdateProcessor(getConnectionManager().getObjectMapper());
 
 			if(cl.hasOption("listUpdates")){
 				String timestr = cl.getOptionValue("listUpdates");
 				long time = Long.parseLong(timestr);
 				displayListResults(up.getUpdatesThatHappenedWithinTimeframe(time));
 			}
 
 			if(cl.hasOption("p")){
 				up.process();
 			}
 
 		}
 		catch (IOException e){
 			System.out.println("Error encountered processing updates: " + e.getMessage());
 		}
 
 	}
 
 }
