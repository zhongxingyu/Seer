 package gov.nysenate.billbuzz.util;
 
 import gov.nysenate.billbuzz.model.BillBuzzConfirmation;
 import gov.nysenate.billbuzz.model.BillBuzzSubscription;
 import gov.nysenate.billbuzz.model.BillBuzzUser;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 
 public class FormProcessor
 {
     private final static Logger logger = Logger.getLogger(FormProcessor.class);
 
     public static BillBuzzConfirmation getConfirmation(HttpServletRequest request) throws SQLException
     {
         String code = request.getParameter("key");
         if (code != null) {
             BillBuzzDAO dao = new BillBuzzDAO();
             BillBuzzConfirmation confirmation = dao.loadConfirmation("update", code);
             if (confirmation != null && !confirmation.isExpired()) {
                 return confirmation;
             }
         }
         return null;
     }
 
     public static Map<String, TreeSet<String>> getSubscriptionMap(List<BillBuzzSubscription> subscriptions)
     {
         Map<String, TreeSet<String>> subscriptionMap = new HashMap<String, TreeSet<String>>();
         subscriptionMap.put("sponsor", new TreeSet<String>());
         subscriptionMap.put("other", new TreeSet<String>());
         subscriptionMap.put("all", new TreeSet<String>());
         for (BillBuzzSubscription subscription : subscriptions) {
             subscriptionMap.get(subscription.getCategory()).add(subscription.getValue());
         }
         return subscriptionMap;
     }
 
     public static BillBuzzUser processRequestForm(HttpServletRequest request) throws SQLException
     {
         BillBuzzDAO dao = new BillBuzzDAO();
         String email = request.getParameter("email");
         if (email == null) {
             return null;
         }
         else {
             return dao.loadUser(email);
         }
     }
 
     /**
     *
     * @param request
     * @param user
     * @param createdAt
     * @return A saved user with its new subscriptions if the form if valid. null if the form is invalid.
     * @throws SQLException
     */
    public static BillBuzzUser processSubscriptionForm(HttpServletRequest request, Date createdAt) throws SQLException
    {
        BillBuzzDAO dao = new BillBuzzDAO();
        String email = request.getParameter("email");
        String email2 = request.getParameter("email2");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
 
       if (email == null || email.trim().isEmpty() || firstName == null || firstName.trim().isEmpty() || email2 == null || email2.trim().isEmpty() || !email.equals(email2)) {
            return null;
        }
 
        BillBuzzUser user = dao.loadUser(email);
        if (user == null) {
            user = new BillBuzzUser(email, firstName, lastName, createdAt);
            dao.saveUser(user);
        }
        else {
            user.setFirstName(firstName);
            user.setLastName(lastName);
        }
 
        user.setSubscriptions(getSubscriptions(request, user.getId(), createdAt));
 
        return user;
    }
 
    public static List<BillBuzzSubscription> getSubscriptions(HttpServletRequest request, Long userId, Date now) throws SQLException {
        String all = request.getParameter("all");
        String other = request.getParameter("other");
        Set<String> sponsors = new TreeSet<String>(Arrays.asList(request.getParameterValues("senators")));
        List<BillBuzzSubscription> subscriptions = new ArrayList<BillBuzzSubscription>();
 
        if (all != null) {
            BillBuzzSubscription subscription = new BillBuzzSubscription(userId, "all", "all", now);
            subscriptions.add(subscription);
        }
        else {
            // Add subscription to other if they've requested it.
            if (other != null) {
                BillBuzzSubscription subscription = new BillBuzzSubscription(userId, "other", "other", now);
                subscriptions.add(subscription);
            }
 
            if (sponsors != null) {
                for (String sponsor : sponsors) {
                    BillBuzzSubscription subscription = new BillBuzzSubscription(userId, "sponsor", sponsor, now);
                    subscriptions.add(subscription);
                }
            }
        }
        return subscriptions;
    }
 }
