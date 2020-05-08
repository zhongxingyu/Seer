 package pleocmd;
 
 import pleocmd.itfc.cli.CommandLine;
 import pleocmd.itfc.gui.MainFrame;
 
 /**
  * TODO: List of tasks:<br>
  * introduce priority system<br>
  * split pipe in 3 threads: in, cvt and out<br>
  * exit while pipe is running?<br>
  * detect endless looping in convert()<br>
  * checkstyle: no exception handling in pipe's, only pass them thru<br>
  * implement DataFilter<br>
  * check terminology (Value, Data, Block, Sequence)<br>
  * add javadoc<br>
  */
 
 public final class Main {
 
 	private Main() {
 		// utility class => hidden
 	}
 
 	@SuppressWarnings("unused")
 	public static void main(final String[] args) throws Exception {
 		if (args.length > 0)
 			CommandLine.the().parse(args);
 		else
			new Thread(new ThreadGroup("Exception-handling Group") {
				@Override
				public void uncaughtException(final Thread thread,
						final Throwable throwable) {
					Log.error(throwable);
				}
			}, "GUI-Thread") {
				@Override
 				public void run() {
 					MainFrame.the();
 				}
			}.start();
 	}
 
 }
