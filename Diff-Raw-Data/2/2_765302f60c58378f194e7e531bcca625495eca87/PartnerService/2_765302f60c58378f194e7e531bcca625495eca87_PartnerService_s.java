 package project.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 
 import org.slim3.controller.validator.Validators;
 import org.slim3.datastore.Datastore;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Transaction;
 import project.meta.PartnerMeta;
 import project.meta.ProgettoMeta;
 import project.model.Partner;
 import project.model.PartnerProgetto;
 import project.model.Progetto;
 
 /**
  * Classe di servizio per la creazione, memorizzazione, gestione e
  * recupero dei partners.
  * @author Aury
  *
  */
 public class PartnerService {
     
     //TODO Controllare i serivizi che usano il partner identificandolo solo col nome!
 
     private PartnerMeta metaP = PartnerMeta.get();
     private ProgettoMeta metaPro = ProgettoMeta.get();
     private PartnerProgettoService ppService = new PartnerProgettoService();
     
     /**
      * Crea un nuovo Partner, data la Request con tutti i parametri del form settati,
      * e lo inserisce nel Datastore tramite transazione.
      * Controllo prima che non esista gi un partner con quel nome.
      * Non eseguo qui alcun controllo sulla validit dei campi!
      * @param request - La request che contiene il form compilato
      * @return il partner appena creato
      */
     public Partner createPartner(HttpServletRequest request){
         String nome = (String) request.getParameter("nomePartner");
         if (this.getPartnerByName(nome) != null)
                  return this.getPartnerByName(nome);
         Partner p = new Partner();
         p.setNome(nome);
         p.setChiSiamo((String) request.getParameter("chiSiamo"));
         p.setEmail((String) request.getParameter("email"));
         p.setIndirizzo((String) request.getParameter("indirizzo"));
         p.setTelefono((String) request.getParameter("telefono"));
         p.setSitoWeb((String) request.getParameter("sitoWeb"));
         Transaction tx = Datastore.beginTransaction();
         Datastore.put(tx,p);
         tx.commit(); 
         return p;
         }
 
     /**
     * Crea un nuovo Partner, dato solamente il nome
     * e lo inserisce nel Datastore tramite transazione.
     * Controllo prima che non esista gi un partner con quel nome.
     *
     * @param il nome del partner
     * @return il partner appena creato
     */
         public Partner createPartner(String nome){
             if (this.getPartnerByName(nome) != null)
                 return this.getPartnerByName(nome);
             Partner p = new Partner();
             p.setNome(nome);
             Transaction tx = Datastore.beginTransaction();
             Datastore.put(tx,p);
             tx.commit();
             return p;
         }
         
     
     /**
      * Inserisce nel datastore il partner passato per parametro
      * @param p il Partner da inserire
      * @return il partner inserito
      */
     public Partner insertPartner(Partner p){
         Transaction tx = Datastore.beginTransaction();
         Datastore.put(p);
         tx.commit();
         return p;
     }
     
     /**
      * Restituisce il partner identificato dalla chiave, oppure null
      * @param la chiave del partner da cercare nel Datastore
      * @return null oppure il Partner
      */
     public Partner getOrNull(Key key){
         return (key != null) ? Datastore.getOrNull(metaP, key) : null;
     }
     
     /**
      * Restituisce una lista contenente tutti i partners
      * ordinata per nome.
      * @return una classe List<Partner>
      */
     public List<Partner> getAllPartnersList(){
         return Datastore.query(metaP).sort(metaP.nome.asc).asList();
     }
     
     /**
      * Ritorna la lista di tutti i partner che sono leader di qualche progetto
      * @return
      */
     public List<Partner> getAllPartnersLeaderList() {
         List<Partner> partner = new ArrayList<Partner>();
         List<Progetto> progetti = Datastore.query(metaPro).asList();
         Partner tmp = null;
         for (Progetto p: progetti){
             // Inserisco in un blocco try-catch perch non sono sicuro di avere sempre il partner dall'altra parte - metti che qualcuno lo cancella direttamente da db...
             try{
                 tmp = p.getLeaderRef().getModel();
                 if (tmp != null && !partner.contains(tmp)){
                     partner.add(tmp);
                 }
             }catch (Exception e) { }
         }
         return partner;
     }
     
     /**
      * Ritorna una lista di soli Partners che sono Leader di almeno un progetto
      * (senza duplicati).
      * @return Una List di leaders
      */
     public List<Partner> getLeaders(){
         List<Partner> leaders = new ArrayList<Partner>();
         for(Progetto prg: Datastore.query(metaPro).asList())
             if(!leaders.contains(prg.getLeaderRef().getModel()))
                 leaders.add(prg.getLeaderRef().getModel());
         return leaders;            
     }
 
     /**
      * Ritorna un partner, cercandolo per il suo nome
      * 
      * @param partnerName
      * @return
      */
     public Partner getPartnerByName(String partnerName) {
         if (partnerName == null)
             return null;
 
         return Datastore.query(metaP).filter(metaP.nome.equal(partnerName)).asSingle();
     }
     
     /**
      * Ritorna tutti i partner che hanno il nome che inizia con la stringa passata per parametro.
      * Serve per il box di autocompletamento del partner
      *  
      * @param name
      * @return
      */
     public List<Partner> getPartnersByStartName(String name){
         if (name == null)
             return null;
 
         return Datastore.query(metaP).filter(metaP.nome.startsWith(name)).asList();
     }
     
     /**
      * Restituisce la lista dei progetto dei quali  leader
      * il partner la cui chiave  passata come parametro.
      * @param partnerKey La Key del partner.
      * @return la lista dei progetti dei quali il partner  leader.
      * Ritorna null se il Partner non viene trovato.
      */
     public List<Progetto> getLeaded(Key partnerKey){
         Partner partner = Datastore.get(metaP, partnerKey);
         if (partner != null)
             return partner.getLeaderOfListRef().getModelList();
         return null;
     }
     
     /**
      * Ritorna la lista di tutti i progetti nei quali il partner passato per chiave partecipa
      * NON come leader. 
      * @param partnerKey - la chiave del partner
      * @return lista di progetti
      */
     public List<Progetto> getProjectByPartner(Key partnerKey){
         Partner partner = Datastore.get(metaP, partnerKey);
         Progetto tmp = null;
         if (partner != null){
             List<Progetto> progetti = new ArrayList<Progetto>();
             for (PartnerProgetto p: partner.getPartnerProgettoListRef().getModelList()){
                 try{
                     tmp = p.getProgettoRef().getModel();
                     if (!progetti.contains(tmp))
                         progetti.add(tmp);
                 }catch (Exception e) { }
             }
             return progetti;
         }
         return null;
     }
 
     /**
      * Associa progetto e partner creando una istanza della
      * entit PartnerProgetto (per la relazione M-M) e salva tutto
      * nel datastore tramite transazione.
      * @param partner L'entit partner da associare al progetto
      * @param progetto L'entit progetto da associare al partner
      */
     public void setProject(Partner partner, Progetto progetto){
         if (partner == null || progetto == null) 
             return;
         Transaction tx = Datastore.beginTransaction();
         PartnerProgetto pp = new PartnerProgetto();
         pp.getPartnerRef().setModel(partner);
         pp.getProgettoRef().setModel(progetto);
         partner.getPartnerProgettoListRef().getModelList().add(pp);
         progetto.getPartnerProgettoListRef().getModelList().add(pp);
         Datastore.put(tx, partner, progetto,pp);
         tx.commit();
     }
 
     /**
      * Rende il partner passato come primo parametro il leader del progetto
      * passato come secondo parametro. Crea anche la relazione inversa
      * progetto-->leader.
      * @param leader il leader (classe Partner)
      * @param progetto il progetto da assegnare al leader
      * @see PartnerService#setLeader(Key, Key)
      */
     public void setLeader(Partner leader, Progetto progetto){
         progetto.getLeaderRef().setModel(leader);
         leader.getLeaderOfListRef().getModelList().add(progetto);
         Transaction tx = Datastore.beginTransaction();
         Datastore.put(progetto);
         tx.commit();
         tx = Datastore.beginTransaction();
         Datastore.put(leader);
         tx.commit();
     }
     
     /**
      * Rende il partner passato come primo parametro il leader del progetto
      * passato come secondo parametro. Crea anche la relazione inversa
      * progetto-->leader.
      * Utilizza il metodo {@link PartnerService#setLeader(Partner, Progetto)}
      * @param leaderKey la Key del leader
      * @param progettoKey la Key del progetto
      * @see PartnerService#setLeader(Partner, Progetto)
      */
     public void setLeader(Key leaderKey, Key progettoKey){
         this.setLeader(Datastore.get(metaP, leaderKey), Datastore.get(metaPro, progettoKey));
     }
 
     /**
      * Elimina i partners le cui chiavi sono passate
      * come parametro.
      * @param pKeys Una lista di chiavi di partners
      * @return true se tutte le chiavi appartengono a partners e
      * l'operazione ha successo. False, altrimenti.
      */
     public boolean elimina(List<Key> pKeys){
         for(Key k: pKeys){
             if(Datastore.get(metaP,k) == null)
                 return false;
         }
         Transaction tx = Datastore.beginTransaction();
         Datastore.delete(pKeys);
         tx.commit();
         return true;
     }
 
     /**
      * Elimina il partner la cui chiave  passata
      * come paramentro.
      * Elimino anche tutti i collegamenti eventuali con progetti
      * @see PartnerService#elimina(List)
      * @param pKey La Key del Partner da eliminare
      */
     public void elimina(Key pKey){
         Partner p = Datastore.get(metaP,pKey);
         if (p != null){
             //prima controllo che non sia un partner leader di un progetto
             if (this.getLeaders().contains(p)){
                 return;
             }
             // poi elimino tutti i collegamenti con i partner
             List<Progetto> progetti = ppService.getProgettoByPartner(p);
             for (Progetto pr: progetti){
                ppService.eliminaCollegamento(pr.getKey(), pKey);
             }
             //quindi lo elimino dal db
             Transaction tx = Datastore.beginTransaction();
             Datastore.delete(pKey);
             tx.commit();
         }
     }
 
     
     /**
     * Valida i dati inseriti nel pannello di amministrazione per l'inserimento di un nuovo partner.
     * Sono RICHIESTI: nome, mail
     * Sono OPZIONALI tutti gli altri parametri
     * @param request
     * @param meta
     * @return true se i campi sono OK, false altrimenti.
     */
     public boolean validate(HttpServletRequest request, PartnerMeta meta) {
                     
         Validators v = new Validators(request);
         v.add(meta.nome, v.required());
         v.add(meta.email, v.required());
         v.add(meta.email, v.regexp(".+@.+\\.[a-z]+")); // controllo la mail con un pattern di una regex
  
        
        return v.validate();
     }
 
     public void updatePartner(Key key, String chiSiamo, String indirizzo,
             String telefono, String email, String sitoWeb) {
 
         Partner partner = this.getOrNull(key);
         
         try {
             partner.setChiSiamo(chiSiamo);
             partner.setIndirizzo(indirizzo);
             partner.setTelefono(telefono);
             partner.setEmail(email);
             partner.setSitoWeb(sitoWeb);
             Transaction tx = Datastore.beginTransaction();
             Datastore.put(partner);
             tx.commit();
         } catch (Exception e) {
             // TODO: handle exception
         }
         
 
         
     }
 
   
 
 }
