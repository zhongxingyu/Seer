 /*
  * ArticleGroup.java
  * 
  * Copyright (C) 2009 Nicola Roberto Viganò
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gestionecassa;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.List;
 import java.util.Vector;
 
 /**
  * Group of Articles, not synchronized.
  *
  * @author ben
  */
 public class ArticleGroup implements Serializable {
 
     /**
      * The Id of the group
      */
     int idGroup;
 
     /**
      * Name of the group
      */
     String groupName;
 
     /**
      * Tells if it's enabled
      */
     boolean enabled;
 
     /**
      * List of the articles sold in this group
      */
     List<Article> list;
 
     /**
      * Default constructor for the list of articles (but explicit for the name)
      * 
      * @param grn Name of this group
      */
     public ArticleGroup(int id, String grn) {
        this(id, grn, new Vector());
     }
 
     /**
      * Explicit constructor
      *
      * @param grn Name of this group
      * @param list List of articles of this group
      */
     public ArticleGroup(int id, String grn, Collection<Article> list) {
         this(id, grn, true, list);
     }
 
     /**
      * Explicit constructor
      *
      * @param grn Name of this group
      * @param en If group is enabled/disabled
      * @param list List of articles of this group
      */
     public ArticleGroup(int id, String grn, boolean en, Collection<Article> list) {
         this.groupName = new String(grn);
         this.list = new Vector<Article>(list);
         this.enabled = en;
         this.idGroup = id;
     }
 
     /**
      * Gets the name of this group
      *
      * @return
      */
     public String getGroupName() {
         return groupName;
     }
 
     /**
      * Gets the id of the group
      * 
      * @return
      */
     public int getIdGroup() {
         return idGroup;
     }
 
     /**
      * Tells if this group is enabled
      * 
      * @return
      */
     public boolean isEnabled() {
         return enabled;
     }
 
     /**
      * Gets the list of articles in this group
      *
      * @return
      */
     public List<Article> getList() {
         return list;
     }
 
     /**
      * Sets the new name of this group
      *
      * @param groupName
      */
     void setGroupName(String groupName) {
         this.groupName = new String(groupName);
     }
 
 
     /**
      * Adds an article
      *
      * @param article The article to add.
      */
     void addArticle(Article article) {
         list.add(article);
     }
 
     /**
      * Enables/disables a specified article
      *
      * @param pos Position of the article
      * @param enable Enable/disable
      */
     Article enableArticle(int pos, boolean enable) {
         return list.get(pos).setEnabled(enable);
     }
 
     /**
      * Enables/disables a specified article
      *
      * @param art The article to modify
      * @param enable Enable/disable
      */
     Article enableArticle(Article art, boolean enable) {
         for (Article article : list) {
             if (article.equals(art)) {
                 article.setEnabled(enable);
                 return article;
             }
         }
         return null;
     }
 
     /**
      * Moves an article
      *
      * @param oldPos Old position
      * @param newPos New position
      */
     public Article moveArticleAt(int oldPos, int newPos) {
         Article temp = list.remove(oldPos);
         list.add(newPos,temp);
         return temp;
     }
 
     /**
      * Moves the specified article
      *
      * @param a Article to move
      * @param newPos New position
      */
     public Article moveArticleAt(Article a, int newPos) {
         for (Article article : list) {
             if (article.equals(a)) {
                 list.remove(article);
                 list.add(newPos, article);
                 return article;
             }
         }
         return null;
     }
 
 
     /**
      * Similar to toString but leaves it fully functional
      *
      * @return a written description of the list
      */
     public String getPrintableFormat() {
         String output = new String("Articoli del gruppo " + groupName + ":\n");
         for (int i = 0; i < list.size(); i++) {
             Article article = list.get(i);
             output += String.format("%2d %s\n",i,article.getPrintableFormat());
         }
         return output;
     }
 
 }
