 package com.crowdstore.models.common;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 
 import javax.annotation.Nullable;
 import java.io.Serializable;
 
 /**
  * @author fcamblor
  *         Generic interface for models which could by identified with a code, allowing to retrieve
  *         model's code in a generic way
  */
 public interface Codifiable extends Serializable {
     // Generic guava extractor by code
    public static final Function<Codifiable, String> EXTRACT_CODE_FCT =
             new Function<Codifiable, String>() {
                 @Override
                 public String apply(@Nullable Codifiable input) {
                     return input.getCode();
                 }
             };
     // Generic guava filter where code is null
    public static final Predicate<Codifiable> NULL_ID =
             new Predicate<Codifiable>() {
                 @Override
                 public boolean apply(@Nullable Codifiable input) {
                     return input.getCode() == null;
                 }
             };
 
     String getCode();
 }
