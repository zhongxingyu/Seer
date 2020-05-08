 package ee.itcollege.borderproject.common;
 
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 
 import javax.validation.Valid;
 
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 
 public abstract class CrudController<T extends BaseEntity> {
 		
 	private Class<T> entityClass;
 	
 	private String addingView;
 	private String listingView;
 	private String updatingView;
 	private String listingAttribute;
 	
 	protected abstract void save(T entity);
 	protected abstract void delete(Integer entityId);
 	protected abstract List<T> getAll();
 	protected abstract T find(Integer entityId);
 	protected abstract void update(T entity);
 	
 	protected static final String ADD = "/add";
 	protected static final String VIEW = "/view";
 	protected static final String UPDATE = "/update";
 	protected static final String DELETE = "/delete";
 	protected static final String ID = "id";
	protected static final String ERRORS = "errors";
 	
 	public CrudController() {
 		entityClass = figureOutPersistentClass();
 	}
 	
 	public void setAddingView(String addingView) {
 		this.addingView = addingView;
 	}
 	
 	public void setListingView(String listingView) {
 		this.listingView = listingView;
 	}
 	
 	public void setUpdatingView(String updatingView) {
 		this.updatingView = updatingView;
 	}
 	
 	public void setListingAttribute(String listingAttribute) {
 		this.listingAttribute = listingAttribute;
 	}
 
 	@RequestMapping(value = VIEW, method = RequestMethod.GET)
 	public String view(Model model) {
 		model.addAttribute(listingAttribute, getAll());
 		System.out.println(getAll().size());
 		return listingView;
 	}
 
 	@RequestMapping(value = ADD, method = RequestMethod.GET)
 	public String add(Model model) {
 		model.addAttribute(getNewEntity());
 		return addingView;
 	}
 
 	@RequestMapping(value = ADD, method = RequestMethod.POST)
 	public String addReceive(Model model,
 			@ModelAttribute @Valid T entity, BindingResult result) {		
 
		if (result.hasErrors()) {
			model.addAttribute( ERRORS, true );
 			return addingView;
		}
 		
 		save(entity);
 		return getRedirectionToListing();
 	}
 	
 	@RequestMapping(value = UPDATE, method = RequestMethod.GET)
 	public String update(Model model,
 			@RequestParam(required=true, value = ID) Integer entityId) {		
 		
 		T entity = find(entityId);
 		
 		if (entity == null || entity.getRemoved() != null) 
 			return getRedirectionToListing();
 
 		model.addAttribute(entity);
 		return updatingView;
 	}
 	
 	@RequestMapping(value = UPDATE, method = RequestMethod.POST)
 	public String receiveUpdate(Model model,
 			@ModelAttribute @Valid T entity, BindingResult result) {				
 
		if(result.hasErrors()) {
			model.addAttribute( ERRORS, true );
 			return updatingView;
		}
 		
 		update(entity);		
 		return getRedirectionToListing();
 	}
 	
 	@RequestMapping(value = DELETE, method = RequestMethod.GET)
 	public String delete(Model model,
 			@RequestParam(required=true, value = ID) Integer entityId) {		
 		
 		delete(entityId);	
 		return getRedirectionToListing();
 	}
 	
 	private T getNewEntity() {
         try {
 			return entityClass.getConstructor().newInstance();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private String getRedirectionToListing() {
 		return "redirect:view";
 	}
 	
 	private Class<T> figureOutPersistentClass() {
         @SuppressWarnings("unchecked")
 		Class<T> clazz = (Class<T>)((ParameterizedType) 
         		(getClass().getGenericSuperclass())).getActualTypeArguments()[0];
         return clazz;
 	}
 		
 }
