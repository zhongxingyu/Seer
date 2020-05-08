 package engine;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.text.SimpleDateFormat;
 
 import java.util.List ;
 import java.util.ArrayList ;
 import java.util.Iterator ;
 import java.util.NoSuchElementException;
 
 import constante.Path;
 import entity.*;
 
 
 import org.apache.log4j.Logger;
 import org.hibernate.HibernateException;
 import org.hibernate.JDBCException;
 import org.hibernate.Session;
 import org.hibernate.Query;
 
 import util.HibernateUtil;
 import util.XmlBuilder;
 import util.google.GooglePoint;
 import util.google.GooglePoint.Point;
 
 public class WebPage {
   public enum Page {PHOTO, IMAGE, USER, ALBUM, CONFIG, CHOIX, TAGS, VOID, PERIODE, MAINT} 
   public enum Type {PHOTO, ALBUM}
   public enum Box  {NONE, MULTIPLE, LIST, MAP, MAP_SCRIPT} 
   public enum Mode {TAG_USED, TAG_NUSED, TAG_ALL, TAG_NEVER, TAG_GEO} ;
   
   private static final long serialVersionUID = -8157612278920872716L;
   public static final Logger log = Logger.getLogger("WebAlbums");
   public static final Logger other = Logger.getLogger("Other");
   public static final Logger critic = Logger.getLogger("Critic");
   
   public static final SimpleDateFormat DATE_STANDARD =
     new SimpleDateFormat("yyyy-MM-dd");
   public static final SimpleDateFormat DATE_FRANCE =
     new SimpleDateFormat("dd-MM-yyyy");
   public static final SimpleDateFormat DATE_HEURE =
     new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
   
   public static final String USER_CHEAT = "0" ;
   
   public static final int TAILLE_PHOTO = 10;
   public static final int TAILLE_ALBUM = 15;
   
   public static Session session ;
 
   static {
    WebPage.log.info("WebAlbums v4 is loading ... ");
     
     WebPage.log.info("Starting up Hibernate ...");
     session = HibernateUtil.currentSession();
     WebPage.log.info("Hibernate is ready !");
   }
   
   public static String restrictToPhotosAllowed(HttpServletRequest request, String photo) {
     String rq = null ;
 
     rq = "SELECT p.ID "+
       " FROM Photo p, Album a "+
       " WHERE p.Album = a.ID " ;
     if (Users.getUserID(request) != USER_CHEAT) {
       rq += " AND ("+ 
 	//albums autorisé
 	"((p.Droit = 0 OR p.Droit is null) AND a.Droit >= '"+Users.getUserID(request)+"') "+
 	"OR "+
 	//albums ayant au moins une photo autorisée
 	"(p.Droit >= '"+Users.getUserID(request)+"')" +
 	")" ;
     }
 
     rq = " "+photo+".ID IN ("+ processListID(request, rq, true)+") " ;
     return rq ;
   }
 
   public static String restrictToAlbumsAllowed(HttpServletRequest request, String album) {
     String rq = null ;
     if (Users.getUserID(request) == USER_CHEAT) {
       rq = "SELECT a.ID FROM Album a WHERE 1 = 1 " ;
     }
     else {
       rq = "SELECT a.ID "+
 	"FROM Album a, Photo p "+
 	"WHERE a.ID = p.Album AND ("+ 
 	//albums autorisé
 	"((p.Droit = 0 OR p.Droit is null) AND a.Droit >= '"+Users.getUserID(request)+"') "+
 	"OR "+
 	//albums ayant au moins une photo autorisée
 	"(p.Droit >= '"+Users.getUserID(request)+"')" +
 	") " ;
     }
     rq = " "+album+".ID IN ("+processListID(request, rq, true)+") " ;
     return rq ;
   }
 
   public static String restrictToThemeAllowed (HttpServletRequest request, String album) {
     if (isRootSession(request)) return " 1 = 1" ;
     else return " "+album+".Theme = '"+getThemeID(request)+"' " ;
   }
   
   @SuppressWarnings("unchecked")
 private static String processListID(HttpServletRequest request, String rq, boolean restrict) {
     boolean EXEC = false ;
 
     if (restrict && !isRootSession(request)) {
       rq += " AND "+WebPage.restrictToThemeAllowed(request, "a")+" " ;
     }
     
     if (EXEC) {
       StringBuilder str = new StringBuilder() ;
       Query query = session.createQuery(rq) ;
       rq = "done" ;
       query.setReadOnly(true).setCacheable(true);
       Iterator it = query.iterate() ;
       
       while (it.hasNext()) {
 	str.append(it.next() + ", ");
       }
       str.append("-1");
       
       return str.toString();
     } else {
       return rq;
     }
   }
     
   //look for the theme which ID is themeID in the DB and log it if possible
   //return themeID if OK or null in case of error
   protected static String tryToSaveTheme(HttpServletRequest request)
     throws HibernateException {
     
     boolean autologging = false ;
     String themeID = request.getParameter("theme") ;
     if (themeID == null) {
       themeID = Path.autoLogin() ;    
       if (themeID == null) return null ;
       autologging = true ;
     }
 
     String currentThemeID = getThemeID(request) ;
     if (themeID.equals(currentThemeID)) return themeID ;
     
     String rq = null ;
     //memoriser le nom de l'auteur
     try {
       rq = "FROM Theme WHERE id='"+themeID+"'" ;
       Theme enrTheme = (Theme) session.createQuery(rq).uniqueResult() ;
       rq = "done" ;
 
       if (enrTheme == null) {
 	WebPage.log.warn("Ce theme ("+themeID+") n'existe pas ...");
 	return null;
       }
 
       String savedID = saveTheme (request, enrTheme);
       
       if (autologging) {
 	Users.saveUser(request, null, null, true);
       }
       return savedID ;
 
     } catch (JDBCException e) {
       e.printStackTrace() ;
       
       WebPage.log.warn("Impossible d'executer la requete => "+rq) ;
       WebPage.log.warn(e.getSQLException());
       return null ;
     }
   }
   
   protected static String saveTheme(HttpServletRequest request,
 				  Theme enrTheme) {
     String oldID = (String) request.getSession().getAttribute("ThemeID");
     String newID = Integer.toString(enrTheme.getID()) ;
 
     if (!newID.equals(oldID) && Users.isLoggedAsCurrentManager(request)) {
       Users.clearUser(request);
     }
     
     log.info("saveTheme ("+enrTheme.getNom()+"-"+newID+")");
     request.getSession().setAttribute("ThemeName", enrTheme.getNom()) ;
     request.getSession().setAttribute("ThemeID", newID) ;
     request.getSession().setAttribute("EditionMode", EditMode.NORMAL) ;
 
     return newID ;
   }
   
   public static String getThemeID(HttpServletRequest request)
     throws HibernateException {
     return (String) request.getSession().getAttribute("ThemeID");
   }
   
   public static boolean isRootSession (HttpServletRequest request) {
     return "-1".equals(getThemeID(request)); 
   }
 
   public static String getThemeName(HttpServletRequest request) {
     return (String) request.getSession().getAttribute("ThemeName");
   }
   
   public static XmlBuilder displayMapInBody(HttpServletRequest request,
 					  String name, String info)
     throws HibernateException {
     return displayListLBNI(Mode.TAG_USED, request, null, Box.MAP, name, info) ;
   }
   public static XmlBuilder displayMapInScript(HttpServletRequest request,
 					      String name, String info)
     throws HibernateException {
     XmlBuilder output = displayListLBNI(Mode.TAG_USED, request, null, Box.MAP_SCRIPT, name, info) ;
     return output ;
   }
 
   
   //display a list into STR
   //according to the MODE
   //and the information found in the REQUEST.
   //List made up of BOX items,
   //and is named NAME
   public static XmlBuilder displayListBN(Mode mode,
 				   HttpServletRequest request,
 				   Box box,
 				   String name)
     throws HibernateException {
     return displayListLBNI(mode, request, null, box, name, null) ;
   }
   
   //display a list into STR
   //according to the MODE
   //and the information found in REQUEST.
   //List is made up of BOX items
   //and is named with the default name for this MODE
   public static XmlBuilder displayListB(Mode mode,
 				  HttpServletRequest request,
 				  Box box)
     throws HibernateException {
     return displayListLBNI(mode, request, null, box,
 			   "tags", null) ;
   }
   
   //display a list into STR
   //according to the MODE
   //and the information found in REQUEST.
   //List is made up of BOX items
   //and is named with the default name for this MODE
   //if type is PHOTO, info (MODE) related to the photo #ID are put in the list
   //if type is ALBUM, info (MODE) related to the album #ID are put in the list
   public static XmlBuilder displayListIBT(Mode mode,
 					  HttpServletRequest request,
 					  int id,
 					  Box box,
 					  Type type)
     throws HibernateException {
     return displayListIBTNI(mode, request, id, box, type,
 			    null,
 			    null) ;
   }
   
   //display a list into STR
   //according to the MODE
   //and the information found in REQUEST.
   //List is made up of BOX items
   //and is filled with the IDs 
   public static XmlBuilder displayListLB(Mode mode,
 					 HttpServletRequest request,
 					 List<Integer> ids,
 					 Box box)
     throws HibernateException {
     return displayListLBNI(mode, request, ids, box,
 			   "tags", null) ;
   }
   
   
   //display a list into STR
   //according to the MODE
   //and the information found in REQUEST.
   //List is made up of BOX items
   //and is named NAME
   //if type is PHOTO, info (MODE) related to the photo #ID are put in the list
   //if type is ALBUM, info (MODE) related to the album #ID are put in the list
   @SuppressWarnings("unchecked")
   public static XmlBuilder displayListIBTNI(Mode mode,
 					    HttpServletRequest request,
 					    int id,
 					    Box box,
 					    Type type,
 					    String name,
 					    String info)
     throws HibernateException {
     String rq = null ;
     try {
       List<Integer> ids = null ;
       if (type == Type.PHOTO) {
 	rq = "from TagPhoto " +
 	  "where photo = '"+id+"'" ;
 	Query query = session.createQuery(rq) ;
 	rq = "done" ;
 	query.setReadOnly(true).setCacheable(true);
 	List list = query.list();
 	
 	ids= new ArrayList<Integer>(list.size()) ;
 	for (Object o : list) {
 	  TagPhoto tag = (TagPhoto) o ;
 	  ids.add(tag.getTag()) ;
 	}
       } else if (type == Type.ALBUM){
 	rq = "SELECT DISTINCT tp " +
 	  "FROM Photo photo, TagPhoto tp " +
 	  "WHERE photo.Album = '"+id+"' " +
 	  "AND photo.ID = tp.Photo " ;
 	Query query = session.createQuery(rq) ;
 	rq = "done" ;
 	query.setReadOnly(true).setCacheable(true);
 	List list = query.list();
 	
 	ids = new ArrayList<Integer>(list.size()) ;
 	for (Object o : list) {
 	  TagPhoto tagPhoto = (TagPhoto) o ;
 	  ids.add(tagPhoto.getTag()) ;
 	}
       }
       return displayListLBNI(mode, request, ids, box, name, info)
 	.addAttribut("box", box)
 	.addAttribut("type", type)
 	.addAttribut("id", id) 
       	.addAttribut("mode", mode);
     } catch (JDBCException e) {
       XmlBuilder output = new XmlBuilder(name != null ? name : "listBuilder") ;
       e.printStackTrace() ;
       output.addException("JDBCException",rq);
       output.addException("JDBCException",e.getSQLException()) ;
       return output.validate() ;
     }
   }
   
   @SuppressWarnings("unchecked")
   //display a list into STR
   //according to the MODE
   //and the information found in REQUEST.
   //List is made up of BOX items
   //and is named NAME
   //Only IDS are added to the list
   //Mode specific information can be provide throug info (null otherwise)
   //(used by Mode.MAP for the link to the relevant address)
   public static XmlBuilder displayListLBNI(Mode mode,
 					   HttpServletRequest request,
 					   List<Integer> ids,
 					   Box box,
 					   String name,
 					   String info)
     throws HibernateException {
     XmlBuilder xmlResult = null ;
     String rq = null ;
     try {
       //
       //affichage de la liste des tags où il y aura des photos
       if (!Users.isLoggedAsCurrentManager(request)) {
 	if (mode != Mode.TAG_USED && mode != Mode.TAG_GEO)
 	  throw new RuntimeException("Don't want to process mode "+mode+" when not logged at manager");
 	
 	rq = "SELECT DISTINCT ta " +
 	  "FROM Tag ta, TagPhoto tp, Photo p, Album a "+
 	  "WHERE  ta.ID = tp.Tag AND tp.Photo = p.ID AND p.Album = a.ID "+
 	  "AND "+WebPage.restrictToPhotosAllowed(request, "p")+" " +
 	  "AND "+WebPage.restrictToThemeAllowed(request, "a")+" " ;
 	
 	if (mode == Mode.TAG_GEO) {
 	  rq += " AND ta.TagType = '3' " ;
 	}
 	rq += " ORDER BY ta.Nom" ;
       } else /* current manager*/ {
 	
 	if (mode ==  Mode.TAG_USED || mode == Mode.TAG_GEO) {
 	  rq = "SELECT DISTINCT ta " +
 	    "FROM Tag ta, TagPhoto tp, Photo p, Album a "+
 	    "WHERE ta.ID = tp.Tag AND tp.Photo = p.ID AND p.Album = a.ID " +
 	    "AND "+WebPage.restrictToThemeAllowed(request, "a")+" " +
 	    "AND "+WebPage.restrictToPhotosAllowed(request, "p")+" " ;
    
 	  if (mode == Mode.TAG_GEO) {
 	    rq += " AND ta.TagType = '3' " ;
 	  }
 	  rq += " ORDER BY ta.Nom" ;
 	} else if (mode ==  Mode.TAG_ALL) {
 	  
 	  //afficher tous les tags
 	  rq = "SELECT DISTINCT ta FROM Tag ta" ;
 	} else if (mode == Mode.TAG_NUSED || mode == Mode.TAG_NEVER) {
 	  
 	  //select the tags not used [in this theme]
 	  if (mode == Mode.TAG_NEVER || isRootSession(request)) {
 	    //select all the tags used
 	    rq = "SELECT DISTINCT tp.Tag FROM TagPhoto tp" ;
 
 	  } else /* TAG_NUSED*/ {
 	    //select all the tags used in photo of this theme
 	    rq = "SELECT DISTINCT tp.Tag " +
 	      "FROM TagPhoto tp, Photo p, Album a "+
 	      "WHERE "+
 	      " tp.Photo = p.ID AND p.Album = a.ID "+
 	      " AND a.Theme = '"+getThemeID(request)+"' ";
 	  }
 	  
 	  rq = "SELECT DISTINCT ta "+
 	    " FROM Tag ta "+
 	    " WHERE ta.id NOT IN ("+processListID(request, rq, false)+") "+
 	    " ORDER BY ta.Nom" ;
 	
 	} else /* not handled mode*/{
 	  xmlResult = new XmlBuilder("list") ;
 	  
 	  xmlResult.addException("Unknown handled mode :"+mode);
 	  return xmlResult.validate() ;
 	}
       } /* isManagerSession */
       
       Query query = session.createQuery(rq) ;
       rq = "done" ;
       query.setReadOnly(true).setCacheable(true);
       Iterator it = query.iterate();
     
       xmlResult = new XmlBuilder ("tags");
       xmlResult.addAttribut("mode", mode) ;
       
       GooglePoint map = null ;
       if (box == Box.MAP_SCRIPT) {
 	map = new GooglePoint(name) ;
       }
       
       xmlResult.addComment("Mode: "+mode);
       xmlResult.addComment("Box:" +box);
       xmlResult.addComment("List: "+ ids);
       
       while (it.hasNext()) {	
 	String type = "nop" ;
 	int id = -1 ;
 	String nom = null ;
 	Point p = null;
 	Integer photo = null ;
 	
 	//first, prepare the information (type, id, nom)
 	Tag enrTag = (Tag) it.next();
 	if (box == Box.MAP_SCRIPT ) {
 	  if (enrTag.getTagType() == 3) {
 	    //ensure that this tag is displayed in this theme
 	    //(in root theme, diplay all of theme
 	    rq = "FROM TagTheme th "+
 	      " WHERE th.Tag = "+enrTag.getID()+" "+
 	      " AND th.Theme = "+getThemeID(request)+" ";
 	    TagTheme enrTagTh =
 	      (TagTheme) session.createQuery(rq).uniqueResult() ;
 	    rq = "done" ;
 	    
 	    if (enrTagTh != null && !enrTagTh.getIsVisible()) {
 	      continue ;
 	    } 
 	    
 	    //get its geoloc
 	    rq = "FROM Geolocalisation g "+
 	      " WHERE g.Tag = "+enrTag.getID()+" ";
 	    Geolocalisation enrGeo =
 	      (Geolocalisation) session.createQuery(rq).uniqueResult() ;
 	    rq = "done" ;
 	    if (enrGeo != null) {
 	      id = enrTag.getID() ;
 	    
 	      p = new Point (enrGeo.getLat(),
 			     enrGeo.getLong(),
 			     enrTag.getNom()) ;
 	      nom = enrTag.getNom() ;
 	      
 	      //Get the photo to display, if any
 	      if (enrTagTh != null) {
 		photo = enrTagTh.getPhoto () ;
 	      }
 	    } 
 	  }
 	} else if (box == Box.MAP) {
 	} else {
 	  id = enrTag.getID() ;
 	  nom = enrTag.getNom();
 	  
 	  switch (enrTag.getTagType()) {
 	    case 1 : type = "who" ; break ;
 	    case 2 : type = "what" ; break ;
 	    case 3 : type = "where" ; break ;
 	    default : type = "unknown" ; break ;
 	  }
 	}
 	//display the value [if in ids][select if in ids]
 	if (box == Box.MAP_SCRIPT)  {
 	  if (nom != null && (ids == null || ids.contains(id))) {
 	    String msg ;
 	    msg = String.format("<center><a href='Tags?tagAsked=%s'>%s</a></center>",
 				id,p.name );
 	    if (photo != null) {
 	      msg += String.format("<br/><img height='150px' alt='%s' "+
 				   "src='Images?"+
 				   "id=%d&amp;mode=PETIT' />",
 				   p.name,
 				   photo);
 	    }
 	    
 	    p.setMsg(msg);
 	    map.addPoint(p) ;
 	  }				  
 	} else if (box == Box.MAP) {
 	} else {
 	  String selected = "" ;
 	  boolean written = true ;
 	  if (ids != null) {
 	    if (box == Box.MULTIPLE) {
 	      if (ids.contains(id)) {
 		selected = "checked" ;
 	      }
 	    } else if (!ids.contains(id)) {
 	      written = false ;
 	    }
 	  }
 	  if (written) {
 	    xmlResult.add(new XmlBuilder(type, nom)
 			  .addAttribut("id", id)
 			  .addAttribut("checked", selected));
 	  }
 	}
       } /* while loop*/
       
       if (box == Box.MAP)  {
 	xmlResult = new XmlBuilder("map") ;
 	xmlResult.add("name", name);
 	xmlResult.add(GooglePoint.getBody());
       } else if (box == Box.MAP_SCRIPT) {
 	xmlResult = XmlBuilder.newText() ;
 	xmlResult.addText(map.getInitFunction()) ;
       }
       
     } catch (JDBCException e) {
       e.printStackTrace() ;
       if (xmlResult != null) xmlResult.cancel() ;
       else xmlResult = new XmlBuilder("list") ;
       xmlResult.addException("JDBCException", rq);
       xmlResult.addException("JDBCException", e.getSQLException());
     }
     return xmlResult.validate() ;
   }
 
   @SuppressWarnings("unchecked")
   public static XmlBuilder displayListDroit (Integer right, Integer albmRight)
     throws HibernateException {
     if (right == null && albmRight == null) throw new NullPointerException ("Droit and Album cannot be null");
     
     XmlBuilder output = new XmlBuilder ("userList") ;
     String rq = "done" ;
     boolean hasSelected = false ;
     
     try {
       rq = "from Utilisateur" ;
       Iterator it = session.createQuery(rq).iterate() ;
       rq = "done" ;
       while (it.hasNext()) {
 	Utilisateur enrUtil = (Utilisateur) it.next() ;
 		
 	String name = enrUtil.getNom() ;
 	Integer id = enrUtil.getID();
 	boolean selected = false ;
 	
 	if (albmRight != null &&  albmRight.equals(enrUtil.getID())) {
 	  name = "["+name+"]" ;
 	  id = null ;
 
 	  if (right == null || right.equals(0)) {
 	    selected = true ;
 	  }
 	} else if (right != null &&  right.equals(enrUtil.getID())) {
 	  selected = true ;
 	}
 	
 	XmlBuilder util = new XmlBuilder("user", name);
 	util.addAttribut("id", id == null ? "null" : id) ;
 	if (selected) {
 	  util.addAttribut("selected", true);
 	  hasSelected = true ;
 	}
 	output.add(util);
       }
       if (!hasSelected) throw new NoSuchElementException("Right problem: "+right+", "+albmRight);
     } catch (JDBCException e) {
       e.printStackTrace() ;
 
       output.addException("JDBCException", rq);
       output.addException("JDBCException", e.getSQLException());
     }
     return output.validate();
   }
 
   public static XmlBuilder getUserName(Integer id) {
     if (id != null) {
       String rq = "from Utilisateur u where u.ID = '"+id+"'" ;
       Utilisateur enrUtil = (Utilisateur) WebPage.session.createQuery(rq).uniqueResult() ;
       
       if (enrUtil != null) {
 	return new XmlBuilder ("user", enrUtil.getNom()) ;
       }
     }
     return null ;
   }
   
   @SuppressWarnings("unchecked")
   public static XmlBuilder getUserInside(int id) throws HibernateException {
     XmlBuilder output = new XmlBuilder("userInside");
     boolean filled = false ;
     String rq = "SELECT DISTINCT p.Droit "+
       " FROM Photo p "+
       " WHERE p.Album = '"+id+"' "+
       " AND p.Droit != null AND p.Droit != 0" ;
     Iterator it = WebPage.session.createQuery(rq).iterate() ;
 
     while (it.hasNext()) {
       Integer userId = (Integer) it.next() ;
       XmlBuilder xmlUser = getUserName(userId) ;
       if (xmlUser != null) {
 	output.add(xmlUser) ;
 	filled = true ;
       }
     }
     if (!filled) return null ;
     else return output.validate();
   }
 
   public static XmlBuilder getUserOutside(int albmId) {
     String rq = "SELECT u FROM Utilisateur u, Album a WHERE u.ID = a.Droit AND a.ID = '"+albmId+"'" ;
     Utilisateur enrUtil = (Utilisateur) WebPage.session.createQuery(rq).uniqueResult() ;
       
     return new XmlBuilder ("user", enrUtil.getNom()).addAttribut("album", true) ;
   }
   
   public static void saveDetails (HttpServletRequest request) {
     String details = request.getParameter("details") ;
     if (details != null) {
       Boolean newValue ;
       if (details.equals("OUI")) {
 	newValue = true ;
       } else if (details.equals("NON")) {
 	newValue = false ;
       } else /* SWAP */ {
 	Boolean old = (Boolean) request.getSession().getAttribute("details") ;
 	if (old == null) old = false ;
 	newValue = !old ;
       }
       request.getSession().setAttribute("details", newValue) ;
     }
   }
   public static Boolean getDetails(HttpServletRequest request) {
     Boolean details = (Boolean) request.getSession().
       getAttribute("details") ;
 		
     return (details == null ? false : details) ;
   }
 
   public static void saveMaps (HttpServletRequest request) {
     String details = request.getParameter("maps") ;
     if (details != null) {
       if (details.equals("OUI")) {
 	request.getSession().setAttribute("maps", true) ;
       } else {
 	request.getSession().setAttribute("maps", false) ;
       }
     }
   }
   public static Boolean getMaps(HttpServletRequest request) {
     Boolean maps = (Boolean) request.getSession().
       getAttribute("maps") ;
 		
     return (maps == null ? false : maps) ;
   }
   
   public static EditMode getEditionMode(HttpServletRequest request) {
     EditMode mode = (EditMode) request.getSession()
       .getAttribute("edition") ;
     
     return (mode == null ? EditMode.NORMAL : mode) ;
       
   }
  
   public static void saveEditionMode(HttpServletRequest request) {
     String editionParam = request.getParameter("edition");
     EditMode newMode ;
     
     if ("NORMAL".equals(editionParam)) {
       newMode = EditMode.NORMAL ;
     } else if ("EDITION".equals(editionParam)) {
       newMode = EditMode.EDITION ;
     } else if ("SWAP".equals(editionParam)) {
       newMode = getNextEditionMode(request) ;
     }else {
       newMode = getEditionMode(request) ;
     }
 
     if (getEditionMode(request) != newMode) {
       log.info(getEditionMode(request) +" --> "+newMode) ;
     }
     request.getSession().setAttribute("edition", newMode) ;    
   }
 
   public static EditMode getNextEditionMode(HttpServletRequest request) {
     EditMode editionMode = getEditionMode(request) ;
     EditMode next ;
     if (editionMode == EditMode.VISITE) {
       next = EditMode.NORMAL ;
     } else if (editionMode == EditMode.NORMAL) {
       next = EditMode.EDITION ;
     } else if (editionMode == EditMode.EDITION) {
       next = EditMode.VISITE ;
     } else {
       next = EditMode.NORMAL ;
     }
     return next ;
   }
   
   //[begin][end][nb pages][page]
   public static Integer[] calculBornes(Type type,
 				       String page,
 				       String asked,
 				       int size) {
     int ipage ;
     int taille = (type == Type.ALBUM ? TAILLE_ALBUM : TAILLE_PHOTO) ;
     Integer[] bornes = new Integer[4] ;
     try {
       if (asked != null) {
 	//compute the page into wich the element asked is in
 	ipage = (int) Math.floor(Integer.parseInt(asked)/taille);
 	bornes[0] = ipage * taille  ;
       } else if (page == null) {
 	bornes[0] = 0 ;
 	ipage = 0 ;
       } else {
 	ipage = Integer.parseInt(page) ;
 	bornes[0] = Math.min(ipage * taille , size)  ;
       }
     } catch (NumberFormatException e) {
       //cannot parse the page
       bornes[0] = 0 ;
       ipage = 0 ;
     }
     bornes[0] = (bornes[0] < 0 ? 0 : bornes[0]) ;
     bornes[1] = Math.min(bornes[0]+taille, size);
     bornes[2] = (int) Math.ceil(((double)size)/((double)taille)) ;
     bornes[3] = ipage ;
     return bornes ;
   }
   
   public static XmlBuilder xmlPage (XmlBuilder from, Integer[] bornes){
     XmlBuilder page = new XmlBuilder ("page") ;
     page.addComment("Page 0 .. "+bornes[3]+" .."+bornes[2]) ;
     page.add ("url", from) ;
 
     if (bornes[2] > 1) {
       int ipage = bornes[3] ;
       
       for (int i = 0; i < bornes[2]; i++) {
 	if (i == ipage || ipage == -1 && i == 0) {
 	    page.add("current", i) ;
 	} else if (i < ipage) {
 	  page.add("prev", i) ;
 	} else {
 	  page.add("next", i) ;
 	}
       }
       page.validate() ;
     } else {
       page.addComment("1 page only") ;
     }
     return page ;
   }
   
   public static void preventCaching(HttpServletRequest request,
 				       HttpServletResponse response) {
     // see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
     String protocol = request.getProtocol();
     if ("HTTP/1.0".equalsIgnoreCase(protocol)) {
       response.setHeader("Pragma", "no-cache");
     } else if ("HTTP/1.1".equalsIgnoreCase(protocol)) {
       response.setHeader("Cache-Control", "no-cache"); // "no-store" work also 
 		}
     response.setDateHeader("Expires", 0);
   }
   
   public enum EditMode {VISITE,NORMAL,EDITION}
   public static class AccessorsException extends Exception {
 	private static final long serialVersionUID = 1L;
 	private String e ;
     public AccessorsException(String e) {
       this.e = e ;
     }
     public String toString() {
       return e ;
     }
   }
 
   public static XmlBuilder xmlLogin(HttpServletRequest request) {
     XmlBuilder login = new XmlBuilder ("login") ;
     login.add("theme",getThemeName(request)) ;
     login.add("user",Users.getUserName(request)) ;
     
     if (Users.isLoggedAsCurrentManager(request)
 	&& !Path.isReadOnly()) {
       login.add("admin");
       if (getEditionMode(request) == WebPage.EditMode.EDITION) {
 	login.add("edit");
       } 
     }
 
     if (isRootSession(request)) {
       login.add("root");
     }
 
     return login.validate() ;
   }
 
   public static XmlBuilder xmlAffichage (HttpServletRequest request) {
     XmlBuilder affichage = new XmlBuilder("affichage") ;
     if (Users.isLoggedAsCurrentManager(request)
 	&& !Path.isReadOnly()) {
       if (getEditionMode(request) == WebPage.EditMode.EDITION) {
 	affichage.add("edit");
       }
       affichage.add("edition", getEditionMode(request)) ;
     }
     affichage.add("maps","Avec Carte") ;
     affichage.add ("details", getDetails(request)) ;
     
     return affichage ;
   }
 }
