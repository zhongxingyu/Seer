 package net.derkholm.nmica.extra.seq.nextgen;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.samtools.SAMFileReader;
 import net.sf.samtools.SAMRecord;
 
 public class SAMPileup {
 	private int extendedLength;
 	private short[] pileup;
 	private int refLength;
 
 	public SAMPileup(String refName, int refLength, int extendedLength) {
 		this.pileup = new short[refLength];
 		this.extendedLength = extendedLength;
 		this.refLength = refLength;
 	}
 	
 	public void add(SAMRecord rec) {
 		int start = rec.getAlignmentStart();
 		int end = rec.getAlignmentEnd();
 		if (!rec.getReadNegativeStrandFlag()) {
			for (int i = start, endpos = Math.min(this.refLength, end); i < endpos; i++) {pileup[i]++;}
 		} else {
			for (int i = Math.max(1, end - extendedLength); i < end; i++) {pileup[i]++;}
 		}
 	}
 	
 	public int depthAt(int i) {
 		return pileup[i];
 	}
 
 	public int getExtendedLength() {
 		return extendedLength;
 	}
 
 	public void setExtendedLength(int extendedLength) {
 		this.extendedLength = extendedLength;
 	}
 }
