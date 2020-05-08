 package com.crowdstore.models.common;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 
 import javax.annotation.Nullable;
 import java.io.Serializable;
 
 /**
  * @author fcamblor
  *         Generic interface for almost every "identity" models, allowing to retrieve
  *         model's id in a generic way
  */
 public interface Identifiable extends Serializable {
     // Generic guava extractor by ID
    public static final Function<? super Identifiable, Long> EXTRACT_ID_FCT =
             new Function<Identifiable, Long>() {
                 @Override
                 public Long apply(@Nullable Identifiable input) {
                     return input.getId();
                 }
             };
     // Generic guava filter where id is null
    public static final Predicate<? super Identifiable> NULL_ID =
             new Predicate<Identifiable>() {
                 @Override
                 public boolean apply(@Nullable Identifiable input) {
                     return input.getId() == null;
                 }
             };
 
     Long getId();
 }
