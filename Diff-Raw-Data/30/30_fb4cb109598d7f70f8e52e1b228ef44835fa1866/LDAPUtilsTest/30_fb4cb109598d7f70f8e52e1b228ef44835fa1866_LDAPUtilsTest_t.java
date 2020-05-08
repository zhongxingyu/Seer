 package se.umu.cs.umume.util;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.SearchResult;
 
 import org.junit.Test;
 
 import se.umu.cs.umume.PersonBean;
 
 public class LDAPUtilsTest {
 
     @Test
     public void testUidQuery() {
         try {
            NamingEnumeration<SearchResult> result = LDAPUtils.searchForUid("joel0001");
             System.out.println(LDAPUtils.toString(result));
             //assertEquals("aonjon04", result.next().getAttributes().get("uid"));
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
     
     //@Test
     public void testPersonSearch() throws NamingException {
         NamingEnumeration<SearchResult> result = LDAPUtils.searchPerson("per-olov");
         System.out.println("Found:");
         System.out.println(LDAPUtils.toString(result));
     }
 
     //@Test
     public void testPersonBeaner() {
         try {
            NamingEnumeration<SearchResult> result = LDAPUtils.searchForUid("joel0001");
             List<PersonBean> personList = LDAPUtils.toPersonBeans(result);
 
             assertEquals(1, personList.size());
 
             PersonBean aonjon = personList.get(0);
 
             assertEquals("Anton", aonjon.getGivenName());
             assertEquals("Johansson", aonjon.getFamilyName());
             assertEquals(2, aonjon.getEmails().size());
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 }
