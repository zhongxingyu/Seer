 /* Commented package visibility check (Checkstyle plugin)
     Copyright (C) 2012  Keita Kita
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package com.github.mikanbako.checkstyle.commentedpackagevisibilitycheck;
 
 import java.util.regex.Pattern;
 
 import org.apache.commons.beanutils.ConversionException;
 
 import com.puppycrawl.tools.checkstyle.api.DetailAST;
 import com.puppycrawl.tools.checkstyle.api.LineColumn;
 import com.puppycrawl.tools.checkstyle.api.Scope;
 import com.puppycrawl.tools.checkstyle.api.ScopeUtils;
 import com.puppycrawl.tools.checkstyle.api.TokenTypes;
 import com.puppycrawl.tools.checkstyle.checks.AbstractFormatCheck;
 
 /**
  * <p>Check that package visibility is commented instead of empty modifier.</p>
  *
  * <p>Example :</p>
  * <pre>
  *   &#x2f;&#x2a; package &#x2a;&#x2f; class PackageVisibilityClass {
  *      ...
  *   }
  * </pre>
  *
  * @author Keita Kita
  */
 public final class CommentedPackageVisibilityCheck extends AbstractFormatCheck
 {
     /**
      * Default format.
      */
     private static final String DEFAULT_FORMAT = "/\\* package \\*/";
 
     /**
      * Whether latter white space is required for package visibility comment.
      */
     private boolean mRequireLatterWhiteSpace = true;
 
     /**
      * Constructor.
      *
      * @throws ConversionException {@inheritDoc}
      */
     public CommentedPackageVisibilityCheck() throws ConversionException
     {
         super(DEFAULT_FORMAT);
     }
 
     @Override
     public int[] getDefaultTokens()
     {
         return new int[] {
             TokenTypes.CLASS_DEF,
             TokenTypes.ENUM_DEF,
             TokenTypes.INTERFACE_DEF,
             TokenTypes.CTOR_DEF,
             TokenTypes.VARIABLE_DEF,
             TokenTypes.METHOD_DEF,
         };
     }
 
     /**
      * Whether modifier represents package visibility.
      *
      * @param aDefinitionAST AST of definition.
      * @return True if the modifier represents package visibility,
      *  otherwise false.
      */
     private static boolean isModifierPackage(DetailAST aDefinitionAST)
     {
        // Modifier in interface or annotation is public.
        if (ScopeUtils.inInterfaceOrAnnotationBlock(aDefinitionAST)) {
            return false;
        }

         final DetailAST modifierAST =
                 aDefinitionAST.findFirstToken(TokenTypes.MODIFIERS);
         final Scope scope = ScopeUtils.getScopeFromMods(modifierAST);
 
         return scope == Scope.PACKAGE;
     }
 
     @Override
     public void visitToken(DetailAST aAST)
     {
         if (ScopeUtils.isLocalVariableDef(aAST)) {
             return;
         }
 
         if (isModifierPackage(aAST)) {
             checkPackageVisibilityCommentExists(aAST);
         }
         else {
             checkInappropiratePackageVisibilityComment(aAST);
         }
     }
 
     /**
      * Set whether latter white space is required for
      * package visibility comment.
      *
      * @param aRequired True if latter white space is required, otherwise false.
      */
     public void setRequireLatterWhiteSpace(boolean aRequired)
     {
         mRequireLatterWhiteSpace = aRequired;
     }
 
     /**
      * Get regexp including latter white space.
      *
      * @return regexp including latter white space.
      */
     private Pattern getRegexpWithLatterWhiteSpace()
     {
         return Pattern.compile(String.format("(?:%s)\\s+", getFormat()),
                 getRegexp().flags());
     }
 
     /**
      * Check whether comment representing package visibility exists.
      *
      * @param aPackageScopeDefinitionAST AST of definition.
      */
     private void checkPackageVisibilityCommentExists(
             DetailAST aPackageScopeDefinitionAST)
     {
         final Pattern commentPattern = mRequireLatterWhiteSpace
                 ? getRegexpWithLatterWhiteSpace()
                 : getRegexp();
 
         // Calculate searching range.
         final LineColumn startSearchingPosition =
                 getStartSearchingPosition(aPackageScopeDefinitionAST);
         final LineColumn endSearchingPosition =
                 getEndSearchingPosition(aPackageScopeDefinitionAST);
 
         if (!existsPackageVisibilityComment(
                 commentPattern, startSearchingPosition, endSearchingPosition))
         {
             // Log that there is not package visibility comment.
 
             final String ident = aPackageScopeDefinitionAST.findFirstToken(
                     TokenTypes.IDENT).getText();
 
             final String messageKey =
                 (mRequireLatterWhiteSpace
                         && existsPackageVisibilityComment(getRegexp(),
                                 startSearchingPosition, endSearchingPosition))
                     ? "packageVisibilityComment.noLetterWhiteSpace"
                     : "noPackageVisibilityComment";
             log(aPackageScopeDefinitionAST.getLineNo(), messageKey, ident);
         }
     }
 
     /**
      * Check whether there is inappropirate package visibility comment.
      *
      * For example, a method has "public" modifier and package visibility
      * comment.
      *
      * @param aNonPackageVisibilityDefinitionAST AST that represents definition
      *  that is not package visibility.
      */
     private void checkInappropiratePackageVisibilityComment(
             DetailAST aNonPackageVisibilityDefinitionAST)
     {
         // Calculate searching range.
         final LineColumn startSearchingPosition =
                 getStartSearchingPosition(aNonPackageVisibilityDefinitionAST);
         final LineColumn endSearchingPosition =
                 getEndSearchingPosition(aNonPackageVisibilityDefinitionAST);
 
         if (existsPackageVisibilityComment(
                 getRegexp(), startSearchingPosition, endSearchingPosition))
         {
             // Log that there are explicit modifier and
             // package visibility comment.
 
             final String ident = aNonPackageVisibilityDefinitionAST.
                     findFirstToken(TokenTypes.IDENT).getText();
 
             log(aNonPackageVisibilityDefinitionAST.getLineNo(),
                     "packageVisibilityComment.modifierExists", ident);
         }
     }
 
     /**
      * Get position to start searching.
      *
      * This position is one of the below.
      *
      * <ul>
      *  <li>A left curly brace just before the definition.</li>
      *  <li>An end of definition just before the definition.</li>
      *  <li>The head of the file.</li>
      * </ul>
      *
      * @param aDefinitionAST AST of definition.
      * @return Position to start searching.
      */
     private static LineColumn getStartSearchingPosition(
             DetailAST aDefinitionAST)
     {
         final DetailAST previousSiblingAST =
                 aDefinitionAST.getPreviousSibling();
 
         // There are not any IMPORT or PACKAGE_DEF before
         // CLASS_DEF of top level.
         if (previousSiblingAST == null) {
             return new LineColumn(1, 0);
         }
 
         DetailAST currentAST = previousSiblingAST;
         while (true) {
             final DetailAST lastChildAST = currentAST.getLastChild();
             if (lastChildAST == null) {
                 break;
             }
 
             currentAST = lastChildAST;
         }
 
         return new LineColumn(currentAST.getLineNo(), currentAST.getColumnNo());
     }
 
     /**
      * Get position to end searching.
      *
      * This position is at modifier of the definition.
      *
      * @param aDefinitionAST AST of definition.
      * @return Position to end searching.
      */
     private static LineColumn getEndSearchingPosition(DetailAST aDefinitionAST)
     {
         final DetailAST modifierAST =
                 aDefinitionAST.findFirstToken(TokenTypes.IDENT);
 
         return new LineColumn(
                 modifierAST.getLineNo(), modifierAST.getColumnNo());
     }
 
     /**
      * Get string of checking target.
      *
      * @param aStart Start position of checking target.
      * @param aEnd End position of checking target.
      * @return String of checking target.
      */
     private String getCheckingTargetString(LineColumn aStart, LineColumn aEnd)
     {
         final StringBuilder targetStringBuilder = new StringBuilder();
         final String[] targetLines = getLines();
 
         // Combine lines.
         for (int lineNumber = aStart.getLine(); lineNumber <= aEnd.getLine();
                 lineNumber++)
         {
             final int lineIndex = lineNumber - 1;
 
             targetStringBuilder.append(targetLines[lineIndex]);
             if (lineNumber < aEnd.getLine()) {
                 targetStringBuilder.append('\n');
             }
         }
 
         // Delete redundant characters at last line.
         final int endDeletingCount =
                 (targetLines[aEnd.getLine() - 1].length() - 1)
                     - aEnd.getColumn();
         targetStringBuilder.delete(
                 targetStringBuilder.length() - endDeletingCount,
                 targetStringBuilder.length()).
             delete(0, aStart.getColumn());
 
         return targetStringBuilder.toString();
     }
 
     /**
      * Whether package visibility comment is exists.
      *
      * @param aPattern Pattern of package visibility comment.
      * @param aStart Start position of checking target.
      * @param aEnd End position of checking target.
      * @return True if package visibility comment exists, otherwise false.
      */
     private boolean existsPackageVisibilityComment(
             Pattern aPattern, LineColumn aStart, LineColumn aEnd)
     {
         final String target = getCheckingTargetString(aStart, aEnd);
 
         return aPattern.matcher(target).find();
     }
 }
