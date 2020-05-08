 /**
  * 
  */
 package net.bioclipse.brunn.business.plateLayout;
 
 import java.util.Collection;
 
 import net.bioclipse.brunn.business.LazyLoadingSessionHolder;
 import net.bioclipse.brunn.pojos.AbstractBasePlate;
 import net.bioclipse.brunn.pojos.AuditType;
 import net.bioclipse.brunn.pojos.Folder;
 import net.bioclipse.brunn.pojos.LayoutWell;
 import net.bioclipse.brunn.pojos.PlateFunction;
 import net.bioclipse.brunn.pojos.PlateLayout;
 import net.bioclipse.brunn.pojos.PlateType;
 import net.bioclipse.brunn.pojos.User;
 import net.bioclipse.brunn.pojos.WellFunction;
 
 /**
  * This class handles everything that has to do with the PlateLayout classes:
  *  PlateLayout
  *  LayoutWell
  *  LayoutMarker
  *  PlateType 
  *  
  * It should be instantiated as a Spring bean for everything to work.
  *
  * @author jonathan
  *
  */
 public class PlateLayoutManager extends AbstractDAOBasedPlateLayoutManager implements IPlateLayoutManager {
 
 	public PlateLayoutManager(){
 		
 	}
 
 	public long createPlateType(User creator, int cols, int rows, String name, Folder folder) {
 
 		folder = folderDAO.merge(folder);
 		PlateType plateType = new PlateType( creator, cols, rows, name);
 		folder.getObjects().add(plateType);
 		
 		plateTypeDAO.save(plateType);
 		folderDAO.save(folder);
 				
 		auditService.audit(creator, AuditType.CREATE_EVENT, plateType);
 		auditService.audit(creator, AuditType.UPDATE_EVENT, folder);
 		
 		return plateType.getId();
     }
 
 	public long createPlateLayout(User creator, String name, PlateType plateType, Folder folder) {
 		
 		folder = folderDAO.merge(folder);
 		creator = userDAO.merge(creator);
 		PlateLayout plateLayout = new PlateLayout( creator, name, plateType );
 		folder.getObjects().add(plateLayout);
 		
 		plateLayoutDAO.save(plateLayout);
 		
 		auditService.audit(creator, AuditType.CREATE_EVENT, plateLayout);
 		evictFromLazyLoading(plateLayout);
 		return plateLayout.getId();
     }
 	
 	public Collection getAllPlateLayouts() {
 		return plateLayoutDAO.findAll();
 	}
 
 	public Collection getAllPlateTypes() {
 		return plateTypeDAO.findAll();
 	}
 
 	public PlateLayout getPlateLayout(long plateLayoutId) {
 		return plateLayoutDAO.getById(plateLayoutId);
 	}
 
 	public PlateType getPlateType(long plateTypeId) {
 		return plateTypeDAO.getById(plateTypeId);
 	}
 
 	public long createPlateFunction(User creator, String name, AbstractBasePlate plate, String expression, double goodFrom, double goodTo) {
 
 		//TODO check whether necessary:
 		//plate = plateDAO.getByid( plate.getID() );
 		
 		PlateFunction plateFunction = new PlateFunction(creator, name, expression, goodFrom, goodTo, true, plate);
 		plate.getPlateFunctions().add(plateFunction);
 		
 		getAuditService().audit(creator, AuditType.UPDATE_EVENT, plate);
 		
 	    return plateFunction.getId();
     }
 
 	public long createPlateFunction(User creator, String name, AbstractBasePlate plate, String expression) {
 	    
 		//TODO check whether necessary:
 		//plate = plateDAO.getByid( plate.getID() );
 		
 		PlateFunction plateFunction = new PlateFunction(creator, name, expression, 0, 0, false, plate);
 		plate.getPlateFunctions().add(plateFunction);
 		
 		getAuditService().audit(creator, AuditType.UPDATE_EVENT, plate);
 		
 	    return plateFunction.getId();
     }
 
 	public long createWellFunction(User creator, String name, LayoutWell well, String expression) {
 	    
 //		well = layoutWellDAO.merge(well);
 		
 		WellFunction wellFunction = new WellFunction(creator, name, expression, well);
 		
 		getAuditService().audit(creator, AuditType.UPDATE_EVENT, well.getPlateLayout());
 		well = layoutWellDAO.merge(well);
 		layoutWellDAO.save(well);
 		
 	    return wellFunction.getId();
     }
 
 	public void edit(User editor, PlateType plateType) {
 	    
 		plateType = plateTypeDAO.merge(plateType);
 		plateTypeDAO.save(plateType);
 		getAuditService().audit(editor, AuditType.UPDATE_EVENT, plateType);
     }
 
 	public void edit(User editor, PlateLayout plateLayout) {
 		
 		plateLayout = plateLayoutDAO.merge(plateLayout);
 		plateLayoutDAO.save(plateLayout);
 		getAuditService().audit(editor, AuditType.UPDATE_EVENT, plateLayout);
     }
 
 	public Collection<PlateType> getAllPlateTypesNotDeleted() {
 	    return plateTypeDAO.findAllNotDeleted();
     }
 
 	public Collection<PlateLayout> getAllPlateLayoutsNotDeleted() {
 	    return plateLayoutDAO.findAllNotDeleted();
     }
 
 	public void evictFromLazyLoading(PlateLayout toBeSaved) {
 	    LazyLoadingSessionHolder.getInstance().evict( toBeSaved );
 	    LazyLoadingSessionHolder.getInstance().evict( toBeSaved.getCreator() );
 	    for ( PlateFunction pf : toBeSaved.getPlateFunctions() ) {
 	    	LazyLoadingSessionHolder.getInstance().evict( pf.getCreator() );
 	    }
     }
 }
