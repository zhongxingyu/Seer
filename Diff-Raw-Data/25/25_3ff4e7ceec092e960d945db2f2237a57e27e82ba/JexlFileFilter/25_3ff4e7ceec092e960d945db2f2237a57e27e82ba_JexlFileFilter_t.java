 /**
  * JBackup is a software managing backups.
  *
  * Copyright (C) 2013-2013 Fabien DUMINY (fabien [dot] duminy [at] webmails [dot] com)
  *
  * JBackup is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * JBackup is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
  * USA.
  */
 package org.jbackup.core.filter;
 
 import org.apache.commons.io.filefilter.AbstractFileFilter;
 import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.jexl2.*;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * An {@link IOFileFilter} that use a JXL expression to filter files.
  */
 public class JexlFileFilter extends AbstractFileFilter {
     private static final JexlEngine ENGINE = new JexlEngine();
 
     static {
         ENGINE.setDebug(true);
         ENGINE.setSilent(false);
         ENGINE.setLenient(false);
         ENGINE.setCache(64);
 
         Map<String, Object> functionMap = new HashMap<String, Object>();
         functionMap.put(null, FileFunctions.class);
         ENGINE.setFunctions(functionMap);
     }
 
     private final String filterName;
     private final String expression;
 
     public JexlFileFilter(String filterName, String expression) {
         this.filterName = filterName;
         this.expression = expression;
     }
 
     @Override
     public boolean accept(File file) {
         // Create an expression object
        Expression e = ENGINE.createExpression(expression, new DebugInfo(filterName, 0, 0));
 
         // Create a context and add data
         JexlContext jctx = new MapContext();
         jctx.set(FileFunctions.FILE, file);
 
         // Now evaluate the expression, getting the result
         return (Boolean) e.evaluate(jctx);
     }
 }
