 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.yace.web.servlets;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
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
  * @author MaBoy <bruno.boi@student.helha.be>
  */
 public class ServletCollectionView extends HttpServlet {
 
     private final static String VUE_PRESENTATION = "welcome.jsp";
     //TODO mettre ta fucking jsp ici mec :p
     private final static String VUE_ITEMS = "WEB-INF/view/user/collection.jsp";
 
     /** 
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         boolean error = false;
 
        String idCollection = request.getParameter("idCollection");
         try {
             int collid = Integer.parseInt(idCollection);
 
             YcollectionFacade collfac = ServicesLocator.getCollectionFacade();
             Ycollection coll = collfac.find(collid);
             Yuser owner = coll.getOwner();
             Yuser user = (Yuser) request.getSession(false).getAttribute("user");
 
             if (coll.isPublic() || user.getIdYUSER() == owner.getIdYUSER()) {
                 request.setAttribute("pageTitle", "Objets dans la collection");
                 request.setAttribute("collection", coll);
                 
                 YitemtypeFacade itfac = ServicesLocator.getItemTypeFacade();
                 YitemFacade ifac = ServicesLocator.getItemFacade();
                 YattributeFacade yatfac = ServicesLocator.getAttributeFacade();
                 YattributevalueFacade yatvfac = ServicesLocator.getAttributeValueFacade();
                
                 List<Yitemtype> itemtypes = itfac.findItemtypesInCollection(coll);
                 List<List<Yattribute>> attributes = new ArrayList<List<Yattribute>>();
                 List<List<List<Yattributevalue>>> values = new ArrayList<List<List<Yattributevalue>>>();
                 
                 for (int i = 0; i < itemtypes.size(); i++) {
                     attributes.add(yatfac.findAttributesByItem(itemtypes.get(i)));
                     
                     List<Yitem> items = ifac.getItemsByCollectionAndType(coll, itemtypes.get(i));
                     values.add(new ArrayList<List<Yattributevalue>>());
                     for (int j = 0; j < items.size(); j++) {
                         values.get(i).add(yatvfac.findAllValuesForItem(items.get(j)));
                     }
                 }
                 
                 request.setAttribute("itemtypes", itemtypes);
                 request.setAttribute("attributes", attributes);
                 request.setAttribute("values", values);
                 
                 request.getRequestDispatcher(VUE_ITEMS).forward(request, response);
             } else {
                 //ERROR
                 error = true;
             }
         } catch (NumberFormatException e) {
             error = true;
         }
         
         if (error) {
             YaceUtils.displayCollectionUnreachableError(request, response);
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
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {/*
         YaceUtils.SessionState state = YaceUtils.getSessionState(request);
         if (state == YaceUtils.SessionState.noauth) {
             request.getRequestDispatcher(VUE_PRESENTATION).forward(request, response);
         } else {
             HttpSession session = request.getSession(false);
             Yuser yuser = (Yuser) session.getAttribute("user");
             /*
              * Session valide: utilisateur connecté
              *
 
             YcollectionFacade facColl = ServicesLocator.getCollectionFacade();
 
             String idCollection = request.getParameter("idCollection");
 
             if (idCollection != null && !idCollection.isEmpty()) {
                 //check collection owner and edit.
                 Ycollection collection = facColl.find(Integer.parseInt(idCollection));
                 if (collection.getOwner().getIdYUSER() == yuser.getIdYUSER()) {
                     //Tout est ok
                 } else {
                     //Error
                     request.getRequestDispatcher(VUE_PRESENTATION).forward(request, response);
                 }
             } else {
                 request.getRequestDispatcher(VUE_PRESENTATION).forward(request, response);
             }
         }*/
         doGet(request, response);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Ajout et édition d'un objet";
     }// </editor-fold>
 }
