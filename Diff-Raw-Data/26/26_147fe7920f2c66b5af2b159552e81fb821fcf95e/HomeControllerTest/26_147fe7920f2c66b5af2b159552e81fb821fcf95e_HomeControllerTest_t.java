 package org.motechproject.ananya.referencedata.web.controller;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.motechproject.ananya.referencedata.contactCenter.service.LocationService;
 import org.motechproject.ananya.referencedata.csv.ImportType;
 import org.motechproject.ananya.referencedata.flw.domain.Location;
 import org.motechproject.ananya.referencedata.flw.domain.LocationStatus;
 import org.motechproject.ananya.referencedata.web.domain.CsvUploadRequest;
 import org.motechproject.importer.model.AllCSVDataImportProcessor;
 import org.motechproject.importer.model.CSVDataImportProcessor;
 import org.springframework.test.web.server.MvcResult;
 import org.springframework.web.multipart.commons.CommonsMultipartFile;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Arrays;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import static org.mockito.Mockito.*;
 import static org.motechproject.ananya.referencedata.web.utils.MVCTestUtils.mockMvc;
 import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
 import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
 
 @RunWith(MockitoJUnitRunner.class)
 public class HomeControllerTest {
 
     @Mock
     private LocationService locationService;
     @Mock
     private AllCSVDataImportProcessor allCSVDataImportProcessor;
 
     private HomeController homeController;
 
     @Before
     public void setup() {
         homeController = new HomeController(locationService, allCSVDataImportProcessor);
     }
 
     @Test
     public void shouldReturnLocationsToBeVerifiedAsCSV() throws Exception {
         Location location = new Location("d", "b", "p", LocationStatus.NOT_VERIFIED, null);
         when(locationService.getLocationsToBeVerified()).thenReturn(Arrays.asList(location));
 
         MvcResult mvcResult = mockMvc(homeController).perform(get("/admin/locationsToBeVerified/download"))
                 .andExpect(status().isOk()).andReturn();
 
         String contentAsString = mvcResult.getResponse().getContentAsString();
         assertTrue(contentAsString.contains("district,block,panchayat,status,newDistrict,newBlock,newPanchayat"));
         assertTrue(contentAsString.contains("d,b,p,NOT VERIFIED,,"));
     }
 
     @Test
     public void shouldThrowExceptionOnError() throws Exception {
         when(locationService.getLocationsToBeVerified()).thenThrow(new RuntimeException("aragorn"));
 
         MvcResult mvcResult = mockMvc(homeController).perform(get("/admin/locationsToBeVerified/download"))
                 .andExpect(status().is(500)).andReturn();
        assertEquals("An error has occurred : The system is down. Please try after some time.",mvcResult.getModelAndView().getModelMap().get("errorMessage"));
     }
 
     @Test
     public void shouldUploadLocationsFile() throws Exception {
         CommonsMultipartFile fileData = mock(CommonsMultipartFile.class);
         HttpServletResponse response = mock(HttpServletResponse.class);
         ServletOutputStream outputStream = mock(ServletOutputStream.class);
         CSVDataImportProcessor csvDataImportProcessor = mock(CSVDataImportProcessor.class);
         CsvUploadRequest csvFileRequest = new CsvUploadRequest(fileData);
         String errorCsv = "response";
         when(response.getOutputStream()).thenReturn(outputStream);
         when(fileData.getBytes()).thenReturn(new byte[1]);
         when(allCSVDataImportProcessor.get(ImportType.Location.name())).thenReturn(csvDataImportProcessor);
         when(csvDataImportProcessor.processContent(csvFileRequest.getStringContent())).thenReturn(errorCsv);
 
         homeController.uploadLocations(csvFileRequest, response);
 
         verify(outputStream).write(errorCsv.getBytes());
         verify(response).setHeader(eq("Content-Disposition"), matches(
                 "attachment; filename=location_upload_failures\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}.csv"));
         verify(outputStream).flush();
     }
 
     @Test(expected = Exception.class)
     public void shouldThrowExceptionOnUploadError() throws Exception {
         when(allCSVDataImportProcessor.get(ImportType.Location.name())).thenThrow(new Exception());
 
        MvcResult mvcResult = mockMvc(homeController).perform(post("/admin/location/upload").body(new byte[1]))
                 .andExpect(status().is(500)).andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString().contains("An error has occurred"));
     }
 }
