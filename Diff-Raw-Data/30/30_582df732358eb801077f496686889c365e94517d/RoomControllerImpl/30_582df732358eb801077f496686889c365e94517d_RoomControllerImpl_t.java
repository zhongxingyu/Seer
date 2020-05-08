 package no.niths.application.rest;
 
import java.util.ArrayList;

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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping(AppConstants.ROOMS)
 public class RoomControllerImpl extends AbstractRESTControllerImpl<Room> {
 
     private static final Logger logger = LoggerFactory
             .getLogger(RoomControllerImpl.class);
 
     @Autowired
     private RoomService service;
 
     private RoomList roomList = new RoomList();
 
    /**
     * FIXME Figure out why there are duplicate rooms
     */
    @Override
    @RequestMapping(
            method  = RequestMethod.GET,
            headers = RESTConstants.ACCEPT_HEADER)
    @ResponseBody
    public ArrayList<Room> getAll(Room room) {
        roomList = (RoomList) super.getAll(room);
        final int roomListSize = roomList.size();

        // Temporary dirty fix to remove duplicates
        for (int i = roomListSize - 1; i >= 1; i--) {
            Room r = roomList.get(i);
            
            if (r.getId() == roomList.get(i - 1).getId()) {
                roomList.remove(i);
            }
        }

        return roomList;
    }

     @Override
     public GenericService<Room> getService() {
         return service;
     }
 
     @Override
     public ListAdapter<Room> getList() {
         return roomList;
     }
 }
