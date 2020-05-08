 package ch.opentrainingcenter.client.action.job;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.opentrainingcenter.core.cache.Cache;
 import ch.opentrainingcenter.core.importer.ConvertContainer;
 import ch.opentrainingcenter.core.importer.IConvert2Tcx;
 import ch.opentrainingcenter.importer.IFileImport;
 import ch.opentrainingcenter.model.importer.IGpsFileModel;
 import ch.opentrainingcenter.model.importer.IGpsFileModelWrapper;
 import ch.opentrainingcenter.transfer.ITraining;
 
 @SuppressWarnings("nls")
 public class ImportManualJobTest {
     private ImportManualJob job;
     private IGpsFileModelWrapper modelWrapper;
     private Cache cache;
     private String filterPath;
     private ConvertContainer cc;
     private IFileImport importer;
 
     @Before
     public void before() throws IOException {
         cc = mock(ConvertContainer.class);
         modelWrapper = mock(IGpsFileModelWrapper.class);
         filterPath = "";
         cache = mock(Cache.class);
         importer = mock(IFileImport.class);
     }
 
     @Test
     public void testConstructor() {
 
         job = new ImportManualJob("junit", modelWrapper, filterPath, importer, cache);
         assertNotNull(job);
     }
 
     @Test
     public void testRun() {
         job = new ImportManualJob("junit", modelWrapper, filterPath, importer, cache);
 
         final IProgressMonitor monitor = mock(IProgressMonitor.class);
         final IStatus status = job.run(monitor);
 
         assertEquals(Status.OK_STATUS, status);
     }
 
     @Test(timeout = 3000)
     public void testRunMitFileModel() throws Exception {
         job = new ImportManualJob("junit", modelWrapper, filterPath, importer, cache);
 
         final List<IGpsFileModel> models = new ArrayList<IGpsFileModel>();
         final IGpsFileModel model = mock(IGpsFileModel.class);
         models.add(model);
         when(model.getFileName()).thenReturn("gmn");
 
         when(modelWrapper.getGpsFileModels()).thenReturn(models);
 
         final IConvert2Tcx convert = mock(IConvert2Tcx.class);
         when(cc.getMatchingConverter(new File(""))).thenReturn(convert);
         final ITraining element = mock(ITraining.class);
         when(convert.convert(new File(""))).thenReturn(element);
 
         final IProgressMonitor monitor = mock(IProgressMonitor.class);
         when(importer.importFile(filterPath, modelWrapper, monitor)).thenReturn(new ArrayList<ITraining>());
 
         final IStatus status = job.run(monitor);
 
         assertEquals(Status.OK_STATUS, status);
     }
 }
