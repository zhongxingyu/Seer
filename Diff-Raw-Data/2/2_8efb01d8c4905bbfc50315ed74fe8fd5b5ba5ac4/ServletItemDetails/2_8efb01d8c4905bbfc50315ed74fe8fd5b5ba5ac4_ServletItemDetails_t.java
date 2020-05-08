 package net.yace.web.servlets;
 
 import java.io.IOException;
 import java.text.Normalizer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import net.yace.entity.Yattributevalue;
 import net.yace.entity.Yitem;
 import net.yace.entity.Yuser;
 import net.yace.facade.YitemFacade;
 import net.yace.web.utils.ServicesLocator;
 import net.yace.web.utils.YaceUtils;
 
 public class ServletItemDetails extends HttpServlet {
 
     private final static String VUE_ITEM = "WEB-INF/view/user/item-attributevalues.jsp";
     private final static String VUE_HOME = "WEB-INF/view/user/home.jsp";
 
     //url pattern : /details
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
         //droits d'acces de l'utilisateur à l'item
         //cas user proprietaire item privé
         //cas consultation item publique
 
         YitemFacade itemFac = ServicesLocator.getItemFacade();
         String idItem = "";
         idItem = request.getParameter("item");
         if (idItem == null || idItem.isEmpty()) {
             YaceUtils.displayItemError(request, response);
         } else {
             int idIt = Integer.parseInt(idItem);
 
             //gestion permission : public ou privé
             //savoir deja si l'item existe
             Yitem item = itemFac.find(idIt);
 
             HttpSession session = request.getSession(false);
             Yuser yuser = null;
             if (session != null) {
                 yuser = (Yuser) session.getAttribute("user");
             }
 
             if (YaceUtils.CanDisplayItem(item, yuser)) {
                 //liste des attributs de l'item
                 List<Yattributevalue> valList = itemFac.getItemsAttrValues(idIt);
                 if (valList != null) {
                     String clrword = request.getParameter("clr");//parametre à surligner
                     if (clrword != null && !clrword.equals("")) {
                         
                         request.setAttribute("clr", clrword);
                         for (Yattributevalue av : valList) {
                            if (!av.getAttribute().getType().equals("Image") && !av.getAttribute().getType().equals("URL")) {
                                 String lowInput = av.getValStr().toUpperCase();
                                 Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
                                 lowInput = pattern.matcher(Normalizer.normalize(lowInput, Normalizer.Form.NFD)).replaceAll("");
                                 String lowSub = clrword.toUpperCase();
                                 lowSub = pattern.matcher(Normalizer.normalize(lowSub, Normalizer.Form.NFD)).replaceAll("");
 
                                 if (lowInput.matches("(?i).*" + lowSub + ".*")) {
                                     av.setValStr(YaceUtils.envelopSubStrings(av.getValStr(), lowInput, lowSub, "<span class=\"search-line\">", "</span>"));
                                 }
                             }
                         }
                     }
 
                     Map<String, List<String>> asideHelp = new HashMap<String, List<String>>();
                     List<String> infoBoxes = new ArrayList<String>();
                     List<String> tipBoxes = new ArrayList<String>();
 
                     infoBoxes.add("Sur cette page, vous voyez le descriptif complet d'un objet.");
                     tipBoxes.add("Les flèches bleues permettent de naviguer parmi les éléments de la collection courante");
                     tipBoxes.add("Si vous venez d'effectuer une recherche, l'élément correspondant est surligné");
 
                     asideHelp.put("tip", tipBoxes);
                     asideHelp.put("info", infoBoxes);
                     
                     int[]tab = YaceUtils.getPrevNextItemId(item);//prev et next
 
                     request.setAttribute("asideHelp", YaceUtils.getAsideHelp(asideHelp));
                     request.setAttribute("canEdit", YaceUtils.canEditItem(item, yuser));
                     request.setAttribute("canDelete", YaceUtils.canDeleteItem(item, yuser));
                     request.setAttribute("curItem", item);
                     request.setAttribute("attributevalues", valList);
                     request.setAttribute("prevIt", tab[0]);
                     request.setAttribute("nextIt", tab[1]);
                     request.setAttribute("pageTitle", "Détails d'un objet de " + item.getCollection().getTheme());
                     request.setAttribute("pageHeaderTitle", "Détails d'un objet de <strong>" + item.getCollection().getTheme() + "</strong>");
                     request.getRequestDispatcher(VUE_ITEM).forward(request, response);
                 }
             }
             else
             {
                 //l'user ne peut pas consulter cet item
                 YaceUtils.displayItemError(request, response);
             }
         }
 
 
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         doGet(request, response);
     }
 
     @Override
     public String getServletInfo() {
         return "Affichage du détail d'un objet";
     }
 }
