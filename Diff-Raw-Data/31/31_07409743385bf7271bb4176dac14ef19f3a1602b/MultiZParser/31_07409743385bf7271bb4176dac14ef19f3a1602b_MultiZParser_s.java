 import java.io.*;
 import java.lang.*;
 import java.util.*;
 
 
 class MultiZParser {
     public BufferedReader reader;
     public MultiZBlock current;
 
     MultiZParser(String multizfile) {
 	try {
 	    reader=new BufferedReader(new InputStreamReader(new FileInputStream(multizfile)));
 	    this.current=parseNext();
 	} catch (IOException ioe) {
 	    new Die(ioe);
 	}
     }	
 
     private MultiZBlock parseNext() {
 	String line=null;
 	StringBuffer buf=new StringBuffer();
 	Interval intreval=null;
	MultiZBlock next=new MultiZBlock();
 	try {
 	    while ((line=reader.readLine())!=null) {
 		// System.err.println(line);
 		if (line.matches("^\\s*$")) {
 		    next.src_block=new String(buf);
 		    break;
 		}
 
 		if (line.startsWith("a")) { 
 		    buf.append(line);
 		    buf.append("\n");
 		}
 
 		if (line.startsWith("s")) {
 		    buf.append(line);
 		    buf.append("\n");
 
 		    // Check if this is the human portion:
 		    if (line.startsWith("s hg19")) {
 			extractChromAndInterval(line, next);
 		    }
 		}
 	    }
 	}  catch (IOException ioe) {
 	    new Die(ioe);
 	}
 
 	return next;
     }
 
     private void extractChromAndInterval(String line, MultiZBlock zb) {
 	try {
 	    String[] fields=line.split("\\s+");
 	    String org_chrom=fields[1];
 	    String f2[]=org_chrom.split("\\.");
 	    String org=f2[0]; // already know this is "hg19"
 	    String chrom=f2[1];
 	    String start=fields[2];
 	    int istart=Integer.valueOf(start).intValue();
 	    String length=fields[3];
 	    int istop=istart+Integer.valueOf(length).intValue()-1;
 	    zb.human_chrom=chrom;
 	    zb.human_interval=new Interval(istart,istop);
 
 	} catch (ArrayIndexOutOfBoundsException e) {
 	    new Die(e, "cannot extract chrom and line: "+line);
 	}
     }
 
 
     private MultiZBlock parseNext_old() {
 	String line=null;
 	MultiZBlock next=null;
 	try {
 	    while ((line=reader.readLine())!=null) {
 		if (line.matches("^\\s*$")) break;
 
 		if (line.startsWith("a")) { next=new MultiZBlock(); }
 
 		if (line.startsWith("s")) {
 		    String[] fields=line.split("\\s+");
 		    try {
 			String[] org_chr=fields[1].split("\\.");
 			String org=org_chr[0];
 			String chrom=org_chr[1];
 			int start=Integer.valueOf(fields[2]).intValue();
 			// fields[3] is length of alignment
 			String strand=fields[4];
 			// fields[5] is srcSize
 			String seq=fields[6];
 			next.put(org,new ChromSeq(org,chrom,seq,start));
 		    
 		    } catch (ArrayIndexOutOfBoundsException e) {
 			new Die(e, "malformed line?: "+line);
 		    }
 		}		
 	    }
 	}  catch (IOException ioe) {
 		new Die(ioe);
 	}
 	return next;
     }
 
     public MultiZBlock next() {
 	MultiZBlock toReturn=current;
 	current=parseNext();
 	return toReturn;
     }
 
     public static void main(String[] argv) {
	MultiZParser mzp=new MultiZParser("chr11.maf");
	System.out.println(mzp.next().src_block);
	System.out.println(mzp.next().src_block);
	System.out.println(mzp.next().src_block);
     }
 }
