 package org.mosaic.util.conversion;
 
import com.google.common.base.Optional;
 import com.google.common.reflect.TypeToken;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 
 /**
  * A service for converting values from one type to another.
  * <p/>
  * Provided source value and the target type, this service will find the shortest path (ie. using as little converters
  * as possible) for converting from the source value's type to the target type.
  * <p/>
 * {@code null} source values will always result in {@code null} result, represented by an empty {@link Optional} instance.
  * <p/>
  * The list of available {@link Converter}s is all registered {@code Converter} instances available at runtime. Thus,
  * to extend convertability of types, simply register your converters as Mosaic services, and the rest will be done
  * automatically.
  *
  * @author arik
  */
 public interface ConversionService
 {
     /**
      * Convert given value to the target type token.
      *
      * @param source          source value
      * @param targetTypeToken the type to convert to, full generics is taken into consideration
      * @param <Source>        source value type
      * @param <Dest>          target type
     * @return an {@link Optional optional} converted value (optional will be empty for {@code null} values)
      */
     @Nullable
     <Source, Dest> Dest convert( @Nullable Source source, @Nonnull TypeToken<Dest> targetTypeToken );
 
     /**
      * Convert given value to the target type token.
      *
      * @param source     source value
      * @param targetType the type to convert to
      * @param <Source>   source value type
      * @param <Dest>     target type
     * @return an {@link Optional optional} converted value (optional will be empty for {@code null} values)
      */
     @Nullable
     <Source, Dest> Dest convert( @Nullable Source source, @Nonnull Class<Dest> targetType );
 }
