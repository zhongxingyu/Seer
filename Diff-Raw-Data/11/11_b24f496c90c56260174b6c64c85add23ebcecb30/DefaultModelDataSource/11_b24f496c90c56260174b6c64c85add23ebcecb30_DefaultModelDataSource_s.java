 package org.apache.maven.shared.model.impl;
 
 import org.apache.maven.shared.model.*;
 
 import java.util.*;
 import java.util.logging.Logger;
 
 /**
  * Default implementation of the ModelDataSource.
  */
 public final class DefaultModelDataSource implements ModelDataSource {
 
     private List<ModelProperty> modelProperties;
 
     private List<ModelProperty> originalModelProperties;
 
     private List<DeleteEvent> deleteEvents;
 
     private Map<String, ModelContainerFactory> modelContainerFactoryMap;
 
     private static Logger logger = Logger.getAnonymousLogger();
 
     public ModelContainer join(ModelContainer a, ModelContainer b) throws DataSourceException {
         if (a == null || a.getProperties() == null || a.getProperties().size() == 0) {
             throw new IllegalArgumentException("a or a.properties: empty");
         }
         if (b == null || b.getProperties() == null) {
             throw new IllegalArgumentException("b: null or b.properties: empty");
         }
 
         if (!modelProperties.containsAll(a.getProperties())) {
             List<ModelProperty> unknownProperties = new ArrayList<ModelProperty>();
             for (ModelProperty mp : a.getProperties()) {
                 if (!modelProperties.contains(mp)) {
                     unknownProperties.add(mp);
                 }
             }
             List<DeleteEvent> des = new ArrayList<DeleteEvent>();
             for (DeleteEvent de : deleteEvents) {
                 if (aContainsAnyOfB(de.getRemovedModelProperties(), unknownProperties)) {
                     des.add(de);
                 }
             }
             //output
             StringBuffer sb = new StringBuffer();
             sb.append("Found unknown properties in container 'a': Name = ").append(a.getClass().getName()).append("\r\n");
             for (ModelProperty mp : unknownProperties) {
                 sb.append(mp).append("\r\n");
             }
 
             //    for (DeleteEvent de : des) {
             //        sb.append(de.toString());
             //    }
             System.out.println(sb);
             throw new DataSourceException("ModelContainer 'a' contains elements not within datasource");
         }
 
         if (a.equals(b) || b.getProperties().size() == 0) {
             return a;
         }
 
         int startIndex = modelProperties.indexOf(b.getProperties().get(0));
         delete(a);
         delete(b);
 
         List<ModelProperty> joinedProperties = mergeModelContainers(a, b);
 
         if (modelProperties.size() == 0) {
             startIndex = 0;
         }
         modelProperties.addAll(startIndex, joinedProperties);
 
         List<ModelProperty> deletedProperties = new ArrayList<ModelProperty>();
         deletedProperties.addAll(a.getProperties());
         deletedProperties.addAll(b.getProperties());
         deletedProperties.removeAll(joinedProperties);
         if (deletedProperties.size() > 0) {
             deleteEvents.add(new DeleteEvent(a, b, deletedProperties, "join"));
         }
 
         return a.createNewInstance(joinedProperties);
     }
 
     public void delete(ModelContainer modelContainer) {
         if (modelContainer == null) {
             throw new IllegalArgumentException("modelContainer: null");
         }
         if (modelContainer.getProperties() == null) {
             throw new IllegalArgumentException("modelContainer.properties: null");
         }
         modelProperties.removeAll(modelContainer.getProperties());
         deleteEvents.add(new DeleteEvent(modelContainer, null, modelContainer.getProperties(), "delete"));
     }
 
     public List<ModelProperty> getModelProperties() {
         return new ArrayList<ModelProperty>(modelProperties);
     }
 
     public List<ModelContainer> queryFor(String uri) throws DataSourceException {
         if (uri == null) {
             throw new IllegalArgumentException("uri");
         }
 
         if (modelProperties.isEmpty()) {
             return Collections.EMPTY_LIST;
         }
 
         ModelContainerFactory factory = modelContainerFactoryMap.get(uri);
         if (factory == null) {
             throw new DataSourceException("Unable to find factory for uri: URI = " + uri);
         }
 
         List<ModelContainer> modelContainers = new LinkedList<ModelContainer>();
 
         final int NO_TAG = 0;
         final int START_TAG = 1;
         final int END_START_TAG = 2;
         final int END_TAG = 3;
         int state = NO_TAG;
 
         List<ModelProperty> tmp = new ArrayList<ModelProperty>();
 
         for (Iterator<ModelProperty> i = modelProperties.iterator(); i.hasNext();) {
             ModelProperty mp = i.next();
             if (state == START_TAG && (!i.hasNext() || !mp.getUri().startsWith(uri))) {
                 state = END_TAG;
             } else if (state == START_TAG && mp.getUri().equals(uri)) {
                 state = END_START_TAG;
             } else if (mp.getUri().startsWith(uri)) {
                 state = START_TAG;
             } else {
                 state = NO_TAG;
             }
             switch (state) {
                 case START_TAG: {
                     tmp.add(mp);
                     if (!i.hasNext()) {
                         modelContainers.add(factory.create(tmp));
                     }
                     break;
                 }
                 case END_START_TAG: {
                     modelContainers.add(factory.create(tmp));
                     tmp.clear();
                     tmp.add(mp);
                     state = START_TAG;
                     break;
                 }
                 case END_TAG: {
                     modelContainers.add(factory.create(tmp));
                     tmp.clear();
                     state = NO_TAG;
                 }
             }
         }
 
         //verify data source integrity
         List<ModelProperty> unknownProperties = findUnknownModelPropertiesFrom(modelContainers);
         if (!unknownProperties.isEmpty()) {
             for (ModelProperty mp : unknownProperties) {
                 System.out.println("Missing property from ModelContainer: " + mp);
             }
             throw new DataSourceException("Unable to query datasource. ModelContainer contains elements not within datasource");
         }
 
         return modelContainers;
     }
 
     public void init(List<ModelProperty> modelProperties, Collection<ModelContainerFactory> modelContainerFactories) {
         if (modelProperties == null) {
             throw new IllegalArgumentException("modelProperties: null");
         }
         if (modelContainerFactories == null) {
             throw new IllegalArgumentException("modeContainerFactories: null");
         }
         this.modelProperties = new LinkedList<ModelProperty>(modelProperties);
         this.modelContainerFactoryMap = new HashMap<String, ModelContainerFactory>();
         this.deleteEvents = new ArrayList<DeleteEvent>();
         this.originalModelProperties = new ArrayList<ModelProperty>(modelProperties);
 
         for (ModelContainerFactory factory : modelContainerFactories) {
             Collection<String> uris = factory.getUris();
             if (uris == null) {
                 throw new IllegalArgumentException("factory.uris: null");
             }
 
             for (String uri : uris) {
                 modelContainerFactoryMap.put(uri, factory);
             }
         }
     }
 
     public String getEventHistory() {
         StringBuffer sb = new StringBuffer();
         sb.append("Original Model Properties\r\n");
         for (ModelProperty mp : originalModelProperties) {
             sb.append(mp).append("\r\n");
         }
 
         for (DeleteEvent de : deleteEvents) {
             sb.append(de.toString());
         }
 
         sb.append("Processed Model Properties\r\n");
         for (ModelProperty mp : modelProperties) {
             sb.append(mp).append("\r\n");
         }
         return sb.toString();
     }
 
     private List<ModelProperty> findUnknownModelPropertiesFrom(List<ModelContainer> modelContainers) {
         List<ModelProperty> unknownProperties = new ArrayList<ModelProperty>();
         for (ModelContainer mc : modelContainers) {
             if (!modelProperties.containsAll(mc.getProperties())) {
                 for (ModelProperty mp : mc.getProperties()) {
                     if (!modelProperties.contains(mp)) {
                         unknownProperties.add(mp);
                     }
                 }
             }
         }
         return unknownProperties;
     }
 
     private static int findLastIndexOfParent(ModelProperty modelProperty, List<ModelProperty> modelProperties) {
         for (int i = modelProperties.size() - 1; i >= 0; i--) {
             if (modelProperties.get(i).getUri().equals(modelProperty.getUri())) {
                 for (int j = i; j < modelProperties.size(); j++) {
                     if (!modelProperties.get(j).getUri().startsWith(modelProperty.getUri())) {
                         return j - 1;
                     }
                 }
                 return modelProperties.size() - 1;
             } else if (modelProperties.get(i).isParentOf(modelProperty)) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Removes duplicate model properties from the containers and return list.
      *
      * @param a container A
      * @param b container B
      * @return list of merged properties
      */
     protected static List<ModelProperty> mergeModelContainers(ModelContainer a, ModelContainer b) {
         List<ModelProperty> m = new ArrayList<ModelProperty>();
         m.addAll(a.getProperties());
         m.addAll(b.getProperties());
 
         LinkedList<ModelProperty> processedProperties = new LinkedList<ModelProperty>();
         List<String> uris = new ArrayList<String>();
         String baseUri = a.getProperties().get(0).getUri();
         for (ModelProperty p : m) {
            String subUri = p.getUri().substring(baseUri.length(), p.getUri().length());
             if (!uris.contains(p.getUri())
                     || (subUri.contains("#collection") && !subUri.endsWith("#collection"))) {
                 processedProperties.add(findLastIndexOfParent(p, processedProperties) + 1, p);
                 uris.add(p.getUri());
             }
         }
         /*
         for (ModelProperty mp : processedProperties) {
             System.out.println(mp);
         }
         */
         return processedProperties;
     }
 
     private static boolean aContainsAnyOfB(List<ModelProperty> a, List<ModelProperty> b) {
         for (ModelProperty mp : b) {
             if (a.contains(mp)) {
                 return true;
             }
         }
         return false;
     }
 
     private static class DeleteEvent {
 
         private List<ModelProperty> removedModelProperties;
 
         private ModelContainer mcA;
 
         private ModelContainer mcB;
 
         private String methodName;
 
         DeleteEvent(ModelContainer mcA, ModelContainer mcB, List<ModelProperty> removedModelProperties, String methodName) {
             this.mcA = mcA;
             this.mcB = mcB;
             this.removedModelProperties = removedModelProperties;
             this.methodName = methodName;
         }
 
         public ModelContainer getMcA() {
             return mcA;
         }
 
         public ModelContainer getMcB() {
             return mcB;
         }
 
         public List<ModelProperty> getRemovedModelProperties() {
             return removedModelProperties;
         }
 
         public String getMethodName() {
             return methodName;
         }
 
         public String toString() {
             StringBuffer sb = new StringBuffer();
             sb.append("Delete Event: ").append(methodName).append("\r\n");
             sb.append("Model Container A:\r\n");
             for (ModelProperty mp : mcA.getProperties()) {
                 sb.append(mp).append("\r\n");
             }
             if (mcB != null) {
                 sb.append("Model Container B:\r\n");
                 for (ModelProperty mp : mcB.getProperties()) {
                     sb.append(mp).append("\r\n");
                 }
             }
 
             sb.append("Removed Properties:\r\n");
             for (ModelProperty mp : removedModelProperties) {
                 sb.append(mp).append("\r\n");
             }
             return sb.toString();
         }
     }
 }
