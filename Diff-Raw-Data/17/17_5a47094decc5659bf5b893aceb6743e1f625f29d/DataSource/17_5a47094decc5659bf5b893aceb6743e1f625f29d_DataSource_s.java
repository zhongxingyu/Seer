 package com.conx.logistics.kernel.datasource.domain;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 import com.conx.logistics.kernel.metamodel.domain.EntityType;
 import com.conx.logistics.mdm.domain.MultitenantBaseEntity;
 
 @Entity
 @Table(name="sysdsdatasource")
 @Inheritance( strategy = InheritanceType.SINGLE_TABLE )
 public class DataSource extends MultitenantBaseEntity {
 	@ManyToOne
 	private EntityType entityType;
 	
 	@OneToMany(mappedBy="dataSource")
 	private Set<DataSourceField> dSfields = new HashSet<DataSourceField>();
 
 	public EntityType getEntityType() {
 		return entityType;
 	}
 
 	public void setEntityType(EntityType entityType) {
 		this.entityType = entityType;
 	}
 
 	public Set<DataSourceField> getDSFields() {
 		return dSfields;
 	}
 	
 	public DataSource() {
 	}
 
 	public void setDSFields(Set<DataSourceField> dSfields) {
 		this.dSfields = dSfields;
 	}
 
 	public DataSource(String code, EntityType entityType) {
 		super();
 		setCode(code);
 		this.entityType = entityType;
 	}
 	
 	public String toString()
 	{
 		int indent = 0;
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append(indent(indent)+"{\n");
 		
 		sb.append(indent(indent+1)+"name: "+getName());
 		sb.append(indent(indent+1)+"type: "+getEntityType().getJpaEntityName());
 		
 		sb.append(indent(indent+1)+"fields : [\n");
 		
 		for (DataSourceField dsf : getDSFields())
 		{
 			sb.append(indent(indent+2)+dsf.toString()+";");
 		}
 		
 		sb.append(indent(indent+1)+"]");
 		
 		sb.append(indent(indent)+"}\n");
 		
 		return sb.toString();
 	}
 
 	static String indent(int indent) {
 		String res = "";
 		for (int i = 0; i<indent; i++)
 			res += res+"\t";
 		
 		return res;
 	}
 }
