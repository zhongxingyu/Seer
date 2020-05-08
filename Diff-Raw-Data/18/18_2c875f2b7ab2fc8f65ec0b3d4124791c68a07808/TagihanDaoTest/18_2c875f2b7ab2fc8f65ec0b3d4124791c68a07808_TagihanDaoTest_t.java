 package com.artivisi.penagihan.dao;
 
 import java.io.File;
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.sql.DataSource;
 
 import org.dbunit.database.DatabaseConnection;
 import org.dbunit.dataset.CompositeDataSet;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
 import org.dbunit.operation.DatabaseOperation;
 import org.joda.time.DateTime;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.artivisi.penagihan.domain.Nasabah;
 import com.artivisi.penagihan.domain.PenagihanService;
 import com.artivisi.penagihan.domain.RekapOutstanding;
 import com.artivisi.penagihan.domain.StatusTagihan;
 import com.artivisi.penagihan.domain.Tagihan;
 import com.artivisi.penagihan.domain.TagihanPK;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath*:com/artivisi/**/applicationContext.xml")
 public class TagihanDaoTest {
 	@Autowired
 	private PenagihanService penagihanService;
 
 	@Autowired
 	private DataSource dataSource;
 
 	@Before
 	public void hapusDataTest() throws Exception {
 		IDataSet[] daftarDataset = new IDataSet[] {
 				new FlatXmlDataSetBuilder().build(new File(
 						"src/test/resources/dbunit/nasabah.xml")),
 				new FlatXmlDataSetBuilder().build(new File(
						"src/test/resources/dbunit/tagihan.xml")),
						new FlatXmlDataSetBuilder().build(new File(
								"src/test/resources/dbunit/pembayaran.xml")) };
 
 		Connection conn = dataSource.getConnection();
 
 		// untuk oracle, harus pakai skema
 		/*
 		 * DatabaseOperation.CLEAN_INSERT.execute (new DatabaseConnection(conn,
 		 * "skema"), new CompositeDataSet(daftarDataset));
 		 */
 
 		// non oracle, tidak pakai skema
 		DatabaseOperation.CLEAN_INSERT.execute(new DatabaseConnection(conn),
 				new CompositeDataSet(daftarDataset));
 
 		conn.close();
 	}
 
 	@Test
 	public void testSimpan() throws Exception {
 		TagihanPK pk = new TagihanPK();
 		pk.setKodeCabang("CAB-002");
 		pk.setNomerBooking("BOOK-001");
 
 		Tagihan t = new Tagihan();
 		t.setId(pk);
 
 		Nasabah n = new Nasabah();
 		n.setId("abcd1234");
 		t.setNasabah(n);
 
 		t.setNilai(new BigDecimal(101000.01));
 		t.setStatus(StatusTagihan.BELUM_DIBAYAR);
 		t.setTanggalJatuhTempo(new DateTime().withDayOfMonth(20).toDate());
 		t.setUser("endy");
 		t.setWaktuBooking(new Date());
 
 		penagihanService.simpan(t);
 
 		String sql = "select * from t_tagihan where kode_cabang = ? and nomer_booking = ?";
 		Connection conn = dataSource.getConnection();
 
 		PreparedStatement ps = conn.prepareStatement(sql);
 		ps.setString(1, "CAB-002");
 		ps.setString(2, "BOOK-001");
 		ResultSet rs = ps.executeQuery();
 		Assert.assertTrue(rs.next());
 
 		conn.close();
 	}
 
 	@Test
 	public void testCariByNasabahDanStatus() {
 		Nasabah n = new Nasabah();
 		n.setId("abcd1234");
 
 		List<Tagihan> hasil = penagihanService.cariTagihanByNasabahDanStatus(n,
 				StatusTagihan.BELUM_DIBAYAR, 0, 10);
 
 		Assert.assertTrue(hasil.size() == 2);
 
 		List<Tagihan> hasil2 = penagihanService.cariTagihanByNasabahDanStatus(
 				n, StatusTagihan.LUNAS, 0, 10);
 
 		Assert.assertTrue(hasil2.size() == 1);
 
 		Tagihan t = hasil2.get(0);
 
 		// test composite PK
 		Assert.assertEquals("CAB-123", t.getId().getKodeCabang());
 		Assert.assertEquals("BOOK-001", t.getId().getNomerBooking());
 
 		// test simple value
 		Assert.assertEquals(new BigDecimal("125000.00"), t.getNilai());
 
 		// test relasi
 		Assert.assertEquals("Anton", t.getNasabah().getNama());
 	}
 
 	@Test
 	public void testCariByTanggalJatuhTempo() {
 		Date mulai = new DateTime(2013, 1, 1, 0, 0, 0, 0).toDate();
 		Date sampai = new DateTime(2013, 3, 1, 0, 0, 0, 0).toDate();
 
 		List<Tagihan> hasil = penagihanService.cariTagihanByPeriodeJatuhTempo(
 				mulai, sampai, 0, 100);
 		Assert.assertTrue(hasil.size() == 2);
 
 		List<Tagihan> hasil2 = penagihanService
 				.cariTagihanByPeriodeJatuhTempo(new DateTime(mulai)
 						.withMonthOfYear(2).toDate(), sampai, 0, 100);
 		Assert.assertTrue(hasil2.size() == 1);
 
 		Tagihan t = hasil2.get(0);
 
 		// test composite PK
 		Assert.assertEquals("CAB-123", t.getId().getKodeCabang());
 		Assert.assertEquals("BOOK-002", t.getId().getNomerBooking());
 
 		// test simple value
 		Assert.assertEquals(new BigDecimal("125000.00"), t.getNilai());
 
 		// test relasi
 		Assert.assertEquals("Anton", t.getNasabah().getNama());
 	}
 	
 	@Test
 	public void testTotalOutstandingByNasabah(){
 		Nasabah n = new Nasabah();
 		n.setId("abcd1234");
 		
 		BigDecimal seharusnya = new BigDecimal("250000.00");
 		Assert.assertEquals(seharusnya, penagihanService.totalOutstandingByNasabah(n));
 	}
 	
 	@Test
 	public void testRekapOutstandingMap(){
 		Map<String, BigDecimal> hasil = penagihanService.rekapOutstandingMap();
 		Assert.assertTrue(hasil.size() == 1);
 		
 		BigDecimal outstanding = hasil.get("abcd1234");
 		Assert.assertEquals(new BigDecimal("250000.00"), outstanding);
 	}
 	
 	@Test
 	public void testRekapOutstandingObject(){
 		List<RekapOutstanding> hasil = penagihanService.rekapOutstandingObject();
 		Assert.assertTrue(hasil.size() == 1);
 		
 		RekapOutstanding r = hasil.get(0);
 		Assert.assertEquals("Anton", r.getNasabah().getNama());
 		Assert.assertEquals(new BigDecimal("250000.00"), r.getOutstanding());
 	}
 	
 	@Test
 	public void testCariTagihanByNomerNasabah(){
 		List<Tagihan> hasil = penagihanService.cariTagihanByNomerNasabah("N-123");
 		Assert.assertTrue(hasil.size() == 3);
 	}
 }
