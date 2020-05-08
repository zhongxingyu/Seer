 /*
 /*
  * jPOS Project [http://jpos.org]
  * Copyright (C) 2000-2010 Alejandro P. Revilla
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
 package org.jpos.ee.pm.struts.actions;
 
 import org.jpos.ee.pm.core.EntityInstanceWrapper;
 import org.jpos.ee.pm.core.PMException;
 import org.jpos.ee.pm.core.PMLogger;
 import org.jpos.ee.pm.core.PMMessage;
 import org.jpos.ee.pm.struts.PMStrutsContext;
 import org.jpos.util.DisplacedList;
 
 public abstract class RowActionSupport extends FieldProcessingActionSupport {
     
     public boolean testSelectedExist(){ return true; }
 
     protected boolean prepare(PMStrutsContext ctx) throws PMException {
         super.prepare(ctx);
         
         //If we get item param, we change the selected item on the container
         String item = ctx.getParameter("item");
         if(item!=null && item.trim().compareTo("") != 0){
             Integer index = Integer.parseInt(item);
             PMLogger.debug(this,"Getting row index: "+ index);
             if(index != null){
                 DisplacedList <Object> al = new DisplacedList<Object>();
                 if(ctx.isWeak()){
                     al.addAll(getModifiedOwnerCollection(ctx, ctx.getEntity().getOwner().getEntityProperty()));
                 }else{
                     al.addAll(ctx.getList().getContents());
                     al.setDisplacement(ctx.getList().getContents().getDisplacement());
                 }
                Object o = al.get(index);
                ctx.getEntity().getDataAccess().refresh(ctx,o);
                 ctx.getEntityContainer().setSelected(new EntityInstanceWrapper(o));
             }
         }else{
             String identified = ctx.getParameter("identified");
             if(identified!=null && identified.trim().compareTo("") != 0){
                 PMLogger.debug(this,"Getting row identified by: "+identified);
                 String[] ss = identified.split(":");
                 //TODO Throw exception when the size of this is not 2
                 if(ss.length != 2) PMLogger.error ("Ivalid row identifier!");
                 else{
                     String prop = ss[0];
                     String value= ss[1];
                     EntityInstanceWrapper wrapper = new EntityInstanceWrapper( ctx.getEntity().getDataAccess().getItem(ctx, prop, value) );
                     ctx.getEntityContainer().setSelected(wrapper);
                 }
             }else{
                 PMLogger.debug(this,"Row Selection ignored");
             }
         }
         
         if(testSelectedExist() && ctx.getEntityContainer().getSelected() == null){
             ctx.getErrors().add(new PMMessage(ENTITY , "unknow.item"));
                throw new PMException();
         }else
             return true;
     }
 }
