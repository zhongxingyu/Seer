 package edu.x3m.kas.monitors;
 
 
 /**
 * Iface for monitoring huffmann's algorithm progress.
 * 
  * @author Hans
  */
 public interface IHuffmannMonitor {
 
 
     /**
      * Dispatched when section starts
      *
      * @param section name of the section
      */
     void onSectionStart (String section);
 
 
 
     /**
      * Dispatched when section ends
      *
      * @param section name of the section
      */
     void onSectionEnd (String section);
 
 
 
     /**
      * Dispatched when progress is changed
      *
      * @param section name of the section
      * @param prc     value from 0 to 100 indication percentual progress
      */
     void onSectionProgress (String section, int prc);
 }
