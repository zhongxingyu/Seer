 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * $Id $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.common;
 
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
 import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
 import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlSelect;
 import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
 import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
 import com.gargoylesoftware.htmlunit.WebClient;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.lang.StringBuffer;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.List;
 
 public class FederationManager {
     private String amadmUrl;
     private String amUrl;
 
     public FederationManager(String url) {
         amUrl = url;
        amadmUrl = url + "/fmadm.jsp?cmd=";
     }
 
 
     /**
      * Add resource bundle to data store.
      *
      * @param webClient HTML Unit Web Client object.
      * @param bundlename Resource Bundle Name.
      * @param bundlefilename Resource bundle physical file name.
      * @param bundlelocale Locale of the resource bundle.
      */
     public HtmlPage addResourceBundle(
         WebClient webClient,
         String bundlename,
         String bundlefilename,
         String bundlelocale
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "add-resource-bundle");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (bundlename != null) {
             HtmlTextInput txtbundlename = (HtmlTextInput)form.getInputByName("bundlename");
             txtbundlename.setValueAttribute(bundlename);
         }
 
         if (bundlefilename != null) {
             HtmlTextArea tabundlefilename = (HtmlTextArea)form.getTextAreasByName("bundlefilename").get(0);
             tabundlefilename.setText(bundlefilename);
         }
 
         if (bundlelocale != null) {
             HtmlTextInput txtbundlelocale = (HtmlTextInput)form.getInputByName("bundlelocale");
             txtbundlelocale.setValueAttribute(bundlelocale);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * List resource bundle in data store.
      *
      * @param webClient HTML Unit Web Client object.
      * @param bundlename Resource Bundle Name.
      * @param bundlelocale Locale of the resource bundle.
      */
     public HtmlPage listResourceBundle(
         WebClient webClient,
         String bundlename,
         String bundlelocale
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "list-resource-bundle");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (bundlename != null) {
             HtmlTextInput txtbundlename = (HtmlTextInput)form.getInputByName("bundlename");
             txtbundlename.setValueAttribute(bundlename);
         }
 
         if (bundlelocale != null) {
             HtmlTextInput txtbundlelocale = (HtmlTextInput)form.getInputByName("bundlelocale");
             txtbundlelocale.setValueAttribute(bundlelocale);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Remove resource bundle from data store.
      *
      * @param webClient HTML Unit Web Client object.
      * @param bundlename Resource Bundle Name.
      * @param bundlelocale Locale of the resource bundle.
      */
     public HtmlPage removeResourceBundle(
         WebClient webClient,
         String bundlename,
         String bundlelocale
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "remove-resource-bundle");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (bundlename != null) {
             HtmlTextInput txtbundlename = (HtmlTextInput)form.getInputByName("bundlename");
             txtbundlename.setValueAttribute(bundlename);
         }
 
         if (bundlelocale != null) {
             HtmlTextInput txtbundlelocale = (HtmlTextInput)form.getInputByName("bundlelocale");
             txtbundlelocale.setValueAttribute(bundlelocale);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Create a new service in server.
      *
      * @param webClient HTML Unit Web Client object.
      * @param xmlfile XML file(s) that contains schema.
      */
     public HtmlPage createService(
         WebClient webClient,
         String xmlfile
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "create-service");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (xmlfile != null) {
             HtmlTextArea taxmlfile = (HtmlTextArea)form.getTextAreasByName("xmlfile").get(0);
             taxmlfile.setText(xmlfile);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Delete service from the server.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Service Name(s).
      * @param deletepolicyrule Delete policy rule.
      */
     public HtmlPage deleteService(
         WebClient webClient,
         List servicename,
         boolean deletepolicyrule
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "delete-service");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlSelect slservicename= (HtmlSelect)form.getSelectByName("servicename");
             String[] fakeOptions = new String[servicename.size()];
             int cnt = 0;
             for (Iterator i = servicename.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slservicename.fakeSelectedAttribute(fakeOptions);
         }
 
         HtmlCheckBoxInput cbdeletepolicyrule = (HtmlCheckBoxInput)form.getInputByName("deletepolicyrule");
         cbdeletepolicyrule.setChecked(deletepolicyrule);
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Update service.
      *
      * @param webClient HTML Unit Web Client object.
      * @param xmlfile XML file(s) that contains schema.
      */
     public HtmlPage updateService(
         WebClient webClient,
         String xmlfile
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "update-service");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (xmlfile != null) {
             HtmlTextArea taxmlfile = (HtmlTextArea)form.getTextAreasByName("xmlfile").get(0);
             taxmlfile.setText(xmlfile);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Add attribute schema to an existing service.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Service Name.
      * @param schematype Schema Type.
      * @param attributeschemafile XML file containing attribute schema definition.
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage addAttributes(
         WebClient webClient,
         String servicename,
         String schematype,
         String attributeschemafile,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "add-attributes");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributeschemafile != null) {
             HtmlTextArea taattributeschemafile = (HtmlTextArea)form.getTextAreasByName("attributeschemafile").get(0);
             taattributeschemafile.setText(attributeschemafile);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Migrate organization to realm.
      *
      * @param webClient HTML Unit Web Client object.
      * @param entrydn Distinguished name of organization to be migrated.
      */
     public HtmlPage doMigration70(
         WebClient webClient,
         String entrydn
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "do-migration70");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (entrydn != null) {
             HtmlTextInput txtentrydn = (HtmlTextInput)form.getInputByName("entrydn");
             txtentrydn.setValueAttribute(entrydn);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Create realm.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm to be created.
      */
     public HtmlPage createRealm(
         WebClient webClient,
         String realm
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "create-realm");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Delete realm.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm to be deleted.
      * @param recursive Delete descendent realms recursively.
      */
     public HtmlPage deleteRealm(
         WebClient webClient,
         String realm,
         boolean recursive
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "delete-realm");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         HtmlCheckBoxInput cbrecursive = (HtmlCheckBoxInput)form.getInputByName("recursive");
         cbrecursive.setChecked(recursive);
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * List realms by name.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm where search begins.
      * @param filter Filter (Pattern).
      * @param recursive Search recursively
      */
     public HtmlPage listRealms(
         WebClient webClient,
         String realm,
         String filter,
         boolean recursive
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "list-realms");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (filter != null) {
             HtmlTextInput txtfilter = (HtmlTextInput)form.getInputByName("filter");
             txtfilter.setValueAttribute(filter);
         }
 
         HtmlCheckBoxInput cbrecursive = (HtmlCheckBoxInput)form.getInputByName("recursive");
         cbrecursive.setChecked(recursive);
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Add service to a realm.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param servicename Service Name.
      * @param attributevalues Attribute values e.g. homeaddress=here.
      */
     public HtmlPage addServiceRealm(
         WebClient webClient,
         String realm,
         String servicename,
         List attributevalues
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "add-service-realm");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (attributevalues != null) {
             HtmlSelect slattributevalues= (HtmlSelect)form.getSelectByName("attributevalues");
             String[] fakeOptions = new String[attributevalues.size()];
             int cnt = 0;
             for (Iterator i = attributevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributevalues.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Show services in a realm.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param mandatory Include Mandatory services.
      */
     public HtmlPage showRealmServices(
         WebClient webClient,
         String realm,
         boolean mandatory
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "show-realm-services");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         HtmlCheckBoxInput cbmandatory = (HtmlCheckBoxInput)form.getInputByName("mandatory");
         cbmandatory.setChecked(mandatory);
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * List the assignable services to a realm.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      */
     public HtmlPage listRealmAssignableServices(
         WebClient webClient,
         String realm
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "list-realm-assignable-services");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Remove service from a realm.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param servicename Name of service to be removed.
      */
     public HtmlPage removeServiceRealm(
         WebClient webClient,
         String realm,
         String servicename
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "remove-service-realm");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Get realm property values.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param servicename Name of service.
      */
     public HtmlPage getRealm(
         WebClient webClient,
         String realm,
         String servicename
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "get-realm");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Show realm's service attribute values.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param servicename Name of service.
      */
     public HtmlPage showRealmServiceAttributes(
         WebClient webClient,
         String realm,
         String servicename
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "show-realm-service-attributes");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Delete attribute from a realm.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param servicename Name of service.
      * @param attributename Name of attribute to be removed.
      */
     public HtmlPage deleteRealmAttribute(
         WebClient webClient,
         String realm,
         String servicename,
         String attributename
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "delete-realm-attribute");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (attributename != null) {
             HtmlTextInput txtattributename = (HtmlTextInput)form.getInputByName("attributename");
             txtattributename.setValueAttribute(attributename);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set service attribute values in a realm.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param servicename Name of service.
      * @param attributevalues Attribute values e.g. homeaddress=here.
      */
     public HtmlPage setServiceAttributes(
         WebClient webClient,
         String realm,
         String servicename,
         List attributevalues
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-service-attributes");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (attributevalues != null) {
             HtmlSelect slattributevalues= (HtmlSelect)form.getSelectByName("attributevalues");
             String[] fakeOptions = new String[attributevalues.size()];
             int cnt = 0;
             for (Iterator i = attributevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributevalues.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Add attribute value to a realm.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param servicename Name of service.
      * @param attributevalues Attribute values e.g. homeaddress=here.
      */
     public HtmlPage addRealmAttributes(
         WebClient webClient,
         String realm,
         String servicename,
         List attributevalues
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "add-realm-attributes");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (attributevalues != null) {
             HtmlSelect slattributevalues= (HtmlSelect)form.getSelectByName("attributevalues");
             String[] fakeOptions = new String[attributevalues.size()];
             int cnt = 0;
             for (Iterator i = attributevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributevalues.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set attribute values of a realm.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param servicename Name of service.
      * @param attributevalues Attribute values e.g. homeaddress=here.
      */
     public HtmlPage setRealmAttributes(
         WebClient webClient,
         String realm,
         String servicename,
         List attributevalues
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-realm-attributes");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (attributevalues != null) {
             HtmlSelect slattributevalues= (HtmlSelect)form.getSelectByName("attributevalues");
             String[] fakeOptions = new String[attributevalues.size()];
             int cnt = 0;
             for (Iterator i = attributevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributevalues.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Create policies in a realm.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param xmlfile Name of file that contains policy XML definition.
      */
     public HtmlPage createPolicies(
         WebClient webClient,
         String realm,
         String xmlfile
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "create-policies");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (xmlfile != null) {
             HtmlTextArea taxmlfile = (HtmlTextArea)form.getTextAreasByName("xmlfile").get(0);
             taxmlfile.setText(xmlfile);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Delete policies from a realm.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param policynames Names of policy to be deleted.
      */
     public HtmlPage deletePolicies(
         WebClient webClient,
         String realm,
         List policynames
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "delete-policies");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (policynames != null) {
             HtmlSelect slpolicynames= (HtmlSelect)form.getSelectByName("policynames");
             String[] fakeOptions = new String[policynames.size()];
             int cnt = 0;
             for (Iterator i = policynames.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slpolicynames.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * List policy definitions in a realm.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param policynames Names of policy. This can be an wildcard. All policy definition in the realm will be returned if this option is not provided.
      */
     public HtmlPage listPolicies(
         WebClient webClient,
         String realm,
         List policynames
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "list-policies");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (policynames != null) {
             HtmlSelect slpolicynames= (HtmlSelect)form.getSelectByName("policynames");
             String[] fakeOptions = new String[policynames.size()];
             int cnt = 0;
             for (Iterator i = policynames.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slpolicynames.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Remove default attribute values in schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributenames Attribute name(s).
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage removeAttributeDefaults(
         WebClient webClient,
         String servicename,
         String schematype,
         List attributenames,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "remove-attribute-defaults");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributenames != null) {
             HtmlSelect slattributenames= (HtmlSelect)form.getSelectByName("attributenames");
             String[] fakeOptions = new String[attributenames.size()];
             int cnt = 0;
             for (Iterator i = attributenames.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributenames.fakeSelectedAttribute(fakeOptions);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Add default attribute values in schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributevalues Attribute values e.g. homeaddress=here.
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage addAttributeDefaults(
         WebClient webClient,
         String servicename,
         String schematype,
         List attributevalues,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "add-attribute-defaults");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributevalues != null) {
             HtmlSelect slattributevalues= (HtmlSelect)form.getSelectByName("attributevalues");
             String[] fakeOptions = new String[attributevalues.size()];
             int cnt = 0;
             for (Iterator i = attributevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributevalues.fakeSelectedAttribute(fakeOptions);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Show default attribute values in schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param subschemaname Name of sub schema.
      * @param attributenames Attribute name(s).
      */
     public HtmlPage showAttributeDefaults(
         WebClient webClient,
         String servicename,
         String schematype,
         String subschemaname,
         List attributenames
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "show-attribute-defaults");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
         if (attributenames != null) {
             HtmlSelect slattributenames= (HtmlSelect)form.getSelectByName("attributenames");
             String[] fakeOptions = new String[attributenames.size()];
             int cnt = 0;
             for (Iterator i = attributenames.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributenames.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set default attribute values in schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param subschemaname Name of sub schema.
      * @param attributevalues Attribute values e.g. homeaddress=here.
      */
     public HtmlPage setAttributeDefaults(
         WebClient webClient,
         String servicename,
         String schematype,
         String subschemaname,
         List attributevalues
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-attribute-defaults");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
         if (attributevalues != null) {
             HtmlSelect slattributevalues= (HtmlSelect)form.getSelectByName("attributevalues");
             String[] fakeOptions = new String[attributevalues.size()];
             int cnt = 0;
             for (Iterator i = attributevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributevalues.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set choice values of attribute schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributename Name of attribute.
      * @param add Set this flag to append the choice values to existing ones.
      * @param subschemaname Name of sub schema.
      * @param choicevalues Choice value e.g. o102=Inactive.
      */
     public HtmlPage setAttributeChoiceValues(
         WebClient webClient,
         String servicename,
         String schematype,
         String attributename,
         boolean add,
         String subschemaname,
         List choicevalues
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-attribute-choice-values");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributename != null) {
             HtmlTextInput txtattributename = (HtmlTextInput)form.getInputByName("attributename");
             txtattributename.setValueAttribute(attributename);
         }
 
         HtmlCheckBoxInput cbadd = (HtmlCheckBoxInput)form.getInputByName("add");
         cbadd.setChecked(add);
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
         if (choicevalues != null) {
             HtmlSelect slchoicevalues= (HtmlSelect)form.getSelectByName("choicevalues");
             String[] fakeOptions = new String[choicevalues.size()];
             int cnt = 0;
             for (Iterator i = choicevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slchoicevalues.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set boolean values of attribute schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributename Name of attribute.
      * @param truevalue Value for true.
      * @param truei18nkey Internationalization key for true value.
      * @param falsevalue Value for false.
      * @param falsei18nkey Internationalization key for false value.
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage setAttributeBooleanValues(
         WebClient webClient,
         String servicename,
         String schematype,
         String attributename,
         String truevalue,
         String truei18nkey,
         String falsevalue,
         String falsei18nkey,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-attribute-boolean-values");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributename != null) {
             HtmlTextInput txtattributename = (HtmlTextInput)form.getInputByName("attributename");
             txtattributename.setValueAttribute(attributename);
         }
 
         if (truevalue != null) {
             HtmlTextInput txttruevalue = (HtmlTextInput)form.getInputByName("truevalue");
             txttruevalue.setValueAttribute(truevalue);
         }
 
         if (truei18nkey != null) {
             HtmlTextInput txttruei18nkey = (HtmlTextInput)form.getInputByName("truei18nkey");
             txttruei18nkey.setValueAttribute(truei18nkey);
         }
 
         if (falsevalue != null) {
             HtmlTextInput txtfalsevalue = (HtmlTextInput)form.getInputByName("falsevalue");
             txtfalsevalue.setValueAttribute(falsevalue);
         }
 
         if (falsei18nkey != null) {
             HtmlTextInput txtfalsei18nkey = (HtmlTextInput)form.getInputByName("falsei18nkey");
             txtfalsei18nkey.setValueAttribute(falsei18nkey);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Remove choice values from attribute schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributename Name of attribute.
      * @param choicevalues Choice values e.g. Inactive
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage removeAttributeChoiceValues(
         WebClient webClient,
         String servicename,
         String schematype,
         String attributename,
         List choicevalues,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "remove-attribute-choice-values");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributename != null) {
             HtmlTextInput txtattributename = (HtmlTextInput)form.getInputByName("attributename");
             txtattributename.setValueAttribute(attributename);
         }
 
         if (choicevalues != null) {
             HtmlSelect slchoicevalues= (HtmlSelect)form.getSelectByName("choicevalues");
             String[] fakeOptions = new String[choicevalues.size()];
             int cnt = 0;
             for (Iterator i = choicevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slchoicevalues.fakeSelectedAttribute(fakeOptions);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set type member of attribute schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributeschema Name of attribute schema
      * @param type Attribute Schema Type
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage setAttributeType(
         WebClient webClient,
         String servicename,
         String schematype,
         String attributeschema,
         String type,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-attribute-type");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributeschema != null) {
             HtmlTextInput txtattributeschema = (HtmlTextInput)form.getInputByName("attributeschema");
             txtattributeschema.setValueAttribute(attributeschema);
         }
 
         if (type != null) {
             HtmlTextInput txttype = (HtmlTextInput)form.getInputByName("type");
             txttype.setValueAttribute(type);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set UI type member of attribute schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributeschema Name of attribute schema
      * @param uitype Attribute Schema UI Type
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage setAttributeUiType(
         WebClient webClient,
         String servicename,
         String schematype,
         String attributeschema,
         String uitype,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-attribute-ui-type");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributeschema != null) {
             HtmlTextInput txtattributeschema = (HtmlTextInput)form.getInputByName("attributeschema");
             txtattributeschema.setValueAttribute(attributeschema);
         }
 
         if (uitype != null) {
             HtmlTextInput txtuitype = (HtmlTextInput)form.getInputByName("uitype");
             txtuitype.setValueAttribute(uitype);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set syntax member of attribute schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributeschema Name of attribute schema
      * @param syntax Attribute Schema Syntax
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage setAttributeSyntax(
         WebClient webClient,
         String servicename,
         String schematype,
         String attributeschema,
         String syntax,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-attribute-syntax");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributeschema != null) {
             HtmlTextInput txtattributeschema = (HtmlTextInput)form.getInputByName("attributeschema");
             txtattributeschema.setValueAttribute(attributeschema);
         }
 
         if (syntax != null) {
             HtmlTextInput txtsyntax = (HtmlTextInput)form.getInputByName("syntax");
             txtsyntax.setValueAttribute(syntax);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set i18nKey member of attribute schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributeschema Name of attribute schema
      * @param i18nkey Attribute Schema I18n Key
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage setAttributeI18nKey(
         WebClient webClient,
         String servicename,
         String schematype,
         String attributeschema,
         String i18nkey,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-attribute-i18n-key");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributeschema != null) {
             HtmlTextInput txtattributeschema = (HtmlTextInput)form.getInputByName("attributeschema");
             txtattributeschema.setValueAttribute(attributeschema);
         }
 
         if (i18nkey != null) {
             HtmlTextInput txti18nkey = (HtmlTextInput)form.getInputByName("i18nkey");
             txti18nkey.setValueAttribute(i18nkey);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set properties view bean URL member of attribute schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributeschema Name of attribute schema
      * @param url Attribute Schema Properties View Bean URL
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage setAttributeViewBeanUrl(
         WebClient webClient,
         String servicename,
         String schematype,
         String attributeschema,
         String url,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-attribute-view-bean-url");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributeschema != null) {
             HtmlTextInput txtattributeschema = (HtmlTextInput)form.getInputByName("attributeschema");
             txtattributeschema.setValueAttribute(attributeschema);
         }
 
         if (url != null) {
             HtmlTextInput txturl = (HtmlTextInput)form.getInputByName("url");
             txturl.setValueAttribute(url);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set any member of attribute schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributeschema Name of attribute schema
      * @param any Attribute Schema Any value
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage setAttributeAny(
         WebClient webClient,
         String servicename,
         String schematype,
         String attributeschema,
         String any,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-attribute-any");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributeschema != null) {
             HtmlTextInput txtattributeschema = (HtmlTextInput)form.getInputByName("attributeschema");
             txtattributeschema.setValueAttribute(attributeschema);
         }
 
         if (any != null) {
             HtmlTextInput txtany = (HtmlTextInput)form.getInputByName("any");
             txtany.setValueAttribute(any);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Delete attribute schema default values.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributeschema Name of attribute schema
      * @param defaultvalues Default value(s) to be deleted
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage deleteAttributeDefaultValues(
         WebClient webClient,
         String servicename,
         String schematype,
         String attributeschema,
         List defaultvalues,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "delete-attribute-default-values");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributeschema != null) {
             HtmlTextInput txtattributeschema = (HtmlTextInput)form.getInputByName("attributeschema");
             txtattributeschema.setValueAttribute(attributeschema);
         }
 
         if (defaultvalues != null) {
             HtmlSelect sldefaultvalues= (HtmlSelect)form.getSelectByName("defaultvalues");
             String[] fakeOptions = new String[defaultvalues.size()];
             int cnt = 0;
             for (Iterator i = defaultvalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             sldefaultvalues.fakeSelectedAttribute(fakeOptions);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set attribute schema validator.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributeschema Name of attribute schema
      * @param validator validator class name
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage setAttributeValidator(
         WebClient webClient,
         String servicename,
         String schematype,
         String attributeschema,
         String validator,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-attribute-validator");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributeschema != null) {
             HtmlTextInput txtattributeschema = (HtmlTextInput)form.getInputByName("attributeschema");
             txtattributeschema.setValueAttribute(attributeschema);
         }
 
         if (validator != null) {
             HtmlTextInput txtvalidator = (HtmlTextInput)form.getInputByName("validator");
             txtvalidator.setValueAttribute(validator);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set attribute schema start range.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributeschema Name of attribute schema
      * @param range Start range
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage setAttributeStartRange(
         WebClient webClient,
         String servicename,
         String schematype,
         String attributeschema,
         String range,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-attribute-start-range");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributeschema != null) {
             HtmlTextInput txtattributeschema = (HtmlTextInput)form.getInputByName("attributeschema");
             txtattributeschema.setValueAttribute(attributeschema);
         }
 
         if (range != null) {
             HtmlTextInput txtrange = (HtmlTextInput)form.getInputByName("range");
             txtrange.setValueAttribute(range);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set attribute schema end range.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributeschema Name of attribute schema
      * @param range End range
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage setAttributeEndRange(
         WebClient webClient,
         String servicename,
         String schematype,
         String attributeschema,
         String range,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-attribute-end-range");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributeschema != null) {
             HtmlTextInput txtattributeschema = (HtmlTextInput)form.getInputByName("attributeschema");
             txtattributeschema.setValueAttribute(attributeschema);
         }
 
         if (range != null) {
             HtmlTextInput txtrange = (HtmlTextInput)form.getInputByName("range");
             txtrange.setValueAttribute(range);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Delete attribute schemas from a service
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param attributeschema Name of attribute schema to be removed.
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage deleteAttribute(
         WebClient webClient,
         String servicename,
         String schematype,
         List attributeschema,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "delete-attribute");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (attributeschema != null) {
             HtmlSelect slattributeschema= (HtmlSelect)form.getSelectByName("attributeschema");
             String[] fakeOptions = new String[attributeschema.size()];
             int cnt = 0;
             for (Iterator i = attributeschema.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributeschema.fakeSelectedAttribute(fakeOptions);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set service schema i18n key.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param i18nkey I18n Key.
      */
     public HtmlPage setServiceI18nKey(
         WebClient webClient,
         String servicename,
         String i18nkey
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-service-i18n-key");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (i18nkey != null) {
             HtmlTextInput txti18nkey = (HtmlTextInput)form.getInputByName("i18nkey");
             txti18nkey.setValueAttribute(i18nkey);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set service schema properties view bean URL.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param url Service Schema Properties View Bean URL
      */
     public HtmlPage setServiceViewBeanUrl(
         WebClient webClient,
         String servicename,
         String url
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-service-view-bean-url");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (url != null) {
             HtmlTextInput txturl = (HtmlTextInput)form.getInputByName("url");
             txturl.setValueAttribute(url);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set service schema revision number.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param revisionnumber Revision Number
      */
     public HtmlPage setRevisionNumber(
         WebClient webClient,
         String servicename,
         String revisionnumber
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-revision-number");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (revisionnumber != null) {
             HtmlTextInput txtrevisionnumber = (HtmlTextInput)form.getInputByName("revisionnumber");
             txtrevisionnumber.setValueAttribute(revisionnumber);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Get service schema revision number.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      */
     public HtmlPage getRevisionNumber(
         WebClient webClient,
         String servicename
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "get-revision-number");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Create a new sub configuration.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param subconfigname Name of sub configuration.
      * @param attributevalues Attribute values e.g. homeaddress=here.
      * @param realm Name of realm (Sub Configuration shall be added to global configuration if this option is not provided).
      * @param subconfigid ID of parent configuration(Sub Configuration shall be added to root configuration if this option is not provided).
      */
     public HtmlPage createSubConfiguration(
         WebClient webClient,
         String servicename,
         String subconfigname,
         List attributevalues,
         String realm,
         String subconfigid
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "create-sub-configuration");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (subconfigname != null) {
             HtmlTextInput txtsubconfigname = (HtmlTextInput)form.getInputByName("subconfigname");
             txtsubconfigname.setValueAttribute(subconfigname);
         }
 
         if (attributevalues != null) {
             HtmlSelect slattributevalues= (HtmlSelect)form.getSelectByName("attributevalues");
             String[] fakeOptions = new String[attributevalues.size()];
             int cnt = 0;
             for (Iterator i = attributevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributevalues.fakeSelectedAttribute(fakeOptions);
         }
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (subconfigid != null) {
             HtmlTextInput txtsubconfigid = (HtmlTextInput)form.getInputByName("subconfigid");
             txtsubconfigid.setValueAttribute(subconfigid);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Remove Sub Configuration.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param subconfigname Name of sub configuration.
      * @param realm Name of realm (Sub Configuration shall be added to global configuration if this option is not provided).
      */
     public HtmlPage deleteSubConfiguration(
         WebClient webClient,
         String servicename,
         String subconfigname,
         String realm
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "delete-sub-configuration");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (subconfigname != null) {
             HtmlTextInput txtsubconfigname = (HtmlTextInput)form.getInputByName("subconfigname");
             txtsubconfigname.setValueAttribute(subconfigname);
         }
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set sub configuration.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param subconfigname Name of sub configuration.
      * @param operation Operation (either add/set/modify) to be performed on the sub configuration.
      * @param attributevalues Attribute values e.g. homeaddress=here.
      * @param realm Name of realm (Sub Configuration shall be added to global configuration if this option is not provided).
      */
     public HtmlPage setSubConfiguration(
         WebClient webClient,
         String servicename,
         String subconfigname,
         String operation,
         List attributevalues,
         String realm
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-sub-configuration");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (subconfigname != null) {
             HtmlTextInput txtsubconfigname = (HtmlTextInput)form.getInputByName("subconfigname");
             txtsubconfigname.setValueAttribute(subconfigname);
         }
 
         if (operation != null) {
             HtmlTextInput txtoperation = (HtmlTextInput)form.getInputByName("operation");
             txtoperation.setValueAttribute(operation);
         }
 
         if (attributevalues != null) {
             HtmlSelect slattributevalues= (HtmlSelect)form.getSelectByName("attributevalues");
             String[] fakeOptions = new String[attributevalues.size()];
             int cnt = 0;
             for (Iterator i = attributevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributevalues.fakeSelectedAttribute(fakeOptions);
         }
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Add sub schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param filename Name of file that contains the schema
      * @param subschemaname Name of sub schema.
      */
     public HtmlPage addSubSchema(
         WebClient webClient,
         String servicename,
         String schematype,
         String filename,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "add-sub-schema");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (filename != null) {
             HtmlTextArea tafilename = (HtmlTextArea)form.getTextAreasByName("filename").get(0);
             tafilename.setText(filename);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Remove sub schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param subschemanames Name(s) of sub schema to be removed.
      * @param subschemaname Name of parent sub schema.
      */
     public HtmlPage removeSubSchema(
         WebClient webClient,
         String servicename,
         String schematype,
         List subschemanames,
         String subschemaname
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "remove-sub-schema");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (subschemanames != null) {
             HtmlSelect slsubschemanames= (HtmlSelect)form.getSelectByName("subschemanames");
             String[] fakeOptions = new String[subschemanames.size()];
             int cnt = 0;
             for (Iterator i = subschemanames.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slsubschemanames.fakeSelectedAttribute(fakeOptions);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set Inheritance value of Sub Schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param schematype Type of schema.
      * @param subschemaname Name of sub schema.
      * @param inheritance Value of Inheritance.
      */
     public HtmlPage setInheritance(
         WebClient webClient,
         String servicename,
         String schematype,
         String subschemaname,
         String inheritance
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-inheritance");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (schematype != null) {
             HtmlTextInput txtschematype = (HtmlTextInput)form.getInputByName("schematype");
             txtschematype.setValueAttribute(schematype);
         }
 
         if (subschemaname != null) {
             HtmlTextInput txtsubschemaname = (HtmlTextInput)form.getInputByName("subschemaname");
             txtsubschemaname.setValueAttribute(subschemaname);
         }
 
         if (inheritance != null) {
             HtmlTextInput txtinheritance = (HtmlTextInput)form.getInputByName("inheritance");
             txtinheritance.setValueAttribute(inheritance);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Add Plug-in interface to service.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param interfacename Name of interface.
      * @param pluginname Name of Plug-in.
      * @param i18nkey Plug-in I18n Key.
      */
     public HtmlPage addPluginInterface(
         WebClient webClient,
         String servicename,
         String interfacename,
         String pluginname,
         String i18nkey
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "add-plugin-interface");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (interfacename != null) {
             HtmlTextInput txtinterfacename = (HtmlTextInput)form.getInputByName("interfacename");
             txtinterfacename.setValueAttribute(interfacename);
         }
 
         if (pluginname != null) {
             HtmlTextInput txtpluginname = (HtmlTextInput)form.getInputByName("pluginname");
             txtpluginname.setValueAttribute(pluginname);
         }
 
         if (i18nkey != null) {
             HtmlTextInput txti18nkey = (HtmlTextInput)form.getInputByName("i18nkey");
             txti18nkey.setValueAttribute(i18nkey);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set properties view bean URL of plug-in schema.
      *
      * @param webClient HTML Unit Web Client object.
      * @param servicename Name of service.
      * @param interfacename Name of interface.
      * @param pluginname Name of Plug-in.
      * @param url Properties view bean URL.
      */
     public HtmlPage setPluginSchemaViewBeanUrl(
         WebClient webClient,
         String servicename,
         String interfacename,
         String pluginname,
         String url
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-plugin-schema-view-bean-url");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (interfacename != null) {
             HtmlTextInput txtinterfacename = (HtmlTextInput)form.getInputByName("interfacename");
             txtinterfacename.setValueAttribute(interfacename);
         }
 
         if (pluginname != null) {
             HtmlTextInput txtpluginname = (HtmlTextInput)form.getInputByName("pluginname");
             txtpluginname.setValueAttribute(pluginname);
         }
 
         if (url != null) {
             HtmlTextInput txturl = (HtmlTextInput)form.getInputByName("url");
             txturl.setValueAttribute(url);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Create identity in a realm
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idname Name of identity.
      * @param idtype Type of Identity such as User, Role and Group.
      * @param attributevalues Attribute values e.g. sunIdentityServerDeviceStatus=Active.
      */
     public HtmlPage createIdentity(
         WebClient webClient,
         String realm,
         String idname,
         String idtype,
         List attributevalues
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "create-identity");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
         if (attributevalues != null) {
             HtmlSelect slattributevalues= (HtmlSelect)form.getSelectByName("attributevalues");
             String[] fakeOptions = new String[attributevalues.size()];
             int cnt = 0;
             for (Iterator i = attributevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributevalues.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Delete identities in a realm
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idnames Names of identites.
      * @param idtype Type of Identity such as User, Role and Group.
      */
     public HtmlPage deleteIdentities(
         WebClient webClient,
         String realm,
         List idnames,
         String idtype
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "delete-identities");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idnames != null) {
             HtmlSelect slidnames= (HtmlSelect)form.getSelectByName("idnames");
             String[] fakeOptions = new String[idnames.size()];
             int cnt = 0;
             for (Iterator i = idnames.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slidnames.fakeSelectedAttribute(fakeOptions);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * List identities in a realm
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param filter Filter (Pattern).
      * @param idtype Type of Identity such as User, Role and Group.
      */
     public HtmlPage listIdentities(
         WebClient webClient,
         String realm,
         String filter,
         String idtype
        
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "list-identities");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (filter != null) {
             HtmlTextInput txtfilter = (HtmlTextInput)form.getInputByName("filter");
             txtfilter.setValueAttribute(filter);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Show the allowed operations of an identity a realm
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idtype Type of Identity such as User, Role and Group.
      */
     public HtmlPage showIdentityOperations(
         WebClient webClient,
         String realm,
         String idtype
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "show-identity-operations");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Show the supported identity type in a realm
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      */
     public HtmlPage showIdentityTypes(
         WebClient webClient,
         String realm
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "show-identity-types");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * List the assignable service to an identity
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idname Name of identity.
      * @param idtype Type of Identity such as User, Role and Group.
      */
     public HtmlPage listIdentityAssignableServices(
         WebClient webClient,
         String realm,
         String idname,
         String idtype
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "list-identity-assignable-services");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Show the service in an identity
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idname Name of identity.
      * @param idtype Type of Identity such as User, Role and Group.
      */
     public HtmlPage showIdentityServices(
         WebClient webClient,
         String realm,
         String idname,
         String idtype
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "show-identity-services");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Show the service attribute values of an identity
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idname Name of identity.
      * @param idtype Type of Identity such as User, Role and Group.
      * @param servicename Name of service.
      */
     public HtmlPage showIdentityServiceAttributes(
         WebClient webClient,
         String realm,
         String idname,
         String idtype,
         String servicename
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "show-identity-service-attributes");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Get identity property values
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idname Name of identity.
      * @param idtype Type of Identity such as User, Role and Group.
      * @param attributenames Attribute name(s). All attribute values shall be returned if the option is not provided.
      */
     public HtmlPage getIdentity(
         WebClient webClient,
         String realm,
         String idname,
         String idtype,
         List attributenames
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "get-identity");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
         if (attributenames != null) {
             HtmlSelect slattributenames= (HtmlSelect)form.getSelectByName("attributenames");
             String[] fakeOptions = new String[attributenames.size()];
             int cnt = 0;
             for (Iterator i = attributenames.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributenames.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Show the memberships of an identity. For sample show the memberships of an user.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idname Name of identity.
      * @param idtype Type of Identity such as User, Role and Group.
      * @param membershipidtype Membership identity type.
      */
     public HtmlPage showMemberships(
         WebClient webClient,
         String realm,
         String idname,
         String idtype,
         String membershipidtype
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "show-memberships");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
         if (membershipidtype != null) {
             HtmlTextInput txtmembershipidtype = (HtmlTextInput)form.getInputByName("membershipidtype");
             txtmembershipidtype.setValueAttribute(membershipidtype);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Show the members of an identity. For example show the members of a role
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idname Name of identity.
      * @param idtype Type of Identity such as User, Role and Group.
      * @param membershipidtype Membership identity type.
      */
     public HtmlPage showMembers(
         WebClient webClient,
         String realm,
         String idname,
         String idtype,
         String membershipidtype
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "show-members");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
         if (membershipidtype != null) {
             HtmlTextInput txtmembershipidtype = (HtmlTextInput)form.getInputByName("membershipidtype");
             txtmembershipidtype.setValueAttribute(membershipidtype);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Add an identity as member of another identity
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param memberidname Name of identity that is member.
      * @param memberidtype Type of Identity of member such as User, Role and Group.
      * @param idname Name of identity.
      * @param idtype Type of Identity
      */
     public HtmlPage addMember(
         WebClient webClient,
         String realm,
         String memberidname,
         String memberidtype,
         String idname,
         String idtype
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "add-member");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (memberidname != null) {
             HtmlTextInput txtmemberidname = (HtmlTextInput)form.getInputByName("memberidname");
             txtmemberidname.setValueAttribute(memberidname);
         }
 
         if (memberidtype != null) {
             HtmlTextInput txtmemberidtype = (HtmlTextInput)form.getInputByName("memberidtype");
             txtmemberidtype.setValueAttribute(memberidtype);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Remove membership of identity from another identity
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param memberidname Name of identity that is member.
      * @param memberidtype Type of Identity of member such as User, Role and Group.
      * @param idname Name of identity.
      * @param idtype Type of Identity
      */
     public HtmlPage removeMember(
         WebClient webClient,
         String realm,
         String memberidname,
         String memberidtype,
         String idname,
         String idtype
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "remove-member");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (memberidname != null) {
             HtmlTextInput txtmemberidname = (HtmlTextInput)form.getInputByName("memberidname");
             txtmemberidname.setValueAttribute(memberidname);
         }
 
         if (memberidtype != null) {
             HtmlTextInput txtmemberidtype = (HtmlTextInput)form.getInputByName("memberidtype");
             txtmemberidtype.setValueAttribute(memberidtype);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Add Service to an identity
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idname Name of identity.
      * @param idtype Type of Identity such as User, Role and Group.
      * @param servicename Name of service.
      * @param attributevalues Attribute values e.g. homeaddress=here.
      */
     public HtmlPage addServiceIdentity(
         WebClient webClient,
         String realm,
         String idname,
         String idtype,
         String servicename,
         List attributevalues
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "add-service-identity");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (attributevalues != null) {
             HtmlSelect slattributevalues= (HtmlSelect)form.getSelectByName("attributevalues");
             String[] fakeOptions = new String[attributevalues.size()];
             int cnt = 0;
             for (Iterator i = attributevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributevalues.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Remove Service from an identity
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idname Name of identity.
      * @param idtype Type of Identity such as User, Role and Group.
      * @param servicename Name of service.
      */
     public HtmlPage removeServiceIdentity(
         WebClient webClient,
         String realm,
         String idname,
         String idtype,
         String servicename
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "remove-service-identity");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set service attribute values of an identity
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idname Name of identity.
      * @param idtype Type of Identity such as User, Role and Group.
      * @param servicename Name of service.
      * @param attributevalues Attribute values e.g. homeaddress=here.
      */
     public HtmlPage setIdentityServiceAttributes(
         WebClient webClient,
         String realm,
         String idname,
         String idtype,
         String servicename,
         List attributevalues
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-identity-service-attributes");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
         if (servicename != null) {
             HtmlTextInput txtservicename = (HtmlTextInput)form.getInputByName("servicename");
             txtservicename.setValueAttribute(servicename);
         }
 
         if (attributevalues != null) {
             HtmlSelect slattributevalues= (HtmlSelect)form.getSelectByName("attributevalues");
             String[] fakeOptions = new String[attributevalues.size()];
             int cnt = 0;
             for (Iterator i = attributevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributevalues.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set attribute values of an identity
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idname Name of identity.
      * @param idtype Type of Identity such as User, Role and Group.
      * @param attributevalues Attribute values e.g. homeaddress=here.
      */
     public HtmlPage setIdentityAttributes(
         WebClient webClient,
         String realm,
         String idname,
         String idtype,
         List attributevalues
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "set-identity-attributes");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
         if (attributevalues != null) {
             HtmlSelect slattributevalues= (HtmlSelect)form.getSelectByName("attributevalues");
             String[] fakeOptions = new String[attributevalues.size()];
             int cnt = 0;
             for (Iterator i = attributevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributevalues.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Show privileges assigned to an identity
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idname Name of identity.
      * @param idtype Type of Identity such Role and Group.
      */
     public HtmlPage showPrivileges(
         WebClient webClient,
         String realm,
         String idname,
         String idtype
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "show-privileges");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Add privileges to an identity
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idname Name of identity.
      * @param idtype Type of Identity such as Role and Group.
      * @param privileges Name of privileges to be added.
      */
     public HtmlPage addPrivileges(
         WebClient webClient,
         String realm,
         String idname,
         String idtype,
         List privileges
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "add-privileges");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
         if (privileges != null) {
             HtmlSelect slprivileges= (HtmlSelect)form.getSelectByName("privileges");
             String[] fakeOptions = new String[privileges.size()];
             int cnt = 0;
             for (Iterator i = privileges.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slprivileges.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Remove privileges from an identity
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param idname Name of identity.
      * @param idtype Type of Identity such as Role and Group.
      * @param privileges Name of privileges to be removed.
      */
     public HtmlPage removePrivileges(
         WebClient webClient,
         String realm,
         String idname,
         String idtype,
         List privileges
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "remove-privileges");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (idname != null) {
             HtmlTextInput txtidname = (HtmlTextInput)form.getInputByName("idname");
             txtidname.setValueAttribute(idname);
         }
 
         if (idtype != null) {
             HtmlTextInput txtidtype = (HtmlTextInput)form.getInputByName("idtype");
             txtidtype.setValueAttribute(idtype);
         }
 
         if (privileges != null) {
             HtmlSelect slprivileges= (HtmlSelect)form.getSelectByName("privileges");
             String[] fakeOptions = new String[privileges.size()];
             int cnt = 0;
             for (Iterator i = privileges.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slprivileges.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * List authentication instances
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      */
     public HtmlPage listAuthInstances(
         WebClient webClient,
         String realm
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "list-auth-instances");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Create authentication instance
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param name Name of authentication instance.
      * @param authtype Type of authentication instance e.g. LDAP, Datastore.
      */
     public HtmlPage createAuthInstance(
         WebClient webClient,
         String realm,
         String name,
         String authtype
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "create-auth-instance");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (name != null) {
             HtmlTextInput txtname = (HtmlTextInput)form.getInputByName("name");
             txtname.setValueAttribute(name);
         }
 
         if (authtype != null) {
             HtmlTextInput txtauthtype = (HtmlTextInput)form.getInputByName("authtype");
             txtauthtype.setValueAttribute(authtype);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Delete authentication instances
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param names Name of authentication instances.
      */
     public HtmlPage deleteAuthInstances(
         WebClient webClient,
         String realm,
         List names
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "delete-auth-instances");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (names != null) {
             HtmlSelect slnames= (HtmlSelect)form.getSelectByName("names");
             String[] fakeOptions = new String[names.size()];
             int cnt = 0;
             for (Iterator i = names.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slnames.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Update authentication instance values
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param name Name of authentication instance.
      * @param attributevalues Attribute values e.g. homeaddress=here.
      */
     public HtmlPage updateAuthInstance(
         WebClient webClient,
         String realm,
         String name,
         List attributevalues
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "update-auth-instance");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (name != null) {
             HtmlTextInput txtname = (HtmlTextInput)form.getInputByName("name");
             txtname.setValueAttribute(name);
         }
 
         if (attributevalues != null) {
             HtmlSelect slattributevalues= (HtmlSelect)form.getSelectByName("attributevalues");
             String[] fakeOptions = new String[attributevalues.size()];
             int cnt = 0;
             for (Iterator i = attributevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributevalues.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Get authentication instance values
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param name Name of authentication instance.
      */
     public HtmlPage getAuthInstance(
         WebClient webClient,
         String realm,
         String name
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "get-auth-instance");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (name != null) {
             HtmlTextInput txtname = (HtmlTextInput)form.getInputByName("name");
             txtname.setValueAttribute(name);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * List authentication configurations
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      */
     public HtmlPage listAuthConfigurations(
         WebClient webClient,
         String realm
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "list-auth-configurations");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Create authentication configuration
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param name Name of authentication configuration.
      */
     public HtmlPage createAuthConfiguration(
         WebClient webClient,
         String realm,
         String name
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "create-auth-configuration");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (name != null) {
             HtmlTextInput txtname = (HtmlTextInput)form.getInputByName("name");
             txtname.setValueAttribute(name);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Delete authentication configurations
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param names Name of authentication configurations.
      */
     public HtmlPage deleteAuthConfigurations(
         WebClient webClient,
         String realm,
         List names
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "delete-auth-configurations");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (names != null) {
             HtmlSelect slnames= (HtmlSelect)form.getSelectByName("names");
             String[] fakeOptions = new String[names.size()];
             int cnt = 0;
             for (Iterator i = names.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slnames.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Get authentication configuration entries
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param name Name of authentication configuration.
      */
     public HtmlPage getAuthConfigurationEntries(
         WebClient webClient,
         String realm,
         String name
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "get-auth-configuration-entries");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (name != null) {
             HtmlTextInput txtname = (HtmlTextInput)form.getInputByName("name");
             txtname.setValueAttribute(name);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Set authentication configuration entries
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param name Name of authentication configuration.
      * @param entries formatted authentication configuration entries in this format name&#124;flag&#124;options. option can be REQUIRED, OPTIONAL, SUFFICIENT, REQUISITE. e.g. myauthmodule&#124;REQUIRED&#124;my options.
      * @param datafile Name of file that contains formatted authentication configuration entries in this format name&#124;flag&#124;options. option can be REQUIRED, OPTIONAL, SUFFICIENT, REQUISITE. e.g. myauthmodule&#124;REQUIRED&#124;my options.
      */
     public HtmlPage updateAuthConfigurationEntries(
         WebClient webClient,
         String realm,
         String name,
         List entries,
         String datafile
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "update-auth-configuration-entries");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (name != null) {
             HtmlTextInput txtname = (HtmlTextInput)form.getInputByName("name");
             txtname.setValueAttribute(name);
         }
 
         if (entries != null) {
             HtmlSelect slentries= (HtmlSelect)form.getSelectByName("entries");
             String[] fakeOptions = new String[entries.size()];
             int cnt = 0;
             for (Iterator i = entries.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slentries.fakeSelectedAttribute(fakeOptions);
         }
 
         if (datafile != null) {
             HtmlTextArea tadatafile = (HtmlTextArea)form.getTextAreasByName("datafile").get(0);
             tadatafile.setText(datafile);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * List data stores under a realm
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      */
     public HtmlPage listDatastores(
         WebClient webClient,
         String realm
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "list-datastores");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Create data store under a realm
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param name Name of datastore.
      * @param datatype Type of datastore.
      * @param attributevalues Attribute values e.g. sunIdRepoClass=com.sun.identity.idm.plugins.files.FilesRepo.
      */
     public HtmlPage createDatastore(
         WebClient webClient,
         String realm,
         String name,
         String datatype,
         List attributevalues
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "create-datastore");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (name != null) {
             HtmlTextInput txtname = (HtmlTextInput)form.getInputByName("name");
             txtname.setValueAttribute(name);
         }
 
         if (datatype != null) {
             HtmlTextInput txtdatatype = (HtmlTextInput)form.getInputByName("datatype");
             txtdatatype.setValueAttribute(datatype);
         }
 
         if (attributevalues != null) {
             HtmlSelect slattributevalues= (HtmlSelect)form.getSelectByName("attributevalues");
             String[] fakeOptions = new String[attributevalues.size()];
             int cnt = 0;
             for (Iterator i = attributevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributevalues.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Delete data stores under a realm
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param names Names of datastore.
      */
     public HtmlPage deleteDatastores(
         WebClient webClient,
         String realm,
         List names
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "delete-datastores");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (names != null) {
             HtmlSelect slnames= (HtmlSelect)form.getSelectByName("names");
             String[] fakeOptions = new String[names.size()];
             int cnt = 0;
             for (Iterator i = names.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slnames.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Update data store profile.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Name of realm.
      * @param name Name of datastore.
      * @param attributevalues Attribute values e.g. sunIdRepoClass=com.sun.identity.idm.plugins.files.FilesRepo.
      */
     public HtmlPage updateDatastore(
         WebClient webClient,
         String realm,
         String name,
         List attributevalues
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "update-datastore");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (name != null) {
             HtmlTextInput txtname = (HtmlTextInput)form.getInputByName("name");
             txtname.setValueAttribute(name);
         }
 
         if (attributevalues != null) {
             HtmlSelect slattributevalues= (HtmlSelect)form.getSelectByName("attributevalues");
             String[] fakeOptions = new String[attributevalues.size()];
             int cnt = 0;
             for (Iterator i = attributevalues.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             slattributevalues.fakeSelectedAttribute(fakeOptions);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Create new metadata template.
      *
      * @param webClient HTML Unit Web Client object.
      * @param entityid Entity ID
      * @param metadata Specify file name for the standard metadata to be created.
      * @param extended Specify file name for the standard metadata to be created.
      * @param serviceprovider Specify metaAlias for hosted service provider to be created. The format must be <realm name>/<identifier>.
      * @param identityprovider Specify metaAlias for hosted identity provider to be created. The format must be <realm name>/<identifier>.
      * @param xacmlpep Specify metaAlias for policy enforcement point to be created. The format must be <realm name>/<identifier>.
      * @param xacmlpdp Specify metaAlias for policy decision point to be created. The format must be <realm name>/<identifier>.
      * @param spscertalias Service provider signing certificate alias
      * @param idpscertalias Identity provider signing certificate alias
      * @param xacmlpdpscertalias Policy decision point signing certificate alias
      * @param xacmlpepscertalias Policy enforcement point signing certificate alias
      * @param specertalias Service provider encryption certificate alias
      * @param idpecertalias Identity provider encryption certificate alias.
      * @param xacmlpdpecertalias Policy decision point encryption certificate alias
      * @param xacmlpepecertalias Policy enforcement point encryption certificate alias
      * @param spec Specify metadata specification, either idff or saml2, defaults to saml2
      */
     public HtmlPage createMetadataTemplate(
         WebClient webClient,
         String entityid,
         boolean metadata,
         boolean extended,
         String serviceprovider,
         String identityprovider,
         String xacmlpep,
         String xacmlpdp,
         String spscertalias,
         String idpscertalias,
         String xacmlpdpscertalias,
         String xacmlpepscertalias,
         String specertalias,
         String idpecertalias,
         String xacmlpdpecertalias,
         String xacmlpepecertalias,
         String spec
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "create-metadata-template");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (entityid != null) {
             HtmlTextInput txtentityid = (HtmlTextInput)form.getInputByName("entityid");
             txtentityid.setValueAttribute(entityid);
         }
 
         HtmlCheckBoxInput cbmetadata = (HtmlCheckBoxInput)form.getInputByName("metadata");
         cbmetadata.setChecked(metadata);
 
         HtmlCheckBoxInput cbextended = (HtmlCheckBoxInput)form.getInputByName("extended");
         cbextended.setChecked(extended);
 
         if (serviceprovider != null) {
             HtmlTextInput txtserviceprovider = (HtmlTextInput)form.getInputByName("serviceprovider");
             txtserviceprovider.setValueAttribute(serviceprovider);
         }
 
         if (identityprovider != null) {
             HtmlTextInput txtidentityprovider = (HtmlTextInput)form.getInputByName("identityprovider");
             txtidentityprovider.setValueAttribute(identityprovider);
         }
 
         if (xacmlpep != null) {
             HtmlTextInput txtxacmlpep = (HtmlTextInput)form.getInputByName("xacmlpep");
             txtxacmlpep.setValueAttribute(xacmlpep);
         }
 
         if (xacmlpdp != null) {
             HtmlTextInput txtxacmlpdp = (HtmlTextInput)form.getInputByName("xacmlpdp");
             txtxacmlpdp.setValueAttribute(xacmlpdp);
         }
 
         if (spscertalias != null) {
             HtmlTextInput txtspscertalias = (HtmlTextInput)form.getInputByName("spscertalias");
             txtspscertalias.setValueAttribute(spscertalias);
         }
 
         if (idpscertalias != null) {
             HtmlTextInput txtidpscertalias = (HtmlTextInput)form.getInputByName("idpscertalias");
             txtidpscertalias.setValueAttribute(idpscertalias);
         }
 
         if (xacmlpdpscertalias != null) {
             HtmlTextInput txtxacmlpdpscertalias = (HtmlTextInput)form.getInputByName("xacmlpdpscertalias");
             txtxacmlpdpscertalias.setValueAttribute(xacmlpdpscertalias);
         }
 
         if (xacmlpepscertalias != null) {
             HtmlTextInput txtxacmlpepscertalias = (HtmlTextInput)form.getInputByName("xacmlpepscertalias");
             txtxacmlpepscertalias.setValueAttribute(xacmlpepscertalias);
         }
 
         if (specertalias != null) {
             HtmlTextInput txtspecertalias = (HtmlTextInput)form.getInputByName("specertalias");
             txtspecertalias.setValueAttribute(specertalias);
         }
 
         if (idpecertalias != null) {
             HtmlTextInput txtidpecertalias = (HtmlTextInput)form.getInputByName("idpecertalias");
             txtidpecertalias.setValueAttribute(idpecertalias);
         }
 
         if (xacmlpdpecertalias != null) {
             HtmlTextInput txtxacmlpdpecertalias = (HtmlTextInput)form.getInputByName("xacmlpdpecertalias");
             txtxacmlpdpecertalias.setValueAttribute(xacmlpdpecertalias);
         }
 
         if (xacmlpepecertalias != null) {
             HtmlTextInput txtxacmlpepecertalias = (HtmlTextInput)form.getInputByName("xacmlpepecertalias");
             txtxacmlpepecertalias.setValueAttribute(xacmlpepecertalias);
         }
 
         if (spec != null) {
             HtmlTextInput txtspec = (HtmlTextInput)form.getInputByName("spec");
             txtspec.setValueAttribute(spec);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Import entity.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Realm where entity resides.
      * @param metadata Specify file name for the standard metadata to be imported.
      * @param extended Specify file name for the extended entity configuration to be imported.
      * @param cot Specify name of the Circle of Trust this entity belongs.
      * @param spec Specify metadata specification, either idff or saml2, defaults to saml2
      */
     public HtmlPage importEntity(
         WebClient webClient,
         String realm,
         String metadata,
         String extended,
         String cot,
         String spec
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "import-entity");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (metadata != null) {
             HtmlTextArea tametadata = (HtmlTextArea)form.getTextAreasByName("metadata").get(0);
             tametadata.setText(metadata);
         }
 
         if (extended != null) {
             HtmlTextArea taextended = (HtmlTextArea)form.getTextAreasByName("extended").get(0);
             taextended.setText(extended);
         }
 
         if (cot != null) {
             HtmlTextInput txtcot = (HtmlTextInput)form.getInputByName("cot");
             txtcot.setValueAttribute(cot);
         }
 
         if (spec != null) {
             HtmlTextInput txtspec = (HtmlTextInput)form.getInputByName("spec");
             txtspec.setValueAttribute(spec);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Export entity.
      *
      * @param webClient HTML Unit Web Client object.
      * @param entityid Entity ID
      * @param realm Realm where data resides
      * @param sign Set this flag to sign the metadata
      * @param metadata Metadata
      * @param extended Extended data
      * @param spec Specify metadata specification, either idff or saml2, defaults to saml2
      */
     public HtmlPage exportEntity(
         WebClient webClient,
         String entityid,
         String realm,
         boolean sign,
         boolean metadata,
         boolean extended,
         String spec
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "export-entity");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (entityid != null) {
             HtmlTextInput txtentityid = (HtmlTextInput)form.getInputByName("entityid");
             txtentityid.setValueAttribute(entityid);
         }
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         HtmlCheckBoxInput cbsign = (HtmlCheckBoxInput)form.getInputByName("sign");
         cbsign.setChecked(sign);
 
         HtmlCheckBoxInput cbmetadata = (HtmlCheckBoxInput)form.getInputByName("metadata");
         cbmetadata.setChecked(metadata);
 
         HtmlCheckBoxInput cbextended = (HtmlCheckBoxInput)form.getInputByName("extended");
         cbextended.setChecked(extended);
 
         if (spec != null) {
             HtmlTextInput txtspec = (HtmlTextInput)form.getInputByName("spec");
             txtspec.setValueAttribute(spec);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Delete entity.
      *
      * @param webClient HTML Unit Web Client object.
      * @param entityid Entity ID
      * @param realm Realm where data resides
      * @param extendedonly Set to flag to delete only extended data.
      * @param spec Specify metadata specification, either idff or saml2, defaults to saml2
      */
     public HtmlPage deleteEntity(
         WebClient webClient,
         String entityid,
         String realm,
         boolean extendedonly,
         String spec
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "delete-entity");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (entityid != null) {
             HtmlTextInput txtentityid = (HtmlTextInput)form.getInputByName("entityid");
             txtentityid.setValueAttribute(entityid);
         }
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         HtmlCheckBoxInput cbextendedonly = (HtmlCheckBoxInput)form.getInputByName("extendedonly");
         cbextendedonly.setChecked(extendedonly);
 
         if (spec != null) {
             HtmlTextInput txtspec = (HtmlTextInput)form.getInputByName("spec");
             txtspec.setValueAttribute(spec);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * List entities under a realm.
      *
      * @param webClient HTML Unit Web Client object.
      * @param realm Realm where entities reside.
      * @param spec Specify metadata specification, either idff or saml2, defaults to saml2
      */
     public HtmlPage listEntities(
         WebClient webClient,
         String realm,
         String spec
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "list-entities");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (spec != null) {
             HtmlTextInput txtspec = (HtmlTextInput)form.getInputByName("spec");
             txtspec.setValueAttribute(spec);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Create circle of trust.
      *
      * @param webClient HTML Unit Web Client object.
      * @param cot Circle of Trust
      * @param realm Realm where circle of trust resides
      * @param trustedproviders Trusted Providers
      * @param prefix Prefix URL for idp discovery reader and writer URL.
      */
     public HtmlPage createCircleOfTrust(
         WebClient webClient,
         String cot,
         String realm,
         List trustedproviders,
         String prefix
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "create-circle-of-trust");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (cot != null) {
             HtmlTextInput txtcot = (HtmlTextInput)form.getInputByName("cot");
             txtcot.setValueAttribute(cot);
         }
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (trustedproviders != null) {
             HtmlSelect sltrustedproviders= (HtmlSelect)form.getSelectByName("trustedproviders");
             String[] fakeOptions = new String[trustedproviders.size()];
             int cnt = 0;
             for (Iterator i = trustedproviders.iterator(); i.hasNext(); ) {
                 fakeOptions[cnt++] = (String)i.next();
             }
             sltrustedproviders.fakeSelectedAttribute(fakeOptions);
         }
 
         if (prefix != null) {
             HtmlTextInput txtprefix = (HtmlTextInput)form.getInputByName("prefix");
             txtprefix.setValueAttribute(prefix);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Delete circle of trust.
      *
      * @param webClient HTML Unit Web Client object.
      * @param cot Circle of Trust
      * @param realm Realm where circle of trust resides
      */
     public HtmlPage deleteCircleOfTrust(
         WebClient webClient,
         String cot,
         String realm
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "delete-circle-of-trust");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (cot != null) {
             HtmlTextInput txtcot = (HtmlTextInput)form.getInputByName("cot");
             txtcot.setValueAttribute(cot);
         }
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
     * List circle of trusts.
      *
      * @param webClient HTML Unit Web Client object.
     * @param realm Realm where circle of trusts reside
      */
     public HtmlPage listCircleOfTrusts(
         WebClient webClient,
         String realm
     ) throws Exception {
        URL cmdUrl = new URL(amadmUrl + "list-circle-of-trusts");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * List the members in a circle of trust.
      *
      * @param webClient HTML Unit Web Client object.
      * @param cot Circle of Trust
      * @param realm Realm where circle of trust resides
      * @param spec Specify metadata specification, either idff or saml2, defaults to saml2
      */
     public HtmlPage listCircleOfTrustMembers(
         WebClient webClient,
         String cot,
         String realm,
         String spec
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "list-circle-of-trust-members");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (cot != null) {
             HtmlTextInput txtcot = (HtmlTextInput)form.getInputByName("cot");
             txtcot.setValueAttribute(cot);
         }
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (spec != null) {
             HtmlTextInput txtspec = (HtmlTextInput)form.getInputByName("spec");
             txtspec.setValueAttribute(spec);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Remove a member from a circle of trust.
      *
      * @param webClient HTML Unit Web Client object.
      * @param cot Circle of Trust
      * @param entityid Entity ID
      * @param realm Realm where circle of trust resides
      * @param spec Specify metadata specification, either idff or saml2, defaults to saml2
      */
     public HtmlPage removeCircleOfTrustMember(
         WebClient webClient,
         String cot,
         String entityid,
         String realm,
         String spec
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "remove-circle-of-trust-member");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (cot != null) {
             HtmlTextInput txtcot = (HtmlTextInput)form.getInputByName("cot");
             txtcot.setValueAttribute(cot);
         }
 
         if (entityid != null) {
             HtmlTextInput txtentityid = (HtmlTextInput)form.getInputByName("entityid");
             txtentityid.setValueAttribute(entityid);
         }
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (spec != null) {
             HtmlTextInput txtspec = (HtmlTextInput)form.getInputByName("spec");
             txtspec.setValueAttribute(spec);
         }
 
 
         return (HtmlPage)form.submit();
     }
 
     /**
      * Add a member to a circle of trust.
      *
      * @param webClient HTML Unit Web Client object.
      * @param cot Circle of Trust
      * @param entityid Entity ID
      * @param realm Realm where circle of trust resides
      * @param spec Specify metadata specification, either idff or saml2, defaults to saml2
      */
     public HtmlPage addCircleOfTrustMember(
         WebClient webClient,
         String cot,
         String entityid,
         String realm,
         String spec
     ) throws Exception {
         URL cmdUrl = new URL(amadmUrl + "add-circle-of-trust-member");
         HtmlPage page = (HtmlPage)webClient.getPage(cmdUrl);
         HtmlForm form = (HtmlForm)page.getForms().get(0);
 
         if (cot != null) {
             HtmlTextInput txtcot = (HtmlTextInput)form.getInputByName("cot");
             txtcot.setValueAttribute(cot);
         }
 
         if (entityid != null) {
             HtmlTextInput txtentityid = (HtmlTextInput)form.getInputByName("entityid");
             txtentityid.setValueAttribute(entityid);
         }
 
         if (realm != null) {
             HtmlTextInput txtrealm = (HtmlTextInput)form.getInputByName("realm");
             txtrealm.setValueAttribute(realm);
         }
 
         if (spec != null) {
             HtmlTextInput txtspec = (HtmlTextInput)form.getInputByName("spec");
             txtspec.setValueAttribute(spec);
         }
 
 
         return (HtmlPage)form.submit();
     }
 }
