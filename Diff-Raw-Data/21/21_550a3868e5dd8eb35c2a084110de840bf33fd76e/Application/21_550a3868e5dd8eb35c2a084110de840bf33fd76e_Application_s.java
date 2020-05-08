 package controllers;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.globalsight.tip.InMemoryBackingStore;
 import com.globalsight.tip.TIPP;
 import com.globalsight.tip.TIPPFactory;
 import com.globalsight.tip.TIPPLoadStatus;
 
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import play.mvc.*;
 
 import views.html.*;
 
 public class Application extends Controller {
 	
     public static Result index() {
         return ok(index.render());
     }
   
     public static Result validate() {
     	MultipartFormData body = request().body().asMultipartFormData();
     	FilePart file = body.getFile("tippupload");
     	if (file == null) {
     		flash("error", "Missing file");
     		return redirect(routes.Application.index());
     	}
     	String fileName = file.getFilename();
     	try {
 	    	TIPPLoadStatus loadStatus = new TIPPLoadStatus();
 	    	InputStream is = new BufferedInputStream(new FileInputStream(file.getFile()));
 	    	TIPP tipp = TIPPFactory.openFromStream(is, new InMemoryBackingStore(), loadStatus);
 	    	is.close();
 	    	switch (loadStatus.getSeverity()) {
 	    	case NONE:
 	    		return ok(valid.render(fileName, tipp, loadStatus));
 	    	case WARN:
 	    		return ok(warn.render(fileName, tipp, loadStatus));
 	    	case ERROR:
 	    		return ok(error.render(fileName, tipp, loadStatus));
 	    	case FATAL:
 	    		return ok(fatal.render(fileName, tipp, loadStatus));
 	    	}
 	    	return TODO; // XXX Shouldn't ever happen
     	}
     	catch (IOException e) {
     		flash("error", "Error reading file: " + e.getMessage());
     		return redirect(routes.Application.index());
     	}
     }
 }
