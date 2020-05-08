 package org.atlasapi.media.entity.simple;
 
 import java.math.BigInteger;
 
 import org.atlasapi.media.entity.ChildRef;
 import org.atlasapi.media.entity.EntityType;
 import org.atlasapi.media.entity.SeriesRef;
 
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.metabroadcast.common.ids.NumberToShortStringCodec;
 
 public abstract class ContentIdentifier {
 
     protected String uri;
     protected String type;
     protected String id;
 
     public ContentIdentifier() {
     }
 
     public ContentIdentifier(String uri, String type) {
         this(uri, type, null);
     }
     
     public ContentIdentifier(String uri, String type, String id) {
         this.uri = uri;
         this.type = type;
         this.id = id;
     }
     
     public void setId(String id) {
         this.id = id;
     }
     
     public String getId() {
         return id;
     }
     
     public void setUri(String uri) {
         this.uri = uri;
     }
 
     public String getUri() {
         return uri;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public String getType() {
         return type;
     }
 
     public abstract ContentIdentifier copy();
 
     public static class ItemIdentifier extends ContentIdentifier {
 
         public ItemIdentifier() {
         }
 
         public ItemIdentifier(String uri) {
             this(uri, null);
         }
         
         public ItemIdentifier(String uri, String id) {
             super(uri, EntityType.ITEM.toString(), id);
         }
 
         @Override
         public ItemIdentifier copy() {
             return new ItemIdentifier(uri, id);
         }
     }
 
     public static class EpisodeIdentifier extends ContentIdentifier {
         
         public EpisodeIdentifier() {
         }
 
         public EpisodeIdentifier(String uri) {
             this(uri, null);
         }
         
         public EpisodeIdentifier(String uri, String id) {
             super(uri, EntityType.EPISODE.toString(), id);
         }
 
         @Override
         public EpisodeIdentifier copy() {
             return new EpisodeIdentifier(uri, id);
         }
     }
 
     public static class FilmIdentifier extends ContentIdentifier {
         
         public FilmIdentifier() {
         }
 
         public FilmIdentifier(String uri) {
             this(uri, null);
         }
         
         public FilmIdentifier(String uri, String id) {
             super(uri, EntityType.FILM.toString(), id);
         }
 
         @Override
         public FilmIdentifier copy() {
             return new FilmIdentifier(uri, id);
         }
     }
 
     public static class BrandIdentifier extends ContentIdentifier {
         
         public BrandIdentifier() {
         }
 
         public BrandIdentifier(String uri) {
             this(uri, null);
         }
         
         public BrandIdentifier(String uri, String id) {
             super(uri, EntityType.BRAND.toString(), id);
         }
 
         @Override
         public BrandIdentifier copy() {
             return new BrandIdentifier(uri, id);
         }
     }
 
     public static class SeriesIdentifier extends ContentIdentifier {
         
         private Integer seriesNumber;
 
         public SeriesIdentifier() {
             
         }
 
         public SeriesIdentifier(String uri) {
             this(uri, null);
         }
         
         public SeriesIdentifier(String uri, Integer seriesNumber) {
             this(uri, seriesNumber, null);
         }
         
         public SeriesIdentifier(String uri, Integer seriesNumber, String id) {
             super(uri, EntityType.SERIES.toString(), id);
             this.seriesNumber = seriesNumber;
         }
 
         @Override
         public SeriesIdentifier copy() {
             return new SeriesIdentifier(uri, seriesNumber, id);
         }
         
         public Integer getSeriesNumber() {
             return seriesNumber;
         }
         
         public void setSeriesNumber(Integer seriesNumber) {
             this.seriesNumber = seriesNumber;
         }
     }
 
     public static class PersonIdentifier extends ContentIdentifier {
         
         public PersonIdentifier() {
         }
 
         public PersonIdentifier(String uri) {
             this(uri, null);
         }
         
         public PersonIdentifier(String uri, String id) {
             super(uri, EntityType.PERSON.toString(), id);
         }
 
         @Override
         public PersonIdentifier copy() {
             return new PersonIdentifier(uri, id);
         }
     }
     
     @Override
     public String toString() {
         return Objects.toStringHelper(this).add("id", id).add("uri", uri).add("type", type).toString();
     }
     
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         } else if (obj instanceof ContentIdentifier) {
            return Objects.equal(this.uri, ((ContentIdentifier)obj).uri) && Objects.equal(this.type, ((ContentIdentifier)obj).uri);
         } else {
             return false;
         }
     }
     
     @Override
     public int hashCode() {
         return Objects.hashCode(uri);
     }
 
 
 	private static ContentIdentifier create(EntityType type, String uri, String id, Integer seriesNumber) {
 	    switch (type) {
         case BRAND:
             return new BrandIdentifier(uri, id);
         case EPISODE:
             return new EpisodeIdentifier(uri, id);
         case FILM:
             return new FilmIdentifier(uri, id);
         case ITEM:
             return new ItemIdentifier(uri, id);
         case PERSON:
             return new PersonIdentifier(uri, id);
         case SERIES:
             return new SeriesIdentifier(uri, seriesNumber, id);
         default:
             throw new RuntimeException("Can't create content identifier for " + uri);	    
 	    }
 	}
     
     public static ContentIdentifier identifierFor(ChildRef childRef, NumberToShortStringCodec idCodec) {
         return create(childRef.getType(), childRef.getUri(), idFrom(idCodec, childRef), null);
     }
     
     public static SeriesIdentifier seriesIdentifierFor(SeriesRef seriesRef, NumberToShortStringCodec idCodec) {
         return (SeriesIdentifier) create(EntityType.SERIES, seriesRef.getUri(), idFrom(idCodec, seriesRef), 
                 seriesRef.getSeriesNumber());
     }
 
     private static String idFrom(NumberToShortStringCodec idCodec, ChildRef childRef) {
         return childRef.getId() != null ? idCodec.encode(BigInteger.valueOf(childRef.getId()))
                                              : null;
     }
     
     private static String idFrom(NumberToShortStringCodec idCodec, SeriesRef seriesRef) {
         return seriesRef.getId() != null ? idCodec.encode(BigInteger.valueOf(seriesRef.getId()))
                                              : null;
     }
     
     public static ContentIdentifier identifierFrom(String id, String canonicalUri, String type) {
         EntityType from = EntityType.from(type);
         if (EntityType.SERIES.equals(from)) {
             throw new IllegalArgumentException("Series not supported, use seriesIdentiferFrom instead");
         }
         
         return create(from, canonicalUri, id, null);
     }
     
     public static ContentIdentifier seriesIdentifierFrom(String canonicalUri, String id, Integer seriesNumber) {
         return create(EntityType.SERIES, canonicalUri, id, seriesNumber);
     }
     
     public static final Function<ContentIdentifier, String> TO_ID = new Function<ContentIdentifier, String>() {
         @Override
         public String apply(ContentIdentifier input) {
             return input.getId();
         }
     };
     
     public static final Function<ContentIdentifier, String> TO_URI = new Function<ContentIdentifier, String>() {
         @Override
         public String apply(ContentIdentifier input) {
             return input.getUri();
         }
     };
     
     public static final Function<ContentIdentifier, ContentIdentifier> COPY = new Function<ContentIdentifier, ContentIdentifier>() {
         @Override
         public ContentIdentifier apply(ContentIdentifier input) {
             return input.copy();
         }
     };
 }
