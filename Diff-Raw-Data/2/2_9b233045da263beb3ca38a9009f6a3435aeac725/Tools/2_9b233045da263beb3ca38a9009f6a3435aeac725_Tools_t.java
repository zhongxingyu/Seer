 /*
  * Created on 24.jun.2005
  *
  * Copyright (c) 2004, Karl Trygve Kalleberg <karltk@ii.uib.no>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter;
 
 import aterm.AFun;
 import aterm.ATerm;
 import aterm.ATermAppl;
 import aterm.ATermInt;
 import aterm.ATermList;
 import aterm.ATermReal;
 import aterm.pure.PureFactory;
 
 public class Tools {
 
     public static String stringAt(ATerm t, int i) {
         return ((ATermAppl) t.getChildAt(i)).getName();
     }
 
     public static ATermAppl applAt(ATerm t, int i) {
         return (ATermAppl) ((ATermAppl) t).getChildAt(i);
     }
 
     public static ATermAppl applAt(ATermList t, int i) {
         return (ATermAppl) t.getChildAt(i);
     }
 
     public static ATermInt intAt(ATerm t, int i) {
         return (ATermInt) ((ATermAppl) t).getChildAt(i);
     }
 
     public static ATermInt intAt(ATermList t, int i) {
         return (ATermInt) t.getChildAt(i);
     }
 
     public ATerm implode(PureFactory factory, ATermAppl t) throws FatalError {
         if (t.getName().equals("Anno")) {
             return implode(factory, applAt(t, 0));
         } else if (t.getName().equals("Op")) {
             String ctr = stringAt(t, 0);
             ATermList children = (ATermList) t.getChildAt(1);
 
             AFun afun = factory.makeAFun(ctr, children.getLength(), false);
             ATermList kids = factory.makeList();
 
             for (int i = 0; i < children.getLength(); i++) {
                 kids = kids.append(implode(factory, (ATermAppl) children
                         .elementAt(i)));
             }
             return factory.makeApplList(afun, kids);
         } else if (t.getName().equals("Int")) {
             ATermAppl x = (ATermAppl) t.getChildAt(0);
             return factory.makeInt(new Integer(x.getName()));
         } else if (t.getName().equals("Str")) {
             ATermAppl x = (ATermAppl) t.getChildAt(0);
             return x;
         }
 
         throw new FatalError("Unknown build constituent '" + t.getName() + "'");
     }
 
     public static ATermList listAt(ATerm t, int i) {
         return (ATermList) ((ATermAppl) t).getChildAt(i);
     }
 
     public static ATermList listAt(ATermList t, int i) {
         return (ATermList) t.getChildAt(i);
     }
 
     public static ATerm termAt(ATermAppl t, int i) {
         return (ATerm) t.getChildAt(i);
     }
 
     public static ATermReal realAt(ATermList tvars, int i) {
         return (ATermReal) tvars.getChildAt(i);
     }
 
     public static ATerm termAt(ATermList tvars, int i) {
         return (ATerm) tvars.getChildAt(i);
     }
 
     public static boolean termType(ATermAppl p, String n) {
         return p.getName().equals(n);
     }
 
     public static ATermList consToList(PureFactory factory, ATermAppl cons) {
         if (cons.getName().equals("Nil"))
             return factory.makeList();
         ATermList tail = consToList(factory, Tools.applAt(cons, 1));
         ATerm head = Tools.termAt(cons, 0);
 
         return tail.insert(head);
     }
 
     public static ATermList consToListDeep(TermFactory factory, ATermAppl cons) {
         if (cons.getName().equals("Nil"))
             return factory.makeList();
             
        ATermList tail = consToListDeep(factory, Tools.applAt(cons, 1));
 
         ATerm head = Tools.termAt(cons, 0);
         if (Tools.isCons(head)) 
             head = consToListDeep(factory,(ATermAppl) head);
         
         return tail.insert(head);
     }
 
     private static boolean isCons(ATerm head) {
         return
             (head.getType() == ATerm.APPL &&
                 ((ATermAppl)head).getName().equals("Cons"));
     }
 }
