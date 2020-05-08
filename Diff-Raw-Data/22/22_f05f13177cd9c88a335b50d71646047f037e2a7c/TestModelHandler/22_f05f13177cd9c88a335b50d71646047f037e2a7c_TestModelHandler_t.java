 package org.lttng.flightbox.junit.model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.util.List;
 
 import org.eclipse.linuxtools.lttng.jni.exception.JniException;
 import org.junit.Test;
 import org.lttng.flightbox.io.TraceEventHandlerModel;
 import org.lttng.flightbox.io.TraceEventHandlerModelMeta;
 import org.lttng.flightbox.io.TraceReader;
 import org.lttng.flightbox.junit.Path;
 import org.lttng.flightbox.model.SymbolTable;
 import org.lttng.flightbox.model.SystemModel;
 import org.lttng.flightbox.model.Task;
 
 public class TestModelHandler {
 
 	static double nanosec = 1000000000;
 	static double p = 100000000.0;
 
 	@Test
 	public void testSyscallTable() throws JniException {
 		String tracePath = new File(Path.getTraceDir(), "sleep-1x-1sec").getPath();
 		SystemModel model = new SystemModel();
 
 		// read metadata and statedump
 		TraceEventHandlerModelMeta handlerMeta = new TraceEventHandlerModelMeta();
 		handlerMeta.setModel(model);
 		TraceReader readerMeta = new TraceReader(tracePath);
 		readerMeta.register(handlerMeta);
 		readerMeta.process();
 
 		SymbolTable syscalls = model.getSyscallTable();
 		SymbolTable interrupts = model.getInterruptTable();
 		SymbolTable softirq = model.getSoftIRQTable();
 
 		// FIXME: will this work on another architecture than x86?
 		assertTrue(syscalls.getMap().size() > 300); // was 336
 
		assertEquals(256, interrupts.getMap().size()); // is always 256
 
 		assertTrue(softirq.getMap().size() > 30); // was 32
 	}
 
 	@Test
 	public void testModelHandler() throws JniException {
 		/*
 		 * This test is based on the fact that the script sleep-1x-1sec
 		 * spawn only one child /bin/sleep
 		 */
 		String tracePath = new File(Path.getTraceDir(), "sleep-1x-1sec").getPath();
 		SystemModel model = new SystemModel();
 
 		// read metadata and statedump
 		TraceEventHandlerModelMeta handlerMeta = new TraceEventHandlerModelMeta();
 		handlerMeta.setModel(model);
 		TraceReader readerMeta = new TraceReader(tracePath);
 		readerMeta.register(handlerMeta);
 		readerMeta.process();
 
 		// read all trace events
 		TraceEventHandlerModel handler = new TraceEventHandlerModel();
 		handler.setModel(model);
 		TraceReader readerTrace = new TraceReader(tracePath);
 		readerTrace.register(handler);
 		readerTrace.process();
 
 		// verify there is /bin/sleep task as child of sleep-1x-1sec
 		Task foundTask = model.getLatestTaskByCmdBasename("sleep-1x-1sec");
 		assertNotNull(foundTask);
 		List<Task> children = foundTask.getChildren();
 		Task task = children.get(0);
 		String cmd = task.getCmd();
 		assertEquals("/bin/sleep", cmd);
 		double duration = (task.getExitTime() - task.getCreateTime());
 		assertEquals(duration, nanosec, p);
 	}
 
 }
