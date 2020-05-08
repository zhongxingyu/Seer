 package mdettlaff.comics.web;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import mdettlaff.comics.domain.FileDownload;
 import mdettlaff.comics.service.ComicsService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class ComicsController {
 
 	private final ComicsService comicsService;
 
 	@Autowired
 	public ComicsController(ComicsService comicsService) {
 		this.comicsService = comicsService;
 	}
 
 	@RequestMapping("")
 	public ModelAndView home() {
 		return comic(1);
 	}
 
 	@RequestMapping("/comic/{index}")
 	public ModelAndView comic(@PathVariable("index") int index) {
 		List<FileDownload> images = comicsService.getImages();
 		List<String> errors = comicsService.getErrors();
 		Map<String, Object> model = createComicModel(index, images, errors);
 		return new ModelAndView("index", model);
 	}
 
 	private Map<String, Object> createComicModel(
 			int index, List<FileDownload> images, List<String> errors) {
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("comicIndex", index);
 		model.put("nextComicIndex", Math.min(index + 1, images.size()));
 		model.put("images", images);
 		model.put("errors", errors);
 		return model;
 	}
 
 	@RequestMapping(value = "/download", method = RequestMethod.POST)
 	public String download() {
 		comicsService.downloadComics();
 		return "download_finished";
 	}
 
 	@RequestMapping("/image/{index}")
 	public ResponseEntity<byte[]> image(@PathVariable("index") int index) {
 		FileDownload image = comicsService.getImages().get(index - 1);
 		HttpHeaders headers = new HttpHeaders();
 		headers.setContentType(image.getContentType().getMediaType());
		return new ResponseEntity<byte[]>(image.getContent(), headers, HttpStatus.OK);
 	}
 }
