 package org.lttng.flightbox.junit.model;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.util.List;
 
 import org.eclipse.linuxtools.lttng.jni.exception.JniException;
 import org.junit.Test;
 import org.lttng.flightbox.dep.BlockingItem;
 import org.lttng.flightbox.dep.BlockingTaskListener;
 import org.lttng.flightbox.io.ModelBuilder;
 import org.lttng.flightbox.junit.Path;
 import org.lttng.flightbox.model.SystemModel;
 import org.lttng.flightbox.model.Task;
 import org.lttng.flightbox.model.WaitInfo;
 
 public class TestDependencyAnalysis {
 
 	@Test
 	public void testNanosleep() throws JniException {
 		String tracePath = new File(Path.getTraceDir(), "sleep-1x-1sec").getPath();
 		SystemModel model = new SystemModel();
 		BlockingTaskListener listener = new BlockingTaskListener();
 		model.addTaskListener(listener);
 
 		ModelBuilder.buildFromTrace(tracePath, model);
 
 		Task foundTask = model.getLatestTaskByCmdBasename("sleep");
 		List<BlockingItem> taskItems = listener.getBlockingItemsForTask(foundTask);
 
 		assertEquals(1, taskItems.size());
 		WaitInfo info = taskItems.get(0).getWaitInfo();
 		double duration = info.getEndTime() - info.getStartTime();
 		assertEquals(1000000000.0, duration, 10000000.0);
 	}
 
 	@Test
 	public void testInception() throws JniException {
		File file = new File(Path.getTraceDir(), "inception-3x-100ms");
		if (!file.isDirectory() || !file.canRead()) {
			return;
		}
 
 		String tracePath = file.getPath();
 		SystemModel model = new SystemModel();
 		BlockingTaskListener listener = new BlockingTaskListener();
 		model.addTaskListener(listener);
 
 		ModelBuilder.buildFromTrace(tracePath, model);
 
 		// get the last spawned child
 		Task foundTask = model.getLatestTaskByCmdBasename("inception");
 		List<BlockingItem> taskItems = listener.getBlockingItemsForTask(foundTask);
 
 		// 100ms + 200ms + 400ms = 700ms
 		assertEquals(1, taskItems.size());
 		WaitInfo info = taskItems.get(0).getWaitInfo();
 		double duration = info.getEndTime() - info.getStartTime();
 		assertEquals(400000000.0, duration, 10000000.0);
 	}
 
 }
