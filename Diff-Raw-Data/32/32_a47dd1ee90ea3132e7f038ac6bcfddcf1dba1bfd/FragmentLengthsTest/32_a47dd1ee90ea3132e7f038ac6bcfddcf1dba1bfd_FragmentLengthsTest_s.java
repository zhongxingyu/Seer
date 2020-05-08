 package net.malariagen.gatk.coverage.test;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import net.malariagen.gatk.coverage.FragmentLengths;
 import net.malariagen.gatk.coverage.FragmentLengthSummary;
 import net.malariagen.gatk.math.IntegerDistribution;
 import net.malariagen.gatk.test.WalkerTest;
 import net.sf.samtools.SAMFileHeader;
 import net.sf.samtools.SAMSequenceDictionary;
 import net.sf.samtools.SAMSequenceRecord;
 
 import org.apache.commons.math.random.GaussianRandomGenerator;
 import org.apache.commons.math.random.MersenneTwister;
 import org.broadinstitute.sting.utils.sam.ArtificialSAMUtils;
 import org.broadinstitute.sting.utils.sam.GATKSAMReadGroupRecord;
 import org.broadinstitute.sting.utils.sam.GATKSAMRecord;
 import org.junit.Test;
 
 public class FragmentLengthsTest {
 
 	private static List<String> smIds = Arrays.asList(new String[] { "s1" });
 	private static List<GATKSAMReadGroupRecord> rgs = new ArrayList<GATKSAMReadGroupRecord>();
 
 	private static List<String> rgIds = Arrays.asList(new String[] { "rg1" });
 
 	private static String seq1Name = "SEQ1";
 	private static int seq1Length = 100000;
 	private static String seq2Name = "SEQ2";
 	private static int seq2Length = 100000;
 
 	private static SAMSequenceRecord seq1 = new SAMSequenceRecord(seq1Name,
 			seq1Length);
 	private static SAMSequenceRecord seq2 = new SAMSequenceRecord(seq2Name,
 			seq2Length);
 	private static List<SAMSequenceRecord> ssr = Arrays
 			.asList(new SAMSequenceRecord[] { seq1, seq2 });
 	private static SAMSequenceDictionary ssd = new SAMSequenceDictionary(ssr);
 	private static SAMFileHeader sh = new SAMFileHeader();
 
 	static {
 		try {
 			sh.setSequenceDictionary(ssd);
 			for (String id : rgIds) {
 				GATKSAMReadGroupRecord rg = new GATKSAMReadGroupRecord(id);
 				rg.setSample(smIds.get(0));
 				sh.addReadGroup(rg);
 				rgs.add(rg);
 			}
 		} catch (Throwable t) {
 			throw new RuntimeException(t);
 		}
 	}
 
 	private static int readLength = 76;
 
 	@Test
 	public void testCreate() {
 
		FragmentLengths fl = FragmentLengths.create(smIds, rgIds, 100);
 		assertNotNull(fl);
 		assert (fl.size() == 0);
 	}
 
 	@Test
 	public void testMerge() {
 		// TODO fail("Not yet implemented");
 	}
 
 	@Test
 	public void testMergeInFragmentLengths() {
 		// TODO fail("Not yet implemented");
 	}
 
 	@Test
 	public void testMergeInFragmentLengthArrays() {
 		// TODO fail("Not yet implemented");
 	}
 
 	@Test
 	public void testMergeInFragmentLengthFrequencies() {
 		// TODO fail("Not yet implemented");
 	}
 
 	@Test
 	public void addGappedPair() {
 		GATKSAMRecord[] pair = createMappedPair("ReadPair1", seq1Name, 10, 500,
 				"20S20M10I5D5M15S", "20S20M10I5D5M15S");
		FragmentLengths fl = FragmentLengths.create(smIds, rgIds, 100000);
 		fl.add(pair[0]);
 		assertEquals(0, fl.size());
 		fl.add(pair[1]);
 		assertEquals(1, fl.size());
 		FragmentLengthSummary summary = fl.summary();
 		assertNotNull(summary);
 		assertNotNull(summary.getSampleFragmentLengths("s1"));
 		assertEquals(1, summary.getSampleFragmentLengths("s1").count());
 		assertNotNull(summary.getReadGroupFragmentLengths("rg1"));
 		assertEquals(1, summary.getReadGroupFragmentLengths("rg1").count());
 		assertEquals(pair[1].getAlignmentEnd() - pair[0].getAlignmentStart()
 				+ 1 + 2 * 50, summary.getReadGroupFragmentLengths("rg1")
 				.maximum());
 		assertEquals(pair[1].getAlignmentEnd() - pair[0].getAlignmentStart()
 				+ 1 - 2 * readLength + 2 * 50, summary
 				.getReadGroupInsertLengths("rg1").maximum());
 		assertEquals(summary.getReadGroupFragmentLengths("rg1").maximum(),
 				summary.getSampleFragmentLengths("s1").maximum());
 		assertEquals(summary.getReadGroupInsertLengths("rg1").maximum(),
 				summary.getSampleInsertLengths("s1").maximum());
 		assertEquals(summary.getReadGroupFragmentLengths("rg1").minimum(),
 				summary.getSampleFragmentLengths("s1").maximum());
 		assertEquals(summary.getReadGroupInsertLengths("rg1").minimum(),
 				summary.getSampleInsertLengths("s1").maximum());
 	}
 
 	@Test
 	public void addBeyondMaxPair() {
 		GATKSAMRecord[] pair = createMappedPair("ReadPair1", seq1Name, 10, 500);
		FragmentLengths fl = FragmentLengths.create(smIds, rgIds, 100);
 		fl.add(pair[0]);
 		assertEquals(0, fl.size());
 		fl.add(pair[1]);
 		assertEquals(0, fl.size());
 		FragmentLengthSummary summary = fl.summary();
 		assertNotNull(summary);
 		assertNotNull(summary.getSampleFragmentLengths("s1"));
 		assertEquals(0, summary.getSampleFragmentLengths("s1").count());
 		assertNotNull(summary.getReadGroupFragmentLengths("rg1"));
 		assertEquals(0, summary.getReadGroupFragmentLengths("rg1").count());
 	}
 
 	@Test
 	public void addMappedPair() {
 		GATKSAMRecord[] pair = createMappedPair("ReadPair1", seq1Name, 10, 500);
		FragmentLengths fl = FragmentLengths.create(smIds, rgIds, 100000);
 		fl.add(pair[0]);
 		assertEquals(0, fl.size());
 		fl.add(pair[1]);
 		assertEquals(1, fl.size());
 		FragmentLengthSummary summary = fl.summary();
 		assertNotNull(summary);
 		assertNotNull(summary.getSampleFragmentLengths("s1"));
 		assertEquals(1, summary.getSampleFragmentLengths("s1").count());
 		assertNotNull(summary.getReadGroupFragmentLengths("rg1"));
 		assertEquals(1, summary.getReadGroupFragmentLengths("rg1").count());
 		assertEquals(500 - 10 + 1, summary.getReadGroupFragmentLengths("rg1")
 				.maximum());
 		assertEquals(500 - 10 + 1 - 2 * readLength, summary
 				.getReadGroupInsertLengths("rg1").maximum());
 		assertEquals(summary.getReadGroupFragmentLengths("rg1").maximum(),
 				summary.getSampleFragmentLengths("s1").maximum());
 		assertEquals(summary.getReadGroupInsertLengths("rg1").maximum(),
 				summary.getSampleInsertLengths("s1").maximum());
 		assertEquals(summary.getReadGroupFragmentLengths("rg1").minimum(),
 				summary.getSampleFragmentLengths("s1").maximum());
 		assertEquals(summary.getReadGroupInsertLengths("rg1").minimum(),
 				summary.getSampleInsertLengths("s1").maximum());
 	}
 
 	@Test
 	public void addInvertedMappedPair() {
 		GATKSAMRecord[] pair = createInvertedPair("ReadPair1", seq1Name, 10,
 				500);
		FragmentLengths fl = FragmentLengths.create(smIds, rgIds, 100000);
 		fl.add(pair[0]);
 		assertEquals(0, fl.size());
 		fl.add(pair[1]);
 		assertEquals(1, fl.size());
 		FragmentLengthSummary summary = fl.summary();
 		assertNotNull(summary);
 		assertNotNull(summary.getSampleFragmentLengths("s1"));
 		assertEquals(1, summary.getSampleFragmentLengths("s1").count());
 		assertNotNull(summary.getReadGroupFragmentLengths("rg1"));
 		assertEquals(1, summary.getReadGroupFragmentLengths("rg1").count());
 		assertEquals(500 - 10 + 1, summary.getReadGroupFragmentLengths("rg1")
 				.maximum());
 		assertEquals(500 - 10 + 1 - 2 * readLength, summary
 				.getReadGroupInsertLengths("rg1").maximum());
 		assertEquals(summary.getReadGroupFragmentLengths("rg1").maximum(),
 				summary.getSampleFragmentLengths("s1").maximum());
 		assertEquals(summary.getReadGroupInsertLengths("rg1").maximum(),
 				summary.getSampleInsertLengths("s1").maximum());
 		assertEquals(summary.getReadGroupFragmentLengths("rg1").minimum(),
 				summary.getSampleFragmentLengths("s1").maximum());
 		assertEquals(summary.getReadGroupInsertLengths("rg1").minimum(),
 				summary.getSampleInsertLengths("s1").maximum());
 	}
 
 	@Test
 	public void addBeyondMaxLengthPair() {
 		GATKSAMRecord[] pair = createMappedPair("ReadPair1", seq1Name, 10, 500);
		FragmentLengths fl = FragmentLengths.create(smIds, rgIds, 100);
 		fl.add(pair[0]);
 		assertEquals(0, fl.size());
 		fl.add(pair[1]);
 		assertEquals(0, fl.size());
 		FragmentLengthSummary summary = fl.summary();
 		assertNotNull(summary);
 		assertNotNull(summary.getSampleFragmentLengths("s1"));
 		assertEquals(0, summary.getSampleFragmentLengths("s1").count());
 		assertNotNull(summary.getReadGroupFragmentLengths("rg1"));
 		assertEquals(0, summary.getReadGroupFragmentLengths("rg1").count());
 	}
 
 	@Test
 	public void addHalfInvertedMappedPair() {
 		GATKSAMRecord[] pair = createHalfInvertedPair("ReadPair1", seq1Name,
 				10, 500);
		FragmentLengths fl = FragmentLengths.create(smIds, rgIds, 100000);
 		fl.add(pair[0]);
 		assertEquals(0, fl.size());
 		fl.add(pair[1]);
 		assertEquals(0, fl.size());
 		FragmentLengthSummary summary = fl.summary();
 		assertNotNull(summary);
 		assertNotNull(summary.getSampleFragmentLengths("s1"));
 		assertEquals(0, summary.getSampleFragmentLengths("s1").count());
 		assertNotNull(summary.getReadGroupFragmentLengths("rg1"));
 		assertEquals(0, summary.getReadGroupFragmentLengths("rg1").count());
 	}
 
 	@Test
 	public void addUnmappedPair() {
 		GATKSAMRecord[] pair = createUnmappedPair("ReadPair1", seq1Name, 10,
 				500, false, false);
		FragmentLengths fl = FragmentLengths.create(smIds, rgIds, 100000);
 		fl.add(pair[0]);
 		assertEquals(0, fl.size());
 		fl.add(pair[1]);
 		assertEquals(0, fl.size());
 		FragmentLengthSummary summary = fl.summary();
 		assertNotNull(summary);
 		assertNotNull(summary.getSampleFragmentLengths("s1"));
 		assertEquals(0, summary.getSampleFragmentLengths("s1").count());
 		assertNotNull(summary.getReadGroupFragmentLengths("rg1"));
 		assertEquals(0, summary.getReadGroupFragmentLengths("rg1").count());
 
 		pair = createUnmappedPair("ReadPair2", seq1Name, 10, 500, false, true);
 		fl.add(pair[0]);
 		assertEquals(0, fl.size());
 		fl.add(pair[1]);
 		assertEquals(0, fl.size());
 		summary = fl.summary();
 		assertNotNull(summary);
 		assertNotNull(summary.getSampleFragmentLengths("s1"));
 		assertEquals(0, summary.getSampleFragmentLengths("s1").count());
 		assertNotNull(summary.getReadGroupFragmentLengths("rg1"));
 		assertEquals(0, summary.getReadGroupFragmentLengths("rg1").count());
 
 		pair = createUnmappedPair("ReadPair3", seq1Name, 10, 500, true, false);
 		fl.add(pair[0]);
 		assertEquals(0, fl.size());
 		fl.add(pair[1]);
 		assertEquals(0, fl.size());
 		summary = fl.summary();
 		assertNotNull(summary);
 		assertNotNull(summary.getSampleFragmentLengths("s1"));
 		assertEquals(0, summary.getSampleFragmentLengths("s1").count());
 		assertNotNull(summary.getReadGroupFragmentLengths("rg1"));
 		assertEquals(0, summary.getReadGroupFragmentLengths("rg1").count());
 	}
 
 	@Test
 	public void addRandomPairs() {
 
 		GaussianRandomGenerator g = new GaussianRandomGenerator(
 				new MersenneTwister());
		FragmentLengths fl = FragmentLengths.create(smIds, rgIds, 100000);
 		int count = 1000000;
 		for (int i = 0; i < count; i++) {
 			int delta = (int) Math.round(g.nextNormalizedDouble() * 10);
 			int end = 1500 + delta;
 			// System.err.println("end " + end);
 			GATKSAMRecord[] pair = createMappedPair("ReadPair1", seq1Name,
 					1000, end);
 			fl = FragmentLengths.add(pair[0], fl);
 			fl = FragmentLengths.add(pair[1], fl);
 
 		}
 		assertEquals(count, fl.size());
 		FragmentLengthSummary summary = fl.summary();
 		assertNotNull(summary);
 		assertNotNull(summary.getSampleFragmentLengths("s1"));
 		assertEquals(count, summary.getSampleFragmentLengths("s1").count());
 		assertNotNull(summary.getReadGroupFragmentLengths("rg1"));
 		assertEquals(count, summary.getReadGroupFragmentLengths("rg1").count());
 		assertEquals(501, (int) Math.round(summary.getReadGroupFragmentLengths(
 				"rg1").mean()));
 		assertEquals(501, (int) Math.round(summary.getReadGroupFragmentLengths(
 				"rg1").median()));
 	}
 
 	@Test
 	public void saveRandomPairSummary() throws IOException {
 
 		GaussianRandomGenerator g = new GaussianRandomGenerator(
 				new MersenneTwister());
		FragmentLengths fl = FragmentLengths.create(smIds, rgIds, 100000);
 		int count = 1000000;
 		for (int i = 0; i < count; i++) {
 			int delta = (int) Math.round(g.nextNormalizedDouble() * 10);
 			int end = 1500 + delta;
 			// System.err.println("end " + end);
 			GATKSAMRecord[] pair = createMappedPair("ReadPair1", seq1Name,
 					1000, end);
 			fl = FragmentLengths.add(pair[0], fl);
 			fl = FragmentLengths.add(pair[1], fl);
 
 		}
 		assertEquals(count, fl.size());
 		FragmentLengthSummary summary = fl.summary();
 		assertNotNull(summary);
 		File f = File.createTempFile("flttest", ".tmp");
 		f.delete();
 		f.mkdir();
 		summary.saveIn(f);
 		FragmentLengthSummary summary2 = FragmentLengthSummary.loadFrom(f);
 		assertNotNull(summary2);
 		System.err.println("Output f " + f);
 		
 	}
 	
 	@Test
 	public void actualBamRun() throws IOException {
 		File testBam = new File(this.getClass().getClassLoader().getResource("3D7.bam").getFile());
 		File refFasta = new File(this.getClass().getClassLoader().getResource("3D7_pm.fa").getFile());
 		File outDir = File.createTempFile("flwtest", ".out");
 		outDir.delete();
 		outDir.mkdir();
		WalkerTest.executeTest("FragmentLength", String.format("-T FragmentLength -R %s -I %s -o %s",refFasta,testBam,outDir), null);
 		assertTrue(new File(outDir,FragmentLengthSummary.SUMMARY_FILE_NAME).exists());
 		assertTrue(new File(outDir,FragmentLengthSummary.HISTO_FILE_NAME).exists());
 		assertTrue(new File(outDir,FragmentLengthSummary.SERIAL_FILE_NAME).exists());
 		FragmentLengthSummary s = FragmentLengthSummary.loadFrom(outDir);
 		assertNotNull(s);
 		IntegerDistribution dist = s.getReadGroupFragmentLengths("Pf3d7.test.1");
 		assertNotNull(dist);
 		assertEquals(dist.mean(),500,0.001);
 		assertEquals(dist.median(),500,0.001);
 	}
 
 	
 	public GATKSAMRecord[] createMappedPair(String name, String seq, int start,
 			int end, String firstCigar, String secondCigar) {
 		List<GATKSAMRecord> list = ArtificialSAMUtils.createPair(sh, name,
 				readLength, start, end - readLength + 1, true, false);
 		for (GATKSAMRecord r : list)
 			r.setReadGroup(rgs.get(0));
 		list.get(0).setCigarString(firstCigar);
 		list.get(1).setCigarString(secondCigar);
 		return new GATKSAMRecord[] { list.get(0), list.get(1) };
 	}
 
 	public GATKSAMRecord[] createMappedPair(String name, String seq, int start,
 			int end) {
 		List<GATKSAMRecord> list = ArtificialSAMUtils.createPair(sh, name,
 				readLength, start, end - readLength + 1, true, false);
 		for (GATKSAMRecord r : list)
 			r.setReadGroup(rgs.get(0));
 		return new GATKSAMRecord[] { list.get(0), list.get(1) };
 	}
 
 	public GATKSAMRecord[] createUnmappedPair(String name, String seq,
 			int start, int end, boolean firstMapped, boolean secondMapped) {
 		if (firstMapped && secondMapped)
 			throw new IllegalArgumentException("both reads cannot be mapped.");
 		List<GATKSAMRecord> list = ArtificialSAMUtils.createPair(sh, name,
 				readLength, start, end - readLength + 1, true, false);
 		for (GATKSAMRecord r : list)
 			r.setReadGroup(rgs.get(0));
 		list.get(0).setReadUnmappedFlag(!firstMapped);
 		list.get(0).setMateUnmappedFlag(!secondMapped);
 		list.get(1).setReadUnmappedFlag(!secondMapped);
 		list.get(1).setMateUnmappedFlag(!firstMapped);
 		return new GATKSAMRecord[] { list.get(0), list.get(1) };
 	}
 
 	public GATKSAMRecord[] createInvertedPair(String name, String seq,
 			int start, int end) {
 		List<GATKSAMRecord> list = ArtificialSAMUtils.createPair(sh, name,
 				readLength, start, end - readLength + 1, false, false);
 		for (GATKSAMRecord r : list)
 			r.setReadGroup(rgs.get(0));
 		return new GATKSAMRecord[] { list.get(0), list.get(1) };
 	}
 
 	public GATKSAMRecord[] createHalfInvertedPair(String name, String seq,
 			int start, int end) {
 		List<GATKSAMRecord> list = ArtificialSAMUtils.createPair(sh, name,
 				readLength, start, end - readLength + 1, false, true);
 		for (GATKSAMRecord r : list)
 			r.setReadGroup(rgs.get(0));
 		return new GATKSAMRecord[] { list.get(0), list.get(1) };
 	}
 
 }
