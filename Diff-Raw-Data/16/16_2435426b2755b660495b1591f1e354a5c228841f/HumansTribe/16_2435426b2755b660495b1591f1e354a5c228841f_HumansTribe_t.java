 package ai.ilikeplaces.entities;
 
 import ai.ilikeplaces.doc.BIDIRECTIONAL;
 import ai.ilikeplaces.doc.FIXME;
 import ai.ilikeplaces.doc.UNIDIRECTIONAL;
 import ai.ilikeplaces.doc.WARNING;
 import ai.ilikeplaces.exception.DBException;
 import ai.ilikeplaces.logic.Listeners.widgets.UserProperty;
 import ai.ilikeplaces.logic.crud.DB;
 import ai.ilikeplaces.logic.validators.unit.HumanId;
 import ai.ilikeplaces.util.Return;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.Set;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Ravindranath Akila
  * Date: 10/12/11
  * Time: 7:47 PM
  */
 @WARNING("THIS ENTITY IS NOT GUARANTEED TO 'BE' EVEN THOUGH A HUMAN IS SIGNED UP. SO CREATE IT IF NOT PRESENT!")
 @Entity
 public class HumansTribe implements HumansFriend, HumanIdFace, HumanEqualsFace , Serializable {
     public String humanId;
 
     public Set<Tribe> tribes;
 
     @Id
     public String getHumanId() {
         return humanId;
     }
 
     @Override
     public String getDisplayName() {
         return UserProperty.HUMANS_IDENTITY_CACHE.get(getHumanId(), "").getHuman().getDisplayName();
     }
 
     @Override
     @Transient
     public boolean isFriend(final String friendsHumanId) {
         final Return<Boolean> r = DB.getHumanCRUDHumanLocal(true).doDirtyIsHumansNetPeople(new ai.ilikeplaces.logic.validators.unit.HumanId(this.humanId), new ai.ilikeplaces.logic.validators.unit.HumanId(friendsHumanId));
         if (r.returnStatus() != 0) {
             throw new DBException(r.returnError());
         }
         return r.returnValue();
     }
 
     @Override
     public boolean notFriend(final String friendsHumanId) {
         return !isFriend(friendsHumanId);
     }
 
     public void setHumanId(final String humanId) {
         this.humanId = humanId;
     }
 
     @Transient
     public HumansTribe setHumanIdR(final String humanId) {
         this.humanId = humanId;
         return this;
     }
 
     @BIDIRECTIONAL(ownerside = BIDIRECTIONAL.OWNING.NOT)
     @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
     public Set<Tribe> getTribes() {
         return tribes;
     }
 
     public void setTribes(final Set<Tribe> tribes) {
         this.tribes = tribes;
     }
 
     @Override
     public boolean equals(final Object o) {
         if (this == o) {
             return true;
         }
 
         if (o == null) {
             return false;
         }
 
         if (getClass() == o.getClass()) {
            final HumanEqualsFace that = (HumanEqualsFace) o;
             return !(this.getHumanId() == null || that.getHumanId() == null) && this.getHumanId().equals(that.getHumanId());
         } else {
             return HumanEquals.staticMatchHumanId(this, o);
         }
     }
 }
 
