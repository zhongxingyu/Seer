 package au.org.scoutmaster.dao;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.joda.time.DateTime;
 
 import au.org.scoutmaster.domain.SectionType;
 
 import com.vaadin.addon.jpacontainer.JPAContainer;
 
 public class SectionTypeDao extends JpaBaseDao<SectionType, Long> implements Dao<SectionType, Long>
 {
 
 	public SectionTypeDao()
 	{
		// inherit the default per request em. 
 	}
	
 	public SectionTypeDao(EntityManager em)
 	{
 		super(em);
 	}
 
 	public SectionType findByName(String name)
 	{
 		return super.findSingleBySingleParameter(SectionType.FIND_BY_NAME, "name", name);
 	}
 
 	/**
 	 * Determines which section a youth member is elidgible for.
 	 * @param birthDate
 	 * @return
 	 */
 	public SectionType getEligibleSection(DateTime birthDate)
 	{
 		SectionType eligible = null;
		
		List<SectionType> sectionTypes = findAll();
 		for (SectionType sectionType : sectionTypes)
 		{
			
 			if (this.isEligible(sectionType, birthDate))
 			{
 				eligible = sectionType;
 				break;
 			}
 		}
 		assert eligible != null : "All dates should map to a valid SectionType";
 		return eligible;
 	}
 
 	private boolean isEligible(SectionType sectionType, DateTime birthDate)
 	{
 		DateTime startingAge = sectionType.getStartingAge().getBirthDate();
 		DateTime endingAge = sectionType.getEndingAge().getBirthDate();
		return (birthDate.equals(startingAge) || birthDate.isBefore(startingAge)) 
 				&& (birthDate.isAfter(endingAge) || birthDate.equals(endingAge));
 	}
 
 	@Override
 	public JPAContainer<SectionType> makeJPAContainer()
 	{
 		return super.makeJPAContainer(SectionType.class);
 	}
 }
