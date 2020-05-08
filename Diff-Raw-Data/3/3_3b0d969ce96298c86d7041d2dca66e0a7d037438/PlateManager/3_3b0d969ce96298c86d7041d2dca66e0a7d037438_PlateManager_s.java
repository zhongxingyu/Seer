 package net.bioclipse.brunn.business.plate;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import net.bioclipse.brunn.business.IAuditService;
 import net.bioclipse.brunn.business.LazyLoadingSessionHolder;
 import net.bioclipse.brunn.pojos.AbstractBasePlate;
 import net.bioclipse.brunn.pojos.AbstractPlate;
 import net.bioclipse.brunn.pojos.AbstractSample;
 import net.bioclipse.brunn.pojos.AuditType;
 import net.bioclipse.brunn.pojos.CellOrigin;
 import net.bioclipse.brunn.pojos.CellSample;
 import net.bioclipse.brunn.pojos.DrugSample;
 import net.bioclipse.brunn.pojos.Folder;
 import net.bioclipse.brunn.pojos.MasterPlate;
 import net.bioclipse.brunn.pojos.PatientOrigin;
 import net.bioclipse.brunn.pojos.PatientSample;
 import net.bioclipse.brunn.pojos.Plate;
 import net.bioclipse.brunn.pojos.PlateFunction;
 import net.bioclipse.brunn.pojos.PlateLayout;
 import net.bioclipse.brunn.pojos.SampleContainer;
 import net.bioclipse.brunn.pojos.User;
 import net.bioclipse.brunn.pojos.Well;
 import net.bioclipse.brunn.pojos.WellFunction;
 import net.bioclipse.brunn.results.PlateResults;
 import net.bioclipse.brunn.results.ResultParser;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 
 /**
  * This class handles everything that has to do with the Plate classes:
  *  Plate
  *  Well
  *  MasterPlate 
  *  
  * It should be instantiated as a Spring bean for everything to work.
  *
  * @author jonathan
  *
  */
 public class PlateManager extends
 		AbstractDAOBasedPlateManager {
 
 	public PlateManager() {
 	    super();
     }
 
 	public PlateManager(IAuditService auditService) {
 	    super();
 	    this.auditService = auditService;
     }
 
 	public Collection<Plate> getAllPlates() {
 	    return plateDAO.findAll();
     }
 
 	public Plate getPlate(long plateId) {
 	    return this.plateDAO.getById(plateId);
     }
 
 	public long createPlate( User creator, 
 	                         String name, 
 	                         String barcode, 
 	                         Folder folder, 
 	                         MasterPlate masterPlate, 
 	                         CellOrigin cellOrigin, 
 	                         Timestamp defrostingDate ) {
 	
 		masterPlate = masterPlateDAO.merge(masterPlate);
 		folder = folderDAO.merge(folder);
 		
 		Plate plate = AbstractPlate.createPlate( creator, 
 												 name, 
 												 masterPlate, 
 												 barcode,
 												 folder );
 		folder.getObjects().add(plate);
 
 		for( Well well : plate.getWells() ) {
 			
 			SampleContainer container = well.getSampleContainer();
 			for( AbstractSample as : container.getSamples() ) {
 				if(as instanceof DrugSample) {
 					drugSampleDAO.save( (DrugSample)as );
 				}
 				else {
 					throw new IllegalArgumentException("There is not supposed to be a " + as + " here");
 				}
 			}
 			
 			CellSample cellSample = new CellSample( creator, 
 					                                cellOrigin.getName(), 
 					                                cellOrigin, 
 					                                defrostingDate, 
 					                                container );
 			cellSampleDAO.save(cellSample);
 			container.getSamples().add(cellSample);
 			
 			sampleContainerDAO.save(container);
 			
 			getAuditService().audit(creator, AuditType.CREATE_EVENT, cellSample);
 		}
 		plateDAO.save(plate);
 //		folder = folderDAO.merge(folder);
 		folderDAO.save(folder);
 		
 		getAuditService().audit(creator, AuditType.CREATE_EVENT, plate);
 		
 	    return plate.getId();
     }
 	
 	public long createMasterPlate( User creator, 
 	                               String name, 
 	                               PlateLayout plateLayout, 
 	                               Folder folder, 
 	                               int numOfPlates ) {
 	    
 		folder  = folderDAO.merge(folder);
 		creator = userDAO.merge(creator);
 
 		MasterPlate masterPlate = 
 			AbstractPlate.createMasterPlate( creator, 
 				                             name, 
 				                             plateLayout,
 				                             folder, 
 				                             numOfPlates );
 		
 		for ( PlateFunction pf : masterPlate.getPlateFunctions() ) {
 			//TODO FIXME: This is a terrible hack but since the creator of 
 			//            a platefunction is not used for anything it works.
 			//            However unless this is fixed it can never be used 
 			//            for anything.
 			pf.setCreator(null); 
 		}
 		
 		masterPlateDAO.save(masterPlate);
 		folderDAO.save(folder);
 		
 		getAuditService().audit(creator, AuditType.CREATE_EVENT, masterPlate);
 		evictfromLazyLoading(masterPlate);
 		evictfromLazyLoading(plateLayout);
 	    return masterPlate.getId();
     }
 	
 	public long createPlateFunction(User creator, String name, Plate plate, String expression, double goodFrom, double goodTo) {
 	    
 		plate = plateDAO.merge(plate);
 		PlateFunction plateFunction = new PlateFunction(creator, name, expression, goodFrom, goodTo, true,true, plate);
 	    plate.getPlateFunctions().add(plateFunction);
 	    
 	    getAuditService().audit(creator, AuditType.UPDATE_EVENT, plate);
 	    plateDAO.save(plate);
 	    return plateFunction.getId();
     }
 	
 	public long createPlateFunction(User creator, String name, MasterPlate plate, String expression, double goodFrom, double goodTo) {
 	    
 	    plate = masterPlateDAO.merge(plate);
 		
 		PlateFunction plateFunction = new PlateFunction(creator, name, expression, goodFrom, goodTo, true,true, plate);
 	    plate.getPlateFunctions().add(plateFunction);
 	    
 	    getAuditService().audit(creator, AuditType.UPDATE_EVENT, plate);
 
 	    masterPlateDAO.save(plate);
 	    return plateFunction.getId();
     }
 
 	public long createPlateFunction(User creator, String name, Plate plate, String expression) {
 	    
 		
 		PlateFunction plateFunction = new PlateFunction(creator, name, expression, 0, 0, false,false, plate);
 		plate.getPlateFunctions().add(plateFunction);
 		
 		getAuditService().audit(creator, AuditType.UPDATE_EVENT, plate);
 		plate = plateDAO.merge(plate);
 		plateDAO.save(plate);
 	    return plateFunction.getId();
     }
 	
 	public long createPlateFunction(User creator, String name, MasterPlate plate, String expression) {
 	    
 		masterPlateDAO.merge(plate);
 		PlateFunction plateFunction = new PlateFunction(creator, name, expression, 0, 0, false,false, plate);
 		plate.getPlateFunctions().add(plateFunction);
 		
 		getAuditService().audit(creator, AuditType.UPDATE_EVENT, plate);
 		
 		masterPlateDAO.save(plate);
 	    return plateFunction.getId();
     }
 
 	public long createWellFunction(User creator, String name, Well well, String expression) {
 	    
 		//TODO check whether necessary:
 		//well = wellDAO.getByid( well.getID() );
 		
 		WellFunction wellFunction = new WellFunction(creator, name, expression, well);
 		well.getWellFunctions().add(wellFunction);
 
 		getAuditService().audit(creator, AuditType.UPDATE_EVENT, well.getPlate());
 		
 		well = wellDAO.merge(well);
 		wellDAO.save(well);
 		
 	    return wellFunction.getId();
     }
 
 	public Collection<MasterPlate> getAllMasterPlates() {
 
 		return masterPlateDAO.findAll();
     }
 
 	public MasterPlate getMasterPlate(long id) {
 	    return masterPlateDAO.getById(id);
     }
 
 	public PlateResults getPlateResults(Plate plate, IProgressMonitor monitor) {
 		return new PlateResults(plate, monitor);
     }
 
 	public void edit(User editor, MasterPlate masterPlate) {
 		
 		masterPlate = masterPlateDAO.merge(masterPlate);
 		getAuditService().audit(editor, AuditType.UPDATE_EVENT, masterPlate);
 		masterPlateDAO.save(masterPlate);
 		LazyLoadingSessionHolder.getInstance().evict(
 			LazyLoadingSessionHolder.getInstance().load( MasterPlate.class, 
 					                                     masterPlate.getId() ) );
 		LazyLoadingSessionHolder.getInstance().evict(editor);
     }
 
 	public void edit(User editor, Plate plate) {
 
 		plate = plateDAO.merge(plate);
 		getAuditService().audit(editor, AuditType.UPDATE_EVENT, plate);
 		plateDAO.save(plate);
 //		LazyLoadingSessionHolder.getInstance().evict(
 //			LazyLoadingSessionHolder.getInstance().load( Plate.class, 
 //					                                     plate.getId() ) );
 
     }
 
 	public List<String> getAllPlateBarcodes() {
 		return plateDAO.findAllPlateBarcodes();
     }
 
 	public Plate getPlate(String barcode) {
 		return plateDAO.findByBarcode(barcode).get(0);
     }
 	
 	public void addResult(User user, ResultParser parser, List<String> barcodesOfPlatesToGetResults, IProgressMonitor monitor ) {
 
 		try {
 			monitor.beginTask("Imports results from file", barcodesOfPlatesToGetResults.size()*2);
 			
 			List<Plate> plates = new ArrayList<Plate>();
 			for(String barcode : barcodesOfPlatesToGetResults) {
 				plates.addAll( plateDAO.findByBarcode(barcode) );
 				monitor.worked(1);
 			}
 	
 			parser.addResultsTo(user, plates);
 			
 			for(Plate plate : plates) {
 //				edit(user, plate);
 				plateDAO.evict(plate);
 				LazyLoadingSessionHolder.getInstance().evict(plate);
 				monitor.worked(1);
 			}
 		}
 		finally {
 			monitor.done();
 		}
     }
 
 	public Collection<Plate> getAllPlatesNotDeleted() {
 		return plateDAO.findAllNotDeleted();
     }
 
 	public Collection<MasterPlate> getAllMasterPlatesNotDeleted() {
 	    return masterPlateDAO.findAllNotDeleted();
     }
 
 	public void editMerging(User editor, Plate plate) {
 		userDAO.update(editor);
 		plate = plateDAO.merge(plate);
 		plateDAO.save(plate);
 		
 		getAuditService().audit(editor, AuditType.UPDATE_EVENT, plate);
     }
 
 //	@Override
 //    public void evictfromLazyLoading(Plate toBeSaved) {
 //		LazyLoadingSessionHolder.getInstance().evict( toBeSaved );
 //		LazyLoadingSessionHolder.getInstance().evict( toBeSaved.getCreator() );
 //    }
 
 	public void evictfromLazyLoading(AbstractBasePlate toBeSaved) {
 		LazyLoadingSessionHolder.getInstance().evict( toBeSaved );
 		LazyLoadingSessionHolder.getInstance().evict( toBeSaved.getCreator() );
 		for(PlateFunction pf : toBeSaved.getPlateFunctions()) {
 			LazyLoadingSessionHolder.getInstance().evict(pf.getCreator());
 		}
     }
 
     public long createPlate( User creator, 
                              String name, 
                              String barcode,
                              Folder folder, 
                              MasterPlate masterPlate,
                              PatientOrigin patientOrigin, 
                              Timestamp defrostingDate ) {
 		
 		masterPlate = masterPlateDAO.merge(masterPlate);
 		folder = folderDAO.merge(folder);
 		
 		Plate plate = AbstractPlate.createPlate( creator, 
 												 name, 
 												 masterPlate, 
 												 barcode,
 												 folder );
 		folder.getObjects().add(plate);
 
 		for( Well well : plate.getWells() ) {
 			
 			SampleContainer container = well.getSampleContainer();
 			for( AbstractSample as : container.getSamples() ) {
 				if(as instanceof DrugSample) {
 					drugSampleDAO.save( (DrugSample)as );
 				}
 				else {
 					throw new IllegalArgumentException("There is not supposed to be a " + as + " here");
 				}
 			}
 			
 			PatientSample patientSample = new PatientSample( creator, 
 					                                         patientOrigin.getName(), 
 					                                         container, 
 					                                         patientOrigin, 
 					                                         defrostingDate );
 			patientSampleDAO.save(patientSample);
 			container.getSamples().add(patientSample);
 			
 			sampleContainerDAO.save(container);
 			
 			getAuditService().audit(creator, AuditType.CREATE_EVENT, patientSample);
 		}
 		plateDAO.save(plate);
 //		folder = folderDAO.merge(folder);
 		folderDAO.save(folder);
 		
 		getAuditService().audit(creator, AuditType.CREATE_EVENT, plate);
 		
 	    return plate.getId();
     }
 
 	@Override
     public long createMasterPlate(User creator, String name,
                                   MasterPlate masterPlate, Folder folder,
                                   int numOfPlates) {
 
     	folder  = folderDAO.merge(folder);
 		creator = userDAO.merge(creator);
 		
 		net.bioclipse.brunn.pojos.MasterPlate copy = masterPlate.makeNewCopy(creator);
     	copy.setName(name);

 		folder.getObjects().add(copy);
 		
 		masterPlateDAO.save(copy);
 		folderDAO.save(folder);
 		
 		getAuditService().audit(creator, AuditType.CREATE_EVENT, masterPlate);
 		evictfromLazyLoading(masterPlate);
 		evictfromLazyLoading(copy);
 	    return copy.getId();
     	
     }
 }
