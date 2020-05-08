 package it.antreem.birretta.service;
 
 import it.antreem.birretta.service.dao.DaoFactory;
 import it.antreem.birretta.service.dto.*;
 import it.antreem.birretta.service.model.*;
 import it.antreem.birretta.service.model.Badge;
 import it.antreem.birretta.service.model.Notification;
 import it.antreem.birretta.service.model.json.*;
 import it.antreem.birretta.service.util.*;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.jackson.map.util.JSONPObject;
 import java.util.Collections;
 import org.jboss.resteasy.annotations.Form;
 /**
  * BirrettaService
  * 
  */
 @Path("/bserv")
 public class BirrettaService
 {
     private static final Log log = LogFactory.getLog(BirrettaService.class);
     
     /**
      * Operazione di login dell'utente.<br/>
      * Nel caso l'utente faccia login correttamente, viene creata una sessione
      * attiva su DB oppure si continua ad utilizzare quella corrente.<br/>
      * In caso di login fallito si ritorna esito negativo (possibile estensione
      * futura con blocco utente, etc.).
      * 
      * @param username idUser
      * @return Esito della login
      */
     @POST
     @Path("/generaToken")
     @Consumes("application/x-www-form-urlencoded")
     @Produces("application/json")
     public ResultDTO generaToken(@FormParam("idUser") String idUser)
             //,@FormParam("password") String password) 
     {
         log.info("generaToken - "+ idUser);
         // Pre-conditions
         if (idUser == null || idUser.equals("") )
         {
             log.debug("Credenziali di login passate a null. Errore.");
             return createResultDTOEmptyResponse(ErrorCodes.LOGIN_FAILED);
         }
         
         LoginResponseDTO response = new LoginResponseDTO();
         
   //      String username = c.getUsername() != null ? c.getUsername() : "";
    //     String password = c.getPassword() != null ? c.getPassword() : "";
    //     String hash = Utils.SHAsum(Utils.SALT.concat(idUser).getBytes());
         
         log.info("Tentativo di login di : " + idUser ); 
         
         /*
          * Login failed
          *  - return an error
          */
         User u = DaoFactory.getInstance().getUserDao().findUserByIdUser(idUser);
         if (u == null ){
             response.setSuccess(false);
             response.setMessage("Login fallito. Credenziali utente errate.");
             response.setSessionId(null);
             return createResultDTOEmptyResponse(ErrorCodes.LOGIN_FAILED);
         }
         
         /*
          * Login successful
          *  - is there an active session? use that one
          *  - else create a new session
          */
         Session s = DaoFactory.getInstance().getSessionDao().findSessionByUsername(idUser);
         if (s == null) {
             s = new Session();
             s.setUsername(idUser);
             s.setSid(UUID.randomUUID().toString());
             s.setTimestamp(new Date());
             DaoFactory.getInstance().getSessionDao().saveSession(s);
         }
         
         HashMap<String,String> map=new HashMap<String, String>();
         map.put("btUsername", idUser);
         map.put("btSid",s.getSid());
         ArrayList<HashMap<String,String>> list=new ArrayList <HashMap<String,String>>();
         list.add(map);
         return createResultDTOResponseOk(list);
     }
     
     @POST
     @Path("/eliminaToken")
     @Consumes("application/x-www-form-urlencoded")
     @Produces("application/json")
     public ResultDTO eliminaToken(@FormParam("idUser") String idUser, @Context HttpServletRequest httpReq) 
     {
         // Pre-conditions
         if (idUser == null)
         {
             log.debug("Parametri di logout passati a null. Errore.");
             return createResultDTOEmptyResponse(ErrorCodes.LOOUT_FAILED);
         }
         
         // Blocco richieste di un utente per un altro
         if (!idUser.equals(httpReq.getHeader("btUsername"))){
             return createResultDTOEmptyResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         
         // Delete any existing session
         Session s = DaoFactory.getInstance().getSessionDao().findSessionByUsername(idUser);
         if (s != null){
             DaoFactory.getInstance().getSessionDao().deleteSessionBySid(s.getSid());
         }
 
         return createResultDTOEmptyResponse(InfoCodes.OK);
     }
     
     /**
      * Operazione di registrazione di un nuovo utente.<br/>
      * @param c Credenziali dell'utente, ovvero username + password.
      * @return Esito della login
      */
     @POST
     @Path("/register")
     @Consumes("application/json")
     @Produces("application/json")
     public Response register(RegistrationRequestDTO r) 
     {
         // Pre-conditions
         if (r == null)
         {
             log.debug("Dati di registrazione passate a null. Errore.");
             return createJsonErrorResponse(ErrorCodes.LOGIN_FAILED);
         }
         
         GenericResultDTO response = new GenericResultDTO();
         
         String username = r.getUsername() != null ? r.getUsername() : "";
         String password = r.getPassword() != null ? r.getPassword() : "";
         String hash = Utils.SHAsum(Utils.SALT.concat(username).concat(password).getBytes());
 
         log.info("Tentativo di registrazione di username: " + username + " con hash pwd: " + hash); 
         
         //-----------------------
         // Controlli di validita
         //-----------------------
         if (username.length() < 5 || !username.matches("^[a-z0-9_]*$")){
             return createJsonErrorResponse(ErrorCodes.REG_U01);
         }
         if (password.length() < 5 || !password.matches("^[a-zA-Z0-9_]*$")){
             return createJsonErrorResponse(ErrorCodes.REG_P01);
         }
         // ^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$
         if (!r.getEmail().matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$")){
             return createJsonErrorResponse(ErrorCodes.REG_INVALID_EMAIL);
         }
         if (r.getGender() == null ){
                 //|| (!r.getGender().toUpperCase().equals("M") && !r.getSex().toUpperCase().equals("F"))){
             return createJsonErrorResponse(ErrorCodes.REG_INVALID_SEX);
         }
         /*
         if (r.getAge() != null && (r.getAge() < 5 || r.getAge() > 110)){
             return createJsonErrorResponse(ErrorCodes.REG_INVALID_AGE);
         }*/
         if (r.getFirstName() == null || r.getFirstName().length() < 2){
             return createJsonErrorResponse(ErrorCodes.REG_INVALID_FIRST);
         }
         if (r.getLastName() == null || r.getLastName().length() < 2){
             return createJsonErrorResponse(ErrorCodes.REG_INVALID_LAST);
         }
         //-----------------------------
         // Fine controlli di validita
         //-----------------------------
         
         // Se username gia' presente, errore...
         User _u = DaoFactory.getInstance().getUserDao().findUserByUsername(username);
         if (_u != null && _u.getUsername().equals(username)){
             return createJsonErrorResponse(ErrorCodes.REG_USER_DUP);
         }
         
         // ...altrimenti registrazione con successo.
         User newuser = new User();
         String birthDate=r.getBirthDate();
         if(birthDate!=null && !birthDate.equals(""))
                  newuser.setBirthDate(new Date(birthDate));
         newuser.setEmail(r.getEmail());
         newuser.setFirstName(r.getFirstName());
         newuser.setLastName(r.getLastName());
         newuser.setGender(r.getGender());
         newuser.setUsername(r.getUsername());
         newuser.setPwdHash(hash);
         newuser.setActivatedOn(new Date());
         newuser.setLastLoginOn(new Date());
         DaoFactory.getInstance().getUserDao().saveUser(newuser);
         
         response.setSuccess(true);
         response.setMessage("Registrazione eseguita correttamente.");
         
         return createJsonOkResponse(response);
     }
     
     @POST
     @Path("/saveUser")
     @Consumes("application/x-www-form-urlencoded")
     @Produces("application/json")
     public Object saveUser(@Form UpdateUserRequestDTO r,@Context HttpServletRequest httpReq){
         log.info("saveUser - "+ r.getIdUser());
         if (!r.getIdUser().equals(httpReq.getHeader("btUsername"))){
             return createResultDTOEmptyResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         User u = DaoFactory.getInstance().getUserDao().findUserByIdUser(r.getIdUser());
         if(u==null){
             User newuser = new User();
             newuser.setIdUser(r.getIdUser());
             newuser.setBirthDate(r.getBirthDate().getDate());
             newuser.setEmail(r.getEmail());
             newuser.setDisplayName(r.getDisplayName());
             newuser.setGender(r.getGender());
             newuser.setUsername(r.getIdUser());//SETTATO IN AUTOMATICO DALL'IDUSER
             newuser.setNationality(r.getNationality());
             newuser.setActivatedOn(new Date());
             DaoFactory.getInstance().getUserDao().saveUser(newuser);
 
             //generazione token
             Session s = DaoFactory.getInstance().getSessionDao().findSessionByUsername(r.getIdUser());
             if (s == null) {
                 s = new Session();
                 s.setUsername(r.getIdUser());
                 s.setSid(UUID.randomUUID().toString());
                 s.setTimestamp(new Date());
                 DaoFactory.getInstance().getSessionDao().saveSession(s);
             }
 
             HashMap<String, String> map = new HashMap<String, String>();
             map.put("btUsername", r.getIdUser());
             map.put("btSid", s.getSid());
             ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
             list.add(map);
 
             return createResultDTOResponseOk(list);
             //return new SuccessPost();
         }
         else{//NON SETTO PIU' LA MAIL E LO USERNAME
             if(r.getActivatedOn()!=null) u.setActivatedOn(r.getActivatedOn());
             if(r.getAvatar()!=null) u.setAvatar(r.getAvatar());
             //BADGE NON SARANNO MAI INSERITI  A PARTIRE DA UNA REQUEST ESTERNA
 //            if(r.getBadges()!=null) u.setBadges(r.getBadges());
             if(r.getBirthDate()!=null)u.setBirthDate(r.getBirthDate().getDate());
             if(r.getCounterBadges()!=null)u.setCounterBadges(r.getCounterBadges());
             if(r.getCounterCheckIns()!=null)u.setCounterCheckIns(r.getCounterCheckIns());
             if(r.getCounterFriends()!=null)u.setCounterFriends(r.getCounterFriends());
             if(r.getDescription()!=null)u.setDescription(r.getDescription());
             if(r.getDisplayName()!=null)u.setDisplayName(r.getDisplayName());
             if(r.isEnableNotification()!=null)u.setEnableNotification(r.isEnableNotification());
             if(r.getFavorites()!=null)u.setFavorites(r.getFavorites());
             if(r.getFirstName()!=null)u.setFirstName(r.getFirstName());
             if(r.getGender()!=null)u.setGender(r.getGender());
             if(r.getHashBeerlist()!=null)u.setHashBeerlist(r.getHashBeerlist());
             if(r.getHashFriendlist()!=null)u.setHashFriendlist(r.getHashFriendlist());
             if(r.getHashNotificationlist()!=null)u.setHashNotificationlist(r.getHashNotificationlist());
             if(r.getLastLoginOn()!=null)u.setLastLoginOn(r.getLastLoginOn());
             if(r.getLastName()!=null)u.setLastName(r.getLastName());
             if(r.getLiked()!=null)u.setLiked(r.getLiked());
             if(r.getNationality()!=null)u.setNationality(r.getNationality());
             if(r.getRole()!=null)u.setRole(r.getRole());
             if(r.isShareFacebook()!=null)u.setShareFacebook(r.isShareFacebook());
             if(r.isShareTwitter()!=null)u.setShareTwitter(r.isShareTwitter());
             if(r.getStatus()!=null)u.setStatus(r.getStatus());
             DaoFactory.getInstance().getUserDao().updateUser(u);
            //generazione token
             Session s = DaoFactory.getInstance().getSessionDao().findSessionByUsername(r.getIdUser());
             if (s == null) {
                 s = new Session();
                 s.setUsername(r.getIdUser());
                 s.setSid(UUID.randomUUID().toString());
                 s.setTimestamp(new Date());
                 DaoFactory.getInstance().getSessionDao().saveSession(s);
             }
 
             HashMap<String, String> map = new HashMap<String, String>();
             map.put("btUsername", r.getIdUser());
             map.put("btSid", s.getSid());
             ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
             list.add(map);
 
             return createResultDTOResponseOk(list);
         }
     }
     
      /**
      * Restituisce i  dettagli di un utente in formato JSONP.
      */
     @GET
     @Path("/detailsUserByUsername_jsonp")
     @Produces("application/json")
     public JSONPObject detailsUserByUsername_jsonp (
 	@QueryParam("username") final String username,
 	@DefaultValue("callback") @QueryParam("callback") String callbackName)
     {
 		return new JSONPObject(callbackName,detailsUserByUsername(username));
 	}
      /**
      *  Restituisce i  dettagli di un utente
      */
     @GET
     @Path("/detailsUserByUsername")
     @Produces("application/json")
         public ResultDTO detailsUserByUsername(@QueryParam("username") final String username)
     {
         if(username==null)
            return createResultDTOResponseFail(ErrorCodes.FRND_MISSED_PARAM);
         log.info("request details of user: "+username);
         ArrayList<Friend> list = new ArrayList<Friend>();
        Friend data=new Friend(DaoFactory.getInstance().getUserDao().findUserByUsername(username));
         //aggiunta punteggio(bevute da inizio settimana)
         Calendar calMon=Calendar.getInstance();      
         calMon.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
         calMon.set(Calendar.HOUR_OF_DAY, 0);
         calMon.set(Calendar.MINUTE, 0);
         calMon.set(Calendar.MILLISECOND, 0);
         List<Drink> drinksList = DaoFactory.getInstance().getDrinkDao().findDrinksInInterval(data.getIdUser(),calMon.getTime(),new Date());
         data.setCurrentPoints(drinksList.size());
         list.add(data);
         ResultDTO result = createResultDTOResponseOk(list);
         return result;
     }
 
    
      /**
      * Restituisce  i miei amici e i relativi dettagli in formato JSONP.
      */
     @GET
     @Path("/listFriend_jsonp")
     @Produces("application/json")
     public JSONPObject listFriend_jsonp (
 	@QueryParam("maxElement") final String _maxElemet,@QueryParam("idUser") final String idUser,
         @QueryParam("btUsername") final String btUsername,
 	@DefaultValue("callback") @QueryParam("callback") String callbackName,
         @Context HttpServletRequest httpReq)
     {
 		return new JSONPObject(callbackName,listFriend(_maxElemet,idUser,btUsername,httpReq));
 	}
      /**
      * Restituisce i miei amici e i relativi dettagli in formato JSON.
      */
     @GET
     @Path("/listFriend")
     @Produces("application/json")
     public ResultDTO listFriend (@QueryParam("maxElement") final String _maxElemet,@QueryParam("idUser") final String idUser,@QueryParam("btUsername") final String btUsername, @Context HttpServletRequest httpReq)
     {
         log.info("reuest list of "+_maxElemet+" friend of "+idUser);
         if (idUser == null)
         {
             return createResultDTOEmptyResponse(ErrorCodes.NULL_USER);
         }
         else if (!idUser.equals(btUsername)){
             return createResultDTOEmptyResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
      
         int maxElemet = _maxElemet == null ? -1 : new Integer(_maxElemet);
         ArrayList<Friend> list = DaoFactory.getInstance().getFriendDao().getAllMyFriends(maxElemet,idUser);
         //calcolo punteggio per ogni friends (numero bevute settimanali)
         Calendar calMon=Calendar.getInstance();      
         calMon.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
         calMon.set(Calendar.HOUR_OF_DAY, 0);
         calMon.set(Calendar.MINUTE, 0);
         calMon.set(Calendar.MILLISECOND, 0);
         for (Friend f : list) {
             List<Drink> drinksList = DaoFactory.getInstance().getDrinkDao().findDrinksInInterval(f.getIdUser(),calMon.getTime(),new Date());
             f.setCurrentPoints(drinksList.size());
         }
    //     ArrayList<Friend> list = DaoFactory.getInstance().getFriendDao().getAllFriends(maxElemet);
         return createResultDTOResponseOk(list);  
     }
     
       /**
      * Restituisce le le attività dei miei amici in formato JSONP.
      */
     @GET
     @Path("/listFriendActivity_jsonp")
     @Produces("application/json")
     public JSONPObject listFriendActivity_jsonp (
 	@QueryParam("maxElement") final String _maxElemet,@QueryParam("idUser") final String idUser,
         @QueryParam("btUsername") final String btUsername,                             
 	@DefaultValue("callback") @QueryParam("callback") String callbackName,
         @Context HttpServletRequest httpReq)
     {
 		return new JSONPObject(callbackName,listFriendActivity(_maxElemet,idUser,btUsername,httpReq));
 	}
      /**
      * Restituisce le attività dei miei amici in formato JSON.
      */
     @GET
     @Path("/listFriendActivity")
     @Produces("application/json")
     public ResultDTO listFriendActivity (@QueryParam("maxElement") final String _maxElemet,@QueryParam("btUsername") final String btUsername,
                                      
         @QueryParam("idUser") final String idUser,
         @Context HttpServletRequest httpReq)
     {
         log.info("reuest list of "+_maxElemet+"activity of friend of "+idUser);
          if (idUser == null)
         {
             return createResultDTOEmptyResponse(ErrorCodes.NULL_USER);
         }
         else if (!idUser.equals(btUsername)){
             return createResultDTOEmptyResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         int maxElemet = _maxElemet == null ? -1 : new Integer(_maxElemet);
         //trovo i miei amici(compresi quelli in pending)
         ArrayList<FriendsRelation> friendList = DaoFactory.getInstance().getFriendRelationDao().getMyFriends(idUser, maxElemet);
         //per ognuno dei quali trovo le attività(se non sono in stato pending)
         ArrayList<Activity> list= new ArrayList<Activity>();
         for(FriendsRelation fr: friendList)
         {
             if(fr.isFriend())
             {
                 ArrayList<Activity> activities = DaoFactory.getInstance().getActivityDao().findByUser(fr.getIdUser2());
                 list.addAll(activities);
             }
         }
          //ordinamento per data
         Collections.sort(list, new ActivityComparator());
         ArrayList<ActivityDTO> listDTO=new ArrayList<ActivityDTO>();
         for(Activity a: list)
         {
             listDTO.add(new ActivityDTO(a));
         }
         return createResultDTOResponseOk(listDTO);  
     }        
     @GET
     @Path("/updatePos")
     @Produces("application/json")
     public Response updatePos (@QueryParam("username") String username,
                                @QueryParam("lon") Double lon,
                                @QueryParam("lat") Double lat,
                                @Context HttpServletRequest httpReq) 
     {
         // Pre-conditions
         if (username == null || lat == null || lon == null){
             return createJsonErrorResponse(ErrorCodes.UPPOS_MISSED_PARAM);
         }
         
         // Blocco richieste di un utente per un altro
         if (!username.equals(httpReq.getHeader("btUsername"))){
             return createJsonErrorResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         
         User u = DaoFactory.getInstance().getUserDao().findUserByUsername(username);
         DaoFactory.getInstance().getGeoLocDao().updateLoc(u.getId().toString(), lon, lat);
         
         return createJsonOkResponse(new GenericResultDTO(true, "Operazione eseguita correttamente"));
     }
     
     @GET
     @Path("/findLocationCategory")
     @Produces("application/json")
     public ResultDTO findLocationCategory (@QueryParam("idCategory") final String _idCategory)
     {
         String cod = _idCategory == null ? "" : _idCategory;
         LocationCategory cat =DaoFactory.getInstance().getLocationCategoryDao().findLocationCategoryByIdCategory(cod);
         List<LocationCategory> list= new ArrayList<LocationCategory>();
         list.add(cat);
         return createResultDTOResponseOk(list);
     }
     
     @GET
     @Path("/findLocation")
     @Produces("application/json")
     public Response findLocation (@QueryParam("name") final String _name)
     {
         String name = _name == null ? "" : _name;
         List<Location> list = DaoFactory.getInstance().getLocationDao().findLocationsByNameLike(name);
         return createJsonOkResponse(list);
     }
     @GET
     @Path("/findLocNear_jsonp")
     @Produces("application/json")
     public JSONPObject findLocNear_jsonp (@QueryParam("lon") final Double lon,
                                  @QueryParam("lat") final Double lat,
                                  @DefaultValue("0.8") @QueryParam("radius") final Double radius,
 	@DefaultValue("callback") @QueryParam("callback") String callbackName) throws MalformedURLException, IOException 
     {
        return new JSONPObject(callbackName,findLocNear(lon, lat, radius));
     }
     @GET
     @Path("/findLocNear")
     @Produces("application/json")
     public ResultDTO findLocNear (@QueryParam("lon") final Double lon,
                                  @QueryParam("lat") final Double lat,
                                  @DefaultValue("0.8") @QueryParam("radius") final Double radius) throws MalformedURLException, IOException
     {
         if (lon == null || lat == null){
             return createResultDTOResponseFail(ErrorCodes.INSLOC_WRONG_PARAM);
         }
         log.info("request loc");
        ArrayList<Place> places=null;
       // modalità solo da mongoDB
       //  List<Location> list = DaoFactory.getInstance().getLocationDao().findLocationNear(lat, lon, radius);
       //  ArrayList<Location> arrayList= new ArrayList<Location>();
       //  arrayList.addAll(list);
         try{
             ArrayList<Location> findLocationNear = FoursquareJsonProxy.findLocationNear(lat, lon, radius);
             log.info("foursquare loc "+findLocationNear.size());
                  
             places= convertToPlace(findLocationNear);
         }catch(Throwable t)
         {
           log.error("fallito reperimento da foursquare! accesso locale");
           // modalità solo da mongoDB
           ArrayList<Location> list =(ArrayList<Location>) DaoFactory.getInstance().getLocationDao().findLocationNear(lat, lon, radius);
           log.info("mongodb loc "+list.size());
           places= convertToPlace(list);
 
         }
         
         //aggiunta per ogni place del numero di bevute(drink)
         addDrinkedIn(places);
         return createResultDTOResponseOk(places);
     }
     
     @POST
     @Path("/insertLoc")
     @Consumes("application/json")
     @Produces("application/json")
     public Response insertLoc(Location l) 
     {
         // Pre-conditions
         if (l == null){
             return createJsonErrorResponse(ErrorCodes.INSLOC_WRONG_PARAM);
         }
         
         // Validazione parametri di input
         // TODO: Creare messaggi di errore appositi per ogni errore
         if (l.getName() == null || l.getName().length() < 2){
             return createJsonErrorResponse(ErrorCodes.INSLOC_WRONG_NAME_PARAM);
         }
         if (l.getPos() == null || l.getPos().size() != 2){
             return createJsonErrorResponse(ErrorCodes.INSLOC_WRONG_POS_PARAM);
         }
         /**
         if (l.getIdLocType() == null){
             return createJsonErrorResponse(ErrorCodes.INSLOC_WRONG_NULL_TIPOLOC_PARAM);
         }
         // Controllo LocType 
         /* gmorlini:
          * in precedenza veniva fatto findLocTypeById
          * più sensato fare findLocTypeByCod
          */
         /*LocType type = DaoFactory.getInstance().getLocTypeDao().findLocTypeByCod(l.getIdLocType());
         if (type == null){
             return createJsonErrorResponse(ErrorCodes.INSLOC_WRONG_TIPOLOC_PARAM);
         }
         */
         // Inserimento su DB
         Location _l = DaoFactory.getInstance().getLocationDao().findLocationByName(l.getName());
         if (_l != null){
             return createJsonErrorResponse(ErrorCodes.INSLOC_LOC_DUP);
         }
         log.debug("location valida");
         DaoFactory.getInstance().getLocationDao().saveLocation(l);
         
         GenericResultDTO result = new GenericResultDTO(true, "Inserimento eseguito con successo");
         return createJsonOkResponse(result);
     }
     
     
     @GET
     @Path("/findBeerById")
     @Produces("application/json")
     public Response findBeerById (@QueryParam("id") final String _id)
     {
         String id = _id == null ? "" : _id;
         Beer b = DaoFactory.getInstance().getBeerDao().findById(id);
         return createJsonOkResponse(b);
     }
     
     @GET
     @Path("/findUserById")
     @Produces("application/json")
     public Response findUserById (@QueryParam("id") final String _id)
     {
         String id = _id == null ? "" : _id;
         User u = DaoFactory.getInstance().getUserDao().findById(id);
         return createJsonOkResponse(u);
     }
     
     @GET
     @Path("/findLocById")
     @Produces("application/json")
     public Response findLocById (@QueryParam("id") final String _id)
     {
         String id = _id == null ? "" : _id;
         Location l = DaoFactory.getInstance().getLocationDao().findById(id);
         return createJsonOkResponse(l);
     }
     
     @GET
     @Path("/findBeer")
     @Produces("application/json")
     public Response findBeer (@QueryParam("name") final String _name)
     {
         String name = _name == null ? "" : _name;
         List<Beer> list = DaoFactory.getInstance().getBeerDao().findBeersByNameLike(name);
         return createJsonOkResponse(list);
     }
     /**
      * Restituisce le birre con tutti i relativi dettagli in formato JSONP.
      */
     @GET
     @Path("/listBeer_jsonp")
     @Produces("application/json")
     public JSONPObject listBeer_jsonp (
 	@QueryParam("maxElement") final String _maxElemet,
 	@DefaultValue("callback") @QueryParam("callback") String callbackName,
         @DefaultValue("complete") @QueryParam("details") String details)
     {
 		return new JSONPObject(callbackName,listBeer(_maxElemet,details));
 	}
      /**
      * Restituisce le birre con tutti i relativi dettagli in formato JSON.
      */
     @GET
     @Path("/listBeer")
     @Produces("application/json")
     public ResultDTO listBeer (@QueryParam("maxElement") final String _maxElemet,
                                @DefaultValue("complete") @QueryParam("details") String details)
     {
         log.info("reuest list of "+_maxElemet+" beer "+details);
         int maxElemet = _maxElemet == null ? -1 : new Integer(_maxElemet);
         if(details.equalsIgnoreCase("single"))
             
             //lista elementi semplificata
         {
         ArrayList<BeerSingle> list = DaoFactory.getInstance().getBeerDao().listBeerSingle(maxElemet);
       
         return createResultDTOResponseOk(list);
         }
         else
             //details=complete lista completa dettagli birra
         {
         ArrayList<Beer> list = DaoFactory.getInstance().getBeerDao().listBeer(maxElemet);
          ArrayList<BeerDTO> listDTO=new ArrayList<BeerDTO>();
          for(Beer b : list)
          {
              listDTO.add(new BeerDTO(b));
          }
         ResultDTO result = new ResultDTO();
         Status status= new Status();
         status.setCode(100);
         status.setMsg("Status OK");
         status.setSuccess(true);
         Body body =new Body();
         body.setList(listDTO);
         Metadata metaData = new Metadata();
    //     metaData.setBadge("OK", 1, "Notification OK");
     //    metaData.setNotification("OK", 1, "Notification OK");
         
             it.antreem.birretta.service.model.json.Response response = new it.antreem.birretta.service.model.json.Response(status, body, metaData);
         result.setResponse(response);
         return result;
         }
         
     }
     /**
      * Restituisce le birre con tutti i relativi dettagli in formato JSONP.
      */
     @GET
     @Path("/listBeerByPlace_jsonp")
     @Produces("application/json")
     public JSONPObject listBeerByPlace_jsonp (
 	@QueryParam("maxElement") final String _maxElemet,
         @QueryParam("idPlace") String idPlace,
 	@DefaultValue("callback") @QueryParam("callback") String callbackName)
     {
 		return new JSONPObject(callbackName,listBeerByPlace(_maxElemet,idPlace));
 	}
      /**
      * Restituisce le birre bevute in un place con tutti i relativi dettagli in formato JSON.
      */
     @GET
     @Path("/listBeerByPlace")
     @Produces("application/json")
     public ResultDTO listBeerByPlace (@QueryParam("maxElement") final String _maxElemet,
                                       @QueryParam("idPlace") String idPlace)
     {
         if(idPlace==null)
             return createResultDTOResponseFail(ErrorCodes.NULL_PLACE);
         log.info("listBeerByPlace - request list of "+_maxElemet+" beer for place "+idPlace);
         ArrayList<Beer> list=new ArrayList<Beer>();
         int maxElement = _maxElemet == null ? -1 : new Integer(_maxElemet);
         ArrayList<Drink> listDrink= DaoFactory.getInstance().getDrinkDao().listDrinksByPlace(idPlace,maxElement);
         log.info("listBeerByPlace - find "+listDrink.size()+" drink for place");
         for(Drink d : listDrink)
         {
             //da verificare metodo reperimento birre
             Beer beer=DaoFactory.getInstance().getBeerDao().findById(d.getIdBeer());
             log.info("brewery of ("+d.getIdBeer()+")-"+beer.getName()+" is "+beer.getBrewery());
             list.add(beer);
             
         }
         return createResultDTOResponseOk(list);
     }
      /**
      * Restituisce le birre con tutti i relativi dettagli in formato JSONP.
      */
     @GET
     @Path("/listDrinkedBeerOfMyFriend_jsonp")
     @Produces("application/json")
     public JSONPObject listDrinkedBeerOfMyFriend_jsonp (
 	@QueryParam("maxElement") final String _maxElemet,
         @QueryParam("idPlace") final String idPlace,
          @QueryParam("idUser") final String idUser,
          @QueryParam("btUsername") final String btUsername,
 	@DefaultValue("callback") @QueryParam("callback") String callbackName)
     {
 		return new JSONPObject(callbackName,listDrinkedBeerOfMyFriend_jsonp(_maxElemet, idPlace, idUser,btUsername, callbackName));
 	}
      /**
      * Restituisce le birre con tutti i relativi dettagli in formato JSON.
      */
     @GET
     @Path("/listDrinkedBeerOfMyFriend")
     @Produces("application/json")
     public ResultDTO listDrinkedBeerOfMyFriend (@QueryParam("maxElement") final String _maxElemet,
         @QueryParam("idPlace") final String idPlace,
          @QueryParam("idUser") final String idUser,
          @QueryParam("btUsername") final String btUsername,
                                       @Context HttpServletRequest httpReq)
     {
         if(idPlace==null)
         {
             return createResultDTOEmptyResponse(ErrorCodes.NULL_PLACE);
         }
         if(idUser==null)
         {
             return createResultDTOEmptyResponse(ErrorCodes.NULL_USER);
         }
         if(!idUser.equals(btUsername))
         {
             return createResultDTOEmptyResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
             }
         log.info("listDrinkedBeerOfMyFriend -"+idUser+" request list of "+_maxElemet+" drink");
         int maxElemet = _maxElemet == null ? -1 : new Integer(_maxElemet);
         ArrayList<FriendsRelation> myFriends = DaoFactory.getInstance().getFriendRelationDao().getMyFriends(idUser, -1);
         ArrayList<String> idFriends=new ArrayList<String>();
         for (FriendsRelation fr: myFriends)
         {
             idFriends.add(fr.getIdUser2());
         }
         List<Drink> list = DaoFactory.getInstance().getDrinkDao().findDrinksByUsernameAndPlace(idFriends,idPlace,maxElemet);
         return createResultDTOResponseOk(list);
         
     }
     /**
      * Restituisce tutte le bevute con tutti i relativi dettagli in formato JSONP.
      */
     @GET
     @Path("/listDrink_jsonp")
     @Produces("application/json")
     public JSONPObject listDrink_jsonp (
 	@QueryParam("maxElement") final String _maxElemet,
 	@DefaultValue("callback") @QueryParam("callback") String callbackName)
     {
 		return new JSONPObject(callbackName,listDrink(_maxElemet));
 	}
      /**
      * Restituisce tutte le bevute con tutti i relativi dettagli in formato JSON.
      */
     @GET
     @Path("/listDrink")
     @Produces("application/json")
     public ResultDTO listDrink (@QueryParam("maxElement") final String _maxElemet)
     {
         log.info("listDrink - request list of "+_maxElemet+" drink");
         int maxElemet = _maxElemet == null ? -1 : new Integer(_maxElemet);
         ArrayList<Drink> list = DaoFactory.getInstance().getDrinkDao().getDrinksList(maxElemet);
         return createResultDTOResponseOk(list);
         
     }
     
      /**
      * Restituisce le birre con tutti i relativi dettagli in formato JSONP.
      */
     @GET
     @Path("/listNotification_jsonp")
     @Produces("application/json")
     public JSONPObject listNotification_jsonp (
 	@QueryParam("idUser") final String idUser,
 	@DefaultValue("callback") @QueryParam("callback") String callbackName
         ,@QueryParam("btUsername") final String btUsername,
         @Context HttpServletRequest httpReq)
     {
 		return new JSONPObject(callbackName,listNotification(idUser,btUsername,httpReq));
 	}
      /**
      * Restituisce le birre con tutti i relativi dettagli in formato JSON.
      */
     @GET
     @Path("/listNotification")
     @Produces("application/json")
     public ResultDTO listNotification (@QueryParam("idUser") final String idUser,@QueryParam("btUsername") final String btUsername,
     @Context HttpServletRequest httpReq)
     {
          if (idUser == null)
         {
             return createResultDTOEmptyResponse(ErrorCodes.NULL_USER);
         }
         else if (!idUser.equals(btUsername)){
             return createResultDTOEmptyResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         log.info("reuest list of "+idUser+" notifications");
         ArrayList<Notification> list = DaoFactory.getInstance().getNotificationDao().findByUser(idUser);
         ArrayList<NotificationDTO> listDTO=new ArrayList<NotificationDTO>();
         for (Notification n: list)
         {
         listDTO.add(new NotificationDTO(n));
         }
         return createResultDTOResponseOk(listDTO);
         
     }
      /**
      * Restituisce le birre con tutti i relativi dettagli in formato JSONP.
      */
     @GET
     @Path("/listMyActivity_jsonp")
     @Produces("application/json")
     public JSONPObject listMyActivity_jsonp (
 	@QueryParam("idUser") final String idUser,
         @QueryParam("btUsername") final String btUsername,
 	@DefaultValue("callback") @QueryParam("callback") String callbackName,
         @Context HttpServletRequest httpReq)
     {
 		return new JSONPObject(callbackName,listMyActivity(idUser,btUsername,httpReq));
 	}
      /**
      * Restituisce le birre con tutti i relativi dettagli in formato JSON.
      */
     @GET
     @Path("/listMyActivity")
     @Produces("application/json")
     public ResultDTO listMyActivity (@QueryParam("idUser") final String idUser,@QueryParam("btUsername") final String btUsername,
                                      @Context HttpServletRequest httpReq )
     {
         log.info("request list of "+idUser+" activity");
          if (idUser == null)
         {
             return createResultDTOEmptyResponse(ErrorCodes.NULL_USER);
         }
         else if (!idUser.equals(btUsername)){
             return createResultDTOEmptyResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         ArrayList<Activity> list = DaoFactory.getInstance().getActivityDao().findByUser(idUser);
         ArrayList<ActivityDTO> listDTO = new ArrayList<ActivityDTO>();
         for(Activity a: list)
         {
             listDTO.add(new ActivityDTO(a));
         }
         return createResultDTOResponseOk(listDTO);
         
     }
     @POST
     @Path("/insertBeer_jsonp")
     @Consumes("application/x-www-form-urlencoded")
     @Produces("application/json")
     public JSONPObject insertBeer_jsonp(@Form Beer b,@DefaultValue("callback") @QueryParam("callback") String callbackName) 
     {
         return new JSONPObject(callbackName,insertBeer(b));
     }
     @POST
     @Path("/insertBeer")
     @Consumes("application/x-www-form-urlencoded")
     @Produces("application/json")
     public Object insertBeer(@Form Beer b) 
     {
         log.info("received insertBeer"+b.getName());
         // Pre-conditions
         if (b == null){
             return createResultDTOEmptyResponse(ErrorCodes.INSBEER_WRONG_PARAM);
         }
         
         // Validazione parametri di input
         // TODO: Creare messaggi di errore appositi per ogni errore
         if (b.getName() == null || b.getName().length() < 2){
             return createResultDTOEmptyResponse(ErrorCodes.INSBEER_WRONG_PARAM);
         }
        
         // Controllo duplicati
         List<Beer> _bList = DaoFactory.getInstance().getBeerDao().findNewBeerByNameLike(b.getName());
         if (!_bList.isEmpty()){
             return createResultDTOEmptyResponse(ErrorCodes.INSBEER_BEER_DUP);
         }
         
         // Inserimento su DB
         DaoFactory.getInstance().getBeerDao().saveBeer(b);
         List<Beer> findBeersByNameLike = DaoFactory.getInstance().getBeerDao().findNewBeerByNameLike(b.getName());
         if(findBeersByNameLike.size()>1)
         {
             log.error("errore in generazione activity");
         }else if(findBeersByNameLike.size()<1)
         {
             return createResultDTOEmptyResponse(ErrorCodes.SAVE_ERROR);
         }
         //inserimento attività
         Activity a=new Activity();
         a.setBeerName(b.getName());
         a.setIdBeer(findBeersByNameLike.get(0).getIdBeer());
         a.setDate(new Date());
         a.setType(ActivityCodes.BEER_CREATED.getType());
         a.setDisplayName(b.getUsername());
         DaoFactory.getInstance().getActivityDao().saveActivity(a);
         return createResultDTOEmptyResponse(InfoCodes.OK_INSERTBEER_00);
       //  return new SuccessPost();
     }
     @POST
     @Path("/insertListBeer")
     @Consumes("application/json")
     @Produces("application/json")
     public Response insertListBeer(ArrayList<Beer> list) 
     {
         // Pre-conditions
         if (list == null){
             return createJsonErrorResponse(ErrorCodes.INSBEER_WRONG_PARAM);
         }
         
        
        for(Beer b : list)
        {
         // Controllo duplicati
         Beer _b = DaoFactory.getInstance().getBeerDao().findBeerByName(b.getName());
         if (_b == null){
             DaoFactory.getInstance().getBeerDao().saveBeer(b);
         }
        }
         
         GenericResultDTO result = new GenericResultDTO(true, "Inserimento eseguito con successo");
         return createJsonOkResponse(result);
     }
     /**
      * Operazione di check-in.
      * TODO: Controllo di prossimita' location =&gt; future versioni
      * 
      * @param c Richiesta di check-in
      * @param httpReq Header HTTP per blocco richieste cross-user
      * @return Esito operazione o errore
      */
     @POST
     @Path("/checkIn")
     @Consumes("application/x-www-form-urlencoded")
     @Produces("application/json")
     public Object checkIn(@Form CheckInRequestDTO c, @Context HttpServletRequest httpReq) 
     {
         log.info("richiesta check-in: "+c.getIdUser()+" "+c.getIdPlace()+" "+c.getIdBeer());
         // Pre-conditions + controllo validita' parametri
         if (c == null){
             return createResultDTOEmptyResponse(ErrorCodes.CHECKIN_WRONG_PARAM);
         }
         if (c.getIdUser() == null) {
             return createResultDTOEmptyResponse(ErrorCodes.NULL_USER);
         }
         if( c.getIdBeer() == null)
         {
             return createResultDTOEmptyResponse(ErrorCodes.NULL_BEER);
         }    
         if( c.getIdPlace() == null){
             return createResultDTOEmptyResponse(ErrorCodes.NULL_PLACE);
         }
         /* Puo' esserci una bevuta senza voto? per ora si'...
         // TODO: Eventuale check di check-in senza voto
         if (c.getIdFeedback() == null || c.getRate() ==null || (new Integer(c.getRate()) < 0 || new Integer(c.getScore()) > 10)){
             return createJsonErrorResponse(ErrorCodes.CHECKIN_WRONG_PARAM);
         }
         */
         // Blocco richieste di un utente per un altro
         if (!c.getIdUser().equals(httpReq.getHeader("btUsername"))){
             return createResultDTOEmptyResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         
         // Recupero dati necessari e controllo esistenza su db degli oggetti interessati
         User u = DaoFactory.getInstance().getUserDao().findUserByUsername(c.getIdUser());
         if(u==null)
         {
             return createResultDTOEmptyResponse(ErrorCodes.USER_NOT_FOUND);
         }
         Beer b = DaoFactory.getInstance().getBeerDao().findById(c.getIdBeer());
         if(b==null)
         {
             return createResultDTOEmptyResponse(ErrorCodes.MISSING_BEER);
         }
         Location l = DaoFactory.getInstance().getLocationDao().findByIdLocation(c.getIdPlace());
         if(l==null)
         {
             return createResultDTOEmptyResponse(ErrorCodes.MISSING_PLACE);
         }
 
         
         // Controllo che negli ultimi 10 minuti non ci siano piu' di tre bevute
         List<Drink> lastDrinks = DaoFactory.getInstance().getDrinkDao().findRecentDrinks(u.getUsername(), 10);
         if (lastDrinks.size() >= 3){
             return createResultDTOEmptyResponse(ErrorCodes.CHECKIN_TOO_MANY_DRINKS);
         }
         
         // Preparazione oggetto di modello
         Drink d = new Drink();
         d.setIdBeer(b.getIdBeer());
         d.setBeerName(b.getName());
         //verificare esistenza birra e impostare nome
         d.setIdPlace(c.getIdPlace());
         d.setPlaceName(l.getName());
          //  d.setPlaceName(c.getPlaceName());
         //verificare esistenza location e impostare nome
         d.setIdUser(u.getIdUser());
         d.setDisplayName(generateDysplayName(u));
         d.setImage(c.getImage());
         if (c.getRate() != null) {
             d.setRate(new Integer(c.getRate()));
         }
         if (c.getRate2() != null) {
             d.setRate2(new Integer(c.getRate2()));
         }
         if (c.getRate3() != null) {
             log.info("rate2: "+c.getRate3());
             d.setRate3(new Integer(c.getRate3()));
         }
         d.setInsertedOn(new Date());
          // Scrittura su DB - gestione transazione?
         DaoFactory.getInstance().getDrinkDao().saveDrink(d);
        
     
                 
         // Ricerca di nuovi badge e premi da assegnare scatenati da questo check-in
         BadgeFinder finder= new BadgeFinder();
         List<Badge> newBadges = finder.checkNewBadge(u);
         
       
         //INCREMENTO CONTEGGIO CHECKIN
         u.setCounterCheckIns(u.getCounterCheckIns()+1);
         DaoFactory.getInstance().getUserDao().updateUser(u);  
         // Controllo mayorships + notifiche a chi le ha perdute
         // TODO: controllo mayorships + notifiche a chi le ha perdute
 
          //inserimento attività
         Activity a=new Activity();
         a.setBeerName(b.getName());
         a.setIdBeer(b.getIdBeer());
         a.setDate(new Date());
         a.setType(ActivityCodes.CHECKIN.getType());
         a.setIdPlace(l.getIdLocation());
         a.setPlaceName(l.getName());
         a.setIdUser(u.getIdUser());
         a.setDisplayName(generateDysplayName(u));
         
         
         DaoFactory.getInstance().getActivityDao().saveActivity(a);
         
         //CREAZIONE RESPONSE
         ResultDTO result= createResultDTOEmptyResponse(InfoCodes.OK_CHECKIN_00);
         //AGGIUNTA NUOVI BADGES
         result.getResponse().getMetaData().setBadge(newBadges);
         return result;
        // return new SuccessPost();
     }
     
     
     @GET
     @Path("/findMyDrinks")
     @Produces("application/json")
     public Response findMyDrinks (@QueryParam("username") final String username, 
                                   @DefaultValue("10") @QueryParam("limit") Integer limit, 
                                   @Context HttpServletRequest httpReq)
     {
         // Blocco richieste di un utente per un altro
         if (username == null || !username.equals(httpReq.getHeader("btUsername"))){
             return createJsonErrorResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         if (limit < 0 || limit > 100) {
             limit = 10;
         }
         List<Drink> list = DaoFactory.getInstance().getDrinkDao().findDrinksByUsername(username, limit);
         return createJsonOkResponse(list);
     }
     @GET
     @Path("/findBadgesUser_jsonp")
     @Produces("application/json")
     public JSONPObject findBadgesUser_jsonp (@QueryParam("idUser") final String idUser, 
                                   @Context HttpServletRequest httpReq,
                                   @DefaultValue("callback") @QueryParam("callback") String callbackName)
     {
         return new JSONPObject(callbackName,findBadgesUser(idUser,httpReq));
     }
     
     @GET
     @Path("/findBadgesUser")
     @Produces("application/json")
     public ResultDTO findBadgesUser (@QueryParam("idUser") final String idUser, 
                                   @Context HttpServletRequest httpReq)
     {
         // Blocco richieste di un utente per un altro
         if (idUser == null ){
             return createResultDTOEmptyResponse(ErrorCodes.NULL_USER);
         }
         User u= DaoFactory.getInstance().getUserDao().findUserByIdUser(idUser);
         if(u==null)
             return createResultDTOEmptyResponse(ErrorCodes.USER_NOT_FOUND);
         
         List<Badge> list = new ArrayList<Badge>();
         for (int idBadge : u.getBadges())
         {
             Badge b=DaoFactory.getInstance().getBadgeDao().findByIdBadge(idBadge);
             if(b.getImage().contains("/g/e"))
                 b.setImage("http://izolaboatshow.com/wp-content/uploads/2012/01/beer-club.jpg");
             list.add(b);
         }
                 
         return createResultDTOResponseOk(list);
     }
     
     @GET
     @Path("/findUsers")
     @Produces("application/json")
     public Response findUsers (@QueryParam("username") String username, 
                                @QueryParam("first") String first,
                                @QueryParam("last") String last, 
                                @Context HttpServletRequest httpReq)
     {
         List<User> list = DaoFactory.getInstance().getUserDao().findUsers(username, first, last);
         return createJsonOkResponse(list);
     }
     
     
     @POST
     @Path("/frndReq")
     @Consumes("application/x-www-form-urlencoded")
     @Produces("application/json")
     public Object frndReq(@Form FriendReqDTO c, @Context HttpServletRequest httpReq) 
     {
         if (c == null){
             return createResultDTOEmptyResponse(ErrorCodes.FRND_MISSED_PARAM);
         }
         
         String myid = c.getIdRequestor();
         String frndid = c.getIdRequested();
         
         User me = DaoFactory.getInstance().getUserDao().findUserByIdUser(myid);
         String username = me.getUsername();
         if (username == null || !username.equals(httpReq.getHeader("btUsername"))){
             return createResultDTOEmptyResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         
         User frnd = DaoFactory.getInstance().getUserDao().findUserByIdUser(frndid);
         if (frnd == null){
             return createResultDTOEmptyResponse(ErrorCodes.USER_NOT_FOUND);
         }
         
         //crea oggetto friendrelation con isFriend=false
         FriendsRelation fr = DaoFactory.getInstance().getFriendRelationDao().getFriendsRelation(myid, frndid);
         if(fr==null){
             FriendsRelation friendsRelation = new FriendsRelation();
             friendsRelation.setFriend(false);
             friendsRelation.setIdUser1(myid);
             friendsRelation.setIdUser2(frndid);
             DaoFactory.getInstance().getFriendRelationDao().saveFriendsRelation(friendsRelation);
         }
         else if(fr.isFriend()==false){
             return createResultDTOEmptyResponse(ErrorCodes.WARN_FRNDREQ_01);
         }
         else if(fr.isFriend()==true){
             return createResultDTOEmptyResponse(ErrorCodes.WARN_FRNDREQ_02);
         } 
         
         
         //crea oggetto notifica e salvo notifica
         Notification n = new Notification();
         n.setIdFriend(myid);
         String friendName;
         friendName = generateDysplayName(me);
         n.setIdUser(frndid);
         n.setFriendName(friendName);
         n.setType(NotificationCodes.FRIEND_REQUEST.getType());
         n.setStatus(NotificationStatusCodes.UNREAD.getStatus());
         DaoFactory.getInstance().getNotificationDao().saveNotification(n);
        // return new SuccessPost();
         return  createResultDTOEmptyResponse(InfoCodes.OK_FRNDREQ_00);
     }
 
     private String generateDysplayName(User me) {
         String displayName;
         if(me.getDisplayName()!=null && !me.getDisplayName().trim().equals("")){
             displayName = me.getDisplayName();
         }
         else if((me.getLastName()==null && me.getFirstName()==null) 
                 || (me.getLastName().trim().equals("") && me.getFirstName().trim().equals("")) ){
             displayName = me.getUsername();
         }
         else{
             String firstname = me.getFirstName()==null?"": me.getFirstName();
             String lastname = me.getLastName()==null?"": me.getLastName();
             displayName = firstname + " "+ lastname;
         }
         return displayName;
     }
     
     @POST
     @Path("/frndConfirm")
     @Consumes("application/x-www-form-urlencoded")
     @Produces("application/json")
     public Object frndConfirm(@Form FriendReqDTO c, @Context HttpServletRequest httpReq) 
     {
         if (c == null){
             return createResultDTOEmptyResponse(ErrorCodes.FRND_MISSED_PARAM);
         }
         
         String frndid = c.getIdRequestor();
         String myid = c.getIdRequested();
         
         
         User me = DaoFactory.getInstance().getUserDao().findUserByIdUser(myid);
         User frnd = DaoFactory.getInstance().getUserDao().findUserByIdUser(frndid);
         
         String username = me.getUsername();
         if (username == null || !username.equals(httpReq.getHeader("btUsername"))){
             return createResultDTOEmptyResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         
         //IMPOSTO A TRUE LA RELATION DI AMICIZIA
         FriendsRelation fr = DaoFactory.getInstance().getFriendRelationDao().getFriendsRelation(frndid, myid);
         if(fr == null){
             return  createResultDTOEmptyResponse(ErrorCodes.WARN_FRNDCONFIRM_00);
         }
         fr.setFriend(true);
         DaoFactory.getInstance().getFriendRelationDao().updateFriendsRelation(fr);
         
         //INCREMENTO COUNTER AMICI
         me.setCounterFriends(me.getCounterFriends()+1);
         frnd.setCounterFriends(frnd.getCounterFriends()+1);
         DaoFactory.getInstance().getUserDao().updateUser(me);
         DaoFactory.getInstance().getUserDao().updateUser(frnd);
         
         //CERCO LA RELAZIONE DI AMICIZIA INVERSA E LA METTO A TRUE, SE NON C'E' LA CREO
         FriendsRelation fr_inv = DaoFactory.getInstance().getFriendRelationDao().getFriendsRelation(myid, frndid);
         if(fr_inv == null){
             FriendsRelation friendsRelation = new FriendsRelation();
             friendsRelation.setFriend(true);
             friendsRelation.setIdUser1(myid);
             friendsRelation.setIdUser2(frndid); 
             DaoFactory.getInstance().getFriendRelationDao().saveFriendsRelation(friendsRelation);
         } else{
             fr_inv.setFriend(true);
             DaoFactory.getInstance().getFriendRelationDao().updateFriendsRelation(fr_inv);
         }
         
         //CREO LE ACTIVITY PER I DUE UTENTI
         Activity myAct = new Activity();
         myAct.setDate(new Date());
         myAct.setIdFriend(frndid);
         myAct.setIdUser(myid);
         String friendName;
         if(frnd.getDisplayName()!=null && !frnd.getDisplayName().trim().equals("")){
             friendName = frnd.getDisplayName();
         }
         else if((frnd.getLastName()==null && frnd.getFirstName()==null) 
                 || (frnd.getLastName().trim().equals("") && frnd.getFirstName().trim().equals("")) ){
             friendName = frnd.getUsername();
         }
         else{
             String firstname = frnd.getFirstName()==null?"": frnd.getFirstName();
             String lastname = frnd.getLastName()==null?"": frnd.getLastName();
             friendName = firstname + " "+ lastname;
         }
         myAct.setFriendName(friendName);
         myAct.setType(ActivityCodes.FRIEND_CONFIRM.getType());
         DaoFactory.getInstance().getActivityDao().saveActivity(myAct);
         
         
         Activity friendAct = new Activity();
         friendAct.setDate(new Date());
         friendAct.setIdFriend(myid);
         friendAct.setIdUser(frndid);
         String friendName2;
         if(me.getDisplayName()!=null && !me.getDisplayName().trim().equals("")){
             friendName2 = me.getDisplayName();
         }
         else if((me.getLastName()==null && me.getFirstName()==null) 
                 || (me.getLastName().trim().equals("") && me.getFirstName().trim().equals("")) ){
             friendName2 = me.getUsername();
         }
         else{
             String firstname = me.getFirstName()==null?"": me.getFirstName();
             String lastname = me.getLastName()==null?"": me.getLastName();
             friendName2 = firstname + " "+ lastname;
         }
         friendAct.setFriendName(friendName2);
         friendAct.setType(ActivityCodes.FRIEND_CONFIRM.getType());
         DaoFactory.getInstance().getActivityDao().saveActivity(friendAct);
         
         //CREO LE DUE NOTIFICHE
         Notification n = new Notification();
         n.setIdFriend(frndid);
         n.setIdUser(myid);
         n.setFriendName(friendName);
         n.setType(NotificationCodes.FRIEND_CONFIRM.getType());
         n.setStatus(NotificationStatusCodes.UNREAD.getStatus());
         DaoFactory.getInstance().getNotificationDao().saveNotification(n);
         
         Notification n2 = new Notification();
         n2.setIdFriend(myid);
         n2.setIdUser(frndid);
         n2.setFriendName(friendName2);
         n2.setType(NotificationCodes.FRIEND_CONFIRM.getType());
         n2.setStatus(NotificationStatusCodes.UNREAD.getStatus());
         DaoFactory.getInstance().getNotificationDao().saveNotification(n2);
         //return new SuccessPost();
         return createResultDTOEmptyResponse(InfoCodes.OK_FRNDCONFIRM_00);
     }
     
     @POST
     @Path("/frndRefuse")
     @Consumes("application/x-www-form-urlencoded")
     @Produces("application/json")
     public Object frndRefuse(@Form FriendReqDTO c, @Context HttpServletRequest httpReq) 
     {
         if (c == null){
             return createResultDTOEmptyResponse(ErrorCodes.FRND_MISSED_PARAM);
         }
         //viene impostato a read la notifica di richiesta d'amicizia
         
         String friendid = c.getIdRequestor();
         String myid = c.getIdRequested();
         
         User me = DaoFactory.getInstance().getUserDao().findUserByIdUser(myid);
         String username = me.getUsername();
         if (username == null || !username.equals(httpReq.getHeader("btUsername"))){
             return createResultDTOEmptyResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         
         //ELIMINARE LA NOTIFICA
         ArrayList<Notification> an = DaoFactory.getInstance().getNotificationDao().findByUser(myid);
         for(int i =0 ;i<an.size();i++){
             Notification n = an.get(i);
             if(n.getIdFriend().equals(friendid) && n.getType()==NotificationCodes.FRIEND_REQUEST.getType()){
                 DaoFactory.getInstance().getNotificationDao().deleteNotificationByMongoID(n.getId().toString());
             }
             return createResultDTOEmptyResponse(InfoCodes.OK_FRNDREFUSE_00);
         }
         return createResultDTOEmptyResponse(ErrorCodes.FRND_REFUSE_ERROR);
         /*FriendsRelation fr = DaoFactory.getInstance().getFriendRelationDao().getFriendsRelation(friendid, myid);
         if(fr!=null){
             DaoFactory.getInstance().getFriendRelationDao().deleteFriendship(friendid, myid);
             return createResultDTOEmptyResponse(InfoCodes.OK_FRNDREFUSE_00);
         }
         else{
             return createResultDTOEmptyResponse(ErrorCodes.FRND_REFUSE_ERROR);
         }*/
         /*User me = DaoFactory.getInstance().getUserDao().findById(id1);
         String username = me.getUsername();
         if (username != null && username.equals(httpReq.getHeader("btUsername")))
         {
             DaoFactory.getInstance().getFriendRelationReqDao().deleteFriendRelationReq(id1, id2);
             DaoFactory.getInstance().getFriendRelationDao().deleteFriendship(id1, id2);
             return createResultDTOEmptyResponse("OK_FRNDREFUSE_00","Amicizia rimossa con successo",true);
         }
         
         me = DaoFactory.getInstance().getUserDao().findById(id2);
         username = me.getUsername();
         if (username != null && username.equals(httpReq.getHeader("btUsername")))
         {
             DaoFactory.getInstance().getFriendRelationReqDao().deleteFriendRelationReq(id1, id2);
             DaoFactory.getInstance().getFriendRelationDao().deleteFriendship(id1, id2);
              return createResultDTOEmptyResponse("OK_FRNDREFUSE_00","Amicizia rimossa con successo",true);
         }*/
         
         //return createResultDTOEmptyResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
     }
    
     @GET
     @Path("/setNotificationRead_jsonp")
     @Produces("application/json")
     public JSONPObject setNotificationRead_jsonp(@QueryParam("idNotification") final String idNotification, 
                                  @Context HttpServletRequest httpReq,
 	@DefaultValue("callback") @QueryParam("callback") String callbackName) {
         return new JSONPObject(callbackName,setNotificationRead(idNotification,httpReq));
     }
     
     @GET
     @Path("/setNotificationRead")
     @Produces("application/json")
     public ResultDTO setNotificationRead(@QueryParam("idNotification") final String idNotification, 
                                  @Context HttpServletRequest httpReq) {
         Notification n = DaoFactory.getInstance().getNotificationDao().findById(idNotification);
         if(n==null){
             return createResultDTOEmptyResponse(ErrorCodes.UPDATE_NOTIFICANION_ERROR_00);
         }
          // Blocco richieste di un utente per un altro
         if (n.getIdUser() == null || !n.getIdUser().equals(httpReq.getHeader("btUsername"))){
             return createResultDTOEmptyResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         n.setStatus(NotificationStatusCodes.READ.getStatus());
         DaoFactory.getInstance().getNotificationDao().setNotificationRead(n);
         return createResultDTOEmptyResponse(InfoCodes.OK_NOTIFICATION_00);
     }
     /*
      * sendFeedback
      */
     
     @POST
     @Path("/saveFeedback")
     @Consumes("application/x-www-form-urlencoded")
     @Produces("application/json")
     public Object saveFeedback(@Form FeedbackDTO feedback, @Context HttpServletRequest httpReq)
     {
         if (feedback.getIdUser() == null)
         {
             return createResultDTOEmptyResponse(ErrorCodes.NULL_USER);
         }
          // Blocco richieste di un utente per un altro
          if (feedback.getIdUser().equals(httpReq.getHeader("btUsername"))){
             return createJsonErrorResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         Feedback f=new Feedback();
         f.setIdActivity(feedback.getIdActivity());
         f.setIdUser(feedback.getIdUser());
         f.setInsertedOn(new Date());
         f.setType(feedback.getType());
         f.setComment(feedback.getComment());
         if(DaoFactory.getInstance().getFeedbackDao().saveFeedback(f)>0)
         {
            // return new SuccessPost();
             return createResultDTOEmptyResponse(InfoCodes.OK);
         }
         else
             return createResultDTOEmptyResponse(ErrorCodes.SAVE_FEEDBACK_ERROR);
     }
     /*
      * getFeedback
      */
     
     @GET
     @Path("/getFeedback")
     @Produces("application/json")
     public ResultDTO getFeedback (@QueryParam("idActivity") final String idActivity, 
                                  @Context HttpServletRequest httpReq) {
         if(idActivity==null)
         {
             return createResultDTOEmptyResponse(ErrorCodes.NULL_ACTIVITY);
         }
         List<Feedback> list = DaoFactory.getInstance().getFeedbackDao().findByIdActivity(idActivity);
         return createResultDTOResponseOk(list);
     }
     
     
     
     
     @GET
     @Path("/findFrndReqs")
     @Consumes("application/json")
     @Produces("application/json")
     public Response findFrndReqs(@QueryParam("username") final String username, 
                                  @Context HttpServletRequest httpReq) 
     {
         // Blocco richieste di un utente per un altro
         if (username == null || !username.equals(httpReq.getHeader("btUsername"))){
             return createJsonErrorResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         User u = DaoFactory.getInstance().getUserDao().findUserByUsername(username);
         List<String> pendingReqs = DaoFactory.getInstance().getFriendRelationReqDao().findPendingReqs(u.getIdUser());
         return createJsonOkResponse(pendingReqs);
     }
     
     /**
      * Metodo di echo di prova per verifica di sessione.
      * 
      * @param value
      * @return 
      */
     @GET
     @Path("/echo")
     @Produces("text/html")
     public String echo (@DefaultValue("puppa") @QueryParam("value") String value) 
     {
         return value;
     }
 
     private ArrayList<Place> convertToPlace(ArrayList<Location> locations) {
          ArrayList<Place> list = new ArrayList<Place>();
         for(Location l : locations)
         {
             Place p=new Place();
             p.setIdPlace(l.getIdLocation());
             p.setPlaceName(l.getName());
             p.setLat(l.getPos().get(0).toString());
             p.setLng(l.getPos().get(1).toString());
             p.setCategory((l.getCategories()!=null&& l.getCategories().size()>0?l.getCategories().get(0):null));
             p.setUrl(l.getUrl());
             p.setCc(l.getCc());
             list.add(p);
         }
         return list;
     }
      
     protected static Response createJsonOkResponse(Object o) {
         Response.ResponseBuilder builder = Response.ok(o, MediaType.APPLICATION_JSON);
         return builder.build();
     }
     /*
      * crea risposta senza body impostando status, per aggiungere metadata eseguire
      *  result.getResponse().getMetaData().setBadge("OK", 1, "Notification OK");
      *   result.getResponse().getMetaData().setNotification("OK", 1, "Notification OK");
      */
      private ResultDTO createResultDTOEmptyResponse(int code,String msg,Boolean success) {
         ResultDTO result = new ResultDTO();
         Status status= new Status();
         status.setCode(code);
         status.setMsg(msg);
         status.setSuccess(success);
         Metadata metaData = new Metadata();
         it.antreem.birretta.service.model.json.Response response = new it.antreem.birretta.service.model.json.Response(status, null, metaData);
         result.setResponse(response);
         return result;
      }
       /*
      * crea risposta senza body impostando status in base all'errore fornito,
      * per aggiungere metadata eseguire
      *  result.getResponse().getMetaData().setBadge("OK", 1, "Notification OK");
      *   result.getResponse().getMetaData().setNotification("OK", 1, "Notification OK");
      */
      private ResultDTO createResultDTOEmptyResponse(ErrorCodes e) {
         ResultDTO result = new ResultDTO();
         Status status= new Status();
         status.setCode(e.getCode());
         status.setMsg(e.getMessage());
         status.setSuccess(true);
         Metadata metaData = new Metadata();
         it.antreem.birretta.service.model.json.Response response = new it.antreem.birretta.service.model.json.Response(status, null, metaData);
         result.setResponse(response);
         log.error(e.getCode()+" - "+ e.getTitle()+" - "+e.getMessage());
         return result;
      }
       /*
      * crea risposta senza body impostando status in base all'info fornita,
      * per aggiungere metadata eseguire
      *  result.getResponse().getMetaData().setBadge("OK", 1, "Notification OK");
      *   result.getResponse().getMetaData().setNotification("OK", 1, "Notification OK");
      */
      private ResultDTO createResultDTOEmptyResponse(InfoCodes i) {
         ResultDTO result = new ResultDTO();
         Status status= new Status();
         status.setCode(i.getCode());
         status.setMsg(i.getMessage());
         status.setSuccess(true);
         Metadata metaData = new Metadata();
         it.antreem.birretta.service.model.json.Response response = new it.antreem.birretta.service.model.json.Response(status, null, metaData);
         result.setResponse(response);
         return result;
      }
      private ResultDTO createResultDTOResponseOk(List list) {
         ResultDTO result = new ResultDTO();
         Status status= new Status();
         status.setCode(100);
         status.setMsg("Status OK");
         status.setSuccess(true);
         Body body =new Body();
         body.setList(list);
         Metadata metaData = new Metadata();
    //     metaData.setBadge("OK", 1, "Notification OK");
      //   metaData.setNotification("OK", 1, "Notification OK");
         it.antreem.birretta.service.model.json.Response response = new it.antreem.birretta.service.model.json.Response(status, body, metaData);
         result.setResponse(response);
         return result;
     }
      private ResultDTO createResultDTOResponseFail(ErrorCodes e) {
         ResultDTO result = new ResultDTO();
         Status status= new Status();
         status.setCode(e.getCode());
         status.setMsg(e.getMessage());
         status.setSuccess(false);
         it.antreem.birretta.service.model.json.Response response = new it.antreem.birretta.service.model.json.Response(status, null, null);
         result.setResponse(response);
         return result;
     }
     protected static Response createJsonErrorResponse(ErrorCodes e, Object... actionType) {
         ErrorDTO err = Utils.createError(e, actionType);
         Response.ResponseBuilder builder = Response.ok(err, MediaType.APPLICATION_JSON);
         return builder.build();
     }
 
     private void addDrinkedIn(ArrayList<Place> places) {
        for(Place place: places)
        {
            place.setDrinkedIn(DaoFactory.getInstance().getDrinkDao().countDrinksByPlace(place.getIdPlace()));
        }
     }
 }
