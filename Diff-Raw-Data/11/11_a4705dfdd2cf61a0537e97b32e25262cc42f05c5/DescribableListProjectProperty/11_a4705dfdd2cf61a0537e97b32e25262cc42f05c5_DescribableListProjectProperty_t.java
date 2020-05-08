 /*******************************************************************************
  *
  * Copyright (c) 2011 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *
  *    Nikita Levyankov
  *
  *******************************************************************************/
 package org.eclipse.hudson.api.model.project.property;
 
 import hudson.util.DescribableList;
 import org.apache.commons.collections.CollectionUtils;
 import org.eclipse.hudson.api.model.IJob;
 
 /**
  * Property represents DescribableList object.
  * <p/>
  * Date: 10/3/11
  *
  * @author Nikita Levyankov
  */
 public class DescribableListProjectProperty extends BaseProjectProperty<DescribableList> {
     public DescribableListProjectProperty(IJob job) {
         super(job);
     }
 
     @Override
     public DescribableList getDefaultValue() {
        DescribableList result = new DescribableList(getJob());
        setOriginalValue(result, false);
        return result;
     }
 
     @Override
     public boolean allowOverrideValue(DescribableList cascadingValue, DescribableList candidateValue) {
         return (null != candidateValue || null != cascadingValue)
             && ((null == cascadingValue || null == candidateValue)
             || !CollectionUtils.isEqualCollection(cascadingValue.toList(), candidateValue.toList()));
     }
 
     @Override
     protected boolean returnOriginalValue() {
         return isOverridden() || (null != getOriginalValue() && !getOriginalValue().isEmpty());
     }
 
     @Override
     public DescribableList getOriginalValue() {
         DescribableList result = super.getOriginalValue();
         return null != result ? result : getDefaultValue();
     }
 }
 
 
