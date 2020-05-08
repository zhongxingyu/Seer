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
 S ::= E
 E ::= E + F | F
 F ::= a | ( E )
 */
 public class EFa extends SGTDBF{
 	
 	private EFa(char[] input, ParserStructure structure){
 		super(input, structure);
 	}
 	
 	public static Alternative[] S(){
 		return new Alternative[]{
 			new Alternative(new Sort("E"))
 		};
 	}
 	
 	public static Alternative[] E(){
 		return new Alternative[]{
 			new Alternative(new Sort("E"), new Char('+'), new Sort("F")),
 			new Alternative(new Sort("F"))
 		};
 	}
 	
 	public static Alternative[] F(){
 		return new Alternative[]{
 			new Alternative(new Char('a')),
 			new Alternative(new Char('('), new Sort("E"), new Char(')'))
 		};
 	}
 	
 	private final static int ITERATIONS = 3;
 	
 	private static char[] createInput(int size){
 		int depth = (size - 3) / 4;
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append('a');
 		sb.append('+');
 		addInput(sb, depth);
 		
 		return sb.toString().toCharArray();
 	}
 	
 	private static void addInput(StringBuilder sb, int counter){
 		sb.append('(');
 		sb.append('a');
 		sb.append('+');
 		if(counter != 0) addInput(sb, counter - 1);
 		else sb.append('a');
 		sb.append(')');
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
 			EFa eFa = new EFa(input, structure);
 			eFa.parse("S");
 			long end = tmxb.getCurrentThreadCpuTime();
 			
 			long time = (end - start) / 1000000;
 			total += time;
 			lowest = (time < lowest) ? time : lowest;
 		}
 		System.out.println(input.length+": avg="+(total / ITERATIONS)+"ms, lowest="+lowest+"ms");
 	}
 	
 	public static void main(String[] args) throws Exception{
		ParserStructure structure = new FromClassGenerator(WorstCase.class).generate();
 		
 		char[] input = createInput(5);
 		
 		EFa testOut = new EFa(input, structure);
 		if(testOut.parse("S") != null) System.out.println("WARNING: Running in parser instead of recognizer mode.");
 		
 		// Warmup.
 		for(int i = 9999; i >= 0; --i){
 			EFa eFa = new EFa(input, structure);
 			eFa.parse("S");
 		}
 		
 		for(int i = 200001; i <= 1000001; i += 200000){
 			input = createInput(i);
 			EFa eFa = new EFa(input, structure);
 			eFa.parse("S");
 		}
 		
 		// The benchmarks.
 		for(int i = 200001; i <= 1000001; i += 200000){
 			input = createInput(i);
 			runTest(input, structure);
 		}
 	}
 }
