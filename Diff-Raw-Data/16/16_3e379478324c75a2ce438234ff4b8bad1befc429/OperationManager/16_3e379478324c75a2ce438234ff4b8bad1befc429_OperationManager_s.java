 package net.bioclipse.brunn.business.operation;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import net.bioclipse.brunn.Springcontact;
 import net.bioclipse.brunn.business.LazyLoadingSessionHolder;
 import net.bioclipse.brunn.pojos.AuditType;
 import net.bioclipse.brunn.pojos.Folder;
 import net.bioclipse.brunn.pojos.Instrument;
 import net.bioclipse.brunn.pojos.Measurement;
 import net.bioclipse.brunn.pojos.Plate;
 import net.bioclipse.brunn.pojos.Result;
 import net.bioclipse.brunn.pojos.ResultType;
 import net.bioclipse.brunn.pojos.SampleContainer;
 import net.bioclipse.brunn.pojos.User;
 import net.bioclipse.brunn.pojos.Well;
 import net.bioclipse.brunn.pojos.WorkList;
 import net.bioclipse.brunn.results.PlateRead;
 import net.bioclipse.brunn.results.orcaParser.OrcaParser;
 import net.bioclipse.brunn.results.orcaParser.OrcaParser.OrcaPlateRead;
 
 public class OperationManager extends AbstractDAOBasedOperationManager
         implements IOperationManager {
 
 	public void addResult(User user, Measurement measurement, double[] result) {
 	    
 		int latestVersion = 0;
 		for (Result aResult : measurement.getResults()) {
 	        if(aResult.getVersion() > latestVersion) {
 	        	latestVersion = aResult.getVersion();
 	        }
         }
 		int nextVersion = latestVersion + 1;
 		
 		Result theResult = new Result(user, "result ver. " + nextVersion, result, nextVersion);
 	    measurement.getResults().add(theResult);
 	    
 	    measurement = measurementDAO.merge(measurement);
 	    measurementDAO.save(measurement);
 	    auditService.audit(user, AuditType.UPDATE_EVENT, measurement);
     }
 
 	public void delete(User user, Measurement measurement) {
 	    
 		auditService.audit(user, AuditType.DELETE_EVENT, measurement);
 	    measurementDAO.delete(measurement);
     }
 
 	public void delete(User user, Instrument instrument) {
 
 		auditService.audit(user, AuditType.DELETE_EVENT, instrument);
 		instrumentDAO.delete(instrument);
     }
 
 	public void delete(User user, ResultType resultType) {
 
 		auditService.audit(user, AuditType.DELETE_EVENT, resultType);
 		resultTypeDAO.delete(resultType);
     }
 
 	public void edit(User user, Measurement measurement) {
 	    
 		measurement = measurementDAO.merge(measurement);
 		auditService.audit(user, AuditType.UPDATE_EVENT, measurement);
 		measurementDAO.save(measurement);
     }
 
 	public void edit(User user, Instrument instrument) {
 
 		auditService.audit(user, AuditType.UPDATE_EVENT, instrument);
 		instrument = instrumentDAO.merge(instrument);
 		instrumentDAO.save(instrument);
     }
 
 	public void edit(User user, ResultType resultType) {
 	    
 		resultType = resultTypeDAO.merge(resultType);
 		resultTypeDAO.save(resultType);
 		auditService.audit(user, AuditType.UPDATE_EVENT, resultType);
     }
 
 	public Collection<Instrument> getAllInstruments() {
 	    
 		return instrumentDAO.findAll();
     }
 
 	public Collection<Measurement> getAllMeasurements() {
 
 		return measurementDAO.findAll();
     }
 
 	
 	public long createInstrument(User creator, String name) {
 	    
 		Instrument instrument = new Instrument(creator, name);
 		instrumentDAO.save(instrument);
 		auditService.audit(creator, AuditType.CREATE_EVENT, instrument);
 		return instrument.getId();
     }
 
 	public long createMeasurement( User creator, 
 	                               String name, 
 	                               WorkList workList, 
 	                               Instrument instrument, 
 	                               ResultType resultType ) {
 
 		workList = plateDAO.mergeObject(workList);
 		instrument = instrumentDAO.merge(instrument);
 		Measurement measurement = new Measurement( creator, name, workList, instrument, resultType );
 		measurementDAO.save(measurement);
 		instrument = instrumentDAO.merge(instrument);
 		SampleContainer sampleContainer = sampleContainerDAO.merge( workList.getSampleContainer() );
 		sampleContainerDAO.save(sampleContainer);
 		auditService.audit(creator, AuditType.CREATE_EVENT, measurement);
 	    return measurement.getId();
     }
 
 	public long createResultType( User creator, String name, int length ) {
 		
 		ResultType resultType = new ResultType(creator, name, length);
 		resultTypeDAO.save(resultType);
 		auditService.audit(creator, AuditType.CREATE_EVENT, resultType);
 	    return resultType.getId();
     }
 
 	public Collection<ResultType> getAllResultTypes() {
 
 	    return resultTypeDAO.findAll();
     }
 
 	public Instrument getInstrument(long id) {
 
 	    return instrumentDAO.getById(id);
     }
 
 	public ResultType getResultType(long id) {
 
 	    return resultTypeDAO.getById(id);
     }
 
 	public Measurement getMeasurement(long id) {
 
 	    return measurementDAO.getById(id);
     }
 
 	public List<ResultType> getResultTypeByName(String name) {
 		
 	    return resultTypeDAO.findByName(name);
     }
 
 	public List<Instrument> getInstrumentByName(String name) {
 
 	    return instrumentDAO.findByName(name);
     }
 
 	public void addResult(User activeUser, PlateRead pr, Plate plate) {
 		
 //		plate = plateDAO.merge(plate);
 		
 		System.out.println("OrcaParser.addResult() //adds the result from a plateread to a plate");
 		HashMap<String, Double> values = new HashMap<String, Double>();
 		
 		for (int i = 0; i < pr.getValues().length; i++) {
 	        for (int j = 0; j < pr.getValues()[i].length; j++) {
 	        	char row = (char)('a' + i);
 	        	int  col =  1  + j;
 	        	values.put( "" + row + col, pr.getValues()[i][j] );
 	        }
         }
 		int j =1;
 		
 		/*
 		 * get orcaResultType and if it doesn't exist create it
 		 */
 		ResultType orcaResultType;
 		String orcaResultTypeName = "orcaResultType";
 		List<ResultType> resultTypes = resultTypeDAO.findByName(orcaResultTypeName);
 		if(resultTypes.size() == 0) {
 			orcaResultType = new ResultType(activeUser, orcaResultTypeName, 1 );
 			resultTypeDAO.save(orcaResultType);
 		}
 		else {
 			orcaResultType = resultTypes.get(0);
 		}
 		
 		/*
 		 * get orcaInstrument and if it doesn't exist create it
 		 */
 		Instrument orca;
 		String orcaInstruementName = "ORCA";
 		List<Instrument> instruments = instrumentDAO.findByName(orcaInstruementName);
 		if(instruments.size() == 0) {
 			orca = new Instrument(activeUser, orcaInstruementName);
 			instrumentDAO.save(orca);
 		}
 		else {
 			orca = instruments.get(0);
 		}
 		
 		orca = instrumentDAO.merge(orca);
 		orcaResultType = resultTypeDAO.merge(orcaResultType);
 		
 		plate = plateDAO.merge(plate);
 		
 		for( Well well : plate.getWells() ) {
 			
 			double[] resultValue = new double[1];
 			Double i = values.get("" + well.getRow() + well.getCol());
 			resultValue[0] = (i == null) ? -1 : i;
 			Result result = new Result(activeUser, pr.getName(), resultValue, 1 );  //TODO: deal with version numbers
 
 			SampleContainer sampleContainer = well.getSampleContainer();
 			
 			WorkList w = sampleContainer.getWorkList();
 			Measurement measurement = new Measurement( activeUser,
                                                        pr.getName(), 
                                                        w, 
                                                        orca, 
                                                        orcaResultType );
 			Set s = w.getAbstractOperations();
 			s.add(measurement);
 			measurement.addResult(result);
 //			measurementDAO.save(measurement);
 //			sampleContainer = sampleContainerDAO.merge(sampleContainer);
 //			sampleContainerDAO.save(sampleContainer);
 			
			System.out.println("iteration: " + j++ + "of " + plate.getWells().size());
 		}
 		
 		plateDAO.save(plate);
     }
 }
