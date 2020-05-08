 /**
  *  C-Nery - A home automation web application for C-Bus.
  *  Copyright (C) 2008  Dave Oxley <dave@daveoxley.co.uk>.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
  
 package cnery;
 
 import com.daveoxley.cbus.CGateException;
 import com.daveoxley.cbus.Group;
 import com.daveoxley.cnery.AbstractTablePageBean;
 import com.sun.data.provider.RowKey;
 import com.sun.webui.jsf.component.Alert;
 import com.sun.webui.jsf.component.TableRowGroup;
 import com.sun.webui.jsf.component.TextField;
 import java.util.ArrayList;
 import javax.faces.FacesException;
 
 /**
  * <p>Page bean that corresponds to a similarly named JSP page.  This
  * class contains component definitions (and initialization code) for
  * all components that you have defined on this page, as well as
  * lifecycle methods and event handlers where you may add behavior
  * to respond to incoming events.</p>
  *
  * @author Dave Oxley <dave@daveoxley.co.uk>
  */
 public class Groups extends AbstractTablePageBean {
     // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
 
     /**
      * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
      * This method is automatically generated, so any user-specified code inserted
      * here is subject to being replaced.</p>
      */
     private void _init() throws Exception {
     }
     private TableRowGroup tableRowGroup1 = new TableRowGroup();
 
     public TableRowGroup getTableRowGroup1() {
         return tableRowGroup1;
     }
 
     public void setTableRowGroup1(TableRowGroup trg) {
         this.tableRowGroup1 = trg;
     }
     private Alert alert1 = new Alert();
 
     public Alert getAlert1() {
         return alert1;
     }
 
     public void setAlert1(Alert a) {
         this.alert1 = a;
     }
 
     // </editor-fold>
 
     private TextField ramp = new TextField();
 
     public TextField getRamp() {
         return ramp;
     }
 
     public void setRamp(TextField tf) {
         this.ramp = tf;
     }
     private TextField seconds = new TextField();
 
     public TextField getSeconds() {
         return seconds;
     }
 
     public void setSeconds(TextField tf) {
         this.seconds = tf;
     }
 
     /**
      * <p>Construct a new Page bean instance.</p>
      */
     public Groups() {
     }
 
     /**
      * <p>Callback method that is called whenever a page is navigated to,
      * either directly via a URL, or indirectly via page navigation.
      * Customize this method to acquire resources that will be needed
      * for event handlers and lifecycle methods, whether or not this
      * page is performing post back processing.</p>
      * 
      * <p>Note that, if the current request is a postback, the property
      * values of the components do <strong>not</strong> represent any
      * values submitted with this request.  Instead, they represent the
      * property values that were saved for this view when it was rendered.</p>
      */
     @Override
     public void init() {
         // Perform initializations inherited from our superclass
         super.init();
         // Perform application initialization that must complete
         // *before* managed components are initialized
         // TODO - add your own initialiation code here
         
         // <editor-fold defaultstate="collapsed" desc="Managed Component Initialization">
         // Initialize automatically managed components
         // *Note* - this logic should NOT be modified
         try {
             _init();
         } catch (Exception e) {
             log("Groups Initialization Failure", e);
             throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
         }
         
         // </editor-fold>
         // Perform application initialization that must complete
         // *after* managed components are initialized
         // TODO - add your own initialization code here
     }
 
     /**
      * <p>Callback method that is called after the component tree has been
      * restored, but before any event processing takes place.  This method
      * will <strong>only</strong> be called on a postback request that
      * is processing a form submit.  Customize this method to allocate
      * resources that will be required in your event handlers.</p>
      */
     public void preprocess() {
     }
 
     /**
      * <p>Callback method that is called just before rendering takes place.
      * This method will <strong>only</strong> be called for the page that
      * will actually be rendered (and not, for example, on a page that
      * handled a postback and then navigated to a different page).  Customize
      * this method to allocate resources that will be required for rendering
      * this page.</p>
      */
     public void prerender() {
     }
 
     /**
      * <p>Callback method that is called after rendering is completed for
      * this request, if <code>init()</code> was called (regardless of whether
      * or not this was the page that was actually rendered).  Customize this
      * method to release resources acquired in the <code>init()</code>,
      * <code>preprocess()</code>, or <code>prerender()</code> methods (or
      * acquired during execution of an event handler).</p>
      */
     public void destroy() {
     }
 
     public void setSelected(Object object) {
         super.setSelected(object);
     }
 
     public Object getSelected() {
         return super.getSelected();
 
     }
 
     public Object getSelectedValue() {
         return super.getSelectedValue();
 
     }
 
     public boolean getSelectedState() {
         return super.getSelectedState();
     }
 
     public String button1_action() throws CGateException {
         try {
             ArrayList<Group> groups = getSessionBean1().application.getGroups(getSessionBean1().cgate_session);
             RowKey[] selectedRowKeys = getTableRowGroup1().getSelectedRowKeys();
             for (RowKey row_key : selectedRowKeys)
             {
                 Group group = groups.get(Integer.parseInt(selectedRowKeys[0].getRowId()));
                 group.on(getSessionBean1().cgate_session);
             }
         } catch (Exception e) {
             alert1.setSummary(e.getLocalizedMessage());
         }
         return null;
     }
 
     public String button2_action() throws CGateException {
         try {
             ArrayList<Group> groups = getSessionBean1().application.getGroups(getSessionBean1().cgate_session);
             RowKey[] selectedRowKeys = getTableRowGroup1().getSelectedRowKeys();
             for (RowKey row_key : selectedRowKeys)
             {
                 Group group = groups.get(Integer.parseInt(selectedRowKeys[0].getRowId()));
                 group.off(getSessionBean1().cgate_session);
             }
         } catch (Exception e) {
             alert1.setSummary(e.getLocalizedMessage());
         }
         return null;
     }
 
     public String button3_action() throws CGateException {
         try {
             ArrayList<Group> groups = getSessionBean1().application.getGroups(getSessionBean1().cgate_session);
             RowKey[] selectedRowKeys = getTableRowGroup1().getSelectedRowKeys();
             for (RowKey row_key : selectedRowKeys)
             {
                 Group group = groups.get(Integer.parseInt(selectedRowKeys[0].getRowId()));
                int ramp = Integer.valueOf((String)getRamp().getValue());
                int seconds = Integer.valueOf((String)getSeconds().getValue());
                 group.ramp(getSessionBean1().cgate_session, ramp, seconds);
             }
         } catch (Exception e) {
             alert1.setSummary(e.getLocalizedMessage());
         }
         return null;
     }
 
     public boolean getActionsDisabled() {
         return getTableRowGroup1().getRenderedSelectedRowsCount() < 1;
     }
 }
 
