 package com.psddev.dari.db;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.joda.time.DateTime;
 
 import com.psddev.dari.util.ObjectUtils;
 
 public class BootstrapPackage extends Record {
 
     @Indexed(unique = true)
     private String name;
 
     private Set<ObjectType> types;
 
     private boolean isInit;
 
     private Map<ObjectType, Set<BootstrapPackage>> typesInOtherPackages;
 
     private Map<ObjectType, Set<ObjectField>> missingTypes;
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Set<ObjectType> getTypes() {
         if (types == null) {
             types = new HashSet<ObjectType>();
         }
         return types;
     }
 
     public void setTypes(Set<ObjectType> types) {
         this.types = types;
     }
 
     public boolean isInit() {
         return isInit;
     }
 
     public void setIsInit(Boolean isInit) {
         this.isInit = isInit;
     }
 
     public Map<ObjectType, Set<BootstrapPackage>> getTypesInOtherPackages() {
         if (typesInOtherPackages == null) {
             typesInOtherPackages = new TreeMap<ObjectType, Set<BootstrapPackage>>();
         }
         return typesInOtherPackages;
     }
 
     public void setTypesInOtherPackages(Map<ObjectType, Set<BootstrapPackage>> typesInOtherPackages) {
         this.typesInOtherPackages = typesInOtherPackages;
     }
 
     public Map<ObjectType, Set<ObjectField>> getMissingTypes() {
         if (missingTypes == null) {
             missingTypes = new TreeMap<ObjectType, Set<ObjectField>>();
         }
         return missingTypes;
     }
 
     public void setMissingTypes(Map<ObjectType, Set<ObjectField>> missingTypes) {
         this.missingTypes = missingTypes;
     }
 
     @TypeData.FieldInternalNamePrefix("bootstrap.")
     public static class TypeData extends Modification<ObjectType> {
 
         private Set<String> packages;
 
         public Set<String> getPackageNames() {
             if (packages == null) packages = new HashSet<String>();
             return packages;
         }
 
         public void setPackageNames(Set<String> packages) {
             this.packages = packages;
         }
 
     }
 
     public final static class Static {
         public static final String INIT_NAME = " _init";
 
         public static final String PACKAGE_NAME_HEADER = "Bootstrap Package Name";
         public static final String PROJECT_HEADER = "Project";
         public static final String DATE_HEADER = "Date";
         public static final String TYPES_HEADER = "Types";
         public static final String ROW_COUNT_HEADER = "Row Count";
         public static final String ALL_TYPES_HEADER_VALUE = "ALL";
 
         public static BootstrapPackage getPackage(Database database, String name) {
             return getPackagesMap(database).get(name);
         }
 
         public static List<BootstrapPackage> getPackages(Database database) {
             return Collections.unmodifiableList(new ArrayList<BootstrapPackage>(getPackagesMap(database).values()));
         }
 
         private static Map<String, BootstrapPackage> getPackagesMap(Database database) {
             Map<String, BootstrapPackage> packagesByName = new TreeMap<String, BootstrapPackage>();
             // First create the _init package for the whole database
             BootstrapPackage initPkg = new BootstrapPackage();
             initPkg.setName(INIT_NAME);
             initPkg.setIsInit(true);
             packagesByName.put(INIT_NAME, initPkg);
 
             for (ObjectType type : database.getEnvironment().getTypes()) {
                 TypeData bmod = type.as(TypeData.class);
                 for (String name : bmod.getPackageNames()) {
                     BootstrapPackage pkg = packagesByName.get(name);
                     if (pkg == null) {
                         pkg = new BootstrapPackage();
                         pkg.setName(name);
                         packagesByName.put(name, pkg);
                     }
                     pkg.getTypes().add(type);
                 }
             }
             for (BootstrapPackage pkg : Query.from(BootstrapPackage.class).using(database).selectAll()) {
                 packagesByName.put(pkg.getName(), pkg);
             }
             for (Map.Entry<String, BootstrapPackage> entry : packagesByName.entrySet()) {
                 checkConsistency(database, entry.getValue(), new HashSet<BootstrapPackage>(packagesByName.values()), null);
             }
             return packagesByName;
         }
 
         public static void checkConsistency(Database database, BootstrapPackage pkg, Set<BootstrapPackage> allPackages, Set<ObjectType> additionalTypes) {
             Set<ObjectType> allMyTypes = getAllTypes(database, pkg);
             Set<ObjectType> allTypes = new HashSet<ObjectType>();
             for (BootstrapPackage otherPkg : allPackages) {
                 allTypes.addAll(getAllTypes(database, otherPkg));
             }
             Map<ObjectField, ObjectType> checkFields = new HashMap<ObjectField, ObjectType>();
             for (ObjectType type : allMyTypes) {
                 for (ObjectField field : type.getFields()) {
                     for (ObjectType t : field.getTypes()) {
                         if (! field.isEmbedded() && ! t.isEmbedded()) {
                             checkFields.put(field, t);
                         }
                     }
                 }
             }
 
             if (additionalTypes != null) {
                 for (ObjectType type : additionalTypes) {
                     for (ObjectField field : type.getFields()) {
                         for (ObjectType t : field.getTypes()) {
                             if (! field.isEmbedded() && ! t.isEmbedded()) {
                                 checkFields.put(field, t);
                             }
                         }
                     }
                 }
             }
 
             for (ObjectField field : database.getEnvironment().getFields()) {
                 for (ObjectType t : field.getTypes()) {
                     if (! field.isEmbedded() && ! t.isEmbedded()) {
                         checkFields.put(field, t);
                     }
                 }
             }
 
             for (Map.Entry<ObjectField, ObjectType> entry : checkFields.entrySet()) {
                 ObjectField field = entry.getKey();
                 ObjectType t = entry.getValue();
                 if (! allMyTypes.contains(t)) {
                     if (allTypes.contains(t)) {
                         if (! pkg.getTypesInOtherPackages().containsKey(t)) {
                             pkg.getTypesInOtherPackages().put(t, new HashSet<BootstrapPackage>());
                         }
                         for (BootstrapPackage otherPkg : allPackages) {
                             if (getAllTypes(database, otherPkg).contains(t)) {
                                 pkg.getTypesInOtherPackages().get(t).add(otherPkg);
                             }
                         }
                     }
                     if (! pkg.getMissingTypes().containsKey(t)) {
                         pkg.getMissingTypes().put(t, new HashSet<ObjectField>());
                     }
                     pkg.getMissingTypes().get(t).add(field);
                 }
             }
         }
 
         public static Set<ObjectType> getAllTypes(Database database, BootstrapPackage pkg) {
             return getAllTypes(database, pkg.getTypes());
         }
 
         public static Set<ObjectType> getAllTypes(Database database, Set<ObjectType> types) {
             Set<ObjectType> allTypes = new HashSet<ObjectType>();
             for (ObjectType type : types) {
                 allTypes.addAll(database.getEnvironment().getTypesByGroup(type.getInternalName()));
             }
             return allTypes;
         }
 
         public static void writeContents(Database database, BootstrapPackage pkg, Set<ObjectType> additionalTypes, Writer writer, String projectName) throws IOException {
             writer.write(PACKAGE_NAME_HEADER + ": ");
             writer.write(pkg.getName());
             writer.write("\n");
             writer.write(PROJECT_HEADER + ": ");
             writer.write(projectName);
             writer.write("\n");
             writer.write(DATE_HEADER + ": ");
             writer.write(new DateTime().toString("yyyy-MM-dd HH:mm:ss z"));
             writer.write("\n");
             boolean first;
             ObjectType objType = database.getEnvironment().getTypeByClass(ObjectType.class);
             Query<?> query = Query.fromAll().using(database);
             query.where("_type != ?", objType);
             writer.write(TYPES_HEADER + ": ");
             Set<ObjectType> exportTypes = new HashSet<ObjectType>();
             if (pkg.isInit()) {
                 writer.write(ALL_TYPES_HEADER_VALUE);
             } else {
                 first = true;
                 exportTypes.addAll(getAllTypes(database, pkg));
                 exportTypes.addAll(getAllTypes(database, additionalTypes));
                 query.where("_type = ?", exportTypes);
                 if (exportTypes.contains(objType)) {
                     writer.write(objType.getInternalName());
                     first = false;
                 }
                 if (exportTypes.contains(objType)) {
                     if (!first) writer.write(","); else first = false;
                     writer.write(objType.getInternalName());
                 }
                 for (ObjectType type : exportTypes) {
                     if (type.equals(objType)) continue;
                     if (!first) writer.write(","); else first = false;
                     writer.write(type.getInternalName());
                 }
             }
             Long count;
             if (pkg.isInit()) {
                 count = Query.fromAll().using(database).noCache().count();
             } else {
                 count = Query.fromAll().using(database).noCache().where("_type = ?", exportTypes).count();
             }
             writer.write("\n");
             writer.write(ROW_COUNT_HEADER + ": ");
             writer.write(ObjectUtils.to(String.class, count));
             writer.write("\n\n"); // blank line between headers and data
             writer.flush();
 
            if (exportTypes.contains(objType)) {
                 for (ObjectType r : Query.from(ObjectType.class).using(database).noCache().resolveToReferenceOnly().iterable(100)) {
                     writer.write(ObjectUtils.toJson(r.getState().getSimpleValues(true)));
                     writer.write("\n");
                 }
             }
 
             for (Object o : query.noCache().resolveToReferenceOnly().iterable(100)) {
                 if (o instanceof Record) {
                     Record r = (Record) o;
                     writer.write(ObjectUtils.toJson(r.getState().getSimpleValues(true)));
                     writer.write("\n");
                 }
             }
             writer.flush();
         }
 
         public static void importContents(Database database, String filename, InputStream fileInputStream, boolean deleteFirst, int numWriters, int commitSize) throws IOException {
             BootstrapImportTask importer = new BootstrapImportTask(database, filename, fileInputStream, deleteFirst, numWriters, commitSize);
             importer.submit();
         }
     }
 }
