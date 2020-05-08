 /*
  * Copyright (C) 2012 SeqWare
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.seqware.queryengine.plugins.contribs;
 
 import com.github.seqware.queryengine.model.Feature;
 import com.github.seqware.queryengine.model.FeatureSet;
 import com.github.seqware.queryengine.plugins.runners.MapperInterface;
 import com.github.seqware.queryengine.plugins.runners.ReducerInterface;
 import com.github.seqware.queryengine.plugins.recipes.FilteredFileOutputPlugin;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import org.apache.hadoop.io.Text;
 
 /**
  * This plug-in implements an experiment with overlapping mutations in map
  * reduce
  *
  * @author dyuen
  * @version $Id: $Id
  */
 public class OverlappingMutationsAggregationPlugin extends FilteredFileOutputPlugin {
 
     private Text text = new Text();
     private Text textKey = new Text();
     
     @Override
     public void map(long position, Map<FeatureSet, Collection<Feature>> atoms, MapperInterface<Text, Text> mapperInterface) {
         // identify mutuations that are actually at this position
         Set<Feature> featuresAtCurrentLocation = new HashSet<Feature>();
         for (FeatureSet fs : atoms.keySet()) {
             for (Feature f : atoms.get(fs)) {
                 if (f.getStart() == position) {
                     featuresAtCurrentLocation.add(f);
                 }
             }
         }
 
         for (FeatureSet fs : atoms.keySet()) {
             for (Feature f : atoms.get(fs)) {
 
                 String fOverlapID = f.getTagByKey("id").getValue().toString();
 
                 for (Feature positionFeature : featuresAtCurrentLocation) {
                     String positionFeatureVarID = calculateVarID(positionFeature);
                     String positionOverlapID = positionFeature.getTagByKey("id").getValue().toString();
                    String fFeatureVarID = calculateVarID(f);
                     if (positionFeatureVarID.equals(fFeatureVarID)){
                         continue;
                     }
  
                     textKey.set(positionFeatureVarID);
                     text.set(fOverlapID);
                     // ( "10:100008435-100008436_G->A MU1157731" , "MU000001")
                     mapperInterface.write(textKey, text); 
                     // display the reverse overlap as well
                     textKey.set(fFeatureVarID);
                     text.set(positionOverlapID);
                     // ( "10:other_mutation MU000001" , "MU000001")
                     mapperInterface.write(textKey, text); 
                     
                 }
             }
         }
     }
 
     @Override
     public void reduce(Text key, Iterable<Text> values, ReducerInterface<Text, Text> reducerInterface) {
         // values 
         Set<Text> seenSet = new HashSet<Text>();
         String newFeatStr = "";
         boolean first = true;
         for (Text val : values) {
             if (seenSet.contains(val)){
                 continue;
             }
             seenSet.add(val);
             String[] fsArr = val.toString().split(",");
             for (String currFS : fsArr) {
                 if (first) {
                     first = false;
                     newFeatStr += currFS;
                 } else {
                     newFeatStr += "," + currFS;
                 }
             }
         }
         // ( "10:100008435-100008436_G->A MU1157731" , "MU000001 , MU000002, MU00003")
         text.set(key.toString() + "\t" + newFeatStr);
         reducerInterface.write(text, null);
     }
 
     private String calculateVarID(Feature positionFeature) {
         String ref = positionFeature.getTagByKey("ref_base").getValue().toString();
         String var = positionFeature.getTagByKey("call_base").getValue().toString();
         String id = positionFeature.getTagByKey("id").getValue().toString();
         String varID = positionFeature.getSeqid() + ":" + positionFeature.getStart()
                 + "-" + positionFeature.getStop() + "_" + ref + "->" + var + "\t" + id;
         return varID;
     }
 }
