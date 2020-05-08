 package org.eclipse.recommenders.livedoc.javadoc;
 
 import java.net.URL;
 
 import com.sun.tools.doclets.internal.toolkit.taglets.Taglet;
 
 public abstract class RecommendersTaglet implements Taglet, Comparable<RecommendersTaglet> {
     
     abstract void setGroupId(String groupId);
     abstract void setArtifactId(String artifactId);
     abstract void setArtifactVersion(String version);
 
     /**
      * Do everything necessary to get ready for documentation output,
      * e.g. download models etc. 
      * @throws Exception 
      */
     public abstract void initialize();
     
     /**
      * Do everthing necessary when this taglet is finished (e.g. close streams etc.)
      */
     public abstract void finish();
 
     abstract void setModelsRepo(URL modelsRepo);
     
     protected int getRanking() {
         return Integer.MAX_VALUE;
     }
     
     @Override
     public int compareTo(RecommendersTaglet o) {
        return (Integer.valueOf(this.getRanking()).compareTo(o.getRanking()));
     }
 }
