 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.persist;
 
 import java.sql.SQLException;
 
 import se.chalmers.dat255.sleepfighter.utils.math.Conversion;
 
 import com.j256.ormlite.field.FieldType;
 import com.j256.ormlite.field.SqlType;
 import com.j256.ormlite.field.types.BaseDataType;
 import com.j256.ormlite.support.DatabaseResults;
 
 /**
  * Defines how to handle a boolean array for OrmLite.<br/>
  * Stored as an integer in database.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Sep 23, 2013
  */
 public class BooleanArrayType extends BaseDataType {
 	private static Class<?> clazz = new boolean[0].getClass();
 	private static final BooleanArrayType singleton = new BooleanArrayType();
 	private static final String[] associatedClassNames = new String[] { clazz.getName() };
 
 	private BooleanArrayType() {
 		super(SqlType.INTEGER, new Class<?>[] {clazz});
 	}
 
 	public static BooleanArrayType getSingleton() {
 		return singleton;
 	}
 
 	@Override
 	public String[] getAssociatedClassNames() {
		return associatedClassNames;
 	}
 
 	@Override
 	public Class<?> getPrimaryClass() {
 		return clazz;
 	}
 
 	@Override
 	public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
 		return Conversion.boolArrayToInt( (boolean[]) javaObject );
 	}
 
 	@Override
 	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
 		return Conversion.intToBoolArray( (Integer) sqlArg, fieldType.getWidth() );
 	}
 
 	@Override
 	public Object parseDefaultString(FieldType fieldType, String defaultStr) {
 		return Conversion.intToBoolArray( Integer.parseInt( defaultStr ), fieldType.getWidth() );
 	}
 
 	@Override
 	public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
 		return results.getInt(columnPos);
 	}
 
 	@Override
 	public boolean isEscapedValue() {
 		return false;
 	}
 
 	@Override
 	public boolean isAppropriateId() {
 		return false;
 	}
 }
