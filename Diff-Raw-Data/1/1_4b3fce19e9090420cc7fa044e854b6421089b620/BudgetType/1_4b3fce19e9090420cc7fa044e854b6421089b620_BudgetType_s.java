 package biz.thaicom.eBudgeting.models.bgt;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderColumn;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import biz.thaicom.eBudgeting.models.pln.TargetUnit;
 import com.fasterxml.jackson.annotation.JsonCreator;
 import com.fasterxml.jackson.annotation.JsonIdentityInfo;
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.fasterxml.jackson.annotation.ObjectIdGenerators;
 
 @Entity
 @Table(name="BGT_BUDGETTYPE")
 @SequenceGenerator(name="BGT_BUDGETTYPE_SEQ", sequenceName="BGT_BUDGETTYPE_SEQ", allocationSize=1)
 @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class BudgetType implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5984004367106256395L;
 	
 	private static final Logger logger = LoggerFactory.getLogger(BudgetType.class);
 	
 	// Field
 	@Id
 	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="BGT_BUDGETTYPE_SEQ")
 	private Long id;
 	
 	@Basic
 	private String name;
 	
 	@Basic
 	private Integer code;
 	
 	@Basic
 	private String parentPath;
 	
 	@Basic 
 	private Integer parentLevel;
 	
 	@Basic
 	private Integer lineNumber;
 	
 	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name="PARENT_BGT_BUDGETTYPE_ID")
 	private BudgetType parent;
 	
 	@OneToMany(mappedBy="parent", fetch=FetchType.LAZY)
 	@OrderColumn(name="IDX")
 	private List<BudgetType> children;
 	
 	@Basic
 	private Integer fiscalYear;
 	
 	@Transient
 	private Integer currentFiscalYear;
 	
 	@Basic
 	@Column(name="IDX")
 	private Integer index;
 	
 	@Transient
 	private List<FormulaStrategy> strategies;
 	
 	@Transient
 	private FormulaStrategy standardStrategy;
 	
 	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name="BGT_BUDGETLEVEL_ID")
 	private BudgetLevel level;
 
 	@ManyToOne
 	@JoinColumn(name="COMMONTYPE_BGT_ID")
 	private BudgetCommonType commonType;
 	
 	@ManyToOne
 	@JoinColumn(name="PLN_UNIT_ID")
 	private TargetUnit unit;
 
 	public BudgetType() {
 		
 	}
 	
 	@JsonCreator
 	public BudgetType(@JsonProperty("id") Long id) {
 		this.id=id;
 	}
 	
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Integer getCode() {
 		return code;
 	}
 
 	public void setCode(Integer code) {
 		this.code = code;
 	}
 
 	public BudgetType getParent() {
 		return parent;
 	}
 
 	public void setParent(BudgetType parent) {
 		this.parent = parent;
 	}
 
 	public List<BudgetType> getChildren() {
 		return children;
 	}
 
 	public void setChildren(List<BudgetType> children) {
 		this.children = children;
 	}
 
 	public Integer getFiscalYear() {
 		return fiscalYear;
 	}
 
 	public void setFiscalYear(Integer fiscalYear) {
 		this.fiscalYear = fiscalYear;
 	}
 	
 	@Transient
 	public Integer getCurrentFiscalYear() {
 		return currentFiscalYear;
 	}
 
 	public void setCurrentFiscalYear(Integer currentFiscalYear) {
 		this.currentFiscalYear = currentFiscalYear;
 	}
 
 	public Integer getIndex() {
 		return index;
 	}
 
 	public void setIndex(Integer index) {
 		this.index = index;
 	}
 	public String getParentPath() {
 		return parentPath;
 	}
 
 	public void setParentPath(String parentPath) {
 		this.parentPath = parentPath;
 	}
 
 	@Transient
 	public List<FormulaStrategy> getStrategies() {
 		return strategies;
 	}
 
 	public void setStrategies(List<FormulaStrategy> strategies) {
 		this.strategies = strategies;
 	}
 	
 	@Transient
 	public FormulaStrategy getStandardStrategy() {
 		return standardStrategy;
 	}
 
 	public void setStandardStrategy(FormulaStrategy standardStrategy) {
 		this.standardStrategy = standardStrategy;
 	}
 
 	public Integer getParentLevel() {
 		return parentLevel;
 	}
 
 	public void setParentLevel(Integer parentLevel) {
 		this.parentLevel = parentLevel;
 	}
 	
 	public Integer getLineNumber() {
 		return lineNumber;
 	}
 
 	public void setLineNumber(Integer lineNumber) {
 		this.lineNumber = lineNumber;
 	}
 	
 	public BudgetLevel getLevel() {
 		return level;
 	}
 
 	public void setLevel(BudgetLevel level) {
 		this.level = level;
 	}
 
 	public BudgetCommonType getCommonType() {
 		return commonType;
 	}
 
 	public void setCommonType(BudgetCommonType commonType) {
 		this.commonType = commonType;
 	}
 	
 	public TargetUnit getUnit() {
 		return unit;
 	}
 
 	public void setUnit(TargetUnit unit) {
 		this.unit = unit;
 	}
 
 	public void doBasicLazyLoad() {
 		//now we get one parent and its type
 		if(this.getParent() != null) {
 			this.getParent().getId();
 		} 
 		
 		logger.debug("id, name: " +this.getId() + ", " + this.getName());
 		
 		if(this.getChildren() != null) {
 			// we have to go deeper one level
 			for(BudgetType child : this.getChildren()){
 				if(child.getLevel() != null) {
 					child.getLevel().getId();
 					if(child.getUnit() != null) {
 						child.getUnit().getId();
 					}
 				}
 				
 			}
 			
 		}
 		
 		if(this.getUnit() != null) {
 			this.getUnit().getId();
 		}
 	}
 	
 
 	public void doEagerLoad() {
 		if(this.getParent() != null)
 			this.getParent().getId();
 		
 		if(this.children != null && this.children.size() > 0) {
 			for(BudgetType b : this.children) {
 				b.doEagerLoad();
 			}
 		}
 		
 	}
 
 	public void doLoadParent() {
 		if(this.getParent() != null) {
 			this.getParent().doLoadParent();
 		}
 	}	
 
 	public List<Long> getParentIds() {
 		// OK will have to travesre back up .. we can get the parent path
 		String parentPath = this.parentPath;
 		if(this.parentPath == null) {
 			return null;
 		}
 		
 		//we will tokenize and put it in List<Long>
 		List<Long> parentIds = new ArrayList<Long>();
 		
 		StringTokenizer tokens = new StringTokenizer(parentPath, ".");
 		
 		while(tokens.hasMoreTokens()) {
 			String token = tokens.nextToken();
 			//convert to Long
 			Long parentId = Long.parseLong(token);
 			
 			parentIds.add(parentId);
 		}
 		Collections.reverse(parentIds);
 		
 		return parentIds;
 	}
 	
 	
 	public String getTopParentName() {
 		if(this.parentPath == null) {
 			return "";
 		}
 		Pattern p = Pattern.compile(".*\\.([0-9]+)\\.0\\.");
 		Matcher m = p.matcher(this.parentPath);
 		if (m.find()) {
 			String topParentId = m.group(1);
 			switch (Integer.parseInt(topParentId)) {
 			case 1:
 				return "งบบุคลากร";
 			case 47:
 				return "งบดำเนินงาน";
 			case 118:
 				return "งบลงทุน";
 			case 779:
 				return "งบอุดหนุน";
 			case 785:
 				return "งบรายจ่ายอื่น";
 			default:
 				return "unknown";
 			}
 		}
 		return "";
 	}
 	
 }
