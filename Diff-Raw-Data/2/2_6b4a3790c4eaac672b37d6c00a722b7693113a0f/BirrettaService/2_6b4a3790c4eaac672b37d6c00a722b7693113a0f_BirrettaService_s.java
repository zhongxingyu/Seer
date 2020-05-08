 package it.antreem.birretta.service;
 
 import it.antreem.birretta.service.model.json.Body;
 import it.antreem.birretta.service.model.json.Metadata;
 import it.antreem.birretta.service.model.json.Status;
 import it.antreem.birretta.service.model.json.BeerSingle;
 import it.antreem.birretta.service.dao.DaoException;
 import it.antreem.birretta.service.dao.DaoFactory;
 import it.antreem.birretta.service.dto.*;
 import it.antreem.birretta.service.model.*;
 import it.antreem.birretta.service.util.ActivityCodes;
 import it.antreem.birretta.service.util.ErrorCodes;
 import it.antreem.birretta.service.util.JsonHandler;
 import it.antreem.birretta.service.util.NotificationCodes;
 import it.antreem.birretta.service.util.NotificationStatusCodes;
 import it.antreem.birretta.service.util.Utils;
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
 /**
  * BirrettaService
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
      * @param c Credenziali dell'utente, ovvero username + password.
      * @return Esito della login
      */
     @POST
     @Path("/login")
     @Consumes("application/x-www-form-urlencoded")
     @Produces("application/json")
     public Response login(@FormParam("username") String username,@FormParam("password") String password) 
     {
         // Pre-conditions
         if (username == null || password==null || username.equals("") || password.equals(""))
         {
             log.debug("Credenziali di login passate a null. Errore.");
             return createJsonErrorResponse(ErrorCodes.LOGIN_FAILED);
         }
         
         LoginResponseDTO response = new LoginResponseDTO();
         
   //      String username = c.getUsername() != null ? c.getUsername() : "";
    //     String password = c.getPassword() != null ? c.getPassword() : "";
         String hash = Utils.SHAsum(Utils.SALT.concat(username).concat(password).getBytes());
         
         log.info("Tentativo di login di username: " + username + " con hash pwd: " + hash); 
         
         /*
          * Login failed
          *  - return an error
          */
         User u = DaoFactory.getInstance().getUserDao().findUserByUsername(username);
         if (u == null || !u.getPwdHash().equals(hash)){
             response.setSuccess(false);
             response.setMessage("Login fallito. Credenziali utente errate.");
             response.setSessionId(null);
 
             return createJsonOkResponse(response);
         }
         
         /*
          * Login successful
          *  - is there an active session? use that one
          *  - else create a new session
          */
         Session s = DaoFactory.getInstance().getSessionDao().findSessionByUsername(username);
         if (s == null) {
             s = new Session();
             s.setUsername(username);
             s.setSid(UUID.randomUUID().toString());
             s.setTimestamp(new Date());
             DaoFactory.getInstance().getSessionDao().saveSession(s);
         }
         
         response.setSuccess(true);
         response.setMessage("Login eseguito correttamente.");
         response.setSessionId(s.getSid());
         
         return createJsonOkResponse(response);
     }
     
     @POST
     @Path("/logout")
     @Consumes("application/json")
     @Produces("application/json")
     public Response logout(LogoutRequestDTO req, @Context HttpServletRequest httpReq) 
     {
         // Pre-conditions
         if (req == null)
         {
             log.debug("Parametri di logout passati a null. Errore.");
             return createJsonErrorResponse(ErrorCodes.LOOUT_FAILED);
         }
         
         // Blocco richieste di un utente per un altro
         if (!req.getUsername().equals(httpReq.getHeader("btUsername"))){
             return createJsonErrorResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         
         // Delete any existing session
         Session s = DaoFactory.getInstance().getSessionDao().findSessionByUsername(req.getUsername());
         if (s != null){
             DaoFactory.getInstance().getSessionDao().deleteSessionBySid(s.getSid());
         }
         
         GenericResultDTO res = new GenericResultDTO();
         res.setSuccess(true);
         res.setMessage("Logout eseguito con successo");
         
         return createJsonOkResponse(res);
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
     @Produces("application/json")
     public ResultDTO saveUser(UpdateUserRequestDTO r){
         User u = DaoFactory.getInstance().getUserDao().findUserByUsername(r.getEmail());
         if(u==null){
             User newuser = new User();
             newuser.setIdUser(r.getIdUser());
             newuser.setBirthDate(r.getBirthDate());
             newuser.setEmail(r.getEmail());
             //newuser.setFirstName(r.getFirstName());
             //newuser.setLastName(r.getLastName());
             newuser.setDisplayName(r.getDisplayName());
             newuser.setGender(r.getGender());
             newuser.setUsername(r.getEmail());//da verificare
             newuser.setNationality(r.getNationality());
             DaoFactory.getInstance().getUserDao().saveUser(newuser);
             return  createResultDTOEmptyResponse(InfoCodes.OK_SAVEUSER_00);
         }
         return createResultDTOEmptyResponse(ErrorCodes.REG_FAILED);
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
         ArrayList<User> list = new ArrayList<User>();
         list.add(DaoFactory.getInstance().getUserDao().findUserByUsername(username));
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
 	@QueryParam("maxElement") final String _maxElemet,@QueryParam("id_user") final String id_user,
 	@DefaultValue("callback") @QueryParam("callback") String callbackName)
     {
 		return new JSONPObject(callbackName,listFriend(_maxElemet,id_user));
 	}
      /**
      * Restituisce i miei amici e i relativi dettagli in formato JSON.
      */
     @GET
     @Path("/listFriend")
     @Produces("application/json")
     public ResultDTO listFriend (@QueryParam("maxElement") final String _maxElemet,@QueryParam("id_user") final String id_user)
     {
         log.info("reuest list of "+_maxElemet+" friend of "+id_user);
         int maxElemet = _maxElemet == null ? -1 : new Integer(_maxElemet);
         ArrayList<Friend> list = DaoFactory.getInstance().getFriendDao().getAllMyFriends(maxElemet,id_user);
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
 	@DefaultValue("callback") @QueryParam("callback") String callbackName)
     {
 		return new JSONPObject(callbackName,listFriendActivity(_maxElemet,idUser));
 	}
      /**
      * Restituisce le attività dei miei amici in formato JSON.
      */
     @GET
     @Path("/listFriendActivity")
     @Produces("application/json")
     public ResultDTO listFriendActivity (@QueryParam("maxElement") final String _maxElemet,@QueryParam("idUser") final String idUser)
     {
         log.info("reuest list of "+_maxElemet+"activity of friend of "+idUser);
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
         return createResultDTOResponseOk(list);  
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
     @Path("/findLocType")
     @Produces("application/json")
     public Response findLocType (@QueryParam("cod") final String _cod)
     {
         String cod = _cod == null ? "" : _cod;
         List<LocType> list = DaoFactory.getInstance().getLocTypeDao().findLocTypesByCodLike(cod);
         return createJsonOkResponse(list);
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
         ArrayList<Location> findLocationNear = JsonHandler.findLocationNear(lat, lon, radius);
       // modalità solo da mongoDB
       //  List<Location> list = DaoFactory.getInstance().getLocationDao().findLocationNear(lat, lon, radius);
      //   ArrayList<Location> arrayList= new ArrayList<Location>();
      //   arrayList.addAll(list);
         log.info("foursquare loc "+findLocationNear.size());
         ArrayList<Place> places= convertToPlace(findLocationNear);
         //TODO: aggiunta per ogni place del numero di bevute(drink)
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
     @Path("/findLocTypeById")
     @Produces("application/json")
     public Response findLocTypeById (@QueryParam("id") final String _id)
     {
         String id = _id == null ? "" : _id;
         LocType l = DaoFactory.getInstance().getLocTypeDao().findById(id);
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
         ResultDTO result = new ResultDTO();
         Status status= new Status();
         status.setCode(100);
         status.setMsg("Status OK");
         status.setSuccess(true);
         Body body =new Body<Beer>();
         body.setList(list);
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
     @Path("/listDrink_jsonp")
     @Produces("application/json")
     public JSONPObject listDrink_jsonp (
 	@QueryParam("maxElement") final String _maxElemet,
 	@DefaultValue("callback") @QueryParam("callback") String callbackName)
     {
 		return new JSONPObject(callbackName,listDrink(_maxElemet));
 	}
      /**
      * Restituisce le birre con tutti i relativi dettagli in formato JSON.
      */
     @GET
     @Path("/listDrink")
     @Produces("application/json")
     public ResultDTO listDrink (@QueryParam("maxElement") final String _maxElemet)
     {
         log.info("request list of "+_maxElemet+" drink");
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
 	@QueryParam("id_user") final String id_user,
 	@DefaultValue("callback") @QueryParam("callback") String callbackName)
     {
 		return new JSONPObject(callbackName,listDrink(id_user));
 	}
      /**
      * Restituisce le birre con tutti i relativi dettagli in formato JSON.
      */
     @GET
     @Path("/listNotification")
     @Produces("application/json")
     public ResultDTO listNotification (@QueryParam("idUser") final String idUser)
     {
         log.info("reuest list of "+idUser+" notifications");
         ArrayList<Notification> list = DaoFactory.getInstance().getNotificationDao().findByUser(idUser);
        
         return createResultDTOResponseOk(list);
         
     }
      /**
      * Restituisce le birre con tutti i relativi dettagli in formato JSONP.
      */
     @GET
     @Path("/listMyActivity_jsonp")
     @Produces("application/json")
     public JSONPObject listMyActivity_jsonp (
 	@QueryParam("idUser") final String idUser,
 	@DefaultValue("callback") @QueryParam("callback") String callbackName)
     {
 		return new JSONPObject(callbackName,listMyActivity(idUser));
 	}
      /**
      * Restituisce le birre con tutti i relativi dettagli in formato JSON.
      */
     @GET
     @Path("/listMyActivity")
     @Produces("application/json")
     public ResultDTO listMyActivity (@QueryParam("idUser") final String idUser)
     {
         log.info("request list of "+idUser+" activity");
         ArrayList<Activity> list = DaoFactory.getInstance().getActivityDao().findByUser(idUser);
        
         return createResultDTOResponseOk(list);
         
     }
     @POST
     @Path("/insertBeer")
     @Consumes("application/json")
     @Produces("application/json")
     public ResultDTO insertBeer(Beer b) 
     {
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
         Beer _b = DaoFactory.getInstance().getBeerDao().findBeerByName(b.getName());
         if (_b != null){
             return createResultDTOEmptyResponse(ErrorCodes.INSBEER_BEER_DUP);
         }
         
         // Inserimento su DB
         DaoFactory.getInstance().getBeerDao().saveBeer(b);
         //inserimento attività
         Activity a=new Activity();
         a.setBeerName(b.getName());
         a.setIdBeer(b.getIdBeer());
         a.setDate(new Date());
         a.setType(ActivityCodes.BEER_CREATED.getType());
         a.setDisplayName(b.getUsername());
         DaoFactory.getInstance().getActivityDao().saveActivity(a);
         return createResultDTOEmptyResponse(InfoCodes.OK_INSERTBEER_00);
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
     @Consumes("application/json")
     @Produces("application/json")
     public ResultDTO checkIn(CheckInRequestDTO c, @Context HttpServletRequest httpReq) 
     {
         // Pre-conditions + controllo validita' parametri
         if (c == null){
             return createResultDTOEmptyResponse(ErrorCodes.CHECKIN_WRONG_PARAM);
         }
         if (c.getUsername() == null || c.getIdBeer() == null || c.getIdLocation() == null){
             return createResultDTOEmptyResponse(ErrorCodes.CHECKIN_WRONG_PARAM);
         }
         /* Puo' esserci una bevuta senza voto? per ora si'...
         // TODO: Eventuale check di check-in senza voto
         if (c.getIdFeedback() != null && (c.getScore() < 0 || c.getScore() > 10)){
             return createJsonErrorResponse(ErrorCodes.CHECKIN_WRONG_PARAM);
         }
         */
         // Blocco richieste di un utente per un altro
         if (!c.getUsername().equals(httpReq.getHeader("btUsername"))){
             return createResultDTOEmptyResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         
         // Recupero dati necessari (utente dovrebbe essere ok perche' ha passato il controllo precedente)
         User u = DaoFactory.getInstance().getUserDao().findUserByUsername(c.getUsername());
         Beer b = DaoFactory.getInstance().getBeerDao().findById(c.getIdBeer());
         Location l = DaoFactory.getInstance().getLocationDao().findById(c.getIdLocation());
         
         // Controllo che location e birra effettivamente esistano
         if (b == null){
             return createResultDTOEmptyResponse(ErrorCodes.CHECKIN_WRONG_PARAM);
         }
         if (l == null){
             return createResultDTOEmptyResponse(ErrorCodes.CHECKIN_WRONG_PARAM);
         }
         
         // Controllo che negli ultimi 10 minuti non ci siano piu' di tre bevute
         List<Drink> lastDrinks = DaoFactory.getInstance().getDrinkDao().findRecentDrinks(u.getUsername(), 10);
         if (lastDrinks.size() >= 3){
             return createResultDTOEmptyResponse(ErrorCodes.CHECKIN_TOO_MANY_DRINKS);
         }
         
         // Preparazione oggetto di modello
         Drink d = new Drink();
         d.setIdBeer(c.getIdBeer());
         //verificare esistenza birra e impostare nome
         d.setIdPlace(c.getIdLocation());
          //  d.setPlaceName(c.getPlaceName());
         //verificare esistenza location e impostare nome
         d.setIdUser(u.getIdUser());
         //verificare esistenza utnete e set nome
         d.setImage(c.getPicture());
      
         d.setInsertedOn(new Date());
         
         // Scrittura su DB
         DaoFactory.getInstance().getDrinkDao().saveDrink(d);
                 
         // Ricerca di nuovi badge e premi da assegnare scatenati da questo check-in
         List<Badge> newBadges = Utils.checkBadges(c.getUsername());
         if (newBadges != null && !newBadges.isEmpty()){
             DaoFactory.getInstance().getBadgeDao().saveUserBadges(c.getUsername(), newBadges);
         }
         
         // Controllo mayorships + notifiche a chi le ha perdute
         // TODO: controllo mayorships + notifiche a chi le ha perdute
         //TODO: creare attività
          //inserimento attività
         Activity a=new Activity();
         a.setBeerName(b.getName());
         a.setIdBeer(b.getIdBeer());
         a.setDate(new Date());
         a.setType(ActivityCodes.CHECKIN.getType());
         a.setDisplayName(b.getUsername());
         a.setIdPlace(l.getIdLocation());
         a.setPlaceName(l.getName());
         a.setIdUser(u.getIdUser());
         String displayName;
         if(u.getDisplayName()!=null && !u.getDisplayName().trim().equals("")){
             displayName = u.getDisplayName();
         }
         else if((u.getLastName()==null && u.getFirstName()==null) 
                 || (u.getLastName().trim().equals("") && u.getFirstName().trim().equals("")) ){
             displayName = u.getUsername();
         }
         else{
             String firstname = u.getFirstName()==null?"": u.getFirstName();
             String lastname = u.getLastName()==null?"": u.getLastName();
             displayName = firstname + " "+ lastname;
         }
         a.setDisplayName(displayName);
         DaoFactory.getInstance().getActivityDao().saveActivity(a);
         return createResultDTOEmptyResponse(InfoCodes.OK_CHECKIN_00);
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
     @Path("/findMyBadges")
     @Produces("application/json")
     public Response findMyBadges (@QueryParam("username") final String username, 
                                   @Context HttpServletRequest httpReq)
     {
         // Blocco richieste di un utente per un altro
         if (username == null || !username.equals(httpReq.getHeader("btUsername"))){
             return createJsonErrorResponse(ErrorCodes.REQ_DELEGATION_BLOCKED);
         }
         
         List<Badge> list = DaoFactory.getInstance().getBadgeDao().findUserBadges(username);
         return createJsonOkResponse(list);
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
     @Consumes("application/json")
     @Produces("application/json")
     public ResultDTO frndReq(FriendReqDTO c, @Context HttpServletRequest httpReq) 
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
         if(me.getDisplayName()!=null && !me.getDisplayName().trim().equals("")){
             friendName = me.getDisplayName();
         }
         else if((me.getLastName()==null && me.getFirstName()==null) 
                 || (me.getLastName().trim().equals("") && me.getFirstName().trim().equals("")) ){
             friendName = me.getUsername();
         }
         else{
             String firstname = me.getFirstName()==null?"": me.getFirstName();
             String lastname = me.getLastName()==null?"": me.getLastName();
             friendName = firstname + " "+ lastname;
         }
         n.setIdUser(frndid);
         n.setFriendName(friendName);
         n.setType(NotificationCodes.FRIEND_REQUEST.getType());
         n.setStatus(NotificationStatusCodes.UNREAD.getStatus());
         DaoFactory.getInstance().getNotificationDao().saveNotification(n);
         
         return  createResultDTOEmptyResponse(InfoCodes.OK_FRNDREQ_00);
     }
     
     @POST
     @Path("/frndConfirm")
     @Consumes("application/json")
     @Produces("application/json")
     public ResultDTO frndConfirm(FriendReqDTO c, @Context HttpServletRequest httpReq) 
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
        
         return createResultDTOEmptyResponse(InfoCodes.OK_FRNDCONFIRM_00);
     }
     
     @POST
     @Path("/frndRefuse")
     @Consumes("application/json")
     @Produces("application/json")
     public ResultDTO frndRefuse(FriendReqDTO c, @Context HttpServletRequest httpReq) 
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
     @Consumes("application/json")
     @Produces("application/json")
     public JSONPObject setNotificationRead_jsonp(@QueryParam("idNotification") final String idNotification, 
                                  @Context HttpServletRequest httpReq,
 	@DefaultValue("callback") @QueryParam("callback") String callbackName) {
         return new JSONPObject(callbackName,setNotificationRead(idNotification,httpReq));
     }
     @GET
     @Path("/setNotificationRead")
     @Consumes("application/json")
     @Produces("application/json")
     public ResultDTO setNotificationRead(@QueryParam("idNotification") final String idNotification, 
                                  @Context HttpServletRequest httpReq) {
         Notification n = DaoFactory.getInstance().getNotificationDao().findById(idNotification);
         if(n==null){
             return createResultDTOEmptyResponse(ErrorCodes.UPDATE_NOTIFICANION_ERROR_00);
         }
         n.setStatus(NotificationStatusCodes.READ.getStatus());
        
         return createResultDTOEmptyResponse(InfoCodes.OK_NOTIFICATION_00);
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
             p.setCategory((l.getCategories()!=null?l.getCategories().get(0):null));
             p.setUrl(l.getUrl());
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
         status.setSuccess(false);
         Metadata metaData = new Metadata();
         it.antreem.birretta.service.model.json.Response response = new it.antreem.birretta.service.model.json.Response(status, null, metaData);
         result.setResponse(response);
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
      private ResultDTO createResultDTOResponseOk(ArrayList list) {
         ResultDTO result = new ResultDTO();
         Status status= new Status();
         status.setCode(100);
         status.setMsg("Status OK");
         status.setSuccess(true);
         Body body =new Body();
         body.setList(list);
         Metadata metaData = new Metadata();
         metaData.setBadge("OK", 1, "Notification OK");
         metaData.setNotification("OK", 1, "Notification OK");
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
 }
