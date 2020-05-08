 package info.beverlyshill.samples.model;
 
 import java.util.List;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import info.beverlyshill.samples.util.HibernateUtil;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 import info.beverlyshill.samples.model.Pages;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Manages database operations for the Pages table.
  * 
  * @author bhill2
  */
 public class PagesManager {
 	private static Log log = LogFactory.getLog(PagesManager.class);
 	private boolean description = false;
 	private String descriptionText = "";
 	private static String NAME = "Index";
 	private int totalPages = 0;
 	private static final String SAXPAGE = "dataDevelopmentSAX.htm";
 
 	/**
 	 * Returns list of all Pages records having name value of Index
 	 */
 	public List getPages() {
 		List PagesList = null;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try {
 			PagesList = session.createQuery(
 					"from Pages where name = 'Index' ORDER BY pageId").list();
 			//get initial count of pages
 			if(totalPages == 0) {
 				totalPages = PagesList.size();
 			}
 			session.getTransaction().commit();
 		} catch (HibernateException e) {
 			session.getTransaction().rollback();
 			log.error(e.getMessage());
 			throw e;
 		}
 		return PagesList;
 	}
 
 	/**
 	 * Gets Pages record with matching pageId
 	 * 
 	 * @param pageId
 	 *            is the page Id to search for
 	 * 
 	 * @return a Page object
 	 */
 	public Pages getPage(int pageId) {
 		Pages page = null;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try {
 			page = (Pages) session
 					.createQuery("from Pages" + " where pageId = ?")
 					.setInteger(0, pageId).uniqueResult();
 			session.getTransaction().commit();
 		} catch (HibernateException e) {
 			session.getTransaction().rollback();
 			log.error(e.getMessage());
 			throw e;
 		} finally {
 			session.close();
 		}
 		return page;
 	}
 
 	/**
 	 * Saves a Pages object to the database.
 	 * 
 	 * @param page
 	 *            is the Page object to save
 	 */
 	public void savePages(Pages page) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try {
 			session.saveOrUpdate(page);
 			session.getTransaction().commit();
 		} catch (HibernateException e) {
 			session.getTransaction().rollback();
			log.error("An error occurred in savePages: " + e.getMessage());
 			throw e;
 		} finally {
 			session.close();
 		}
 	}
 
 	/**
 	 * Deletes a Pages record with matching pageId
 	 * 
 	 * @param pageId
 	 *            is the Id of the Page record to delete
 	 */
 	public void deletePage(int pageId) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try {
 			session.delete(session.load(Pages.class, new Integer(pageId)));
 			session.flush();
 			session.getTransaction().commit();
 		} catch (HibernateException e) {
 			session.getTransaction().rollback();
 			log.error(e.getMessage());
 			throw e;
 		} finally {
 			session.close();
 		}
 	}
 	
 	/**
 	 * Reads data.xml file value and inserts value into the sample database
 	 */
 	public void readXML(Pages pages) {
 		try {
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser saxParser = factory.newSAXParser();
 			
 			DefaultHandler handler = new DefaultHandler() {
 				
 				public void startElement(String uri, String localName,String qName, 
 			                Attributes attributes) throws SAXException {
 					if (qName.equalsIgnoreCase("DESCRIPTION")) {
 						description = true;
 					}
 				}
 
 				public void endElement(String uri, String localName,
 					String qName) throws SAXException {
 					
 			 
 				}
 			 
 				public void characters(char ch[], int start, int length) throws SAXException {
 			 
 					if (description) {
 						descriptionText = new String(ch, start, length);
 						description = false;
 					}
 				}
 			};
 			saxParser.parse("../../Documents/beverlyshillsamples/samples/src/data.xml", handler);
 			List checkList = this.getPages();
 			if (checkList.size() <= totalPages) {
 				pages.setName(NAME);
 				pages.setTextDesc(descriptionText);
 				pages.setDetailPage(SAXPAGE);
 				savePages(pages);
 			}
 		}
 		catch(Exception e){
 			log.info("A SAX exception has occurred " + e.getMessage());
 		}
 	}
 }
