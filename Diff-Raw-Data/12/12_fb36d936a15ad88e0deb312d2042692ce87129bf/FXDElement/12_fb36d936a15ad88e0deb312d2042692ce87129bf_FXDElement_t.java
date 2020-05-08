 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 package org.netbeans.modules.javafx.fxd.composer.model;
 
 import com.sun.javafx.geom.Bounds2D;
 import com.sun.javafx.geom.transform.Affine2D;
 import com.sun.javafx.geom.transform.BaseTransform;
 import com.sun.javafx.geom.transform.NoninvertibleTransformException;
 import javafx.scene.Node;
 import javafx.scene.Parent;
 import org.netbeans.modules.editor.structure.api.DocumentElement;
 import org.netbeans.modules.javafx.fxd.dataloader.fxz.FXZDataObject;
 import org.openide.util.Exceptions;
 
 /**
  *
  * @author Pavel Benes
  */
 public final class FXDElement {
     private final FXZDataObject       m_dObj;
     private final String              m_id;
     private       FXDElementOutline   m_outline   = null;
     private       boolean             m_isDeleted = false;
 
 /*
     private       float               m_translateDx = 0;
     private       float               m_translateDy = 0;
     private       float               m_skewX       = 0;
     private       float               m_skewY       = 0;
     private       float               m_scaleX      = 1;
     private       float               m_scaleY      = 1;
     private       float               m_rotate      = 0;
     
     private       float               m_tempTranslateDx = 0;
     private       float               m_tempTranslateDy = 0;
     private       float               m_tempSkewX       = 0;
     private       float               m_tempSkewY       = 0;
     private       float               m_tempScaleX      = 1;
     private       float               m_tempScaleY      = 1;
     private       float               m_tempRotate      = 0;
 */
     public FXDElement(final FXZDataObject dObj, final String id) {
         assert dObj != null;
         assert id != null;
         assert id.length() > 0;
         
         m_dObj = dObj;
         m_id   = id;
 //        SceneManager.log(Level.FINE, "SVGObject created: " + m_elem); //NOI18N        
     }
 
     public boolean isVisible() {
         return getController().getNode(m_id).get$visible();
     }
     
     public String getName() {
         return getDocumentElement().getName();
     }
     
     public void visitAttributes( FXDFileModel.ElementAttrVisitor visitor) {
         FXDFileModel.visitAttributes( getDocumentElement(), visitor, true);
     }
     
     //TODO should be private
     public DocumentElement getDocumentElement() {
         DocumentElement de = null;
         try {
             FXDComposerModel model = m_dObj.getDataModel();
             de = model.getFXDContainer().getFileModel(model.getSelectedEntry()).getElementById(m_id);
         } catch (Exception ex) {
             //TODO Do not swallow it
             Exceptions.printStackTrace(ex);
         }
         return de;
     }
     
     public int getStartOffset() {
         return getDocumentElement().getStartOffset();
     }
     
     public FXDElement getVisibleParent() {
         DocumentElement de = getDocumentElement();
         while ( de != null) {
             de = de.getParentElement();
             if ( de != null && FXDFileModel.FXD_NODE.equals(de.getType())) {
                 String id = FXDFileModel.getElementId(de);
                 return new FXDElement(m_dObj, id);
             }
         }
         return null;
     }
     
     public boolean hasParent() {
         DocumentElement de = getDocumentElement();
         if ( de != null) {
             de = de.getParentElement();
             if ( de != null && !FXDFileModel.DOCUMENT_ROOT_ELEMENT_TYPE.equals(de.getType())) {
                 return true;
             }
         }
         return false;
     }
 
     Bounds2D getBounds() {
         Node node = getController().getNode(m_id);
         assert node != null;
         return getCompleteNodeBounds(node);
     }
 
     private Bounds2D getCompleteNodeBounds(Node node){
         Bounds2D b = new Bounds2D();
         Affine2D parentTx = getParentTx(node);
         return node.getTransformedBounds(b, parentTx);
     }
 
     private Affine2D getParentTx(Node node){
         Parent p = node.get$parent();
         // parent tx
         Affine2D parentTx = null;
         if (p != null) {
             parentTx = getParentTx(p);
         } else {
            float zoom = m_dObj.getDataModel().getZoomRatio();
            parentTx = new Affine2D(BaseTransform.getScaleInstance(zoom, zoom));
         }
         // local to parent tx
         Affine2D tx = new Affine2D();
         node.getLocalToParentTransform(tx);
 
         try{
             tx.invert();
         }catch(NoninvertibleTransformException e){
             e.printStackTrace();
         }
         tx.concatenate(parentTx);
         return parentTx;
     }
 
     public boolean isDeleted() {
         return m_isDeleted;
     }
     
     protected void repaint(int x, int y, int w, int h) {
         m_dObj.getController().getScenePanel().repaint(x, y, w, h);
     }
     
     public void repaint(double overlap) {
         if ( !m_isDeleted) {
             Bounds2D b = getBounds();
             //TODO the attribute elements should not be repainted
             if ( b != null) {
                 repaint( (int) Math.round( b.x1 - overlap),
                          (int) Math.round(b.y1 - overlap),
                          (int) Math.round(b.getWidth() + 2 * overlap),
                          (int) Math.round(b.getHeight() + 2 * overlap));
             }
         }
     }
     
     public synchronized FXDElementOutline getOutline() {
         if ( m_outline == null) {
             if (isVisible()) {
                 m_outline = new FXDElementOutline(this);
             }
         }
         return m_outline;
     }
     
     @Override
     public boolean equals(Object o) {
         if ( o instanceof FXDElement) {
             return ((FXDElement)o).m_id.equals(m_id);
         } else {
             return false;
         }        
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 89 * hash + (m_id != null ? m_id.hashCode() : 0);
         return hash;
     }
     
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append( "FXDElement( id="); //NOI18N
         sb.append( m_id);
         sb.append( " isDeleted="); //NOI18N
         sb.append( m_isDeleted);
         sb.append(")"); //NOI18N
 
         return sb.toString();
     }
     
     protected final FXDComposerController getController() {
         return m_dObj.getController();
     }
     
     public static boolean areSame(FXDElement [] arr1,FXDElement [] arr2) {
         if (arr1 == arr2) {
             return true;
         } else if (arr1 == null || arr2 == null) {
             return false;
         } else if (arr1.length != arr2.length) {
             return false;
         } else {
             for (int i = 0; i < arr1.length; i++) {
                 if ( arr1[i] != arr2[i]) {
                     return false;
                 }
             }
             return true;
         }
     }
     
     public static void repaint( final FXDElement [] elems, final int overlap) {
         for (FXDElement elem : elems) {
             if ( elem.isVisible()) {
                 elem.repaint(overlap);
             }
         }
     }
 }
