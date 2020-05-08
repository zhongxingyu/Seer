 package org.geworkbench.analysis;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.model.analysis.ParamValidationResults;
 
 /**
  * Analyses that have a corresponding (ca)Grid component should extend this
  * abstract class to have the grid analyses exposed.
  * 
  * @author keshav
  * @version $Id$
  */
 public abstract class AbstractGridAnalysis extends AbstractAnalysis {
 	private static final long serialVersionUID = -7310721066701534971L;
 	private Log log = LogFactory.getLog(AbstractGridAnalysis.class);
 
 	/**
 	 * Analyses extending this class will implement this method, setting the
 	 * name of the service. This name should be equivalent to one of the names
 	 * in the "grid services cache". See {@link GridServiceRunner}.
 	 * 
 	 * @return String
 	 */
 	public abstract String getAnalysisName();
 
 	/**
 	 * 
 	 * @return
 	 */
 	public abstract Class<?> getBisonReturnType();
 
 	/**
 	 * Some grid analyses use microarray set views as inputs, some don't.
 	 * 
 	 * @return
 	 */
 	protected abstract boolean useMicroarraySetView();
 
 	/**
 	 * Some grid analyses use "other" data sets (ie. EdgeList) as inputs, some
 	 * don't.
 	 * 
 	 * @return
 	 */
 	protected abstract boolean useOtherDataSet();
 
 	/**
 	 * This method should be implmented to obtain the user input (which is
 	 * stored as BISON parameters), convert it to caGrid parameters, and return
 	 * a map with caGrid parameter values keyed by parameter names.
 	 * 
 	 * @return
 	 */
 	protected abstract Map<Serializable, Serializable> getBisonParameters();
 
 	/**
 	 * Packs the bison inputs (including parameters) for the grid service.
 	 * Services will take in a {@link DSMicroarraySetView} and/or a
 	 * {@link DSDataSet}.
 	 * 
 	 * @param microarraySetView
 	 * @param otherDataset
 	 * @return {@link List} - A list of service inputs.
 	 */
 	public List<Serializable> handleBisonInputs(
 			DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySetView, DSDataSet<?> otherDataset) {
 
 		List<Serializable> serviceParameterList = new ArrayList<Serializable>();
 
 		if (useMicroarraySetView()) {
 			serviceParameterList.add(microarraySetView);
 		}
 		if (useOtherDataSet()) {
 			if (otherDataset != null) {
 				serviceParameterList.add(otherDataset);
 			} else {
 				log
 						.error("Was told to use the other dataset but cannot.  This is null.");
 			}
 		}
 
 		serviceParameterList.add((Serializable) getBisonParameters());
 
 		return serviceParameterList;
 	}
 	
 	public abstract ParamValidationResults validInputData(DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView, DSDataSet<?> refMASet);
 	
 	/**
 	 * Authorization is required unless the sub-class overwrite this.
 	 * @return true;
 	 */
 	public boolean isAuthorizationRequired() {
 		return true;
 	}
	
	// this has to be non-static method so to be able to override
	@SuppressWarnings("unchecked")
	public DSAncillaryDataSet<? extends DSBioObject> postProcessResult(Object object) {
		// default assuming DSAncillaryDataSet
		if(!(object instanceof DSAncillaryDataSet)) return null;
		return (DSAncillaryDataSet<? extends DSBioObject>) object;
	}
 }
