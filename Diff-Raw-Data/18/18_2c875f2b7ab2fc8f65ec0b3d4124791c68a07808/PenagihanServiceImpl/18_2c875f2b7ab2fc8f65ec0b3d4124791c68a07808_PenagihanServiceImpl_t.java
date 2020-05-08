 package com.artivisi.penagihan.service;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.artivisi.penagihan.dao.NasabahDao;
 import com.artivisi.penagihan.dao.PembayaranDao;
 import com.artivisi.penagihan.dao.TagihanDao;
 import com.artivisi.penagihan.domain.Kolektor;
 import com.artivisi.penagihan.domain.Nasabah;
 import com.artivisi.penagihan.domain.Pembayaran;
 import com.artivisi.penagihan.domain.PenagihanService;
 import com.artivisi.penagihan.domain.RekapOutstanding;
 import com.artivisi.penagihan.domain.StatusTagihan;
 import com.artivisi.penagihan.domain.Tagihan;
 import com.artivisi.penagihan.domain.TagihanPK;
 
@Service("penagihanService")
 @Transactional(propagation=Propagation.REQUIRED)
 public class PenagihanServiceImpl implements PenagihanService {
 
 	@Autowired private NasabahDao nasabahDao;
 	@Autowired private TagihanDao tagihanDao;
 	@Autowired private PembayaranDao pembayaranDao;
 	
 	public void simpan(Nasabah n) {
 		nasabahDao.simpan(n);
 	}
 	
 	public Nasabah cariNasabahById(String id) {
 		return nasabahDao.findNasabahById(id);
 	}
 
 	public Nasabah cariNasabahByNomer(String nomer) {
 		return nasabahDao.findNasabahByNomer(nomer);
 	}
 
 	public Long hitungSemuaNasabah() {
 		return nasabahDao.hitungSemua();
 	}
 
 	public List<Nasabah> cariSemuaNasabah(Integer start, Integer rows) {
 		return nasabahDao.cariSemua(start, rows);
 	}
 
 	public void simpan(Kolektor k) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public Kolektor cariKolektorById(String id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Long hitungSemuaKolektor() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<Kolektor> cariSemuaKolektor(Integer start, Integer rows) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void simpan(Tagihan t) {
 		tagihanDao.simpan(t);
 	}
 
 	public Tagihan cariTagihanById(TagihanPK id) {
 		return tagihanDao.cariTagihanById(id);
 	}
 
 	public Long hitungTagihanByPeriodeJatuhTempo(Date mulai, Date sampai) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<Tagihan> cariTagihanByPeriodeJatuhTempo(Date mulai,
 			Date sampai, Integer start, Integer rows) {
 		return tagihanDao.cariTagihanByPeriodeJatuhTempo(mulai, sampai, start, rows);
 	}
 
 	public Long hitungTagihanByNasabahDanStatus(Nasabah n, StatusTagihan status) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<Tagihan> cariTagihanByNasabahDanStatus(Nasabah n,
 			StatusTagihan status, Integer start, Integer rows) {
 		return tagihanDao.cariTagihanByNasabahDanStatus(n,status,start,rows);
 	}
 
 	public BigDecimal totalOutstandingByNasabah(Nasabah n) {
 		return tagihanDao.totalOutstandingByNasabah(n);
 	}
 
 	public Map<String, BigDecimal> rekapOutstandingMap() {
 		return tagihanDao.rekapOutstandingMap();
 	}
 
 	public List<RekapOutstanding> rekapOutstandingObject() {
 		return tagihanDao.rekapOutstandingObject();
 	}
 
 	public List<Tagihan> cariTagihanByNomerNasabah(String nomer) {
 		return tagihanDao.cariTagihanByNomerNasabah(nomer);
 	}
 
 	public List<Pembayaran> cariPembayaranByNomerNasabah(String nomer) {
 		return pembayaranDao.cariPembayaranByNomerNasabah(nomer);
 	}
 
 	public void simpan(Pembayaran p) {
 		Tagihan t = p.getTagihan();
 		t.setStatus(StatusTagihan.LUNAS);
 		
 		pembayaranDao.simpan(p);
 		tagihanDao.simpan(t); // kalau ini exception, maka insert pembayaran akan rollback
 	}
 
 }
