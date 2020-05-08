 package odb.core;
 
 import java.util.Vector;
 import bridge.ODBridge;
 import odb.utils.Constants;
 import org.neodatis.odb.ODB;
 import org.neodatis.odb.Objects;
 import org.neodatis.odb.core.query.IQuery;
 import org.neodatis.odb.core.query.criteria.Where;
 import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery;
 import org.w3c.dom.Element;
 
 /**
  *
  * @author praise
  */
 public class Search implements ODBridge {
 
     private ODB odb = null;
     private float wspX;
     private float wspY;
 
     @Override
     public Vector<Element> searchCategoryRadius(String category, float x, float y) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Vector<String> getCategories() {
         odb = Constants.getDbConnection();
         Vector<String> v = new Vector<String>();
         //null pointer exception if data base not connected FIXED !
         if(odb!=null){
             Objects categories = odb.getObjects(Category.class);
             while (categories.hasNext()) {
                 Category c = (Category) categories.next();
                 v.add((String)c.getName());
             }
         }else System.err.println("No database initialized");
 
         return v;
     }
 
     @Override
     public Vector<String> getSubcategories(String category) {
         odb = Constants.getDbConnection();
         Vector<String> v = new Vector<String>();
         //null pointer exception if data base not connected FIXED !
         if(odb !=null){
             IQuery query1 = new CriteriaQuery(Category.class, Where.equal("name", category));
             Objects categories = odb.getObjects(query1);
            Category c = (Category) categories.getFirst();
            for (Object obj : c.getSubcategories()) {
                Subcategory sub = (Subcategory) obj;
                v.add((String)sub.getName());
             }
         }else System.err.println("No database initialized");
         
         return v;
     }
 
     @Override
     public Vector<String> getSubcategories(Vector<String> category) {
         Vector<String> v = new Vector<String>();
         if(!category.isEmpty()){
             for (String string : category) {
                 Vector<String> tmpVec = getSubcategories(string);
                 if(!tmpVec.isEmpty()){
                     v.addAll(tmpVec);
                 }
             }
         }
         return v;
     }
 }
