 // Copyright (c) 2000 Oracle Corporation
 package gov.nih.nci.ncicb.cadsr.common.util;
 
 
 import gov.nih.nci.ncicb.cadsr.common.util.logging.Log;
 import gov.nih.nci.ncicb.cadsr.common.util.logging.LogFactory;
 
 import java.sql.SQLException;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.StringEscapeUtils;
 /**
  * A Bean class.
  * <P>
  * @author Oracle Corporation
  */
 public class CommonListBean extends Object {
   private static Log log = LogFactory.getLog(CommonListBean.class.getName());
 
   private HttpServletRequest myRequest;
 
   private String detailReq_Type = "";
   private String dlbMId = "";
   private int numOfColumns =0;
   private String[] searchParm;
   private String[] displayParm;
   private String[] sqlStmtParm;
   private String[] jspLinkParm;
   private String   mySqlStmt=""; // hold partial SQL statement
   private String   orderByClause;
   private String   sqlStmt="";   // for query
   private DBUtil dBBroker = null;
   private Vector rsVector = null;
   private Vector dataVector = null;
   private int rsVectorIdx = 0;
   private boolean compressFlag=true;
   private int linkCol = 0;
   private String[] mySearchStr;
   private String searchClause="";
   
   private UserInfoBean uib = null;
   private String extraURL=""; //for additional link parameters
   private int[] myLovCols = null;
   private List pageList = null;
 
   /**
    * Display information (link) will be formatted for an LOV
    */
   public static final int LOV_DISPLAY = 0;
   /**
    * Display information (link) will be formatted for an "in-page" LIST.
    */
   public static final int LIST_DISPLAY = 1;
 
   private int displayMode = LIST_DISPLAY;
 
   private boolean firstTime;
 
   /**
    * Number of rows Displayed can be setShowRowNum( int ) method,
    * the desault number is 10.
    */
   private int showRowNum =10;
 
   int startRec=0; // (0 = first record)
   int rNum=0;
   String   xSearchStr="";
 
   /**
    * Constructor
    *
    *  @param request
    *  @param response
    *  @param dBBroker
    *  @param mySearch
    *  @param myJspLinkParm
    *  @param myDispParm
    *  @param mySqlParm
    */
   public CommonListBean(
     HttpServletRequest request,
     //DBBroker dBBroker,
     DBUtil dBBroker,
     String[] mySearch,
     String[] myJspLinkParm,
     String[] myDispParm,
     String[] mySqlParm,
     boolean buildSearchClause,
     int[] lovPassbackCols
     )
 
     throws SQLException {
 
     myRequest = request;
     this.dBBroker = dBBroker;
     
     
     
     searchParm    =mySearch;
     jspLinkParm   =myJspLinkParm;
     displayParm   =myDispParm;
     sqlStmtParm   =mySqlParm;
     myLovCols = lovPassbackCols;
     int as=displayParm.length;
     if (searchParm != null) {
       int numSearchFields=this.searchParm.length /2;
     }
     int ss=0;
     //System.out.println("numSearchParm: "+ numSearchFields);
 
     mySearchStr=request.getParameterValues("SEARCH");    
     if (buildSearchClause) {
       if (mySearchStr!=null){
         ss=mySearchStr.length;
         for (int s=0; s<ss; s++) {
           String myStr=mySearchStr[s];
           //String newStr = StringReplace.strReplace(myStr,"*","%");
           searchClause+= " and upper (nvl(" + this.searchParm[2*s] + ",'%')) like upper ( '%" +
                         myStr + "%') " ;
             /*searchClause+= " and upper (nvl(" + this.searchParm[2*s] + ",'%')) like upper ( nvl('" +
                         newStr + "','%')) " ;*/
             //System.out.println("myStr: " + myStr + " searchClause: " + searchClause);
         }
       }
     }
     
     if (request.getParameter("NOT_FIRST_DISPLAY") != null) {
       this.firstTime = false;
       populate();
     }
     else {
       this.firstTime = true;
     }
 }
 
   /**
    * Forces the bean to execute the database query.  This is useful in cases where
    * the hit list will be displayed without any search fields or find button.
    * <p>
    * NOTE: It is not advisable to use this method when the NOT_FIRST_DISPLAY
    * HTTP request parameter is passed by the browser.
    * @return True if first time, false otherwise
    */
   public void forcePopulate() throws SQLException {
     this.populate();
     this.firstTime = false;
   }
 
   /**
    * Specifies if this is the first time that the list/lov was displayed.
    * @return True if first time, false otherwise
    */
   public boolean isFirstDisplay() {
     return this.firstTime;
   }
 
   /**
    * Change the display mode for this instance.
    * @param New display mode
    */
   public void setDisplayMode(int newMode) {
     this.displayMode = newMode;
   }
 
   /**
    * Change the number of display rows for this instance.
    * @param New number of display rows
    */
   public void setShowRowNum(int newNum) {
     this.showRowNum = newNum;
   }
 
   private String[] getSearchStrArray() {
     return mySearchStr;
   }
 
   private String[] getSearchParm(){
     return searchParm;
   }
 
   private String[] getDisplayParm(){
     return displayParm;
   }
 
   private String[] getJspLinkParm(){
     return   jspLinkParm;
   }
 
   private int getLinkCol(){
     return linkCol;
   }
 
   /*
   public void setDisplayParm(String[] myDisParm){
     displayParm=myDisParm;
   }
 
   public void setJspLinkParm(String[] myJspLinkParm){
          jspLinkParm=myJspLinkParm;
   }
   */
 
   public  void setLinkCol(int linkcol){
       linkCol=linkcol;
   }
 
   private boolean getCompressFlag(){
     return compressFlag;
   }
 
   public void setCompressFlag(boolean flag){
          compressFlag=flag;
   }
 
   private String[] getSqlStmtParm(){
     return sqlStmtParm;
   }
   public void setSqlStmtParm(String[] mySqlStmtParm){
          sqlStmtParm=mySqlStmtParm;
   }
 
   private String getDetailReq_Type(){
     return detailReq_Type;
   }
 
   public void setDetailReq_Type(String reqtype){
       detailReq_Type=reqtype;
   }
 
   private String getOrderByClause(){
     orderByClause="" + this.getSqlStmtParm()[1];
     return orderByClause;
   }
 
   private String getMySqlStmt(){
      String[] sA=this.getDisplayParm();
      String[] sA1=this.getJspLinkParm();
      int sALength=sA.length;
      int sA1Length=sA1.length;
 
      String tableColName="";
 
      for (int i=0; i<sALength; i+=2) {
        tableColName+=(tableColName.equals(""))?"" + sA[i]:", " + sA[i];
      }
      for (int i=0; i<sA1Length; i+=2) {
        tableColName+=(tableColName.equals(""))?"" + sA1[i]:", " + sA1[i];
      }
 
     mySqlStmt="select " + tableColName + this.getSqlStmtParm()[0];
   return mySqlStmt;
   }
 
   private void populate() throws SQLException {
     sqlStmt = this.getMySqlStmt() +
               searchClause +
               this.getOrderByClause();
     log.debug("sqlstmt: "+sqlStmt);
     rsVector = dBBroker.retrieveMultipleRecordsDB(sqlStmt);
     log.debug("rsVector: "+rsVector.size());
     pageList = new ArrayList(9);
     pageList.add(new Integer(40));
     pageList.add(new Integer(80));
     pageList.add(new Integer(115));
     pageList.add(new Integer(145));
     pageList.add(new Integer(170));
     pageList.add(new Integer(200));
     pageList.add(new Integer(230));
     
   }
 
   private boolean hasMoreDetailRows() {
     if (rsVectorIdx < rsVector.size()) {
       dataVector = (Vector)rsVector.elementAt(rsVectorIdx);
       rsVectorIdx++;
       return true;
     }
     else {
       return false;
     }
   }
 
   private String getColValue(int i) {
     return (String)dataVector.elementAt(i);
   }
 
   private int getRowCount() {
     /*int count = 0;
     try {
       String str="select count(*)" + this.getSqlStmtParm()[0];
       Vector vec = dBBroker.retrieveRecordDB(str);
       count = Integer.parseInt((String)vec.elementAt(0));
     } 
     catch (Exception ex) {
       ex.printStackTrace();
     } 
     return count;*/   
     return rsVector.size();
     
   }
 
   //--------> The masterName is the first column in the select stmt
   //--------> masterId is the second to the last column, detailId is the last column!!!
   //
   // DOES NOT APPEAR TO BE USED
   //
   //public String getMasterName() {
   //    return (String)dataVector.elementAt(0);
   //  }
 
   /**
    * Displays the page information that normally appears below the hit list.  Includes
    * current page, next page, and go page information.
    * @return String containing page info HTML.
    */
   public String getPageInfo() {
     StringBuffer result = new StringBuffer();
     String extraURLInfo = this.getExtraURLInfo();
     if (rNum>this.showRowNum){
       result.append("<p class=\"OraFieldText\">Current Page: <b>" + (startRec/this.showRowNum +1) + "&nbsp;&nbsp;&nbsp;&nbsp;");
       if (rNum-startRec>this.showRowNum){
         if(this.mySearchStr!=null) {
          result.append("&nbsp;<A HREF= \"javascript:goPage('page_num="+ (startRec+this.showRowNum) +"&NOT_FIRST_DISPLAY=1" +StringEscapeUtils.escapeJavaScript(extraURLInfo + xSearchStr) +"')\">Next Page</A>");
         }
         else {
          result.append("&nbsp;<A HREF= \"javascript:goPage('page_num="+ (startRec+this.showRowNum) +"&NOT_FIRST_DISPLAY=1" +StringEscapeUtils.escapeJavaScript(extraURLInfo) +"')\">Next Page</A>");
         }
       }
       result.append("</p>");
       result.append("<P class=\"OraFieldText\"><b>Go Page: ");
       for (int i=0; i < rNum; i+=this.showRowNum) {
         //if ((i/this.showRowNum +1) ==50 ||(i/this.showRowNum +1) ==100) {
         if (pageList.contains(new Integer(i/this.showRowNum +1))) {  
           result.append("<br>");
         }
         if(this.mySearchStr!=null) {
          result.append("&nbsp;<A HREF= \"javascript:goPage('page_num="+ i +"&NOT_FIRST_DISPLAY=1" + StringEscapeUtils.escapeJavaScript(extraURLInfo + xSearchStr)  +"')\">" + (i/this.showRowNum +1) + "</A>");
         }
         else {
          result.append("&nbsp;<A HREF= \"javascript:goPage('page_num="+ i +"&NOT_FIRST_DISPLAY=1" + StringEscapeUtils.escapeJavaScript(extraURLInfo) +"')\">" + (i/this.showRowNum +1) + "</A>");
         }
       }
       
     }
     return result.toString();
   }
 
   /**
    * Makes the total number of records available to calling JSP's.  Note that
    * this is the total number of records satisfied by the query, not displayed at one time.
    * @return Total number of records
    */
   public int getTotalRecordCount() {
     int result = 0;
     if (this.rsVector != null) {
       result = rsVector.size();
     }
     return result;
   }
 
   /**
    * Displays the result hit list based on the results of the user's search.
    * Calling JSP must provide &lt;TABLE&gt; tags.
    * @return String containing the hit list HTML.
    */
   public String getHitList() {
     StringBuffer result = new StringBuffer();
     int numOfCol      =(displayParm.length + jspLinkParm.length) /2 ;
     int numOfLinkParm = jspLinkParm.length/2;
     int colNum=0;
     String[] linkParmName =  new String[numOfLinkParm];
     String[] linkParm =  new String[numOfLinkParm];
     int display_num;
     String masterName="";
     String linkParms="";
 
     if (this.isFirstDisplay()) {
       result.append("");
     }
     else {
 
      for (int a=0; a<numOfLinkParm; a++) {
               linkParmName[a]=jspLinkParm[a*2 +1];
      }
 
      //result.append("<TABLE border=\"0\" cellspacing=1 cellpadding=1 BGCOLOR=\"#F7F7E7\">");
      result.append("<TABLE border=\"0\" cellspacing=1 cellpadding=1 class=\"OraBGAccentVeryDark\">");
      //result.append("<TABLE border=\"0\" CLASS=\"OracleHitListTable\">");
 
      result.append("<TR class=\"OraTableRowHeader\">");
 
      rNum=this.getRowCount();
      display_num=this.showRowNum;
      colNum = displayParm.length;
      for (int i=1; i<colNum; i+=2){ // for loop 1
        //result.append("<TH ALIGN=\"LEFT\" valign=\"BOTTOM\" BGCOLOR=\"#CCCC99\"><font size=\"-1\" FACE=\"ARIAL\" >" + displayParm[i] + "</font></TH>");
        result.append("<TH ALIGN=\"LEFT\" valign=\"BOTTOM\"><font size=\"-1\" FACE=\"ARIAL\" nowrap>" + StringEscapeUtils.escapeHtml(displayParm[i]) + "</font></TH>");
      } //for loop 1  over
 
      result.append("</TR>");
 
      if (rNum > 0) {
        startRec=(myRequest.getParameter("page_num")!=null)?Integer.parseInt(myRequest.getParameter("page_num")):0;
 
        /* check  start record s and assign display record number */
        if (rNum - startRec >= this.showRowNum) {
            display_num = this.showRowNum;
        }
        else {
            display_num = rNum- startRec;
        }
 
        //
        // Skip the previous page's records up to the start record for the current page
        //
        for (int i=0; i <startRec && this.hasMoreDetailRows(); i++ ) {
        }
 
        for (int i =startRec ; i<display_num + startRec && this.hasMoreDetailRows(); i++){
          for (int a=0; a<numOfLinkParm; a++) {
            int p=numOfCol-numOfLinkParm + a;
            linkParm[a]=linkParmName[a]+ "=" + this.getColValue(p);
            //System.out.println("i=s-->linkParm[" +a +"]: " + linkParm[a]);
          }
          linkParms="";
          for (int m=0; m<linkParm.length; m++) {
            linkParms+="&" + linkParm[m];
          }
       
 
 
          //System.out.println("linkParms: " + linkParms);
 
          result.append("<TR class=\"OraTabledata\">");
 
          // check if the display column is the link column
          int dispLength=0;
          if ( this.getDisplayParm()!=null){
            dispLength=this.getDisplayParm().length;
          }
          //System.out.println("dispLength: " + dispLength);
          for (int d=0; d<dispLength/2; d++) {
            /********* Skip repetitive first col but show it on new page ***********/
           if (d==0){
             if (!masterName.equals(this.getColValue(d)) ||
                 i==startRec ||
                 this.getCompressFlag()==false){
               if (d==this.getLinkCol()) {
                 if (displayMode == LIST_DISPLAY) {
                   result.append(
                     "<TD ALIGN=\"LEFT\" valign=\"TOP\"><font size=\"-1\" FACE=\"ARIAL\" nowrap><a href=\"javascript:redirect1('" +
                     StringEscapeUtils.escapeHtml(this.getDetailReq_Type()) + "','" + StringEscapeUtils.escapeHtml(linkParms) +
                     "')\">" + StringEscapeUtils.escapeHtml(this.getColValue(d)) + "</font></a></TD>");
                 }
                 else {
                   // THIS ASSUMES THAT THERE IS ONLY ONE LINK COLUMN
                   // AND ONLY ONE DISPLAY COLUMN
                   //
                   // LOV's should always have first display column = return display column
                   // and first link column (only one) = ID column
                   String lovLink = "";
                   if (myLovCols != null) {
 
                     for (int m=0; m<myLovCols.length; m++) {
                       if (m != (myLovCols.length-1)) {
                         lovLink = lovLink + "'"+getValidJSString(this.getColValue(myLovCols[m])) +"'" +",";
                       }
                       else{
                         lovLink = lovLink + "'"+getValidJSString(this.getColValue(myLovCols[m]))+"'";
                       }
 
                     }
                   }
                   result.append(
                     "<TD ALIGN=\"LEFT\" valign=\"TOP\"><font size=\"-1\" FACE=\"ARIAL\" nowrap><a href=\"javascript:passback('" +
                     StringEscapeUtils.escapeHtml(this.getColValue(dispLength/2)) + "'," + StringEscapeUtils.escapeHtml(lovLink) +
                     ")\">" + StringEscapeUtils.escapeHtml(this.getColValue(d)) + "</font></a></TD>");
                 }
               }
               else {
                 result.append("<TD ALIGN=\"LEFT\" valign=\"TOP\"><font size=\"-1\" FACE=\"ARIAL\" nowrap>" + StringEscapeUtils.escapeHtml(this.getColValue(d)) + "</font></TD>");
               }
             }
             else {
               result.append("<TD>&nbsp;</TD>");
             }
           }
           else if (d==this.getLinkCol()) {
             if (displayMode == LIST_DISPLAY) {
               result.append(
                 "<TD ALIGN=\"LEFT\" valign=\"TOP\"><font size=\"-1\" FACE=\"ARIAL\" nowrap><a href=\"javascript:redirect1('" +
                 StringEscapeUtils.escapeHtml(this.getDetailReq_Type()) + "','" + StringEscapeUtils.escapeHtml(linkParms) + "')\">" +
                 StringEscapeUtils.escapeHtml(this.getColValue(d)) + "</font></a></TD>");
             }
             else {
               // THIS ASSUMES THAT THERE IS ONLY ONE LINK COLUMN
               // AND ONLY ONE DISPLAY COLUMN
               //
               // LOV's should always have first display column = return display column
               // and first link column (only one) = ID column
               
               result.append(
                 "<TD ALIGN=\"LEFT\" valign=\"TOP\"><font size=\"-1\" FACE=\"ARIAL\" nowrap><a href=\"javascript:passback('" +
                 StringEscapeUtils.escapeHtml(this.getColValue(dispLength/2)) + "','" + StringEscapeUtils.escapeHtml(this.getColValue(0)) +
                 "')\">" + StringEscapeUtils.escapeHtml(this.getColValue(d)) + "</font></a></TD>");
             }
           }
           else {
             // for all non link columns, replace nulls with a single space
             if (this.getColValue(d) != null) {
               result.append("<TD ALIGN=\"LEFT\" valign=\"TOP\"><font size=\"-1\" FACE=\"ARIAL\" nowrap>" + StringEscapeUtils.escapeHtml(this.getColValue(d)) + "</font></TD>");
             }
             else {
               result.append("<TD>&nbsp;</TD>");
             }
           }
         }
         result.append("</TR>");
         masterName = this.getColValue(0);
       }
     }
     result.append("</TABLE>");
     }
     return result.toString();
   }
 
   /**
    * Displays the search fields that accept user input.  Calling JSP's must
    * provide &lt;TABLE&gt;, &lt;FORM&gt;, and FIND button tags.
    * @return String containing the search fields HTML.
    */
   public String getSearchFields() {
     StringBuffer result = new StringBuffer();
     int sArLength = searchParm.length / 2;
 
     //String   xSearchStr="";
     for( int s=0; s<sArLength; s++) {
       //result.append("<TR><TD ALIGN=\"LEFT\">" + searchParm[2*s+1] + "</TD>");
       result.append("<TR><TD class=\"fieldtitlebold\" nowrap>" + StringEscapeUtils.escapeHtml(searchParm[2*s+1]) + "</TD>");
       if (mySearchStr!=null) {
         xSearchStr+=StringEscapeUtils.escapeHtml("&SEARCH=" + mySearchStr[s]);        
         //result.append("<TD ALIGN=\"LEFT\"><INPUT TYPE=\"text\" NAME=\"SEARCH\" SIZE=\"15\" value=\"" + mySearchStr[s] + "\"></TD></TR>");
         result.append("<TD class=\"OraFieldText\" nowrap><INPUT TYPE=\"text\" NAME=\"SEARCH\" SIZE=\"15\" value=\"" + StringEscapeUtils.escapeHtml(mySearchStr[s]) + "\"></TD></TR>");
       }
       else {
         //result.append("<TD ALIGN=\"LEFT\"><INPUT TYPE=\"text\" NAME=\"SEARCH\" SIZE=\"15\" value=\"\"></TD></TR>");
         result.append("<TD class=\"OraFieldText\" nowrap><INPUT TYPE=\"text\" NAME=\"SEARCH\" SIZE=\"15\" value=\"\"></TD></TR>");
       }
     }
     return result.toString();
   }
 
   public String getExtraURLInfo() {
     return extraURL;
   }
 
   public void setExtraURLInfo(String s) {
     extraURL=s;
   }
 // Added for SBR  
   public String getValidJSString(String sourceStr) {
     String newStr = StringReplace.strReplace(sourceStr,"'","\\'");
     newStr = StringReplace.strReplace(newStr,"\"","&quot;");
     return newStr;
   }
 
   public void setSearchInfo() {
     int sArLength = searchParm.length / 2;
     for( int s=0; s<sArLength; s++) {
       if (mySearchStr!=null) {
         xSearchStr+="&SEARCH=" + mySearchStr[s];
       }
     }
   }
   public void setSearchInfo(String urlParams) {
     xSearchStr = urlParams;
   }
   public Vector getQueryResults(){
     return rsVector;
   }
   public void resetRequest(HttpServletRequest request) {
     myRequest = request;
     rsVectorIdx = 0;
   }
   public void resetResultVectorIdx(){
     rsVectorIdx = 0;
   }
 
   /**
    * Displays the page information in a list.
    * @return String containing page info HTML.
    */
   public String getPageInfoAsPopList() {
     StringBuffer result = new StringBuffer();
     String extraURLInfo = this.getExtraURLInfo();
     if (rNum>this.showRowNum){
       result.append("<p>Current Page: <b>" + (startRec/this.showRowNum +1) + "&nbsp;&nbsp;&nbsp;&nbsp;");
       if (rNum-startRec>this.showRowNum){
         if(this.mySearchStr!=null) {
          result.append("&nbsp;<A HREF= \"javascript:goPage('page_num="+ (startRec+this.showRowNum) +"&NOT_FIRST_DISPLAY=1" +StringEscapeUtils.escapeJavaScript(extraURLInfo + xSearchStr) +"')\">Next Page</A>");
         }
         else {
          result.append("&nbsp;<A HREF= \"javascript:goPage('page_num="+ (startRec+this.showRowNum) +"&NOT_FIRST_DISPLAY=1" +StringEscapeUtils.escapeJavaScript(extraURLInfo) +"')\">Next Page</A>");
         }
       }
       result.append("</p>");
       result.append("<P><b>Go Page: ");
       for (int i=0; i < rNum; i+=this.showRowNum) {
         if(this.mySearchStr!=null) {
          result.append("&nbsp;<A HREF= \"javascript:goPage('page_num="+ i +"&NOT_FIRST_DISPLAY=1" + StringEscapeUtils.escapeJavaScript(extraURLInfo) + xSearchStr  +"')\">" + (i/this.showRowNum +1) + "</A>");
         }
         else {
          result.append("&nbsp;<A HREF= \"javascript:goPage('page_num="+ i +"&NOT_FIRST_DISPLAY=1" + StringEscapeUtils.escapeJavaScript(extraURLInfo) +"')\">" + (i/this.showRowNum +1) + "</A>");
         }
       }
     }
     return result.toString();
   }
 }
