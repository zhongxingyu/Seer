 package fr.iutvalence.ubpe.ubpecommons.services;
 
 import java.io.File;
 import java.util.HashMap;
 
 import fr.iutvalence.json.AffTransform;
 import fr.iutvalence.json.ArrayFilter;
 
public class JsonFilteredFileProducerService extends AbstractTextFileTransformationPeriodicService
 {	
 	private int[] fieldsfilter;
 	
 	public JsonFilteredFileProducerService(long period, File inputFile, String charset, File outputfile, int[] fieldsFilter)
 	{		
 		super(period, inputFile, charset, outputfile);
 		this.fieldsfilter = fieldsFilter;
 	}
 	
 	/**
 	 * @see fr.iutvalence.ubpe.core.services.AbstractService#serve()
 	 */
 	public void serve()
 	{			
 		try
 		{		
 			ArrayFilter.jsonFilter(this.getInputFile(), this.getOutputFile(), this.fieldsfilter, new HashMap<Integer, AffTransform>());
 		}
 		// TODO add an error handler
 		catch (Exception e)
 		{
 			return;
 		}
 	}
 }
