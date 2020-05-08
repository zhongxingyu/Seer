 /**
  * Copyright (C) 2011 Shaun Johnson, LMXM LLC
  * 
  * This file is part of Universal Task Executer.
  * 
  * Universal Task Executer is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * Universal Task Executer is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Universal Task Executer. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.lmxm.ute.beans;
 
 import com.google.common.base.Objects;
 import org.apache.commons.lang3.StringUtils;
 
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 /**
  * A Find/Replace pattern represents a pair of values used to find and replace text. The find value represents a
  * regular expression value stored as a String. The value is not stored as an instance of Pattern as the user must be
  * able to modify the value to make it a valid regular expression. The replace string represents the value that is
  * used to replace all matching values.
  */
 public final class FindReplacePattern implements DomainBean {
 
 	/** The Constant serialVersionUID. */
 	private static final long serialVersionUID = -124788145989938819L;
 
 	/** The regular expression used to find a match, as a String. */
 	private String find;
 
 	/** The replacement text. */
 	private String replace;
 
 	@Override
 	public boolean equals(final Object object) {
         if (object instanceof FindReplacePattern) {
             final FindReplacePattern that = (FindReplacePattern) object;
 
             return Objects.equal(find, that.find) && Objects.equal(replace, that.replace);
         }
         else {
             return false;
         }
 	}
 
 	/**
 	 * Gets the regular expression used to find a match, as a String.
 	 * 
 	 * @return the find expression
 	 */
 	public String getFind() {
 		return find;
 	}
 
 	/**
 	 * Gets the replacement text.
 	 * 
 	 * @return the replacement text
 	 */
 	public String getReplace() {
 		return replace;
 	}
 
 	@Override
 	public int hashCode() {
 		return Objects.hashCode(find, replace);
 	}
 
     /**
      * Checks if the domain object is empty.
      *
      * @return true, if is empty
      */
 	public boolean isEmpty() {
		return StringUtils.isBlank(find) && StringUtils.isBlank(replace);
 	}
 
 	/**
 	 * Checks if the find regular expression is a valid regular expression.
 	 * 
 	 * @return true, if is find is a valid regular expression, otherwise false
 	 */
 	public boolean isValid() {
 		try {
 			Pattern.compile(find);
 			return true;
 		}
 		catch (final PatternSyntaxException e) {
 			return false;
 		}
 	}
 
     /**
      * Removes all empty child objects.
      */
 	public void removeEmptyObjects() {
 		// No children objects
 	}
 
 	/**
      * Sets the regular expression used to find a match, as a String.
 	 * 
 	 * @param find the new find expression
 	 */
 	public void setFind(final String find) {
 		this.find = find;
 	}
 
 	/**
 	 * Sets the replacement text.
 	 * 
 	 * @param replace the new replacement text
 	 */
 	public void setReplace(final String replace) {
 		this.replace = replace;
 	}
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this).add("find", find).add("replace", replace).toString();
     }
 }
