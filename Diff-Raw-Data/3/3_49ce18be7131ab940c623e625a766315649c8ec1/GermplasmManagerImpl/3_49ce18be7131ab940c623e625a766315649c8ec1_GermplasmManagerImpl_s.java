 package org.strasa.middleware.manager;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.ibatis.session.ExecutorType;
 import org.apache.ibatis.session.SqlSession;
 import org.strasa.middleware.factory.ConnectionFactory;
 import org.strasa.middleware.mapper.GermplasmCharacteristicsMapper;
 import org.strasa.middleware.mapper.GermplasmMapper;
 import org.strasa.middleware.mapper.other.GermplasmBreederMapper;
 import org.strasa.middleware.model.Germplasm;
 import org.strasa.middleware.model.GermplasmExample;
 import org.strasa.web.uploadstudy.view.pojos.GermplasmDeepInfoModel;
 import org.zkoss.zk.ui.select.annotation.WireVariable;
 
 public class GermplasmManagerImpl {
 
 	@WireVariable
 	ConnectionFactory connectionFactory;
 
 	public Germplasm getGermplasmByName(String value) {
 		SqlSession session = connectionFactory.sqlSessionFactory.openSession();
 		GermplasmMapper mapper = session.getMapper(GermplasmMapper.class);
 
 		try {
 			GermplasmExample example = new GermplasmExample();
 			example.createCriteria().andGermplasmnameEqualTo(value);

 			return mapper.selectByExample(example).get(0);
 
 		} finally {
 			session.close();
 		}
 	}
 
 	public Germplasm getGermplasmById(int id) {
 		SqlSession session = connectionFactory.sqlSessionFactory.openSession();
 		GermplasmMapper mapper = session.getMapper(GermplasmMapper.class);
 		try {
 			return mapper.selectByPrimaryKey(id);
 		} finally {
 			session.close();
 		}
 
 	}
 
 	public List<Germplasm> getGermplasmListByName(String value) {
 		SqlSession session = connectionFactory.sqlSessionFactory.openSession();
 		GermplasmMapper mapper = session.getMapper(GermplasmMapper.class);
 
 		try {
 			GermplasmExample example = new GermplasmExample();
 			if (value.contains("%")) {
 				example.createCriteria().andGermplasmnameLike(value);
 			} else {
 				example.createCriteria().andGermplasmnameEqualTo(value);
 			}
 			if (mapper.selectByExample(example).isEmpty())
 				return null;
 			return mapper.selectByExample(example);
 
 		} finally {
 			session.close();
 		}
 	}
 
 	public List<Germplasm> getGermplasmListByUserID(int userid) {
 		SqlSession session = connectionFactory.sqlSessionFactory.openSession();
 		GermplasmMapper mapper = session.getMapper(GermplasmMapper.class);
 
 		try {
 			GermplasmExample example = new GermplasmExample();
 			example.createCriteria().andUseridEqualTo(userid);
 			if (mapper.selectByExample(example).isEmpty())
 				return null;
 			return mapper.selectByExample(example);
 		} finally {
 			session.close();
 		}
 	}
 
 	public List<Germplasm> getGermplasmListByType(int id) {
 		SqlSession session = connectionFactory.sqlSessionFactory.openSession();
 		GermplasmMapper mapper = session.getMapper(GermplasmMapper.class);
 
 		try {
 			GermplasmExample example = new GermplasmExample();
 			example.createCriteria().andGermplasmtypeidEqualTo(id);
 			if (mapper.selectByExample(example).isEmpty())
 				return null;
 			return mapper.selectByExample(example);
 		} finally {
 			session.close();
 		}
 	}
 
 	public boolean isGermplasmExisting(String value) {
 		if (this.getGermplasmByName(value) == null)
 			return false;
 		return true;
 	}
 
 	public int addGermplasm(Germplasm record) {
 		SqlSession session = connectionFactory.sqlSessionFactory.openSession();
 		GermplasmMapper mapper = session.getMapper(GermplasmMapper.class);
 		try {
 			mapper.insert(record);
 			session.commit();
 		} finally {
 			session.close();
 		}
 		return record.getId();
 	}
 
 	public int updateGermplasm(Germplasm record) {
 		SqlSession session = connectionFactory.sqlSessionFactory.openSession();
 		GermplasmMapper mapper = session.getMapper(GermplasmMapper.class);
 		try {
 			mapper.updateByPrimaryKey(record);
 			session.commit();
 		} finally {
 			session.close();
 		}
 		return record.getId();
 	}
 
 	public void addGermplasmList(Collection<GermplasmDeepInfoModel> collection) {
 		SqlSession session = connectionFactory.sqlSessionFactory.openSession(ExecutorType.BATCH);
 		GermplasmMapper mapper = session.getMapper(GermplasmMapper.class);
 		try {
 
 			for (Germplasm record : collection) {
 				System.out.print(record.toString());
 
 				if (record.getId() == null)
 					mapper.insert(record);
 				else {
 					mapper.updateByPrimaryKey(record);
 
 				}
 			}
 			session.commit();
 		} finally {
 			session.close();
 		}
 		return;
 	}
 
 	public void addGermplasmListNoRepeat(Collection<GermplasmDeepInfoModel> collection, Integer userid) {
 		SqlSession session = connectionFactory.sqlSessionFactory.openSession();
 		GermplasmMapper mapper = session.getMapper(GermplasmMapper.class);
 		try {
 
 			for (Germplasm record : collection) {
 				System.out.print(record.toString());
 
 				GermplasmExample example = new GermplasmExample();
 				example.createCriteria().andGermplasmnameEqualTo(record.getGermplasmname()).andUseridEqualTo(userid);
 				if (record.getId() == null) {
 					if (mapper.countByExample(example) > 0) {
 						record.setId(mapper.selectByExample(example).get(0).getId());
 						mapper.updateByPrimaryKey(record);
 					} else {
 						mapper.insert(record);
 					}
 				} else {
 					mapper.updateByPrimaryKey(record);
 
 				}
 			}
 			session.commit();
 		} finally {
 			session.close();
 		}
 		return;
 	}
 
 	public void updateBreeders(List<GermplasmDeepInfoModel> lstRecord) {
 		SqlSession session = connectionFactory.sqlSessionFactory.openSession(ExecutorType.BATCH);
 		GermplasmBreederMapper mapper = session.getMapper(GermplasmBreederMapper.class);
 		try {
 			for (GermplasmDeepInfoModel record : lstRecord) {
 
 				Germplasm newRec = record;
 
 				mapper.Update(newRec);
 
 			}
 			session.commit();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			session.close();
 		}
 		return;
 	}
 
 	public void addGermplasmBatch(List<GermplasmDeepInfoModel> lstRecord) {
 		SqlSession session = connectionFactory.sqlSessionFactory.openSession(ExecutorType.BATCH);
 		GermplasmMapper mapper = session.getMapper(GermplasmMapper.class);
 		GermplasmCharacteristicsMapper charmapper = session.getMapper(GermplasmCharacteristicsMapper.class);
 		try {
 			for (GermplasmDeepInfoModel rec : lstRecord) {
 
 				Germplasm record = rec;
 				if (record.getId() == null)
 					mapper.insert(record);
 				else {
 					mapper.updateByPrimaryKey(record);
 
 				}
 
 			}
 			session.commit();
 		} finally {
 			session.close();
 		}
 		return;
 	}
 
 }
