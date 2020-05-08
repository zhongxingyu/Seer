 import java.util.*;
 import java.io.*;
 
 public class FASTQParse extends BaseParse {
     protected static final int BUFF_SIZE = 1024;
     protected static final byte[] buffer = new byte[BUFF_SIZE];
     protected int buffer_pos;
     protected int buffer_size;
     
     protected long file_pos;
     protected RandomAccessFile filein;
 
     String col_names[] =  {
 	"Sequence Name",
 	"Sequence",
 	"Quality"
     };
 
     public FASTQParse(final String filename){
 	super(filename);
 	
 	buffer_pos = 0;
         file_pos = 0;
       	
         try {
             filein = new RandomAccessFile(filename, "r");
             parseHeader();
 	    file_pos = filein.getFilePointer();
 	}catch(IOException ie){
             System.out.println("Couldn't open file as FASTQ");
         }
 	
     }
 
     protected void parseHeader(){
 	String line = null;
         header = "";
         long pos = 0;
 
         try {
             pos = filein.getFilePointer();
             while((line = filein.readLine()) != null){
                 String tag = line.substring(0,1);
                 if(tag.equals("#")){
                     header += line;
                     header += "\n";
                     pos = filein.getFilePointer();
                 }else{
                     break;
                 }
             }
 
             if(pos >=0){
                 filein.seek(pos);
             }
         }catch(IOException ie){}
     }
 
     public void seek(final long offset) throws IOException{
         filein.seek(offset);
     }
 
     //Invariant: should be at beginning of record
     public long getNextRecordIndex(){
 	long offset = file_pos + buffer_pos;
 	
 	boolean on_seq_header = true;
 	boolean on_seq = false;
 	boolean on_qual_header = false;
 	boolean on_qual = false;
 	
 	int num_seq_lines = 0;
 	int num_qual_lines = 0;
 
 	try{
 	    while(true){
 		if(buffer_size == 0 || buffer_pos >= buffer_size){
                     file_pos = filein.getFilePointer();
                     buffer_size = filein.read(buffer);
                     buffer_pos = 0;
                 }
                 if(buffer_size < 1) return -1;
 		
 		if(buffer[buffer_pos] == '+' && on_seq){
 		    on_qual_header = true;
 		}
 
 		if(buffer[buffer_pos] == '\n'){
 
 		    if(on_qual){
 			num_qual_lines++;
 			if(num_seq_lines == num_qual_lines){
 			    buffer_pos++;
 			    return offset;
 			}
 		    } else if(on_qual_header){
 			on_qual = true;
 		    } else if(on_seq){
 			num_seq_lines++;
 		    }else if(on_seq_header){
 			on_seq = true;
 		    }
 
 		}
 		buffer_pos++;		
 	    }
 	}catch(Exception e){
 	    return -1;
 	}
     }
 
     public String[] getNextRecord(){
 	try {
 	    String ans[] = new String[3];
 	    String line;
 	    
 	    while((line=filein.readLine()) != null){
 		//get description
		if(!line.substring(0,1).equals("@")) break;
 		ans[0] = line.substring(1);
 		String seq = "";
 		String qual = "";
 		while((line = filein.readLine()) != null){
 		    if(line.matches("[ACTGNacgtnURYSWKMBDHVN.-]*")){
 			seq += line;
 		    }else{
			if(!line.substring(0,1).equals("+")) break;
 			//ans[2] = line.substring(1);
 			while((line = filein.readLine()) != null){
 			    qual += line;
 			    if(seq.length() <= qual.length()){
 				ans[1] = seq;
 				ans[2] = qual;
 				return ans;
 			    }
			    break;
 			}
 		    }
 		}
		break;
	    
 	    }
 	    return null;
 	} catch (Exception e){
 	    return null;
 	}
     }
 
     public double getProgress(){
 	try {
             return 1.0*filein.getFilePointer()/(1.0*filein.length());
         } catch (IOException e){
             return 1.0;
         }
     }
 
     public String getToolTip(final String value, int row, int col, final String[] other_values){
 	if(col == 1 && other_values.length > 2){
 	    String qual = other_values[2];
 	    return prettyPrintBaseQual(value, qual);
 	}
 	
 	return value;
     }
     
     public String getColumnName(int col){
 	if(col >= col_names.length){
             return "Unknown";
         }
         if(col < 0) return "Unknown";
         return col_names[col];
     }
     
     public int getNumColumnLabels() {
 	if(col_names == null) return 0;
         return col_names.length;
     }
 
     protected String prettyPrintBaseQual(String bases, String quals){
         if(bases.equals("*") || bases.length() != quals.length()) return ("<html><font size=\"5\">" + bases + "</font></html>");
         String hexcolor = "<html>";
         for(int i = 0; i < bases.length(); i++){
             hexcolor += "<font size=\"5\" color=\"";
             int c = (int)quals.charAt(i) - 33;
             if(c < 20) hexcolor += "#E9CFEC";else hexcolor += "#571B7e";
 
             hexcolor += "\">";
             hexcolor += bases.charAt(i);
             hexcolor += "</font>";
         }
         hexcolor += "</html>";
         return hexcolor;
     }
 
 }
