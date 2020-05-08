 /**
  * 
  */
 package com.tokogame.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.tokogame.dao.KoleksiDAO;
 import com.tokogame.domain.Koleksi;
 import com.tokogame.domain.KoleksiExample;
 
 /**
  * @author mardy jonathan
  *
  */
@Service("koleksiMaintenanceService")
 public class KoleksiMaintenanceServiceImpl implements KoleksiMaintenanceService{
 
 	@Autowired
 	private KoleksiDAO koleksiDAO;
 	
 	@Override
 	public List<Koleksi> getKoleksiList(Koleksi koleksi) {
 		// TODO Auto-generated method stub
 		KoleksiExample koleksiExample = new KoleksiExample();
 		KoleksiExample.Criteria criteria = koleksiExample.createCriteria();
 		if(koleksi.getPkKoleksi()!=null){
 			criteria.andPkKoleksiEqualTo(koleksi.getPkKoleksi());
 		}
 		if(koleksi.getKoleksiName()!=null){
 			criteria.andKoleksiNameEqualTo(koleksi.getKoleksiName());
 		}
 		koleksiExample.setOrderByClause("koleksi_name asc");
 		return koleksiDAO.selectByExample(koleksiExample);
 	}
 
 	@Override
 	public void insertKoleksi(Koleksi koleksi) {
 		// TODO Auto-generated method stub
 		koleksiDAO.insert(koleksi);
 	}
 
 	@Override
 	public void updateKoleksi(Koleksi koleksi) {
 		// TODO Auto-generated method stub
 		koleksiDAO.updateByPrimaryKey(koleksi);
 	}
 
 	@Override
 	public void deleteKoleksi(Koleksi koleksi) {
 		// TODO Auto-generated method stub
 		int pkKoleksi = koleksi.getPkKoleksi();
 		koleksiDAO.deleteByPrimaryKey(pkKoleksi);
 	}
 	
 	
 
 	
 }
