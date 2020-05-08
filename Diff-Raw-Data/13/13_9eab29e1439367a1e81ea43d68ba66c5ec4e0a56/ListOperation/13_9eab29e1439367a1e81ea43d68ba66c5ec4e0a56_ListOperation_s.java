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
 package org.jpos.ee.pm.core.operations;
 
 import org.jpos.ee.Constants;
 import org.jpos.ee.pm.core.*;
 
 /**
  *
  * @author jpaoletti
  */
 public class ListOperation extends OperationCommandSupport {
 
     public ListOperation(String operationId) {
         super(operationId);
     }
 
     @Override
     protected void doExecute(PMContext ctx) throws PMException {
         super.doExecute(ctx);
         final ListManager listManager = new ListManager();
         final Operations operations = (Operations) ctx.get(OPERATIONS);
 
         PaginatedList pmlist = ctx.getList();
         if (pmlist == null) {
             pmlist = listManager.initList(ctx, operations);
         }
 
         configureOrder(ctx, pmlist);
        pmlist.setPage((Integer) ctx.get("page"));
        pmlist.setRowsPerPage((Integer) ctx.get("rows_per_page"));
 
         ctx.put(Constants.PM_LIST_ORDER, pmlist.getOrder());
         ctx.put(Constants.PM_LIST_ASC, !pmlist.isDesc());
         listManager.configureList(ctx, pmlist, operations);
     }
 
     public void configureOrder(PMContext ctx, PaginatedList pmlist) {
         final String o = ctx.getString("order");
         try {
             if (o != null) {
                 pmlist.setOrder(o);
             } else {
                 pmlist.setOrder(ctx.getEntity().getOrderedFields().get(0).getId());
             }
         } catch (PMException e) {
             PresentationManager.getPm().error(e);
         }
        pmlist.setDesc(ctx.getBoolean("desc",false));
     }
 }
