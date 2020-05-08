 package no.niths.application.rest;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.application.rest.interfaces.RoomController;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.application.rest.lists.RoomList;
 import no.niths.common.AppConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.location.Room;
 import no.niths.domain.signaling.AccessField;
 import no.niths.services.interfaces.AccessFieldService;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.interfaces.RoomService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 @Controller
 @RequestMapping(AppConstants.ROOMS)
 public class RoomControllerImpl extends AbstractRESTControllerImpl<Room>
 		implements RoomController {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(RoomControllerImpl.class);
 
 	@Autowired
 	private RoomService service;
 
 	@Autowired
 	private AccessFieldService afService;
 
 	private RoomList roomList = new RoomList();
 
 	@Override
 	public GenericService<Room> getService() {
 		return service;
 	}
 
 	@Override
 	public ListAdapter<Room> getList() {
 		return roomList;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@RequestMapping(value = "paginated/{firstResult}/{maxResults}", method = RequestMethod.GET)
 	@ResponseBody
 	public ArrayList<Room> getAll(Room room, @PathVariable int firstResult,
 			@PathVariable int maxResults) {
 		super.getAll(room, firstResult, maxResults);
 
 		return roomList;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
	@RequestMapping(value = "{roomid}/add-accessfield/{afId}", method = RequestMethod.PUT)
 	@ResponseStatus(value = HttpStatus.OK, reason = "AcessField Removed")
 	public void addAccessField(@PathVariable long roomId,
 			@PathVariable long afId) {
 		Room room = service.getById(roomId);
 		ValidationHelper.isObjectNull(room, "Room does not exist");
 		AccessField af = afService.getById(afId);
 		ValidationHelper.isObjectNull(afId, "Acess field does not exist");
 
 		room.getAccessFields().add(af);
 		service.update(room);
 		logger.debug("Room updated");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@RequestMapping(value = "remove/accessfield/{roomId}/{afId}", method = RequestMethod.PUT)
 	@ResponseStatus(value = HttpStatus.OK, reason = "AcessField Removed")
 	public void removeAccessField(@PathVariable long roomId,
 			@PathVariable long afId) {
 		Room room = service.getById(roomId);
 		ValidationHelper.isObjectNull(room, "Room does not exist");
 
 		boolean isRemoved = false;
 		for (int i = 0; i < room.getAccessFields().size(); i++) {
 			if (room.getAccessFields().get(i).getId() == afId) {
 				room.getAccessFields().remove(i);
 				isRemoved = true;
 				break;
 			}
 		}
 
 		if (isRemoved) {
 			service.update(room);
 		} else {
 			logger.debug("Accss field not Found");
 			throw new ObjectNotFoundException("Accss field not Found");
 		}
 	}
 
 	/**
 	 * 
 	 * @param fields
 	 */
 	private void clearRelationships(Field[] fields) {
 		for (Room r : roomList) {
 			for (Field f : fields) {
 				Class<?> c = Collection.class;
 				if (c.isAssignableFrom(f.getType())) {
 					try {
 						String name = f.getName();
 						Method m = r.getClass().getMethod(
 								"set" + Character.toUpperCase(name.charAt(0))
 										+ name.substring(1), f.getType());
 						m.invoke(r, new Object[] { null });
 
 					} catch (NoSuchMethodException | SecurityException
 							| IllegalAccessException | IllegalArgumentException
 							| InvocationTargetException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 }
