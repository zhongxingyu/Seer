 // Released under GPLv2 or later. See http://www.gnu.org/ for details.
 package tags.proto;
 
 import junit.framework.TestCase;
 
 import tags.proto.cont.*;
 import tags.proto.name.*;
 import tags.proto.route.*;
 import tags.store.*;
 import tags.util.*;
 import tags.util.exec.*;
 import tags.util.Maps.U2Map;
 
 import java.util.concurrent.*;
 import java.util.*;
 import java.io.IOException;
 
 public class MainTest extends TestCase {
 
	protected boolean report = true;
 
 	public static class SimpleQP<I, T, S> extends QueryProcessor<I, T, I, S, S, S, S> {
 		public SimpleQP(
 			Query<I, T> query,
 			StoreControl<I, T, I, S, S, S, S> sctl,
 			PTableComposer<I, I, S, S> mod_ptb_cmp,
 			TGraphComposer<T, I, S, S, S> mod_tgr_cmp,
 			AddressSchemeBuilder<T, I, S, S> mod_asc_bld,
 			LocalViewFactory<I, LocalTGraph<T, I, S, S>> view_fac_g,
 			ScoreInferer<S> score_inf_g,
 			IndexComposer<T, I, S, S> mod_idx_cmp,
 			LookupScorer<S, S> mod_lku_scr,
 			LocalViewFactory<I, LocalIndex<T, I, S>> view_fac_h,
 			ScoreInferer<S> score_inf_h,
 			Executor exec
 		) {
 			super(query, sctl, mod_ptb_cmp, mod_tgr_cmp, mod_asc_bld, view_fac_g, score_inf_g, mod_idx_cmp, mod_lku_scr, view_fac_h, score_inf_h, exec);
 		};
 	}
 
 	public static class DefaultQP extends SimpleQP<Long, String, Probability> {
 		public DefaultQP(
 			Query<Long, String> query,
 			StoreControl<Long, String, Long, Probability, Probability, Probability, Probability> sctl,
 			PTableComposer<Long, Long, Probability, Probability> mod_ptb_cmp,
 			TGraphComposer<String, Long, Probability, Probability, Probability> mod_tgr_cmp,
 			AddressSchemeBuilder<String, Long, Probability, Probability> mod_asc_bld,
 			LocalViewFactory<Long, LocalTGraph<String, Long, Probability, Probability>> view_fac_g,
 			ScoreInferer<Probability> score_inf_g,
 			IndexComposer<String, Long, Probability, Probability> mod_idx_cmp,
 			LookupScorer<Probability, Probability> mod_lku_scr,
 			LocalViewFactory<Long, LocalIndex<String, Long, Probability>> view_fac_h,
 			ScoreInferer<Probability> score_inf_h,
 			Executor exec
 		) {
 			super(query, sctl, mod_ptb_cmp, mod_tgr_cmp, mod_asc_bld, view_fac_g, score_inf_g, mod_idx_cmp, mod_lku_scr, view_fac_h, score_inf_h, exec);
 		};
 	}
 
 	public void testProbabilityQueryProcessor() {
 		Executor exec = new ThreadPoolExecutor(
 			0x40, 0x40, 1, TimeUnit.SECONDS,
 			new LinkedBlockingQueue<Runnable>(),
 			new ThreadPoolExecutor.CallerRunsPolicy()
 		);
 		FileStoreControl<Long, String, Long, Probability, Probability, Probability, Probability> sctl =
 		new FileStoreControl<Long, String, Long, Probability, Probability, Probability, Probability>(".");
 		StoreGenerator.sctl_gen_all(sctl);
 
 		System.out.println("Test data initialised with " + sctl.getSummary());
 
		for (long id: new long[]{8028L, 8032L, 8036L, 8040L, 8044L}) {
		//for (long id: new long[]{8044L}) {
 
 			Query<Long, String> query = new Query<Long, String>(id, "aacs");
 
 			DefaultQP proc = new DefaultQP(query, sctl,
 				new ProtoPTableComposer<Long, Long>(),
 				new ProbabilityEntropyTGraphComposer<String, Long>(),
 				new ShortestPathAddressSchemeBuilder<String, Long, Probability, Probability, Probability>(new ProbabilityDistanceMetric()),
 				LocalTGraph.<String, Long, Probability, Probability>getFactory(),
 				new SPUProbabilityInferer(),
 				new ProbabilityIndexComposer<String, Long>(),
 				new ProbabilityLookupScorer(),
 				LocalIndex.<String, Long, Probability>getFactory(),
 				new SPUProbabilityInferer(),
 				exec
 			);
 
 			System.out.println("Starting query " + query);
 
 			// get some results
 
 			while (proc.getResults() == null || proc.getResults().isEmpty()) {
 				nextStep(proc, report);
 			}
 			showResults(query, proc.getResults());
 
 			U2Map<Long, Long, Probability> res = proc.getResults();
 			for (int i=0; i<16; ++i) {
 				nextStep(proc, false);
 				if (proc.getResults() == res) { continue; }
 				System.out.println("Got " + res.K0Map().size() + "," + res.K1Map().size() + " results for " + query);
 				if (report) { System.out.println(showResults(query, proc.getResults())); }
 				res = proc.getResults();
 			}
 
 			// test whether the results actually match
 
 			int d = sctl.map_tag.get(query.tag).size();
 			int r = proc.getResults().K0Map().size();
 
 			Set<Long> doc = new HashSet<Long>(sctl.map_tag.get(query.tag));
 			doc.retainAll(proc.getResults().K0Map().keySet());
 			int x = doc.size();
 
 			System.out.println(r + " results, " + d + " real; intersection is " + x);
 
 			// fail if less than half the results actually match
 			assertTrue(x<<1 > r);
 
 		}
 
 	}
 
 	public String showResults(Query<Long, String> query, U2Map<Long, Long, Probability> res) {
 		return "doc: " + outputMap(res.K0Map()) + "\nidx: " + outputMap(res.K1Map());
 	}
 
 	public String outputMap(Map<Long, Probability> map) {
 		StringBuilder s = new StringBuilder();
 		s.append("{ ");
 		for (Map.Entry<Long, Probability> en: map.entrySet()) {
 			s.append("(").append(en.getKey()).append(':').append(en.getValue().toString().substring(0, 6)).append(") ");
 		}
 		s.append('}');
 		return s.toString();
 	}
 
 	public void nextStep(DefaultQP proc, boolean stat) {
 		try {
 			proc.getMoreData();
 		} catch (MessageRejectedException e) {
 			if (stat && !e.getMessage().equals("bad timing")) { System.out.println(e); }
 		}
 		if (stat) {
 			System.out.println("[ " + proc.contact.getStatus() + " | " + proc.naming.getStatus() + " | " + proc.routing.getStatus() + " ]");
 		}
 		try { Thread.sleep(250); } catch (InterruptedException e) { }
 	}
 
 }
