 package no.met.wdb.netcdf;
 
 import java.util.Date;
 import java.util.TreeSet;
 
 import no.met.wdb.store.WdbIndex;
 import ucar.ma2.Array;
 import ucar.ma2.DataType;
 import ucar.nc2.Attribute;
 import ucar.nc2.Dimension;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.Variable;
 
 class ValidTimeHandler implements DataHandler {
 
 	private WdbIndex index;
 	private GlobalWdbConfiguration config;
 	private Array data = null;
 	
 	public static String cfName = "time";
 
 	
 	public ValidTimeHandler(WdbIndex index, GlobalWdbConfiguration config) {
 		this.index = index;
 		this.config = config;
 	}
 	
 	
 	@Override
 	public void addToNetcdfFile(NetcdfFile out) {
 		TreeSet<Date> referenceTimes = index.getAllReferenceTimes();
 		TreeSet<Long> validTimes = index.getAllValidtimes();
 
 		String timeShape = "";
 		if ( referenceTimes.size() > 1 )
			timeShape = TimeOffsetHandler.cfName + " " + ReferenceTimeHandler.cfName;
 		else {
 			int validTimeCount = validTimes.size();
 			if ( validTimeCount > 1 )
 				timeShape = cfName;
 			Dimension time = new Dimension(cfName, validTimes.size());
 			time.setUnlimited(true);
 			out.addDimension(null, time);
 		}
 
 		Variable var = new Variable(out, null, null, cfName, DataType.DOUBLE, timeShape);
 		var.addAttribute(new Attribute("units", "seconds since 1970-01-01 00:00:00 +00:00"));
 		var.addAttribute(new Attribute("long_name", "forecast (valid) time"));
 		var.addAttribute(new Attribute("standard_name", "time"));
 		var.addAttribute(new Attribute("axis", "T"));
 		
 		out.addVariable(null, var);
 	}
 
 	@Override
 	public Array getData(Variable variable) {
 		if ( data == null ) {
 			TreeSet<Date> referenceTimes = index.getAllReferenceTimes();
 			TreeSet<Long> validTimes = index.getAllValidtimes();
 			
 			
 			data = Array.factory(variable.getDataType(), variable.getShape());
 			
 //			if ( referenceTimes.size() > 1 && validTimes.size() < 1 ) {
 //				int[] shape = new int[2];
 //				shape[0] = referenceTimes.size();
 //				shape[1] = validTimes.size();
 //				data = Array.factory(DataType.DOUBLE, shape);
 //			}
 //			else if ( referenceTimes.size() > 1 ) {
 //				int[] shape = new int[1];
 //				shape[0] = referenceTimes.size();
 //				data = Array.factory(DataType.DOUBLE, shape);				
 //			}
 //			else {
 //				int[] shape = new int[1];
 //				shape[0] = validTimes.size();
 //				data = Array.factory(DataType.DOUBLE, shape);
 //			}
 			
 			int i = 0;
 			for ( Date d : referenceTimes )
 				for ( Long l :  validTimes )
 					data.setDouble(i ++, (d.getTime() / 1000) + l.longValue());
 		}
 		return data;
 	}
 
 	@Override
 	public boolean canHandle(Variable variable) {
 		return variable.getName() == cfName;
 	}
 
 	@Override
 	public String getCoordinatesAttributes(String cfName) {
 		String wdbName = config.wdbName(cfName);
 		if ( index.hasManyReferenceTimes(wdbName) && index.hasManyValidTimeOffsets(wdbName) )
 			return cfName;
 		return "";
 	}
 }
