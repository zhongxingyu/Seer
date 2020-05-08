 package com.thoughtworks.lirenlab.interfaces.library.web;
 
 import com.thoughtworks.lirenlab.interfaces.library.facade.LibraryServerFacade;
 import com.thoughtworks.lirenlab.interfaces.library.facade.dto.LibraryDTO;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 
 import javax.ws.rs.core.Response;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.BDDMockito.given;
 import static org.mockito.Mockito.verify;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class LibraryResourcesTest {
 
     @Mock
     private LibraryServerFacade libraryServerFacade;
 
     private LibraryResources libraryResources;
 
     @Before
     public void setUp() {
         initMocks(this);
         libraryResources = new LibraryResources(libraryServerFacade);
     }
 
     @Test
     public void should_add_a_new_library(){
         String address = "Sichuan Chengdu";
         LibraryDTO dto = new LibraryDTO();
         dto.setAddress(address);
 
         libraryResources.add(dto);
 
         verify(libraryServerFacade).add(12l, address);
     }
 
     @Test
     public void should_return_success_statue_after_added_a_new_library(){
         LibraryDTO dto = new LibraryDTO();
 
         Response response = libraryResources.add(dto);
 
         assertThat(response.getStatus(), is(200));
     }
 }
