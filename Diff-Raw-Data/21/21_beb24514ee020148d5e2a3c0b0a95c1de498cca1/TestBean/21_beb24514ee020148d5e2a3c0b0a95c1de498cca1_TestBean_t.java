 package test;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import util.MailSender;
 import businessLayer.Agreement;
 import businessLayer.ChiefScientist;
 import businessLayer.Company;
 import businessLayer.Contract;
 import businessLayer.Department;
 import businessLayer.Funding;
 import daoLayer.ContractDaoBean;
 import daoLayer.ContractSearchService;
 import daoLayer.ChiefScientistDaoBean;
 import daoLayer.CompanyDaoBean;
 import daoLayer.DeadlineSearchService;
 import daoLayer.DepartmentDaoBean;
 
 @Named
 @SessionScoped
 public class TestBean implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	@EJB
 	private ChiefScientistDaoBean chiefSB;
 	@EJB
 	private DepartmentDaoBean UARSB;
 	@EJB
 	private ContractDaoBean agrSB;
 	@EJB
 	private CompanyDaoBean compDB;
 	@EJB
 	private ContractSearchService searchService;
 	@EJB
 	private DeadlineSearchService deadService;
 	
 	@Inject
 	private MailSender mailSender;
 	public TestBean() {
 	}
 
 	private void testDepartmentGetAll() {
 
 		Department d = new Department();
 		d.setName("Disit");
 		d.setCode("sdsdgh765");
 
 		Department d1 = new Department();
 		d1.setName("Energetica");
 
 		Department d2 = new Department();
 		d2.setName("Dsi");
 		d2.setCode("dswdasd");
 
 		UARSB.createDepartment(d);
 		UARSB.createDepartment(d1);
 		UARSB.createDepartment(d2);
 
 		List<Department> result = UARSB.getAll();
 		System.out.println(result);
 
 	}
 
 	private void testCompanyGetAll() {
 
 		Company c = new Company();
 		c.setName("Apple");
 
 		Company c1 = new Company();
 		c1.setName("Microsoft");
 
 		Company c2 = new Company();
 		c2.setName("Google");
 
 		compDB.create(c);
 		compDB.create(c1);
 		compDB.create(c2);
 
 		List<Company> result = compDB.getAll();
 		System.out.println(result);
 
 	}
 
 	private void testChiefScientistGetAll() {
 
 		ChiefScientist c = new ChiefScientist();
 		c.setName("Enrico");
 		c.setSurname("Vicario");
 
 		ChiefScientist c1 = new ChiefScientist();
 		c1.setName("Beppe");
 		c1.setSurname("Modica");
 
 		chiefSB.createChiefScientist(c);
 		chiefSB.createChiefScientist(c1);
 
 		List<ChiefScientist> result = chiefSB.getAll();
 		System.out.println(result);
 
 	}
 
 	@PostConstruct
 	public void init() {
 		
 		int chiefId = 11;
 		int companyId = 12;
 		Date lower = new Date(Calendar.getInstance().getTimeInMillis());
 		Calendar c = new GregorianCalendar();
 		c.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
 		c.add(Calendar. DAY_OF_MONTH,3);
 		Date upper = new Date(c.getTimeInMillis());
		deadService.setPageSize(1);
 		
 		System.out.println("Querying in dates: "+lower+", "+upper);
 
		deadService.init(null, null, null, null, null,Agreement.class);
 	}
 
 	public void doJob1() {
 		
 
		System.out.println(deadService.getCurrentResults());
 
 	}
 
 	public void doJob2() {
 
		deadService.next();
 	}
 
 	public void doJob3() {
 
		deadService.previous();
 	}
 	
 	
 	public void doDelta(){
 		
 		//mailSender.sendToLoggedUser("nuovo", "g");
 		
 	}
 
 }
