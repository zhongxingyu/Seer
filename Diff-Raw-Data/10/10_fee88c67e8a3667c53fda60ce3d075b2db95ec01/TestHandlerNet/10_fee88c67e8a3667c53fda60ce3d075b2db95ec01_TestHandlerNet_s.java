 package org.lttng.flightbox.junit;
 
 import java.io.File;
 
 import org.eclipse.linuxtools.lttng.jni.exception.JniException;
 import org.junit.Test;
 import org.lttng.flightbox.cpu.TraceEventHandlerProcess;
 import org.lttng.flightbox.io.EventQuery;
 import org.lttng.flightbox.io.TraceReader;
 import org.lttng.flightbox.net.TraceEventHandlerNet;
 
 public class TestHandlerNet {
 
	@Test
 	public void testHandlerNetSimple() throws JniException {
 		String trace_path = new File("/home/francis/workspace/traces/client-server-depanalysis", "trace-cmd-deadbeef2227").toString();
 		// FIXME: the handler should provide the events it needs to the reader
 		EventQuery net_query = new EventQuery();
 		net_query.addEventType("net");
 		net_query.addEventName("dev_xmit_extended");
 		net_query.addEventType("net");
 		net_query.addEventName("tcpv4_rcv_extended");
 		TraceEventHandlerNet net_handler = new TraceEventHandlerNet();
 		
 		EventQuery sched_query = new EventQuery();
 		sched_query.addEventType("kernel");
 		sched_query.addEventName("sched_schedule");
 		sched_query.addEventName("process_fork");
 		sched_query.addEventType("fs");
 		sched_query.addEventName("exec");
 		sched_query.addEventType("task_state");
 		sched_query.addEventName("process_state");
 		TraceEventHandlerProcess sched_handler = new TraceEventHandlerProcess();
 		
 		TraceReader reader = new TraceReader(trace_path);
 		reader.register(net_handler);
 		reader.register(sched_handler);
 		reader.process();
 	}
 	
 }
