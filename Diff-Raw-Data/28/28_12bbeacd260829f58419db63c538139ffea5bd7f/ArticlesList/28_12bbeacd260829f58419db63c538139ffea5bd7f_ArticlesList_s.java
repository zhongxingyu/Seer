 /*
  * ArticlesList.java
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
 
 import gestionecassa.exceptions.DuplicateArticleException;
 import gestionecassa.exceptions.DuplicateGroupException;
 import gestionecassa.exceptions.NotExistingGroupException;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.ArrayList;
 
 /**
  * Manages the list of groups of articles.
  *
  * @author ben
  */
 public class ArticlesList implements Serializable {
 
     /**
      * List of the articles sold.
      */
     private List<ArticleGroup> groups;
 
     /**
      * Support map to retrieve cardinal number of the given group name
      */
     private Map<String, Integer> grNum;
 
     /**
      * Support map used to retrieve the article correpsonding to the given name
      */
     private Map<String, Article> articles;
 
     /**
      * The signature of this list
      */
     private List<Integer> signature;
 
     /**
      * Default constructor
      */
     public ArticlesList() {
         this(new ArrayList<ArticleGroup>());
         generateSignature();
     }
 
     /**
      * Explicit constructor
      *
      * @param list List of groups
      */
     public ArticlesList(Collection<ArticleGroup> list) {
         this.groups = new ArrayList<ArticleGroup>(list);
         generateArtMap();
         generateNumMap();
         generateSignature();
     }
 
     /**
      * private constructor for enabled list creation
      * @param list list of Groups of Articles
      * @param listSign the signature of the original list
      */
     private ArticlesList(Collection<ArticleGroup> list, List<Integer> listSign) {
         this.groups = new ArrayList<ArticleGroup>(list);
         this.signature = listSign;
         generateArtMap();
         generateNumMap();
     }
 
     /**
      * Copy constructor
      * 
      * @param list The list to copy from
      */
     public ArticlesList(ArticlesList list) {
         this.groups = new ArrayList<ArticleGroup>(list.groups);
         this.grNum = new TreeMap<String, Integer>(list.grNum);
         this.articles = new TreeMap<String, Article>(list.articles);
         this.signature = list.signature;
     }
 
     /**
      * Support functions that regenerates the Map <code>{@link grNum}</code>
      */
     private void generateNumMap() {
         grNum = new TreeMap<String, Integer>();
         for (int i = 0; i < groups.size(); i++) {
             grNum.put(groups.get(i).getName(), i);
         }
     }
 
     /**
      * Support functions that regenerates the Map <code>{@link articles}</code>
      */
     private void generateArtMap() {
         articles = new TreeMap<String, Article>();
         for (ArticleGroup artGroup : groups) {
             for (Article article : artGroup.getList()) {
                 articles.put(article.getName(), article);
             }
         }
     }
 
     /**
      * Similar to toString but leaves it fully functional
      *
      * @return a written description of the list
      */
     public String getPrintableFormat() {
         String output = "Lista degli articoli venduti (per gruppi):\n";
         for (int i = 0; i < groups.size(); i++) {
             ArticleGroup artgr = groups.get(i);
             output += String.format("%2d %s\n",i,artgr.getPrintableFormat());
         }
         return output;
     }
 
     /**
      * Adds a new group to the list
      *
      * @param group New group to add
      * @throws DuplicateGroupException
      * @throws DuplicateArticleException
      */
     public void addGroup(ArticleGroup group) throws DuplicateGroupException,
             DuplicateArticleException {
         if (!grNum.containsKey(group.getName())) {
             for (Article article : group.getList()) {
                 if (articles.containsKey(article.getName())) {
                     throw new DuplicateArticleException("Duplicated article: "
                             + article.getName() + " in group that's being added: " + 
                             group.getName());
                 }
             }
             groups.add(group);
             grNum.put(group.getName(), groups.size()-1);
             generateArtMap();
         } else {
             throw new DuplicateGroupException("Already existing group: "
                     + group.getName());
         }
 
         //FIXME can be heavy if in a batch of adds
         generateSignature();
     }
 
     /**
      * Adds an article to the specified group
      *
      * @param article The article to add.
      * @param groupName Name of the group.
      * @throws DuplicateArticleException
      * @throws NotExistingGroupException
      */
     public void addArticleToGroup(String groupName, Article article)
             throws DuplicateArticleException, NotExistingGroupException {
         if (articles.containsKey(article.getName())) {
             throw new DuplicateArticleException("Duplicate article with name:" +
                     article.getName());
         } else if (!grNum.containsKey(groupName)) {
             throw new NotExistingGroupException("No group with name:" +
                     " " + groupName);
         } else {
             articles.put(article.getName(), article);
             groups.get(grNum.get(groupName)).addArticle(article);
         }
 
         //FIXME can be heavy if in a batch of adds
         generateSignature();
     }
 
     /**
      * Adds an article to the specified group
      *
      * @param article The article to add.
      * @param groupNum Cardinal number of the group
      * @throws DuplicateArticleException
      * @throws NotExistingGroupException
      */
     public void addArticleToGroup(int groupNum, Article article)
             throws DuplicateArticleException, NotExistingGroupException {
         if (articles.containsKey(article.getName())) {
             throw new DuplicateArticleException("Duplicate article with name:" +
                     article.getName());
         } else if (groupNum >= 0 && groupNum < groups.size()) {
             throw new NotExistingGroupException("No group with cardinal number:" +
                     " " + groupNum);
         } else {
             articles.put(article.getName(), article);
             groups.get(groupNum).addArticle(article);
         }
 
         //FIXME can be heavy if in a batch of adds
         generateSignature();
     }
 
     /**
      * Enables/disables a specified article
      *
      * @param group Cardinal number of the group
      * @param pos Position of the article
      * @param enable Enable/disable
      * @return reference to the article enabled
      */
     public Article enableArticle(int group, int pos, boolean enable) {
         if ((group >= 0 && group < groups.size()) &&
                 ( pos >= 0 && pos < groups.get(group).getList().size()) ) {
             return groups.get(group).enableArticle(pos, enable);
         } else {
             return null;
         }
     }
 
     /**
      * Enables/disables a specified article
      *
      * @param art The article to modify
      * @param enable Enable/disable
      * @return reference to the article enabled
      */
     public Article enableArticle(Article art, boolean enable) {
         if (articles.containsKey(art.getName())) {
             Article temp = articles.get(art.getName());
             temp.setEnabled(enable);
             return temp;
         } else {
             return null;
         }
     }
 
     /**
      * Enables/disables a specified article
      *
      * @param group Cardinal number of the group
      * @param pos Position of the article
      * @param avail Enable/disable
      * @return reference to the article enabled
      */
     public Article markArticleAvailable(int group, int pos, boolean avail) {
         if ((group >= 0 && group < groups.size()) &&
                 ( pos >= 0 && pos < groups.get(group).getList().size()) ) {
             return groups.get(group).markArticleAvailable(pos, avail);
         } else {
             return null;
         }
     }
 
     /**
      * Enables/disables a specified article
      *
      * @param art The article to modify
      * @param avail Enable/disable
      * @return reference to the article enabled
      */
     public Article markArticleAvailable(Article art, boolean avail) {
         if (articles.containsKey(art.getName())) {
             Article temp = articles.get(art.getName());
             temp.setAvailabe(avail);
             return temp;
         } else {
             return null;
         }
     }
 
 //    /**
 //     * Moves an article to another group
 //     *
 //     * @param oldGroup
 //     * @param newGroup
 //     * @param art
 //     *
 //     * @return
 //     */
 //    public Article moveArticleToGroup(int oldGroup, int newGroup, int art) {
 //
 //        Article temp = list.remove(oldPos);
 //        list.add(newPos,temp);
 //        return temp;
 //    }
 
     /**
      * Getter for the list
      *
      * @return the list in a <code>java.util.List</code> form.
      */
     final public List<ArticleGroup> getGroupsList() {
         return new ArrayList<ArticleGroup>(groups);
     }
 
     /**
      * Getter for the list
      *
      * @return the list in a <code>java.util.List</code> form.
      */
     final public Collection<Article> getArticlesList() {
         return new ArrayList<Article>(articles.values());
     }
 
     /**
      * Gets the ArticleGroup corresponding at the give position.
      *
      * @param pos Cardinal number of the group on the ordered list
      * @return The requested group
      */
     public ArticleGroup getGroup(int pos) {
         if (pos >= 0 && pos < groups.size()) {
             return groups.get(pos);
         } else {
             return null;
         }
     }
 
     /**
      * Gets group by name
      *
      * @param name The name of the requested group
      * @return The group which has the given name
      */
     public ArticleGroup getGroup(String name) {
         if (grNum.containsKey(name)) {
             return groups.get(grNum.get(name));
         } else {
             return null;
         }
     }
 
     /**
      * Returns the position of the group with the given name
      *
      * @param name Name of the group
      * @return An integer rapresenting the cardinal position of the group
      */
     public int getGroupPos(String name) {
         if (grNum.containsKey(name)) {
             return grNum.get(name);
         } else {
             return -1;
         }
     }
 
     /**
      * Selects from the list of articles just the enabled ones, in enabled groups
      *
      * @return List of enabled articles in enabled groups
      */
     public ArticlesList getEnabledList() {
         List<ArticleGroup> tempList = new ArrayList<ArticleGroup>();
         for (ArticleGroup artGr : groups) {
            ArticleGroup tempGroup =
                    new ArticleGroup(artGr.getId(), artGr.getName());
            if (tempGroup.isEnabled()) {
                 for (Article art : artGr.getList()) {
                     if (art.isEnabled()) {
                        tempGroup.addArticle(art);
                     }
                 }
                 tempList.add(tempGroup);
             }
         }
         return new ArticlesList(tempList, this.signature);
     }
 
     /**
      * Gets a copy of the signature
      * @return an array of bytes containing the signature
      */
     public List<Integer> getSignature() {
         return signature;
     }
 
     /**
      * Generates a new signature for the list.
      *
      * It's invoked after updates. It's not much performance oriented in a batch
      * of updates.
      */
     public final void generateSignature() {
         signature = new ArrayList<Integer>();
         for (ArticleGroup articleGroup : groups) {
             if (articleGroup.isEnabled()) {
                 signature.add(articleGroup.hashCode());
                 for (Article article : articleGroup.getList()) {
                     if (article.isEnabled()) {
                         signature.add(article.hashCode());
                     }
                 }
             }
         }
         signature.add(java.util.Calendar.getInstance().getTime().hashCode());
     }
 }
