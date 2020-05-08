 package com.discoverydns.dnsapiclient.internal;
 
 import org.xbill.DNS.utils.json.resourcerecords.ZoneResourceRecordModule;
 
 import com.discoverydns.dnsapiclient.ObjectMapperFactory;
 import com.discoverydns.dnsapiclient.command.nameServerInterfaceSet.NameServerInterface;
 import com.discoverydns.dnsapiclient.internal.json.nameserverinterfaceset.NameServerInterfaceDeserializer;
 import com.discoverydns.dnsapiclient.internal.json.nameserverinterfaceset.NameServerInterfaceSerializer;
 import com.fasterxml.jackson.core.JsonGenerator;
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.databind.DeserializationFeature;
 import com.fasterxml.jackson.databind.MapperFeature;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.SerializationFeature;
 import com.fasterxml.jackson.databind.module.SimpleModule;
 import com.fasterxml.jackson.datatype.joda.JodaModule;
 
 public class DefaultObjectMapperFactory implements ObjectMapperFactory {
 
 	@Override
 	public ObjectMapper createInstance() {
 		final ObjectMapper mapper = new ObjectMapper();
 		mapper.registerModule(new JodaModule());
 
 		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
 		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, false);
 		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, false);
 		mapper.configure(
 				JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER,
 				false);
 		mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, false);
 		mapper.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, false);
 		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, false);
 		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, false);
 		mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
 		mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
 		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, true);
 		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, true);
 		mapper.configure(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM, true);
 		mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
 		mapper.configure(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS, true);
 		mapper.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, false);
 
 		mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
 		mapper.configure(MapperFeature.AUTO_DETECT_CREATORS, true);
 		mapper.configure(MapperFeature.AUTO_DETECT_FIELDS, true);
 		mapper.configure(MapperFeature.AUTO_DETECT_GETTERS, true);
 		mapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, true);
 		mapper.configure(MapperFeature.AUTO_DETECT_SETTERS, true);
 		mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, false);
 		mapper.configure(MapperFeature.USE_GETTERS_AS_SETTERS, true);
 		mapper.configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
 		mapper.configure(MapperFeature.INFER_PROPERTY_MUTATORS, false);
 		mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false);
 		mapper.configure(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME, false);
 		mapper.configure(MapperFeature.USE_STATIC_TYPING, false);
 		mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
 		mapper.configure(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS, false);
 
 		mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS,
 				false);
 		mapper.configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, false);
 		mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY,
 				false);
 		mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING,
 				false);
 		mapper.configure(
 				DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
 				false);
 		mapper.configure(
 				DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, false);
 		mapper.configure(
 				DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS,
 				false);
 		mapper.configure(
 				DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
 		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
 				false);
 		mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
 				true);
 		mapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
 		mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, true);
 		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
 				false);
 		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
 		mapper.configure(DeserializationFeature.EAGER_DESERIALIZER_FETCH, true);
 		mapper.configure(DeserializationFeature.WRAP_EXCEPTIONS, true);
 
 		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
 		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
 		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
 		mapper.configure(SerializationFeature.WRAP_EXCEPTIONS, true);
 		mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, false);
 		mapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, true);
 		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
 		mapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS,
 				false);
 		mapper.configure(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS,
 				false);
 		mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING,
 				false);
 		mapper.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, false);
 		mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true);
 		mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
 		mapper.configure(
 				SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, false);
 		mapper.configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, false);
 		mapper.configure(
 				SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS,
 				false);
 		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
 		mapper.configure(SerializationFeature.EAGER_SERIALIZER_FETCH, true);
 
 		mapper.registerModule(zoneResourceRecordModule());
 		mapper.registerModule(nameServerInterfaceModule());
 		return mapper;
 	}
 
 	private SimpleModule zoneResourceRecordModule() {
		return new ZoneResourceRecordModule("ZoneResourceRecordModule");
 	}
 
	private SimpleModule nameServerInterfaceModule() {
 		SimpleModule module = new SimpleModule("NameServerInterfaceModule");
 
 		module.addDeserializer(NameServerInterface.class,
 				nameServerInterfaceDeserializer());
 
 		module.addSerializer(NameServerInterface.class,
 				nameServerInterfaceSerializer());
 
 		return module;
 	}
 
	private NameServerInterfaceDeserializer nameServerInterfaceDeserializer() {
 		return new NameServerInterfaceDeserializer();
 	}
 
	private NameServerInterfaceSerializer nameServerInterfaceSerializer() {
 		return new NameServerInterfaceSerializer();
 	}
 
 }
