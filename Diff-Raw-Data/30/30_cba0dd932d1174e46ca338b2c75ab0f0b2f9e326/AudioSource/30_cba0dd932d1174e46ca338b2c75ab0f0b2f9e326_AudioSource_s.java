 /**
  * [[[copyright]]]
  */
 
 package edu.cmu.sphinx.frontend;
 
 import java.io.IOException;
 
 
 /**
 * A AudioSource produces Audio objects to be read by other objects.
  */
 public interface AudioSource {
 
     /**
      * Returns the next Audio object produced by this AudioSource.
      *
      * @return the next available Audio object, returns null if no
      *     Audio object is available
      *
      * @throws java.io.IOException
      */
     public Audio getAudio() throws IOException;
 }
