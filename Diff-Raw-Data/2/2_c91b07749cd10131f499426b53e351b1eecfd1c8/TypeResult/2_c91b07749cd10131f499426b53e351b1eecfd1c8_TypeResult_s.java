 package org.zwobble.shed.compiler.typechecker;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import lombok.EqualsAndHashCode;
 import lombok.ToString;
 
 import org.zwobble.shed.compiler.Function0;
 import org.zwobble.shed.compiler.HasErrors;
 import org.zwobble.shed.compiler.parsing.CompilerError;
 
 import com.google.common.base.Function;
 
 @ToString
 @EqualsAndHashCode
 public class TypeResult<T> implements HasErrors {
     public static <T> TypeResult<T> success(T value) {
         return new TypeResult<T>(true, value, Collections.<CompilerError>emptyList());
     }
     
     public static <T> TypeResult<T> failure(List<CompilerError> errors) {
         return new TypeResult<T>(false, null, errors);
     }
     
     public static <T> TypeResult<List<T>> combine(Iterable<? extends TypeResult<? extends T>> results) {
         List<T> values = new ArrayList<T>();
         List<CompilerError> errors = new ArrayList<CompilerError>();
         boolean success = true;
         for (TypeResult<? extends T> result : results) {
             success &= result.success;
             errors.addAll(result.getErrors());
             values.add(result.value);
         }
         return new TypeResult<List<T>>(success, values, errors);
         
     }
     
     private final boolean success;
     private final T value;
     private final List<CompilerError> errors;
     
     private TypeResult(boolean success, T value, List<CompilerError> errors) {
         this.success = success;
         this.value = value;
         this.errors = errors;
     }
     
     public T get() {
         return value;
     }
     
     @Override
     public List<CompilerError> getErrors() {
         return errors;
     }
     
     public boolean isSuccess() {
         return success;
     }
     
     public boolean hasValue() {
         return success;
     }
     
     public <R> TypeResult<R> then(Function0<TypeResult<R>> function) {
         TypeResult<R> result = function.apply();
         return thenResult(result);
     }
     
     public <R> TypeResult<R> use(Function<T, TypeResult<R>> function) {
         if (hasValue()) {
             return function.apply(value);
         } else {
            return new TypeResult<R>(false, null, Collections.<CompilerError>emptyList());
         }
     }
     
     
     public <R> TypeResult<R> ifValueThen(Function<T, TypeResult<R>> function) {
         if (hasValue()) {
             TypeResult<R> result = function.apply(value);
             return thenResult(result);
         } else {
             return new TypeResult<R>(success, null, errors);
         }
     }
     
     public TypeResult<T> withErrorsFrom(TypeResult<?> other) {
         return thenResult(other, value);
     }
     
     private <R> TypeResult<R> thenResult(TypeResult<R> result) {
         return thenResult(result, result.value);
     }
     
     private <R> TypeResult<R> thenResult(TypeResult<?> result, R value) {
         List<CompilerError> newErrors = new ArrayList<CompilerError>();
         newErrors.addAll(errors);
         newErrors.addAll(result.errors);
         return new TypeResult<R>(success && result.success, value, newErrors);
     }
 }
