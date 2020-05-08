 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import javax.sql.*;
 
 public class FilterApplier
 {
 
     private String output = "Incomplete";
     private String filterName;
     private String vcfName;
     private String fileName;
 	private DatabaseConnector connection;   
 	private DatabaseConnector nestedConnection;
 	private DatabaseConnector nestedConnection2;
 
     public FilterApplier( String filterName, String vcfName, String filename )
     {
 		this.filterName = filterName;
 		this.vcfName = vcfName;
 		this.fileName = filename;
     }
     
     //@Override
     public String execute() {
     	
     	try {
     		this.connection = new DatabaseConnector();
     		this.nestedConnection = new DatabaseConnector();
     		this.nestedConnection2 = new DatabaseConnector();
     	}
     	catch( Exception e)
     	{
     		this.output = "Cannot connect to database";
     		return this.output;
     	}
 	
 		try
 		    {
 
 			//TODO load filter
 			this.output = applyFilter();
 			connection.CloseConnection();
 			nestedConnection.CloseConnection();
 			nestedConnection2.CloseConnection();
 		    }
 		catch (Exception e)
 		    {
 			this.output = e.getMessage();
 		    }
 		return this.output;
 	
     }
 
     private String applyFilter()
     {
     	vcfWriter writer = null;
 		try {
 			boolean passing = true;
 			writer = new vcfWriter(this.fileName);
 			
 			long vcfId = this.connection.getVcfId( this.vcfName);
 			
 			writer.writeHeader( this.connection.getVcfHeader(vcfId) );
 			
 			ResultSet entries = this.connection.getVcfEntries( vcfId );
 			
 			while (entries.next() )
 		    {
 				System.out.println("entry");
 				long entryId = entries.getLong("EntryId");
 				ArrayList<String> infoTableName = new ArrayList<String>();
 	    		ArrayList<ResultSet> infoData = new ArrayList<ResultSet>();
 				
 				this.nestedConnection.getInfoData(entryId, infoTableName, infoData);
 				//TODO test entry
 				if (passing)
 				{
 					writer.writeEntry( entries, infoData, infoTableName );
 					//individual level
 					String indFormat = entries.getString("Format");
 					ArrayList<String> genotypes = new ArrayList<String>( 
 				    		Arrays.asList( indFormat.split(":") ));
 					
 					ResultSet individuals = this.nestedConnection.getIndividuals( entryId );
 					while (individuals.next() )
 					{
 						System.out.println("individuals");
 						long indId = entries.getLong("IndID");
 					
 						ArrayList<ResultSet> genotypeData = this.nestedConnection2.getIndividualData( indId, genotypes);
 						
 						//if pass write
 						//if fail close genotypeData
 						//writer closes genotypeData,
 						writer.writeIndividual( genotypeData, genotypes );
 					}
 					individuals.close();
 					writer.writeEOL();
 				}
 
 		    }
 			entries.close();
 			writer.closeWriter();
 			return "Completed filter";
 			
 		} catch (Exception exception) {
 			if (writer != null)
 			{
 				writer.closeWriter();
 			}
 			return exception.getMessage();
 		}
     }
 
     //@Override
     public void pipeOutput() {
 	// TODO Auto-generated method stub.
 	
     }
 
 }
