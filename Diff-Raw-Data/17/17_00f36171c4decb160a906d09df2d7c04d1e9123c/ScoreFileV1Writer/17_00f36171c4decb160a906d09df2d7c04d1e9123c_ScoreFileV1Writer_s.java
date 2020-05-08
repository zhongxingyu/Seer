 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.pitt.isp.sverchkov.scorecalculator;
 
 import java.io.*;
 import java.util.HashSet;
 import java.util.Set;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author YUS24
  */
 public class ScoreFileV1Writer implements ScoreWriter {
     
     private static final Logger LOG = Logger.getLogger(ScoreFileV1Writer.class);
     
     private final File outFile;
     
     public ScoreFileV1Writer( File output ){
         outFile = output;
     }
 
     @Override
     public void writeScores(RecordSet rs, ScoreFunction sf) {
         
         Variable[] variables = rs.getVariableArray();
         
         if( variables.length > 31 )
             throw new UnsupportedOperationException("Current implementation does not support more than 31 variables.");
         
        long[] bitmasks = new long[variables.length];
        long all = 0;
         for( int i=0; i < bitmasks.length; i++ )
            all |= ( bitmasks[i] = 1L << i );
         
         LOG.debug("All-parent bitmask: "+Long.toBinaryString(all));
         
         // Initialize the output stream
         try {
             try ( DataOutputStream stream = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( outFile ) ) )){
                 
                 LOG.debug("Output file opened.");
                 
                 // Write the header
                 writeHeader( stream, variables.length-1, rs.size(), variables.length );
                 
                 LOG.debug("Header written.");
 
                 // Iterate over all variables
                 for( int i=0; i < variables.length; i++ ){
                     
                     LOG.info("Writing scores for variable \""+variables[i].getName()+"\" ("+i+" out of "+variables.length+").");
                     LOG.debug("Variable bitmask: "+Long.toBinaryString(bitmasks[i]));
                     
                     // Write header for variable
                    writeVariable( stream, variables[i], (int) all/2 );
                     
                    LOG.debug("Variable header weritten. Writing "+all/2+" scores...");
 
                     // Iterate over all subsets
                     for( long bits = all; bits >= 0; bits-- )
                         // If set doesn't contain current variable (i)
                         if( ( bits & bitmasks[i] ) == 0 ){
 
                             // Make set
                             Set<Variable> set = new HashSet<>();
                             for( int j=0; j<variables.length; j++ )
                                 // If set contains variable j
                                 if( ( bits & bitmasks[j] ) != 0 )
                                     set.add( variables[j] );
 
                             // Compute score
                            LOG.debug("Computing score...");
                             double score = sf.score( variables[i], set, rs );
 
                             // Write score
                             writeScore( stream, bits, score );
                         }
                     
                     LOG.debug("Scores written.");
                 }
             }        
         }catch( FileNotFoundException e ){
             LOG.fatal( e.getMessage() );
             LOG.trace( e, e );
         }catch( IOException e ){
             LOG.fatal( e.getMessage() );
             LOG.trace( e, e );
         }
     }
 
     private void writeVariable(DataOutputStream stream, Variable variable, int numScores) throws IOException {
         stream.writeUTF( variable.getName() );
         stream.writeByte( variable.getCardinality() );
         for( String instantiation : variable.getInstantiations() )
             stream.writeUTF(instantiation);
         stream.writeInt(numScores);
     }
 
     private void writeScore(DataOutputStream stream, long bits, double score) throws IOException {
         LOG.trace("Writing score "+score+" for parent set "+Long.toBinaryString(bits));
         stream.writeLong(bits);
         stream.writeFloat( (float) score );
     }
 
     private void writeHeader(DataOutputStream stream, int nParents, int nRecords, int nVariables) throws IOException {
         stream.writeByte(1); // Version number 1
         stream.writeByte(nParents); // Max number of parents
         stream.writeInt(nRecords); // Number of records
         stream.writeInt(nVariables); // Number of variables
     }
     
 }
