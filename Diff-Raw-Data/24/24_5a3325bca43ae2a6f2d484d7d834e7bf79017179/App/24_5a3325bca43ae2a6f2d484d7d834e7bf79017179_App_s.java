 import java.text.*;
 import java.util.*;
 
 class App {
 
 
 
 	public static boolean isAlive(WarGameThread[] threads) {
 		boolean life = false;
 		for (WarGameThread t : threads) {
 			if (t.isAlive()) {
 				life = true;
 				break;
 			}
 		}
 		return life;
 	}
 
 	public static int getCompleted(WarGameThread[] threads) {
 		int processed = 0;
 		for (WarGameThread t : threads ) {
 			processed = processed + t.processed;
 		}
 
 		return processed;
 	}
 	
 	public static void terminateThreads(WarGameThread[] threads) {
 		for (WarGameThread t: threads) {
 			t.terminate();
 		}
 	}
 
 	public static long getTime() {
 		return System.nanoTime();
 	}
 
 	public static void print_help() {
 			System.out.println( "  --- Help");
 			System.out.println( "  ---  java App: runs with default settings");
 			System.out.println( "  ---  java App s: runs a short test");
 			System.out.println( "  ---  java App i: allows user to choose settings");
 			System.out.println( "  ---  java App help: displays this information");
 			System.out.println( "    --  threads: defaults to the number of logical processors available");
 			System.out.println( "    --  confidence threshold: defaults to 00.01%, results will be +/- .01%");
 			System.out.println( "    --  prime time: the period to wait before beginning testing");
 			System.out.println( "  ---  Visit http://ifupdown.com/wg for more information...");
 			System.out.println();
 			System.out.println();
 	}
 
 	public static void print_header() {
 		System.out.println( "    =====================    " );
 		System.out.println( "    ====  WarGame 2  ====    " );
 		System.out.println( "    =====           =====    " );
 		System.out.println( "    ===   V 1.0.1.0   ===    " );
 		System.out.println( "    =====================    " );
 		System.out.println();
 	}
 
 	public static void main(String[] args) throws Exception {
 		
 		print_header();
 
 		NumberFormat pf = NumberFormat.getPercentInstance();
 		NumberFormat df = NumberFormat.getInstance();
 		NumberFormat inf = NumberFormat.getIntegerInstance();
 
 		pf.setMaximumFractionDigits(2);
 		df.setMaximumFractionDigits(4);
 		df.setMinimumFractionDigits(4);
 
 
 		final long ms = 1000000L;
 		final long ns = 1000000000L;
 
 
 		int threads = Runtime.getRuntime().availableProcessors();
 		double percent_variation = 0.0001;
 		long prime_time = 60 * ns;
 
 		if ( args.length > 0 && args[0].equals("i") ) {
 			Scanner scanner = new Scanner(System.in);
 			int temp;
 
 			System.out.println( " --- Interactive Mode" );
 
 			System.out.print( "  -- Enter number of threads\n   (enter 0 for " + threads + " threads): " );
 			temp = scanner.nextInt();
 			if ( 0 != temp ) threads = temp;
 			
 
 			System.out.print( "  -- Enter confidence threshold [10^-n]\n   (enter 0 for " + Math.abs(Math.log10(percent_variation)) + "): " );
 			temp = scanner.nextInt();
 			if ( 0 != temp ) {
 				percent_variation = (double)(Math.pow(10, -1 * temp));
 				if (temp > 4) pf.setMaximumFractionDigits( temp + 1 );
 			}
 			
 
 			System.out.print( "  -- Enter priming time\n   (enter 0 for " + inf.format(prime_time / ns) + " seconds): " );
 			temp = scanner.nextInt();
 			if ( 0 != temp ) prime_time = ns * (long)temp;
 
 		} else if ( args.length > 0 && args[0].equals("s") ) {
 			System.out.println( " --- Short Run Mode" );
 			percent_variation = (double)(Math.pow(10, -1 * 2));
 			prime_time = ns * (long)10;
 
 		} else if ( args.length > 0 && (args[0].equals("h") || args[0].equals("help")) ) {
 			print_help();
 			System.exit(1);
 		}
 
 		System.out.println();
 		System.out.println( "  Using " + threads + " for number of threads." );
 		System.out.println( "  Using " + Math.abs(Math.log10(percent_variation)) + " [" + pf.format(percent_variation) + "] for confidence threshold." );
 		System.out.println( "  Using " + inf.format(prime_time / ns) + " seconds for prime time." );
 		System.out.println();
 				
 		WarGameThread[] wgts = new WarGameThread[ threads ];
 
 
 		String display_tail = "";
 		long print_last = getTime();
 
 		int tests = 1;
 		double completed = 0;
 		double speed = 0;
 		double rate = 0;
 		double rate_low = 0, rate_high = 0, prime_speed = 0;
 		double percent_rate = 0;
 
 		boolean test_started = false;
 
 		long elapsed_time = 0;
 		long current_time = 0;
 		long test_initial = 0;
 		long test_duration = 0;
 
 		long start = getTime();
 
 		for (int i = 0; i < threads; i++) {
 			wgts[i] = new WarGameThread();
 		}
 
 		while ( isAlive(wgts) ) {
 			
 			 completed = getCompleted(wgts);
 			 current_time = getTime();
 			 elapsed_time = current_time - start;
 
 			 rate = (elapsed_time / completed);
 			 speed = 1 / rate;
 
 			 if ( !test_started && elapsed_time >= prime_time ) {
 				test_started = true;
 				
 				test_duration = (long)(1 + Math.ceil( (speed * ms) ));
 				test_initial = elapsed_time + ( test_duration * ns );
 
 				percent_rate = rate * percent_variation;
 
 				rate_low = rate - percent_rate;
 				rate_high = rate + percent_rate;
 
 				prime_speed = speed;
 				// System.out.println();
 			 } else if ( test_started && elapsed_time >= test_initial ) {
 
 			 	if ( rate_low < rate && rate < rate_high ) {
 			 		terminateThreads(wgts);
 			 	} else if ( tests >= 100 ) {
 			 		terminateThreads(wgts);
 			 	} else {
 
 					test_duration = (long)(1 + Math.ceil( (speed * ms) ));
 					test_initial = elapsed_time + ( test_duration * ns );
 
 					percent_rate = rate * percent_variation;
 
 					rate_low = rate - percent_rate;
 					rate_high = rate + percent_rate;
 
 					tests++;
 			 	}
 
 			 }			 
 
			 if ( ( current_time - print_last) > (1000 * ms) ) {
 			 	if ( test_started ) display_tail = "Test #" + tests + " at " + inf.format( (test_initial - elapsed_time) / ns  ) + " seconds";
 			 	else display_tail = inf.format( (prime_time - elapsed_time) / ns ) + " seconds left...";
			 	print_last = current_time;
 			 	System.out.print("\r " + " Speed: " + df.format( (speed * ms) ) + " (g/ms) - " + display_tail );
 			 	Thread.sleep(500);
 			 }
 
 		}
 
 		long end = getTime();
 
 		System.out.print("\r " + " Speed: " + df.format( (speed * ms) ) + " (g/ms) - " + display_tail );
 
 		System.out.println();
 		System.out.println();
 				
 		System.out.println( "  Elapsed time: \n\t" + 
			inf.format((end - start) / ns) + " seconds or \n\t~" + df.format((end - start) / ms / 1000.0 / 60.0) + " minutes");
 		System.out.println( "  Games completed: \t" + inf.format( getCompleted(wgts) ) + " " );
 		System.out.println( "  Tests completed: \t" + inf.format( tests ) + " " );
		System.out.println( "  Threads used: \t" + threads + " ");
		System.out.println( "  Confidence range: \t" + pf.format( percent_variation ) + " " );

 
 		System.out.println();
 		
		System.out.println( "  " + inf.format(tests) + " tests improved speed by " + pf.format( 1 - (prime_speed / speed) ) + "");
 		System.out.println( "   from " + df.format(prime_speed * ms) + " (g/ms) to " + df.format(speed * ms) + " (g/ms)");
 		System.out.println( "  Final confidence range:\n   " + df.format(1/rate_low * ms) + " (g/ms) > " + df.format(speed * ms) + " (g/ms) > " + df.format(1/rate_high * ms) + " (g/ms)" );
 
 		System.out.println();
 
 		System.out.println( "  Final rate: \t" + df.format( (rate / ms) ) + " (ms/g)" );
 		System.out.println( "  Final speed: \t" + df.format( (speed * ms) ) + " (g/ms)" );
 		System.out.println();
		System.out.println( "  Final speed: \t" + inf.format( Math.round((speed * ms) * 1000)) + " (g/s)" );
 		
 		System.out.println();
 	}
 
 }
 
 
