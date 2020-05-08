 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import models.Agent;
 import models.Customer;
 import models.Operational;
 import models.Owner;
 import models.Port;
 import models.Tariff;
 import models.User;
 import models.Vessel;
 import play.jobs.*;
 import play.modules.morphia.utils.MorphiaFixtures;
 import play.test.Fixtures;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
 	
 	public void doJob() throws Exception {
		//MorphiaFixtures.deleteAll();
 		if(User.count() ==0) {
 			User tito = new User("tito", "nindia", "tito@cosco-ogs.com");
 			tito.isAdmin = true;
 			tito.isManager = true;
 			tito.save();
 			
 			User mundir = new User("mundir", "mundir", "mundir@cosco-ogs.com");
 			mundir.isAdmin = false;
 			mundir.isManager = false;
 			mundir.save();
 			
 			User tonny = new User("tonny", "tonny", "tonny@cosco-ogs.com");
 			tonny.save();
 			
 			new Owner("COSCON", "Shanghai", "tito@cosco-ogs.com").save();
 	    	Owner coscon = Owner.find("byName", "COSCON").first();
 	    	
 	    	new Vessel("Hu Tuo He", 10100).save();
 	    	new Vessel("YJH", "Yang Jiang He", 10000, coscon).save();
 	    	Vessel vsl = Vessel.find("byName", "Yang Jiang He").first();   	
 	    	
 	    	Tariff jktcost = new Tariff("A", 0.1, 0.1, 37.5, 0.018, 0.034);
 	    	Tariff subcost = new Tariff("A", 0.084, 0.111, 42.0, 0.014, 0.034);
 	    	Tariff blwcost = new Tariff("A", 0.096, 0.111, 49.0, 0.018, 0.034);
 	    	Tariff smgcost = new Tariff("A", 0.082, 0.088, 42.0, 0.014, 0.034);
 	    	Tariff clpcost = new Tariff("A", 0.082, 0.088, 42.0, 0.014, 0.034);
 	    	Tariff bnocost = new Tariff("A", 0.082, 0.088, 42.0, 0.014, 0.034);
 	    	Tariff grscost = new Tariff("A", 0.082, 0.088, 42, 0.014, 0.034);
 	    	Tariff bjrcost = new Tariff("A", 0.082, 0.0, 0.0, 0.0, 0.034);
 	    	Tariff smrcost = new Tariff("A", 0.088, 0.0, 0.0, 0.0, 0.034);
 	    	Tariff ktbcost = new Tariff("A", 0.088, 0.0, 0.0, 0.0, 0.034);
 	    	Tariff kltcost = new Tariff("A", 0.082, 0.088, 49.0, 0.018, 0.034);
 	    	Tariff lhkcost = new Tariff("A", 0.082, 0.088, 49.0, 0.018, 0.034);
 	    	Tariff dumcost = new Tariff("A", 0.082, 0.088, 49.0, 0.018, 0.034);
 	    	Tariff tjpcost = new Tariff("A", 0.096, 0.0, 0.0, 0.0, 0.034);
 	    	Tariff morocost = new Tariff("A", 0.035, 0.0, 0.0, 0.0, 0.034);
 	    	Tariff dabocost = new Tariff("A", 0.035, 0.0, 0.0, 0.0, 0.034);
 	    	Tariff kdwgcost = new Tariff("A", 0.035, 0.0, 0.0, 0.0, 0.034);
 	    	Tariff kjngcost = new Tariff("A", 0.096, 0.088, 49.0, 0.018, 0.034);
 	    	Tariff lbacost = new Tariff("A", 0.035, 0.0, 0.0, 0.0, 0.034);
 	    	Tariff msacost = new Tariff("A", 0.088, 0.0, 0.0, 0.0, 0.034);
 	    	Tariff pjgcost = new Tariff("A", 0.088, 0.086, 37.5, 0.018, 0.034);
 	    	Tariff cgdcost = new Tariff("A", 0.088, 0.086, 37.5, 0.018, 0.034);
 
 	    	jktcost.addTugTariff(1, 3500, 181.25, 0.0072);
 	    	jktcost.addTugTariff(3501, 8000, 468.75, 0.0072);
 	    	jktcost.addTugTariff(8001, 14000, 712.5, 0.0072);
 	    	jktcost.addTugTariff(14001, 18000, 1525.0, 0.0072);
 	    	jktcost.addTugTariff(18001, 26000, 1525.0, 0.0072);
 	    	jktcost.addTugTariff(26001, 40000, 1525.0, 0.0072);
 	    	jktcost.addTugTariff(40001, 75000, 1525.0, 0.0072);
 	    	jktcost.addTugTariff(75001, 9999999, 1525.0, 0.0072);
 	    	new Port("Jakarta", jktcost).save();
 
 	    	subcost.addTugTariff(1, 3500, 145.0, 0.004);
 	    	subcost.addTugTariff(3501, 8000, 375.0, 0.004);
 	    	subcost.addTugTariff(8001, 14000, 570.0, 0.004);
 	    	subcost.addTugTariff(14001, 18000, 770.0, 0.004);
 	    	subcost.addTugTariff(18001, 26000, 1220.0, 0.004);
 	    	subcost.addTugTariff(26001, 40000, 1220.0, 0.004);
 	    	subcost.addTugTariff(40001, 75000, 1300.0, 0.002);
 	    	subcost.addTugTariff(75001, 9999999, 1700.0, 0.002);
 	    	new Port("Surabaya", subcost).save();
 
 	    	blwcost.addTugTariff(1, 3500, 174.0, 0.0048);
 	    	blwcost.addTugTariff(3501, 8000, 450.0, 0.0048);
 	    	blwcost.addTugTariff(8001, 14000, 924.0, 0.0040);
 	    	blwcost.addTugTariff(14001, 18000, 770.0, 0.004);
 	    	blwcost.addTugTariff(18001, 26000, 1220.0, 0.004);
 	    	blwcost.addTugTariff(26001, 40000, 1220.0, 0.004);
 	    	blwcost.addTugTariff(40001, 75000, 1300.0, 0.002);
 	    	blwcost.addTugTariff(75001, 9999999, 1700.0, 0.002);
 	    	new Port("Belawan", blwcost).save();
 
 	    	smgcost.addTugTariff(1, 3500, 145.0, 0.0048);
 	    	smgcost.addTugTariff(3501, 8000, 375.0, 0.0048);
 	    	smgcost.addTugTariff(8001, 14000, 570.0, 0.0040);
 	    	smgcost.addTugTariff(14001, 18000, 770.0, 0.004);
 	    	smgcost.addTugTariff(18001, 26000, 1220.0, 0.004);
 	    	smgcost.addTugTariff(26001, 40000, 1220.0, 0.004);
 	    	smgcost.addTugTariff(40001, 75000, 1300.0, 0.002);
 	    	smgcost.addTugTariff(75001, 9999999, 1700.0, 0.002);
 	    	new Port("Semarang", smgcost).save();
 
 	    	clpcost.addTugTariff(1, 3500, 145.0, 0.0048);
 	    	clpcost.addTugTariff(3501, 8000, 375.0, 0.0048);
 	    	clpcost.addTugTariff(8001, 14000, 570.0, 0.0040);
 	    	clpcost.addTugTariff(14001, 18000, 770.0, 0.004);
 	    	clpcost.addTugTariff(18001, 26000, 1220.0, 0.004);
 	    	clpcost.addTugTariff(26001, 40000, 1220.0, 0.004);
 	    	clpcost.addTugTariff(40001, 75000, 1300.0, 0.002);
 	    	clpcost.addTugTariff(75001, 9999999, 1700.0, 0.002);
 	    	new Port("Cilacap", clpcost).save();
 
 	    	bnocost.addTugTariff(1, 3500, 145.0, 0.0048);
 	    	bnocost.addTugTariff(3501, 8000, 375.0, 0.0048);
 	    	bnocost.addTugTariff(8001, 14000, 570.0, 0.0040);
 	    	bnocost.addTugTariff(14001, 18000, 770.0, 0.004);
 	    	bnocost.addTugTariff(18001, 26000, 1220.0, 0.004);
 	    	bnocost.addTugTariff(26001, 40000, 1220.0, 0.004);
 	    	bnocost.addTugTariff(40001, 75000, 1300.0, 0.002);
 	    	bnocost.addTugTariff(75001, 9999999, 1700.0, 0.002);
 	    	new Port("Benoa", bnocost).save();
 
 	    	grscost.addTugTariff(1, 3500, 145.0, 0.0048);
 	    	grscost.addTugTariff(3501, 8000, 375.0, 0.0048);
 	    	grscost.addTugTariff(8001, 14000, 924.0, 0.0040);
 	    	grscost.addTugTariff(14001, 18000, 770.0, 0.004);
 	    	grscost.addTugTariff(18001, 26000, 1220.0, 0.004);
 	    	grscost.addTugTariff(26001, 40000, 1220.0, 0.004);
 	    	grscost.addTugTariff(40001, 75000, 1300.0, 0.002);
 	    	grscost.addTugTariff(75001, 9999999, 1700.0, 0.002);
 	    	new Port("Gresik", grscost).save();
 
 	    	bjrcost.addTugTariff(1, 3500, 0.0, 0.0);
 	    	bjrcost.addTugTariff(3501, 8000, 0.0, 0.0);
 	    	bjrcost.addTugTariff(8001, 14000, 0.0, 0.0);
 	    	bjrcost.addTugTariff(14001, 18000, 0.0, 0.0);
 	    	bjrcost.addTugTariff(18001, 26000, 0.0, 0.0);
 	    	bjrcost.addTugTariff(26001, 40000, 0.0, 0.0);
 	    	bjrcost.addTugTariff(40001, 75000, 0.0, 0.0);
 	    	bjrcost.addTugTariff(75001, 9999999, 0.0, 0.0);
 	    	new Port("Banjarmasin", bjrcost).save();
 
 	    	smrcost.addTugTariff(1, 3500, 0.0, 0.0);
 	    	smrcost.addTugTariff(3501, 8000, 0.0, 0.0);
 	    	smrcost.addTugTariff(8001, 14000, 0.0, 0.0);
 	    	smrcost.addTugTariff(14001, 18000, 0.0, 0.0);
 	    	smrcost.addTugTariff(18001, 26000, 0.0, 0.0);
 	    	smrcost.addTugTariff(26001, 40000, 0.0, 0.0);
 	    	smrcost.addTugTariff(40001, 75000, 0.0, 0.0);
 	    	smrcost.addTugTariff(75001, 9999999, 0.0, 0.0);
 	    	new Port("Samarinda", smrcost).save();
 
 	    	ktbcost.addTugTariff(1, 3500, 0.0, 0.0);
 	    	ktbcost.addTugTariff(3501, 8000, 0.0, 0.0);
 	    	ktbcost.addTugTariff(8001, 14000, 0.0, 0.0);
 	    	ktbcost.addTugTariff(14001, 18000, 0.0, 0.0);
 	    	ktbcost.addTugTariff(18001, 26000, 0.0, 0.0);
 	    	ktbcost.addTugTariff(26001, 40000, 0.0, 0.0);
 	    	ktbcost.addTugTariff(40001, 75000, 0.0, 0.0);
 	    	ktbcost.addTugTariff(75001, 9999999, 0.0, 0.0);
 	    	new Port("Kotabaru", ktbcost).save();
 
 	    	kltcost.addTugTariff(1, 3500, 450.0, 0.0048);
 	    	kltcost.addTugTariff(3501, 8000, 684.0, 0.0048);
 	    	kltcost.addTugTariff(8001, 14000, 924.0, 0.0048);
 	    	kltcost.addTugTariff(14001, 18000, 1464.0, 0.0048);
 	    	kltcost.addTugTariff(18001, 26000, 1464.0, 0.0048);
 	    	kltcost.addTugTariff(26001, 40000, 1560.0, 0.0024);
 	    	kltcost.addTugTariff(40001, 75000, 1560.0, 0.0024);
 	    	kltcost.addTugTariff(75001, 9999999, 2040.0, 0.0024);
 	    	new Port("Kuala Tanjung", kltcost).save();
 
 	    	lhkcost.addTugTariff(1, 3500, 450.0, 0.0048);
 	    	lhkcost.addTugTariff(3501, 8000, 684.0, 0.0048);
 	    	lhkcost.addTugTariff(8001, 14000, 924.0, 0.0048);
 	    	lhkcost.addTugTariff(14001, 18000, 1464.0, 0.0048);
 	    	lhkcost.addTugTariff(18001, 26000, 1464.0, 0.0048);
 	    	lhkcost.addTugTariff(26001, 40000, 1560.0, 0.0024);
 	    	lhkcost.addTugTariff(40001, 75000, 1560.0, 0.0024);
 	    	lhkcost.addTugTariff(75001, 9999999, 2040.0, 0.0024);
 	    	new Port("Lhokseumawe", lhkcost).save();
 
 	    	dumcost.addTugTariff(1, 3500, 450.0, 0.0048);
 	    	dumcost.addTugTariff(3501, 8000, 684.0, 0.0048);
 	    	dumcost.addTugTariff(8001, 14000, 924.0, 0.0048);
 	    	dumcost.addTugTariff(14001, 18000, 1464.0, 0.0048);
 	    	dumcost.addTugTariff(18001, 26000, 1464.0, 0.0048);
 	    	dumcost.addTugTariff(26001, 40000, 1560.0, 0.0024);
 	    	dumcost.addTugTariff(40001, 75000, 1560.0, 0.0024);
 	    	dumcost.addTugTariff(75001, 9999999, 2040.0, 0.0024);
 	    	new Port("Dumai", dumcost).save();
 
 	    	tjpcost.addTugTariff(1, 3500, 0.0, 0.0);
 	    	tjpcost.addTugTariff(3501, 8000, 0.0, 0.0);
 	    	tjpcost.addTugTariff(8001, 14000, 0.0, 0.0);
 	    	tjpcost.addTugTariff(14001, 18000, 0.0, 0.0);
 	    	tjpcost.addTugTariff(18001, 26000, 0.0, 0.0);
 	    	tjpcost.addTugTariff(26001, 40000, 0.0, 0.0);
 	    	tjpcost.addTugTariff(40001, 75000, 0.0, 0.0);
 	    	tjpcost.addTugTariff(75001, 9999999, 0.0, 0.0);
 	    	new Port("Tanjung Pinang", tjpcost).save();
 
 	    	morocost.addTugTariff(1, 3500, 0.0, 0.0);
 	    	morocost.addTugTariff(3501, 8000, 0.0, 0.0);
 	    	morocost.addTugTariff(8001, 14000, 0.0, 0.0);
 	    	morocost.addTugTariff(14001, 18000, 0.0, 0.0);
 	    	morocost.addTugTariff(18001, 26000, 0.0, 0.0);
 	    	morocost.addTugTariff(26001, 40000, 0.0, 0.0);
 	    	morocost.addTugTariff(40001, 75000, 0.0, 0.0);
 	    	morocost.addTugTariff(75001, 9999999, 0.0, 0.0);
 	    	new Port("Moro", morocost).save();
 
 	    	dabocost.addTugTariff(1, 3500, 0.0, 0.0);
 	    	dabocost.addTugTariff(3501, 8000, 0.0, 0.0);
 	    	dabocost.addTugTariff(8001, 14000, 0.0, 0.0);
 	    	dabocost.addTugTariff(14001, 18000, 0.0, 0.0);
 	    	dabocost.addTugTariff(18001, 26000, 0.0, 0.0);
 	    	dabocost.addTugTariff(26001, 40000, 0.0, 0.0);
 	    	dabocost.addTugTariff(40001, 75000, 0.0, 0.0);
 	    	dabocost.addTugTariff(75001, 9999999, 0.0, 0.0);
 	    	new Port("Dabo", dabocost).save();
 
 	    	kdwgcost.addTugTariff(1, 3500, 0.0, 0.0);
 	    	kdwgcost.addTugTariff(3501, 8000, 0.0, 0.0);
 	    	kdwgcost.addTugTariff(8001, 14000, 0.0, 0.0);
 	    	kdwgcost.addTugTariff(14001, 18000, 0.0, 0.0);
 	    	kdwgcost.addTugTariff(18001, 26000, 0.0, 0.0);
 	    	kdwgcost.addTugTariff(26001, 40000, 0.0, 0.0);
 	    	kdwgcost.addTugTariff(40001, 75000, 0.0, 0.0);
 	    	kdwgcost.addTugTariff(75001, 9999999, 0.0, 0.0);
 	    	new Port("Kendawangan", kdwgcost).save();
 
 	    	kjngcost.addTugTariff(1, 3500, 174.0, 0.0048);
 	    	kjngcost.addTugTariff(3501, 8000, 450.0, 0.0048);
 	    	kjngcost.addTugTariff(8001, 14000, 684.0, 0.0048);
 	    	kjngcost.addTugTariff(14001, 18000, 924.0, 0.0048);
 	    	kjngcost.addTugTariff(18001, 26000, 1464.0, 0.0048);
 	    	kjngcost.addTugTariff(26001, 40000, 1560.0, 0.0024);
 	    	kjngcost.addTugTariff(40001, 75000, 1560.0, 0.0024);
 	    	kjngcost.addTugTariff(75001, 9999999, 2040.0, 0.0024);
 	    	new Port("Kijang", kjngcost).save();
 
 	    	lbacost.addTugTariff(1, 3500, 0.0, 0.0);
 	    	lbacost.addTugTariff(3501, 8000, 0.0, 0.0);
 	    	lbacost.addTugTariff(8001, 14000, 0.0, 0.0);
 	    	lbacost.addTugTariff(14001, 18000, 0.0, 0.0);
 	    	lbacost.addTugTariff(18001, 26000, 0.0, 0.0);
 	    	lbacost.addTugTariff(26001, 40000, 0.0, 0.0);
 	    	lbacost.addTugTariff(40001, 75000, 0.0, 0.0);
 	    	lbacost.addTugTariff(75001, 9999999, 0.0, 0.0);
 	    	new Port("Labai", lbacost).save();
 
 	    	msacost.addTugTariff(1, 3500, 0.0, 0.0);
 	    	msacost.addTugTariff(3501, 8000, 0.0, 0.0);
 	    	msacost.addTugTariff(8001, 14000, 0.0, 0.0);
 	    	msacost.addTugTariff(14001, 18000, 0.0, 0.0);
 	    	msacost.addTugTariff(18001, 26000, 0.0, 0.0);
 	    	msacost.addTugTariff(26001, 40000, 0.0, 0.0);
 	    	msacost.addTugTariff(40001, 75000, 0.0, 0.0);
 	    	msacost.addTugTariff(75001, 9999999, 0.0, 0.0);
 	    	new Port("Muara Satui", msacost).save();
 
 	    	pjgcost.addTugTariff(1, 3500, 181.25, 0.0072);
 	    	pjgcost.addTugTariff(3501, 8000, 468.75, 0.0072);
 	    	pjgcost.addTugTariff(8001, 14000, 712.5, 0.0072);
 	    	pjgcost.addTugTariff(14001, 18000, 1525.0, 0.0072);
 	    	pjgcost.addTugTariff(18001, 26000, 1525.0, 0.0072);
 	    	pjgcost.addTugTariff(26001, 40000, 1525.0, 0.0072);
 	    	pjgcost.addTugTariff(40001, 75000, 1525.0, 0.0072);
 	    	pjgcost.addTugTariff(75001, 9999999, 1525.0, 0.0072);
 	    	new Port("Panjang", pjgcost).save();
 
 	    	cgdcost.addTugTariff(1, 3500, 181.25, 0.0072);
 	    	cgdcost.addTugTariff(3501, 8000, 468.75, 0.0072);
 	    	cgdcost.addTugTariff(8001, 14000, 712.5, 0.0072);
 	    	cgdcost.addTugTariff(14001, 18000, 1525.0, 0.0072);
 	    	cgdcost.addTugTariff(18001, 26000, 1525.0, 0.0072);
 	    	cgdcost.addTugTariff(26001, 40000, 1525.0, 0.0072);
 	    	cgdcost.addTugTariff(40001, 75000, 1525.0, 0.0072);
 	    	cgdcost.addTugTariff(75001, 9999999, 1525.0, 0.0072);
 	    	new Port("Cigading", cgdcost).save();
 	    	
 	    	Port jkt = Port.find("byName", "Jakarta").first();
 	    	
 	    	new Agent("OGS", "Jakarta", "agency@cosco-ogs.com").save();
 	    	Agent agent = Agent.find("byEmail", "agency@cosco-ogs.com").first();
 	    	new Agent("OGSTPK", "Jakarta", "ogstpk@cosco-ogs.com").save();
 	    	Agent subAgent = Agent.find("byEmail", "ogstpk@cosco-ogs.com").first();
 	    	new Customer("GPI", "Jakarta", "test@gpi-g.com").save();
 	    	Customer customer = Customer.find("byName", "GPI").first();
 	    	
 	    	new Operational(vsl, "249 N", jkt, customer, agent, "General", subAgent).save();
 	    	Operational agc001 = Operational.find("byVoyage", "249 N").first();
 	    	
 	    	DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
 	    	Date eta = df.parse("05/12/2010");
 	    	Date etd = df.parse("08/12/2010");
 			
 	    	agc001.oBooking(eta, etd, 2, 2.0, 2.0, "Bauxite", 20000);
 	    	agc001.booking.addAdditional("Entertain", 20000.0);
 	    	agc001.booking.addAdditional("Other", 20000.0);
 	    	agc001.booking.addAdditional("What else", 20000.0);
 	    	agc001.save();
 	    	
 	    	Date ata = df.parse("14/11/2010");
 	    	etd = df.parse("21/11/2010");
 	    	
 	    	agc001.oBerthing(ata, etd, 2, 2.0, "Bauxite", 20000);
 	    	agc001.booking.addAdditional("Entertain", 20000.0);
 	    	agc001.booking.addAdditional("Other", 20000.0);
 	    	agc001.booking.addAdditional("What else", 20000.0);
 	    	agc001.save();
 	    	
 	    	Date atd = df.parse("21/11/2010");
 	    	
 	    	agc001.oDeparture(atd, 2, 2.0, "Bauxite", 25000);
 	    	agc001.booking.addAdditional("Entertain", 20000.0);
 	    	agc001.booking.addAdditional("Other", 20000.0);
 	    	agc001.booking.addAdditional("What else", 20000.0);
 	    	agc001.save();
 //	    	agc001.oFinalCharge(2.0, 2.0, "Bauxite", 21000);
 //	    	agc001.save();
 		}
 	}
 
 }
