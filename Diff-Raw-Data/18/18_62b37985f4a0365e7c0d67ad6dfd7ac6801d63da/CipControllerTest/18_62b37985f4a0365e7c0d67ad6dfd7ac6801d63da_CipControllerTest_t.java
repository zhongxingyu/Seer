 /*
  * Cip&Ciop
  * Copyright (C) 2012 Stefano Fornari
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
  * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  */
 package tests.ste.cipeciop.ajax;
 
 import static org.junit.Assert.*;
 import        org.junit.Test;
 
 import ste.cipeciop.Constants;
 import com.funambol.tools.test.BeanShellTest;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.http.HttpSession;
 import ste.cipeciop.Ciop;
 import ste.cipeciop.Cip;
 import ste.cipeciop.CipCiopManager;
 import ste.cipeciop.test.web.mock.HttpServletRequestMock;
 import ste.cipeciop.test.web.mock.ServletContextMock;
 import tests.ste.cipeciop.CipCiopTestUtil;
 
 /**
  *
  * @author ste
  */
 public class CipControllerTest extends BeanShellTest implements Constants {
     
     
     
     public CipControllerTest() throws Exception {
         setCommandsDirectory("src/main/webapp/WEB-INF/commands");
         setBshFileName("src/main/webapp/ajax/controllers/cip.bsh");
     }
     
     @Override
     protected void beanshellSetup() throws Exception {
         CipCiopTestUtil.resetEnvironment();
         CipCiopTestUtil.prepareEnvironment();
         
         ServletContextMock context = new ServletContextMock();
         HttpServletRequestMock r = new HttpServletRequestMock(context);
         
         HttpSession s = r.getSession();
         Map attributes = new HashMap();
         attributes.put(ALIAS_USER_ID, CipCiopTestUtil.TEST_USER1);
         s.setAttribute(ATTRIBUTE_IDENTIFIER, attributes);
         
         beanshell.set("request", r);
         beanshell.set("session", r.getSession());
     }
 
     @Test
     public void deleteCip() throws Exception {
         //
        // No id given, no cips deleted, view shall be cip.jsp
         //
         beanshell.set(AJAX_ACTION, AJAX_ACTION_DELETE);
         
         exec();
         
         CipCiopManager ccm  = new CipCiopManager(CipCiopTestUtil.TEST_USER1);
         List<Cip> cips = ccm.getCips();
         List<Ciop> ciops = ccm.getCiops();
         
        assertEquals(3, ccm.getCips().size()); assertEquals(2, ccm.getCiops().size());
        assertEquals("cip.jsp", beanshell.get("view"));
         
         //
         // If type is missing, nothing gets deleted
         //
         beanshell.set(AJAX_PARAM_ID, String.valueOf(cips.get(0).getId()));
         
         exec();
         
         assertEquals(3, ccm.getCips().size()); assertEquals(2, ccm.getCiops().size());
        assertEquals("cip.jsp", beanshell.get("view"));
         
         //
        // now we delete one cip, the change shall be reflected by ccm
         //
         beanshell.set(AJAX_PARAM_TYPE, DB_ENTITY_CIP);
         
         exec();
         
         assertEquals(2, ccm.getCips().size()); assertEquals(2, ccm.getCiops().size());
         assertFalse(cips.get(0).getId() == ccm.getCips().get(0).getId());
        assertEquals("cip.jsp", beanshell.get("view"));
         
         //
        // now we delete one ciop, the change shall be reflected by ccm
         //
         beanshell.set(AJAX_PARAM_ID, String.valueOf(ciops.get(0).getId()));
         beanshell.set(AJAX_PARAM_TYPE, DB_ENTITY_CIOP);
         
         exec();
         assertEquals(1, ccm.getCiops().size());
        assertEquals("cip.jsp", beanshell.get("view"));
     }
 }
