 package be.occam.colloseum.action.repository;
 
 import java.util.List;
 
 import javax.ws.rs.ext.Providers;
 
 import be.occam.colloseum.action.Action;
 import be.occam.colloseum.action.ActionDTO;
 
 public interface IActionRepository {
 
 	public ActionDTO findOne( String id );
 	
	public List<ActionDTO> findAll( Providers providers );
 	
 	public ActionDTO persist( ActionDTO persist );
 
 }
