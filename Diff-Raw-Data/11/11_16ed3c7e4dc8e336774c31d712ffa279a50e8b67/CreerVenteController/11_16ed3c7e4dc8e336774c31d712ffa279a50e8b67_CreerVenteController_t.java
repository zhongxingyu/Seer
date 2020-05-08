 package abey;
 
 import abey.entities.Achat;
 import abey.entities.Boutique;
 import abey.entities.Enchere;
 import abey.entities.Produit;
 import abey.entities.VenteImmediate;
 import abey.services.BoutiqueService;
 import abey.services.EnchereService;
 import abey.services.ProduitService;
 import abey.services.UtilisateurService;
 import abey.services.VenteImmediateService;
 import abey.util.JsfUtil;
 import abey.util.LangString;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.ResourceBundle;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.validation.ConstraintViolationException;
 
 /**
  *
  * @author nicolas
  */
 @ManagedBean
 @SessionScoped
 public class CreerVenteController extends AbstractController {
 
     public static final int ACTION_CREER_ENCHERE = 1;
     public static final int ACTION_CREER_VENTE_IMMEDIATE = 2;
 
     @ManagedProperty(value = "#{creerProduitController}")
     private CreerProduitController creerProduitController;
 
     @EJB
     private VenteImmediateService venteImmediateService;
 
     @EJB
     private EnchereService enchereService;
     
     @EJB
     private ProduitService produitService;
 
     @EJB
     private BoutiqueService boutiqueService;
     
     private VenteImmediate venteImmediate;
 
     private Enchere enchere;
     
     private String recherche;
 
     private int action;
     private Produit produit;
     private List<Produit> produits;
 
     public void setCreerProduitController(CreerProduitController creerProduitController) {
         this.creerProduitController = creerProduitController;
     }
 
     public String getRecherche() {
         return recherche;
     }
 
     public void setRecherche(String recherche) {
         this.recherche = recherche;
     }
 
     public List<Produit> getProduits() {
         return produits;
     }
 
     public void setProduits(List<Produit> produits) {
         this.produits = produits;
     }
 
     public VenteImmediate getVenteImmediate() {
         if (venteImmediate == null) {
             venteImmediate = new VenteImmediate();
             venteImmediate.setStock(1);
             venteImmediate.setPrix(BigDecimal.ONE);
         }
         return venteImmediate;
     }
 
     public void setVenteImmediate(VenteImmediate venteImmediate) {
         this.venteImmediate = venteImmediate;
     }
     
     public Enchere getEnchere(){
         if(enchere == null){
             enchere = new Enchere();
             enchere.setPrixInitial(BigDecimal.ONE);
             enchere.setTerminee(false);
             enchere.setDuree(0);
         }
         return enchere;
     }
 
     public Produit getProduit() {
         if (produit != null) {
             return produit;
         }
 
         Produit produitCree = creerProduitController.getProduit();
         if (produitCree.getNom() != null) {
             return produitCree;
         }
 
         return null;
     }
 
     public void setProduit(Produit produit) {
         this.produit = produit;
         if(action == ACTION_CREER_ENCHERE){
             enchere.setProduit(produit);
         }
     }
 
     private void annulerCreer() {
         venteImmediate = null;
         recherche = null;
         produits = null;
         produit = null;
         enchere = null;
         creerProduitController.setProduit(null);
     }
 
     public String creer() {
         switch (action) {
             case ACTION_CREER_ENCHERE:
                 return creerEnchere();
             case ACTION_CREER_VENTE_IMMEDIATE:
                 return creerVenteImmediate();
         }
         return null;
     }
 
     public String creerVenteImmediate() {
         venteImmediate = getVenteImmediate();
         Produit produitVente = getProduit();
         if (produitVente == null) {
             if (recherche != null) {
                 produits = produitService.rechercheProduits(recherche);
             }
             return "/vente/Create";
         } else if (venteImmediate.getStock() > 0 && venteImmediate.getPrix().compareTo(BigDecimal.ZERO) > 0) {
             try {
                 Boutique boutique = getUtilisateurConnecte().getBoutique();
 
                 if (produitVente.getId() == null) {
                     produitService.create(produitVente);
                 }
                 venteImmediate.setDateVente(new Date());
                 venteImmediate.setProduit(produitVente);
                 venteImmediate.setBoutique(boutique);
                 venteImmediate.setAchats(new ArrayList<Achat>());
 
                 List<VenteImmediate> ventesImmediates = boutique.getVentesImmediates();
                 ventesImmediates.add(venteImmediate);
                 boutique.setVentesImmediates(ventesImmediates);
 
                System.out.println("VENTES IMM "+ventesImmediates.size());

                 
 
                 venteImmediateService.create(venteImmediate);
                produitVente.getVentesImmediates().add(venteImmediate);
 
                 annulerCreer();
                 JsfUtil.addSuccessMessage(
                         LangString.params(
                                 ResourceBundle.getBundle("/Bundle", getLangueSession().getLocale()).getString("SaleCreated"),
                                 produitVente.getNom()
                         )
                 );
                 return "/utilisateurs/Profil";
             } catch (Exception e) {
                 System.out.println("ex1=" + e);
                 System.out.println("ex2=" + e.getCause());
                 if (e.getCause() instanceof ConstraintViolationException) {
                     System.out.println("ex3=" + ((ConstraintViolationException) e.getCause()).getConstraintViolations());
                 }
                 JsfUtil.addErrorMessage(
                         ResourceBundle.getBundle("/Bundle", getLangueSession().getLocale()).getString("SaleCreatedError")
                 );
                 return "/vente/Create";
             }
         } else {
             System.out.println("Probleme avec le formulaire??");
             return "/vente/Create";
         }
     }
 
     public String creerProduit() {
         annulerCreer();
         creerProduitController.setAction(action);
         return "/produits/Create";
     }
 
     public String annuler() {
         annulerCreer();
         return "Create";
     }
 
     public String initVenteImmediate() {
         action = ACTION_CREER_VENTE_IMMEDIATE;
         return "/vente/Create";
     }
 
     public String initEnchere() {
         action = ACTION_CREER_ENCHERE;
         return "/encheres/Create";
     }
 
     private String creerEnchere() {
         enchere = getEnchere();
         Produit produitEnchere = getProduit();
         if (produitEnchere == null) {
             if (recherche != null) {
                 produits = produitService.rechercheProduits(recherche);
             }
             return "/encheres/Create";
         } else if (enchere.getPrixInitial().compareTo(BigDecimal.ZERO) > 0) {
             try {
                 enchere.setDateDebut(new Date());
                 enchere.setProduit(produitEnchere);
                 enchere.setDateFin(new Date(enchere.getDateDebut().getTime()+enchere.getDuree()*24*60*60*1000));
                 enchere.setVendeur(getUtilisateurConnecte());
 
                 if (produitEnchere.getId() == null) {
                     produitService.create(produitEnchere);
                 }
                 enchere.setProduit(produitEnchere);
                 enchereService.create(enchere);
                produitEnchere.getEncheres().add(enchere);
                 getUtilisateurConnecte().getEncheresCrees().add(enchere);
                 annulerCreer();
                 JsfUtil.addSuccessMessage(
                         LangString.params(
                                 ResourceBundle.getBundle("/Bundle", getLangueSession().getLocale()).getString("AuctionSaleCreated"),
                                 produitEnchere.getNom()
                         )
                 );
                 return "/utilisateurs/Profil";
             } catch (Exception e) {
                 System.out.println("ex1=" + e);
                 System.out.println("ex2=" + e.getCause());
                 if (e.getCause() instanceof ConstraintViolationException) {
                     System.out.println("ex3=" + ((ConstraintViolationException) e.getCause()).getConstraintViolations());
                 }
                 JsfUtil.addErrorMessage(
                         ResourceBundle.getBundle("/Bundle", getLangueSession().getLocale()).getString("AuctionSaleCreatedError")
                 );
                 return "/encheres/Create";
             }
         } else {
             System.out.println("Probleme avec le formulaire??");
             return "/encheres/Create";
         }
     }
     
 }
