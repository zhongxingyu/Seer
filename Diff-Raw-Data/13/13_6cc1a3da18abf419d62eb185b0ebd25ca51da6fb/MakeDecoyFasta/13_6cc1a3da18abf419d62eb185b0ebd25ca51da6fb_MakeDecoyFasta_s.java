 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package makedecoyfasta;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 /**
  *
  * @author dfermin
  */
 public class MakeDecoyFasta {
 
     static String inputF;
     static int mode;
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws FileNotFoundException, IOException {
         
         if(args.length != 2) {
             System.err.println("\nUSAGE: java -jar makeDecoyFasta.jar <input_fasta> <0-3>");
             System.err.println("\t0 = reverse peptide FASTA");
             System.err.println("\t1 = reverse protein FASTA");
             System.err.println("\t2 = reverse peptide digest table");
             System.err.println("\t3 = reverse protein digest table\n");
             
             System.exit(0);
         }
         inputF = args[0];
         mode = Integer.valueOf(args[1]);
         
         readFasta();
         
         System.err.println("mode = " + mode);
         
         
     }
 
     
     private static void readFasta() throws FileNotFoundException, IOException {
         
         File inF = new File(inputF);
         if(!inF.exists()) {
             System.err.println("\nERROR: Unable to open '" + inputF + "'\n");
             System.exit(0);
         }
         
 		
 	if(mode >= 2) { // digest table will be created so write the header line
             System.out.println("protid\tisDecoy\tpepStart\tpepEnd\tpepLen\tpepSeq\tpepMHplus");
 	}
 		
 		
         BufferedReader br = new BufferedReader(new FileReader(inF));
         String line = null;
         SeqClass curSeq = null;
         int ctr = 0;
         while( (line = br.readLine()) != null ) {
             
             if(line.startsWith(">")) {
                 
                 if(null != curSeq) {
                     process(curSeq);
                     
                     ctr++;
                     System.err.print("\rReading sequences from " + inputF + ":  " + ctr);
                 }
                 curSeq = null;
                 curSeq = new SeqClass();
                 
                 curSeq.parseFastaHdr(line);
             }
             else curSeq.append(line);
         }
         br.close();
         
         if(null != curSeq) {
            process(curSeq);
         }
         curSeq = null;
         
         System.err.print("\n");
     }
 
     
     // function creates a FASTA file with peptide sequences reversed
     private static void makePepFASTA(SeqClass curSeq) {
 		String line = null;
 		
         // Forward proteins
         line = ">" + curSeq.getId() + " " + curSeq.getDefline() + "\n"
              + curSeq.getFasta(0);
         System.out.print(line);
 		
 		
         // Reversed peptides        
         line = ">rev_" + curSeq.getId() + " ## REVERSE PEPTIDES OF "
              + curSeq.getId() + "\n" + curSeq.getFasta(1);
         System.out.print(line);
     }
 
     
     
     private static void makeProtFASTA(SeqClass curSeq) {
         String line = null;
 		
         // Forward sequences
         line = ">" + curSeq.getId() + " " + curSeq.getDefline() + "\n"
              + curSeq.getFasta(0);
        System.out.println(line);
 		
 		
         // Reversed protein sequences
         line = ">rev_" + curSeq.getId() + " ## REVERSE PROTEIN OF "
              + curSeq.getId() + "\n" + curSeq.getFasta(2);
        System.out.println(line);
     }
 
     
     
     private static void makePepTable(SeqClass curSeq) {
         
         System.out.print( curSeq.getDigestTable(0) ); // forwards
             
         if(mode == 2) {
             System.out.print( curSeq.getDigestTable(1) ); // reversed peptide
         }
         
         if(mode == 3) {
             System.out.print( curSeq.getDigestTable(2) ); // reverse protein
         }
     }
 
 
     // This functin determines what type of processing a SeqClass object needs
     // based upon the 'mode' given by the user. 
     private static void process(SeqClass curSeq) {
     
         switch(mode) {
             case 0:  // reverse peptide fasta file
                 curSeq.digest(0);
                 curSeq.buildReversePeptideSeq();
                 makePepFASTA(curSeq);
                 break;
             case 1: // reverse protein fasta file
                 curSeq.reverseSeq();
                 makeProtFASTA(curSeq);
                 break;
             case 2: // reverse peptide digest table
                 curSeq.digest(0);
                 curSeq.buildReversePeptideSeq();
                 makePepTable(curSeq);
                 break;
             case 3: // reverse protein digest table
                 curSeq.digest(0);
                 curSeq.reverseSeq();
                 curSeq.digest(1);
                 makePepTable(curSeq);
                 break;
             default:
                 break;
                 
         }
     }
     
 }
