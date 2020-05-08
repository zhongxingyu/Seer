 package ca.usask.gmcte.currimap.action;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.transform.Transformers;
 
 import ca.usask.gmcte.currimap.model.Characteristic;
 import ca.usask.gmcte.currimap.model.CharacteristicType;
 import ca.usask.gmcte.currimap.model.ContributionOptionValue;
 import ca.usask.gmcte.currimap.model.Course;
 import ca.usask.gmcte.currimap.model.CourseClassification;
 import ca.usask.gmcte.currimap.model.CourseOffering;
 import ca.usask.gmcte.currimap.model.CourseOutcome;
 import ca.usask.gmcte.currimap.model.Department;
 import ca.usask.gmcte.currimap.model.LinkCourseContributionProgramOutcome;
 import ca.usask.gmcte.currimap.model.LinkCourseOfferingContributionProgramOutcome;
 import ca.usask.gmcte.currimap.model.LinkCourseOutcomeProgramOutcome;
 import ca.usask.gmcte.currimap.model.LinkCourseProgram;
 import ca.usask.gmcte.currimap.model.LinkDepartmentCharacteristicType;
 import ca.usask.gmcte.currimap.model.LinkProgramProgramOutcome;
 import ca.usask.gmcte.currimap.model.MasteryOptionValue;
 import ca.usask.gmcte.currimap.model.Organization;
 import ca.usask.gmcte.currimap.model.Program;
 import ca.usask.gmcte.currimap.model.ProgramAdmin;
 import ca.usask.gmcte.currimap.model.ProgramOutcome;
 import ca.usask.gmcte.currimap.model.ProgramOutcomeGroup;
 import ca.usask.gmcte.currimap.model.Time;
 import ca.usask.gmcte.currimap.model.to.CourseAssessmentOption;
 import ca.usask.gmcte.currimap.model.to.CourseOfferingContribution;
 import ca.usask.gmcte.currimap.model.to.CourseTeachingMethodOption;
 import ca.usask.gmcte.currimap.model.to.Pair;
 import ca.usask.gmcte.currimap.model.to.ProgramOutcomeCourseContribution;
 import ca.usask.gmcte.util.HibernateUtil;
 
 public class ProgramManager
 {
 	private static ProgramManager instance;
 	private static Logger logger = Logger.getLogger(ProgramManager.class);
 
 	public boolean save(String name,String description, String organizationId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		/** Creating Pojo */
 			Organization organization = (Organization) session.get(Organization.class,
 					Integer.parseInt(organizationId));
 			
 			Program o = new Program();
 			o.setOrganization(organization);
 			o.setName(name);
 			o.setDescription(description);
 			
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
 	
 	public boolean saveProgramOutcomeGroupNameById(String value, int programOutcomeGroupId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			ProgramOutcomeGroup o = (ProgramOutcomeGroup) session.get(ProgramOutcomeGroup.class,programOutcomeGroupId);
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
 	
 	public boolean saveNewProgramOutcomeNameAndProgram(String value, int programId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			ProgramOutcomeGroup o = new ProgramOutcomeGroup();
 			o.setProgramSpecific( programId < 0? "N": "Y");
 			o.setProgramId(programId);
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
 	public boolean saveProgramOutcomeNameById(String value, int programOutcomeId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			ProgramOutcome o = (ProgramOutcome) session.get(ProgramOutcome.class,programOutcomeId);
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
 	
 	public boolean saveNewProgramOutcomeNameAndGroup(String value, int programOutcomeGroupId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			ProgramOutcome o = new ProgramOutcome();
 			ProgramOutcomeGroup group = (ProgramOutcomeGroup) session.get(ProgramOutcomeGroup.class,programOutcomeGroupId);
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
 	public boolean saveProgramOutcomeDescriptionById(String value, int programOutcomeId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			ProgramOutcome o = (ProgramOutcome) session.get(ProgramOutcome.class,programOutcomeId);
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
 
 	
 	public boolean saveCourseOutcomeProgramOutcome(int outcomeId, int programOutcomeId,int courseOfferingId, int existingLinkId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			
 			ProgramOutcome pOutcome  = (ProgramOutcome)session.get(ProgramOutcome.class,programOutcomeId);
 			CourseOffering courseOffering  = (CourseOffering)session.get(CourseOffering.class,courseOfferingId);
 			CourseOutcome outcome = (CourseOutcome) session.get(CourseOutcome.class,outcomeId);
 			
 			LinkCourseOutcomeProgramOutcome o = null;
 			if(existingLinkId > -1) // need to update or delete
 			{
 				o = (LinkCourseOutcomeProgramOutcome) session.get(LinkCourseOutcomeProgramOutcome.class,existingLinkId);
 				if(outcomeId > -1)
 				{
 					o.setCourseOutcome(outcome);
 					session.merge(o);
 					
 				}
 				else
 					session.delete(o); // contribution is invalid (or 0)  Delete it 
 				
 			}
 			else
 			{
 				//need to create a new one
 				o = new LinkCourseOutcomeProgramOutcome();
 				o.setCourseOffering(courseOffering);
 				o.setCourseOutcome(outcome);
 				o.setProgramOutcome(pOutcome);
 				session.save(o);
 			}
 			session.getTransaction().commit();
 			return true;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			logger.error("Oops "+outcomeId+ " "+programOutcomeId+" "+courseOfferingId,e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	public boolean update(String id, String name, String description)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 
 		Program o = (Program) session.get(Program.class,
 				Integer.parseInt(id));
 		o.setName(name);
 		o.setDescription(description);
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
 	public boolean saveLinkCourseProgram(int courseId, int programId,int classification, int time)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		/** Creating Pojo */
 		CourseClassification cl = (CourseClassification) session.get(CourseClassification.class,
 				classification);
 		Program p = (Program) session.get(Program.class, programId);
 		Course c = (Course) session.get(Course.class,courseId);
 		Time min = (Time) session.get(Time.class, time);
 		LinkCourseProgram l = new LinkCourseProgram();
 		l.setCourse(c);
 		l.setCourseClassification(cl);
 		l.setTime(min);
 		l.setProgram(p);
 		session.save(l);
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
 	public boolean updateLinkCourseProgram(int id, int classification, int time)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		LinkCourseProgram l = (LinkCourseProgram) session.get(LinkCourseProgram.class, id);
 		CourseClassification cl = (CourseClassification) session.get(CourseClassification.class,
 				classification);
 		Time min = (Time) session.get(Time.class, time);
 		l.setCourseClassification(cl);
 		l.setTime(min);
 		session.merge(l);
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
 	public boolean removeProgramCourse(int courseLinkId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		LinkCourseProgram l = (LinkCourseProgram) session.get(LinkCourseProgram.class, courseLinkId);
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
 
 	public boolean removeLinkCourseOutcomeProgramOutcome(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			LinkCourseOutcomeProgramOutcome l = (LinkCourseOutcomeProgramOutcome) session.get(LinkCourseOutcomeProgramOutcome.class, id);
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
 	public boolean removeProgram(int programId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		Program p = (Program) session.get(Program.class, programId);
 		List<LinkProgramProgramOutcome> outcomeLinks = p.getLinkProgramOutcomes();
 		for(LinkProgramProgramOutcome link: outcomeLinks)
 		{
 			session.delete(link);
 		}
 		List<LinkCourseProgram> courseLinks = p.getLinkCoursePrograms();
 		for(LinkCourseProgram link: courseLinks)
 		{
 			session.delete(link);
 		}
 		/*List<LinkDepartmentCharacteristicType> characteristicLinks = p.getLinkProgramCharacteristicTypes();
 		for(LinkDepartmentCharacteristicType link: characteristicLinks)
 		{
 			session.delete(link);
 		}*/
 		@SuppressWarnings("unchecked")
 		List<ProgramAdmin> adminLinks = session.createQuery("FROM ProgramAdmin WHERE program.id = :programId").setParameter("programId",p.getId()).list();
 		for(ProgramAdmin link: adminLinks)
 		{
 			session.delete(link);
 		}
 		session.delete(p);
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
 	public boolean removeProgramOutcome(int outcomeLinkId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		LinkProgramProgramOutcome l = (LinkProgramProgramOutcome) session.get(LinkProgramProgramOutcome.class, outcomeLinkId);
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
 	
 	public Program getProgramById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Program o = null;
 		try
 		{
 			o = (Program) session.get(Program.class, id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return o;
 	}
 	@SuppressWarnings("unchecked")
 	public List<String> getAllAvailableTerms(Program p)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<String> toReturn = null;
 		try
 		{
 			toReturn = (List<String>) session.createQuery("SELECT DISTINCT co.term FROM CourseOffering co WHERE co.course IN (SELECT lcp.course FROM LinkCourseProgram lcp WHERE lcp.program.id=:programId) ORDER BY  co.term")
 					.setParameter("programId", p.getId())
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
 	public List<ContributionOptionValue> 	getContributionOptions()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<ContributionOptionValue> toReturn = null;
 		try
 		{
 			toReturn = (List<ContributionOptionValue>) session.createQuery("FROM ContributionOptionValue ORDER BY lower(displayIndex)").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<MasteryOptionValue> 	getMasteryOptions()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<MasteryOptionValue> toReturn = null;
 		try
 		{
 			toReturn = (List<MasteryOptionValue>) session.createQuery("FROM MasteryOptionValue ORDER BY lower(displayIndex)").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public ProgramOutcome getProgramOutcomeById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		ProgramOutcome o = null;
 		try
 		{
 			o = (ProgramOutcome) session.get(ProgramOutcome.class, id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return o;
 	}
 	
 	public ProgramOutcomeGroup getProgramOutcomeGroupById(int id)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		ProgramOutcomeGroup o = null;
 		try
 		{
 			o = (ProgramOutcomeGroup) session.get(ProgramOutcomeGroup.class, id);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return o;
 	}
 	@SuppressWarnings("unchecked")
 	public List<Program> getAllPrograms()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Program> toReturn = null;
 		try
 		{
 			toReturn = (List<Program>) session.createQuery("FROM Program ORDER BY lower(name)").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<CharacteristicType> getProgramCharacteristicTypes(Program p)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CharacteristicType> toReturn = null;
 		try
 		{
 			toReturn = (List<CharacteristicType>)session.createQuery("select ct from CharacteristicType ct, LinkProgramCharacteristicType lpct where lpct.program.id=:programId and lpct.characteristicType = ct order by lpct.displayIndex").setParameter("programId",p.getId()).list();
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
 	public boolean addCharacteristicToDepartment(int charId, int deptId)
 	{
 		boolean createSuccessful = false;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		Department d = (Department) session.get(Department.class, deptId);
 		CharacteristicType cType = (CharacteristicType) session.get(CharacteristicType.class,charId);
 		LinkDepartmentCharacteristicType link  = new LinkDepartmentCharacteristicType();
 		int max = 0;
 		try
 		{
 			max = (Integer)session.createQuery("select max(displayIndex) from LinkDepartmentCharacteristicType l where l.department.id = :deptId").setParameter("deptId",d.getId()).uniqueResult();
 		}
 		catch(Exception e)
 		{
 			
 			logger.error("unable to determine max!",e);
 		}
 		
 		link.setDisplayIndex(max+1);
 		link.setCharacteristicType(cType);
 		link.setDepartment(d);
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
 		List<LinkDepartmentCharacteristicType> existing = (List<LinkDepartmentCharacteristicType>)session.createQuery("select l from LinkProgramCharacteristicType l where l.program.id = :programId order by l.displayIndex").setParameter("programId",id).list();
 		if(direction.equals("up"))
 		{
 			LinkDepartmentCharacteristicType prev = null;
 			for(LinkDepartmentCharacteristicType link : existing)
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
 			LinkDepartmentCharacteristicType prev = null;
 			for(LinkDepartmentCharacteristicType link : existing)
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
 			LinkDepartmentCharacteristicType toDelete = null;
 			for(LinkDepartmentCharacteristicType link : existing)
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
 		return done;
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			try{session.getTransaction().rollback();}catch(Exception e2){logger.error("Unable to roll back!",e2);}
 			return false;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<CharacteristicType> setDepartmentCharacteristicTypes(Department d)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CharacteristicType> toReturn = null;
 		try
 		{
 			toReturn = (List<CharacteristicType>)session.createQuery("select ct from CharacteristicType ct, LinkDepartmentCharacteristicType lpct where lpct.department.id=:deptId and lpct.characteristicType = ct  order by lpct.displayIndex")
 					.setParameter("deptId",d.getId()).list();
 			d.setCharacteristicTypes(toReturn);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Characteristic> getProgramCharacteristics(Program p, int level)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Characteristic> toReturn = null;
 		try
 		{
 			toReturn = (List<Characteristic>)session.createQuery("select c from Characteristic c, CharacteristicType ct, LinkProgramCharacteristicType lpct where lpct.program.id=:programId and lpct.displayIndex=:index and lpct.characteristicType = ct and c.characteristicType = ct order by c.displayIndex").setParameter("programId",p.getId()).setParameter("index",level).list();
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
 	public List<Program> getProgramOrderedByName()
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Program> list = null;
 		try
 		{
 			list = (List<Program>) session.createQuery("from Program order by name").list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return list;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<LinkProgramProgramOutcome> getLinkProgramOutcomeForProgram(Program p)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<LinkProgramProgramOutcome> toReturn = null;
 		try
 		{
 			toReturn = (List<LinkProgramProgramOutcome>)session.createQuery("from LinkProgramProgramOutcome l where l.program.id = :programId order by l.programOutcome.group.name, l.programOutcome.name").setParameter("programId",p.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<ProgramOutcomeGroup> getProgramOutcomeGroupsProgram(Program p)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<ProgramOutcomeGroup> toReturn = null;
 		try
 		{
 			toReturn = (List<ProgramOutcomeGroup>)session.createQuery("SELECT distinct l.programOutcome.group from LinkProgramProgramOutcome l where l.program.id = :programId order by l.programOutcome.group.name").setParameter("programId",p.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<ProgramOutcome> getProgramOutcomesForGroup(ProgramOutcomeGroup group)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<ProgramOutcome> toReturn = null;
 		try
 		{
 			toReturn = (List<ProgramOutcome>)session.createQuery("from ProgramOutcome l where l.group.id=:groupId order by l.name").setParameter("groupId",group.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<LinkProgramProgramOutcome> getProgramOutcomeForGroupAndProgram(Program p,ProgramOutcomeGroup group )
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<LinkProgramProgramOutcome> toReturn = null;
 		try
 		{
 			toReturn = (List<LinkProgramProgramOutcome>)session
 			.createQuery("from LinkProgramProgramOutcome l where l.program.id = :programId AND l.programOutcome.group.id = :groupId order by l.programOutcome.name")
 			.setParameter("programId",p.getId())
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
 	public List<LinkCourseProgram> getLinkCourseProgramForProgram(Program p)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<LinkCourseProgram> toReturn = null;
 		try
 		{
 			toReturn = (List<LinkCourseProgram>)session.createQuery("from LinkCourseProgram l where l.program.id = :programId order by l.time.displayIndex, l.course.subject, l.course.courseNumber").setParameter("programId",p.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public TreeMap<String, Integer> getCourseOfferingsContributingToProgram(Program program)
 	{
 		return getCourseOfferingsContributingToProgram(program, new ArrayList<String>(0));
 	}
 	@SuppressWarnings("unchecked")
 	public TreeMap<String, Integer> getCourseOfferingsContributingToProgram(Program program, List<String> termList)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		TreeMap<String, Integer> toReturn = new TreeMap<String, Integer>();
 		List<CourseOffering> tempList = null;
 		try
 		{
 			String query = "SELECT DISTINCT l.courseOffering FROM LinkCourseOfferingContributionProgramOutcome l where "
 			   + HibernateUtil.getListAsString("l.term",termList,true) +" l.linkProgramOutcome.program.id = :programId AND (l.contribution.calculationValue + l.mastery.calculationValue) > 0";
 			
 			 tempList = (List<CourseOffering>)session
 					 .createQuery(query)
 					 .setParameter("programId",program.getId()).list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 			tempList = new ArrayList<CourseOffering>();
 		}
 		for(CourseOffering offering : tempList)
 		{
 			if(!termList.contains(offering.getTerm()))
 			{ //don't want the course offering if it isn't in the term-list
 				continue;
 			}
 			String course = offering.getCourse().getSubject() + " " + offering.getCourse().getCourseNumber();
 			Integer count = toReturn.get(course);
 			if(count == null)
 				count = 1;
 			else
 				count++;
 			toReturn.put(course,new Integer(count));
 		}
 		return toReturn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<LinkCourseOutcomeProgramOutcome> getCourseOutcomeLinksForProgramOutcome(CourseOffering offering, ProgramOutcome po)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<LinkCourseOutcomeProgramOutcome> toReturn = null;
 		logger.debug("OfferingId="+offering.getId()+" programOutcomeId = "+po.getId());
 		try
 		{
 			toReturn = (List<LinkCourseOutcomeProgramOutcome>)session.createQuery("from LinkCourseOutcomeProgramOutcome l where l.programOutcome.id = :programOutcomeId AND l.courseOffering.id=:courseOfferingId order by l.courseOutcome.name")
 				.setParameter("programOutcomeId",po.getId())
 				.setParameter("courseOfferingId",offering.getId())
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
 	public List<LinkCourseOutcomeProgramOutcome> getCourseOutcomeLinks(CourseOffering offering,Session session)
 	{
 		return (List<LinkCourseOutcomeProgramOutcome>)session.createQuery("from LinkCourseOutcomeProgramOutcome l where  l.courseOffering.id=:courseOfferingId")
 				.setParameter("courseOfferingId",offering.getId())
 				.list();
 	}
 	@SuppressWarnings("unchecked")
 	public List<LinkCourseOutcomeProgramOutcome> getProgramOutcomeLinksForCourseOutcome(int offeringId,int courseOutcomeId, Session session)
 	{
 		List<LinkCourseOutcomeProgramOutcome> toReturn = null;
 		logger.debug("OfferingId="+offeringId+" courseOutcomeId = "+courseOutcomeId);
 		toReturn = (List<LinkCourseOutcomeProgramOutcome>)session.createQuery("from LinkCourseOutcomeProgramOutcome l where l.courseOutcome.id = :courseOutcomeId AND l.courseOffering.id=:courseOfferingId order by l.courseOutcome.name")
 				.setParameter("courseOutcomeId",courseOutcomeId)
 				.setParameter("courseOfferingId",offeringId)
 				.list();
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<LinkCourseOutcomeProgramOutcome> getCourseOutcomeLinksForProgramOutcome(CourseOffering offering, int programOutcomeId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<LinkCourseOutcomeProgramOutcome> toReturn = null;
 		logger.debug("OfferingId="+offering.getId()+" programOutcomeId = "+programOutcomeId);
 		try
 		{
 			toReturn = (List<LinkCourseOutcomeProgramOutcome>)session.createQuery("from LinkCourseOutcomeProgramOutcome l where l.programOutcome.id = :programOutcomeId AND l.courseOffering.id=:courseOfferingId order by l.courseOutcome.id")
 				.setParameter("programOutcomeId",programOutcomeId)
 				.setParameter("courseOfferingId",offering.getId())
 				.list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}	
 	
 
 	public LinkCourseContributionProgramOutcome getCourseContributionLinksForProgramOutcome(Course course, LinkProgramProgramOutcome po)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		LinkCourseContributionProgramOutcome toReturn = null;
 		try
 		{
 			toReturn = (LinkCourseContributionProgramOutcome)session.createQuery("from LinkCourseContributionProgramOutcome l where l.linkProgramOutcome.id = :linkProgramOutcomeId AND l.course.id=:courseId")
 				.setParameter("linkProgramOutcomeId",po.getId())
 				.setParameter("courseId",course.getId())
 				.uniqueResult();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	
 	
 	public LinkCourseOfferingContributionProgramOutcome getCourseOfferingContributionLinksForProgramOutcome(CourseOffering courseOffering, LinkProgramProgramOutcome po)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		LinkCourseOfferingContributionProgramOutcome toReturn = null;
 		try
 		{
 			toReturn = (LinkCourseOfferingContributionProgramOutcome)session.createQuery("from LinkCourseOfferingContributionProgramOutcome l where l.linkProgramOutcome.id = :linkProgramOutcomeId AND l.courseOffering.id=:courseOfferingId")
 				.setParameter("linkProgramOutcomeId",po.getId())
 				.setParameter("courseOfferingId",courseOffering.getId())
 				.uniqueResult();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}	
 	
 	@SuppressWarnings("unchecked")
 	public List<LinkCourseOfferingContributionProgramOutcome> getCourseOfferingContributionLinks(CourseOffering courseOffering, Session session)
 	{
 		return  (List<LinkCourseOfferingContributionProgramOutcome>)session.createQuery("from LinkCourseOfferingContributionProgramOutcome l where l.courseOffering.id=:courseOfferingId")
 				.setParameter("courseOfferingId",courseOffering.getId())
 				.list();
 	}
 	
 	public boolean saveCourseOfferingContributionLinksForProgramOutcome(int courseOfferingId, int linkProgramOutcomeId, int contributionId, int masteryId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			MasteryOptionValue mastery = (MasteryOptionValue) session.get(MasteryOptionValue.class, masteryId);
 			ContributionOptionValue contribution = (ContributionOptionValue) session.get(ContributionOptionValue.class, contributionId);
 			CourseOffering courseOffering = (CourseOffering) session.get(CourseOffering.class, courseOfferingId);
 			LinkProgramProgramOutcome lpo = (LinkProgramProgramOutcome)session.get(LinkProgramProgramOutcome.class, linkProgramOutcomeId);
 			
 			LinkCourseOfferingContributionProgramOutcome o =  getCourseOfferingContributionLinksForProgramOutcome(courseOffering, lpo,session);
 			if(o == null)
 			{
 				o = new LinkCourseOfferingContributionProgramOutcome();
 				o.setCourseOffering(courseOffering);
 				o.setLinkProgramOutcome(lpo);
 				o.setContribution(contribution);
 				o.setMastery(mastery);
 				session.save(o);
 			}
 			else
 			{
 				o.setContribution(contribution);
 				o.setMastery(mastery);
 				session.merge(o);
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
 	public boolean saveCourseContributionLinksForProgramOutcome(int courseId, int linkProgramOutcomeId, int contributionId, int masteryId)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 			
 			ContributionOptionValue contribution = (ContributionOptionValue) session.get(ContributionOptionValue.class, contributionId);
 			MasteryOptionValue mastery = (MasteryOptionValue) session.get(MasteryOptionValue.class, masteryId);
 			Course course = (Course) session.get(Course.class, courseId);
 			LinkProgramProgramOutcome lpo = (LinkProgramProgramOutcome)session.get(LinkProgramProgramOutcome.class, linkProgramOutcomeId);
 			
 			LinkCourseContributionProgramOutcome o =  getCourseContributionLinksForProgramOutcome(course, lpo,session);
 			if(o == null)
 			{
 				o = new LinkCourseContributionProgramOutcome();
 				o.setCourse(course);
 				o.setLinkProgramOutcome(lpo);
 				o.setContribution(contribution);
 				o.setMastery(mastery);
 				session.save(o);
 			}
 			else
 			{
 				o.setContribution(contribution);
 				o.setMastery(mastery);
 				session.merge(o);
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
 	public LinkCourseContributionProgramOutcome getCourseContributionLinksForProgramOutcome(Course course, LinkProgramProgramOutcome po,Session session)
 	{
 		LinkCourseContributionProgramOutcome toReturn = null;
 			toReturn = (LinkCourseContributionProgramOutcome)session.createQuery("from LinkCourseContributionProgramOutcome l where l.linkProgramOutcome.id = :linkProgramOutcomeId AND l.course.id=:courseId")
 				.setParameter("linkProgramOutcomeId",po.getId())
 				.setParameter("courseId",course.getId())
 				.uniqueResult();
 		return toReturn;
 	}
 	public LinkCourseOfferingContributionProgramOutcome getCourseOfferingContributionLinksForProgramOutcome(CourseOffering courseOffering, LinkProgramProgramOutcome po,Session session)
 	{
 		LinkCourseOfferingContributionProgramOutcome toReturn = null;
 			toReturn = (LinkCourseOfferingContributionProgramOutcome)session.createQuery("from LinkCourseOfferingContributionProgramOutcome l where l.linkProgramOutcome.id = :linkProgramOutcomeId AND l.courseOffering.id=:courseOfferingId")
 				.setParameter("linkProgramOutcomeId",po.getId())
 				.setParameter("courseOfferingId",courseOffering.getId())
 				.uniqueResult();
 		return toReturn;
 	}	
 	
 	
 	@SuppressWarnings("unchecked")
 	public List<ProgramOutcomeGroup> getProgramOutcomeGroupsForProgram(Program p)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<ProgramOutcomeGroup> toReturn = null;
 		try
 		{
 			toReturn = (List<ProgramOutcomeGroup>)session.createQuery("from ProgramOutcomeGroup l where (l.programSpecific='Y' AND l.programId=:programId) OR l.programSpecific='N' ORDER BY l.programSpecific DESC,l.name")
 				.setParameter("programId",p.getId())
 				.list();
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public List<CourseOfferingContribution> getContributionForProgramOutcome(ProgramOutcome programOutcome, Program program)
 	{
 		return getContributionForProgramOutcome(programOutcome, program, new ArrayList<String>(0));
 
 	}
 	@SuppressWarnings("unchecked")
 	public List<CourseOfferingContribution> getContributionForProgramOutcome(ProgramOutcome programOutcome, Program program,List<String> terms)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CourseOfferingContribution> toReturn = new ArrayList<CourseOfferingContribution>();
 		try
 		{
 			StringBuffer sql = new StringBuffer();
 			//sql.append(" SELECT co.course_id AS {p.courseId}, lppo.program_outcome_id AS {p.programOutcomeId}, sum(cov.calculation_value) AS {p.contributionSum},count( distinct course_offering_id) AS {p.contributionCount} ");
 		//	sql.append(" SELECT co.course_id, lppo.program_outcome_id, sum(cov.calculation_value),count( distinct course_offering_id)");
 			sql.append(" SELECT co.id AS courseOfferingId, cov.calculation_value AS contributionObject, mov.calculation_value as masteryObject");
 
 			sql.append("  FROM course_offering co");
 			sql.append("      ,link_course_program lcp");
 			sql.append("      ,link_program_program_outcome lppo");
 			sql.append("      ,link_course_offering_contribution_program_outcome lcopo");
 			sql.append("      ,contribution_option_value cov ");
 			sql.append("      ,mastery_option_value mov ");
 			sql.append(" WHERE ");
 			sql.append( HibernateUtil.getListAsString("co.term", terms, true));
 			sql.append("       co.course_id = lcp.course_id");
 			sql.append("   AND co.id = lcopo.course_offering_id");
 			sql.append("   AND lcopo.link_program_program_outcome_id = lppo.id");
 			sql.append("   AND lppo.program_id = lcp.program_id");
 			sql.append("   AND lcopo.contribution_option_id = cov.id");
 			sql.append("   AND lcopo.mastery_option_id = mov.id");
 			sql.append("   AND lcp.program_id = :programId");
 			sql.append("   AND lppo.program_outcome_id = :programOutcomeId");
 			sql.append("   AND (cov.calculation_value + mov.calculation_value) > 0 ");
 			
 			logger.error("getContributionForProgramOutcome Core contributions:"+programOutcome.getId() + " "+ program.getId()+" "+sql.toString());
 			List<CourseOfferingContribution> fromOfferings = (List<CourseOfferingContribution>) session
 							.createSQLQuery(sql.toString()).setResultTransformer(Transformers.aliasToBean(CourseOfferingContribution.class))
 							.setParameter("programId",program.getId())
 							.setParameter("programOutcomeId",programOutcome.getId())
 							.list();
 			toReturn.addAll(fromOfferings);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<CourseOfferingContribution> getServiceCourseContributionForProgramOutomce(ProgramOutcome programOutcome, Program program)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CourseOfferingContribution> toReturn = new ArrayList<CourseOfferingContribution>();
 		try
 		{
 			StringBuffer sql = new StringBuffer();
 			sql.append(" SELECT lccpo.course_id AS courseOfferingId, cov.calculation_value AS contributionObject, mov.calculation_value AS masteryObject ");
 			sql.append("   from link_course_contribution_program_outcome lccpo");
 			sql.append("      , link_program_program_outcome lppo ");
 			sql.append("      , contribution_option_value cov ");
 			sql.append("      , mastery_option_value mov ");
 			sql.append("  WHERE lccpo.link_program_program_outcome_id = lppo.id ");
 			sql.append("    AND lccpo.contribution_option_id = cov.id");
 			sql.append("    AND lccpo.mastery_option_id = mov.id");
 			sql.append("    AND lppo.program_id=:programId");
 			sql.append("    AND lppo.program_outcome_id = :programOutcomeId");
 			sql.append("    AND (cov.calculation_value + mov.calculation_value)  > 0 ");
 			logger.error("getServiceCourseContributionForProgramOutomce "+sql.toString());
 			List<CourseOfferingContribution> fromCourses = (List<CourseOfferingContribution>) session
 					.createSQLQuery(sql.toString()).setResultTransformer(Transformers.aliasToBean(CourseOfferingContribution.class))
 					.setParameter("programId",program.getId())
 					.setParameter("programOutcomeId",programOutcome.getId())
 					.list();
 			
 			toReturn.addAll(fromCourses);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<ProgramOutcomeCourseContribution> getProgramOutcomeCoreCourseContributionForProgram(Program program)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<ProgramOutcomeCourseContribution> toReturn = new ArrayList<ProgramOutcomeCourseContribution>();
 		try
 		{
 			StringBuffer sql = new StringBuffer();
 			//sql.append(" SELECT co.course_id AS {p.courseId}, lppo.program_outcome_id AS {p.programOutcomeId}, sum(cov.calculation_value) AS {p.contributionSum},count( distinct course_offering_id) AS {p.contributionCount} ");
 		//	sql.append(" SELECT co.course_id, lppo.program_outcome_id, sum(cov.calculation_value),count( distinct course_offering_id)");
 			sql.append(" SELECT co.course_id AS courseId, lppo.program_outcome_id AS programOutcomeId, sum(cov.calculation_value) AS contributionSumObject,  sum(mov.calculation_value) AS masterySumObject  ");
 
 			sql.append("  FROM course_offering co");
 			sql.append("      ,link_course_program lcp");
 			sql.append("      ,link_program_program_outcome lppo");
 			sql.append("      ,link_course_offering_contribution_program_outcome lcopo");
 			sql.append("      ,contribution_option_value cov ");
 			sql.append("      ,mastery_option_value mov ");
 			sql.append(" WHERE co.course_id = lcp.course_id");
 			sql.append("   AND co.id = lcopo.course_offering_id");
 			sql.append("   AND lcopo.link_program_outcome_id = lppo.id");
 			sql.append("   AND lppo.program_id = lcp.program_id");
 			sql.append("   AND lcopo.contribution_option_id = cov.id");
 			sql.append("   AND lcopo.mastery_option_id = mov.id");
 			sql.append("   AND lcp.program_id = :programId");
 			sql.append("   GROUP BY  co.course_id, lppo.program_outcome_id");
 			logger.error("getProgramOutcomeCoreCourseContributionForProgram Core contributions:"+sql.toString());
 			List<ProgramOutcomeCourseContribution> fromOfferings = (List<ProgramOutcomeCourseContribution>) session
 							.createSQLQuery(sql.toString()).setResultTransformer(Transformers.aliasToBean(ProgramOutcomeCourseContribution.class))
 							.setParameter("programId",program.getId())
 							.list();
 			toReturn.addAll(fromOfferings);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	@SuppressWarnings("unchecked")
 	public List<ProgramOutcomeCourseContribution> getProgramOutcomeServiceCourseContributionForProgram(Program program)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<ProgramOutcomeCourseContribution> toReturn = new ArrayList<ProgramOutcomeCourseContribution>();
 		try
 		{
 			StringBuffer sql = new StringBuffer();
 			sql.append(" SELECT lccpo.course_id AS courseId, lppo.program_outcome_id AS programOutcomeId, cov.calculation_value AS contributionSum, mov.calculation_value AS masterySum ");
 			sql.append("   from link_course_contribution_program_outcome lccpo");
 			sql.append("      , link_program_program_outcome lppo ");
 			sql.append("      , contribution_option_value cov ");
 			sql.append("      , mastery_option_value mov ");
 			sql.append("  WHERE lccpo.link_program_program_outcome_id = lppo.id ");
 			sql.append("    AND lccpo.contribution_option_id = cov.id");
 			sql.append("    AND lccpo.mastery_option_id = mov.id");
 			sql.append("    AND lppo.program_id=:programId");
 			logger.error("getProgramOutcomeServiceCourseContributionForProgram :"+sql.toString());
 			List<ProgramOutcomeCourseContribution> fromCourses = (List<ProgramOutcomeCourseContribution>) session
 					.createSQLQuery(sql.toString()).setResultTransformer(Transformers.aliasToBean(ProgramOutcomeCourseContribution.class))
 					.setParameter("programId",program.getId())
 					.list();
 			
 			toReturn.addAll(fromCourses);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	public List<CourseAssessmentOption> getProgramCourseAssessmentOptions(Program program)
 	{
 		return getProgramCourseAssessmentOptions(program,new ArrayList<String>(0));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<CourseAssessmentOption> getProgramCourseAssessmentOptions(Program program, List<String> terms)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CourseAssessmentOption> toReturn = new ArrayList<CourseAssessmentOption>();
 		try
 		{
 			StringBuffer sql = new StringBuffer();
 			sql.append(" select sum(lcoa.weight) as weight, ato.display_index as optionId, co.course_id as courseId  ");
 			sql.append("    from course_offering co ");
 			sql.append("        ,link_course_offering_assessment lcoa ");
 			sql.append("        ,link_course_program lcp ");
 			sql.append("        ,assessment_time_option ato ");
 			sql.append("  WHERE ");
 			sql.append(HibernateUtil.getListAsString("co.term", terms, true));
 			sql.append("        lcp.program_id = :programId ");
 			sql.append("    AND lcp.course_id = co.course_id ");
 			sql.append("    AND co.id = lcoa.course_offering_id ");
 			sql.append("    AND ato.id =  lcoa.assessment_time_option_id ");
 			sql.append("group by co.course_id, ato.display_index  ");
 			sql.append("order by co.course_id, ato.display_index ");
 			//one record for each course that is part of the program.  Each record contains courseid, sum of weights for each time-option and the number of course_offerings for each.
 			List<CourseAssessmentOption> fromOfferings = (List<CourseAssessmentOption>) session
 							.createSQLQuery(sql.toString()).setResultTransformer(Transformers.aliasToBean(CourseAssessmentOption.class))
 							.setParameter("programId",program.getId())
 							.list();
 			toReturn.addAll(fromOfferings);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public HashMap<String, ArrayList<Double>> organizeAssessmentData(List<CourseAssessmentOption> dataList, int maxOptions,Map<String, Integer> offeringCounts) 
 	{
 		//key = course-id
 		//data is average for each course, for each assessment option id.  Average is calculated by taking the sum / maximum of course Offerings for that course (the number of offerings that have assessment data)
 		//value in location 0 is the # of course offerings for the course 		
 		
 		HashMap<String, ArrayList<Double>> r = new HashMap<String, ArrayList<Double>>();
 		
 		for(CourseAssessmentOption option : dataList)
 		{
 		
 			logger.debug(option.getCourseId() +  " "+option.getOptionId() + " "+option.getWeight());
 			//retrieve set of values from HashMap, if not already in there, add it
 			ArrayList<Double> data = r.get(""+option.getCourseId());
 			if(data == null)
 			{
 				data=new ArrayList<Double>(maxOptions+1);
 				for(int i = 0 ; i<= maxOptions; i++)
 					data.add(new Double(0.0));
 			}
 			//set the value in the corresponding location
 			//by retrieving the value that already exists and adding the weight to it
 			//and putting it back in the array at the saem location
 			Double value = data.get(option.getOptionId());
 			value += option.getWeight();
 			data.set(option.getOptionId(), value);
 			r.put(""+option.getCourseId() , data);
 		}
 
 		//calculate the averages by dividing the sum by the number of offerings. 
 		for(String key: r.keySet())
 		{
 			ArrayList<Double> data = r.get(key);
 			int offerings = offeringCounts.get(key)!=null? offeringCounts.get(key) : 1; 
 			for(int i = 1; i < data.size(); i++)
 			{
 				data.set(i,data.get(i)/offerings);
 			}
 		}
 		return r;
 	}
 	
 	public List<CourseTeachingMethodOption> getProgramCourseTeachingMethodOptions(Program program)
 	{
 		return getProgramCourseTeachingMethodOptions(program, new ArrayList<String>(0));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<CourseTeachingMethodOption> getProgramCourseTeachingMethodOptions(Program program, List<String> terms)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CourseTeachingMethodOption> toReturn = new ArrayList<CourseTeachingMethodOption>();
 		try
 		{
 			StringBuffer sql = new StringBuffer();
 			sql.append("  select sum(tmpo.comparative_value) as weight, tm.display_index as teachingMethodIndex, count(distinct lctm.course_offering_id) as offeringCount, co.course_id as courseId  ");
 			sql.append("    from course_offering co ");
 			sql.append("        ,link_course_offering_teaching_method lctm ");
 			sql.append("        ,link_course_program lcp ");
 			sql.append("        ,teaching_method tm ");
 			sql.append("        ,teaching_method_portion_option tmpo ");
 			sql.append("  WHERE ");
 			sql.append(HibernateUtil.getListAsString("co.term",terms,true));
 			sql.append(" lcp.program_id = :programId ");
 			sql.append("    AND lcp.course_id = co.course_id ");
 			sql.append("    AND co.id = lctm.course_offering_id ");
 			sql.append("    AND lctm.teaching_method_id = tm.id ");
 			sql.append("    AND lctm.teaching_method_portion_option_id = tmpo.id ");
 			sql.append("group by co.course_id, tm.display_index  ");
 			sql.append("order by co.course_id, tm.display_index ");
 			//one record for each course that is part of the program.  Each record contains courseid, sum of weights for each time-option and the number of course_offerings for each.
 			List<CourseTeachingMethodOption> fromOfferings = (List<CourseTeachingMethodOption>) session
 							.createSQLQuery(sql.toString()).setResultTransformer(Transformers.aliasToBean(CourseTeachingMethodOption.class))
 							.setParameter("programId",program.getId())
 							.list();
 			toReturn.addAll(fromOfferings);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public HashMap<String, ArrayList<Double>> organizeTeachingMethodData(List<CourseTeachingMethodOption> dataList, int maxOptions,Map<String, Integer> offeringCounts) 
 	{
 		//key = course-id
 		//data is average for each course, for each assessment option id.  Average is calculated by taking the sum / maximum of course Offerings for that course (the number of offerings that have assessment data)
 		//value in location 0 is the # of course offerings for the course 
 		
 		
 		HashMap<String, ArrayList<Double>> r = new HashMap<String, ArrayList<Double>>();
 		
 		for(CourseTeachingMethodOption option : dataList)
 		{
 			logger.debug(option.getCourseId() + " "+option.getOfferingCount() + " "+option.getTeachingMethodIndexId() + " "+option.getWeight());
 			//retrieve set of values from HashMap, if not already in there, add it
 			ArrayList<Double> data = r.get(""+option.getCourseId());
 			if(data == null)
 			{
 				data=new ArrayList<Double>(maxOptions+1);
 				for(int i = 0 ; i<= maxOptions; i++)
 					data.add(new Double(0.0));
 			}
 			//set the value in the corresponding location
 			//by retrieving the value that already exists and adding the weight to it
 			//and putting it back in the array at the saem location
 			Double value = data.get(option.getTeachingMethodIndexId());
 			value += option.getWeight();
 			data.set(option.getTeachingMethodIndexId(), value);
 			
 			//put the value back in the hashmap
 			r.put(""+option.getCourseId(), data);
 		}
 
 		//calculate the averages by dividing the sum by the number of offerings. 
 		for(String key: r.keySet())
 		{
 			ArrayList<Double> data = r.get(key);
 			int offerings = offeringCounts.get(key)!=null? offeringCounts.get(key) : 1; 
 			for(int i = 1; i < data.size(); i++)
 			{
 				data.set(i,data.get(i)/offerings);
 			}
 		}
 		return r;
 	}
 
 	public List<CourseAssessmentOption> getProgramAssessmentGroups(Program program)
 	{
 		return getProgramAssessmentGroups(program, new ArrayList<String>(0));
 	}
     
 
 	@SuppressWarnings("unchecked")
 	public List<CourseAssessmentOption> getProgramAssessmentGroups(Program program , List<String> terms)
 	{
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<CourseAssessmentOption> toReturn = new ArrayList<CourseAssessmentOption>();
 		try
 		{
 			StringBuffer sql = new StringBuffer();
 			sql.append(" select co.course_id as courseId,gr.display_index as optionId, sum(lcoa.weight) as weight ");
 			sql.append("  from assessment_group gr ");
 			sql.append("     , assessment a ");
 			sql.append("     , link_course_offering_assessment lcoa ");
 			sql.append("     , course_offering co ");
 			sql.append("     , link_course_program lcp ");
 			sql.append("where  ");
 			sql.append(HibernateUtil.getListAsString("co.term",terms,true));
 			sql.append("lcp.program_id=:programId  ");
 			sql.append("  and lcp.course_id = co.course_id ");
 			sql.append("  and co.id = lcoa.course_offering_id ");
 			sql.append("  and lcoa.assessment_id = a.id ");
 			sql.append("  and a.assessment_group_id = gr.id ");
 			sql.append("  group by co.course_id,gr.display_index ");
 			//one record for each course that is part of the program.  Each record contains courseid, sum of weights for each time-option and the number of course_offerings for each.
 			List<CourseAssessmentOption> fromOfferings = (List<CourseAssessmentOption>) session
 							.createSQLQuery(sql.toString()).setResultTransformer(Transformers.aliasToBean(CourseAssessmentOption.class))
 							.setParameter("programId",program.getId())
 							.list();
 			toReturn.addAll(fromOfferings);
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	public HashMap<String, ArrayList<Double>> organizeAssessmentGroupData(List<CourseAssessmentOption> dataList, int maxOptions,Map<String, Integer> offeringCounts) 
 	{
 		//key = course-id
 		//data is average for each course, for each assessment option id.  Average is calculated by taking the sum / maximum of course Offerings for that course (the number of offerings that have assessment data)
 		//value in location 0 is the # of course offerings for the course 
 		
 		
 		HashMap<String, ArrayList<Double>> r = new HashMap<String, ArrayList<Double>>();
 		
 		for(CourseAssessmentOption option : dataList)
 		{
 			logger.debug(option.getCourseId() + " "+option.getOptionId() + " "+option.getWeight());
 			//retrieve set of values from HashMap, if not already in there, add it
 			ArrayList<Double> data = r.get(""+option.getCourseId());
 			if(data == null)
 			{
 				data=new ArrayList<Double>(maxOptions+1);
 				for(int i = 0 ; i<= maxOptions; i++)
 					data.add(new Double(0.0));
 			}
 			//set the value in the corresponding location
 			//by retrieving the value that already exists and adding the weight to it
 			//and putting it back in the array at the saem location
 			Double value = data.get(option.getOptionId());
 			value += option.getWeight();
 			data.set(option.getOptionId(), value);
 			
 			//put the value back in the hashmap
 			r.put(""+option.getCourseId(), data);
 		}
 
 		//calculate the averages by dividing the sum by the number of offerings. 
 		for(String key: r.keySet())
 		{
 			ArrayList<Double> data = r.get(key);
 			int offerings = offeringCounts.get(key)!=null? offeringCounts.get(key) : 1; 
 			for(int i = 1; i < data.size(); i++)
 			{
 				data.set(i,data.get(i)/offerings);
 			}
 		}
 		return r;
 	}
 	public Map<String, Integer> getCourseOfferingCounts(Program p)
 	{
 		return getCourseOfferingCounts( p, new ArrayList<String>(0));
 	}
 	public Map<String, Integer> getCourseOfferingCounts(Program p, List<String> terms)
 	{
 	
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Map<String, Integer> toReturn = new TreeMap<String, Integer>();
 		try
 		{
 			StringBuffer sql = new StringBuffer();
 			sql.append("  select co.course_id as a, count( distinct co.id) as b");
 			sql.append(" from course_offering co ");
 			sql.append("    , link_course_program lcp ");
 			sql.append("where ");
 			sql.append(HibernateUtil.getListAsString("co.term",terms,true));
 			sql.append(" lcp.program_id = :programId ");
 			sql.append(" and lcp.course_id = co.course_id ");
 			sql.append(" and co.id in (select course_offering_id from link_course_offering_assessment) ");
 			sql.append(" group by co.course_id; ");
 			
 			@SuppressWarnings("unchecked")
 			List<Pair> counts = (List<Pair>) session
 					.createSQLQuery(sql.toString()).setResultTransformer(Transformers.aliasToBean(Pair.class))
 					.setParameter("programId",p.getId())
 					.list();
 			for(Pair pair: counts)
 			{
 				toReturn.put(""+pair.getA(), pair.getB());
 			}
 			session.getTransaction().commit();
 		}
 		catch(Exception e)
 		{
 			HibernateUtil.logException(logger, e);
 		}
 		return toReturn;
 	}
 	
 	
 	public ProgramManager()
 	{
 
 	}
 	
 
 	public static ProgramManager instance()
 	{
 		if (instance == null)
 		{
 			instance = new ProgramManager();
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
