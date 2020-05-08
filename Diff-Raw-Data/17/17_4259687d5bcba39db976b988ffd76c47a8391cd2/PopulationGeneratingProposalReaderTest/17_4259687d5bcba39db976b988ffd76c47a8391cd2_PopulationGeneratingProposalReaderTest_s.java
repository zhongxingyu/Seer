 package local.radioschedulers.importer;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.LineNumberReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import junit.framework.Assert;
 import local.radioschedulers.Job;
 import local.radioschedulers.JobWithResources;
 import local.radioschedulers.Proposal;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class PopulationGeneratingProposalReaderTest {
 
	private static final String PROPOSALS_CSV = "target/test-classes/ata-proposals.csv";
	private static final String TMPDIR = "testoutput";
 	private PopulationGeneratingProposalReader reader;
 
 	@Before
 	public void setUp() throws Exception {
 		reader = new PopulationGeneratingProposalReader();
 		reader.fill(10000);
 	}
 
 	@Test
 	public void testConvergence() throws Exception {
 		Collection<Proposal> proposals = reader.readall();
 		Collection<Job> jobs = getallJobs(proposals);
 		Histogram<Integer> histhours = new Histogram<Integer>();
 		Histogram<Double> histdurations = new Histogram<Double>();
 		Histogram<Double> histdurationsw = new Histogram<Double>();
 		Histogram<Integer> histantennas = new Histogram<Integer>();
 		Histogram<Integer> histantennasw = new Histogram<Integer>();
 
 		calculateDurationBins(histdurations);
 		calculateDurationBins(histdurationsw);
 		calculateHourBins(histhours);
 		calculateAntennaBins(histantennas);
 		calculateAntennaBins(histantennasw);
 
 		// hourhist.addBin(lastborder, 10000);
 		for (Job j : jobs) {
 			Assert.assertNotNull(j.lstmax);
 			Assert.assertNotNull(j.lstmin);
 			Assert.assertTrue(j.lstmin >= 0);
 			Assert.assertTrue(j.lstmax >= 0);
 			Assert.assertTrue(j.lstmin < 24);
 			Assert.assertTrue(j.lstmax < 24);
 			Assert.assertNotNull(j.hours);
 			Assert.assertNotNull(j.proposal);
 			Assert.assertNotNull(j.proposal.jobs);
 			Assert.assertFalse(j.proposal.jobs.isEmpty());
 			Assert.assertNotNull(j.proposal.priority);
 			Assert.assertTrue(j.proposal.priority > 0);
 
 			histhours.addItem(j.hours.intValue());
 			if (j.lstmax == null || j.lstmin == null)
 				System.out.println(j);
 			double diff = j.lstmax - j.lstmin;
 			if (diff < 0)
 				diff = j.lstmax - j.lstmin + 24;
 			histdurations.addItem(diff);
 			histdurationsw.addItem(diff, j.hours * 1.);
 			JobWithResources jwr = (JobWithResources) j;
 			histantennas.addItem(jwr.resources.get("antennas").numberrequired);
 			histantennasw.addItem(jwr.resources.get("antennas").numberrequired,
 					j.hours * 1.);
 		}
 
 		System.out.println("hours:");
 		for (Bin<Integer> bin : histhours) {
 			int c = round(bin.getCount());
 			System.out.print(bin.getLow() + " .. " + bin.getHigh() + "  \t");
 			while (c >= 100) {
 				System.out.print("C");
 				c -= 100;
 			}
 			System.out.println(" | " + bin.getCount());
 		}
 		System.out.println("duration:");
 		for (Bin<Double> bin : histdurations) {
 			int c = round(bin.getCount());
 			System.out.print(bin.getLow() + " .. " + bin.getHigh() + "  \t");
 			while (c >= 100) {
 				System.out.print("C");
 				c -= 100;
 			}
 			System.out.print(" ");
 			while (c >= 10) {
 				System.out.print("X");
 				c -= 10;
 			}
 			System.out.print(" ");
 			while (c >= 1) {
 				System.out.print("i");
 				c -= 1;
 			}
 
 			System.out.println(" | " + bin.getCount());
 		}
 		histdurations.normalize();
 		histdurationsw.normalize();
 		histhours.normalize();
 		histantennas.normalize();
 		histantennasw.normalize();
 
 		dumpHistogram(histdurations, "histdurationsgen.txt");
 		dumpHistogram(histdurationsw, "histdurationswgen.txt");
 		dumpHistogramInt(histhours, "histhoursgen.txt");
 		dumpHistogramInt(histantennas, "histantennasgen.txt");
 		dumpHistogramInt(histantennasw, "histantennaswgen.txt");
 	}
 
 	private void dumpHistogram(Histogram<Double> durationhist, String filename)
 			throws IOException {
 		FileWriter fw2;
 		fw2 = new FileWriter(getOutputFile(filename));
 		for (Bin<Double> bin : durationhist) {
 			fw2.write((bin.getHigh() + bin.getLow()) / 2. + "\t"
 					+ bin.getCount() + "\t" + bin.getLow() + "\t"
 					+ bin.getHigh() + "\n");
 		}
 		fw2.close();
 	}
 
 	private File getOutputFile(String filename) {
		return new File(TMPDIR, filename);
 	}
 
 	private void dumpHistogramInt(Histogram<Integer> durationhist,
 			String filename) throws IOException {
 		FileWriter fw2;
 		fw2 = new FileWriter(getOutputFile(filename));
 		for (Bin<Integer> bin : durationhist) {
 			fw2.write((bin.getHigh() + bin.getLow()) / 2. + "\t"
 					+ bin.getCount() + "\t" + bin.getLow() + "\t"
 					+ bin.getHigh() + "\n");
 		}
 		fw2.close();
 	}
 
 	private void calculateDurationBins(Histogram<Double> durationhist) {
 		for (int i = 0; i + 2 <= 24; i += 2) {
 			durationhist.addBin(i * 1., i + 2.);
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private void calculateAntennaBins2(Histogram<Integer> histantennas) {
 		histantennas.addBin(0, 1);
 		histantennas.addBin(1, 2);
 		histantennas.addBin(2, 5);
 		histantennas.addBin(5, 8);
 		histantennas.addBin(8, 11);
 		histantennas.addBin(11, 17);
 		histantennas.addBin(17, 24);
 		histantennas.addBin(24, 30);
 		histantennas.addBin(30, 35);
 		histantennas.addBin(35, 40);
 		histantennas.addBin(40, 42);
 		histantennas.addBin(42, 45);
 	}
 
 	private void calculateAntennaBins(Histogram<Integer> histantennas) {
 		histantennas.addBin(0, 1);
 		histantennas.addBin(1, 2);
 		for (int i = 2; i <= 42; i += 2) {
 			histantennas.addBin(i, i + 2);
 		}
 	}
 
 	private void calculateHourBins(Histogram<Integer> hourhist) {
 		Integer lastborder = 0;
 		for (int i = 9; i >= 0; i--) {
 			Integer border = round(10000 * Math.pow(3, -i));
 			hourhist.addBin(lastborder, border);
 			System.out.println(lastborder + " .. " + border);
 			lastborder = border;
 		}
 	}
 
 	@Test
 	public void fetchDataHistogram() throws IOException {
		File f = new File(PROPOSALS_CSV);
 		// List<Double> start = new ArrayList<Double>();
 		// List<Double> end = new ArrayList<Double>();
 		List<Double> duration = new ArrayList<Double>();
 		List<Double> visibility = new ArrayList<Double>();
 		List<Integer> antennas = new ArrayList<Integer>();
 		List<Integer> hours = new ArrayList<Integer>();
 		LineNumberReader r = new LineNumberReader(new FileReader(f));
 		// skipping first line, header
 		String line = r.readLine();
 
 		while (true) {
 			line = r.readLine();
 			if (line == null)
 				break;
 			String[] parts = line.split(", ");
 			Double[] partsd = new Double[parts.length];
 			for (int i = 0; i < parts.length; i++) {
 				try {
 					partsd[i] = Double.parseDouble(parts[i]);
 				} catch (NumberFormatException e) {
 					partsd[i] = Double.NaN;
 				}
 			}
 
 			// start, end, duration, antennas, hours
 			duration.add(Math.abs(partsd[2]));
 			visibility.add(Math.abs(partsd[0] - partsd[1]));
 			antennas.add((int) Math.round(partsd[3]));
 			hours.add((int) Math.round(partsd[4]));
 		}
 		Histogram<Double> histdurations = new Histogram<Double>();
 		Histogram<Double> histdurationsw = new Histogram<Double>();
 		Histogram<Integer> histhours = new Histogram<Integer>();
 		Histogram<Integer> histantennas = new Histogram<Integer>();
 		Histogram<Integer> histantennasw = new Histogram<Integer>();
 		calculateDurationBins(histdurations);
 		calculateDurationBins(histdurationsw);
 		calculateHourBins(histhours);
 		calculateAntennaBins(histantennas);
 		calculateAntennaBins(histantennasw);
 
 		for (int i = 0; i < duration.size(); i++) {
 			if (Double.isNaN(duration.get(i))) {
 				Assert.assertTrue(Double.isNaN(visibility.get(i)));
 				continue;
 			}
 			Assert.assertEquals(duration.get(i), visibility.get(i), 0.001);
 			histdurations.addItem(duration.get(i));
 			histdurationsw.addItem(duration.get(i), hours.get(i) * 1.);
 			histhours.addItem(round(hours.get(i)));
 			histantennas.addItem(antennas.get(i));
 			histantennasw.addItem(antennas.get(i), hours.get(i) * 1.);
 		}
 		histdurations.normalize();
 		histdurationsw.normalize();
 		histhours.normalize();
 		histantennas.normalize();
 		histantennasw.normalize();
 		dumpHistogram(histdurations, "histdurations.txt");
 		dumpHistogram(histdurationsw, "histdurationsw.txt");
 		dumpHistogramInt(histhours, "histhours.txt");
 		dumpHistogramInt(histantennas, "histantennas.txt");
 		dumpHistogramInt(histantennasw, "histantennasw.txt");
 	}
 
 	private Integer round(double d) {
 		return Integer.valueOf((int) Math.round(d));
 	}
 
 	private Collection<Job> getallJobs(Collection<Proposal> proposals) {
 		Collection<Job> jobs = new ArrayList<Job>(proposals.size());
 		for (Proposal p : proposals) {
 			jobs.addAll(p.jobs);
 		}
 		return jobs;
 	}
 
 }
