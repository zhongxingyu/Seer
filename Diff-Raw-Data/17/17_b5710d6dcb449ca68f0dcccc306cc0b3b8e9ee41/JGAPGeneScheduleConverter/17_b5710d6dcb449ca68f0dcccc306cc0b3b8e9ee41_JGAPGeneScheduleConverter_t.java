 package local.radioschedulers.alg.ga.jgap;
 
 import java.util.Set;
 import java.util.Map.Entry;
 
 import local.radioschedulers.JobCombination;
 import local.radioschedulers.LSTTime;
 import local.radioschedulers.Schedule;
 import local.radioschedulers.ScheduleSpace;
 import local.radioschedulers.alg.ga.GeneticAlgorithmScheduler;
 
 import org.apache.log4j.Logger;
 import org.jgap.Chromosome;
 import org.jgap.Configuration;
 import org.jgap.Gene;
 import org.jgap.IChromosome;
 import org.jgap.InvalidConfigurationException;
 
 public class JGAPGeneScheduleConverter {
 	private static Logger log = Logger
 			.getLogger(JGAPGeneScheduleConverter.class);
 	private Configuration conf;
 
 	public JGAPGeneScheduleConverter(Configuration conf) {
 		this.conf = conf;
 	}
 
 	public static LSTTime getLSTTimeFromGeneId(int i) {
		return new LSTTime(i / ScheduleSpace.LST_SLOTS_PER_DAY,
				(i % ScheduleSpace.LST_SLOTS_PER_DAY)
						* ScheduleSpace.LST_SLOTS_MINUTES);
 	}
 
 	/**
 	 * Converts a Chromosome back to a Schedule
 	 * 
 	 * @param c
 	 * @return
 	 */
 	public static Schedule getScheduleFromChromosome(IChromosome c) {
 		int i;
 		Gene[] genes = c.getGenes();
 		Schedule s = new Schedule();
 
 		for (i = 0; i < genes.length; i++) {
 			JobCombination jc = (JobCombination) genes[i].getAllele();
 			LSTTime t = getLSTTimeFromGeneId(i);
 			s.add(t, jc);
 		}
 
 		return s;
 	}
 
 	/**
 	 * Build a Chromosome, where a Gene is a scheduling slot, and each possible
 	 * Job(Combination) is a Allele.
 	 * 
 	 * @param s
 	 *            Schedule to use for activating alleles (can be null)
 	 * @return the corresponding Chromosome
 	 * @throws InvalidConfigurationException
 	 */
 	public IChromosome getChromosomeFromSpecificSchedule(
 			ScheduleSpace possibles, Schedule s)
 			throws InvalidConfigurationException {
 		int ngenes = GeneticAlgorithmScheduler.calculateNGenes(possibles);
 		Gene[] genes = new Gene[ngenes];
 		int i = 0;
 		for (Entry<LSTTime, Set<JobCombination>> e : possibles) {
 			LSTTime t = e.getKey();
 			Set<JobCombination> jcs = e.getValue();
 
 			BetterSetGene<JobCombination> g = new BetterSetGene<JobCombination>(
 					conf);
 			// idle job encoded as null
 			g.addAllele(null);
 			g.setAllele(null);
 
 			// add all possible jobs, and select the one specified
 			for (JobCombination jc : jcs) {
 				if (jc.jobs == null)
 					throw new NullPointerException("no jobs in JobCombination "
 							+ jc);
 				g.addAllele(jc);
 
 				if (s != null && jc.equals(s.get(t)))
 					g.setAllele(jc);
 
 			}
 			if (i >= ngenes) {
 				log.error("ran out of genes at " + t);
 			}
 			genes[i] = g;
 			i++;
 		}
 		if (i != ngenes) {
 			throw new IllegalStateException(
 					"ScheduleSpace length doesn't match number of genes");
 		}
 
 		Chromosome c = new Chromosome(conf, genes);
 		return c;
 	}
 
 }
