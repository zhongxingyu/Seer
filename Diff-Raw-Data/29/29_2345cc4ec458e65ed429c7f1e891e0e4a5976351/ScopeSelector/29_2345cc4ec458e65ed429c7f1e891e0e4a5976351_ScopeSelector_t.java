 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003 - 2004  Lars Khne
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //////////////////////////////////////////////////////////////////////////////
 package net.sf.clirr.core;
 
 import org.apache.bcel.classfile.JavaClass;
 import org.apache.bcel.classfile.AccessFlags;
 import org.apache.bcel.classfile.Attribute;
 import org.apache.bcel.classfile.InnerClasses;
 import org.apache.bcel.classfile.InnerClass;
 import org.apache.bcel.classfile.ConstantPool;
 import org.apache.bcel.classfile.Constant;
 import org.apache.bcel.classfile.ConstantUtf8;
 import org.apache.bcel.util.Repository;
 import org.apache.bcel.Constants;
 
 /**
  * Selects zero or more java scope values (public, protected, package,
  * private). An instance of this class is used when comparing two versions
  * of an application to indicate what items are of interest. When the target
  * audience is "normal" users of the applications, only changes to items
  * which have public or protected scope are relevant. When the audience is
  * developers of the applications, then package-scope and private-scope
  * changes are also of interest.
  *
  * @author Simon Kitching
  */
 public final class ScopeSelector
 {
     /**
     * Represents an "accessibility" level for a java class, field or method.
      * <p>
      * Change of access rights from lower to higher visibility rating is a
      * binary-compatible change. Change of access rights from higher to
      * lower is a binary-incompatible change.
      * <p>
      * Public > Protected > Package > Private
      */
     public static final class Scope
     {
         private int vis;
         private String desc;
         private String decl;
 
         private Scope(int vis, String desc, String decl)
         {
             this.vis = vis;
             this.desc = desc;
             this.decl = decl;
         }
 
         public boolean isMoreVisibleThan(Scope v)
         {
             return this.vis > v.vis;
         }
 
         public boolean isLessVisibleThan(Scope v)
         {
             return this.vis < v.vis;
         }
 
         public String getDesc()
         {
             return desc;
         }
 
         public String getDecl()
         {
             return decl;
         }
     }
 
     /** Object representing private scoped objects. */
     public static final Scope SCOPE_PRIVATE = new Scope(0, "private", "private");
 
     /** Object representing package scoped objects. */
     public static final Scope SCOPE_PACKAGE = new Scope(1, "package", "");
 
     /** Object representing protected scoped objects. */
     public static final Scope SCOPE_PROTECTED = new Scope(2, "protected", "protected");
 
     /** Object representing public scoped objects. */
     public static final Scope SCOPE_PUBLIC = new Scope(3, "public", "public");
 
     private Scope scope = SCOPE_PROTECTED;
 
     /**
      * Construct an instance which selects public and protected objects and
      * ignores package and private objects. The selectXXX methods can later
      * be used to adjust this default behaviour.
      */
     public ScopeSelector()
     {
     }
 
     /**
      * Construct an instance which selects public and protected objects and
      * ignores package and private objects. The selectXXX methods can later
      * be used to adjust this default behaviour.
      */
     public ScopeSelector(Scope scope)
     {
         this.scope = scope;
     }
 
     /** Specify which scope objects are of interest. */
     public void setScope(Scope scope)
     {
         this.scope = scope;
     }
 
     /**
      * Get the scope that this object is configured with.
      */
     public Scope getScope()
     {
         return scope;
     }
 
     /**
      * Return a string which indicates what scopes this object will consider
      * to be selected (ie relevant).
      */
     public String toString()
     {
         return scope.getDesc();
     }
 
     /**
      * Given a BCEL object, return true if this object's scope is one of the
      * values this object is configured to match.
      * <p>
      * Note that BCEL classes Field and Method inherit from the AccessFlags
      * base class and so are valid parameters to this method.
      * <p>
      * Note that despite JavaClass objects extending AccessFlags, the
     * methods which determine the accessibility of a JavaClass fail
      * miserably (bad bcel design) for nested classes. Therefore this
      * method <i>must not</i> be passed a JavaClass object as a parameter.
      * If this is done, a RuntimeException will be thrown to indicate a
      * programmer error.
      *
      * @param object is the object whose scope is to be checked.
      * @return true if the object is selected.
      */
     public boolean isSelected(AccessFlags object)
     {
         return !getScope(object).isLessVisibleThan(scope);
     }
 
     /**
      * Return true if objects of the specified scope, or more visible,
      * are selected by this selector.
      *
      * @param scope is the scope being checked
      * @return true if objects of the specified scope are selected.
      */
     public boolean isSelected(Scope scope)
     {
         return !scope.isLessVisibleThan(this.scope);
     }
 
     /**
      * Given a BCEL object, return the string which would be used in java
      * source code to declare that object's scope. <p>
      * <p>
      * Note that BCEL classes Field and Method inherit from the AccessFlags
      * base class and so are valid parameters to this method.
      * <p>
      * Note that despite JavaClass objects extending AccessFlags, the
     * methods which determine the accessibility of a JavaClass fail
      * miserably (bad bcel design) for nested classes. Therefore this
      * method <i>must not</i> be passed a JavaClass object as a parameter.
      * If this is done, a RuntimeException will be thrown to indicate a
      * programmer error.
      */
     public static String getScopeDecl(AccessFlags object)
     {
         return getScope(object).getDecl();
     }
 
     /**
      * Given an integer representing an object's access flags, return the
      * string which would be used in java source code to declare that object's
      * scope.
      * <p>
      * Note that this method gives the wrong results for JavaClass objects
      * which are nested classes. Use getClassScope(jclass).getDecl() instead.
      */
     public static String getScopeDecl(int accessFlags)
     {
         return getScope(accessFlags).getDecl();
     }
 
     /**
      * Given a BCEL object, return a string indicating whether the object is
      * public/protected/private/package scope. This is similar to
      * getScopeName, except for package-scope objects where this method
      * returns the string "package".
      * <p>
      * Note that BCEL classes Field and Method inherit from the AccessFlags
      * base class and so are valid parameters to this method.
      * <p>
      * Note that despite JavaClass objects extending AccessFlags, the
     * methods which determine the accessibility of a JavaClass fail
      * miserably (bad bcel design) for nested classes. Therefore this
      * method <i>must not</i> be passed a JavaClass object as a parameter.
      * If this is done, a RuntimeException will be thrown to indicate a
      * programmer error.
      */
     public static String getScopeDesc(AccessFlags object)
     {
         return getScope(object).getDesc();
     }
 
     /**
      * Given an integer representing the object's access flags, return a string
      * indicating whether the object is public/protected/private/package scope.
      * <p>
      * This is similar to getScopeName, except for package-scope objects where
      * this method returns the string "package".
      * <p>
      * Note that this method gives the wrong results for JavaClass objects
      * which are nested classes. Use getClassScope(jclass).getDesc() instead.
      */
     public static String getScopeDesc(int accessFlags)
     {
         return getScope(accessFlags).getDesc();
     }
 
     /**
      * Get a Scope object representing the accessibility of the specified
      * object.
      * <p>
      * Note that BCEL classes Field and Method inherit from the AccessFlags
      * base class and so are valid parameters to this method.
      * <p>
      * Note that despite JavaClass objects extending AccessFlags, the
     * methods which determine the accessibility of a JavaClass fail
      * miserably (bad bcel design) for nested classes. Therefore this
      * method <i>must not</i> be passed a JavaClass object as a parameter.
      * If this is done, a RuntimeException will be thrown to indicate a
      * programmer error. Use getClassScope instead.
      */
     public static Scope getScope(AccessFlags object)
     {
         if (object instanceof JavaClass)
         {
            throw new RuntimeException(
                "getScope called for JavaClass object. This is not permitted;"
                + " use method getClassScope for JavaClass objects.");
 
         }
 
         return getScope(object.getAccessFlags());
     }
 
     /**
      * Get a Scope object representing the accessibility of the specified
      * object.
      * <p>
      * Note that this method gives the wrong results for JavaClass objects
      * which are nested classes. Use getClassScope(jclass) instead.
      */
     public static Scope getScope(int accessFlags)
     {
         if ((accessFlags & Constants.ACC_PUBLIC) > 0)
         {
             return SCOPE_PUBLIC;
         }
 
         if ((accessFlags & Constants.ACC_PROTECTED) > 0)
         {
             return SCOPE_PROTECTED;
         }
 
         if ((accessFlags & Constants.ACC_PRIVATE) > 0)
         {
             return SCOPE_PRIVATE;
         }
 
         return SCOPE_PACKAGE;
     }
 
     /**
      * Java class files only ever contain scope specifiers of "public" or
      * "package". For top-level classes, this is expected: it is not possible
      * to have a top-level protected or private class.
      * <p>
      * However nested classes <i>can</i> be declared as protected or private. The
      * way to tell the real scope of a nested class is to ignore the scope in
      * the actual class file itself, and instead look in the "InnerClasses"
      * attribute stored on the enclosing class. This is exactly what the java
      * compiler does when compiling, and what the jvm does when verifying class
      * linkage at runtime.
      * <p>
      * For a "top-level" class, this method just returns the access scope for
      * the class itself. For nested classes, the enclosing class of the
      * specified class is retrieved and its InnerClasses attribute checked to
      * find the true scope for the specified class.
      * <p>
      * @throws CheckerException if the specified class is a nested class and
      * the enclosing class could not be found, or if the supposedly enclosing
      * class has no reference to the nested class. This exception is not
      * expected to occur in practice, unless a truly screwed-up jar file is
      * passed to clirr for inspection.
      */
     public static Scope getClassScope(JavaClass jclass) throws CheckerException
     {
         int dollarPos = jclass.getClassName().lastIndexOf('$');
         if (dollarPos == -1)
         {
             // not a nested class
             return getScope(jclass.getAccessFlags());
         }
 
         // ok this is a nested class
         String jclassName = jclass.getClassName();
         String enclosingClassName = jclassName.substring(0, dollarPos);
         String jclassNestedName = jclassName.substring(dollarPos + 1);
 
         Repository repo = jclass.getRepository();
         JavaClass enclosingClass = repo.findClass(enclosingClassName);
 
         if (enclosingClass == null)
         {
            throw new CheckerException(
                "Unable to locate enclosing class " + enclosingClassName
                + " for nested class " + jclassName);
         }
 
         ConstantPool pool = enclosingClass.getConstantPool();
         Attribute[] attrs = enclosingClass.getAttributes();
         for (int i = 0; i < attrs.length; ++i)
         {
             if (attrs[i] instanceof InnerClasses)
             {
                 InnerClasses ics = (InnerClasses) attrs[i];
                 InnerClass[] icarray = ics.getInnerClasses();
                 for (int j = 0; j < icarray.length; ++j)
                 {
                     InnerClass ic = icarray[j];
                     int nameIndex = ic.getInnerNameIndex();
 
                     Constant nameconst = pool.getConstant(nameIndex);
                     if (nameconst instanceof ConstantUtf8)
                     {
                         String classname = ((ConstantUtf8) nameconst).getBytes();
                         if (jclassNestedName.equals(classname))
                         {
                             return getScope(ic.getInnerAccessFlags());
                         }
                     }
                 }
             }
         }
 
         // weird; no nested class info found
        throw new CheckerException(
            "Unable to find information in class " + enclosingClass.getClassName()
            + " referring back to nested class " + jclassName);
 
     }
 }
 
