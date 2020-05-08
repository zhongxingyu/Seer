 package com.undeadscythes.supergenes;
 
 import com.undeadscythes.authenticmd.*;
 import com.undeadscythes.genebase.*;
 import com.undeadscythes.tipscript.*;
 import java.util.*;
 import static java.util.logging.Logger.*;
 
 /**
  * The main beef of {@link SuperGenes} is the map that contains a collection of
 * {@link GeneBase}s that can be manipulated using the various {@link Service}s.
  *
  * @author UndeadScythes
  */
 public class SuperGenes extends AuthentiCmd {
     private Map<String, GeneBase> genebases = new HashMap<String, GeneBase>(0);
     private GeneBase defaultGeneBase = null;
 
     /**
      * Constructs a new default {@link SuperGenes} program with input
      * {@link System#in} and a {@link TipScript} using a named logger and prompt
      * "SuperGenes:> ".
      *
      * @see AuthentiCmd#AuthentiCmd(InputStream, TipScript)
      */
     public SuperGenes() {
         super(System.in, new TipScript(getLogger(SuperGenes.class.getName()), "SuperGenes:> "));
     }
 
     /**
      * Grab a {@link GeneBase} with the specified name.
      */
     public GeneBase getGeneBase(final String name) {
         return genebases.get(name);
     }
 
     /**
     * Add a new {@link GeneBase} with a {@link GEDCOM} loaded from the provided
      * file path.
      */
     public void addGeneBase(final String path, final GeneBase geneBase) {
         genebases.put(geneBase.getUID().toString(), geneBase);
     }
 
     /**
      * Get the default {@link GeneBase} that will be used with all
     * {@link Service}s executed.
      */
     public GeneBase getDefault() {
         return defaultGeneBase;
     }
 
     /**
      * Set the {@link GeneBase} which will be used by default for each
     * {@link Service} executed.
      */
     public void setAuto(final GeneBase geneBase) {
         defaultGeneBase = geneBase;
     }
 
     /**
      * Get the {@link TipScript} used by this program to handle output.
      */
     public TipScript getTipScript() {
         return output;
     }
 }
