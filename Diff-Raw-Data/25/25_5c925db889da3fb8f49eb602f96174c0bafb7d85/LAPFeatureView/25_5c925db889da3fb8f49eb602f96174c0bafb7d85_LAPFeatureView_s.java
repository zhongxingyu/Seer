 package Engine;
 
 
 import ViewCanvas.LAPFeature;
 import ViewCanvas.LAPFeatureInterval;
 import ViewCanvas.LAPFeatureType;
 
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.Sequence;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.alignment.AlignmentSequenceIndexer;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.feature.Feature;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.index.BasicIndexer;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.interval.Interval;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.region.Region;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.region.RegionTools;
 import com.clcbio.api.free.framework.workspace.WorkspaceManager;
 import com.clcbio.api.free.workbench.WorkbenchManager;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Set;
 
 public class LAPFeatureView {
 	
 	Sequence seq;
 	List<LAPFeatureType> types;
 	List<LAPFeatureType> relevantTypes;
 	List<LAPFeature> features;
 	private int typeHeight = 24;
 	private int typeWidth;
 	private LAP root;
 	
 	private int featuresUpperY;
 	private int featuresLowerY;
 	
 	
 	private boolean showAnnotations;
 	private boolean showArrows;
 	private boolean showGradients;
 	
 	
 	
 	public LAPFeatureView(Sequence seq, LAP root){
 		this.root = root;
 		this.seq = seq;
 		
 		
 		this.showAnnotations = true;
 		this.showArrows = true;
 		this.showGradients = true;
 		
 		this.types = new ArrayList<LAPFeatureType>();
 		this.relevantTypes = new ArrayList<LAPFeatureType>();
 		
 		features = new ArrayList<LAPFeature>();
 		typeWidth = seq.getLength();
 		buildFeatureTypes(root);
 	}
 	
 
 public void buildFeatureTypes(LAP root){
 	String s = com.clcbio.api.clc.datatypes.bioinformatics.structure.rnasecondary.annotation.RnaStructureAnnotation.class.getName();
 		//Get an iterator over the features and only proceed if at least one feature exists.
 		
 		Iterator<Feature> featureIter = seq.getFeatureIterator();
 		
 		
 		if(featureIter.hasNext()) types = new ArrayList<LAPFeatureType>();
 		else{
 			return;
 		}
 		
 		//Build LAPFeatureType elements for each different type of feature in the sequence.
 		buildTypes(seq.getFeatureTypes(),root);
 		
 		//Get type iterator
 		if(types == null){
			System.out.println("types er null");
 		}
 		Iterator<LAPFeatureType> typeIter = types.iterator();
 		LAPFeatureType cur = typeIter.next();
 		
 		int curOverlaps = 0;
 		int offset = 60;
 		
 		BasicIndexer indexer = new AlignmentSequenceIndexer(root.getAlign(), root.getCurrentSequenceNumber());
 		while(featureIter.hasNext()){
 			Feature fet = featureIter.next(); 
 			
 					if(fet.getType().compareToIgnoreCase(cur.getName()) != 0){
 				cur = changeCurrentType(fet.getType());
 			}
 			if(fet.getName().length() > 20) continue;
 						
 			Region fetregion = fet.getRegion();
 			int fetstart = fetregion.getFirstPos().getMin(); 
 			int fetstop = fetregion.getLastPos().getMin(); 
 			
 			LAPFeature tmp = new LAPFeature(fet.getName(), fetstart, fetstop, offset, root);
 			
 			Iterator<Interval> II = fetregion.getIntervalIterator();
 			
 			while(II.hasNext()){
 				Interval in = II.next();
 				LAPFeatureInterval li = new LAPFeatureInterval(fet.getName(), RegionTools.convertPosition(in.getFirstPos().getMin(),false,indexer),RegionTools.convertPosition(in.getLastPos().getMax(),false,indexer),cur.getTypeOffset(), root, cur);
 				tmp.addFeatureInterval(li);
 				
 				cur.addInterval(li);
 			}
 			features.add(tmp);
 			
 			
 		}			
 		
 		sortTypes();
 		
 		
 		//drawFeatures();
 		
 		relevantTypes = new ArrayList<LAPFeatureType>();
 		
 		buildRelevantTypes();
 }
 	
 private void sortTypes() {
 	for(LAPFeatureType t : types){
 		java.util.Collections.sort(t.getIntervals());
 	}	
 }
 
 public void buildRelevantTypes(){
 	int minusOffset = 0;
 	int startOffset = 60;
 	int curAdded = 0;
 	int expanded = 0;
 	
 	
 	double startPosX = 0;
 	double endPosX = 0;
 	
 	int curX;
 	int curWidth;
 	try{
 		curX = root.getXViewBounds();
 	} catch(java.lang.NullPointerException e){
 		curX = 0;
 	}
 	try{
 		curWidth = root.getViewPaneWidth();
 	} catch (java.lang.NullPointerException e){
 		curWidth = 100000;
 	}
 	
 	relevantTypes.clear();
 	for(LAPFeatureType l : types){
 		if(l.isSelected()){
 		if(l.getIntervals().size() > 0){
 			
 			boolean rel = false;
 			l.setTypeOffset(startOffset+((typeHeight+20)*curAdded)+(expanded*45));
 			for(LAPFeatureInterval li : l.getIntervals()){
 				//Check if type has at least one feature interval within the current view.
 				if(!rel){				
 					startPosX = li.getStartPos()*li.getScaleX();
 					endPosX = li.getEndPos()*li.getScaleX();
 					if(!(startPosX > curX+curWidth || endPosX < curX)){
 						rel = true;
 					}
 				}
 				li.setOffset(startOffset+((typeHeight+20)*curAdded)+(expanded*45));
 			}
 			if(rel) { 
 				relevantTypes.add(l);			
 				curAdded += 1;
 				l.setRelevant(true);
 				if(l.isExpanded()) expanded+=1;
 			} else {
 				l.setRelevant(false);
 			}
 		}
 		}
 	}
 	featuresLowerY = (expanded*45)+(relevantTypes.size()*(typeHeight+20))+startOffset;
 	featuresUpperY = startOffset;	
 }
 
 private LAPFeatureType changeCurrentType(String fetType){
 	for(LAPFeatureType type : types){
 		if( type.getName().compareToIgnoreCase(fetType) == 0) return type;
 	}
 	return null;
 }
 
 
 private void buildTypes(Set<String> s, LAP root){
 	int offset = 70;
 	for(String t : s){
 		types.add(new LAPFeatureType(t,0,offset,seq.getLength(),typeHeight,root));
 		offset+=typeHeight+20;
 	}
 }
 	
 public void drawFeatures(){
 	Queue<Integer> lvl = new PriorityQueue<Integer>();
 	int overlap = 0;	
 	int curLvl = 0;
 	int prevEnd = -998;
 	int curEnd=-999;
 	
 	
 	
 	for(LAPFeatureType l : types){
 		for(LAPFeatureInterval i : l.getIntervals()){
 			if(i.getStartPos() < curEnd){
 				i.addToOffset(overlap+5);
 				overlap+=1;
 				
 				if(i.getEndPos() < curEnd){
 					prevEnd = curEnd;
 					curEnd = i.getEndPos();					
 				}
 				
 			} else {
 				curEnd = i.getEndPos();
 				
 				overlap=0;
 				
 			}
 		}
 	}
 }
 
 public List<LAPFeature> getFeatures(){
 	return features;
 }
 
 public double getVersion() {
 return 1.0;
 }
 //Version of the plugin
 
 public String getName() {
 return "Sequence Feqtures";
 }
 //Human readable name of the plugin
 
 public String getClassKey() {
 return "alignment_annotations_exporter";
 }
 //Class identifier of the plugin
 
 
 public List<LAPFeatureType> getTypes() {
 	return types;
 }
 
 
 public void setTypes(List<LAPFeatureType> types) {
 	this.types = types;
 }
 
 
 public List<LAPFeatureType> getRelevantTypes() {
 	return relevantTypes;
 }
 
 
 public void setRelevantTypes(List<LAPFeatureType> relevantTypes) {
 	this.relevantTypes = relevantTypes;
 }
 
 public boolean isShowArrows() {
 	return showArrows;
 }
 
 public void setShowAnnotations(boolean b){
 	this.showAnnotations = b;
 	repaintTypes();
 }
 
 public boolean isShowAnnotations() {
 	return showAnnotations;
 }
 
 
 public boolean isShowGradients() {
 	return showGradients;
 }
 
 
 public void setShowGradients(boolean showGradients) {
 	this.showGradients = showGradients;
 	repaintTypes();
 }
 
 public void repaintTypes(){
 	System.out.println("Repainting types");
 	for(LAPFeatureType t : types){
 		for(LAPFeatureInterval i : t.getIntervals()){
 			i.repaintInterval();
 		}
 	}
 }
 
 public void setTypeAcces(String lastUpdated) {
 	for(LAPFeatureType l : types){
 		if(l.getName() == lastUpdated){
 			l.setSelected(!l.isSelected());
 			break;
 		}
 	}
 	repaintTypes();	
 }
 
 public void setTypeColor(String type, Color col){
 	for(LAPFeatureType l : types){
 		if(l.getName() == type){
 			l.setColor(col);
 			for(LAPFeatureInterval i : l.getIntervals()){
 				i.setCol(col);
 			}
 			break;
 		}
 	}
 	repaintTypes();	
 }
 
 
 public void setShowView(String selected) {
 	if(selected.equals("arrows")){
 		for(LAPFeatureType l : types){
 			l.setAsLines(false);
 			l.setAsArrows(true);
 		}
 	} else if(selected.equals("lines")){
 		for(LAPFeatureType l : types){
 			l.setAsArrows(false);
 			l.setAsLines(true);
 		}	
 	}	
 }
 
 
 public int getFeaturesUpperY() {
 	return featuresUpperY;
 }
 
 
 public void setGetFeaturesUpperY(int featuresUpperY) {
 	this.featuresUpperY = featuresUpperY;
 }
 
 
 public int getFeaturesLowerY() {
 	return showAnnotations ? featuresLowerY : 0;
 }
 
 
 public void setFeaturesLowerY(int featuresLowerY) {
 	this.featuresLowerY = featuresLowerY;
 }
 
 }
