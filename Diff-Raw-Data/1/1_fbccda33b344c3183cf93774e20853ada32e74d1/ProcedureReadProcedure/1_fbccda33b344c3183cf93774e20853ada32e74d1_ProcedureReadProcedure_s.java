 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2012 Per Cederberg. All rights reserved.
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
 import org.rapidcontext.core.data.Dict;
 import org.rapidcontext.core.proc.AddOnProcedure;
 import org.rapidcontext.core.proc.Bindings;
 import org.rapidcontext.core.proc.CallContext;
 import org.rapidcontext.core.proc.Library;
 import org.rapidcontext.core.proc.Procedure;
 import org.rapidcontext.core.proc.ProcedureException;
 import org.rapidcontext.core.security.Restricted;
 import org.rapidcontext.core.security.SecurityContext;
 
 /**
  * The built-in procedure read procedure.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class ProcedureReadProcedure implements Procedure, Restricted {
 
     /**
      * The procedure name constant.
      */
     public static final String NAME = "System.Procedure.Read";
 
     /**
      * The default bindings.
      */
     private Bindings defaults = new Bindings();
 
     /**
      * Creates a new procedures read procedure.
      *
      * @throws ProcedureException if the initialization failed
      */
     public ProcedureReadProcedure() throws ProcedureException {
         defaults.set("name", Bindings.ARGUMENT, "", "The procedure name");
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
         return "Returns detailed information about a procedure.";
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
 
         String     name;
         Procedure  proc;
 
         name = (String) bindings.getValue("name");
         proc = cx.getLibrary().getProcedure(name);
         if (!SecurityContext.hasAccess(proc, "")) {
             throw new ProcedureException("no procedure '" + name + "' found");
         }
         return getProcedureData(cx.getLibrary(), proc);
     }
 
     /**
      * Converts a procedure object into a data object.
      *
      * @param proc           the procedure
      *
      * @return the data object created
      *
      * @throws ProcedureException if the bindings data access
      *             failed
      */
     static Dict getProcedureData(Library library, Procedure proc)
     throws ProcedureException {
         Dict res = new Dict();
         res.set("name", proc.getName());
         if (proc instanceof AddOnProcedure) {
             res.set("type", ((AddOnProcedure) proc).getType());
         } else {
             res.set("type", "built-in");
         }
         res.set("plugin", library.getProcedurePluginId(proc.getName()));
         res.set("description", proc.getDescription());
         res.set("bindings", getBindingsData(proc.getBindings()));
         return res;
     }
 
     /**
      * Converts a procedure bindings object into a data object.
      *
      * @param bindings       the procedure bindings
      *
      * @return the bindings data array object
      *
      * @throws ProcedureException if the bindings data access
      *             failed
      */
     static Array getBindingsData(Bindings bindings) throws ProcedureException {
         String[]  names = bindings.getNames();
         Array     res = new Array(names.length);
         Dict      obj;
 
         for (int i = 0; i < names.length; i++) {
             obj = new Dict();
             obj.set("name", names[i]);
             obj.set("type", bindings.getTypeName(names[i]));
             obj.set("value", bindings.getValue(names[i], ""));
             obj.set("description", bindings.getDescription(names[i]));
             res.add(obj);
         }
         return res;
     }
 }
