 package com.github.mikanbako.checkstyle.commentedpackagevisibilitycheck;
 
 class CommentedPackageVisibilityCheckTestInput
 {
     class NoCommentedPackageVisibilityClass
     {
 
     }
 
     /* package */ class CommentedPackageVisibilityClass
     {
 
     }
 
     /* package */ protected class InvalidCommentedPackageVisibilityClass
     {
 
     }
 
     interface NoCommentedPackageVisibilityInterface
     {

     }
 
     /* package */ interface CommentedPackageVisibilityInterface
     {
 
     }
 
     /* package */ public interface InvalidCommentedPackageVisibilityInterface
     {
 
     }
 
     enum NoCommentedPackageVisibilityEnum
     {
 
     }
 
     /* package */ enum CommentedPackageVisibilityEnum
     {
 
     }
 
     /* package */ protected enum InvalidCommentedPackageVisibilityEnum
     {
 
     }
 
     int noCommentedPackageVisibilityField;
 
     /* package */ int commentedPackageVisibilityField;
 
     protected/* package */int invalidCommentedPackageVisibilityField;
 
     // No commented package visibility constructor.
     CommentedPackageVisibilityCheckTestInput()
     {
 
     }
 
     // Commented package visibility constructor.
     /* package */ CommentedPackageVisibilityCheckTestInput(int parameter)
     {
 
     }
 
     // Invalid commented package visibility constructor.
     /* package */ protected CommentedPackageVisibilityCheckTestInput(
             int parameter1, int parmeter2)
     {
 
     }
 
     void noCommentedPackageVisibilityMethod()
     {
 
     }
 
     /* package */ void commentedPackageVisibilityMethod()
     {
 
     }
 
     /* package */void commentdPackageVisibilityWithoutWhitespaceMethod()
     {
 
     }
 
     // package
     void commentedPackageVisibilityWithOtherFormatMethod()
     {
 
     }
 
     /* package */ public void invalidCommentedPackageVisibilityMethod()
     {
 
     }
 
     /* package */public void invalidCommentedPackageVisibilityWithoutSpaceMethod()
     {
 
     }
 
     // package
     public void invalidCommentedPackageVisibilityWithOtherFormatMethod()
     {
 
     }
 
     public void localVariableContainedMethod() {
         int localVariable;
     }
 
     public void anonimousClassContainsMethod() {
         new Object() {
 
         };
     }
 }
