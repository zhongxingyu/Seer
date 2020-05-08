 package danilofes.mes;
 
 import java.io.File;
 import java.sql.SQLException;
 
 import javax.xml.bind.JAXB;
 
 import danilofes.mes.db.dao.CloneResultDAO;
 import danilofes.mes.db.dao.DuplicationDAO;
 import danilofes.mes.db.dao.FragmentDAO;
 import danilofes.mes.db.entity.generic.GenericCloneResult;
 import danilofes.mes.db.entity.simian.SimianCloneResult;
 
 public class ParseFiles {
 
 	public static final String SIMIAN = "simian";
 	public static final String CPD = "cpd";
 	public static final String CLONEDIGGER = "clonedigger";
 	private static CloneResultDAO resultDao = new CloneResultDAO();
 	private static CloneReports reports = new CloneReports();
 
 	public static void main(String[] args) throws Exception {
 
 		/*
 		 */
 		// clearDb();
 		// createSimianResults();
 		// createCpdResults();
 		// createCloneDiggerResult();
 
		reports.printResultSummary();
 
 		// reports.duplicationLOCProportion();
 		reports.similarFragmentsCloneDetectionFound();
 	}
 
 	private static void createCloneDiggerResult() {
 		File cloneDiggerFile = new File("data/seed-clonedigger.xml");
 		GenericCloneResult cloneDiggerResult = JAXB.unmarshal(cloneDiggerFile, GenericCloneResult.class);
 		cloneDiggerResult.setAppName(CLONEDIGGER);
 		resultDao.create(cloneDiggerResult);
 		System.out.println("Clone digger file processed.");
 	}
 
 	private static void createCpdResults() {
 		File cpdfile = new File("data/seed-cpd.xml");
 		GenericCloneResult cpdResult = JAXB.unmarshal(cpdfile, GenericCloneResult.class);
 		cpdResult.setAppName(CPD);
 		resultDao.create(cpdResult);
 		System.out.println("CPD file processed.");
 	}
 
 	private static void createSimianResults() {
 		File simianfile = new File("data/seed-simian.xml");
 		SimianCloneResult simianResult = JAXB.unmarshal(simianfile, SimianCloneResult.class);
 		simianResult.setAppName(SIMIAN);
 		resultDao.create(simianResult);
 		System.out.println("Simian file processed.");
 	}
 
 	private static void clearDb() throws SQLException {
 		new FragmentDAO().clear();
 		new DuplicationDAO().clear();
 		new CloneResultDAO().clear();
 		System.out.println("DB clear");
 	}
 
 }
