 import java.io.*;
 import java.util.*;
 import java.lang.*;
 
 /*
   This program reads the list of found HCRs (see FindHCR.java) and prints out the 
   corresponding areas of multiple alignments as found in the MULTIZ files located
   in /projects/instr/11wi/cse427/multiz/chr%d.maf.
  */
 
 class AlignHCRs {
     public static void main(String[] argv) {
 	Date start_time=new Date();
 
 	// Read in serialized HashMap of HCRs
 	String hcr_file="hcrs.ser";
 	HashMap chr2HCRs=readHCRs(hcr_file); // k=chrX, v=PhyloBlock?
 	System.out.println("read "+hcr_file);
 
 	// Look through multiz files for blocks overlapping one of our HCRs:
 	String[] human_chrs={"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
	//String[] human_chrs={"11"};
 	for (int i=0; i<human_chrs.length; i++) {
 	    String chrom="chr"+human_chrs[i];
 	    System.out.println("processing "+chrom);
 	    filterZBlocks(chrom, chr2HCRs);
 	}
 
 	Date end_time=new Date();
 	System.out.println(String.format("execution time: %s",new TimeSpan(start_time,end_time)));
     }
 
     public static void filterZBlocks(String chrom, HashMap chr2HCRs) {
 	String multiZfile=String.format("/projects/instr/11wi/cse427/multiz/%s.maf",chrom);
 	MultiZParser parser=new MultiZParser(multiZfile);
 
 	try {
 	    String filtered_multiZfile=String.format("%s.maf.filtered",chrom);
 	    FileWriter writer=new FileWriter(filtered_multiZfile);
 
 	    MultiZBlock zBlock=null;
 	    while ((zBlock=parser.next())!=null) {
 		String z_chrom=zBlock.human_chrom;
 		if (z_chrom==null) continue;
 		Interval zb_int=zBlock.human_interval;
 		if (zb_int==null) continue;
 		HCR[] hcrs=(HCR[])chr2HCRs.get(z_chrom);
 		if (hcrs==null || hcrs.length==0) continue;
 
 		for (int i=0; i<hcrs.length; i++) {
 		    Interval hcr_int=hcrs[i].interval;
 		    if (zb_int.overlaps(hcr_int)) {
 			writer.write(zBlock.src_block);
 			writer.write("\n");
 			System.out.println(zBlock.header());
 		    }
 		}
 	    }
 	    writer.flush();	// close() probably does this for us
 	    writer.close();
 	    System.out.println(filtered_multiZfile+" written");
 	} catch (IOException ioe) {
 	    new Die(ioe);
 	}
     }
 
     // fixme: this might become obsolete
     public static void findZBlocks(String chrom, HCR[] hcrs) {
 	String multiZfile=String.format("/projects/instr/11wi/cse427/multiz/%s.maf",chrom);
 	MultiZParser parser=new MultiZParser(multiZfile);
 	MultiZBlock zBlock=null;
 	while ((zBlock=parser.next())!=null) {
 	    ChromSeq cs=zBlock.get("hg19"); 
 	    if (cs==null) continue; // that block didn't have a human portion (seems unlikely, but hey)
 
 	    for (int j=0; j<hcrs.length; j++) {
 		if (cs.interval.overlaps(hcrs[j].interval)) {
 		    hcrs[j].zBlocks.add(zBlock);
 		    System.out.println(String.format("Added %s to %s", zBlock.toString(), hcrs[j].toString()));
 		}
 	    }
 	}
     }
 
     public static String alignZBlocks(HCR hcr) throws RuntimeException {
 	// sort hcr.zBlocks by starting coord
 	MultiZBlock[] zBlocks=hcr.zBlocks.toArray(new MultiZBlock[hcr.zBlocks.size()]);
 	Arrays.sort(zBlocks); // sorts by human chr interval
 
 	HashMap<String,StringBuffer> org2seq=new HashMap();
 
 	// This currently builds the hg19 seq (we hope)
 	for (int k=0; k<MultiZBlock.list_order.length; k++) {
 	    String org=MultiZBlock.list_order[k];
 	    StringBuffer buf=org2seq.containsKey(org)? org2seq.get(org) : new StringBuffer();
 
 	    buf.append(zBlocks[0].get(org).seq); // start sequence
 	    for (int i=1; i<zBlocks.length; i++) {
 		MultiZBlock prevZBlock=zBlocks[i-1];
 		MultiZBlock thisZBlock=zBlocks[i];
 
 		// have to check case where one of these zblocks doesn't have org
 		int gap=thisZBlock.get(org).interval.start - prevZBlock.get(org).interval.stop - 1;
 		if (gap < 0) {
 		    throw new RuntimeException("something's wrong; zblocks overlap");
 		} else if (gap > 0) {
 		    char[] dashes=new char[gap];
 		    for (int j=0; j<gap; j++) { dashes[j]='-'; } // better way of doing this???
 		    buf.append(dashes);
 		} else {
 		    // gap == 0, do nothing
 		}
 		buf.append(thisZBlock.get(org).seq);
 		org2seq.put(org, buf); // re-save buf
 	    }
 
 
 
 	    // concat all seqs in zblocks together, by org; fill in gaps with '-'
 	    // Have to create a String[] for each species; each entry the concat of the appropriate
 	    // entries from the zblocks.
 	    // within a multiz block, all seqs are of equal length (yay)
 	}
 	return new String("fixme");
     }
 
     public static void dump_chr2HCRs(HashMap chr2HCRs) {
 	Iterator i=chr2HCRs.keySet().iterator();
 	while (i.hasNext()) {
 	    String chrom=(String)i.next();
 	    System.out.println(chrom);
 	    HCR[] hcrs=(HCR[])chr2HCRs.get(chrom);
 	    for (int j=0; j<hcrs.length; j++) {
 		System.out.println("  "+hcrs[j].toString());
 	    }
 	}
     }
 
     public static HashMap readHCRs(String hcr_file) {
 	HashMap chr2HCRs=null;
 	try {
 	    ObjectInputStream ois=new ObjectInputStream(new FileInputStream(hcr_file));
 	    chr2HCRs=(HashMap)ois.readObject();
 	} catch (IOException ioe) {
 	    new Die(ioe);
 	} catch (ClassNotFoundException e) {
 	    new Die(e);
 	}
 	return chr2HCRs;
     }
 }
