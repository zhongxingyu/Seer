 package Analyzer.code.java.metrics;
 
 import Analyzer.code.LineType;
 import Analyzer.code.java.Contains;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ClassMetrics extends CodeMetrics {
 
     private List<MethodMetrics> methodMetricsList;
     private List<String> attributeList;
 
     public ClassMetrics(String name) {
         super(name);
         methodMetricsList = new ArrayList<>();
         attributeList = new ArrayList<>();
     }
 
     public ClassMetrics(String name, String[] code) {
         super(name, code);
         methodMetricsList = new ArrayList<>();
         attributeList = new ArrayList<>();
         extractAttributes();
     }
 
     @Override
     public void setCode(String[] codeArray) {
         super.setCode(codeArray);
         extractAttributes();
     }
 
     public void addMethod(MethodMetrics method) {
         methodMetricsList.add(method);
     }
 
     public Double getNumberOfMethod() {
         return (double) methodMetricsList.size();
     }
 
     public Double getNumberOfAttribute() {
         return (double) attributeList.size();
     }
 
     public Double getLackOfCohesion() {
         return calculateLOCM();
     }
 
     public MethodMetrics[] getMethodMetricsList() {
         return methodMetricsList.toArray(new MethodMetrics[methodMetricsList.size()]);
     }
 
    public Double getMethodMetricsListSize() {
        return (double) methodMetricsList.size();
     }
 
     private Double calculateLOCM() {
         Double F = getNumberOfAttribute();
         Double M = getNumberOfMethod();
         return 1 - (getSummationMF() / (M * F));
     }
 
     private Double getSummationMF() {
         Double total = 0.0;
         for (MethodMetrics method : methodMetricsList) {
             for (String attribute : attributeList) {
                 if (method.isUsed(attribute)) {
                     total++;
                 }
             }
         }
         return total;
     }
 
     private void extractAttributes() {
         Boolean inAttributeZone = false;
         Integer numberOfLine = 0;
         for (String line : code) {
             numberOfLine++;
             if (Contains.Class(line) && !inAttributeZone) {
                 inAttributeZone = true;
             } else if (Contains.Function(line) && inAttributeZone) {
                 inAttributeZone = false;
             } else if (inAttributeZone
                     && LineType.EFFECTIVE == lineTypeTable.get(numberOfLine)
                     && Contains.SemiColon(line)) {
                 String[] tokens = line.split("[ ;]");
                 attributeList.add(tokens[tokens.length - 1]);
             }
         }
     }
     //TODO Cread metrica de Dependencias
     //Nota HashTable for Eferente(num import) (nombre de clase, numero de repeticiones)
     //                   Aferente consultar Hashmap
 
     @Override
     public Boolean isClassMetrics() {
         return true;
     }
 
     @Override
     public Boolean isMethodMetrics() {
         return false;
     }
 
     @Override
     public Boolean isPackageMetrics() {
         return false;
     }
 }
