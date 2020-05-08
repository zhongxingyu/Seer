 package net.metadata.auselit.lorestore.servlet;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.servlet.http.HttpServletResponse;
 
 import net.metadata.auselit.lorestore.exceptions.InvalidQueryParametersException;
 import net.metadata.auselit.lorestore.exceptions.NotFoundException;
 import net.metadata.auselit.lorestore.exceptions.OREException;
 
 import org.apache.log4j.Logger;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 import au.edu.diasb.chico.mvc.RequestFailureException;
 
 @Controller
 public class OREController {
 	private final Logger LOG = Logger.getLogger(OREController.class);
 
 	private final OREControllerConfig occ;
 	private OREQueryHandler qh;
 	private OREUpdateHandler uh;
 
 	public OREController(OREControllerConfig occ) {
 		this.occ = occ;
 		this.qh = new OREQueryHandler(occ);
 		this.uh = new OREUpdateHandler(occ);
 	}
 
 	@RequestMapping(value = "/", method = RequestMethod.POST)
 	public OREResponse post(InputStream inputRDF)
 			throws RequestFailureException, IOException, OREException,
 			InterruptedException {
 
 		return uh.post(inputRDF);
 	}
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
 	public OREResponse get(@PathVariable("id") String oreId)
 			throws NotFoundException, InterruptedException,
 			RequestFailureException {
 		return qh.getOreObject(oreId);
 	}
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
 	public OREResponse put(@PathVariable("id") String oreId, InputStream in)
 			throws RequestFailureException, IOException, OREException,
 			InterruptedException {
 		return uh.put(oreId, in);
 	}
 
 	@RequestMapping(value = "/", params = "refersTo", method = RequestMethod.GET)
 	public ResponseEntity<String> refersToQuery(
 			@RequestParam("refersTo") String urlParam) throws Exception {
 		if (urlParam == null || urlParam.isEmpty()) {
 			throw new InvalidQueryParametersException(
 					"Missing or empty query parameters");
 		}
 
 		return qh.browseQuery(urlParam);
 	}
 
 	@RequestMapping(value = "/", params = { "refersTo", "matchpred", "matchval" }, method = RequestMethod.GET)
 	public ResponseEntity<String> searchQuery(
 			@RequestParam("refersTo") String urlParam,
 			@RequestParam("matchpred") String matchpred,
 			@RequestParam("matchval") String matchval) throws Exception {
 		if (urlParam == null || urlParam.isEmpty()) {
 			throw new InvalidQueryParametersException(
 					"Missing or empty query parameters");
 		}
 
 		return qh.searchQuery(urlParam, matchpred, matchval);
 	}
 
 	@RequestMapping(value = "/", params = { "matchval" }, method = RequestMethod.GET)
 	public ResponseEntity<String> keywordSearch(
 			@RequestParam("matchval") String matchval) throws Exception {
 		if (matchval == null || matchval.isEmpty()) {
 			throw new InvalidQueryParametersException(
 					"Missing or empty query parameters");
 		}
 
 		return qh.searchQuery(null, null, matchval);
 	}
 
 	@RequestMapping(value = "/", params = "exploreFrom", method = RequestMethod.GET)
 	public ResponseEntity<String> exploreQuery(
 			@RequestParam("exploreFrom") String urlParam) throws Exception {
 		if (urlParam == null || urlParam.isEmpty()) {
 			throw new InvalidQueryParametersException(
 					"Missing or empty query parameters");
 		}
 
 		return qh.exploreQuery(urlParam);
 	}
 
 	@RequestMapping(value = "/rss", params = "refersTo", method = RequestMethod.GET)
 	public ModelAndView rssRefersToQuery(
 			@RequestParam("refersTo") String urlParam) throws Exception {
 		if (urlParam == null || urlParam.isEmpty()) {
 			throw new InvalidQueryParametersException(
 					"Missing or empty query parameters");
 		}
 
 		return qh.browseRSSQuery(urlParam);
 	}
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<String> delete(@PathVariable("id") String oreId)
 			throws NotFoundException, InterruptedException {
 		uh.delete(oreId);
		return new ResponseEntity<String>("", HttpStatus.NO_CONTENT);
 	}
 
 	@ResponseStatus(HttpStatus.NOT_FOUND)
 	@ExceptionHandler({ RequestFailureException.class, NotFoundException.class })
 	public void return404(Exception ex, HttpServletResponse response)
 			throws IOException {
 		LOG.debug("Handling Exception, returning 404" + ex);
 		response.sendError(HttpStatus.NOT_FOUND.value());
 	}
 
 	public OREControllerConfig getControllerConfig() {
 		return occ;
 	}
 }
