 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.LinkedList;
 import java.util.List;
 
 import dfh.cli.Cli;
 import dfh.cli.Cli.Opt;
 import dfh.cli.Modifiers;
 import dfh.trie.Trie;
 
 /**
  * Command line utility for growing tries. --help gives full usage information.
  * <p>
  * <b>Creation date:</b> Dec 3, 2011
  * 
  * @author David Houghton
  * 
  */
 public class trieify {
 
 	/**
 	 * Procedural class.
 	 */
 	private trieify() {
 	}
 
 	/**
 	 * @param args
 	 *            provide --help argument for full usage
 	 */
 	public static void main(String[] args) {
 		Cli cli = parseArguments(args);
 		int options = collectOptions(cli);
 		List<String> phrases = new LinkedList<String>();
 		try {
 			for (Object fn : cli.collection("file")) {
 				File f = new File(fn.toString());
 				BufferedReader reader = new BufferedReader(new FileReader(f));
 				String line;
 				while ((line = reader.readLine()) != null)
 					phrases.add(line);
 			}
 			phrases.addAll(cli.argList());
 			String re = Trie.trie(phrases, options);
 			if (cli.bool("java-safe"))
 				re = re.replaceAll("\\\\", "\\\\\\\\");
 			String fn = cli.string("output");
 			if (fn == null)
 				System.out.println(re);
 			else {
 				File f = new File(fn);
 				BufferedWriter writer = new BufferedWriter(new FileWriter(f));
 				writer.write(re);
 				writer.close();
 			}
 		} catch (Exception e) {
 			cli.error(e.toString());
 			cli.usage(1);
 		}
 	}
 
 	private static int collectOptions(Cli cli) {
 		int options = cli.bool("case-sensitive") ? 0 : Trie.CASEINSENSITIVE;
 		if (!cli.bool("no-boundary"))
 			options |= Trie.AUTO_BOUNDARY;
 		if (cli.bool("no-character-classes"))
 			options |= Trie.NO_CHAR_CLASSES;
 		if (!cli.bool("no-condense"))
 			options |= Trie.CONDENSE;
 		if (cli.bool("backtracking"))
 			options |= Trie.BACKTRACKING;
 		int count = 0;
 		if (cli.bool("whitespace-characters")) {
 			count++;
 			options |= Trie.PRESERVE_WHITESPACE;
 		}
 		if (cli.bool("space-and-tab")) {
 			count++;
 			options |= Trie.SPACEANDTAB;
 		}
 		if (cli.bool("space-only")) {
 			count++;
 			options |= Trie.SPACEONLY;
 		}
 		if (count > 1)
 			cli.error("only one of --whitespace-characters, --space-and-tab, and --space-only may be specified");
 		if (cli.bool("perl-safe"))
 			options |= Trie.PERL_SAFE;
 		if (cli.collection("file").isEmpty() && cli.argList().isEmpty())
 			cli.error("no phrases provided");
 		cli.errorCheck();
 		return options;
 	}
 
 	private static Cli parseArguments(String[] args) {
 		String name = trieify.class.getName();
 		Object[][][] spec = {
 				{ { Opt.NAME, name } },//
 				{ { "case-sensitive", 'I' }, { "preserve case distinctions" } },//
 				{ { "no-boundary", 'n' },
 						{ "do not assume word boundaries in input" } },//
 				{ { "no-character-classes", 'C' },
 						{ "use (?>a|b|c) rather than [abc]" } },//
 				{
 						{ "no-condense", 'D' },
						{ "if set, long repeating sequences will be left alone; aaaaaaaaaa -/-> (?:aa){5}" } },//
 				{
 						{ "backtracking", 'b' },
 						{ "whether to drop the possessive suffix + and possessive group prefix (?>" } },//
 				{ { Opt.TEXT } },//
 				{ { "whitespace-characters", 'w' },
 						{ "treat whitespace characters the same as others" } },//
 				{
 						{ "space-and-tab", 's' },
 						{ "treat whitespace characters as signifying [ \\u00a0\\t]" } },//
 				{
 						{ "space-only", 'y' },
 						{ "treat whitespace characters as signifying [ \\u00a0]" } },//
 				{ { Opt.TEXT } },//
 				{ { "file", 'f', String.class },
 						{ "take word list from file; repeatable", "file" },
 						{ Cli.REPEATABLE } },//
 				{ { "output", 'o', String.class },
 						{ "write output to file", "file" } },//
 				{ { Opt.TEXT } },//
				{
						{ "perl-safe", 'p' },
						{ "whether to avoid constructs unavailable in Perl 5.8" } },//
 				{
 						{ "java-safe", 'j' },
 						{ "double all \\ characters so trie can be pasted into a Java string" } },//
 				{ { Opt.ARGS, "phrase", Opt.STAR } },//
 				{ {
 						Opt.USAGE,
 						"condense word lists into trie regex",
 						name
 								+ " provides command line access to "
 								+ Trie.class
 								+ ", letting you condense word lists into a trie regex. \n"
 								+ "The word list will consist of any command line arguments or the lines read in from "
 								+ "files specified by the --file \n"
 								+ "argument.\n\nNote that whitespace receives special treatment unless the --whitespace "
 								+ "option is provided. Whitespace will be \n"
 								+ "trimmed off the ends of all phrases and interior whitespace will be represented by some possessive character \n"
 								+ "class sequence, \\s++ by default. Thus 'foo bar' will be converted into an expression "
 								+ "that matches 'foo', \n"
 								+ "some amount of whitespace, and 'bar'.\n\n"
 								+ "Note that the argument '--' will block option parsing, so if you want to convert '-foo' and '-bar' into \n"
 								+ "(?i:-(?>foo|bar)\\b) you provide "
 								+ name
 								+ " the arguments '-- -foo -bar'. The single character options can be bundled, \n"
 								+ "so \n\n\t" + name
 								+ " -InC foo bar \n\nis the same as \n\n\t"
 								+ name + " -I -n -C foo bar" } },//
 		};
 		Cli cli = new Cli(spec, Modifiers.HELP);
 		cli.parse(args);
 		return cli;
 	}
 
 }
