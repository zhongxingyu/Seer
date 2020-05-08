 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package perpus.service;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import net.sf.jasperreports.engine.JREmptyDataSource;
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 import org.hibernate.SessionFactory;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import perpus.domain.Anggota;
 import perpus.domain.Buku;
 import perpus.domain.LaporanPeminjamanDto;
 import perpus.domain.PeminjamanDetail;
 import perpus.domain.PengembalianDetail;
 
 /**
  *
  * @author adi
  */
 @Service("reportService") @Transactional(readOnly=true)
 public class ReportServiceImpl implements ReportService{
     
     @Autowired private SessionFactory sessionFactory;
     @Autowired private TransaksiService transaksiService;
     
     public List<LaporanPeminjamanDto> dataLaporanPeminjaman(String mode, Date mulai, Date sampai){
         List<LaporanPeminjamanDto> result = new ArrayList<LaporanPeminjamanDto>();
         
         if(mode.equalsIgnoreCase("PEMINJAMAN")){
             List<PeminjamanDetail> detailPeminjamans = transaksiService.getTransaksiBelumKembali(mulai, sampai);
             for(PeminjamanDetail p : detailPeminjamans){
                 PengembalianDetail kembali = 
                         transaksiService.getPengembalianByIdPinjamAndKodeBuku(
                             p.getHeader().getId(), p.getBuku().getId());
 
                 LaporanPeminjamanDto lpd = new LaporanPeminjamanDto();
                 lpd.setId(p.getHeader().getId().toString());
                 lpd.setKodeAnggota(p.getHeader().getAnggota().getKodeAnggota());
                 lpd.setNamaAnggota(p.getHeader().getAnggota().getNamaAnggota());
                 lpd.setTglPinjam(p.getHeader().getTglPinjam());
                 lpd.setTglKembali(p.getTglKembali());
                 lpd.setKodeBuku(p.getBuku().getKodeBuku());
                 lpd.setJudul(p.getBuku().getJudulBuku());
 
                 if(kembali != null){
                     lpd.setTglKembaliSebenarnya(new DateTime(lpd.getTglKembali())
                             .plusDays(kembali.getTelat()).toDate());
                     lpd.setTelat(kembali.getTelat());
                     lpd.setDenda(kembali.getDenda());
                     lpd.setStatus("Sudah Kembali");
                 } else {
                     lpd.setStatus("Belum Kembali");
                 }
                 result.add(lpd);
             }
         } else {
             List<PengembalianDetail> detailpengembalians = 
                     transaksiService.getTransaksiPengembalian(mulai, sampai);
             for (PengembalianDetail p : detailpengembalians) {
                 PeminjamanDetail pinjam = transaksiService.getTransaksiPeminjamanByIdAndBuku(
                         p.getHeader().getTransaksiPeminjaman().getId(), 
                         p.getBuku().getId());
                 LaporanPeminjamanDto lpd = new LaporanPeminjamanDto();
                 lpd.setId(p.getHeader().getId().toString());
                 lpd.setKodeAnggota(p.getHeader().getTransaksiPeminjaman().getAnggota().getKodeAnggota());
                 lpd.setNamaAnggota(p.getHeader().getTransaksiPeminjaman().getAnggota().getNamaAnggota());
                 lpd.setTglPinjam(p.getHeader().getTransaksiPeminjaman().getTglPinjam());
                 lpd.setTglKembali(pinjam.getTglKembali());
                 lpd.setKodeBuku(p.getBuku().getKodeBuku());
                 lpd.setJudul(p.getBuku().getJudulBuku());
                 lpd.setTglKembaliSebenarnya(p.getCreatedDate());
                 lpd.setTelat(p.getTelat());
                 lpd.setDenda(p.getDenda());
                 lpd.setStatus("Sudah Kembali");
                 result.add(lpd);
             }
         }
         return result;
     }
     
     @Override
     public JasperPrint printLaporanPeminjaman(String mode, Date mulai, Date sampai){
         try {
             InputStream inputStream = 
                     getClass().getResourceAsStream("/perpus/jrxml/LaporanPeminjaman.jasper");
             List<LaporanPeminjamanDto> listWrappers = dataLaporanPeminjaman(mode, mulai, sampai);
             Map<String, Object> parameters = new HashMap<String, Object>();
             parameters.put("tglMulai", mulai);
             parameters.put("tglSampai", sampai);
             
             if(mode.equalsIgnoreCase("PEMINJAMAN")){
                 parameters.put("title", "LAPORAN PEMINJAMAN BUKU");
             } else {
                 parameters.put("title", "LAPORAN PENGEMBALIAN BUKU");
             }
 
             JasperPrint j =
                     JasperFillManager.fillReport(
                     inputStream, parameters, new JRBeanCollectionDataSource(listWrappers));
 
             return j;
         } catch (JRException ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     /**
      * Method printLaporanBuku
      * @params
      * @param params[0] adalah nama kolom
      * @param params[1] adalah nama relational
      * @param params[2] adalah nama value
      * @return JasperPrint
      */
     @Override
     public JasperPrint printLaporanBuku(String...params) {
         try {
             InputStream inputStream = 
                     getClass().getResourceAsStream("/perpus/jrxml/LaporanBuku.jasper");
             List<Buku> bukus = dataLaporanBuku(params);
             Map<String, Object> parameters = new HashMap<String, Object>();
             
             JasperPrint j =
                     JasperFillManager.fillReport(
                     inputStream, parameters, new JRBeanCollectionDataSource(bukus));
 
             return j;
         } catch (JRException ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     private List<Buku> dataLaporanBuku(String...params) {
         if(params.length < 3){
             return sessionFactory.getCurrentSession()
                     .createQuery("from Buku b order by b.jenisBuku asc, b.createdDate asc")
                     .list();
         } else {
             String kolom = params[0];
             String relational = params[1];
             String value = params[2];
             
             StringBuilder sb = new StringBuilder("select b from Buku b where ");
             
             if(kolom.equalsIgnoreCase("KODE")){
                 sb.append("b.kodeBuku " + relational + "'" + value + "' ");
             } else if(kolom.equalsIgnoreCase("JUDUL")){
                 sb.append("b.judulBuku " + relational + "'" + value + "' ");
             } else if(kolom.equalsIgnoreCase("JENIS")){
                 sb.append("b.jenisBuku " + relational + "'" + value + "' ");
             } else if(kolom.equalsIgnoreCase("KOTA_TERBIT")){
                 sb.append("b.kotaTerbit " + relational + "'" + value + "' ");
             } else if(kolom.equalsIgnoreCase("PENERBIT")){
                 sb.append("b.penerbit " + relational + "'" + value + "' ");
             } else if(kolom.equalsIgnoreCase("PENGARANG")){
                 sb.append("b.pengarang " + relational + "'" + value + "' ");
             } else if(kolom.equalsIgnoreCase("TAHUN_TERBIT")){
                sb.append("b.tahunTerbit " + relational + "'" + value + "' ");
             } else if(kolom.equalsIgnoreCase("JUMLAH")){
                 sb.append("b.jumlahBuku " + relational + "'" + value + "' ");
             }
             
             sb.append("order by b.jenisBuku asc, b.createdDate asc ");
             
             return sessionFactory.getCurrentSession()
                     .createQuery(sb.toString())
                     .list();
         }
     }
 
     @Override
     public JasperPrint printKartuAnggota(Anggota anggota) {
         try {
             InputStream inputStream = 
                     getClass().getResourceAsStream("/perpus/jrxml/KartuAnggota.jasper");
             Map<String, Object> parameters = new HashMap<String, Object>();
             parameters.put("noAnggota", anggota.getKodeAnggota());
             parameters.put("nama", anggota.getNamaAnggota());
             parameters.put("jkel", anggota.getJenisKelamin());
             parameters.put("alamat", anggota.getAlamat());
             parameters.put("agama", anggota.getAgama());
             parameters.put("telp", anggota.getNoTelp());
             parameters.put("email", anggota.getEmail());
             parameters.put("berlaku", new DateTime(anggota.getTahunMasuk()).plusYears(1).toDate());
             
             JasperPrint j =
                     JasperFillManager.fillReport(
                     inputStream, parameters, new JREmptyDataSource());
 
             return j;
         } catch (JRException ex) {
             ex.printStackTrace();
             return null;
         }
     }
 }
