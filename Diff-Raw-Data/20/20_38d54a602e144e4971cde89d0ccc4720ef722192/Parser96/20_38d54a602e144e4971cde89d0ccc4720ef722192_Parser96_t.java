 package net.bioclipse.brunn.results.parser96;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Scanner;
 import java.util.logging.Logger;
 
 import net.bioclipse.brunn.Springcontact;
 import net.bioclipse.brunn.business.operation.IOperationManager;
 import net.bioclipse.brunn.pojos.Plate;
 import net.bioclipse.brunn.pojos.User;
 import net.bioclipse.brunn.results.PlateRead;
 import net.bioclipse.brunn.results.ResultParser;
 import net.bioclipse.brunn.results.orcaParser.OrcaParser.OrcaPlateRead;
 
 public class Parser96 implements ResultParser {
 
 	private Scanner scanner;
 	private List<PlateRead> plateReads;
 
 	public Parser96(File file) throws FileNotFoundException {
 		scanner = new Scanner(file);
 		plateReads = new ArrayList<PlateRead>();
 	}
 
 	public List<PlateRead> getPlatesInFile() {
 		while( scanner.hasNextLine() ) {
 			try {
 				plateReads.add( parseAPlateRead() );
 			}
 			catch( Exception e ) {
 				
 			}
 		}
 		return plateReads;
 	}
 	
 	
 	private PlateRead parseAPlateRead() {
 		
 		PlateRead plateRead = new PlateRead96();
 		plateRead.setError("Parsing returned early");
 		boolean plateReadParsed = false;
 		List< List<Double> > rows = new ArrayList< List<Double> >();
 		
 		while( !plateReadParsed ) {
 			String row = scanner.nextLine();
 			List<Double> rowValues = new ArrayList<Double>();
 			if( row.matches("^[A-H](\\s+\\S+)+$") ) {
 				for ( String s : row.split("\\s+") ) {
 					try {
 						rowValues.add( Double.parseDouble( s.trim() ) );
 					}
 					catch (NumberFormatException e) {
 					}
 				}
 			}
 			else if ( row.matches("^(\\s+\\d+,)+.*") ) {
 				for ( String s : row.split(",\\s") ) {
 					try {
 						rowValues.add( Double.parseDouble( s.trim() ) );
 					}
 					catch (NumberFormatException e) {
 					}
 				}
 			}
			else if ( row.contains(";") ) {
				for ( String s : row.split(";") ) {
					try {
						if(s.contains(",")) {
							s = s.replace(",", ".");
						}
						rowValues.add( Double.parseDouble( s.trim() ) );
					}
					catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
 			else {
 				continue;
 			}
 			if( rowValues.size() != 12 ) {
 				plateRead.setError( "found " + rowValues.size() + "cols" );
 				return plateRead;
 			}
 			rows.add(rowValues);
 			if( rows.size() == 8 ) {
 				plateReadParsed = true;
 			}
 		}
 
 		double[][] values = new double[8][12];
 		for (int i = 0; i < rows.size(); i++) {
 			List<Double> row = rows.get(i);
 			for (int j = 0; j < row.size(); j++) {
 				values[i][j] = row.get(j);
 			}
         }
 		plateRead.setValues(values);
 		plateRead.setError("OK");
 		return plateRead;
     }
 
 	@Override
     public List<Plate> addResultsTo(User activeUser, List<Plate> plates)
                        throws IllegalArgumentException {
 		HashMap<String, PlateRead> plateReadMap = new HashMap<String, PlateRead>();
 		for(PlateRead p : plateReads) {
 			plateReadMap.put(p.getBarCode(), p);
 		}
 		
 		IOperationManager om = (IOperationManager) Springcontact.getBean("operationManager");
 		List<Plate> results = new ArrayList<Plate>();
 		for(Plate p : plates) {
 			om.addResult( activeUser, plateReadMap.get(p.getBarcode()), p );
 			results.add(p);
 		}
 		return plates;
     }
 
 	static class PlateRead96 implements PlateRead {
 
 		private String error;
 		private double[][] values;
 		private String barcode = "unknown";
 		
 		public PlateRead96(String message) {
 			this.error = message;
         }
 
 		public PlateRead96() {
 			this.error = "OK";
         }
 
 		@Override
         public double[][] getValues() {
 			return values;
         }
 
 		@Override
         public String getBarCode() {
 			return barcode;
 		}
 
 		@Override
         public void setBarCode(String barcode) {
 			this.barcode = barcode;
         }
 
 		@Override
         public String getError() {
 			return error;
         }
 
 		@Override
         public void setError(String error) {
 			this.error = error;
         }
 
 		@Override
         public void setValues(double[][] values) {
 			this.values = values;
         }
 
 		@Override
         public String getName() {
 	        return "no name";
         }
 	}
 }
