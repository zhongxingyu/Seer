 package twp.application;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import twp.core.Container;
 import twp.core.Extension;
 import twp.core.GenericRegisteredExtension;
 import twp.generated.CalculatorError;
 import twp.generated.CalculatorHandler;
 import twp.generated.CalculatorProtocol;
 import twp.generated.CalculatorReply;
 import twp.generated.CalculatorRequest;
 import twp.generated.Double;
 import twp.generated.Expression;
 import twp.generated.LoggingProtocol;
 import twp.generated.Parameters;
 import twp.generated.Term;
 import twp.generated.ThreadID;
 import twp.generated.ThreadID2;
 import twp.generated.ThreadID3;
 import twp.generated.ThreadID4;
 import twp.generated.ThreadID5;
 import twp.generated.ThreadID6;
 import twp.generated.ThreadID7;
 import twp.generated.ThreadID8;
 
 abstract public class CalculatorServerHandler implements CalculatorHandler {
 
 	private static int jobId = 0;
 	
 	private class Job {
 		public CalculatorRequest request;
 		public Double[] values;
 		public int max;
 		public Job(CalculatorRequest req, int max) {
 			request = req; this.max = max;
 			values = new Double[max];
 		}
 	}
 	
 	// our job center, requests are stored here until all intermediate results are resolved 
 	private HashMap<Integer, Job> jobs = new HashMap<Integer, Job>(); 
 	
 	private LoggingProtocol logger;
 	
 	public CalculatorServerHandler(String host, int port) {
 		try {
 			logger = new LoggingProtocol(host, port);
 		} catch (UnknownHostException e) {
 			logger = null;
 			e.printStackTrace();
 		} catch (IOException e) {
 			logger = null;
 			e.printStackTrace();
 		}
 	}
 	
 	abstract protected int getParamCount();
 	
 	@Override
 	public void onCalculatorError(CalculatorError message) {
 		System.err.println(message.getText());
 	}
 
 	@Override
 	public void onCalculatorReply(CalculatorReply message) {
 		int[] id = decomposeId(message.getRequestId());
 		Job job = updateJob(id[0], id[1], message.getResult());
 		if (job != null) {
 			// all intermediate results were returned
 			// so finish the job
			if (handleThreadID(job.request.getExtensions()) != null)
				log(handleThreadID(job.request.getExtensions()).toString(), getOperationForLog(job.values));
 			finish(job.request.getProtocol(), job.request.getRequestId(), job.values);
 		}
 		
 	}
 
 	@Override
 	public void onCalculatorRequest(CalculatorRequest message) {
 		Extension threadId = handleThreadID(message.getExtensions());
 		if (getParamCount() == 0) {
 			// in case we implement a constant
 			finish(message.getProtocol(), message.getRequestId(), new Double[0]);
 		} else if (message.getArguments().getElements().size() != getParamCount())
 			try {
 				message.getProtocol().sendError("Wrong number of parameters (" + getParamCount() + " were expected).");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		else {
 			// correct number of parameters
 			Job job = new Job(message, getParamCount());
 			int i = 0;
 			HashMap<Integer, Expression> resolve = new HashMap<Integer, Expression>();
 			for (Term term:message.getArguments().getElements()) {
 				// check if all given parameters are already Doubles or 
 				// if we have to contact some other calculation service
 				if (term.getContentClass() == Double.class)
 					job.values[i] = term.getValue();
 				else 
 					resolve.put(i, term.getExpr());
 				i++;
 			}
 			if (resolve.isEmpty()) {
 				// nothing to resolve, just calculate the result
				if (threadId != null)
					log(threadId.toString(), getOperationForLog(job.values));
 				finish(message.getProtocol(), message.getRequestId(), job.values);
 			} else {
 				// store the job and send the requests to the other services
 				int id = addJob(job);
 				for (int key:resolve.keySet()) {
 					Expression expr = resolve.get(key);
 					sendRequest(expr.getHost(), expr.getPort(), expr.getArguments(), composeId(id, key), threadId);
 					if (threadId instanceof ThreadID7)
 						((ThreadID7) threadId).incPath();
 				}
 			}
 				
 		}
 	}
 	
 	protected void log(String threadId, String message) {
 		if (logger != null) {
 			Date date = new Date();
 			try {
 				logger.sendLogEntry((int) (date.getTime() / 1000), (int) (date.getTime() * 1000), getOperationName(), threadId, message);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private void finish(CalculatorProtocol protocol, int requestId, Double...values) {
 		Double result = null;
 		String error = null;
 		try {
 			result = runCalculation(values);
 		} catch(Exception e) {
 			// an error occured during calculation
 			error = e.getMessage();
 		}
 		try {
 			if (error == null)
 				protocol.sendReply(requestId, result);
 			else
 				protocol.sendError(error);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	abstract protected String getOperationName();
 	
 	abstract protected String getOperationForLog(Double...values);
 	
 	private Extension handleThreadID(List<GenericRegisteredExtension> extensions) {
 		Extension container = null;
 		for (GenericRegisteredExtension ext:extensions) {
 			switch(ext.getId()) { 
 				case ThreadID.ID:
 					container = new ThreadID(ext);
 					((ThreadID) container).incDepth();
 					break;
 				case ThreadID2.ID:
 					container = new ThreadID2(ext);
 					break;
 				case ThreadID3.ID:
 					container = new ThreadID3(ext);
 					((ThreadID3) container).incDepth();
 					break;
 				case ThreadID4.ID:
 					container = new ThreadID4(ext);
 					((ThreadID4) container).incDepth();
 					break;
 				case ThreadID5.ID:
 					container = new ThreadID5(ext);
 					((ThreadID5) container).incDepth();
 					break;
 				case ThreadID6.ID:
 					container = new ThreadID6(ext);
 					((ThreadID6) container).incDepth();
 					break;
 				case ThreadID7.ID:
 					container = new ThreadID7(ext);
 					break;
 				case ThreadID8.ID:
 					container = new ThreadID8(ext);
 					break;
 			}
 		}
 		return container;
 	}
 	
 	private void sendRequest(byte[] host, int port, Parameters args, int reqId, Container threadId) {
 		try {
 			String ip = InetAddress.getByAddress(host).getHostAddress();
 			CalculatorProtocol prot = new CalculatorProtocol(ip, port, this);
 			if (threadId != null) {
 				ArrayList<Container> list = new ArrayList<Container>();
 				list.add(threadId);
 				prot.sendRequest(reqId, args, list);
 			} else 
 				prot.sendRequest(reqId, args);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private int composeId(int reqId, int index) {
 		return reqId * 10 + index;
 	}
 	
 	private int[] decomposeId(int combined) {
 		int[] result = new int[2];
 		result[0] = combined / 10;
 		result[1] = combined % 10;
 		return result;
 	}
 	
 	abstract protected Double runCalculation(Double...values) throws Exception;
 	
 	private synchronized Job updateJob(int reqId, int index, Double value) {
 		if (jobs.containsKey(reqId)) {
 			// update the job
 			Job current = jobs.get(reqId);
 			current.values[index] = value;
 			for (int i=0; i < current.max; i++)
 				if (current.values[i] == null)
 					return null;
 			// if all values are returned finish the job
 			jobs.remove(reqId);
 			return current;
 		}
 		return null;
 	}
 	
 	private synchronized int addJob(Job job) {
 		int id = jobId++;
 		jobs.put(id, job);
 		return id; 
 	}
 	
 }
