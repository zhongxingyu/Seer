 package no.niths.application.rest;
 
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.application.rest.lists.RoomList;
 import no.niths.common.AppConstants;
 import no.niths.domain.Room;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.interfaces.RoomService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 @RequestMapping(AppConstants.ROOMS)
 public class RoomControllerImpl extends AbstractRESTControllerImpl<Room> {
 
     private static final Logger logger = LoggerFactory
             .getLogger(RoomControllerImpl.class);
 
     @Autowired
     private RoomService service;
 
     private RoomList roomList = new RoomList();
 
     @Override
     public GenericService<Room> getService() {
         return service;
     }
 
     @Override
     public ListAdapter<Room> getList() {
         return roomList;
     }
 }
