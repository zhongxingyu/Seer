 package ee.itcollege.p0rn.entities;
 
 import org.springframework.roo.addon.entity.RooEntity;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.tostring.RooToString;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.EntityManager;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PrePersist;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import org.springframework.format.annotation.DateTimeFormat;
 import ee.itcollege.p0rn.entities.Piiririkkuja;
 import javax.persistence.ManyToOne;
 
 @RooJavaBean
 @RooToString
 @RooEntity
 public class Kodakondsus extends Base {
     @PersistenceContext
     transient EntityManager entityManager;
     
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
     private Long kodakondsus_ID;
 
 	@DateTimeFormat(pattern = "dd.MM.yyyy")
     private Date alates;
 
     @DateTimeFormat(pattern = "dd.MM.yyyy")
     private Date kuni;
 
     @ManyToOne
     private Riik riik_ID;
 
     @Size(max = 20)
     private String isikukood;
 
     @ManyToOne
     private Piiririkkuja piiririkkuja_ID;
     
     public static final EntityManager entityManager() {
         EntityManager em = new Kodakondsus().entityManager;
         if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
         return em;
     }
     
     public static List<Kodakondsus> findAllByPiiririkkuja(long id) {
         return entityManager().createQuery("SELECT o FROM Kodakondsus o WHERE suletud > CURDATE() AND piiririkkuja_ID = " + id, Kodakondsus.class).getResultList();
     }
 
     public static void importFUCK() {
     	if (Riik.countRiiks() < 1) {
 	    	Riik a = new Riik();
 	    	a.setISO_kood("EE");
 	    	a.setANSI_kood("EST");
 	    	a.setKommentaar("");
 	    	a.persist();
 	    	Riik b = new Riik();
 	    	b.setISO_kood("EN");
 	    	b.setANSI_kood("ENG");
 	    	b.setKommentaar("");
 	    	b.persist();
 	    	Riik c = new Riik();
 	    	c.setISO_kood("DE");
 	    	c.setANSI_kood("DEU");
 	    	c.setKommentaar("");
 	    	c.persist();
 	    	
 	    	Piiririkkuja d = new Piiririkkuja();
 	    	d.setEesnimi("Jaan");
 	    	d.setPerek_nimi("Kuusk");
 	    	d.setIsikukood("4342346000");
 	    	d.setKommentaar("");
 	    	d.setSynniaeg(new Date());
 	    	d.persist();
 	    	
 	    	Piiririkkuja d2 = new Piiririkkuja();
 	    	d2.setEesnimi("Triin");
 	    	d2.setPerek_nimi("Tamm");
 	    	d2.setIsikukood("50003234444");
 	    	d2.setKommentaar("");
 	    	d2.setSynniaeg(new Date());
 	    	d2.persist();
 	    	
 	    	Kodakondsus e = new Kodakondsus();
 	    	e.setIsikukood("1001");
 	    	e.setAlates(new Date());
 	    	e.setKuni(new Date());
 	    	e.setKommentaar("");
 	    	e.setPiiririkkuja_ID(d);
 	    	e.setRiik_ID(a);
 	    	e.persist();
 	    	
 	    	Kodakondsus f = new Kodakondsus();
 	    	f.setIsikukood("1001");
 	    	f.setAlates(new Date());
 	    	f.setKuni(new Date());
 	    	f.setKommentaar("");
 	    	f.setPiiririkkuja_ID(d);
 	    	f.setRiik_ID(b);
 	    	f.persist();
 	    	
 	    	Kodakondsus g = new Kodakondsus();
 	    	g.setIsikukood("1001");
 	    	g.setAlates(new Date());
 	    	g.setKuni(new Date());
 	    	g.setKommentaar("");
 	    	g.setPiiririkkuja_ID(d);
 	    	g.setRiik_ID(c);
 	    	g.persist();
 	    	
 	    	Kodakondsus g2 = new Kodakondsus();
 	    	g2.setIsikukood("1001");
 	    	g2.setAlates(new Date());
 	    	g2.setKuni(new Date());
 	    	g2.setKommentaar("");
 	    	g2.setPiiririkkuja_ID(d2);
 	    	g2.setRiik_ID(c);
 	    	g2.persist();
 	    
 	    	Seadus s = new Seadus();
	    	s.setKehtiv_alates("2010-30-12");
	    	s.setKehtiv_kuni("2011-30-12");
 	    	s.setKommentaar("");
 	    	s.setKood("AA");
 	    	s.setNimetus("Relva seadus");
 	    	s.persist();
 	    	
 	    	Seadus s2 = new Seadus();
	    	s2.setKehtiv_alates("2012-30-12");
	    	s2.setKehtiv_kuni("2015-30-12");
 	    	s2.setKommentaar("");
 	    	s2.setKood("ABWS");
 	    	s2.setNimetus("Raha seadus");
 	    	s2.persist();
 	    	
 	    	SeadusePunkt sp1 = new SeadusePunkt();
 	    	sp1.setParagrahv("1");
 	    	sp1.setPais("Palju raha punkt");
 	    	sp1.setTekst("Kõigile raha");
 	    	sp1.setKommentaar("Ausad oleme");
	    	sp1.setKehtiv_alates("2011-25-01");
	    	sp1.setKehtiv_kuni("2012-30-12");
 	    	sp1.setSeaduse_ID(s2);
 	    	sp1.setYlemus_seaduse_punkt_ID(null);
 	    	sp1.persist();
 	    	
 	    	SeadusePunkt sp2 = new SeadusePunkt();
 	    	sp2.setParagrahv("2");
 	    	sp2.setPais("Täpsustus");
 	    	sp2.setTekst("Rikastele!");
 	    	sp2.setKommentaar("Problem?");
	    	sp2.setKehtiv_alates("2011-25-01");
	    	sp2.setKehtiv_kuni("2020-30-12");
 	    	sp2.setSeaduse_ID(s2);
 	    	sp2.setYlemus_seaduse_punkt_ID(sp1);
 	    	sp2.persist();
 	    	
 	    	SeadusePunkt sp3 = new SeadusePunkt();
 	    	sp3.setParagrahv("3");
 	    	sp3.setPais("Relvad!");
 	    	sp3.setTekst("Lastele relvi!");
 	    	sp3.setKommentaar("Tehtud!");
	    	sp3.setKehtiv_alates("2012-25-01");
	    	sp3.setKehtiv_kuni("2012-30-12");
 	    	sp3.setSeaduse_ID(s);
 	    	sp3.setYlemus_seaduse_punkt_ID(null);
 	    	sp3.persist();
     	}
     }
 
 	@Override
 	public String getTableName() {
 		return "Kodakondsus";
 	}
 
 	@Override
 	public String getIdName() {
 		return "kodakondsus_ID";
 	}
 
 	@Override
 	public Long getId() {
 		return kodakondsus_ID;
 	}
 	
     public static long countKodakondsuses() {
         return entityManager().createQuery("SELECT COUNT(o) FROM Kodakondsus o WHERE suletud > CURDATE()", Long.class).getSingleResult();
     }
     
     public static List<Kodakondsus> findAllKodakondsuses() {
         return entityManager().createQuery("SELECT o FROM Kodakondsus o WHERE suletud > CURDATE()", Kodakondsus.class).getResultList();
     }
 
     public static List<Kodakondsus> findKodakondsusEntries(int firstResult, int maxResults) {
         return entityManager().createQuery("SELECT o FROM Kodakondsus o WHERE suletud > CURDATE()", Kodakondsus.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
     }
 }
