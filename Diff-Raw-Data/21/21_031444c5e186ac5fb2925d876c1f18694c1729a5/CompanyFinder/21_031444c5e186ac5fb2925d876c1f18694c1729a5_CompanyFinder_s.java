 package com.blogspot.aptgetmoo.dhjclient.company;
 
 import java.io.IOException;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.safety.Whitelist;
 import org.jsoup.select.Elements;
 
 import com.blogspot.aptgetmoo.dhjclient.parser.Webpage;
 
 /**
  * @author Dzul Nizam
  */
 public class CompanyFinder implements ICompanyFinder {
 
     private Webpage mPage;
 
     /**
      * @param 	pPage 	CompanyPage
      */
     public CompanyFinder(CompanyPage pPage) {
         mPage = pPage;
     }
 
     @Override
     public Company find(String pCompCode) throws IOException {
         ((CompanyPage) mPage).setCompanyCode(pCompCode);
 
         Document doc;
         doc = Jsoup.parse(mPage.getParseable());
         Company company = new Company(pCompCode);
 
         if (doc != null) {
             company = parseRows(company, doc.select("table table tr"));
         }
 
         return company;
     }
 
     private Company parseRows(Company pCom, Elements pRows) {
         if (pRows.size() < 9) return null;
 
         Elements temp;
         if ((temp = pRows.get(1).select("td")).size() > 0) pCom.name = cleanUp(temp.get(1).text());
         if (pCom.name.trim().length() == 0) {
             // That's it, no need further processing, the company doesn't exists
             return null;
         }
 
         if ((temp = pRows.get(2).select("td")).size() > 0) pCom.address = cleanUp(temp.get(1).html());
         if ((temp = pRows.get(4).select("td")).size() > 0) pCom.phone = temp.get(1).text();
         if ((temp = pRows.get(5).select("td")).size() > 0) pCom.fax = temp.get(1).text();
         if ((temp = pRows.get(6).select("td")).size() > 0) pCom.email = temp.get(1).text();
         if ((temp = pRows.get(7).select("td")).size() > 0) pCom.web = temp.get(1).text();
         if ((temp = pRows.get(8).select("td")).size() > 0) pCom.jakimRefNo = temp.get(1).text();
         if ((temp = pRows.get(9).select("td")).size() > 0) pCom.jakimOfficer = temp.get(1).text();
 
         return pCom;
     }
 
     private String cleanUp(String pStr) {
         final Whitelist whiteList = new Whitelist();
         pStr = StringEscapeUtils.unescapeHtml4(Jsoup.clean(pStr, whiteList.addTags("br")));
 
         return pStr.replace("<br />", "")
             .replace("<br>", "")
             // .replace("&nbsp;", " ")
             .replaceAll("(?m)(?:^|\\G) ", "")
            .replaceAll("r\r?\n", "\n");
     }
 
 }
