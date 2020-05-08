 package com.danone.bonafont.batch.dao.imp.jpa;
 
import java.util.HashMap;
 import java.util.List;
import java.util.Map;
 
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.danone.bonafont.batch.dao.SapOrdenDAO;
 import com.danone.bonafont.batch.model.SapOrden;
 
 /**
  * @author Eduardo Rodriguez
  * 
  */
 @Repository("sapOrdenDAO")
 public class SapOrdenDAOJPAImpl extends GenericDAOJPAImpl<SapOrden, Long>
 		implements SapOrdenDAO {
 
 	@SuppressWarnings("unchecked")
 	@Transactional(readOnly = true)
 	public List<SapOrden> findByOrdenCompra(String orden) {
		Map<String,String> paramMap=new HashMap<String, String>();	

		paramMap.put("ch_orden_compra", orden);	

		return getJpaTemplate().findByNamedQueryAndNamedParams("SapOrden.findByOrden", paramMap);
 	}
 	
 }
