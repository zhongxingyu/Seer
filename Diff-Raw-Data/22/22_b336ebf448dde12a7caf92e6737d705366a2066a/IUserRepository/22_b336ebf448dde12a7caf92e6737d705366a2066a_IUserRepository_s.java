 package nl.zuyd.marktplaats_revised.repositories;
 
 import java.util.List;
 
 import javax.ejb.Local;
 
 import nl.zuyd.marktplaats_revised.entities.User; 
 
 @Local
 public interface IUserRepository
 {
	User getById(int id);
 	List<User> getAll();
}
