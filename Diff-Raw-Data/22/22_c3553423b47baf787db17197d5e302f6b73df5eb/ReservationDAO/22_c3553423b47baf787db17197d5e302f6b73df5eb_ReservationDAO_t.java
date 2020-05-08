 package dal.concrete.mysql;
 
 import dal._abstract.mysql.AbstractDAO;
 
 import pojo.ObjectResource;
 import pojo.Reservation;
 
 public class ReservationDAO extends AbstractDAO<Reservation> {
 	private static ReservationDAO instance;
 
 	public ReservationDAO() {
 		super(Reservation.class);
 	}
 
 	public static ReservationDAO getInstance() {
 		if (instance == null)
 			instance = new ReservationDAO();
 		return instance;
 	}
 	
 	public boolean isPossible(Reservation reservation) {
 		loadRelationships(reservation);
 		
 		for (ObjectResource or: reservation.objects) {
			if (!ObjectResourceDAO.getInstance().isAvailable(or, reservation.beginTime, reservation.endTime)) {
				System.out.println(or.name + " " + reservation.beginTime + " " + reservation.endTime);
 				return false;
			}
 		}
 		
 		return PlaceResourceDAO.getInstance().isAvailable(reservation.placeReserved, reservation.beginTime, reservation.endTime);
 	}
 }
