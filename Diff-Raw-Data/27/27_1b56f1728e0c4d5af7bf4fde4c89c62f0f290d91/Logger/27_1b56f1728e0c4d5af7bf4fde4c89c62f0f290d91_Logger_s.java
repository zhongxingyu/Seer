 package nz.ac.vuw.ecs.rprofs.server;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import nz.ac.vuw.ecs.rprofs.Context;
 import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
 import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
 import nz.ac.vuw.ecs.rprofs.server.domain.id.RequestId;
 import org.eclipse.jetty.continuation.Continuation;
 import org.slf4j.LoggerFactory;
 
 @Singleton
 public class Logger extends HttpServlet {
 
 	private final org.slf4j.Logger log = LoggerFactory.getLogger(Logger.class);
 
 	private final DatasetManager datasets;
 	private final Workers workers;
 	private final Provider<RequestId> requests;
 
 	@Inject
 	Logger(DatasetManager datasets, Workers workers, Provider<RequestId> requests) {
 		this.datasets = datasets;
 		this.workers = workers;
 		this.requests = requests;
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		log.trace("receiving events");
 
 		String dataset = req.getHeader("Dataset");
 
 		Dataset ds = datasets.findDataset(dataset);
 		Context.setDataset(ds);
 
 		byte[] buffer = new byte[req.getContentLength()];
 		int read = 0;
 		InputStream in = req.getInputStream();
 		while (read < buffer.length) {
 			int r = in.read(buffer, read, buffer.length - read);
 			read += r;
 		}
 		log.trace("done reading: {} bytes", buffer.length);
 
 		Continuation worker;
 		try {
 			while (true) {
 				try {
 					worker = workers.getWorker();
 					while (true) {
 						if (worker.isExpired()) {
 							worker = workers.getWorker();
						} else break;
 					}
 
 					if (worker.getAttribute("RequestId") == null) {
 						worker.setAttribute("RequestId", requests.get());
 					}
 					worker.setAttribute("Dataset", dataset);
 					worker.setAttribute("Data", buffer);
 					worker.resume();
 
 					log.debug("sent {} bytes to worker ({})", buffer.length, dataset);
 					break;
 				} catch (IllegalStateException ex) {
 					log.error(ex.getMessage(), ex);
 				}
 			}
 		} catch (InterruptedException ex) {
 			throw new ServletException("interrupted while waiting for a worker", ex);
 		}
 
 		Context.clear();
 
 		resp.setStatus(201);
 		resp.setContentLength(0);
 		resp.getOutputStream().close();
 	}
 }
