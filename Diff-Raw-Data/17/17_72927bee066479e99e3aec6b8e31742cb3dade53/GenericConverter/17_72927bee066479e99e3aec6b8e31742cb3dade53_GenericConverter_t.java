 /*
  * jPOS Project [http://jpos.org]
  * Copyright (C) 2000-2011 Alejandro P. Revilla
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
 package org.jpos.ee.pm.converter;
 
 import java.io.InputStream;
 
 import org.jpos.ee.pm.core.EntityInstanceWrapper;
 import org.jpos.ee.pm.core.Field;
 import org.jpos.ee.pm.core.PMContext;
 
 import bsh.BshClassManager;
 import bsh.EvalError;
 import bsh.Interpreter;
 import bsh.UtilEvalError;
 import java.io.File;
 import java.io.FileInputStream;
 
 /**
  * A generic converter that uses a beanbash based xml for excecution.
  * 
  * @author jpaoletti
  */
 public class GenericConverter extends Converter {
 
     private String filename;
     private Interpreter bsh;
     private String content;
 
     @Override
     public String visualize(PMContext ctx) throws ConverterException {
         try {
             Interpreter bash = getBsh();
             EntityInstanceWrapper einstance = (EntityInstanceWrapper) ctx.get(PM_ENTITY_INSTANCE_WRAPPER);
             Field field = (Field) ctx.get(PM_FIELD);
             Object o = getValue(einstance, field);
             bash.set("value", o);
             bash.set("converter", this);
             debug("Generic Converter Visualize value: " + o);
            String res = getConfig("null-value", "-");
            if (o != null) {
                String result = bash.eval(content + "\n" + "visualize();").toString();
                res = visualize(result, ctx.getString(PM_EXTRA_DATA));
                if ("IgnoreConvertionException".equals(res)) {
                    throw new IgnoreConvertionException("");
                }
             }
             final Converter c = field.getDefaultConverter();
             if (c != null) {
                 ctx.put(PM_FIELD_VALUE, res);
                 return (String) c.visualize(ctx);
             } else {
                 return res;
             }
         } catch (EvalError e) {
             getLog().error("BSH Interpreter Evaluation", e);
         }
         return null;
     }
 
     @Override
     public Object build(PMContext ctx) throws ConverterException {
         try {
             Interpreter bash = getBsh();
             bash.set("value", ctx.get(PM_FIELD_VALUE));
             bash.set("converter", this);
            final Object res = bash.eval(content + "\n" + "build();");
             if ("IgnoreConvertionException".equals(res)) {
                 throw new IgnoreConvertionException("");
             }
             return res;
         } catch (EvalError e) {
             getLog().error("BSH Interpreter Evaluation Error", e);
         }
         return null;
     }
 
     /**
      *
      */
     public GenericConverter() {
         super();
     }
 
     private Interpreter getBsh() {
         if (bsh == null) {
             try {
                 this.filename = getConfig("filename");
                 readFile(filename);
                 bsh = initBSH();
             } catch (Exception e) {
                 getLog().error("BSH Interpreter Creation", e);
             }
         }
         return bsh;
     }
 
     /**
      * Reads the content of the converter file
      *
      * @param filename The file
      * @throws ConverterException
      */
     public void readFile(String filename) throws ConverterException {
         try {
             final File file = new File(filename);
             content = "";
             final InputStream input = new FileInputStream(file);
             while (input.available() > 0) {
                 content = content + (char) input.read();
             }
             input.close();
         } catch (Exception e) {
             throw new ConverterException(e);
         }
     }
 
     /**
      *
      * @return a descriptive string
      */
     public String getDescription() {
         StringBuilder sb = new StringBuilder();
         sb.append(getClass().getName());
         if (filename != null) {
             sb.append('[');
             sb.append(filename);
             sb.append(']');
         }
         return sb.toString();
     }
 
     private Interpreter initBSH() throws UtilEvalError, EvalError {
         Interpreter bash = new Interpreter();
         BshClassManager bcm = bash.getClassManager();
         bcm.setClassPath(getPresentationManager().getService().getServer().getLoader().getURLs());
         bcm.setClassLoader(getPresentationManager().getService().getServer().getLoader());
         bash.set("qbean", this);
         return bash;
     }
 }
