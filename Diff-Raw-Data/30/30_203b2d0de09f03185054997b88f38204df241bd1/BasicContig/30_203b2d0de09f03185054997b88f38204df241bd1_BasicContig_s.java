 package enderdom.eddie.bio.assembly;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import enderdom.eddie.bio.lists.ClustalAlign;
 import enderdom.eddie.bio.sequence.BasicRegion;
 import enderdom.eddie.bio.sequence.BioFileType;
 import enderdom.eddie.bio.sequence.Contig;
 import enderdom.eddie.bio.sequence.SequenceObject;
 import enderdom.eddie.bio.sequence.GenericSequenceXT;
 import enderdom.eddie.bio.sequence.SequenceObjectXT;
 import enderdom.eddie.bio.sequence.UnsupportedTypeException;
 import enderdom.eddie.tools.Tools_Math;
 import enderdom.eddie.ui.BasicPropertyLoader;
 
 public class BasicContig implements Contig{
 
 	protected LinkedHashMap<String, SequenceObjectXT> sequences;
 	protected String contigname;
 	protected Logger logger = Logger.getRootLogger();
 	protected int iteratorcount = 0;
 	protected int position = 1;
 	protected ArrayList<BasicRegion> regions;
 	private boolean noQual2fastq;
 	
 	public BasicContig(){
 		this.sequences = new LinkedHashMap<String, SequenceObjectXT>();
 		this.contigname = "Unnamed_"+BasicPropertyLoader.numbercache++;
 	}
 	
 	public BasicContig(String contig){
 		this.sequences = new LinkedHashMap<String, SequenceObjectXT>();
 		this.contigname = contig;
 	}
 	
 	//Not all the relevant for ACErecord but
 	public int getN50() {
 		logger.error("Not implemented");
 		return 0;
 	}
 
 	public int[] getListOfLens() {
 		int[] lens = new int[this.getNoOfSequences()];
 		int i=0;
 		for(String s : sequences.keySet()){
 			if(!s.equals(contigname)){
 				lens[i] = sequences.get(s).getLength();
 				i++;
 			}
 		}
 		return lens;
 	}
 	
 	public int[] getListOfActualLens() {
 		int[] lens = new int[this.getNoOfSequences()];
 		int i=0;
 		for(String s : sequences.keySet()){
 			if(!s.equals(contigname)){
 				lens[i] = sequences.get(s).getActualLength();
 				i++;
 			}
 		}
 		return lens;
 	}
 
 	public int getNoOfMonomers() {
 		return Tools_Math.sum(getListOfActualLens());
 	}
 
 	public int getQuickMonomers() {
 		return Tools_Math.sum(getListOfLens());
 	}
 	
 	/**
 	 * @return number of reads in this contig
 	 */
 	public int getNoOfSequences() {
 		return this.sequences.size()-1;
 	}
 
 	public synchronized SequenceObject getSequence(int i) {
 		for(String s : sequences.keySet()){
 			if(sequences.get(s).getPositionInList()-1 == i){
 				return sequences.get(s);
 			}
 		}
 		return null;
 	}
 	
 	public String getContigName() {
 		return this.contigname;
 	}
 
 
 	public void setContigName(String s) {
 		this.contigname = s;
 	}
 
 	public boolean hasNext() {
 		return iteratorcount+1 < this.getNoOfSequences();
 	}
 
 	public SequenceObject next() {
 		iteratorcount++;
 		return this.getSequence(iteratorcount);
 	}
 
 	public void remove() {
 		logger.error("Not implemented");
 	}
 	
 	public int getCoverageAtBp(int position, int base) {
 		int coverage = 0;
 		for(String read : this.getReadNames()){
 			char c = this.getCharAtRelative2Contig(read, position, base);
 			if(c == '-' || c=='*');
 			else coverage++;
 		}
 		return coverage;
 	}
 
 	public String getFileName() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getFilePath() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Set<String> keySet() {
 		return this.sequences.keySet();
 	}
 
 	/**
 	 * 
 	 * @return Consensus sequence as a SequenceObject
 	 */
 	public SequenceObjectXT getConsensus(){
 		return sequences.get(contigname);
 	}
 	
 
 	public void setConsensus(SequenceObjectXT s) {
 		this.sequences.put(contigname, s);
 	}
 
 	public boolean canAddSequenceObjects() {
 		return true;
 	}
 
 	public void addSequenceObject(SequenceObjectXT object) {
 		object.setPositionInList(position);
 		position++;
 		this.sequences.put(object.getIdentifier(), object);
 	}
 
 	public boolean canRemoveSequenceObjects() {
 		return true;
 	}
 
 	public void removeSequenceObject(String name) {
 		this.sequences.remove(name);
 	}
 	
 	public BioFileType getFileType() {
 		return BioFileType.CONTIG_BASIC;
 	}
 
 	public SequenceObject getSequence(String s) {
 		return sequences.get(s);
 	}
 		
 	public String[] saveFile(File file, BioFileType filetype) throws Exception {
 		if(filetype == BioFileType.CLUSTAL_ALN){
 			ClustalAlign align = new ClustalAlign();
 			for(String k : this.sequences.keySet()){
 				if(sequences.get(k).getPositionInList()==0){
 					align.addSequenceObject(sequences.get(k));
 				}
 				else{
 					SequenceObjectXT o = sequences.get(k);
 					int off = o.getOffset(0);
 					if(off < 0){
 						o.leftTrim(off*-1, 0);
 					}
 					else if(off > 0){
 						o.extendLeft(off);
 					}
 					align.addSequenceObject(o);
 				}
 			}
 			return align.saveFile(file, filetype);
 		}
 		else{
 			logger.error("Not implemented");
 			return null;
 		}
 	}
 
 	public int loadFile(File file, BioFileType filetype) throws Exception,
 			UnsupportedTypeException {
 		logger.error("Not implemented");
 		return -1;
 	}
 
 
 	public int trimLeftAllContig() {
 		logger.error("Not implemented");
 		return 0;
 	}
 
 	public int trimRightAllContig() {
 		logger.error("Not implemented");
 		return 0; 
 	}
 
 	public Contig[] removeSectionAllContig(int opts) {
 		logger.error("Not implemented");
 		return null;
 	}
 
 	
 	public void setRange(String s, int start, int stop, int base){
 		checkSequence(s);
 		sequences.get(s).setRange(start, stop, base);
 	}
 	
 	public void setPaddedRange(String s, int start, int stop, int base){
 		checkSequence(s);
 		sequences.get(s).setPaddedRange(start, stop, base);
 	}
 
 	private void checkSequence(String s) {
 		if(!sequences.containsKey(s)){
 			sequences.put(s, new GenericSequenceXT(s, position));
 			position++;
 		}
 	}
 
 	public int createPosition() {
 		return this.position++;
 	}
 
 	public String[] getReadNames() {
 		String[] str = new String[this.getNoOfSequences()];
 		for(String s : this.sequences.keySet()){
 			int i = this.sequences.get(s).getPositionInList()-1;
 			if(i != -1){
 				str[i] = this.sequences.get(s).getIdentifier();
 			}
 		}
 		return str;
 	}
 
 	public int getOffset(String s, int base) {
 		return this.sequences.get(s).getOffset(base);
 	}
 
 	public void setOffset(String s, int offset, int base){
 		checkSequence(s);
 		this.sequences.get(s).setOffset(offset, base);
 	}
 	
 	public int[] getRange(String s, int base) {
 		return this.sequences.get(s).getRange(base);
 	}
 
 	public int[] getPaddedRange(String s, int base) {
 		return this.sequences.get(s).getPaddedRange(base);
 	}
 
 	public void setCompliment(String s, char c) {
 		this.sequences.get(s).setCompliment(c);
 	}
 
 	public char getCompliment(String s) {
 		return this.sequences.get(s).getCompliments();
 	}
 
 
 	/**
 	 * Sets the number of regions (BS)
 	 * 
 	 * @param i
 	 */
 	public void setNumberOfRegions(int i){
 		ArrayList<BasicRegion> regs = new ArrayList<BasicRegion>(i);
 		if(regions.size() !=0){
 			for(int j=0;j < regions.size(); j++){
 				regs.add(regions.get(j));
 			}
 		}
 		regions = regs;
 	}
 	
 	/**
 	 * Add region for read 'readname'
 	 * @param i1
 	 * @param i2
 	 * @param readname
 	 */
 	public void addRegion(int i1, int i2, String readname, int base){		
 		regions.add(new BasicRegion(i1-base, i2-base, 0, readname));
 	}
 
 	public ArrayList<BasicRegion> getRegions() {
 		return this.regions;
 	}
 
 	public char getConsensusCompliment() {
 		return this.getConsensus().getCompliments();
 	}
 
 	public void setConsensusCompliment(char c) {
 		if(this.getConsensus() == null){
 			if(this.getContigName() == null){
 				logger.error("Contig name must be set before adding contig information");
 			}
 			this.setConsensus(new GenericSequenceXT(contigname, 0));
 		}
 		this.getConsensus().setCompliment(c);
 	}
 
 	public void addSequenceObject(SequenceObject object) {
 		this.sequences.put(object.getIdentifier(), new GenericSequenceXT(object.getIdentifier(), object.getSequence(), object.getQuality(), position));
 		position++;
 	}
 
 	public void setConsensus(SequenceObject s) {
 		this.sequences.put(s.getIdentifier(), new GenericSequenceXT(s.getIdentifier(), s.getSequence(), s.getQuality(), 0));
 		this.contigname = s.getIdentifier();
 	}
 	
 
 	public char getCharAtRelative2Contig(String s, int position, int base) {
 		return this.sequences.get(s).getSequence().charAt((position-base)+this.sequences.get(s).getOffset(0));
 	}
 	
 
 	public boolean isNoQual2fastq() {
 		return noQual2fastq;
 	}
 
 	public void setNoQual2fastq(boolean noQual2fastq) {
 		this.noQual2fastq = noQual2fastq;
 	}
 
 	
 }
