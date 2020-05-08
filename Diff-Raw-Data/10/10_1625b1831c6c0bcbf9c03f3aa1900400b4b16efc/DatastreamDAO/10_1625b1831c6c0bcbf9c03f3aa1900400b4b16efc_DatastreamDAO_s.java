 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package health.database.DAO;
 
 import health.database.models.CreationTemplate;
 import health.database.models.Datastream;
 import health.database.models.DatastreamBlocks;
 import health.database.models.DatastreamUnits;
 import health.database.models.DeviceTemplate;
 import health.database.models.JobsTable;
 import health.input.jsonmodels.JsonDatastreamUnits;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 import javax.persistence.NonUniqueResultException;
 
 import org.hibernate.Criteria;
 import org.hibernate.FetchMode;
 import org.hibernate.Session;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import util.AllConstants;
 import util.HibernateUtil;
 import util.UnitValueTypes;
 import device.input.jsonmodels.JsonDeviceTemplate;
 
 /**
  * 
  * @author Leon
  */
 public class DatastreamDAO extends BaseDAO {
 
 	public String createDatastream(Datastream object) {
 		try {
 			Session session = HibernateUtil.beginTransaction();
 			session.save(object);
 			HibernateUtil.commitTransaction();
 			if (object.getStreamId() != null) {
 				return object.getStreamId();
 			} else {
 				return null;
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			return null;
 		} finally {
 		}
 	}
 
 	public Datastream basicDefaultDatastreamCreate(String loginID, int subjectID) {
 		Datastream ds = new Datastream();
 		Date now = new Date();
 		UUID uuid = UUID.randomUUID();
 		ds.setStreamId(uuid.toString());
 		ds.setCreatedTime(now);
 		ds.setUpdated(now);
 		ds.setOwner(loginID);
 		ds.setSubId(subjectID);
 		ds.setPurpose(AllConstants.HealthConts.default_health_subject_purpose);
 		return ds;
 	}
 
 	public Datastream attachDatastreamUnitsFromTemplate(Datastream ds,
 			String templateID) throws Exception {
 		String devicetemplateid = templateID;
 		DeviceTemplateDAO dtDao = new DeviceTemplateDAO();
 		DeviceTemplate devicetemplate = dtDao
 				.getDeviceTemplate(devicetemplateid);
 		if (devicetemplate == null) {
 			throw new Exception("cannot find template in database");
 		}
 		JsonDeviceTemplate jdtemplate = dtDao
 				.toJsonDeviceTemplate(devicetemplate);
 		if (jdtemplate == null || jdtemplate.getUnits_list().isEmpty()) {
 			throw new Exception("template parsing error");
 		}
 		ArrayList<DatastreamUnits> datastreamUnits = new ArrayList<DatastreamUnits>();
 		for (JsonDatastreamUnits unit : jdtemplate.getUnits_list()) {
 			if (unit.getValue_type() == null) {
 				throw new Exception("template parsing error--getValue_type");
 			}
 			if (!UnitValueTypes.existValueType(unit.getValue_type())) {
 				throw new Exception(
 						"template parsing error--getValue_type--doesnt exist type:"
 								+ unit.getValue_type());
 			}
 			DatastreamUnits dsUnit = new DatastreamUnits();
 			dsUnit.setStreamID(ds);
 			dsUnit.setCreatedTime(new Date());
 			dsUnit.setUpdatedTime(new Date());
 			dsUnit.setMaxValue(unit.getMax_value());
 			dsUnit.setMinValue(unit.getMin_value());
 			dsUnit.setCurrentValue(unit.getCurrent_value());
 			dsUnit.setUnitLabel(unit.getUnit_label());
 			dsUnit.setValueType(unit.getValue_type());
 			dsUnit.setUnitSymbol(unit.getUnit_symbol());
 			dsUnit.setUnitID(UUID.randomUUID().toString());
 			datastreamUnits.add(dsUnit);
 		}
 		ds.setDatastreamUnitsList(datastreamUnits);
 		return ds;
 	}
 
 	public void createDefaultDatastreamsOnDefaultSubject(String loginID,
 			int subjectID) throws Exception {
 
 		creationTemplateDAO ctDao = new creationTemplateDAO();
 		List<CreationTemplate> ctList = ctDao.findall();
 		ArrayList<Datastream> dsList=new ArrayList<Datastream>();
 		for (CreationTemplate ct : ctList) {
 			Datastream ds = basicDefaultDatastreamCreate(loginID, subjectID);
 			ds.setTitle(ct.getInput2());
 			attachDatastreamUnitsFromTemplate(ds,
 					ct.getInput1());
 			dsList.add(ds);
 		}
 		createDatastreamList(dsList);		
 	}
 
 	public Datastream getDatastream(String StreamID, boolean fetchDataUnits,
 			boolean fetchDatablocks) {
 		Datastream stream = null;
 		// stream = (Datastream) session.get(Datastream.class, StreamID);
 		Session session = HibernateUtil.beginTransaction();
 		Criteria criteria = session.createCriteria(Datastream.class);
 		if (fetchDataUnits) {
 			criteria.setFetchMode("datastreamUnitsList", FetchMode.JOIN);
 		}
 		if (fetchDatablocks) {
 			criteria.setFetchMode("datastreamBlocksList", FetchMode.JOIN);
 		}
 		stream = (Datastream) criteria.add(Restrictions.idEq(StreamID))
 				.uniqueResult();
 		if (session.isOpen()) {
 			session.close();
 		}
 		return stream;
 	}
 	public Datastream getHealthDatastreamByTitle(int subjectID,String streamTitle, boolean fetchDataUnits,
 			boolean fetchDatablocks) throws NonUniqueResultException{
 		Datastream stream = null;
 		// stream = (Datastream) session.get(Datastream.class, StreamID);
 		Session session = HibernateUtil.beginTransaction();
 		Criteria criteria = session.createCriteria(Datastream.class);
 		criteria.add(Restrictions.eq("purpose", AllConstants.HealthConts.defaultDatastreamPurpose));
 		criteria.add(Restrictions.eq("title", streamTitle));
 		criteria.add(Restrictions.eq("subId", subjectID));
 		if (fetchDataUnits) {
 			criteria.setFetchMode("datastreamUnitsList", FetchMode.JOIN);
 		}
 		if (fetchDatablocks) {
 			criteria.setFetchMode("datastreamBlocksList", FetchMode.JOIN);
 		}
 		stream = (Datastream) criteria.uniqueResult();
 		if (session.isOpen()) {
 			session.close();
 		}
 		return stream;
 	}
 
 	public boolean existBlockID_fromDatastream(Datastream stream, String blockID) {
 		List<DatastreamBlocks> blockList = stream.getDatastreamBlocksList();
 		boolean exist = false;
 		for (DatastreamBlocks block : blockList) {
 			if (block.getBlockId().equalsIgnoreCase(blockID)) {
 				exist = true;
 				return exist;
 			}
 		}
 		return exist;
 	}
 
 	public List<Datastream> getDatastreamList(Integer SubjectID,
 			boolean fetchDataUnits, boolean fetchDatablocks) {
 		Session session = HibernateUtil.beginTransaction();
 		System.out.println(SubjectID);
 		Criteria criteria = session.createCriteria(Datastream.class).add(
 				Restrictions.eq("subId", SubjectID));
 		// if (fetchDataUnits) {
 		// criteria.setFetchMode("datastreamUnitsList", FetchMode.JOIN);
 		// }
 		// if (fetchDatablocks) {
 		// criteria.setFetchMode("datastreamBlocksList", FetchMode.JOIN);
 		// }
 		List<Datastream> list = criteria.list();
 		if (session.isOpen()) {
 			session.close();
 		}
 		return list;
 	}
 
 	public List<DatastreamUnits> getDatastreamUnits(String datastreamID) {
 		Session session = HibernateUtil.beginTransaction();
 		Criteria criteria = session.createCriteria(DatastreamUnits.class).add(
 				Restrictions.eq("streamID", datastreamID));
 		List<DatastreamUnits> list = criteria.list();
 		if (session.isOpen()) {
 			session.close();
 		}
 		return list;
 	}
 
 	public Datastream createDatastream(Datastream streamObject,
 			List<DatastreamUnits> unitList) {
 		try {
 			Session session = HibernateUtil.beginTransaction();
 
 			session.save(streamObject);
 			for (DatastreamUnits unit : unitList) {
 				session.save(unit);
 			}
 			HibernateUtil.commitTransaction();
 			if (streamObject.getStreamId() != null) {
 				return streamObject;
 			} else {
 				return null;
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			return null;
 		} finally {
 		}
 	}
 
 	public void createDatastreamList(List<Datastream> streamObjectList) {
 		try {
 			Session session = HibernateUtil.beginTransaction();
 			for (Datastream ds : streamObjectList) {
 				session.save(ds);
 				List<DatastreamUnits> unitsList = ds.getDatastreamUnitsList();
 				for (DatastreamUnits unit : unitsList) {
 					session.save(unit);
 				}
 			}
 			HibernateUtil.commitTransaction();
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		} finally {
 		}
 	}
 
 	public DatastreamBlocks CreateDatastreamBlock(Datastream streamID,
 			String blockName, String blockDesc) {
 		try {
 			Session session = HibernateUtil.beginTransaction();
 			DatastreamBlocks block = new DatastreamBlocks();
 			UUID uuid = UUID.randomUUID();
 			block.setBlockId(uuid.toString());
 			// block.setStreamID(streamID);
 			Date now = new Date();
 			block.setCreated(now);
 			block.setDisplayName(blockName);
 			block.setStreamID(streamID);
 			block.setUpdated(now);
 			session.save(block);
 			HibernateUtil.commitTransaction();
 			return block;
 		} catch (Exception ex) {
 			HibernateUtil.rollBackTransaction();
 			ex.printStackTrace();
 			return null;
 		}
 	}
 
 	public List<DatastreamBlocks> getDatastreamBlockList(String streamID) {
 		List<?> objects = null;
 		Session session = HibernateUtil.beginTransaction();
 		Criteria criteria = session.createCriteria(DatastreamBlocks.class);
 		criteria.add(Restrictions.eq("streamID", streamID)).addOrder(
 				Order.asc("created"));
 		;
 		try {
 			objects = criteria.list();
 			return (List<DatastreamBlocks>) objects;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			return null;
 		} finally {
 			if (session.isOpen()) {
 				session.close();
 			}
 		}
 	}
 
 	public DatastreamBlocks getDatastreamBlock(String blockID) {
 		Session session = HibernateUtil.beginTransaction();
 		DatastreamBlocks object = (DatastreamBlocks) session.get(
 				DatastreamBlocks.class, blockID);
 		try {
 			return object;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			return null;
 		} finally {
 			if (session.isOpen()) {
 				session.close();
 			}
 		}
 	}
 
 	public void DeleteDatastream(Datastream datastream) {
 		try {
 			Session session = HibernateUtil.beginTransaction();
 			session.delete(datastream);
 			{
 				Date now = new Date();
 				JobsTable job = new JobsTable();
 				job.setCreatedDate(now);
 				job.setUpdatedDate(now);
 				job.setStatus(AllConstants.ProgramConts.job_status_pending);
 				job.setMethod(AllConstants.ProgramConts.job_method_delete);
 				job.setTargetObject(AllConstants.ProgramConts.job_targetObject_datastream);
 				job.setTargetObjectID(datastream.getStreamId());
 				session.save(job);
 			}
 			HibernateUtil.commitTransaction();
 			if (session.isOpen()) {
 				session.close();
 			}
 
 		} catch (Exception e) {
 			HibernateUtil.rollBackTransaction();
 			e.printStackTrace();
 		}
 	}
 	public void DeleteDatastreamUnitList(Datastream datastream) {
 		try {
 			Session session = HibernateUtil.beginTransaction();
 //			session.delete(datastream);
 			List<DatastreamUnits> unitList=datastream.getDatastreamUnitsList();
 			for(DatastreamUnits unit:unitList)
 			{
 				session.delete(unit);
 			}
 			
 			{
 				Date now = new Date();
 				JobsTable job = new JobsTable();
 				job.setCreatedDate(now);
 				job.setUpdatedDate(now);
 				job.setStatus(AllConstants.ProgramConts.job_status_pending);
 				job.setMethod(AllConstants.ProgramConts.job_method_delete);
 				job.setTargetObject(AllConstants.ProgramConts.job_targetObject_datastream);
 				job.setTargetObjectID(datastream.getStreamId());
 				session.save(job);
 			}
 			HibernateUtil.commitTransaction();
 			if (session.isOpen()) {
 				session.close();
 			}
 
 		} catch (Exception e) {
 			HibernateUtil.rollBackTransaction();
 			e.printStackTrace();
 		}
 	}
 
 	public void DeleteDataBlock(DatastreamBlocks block) {
 		try {
 			Session session = HibernateUtil.beginTransaction();
 			session.delete(block);
 			{
 				Date now = new Date();
 				JobsTable job = new JobsTable();
 				job.setCreatedDate(now);
 				job.setUpdatedDate(now);
 				job.setStatus(AllConstants.ProgramConts.job_status_pending);
 				job.setMethod(AllConstants.ProgramConts.job_method_delete);
 				job.setTargetObject(AllConstants.ProgramConts.job_targetObject_datablock);
 				job.setTargetObjectID(block.getBlockId());
 				session.save(job);
 			}
 			HibernateUtil.commitTransaction();
 			if (session.isOpen()) {
 				session.close();
 			}
 
 		} catch (Exception e) {
 			HibernateUtil.rollBackTransaction();
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String arsg[]) throws Exception {
 		DatastreamDAO dao = new DatastreamDAO();
 		dao.createDefaultDatastreamsOnDefaultSubject("leoncool", 644);
 
 	}
 }
