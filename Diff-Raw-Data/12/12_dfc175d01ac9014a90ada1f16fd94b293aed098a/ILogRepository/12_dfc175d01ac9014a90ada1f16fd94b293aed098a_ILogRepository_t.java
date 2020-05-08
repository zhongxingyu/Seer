 package cs.wintoosa.repository;
 
 import cs.wintoosa.domain.Log;
 import java.util.List;
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.data.repository.CrudRepository;
 
 /**
  *
  * @author jonimake
  */
 public interface ILogRepository extends JpaRepository<Log, Long>{
    public List<Log> findByPhoneIdAndTimestampBetween(String phoneId, Long sessionStart, Long sessionEnd);
 }
