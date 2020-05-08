 package amu.licence.edt.model.dao.jpa;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import amu.licence.edt.model.beans.CRoom;
 import amu.licence.edt.model.beans.CRoomType;
 import amu.licence.edt.model.beans.Group;
 import amu.licence.edt.model.beans.SessionType;
 import amu.licence.edt.model.dao.DAOCRoom;
 
 public class DAOCRoomJPA extends DAOGeneriqueJPA<CRoom> implements DAOCRoom {
 
     public DAOCRoomJPA(EntityManager entityManager) {
         super(entityManager);
     }
 
     @Override
     public List<CRoom> findAvailables(Group group, SessionType st, Date date, Integer duration) {
        Query q = entityManager.createNamedQuery(CRoom.FIND_AVAILABLES_BY_ST_PERIOD);
         q.setParameter(2, date);
         q.setParameter(3, duration);
 
         List<CRoom> availableCRooms = new ArrayList<CRoom>();
         for (CRoomType crt : st.getCompatibleCRoomTypes()) {
             for (CRoom cr : crt.getCrooms()) {
                 if (((BigDecimal)q.setParameter(1, cr.getId()).getSingleResult()).intValue() == 0 &&
                     cr.getMaxSize() >= group.getGroupSize()) {
                     availableCRooms.add(cr);
                 }
             }
         }
         return availableCRooms;
     }
 
 }
