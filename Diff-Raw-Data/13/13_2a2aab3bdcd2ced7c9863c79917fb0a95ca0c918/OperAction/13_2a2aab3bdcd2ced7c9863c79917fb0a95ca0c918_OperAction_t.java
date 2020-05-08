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
 package org.jpos.ee.pm.struts.actions;
 
 import org.jpos.ee.pm.core.Operations;
 import org.jpos.ee.pm.core.PMException;
 import org.jpos.ee.pm.struts.PMStrutsContext;
 
 public class OperAction extends EntityActionSupport{
     
	/**Opens an hibernate transaction before doExecute*/
     protected boolean openTransaction() { return false;    }
     /**Makes the operation generate an audithory entry*/
     protected boolean isAudited() {    return false; }
     /**Forces execute to check if any user is logged in*/
     protected boolean checkUser(){     return true;}
     /**Forces execute to check if there is an entity defined in parameters*/
     protected boolean checkEntity(){ return true; }
 
     protected boolean prepare(PMStrutsContext ctx) throws PMException {
    	Operations operations;
         configureEntityContainer(ctx);
         ctx.setOperation ( ctx.getEntity().getOperations().getOperation("list") );
         ctx.getRequest().setAttribute(OPERATION, ctx.getOperation());
         operations = ctx.getEntity().getOperations().getOperationsFor(ctx.getOperation());
        ctx.getRequest().setAttribute(ITEM_OPERATIONS, operations.getOperationsForScope(SCOPE_ITEM));
         return true;
     }
     
    protected void doExecute(PMStrutsContext ctx)throws PMException{}
 }
