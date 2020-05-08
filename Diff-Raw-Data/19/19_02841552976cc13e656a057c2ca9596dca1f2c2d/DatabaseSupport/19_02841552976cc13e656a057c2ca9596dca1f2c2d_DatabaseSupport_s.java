 package at.bestsolution.persistence.emap.generator;
 
 import org.eclipse.emf.ecore.EDataType;
 
 import at.bestsolution.persistence.emap.eMap.EAttribute;
 
 public abstract class DatabaseSupport {
 	public abstract String getDatabaseId();
 	public abstract boolean supportsGeneratedKeys();
 	public abstract String getSequenceStatement(EAttribute primaryKey);
 	public abstract String processInsert(EAttribute primaryKey, String insert);
 	public abstract String getDatabaseType(EDataType dataType);
 	public abstract String getAutokeyDefinition(EAttribute primaryKey);
 	public abstract boolean isPrimaryKeyPartOfColDef(EAttribute primaryKey);
 }
