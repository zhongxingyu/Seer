 package obr;
 
 import moca.graphs.vertices.Vertex;
 import moca.graphs.vertices.VertexBinaryFunction;
 import moca.graphs.Graph;
 import java.util.ArrayList;
 
 /**
  * Analyse a graph of rule dependencies to determine if it belongs to decidable class.<br />
  * Furthermore, the GRDAnalyser will check the strongly connected components of the GRD.
  * Then, it will check if a query will be answered in a finite time.
  */
 public class GRDAnalyser {
 
 	public abstract class ClassChecker {
 		public void unprocess() { _processed = false; }
 		public void process() { _processed = true; }
 		public abstract String diagnostic();
 		public boolean isProcessed() { return _processed; }
 		protected boolean _processed = false;
 	}
 
 	protected class DecidableClassChecker extends ClassChecker {
 	
 		public static final String UNPROCESSED_MSG = "Decidable class checks unprocessed!";
 
 		/**
 		 * Adds a new decidable class check function into the list.
 		 * If the function already exists, it will not be added.
 		 * @param checkFunction The new function to add.
 		 */
 		public void addDecidableClassCheck(DecidableClassCheck checkFunction) {
 			if (!_checkFunctions.contains(checkFunction))
 				_checkFunctions.add(checkFunction);
 		}
 		public boolean isFES(int sccID) {
 			if (!_processed)
 				return false;
 			ArrayList<DecidableClassLabel> labels = _sccLabels.get(sccID);
 			for (DecidableClassLabel label : labels)
 				if (label.isFES())
 					return true;
 			return false;
 		}
 		public boolean isGBTS(int sccID) {
 			if (!_processed)
 				return false;
 			ArrayList<DecidableClassLabel> labels = _sccLabels.get(sccID);
 			for (DecidableClassLabel label : labels)
 				if (label.isGBTS())
 					return true;
 			return false;
 		}
 		public boolean isFUS(int sccID) {
 			if (!_processed)
 				return false;
 			ArrayList<DecidableClassLabel> labels = _sccLabels.get(sccID);
 			for (DecidableClassLabel label : labels)
 				if (label.isFUS())
 					return true;
 			return false;
 		}
 		public boolean isFES() {
 			if (_grdLabels.size() > 0) {
 				for (int i = 0 ; (i < _grdLabels.size()) ; i++) {
 					if (_grdLabels.get(i).isFES())
 						return true;
 				}
 			}
 			return false;
 		}
 		public boolean isGBTS() {
 			if (_grdLabels.size() > 0) {
 				for (int i = 0 ; (i < _grdLabels.size()) ; i++) {
 					if (_grdLabels.get(i).isGBTS())
 						return true;
 				}
 			}
 			return false;
 		}
 		public boolean isFUS() {
 			if (_grdLabels.size() > 0) {
 				for (int i = 0 ; (i < _grdLabels.size()) ; i++) {
 					if (_grdLabels.get(i).isFUS())
 						return true;
 				}
 			}
 			return false;
 		}
 
 		/**
 		 * <p>Process all checks in a specific order :
 		 * <ul>
 		 * <li>checks if the graph is cyclic ;</li>
 		 * <li>if it is, checks all functions onto the complete graph of rule dependencies ;</li>
 		 * <li>and checks all functions onto each strongly connected component of the GRD.</li>
 		 * </p>
 		 */
 		@Override
 		public void process() {
 			// reset old data
 			if (_processed == true)
 				unprocess();
 			// init
 			_grdLabels = new ArrayList<DecidableClassLabel>();
 			_sccLabels = new ArrayList<ArrayList<DecidableClassLabel> >(_grd.getNbComponents());
 				// starts checking
 			if (_grd.isCyclic()) {
 				DecidableClassLabel l = null;
 				for (DecidableClassCheck function : _checkFunctions) {
 					try {
 						l = function.grdCheck(_grd);
 						if (l != null)
 							_grdLabels.add(l);
 					}
 					catch (UnsupportedOperationException e) { /* this check function cannot be used against the full grd */ }
 				}
 				for (int i = 0 ; i < _grd.getNbComponents() ; i++) {
 					_sccLabels.add(new ArrayList<DecidableClassLabel>());
 					for (DecidableClassCheck function : _checkFunctions) {
 						try {
 							l = function.sccCheck(_grd,i);
 							if (l != null)
 								_sccLabels.get(i).add(l);
 						}
 						catch (UnsupportedOperationException e) { /* ... against a single scc */ }
 					}
 				}
 			}
 			else
 				_grdLabels.add(AGRD_LABEL);
 			_processed = true;
 		}
 
 		@Override
 		public void unprocess() {
 			_grdLabels = null;
 			_sccLabels = null;
 			_processed = false;
 		}
 
 		@Override
 		public String diagnostic() {
 			if (!_processed)
 				return UNPROCESSED_MSG;
 
 			StringBuilder result = new StringBuilder();
 
 			// complete grd
 			result.append("Graph of Rule Dependencies labels :\n");
 			for (DecidableClassLabel label : _grdLabels) {
 				result.append("\t");
 				result.append(label);
 				result.append('\n');
 			}
 			result.append('\n');
 
 			// scc
 			if (_sccLabels.size() > 0)
 				result.append("Strongly Connected Components labels :\n");
 			for (int i = 0 ; i < _sccLabels.size() ; i++) {
 				result.append("C ");
 				result.append(i);
 				result.append('\t');
 				if (_sccLabels.get(i).size() > 0) {
 					for (int j = 0 ; j < _sccLabels.get(i).size() ; j++) {
 						result.append(_sccLabels.get(i).get(j));
 						result.append(" ; ");
 					}
 				}
 				else 
 					result.append("none");
 				result.append('\n');
 			}
 			
 			return result.toString();
 		}
 
 		/** Contains all determined decidable class labels for the GRD. */
 		private ArrayList<DecidableClassLabel> _grdLabels;
 		/** Contains all determined decidable class labels for each strongly connected component of the GRD. */
 		private ArrayList<ArrayList<DecidableClassLabel> > _sccLabels;
 
 		/** The check functions to be used. */
 		private ArrayList<DecidableClassCheck> _checkFunctions = new ArrayList<DecidableClassCheck>();
 
 	};
 	
 	protected class AbstractClassChecker extends ClassChecker
 				implements VertexBinaryFunction<ArrayList<Vertex<AtomicRule> > > {
 
 		public static final String UNPROCESSED_MSG = "Abstract class checks unprocessed!";
 
 		@Override
 		public void unprocess() {
 			_grdAbstractClass = 0;
 			_sccAbstractClass = null;
 			_processed = false;
 		}
 
 		@Override
 		public void process() { 
 			if (_processed)
 				unprocess();
 			_decidable = true;
 			if (isFES())
 				_grdAbstractClass = DecidableClassLabel.FES;
 			else if (isGBTS())
 				_grdAbstractClass = DecidableClassLabel.GBTS;
 			else if (isFUS())
 				_grdAbstractClass = DecidableClassLabel.FUS;
 			else
 				_decidable = false;
 			//if (!_decidable) {
 				_sccAbstractClass = new int[_grd.getNbComponents()];
 				processCombine();
 			//}
 			_processed = true;
 		}
 
 		protected void processCombine() {
 			ArrayList<Integer> sourceID = new ArrayList<Integer>();
 			final int nbComponents = _grd.getNbComponents();
 			int i,j;
 			boolean ended;
 			for (i = 0 ; i < nbComponents ; i++) {
 				ended = false;
 				for (j = 0 ; (j < nbComponents) && !ended ; j++) {
 					if ((i != j) && (_grd.getStronglyConnectedComponentsGraph().isEdge(j,i)))
 						ended = true;
 				}
 				if (j == nbComponents)
 					sourceID.add(new Integer(i));
 			}
 	
 			_decidable = true;
 			Graph.WalkIterator iterator;
			for (i = 0 ; (i < sourceID.size()) && _decidable ; i++)
 				setMin(sourceID.get(i),0);
			iterator = _grd.getStronglyConnectedComponentsGraph().BFSIterator(sourceID,this,null);
			while ((iterator.hasNext()) && _decidable)
				iterator.next();
 		}
 
 		@Override
 		public String diagnostic() {
 			if (!_processed)
 				return UNPROCESSED_MSG; 
 
 			if (!_decidable)
 				return "This set of rule cannot be used to make a query.";
 		
 			StringBuilder result = new StringBuilder();
 			if (_grdAbstractClass > 0) {
 				result.append("All the GRD rules belongs to the ");
 				result.append(DecidableClassLabel.longName(_grdAbstractClass));
 				result.append(" abstract class.\n");
 				result.append("But you can also use specific algorithms :\n\n");
 				result.append("GRD   \t");
 				result.append(DecidableClassLabel.longName(_grdAbstractClass));
 				result.append('\n');
 			}
 			else 
 				result.append("You must use different algorithms depending on the stongly connected component.\n\n");
 //			else {
 //				result.append("Strongly Connected Components Abstract Classes :\n");
 			for (int i = 0 ; i < _sccAbstractClass.length ; i++) {
 				result.append("SCC[");
 				result.append(i);
 				result.append("]\t");
 				result.append(DecidableClassLabel.longName(_sccAbstractClass[i]));
 				result.append('\n');
 //				}
 			}
 			return result.toString();
 		}
 		
 		/** 
 		 * Abstract class to "use" on the full GRD.
 		 * 0 means there is not only one abstract class which contains all rules.
 		 */
 		private int _grdAbstractClass = 0;
 		/** 
 		 * Contains the abstract class label to use on the specific strongly connected
 		 * component. 
 		 */
 		private int _sccAbstractClass[] = null; 
 		private boolean _decidable = false;
 
 		@Override
 		public void exec(Vertex<ArrayList<Vertex<AtomicRule> > > pred, 
 						 Vertex<ArrayList<Vertex<AtomicRule> > > next) {
 			final int sccPredID = pred.getID();
 			final int sccNextID = next.getID();
 			if (_sccAbstractClass[sccPredID] > _sccAbstractClass[sccNextID])
 				setMin(sccNextID,_sccAbstractClass[sccPredID]);
 		}
 		private void setMin(int sccNextID, int label) {
 			if (label <= DecidableClassLabel.FES) {
 				if (isFES(sccNextID)) {
 					_sccAbstractClass[sccNextID] = DecidableClassLabel.FES;
 					return;
 				}
 			}
 			if (label <= DecidableClassLabel.GBTS) {
 				if (isGBTS(sccNextID)) {
 					_sccAbstractClass[sccNextID] = DecidableClassLabel.GBTS;
 					return;
 				}
 			}
			if (label <= DecidableClassLabel.FUS) {
				if (isFUS(sccNextID)) {
					_sccAbstractClass[sccNextID] = DecidableClassLabel.FUS;
					return;
				}
			}
 			_decidable = false;
 			_sccAbstractClass[sccNextID] = 0;
 		}
 
 	};
 
 
 
 	public static final DecidableClassLabel AGRD_LABEL = new DecidableClassLabel("acyclic-grd",true,true,false);
 
 	/**
 	 * Constructor.
 	 * @param grd The graph of rule dependencies to be analysed.
 	 */
 	public GRDAnalyser(GraphRuleDependencies grd) {
 		_grd = grd;
 	}
 
 	public void setGRD(GraphRuleDependencies grd) {
 		_grd = grd;
 		_decidableChecker.unprocess();
 		_abstractChecker.unprocess();
 	}
 
 	public String diagnostic() {
 		StringBuilder result = new StringBuilder();
 		result.append("[a] DECIDABLE CLASS CHECKS\n\n");
 		result.append(_decidableChecker.diagnostic());
 		result.append("\n\n[b] ABSTRACT CLASS TO USE\n\n");
 		result.append(_abstractChecker.diagnostic());
 		return result.toString();
 	}
 
 	public void addDecidableClassCheck(DecidableClassCheck checkFunction) {
 		_decidableChecker.addDecidableClassCheck(checkFunction);
 	}
 
 	public void process() {
 		processDecidableClass();
 		processAbstractClass();
 	}
 
 	public void processDecidableClass() {
 		_decidableChecker.process();
 	}
 
 	public void processAbstractClass() {
 		_abstractChecker.process();
 	}
 
 	public boolean isFUS() {
 		return _decidableChecker.isFUS();
 	}
 
 	public boolean isFES() {
 		return _decidableChecker.isFES();
 	}
 	
 	public boolean isGBTS() {
 		return _decidableChecker.isGBTS();
 	}
 
 	public boolean isFUS(int sccID) {
 		return _decidableChecker.isFUS(sccID);
 	}
 
 	public boolean isFES(int sccID) {
 		return _decidableChecker.isFES(sccID);
 	}
 	
 	public boolean isGBTS(int sccID) {
 		return _decidableChecker.isGBTS(sccID);
 	}
 
 	/** The graph of rule dependencies to be analysed. */
 	private GraphRuleDependencies _grd;
 
 	private DecidableClassChecker _decidableChecker = new DecidableClassChecker();
 	private AbstractClassChecker _abstractChecker = new AbstractClassChecker();
 
 };
 
