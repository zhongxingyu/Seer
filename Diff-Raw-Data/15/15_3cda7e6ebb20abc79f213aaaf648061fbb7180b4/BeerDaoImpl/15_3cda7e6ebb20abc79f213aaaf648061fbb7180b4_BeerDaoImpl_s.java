 package it.antreem.birretta.service.dao.impl;
 
 import com.mongodb.*;
 import it.antreem.birretta.service.dao.BeerDao;
 import it.antreem.birretta.service.dao.DaoException;
 import it.antreem.birretta.service.model.Beer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.bson.types.ObjectId;
 
 /**
  *
  * @author alessio
  */
 public class BeerDaoImpl extends AbstractMongoDao implements BeerDao 
 {
     public final static String BEERS_COLLNAME = "beers";
     
     private static final Log log = LogFactory.getLog(BeerDaoImpl.class);
     
     @Override
     public List<Beer> findBeersByNameLike(String name) throws DaoException 
     {
         List<Beer> list = new ArrayList<Beer>();
         
         DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection beers = db.getCollection(BEERS_COLLNAME);
             BasicDBObject query = new BasicDBObject();
             Pattern pattern = Pattern.compile(/*"^" + */name.trim(), Pattern.CASE_INSENSITIVE);
             query.put("name", pattern);
             DBCursor cur = beers.find(query).limit(10);
             
             while (cur.hasNext()){
                 DBObject _b = cur.next();
                 Beer l = createBeerFromDBObject(_b);
                 list.add(l);
             }
         }
         catch(MongoException ex){
             log.error(ex.getLocalizedMessage(), ex);
             throw new DaoException(ex.getLocalizedMessage(), ex);
         }
         finally {
             if (db != null){
                 db.requestDone();
             }
         }
         
         return list;
     }
 
     @Override
     public ArrayList<Beer>  listBeer(int maxElement) throws DaoException 
     {
         ArrayList<Beer> list = new ArrayList<Beer>();
         DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection beers = db.getCollection(BEERS_COLLNAME);
             BasicDBObject param = new BasicDBObject();
             //ordinamento crescente(1) per il nome
             param.put("name", 1);
             DBCursor cur=null;
             if(maxElement>0)
                cur = beers.find().limit(maxElement);
            else
                cur=beers.find();
            cur.sort(param);
            while (cur.hasNext()){
                DBObject _b = cur.next();
                 Beer u = createBeerFromDBObject(_b);
                list.add(u);
             }
             
         }
         catch(MongoException ex){
             log.error(ex.getLocalizedMessage(), ex);
             throw new DaoException(ex.getLocalizedMessage(), ex);
         }
         finally {
             if (db != null){
                 db.requestDone();
             }
         }
          return list;
     }
 
     @Override
     public Beer findBeerByName(String name) throws DaoException 
     {
         DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection beers = db.getCollection(BEERS_COLLNAME);
             BasicDBObject query = new BasicDBObject();
             query.put("name", name);
             DBCursor cur = beers.find(query);
             
             while (cur.hasNext()){
                 DBObject _b = cur.next();
                 Beer u = createBeerFromDBObject(_b);
                 assert u.getName().equals(name);
                 return u;
             }
             
             return null;
         }
         catch(MongoException ex){
             log.error(ex.getLocalizedMessage(), ex);
             throw new DaoException(ex.getLocalizedMessage(), ex);
         }
         finally {
             if (db != null){
                 db.requestDone();
             }
         }
     }
 
     @Override
     public Beer findById(String id) throws DaoException {
         DBObject obj = findById(id, BEERS_COLLNAME);
         if (obj == null) return null;
         Beer b = createBeerFromDBObject(obj);
         return b;
     }
 
     @Override
     public int saveBeer(Beer b) throws DaoException 
     {
         DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection beers = db.getCollection(BEERS_COLLNAME);
             BasicDBObject beer = createDBObjectFromBeer(b);
             return beers.insert(beer).getN();
         }
         catch(MongoException ex){
             log.error(ex.getLocalizedMessage(), ex);
             throw new DaoException(ex.getLocalizedMessage(), ex);
         }
         finally {
             if (db != null){
                 db.requestDone();
             }
         }
     }
     
     
     protected static Beer createBeerFromDBObject(DBObject obj)
     {
         Beer b = new Beer();
         /*
          * Da vedere come gestire valori nulli, vuoti..
          */
         
         b.setName((String) obj.get("name"));
         b.setBrewery((String) obj.get("brewery"));
         b.setBeerstyle((Integer)obj.get("beerstyle"));
         b.setBeertype((Integer)obj.get("beertype"));
         b.setNationality((String) obj.get("nationality"));
         b.setGrad((String) obj.get("grad"));
         // id fisico del db
       //  b.setId((ObjectId) obj.get("_id")); 
         b.setIdBeer((String)obj.get("idBeer"));
         b.setIdUser((String) obj.get("idUser"));
         b.setUsername((String) obj.get("username"));
         b.setImage((byte[]) obj.get("image"));
         b.setStatus((String)obj.get("status"));
         b.setInsertedOn((String)obj.get("insertedOn"));
         b.setParam1((String) obj.get("param1"));
         b.setParam2((String)obj.get("param2"));
         b.setParam3((String)obj.get("param3"));
         return b;
     }
     
     // For first insert
     protected static BasicDBObject createDBObjectFromBeer (Beer b)
     {
         BasicDBObject _b = new BasicDBObject();
         _b.put("name", b.getName().trim());
         _b.put("idBeer",b.getIdBeer().trim());
         _b.put("brewery", b.getBrewery());
         _b.put("beerstyle", b.getBeerstyle());
         _b.put("beertype", b.getBeertype());
         _b.put("nationality",b.getNationality());
         _b.put("grad", b.getGrad());
         _b.put("idUser",b.getIdUser());
         _b.put("username",b.getUsername());
         _b.put("image", b.getImage());
         _b.put("status",b.getStatus());
         _b.put("insertedOn", b.getInsertedOn());
         _b.put("param1", b.getParam1());
         _b.put("param2", b.getParam2());
         _b.put("param3", b.getParam3());
         
         return _b;
     }
 }
