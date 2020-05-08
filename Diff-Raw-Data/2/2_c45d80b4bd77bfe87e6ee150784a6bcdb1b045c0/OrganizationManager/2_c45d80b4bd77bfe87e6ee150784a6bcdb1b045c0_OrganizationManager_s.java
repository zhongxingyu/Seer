 package ca.usask.gmcte.currimap.action;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 
 import ca.usask.gmcte.currimap.model.Characteristic;
 import ca.usask.gmcte.currimap.model.CharacteristicType;
 import ca.usask.gmcte.currimap.model.Course;
 import ca.usask.gmcte.currimap.model.CourseAttribute;
 import ca.usask.gmcte.currimap.model.CourseAttributeValue;
 import ca.usask.gmcte.currimap.model.CourseOffering;
 import ca.usask.gmcte.currimap.model.CourseOutcome;
 import ca.usask.gmcte.currimap.model.InstructorAttribute;
 import ca.usask.gmcte.currimap.model.InstructorAttributeValue;
 import ca.usask.gmcte.currimap.model.LinkCourseOrganization;
 import ca.usask.gmcte.currimap.model.LinkOrganizationCharacteristicType;
 import ca.usask.gmcte.currimap.model.LinkOrganizationOrganizationOutcome;
 import ca.usask.gmcte.currimap.model.LinkProgramOutcomeOrganizationOutcome;
 import ca.usask.gmcte.currimap.model.Organization;
 import ca.usask.gmcte.currimap.model.OrganizationOutcome;
 import ca.usask.gmcte.currimap.model.OrganizationOutcomeGroup;
 import ca.usask.gmcte.currimap.model.Program;
 import ca.usask.gmcte.currimap.model.ProgramOutcome;
 import ca.usask.gmcte.currimap.model.to.ObjectPair;
 import ca.usask.gmcte.util.HibernateUtil;
 
 public class OrganizationManager
 {
 	private static OrganizationManager instance;
 	private static Logger logger = Logger.getLogger(OrganizationManager.class);
 
 	public boolean save(String name, int parentId,String systemName)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		Organization o = new Organization();
 		o.setName(name);
 		o.setActive("Y");
 		o.setSystemName(systemName);
 		if(parentId > -1)
 		{
 			Organization parent = (Organization) session.get(Organization.class,parentId);
 			o.setParentOrganization(parent);
 		}
 		session.save(o);
 		CharacteristicType defaultType = CharacteristicManager.instance().getFirstCharacteristicType(session);
 		if(defaultType !=null)
 		{
 			LinkOrganizationCharacteristicType link = new LinkOrganizationCharacteristicType();
 			link.setOrganization(o);
 			link.setCharacteristicType(defaultType);	
 			session.save(link);
 		}
 		
 		session.getTransaction().commit();
 		return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 
 	public boolean update(String id, String name, String systemName, String active,int parentId,int oldParentId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 
 
 			Organization o = (Organization) session.get(Organization.class,	Integer.parseInt(id));
			if(oldParentId > -1 && oldParentId != parentId)
 			{
 				Organization parent = (Organization) session.get(Organization.class, parentId);
 				o.setParentOrganization(parent);
 			}
 			o.setName(name);
 			o.setActive(active);
 			o.setSystemName(systemName);
 			session.merge(o);
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public boolean deleteOrganization(String id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 	
 			Organization o = (Organization) session.get(Organization.class, Integer.parseInt(id));
 			session.delete(o);
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public boolean saveOrganizationOutcomeGroupNameById(String value, int organizationOutcomeGroupId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			OrganizationOutcomeGroup o = (OrganizationOutcomeGroup) session.get(OrganizationOutcomeGroup.class,organizationOutcomeGroupId);
 			o.setName(value);
 			session.merge(o);
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	
 	public boolean saveNewOrganizationOutcomeNameAndOrganization(String value, int organizationId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			OrganizationOutcomeGroup o = new OrganizationOutcomeGroup();
 			o.setOrganizationSpecific( organizationId < 0? "N": "Y");
 			o.setOrganizationId(organizationId);
 			o.setName(value);
 			session.save(o);
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public boolean saveOrganizationOutcomeNameById(String value, int organizationOutcomeId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			OrganizationOutcome o = (OrganizationOutcome) session.get(OrganizationOutcome.class,organizationOutcomeId);
 			o.setName(value);
 			session.merge(o);
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public Organization getOrganizationByName(String name)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Organization c = null;
 		try
 		{
 			c = (Organization) session.createQuery("FROM Organization WHERE name=:name").setParameter("name",name).uniqueResult();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return c;
 	}
 	public boolean saveNewOrganizationOutcomeNameAndGroup(String value, int organizationOutcomeGroupId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			OrganizationOutcome o = new OrganizationOutcome();
 			OrganizationOutcomeGroup group = (OrganizationOutcomeGroup) session.get(OrganizationOutcomeGroup.class,organizationOutcomeGroupId);
 			o.setGroup(group);
 			o.setDescription("");
 			o.setName(value);
 			session.save(o);
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public boolean saveOrganizationOutcomeDescriptionById(String value, int organizationOutcomeId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			OrganizationOutcome o = (OrganizationOutcome) session.get(OrganizationOutcome.class,organizationOutcomeId);
 			o.setDescription(value);
 			session.merge(o);
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public Organization getOrganizationById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Organization o = null;
 		try
 		{
 			o = (Organization) session.get(Organization.class, id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return o;
 	}
 	@SuppressWarnings("unchecked")
 	public List<InstructorAttribute> getInstructorAttributes(Organization o)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<InstructorAttribute> toReturn = null;
 		try
 		{
 			toReturn = (List<InstructorAttribute>) session.createQuery("FROM InstructorAttribute WHERE organization.id=:orgId ORDER BY name").setParameter("orgId",o.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 
 	public TreeMap<String, ObjectPair> getOrganizationOfferings(Organization o)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		TreeMap<String, ObjectPair> toReturn = new TreeMap<String, ObjectPair>();
 		try
 		{
 			List<CourseOffering> courseOfferings = getOrganizationOfferings(o,session);
 			for(CourseOffering offering: courseOfferings)
 			{
 				String display = offering.getFullDisplay();
 				Boolean[] completion = CourseManager.instance().completedRecord(offering, session);
 				toReturn.put(display, new ObjectPair(offering,completion));
 			}
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<CourseOffering> getOrganizationOfferings(Organization o, Session session)
 	{
 		return (List<CourseOffering>) session.createQuery("FROM CourseOffering co WHERE co.course.id in (SELECT l.course.id FROM LinkCourseOrganization l WHERE l.organization.id = :orgId) order by co.course.subject, co.course.courseNumber, co.term, co.sectionNumber")
 					.setParameter("orgId",o.getId()).list();
 	}
 	
 	
 	public boolean addAttribute(Organization o, String toAdd)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		
 		InstructorAttribute newA = new InstructorAttribute();
 		newA.setName(toAdd);
 		newA.setOrganization(o);
 		session.save(newA);
 				session.getTransaction().commit();
 		return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public boolean removeAttribute(int toRemoveId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		@SuppressWarnings("unchecked")
 		List<InstructorAttributeValue> existing = (List<InstructorAttributeValue>)session.createQuery("FROM InstructorAttributeValue WHERE attribute.id = :attrId" ).setParameter("attrId",toRemoveId).list();
 		for(InstructorAttributeValue toDel : existing)
 		{
 			session.delete(toDel);
 		}
 		InstructorAttribute o = (InstructorAttribute)session.get(InstructorAttribute.class,toRemoveId);
 		session.delete(o);
 				session.getTransaction().commit();
 		return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public boolean removeAttributeValue(int toRemoveId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		InstructorAttributeValue o = (InstructorAttributeValue)session.get(InstructorAttributeValue.class,toRemoveId);
 		session.delete(o);
 				session.getTransaction().commit();
 		return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Course> getAllCourses(Organization o)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Course> toReturn = null;
 		try
 		{
 			toReturn = (List<Course>) session.createQuery("SELECT distinct l.course FROM LinkCourseProgram l WHERE l.program.organization.id=:orgId ORDER BY l.course.subject, l.course.courseNumber").setParameter("orgId",o.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<CourseAttribute> getCourseAttributes(Organization o)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CourseAttribute> toReturn = null;
 		try
 		{
 			toReturn = (List<CourseAttribute>) session.createQuery("FROM CourseAttribute WHERE organization.id=:orgId ORDER BY name").setParameter("orgId",o.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public boolean addCourseAttribute(Organization o, String toAdd)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		
 		CourseAttribute newA = new CourseAttribute();
 		newA.setName(toAdd);
 		newA.setOrganization(o);
 		session.save(newA);
 				session.getTransaction().commit();
 		return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public boolean removeCourseAttribute(int toRemoveId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		@SuppressWarnings("unchecked")
 		List<CourseAttributeValue> existing = (List<CourseAttributeValue>)session.createQuery("FROM CourseAttributeValue WHERE attribute.id = :attrId" ).setParameter("attrId",toRemoveId).list();
 		for(CourseAttributeValue toDel : existing)
 		{
 			session.delete(toDel);
 		}
 		CourseAttribute o = (CourseAttribute)session.get(CourseAttribute.class,toRemoveId);
 		session.delete(o);
 				session.getTransaction().commit();
 		return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public boolean removeCourseAttributeValue(int toRemoveId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		CourseAttributeValue o = (CourseAttributeValue)session.get(CourseAttributeValue.class,toRemoveId);
 		session.delete(o);
 				session.getTransaction().commit();
 		return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	
 	
 	public Organization getOrganizationByProgram(Program p)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Organization o = null;
 		try
 		{
 			o = (Organization) session.createQuery(
 				"select p.organization from Program p where p.id=:programId").setParameter("programId",p.getId()).uniqueResult();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return o;
 	}
 	public Organization getOrganizationByProgramId(String programId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Organization o = null;
 		try
 		{
 			o = (Organization) session.createQuery(
 				"select p.organization from Program p where p.id=:programId").setParameter("programId",Integer.parseInt(programId)).uniqueResult();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return o;
 	}
 
 	@SuppressWarnings("unchecked")
 	public TreeMap<Organization, ArrayList<Organization>> getOrganizationsOrderedByName()
 	{
 		TreeMap<Organization, ArrayList<Organization>> toReturn = new TreeMap<Organization,ArrayList<Organization>>();
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			List<Organization> list = (List<Organization>) session.createQuery(
 					"from Organization WHERE parentOrganization is null order by name").list();
 			for(Organization o : list)
 			{
 				ArrayList<Organization> children =  getChildrenForParentOrganization(o, session);
 				toReturn.put(o, children);
 			}
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<Organization> getParentOrganizationsOrderedByName(boolean activeOnly)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Organization> list = null;
 		try
 		{
 			String query = activeOnly?"from Organization WHERE parentOrganization is null AND active='Y' order by name":
 			"from Organization WHERE parentOrganization is null order by name";
 			list = (List<Organization>) session.createQuery(query).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return list;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Organization> getAllOrganizations(boolean activeOnly)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Organization> list = null;
 		try
 		{
 			String query = activeOnly?"from Organization WHERE active='Y' order by lower(name)":"from Organization order by lower(name)";
 			list = (List<Organization>) session.createQuery(query).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return list;
 	}
 	@SuppressWarnings("unchecked")
 	public List<Organization> getChildOrganizationsOrderedByName(Organization o, boolean activeOnly)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Organization> list = null;
 		try
 		{
 			String query=activeOnly ? "from Organization WHERE parentOrganization.id=:orgId AND active='Y' order by name" : "from Organization WHERE parentOrganization.id=:orgId order by name";
 			list = (List<Organization>) session
 					.createQuery(query)
 					.setParameter("orgId",o.getId())
 					.list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return list;
 	}
 	
 	public boolean removeOrganizationOutcome(int outcomeLinkId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			LinkOrganizationOrganizationOutcome l = (LinkOrganizationOrganizationOutcome) session.get(LinkOrganizationOrganizationOutcome.class, outcomeLinkId);
 			session.delete(l);
 			session.getTransaction().commit();
 		return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public boolean removeLinkProgramOutcomeOrganizationOutcome(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			LinkProgramOutcomeOrganizationOutcome l = (LinkProgramOutcomeOrganizationOutcome) session.get(LinkProgramOutcomeOrganizationOutcome.class, id);
 			session.delete(l);
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public boolean saveProgramOutcomeOrganizationOutcome(int outcomeId, int organizationOutcomeId,int programId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			LinkProgramOutcomeOrganizationOutcome o = new LinkProgramOutcomeOrganizationOutcome();
 			
 			ProgramOutcome outcome = (ProgramOutcome) session.get(ProgramOutcome.class,outcomeId);
 			OrganizationOutcome oOutcome  = (OrganizationOutcome)session.get(OrganizationOutcome.class,organizationOutcomeId);
 			Program program  = (Program)session.get(Program.class,programId);
 			o.setProgram(program);
 			o.setProgramOutcome(outcome);
 			o.setOrganizationOutcome(oOutcome);
 			session.save(o);
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public OrganizationOutcome getOrganizationOutcomeById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		OrganizationOutcome o = null;
 		try
 		{
 			o = (OrganizationOutcome) session.get(OrganizationOutcome.class, id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return o;
 	}
 	
 	public OrganizationOutcomeGroup getOrganizationOutcomeGroupById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		OrganizationOutcomeGroup o = null;
 		try
 		{
 			o = (OrganizationOutcomeGroup) session.get(OrganizationOutcomeGroup.class, id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return o;
 	}
 	@SuppressWarnings("unchecked")
 	public List<OrganizationOutcome> getOrganizationOutcomesForOrg(Organization o)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<OrganizationOutcome> toReturn = null;
 		try
 		{
 			toReturn = (List<OrganizationOutcome>)session.createQuery("FROM OrganizationOutcome o WHERE (o.group.organizationSpecific = 'Y' AND o.group.organizationId = :orgId) OR (o.group.organizationSpecific = 'N') order by o.group.name, o.name").setParameter("orgId",o.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<OrganizationOutcome> getOrganizationOutcomeForGroup(OrganizationOutcomeGroup group)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<OrganizationOutcome> toReturn = null;
 		try
 		{
 			toReturn = (List<OrganizationOutcome>)session.createQuery("FROM OrganizationOutcome o WHERE o.group.id = :groupId order by o.group.name, o.name").setParameter("groupId",group.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<OrganizationOutcomeGroup> getOrganizationOutcomeGroupsForOrg(Organization o)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<OrganizationOutcomeGroup> toReturn = null;
 		try
 		{
 			toReturn = (List<OrganizationOutcomeGroup>)session.createQuery("FROM OrganizationOutcomeGroup o WHERE (o.organizationSpecific = 'Y' AND o.organizationId = :orgId) OR (o.organizationSpecific = 'N') order by o.organizationSpecific DESC, o.name").setParameter("orgId",o.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<OrganizationOutcomeGroup> getOrganizationOutcomeGroupsForOrgForDelete(Organization o)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<OrganizationOutcomeGroup> toReturn = null;
 		try
 		{
 			toReturn = (List<OrganizationOutcomeGroup>)session.createQuery("FROM OrganizationOutcomeGroup o WHERE o.organizationSpecific = 'Y' AND o.organizationId = :orgId").setParameter("orgId",o.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<LinkOrganizationOrganizationOutcome> getLinkOrganizationOrganizationOutcomeForOrg(Organization o)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<LinkOrganizationOrganizationOutcome> toReturn = null;
 		try
 		{
 			toReturn = (List<LinkOrganizationOrganizationOutcome>)session.createQuery("from LinkOrganizationOrganizationOutcome l where l.organization.id = :orgId order by l.organizationOutcome.group.name, l.organizationOutcome.name").setParameter("orgId",o.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<OrganizationOutcomeGroup> getOrganizationOutcomeGroupsOrganization(Organization o)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<OrganizationOutcomeGroup> toReturn = null;
 		try
 		{
 			toReturn = (List<OrganizationOutcomeGroup>)session.createQuery("SELECT distinct l.organizationOutcome.group from LinkOrganizationOrganizationOutcome l where l.organization.id = :orgId order by l.organizationOutcome.group.name").setParameter("orgId",o.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<LinkOrganizationOrganizationOutcome> getOrganizationOutcomeForGroupAndOrganization(Organization o,OrganizationOutcomeGroup group )
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<LinkOrganizationOrganizationOutcome> toReturn = null;
 		try
 		{
 			toReturn = (List<LinkOrganizationOrganizationOutcome>)session
 			.createQuery("from LinkOrganizationOrganizationOutcome l where l.organization.id = :orgId AND l.organizationOutcome.group.id = :groupId order by l.organizationOutcome.name")
 			.setParameter("orgId",o.getId())
 			.setParameter("groupId",group.getId())
 			.list();
 		session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public ArrayList<Organization> getChildrenForParentOrganization(Organization o, Session session)
 	{
 		return (ArrayList<Organization>)session.createQuery("FROM Organization where parentOrganization.id = :organizationId ORDER BY name").setParameter("organizationId",o.getId()).list();
 	}
 	@SuppressWarnings("unchecked")
 	public List<LinkProgramOutcomeOrganizationOutcome> getProgramOutcomeLinksForOrganizationOutcome(Program program, OrganizationOutcome oo)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<LinkProgramOutcomeOrganizationOutcome> toReturn = null;
 		try
 		{
 			toReturn = (List<LinkProgramOutcomeOrganizationOutcome>)session.createQuery("from LinkProgramOutcomeOrganizationOutcome l where l.organizationOutcome.id = :orgOutcomeId AND l.program.id=:programId order by l.programOutcome.group.name, l.programOutcome.name")
 				.setParameter("orgOutcomeId",oo.getId())
 				.setParameter("programId",program.getId())
 				.list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<CharacteristicType> getOrganizationCharacteristicTypes(Organization o)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CharacteristicType> toReturn = null;
 		try
 		{
 			toReturn = (List<CharacteristicType>)session
 			.createQuery("select ct from CharacteristicType ct, LinkOrganizationCharacteristicType ldct where ldct.organization.id=:orgId and ldct.characteristicType = ct order by ldct.displayIndex")
 			.setParameter("orgId",o.getId())
 			.list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<CharacteristicType> getCandidateCharacteristicTypes(List<Integer> alreadyUsed)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CharacteristicType> toReturn = null;
 		try
 		{
 			StringBuilder queryString = new StringBuilder("select ct from CharacteristicType ct ");
 			if(alreadyUsed.size()>0)
 			{
 				queryString.append("where ct.id not in (");
 				queryString.append(inString(alreadyUsed));
 				queryString.append(") ");
 			}
 			queryString.append(" order by ct.name");
 			toReturn = (List<CharacteristicType>)session.createQuery(queryString.toString()).list();
 			session.getTransaction().commit();
 			}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public boolean addCharacteristicToOrganization(int charId, int deptId)
 	{
 		boolean createSuccessful = false;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			Organization o = (Organization) session.get(Organization.class, deptId);
 			CharacteristicType cType = (CharacteristicType) session.get(CharacteristicType.class,charId);
 			LinkOrganizationCharacteristicType link  = new LinkOrganizationCharacteristicType();
 			int max = 0;
 			try
 			{
 				max = (Integer)session.createQuery("select max(displayIndex) from LinkOrganizationCharacteristicType l where l.organization.id = :orgId").setParameter("orgId",o.getId()).uniqueResult();
 			}
 			catch(Exception e)
 			{
 				logger.error("unable to determine max!",e);
 			}
 			
 			link.setDisplayIndex(max+1);
 			link.setCharacteristicType(cType);
 			link.setOrganization(o);
 			//p.getLinkProgramCharacteristicTypes().add(link);
 			session.persist(link);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		createSuccessful = true;
 		return createSuccessful;
 	}
 	public boolean moveCharacteristicType(int id, int charTypeId, String direction)
 	{
 		//when moving up, find the one to be moved (while keeping track of the previous one) and swap display_index values
 		//when moving down, find the one to be moved, swap displayIndex values of it and the next one
 		//when deleting, reduce all links following one to be deleted by 1
 		boolean done = false;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			@SuppressWarnings("unchecked")
 			List<LinkOrganizationCharacteristicType> existing = (List<LinkOrganizationCharacteristicType>)session.createQuery("select l from LinkOrganizationCharacteristicType l where l.organization.id = :orgId order by l.displayIndex").setParameter("orgId",id).list();
 			if(direction.equals("up"))
 			{
 				LinkOrganizationCharacteristicType prev = null;
 				for(LinkOrganizationCharacteristicType link : existing)
 				{
 					if(link.getCharacteristicType().getId() == charTypeId && prev!=null)
 					{
 						int swap = prev.getDisplayIndex();
 						prev.setDisplayIndex(link.getDisplayIndex());
 						link.setDisplayIndex(swap);
 						session.merge(prev);
 						session.merge(prev);
 						done = true;
 						break;
 					}
 					prev = link;
 				}
 			}
 			else if(direction.equals("down"))
 			{
 				LinkOrganizationCharacteristicType prev = null;
 				for(LinkOrganizationCharacteristicType link : existing)
 				{
 					if(prev !=null)
 					{
 						int swap = prev.getDisplayIndex();
 						prev.setDisplayIndex(link.getDisplayIndex());
 						link.setDisplayIndex(swap);
 						session.merge(prev);
 						session.merge(link);
 						done = true;
 						break;
 					}
 					if(link.getCharacteristicType().getId() == charTypeId)
 					{
 						prev = link;
 					}
 					
 				}
 			}
 			else if(direction.equals("delete"))
 			{
 				LinkOrganizationCharacteristicType toDelete = null;
 				for(LinkOrganizationCharacteristicType link : existing)
 				{
 					if(toDelete !=null)
 					{
 						link.setDisplayIndex(link.getDisplayIndex()-1);
 						session.merge(link);
 					}
 					if(link.getCharacteristicType().getId() == charTypeId)
 					{
 						toDelete = link;
 					}
 					
 				}
 				if(toDelete !=null)
 				{
 						session.delete(toDelete);
 						done = true;
 				}
 			}
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 		return done;
 	}
 	@SuppressWarnings("unchecked")
 	public List<CharacteristicType> setOrganizationCharacteristicTypes(Organization o)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CharacteristicType> toReturn = null;
 		try
 		{
 			toReturn = (List<CharacteristicType>)session.createQuery("select ct from CharacteristicType ct, LinkOrganizationCharacteristicType lpct where lpct.organization.id=:orgId and lpct.characteristicType = ct  order by lpct.displayIndex")
 					.setParameter("orgId",o.getId()).list();
 			o.setCharacteristicTypes(toReturn);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<Characteristic> getCharacteristicsForType(CharacteristicType ct)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Characteristic> toReturn = null;
 		try
 		{
 			toReturn = (List<Characteristic>)session.createQuery("select c from Characteristic c where c.characteristicType.id = :ctId order by c.displayIndex").setParameter("ctId",ct.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<Program> getProgramOrderedByNameForOrganizationLinkedToCourse(Organization org, Course course)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Program> list = null;
 		try
 		{
 			list = (List<Program>) session.createQuery("FROM Program p WHERE p.organization.id=:orgId and p.id in (SELECT l.program.id FROM LinkCourseProgram l WHERE l.course.id=:courseId) order by lower(name)")
 					.setParameter("orgId",org.getId())
 					.setParameter("courseId", course.getId())
 					.list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return list;
 	}
 	
 	public boolean addCourseToOrganization(String subject, String courseNumber, int organizationId)
 	{
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			Organization o = (Organization) session.get(Organization.class, organizationId);
 			 Course course = CourseManager.instance().getCourseBySubjectAndNumber(subject, courseNumber,session);
 			 LinkCourseOrganization newLink = new LinkCourseOrganization();
 			 newLink.setCourse(course);
 			 newLink.setOrganization(o);
 			 session.save(newLink);
 			 session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 
 	}
 	public boolean removeCourseFromOrganization(String subject, String courseNumber, int organizationId)
 	{
 		
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			 Course course = CourseManager.instance().getCourseBySubjectAndNumber(subject, courseNumber,session);
 			 LinkCourseOrganization toDelete = (LinkCourseOrganization)session.createQuery("FROM LinkCourseOrganization WHERE organization.id=:orgId and course.id=:courseId")
 					 .setParameter("orgId",organizationId)
 					 .setParameter("courseId",course.getId())
 					 .uniqueResult();
 			 
 			 session.delete(toDelete);
 			 session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Program> getProgramOrderedByNameForOrganization(Organization o)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Program> list = null;
 		try
 		{
 			list = (List<Program>) session.createQuery("FROM Program p WHERE p.organization.id=:orgId order by lower(name)").setParameter("orgId",o.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return list;
 	}
 	public CourseOutcome getCourseOutcomeById(int id)
     {
             Session session = HibernateUtil.getSessionFactory().getCurrentSession();
             session.beginTransaction();
             CourseOutcome c = null;
             try
             {
                     c = (CourseOutcome) session.get(CourseOutcome.class, id);
                     session.getTransaction().commit();
             }
             catch(Exception e)
             {
                     HibernateUtil.logException(logger, e);
             }
             return c;
     }
 	public boolean saveCourseOutcomeValue(CourseOutcome o , String name)
     {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
             session.beginTransaction();
             try
             {
             	o.setName(name);
             	session.merge(o);
                     session.getTransaction().commit();
             	return true;
             }
             catch(Exception e)
             {
                     HibernateUtil.logException(logger, e);
                     try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
                     return false;
             }
     }
 
 	public OrganizationManager()
 	{
 
 	}
 
 	public static OrganizationManager instance()
 	{
 		if (instance == null)
 		{
 			instance = new OrganizationManager();
 		}
 		return instance;
 
 	}
 	public String inString(List<Integer> in)
 	{
 		StringBuilder sb = new StringBuilder();
 		for(int i : in)
 		{
 			sb.append(",");
 			sb.append(i);
 			
 		}
 		return sb.substring(1);
 	}
 
 
 }
