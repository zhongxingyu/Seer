 package mdettlaff.mobilemachine.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import mdettlaff.mobilemachine.domain.SimplifiedWebpage;
 import mdettlaff.mobilemachine.repository.WebpageRepository;
 
 import org.apache.http.client.ClientProtocolException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class PageSimplifierService {
 
 	private static final int MAX_PAGE_SIZE_IN_BYTES = 10000;
 
 	private final WebpageRepository repository;
 	private final HttpService httpService;
 
 	@Autowired
 	public PageSimplifierService(WebpageRepository repository, HttpService httpService) {
 		this.repository = repository;
 		this.httpService = httpService;
 	}
 
 	public String simplify(String url, int pageNumber) throws ClientProtocolException, IOException {
 		SimplifiedWebpage webpage = repository.getByUrl(url);
 		if (webpage == null) {
 			webpage = simplify(url);
 		}
 		return webpage.getPage(pageNumber);
 	}
 
 	private SimplifiedWebpage simplify(String url) throws ClientProtocolException, IOException {
 		// TODO uncomment
 		String content = null;//httpService.download(url);
 		List<String> atoms = splitIntoAtoms(content);
 		List<String> pages = splitIntoPages(atoms);
 		return new SimplifiedWebpage(pages);
 	}
 
 	private List<String> splitIntoPages(List<String> atoms) {
 		List<String> pages = new ArrayList<String>();
 		StringBuilder currentPage = new StringBuilder();
 		for (String atom : atoms) {
 			if (currentPage.length() + atom.length() <= MAX_PAGE_SIZE_IN_BYTES) {
 				currentPage.append(atom);
 			} else {
 				pages.add(currentPage.toString());
 				currentPage = new StringBuilder(atom);
 			}
 		}
 		if (currentPage.length() > 0) {
 			pages.add(currentPage.toString());
 		}
 		return pages;
 	}
 
 	private List<String> splitIntoAtoms(String content) {
 		// TODO implement
 		return Arrays.asList("Hejka, <b>Zbyszek</b>");
 	}
 }
