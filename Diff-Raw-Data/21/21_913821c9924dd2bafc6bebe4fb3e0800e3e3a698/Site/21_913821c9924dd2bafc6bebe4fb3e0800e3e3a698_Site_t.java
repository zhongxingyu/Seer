 package com.meterware.website;
 /********************************************************************************************************************
  * $Id$
  *
  * Copyright (c) 2003,2005, Russell Gold
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
  * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
  * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
  * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions
  * of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
  * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  *
  *******************************************************************************************************************/
 import com.meterware.xml.DocumentSemantics;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 
 /**
  *
  * @author <a href="mailto:russgold@meterware.com">Russell Gold</a>
  **/
 public class Site {
 
     private ArrayList _commonElements = new ArrayList();
     private ArrayList _siteElements = new ArrayList();
 
     private String _siteName;
     private String _logo;
     private CopyRight _copyRight;
 
 
     protected static void generate( Site site, File siteFile, SiteTemplate template, PageGenerator generator ) throws SAXException, IOException {
         PageFragment.setRoot( siteFile.getParentFile() );
         Document document = DocumentSemantics.parseDocument( siteFile );
         DocumentSemantics.build( document, site, siteFile.getAbsolutePath() );
 
         for (int i = 0; i < site._siteElements.size(); i++) {
             SiteElement siteElement = (SiteElement) site._siteElements.get( i );
             siteElement.generate( generator, template, site );
         }
     }
 
 
     public CommonElement createCommonElement() {
         CommonElement element = new CommonElement();
         _commonElements.add( element );
         return element;
     }
 
 
     public WebPage createPage() {
         return (WebPage) addedSiteElement( new WebPage() );
     }
 
 
     public WebCategory createCategory() {
         return (WebCategory) addedSiteElement( new WebCategory() );
     }
 
 
     protected SiteElement addedSiteElement( SiteElement page ) {
         _siteElements.add( page );
         return page;
     }
 
 
     public ExternalPage createExternal() {
         return (ExternalPage) addedSiteElement( new ExternalPage() );
     }
 
 
     public MenuSpace createSpace() {
         return (MenuSpace) addedSiteElement( new MenuSpace() );
     }
 
 
     public String getSiteName() {
         return _siteName;
     }
 
 
     public void setSiteName( String siteName ) {
         _siteName = siteName;
     }
 
 
     public String getLogo() {
         return _logo;
     }
 
 
     public void setLogo( String logo ) {
         _logo = logo;
     }
 
 
     public CopyRight createCopyright() {
         _copyRight = new CopyRight();
         return _copyRight;
     }
 
 
     public CopyRight getCopyRight() {
         return _copyRight;
     }
 
 
     public void appendPageTitle( StringBuffer sb, SiteLocation currentPage ) {
        sb.append( "<html><head><meta http-equiv='Content-Type' content='text/html; charset=UTF-8'><title>" ).append( getSiteName() ).append( ' ' ).append( currentPage.getTitle() ).append( "</title>" ).append( FragmentTemplate.LINE_BREAK );
         sb.append( "<link rel='stylesheet' href='" ).append( SiteUtils.relativeURL( currentPage.getLocation(), "site.css"  ) ).append( "' type='text/css'>" ).append( FragmentTemplate.LINE_BREAK );
         sb.append( "</head>" ).append( FragmentTemplate.LINE_BREAK );
         sb.append( "<body>" ).append( FragmentTemplate.LINE_BREAK );
         sb.append( "<image src='" ).append( SiteUtils.relativeURL( currentPage.getLocation(), getLogo() ) ).append( "'/>" ).append( FragmentTemplate.LINE_BREAK );
         sb.append( "<h1>" ).append( currentPage.getTitle() ).append( "</h1>" ).append( FragmentTemplate.LINE_BREAK );
     }
 
 
     public class CopyRight {
         private String _owner;
         private String _dates;
 
         public String getNotice() {
             StringBuffer sb = new StringBuffer();
             sb.append( "Copyright &copy; ").append( _dates ).append( ", " ).append( _owner );
             return sb.toString();
         }
 
 
         public String getOwner() {
             return _owner;
         }
 
 
         public void setOwner( String owner ) {
             _owner = owner;
         }
 
 
         public String getDates() {
             return _dates;
         }
 
 
         public void setDates( String dates ) {
             _dates = dates;
         }
     }
 
 
     public class CommonElement {
         private ArrayList _fragments = new ArrayList();
         private String _div;
 
 
         public PageFragment createFragment() {
             PageFragment fragment = new PageFragment();
             _fragments.add( fragment );
             return fragment;
         }
 
 
         public void setDiv( String div ) {
             _div = div;
         }
 
 
         public void appendTo( StringBuffer sb ) {
             sb.append( "<div id='" ).append( _div ).append( "'>" ).append( FragmentTemplate.LINE_BREAK );
             for (int i = 0; i < _fragments.size(); i++) {
                 PageFragment pageFragment = (PageFragment) _fragments.get( i );
                 sb.append( pageFragment.asText() ).append( FragmentTemplate.LINE_BREAK );
             }
             sb.append( "</div>" ).append( FragmentTemplate.LINE_BREAK );
         }
     }
 
 
     public void appendCommonElements( StringBuffer sb ) {
         for (int i = 0; i < _commonElements.size(); i++) {
             CommonElement commonElement = (CommonElement) _commonElements.get( i );
             commonElement.appendTo( sb );
         }
     }
 
 
     public void appendMenu( StringBuffer sb, SiteTemplate siteTemple, String currentLocation ) {
         sb.append( "<div id='Menu'>" ).append( FragmentTemplate.LINE_BREAK );
         for (int i = 0; i < _siteElements.size(); i++) {
             SiteElement siteElement = (SiteElement) _siteElements.get( i );
             siteElement.appendMenuItem( sb, siteTemple, currentLocation );
         }
         sb.append( "</div>" ).append( FragmentTemplate.LINE_BREAK );
     }
 
 
 }
