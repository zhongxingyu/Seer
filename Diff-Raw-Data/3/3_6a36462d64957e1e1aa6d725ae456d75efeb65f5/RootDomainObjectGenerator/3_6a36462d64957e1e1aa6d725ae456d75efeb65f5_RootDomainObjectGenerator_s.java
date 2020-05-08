 /**
  * 
  */
 package pt.utl.ist.fenix.tools.codeGenerator;
 
 import java.io.IOException;
 import java.util.Formatter;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 
 import pt.utl.ist.fenix.tools.util.FileUtils;
 import dml.DomainClass;
 import dml.Role;
 
 /**
  * @author - Shezad Anavarali (shezad@ist.utl.pt)
  * 
  */
 public class RootDomainObjectGenerator extends DomainObjectGenerator {
 
     private static final String CLASS_NAME = "net.sourceforge.fenixedu.domain.RootDomainObject";
 
     public void appendMethodsInTheRootDomainObject() throws IOException {
 
         String rootObjectSourceCodeFilePath = outputFolder + "/" + CLASS_NAME.replace('.', '/')
                 + sourceSuffix;
         Set<String> usedNames = new HashSet<String>();
         Map<DomainClass, String> classes = new HashMap<DomainClass, String>();
 
         String rootObjectSourceCode = FileUtils.readFile(rootObjectSourceCodeFilePath);
         int lastBrace = rootObjectSourceCode.lastIndexOf('}');
         if (lastBrace > 0) {
             StringBuilder resultSourceCode = new StringBuilder();
             resultSourceCode.append(rootObjectSourceCode.substring(0, lastBrace));
 
             appendClosureAccessMap(resultSourceCode);
 
             Formatter methods = new Formatter(resultSourceCode);
                      
             
             DomainClass rootDomainObjectClass = getModel().findClass(CLASS_NAME);
             for (Iterator<Role> iter = rootDomainObjectClass.getRoleSlots(); iter.hasNext();) {
                 Role roleSlot = iter.next();
                 if (roleSlot.getMultiplicityUpper() != 1) {
                     //String slotName = StringUtils.capitalize(roleSlot.getName());
                     DomainClass otherDomainClass = (DomainClass) roleSlot.getType();
                     String className = otherDomainClass.getName();
                     if(usedNames.contains(className)){
                        className = otherDomainClass.getSuperclassName() + className;
                     }
                     
                     if(!className.equals("DomainObject")){
                         methods.format("\n\tpublic %s read%sByOID(Integer idInternal){\n", otherDomainClass
                                 .getFullName(), className);
                         
                         methods.format("\t\tfinal %s domainObject = (%s) pt.ist.fenixframework.pstm.Transaction.readDomainObject(%s.class.getName(), idInternal);\n",otherDomainClass.getFullName(), otherDomainClass.getFullName(), otherDomainClass.getFullName());
                         methods.format("return (domainObject == null || domainObject.getRootDomainObject() == null) ? null : domainObject;\n\t}\n");
                         
                         usedNames.add(className);
                         classes.put(otherDomainClass, className);
                     }
 
                     //appendAddToClosureAccessMap(resultSourceCode, otherDomainClass, slotName);
                 }
             }
 
         	resultSourceCode.append("\n\tpublic void initAccessClosures () {");
             for (final Iterator<Role> iter = rootDomainObjectClass.getRoleSlots(); iter.hasNext();) {
                 final Role roleSlot = iter.next();
                 if (roleSlot.getMultiplicityUpper() != 1) {
                     final String slotName = StringUtils.capitalize(roleSlot.getName());
                     final DomainClass otherDomainClass = (DomainClass) roleSlot.getType();
                     appendAddToClosureAccessMap(resultSourceCode, otherDomainClass.getFullName(), classes.get(otherDomainClass), slotName);
                 }
             }
             resultSourceCode.append("\n\t}");
 
             resultSourceCode.append("\n\n}\n");
             //System.out.println(resultSourceCode.toString());
             FileUtils.writeFile(rootObjectSourceCodeFilePath, resultSourceCode.toString(), false);
         }
 
     }
     
 	private void appendClosureAccessMap(final StringBuilder resultSourceCode) {
         resultSourceCode.append("\n\tprivate interface DomainObjectReader {");
         resultSourceCode.append("\n\t\tpublic DomainObject readDomainObjectByOID(final Integer idInternal);");
         resultSourceCode.append("\n\t\tpublic java.util.Set readAllDomainObjects();");
         resultSourceCode.append("\n\t}");
     	resultSourceCode.append("\n\tprivate static final java.util.Map<String, DomainObjectReader> closureAccessMap = new java.util.HashMap<String, DomainObjectReader>();");
         resultSourceCode.append("\n\tpublic static DomainObject readDomainObjectByOID(final Class domainClass, final Integer idInternal) {");
         resultSourceCode.append("\n\t\tif (domainClass != null) {");
         resultSourceCode.append("\n\t\t\tfinal DomainObjectReader domainObjectReader = closureAccessMap.get(domainClass.getName());");
         resultSourceCode.append("\n\t\t\tif (domainObjectReader != null) {");
         resultSourceCode.append("\n\t\t\t\treturn domainObjectReader.readDomainObjectByOID(idInternal);");
         resultSourceCode.append("\n\t\t\t} else if (domainClass != Object.class && domainClass != DomainObject.class) {");
         resultSourceCode.append("\n\t\t\t\treturn readDomainObjectByOID(domainClass.getSuperclass(), idInternal);");
         resultSourceCode.append("\n\t\t\t}");
         resultSourceCode.append("\n\t\t}");
         resultSourceCode.append("\n\t\treturn null;");
         resultSourceCode.append("\n\t}");
         resultSourceCode.append("\n\tpublic static java.util.Set readAllDomainObjects(final Class domainClass) {");
         resultSourceCode.append("\n\t\tfinal java.util.Set domainObjects = readAllDomainObjectsAux(domainClass);");
         resultSourceCode.append("\n\t\tfinal java.util.Set resultSet = new java.util.HashSet();");
         resultSourceCode.append("\n\t\tif (domainObjects != null) {");
         resultSourceCode.append("\n\t\t\tfor (final Object object : domainObjects) {");
         resultSourceCode.append("\n\t\t\t\tif (domainClass.isInstance(object)) {");
         resultSourceCode.append("\n\t\t\t\t\tresultSet.add(object);");
         resultSourceCode.append("\n\t\t\t\t}");
         resultSourceCode.append("\n\t\t\t}");
         resultSourceCode.append("\n\t\t}");
         resultSourceCode.append("\n\t\treturn resultSet;");
         resultSourceCode.append("\n\t}");
         resultSourceCode.append("\n\tpublic static java.util.Set readAllDomainObjectsAux(final Class domainClass) {");
         resultSourceCode.append("\n\t\tif (domainClass != null) {");
         resultSourceCode.append("\n\t\t\tfinal DomainObjectReader domainObjectReader = closureAccessMap.get(domainClass.getName());");
         resultSourceCode.append("\n\t\t\tif (domainObjectReader != null) {");
         resultSourceCode.append("\n\t\t\t\treturn domainObjectReader.readAllDomainObjects();");
         resultSourceCode.append("\n\t\t\t} else if (domainClass != Object.class && domainClass != DomainObject.class) {");
         resultSourceCode.append("\n\t\t\t\treturn readAllDomainObjectsAux(domainClass.getSuperclass());");
         resultSourceCode.append("\n\t\t\t}");
         resultSourceCode.append("\n\t\t}");
         resultSourceCode.append("\n\t\treturn null;");
         resultSourceCode.append("\n\t}");
 	}
 
     private void appendAddToClosureAccessMap(final StringBuilder resultSourceCode, final String fullName, final String name, final String slotName) {
     	resultSourceCode.append("\n\t\tclosureAccessMap.put(");
     	resultSourceCode.append(fullName);
     	resultSourceCode.append(".class.getName(), new DomainObjectReader() {");
     	resultSourceCode.append("\n\t\t\tpublic DomainObject readDomainObjectByOID(final Integer idInternal) {");
     	resultSourceCode.append("\n\t\t\t\treturn read");
     	resultSourceCode.append(name);
     	resultSourceCode.append("ByOID(idInternal);");
     	resultSourceCode.append("\n\t\t\t}");
     	resultSourceCode.append("\n\t\t\tpublic java.util.Set readAllDomainObjects() {");
     	resultSourceCode.append("\n\t\t\t\treturn get");
     	resultSourceCode.append(slotName);
     	resultSourceCode.append("Set();");
     	resultSourceCode.append("\n\t\t\t}");
     	resultSourceCode.append("\n\t\t});");
 	}
 
 	public static void main(String[] args) {
 	    process(args, new RootDomainObjectGenerator());
 	    System.exit(0);
     }
 
 }
