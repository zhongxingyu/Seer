 package DWLProject;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 
 import model.modeling.IODevs;
 import view.modeling.ViewableAtomic;
 
 /**
  * LoaderManager.java
  * 
  * This class manages creation of the loaders as well as 
  * adding them to the system
  * 
  * @author Ivan C
  *
  */
 public class LoaderManager {
 	
 	private static final String LOAD = "load";
 	private static final String LOADER = "Loader_1_";
 
 	private LoaderManager() {
 	}
 
 	/**
 	 * Adds the <code>numberOfLoaders</code> of type <code>Loader_0_0</code> to the
 	 * system using the <code>Coord_0_0</code> as the link up to the <tt>coupled
 	 * parent model.</tt>
 	 * 
 	 * @param numberOfLoaders
 	 * @param coord
 	 * @return
 	 */
 	public static List<Loader_0_0> addLoadersToSystem(int numberOfLoaders, ViewableAtomic coord) {
 		List<Loader_0_0> theLoaders = createLoaders(numberOfLoaders);
 		for(Loader_0_0 loader : theLoaders) {
 			addLoaderToSystem(loader, coord);
		}
 		return theLoaders;
 	}
 	/**
 	 * Return a <code>List</code> of <code>Loader_0_0</code>
 	 * 
 	 * @param numberOfLoaders
 	 * @return list of loaders
 	 */
 	private static List<Loader_0_0> createLoaders(int numberOfLoaders) {
 		List<Loader_0_0> theList = new ArrayList<Loader_0_0>(numberOfLoaders);
 		for (int i=0 ; i < numberOfLoaders; i++) {
 			Loader_0_0 aLoader = new Loader_0_0(LOADER + (i+1));
 			aLoader.setPreferredLocation(new Point(100, 80));
 			theList.add(aLoader);
 		}
 		return theList;
 	}
 	
 	/**
 	 * Adds the <code>Loader_0_0</code> to the system using the 
 	 * <code>Coord_0_0</code> as link to the parent <tt>coupled model</tt>.
 	 * It also creates the appropriate couplings
 	 * 
 	 * @param aLoader
 	 * @param coordinator
 	 */
 	private static void addLoaderToSystem(Loader_0_0 aLoader, ViewableAtomic coordinator) {
 		IODevs aDevs = coordinator.getParent();
 		if (aDevs instanceof DWL_1_1) {
 			DWL_1_1 theParent = (DWL_1_1) aDevs; 
 			coordinator.addModel(aLoader);
 			coordinator.addCoupling(coordinator.getName(), Coord_0_0.getCatOut(), aLoader.getName(), Loader_0_0.getCatIn());
 			coordinator.addCoupling(aLoader.getName(), Loader_0_0.getDone(), coordinator.getName(), Coord_0_0.getLdrDone());
 			coordinator.addCoupling(aLoader.getName(), Loader_0_0.getExtCatOut(), theParent.getName(), LOAD);
 		}
 	} 
 
 }
