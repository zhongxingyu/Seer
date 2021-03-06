 package org.broadinstitute.sting.gatk.traversals;
 
 import org.broadinstitute.sting.gatk.walkers.ReadWalker;
 import org.broadinstitute.sting.gatk.walkers.LocusWalker;
 import org.broadinstitute.sting.gatk.walkers.Walker;
 import org.broadinstitute.sting.gatk.LocusContext;
 import org.broadinstitute.sting.gatk.refdata.ReferenceOrderedData;
 import org.broadinstitute.sting.gatk.refdata.ReferenceOrderedDatum;
 import org.broadinstitute.sting.gatk.iterators.ReferenceIterator;
 import org.broadinstitute.sting.gatk.iterators.LocusIterator;
 import org.broadinstitute.sting.gatk.iterators.LocusIteratorByHanger;
 import org.broadinstitute.sting.utils.GenomeLoc;
 import org.broadinstitute.sting.utils.Utils;
 
 import java.util.List;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.io.File;
 
 import net.sf.samtools.SAMRecord;
 import net.sf.samtools.util.CloseableIterator;
 import edu.mit.broad.picard.filter.FilteringIterator;
 
 /**
  * Created by IntelliJ IDEA.
  * User: mdepristo
  * Date: Mar 27, 2009
  * Time: 10:26:03 AM
  * To change this template use File | Settings | File Templates.
  */
 public class TraverseByLoci extends TraversalEngine {
 
     public TraverseByLoci(File reads, File ref, List<ReferenceOrderedData> rods) {
         super(reads, ref, rods);
     }
 
     public <M,T> T traverse(Walker<M,T> walker, ArrayList<GenomeLoc> locations) {
         if ( walker instanceof LocusWalker ) {
             Walker x = walker;
             LocusWalker<?, ?> locusWalker = (LocusWalker<?, ?>)x;
             return (T)this.traverseByLoci(locusWalker, locations);
         } else {
             throw new IllegalArgumentException("Walker isn't a loci walker!");
         }
     }
 
     /**
      * Traverse by loci -- the key driver of linearly ordered traversal of loci.  Provides reads, RODs, and
      * the reference base for each locus in the reference to the LocusWalker walker.  Supports all of the
      * interaction contract implied by the locus walker
      *
      * @param walker A locus walker object
      * @param <M>    MapType -- the result of calling map() on walker
      * @param <T>    ReduceType -- the result of calling reduce() on the walker
      * @return 0 on success
      */
     protected <M, T> T traverseByLoci(LocusWalker<M, T> walker, ArrayList<GenomeLoc> locations) {
         logger.debug("Entering traverseByLoci");
 
         samReader = initializeSAMFile(readsFile);
 
         verifySortOrder(true);
 
         // initialize the walker object
         walker.initialize();
 
         T sum = walker.reduceInit();
         if ( ! locations.isEmpty() ) {
             logger.debug("Doing interval-based traversal");
 
             if ( ! samReader.hasIndex() )
                 Utils.scareUser("Processing locations were requested, but no index was found for the input SAM/BAM file. This operation is potentially dangerously slow, aborting.");
 
             // we are doing interval-based traversals
             for ( GenomeLoc interval : locations ) {
                 logger.debug(String.format("Processing locus %s", interval.toString()));
 
                 CloseableIterator<SAMRecord> readIter = samReader.queryOverlapping( interval.getContig(),
                         (int)interval.getStart(),
                         (int)interval.getStop() );
 
                 Iterator<SAMRecord> wrappedIter = WrapReadsIterator( readIter, false );
                 sum = carryWalkerOverInterval(walker, wrappedIter, sum, interval);
                 readIter.close();
             }
         }
         else {
             // We aren't locus oriented
             logger.debug("Doing non-interval-based traversal");
             samReadIter = WrapReadsIterator(getReadsIterator(samReader), true);
             sum = carryWalkerOverInterval(walker, samReadIter, sum, null);
         }
 
         printOnTraversalDone("loci", sum);
         walker.onTraversalDone(sum);
         return sum;
     }
 
     protected <M, T> T carryWalkerOverInterval( LocusWalker<M, T> walker, Iterator<SAMRecord> readIter, T sum, GenomeLoc interval ) {
         logger.debug(String.format("TraverseByLoci.carryWalkerOverInterval Genomic interval is %s", interval));
 
         // prepare the read filtering read iterator and provide it to a new locus iterator
         FilteringIterator filterIter = new FilteringIterator(readIter, new locusStreamFilterFunc());
 
         boolean done = false;
         LocusIterator iter = new LocusIteratorByHanger(filterIter);
         while (iter.hasNext() && !done) {
             this.nRecords++;
 
             // actually get the read and hand it to the walker
             LocusContext locus = iter.next();
             if ( DOWNSAMPLE_BY_COVERAGE )
                 locus.downsampleToCoverage(downsamplingCoverage);
 
             // if we don't have a particular interval we're processing, check them all, otherwise only operate at this
             // location
             if ( interval == null || interval.overlapsP(locus.getLocation()) )  {
                 ReferenceIterator refSite = refIter.seekForward(locus.getLocation());
                 locus.setReferenceContig(refSite.getCurrentContig());
 
                 // Iterate forward to get all reference ordered data covering this locus
                 final List<ReferenceOrderedDatum> rodData = getReferenceOrderedDataAtLocus(rodIters, locus.getLocation());
 
                 sum = walkAtLocus( walker, sum, locus, refSite, rodData );
 
                 //System.out.format("Working at %s\n", locus.getLocation().toString());
 
                 if (this.maxReads > 0 && this.nRecords > this.maxReads) {
                     logger.warn(String.format("Maximum number of reads encountered, terminating traversal " + this.nRecords));
                     done = true;
                 }
 
             }
 
             done = interval != null && locus.getLocation().isPast(interval);
             //System.out.printf("done is %b, %s vs. %s%n", done, interval, locus.getLocation());
         }
         return sum;
     }
 
     protected <M, T> T walkAtLocus( final LocusWalker<M, T> walker,
                                     T sum, 
                                     final LocusContext locus,
                                     final ReferenceIterator refSite,
                                     final List<ReferenceOrderedDatum> rodData ) {
         final char refBase = refSite.getBaseAsChar();
 
         //logger.debug(String.format("  Reference: %s:%d %c", refSite.getCurrentContig().getName(), refSite.getPosition(), refBase));
 
         //
         // Execute our contract with the walker.  Call filter, map, and reduce
         //
         final boolean keepMeP = walker.filter(rodData, refBase, locus);
         if (keepMeP) {
             M x = walker.map(rodData, refBase, locus);
             sum = walker.reduce(x, sum);
         }
 
         printProgress("loci", locus.getLocation());
         return sum;
     }
 }
