 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jtoodle.api.beans;
 
 import com.fasterxml.jackson.core.Version;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.module.SimpleModule;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import jtoodle.api.json.deser.TD_BooleanDeserializer;
 import jtoodle.api.json.deser.TD_EnumDeserializer;
 import jtoodle.api.json.deser.TD_ReminderTimeDeserializer;
 import jtoodle.api.json.deser.TD_UnixDateDeserializer;
 import jtoodle.api.json.enums.DateFormat;
 import jtoodle.api.json.enums.DueDateModifier;
 import jtoodle.api.json.enums.Priority;
 import jtoodle.api.json.enums.ReminderTime;
 import jtoodle.api.json.enums.Status;
 import jtoodle.api.json.mixin.AccountInfoMixIn;
 import jtoodle.api.json.mixin.FolderMixIn;
 import jtoodle.api.json.mixin.JToodleExceptionMixIn;
 import jtoodle.api.json.mixin.TaskMixIn;
 import jtoodle.api.json.mixin.TaskQueryResultMixIn;
 import jtoodle.api.json.mixin.TokenMixIn;
 import jtoodle.api.json.mixin.UserIdMixIn;
 
 /**
  *
  * @author justo
  */
 public class BeanParser {
 
 	private BeanParser() {}
 
 	private static final ObjectMapper mapper = new ObjectMapper();
 
 	static {
 		mapper.addMixInAnnotations( JToodleException.class, JToodleExceptionMixIn.class );
 		mapper.addMixInAnnotations( UserId.class, UserIdMixIn.class );
 		mapper.addMixInAnnotations( Token.class, TokenMixIn.class );
 		mapper.addMixInAnnotations( AccountInfo.class, AccountInfoMixIn.class );
 		mapper.addMixInAnnotations( Folder.class, FolderMixIn.class );
 		mapper.addMixInAnnotations( TaskQueryResult.class, TaskQueryResultMixIn.class );
 		mapper.addMixInAnnotations( Task.class, TaskMixIn.class );
 
 		mapper.registerModule(
 			new SimpleModule( "TD_DeserializationModule", Version.unknownVersion() )
 				.addDeserializer( Boolean.class, new TD_BooleanDeserializer() )
 				.addDeserializer( Date.class, new TD_UnixDateDeserializer() )
				.addDeserializer( DateFormat.class, new TD_EnumDeserializer<>( DateFormat.class ) )
				.addDeserializer( Priority.class, new TD_EnumDeserializer<>( Priority.class ) )
				.addDeserializer( Status.class, new TD_EnumDeserializer<>( Status.class ) )
				.addDeserializer( DueDateModifier.class, new TD_EnumDeserializer<>( DueDateModifier.class ) )
 				.addDeserializer( ReminderTime.class, new TD_ReminderTimeDeserializer() )
 		);
 
 	}
 
 	public static <T extends AbstractJToodleBean> T parseBean( String js, Class<T> clazz )
 	throws IOException, JToodleException {
 		throwJToodleExceptionIfError( js );
 		return( mapper.readValue( js, clazz ) );
 	}
 
 	public static <T extends AbstractJToodleBean> List<T> parseBeanList( String js, Class<T> clazz )
 	throws IOException, JToodleException {
 		throwJToodleExceptionIfError( js );
 		return( mapper.readValue( js, mapper.getTypeFactory().constructCollectionType( List.class, clazz ) ) );
 	}
 
 	private static void throwJToodleExceptionIfError( String js ) throws IOException, JToodleException {
 		if( js.contains( "errorCode" ) ) {
 			JToodleException jte = mapper.readValue( js, JToodleException.class );
 			throw( jte );
 		}
 	}
 
 	public static TaskQueryResult parseTaskQueryResults( String js )
 	throws IOException, JToodleException {
 		throwJToodleExceptionIfError( js );
 
 		TaskQueryResult results = null;
 		List<Task> tasks = new ArrayList<>();
 
		List<LinkedHashMap> values = mapper.readValue(
			js, mapper.getTypeFactory().constructCollectionType(
				List.class, LinkedHashMap.class ) );
 
 		for( LinkedHashMap map : values ) {
 			if( map.containsKey( "num" ) ) {
 				results = mapper.convertValue( map, TaskQueryResult.class );
 			} else if( map.containsKey( "id" ) ) {
 				tasks.add( mapper.convertValue( map, Task.class ) );
 			}
 		}
 
 		if( results != null ) {
 			results.setTasks( tasks );
 		}
 
 		return( results );
 	}
 }
