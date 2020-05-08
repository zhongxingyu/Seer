 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package utils;
 
 /**
  *
  * @author Markov
  */
 public class Normalize {
    /**
     * Normalizza un valore
     * @param val valore da normalizzare
     * @param min minimo
     * @param max massimo
     * @param minRange valore minimo del range normalizzato
     * @param maxRange valore massimo del range normalizzato
     * @return valore normalizzato
     */
    static public double normalize(double val, double min, double max, 
            double minRange, double maxRange){
       return ((val - min) / (max - min)) * (maxRange - minRange) + minRange;
    }
 
    /**
     * Normalizza un valore tra zero e uno
     * @param val valore da normalizzare
     * @param min minimo
     * @param max massimo
     * @return valore normalizzato
     */
    static public double normalize(double val, double min, double max){
       return normalize(val, min, max, 0, 1);
    }
 
    /**
     * Denormalizza un valore
     * @param val valore da denormalizzare
     * @param min minimo
     * @param max massimo
     * @param minRange valore minimo del range normalizzato
     * @param maxRange valore massimo del range normalizzato
     * @return valore denormalizzato
     */
    static public double denormalize(double val, double min, double max,
            double minRange, double maxRange){
      return (val + minRange) / (maxRange - minRange) * (max - min) + min;
    }
 
    /**
     * Deormalizza un valore tra zero e uno
     * @param val valore da denormalizzare
     * @param min minimo
     * @param max massimo
     * @return valore denormalizzato
     */
    static public double denormalize(double val, double min, double max){
       return denormalize(val, min, max, 0, 1);
    }
 }
