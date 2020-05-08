/**
 *
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
  */
 package org.apache.tuscany.test.das;
 
 import java.net.URL;
 
 import junit.framework.TestCase;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
 
 public class DasTestCase extends TestCase {
     public static final String testUrl = "http://localhost:8080/sample-companyweb/";
 
     protected WebClient _webClient;
     protected URL _url;
 
     protected void setUp() throws Exception {
         //New HTMLunit web client
         _webClient = new WebClient();
         _webClient.setRedirectEnabled(true);
 
         // Going to have the WebClient connect to this URL
         _url = new URL(testUrl);
 
     }
 
     public void testHomepage() throws Exception {
         final String TEST_CASE = "HomePage";
         System.out.print("Running:" + TEST_CASE + "\t\t\t");
 
         final HtmlPage page = (HtmlPage) _webClient.getPage(_url);
         //System.out.println(page.getTitleText());
 
         String responseText = page.asText();
         //System.out.println("html-->\n" + responseText);
         //check page header
         assertTrue(-1 != responseText.indexOf("Tuscany DAS Companies WEB Example"));
         //check table headers
         assertTrue(-1 != responseText.indexOf("ID"));
         assertTrue(-1 != responseText.indexOf("Name"));
         assertTrue(-1 != responseText.indexOf("Department_ID"));
         assertTrue(-1 != responseText.indexOf("Department_Name"));
         //check companies values
         assertTrue(-1 != responseText.indexOf("51"));
         assertTrue(-1 != responseText.indexOf("ACME Publishing"));
         assertTrue(-1 != responseText.indexOf("53"));
         assertTrue(-1 != responseText.indexOf("Do-rite plumbing"));
         assertTrue(-1 != responseText.indexOf("53"));
         assertTrue(-1 != responseText.indexOf("MegaCorp"));
         //check departments
         assertTrue(-1 != responseText.indexOf("73"));
         assertTrue(-1 != responseText.indexOf("74"));
         assertTrue(-1 != responseText.indexOf("75"));
         assertTrue(-1 != responseText.indexOf("76"));
         assertTrue(-1 != responseText.indexOf("77"));
         assertTrue(-1 != responseText.indexOf("78"));
         assertTrue(-1 != responseText.indexOf("12"));
 
         System.out.println("SUCCESS!!!");
     }
 
 
     public void testAllCompanies() throws Exception{
         final String TEST_CASE = "AllCompanies";
         System.out.print("Running:" + TEST_CASE + "\t\t\t");
 
         // Get the first page
         final HtmlPage page = (HtmlPage) _webClient.getPage(_url);
 
         // Get the form that we are dealing with and within that form,
         // find the submit button and the field that we want to change.
         final HtmlForm form = (HtmlForm) page.getForms().get(0);
         final HtmlSubmitInput allCompaniesButton = (HtmlSubmitInput)form.getHtmlElementById("doFill");
         // Now submit the form by clicking the button
         final HtmlPage resultPage = (HtmlPage)allCompaniesButton.click();
 
         //verify the resultPage
         String responseText = resultPage.asText();
         //System.out.println("html-->\n" + responseText);
         // check table headers
         assertTrue(-1 != responseText.indexOf("ID"));
         assertTrue(-1 != responseText.indexOf("Name"));
         //check table values
         assertTrue(-1 != responseText.indexOf("51"));
         assertTrue(-1 != responseText.indexOf("ACME Publishing"));
         assertTrue(-1 != responseText.indexOf("52"));
         assertTrue(-1 != responseText.indexOf("Do-rite plumbing"));
         assertTrue(-1 != responseText.indexOf("53"));
         assertTrue(-1 != responseText.indexOf("MegaCorp"));
 
         System.out.println("SUCCESS!!!");
     }
 
     public void testAllCompaniesDepartments() throws Exception{
         final String TEST_CASE = "AllCompaniesDepartments";
         System.out.print("Running:" + TEST_CASE + "\t\t");
 
         // Get the first page
         final HtmlPage page = (HtmlPage) _webClient.getPage(_url);
 
         // Get the form that we are dealing with and within that form,
         // find the submit button and the field that we want to change.
         final HtmlForm form = (HtmlForm) page.getForms().get(0);
         final HtmlSubmitInput allCompaniesButton = (HtmlSubmitInput)form.getHtmlElementById("doFillAll");
         // Now submit the form by clicking the button
         final HtmlPage resultPage = (HtmlPage)allCompaniesButton.click();
 
         //verify the resultPage
         String responseText = resultPage.asText();
         //System.out.println("html-->\n" + responseText);
         //check table headers
         assertTrue(-1 != responseText.indexOf("ID"));
         assertTrue(-1 != responseText.indexOf("Name"));
         assertTrue(-1 != responseText.indexOf("Department_ID"));
         assertTrue(-1 != responseText.indexOf("Department_Name"));
         //check companies values
         assertTrue(-1 != responseText.indexOf("51"));
         assertTrue(-1 != responseText.indexOf("ACME Publishing"));
         assertTrue(-1 != responseText.indexOf("53"));
         assertTrue(-1 != responseText.indexOf("Do-rite plumbing"));
         assertTrue(-1 != responseText.indexOf("53"));
         assertTrue(-1 != responseText.indexOf("MegaCorp"));
         //check departments
         assertTrue(-1 != responseText.indexOf("73"));
         assertTrue(-1 != responseText.indexOf("74"));
         assertTrue(-1 != responseText.indexOf("75"));
         assertTrue(-1 != responseText.indexOf("76"));
         assertTrue(-1 != responseText.indexOf("77"));
         assertTrue(-1 != responseText.indexOf("78"));
         assertTrue(-1 != responseText.indexOf("12"));
 
         System.out.println("SUCCESS!!!");
     }
 
     public void testAddDepartmentToFirstCompany() throws Exception{
         final String TEST_CASE = "AddDepartmentToFirstCompany";
         System.out.print("Running:" + TEST_CASE + "\t");
 
         // Get the first page
         final HtmlPage page = (HtmlPage) _webClient.getPage(_url);
 
         // Get the form that we are dealing with and within that form,
         // find the submit button and the field that we want to change.
         final HtmlForm form = (HtmlForm) page.getForms().get(0);
         final HtmlSubmitInput allCompaniesButton = (HtmlSubmitInput)form.getHtmlElementById("doAddDepartment");
         // Now submit the form by clicking the button
         final HtmlPage resultPage = (HtmlPage)allCompaniesButton.click();
 
         //verify the resultPage
         String responseText = resultPage.asText();
         //System.out.println("html-->\n" + responseText);
         //check new department
         assertTrue(-1 != responseText.indexOf("79"));
 
         System.out.println("SUCCESS!!!");
     }
 
 
     public void testChangeCompanyDepartmentNames() throws Exception{
         final String TEST_CASE = "ChangeCompanyDepartmentNames";
         System.out.print("Running:" + TEST_CASE + "\t");
 
         // Get the first page
         final HtmlPage page = (HtmlPage) _webClient.getPage(_url);
 
         // Get the form that we are dealing with and within that form,
         // find the submit button and the field that we want to change.
         final HtmlForm form = (HtmlForm) page.getForms().get(0);
         final HtmlSubmitInput allCompaniesButton = (HtmlSubmitInput)form.getHtmlElementById("doChangeDepartmentNames");
         // Now submit the form by clicking the button
         final HtmlPage resultPage = (HtmlPage)allCompaniesButton.click();
 
         //verify the resultPage
         String responseText = resultPage.asText();
         //System.out.println("html-->\n" + responseText);
         //check update departments
         assertTrue(-1 != responseText.indexOf("Dept-"));
         //also, check to not have old departments names (default name)
         assertTrue(-1 == responseText.indexOf("Default Name"));
 
         System.out.println("SUCCESS!!!");
     }
 
     public void testDeleteCompanyOneDepartments() throws Exception{
         final String TEST_CASE = "DeleteCompanyOneDepartments";
         System.out.print("Running:" + TEST_CASE + "\t");
 
         // Get the first page
         final HtmlPage page = (HtmlPage) _webClient.getPage(_url);
 
         // Get the form that we are dealing with and within that form,
         // find the submit button and the field that we want to change.
         final HtmlForm form = (HtmlForm) page.getForms().get(0);
         final HtmlSubmitInput allCompaniesButton = (HtmlSubmitInput)form.getHtmlElementById("doDeleteDepartments");
         // Now submit the form by clicking the button
         final HtmlPage resultPage = (HtmlPage)allCompaniesButton.click();
 
         //verify the resultPage
         String responseText = resultPage.asText();
         //System.out.println("html-->\n" + responseText);
         //check that al the company 1 departments are gone..
         assertTrue(-1 == responseText.indexOf("73"));
         assertTrue(-1 == responseText.indexOf("74"));
         assertTrue(-1 == responseText.indexOf("75"));
         assertTrue(-1 == responseText.indexOf("76"));
         assertTrue(-1 == responseText.indexOf("77"));
         assertTrue(-1 == responseText.indexOf("78"));
 
         System.out.println("SUCCESS!!!");
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         DasTestCase t = new DasTestCase();
         try{
             t.testHomepage();
             t.testAllCompanies();
             t.testAllCompaniesDepartments();
             t.testAddDepartmentToFirstCompany();
             t.testChangeCompanyDepartmentNames();
             t.testDeleteCompanyOneDepartments();
         }catch(Exception e){
             e.printStackTrace();
         }
 
     }
 
 }
