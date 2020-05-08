 package edu.iup.chem.inventory.dao;
 
 import static edu.iup.chem.inventory.db.inventory.Tables.ACCESS;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.jooq.BatchBindStep;
 
 import edu.iup.chem.inventory.ConnectionPool;
 import edu.iup.chem.inventory.Constants;
 import edu.iup.chem.inventory.db.inventory.InventoryFactory;
 import edu.iup.chem.inventory.db.inventory.tables.records.RoomRecord;
 import edu.iup.chem.inventory.db.inventory.tables.records.UserRecord;
 
 public class AccessDao {
 
 	private static final Logger	LOG	= Logger.getLogger(AccessDao.class);
 
 	public static void grantUserAccess(final UserRecord rec,
 			final List<RoomRecord> rooms) {
 		final String role = rec.getRoleName();
		ArrayList<String> roomsToAdd = new ArrayList<>();
 
 		switch (role) {
 			case Constants.ADMIN_ROLE:
 			case Constants.SITE_ADMIN_ROLE:
 			case Constants.DATA_ENTRY_ROLE:
				roomsToAdd = new ArrayList<>(RoomDao.getAllRoomNames());
 				break;
 			case Constants.FACULTY_ROLE:
				roomsToAdd = new ArrayList<>(Arrays.asList("Weyandt 146",
						"Weyandt 126A"));
				//$FALL-THROUGH$
			case Constants.RESEARCHER_ROLE:
 				for (final RoomRecord room : rooms) {
 					roomsToAdd.add(room.getRoom());
 				}
 				break;
 			default:
 				break;
 		}
 
		LOG.info("Adding access for " + rec.getName() + " to "
 				+ roomsToAdd.size() + " rooms: " + roomsToAdd.toString());
 		try (Connection conn = ConnectionPool.getConnection()) {
 			final InventoryFactory create = new InventoryFactory(conn);
 			BatchBindStep bindTo = create
 					.batch(create.insertInto(ACCESS, ACCESS.UID, ACCESS.NAME,
 							ACCESS.ROOM).values("?", "?", "?"));
 
 			final Integer uid = rec.getUid();
 			final String name = rec.getName();
 
 			for (final String room : roomsToAdd) {
 				bindTo = bindTo.bind(uid, name, room);
 			}
 
 			bindTo.execute();
 		} catch (final SQLException e) {
 			LOG.error("Failed to grant access.", e.getCause());
 		}
 
 	}
 
 	public static void revokeUserAccess(final UserRecord rec) {
 		try (Connection conn = ConnectionPool.getConnection()) {
 			final InventoryFactory create = new InventoryFactory(conn);
 			create.delete(ACCESS).where(ACCESS.UID.equal(rec.getUid()))
 					.execute();
 		} catch (final SQLException e) {
 			LOG.error("Failed to revoke user access");
 		}
 
 	}
 
 	public static void updateUserAccess(final UserRecord rec,
 			final List<RoomRecord> rooms) {
 		revokeUserAccess(rec);
 		grantUserAccess(rec, rooms);
 
 	}
 }
