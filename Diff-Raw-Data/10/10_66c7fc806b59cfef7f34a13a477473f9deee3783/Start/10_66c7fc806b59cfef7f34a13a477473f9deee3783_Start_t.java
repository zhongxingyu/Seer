 package nz.ac.vuw.ecs.rprofs.server;
 
 import com.google.common.annotations.VisibleForTesting;
 import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
 import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowire;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 @Configurable(autowire = Autowire.BY_TYPE)
 public class Start extends HttpServlet {
 
 	private final org.slf4j.Logger log = LoggerFactory.getLogger(Start.class);
 
 	@VisibleForTesting
 	@Autowired
 	DatasetManager datasets;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		Dataset dataset = datasets.createDataset();
 
 		log.info("profiler run started");
 
		log.debug("waiting 60 seconds to ensure a reasonable timeout available");

		try {
			Thread.sleep(60000l);
		} catch (InterruptedException ex) {
			throw new ServletException("interrupted");
		}

		log.debug("done waiting");

 		resp.addHeader("Dataset", dataset.getHandle());
 		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
 	}
 
 }
