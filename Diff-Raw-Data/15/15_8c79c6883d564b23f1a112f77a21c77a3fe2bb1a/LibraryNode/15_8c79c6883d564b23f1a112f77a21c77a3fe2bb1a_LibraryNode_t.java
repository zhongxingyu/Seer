 /*
 * Copyright (c) 2010 The Jackson Laboratory
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.jax.drakegenetics.shareddata.client;
 
 /**
  *
  * @author gbeane
  */
 public class LibraryNode extends TreeNode<String> {
	private static final long serialVersionUID = 1L;  
     private boolean isDocument;
 
    /** This constructor is to support inherited Serializable interface.
     * 
     */
    public LibraryNode() {
    	super();
    }
    
     public LibraryNode(String name) {
         super(name);
         isDocument = false;
     }
 
     public void setIsDocument(boolean isDocument) {
         assert (this.getChildCount() == 0);
         this.isDocument = isDocument;
     }
 
     public boolean getIsDocument() {
         return isDocument;
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("{").append(data);
 
         if (isDocument) {
             sb.append("}");
         }
         else {
             sb.append(",[");
 
             for (int i = 0; i < children.size(); i++) {
                 if (i > 0) {
                     sb.append(",");
                 }
                 sb.append(children.get(i).getData());
             }
 
             sb.append("]}");
         }
         return sb.toString();
     }
 
 }
