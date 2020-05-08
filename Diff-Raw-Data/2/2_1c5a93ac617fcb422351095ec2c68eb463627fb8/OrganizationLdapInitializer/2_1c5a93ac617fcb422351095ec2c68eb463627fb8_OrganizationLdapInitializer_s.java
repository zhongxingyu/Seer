 /*
  * Copyright (C) 2009 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.exoplatform.services.organization.ldap;
 
 import org.exoplatform.container.component.BaseComponentPlugin;
 import org.exoplatform.container.component.ComponentPlugin;
 import org.exoplatform.services.ldap.ObjectClassAttribute;
 import org.exoplatform.services.organization.OrganizationService;
 import org.exoplatform.services.organization.OrganizationServiceInitializer;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.naming.NamingEnumeration;
 import javax.naming.directory.BasicAttributes;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 import javax.naming.ldap.LdapContext;
 
 /**
  * Created by The eXo Platform SAS Author : Thuannd nhudinhthuan@yahoo.com Feb
  * 14, 2006
  */
 public class OrganizationLdapInitializer extends BaseComponentPlugin implements OrganizationServiceInitializer,
    ComponentPlugin
 {
 
    protected static Pattern COMPACT_DN = Pattern.compile("\\b\\p{Space}*=\\p{Space}*", Pattern.CASE_INSENSITIVE);
 
    /**
     * See {@link BaseDAO}.
     */
    private BaseDAO baseHandler;
 
    /**
     * Create basic LDAP structure. {@inheritDoc}
     */
    public void init(OrganizationService service) throws Exception
    {
       baseHandler = (BaseDAO)service.getUserHandler();
       createSubContextNew(baseHandler.ldapAttrMapping.baseURL, baseHandler.ldapAttrMapping.groupsURL);
       createSubContextNew(baseHandler.ldapAttrMapping.baseURL, baseHandler.ldapAttrMapping.userURL);
       createSubContextNew(baseHandler.ldapAttrMapping.baseURL, baseHandler.ldapAttrMapping.membershipTypeURL);
       createSubContextNew(baseHandler.ldapAttrMapping.baseURL, baseHandler.ldapAttrMapping.profileURL);
    }
 
    /**
     * @param dn Distinguished Name of sub-context to be created
     * @throws Exception if any errors occurs
     */
    public void createSubContext(String dn) throws Exception
    {
       Pattern pattern = Pattern.compile("\\b\\p{Space}*=\\p{Space}*", Pattern.CASE_INSENSITIVE);
       Matcher matcher = pattern.matcher(dn);
       dn = matcher.replaceAll("=");
       LdapContext context = baseHandler.ldapService.getLdapContext();
       try
       {
          String[] explodeDN = baseHandler.explodeDN(dn, false);
          if (explodeDN.length < 1)
             return;
          dn = explodeDN[explodeDN.length - 1];
          int i = explodeDN.length - 2;
          for (; i > -1; i--)
          {
             if (!explodeDN[i].toLowerCase().startsWith("dc="))
                break;
             dn = explodeDN[i] + "," + dn;
          }
          createDN(dn, context);
          for (; i > -1; i--)
          {
             dn = explodeDN[i] + "," + dn;
             createDN(dn, context);
          }
       }
       finally
       {
          baseHandler.ldapService.release(context);
       }
    }
 
    private void createDN(String dn, LdapContext context) throws Exception
    {
       NamingEnumeration<SearchResult> results = null;
       try
       {
          SearchControls control = new SearchControls();
          control.setSearchScope(SearchControls.OBJECT_SCOPE);
          results = context.search(dn, "(objectClass=*)", control);
 
          if (results.hasMoreElements())
          {
             return;
          }
       }
       catch (Exception exp)
       {
       }
       finally
       {
          if (results != null)
          {
             results.close();
          }
       }
 
       String nameValue = dn.substring(dn.indexOf("=") + 1, dn.indexOf(","));
       BasicAttributes attrs = new BasicAttributes();
       if (dn.toLowerCase().startsWith("ou="))
       {
          attrs.put(new ObjectClassAttribute(new String[]{"top", "organizationalUnit"}));
          attrs.put("ou", nameValue);
       }
       else if (dn.toLowerCase().startsWith("cn="))
       {
          attrs.put(new ObjectClassAttribute(new String[]{"top", "organizationalRole"}));
          attrs.put("cn", nameValue);
       }
       else if (dn.toLowerCase().startsWith("c="))
       {
          attrs.put(new ObjectClassAttribute(new String[]{"country"}));
          attrs.put("c", nameValue);
       }
       else if (dn.toLowerCase().startsWith("o="))
       {
          attrs.put(new ObjectClassAttribute(new String[]{"organization"}));
          attrs.put("o", nameValue);
       }
       else if (dn.toLowerCase().startsWith("dc="))
       {
          attrs.put(new ObjectClassAttribute(new String[]{"top", "dcObject", "organization"}));
          attrs.put("dc", nameValue);
          attrs.put("o", nameValue);
       }
       attrs.put("description", nameValue);
       context.createSubcontext(dn, attrs);
    }
 
    public void createSubContextNew(String basedn, String dn) throws Exception
    {
       Matcher matcher = COMPACT_DN.matcher(dn);
       dn = matcher.replaceAll("=");
 
       matcher = COMPACT_DN.matcher(basedn);
       basedn = matcher.replaceAll("=");
 
       LdapContext context = baseHandler.ldapService.getLdapContext();
 
       try
       {
          String[] edn = baseHandler.explodeDN(dn, false);
          String[] ebasedn = baseHandler.explodeDN(basedn, false);
 
          if (edn.length < 1)
             throw new IllegalArgumentException("Zerro DN length, [" + dn + "]");
          if (ebasedn.length < 1)
             throw new IllegalArgumentException("Zerro Base DN length, [" + basedn + "]");
          if (edn.length < ebasedn.length)
             throw new IllegalArgumentException("DN length smaller Base DN [" + dn + " >= " + basedn + "]");
 
          String rdn = basedn;
          for (int i = 1; i <= edn.length; i++)
          {
             // for (int i=edn.length - 1; i>=0; i--) {
             String n = edn[edn.length - i];
             if (i <= ebasedn.length)
             {
                String bn = ebasedn[ebasedn.length - i];
                if (!n.equals(bn))
                   throw new IllegalArgumentException("DN does not starts with Base DN [" + dn + " != " + basedn + "]");
             }
             else
             {
                // create RDN elem
               rdn = n + "," + rdn;
                createDN(rdn, context);
             }
          }
       }
       finally
       {
          baseHandler.ldapService.release(context);
       }
    }
 }
