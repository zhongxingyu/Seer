 package algo;
 import io.GbkReader;
 import io.SolutionWriter;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.apache.commons.lang.time.StopWatch;
 
 import model.CGene;
 import model.Constraints;
 import model.FamilyCriteria;
 import model.GlobalContext;
 import model.Helix;
 import utils.Utils;
 
 public class DynamicSolver {
 	public static class State {
 		public Helix h;
 		public int answer;
 		public int cumulativeAnswer;
 		public List<State> inner;
 		// last included item
 		public State best;
 		// if included, previous one
 		public State prev;
 		public State(Helix h) {
 			super();
 			this.h = h;
 			answer = -1;
 		}
 		public double getScore() {
 			return answer*2.0/h.getSpan();
 		}
 		@Override
 		public String toString() {
 			return "Score "+getScore()+" span "+h.getSpan()+" top "+h;
 		}
 		public void collectHelixes(List<Helix> helixes) {
 			helixes.add(h);
 			for (State s: inner) {
 				s.collectHelixes(helixes);
 			}
 		}
 		public void writeDebug(String prefix) {
 			Collections.sort(inner,new Comparator<State>() {
 				@Override
 				public int compare(State x, State y) {
 					return x.h.getRight() < y.h.getRight() ? -1 : 1;
 				}
 			});
 			System.out.println(prefix+h+" [");
 			for (State state : inner) {
 				state.writeDebug(prefix+"  ");
 			}
 			System.out.println(prefix+" ]");
 		}
 	}
 	
 	public State[] a;
 
 	public DynamicSolver(List<Helix> h) {
 		super();
 		a = new State[h.size()];
 		for (int i = 0; i < h.size(); ++i)
 			a[i] = new State(h.get(i));
 	}
 	
 	public List<CGene> solve() {
 		Arrays.sort(a,new Comparator<State>() {
 			@Override
 			public int compare(State x, State y) {
 				return x.h.getRight() - y.h.getRight();
 			}
 		});
 		State dummy = new State(new Helix(-1,-1,0));
 		dummy.cumulativeAnswer = 0;
 		List<CGene> res = new ArrayList<CGene>();
 		for (int i = 0; i < a.length; i++) {
 			ArrayList<State> inside = new ArrayList<State>();
 			inside.add(dummy);
 			for (int j = 0; j < i; j++) {
 				if (!(a[i].h.start < a[j].h.getLeft() && a[j].h.getRight() < a[i].h.end)
						|| a[j].answer == 0)
 					continue;
 				int p = inside.size()-1;
 				while (inside.get(p).h.getRight() >= a[j].h.getLeft())
 					--p;
 				a[j].cumulativeAnswer = inside.get(p).cumulativeAnswer + a[j].answer;
 				if (inside.get(inside.size()-1).cumulativeAnswer > a[j].cumulativeAnswer) {
 					a[j].cumulativeAnswer = inside.get(inside.size()-1).cumulativeAnswer;
 					a[j].best = inside.get(inside.size()-1).best;
 				} else {
 					a[j].prev = inside.get(p).best;
 					a[j].best = a[j];
 				}
 				inside.add(a[j]);
 			}
 			if (true) {
 				a[i].inner = new ArrayList<DynamicSolver.State>();
 				for (State s = inside.get(inside.size()-1).best; s != null; s = s.prev) {
 					a[i].inner.add(s);
 				}
 			}
 			a[i].answer = inside.get(inside.size()-1).cumulativeAnswer;
 			// there is something inside or we have a valid hairpin
 			if (a[i].answer > 0 || a[i].h.isHairpin())
 				a[i].answer += a[i].h.len;
 			if (a[i].h.getSpan() >= Constraints.GENE_MIN_LEN && a[i].getScore() >= Constraints.PAIRED_MIN_RATIO)
 				if (!a[i].h.isFAT()) {
 					List<Helix> helixes = new ArrayList<Helix>();
 					a[i].collectHelixes(helixes);
 					res.add(new CGene(helixes));
 				}
 		}
 		return res;
 	}
 
 	public static void main(String[] args) throws IOException {
 		StopWatch tm = new StopWatch();
 		tm.start();
 		//String gbk = GbkReader.read("c:\\yandex\\Tests\\gbk_for_students\\ref_chr7_00.gbk");
 		String gbk = GbkReader.read("../ref_chr7_00.gbk");
 		//gbk = gbk.substring(0, 100000);
 		GlobalContext.init(gbk);
 		int total = 0;
 		List<CGene> all = new ArrayList<CGene>();
 		for (int i = 0; i < gbk.length(); i+=200) {
 			//if (i%10000 == 0) System.err.println(i);
 			int[] codes = Utils.toInt(gbk.substring(i,Math.min(i+400,gbk.length())));
 			List<Helix> helixes = HelixFinder.findHelixesUnefficient(codes, 4, 4, 1000000);
 			Helix.addShift(helixes, i);
 			//System.out.println(helixes.size());
 			DynamicSolver solver = new DynamicSolver(helixes);
 			List<CGene> res = solver.solve();
 			total += res.size();
 			all.addAll(res);
 		}
 		Collections.sort(all, new Comparator<CGene>() {
 			@Override
 			public int compare(CGene arg0, CGene arg1) {
 				//return arg0.minPosition - arg1.minPosition; 
 				return Double.compare(arg1.getScore(), arg0.getScore());
 			}
 		});
 		System.out.println(total);
 		for (int i = 0; i < all.size() && i < 100; ++i)
 			System.out.println(all.get(i));
 		System.out.println("Elapsed "+tm);
 		tm.reset();
 		tm.start();
 
 		System.out.println("Candidates " + all.size());
 		List<CGene> filtered = GeneSelection.selectOptimal(all, new FamilyCriteria.Weight());
 		System.out.println("After "+filtered.size());
 		SolutionWriter.write(filtered, "../solution1.txt");
 		
 		System.out.println("Elapsed "+tm);
 	}
 }
