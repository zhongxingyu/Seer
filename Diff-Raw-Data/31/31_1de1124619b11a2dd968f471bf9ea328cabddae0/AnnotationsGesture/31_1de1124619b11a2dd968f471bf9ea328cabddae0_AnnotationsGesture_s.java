 package extractor;
 
 import java.util.*;
 import utils.Item;
 import utils.MysqlDatabase;
 import utils.Prop;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class AnnotationsGesture {
     private static ArrayList<Prop> getAllAnnotations(Item item){
         ArrayList<Prop> annotations = new ArrayList<Prop>();
         
         //get opencalais annotations
         ArrayList<Prop> citiesProps = openCalaisExtractor.getCitiesResultProp(item.concat());
         annotations.addAll(citiesProps);
         
         
         //get WikiMeta annotations
         ArrayList<Prop> WikiMetaProps = WikiMetaRequest.getProperties(item);
         annotations.addAll(WikiMetaProps);
         
         //get location annotations using WikiMeta
         /*ArrayList<Prop> DBpediaProps = DBpediaRequest.getProperties(WikiMetaProps);
                    DBpediaProps.addAll(DBpediaRequest.getProperties(citiesProps));
         annotations.addAll(DBpediaProps);
         */
         
         //get the grid of locations length using INSEE database
         
         ArrayList<Prop> InseeProps = InseeSparqlRequest.getProperties(citiesProps);
         annotations.addAll(InseeProps);
         return annotations;        
     }
     
     public static void addAnnotations(ArrayList<Item> items){
         Iterator<Item> it = items.iterator();
         while(it.hasNext()){
             Item i = it.next();
             
            System.out.println("get WikiMeta annotations for "+i.getUri());
             System.out.println("\t"+i);
             ArrayList<Prop> props = AnnotationsGesture.getAllAnnotations(i);
             i.addProps(props);
             System.out.println("\t"+props);
             
            System.out.println("get WikiMeta annotations => done");
         }
     }
     
     
     public static void main(String[] args){
         Item it = new Item("http://test","bienvenue à Antibes","", new Date());
         System.out.println(getAllAnnotations(it));
     }
 }
