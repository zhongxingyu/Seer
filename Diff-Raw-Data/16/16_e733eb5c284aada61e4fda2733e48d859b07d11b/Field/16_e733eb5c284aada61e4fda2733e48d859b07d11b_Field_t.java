 /*
  * jPOS Presentation Manager [http://jpospm.blogspot.com]
  * Copyright (C) 2010 Jeronimo Paoletti [jeronimo.paoletti@gmail.com]
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jpos.ee.pm.core;
 
 import java.util.ArrayList;
 import java.util.Properties;
 
 import org.jpos.ee.pm.converter.Converter;
 import org.jpos.ee.pm.converter.ConverterException;
 import org.jpos.ee.pm.converter.Converters;
 import org.jpos.ee.pm.converter.GenericConverter;
 import org.jpos.ee.pm.validator.Validator;
 
 /**A Field represents an attribute of the represented entity. 
  * 
  * <h2>Simple entity configuration file</h2>
  * <pre>
  * {@code
  * <field id="id" display="all | some_operations" align="right | left | center" width="xxxx" />
  *     ....
  * </field>
  * }
  * </pre>
  * @author J.Paoletti jeronimo.paoletti@gmail.com
  * */
 public class Field extends PMCoreObject{
     /**The id of the field, there must be a getter and a setter for this name on the represented entity.*/
     private String id;
     
     /**The width of the field value*/
     private String width;
     private String display;
     private int size;
     private int maxLength;
     /**@deprecated*/
     private boolean sortable;
     /**@deprecated*/
     private boolean searchable;
     private ArrayList<Validator> validators;
     private Converters converters;
     /**@deprecated*/
     private String searchCriteria; 
     private String defaultValue;   
     private String align; //left right center          
     private boolean multiEditable; 
     
     
     public Field () {
         super();
         align="left";
         multiEditable=false;
         defaultValue="";
     }
 
     public String visualize(PMContext ctx) throws PMException{
         debug("Converting ["+ctx.getOperation().getId()+"]"+ctx.getEntity().getId()+"."+getId());
         try {
             Converter c = null;
             if(getConverters()!= null){
                 c = getConverters().getConverterForOperation(ctx.getOperation().getId());
             }
             if(c == null){
                 c = new GenericConverter();
                 c.setService(getService());
                 Properties properties = new Properties();
                 properties.put("filename", "cfg/converters/show.tostring.converter");
                 c.setProperties(properties);
             }
             ctx.put(PM_ENTITY_INSTANCE_WRAPPER, new EntityInstanceWrapper(ctx.get(PM_ENTITY_INSTANCE)));
             ctx.put(PM_FIELD, this);
             return getService().visualizationWrapper(c.visualize(ctx));
         } catch (Exception e) {
             PMLogger.error(e);
             throw new ConverterException("Unable to convert "+ctx.getEntity().getId()+"."+getId());
         }
     }
     
     /**Set also converters service
      * @see org.jpos.ee.pm.core.PMCoreObject#setService(org.jpos.ee.pm.core.PMService)
      */
     public void setService(PMService service) {
         super.setService(service);
         getConverters().setService(service);
     }
 
 
     
     public int compareTo(Field o) {
         return getId().compareTo(o.getId());
     }
     
     public ArrayList<Validator> getValidators() {
         return validators;
     }
 
 
     public void setValidators(ArrayList<Validator> validators) {
         this.validators = validators;
     }
 
     public void setId (String id) {
         this.id = id;
     }
     public String getId() {
         return id;
     }
     /*public void setName (String name) {
         this.name = name;
     }
     public String getName() {
         return name;
     }
     public void setShortName (String shortName) {
         this.shortName = shortName;
     }
     public String getShortName() {
         return shortName != null ? shortName : name;
     }*/
 
 /*    public void setReadPerm (Permission readPerm) {
         this.readPerm = readPerm;
     }
     public Permission getReadPerm () {
         return readPerm;
     }
     public void setWritePerm (Permission writePerm) {
         this.writePerm = writePerm;
     }
     public Permission getWritePerm () {
         return writePerm;
     }*/
     public void setMaxLength (int maxLength) {
         this.maxLength = maxLength;
     }
     public int getMaxLength() {
         return maxLength;
     }
     public void setSize (int size) {
         this.size = size;
     }
     public int getSize () {
         return size;
     }
 
     public void setDisplay (String display) {
         this.display = display;
     }
     public String getDisplay() {
     	if(display==null || display.trim().compareTo("")==0) return "all";
         return display;
     }
     public void setSortable (boolean sortable) {
         this.sortable = sortable;
     }
     public boolean isSortable () {
         return sortable;
     }
     public void setSearchable (boolean searchable) {
         this.searchable = searchable;
     }
     public boolean isSearchable () {
         return searchable || "id".equals (getId());
     }

    /**Indicates if the field is shown in the given operation id
     *
     * @param operationId  The Operation id
     * @return true if field is displayed on the operation
     */
    public boolean shouldDisplay (String operationId) {
        if (operationId == null || getDisplay() == null) return false;
        return "all".equalsIgnoreCase(getDisplay()) || getDisplay().indexOf (operationId) >= 0;
     }
     // Helpers
     public boolean canUpdate () {
         return isDisplayInEdit();
     }
     public boolean isDisplayInList () {
         return shouldDisplay("list");// && hasPermission (getReadPerm());
     }
     public boolean isDisplayInShow() {
         return shouldDisplay("show");// && hasPermission (getReadPerm());
     }
     public boolean isDisplayInEdit() {
         return shouldDisplay("edit");// && hasPermissions (getReadPerm(), getWritePerm());
     }
     public boolean isDisplayInAdd() {
         return shouldDisplay("add");// && hasPermissions (getReadPerm(), getWritePerm());
     }
     /*public boolean hasPermission (Permission permName) {
         if (permName != null) {
             SECUser u = PresentationManagerAction.getContextUser();
             if (u == null)
                 return false;
             return u.hasPermission(permName);
         }
         return true;
     }
     public boolean hasPermissions (Permission perm0, Permission perm1) {
         SECUser u = PresentationManagerAction.getContextUser();
         if ((u == null) ||
             (perm0 != null && !u.hasPermission(perm0)) ||
             (perm1 != null && !u.hasPermission(perm1)))
             return false;
         return true;
     }*/
 
     public Object fromString (String s) {
         return s;
     }
    
     public String toString (Object obj) {
         return obj.toString();
     }
     public void setSearchCriteria(String searchCriteria) {
         this.searchCriteria = searchCriteria;
     }
     public String getSearchCriteria() {
         return searchCriteria;
     }
 
     public void setDefaultValue(String defaultValue) {
         this.defaultValue = defaultValue;
     }
 
     public String getDefaultValue() {
         return defaultValue;
     }
     public void setAlign(String align) {
         this.align = align;
     }
     public String getAlign() {
         return align;
     }
     public void setMultiEditable(boolean multiEditable) {
         this.multiEditable = multiEditable;
     }
     public boolean isMultiEditable() {
         return multiEditable;
     }
 
 
     public void setWidth(String width) {
         this.width = width;
     }
 
 
     public String getWidth() {
     	if(width==null) return "";
         return width;
     }
 
     /**
      * @param converters the converters to set
      */
     public void setConverters(Converters converters) {
         this.converters = converters;
     }
 
     /**
      * @return the converters
      */
     public Converters getConverters() {
         if(converters==null) converters = new Converters();
         return converters;
     }
 
 	public String toString() {
 		return id;
 	}
 
 }
