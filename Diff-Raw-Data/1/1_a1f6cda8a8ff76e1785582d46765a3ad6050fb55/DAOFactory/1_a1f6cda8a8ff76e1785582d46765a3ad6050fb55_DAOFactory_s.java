 package ru.xrm.app.dao;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class DAOFactory {
 
 	private static DAOFactory instance;
 	
 	Map<Class<? extends GenericDAO>, GenericDAO> daos = new HashMap<Class<? extends GenericDAO>, GenericDAO>();
 	
 	public static synchronized DAOFactory getInstance(){
 		if (instance==null){
 			instance=new DAOFactory();
 		}
 		return instance;
 	}
 	
 	public <T extends GenericDAO> T getDao(Class<T> daoClazz){
 		T dao=daoClazz.cast( daos.get(daoClazz) );
 		if (dao!=null){
 			return dao;
 		}else{
 			try {
 				dao = daoClazz.newInstance();
 				daos.put(daoClazz, dao);
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 	
 }
