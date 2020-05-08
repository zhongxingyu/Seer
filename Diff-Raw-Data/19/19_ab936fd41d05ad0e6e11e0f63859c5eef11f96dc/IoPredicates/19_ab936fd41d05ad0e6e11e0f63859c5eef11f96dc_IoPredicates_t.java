 package suite.lp.predicate;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
import java.io.PrintStream;
 import java.util.Date;
 
 import suite.lp.doer.Prover;
 import suite.lp.predicate.SystemPredicates.SystemPredicate;
 import suite.node.Int;
 import suite.node.Node;
 import suite.node.Str;
 import suite.node.Tree;
 import suite.node.io.Formatter;
 import suite.util.FileUtil;
 import suite.util.LogUtil;
 import suite.util.To;
 
 public class IoPredicates {
 
 	public static class Dump implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			System.out.print(Formatter.dump(ps));
 			return true;
 		}
 	}
 
 	public static class DumpStack implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			String date = To.string(new Date());
 			String trace = prover.getTracer().getStackTrace();
 			LogUtil.info("-- Stack trace at " + date + " --\n" + trace);
 			return true;
 		}
 	}
 
 	public static class Exec implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			if (ps instanceof Str)
 				try {
 					String cmd = ((Str) ps).getValue();
 					return Runtime.getRuntime().exec(cmd).waitFor() == 0;
 				} catch (Exception ex) { // IOException or InterruptedException
 					LogUtil.error(ex);
 				}
 			return false;
 		}
 	}
 
 	public static class Exit implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			System.exit(ps instanceof Int ? ((Int) ps).getNumber() : 0);
 			return true;
 		}
 	}
 
 	public static class FileExists implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			return new File(Formatter.display(ps)).exists();
 		}
 	}
 
 	public static class FileRead implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			final Node params[] = Tree.getParameters(ps, 2);
 			String filename = Formatter.display(params[0]);
 			try {
 				String content = To.string(new File(filename));
 				return prover.bind(new Str(content), params[1]);
 			} catch (IOException ex) {
 				throw new RuntimeException(ex);
 			}
 		}
 	}
 
 	public static class FileWrite implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			final Node params[] = Tree.getParameters(ps, 2);
 			String filename = Formatter.display(params[0]);
 			String content = Formatter.display(params[1]);
 
 			try (OutputStream fos = FileUtil.out(new File(filename))) {
 				fos.write(content.getBytes(FileUtil.charset));
 			} catch (IOException ex) {
 				throw new RuntimeException(ex);
 			}
 
 			return true;
 		}
 	}
 
 	public static class HomeDir implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			String homeDir = System.getProperty("home.dir");
 			homeDir = homeDir != null ? homeDir : ".";
 			return prover.bind(new Str(homeDir), ps);
 		}
 	}
 
 	public static class Nl implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			System.out.println();
 			return true;
 		}
 	}
 
 	public static class Log implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			LogUtil.info(Formatter.dump(ps));
 			return true;
 		}
 	}
 
 	public static class Sink implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			prover.config().getSink().sink(ps);
 			return false;
 		}
 	}
 
 	public static class Source implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			Node source = prover.config().getSource().source();
 			return prover.bind(ps, source);
 		}
 	}
 
 	public static class Write implements SystemPredicate {
		private PrintStream printStream;

		public Write(PrintStream printStream) {
			this.printStream = printStream;
		}

 		public boolean prove(Prover prover, Node ps) {
			printStream.print(Formatter.display(ps));
 			return true;
 		}
 	}
 
 }
