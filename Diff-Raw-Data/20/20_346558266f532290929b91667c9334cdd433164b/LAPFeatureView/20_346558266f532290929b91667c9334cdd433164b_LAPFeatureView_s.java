 package LinearArcPlotEditor;
 
 
 import com.clcbio.api.base.io.ClcFileFilter;
 import com.clcbio.api.base.io.ExportPlugin;
 import com.clcbio.api.base.persistence.PersistenceException;
 import com.clcbio.api.base.process.Activity;
 import com.clcbio.api.base.util.iterator.MovableIntegerIterator;
 import com.clcbio.api.clc.datatypes.bioinformatics.structure.rnasecondary.RnaStructures;
 import com.clcbio.api.clc.datatypes.bioinformatics.structure.rnasecondary.annotation.RnaStructureAnnotation;
 import com.clcbio.api.clc.graphics.framework.ChildDrawingNode;
 import com.clcbio.api.free.datatypes.ClcObject;
 import com.clcbio.api.free.datatypes.ClcString;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.Sequence;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.alignment.Alignment;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.feature.Feature;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.interval.Interval;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.region.Region;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.rnasecondary.RnaStructureAnnotationTools;
 import com.clcbio.api.free.gui.dialog.ClcMessages;
 import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;
 //import com.ppfold.algo.ExportTools;
 //import com.ppfold.algo.MatrixTools;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Set;
 
 public class LAPFeatureView {
 	
 	Sequence seq;
 	List<LAPFeatureType> types;
 	List<LAPFeatureType> relevantTypes;
 	List<LAPFeature> features;
	private int typeHeight = 30;
 	private int typeWidth;
 	private LAP root;
 	
 	public LAPFeatureView(Sequence seq, LAP root){
 		this.root = root;
 		this.seq = seq;
 		
 		features = new ArrayList<LAPFeature>();
 		typeWidth = seq.getLength();
 		buildFeatureTypes(root);
 	}
 	
 
 public void buildFeatureTypes(LAP root){
 	String s = com.clcbio.api.clc.datatypes.bioinformatics.structure.rnasecondary.annotation.RnaStructureAnnotation.class.getName();
 		//Get an iterator over the features and only proceed if at least one feature exists.
 		
 		Iterator<Feature> featureIter = seq.getFeatureIterator();
 		
 		
 		if(featureIter.hasNext()) types = new ArrayList<LAPFeatureType>();
 		
 		//Build LAPFeatureType elements for each different type of feature in the sequence.
 		buildTypes(seq.getFeatureTypes(),root);
 		
 		//Get type iterator
 		Iterator<LAPFeatureType> typeIter = types.iterator();
 		LAPFeatureType cur = typeIter.next();
 		
 		
 		int curOverlaps = 0;
 		int offset = 60;
 		
 		
 		while(featureIter.hasNext()){
 			Feature fet = featureIter.next(); 
 			System.out.println(fet.getType() + " is the type of the feature and " + cur.getName() + " is the name of the type");
 			if(fet.getType().compareToIgnoreCase(cur.getName()) != 0){
 				cur = changeCurrentType(fet.getType());
 			}
 			System.out.println("Feature type = " + fet.getType() + " Type Name = " + cur.getName());
 			if(fet.getName().length() > 20) continue;
 						
 			Region fetregion = fet.getRegion();
 			int fetstart = fetregion.getFirstPos().getMin(); 
 			int fetstop = fetregion.getLastPos().getMin(); 
 			
 			LAPFeature tmp = new LAPFeature(fet.getName(), fetstart, fetstop, offset, root);
 			
 			Iterator<Interval> II = fetregion.getIntervalIterator();
 			
 			while(II.hasNext()){
 				Interval in = II.next();
 				LAPFeatureInterval li = new LAPFeatureInterval(fet.getName(), in.getFirstPos().getMin(),in.getLastPos().getMax(),cur.getTypeOffset(), root);
 				li.setType(cur);
 				tmp.addFeatureInterval(li);
 				//System.out.println("Interval \n First Position: " + in.getFirstPos() + " \n Last Position: " + in.getLastPos());
 				//System.out.println("Interval \n First Position.getMin: " + in.getFirstPos().getMin() + " \n Last Position.getMin: " + in.getLastPos().getMin());
 				//System.out.println("Interval \n First Position.getMax: " + in.getFirstPos().getMax() + " \n Last Position.getMax: " + in.getLastPos().getMax());
 				cur.addInterval(li);
 			}
 			features.add(tmp);
 			
 			
 		}			
 		
 		sortTypes();
 		
 		
 		//drawFeatures();
 		
 		relevantTypes = new ArrayList<LAPFeatureType>();
 		
 		
 }
 	
 private void sortTypes() {
 	for(LAPFeatureType t : types){
 		java.util.Collections.sort(t.getIntervals());
 	}	
 }
 
 public void buildRelevantTypes(){
 	int minusOffset = 0;
 	int startOffset = 50;
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
 	System.out.println(curX + " is current x");
 	System.out.println(curWidth + " is current width");
 	System.out.println(curWidth/root.getScaleX());
 	
 	relevantTypes.clear();
 	for(LAPFeatureType l : types){
 		if(l.getIntervals().size() > 0){
 			boolean rel = false;
 			l.setTypeOffset(startOffset+((typeHeight+20)*curAdded)+(expanded*30));
 			for(LAPFeatureInterval li : l.getIntervals()){
 				//Check if type has at least one feature interval within the current view.
 				if(!rel){				
 					startPosX = li.getStartPos()*li.getScaleX();
 					endPosX = li.getEndPos()*li.getScaleX();
 					if(!(startPosX > curX+curWidth || endPosX < curX)){
 						rel = true;
 					}
 				}
 				System.out.println(li.getStartPos() + " is start pos" + li.getEndPos() + " is end pos ");
 				System.out.println(startPosX + " is start pos" + endPosX + " is end pos ");
 				li.setOffset(startOffset+((typeHeight+20)*curAdded)+(expanded*30));
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
 
 private LAPFeatureType changeCurrentType(String fetType){
 	for(LAPFeatureType type : types){
 		if( type.getName().compareToIgnoreCase(fetType) == 0) return type;
 	}
 	return null;
 }
 
 
 private void buildTypes(Set<String> s, LAP root){
 	int offset = 50;
 	for(String t : s){
 		System.out.println(t);
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
 
 }
