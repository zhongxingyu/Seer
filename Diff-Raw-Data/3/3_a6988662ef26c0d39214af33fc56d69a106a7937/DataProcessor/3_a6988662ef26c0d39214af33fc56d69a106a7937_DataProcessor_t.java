 /*
  * Copyright 1999-2002 Carnegie Mellon University.  
  * Portions Copyright 2002 Sun Microsystems, Inc.  
  * Portions Copyright 2002 Mitsubishi Electronic Research Laboratories.
  * All Rights Reserved.  Use is subject to license terms.
  * 
  * See the file "license.terms" for information on usage and
  * redistribution of this file, and for a DISCLAIMER OF ALL 
  * WARRANTIES.
  *
  */
 
 
 package edu.cmu.sphinx.frontend;
 
 import edu.cmu.sphinx.util.SphinxProperties;
 import edu.cmu.sphinx.util.Timer;
 
 import java.io.IOException;
 
 
 /**
  * DataProcessor contains the common elements of all frontend data
  * processors, namely the name, context, timers, SphinxProperties,
  * and dumping. It also contains the acoustic properties object from
  * which acoustic model properties can be queried.
  */
 public abstract class DataProcessor {
 
 
     /**
      * The name of this DataProcessor.
      */
     private String name;
 
 
     /**
      * The context of this DataProcessor.
      */
     private String context;
 
 
     /**
      * A Timer for timing processing.
      */
     private Timer timer;
 
     
     /**
      * Indicates whether to dump the processed Data
      */
     private boolean dump = false;
 
 
     /**
      * The SphinxProperties used by this DataProcessor
      */
     private SphinxProperties sphinxProperties;
 
 
     // true if processing Data objects within an Utterance
     private boolean inUtterance;
 
     
     /**
      * Constructs a default DataProcessor
      */
     public DataProcessor() {}
 
 
     /**
      * Constructs a DataProcessor with the given name and context.
      * 
      * @param name the name of this DataProcessor
      * @param context the context of this DataProcessor
      */
     public DataProcessor(String name, String context) {
 	initialize(name, context, null);
     }
 
 
     /**
      * Constructs a DataProcessor of the given name and at the given context. 
      *
      * @param name the name of this DataProcessor
      * @param context the context of this DataProcessor
      * @param sphinxProperties the sphinx properties used
      */
     public DataProcessor(String name, String context, 
 			 SphinxProperties sphinxProperties) {
 	initialize(name, context, sphinxProperties);
     }
 
     
     /**
      * Initializes this DataProcessor.
      *
      * @param name the name of this DataProcessor
      * @param context the context of this DataProcessor
      * @param sphinxProperties the SphinxProperties to use
      */
     public void initialize(String name, String context, 
 			   SphinxProperties sphinxProperties) {
 	this.name = name;
 	this.context = context;
 	this.sphinxProperties = sphinxProperties;
 	this.timer = Timer.getTimer(context, name);
     }
 
 
     /**
      * Returns the name of this DataProcessor.
      *
      * @return the name of this DataProcessor
      */
     public final String getName() {
         return name;
     }
 
 
     /**
      * Returns the context of this DataProcessor.
      *
      * @return the context of this DataProcessor
      */
     public final String getContext() {
         return context;
     }
 
 
     /**
      * Returns the SphinxProperties used by this DataProcessor.
      *
      * @return the SphinxProperties
      */
     public final SphinxProperties getSphinxProperties() {
 	if (sphinxProperties != null) {
 	    return sphinxProperties;
 	} else {
 	    return SphinxProperties.getSphinxProperties(getContext());
 	}
     }
 
 
     /**
      * Sets the SphinxProperties to use.
      *
      * @param sphinxProperties the SphinxProperties to use
      */
     public void setSphinxProperties(SphinxProperties sphinxProperties) {
 	this.sphinxProperties = sphinxProperties;
     }
 
 
     /**
      * Returns the Timer for metrics collection purposes. 
      *
      * @return the Timer
      */
     public final Timer getTimer() {
         return timer;
     }
 
 
     /**
      * Determine whether to dump the output for debug purposes.
      *
      * @return true to dump, false to not dump
      */
     public final boolean getDump() {
 	return this.dump;
     }
 
 
     /**
      * Set whether we should dump the output for debug purposes.
      *
      * @param dump true to dump the output; false otherwise
      */
     public void setDump(boolean dump) {
 	this.dump = dump;
     }
 
 
     /**
      * Returns the name of this DataProcessor.
      *
      * @return the name of this DataProcessor
      */
     public String toString() {
         return name;
     }
 
 
     /**
      * Does sanity check on whether the Signals UTTERANCE_START and
      * UTTERANCE_END are in sequence. Throws an Error if:
      * <ol>
      * <li> We have not received an UTTERANCE_START Signal before
      *      receiving an Signal/Data.
      * <li> We received an UTTERANCE_START after an UTTERANCE_START
      *      without an intervening UTTERANCE_END;
      * </ol>
      *
      * @throws Error if the UTTERANCE_START and UTTERANCE_END signals
      *    are not in sequence
      */
     protected void signalCheck(Data data) {
 	if (!inUtterance) {
 	    if (data != null) {
 		if (data.hasSignal(Signal.UTTERANCE_START)) {
 		    inUtterance = true;
 		} else {
 		    throw new Error(getName() + ": no UTTERANCE_START");
 		}
 	    }
 	} else {
 	    if (data == null) {
		throw new Error
                    (getName() + ": unexpected return of null Data");
 	    } else if (data.hasSignal(Signal.UTTERANCE_END)) {
                 inUtterance = false;
             } else if (data.hasSignal(Signal.UTTERANCE_START)) {
 		throw new Error(getName() + ": too many UTTERANCE_START");
             }
         }
     }
 }
