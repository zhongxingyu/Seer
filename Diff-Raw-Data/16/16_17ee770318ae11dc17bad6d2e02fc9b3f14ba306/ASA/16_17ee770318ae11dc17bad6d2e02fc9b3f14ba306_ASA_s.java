 package gtd.bench;
 
 import gtd.SGTDBF;
 import gtd.generator.FromClassGenerator;
 import gtd.generator.ParserStructure;
 import gtd.grammar.structure.Alternative;
 import gtd.grammar.symbols.Char;
 import gtd.grammar.symbols.Sort;
 
 import java.lang.management.ManagementFactory;
 import java.lang.management.ThreadMXBean;
 
 /*
 S ::= aSa | a 
 */
 public class ASA extends SGTDBF{
 	
 	private ASA(char[] input, ParserStructure structure){
 		super(input, structure);
 	}
 	
 	public static Alternative[] S(){
 		return new Alternative[]{
 			new Alternative(new Char('a'), new Sort("S"), new Char('a')),
 			new Alternative(new Char('a'))
 		};
 	}
 	
 	private final static int ITERATIONS = 3;
 	
 	private static char[] createInput(int size){
 		char[] input = new char[size];
 		for(int i = size - 1; i >= 0; --i){
 			input[i] = 'a';
 		}
 		return input;
 	}
 	
 	private static void cleanup() throws Exception{
 		System.gc();
 		System.gc();
 		System.gc();
 		System.gc();
 		System.gc();
 		Thread.sleep(1000);
 	}
 	
 	private static void runTest(char[] input, ParserStructure structure) throws Exception{
 		ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
 		
 		long total = 0;
 		long lowest = Long.MAX_VALUE;
 		for(int i = ITERATIONS - 1; i >= 0; --i){
 			cleanup();
 			
 			long start = tmxb.getCurrentThreadCpuTime();
 			ASA asa = new ASA(input,structure);
 			asa.parse("S");
 			long end = tmxb.getCurrentThreadCpuTime();
 			
 			long time = (end - start) / 1000000;
 			total += time;
 			lowest = (time < lowest) ? time : lowest;
 		}
 		System.out.println(input.length+": avg="+(total / ITERATIONS)+"ms, lowest="+lowest+"ms");
 	}
 	
 	public static void main(String[] args) throws Exception{
		ParserStructure structure = new FromClassGenerator(WorstCase.class).generate();
 		
 		// Warmup.
 		char[] input = createInput(51);
 		
 		for(int i = 9999; i >= 0; --i){
 			ASA asa = new ASA(input, structure);
 			asa.parse("S");
 		}
 		
 		// The benchmarks.
 		for(int i = 501; i <= 10001; i += 500){
 			input = createInput(i);
 			runTest(input, structure);
 		}
 	}
 }
