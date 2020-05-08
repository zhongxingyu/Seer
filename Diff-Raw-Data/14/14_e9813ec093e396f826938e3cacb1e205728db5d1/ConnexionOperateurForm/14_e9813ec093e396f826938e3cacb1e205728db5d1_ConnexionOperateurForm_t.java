 package form;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import Modele.Operateur;
 
 public final class ConnexionOperateurForm {
     private static final String CHAMP_EMAIL  = "nom";
     private static final String CHAMP_PASS   = "mdp";
     private String              resultat;
     private Map<String, String> erreurs      = new HashMap<String, String>();
 
     public String getResultat() {
         return resultat;
     }
 
     public Map<String, String> getErreurs() {
         return erreurs;
     }
 
     public Operateur connecterUtilisateur( HttpServletRequest request ) {
         /* Récupération des champs du formulaire */
         String nom = getValeurChamp( request, CHAMP_EMAIL );
         String motDePasse = getValeurChamp( request, CHAMP_PASS );
 
         Operateur utilisateur = new Operateur();
 
         /* Validation du champ email. */
         try {
             validationNom( nom );
         } catch ( Exception e ) {
             setErreur( CHAMP_EMAIL, e.getMessage() );
         }
         utilisateur.setO_Nom(nom );
         /* Validation du champ mot de passe. */
         try {
             validationMotDePasse( motDePasse );
         } catch ( Exception e ) {
             setErreur( CHAMP_PASS, e.getMessage() );
         }
         utilisateur.setO_MotDePasse( motDePasse );
 
         /* Initialisation du résultat global de la validation. */
         if ( erreurs.isEmpty() ) {
             resultat = "Succès de la connexion.";
         } else {
             resultat = "Échec de la connexion.";
         }
 
         return utilisateur;
     }
 
     /**
      * Valide l'adresse email saisie.
      */
     private void validationNom( String nom ) throws Exception {
         if ( nom != null ) {
             throw new Exception( "Le champs nom est nul" );
         }
     }
 
     /**
      * Valide le mot de passe saisi.
      */
     private void validationMotDePasse( String motDePasse ) throws Exception {
         if ( motDePasse != null ) {
             if ( motDePasse.length() < 3 ) {
                 throw new Exception( "Le mot de passe doit contenir au moins 3 caractères." );
             }
         } else {
             throw new Exception( "Merci de saisir votre mot de passe." );
         }
     }
 
     /*
      * Ajoute un message correspondant au champ spécifié à la map des erreurs.
      */
     private void setErreur( String champ, String message ) {
         erreurs.put( champ, message );
     }
 
     /*
      * Méthode utilitaire qui retourne null si un champ est vide, et son contenu
      * sinon.
      */
     private static String getValeurChamp( HttpServletRequest request, String nomChamp ) {
         String valeur = request.getParameter( nomChamp );
         if ( valeur == null || valeur.trim().length() == 0 ) {
             return null;
         } else {
             return valeur;
         }
     }
 }
