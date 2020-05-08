 package com.shpandrak.shpanlist.web.pages;
 
 import com.shpandrak.datamodel.field.EntityKey;
 import com.shpandrak.persistence.PersistenceException;
 import com.shpandrak.shpanlist.model.ListInstance;
 import com.shpandrak.shpanlist.model.ListInstanceItem;
 import com.shpandrak.shpanlist.model.auth.LoggedInUser;
 import com.shpandrak.shpanlist.services.ListInstanceService;
 import com.shpandrak.shpanlist.web.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Created with love
  * User: shpandrak
  * Date: 7/12/13
  * Time: 15:06
  */
 public class ListInstanceEditPageServlet extends BasePageServlet {
 
     @Override
     public boolean drawPage(HttpServletRequest request, HttpServletResponse response, StringBuilder sb, LoggedInUser loggedInUser) throws IOException, PersistenceException {
         String pathInfo = request.getPathInfo();
         if (!pathInfo.startsWith("/")) {
             response.sendRedirect("/");
             return false;
         }
         String listInstanceIdString = pathInfo.substring(1);
         if (listInstanceIdString == null || listInstanceIdString.isEmpty()) {
             response.sendRedirect("/");
             return false;
         }
 
         EntityKey listInstanceId = ListInstance.DESCRIPTOR.idFieldDescriptor.fromString(listInstanceIdString);
 
         ListInstance listInstanceFull = ListInstanceService.getListInstanceFull(listInstanceId);
 
 
         sb.append("<head>\n" +
                 "\t<title>Shpanlist - Edit List</title>\n").append(
                getHeaderConstants());

        sb.append("\t<script type=\"text/javascript\">\n" +
                "\n" +
                "\tfunction onLoad() {\n" +
                "\tListInstanceEditPageView.listInstanceId = '").append(listInstanceIdString).append("';\n" +
                "\t}\n" +
                "\t</script>\n" +
                 "</head>");
 

         sb.append(
                "<body onload=\"onLoad()\" data-listInstanceId=\"").append(listInstanceIdString).append("\">\n" +
                         "<div id=\"pageEditListInstance\" data-role=\"page\">\n" +
                         "    <div data-role=\"header\"><h2 id =\"pageEditListInstanceHeader\">").append(
                 listInstanceFull.getName()).append("</h2>\n" +
                 "    </div>\n" +
                 "    <div data-role=\"content\">\n" +
                 "        <input id =\"listInstanceId\" type=\"hidden\" value=\"").append(listInstanceIdString).append("\"/>" +
                "        List Name:<input id=\"txtListName\" type=\"text\" value=\"").append(listInstanceFull.getName()).append("\">\n" +
                 "        <input type=\"button\" value=\"Update List Name\" onclick=\"ListInstanceEditPageView.updateName(txtListName.value)\" />\n" +
                 "        <table id=\"tabListInstanceItems\" data-role=\"table\" class=\"ui-responsive table-stroke\">\n");
 
         appendItemsTableBody(sb, listInstanceFull);
 
         sb.append(
                 "        </table>\n" +
                         "        <br/>\n" +
                         "        <input value=\"Add Item\" type=\"button\" data-icon=\"plus\" onclick=\"ShpanlistController.menuAddNewListInstanceItem('").append(listInstanceIdString).append("')\" />\n" +
                 "    </div>\n" +
                 "    <div data-role=\"footer\">\n" +
                 "        <a data-icon=\"arrow-l\" href=\"javascript:$.mobile.back();\"> Back</a>\n" +
                 "        <a href='javascript:ShpanlistController.menuHome()' data-icon=\"home\">Home</a>\n" +
                 "        <a href='javascript:ShpanlistController.signOut()'>Sign Out</a>\n" +
                 "    </div>\n" +
                 "</div>\n" +
                 "</body>");
         return true;
     }
 
     private void appendItemsTableBody(StringBuilder sb, ListInstance listInstanceFull) throws IOException {
         sb.append(
                 "            <thead>\n" +
                         "            <tr>\n" +
                         "                <th>Item</th><th>Description</th><th>Amount</th><th>Actions</th>\n" +
                         "            </tr>\n" +
                         "            </thead>\n" +
                         "            <tbody>\n");
 
         int idx = 0;
         List<ListInstanceItem> listInstanceItems = listInstanceFull.getListInstanceItemRelationship().getTargetEntities();
         for (ListInstanceItem currListInstanceItem : listInstanceItems) {
 
             appendListInstanceItemListElement(sb, currListInstanceItem, idx == 0, idx == listInstanceItems.size() - 1);
             ++idx;
         }
 
 
         sb.append(
                 "</tbody>\n");
     }
 
 
     private void appendListInstanceItemListElement(Appendable writer, ListInstanceItem currListInstanceItem, boolean first, boolean lastItem) throws IOException {
         writer.append(
                 "<tr><td>").append(currListInstanceItem.getName()).append("</td>\n" +
                 "<td>").append(currListInstanceItem.getDescription()).append("</td>\n" +
                 "<td>").append(currListInstanceItem.getAmount() == null ? "" : String.valueOf(currListInstanceItem.getAmount())).append("</td>\n" +
                 "<td><a HREF=\"javascript:ListInstanceEditPageView.removeItem('").append(currListInstanceItem.getId().toString()).append("')\">Remove</a>");
 
         if (!lastItem) {
             writer.append("&nbsp;<img src=\"/images/down.png\" onclick=\"ListInstanceEditPageView.pushItemDown('").append(currListInstanceItem.getId().toString()).append("')\"/>");
         }
 
         if (!first) {
             writer.append("&nbsp;<img src=\"/images/up.png\" onclick=\"ListInstanceEditPageView.pushItemUp('").append(currListInstanceItem.getId().toString()).append("')\"/>");
         }
 
         writer.append("</td></tr>");
     }
 
 
     public HtmlResponsePrinter doItPlease(HttpServletRequest request, HttpServletResponse response, LoggedInUser loggedInUser, String what) throws IOException, PersistenceException, UserMustSignInException {
         if ("updateListInstanceName".equals(what)) {
             return updateListInstanceName(loggedInUser, request, response);
         } else if ("pushListInstanceItemUp".equals(what)) {
             return pushListInstanceItemUp(loggedInUser, request, response);
         } else if ("pushListInstanceItemDown".equals(what)) {
             return pushListInstanceItemDown(loggedInUser, request, response);
         } else if ("removeListInstanceItem".equals(what)) {
             return removeListInstanceItem(loggedInUser, request, response);
         } else {
             throw new IllegalArgumentException("Invalid action " + what);
         }
     }
 
     private HtmlResponsePrinter removeListInstanceItem(LoggedInUser loggedInUser, HttpServletRequest request, HttpServletResponse response) throws PersistenceException, IOException {
         String listInstanceId = request.getParameter("listInstanceId");
         String listInstanceItemId = request.getParameter("listInstanceItemId");
         ListInstanceService.removeListInstanceItem(ListInstanceItem.DESCRIPTOR.idFieldDescriptor.fromString(listInstanceItemId));
 
         //todo: return a removeFromDOM html refresh command...
         return getTableRefreshResponsePrinter(listInstanceId);
     }
 
     private HtmlResponsePrinter updateListInstanceName(LoggedInUser loggedInUser, HttpServletRequest request, HttpServletResponse response) throws PersistenceException, IOException {
         String listInstanceId = request.getParameter("listInstanceId");
         String listInstanceName = request.getParameter("listInstanceName");
 
         ListInstanceService.updateListInstanceName(ListInstance.DESCRIPTOR.idFieldDescriptor.fromString(listInstanceId), listInstanceName);
 
         return new HtmlResponsePrinter(
                 Arrays.asList(new HtmlBindingResponse("#pageEditListInstanceHeader", listInstanceName)),
                 Collections.<HtmlRefreshResponse>emptyList());
     }
 
     private HtmlResponsePrinter pushListInstanceItemUp(LoggedInUser loggedInUser, HttpServletRequest request, HttpServletResponse response) throws PersistenceException, IOException {
         String listInstanceId = request.getParameter("listInstanceId");
         String listInstanceItemId = request.getParameter("listInstanceItemId");
         ListInstanceService.pushListInstanceItemUp(ListInstanceItem.DESCRIPTOR.idFieldDescriptor.fromString(listInstanceItemId));
 
         return getTableRefreshResponsePrinter(listInstanceId);
     }
 
     private HtmlResponsePrinter pushListInstanceItemDown(LoggedInUser loggedInUser, HttpServletRequest request, HttpServletResponse response) throws PersistenceException, IOException {
         String listInstanceId = request.getParameter("listInstanceId");
         String listInstanceItemId = request.getParameter("listInstanceItemId");
         ListInstanceService.pushListInstanceItemDown(ListInstanceItem.DESCRIPTOR.idFieldDescriptor.fromString(listInstanceItemId));
 
         return getTableRefreshResponsePrinter(listInstanceId);
     }
 
     private HtmlResponsePrinter getTableRefreshResponsePrinter(String listInstanceId) throws PersistenceException, IOException {
         ListInstance listInstanceFull = ListInstanceService.getListInstanceFull(ListInstance.DESCRIPTOR.idFieldDescriptor.fromString(listInstanceId));
         StringBuilder sb = new StringBuilder();
         appendItemsTableBody(sb, listInstanceFull);
         //todo:proper encoding
 
         return new HtmlResponsePrinter(
                 Arrays.asList(new HtmlBindingResponse("#tabListInstanceItems", encodeHtmlForJSONTransport(sb))),
                 Arrays.asList(new HtmlRefreshResponse("#tabListInstanceItems", "table")));
     }
 
 
 }
