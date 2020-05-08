 package local.radioschedulers.run;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import local.radioschedulers.IScheduler;
 import local.radioschedulers.Proposal;
 import local.radioschedulers.Schedule;
 import local.radioschedulers.ScheduleSpace;
 import local.radioschedulers.alg.ga.GeneticAlgorithmScheduler;
 import local.radioschedulers.alg.ga.HeuristicsScheduleCollector;
 import local.radioschedulers.alg.ga.ScheduleFitnessFunction;
 import local.radioschedulers.alg.ga.fitness.NormalizedScheduleFitnessFunction;
 import local.radioschedulers.alg.ga.fitness.SimpleScheduleFitnessFunction;
 import local.radioschedulers.alg.ga.watchmaker.SortedCollection;
 import local.radioschedulers.alg.ga.watchmaker.WFScheduler;
 import local.radioschedulers.alg.ga.watchmaker.SortedCollection.MappingFunction;
 import local.radioschedulers.exporter.CsvExport;
 import local.radioschedulers.exporter.HtmlExportWithFitness;
 import local.radioschedulers.exporter.IExport;
 import local.radioschedulers.importer.AtcaProposalReader;
 import local.radioschedulers.importer.CsvScheduleReader;
 import local.radioschedulers.importer.IProposalReader;
 import local.radioschedulers.importer.JsonProposalReader;
 import local.radioschedulers.preschedule.ITimelineGenerator;
 import local.radioschedulers.preschedule.SimpleTimelineGenerator;
 import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;
 
 import org.uncommons.watchmaker.framework.EvolutionObserver;
 import org.uncommons.watchmaker.framework.PopulationData;
 
 public class RunGA {
 	private static int ndays = 365 / 2;
 	private static double bestFitness;
 	private static double goodFitnessLimit;
 	private static final File outputDir = new File(".");
 	private static final boolean WRITE_PROPOSALS = true;
 	private static final boolean READ_SCHEDULES = true;
 	private static final boolean WRITE_SCHEDULES = true;
 
 	public static void main(String[] args) throws Exception {
 		IProposalReader pr = getProposalReader();
 		Collection<Proposal> proposals = pr.readall();
 		for (Proposal p : proposals) {
 			System.out.println(p.toString());
 		}
 		if (WRITE_PROPOSALS) {
 			JsonProposalReader json = new JsonProposalReader(new File(
 					"proposals_testset_mopra.json"));
 			json.write(proposals);
 		}
 
 		ITimelineGenerator tlg = new SimpleTimelineGenerator(
 				new ParallelRequirementGuard());
 		ScheduleSpace template = tlg.schedule(proposals, ndays);
 
 		CsvScheduleReader csv = getScheduleReader(proposals);
 		if (WRITE_SCHEDULES) {
 			csv.write(template);
 		}
 
 		Map<String, Schedule> schedules = null;
 		if (READ_SCHEDULES) {
 			try {
 				schedules = csv.readall();
 			} catch (IOException e) {
 			} catch (NullPointerException e) {
 			}
 		}
 		if (schedules == null || schedules.isEmpty()) {
 			Map<IScheduler, Schedule> schedules2 = HeuristicsScheduleCollector
 					.getStartSchedules(template);
 
 			schedules = new HashMap<String, Schedule>();
 			for (Entry<IScheduler, Schedule> e : schedules2.entrySet()) {
 				schedules.put(e.getKey().toString(), e.getValue());
 			}
 			if (WRITE_SCHEDULES) {
 				csv.write(schedules);
 			}
 		}
 
 		final ScheduleFitnessFunction fitness = n(getFitnessFunction(),
 				proposals, template);
 		ScheduleFitnessFunction observatoryFitness = n(
 				getObservatoryFitnessFunction(), proposals, template);
 		ScheduleFitnessFunction observerFitness = n(
 				getObserverFitnessFunction(), proposals, template);
 
 		GeneticAlgorithmScheduler scheduler = getScheduler(fitness);
 		bestFitness = calculateBestFitness(schedules, fitness);
 		goodFitnessLimit = bestFitness * 0.95;
 		scheduler.setPopulation(getInitialPopulationTopPercent(schedules,
 				fitness));
 
 		PrintWriter oq = new PrintWriter("quality.txt");
 		int i = 1;
 		File index = new File(outputDir, "index.html");
 		PrintWriter o = new PrintWriter(index);
 		o.println("<h2>Initial results</h2>");
 		o.println("<ul>");
 		Iterable<Entry<String, Schedule>> sortedSchedules = new SortedCollection<Entry<String, Schedule>>(
 				schedules.entrySet(),
 				new MappingFunction<Entry<String, Schedule>, Comparable<Double>>() {
 
 					@Override
 					public Comparable<Double> map(Entry<String, Schedule> item) {
 						return -fitness.evaluate(item.getValue());
 					}
 				});
 		for (Entry<String, Schedule> e : sortedSchedules) {
 			Schedule s = e.getValue();
 			String name = e.getKey().toString();
 			File f = export(name, "schedule_" + i, s, fitness);
 			double fv = fitness.evaluate(s);
 			o.println("\t<li><a href='"
 					+ f.getName()
 					+ "'"
 					+ (fv >= goodFitnessLimit ? " style='font-weight:bold'"
 							: "") + ">" + formatFitness(fv) + " -- " + name
 					+ "</a></li>");
 			oq.println(fv + "\t" + observatoryFitness.evaluate(s) + "\t"
 					+ observerFitness.evaluate(s) + "\t" + name);
 			System.out.println("Schedule of " + name + " in " + f);
 			i++;
 		}
 		o.println("</ul>");
 		o.flush();
 		oq.flush();
 
 		scheduler.schedule(template);
 
 		o.println("<h2>GA survivors</h2>");
		o.println("<strong><a href='schedule_" + i
				+ ".html'>&quot;Best&quot; schedule<a></strong>");

		o.println("<h3>GA survivors</h3>");
 		o.println("<ul>");
 		int j = 1;
 		for (Schedule s : scheduler.getPopulation()) {
 			String name = "GA survivor " + j;
 			File f = export(name, "schedule_" + i, s, fitness);
 			double fv = fitness.evaluate(s);
 			o.println("\t<li><a href='"
 					+ f.getName()
 					+ "'"
 					+ (fv >= goodFitnessLimit ? " style='font-weight:bold'"
 							: "") + ">" + formatFitness(fv) + " -- " + name
 					+ "</a></li>");
 			oq.println(fv + "\t" + observatoryFitness.evaluate(s) + "\t"
 					+ observerFitness.evaluate(s) + "\t" + name);
 			i++;
 			j++;
 		}
 		o.println("</ul>");
 		o.close();
 		oq.close();
 		System.out.println("Index of results at " + index);
 	}
 
 	private static ScheduleFitnessFunction n(
 			ScheduleFitnessFunction fitnessFunction,
 			Collection<Proposal> proposals, ScheduleSpace space) {
 		NormalizedScheduleFitnessFunction normfit = new NormalizedScheduleFitnessFunction(
 				fitnessFunction);
 		normfit.setupNormalization(space, proposals);
 		return normfit;
 	}
 
 	private static double calculateBestFitness(Map<String, Schedule> schedules,
 			final ScheduleFitnessFunction fitness) {
 		SortedCollection<Schedule> sortedPopulation = new SortedCollection<Schedule>(
 				schedules.values(),
 				new MappingFunction<Schedule, Comparable<Double>>() {
 
 					@Override
 					public Comparable<Double> map(Schedule item) {
 						return -fitness.evaluate(item);
 					}
 				});
 		return fitness.evaluate(sortedPopulation.first());
 	}
 
 	@SuppressWarnings("unused")
 	private static ArrayList<Schedule> getInitialPopulationTopN(
 			Map<String, Schedule> schedules,
 			final ScheduleFitnessFunction fitness) {
 		SortedCollection<Schedule> sortedPopulation = new SortedCollection<Schedule>(
 				schedules.values(),
 				new MappingFunction<Schedule, Comparable<Double>>() {
 
 					@Override
 					public Comparable<Double> map(Schedule item) {
 						return -fitness.evaluate(item);
 					}
 				});
 		ArrayList<Schedule> initialPopulation = new ArrayList<Schedule>();
 		Iterator<Schedule> it = sortedPopulation.iterator();
 		Schedule s, s2;
 
 		s = it.next();
 		bestFitness = fitness.evaluate(s);
 		initialPopulation.add(s);
 		while (initialPopulation.size() < 3) {
 			s2 = it.next();
 			if (!s2.equals(s)) {
 				initialPopulation.add(s);
 			} else {
 				System.out.println("skipping duplicate solution");
 			}
 			s = s2;
 		}
 		return initialPopulation;
 	}
 
 	private static String formatFitness(double v) {
 		return Double.toString(Math.round(v * 10000) / 100.) + "%";
 	}
 
 	private static ArrayList<Schedule> getInitialPopulationTopPercent(
 			Map<String, Schedule> schedules,
 			final ScheduleFitnessFunction fitness) {
 		ArrayList<Schedule> initialPopulation = new ArrayList<Schedule>();
 		for (Entry<String, Schedule> e : schedules.entrySet()) {
 			double fv = fitness.evaluate(e.getValue());
 			if (fv > goodFitnessLimit)
 				initialPopulation.add(e.getValue());
 		}
 		return initialPopulation;
 	}
 
 	private static CsvScheduleReader getScheduleReader(
 			Collection<Proposal> proposals) {
 		CsvScheduleReader csv = new CsvScheduleReader(new File(
 				"schedules_mopra.csv"), new File("space_mopra.csv"), proposals);
 		return csv;
 	}
 
 	private static File export(String title, String prefix, Schedule s,
 			ScheduleFitnessFunction fitness) throws IOException {
 		File f = new File(outputDir, prefix + "__" + title + ".html");
 		IExport ex = new HtmlExportWithFitness(f, title, fitness);
 		ex.export(s);
 		ex = new CsvExport(new File(outputDir, prefix + ".csv"));
 		ex.export(s);
 		return f;
 	}
 
 	private static GeneticAlgorithmScheduler getScheduler(
 			ScheduleFitnessFunction f) {
 		WFScheduler scheduler = new WFScheduler(f);
 		scheduler.setPopulationSize(50);
 		scheduler.setNumberOfGenerations(2000 / scheduler.getPopulationSize());
 		scheduler.setEliteSize(2);
 
 		scheduler.setCrossoverProbability(0.02);
 		scheduler.setMutationProbability(0.);
 
 		scheduler.setDoubleCrossoverProbability(0.1);
 		scheduler.setCrossoverDays(1);
 
 		// scheduler.setMutationKeepingProbability(0.02);
 		// scheduler.setMutationSimilarForwardsProbability(0.03);
 		// scheduler.setMutationSimilarBackwardsProbability(0.02);
 		// scheduler.setMutationSimilarPrevProbability(0.03);
		scheduler.setMutationExchangeProbability(0.05);
		scheduler.setMutationJobPlacementProbability(0.02);
 
 		scheduler.setObserver(new EvolutionObserver<Schedule>() {
 
 			@Override
 			public void populationUpdate(PopulationData<? extends Schedule> data) {
 				System.out.println("Gen. " + data.getGenerationNumber()
 						+ ", pop.-qual. avg: " + data.getMeanFitness()
 						+ " best: " + data.getBestCandidateFitness()
 						+ " stdev: " + data.getFitnessStandardDeviation());
 			}
 		});
 
 		return scheduler;
 	}
 
 	private static ScheduleFitnessFunction getFitnessFunction() {
 		SimpleScheduleFitnessFunction f = new SimpleScheduleFitnessFunction();
 		f.setSwitchLostMinutes(15);
 		return f;
 	}
 
 	private static ScheduleFitnessFunction getObserverFitnessFunction() {
 		SimpleScheduleFitnessFunction f = new SimpleScheduleFitnessFunction();
 		f.setSwitchLostMinutes(60);
 		return f;
 	}
 
 	private static ScheduleFitnessFunction getObservatoryFitnessFunction() {
 		SimpleScheduleFitnessFunction f = new SimpleScheduleFitnessFunction();
 		f.setSwitchLostMinutes(0);
 		return f;
 	}
 
 	@SuppressWarnings("deprecation")
 	private static IProposalReader getProposalReader() throws Exception {
 		// SqliteProposalReader pr = new SqliteProposalReader();
 		// PopulationGeneratingProposalReader pr = new
 		// PopulationGeneratingProposalReader();
 		// pr.fill(ndays * 4);
 		AtcaProposalReader pr = new AtcaProposalReader(new File(
 				"/home/user/Downloads/ata-proposals/mp-johannes.txt"),
 				new Date(2011 - 1900, 1, 4), ndays);
 		return pr;
 	}
 
 }
