 package net.malariagen.gatk.coverage;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import net.sf.samtools.BAMRecord;
 import net.sf.samtools.SAMReadGroupRecord;
 
 import org.broadinstitute.sting.commandline.Argument;
 import org.broadinstitute.sting.commandline.Output;
 import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
 import org.broadinstitute.sting.gatk.datasources.reads.SAMDataSource;
 import org.broadinstitute.sting.gatk.filters.BadMateFilter;
 import org.broadinstitute.sting.gatk.filters.NotPrimaryAlignmentFilter;
 import org.broadinstitute.sting.gatk.filters.UnmappedReadFilter;
 import org.broadinstitute.sting.gatk.refdata.ReadMetaDataTracker;
 import org.broadinstitute.sting.gatk.samples.Sample;
 import org.broadinstitute.sting.gatk.walkers.BAQMode;
 import org.broadinstitute.sting.gatk.walkers.By;
 import org.broadinstitute.sting.gatk.walkers.DataSource;
 import org.broadinstitute.sting.gatk.walkers.ReadFilters;
 import org.broadinstitute.sting.gatk.walkers.ReadWalker;
 import org.broadinstitute.sting.gatk.walkers.Requires;
 import org.broadinstitute.sting.gatk.walkers.TreeReducible;
import org.broadinstitute.sting.gatk.walkers.Window;
 import org.broadinstitute.sting.utils.baq.BAQ;
 import org.broadinstitute.sting.utils.exceptions.UserException;
 import org.broadinstitute.sting.utils.sam.GATKSAMRecord;
 import org.jets3t.service.model.S3Object;
 
 @ReadFilters({ BadMateFilter.class, UnmappedReadFilter.class,
 	NotPrimaryAlignmentFilter.class })
 @By(DataSource.READS)
 @Requires(DataSource.READS)
 @BAQMode(QualityMode = BAQ.QualityMode.DONT_MODIFY, ApplicationTime = BAQ.ApplicationTime.HANDLED_IN_WALKER)
 public class FragmentLengthWalker extends ReadWalker<GATKSAMRecord,FragmentLengths> implements TreeReducible<FragmentLengths> {
 
 
 	@Argument(shortName="maxfl", fullName="max_fragment_length", doc = "maximum admissible fragment length; mapped read pairs on the same cromosome that have a larger fragment size would be discarded", required = false)
 	public int maxLength = 10000;
 	
 	BAMRecord r;
 //	@Argument(fullName="overwrite", shortName="ow", doc = "allow the program to overwrite the content of the output directory if it already exists and is not empty",required = false)
 //	public boolean overwrite = false;
 	
 	
 	@Output(shortName="o", fullName="output_directory", doc = "directory where to show the output the fragment length summary stats",required = true)
 	public File outDir;
 	
 	
 	private Set<String> sampleIds;
 	
 	private Set<String> rgIds;
 	
 	
 	@Override
 	public GATKSAMRecord map(ReferenceContext ref, GATKSAMRecord read,
 			ReadMetaDataTracker metaDataTracker) {
 		return read;
 	}
 	
 	@Override
 	public void initialize() {
 		super.initialize();
		Window w;
 		Collection<? extends Sample> samples = getToolkit().getSampleDB().getSamples();
 		SAMDataSource rds = getToolkit().getReadsDataSource();
 		Collection<? extends SAMReadGroupRecord> rgs = 
 		rds.getHeader().getReadGroups();
 		sampleIds = new LinkedHashSet<String>(samples.size());
 		rgIds = new LinkedHashSet<String>(rgs.size());
 		for (Sample s : samples)
 			sampleIds.add(s.getID());
 		for (SAMReadGroupRecord r : rgs)
 			rgIds.add(r.getId());
 		checkOutDir();
 	}
 
 	private void checkOutDir() {
 		if (outDir == null)
 			throw new IllegalStateException("output directory paramer not set");
 		if (outDir.exists()) {
 			if (!outDir.isDirectory())
 				throw new UserException("the output directory provided '" + outDir + "' is not in fact a directory ");
 			if (!outDir.canWrite())
 				throw new UserException("the output directory provided '" + outDir + "' is not writable");
 //			FileFilter ff = new FileFilter() {
 //				@Override
 //				public boolean accept(File pathname) {
 //					if (pathname.equals(".") || pathname.equals(".."))
 //						return false;
 //					return true;
 //				}
 //			};
 			
 //			if (!overwrite && outDir.listFiles(ff).length >= 0)
 //				throw new UserException("the output directory provided '" + outDir + "' is not empty but overwrite was not forced (option --overwrite or -ow) ");
 		}
 		else {
 			File parent = outDir.getParentFile();
 			if (parent == null) 
 				parent = new File(".");
 			if (!parent.exists())
 				throw new UserException("the out directory parent '" + parent + "' does not exists");
 			if (!parent.isDirectory())
 				throw new UserException("the out directory parent '" + parent + "' is not a directory");
 			if (!parent.canWrite())
 				throw new UserException("cannot write in the out directory parent directory '" + parent + "'");
 		}
 		
 	}
 
 	@Override
 	public FragmentLengths reduceInit() {
 		return FragmentLengths.create(sampleIds, rgIds, maxLength);
 	}
 
 	@Override
 	public FragmentLengths reduce(
 			GATKSAMRecord value,
 			FragmentLengths sum) {
 		return FragmentLengths.add(value,sum);
 	}
 
 	@Override
 	public FragmentLengths treeReduce(FragmentLengths lhs, FragmentLengths rhs) {
 		return FragmentLengths.merge(lhs,rhs);
 	}
 
 	@Override
 	public boolean requiresOrderedReads() {
 		return true;
 	}
 
 	@Override
 	public boolean includeReadsWithDeletionAtLoci() {
 		return true;
 	}
 
 
 	@Override
 	public void onTraversalDone(FragmentLengths result) {
 		super.onTraversalDone(result);
 		FragmentLengthSummary summary = result.summary();
 		try {
 			summary.saveIn(outDir);
 			File sf = new File(outDir,FragmentLengthSummary.SUMMARY_FILE_NAME);
 			BufferedReader r = new BufferedReader(new FileReader(sf));
 			String line;
 			while ((line = r.readLine()) != null)
 				logger.info(line);
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e.getMessage(),e);
 		}
 	}
 
 	@Override
 	public boolean isReduceByInterval() {
 		return false;
 	}
 	
 	
 
 }
