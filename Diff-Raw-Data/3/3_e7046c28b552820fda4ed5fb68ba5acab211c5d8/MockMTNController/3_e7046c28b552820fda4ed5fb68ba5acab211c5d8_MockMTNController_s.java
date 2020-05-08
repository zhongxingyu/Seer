 package org.motechproject.ghana.mtn.controller;
 
 import org.motechproject.ghana.mtn.billing.domain.MTNMockUser;
 import org.motechproject.ghana.mtn.billing.repository.AllMTNMockUsers;
 import org.motechproject.ghana.mtn.vo.Money;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.List;
 
 import static org.apache.commons.lang.math.NumberUtils.isNumber;
 
 @Controller
 public class MockMTNController {
 
     @Autowired
     private AllMTNMockUsers allMTNMockUsers;
 
     @RequestMapping(value = "/mock-mtn/users/all", method = RequestMethod.GET)
     public void showUsers(HttpServletResponse response) throws IOException {
         List<MTNMockUser> users = allMTNMockUsers.getAll();
 
         StringBuilder builder = new StringBuilder();
         builder.append("<table id=\"mtn_user_table\">");
         builder.append(header("Mobile No", "Balance", "Last Updated"));
 
         for (MTNMockUser user : users)
             builder.append("<tr><td>" + user.getMobileNumber() + "</td><td>" + user.getBalance() + "</td><td>" + user.getAudit().getLastUpdated() + "</tr></td>");
 
         builder.append("</table>");
         response.getWriter().write(builder.toString());
     }
 
     @RequestMapping(value = "/mock-mtn/users/add", method = RequestMethod.POST)
     public void addUser(@RequestParam String mtnUserNumber, @RequestParam String mtnUserBalance) {
         if (!isNumber(mtnUserBalance) || !isNumber(mtnUserBalance))
             return;
         List<MTNMockUser> byMobileNumber = allMTNMockUsers.findByMobileNumber(mtnUserNumber);
         if (byMobileNumber != null && !byMobileNumber.isEmpty())
             return;
         allMTNMockUsers.add(new MTNMockUser(mtnUserNumber, new Money(Double.valueOf(mtnUserBalance))));
     }
 
     private String header(Object... headers) {
         StringBuilder builder = new StringBuilder();
         for (Object header : headers)
             builder.append("<th>").append(header).append("</th>");
         return builder.toString();
 
     }
 
 }
