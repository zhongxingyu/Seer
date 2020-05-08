 /**
  * 
  */
 package test.dataportal.controllers;
 
 import java.util.ArrayList;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import org.dataportal.controllers.JPADownloadController;
 import org.dataportal.model.Download;
 import org.dataportal.model.DownloadItem;
 import org.dataportal.model.User;
 import org.dataportal.utils.Utils;
 
 import junit.framework.TestCase;
 
 /**
  * @author Micho Garcia
  * 
  */
 public class JPADownloadControllerTest extends TestCase {
 
 	private JPADownloadController controladorDescarga;
 	private User user = null;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		controladorDescarga = new JPADownloadController();
 		user = (User) exits("micho.garcia", User.class);
 	}
 
 	/**
 	 * Check if object exits into RDBMS and returns
 	 * 
 	 * @param object
 	 *            ID
 	 * @return object record or null
 	 */
 	@SuppressWarnings("unchecked")
 	private Object exits(String id, Class clase) {
 		EntityManager manager = getEntityManager();
 		Object objeto = manager.find(clase, id);
 		manager.close();
 		if (objeto != null)
 			return objeto;
 		else
 			return null;
 	}
 
 	/**
 	 * Create an EntityManager
 	 */
 	public EntityManager getEntityManager() {
 		EntityManagerFactory factoria = Persistence
 				.createEntityManagerFactory("dataportal");
 		EntityManager manager = factoria.createEntityManager();
 		return manager;
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.dataportal.controllers.JPADownloadController#insert(Download)}
 	 * .
 	 */
 	public void testInsert() {
 
 		String idDownload = "10.4324/4234";
 		Download download = new Download(idDownload,
 				"micho.garcia_20110509.zip",
 				Utils.extractDateSystemTimeStamp(), user);
 		boolean insertado = controladorDescarga.insert(download);
 		try {
			Thread.sleep(3000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		Download downloadInsertada = controladorDescarga.exits(download);
 		boolean insertada = false;
 		if (downloadInsertada != null)
 			insertada = true;
 
 		assertTrue(insertado);
 		assertTrue(insertada);
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.dataportal.controllers.JPADownloadController#insert(Download)}
 	 * .
 	 */
 	public void testInsertItems() {
 
 		String idDownload = "10.4324/4235";
 		Download download = new Download(idDownload,
 				"micho.garcia_20110509.zip",
 				Utils.extractDateSystemTimeStamp(), user);
 
 		ArrayList<DownloadItem> items = new ArrayList<DownloadItem>();
 		DownloadItem item1 = new DownloadItem("un nombre de archivo");
 		items.add(item1);
 		DownloadItem item2 = new DownloadItem("otro nombre de un archivo");
 		items.add(item2);
 		DownloadItem item3 = new DownloadItem("el ultimo nombre de archivo");
 		items.add(item3);
 
 		boolean insertado = controladorDescarga.insertItems(download, items);
 		assertTrue(insertado);
 
 		Download insertada = (Download) exits(idDownload, Download.class);
 		assertNotSame(insertada, null);
 
 		controladorDescarga.delete(insertada);
 
 		insertada = (Download) exits(idDownload, Download.class);
 		assertEquals(insertada, null);
 
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.dataportal.controllers.JPADownloadController#delete(Download)}
 	 * .
 	 */
 	public void testDelete() {
 		Download download = new Download("10.4324/4234");
 		Download downloadToRemove = controladorDescarga.exits(download);
 		boolean borrada = false;
 		if (downloadToRemove != null)
 			borrada = true;
 		controladorDescarga.delete(downloadToRemove);
 		downloadToRemove = controladorDescarga.exits(download);
 
 		assertTrue(borrada);
 		assertEquals(downloadToRemove, null);
 	}
 
 }
