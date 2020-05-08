 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package weka.core;
 import java.util.Vector;
 
 /**
  *
  * @author Todor Tsonkov
  */
 public class KPrototypes_DistanceFunction extends NormalizableDistance
   implements Cloneable  {
 
   private double m_Gamma;
 
     /** for serialization. */
   private static final long serialVersionUID = 1068606253458807903L;
 
   public KPrototypes_DistanceFunction( double gamma){
         m_Gamma = gamma;
   }
 
   public KPrototypes_DistanceFunction() {
     super();
     m_Gamma = 0.5;
   }
 
   public KPrototypes_DistanceFunction(Instances data, double gamma) {
     super(data);
     m_Gamma = gamma;
   }
 
   /**
    * Returns a string describing this object.
    *
    * @return 		a description of the evaluator suitable for
    * 			displaying in the explorer/experimenter gui
    */
   public String globalInfo() {
     return
         "Implementing KPrototypes distance (or similarity) function.\n\n"
       + "One object defines not one distance but the data model in which "
       + "the distances between objects of that data model can be computed.\n\n"
       + "Attention: For efficiency reasons the use of consistency checks "
       + "(like are the data models of the two instances exactly the same), "
       + "is low.\n\n";
   }
 
     //MOST IMPORTANT PART!
   /**
    * Calculates the distance between two instances.
    *
    * @param first 	the first instance
    * @param second 	the second instance
    * @return 		the distance between the two given instances
    */
   @Override
   public double distance(Instance first, Instance second) {
      double sum_nominal = 0.0;
      double sum_continuous = 0.0;
      
       for(int i = 0; i < first.numAttributes(); i++){
         if(first.attribute(i).isNominal()){
             if(!first.attribute(i).equals(second.attribute(i)) )
                 sum_nominal+=1.0;
         }else
         if (first.attribute(i).isNumeric()){
            sum_continuous +=  Math.pow(first.m_AttValues[i] - second.m_AttValues[i], 2);
         }
       }
      
       return sum_continuous + m_Gamma * sum_nominal;
   }
 
   /**
    * Returns the tip text for this property
    * @return tip text for this property suitable for
    * displaying in the explorer/experimenter gui
    */
   public String gamma() {
     return "gamma parameter";
   }
 
   /**
    * fet the gamma parameter
    *
    * @return the gamma parameter
    */
   public double getGamma()
   {
     return m_Gamma;
   }
 
   /**
    * set the gamma parameter
    *
    * @param g the gamma parameter
    */
   public void setGamma(double g)
   {
     m_Gamma = g;
   }
 
   /**
    * Gets the current settings of BisectingKMeans
    *
    * @return an array of strings suitable for passing to setOptions()
    */
   @Override
   public String[] getOptions() {
     Vector<String>	result;
     String[]            options;
     
     result = new Vector<String>();
 
     result.add("-G");
     result.add("" + getGamma());
 
     options = super.getOptions();
     for (int i = 0; i < options.length; i++)
       result.add(options[i]);
 
     //result.
     return result.toArray(new String[result.size()]);
   }
 
   /**
    * Parses a given list of options.
    *
    * @param options 	the list of options as an array of strings
    * @throws Exception 	if an option is not supported
    */
   @Override
   public void setOptions(String[] options) throws Exception {
     String optionString;
 
     optionString = Utils.getOption("G", options);
     setGamma(Double.parseDouble(optionString));
 
     super.setOptions(options);
   }
 
 
   //TODO: IMPLEMENT?
   protected double updateDistance(double currDist, double diff) {
     double	result;
 
     result  = currDist;
     result += diff * diff;
 
     return result;
   }
 
   public String getRevision() {
     return RevisionUtils.extract("$Revision: 1.13 $");
   }
     
 }
