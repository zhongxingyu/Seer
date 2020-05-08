 package com.kontagent;
 
 
 class MatrixRPNCalculator {
 	public static void main(String[] args) throws InvalidNodeException {
 		
 		if(args.length == 0)
 	    {
 	        System.out.println("Usage: java MatrixRPNCalculator csvInputFileName");
 	        System.exit(0);
 	    }
 		
		String inputString = IOUtils.getInput(args[0]);
 		
 		SpreadSheet spreadSheet = new SpreadSheet(inputString);
 		
 		spreadSheet.processSpreadsheet();
 		
		IOUtils.sendOutput(spreadSheet.getAsCSV());
 	}
 }
