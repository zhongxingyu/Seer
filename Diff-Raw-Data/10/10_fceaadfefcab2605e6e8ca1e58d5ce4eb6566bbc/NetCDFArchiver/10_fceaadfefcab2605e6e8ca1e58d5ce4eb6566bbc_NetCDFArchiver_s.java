 package gov.usgs.cida.data.grib;
 
 import java.io.File;
 import java.util.List;
 
 import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.ServiceActivator;
 
 public class NetCDFArchiver {
 	
    @ServiceActivator
	public void processFiles(
 			List<File> input,
 			@Header(value="outputFile", required=true) String filename,
 			@Header(value="config", required=true) ArchiveConfig cfg
 			) 
 		throws Exception 
 	{
 		
     	String outputDir = cfg.getOutput_dir();
     	File output = new File(outputDir,filename);
     	
     	RollingNetCDFArchive rnca = new RollingNetCDFArchive(output);
     	try { 
 	    	rnca.setExcludeList("dim_excludes", cfg.getDim_excludes());
 	    	rnca.setExcludeList("var_excludes", cfg.getVar_excludes());
 	    	rnca.setExcludeList("xy_excludes", cfg.getXy_excludes());
 	
 	    	rnca.setUnlimitedDimension(cfg.getUnlim_dim(), cfg.getUnlim_units());
 	    	
 	    	rnca.setGridVariables(cfg.getRenames());
 	    	rnca.setGridMapping("Latitude_Longitude");
 	    	
 	    	rnca.define(input.get(0));
 	    	
 	    	for (File f : input) {
 	    		rnca.addFile(f);
 	    	}
 	    	
     	} finally {
     		rnca.close();
     	}
 	}
     
 }
