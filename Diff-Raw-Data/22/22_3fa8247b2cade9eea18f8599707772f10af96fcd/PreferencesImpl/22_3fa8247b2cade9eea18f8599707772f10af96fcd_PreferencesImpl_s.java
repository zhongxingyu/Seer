 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contentsOfthis file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copyOfthe License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is partOfdcm4che, an implementationOfDICOM(TM) in
  * Java(TM), hosted at https://github.com/gunterze/dcm4che.
  *
  * The Initial DeveloperOfthe Original Code is
  * Agfa Healthcare.
  * Portions created by the Initial Developer are Copyright (C) 2011
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s):
  * See @authors listed below
  *
  * Alternatively, the contentsOfthis file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisionsOfthe GPL or the LGPL are applicable instead
  *Ofthose above. If you wish to allow useOfyour versionOfthis file only
  * under the termsOfeither the GPL or the LGPL, and not to allow others to
  * use your versionOfthis file under the termsOfthe MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your versionOfthis file under
  * the termsOfany oneOfthe MPL, the GPL or the LGPL.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package org.dcm4che.jdbc.prefs;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.prefs.AbstractPreferences;
 import java.util.prefs.BackingStoreException;
 
 import org.apache.log4j.Logger;
 import org.dcm4che.jdbc.prefs.entity.Attribute;
 import org.dcm4che.jdbc.prefs.entity.Node;
 
 /**
  * @author Michael Backhaus <michael.backhaus@agfa.com>
  */
 public class PreferencesImpl extends AbstractPreferences {
 
     private static final Logger LOG = Logger.getLogger(PreferencesImpl.class);
 
     private QueryPreferences queryPreferences;
     private Node node = new Node();
     private HashMap<String, String> attributes;
     private HashMap<String, Node> childs;
 
     private HashMap<String, String> attributes() {
         if (attributes == null) {
             attributes = new HashMap<String, String>();
             for (Attribute attr : node.getAttributes())
                 attributes.put(attr.getKey(), attr.getValue());
         }
         return attributes;
     }
 
     private HashMap<String, Node> childs() {
         if (childs == null) {
             childs = new HashMap<String, Node>();
             for (Node child : queryPreferences.getChildren(node))
                 childs.put(child.getName(), child);
         }
         return childs;
     }
 
     public PreferencesImpl(QueryPreferences queryPreferences) {
         super(null, "");
         this.queryPreferences = queryPreferences;
         List<Node> results = queryPreferences.getRootNode();
         if (results.isEmpty()) {
             LOG.debug("PreferencesImpl() - insert new rootNode");
             node.setName("rootNode");
             node.setParentNode(null);
             queryPreferences.insertNode(node);
         } else {
             node = results.get(0);
         }
     }
 
     public PreferencesImpl(PreferencesImpl parent, Node child) {
         super(parent, child.getName());
         this.queryPreferences = parent.queryPreferences;
         node = child;
     }
 
     @Override
     protected AbstractPreferences childSpi(String name) {
         LOG.debug("childSpi(String) - name = " + name);
         Node child = childs().get(name);
         if (child == null) {
             LOG.debug("create new Node name=" + name);
             child = new Node();
             child.setName(name);
             child.setParentNode(node);
             queryPreferences.insertNode(child);
             childs.put(child.getName(), child);
         }
         return new PreferencesImpl(this, child);
     }
 
     @Override
     protected String[] childrenNamesSpi() throws BackingStoreException {
         LOG.debug("childrenNamesSpi() node.pk = " + node.getPk());
         return childs().keySet().toArray(new String[childs.size()]);
     }
 
     @Override
     protected String getSpi(String key) {
         LOG.debug("getSpi(String) - key=" + key);
         return attributes().get(key);
     }
 
     @Override
     protected String[] keysSpi() throws BackingStoreException {
         LOG.debug("keysSpi()");
         return attributes().keySet().toArray(new String[attributes.size()]);
     }
 
     @Override
     public String name() {
         return (this.parent() == null) ? "root" : super.name();
     }
 
     @Override
     protected void putSpi(String key, String value) {
         LOG.debug("putSpi(String, String) - key=" + key + ", value=" + value);
         Attribute attr = new Attribute();
         attr.setKey(key);
         attr.setValue(value);
         attr.setNode(node);
         queryPreferences.insertAttribute(attr);
         attributes().put(key, value);
     }
 
     @Override
     protected void removeNodeSpi() throws BackingStoreException {
         LOG.debug("removeNodeSpi() pk = " + node.getPk());
         queryPreferences.removeNode(node);
         ((PreferencesImpl) parent()).childs().remove(name());
     }
 
     @Override
     protected void removeSpi(String key) {
         LOG.debug("removeSpi(String) - key = " + key);
         queryPreferences.removeAttributeByKey(key, node);
         attributes().remove(key);
     }
 
     @Override
     protected void flushSpi() throws BackingStoreException {
         LOG.debug("flushSpi()");
         queryPreferences.flush();
     }
 
     @Override
     protected void syncSpi() throws BackingStoreException {
         LOG.debug("syncSpi()");
         queryPreferences.refresh(node);
     }
 }
