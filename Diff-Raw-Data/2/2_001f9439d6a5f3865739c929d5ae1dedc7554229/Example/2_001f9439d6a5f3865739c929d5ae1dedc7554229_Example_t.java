 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package datamining;
 
 import java.util.ArrayList;
 
 /**
  * @author Fábio Gomes
  * @author Gabriel Baims
  * @author Marianna Portela
  * @author Igor Giusti
  */
 public class Example {
     
     private DataBase dataBase;
     private ArrayList<Double> attrValues;
     
     public Example(DataBase base) {
         dataBase = base;
         attrValues = new ArrayList<Double>();
         
         for (int i = 0; i < dataBase.numAttributes(); i++)
             attrValues.add(null);
     }
         
     public Example(DataBase base, String line) throws Exception {
         dataBase = base;
         String [] strAttrs = line.split(",");
         
         if (strAttrs.length != dataBase.numAttributes()){
             throw new Exception("Numero de atributos errado");
         }
         
         attrValues = new ArrayList<Double>(dataBase.numAttributes());
         for (int i = 0; i < dataBase.numAttributes(); i++) {
             Double value = dataBase.attribute(i).doubleForDomainValue(strAttrs[i]);
             attrValues.add(value);
         }
     }
     
     public void setDataBase(DataBase dataBase) {
         this.dataBase = dataBase;
     }
     
     public DataBase getDataBase() {
         return dataBase;
     }
             
     public double getAttrValue(int index) {
         if (index < 0 || index >= attrValues.size()) {
             throw new RuntimeException("Valor nao pode ser recuperado, " +
                                        "atributo inexistente!");
         }
         return attrValues.get(index).doubleValue();
     }
     
     public double getAttrValue(String name) {
         Attribute attr = dataBase.attribute(name);
         return attrValues.get(attr.getIndex()).doubleValue();
     }
     
     public double getClassValue() {
         return attrValues.get(dataBase.getClassIndex()).doubleValue();
     }
     
     public void setAttrValue(int index, double value) {
         if (index < 0 || index >= attrValues.size()) {
             throw new RuntimeException("O valor nao pode ser atribuido, " +
                                        "atributo inexistente!");
         }
         
         attrValues.set(index, new Double(value));
     }
     
     public void setClassValue(double value) {
         setAttrValue(dataBase.getClassIndex(), value);
     }
     
     public int numAttributes() {
         return attrValues.size();
     }
     
     /**
      * Retorna a representacao do exemplo em String. O formato e o mesmo de um
      * registro em uma base de dados.
      * 
      * @return a representacao do exemplo em formato de registro 
      */
     public String toRegisterString() {
         Attribute attr = dataBase.attribute(0);
         String example = attr.getDomainValue(attrValues.get(0));
         
        for (int i = 1; i < attrValues.size(); i++) {
             attr = dataBase.attribute(i);
             String strAttr = attr.getDomainValue(attrValues.get(i));
             example += ", " + strAttr;
         }
         
         return example;
     }
 
     @Override
     public String toString () {
         String example = "";
         for (int i = 0; i < attrValues.size(); i++) {
             Attribute attr = dataBase.attribute(i);
             example += attr.getName() + ": " + 
                        attr.getDomainValue(attrValues.get(i)) + "\n";
         }
         return example;
     }
 }
