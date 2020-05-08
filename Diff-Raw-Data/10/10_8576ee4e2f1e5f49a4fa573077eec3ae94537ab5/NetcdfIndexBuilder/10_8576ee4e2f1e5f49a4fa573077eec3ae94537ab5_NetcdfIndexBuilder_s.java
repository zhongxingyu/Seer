 package no.met.wdb.netcdf;
 
 import java.util.List;
 import java.util.Vector;
 
 import no.met.wdb.GridData;
 import no.met.wdb.store.IndexCreationException;
 import no.met.wdb.store.WdbIndex;
 import ucar.ma2.Array;
 import ucar.ma2.DataType;
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Range;
 import ucar.ma2.Section;
 import ucar.nc2.Attribute;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.Variable;
 
 class NetcdfIndexBuilder {
 
 	private WdbIndex index;
 	private GlobalWdbConfiguration config;
 	private Vector<DataHandler> dataHandlers = new Vector<DataHandler>();
 	
 	private GridHandler gridHandler;
 	
 	private void setupHandlers(Iterable<GridData> gridData) {
 		
 		gridHandler = new GridHandler(gridData);
 		
 		dataHandlers.add(gridHandler);
 		dataHandlers.add(new ReferenceTimeHandler(index));
		dataHandlers.add(new ValidTimeHandler(index, config));
 		dataHandlers.add(new TimeOffsetHandler(index));
 		dataHandlers.add(new VersionHandler(index));
 		dataHandlers.add(new LevelHandler(index, config));
 	}
 	
 	public NetcdfIndexBuilder(Iterable<GridData> gridData, GlobalWdbConfiguration config) throws IndexCreationException {
 
 		index = new WdbIndex(gridData);
 		this.config = config;
 
 		setupHandlers(gridData);
 	}
 	
 	/**
 	 * For testing - allows setting data to be worked on later
 	 */
 	NetcdfIndexBuilder(GlobalWdbConfiguration config) {
 		index = null;
 		this.config = config;
 	}
 	
 	/**
 	 * For testing, create and throw away a WdbIndex object.
 	 * 
 	 * @param gridData input to WdbIndex object creation
 	 * @param out the object to be populated
 	 */
 	void populate(List<GridData> gridData, NetcdfFile out) throws IndexCreationException {
 		index = new WdbIndex(gridData);
 		setupHandlers(gridData);
 		populate(out);
 	}
 	
 	
 	public void populate(NetcdfFile out) {
 
 		for ( Attribute attr : config.getGlobalAttributes() )
 			out.addAttribute(null, attr);
 		addDimensions(out);
 		addParameterVariables(out);
 		
 		out.finish();
 	}
 
 	public Array getMetadata(Variable v2, Section section) throws InvalidRangeException {
 		
 		//System.out.println(section.toString());
 		
 		Array ret = null;
 		for ( DataHandler handler : dataHandlers )
 			if ( handler.canHandle(v2) )
 				ret = handler.getData(v2);
 
 		if ( ret != null )
 			return ret.section(section.getRanges());
 		
 		return null;
 	}
 	
 	/**
 	 * Does the given variable name refer to a wdb parameter?
 	 */
 	public boolean isDatabaseField(String variableName) {
 		return index.hasParameter(config.wdbName(variableName));
 	}
 
 	public long[] getGridIdentifiers(Variable variable, Section section) {
 			
 		String parameter = config.wdbName(variable.getName());
 
 		 // Remove x/y dimensions
 		// and copy to a vector, to allow calling add(idx, ...) on it
 		List<Range> sectionRanges = section.getRanges(); 
 		Vector<Range> ranges = new Vector<Range>(
 				sectionRanges.subList(0, sectionRanges.size() -2));
 		
 		Range oneElementRange = new Range(1);
 		int idx = 0;
 		if ( ! index.hasManyReferenceTimes(parameter) )
 			ranges.add(idx, oneElementRange);
 		idx ++;
 		if ( ! index.hasManyValidTimeOffsets(parameter) )
 			ranges.add(idx, oneElementRange);
 		idx ++;
 		if ( ! index.hasManyLevels(parameter) )
 			ranges.add(idx, oneElementRange);
 		idx ++;
 		if ( ! index.hasManyVersions(parameter) )
 			ranges.add(idx, oneElementRange);
 			
 		if ( ranges.size() != 4 )
 			throw new RuntimeException("Internal error: Generated " + ranges.size() + " indices. Needed 4");
 		
 		
 		long[] ret = index.getData(parameter, ranges.get(0), ranges.get(1), ranges.get(2), ranges.get(3));
 		
 		return ret;
 	}
 
 
 	private String addToString(String base, String toAdd) {
 		if ( ! base.isEmpty() )
 			return base + " " + toAdd;
 		return toAdd;
 	}
 	
 	private String getDimensionList(String parameter) {
 
 		String ret = "";
 		
 		if ( index.hasManyReferenceTimes(parameter) )
 			ret = addToString(ret, ReferenceTimeHandler.cfName);
 		if ( index.hasManyValidTimeOffsets(parameter) )
 		{
 			if ( index.hasManyReferenceTimes(parameter) )
 				ret = addToString(ret, TimeOffsetHandler.cfName);
 			else
 				ret = addToString(ret, ValidTimeHandler.cfName);
 		}
 		if ( index.hasManyLevels(parameter) )
 			ret = addToString(ret, config.cfName(index.getLevelForParameter(parameter).getName()));
 		if ( index.hasManyVersions(parameter) )
 			ret = addToString(ret, VersionHandler.cfName);
 	
 		ret = addToString(ret, gridHandler.getYDimension());
 		ret = addToString(ret, gridHandler.getXDimension());
 		
 		return ret;
 	}
 
 	private void addDimensions(NetcdfFile out) {
 		for ( DataHandler handler : dataHandlers )
 			handler.addToNetcdfFile(out);
 	}
 	
 	private void addParameterVariables(NetcdfFile out) {
 
 		for ( String parameter : index.allParameters() ) {
 
 			String cfName = config.cfName(parameter);
 
 			Variable var = new Variable(out, null, null, cfName);
 			var.setDataType(DataType.FLOAT);
 
 			String dimensions = getDimensionList(parameter);
 			var.setDimensions(dimensions);
 			
 			for ( Attribute attr : config.getAttributes(parameter, index.unitForParameter(parameter)) )
 				var.addAttribute(attr);
 			var.addAttribute(new Attribute("FillValue", Float.NaN));
 
 			String coordinates = "";
 			for ( DataHandler handler : dataHandlers ) {
 				String coordinateAddition = handler.getCoordinatesAttributes(cfName);
 				if ( coordinateAddition != null && ! coordinateAddition.isEmpty()) {
 					if ( ! coordinates.isEmpty() )
 						coordinates += " ";
 					coordinates += coordinateAddition;
 				}
 			}
 			if ( ! coordinates.isEmpty() )
 				var.addAttribute(new Attribute("coordinates", coordinates));
 			
 //		   setAttribute(attributes, "grid_mapping", gridInfo->getProjectionName());
 
 			out.addVariable(null, var);
 		}
 	}
 }
