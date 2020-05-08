 package org.pillarone.riskanalytics.graph.pc.components.claims;
 
 import org.pillarone.riskanalytics.core.components.ComponentCategory;
 import org.pillarone.riskanalytics.core.util.MathUtils;
 import umontreal.iro.lecuyer.randvar.LognormalGen;
 import umontreal.iro.lecuyer.randvar.RandomVariateGen;
 
 /**
  * @author martin.melchior (at) fhnw (dot) ch
  * @author stefan.kunz (at) intuitive-collaboration (dot) com
  */
 @ComponentCategory(categories={"Claims","Generator"})
 public class LognormalClaimsGenerator extends AbstractClaimsGenerator {
 
     private double parmMean = 0d;
     private double parmStdev = 0d;
 
     @Override
     RandomVariateGen getSeverityGenerator() {
        return new LognormalGen(MathUtils.getRandomStreamBase(), parmMean, parmStdev);
     }
 
     public double getParmMean() {
         return parmMean;
     }
 
     public void setParmMean(double parmMean) {
         this.parmMean = parmMean;
     }
 
     public double getParmStdev() {
         return parmStdev;
     }
 
     public void setParmStdev(double parmStdev) {
         this.parmStdev = parmStdev;
     }
 }
