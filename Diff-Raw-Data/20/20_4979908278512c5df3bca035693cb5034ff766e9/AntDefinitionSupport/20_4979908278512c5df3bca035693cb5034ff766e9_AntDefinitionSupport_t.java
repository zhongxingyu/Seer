 package ceylon.ant.internal;
 
 import java.util.Map;
 
 import org.apache.tools.ant.IntrospectionHelper;
 import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
 
import ceylon.collection.LinkedList;
 
 public class AntDefinitionSupport {
 	private Project project;
 	private IntrospectionHelper introspectionHelper;
 
 	
 	public AntDefinitionSupport(Project project, IntrospectionHelper introspectionHelper) {
 		this.project = project;
 		this.introspectionHelper = introspectionHelper;
 	}
 	
    public void fillAttributeList(LinkedList<ceylon.language.String> result) {
     	Map<String, Class<?>> attributeMap = introspectionHelper.getAttributeMap();
         for(String attributeName : attributeMap.keySet()) {
             ceylon.language.String attributeNameCeylonString = new ceylon.language.String(attributeName);
             result.add(attributeNameCeylonString);
         }
     }
     
    public void fillNestedElementList(LinkedList<ceylon.language.String> result) {
     	Map<String, Class<?>> nestedElementMap = introspectionHelper.getNestedElementMap();
         for(String nestedElementName : nestedElementMap.keySet()) {
             ceylon.language.String nestedElementNameCeylonString = new ceylon.language.String(nestedElementName);
             result.add(nestedElementNameCeylonString);
         }
     }
     
     public Class<Object> nestedElementType(String nestedElementName) {
     	Class<?> elementType = introspectionHelper.getElementType(nestedElementName);
     	@SuppressWarnings("unchecked")
 		Class<Object> result = (Class<Object>) elementType;
     	return result;
     }
 
     public IntrospectionHelper nestedElementIntrospectionHelper(String nestedElementName, Class<Object> nestedElementType) {
     	IntrospectionHelper result = IntrospectionHelper.getHelper(project, nestedElementType);
     	return result;
     }
     
    public boolean isTask(Class<Object> elementType) {
        return Task.class.isAssignableFrom(elementType);
    }
    
    public boolean isDataType(Class<Object> elementType) {
        return DataType.class.isAssignableFrom(elementType);
    }
    
     public boolean isTextSupported() {
     	return introspectionHelper.supportsCharacters();
     }
     
     // needs to be implemented explicitly, because "dynamic" is a Ceylon keyword
     public boolean isDynamicType() {
         return introspectionHelper.isDynamic();
     }
     
     public boolean isContainer() {
         return introspectionHelper.isContainer();
     }
     
 }
