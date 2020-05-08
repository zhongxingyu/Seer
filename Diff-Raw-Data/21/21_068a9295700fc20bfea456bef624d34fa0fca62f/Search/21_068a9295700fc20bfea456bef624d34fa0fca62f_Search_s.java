 package ru.xrm.app.webapp.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.html.HtmlInputText;
 
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.criterion.CriteriaQuery;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Expression;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.engine.TypedValue;
 
 import ru.xrm.app.dao.DAOFactory;
 import ru.xrm.app.dao.SectionDAO;
 import ru.xrm.app.dao.hibernate.impl.SectionDAOHibernateImpl;
 import ru.xrm.app.domain.Section;
 import ru.xrm.app.domain.Vacancy;
 import ru.xrm.app.util.DAOUtil;
 import ru.xrm.app.util.HibernateUtil;
 
 @ManagedBean(name="session")
 @SessionScoped
 public class Search implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	private static final int PERPAGE=10; 
 
 	private HtmlInputText searchString;
 	private List<SectionHolder> sectionHolders;
 	private List<Vacancy> currentSectionVacancies;
 	private Long currentSectionId=-1L;
 	private Integer totalPages=-1;
 	private Integer currentPage=-1;
 	private List<PaginatorItem> vacancyPages=new ArrayList<PaginatorItem>();
 
 	public Search(){
		init();
 	}
 	
 	private void init(){
 		sectionHolders=new ArrayList<SectionHolder>();
 		Session session=HibernateUtil.getSessionFactory().getCurrentSession();

 		session.beginTransaction();
 
		List<Section> sections = session.createQuery("from Section").list();
 		
 		for (Section s:sections){
			Query q=session.getNamedQuery("Vacancy.countByCategoryId").setParameter("id", s.getId());
			Integer vacanciesCount=((Long)q.iterate().next()).intValue();
 			
 			sectionHolders.add(new SectionHolder(s, vacanciesCount));
 		}
 
 		session.getTransaction().commit();
 	}
 
 	public HtmlInputText getSearchString() {
 		return searchString;
 	}
 
 	public void setSearchString(HtmlInputText searchString) {
 		this.searchString = searchString;
 	}
 
 	public List<SectionHolder> getSectionHolders() {
 		return sectionHolders;
 	}
 
 	public void setSectionHolders(List<SectionHolder> sectionHolders) {
 		this.sectionHolders = sectionHolders;
 	}
 
 	public List<Vacancy> getCurrentSectionVacancies() {
 		return currentSectionVacancies;
 	}
 
 	public void setCurrentSectionVacancies(List<Vacancy> currentSectionVacancies) {
 		this.currentSectionVacancies = currentSectionVacancies;
 	}
 
 	public Long getCurrentSectionId() {
 		return currentSectionId;
 	}
 
 	public void setCurrentSectionId(Long currentSectionId) {
 		this.currentSectionId = currentSectionId;
 	}
 
 	public List<PaginatorItem> getVacancyPages() {
 		return vacancyPages;
 	}
 
 	public void setVacancyPages(List<PaginatorItem> vacancyPages) {
 		this.vacancyPages = vacancyPages;
 	}
 
 	public Integer getCurrentPage() {
 		return currentPage;
 	}
 
 	public void setCurrentPage(Integer currentPage) {
 		this.currentPage = currentPage;
 	}
 	
 	// misc
 
 	public boolean ifNotCurrentPage(Integer pageNum){
 		return pageNum!=currentPage;
 	}
 
 	public boolean ifCurrentPage(Integer pageNum){
 		return pageNum==currentPage;
 	}
 	
 
 	// web actions
 	public String doSearch(){
 		searchString.setValue("processed");
 		return ""; // stay on same page
 	}
 	
 	public String doShowVacancies(Long sectionId, Integer page){
 		Session session=HibernateUtil.getSessionFactory().getCurrentSession();
 		Query q;
 
 		session.beginTransaction();
 
 		// this is new section, get count of vacancies in this section and calculate pages
 
 		if (currentSectionId!=sectionId){
 			q=session.createQuery("Vacancy.countByCategoryId");
 			q.setParameter("id", sectionId);
 			Integer totalVacanvies=((Long)q.iterate().next()).intValue();
 
 			if (totalVacanvies % PERPAGE == 0){
 				totalPages=totalVacanvies/PERPAGE;
 			}else{
 				totalPages=totalVacanvies/PERPAGE+1;
 			}
 			currentSectionId=sectionId;
 		}
 
 		currentPage=page;
 		
 		vacancyPages.clear();
 		int left=Math.max(0, currentPage-5);
 		int right=Math.min(currentPage+5, totalPages);
 		if (right-left<10){
 				right=Math.min(right+(10-right+left), totalPages);
 				left=Math.max(0, left-(10-right+left));
 		}
 
 		for (int i=left;i<right;i++){
 			vacancyPages.add(new PaginatorItem(sectionId,i));
 		}
 
 		// get data with paginationg
 		q=session.getNamedQuery("Vacancy.findByCategoryId");
 		q.setParameter("id", sectionId);
 		q.setFirstResult(PERPAGE*page);
 		q.setMaxResults(PERPAGE);
 		currentSectionVacancies = q.list();
 
 		session.getTransaction().commit();
 
 		return "";
 	}
 
 }
