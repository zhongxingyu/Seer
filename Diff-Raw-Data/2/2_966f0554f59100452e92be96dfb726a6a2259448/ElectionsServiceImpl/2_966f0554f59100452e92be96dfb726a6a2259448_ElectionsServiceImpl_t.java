 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import com.hashin.project.bean.ElectionsBean;
 import com.hashin.project.dao.ElectionsDAO;
 import com.hashin.project.dao.ElectionsDAOImpl;
 
 public class ElectionsServiceImpl implements ElectionsService 
 {
     	@Autowired
 	private ElectionsDAO electionsDao;
     	
 	
     	public int create(ElectionsBean election)
 	{
     	    return electionsDao.create(election); 
     	    //todo check if the the return value ==0 or <0; then then throw some database related custome exception
 	}
 	
 	public ElectionsBean getById(int electId)
 	{
 	    ElectionsBean election = electionsDao.getById(electId);
 	    return election;
 	}
 	
 	public List<ElectionsBean> listAll()
 	{
 	    List<ElectionsBean> electionList = electionsDao.listAll();
 	    return electionList;
 	}
 	
 	public List<ElectionsBean> searchWildCard(String electTitle){
 	    List<ElectionsBean> electionList = electionsDao.searchWildCard(electTitle);
 	    return electionList;
 	}
 	
 	public int deleteById(int electId)
 	{
 	    return electionsDao.deleteById(electId);
 	}
 
 }
