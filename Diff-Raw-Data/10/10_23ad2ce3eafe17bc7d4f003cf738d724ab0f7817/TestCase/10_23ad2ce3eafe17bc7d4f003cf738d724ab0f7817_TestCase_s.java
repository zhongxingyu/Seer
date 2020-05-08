 package net.alpha01.jwtest.beans;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 import net.alpha01.jwtest.dao.RequirementMapper;
 import net.alpha01.jwtest.dao.SqlConnection;
 import net.alpha01.jwtest.dao.SqlSessionMapper;
 
 public class TestCase extends IdBean implements Serializable {
 	private static final long serialVersionUID = -4393350179491773029L;
 	private BigInteger id_requirement;
 	private BigInteger new_version;
 	private String name;
 	private String description;
 	private String expected_result;
 	private BigInteger success;
 	private BigInteger nresults;
 	@SuppressWarnings("unused")
 	private BigDecimal percSuccess;
 	private Requirement requirement;
 
 	public TestCase() {
 		this.new_version=BigInteger.ZERO;
 	}
 
 	public TestCase(BigInteger id, BigInteger id_requirement, String name, String description) {
 		super();
 		setId(id);
 		this.new_version=BigInteger.ZERO;
 		this.id_requirement = id_requirement;
 		this.name = name;
 		this.description = description;
 	}
 	
 	public BigInteger getNew_version() {
 		return new_version;
 	}
 
 	public void setNew_version(BigInteger id_parent) {
 		this.new_version = id_parent;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public void setId_requirement(BigInteger id_requirement) {
 		this.id_requirement = id_requirement;
 	}
 
 	public BigInteger getId_requirement() {
 		return id_requirement;
 	}
 
 	public BigInteger getSuccess() {
 		return success;
 	}
 
 	public void setSuccess(BigInteger success) {
 		this.success = success;
 	}
 
 	public BigInteger getNresults() {
 		return nresults;
 	}
 
 	public void setNresults(BigInteger nresults) {
 		this.nresults = nresults;
 	}
 
 	public BigDecimal getPercSuccess() {
 		if (getNresults().equals(BigInteger.ZERO)) {
 			return BigDecimal.valueOf(-1);
 		} else {
 			return BigDecimal.valueOf(100 * ((float) getSuccess().intValue() / getNresults().intValue()));
 		}
 	}
 
 	public void setPercSuccess(BigDecimal percSuccess) {
 		this.percSuccess = percSuccess;
 	}
 
 	@Override
 	public String toString() {
 		return getName();
 	}
 
 	public Requirement getRequirement() {
 		if (requirement == null) {
 			SqlSessionMapper<RequirementMapper> sesMapper = SqlConnection.getSessionMapper(RequirementMapper.class);
 			requirement = sesMapper.getMapper().get(getId_requirement());
 			sesMapper.close();
 		}
 		return requirement;
 	}
 
 	public String getExpected_result() {
 		return expected_result;
 	}
 
 	public void setExpected_result(String expected_result) {
 		this.expected_result = expected_result;
 	}
 
 }
