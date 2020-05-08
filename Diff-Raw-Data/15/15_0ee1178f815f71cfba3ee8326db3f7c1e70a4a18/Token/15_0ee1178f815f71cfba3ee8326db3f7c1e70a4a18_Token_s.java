 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich(at)weltraumschaf(dot)de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  *
  */
 
 package de.weltraumschaf.jebnf.parser;
 
 import com.google.common.collect.Sets;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Represents a scanned EBNF token with its type, value and position in the source file.
  *
  * A token is a immutable value object.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class Token {
 
     /**
      * Maximum length of token value displayd in human readable string representation.
      *
      * @see #toString()
      */
     private static final int MAX_VALUE_LENGTH = 15;
 
     /**
      * All operator token types.
      */
     private static final Set<TokenType> OPERATOR_TYPES = Sets.newHashSet(
         TokenType.ASSIGN,
         TokenType.CHOICE,
         TokenType.END_OF_RULE,
         TokenType.RANGE,
         TokenType.L_BRACE,
         TokenType.L_BRACK,
         TokenType.L_PAREN,
         TokenType.R_BRACE,
         TokenType.R_BRACK,
         TokenType.R_PAREN
     );
 
     /**
      * One of the class constants.
      */
     private final TokenType type;
 
     /**
      * The literal string.
      */
     private final String value;
 
     /**
      * Start position in source.
      */
     private final Position position;
 
     /**
      * Initializes the immutable object.
      *
      * @param type     Type of token. One of the class constants.
      * @param value    The scanned token string.
      * @param position The start position of scanned token.
      */
     public Token(final TokenType type, final String value, final Position position) {
         this.type     = type;
         this.value    = value;
         this.position = position;
     }
 
     /**
      * Returns token type as string.
      *
      * @return The token type.
      */
     public final TokenType getType() {
         return type;
     }
 
     /**
      * Returns the scanned token string.
      *
      * @return The token value.
      */
     public final String getValue() {
         return getValue(false);
     }
 
     /**
      * Returns the scanned token string.
      *
      * @param unquote Whether to unquote a literal value.
      * @return         The token value.
      */
     public final String getValue(final boolean unquote) {
         if (unquote) {
             return StringHelper.unquoteString(value);
         }
 
         return value;
     }
 
     /**
      * Returns the start position of the token string in the source.
      *
      * @return The token position.
      */
     public final Position getPosition() {
         return getPosition(false);
     }
 
     /**
      * Returns the start position of the token string in the source.
      *
      * @param end If true the tokens end position is returned instead of the start.
      * @return     The token position.
      */
     public final Position getPosition(final boolean end) {
         if (end) {
             return new Position(
                 position.getLine(),
                 position.getColumn() + value.length(),
                 position.getFile()
             );
         }
 
         return position;
     }
 
     /**
      * Human readable string representation.
      *
      * Token values longer than 15 characters are shortened.
      *
      * @return String representation.
      */
     @Override
     public final String toString() {
         final StringBuilder str = new StringBuilder("<");
 
         if (null != value && value.length() > 0) {
             str.append('\'');
 
             if (value.length() > MAX_VALUE_LENGTH) {
                 str.append(value.substring(0, MAX_VALUE_LENGTH)).append("...");
             } else {
                 str.append(value);
             }
 
             str.append("', ");
         }
 
         str.append(type).append(", ").append(position).append('>');
         return str.toString();
     };
 
     /**
      * Returns whether the token is of an operator type or not.
      *
      * @return True or false.
      */
     public final boolean isOperator() {
         return OPERATOR_TYPES.contains(type);
     }
 
     /**
      * Checks if the token is of a particular type.
      *
      * @param checkedType The token type to check.
      * @return             True or false.
      */
     public final boolean isType(final TokenType checkedType) {
        return type == checkedType;
     }
 
     /**
      * Returns true if the token is of the type of one given {@link TokenType type} in the list.
      *
      * @param checkedTypes List of token types to check against.
      * @return Return true if it is of a passed type.
      */
     public final boolean isOfType(final List<TokenType> checkedTypes) {
         boolean isOfType = false;
 
         for (final TokenType checkedType : checkedTypes) {
             if (isType(checkedType)) {
                 isOfType = true;
                 break;
             }
         }
 
         return isOfType;
     }
 
     /**
      * Checks if token's value is not equals any of the passed in strings.
      *
      * @param others Array of strings to check against.
      * @return        True or false.
      */
     public final boolean isNotEquals(final String[] others) {
         return !isEquals(others);
     }
 
     /**
      * Checks if token's value is equals any of the passed in strings.
      *
      * @param others Array of strings to check against.
      * @return        True or false.
      */
     public final boolean isEquals(final String[] others) {
         for (int i = 0; i < others.length; ++i) {
             if (isEqual(others[i])) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Checks if token's value is not equals to passed in string.
      *
      * @param other String to check against.
      * @return       True or false.
      */
     public final boolean isNotEqual(final String other) {
         return !isEqual(other);
     }
 
     /**
      * Checks if token's value is equals to passed in string.
      *
      * @param other String to check against.
      * @return       True or false.
      */
     public final boolean isEqual(final String other) {
         return value.equals(other);
     }
 
 }
