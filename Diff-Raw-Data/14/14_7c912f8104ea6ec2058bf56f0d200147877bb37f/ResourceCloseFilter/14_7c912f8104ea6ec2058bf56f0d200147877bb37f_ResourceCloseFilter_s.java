 package pl.psnc.dl.wf4ever.auth;
 
 import pl.psnc.dl.wf4ever.rosrs.ROSRService;
 
 import com.sun.jersey.spi.container.ContainerRequest;
 import com.sun.jersey.spi.container.ContainerResponse;
 import com.sun.jersey.spi.container.ContainerResponseFilter;
 
 public class ResourceCloseFilter implements ContainerResponseFilter {
 
     @Override
     public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        if (ROSRService.SMS.get() != null) {
            ROSRService.SMS.get().close();
        }
         return response;
     }
 
 }
