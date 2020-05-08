 /*
  * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
  *
 * This software is distributable under the BSD license. See the terms of the BSD license
 * in the documentation provided with this software.
  */
 package de.hunsicker.jalopy.language;
 
 import java.lang.reflect.Modifier;
 import java.util.Comparator;
 
 import de.hunsicker.antlr.collections.AST;
 
 
 /**
  * Comparator for certain AST nodes (namely CLASS_DEF, INTERFACE_DEF, METHOD_DEF,
  * CTOR_DEF, INSTANCE_INT and STATIC_INIT). The nodes will be sorted according to their
  * access modifiers first, then by type and - if these are equal - lexicographically.
  *
  * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
  * @version $Revision$
  */
 final class NodeComparator
     implements Comparator
 {
     //~ Static variables/initializers ----------------------------------------------------
 
     static final int PUBLIC_STATIC = Modifier.PUBLIC | Modifier.STATIC;
 
     //~ Instance variables ---------------------------------------------------------------
 
     /**
      * Indicates whether sorting methods should take Java Bean convention names into
      * account.
      */
     private boolean _beanSorting = true;
 
     //~ Constructors ---------------------------------------------------------------------
 
     /**
      * Creates a new NodeComparator object.
      */
     public NodeComparator()
     {
     }
 
     //~ Methods --------------------------------------------------------------------------
 
     /**
      * Sets whether the sorting of method names should take the Java Bean naming
      * convention into account (for METHOD_DEF nodes).
      *
      * @param sort if <code>true</code> sorting will take the Java Bean naming convention
      *        into account.
      */
     public void setBeanSorting(boolean sort)
     {
         _beanSorting = sort;
     }
 
 
     /**
      * Compares its two arguments for order. Returns a negative integer, zero, or a
      * positive integer as the first argument is less than, equal to, or greater than
      * the second.
      * 
      * <p>
      * Delegates to {@link #compareNodes} to perform the comparison.
      * </p>
      *
      * @param o1 the first node.
      * @param o2 the second node
      *
      * @return a negative integer, zero, or a positive integer as the first argument is
      *         less than, equal to, or greater than the second.
      */
     public int compare(
         Object o1,
         Object o2)
     {
         if (o1 == o2)
         {
             return 0;
         }
 
         return compareNodes((AST) o1, (AST) o2);
     }
 
 
     /**
      * Indicates whether some other object is <code>equal to</code> this one.
      *
      * @param o the reference object with which to compare.
      *
      * @return <code>true</code> if this object is the same as the obj argument.
      */
     public boolean equals(Object o)
     {
         return false;
     }
 
 
     /**
      * Determines whether the given (method) name makes up a Java bean method to test for
      * a boolean condition.
      *
      * @param name method name.
      *
      * @return <code>true</code> if the given name denotes a Java bean tester method.
      */
     protected boolean isBooleanTester(String name)
     {
         if (name.length() > 2)
         {
             if (
                 (name.startsWith("is") && Character.isUpperCase(name.charAt(2)))
                 || (name.startsWith("has") && Character.isUpperCase(name.charAt(3)))
                 || (name.startsWith("should") && Character.isUpperCase(name.charAt(6))))
             {
                 return true;
             }
         }
 
         return false;
     }
 
 
     /**
      * Determines if the given (method) name makes up for Java bean method getter.
      *
      * @param name method name.
      *
      * @return <code>true</code> if the given name denotes a Java bean getter method.
      */
     protected boolean isGetter(String name)
     {
         if (name.length() > 3)
         {
             if (name.startsWith("get") && Character.isUpperCase(name.charAt(3)))
             {
                 return true;
             }
         }
 
         return false;
     }
 
 
     /**
      * Returns the number of parameters of the given node.
      *
      * @param node PARAMETERS node.
      *
      * @return the parameters count.
      */
     protected int getParamCount(AST node)
     {
         AST params = NodeHelper.getFirstChild(node, JavaTokenTypes.PARAMETERS);
 
         if (params == null)
         {
             return 0;
         }
 
         int result = 0;
 
         for (AST child = node.getFirstChild(); child != null;
             child = child.getNextSibling())
         {
             result++;
         }
 
         return result;
     }
 
 
     /**
      * Determines if the given (method) name makes up for Java bean method setter.
      *
      * @param name method name.
      *
      * @return <code>true</code> if the given name denotes a Java bean setter method.
      */
     protected boolean isSetter(String name)
     {
         if (name.length() > 3)
         {
             if (name.startsWith("set") && Character.isUpperCase(name.charAt(3)))
             {
                 return true;
             }
         }
 
         return false;
     }
 
 
     /**
      * DOCUMENT ME!
      *
      * @param node1 DOCUMENT ME!
      * @param node2 DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     protected int compareBeanNames(
         AST node1,
         AST node2)
     {
         String name1 = NodeHelper.getFirstChild(node1, JavaTokenTypes.IDENT).getText();
         String name2 = NodeHelper.getFirstChild(node2, JavaTokenTypes.IDENT).getText();
         MethodType type1 = MethodType.valueOf(name1);
         MethodType type2 = MethodType.valueOf(name2);
 
         if (type1 == type2)
         {
             return name1.compareTo(name2);
         }
 
         String base1 = stripPrefix(name1);
         String base2 = stripPrefix(name2);
         int result = base1.compareTo(base2);
 
         if (result == 0) // both are Java Bean methods
         {
             result = type1.compareTo(type2);
         }
         else
         {
             if (!type1.isBean())
             {
                 if (!type2.isBean())
                 {
                     result = name1.compareTo(name2);
                 }
 
                 result = type1.compareTo(type2);
             }
 
             if (!type2.isBean())
             {
                 result = type1.compareTo(type2);
             }
         }
 
         return result;
     }
 
 
     /**
      * Compares the two class declaration nodes.
      *
      * @param node1 the first CLASS_DEF node.
      * @param node2 the second  CLASS_DEF node.
      *
      * @return a negative integer, zero, or a positive integer as the first node is less
      *         than, equal to, or greater than the second node.
      */
     protected int compareClass(
         AST node1,
         AST node2)
     {
         int mod1 = JavaNodeModifier.valueOf(node1);
         int mod2 = JavaNodeModifier.valueOf(node2);
         int result = compareModifiers(mod1, mod2);
 
         if (result != 0)
         {
             return result;
         }
 
         return compareNames(node1, node2);
     }
 
 
     /**
      * Compares the two method declaration nodes.
      *
      * @param node1 the first METHOD_DEF node.
      * @param node2 the second METHOD_DEF node.
      *
      * @return a negative integer, zero, or a positive integer as the first node is less
      *         than, equal to, or greater than the second.
      */
     protected int compareMethod(
         AST node1,
         AST node2)
     {
         int mod1 = JavaNodeModifier.valueOf(node1.getFirstChild());
         int mod2 = JavaNodeModifier.valueOf(node2.getFirstChild());
         int result = compareModifiers(mod1, mod2);
 
         if (result != 0)
         {
             return result;
         }
 
         if (_beanSorting)
         {
             result = compareBeanNames(node1, node2);
         }
         else
         {
             result = compareNames(node1, node2);
         }
 
         if (result != 0)
         {
             return result;
         }
 
         return compareParamCount(node1, node2);
     }
 
 
     /**
      * Compares the two nodes.
      *
      * @param node1 the first node.
      * @param node2 the second node.
      *
      * @return a negative integer, zero, or a positive integer as the first node is less
      *         than, equal to, or greater than the second.
      *
      * @throws IllegalArgumentException if either one of the given nodes is not of type
      *         INSTANCE_INIT, STATIC_INIT, CLASS_DEF, INTERFACE_DEF, CTOR_DEF or
      *         METHOD_DEF.
      */
     protected int compareNodes(
         AST node1,
         AST node2)
     {
         int type1 = node1.getType();
         int type2 = node2.getType();
 
         if (type1 == type2)
         {
             switch (type1)
             {
                 case JavaTokenTypes.METHOD_DEF :
                 case JavaTokenTypes.CTOR_DEF :
                     return compareMethod(node1, node2);
 
                 case JavaTokenTypes.VARIABLE_DEF :
                     return compareVariable(node1, node2);
 
                 case JavaTokenTypes.CLASS_DEF :
                 case JavaTokenTypes.INTERFACE_DEF :
                     return compareClass(node1, node2);
 
                 // nothing to compare here
                 case JavaTokenTypes.STATIC_INIT :
                 case JavaTokenTypes.INSTANCE_INIT :
                     return 0;
 
                 default :
                     throw new IllegalArgumentException(
                         "Heck. I don't know about this type -- " + type1);
             }
         }
         else
         {
             switch (type1)
             {
                 case JavaTokenTypes.METHOD_DEF :
 
                     switch (type2)
                     {
                         case JavaTokenTypes.VARIABLE_DEF :
                         case JavaTokenTypes.CTOR_DEF :
                         case JavaTokenTypes.STATIC_INIT :
                         case JavaTokenTypes.INSTANCE_INIT :
                             return 1;
 
                         default :
                             return -1;
                     }
 
                 // fall through
                 case JavaTokenTypes.VARIABLE_DEF :
 
                     switch (type2)
                     {
                         case JavaTokenTypes.STATIC_INIT :
                         case JavaTokenTypes.INSTANCE_INIT :
                             return 1;
 
                         default :
                             return -1;
                     }
 
                 case JavaTokenTypes.CTOR_DEF :
 
                     switch (type2)
                     {
                         case JavaTokenTypes.VARIABLE_DEF :
                         case JavaTokenTypes.STATIC_INIT :
                         case JavaTokenTypes.INSTANCE_INIT :
                             return 1;
 
                         default :
                             return -1;
                     }
 
                 // fall through
                 case JavaTokenTypes.CLASS_DEF :
                     return 1;
 
                 case JavaTokenTypes.INTERFACE_DEF :
 
                     switch (type2)
                     {
                         case JavaTokenTypes.METHOD_DEF :
                         case JavaTokenTypes.VARIABLE_DEF :
                         case JavaTokenTypes.CTOR_DEF :
                         case JavaTokenTypes.STATIC_INIT :
                         case JavaTokenTypes.INSTANCE_INIT :
                             return 1;
 
                         default :
                             return -1;
                     }
 
                 // fall through
                 case JavaTokenTypes.STATIC_INIT :
 
                     switch (type2)
                     {
                         default :
                             return -1;
                     }
 
                 // fall through
                 case JavaTokenTypes.INSTANCE_INIT :
 
                     switch (type2)
                     {
                         case JavaTokenTypes.VARIABLE_DEF :
                         case JavaTokenTypes.STATIC_INIT :
                             return 1;
 
                         default :
                             return -1;
                     }
 
                 // fall through
                 default :
                     throw new IllegalArgumentException(
                         "invalid node type given -- " + type1);
             }
         }
     }
 
 
     /**
      * Compares the two nodes according to their parameter counts.
      *
      * @param node1 the first node.
      * @param node2 the second node.
      *
      * @return a negative integer, zero, or a positive integer as the first node's
      *         parameter count is less than, equal to, or greater than parameter count
      *         of the second node.
      */
     protected int compareParamCount(
         AST node1,
         AST node2)
     {
         int count1 = getParamCount(node1);
         int count2 = getParamCount(node2);
 
         if (count1 > count2)
         {
             return 1;
         }
         else if (count1 < count2)
         {
             return -1;
         }
 
         return 0;
     }
 
 
     /**
      * Compares the two variable declaration nodes.
      *
      * @param node1 the first VARIABLE_DEF node.
      * @param node2 the second VARIABLE_DEF node.
      *
      * @return a negative integer, zero, or a positive integer as the first node is less
      *         than, equal to, or greater than the second node.
      */
     protected int compareVariable(
         AST node1,
         AST node2)
     {
         int mod1 = JavaNodeModifier.valueOf(node1);
         int mod2 = JavaNodeModifier.valueOf(node2);
         int result = compareModifiers(mod1, mod2);
 
         if (result != 0)
         {
             return result;
         }
 
         result = compareTypes(node1, node2);
 
         if (result != 0)
         {
             return result;
         }
 
         return compareNames(node1, node2);
     }
 
 
     /**
      * Strips the 'set', 'get', or 'is' prefix from method names that follow the Java
      * Bean conventions.
      *
      * @param name name of method to parse.
      *
      * @return return normalized method mame. If the method names denotes no Java Bean
      *         like method, the original method name is returned.
      */
     protected static String stripPrefix(String name)
     {
         int length = name.length();
 
         if (length > 3)
         {
             if (
                 (name.startsWith("get") || name.startsWith("set"))
                 && (Character.isUpperCase(name.charAt(3))))
             {
                 return name.substring(3, length);
             }
             else if (name.startsWith("is") && Character.isUpperCase(name.charAt(2)))
             {
                 return name.substring(2, length);
             }
             else
             {
                 return name;
             }
         }
         else if (
             (length == 3)
             && (name.startsWith("is") && Character.isUpperCase(name.charAt(2))))
         {
             return name.substring(2, length);
         }
 
         return name;
     }
 
 
     /**
      * Compares the two modifier masks.
      *
      * @param mod1 the first modifier mask.
      * @param mod2 the second modifier mask.
      *
      * @return a negative integer, zero, or a positive integer as the first mask is less
      *         than, equal to, or greater than the mask.
      */
     static int compareModifiers(
         int mod1,
         int mod2)
     {
        // public
         if (Modifier.isPublic(mod1))
         {
             if (!Modifier.isPublic(mod2))
             {
                 return -1;
             }
         }
 
        // protected
         if (Modifier.isProtected(mod1))
         {
             if (Modifier.isPublic(mod2))
             {
                 return 1;
             }
             else if (Modifier.isPrivate(mod2))
             {
                 return -1;
             }
             else if (!Modifier.isProtected(mod2))
             {
                 return -1;
             }
         }
 
        // private
         if (Modifier.isPrivate(mod1))
         {
             if (!Modifier.isPrivate(mod2))
             {
                 return 1;
             }
         }
 
        // package protected (friendly)
         if (JavaNodeModifier.isFriendly(mod1))
         {
             if (Modifier.isPrivate(mod2))
             {
                 return -1;
             }
 
             if (Modifier.isPublic(mod2) || Modifier.isProtected(mod2))
             {
                 return 1;
             }
         }
 
         if (Modifier.isAbstract(mod1))
         {
             if (!Modifier.isAbstract(mod2))
             {
                 return -1;
             }
         }
 
         if (Modifier.isStatic(mod1))
         {
            if (Modifier.isAbstract(mod2))
            {
                return 1;
            }

             if (!Modifier.isStatic(mod2))
             {
                 return -1;
             }
         }
 
         if (Modifier.isFinal(mod1))
         {
            if (Modifier.isAbstract(mod2) || Modifier.isStatic(mod2))
            {
                return 1;
            }

             if (!Modifier.isFinal(mod2))
             {
                 return -1;
             }
         }
 
         if (Modifier.isTransient(mod1))
         {
            if (
                Modifier.isAbstract(mod2) || Modifier.isStatic(mod2)
                || Modifier.isFinal(mod2))
            {
                return 1;
            }

             if (!Modifier.isTransient(mod2))
             {
                 return -1;
             }
         }
 
         /**
          * @todo implement the rest
          */
         return 0;
     }
 
 
     /**
      * Compares the names of the two nodes.
      *
      * @param node1 the first node.
      * @param node2 the second node.
      *
      * @return a negative integer, zero, or a positive integer as the first node's name
      *         is less than, equal to, or greater than the second node's name.
      */
     static int compareNames(
         AST node1,
         AST node2)
     {
         String ident1 = NodeHelper.getFirstChild(node1, JavaTokenTypes.IDENT).getText();
         String ident2 = NodeHelper.getFirstChild(node2, JavaTokenTypes.IDENT).getText();
 
         return ident1.compareTo(ident2);
     }
 
 
     /**
      * Compares the two nodes by their type names.
      *
      * @param node1 the first node.
      * @param node2 the second node.
      *
      * @return a negative integer, zero, or a positive integer as the first node's name
      *         is less than, equal to, or greater than the second node's name.
      *
      * @since 1.0b8
      */
     static int compareTypes(
         AST node1,
         AST node2)
     {
         String ident1 = getType(node1);
         String ident2 = getType(node2);
 
         return ident1.compareTo(ident2);
     }
 
 
     /**
      * Returns the type name of the given node.
      *
      * @param node a declaration node.
      *
      * @return the type name.
      *
      * @since 1.0b8
      */
     private static String getType(AST node)
     {
         AST type = NodeHelper.getFirstChild(node, JavaTokenTypes.TYPE);
         String ident = type.getFirstChild().getText();
         int offset = ident.lastIndexOf('.');
 
         if (offset == -1)
         {
             return ident;
         }
         else
         {
             return ident.substring(offset + 1);
         }
     }
 }
