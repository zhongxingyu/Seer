 import java.text.*;
 import java.util.*;
 
 class WarGameThread extends Thread {
 
 	public int processed = 0;
 	public boolean terminate = false;
 
 	public WarGameThread() {
 		super();
 		start();
 	}
 
 	public void terminate() {
 		this.terminate = true;
 	}
 	
 	public void run() {
 		while ( !this.terminate ) {
 			this.processed++;
 			WarGame wg = new WarGame();
 		}	
 
 	}
 
 }
 
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
 
 	public static void main(String[] args) throws Exception {
 		
 		NumberFormat pf = NumberFormat.getPercentInstance();
 		pf.setMaximumFractionDigits(2);
 		NumberFormat df = NumberFormat.getInstance();
 		df.setMaximumFractionDigits(4);
 		df.setMinimumFractionDigits(4);
 		NumberFormat inf = NumberFormat.getIntegerInstance();
 
 		final long ms = 1000000L;
 		final long ns = 1000000000L;
 
 		System.out.println( "    =====================    " );
 		System.out.println( "    ====  WarGame 2  ====    " );
 		System.out.println( "    =====           =====    " );
 		System.out.println( "    ===   V 1.0.1.0   ===    " );
 		System.out.println( "    =====================    " );
 		System.out.println();
 
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
 		System.out.println("Beginning priming phase...");		
 		System.out.println();		
 				
 		WarGameThread[] wgts = new WarGameThread[ threads ];
 
 
 		String display_tail = "";
 		long print_last = getTime();
 
 		int tests = 1;
 		double completed = 0;
 		double speed = 0;
 		double speed_low = 0, speed_high = 0, prime_speed = 0;
 		double percent_speed = 0;
 
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
 
			 speed = 1 / (elapsed_time / completed);
			 //speed = completed / elapsed_time;
 
 			 if ( !test_started && elapsed_time >= prime_time ) {
 				test_started = true;
				
				test_duration = (long)(1 + Math.ceil( (speed * ms) ));
 				test_initial = elapsed_time + ( test_duration * ns);
				// test_duration = (long)(1 + Math.ceil( 1 / (speed / ms) ));
				// test_initial = elapsed_time + ( test_duration * ns) ;

 				percent_speed = speed * percent_variation;
 				speed_low = speed - percent_speed;
 				speed_high = speed + percent_speed;
 				prime_speed = speed;
 				System.out.println();
 			 } else if ( test_started && elapsed_time >= test_initial ) {
 
 			 	if ( speed_low < speed && speed < speed_high ) {
 			 		terminateThreads(wgts);
 			 	} else if ( tests >= 100 ) {
 			 		terminateThreads(wgts);
 			 	} else {

					test_duration = (long)(1 + Math.ceil( (speed * ms) ));
 					test_initial = elapsed_time + ( test_duration * ns);
					// test_duration = (long)(1 + Math.ceil( 1 / (speed / ms) ));
					// test_initial = elapsed_time + ( test_duration * ns );

 					percent_speed = speed * percent_variation;
 					speed_low = speed - percent_speed;
 					speed_high = speed + percent_speed;
 					tests++;
 			 	}
 
 			 }
 			 else {
			 	if ( test_started ) display_tail = "Test #" + tests + " at " + inf.format( (test_initial - elapsed_time) / ns  ) + " seconds";
 			 	else display_tail = inf.format( (prime_time - elapsed_time) / ns ) + " seconds left...";
 			 }
 			 
 
 			 if ( ( current_time - print_last) > (1000 * ms) ) {
 			 	print_last = current_time;
 			 	System.out.print("\r " + " Speed: " + df.format( (speed * ms) ) + " (g/ms) - " + display_tail );
 			 	Thread.sleep(500);
 			 }
 
 		}
 
 		long end = getTime();
 
 		System.out.print("\r " + " Speed: " + df.format( (speed * ms) ) + " (g/ms) - " + display_tail );
 
 		System.out.println();
 		System.out.println();
 				
 		System.out.println( "  Elasped time: " + inf.format((end - start) / ns) + " seconds or about " + df.format((end - start) / ms / 1000.0 / 60.0) + " minutes ");
 		System.out.println( "  Games completed: " + inf.format( getCompleted(wgts) ) + " " );
 		System.out.println( "  Tests completed: " + inf.format( tests ) + " " );
 		System.out.println( "  Threads used: " + threads + " ");
 		System.out.println( "  Confidence: " + pf.format( percent_variation ) + " " );
 
 
 		System.out.println();
 		
 		System.out.println( "  " + inf.format(tests) + " tests improved speed by " + pf.format( 1 - (prime_speed / speed) ) + "");
 		System.out.println( "   from " + df.format(prime_speed * ms) + " (g/ms) to " + df.format(speed * ms) + " (g/ms)");
 		System.out.println( "  Final confidence range:\n   " + df.format(speed_low * ms) + " (g/ms) < " + df.format(speed * ms) + " (g/ms) < " + df.format(speed_high * ms) + " (g/ms)" );
 
 		System.out.println();
 
 		System.out.println( "  Final speed: " + df.format( (speed * ms) ) + " (g/ms)" );
 		
 		System.out.println();
 	}
 
 }
 
 
