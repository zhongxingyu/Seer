 package com.artivisi.penagihan.dao;
 
 import java.math.BigDecimal;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.springframework.stereotype.Repository;
 
 import com.artivisi.penagihan.domain.Nasabah;
 import com.artivisi.penagihan.domain.RekapOutstanding;
 import com.artivisi.penagihan.domain.StatusTagihan;
 import com.artivisi.penagihan.domain.Tagihan;
 import com.artivisi.penagihan.domain.TagihanPK;
 
 @Repository
 public class TagihanDao {
 	@PersistenceContext private EntityManager entityManager;
 	
 	public void simpan(Tagihan t){
 		if(t.getId() == null) {
 			entityManager.persist(t);
 		} else {
 			Tagihan tx = cariTagihanById(t.getId()); // bandingkan dengan kondisi existing di db
			if(StatusTagihan.LUNAS.equals(tx.getStatus())){
 				throw new IllegalStateException("Tagihan yang sudah lunas tidak boleh dimodifikasi");
 			}
 			entityManager.merge(t);
 		}
 	}
 	
 	public Tagihan cariTagihanById(TagihanPK id){
 		return entityManager.find(Tagihan.class, id);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Tagihan> cariTagihanByNasabahDanStatus(Nasabah n,
 			StatusTagihan status, Integer start, Integer rows) {
 		if(start == null || start < 0){
 			start = 0;
 		}
 		
 		if(rows == null || rows < 1){
 			rows = 10;
 		}
 		
 		String jpql = "select t from Tagihan t " +
 				"where t.nasabah.id = :idNasabah " +
 				"and t.status = :status " +
 				"order by t.tanggalJatuhTempo";
 		
 		
 		return entityManager.createQuery(jpql)
 				.setParameter("idNasabah", n.getId())
 				.setParameter("status", status)
 				.setFirstResult(start)
 				.setMaxResults(rows)
 				.getResultList();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Tagihan> cariTagihanByPeriodeJatuhTempo(Date mulai,
 			Date sampai, Integer start, Integer rows) {
 		if(start == null || start < 0){
 			start = 0;
 		}
 		
 		if(rows == null || rows < 1){
 			rows = 10;
 		}
 		
 		String jpql = "select t from Tagihan t " +
 				"where t.tanggalJatuhTempo between :mulai and :sampai " +
 				"order by t.tanggalJatuhTempo";
 		
 		return entityManager.createQuery(jpql)
 				.setParameter("mulai", mulai)
 				.setParameter("sampai", sampai)
 				.setFirstResult(start)
 				.setMaxResults(rows)
 				.getResultList();
 	}
 
 	public BigDecimal totalOutstandingByNasabah(Nasabah n) {
 		if(n == null || n.getId() == null){
 			return BigDecimal.ZERO;
 		}
 		
 		String jpql = "select sum(t.nilai) from Tagihan t " +
 				"where t.nasabah.id = :idNasabah " +
 				"and t.status in (:status)";
 		
 		return (BigDecimal) entityManager.createQuery(jpql)
 				.setParameter("idNasabah", n.getId())
 				.setParameter("status", Arrays.asList(StatusTagihan.BELUM_DIBAYAR, StatusTagihan.DIBAYAR_SEBAGIAN))
 				.getSingleResult();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<RekapOutstanding> rekapOutstandingObject() {
 		String jpql = "select new com.artivisi.penagihan.domain.RekapOutstanding(t.nasabah, sum(t.nilai))" +
 				" from Tagihan t " +
 				"where t.status in (:status) " +
 				"group by t.nasabah ";
 		
 		return entityManager.createQuery(jpql)
 				.setParameter("status", Arrays.asList(StatusTagihan.BELUM_DIBAYAR, StatusTagihan.DIBAYAR_SEBAGIAN))
 				.getResultList();
 	}
 
 	@SuppressWarnings("unchecked")
 	public Map<String, BigDecimal> rekapOutstandingMap() {
 		String jpql = "select t.nasabah.id, sum(t.nilai)" +
 				" from Tagihan t " +
 				"where t.status in (:status) " +
 				"group by t.nasabah ";
 		
 		List<Object[]> hasilQuery = entityManager.createQuery(jpql)
 				.setParameter("status", Arrays.asList(StatusTagihan.BELUM_DIBAYAR, StatusTagihan.DIBAYAR_SEBAGIAN))
 				.getResultList();
 		
 		Map<String, BigDecimal> hasil = new HashMap<String, BigDecimal>();
 		for (Object[] data : hasilQuery) {
 			hasil.put((String)data[0], (BigDecimal)data[1]);
 		}
 		return hasil;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Tagihan> cariTagihanByNomerNasabah(String nomer) {
 		String jpql = "select t from Tagihan t where t.nasabah.nomer = :nomer";
 		return entityManager.createQuery(jpql)
 				.setParameter("nomer", nomer)
 				.getResultList();
 	}
 }
