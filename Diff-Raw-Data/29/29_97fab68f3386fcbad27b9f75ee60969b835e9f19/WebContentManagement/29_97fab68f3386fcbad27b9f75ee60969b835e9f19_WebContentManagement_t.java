 package ro.finsiel.eunis;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.Timestamp;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.lang.StringUtils;
 
 import ro.finsiel.eunis.jrfTables.Chm62edtLanguageDomain;
 import ro.finsiel.eunis.jrfTables.Chm62edtLanguagePersist;
 import ro.finsiel.eunis.jrfTables.EunisISOLanguagesDomain;
 import ro.finsiel.eunis.jrfTables.EunisISOLanguagesPersist;
 import ro.finsiel.eunis.jrfTables.WebContentDomain;
 import ro.finsiel.eunis.jrfTables.WebContentPersist;
 import ro.finsiel.eunis.search.Utilities;
 import ro.finsiel.eunis.utilities.EunisUtil;
 import eionet.eunis.util.Pair;
 
 /**
  * Mange the content from the WEB_CONTENT table. Used for HTML editing of the web pages.
  * This object is constructed to store the content of page in a cache. If HTML is edited directly on database,
  * the looks on page might not be resembled in current page without starting a new browser session. This is because
  * page content is loaded on cache only when new session is created.
  *
  * @author finsiel
  */
 public class WebContentManagement implements java.io.Serializable {
   private static final String STR_EMPTY = "";
   private static final String STR_BR = "<br />";
   /**
    * This page keeps all the content of the web pages from database when object initialized in order to avoid
    * query overhead. It is declared as static so that all sessions share the same HTML content to avoid memory overheads
    * in Java VM.
    * The content of the hashmap is:
    * <UL>
    * <LI>Key - ID of the page from database</LI>
    * <LI>Value - WebContentPersist associated in database with that ID_PAGE</LI>
    * </UL>
    * So this is a (String, WebContentPersist) pair.
    */
   private HashMap<String, WebContentPersist> htmlContent = new HashMap<String, WebContentPersist>();
 
   private String language = "en";
 
   private boolean editMode = false;
   private boolean advancedEditMode = false;
 
   /**
    * Change current language displayed within web pages
    *
    * @param language
    */
   public void setLanguage( final String language ) {
     this.language = language;
     reloadLanguageData();
   }
 
   public void reloadLanguageData() {
     cacheHTMLContent( language );
   }
 
   public String br() {
     if ( !editMode && !advancedEditMode )
     {
       return STR_EMPTY;
     }
     else
     {
       return STR_BR;
     }
   }
 
   /**
    * Get the text for a token in the content table
    * Tokens can't contain apostrophs
    *
    * @param idPage
    */
   public String cms( String idPage ) {
     return getText( idPage );
   }
 
   /**
    * Get the text for a token in the content table
    * Tokens can't contain apostrophs
    * Not to be used inside HTML attribute values
    * If we're in edit mode, then show an edit-icon.
    *
    * @param idPage
    */
   public String cmsText( String idPage ) {
 	String ret = Utilities.replace(idPage, "'", "''");
 	ret = getTextByMD5( ret );
 	if(ret == null)
 		ret = getText( ret );
     
     if ( editMode )
     {
       ret += "<a title=\"Edit this text\" href=\"javascript:openContentManager('" + idPage + "', 'text');\"><img src=\"images/edit-content.gif\" style=\"border : 0px; padding-left : 2px;\" width=\"9\" height=\"9\" /></a>";
     }
     return ret;
   }
   
   /**
    * Look up a longer phrase (potentially HTML) in the content table
    * It is probably not necessary to escape apostrophs
    *
    * @param idPage
    */
   public String cmsPhrase( String idPage ) {
 	String ret = Utilities.replace(idPage, "'", "\'");
 	return getTextByMD5( ret );
   }
   
   public String cmsPhrase( String idPage, Object... arguments ) {
 	String ret = Utilities.replace(idPage, "'", "\'");  
 	ret = getTextByMD5( ret );
 	ret = MessageFormat.format(ret, arguments);
 	return ret;
   }
 
   public String cmsMsg( String idPage ) {
     if ( !editMode )
     {
       return STR_EMPTY;
     }
     else
     {
       String ret = "<strong><em>" + getDescription( idPage ) + "</em></strong>: ";
       ret += "<em>";
       ret += getText( idPage );
       ret += "</em>";
       ret += "<a title=\"Edit Text from this page (normally not visible online - javascript, error messages, page title etc.)\" href=\"javascript:openContentManager('" + idPage + "', 'msg');\"><img src=\"images/edit-content-msg.gif\" style=\"border : 0px;\" width=\"9\" height=\"9\" /></a>";
       return ret;
     }
   }
 
   /**
    * Edit mode - use this method where the crayon will appear
    *
    * @param idPage
    */
   public String cmsAlt( String idPage ) {
     if ( !advancedEditMode )
     {
       return STR_EMPTY;
     }
     else
     {
       return "<a title=\"Edit Alternative text\" href=\"javascript:openContentManager('" + idPage + "', 'alt_title');\"><img src=\"images/edit-content-alt.gif\" style=\"border : 0px; padding-left : 2px;\" width=\"9\" height=\"9\" /></a>";
     }
   }
 
   /**
    * Edit mode - use this method where the crayon will appear
    *
    * @param idPage
    */
   public String cmsTitle( String idPage ) {
     if ( !advancedEditMode )
     {
       return STR_EMPTY;
     }
     else
     {
       return "<a title=\"Edit Alternative text\" href=\"javascript:openContentManager('" + idPage + "', 'alt_title');\"><img src=\"images/edit-content-title.gif\" style=\"border : 0px; padding-left : 2px;\" width=\"9\" height=\"9\" /></a>";
     }
   }
 
   /**
    * Edit mode - use this method where the crayon will appear
    *
    * @param idPage
    */
   public String cmsInput( String idPage ) {
     if ( !advancedEditMode )
     {
       return STR_EMPTY;
     }
     else
     {
       return "<a title=\"Edit the Value attribute\" href=\"javascript:openContentManager('" + idPage + "', 'alt');\"><img src=\"images/edit-content-msg.gif\" style=\"border : 0px; padding-left : 2px;\" width=\"9\" height=\"9\" /></a>";
     }
   }
 
   /**
    * Edit mode - use this method where the crayon will appear
    *
    * @param idPage
    */
   public String cmsLabel( String idPage ) {
     if ( !advancedEditMode )
     {
       return STR_EMPTY;
     }
     else
     {
       return "<a title=\"Edit the Label attribute\" href=\"javascript:openContentManager('" + idPage + "', 'label');\"><img src=\"images/edit-content-msg.gif\" style=\"border : 0px; padding-left : 2px;\" width=\"9\" height=\"9\" /></a>";
     }
   }
 
   /**
    * Load the HTML content when object initialized from EUNIS_WEB_CONTENT into a
    * HashMap object and caches for later use while generating pages.
    */
   private void cacheHTMLContent( final String language ) {
     long l = new Date().getTime();
     try
     {
       htmlContent.clear();
       final List<WebContentPersist> pages = new WebContentDomain().findCustom( "select a.* from `eunis_web_content` as a, (select max(record_date) mx,id_page,lang from `eunis_web_content` group by id_page,lang) as b where a.id_page = b.id_page and a.lang = b.lang and a.record_date = b.mx and a.lang='" + language + "' and concat(a.record_date)<>'0000-00-00 00:00:00'" );
       for ( int i = 0; i < pages.size(); i++ )
       {
         final WebContentPersist text = pages.get( i );
         final String id_page = text.getIDPage().trim();
         htmlContent.put( id_page, text );
       }
     }
     catch ( Exception ex )
     {
       ex.printStackTrace();
     }
   }
 
   public String getText( String idPage )
   {
     if ( idPage == null )
     {
       return "";
     }
     String ret = idPage;
     idPage = idPage.trim();
     String md5 = EunisUtil.digestHexDec(idPage, "MD5");	
     if ( htmlContent.containsKey( idPage ) || htmlContent.containsKey( md5 ) )
     {
       WebContentPersist text = htmlContent.get( md5 );
       if(text == null)
     	  text = htmlContent.get( idPage );
       
       if ( null != text )
       {
         if ( text.getContent() != null )
         {
           ret = text.getContent().trim();
         }
         else
         {
           ret = idPage;
         }
       }
       else
       {
         System.out.println( "Warning:" + WebContentManagement.class.getName() + "::getText(" + idPage + "): Page not found in cache." );
       }
     }
     else
     {
       List<WebContentPersist> dbKeyList = new WebContentDomain().findWhereOrderBy( "ID_PAGE='" + md5 + "' AND CONCAT(RECORD_DATE)<> '0000-00-00 00:00:00' ", "RECORD_DATE DESC" );
       if(dbKeyList == null)
     	  dbKeyList = new WebContentDomain().findWhereOrderBy( "ID_PAGE='" + idPage + "' AND CONCAT(RECORD_DATE)<> '0000-00-00 00:00:00' ", "RECORD_DATE DESC" );
       
       if ( !dbKeyList.isEmpty() )
       {
         htmlContent.put( idPage, dbKeyList.get( 0 ) );
         ret = dbKeyList.get( 0 ).getContent();
       }
       else
       {
         System.out.println( "Warning:" + WebContentManagement.class.getName() + "::getText(" + idPage + "): Page not found in cache and database." );
       }
     }
     return ret;
   }
   
 	public String getTextByMD5( String idPage ){
 		if ( idPage == null ){
 			return "";
 		}
 		String md5 = EunisUtil.digestHexDec(idPage, "MD5");	
     
 		String ret = idPage;
 		idPage = idPage.trim();
 		if ( htmlContent.containsKey( md5 ) ){
 			final WebContentPersist text = htmlContent.get( md5 );
 			if ( null != text ){
 				if ( text.getContent() != null ){
 					ret = text.getContent().trim();
 				} else {
 					ret = idPage;
 				}
 			} else {
 				System.out.println( "Warning:" + WebContentManagement.class.getName() + "::getTextByMD5(" + idPage + "): Page not found in cache." );
 			}
 		} else	{
 			List<WebContentPersist> dbKeyList = null;
 			dbKeyList = new WebContentDomain().findWhereOrderBy( "ID_PAGE='" + md5 + "' AND CONCAT(RECORD_DATE)<> '0000-00-00 00:00:00' ", "RECORD_DATE DESC" );
 			if ( !dbKeyList.isEmpty() ){
 				htmlContent.put( idPage, dbKeyList.get( 0 ) );
 				ret = dbKeyList.get( 0 ).getContent();
 			}
 		}
 		return ret;
 	}  
 
   public String getDescription( String idPage )
   {
     if ( idPage == null )
     {
       return "";
     }
     String ret = idPage;
     idPage = idPage.trim();
     if ( htmlContent.containsKey( idPage ) )
     {
       final WebContentPersist text = htmlContent.get( idPage );
       if ( null != text )
       {
         if ( text.getDescription() != null )
         {
           ret = text.getDescription().trim();
         }
         else
         {
           ret = idPage;
         }
       }
       else
       {
         System.out.println( "Warning:" + WebContentManagement.class.getName() + "::getDescription(" + idPage + "): Page not found in cache." );
       }
     }
     else
     {
       final List<WebContentPersist> dbKeyList = new WebContentDomain().findWhereOrderBy( "ID_PAGE='" + idPage + "' AND CONCAT(RECORD_DATE)<> '0000-00-00 00:00:00' ", "RECORD_DATE DESC" );
       if ( !dbKeyList.isEmpty() )
       {
         htmlContent.put( idPage, dbKeyList.get( 0 ) );
         ret = dbKeyList.get( 0 ).getDescription();
       }
       else
       {
         System.out.println( "Warning:" + WebContentManagement.class.getName() + "::getDescription(" + idPage + "): Page not found in cache and database." );
       }
     }
     return ret;
   }
 
   public boolean idPageExists( String idPage, String language ) {
     boolean ret = false;
     final List<WebContentPersist> dbKeyList = new WebContentDomain().findWhere( "ID_PAGE='" + idPage + "' AND LANG='" + language + "' AND CONCAT(RECORD_DATE)<> '0000-00-00 00:00:00' " );
     if ( dbKeyList.size() > 0 )
     {
       ret = true;
     }
     return ret;
   }
 
   public WebContentPersist getPersistentObject( String idPage ) {
     WebContentPersist ret = null;
     if ( idPage == null )
     {
       return null;
     }
     idPage = idPage.trim();
     if ( htmlContent.containsKey( idPage ) )
     {
       ret = htmlContent.get( idPage );
     }
     else
     {
       final List<WebContentPersist> dbKeyList = new WebContentDomain().findWhereOrderBy( "ID_PAGE='" + idPage + "' AND CONCAT(RECORD_DATE)<> '0000-00-00 00:00:00' ", "RECORD_DATE DESC" );
       if ( !dbKeyList.isEmpty() )
       {
         htmlContent.put( idPage, dbKeyList.get( 0 ) );
         ret = dbKeyList.get( 0 );
       }
       else
       {
         System.out.println( "Warning:" + WebContentManagement.class.getName() + "::getPersistentObject(" + idPage + "): Page not found in cache and database." );
       }
     }
     return ret;
   }
 
   /**
    * Retrieve always last version of english version of page content directly from database.
    * $MODULE$_$PAGE$_$PARAGRAPH$, for example: species_names-result_0 means first paragraph from
    * Species::Names::Results (species-names-result.jsp) page.<BR>
    *
    * @param idPage ID of the page to be retrieved (ID_PAGE from WEB_CONTENT table)
    * @return HTML content of the web page or if ID not found or exception ocurred the empty "" string.
    */
   public WebContentPersist getPageContentEnglish( final String idPage ) {
     WebContentPersist ret = new WebContentPersist();
     try
     {
       final List<WebContentPersist> results = new WebContentDomain().findWhere( "ID_PAGE='" + idPage + "' AND LANG='en' AND CONCAT(RECORD_DATE)<> '0000-00-00 00:00:00' ORDER BY RECORD_DATE DESC" );
       if ( results.size() > 0 )
       {
         ret = results.get( 0 );
       }
     }
     catch ( Exception ex )
     {
       ex.printStackTrace();
     }
     return ret;
   }
 
   /**
    * Get all the versions for an ID_PAGE. Always, first element is the most recent version.
    *
    * @param idPage
    */
   public List getIDPageVersions( String idPage, String language ) {
     List ret = new ArrayList();
     try
     {
     	String md5 = EunisUtil.digestHexDec(idPage, "MD5");	
     	ret = new WebContentDomain().findWhereOrderBy( "ID_PAGE='" + md5 + "' AND LANG='" + language + "' AND CONCAT(RECORD_DATE)<> '0000-00-00 00:00:00' ", "RECORD_DATE DESC" );
     	if(ret == null)
     		ret = new WebContentDomain().findWhereOrderBy( "ID_PAGE='" + idPage + "' AND LANG='" + language + "' AND CONCAT(RECORD_DATE)<> '0000-00-00 00:00:00' ", "RECORD_DATE DESC" );
     }
     catch ( Exception ex )
     {
       ex.printStackTrace();
     }
     return ret;
   }
 
 
   /**
    * @param idPage
    * @param showEditTag
    * @deprecated use cmsXXXX methods instead.
    */
   public String writeEditTag( final String idPage, boolean showEditTag ) {
     if ( !showEditTag )
     {
       return "";
     }
     else
     {
       return "<a title=\"Edit this text\" href=\"javascript:editContent('" + idPage + "');\">" +
               "<img src=\"images/edit-content.gif\" style=\"border : 0px;\" width=\"9\" height=\"9\" " +
               "alt=\"Edit this text\" " +
               "title=\"Edit this text\" /></a>";
     }
   }
 
 //  public String writeEditTag( final String idPage ) {
 //    return writeEditTag( idPage, editMode );
 //  }
 
   public boolean savePageContentJDBC( String idPage,
                                       final String content,
                                       final String description,
                                       final String lang,
                                       final short contentLength,
                                       final String username,
                                       final boolean modifyAllIdentical,
                                       String SQL_DRV,
                                       String SQL_URL,
                                       String SQL_USR,
                                       String SQL_PWD ) {
     boolean result = false;
     Connection con = null;
     PreparedStatement ps = null;
     try
     {
       String md5 = EunisUtil.digestHexDec(idPage, "MD5");	
     	
       Class.forName( SQL_DRV );
       con = DriverManager.getConnection( SQL_URL, SQL_USR, SQL_PWD );
       
      if (modifyAllIdentical) {
    	  ps = con.prepareStatement("DELETE FROM EUNIS_WEB_CONTENT WHERE ID_PAGE = ? AND LANG = ? ");
    	  ps.setString(1, md5);
    	  ps.setString(2, lang);
    	  ps.execute();
      }
      
       if(lang.equalsIgnoreCase("EN")) {
    		ps = con.prepareStatement( "INSERT INTO EUNIS_WEB_CONTENT( ID_PAGE, CONTENT, DESCRIPTION, LANG, CONTENT_LENGTH, RECORD_AUTHOR, RECORD_DATE, CONTENT_VALID ) VALUES ( ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, 0 )" );
         ps.setString( 1, md5 );
         ps.setString( 2, content );
         ps.setString( 3, description );
         ps.setString( 4, lang );
         ps.setShort( 5, contentLength );
         ps.setString( 6, username );
       } else {
    		ps = con.prepareStatement( "INSERT INTO EUNIS_WEB_CONTENT( ID_PAGE, CONTENT, DESCRIPTION, LANG, CONTENT_LENGTH, RECORD_AUTHOR, RECORD_DATE ) VALUES ( ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP )" );
         ps.setString( 1, md5 );
         ps.setString( 2, content );
         ps.setString( 3, description );
         ps.setString( 4, lang );
         ps.setShort( 5, contentLength );
         ps.setString( 6, username );
       }
       ps.executeUpdate();
 
       // Do not reload all language again, just modify the current key.
       // cacheHTMLContent( this.language );
       idPage = idPage.trim();
       if ( htmlContent.containsKey( md5 ) )
       {
         WebContentPersist data = htmlContent.get( md5 );
         data.setContent( content );
         data.setDescription( description );
         data.setContentLength( contentLength );
         htmlContent.remove( md5 );
         htmlContent.put( md5, data );
       }
       else
       {
         System.out.println( "savePageContentJDBC: Could not find in cache id_page= " + idPage );
       }
       result = true;
     }
     catch ( Exception e )
     {
       e.printStackTrace();
       result = false;
     }
     finally
     {
       try
       {
         if ( ps != null )
         {
           ps.close();
         }
         if ( con != null )
         {
           con.close();
         }
       }
       catch ( Exception ex )
       {
         ex.printStackTrace();
       }
     }
     return result;
   }
 
  
  
   public boolean insertContentJDBC( String idPage,
                                     final String content,
                                     final String description,
                                     final String lang,
                                     final String username,
                                     final boolean modifyAllIdentical,
                                     String SQL_DRV,
                                     String SQL_URL,
                                     String SQL_USR,
                                     String SQL_PWD ) {
     boolean result = false;
     Connection con = null;
     PreparedStatement ps = null;
     try
     {
       Class.forName( SQL_DRV );
       con = DriverManager.getConnection( SQL_URL, SQL_USR, SQL_PWD );
       
      if (modifyAllIdentical) {
    	  ps = con.prepareStatement("DELETE FROM EUNIS_WEB_CONTENT WHERE ID_PAGE = MD5(?) AND LANG = ? ");
    	  ps.setString(1, idPage);
    	  ps.setString(2, lang);
    	  ps.execute();
      }
      
   	  ps = con.prepareStatement("INSERT INTO EUNIS_WEB_CONTENT( ID_PAGE, CONTENT, DESCRIPTION, LANG, RECORD_AUTHOR, RECORD_DATE ) VALUES ( MD5(?), ?, ?, ?, ?, CURRENT_TIMESTAMP )" );
 
       ps.setString( 1, idPage );
       ps.setString( 2, content );
       ps.setString( 3, description );
       ps.setString( 4, lang );
       ps.setString( 5, username );
       ps.executeUpdate();
       result = true;
     }
     catch ( Exception e )
     {
       e.printStackTrace();
       result = false;
     }
     finally
     {
       try
       {
         if ( ps != null )
         {
           ps.close();
         }
         if ( con != null )
         {
           con.close();
         }
       }
       catch ( Exception ex )
       {
         ex.printStackTrace();
       }
     }
     return result;
   }
 
   /**
    * Translate language code into language name in english. For example 'da' translates to 'Danish';
    *
    * @param code Code which will be decoded.
    * @return Decoded language or null if code is not found within CHM62EDT_LANGUAGE
    */
   public String translateLanguageCode( final String code ) {
     String value = null;
     try
     {
       final List<Chm62edtLanguagePersist> items = new Chm62edtLanguageDomain().findWhere( "CODE='" + code + "'" );
       if ( items.size() > 0 )
       {
         final Chm62edtLanguagePersist languageName = items.get( 0 );
         value = languageName.getNameEn();
       }
     }
     catch ( Exception ex )
     {
       ex.printStackTrace();
     }
     return value;
   }
 
   /**
    * Retrieve the list of languages in which application is already translated.
    *
    * @return List of languages.
    */
   public List<EunisISOLanguagesPersist> getTranslatedLanguages() {
     final List<EunisISOLanguagesPersist> languages = new Vector<EunisISOLanguagesPersist>();
     try
     {
       final List<WebContentPersist> items = new WebContentDomain().findCustom( "SELECT * FROM EUNIS_WEB_CONTENT WHERE LANG_STATUS > 0 AND CONCAT(RECORD_DATE)<> '0000-00-00 00:00:00' GROUP BY LANG" );
       // Decode the languages from CHM62EDT_LANGUAGE table
       for ( WebContentPersist item : items )
       {
         final List<EunisISOLanguagesPersist> tmp = new EunisISOLanguagesDomain().findWhere( "CODE = '" + item.getLang() + "'" );
         if ( tmp.size() > 0 )
         {
           languages.add( tmp.get( 0 ) );
         }
       }
     }
     catch ( Exception ex )
     {
       ex.printStackTrace();
       System.out.println();
     }
     return languages;
   }
 
   /**
    * Find all available language into which EUNIS can be translated.
    * Formula is: SELECT CODE FROM CHM62EDT_LANGUAGE - ( SELECT DISTINCT(LANG) EUNIS_WEB_CONTENT ) )
    */
   public List<Chm62edtLanguagePersist> getAvailableLanguages() {
     List<Chm62edtLanguagePersist> languages = new Vector<Chm62edtLanguagePersist>();
     try
     {
       final List<Chm62edtLanguagePersist> all_languages = new Chm62edtLanguageDomain().findOrderBy( "NAME_EN" );
       final List<WebContentPersist> translated_languages = new WebContentDomain().findCustom( "SELECT * FROM EUNIS_WEB_CONTENT WHERE CONCAT(RECORD_DATE)<> '0000-00-00 00:00:00' GROUP BY LANG" );
       for ( Chm62edtLanguagePersist language : all_languages )
       {
         boolean exists = false;
         for ( WebContentPersist existing_lang : translated_languages )
         {
           if ( existing_lang.getLang().equalsIgnoreCase( language.getCode() ) )
           {
             exists = true;
             break;
           }
         }
         if ( !exists )
         {
           languages.add( language );
         }
       }
     }
     catch ( Exception ex )
     {
       ex.printStackTrace();
     }
     return languages;
   }
 
   /**
    * Add a new language for translation into the database.
    *
    * @param code Language code from CHM62EDT_LANGUAGE
    */
   public boolean addLanguage( final String code ) throws Exception {
     boolean ret = false;
     if ( code != null && !code.equalsIgnoreCase( "en" ) )
     {
       // Check if languages is not already in database
       List<WebContentPersist> languages = new WebContentDomain().findWhere( "LANG='" + code + "' AND CONCAT(RECORD_DATE)<> '0000-00-00 00:00:00' " );
       if ( languages.size() > 0 )
       {
         throw new Exception( "Language already added for translation." );
       }
       try
       {
         //System.out.println( "Saving new language" );
         final WebContentPersist row = new WebContentPersist();
         row.setIDPage( "language_support" );
         row.setContent( "" );
         row.setDescription( "null" );
         row.setLang( code );
         row.setLangStatus( new Short( ( short ) 0 ) );
         row.setContentLength( new Short( ( short ) 0 ) );
         row.setRecordDate( new Timestamp( new Date().getTime() ) );
         new WebContentDomain().save( row );
         ret = true;
       }
       catch ( Exception ex )
       {
         ret = false;
         ex.printStackTrace();
       }
     }
     return ret;
   }
 
   public String getLanguage()
   {
     return this.language;
   }
 
   public boolean isEditMode()
   {
     return editMode;
   }
 
   public void setEditMode( boolean editMode )
   {
     this.editMode = editMode;
   }
 
   public String getCurrentLanguage()
   {
     return language;
   }
 
   public boolean isAdvancedEditMode()
   {
     return advancedEditMode;
   }
 
   public void setAdvancedEditMode( boolean advancedEditMode )
   {
     this.advancedEditMode = advancedEditMode;
   }
  
 	/**
 	 * Reads the URL and returns the content.
 	 * Pair id - HTTP status code, Pair value - response body.
 	 * 
 	 * @param url url to check.
 	 * @return
 	 */
 	public Pair<Integer,String> readContentFromUrl(String url) {
 	  Pair<Integer, String> result = null;
 	  if (StringUtils.isNotBlank(url)) {
 		  try {
 			  HttpClient client = new HttpClient();
 			  HttpMethod get = new GetMethod(url);
 			  client.executeMethod(get);
 			  result = new Pair<Integer, String>();
 			  result.setId(get.getStatusCode());
 			  if (get.getStatusCode() != 404) {
 				  result.setValue(get.getResponseBodyAsString());
 			  }
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	  }
 	  return result;
   }
   
   public boolean importNewTexts(String description,
           						String lang,
           						String username,
           						String SQL_DRV, 
           						String SQL_URL, 
           						String SQL_USR, 
           						String SQL_PWD) {
 		Connection con = null;
 	    PreparedStatement ps = null;
 	    BufferedReader input = null;
 	    String DATA_FILE_NAME = "new_texts.txt";
 	    try
 	    {
 	    	Class.forName( SQL_DRV );
 	    	con = DriverManager.getConnection( SQL_URL, SQL_USR, SQL_PWD );
 	    	
 			InputStream is = this.getClass().getClassLoader().getResourceAsStream(DATA_FILE_NAME);
 			input = new BufferedReader(new InputStreamReader(is));
 			String line = null;
 			
 			List<String> new_ids = new ArrayList<String>(); 
 			
 	        while (( line = input.readLine()) != null){
 	        	StringTokenizer st = new StringTokenizer(line, "|");
 	        	if(st != null && st.countTokens() == 2){
 	        		String text = st.nextToken();
 	        		if(!new_ids.contains(text)){
 				   		ps = con.prepareStatement( "INSERT INTO EUNIS_WEB_CONTENT( ID_PAGE, CONTENT, DESCRIPTION, LANG, RECORD_AUTHOR, RECORD_DATE ) VALUES ( MD5(?), ?, ?, ?, ?, CURRENT_TIMESTAMP )" );
 				        ps.setString( 1, text );
 				        ps.setString( 2, text );
 				        ps.setString( 3, description );
 				        ps.setString( 4, lang );
 				        ps.setString( 5, username );
 			
 				        ps.executeUpdate();
 				        new_ids.add(text);
 	        		}
 		        }
 	        }
 	        return true;
 	    }
 	    catch ( Exception e )
 	    {
 	      e.printStackTrace();
 	      return false;
 	    }
 	    finally
 	    {
 	      try
 	      {
 	        if ( ps != null )
 	        {
 	          ps.close();
 	        }
 	        if ( con != null )
 	        {
 	          con.close();
 	        }
 	        if(input != null){
 	        	input.close();
 	        }
 	      }
 	      catch ( Exception ex )
 	      {
 	        ex.printStackTrace();
 	      }
 	    }
 
 	}
 }
 
