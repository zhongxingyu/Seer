 package us.exultant.ahs.io;
 
 import us.exultant.ahs.core.*;
 import us.exultant.ahs.thread.*;
 import java.nio.channels.*;
 
 public class InputSystem<$MSG> {
 	public static <$MSG> InputSystem<$MSG> setup(WriteHead<$MSG> $sink, ReadableByteChannel $source, ChannelReader<$MSG> $framer) {
 		return setup(WorkManager.getDefaultScheduler(), $sink, $source, $framer);
 	}
 	public static <$MSG> InputSystem<$MSG> setup(WorkScheduler $scheduler, WriteHead<$MSG> $sink, ReadableByteChannel $source, ChannelReader<$MSG> $framer) {
 		return null;	//TODO:AHS:IO: behavior for filesystem or other crap that doesn't match the SelectableChannel interface
 	}
 	
 	public static <$MSG, $CHAN extends SelectableChannel & ReadableByteChannel> InputSystem<$MSG> setup(WriteHead<$MSG> $sink, $CHAN $source, ChannelReader<$MSG> $framer) {
 		return setup(WorkManager.getDefaultScheduler(), IOManager.getDefaultSelectionSignaller(), $sink, $source, $framer);
 	}
 	public static <$MSG, $CHAN extends SelectableChannel & ReadableByteChannel> InputSystem<$MSG> setup(WorkScheduler $scheduler, SelectionSignaller $selector, WriteHead<$MSG> $sink, $CHAN $source, ChannelReader<$MSG> $framer) {
		if ($source.isBlocking()) throw new IllegalArgumentException("channel must be in nonblocking mode");
 		final InputSystem_WorkerChannelSelectable<$MSG, $CHAN> $wt = new InputSystem_WorkerChannelSelectable<$MSG, $CHAN>($selector, $sink, $source, $framer);
 		final WorkFuture<$MSG> $wf = $scheduler.schedule($wt, ScheduleParams.NOW);
 		$wt.install($wf);
 		return new InputSystem<$MSG>($scheduler, $sink, $framer, $wt, $wf);
 	}
 	
 	
 	
 	private InputSystem(WorkScheduler $scheduler, WriteHead<$MSG> $sink, ChannelReader<$MSG> $framer, WorkTarget<$MSG> $worker, WorkFuture<$MSG> $future) {
 		this.$framer = $framer;
 		this.$sink = $sink;
 		this.$worker = $worker;
 		this.$future = $future;
 		this.$scheduler = $scheduler;
 	}
 
 	/** System parameter. */
 	private final ChannelReader<$MSG>	$framer;
 
 	/** System parameter.  Where we push our freshly read messages into. */
 	private final WriteHead<$MSG>		$sink;
 	
 	/** Always made by factory methods based on the channel type we get. */
 	private final WorkTarget<$MSG>		$worker;	//XXX:AHS:IO: actually not sure why we'd ever need this pointer.
 	
 	/** Always made by factory methods, since we often have things to do that are closely bound to the initial scheduling. */
 	private final WorkFuture<$MSG>		$future;
 
 	/** System parameter.  Usually defaults to {@link WorkManager#getDefaultScheduler()}. */
 	private final WorkScheduler		$scheduler;
 	
 	public WorkFuture<$MSG> getWorkFuture() {
 		return this.$future;
 	}
 }
