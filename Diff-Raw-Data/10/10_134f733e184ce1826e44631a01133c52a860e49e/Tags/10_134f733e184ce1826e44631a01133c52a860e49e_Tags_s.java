 package engine;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Iterator ;
 
 import javax.servlet.http.HttpServletRequest;
 
 import constante.Path;
 import java.util.Random ;
 import org.hibernate.HibernateException;
 import org.hibernate.JDBCException;
 import org.hibernate.Query;
 
 import util.StringUtil;
 import util.XmlBuilder;
 import engine.WebPage.Mode;
 import entity.TagTheme ;
 import entity.Tag ;
 import entity.Geolocalisation;
 import display.Photos ;
 import system.SystemTools;
 public class Tags {
   private static final long serialVersionUID = 1L;
     
   @SuppressWarnings("unchecked")
 public static XmlBuilder treatTAGS(HttpServletRequest request)
     throws WebPage.AccessorsException, HibernateException {
     String tagList = "" ;
     XmlBuilder output = new XmlBuilder("tags");
     String special = request.getParameter("special") ;
     if ("CLOUD".equals(special)) {
       XmlBuilder cloud = new XmlBuilder("cloud");
       
       int sizeScale = 200 ;
       int sizeMin = 100 ;
       try {
 	String rq = "SELECT t.ID, t.Nom, count( tp.Photo ) AS count " +
 	  " FROM Tag t, TagPhoto tp, Photo p, Album a "+
 	  " WHERE t.ID = tp.Tag " +
 	  " AND tp.Photo = p.ID " +
 	  " AND p.Album = a.ID "+
 	  " AND "+WebPage.restrictToPhotosAllowed(request, "p")+" " +
 	  " AND "+WebPage.restrictToThemeAllowed(request, "a")+" " +
	  " GROUP BY t.ID " ;
 	
 	String rqMax = "SELECT max( count ) "+
 	  "FROM ( "+
 	  rq +
 	  ")temp" ;
 	Query query = WebPage.session.createSQLQuery(rqMax);
 	rqMax = "done" ;
 
 	Object val = query.uniqueResult() ;
 	long max = 100 ;
 	if (val != null) {
 	  max = Long.valueOf(val.toString()) ;
 	}
 	
 	query = WebPage.session.createQuery(rq);
 	query.setReadOnly(true).setCacheable(true);
 	rq = "done" ;
 	
 	Iterator it = query.iterate() ;
 	while (it.hasNext()) {
 	  Object[] select = (Object[]) it.next() ;
 	  long current = (Long)select[2] ;
 	  int size = (int)(sizeMin + ((double)current/max)*sizeScale) ;
 	  cloud.add(new XmlBuilder("tag", select[1])
 		     .addAttribut("size", size)
 		     .addAttribut("nb", current)
 		     .addAttribut("id", select[0])) ;
 	}
 	cloud.validate() ;
       } catch (Exception e) {
 	e.printStackTrace();
 
 	cloud.cancel() ;
 	cloud.addException(e.getMessage()) ;
       }
       output.add(cloud);
       return output.validate() ;
     }
 
     if ("PERSONS".equals(special) || "PLACES".equals(special) || "RSS".equals(special)) {
       XmlBuilder xmlSpec = new XmlBuilder(special.toLowerCase());
       
       int type ;
       if ("PERSONS".equals(special)) {
 	type = 1 ;
       } else {
 	type = 3 ;
       }
        
       try {
 	String rq ;
 	if (WebPage.isRootSession(request)) {
 	  rq = "FROM Tag t WHERE t.TagType = '"+type+"'" ;
 	} else {
 	  rq = "SELECT DISTINCT t " +
 	    "FROM Tag t, TagPhoto tp, Photo p, Album a "+
 	    "WHERE t.TagType = '"+type+"' "+
 	    "AND t.ID = tp.Tag " +
 	    "AND tp.Photo = p.ID " +
 	    "AND p.Album = a.ID "+
 	    "AND "+WebPage.restrictToPhotosAllowed(request, "p")+" " +
 	    "AND "+WebPage.restrictToThemeAllowed(request, "a")+" " ;
 	}
 	
 	Query query = WebPage.session.createQuery(rq);
 	query.setReadOnly(true).setCacheable(true);
 	rq = "done" ;
 	
 	Iterator it = query.iterate() ;
 	while (it.hasNext()) {
 	  Tag enrTag = (Tag) it.next() ;
 
 	  rq = "FROM TagTheme th "+
 	    "WHERE th.Tag = '"+enrTag.getID()+"' "+
 	    (WebPage.isRootSession(request) ? "" : "AND th.Theme = "+WebPage.getThemeID(request) + " ");
 
 	  query = WebPage.session.createQuery(rq);
 	  query.setReadOnly(true).setCacheable(true);
 	  rq = "done" ;
 
 	  XmlBuilder tag = new XmlBuilder("tag", enrTag.getNom())
 	    .addAttribut("id", enrTag.getID()) ;
 	  List lstTh = query.list() ;
 	  Random rand = new Random() ;
 	  while (!lstTh.isEmpty()) {
 	    int i = rand.nextInt(lstTh.size()) ;
 	    TagTheme enrTh= (TagTheme) lstTh.get(i) ;
 	    if (enrTh.getPhoto() != null) {
 	      tag.addAttribut("picture", enrTh.getPhoto());
 	      break ;
 	    } else {
 	      lstTh.remove(i);
 	    }
 	  }
 	  if ("RSS".equals(special)) {
 	    Geolocalisation enrGeo = enrTag.getGeolocEnt() ;
 	    if (enrGeo != null) {
 	      tag.add("lat", enrGeo.getLat());
 	      tag.add("long", enrGeo.getLong());
 	    }
 	  }
 	  xmlSpec.add(tag);
 	}
 	xmlSpec.validate();	
       } catch (Exception e) {
 	e.printStackTrace();
 
 	xmlSpec.cancel() ;
 	xmlSpec.addException(e) ;
       }
       output.add(xmlSpec);
       return output.validate() ;
     }
 
     String[] tags = request.getParameterValues("tagAsked");
     String action = request.getParameter("action") ;
     String page = request.getParameter("page") ;
     XmlBuilder submit = null ;
     Boolean correct = true ;
     
     if ("SUBMIT".equals(action)
 	&& Users.isLoggedAsCurrentManager(request)
 	&& !Path.isReadOnly()) {
       submit = engine.Photos.treatPhotoSUBMIT(request, correct);
     }
 
     if (("EDIT".equals(action) || !correct)
 	&& Users.isLoggedAsCurrentManager(request)
 	&& !Path.isReadOnly()) {
       output =  Photos.treatPhotoEDIT(request, submit);
       XmlBuilder return_to = new XmlBuilder("return_to");
       return_to.add("name", "Tags");
       return_to.add("page", page);
       for (int i = 0; i < tags.length; i++) {
 	return_to.add("tagAsked", tags[i]);
       }
       output.add(return_to);
       return output.validate() ;
     }		
         
     XmlBuilder thisPage = new XmlBuilder(null);
     thisPage.add("name", "Tags");
     for (int i = 0; tags != null && i < tags.length; i++) {
       thisPage.add("tagAsked", tags[i]);
     }
     
     String rq = null ;
     try { 
       if (tags != null) {
 	List<Integer> listTagId = new ArrayList<Integer> (tags.length) ;
 	for (int i = 0; i < tags.length; i++) {
 	  try {
 	    listTagId.add(Integer.parseInt(tags[i]));
 	  } catch (NumberFormatException e) {}
 	}
 	XmlBuilder title = new XmlBuilder("title");
 	title.add(WebPage.displayListLB(Mode.TAG_USED, request, listTagId,
 					WebPage.Box.NONE)) ;
 	output.add(title);
 	
 	//creation de la requete
 	String tagsID = "";
 	rq = "SELECT p FROM Photo p, Album a, TagPhoto tp " +
 	  " WHERE a.ID = p.Album and p.ID = tp.Photo"+
 	  " AND tp.Tag in ('-1' " ;
 	for (int id : listTagId) {
 	  rq += ", '"+id+"'" ;
 	  tagsID += "-"+id ;
 	}
 	rq += ")" ;
 	
 	rq += " AND "+WebPage.restrictToPhotosAllowed(request, "p")+" " ;
 	rq += " AND "+WebPage.restrictToThemeAllowed(request, "a")+" " ;
 	rq += " GROUP BY p.ID " ;	
 	rq += " ORDER BY p.Path DESC " ;
 
 	
 	Query query = WebPage.session.createQuery(rq);
 	query.setReadOnly(true).setCacheable(true);
 	rq = "done" ;
 	if ("FULLSCREEN".equals(special)) {
 	  SystemTools.fullscreen(query, "Tags", tagsID, page, request);
 	  return null ;
 	} else {
 	  output.add(display.Photos.displayPhoto(query, request, thisPage, StringUtil.escapeURL(tagList), submit)) ;
 	}
       }
     }catch (JDBCException e) {
       e.printStackTrace() ;
       
       output.cancel() ;
       output.addException("JDBCException", rq ) ;
       output.addException("JDBCException", e.getSQLException()) ;
     }
     return output.validate () ;
   }  
 }
