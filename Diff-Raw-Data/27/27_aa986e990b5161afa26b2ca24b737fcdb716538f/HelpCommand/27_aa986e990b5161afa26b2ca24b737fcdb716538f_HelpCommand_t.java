 /**
  * 
  */
 package com.mimpidev.podsalinan.cli.options;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.cli.CLIOption;
 import com.mimpidev.podsalinan.cli.ReturnCall;
 import com.mimpidev.podsalinan.cli.options.help.*;
 
 /**
  * @author sbell
  *
  */
 public class HelpCommand extends CLIOption {
 
 	private Map<String, CLIOption> options;
 	/**
 	 * @param newData
 	 */
 	public HelpCommand(DataStorage newData) {
 		super(newData);
 		options = new HashMap<String, CLIOption>();
 		options.put("", new Help(newData));
 		options.put("select", new HelpSelect(newData));
 		options.put("list", new HelpList(newData));
 		options.put("set", new HelpSet(newData));
 	}
 
 	@Override
 	public ReturnCall execute(String command) {
 		System.out.println("");
		String subOption;
        if (command.split(" ").length>1)
		   subOption=command.split(" ", 2)[1];
        else
        	subOption="";
        
 		
 		if (!options.containsKey(subOption)){
             System.out.println("Error: Invalid Help request.");
     		System.out.println("");
 			((CLIOption)options.get("")).execute(command);
 		} else {
 		    ((CLIOption)options.get(subOption)).execute(command);
 		}
 		
 		System.out.println("");
 		System.out.println("");
 		return null;
 	}
 }
