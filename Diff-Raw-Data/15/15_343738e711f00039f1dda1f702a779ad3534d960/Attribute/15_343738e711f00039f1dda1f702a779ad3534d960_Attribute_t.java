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
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.Index;
 
 /**
  * @author Michael Backhaus <michael.backhaus@agfa.com>
  */
 @NamedQueries({
     @NamedQuery(
             name = "Attribute.deleteByKeyAndNodePK",
             query = "DELETE FROM Attribute attr WHERE attr.key = :key and attr.node.pk = :nodePK"),
     @NamedQuery(
             name = "Attribute.getAttributesByNodePK",
             query = "SELECT attr FROM Attribute attr WHERE attr.node.pk = :nodePK")
 })
 @Entity
 @Table(name = "attribute")
 public class Attribute {
 
     public static final String DELETE_BY_KEY_AND_NODE_PK = "Attribute.deleteByKeyAndNodePK";
     public static final String SELECT_BY_NODE_PK = "Attribute.getAttributesByNodePK";
 
     @Id
     @GeneratedValue
     private int pk;
 
     @Basic(optional = false)
    @Column(name = "attr_key")
     @Index(name="attribute_key_idx")
     private String key;
 
     @Basic(optional = false)
    @Column(length=4000, name = "attr_value")
     private String value;
 
     @ManyToOne(fetch = FetchType.EAGER)
     @JoinColumn(name = "node_fk")
     private Node node;
 
     public int getPk() {
         return pk;
     }
 
     public void setPk(int pk) {
         this.pk = pk;
     }
 
     public String getKey() {
         return key;
     }
 
     public void setKey(String key) {
         this.key = key;
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     public Node getNode() {
         return node;
     }
 
     public void setNode(Node node) {
         this.node = node;
     }
 }
