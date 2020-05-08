 package com.l5m.!REPLACE_STYLE!.engine.worker;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.l5m.common.bean.NavigationBean;
 import com.l5m.common.engine.servicer.ServicerType;
 import com.l5m.common.util.ActionConstants;
 import com.l5m.customtags.beans.BaseServicerParaBean.AbstractBaseWorker;
 import com.l5m.customtags.tags.utils.IconStatus;
 import com.l5m.customtags.tags.utils.TopBarIcon;
 import com.l5m.!REPLACE_STYLE!.engine.servicer.!REPLACE_ME_FILE!ServicerImpl;
 import com.l5m.!REPLACE_STYLE!.engine.servicer.!REPLACE_ME_FILE!ServicerImpl.PANEL;
 
 public class !REPLACE_ME_FILE!Worker extends AbstractBaseWorker {
     private static final long serialVersionUID = 1L;
     public static final String JSP_TOGO_PERIX; /*
      * worker name w/o
      * "Worker"
      */
     public static final String packageLowerCase;    
     static{
         String clazzNames[] =  Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");     
        JSP_TOGO_PERIX = clazzNames[5].substring(0, clazzNames[5].length() - "Worker".length());
         packageLowerCase = clazzNames[2];
     }
     public static final String JSP_TOGO = JSP_TOGO_PERIX + ".jsp";
     public static final String WORKER_NAME = JSP_TOGO_PERIX + "Worker";
 
     public static final String FORM_NAME = JSP_TOGO_PERIX + "Form";
 
     public static final String packageUpperCase = packageLowerCase.toUpperCase();
 
     public static void main(String[] x) {
         // System.out.println(StringEscapeUtils.escapeHtml( "NETWORKPROGRAM"));
     }
 
     @ServicerType("com.l5m.!REPLACE_STYLE!.engine.servicer.!REPLACE_ME_FILE!ServicerImpl")
     private !REPLACE_ME_FILE!ServicerImpl servicer;
 
     public !REPLACE_ME_FILE!Worker() {
         this.navigationBean = new NavigationBean();
         this.navigationBean.setJSPGoTo(JSP_TOGO);
 
     }
 
     @Override
     protected void bindParams(HttpServletRequest request, String btnClicked, String actionMethod) {
         super.bindParams(request, btnClicked, actionMethod);
 
     }
 
     public String[] getActions() {
         return new String[] { "", "pageAction.showDialog('filterDialog')", // filter
                 "", // search
                 "", // highlight
                 "pageAction.showDialog('sortDialog')", // sort
                 "pageAction.showDialog('displayDialog')", // display
                 "pageAction.clickButton('changeDecimal')", // decimal
                 "pageAction.clickExportButton('" + ActionConstants.EXPORT_EXCEL + "')", // excel
                 "pageAction.clickExportButton('" + ActionConstants.EXPORT_PDF + "')", // pdf
                 "pageAction.showDialog('optionDialog')", // options
                 //"pageAction.showDialogDirectly('emailDialog')", // email
                 "pageAction.clickButton('changeSortDisplayMode')", // inc
 
         };
     }
 
     @Override
     public int getDataAvailability() {
         return PANEL.DataAvailability.panelIndex();
     }
 
     /**
      * @return the fORM_NAME
      */
     @Override
     public String getFORM_NAME() {
         return FORM_NAME;
     }
 
     @Override
     public int getHelp() {
         return PANEL.Help.panelIndex();
     }
 
     public IconStatus[] getIconsStatus() {
         final boolean hasData = this.hasData();
         return new IconStatus[] { this.servicer.getBaseServicerParameterBean().isSelectionPanelStatusOpen() ? IconStatus.open : IconStatus.close, hasData ? IconStatus.inactive : IconStatus.inactive, // filter
                 IconStatus.inactive, // search
                 IconStatus.inactive, // highlight
                 hasData ? IconStatus.active : IconStatus.inactive, // sort
                 hasData ? IconStatus.active : IconStatus.inactive, // display
                 hasData ? IconStatus.active : IconStatus.inactive, // decimal
                 hasData ? IconStatus.active : IconStatus.inactive, // excel
                 IconStatus.inactive, // pdf
                 IconStatus.inactive, // options
                 //hasData ? IconStatus.active : IconStatus.inactive, // email
                 IconStatus.active, // inc
         };
 
     }
 
     /**
      * @return the jSP_TOGO
      */
     @Override
     public String getJSP_TOGO() {
         return JSP_TOGO;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.l5m.customtags.beans.BaseServicerParaBean.AbstractBaseWorker#getJSP_TOGO_PERIX()
      */
     @Override
     public String getJSP_TOGO_PERIX() {
         // TODO Auto-generated method stub
         return JSP_TOGO_PERIX;
     }
 
     @Override
     public String getPackageLowerCase() {
         return packageLowerCase;
     }
 
     @Override
     public String getPackageUpperCase() {
         return packageUpperCase;
     }
 
     public PANEL[] getPANEL() {
         return PANEL.values();
     }
 
     @Override
     public String getRenderHTML() {
         if (!this.hasData()) {
             return "";
         }
         try {
         } catch (Exception e) {
             e.printStackTrace();
         }
         return "";
 
     }
 
     @Override
     public !REPLACE_ME_FILE!ServicerImpl getServicer() {
         return this.servicer;
     }
 
     public TopBarIcon[] getTopBarIcons() {
         return new TopBarIcon[] { TopBarIcon.Start, TopBarIcon.Filter, TopBarIcon.Search, TopBarIcon.Highlight, TopBarIcon.Sort, TopBarIcon.Display, TopBarIcon.Decimal, TopBarIcon.Excel,
                 TopBarIcon.PDF, TopBarIcon.Options, /*TopBarIcon.Email,*/ TopBarIcon.IncIcon, };
 
     }
 
     /*
      * @return the wORKER_NAME
      
     @Override
     public String getWORKER_NAME() {
         return WORKER_NAME;
     }*/
 
     @Override
     public boolean hasData() {
         final int panelIndex = this.servicer.getPanelIndex();
         if (panelIndex == PANEL.Help.panelIndex() || panelIndex == PANEL.DataAvailability.panelIndex()) {
             return false;
         }
         return true;
     }
 
     @Override
     protected void initWorker() {
         this.servicer.getBaseServicerParameterBean().setPanelIndex(PANEL.Help.panelIndex());
 
     }
 
     public void setServicer(!REPLACE_ME_FILE!ServicerImpl servicer) {
         this.servicer = servicer;
     }
 
     @Override
     public void submitAction() { 
         super.submitAction();
 
     }
 }
