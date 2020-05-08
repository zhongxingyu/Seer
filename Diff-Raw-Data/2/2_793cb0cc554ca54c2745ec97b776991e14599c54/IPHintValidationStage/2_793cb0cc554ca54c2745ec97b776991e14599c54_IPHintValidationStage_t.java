 /*
  * Copyright (C) 2011 University of Edinburgh.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.org.ukfederation.mda.validate.mdui;
 
 import javax.annotation.Nonnull;
 import javax.annotation.concurrent.ThreadSafe;
 
 import net.shibboleth.metadata.dom.DomElementItem;
 import net.shibboleth.utilities.java.support.component.ComponentSupport;
 import net.shibboleth.utilities.java.support.net.IPRange;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import uk.org.ukfederation.mda.validate.BaseValidationStage;
 
 /**
  * A stage which validates mdui:IPHint elements.
  */
 @ThreadSafe
 public class IPHintValidationStage extends BaseValidationStage {
 
     /** Whether to check that the CIDR notation describes a network. Defaults to true. */
     private boolean checkingNetworks = true;
     
     /**
      * Gets whether the stage is checking for network addresses only.
      * 
      * @return whether the stage is checking for network addresses only
      */
     public boolean isCheckingNetworks() {
         return checkingNetworks;
     }
 
     /**
      * Sets whether the stage is checking for network addresses only.
      * 
      * @param check whether to check for network addresses only
      */
     public void setCheckingNetworks(final boolean check) {
         ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
         ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
 
         this.checkingNetworks = check;
     }
 
     /** {@inheritDoc} */
     protected void validateItem(@Nonnull final DomElementItem item, @Nonnull final Element docElement) {
         assert item != null;
         assert docElement != null;
         final NodeList ipHints = docElement.getElementsByTagNameNS(MduiConstants.MDUI_NS, "IPHint");
         for (int index = 0; index < ipHints.getLength(); index++) {
             final Element ipHint = (Element)ipHints.item(index);
             final String hint = ipHint.getTextContent();
             try {
                 final IPRange range = IPRange.parseCIDRBlock(hint);
                 if (checkingNetworks) {
                     if (range.getHostAddress() != null) {
                         addError(item, ipHint, "invalid IPHint '" + hint +
                                "': CIDR notation represents a host, not a network");
                     }
                 }
             } catch (IllegalArgumentException e) {
                 addError(item, ipHint, "invalid IPHint '" + hint + "': " + e.getMessage());
             }
         }
     }
 
 }
