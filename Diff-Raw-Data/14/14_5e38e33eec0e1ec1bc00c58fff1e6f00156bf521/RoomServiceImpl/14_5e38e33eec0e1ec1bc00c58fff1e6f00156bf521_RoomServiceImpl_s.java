 package no.niths.services.location;
 
 import java.util.List;
 
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.common.LazyFixer;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.location.Room;
 import no.niths.domain.signaling.AccessField;
 import no.niths.infrastructure.interfaces.GenericRepository;
 import no.niths.infrastructure.interfaces.RoomRepository;
 import no.niths.services.AbstractGenericService;
 import no.niths.services.location.interfaces.RoomService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class RoomServiceImpl extends AbstractGenericService<Room>
         implements RoomService {
 
     private Logger logger = LoggerFactory.getLogger(RoomServiceImpl.class);
 
     LazyFixer<Room> lazyFixer = new LazyFixer<Room>();
 
     @Autowired
     private RoomRepository repo;
 
     @Override
     public List<Room> getAll(Room domain) {
         List<Room> rooms = repo.getAll(domain);
         lazyFixer.fetchChildren(rooms);
         return rooms;
     }
 
     @Override
     public Room getById(long id) {
         Room room = repo.getById(id);
        if (room != null) {
            room.getAccessFields().size();
        }
         return room;
     }
 
     public void addAccessField(long roomId, long AccessFieldId) {
         Room room = getById(roomId);
         
     }
 
     @Override
     public void removeAccessField(long roomId, long accessFieldId) {
         Room room = getById(roomId);
         
         ValidationHelper.isObjectNull(room, Room.class);
 
         List<AccessField> accessFields = room.getAccessFields();
 
         boolean wasRemoved = false;
         for (int i = 0; i < accessFields.size(); i++) {
             AccessField accessField = accessFields.get(i);
 
             if (accessField.getId() == accessFieldId) {
                 accessFields.remove(i);
                 wasRemoved = true;
                 break;
             }
         }
 
         if (wasRemoved) {
             super.mergeUpdate(room);
         } else {
             final String message = "Access field not found";
             logger.error(message);
             throw new ObjectNotFoundException(message);
         }
     }
 
     @Override
     public GenericRepository<Room> getRepository() {
         return repo;
     }
 }
