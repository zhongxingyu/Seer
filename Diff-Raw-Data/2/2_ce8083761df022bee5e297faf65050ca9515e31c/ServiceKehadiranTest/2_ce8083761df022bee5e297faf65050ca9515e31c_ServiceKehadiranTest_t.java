 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.artivisi.absensi.service;
 
 import com.artivisi.absensi.domain.Kehadiran;
 import com.artivisi.absensi.domain.Peserta;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  *
  * @author endy
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations="classpath*:com/artivisi/**/applicationContext.xml")
 public class ServiceKehadiranTest {
     
     @Autowired
     private AplikasiAbsenService service;
     
     @Test
     public void testCariSemua() throws Exception {
         List<Kehadiran> hasilQuery = service.cariSemuaKehadiran();
         System.out.println("Jumlah Record : "+hasilQuery.size());
         for (Kehadiran kehadiran : hasilQuery) {
             System.out.println("ID : "+kehadiran.getId());
             System.out.println("Jam Masuk : "+kehadiran.getJamMasuk());
             System.out.println("Jam Pulang : "+kehadiran.getJamPulang());
             System.out.println("Karyawan : "
                     +kehadiran.getPeserta().getNomor()
                     +" - "
                     +kehadiran.getPeserta().getNama());
         }
     }
     
     @Test
     public void testInsert() throws Exception {
         Peserta p = new Peserta();
         p.setId(6);
         
         Kehadiran k = new Kehadiran();
         k.setJamMasuk(new Date());
         k.setJamPulang(new Date());
         k.setPeserta(p);
         
         service.simpan(k);
     }
     
     @Test
     public void testUpdate() throws Exception {
         Kehadiran k = new Kehadiran();
        k.setId(7);
         k.setJamMasuk(new Date());
         k.setJamPulang(new Date());
         
         service.simpan(k);
     }
     
     @Test
     public void testCariKehadiranDenganPaging(){
         int start = 0;
         int rows = 10;
         List<Kehadiran> hasil = service.cariSemuaKehadiran(start, rows);
     }
     
     @Test
     public void testCariKehadiranByPeriode() throws Exception {
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
         
         Date mulai  = formatter.parse("2013-01-01");
         Date sampai  = formatter.parse("2013-01-30");
         
         int start = 0;
         int rows = 10;
         List<Kehadiran> hasil = service.cariKehadiranDalamPeriode(mulai, sampai, start, rows);
     }
     
     @Test
     public void testCariKehadiranByNamaPeserta() throws Exception {
         String nama = "nda";
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
         
         Date mulai  = formatter.parse("2013-01-01");
         Date sampai  = formatter.parse("2013-01-30");
         int start = 0;
         int rows = 10;
         List<Kehadiran> hasil = service.cariKehadiranPesertaByNamaDanPeriode(nama, mulai, sampai, start, rows);
     }
 }
