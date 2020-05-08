 package com.flexive.shared.value.mapper;
 
 import com.flexive.shared.value.FxLargeNumber;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.search.query.ValueComparator;
 import com.flexive.shared.search.query.PropertyValueComparator;
 import com.flexive.shared.structure.FxDataType;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.EJBLookup;
 
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * @author Daniel Lichtenberger, UCS
  * @version $Rev$
  */
 public abstract class NumberQueryInputMapper extends InputMapper<FxLargeNumber, FxString> {
     public static class AccountQueryInputMapper extends NumberQueryInputMapper {
         public AccountQueryInputMapper() {
             buildAutocompleteHandler("AutoCompleteProvider.userQuery");
         }
 
         @Override
        public String encodeId(Long id) {
            if (id == null) {
                return "";
            }
             try {
                 return EJBLookup.getAccountEngine().load(id).getLoginName();
             } catch (FxApplicationException e) {
                 return "";
             }
         }
 
         @Override
         public Long decodeQuery(String query) {
             if (StringUtils.isNotBlank(query)) {
                 try {
                     return EJBLookup.getAccountEngine().load(query).getId();
                 } catch (FxApplicationException e) {
                     return null;    // fail silently - reset input
                 }
             }
             return null;
         }
     }
 
    public abstract String encodeId(Long id);
 
     public abstract Long decodeQuery(String query);
 
     @Override
     public FxString encode(FxLargeNumber value) {
         if (value.isMultiLanguage()) {
             throw new FxInvalidParameterException("VALUE", "ex.content.value.mapper.select.singleLanguage").asRuntimeException();
         }
        return new FxString(value.isMultiLanguage(), encodeId(value.isEmpty() ? null : value.getDefaultTranslation()));
     }
 
     @Override
     public FxLargeNumber decode(FxString value) {
         final Long result = decodeQuery(value.getDefaultTranslation());
         return result != null ? new FxLargeNumber(false, result) : new FxLargeNumber(false, FxLargeNumber.EMPTY).setEmpty();
     }
 
     @Override
     public List<? extends ValueComparator> getAvailableValueComparators() {
         return PropertyValueComparator.getAvailable(FxDataType.SelectOne);
     }
 }
