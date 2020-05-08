 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
  * Java(TM), hosted at https://github.com/dcm4che.
  *
  * The Initial Developer of the Original Code is
  * Agfa Healthcare.
  * Portions created by the Initial Developer are Copyright (C) 2011
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s):
  * See @authors listed below
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package org.dcm4che.jdbc.prefs.entity;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import javax.persistence.Basic;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.Index;
 import org.hibernate.annotations.OnDelete;
 import org.hibernate.annotations.OnDeleteAction;
 
 /**
  * @author Michael Backhaus <michael.backhaus@agfa.com>
  */
 @NamedQueries({
         @NamedQuery(name = "Node.getChildren", query = "SELECT n from Node n LEFT JOIN FETCH n.attributes where n.parentNode = ?1"),
         @NamedQuery(name = "Node.getRootNode", query = "SELECT n from Node n where n.name = 'rootNode'") })
 @Entity
 @Table(name = "node")
 public class Node {
 
     public static final String GET_CHILDREN = "Node.getChildren";
     public static final String GET_ROOT_NODE = "Node.getRootNode";
 
     @Id
     @GeneratedValue
     private int pk;
 
     @Basic(optional = false)
     @Index(name = "node_name_idx")
     private String name;
 
     @ManyToOne
     @JoinColumn(name = "parent_pk")
     private Node parentNode;
 
     @OneToMany(mappedBy = "node")
     @OnDelete(action = OnDeleteAction.CASCADE)
     private Collection<Attribute> attributes = new HashSet<Attribute>();
 
     public int getPk() {
         return pk;
     }
 
     public void setPk(int pk) {
         this.pk = pk;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Node getParentNode() {
         return parentNode;
     }
 
     public void setParentNode(Node parent) {
         this.parentNode = parent;
     }
 
     public Collection<Attribute> getAttributes() {
         return attributes;
     }
 }
