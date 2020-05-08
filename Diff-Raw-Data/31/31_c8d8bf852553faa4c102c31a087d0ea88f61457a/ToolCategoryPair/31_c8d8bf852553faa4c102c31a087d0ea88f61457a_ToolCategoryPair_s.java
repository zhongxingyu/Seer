 package org.hackystat.dailyprojectdata.resource.codeissue;
 
 /**
  * Represents a tool and one of its categories.
  * 
  * @author jsakuda
  */
 class ToolCategoryPair {
  private String tool;
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
    * Compares two <code>ToolCategoryPair</code>s for equality based on tool and category.
    * 
    * @param obj The <code>ToolCategoryPair</code> to compare with.
    * @return Returns true if the two <code>ToolCategoryPair</code>s are equal.
    */
   @Override
   public boolean equals(Object obj) {
     if (obj instanceof ToolCategoryPair) {
       ToolCategoryPair pair = (ToolCategoryPair) obj;
      String objCategory = pair.getCategory();
      if (tool.equals(pair.getTool())
          && ((category == null && objCategory == null) || category.equals(objCategory))) {
        return true;
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
