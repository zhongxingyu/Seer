 package org.hackystat.dailyprojectdata.resource.codeissue;
 
 /**
  * Represents a tool and one of its categories.
  * 
  * @author jsakuda
  */
 class ToolCategoryPair {
  private String tool = "";
   private String category;
 
   /**
    * Creates a new tool/category pair.
    * 
    * @param tool The tool that contains the given category.
    * @param category The category for the tool or null if there is no category.
    */
   ToolCategoryPair(String tool, String category) {
     if (tool == null) {
       throw new IllegalArgumentException("Tool cannot be null.");
     }
 
     this.tool = tool;
     this.category = category;
   }
 
   /**
    * Gets the tool of the pair.
    * 
    * @return Returns the tool.
    */
   String getTool() {
     return this.tool;
   }
 
   /**
    * Gets the category of the pair.
    * 
    * @return Returns the category or null if there isn't a category.
    */
   String getCategory() {
     return this.category;
   }
 
   /**
   * Determines if the pair passed in has a category equivalent to the category of this object.
   * 
   * @param pair The <code>ToolCategoryPair</code> whose category should be compared to this
   *          object's category.
   * @return Returns true if they are equivalent.
   */
  private boolean hasEquivalentCategory(ToolCategoryPair pair) {
    if (this.category == null) {
      if (pair.getCategory() == null) {
        return true;
      }
      // else just return false
    }
    else {
      // category is not null so just use .equals
      return this.category.equals(pair.getCategory());
    }
    return false;
  }

  /**
    * Compares two <code>ToolCategoryPair</code>s for equality based on tool and category.
    * 
    * @param obj The <code>ToolCategoryPair</code> to compare with.
    * @return Returns true if the two <code>ToolCategoryPair</code>s are equal.
    */
   @Override
   public boolean equals(Object obj) {
     if (obj instanceof ToolCategoryPair) {
       ToolCategoryPair pair = (ToolCategoryPair) obj;
      if (this.tool.equals(pair.getTool())) {
        return this.hasEquivalentCategory(pair);
       }
     }
     return false;
   }
 
   /**
    * Gets the hash code.
    * 
    * @return The hash code.
    */
   @Override
   public int hashCode() {
     int result = 17; // start off with constant
     result = 37 * result + tool.hashCode();
 
     if (category != null) {
       result = 37 * result + category.hashCode();
     }
     return result;
   }
 }
