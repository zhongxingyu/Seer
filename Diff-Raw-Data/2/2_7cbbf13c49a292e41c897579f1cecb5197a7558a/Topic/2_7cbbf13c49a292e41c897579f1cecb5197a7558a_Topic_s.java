 /**
  * 
  * Copyright 2011-2013 Martin Goellnitz
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package org.tangram.rdbms.solution;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.jdo.annotations.NotPersistent;
 import javax.jdo.annotations.PersistenceCapable;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.tangram.content.Content;
 import org.tangram.solution.protection.ProtectedContent;
 
 @PersistenceCapable
 public class Topic extends Linkable implements ProtectedContent {
 
     private static final Log log = LogFactory.getLog(Topic.class);
 
     private List<String> subTopicIds;
 
     private List<String> elementIds;
 
     private ImageData thumbnail;
 
     private char[] teaser;
 
     private List<String> relatedContainerIds;
 
 
     public List<Topic> getSubTopics() {
         return getContents(Topic.class, subTopicIds);
     }
 
 
     public void setSubTopics(List<Topic> subTopics) {
         this.subTopicIds = getIds(subTopics);
     }
 
 
     public List<Linkable> getElements() {
         return getContents(Linkable.class, elementIds);
     }
 
 
     public void setElements(List<Linkable> elements) {
         this.elementIds = getIds(elements);
     }
 
 
     public ImageData getThumbnail() {
         return thumbnail;
     }
 
 
     public void setThumbnail(ImageData thumbnail) {
         this.thumbnail = thumbnail;
     }
 
 
     public char[] getTeaser() {
         return teaser;
     }
 
 
     public void setTeaser(char[] teaser) {
         this.teaser = teaser;
     }
 
 
     public List<Container> getRelatedContainers() {
         return getContents(Container.class, relatedContainerIds);
     }
 
 
     public void setRelatedContainers(List<Container> relatedContainers) {
         this.relatedContainerIds = getIds(relatedContainers);
     }
 
     /************************************/
 
     @NotPersistent
     RootTopic rootTopic = null;
 
 
     public RootTopic getRootTopic() {
         if (rootTopic==null) {
             List<RootTopic> rootTopics = getBeanFactory().listBeans(RootTopic.class, null);
             if (rootTopics!=null) {
                 if (rootTopics.size()>0) {
                     rootTopic = rootTopics.get(0);
                 } // if
             } // if
         } // if
         return rootTopic;
     } // getRootTopic()
 
 
     public List<Topic> getPathRecursive(Topic t) {
         List<Topic> result = null;
         if (equals(t)) {
             result = new ArrayList<Topic>();
             result.add(t);
         } else {
             List<Topic> subs = t.getSubTopics();
             if (subs!=null) {
                 for (Topic x : subs) {
                     if (x!=null) {
                         List<Topic> p = getPathRecursive(x);
                         if (p!=null) {
                             p.add(0, t);
                             return p;
                         } // if
                     } else {
                         log.error("getPathRecursive() "+t.getId()+" has null pointer subtopic");
                     } // if
                 } // for
             } // if
         } // if
 
         return result;
     } // getPath()
 
 
     @Override
     public String toString() {
         return "Topic [subTopics="+subTopicIds+", elements="+elementIds+", thumbnail="+thumbnail+", teaser="+Arrays.toString(teaser)
                +", relatedContainers="+relatedContainerIds+", rootTopic="+rootTopic+", path="+path+", inheritedRelatedContainers="
                 +inheritedRelatedContainers+"]";
     } // toString()
 
     @NotPersistent
     private List<Topic> path;
 
 
     public List<Topic> getPath() {
         if (log.isDebugEnabled()) {
             log.debug(getClass().getName()+".getPath("+getId()+") "+getRootTopic().getClass().getName());
         } // if
         if (path==null) {
             path = getPathRecursive(getRootTopic());
             if (path==null) {
                 path = new ArrayList<Topic>();
                 // path.add(getRootTopic());
                 path.add(this);
             } // if
         } // if
         return path;
     } // getPath()
 
     @NotPersistent
     private List<Container> inheritedRelatedContainers;
 
 
     public List<Container> getInheritedRelatedContainers() {
         List<Container> result = new ArrayList<Container>();
         if (inheritedRelatedContainers==null) {
             List<Topic> p = getPath();
             int i = p.size();
             while (( --i>=0)&&(result.size()==0)) {
                 result = p.get(i).getRelatedContainers();
             } // while
             inheritedRelatedContainers = result;
         } else {
             result = inheritedRelatedContainers;
         } // if
         return result;
     } // getInheritedRelatedContainers
 
 
     /**** Protections ***/
 
     @Override
     public List<? extends Content> getProtectionPath() {
         return getPath();
     } // getProtectionPath()
 
 } // Topic
