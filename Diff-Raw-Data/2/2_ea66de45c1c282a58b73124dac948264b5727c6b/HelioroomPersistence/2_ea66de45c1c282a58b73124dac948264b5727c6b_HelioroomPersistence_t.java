 /*
  * Created Nov 18, 2010
  */
 package ltg.ps.phenomena.helioroom.support;
 
 import ltg.ps.phenomena.helioroom.Helioroom;
 
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 /**
  * TODO Description
  *
  * @author Gugo
  */
 public class HelioroomPersistence extends Persistence {
 
 	private Helioroom p = null;
 
 	/**
 	 * @param fileName
 	 */
 	public HelioroomPersistence(String fileName) {
 		super(fileName);
 	}
 	
 	
 	public HelioroomPersistence(Helioroom p) {
 		this(p.getInstanceName());
 		this.p  = p;
 	}
 	
 
 	/* (non-Javadoc)
 	 * @see ltg.ps.api.Persistence#restore()
 	 */
 	@Override
 	public void restore() {
 		readFile();
 		Element config = doc.getRootElement().element("config");
 		Element wins = doc.getRootElement().element("windows");
		if(config!=null) {
 			p.configure(config.asXML());
 		}
 		if(wins!=null) {
 			p.configureWindows(wins.asXML());	
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see ltg.ps.api.Persistence#save()
 	 */
 	@Override
 	public void save() {
 		try {
 			doc = DocumentHelper.parseText(p.toXML());
 			writeFile();
 		} catch (DocumentException e) {
 			log.info("Impossible to save Helioroom.");
 		}
 		
 	}
 }
