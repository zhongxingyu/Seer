 package com.dzebsu.acctrip.dictionary.utils;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.Context;
 
 import com.dzebsu.acctrip.db.datasources.CategoryDataSource;
 import com.dzebsu.acctrip.db.datasources.CurrencyDataSource;
 import com.dzebsu.acctrip.db.datasources.IDictionaryDataSource;
 import com.dzebsu.acctrip.db.datasources.PlaceDataSource;
 import com.dzebsu.acctrip.dictionary.DictionaryType;
 import com.dzebsu.acctrip.models.dictionaries.BaseDictionary;
 import com.dzebsu.acctrip.models.dictionaries.Category;
 import com.dzebsu.acctrip.models.dictionaries.Currency;
 import com.dzebsu.acctrip.models.dictionaries.Place;
 
 public class DictUtils {
 
 	private static Map<Class<?>, DictionaryType> dictTypeMapping;
 
 	static {
 		dictTypeMapping = new HashMap<Class<?>, DictionaryType>();
 		dictTypeMapping.put(Place.class, DictionaryType.PLACE);
 		dictTypeMapping.put(Category.class, DictionaryType.CATEGORY);
 		dictTypeMapping.put(Currency.class, DictionaryType.CURRENCY);
 	}
 
 	public static DictionaryType getDictionaryType(Class<?> clazz) {
 		return dictTypeMapping.get(clazz);
 	}
 
 	public static <T extends BaseDictionary> IDictionaryDataSource<T> getEntityDataSourceInstance(Class<T> clazz,
 			Context cxt) {
 		if (Place.class.isAssignableFrom(clazz)) return (IDictionaryDataSource<T>) new PlaceDataSource(cxt);
 		if (Category.class.isAssignableFrom(clazz)) return (IDictionaryDataSource<T>) new CategoryDataSource(cxt);
 		if (Currency.class.isAssignableFrom(clazz)) return (IDictionaryDataSource<T>) new CurrencyDataSource(cxt);
 
 		return null;
 	}
 
 }
