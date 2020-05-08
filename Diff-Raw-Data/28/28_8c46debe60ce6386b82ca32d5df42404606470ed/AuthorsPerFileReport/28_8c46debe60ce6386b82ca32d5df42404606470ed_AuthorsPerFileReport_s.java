 /*
 * statcvs-xml
 * TODO
 * Created on 24.06.2003
 *
 */
 package net.sf.statcvs.output.xml.report;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import net.sf.statcvs.I18n;
 import net.sf.statcvs.model.Author;
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.model.CvsFile;
 import net.sf.statcvs.model.FilesRevisionCountComparator;
 import net.sf.statcvs.output.ConfigurationOptions;
 import net.sf.statcvs.output.WebRepositoryIntegration;
 
 import org.jdom.Element;
 
 /**
  * AuthorsPerFileReport
  * 
  * @author Tammo van Lessen
 * @version $id: $
  */
 public class AuthorsPerFileReport extends ReportElement {
 
 	public static final int MAX_ITEMS = 20;
 	private CvsContent content;
 	
 	/**
 	 * 
 	 */
 	public AuthorsPerFileReport(CvsContent content) {
 		//super(I18n.tr("Authors per File (TOP {0})", new Integer(MAX_ITEMS)));
 		super(I18n.tr("Authors per File"));
 		this.content = content;
 		createReport();		
 	}
 
 	private void createReport() {
 		//TODO: Sorting and limitting to MAX_ITEMS!!
 		Element filesEl = new Element("files");
 		
 		List files = content.getFiles();
 		Collections.sort(files, new FilesRevisionCountComparator());
 		Iterator it = files.iterator();
 		while (it.hasNext()) {
 			CvsFile file = (CvsFile) it.next();
 			if (file.isBinary() || file.isDead()) {
 				continue;
 			}
 
 			Element fileEl = new Element("file");
 			
 			WebRepositoryIntegration webRepository = ConfigurationOptions.getWebRepository();
 			fileEl.setAttribute("name", file.getFilenameWithPath());
 			
 			if (webRepository != null) {
 				fileEl.setAttribute("url", webRepository.getFileViewUrl(file));				
 			}
 
 			int authorsCount = 0;
 			Iterator authors = content.getAuthors().iterator();
 			while (authors.hasNext()) {
 				Author author = (Author) authors.next();
 				if (file.hasAuthor(author)) {
 					authorsCount++;
 				}
 			}
 			fileEl.setAttribute("authors", ""+authorsCount);
 			filesEl.addContent(fileEl); 
 		}
 		addContent(new Element("authorsPerFile").addContent(filesEl));
 	}
 
 	private class AuthorsPerFileComparator implements Comparator {
 
 		/**
 		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
 		 */
 		public int compare(Object o1, Object o2) {
 			int e1 = Integer.parseInt(((Element)o1).getAttributeValue("authors"));
 			int e2 = Integer.parseInt(((Element)o2).getAttributeValue("authors"));
 			return e1 - e2;
 		}
 	}
 }
