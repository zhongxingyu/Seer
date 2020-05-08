 package com.crowdstore.models.common;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 
 import javax.annotation.Nullable;
 import java.io.Serializable;
 
 /**
  * @author fcamblor
  *         Generic interface for almost every models, allowing to retrieve
  *         model's identity in a generic way
  */
 public interface IdentifiableContainer<T extends Identifiable> extends Serializable {
     // Generic guava extractor by ID
    public static final Function<IdentifiableContainer, Long> EXTRACT_ID_FCT =
             new Function<IdentifiableContainer, Long>() {
                 @Override
                 public Long apply(@Nullable IdentifiableContainer input) {
                     return input.getIdentity().getId();
                 }
             };
     // Generic guava filter where id is null
    public static final Predicate<IdentifiableContainer> NULL_ID =
             new Predicate<IdentifiableContainer>() {
                 @Override
                 public boolean apply(@Nullable IdentifiableContainer input) {
                     return input.getIdentity().getId() == null;
                 }
             };
 
     T getIdentity();
     void setIdentity(T identity);
 }
