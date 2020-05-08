 package de.hswt.hrm.inspection.service;
 
 import org.eclipse.e4.core.di.annotations.Creatable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import java.util.Collection;
 
 import javax.inject.Inject;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.inspection.dao.core.ILayoutDao;
 import de.hswt.hrm.inspection.model.Layout;
 
 @Creatable
 public class LayoutService {
 
     private final static Logger LOG = LoggerFactory.getLogger(LayoutService.class);
 
     private final ILayoutDao layoutDao;
 
     @Inject
     public LayoutService(ILayoutDao layoutDao) {
 
         checkNotNull(layoutDao, "LayoutDao must be injected properly");
         this.layoutDao = layoutDao;
        LOG.debug("Layout Dao injected successfully");
     }
 
     public Collection<Layout> findAll() throws DatabaseException {
         return layoutDao.findAll();
 
     }
 
     public Layout findById(int id) throws ElementNotFoundException, DatabaseException {
         return layoutDao.findById(id);
     }
 
     public Layout insert(final Layout layout) throws SaveException {
         return layoutDao.insert(layout);
     }
 
     public void update(final Layout layout) throws ElementNotFoundException, SaveException {
         layoutDao.update(layout);
     }
 
     public void refresh(Layout layout) throws DatabaseException {
 
         Layout fromDb = layoutDao.findById(layout.getId());
 
         layout.setName(fromDb.getName());
         layout.setFileName(fromDb.getFileName());
 
     }
 
 }
