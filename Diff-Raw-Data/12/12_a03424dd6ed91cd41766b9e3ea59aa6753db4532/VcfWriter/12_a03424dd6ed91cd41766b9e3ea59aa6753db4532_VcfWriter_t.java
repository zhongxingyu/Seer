 import java.io.*;
 import java.util.*;
 import java.sql.*;
 
 public class VcfWriter
 {
     private BufferedWriter writer;
     private int infoCount = 0;
     private String writeBuffer = "";
 	private boolean individualMiddle;
     
     public VcfWriter( String filename ) throws IOException
     {
         try
         {
             File outFile = new File(filename);
             this.writer = new BufferedWriter(new FileWriter(outFile));
         }
         catch (IOException e)
         {
             throw new IOException("Could not open file:" + filename);
         }
     }
     
     public void writeHeader(String header) throws IOException
     {
         this.writer.write( header);
     }
     
     public void writeEntryStart( ResultSet entryData ) throws SQLException
     {
         try {
         	this.writeBuffer = "";
             this.writeBuffer +=( entryData.getString("Chrom") );
             this.writeBuffer +=( '\t' );
             this.writeBuffer +=( entryData.getString("Pos") );
             this.writeBuffer +=( '\t' );
             this.writeBuffer +=( entryData.getString("Id") );
             this.writeBuffer +=( '\t' );
             this.writeBuffer +=( entryData.getString("Ref") );
             this.writeBuffer +=( '\t' );
             this.writeBuffer +=( entryData.getString("Alt") );
             this.writeBuffer +=( '\t' );
             this.writeBuffer +=( entryData.getString("Qual") );
             this.writeBuffer +=( '\t' );
             this.writeBuffer +=( entryData.getString("Filter") );
             this.writeBuffer +=( '\t' );
 
             this.infoCount = 0;
         
         } catch (SQLException exception) {
         	throw new SQLException("VCF Entry data improperly formatted");
         }
     }
     
     public void writeEntryEnd( ResultSet entryData ) throws IOException, SQLException
     {
     	this.writer.write( this.writeBuffer );
     	this.writer.write("\t");
     	this.writer.write( entryData.getString("Format") );
     }
     
     public void writeInfoSection( String infoName, ResultSet infoData) throws IOException, SQLException
     {
 	    
 	    String infoDatum = "";
 	    if (infoData.next()) 
 		{
 		    if (this.infoCount!= 0)
 		    {
 		    	this.writeBuffer += (";");
 		    }
 		    this.infoCount++;
 	    	
 		    ResultSetMetaData rsMetaData = infoData.getMetaData();
 		    int numberOfColumns = rsMetaData.getColumnCount();
 	    	
 		    if (numberOfColumns > 1)
 		    {
 		    	infoDatum = infoData.getString(2);
 		    	this.writeBuffer +=( infoName+"="+infoDatum );
 		    }
 		    else
 		    {
 		    	this.writeBuffer +=( infoName );
 		    }
 		}
 	    infoData.close();
     }
     
     public void writeIndividualStart() throws IOException
     {
     	///TODO remove this.writeBuffer = "\t";
     	this.writer.write("\t");
    	this.writeBuffer = "";
     	this.individualMiddle = false;
     	
     }
     
     public void writeIndividualEnd() throws IOException
     {
     	this.writer.write(this.writeBuffer);
     }
     
     public void writeIndividualDatum( 
             ResultSet genotypeData,
             String genotypeName ) throws SQLException
 	{
     	if ( genotypeData == null)
     	{
     		return;
     	}
     	
     	if (genotypeData.next() )
     	{
 			if ( this.individualMiddle )
 			{
 				this.writeBuffer +=(":");
 			}
 			else
 			{
 				this.individualMiddle = true;
 			}
 			
 			if ( isSpecialCase(genotypeName ) )
 			{
 					
 				this.writeBuffer +=( formatSpecialCase( genotypeName, genotypeData ) );
 			}
 			else
 			{
 				this.writeBuffer +=( formatStandardCase( genotypeName, genotypeData ) );
 			}	
     	}
 		genotypeData.close();	
     	
 	}
     
 	private String formatStandardCase(String genotypeName, ResultSet rs) throws SQLException {
 
 	    ResultSetMetaData rsMetaData = rs.getMetaData();
 
 	    int numberOfColumns = rsMetaData.getColumnCount();
 	    
 	    String indData = "";
 	    String nullValues = null;
 	    
 	    for (int i=2; i<= numberOfColumns; i++)
 	    {
 	    	String separator = ",";
 	    	if (i==2)
 	    	{
 	    		separator = "";
 	    	}
 	    	
 	    	String genoDatum = rs.getString(i);
 	    	if ( rs.wasNull() )
 	    	{
 	    		if ( nullValues == null )
 	    			nullValues = separator + ".";
 	    		else
 	    			nullValues += separator + ".";
 	    	}
 	    	else
 	    	{
 	    		if ( nullValues != null )
 	    		{
 	    			//valid value; append stored nulls
 	    			indData += nullValues;
 	    			nullValues = null;
 	    		}
 	    		indData += separator + genoDatum;
 	    	}
 	    	
 	    }
 	    if (indData.isEmpty())
 	    {
 	    	indData = ".";
 	    }
 	    
 		return indData;
 	}
 
 	private boolean isSpecialCase(String genotypeName ) throws SQLException
 	{
 		return genotypeName.equals( "GT");
 	}
 	
 	private String formatSpecialCase(String genotypeName, ResultSet data) throws SQLException {
 		if ( genotypeName.equals( "GT") )
 		{
 			int i = 0;
 			StringBuilder gtData = new StringBuilder();
 			appendAllele( gtData, data, "1") ;
 			if ( appendPhase( gtData, data, "1") )
 			{
 				appendAllele( gtData, data, "2") ;
 				if (appendPhase( gtData, data, "2") )
 				{
 					appendAllele( gtData, data, "3");
 				}
 			}
 			return gtData.toString();
 			
 		}
 		return "";
 	}
 	
 	private void appendAllele( StringBuilder gtData, ResultSet data, String count )throws SQLException
 	{
 		String allele = data.getString("Allele" +count);
 		if ( data.wasNull() )
 		{
 			gtData.append(".");
 		}
 		else
 		{
 			gtData.append(allele);
 		}
 		
 	}
 	
 	private boolean appendPhase( StringBuilder gtData, ResultSet data, String count )throws SQLException
 	{
 		String phase = data.getString("Phase" +count);
 		if ( data.wasNull() )
 		{
 			//end of data
 			return false;
 		}
 		else
 		{
 			if ( phase.equals("0") )
 			{
 				gtData.append("/");
 			}
 			else if (  phase.equals("1") )
 			{
 				gtData.append( "|");
 			}
 			else
 			{
 				throw new SQLException("Invalid GT data");
 			}
 			return true;
 		}
 	}
 
 	public void writeEOL() throws IOException
     {
     	this.writer.write("\n");
     	
     }
     
     public void closeWriter()
     {
         if ( this.writer != null )
         {
             try {
                 this.writer.close();
             } catch (IOException exception) {
                 //Do nothing
             }
         }
     }
 }
