 package net.yace.web.servlets;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import net.yace.entity.Yattribute;
 import net.yace.entity.Yattributevalue;
 import net.yace.entity.Ycollection;
 import net.yace.entity.Yitem;
 import net.yace.entity.Yitemtype;
 import net.yace.entity.Yuser;
 import net.yace.facade.YattributeFacade;
 import net.yace.facade.YattributevalueFacade;
 import net.yace.facade.YcollectionFacade;
 import net.yace.facade.YitemFacade;
 import net.yace.facade.YitemtypeFacade;
 import net.yace.web.utils.ServicesLocator;
 import net.yace.web.utils.YaceUtils;
 
 /**
  *
  * @author Scohy Jérôme
  */
 public class ServletItemAddEdit extends HttpServlet {
 
     private final static String VUE_PRESENTATION = "welcome.jsp";
     private final static String SVLT_COLLECTION = "see?idCollection=";
     private final static String VUE_ITEM_ADDEDIT = "WEB-INF/view/user/item-addedit.jsp";
 
     /** 
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         YaceUtils.SessionState state = YaceUtils.getSessionState(request);
         if (state != YaceUtils.SessionState.noauth) {
             HttpSession session = request.getSession(false);
             Yuser yuser = (Yuser) session.getAttribute("user");
 
             YcollectionFacade facColl = ServicesLocator.getCollectionFacade();
             YitemtypeFacade facItemtype = ServicesLocator.getItemTypeFacade();
             YattributeFacade facAttribute = ServicesLocator.getAttributeFacade();
             YitemFacade facItem = ServicesLocator.getItemFacade();
             
             String idCollection = request.getParameter("coll");
             String idType = request.getParameter("type");
             if (idCollection != null && !idCollection.isEmpty() && idType != null && !idType.isEmpty()) {
                 Ycollection collection = facColl.find(Integer.parseInt(idCollection));
                 Yitemtype itemtype = facItemtype.find(Integer.parseInt(idType));
                 // TODO : Vérifier si l'itemtype est associé à la collection
                 if (itemtype!=null && collection!=null && collection.getOwner().getIdYUSER() == yuser.getIdYUSER()) {
                     
                     // Vérifie si l'autocompletion doit etre prise en compte
                     boolean autocomplete = false;
                     if(itemtype.isPublic()) {
                         String name;
                         if(itemtype.getName().equalsIgnoreCase("film"))
                             name="film";
                        else if(itemtype.getName().equalsIgnoreCase("album"))
                             name="music";
                         else if(itemtype.getName().equalsIgnoreCase("livre"))
                             name="book";
                         else
                             name="";
                         
                         if(!name.isEmpty()) {
                             autocomplete = true;
                             request.setAttribute("autocomplete", name);
                         }
                     }
                     
                     //ASIDE HELP
                     Map<String, List<String>> asideHelp = new HashMap<String, List<String>>();
                     List<String> infoBoxes = new ArrayList<String>();
                     List<String> tipBoxes = new ArrayList<String>();
 
                     if(autocomplete) {
                         tipBoxes.add("Le champ de recherche tout au dessus vous permettra de trouver une série de résultats correspondant.");
                         tipBoxes.add("Un clic sur un résultat permet de pré-remplir les champs.");
                     }
 
                     // Récupération des attributes et conversion des noms
                     List<Yattribute> attrs = facAttribute.findAttributesByItem(itemtype);
                     List<String> attrsName = new ArrayList<String>();
                     for(Yattribute att : attrs) {
                         attrsName.add(YaceUtils.deAccent(att.getName()));
                     }
                     request.setAttribute("attrsName", attrsName);
                     
                     request.setAttribute("idColl", idCollection);
                     request.setAttribute("idType", idType);
                     
                     String editItem = request.getParameter("edit");
                     if (editItem != null && !editItem.isEmpty()) { // Si editItem, c'est l'édition
                         Yitem item = facItem.find(Integer.parseInt(editItem));
 
                         if (item.getCollection().equals(collection)) {
                             request.setAttribute("pageTitle", "Edition d'un objet " + itemtype.getName() + " de la collection " + collection.getTheme());
                             request.setAttribute("pageHeaderTitle", "Edition d'un objet <strong>" + itemtype.getName() + "</strong> de la collection <strong>" + collection.getTheme() + "</strong>");
                             request.setAttribute("edit", editItem);
 
                             // Ajout des valeurs de l'item
                             YattributevalueFacade facAttrVal = ServicesLocator.getAttributeValueFacade();
                             List<Yattributevalue> attrVals = new ArrayList<Yattributevalue>();
                             attrVals = facAttrVal.findAllValuesForItem(item);
                             request.setAttribute("itemValues", attrVals);
 
                             infoBoxes.add("Sur cette page, vous pouvez ajouter un objet à votre collection.");
                             infoBoxes.add("Il vous suffit de remplir les champs et de valider le formulaire.");
                             infoBoxes.add("Les deux premiers champs sont obligatoires.");
                             
                             asideHelp.put("tip", tipBoxes);
                             asideHelp.put("info", infoBoxes);
                             request.setAttribute("asideHelp", YaceUtils.getAsideHelp(asideHelp));
                     
                             request.getRequestDispatcher(VUE_ITEM_ADDEDIT).forward(request, response);
                         } else {
                             YaceUtils.displayCollectionUnreachableError(request, response);
                         }
                     } else { // Sinon l'ajout
                         
                         // Vérification du nombre max d'objet
                         int max_items = yuser.getRank().getNbMaxItem();
                         int nb_items = facItem.countNbItemsFromUuser(yuser);
                         if(max_items<0 || max_items>nb_items) {
                             request.setAttribute("pageTitle", "Ajout d'un objet " + itemtype.getName() + " dans la collection " + collection.getTheme());
                             request.setAttribute("pageHeaderTitle", "Ajout d'un objet <strong>" + itemtype.getName() + "</strong> dans la collection <strong>" + collection.getTheme() + "</strong>");
                             
                             infoBoxes.add("Sur cette page, vous pouvez éditer un objet de votre collection.");
                             infoBoxes.add("L'édition fonctionne de la même manière que l'ajout d'objet : Remplissez les champs et validez.");
                             
                             asideHelp.put("tip", tipBoxes);
                             asideHelp.put("info", infoBoxes);
                             request.setAttribute("asideHelp", YaceUtils.getAsideHelp(asideHelp));
                             
                             request.getRequestDispatcher(VUE_ITEM_ADDEDIT).forward(request, response);
                         } else {
                             YaceUtils.displayMaxItemReachError(request, response);
                         }
                     }
                 } else {
                     YaceUtils.displayCollectionUnreachableError(request, response);
                 }
             } else {
                 YaceUtils.displayCollectionUnreachableError(request, response);
             }
         } else {
             request.getRequestDispatcher(VUE_PRESENTATION).forward(request, response);
         }
     }
 
     /** 
      * Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         boolean redirect = false;
 
         YaceUtils.SessionState state = YaceUtils.getSessionState(request);
         if (state != YaceUtils.SessionState.noauth) {
             HttpSession session = request.getSession(false);
             Yuser yuser = (Yuser) session.getAttribute("user");
 
             YcollectionFacade facColl = ServicesLocator.getCollectionFacade();
             YitemtypeFacade facItemtype = ServicesLocator.getItemTypeFacade();
 
             String idCollection = request.getParameter("coll");
             String idType = request.getParameter("type");
             if (idCollection != null && !idCollection.isEmpty() && idType != null && !idType.isEmpty()) {
                 Ycollection collection = facColl.find(Integer.parseInt(idCollection));
                 Yitemtype itemtype = facItemtype.find(Integer.parseInt(idType));
                 // TODO : Vérifier si l'itemtype est associé à la collection
                 if (itemtype != null && collection != null && collection.getOwner().getIdYUSER() == yuser.getIdYUSER()) {
 
                     YitemFacade itemFacade = ServicesLocator.getItemFacade();
                     YattributeFacade attrFacade = ServicesLocator.getAttributeFacade();
                     YattributevalueFacade attrValFacade = ServicesLocator.getAttributeValueFacade();
                     
                     // Récupération des attributes et conversion des noms
                     List<Yattribute> listAttributes = attrFacade.findAttributesByItem(itemtype);
                     List<String> attrsName = new ArrayList<String>();
                     for(Yattribute att : listAttributes) {
                         attrsName.add(YaceUtils.deAccent(att.getName()));
                     }
                     
                     String buttonAdd = request.getParameter("button_add");
                     String buttonEdit = request.getParameter("button_edit");
                     String buttonDelete = request.getParameter("delete");
                     if (buttonAdd != null) {
                         // Création de l'objet
                         Yitem item = new Yitem();
                         item.setType(itemtype);
                         item.setCollection(collection);
                         itemFacade.create(item);
                         
                         // Remplissage des attributeValue
                         for(int i=0; i<listAttributes.size(); i++) {
                             Yattribute attr = listAttributes.get(i);
                             Yattributevalue av = new Yattributevalue();
                             av.setAttribute(attr);
                             
                             // Gestion des types d'attributs
                             String attrName = attrsName.get(i);
                             if(attr.getType().equalsIgnoreCase("string")) {
                                 av.setValStr(request.getParameter("attr_" + attrName));
                             } else {
                                 //tester si l'url est valide
                                 //sinon attributevalue vide, afficher image par defaut
                                 if(YaceUtils.isValidURL(request.getParameter("attr_" + attrName)))
                                     av.setValStr(request.getParameter("attr_" + attrName));
                                 else
                                     av.setValStr("");
                             }
                             
                             // Enregistrement de l'attributevalue
                             attrValFacade.create(av);
                             av.addYitem(item);
                             item.addYattributevalue(av);
                             itemFacade.edit(item);
                             attrValFacade.edit(av);
                         }
                         
                         request.setAttribute("messageInfos", "L'objet a bien été ajouté !");
                     } else if (buttonEdit != null) {
                         String itemId = request.getParameter("edit");
 
                         if (itemId != null && !itemId.isEmpty()) {
                             // Recherche de l'objet, de ses valeurs et sa structure
                             Yitem item = itemFacade.find(Integer.parseInt(itemId));
                             List<Yattributevalue> attrVals = new ArrayList<Yattributevalue>();
                             attrVals = attrValFacade.findAllValuesForItem(item);
                         
                             for(int i=0; i<attrVals.size(); i++) {
                                 Yattributevalue attrVal = attrVals.get(i);
                                 String attrName = attrsName.get(i);
                                 String attrType = listAttributes.get(i).getType();
 
                                 String newValue = request.getParameter("attr_" + attrName);
                                 if (attrType.equalsIgnoreCase("string")) {
                                     attrVal.setValStr(newValue);
                                 } else {
                                     //tester si l'url est valide
                                     //sinon attributevalue vide, afficher image par defaut
                                     if(YaceUtils.isValidURL(newValue))
                                         attrVal.setValStr(newValue);
                                     else
                                         attrVal.setValStr("");
                                 }
                                 attrValFacade.edit(attrVal);
                             }
                             
                             request.setAttribute("messageInfos", "L'objet a bien été édité !");
                         }
                     } else if(buttonDelete != null) {
                         String itemId = request.getParameter("itemId");
                         
                         if(itemId != null && !itemId.isEmpty()) {
                             Yitem item = itemFacade.find(Integer.parseInt(itemId));
                             itemFacade.remove(item);
                             
                             request.setAttribute("messageInfos", "L'objet a bien été supprimé !");
                         }
                     }
 
                     redirect = true;
                     request.getRequestDispatcher(SVLT_COLLECTION + idCollection).forward(request, response);
                     //response.sendRedirect(SVLT_COLLECTION + idCollection);
                 }
             }
         }
 
         if (!redirect) // si pas d'ajout ou d'édition ou de suppression, on fait appel à l'affichage normal de la page
         {
             doGet(request, response);
         }
     }
 
     @Override
     public String getServletInfo() {
         return "Gestion de l'ajout/edition/suppression d'objet à une collection.";
     }
 }
