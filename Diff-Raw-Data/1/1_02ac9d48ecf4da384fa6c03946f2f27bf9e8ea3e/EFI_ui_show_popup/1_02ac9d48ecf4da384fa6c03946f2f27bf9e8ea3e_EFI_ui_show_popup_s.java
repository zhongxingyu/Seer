 /*
  * Created on 21. feb. 2007
  *
  * Copyright (c) 2005, Karl Trygve Kalleberg <karltk@ii.uib.no>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter.library.eclipse;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.spoofax.interpreter.IConstruct;
 import org.spoofax.interpreter.IContext;
 import org.spoofax.interpreter.InterpreterException;
 import org.spoofax.interpreter.Tools;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class EFI_ui_show_popup extends AbstractPrimitive {
 
     public EFI_ui_show_popup() {
         super("EFI_ui_show_popup", 0, 2);
     }
     
     @Override
     public boolean call(IContext env, IConstruct[] svars, IStrategoTerm[] tvars)
             throws InterpreterException {
         if(!Tools.isTermString(tvars[0]))
             return false;
         if(!Tools.isTermString(tvars[1]))
             return false;
         
         final String title = Tools.asJavaString(tvars[0]); 
         final String body = Tools.asJavaString(tvars[1]);
         
         Shell shell = new Shell();
         MessageDialog.openInformation(
             shell,
             title,
             body);
         
         return true;
     }
 
 }
