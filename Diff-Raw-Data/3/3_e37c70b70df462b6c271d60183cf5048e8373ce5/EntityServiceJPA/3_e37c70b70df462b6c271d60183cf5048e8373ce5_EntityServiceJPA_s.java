 package biz.thaicom.eBudgeting.services;
 
 import java.io.IOException;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import javassist.expr.NewArray;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Pageable;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import biz.thaicom.eBudgeting.exception.ObjectiveHasBudgetProposalException;
 import biz.thaicom.eBudgeting.models.bgt.AllocatedFormulaColumnValue;
 import biz.thaicom.eBudgeting.models.bgt.AllocationRecord;
 import biz.thaicom.eBudgeting.models.bgt.AllocationRecordStrategy;
 import biz.thaicom.eBudgeting.models.bgt.AllocationStandardPrice;
 import biz.thaicom.eBudgeting.models.bgt.BudgetCommonType;
 import biz.thaicom.eBudgeting.models.bgt.BudgetLevel;
 import biz.thaicom.eBudgeting.models.bgt.BudgetProposal;
 import biz.thaicom.eBudgeting.models.bgt.BudgetSignOff;
 import biz.thaicom.eBudgeting.models.bgt.BudgetType;
 import biz.thaicom.eBudgeting.models.bgt.FiscalBudgetType;
 import biz.thaicom.eBudgeting.models.bgt.FormulaColumn;
 import biz.thaicom.eBudgeting.models.bgt.FormulaStrategy;
 import biz.thaicom.eBudgeting.models.bgt.ObjectiveBudgetProposal;
 import biz.thaicom.eBudgeting.models.bgt.ObjectiveBudgetProposalTarget;
 import biz.thaicom.eBudgeting.models.bgt.ProposalStrategy;
 import biz.thaicom.eBudgeting.models.bgt.RequestColumn;
 import biz.thaicom.eBudgeting.models.bgt.ReservedBudget;
 import biz.thaicom.eBudgeting.models.hrx.Organization;
 import biz.thaicom.eBudgeting.models.hrx.Person;
 import biz.thaicom.eBudgeting.models.pln.Objective;
 import biz.thaicom.eBudgeting.models.pln.ObjectiveDetail;
 import biz.thaicom.eBudgeting.models.pln.ObjectiveName;
 import biz.thaicom.eBudgeting.models.pln.ObjectiveRelations;
 import biz.thaicom.eBudgeting.models.pln.ObjectiveTarget;
 import biz.thaicom.eBudgeting.models.pln.ObjectiveType;
 import biz.thaicom.eBudgeting.models.pln.ObjectiveTypeId;
 import biz.thaicom.eBudgeting.models.pln.TargetUnit;
 import biz.thaicom.eBudgeting.models.pln.TargetValue;
 import biz.thaicom.eBudgeting.models.pln.TargetValueAllocationRecord;
 import biz.thaicom.eBudgeting.models.webui.Breadcrumb;
 import biz.thaicom.eBudgeting.repositories.AllocationRecordRepository;
 import biz.thaicom.eBudgeting.repositories.AllocationRecordStrategyRepository;
 import biz.thaicom.eBudgeting.repositories.BudgetCommonTypeRepository;
 import biz.thaicom.eBudgeting.repositories.BudgetProposalRepository;
 import biz.thaicom.eBudgeting.repositories.BudgetSignOffRepository;
 import biz.thaicom.eBudgeting.repositories.BudgetTypeRepository;
 import biz.thaicom.eBudgeting.repositories.FiscalBudgetTypeRepository;
 import biz.thaicom.eBudgeting.repositories.FormulaColumnRepository;
 import biz.thaicom.eBudgeting.repositories.FormulaStrategyRepository;
 import biz.thaicom.eBudgeting.repositories.ObjectiveBudgetProposalRepository;
 import biz.thaicom.eBudgeting.repositories.ObjectiveDetailRepository;
 import biz.thaicom.eBudgeting.repositories.ObjectiveNameRepository;
 import biz.thaicom.eBudgeting.repositories.ObjectiveRelationsRepository;
 import biz.thaicom.eBudgeting.repositories.ObjectiveRepository;
 import biz.thaicom.eBudgeting.repositories.ObjectiveTargetRepository;
 import biz.thaicom.eBudgeting.repositories.ObjectiveTypeRepository;
 import biz.thaicom.eBudgeting.repositories.OrganizationRepository;
 import biz.thaicom.eBudgeting.repositories.ProposalStrategyRepository;
 import biz.thaicom.eBudgeting.repositories.RequestColumnRepositories;
 import biz.thaicom.eBudgeting.repositories.ReservedBudgetRepository;
 import biz.thaicom.eBudgeting.repositories.TargetUnitRepository;
 import biz.thaicom.eBudgeting.repositories.TargetValueAllocationRecordRepository;
 import biz.thaicom.eBudgeting.repositories.TargetValueRepository;
 import biz.thaicom.eBudgeting.repositories.UserRepository;
 import biz.thaicom.security.models.ThaicomUserDetail;
 import biz.thaicom.security.models.User;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 @Service
 @Transactional
 public class EntityServiceJPA implements EntityService {
 	private static final Logger logger = LoggerFactory.getLogger(EntityServiceJPA.class);
 	
 	@PersistenceContext
 	private EntityManager em;
 	
 	@Autowired
 	private ObjectiveRepository objectiveRepository;
 	
 	@Autowired
 	private ObjectiveTypeRepository objectiveTypeRepository;
 	
 	@Autowired
 	private BudgetTypeRepository budgetTypeRepository;
 	
 	@Autowired
 	private FormulaStrategyRepository formulaStrategyRepository;
 	
 	@Autowired
 	private FormulaColumnRepository formulaColumnRepository;
 	
 	@Autowired
 	private BudgetProposalRepository budgetProposalRepository;
 	
 	@Autowired
 	private RequestColumnRepositories requestColumnRepositories;
 	
 	@Autowired 
 	private ProposalStrategyRepository proposalStrategyRepository;
 	
 	@Autowired 
 	private AllocationRecordRepository allocationRecordRepository;
 	
 	@Autowired
 	private ReservedBudgetRepository reservedBudgetRepository;
 	
 	@Autowired
 	private ObjectiveTargetRepository objectiveTargetRepository;
 	
 	@Autowired
 	private TargetValueRepository targetValueRepository;
 	
 	@Autowired
 	private TargetUnitRepository targetUnitRepository;
 	
 	@Autowired
 	private TargetValueAllocationRecordRepository targetValueAllocationRecordRepository;
 	
 	@Autowired
 	private ObjectiveRelationsRepository objectiveRelationsRepository;
 	
 	@Autowired
 	private BudgetCommonTypeRepository budgetCommonTypeRepository;
 	
 	@Autowired
 	private ObjectiveBudgetProposalRepository objectiveBudgetProposalRepository; 
 	
 	@Autowired
 	private FiscalBudgetTypeRepository fiscalBudgetTypeRepository;
 	
 	@Autowired
 	private ObjectiveNameRepository objectiveNameRepository;
 	
 	@Autowired
 	private BudgetSignOffRepository budgetSignOffRepository;
 	
 	@Autowired
 	private ObjectiveDetailRepository objectiveDetailRepository;
 	
 	@Autowired
 	private UserRepository userRepository;
 	
 	@Autowired
 	private OrganizationRepository organizationRepository;
 	
 	@Autowired
 	private AllocationRecordStrategyRepository allocationRecordStrategyRepository;
 	
 	@Autowired
 	private ObjectMapper mapper;
 	
 	@Override
 	public ObjectiveType findObjectiveTypeById(Long id) {
 		ObjectiveType type = objectiveTypeRepository.findOne(id);
 		if(type.getParent() !=null) {
 			type.getParent().getName();
 		}
 		type.getChildren().size();
 		return type;
 	}
 
 	@Override
 	public Set<ObjectiveType> findChildrenObjectiveType(ObjectiveType type) {
 		ObjectiveType self = objectiveTypeRepository.findOne(type.getId());
 		return self.getChildren();
 	}
 
 	@Override
 	public ObjectiveType findParentObjectiveType(ObjectiveType type) {
 		ObjectiveType self = objectiveTypeRepository.findOne(type.getId());
 		return self.getParent();
 	}
 
 	@Override
 	public List<Objective> findObjectivesOf(ObjectiveType type) {
 		return objectiveRepository.findByTypeId(type.getId());
 	}
 
 	@Override
 	public List<Objective> findObjectiveChildren(Objective objective) {
 		return findObjectiveChildrenByObjectiveId(objective.getId());
 	}
 
 	@Override
 	public List<Objective> findAllObjectiveChildren(Integer fiscalYear, Long id) {
 		List<Objective> obj = objectiveRepository.findAllByFiscalYearAndType_id(fiscalYear, id);
 		
 		for (Objective o : obj) {
 			deepInitObjective(o);
 		}
 		
 		return obj;
 	}
 
 	private void deepInitObjective(Objective obj) {
 		if(obj == null || obj.getChildren() == null || obj.getChildren().size() == 0) {
 			return;
 		} else {
 			obj.getChildren().size();
 			for(Objective o : obj.getChildren()) {
 				deepInitObjective(o);
 			}
 		}
 	}
 
 	@Override
 	public Objective findParentObjective(Objective objective) {
 		Objective self = objectiveRepository.findOne(objective.getId());
 		
 		return self.getParent();
 	}
 
 	@Override
 	public Objective findOjectiveById(Long id) {
 		Objective objective = objectiveRepository.findOne(id);
 		if(objective != null) {
 			objective.doBasicLazyLoad();
 		}
 		
 		
 		return objective;
 	}
 
 	@Override
 	public List<Objective> findObjectiveChildrenByObjectiveId(Long id) {
 //		Objective self = objectiveRepository.findOne(id);
 //		if(self != null) {
 //			logger.debug("--id: " + self.getId());
 //			logger.debug("children.getSize() = " + self.getChildren().size());
 //			self.getChildren().size();
 //			for(Objective objective: self.getChildren()) {
 //				if(objective != null) {
 //					logger.debug(" child.id --> " + objective.getId());
 //					objective.doBasicLazyLoad();
 //				}
 //			}
 //		}
 //		return self.getChildren();
 		
 		List<Objective> objs =  objectiveRepository.findChildrenWithParentAndTypeAndBudgetType(id);
 		
 		for(Objective obj : objs){
 			obj.getTargets().size();
 			if(obj.getChildren().size() > 0) {
 				obj.setIsLeaf(false);
 			} else {
 				obj.setIsLeaf(true);
 			}
 			// assume no children!
 			obj.setChildren(null);
 		}
 		
 		return objs;
 	}
 	
 	
 
 	
 	
 	@Override
 	public List<Objective> findObjectiveChildrenByObjectiveIdLoadProposal(
 			Long id, Organization organization) {
 
 		List<Objective> objs =  objectiveRepository.findChildrenWithParentAndTypeAndBudgetType(id);
 		List<Objective> returnObjs = new ArrayList<Objective>();
 		for(Objective obj : objs){
 			obj.getTargets().size();
 			if(obj.getChildren().size() > 0) {
 				obj.setIsLeaf(false);
 			} else {
 				obj.setIsLeaf(true);
 			}
 			// assume no children!
 			obj.setChildren(null);
 			
 			obj.setFilterProposals(budgetProposalRepository.findAllByForObjectiveAndOwner(obj, organization));
 			obj.setFilterObjectiveBudgetProposals(objectiveBudgetProposalRepository.findAllByForObjective_IdAndOwner_Id(obj.getId(), organization.getId()));
 			
 			if(obj.getFilterObjectiveBudgetProposals().size() > 0 || 
 					obj.getFilterProposals().size() > 0 ) {
 				returnObjs.add(obj);
 			}
 			
 			
 		}
 		
 		return returnObjs;
 
 	}
 
 	@Override
 	public List<Objective> findObjectiveAllChildrenByObjectiveIdLoadProposal(
 			Long id) {
 
 		List<Objective> objs =  objectiveRepository.findChildrenWithParentAndTypeAndBudgetType(id);
 		List<Objective> returnObjs = new ArrayList<Objective>();
 		for(Objective obj : objs){
 			obj.getTargets().size();
 			if(obj.getChildren().size() > 0) {
 				obj.setIsLeaf(false);
 			} else {
 				obj.setIsLeaf(true);
 			}
 			// assume no children!
 			obj.setChildren(null);
 			
 			obj.getProposals().size();
 			obj.getObjectiveProposals().size();
 			
 			obj.setFilterProposals(obj.getProposals());
 			obj.setFilterObjectiveBudgetProposals(obj.getObjectiveProposals());
 			
 			if(obj.getFilterObjectiveBudgetProposals().size() > 0 || 
 					obj.getFilterProposals().size() > 0 ) {
 				returnObjs.add(obj);
 			}
 			
 			
 		}
 		
 		return returnObjs;
 
 	}
 	
 	
 	@Override
 	public List<Objective> findRootObjectiveByFiscalyear(Integer fiscalYear, Boolean eagerLoad) {
 		
 		List<Objective> list = objectiveRepository.findByParentIdAndFiscalYearAndParent_Name(null, fiscalYear, "ROOT");
 		if(eagerLoad == true) {
 			for(Objective objective: list) {
 				objective.doEagerLoad();
 			}
 		} else {
 			for(Objective objective : list) {
 				objective.doBasicLazyLoad();
 			}
 		}
 		return list;
 	}
 	
 	@Override
 	public Objective findOneRootObjectiveByFiscalyear(Integer fiscalYear) {
 		return objectiveRepository.findRootOfFiscalYear(fiscalYear);
 	}
 
 	@Override
 	public List<Objective> findAvailableObjectiveChildrenByObjectiveId(Long id) {
 		Objective o = objectiveRepository.findOne(id);
 		Set<ObjectiveType> childrenSet = o.getType().getChildren();
 		
 		logger.debug("++++++++++++++++++++++++++++" + o.getType().getId());
 		
 		
 		if(childrenSet != null && childrenSet.size() >0 ) {
 			for(ObjectiveType ot : childrenSet) {
 				logger.debug(ot.getName());
 			}
 			
 			return objectiveRepository.findAvailableChildrenOfObjectiveType(childrenSet);
 		} else {
 			return null;
 		}
 		
 	}
 
 	@Override
 	public List<Objective> findRootFiscalYear() {
 		return objectiveRepository.findRootFiscalYear();
 	}
 
 	@Override
 	public List<Integer> findObjectiveTypeRootFiscalYear() {
 		return objectiveTypeRepository.findRootFiscalYear();
 	}
 
 	@Override
 	public List<ObjectiveType> findObjectiveTypeByFiscalYearEager(
 			Integer fiscalYear, Long parentId) {
 		List<ObjectiveType>  list = objectiveTypeRepository.findByFiscalYearAndParentId(fiscalYear, parentId);
 		
 		// now we'll have to just fill 'em up
 		for(ObjectiveType type : list) {
 			deepInitObjectiveType(type);
 		}
 		
 		return list;
 		
 	}
 
 	@Override
 	public ObjectiveType findDeepObjectiveTypeById(Long id) {
 		ObjectiveType  type = (ObjectiveType) objectiveTypeRepository.findOne(id);
 		
 		// now we'll have to just fill 'em up
 			deepInitObjectiveType(type);
 		
 		return type;
 		
 	}
 
 	private void deepInitObjectiveType(ObjectiveType type) {
 		if(type == null || type.getChildren() == null || type.getChildren().size() == 0) {
 			return;
 		} else {
 			type.getChildren().size();
 			for(ObjectiveType t : type.getChildren()) {
 				deepInitObjectiveType(t);
 			}
 		}
 	}
 
 	@Override
 	public List<BudgetType> findRootBudgetType() {
 		return budgetTypeRepository.findRootBudgetType();
 	}
 	
 	@Override
 	public BudgetType findBudgetTypeById(Long id) {
 		BudgetType b = budgetTypeRepository.findOne(id);
 		if(b!=null) {
 			b.doBasicLazyLoad();
 			
 			b.getChildren().size();
 			//for each children get
 		}
 		return b;
 	}
 	
 	@Override
 	public Page<BudgetType> findBudgetTypeByLevelAndMainType(Integer fiscalYear, Integer level,
 			Long typeId, String query, Pageable pageable) {
 		String mainTypePath = "%." + typeId.toString() + ".%";
 		logger.debug(query);
 		Page<BudgetType> p = budgetTypeRepository.findAllByParentLevelAndParentPathLike(level,mainTypePath, query,pageable);
 		
 		// now we load the necceessary 
 		for(BudgetType b : p ) {
 			b.getLevel().getId();
 			if(b.getCommonType() != null) {
 				b.getCommonType().getId();
 			}
 			if(b.getUnit() != null) {
 				b.getUnit().getId();
 			}
 			b.setStrategies(formulaStrategyRepository.findOnlyNonStandardByfiscalYearAndType_id(fiscalYear, b.getId()));
 			b.setStandardStrategy(formulaStrategyRepository.findOnlyStandardByfiscalYearAndType_id(fiscalYear, b.getId()));
 			
 			
 			
 			b.setCurrentFiscalYear(fiscalYear);
 		}
 		
 		return p;
 		
 	}
 	
 	@Override
 	public List<BudgetType> findBudgetTypeByLevel(Integer fiscalYear, Integer level) {
 		List<BudgetType> p = budgetTypeRepository.findAllByParentLevel(level);
 		
 		// now we load the necceessary 
 		for(BudgetType b : p ) {
 			b.getLevel().getId();
 			if(b.getCommonType() != null) {
 				b.getCommonType().getId();
 			}
 			if(b.getUnit() != null) {
 				b.getUnit().getId();
 			}
 			
 			b.getParent().getId();
 			b.getChildren().size();
 			
 			logger.debug("children size: " + b.getChildren().size());
 			
 			b.setStrategies(formulaStrategyRepository.findOnlyNonStandardByfiscalYearAndType_id(fiscalYear, b.getId()));
 			b.setStandardStrategy(formulaStrategyRepository.findOnlyStandardByfiscalYearAndType_id(fiscalYear, b.getId()));
 			b.setCurrentFiscalYear(fiscalYear);
 		}
 		
 		return p;
 		
 	}
 	
 	@Override
 	public BudgetType saveBudgetType(JsonNode node) {
 		BudgetType budgetType;
 		if(node.get("id") == null) {
 			budgetType = new BudgetType();
 			Long parentTypeId = node.get("parent").get("id").asLong();
 			
 			BudgetType parent = budgetTypeRepository.findOne(parentTypeId);
 			if(parent != null) {
 				logger.debug("parentId: " + parent.getId());
 				budgetType.setParent(parent);
 				budgetType.setParentPath("." + parent.getId() + parent.getParentPath());
 				budgetType.setParentLevel(node.get("parentLevel").asInt());
 			}
 			
 			BudgetLevel level = budgetTypeRepository.findBudgetLevelNumber(budgetType.getParentLevel());
 			budgetType.setLevel(level);
 			
 			Integer prevLineNumber = null;
 			if(parent.getChildren().size() > 0) {
 				BudgetType lastIndexType = parent.getChildren().get(parent.getChildren().size()-1);
 				budgetType.setIndex(lastIndexType.getIndex()+1);
 				
 				prevLineNumber = lastIndexType.getLineNumber();
 				
 			} else {
 				budgetType.setIndex(0);
 				
 				prevLineNumber = parent.getLineNumber();
 			}
 			
 			budgetType.setLineNumber(prevLineNumber+1);
 			
 			// now the code
 			logger.debug("level " + level.getId());
 			Integer maxCode = budgetTypeRepository.findMaxCodeAtLevel(level);
 			logger.debug("maxCode" +maxCode);
 			budgetType.setCode(maxCode+1);
 			
 			
 			// and the last piece will be lineNumber 
 			budgetTypeRepository.incrementLineNumber(prevLineNumber);
 		} else {
 			budgetType = budgetTypeRepository.findOne(node.get("id").asLong());
 		}
 		
 		
 		budgetType.setName(node.get("name").asText());
 		budgetType.setParentLevel(node.get("parentLevel").asInt());
 		
 		
 		
 		
 		// then the commontype
 		if(getJsonNodeId(node.get("commonType"))!=null) {
 			BudgetCommonType bct = budgetCommonTypeRepository.findOne(getJsonNodeId(node.get("commonType")));
 			budgetType.setCommonType(bct);
 		}
 		
 		// lastly unit
 		if(getJsonNodeId(node.get("unit"))!=null) {
 			TargetUnit unit = targetUnitRepository.findOne(getJsonNodeId(node.get("unit")));
 			budgetType.setUnit(unit);
 		}
 		
 		budgetTypeRepository.save(budgetType);
 		
 		return budgetType;
 	}
 
 	@Override
 	public BudgetType updateBudgetType(JsonNode node) {
 		BudgetType type = budgetTypeRepository.findOne(node.get("id").asLong());
 		if(type != null) {
 			type.setName(node.get("name").asText());
 			
 			budgetTypeRepository.save(type);
 		}
 		
 		return type;
 	}
 
 	@Override
 	public void deleteBudgetType(Long id) {
 		BudgetType type = budgetTypeRepository.findOne(id);
 		if(type == null) {
 			return ;
 		}
 		
 		budgetTypeRepository.delete(type);
 		
 	}
 
 	@Override
 	public BudgetType findBudgetTypeEagerLoadById(Long id, Boolean isLoadParent) {
 		BudgetType b = findBudgetTypeById(id);
 		b.doEagerLoad();
 		
 		if(isLoadParent) {
 			b.doLoadParent();
 		}
 		
 		return b;
 	}
 
 	@Override
 	public List<BudgetType> findAllMainBudgetTypeByFiscalYear(Integer fiscalYear) {
 		return fiscalBudgetTypeRepository.findAllMainBudgetTypeByFiscalYear(fiscalYear);
 	}
 
 	
 	@Override
 	public List<Integer> findFiscalYearBudgetType() {
 		List<Integer> fiscalYears = budgetTypeRepository.findFiscalYears();
 		
 		return fiscalYears;
 	}
 	
 	
 
 	@Override
 	public void initFiscalBudgetType(Integer fiscalYear) {
 		Iterable<BudgetType> types = budgetTypeRepository.findAll();
 		
 		for(BudgetType type : types) {
 			// check first if we already have this one
 			FiscalBudgetType fbt = fiscalBudgetTypeRepository.findOneByBudgetTypeAndFiscalYear(type, fiscalYear); 
 			logger.debug("fbt: " + fbt);
 			if(fbt == null) {
 				// we have to add this one
 				FiscalBudgetType newFbt = new FiscalBudgetType();
 				newFbt.setFiscalYear(fiscalYear);
 				newFbt.setBudgetType(type);
 				
 				// set the main type to be the same as last year!?
 				FiscalBudgetType lastYearFbt = fiscalBudgetTypeRepository.findOneByBudgetTypeAndFiscalYear(type, fiscalYear-1);
 				if(lastYearFbt == null) {
 					newFbt.setIsMainType(false);
 				} else {
 					newFbt.setIsMainType(lastYearFbt.getIsMainType());
 				}
 				
 				//now save!?
 				fiscalBudgetTypeRepository.save(newFbt);
 			}
 		}
 		
 	}
 	
 	@Override
 	@Transactional (propagation = Propagation.REQUIRED, readOnly = true)
 	public List<Breadcrumb> createBreadCrumbBudgetType(String prefix,
 			Integer fiscalYear, BudgetType budgetType) {
 		if(budgetType == null) {
 			return null;
 		}
 		
 		BudgetType current = budgetTypeRepository.findOne(budgetType.getId());
 		
 		Stack<Breadcrumb> stack = new Stack<Breadcrumb>();
 		
 		while(current != null) {
 			Breadcrumb b = new Breadcrumb();
 			if(current.getParent() == null) {
 				// this is the root!
 				b.setUrl(prefix + "/" + fiscalYear + "/" + current.getId() + "/");
 				b.setValue("ปี " + fiscalYear);
 				stack.push(b);
 				
 				b = new Breadcrumb();
 				b.setUrl(prefix+ "/" );
 				b.setValue("ROOT");
 				stack.push(b);
 				
 			} else {
 			
 				
 				b.setUrl(prefix + "/" + + fiscalYear + "/" + current.getId() + "/");
 				b.setValue(current.getName());
 				stack.push(b);
 			}
 			
 			current = current.getParent();
 		}
 		
 		List<Breadcrumb> list = new ArrayList<Breadcrumb>();
 		
 		while (stack.size() > 0) {
 			list.add(stack.pop());
 		}
 		
 		return list;
 	}
 
 	@Override
 	public List<FormulaStrategy> findFormulaStrategyByfiscalYearAndTypeId(
 			Integer fiscalYear, Long budgetTypeId) {
 		List<FormulaStrategy> list = formulaStrategyRepository.findByfiscalYearAndType_id(fiscalYear, budgetTypeId);
 		for(FormulaStrategy strategy : list) {
 			strategy.getFormulaColumns().size();
 			logger.debug("-----" + strategy.getType().getName());
 			if(strategy.getType().getParent()!=null) {
 				strategy.getType().getParent().getName();
 			}
 			//logger.debug("-----" + strategy.getType().getParent().getName());
 		}
 		
 		return list;
 	}
 	
 	@Override
 	public List<FormulaStrategy> findAllFormulaStrategyByfiscalYearAndBudgetType_ParentPathLike(
 			Integer fiscalYear, String parentPath) {
 		logger.debug("parentPath: {} ", parentPath);
 		List<FormulaStrategy> list = formulaStrategyRepository.findAllByfiscalYearAndType_ParentPathLike(fiscalYear, parentPath);
 		for(FormulaStrategy strategy : list) {
 			strategy.getFormulaColumns().size();
 		}
 		
 		return list;
 	}
 	
 	@Override
 	public List<FormulaStrategy> findAllFormulaStrategyByfiscalYearAndIsStandardItemAndBudgetType_ParentPathLike(
 			Integer fiscalYear, Boolean isStandardItem, Long budgetTypeId, String parentPath) {
 		List<FormulaStrategy> list = formulaStrategyRepository.findAllByfiscalYearAndIsStandardItemAndType_ParentPathLike(fiscalYear, isStandardItem, budgetTypeId, parentPath);
 		for(FormulaStrategy strategy : list) {
 			strategy.getFormulaColumns().size();
 			strategy.getType().getParent().getName();
 			if(strategy.getCommonType() !=null) {
 				strategy.getCommonType().getName();
 			}
 			if(strategy.getUnit() !=null) {
 				strategy.getUnit().getName();
 			}
 		}
 		
 		return list;
 	}
 
 
 	@Override
 	public FormulaStrategy saveFormulaStrategy(JsonNode strategy) {
 		FormulaStrategy fs;
 		if(strategy.get("id") == null) {
 			fs=new FormulaStrategy();
 		} else {
 			fs = formulaStrategyRepository.findOne(strategy.get("id").asLong());
 			
 		}
 		
 		fs.setName(strategy.get("name").asText());
 		fs.setFiscalYear(strategy.get("fiscalYear").asInt());
 		fs.setIsStandardItem(strategy.get("isStandardItem").asBoolean());
 		
 		if(strategy.get("standardPrice") == null) {
 			fs.setStandardPrice(0);
 		} else {
 			
 			try {
 				fs.setStandardPrice(strategy.get("standardPrice").asInt());
 			} catch (NumberFormatException e) {
 				fs.setStandardPrice(0);
 			}
 		}
 		
 		if(strategy.get("isStandardItem") != null) {
 			fs.setIsStandardItem(strategy.get("isStandardItem").asBoolean());
 		} else {
 			fs.setIsStandardItem(false);
 		}
 		
 		// now get the budgetType
 		if(strategy.get("type") != null) {
 			Long btId = strategy.get("type").get("id").asLong();
 			BudgetType budgetType = budgetTypeRepository.findOne(btId);
 			fs.setType(budgetType);
 		}
 		
 		fs.getType().getParent().getChildren().size();
 		
 		
 		// now save the commonType
 		if(strategy.get("commonType") != null && strategy.get("commonType").get("id") != null) {
 			
 			Long cid = strategy.get("commonType").get("id").asLong();
 			BudgetCommonType bct = budgetCommonTypeRepository.findOne(cid);
 			fs.setCommonType(bct);
 		}
 		
 		if(strategy.get("unit") != null && strategy.get("unit").get("id") != null) {
 			Long unitId = strategy.get("unit").get("id").asLong();
 			TargetUnit unit = targetUnitRepository.findOne(unitId);
 			fs.setUnit(unit);
 		}
 		
 		List<FormulaColumn> newFcList = new ArrayList<FormulaColumn>();
 		List<FormulaColumn> oldFcList = fs.getFormulaColumns();
 		// check if formulaColumn is exists!
 		for(JsonNode fcNode : strategy.get("formulaColumns")) {
 			if(fcNode.get("id") != null) {
 				FormulaColumn fc = formulaColumnRepository.findOne(fcNode.get("id").asLong());
 				if(fc!=null) {
 					//remove from old Fc
 					oldFcList.remove(fc);
 					
 					//update fc
 					fc.setUnitName(fcNode.get("unitName").asText());
 					fc.setIndex(fcNode.get("index").asInt());
 					if(fcNode.get("isFixed") == null) {
 						fc.setIsFixed(true);
 					} else {
 						fc.setIsFixed(fcNode.get("isFixed").asBoolean());
 					}
 					
 					newFcList.add(fc);
 					formulaColumnRepository.save(fc);
 					logger.debug("fc.unitName: " + fc.getUnitName());
 				}
 			} else {
 				FormulaColumn fc = new FormulaColumn();
 				fc.setUnitName(fcNode.get("unitName").asText());
 				fc.setIndex(fcNode.get("index").asInt());
 				if(fcNode.get("isFixed") == null) {
 					fc.setIsFixed(true);
 				} else {
 					fc.setIsFixed(fcNode.get("isFixed").asBoolean());
 				}
 				fc.setStrategy(fs);
 				
 				newFcList.add(fc);
 				formulaColumnRepository.save(fc);
 				logger.debug("fc.unitName: " + fc.getUnitName());
 			}
 		}
 		
 		fs.setFormulaColumns(newFcList);
 		
 		// we should destroy all fc in old list!
 		if(oldFcList!=null) {
 			for(FormulaColumn fc : oldFcList) {
 				formulaColumnRepository.delete(fc);
 			}
 		}
 		
 		
 		FormulaStrategy saveFs = formulaStrategyRepository.save(fs);
 		
 		saveFs.getType().setStrategies(formulaStrategyRepository.findOnlyNonStandardByfiscalYearAndType_id(fs.getFiscalYear(), fs.getType().getId()));
 		saveFs.getType().setStandardStrategy(formulaStrategyRepository.findOnlyStandardByfiscalYearAndType_id(fs.getFiscalYear(), fs.getType().getId()));
 		saveFs.getType().setCurrentFiscalYear(fs.getFiscalYear());
 		
 		logger.debug("about to return fs!");
 		
 		for(FormulaColumn fc : saveFs.getFormulaColumns()) {
 			logger.debug(fc.getUnitName());
 		}
 		
 		return saveFs;
 		
 	}
 
 	@Override
 	public FormulaStrategy updateFormulaStrategy(JsonNode strategy) {
 		return saveFormulaStrategy(strategy);
 	}
 
 	
 	@Override
 	public void deleteFormulaStrategy(Long id) {
 		// we'll have to update the rest of index !
 		// so get the one we want to delete first 
 		FormulaStrategy strategy = formulaStrategyRepository.findOne(id);
 		
 		formulaStrategyRepository.delete(id);
 		formulaStrategyRepository.reIndex(strategy.getIndex(), 
 				strategy.getFiscalYear(), strategy.getType());
 	}
 
 	@Override
 	public void deleteFormulaColumn(Long id) {
 		// get this one first
 		FormulaColumn column = formulaColumnRepository.findOne(id);
 		formulaColumnRepository.delete(id);
 		formulaColumnRepository.reIndex(column.getIndex(), column.getStrategy());
 	}
 
 	@Override
 	public FormulaColumn saveFormulaColumn(
 			FormulaColumn formulaColumn) {
 		return formulaColumnRepository.save(formulaColumn);
 	}
 
 	@Override
 	public FormulaColumn updateFormulaColumn(
 			FormulaColumn formulaColumn) {
 		// so we'll get FormulaColumn First
 		FormulaColumn columnFromJpa = formulaColumnRepository.findOne(
 				formulaColumn.getId());
 		
 		// now update this columnFromJpa
 		columnFromJpa.setColumnName(formulaColumn.getColumnName());
 		columnFromJpa.setIsFixed(formulaColumn.getIsFixed());
 		columnFromJpa.setUnitName(formulaColumn.getUnitName());
 		columnFromJpa.setValue(formulaColumn.getValue());
 		
 		
 		// and we can save now
 		formulaColumnRepository.save(columnFromJpa);
 		
 		// and happily return 
 		return columnFromJpa;
 	}
 
 	@Override
 	public List<Breadcrumb> createBreadCrumbObjective(String prefix,
 			Integer fiscalYear, Objective objective) {
 		if(objective == null) {
 			List<Breadcrumb> list = new ArrayList<Breadcrumb>();
 			Breadcrumb b = new Breadcrumb();
 			b.setUrl(prefix+ "/" );
 			b.setValue("ROOT");
 			
 			list.add(b);
 			
 			b = new Breadcrumb();
 			b.setUrl(prefix + "/" + fiscalYear + "/");
 			b.setValue("ทะเบียนปี " + fiscalYear);
 			list.add(b);
 			
 			return list;
 		}
 		
 		Objective current = objectiveRepository.findOne(objective.getId());
 		
 		Stack<Breadcrumb> stack = new Stack<Breadcrumb>();
 		
 		while(current != null) {
 			Breadcrumb b = new Breadcrumb();
 			String code = current.getCode();
 			if(code==null) {
 				code= "";
 			}
 			
 			if(current.getParent() == null || current.getType().getId() == 103) {
 				// this is the root!
 				
 						
 				b.setUrl(prefix + "/" + fiscalYear + "/");
 				b.setValue(current.getType().getName() + " "+ code + ". <br/>" + current.getName());
 				stack.push(b);
 				
 				
 				b = new Breadcrumb();
 				b.setUrl(prefix + "/" + fiscalYear + "/");
 				b.setValue("ทะเบียนปี " + fiscalYear);
 				stack.push(b);
 				
 				
 				
 				b = new Breadcrumb();
 				b.setUrl(prefix+ "/" );
 				b.setValue("ROOT");
 				stack.push(b);
 				
 				break;
 				
 			} else {
 				b.setUrl(prefix + "/" + + fiscalYear + "/" + current.getId() + "/");
 				
 				b.setValue(current.getType().getName() + " " + code + ". <br/>" + current.getName());
 				stack.push(b);
 			}
 			
 			current = current.getParent();
 		}
 		
 		List<Breadcrumb> list = new ArrayList<Breadcrumb>();
 		
 		while (stack.size() > 0) {
 			list.add(stack.pop());
 		}
 		
 		return list;
 	}
 
 	@Override
 	public Objective objectiveDoEagerLoad(Long objectiveId) {
 		Objective objective = objectiveRepository.findOne(objectiveId);
 		objective.doEagerLoad();
 		
 		return objective;
 	}
 
 	@Override
 	public Objective updateObjective(Objective objective) {
 		// now get Objective form our DB first
 		Objective objectiveFromJpa = objectiveRepository.findOne(objective.getId());
 		
 		if(objectiveFromJpa != null) {
 			// OK go through the supposed model
 			objectiveFromJpa.setName(objective.getName());
 			objectiveFromJpa.setFiscalYear(objective.getFiscalYear());
 			
 //			if(objective.getBudgetType() != null && objective.getBudgetType().getId() != null) {
 //				objectiveFromJpa.setBudgetType(objective.getBudgetType());
 //			} 
 			
 			if(objective.getParent() != null && objective.getParent().getId() != null) {
 				objectiveFromJpa.setParent(objective.getParent());
 			}
 			
 			if(objective.getType() != null && objective.getType().getId() != null) {
 				objectiveFromJpa.setType(objective.getType());
 			}
 			
 			// we don't do anything for children
 			
 			objectiveRepository.save(objectiveFromJpa);
 		}
 		
 		return objectiveFromJpa;
 		
 	}
 
 	@Override
 	public List<Objective> findChildrenObjectivewithBudgetProposal(
 			Integer fiscalYear, Long ownerId, Long objectiveId, Boolean isChildrenTraversal) {
 		logger.debug("ownerId: " + ownerId);
 		List<Objective> objectives = objectiveRepository.findByObjectiveBudgetProposal(fiscalYear, ownerId, objectiveId);
 		
 		for(Objective objective : objectives) {
 //			logger.debug("** " + objective.getBudgetType().getName());
 			objective.doEagerLoadWithBudgetProposal(isChildrenTraversal);
 		}
 		return objectives;
 	}
 
 	@Override
 	public List<Objective> findChildrenObjectivewithObjectiveBudgetProposal(
 			Integer fiscalYear, Long ownerId, Long objectiveId,
 			Boolean isChildrenTraversal) {
 		List<Objective> objectives = objectiveRepository.findByObjectiveBudgetProposal(fiscalYear, ownerId, objectiveId);
 		
 		for(Objective objective : objectives) {
 //			logger.debug("** " + objective.getBudgetType().getName());
 			objective.doEagerLoadWithBudgetProposal(isChildrenTraversal);
 		}
 		return objectives;
 	}
 	
 	
 	@Override
 	public BudgetProposal findBudgetProposalById(Long budgetProposalId) {
 		return budgetProposalRepository.findOne(budgetProposalId);
 	}
 
 	@Override
 	public String initReservedBudget(Integer fiscalYear) {
 		Objective root = objectiveRepository.findRootOfFiscalYear(fiscalYear);
 		String parentPathLikeString = "%."+root.getId()+"%";		
 		List<Objective> list = objectiveRepository.findFlatByObjectiveBudgetProposal(fiscalYear,parentPathLikeString);
 		
 		// we will copy from the last round (index = 2)
 					List<AllocationRecord> allocationRecordList = allocationRecordRepository
 							.findAllByForObjective_fiscalYearAndIndex(fiscalYear, 2);
 					
 		// go through this one
 		for(AllocationRecord record: allocationRecordList) {
 			ReservedBudget reservedBudget = reservedBudgetRepository.findOneByBudgetTypeAndObjective(record.getBudgetType(), record.getForObjective());
 			
 			if(reservedBudget == null) {
 				reservedBudget = new ReservedBudget();
 			}
 			reservedBudget.setAmountReserved(0L);
 			reservedBudget.setBudgetType(record.getBudgetType());
 			reservedBudget.setForObjective(record.getForObjective());
 
 			
 			reservedBudgetRepository.save(reservedBudget);
 		}
 		
 //		List<RequestColumn> requestColumns = requestColumnRepositories.findAllByFiscalYear(fiscalYear);
 //		for(RequestColumn rc : requestColumns) {
 //			rc.setAllocatedAmount(rc.getAmount());
 //			requestColumnRepositories.save(rc);
 //		}
 //	
 //		List<FormulaColumn> formulaColumns = formulaColumnRepository.findAllByFiscalYear(fiscalYear);
 //		for(FormulaColumn fc : formulaColumns) {
 //			fc.setAllocatedValue(fc.getValue());
 //			formulaColumnRepository.save(fc);
 //		}
 		
 
 		return "success";
 	
 
 	}
 
 	
 	@Override
 	public String initAllocationRecord(Integer fiscalYear, Integer round) {
 		Objective root = objectiveRepository.findRootOfFiscalYear(fiscalYear);
 		String parentPathLikeString = "%."+root.getId()+"%";		
 		List<Objective> list = objectiveRepository.findFlatByObjectiveBudgetProposal(fiscalYear,parentPathLikeString);
 		
 //		List<BudgetProposal> proposalList = budgetProposalRepository
 //				.findBudgetProposalByFiscalYearAndParentPath(fiscalYear, parentPathLikeString);
 		
 		if(round == 1) {
 			// we update all formulaStrategy here
 			List<FormulaStrategy> formulaList = formulaStrategyRepository.findAllByFiscalYear(fiscalYear);
 			for(FormulaStrategy fs : formulaList) {
 				AllocationStandardPrice asp;
 				if(fs.getAllocationStandardPriceMap()==null) {
 					fs.setAllocationStandardPriceMap(new ArrayList<AllocationStandardPrice>());
 				}
 				
 				if(fs.getAllocationStandardPriceMap().size() < round ){
 					asp = new AllocationStandardPrice();
 					asp.setIndex(round-1);
 					
 					fs.getAllocationStandardPriceMap().add(round-1, asp);
 				} else{
 					asp = fs.getAllocationStandardPriceMap().get(round-1);	
 				}
 				 
 				
 				asp.setStandardPrice(fs.getStandardPrice());
 				for(FormulaColumn fc: fs.getFormulaColumns()) {
 					AllocatedFormulaColumnValue afcv;
 					if(fc.getAllocatedFormulaColumnValueMap() == null) {
 						fc.setAllocatedFormulaColumnValueMap(new ArrayList<AllocatedFormulaColumnValue>());
 					} 
 					
 					if(fc.getAllocatedFormulaColumnValueMap().size() < round) {
 						afcv = new AllocatedFormulaColumnValue();
 						afcv.setIndex(round-1);
 						
 						fc.getAllocatedFormulaColumnValueMap().add(round-1, afcv);
 					} else {
 						 afcv = fc.getAllocatedFormulaColumnValueMap().get(round-1);	
 					}
 					
 					afcv.setAllocatedValue(fc.getValue());
 				}
 				formulaColumnRepository.save(fs.getFormulaColumns());
 			}
 			formulaStrategyRepository.save(formulaList);
 			
 			// here we have to remove allocationRecord and its associated 
 			List<AllocationRecord> arList = allocationRecordRepository.findAllByForObjective_fiscalYearAndIndex(fiscalYear, round-1);
 			allocationRecordRepository.delete(arList);
 			
 			
 			for(Objective o : list) {
 				HashMap<Long, AllocationRecord> budgetTypeMap = new HashMap<Long, AllocationRecord>();
 				HashMap<FormulaStrategy, AllocationRecordStrategy> formulaStrategyMap = new HashMap<FormulaStrategy, AllocationRecordStrategy>();
 				HashMap<BudgetType, AllocationRecordStrategy> allocStrgyBudgetTypeMap = new HashMap<BudgetType, AllocationRecordStrategy>();
 				HashMap<FormulaColumn, RequestColumn> columnMap = new HashMap<FormulaColumn, RequestColumn>();
 				
 				for(BudgetProposal p : o.getProposals()) {
 					AllocationRecord ar = budgetTypeMap.get(p.getBudgetType().getId());
 					if(p.getId().equals(2887L)) {
 						logger.debug("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
 					} 
 					if(ar == null) {
 						ar = new AllocationRecord();
 						ar.setIndex(0);
 						ar.setForObjective(o);
 						ar.setBudgetType(p.getBudgetType());
 						ar.setAmountAllocated(p.getAmountRequest());
 						
 						budgetTypeMap.put(p.getBudgetType().getId(), ar);
 
 					} else {						
 						ar.setAmountAllocated(ar.getAmountAllocated() + p.getAmountRequest());
 					}
 					allocationRecordRepository.save(ar);
 					
 					// now for Each ps
 					for(ProposalStrategy ps : p.getProposalStrategies()) {
 						ar.setAllocationRecordStrategies(new ArrayList<AllocationRecordStrategy>());
 						AllocationRecordStrategy ars;
 						if(ps.getFormulaStrategy() == null) {
 							ars = allocStrgyBudgetTypeMap.get(ar.getBudgetType());
 							
 						} else {
 							ars = formulaStrategyMap.get(ps.getFormulaStrategy());
 						}
 						if(ars == null) {
 							ars = new AllocationRecordStrategy();
 							ars.setAllocationRecord(ar);
 							ars.setStrategy(ps.getFormulaStrategy());
 							if(ars.getProposalStrategies() == null) {
 								ars.setProposalStrategies(new ArrayList<ProposalStrategy>());
 							}
 							
 							ars.getProposalStrategies().add(ps);
 							ars.setTotalCalculatedAmount(ps.getTotalCalculatedAmount());
 							
 							formulaStrategyMap.put(ps.getFormulaStrategy(), ars);
 							allocationRecordStrategyRepository.save(ars);
 						} else {
 							ars.setTotalCalculatedAmount(ars.getTotalCalculatedAmount() + ps.getTotalCalculatedAmount());
 							ars.getProposalStrategies().add(ps);
 						}
 						
 						for(RequestColumn rc : ps.getRequestColumns()) {
 							RequestColumn allocRc = columnMap.get(rc.getColumn());
 							if(allocRc == null) {
 								allocRc = new RequestColumn();
 								allocRc.setAllocationRecordStrategy(ars);
 								allocRc.setAmount(rc.getAmount());
 								allocRc.setColumn(rc.getColumn());
 								
 								columnMap.put(rc.getColumn(), allocRc);
 							} else {
 								allocRc.setAmount(allocRc.getAmount() + rc.getAmount());
 							}
 						}
 					
 					}
 				}
 				// now we can save all this
 				allocationRecordRepository.save(budgetTypeMap.values());
 				allocationRecordStrategyRepository.save(formulaStrategyMap.values());
 				requestColumnRepositories.save(columnMap.values());
 			}
 			
 		}
 //			//loop through proposalList
 //			for(BudgetProposal proposal : proposalList) {
 //				Integer index = list.indexOf(proposal.getForObjective());
 //				Objective o = list.get(index);
 //				o.getProposals().size();
 //				
 //				o.addToSumBudgetTypeProposals(proposal,true);
 //				
 //				logger.debug("AAding proposal {} to objective: {}", proposal.getId(), o.getId());
 //				
 //				
 //				//o.getProposals().add(proposal);
 //				logger.debug("proposal size is " + o.getProposals().size());
 //				
 //			}
 //			
 //			// then update all FormulaStrategy & FormulaColumn
 //			List<FormulaStrategy> formulaStrategies = formulaStrategyRepository.findAllByFiscalYear(fiscalYear);
 //			for(FormulaStrategy fs: formulaStrategies) {
 //				AllocationStandardPrice stdPrice = fs.getAllocationStandardPriceMap().get(round-1);
 //				if(stdPrice == null) {
 //					stdPrice = new AllocationStandardPrice();
 //					stdPrice.setIndex(round-1);
 //				} else {
 //					fs.getAllocationStandardPriceMap().remove(stdPrice);
 //				}
 //				stdPrice.setStandardPrice(fs.getStandardPrice());
 //				fs.getAllocationStandardPriceMap().add(round-1, stdPrice);
 //				
 //				// now forEach formularColumn
 //				for(FormulaColumn fc: fs.getFormulaColumns()) {
 //					AllocatedFormulaColumnValue fcValue = fc.getAllocatedFormulaColumnValueMap().get(round-1);
 //					if(fcValue == null) {
 //						fcValue = new AllocatedFormulaColumnValue();
 //						fcValue.setIndex(round-1);
 //					} else {
 //						fc.getAllocatedFormulaColumnValueMap().remove(fcValue);
 //					}
 //					fcValue.setAllocatedValue(fc.getValue());
 //					
 //					fc.getAllocatedFormulaColumnValueMap().add(round-1, fcValue);
 //					formulaColumnRepository.save(fc);
 //				}
 //				
 //				formulaStrategyRepository.save(fs);
 //			}
 //			
 //			// and then remove the allocationRecordStrategy
 //			List<AllocationRecordStrategy> strategies = allocationRecordStrategyRepository.findAllByIndexAndFiscalYear(round-1, fiscalYear);
 //			allocationRecordStrategyRepository.delete(strategies);
 //			
 //			
 //			// now loop through the Objective
 //			for(Objective o : list) {
 //				if(o.getSumBudgetTypeProposals() != null) {
 //					for(BudgetProposal b : o.getSumBudgetTypeProposals()) {
 //						// now get this on
 //						AllocationRecord allocationRecord = allocationRecordRepository.findOneByBudgetTypeAndObjectiveAndIndex(b.getBudgetType(), o ,round-1);  
 //						if(allocationRecord != null) {
 //							allocationRecord.setAmountAllocated(b.getAmountRequest());
 //						} else {
 //							allocationRecord = new AllocationRecord();
 //							allocationRecord.setAmountAllocated(b.getAmountRequest());
 //							allocationRecord.setBudgetType(b.getBudgetType());
 //							allocationRecord.setForObjective(o);
 //							allocationRecord.setIndex(round-1);
 //						}
 //						
 //						List<AllocationRecordStrategy> recordStrgies = new ArrayList<AllocationRecordStrategy>();
 //						allocationRecord.setAllocationRecordStrategies(recordStrgies);
 //						
 //						
 //						if(b.getProposalStrategies()!=null) {
 //							for(ProposalStrategy ps : b.getProposalStrategies()) {
 //								AllocationRecordStrategy strgy = new AllocationRecordStrategy();
 //								strgy.setAllocationRecord(allocationRecord);
 //								strgy.setStrategy(ps.getFormulaStrategy());
 //								strgy.setTotalCalculatedAmount(ps.getTotalCalculatedAmount());
 //								
 //								allocationRecordStrategyRepository.save(strgy);
 //											
 //								// now deal with RequestColumn
 //								for(RequestColumn rc : ps.getRequestColumns()) {
 //									RequestColumn allocRc = new RequestColumn();
 //									allocRc.setColumn(rc.getColumn());
 //									allocRc.setAmount(rc.getAmount());
 //									allocRc.setAllocationRecordStrategy(strgy);
 //									
 //									requestColumnRepositories.save(allocRc);
 //								}
 //								
 //								recordStrgies.add(strgy);
 //							}
 //						}
 //						
 //						allocationRecordRepository.save(allocationRecord);
 //						
 //					}
 //				}
 //				
 //				// now the targetValue
 //				for(ObjectiveTarget target: o.getTargets()) {
 //					List<TargetValue> targetvalues = targetValueRepository.findAllByTargetIdAndObjectiveId(target.getId(), o.getId());
 //					
 //					logger.debug("----------------------------- {} / {} ", o.getId(), target.getId());
 //					
 //					Long sum = 0L;
 //					for(TargetValue tv : targetvalues) {
 //						sum += tv.getRequestedValue();
 //					}
 //					
 //					// now we can add 
 //					TargetValueAllocationRecord tvar = targetValueAllocationRecordRepository.findOneByIndexAndForObjectiveAndTarget(round-1, o, target);
 //					if(tvar == null) {
 //						logger.debug("+++++++++++++++++++++++++++++++++++++++++ {} / {} ", o.getId(), target.getId());
 //						tvar = new TargetValueAllocationRecord();
 //						tvar.setIndex(round-1);
 //						tvar.setForObjective(o);
 //						tvar.setTarget(target);
 //					} 
 //					
 //					tvar.setAmountAllocated(sum);
 //					
 //					targetValueAllocationRecordRepository.save(tvar);
 //					
 //				}
 //				
 //			}
 //		} else {
 //			// we will copy from the previous round...
 //			List<AllocationRecord> allocationRecordList = allocationRecordRepository
 //					.findAllByForObjective_fiscalYearAndIndex(fiscalYear, round-2);
 //			
 //			// go through this one
 //			for(AllocationRecord record: allocationRecordList) {
 //				AllocationRecord dbRecord = allocationRecordRepository.findOneByBudgetTypeAndObjectiveAndIndex(record.getBudgetType(), record.getForObjective(), round-1);
 //				
 //				if(dbRecord == null) {
 //					dbRecord = new AllocationRecord();
 //				}
 //				dbRecord.setAmountAllocated(record.getAmountAllocated());
 //				dbRecord.setBudgetType(record.getBudgetType());
 //				dbRecord.setForObjective(record.getForObjective());
 //				dbRecord.setIndex(round-1);
 //				
 //				allocationRecordRepository.save(dbRecord);
 //			}
 //			
 //			List<TargetValueAllocationRecord> tvarList = targetValueAllocationRecordRepository
 //					.findAllByForObjective_FiscalYearAndIndex(fiscalYear, round-2);
 //			for(TargetValueAllocationRecord rvar : tvarList) {
 //				TargetValueAllocationRecord dbRecord = targetValueAllocationRecordRepository
 //						.findOneByTargetAndForObjectiveAndIndex(rvar.getTarget(), rvar.getForObjective(), round-1);
 //				
 //				logger.debug("objectiveid: {}", rvar.getForObjective().getId());
 //				
 //				if(dbRecord == null) {
 //					dbRecord = new TargetValueAllocationRecord();
 //					dbRecord.setIndex(round-1);
 //					dbRecord.setForObjective(rvar.getForObjective());
 //					dbRecord.setTarget(rvar.getTarget());
 //				}
 //				
 //				dbRecord.setAmountAllocated(rvar.getAmountAllocated());
 //				targetValueAllocationRecordRepository.save(dbRecord);
 //			}
 //		}
 		return "success";
 		
 	}
 
 	
 	@Override
 	public List<Objective> findFlatChildrenObjectivewithBudgetProposalAndAllocation(
 			Integer fiscalYear, Long objectiveId, Boolean isFindObjectiveBudget) {
 		String parentPathLikeString = "%."+objectiveId.toString()+"%";
 		List<Objective> list = objectiveRepository.findFlatByObjectiveBudgetProposal(fiscalYear, parentPathLikeString);
 		Objective parent = objectiveRepository.findOne(objectiveId);
 		
 		List<BudgetProposal> proposalList = budgetProposalRepository
 				.findBudgetProposalByFiscalYearAndParentPath(fiscalYear, parentPathLikeString);
 		
 		//loop through proposalList
 		for(BudgetProposal proposal : proposalList) {
 			Integer index = list.indexOf(proposal.getForObjective());
 			Objective o = list.get(index);
 			o.getProposals().size();
 			
 			o.addToSumBudgetTypeProposalsOnlyAmount(proposal);
 			
 			//logger.debug("AAding proposal {} to objective: {}", proposal.getId(), o.getId());
 			
 			//o.getProposals().add(proposal);
 			//logger.debug("proposal size is " + o.getProposals().size());
 		}
 		
 		//now loop through allocationRecord
 		List<AllocationRecord> recordList = allocationRecordRepository
 				.findBudgetProposalByFiscalYearAndOwnerAndParentPath(fiscalYear, parentPathLikeString);
 		for(AllocationRecord record : recordList) {
 			
 			record.getAllocationRecordStrategies().size();
 			
 			Integer index = list.indexOf(record.getForObjective());
 			Objective o = list.get(index);
 			//logger.debug("AAding Allocation {} to objective: {}", record.getId(), o.getId());
 			
 			if(o.getAllocationRecords()==null) {
 				o.setAllocationRecords(new ArrayList<AllocationRecord>());
 			}
 			
 			if(record.getIndex() == 0) {
 				o.getAllocationRecordsR1().add(record);
 			} else if(record.getIndex() == 1) {
 				o.getAllocationRecordsR2().add(record);
 			} else if(record.getIndex() == 2) {
 				o.getAllocationRecordsR3().add(record);
 			}
 			
 			//o.getProposals().add(record);
 			//logger.debug("proposal size is " + o.getAllocationRecords().size());
 		}
 		
 		// And lastly loop through reservedBudget
 		List<ReservedBudget> reservedBudgets = reservedBudgetRepository.findAllByFiscalYearAndParentPathLike(fiscalYear, parentPathLikeString);
 		for(ReservedBudget rb : reservedBudgets) {
 			//logger.debug("reservedBuget: {} ", rb.getForObjective().getId());
 			Integer index = list.indexOf(rb.getForObjective());
 			Objective o = list.get(index);
 			
 			o.getReservedBudgets().size();
 			
 			
 		}
 		
 		List<Objective> returnList = new ArrayList<Objective>();
 		
 		// oh not yet!
 		for(Objective o : list) {
 			if(o.getParent().getId().equals(parent.getId()) && o.getProposals().size() > 0) {
 				//logger.debug("---------------------adding {}", o.getId() );
 				returnList.add(o); 
 			} 
 			
 			if(o.getProposals().size() > 0 ) {
 			
 				if(o.getChildren().size() >0) {
 					o.setIsLeaf(false);
 				} else {
 					o.setIsLeaf(true);
 				}
 				
 				o.getTargetValueAllocationRecords().size();
 				o.getTargetValues().size();
 				for(TargetValue tv : o.getTargetValues()) {
 					tv.getOwner().getId();
 				}
 			} else {
 				
 			}
 			
 		}
 		
 		return returnList;
 	}
 	
 	
 	@Override
 	public List<Objective> findFlatChildrenObjectivewithBudgetProposal(
 			Integer fiscalYear, Long ownerId, Long objectiveId) {
 		String parentPathLikeString = "%."+objectiveId.toString()+"%";
 		List<Objective> list = objectiveRepository.findFlatByObjectiveBudgetProposal(fiscalYear, ownerId, parentPathLikeString);
 	
 		
 		
 		List<BudgetProposal> proposalList = budgetProposalRepository
 				.findBudgetProposalByFiscalYearAndOwnerAndParentPath(fiscalYear, ownerId, parentPathLikeString);
 		
 		//loop through proposalList
 		for(BudgetProposal proposal : proposalList) {
 			Integer index = list.indexOf(proposal.getForObjective());
 			Objective o = list.get(index);
 			logger.debug("AAding proposal {} to objective: {}", proposal.getId(), o.getId());
 			
 			o.addfilterProposal(proposal);
 			//logger.debug("proposal size is " + o.getProposals().size());
 			
 		}
 		
 		// get List of targetValue
 		Map<String, TargetValue> targetValueMap = new HashMap<String, TargetValue>();
 		List<TargetValue> targetValues = targetValueRepository.findAllByOnwerIdAndObjectiveParentPathLike(ownerId, parentPathLikeString);
 		for(TargetValue tv : targetValues) {
 			targetValueMap.put(tv.getForObjective().getId()+ "," + tv.getTarget().getId(), tv);
 				
 		}
 		
 		// get List of ObjectiveTarget?
 		List<ObjectiveTarget> targets = objectiveTargetRepository.findAllByObjectiveParentPathLike(parentPathLikeString);
 		
 		for(ObjectiveTarget target : targets) {
 			target.getForObjectives().size();
 			
 			for(Objective o : target.getForObjectives()) {
 				logger.debug("Adding objective target to list");
 				Integer index = list.indexOf(o);
 				Objective objInlist = list.get(index);
 				logger.debug("objInList target size = " + objInlist.getTargets().size());
 				
 				TargetValue tv = targetValueMap.get(objInlist.getId() + "," + target.getId());
 				if(tv==null) {
 					tv = new TargetValue();
 					tv.setTarget(target);
 					tv.setForObjective(objInlist);
 					
 				}
 				objInlist.addfilterTargetValue(tv);
 				
 			}
 						
 		}
 		
 		return list;
 	}
 	
 	@Override
 	public List<Objective> findFlatChildrenObjectivewithObjectiveBudgetProposal(
 			Integer fiscalYear, Long ownerId, Long objectiveId) {
 		String parentPathLikeString = "%."+objectiveId.toString()+"%";
 		List<Objective> list = objectiveRepository.findFlatByObjectiveObjectiveBudgetProposal(fiscalYear, ownerId, parentPathLikeString);
 		
 		for(Objective o : list) {
 		//get List of ObjectiveBudgetProposal
 			o.getType().getId();
 			logger.debug("finding objective budget proposal of objective code: " + o.getCode() + " ");
 			List<ObjectiveBudgetProposal> obpList = objectiveBudgetProposalRepository.findAllByForObjective_IdAndOwner_Id(o.getId(), ownerId);
 			
 			logger.debug("found " + obpList.size() + " proposals" );
 			o.setFilterObjectiveBudgetProposals(obpList);
 		}
 		
 		// get List of targetValue
 		Map<String, TargetValue> targetValueMap = new HashMap<String, TargetValue>();
 		List<TargetValue> targetValues = targetValueRepository.findAllByOnwerIdAndObjectiveParentPathLike(ownerId, parentPathLikeString);
 		for(TargetValue tv : targetValues) {
 			targetValueMap.put(tv.getForObjective().getId()+ "," + tv.getTarget().getId(), tv);
 				
 		}
 		
 		// get List of ObjectiveTarget?
 		List<ObjectiveTarget> targets = objectiveTargetRepository.findAllByObjectiveParentPathLike(parentPathLikeString);
 		
 		for(ObjectiveTarget target : targets) {
 			target.getForObjectives().size();
 			
 			for(Objective o : target.getForObjectives()) {
 				//logger.debug("Adding objective target to list");
 				Integer index = list.indexOf(o);
 				Objective objInlist = list.get(index);
 				//logger.debug("objInList target size = " + objInlist.getTargets().size());
 				
 				TargetValue tv = targetValueMap.get(objInlist.getId() + "," + target.getId());
 				if(tv==null) {
 					tv = new TargetValue();
 					tv.setTarget(target);
 					tv.setForObjective(objInlist);
 					
 				}
 				objInlist.addfilterTargetValue(tv);
 				
 			}
 						
 		}
 		
 		return list;
 	}
 
 	@Override
 	public ProposalStrategy deleteProposalStrategy(Long id) {
 		ProposalStrategy proposalStrategy = proposalStrategyRepository.findOne(id);
 		
 		Long amountToBeReduced = proposalStrategy.getTotalCalculatedAmount();
 		Long amountRequestNext1Year = proposalStrategy.getAmountRequestNext1Year();
 		Long amountRequestNext2Year = proposalStrategy.getAmountRequestNext2Year();
 		Long amountRequestNext3Year = proposalStrategy.getAmountRequestNext3Year();
 		
 		BudgetProposal b = proposalStrategy.getProposal();
 		b.addAmountRequest(-amountToBeReduced);
 		b.addAmountRequestNext1Year(-amountRequestNext1Year);
 		b.addAmountRequestNext2Year(-amountRequestNext2Year);
 		b.addAmountRequestNext3Year(-amountRequestNext3Year);
 		budgetProposalRepository.save(b);
 		
 		Organization owner = b.getOwner();
 		
 		// now walk up ward
 		BudgetProposal temp = b;
 		// OK we'll go through the amount of this one and it's parent!?
 		while (temp.getForObjective().getParent() != null) {
 			// now we'll get all proposal
 			Objective parent = temp.getForObjective().getParent();
 			temp = budgetProposalRepository.findByForObjectiveAndOwnerAndBudgetType(parent,owner,b.getBudgetType());
 			
 			if(temp!=null) {
 				temp.addAmountRequest(-amountToBeReduced);
 				temp.addAmountRequestNext1Year(-amountRequestNext1Year);
 				temp.addAmountRequestNext2Year(-amountRequestNext2Year);
 				temp.addAmountRequestNext3Year(-amountRequestNext3Year);
 				
 			} 
 			budgetProposalRepository.save(temp);
 		}
 		
 		proposalStrategyRepository.delete(proposalStrategy);
 		
 		return proposalStrategy;
 	}
 	
 	
 	private ProposalStrategy saveProposalStrategy(ProposalStrategy strategy, ProposalStrategy oldStrategy, Long budgetProposalId, Long formulaStrategyId) {
 		
 		FormulaStrategy formulaStrategy=null;
 		if(formulaStrategyId != null) {
 		 formulaStrategy = formulaStrategyRepository.findOne(formulaStrategyId);
 		}
 		
 		strategy.setFormulaStrategy(formulaStrategy);
 		
 		// 
 		BudgetProposal b = budgetProposalRepository.findOne(budgetProposalId);
 		
 		b.addAmountRequest(strategy.getTotalCalculatedAmount()-oldStrategy.getTotalCalculatedAmount());
 		b.addAmountRequestNext1Year(strategy.getAmountRequestNext1Year()-oldStrategy.getAmountRequestNext1Year());
 		b.addAmountRequestNext2Year(strategy.getAmountRequestNext2Year()-oldStrategy.getAmountRequestNext2Year());
 		b.addAmountRequestNext3Year(strategy.getAmountRequestNext2Year()-oldStrategy.getAmountRequestNext3Year());
 		
 		budgetProposalRepository.save(b);
 		
 		strategy.setProposal(b);
 		
 		Organization owner = b.getOwner();
 		BudgetType budgetType = b.getBudgetType();
 		
 		BudgetProposal temp = b;
 		// OK we'll go through the amount of this one and it's parent!?
 		while (temp.getForObjective().getParent() != null) {
 			// now we'll get all proposal
 			Objective parent = temp.getForObjective().getParent();
 			temp = budgetProposalRepository.findByForObjectiveAndOwnerAndBudgetType(parent,owner, budgetType);
 			
 			if(temp!=null) {
 				temp.addAmountRequest(strategy.getTotalCalculatedAmount()-oldStrategy.getTotalCalculatedAmount());
 				temp.setBudgetType(budgetType);
 				temp.addAmountRequestNext1Year(strategy.getAmountRequestNext1Year()-oldStrategy.getAmountRequestNext1Year());
 				temp.addAmountRequestNext2Year(strategy.getAmountRequestNext2Year()-oldStrategy.getAmountRequestNext2Year());
 				temp.addAmountRequestNext3Year(strategy.getAmountRequestNext3Year()-oldStrategy.getAmountRequestNext3Year());
 			} else {
 				temp = new BudgetProposal();
 				temp.setForObjective(parent);
 				temp.setOwner(owner);
 				temp.setBudgetType(budgetType);
 				temp.setAmountRequest(strategy.getTotalCalculatedAmount());
 				temp.setAmountRequestNext1Year(strategy.getAmountRequestNext1Year());
 				temp.setAmountRequestNext2Year(strategy.getAmountRequestNext2Year());
 				temp.setAmountRequestNext3Year(strategy.getAmountRequestNext3Year());
 			}
 
 			budgetProposalRepository.save(temp);
 		}
 		
 		// now deal with target
 		if(strategy.getTargetValue() != null && strategy.getTargetValue() > 0) {
 			Objective obj = strategy.getProposal().getForObjective();
 			
 			while(obj.getParent()!=null) {
 				List<TargetValue> tvList = targetValueRepository.findAllByOnwerIdAndTargetUnitIdAndObjectiveId(owner.getId(), strategy.getTargetUnit().getId(), obj.getId());
 				TargetValue tv = null;
 				if(tvList.size() == 0) {
 					
 					//find a matching Target
 					ObjectiveTarget matchingTarget = objectiveTargetRepository.findOneByForObjectivesAndUnit(obj, strategy.getTargetUnit());
 					
 					//crate a new TargetValue
 					if(matchingTarget != null) {
 					
 						tv = new TargetValue();	
 						tv.setTarget(matchingTarget);
 						tv.setForObjective(obj);
 						tv.setOwner(owner);
 						tv.setRequestedValue(strategy.getTargetValue());
 					} else {
 						break;
 					}
 					
 				} else {
 				
 					for(TargetValue tvInList: tvList) {
 						if(tvInList.getTarget().getIsSumable()) {
 							tv = tvInList;
 							
 							tv.adjustRequestedValue(oldStrategy.getTargetValue()-strategy.getTargetValue());
 						} else {
 							break;
 						}
 					}
 				}
 				
 				if(tv != null) targetValueRepository.save(tv);
 				
 				obj = obj.getParent();
 				
 			}
 			
 		}
 		
 		ProposalStrategy strategyJpa =  proposalStrategyRepository.save(strategy);
 		
 		if(strategy.getRequestColumns() != null) {
 			// we have to save these columns first
 			
 			for(RequestColumn rc : strategy.getRequestColumns()) {
 				rc.setProposalStrategy(strategyJpa);
 				requestColumnRepositories.save(rc);
 			}
 		}
 		
 		return strategyJpa;
 	}
 
 	private Long getJsonNodeId(JsonNode node) {
 		if(node == null) {
 			return null;
 		}
 		
 		if(node.get("id") != null) {
 			
 			return node.get("id").asLong();
 		} else {
 			if(node.asLong() == 0) {
 				return null;
 			} else {
 				return node.asLong();
 			}
 		}
 		
 	}
 	
 	private  ProposalStrategy createProposalStrategy(JsonNode psNode) {
 		ProposalStrategy ps;
 		if(getJsonNodeId(psNode) != null) {
 			ps = proposalStrategyRepository.findOne(getJsonNodeId(psNode));
 		} else {
 			ps = new ProposalStrategy();
 		}
 		
 		// the fs suppose to be there or either null?
 		FormulaStrategy fs = null;
 		if(getJsonNodeId(psNode.get("formulaStrategy")) != null) {
 			fs = formulaStrategyRepository.findOne(getJsonNodeId(psNode.get("formulaStrategy")));
 		}
 		
 		
 		ps.setFormulaStrategy(fs);
 		
 		if(psNode.get("name") != null)
 			ps.setName(psNode.get("name").asText());
 		else 
 			ps.setName("");
 		
 		
 		ps.setTotalCalculatedAmount(psNode.get("totalCalculatedAmount").asLong());
 		ps.setAmountRequestNext1Year(psNode.get("amountRequestNext1Year").asLong());
 		ps.setAmountRequestNext2Year(psNode.get("amountRequestNext2Year").asLong());
 		ps.setAmountRequestNext3Year(psNode.get("amountRequestNext3Year").asLong());
 		
 		
 		
 		// now look at the formulaColumns
 		if(psNode.get("formulaStrategy") != null) {
 			
 			logger.debug(">> formulaStrategy: "+ psNode.get("formulaStrategy").toString());
 			
 			List<RequestColumn> rcList = new ArrayList<RequestColumn>();
 			ps.setRequestColumns(rcList);
 			
 			logger.debug(">> requestColumns: "+ psNode.get("requestColumns").toString());
 			Iterator<JsonNode> rcNodeIter = psNode.get("requestColumns").iterator();
 			while (rcNodeIter.hasNext()) {
 			
 				JsonNode rcNode  = rcNodeIter.next();
 				RequestColumn rc;
 				if(getJsonNodeId(rcNode) != null) {
 					rc = requestColumnRepositories.findOne(getJsonNodeId(rcNode));
 				} else {
 					rc = new RequestColumn();
 				}
 				
 				FormulaColumn fc = formulaColumnRepository.findOne(getJsonNodeId(rcNode.get("column")));
 				
 				rc.setAmount(rcNode.get("amount").asInt());
 				rc.setProposalStrategy(ps);
 				rc.setColumn(fc);
 				
 				rcList.add(rc);
 			}
 		}
 		logger.debug(">>> " + psNode.toString());
 		logger.debug(">>> " + psNode.get("targetUnit").toString());
 		
 		// lastly do the targetValue
 		if( getJsonNodeId(psNode.get("targetUnit")) != null ) {
 			TargetUnit unit = targetUnitRepository.findOne(getJsonNodeId(psNode.get("targetUnit")));
 			ps.setTargetUnit(unit);
 			if(psNode.get("targetValue") != null) {
 				ps.setTargetValue(psNode.get("targetValue").asLong());
 				ps.setTargetValueNext1Year(psNode.get("targetValueNext1Year").asLong());
 				ps.setTargetValueNext2Year(psNode.get("targetValueNext2Year").asLong());
 				ps.setTargetValueNext3Year(psNode.get("targetValueNext3Year").asLong());
 			} else {
 				ps.setTargetValue(0L);
 			}
 		}
 		
 		
 		return ps;
 	}
 	
 	@Override
 	public BudgetProposal saveBudgetProposal(JsonNode proposalNode, ThaicomUserDetail currentUser) {
 		
 		logger.debug(proposalNode.toString());
 		
 		// we only deal with new proposal here
 		if(proposalNode.get("id") != null) {
 			return null;
 		}
 		
 		BudgetProposal proposal = new BudgetProposal();
 		// now wire up all the dressing?
 		
 		proposal.setOwner(currentUser.getWorkAt());
 		
 		Long objectiveId = getJsonNodeId(proposalNode.get("forObjective"));
 		
 		Objective forObjective = objectiveRepository.findOne(objectiveId);
 		proposal.setForObjective(forObjective);
 
 		
 		
 		JsonNode a = proposalNode.get("budgetType");
 		getJsonNodeId(a);
 		
 		
 		BudgetType budgetType = budgetTypeRepository.findOne(getJsonNodeId(proposalNode.get("budgetType")));
 		proposal.setBudgetType(budgetType);
 		
 		budgetProposalRepository.save(proposal);
 		
 		ProposalStrategy oldps = null;
 		if(proposalNode.get("proposalStrategies") != null && proposalNode.get("proposalStrategies").get(0) != null 
 				&& proposalNode.get("proposalStrategies").get(0).get("id")!= null) { 
 			 oldps = proposalStrategyRepository.findOne(proposalNode.get("proposalStrategies").get(0).get("id").asLong());
 		}
 		
 		ProposalStrategy oldStrategy = ProposalStrategy.copyLongValue(oldps);
 		
 		
 		ProposalStrategy ps = createProposalStrategy(proposalNode.get("proposalStrategies").get(0));
 	
 		saveProposalStrategy(ps,  oldStrategy, proposal.getId(),
 				ps.getFormulaStrategy() != null ? ps.getFormulaStrategy().getId() : null );
 		
 		if(proposal.getProposalStrategies() == null) {
 			List<ProposalStrategy> psList = new ArrayList<ProposalStrategy> (); 
 			psList.add(ps);
 			
 			proposal.setProposalStrategies(psList);
 		}
 		
 		
 		return proposal;
 	}
 
 	@Override
 	public RequestColumn saveRequestColumn(RequestColumn requestColumn) {
 		return requestColumnRepositories.save(requestColumn);
 	}
 
 	@Override
 	public Objective addBudgetTypeToObjective(Long id, Long budgetTypeId) {
 		// Ok so we get one objective
 		Objective obj = objectiveRepository.findOne(id);
 		
 		if(obj!= null) {
 			//now find the budgetType
 			BudgetType b = budgetTypeRepository.findOne(budgetTypeId);
 			
 			//now we're just ready to add to obj
 			obj.getBudgetTypes().add(b);
 			
 			obj.getTargets().size();
 			objectiveRepository.save(obj);
 			return obj;
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public Objective removeBudgetTypeToObjective(Long id, Long budgetTypeId) {
 		// Ok so we get one objective
 		Objective obj = objectiveRepository.findOne(id);
 		
 		if(obj!= null) {
 			//now find the budgetType
 			BudgetType b = budgetTypeRepository.findOne(budgetTypeId);
 			
 			//now we're just ready to add to obj
 			obj.getBudgetTypes().remove(b);
 			objectiveRepository.save(obj);
 			
 			return obj;
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public Objective updateObjectiveFields(Long id, String name, String code) {
 		/// Ok so we get one objective
 		Objective obj = objectiveRepository.findOne(id);
 		
 		obj.setName(name);
 		obj.setCode(code);
 		
 		objectiveRepository.save(obj);
 		return obj;
 	}
 
 	@Override
 	public Objective saveObjective(JsonNode objectiveJsonNode) {
 		Objective objective;
 		
 		//first check objective id
 		if(objectiveJsonNode.get("id") == null) { 
 			objective = new Objective();
 			objective.setObjectiveName(new ObjectiveName());
 			
 		} else {
 			objective = objectiveRepository.findOne(objectiveJsonNode.get("id").asLong());
 		}
 		
 		objective.setName(objectiveJsonNode.get("name").asText());
 		
 		// doing away with name?
 		if(objectiveJsonNode.get("objectiveName") != null) {
 			String nameTxt = objectiveJsonNode.get("objectiveName").get("name").asText();
 			objective.getObjectiveName().setName(nameTxt);
 			
 		}
 		
 		objective.setFiscalYear(objectiveJsonNode.get("fiscalYear").asInt());
 		objective.getObjectiveName().setFiscalYear(objectiveJsonNode.get("fiscalYear").asInt());
 
 		if(objectiveJsonNode.get("type") != null) {
 			Long objectiveTypeId = objectiveJsonNode.get("type").get("id").asLong();
 			ObjectiveType ot = objectiveTypeRepository.findOne(objectiveTypeId);
 			
 			objective.setType(ot);
 			objective.getObjectiveName().setType(ot);
 			
 		}
 		
 		if(objective.getCode() == null || objective.getCode().length() == 0) {
 			
 			// now find the max code and put this one as the next
 			String maxCode = objectiveRepository.findMaxCodeOfTypeAndFiscalYear(objective.getType(), objective.getFiscalYear());
 			
 			if(maxCode == null || maxCode.length() == 0) {
 				maxCode = "0";
 			}
 			Integer nextCode = Integer.parseInt(maxCode) + 1;
 			objective.setCode(String.format("%0" + objective.getType().getCodeLength() + "d", nextCode));
 		}
 		
 		
 		
 		//lastly the unit
 		if(objectiveJsonNode.get("units") != null ) {
 			objective.setUnits(new ArrayList<TargetUnit>());
 			if(objective.getTargets() != null) {
 				List<ObjectiveTarget> tList = new ArrayList<ObjectiveTarget>();
 				for(ObjectiveTarget t : objective.getTargets()) {
 					tList.add(t);
 				}
 				
 				// now delete t here?
 				while(tList.isEmpty() == false) {
 					ObjectiveTarget t = tList.remove(0);
 					objective.getTargets().remove(t);
 					objectiveTargetRepository.delete(t);
 				}
 				
 			}
 			
 			for(JsonNode unit : objectiveJsonNode.get("units")) {
 				TargetUnit unitJpa = targetUnitRepository.findOne(unit.get("id").asLong());
 				objective.addUnit(unitJpa);
 				
 				ObjectiveTarget t = new ObjectiveTarget();
 				t.setFiscalYear(objective.getFiscalYear());
 				t.setUnit(unitJpa);
 				
 				objectiveTargetRepository.save(t);
 				
 				objective.addTarget(t);
 					
 			}
 			
 			
 		}
 		 
 		
 		logger.debug("1. {} " , objectiveJsonNode.get("parent"));
 		Objective oldParent = objective.getParent();
 		String parentPathLikeString = "." + objective.getId() + ".";
 		List<Objective> allDescendant = objectiveRepository.findAllDescendantOf(parentPathLikeString);
 		
 		if(objectiveJsonNode.get("parent") != null &&  objectiveJsonNode.get("parent").get("id") != null  ) {
 			Long parentId = objectiveJsonNode.get("parent").get("id").asLong();
 			
 			logger.debug("fetching parent : " + parentId);
 			Objective parent = objectiveRepository.findOne(parentId);
 			logger.debug("111111......parent.getChildren.size() = " + parent.getChildren().size());
 			
 			if(parent.getParentPath() == null || parent.getParentPath().length() == 0) {
 			
 				objective.setParentPath("."+parentId+".");
 				objective.setParentLevel(parent.getParentLevel()+1);
 				
 			} else {
 				objective.setParentPath("."+parentId+parent.getParentPath());
 				objective.setParentLevel(parent.getParentLevel()+1);
 				
 			} 
 			
 			if(parent.getLineNumber() != null) {
 				
 				if(objective.getLineNumber() != null) {
 					objectiveRepository.removeFiscalyearLineNumberAt(objective.getFiscalYear(), objective.getLineNumber(), allDescendant.size()+1);
 				}
 				
 				Integer maxLineNumber = null;
 				
 				if(parent.getChildren().size() == 0) {
 					logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>parent LineNumber Before: " + parent.getLineNumber());
 					parent = objectiveRepository.findOne(parentId);
 					logger.debug("parent LineNumber After: " + parent.getLineNumber());
 					maxLineNumber = parent.getLineNumber();
 				} else {
 					maxLineNumber = objectiveRepository.findMaxLineNumberChildrenOf(parent);
 					logger.debug("++++ objectiveRepository.findMaxLineNumberChildrenOf: " + maxLineNumber);
 				}
 				
 				objectiveRepository.insertFiscalyearLineNumberAt(objective.getFiscalYear(), maxLineNumber+allDescendant.size()+1, maxLineNumber+allDescendant.size()+1);
 				objective.setLineNumber(maxLineNumber+1);
 				
 				// now we should set the Line number of all descendant
 				objective.calculateAndSetLineNumberForChildren();
 				
 				// now save all descendant 
 				for(Objective descendant : allDescendant) {
 					objectiveRepository.save(descendant);
 				}
 				
 			}
 			
 			
 			objective.setParent(parent);
 			parent.setIsLeaf(false);
 			objectiveRepository.save(parent);
 			
 		} else if(objective.getType().getId() == ObjectiveTypeId.แผนงาน.getValue()) {
 			// this parent  must be root!
 			Objective root = objectiveRepository.findRootOfFiscalYear(objective.getFiscalYear());
 			objective.setParent(root);
 			
 			objective.setParentLevel(2);
 			objective.setParentPath("." + root.getId().toString() + ".");
 			if(objective.getLineNumber() == null) {
 				// we'll have to find out this line number  
 				Integer maxLineNumber = objectiveRepository.findMaxLineNumberFiscalYear(objective.getFiscalYear());
 				if(maxLineNumber != null) {
 					objective.setLineNumber(maxLineNumber+1);
 				} else {
 					objective.setLineNumber(1);
 				}
 			}
 			
 		} else{
 			// parent is null 
 			objective.setParent(null);
 			objective.setParentPath(".");
 			objective.setParentLevel(1);
 
 			if(objective.getLineNumber() != null) {
 				objectiveRepository.removeFiscalyearLineNumberAt(objective.getFiscalYear(), objective.getLineNumber(), allDescendant.size()+1);
 				objective.setLineNumber(null);
 			}
 			
 			// now save all descendant 
 			for(Objective descendant : allDescendant) {
 				descendant.setLineNumber(null);
 				objectiveRepository.save(descendant);
 			}
 			
 			
 		}
 		
 		// now reset the isLeaf on Old parent 
 		if(oldParent != null) {
 			if(oldParent.getChildren().size() == 0) {
 				oldParent.setIsLeaf(true);
 				objectiveRepository.save(oldParent);
 			}
 		}
 		// and on itself
 		if(objective.getChildren() != null) {
 			if(objective.getChildren().size() == 0) {
 				objective.setIsLeaf(true);
 			} else {
 				objective.setIsLeaf(false);
 			}
 		} else {
 			objective.setIsLeaf(true);
 		}
 		
 		
 		
 		//will have to find the maxone and put the increment here!
 		if(objective.getCode() == null || objective.getCode().length() == 0) {
 			String maxCode = objectiveNameRepository.findMaxCodeOfTypeAndFiscalYear(objective.getType(), objective.getFiscalYear());
 			
 			if(maxCode == null || maxCode.length() == 0) {
 				maxCode = "0";
 			}
 			Integer nextCode = Integer.parseInt(maxCode) + 1;
 			objective.setCode(String.format("%0" + objective.getType().getCodeLength() + "d", nextCode));
 		
 			objective.setCode(nextCode.toString());
 			objective.getObjectiveName().setCode(nextCode.toString());
 			objective.setIndex(nextCode);
 			
 		}
 		
 		logger.debug("code here : " + objective.getCode());
 		
 		objectiveRepository.save(objective);
 		
 		// we have to assume to save only the parameter!
 		
 		// now deal with changes in relations!
 		for(JsonNode relation : objectiveJsonNode.get("relations")) {
 			// if(relation.getId == null 
 			
 			if(relation.get("id") == null) {
 				if(relation.get("parent") != null) {
 					logger.debug("{} ", relation.get("parent"));
 					
 					if(relation.get("parent").get("id") != null) {
 						Long parentId = relation.get("parent").get("id").asLong();
 						Objective parent = objectiveRepository.findOne(parentId);
 						
 						ObjectiveRelations relationJpa = new ObjectiveRelations();
 						relationJpa.setObjective(objective);
 						relationJpa.setChildType(objective.getType());
 						relationJpa.setFiscalYear(objective.getFiscalYear());
 						relationJpa.setParent(parent);
 						relationJpa.setParentType(parent.getType());
 						
 						objectiveRelationsRepository.save(relationJpa);
 					}
 				}
 				
 			} else {
 				ObjectiveRelations relationJpa = objectiveRelationsRepository.findOne(relation.get("id").asLong());
 				if(relation.get("parent").get("id") != null) {
 					Long parentId = relation.get("parent").get("id").asLong();
 					Objective parent = objectiveRepository.findOne(parentId);
 					
 					relationJpa.setParent(parent);
 				//now this will have to only change parent!
 				} else {
 					logger.debug("*************************************parent is null");
 					relationJpa.setParent(null);
 				}
 				
 				objectiveRelationsRepository.save(relationJpa);
 			}
 			
 			
 		}
 		
 		
 		return objective;
 	}
 
 	@Override
 	public Objective newObjectiveWithParam(String name, String code, Long parentId,
 			Long typeId, String parentPath, Integer fiscalYear) {
 		Objective obj = new Objective();
 		obj.setName(name);
 		obj.setCode(code);
 		obj.setParentPath(parentPath);
 		obj.setFiscalYear(fiscalYear);
 		
 		if(parentId != null) {
 			Objective parent = objectiveRepository.findOne(parentId);
 			obj.setParent(parent);
 			obj.setIndex(parent.getChildren().size());
 			
 			// now the parent will not be leaf node anymore
 			parent.setIsLeaf(false);
 			objectiveRepository.save(parent);
 		}
 		
 		ObjectiveType type = objectiveTypeRepository.findOne(typeId);
 		
 		
 		obj.setType(type);
 		
 		obj.setIsLeaf(true);
 		
 		return objectiveRepository.save(obj);
 	}
 
 	@Override
 	public Objective deleteObjective(Long id, Boolean nameCascade) throws ObjectiveHasBudgetProposalException {
 		// ok we'll have to get this one first
 		Objective obj = objectiveRepository.findOne(id);
 		
 		//check first if this objective has been used in proposal!
 		List<BudgetProposal> list = budgetProposalRepository.findAllByForObjective(obj); 
 		
 		if(list.size() > 0) {
 			throw new ObjectiveHasBudgetProposalException(list);
 		}
 		
 		
 		//then get its parent
 		Objective parent = obj.getParent();
 		
 		if(parent != null) {
 			parent.getChildren().remove(obj);
 		
 		
 			if(parent.getChildren() != null && parent.getChildren().size() == 0) {
 				parent.setIsLeaf(true);
 				objectiveRepository.save(parent);
 			} 
 		}
 		
 		objectiveRelationsRepository.deleteAllObjective(obj);
 		
 		
 		if(nameCascade == true) {
 			ObjectiveName name= obj.getObjectiveName();
 			obj.setObjectiveName(null);
 			objectiveNameRepository.delete(name);
 		}
 		
 		objectiveRepository.delete(obj);
 		
 		return obj;
 	}
 
 	@Override
 	public List<ProposalStrategy> findProposalStrategyByBudgetProposal(
 			Long budgetProposalId) {
 		BudgetProposal budgetProposal = budgetProposalRepository.findOne(budgetProposalId);
 		return proposalStrategyRepository.findByProposal(budgetProposal);
 	}
 
 	@Override
 	public List<ProposalStrategy> findProposalStrategyByFiscalyearAndObjective(
 			Integer fiscalYear, Long ownerId, Long objectiveId) {
 		List<ProposalStrategy> psList = proposalStrategyRepository.findByObjectiveIdAndfiscalYearAndOwnerId(fiscalYear, ownerId, objectiveId);
 		for(ProposalStrategy ps : psList) {
 			if(ps.getFormulaStrategy()!=null) {
 				ps.getFormulaStrategy().getFormulaColumns().size();
 				ps.getRequestColumns().size();
 			}
 		}
 		return psList;
 		
 	}
 	
 	@Override
 	public List<ProposalStrategy> findAllProposalStrategyByFiscalyearAndObjective(
 			Integer fiscalYear, Long objectiveId) {
 		return proposalStrategyRepository.findAllByObjectiveIdAndfiscalYearAndOwnerId(fiscalYear, objectiveId);
 	}
 
 	
 
 	
 	@Override
 	public FormulaStrategy updateFormulaStrategyStandardPriceRound(
 			Integer round, Long id, JsonNode data) {
 		FormulaStrategy fs = formulaStrategyRepository.findOne(id);
 		List<AllocationStandardPrice> list = fs.getAllocationStandardPriceMap();
 		AllocationStandardPrice asp = list.get(round-1);
 		asp.setStandardPrice(data.get("allocationStandardPriceMap").get(round-1).get("standardPrice").asInt());
 		formulaStrategyRepository.save(fs);	
 		
 		// now we we'll have to update the allocRecord
 		
 		List<AllocationRecordStrategy> allocStrgyList = allocationRecordStrategyRepository.findAllByStrategy(fs);
 		for(AllocationRecordStrategy ars : allocStrgyList) {
 			
 			Long newTotal = 1L;
 			for(RequestColumn rc : ars.getRequestColumns()) {
 				if(rc.getAmount() != null) {
 					newTotal = newTotal * rc.getAmount();
 				}
 			}
 			newTotal = newTotal * asp.getStandardPrice();
 			
 			// now we can update the totalCalculatedAmount
 			Long adjustedAmount = ars.getTotalCalculatedAmount() - newTotal;
 			ars.setTotalCalculatedAmount(newTotal);
 			
 			// then we'll have to update all the way to the parent!
 			ars.getAllocationRecord().adjustAmountAllocated(adjustedAmount);
 			allocationRecordRepository.save(ars.getAllocationRecord());
 			
 			// now update parent
 			Objective parent = ars.getAllocationRecord().getForObjective().getParent();
 			while(parent != null) {
 				
 				
 				AllocationRecord ar = allocationRecordRepository.findOneByBudgetTypeAndObjectiveAndIndex(
 						fs.getType(), parent, round-1);
 				
 				AllocationRecordStrategy parentArs = allocationRecordStrategyRepository
 						.findOneByAllocationRecordAndStrategy(ar, fs);
 				
 				if(parentArs != null) {
 					parentArs.adjustTotalCalculatedAmount(adjustedAmount);
 					allocationRecordStrategyRepository.save(parentArs);
 					
 					ar.adjustAmountAllocated(adjustedAmount);
 					allocationRecordRepository.save(ar);
 				
 				}
 				
 				parent = parent.getParent();
 				
 			}
 			
 		}
 		
 		return null;
 	}
 
 	@Override
 	public ProposalStrategy updateProposalStrategy(Long id,
 			JsonNode rootNode) throws JsonParseException, JsonMappingException, IOException {
 
 		ProposalStrategy oldPs = proposalStrategyRepository.findOne(id);
 		
 		ProposalStrategy oldStrategy = ProposalStrategy.copyLongValue(oldPs);
 		
 		ProposalStrategy ps = createProposalStrategy(rootNode);
 		
 		
 		
 		saveProposalStrategy(ps, oldStrategy, ps.getProposal().getId(),
 				ps.getFormulaStrategy() == null?null:ps.getFormulaStrategy().getId());
 		
 		
 		return ps;
 		
 //		ProposalStrategy strategy = proposalStrategyRepository.findOne(id);
 //		
 //		if(strategy != null) {
 //			// now get information from JSON string?
 //			
 //			Long adjustedAmount = strategy.getTotalCalculatedAmount() - rootNode.get("totalCalculatedAmount").asLong();
 //			Long adjustedAmountRequestNext1Year = strategy.getAmountRequestNext1Year()==null?0:strategy.getAmountRequestNext1Year() - rootNode.get("amountRequestNext1Year").asLong();
 //			Long adjustedAmountRequestNext2Year = strategy.getAmountRequestNext2Year()==null?0:strategy.getAmountRequestNext2Year() - rootNode.get("amountRequestNext2Year").asLong();
 //			Long adjustedAmountRequestNext3Year = strategy.getAmountRequestNext3Year()==null?0:strategy.getAmountRequestNext3Year() - rootNode.get("amountRequestNext3Year").asLong();
 //			Long adjustedTargetValue = strategy.getTargetValue()==null?0:strategy.getTargetValue() - rootNode.get("targetValue").asLong();
 //			
 //			strategy.adjustTotalCalculatedAmount(adjustedAmount);
 //			
 //			strategy.adjustAmountRequestNext1Year(adjustedAmountRequestNext1Year);
 //			strategy.adjustAmountRequestNext2Year(adjustedAmountRequestNext2Year);
 //			strategy.adjustAmountRequestNext3Year(adjustedAmountRequestNext3Year);
 //			
 //			strategy.setTargetValue(rootNode.get("targetValue").asLong());
 //			
 //			// now looping through the RequestColumns
 //			JsonNode requestColumnsArray = rootNode.get("requestColumns");
 //			
 //			List<RequestColumn> rcList = strategy.getRequestColumns();
 //			for(RequestColumn rc : rcList) {
 //				Long rcId = rc.getId();
 //				// now find this in
 //				for(JsonNode rcNode : requestColumnsArray) {
 //					if( rcId == rcNode.get("id").asLong()) {
 //						//we can just update this one ?
 //						rc.setAmount(rcNode.get("amount").asInt());
 //						break;
 //					}
 //				}
 //				
 //			}
 //			
 //			proposalStrategyRepository.save(strategy);
 //			
 //			// now save this budgetProposal
 //			BudgetProposal b = strategy.getProposal();
 //			b.adjustAmountRequest(adjustedAmount);
 //			b.adjustAmountRequestNext1Year(adjustedAmountRequestNext1Year);
 //			b.adjustAmountRequestNext2Year(adjustedAmountRequestNext2Year);
 //			b.adjustAmountRequestNext3Year(adjustedAmountRequestNext3Year);
 //			
 //			budgetProposalRepository.save(b);
 //			
 //			
 //			
 //			Organization owner = strategy.getProposal().getOwner();
 //			
 //			BudgetProposal temp = b;
 //			// OK we'll go through the amount of this one and it's parent!?
 //			while (temp.getForObjective().getParent() != null) {
 //				// now we'll get all proposal
 //				Objective parent = temp.getForObjective().getParent();
 //				temp = budgetProposalRepository.findByForObjectiveAndOwnerAndBudgetType(parent,owner,b.getBudgetType());
 //				
 //				if(temp!=null) {
 //					temp.adjustAmountRequest(adjustedAmount);
 //					temp.adjustAmountRequestNext1Year(adjustedAmountRequestNext1Year);
 //					temp.adjustAmountRequestNext2Year(adjustedAmountRequestNext2Year);
 //					temp.adjustAmountRequestNext3Year(adjustedAmountRequestNext3Year);
 //				} else {
 //					temp = new BudgetProposal();
 //					temp.setForObjective(parent);
 //					temp.setOwner(owner);
 ////					temp.setBudgetType(parent.getBudgetType());
 //					temp.setAmountRequest(strategy.getTotalCalculatedAmount());
 //					temp.setAmountRequestNext1Year(strategy.getAmountRequestNext1Year());
 //					temp.setAmountRequestNext2Year(strategy.getAmountRequestNext2Year());
 //					temp.setAmountRequestNext3Year(strategy.getAmountRequestNext3Year());
 //				}
 //				budgetProposalRepository.save(temp);
 //			}
 //			
 //			
 //			
 //			
 //			
 //			return strategy;
 //		} else {
 //			return null;
 //		}
 //		
 //		
 	}
 
 
 	@Override
 	public AllocationRecord updateAllocationRecord(Long id, JsonNode data) {
 		AllocationRecord record = allocationRecordRepository.findOne(id);
 		
 		// now update the value
 		Long amountUpdate = data.get("amountAllocated").asLong();
 		Long oldAmount = record.getAmountAllocated();
 		Long adjustedAmount = oldAmount - amountUpdate;
 		
 		Integer index = record.getIndex();
 		BudgetType budgetType = record.getBudgetType();
 		Objective objective = record.getForObjective();
 		
 		record.setAmountAllocated(amountUpdate);
 		allocationRecordRepository.save(record);
 		
 		
 		// now looking back
 		Objective parent = objective.getParent();
 		while(parent.getParent() != null) {
 			logger.debug("parent.id: {}", parent.getId());
 			AllocationRecord temp = allocationRecordRepository.findOneByBudgetTypeAndObjectiveAndIndex(budgetType, parent, index);
 			
 			temp.adjustAmountAllocated(adjustedAmount);
 			
 			allocationRecordRepository.save(temp);
 			
 			parent = parent.getParent();
 			logger.debug("parent.id--: {}", parent.getId());
 		}
 		
 		return record;
 	}
 	
 	
 
 	@Override
 	public ProposalStrategy updateAllocationRecordStrategy(Long id,
 			JsonNode data) {
 		
 		AllocationRecordStrategy ars = allocationRecordStrategyRepository.findOne(id);
 		if(ars != null) {
 			Long amountUpdate = data.get("totalCalculatedAmount").asLong();
 			Long oldAmount = ars.getTotalCalculatedAmount();
 			Long adjustedAmount = oldAmount - amountUpdate;
 			
 			ars.setTotalCalculatedAmount(amountUpdate);
 			
 			for(JsonNode rcNode : data.get("requestColumns")) {
 				RequestColumn rc = requestColumnRepositories.findOne(rcNode.get("id").asLong());
 				rc.setAmount(rcNode.get("amount").asInt());
 				
 				requestColumnRepositories.save(rc);
 			}
 			allocationRecordStrategyRepository.save(ars);
 			
 			// then we update the allocation!
 			AllocationRecord record = ars.getAllocationRecord();
 			record.adjustAmountAllocated(adjustedAmount);
 			allocationRecordRepository.save(record);
 			
 			// now looking back
 			Objective parent = record.getForObjective().getParent();
 			while(parent.getParent() != null) {
 				logger.debug("parent.id: {}", parent.getId());
 				AllocationRecord temp = allocationRecordRepository.findOneByBudgetTypeAndObjectiveAndIndex(record.getBudgetType(), parent, 0);
 				
 				temp.adjustAmountAllocated(adjustedAmount);
 				
 				allocationRecordRepository.save(temp);
 				
 				parent = parent.getParent();
 				logger.debug("parent.id--: {}", parent.getId());
 			}
 			
 		}
 		
 		return null;
 	}
 
 	@Override
 	public List<BudgetProposal> findBudgetProposalByObjectiveIdAndBudgetTypeId(Long objectiveId, Long budgetTypeId) {
 		
 		return budgetProposalRepository.findByForObjective_idAndBudgetType_id(objectiveId, budgetTypeId);
 	}
 
 	
 	
 	@Override
 	public List<BudgetProposal> findBudgetProposalByObjectiveId(Long objectiveId) {
 		return budgetProposalRepository.findByForObjective_id(objectiveId);
 	}
 
 	@Override
 	public Boolean updateBudgetProposalAndReservedBudget(JsonNode data) {
 		//deal with BudgetReseved first
 		JsonNode reservedBudgetJson =  data.get("reservedBudget");
 		
 		Long rbId = reservedBudgetJson.get("id").asLong();
 		Long objectiveId = reservedBudgetJson.get("forObjective").get("id").asLong();
 		Long budgetTypeId = reservedBudgetJson.get("budgetType").get("id").asLong();
 		
 		//get Objective first
 		Objective currentObj = objectiveRepository.findOne(objectiveId);
 		//then BudgetType
 		BudgetType currentBudgetType = budgetTypeRepository.findOne(budgetTypeId);
 		
 		//now find the one
 		ReservedBudget rb = reservedBudgetRepository.findOne(rbId);
 		
 		//get Old value 
 		Long oldAmountReserved = rb.getAmountReserved();
 		if(oldAmountReserved == null) {
 			oldAmountReserved = 0L;
 		}
 		Long newAmountReserved = reservedBudgetJson.get("amountReserved").asLong();
 		Long adjustedAmountReserved = oldAmountReserved - newAmountReserved;
 		
 		rb.setAmountReserved(newAmountReserved);
 		
 		//should be OK to save here
 		reservedBudgetRepository.save(rb);
 		
 		List<Long> parentIds = currentObj.getParentIds();
 		
 		// We are ready to update the parent...
 		
 		List<ReservedBudget> parentReservedBudgets = reservedBudgetRepository.findAllByObjetiveIds(parentIds, currentBudgetType);
 		for(ReservedBudget parentRB : parentReservedBudgets) {
 			Long parentOldAmountReserved = parentRB.getAmountReserved();
 			
 			parentRB.setAmountReserved(parentOldAmountReserved - adjustedAmountReserved);
 			// and we can save 'em
 			reservedBudgetRepository.save(parentRB);
 		}
 		
 		// now we're updating proposals
 		// first get the budgetProposal into Hash
 		Map<Long, JsonNode> budgetProposalMap = new HashMap<Long, JsonNode>();
 		Map<Long, Long> ownerBudgetProposalAdjustedAllocationMap = new HashMap<Long, Long>();
 		Map<Long, JsonNode> requestColumnMap = new HashMap<Long, JsonNode>();
 		Map<Long, JsonNode> formulaColumnMap = new HashMap<Long, JsonNode>();
 		Map<Long, JsonNode> proposalStrategyMap = new HashMap<Long, JsonNode>();
 		for(JsonNode node : data.get("proposals")){
 			budgetProposalMap.put(node.get("id").asLong(), node);
 			
 			for(JsonNode proposalStrategyNode : node.get("proposalStrategies")) {
 				proposalStrategyMap.put(proposalStrategyNode.get("id").asLong(), proposalStrategyNode);
 				
 				for(JsonNode reqeustColumnNode : proposalStrategyNode.get("requestColumns")) {
 					requestColumnMap.put(reqeustColumnNode.get("id").asLong(), reqeustColumnNode);
 				}
 				
 				for(JsonNode formulaColumnNode : proposalStrategyNode.get("formulaStrategy").get("formulaColumns")) {
 					
 					if(!formulaColumnMap.containsKey(formulaColumnNode.get("id").asLong())) {
 						formulaColumnMap.put(formulaColumnNode.get("id").asLong(), formulaColumnNode);
 					}
 				}
 			}
 			
 		}
 		
 		List<BudgetProposal> proposals = budgetProposalRepository.findAllByForObjectiveAndBudgetType(currentObj, currentBudgetType);
 		// ready to loop through and set the owner..
 		for(BudgetProposal proposal : proposals) {
 			JsonNode node = budgetProposalMap.get(proposal.getId());
 			Long oldAmount = proposal.getAmountAllocated() == null ? 0L : proposal.getAmountAllocated();
 			Long newAmount = node.get("amountAllocated").asLong();
 			proposal.setAmountAllocated(newAmount);
 			
 			ownerBudgetProposalAdjustedAllocationMap.put(proposal.getOwner().getId(), oldAmount-newAmount);
 			logger.debug("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++adjusted BudgetProposal: {} " , oldAmount - newAmount);
 			budgetProposalRepository.save(proposal);
 			
 		}
 		
 		//now update the parents
 		List<BudgetProposal> parentProposals = budgetProposalRepository.findAllByForObjectiveIdsAndBudgetType(parentIds, currentBudgetType);
 		for(BudgetProposal parentProposal:  parentProposals) {
 			Long adjustedAmount = ownerBudgetProposalAdjustedAllocationMap.get(parentProposal.getOwner().getId());
 			
 			if(parentProposal.getAmountAllocated() != null ) {
 				parentProposal.setAmountAllocated(parentProposal.getAmountAllocated() - adjustedAmount);
 			} else {
 				parentProposal.setAmountAllocated(0-adjustedAmount);
 			}
 			
 			budgetProposalRepository.save(parentProposal);
 		}
 		
 		//last thing is to update formularStrategy & RequestColumns!
 		
 		// let's get the easy one first, request columns
 		
 		for(JsonNode rcNode : requestColumnMap.values()) {
 			Long rcid = rcNode.get("id").asLong();
 			RequestColumn rc = requestColumnRepositories.findOne(rcid);
 			rc.setAllocatedAmount(rcNode.get("allocatedAmount").asInt());
 			
 			
 			logger.debug("saving... rc.id {} with allocatedAmount {}", rc.getId(), rcNode.get("allocatedAmount").asInt());
 			requestColumnRepositories.save(rc);
 		}
 		
 		for(JsonNode fcNode : formulaColumnMap.values()) {
 			Long fcid = fcNode.get("id").asLong();
 			FormulaColumn fc = formulaColumnRepository.findOne(fcid);
 			fc.setAllocatedValue(fcNode.get("allocatedValue").asLong());
 			
 			formulaColumnRepository.save(fc);
 		}
 		
 		for(JsonNode psNode : proposalStrategyMap.values()) {
 			Long psid = psNode.get("id").asLong();
 			ProposalStrategy ps = proposalStrategyRepository.findOne(psid);
 			ps.setTotalCalculatedAllocatedAmount(psNode.get("totalCalculatedAllocatedAmount").asLong());
 			
 			proposalStrategyRepository.save(ps);					
 		}
 		
 		return true;
 	}
 
 	@Override
 	public List<TargetUnit> findAllTargetUnits() {
 		return (List<TargetUnit>) targetUnitRepository.findAllSortedByName();
 	}
 
 	@Override
 	public Page<TargetUnit> findAllTargetUnits(PageRequest pageRequest) {
 		return (Page<TargetUnit>) targetUnitRepository.findAll(pageRequest);
 	}
 	
 	@Override
 	public Page<TargetUnit> findAllTargetUnits(PageRequest pageRequest,
 			String query) {
 		query = "%" + query + "%";
 		return (Page<TargetUnit>) targetUnitRepository.findAllByNameLike( query, pageRequest);
 	}
 
 	@Override
 	public List<ObjectiveTarget> findAllObjectiveTargets() {
 		return (List<ObjectiveTarget>) objectiveTargetRepository.findAll();
 	}
 
 	@Override
 	public TargetUnit saveTargetUnits(TargetUnit targetUnit) {
 		return targetUnitRepository.save(targetUnit);
 	}
 
 	@Override
 	public TargetUnit updateTargetUnit(TargetUnit targetUnit) {
 		TargetUnit targetUnitJPA = targetUnitRepository.findOne(targetUnit.getId());
 		if(targetUnitJPA != null) {
 			targetUnitJPA.setName(targetUnit.getName());
 			
 			targetUnitRepository.save(targetUnitJPA);
 			return targetUnitJPA;
 		}
 		return null;
 	}
 
 	@Override
 	public TargetUnit deleteTargetUnit(TargetUnit targetUnit) {
 		TargetUnit targetUnitJPA = targetUnitRepository.findOne(targetUnit.getId());
 		if(targetUnitJPA != null) {
 			targetUnitRepository.delete(targetUnitJPA);
 			
 			return targetUnitJPA;
 		}
 		return null;	}
 
 	@Override
 	public ObjectiveTarget saveObjectiveTarget(ObjectiveTarget objectiveTarget) {
 		return objectiveTargetRepository.save(objectiveTarget);
 	}
 
 	@Override
 	public ObjectiveTarget updateObjectiveTarget(
 			ObjectiveTarget objectiveTarget) {
 		ObjectiveTarget objectiveTargetJPA = objectiveTargetRepository.findOne(objectiveTarget.getId());
 		if(objectiveTargetJPA != null) {
 			objectiveTargetJPA.setName(objectiveTarget.getName());
 			
 			objectiveTargetRepository.save(objectiveTargetJPA);
 			return objectiveTargetJPA;
 		}
 		return null;
 	}
 
 	@Override
 	public ObjectiveTarget deleteObjectiveTarget(
 			ObjectiveTarget objectiveTarget) {
 		ObjectiveTarget objectiveTargetJPA = objectiveTargetRepository.findOne(objectiveTarget.getId());
 		if(objectiveTargetJPA != null) {
 			
 			objectiveTargetRepository.delete(objectiveTargetJPA);
 			return objectiveTargetJPA;
 		}
 		return null;
 	}
 
 	@Override
 	public TargetUnit findOneTargetUnit(Long id) {
 		return targetUnitRepository.findOne(id);
 	}
 
 	@Override
 	public ObjectiveTarget findOneObjectiveTarget(Long id) {
 		return objectiveTargetRepository.findOne(id);
 	}
 
 	@Override
 	public ObjectiveTarget updateObjectiveTarget(JsonNode node) {
 		ObjectiveTarget ot = findOneObjectiveTarget(node.get("id").asLong());
 		// now filling in only what we need!
 		if(ot!=null) {
 			ot.setName(node.get("name").asText());
 			ot.setIsSumable(node.get("isSumable").asBoolean());
 			
 			TargetUnit tu = findOneTargetUnit(node.get("unit").get("id").asLong());
 			if(tu != null) {
 				ot.setUnit(tu);
 			}
 			
 			return saveObjectiveTarget(ot);
 		} 
 		 return null;
 	}
 
 	@Override
 	public ObjectiveTarget saveObjectiveTarget(JsonNode node) {
 		ObjectiveTarget ot = new ObjectiveTarget();
 		// now filling in only what we need!
 
 		ot.setName(node.get("name").asText());
 		ot.setIsSumable(node.get("isSumable").asBoolean());
 		ot.setFiscalYear(node.get("fiscalYear").asInt());
 		
 		TargetUnit tu = findOneTargetUnit(node.get("unit").get("id").asLong());
 		if(tu != null) {
 			ot.setUnit(tu);
 		}
 		
 		return saveObjectiveTarget(ot);
 	}
 
 	@Override
 	public ObjectiveTarget deleteObjectiveTarget(Long id) {
 		ObjectiveTarget objectiveTarget = objectiveTargetRepository.findOne(id);
 		return deleteObjectiveTarget(objectiveTarget);
 	}
 
 	@Override
 	public TargetUnit updateTargetUnit(JsonNode node) {
 		TargetUnit tu = findOneTargetUnit(node.get("id").asLong());
 		// now filling in only what we need!
 		if(tu!=null) {
 			tu.setName(node.get("name").asText());
 	
 			
 			return saveTargetUnits(tu);
 		} 
 		 return null;
 	}
 
 	@Override
 	public TargetUnit saveTargetUnit(JsonNode node) {
 		TargetUnit tu = new TargetUnit();
 		tu.setName(node.get("name").asText());
 		return saveTargetUnits(tu);
 	}
 
 	@Override
 	public TargetUnit deleteTargetUnit(Long id) {
 		TargetUnit targetUnit = findOneTargetUnit(id);
 		
 		return deleteTargetUnit(targetUnit);
 	}
 
 	@Override
 	public List<ObjectiveTarget> findAllObjectiveTargetsByFiscalyear(Integer fiscalYear) {
 		return objectiveTargetRepository.findAllByFiscalYear(fiscalYear);
 	}
 
 	@Override
 	public void addTargetToObjective(Long id, Long targetId) {
 		Objective o = objectiveRepository.findOne(id);
 		ObjectiveTarget ot = objectiveTargetRepository.findOne(targetId);
 		
 		if(o.getTargets().lastIndexOf(ot) >= 0) {
 			// we should be save to return here?
 			return;
 		} else {
 			
 			ObjectiveTarget otJPA = null;
 			if(o.getTargets().size() > 0) {
 				otJPA = o.getTargets().get(0);
 			}
 
 			// remove the one we have first
 			if(otJPA != null) {
 				o.getTargets().remove(otJPA);
 			} 
 			
 			o.addTarget(ot);
 			
 			// and we should be save this one
 			objectiveRepository.save(o);
 			
 			// now go on to its parents;
 			List<Objective> parents = objectiveRepository.findAllObjectiveByIds(
 					o.getParentIds());
 			
 			for(Objective parent: parents) {
 				
 				// we'll have to somehow take out the old one out too
 				if(otJPA != null) {
 					
 					
 					List<ObjectiveTarget> otList = findObjectiveTargetForChildrenObjective(parent.getId(), otJPA.getId());
 					if(otList.size() == 0) {
 						// now we can take the old out
 						parent.getTargets().remove(otJPA);
 					}
 				}					
 
 				if(parent.addTarget(ot)) {
 					objectiveRepository.save(parent);
 				}
 
 				
 			}
 		}
 		
 	}
 
 	private List<ObjectiveTarget> findObjectiveTargetForChildrenObjective(
 			Long objectiveId, Long targetId) {
 		
 		logger.debug("targetId: {} ",  targetId);
 		logger.debug("objectiveIdLike: {}", "%."+objectiveId+"%");
 		
 		return objectiveTargetRepository.findAllByIdAndChildrenOfObjectiveId(targetId, "%."+objectiveId+"%");
 	}
 
 	@Override
 	public TargetValue saveTargetValue(JsonNode node, Organization workAt) throws Exception {
 		Long targetValueId = null;
 		if(node.get("id") != null) {
 			targetValueId = node.get("id").asLong();
 		}
 		
 		Long forObjectiveId = node.get("forObjective").get("id").asLong();
 		Objective obj = objectiveRepository.findOne(forObjectiveId);
 
 		Long objectiveTargetId = node.get("target").get("id").asLong();
 		ObjectiveTarget target = objectiveTargetRepository.findOne(objectiveTargetId);
 		
 		Long adjustedRequestedValue = 0L;
 		Long requestedValue = node.get("requestedValue").asLong();
 		
 		TargetValue tv;
 		if(targetValueId == null) {
 			tv = new TargetValue();
 			tv.setOwner(workAt);
 			tv.setForObjective(obj);
 			tv.setTarget(target);
 			
 			
 		} else {
 			tv = targetValueRepository.findOne(targetValueId);
 			tv.setOwner(workAt);
 			adjustedRequestedValue = tv.getRequestedValue();
 			
 		}
 		
 		tv.setRequestedValue(node.get("requestedValue").asLong());
 		adjustedRequestedValue -= requestedValue;
 		targetValueRepository.save(tv);
 		
 		if(target.getIsSumable() == true ) {
 		
 			logger.debug("---------------------------------parents : " + obj.getParentIds());
 			
 			// now loop for parent
 			Objective parent = obj.getParent();
 			while(parent!=null) {
 				
 				// now get the matching target
 				ObjectiveTarget matchingTarget = null;
 				// now find the matching unit
 				for(ObjectiveTarget t : parent.getTargets()) {
 					if(t.getUnit().getId() == target.getUnit().getId()) {
 						matchingTarget = t;
 					}
 				}
 				
 				if(matchingTarget == null) {
 					break;
 				}
 		
 				
 				// now we'll find the matching value
 				List<TargetValue> tvs = targetValueRepository.findAllByOnwerIdAndTargetUnitIdAndObjectiveId(
 						workAt.getId(), matchingTarget.getUnit().getId(), parent.getId());
 				
 				if(tvs.size() == 0) {
 					//crate a new TargetValue
 					TargetValue newTv = new TargetValue();
 					newTv.setTarget(matchingTarget);
 					newTv.setForObjective(parent);
 					newTv.setOwner(workAt);
 					newTv.setRequestedValue(requestedValue);
 					
 					logger.debug("---------adding new tv with target.id: {}, requestedValue : {}",  matchingTarget.getId(), requestedValue);
 					targetValueRepository.save(newTv);
 					
 				} else if (tvs.size() == 1) {
 					TargetValue matchTv = tvs.get(0);
 					logger.debug("---------updating tv with adjustedReqeust : {}",  adjustedRequestedValue);
 					logger.debug("----------oldone is {}", matchTv.getRequestedValue());
 					matchTv.adjustRequestedValue(adjustedRequestedValue);
 					logger.debug("----------newone is {}", matchTv.getRequestedValue());
 					
 					targetValueRepository.save(matchTv);
 				}
 				
 				logger.debug("-------------------------------parents: " + parent.getId());
 				
 				logger.debug("matchingTarget == null {} ", matchingTarget == null);
 				
 				logger.debug("matchingTarget.getIsSumable == null {} ", matchingTarget.getIsSumable() == null);
 				logger.debug("matchingTarget.getIsSumable {} ", matchingTarget.getIsSumable());
 				
 				
 				if(matchingTarget == null || matchingTarget.getIsSumable() == null || matchingTarget.getIsSumable() == false) {
 					logger.debug("******not found");
 					break;
 				}
 				parent = parent.getParent();
 			}
 		}
 		return tv;
 	}
 
 	@Override
 	public TargetValueAllocationRecord saveTargetValueAllocationRecord(JsonNode node,
 			Organization workAt) {
 		Long tvarId = null;
 		if(node.get("id") != null) {
 			tvarId = node.get("id").asLong();
 		}
 		
 		Long forObjectiveId = node.get("forObjective").get("id").asLong();
 		Objective obj = objectiveRepository.findOne(forObjectiveId);
 
 		Long objectiveTargetId = node.get("target").get("id").asLong();
 		ObjectiveTarget target = objectiveTargetRepository.findOne(objectiveTargetId);
 		
 		Long adjustedRequestedValue = 0L;
 		Long requestedValue = node.get("amountAllocated").asLong();
 		
 		TargetValueAllocationRecord tvar;
 		tvar = targetValueAllocationRecordRepository.findOne(tvarId);
 		
 		adjustedRequestedValue = tvar.getAmountAllocated();
 			
 	
 		
 		tvar.setAmountAllocated(requestedValue);
 		adjustedRequestedValue -= requestedValue;
 		targetValueAllocationRecordRepository.save(tvar);
 		
 		
 		for(Objective parent : objectiveRepository.findAllObjectiveByIds(obj.getParentIds())) {
 			TargetValueAllocationRecord parentTvar = targetValueAllocationRecordRepository.findOneByIndexAndForObjectiveAndTarget(tvar.getIndex(), parent, tvar.getTarget());
 			
 			
 			parentTvar.adjustAmountAllocated(adjustedRequestedValue);
 			
 			targetValueAllocationRecordRepository.save(parentTvar);			
 			
 		}
 		
 		return tvar;
 	}
 
 	@Override
 	public void saveLotsTargetValue(JsonNode node) {
 		for(JsonNode n: node) {
 			
 			
 			Long id = n.get("id").asLong();
 			logger.debug("++++++++++++++++++++++++++++++++++++++++++++++++++ {} ", id);
 			
 			
 			
 			TargetValue tv = targetValueRepository.findOne(id);
 			
 			Long oldAmount = tv.getAllocatedValue();
 			if(oldAmount == null) {
 				oldAmount = 0L;
 			}
 			
 			tv.setAllocatedValue(n.get("allocatedValue").asLong());
 			
 			Long newAmout = tv.getAllocatedValue();
 			Long adjustedRequestedValue = oldAmount-newAmout;
 			
 			
 			targetValueRepository.save(tv);
 			
 			List<TargetValue> tvs = targetValueRepository
 				.findAllByOnwerIdAndObjectiveIdIn(
 						tv.getOwner().getId(), tv.getTarget().getId(),  tv.getForObjective().getParentIds());
 
 			
 			for(TargetValue parentTv: tvs) {
 				parentTv.adjustAllocatedValue(adjustedRequestedValue);
 				
 				targetValueRepository.save(parentTv);
 			}
 			
 			//now ineach tv has to go get the parents?
 		}
 
 		
 	}
 
 	/**
 	 * ค้นหา  Objective ด้วยปีงบประมาณ และ ชนิด โดยกำหนด PageRequest และผ่านค่า 
 	 * คำที่ต้องการค้นหาเบื้องต้น
 	 * 
 	 *  
 	 */
 	@Override
 	public Page<Objective> findObjectivesByFiscalyearAndTypeId(
 			Integer fiscalYear, Long typeId,
 			String query,   Pageable pageable) {
 		
 		logger.debug("++++ query: " + query);
 		
 		Page<Objective> page = objectiveRepository.findByFiscalYearAndType_Id(fiscalYear, typeId, query, pageable);
 		for(Objective obj : page.getContent()) {
 			obj.getTargets().size();
 			if(obj.getType().getParent() != null) {
 				obj.getType().getParent().getName();
 				if(obj.getParent() != null) {
 					obj.getParent().getName();
 					logger.debug(" +++++++++++++++++++++++++++++++++++++++++  {} ",obj.getParent().getName() );
 				}
 			}
 			obj.getUnits().size();
 			logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + obj.getTargets().size());
 			obj.getTargets().size();
 		}
 		return page;
 	}
 
 	
 	@Override
 	public List<Objective> findObjectivesByFiscalyearAndTypeId(
 			Integer fiscalYear, Long typeId) {
 		List<Objective> objs = objectiveRepository.findAllByFiscalYearAndType_id(fiscalYear, typeId);
 		for(Objective obj : objs){
 			obj.getTargets().size();
 			if(obj.getType().getParent() != null) {
 				obj.getType().getParent().getName();
 			}
 		}
 		
 		return objs;
 	}
 	
 	@Override
 	public Page<Objective> findObjectivesByFiscalyearAndTypeId(
 			Integer fiscalYear, Long typeId, Pageable pageable) {
 		
 		Page<Objective> page = objectiveRepository.findPageByFiscalYearAndType_id(fiscalYear, typeId, pageable);
 		for(Objective obj : page.getContent()) {
 			obj.getTargets().size();
 			if(obj.getType().getParent() != null) {
 				obj.getType().getParent().getName();
 				if(obj.getParent() != null) {
 					obj.getParent().getName();
 					logger.debug(" +++++++++++++++++++++++++++++++++++++++++  {} ",obj.getParent().getName() );
 				}
 			}
 			obj.getUnits().size();
 			logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + obj.getTargets().size());
 			obj.getTargets().size();
 		}
 		
 		return page;
 	}
 	
 	
 	
 
 	@Override
 	public Objective updateObjectiveParent(Long id, Long parentId) {
 		Objective o = objectiveRepository.findOne(id);
 		String oldParentPath = "." +  o.getId().toString() +  o.getParentPath();
 		
 		Objective parent = objectiveRepository.findOne(parentId);
 		
 		if(o != null && parent != null) {
 			o.setParent(parent);
 			if(parent.getParentPath()==null) {
 				o.setParentPath("."+parent.getId()+".");
 			} else {
 				o.setParentPath("."+parent.getId()+parent.getParentPath());
 			}
 			objectiveRepository.save(o);
 			
 			
 			// now every child with old o parentpath will have to be updated
 			logger.debug(oldParentPath);
 			
 			List<Objective> children = objectiveRepository
 					.findAllByFiscalYearAndParentPathLike(o.getFiscalYear(), "%"+oldParentPath);
 			
 			for(Objective child : children) {
 				logger.debug("-------" + child.getId().toString() + " old ParentPath : " + child.getParentPath());
 				String oldString = child.getParentPath();
 				child.setParentPath(oldString.replace(oldParentPath, "." +  o.getId().toString() +o.getParentPath()));
 				logger.debug("-------" + child.getId().toString() + " new ParentPath : " + child.getParentPath());
 				objectiveRepository.save(child);
 			}
 			
 			
 			return o;
 		}
 		return null;
 	}
 
 	@Override
 	public Objective objectiveAddReplaceUnit(Long id, Long unitId) {
 		Objective o = objectiveRepository.findOne(id);
 		TargetUnit unit = targetUnitRepository.findOne(unitId);
 		
 		if(o != null && unit != null ) {
 			o.addReplaceUnit(unit);
 		}
 		objectiveRepository.save(o);
 		return o;
 	}
 
 	@Override
 	public List<ObjectiveRelations> findObjectiveRelationsByFiscalYearAndChildTypeRelation(
 			Integer fiscalYear, Long childTypeId) {
 		
 		ObjectiveType childType = objectiveTypeRepository.findOne(childTypeId);
 		
 		return objectiveRelationsRepository.findAllByFiscalYearAndChildType(fiscalYear, childType);
 	}
 
 	@Override
 	public List<ObjectiveRelations> findObjectiveRelationsByFiscalYearAndChildTypeRelationWithObjectiveIds(
 			Integer fiscalYear, Long childTypeId, List<Long> ids) {
 		
 		ObjectiveType childType = objectiveTypeRepository.findOne(childTypeId);
 		return objectiveRelationsRepository.findAllByFiscalYearAndChildTypeWithIds(fiscalYear, childType, ids);
 	}
 	
 	@Override
 	public ObjectiveRelations saveObjectiveRelations(JsonNode relation) {
 		
 		Long objectiveId = relation.get("objective").get("id").asLong();
 		Long parentId = relation.get("parent").get("id").asLong();
 		Integer fiscalYear = relation.get("fiscalYear").asInt();
 		
 		Objective objective = objectiveRepository.findOne(objectiveId);
 		if(objective.getParent() != null) {
 			objective.getParent().getName();
 		}
 		Objective parent = objectiveRepository.findOne(parentId);
 		
 		ObjectiveRelations relationJpa = new ObjectiveRelations();
 		
 		relationJpa.setFiscalYear(fiscalYear);
 		relationJpa.setObjective(objective);
 		relationJpa.setChildType(objective.getType());
 		
 		relationJpa.setParent(parent);
 		relationJpa.setParentType(parent.getType());
 		relationJpa.getObjective().getUnits().size();
 		
 		objectiveRelationsRepository.save(relationJpa);
 		
 		return relationJpa;
 	}
 
 	@Override
 	public ObjectiveRelations updateObjectiveRelations(Long id,
 			JsonNode relation) {
 		ObjectiveRelations relationJpa = objectiveRelationsRepository.findOne(id);
 		Long parentId = relation.get("parent").get("id").asLong();
 		Objective parent = objectiveRepository.findOne(parentId);
 		
 		relationJpa.setParent(parent);
 		relationJpa.setParentType(parent.getType());
 		
 		relationJpa.getObjective().getParent();
 		
 		if(relationJpa.getObjective().getParent() != null) {
 			relationJpa.getObjective().getParent().getName();
 		}
 		
 		relationJpa.getObjective().getUnits().size();
 		
 		objectiveRelationsRepository.save(relationJpa);
 		
 		return relationJpa;
 	}
 
 	@Override
 	public String initFiscalYear(Integer fiscalYear) {
 		
 		
 		Objective obj = objectiveRepository.findRootOfFiscalYear(fiscalYear);
 		
 		if(obj == null) {
 			ObjectiveType rootType = objectiveTypeRepository.findOne(ObjectiveTypeId.ROOT.getValue());
 			
 			ObjectiveName objName = new ObjectiveName();
 			objName.setName("ROOT");
 			objName.setFiscalYear(fiscalYear);
 			objName.setType(rootType);
 			
 			obj = new Objective();
 			obj.setName("ROOT");
 			obj.setParent(null);
 			obj.setParentPath(".");
 			obj.setParentLevel(1);
 			obj.setFiscalYear(fiscalYear);
 			obj.setObjectiveName(objName);
 			
 			
 			obj.setType(rootType);
 			
 			objectiveNameRepository.save(objName);
 			objectiveRepository.save(obj);
 			
 			
 		}
 		
 		// now init fiscalBudgetType
 		logger.debug("initfiscalBudgetType");
 		initFiscalBudgetType(fiscalYear);
 		
 		return "success";
 	}
 
 	@Override
 	public String mappedUnit() {
 		Iterable<Objective> list = objectiveRepository.findAll();
 		for(Objective o : list) {
 			if(o.getUnits() != null) {
 				for(TargetUnit u : o.getUnits()) {
 					ObjectiveTarget t = new ObjectiveTarget();
 					t.setUnit(u);
 					t.setFiscalYear(o.getFiscalYear());
 					
 					objectiveTargetRepository.save(t);
 					
 					o.addTarget(t);
 					
 					objectiveRepository.save(o);
 				}
 				
 				
 			}
 		}
 		
 		return "success";
 	}
 
 	@Override
 	public ObjectiveTarget addUnitToObjective(Long objectiveId, Long unitId,
 			Integer isSumable) {
 		Objective o = objectiveRepository.findOne(objectiveId);
 		TargetUnit u = targetUnitRepository.findOne(unitId);
 		
 		ObjectiveTarget t = new ObjectiveTarget();
 		t.setUnit(u);
 		
 		if(isSumable == 1 ) {
 			t.setIsSumable(true);
 		} else { 
 			t.setIsSumable(false);
 		}
 		t.setFiscalYear(o.getFiscalYear());
 		
 		objectiveTargetRepository.save(t);
 		
 		// now save t
 		o.addTarget(t);
 		
 		objectiveRepository.save(o);
 		
 		
 		return t;
 	}
 
 	@Override
 	public String removeUnitFromObjective(Long objectiveId,
 			Long targetId) {
 		Objective o = objectiveRepository.findOne(objectiveId);
 		ObjectiveTarget t= objectiveTargetRepository.findOne(targetId);
 		
 
 		
 		o.getTargets().remove(t);
 		o.getObjectiveName().getTargets().remove(t);
 		
 		t.setUnit(null);
 		
 		
 		objectiveTargetRepository.delete(t);
 				
 		
 		return "success";
 	}
 	
 	@Override
 	public ObjectiveTarget addUnitToObjectiveName(Long id, Long unitId,
 			Integer isSumable) {
 		ObjectiveName o = objectiveNameRepository.findOne(id);
 		TargetUnit u = targetUnitRepository.findOne(unitId);
 		
 		ObjectiveTarget t = new ObjectiveTarget();
 		t.setUnit(u);
 		
 		if(isSumable == 1 ) {
 			t.setIsSumable(true);
 		} else { 
 			t.setIsSumable(false);
 		}
 		t.setFiscalYear(o.getFiscalYear());
 		
 		objectiveTargetRepository.save(t);
 		
 		// now save t
 		o.addTarget(t);
 		
 		objectiveNameRepository.save(o);
 		
 		
 		return t;
 	}
 
 	@Override
 	public String removeUnitFromObjectiveName(Long id, Long targetId) {
 		ObjectiveName o = objectiveNameRepository.findOne(id);
 		ObjectiveTarget t= objectiveTargetRepository.findOne(targetId);
 		
 		o.getTargets().remove(t);
 		
 		t.setUnit(null);
 				
 		objectiveTargetRepository.delete(t);
 		return "success";
 	}
 
 	@Override
 	public List<BudgetCommonType> findAllBudgetCommonTypes(Integer fiscalYear) {
 		
 		return budgetCommonTypeRepository.findAllByFiscalYear(fiscalYear);
 	}
 
 	@Override
 	public BudgetCommonType findOneBudgetCommonType(Long id) {
 
 		return budgetCommonTypeRepository.findOne(id);
 	}
 
 	@Override
 	public BudgetCommonType updateBudgetCommonType(JsonNode node) {
 		Long id = null;
 		if(node.get("id") == null) {
 			return null;
 		}
 		
 		id = node.get("id").asLong();
 		BudgetCommonType bct = budgetCommonTypeRepository.findOne(id);
 		
 		bct.setName(node.get("name").asText());
 		
 		return budgetCommonTypeRepository.save(bct);
 	}
 
 	@Override
 	public BudgetCommonType saveBudgetCommonType(JsonNode node) {
 		BudgetCommonType bct = new BudgetCommonType();
 		
 		// now set 
 		if(node.get("code") != null)  {
 			bct.setCode(node.get("code").asText());
 		}
 		bct.setFiscalYear(node.get("fiscalYear").asInt());
 		bct.setName(node.get("name").asText());
 		
 		budgetCommonTypeRepository.save(bct);
 		
 		bct.setCode(bct.getId().toString());
 		
 		budgetCommonTypeRepository.save(bct);
 		
 		return bct;
 		
 	}
 
 	@Override
 	public BudgetCommonType deleteBudgetCommonType(Long id) {
 		BudgetCommonType bct = budgetCommonTypeRepository.findOne(id);
 		
 		if(bct != null) {
 			budgetCommonTypeRepository.delete(bct);
 		}
 		
 		return bct;
 	}
 
 	@Override
 	public List<ObjectiveBudgetProposal> findObjectiveBudgetproposalByObjectiveIdAndOwnerId(
 			Long objectiveId, Long ownerId) {
 		
 		return objectiveBudgetProposalRepository.findAllByForObjective_IdAndOwner_Id(objectiveId, ownerId);
 	}
 
 	@Override
 	public List<FiscalBudgetType> findAllFiscalBudgetTypeByFiscalYear(
 			Integer fiscalYear) {
 		return fiscalBudgetTypeRepository.findAllByFiscalYear(fiscalYear);
 	}
 	
 	@Override
 	public List<FiscalBudgetType> findAllFiscalBudgetTypeByFiscalYearUpToLevel(
 			Integer fiscalYear, Integer level) {
 		// TODO Auto-generated method stub
 		return fiscalBudgetTypeRepository.findAllByFiscalYearUpToLevel(fiscalYear, level);
 	}
 
 
 	@Override
 	public String updateFiscalBudgetTypeIsMainBudget(Integer fiscalYear, List<Long> idList) {
 		fiscalBudgetTypeRepository.setALLIsMainBudgetToFALSE(fiscalYear);
 		
 		if(idList.size() > 0) {
 			fiscalBudgetTypeRepository.setIsMainBudget(fiscalYear, idList);
 		}
 		
 		return "success";
 	}
 
 	@Override
 	public ObjectiveBudgetProposal saveObjectiveBudgetProposal(
 			Organization workAt, JsonNode node) {
 
 		ObjectiveBudgetProposal obp;
 		
 		if(getJsonNodeId(node) != null) {
 			obp = objectiveBudgetProposalRepository.findOne(getJsonNodeId(node));
 			
 		} else {
 			obp = new ObjectiveBudgetProposal();
 			// now for Each targetValue we'll have to init one here
 			if(node.get("targets") != null) {
 				
 				obp.setTargets(new ArrayList<ObjectiveBudgetProposalTarget>());
 				for(JsonNode target : node.get("targets")){
 					ObjectiveBudgetProposalTarget targetJPA = new ObjectiveBudgetProposalTarget();
 					targetJPA.setUnit(targetUnitRepository.findOne(getJsonNodeId(target.get("unit"))));
 					targetJPA.setObjectiveBudgetProposal(obp);
 					obp.getTargets().add(targetJPA);
 					
 				}
 				
 			}
 		}
 		ObjectiveBudgetProposal obpOldValue = new ObjectiveBudgetProposal();
 		obpOldValue.copyValue(obp);
 		
 		if(node.get("targets") != null) {
 			for(JsonNode target : node.get("targets")){
 				logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>JSON_NODE_UNIT_ID" + getJsonNodeId(node.get("unit")));
 				for(ObjectiveBudgetProposalTarget targetJpa : obp.getTargets()) {
 					logger.debug("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<TARGETJPA_UNIT_ID" + targetJpa.getUnit().getId());
 					if(targetJpa.getUnit().getId() == getJsonNodeId(target.get("unit"))) {
 						targetJpa.setTargetValue(target.get("targetValue").asLong());
 						targetJpa.setTargetValueNext1Year(target.get("targetValueNext1Year").asLong());
 						targetJpa.setTargetValueNext2Year(target.get("targetValueNext2Year").asLong());
 						targetJpa.setTargetValueNext3Year(target.get("targetValueNext3Year").asLong());
 						break;
 					}
 				}
 			}	
 		}
 		
 		obp.setOwner(workAt);
 		
 		BudgetType type = null;
 		
 		if(node.get("budgetType") !=null ) {
 			if(node.get("budgetType").get("id") != null) {
 				type = budgetTypeRepository.findOne(node.get("budgetType").get("id").asLong());
 			}
 		}
 		
 		Objective objective= null;
 		if(node.get("forObjective") !=null ) {
 			if(node.get("forObjective").get("id") != null) {
 				objective = objectiveRepository.findOne(node.get("forObjective").get("id").asLong());
 			}
 		}
 		
 		if(type == null) {
 			return null;
 		}
 		
 		if(objective == null) {
 			return null;
 		}
 		
 		obp.setBudgetType(type);
 		obp.setForObjective(objective);
 		
 		if(node.get("amountRequest")!=null ) {
 			obp.setAmountRequest(node.get("amountRequest").asLong());
 		}
 		
 		if(node.get("amountRequestNext1Year")!=null ) {
 			obp.setAmountRequestNext1Year(node.get("amountRequestNext1Year").asLong());
 		}
 		
 		if(node.get("amountRequestNext2Year")!=null ) {
 			obp.setAmountRequestNext2Year(node.get("amountRequestNext2Year").asLong());
 		}
 		
 		if(node.get("amountRequestNext3Year")!=null ) {
 			obp.setAmountRequestNext3Year(node.get("amountRequestNext3Year").asLong());
 		}
 		
 		objectiveBudgetProposalRepository.save(obp);
 		List<ObjectiveBudgetProposal> obpList = objectiveBudgetProposalRepository.findAllByForObjective_IdAndOwner_Id(obp.getForObjective().getId(), workAt.getId());
 		obp.getForObjective().setFilterObjectiveBudgetProposals(obpList);
 		
 		//now before return back we'll update the parents
 		Objective o = obp.getForObjective().getParent();
 		BudgetType budgetType = obp.getBudgetType();
 		while(o != null) {
 			ObjectiveBudgetProposal obpParent = objectiveBudgetProposalRepository.findByForObjectiveAndOwnerAndBudgetType(o, workAt, budgetType);
 			if(obpParent == null) {
 				obpParent= new ObjectiveBudgetProposal();
 				obpParent.setForObjective(o);
 				obpParent.setBudgetType(budgetType);
 				obpParent.setOwner(workAt);
 			}
 			
 			// now we set the obp to this one!
 			obpParent.adjustAmount(obp, obpOldValue);
 			
 			objectiveBudgetProposalRepository.save(obpParent);
 			
 			for(ObjectiveBudgetProposalTarget tt: obpParent.getTargets()) {
 				logger.debug("-------------->>>>>>>>>>>> " + tt.getTargetValue());
 			}
 			
 			o = o.getParent();
 		}
 		
 		obp.getForObjective().getTargets().size();
 		
 		return obp;
 	}
 
 	@Override
 	public ObjectiveBudgetProposal deleteObjectiveBudgetProposal(Long id) {
 		
 		ObjectiveBudgetProposal obp = objectiveBudgetProposalRepository.findOne(id);
 		if(obp == null) {
 			return null;
 		}
 
 		obp.getForObjective().getTargets().size();
 		
 		//now before return back we'll update the parents
 		Objective o = obp.getForObjective().getParent();
 		BudgetType budgetType = obp.getBudgetType();
 		Organization workAt = obp.getOwner();
 		ObjectiveBudgetProposal zeroObp = new ObjectiveBudgetProposal();
 		zeroObp.copyValue(obp);
 		// now reset all to zero
 		zeroObp.resetToZeroValue();
 		
 		while(o != null) {
 			ObjectiveBudgetProposal obpParent = objectiveBudgetProposalRepository.findByForObjectiveAndOwnerAndBudgetType(o, workAt, budgetType);
 			if(obpParent == null) {
 				obpParent= new ObjectiveBudgetProposal();
 				obpParent.setForObjective(o);
 				obpParent.setBudgetType(budgetType);
 				obpParent.setOwner(workAt);
 			}
 			
 			// now we set the obp to this one!
 			obpParent.adjustAmount(zeroObp, obp);
 			
 			objectiveBudgetProposalRepository.save(obpParent);
 			
 			o = o.getParent();
 		}
 		
 		objectiveBudgetProposalRepository.delete(obp);
 		
 		return obp;
 		
 	}
 
 	@Override
 	public String findObjectiveTypeChildrenNameOf(Long id) {
 		ObjectiveType t = objectiveTypeRepository.findOne(id);
 		if(t.getChildren() != null && t.getChildren().size() > 0 ) {
 			return t.getChildren().iterator().next().getName();
 		} else {
 			return "";
 		}
 		
 	}
 
 	@Override
 	public String findObjectiveChildrenTypeName(Long id) {
 		Objective o = objectiveRepository.findOne(id);
 		if(o.getType() != null ) {
 			if(o.getType().getChildren() != null && o.getType().getChildren().size() > 0 ) {
 				return o.getType().getChildren().iterator().next().getName();
 			}
 		}
 		return null;
 	}
 
 	
 	
 	@Override
 	public ObjectiveName saveObjectiveName(JsonNode node) {
 		ObjectiveName on = new ObjectiveName();
 		
 		if(node.get("name") != null) {
 			on.setName(node.get("name").asText());
 		}
 		
 		if(node.get("type") != null) {
 			ObjectiveType type = objectiveTypeRepository.findOne(
 					node.get("type").get("id").asLong());
 			on.setType(type);
 		}
 		
 		if(node.get("fiscalYear") != null ) {
 			on.setFiscalYear(node.get("fiscalYear").asInt());
 		}
 		
 		// now find the maximum number in this type
 	
 		String maxCode = objectiveNameRepository.findMaxCodeOfTypeAndFiscalYear(
 				on.getType(), on.getFiscalYear());
 		
 		Integer max;
 		if(maxCode == null) {
 			max = 0;
 		} else {
 			max = Integer.parseInt(maxCode);
 		}
 		
 		Integer nextCode = max + 1;
 		logger.debug("nextCode:" + nextCode);
 		on.setCode(String.format("%0" + on.getType().getCodeLength() + "d", nextCode));
 	
 		
 		objectiveNameRepository.save(on);
 		return on;
 	}
 
 	@Override
 	public ObjectiveName updateObjectiveName(JsonNode node) {
 		ObjectiveName on = objectiveNameRepository.findOne(node.get("id").asLong());
 		
 		if(node.get("name") != null) {
 			on.setName(node.get("name").asText());
 		}
 		
 		if(node.get("type") != null) {
 			ObjectiveType type = objectiveTypeRepository.findOne(
 					node.get("type").get("id").asLong());
 			on.setType(type);
 		}
 		
 		if(node.get("fiscalYear") != null ) {
 			on.setFiscalYear(node.get("fiscalYear").asInt());
 		}
 		
 		objectiveNameRepository.save(on);
 		return on;
 	}
 
 	@Override
 	public Page<ObjectiveName> findAllObjectiveNameByFiscalYearAndTypeId(
 			Integer fiscalYear, Long typeId, PageRequest pageRequest) {
 		Page<ObjectiveName> page =  objectiveNameRepository.findAllObjectiveNameByFiscalYearAndTypeId(fiscalYear, typeId, pageRequest);
 		
 		for(ObjectiveName n : page) {
 			n.getType().getId();
 			n.getTargets().size();
 		}
 		
 		return page;
 	}
 
 	
 	
 	@Override
 	public Page<ObjectiveName> findAllObjectiveNameByFiscalYearAndTypeIdWithQuery(
 			Integer fiscalYear, Long typeId, String query,
 			PageRequest pageRequest) {
 		Page<ObjectiveName> page =  objectiveNameRepository
 				.findAllObjectiveNameByFiscalYearAndTypeIdWithQuery(fiscalYear, typeId, "%"+query+"%", pageRequest);
 		
 		for(ObjectiveName n : page) {
 			n.getType().getId();
 			n.getTargets().size();
 		}
 		
 		return page;
 	}
 
 	@Override
 	public ObjectiveName findOneObjectiveName(Long id) {
 		return objectiveNameRepository.findOne(id);
 	}
 
 	@Override
 	public ObjectiveName deleteObjectiveName(Long id) {
 		ObjectiveName on = objectiveNameRepository.findOne(id);
 		if(on!=null) {
 			List<Objective> oList = objectiveRepository.findAllByObjectiveName(on);
 			
 			// we must delete all Objective before 
 			for(Objective o : oList) {
 				// now delete all relation that have o
 				
 				
 				objectiveRepository.delete(o);
 			}
 			
 			
 			objectiveNameRepository.delete(on);
 		}
 		return on;
 	}
 
 	@Override
 	public List<ObjectiveName> findAvailableObjectiveNameChildrenByObejective(Long id, String searchQuery) {
 		Objective objective = objectiveRepository.findOne(id);
 		
 		logger.debug(searchQuery);
 		
 		if(searchQuery == null || searchQuery.length() == 0) {
 		
 			return objectiveNameRepository.findAllChildrenTypeObjectiveNameByFiscalYearAndTypeId(objective.getFiscalYear(), objective.getType().getId()) ;
 		} else {
 			return objectiveNameRepository.findAllChildrenTypeObjectiveNameByFiscalYearAndTypeId(objective.getFiscalYear(), objective.getType().getId(), "%"+searchQuery+"%") ;
 		}
 	}
 
 	@Override
 	public Objective objectiveAddChildObjectiveName(Long parentId, Long nameId) {
 		ObjectiveName oName = objectiveNameRepository.findOne(nameId);
 		
 		Objective o = new Objective();
 		o.setName(oName.getName());
 		o.setCode(oName.getCode());
 		o.setFiscalYear(oName.getFiscalYear());
 		o.setType(oName.getType());
 		
 		o.getType().getName();
 		
 		o.setIndex(Integer.parseInt(oName.getCode()));
 		o.setIsLeaf(true);
 		o.setObjectiveName(oName);
 		
 		Objective parent = objectiveRepository.findOne(parentId);
 		parent.getType().getName();
 		
 		o.setParent(parent);
 		o.setParentLevel(parent.getParentLevel()+1);
 		o.setParentPath("." + parentId + parent.getParentPath());
 		
 		// now set the target
 		for(ObjectiveTarget ot : oName.getTargets()) {
 			o.setTargets(new ArrayList<ObjectiveTarget>());
 			o.getTargets().add(ot);
 		}
 		
 		// now save O
 		objectiveRepository.save(o);
 		
 		return o;
 	}
 
 	@Override
 	public BudgetSignOff findBudgetSignOffByFiscalYearAndOrganization(
 			Integer fiscalYear, Organization workAt) {
 		BudgetSignOff budgetSignOff = budgetSignOffRepository.findOneByFiscalYearAndOwner(fiscalYear, workAt);
 		
 		if(budgetSignOff == null) {
 			budgetSignOff = new BudgetSignOff();
 			budgetSignOff.setFiscalYear(fiscalYear);
 			budgetSignOff.setOwner(workAt);
 			budgetSignOffRepository.save(budgetSignOff);
 		}
 		
 		return budgetSignOff;
 	}
 
 	@Override
 	public Long findSumTotalBudgetProposalOfOwner(Integer fiscalYear,
 			Organization workAt) {
 		return budgetProposalRepository.findSumTotalOfOwner(fiscalYear,workAt);
 	}
 
 	@Override
 	public Long findSumTotalObjectiveBudgetProposalOfOwner(Integer fiscalYear,
 			Organization workAt) {
 		return objectiveBudgetProposalRepository.findSumTotalOfOwner(fiscalYear,workAt);
 	}
 
 	@Override
 	public BudgetSignOff updateBudgetSignOff(Integer fiscalYear,ThaicomUserDetail currentUser,
 			String command) {
 		
 		
 		BudgetSignOff bso = findBudgetSignOffByFiscalYearAndOrganization(fiscalYear, currentUser.getWorkAt());
 		if(command.equals("lock1")) {
 			bso.setLock1Person(currentUser.getPerson());
 			bso.setLock1TimeStamp(new Date());
 			
 			bso.setUnLock1Person(null);
 			bso.setUnLock1TimeStamp(null);
 		} else if(command.equals("lock2")) {
 			bso.setLock2Person(currentUser.getPerson());
 			bso.setLock2TimeStamp(new Date());
 			
 			bso.setUnLock2Person(null);
 			bso.setUnLock2TimeStamp(null);
 			
 		} else if(command.equals("unLock1")) {
 			bso.setUnLock1Person(currentUser.getPerson());
 			bso.setUnLock1TimeStamp(new Date());
 			
 			bso.setLock1Person(null);
 			bso.setLock1TimeStamp(null);
 			
 		} else if(command.equals("unLock2")) {
 			bso.setUnLock2Person(currentUser.getPerson());
 			bso.setUnLock2TimeStamp(new Date());
 			
 			bso.setLock2Person(null);
 			bso.setLock2TimeStamp(null);
 			
 		}
 		
 		
 		budgetSignOffRepository.save(bso);
 		
 		return bso;
 	}
 	
 	public List<List<Objective>> findObjectivesByFiscalyearAndTypeIdAndInitBudgetProposal(
 			Integer fiscalYear, long typeId, Organization workAt) {
 		Objective root = objectiveRepository.findRootOfFiscalYear(fiscalYear);
 		List<Objective> allList = new ArrayList<Objective>();
 		
 		
 		allList = findFlatChildrenObjectivewithBudgetProposal(
 					fiscalYear, workAt.getId(), root.getId());
 		
 		
 		List<List<Objective>> returnList = new ArrayList<List<Objective>>();
 		returnList.add(allList);
 		return returnList;
 	}
 
 	@Override
 	public List<List<Objective>> findObjectivesByFiscalyearAndTypeIdAndInitObjectiveBudgetProposal(
 			Integer fiscalYear, long typeId, Organization workAt) {
 		Objective root = objectiveRepository.findRootOfFiscalYear(fiscalYear);
 		List<Objective> allList = new ArrayList<Objective>();
 		
 		
 		allList = findFlatChildrenObjectivewithObjectiveBudgetProposal(
 					fiscalYear, workAt.getId(), root.getId());
 		
 		
 		List<List<Objective>> returnList = new ArrayList<List<Objective>>();
 		returnList.add(allList);
 		return returnList;
 	}
 
 	@Override
 	public ObjectiveDetail findOneObjectiveDetail(Long id) {
 		ObjectiveDetail detail = objectiveDetailRepository.findOne(id);
 		detail.getForObjective().getId();
 		return detail;
 	}
 
 	@Override
 	public ObjectiveDetail updateObjectiveDetail(JsonNode node, Organization owner) {
 		return saveObjectiveDetail(node, owner);
 	}
 
 	@Override
 	public ObjectiveDetail saveObjectiveDetail(JsonNode node, Organization owner) {
 		ObjectiveDetail detail;
 		if(getJsonNodeId(node) != null) {
 			detail = objectiveDetailRepository.findOne(getJsonNodeId(node));
 		} else {
 			detail = new ObjectiveDetail();
 		}
 		
 		// now will do the dull mapping 
 		
 		if(getJsonNodeId(node.get("forObjective")) != null) {
 			Objective forObjective = objectiveRepository.findOne(getJsonNodeId(node.get("forObjective")));
 			detail.setForObjective(forObjective);
 		}
 		
 		detail.updateField("officerInCharge", node.get("officerInCharge"));
 		detail.updateField("phoneNumber", node.get("phoneNumber"));
 		detail.updateField("email", node.get("email"));
 		
 		detail.updateField("reason", node.get("reason"));
 		detail.updateField("projectObjective", node.get("projectObjective"));
 		
 		detail.updateField("methodology1", node.get("methodology1"));
 		detail.updateField("methodology2", node.get("methodology2"));
 		detail.updateField("methodology3", node.get("methodology3"));
 		
 		detail.updateField("location", node.get("location"));
 		detail.updateField("timeframe", node.get("timeframe"));
 		detail.updateField("targetDescription", node.get("targetDescription"));
 		
 		detail.updateField("outcome", node.get("outcome"));
 		
 		detail.updateField("output", node.get("output"));
 		
 		detail.updateField("targetArea", node.get("targetArea"));
 		
 		/**
 		if(node.get("officerInCharge")!=null) detail.setOfficerInCharge(node.get("officerInCharge").asText());
 		if(node.get("phoneNumber") != null) detail.setPhoneNumber(node.get("phoneNumber").asText());
 		if(node.get("email") != null) detail.setEmail(node.get("email").asText());
 		
 		if(node.get("reason") != null) detail.setReason(node.get("reason").asText());
 		if(node.get("projectObjective") != null) detail.setProjectObjective(node.get("projectObjective").asText());
 		
 		if(node.get("methodology1") != null) detail.setMethodology1(node.get("methodology1").asText());
 		if(node.get("methodology2") != null) detail.setMethodology1(node.get("methodology2").asText());
 		if(node.get("methodology3") != null) detail.setMethodology1(node.get("methodology3").asText());
 		
 		if(node.get("location") != null) detail.setLocation(node.get("location").asText());
 		if(node.get("timeframe") != null) detail.setTimeframe(node.get("timeframe").asText());
 		if(node.get("targetDescription") != null) detail.setTargetDescription(node.get("targetDescription").asText());
 		
 		if(node.get("outcome") != null) detail.setOutcome(node.get("outcome").asText());
 		
 		if(node.get("output") != null) detail.setOutput(node.get("output").asText());
 		
 		if(node.get("targetArea") != null) detail.setTargetArea(node.get("targetArea").asText());
 		**/
 		
 		
 		// lastly 
 		detail.setOwner(owner);
 		
 		
 		objectiveDetailRepository.save(detail);
 		
 		
 		return detail;
 	}
 
 	@Override
 	public ObjectiveDetail deleteObjectiveDetail(Long id) {
 		ObjectiveDetail detail = objectiveDetailRepository.findOne(id);
 		if(detail != null ) {
 			objectiveDetailRepository.delete(detail);
 		}
 		return detail;
 	}
 
 	@Override
 	public ObjectiveDetail findOneObjectiveDetailByObjectiveIdAndOwner(Long objectiveId,
 			ThaicomUserDetail currentUser) {
 		return objectiveDetailRepository.findByForObjective_IdAndOwner(objectiveId, currentUser.getWorkAt());
 	}
 
 	@Override
 	public Page<User> findUser(PageRequest pageRequest) {
 		return userRepository.findAll(pageRequest);
 	}
 
 	@Override
 	public User findOneUser(Long id) {
 		return userRepository.findOne(id);
 	}
 
 	@Override
 	public User updateUser(JsonNode node) {
 		Long id = getJsonNodeId(node);
 		if(id == null) {
 			return null;
 		}
 		User user = userRepository.findOne(id);
 		
 		user.setPassword(node.get("password").asText());
 		user.getPerson().setFirstName(node.get("person").get("firstName").asText());
 		user.getPerson().setLastName(node.get("person").get("lastName").asText());
 		
 		Long workAtId = getJsonNodeId(node.get("person").get("workAt"));
 		
 		user.getPerson().setWorkAt(organizationRepository.findOne(workAtId));
 		
 		userRepository.save(user);
 		
 		
 		return user;
 	}
 
 	@Override
 	public User saveUser(JsonNode node) {
 		
 		User user = new User();
 		user.setPassword(node.get("password").asText());
 		user.setUsername(node.get("username").asText());
 		
 		
 		user.setPerson(new Person());
 		
 		user.getPerson().setFirstName(node.get("person").get("firstName").asText());
 		user.getPerson().setLastName(node.get("person").get("lastName").asText());
 		
 		Long workAtId = getJsonNodeId(node.get("person").get("workAt"));
 		
 		user.getPerson().setWorkAt(organizationRepository.findOne(workAtId));
 		
 		userRepository.save(user);
 		
 		
 		return user;
 	}
 
 	@Override
 	public User deleteUser(Long id) {
 		
 		User user = userRepository.findOne(id);
 		
 		userRepository.delete(user);
 		
 		return user;
 	}
 
 	@Override
 	public void copyFromProposalToObjectiveProposal(Integer fiscalYear, Organization workAt) {
 		Objective root = findOneRootObjectiveByFiscalyear(fiscalYear);
 		
 		List<BudgetType> budgetMainType = fiscalBudgetTypeRepository.findAllMainBudgetTypeByFiscalYear(fiscalYear);
 		
 		for(Objective obj : root.getChildren()) {
 			copyFromProposalToObjectiveProposal(obj.getChildren(), workAt, budgetMainType);
 			copyFromProposalToObjectiveProposal(obj, workAt, budgetMainType);
 		}
 		
  	}
 
 	private void copyFromProposalToObjectiveProposal(List<Objective> children,
 			Organization workAt, List<BudgetType> budgetMainType) {
 		if(children == null || children.size() == 0) {
 			return;
 		}
 		for(Objective obj : children) {
 			copyFromProposalToObjectiveProposal(obj.getChildren(), workAt, budgetMainType);
 			copyFromProposalToObjectiveProposal(obj, workAt, budgetMainType);
 		}
 	
 	}
 
 	private void copyFromProposalToObjectiveProposal(Objective obj,
 			Organization workAt, List<BudgetType> budgetMainType) {
 		// 
 		List<BudgetProposal> proposals = budgetProposalRepository.findAllByForObjectiveAndOwner(obj, workAt);
 		
 		List<ObjectiveBudgetProposal> objectiveProposals = new ArrayList<ObjectiveBudgetProposal>();
 		
 		for(BudgetProposal proposal : proposals) {
 			BudgetType mainType = null;
 			Boolean found = false;
 			
 			for(BudgetType type : budgetMainType) {
 				if(proposal.getBudgetType().getParentPath().contains("." + type.getId() +".") ) {
 					mainType = type;
 					break;
 				}
 			}
 			
 			// now find mianType in 
 			for(ObjectiveBudgetProposal budgetProposal : objectiveProposals) {
 				if(budgetProposal.getBudgetType().getId().equals(mainType.getId())) {
 					found = true;
 					budgetProposal.setAmountRequest(nullZero(proposal.getAmountRequest()) + nullZero(budgetProposal.getAmountRequest()));
 					budgetProposal.setAmountRequestNext1Year(nullZero(proposal.getAmountRequestNext1Year()) + nullZero(budgetProposal.getAmountRequestNext1Year()));
 					budgetProposal.setAmountRequestNext2Year(nullZero(proposal.getAmountRequestNext2Year()) + nullZero(budgetProposal.getAmountRequestNext2Year()));
 					budgetProposal.setAmountRequestNext3Year(nullZero(proposal.getAmountRequestNext3Year()) + nullZero(budgetProposal.getAmountRequestNext3Year()));
 					
 					for(ProposalStrategy strategy : proposal.getProposalStrategies()) {
 						for(ObjectiveTarget target: obj.getTargets()) {
 							if(target.getUnit().getId().equals(strategy.getTargetUnit().getId())) {
 								Boolean foundTarget = false;
 								if(budgetProposal.getTargets() != null) {
 									for(ObjectiveBudgetProposalTarget pTarget : budgetProposal.getTargets()) {
 										if(pTarget.getUnit().getId().equals(strategy.getTargetUnit().getId())) {
 											foundTarget  = true;
 											pTarget.setTargetValue(nullZero(pTarget.getTargetValue()) + nullZero(strategy.getTargetValue()));
 											pTarget.setTargetValueNext1Year(nullZero(pTarget.getTargetValueNext1Year()) + nullZero(strategy.getTargetValueNext1Year()));
 											pTarget.setTargetValueNext2Year(nullZero(pTarget.getTargetValueNext2Year()) + nullZero(strategy.getTargetValueNext2Year()));
 											pTarget.setTargetValueNext3Year(nullZero(pTarget.getTargetValueNext3Year()) + nullZero(strategy.getTargetValueNext3Year()));
 											break;
 										}
 									}
 								}
 								
 								if(!foundTarget) {
 									ObjectiveBudgetProposalTarget pTarget = new ObjectiveBudgetProposalTarget();
 									pTarget.setTargetValue(strategy.getTargetValue());
 									pTarget.setTargetValueNext1Year(strategy.getTargetValueNext1Year());
 									pTarget.setTargetValueNext2Year(strategy.getTargetValueNext2Year());
 									pTarget.setTargetValueNext3Year(strategy.getTargetValueNext3Year());
 									pTarget.setObjectiveBudgetProposal(budgetProposal);
 									pTarget.setUnit(strategy.getTargetUnit());
 									
 									budgetProposal.setTargets(new ArrayList<ObjectiveBudgetProposalTarget>());
 									budgetProposal.getTargets().add(pTarget);
 								}
 							}
 						}
 					}
 					
 					break;
 				}
 			}
 			
 			if(!found) {
 				ObjectiveBudgetProposal objectiveBudgetProposal = new ObjectiveBudgetProposal();
 				objectiveBudgetProposal.setOwner(workAt);
 				objectiveBudgetProposal.setForObjective(obj);
 				objectiveBudgetProposal.setAmountRequest(proposal.getAmountRequest());
 				objectiveBudgetProposal.setAmountRequestNext1Year(proposal.getAmountRequestNext1Year());
 				objectiveBudgetProposal.setAmountRequestNext2Year(proposal.getAmountRequestNext2Year());
 				objectiveBudgetProposal.setAmountRequestNext3Year(proposal.getAmountRequestNext3Year());
 				objectiveBudgetProposal.setBudgetType(mainType);
 				
 				
 				for(ProposalStrategy strategy : proposal.getProposalStrategies()) {
 					for(ObjectiveTarget target: obj.getTargets()) {
 						if(target.getUnit() != null && strategy.getTargetUnit() != null && 
 								target.getUnit().getId().equals(strategy.getTargetUnit().getId())) {
 							Boolean foundTarget = false;
 							if(objectiveBudgetProposal.getTargets() != null) {
 								for(ObjectiveBudgetProposalTarget pTarget : objectiveBudgetProposal.getTargets()) {
 									if(pTarget.getUnit().getId().equals(strategy.getTargetUnit().getId())) {
 										foundTarget  = true;
 										pTarget.setTargetValue(nullZero(pTarget.getTargetValue()) + nullZero(strategy.getTargetValue()));
 										pTarget.setTargetValueNext1Year(nullZero(pTarget.getTargetValueNext1Year()) + nullZero(strategy.getTargetValueNext1Year()));
 										pTarget.setTargetValueNext2Year(nullZero(pTarget.getTargetValueNext2Year()) + nullZero(strategy.getTargetValueNext2Year()));
 										pTarget.setTargetValueNext3Year(nullZero(pTarget.getTargetValueNext3Year()) + nullZero(strategy.getTargetValueNext3Year()));
 										break;
 									}
 								}
 							}
 							
 							if(!foundTarget) {
 								ObjectiveBudgetProposalTarget pTarget = new ObjectiveBudgetProposalTarget();
 								pTarget.setTargetValue(strategy.getTargetValue());
 								pTarget.setTargetValueNext1Year(strategy.getTargetValueNext1Year());
 								pTarget.setTargetValueNext2Year(strategy.getTargetValueNext2Year());
 								pTarget.setTargetValueNext3Year(strategy.getTargetValueNext3Year());
 								pTarget.setObjectiveBudgetProposal(objectiveBudgetProposal);
 								pTarget.setUnit(strategy.getTargetUnit());
 								
 								objectiveBudgetProposal.setTargets(new ArrayList<ObjectiveBudgetProposalTarget>());
 								objectiveBudgetProposal.getTargets().add(pTarget);
 								
 								
 							}
 						}
 					}
 				}
 				objectiveProposals.add(objectiveBudgetProposal);
 			}
 		}
 		
 		//delete the old one
 		objectiveBudgetProposalRepository.delete(objectiveBudgetProposalRepository.findAllByForObjective_IdAndOwner_Id(obj.getId(), workAt.getId()));
 		
 		// now save the ObjectiveProposalarget
 		objectiveBudgetProposalRepository.save(objectiveProposals);
 	}
 
 	private Long nullZero(Long value) {
 		if(value == null ) {
 			return 0L;
 		} else {
 			return value;
 		}
 	}
 
 	@Override
 	public FormulaStrategy findFormulaStrategy(Long id) {
 		FormulaStrategy fs = formulaStrategyRepository.findOne(id);
 		fs.getAllocationStandardPriceMap().size();
 		
 		for(FormulaColumn fc: fs.getFormulaColumns()) {
 			fc.getAllocatedFormulaColumnValueMap().size();
 		}
 		
 		return fs;
 	}
 
 	@Override
 	public AllocationRecord findAllocationRecordById(Long id) {
 		AllocationRecord ar = allocationRecordRepository.findOne(id);
 		
 		ar.getAllocationRecordStrategies().size();
 		for(AllocationRecordStrategy ars : ar.getAllocationRecordStrategies()) {
 			ars.getRequestColumns().size();
 			if(ars.getStrategy()!= null && ars.getStrategy().getAllocationStandardPriceMap()!=null) {
 				ars.getStrategy().getAllocationStandardPriceMap().size();
 				for(FormulaColumn fc: ars.getStrategy().getFormulaColumns()) {
 					fc.getAllocatedFormulaColumnValueMap().size();
 				}
 			}
 			
 		}
 		
 		return ar;
 	}
 	
 	
 	
  
 }
