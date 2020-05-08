 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2010 Per Cederberg & Dynabyte AB.
  * All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.app.proc;
 
 import org.rapidcontext.core.data.Array;
 import org.rapidcontext.core.proc.Bindings;
 import org.rapidcontext.core.proc.CallContext;
 import org.rapidcontext.core.proc.Library;
 import org.rapidcontext.core.proc.Procedure;
 import org.rapidcontext.core.proc.ProcedureException;
 import org.rapidcontext.core.security.Restricted;
 import org.rapidcontext.core.security.SecurityContext;
 
 /**
  * The built-in procedure list procedure.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class ProcedureListProcedure implements Procedure, Restricted {
 
     /**
      * The procedure name constant.
      */
     public static final String NAME = "System.Procedure.List";
 
     /**
      * The default bindings.
      */
     private Bindings defaults = new Bindings();
 
     /**
      * Creates a new library list procedure.
      */
     public ProcedureListProcedure() {
         defaults.seal();
     }
 
     /**
      * Checks if the currently authenticated user has access to this
      * object.
      *
      * @return true if the current user has access, or
      *         false otherwise
      */
     public boolean hasAccess() {
         return SecurityContext.currentUser() != null;
     }
 
     /**
      * Returns the procedure name.
      *
      * @return the procedure name
      */
     public String getName() {
         return NAME;
     }
 
     /**
      * Returns the procedure description.
      *
      * @return the procedure description
      */
     public String getDescription() {
         return "List all available procedures.";
     }
 
     /**
      * Returns the bindings for this procedure. If this procedure
      * requires any special data, adapter connection or input
      * argument binding, those bindings should be set (but possibly
      * to null or blank values).
      *
      * @return the bindings for this procedure
      */
     public Bindings getBindings() {
         return defaults;
     }
 
     /**
      * Executes a call of this procedure in the specified context
      * and with the specified call bindings. The semantics of what
      * the procedure actually does, is up to each implementation.
      * Note that the call bindings are normally inherited from the
      * procedure bindings with arguments bound to their call values.
      *
      * @param cx             the procedure call context
      * @param bindings       the call bindings to use
      *
      * @return the result of the call, or
      *         null if the call produced no result
      *
      * @throws ProcedureException if the call execution caused an
      *             error
      */
     public Object call(CallContext cx, Bindings bindings)
         throws ProcedureException {
 
         Library   library = cx.getLibrary();
         String[]  names;
         Array     list;
 
         names = library.getProcedureNames();
         list = new Array(names.length);
         for (int i = 0; i < names.length; i++) {
            // TODO: This security check should be built-into SecurityContext
            if (library.hasBuiltIn(names[i])) {
                if (SecurityContext.hasAccess(library.getProcedure(names[i]))) {
                    list.add(names[i]);
                }
            } else if (SecurityContext.hasAccess("procedure", names[i], "")) {
                 list.add(names[i]);
             }
         }
         list.sort();
         return list;
     }
 }
