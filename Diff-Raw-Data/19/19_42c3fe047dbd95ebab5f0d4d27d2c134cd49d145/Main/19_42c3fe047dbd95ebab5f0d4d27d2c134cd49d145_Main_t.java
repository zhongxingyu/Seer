 package pleocmd;
 
 import pleocmd.itfc.cli.CommandLine;
 import pleocmd.itfc.gui.MainFrame;
 
 /**
  * TODO: List of tasks:<br>
  * add send button to input (before "send eos")<br>
  * exit while pipe is running?<br>
  * detect endless looping in convert()<br>
  * checkstyle: no exception handling in pipe's, only pass them thru<br>
  * implement DataFilter<br>
  * check terminology (Value, Data, Block, Sequence, Ascii <-> ASCII)<br>
  * add javadoc<br>
  * move Data... classes to value package<br>
 * autodetect pipeparts, not hardcoded<br>
 * implement static PipePart.help()<br>
  */
 
 public final class Main {
 
 	private Main() {
 		// utility class => hidden
 	}
 
	public static void main(final String[] args) {
 		if (args.length > 0)
 			CommandLine.the().parse(args);
 		else
 			new Thread(new ThreadGroup("Exception-handling Group") {
 				@Override
 				public void uncaughtException(final Thread thread,
 						final Throwable throwable) {
					throwable.printStackTrace();
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
