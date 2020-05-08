 package alpv.mwp.crawler;
 
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.List;
 
 import javax.swing.text.BadLocationException;
 
 import alpv.mwp.Task;
 
 public class CrawlerTask implements Task<HttpURL, List<String>> {
 
 	private static final long serialVersionUID = 3659366838266519515L;
 	private CrawlerJob _job;
 
 	public CrawlerTask(CrawlerJob job) {
 		_job = job;
 	}
 
 	@Override
 	public List<String> exec(HttpURL a) {
 		try {
			URLParser parser = new URLParser(a.openConnection().getContent(), a);
 			parser.parse();
 			for (HttpURL url : parser.get_urls()){
 				_job.getArgPool().put(url);
 			}
 			return parser.get_mailTos();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
