 package au.com.regimo.core.service;
 
 import java.util.List;
 
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Expression;
 import javax.persistence.criteria.Join;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeanUtils;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.jpa.domain.Specification;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 
 import au.com.regimo.core.domain.IdEntity;
 import au.com.regimo.core.form.DataTablesSearchCriteria;
 import au.com.regimo.core.repository.GenericRepository;
 import au.com.regimo.core.utils.ReflectionUtils;
 
 import com.google.common.base.CaseFormat;
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 
 @Transactional(readOnly = true)
 public abstract class GenericService<R extends GenericRepository<T>, T extends IdEntity> {
 
 	protected final Logger logger = LoggerFactory.getLogger(getClass());
 
 	protected final Class<T> entityClass;
 
 	protected final String entityName;
 
 	protected final R repository;
 
 	public GenericService(R repository) {
 		this.repository = repository;
         entityClass = ReflectionUtils.getSuperClassGenricType(getClass(), 1);
         entityName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,
         		entityClass.getSimpleName());
     }
 
 	public Iterable<T> findAll(Sort sort){
 		return repository.findAll(sort);
 	}
 
 	public Page<T> findAll(Pageable pageable){
 		return repository.findAll(pageable);
 	}
 
 	public T findOne(Specification<T> spec){
 		return repository.findOne(spec);
 	}
 
 	public List<T> findAll(Specification<T> spec){
 		return repository.findAll(spec);
 	}
 
 	public Page<T> findAll(Specification<T> spec, Pageable pageable){
 		return repository.findAll(spec, pageable);
 	}
 
 	public List<T> findAll(Specification<T> spec, Sort sort){
 		return repository.findAll(spec, sort);
 	}
 
 	public Page<T> searchFullText(DataTablesSearchCriteria searchCriteria){
 		return repository.findAll(fullTextSearchSpec(
 				searchCriteria.getSearchableFields(), searchCriteria.getsSearch()), searchCriteria);
 	}
 
 	public Page<T> searchFullText(DataTablesSearchCriteria searchCriteria, ModelMap modelMap){
 		return searchFullText(searchCriteria, modelMap, null);
 	}
 
 	public Page<T> searchFullText(DataTablesSearchCriteria searchCriteria, ModelMap modelMap,
 			Function<T, ?> transformer){
 		Page<T> results =	 searchFullText(searchCriteria);
 		modelMap.addAttribute("aaData", transformer!=null ? 
 				Collections2.transform(results.getContent(), transformer) : results.getContent());
 		modelMap.addAttribute("sEcho", searchCriteria.getsEcho());
 		modelMap.addAttribute("iTotalRecords", results.getTotalElements());
 		modelMap.addAttribute("iTotalDisplayRecords", results.getTotalElements());
 		return results;
 	}
 
 	public long count(Specification<T> spec){
 		return repository.count(spec);
 	}
 
 	public T getNewEntity() {
 		T object = null;
         try {
         	object = entityClass.newInstance();
 		} catch (Exception e) {
 			logger.error("Error while create object of class "+entityClass.getSimpleName()+", exception:"+e);
 		}
 		return object;
     }
 
 	@Transactional
 	public T save(T entity){
 		return repository.save(entity);
 	}
 
 	public T findOne(Long id) {
 		return repository.findOne(id);
 	}
 
 	public void loadModel(ModelMap modelMap){
 		modelMap.addAttribute(entityName, getNewEntity());
 	}
 
 	public void loadModel(ModelMap modelMap, T entity){
 		modelMap.addAttribute(entityName, entity);
 	}
 
 	public void loadModel(ModelMap modelMap, Long id) {
 		loadModel(modelMap, findOne(id));
 	}
 
 	public boolean saveModel(ModelMap modelMap, T model, BindingResult result){
 		if (!result.hasErrors()){
 			String[] ignoreProperties = getIgnoreProperties();
			if(ignoreProperties!=null && model.getId()!=null){
 				T entity = this.findOne(model.getId());
 				BeanUtils.copyProperties(model, entity, ignoreProperties);
 				model = entity;
 			}
 			model = save(model);
 		}
 		loadModel(modelMap, model);
 		return !result.hasErrors();
 	}
 
 	protected String[] getIgnoreProperties(){
 		return null;
 	}
 
 	public boolean exists(Long id){
 		return repository.exists(id);
 	}
 
 	public Iterable<T> findAll(){
 		return repository.findAll();
 	}
 
 	public List<?> findAll(Function<T, ?> transformer){
 		List<T> entities = Lists.newArrayList(findAll());
 		return transformer!=null ? Lists.transform(entities, transformer) : entities;
 	}
 
 	public long count(){
 		return repository.count();
 	}
 
 	@Transactional
 	public void delete(Long id){
 		repository.delete(id);
 	}
 
 	@Transactional
 	public void delete(T entity){
 		repository.delete(entity);
 	}
 
 	protected Specification<T> fullTextSearchSpec(final List<String> seachableFields, final String value) {
 		return new Specification<T>() {
 			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> cq,
 					CriteriaBuilder cb) {
 				Predicate predicate = cb.disjunction();
 				for(String field : seachableFields){
 					if(field.equals("id")){
 						continue;
 					}
 					predicate = cb.or(predicate, cb.like(cb.upper(
 							getQueryExpression(root,field).as(String.class)),
 							"%"+value.toUpperCase()+"%"));
 				}
 				return predicate;
 			}
 		};
 	}
 
 	private Expression<?> getQueryExpression(Root<?> root, String pathString){
 		Expression<?> expression = null;
 		String[] pathElements = pathString.split("\\.");
 		int pathSize = pathElements.length;
 
 		if (pathSize > 1) {
 			Join<Object, Object> path = root.join(pathElements[0]);
 			for (int i = 1; i < pathSize - 1; i++) {
 				path = path.join(pathElements[i]);
 			}
 			expression = path.get(pathElements[pathSize - 1]);
 		} else {
 			expression = root.get(pathString);
 		}
 		return expression;
 	}
 
 	public String getEntityName() {
 		return entityName;
 	}
 
 }
