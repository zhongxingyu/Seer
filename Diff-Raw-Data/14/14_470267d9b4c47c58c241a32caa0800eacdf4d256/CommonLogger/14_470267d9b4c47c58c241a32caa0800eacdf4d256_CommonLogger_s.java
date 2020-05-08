 package net.cheney.motown.server.middleware;
 
 import static java.lang.String.format;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import org.apache.commons.lang.time.FastDateFormat;
 
 import net.cheney.motown.common.api.Response;
 import net.cheney.motown.server.api.Application;
 import net.cheney.motown.server.api.Environment;
 
 public class CommonLogger implements Application {
 
 	private final Application app;
 	private final Writer logger;
 	
	private final String NCSA_FORMAT = "%s - %s [%s] \"%s %s%s %s\" %d %s %0.4f\n";
	private final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("%d/%b/%Y %H:%M:%S");
 
 	public CommonLogger(Application app, Writer logger) {
 		this.app = app;
 		this.logger = logger;
 	}
 	
 	@Override
 	public Response call(Environment env) {
 		long begin = System.currentTimeMillis();
 		Response response = app.call(env);
 		log(env, response, begin);
 		return response;
 	}
 
 	private void log(Environment env, Response response, long begin) {
 		long finish = System.currentTimeMillis();
 		long contentLength = getContentLength(response);
 		String line = format(NCSA_FORMAT, 
 				"-", // remote addr
 				"-", // user
 				DATE_FORMAT.format(finish),
 				env.method(),
 				env.uri(),
 				"", // query string
 				env.version(),
 				response.status().code(),
 				contentLength,
 				finish - begin
 			);
 		log(line);
 	}
 
 	private void log(String line) {
 		try {
 			logger.append(line);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private long getContentLength(Response response) {
 		try {
 			return response.contentLength();
 		} catch (IOException e) {
 			return -1;
 		}
 	}
 
 }
