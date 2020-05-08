 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import dfh.cli.Cli;
 import dfh.cli.rules.Range;
 import dfh.cli.rules.StrSet;
 import dfh.grammar.Grammar;
 import dfh.grammar.GrammarException;
 import dfh.grammar.Matcher;
 import dfh.grammar.Options;
 
 public class Benchmarks {
 
 	private static int warmup;
 	private static Integer trials;
 	private static Integer group;
 	private static double trim;
 	private static String cache;
 
 	/**
 	 * <pre>
 	 * USAGE: EXECUTABLE [options]
 	 * 
 	 *   A basic set of benchmark tests comparing grammars using various parameters to 
 	 *   equivalent regular expressions.
 	 * 
 	 *     --group -g   &lt;val&gt;  number or iterations to time together to overcome the
 	 *                         limitations of millisecond time granularity; default: 
 	 *                         2000
 	 *     --trials -t  &lt;val&gt;  number of groups to time; default: 50
 	 *     --trim       &lt;val&gt;  fraction of time-sorted trials to discard from the high
 	 *                         and low ends of the sort to eliminate outliers; default:
 	 *                         0.1
 	 *     --warmup -w  &lt;val&gt;  number to iterations to warm up the JIT compiler;
 	 *                         default: 50000
 	 *     --cache -c   &lt;val&gt;  match cache type; one of {tree,hash,array} default:
 	 *                         array
 	 * 
 	 *     --help -? -h        print usage information
 	 * </pre>
 	 * 
 	 * @param args
 	 * @throws IOException
 	 * @throws GrammarException
 	 */
 	public static void main(String[] args) throws GrammarException, IOException {
 		Object[][][] spec = {
 				{ {
 						Cli.Opt.USAGE,
 						"A basic set of benchmark tests comparing grammars using various parameters to equivalent regular expressions." } },//
 				{ { Cli.Opt.ARGS } },//
 				{
 						{ "group", 'g', Integer.class, 2000 },
 						{ "number or iterations to time together to overcome the limitations of millisecond time granularity" },
 						{ Range.positive() } },//
 				{ { "trials", 't', Integer.class, 50 },
 						{ "number of groups to time" }, { Range.positive() } },//
 				{
 						{ "trim", Double.class, .1D },
 						{ "fraction of time-sorted trials to discard from the high and low ends of the sort to eliminate outliers" },
 						{ Range.lowIncl(0, .25) } },//
 				{ { "warmup", 'w', Integer.class, 50000 },
 						{ "number to iterations to warm up the JIT compiler" },
 						{ Range.nonNegative() } },//
 				{ { "cache", 'c', String.class, "array" },
 						{ "match cache type; one of {tree,hash,array}" },
 						{ new StrSet("array", "tree", "hash") } },//
 		};
 		Cli cli = new Cli(spec, Cli.Mod.HELP);
 		cli.parse(args);
 		System.out.println(cli.dump());
 		trials = cli.integer("trials");
 		warmup = cli.integer("warmup");
 		group = cli.integer("group");
		trim = cli.dbl("trim");
 		cache = cli.string("cache");
 		test1();
 		test2();
 		test3();
 		test4();
 		test5();
 		longStringTest();
 	}
 
 	private static void test1() throws IOException {
 		String[] rules = {
 		//
 		"<ROOT> = 'a' | 'b'",//
 		};
 		Pattern p = Pattern.compile("[ab]");
 		String s = "qewrqewrqewraqwreqewr";
 		Grammar g = new Grammar(rules);
 		iterate(p, g, s, false);
 	}
 
 	private static void iterate(Pattern p, Grammar g, String s,
 			boolean allMatches) {
 		System.out.println("=============\n");
 		System.out.println("string: "
 				+ (s.length() > 60 ? s.substring(0, 60) + "... (length "
 						+ s.length() + ")" : s));
 		System.out.println();
 		System.out.println("pattern: " + p);
 		for (int i = 0; i < warmup; i++) {
 			if (allMatches) {
 				java.util.regex.Matcher m = p.matcher(s);
 				while (m.find())
 					;
 			} else {
 				p.matcher(s).find();
 			}
 		}
 		List<Long> times = new ArrayList<Long>();
 		for (int i = 0; i < trials; i++) {
 			long t1 = System.currentTimeMillis();
 			for (int j = 0; j < group; j++) {
 				if (allMatches) {
 					java.util.regex.Matcher m = p.matcher(s);
 					while (m.find())
 						;
 				} else {
 					p.matcher(s).find();
 				}
 			}
 			times.add(System.currentTimeMillis() - t1);
 		}
 		trimmedMean(times);
 		times.clear();
 		Options opt = new Options();
 		if (cache.equals("array"))
 			opt.fatMemory(true);
 		else if (cache.equals("hash"))
 			opt.longStringLength(1);
 		else if (cache.equals("tree"))
 			opt.leanMemory(true);
 		opt.longestMatch(false);
 		System.out.println(g.describe());
 		System.out.println("with studying");
 		timeGrammar(g, s, allMatches, times, opt);
 		System.out.println("without studying");
 		timeGrammar(g, s, allMatches, times, opt);
 		System.out.println("with studying using LTM");
 		opt.study(true);
 		opt.longestMatch(true);
 		timeGrammar(g, s, allMatches, times, opt);
 	}
 
 	private static void timeGrammar(Grammar g, String s, boolean allMatches,
 			List<Long> times, Options opt) {
 		for (int i = 0; i < warmup; i++) {
 			if (allMatches) {
 				Matcher m = g.find(s, opt);
 				while (m.match() != null)
 					;
 			} else {
 				g.find(s, opt).match();
 			}
 		}
 		for (int i = 0, lim = trials; i < lim; i++) {
 			long t = System.currentTimeMillis();
 			for (int j = 0; j < group; j++) {
 				if (allMatches) {
 					Matcher m = g.find(s, opt);
 					while (m.match() != null)
 						;
 				} else {
 					g.find(s, opt).match();
 				}
 			}
 			times.add(System.currentTimeMillis() - t);
 		}
 		trimmedMean(times);
 	}
 
 	private static void trimmedMean(List<Long> times) {
 		Collections.sort(times);
 		int start = (int) Math.round(times.size() * trim);
 		int end = times.size() - start;
 		List<Long> sublist = times.subList(start, end);
 		double avg = 0;
 		for (long l : sublist)
 			avg += l;
 		avg /= sublist.size() * 2000;
 		System.out.printf("%.5f milliseconds per sequence%n", avg);
 	}
 
 	private static void test2() throws IOException {
 		String[] rules = {
 				//
 				"<ROOT> = <a> | <b>", //
 				"<a> = <foo> <s> <bar>",//
 				"<b> = <quux> <s> <baz>",//
 				"<s> = /\\s++/",//
 				"<foo> = /foo/",//
 				"<bar> = /bar/",//
 				"<quux> = /quux/",//
 				"<baz> = /baz/",//
 		};
 		Pattern p = Pattern.compile("foo\\s++bar|quux\\s++baz");
 		String s = "foo bar";
 		Grammar g = new Grammar(rules);
 		iterate(p, g, s, false);
 	}
 
 	private static void test3() throws IOException {
 		String[] rules = {
 				//
 				"<ROOT> = [ <c> | <d> ]{2} <d>",//
 				"<c> = <a>{,2}",//
 				"<d> = <a> <b>",//
 				"<a> = /a/",//
 				"<b> = /b/",//
 		};
 		Pattern p = Pattern.compile("(?:a{0,2}|a{0,2}b){2}b");
 		String s = "aabb";
 		Grammar g = new Grammar(rules);
 		iterate(p, g, s, false);
 	}
 
 	private static void test4() throws IOException {
 		String[] rules = {
 				//
 				"<ROOT> = <c> | <d>",//
 				"<c> = /a/",//
 				"<d> = /b/",//
 		};
 		Pattern p = Pattern.compile("[ab]");
 		String s = "qewrqewrqewraqwreqewr";
 		Grammar g = new Grammar(rules);
 		iterate(p, g, s, false);
 	}
 
 	private static void test5() throws IOException {
 		String[] rules = {
 				//
 				"<ROOT> = [ <a> | <b> ]{2} <b>",//
 				"<a> = 'a'{,2}",//
 				"<b> = 'ab'",//
 		};
 		Pattern p = Pattern.compile("(?:a{0,2}|ab){2}ab");
 		String s = "aabb";
 		Grammar g = new Grammar(rules);
 		iterate(p, g, s, false);
 	}
 
 	private static void longStringTest() throws IOException {
 		String[] rules = {
 		//
 		"<ROOT> = 'cat' | 'dog' | 'monkey'",//
 		};
 		Pattern p = Pattern.compile("cat|dog|monkey");
 		StringBuilder b = new StringBuilder();
 		for (int i = 0; i < 1000; i++) {
 			b.append("__________");
 			switch (i % 3) {
 			case 0:
 				b.append("cat");
 				break;
 			case 1:
 				b.append("dog");
 				break;
 			case 2:
 				b.append("monkey");
 			}
 		}
 		String s = b.toString();
 		Grammar g = new Grammar(rules);
 		iterate(p, g, s, true);
 
 	}
 }
